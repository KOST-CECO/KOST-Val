/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ch.kostceco.tools.kosttools.fileservice.Xmllint;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;

public class Validation1dMetadataModuleImpl extends ValidationModuleImpl
		implements Validation1dMetadataModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	final int							BUFFER	= 2048;

	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws Validation1dMetadataException
	{
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File pathToWorkDirFile = new File( pathToWorkDir + File.separator + "header" );
		try {
			if ( !pathToWorkDirFile.exists() ) {
				// System.out.println( pathToWorkDirFile.getAbsolutePath() );
				pathToWorkDirFile.mkdirs();
				Thread.sleep( 1000 );
			}
			File valDateiHeader = new File( valDatei.getAbsolutePath() + File.separator + "header" );
			Util.copyDir( valDateiHeader, pathToWorkDirFile );
			Thread.sleep( 10000 );
		} catch ( FileNotFoundException e1 ) {
			e1.printStackTrace();
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWork.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "1D   " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean result = true;
		String sipVer = "ECH160_1.0.txt";
		File sipVersionFile;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream(
					new File( pathToWorkDirFile.getAbsolutePath() + File.separator + "metadata.xml" ) ) );
			doc.getDocumentElement().normalize();

			BufferedReader in = new BufferedReader( new FileReader(
					new File( pathToWorkDirFile.getAbsolutePath() + File.separator + "metadata.xml" ) ) );
			StringBuffer concatenatedOutputs = new StringBuffer();
			String line;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );
				/* Kontrollieren, dass kein Namespace verwendet wurde wie z.B. v4:
				 * 
				 * <?xml version="1.0" encoding="UTF-8"?> <v4:paket schemaVersion="4.1"
				 * xsi:type="v4:paketSIP" xmlns:v4="http://bar.admin.ch/arelda/v4"
				 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <v4:paketTyp>SIP</v4:paketTyp>
				 * <v4:inhaltsverzeichnis> */
				if ( line.contains( "paketTyp>" ) ) {
					if ( !line.contains( "<paketTyp>" ) ) {
						// Invalider Status
						int start = line.indexOf( "<" ) + 1;
						int ns = line.indexOf( ":" ) + 1;
						int end = line.indexOf( ">" );
						String lineNode = line.substring( ns, end );
						String lineNodeNS = line.substring( start, end );
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText( locale, ERROR_XML_AD_NSFOUND, lineNode,
												lineNodeNS ) );
						in.close();
						return false;
					} else {
						// valider Status
						line = null;
					}
				}
			}
			in.close();

			File xmlToValidate = new File(
					pathToWorkDirFile.getAbsolutePath() + File.separator + "metadata.xml" );
			File xsdToValidateEch160 = new File( pathToWorkDirFile.getAbsolutePath() + File.separator
					+ "xsd" + File.separator + "arelda.xsd" );

			/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
			 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
			 * 
			 * dirOfJarPath + File.separator +
			 * 
			 * erweitern. */
			String path = new java.io.File(
					KOSTVal.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
							.getAbsolutePath();
			String locationOfJarPath = path;
			String dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}

			File xsd10 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.0" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xsd11 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.1" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xsd12 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.2" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xml10 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.0" + File.separator + "metadata.xml" );
			File xml11 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.1" + File.separator + "metadata.xml" );
			File xml12 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.2" + File.separator + "metadata.xml" );

			/* System.out .println( dirOfJarPath + " " + xsd10.getAbsolutePath() + " " +
			 * xsd11.getAbsolutePath() + " " + xml10.getAbsolutePath() + " " + xml11.getAbsolutePath()
			 * ); */
			File xmlIntern = xml10;
			File xsdIntern = xsd10;

			if ( (xmlToValidate.exists() && xsdToValidateEch160.exists()) ) {
				/* eCH-0160_v1.1 enthält in arelda.xsd neu "vorgangAktivitaet" */
				try {
					Scanner scanner = new Scanner( xmlToValidate );

					// Datei Zeile für Zeile lesen und "schemaVersion=" herauslesen
					while ( scanner.hasNextLine() ) {
						String lineXml = scanner.nextLine();
						if ( lineXml.contains( "schemaVersion=" ) ) {
							// richtige Zeile
							if ( lineXml.contains( "schemaVersion=\"5.0\"" ) ) {
								// es ist eine eCH-0160 v1.2
								xmlIntern = xml12;
								xsdIntern = xsd12;
								sipVer = "ECH160_1.2.txt";
								break;
							} else if ( lineXml.contains( "schemaVersion=\"4.1\"" ) ) {
								// es ist eine eCH-0160 v1.1
								xmlIntern = xml11;
								xsdIntern = xsd11;
								sipVer = "ECH160_1.1.txt";
								break;
							} else {
								// dann validieren wir nach eCH-0160 v1.0
								xmlIntern = xml10;
								xsdIntern = xsd10;
								sipVer = "ECH160_1.0.txt";
								break;
							}
						}
					}
					scanner.close();

					// System.out.println("sipVer: "+sipVer+ " "+xmlIntern.getAbsolutePath()+ "
					// "+xsdIntern.getAbsolutePath());
					// ins log eine txt anlegen mit der Version
					sipVersionFile = new File(
							directoryOfLogfile.getAbsolutePath() + File.separator + sipVer );
					try {
						sipVersionFile.createNewFile();
					} catch ( IOException e ) {
						e.printStackTrace();
					}

				} catch ( FileNotFoundException e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											"FileNotFoundException" ) );
				} catch ( Exception e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											(e.getMessage() + " 1") ) ); //
					return false;
				}

				// Variante Xmllint
				File workDir = new File( pathToWorkDir );
				if ( !workDir.exists() ) {
					workDir.mkdir();
				}
				// Pfad zum Programm existiert die Dateien?
				String checkTool = Xmllint.checkXmllint( dirOfJarPath );
				if ( !checkTool.equals( "OK" ) ) {
					// mindestens eine Datei fehlt fuer die Validierung
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale, MESSAGE_XML_MISSING_FILE, checkTool,
											getTextResourceService().getText( locale, ABORTED ) ) );
					result = false;
				} else {
					// System.out.println("Validierung mit xmllint: ");
					try {
						// XML-SIP gegen XSD-SIP
						String resultExecSS = Xmllint.execXmllint( xmlToValidate, xsdToValidateEch160, workDir,
								dirOfJarPath );
						if ( !resultExecSS.equals( "OK" ) ) {
							// System.out.println("Validierung NICHT bestanden");
							result = false;
							String tableXmlShortString = xmlToValidate.getAbsolutePath()
									.replace( workDir.getAbsolutePath(), "" );
							String tableXsdShortString = xsdToValidateEch160.getAbsolutePath()
									.replace( workDir.getAbsolutePath(), "" );
							// val.message.xml.h.invalid.xml = <Message>{0} ist invalid zu
							// {1}</Message></Error>
							// val.message.xml.h.invalid.error = <Message>{0}</Message></Error>
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_XML,
													tableXmlShortString, tableXsdShortString ) );
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_ERROR,
													resultExecSS ) );
						} else {
							// System.out.println("Validierung SS bestanden");

							// XML-SIP gegen XSD-Intern
							String resultExecSI = Xmllint.execXmllint( xmlToValidate, xsdIntern, workDir,
									dirOfJarPath );
							if ( !resultExecSI.equals( "OK" ) ) {
								// System.out.println("Validierung NICHT bestanden");
								result = false;
								String tableXmlShortString = xmlToValidate.getAbsolutePath()
										.replace( workDir.getAbsolutePath(), "" );
								String tableXsdShortString = xsdIntern.getAbsolutePath();
								// val.message.xml.h.invalid.xml = <Message>{0} ist invalid zu
								// {1}</Message></Error>
								// val.message.xml.h.invalid.error = <Message>{0}</Message></Error>
								getMessageService()
										.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
												+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_XML,
														tableXmlShortString, tableXsdShortString ) );
								getMessageService()
										.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
												+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_ERROR,
														resultExecSI ) );
							} else {
								// System.out.println("Validierung SI bestanden");
								// XML-Intern gegen XSD-SIP
								String resultExecIS = Xmllint.execXmllint( xmlIntern, xsdToValidateEch160, workDir,
										dirOfJarPath );
								if ( !resultExecIS.equals( "OK" ) ) {
									// System.out.println("Validierung NICHT bestanden");
									result = false;
									String tableXmlShortString = xmlIntern.getAbsolutePath();
									String tableXsdShortString = xsdToValidateEch160.getAbsolutePath()
											.replace( workDir.getAbsolutePath(), "" );
									// val.message.xml.h.invalid.xml = <Message>{0} ist invalid zu
									// {1}</Message></Error>
									// val.message.xml.h.invalid.error = <Message>{0}</Message></Error>
									getMessageService().logError(
											getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
													+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_XML,
															tableXmlShortString, tableXsdShortString ) );
									getMessageService().logError(
											getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
													+ getTextResourceService().getText( locale, MESSAGE_XML_H_INVALID_ERROR,
															resultExecIS ) );
								} else {
									// System.out.println("Validierung bestanden");
								}
							}
						}
					} catch ( InterruptedException e1 ) {
						result = false;
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
												e1.getMessage() + " (InterruptedException Xmllint.execXmllint)" ) );
					}
				}
			}
		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}
}
