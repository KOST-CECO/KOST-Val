/* == KOST-Val ==================================================================================
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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

import ch.kostceco.tools.kosttools.fileservice.Recognition;
import ch.kostceco.tools.kosttools.fileservice.egovdv;
import ch.kostceco.tools.kosttools.util.Hash;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * kostval --> Controllervalfofile
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllervalfofile implements MessageConstants
{
	private boolean						min				= false;
	private String						hash			= "";
	private String						sizeWarningTxt	= "";

	private static TextResourceService	textResourceService;

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService(
			TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public String valFoFile( File valDatei, String logFileName,
			File directoryOfLogfile, boolean verbose, String dirOfJarPath,
			Map<String, String> configMap, ApplicationContext context,
			Locale locale, File logFile, int countToValidated )
			throws IOException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}
		// Formatvalidierung und Erkennung einer Datei

		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidation = configMap.get( "pdfaValidation" );
		String txtValidation = configMap.get( "txtValidation" );
		String pdfValidation = configMap.get( "pdfValidation" );
		String jp2Validation = configMap.get( "jp2Validation" );
		String jpegValidation = configMap.get( "jpegValidation" );
		String tiffValidation = configMap.get( "tiffValidation" );
		String pngValidation = configMap.get( "pngValidation" );
		String flacValidation = configMap.get( "flacValidation" );
		String waveValidation = configMap.get( "waveValidation" );
		String mp3Validation = configMap.get( "mp3Validation" );
		String mkvValidation = configMap.get( "mkvValidation" );
		String mp4Validation = configMap.get( "mp4Validation" );
		String xmlValidation = configMap.get( "xmlValidation" );
		String jsonValidation = configMap.get( "jsonValidation" );
		String siardValidation = configMap.get( "siardValidation" );
		String csvValidation = configMap.get( "csvValidation" );
		String xlsxValidation = configMap.get( "xlsxValidation" );
		String odsValidation = configMap.get( "odsValidation" );
		String otherformats = configMap.get( "otherformats" );

		String configHash = configMap.get( "hash" );
		String sizeWarning = configMap.get( "sizeWarning" );

		try {
			hash = "";
			if ( !valDatei.isDirectory() ) {
				String valDateiName = valDatei.getName();
				String valDateiExt = "." + FilenameUtils
						.getExtension( valDateiName ).toLowerCase();

				// Formaterkennung der Datei
				String recFormat = "new";
				recFormat = Recognition.formatRec( valDatei );
				String recMsg = "new";
				String intro = "new";

				// System.out.println(" - Datei: "+valDatei+" =
				// "+recFormat);

				// Hashwert der Datei berechnen welche gewuenscht sind
				// MD5, SHA-1, SHA-256, SHA-512

				if ( configHash.equals( "MD5" ) ) {
					hash = "<md5>" + Hash.getMd5( valDatei ) + "</md5>";
				} else if ( configHash.equals( "SHA-1" ) ) {
					hash = "<sha1>" + Hash.getSha1( valDatei ) + "</sha1>";
				} else if ( configHash.equals( "SHA-256" ) ) {
					hash = "<sha256>" + Hash.getSha256( valDatei )
							+ "</sha256>";
				} else if ( configHash.equals( "SHA-512" ) ) {
					hash = "<sha512>" + Hash.getSha512( valDatei )
							+ "</sha512>";
				} else {
					hash = "";
				}

				// Warnung bei kleinen Dateien ausgeben falls gewuenscht
				sizeWarning = configMap.get( "sizeWarning" );
				int sizeWarningInt = -1;
				if ( sizeWarning.equals( "0.5KB" ) ) {
					sizeWarningInt = 512;
				} else if ( sizeWarning.equals( "1KB" ) ) {
					sizeWarningInt = 1024;
				} else if ( sizeWarning.equals( "5KB" ) ) {
					sizeWarningInt = 5120;
				} else {
					sizeWarningInt = -1;
				}

				long length = 99999;
				sizeWarningTxt = "";
				if ( sizeWarningInt == -1 ) {
					// Keine Warnung ausgeben
				} else {
					length = valDatei.length();
					if ( sizeWarningInt > length ) {
						// System.out.println( "Warnung ausgeben" );
						sizeWarningTxt = getTextResourceService().getText(
								locale, MESSAGE_WARNING_XML_SIZE, length,
								sizeWarningInt );
					} else {
						// System.out.println( "keine Warnung ausgeben" );
					}
				}

				String valDateiXml = "<ValFile>" + valDatei.getAbsolutePath()
						+ "</ValFile>" + sizeWarningTxt;

				/*
				 * Ergebnis ist die Datei z.B. PDFA oder SIARD wenn einwandfrei
				 * erkannt ansonsten wird _ext angehängt, wenn die Dateiendung
				 * nicht stimmt z.B. JPEG_ext wenn eine .tiff-Datei als JPEG
				 * erkannt wird.
				 * 
				 * Wenn nichts erkannt wird UNKNOWN_DATEIENDUNG ausgegeben z.B.
				 * UNKNOWN_CDR
				 */

				if ( recFormat.contains( "UNKNOWN_" ) ) {
					// TODO Unbekanntes oder weiteres Format

					String formatUK = recFormat.replace( "UNKNOWN_", "" );

					if ( otherformats.contains( formatUK ) ) {
						// nur akzeptiert -> KEINE Validierung,
						// nur Erkennung
						Logtxt.logtxt( logFile, "<Validation>" + hash
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_AZTYPE, formatUK )
								+ valDateiXml );
						Logtxt.logtxt( logFile,
								"<Accepted>accepted</Accepted></Validation>" );
						// System.out.println( " = Accepted" );
						return "countValid"; // countValid = countValid + 1;
					} else {
						// NICHT akzeptiert -> invalid
						Logtxt.logtxt( logFile, "<Validation>" + hash
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_AZTYPE, "???" )
								+ valDateiXml );
						if ( !min ) {
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_AZ )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_NOTAZ_DROID ) );
						}
						Logtxt.logtxt( logFile,
								"<Notaccepted>not accepted</Notaccepted></Validation>" );
						// System.out.println( " = Not accepted" );
						return "countNotaz"; // countNotaz = countNotaz + 1;
					}
				} else if ( recFormat.contains( "_ext" ) ) {
					// TODO bekanntes Format mit falscher Dateiendung =>
					// invalid

					String formatEXT = recFormat.replace( "_ext", "" );

					Logtxt.logtxt( logFile,
							"<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, formatEXT )
									+ valDateiXml );
					// NICHT akzeptiert -> invalid
					if ( !min ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_AZ )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_NOTAZ, "" ) );
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_AZ )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_AZ_INCORRECTFILE,
										"`" + valDateiExt.toLowerCase() + "`",
										formatEXT ) );

					}
					Logtxt.logtxt( logFile,
							"<Notaccepted>not accepted</Notaccepted></Validation>" );
					// System.out.println( " = Not accepted" );
					return "countNotaz"; // countNotaz = countNotaz + 1;

				} else {
					// Format einwandfrei erkannt
					// TODO Text
					if ( recFormat.equals( "PDFA" ) ) {
						intro = countToValidated + " " + "PDFA:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( pdfaValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;

							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;

							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "PDF/A" )
									+ valDateiXml );

							/*
							 * Fuer das weitere Vorgehen ist es wichtig zu
							 * wissen ob Signaturen enthalten sind.
							 * 
							 * Entsprechend werden diese jetzt hier ermittelt:
							 * 
							 * ggf auch direkt validieren und dokumentieren
							 */
							String egovdvMsg = "";
							egovdvMsg = Controllervalfofile.valFoFileEgodv(
									valDatei, directoryOfLogfile, dirOfJarPath,
									configMap, locale );

							if ( egovdvMsg == "NoSignature" ) {
								// keine Signature
							} else {
								Logtxt.logtxt( logFile, egovdvMsg );
							}

							if ( pdfaValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "TXT" ) ) {
						intro = countToValidated + " " + "TXT:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( txtValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "TXT" )
									+ valDateiXml );
							if ( txtValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "PDF" ) ) {
						intro = countToValidated + " " + "PDF:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( pdfValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "PDF" )
									+ valDateiXml );

							/*
							 * Fuer das weitere Vorgehen ist es wichtig zu
							 * wissen ob Signaturen enthalten sind.
							 * 
							 * Entsprechend werden diese jetzt hier ermittelt:
							 * 
							 * ggf auch direkt validieren und dokumentieren
							 */
							String egovdvMsg = "";
							egovdvMsg = Controllervalfofile.valFoFileEgodv(
									valDatei, directoryOfLogfile, dirOfJarPath,
									configMap, locale );

							if ( egovdvMsg == "NoSignature" ) {
								// keine Signature
							} else {
								Logtxt.logtxt( logFile, egovdvMsg );
							}
							if ( pdfValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}

						// TODO Bilder
					} else if ( recFormat.equals( "JP2" ) ) {
						intro = countToValidated + " " + "JP2:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( jp2Validation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "JP2" )
									+ valDateiXml );
							if ( jp2Validation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "JPEG" ) ) {
						intro = countToValidated + " " + "JPEG:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( jpegValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "JPEG" )
									+ valDateiXml );
							if ( jpegValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "TIFF" ) ) {
						intro = countToValidated + " " + "TIFF:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( tiffValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "TIFF" )
									+ valDateiXml );
							if ( tiffValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "PNG" ) ) {
						intro = countToValidated + " " + "PNG:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( pngValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "PNG" )
									+ valDateiXml );
							if ( pngValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}

						// TODO Audio
					} else if ( recFormat.equals( "FLAC" ) ) {
						intro = countToValidated + "  " + "FLAC:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( flacValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "FLAC" )
									+ valDateiXml );
							if ( flacValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "WAVE" ) ) {
						intro = countToValidated + "  " + "WAVE:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( waveValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "WAVE" )
									+ valDateiXml );
							if ( waveValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "MP3" ) ) {
						intro = countToValidated + "  " + "MP3:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( mp3Validation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "MP3" )
									+ valDateiXml );
							if ( mp3Validation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}

						// TODO Video
					} else if ( recFormat.equals( "MKV" ) ) {
						intro = countToValidated + "  " + "MKV:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( mkvValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "MKV" )
									+ valDateiXml );
							if ( mkvValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "MP4" ) ) {
						intro = countToValidated + " " + "MP4:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( mp4Validation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "MP4" )
									+ valDateiXml );
							if ( mp4Validation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "XML" ) ) {
						intro = countToValidated + " " + "XML:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( xmlValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "XML" )
									+ valDateiXml );
							if ( xmlValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "JSON" ) ) {
						intro = countToValidated + " " + "JSON:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( jsonValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "JSON" )
									+ valDateiXml );
							if ( jsonValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "SIARD" ) ) {
						intro = countToValidated + " " + "SIARD: "
								+ valDatei.getAbsolutePath() + " ";
						if ( siardValidation.equals( "yes" ) ) {
							System.out.print( intro );
							// akzeptiert und soll validiert werden
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei,
									logFileName, directoryOfLogfile, verbose,
									dirOfJarPath, configMap, context, locale,
									logFile, hash, valDateiXml );
							if ( valFile ) {
								return "countValid"; // countValid = countValid
														// + 1;
							} else {
								return "countInvalid"; // countInvalid =
														// countInvalid + 1;
							}
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "SIARD" )
									+ valDateiXml );
							if ( siardValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "CSV" ) ) {
						intro = countToValidated + " " + "CSV:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( csvValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "CSV" )
									+ valDateiXml );
							if ( csvValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "XLSX" ) ) {
						intro = countToValidated + " " + "XLSX:  "
								+ valDatei.getAbsolutePath() + " ";
						if ( xlsxValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "XLSX" )
									+ valDateiXml );
							if ( xlsxValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else if ( recFormat.equals( "ODS" ) ) {
						intro = countToValidated + " " + "ODS:   "
								+ valDatei.getAbsolutePath() + " ";
						if ( odsValidation.equals( "yes" ) ) {
							// akzeptiert und soll validiert werden
							// Aktuell nicht moeglich, kein Validator dafuer
						} else {
							// akzeptiert oder nicht
							Logtxt.logtxt( logFile, "<Validation>" + hash
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AZTYPE, "ODS" )
									+ valDateiXml );
							if ( odsValidation.equals( "az" ) ) {
								// nur akzeptiert -> KEINE Validierung, nur
								// Erkennung
								recMsg = "AZ";
							} else {
								// NICHT akzeptiert -> invalid
								recMsg = "notAZ";
							}
						}
					} else {
						// TODO Kontrolle weitere Formate

						intro = countToValidated + " " + recFormat + ":  "
								+ valDatei.getAbsolutePath() + " ";

						Logtxt.logtxt( logFile, "<Validation>" + hash
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_AZTYPE, recFormat )
								+ valDateiXml );
						if ( otherformats.contains( recFormat ) ) {
							// nur akzeptiert -> KEINE Validierung, nur
							// Erkennung
							recMsg = "AZ";
						} else {
							// NICHT akzeptiert -> invalid
							recMsg = "notAZ";
						}
					}
				}

				// Meldungen ausgeben bei az und notaz
				if ( recMsg.equals( "AZ" ) ) {
					Logtxt.logtxt( logFile,
							"<Accepted>accepted</Accepted></Validation>" );
					/*
					 * System.out.println( intro+" = Accepted" ); Keine
					 * Konsolenausgabe, da es ansonsten staut
					 */
					recMsg = "new";
					intro = "new";
					return "countValid"; // countValid = countValid + 1;
				} else if ( recMsg.equals( "notAZ" ) ) {
					if ( !min ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_AZ )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_NOTAZ, " " + recFormat ) );
					}
					Logtxt.logtxt( logFile,
							"<Notaccepted>not accepted</Notaccepted></Validation>" );
					/*
					 * System.out.println( intro + " = Not accepted" );Keine
					 * Konsolenausgabe, da es ansonsten staut
					 */
					recMsg = "new";
					intro = "new";
					return "countNotaz"; // countNotaz = countNotaz + 1;
				}

				// Nach allen Dateien: Loeschen des Arbeitsverzeichnisses,
				// falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
			} else {
				// Ordner. Count aktualisieren
				return "countProgress"; // countProgress = countProgress + 1;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					"<Error>"
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN,
									"Formatvalidation: " + e.getMessage() )
							+ "</Format></KOSTValLog>" );
			System.out.println( "Exception: " + e.getMessage() );
		} catch ( StackOverflowError eso ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_STACKOVERFLOWMAIN ) );
			System.out.println( "Exception: " + "StackOverflowError" );
		} catch ( OutOfMemoryError eoom ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_OUTOFMEMORYMAIN ) );
			System.out.println( "Exception: " + "OutOfMemoryError" );
		}

		return "END";

	}

	public static String valFoFileEgodv( File valDatei, File directoryOfLogfile,
			String dirOfJarPath, Map<String, String> configMap, Locale locale )
			throws IOException
	{

		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		// Informationen holen, betreffend der Signaturvalidierung
		String dvvalidation = configMap.get( "dvvalidation" );
		String Mixed = configMap.get( "Mixed" );
		String Qualified = configMap.get( "Qualified" );
		String SwissGovPKI = configMap.get( "SwissGovPKI" );
		String Upregfn = configMap.get( "Upregfn" );
		String Siegel = configMap.get( "Siegel" );
		String Amtsblattportal = configMap.get( "Amtsblattportal" );
		String Edec = configMap.get( "Edec" );
		String ESchKG = configMap.get( "ESchKG" );
		String FederalLaw = configMap.get( "FederalLaw" );
		String Strafregisterauszug = configMap.get( "Strafregisterauszug" );
		String KantonZugFinanzdirektion = configMap
				.get( "KantonZugFinanzdirektion" );

		/*
		 * System.out.println( Mixed + Qualified + SwissGovPKI + Upregfn +
		 * Siegel + Amtsblattportal + Edec + ESchKG + FederalLaw +
		 * Strafregisterauszug + KantonZugFinanzdirektion );
		 */

		/*
		 * Fuer das weitere Vorgehen ist es wichtig zu wissen ob Signaturen
		 * enthalten sind.
		 * 
		 * Wenn gewuenscht und moeglich werden diese validiert und dokumentiert
		 * 
		 * Entsprechend werden diese jetzt hier ermittelt:
		 */

		// Ermittlung ob Signaturen enthalten sind
		File workDir2 = new File( pathToWorkDir );

		String returnEgovdvSum = "Error_egovdv";
		try {
			Integer countSig = egovdv.execEgovdvCountSig( valDatei, workDir2,
					dirOfJarPath );
			/*
			 * Gibt mit egovdv via cmd die Anzahl Signaturen in pdf aus
			 * 
			 * 0 = keine Signatur
			 * 
			 * 999 = Fehler: Es existiert nicht alles zu egovdv
			 * 
			 * 998 = Fehler: Exception oder Report existiert nicht
			 * 
			 * 997 = Fehler: Die ersten beiden Zeilen zu egovdv fehlen
			 * 
			 * 996 = Fehler: Exception UNKNOWN Catch
			 * 
			 * @return Integer mit der Anzahl Signaturen
			 */
			if ( countSig == 999 ) {
				// 999 = Fehler: Es existiert nicht alles zu
				// egovdv
				returnEgovdvSum = getTextResourceService().getText( locale,
						MESSAGE_XML_MODUL_A_PDFA )
						+ getTextResourceService().getText( locale,
								MESSAGE_XML_MISSING_FILE, "checkTool" );
			} else if ( countSig == 998 ) {
				// 998 = Fehler: Exception oder Report
				// existiert nicht
				returnEgovdvSum = getTextResourceService().getText( locale,
						MESSAGE_XML_MODUL_A_PDFA )
						+ getTextResourceService().getText( locale,
								MESSAGE_XML_SERVICEINVALID, "egovdv", "" );
			} else if ( countSig == 997 ) {
				// die ersten beiden Zeilen fehlen
				returnEgovdvSum = getTextResourceService().getText( locale,
						MESSAGE_XML_MODUL_A_PDFA )
						+ getTextResourceService().getText( locale,
								ERROR_XML_SERVICEFAILED, "egovdv",
								"missing lines" );
			} else if ( countSig == 996 ) {
				returnEgovdvSum = getTextResourceService().getText( locale,
						MESSAGE_XML_MODUL_A_PDFA )
						+ getTextResourceService().getText( locale,
								ERROR_XML_UNKNOWN, "egovdv: catch-Error" );
			} else if ( countSig == 0 ) {
				// keine Signature
				returnEgovdvSum = "NoSignature";
			} else {
				if ( dvvalidation.equals( "yes" ) ) {
					// Signaturen validieren (Mixed)
					File outMixedSig = new File(
							directoryOfLogfile.getAbsolutePath()
									+ File.separator + valDatei.getName()
									+ "_dvReport_Mixed.pdf" );

					String mixedSig = egovdv.execEgovdvCheck( valDatei,
							outMixedSig, workDir2, dirOfJarPath, "Mixed" );

					if ( mixedSig.contains( "noLicense" ) ) {
						// Warnung mit Anzahl Signaturen ausgeben
						returnEgovdvSum = getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( locale,
										WARNING_XML_A_SIGNATURE, countSig,
										"</Message><Message></Message><Message>"
												+ getTextResourceService()
														.getText( locale,
																ERROR_XML_A_EGOVDV_LICENSE ) );
					} else if ( mixedSig.contains( "_NoReport_" ) ) {
						// Warnung mit Anzahl Signaturen
						// und Egovdv-NoReport-Fehler ausgeben
						returnEgovdvSum = getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
								+ getTextResourceService().getText( locale,
										WARNING_XML_A_SIGNATURE_SUM1, countSig,
										"</Message><Message></Message><Message>"
												+ getTextResourceService()
														.getText( locale,
																ERROR_XML_A_EGOVDV_NOREPORT )
												+ "</Message><Message></Message><Message>("
												+ mixedSig + ") " );
					} else {

						// Analyse des Mixed-Ergebnisses

						if ( mixedSig.contains( "Validity-VALID_" ) ) {
							// Mixed-Signatur-Validierung bestanden
							// Mindestens ein weiterer Mandant ist valid
							/*
							 * TODO Testen welche es sind und behalten jener
							 * Mandanten, welche eingeschalten und bestanden
							 * sind
							 */

							String strAnalysePdf = egovdv
									.analyseEgovdvPdf( outMixedSig );
							
							// System.out.println(strAnalysePdf);

							String oldMsg = "<Message>Das Dokument ist gültig signiert.";
							String oldMsg2 = "<Message>Das Dokument ist gültig signiert. ";
							String newMsg = "<Message>Das Dokument ist gültig signiert (Mandant: Mixed).";
							strAnalysePdf = strAnalysePdf.replace( oldMsg,
									newMsg );
							strAnalysePdf = strAnalysePdf.replace( oldMsg2,
									newMsg );

							// Signaturen validieren (Qualified)
							if ( Qualified != "no" ) {
								File outQualifiedSig = new File(
										directoryOfLogfile.getAbsolutePath()
												+ File.separator
												+ valDatei.getName()
												+ "_dvReport_Qualified.pdf" );
								String QualifiedSig = egovdv.execEgovdvCheck(
										valDatei, outQualifiedSig, workDir2,
										dirOfJarPath, Qualified );
								if ( QualifiedSig
										.contains( "Validity-VALID_" ) ) {
									// pdf behalten
									oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
									newMsg = "Das Dokument ist gültig signiert (Mandant: "
											+ Qualified + ", ";
									strAnalysePdf = strAnalysePdf
											.replace( oldMsg, newMsg );
								} else {
									// PDF-Report loeschen, da er nicht
									// bestanden hat
									Util.deleteFile( outQualifiedSig );
								}
							}
							// Signaturen validieren (SwissGovPKI)
							if ( SwissGovPKI != "no" ) {
								File outSwissGovPKISig = new File(
										directoryOfLogfile.getAbsolutePath()
												+ File.separator
												+ valDatei.getName()
												+ "_dvReport_SwissGovPKI.pdf" );
								String SwissGovPKISig = egovdv.execEgovdvCheck(
										valDatei, outSwissGovPKISig, workDir2,
										dirOfJarPath, SwissGovPKI );
								if ( SwissGovPKISig
										.contains( "Validity-VALID_" ) ) {
									// pdf behalten
									oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
									newMsg = "Das Dokument ist gültig signiert (Mandant: "
											+ SwissGovPKI + ", ";
									strAnalysePdf = strAnalysePdf
											.replace( oldMsg, newMsg );
								} else {
									// PDF-Report loeschen, da er nicht
									// bestanden hat
									Util.deleteFile( outSwissGovPKISig );
								}
							}
							// Signaturen validieren (Upregfn)
							if ( Upregfn != "no" ) {
								File outUpregfnSig = new File(
										directoryOfLogfile.getAbsolutePath()
												+ File.separator
												+ valDatei.getName()
												+ "_dvReport_Upregfn.pdf" );
								String UpregfnSig = egovdv.execEgovdvCheck(
										valDatei, outUpregfnSig, workDir2,
										dirOfJarPath, Upregfn );
								if ( UpregfnSig
										.contains( "Validity-VALID_" ) ) {
									// pdf behalten
									oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
									newMsg = "Das Dokument ist gültig signiert (Mandant: "
											+ Upregfn + ", ";
									strAnalysePdf = strAnalysePdf
											.replace( oldMsg, newMsg );
								} else {
									// PDF-Report loeschen, da er nicht
									// bestanden hat
									Util.deleteFile( outUpregfnSig );
								}
							}
							// Signaturen validieren (KantonZugFinanzdirektion)
							if ( KantonZugFinanzdirektion != "no" ) {
								File outKantonZugFinanzdirektionSig = new File(
										directoryOfLogfile.getAbsolutePath()
												+ File.separator
												+ valDatei.getName()
												+ "_dvReport_KantonZugFinanzdirektion.pdf" );
								String KantonZugFinanzdirektionSig = egovdv
										.execEgovdvCheck( valDatei,
												outKantonZugFinanzdirektionSig,
												workDir2, dirOfJarPath,
												KantonZugFinanzdirektion );
								if ( KantonZugFinanzdirektionSig
										.contains( "Validity-VALID_" ) ) {
									// pdf behalten
									oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
									newMsg = "Das Dokument ist gültig signiert (Mandant: "
											+ KantonZugFinanzdirektion + ", ";
									strAnalysePdf = strAnalysePdf
											.replace( oldMsg, newMsg );
								} else {
									// PDF-Report loeschen, da er nicht
									// bestanden hat
									Util.deleteFile(
											outKantonZugFinanzdirektionSig );
								}
							}
							// Signaturen validieren (Siegel)
							File outSiegelSig = new File(
									directoryOfLogfile.getAbsolutePath()
											+ File.separator
											+ valDatei.getName()
											+ "_dvReport_Siegel.pdf" );
							String SiegelSig = egovdv.execEgovdvCheck( valDatei,
									outSiegelSig, workDir2, dirOfJarPath,
									Siegel );
							if ( SiegelSig.contains( "Validity-VALID_" ) ) {
								if ( Siegel != "no" ) {
									// pdf behalten
									oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
									newMsg = "Das Dokument ist gültig signiert (Mandant: "
											+ Siegel + ", ";
									strAnalysePdf = strAnalysePdf
											.replace( oldMsg, newMsg );
								} else {
									// PDF-Report loeschen, da er nicht
									// bestanden wurde
									Util.deleteFile( outSiegelSig );
								}
								// Kontrolle der anderen Siegel
								// Signaturen validieren (Amtsblattportal)
								if ( Amtsblattportal != "no" ) {
									File outAmtsblattportalSig = new File(
											directoryOfLogfile.getAbsolutePath()
													+ File.separator
													+ valDatei.getName()
													+ "_dvReport_Amtsblattportal.pdf" );
									String AmtsblattportalSig = egovdv
											.execEgovdvCheck( valDatei,
													outAmtsblattportalSig,
													workDir2, dirOfJarPath,
													Amtsblattportal );
									if ( AmtsblattportalSig
											.contains( "Validity-VALID_" ) ) {
										// pdf behalten
										oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
										newMsg = "Das Dokument ist gültig signiert (Mandant: "
												+ Amtsblattportal + ", ";
										strAnalysePdf = strAnalysePdf
												.replace( oldMsg, newMsg );
									} else {
										// PDF-Report loeschen, da er nicht
										// bestanden hat
										Util.deleteFile(
												outAmtsblattportalSig );
									}
								}
								// Signaturen validieren (Edec)
								if ( Edec != "no" ) {
									File outEdecSig = new File(
											directoryOfLogfile.getAbsolutePath()
													+ File.separator
													+ valDatei.getName()
													+ "_dvReport_Edec.pdf" );
									String EdecSig = egovdv.execEgovdvCheck(
											valDatei, outEdecSig, workDir2,
											dirOfJarPath, Edec );
									if ( EdecSig
											.contains( "Validity-VALID_" ) ) {
										// pdf behalten
										oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
										newMsg = "Das Dokument ist gültig signiert (Mandant: "
												+ Edec + ", ";
										strAnalysePdf = strAnalysePdf
												.replace( oldMsg, newMsg );
									} else {
										// PDF-Report loeschen, da er nicht
										// bestanden hat
										Util.deleteFile( outEdecSig );
									}
								}
								// Signaturen validieren (ESchKG)
								if ( ESchKG != "no" ) {
									File outESchKGSig = new File(
											directoryOfLogfile.getAbsolutePath()
													+ File.separator
													+ valDatei.getName()
													+ "_dvReport_ESchKG.pdf" );
									String ESchKGSig = egovdv.execEgovdvCheck(
											valDatei, outESchKGSig, workDir2,
											dirOfJarPath, ESchKG );
									if ( ESchKGSig
											.contains( "Validity-VALID_" ) ) {
										// pdf behalten
										oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
										newMsg = "Das Dokument ist gültig signiert (Mandant: "
												+ ESchKG + ", ";
										strAnalysePdf = strAnalysePdf
												.replace( oldMsg, newMsg );
									} else {
										// PDF-Report loeschen, da er nicht
										// bestanden hat
										Util.deleteFile( outESchKGSig );
									}
								}
								// Signaturen validieren (FederalLaw)
								if ( FederalLaw != "no" ) {
									File outFederalLawSig = new File(
											directoryOfLogfile.getAbsolutePath()
													+ File.separator
													+ valDatei.getName()
													+ "_dvReport_FederalLaw.pdf" );
									String FederalLawSig = egovdv
											.execEgovdvCheck( valDatei,
													outFederalLawSig, workDir2,
													dirOfJarPath, FederalLaw );
									if ( FederalLawSig
											.contains( "Validity-VALID_" ) ) {
										// pdf behalten
										oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
										newMsg = "Das Dokument ist gültig signiert (Mandant: "
												+ FederalLaw + ", ";
										strAnalysePdf = strAnalysePdf
												.replace( oldMsg, newMsg );
									} else {
										// PDF-Report loeschen, da er nicht
										// bestanden hat
										Util.deleteFile( outFederalLawSig );
									}
								}
								// Signaturen validieren (Strafregisterauszug)
								if ( Strafregisterauszug != "no" ) {
									File outStrafregisterauszugSig = new File(
											directoryOfLogfile.getAbsolutePath()
													+ File.separator
													+ valDatei.getName()
													+ "_dvReport_Strafregisterauszug.pdf" );
									String StrafregisterauszugSig = egovdv
											.execEgovdvCheck( valDatei,
													outStrafregisterauszugSig,
													workDir2, dirOfJarPath,
													Strafregisterauszug );
									if ( StrafregisterauszugSig
											.contains( "Validity-VALID_" ) ) {
										// pdf behalten
										oldMsg = "Das Dokument ist gültig signiert (Mandant: ";
										newMsg = "Das Dokument ist gültig signiert (Mandant: "
												+ Strafregisterauszug + ", ";
										strAnalysePdf = strAnalysePdf
												.replace( oldMsg, newMsg );
									} else {
										// PDF-Report loeschen, da er nicht
										// bestanden hat
										Util.deleteFile(
												outStrafregisterauszugSig );
									}
								}
							} else {
								// PDF-Report loeschen, da er nicht bestanden
								// wurde
								Util.deleteFile( outSiegelSig );
							}

							// Warnung mit Anzahl Signaturen und Ergebnis
							// ausgeben
							returnEgovdvSum = getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											WARNING_XML_A_SIGNATURE_SUM1,
											countSig,
											strAnalysePdf
													+ "</Message><Message></Message><Message>("
													+ mixedSig + ") " );

						} else {
							// Mixed-Signatur-Validierung NICHT bestanden
							// Kein Mandant ist valid
							// Dokumentation des Ergebnisses

							String strAnalysePdf = egovdv
									.analyseEgovdvPdf( outMixedSig );

							// Warnung mit Anzahl Signaturen und Ergebnis
							// ausgeben
							returnEgovdvSum = getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( locale,
											WARNING_XML_A_SIGNATURE_SUM1,
											countSig,
											strAnalysePdf
													+ "</Message><Message></Message><Message>("
													+ mixedSig + ") " );

							// PDF-Report loeschen, da er nicht bestanden wurde
							Util.deleteFile( outMixedSig );
						}
					}
					if ( Mixed == "no" ) {
						// PDF-Report loeschen, da er nicht gewuenscht wird
						Util.deleteFile( outMixedSig );
					}
				} else {
					// Warnung mit Anzahl Signaturen ausgeben (keine
					// Validierung)
					returnEgovdvSum = getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( locale,
									WARNING_XML_A_SIGNATURE, countSig );
				}
			}
		} catch ( Exception e ) {
			returnEgovdvSum = getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( locale,
							ERROR_XML_UNKNOWN, "egovdv: " + e.getMessage() );
		}
		// Ende Ermittlung ob Signaturen enthalten sind und ggf validierung
		return returnEgovdvSum;
	}
}