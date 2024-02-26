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
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt H (Groessen-Validierung) Ist die TIFF-Datei gemäss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationHsizeValidationModuleImpl extends ValidationModuleImpl
		implements ValidationHsizeValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationHsizeValidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		boolean isValid = true;

		/*
		 * TODO: jhoveReport auswerten! Auf Exiftool wird verzichtet. Exiftool
		 * verwendet Perl, welche seit einiger Zeit hohe nicht geloese
		 * Sicherheitsrisiken birgt. zudem koennen die Metadaten vermehrt
		 * komplett durch jhove ausgelesen werden. Jhove hat bereits einen der
		 * Probleme, welche das teilweise die Ausgabe der Metadaten verhindert
		 * behoben.
		 */
		File jhoveReport = new File( directoryOfLogfile,
				valDatei.getName() + ".jhove-log.txt" );

		if ( !jhoveReport.exists() ) {
			isValid = false;
			if ( min ) {
				return false;
			} else {
				Logtxt.logtxt( logFile, getTextResourceService()
						.getText( locale, MESSAGE_XML_MODUL_B_TIFF )
						+ getTextResourceService().getText( locale,
								ERROR_XML_UNKNOWN, "No Jhove report." ) );
				return false;
			}
		} else {
			String size = configMap.get( "AllowedSize" );

			Integer jhoveio = 0;

			if ( size.equalsIgnoreCase( "yes" ) ) {
				// Valider Status (Giga-Tiffs sind erlaubt)
			} else {
				// Giga-Tiffs sind nicht erlaubt -> analysieren
				try {
					BufferedReader in = new BufferedReader(
							new FileReader( jhoveReport ) );
					String line;
					while ( (line = in.readLine()) != null ) {
						if ( line.contains( " Size: " ) ) {
							jhoveio = 1;
							Integer intSize = line.toCharArray().length;
							if ( size.contains( "1" ) ) {
								// Valider Status (Giga-Tiffs sind erlaubt)
							} else if ( intSize > 17 ) {
								// Invalider Status (Giga-Tiffs sind nicht
								// erlaubt und
								// zuviele Stellen)
								isValid = false;
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_H_TIFF )
										+ getTextResourceService().getText(
												locale, MESSAGE_XML_CG_INVALID,
												line ) );
							}
						}
					}

					if ( jhoveio == 0 ) {
						// Invalider Status
						isValid = false;
						if ( min ) {
							in.close();
							return false;
						} else {

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_H_TIFF )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_CG_JHOVENIO, "H" ) );
						}
					}
					in.close();

				} catch ( Exception e ) {
					if ( min ) {
						return false;
					} else {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_H_TIFF )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
						return false;
					}
				}
			}
			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			File newReport = new File( pathToWorkDir,
					valDatei.getName() + ".jhove-log.txt" );
			if ( newReport.exists() ) {
				Util.deleteFile( newReport );
			}
			return isValid;
		}
	}
}