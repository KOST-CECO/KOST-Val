﻿/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath,
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
 * -----------------------------------------------------------------------------------------------
 * KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all copyright
 * interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March
 * 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2cChecksumException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2cChecksumModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt 2c: Stimmen die Pr�fsummen der Dateien mit Pr�fsumme
 * überein? metadata.xml: pruefsumme, pruefalgorithmus und name pro Datei
 * auslesen pfad ermitteln, l�nge der summe kontrollieren datei: Summe berechnen
 * und vergleichen
 */

public class Validation2cChecksumModuleImpl extends ValidationModuleImpl
		implements Validation2cChecksumModule
{

	private ConfigurationService configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws Validation2cChecksumException
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "2C   " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean valid = true;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream( new File(
					valDatei.getAbsolutePath() + "//header//metadata.xml" ) ) );
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName( "datei" );

			for ( int s = 0; s < nodeLst.getLength(); s++ ) {
				String pruefsumme = null;
				String pruefalgorithmus = null;
				String path = null;

				/*
				 * Temp-Ausgabe verwendet zur Zeitoptimierung count = count + 1;
				 * java.util.Date now = new java.util.Date();
				 * java.text.SimpleDateFormat sdf = new
				 * java.text.SimpleDateFormat( "dd.MM.yyyy HH.mm.ss" ); String
				 * ausgabe = sdf.format( now ); System.out.print( ausgabe +
				 * "  -->  Zaehler " + count + "  " );
				 */

				Node dateiNode = nodeLst.item( s );
				// System.out.println(dateiNode.getTextContent());

				NodeList childNodes = dateiNode.getChildNodes();
				for ( int y = 0; y < childNodes.getLength(); y++ ) {
					Node subNode = childNodes.item( y );
					if ( subNode.getNodeName().equals( "pruefsumme" ) ) {
						pruefsumme = subNode.getTextContent();
					} else if ( subNode.getNodeName()
							.equals( "pruefalgorithmus" ) ) {
						pruefalgorithmus = subNode.getTextContent();
					} else if ( subNode.getNodeName().equals( "name" ) ) {
						path = subNode.getTextContent();
					}
				}

				// selectNodeIterator ist zu Zeitintensiv bei grossen
				// XML-Dateien mit getChildNodes()
				// ersetzt

				/*
				 * NodeIterator nl2 = XPathAPI.selectNodeIterator( dateiNode,
				 * "pruefsumme" ); Node pruefsummeNode = nl2.nextNode(); String
				 * pruefsumme = pruefsummeNode.getTextContent();
				 * 
				 * NodeIterator nl3 = XPathAPI.selectNodeIterator( dateiNode,
				 * "pruefalgorithmus" ); Node pruefalgorithmusNode =
				 * nl3.nextNode(); String pruefalgorithmus =
				 * pruefalgorithmusNode .getTextContent();
				 * 
				 * NodeIterator nl = XPathAPI.selectNodeIterator( dateiNode,
				 * "name" ); Node nameNode = nl.nextNode(); String path =
				 * nameNode.getTextContent();
				 */

				String pruefalgorithmusMD5 = "MD5";
				String pruefalgorithmusSHA1 = "SHA-1";
				String pruefalgorithmusSHA256 = "SHA-256";
				String pruefalgorithmusSHA512 = "SHA-512";

				boolean topReachedPath = false;

				while ( !topReachedPath ) {

					Node parentNodePath = dateiNode.getParentNode();
					if ( parentNodePath.getNodeName()
							.equals( "inhaltsverzeichnis" ) ) {
						topReachedPath = true;
						break;
					}

					NodeList childrenNodesPath = parentNodePath.getChildNodes();
					for ( int x = 0; x < childrenNodesPath.getLength(); x++ ) {
						Node childNodePath = childrenNodesPath.item( x );

						if ( childNodePath.getNodeName().equals( "name" ) ) {
							path = childNodePath.getTextContent() + "/" + path;
							if ( dateiNode.getParentNode() != null ) {
								dateiNode = dateiNode.getParentNode();
							}
							break;
						}
					}
				}

				if ( pruefalgorithmus.equals( pruefalgorithmusMD5 ) ) {
					// pruefalgorithmus ist MD5 und wird auf die korrekte L�nge
					// (=32) �berpr�ft

					if ( pruefsumme.length() != 32 ) {
						valid = false;

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_BC_WRONGMD5, path ) );

					} else {
						// Start MD5 Berechnung
						File fileInSip = new File(
								valDatei.getAbsolutePath() + "//" + path );
						if ( fileInSip.exists() ) {
							try {
								String filepath = fileInSip.getAbsolutePath();
								MessageDigest messageDigest = MessageDigest
										.getInstance( "MD5" );

								FileInputStream fileInput = new FileInputStream(
										filepath );
								byte[] dataBytes = new byte[1024];
								int bytesRead = 0;
								while ( (bytesRead = fileInput
										.read( dataBytes )) != -1 ) {
									messageDigest.update( dataBytes, 0,
											bytesRead );
								}
								byte[] digestBytes = messageDigest.digest();
								StringBuffer sb = new StringBuffer( "" );

								for ( int i = 0; i < digestBytes.length; i++ ) {
									sb.append( Integer
											.toString( (digestBytes[i] & 0xff)
													+ 0x100, 16 )
											.substring( 1 ) );
								}
								String output = sb.toString();
								while ( output.length() < 32 ) {
									output = "0" + output;
								}

								try {
									fileInput.close();
								} catch ( IOException e ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_BC_CANNOTCLOSESTREAMMD5 ) );
									return false;
								}

								// Vergleich der Pr�fsummen
								if ( output.equalsIgnoreCase( pruefsumme ) ) {
									// identisch :-)
								} else {
									valid = false;

									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_BC_WRONGMD5,
																	path + ": "
																			+ pruefsumme
																			+ " ~ "
																			+ output ) );
								}
							} catch ( IOException e ) {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Bc_SIP )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_BC_CANNOTPROCESSMD5 ) );
								return false;
							}
						} else {
							// ( "Die Datei befindet sich nicht im SIP ");
							// Kein Fehler da in 2a bereits ausgegeben
						}
					}
				}
				if ( pruefalgorithmus.equals( pruefalgorithmusSHA1 ) ) {
					// pruefalgorithmus ist SHA1 und wird auf die korrekte L�nge
					// (=40) �berpr�ft

					if ( pruefsumme.length() != 40 ) {
						valid = false;

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_BC_WRONGMD5, path ) );

					} else {
						// Start SHA-1 Berechnung
						File fileInSip = new File(
								valDatei.getAbsolutePath() + "//" + path );
						if ( fileInSip.exists() ) {
							try {
								String filepath = fileInSip.getAbsolutePath();
								MessageDigest messageDigest = MessageDigest
										.getInstance( "SHA-1" );

								FileInputStream fileInput = new FileInputStream(
										filepath );
								byte[] dataBytes = new byte[1024];
								int bytesRead = 0;
								while ( (bytesRead = fileInput
										.read( dataBytes )) != -1 ) {
									messageDigest.update( dataBytes, 0,
											bytesRead );
								}
								byte[] digestBytes = messageDigest.digest();
								StringBuffer sb = new StringBuffer( "" );

								for ( int i = 0; i < digestBytes.length; i++ ) {
									sb.append( Integer
											.toString( (digestBytes[i] & 0xff)
													+ 0x100, 16 )
											.substring( 1 ) );
								}
								String output = sb.toString();
								while ( output.length() < 40 ) {
									output = "0" + output;
								}

								try {
									fileInput.close();
								} catch ( IOException e ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_BC_CANNOTCLOSESTREAMMD5 ) );
									return false;
								}

								// Vergleich der Pr�fsummen
								if ( !output.equalsIgnoreCase( pruefsumme ) ) {
									valid = false;

									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_BC_WRONGMD5,
																	path + ": "
																			+ pruefsumme
																			+ " ~ "
																			+ output ) );
								}
							} catch ( IOException e ) {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Bc_SIP )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_BC_CANNOTPROCESSMD5 ) );
								return false;
							}
						} else {
							// ( "Die Datei befindet sich nicht im SIP ");
							// Kein Fehler da in 2a bereits ausgegeben
						}
					}
				}

				if ( pruefalgorithmus.equals( pruefalgorithmusSHA256 ) ) {
					// pruefalgorithmus ist SHA256 und wird auf die korrekte
					// L�nge (=64) �berpr�ft

					if ( pruefsumme.length() != 64 ) {
						valid = false;

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_BC_WRONGMD5, path ) );

					} else {
						// Start SHA-256 Berechnung
						File fileInSip = new File(
								valDatei.getAbsolutePath() + "//" + path );
						if ( fileInSip.exists() ) {
							try {
								String filepath = fileInSip.getAbsolutePath();
								MessageDigest messageDigest = MessageDigest
										.getInstance( "SHA-256" );

								FileInputStream fileInput = new FileInputStream(
										filepath );
								byte[] dataBytes = new byte[1024];
								int bytesRead = 0;
								while ( (bytesRead = fileInput
										.read( dataBytes )) != -1 ) {
									messageDigest.update( dataBytes, 0,
											bytesRead );
								}
								byte[] digestBytes = messageDigest.digest();
								StringBuffer sb = new StringBuffer( "" );

								for ( int i = 0; i < digestBytes.length; i++ ) {
									sb.append( Integer
											.toString( (digestBytes[i] & 0xff)
													+ 0x100, 16 )
											.substring( 1 ) );
								}
								String output = sb.toString();
								while ( output.length() < 64 ) {
									output = "0" + output;
								}

								try {
									fileInput.close();
								} catch ( IOException e ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_BC_CANNOTCLOSESTREAMMD5 ) );
									return false;
								}

								// Vergleich der Pr�fsummen
								if ( !output.equalsIgnoreCase( pruefsumme ) ) {
									valid = false;

									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_BC_WRONGMD5,
																	path + ": "
																			+ pruefsumme
																			+ " ~ "
																			+ output ) );
								}
							} catch ( IOException e ) {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Bc_SIP )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_BC_CANNOTPROCESSMD5 ) );
								return false;
							}
						} else {
							// ( "Die Datei befindet sich nicht im SIP ");
							// Kein Fehler da in 2a bereits ausgegeben
						}
					}
				}

				if ( pruefalgorithmus.equals( pruefalgorithmusSHA512 ) ) {
					// pruefalgorithmus ist SHA512 und wird auf die korrekte
					// L�nge (=128) �berpr�ft

					if ( pruefsumme.length() != 128 ) {
						valid = false;

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_BC_WRONGMD5, path ) );

					} else {
						// Start SHA-512 Berechnung
						File fileInSip = new File(
								valDatei.getAbsolutePath() + "//" + path );
						if ( fileInSip.exists() ) {
							try {
								String filepath = fileInSip.getAbsolutePath();
								MessageDigest messageDigest = MessageDigest
										.getInstance( "SHA-512" );

								FileInputStream fileInput = new FileInputStream(
										filepath );
								byte[] dataBytes = new byte[1024];
								int bytesRead = 0;
								while ( (bytesRead = fileInput
										.read( dataBytes )) != -1 ) {
									messageDigest.update( dataBytes, 0,
											bytesRead );
								}
								byte[] digestBytes = messageDigest.digest();
								StringBuffer sb = new StringBuffer( "" );

								for ( int i = 0; i < digestBytes.length; i++ ) {
									sb.append( Integer
											.toString( (digestBytes[i] & 0xff)
													+ 0x100, 16 )
											.substring( 1 ) );
								}
								String output = sb.toString();
								while ( output.length() < 128 ) {
									output = "0" + output;
								}

								try {
									fileInput.close();
								} catch ( IOException e ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_BC_CANNOTCLOSESTREAMMD5 ) );
									return false;
								}

								// Vergleich der Pr�fsummen
								if ( !output.equalsIgnoreCase( pruefsumme ) ) {
									valid = false;

									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Bc_SIP )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_BC_WRONGMD5,
																	path + ": "
																			+ pruefsumme
																			+ " ~ "
																			+ output ) );
								}
							} catch ( IOException e ) {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Bc_SIP )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_BC_CANNOTPROCESSMD5 ) );
								return false;
							}
						} else {
							// ( "Die Datei befindet sich nicht im SIP ");
							// Kein Fehler da in 2a bereits ausgegeben
						}
					}
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "2C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "2C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "2C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "2C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Bc_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			valid = false;
		}
		return valid;
	}
}
