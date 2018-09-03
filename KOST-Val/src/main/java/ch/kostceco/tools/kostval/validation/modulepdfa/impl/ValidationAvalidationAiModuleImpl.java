/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.sql.Timestamp;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

import com.pdftools.*;
import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.util.UtilCallas;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAvalidationAiModule;

/** Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit callas und oder
 * PDF-Tools.
 * 
 * Folgendes ist Konfigurierbar: welche Validatoren verwendet werden sollen. Sollen beide verwendet
 * werden wird die Duale Validierung durchgeführt. Bei der dualen Validierung müssen beide
 * Validatoren die Datei als invalide betrachten, damit diese als invalid gilt. Bei Uneinigkeit gilt
 * diese als valid.
 * 
 * Es wird falls vorhanden die Vollversion von PDF-Tools verwendet. KOST-Val muss nicht angepasst
 * werden und verwendet automatisch den internen Schlüssel, sollte keine Vollversion existieren.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit PDF Tools und oder
 * callas. Die Fehler werden den Einzelnen Gruppen (Modulen) zugeordnet
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationAvalidationAiModuleImpl extends ValidationModuleImpl implements
		ValidationAvalidationAiModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap )
			throws ValidationApdfvalidationException
	{
		@SuppressWarnings("unused")
		boolean valid = false;
		int iCategory = 999999999;
		String errorK = "";
		// Create object
		PdfValidatorAPI docPdf = new PdfValidatorAPI();

		// Version & Level herausfinden
		String pdfa1 = configMap.get( "pdfa1" );
		String pdfa2 = configMap.get( "pdfa2" );

		Integer pdfaVer1 = 0;
		Integer pdfaVer2 = 0;

		String pathToLogDir = configMap.get( "PathToLogfile" );
		String pathToWorkDir = pathToLogDir;
		/* Beim schreiben ins Workverzeichnis trat ab und zu ein fehler auf. entsprechend wird es jetzt
		 * ins logverzeichnis geschrieben */

		String pathToPdfapilotOutput = pathToLogDir + File.separator + "callasTEMP.txt";
		File report = new File( pathToPdfapilotOutput );

		// falls das File bereits existiert, z.B. von einemvorhergehenden Durchlauf, löschen wir es
		if ( report.exists() ) {
			report.delete();
		}

		/* Neu soll die Validierung mit PDFTron konfigurier bar sein Mögliche Werte 1A, 1B und no sowie
		 * 2A, 2B, 2U und no Da Archive beide Versionen erlauben können sind es 2 config einträge Es
		 * gibt mehre Möglichkeiten das PDF in der gewünschten Version zu testen - Unterscheidung anhand
		 * DROID --> braucht viel Zeit auch mit KaD_Signaturefile - Unterscheidung anhand PDF/A-Eintrag
		 * wie Droid aber selber programmiert --> ist viel schneller */
		if ( pdfa2.equals( "2A" ) || pdfa2.equals( "2B" ) || pdfa2.equals( "2U" ) ) {
			// gültiger Konfigurationseintrag und V2 erlaubt
			pdfaVer2 = 2;
		} else {
			// v2 nicht erlaubt oder falscher eintrag
			pdfa2 = "no";
		}
		if ( pdfa1.equals( "1A" ) || pdfa1.equals( "1B" ) ) {
			// gültiger Konfigurationseintrag und V1 erlaubt
			pdfaVer1 = 1;
		} else {
			// v1 nicht erlaubt oder falscher eintrag
			pdfa1 = "no";
		}
		if ( pdfa1 == "no" && pdfa2 == "no" ) {
			// keine Validierung möglich. keine PDFA-Versionen konfiguriert
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_A_PDFA_NOCONFIG ) );
			valid = false;
			return false;
		}

		String level = "no";
		// Richtiges Level definieren
		// TODO: <pdfaid:conformance>A< evtl mit berücksichtigen
		if ( pdfaVer1 != 1 ) {
			// Level 1 nicht erlaubt --> Level 2
			level = pdfa2;
		} else if ( pdfaVer2 != 2 ) {
			// Level 2 nicht erlaubt --> Level 1
			level = pdfa1;
		} else {
			try {
				// Beide sind möglich --> Level je nach File auswählen
				pdfaVer1 = 0;
				pdfaVer2 = 0;
				BufferedReader in = new BufferedReader( new FileReader( valDatei ) );
				String line;
				while ( (line = in.readLine()) != null ) {
					// häufige Partangaben: pdfaid:part>1< pdfaid:part='1' pdfaid:part="1"
					if ( line.contains( "pdfaid:part" ) ) {
						// pdfaid:part
						if ( line.contains( "pdfaid:part>1<" ) ) {
							level = pdfa1;
							pdfaVer1 = 1;
						} else if ( line.contains( "pdfaid:part='1'" ) ) {
							level = pdfa1;
							pdfaVer1 = 1;
						} else if ( line.contains( "pdfaid:part=\"1\"" ) ) {
							level = pdfa1;
							pdfaVer1 = 1;
						} else if ( line.contains( "pdfaid:part>2<" ) ) {
							level = pdfa2;
							pdfaVer2 = 2;
						} else if ( line.contains( "pdfaid:part='2'" ) ) {
							level = pdfa2;
							pdfaVer2 = 2;
						} else if ( line.contains( "pdfaid:part=\"2\"" ) ) {
							level = pdfa2;
							pdfaVer2 = 2;
						} else if ( line.contains( "pdfaid:part" ) && line.contains( "1" ) ) {
							// PDFA-Version = 1
							level = pdfa1;
							pdfaVer1 = 1;
						} else if ( line.contains( "pdfaid:part" ) && line.contains( "2" ) ) {
							// PDFA-Version = 2
							level = pdfa2;
							pdfaVer2 = 2;
						}
					}
					if ( pdfaVer1 == 0 && pdfaVer2 == 0 ) {
						// der Part wurde nicht gefunden --> Level 2
						level = pdfa2;
					}
				}
				in.close();
			} catch ( Throwable e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			}
		}
		getMessageService().logError(
				getTextResourceService().getText( MESSAGE_FORMATVALIDATION_VL, level ) );

		// Start mit der Erkennung

		// Eine PDF Datei (.pdf / .pdfa) muss mit %PDF [25504446] beginnen
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_A_PDFA_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".pdf" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// Hex 25 in Char umwandeln
				String str1 = "25";
				int i1 = Integer.parseInt( str1, 16 );
				char c1 = (char) i1;
				// Hex 50 in Char umwandeln
				String str2 = "50";
				int i2 = Integer.parseInt( str2, 16 );
				char c2 = (char) i2;
				// Hex 44 in Char umwandeln
				String str3 = "44";
				int i3 = Integer.parseInt( str3, 16 );
				char c3 = (char) i3;
				// Hex 46 in Char umwandeln
				String str4 = "46";
				int i4 = Integer.parseInt( str4, 16 );
				char c4 = (char) i4;

				// auslesen der ersten 4 Zeichen der Datei
				int length;
				int i;
				char[] buffer = new char[4];
				length = read.read( buffer );
				for ( i = 0; i != length; i++ )
					;

				// die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c2c3c4
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { c1, c2, c3, c4 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					// höchstwahrscheinlich ein PDF da es mit 25504446 respektive %PDF beginnt
					valid = true;
				} else {
					// Droid-Erkennung, damit Details ausgegeben werden können
					String nameOfSignature = configMap.get( "PathToDroidSignatureFile" );
					if ( nameOfSignature == null ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText(
												MESSAGE_XML_CONFIGURATION_ERROR_NO_SIGNATURE ) );
						read.close();
						return false;
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText( MESSAGE_XML_CA_DROID ) );
						read.close();
						return false;
					}

					Droid droid = null;
					try {
						/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den
						 * Output stören Util.switchOffConsole() als Kommentar markieren wenn man die
						 * Fehlermeldung erhalten möchte */
						Util.switchOffConsole();
						droid = new Droid();

						droid.readSignatureFile( nameOfSignature );

					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText( ERROR_XML_CANNOT_INITIALIZE_DROID ) );
						read.close();
						return false;
					} finally {
						Util.switchOnConsole();
					}
					File file = valDatei;
					String puid = "";
					IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
					for ( int x = 0; x < ifile.getNumHits(); x++ ) {
						FileFormatHit ffh = ifile.getHit( x );
						FileFormat ff = ffh.getFileFormat();
						puid = ff.getPUID();
					}
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_A_PDFA_INCORRECTFILE, puid ) );
					read.close();
					return false;
				}
				read.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( ERROR_XML_A_PDFA_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit pdf oder pdfa -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_A_PDFA_INCORRECTFILEENDING ) );
			return false;
		}
		// Ende der Erkennung

		boolean isValid = false;
		boolean isValidFont = true;
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

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		if ( callasConfig.contentEquals( "yes" ) ) {
			// callas Validierung gewünscht
			callas = true;
		}
		if ( pdftoolsConfig.contentEquals( "yes" ) ) {
			// pdftools Validierung gewünscht
			pdftools = true;
		}

		try {

			/* TODO: Erledigt Start mit PDFTools
			 * 
			 * Wenn pdftools eingeschaltet ist, wird immer zuerst pdftools genommen, da dieser in
			 * KOST-Val schneller ist alls callas */
			if ( pdftools ) {
				try {
					if ( docPdf.open( valDatei.getAbsolutePath(), "", NativeLibrary.COMPLIANCE.ePDFUnk ) ) {
						// PDF Konnte geöffnet werden
						docPdf.setStopOnError( true );
						docPdf.setReportingLevel( 1 );
						if ( docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD ) {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_A_PDFTOOLS_ENCRYPTED ) );
							// Encrypt-Fileanlegen, damit in J nicht validiert wird
							File encrypt = new File( pathToWorkDir + File.separator + valDatei.getName()
									+ "_encrypt.txt" );
							if ( !encrypt.exists() ) {
								try {
									encrypt.createNewFile();
								} catch ( IOException e ) {
									e.printStackTrace();
								}
							}
							return false;
						}
					} else {
						if ( docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD ) {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_A_PDFTOOLS_ENCRYPTED ) );
							// Encrypt-Fileanlegen, damit in J nicht validiert wird
							File encrypt = new File( pathToWorkDir + File.separator + valDatei.getName()
									+ "_encrypt.txt" );
							if ( !encrypt.exists() ) {
								try {
									encrypt.createNewFile();
								} catch ( IOException e ) {
									e.printStackTrace();
								}
							}
							return false;
						} else {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_A_PDFTOOLS_DAMAGED ) );
							return false;
						}
					}

					docPdf = new PdfValidatorAPI();
					docPdf.setStopOnError( false );
					docPdf.setReportingLevel( 2 );

					/* ePDFA1a 5122 ePDFA1b 5121 ePDFA2a 5891 ePDFA2b 5889 ePDFA2u 5890 */
					if ( level.contentEquals( "1A" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5122 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "1B" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5121 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2A" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5891 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2B" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5889 ) ) {
							docPdf.validate();
						}
					} else if ( level.contentEquals( "2U" ) ) {
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5890 ) ) {
							docPdf.validate();
						}
					} else {
						// Validierung nach 2b
						level = "2B";
						if ( docPdf.open( valDatei.getAbsolutePath(), "", 5889 ) ) {
							docPdf.validate();
						}
					}

					// Anzahl errors
					PdfError err = docPdf.getFirstError();
					int success = 0;

					if ( err != null ) {
						for ( ; err != null; err = docPdf.getNextError() ) {
							success = success + 1;
						}
					}

					// Error Category
					iCategory = docPdf.getCategories();
					/* die Zahl kann auch eine Summe von Kategorien sein z.B. 6144=2048+4096 ->
					 * getCategoryText gibt nur die erste Kategorie heraus (z.B. 2048) */

					if ( success == 0 && iCategory == 0 ) {
						// valide
						isValid = true;
					}

					String fontYesNo = configMap.get( "pdfafont" );
					if ( fontYesNo.equalsIgnoreCase( "yes" ) ) {
						/* WriteFontValidationXML Method: Boolean WriteFontValidationXML(Stream outputStream)
						 * Write font validation information in XML format to a stream. This method must be
						 * called after Validate and before Close. For more information on the structure of the
						 * resulting XML, see the XML schema ValidatorFontInformation.xsd and the stylesheet
						 * ValidatorFontInformation.xsl in the documentation directory. Parameter: outputStream
						 * [Stream] The stream the font validation information is written to. Returns: True The
						 * font information has been written successfully. False Otherwise. */
						String pathToFontOutput = pathToLogDir + File.separator + valDatei.getName()
								+ "_FontValidation.xml";
						File fontReport = new File( pathToFontOutput );
						if ( fontReport.exists() ) {
							fontReport.delete();
						}
						String pathToFontOutputError = pathToLogDir + File.separator + valDatei.getName()
								+ "_FontValidation_Error.xml";
						File fontReportError = new File( pathToFontOutputError );
						if ( fontReportError.exists() ) {
							fontReportError.delete();
						}

						// Write font validation information
						FileStream fs = new FileStream( fontReport, "rw" );
						Stream xmlStream = fs;
						if ( !docPdf.writeFontValidationXML( xmlStream ) ) {
							// throw new
							// Exception(String.format("Failed to write font validation information: %s",
							// docPdf.getErrorMessage()));
							System.out.println( String.format( "Failed to write font validation information: %s",
									docPdf.getErrorMessage() ) );
						}
						fs.close();

						// TODO erledigt: Start der Font-Auswertung betreffend unbekannt und undefiniert
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
								outputChannel.transferFrom( inputChannel, 0, inputChannel.size() );
							} finally {
								inputChannel.close();
								outputChannel.close();
							}
							fis.close();
							fos.close();
							inputChannel.close();
							outputChannel.close();
						} catch ( Exception e ) {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
													"Exec PDF Tools FileChannel: " + e.getMessage() ) );
							return false;
						}

						Document doc = null;
						Document docError = null;

						BufferedInputStream bis = new BufferedInputStream(
								new FileInputStream( fontReportError ) );
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						doc = db.parse( bis );
						doc.normalize();

						// <characterCount>122</characterCount>
						// <characterUnknown>0</characterUnknown>
						// <characterUnknownPercentage>0</characterUnknownPercentage>
						// <characterUndefined>122</characterUndefined>
						// <characterUndefinedPercentage>100</characterUndefinedPercentage>

						String elementCount = doc.getElementsByTagName( "characterCount" ).item( 0 )
								.getTextContent();
						String elementUnknown = doc.getElementsByTagName( "characterUnknown" ).item( 0 )
								.getTextContent();
						String elementUnknownP = doc.getElementsByTagName( "characterUnknownPercentage" )
								.item( 0 ).getTextContent();
						String elementUndefined = doc.getElementsByTagName( "characterUndefined" ).item( 0 )
								.getTextContent();
						String elementUndefinedP = doc.getElementsByTagName( "characterUndefinedPercentage" )
								.item( 0 ).getTextContent();

						if ( !elementUnknown.equals( "0" ) || !elementUndefined.equals( "0" ) ) {
							try {
								isValidFont = false;
								errorK = errorK
										+ getTextResourceService().getText( MESSAGE_XML_MODUL_K_PDFA )
										+ getTextResourceService().getText( ERROR_XML_K_OVERVIEW, elementCount,
												elementUnknown, elementUnknownP, elementUndefined, elementUndefinedP );

								NodeList nodeCharacterLst = doc.getElementsByTagName( "character" );
								Set<Node> targetNode = new HashSet<Node>();

								for ( int s = 0; s < nodeCharacterLst.getLength(); s++ ) {
									boolean charUndef = false;
									boolean unicode = false;
									Node charNode = nodeCharacterLst.item( s );

									if ( charNode.hasAttributes() ) {
										NamedNodeMap attrs = charNode.getAttributes();
										for ( int i = 0; i < attrs.getLength(); i++ ) {
											Attr attribute = (Attr) attrs.item( i );
											String attName = attribute.getName();
											String attNameValue = attribute.getName() + " = " + attribute.getValue();
											// System.out.println( " -> " + attribute.getName() + " = " +
											// attribute.getValue() );

											/* -> cid = 60 -> glyphId = 60 -> unicode = U+FFFD -> unicodeUndefined = true */
											if ( attName.equalsIgnoreCase( "unicode" ) ) {
												unicode = true;
											}
											if ( attNameValue.equalsIgnoreCase( "unicodeUndefined = true" ) ) {
												charUndef = true;
											}
										}
										if ( !unicode ) {
											// System.out.println( " unicode nicht bekannt -> node interessant");
										} else if ( charUndef ) {
											// System.out.println( " unicode nicht definiert -> node interessant");
										} else {
											// System.out.println( " unicode bekannt -> dieser node kann geloescht werden"
											// );
											// Node zum leschen vormerken
											targetNode.add( charNode );
										}
									}
								}

								for ( Node e : targetNode ) {
									e.getParentNode().removeChild( e );
								}

								// write the content into xml file
								TransformerFactory transformerFactory = TransformerFactory.newInstance();
								Transformer transformer = transformerFactory.newTransformer();
								DOMSource source = new DOMSource( doc );
								StreamResult result = new StreamResult( fontReportError );
								// Output to console for testing
								// result = new StreamResult( System.out );

								transformer.transform( source, result );

								// Fonts ohne character loeschen
								BufferedInputStream bisError = new BufferedInputStream( new FileInputStream(
										fontReportError ) );
								DocumentBuilderFactory dbfError = DocumentBuilderFactory.newInstance();
								DocumentBuilder dbError = dbfError.newDocumentBuilder();
								docError = dbError.parse( bisError );
								docError.normalize();

								NodeList nodeFontLst = docError.getElementsByTagName( "font" );
								Set<Node> targetNodeFont = new HashSet<Node>();

								for ( int s = 0; s < nodeFontLst.getLength(); s++ ) {
									Node fontNode = nodeFontLst.item( s );

									NodeList nodeFontCharLst = fontNode.getChildNodes();
									if ( nodeFontCharLst.getLength() <= 1 ) {
										// font Node zum leschen vormerken
										targetNodeFont.add( fontNode );
									}

								}

								for ( Node f : targetNodeFont ) {
									f.getParentNode().removeChild( f );
								}
								docError.getDocumentElement().normalize();
								XPathExpression xpath = XPathFactory.newInstance().newXPath()
										.compile( "//text()[normalize-space(.) = '']" );
								NodeList blankTextNodes = (NodeList) xpath.evaluate( docError,
										XPathConstants.NODESET );

								for ( int i = 0; i < blankTextNodes.getLength(); i++ ) {
									blankTextNodes.item( i ).getParentNode().removeChild( blankTextNodes.item( i ) );
								}

								// Ende Bereinigung
								Node nodeInfo = docError.getElementsByTagName( "docInfo" ).item( 0 );
								String stringInfo = nodeToString( nodeInfo );
								Node nodeFonts = docError.getElementsByTagName( "fonts" ).item( 0 );
								String stringFonts = nodeToString( nodeFonts );
								errorK = errorK
										+ getTextResourceService()
												.getText( ERROR_XML_K_DETAIL, stringInfo, stringFonts );
								bisError.close();

							} catch ( Exception e ) {
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
														"Exec PDF Tools Font: " + e.getMessage() ) );
								return false;
							}
						}
						bis.close();
						fontReportError.deleteOnExit();
					}
				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											"Exec PDF Tools: " + e.getMessage() ) );
					return false;
				}
				if ( !isValid ) {

					// Ermittlung Detail-Fehlermeldungen von pdftools (entspricht -rd)
					PdfError err = docPdf.getFirstError();
					boolean rd = false;
					String detailConfig = configMap.get( "detail" );
					if ( detailConfig.equalsIgnoreCase( "detail" ) ) {
						rd = true;
					}
					if ( err != null && rd ) {
						for ( ; err != null; err = docPdf.getNextError() ) {
							// Ermittlung der einzelnen Error Code und Message
							int errorCode = err.getErrorCode();
							String errorCode0x = String.format( "0x%08X", errorCode );
							String errorMsg = err.getMessage();
							// Ausgabe
							String errorMsgCode0xText = errorMsg + " [PDF Tools: " + errorCode0x + "]";
							String errorMsgCode0x = " - " + errorMsgCode0xText;
							// System.out.println(errorMsgCode0x);

							/* TODO zu ignorierende Werte ignorieren und dann kontrollieren ob noch invalid
							 * (weitere Fehler existieren)
							 * 
							 * zB - The value of the key N is 4 but must be 3. [PDF Tools: 0x80410607] */
							String detailIgnore = configMap.get( "ignore" );

							if ( detailIgnore.contains( errorMsgCode0xText ) ) {
								// Fehler wird ignoriert. Entsprechend wird kein Detail geschrieben.
							} else {
								// Fehler wird nicht ignoriert und dem Modul zugeordnet
								if ( errorMsgCode0x.toLowerCase().contains( "graphics" )
										|| errorMsgCode0x.toLowerCase().contains( "image" )
										|| errorMsgCode0x.toLowerCase().contains( "interpolate" )
										|| errorMsgCode0x.toLowerCase().contains( "icc" )
										|| errorMsgCode0x.toLowerCase().contains( "color" )
										|| errorMsgCode0x.toLowerCase().contains( "colour" )
										|| errorMsgCode0x.toLowerCase().contains( "rgb" )
										|| errorMsgCode0x.toLowerCase().contains( "rvb" )
										|| errorMsgCode0x.toLowerCase().contains( "cmyk" )
										|| errorMsgCode0x.toLowerCase().contains( "cmjn" )
										|| errorMsgCode0x.toLowerCase().contains( "outputintent" )
										|| errorMsgCode0x.toLowerCase().contains( "jpeg2000" )
										|| errorMsgCode0x.toLowerCase().contains( "devicegray" )
										|| errorMsgCode0x.toLowerCase().contains( "tr2" ) ) {
									if ( pdftoolsC.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsC = pdftoolsC
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "police" )
										|| errorMsgCode0x.toLowerCase().contains( "font" )
										|| errorMsgCode0x.toLowerCase().contains( "gly" )
										|| errorMsgCode0x.toLowerCase().contains( "truetype" )
										|| errorMsgCode0x.toLowerCase().contains( "unicode" )
										|| errorMsgCode0x.toLowerCase().contains( "cid" )
										|| errorMsgCode0x.toLowerCase().contains( "charset" ) ) {
									if ( pdftoolsD.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsD = pdftoolsD
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "disponibi" )
										|| errorMsgCode0x.toLowerCase().contains( "accessibi" )
										|| errorMsgCode0x.toLowerCase().contains( "markinfo" )
										|| errorMsgCode0x.toLowerCase().contains( "structree" )
										|| errorMsgCode0x.toLowerCase().contains( "structure tree root" )
										|| errorMsgCode0x.toLowerCase().contains( "strukturbaum" ) ) {
									if ( pdftoolsI.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsI = pdftoolsI
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_I_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "structure" )
										|| errorMsgCode0x.toLowerCase().contains( " eol" ) ) {
									if ( pdftoolsB.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsB = pdftoolsB
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "metad" )
										|| errorMsgCode0x.toLowerCase().contains( "xmp" ) ) {
									if ( pdftoolsH.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsH = pdftoolsH
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_H_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "transparen" ) ) {
									if ( pdftoolsE.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsE = pdftoolsE
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_E_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "action" )
										|| errorMsgCode0x.toLowerCase().contains( "aa" ) ) {
									if ( pdftoolsG.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsG = pdftoolsG
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_G_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else if ( errorMsgCode0x.toLowerCase().contains( "annotation" )
										|| errorMsgCode0x.toLowerCase().contains( "embedd" )
										|| errorMsgCode0x.toLowerCase().contains( "comment" )
										|| errorMsgCode0x.toLowerCase().contains( "structure" )
										|| errorMsgCode0x.toLowerCase().contains( "print" )
										|| errorMsgCode0x.toLowerCase().contains( "incorpor" ) ) {
									if ( pdftoolsF.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsF = pdftoolsF
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}

								} else {
									if ( pdftoolsA.toLowerCase().contains( errorMsgCode0x.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										pdftoolsA = pdftoolsA
												+ getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
												+ "<Message>" + errorMsgCode0x + "</Message></Error>";
									}
								}
							}
						}

						// TODO Kontrolle ob details noch existieren
						if ( pdftoolsA.equals( "" ) && pdftoolsB.equals( "" ) && pdftoolsC.equals( "" )
								&& pdftoolsD.equals( "" ) && pdftoolsE.equals( "" ) && pdftoolsF.equals( "" )
								&& pdftoolsG.equals( "" ) && pdftoolsH.equals( "" ) && pdftoolsI.equals( "" ) ) {
							isValid = true;
						}
					}
				}
			}

			// TODO: Validierung mit callas
			if ( callas && !isValid ) {
				// Validierung mit callas

				/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
				 * entsprechenden Modul die property anzugeben: <property name="configurationService"
				 * ref="configurationService" /> */
				String nEntry = configMap.get( "nentry" );
				boolean bNentryError = true;
				if ( nEntry.equalsIgnoreCase( "W" ) ) {
					bNentryError = false;
				}

				try {
					// Initialisierung callas -> existiert pdfaPilot in den resources?
					/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO
					 * in allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
					 * 
					 * dirOfJarPath + File.separator +
					 * 
					 * erweitern. */
					String path = new java.io.File( KOSTVal.class.getProtectionDomain().getCodeSource()
							.getLocation().getPath() ).getAbsolutePath();
					path = path.substring( 0, path.lastIndexOf( "." ) );
					path = path + System.getProperty( "java.class.path" );
					String locationOfJarPath = path;
					String dirOfJarPath = locationOfJarPath;
					if ( locationOfJarPath.endsWith( ".jar" ) ) {
						File file = new File( locationOfJarPath );
						dirOfJarPath = file.getParent();
					}

					File fpdfapilotExe = new File( dirOfJarPath + File.separator + "resources"
							+ File.separator + "callas_pdfaPilotServer_Win_7.2.276_cli-a" + File.separator
							+ "pdfaPilot.exe" );
					if ( !fpdfapilotExe.exists() ) {
						// Keine callas Validierung möglich

						/* Testen der Installation und System anhand
						 * 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf -> invalid */
						if ( pdftools ) {
							callas = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_CALLAS_MISSING2,
													fpdfapilotExe.getAbsolutePath() ) );
							isValid = false;
						} else {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_CALLAS_MISSING,
													fpdfapilotExe.getAbsolutePath() ) );
							return false;
						}
					}
					String pdfapilotExe = fpdfapilotExe.getAbsolutePath();

					/* Aufbau command:
					 * 
					 * 1) pdfapilotExe: Pfad zum Programm pdfapilot
					 * 
					 * 2) analye: Befehl inkl optionen
					 * 
					 * 3) lang: Sprache
					 * 
					 * 4) valPath: Pfad zur Datei
					 * 
					 * 5) reportPath: Pfad zum Report */

					String profile = dirOfJarPath + File.separator + "resources" + File.separator
							+ "callas_pdfaPilotServer_Win_7.2.276_cli-a" + File.separator + "N-Entry.kfpx";
					String analye = "-a --noprogress --nohits --level=" + level + " --profile=\"" + profile
							+ "\"";
					String langConfig = getTextResourceService().getText( MESSAGE_XML_LANGUAGE );
					String lang = "-l=" + getTextResourceService().getText( MESSAGE_XML_LANGUAGE );
					String valPath = valDatei.getAbsolutePath();
					String valPathTest = dirOfJarPath + File.separator + "license" + File.separator
							+ "other_License" + File.separator + "3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf";
					String reportPath = report.getAbsolutePath();

					/* Testen der Installation und System anhand 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf
					 * -> invalid */
					callasReturnCodeTest = UtilCallas.execCallas( pdfapilotExe, analye, lang, valPathTest,
							reportPath );

					if ( callasReturnCodeTest == 0 || callasReturnCodeTest == 1 || callasReturnCodeTest == 2
							|| callasReturnCodeTest == 3 ) {
						// Keine callas Validierung möglich

						if ( pdftools ) {
							callas = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_CALLAS_FATAL2,
													fpdfapilotExe.getAbsolutePath() ) );
							isValid = false;
						} else {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( ERROR_XML_CALLAS_FATAL,
													fpdfapilotExe.getAbsolutePath() ) );
							return false;
						}
					}

					if ( callas ) {

						/* callas separat ausführen und Ergebnis in isValid zurückgeben */
						callasReturnCode = UtilCallas.execCallas( pdfapilotExe, analye, lang, valPath,
								reportPath );

						if ( callasReturnCode == 0 ) {
							/* 0 PDF is valid PDF/A-file additional checks wihtout problems
							 * 
							 * 1 PDF is valid PDF/A-file but additional checks with problems – severity info
							 * 
							 * 2 PDF is valid PDF/A-file but additional checks with problems – severity warning
							 * 
							 * 3 PDF is valid PDF/A-file but additional checks with problems – severity error -->
							 * N-Eintrag
							 * 
							 * 4 PDF is not a valid PDF/A-file */
							isValid = true;
						} else if ( callasReturnCode > 3 ) {
							isValid = false;
						} else if ( callasReturnCode == 1 || callasReturnCode == 2 || callasReturnCode == 3 ) {
							// Zusatzprüfung nicht bestanden

							if ( bNentryError ) {
								// Zusatzprüfung nicht bestanden = Error
								isValid = false;
							} else {
								/* Zusatzprüfung nicht bestanden = Warnung aber valide
								 * 
								 * Warnung jetzt ausgeben */
								String warning = "";
								isValid = true;
								if ( langConfig.equalsIgnoreCase( "de" ) ) {
									warning = "Warnung: Komponentenanzahl im N-Eintrag des PDF/A Output Intent stimmt nicht mit ICC-Profil ueberein. [callas] ";
								} else if ( langConfig.equalsIgnoreCase( "fr" ) ) {
									warning = "Avertissement: Le nombre de composants dans l'entree N des conditions de sortie PDF/A ne correspond pas au profil ICC. [callas] ";
								} else {
									warning = "Warning: Number of components in PDF/A OutputIntent N entry does not match ICC profile. [callas] ";
								}
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA ) + "<Message>"
												+ warning + "</Message></Error>" );
							}
						} else {
							isValid = false;
							callasServiceFailed = true;
						}

						// Ende callas direkt auszulösen
					}
				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											"Exec pdfaPilot: " + e.getMessage() ) );
					return false;
				}
			}

			// TODO: Erledigt: Fehler Auswertung
			if ( isValid ) {
				isValid = isValidFont;
			}

			if ( !isValid ) {
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
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText( ERROR_XML_A_CALLAS_SERVICEFAILED,
												callasReturnCode ) );
					}

					// aus dem Output die Fehler holen
					// TODO: umschreiben

					try {
						BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(
								report ) ) );

						/* Datei Zeile für Zeile lesen und ermitteln ob "Error" darin enthalten ist
						 * 
						 * Errors 1013 CharSet incomplete for Type 1 font Errors 9 OpenType font used Errors
						 * 790 Transparency used (transparency group)
						 * 
						 * Error: The document structure is corrupt. */
						for ( String line = br.readLine(); line != null; line = br.readLine() ) {
							int index = 0;

							line = line.replace( "ü", "ue" );
							line = line.replace( "ö", "oe" );
							line = line.replace( "ä", "ae" );
							line = line.replace( "é", "e" );
							line = line.replace( "è", "e" );
							line = line.replace( "ê", "e" );
							line = line.replace( "ë", "e" );
							line = line.replace( "à", "a" );
							line = line.replace( "â", "a" );
							line = line.replace( "û", "u" );
							line = line.replace( "ô", "o" );
							line = line.replace( "î", "i" );
							line = line.replace( "ï", "i" );
							line = line.replace( "ß", "ss" );

							/* Die Linien (Fehlermeldung von Callas) anhand von Wörter den Modulen zuordnen
							 * 
							 * A) Allgemeines B) Struktur C) Grafiken D) Schrift E) transparen F) annotation G)
							 * aktion H) metadata I) Zugaenglichkeit */

							if ( line.startsWith( "Errors" ) ) {
								// Errors plus Zahl entfernen aus Linie
								index = line.indexOf( "\t", 7 );
								line = line.substring( index );
								if ( line
										.contains( "Komponentenanzahl im N-Eintrag des PDF/A Output Intent stimmt nicht mit ICC-Profil ueberein" )
										|| line
												.contains( "Number of components in PDF/A OutputIntent N entry does not match ICC profile" ) ) {
									// als zusatz im Log kennzeichnen
									line = line + " [callas] ";
								} else if ( line.contains( "Le nombre de composants dans l'entree" )
										&& line
												.contains( "N des conditions de sortie PDF/A ne correspond pas au profil ICC" ) ) {
									// als zusatz im Log kennzeichnen
									// enthält " l'entreeÂ N " entsprechend alles neu...
									line = "Le nombre de composants dans l'entree N des conditions de sortie PDF/A ne correspond pas au profil ICC [callas] ";
								} else {
									line = line + " [callas] ";
								}

								if ( line.toLowerCase().contains( "grafiken" )
										|| line.toLowerCase().contains( "graphique" )
										|| line.toLowerCase().contains( "graphics" )
										|| line.toLowerCase().contains( "image" )
										|| line.toLowerCase().contains( "bild" ) || line.toLowerCase().contains( "icc" )
										|| line.toLowerCase().contains( "color" )
										|| line.toLowerCase().contains( "couleur" )
										|| line.toLowerCase().contains( "farb" ) || line.toLowerCase().contains( "rgb" )
										|| line.toLowerCase().contains( "rvb" ) || line.toLowerCase().contains( "cmyk" )
										|| line.toLowerCase().contains( "cmjn" )
										|| line.toLowerCase().contains( "outputintent" )
										|| line.toLowerCase().contains( "jpeg2000" )
										|| line.toLowerCase().contains( "devicegray" )
										|| line.toLowerCase().contains( "tr2" ) ) {
									if ( callasC.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasC = callasC + getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "schrift" )
										|| line.toLowerCase().contains( "police" )
										|| line.toLowerCase().contains( "font" ) || line.toLowerCase().contains( "gly" )
										|| line.toLowerCase().contains( "truetype" )
										|| line.toLowerCase().contains( "unicode" )
										|| line.toLowerCase().contains( "cid" )
										|| line.toLowerCase().contains( "charset" ) ) {
									if ( callasD.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasD = callasD + getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "zugaenglich" )
										|| line.toLowerCase().contains( "disponibi" )
										|| line.toLowerCase().contains( "accessibi" )
										|| line.toLowerCase().contains( "markinfo" )
										|| line.toLowerCase().contains( "structree" )
										|| line.toLowerCase().contains( "structure tree root" )
										|| line.toLowerCase().contains( "strukturbaum" ) ) {
									if ( callasI.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasI = callasI + getTextResourceService().getText( MESSAGE_XML_MODUL_I_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "struktur" )
										|| line.toLowerCase().contains( "structure" )
										|| line.toLowerCase().contains( " eol" ) ) {
									if ( callasB.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasB = callasB + getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "metad" )
										|| line.toLowerCase().contains( "xmp" ) ) {
									if ( callasH.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasH = callasH + getTextResourceService().getText( MESSAGE_XML_MODUL_H_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "transparen" ) ) {
									if ( callasE.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasE = callasE + getTextResourceService().getText( MESSAGE_XML_MODUL_E_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "aktion" )
										|| line.toLowerCase().contains( "action" )
										|| line.toLowerCase().contains( "aa" ) ) {
									if ( callasG.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasG = callasG + getTextResourceService().getText( MESSAGE_XML_MODUL_G_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else if ( line.toLowerCase().contains( "annotation" )
										|| line.toLowerCase().contains( "embedd" )
										|| line.toLowerCase().contains( "komment" )
										|| line.toLowerCase().contains( "comment" )
										|| line.toLowerCase().contains( "structure" )
										|| line.toLowerCase().contains( "drucke" )
										|| line.toLowerCase().contains( "print" )
										|| line.toLowerCase().contains( "imprim" )
										|| line.toLowerCase().contains( "eingebette" )
										|| line.toLowerCase().contains( "incorpor" ) ) {
									if ( callasF.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasF = callasF + getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}

								} else {
									if ( callasA.toLowerCase().contains( line.toLowerCase() ) ) {
										// Fehlermeldung bereits erfasst -> keine Aktion
									} else {
										callasA = callasA + getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
												+ "<Message>" + line + "</Message></Error>";
									}
								}
							} else if ( line.startsWith( "Error:" ) ) {
								line = line.substring( 7 );
								line = line + " [callas] ";
								if ( callasA.toLowerCase().contains( line.toLowerCase() ) ) {
									// Fehlermeldung bereits erfasst -> keine Aktion
								} else {
									callasA = callasA + getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ "<Message>" + line + "</Message></Error>";
								}
							} else if ( line.startsWith( "Error" ) ) {
								line = line.substring( 11 );
								line = line + " [callas] ";
								if ( callasA.toLowerCase().contains( line.toLowerCase() ) ) {
									// Fehlermeldung bereits erfasst -> keine Aktion
								} else {
									callasA = callasA + getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
											+ "<Message>" + line + "</Message></Error>";
								}
							}
						}

						br.close();
					} catch ( FileNotFoundException e ) {
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
														"FileNotFoundException" ) );
						return false;
					} catch ( Exception e ) {
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
														(e.getMessage() + " 1") ) ); //
						return false;
					}
					if ( !callasA.equals( "" ) ) {
						getMessageService().logError( callasA );
					}
				}

				if ( exponent1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_1, "PDF Tools: iCategory_1" ) );
				}
				if ( exponent2 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_2, "PDF Tools: iCategory_2" ) );
					// Encrypt-Fileanlegen, damit in J nicht validiert wird
					File encrypt = new File( pathToWorkDir + File.separator + valDatei.getName()
							+ "_encrypt.txt" );
					if ( !encrypt.exists() ) {
						try {
							encrypt.createNewFile();
						} catch ( IOException e ) {
							e.printStackTrace();
						}
					}
					return false;
				}
				getMessageService().logError( pdftoolsA );

				/** Modul B **/
				if ( callas && !callasB.equals( "" ) ) {
					getMessageService().logError( callasB );
				}
				if ( exponent0 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_0, "PDF Tools: iCategory_0" ) );
				}
				if ( exponent7 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_7, "PDF Tools: iCategory_7" ) );
				}
				if ( exponent18 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_18, "PDF Tools: iCategory_18" ) );
				}
				getMessageService().logError( pdftoolsB );

				/** Modul C **/
				if ( callas && !callasC.equals( "" ) ) {
					getMessageService().logError( callasC );
				}
				if ( exponent3 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_3, "PDF Tools: iCategory_3" ) );
				}
				if ( exponent4 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_4, "PDF Tools: iCategory_4" ) );
				}
				if ( exponent5 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_5, "PDF Tools: iCategory_5" ) );
				}
				if ( exponent6 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_6, "PDF Tools: iCategory_6" ) );
				}
				getMessageService().logError( pdftoolsC );

				/** Modul D **/
				if ( callas && !callasD.equals( "" ) ) {
					getMessageService().logError( callasD );
				}
				if ( exponent8 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_8, "PDF Tools: iCategory_8" ) );
				}
				if ( exponent9 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_9, "PDF Tools: iCategory_9" ) );
				}
				getMessageService().logError( pdftoolsD );

				/** Modul E **/
				if ( callas && !callasE.equals( "" ) ) {
					getMessageService().logError( callasE );
				}
				if ( exponent10 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_E_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_10, "PDF Tools: iCategory_10" ) );
				}
				getMessageService().logError( pdftoolsE );

				/** Modul F **/
				if ( callas && !callasF.equals( "" ) ) {
					getMessageService().logError( callasF );
				}
				if ( exponent11 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_11, "PDF Tools: iCategory_11" ) );
				}
				if ( exponent12 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_12, "PDF Tools: iCategory_12" ) );
				}
				if ( exponent13 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_13, "PDF Tools: iCategory_13" ) );
				}
				if ( exponent14 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_14, "PDF Tools: iCategory_14" ) );
				}
				getMessageService().logError( pdftoolsF );

				/** Modul G **/
				if ( callas && !callasG.equals( "" ) ) {
					getMessageService().logError( callasG );
				}
				if ( exponent15 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_G_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_15, "PDF Tools: iCategory_15" ) );
				}
				getMessageService().logError( pdftoolsG );

				/** Modul H **/
				if ( callas && !callasH.equals( "" ) ) {
					getMessageService().logError( callasH );
				}
				if ( exponent16 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_H_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_16, "PDF Tools: iCategory_16" ) );
				}
				getMessageService().logError( pdftoolsH );

				/** Modul I **/
				if ( callas && !callasI.equals( "" ) ) {
					getMessageService().logError( callasI );
				}
				if ( exponent17 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_I_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_17, "PDF Tools: iCategory_17" ) );
				}
				getMessageService().logError( pdftoolsI );

				/** Modul J **/
				// neu sind die Interaktionen (J) bei den Aktionen (G)

				/** Modul K **/
				getMessageService().logError( errorK );

				docPdf.close();

				// Destroy the object
				docPdf.destroyObject();

			}
			if ( report.exists() ) {
				report.delete();
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		docPdf.close();

		// Destroy the object
		docPdf.destroyObject();
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