/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.File;

import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAinitialisationModule;

/**
 * Initialisierung PDF-Tools
 * 
 * Kontrolle ob ein Schlüssel vorhanden ist (Vollversion) wenn nicht, wird die
 * eingeschränkte Version verwendet
 * 
 * Da der Schlüssel nur für KOST-Val verwendet werden darf, wird er nicht
 * publiziert
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class ValidationAinitialisationModuleImpl extends ValidationModuleImpl
		implements ValidationAinitialisationModule
{

	private ConfigurationService	configurationService;

	public static String			NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationApdfvalidationException
	{
		boolean isValid = false;
		@SuppressWarnings("unused")
		boolean dual = false;

		// Initialisierung PDFTron -> überprüfen der Angaben: existiert die
		// PdftronExe am angebenen Ort?
		String producerFirstValidator = getConfigurationService()
				.firstValidator();
		String dualValidation = getConfigurationService().dualValidation();

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		try {
			// PDFTools: Check license
			if ( !PdfValidatorAPI.getLicenseIsValid() ) {
				// Keine Vollversion vorhanden -> Interne
				// eigeschränke Version verwenden
				
				/**
				 * Da der Schlüssel nur für KOST-Val verwendet werden darf, wird
				 * er nicht publiziert --> 000-YOUR-OWN-OEM-LICENCEKEY-000 mit
				 * dem OEM-Lizenzschlüssel ersetzten!
				 * 
				 * Because the key is only allowed to be used for KOST-Val, he
				 * is not published -> replaced 000-YOUR-OWN-OEM-LICENCEKEY-000
				 * with the your OEM license key!
				 **/

				String strLicenseKey = "000-YOUR-OWN-OEM-LICENCEKEY-000";
				PdfValidatorAPI.setLicenseKey( strLicenseKey );
			}
			if ( !PdfValidatorAPI.getLicenseIsValid() ) {
				if ( dualValidation.contentEquals( "dual" )
						|| !producerFirstValidator.contentEquals( "PDFTron" ) ) {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText(
											ERROR_XML_A_PDFTOOLS_LICENSE ) );
					return false;
				}
			}
			// System.out.println(PdfValidatorAPI.VERSION);
			isValid = true;
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}
}
