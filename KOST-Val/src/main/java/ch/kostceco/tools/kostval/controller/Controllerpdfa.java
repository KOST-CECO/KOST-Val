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

package ch.kostceco.tools.kostval.controller;

import java.io.File;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAvalidationAiModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAinitialisationModule;

/** kostval -->
 * 
 * Der Controller ruft die benötigten Module zur Validierung der PDFA-Datei in der benötigten
 * Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllerpdfa implements MessageConstants
{

	private static final Logger							LOGGER	= new Logger( Controllerpdfa.class );
	private TextResourceService							textResourceService;

	private ValidationAvalidationAiModule		validationAvalidationAiModule;
	private ValidationAinitialisationModule	validationAinitialisationModule;

	public ValidationAvalidationAiModule getValidationAvalidationAiModule()
	{
		return validationAvalidationAiModule;
	}

	public void setValidationAvalidationAiModule(
			ValidationAvalidationAiModule validationAvalidationAiModule )
	{
		this.validationAvalidationAiModule = validationAvalidationAiModule;
	}

	public ValidationAinitialisationModule getValidationAinitialisationModule()
	{
		return validationAinitialisationModule;
	}

	public void setValidationAinitialisationModule(
			ValidationAinitialisationModule validationAinitialisationModule )
	{
		this.validationAinitialisationModule = validationAinitialisationModule;
	}

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File valDatei, File directoryOfLogfile )
	{
		boolean valid = true;

		// Initialisation PDF-Tools
		try {
			if ( this.getValidationAinitialisationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationAinitialisationModule().getMessageService().print();
			} else {
				this.getValidationAinitialisationModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationApdfvalidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationAinitialisationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation A - I
		try {
			if ( this.getValidationAvalidationAiModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationAvalidationAiModule().getMessageService().print();
			} else {
				this.getValidationAvalidationAiModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationApdfvalidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationAvalidationAiModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;

	}
}
