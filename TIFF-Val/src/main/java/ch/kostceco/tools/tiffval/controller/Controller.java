/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Tagged Image File Format (TIFF).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.tiffval.controller;

import java.io.File;

import ch.kostceco.tools.tiffval.exception.module1.ValidationArecognitionException;
import ch.kostceco.tools.tiffval.exception.module2.ValidationBjhoveValidationException;
import ch.kostceco.tools.tiffval.logging.Logger;
import ch.kostceco.tools.tiffval.logging.MessageConstants;
import ch.kostceco.tools.tiffval.service.TextResourceService;
import ch.kostceco.tools.tiffval.validation.module1.ValidationArecognitionModule;
import ch.kostceco.tools.tiffval.validation.module2.ValidationBjhoveValidationModule;
import ch.kostceco.tools.tiffval.validation.module2.ValidationCcompressionValidationModule;

/**
 * Der Controller ruft die benötigten Module zur Validierung des TIFF-Archivs in
 * der benötigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public class Controller implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger(
																Controller.class );

	private ValidationArecognitionModule		validationArecognitionModule;
	private ValidationBjhoveValidationModule	validationBjhoveValidationModule;
	private ValidationCcompressionValidationModule	validationCcompressionValidationModule;
	
	private TextResourceService					textResourceService;

	public ValidationArecognitionModule getValidationArecognitionModule()
	{
		return validationArecognitionModule;
	}

	public void setValidationArecognitionModule(
			ValidationArecognitionModule validationArecognitionModule )
	{
		this.validationArecognitionModule = validationArecognitionModule;
	}

	public ValidationBjhoveValidationModule getValidationBjhoveValidationModule()
	{
		return validationBjhoveValidationModule;
	}

	public void setValidationBjhoveValidationModule(
			ValidationBjhoveValidationModule validationBjhoveValidationModule )
	{
		this.validationBjhoveValidationModule = validationBjhoveValidationModule;
	}
	
	public ValidationCcompressionValidationModule getValidationCcompressionValidationModule()
	{
		return validationCcompressionValidationModule;
	}

	public void setValidationCcompressionValidationModule(
			ValidationCcompressionValidationModule validationCcompressionValidationModule )
	{
		this.validationCcompressionValidationModule = validationCcompressionValidationModule;
	}


	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File tiffDatei )
	{
		boolean valid = true;

		// Validation Step A
		try {
			if ( this.getValidationArecognitionModule().validate( tiffDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) ) );
				this.getValidationArecognitionModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_A ) );
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidationArecognitionModule().getMessageService()
						.print();
				return false;
			}

		} catch ( ValidationArecognitionException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_A ),
					e.getMessage() ) );
			this.getValidationArecognitionModule().getMessageService().print();
			return false;

		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}
		return valid;

	}

	public boolean executeOptional( File tiffDatei )
	{
		boolean valid = true;
		// Validation Step B
		try {
			if ( this.getValidationBjhoveValidationModule()
					.validate( tiffDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) ) );
				this.getValidationBjhoveValidationModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_B ) );
				this.getValidationBjhoveValidationModule().getMessageService()
						.print();
				valid = false;
			}

		} catch ( ValidationBjhoveValidationException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_B ),
					e.getMessage() ) );
			this.getValidationBjhoveValidationModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}
		
		// Validation Step C
		try {
			if ( this.getValidationCcompressionValidationModule()
					.validate( tiffDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) ) );
				this.getValidationCcompressionValidationModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_C ) );
				this.getValidationBjhoveValidationModule().getMessageService()
						.print();
				valid = false;
			}

/*		} catch ( ValidationCcompressionValidationException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_C ),
					e.getMessage() ) );
			this.getValidationCcompressionValidationModule().getMessageService()
					.print();
			return false;*/
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}


		return valid;
	}
}
