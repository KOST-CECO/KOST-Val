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

package ch.kostceco.tools.kostval.validation.modulepng.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulepng.ValidationApngvalidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepng.ValidationAvalidationPngModule;
import coderslagoon.badpeggy.scanner.ImageScanner.Callback;
import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

/** Ist die vorliegende PNG-Datei eine valide PNG-Datei? PNG Validierungs mit pngcheck.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit pngcheck.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationAvalidationPngModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationPngModule, Callback
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationApngvalidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		// Start mit der Erkennung

		Boolean reco = false;

		// Eine PNG Datei (.png) muss mit 89 50 4e 47 0d 0a 1a 0a (hex) beginnen
		if ( valDatei.isDirectory() ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_ISDIRECTORY ) );
				return false;
			}
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".png" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// die ersten 4 Zeichen einer PNG Datei

				// 1 89
				// 2 50
				// 3 4e
				// 4 47

				// Open the file using FileInputStream
				try (FileInputStream fis = new FileInputStream( valDatei )) {
					// A variable to hold a single byte of the file data
					int i = 0;

					// A counter to print a new line every 16 bytes read.
					int cnt = 0;

					// Read till the end of the file and print the byte in hexadecimal valueS.
					StringBuilder sb = new StringBuilder();
					String sb2str1 = "";
					String sb2str2 = "";
					String sb2str3 = "";
					String sb2str4 = "";
					String sb1234 = "";
					while ( (i = fis.read()) != -1 ) {
						// System.out.printf("%02X ", i);
						sb.append( String.format( "%02X ", i ) );
						if ( sb2str1 == "" ) {
							sb2str1 = sb + "";
						} else if ( sb2str2 == "" ) {
							sb2str2 = sb + "";
						} else if ( sb2str3 == "" ) {
							sb2str3 = sb + "";
						} else if ( sb2str4 == "" ) {
							sb2str4 = sb + "";
							sb1234 = sb + "";
							break;
						}
						cnt++;
						if ( cnt == 16 ) {
							cnt = 0;
						}
					}
					if ( sb1234.contains( "89 50 4E 47" ) ) {
						reco = true;
					}
				}

				if ( reco ) {
					/* hoechstwahrscheinlich ein PNG da es mit 89504E47 beginnt */
				} else {
					// Droid-Erkennung, damit Details ausgegeben werden koennen

					/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
					 * entsprechenden Modul die property anzugeben: <property name="configurationService"
					 * ref="configurationService" /> */
					String nameOfSignature = configMap.get( "PathToDroidSignatureFile" );
					if ( nameOfSignature.startsWith( "Configuration-Error:" ) ) {
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
											+ nameOfSignature );
							return false;
						}
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
											+ getTextResourceService().getText( locale, MESSAGE_XML_CA_DROID ) );
							return false;
						}
					}

					Droid droid = null;
					try {
						/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den
						 * Output stoeren Util.switchOffConsole() als Kommentar markieren wenn man die
						 * Fehlermeldung erhalten moechte */
						Util.switchOffConsole();
						droid = new Droid();

						droid.readSignatureFile( nameOfSignature );

					} catch ( Exception e ) {
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService().logError( getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_PNG )
									+ getTextResourceService().getText( locale, ERROR_XML_CANNOT_INITIALIZE_DROID ) );
							return false;
						}
					} finally {
						Util.switchOnConsole();
					}
					File file = valDatei;
					String puid = " ??? ";
					IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
					for ( int x = 0; x < ifile.getNumHits(); x++ ) {
						FileFormatHit ffh = ifile.getHit( x );
						FileFormat ff = ffh.getFileFormat();
						puid = ff.getPUID();
					}
					read.close();
					if ( min ) {
						return false;
					} else {
						getMessageService().logError( getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_INCORRECTFILE, puid ) );
						return false;
					}
				}
				read.close();
			} catch ( Exception e ) {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit png -> Fehler
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_INCORRECTFILEENDING ) );
				return false;
			}
		}
		// Ende der Erkennung

		boolean isValid = false;

		// TODO: Erledigt: PNG Validierung

		if ( valDatei.getName().contains( "*" ) ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_SPECIAL_CHARACTER,
										valDatei.getName(), "*" ) );
				return false;
			}
		} else if ( valDatei.getName().contains( "?" ) ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_SPECIAL_CHARACTER,
										valDatei.getName(), "?" ) );
				return false;
			}
		} else if ( valDatei.getName().contains( "<" ) || valDatei.getName().contains( ">" ) ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_SPECIAL_CHARACTER,
										valDatei.getName(), "<" ) );
				return false;
			}
		} else if ( valDatei.getName().contains( ">" ) ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_SPECIAL_CHARACTER,
										valDatei.getName(), ">" ) );
				return false;
			}
		}

		// TODO: Erledigt - Initialisierung pngcheck -> existiert pngcheck?

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

		String pathToPngcheckExe = dirOfJarPath + File.separator + "resources" + File.separator
				+ "pngcheck-3.0.2-win32" + File.separator + "pngcheck.win32.exe";

		File fPngcheckExe = new File( pathToPngcheckExe );
		if ( !fPngcheckExe.exists() ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_PNGCHECK_MISSING ) );
			}
		}

		pathToPngcheckExe = "\"" + pathToPngcheckExe + "\"";

		try {
			File report;
			try {
				String outputPath = directoryOfLogfile.getAbsolutePath();
				String outputName = File.separator + valDatei.getName() + ".Pngcheck.txt";
				String pathToPngcheckReport = outputPath + outputName;
				File output = new File( pathToPngcheckReport );
				Runtime rt = Runtime.getRuntime();
				Process proc = null;

				try {
					report = output;

					// falls das File von einem vorhergehenden Durchlauf bereits existiert, loeschen wir es
					if ( report.exists() ) {
						report.delete();
					}

					/* Das redirect Zeichen verunmoeglicht eine direkte eingabe. mit dem geschachtellten
					 * Befehl gehts: cmd /c\"urspruenlicher Befehl\" */
					String command = "cmd /c \"" + pathToPngcheckExe + " \"" + valDatei.getAbsolutePath()
							+ "\" > \"" + output.getAbsolutePath() + "\"\"";
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Warte, bis proc fertig ist
					proc.waitFor();

				} catch ( Exception e ) {
					if ( min ) {
						return false;
					} else {
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
										+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_SERVICEFAILED,
												e.getMessage() ) );
						return false;
					}
				} finally {
					if ( proc != null ) {
						proc.getOutputStream().close();
						proc.getInputStream().close();
						proc.getErrorStream().close();
					}
				}
				if ( report.exists() ) {
					// alles io
				} else {
					// Datei nicht angelegt...
					if ( min ) {
						return false;
					} else {
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
										+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_NOREPORT ) );
						return false;
					}
				}

				// Ende Pngcheck direkt auszuloesen

				// TODO: Erledigt - Ergebnis auslesen

				BufferedReader reader = new BufferedReader( new FileReader( pathToPngcheckReport ) );
				String line = null;
				while ( (line = reader.readLine()) != null ) {
					/* OK: {Dateipfad} (465x224, 8-bit palette, non-interlaced, 86.7%).
					 * 
					 * {Dateipfad} invalid IHDR image type (1) ERROR: {Dateipfad} */
					if ( line.startsWith( "OK: " ) ) {
						isValid = true;
						// While beenden
						break;
					} else {
						if ( min ) {
							reader.close();
							report.delete();
							return false;
						} else {
							isValid = false;
							// Erster Fehler! Meldung A ausgeben und invalid setzten
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
											+ getTextResourceService().getText( locale, ERROR_XML_A_PNG_PNGCHECK_FAIL ) );
							if ( line.startsWith( valDatei.getAbsolutePath() ) ) {
								// Linie mit der Fehlermeldung
								String errorMsgOrig = line.replace( valDatei.getAbsolutePath(), "" );
								// System.out.println( "" );
								// System.out.println( valDatei.getAbsolutePath() );
								// System.out.println( errorMsgOrig );

								// TODO: Erledigt: Fehler Auswertung
								String modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_PNG );
								String msg = errorMsgOrig;
								if ( errorMsgOrig.contains( "IHDR" ) || errorMsgOrig.contains( "IEND" ) ) {
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_PNG );
								} else if ( errorMsgOrig.contains( "PLTE" ) || errorMsgOrig.contains( "bKGD" )
										|| errorMsgOrig.contains( "cHRM" ) || errorMsgOrig.contains( "hIST" )
										|| errorMsgOrig.contains( "iCCP" ) || errorMsgOrig.contains( "sPLT" )
										|| errorMsgOrig.contains( "sRGB" ) || errorMsgOrig.contains( "tRNS" ) ) {
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_PNG );
								} else if ( errorMsgOrig.contains( "IDAT" ) ) {
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_D_PNG );
								} else if ( errorMsgOrig.contains( "dSIG" ) || errorMsgOrig.contains( "eXIf" )
										|| errorMsgOrig.contains( "iTXt" ) || errorMsgOrig.contains( "tEXt" )
										|| errorMsgOrig.contains( "tIME" ) || errorMsgOrig.contains( "zTXt" ) ) {
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_E_PNG );
								} else if ( errorMsgOrig.contains( "gAMA" ) || errorMsgOrig.contains( "pHYs" )
										|| errorMsgOrig.contains( "sTER" ) || errorMsgOrig.contains( "sBIT" ) ) {
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_PNG );
								}

								if ( msg.contains( "additional data after IEND chunk" ) ) {
									// additional data after IEND chunk
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "Zusaetzliche Daten nach IEND-Chunk.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "Donnees supplementaires apres le chunk IEND.";
									} else {
										msg = "Additional data after IEND chunk.";
									}
								} else if ( msg.contains( "must precede" ) ) {
									// bKGD must precede IDAT
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "must precede", "muss diesem vorausgehen" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "must precede", "doit preceder ceci" );
									} else {
										// msg ok
									}
								} else if ( msg
										.contains( "this is neither a PNG or JNG image nor a MNG stream" ) ) {
									// this is neither a PNG or JNG image nor a MNG stream
									// die meisten dieser Fehler muessten bereits abgefangen sein
									msg = msg + ".";
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "Es handelt sich nicht um einen PNG-Stream.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "Ce n`est pas un stream PNG.";
									} else {
										msg = "This is not a PNG stream.";
									}
								} else if ( msg
										.contains( "illegal (unless recently approved) unknown, public chunk" ) ) {
									// illegal (unless recently approved) unknown, public chunk aDAT
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "illegal (unless recently approved) unknown, public chunk",
												"Illegales (sofern nicht kuerzlich genehmigt) unbekanntes, oeffentliches Chunk" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "illegal (unless recently approved) unknown, public chunk",
												"illegal (sauf approbation recente) inconnu, public chunk" );
									} else {
										msg = msg.replace( "illegal", "Illegal" );
									}
								} else if ( msg.contains( "CRC error in chunk" ) ) {
									// CRC error in chunk bKGD (computed dfe780ae, expected 1f5dec03)
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "CRC error in chunk", "CRC-Fehler in folgendem Chunk:" );
										msg = msg.replace( "computed", "berechnet" );
										msg = msg.replace( "expected", "erwartet" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "CRC error in chunk", "Erreur CRC dans le chunk" );
										msg = msg.replace( "computed", "calcule" );
										msg = msg.replace( "expected", "attendu" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "EOF while reading" ) ) {
									// EOF while reading CRC value
									msg = msg + ".";
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "EOF while reading",
												"Unerwartetes Ende (EOF) beim Lesen von" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "EOF while reading", "Fin inattendue (EOF) en lisant" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "cannot read PNG or MNG signature" ) ) {
									// cannot read PNG or MNG signature
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "PNG-Signatur kann nicht gelesen werden.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "La signature en PNG ne peut pas etre lue.";
									} else {
										msg = "Cannot read PNG signature.";
									}
								} else if ( msg.contains( "multiple" ) ) {
									// multiple bKGD not allowed
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "multiple", "Nur ein" );
										msg = msg.replace( " not allowed", "-Chunk erlaubt" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "multiple", "Un seul" );
										msg = msg.replace( "not allowed", "chunk autorise" );
									} else {
										msg = msg.replace( "multiple", "Multiple" );
									}
								} else if ( msg.startsWith( "invalid number of" )
										|| msg.startsWith( " invalid number of" )
										|| msg.startsWith( "  invalid number of" ) ) {
									// invalid number of hIST entries (14)
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "invalid number of", "Unguelige Anzahl von" );
										msg = msg.replace( "entries", "Eintraegen" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "invalid number of", "Nombre d'entrees" );
										msg = msg.replace( "entries", "non valables" );
									} else {
										msg = msg.replace( "invalid number of", "Invalid number of" );
									}
								} else if ( msg.startsWith( "invalid" ) || msg.startsWith( " invalid" )
										|| msg.startsWith( "  invalid" ) ) {
									// invalid bKGD length
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "invalid", "Ungueltiges Element:" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "invalid", "Element invalide:" );
									} else {
										msg = msg.replace( "invalid", "Invalid" );
									}
								} else if ( msg.contains( "file doesn't end with an IEND chunk" ) ) {
									// file doesn't end with an IEND chunk
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "Die Datei endet nicht mit einem IEND-Chunk.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "Le fichier ne se termine pas par le chunk IEND.";
									} else {
										msg = "File doesn't end with an IEND chunk.";
									}
								} else if ( msg.contains( "first chunk must be IHDR" ) ) {
									// first chunk must be IHDR
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "Erster Chunk muss IHDR sein.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "Le premier chunk doit etre IHDR.";
									} else {
										msg = "First chunk must be IHDR.";
									}
								} else if ( msg.contains( "keyword is longer than" ) ) {
									// tEXt keyword is longer than 79 characters
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( " keyword is longer than",
												"-Schluesselwort ist laenger als" );
										msg = msg.replace( "characters", "Zeichen" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "keyword is longer than", "le mot-cle comporte plus de" );
										msg = msg.replace( "characters", "caracteres" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "text contains" ) ) {
									// tEXt text contains NULL character(s)
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( " text contains", "-Text enthaelt" );
										msg = msg.replace( "  character(s)", "-Zeichen(e)" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "text contains",
												"texte contient un ou plusieurs caracteres" );
										msg = msg.replace( " character(s)", "" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "not allowed with" ) ) {
									// iCCP not allowed with sRGB
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "not allowed with", "nicht erlaubt mit" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "not allowed with", "n`est pas autorisee avec" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "falls outside" ) ) {
									// bKGD index (1) falls outside PLTE (1)
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "falls outside", "faellt aus" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "falls outside", "tombe hors" );
									} else {
										// msg ok
									}
								} else if ( msg.contains( "CORRUPTED by text conversion" ) ) {
									// CORRUPTED by text conversion
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = "Durch Textkonvertierung beschaedigt.";
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = "Corrompu par la conversion du texte.";
									} else {
										msg = "Corrupted by text conversion.";
									}
								} else if ( msg.contains( "not allowed in" ) ) {
									// PLTE not allowed in grayscale image
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "not allowed in", "nicht erlaubt in" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "not allowed in", "non autorisés en" );
									} else {
										msg = msg.replace( "not allowed in", "Not allowed in" );
									}
								} else if ( msg.contains( "private (invalid?)" ) ) {
									// private (invalid?) IDAT row-filter type (136) (warning)
									msg = msg.replace( " (warning)", "." );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "private (invalid?)", "Warnung: Privat (ungueltig?)" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "private (invalid?)", "Avertissement: Prive (invalide ?)" );
									} else {
										msg = msg.replace( "private (invalid?)", "Warning: Private (invalid?)" );
									}
								} else if ( msg.contains( "(trying to skip" ) ) {
									// (trying to skip MacBinary header)
									msg = msg + ".";
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "(trying to skip",
												"Ein allgemeines Strukturproblem ist aufgetreten (" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "(trying to skip",
												"Un probleme structurel general est apparu (" );
									} else {
										msg = msg.replace( "(trying to skip",
												"A general structural problem has arisen (" );
									}
								} else if ( msg.contains( "zlib" ) && msg.contains( "(data error)" ) ) {
									// zlib: inflate error = -3 (data error)
									modul = getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG );
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "zlib:", "Daten Komprimierungsfehler	zlib:" );
										msg = msg.replace( " (data error)", "." );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "zlib:", "Erreur de compression des donnees zlib:" );
										msg = msg.replace( " (data error)", "." );
									} else {
										msg = msg.replace( "zlib:", "Data compression error	zlib:" );
										msg = msg.replace( " (data error)", "." );
									}
									/* TODO: Hier neue zu uebersetztende Fehlemeldungen eintragen
									 * 
									 * Gemeldete Fehler: */

								} else if ( msg.contains( "no " ) && msg.contains( " chunks" ) ) {
									// no IDAT chunks
									msg = msg + ".";
									if ( locale.toString().startsWith( "de" ) ) {
										msg = msg.replace( "no", "Keine" );
										msg = msg.replace( " chunks", "-Chunks" );
									} else if ( locale.toString().startsWith( "fr" ) ) {
										msg = msg.replace( "no", "Pas de chunk" );
										msg = msg.replace( " chunks", "" );
									} else {
										msg = msg.replace( "no", "no" );
									}
								} else {
									// Fehler noch nicht uebersetzt (DE und FR)
									msg = getTextResourceService().getText( locale, ERROR_XML_AF_PNG_TRANSLATE, msg );
								}

								// System.out.println(modul+" "+msg);
								getMessageService().logError( modul
										+ getTextResourceService().getText( locale, ERROR_XML_AF_PNG_ERROR, msg ) );
								isValid = false;
							}

							// While beenden
							break;
						}
					}
				}
				reader.close();
				report.delete();
			} catch ( Exception e ) {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Erledigt: Fehler Auswertung

		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PNG )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}

	@Override
	public boolean onProgress( float percent )
	{
		// Muss auf return true sein, da ansonsten BadPeggy nicht funktioniert
		return true;
	}
}
