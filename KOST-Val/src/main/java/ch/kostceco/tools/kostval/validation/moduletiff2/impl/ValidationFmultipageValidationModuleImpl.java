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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.io.FileReader;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationFmultipageValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationFmultipageValidationModule;

/** Validierungsschritt F (Multipage-Validierung) Ist die TIFF-Datei gem�ss Konfigurationsdatei
 * valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationFmultipageValidationModuleImpl extends ValidationModuleImpl
		implements ValidationFmultipageValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean				min			= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationFmultipageValidationException
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

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		String mp = configMap.get( "AllowedMultipage" );
		if ( mp.startsWith( "Configuration-Error:" ) ) {
			if ( min ) {
				/* exiftoolReport loeschen */
				if ( exiftoolReport.exists() ) {
					exiftoolReport.delete();
				}
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_TIFF ) + mp );
				return false;
			}
		}
		// 0=Singelpage / 1=Multipage

		Integer exiftoolio = 0;
		Integer ifdCount = 0;
		String ifdMsg;
		if ( mp.equalsIgnoreCase( "yes" ) ) {
			// Valider Status (Multipage erlaubt)
		} else {
			try {
				BufferedReader in = new BufferedReader( new FileReader( exiftoolReport ) );
				String line;
				while ( (line = in.readLine()) != null ) {

					// Number und IFD: enthalten auch Exif Eintr�ge. Ensprechend muss "Type: TIFF" gez�hlt
					// werden
					if ( line.contains( "Compression: " ) && line.contains( "[EXIF:IFD" ) ) {
						exiftoolio = 1;
						ifdCount = ifdCount + 1;
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
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_TIFF )
										+ getTextResourceService().getText( locale, MESSAGE_XML_CG_ETNIO, "F" ) );
					}
				}
				if ( ifdCount == 1 ) {
					// Valider Status (nur eine Seite)
				} else {
					// Invalider Status
					ifdMsg = ("Multipage (" + ifdCount + " TIFFs)");
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
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_TIFF )
										+ getTextResourceService().getText( locale, MESSAGE_XML_CG_INVALID, ifdMsg ) );
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
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_F_TIFF )
									+ getTextResourceService().getText( locale, MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
					return false;
				}
			}
		}
		return isValid;
	}
}