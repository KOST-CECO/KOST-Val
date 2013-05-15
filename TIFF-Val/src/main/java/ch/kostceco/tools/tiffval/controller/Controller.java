/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
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

import ch.kostceco.tools.tiffval.exception.module1.Validation1aZipException;
import ch.kostceco.tools.tiffval.exception.module1.Validation1bFolderStructureException;
import ch.kostceco.tools.tiffval.exception.module1.Validation1cNamingException;
import ch.kostceco.tools.tiffval.exception.module1.Validation1dMetadataException;
import ch.kostceco.tools.tiffval.exception.module3.Validation3aFormatRecognitionException;
import ch.kostceco.tools.tiffval.exception.module3.Validation3bUnspecifiedFormatException;
import ch.kostceco.tools.tiffval.exception.module3.Validation3cFormatValidationException;
import ch.kostceco.tools.tiffval.logging.Logger;
import ch.kostceco.tools.tiffval.logging.MessageConstants;
import ch.kostceco.tools.tiffval.service.TextResourceService;
import ch.kostceco.tools.tiffval.validation.module1.Validation1aZipModule;
import ch.kostceco.tools.tiffval.validation.module1.Validation1bFolderStructureModule;
import ch.kostceco.tools.tiffval.validation.module1.Validation1cNamingModule;
import ch.kostceco.tools.tiffval.validation.module1.Validation1dMetadataModule;
import ch.kostceco.tools.tiffval.validation.module3.Validation3aFormatRecognitionModule;
import ch.kostceco.tools.tiffval.validation.module3.Validation3bUnspecifiedFormatModule;
import ch.kostceco.tools.tiffval.validation.module3.Validation3cFormatValidationModule;

/**
 * Der Controller ruft die benötigten Module zur Validierung des SIP-Archivs in
 * der benötigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 * 
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public class Controller implements MessageConstants
{

	private static final Logger						LOGGER	= new Logger(
																	Controller.class );

	private Validation1aZipModule					validation1aZipModule;

	private Validation1bFolderStructureModule		validation1bFolderStructureModule;

	private Validation1cNamingModule				validation1cNamingModule;

	private Validation1dMetadataModule				validation1dMetadataModule;

	private Validation3aFormatRecognitionModule		validation3aFormatRecognitionModule;

	private Validation3bUnspecifiedFormatModule		validation3bUnspecifiedFormatModule;

	private Validation3cFormatValidationModule		validation3cFormatValidationModule;

	

	private TextResourceService						textResourceService;

	public Validation1aZipModule getValidation1aZipModule()
	{
		return validation1aZipModule;
	}

	public void setValidation1aZipModule(
			Validation1aZipModule validation1aZipModule )
	{
		this.validation1aZipModule = validation1aZipModule;
	}

	public Validation1bFolderStructureModule getValidation1bFolderStructureModule()
	{
		return validation1bFolderStructureModule;
	}

	public void setValidation1bFolderStructureModule(
			Validation1bFolderStructureModule validation1bFolderStructureModule )
	{
		this.validation1bFolderStructureModule = validation1bFolderStructureModule;
	}

	public Validation1cNamingModule getValidation1cNamingModule()
	{
		return validation1cNamingModule;
	}

	public void setValidation1cNamingModule(
			Validation1cNamingModule validation1cNamingModule )
	{
		this.validation1cNamingModule = validation1cNamingModule;
	}

	public Validation1dMetadataModule getValidation1dMetadataModule()
	{
		return validation1dMetadataModule;
	}

	public void setValidation1dMetadataModule(
			Validation1dMetadataModule validation1dMetadataModule )
	{
		this.validation1dMetadataModule = validation1dMetadataModule;
	}

	public Validation3aFormatRecognitionModule getValidation3aFormatRecognitionModule()
	{
		return validation3aFormatRecognitionModule;
	}

	public void setValidation3aFormatRecognitionModule(
			Validation3aFormatRecognitionModule validation3aFormatRecognitionModule )
	{
		this.validation3aFormatRecognitionModule = validation3aFormatRecognitionModule;
	}

	public Validation3bUnspecifiedFormatModule getValidation3bUnspecifiedFormatModule()
	{
		return validation3bUnspecifiedFormatModule;
	}

	public void setValidation3bUnspecifiedFormatModule(
			Validation3bUnspecifiedFormatModule validation3bUnspecifiedFormatModule )
	{
		this.validation3bUnspecifiedFormatModule = validation3bUnspecifiedFormatModule;
	}

	public Validation3cFormatValidationModule getValidation3cFormatValidationModule()
	{
		return validation3cFormatValidationModule;
	}

	public void setValidation3cFormatValidationModule(
			Validation3cFormatValidationModule validation3cFormatValidationModule )
	{
		this.validation3cFormatValidationModule = validation3cFormatValidationModule;
	}


	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File sipDatei )
	{
		boolean valid = true;

		// Validation Step Aa
		try {
			if ( this.getValidation1aZipModule().validate( sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Aa ) ) );
				this.getValidation1aZipModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Aa ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Aa ) );
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidation1aZipModule().getMessageService().print();
				return false;
			}

		} catch ( Validation1aZipException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Aa ),
					e.getMessage() ) );
			this.getValidation1aZipModule().getMessageService().print();
			return false;

		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step Ab
		try {
			if ( this.getValidation1bFolderStructureModule()
					.validate( sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ab ) ) );
				this.getValidation1bFolderStructureModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ab ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Ab ) );
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidation1bFolderStructureModule().getMessageService()
						.print();
				return false;
			}
		} catch ( Validation1bFolderStructureException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Ab ),
					e.getMessage() ) );
			this.getValidation1bFolderStructureModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step Ac
		try {
			if ( this.getValidation1cNamingModule().validate( sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ac ) ) );
				this.getValidation1cNamingModule().getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ac ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Ac ) );
				this.getValidation1cNamingModule().getMessageService().print();
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( Validation1cNamingException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Ac ),
					e.getMessage() ) );
			this.getValidation1cNamingModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step Ad
		try {
			if ( this.getValidation1dMetadataModule().validate( sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ad ) ) );
				this.getValidation1dMetadataModule().getMessageService()
						.print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ad ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Ad ) );
				this.getValidation1dMetadataModule().getMessageService()
						.print();
				// Ein negatives Validierungsresultat in diesem Schritt führt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( Validation1dMetadataException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Ad ),
					e.getMessage() ) );
			this.getValidation1dMetadataModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		return valid;

	}

	public boolean executeOptional( File sipDatei )
	{
		boolean valid = true;




		// Validation Step Ca
		try {
			if ( this.getValidation3aFormatRecognitionModule().validate(
					sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ca ) ) );
				this.getValidation3aFormatRecognitionModule()
						.getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Ca ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Ca ) );
				this.getValidation3aFormatRecognitionModule()
						.getMessageService().print();
				valid = false;
			}
		} catch ( Validation3aFormatRecognitionException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Ca ),
					e.getMessage() ) );
			this.getValidation3aFormatRecognitionModule().getMessageService()
					.print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		// Validation Step Cb
		try {
			if ( this.getValidation3bUnspecifiedFormatModule().validate(
					sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Cb ) ) );
				this.getValidation3bUnspecifiedFormatModule()
						.getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Cb ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Cb ) );
				valid = false;
				this.getValidation3bUnspecifiedFormatModule()
						.getMessageService().print();
			}
		} catch ( Validation3bUnspecifiedFormatException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Cb ),
					e.getMessage() ) );
			this.getValidation3bUnspecifiedFormatModule().getMessageService()
					.print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		return valid;

	}

	public boolean execute3c( File sipDatei )
	{
		boolean valid = true;

		// Validation Step 3c
		try {
			if ( this.getValidation3cFormatValidationModule().validate(
					sipDatei ) ) {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_VALID,
						getTextResourceService().getText( MESSAGE_MODULE_Cc ) ) );
				this.getValidation3cFormatValidationModule()
						.getMessageService().print();
			} else {
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_MODULE_INVALID,
						getTextResourceService().getText( MESSAGE_MODULE_Cc ) )
						+ getTextResourceService().getText(
								MESSAGE_STEPERGEBNIS_Cc ) );
				this.getValidation3cFormatValidationModule()
						.getMessageService().print();
				valid = false;
			}

		} catch ( Validation3cFormatValidationException e ) {
			LOGGER.logInfo( getTextResourceService().getText(
					MESSAGE_MODULE_INVALID_2ARGS,
					getTextResourceService().getText( MESSAGE_MODULE_Cc ),
					e.getMessage() ) );
			this.getValidation3cFormatValidationModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logInfo( getTextResourceService().getText( ERROR_UNKNOWN ) );
			LOGGER.logError( e.getMessage() );
			return false;
		}

		return valid;

	}


}
