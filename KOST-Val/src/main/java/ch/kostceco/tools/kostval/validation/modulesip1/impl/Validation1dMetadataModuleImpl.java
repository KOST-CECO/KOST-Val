/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.kostceco.tools.kosttools.fileservice.Xmllint;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;

public class Validation1dMetadataModuleImpl extends ValidationModuleImpl
		implements Validation1dMetadataModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	final int				BUFFER	= 2048;

	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws Validation1dMetadataException
	{
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File pathToWorkDirFile = new File(
				pathToWorkDir + File.separator + "header" );
		try {
			if ( !pathToWorkDirFile.exists() ) {
				// System.out.println( pathToWorkDirFile.getAbsolutePath() );
				pathToWorkDirFile.mkdirs();
			}
			if ( !pathToWorkDirFile.exists() ) {
				Thread.sleep( 10 );
			}
			if ( !pathToWorkDirFile.exists() ) {
				Thread.sleep( 100 );
			}
			if ( !pathToWorkDirFile.exists() ) {
				Thread.sleep( 1000 );
			}
			File valDateiHeader = new File(
					valDatei.getAbsolutePath() + File.separator + "header" );
			Util.copyDir( valDateiHeader, pathToWorkDirFile );
			File xmlCopy = new File( pathToWorkDirFile.getAbsolutePath()
					+ File.separator + "metadata.xml" );
			/*
			 * Das Kopieren des ganzen headers benoetigt einige Zeit. Falls
			 * metadata.xml (noch) nicht existiert wird pausiert.
			 */
			if ( !xmlCopy.exists() ) {
				Thread.sleep( 10 );
			}
			if ( !xmlCopy.exists() ) {
				Thread.sleep( 100 );
			}
			if ( !xmlCopy.exists() ) {
				Thread.sleep( 1000 );
			}
			if ( !xmlCopy.exists() ) {
				Thread.sleep( 10000 );
			}

		} catch ( FileNotFoundException e1 ) {
			e1.printStackTrace();
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
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
					new File( pathToWorkDirFile.getAbsolutePath()
							+ File.separator + "metadata.xml" ) ) );
			doc.getDocumentElement().normalize();

			BufferedReader in = new BufferedReader( new FileReader(
					new File( pathToWorkDirFile.getAbsolutePath()
							+ File.separator + "metadata.xml" ) ) );
			StringBuffer concatenatedOutputs = new StringBuffer();
			String line;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );
				/*
				 * Kontrollieren, dass kein Namespace verwendet wurde wie z.B.
				 * v4:
				 * 
				 * <?xml version="1.0" encoding="UTF-8"?> <v4:paket
				 * schemaVersion="4.1" xsi:type="v4:paketSIP"
				 * xmlns:v4="http://bar.admin.ch/arelda/v4"
				 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				 * <v4:paketTyp>SIP</v4:paketTyp> <v4:inhaltsverzeichnis>
				 */
				if ( line.contains( "paketTyp>" ) ) {
					if ( !line.contains( "<paketTyp>" ) ) {
						// Invalider Status
						int start = line.indexOf( "<" ) + 1;
						int ns = line.indexOf( ":" ) + 1;
						int end = line.indexOf( ">" );
						String lineNode = line.substring( ns, end );
						String lineNodeNS = line.substring( start, end );

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AD_NSFOUND,
												lineNode, lineNodeNS ) );
						in.close();
						return false;
					} else {
						// valider Status
						line = null;
						break;
					}
				}
			}
			in.close();
			File xmlToValidate = new File( pathToWorkDirFile.getAbsolutePath()
					+ File.separator + "metadata.xml" );
			File xsdToValidateEch160 = new File(
					pathToWorkDirFile.getAbsolutePath() + File.separator + "xsd"
							+ File.separator + "arelda.xsd" );

			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist
			 * ein generelles TODO in allen Modulen. Zuerst immer dirOfJarPath
			 * ermitteln und dann alle Pfade mit
			 * 
			 * dirOfJarPath + File.separator +
			 * 
			 * erweitern.
			 */
			File pathFile = new File( ClassLoader.getSystemClassLoader()
					.getResource( "." ).getPath() );
			String locationOfJarPath = pathFile.getAbsolutePath();
			String dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" )
					|| locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}

			File xsd10 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.0" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xsd11 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.1" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xsd12 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.2" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xml10 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.0" + File.separator + "metadata.xml" );
			File xml11 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.1" + File.separator + "metadata.xml" );
			File xml12 = new File( dirOfJarPath + File.separator + "resources"
					+ File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.2" + File.separator + "metadata.xml" );

			/*
			 * System.out .println( dirOfJarPath + " " + xsd10.getAbsolutePath()
			 * + " " + xsd11.getAbsolutePath() + " " + xml10.getAbsolutePath() +
			 * " " + xml11.getAbsolutePath() );
			 */
			File xmlIntern = xml10;
			File xsdIntern = xsd10;

			/*
			 * Das Kopieren des ganzen headers benoetigt einige Zeit. Falls
			 * xsdToValidateEch160 (noch) nicht existiert wird pausiert.
			 */
			if ( !xsdToValidateEch160.exists() ) {
				Thread.sleep( 10 );
			}
			if ( !xsdToValidateEch160.exists() ) {
				Thread.sleep( 100 );
			}
			if ( !xsdToValidateEch160.exists() ) {
				Thread.sleep( 1000 );
			}
			if ( !xsdToValidateEch160.exists() ) {
				Thread.sleep( 10000 );
			}
			if ( (xmlToValidate.exists() && xsdToValidateEch160.exists()) ) {
				/*
				 * eCH-0160_v1.1 enthält in arelda.xsd neu "vorgangAktivitaet"
				 */
				try {
					Scanner scanner = new Scanner( xmlToValidate );

					// Datei Zeile für Zeile lesen und "schemaVersion="
					// herauslesen
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
							} else if ( lineXml
									.contains( "schemaVersion=\"4.1\"" ) ) {
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

					// System.out.println("sipVer: "+sipVer+ "
					// "+xmlIntern.getAbsolutePath()+ "
					// "+xsdIntern.getAbsolutePath());
					// ins log eine txt anlegen mit der Version
					sipVersionFile = new File(
							directoryOfLogfile.getAbsolutePath()
									+ File.separator + sipVer );
					try {
						sipVersionFile.createNewFile();
					} catch ( IOException e ) {
						e.printStackTrace();
					}

				} catch ( FileNotFoundException e ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale,
											ERROR_XML_UNKNOWN,
											"FileNotFoundException" ) );
				} catch ( Exception e ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale,
											ERROR_XML_UNKNOWN,
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

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_MISSING_FILE, checkTool,
											getTextResourceService().getText(
													locale, ABORTED ) ) );
					result = false;
				} else {
					// System.out.println("Validierung mit xmllint: ");
					try {
						// XML-SIP gegen XSD-SIP
						String resultExecSS = Xmllint.execXmllint(
								xmlToValidate, xsdToValidateEch160, workDir,
								dirOfJarPath, locale );
						if ( !resultExecSS.equals( "OK" ) ) {
							// System.out.println("Validierung NICHT
							// bestanden");
							result = false;
							String tableXmlShortString = xmlToValidate
									.getAbsolutePath()
									.replace( workDir.getAbsolutePath(), "" );
							String tableXsdShortString = xsdToValidateEch160
									.getAbsolutePath()
									.replace( workDir.getAbsolutePath(), "" );
							// val.message.xml.h.invalid.xml = <Message>{0} ist
							// invalid zu
							// {1}</Message></Error>
							// val.message.xml.h.invalid.error =
							// <Message>{0}</Message></Error>

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale,
											ERROR_XML_AD_INVALID_XML,
											tableXmlShortString,
											tableXsdShortString ) );

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_SERVICEMESSAGE, " - ",
											resultExecSS ) );
						} else {
							// System.out.println("Validierung SS bestanden");

							// XML-SIP gegen XSD-Intern
							/*
							 * Bei den Internen XSD wurde die Mindestlaenge bei
							 * den obligatorischen Feldern bei text 1-4 auf 1
							 * gesetzt. Ansonsten wird ein muss-Feld nicht
							 * bemaengelt, wenn es existiert aber leer ist.
							 * 
							 * TODO: bei den neueren xsd (base.xsd)
							 * 
							 * - ERROR: Schemas validity error : Element
							 * '{http://bar.admin.ch/arelda/v4}aktenbildnerName'
							 * : [facet 'minLength'] The value has a length of
							 * '0'; this underruns the allowed minimum length of
							 * '1'.
							 */
							String resultExecSI = Xmllint.execXmllint(
									xmlToValidate, xsdIntern, workDir,
									dirOfJarPath, locale );
							if ( !resultExecSI.equals( "OK" ) ) {
								// System.out.println("Validierung NICHT
								// bestanden");
								result = false;
								String tableXmlShortString = xmlToValidate
										.getAbsolutePath().replace(
												workDir.getAbsolutePath(), "" );
								String tableXsdShortString = xsdIntern
										.getAbsolutePath();
								// val.message.xml.h.invalid.xml = <Message>{0}
								// ist invalid zu
								// {1}</Message></Error>
								// val.message.xml.h.invalid.error =
								// <Message>{0}</Message></Error>

								/*- ERROR: Schemas validity error : Element '{http://bar.admin.ch/arelda/v4}aktenbildnerName': [facet 'minLength'] The value has a length of '0'; this underruns the allowed minimum length of '1'.
								 * 
								 * umschreiben nach
								 * 
								 * - ERROR: Schemas validity error : Element '{http://bar.admin.ch/arelda/v4}aktenbildnerName': It does not contain a value; The element is empty.
								 */
								String oldstring = " [facet 'minLength'] The value has a length of '0'; this underruns the allowed minimum length of '1'.";
								String newstring = " It does not contain a value; The element is empty.";
								resultExecSI = resultExecSI.replace( oldstring,
										newstring );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_AD_INVALID_XML,
												tableXmlShortString,
												tableXsdShortString ) );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText(
												locale,
												MESSAGE_XML_SERVICEMESSAGE,
												" - ", resultExecSI ) );
							} else {
								// System.out.println("Validierung SI
								// bestanden");
								// XML-Intern gegen XSD-SIP
								String resultExecIS = Xmllint.execXmllint(
										xmlIntern, xsdToValidateEch160, workDir,
										dirOfJarPath, locale );
								if ( !resultExecIS.equals( "OK" ) ) {
									// System.out.println("Validierung NICHT
									// bestanden");
									result = false;
									String tableXmlShortString = xmlIntern
											.getAbsolutePath();
									String tableXsdShortString = xsdToValidateEch160
											.getAbsolutePath()
											.replace( workDir.getAbsolutePath(),
													"" );
									// val.message.xml.h.invalid.xml =
									// <Message>{0} ist invalid zu
									// {1}</Message></Error>
									// val.message.xml.h.invalid.error =
									// <Message>{0}</Message></Error>
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Ad_SIP )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_AD_INVALID_XML,
																	tableXmlShortString,
																	tableXsdShortString ) );
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_Ad_SIP )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_SERVICEMESSAGE,
																	" - ",
																	resultExecIS ) );
								} else {
									// System.out.println("Validierung
									// bestanden");
								}
							}
						}
					} catch ( InterruptedException e1 ) {
						result = false;

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, e1.getMessage()
												+ " (InterruptedException Xmllint.execXmllint)" ) );
					}
				}
			}

			if ( result ) {
				// System.out.println("Kontrolle ob OSP in OS vorhanden ist");
				XPath xpath = XPathFactory.newInstance().newXPath();
				Element elementOS, elementOSP, elementOSPm = null;

				elementOS = (Element) xpath.evaluate( "/*/*/ordnungssystem",
						doc, XPathConstants.NODE );

				elementOSP = (Element) xpath.evaluate(
						"/*/*/*/ordnungssystemposition", doc,
						XPathConstants.NODE );

				elementOSPm = (Element) xpath.evaluate(
						"/*/*/*/*/ordnungssystemposition", doc,
						XPathConstants.NODE );

				/*
				 * OSP kann direkt bei FILES in OS angezogen werden oder via
				 * Mappe. OSP muss aber immer mindestens einmal in OS vorhanden
				 * sein. Kann nicht mit xsd kontrolliert werden
				 */
				if ( elementOS != null ) {

					// System.out.println("Das elementOS existiert und
					// entsprechend muss auch OSP
					// existieren");
					if ( elementOSP == null && elementOSPm == null ) {
						// System.out.println("Kein OSP > Validierung
						// fehlgeschlagen");
						result = false;

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AD_NOOSP ) );
					}
				}
			}

			// unstrukturierterAnhang ist veraltet und soll nicht mehr verwendet
			// werden
			// System.out.println( "Kontrolle ob unstrukturierterAnhang
			// vorhanden ist" );
			XPath xpathUA = XPathFactory.newInstance().newXPath();

			Element elementUA = null;
			String uaPath = "/*/*/unstrukturierterAnhang";
			elementUA = (Element) xpathUA.evaluate( uaPath, doc,
					XPathConstants.NODE );
			if ( elementUA != null ) {
				// System.out.println( "existiert und entsprechend Fehler
				// ausgeben" );
				result = false;
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_AD_UADEP ) );
			}

			/*
			 * Warnung ausgeben wenn Archivischer Vorgang und oder Archivische
			 * Notiz enthalten ist. Die Anzahl der archivischen Notizen sowie
			 * Archivischer Vorgang in einem SIP einer abliefernden Stelle vor
			 * dem Transfer sollte immer 0 betragen.
			 */

			// System.out.println( "Kontrolle ob Archivischer Vorgang vorhanden
			// ist" );
			XPath xpathAV = XPathFactory.newInstance().newXPath();

			Element elementAV = null;
			String avPath = "/paket/archivischerVorgang";
			elementAV = (Element) xpathAV.evaluate( avPath, doc,
					XPathConstants.NODE );
			if ( elementAV != null ) {
				// System.out.println( "existiert und entsprechend Warnung
				// ausgeben" );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_AD_AVAN_WARNING, avPath ) );
			}

			String anPath = "archivischeNotiz";

			Element elementAN1, elementAN2, elementAN3, elementAN4, elementAN5,
					elementAN6, elementAN7, elementAN8, elementAN9, elementAN10,
					elementAN11 = null;
			String an1Path = "/*/archivischeNotiz";
			elementAN1 = (Element) xpathAV.evaluate( an1Path, doc,
					XPathConstants.NODE );
			String an2Path = "/*/*/archivischeNotiz";
			elementAN2 = (Element) xpathAV.evaluate( an2Path, doc,
					XPathConstants.NODE );
			String an3Path = "/*/*/*/archivischeNotiz";
			elementAN3 = (Element) xpathAV.evaluate( an3Path, doc,
					XPathConstants.NODE );
			String an4Path = "*/*/*/*/archivischeNotiz";
			elementAN4 = (Element) xpathAV.evaluate( an4Path, doc,
					XPathConstants.NODE );
			String an5Path = "*/*/*/*/*/archivischeNotiz";
			elementAN5 = (Element) xpathAV.evaluate( an5Path, doc,
					XPathConstants.NODE );
			String an6Path = "*/*/*/*/*/*/archivischeNotiz";
			elementAN6 = (Element) xpathAV.evaluate( an6Path, doc,
					XPathConstants.NODE );
			String an7Path = "*/*/*/*/*/*/*/archivischeNotiz";
			elementAN7 = (Element) xpathAV.evaluate( an7Path, doc,
					XPathConstants.NODE );
			String an8Path = "*/*/*/*/*/*/*/*/archivischeNotiz";
			elementAN8 = (Element) xpathAV.evaluate( an8Path, doc,
					XPathConstants.NODE );
			String an9Path = "*/*/*/*/*/*/*/*/*/archivischeNotiz";
			elementAN9 = (Element) xpathAV.evaluate( an9Path, doc,
					XPathConstants.NODE );
			String an10Path = "*/*/*/*/*/*/*/*/*/*/archivischeNotiz";
			elementAN10 = (Element) xpathAV.evaluate( an10Path, doc,
					XPathConstants.NODE );
			String an11Path = "*/*/*/*/*/*/*/*/*/*/*/archivischeNotiz";
			elementAN11 = (Element) xpathAV.evaluate( an11Path, doc,
					XPathConstants.NODE );
			if ( elementAN1 != null || elementAN2 != null || elementAN3 != null
					|| elementAN4 != null || elementAN5 != null
					|| elementAN6 != null || elementAN7 != null
					|| elementAN8 != null || elementAN9 != null
					|| elementAN10 != null || elementAN11 != null ) {
				// System.out.println( "existiert und entsprechend Warnung
				// ausgeben" );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_AD_AVAN_WARNING, anPath ) );
			}

		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}
}
