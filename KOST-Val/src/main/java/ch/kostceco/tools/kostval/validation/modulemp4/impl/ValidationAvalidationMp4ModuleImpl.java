/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.modulemp4.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import ch.kostceco.tools.kosttools.fileservice.ffmpeg;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulemp4.ValidationAmp4validationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulemp4.ValidationAvalidationMp4Module;

/**
 * a) MP4-Erkennung mit ffprobe inkl Codecs
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationMp4ModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationMp4Module
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationAmp4validationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File workDir = new File( pathToWorkDir );
		if ( !workDir.exists() ) {
			workDir.mkdir();
		}
		File outputProbe = new File(
				pathToWorkDir + File.separator + "ffprobe.txt" );
		File outputFfmpeg = new File(
				pathToWorkDir + File.separator + "ffmpeg.txt" );
		// falls das File von einem vorhergehenden Durchlauf bereits
		// existiert, loeschen wir es
		if ( outputProbe.exists() ) {
			outputProbe.delete();
		}
		if ( outputFfmpeg.exists() ) {
			outputFfmpeg.delete();
		}

		// Die Container-Erkennung erfolgt bereits im Vorfeld

		boolean isValid = true;
		boolean isValidB = true;

		// TODO: inArbeit: codec-Erkennung mit ffprobe

		// - Initialisierung ffprobe -> existiert alles zu ffmpeg?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = ffmpeg.checkFfmpegPlayProbe( dirOfJarPath );
		if ( !checkTool.equals( "OK" ) ) {
			if ( min ) {
				return false;
			} else {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_MP4 )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService()
												.getText( locale, ABORTED ) ) );
				isValid = false;
			}
		} else {
			// ffmpeg, ffplay und ffprobe sollte vorhanden sein
			try {
				/*
				 * Doppelleerschlag im Pfad oder im Namen einer Datei bereitet
				 * Probleme (leerer Report) Video-Datei wird bei
				 * Doppelleerschlag in temp-Verzeichnis kopiert
				 */
				String valDateiPath = valDatei.getAbsolutePath();
				String valDateiName = valDatei.getName().replace( "  ", " " );
				valDateiName = valDateiName.replace( "  ", " " );
				valDateiName = valDateiName.replace( "  ", " " );

				File valDateiTemp = new File(
						pathToWorkDir + File.separator + valDateiName );
				if ( valDateiPath.contains( "  " ) ) {
					Util.copyFile( valDatei, valDateiTemp );
				} else {
					valDateiTemp = valDatei;
				}

				String resultExec = ffmpeg.execFfprobe( valDateiTemp,
						outputProbe, workDir, dirOfJarPath );
				if ( !resultExec.equals( "OK" ) || !outputProbe.exists() ) {
					// Exception oder Report existiert nicht
					if ( min ) {
						return false;
					} else {
						isValid = false;
						// Erster Fehler! Meldung A ausgeben und invalid setzten
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_SERVICEINVALID, "ffprobe",
										"" ) );
					}
				} else {
					// Report existiert -> Auswerten...
					String mp4 = "format_name=mov,mp4,m4a,3gp,3g2,mj2";
					String avcCodec = "codec_name=h264";
					String hevcCodec = "codec_name=hevc";
					String mp3Codec = "codec_name=mp3";
					String aacCodec = "codec_name=aac";
					String formatCodec = "";
					Scanner scannerFormat = new Scanner( outputProbe );
					int countFormat = 0;
					int countVideoCodec = 0;
					int countAudioCodec = 0;
					while ( scannerFormat.hasNextLine() ) {
						// format_name=mov,mp4,m4a,3gp,3g2,mj2
						String line = scannerFormat.nextLine();
						if ( line.contains( "format_name=" ) ) {
							// container format auswerten
							countFormat = countFormat + 1;
							String format = line.replace( "format_name=", "" );
							String formatName = format.toUpperCase();
							if ( line.contains( mp4 ) ) {
								// OK
								formatName = "MP4";
								formatCodec = " container=" + formatName + "  ";
							} else {
								// NOK
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_MP4 )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_AUDIOVIDEO_FORMAT_NAZ,
												formatName ) );
								isValid = false;
							}
						}
					}
					scannerFormat.close();
					// codec auswertung

					/*
					 * Nicht vergessen in
					 * "src/main/resources/config/applicationContext-services.xml"
					 * beim entsprechenden Modul die property anzugeben:
					 * <property name="configurationService"
					 * ref="configurationService" />
					 */

					String avcConfig = configMap.get( "Allowedmp4avc" );
					String hevcConfig = configMap.get( "Allowedmp4hevc" );
					String mp3Config = configMap.get( "Allowedmp4mp3" );
					String aacConfig = configMap.get( "Allowedmp4aac" );

					Scanner scanner = new Scanner( outputProbe );
					while ( scanner.hasNextLine() ) {
						String line = scanner.nextLine();
						if ( line.contains( "codec_name=" ) ) {
							// Codec auswerten

							// Video: AVC HEVC
							// Audio: MP3 AAC

							String codec = line.replace( "codec_name=", "" );
							String codecName = codec.toUpperCase();
							String type = "codec";
							if ( line.contains( avcCodec )
									&& avcConfig.equals( "AVC" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "="
										+ avcConfig + "  ";
								countVideoCodec = countVideoCodec + 1;
							} else if ( line.contains( hevcCodec )
									&& hevcConfig.equals( "HEVC" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "="
										+ hevcConfig + "  ";
								countVideoCodec = countVideoCodec + 1;
							} else if ( line.contains( mp3Codec )
									&& mp3Config.equals( "MP3" ) ) {
								// OK
								type = "audiocodec";
								formatCodec = formatCodec + type + "="
										+ mp3Config + "  ";
								countAudioCodec = countAudioCodec + 1;
							} else if ( line.contains( aacCodec )
									&& aacConfig.equals( "AAC" ) ) {
								// OK
								type = "audiocodec";
								formatCodec = formatCodec + type + "="
										+ aacConfig + "  ";
								countAudioCodec = countAudioCodec + 1;
							} else {
								// NOK
								if ( codec.equals( "h264" )
										|| codec.equals( "hevc" )
										|| codec.equals( "ffv1" )
										|| codec.equals( "av1" )
										|| codec.equals( "jpeg2000" )
										|| codec.equals( "huffyuv" )
										|| codec.equals( "vp8" )
										|| codec.equals( "vp9" ) ) {
									// TODO: laufend erweitern auch bei MKV
									type = "videocodec";
									countVideoCodec = countVideoCodec + 1;
								} else if ( codec.equals( "mp3" )
										|| codec.equals( "flac" )
										|| codec.equals( "aac" )
										|| codec.equals( "mp2" )
										|| codec.equals( "ac3" )
										|| codec.equals( "alac" )
										|| codec.equals( "opus" )
										|| codec.equals( "vorbis" )
										|| codec.contains( "pcm_" ) ) {
									// TODO: laufend erweitern auch bei MKV
									type = "audiocodec";
									countAudioCodec = countAudioCodec + 1;
								}
								formatCodec = formatCodec + type + "="
										+ codecName + "  ";
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_MP4 )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_AUDIOVIDEO_CODEC_NAZ,
												codecName, type ) );
								isValid = false;
							}
						}
					}
					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_MP4 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_AUDIOVIDEO_FORMATCODEC,
											formatCodec ) );

					if ( countFormat == 0 ) {
						// NOK
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_AUDIOVIDEO_CODEC_NO,
										"format", "MP4" ) );
						isValid = false;
					}
					if ( countVideoCodec == 0 ) {
						if ( configMap.get( "Allowedmp4novideo" )
								.equals( "Error" ) ) {
							// NOK
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_VIDEO_CODEC_NOVIDEO_ERROR,
											"MP4" ) );
							isValid = false;
						} else {
							// Warnung
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_VIDEO_CODEC_NOVIDEO_WARNING,
											"MP4" ) );
						}
					}
					if ( countAudioCodec == 0 ) {
						if ( configMap.get( "Allowedmp4noaudio" )
								.equals( "Error" ) ) {
							// NOK
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_AUDIOVIDEO_CODEC_NOAUDIO_ERROR,
											"MP4" ) );
							isValid = false;
						} else {
							// Warnung
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_VIDEO_CODEC_NOAUDIO_WARNING,
											"MP4" ) );
						}
					}
					if ( countVideoCodec == 0 && countAudioCodec == 0 ) {
						// NOK
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MP4 )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_AUDIOVIDEO_CODEC_NO,
										"codec", "MP4" ) );
						isValid = false;
					}

					scanner.close();
				}
				// TODO: Erledigt: Codec Auswertung

				if ( isValid ) {
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
					if ( outputFfmpeg.exists() ) {
						outputFfmpeg.delete();
					}

					String resultFfmpegExec = ffmpeg.execFfmpeg( valDateiTemp,
							outputFfmpeg, workDir, dirOfJarPath );
					if ( !resultFfmpegExec.equals( "OK" )
							|| !outputFfmpeg.exists() ) {
						// Exception oder Report existiert nicht
						if ( min ) {
							return false;
						} else {
							isValid = false;
							// Erster Fehler! Meldung B ausgeben und invalid
							// setzten
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_B_MP4 )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_SERVICEINVALID,
											"ffmpeg", "" ) );
						}
					} else {
						// Report existiert -> Auswerten...
						String errorFilePathName = "Error opening output file "
								+ valDateiTemp.getAbsolutePath();
						Scanner scannerFormat = new Scanner( outputFfmpeg );
						while ( scannerFormat.hasNextLine() ) {
							String line = "";
							line = scannerFormat.nextLine();
							// [out#0/mp4 @ 0000023c307ec780] Output file does
							// not contain any stream
							// Error opening output file
							// C:\Users\clair\Documents\PPEG_Video\div_Murmeli_Container_Codecs\MP4_AVC_MP3_az_Murmeli_2018.mp4.
							// Error opening output files: Invalid argument
							if ( line.startsWith( "[" )
									|| line.startsWith( "Error " ) ) {
								if ( line.contains( errorFilePathName ) ) {
									// Meldung ignorieren, kein Mehrwert
								} else {
									// NOK
									isValidB = false;
									if ( errB1 == "" ) {
										errB1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB2 == "" ) {
										errB2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB3 == "" ) {
										errB3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB4 == "" ) {
										errB4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB5 == "" ) {
										errB5 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB6 == "" ) {
										errB6 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB7 == "" ) {
										errB7 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB8 == "" ) {
										errB8 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB9 == "" ) {
										errB9 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB10 == "" ) {
										errB10 = "<Message> - " + line
												+ "</Message>";
									} else {
										errB10 = errB10.replace( "</Message>",
												" ... </Message>" );
									}
								}
							}
						}
						scannerFormat.close();
					}
					if ( !isValidB ) {
						// Fehlermeldungen ausgeben
						isValid = false;
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_B_MP4 )
								+ getTextResourceService().getText( locale,
										ERROR_XML_B_AUDIOVIDEO_ERROR,
										errB1 + errB2 + errB3 + errB4 + errB5
												+ errB6 + errB7 + errB8 + errB9
												+ errB10 ) );
					}
				}
				// TODO: Erledigt: Analyse ffmpeg
			} catch ( Exception e ) {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_MP4 )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
		}
		return isValid;
	}
}
