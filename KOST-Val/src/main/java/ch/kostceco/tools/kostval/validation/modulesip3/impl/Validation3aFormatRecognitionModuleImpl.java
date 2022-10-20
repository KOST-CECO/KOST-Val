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

package ch.kostceco.tools.kostval.validation.modulesip3.impl;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ch.kostceco.tools.kosttools.fileservice.Recognition;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3aFormatRecognitionException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3aFormatRecognitionModule;

public class Validation3aFormatRecognitionModuleImpl extends
		ValidationModuleImpl implements Validation3aFormatRecognitionModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws Validation3aFormatRecognitionException
	{

		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "3A   " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean valid = true;

		// TODO ermitteln welche Formate validiert werden koennen
		// respektive eingeschaltet sind
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
		String ffv1Validation = configMap.get( "ffv1Validation" );
		String mp4Validation = configMap.get( "mp4Validation" );
		String mj2Validation = configMap.get( "mj2Validation" );
		String xmlValidation = configMap.get( "xmlValidation" );
		String siardValidation = configMap.get( "siardValidation" );
		String csvValidation = configMap.get( "csvValidation" );
		String xlsxValidation = configMap.get( "xlsxValidation" );
		String odsValidation = configMap.get( "odsValidation" );
		String otherformats = configMap.get( "otherformats" );

		String formatValOn = "";
		if ( !pdfaValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " PDF/A ";
			} else {
				formatValOn = formatValOn + " PDF/A ";
			}
		}
		if ( !txtValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " TXT  ";
			} else {
				formatValOn = formatValOn + " TXT  ";
			}
		}
		if ( !pdfValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " PDF  ";
			} else {
				formatValOn = formatValOn + " PDF  ";
			}
		}
		if ( !jp2Validation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " JP2  ";
			} else {
				formatValOn = formatValOn + " JP2  ";
			}
		}
		if ( !jpegValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " JPEG  ";
			} else {
				formatValOn = formatValOn + " JPEG  ";
			}
		}
		if ( !tiffValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " TIFF  ";
			} else {
				formatValOn = formatValOn + " TIFF  ";
			}
		}
		if ( !pngValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " PNG ";
			} else {
				formatValOn = formatValOn + " PNG ";
			}
		}
		if ( !flacValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " FLAC ";
			} else {
				formatValOn = formatValOn + " FLAC ";
			}
		}
		if ( !waveValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " WAVE ";
			} else {
				formatValOn = formatValOn + " WAVE ";
			}
		}
		if ( !mp3Validation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " MP3 ";
			} else {
				formatValOn = formatValOn + " MP3 ";
			}
		}
		if ( !ffv1Validation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " FFV1 ";
			} else {
				formatValOn = formatValOn + " FFV1 ";
			}
		}
		if ( !mp4Validation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " MP4 ";
			} else {
				formatValOn = formatValOn + " MP4 ";
			}
		}
		if ( !mj2Validation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " MJ2 ";
			} else {
				formatValOn = formatValOn + " MJ2 ";
			}
		}
		if ( !xmlValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " XML ";
			} else {
				formatValOn = formatValOn + " XML ";
			}
		}
		if ( !siardValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " SIARD ";
			} else {
				formatValOn = formatValOn + " SIARD ";
			}
		}
		if ( !csvValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " CSV ";
			} else {
				formatValOn = formatValOn + " CSV ";
			}
		}
		if ( !xlsxValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " XLSX ";
			} else {
				formatValOn = formatValOn + " XLSX ";
			}
		}
		if ( !odsValidation.equals( "no" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " ODS  ";
			} else {
				formatValOn = formatValOn + " ODS  ";
			}
		}
		if ( !otherformats.equals( "" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = " " + otherformats + " ";
			} else {
				formatValOn = formatValOn + " " + otherformats + " ";
			}
		}

		// TODO 3a und 3b
		Map<String, File> fileMap = Util.getFileMap( valDatei, true );
		Set<String> fileMapKeys = fileMap.keySet();
		String error3a = "";
		String error3b = "";
		for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
				.hasNext(); ) {
			String entryName = iterator.next();
			File newFile = fileMap.get( entryName );

			if ( !newFile.isDirectory() ) {
				String recFormat = "new";
				try {
					recFormat = Recognition.formatRec( newFile );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}

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
					recFormat = recFormat.replace( "UNKNOWN_", "" );
				}
				String formatValOnCtr = formatValOn;
				if ( formatValOnCtr.contains( "PDF/A" ) ) {
					formatValOnCtr = formatValOnCtr.replace( "PDF/A", "PDFA" );
				}

				String recFormatSpace = " " + recFormat + " ";

				// " "recFormat" " muss in formatValOn enthalten sein

				if ( !formatValOnCtr.contains( recFormatSpace ) ) {
					if ( error3a.equals( "" ) ) {
						error3a = recFormat;
					} else if ( !error3a.contains( recFormat ) ) {
						error3a = error3a + ", " + recFormat;
					}

					String contentPath = newFile.getAbsolutePath()
							.replace( valDatei.getAbsolutePath() + "/", "" );
					contentPath = newFile.getAbsolutePath()
							.replace( valDatei.getAbsolutePath() + "\\", "" );
					if ( error3b.equals( "" ) ) {
						error3b = getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Cb_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_SERVICEMESSAGE, contentPath,
										"(" + recFormat + ")" );
					} else {
						error3b = error3b + " "
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MODUL_Cb_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_SERVICEMESSAGE, contentPath,
										"(" + recFormat + ")" );
					}
					valid = false;
				}
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "3A-  " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "3A\\  " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "3A|  " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "3A/  " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		if ( !valid ) {
			// TODO Fehlermeldungen ausgeben
			// Fehlermedung 3a ausgeben
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CA_FILES, error3a ) );
			// Fehlermedung 3b ausgeben
			Logtxt.logtxt( logFile, error3b );
		}
		return valid;
	}

}
