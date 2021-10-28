/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationHsizeValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationHsizeValidationModule;

/** Validierungsschritt H (Groessen-Validierung) Ist die TIFF-Datei gemäss Konfigurationsdatei
 * valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationHsizeValidationModuleImpl extends ValidationModuleImpl
		implements ValidationHsizeValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean				min			= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationHsizeValidationException
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
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_TIFF )
							+ getTextResourceService().getText( locale, MESSAGE_XML_MISSING_REPORT,
									exiftoolReport.getAbsolutePath(),
									getTextResourceService().getText( locale, ABORTED ) ) );
			return false;
		} else {
			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */

			String size = configMap.get( "AllowedSize" );

			Integer exiftoolio = 0;

			if ( size.equalsIgnoreCase( "yes" ) ) {
				// Valider Status (Giga-Tiffs sind erlaubt)
			} else {
				// Giga-Tiffs sind nicht erlaubt -> analysieren
				try {
					BufferedReader in = new BufferedReader( new FileReader( exiftoolReport ) );
					String line;
					while ( (line = in.readLine()) != null ) {
						if ( line.contains( "[File:System] FileSize: " ) ) {
							// System.out.print( line + " " );
							exiftoolio = 1;
							Integer intSize = line.toCharArray().length;
							if ( line.contains( "byte" ) || line.contains( "kB" ) ) {
								// Valider Status (kleines TIFF)
							} else if ( line.contains( "MB" ) ) {
								if ( line.contains( "." ) ) {
									// Valider Status <=10.0 MB
								} else if ( intSize > 30 ) {
									/* Invalider Status (Giga-Tiffs sind nicht erlaubt und zuviele Stellen und keine
									 * Kommastelle) */
									isValid = false;
									if ( min ) {
										in.close();
										/* exiftoolReport loeschen */
										if ( exiftoolReport.exists() ) {
											exiftoolReport.delete();
										}
										return false;
									} else {
										getMessageService().logError(
												getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_TIFF )
														+ getTextResourceService().getText( locale, MESSAGE_XML_CG_INVALID,
																line ) );
									}
								}
							} else {
								// Invalider Status (unbekannte Grösse)
								isValid = false;
								if ( min ) {
									in.close();
									/* exiftoolReport loeschen */
									if ( exiftoolReport.exists() ) {
										exiftoolReport.delete();
									}
									return false;
								} else {
									getMessageService().logError( getTextResourceService().getText( locale,
											MESSAGE_XML_MODUL_H_TIFF )
											+ getTextResourceService().getText( locale, MESSAGE_XML_CG_INVALID, line ) );
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
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_TIFF )
											+ getTextResourceService().getText( locale, MESSAGE_XML_CG_ETNIO, "H" ) );
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
						getMessageService().logError( getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_H_TIFF )
								+ getTextResourceService().getText( locale, MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
						/* exiftoolReport loeschen */
						if ( exiftoolReport.exists() ) {
							exiftoolReport.delete();
						}
						return false;
					}
				}
			}
			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			File newReport = new File( pathToWorkDir, valDatei.getName() + ".jhove-log.txt" );
			if ( newReport.exists() ) {
				Util.deleteFile( newReport );
			}
			/* exiftoolReport löschen */
			if ( exiftoolReport.exists() ) {
				exiftoolReport.delete();
			}
			if ( exiftoolReport.exists() ) {
				Util.deleteFile( exiftoolReport );
			}
			return isValid;
		}
	}
}