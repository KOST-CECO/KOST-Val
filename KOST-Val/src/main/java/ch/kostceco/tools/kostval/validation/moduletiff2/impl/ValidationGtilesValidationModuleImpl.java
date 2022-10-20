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
import java.util.Locale;
import java.util.Map;
import java.io.FileReader;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationGtilesValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationGtilesValidationModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt G (Kacheln-Validierung) Ist die TIFF-Datei gem�ss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationGtilesValidationModuleImpl extends ValidationModuleImpl
		implements ValidationGtilesValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws ValidationGtilesValidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File( pathToExiftoolOutput,
				valDatei.getName() + ".exiftool-log.txt" );
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		if ( !exiftoolReport.exists() ) {
			// Report existiert nicht

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_G_TIFF )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_MISSING_REPORT,
									exiftoolReport.getAbsolutePath(),
									getTextResourceService().getText( locale,
											ABORTED ) ) );
			return false;
		} else {
			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			String tiles = configMap.get( "AllowedTiles" );

			Integer exiftoolio = 0;
			String oldErrorLine1 = "";
			String oldErrorLine2 = "";
			String oldErrorLine3 = "";
			String oldErrorLine4 = "";
			String oldErrorLine5 = "";

			if ( tiles.equalsIgnoreCase( "yes" ) ) {
				// Valider Status (Kacheln sind erlaubt)
			} else {
				try {
					BufferedReader in = new BufferedReader(
							new FileReader( exiftoolReport ) );
					String line;
					while ( (line = in.readLine()) != null ) {
						/*
						 * zu analysierende TIFF-IFD-Zeile die StripOffsets-
						 * oder TileOffsets-Zeile gibt Auskunft ueber die
						 * Aufteilungsart
						 * 
						 * -StripByteCounts -RowsPerStrip -FileSize -TileWidth
						 * -TileLength -TileDepth
						 */
						if ( (line.contains( "StripByteCounts: " )
								&& line.contains( "[EXIF:IFD" ))
								|| (line.contains( "RowsPerStrip: " )
										&& line.contains( "[EXIF:IFD" ))
								|| (line.contains( "Tile" )
										&& line.contains( "[EXIF:IFD" )) ) {
							exiftoolio = 1;
							if ( line.contains( "Tile" ) ) {
								// Invalider Status (Kacheln sind nicht erlaubt)
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
												getTextResourceService()
														.getText( locale,
																MESSAGE_XML_MODUL_G_TIFF )
														+ getTextResourceService()
																.getText(
																		locale,
																		MESSAGE_XML_CG_INVALID,
																		line ) );
										if ( oldErrorLine1.equals( "" ) ) {
											oldErrorLine1 = line;
										} else if ( oldErrorLine2
												.equals( "" ) ) {
											oldErrorLine2 = line;
										} else if ( oldErrorLine3
												.equals( "" ) ) {
											oldErrorLine3 = line;
										} else if ( oldErrorLine4
												.equals( "" ) ) {
											oldErrorLine4 = line;
										} else if ( oldErrorLine5
												.equals( "" ) ) {
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
									.getText( locale, MESSAGE_XML_MODUL_G_TIFF )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_CG_ETNIO, "G" ) );
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
								.getText( locale, MESSAGE_XML_MODUL_G_TIFF )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
						return false;
					}
				}
			}
			return isValid;
		}
	}
}