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
			Locale locale, String ausgabeStart, File logFile, String dirOfJarPath ) throws IOException
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
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_INFO ) );
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
			System.exit( 1 );
		}

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = configMap.get( "PathToWorkDir" );

		File xslOrig = new File(
				dirOfJarPath + File.separator + "resources" + File.separator + "kost-val.xsl" );
		File xslCopy = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "kost-val.xsl" );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		File tmpDir = new File( pathToWorkDir );

		// Im Pfad keine Sonderzeichen xml-Validierung SIP 1d und SIARD C stuerzen ab

		String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~:\\\\a-zA-Z0-9 ]";
		Pattern pattern = Pattern.compile( patternStr );

		String name = tmpDir.getAbsolutePath();

		String[] pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER
						.logError( getTextResourceService().getText( locale, ERROR_IOE, getTextResourceService()
								.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) ) );
				System.console().printf( getTextResourceService().getText( locale, ERROR_SPECIAL_CHARACTER,
						name, matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		name = directoryOfLogfile.getAbsolutePath();

		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER
						.logError( getTextResourceService().getText( locale, ERROR_IOE, getTextResourceService()
								.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) ) );
				System.console().printf( getTextResourceService().getText( locale, ERROR_SPECIAL_CHARACTER,
						name, matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		// Im Pfad keine Sonderzeichen xml-Validierung SIP 1d und SIARD C stuerzen ab
		File valDatei = new File( args[1] );

		name = valDatei.getAbsolutePath();

		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER
						.logError( getTextResourceService().getText( locale, ERROR_IOE, getTextResourceService()
								.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) ) );
				System.console().printf( getTextResourceService().getText( locale, ERROR_SPECIAL_CHARACTER,
						name, matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		return valInitlog;

	}
}