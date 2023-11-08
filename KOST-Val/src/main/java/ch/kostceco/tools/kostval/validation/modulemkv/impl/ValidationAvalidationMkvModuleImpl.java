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

package ch.kostceco.tools.kostval.validation.modulemkv.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import ch.kostceco.tools.kosttools.fileservice.ffmpeg;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulemkv.ValidationAmkvvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulemkv.ValidationAvalidationMkvModule;

/**
 * a) MKV-Erkennung mit ffprobe inkl Codecs
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationMkvModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationMkvModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationAmkvvalidationException
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
		// falls das File von einem vorhergehenden Durchlauf bereits
		// existiert, loeschen wir es
		if ( outputProbe.exists() ) {
			outputProbe.delete();
		}

		// Die Container-Erkennung erfolgt bereits im Vorfeld

		boolean isValid = true;

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
								MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService()
												.getText( locale, ABORTED ) ) );
				isValid = false;
			}
		} else {
			// ffprobe sollte vorhanden sein
			try {
				/*
				 * Doppelleerschlag im Pfad einer Datei bereitet Probleme
				 * (leerer Report) Video-Datei wird bei Doppelleerschlag in
				 * temp-Verzeichnis kopiert
				 */
				String valDateiPath = valDatei.getAbsolutePath();

				File valDateiTemp = new File(
						pathToWorkDir + File.separator + valDatei.getName() );
				if ( valDateiPath.contains( "  " ) ) {
					Util.copyFile( valDatei, valDateiTemp );
					System.out.println( " " );
					System.out.println(
							"   copy " + valDatei + " -> " + valDateiTemp );
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
								.getText( locale, MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_SERVICEINVALID, "ffprobe",
										"" ) );
					}
				} else {
					// Report existiert -> Auswerten...
					String matroska = "format_name=matroska,webm";
					String ffv1Codec = "codec_name=ffv1";
					String h264Codec = "codec_name=h264";
					String h265Codec = "codec_name=h265";
					String av1Codec = "codec_name=av1";
					String flacCodec = "codec_name=flac";
					String mp3Codec = "codec_name=mp3";
					String aacCodec = "codec_name=aac";
					String formatCodec = "";
					Scanner scannerFormat = new Scanner( outputProbe );
					int countFormat = 0;
					int countCodec = 0;
					while ( scannerFormat.hasNextLine() ) {
						// format_name=matroska,webm
						String line = scannerFormat.nextLine();
						if ( line.contains( "format_name=" ) ) {
							// container format auswerten
							countFormat = countFormat + 1;
							String format = line.replace( "format_name=", "" );
							if ( line.contains( matroska ) ) {
								// OK
								formatCodec = " container=" + format + "  ";
							} else {
								// NOK
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_MKV )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_MKV_FORMAT_NAZ,
												format ) );
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

					String ffv1Config = configMap.get( "Allowedmkvffv1" );
					String h264Config = configMap.get( "Allowedmkvh264" );
					String h265Config = configMap.get( "Allowedmkvh265" );
					String av1Config = configMap.get( "Allowedmkvav1" );
					String flacConfig = configMap.get( "Allowedmkvflac" );
					String mp3Config = configMap.get( "Allowedmkvmp3" );
					String aacConfig = configMap.get( "Allowedmkvaac" );

					Scanner scanner = new Scanner( outputProbe );
					while ( scanner.hasNextLine() ) {
						String line = scanner.nextLine();
						if ( line.contains( "codec_name=" ) ) {
							// Codec auswerten
							
							// Video: FFV1  H.264 (AVC) H.265 (HEVC) AV1
						// Audio: FLAC MP3 AAC

							countCodec = countCodec + 1;
							String codec = line.replace( "codec_name=", "" );
							String type = "codec";
							if ( line.contains( ffv1Codec )&&ffv1Config.equals( "FFV1" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( h264Codec )&&h264Config.equals( "H.264 (AVC)" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( h265Codec )&&h265Config.equals( "H.265 (HEVC)" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( av1Codec )&&av1Config.equals( "AV1" ) ) {
								// OK
								type = "videocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( flacCodec )&&flacConfig.equals( "FLAC" ) ) {
								// OK
								type = "audiocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( mp3Codec )&&mp3Config.equals( "MP3" ) ) {
								// OK
								type = "audiocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else if ( line.contains( aacCodec )&&aacConfig.equals( "AAC" ) ) {
								// OK
								type = "audiocodec";
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
							} else {
								// NOK
								if ( codec.equals( "h264" )
										|| codec.equals( "h265" )
										|| codec.equals( "ffv1" )
										|| codec.equals( "av1" )
										|| codec.equals( "jpeg2000" ) ) {
									// TODO: laufend erweitern
									type = "videocodec";
								} else if ( codec.equals( "mp3" )
										|| codec.equals( "flac" )
										|| codec.equals( "aac" )
										|| codec.equals( "vorbis" ) ) {
									// TODO: laufend erweitern
									type = "audiocodec";
								}
								formatCodec = formatCodec + type + "=" + codec
										+ "  ";
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_MKV )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_MKV_CODEC_NAZ,
												codec, type ) );
								isValid = false;
							}
						}
					}
					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_MKV )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_MKV_FORMATCODEC,
											formatCodec ) );

					if ( countFormat == 0 ) {
						// NOK
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_MKV_CODEC_NO, "format" ) );
						isValid = false;
					}
					if ( countCodec == 0 ) {
						// NOK
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_MKV_CODEC_NO, "codec" ) );
						isValid = false;
					}
					if ( countCodec == 1 ) {
						// Warnung. ggf Stummfilm
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_MKV_CODEC_ONE ) );
					}
					scanner.close();
				}
			} catch ( Exception e ) {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Erledigt: Codec Auswertung
		}
		return isValid;
	}
}
