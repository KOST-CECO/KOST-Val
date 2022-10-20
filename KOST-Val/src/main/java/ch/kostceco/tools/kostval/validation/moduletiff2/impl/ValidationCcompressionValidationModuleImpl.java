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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kosttools.fileservice.Exiftool;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationCcompressionValidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationCcompressionValidationModule;

/**
 * Validierungsschritt C (Komprimierung-Validierung) Ist die TIFF-Datei gemäss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationCcompressionValidationModuleImpl extends
		ValidationModuleImpl implements ValidationCcompressionValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws ValidationCcompressionValidationException
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

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File( pathToExiftoolOutput,
				valDatei.getName() + ".exiftool-log.txt" );
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		String com1 = configMap.get( "AllowedCompression1" );
		String com2 = configMap.get( "AllowedCompression2" );
		String com3 = configMap.get( "AllowedCompression3" );
		String com4 = configMap.get( "AllowedCompression4" );
		String com5 = configMap.get( "AllowedCompression5" );
		String com7 = configMap.get( "AllowedCompression7" );
		String com8 = configMap.get( "AllowedCompression8" );
		String com32773 = configMap.get( "AllowedCompression32773" );

		if ( com1.equals( "" ) ) {
			com1 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com2.equals( "" ) ) {
			com2 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com3.equals( "" ) ) {
			com3 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com4.equals( "" ) ) {
			com4 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com5.equals( "" ) ) {
			com5 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com7.equals( "" ) ) {
			com7 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com8.equals( "" ) ) {
			com8 = "DieseKompressionIstNichtErlaubt";
		}
		if ( com32773.equals( "" ) ) {
			com32773 = "DieseKompressionIstNichtErlaubt";
		}

		Integer exiftoolio = 0;
		String oldErrorLine1 = "";
		String oldErrorLine2 = "";
		String oldErrorLine3 = "";
		String oldErrorLine4 = "";
		String oldErrorLine5 = "";

		/*
		 * TODO: Exiftool starten. Anschliessend auswerten! Auf jhove wird
		 * verzichtet
		 */

		/*
		 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein
		 * generelles TODO in allen Modulen. Zuerst immer dirOfJarPath ermitteln
		 * und dann alle Pfade mit
		 * 
		 * dirOfJarPath + File.separator +
		 * 
		 * erweitern.
		 */
		File pathFile = new File( ClassLoader.getSystemClassLoader()
				.getResource( "." ).getPath() );
		String locationOfJarPath = pathFile.getAbsolutePath();
		String dirOfJarPath = locationOfJarPath;
		if ( locationOfJarPath.endsWith( ".jar" )
				|| locationOfJarPath.endsWith( ".exe" )
				|| locationOfJarPath.endsWith( "." ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Exiftool.checkExiftool( dirOfJarPath );
		if ( !checkTool.equals( "OK" ) ) {
			// mindestens eine Datei fehlt fuer die Validierung
			if ( min ) {
				return false;
			} else {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_C_TIFF )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService()
												.getText( locale, ABORTED ) ) );
				isValid = false;
			}
		} else {
			try {
				String options = "-ver -a -s2 -FileName -Directory -Compression -PhotometricInterpretation"
						+ " -PlanarConfiguration -BitsPerSample -StripByteCounts -RowsPerStrip -FileSize"
						+ " -TileWidth -TileLength -TileDepth -G0:1";

				// Exiftool-Befehl: pathToPerl pathToExiftoolExe options
				// tiffFile >> report

				String resultExec = Exiftool.execExiftool( options, valDatei,
						exiftoolReport, workDir, dirOfJarPath );
				if ( !resultExec.equals( "OK" ) ) {
					// Exception oder Report existiert nicht
					if ( min ) {
						return false;
					} else {
						if ( resultExec.equals( "NoReport" ) ) {
							// Report existiert nicht

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_C_TIFF )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_MISSING_REPORT,
											exiftoolReport.getAbsolutePath(),
											getTextResourceService().getText(
													locale, ABORTED ) ) );
							return false;
						} else {
							// Exception

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_C_TIFF )
									+ getTextResourceService().getText( locale,
											ERROR_XML_SERVICEFAILED, "Exiftool",
											resultExec ) );
							return false;
						}
					}
				}
				// Ende Exiftool direkt auszuloesen

			} catch ( Exception e ) {
				if ( min ) {
					/* exiftoolReport loeschen */
					if ( exiftoolReport.exists() ) {
						exiftoolReport.delete();
					}
					return false;
				} else {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_C_TIFF )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				}
			}

			try {
				BufferedReader in = new BufferedReader(
						new FileReader( exiftoolReport ) );
				String line;
				while ( (line = in.readLine()) != null ) {
					/*
					 * zu analysierende TIFF-IFD-Zeile die
					 * CompressionScheme-Zeile enthält einer dieser Freitexte
					 * der Komprimierungsart
					 */
					if ( line.contains( "Compression: " )
							&& line.contains( "[EXIF:IFD" ) ) {
						exiftoolio = 1;
						if ( line.contains( "Compression: " + com1 )
								|| line.contains( "Compression: " + com2 )
								|| line.contains( "Compression: " + com3 )
								|| line.contains( "Compression: " + com4 )
								|| line.contains( "Compression: " + com5 )
								|| line.contains( "Compression: " + com7 )
								|| line.contains( "Compression: " + com8 )
								|| line.contains(
										"Compression: " + com32773 ) ) {
							// Valider Status
						} else {
							// Invalider Status
							isValid = false;
							if ( min ) {
								in.close();
								/* exiftoolReport loeschen */
								if ( exiftoolReport.exists() ) {
									exiftoolReport.delete();
								}
								return false;
							} else {
								if ( !line.equals( oldErrorLine1 )
										&& !line.equals( oldErrorLine2 )
										&& !line.equals( oldErrorLine3 )
										&& !line.equals( oldErrorLine4 )
										&& !line.equals( oldErrorLine5 ) ) {
									// neuer Fehler
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_C_TIFF )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_CG_INVALID,
																	line ) );
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
				}
				if ( exiftoolio == 0 ) {
					// Invalider Status
					isValid = false;
					if ( min ) {
						in.close();
						/* exiftoolReport loeschen */
						if ( exiftoolReport.exists() ) {
							exiftoolReport.delete();
						}
						return false;
					} else {

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_C_TIFF )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_CG_ETNIO, "C" ) );
					}
				}
				in.close();
			} catch ( Exception e ) {
				if ( min ) {
					/* exiftoolReport loeschen */
					if ( exiftoolReport.exists() ) {
						exiftoolReport.delete();
					}
					return false;
				} else {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_C_TIFF )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
					return false;
				}
			}
		}
		return isValid;
	}
}