/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;

/** kostval --> Controllervalinit
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllervalinitlog implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger( Controllervalinitlog.class );
	private static TextResourceService	textResourceService;

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	// TODO
	public boolean valInitlog( String[] args, Map<String, String> configMap, File directoryOfLogfile,
			Locale locale, String ausgabeStart, File logFile, String dirOfJarPath, String versionKostVal )
			throws IOException
	{
		boolean valInitlog = true;
		// ggf alte SIP-Validierung-Versions-Notiz loeschen
		File ECH160_1_1 = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "ECH160_1.1.txt" );
		File ECH160_1_0 = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "ECH160_1.0.txt" );
		if ( ECH160_1_1.exists() ) {
			Util.deleteFile( ECH160_1_1 );
		} else if ( ECH160_1_0.exists() ) {
			Util.deleteFile( ECH160_1_0 );
		}

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidation = configMap.get( "pdfavalidation" );
		String siardValidation = configMap.get( "siardValidation" );
		String tiffValidation = configMap.get( "tiffValidation" );
		String jp2Validation = configMap.get( "jp2Validation" );
		String jpegValidation = configMap.get( "jpegValidation" );

		String version = "";

		String formatValOn = "";
		// ermitteln welche Formate validiert werden koennen respektive eingeschaltet sind
		if ( pdfaValidation.equals( "yes" ) ) {
			formatValOn = "PDF/A";
			if ( tiffValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", TIFF";
			}
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( tiffValidation.equals( "yes" ) ) {
			formatValOn = "TIFF";
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jp2Validation.equals( "yes" ) ) {
			formatValOn = "JP2";
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( siardValidation.equals( "yes" ) ) {
			formatValOn = "SIARD";
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jpegValidation.equals( "yes" ) ) {
			formatValOn = "JPEG";
		}

		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_HEADER ) );
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_START, ausgabeStart ) );
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_END ) );
		LOGGER.logError(
				getTextResourceService().getText( locale, MESSAGE_XML_FORMATON, formatValOn, version ) );
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_INFO, versionKostVal ) );
		String config = "";
		for ( String key : configMap.keySet() ) {
			config = config + key + " " + configMap.get( key ) + "; ";
		}
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_CONFIG, config ) );

		if ( args[0].equals( "--format" ) && formatValOn.equals( "" ) ) {
			// Formatvalidierung aber alle Formate ausgeschlossen
			LOGGER.logError( getTextResourceService().getText( locale, ERROR_IOE,
					getTextResourceService().getText( locale, ERROR_NOFILEENDINGS ) ) );
			System.out.println( getTextResourceService().getText( locale, ERROR_NOFILEENDINGS ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			valInitlog = false;
			return valInitlog;
		}

		// Informationen zum Arbeitsverzeichnis holen
		File xslOrig = new File(
				dirOfJarPath + File.separator + "resources" + File.separator + "kost-val.xsl" );
		File xslCopy = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "kost-val.xsl" );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		/* Java 64 oder 32 bit? bei 32 dll austauschen wenn nicht bereits geschehen Auch 64
		 * kontrollieren, da es ja zwischenzeitlich geaendert haben koennte */
		String java64 = System.getProperty( "sun.arch.data.model" );
		File PdfValidatorAPIDll = new File( dirOfJarPath + File.separator + "PdfValidatorAPI.dll" );
		if ( !PdfValidatorAPIDll.exists() ) {
			String dll = (getTextResourceService().getText( locale, ERROR_MISSING,
					PdfValidatorAPIDll.getAbsolutePath() ));
			LOGGER.logError( dll );
			System.out.println( dll );
			valInitlog = false;
			return valInitlog;
		}
		String lengthNow = PdfValidatorAPIDll.length() + "";
		// System.out.println( PdfValidatorAPIDll.getAbsolutePath() + " " + lengthNow );
		File PdfValidatorAPIDll32 = new File(
				dirOfJarPath + File.separator + "resources" + File.separator + "3-Heights_PDF-Validator_dll"
						+ File.separator + "Win32" + File.separator + "PdfValidatorAPI.dll" );
		if ( !PdfValidatorAPIDll32.exists() ) {
			String dll32 = (getTextResourceService().getText( locale, ERROR_MISSING,
					PdfValidatorAPIDll32.getAbsolutePath() ));
			LOGGER.logError( dll32 );
			System.out.println( dll32 );
			valInitlog = false;
			return valInitlog;
		}
		String length32 = PdfValidatorAPIDll32.length() + "";
		// System.out.println( PdfValidatorAPIDll32.getAbsolutePath() + " " + length32 );
		File PdfValidatorAPIDll64 = new File(
				dirOfJarPath + File.separator + "resources" + File.separator + "3-Heights_PDF-Validator_dll"
						+ File.separator + "x64" + File.separator + "PdfValidatorAPI.dll" );
		if ( !PdfValidatorAPIDll64.exists() ) {
			String dll64 = (getTextResourceService().getText( locale, ERROR_MISSING,
					PdfValidatorAPIDll64.getAbsolutePath() ));
			LOGGER.logError( dll64 );
			System.out.println( dll64 );
			valInitlog = false;
			return valInitlog;
		}
		String length64 = PdfValidatorAPIDll64.length() + "";
		// System.out.println( PdfValidatorAPIDll64.getAbsolutePath() + " " + length64 );
		if ( java64.contains( "32" ) ) {
			if ( !lengthNow.equals( length32 ) ) {
				if ( !PdfValidatorAPIDll.getParentFile().canWrite() ) {
					// kein Schreibrecht
					// Installationsverzeichnis {0}; bestehende {3}-dll "{1}"; neu {4}-dll "{2}"
					String warning32xml = getTextResourceService().getText( locale,
							WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(), "64",
							java64 );
					LOGGER.logError( warning32xml );
					String warning32 = getTextResourceService().getText( locale,
							WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(), "64",
							java64 );
					System.out.println( warning32 );
					configMap.put( "pdftools", "no" );
				} else {
					Util.deleteFile( PdfValidatorAPIDll );
					if ( PdfValidatorAPIDll.exists() ) {
						// dll konnte nicht ersetzt werden
						String warning32xml = getTextResourceService().getText( locale,
								WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(), "64",
								java64 );
						LOGGER.logError( warning32xml );
						String warning32 = getTextResourceService().getText( locale,
								WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(), "64",
								java64 );
						System.out.println( warning32 );
						configMap.put( "pdftools", "no" );
					} else {
						Util.copyFile( PdfValidatorAPIDll32, PdfValidatorAPIDll );
						lengthNow = PdfValidatorAPIDll.length() + "";
						if ( !lengthNow.equals( length32 ) ) {
							// dll konnte nicht ersetzt werden
							String warning32xml = getTextResourceService().getText( locale,
									WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
									PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(),
									"64", java64 );
							LOGGER.logError( warning32xml );
							String warning32 = getTextResourceService().getText( locale,
									WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
									PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll32.getAbsolutePath(),
									"64", java64 );
							System.out.println( warning32 );
							configMap.put( "pdftools", "no" );
						}
					}
				}
			}
		} else if ( java64.contains( "64" ) ) {
			if ( !lengthNow.equals( length64 ) ) {
				if ( !PdfValidatorAPIDll.getParentFile().canWrite() ) {
					// kein Schreibrecht
					String warning64xml = getTextResourceService().getText( locale,
							WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(), "32",
							java64 );
					LOGGER.logError( warning64xml );
					String warning64 = getTextResourceService().getText( locale,
							WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
							PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(), "32",
							java64 );
					System.out.println( warning64 );
					configMap.put( "pdftools", "no" );
				} else {
					Util.deleteFile( PdfValidatorAPIDll );
					if ( PdfValidatorAPIDll.exists() ) {
						// dll konnte nicht ersetzt werden
						String warning64xml = getTextResourceService().getText( locale,
								WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(), "32",
								java64 );
						LOGGER.logError( warning64xml );
						String warning64 = getTextResourceService().getText( locale,
								WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
								PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(), "32",
								java64 );
						System.out.println( warning64 );
						configMap.put( "pdftools", "no" );
					} else {
						Util.copyFile( PdfValidatorAPIDll64, PdfValidatorAPIDll );
						lengthNow = PdfValidatorAPIDll.length() + "";
						if ( !lengthNow.equals( length32 ) ) {
							// dll konnte nicht ersetzt werden
							String warning64xml = getTextResourceService().getText( locale,
									WARNING_JARDIRECTORY_NOTWRITABLEXML, PdfValidatorAPIDll.getParentFile(),
									PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(),
									"32", java64 );
							LOGGER.logError( warning64xml );
							String warning64 = getTextResourceService().getText( locale,
									WARNING_JARDIRECTORY_NOTWRITABLE, PdfValidatorAPIDll.getParentFile(),
									PdfValidatorAPIDll.getAbsolutePath(), PdfValidatorAPIDll64.getAbsolutePath(),
									"32", java64 );
							System.out.println( warning64 );
							configMap.put( "pdftools", "no" );
						}
					}
				}
			}
		}

		return valInitlog;

	}
}