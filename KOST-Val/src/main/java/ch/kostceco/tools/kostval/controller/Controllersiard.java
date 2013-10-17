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

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationAzipException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationBprimaryStructureException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationCheaderException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationDstructureException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationEcolumnException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationFrowException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationGtableException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationHcontentException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationIrecognitionException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationJsurplusFilesException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationAzipModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationBprimaryStructureModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationCheaderModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationDstructureModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationEcolumnModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationFrowModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationGtableModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationHcontentModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationIrecognitionModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationJsurplusFilesModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die benötigten Module zur Validierung der SIARD-Datei in
 * der benötigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllersiard implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger(
																Controllersiard.class );
	private TextResourceService					textResourceService;

	private ValidationAzipModule				validationAzipModule;
	private ValidationBprimaryStructureModule	validationBprimaryStructureModule;
	private ValidationCheaderModule				validationCheaderModule;
	private ValidationDstructureModule			validationDstructureModule;
	private ValidationEcolumnModule				validationEcolumnModule;
	private ValidationFrowModule 				validationFrowModule;
	private ValidationGtableModule				validationGtableModule;
	private ValidationHcontentModule			validationHcontentModule;
	private ValidationIrecognitionModule		validationIrecognitionModule;
	private ValidationJsurplusFilesModule		validationJsurplusFilesModule;

	public ValidationAzipModule getValidationAzipModule()
	{
		return validationAzipModule;
	}

	public void setValidationAzipModule(
			ValidationAzipModule validationAzipModule )
	{
		this.validationAzipModule = validationAzipModule;
	}

	public ValidationBprimaryStructureModule getValidationBprimaryStructureModule()
	{
		return validationBprimaryStructureModule;
	}

	public void setValidationBprimaryStructureModule(
			ValidationBprimaryStructureModule validationBprimaryStructureModule )
	{
		this.validationBprimaryStructureModule = validationBprimaryStructureModule;
	}

	public ValidationCheaderModule getValidationCheaderModule()
	{
		return validationCheaderModule;
	}

	public void setValidationCheaderModule(
			ValidationCheaderModule validationCheaderModule )
	{
		this.validationCheaderModule = validationCheaderModule;
	}

	public ValidationDstructureModule getValidationDstructureModule()
	{
		return validationDstructureModule;
	}

	public void setValidationDstructureModule(
			ValidationDstructureModule validationDstructureModule )
	{
		this.validationDstructureModule = validationDstructureModule;
	}

	/*
	 * public ValidationEcolumnModule getValidationEcolumnModule() { return
	 * validationEcolumnModule; } public void
	 * setValidationEcolumnModule(ValidationEcolumnModule
	 * validationEcolumnModule) { this.validationEcolumnModule =
	 * validationEcolumnModule; }
	 */

	/*
	 * public ValidationFrowModule getValidationFrowModule() { return
	 * validationFrowModule; } public void
	 * setValidationFrowModule(ValidationFrowModule validationFrowModule) {
	 * this.validationFrowModule = validationFrowModule; }
	 */

	public ValidationGtableModule getValidationGtableModule()
	{
		return validationGtableModule;
	}

	public void setValidationGtableModule(
			ValidationGtableModule validationGtableModule )
	{
		this.validationGtableModule = validationGtableModule;
	}

	public ValidationHcontentModule getValidationHcontentModule()
	{
		return validationHcontentModule;
	}

	public void setValidationHcontentModule(
			ValidationHcontentModule validationHcontentModule )
	{
		this.validationHcontentModule = validationHcontentModule;
	}

	public ValidationIrecognitionModule getValidationIrecognitionModule()
	{
		return validationIrecognitionModule;
	}

	public void setValidationIrecognitionModule(
			ValidationIrecognitionModule validationIrecognitionModule )
	{
		this.validationIrecognitionModule = validationIrecognitionModule;
	}

	public ValidationJsurplusFilesModule getValidationJsurplusFilesModule()
	{
		return validationJsurplusFilesModule;
	}

	public void setValidationJsurplusFilesModule(
			ValidationJsurplusFilesModule validationJsurplusFilesModule )
	{
		this.validationJsurplusFilesModule = validationJsurplusFilesModule;
	}

	/*
	 * public ValidationKchecksumModule getValidationKchecksumModule() { return
	 * validationKchecksumModule; } public void
	 * setValidationKchecksumModule(ValidationKchecksumModule
	 * validationKchecksumModule) { this.validationKchecksumModule =
	 * validationKchecksumModule; }
	 */

	/*
	 * public ValidationLconstraintModule getValidationLconstraintModule() {
	 * return validationLconstraintModule; } public void
	 * setValidationLconstraintModule(ValidationLconstraintModule
	 * validationLconstraintModule) { this.validationLconstraintModule =
	 * validationLconstraintModule; }
	 */

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

		// Validation Step A (Lesbarkeit)
		try {
			if ( this.getValidationAzipModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) ) );
				this.getValidationAzipModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_A ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_A_SIARD ) );
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidationAzipModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationAzipException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_A ),
					e.getMessage() ) );
			this.getValidationAzipModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step B (primäre Verzeichnisstruktur)
		try {
			if ( this.getValidationBprimaryStructureModule().validate(
					valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) ) );
				this.getValidationBprimaryStructureModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_B ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_B_SIARD ) );
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidationBprimaryStructureModule().getMessageService()
						.print();
				return false;
			}
		} catch ( ValidationBprimaryStructureException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_B ),
					e.getMessage() ) );
			this.getValidationBprimaryStructureModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step C (Header-Validierung)
		try {
			if ( this.getValidationCheaderModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) ) );
				this.getValidationCheaderModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_C ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_C_SIARD ) );
				this.getValidationCheaderModule().getMessageService().print();
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( ValidationCheaderException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_C ),
					e.getMessage() ) );
			this.getValidationCheaderModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step D (Struktur-Validierung)
		try {
			if ( this.getValidationDstructureModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_D ) ) );
				this.getValidationDstructureModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_D ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_D_SIARD ) );
				this.getValidationDstructureModule().getMessageService()
						.print();
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( ValidationDstructureException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_D ),
					e.getMessage() ) );
			this.getValidationDstructureModule().getMessageService().print();
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

		// Validation Step E (Spalten-Validierung)
		try {
			if ( this.getValidationEcolumnModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_E ) ) );
				this.getValidationEcolumnModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_E ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_E_SIARD ) );
				this.getValidationEcolumnModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationEcolumnException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_E ),
					e.getMessage() ) );
			this.getValidationEcolumnModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}
		
		try {
			if ( this.getValidationFrowModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_F ) ) );
				this.getValidationFrowModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_F ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_F_SIARD ) );
				this.getValidationFrowModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationFrowException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_F ),
					e.getMessage() ) );
			this.getValidationFrowModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step G (Tabellen-Validierung)
		try {
			if ( this.getValidationGtableModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_G ) ) );
				this.getValidationGtableModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_G ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_G_SIARD ) );
				this.getValidationGtableModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationGtableException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_G ),
					e.getMessage() ) );
			this.getValidationGtableModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step H (Content-Validierung)
		try {
			if ( this.getValidationHcontentModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_H ) ) );
				this.getValidationHcontentModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_H ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_H_SIARD ) );
				this.getValidationHcontentModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationHcontentException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_H ),
					e.getMessage() ) );
			this.getValidationHcontentModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step I (SIARD-Erkennung)
		try {
			if ( this.getValidationIrecognitionModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_I ) ) );
				this.getValidationIrecognitionModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_I ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_I_SIARD ) );
				this.getValidationIrecognitionModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationIrecognitionException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_I ),
					e.getMessage() ) );
			this.getValidationIrecognitionModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step J (Zusätzliche Primärdateien)
		try {
			if ( this.getValidationJsurplusFilesModule().validate( valDatei, directoryOfLogfile ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_J ) ) );
				this.getValidationJsurplusFilesModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_J ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_J_SIARD ) );
				this.getValidationJsurplusFilesModule().getMessageService()
						.print();
				valid = false;
			}
		} catch ( ValidationJsurplusFilesException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_J ),
					e.getMessage() ) );
			this.getValidationJsurplusFilesModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		return valid;
	}

	public ValidationEcolumnModule getValidationEcolumnModule()
	{
		return validationEcolumnModule;
	}

	public void setValidationEcolumnModule(
			ValidationEcolumnModule validationEcolumnModule )
	{
		this.validationEcolumnModule = validationEcolumnModule;
	}

	/**
	 * @return the validationFrowModule
	 */
	public ValidationFrowModule getValidationFrowModule()
	{
		return validationFrowModule;
	}

	/**
	 * @param validationFrowModule the validationFrowModule to set
	 */
	public void setValidationFrowModule( ValidationFrowModule validationFrowModule )
	{
		this.validationFrowModule = validationFrowModule;
	}
}
