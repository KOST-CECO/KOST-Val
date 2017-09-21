/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2017 Claire Roethlisberger (KOST-CECO), Christian
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;
import com.pdftools.NativeLibrary;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
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

	private ConfigurationService	configurationService;

	public static String					NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationApdfvalidationException
	{
		@SuppressWarnings("unused")
		boolean valid = false;
		int iCategory = 999999999;

		// Version & Level herausfinden
		String pdfa1 = getConfigurationService().pdfa1();
		String pdfa2 = getConfigurationService().pdfa2();

		Integer pdfaVer1 = 0;
		Integer pdfaVer2 = 0;

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		// String pathToWorkDir = getConfigurationService().getPathToWorkDir();
		String pathToLogDir = getConfigurationService().getPathToLogfile();
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
				getTextResourceService().getText( MESSAGE_PDFAVALIDATION_VL, level ) );

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
					String nameOfSignature = getConfigurationService().getPathToDroidSignatureFile();
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
		boolean callas = false;
		boolean pdftools = false;

		String callasConfig = getConfigurationService().callas();
		String pdftoolsConfig = getConfigurationService().pdftools();

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
			// Create object
			PdfValidatorAPI docPdf = new PdfValidatorAPI();

			/* TODO: Erledigt Start mit PDFTools
			 * 
			 * Wenn pdftools eingeschaltet ist, wird immer zuerst pdftools genommen, da diese schneller
			 * ist alls callas */
			if ( pdftools ) {
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

				docPdf.setStopOnError( false );
				docPdf.setReportingLevel( 2 );

				// Error Category
				iCategory = docPdf.getCategories();
				/* die Zahl kann auch eine Summe von Kategorien sein z.B. 6144=2048+4096 -> getCategoryText
				 * gibt nur die erste Kategorie heraus (z.B. 2048) */

				int success = 0;

				PdfError err = docPdf.getFirstError();

				while ( err != null ) {
					success = success + 1;
					// Get next error
					err = docPdf.getNextError();
				}

				if ( success == 0 && iCategory == 0 ) {
					// valide
					isValid = true;
				}
			}

			// TODO: Validierung mit callas
			if ( callas && !isValid ) {
				// Validierung mit callas

				try {
					// Initialisierung callas -> existiert pdfaPilot in den resources?
					File fpdfapilotExe = new File( "resources" + File.separator
							+ "callas_pdfaPilotServer_Win_7.0.268" + File.separator + "cli" + File.separator
							+ "pdfaPilot.exe" );
					if ( !fpdfapilotExe.exists() ) {
						// Keine callas Validierung möglich
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText( ERROR_XML_CALLAS_MISSING ) );
						return false;
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

					String analye = "-a --noprogress --nohits --level=" + level;
					String lang = "-l="+getTextResourceService().getText( MESSAGE_XML_LANGUAGE );
					String valPath = valDatei.getAbsolutePath();
					String reportPath = report.getAbsolutePath();

					/* callas separat ausführen und Ergebnis in isValid zurückgeben */
					isValid = UtilCallas.execCallas( pdfapilotExe, analye, lang, valPath, reportPath );
					// Ende callas direkt auszulösen

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											e.getMessage() ) );
					return false;
				}
			}

			// TODO: Erledigt: Fehler Auswertung

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
				if ( exponent1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_1, "iCategory_1" ) );
				}
				if ( exponent2 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_2, "iCategory_2" ) );
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
				if ( callas ) {
					// aus dem Output die Fehler holen
					// TODO: umschreiben

					try {
						BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(
								report ), "ISO-8859-15" ) );

						/* Datei Zeile für Zeile lesen und ermitteln ob "Error" darin enthalten ist
						 * 
						 * Errors 1013 CharSet incomplete for Type 1 font Errors 9 OpenType font used Errors
						 * 790 Transparency used (transparency group)
						 * 
						 * Error: The document structure is corrupt. */
						for ( String line = br.readLine(); line != null; line = br.readLine() ) {
							int index = 0;
							if ( line.startsWith( "Errors" ) ) {
								// Errors plus Zahl entfernen aus Linie
								 index = line.indexOf("\t",7);
								 System.out.print (" "+index+" ");
								line=line.substring(index);
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA ) + "<Message>"
												+ line + "</Message></Error>" );
							}
							if ( line.startsWith( "Error:" ) ) {
								line=line.substring(7);
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA ) + "<Message>"
												+ line + "</Message></Error>" );
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
				}

				/** Modul B **/
				if ( exponent0 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_0, "iCategory_0" ) );
				}
				if ( exponent7 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_7, "iCategory_7" ) );
				}
				if ( exponent18 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_18, "iCategory_18" ) );
				}

				/** Modul C **/
				if ( exponent3 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_3, "iCategory_3" ) );
				}
				if ( exponent4 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_4, "iCategory_4" ) );
				}
				if ( exponent5 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_5, "iCategory_5" ) );
				}
				if ( exponent6 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_6, "iCategory_6" ) );
				}

				/** Modul D **/
				if ( exponent8 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_8, "iCategory_8" ) );
				}
				if ( exponent9 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_9, "iCategory_9" ) );
				}

				/** Modul E **/
				if ( exponent10 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_E_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_10, "iCategory_10" ) );
				}

				/** Modul F **/
				if ( exponent11 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_11, "iCategory_11" ) );
				}
				if ( exponent12 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_12, "iCategory_12" ) );
				}
				if ( exponent13 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_13, "iCategory_13" ) );
				}
				if ( exponent14 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_F_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_14, "iCategory_14" ) );
				}
				/** Modul G **/
				if ( exponent15 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_G_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_15, "iCategory_15" ) );
				}

				/** Modul H **/
				if ( exponent16 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_H_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_16, "iCategory_16" ) );
				}

				/** Modul I **/
				if ( exponent17 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_I_PDFA )
									+ getTextResourceService().getText( ERROR_XML_AI_17, "iCategory_17" ) );
				}

				/** Modul J **/
				// neu sind die Interaktionen (J) bei den Aktionen (G)

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
		return isValid;
	}

}