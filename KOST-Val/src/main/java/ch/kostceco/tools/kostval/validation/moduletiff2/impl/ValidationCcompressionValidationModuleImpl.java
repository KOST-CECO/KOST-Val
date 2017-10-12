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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationCcompressionValidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.StreamGobbler;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationCcompressionValidationModule;

/** Validierungsschritt C (Komprimierung-Validierung) Ist die TIFF-Datei gemäss Konfigurationsdatei
 * valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationCcompressionValidationModuleImpl extends ValidationModuleImpl implements
		ValidationCcompressionValidationModule
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
			throws ValidationCcompressionValidationException
	{

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File( pathToExiftoolOutput, valDatei.getName() + ".exiftool-log.txt" );
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		String com1 = getConfigurationService().getAllowedCompression1();
		String com2 = getConfigurationService().getAllowedCompression2();
		String com3 = getConfigurationService().getAllowedCompression3();
		String com4 = getConfigurationService().getAllowedCompression4();
		String com5 = getConfigurationService().getAllowedCompression5();
		String com7 = getConfigurationService().getAllowedCompression7();
		String com8 = getConfigurationService().getAllowedCompression8();
		String com32773 = getConfigurationService().getAllowedCompression32773();

		if ( com1.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com1 );
			return false;
		}
		if ( com2.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com2 );
			return false;
		}
		if ( com3.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com3 );
			return false;
		}
		if ( com4.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com4 );
			return false;
		}
		if ( com5.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com5 );
			return false;
		}
		if ( com7.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com7 );
			return false;
		}
		if ( com8.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com8 );
			return false;
		}
		if ( com32773.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF ) + com32773 );
			return false;
		}

		Integer exiftoolio = 0;
		String oldErrorLine1 = "";
		String oldErrorLine2 = "";
		String oldErrorLine3 = "";
		String oldErrorLine4 = "";
		String oldErrorLine5 = "";

		/* TODO: Exiftool starten. Anschliessend auswerten! Auf jhove wird verzichtet */

		
		/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
		 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
		 * 
		 * dirOfJarPath + File.separator +
		 * 
		 * erweitern. */
		String path = new java.io.File( KOSTVal.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath() ).getAbsolutePath();
		path = path.substring( 0, path.lastIndexOf( "." ) );
		path = path + System.getProperty( "java.class.path" );
		String locationOfJarPath = path;
		String dirOfJarPath = locationOfJarPath;
		if ( locationOfJarPath.endsWith( ".jar" ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		File fIdentifyPl = new File(dirOfJarPath + File.separator + "resources" + File.separator + "ExifTool-10.15" + File.separator
				+ "exiftool.pl" );
		String pathToIdentifyPl = fIdentifyPl.getAbsolutePath();
		if ( !fIdentifyPl.exists() ) {
			// exiftool.pl existiert nicht --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
							+ getTextResourceService().getText( MESSAGE_XML_CG_ET_MISSING, pathToIdentifyPl ) );
			return false;
		} else {
			File fPerl = new File(dirOfJarPath + File.separator + "resources" + File.separator + "ExifTool-10.15" + File.separator
					+ "Perl" + File.separator + "bin" + File.separator + "perl.exe" );
			String pathToPerl = fPerl.getAbsolutePath();
			if ( !fPerl.exists() ) {
				// Perl.exe existiert nicht --> Abbruch
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_CG_ET_MISSING, pathToPerl ) );
				return false;
			} else {

				try {

					String command = "cmd /c \"\""
							+ pathToPerl
							+ "\" \""
							+ pathToIdentifyPl
							+ "\" -ver -a -s2 -FileName -Directory -Compression -FillOrder -PhotometricInterpretation"
							+ " -PlanarConfiguration -BitsPerSample -StripByteCounts -RowsPerStrip -FileSize"
							+ " -Orientation -TileWidth -TileLength -TileDepth \"" + valDatei.getAbsolutePath()
							+ "\" >>\"" + pathToExiftoolOutput + "\"";
					/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
					 * gehts: cmd /c\"urspruenlicher Befehl\" */

					Process proc = null;
					Runtime rt = null;

					try {
						/* falls das File exiftoolReport bereits existiert, z.B. von einem vorhergehenden
						 * Durchlauf, löschen wir es */
						if ( exiftoolReport.exists() ) {
							exiftoolReport.delete();
						}
						Util.switchOffConsole();
						rt = Runtime.getRuntime();
						proc = rt.exec( command.toString().split( " " ) );
						// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

						// Fehleroutput holen
						StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

						// Output holen
						StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

						// Threads starten
						errorGobbler.start();
						outputGobbler.start();

						// Warte, bis wget fertig ist
						proc.waitFor();

						Util.switchOnConsole();
						// Kontrolle ob der Report existiert
						if ( !exiftoolReport.exists() ) {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_ET_MISSING ) );
							return false;
						}
					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
										+ getTextResourceService().getText( MESSAGE_XML_CG_ET_SERVICEFAILED,
												e.getMessage() ) );
						return false;
					} finally {
						if ( proc != null ) {
							closeQuietly( proc.getOutputStream() );
							closeQuietly( proc.getInputStream() );
							closeQuietly( proc.getErrorStream() );
						}
					}

					// Ende Exiftool direkt auszulösen

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				}

			}
		}

		try {
			BufferedReader in = new BufferedReader( new FileReader( exiftoolReport ) );
			String line;
			while ( (line = in.readLine()) != null ) {
				/* zu analysierende TIFF-IFD-Zeile die CompressionScheme-Zeile enthält einer dieser
				 * Freitexte der Komprimierungsart */
				if ( line.contains( "Compression: " ) ) {
					exiftoolio = 1;
					if ( line.equalsIgnoreCase( "Compression: " + com1 )
							|| line.equalsIgnoreCase( "Compression: " + com2 )
							|| line.equalsIgnoreCase( "Compression: " + com3 )
							|| line.equalsIgnoreCase( "Compression: " + com4 )
							|| line.equalsIgnoreCase( "Compression: " + com5 )
							|| line.equalsIgnoreCase( "Compression: " + com7 )
							|| line.equalsIgnoreCase( "Compression: " + com8 )
							|| line.equalsIgnoreCase( "Compression: " + com32773 ) ) {
						// Valider Status
					} else {
						// Invalider Status
						isValid = false;
						if ( !line.equals( oldErrorLine1 ) && !line.equals( oldErrorLine2 )
								&& !line.equals( oldErrorLine3 ) && !line.equals( oldErrorLine4 )
								&& !line.equals( oldErrorLine5 ) ) {
							// neuer Fehler
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							if ( oldErrorLine1.equals( "" ) ) {
								oldErrorLine1 = line;
							} else if ( oldErrorLine2.equals( "" ) ) {
								oldErrorLine2 = line;
							} else if ( oldErrorLine3.equals( "" ) ) {
								oldErrorLine3 = line;
							} else if ( oldErrorLine4.equals( "" ) ) {
								oldErrorLine4 = line;
							} else if ( oldErrorLine5.equals( "" ) ) {
								oldErrorLine5 = line;
							}
						}
					}
				}
			}
			if ( exiftoolio == 0 ) {
				// Invalider Status
				isValid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_CG_ETNIO, "C" ) );
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
							+ getTextResourceService().getText( MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
			return false;
		}
		return isValid;
	}
}