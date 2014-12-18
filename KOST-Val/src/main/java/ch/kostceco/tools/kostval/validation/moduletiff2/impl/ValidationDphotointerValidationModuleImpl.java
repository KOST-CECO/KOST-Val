/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
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

import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationDphotointerValidationModule;

/** Validierungsschritt D (Farbraum-Validierung) Ist die TIFF-Datei gemäss Konfigurationsdatei valid?
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

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
	{

		boolean isValid = true;

		// Informationen zum Jhove-Logverzeichnis holen
		String pathToJhoveOutput = directoryOfLogfile.getAbsolutePath();
		File jhoveReport = new File( pathToJhoveOutput, valDatei.getName() + ".jhove-log.txt" );

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

		Integer jhoveio = 0;
		Integer typetiff = 0;

		try {
			BufferedReader in = new BufferedReader( new FileReader( jhoveReport ) );
			String line;
			while ( (line = in.readLine()) != null ) {
				if ( line.contains( "Type: TIFF" ) ) {
					typetiff = 1;
					// TIFF-IFD
				} else if ( line.contains( "Type: Exif" ) ) {
					typetiff = 0;
					// Exif-IFD
				}
				if ( typetiff == 1 ) {
					// zu analysierende TIFF-IFD-Zeile

					// die ColorSpace-Zeile enthält einer dieser Freitexte der Farbraumart
					if ( line.contains( "ColorSpace:" ) ) {
						jhoveio = 1;
						if ( line.contains( pi0 ) || line.contains( pi1 ) || line.contains( pi2 )
								|| line.contains( pi3 ) || line.contains( pi4 ) || line.contains( pi5 )
								|| line.contains( pi6 ) || line.contains( pi8 ) ) {
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
			if ( jhoveio == 0 ) {
				// Invalider Status
				isValid = false;
				isValid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_CG_JHOVENIO, "D" ) );
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
							+ getTextResourceService().getText( MESSAGE_XML_CG_CANNOTFINDJHOVEREPORT ) );
			return false;
		}
		return isValid;
	}
}