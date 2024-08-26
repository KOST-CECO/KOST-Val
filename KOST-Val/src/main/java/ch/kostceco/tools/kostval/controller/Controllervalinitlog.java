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

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * kostval --> Controllervalinit
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllervalinitlog implements MessageConstants
{

	private static TextResourceService textResourceService;

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

	// TODO
	public boolean valInitlog( String[] args, Map<String, String> configMap,
			File directoryOfLogfile, Locale locale, String ausgabeStart,
			File logFile, String dirOfJarPath, String versionKostVal )
			throws IOException
	{
		boolean valInitlog = true;
		// ggf alte SIP-Validierung-Versions-Notiz loeschen
		File ECH160_1_2 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.2.txt" );
		File ECH160_1_1 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.1.txt" );
		File ECH160_1_0 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.0.txt" );
		if ( ECH160_1_2.exists() ) {
			Util.deleteFile( ECH160_1_2 );
		} else if ( ECH160_1_1.exists() ) {
			Util.deleteFile( ECH160_1_1 );
		} else if ( ECH160_1_0.exists() ) {
			Util.deleteFile( ECH160_1_0 );
		}

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

		String formatValOn = "";
		String formatRecOn = "";
		// ermitteln welche Formate validiert werden koennen respektive
		// eingeschaltet sind "yes"; "az"; "no";
		if ( pdfaValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "PDF/A";
			} else {
				formatValOn = formatValOn + ", PDF/A";
			}
		} else if ( pdfaValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "PDF/A";
			} else {
				formatRecOn = formatRecOn + ", PDF/A";
			}
		}
		if ( txtValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "TXT";
			} else {
				formatValOn = formatValOn + ", TXT";
			}
		} else if ( txtValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "TXT";
			} else {
				formatRecOn = formatRecOn + ", TXT";
			}
		}
		if ( pdfValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "PDF";
			} else {
				formatValOn = formatValOn + ", PDF";
			}
		} else if ( pdfValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "PDF";
			} else {
				formatRecOn = formatRecOn + ", PDF";
			}
		}
		if ( jp2Validation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "JP2";
			} else {
				formatValOn = formatValOn + ", JP2";
			}
		} else if ( jp2Validation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "JP2";
			} else {
				formatRecOn = formatRecOn + ", JP2";
			}
		}
		if ( jpegValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "JPEG";
			} else {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jpegValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "JPEG";
			} else {
				formatRecOn = formatRecOn + ", JPEG";
			}
		}
		if ( tiffValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "TIFF";
			} else {
				formatValOn = formatValOn + ", TIFF";
			}
		} else if ( tiffValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "TIFF";
			} else {
				formatRecOn = formatRecOn + ", TIFF";
			}
		}
		if ( pngValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "PNG";
			} else {
				formatValOn = formatValOn + ", PNG";
			}
		} else if ( pngValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "PNG";
			} else {
				formatRecOn = formatRecOn + ", PNG";
			}
		}
		if ( flacValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "FLAC";
			} else {
				formatValOn = formatValOn + ", FLAC";
			}
		} else if ( flacValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "FLAC";
			} else {
				formatRecOn = formatRecOn + ", FLAC";
			}
		}
		if ( waveValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "WAVE";
			} else {
				formatValOn = formatValOn + ", WAVE";
			}
		} else if ( waveValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "WAVE";
			} else {
				formatRecOn = formatRecOn + ", WAVE";
			}
		}
		if ( mp3Validation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "MP3";
			} else {
				formatValOn = formatValOn + ", MP3";
			}
		} else if ( mp3Validation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "MP3";
			} else {
				formatRecOn = formatRecOn + ", MP3";
			}
		}
		if ( mkvValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "MKV";
			} else {
				formatValOn = formatValOn + ", MKV";
			}
		} else if ( mkvValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "MKV";
			} else {
				formatRecOn = formatRecOn + ", MKV";
			}
		}
		if ( mp4Validation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "MP4";
			} else {
				formatValOn = formatValOn + ", MP4";
			}
		} else if ( mp4Validation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "MP4";
			} else {
				formatRecOn = formatRecOn + ", MP4";
			}
		}
		if ( xmlValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "XML";
			} else {
				formatValOn = formatValOn + ", XML";
			}
		} else if ( xmlValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "XML";
			} else {
				formatRecOn = formatRecOn + ", XML";
			}
		}
		if ( jsonValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "JSON";
			} else {
				formatValOn = formatValOn + ", JSON";
			}
		} else if ( jsonValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "JSON";
			} else {
				formatRecOn = formatRecOn + ", JSON";
			}
		}
		if ( siardValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "SIARD";
			} else {
				formatValOn = formatValOn + ", SIARD";
			}
		} else if ( siardValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "SIARD";
			} else {
				formatRecOn = formatRecOn + ", SIARD";
			}
		}
		if ( csvValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "CSV";
			} else {
				formatValOn = formatValOn + ", CSV";
			}
		} else if ( csvValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "CSV";
			} else {
				formatRecOn = formatRecOn + ", CSV";
			}
		}
		if ( xlsxValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "XLSX";
			} else {
				formatValOn = formatValOn + ", XLSX";
			}
		} else if ( xlsxValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "XLSX";
			} else {
				formatRecOn = formatRecOn + ", XLSX";
			}
		}
		if ( odsValidation.equals( "yes" ) ) {
			if ( formatValOn.equals( "" ) ) {
				formatValOn = "ODS";
			} else {
				formatValOn = formatValOn + ", ODS";
			}
		} else if ( odsValidation.equals( "az" ) ) {
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = "ODS";
			} else {
				formatRecOn = formatRecOn + ", ODS";
			}
		}
		if ( !otherformats.equals( "" ) ) {
			otherformats = otherformats.replace( " ", ", " );
			if ( formatRecOn.equals( "" ) ) {
				formatRecOn = otherformats;
			} else {
				formatRecOn = formatRecOn + ", " + otherformats;
			}
		}

		Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
				MESSAGE_XML_HEADER ) );
		Logtxt.logtxt( logFile, "<Infos><Start>" + ausgabeStart + "</Start>" );
		Logtxt.logtxt( logFile, "<End></End>" );
		String val = getTextResourceService().getText( locale,
				MESSAGE_XML_VALTYPE, " " + formatValOn );
		String rec = getTextResourceService().getText( locale,
				MESSAGE_XML_AZTYPE, " " + formatRecOn );
		// val.message.xml.valtype = <ValType>Validierung: {0}</ValType>
		// val.message.xml.aztype = <ValType>Erkennung: {0}</ValType>
		formatValOn = val.replace( "ValType", "FormatValOn" );
		formatRecOn = rec.replace( "ValType", "FormatRecOn" );
		Logtxt.logtxt( logFile, formatValOn );
		Logtxt.logtxt( logFile, formatRecOn );
		Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
				MESSAGE_XML_INFO, versionKostVal ) );
		String config = "";
		for ( String key : configMap.keySet() ) {
			config = config + key + " " + configMap.get( key ) + "; ";
		}
		Logtxt.logtxt( logFile,
				"<configuration>" + config + "</configuration>" );

		if ( args[0].equals( "--format" ) && formatValOn.equals( "" ) ) {
			// Formatvalidierung aber alle Formate ausgeschlossen
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, ERROR_IOE,
							getTextResourceService().getText( locale,
									ERROR_NOFILEENDINGS ) ) );
			System.out.println( getTextResourceService().getText( locale,
					ERROR_NOFILEENDINGS ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			valInitlog = false;
			return valInitlog;
		}

		// Informationen zum Arbeitsverzeichnis holen
		File xslOrig = new File( dirOfJarPath + File.separator + "resources"
				+ File.separator + "kost-val.xsl" );
		File xslCopy = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "kost-val.xsl" );
		String xslLog = "<!-- kost-val.xsl_v" + versionKostVal + " -->";
		Boolean oldXslLog = !Util.stringInFile( xslLog, xslCopy );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		} else if ( oldXslLog ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		/*
		 * Java 64 kontrollieren, da es ja zwischenzeitlich geaendert haben
		 * koennte
		 */
		String java64 = System.getProperty( "sun.arch.data.model" );
		File PdfValidatorAPIDll = new File(
				dirOfJarPath + File.separator + "PdfValidatorAPI.dll" );
		if ( !PdfValidatorAPIDll.exists() ) {
			String dll = (getTextResourceService().getText( locale,
					ERROR_MISSING, PdfValidatorAPIDll.getAbsolutePath() ));
			Logtxt.logtxt( logFile, dll );
			System.out.println( dll );
			valInitlog = false;
			return valInitlog;
		}
		String lengthNow = PdfValidatorAPIDll.length() + "";
		// System.out.println( PdfValidatorAPIDll.getAbsolutePath() + " " +
		// lengthNow );
		File PdfValidatorAPIDll64 = new File(
				dirOfJarPath + File.separator + "resources" + File.separator
						+ "3-Heights_PDF-Validator_dll" + File.separator + "x64"
						+ File.separator + "PdfValidatorAPI.dll" );
		if ( !PdfValidatorAPIDll64.exists() ) {
			String dll64 = (getTextResourceService().getText( locale,
					ERROR_MISSING, PdfValidatorAPIDll64.getAbsolutePath() ));
			Logtxt.logtxt( logFile, dll64 );
			System.out.println( dll64 );
			valInitlog = false;
			return valInitlog;
		}
		String length64 = PdfValidatorAPIDll64.length() + "";
		// System.out.println( PdfValidatorAPIDll64.getAbsolutePath() + " " +
		// length64 );
		if ( java64.contains( "64" ) ) {
			if ( !lengthNow.equals( length64 ) ) {
				if ( !PdfValidatorAPIDll.getParentFile().canWrite() ) {
					// kein Schreibrecht
					String warning64xml = getTextResourceService().getText(
							locale, WARNING_JARDIRECTORY_NOTWRITABLEXML,
							PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(),
							PdfValidatorAPIDll64.getAbsolutePath(), "32",
							java64 );
					Logtxt.logtxt( logFile, warning64xml );
					String warning64 = getTextResourceService().getText( locale,
							WARNING_JARDIRECTORY_NOTWRITABLE,
							PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(),
							PdfValidatorAPIDll64.getAbsolutePath(), "32",
							java64 );
					System.out.println( warning64 );
					configMap.put( "pdftools", "no" );
				} else {
					Util.deleteFile( PdfValidatorAPIDll );
					if ( PdfValidatorAPIDll.exists() ) {
						// dll konnte nicht ersetzt werden
						String warning64xml = getTextResourceService().getText(
								locale, WARNING_JARDIRECTORY_NOTWRITABLEXML,
								PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(),
								PdfValidatorAPIDll64.getAbsolutePath(), "32",
								java64 );
						Logtxt.logtxt( logFile, warning64xml );
						String warning64 = getTextResourceService().getText(
								locale, WARNING_JARDIRECTORY_NOTWRITABLE,
								PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(),
								PdfValidatorAPIDll64.getAbsolutePath(), "32",
								java64 );
						System.out.println( warning64 );
						configMap.put( "pdftools", "no" );
					} else {
						Util.copyFile( PdfValidatorAPIDll64,
								PdfValidatorAPIDll );
						lengthNow = PdfValidatorAPIDll.length() + "";
					}
				}
			}
		}

		return valInitlog;

	}
}