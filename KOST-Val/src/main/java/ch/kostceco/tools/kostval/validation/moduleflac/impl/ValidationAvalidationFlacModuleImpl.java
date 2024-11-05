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

package ch.kostceco.tools.kostval.validation.moduleflac.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import ch.kostceco.tools.kosttools.fileservice.ffmpeg;
import ch.kostceco.tools.kosttools.fileservice.flac;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.moduleflac.ValidationAflacvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduleflac.ValidationAvalidationFlacModule;

/**
 * a) FLAC-Erkennung mit ffprobe inkl Codecs
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationFlacModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationFlacModule {

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationAflacvalidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}
		String pathToWorkDir = configMap.get("PathToWorkDir");
		File workDir = new File(pathToWorkDir);
		if (!workDir.exists()) {
			workDir.mkdir();
		}
		File outputProbe = new File(pathToWorkDir + File.separator + "ffprobe.txt");
		File outputFfmpeg = new File(pathToWorkDir + File.separator + "ffmpeg.txt");
		File outputFlac = new File(pathToWorkDir + File.separator + "flac.txt");
		// falls das File von einem vorhergehenden Durchlauf bereits
		// existiert, loeschen wir es
		if (outputProbe.exists()) {
			outputProbe.delete();
		}
		if (outputFfmpeg.exists()) {
			outputFfmpeg.delete();
		}
		if (outputFlac.exists()) {
			outputFlac.delete();
		}

		// Die Container-Erkennung erfolgt bereits im Vorfeld

		boolean isValid = true;
		boolean isValidB = true;
		boolean isValidC = true;

		/*
		 * Doppelleerschlag im Pfad oder im Namen einer Datei bereitet Probleme (leerer
		 * Report) Video-Datei wird bei Doppelleerschlag in temp-Verzeichnis kopiert
		 */
		String valDateiPath = valDatei.getAbsolutePath();
		String valDateiName = valDatei.getName().replace("  ", " ");
		valDateiName = valDateiName.replace("  ", " ");
		valDateiName = valDateiName.replace("  ", " ");

		File valDateiTemp = new File(pathToWorkDir + File.separator + valDateiName);
		if (valDateiPath.contains("  ")) {
			try {
				Util.copyFile(valDatei, valDateiTemp);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			valDateiTemp = valDatei;
		}

		// TODO: inArbeit: codec-Erkennung mit ffprobe

		// - Initialisierung ffprobe -> existiert alles zu ffmpeg?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = ffmpeg.checkFfmpegPlayProbe(dirOfJarPath);
		if (!checkTool.equals("OK")) {
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
								+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService().getText(locale, ABORTED)));
				isValid = false;
			}
		} else {
			// ffmpeg, ffplay und ffprobe sollte vorhanden sein
			try {
				String resultExec = ffmpeg.execFfprobe(valDateiTemp, outputProbe, workDir, dirOfJarPath);
				if (!resultExec.equals("OK") || !outputProbe.exists()) {
					// Exception oder Report existiert nicht
					if (min) {
						return false;
					} else {
						isValid = false;
						// Erster Fehler! Meldung A ausgeben und invalid setzten
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
								+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEINVALID, "ffprobe", ""));
					}
				} else {
					// Report existiert -> Auswerten...
					String flac = "format_name=flac";
					String flacCodec = "codec_name=flac";
					String formatCodec = "";
					Scanner scannerFormat = new Scanner(outputProbe);
					int countFormat = 0;
					int countAudioCodec = 0;
					while (scannerFormat.hasNextLine()) {
						// format_name=flac
						String line = scannerFormat.nextLine();
						if (line.contains("format_name=")) {
							// container format auswerten
							countFormat = countFormat + 1;
							String format = line.replace("format_name=", "");
							String formatName = format.toUpperCase();
							if (line.contains(flac)) {
								// OK
								formatCodec = " container=FLAC  ";
							} else {
								// NOK
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
												+ getTextResourceService().getText(locale,
														ERROR_XML_A_AUDIOVIDEO_FORMAT_NAZ, formatName));
								isValid = false;
							}
						}
					}
					scannerFormat.close();
					// codec auswertung

					/*
					 * Nicht vergessen in
					 * "src/main/resources/config/applicationContext-services.xml" beim
					 * entsprechenden Modul die property anzugeben: <property
					 * name="configurationService" ref="configurationService" />
					 */

					Scanner scanner = new Scanner(outputProbe);
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains("codec_name=")) {
							// Codec auswerten

							String codec = line.replace("codec_name=", "");
							String codecName = codec.toUpperCase();
							String type = "audiocodec";
							if (line.contains(flacCodec)) {
								// OK
								formatCodec = formatCodec + type + "=FLAC  ";
								countAudioCodec = countAudioCodec + 1;
							} else {
								// NOK
								countAudioCodec = countAudioCodec + 1;
								formatCodec = formatCodec + type + "=" + codecName + "  ";
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
												+ getTextResourceService().getText(locale,
														ERROR_XML_A_AUDIOVIDEO_CODEC_NAZ, codecName, type));
								isValid = false;
							}
						}
					}
					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
									+ getTextResourceService().getText(locale, ERROR_XML_A_AUDIOVIDEO_FORMATCODEC,
											formatCodec));

					if (countFormat == 0) {
						// NOK
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
										+ getTextResourceService().getText(locale, ERROR_XML_A_AUDIOVIDEO_CODEC_NO,
												"format", "FLAC"));
						isValid = false;
					}
					if (countAudioCodec == 0) {
						// NOK
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
										+ getTextResourceService().getText(locale,
												ERROR_XML_A_AUDIOVIDEO_CODEC_NOAUDIO_ERROR, "FLAC"));
						isValid = false;
					}

					scanner.close();
				}
				// TODO: Erledigt: Codec Auswertung

				if (isValid) {
					// Start: Analyse ffmpeg
					String errB1 = "";
					String errB2 = "";
					String errB3 = "";
					String errB4 = "";
					String errB5 = "";
					String errB6 = "";
					String errB7 = "";
					String errB8 = "";
					String errB9 = "";
					String errB10 = "";
					if (outputFfmpeg.exists()) {
						outputFfmpeg.delete();
					}

					String resultFfmpegExec = ffmpeg.execFfmpeg(valDateiTemp, outputFfmpeg, workDir, dirOfJarPath);
					if (!resultFfmpegExec.equals("OK") || !outputFfmpeg.exists()) {
						// Exception oder Report existiert nicht
						if (min) {
							return false;
						} else {
							isValid = false;
							// Erster Fehler! Meldung B ausgeben und invalid
							// setzten
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_FLAC)
											+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEINVALID,
													"ffmpeg", ""));
						}
					} else {
						// Report existiert -> Auswerten...
						String errorFilePathName = "Error opening output file " + valDateiTemp.getAbsolutePath();
						Scanner scannerFormat = new Scanner(outputFfmpeg);
						while (scannerFormat.hasNextLine()) {
							String line = "";
							line = scannerFormat.nextLine();
							// [out#0/mp4 @ 0000023c307ec780] Output file does
							// not contain any stream
							// Error opening output file
							// C:\Users\clair\Documents\PPEG_Video\div_Murmeli_Container_Codecs\MP4_AVC_MP3_az_Murmeli_2018.mp4.
							// Error opening output files: Invalid argument
							if (line.startsWith("[") || line.startsWith("Error ")) {
								if (line.contains(errorFilePathName)) {
									// Meldung ignorieren, kein Mehrwert
								} else {
									// NOK
									isValidB = false;
									if (errB1 == "") {
										errB1 = "<Message> - " + line + "</Message>";
									} else if (errB2 == "") {
										errB2 = "<Message> - " + line + "</Message>";
									} else if (errB3 == "") {
										errB3 = "<Message> - " + line + "</Message>";
									} else if (errB4 == "") {
										errB4 = "<Message> - " + line + "</Message>";
									} else if (errB5 == "") {
										errB5 = "<Message> - " + line + "</Message>";
									} else if (errB6 == "") {
										errB6 = "<Message> - " + line + "</Message>";
									} else if (errB7 == "") {
										errB7 = "<Message> - " + line + "</Message>";
									} else if (errB8 == "") {
										errB8 = "<Message> - " + line + "</Message>";
									} else if (errB9 == "") {
										errB9 = "<Message> - " + line + "</Message>";
									} else if (errB10 == "") {
										errB10 = "<Message> - " + line + "</Message>";
									} else {
										errB10 = errB10.replace("</Message>", " ... </Message>");
									}
								}
							}
						}
						scannerFormat.close();
					}
					if (!isValidB) {
						// Fehlermeldungen ausgeben
						isValid = false;
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_FLAC)
								+ getTextResourceService().getText(locale, ERROR_XML_B_AUDIOVIDEO_ERROR, errB1 + errB2
										+ errB3 + errB4 + errB5 + errB6 + errB7 + errB8 + errB9 + errB10));
					}
				}
				// TODO: Erledigt: Analyse ffmpeg
			} catch (Exception e) {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_FLAC)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
				return false;
			}
		}

		// TODO: Start: Test mit flac

		// - Initialisierung flac -> existiert alles zu flac?

		// Pfad zum Programm existiert die Dateien?
		String checkToolFlac = flac.checkFlac(dirOfJarPath);
		if (!checkToolFlac.equals("OK")) {
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_FLAC)
								+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkToolFlac,
										getTextResourceService().getText(locale, ABORTED)));
				return false;
			}
		} else {
			// flac sollte vorhanden sein
			try {
				String resultExec = flac.execFlac(valDateiTemp, outputFlac, workDir, dirOfJarPath);

				if (!resultExec.equals("OK") || !outputFlac.exists()) {
					// Exception oder Report existiert nicht
					if (min) {
						return false;
					} else {
						isValidC = false;
						// Erster Fehler! Meldung B ausgeben und invalid setzten
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_FLAC)
								+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEINVALID, "flac", ""));
					}
				} else {
					// Report existiert -> Auswerten...

					/*
					 * FLAC_valid_Murmeli_2018 - Kopie.flac: *** Got error code
					 * 4:FLAC__STREAM_DECODER_ERROR_STATUS_BAD_METADATA
					 * 
					 * 
					 * FLAC_valid_Murmeli_2018 - Kopie.flac: ERROR while decoding metadata state =
					 * FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC
					 * 
					 * FLAC_valid_Murmeli_2018.flac: ok
					 * 
					 * FLAC_az_KOST.flac: WARNING, cannot check MD5 signature since it was unset in
					 * the STREAMINFO ok
					 * 
					 * MP3_az_KOST.mp3: *** Got error code
					 * 0:FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC
					 * 
					 * The input file is either not a FLAC file or is corrupted. If you are
					 * convinced it is a FLAC file, you can rerun the same command and add the -F
					 * parameter to try and recover as much as possible from the file.
					 */

					// zu ignorierende Info-Zeilen startend mit:
					// flac 1.4.3 Copyright (C) ...
					// flac comes ...
					// welcome to redistribute it ...

					String lineEmpty = "";
					String line2 = "flac 1";
					String line3 = "Copyright (";
					String line4 = "flac comes";
					String line5 = "welcome to";

					String ok1 = valDateiTemp.getName() + ": ok";
					String ok2 = "ok";
					String error1 = valDateiTemp.getName() + ": ERROR";
					String error2 = "Got error code";
					String error3 = "FLAC__";
					String warning = "WARNING, ";
					Boolean warningFlac = false;
					String logError1 = "";
					String logError2 = "";
					String logError3 = "";
					String logError4 = "";
					String logError5 = "";
					String logWarning1 = "";
					String logWarning2 = "";
					String logWarning3 = "";
					String logWarning4 = "";
					String logWarning5 = "";
					Scanner scannerOutputFlac = new Scanner(outputFlac);
					while (scannerOutputFlac.hasNextLine()) {
						String line = scannerOutputFlac.nextLine();
						if (line.equals(lineEmpty)) {
							// Zeile ignorieren
						} else if (line.startsWith(line2)) {
							// Info-Zeile ignorieren
						} else if (line.startsWith(line3)) {
							// Info-Zeile ignorieren
						} else if (line.startsWith(line4)) {
							// Info-Zeile ignorieren
						} else if (line.startsWith(line5)) {
							// Info-Zeile ignorieren
						} else {
							if (line.contains(ok1)) {
								// Validierung mit mkvalidator bestanden
								// isValidC= true;
							} else {
								String lineShort = line.replace(valDateiTemp.getName() + ": ", "");
								lineShort = lineShort.replace("*** ", "");
								lineShort = lineShort.trim();
								if (line.startsWith(ok2) && warningFlac) {
									// Valid und warnung wurde bereits ausgeben

								} else if (line.contains(warning)) {
									warningFlac = true;
									String lineShort2 = lineShort.substring(lineShort.indexOf("WARNING"),
											lineShort.length());
									lineShort = lineShort2;
									// die ersten 5 Warnungen speichern wenn neu
									if (logWarning1 == "") {
										logWarning1 = lineShort;
									} else if (logWarning2 == "") {
										if (logWarning1 == lineShort) {
											// alter Fehler
										} else {
											logWarning2 = "</Message><Message> - " + lineShort;
										}
									} else if (logWarning3 == "") {
										if (logWarning1 == lineShort || logWarning2 == lineShort) {
											// alter Fehler
										} else {
											logWarning3 = "</Message><Message> - " + lineShort;
										}
									} else if (logWarning4 == "") {
										if (logWarning1 == lineShort || logWarning2 == lineShort
												|| logWarning3 == lineShort) {
											// alter Fehler
										} else {
											logWarning4 = "</Message><Message> - " + lineShort;
										}
									} else if (logWarning5 == "") {
										if (logWarning1 == lineShort || logWarning2 == lineShort
												|| logWarning3 == lineShort || logWarning4 == lineShort) {
											// alter Fehler
										} else {
											logWarning5 = "</Message><Message> - " + lineShort;
										}

									} else if (logWarning3 == ""
											&& (logWarning1 != lineShort || logWarning2 != lineShort)) {
										logWarning3 = "</Message><Message> - " + lineShort;
									} else if (logWarning4 == "" && (logWarning1 != lineShort
											|| logWarning2 != lineShort || logWarning3 != lineShort)) {
										logWarning4 = "</Message><Message> - " + lineShort;
									} else if (logWarning5 == ""
											&& (logWarning1 != lineShort || logWarning2 != lineShort
													|| logWarning3 != lineShort || logWarning4 != lineShort)) {
										logWarning5 = "</Message><Message> - " + lineShort;
									}
								} else if (line.startsWith(error1) || line.contains(error2) || line.contains(error3)) {
									// NOK
									isValidC = false;
									// die ersten 5 Fehleren speichern wenn neu
									if (logError1 == "") {
										logError1 = lineShort;
									} else if (logError2 == "") {
										if (logError1 == lineShort) {
											// alter Fehler
										} else {
											logError2 = "</Message><Message> - " + lineShort;
										}
									} else if (logError3 == "") {
										if (logError1 == lineShort || logError2 == lineShort) {
											// alter Fehler
										} else {
											logError3 = "</Message><Message> - " + lineShort;
										}
									} else if (logError4 == "") {
										if (logError1 == lineShort || logError2 == lineShort
												|| logError3 == lineShort) {
											// alter Fehler
										} else {
											logError4 = "</Message><Message> - " + lineShort;
										}
									} else if (logError5 == "") {
										if (logError1 == lineShort || logError2 == lineShort || logError3 == lineShort
												|| logError4 == lineShort) {
											// alter Fehler
										} else {
											logError5 = "</Message><Message> - " + lineShort;
										}

									} else if (logError3 == "" && (logError1 != lineShort || logError2 != lineShort)) {
										logError3 = "</Message><Message> - " + lineShort;
									} else if (logError4 == "" && (logError1 != lineShort || logError2 != lineShort
											|| logError3 != lineShort)) {
										logError4 = "</Message><Message> - " + lineShort;
									} else if (logError5 == "" && (logError1 != lineShort || logError2 != lineShort
											|| logError3 != lineShort || logError4 != lineShort)) {
										logError5 = "</Message><Message> - " + lineShort;
									}
								} else {
									// Infos nicht ausgeben
								}
							}
						}
					}
					scannerOutputFlac.close();
					if (logWarning1 != "") {
						// warnung ausgeben
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_FLAC)
										+ getTextResourceService().getText(locale, ERROR_XML_C_FLAC_WARNING,
												logWarning1 + logWarning2 + logWarning3 + logWarning4 + logWarning5));
					}

					if (logError1 != "") {
						// Fehler ausgeben
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_FLAC)
										+ getTextResourceService().getText(locale, ERROR_XML_C_FLAC_ERROR,
												logError1 + logError2 + logError3 + logError4 + logError5));
					}
					if (!isValidC) {
						isValid = false;
					}
				}
			} catch (Exception e) {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_FLAC)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
				return false;
			}
			// TODO: Ende: Codec Auswertung
		}
		return isValid;
	}
}
