/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, and PDF/A-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
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

package ch.kostceco.tools.kostval.controller;

import java.io.File;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdftronException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationBstructureException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationCgraphicsException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationDfontsException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationEtransparencyException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationFannotationsException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationGactionsException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationHmetadataException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationIaccessibleException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationJinteractiveformsException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationApdftronModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationBstructureModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationCgraphicsModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationDfontsModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationEtransparencyModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationFannotationsModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationGactionsModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationHmetadataModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationIaccessibleModule;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationJinteractiveformsModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die benötigten Module zur Validierung der SIARD-Datei in
 * der benötigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllerpdfa implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger(
																Controllerpdfa.class );
	private TextResourceService					textResourceService;

	private ValidationApdftronModule			validationApdftronModule;
	private ValidationBstructureModule			validationBstructureModule;
	private ValidationCgraphicsModule			validationCgraphicsModule;
	private ValidationDfontsModule				validationDfontsModule;
	private ValidationEtransparencyModule		validationEtransparencyModule;
	private ValidationFannotationsModule		validationFannotationsModule;
	private ValidationGactionsModule			validationGactionsModule;
	private ValidationHmetadataModule			validationHmetadataModule;
	private ValidationIaccessibleModule			validationIaccessibleModule;
	private ValidationJinteractiveformsModule	validationJinteractiveformsModule;

	public ValidationApdftronModule getValidationApdftronModule()
	{
		return validationApdftronModule;
	}

	public void setValidationApdftronModule(
			ValidationApdftronModule validationApdftronModule )
	{
		this.validationApdftronModule = validationApdftronModule;
	}

	public ValidationBstructureModule getValidationBstructureModule()
	{
		return validationBstructureModule;
	}

	public void setValidationBstructureModule(
			ValidationBstructureModule validationBstructureModule )
	{
		this.validationBstructureModule = validationBstructureModule;
	}

	public ValidationCgraphicsModule getValidationCgraphicsModule()
	{
		return validationCgraphicsModule;
	}

	public void setValidationCgraphicsModule(
			ValidationCgraphicsModule validationCgraphicsModule )
	{
		this.validationCgraphicsModule = validationCgraphicsModule;
	}

	public ValidationDfontsModule getValidationDfontsModule()
	{
		return validationDfontsModule;
	}

	public void setValidationDfontsModule(
			ValidationDfontsModule validationDfontsModule )
	{
		this.validationDfontsModule = validationDfontsModule;
	}

	public ValidationEtransparencyModule getValidationEtransparencyModule()
	{
		return validationEtransparencyModule;
	}

	public void setValidationEtransparencyModule(
			ValidationEtransparencyModule validationEtransparencyModule )
	{
		this.validationEtransparencyModule = validationEtransparencyModule;
	}

	public ValidationFannotationsModule getValidationFannotationsModule()
	{
		return validationFannotationsModule;
	}

	public void setValidationFannotationsModule(
			ValidationFannotationsModule validationFannotationsModule )
	{
		this.validationFannotationsModule = validationFannotationsModule;
	}

	public ValidationGactionsModule getValidationGactionsModule()
	{
		return validationGactionsModule;
	}

	public void setValidationGactionsModule(
			ValidationGactionsModule validationGactionsModule )
	{
		this.validationGactionsModule = validationGactionsModule;
	}

	public ValidationHmetadataModule getValidationHmetadataModule()
	{
		return validationHmetadataModule;
	}

	public void setValidationHmetadataModule(
			ValidationHmetadataModule validationHmetadataModule )
	{
		this.validationHmetadataModule = validationHmetadataModule;
	}

	public ValidationIaccessibleModule getValidationIaccessibleModule()
	{
		return validationIaccessibleModule;
	}

	public void setValidationIaccessibleModule(
			ValidationIaccessibleModule validationIaccessibleModule )
	{
		this.validationIaccessibleModule = validationIaccessibleModule;
	}

	public ValidationJinteractiveformsModule getValidationJinteractiveformsModule()
	{
		return validationJinteractiveformsModule;
	}

	public void setValidationJinteractiveformsModule(
			ValidationJinteractiveformsModule validationJinteractiveformsModule )
	{
		this.validationJinteractiveformsModule = validationJinteractiveformsModule;
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

		// Validation A PDFTRON
		try {
			if ( this.getValidationApdftronModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) ) );
				this.getValidationApdftronModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_A_PDFA ) );
				this.getValidationApdftronModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationApdftronException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_A ),
					e.getMessage() ) );
			this.getValidationApdftronModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}
		return valid;

	}

	public boolean executeOptional( File valDatei, File directoryOfLogfile )
	{
		boolean valid = true;

		// Validation B
		try {
			if ( this.getValidationBstructureModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) ) );
				this.getValidationBstructureModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_B_PDFA ) );
				this.getValidationBstructureModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationBstructureException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_B ),
					e.getMessage() ) );
			this.getValidationBstructureModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation C
		try {
			if ( this.getValidationCgraphicsModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) ) );
				this.getValidationCgraphicsModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_C_PDFA ) );
				this.getValidationCgraphicsModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationCgraphicsException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_C ),
					e.getMessage() ) );
			this.getValidationCgraphicsModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation D
		try {
			if ( this.getValidationDfontsModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_D ) ) );
				this.getValidationDfontsModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_D ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_D_PDFA ) );
				this.getValidationDfontsModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationDfontsException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_D ),
					e.getMessage() ) );
			this.getValidationDfontsModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation E
		try {
			if ( this.getValidationEtransparencyModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_E ) ) );
				this.getValidationEtransparencyModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_E ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_E_PDFA ) );
				this.getValidationEtransparencyModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationEtransparencyException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_E ),
					e.getMessage() ) );
			this.getValidationEtransparencyModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation F
		try {
			if ( this.getValidationFannotationsModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_F ) ) );
				this.getValidationFannotationsModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_F ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_F_PDFA ) );
				this.getValidationFannotationsModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationFannotationsException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_F ),
					e.getMessage() ) );
			this.getValidationFannotationsModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation G
		try {
			if ( this.getValidationGactionsModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_G ) ) );
				this.getValidationGactionsModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_G ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_G_PDFA ) );
				this.getValidationGactionsModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationGactionsException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_G ),
					e.getMessage() ) );
			this.getValidationGactionsModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation H
		try {
			if ( this.getValidationHmetadataModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_H ) ) );
				this.getValidationHmetadataModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_H ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_H_PDFA ) );
				this.getValidationHmetadataModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationHmetadataException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_H ),
					e.getMessage() ) );
			this.getValidationHmetadataModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation I
		try {
			if ( this.getValidationIaccessibleModule().validate( valDatei,
					directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_I ) ) );
				this.getValidationIaccessibleModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_I ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_I_PDFA ) );
				this.getValidationIaccessibleModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationIaccessibleException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_I ),
					e.getMessage() ) );
			this.getValidationIaccessibleModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		// Validation J
		try {
			if ( this.getValidationJinteractiveformsModule().validate(
					valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_J ) ) );
				this.getValidationJinteractiveformsModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_J ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_J_PDFA ) );
				this.getValidationJinteractiveformsModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationJinteractiveformsException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_J ),
					e.getMessage() ) );
			this.getValidationJinteractiveformsModule().getMessageService()
					.print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			valid = false;
		}

		return valid;
	}
}
