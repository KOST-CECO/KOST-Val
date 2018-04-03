/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
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
import java.io.FileReader;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationDphotointerValidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationDphotointerValidationModule;

/** Validierungsschritt D (Farbraum-Validierung) Ist die TIFF-Datei gemäss Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationDphotointerValidationModuleImpl extends ValidationModuleImpl implements
		ValidationDphotointerValidationModule
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
			throws ValidationDphotointerValidationException
	{

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File( pathToExiftoolOutput, valDatei.getName() + ".exiftool-log.txt" );
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		String pi0 = getConfigurationService().getAllowedPhotointer0();
		String pi1 = getConfigurationService().getAllowedPhotointer1();
		String pi2 = getConfigurationService().getAllowedPhotointer2();
		String pi3 = getConfigurationService().getAllowedPhotointer3();
		String pi4 = getConfigurationService().getAllowedPhotointer4();
		String pi5 = getConfigurationService().getAllowedPhotointer5();
		String pi6 = getConfigurationService().getAllowedPhotointer6();
		String pi8 = getConfigurationService().getAllowedPhotointer8();

		if ( pi0.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi0 );
			return false;
		}
		if ( pi1.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi1 );
			return false;
		}
		if ( pi2.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi2 );
			return false;
		}
		if ( pi3.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi3 );
			return false;
		}
		if ( pi4.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi4 );
			return false;
		}
		if ( pi5.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi5 );
			return false;
		}
		if ( pi6.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi6 );
			return false;
		}
		if ( pi8.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF ) + pi8 );
			return false;
		}

		Integer exiftoolio = 0;
		String oldErrorLine1 = "";
		String oldErrorLine2 = "";
		String oldErrorLine3 = "";
		String oldErrorLine4 = "";
		String oldErrorLine5 = "";

		try {
			BufferedReader in = new BufferedReader( new FileReader( exiftoolReport ) );
			String line;
			while ( (line = in.readLine()) != null ) {
				// die PhotometricInterpretation-Zeile enthält einer dieser Freitexte der Farbraumart
				if ( line.contains( "PhotometricInterpretation: " ) ) {
					exiftoolio = 1;
					if ( line.equalsIgnoreCase( "PhotometricInterpretation: " + pi0 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi1 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi2 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi3 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi4 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi5 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi6 )
							|| line.equalsIgnoreCase( "PhotometricInterpretation: " + pi8 ) ) {
						// Valider Status
					} else {
						// Invalider Status
						isValid = false;
						if ( !line.equals( oldErrorLine1 ) && !line.equals( oldErrorLine2 )
								&& !line.equals( oldErrorLine3 ) && !line.equals( oldErrorLine4 )
								&& !line.equals( oldErrorLine5 ) ) {
							// neuer Fehler
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
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
					/* die PlanarConfiguration-Zeile muss Chunkey sein, da ansonsten nicht Baseline. Planar
					 * wird kaum unterstützt */
					if ( line.contains( "PlanarConfiguration: " ) ) {
						exiftoolio = 1;
						if ( line.contains( "Chunky" ) ) {
							// Valider Status
						} else {
							// Invalider Status
							isValid = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
						}
					}
				}
			}
			if ( exiftoolio == 0 ) {
				// Invalider Status
				isValid = false;
				isValid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_CG_ETNIO, "D" ) );
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
							+ getTextResourceService().getText( MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
			return false;
		}
		return isValid;
	}
}