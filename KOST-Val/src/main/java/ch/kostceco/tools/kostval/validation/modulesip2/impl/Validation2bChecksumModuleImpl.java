/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// import org.apache.xpath.XPathAPI; 
//wird nicht mehr verwendet, wenn Zeitoptimiert
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.w3c.dom.traversal.NodeIterator;
//wird nicht mehr verwendet, wenn Zeitoptimiert

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2bChecksumException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2bChecksumModule;
import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */

public class Validation2bChecksumModuleImpl extends ValidationModuleImpl
		implements Validation2bChecksumModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation2bChecksumException
	{

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		boolean valid = true;
		FileEntry metadataxml = null;
		Map<String, String> filesInSipFileMD5 = new HashMap<String, String>();
		Map<String, String> filesInSipFileSHA1 = new HashMap<String, String>();
		Map<String, String> filesInSipFileSHA256 = new HashMap<String, String>();
		Map<String, String> filesInSipFileSHA512 = new HashMap<String, String>();
		Map<String, String> filesInMetadataMD5 = new HashMap<String, String>();
		Map<String, String> filesInMetadataSHA1 = new HashMap<String, String>();
		Map<String, String> filesInMetadataSHA256 = new HashMap<String, String>();
		Map<String, String> filesInMetadataSHA512 = new HashMap<String, String>();

		try {
			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();

			filesInSipFileMD5.put( "", "" );
			filesInSipFileSHA1.put( "", "" );
			filesInSipFileSHA256.put( "", "" );

			for ( FileEntry fileEntry : fileEntryList ) {
				if ( fileEntry.getName().equals( "header/" + METADATA )
						|| fileEntry.getName().equals(
								toplevelDir + "/header/" + METADATA ) ) {
					metadataxml = fileEntry;
				}
			}

			// keine metadata.xml in der SIP-Datei gefunden
			if ( metadataxml == null ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bb_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_AE_NOMETADATAFOUND ) );
				return false;
			}

			// Lesen der metadata.xml im Bereich name, pruefsumme und
			// pruefalgorithmus inkl.erste Tests
			EntryInputStream eis = zipfile.openEntryInputStream( metadataxml
					.getName() );
			BufferedInputStream is = new BufferedInputStream( eis );

			// Integer count = 0;

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( is );
				doc.normalize();
				NodeList nodeLst = doc.getElementsByTagName( "datei" );

				for ( int s = 0; s < nodeLst.getLength(); s++ ) {
					String pruefsumme = null;
					String pruefalgorithmus = null;
					String path = null;

					/*
					 * Temp-Ausgabe verwendet zur Zeitoptimierung count = count
					 * + 1; java.util.Date now = new java.util.Date();
					 * java.text.SimpleDateFormat sdf = new
					 * java.text.SimpleDateFormat( "dd.MM.yyyy HH.mm.ss" );
					 * String ausgabe = sdf.format( now ); System.out.print(
					 * ausgabe + "  -->  Zaehler " + count + "  " );
					 */

					Node dateiNode = nodeLst.item( s );
					// System.out.println(dateiNode.getTextContent());

					NodeList childNodes = dateiNode.getChildNodes();
					for ( int y = 0; y < childNodes.getLength(); y++ ) {
						Node subNode = childNodes.item( y );
						if ( subNode.getNodeName().equals( "pruefsumme" ) ) {
							// System.out.println("pruefsumme gefunden --> " +
							// subNode.getTextContent());
							pruefsumme = subNode.getTextContent();
						} else if ( subNode.getNodeName().equals(
								"pruefalgorithmus" ) ) {
							// System.out.println("pruefalgorithmus gefunden --> "
							// + subNode.getTextContent());
							pruefalgorithmus = subNode.getTextContent();
						} else if ( subNode.getNodeName().equals( "name" ) ) {
							// System.out.println("name gefunden --> " +
							// subNode.getTextContent());
							path = subNode.getTextContent();
						}
					}

					// selectNodeIterator ist zu Zeitintensiv bei grossen
					// XML-Dateien mit getChildNodes() ersetzt

					/*
					 * NodeIterator nl2 = XPathAPI.selectNodeIterator(
					 * dateiNode, "pruefsumme" ); Node pruefsummeNode =
					 * nl2.nextNode(); String pruefsumme =
					 * pruefsummeNode.getTextContent();
					 * 
					 * NodeIterator nl3 = XPathAPI.selectNodeIterator(
					 * dateiNode, "pruefalgorithmus" ); Node
					 * pruefalgorithmusNode = nl3.nextNode(); String
					 * pruefalgorithmus = pruefalgorithmusNode
					 * .getTextContent();
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
						if ( parentNodePath.getNodeName().equals(
								"inhaltsverzeichnis" ) ) {
							topReachedPath = true;
							break;
						}

						NodeList childrenNodesPath = parentNodePath
								.getChildNodes();
						for ( int x = 0; x < childrenNodesPath.getLength(); x++ ) {
							Node childNodePath = childrenNodesPath.item( x );

							if ( childNodePath.getNodeName().equals( "name" ) ) {
								path = childNodePath.getTextContent() + "/"
										+ path;
								if ( dateiNode.getParentNode() != null ) {
									dateiNode = dateiNode.getParentNode();
								}
								break;
							}
						}
					}

					if ( pruefalgorithmus.equals( pruefalgorithmusMD5 ) ) {
						// pruefalgorithmus ist MD5 und wird auf die korrekte
						// Länge (=32) überprüft

						if ( pruefsumme.length() != 32 ) {
							valid = false;

							getMessageService().logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bb_SIP )
											+ getTextResourceService().getText(
													MESSAGE_XML_Bb_WRONGMD5,
													path ) );

						} else {

							// Start MD5 berechnung

							for ( FileEntry fileEntry : fileEntryList ) {
								String fileName = fileEntry.getName();
								String toReplace = toplevelDir + "/";
								fileName = fileName.replace( toReplace, "" );

								if ( fileName.equals( path ) ) {

									filesInMetadataMD5.put( path, pruefsumme );

									MessageDigest digest = MessageDigest
											.getInstance( "MD5" );
									EntryInputStream eis2 = zipfile
											.openEntryInputStream( fileEntry
													.getName() );
									BufferedInputStream is2 = new BufferedInputStream(
											eis2 );

									byte[] buffer = new byte[8192];
									int read = 0;
									try {
										while ( (read = is2.read( buffer )) > 0 ) {
											digest.update( buffer, 0, read );
										}

										byte[] md5sum = digest.digest();

										BigInteger bigInt = new BigInteger( 1,
												md5sum );
										String output = bigInt.toString( 16 );

										while ( output.length() < 32 ) {
											output = "0" + output;
										}

										filesInSipFileMD5
												.put( fileName, output );

									} catch ( IOException e ) {
										getMessageService()
												.logError(
														getTextResourceService()
																.getText(
																		MESSAGE_XML_MODUL_Bb_SIP )
																+ getTextResourceService()
																		.getText(
																				ERROR_XML_BB_CANNOTPROCESSMD5 ) );
										return false;

									} finally {
										try {
											eis2.close();
											is2.close();
										} catch ( IOException e ) {
											getMessageService()
													.logError(
															getTextResourceService()
																	.getText(
																			MESSAGE_XML_MODUL_Bb_SIP )
																	+ getTextResourceService()
																			.getText(
																					ERROR_XML_BB_CANNOTCLOSESTREAMMD5 ) );
											return false;
										}
									}
									// Ende MD5 Berechnung
								} else {
									// Die Datei befindet sich nicht im SIP ->
									// Kein Fehler da in 2a bereits ausgegeben
								}
							}
						}

					}
					if ( pruefalgorithmus.equals( pruefalgorithmusSHA1 ) ) {
						// pruefalgorithmus ist SHA1 und wird auf die korrekte
						// Länge (=40) überprüft

						if ( pruefsumme.length() != 40 ) {
							valid = false;

							getMessageService().logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bb_SIP )
											+ getTextResourceService().getText(
													MESSAGE_XML_Bb_WRONGMD5,
													path ) );

						} else {

							// Start SHA1 berechnung
							for ( FileEntry fileEntry : fileEntryList ) {
								String fileName = fileEntry.getName();
								String toReplace = toplevelDir + "/";
								fileName = fileName.replace( toReplace, "" );

								if ( fileName.equals( path ) ) {

									filesInMetadataSHA1.put( path, pruefsumme );

									MessageDigest digest = MessageDigest
											.getInstance( "SHA-1" );
									EntryInputStream eis3 = zipfile
											.openEntryInputStream( fileEntry
													.getName() );
									BufferedInputStream is3 = new BufferedInputStream(
											eis3 );

									byte[] buffer = new byte[8192];
									int read = 0;
									try {
										while ( (read = is3.read( buffer )) > 0 ) {
											digest.update( buffer, 0, read );
										}

										byte[] sha1sum = digest.digest();

										BigInteger bigInt = new BigInteger( 1,
												sha1sum );
										String output = bigInt.toString( 16 );

										while ( output.length() < 40 ) {
											output = "0" + output;
										}

										filesInSipFileSHA1.put( fileName,
												output );

									} catch ( IOException e ) {
										getMessageService()
												.logError(
														getTextResourceService()
																.getText(
																		MESSAGE_XML_MODUL_Bb_SIP )
																+ getTextResourceService()
																		.getText(
																				ERROR_XML_BB_CANNOTPROCESSMD5 ) );
										return false;

									} finally {
										try {
											eis3.close();
											is3.close();
										} catch ( IOException e ) {
											getMessageService()
													.logError(
															getTextResourceService()
																	.getText(
																			MESSAGE_XML_MODUL_Bb_SIP )
																	+ getTextResourceService()
																			.getText(
																					ERROR_XML_BB_CANNOTCLOSESTREAMMD5 ) );
											return false;
										}
									}
									// Ende SHA1 Berechnung
								} else {
									// Die Datei befindet sich nicht im SIP ->
									// Kein Fehler da in 2a bereits ausgegeben
								}
							}
						}

					}

					if ( pruefalgorithmus.equals( pruefalgorithmusSHA256 ) ) {
						// pruefalgorithmus ist SHA256 und wird auf die korrekte
						// Länge (=64) überprüft

						if ( pruefsumme.length() != 64 ) {
							valid = false;

							getMessageService().logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bb_SIP )
											+ getTextResourceService().getText(
													MESSAGE_XML_Bb_WRONGMD5,
													path ) );

						} else {

							// Start SHA256 berechnung
							for ( FileEntry fileEntry : fileEntryList ) {
								String fileName = fileEntry.getName();
								String toReplace = toplevelDir + "/";
								fileName = fileName.replace( toReplace, "" );

								if ( fileName.equals( path ) ) {

									filesInMetadataSHA256
											.put( path, pruefsumme );

									MessageDigest digest = MessageDigest
											.getInstance( "SHA-256" );
									EntryInputStream eis4 = zipfile
											.openEntryInputStream( fileEntry
													.getName() );
									BufferedInputStream is4 = new BufferedInputStream(
											eis4 );

									byte[] buffer = new byte[8192];
									int read = 0;
									try {
										while ( (read = is4.read( buffer )) > 0 ) {
											digest.update( buffer, 0, read );
										}

										byte[] sha256sum = digest.digest();

										BigInteger bigInt = new BigInteger( 1,
												sha256sum );
										String output = bigInt.toString( 16 );

										while ( output.length() < 64 ) {
											output = "0" + output;
										}

										filesInSipFileSHA256.put( fileName,
												output );

									} catch ( IOException e ) {
										getMessageService()
												.logError(
														getTextResourceService()
																.getText(
																		MESSAGE_XML_MODUL_Bb_SIP )
																+ getTextResourceService()
																		.getText(
																				ERROR_XML_BB_CANNOTPROCESSMD5 ) );
										return false;

									} finally {
										try {
											eis4.close();
											is4.close();
										} catch ( IOException e ) {
											getMessageService()
													.logError(
															getTextResourceService()
																	.getText(
																			MESSAGE_XML_MODUL_Bb_SIP )
																	+ getTextResourceService()
																			.getText(
																					ERROR_XML_BB_CANNOTCLOSESTREAMMD5 ) );

											return false;
										}
									}
									// Ende SHA256 Berechnung
								} else {
									// Die Datei befindet sich nicht im SIP ->
									// Kein Fehler da in 2a bereits ausgegeben
								}
							}
						}

					}

					if ( pruefalgorithmus.equals( pruefalgorithmusSHA512 ) ) {
						// pruefalgorithmus ist SHA512 und wird auf die korrekte
						// Länge (=128) überprüft

						if ( pruefsumme.length() != 128 ) {
							valid = false;

							getMessageService().logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bb_SIP )
											+ getTextResourceService().getText(
													MESSAGE_XML_Bb_WRONGMD5,
													path ) );

						} else {

							// Start SHA512 berechnung
							for ( FileEntry fileEntry : fileEntryList ) {
								String fileName = fileEntry.getName();
								String toReplace = toplevelDir + "/";
								fileName = fileName.replace( toReplace, "" );

								if ( fileName.equals( path ) ) {

									filesInMetadataSHA512
											.put( path, pruefsumme );

									MessageDigest digest = MessageDigest
											.getInstance( "SHA-512" );
									EntryInputStream eis5 = zipfile
											.openEntryInputStream( fileEntry
													.getName() );
									BufferedInputStream is5 = new BufferedInputStream(
											eis5 );

									byte[] buffer = new byte[8192];
									int read = 0;
									try {
										while ( (read = is5.read( buffer )) > 0 ) {
											digest.update( buffer, 0, read );
										}

										byte[] sha512sum = digest.digest();

										BigInteger bigInt = new BigInteger( 1,
												sha512sum );
										String output = bigInt.toString( 16 );

										while ( output.length() < 128 ) {
											output = "0" + output;
										}

										filesInSipFileSHA512.put( fileName,
												output );

									} catch ( IOException e ) {
										getMessageService()
												.logError(
														getTextResourceService()
																.getText(
																		MESSAGE_XML_MODUL_Bb_SIP )
																+ getTextResourceService()
																		.getText(
																				ERROR_XML_BB_CANNOTPROCESSMD5 ) );
										return false;

									} finally {
										try {
											eis5.close();
											is5.close();
										} catch ( IOException e ) {
											getMessageService()
													.logError(
															getTextResourceService()
																	.getText(
																			MESSAGE_XML_MODUL_Bb_SIP )
																	+ getTextResourceService()
																			.getText(
																					ERROR_XML_BB_CANNOTCLOSESTREAMMD5 ) );
											return false;
										}
									}
									// Ende SHA512 Berechnung
								} else {
									// Die Datei befindet sich nicht im SIP ->
									// Kein Fehler da in 2a bereits ausgegeben
								}
							}
						}

					}

					boolean topReached2 = false;

					while ( !topReached2 ) {

						Node parentNode = dateiNode.getParentNode();
						if ( parentNode.getNodeName().equals(
								"inhaltsverzeichnis" ) ) {
							topReached2 = true;
							break;
						}

						NodeList childrenNodes = parentNode.getChildNodes();
						for ( int x = 0; x < childrenNodes.getLength(); x++ ) {
							Node childNode = childrenNodes.item( x );

							if ( childNode.getNodeName().equals( "name" ) ) {
								path = childNode.getTextContent() + "/" + path;
								if ( dateiNode.getParentNode() != null ) {
									dateiNode = dateiNode.getParentNode();
								}
								break;
							}
						}
					}
				}

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bb_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

			String pruefsummeMetadataMD5 = "";
			String pruefsummeMetadataSHA1 = "";
			String pruefsummeMetadataSHA256 = "";

			// Vergleichen der pruefsumme je nach pruefalgorithmus = MD5 in
			// metadata.xml
			Set<String> keysMD5 = filesInMetadataMD5.keySet();
			for ( Iterator<String> iteratorMD5 = keysMD5.iterator(); iteratorMD5
					.hasNext(); ) {
				String keyMetadataMD5 = iteratorMD5.next();
				pruefsummeMetadataMD5 = filesInMetadataMD5.get( keyMetadataMD5 );
				String pruefsummeSipMD5 = filesInSipFileMD5
						.get( keyMetadataMD5 );

				if ( pruefsummeMetadataMD5 != null ) {
					if ( !pruefsummeSipMD5
							.equalsIgnoreCase( pruefsummeMetadataMD5 ) ) {
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Bb_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_Bb_WRONGMD5,
												keyMetadataMD5 ) );
						valid = false;
					}

				}

			}

			// Vergleichen der pruefsumme je nach pruefalgorithmus = SHA1 in
			// metadata.xml
			Set<String> keysSHA1 = filesInMetadataSHA1.keySet();
			for ( Iterator<String> iteratorSHA1 = keysSHA1.iterator(); iteratorSHA1
					.hasNext(); ) {
				String keyMetadataSHA1 = iteratorSHA1.next();
				pruefsummeMetadataSHA1 = filesInMetadataSHA1
						.get( keyMetadataSHA1 );
				String pruefsummeSipSHA1 = filesInSipFileSHA1
						.get( keyMetadataSHA1 );

				if ( pruefsummeMetadataSHA1 != null ) {
					if ( !pruefsummeSipSHA1
							.equalsIgnoreCase( pruefsummeMetadataSHA1 ) ) {
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Bb_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_Bb_WRONGMD5,
												keyMetadataSHA1 ) );
						valid = false;
					}

				}

			}

			// Vergleichen der pruefsumme je nach pruefalgorithmus = SHA256 in
			// metadata.xml
			Set<String> keysSHA256 = filesInMetadataSHA256.keySet();
			for ( Iterator<String> iteratorSHA256 = keysSHA256.iterator(); iteratorSHA256
					.hasNext(); ) {
				String keyMetadataSHA256 = iteratorSHA256.next();
				pruefsummeMetadataSHA256 = filesInMetadataSHA256
						.get( keyMetadataSHA256 );
				String pruefsummeSipSHA256 = filesInSipFileSHA256
						.get( keyMetadataSHA256 );

				if ( pruefsummeMetadataSHA256 != null ) {
					if ( !pruefsummeSipSHA256
							.equalsIgnoreCase( pruefsummeMetadataSHA256 ) ) {
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Bb_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_Bb_WRONGMD5,
												keyMetadataSHA256 ) );
						valid = false;
					}
				}
			}

			zipfile.close();
			is.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Bb_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;
	}
}
