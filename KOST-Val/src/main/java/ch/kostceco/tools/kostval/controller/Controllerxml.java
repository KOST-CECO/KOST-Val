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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.exception.modulexml.ValidationAxmlvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulexml.ValidationAvalidationXmlModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die beoetigten Module zur Validierung der XML-Datei in
 * der beoetigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllerxml implements MessageConstants
{

	private TextResourceService				textResourceService;

	private ValidationAvalidationXmlModule	validationAvalidationXmlModule;

	public ValidationAvalidationXmlModule getValidationAvalidationXmlModule()
	{
		return validationAvalidationXmlModule;
	}

	public void setValidationAvalidationXmlModule(
			ValidationAvalidationXmlModule validationAvalidationXmlModule )
	{
		this.validationAvalidationXmlModule = validationAvalidationXmlModule;
	}

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService(
			TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
	{
		boolean valid = true;

		// Validation A
		try {
			if ( this.getValidationAvalidationXmlModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile ) ) {
				this.getValidationAvalidationXmlModule().getMessageService()
						.print();
			} else {
				this.getValidationAvalidationXmlModule().getMessageService()
						.print();
				return false;
			}
		} catch ( ValidationAxmlvalidationException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_XML )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationAvalidationXmlModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_XML )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;

	}
}
