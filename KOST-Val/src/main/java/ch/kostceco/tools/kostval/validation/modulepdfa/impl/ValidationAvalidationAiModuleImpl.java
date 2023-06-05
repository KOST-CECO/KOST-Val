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

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pdftools.FileStream;
import com.pdftools.NativeLibrary;
import com.pdftools.Stream;
import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kosttools.util.UtilCallas;
import ch.kostceco.tools.kosttools.util.UtilCharacter;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAvalidationAiModule;

/**
 * Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit
 * callas und oder PDF-Tools.
 * 
 * Folgendes ist Konfigurierbar: welche Validatoren verwendet werden sollen.
 * Sollen beide verwendet werden wird die Duale Validierung durchgefuehrt. Bei
 * der dualen Validierung muessen beide Validatoren die Datei als invalide
 * betrachten, damit diese als invalid gilt. Bei Uneinigkeit gilt diese als
 * valid.
 * 
 * Es wird falls vorhanden die Vollversion von PDF-Tools verwendet. KOST-Val
 * muss nicht angepasst werden und verwendet automatisch den internen
 * Schluessel, sollte keine Vollversion existieren.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit PDF
 * Tools und oder callas. Die Fehler werden den Einzelnen Gruppen (Modulen)
 * zugeordnet
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationAiModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationAiModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationApdfvalidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		int iCategory = 999999999;
		String errorK = "";
		// Create object
		PdfValidatorAPI docPdf = null;

		// Version & Level herausfinden
		String pdfa1a = configMap.get( "pdfa1a" );
		String pdfa1b = configMap.get( "pdfa1b" );
		String pdfa2a = configMap.get( "pdfa2a" );
		String pdfa2b = configMap.get( "pdfa2b" );
		String pdfa2u = configMap.get( "pdfa2u" );
		String warning3to2 = configMap.get( "warning3to2" );

		Integer pdfaVer1 = 0;
		Integer pdfaVer2 = 0;

		String pdfaCl = "B";

		String pathToLogDir = configMap.get( "PathToLogfile" );
		String pathToWorkDir = pathToLogDir;
		/*
		 * Beim schreiben ins Workverzeichnis trat ab und zu ein fehler auf.
		 * entsprechend wird es jetzt ins logverzeichnis geschrieben
		 */

		String pdf3warning = getTextResourceService().getText( locale,
				MESSAGE_XML_MODUL_A_PDFA )
				+ getTextResourceService().getText( locale,
						WARNING_XML_A_PDFA3 );
		Boolean warning3to2done = false;

		File callasNo = new File(
				pathToWorkDir + File.separator + "_callas_NO.txt" );

		String pathToPdfapilotOutput = pathToLogDir + File.separator
				+ "callasTEMP.txt";
		File report = new File( pathToPdfapilotOutput );
		String pathToPdfapilotOutputReport = pathToLogDir + File.separator
				+ "callasTEMPreport.txt";
		File reportOriginal = new File( pathToPdfapilotOutputReport );

		// falls das File bereits existiert, z.B. von einemvorhergehenden
		// Durchlauf, loeschen wir es
		if ( report.exists() ) {
			report.delete();
		}
		/*
		 * Neu soll die Validierung konfigurier bar sein Moegliche Werte 1A, 1B
		 * und no sowie 2A, 2B, 2U und no Da Archive beide Versionen erlauben
		 * koennen sind es 2 config eintraege Es gibt mehre Moeglichkeiten das
		 * PDF in der gewuenschten Version zu testen - Unterscheidung anhand
		 * PDF/A-Eintrag
		 */
		if ( pdfa2a.equals( "2A" ) || pdfa2b.equals( "2B" )
				|| pdfa2u.equals( "2U" ) ) {
			// gueltiger Konfigurationseintrag und V2 erlaubt
			pdfaVer2 = 2;
		} else {
			// v2 nicht erlaubt oder falscher eintrag
			pdfa2a = "no";
			pdfa2b = "no";
			pdfa2u = "no";
		}
		if ( pdfa1a.equals( "1A" ) || pdfa1b.equals( "1B" ) ) {
			// gueltiger Konfigurationseintrag und V1 erlaubt
			pdfaVer1 = 1;
		} else {
			// v1 nicht erlaubt oder falscher eintrag
			pdfa1a = "no";
			pdfa1b = "no";
		}
		if ( pdfaVer1 == 0 && pdfaVer2 == 0 ) {
			// keine Validierung moeglich. keine PDFA-Versionen konfiguriert
			if ( min ) {
				return false;
			} else {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_PDFA_NOCONFIG ) );
				return false;
			}
		}

		String level = "no";
		// Richtiges Level definieren
		try {
			// Level je nach File auswaehlen
			int pdfaVer = 0;
			BufferedReader in = new BufferedReader(
					new FileReader( valDatei ) );
			String line;
			while ( (line = in.readLine()) != null ) {
				// haeufige Partangaben: pdfaid:part>1< pdfaid:part='1'
				// pdfaid:part="1"

				// <pdfaid:part>2</pdfaid:part>
				// <pdfaid:conformance>U</pdfaid:conformance>

				if ( line.contains( "pdfaid:part" ) ) {
					// pdfaid:part
					if ( line.contains( "pdfaid:part>1<" )
							|| line.contains( "pdfaid:part='1'" )
							|| line.contains( "pdfaid:part=\"1\"" ) ) {
						pdfaVer = 1;
					} else if ( line.contains( "pdfaid:part>2<" )
							|| line.contains( "pdfaid:part='2'" )
							|| line.contains( "pdfaid:part=\"2\"" ) ) {
						pdfaVer = 2;
					} else if ( line.contains( "pdfaid:part>3<" )
							|| line.contains( "pdfaid:part='3'" )
							|| line.contains( "pdfaid:part=\"3\"" ) ) {
						// 3 wird nie akzeptier, Validierung gegen 2
						pdfaVer = 2;
						if ( warning3to2.equalsIgnoreCase( "yes" ) ) {
							// Fehler betreffend Version 3 statt 2 wird
							// ignoriert.
							// Es wird eine Warnung ausgegeben.
							Logtxt.logtxt( logFile, pdf3warning );
							warning3to2done = true;
						}
					} else if ( line.contains( "pdfaid:part" )
							&& line.contains( "1" ) ) {
						// PDFA-Version = 1
						pdfaVer = 1;
					} else if ( line.contains( "pdfaid:part" )
							&& line.contains( "2" ) ) {
						// PDFA-Version = 2
						pdfaVer = 2;
					}
				}
				if ( line.contains( "pdfaid:conformance" ) ) {
					// pdfaid:part
					if ( line.contains( "pdfaid:conformance>A<" )
							|| line.contains( "pdfaid:conformance='A'" )
							|| line.contains( "pdfaid:conformance=\"A\"" )
							|| line.contains( "pdfaid:conformance>a<" )
							|| line.contains( "pdfaid:conformance='a'" )
							|| line.contains( "pdfaid:conformance=\"a\"" ) ) {
						pdfaCl = "A";
					} else if ( line.contains( "pdfaid:conformance>U<" )
							|| line.contains( "pdfaid:conformance='U'" )
							|| line.contains( "pdfaid:conformance=\"U\"" )
							|| line.contains( "pdfaid:conformance>u<" )
							|| line.contains( "pdfaid:conformance='u'" )
							|| line.contains( "pdfaid:conformance=\"u\"" ) ) {
						pdfaCl = "U";
					} else {
						pdfaCl = "B";
					}
				}
				if ( pdfaVer == 0 ) {
					// der Part wurde nicht gefunden --> Level 2B
					pdfaVer = 2;
				}
				level = pdfaVer + pdfaCl;
				if ( level == "1U" ) {
					level = "1B";
				}
			}
			// System.out.println( " " );
			// System.out.print( " Level " + level + " geaendert ... " );
			if ( level.toLowerCase().contains( "1a" ) ) {
				// wurde als 1A erkannt, wenn erlaubt als 1a validieren
				if ( pdfa1a.equals( "1A" ) ) {
					// 1A erlaubt, Level 1A bleibt
				} else {
					if ( pdfa1b.equals( "1B" ) ) {
						level = "1B";
					} else if ( pdfa2a.equals( "2A" ) ) {
						level = "2A";
					} else if ( pdfa2u.equals( "2U" ) ) {
						level = "2U";
					} else {
						level = "2B";
					}
				}
			}
			if ( level.toLowerCase().contains( "1b" ) ) {
				// wurde als 1B erkannt, wenn erlaubt als 1b validieren
				if ( pdfa1b.equals( "1B" ) ) {
					// 1B erlaubt, Level 1B bleibt
				} else {
					if ( pdfa1a.equals( "1A" ) ) {
						level = "1A";
					} else if ( pdfa2b.equals( "2B" ) ) {
						level = "2B";
					} else if ( pdfa2u.equals( "2U" ) ) {
						level = "2U";
					} else {
						level = "2A";
					}
				}
			}
			if ( level.toLowerCase().contains( "2a" ) ) {
				// wurde als 2A erkannt, wenn erlaubt als 2a validieren
				if ( pdfa2a.equals( "2A" ) ) {
					// 2A erlaubt, Level 2A bleibt
				} else {
					if ( pdfa2u.equals( "2U" ) ) {
						level = "2U";
					} else if ( pdfa2b.equals( "2B" ) ) {
						level = "2B";
					} else if ( pdfa1a.equals( "1A" ) ) {
						level = "1A";
					} else {
						level = "1B";
					}
				}
			}
			if ( level.toLowerCase().contains( "2u" ) ) {
				// wurde als 2U erkannt, wenn erlaubt als 2u validieren
				if ( pdfa2u.equals( "2U" ) ) {
					// 2U erlaubt, Level 2U bleibt
				} else {
					if ( pdfa2b.equals( "2B" ) ) {
						level = "2B";
					} else if ( pdfa2a.equals( "2A" ) ) {
						level = "2A";
					} else if ( pdfa1a.equals( "1A" ) ) {
						level = "1A";
					} else {
						level = "1B";
					}
				}
			}
			if ( level.toLowerCase().contains( "2b" ) ) {
				// wurde als 2B erkannt, wenn erlaubt als 2b validieren
				if ( pdfa2b.equals( "2B" ) ) {
					// 2B erlaubt, Level 2B bleibt
				} else {
					if ( pdfa2u.equals( "2U" ) ) {
						level = "2U";
					} else if ( pdfa2a.equals( "2A" ) ) {
						level = "2A";
					} else if ( pdfa1b.equals( "1B" ) ) {
						level = "1B";
					} else {
						level = "1A";
					}
				}
			}
			// System.out.println( " --> " + level + "!" );
			in.close();
			// set to null
			in = null;

		} catch ( Throwable e ) {
			if ( min ) {
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										"Version " + e.getMessage() ) );
			}
		}

		Logtxt.logtxt( logFile, "<FormatVL>-" + level + "</FormatVL>" );

		// Die Erkennung erfolgt bereits im Vorfeld

		boolean isValid = false;
		boolean isValidPdftools = true;
		boolean isValidCallas = false;
		boolean isValidCa = true;
		boolean isValidCb = true;
		boolean isValidCc = true;
		boolean isValidCd = true;
		boolean isValidCe = true;
		boolean isValidCf = true;
		boolean isValidCg = true;
		boolean isValidCh = true;
		boolean isValidCi = true;
		boolean isValidJ = true;
		boolean isValidFont = true;
		boolean ignorUndefinied = false;
		// String undefiniedWarningString = "";
		// int symbolWarning = 0;
		// String symbolWarningString = "";
		String fontErrorIgnor = "I";
		boolean callas = false;
		boolean pdftools = false;
		int callasReturnCode = 9;
		int callasReturnCodeTest = 9;
		boolean callasServiceFailed = false;

		String pdftoolsA = "";
		String pdftoolsB = "";
		String pdftoolsC = "";
		String pdftoolsD = "";
		String pdftoolsE = "";
		String pdftoolsF = "";
		String pdftoolsG = "";
		String pdftoolsH = "";
		String pdftoolsI = "";

		String callasConfig = configMap.get( "callas" );
		String pdftoolsConfig = configMap.get( "pdftools" );

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		if ( callasConfig.contentEquals( "yes" ) ) {
			// callas Validierung gewuenscht
			if ( callasNo.exists() ) {
				/*
				 * Callas wurde in einem frueheren durchgang getestet und es
				 * funktioniert bei dem Benutzer nicht korrekt. Entsprechend ist
				 * die Validierung mit callas nicht moeglich
				 */
				callas = false;
			} else {
				callas = true;
			}
		}
		if ( pdftoolsConfig.contentEquals( "yes" ) ) {
			// pdftools Validierung gewuenscht
			pdftools = true;
		}

		if ( !pdftools && !callas ) {
			// pdf Validierung nicht moeglich
			configMap.put( "pdfavalidation", "no" );
		}

		try {

			/*
			 * TODO: Erledigt Start mit PDFTools
			 * 
			 * Wenn pdftools eingeschaltet ist, wird immer zuerst pdftools
			 * genommen, da dieser in KOST-Val schneller ist als callas
			 */
			if ( pdftools ) {
				docPdf = new PdfValidatorAPI();

				try {
					if ( docPdf.open( valDatei.getAbsolutePath(), "",
							NativeLibrary.COMPLIANCE.ePDFUnk ) ) {
						// PDF Konnte geoeffnet werden
						docPdf.setStopOnError( true );
						docPdf.setReportingLevel( 1 );
						if ( docPdf
								.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD ) {
							if ( min ) {
								return false;
							} else {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_PDFTOOLS_ENCRYPTED ) );
								return false;
							}
						}
					} else {
						if ( docPdf
								.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD ) {
							if ( min ) {
								return false;
							} else {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_PDFTOOLS_ENCRYPTED ) );
								return false;
							}
						} else {
							if ( min ) {
								return false;
							} else {
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_PDFTOOLS_DAMAGED ) );
								return false;
							}
						}
					}

					docPdf = new PdfValidatorAPI();
					if ( min ) {
						docPdf.setStopOnError( true );
					} else {
						docPdf.setStopOnError( false );
					}
					docPdf.setReportingLevel( 2 );

					/*
					 * ePDFA1a 5122 ePDFA1b 5121 ePDFA2a 5891 ePDFA2b 5889
					 * ePDFA2u 5890
					 */
					if ( level.contentEquals( "1A" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5122 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "1B" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5121 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2A" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5891 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2B" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5889 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2U" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5890 ) ) {
							docPdf.validate();
						}
					} else {
						// Validierung nach 2b
						level = "2B";
						if ( docPdf.open( valDatei.getAbsolutePath(), "",
								5889 ) ) {
							docPdf.validate();
						}
					}

					// Anzahl errors
					PdfError err = docPdf.getFirstError();
					int success = 0;

					if ( err != null ) {
						// auch bei min durchfuehren!
						for ( ; err != null; err = docPdf.getNextError() ) {
							success = success + 1;
						}
					}

					// Error Category
					iCategory = docPdf.getCategories();
					/*
					 * die Zahl kann auch eine Summe von Kategorien sein z.B.
					 * 6144=2048+4096 -> getCategoryText gibt nur die erste
					 * Kategorie heraus (z.B. 2048)
					 */

					if ( success == 0 && iCategory == 0 ) {
						// valide
						isValid = true;
					}

					String fontYesNo = configMap.get( "pdfafont" );
					if ( fontYesNo.equalsIgnoreCase( "yes" )
							|| fontYesNo.equalsIgnoreCase( "tolerant" )
							|| fontYesNo.equalsIgnoreCase( "strict" )
							|| fontYesNo.equalsIgnoreCase( "only" ) ) {
						/*
						 * WriteFontValidationXML Method: Boolean
						 * WriteFontValidationXML(Stream outputStream) Write
						 * font validation information in XML format to a
						 * stream. This method must be called after Validate and
						 * before Close. For more information on the structure
						 * of the resulting XML, see the XML schema
						 * ValidatorFontInformation.xsd and the stylesheet
						 * ValidatorFontInformation.xsl in the documentation
						 * directory. Parameter: outputStream [Stream] The
						 * stream the font validation information is written to.
						 * Returns: True The font information has been written
						 * successfully. False Otherwise.
						 */
						String pathToFontOutput = pathToLogDir + File.separator
								+ valDatei.getName() + "_FontValidation.xml";
						File fontReport = new File( pathToFontOutput );
						if ( fontReport.exists() ) {
							fontReport.delete();
						}
						String pathToFontOutputError = pathToLogDir
								+ File.separator + valDatei.getName()
								+ "_FontValidation_Error.xml";
						File fontReportError = new File(
								pathToFontOutputError );
						if ( fontReportError.exists() ) {
							fontReportError.delete();
						}

						// Write font validation information
						FileStream fs = new FileStream( fontReport, "rw" );
						Stream xmlStream = fs;
						if ( !docPdf.writeFontValidationXML( xmlStream ) ) {
							// throw new
							// Exception(String.format("Failed to write font
							// validation information: %s",
							// docPdf.getErrorMessage()));
							System.out.println( String.format(
									"Failed to write font validation information: %s",
									docPdf.getErrorMessage() ) );
						}
						fs.close();
						// set to null
						fs = null;

						// TODO erledigt: Start der Font-Auswertung betreffend
						// unbekannt und undefiniert
						try {
							FileChannel inputChannel = null;
							FileChannel outputChannel = null;
							FileInputStream fis = null;
							FileOutputStream fos = null;
							try {
								fis = new FileInputStream( fontReport );
								inputChannel = fis.getChannel();
								fos = new FileOutputStream( fontReportError );
								outputChannel = fos.getChannel();
								outputChannel.transferFrom( inputChannel, 0,
										inputChannel.size() );
							} finally {
								inputChannel.close();
								outputChannel.close();
								// set to null
								inputChannel = null;
								outputChannel = null;
							}
							fis.close();
							fos.close();
							// set to null
							fis = null;
							fos = null;
						} catch ( Exception e ) {

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											ERROR_XML_UNKNOWN,
											"Exec PDF Tools FileChannel: "
													+ e.getMessage() ) );
							return false;
						}

						Document doc = null;
						Document docError = null;

						BufferedInputStream bis = new BufferedInputStream(
								new FileInputStream( fontReportError ) );
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						doc = db.parse( bis );
						doc.normalize();

						// <characterCount>122</characterCount>
						// <characterUnknown>0</characterUnknown>
						// <characterUnknownPercentage>0</characterUnknownPercentage>
						// <characterUndefined>122</characterUndefined>
						// <characterUndefinedPercentage>100</characterUndefinedPercentage>

						String elementCount = doc
								.getElementsByTagName( "characterCount" )
								.item( 0 ).getTextContent();
						int count = Integer.valueOf( elementCount );
						String elementUnknown = doc
								.getElementsByTagName( "characterUnknown" )
								.item( 0 ).getTextContent();
						// int unknown=Integer.valueOf( elementUnknown );
						String elementUnknownP = doc
								.getElementsByTagName(
										"characterUnknownPercentage" )
								.item( 0 ).getTextContent();
						double unknownPdouble = Double
								.valueOf( elementUnknownP );
						String elementUndefined = doc
								.getElementsByTagName( "characterUndefined" )
								.item( 0 ).getTextContent();
						// int undefined=Integer.valueOf( elementUndefined );
						String elementUndefinedP = doc
								.getElementsByTagName(
										"characterUndefinedPercentage" )
								.item( 0 ).getTextContent();
						double undefinedPdouble = Double
								.valueOf( elementUndefinedP );
						double elementTolerance = unknownPdouble
								+ undefinedPdouble;
						String docInfo = nodeToString( doc
								.getElementsByTagName( "docInfo" ).item( 0 ) );
						String docInfoCharacterCount = docInfo
								+ "<characterCount>" + elementCount
								+ "</characterCount>";
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n  ", "" );
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n ", "" );
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n", "" );
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n  ", "" );
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n ", "" );
						docInfoCharacterCount = docInfoCharacterCount
								.replaceAll( "\n", "" );

						if ( elementUnknown.equals( "0" )
								&& elementUndefined.equals( "0" ) ) {
							if ( fontYesNo.equalsIgnoreCase( "only" ) ) {
								// Modul K bestanden

								/*
								 * Bei only wird in Log_Modul_K.txt die Dateien,
								 * hereingeschrieben, welche die Strikte
								 * Validierung nicht bestanden haben. Dies ist
								 * hier nicht der Fall. Entsprechend wird mit
								 * return true die Validierung beendet
								 */
								return false;
							} else {
								// Modul K bestanden

								// <docInfo> und <characterCount> auf eine Zeile
								// ausgeben
								if ( min ) {
								} else {
									Logtxt.logtxt( logFile,
											docInfoCharacterCount );
								}
							}
						} else {
							// Modul K evtl nicht bestanden
							try {
								if ( fontYesNo.equalsIgnoreCase( "only" ) ) {
									// only=strict -> Modul K nicht bestanden

									/*
									 * Bei only wird in Log_Modul_K.txt die
									 * Dateien, hereingeschrieben, welche die
									 * Strikte Validierung nicht bestanden
									 * haben. Danach wird mit return false die
									 * Validierung beendet
									 */
									String modulKFilePath = directoryOfLogfile
											.getAbsolutePath() + File.separator
											+ "Log_Modul_K.txt";
									String line = valDatei.getAbsolutePath()
											+ " ";
									Files.write( Paths.get( modulKFilePath ),
											line.getBytes(),
											StandardOpenOption.APPEND );
									return false;
								} else if ( fontYesNo
										.equalsIgnoreCase( "tolerant" ) ) {
									/*
									 * Ausnahmen ermitteln und abfangen bei
									 * tolerant:
									 * 
									 * A) max 5 Zeichen
									 * 
									 * B) Undefinierte Zeichen ignorieren wenn
									 * Maengel<=20%
									 * 
									 * C) Bekannte Abstaende und
									 * Aufzaehlungszeichen ignorieren
									 * 
									 * D) Symbol-Schriften ignorieren
									 * 
									 * E) Wenn am Schluss nur noch max 4 Zeichen
									 * bemaengelt werden
									 */
									if ( count <= 5 ) {
										/*
										 * => A) max 5 Zeichen
										 * 
										 * -> Modul K bestanden, keine
										 * Auswertung noetig
										 * 
										 * <docInfo> und <characterCount> auf
										 * eine Zeile ausgeben
										 */
										if ( min ) {
										} else {
											Logtxt.logtxt( logFile,
													docInfoCharacterCount );
										}
									} else {
										double tolerance = 20.000;
										fontErrorIgnor = "I";
										if ( tolerance > elementTolerance ) {
											// elementTolerance = unknownPdouble
											// + undefinedPdouble;
											// toleranzschwelle nicht
											// ueberschritten
											// undefiniertes kann bei tolerant
											// ignoriert werden
											ignorUndefinied = true;
										} else {
											// toleranzschwelle ueberschritten
											// -> Modul K nicht bestanden
											ignorUndefinied = false;
											fontErrorIgnor = "E";
										}

										if ( elementUnknown.equals( "0" )
												&& ignorUndefinied ) {
											/*
											 * => B) Undefinierte Zeichen
											 * ignorieren wenn Maengel<=20%
											 * 
											 * -> Modul K bestanden, keine
											 * Auswertung noetig da nur
											 * undefiniertes
											 * 
											 * <docInfo> und <characterCount>
											 * auf eine Zeile ausgeben
											 */
											if ( min ) {
											} else {
												Logtxt.logtxt( logFile,
														docInfoCharacterCount );
											}
										} else {
											// weiter auswerten
											NodeList nodeCharacterLst = doc
													.getElementsByTagName(
															"character" );
											Set<Node> targetNode = new HashSet<Node>();

											for ( int s = 0; s < nodeCharacterLst
													.getLength(); s++ ) {
												// unnoetige character aus log
												// loeschen
												boolean charUndef = false;
												boolean unicode = false;
												Node charNode = nodeCharacterLst
														.item( s );

												if ( charNode
														.hasAttributes() ) {
													NamedNodeMap attrs = charNode
															.getAttributes();
													for ( int i = 0; i < attrs
															.getLength(); i++ ) {
														Attr attribute = (Attr) attrs
																.item( i );
														String attName = attribute
																.getName();
														String attNameValue = attribute
																.getName()
																+ " = "
																+ attribute
																		.getValue();

														/*
														 * -> cid = 60 ->
														 * glyphId = 60 ->
														 * unicode = U+FFFD ->
														 * unicodeUndefined =
														 * true
														 */
														if ( attName
																.equalsIgnoreCase(
																		"unicode" ) ) {
															unicode = true;
														}
														if ( attNameValue
																.equalsIgnoreCase(
																		"unicodeUndefined = true" ) ) {
															charUndef = true;
														}
													}
													if ( !unicode ) {
														// unicode nicht bekannt
														// -> node
														// weiteranalysieren

														/*
														 * wenn I = tolerant
														 * analysieren ob es ein
														 * bekanntes Abstand-
														 * oder
														 * Aufzaehlungszeichen
														 * ist.
														 * 
														 * ist es ein solches
														 * bekanntes Zeichen das
														 * Zeichen ignorieren
														 * ansonsten belassen
														 */
														if ( fontErrorIgnor
																.equalsIgnoreCase(
																		"I" ) ) {
															String characterTextContent = charNode
																	.getTextContent();
															characterTextContent = characterTextContent
																	.replaceAll(
																			"\n",
																			"" );
															boolean ignoreCharacter = UtilCharacter
																	.ignoreCharacter(
																			characterTextContent );

															if ( ignoreCharacter ) {
																// => C)
																// Bekannte
																// Abstaende und
																// Aufzaehlungszeichen
																// ignorieren

																/*
																 * Node zum
																 * leschen
																 * vormerken, da
																 * er ein
																 * bekanntes
																 * Aufzaehlungszeichen
																 * oder Abstand
																 * ist
																 */
																targetNode.add(
																		charNode );
															}
														}
													} else if ( charUndef ) {
														// System.out.println( "
														// unicode nicht
														// definiert ");

														// wenn I ignorieren und
														// sonst belassen
														if ( fontErrorIgnor
																.equalsIgnoreCase(
																		"I" ) ) {
															// => B)
															// Undefinierte
															// Zeichen
															// ignorieren wenn
															// Maengel<=20%

															// Node zum loeschen
															// vormerken, da er
															// ignoriert werden
															// kann (I)
															targetNode.add(
																	charNode );
														}
													} else {
														// unicode bekannt ->
														// dieser node kann
														// geloescht werden

														// Node zum loeschen
														// vormerken
														targetNode.add(
																charNode );
													}
												}
											}

											for ( Node e : targetNode ) {
												e.getParentNode()
														.removeChild( e );
											}

											// write the content into xml file
											TransformerFactory transformerFactory = TransformerFactory
													.newInstance();
											Transformer transformer = transformerFactory
													.newTransformer();
											DOMSource source = new DOMSource(
													doc );
											StreamResult result = new StreamResult(
													fontReportError );
											// Output to console for testing
											// result = new StreamResult(
											// System.out );

											transformer.transform( source,
													result );

											// Fonts ohne character loeschen
											BufferedInputStream bisError = new BufferedInputStream(
													new FileInputStream(
															fontReportError ) );
											DocumentBuilderFactory dbfError = DocumentBuilderFactory
													.newInstance();
											DocumentBuilder dbError = dbfError
													.newDocumentBuilder();
											docError = dbError
													.parse( bisError );
											docError.normalize();

											NodeList nodeFontLst = docError
													.getElementsByTagName(
															"font" );
											Set<Node> targetNodeFont = new HashSet<Node>();

											for ( int s = 0; s < nodeFontLst
													.getLength(); s++ ) {
												Node fontNode = nodeFontLst
														.item( s );

												NodeList nodeFontCharLst = fontNode
														.getChildNodes();
												if ( nodeFontCharLst
														.getLength() <= 1 ) {
													// Leeren Font durch B) und
													// C) oder unicode-Zeichen

													// font Node zum leschen
													// vormerken
													targetNodeFont
															.add( fontNode );
												} else if ( fontNode
														.hasAttributes()
														&& fontErrorIgnor
																.equalsIgnoreCase(
																		"I" ) ) {
													NamedNodeMap attrs = fontNode
															.getAttributes();
													for ( int i = 0; i < attrs
															.getLength(); i++ ) {
														Attr attribute = (Attr) attrs
																.item( i );
														String attName = attribute
																.getName();
														String attValue = attribute
																.getValue();

														/*
														 * <font
														 * fontfile="TrueType"
														 * fullname="Symbol"
														 * name="Symbol"
														 * objectNo="161"
														 * type="Type0 (CIDFontType2)"
														 * >
														 */
														if ( attName
																.equalsIgnoreCase(
																		"name" ) ) {
															if ( attValue
																	.contains(
																			"Symbol" )
																	|| attValue
																			.contains(
																					"Webdings" )
																	|| attValue
																			.contains(
																					"Wingdings" )
																	|| attValue
																			.contains(
																					"Math" )
																	|| attValue
																			.contains(
																					"symbol" )
																	|| attValue
																			.contains(
																					"webdings" )
																	|| attValue
																			.contains(
																					"wingdings" )
																	|| attValue
																			.contains(
																					"math" ) ) {
																// => D)
																// Symbol-Schriften
																// ignorieren

																// font Node zum
																// leschen
																// vormerken
																targetNodeFont
																		.add( fontNode );
															}
														}
													}
												}
											}

											for ( Node f : targetNodeFont ) {
												f.getParentNode()
														.removeChild( f );
											}
											docError.getDocumentElement()
													.normalize();
											XPathExpression xpath = XPathFactory
													.newInstance().newXPath()
													.compile(
															"//text()[normalize-space(.) = '']" );
											NodeList blankTextNodes = (NodeList) xpath
													.evaluate( docError,
															XPathConstants.NODESET );

											for ( int i = 0; i < blankTextNodes
													.getLength(); i++ ) {
												blankTextNodes.item( i )
														.getParentNode()
														.removeChild(
																blankTextNodes
																		.item( i ) );
											}

											// Ende Bereinigung

											/*
											 * Nach der Bereinigung ermitteln
											 * wieviele unbekannt und
											 * undefiniert sind und ob es ein
											 * Fehler ist oder nicht
											 * 
											 * unbekannt unknown = count -
											 * undefined
											 * 
											 * undefiniert undefined =
											 * (unicodeUndefined = true)
											 */
											int undefinedE = 0;

											NodeList nodeCharacterLstError = docError
													.getElementsByTagName(
															"character" );

											for ( int s = 0; s < nodeCharacterLstError
													.getLength(); s++ ) {
												Node charNode = nodeCharacterLstError
														.item( s );

												if ( charNode
														.hasAttributes() ) {
													NamedNodeMap attrs = charNode
															.getAttributes();
													for ( int i = 0; i < attrs
															.getLength(); i++ ) {
														Attr attribute = (Attr) attrs
																.item( i );
														String attNameValue = attribute
																.getName()
																+ " = "
																+ attribute
																		.getValue();
														if ( attNameValue
																.equalsIgnoreCase(
																		"unicodeUndefined = true" ) ) {
															if ( fontErrorIgnor
																	.equalsIgnoreCase(
																			"E" ) ) {
																undefinedE = undefinedE
																		+ 1;
															}
														}
													}
												}
											}
											int unknownW = nodeCharacterLstError
													.getLength();
											float unknownPdoubleW = 100
													/ (float) count
													* (float) unknownW;

											if ( unknownW <= 4 ) {
												/*
												 * => E) Wenn am Schluss nur
												 * noch max 4 Zeichen bemaengelt
												 * werden
												 * 
												 * -> Modul K bestanden, keine
												 * Auswertung noetig
												 */
												isValidFont = true;
												errorK = "";
											} else {
												// Fehler im Modul K
												if ( fontErrorIgnor
														.equalsIgnoreCase(
																"I" ) ) {
													isValidFont = false;
													errorK = errorK
															+ getTextResourceService()
																	.getText(
																			locale,
																			MESSAGE_XML_MODUL_K_PDFA )
															+ getTextResourceService()
																	.getText(
																			locale,
																			ERROR_XML_K_OVERVIEW2,
																			count,
																			unknownW,
																			unknownPdoubleW );
												} else {
													isValidFont = false;
													errorK = errorK
															+ getTextResourceService()
																	.getText(
																			locale,
																			MESSAGE_XML_MODUL_K_PDFA )
															+ getTextResourceService()
																	.getText(
																			locale,
																			ERROR_XML_K_OVERVIEW,
																			elementCount,
																			elementUnknown,
																			elementUnknownP,
																			elementUndefined,
																			elementUndefinedP );
												}
											}
											if ( !isValidFont ) {
												Node nodeInfo = docError
														.getElementsByTagName(
																"docInfo" )
														.item( 0 );
												String stringInfo = nodeToString(
														nodeInfo );
												Node nodeFonts = docError
														.getElementsByTagName(
																"fonts" )
														.item( 0 );
												String stringFonts = nodeToString(
														nodeFonts );
												errorK = errorK
														+ getTextResourceService()
																.getText(
																		locale,
																		ERROR_XML_K_DETAIL,
																		stringInfo,
																		stringFonts );
											} else {
												// <docInfo> und
												// <characterCount> auf eine
												// Zeile ausgeben
												if ( min ) {
												} else {
													Logtxt.logtxt( logFile,
															docInfoCharacterCount );
												}
											}
											bisError.close();
											// set to null
											bisError = null;
											source = null;
											result = null;
										}
									}
								} else {
									/* strict: Fehler ausgeben */
									NodeList nodeCharacterLst = doc
											.getElementsByTagName(
													"character" );
									Set<Node> targetNode = new HashSet<Node>();

									for ( int s = 0; s < nodeCharacterLst
											.getLength(); s++ ) {
										// unnoetige character aus log loeschen
										boolean charUndef = false;
										boolean unicode = false;
										Node charNode = nodeCharacterLst
												.item( s );

										if ( charNode.hasAttributes() ) {
											NamedNodeMap attrs = charNode
													.getAttributes();
											for ( int i = 0; i < attrs
													.getLength(); i++ ) {
												Attr attribute = (Attr) attrs
														.item( i );
												String attName = attribute
														.getName();
												String attNameValue = attribute
														.getName() + " = "
														+ attribute.getValue();

												/*
												 * -> cid = 60 -> glyphId = 60
												 * -> unicode = U+FFFD ->
												 * unicodeUndefined = true
												 */
												if ( attName.equalsIgnoreCase(
														"unicode" ) ) {
													unicode = true;
												}
												if ( attNameValue
														.equalsIgnoreCase(
																"unicodeUndefined = true" ) ) {
													charUndef = true;
												}
											}
											if ( !unicode ) {
												// unicode nicht bekannt -> node
												// behalten da strict

											} else if ( charUndef ) {
												// unicode nicht definiert ->
												// node behalten da strict
											} else {
												// unicode bekannt -> dieser
												// node kann geloescht werden

												// Node zum leschen vormerken
												targetNode.add( charNode );
											}
										}
									}

									for ( Node e : targetNode ) {
										e.getParentNode().removeChild( e );
									}

									// write the content into xml file
									TransformerFactory transformerFactory = TransformerFactory
											.newInstance();
									Transformer transformer = transformerFactory
											.newTransformer();
									DOMSource source = new DOMSource( doc );
									StreamResult result = new StreamResult(
											fontReportError );
									// Output to console for testing
									// result = new StreamResult( System.out );

									transformer.transform( source, result );

									// Fonts ohne character loeschen
									BufferedInputStream bisError = new BufferedInputStream(
											new FileInputStream(
													fontReportError ) );
									DocumentBuilderFactory dbfError = DocumentBuilderFactory
											.newInstance();
									DocumentBuilder dbError = dbfError
											.newDocumentBuilder();
									docError = dbError.parse( bisError );
									docError.normalize();

									NodeList nodeFontLst = docError
											.getElementsByTagName( "font" );
									Set<Node> targetNodeFont = new HashSet<Node>();

									for ( int s = 0; s < nodeFontLst
											.getLength(); s++ ) {
										Node fontNode = nodeFontLst.item( s );

										NodeList nodeFontCharLst = fontNode
												.getChildNodes();
										if ( nodeFontCharLst
												.getLength() <= 1 ) {
											// Leeren Font durch B) und C) oder
											// unicode-Zeichen

											// font Node zum leschen vormerken
											targetNodeFont.add( fontNode );
										}
									}

									for ( Node f : targetNodeFont ) {
										f.getParentNode().removeChild( f );
									}
									docError.getDocumentElement().normalize();
									XPathExpression xpath = XPathFactory
											.newInstance().newXPath().compile(
													"//text()[normalize-space(.) = '']" );
									NodeList blankTextNodes = (NodeList) xpath
											.evaluate( docError,
													XPathConstants.NODESET );

									for ( int i = 0; i < blankTextNodes
											.getLength(); i++ ) {
										blankTextNodes.item( i ).getParentNode()
												.removeChild( blankTextNodes
														.item( i ) );
									}

									// Ende Bereinigung

									/*
									 * Nach der Bereinigung ermitteln wieviele
									 * unbekannt und undefiniert sind
									 * 
									 * unbekannt unknown = count - undefined
									 * 
									 * undefiniert undefined = (unicodeUndefined
									 * = true)
									 */
									int undefinedE = 0;

									NodeList nodeCharacterLstError = docError
											.getElementsByTagName(
													"character" );

									for ( int s = 0; s < nodeCharacterLstError
											.getLength(); s++ ) {
										Node charNode = nodeCharacterLstError
												.item( s );

										if ( charNode.hasAttributes() ) {
											NamedNodeMap attrs = charNode
													.getAttributes();
											for ( int i = 0; i < attrs
													.getLength(); i++ ) {
												Attr attribute = (Attr) attrs
														.item( i );
												String attNameValue = attribute
														.getName() + " = "
														+ attribute.getValue();
												if ( attNameValue
														.equalsIgnoreCase(
																"unicodeUndefined = true" ) ) {
													if ( fontErrorIgnor
															.equalsIgnoreCase(
																	"E" ) ) {
														undefinedE = undefinedE
																+ 1;
													}
												}
											}
										}
									}

									if ( !elementUnknown.equals( "0" )
											|| !elementUndefined
													.equals( "0" ) ) {
										isValidFont = false;
										errorK = errorK
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_K_PDFA )
												+ getTextResourceService()
														.getText( locale,
																ERROR_XML_K_OVERVIEW,
																elementCount,
																elementUnknown,
																elementUnknownP,
																elementUndefined,
																elementUndefinedP );
									}

									if ( !isValidFont ) {
										Node nodeInfo = docError
												.getElementsByTagName(
														"docInfo" )
												.item( 0 );
										String stringInfo = nodeToString(
												nodeInfo );
										Node nodeFonts = docError
												.getElementsByTagName( "fonts" )
												.item( 0 );
										String stringFonts = nodeToString(
												nodeFonts );
										errorK = errorK
												+ getTextResourceService()
														.getText( locale,
																ERROR_XML_K_DETAIL,
																stringInfo,
																stringFonts );
									} else {
										// <docInfo> und <characterCount> auf
										// eine Zeile ausgeben
										if ( min ) {
										} else {
											Logtxt.logtxt( logFile,
													docInfoCharacterCount );
										}
									}
									bisError.close();
									// set to null
									bisError = null;
									source = null;
									result = null;

								}
							} catch ( Exception e ) {

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_UNKNOWN,
												"Exec PDF Tools Font: "
														+ e.getMessage() ) );
								return false;
							}
						}
						bis.close();
						// set to null
						bis = null;
						doc = null;
						docError = null;
						if ( fontReportError.exists() ) {
							// Kann noch nicht geloescht werden, da noch aktiv
							// fontReportError wird in Controllervalfile
							// geloescht (je nach verbose)
						}
					} else {
						isValidFont = true;
					}
				} catch ( Exception e ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN,
									"Exec PDF Tools: " + e.getMessage() ) );
					return false;
				}
				if ( !isValid ) {

					// Ermittlung Detail-Fehlermeldungen von pdftools
					// (entspricht -rd)
					PdfError err = docPdf.getFirstError();
					boolean rd = false;
					String detailConfig = configMap.get( "detail" );
					if ( detailConfig.equalsIgnoreCase( "detail" )
							|| detailConfig.equalsIgnoreCase( "yes" ) ) {
						rd = true;
					}
					if ( err != null && rd ) {
						for ( ; err != null; err = docPdf.getNextError() ) {
							// Ermittlung der einzelnen Error Code und Message
							int errorCode = err.getErrorCode();
							String errorCode0x = String.format( "0x%08X",
									errorCode );
							String errorMsg = err.getMessage();

							// aus errorMsg < und > entfernen --> Probleme mit
							// XML
							errorMsg = errorMsg.replace( "<", "'" );
							errorMsg = errorMsg.replace( ">", "'" );

							// Ausgabe
							String errorMsgCode0xText = errorMsg
									+ " [PDF Tools: " + errorCode0x + "]";
							String errorMsgCode0x = " - " + errorMsgCode0xText;
							// System.out.println(errorMsgCode0x);

							/*
							 * TODO zu ignorierende Werte ignorieren und dann
							 * kontrollieren ob noch invalid (weitere Fehler
							 * existieren)
							 * 
							 * zB - The value of the key N is 4 but must be 3.
							 * [PDF Tools: 0x80410607]
							 * 
							 * Wenn warning3to2 = yes wird die
							 * Versionsfehlermeldung durch einen Warnung
							 * ersetzt.
							 * 
							 * The XMP property 'pdfaid:part' has the invalid
							 * value '3'. Required is '2'. [PDF Tools:
							 * 0x8341052E]
							 */
							String detailIgnore = configMap.get( "ignore" );
							String detailWarning3to2 = "The XMP property 'pdfaid:part' has the invalid value '3'. Required is '2'. [PDF Tools: 0x8341052E]";

							if ( warning3to2.equalsIgnoreCase( "yes" )
									&& errorMsgCode0xText
											.contains( detailWarning3to2 ) ) {
								// Fehler wird ignoriert. Es wird ggf eine
								// Warnung ausgegeben.
								if ( !warning3to2done ) {
									// Fehler wurde noch nicht ausgegeben.
									Logtxt.logtxt( logFile, pdf3warning );
									warning3to2done = true;
								}
							} else if ( detailIgnore
									.contains( errorMsgCode0xText ) ) {
								// Fehler wird ignoriert. Entsprechend wird kein
								// Detail geschrieben.
							} else {
								// Fehler wird nicht ignoriert und dem Modul
								// zugeordnet
								if ( errorMsgCode0x.toLowerCase()
										.contains( "graphic" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "image" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "interpolate" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "icc" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "color" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "colour" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "rgb" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "rvb" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "cmyk" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "cmjn" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "outputintent" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "jpeg2000" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "devicegray" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "key 'tr'." )
										|| errorMsgCode0x.toLowerCase()
												.contains( "tr2" ) ) {
									if ( pdftoolsC.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsC = pdftoolsC
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_C_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "police" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "font" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "gly" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "truetype" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "unicode" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "cid" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "encoding" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "charset" ) ) {
									if ( pdftoolsD.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsD = pdftoolsD
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_D_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "disponibi" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "accessibi" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "markinfo" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "structree" )
										|| errorMsgCode0x.toLowerCase()
												.contains(
														"structure tree root" )
										|| errorMsgCode0x.toLowerCase()
												.contains( " cross reference " )
										|| errorMsgCode0x.toLowerCase()
												.contains(
														" but must be a standard type. [PDF Tools: 0x00418607]" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "strukturbaum" ) ) {
									if ( pdftoolsI.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsI = pdftoolsI
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_I_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "structure" )
										|| errorMsgCode0x.toLowerCase()
												.contains( " ocproperties" )
										|| errorMsgCode0x.toLowerCase()
												.contains( " lzw" )
										|| errorMsgCode0x.toLowerCase()
												.contains( " structelem" )
										|| errorMsgCode0x.toLowerCase()
												.contains( " eol" ) ) {
									if ( pdftoolsB.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsB = pdftoolsB
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_B_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "metad" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "xmp" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "xml" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "key 'filter'." )
										|| errorMsgCode0x.toLowerCase()
												.contains(
														"schema description for namespace" )
										|| errorMsgCode0x.toLowerCase()
												.contains(
														"multiple occurrences of property 'pdf:" )
										|| errorMsgCode0x.toLowerCase()
												.contains(
														"is not defined in schema" ) ) {
									if ( pdftoolsH.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsH = pdftoolsH
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_H_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "transparen" ) ) {
									if ( pdftoolsE.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsE = pdftoolsE
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_E_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "action" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "aa" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "key 'a'" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "javascript" ) ) {
									if ( pdftoolsG.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsG = pdftoolsG
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_G_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase()
										.contains( "annotation" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "embedd" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "comment" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "structure" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "print" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "incorpor" )
										|| errorMsgCode0x.toLowerCase()
												.contains( "key f " )
										|| errorMsgCode0x.toLowerCase()
												.contains( "appearance" ) ) {
									if ( pdftoolsF.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsF = pdftoolsF
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_F_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}

								} else {
									if ( pdftoolsA.toLowerCase().contains(
											errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										pdftoolsA = pdftoolsA
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_A_PDFA )
												+ "<Message>" + errorMsgCode0x
												+ "</Message></Error>";
									}
								}
							}
						}

						// Kontrolle ob details noch existieren
						if ( pdftoolsA.equals( "" ) && pdftoolsB.equals( "" )
								&& pdftoolsC.equals( "" )
								&& pdftoolsD.equals( "" )
								&& pdftoolsE.equals( "" )
								&& pdftoolsF.equals( "" )
								&& pdftoolsG.equals( "" )
								&& pdftoolsH.equals( "" )
								&& pdftoolsI.equals( "" ) ) {
							isValid = true;
						}
					}
				}
			} else {
				// ohne pdftools auch keine Font validierung
				isValidFont = true;
			}
			isValidPdftools = isValid;

			// TODO: Validierung mit callas
			if ( callas && !isValid ) {
				// Validierung mit callas

				/*
				 * Nicht vergessen in
				 * "src/main/resources/config/applicationContext-services.xml"
				 * beim entsprechenden Modul die property anzugeben: <property
				 * name="configurationService" ref="configurationService" />
				 */
				String nEntry = configMap.get( "nentry" );
				boolean bNentryError = true;
				if ( nEntry.equalsIgnoreCase( "W" ) ) {
					bNentryError = false;
				}

				try {
					// Initialisierung callas -> existiert pdfaPilot in den
					// resources?
					String folderCallas = "callas_pdfaPilotServer_x64_Win_12-2-366_cli";
					/*
					 * Update von Callas: callas_pdfaPilotServer_Win_...-Version
					 * herunterladen, installieren, odner im Workbench umbenennen
					 * alle Dateine vom Ordner cli ersetzen aber lizenz.txt und
					 * N-Entry.kfpx muessen die alten bleiben
					 */

					File fpdfapilotExe = new File( dirOfJarPath + File.separator
							+ "resources" + File.separator + folderCallas
							+ File.separator + "pdfaPilot.exe" );
					if ( !fpdfapilotExe.exists() ) {
						// Keine callas Validierung moeglich

						/*
						 * Testen der Installation und System anhand
						 * 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf ->
						 * invalid
						 */
						if ( pdftools ) {
							callas = false;

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											ERROR_XML_CALLAS_MISSING2,
											fpdfapilotExe.getAbsolutePath() ) );
							isValid = false;
						} else {

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											ERROR_XML_CALLAS_MISSING,
											fpdfapilotExe.getAbsolutePath() ) );
							return false;
						}
					}
					String pdfapilotExe = fpdfapilotExe.getAbsolutePath();

					/*
					 * Aufbau command:
					 * 
					 * 1) pdfapilotExe: Pfad zum Programm pdfapilot
					 * 
					 * 2) analye: Befehl inkl optionen
					 * 
					 * 3) lang: Sprache
					 * 
					 * 4) valPath: Pfad zur Datei
					 * 
					 * 5) reportPath: Pfad zum Report
					 */

					String levelCallas = level.toLowerCase();

					String profile = dirOfJarPath + File.separator + "resources"
							+ File.separator + folderCallas + File.separator
							+ "N-Entry.kfpx";
					String analye = "-a --noprogress --nohits --level="
							+ levelCallas + " --profile=\"" + profile + "\"";
					String langConfig = getTextResourceService()
							.getText( locale, MESSAGE_XML_LANGUAGE );
					String lang = "-l=" + getTextResourceService()
							.getText( locale, MESSAGE_XML_LANGUAGE );
					String valPath = valDatei.getAbsolutePath();
					String reportPath = report.getAbsolutePath();

					if ( callas ) {
						if ( report.exists() ) {
							report.delete();
						}

						/*
						 * callas separat ausfuehren und Ergebnis in isValid
						 * zurueckgeben
						 */
						callasReturnCode = UtilCallas.execCallas( pdfapilotExe,
								analye, lang, valPath, reportPath );

						Util.copyFile( report, reportOriginal );

						if ( callasReturnCode == 0 ) {
							/*
							 * 0 PDF is valid PDF/A-file additional checks
							 * wihtout problems
							 * 
							 * 1 PDF is valid PDF/A-file but additional checks
							 * with problems severity info
							 * 
							 * 2 PDF is valid PDF/A-file but additional checks
							 * with problems severity warning
							 * 
							 * 3 PDF is valid PDF/A-file but additional checks
							 * with problems severity error --> N-Eintrag
							 * 
							 * 4 PDF is not a valid PDF/A-file
							 */

							String valPathTest = dirOfJarPath + File.separator
									+ "license" + File.separator
									+ "other_License" + File.separator
									+ "3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf";

							/*
							 * Testen der Installation und System anhand
							 * 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf ->
							 * invalid
							 */
							callasReturnCodeTest = UtilCallas.execCallas(
									pdfapilotExe, analye, lang, valPathTest,
									reportPath );

							// report des Testdurchlaufes loeschen
							if ( report.exists() ) {
								report.delete();
							}

							if ( callasReturnCodeTest == 0
									|| callasReturnCodeTest == 1
									|| callasReturnCodeTest == 2
									|| callasReturnCodeTest == 3 ) {
								// Keine callas Validierung moeglich
								configMap.put( "callas", "no" );

								// -callas_NO -Fileanlegen, damit in J nicht
								// validiert wird
								if ( !callasNo.exists() ) {
									try {
										callasNo.createNewFile();
									} catch ( IOException e ) {
										e.printStackTrace();
									}
								}

								if ( pdftools ) {
									callas = false;
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_A_PDFA )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_CALLAS_FATAL2,
																	fpdfapilotExe
																			.getAbsolutePath() ) );
									isValid = false;
								} else {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_A_PDFA )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_CALLAS_FATAL,
																	fpdfapilotExe
																			.getAbsolutePath() ) );
									return false;
								}
							}
							isValid = true;
						} else if ( callasReturnCode > 3 ) {
							isValid = false;
						} else if ( callasReturnCode == 1
								|| callasReturnCode == 2
								|| callasReturnCode == 3 ) {
							// Zusatzpruefung nicht bestanden
							String valPathTest = dirOfJarPath + File.separator
									+ "license" + File.separator
									+ "other_License" + File.separator
									+ "3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf";

							/*
							 * Testen der Installation und System anhand
							 * 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf ->
							 * invalid
							 */
							callasReturnCodeTest = UtilCallas.execCallas(
									pdfapilotExe, analye, lang, valPathTest,
									reportPath );

							// report des Testdurchlaufes loeschen
							if ( report.exists() ) {
								report.delete();
							}

							if ( callasReturnCodeTest == 0
									|| callasReturnCodeTest == 1
									|| callasReturnCodeTest == 2
									|| callasReturnCodeTest == 3 ) {
								// Keine callas Validierung moeglich

								// -callas_NO -Fileanlegen, damit in J nicht
								// validiert wird
								if ( !callasNo.exists() ) {
									try {
										callasNo.createNewFile();
									} catch ( IOException e ) {
										e.printStackTrace();
									}
								}

								if ( pdftools ) {
									callas = false;
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_A_PDFA )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_CALLAS_FATAL2,
																	fpdfapilotExe
																			.getAbsolutePath() ) );
									isValid = false;
								} else {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_A_PDFA )
													+ getTextResourceService()
															.getText( locale,
																	ERROR_XML_CALLAS_FATAL,
																	fpdfapilotExe
																			.getAbsolutePath() ) );
									return false;
								}
							}

							if ( bNentryError ) {
								// Zusatzpruefung nicht bestanden = Error
								isValid = false;
							} else {
								/*
								 * Zusatzpruefung nicht bestanden = Warnung aber
								 * valide
								 * 
								 * Warnung jetzt ausgeben
								 */
								String warning = "";
								isValid = true;
								if ( langConfig.equalsIgnoreCase( "de" ) ) {
									warning = "Warnung: Komponentenanzahl im N-Eintrag des PDF/A Output Intent stimmt nicht mit ICC-Profil ueberein. [callas] ";
								} else if ( langConfig
										.equalsIgnoreCase( "fr" ) ) {
									warning = "Avertissement: Le nombre de composants dans l'entree N des conditions de sortie PDF/A ne correspond pas au profil ICC. [callas] ";
								} else {
									warning = "Warning: Number of components in PDF/A OutputIntent N entry does not match ICC profile. [callas] ";
								}

								Logtxt.logtxt( logFile,
										getTextResourceService().getText(
												locale,
												MESSAGE_XML_MODUL_C_PDFA )
												+ "<Message>" + warning
												+ "</Message></Error>" );
							}
						} else {
							isValid = false;
							callasServiceFailed = true;
						}

						// Ende callas direkt auszuloesen
					}
				} catch ( Exception e ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN,
									"Exec pdfaPilot: " + e.getMessage() ) );
					return false;
				}
			}

			/** Modul J **/
			String jbig2allowed = configMap.get( "jbig2allowed" );
			if ( jbig2allowed.contentEquals( "yes" ) ) {
				// JBIG2 erlaubt kein Fehler moeglich und auch kein Test noetig
			} else {
				try {
					BufferedReader in = new BufferedReader(
							new FileReader( valDatei ) );
					String line;
					while ( (line = in.readLine()) != null ) {
						line = line.toLowerCase();
						if ( line.contains( "jbig2decode" ) ) {
							isValidJ = false;
							if ( min ) {
								in.close();
								return false;
							} else {
								break;
							}
						}
					}
					in.close();
					// set to null
					in = null;

				} catch ( Throwable e ) {
					if ( min ) {
					} else {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										" Modul J " + e.getMessage() ) );
					}
				}
			}

			// TODO: Erledigt: Fehler Auswertung
			if ( !isValid ) {
				if ( min ) {
					return false;
				} else {
					boolean exponent0 = false;
					boolean exponent1 = false;
					boolean exponent2 = false;
					boolean exponent3 = false;
					boolean exponent4 = false;
					boolean exponent5 = false;
					boolean exponent6 = false;
					boolean exponent7 = false;
					boolean exponent8 = false;
					boolean exponent9 = false;
					boolean exponent10 = false;
					boolean exponent11 = false;
					boolean exponent12 = false;
					boolean exponent13 = false;
					boolean exponent14 = false;
					boolean exponent15 = false;
					boolean exponent16 = false;
					boolean exponent17 = false;
					boolean exponent18 = false;

					int iExp0 = (int) Math.pow( 2, 0 );
					int iExp1 = (int) Math.pow( 2, 1 );
					int iExp2 = (int) Math.pow( 2, 2 );
					int iExp3 = (int) Math.pow( 2, 3 );
					int iExp4 = (int) Math.pow( 2, 4 );
					int iExp5 = (int) Math.pow( 2, 5 );
					int iExp6 = (int) Math.pow( 2, 6 );
					int iExp7 = (int) Math.pow( 2, 7 );
					int iExp8 = (int) Math.pow( 2, 8 );
					int iExp9 = (int) Math.pow( 2, 9 );
					int iExp10 = (int) Math.pow( 2, 10 );
					int iExp11 = (int) Math.pow( 2, 11 );
					int iExp12 = (int) Math.pow( 2, 12 );
					int iExp13 = (int) Math.pow( 2, 13 );
					int iExp14 = (int) Math.pow( 2, 14 );
					int iExp15 = (int) Math.pow( 2, 15 );
					int iExp16 = (int) Math.pow( 2, 16 );
					int iExp17 = (int) Math.pow( 2, 17 );
					int iExp18 = (int) Math.pow( 2, 18 );

					if ( pdftools ) {
						// Invalide Kategorien von PDF-Tools
						if ( iCategory >= iExp18 ) {
							exponent18 = true;
							iCategory = iCategory - iExp18;
						}
						if ( iCategory >= iExp17 ) {
							exponent17 = true;
							iCategory = iCategory - iExp17;
						}
						if ( iCategory >= iExp16 ) {
							exponent16 = true;
							iCategory = iCategory - iExp16;
						}
						if ( iCategory >= iExp15 ) {
							exponent15 = true;
							iCategory = iCategory - iExp15;
						}
						if ( iCategory >= iExp14 ) {
							exponent14 = true;
							iCategory = iCategory - iExp14;
						}
						if ( iCategory >= iExp13 ) {
							exponent13 = true;
							iCategory = iCategory - iExp13;
						}
						if ( iCategory >= iExp12 ) {
							exponent12 = true;
							iCategory = iCategory - iExp12;
						}
						if ( iCategory >= iExp11 ) {
							exponent11 = true;
							iCategory = iCategory - iExp11;
						}
						if ( iCategory >= iExp10 ) {
							exponent10 = true;
							iCategory = iCategory - iExp10;
						}
						if ( iCategory >= iExp9 ) {
							exponent9 = true;
							iCategory = iCategory - iExp9;
						}
						if ( iCategory >= iExp8 ) {
							exponent8 = true;
							iCategory = iCategory - iExp8;
						}
						if ( iCategory >= iExp7 ) {
							exponent7 = true;
							iCategory = iCategory - iExp7;
						}
						if ( iCategory >= iExp6 ) {
							exponent6 = true;
							iCategory = iCategory - iExp6;
						}
						if ( iCategory >= iExp5 ) {
							exponent5 = true;
							iCategory = iCategory - iExp5;
						}
						if ( iCategory >= iExp4 ) {
							exponent4 = true;
							iCategory = iCategory - iExp4;
						}
						if ( iCategory >= iExp3 ) {
							exponent3 = true;
							iCategory = iCategory - iExp3;
						}
						if ( iCategory >= iExp2 ) {
							exponent2 = true;
							iCategory = iCategory - iExp2;
						}
						if ( iCategory >= iExp1 ) {
							exponent1 = true;
							iCategory = iCategory - iExp1;
						}
						if ( iCategory >= iExp0 ) {
							exponent0 = true;
							iCategory = iCategory - iExp0;
						}
					} else {
						iCategory = 0;
					}

					/** Modul A **/
					String callasA = "";
					String callasB = "";
					String callasC = "";
					String callasD = "";
					String callasE = "";
					String callasF = "";
					String callasG = "";
					String callasH = "";
					String callasI = "";
					if ( callas ) {
						if ( callasServiceFailed ) {

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											ERROR_XML_SERVICEFAILED,
											"pdfaPilot", callasReturnCode ) );
						}

						// aus dem Output die Fehler holen
						// TODO: umschreiben

						try {
							BufferedReader br = new BufferedReader(
									new InputStreamReader( new FileInputStream(
											reportOriginal ) ) );

							/*
							 * Datei Zeile fuer Zeile lesen und ermitteln ob
							 * "Error" darin enthalten ist
							 * 
							 * Errors 1013 CharSet incomplete for Type 1 font
							 * Errors 9 OpenType font used Errors 790
							 * Transparency used (transparency group)
							 * 
							 * Error: The document structure is corrupt.
							 */
							for ( String line = br
									.readLine(); line != null; line = br
											.readLine() ) {
								int index = 0;

								line = line.replace( "ß", "ss" );
								line = line.replace( "�", "ss" );

								line = line.replace( "Ä", "Ae" );
								line = line.replace( "‘", "'" );
								line = line.replace( "’", "'" );
								line = line.replace( "Ò", "O" );
								line = line.replace( "Ó", "O" );
								line = line.replace( "Ô", "O" );
								line = line.replace( "Ö", "Oe" );
								line = line.replace( "œ", "oe" );
								line = line.replace( "Ü", "Ue" );
								line = line.replace( "á", "a" );
								line = line.replace( "â", "a" );
								line = line.replace( "ä", "ae" );
								line = line.replace( "ç", "c" );
								line = line.replace( "è", "e" );
								line = line.replace( "é", "e" );
								line = line.replace( "ê", "e" );
								line = line.replace( "ë", "e" );
								line = line.replace( "ì", "i" );
								line = line.replace( "í", "i" );
								line = line.replace( "î", "i" );
								line = line.replace( "ï", "i" );
								line = line.replace( "ö", "oe" );
								line = line.replace( "ù", "u" );
								line = line.replace( "ú", "u" );
								line = line.replace( "û", "u" );
								line = line.replace( "ü", "ue" );
								line = line.replace( "�", "a" );

								line = line.replace( "�", "Ae" );
								line = line.replace( "��", "'" );
								line = line.replace( "�", "'" );
								line = line.replace( "�", "O" );
								line = line.replace( "�", "O" );
								line = line.replace( "�", "O" );
								line = line.replace( "�", "Oe" );
								line = line.replace( "�", "oe" );
								line = line.replace( "�", "Ue" );
								line = line.replace( "�", "a" );
								line = line.replace( "�", "a" );
								line = line.replace( "�", "ae" );
								line = line.replace( "�", "c" );
								line = line.replace( "�", "e" );
								line = line.replace( "�", "e" );
								line = line.replace( "�", "e" );
								line = line.replace( "�", "e" );
								line = line.replace( "�", "i" );
								line = line.replace( "�", "i" );
								line = line.replace( "�", "i" );
								line = line.replace( "�", "i" );
								line = line.replace( "�", "oe" );
								line = line.replace( "�", "u" );
								line = line.replace( "�", "u" );
								line = line.replace( "�", "u" );
								line = line.replace( "�", "ue" );
								line = line.replace( "�", "a" );
								line = line.replace( "a��", "'" );

								/*
								 * Die Linien (Fehlermeldung von Callas) anhand
								 * von Woerter den Modulen zuordnen
								 * 
								 * A) Allgemeines B) Struktur C) Grafiken D)
								 * Schrift E) transparen F) annotation G) aktion
								 * H) metadata I) Zugaenglichkeit
								 */

								if ( line.startsWith( "Error" ) ) {
									// Error plus Zahl entfernen aus Linie
									index = line.indexOf( "\t", 8 );
									line = line.substring( index );
									if ( line.contains(
											"Komponentenanzahl im N-Eintrag des PDF/A Output Intent stimmt nicht mit ICC-Profil ueberein" )
											|| line.contains(
													"Number of components in PDF/A OutputIntent N entry does not match ICC profile" ) ) {
										// als zusatz im Log kennzeichnen
										line = line + " [callas] ";
									} else if ( line.contains(
											"Le nombre de composants dans l'entr" )
											&& line.contains(
													"N des conditions de sortie PDF/A ne correspond pas au profil ICC" ) ) {
										// als zusatz im Log kennzeichnen
										// enthaelt " l'entree� N " entsprechend
										// alles neu...
										line = "Le nombre de composants dans l'entree N des conditions de sortie PDF/A ne correspond pas au profil ICC [callas] ";
									} else {
										line = line + " [callas] ";
									}

									String callasAwarningDE = "Ungueltige PDF/A-Versionsnummer (muss \"2\" sein) [callas] ";
									String callasAwarningFR = "Numero de version PDF/A incorrect (doit etre 2) [callas] ";
									String callasAwarningEN = "Incorrect PDF/A version number (must be 2) [callas] ";
									if ( warning3to2.equalsIgnoreCase( "yes" )
											&& (line.contains(
													callasAwarningDE )
													|| line.contains(
															callasAwarningFR )
													|| line.contains(
															callasAwarningEN )) ) {
										// Fehler wird ignoriert. Es wird ggf
										// eine
										// Warnung ausgegeben.
										if ( !warning3to2done ) {
											// Fehler wurde noch nicht
											// ausgegeben.
											Logtxt.logtxt( logFile,
													pdf3warning );
											warning3to2done = true;
										}
									} else if ( line.toLowerCase()
											.contains( "schrift" )
											|| line.toLowerCase()
													.contains( "police" )
											|| line.toLowerCase()
													.contains( "font" )
											|| line.toLowerCase()
													.contains( "gly" )
											|| line.toLowerCase()
													.contains( "truetype" )
											|| line.toLowerCase()
													.contains( "unicode" )
											|| line.toLowerCase()
													.contains( "cid" )
											|| line.toLowerCase()
													.contains( "charset" ) ) {
										isValidCd = false;
										if ( callasD.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasD = callasD
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_D_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "grafiken" )
											|| line.toLowerCase()
													.contains( "graphique" )
											|| line.toLowerCase()
													.contains( "graphic" )
											|| line.toLowerCase()
													.contains( "image" )
											|| line.toLowerCase()
													.contains( "bild" )
											|| line.toLowerCase()
													.contains( "icc" )
											|| line.toLowerCase()
													.contains( "color" )
											|| line.toLowerCase()
													.contains( "couleur" )
											|| line.toLowerCase()
													.contains( "farb" )
											|| line.toLowerCase()
													.contains( "rgb" )
											|| line.toLowerCase()
													.contains( "rvb" )
											|| line.toLowerCase()
													.contains( "cmyk" )
											|| line.toLowerCase()
													.contains( "cmjn" )
											|| line.toLowerCase()
													.contains( "outputintent" )
											|| line.toLowerCase()
													.contains( "jpeg2000" )
											|| line.toLowerCase()
													.contains( "devicegray" )
											|| line.toLowerCase()
													.contains( "tr2" ) ) {
										isValidCc = false;
										if ( callasC.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasC = callasC
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_C_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "zugaenglich" )
											|| line.toLowerCase()
													.contains( "disponibi" )
											|| line.toLowerCase()
													.contains( "accessibi" )
											|| line.toLowerCase()
													.contains( "markinfo" )
											|| line.toLowerCase()
													.contains( "structree" )
											|| line.toLowerCase().contains(
													"structure tree root" )
											|| line.toLowerCase().contains(
													"strukturbaum" ) ) {
										isValidCi = false;
										if ( callasI.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasI = callasI
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_I_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "struktur" )
											|| line.toLowerCase()
													.contains( "ebenen" )
											|| line.toLowerCase()
													.contains( "structure" )
											|| line.toLowerCase()
													.contains( "lzw" )
											|| line.toLowerCase()
													.contains( " eol" ) ) {
										isValidCb = false;
										if ( callasB.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasB = callasB
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_B_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "metad" )
											|| line.toLowerCase()
													.contains( "xmp" ) ) {
										isValidCh = false;
										if ( callasH.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasH = callasH
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_H_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "transparen" ) ) {
										isValidCe = false;
										if ( callasE.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasE = callasE
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_E_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "aktion" )
											|| line.toLowerCase()
													.contains( "action" )
											|| line.toLowerCase()
													.contains( "aa" )
											|| line.toLowerCase().contains(
													"javascript" ) ) {
										isValidCg = false;
										if ( callasG.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasG = callasG
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_G_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else if ( line.toLowerCase()
											.contains( "annotation" )
											|| line.toLowerCase()
													.contains( "embedd" )
											|| line.toLowerCase()
													.contains( "komment" )
											|| line.toLowerCase()
													.contains( "comment" )
											|| line.toLowerCase()
													.contains( "structure" )
											|| line.toLowerCase()
													.contains( "drucke" )
											|| line.toLowerCase()
													.contains( "print" )
											|| line.toLowerCase()
													.contains( "imprim" )
											|| line.toLowerCase()
													.contains( "eingebette" )
											|| line.toLowerCase()
													.contains( "incorpor" ) ) {
										isValidCf = false;
										if ( callasF.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasF = callasF
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_F_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}

									} else {
										isValidCa = false;
										if ( callasA.toLowerCase().contains(
												line.toLowerCase() ) ) {
											// Fehlermeldung bereits erfasst ->
											// keine Aktion
										} else {
											callasA = callasA
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_MODUL_A_PDFA )
													+ "<Message>" + line
													+ "</Message></Error>";
										}
									}
								} else if ( line.startsWith( "Error:" ) ) {
									line = line.substring( 7 );
									line = line + " [callas] ";
									if ( callasA.toLowerCase()
											.contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										callasA = callasA
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_A_PDFA )
												+ "<Message>" + line
												+ "</Message></Error>";
									}
								} else if ( line.startsWith( "Error" ) ) {
									line = line.substring( 11 );
									line = line + " [callas] ";
									if ( callasA.toLowerCase()
											.contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst ->
										// keine Aktion
									} else {
										callasA = callasA
												+ getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_A_PDFA )
												+ "<Message>" + line
												+ "</Message></Error>";
									}
								}
							}

							br.close();
							// set to null
							br = null;
						} catch ( FileNotFoundException e ) {

							Logtxt.logtxt( logFile,
									getTextResourceService().getText( locale,
											MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText(
													locale, ERROR_XML_UNKNOWN,
													"FileNotFoundException" ) );
							return false;
						} catch ( Exception e ) {

							Logtxt.logtxt( logFile,
									getTextResourceService().getText( locale,
											MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText(
													locale, ERROR_XML_UNKNOWN,
													(e.getMessage() + " 1") ) ); //
							return false;
						}
						if ( !callasA.equals( "" ) ) {
							Logtxt.logtxt( logFile, callasA );
						}
					}

					if ( exponent1 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_1,
												"PDF Tools: iCategory_1" ) );
					}
					if ( exponent2 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_2,
												"PDF Tools: iCategory_2" ) );
						return false;
					}
					Logtxt.logtxt( logFile, pdftoolsA );

					/** Modul B **/
					if ( callas && !callasB.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasB );
					}
					if ( exponent0 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_B_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_0,
												"PDF Tools: iCategory_0" ) );
					}
					if ( exponent7 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_B_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_7,
												"PDF Tools: iCategory_7" ) );
					}
					if ( exponent18 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_B_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_18,
												"PDF Tools: iCategory_18" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsB );

					/** Modul C **/
					if ( callas && !callasC.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasC );
					}
					if ( exponent3 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_C_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_3,
												"PDF Tools: iCategory_3" ) );
					}
					if ( exponent4 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_C_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_4,
												"PDF Tools: iCategory_4" ) );
					}
					if ( exponent5 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_C_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_5,
												"PDF Tools: iCategory_5" ) );
					}
					if ( exponent6 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_C_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_6,
												"PDF Tools: iCategory_6" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsC );

					/** Modul D **/
					if ( callas && !callasD.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasD );
					}
					if ( exponent8 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_D_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_8,
												"PDF Tools: iCategory_8" ) );
					}
					if ( exponent9 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_D_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_9,
												"PDF Tools: iCategory_9" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsD );

					/** Modul E **/
					if ( callas && !callasE.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasE );
					}
					if ( exponent10 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_E_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_10,
												"PDF Tools: iCategory_10" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsE );

					/** Modul F **/
					if ( callas && !callasF.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasF );
					}
					if ( exponent11 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_F_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_11,
												"PDF Tools: iCategory_11" ) );
					}
					if ( exponent12 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_F_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_12,
												"PDF Tools: iCategory_12" ) );
					}
					if ( exponent13 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_F_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_13,
												"PDF Tools: iCategory_13" ) );
					}
					if ( exponent14 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_F_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_14,
												"PDF Tools: iCategory_14" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsF );

					/** Modul G **/
					if ( callas && !callasG.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasG );
					}
					if ( exponent15 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_G_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_15,
												"PDF Tools: iCategory_15" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsG );

					/** Modul H **/
					if ( callas && !callasH.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasH );
					}
					if ( exponent16 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_H_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_16,
												"PDF Tools: iCategory_16" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsH );

					/** Modul I **/
					if ( callas && !callasI.equals( "" ) ) {
						Logtxt.logtxt( logFile, callasI );
					}
					if ( exponent17 ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_I_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_AI_17,
												"PDF Tools: iCategory_17" ) );
					}
					Logtxt.logtxt( logFile, pdftoolsI );

					/** Modul J **/
					if ( !isValidJ ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_J_JBIG2 ) );
					}

					/** Modul K **/
					Logtxt.logtxt( logFile, errorK );

					try {
						docPdf.close();
						// Destroy the object
						docPdf.destroyObject();
					} catch ( Exception ed1 ) {
					}
				}
			} else if ( !isValidFont ) {
				isValid = false;
				if ( min ) {
					return false;
				} else {
					/** Modul J **/
					if ( !isValidJ ) {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_J_JBIG2 ) );
					}

					/** Modul K **/
					Logtxt.logtxt( logFile, errorK );

					try {
						docPdf.close();
						// Destroy the object
						docPdf.destroyObject();
					} catch ( Exception ed1 ) {
					}
				}
			} else {
				// Modul J noch ueberpruefen
				/** Modul J **/
				if ( !isValidJ ) {
					isValid = false;
					if ( min ) {
						return false;
					} else {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText(
												locale, ERROR_XML_J_JBIG2 ) );
					}
				}

				try {
					docPdf.close();
					// Destroy the object and set to null
					docPdf.destroyObject();
					docPdf = null;
					PdfValidatorAPI.terminate();
					File internLicenseFile = new File( directoryOfLogfile
							+ File.separator + ".useKOSTValLicense.txt" );
					if ( internLicenseFile.exists() ) {
						// interne Lizenz verwendet. Lizenz ueberschreiben
						internLicenseFile.delete();
						if ( internLicenseFile.exists() ) {
							internLicenseFile.deleteOnExit();
						}
						if ( internLicenseFile.exists() ) {
							Util.deleteFile( internLicenseFile );
						}
						PdfValidatorAPI.setLicenseKey( " " );
					}
				} catch ( Exception ed2 ) {
				}
				if ( errorK.isEmpty() ) {
					// System.out.println( "errorK.isEmpty" );
				} else {
					// ggf Warning Modul K ausgeben
					Logtxt.logtxt( logFile, errorK );
				}
			}
			if ( report.exists() ) {
				report.delete();
			}
			if ( reportOriginal.exists() ) {
				reportOriginal.delete();
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( locale,
							ERROR_XML_UNKNOWN, e.getMessage() + " _" ) );
		}
		try {
			PdfValidatorAPI.terminate();
			docPdf.close();
			// Destroy the object and set to null
			docPdf.destroyObject();
			docPdf = null;
			File internLicenseFile = new File( directoryOfLogfile
					+ File.separator + ".useKOSTValLicense.txt" );
			if ( internLicenseFile.exists() ) {
				// interne Lizenz verwendet. Lizenz ueberschreiben
				internLicenseFile.delete();
				if ( internLicenseFile.exists() ) {
					internLicenseFile.deleteOnExit();
				}
				if ( internLicenseFile.exists() ) {
					Util.deleteFile( internLicenseFile );
				}
				PdfValidatorAPI.setLicenseKey( " " );
			}
		} catch ( Exception ed3 ) {
		}

		/*
		 * durch die diversen nacharbeiten (Warnung anstelle Fehler) muss
		 * kontrolliert werden ob es valide mit Warnungen oder Invalide ist.
		 */

		if ( callas && isValidCa && isValidCb && isValidCc && isValidCd
				&& isValidCe && isValidCf && isValidCg && isValidCh
				&& isValidCi ) {
			isValidCallas = true;
		}

		if ( pdftools ) {
			// pdftools eingeschaltet
			if ( isValidPdftools ) {
				// Validierung mit pdftools bestanden
				isValid = true;
			} else {
				// Validierung mit pdftools nicht bestanden
				if ( callas ) {
					// callas eingeschaltet (dual)
					if ( isValidCallas ) {
						// Validierung mit callas bestanden
						isValid = true;
					} else {
						// Validierung mit pdftools&callas nicht bestanden
						isValid = false;
					}
				} else {
					// Keine Validierung mit Callas; bleibt invalid
					isValid = false;
				}
			}
		} else {
			// pdftools nicht eingeschaltet
			if ( callas ) {
				// callas eingeschaltet (simple)
				if ( isValidCallas ) {
					// Validierung mit callas bestanden
					isValid = true;
				} else {
					// Validierung mit callas nicht bestanden
					isValid = false;
				}
			} else {
				// Keine Validierung
				// Fehler bereits aufgefangen
				isValid = false;
			}
		}
		if ( !isValidFont || !isValidJ ) {
			isValid = false;
		}

		return isValid;
	}

	private String nodeToString( Node node )
	{
		String swString = "";
		try {
			StringWriter sw = new StringWriter();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
			t.transform( new DOMSource( node ), new StreamResult( sw ) );
			swString = sw.toString();
			sw.close();
		} catch ( TransformerException te ) {
			System.out.println( "nodeToString Transformer Exception" );
		} catch ( IOException e ) {
			e.printStackTrace();
			System.out.println( "IOException:" + e );
		}
		return swString;
	}
}