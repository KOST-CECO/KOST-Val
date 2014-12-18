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

// import ch.kostceco.tools.kostval.exception.modulesip1.Validation1aZipException;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1bFolderStructureException;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1cNamingException;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1eSipTypeException;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1fPrimaryDataException;
import ch.kostceco.tools.kostval.exception.modulesip2.Validation2aFileIntegrityException;
// import ch.kostceco.tools.kostval.exception.modulesip2.Validation2bSurplusFilesException;
import ch.kostceco.tools.kostval.exception.modulesip2.Validation2cChecksumException;
import ch.kostceco.tools.kostval.exception.modulesip2.Validation2dGeverFileIntegrityException;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3aFormatRecognitionException;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3bUnspecifiedFormatException;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3cFormatValidationException;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3dPeriodException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
// import ch.kostceco.tools.kostval.validation.modulesip1.Validation1aZipModule;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1bFolderStructureModule;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1cNamingModule;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1eSipTypeModule;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1fPrimaryDataModule;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2aFileIntegrityModule;
// import ch.kostceco.tools.kostval.validation.modulesip2.Validation2bSurplusFilesModule;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2cChecksumModule;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2dGeverFileIntegrityModule;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3aFormatRecognitionModule;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3bUnspecifiedFormatModule;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3cFormatValidationModule;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3dPeriodModule;

/** Der Controller ruft die benötigten Module zur Validierung des SIP-Archivs in der benötigten
 * Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllersip implements MessageConstants
{

	private static final Logger										LOGGER	= new Logger( Controllersip.class );

	private Validation1bFolderStructureModule			validation1bFolderStructureModule;

	private Validation1cNamingModule							validation1cNamingModule;

	private Validation1dMetadataModule						validation1dMetadataModule;

	private Validation1eSipTypeModule							validation1eSipTypeModule;

	private Validation1fPrimaryDataModule					validation1fPrimaryDataModule;

	private Validation2aFileIntegrityModule				validation2aFileIntegrityModule;

	private Validation2cChecksumModule						validation2cChecksumModule;

	private Validation2dGeverFileIntegrityModule	validation2dGeverFileIntegrityModule;

	private Validation3aFormatRecognitionModule		validation3aFormatRecognitionModule;

	private Validation3bUnspecifiedFormatModule		validation3bUnspecifiedFormatModule;

	private Validation3cFormatValidationModule		validation3cFormatValidationModule;

	private Validation3dPeriodModule							validation3dPeriodModule;

	private TextResourceService										textResourceService;

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

	public void setValidation1cNamingModule( Validation1cNamingModule validation1cNamingModule )
	{
		this.validation1cNamingModule = validation1cNamingModule;
	}

	public Validation1dMetadataModule getValidation1dMetadataModule()
	{
		return validation1dMetadataModule;
	}

	public void setValidation1dMetadataModule( Validation1dMetadataModule validation1dMetadataModule )
	{
		this.validation1dMetadataModule = validation1dMetadataModule;
	}

	public Validation1eSipTypeModule getValidation1eSipTypeModule()
	{
		return validation1eSipTypeModule;
	}

	public void setValidation1eSipTypeModule( Validation1eSipTypeModule validation1eSipTypeModule )
	{
		this.validation1eSipTypeModule = validation1eSipTypeModule;
	}

	public Validation1fPrimaryDataModule getValidation1fPrimaryDataModule()
	{
		return validation1fPrimaryDataModule;
	}

	public void setValidation1fPrimaryDataModule(
			Validation1fPrimaryDataModule validation1fPrimaryDataModule )
	{
		this.validation1fPrimaryDataModule = validation1fPrimaryDataModule;
	}

	public Validation2aFileIntegrityModule getValidation2aFileIntegrityModule()
	{
		return validation2aFileIntegrityModule;
	}

	public void setValidation2aFileIntegrityModule(
			Validation2aFileIntegrityModule validation2aFileIntegrityModule )
	{
		this.validation2aFileIntegrityModule = validation2aFileIntegrityModule;
	}

	public Validation2cChecksumModule getValidation2cChecksumModule()
	{
		return validation2cChecksumModule;
	}

	public void setValidation2cChecksumModule( Validation2cChecksumModule validation2cChecksumModule )
	{
		this.validation2cChecksumModule = validation2cChecksumModule;
	}

	public Validation2dGeverFileIntegrityModule getValidation2dGeverFileIntegrityModule()
	{
		return validation2dGeverFileIntegrityModule;
	}

	public void setValidation2dGeverFileIntegrityModule(
			Validation2dGeverFileIntegrityModule validation2dGeverFileIntegrityModule )
	{
		this.validation2dGeverFileIntegrityModule = validation2dGeverFileIntegrityModule;
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

	public Validation3dPeriodModule getValidation3dPeriodModule()
	{
		return validation3dPeriodModule;
	}

	public void setValidation3dPeriodModule( Validation3dPeriodModule validation3dPeriodModule )
	{
		this.validation3dPeriodModule = validation3dPeriodModule;
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

		// Validation Step Aa (wurde vor der Formatvalidierung in KOSTVal.jar bereits durchgeführt)

		// Validation Step Ab
		try {
			if ( this.getValidation1bFolderStructureModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation1bFolderStructureModule().getMessageService().print();
			} else {
				// Ein negatives Validierungsresultat in diesem Schritt führt zum Abbruch der weiteren
				// Verarbeitung
				this.getValidation1bFolderStructureModule().getMessageService().print();
				return false;
			}
		} catch ( Validation1bFolderStructureException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation1bFolderStructureModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Ac
		try {
			if ( this.getValidation1cNamingModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation1cNamingModule().getMessageService().print();
			} else {
				this.getValidation1cNamingModule().getMessageService().print();
				// Ein negatives Validierungsresultat in diesem Schritt führt zum Abbruch der weiteren
				// Verarbeitung
				return false;
			}
		} catch ( Validation1cNamingException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation1cNamingModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Ad
		try {
			if ( this.getValidation1dMetadataModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation1dMetadataModule().getMessageService().print();
			} else {
				this.getValidation1dMetadataModule().getMessageService().print();
				// Ein negatives Validierungsresultat in diesem Schritt führt zum Abbruch der weiteren
				// Verarbeitung
				return false;
			}
		} catch ( Validation1dMetadataException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation1dMetadataModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

	public boolean executeOptional( File valDatei, File directoryOfLogfile )
	{
		boolean valid = true;
		// Validation Step Ae
		try {
			if ( this.getValidation1eSipTypeModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation1eSipTypeModule().getMessageService().print();
			} else {
				this.getValidation1eSipTypeModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation1eSipTypeException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ae_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation1eSipTypeModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ae_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Af
		try {
			if ( this.getValidation1fPrimaryDataModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation1fPrimaryDataModule().getMessageService().print();
			} else {
				this.getValidation1fPrimaryDataModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation1fPrimaryDataException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Af_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation1fPrimaryDataModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Af_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Ba
		try {
			if ( this.getValidation2aFileIntegrityModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation2aFileIntegrityModule().getMessageService().print();
			} else {
				this.getValidation2aFileIntegrityModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation2aFileIntegrityException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ba_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation2aFileIntegrityModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ba_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Bb (wurde zusammen mit Ba (2a) durchgeführt)

		// Validation Step Bc
		try {
			if ( this.getValidation2cChecksumModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation2cChecksumModule().getMessageService().print();
			} else {
				this.getValidation2cChecksumModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation2cChecksumException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Bc_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation2cChecksumModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Bc_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Bd
		try {
			if ( this.getValidation2dGeverFileIntegrityModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation2dGeverFileIntegrityModule().getMessageService().print();
			} else {
				this.getValidation2dGeverFileIntegrityModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation2dGeverFileIntegrityException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Bd_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation2dGeverFileIntegrityModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Bd_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Ca
		try {
			if ( this.getValidation3aFormatRecognitionModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation3aFormatRecognitionModule().getMessageService().print();
			} else {
				this.getValidation3aFormatRecognitionModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation3aFormatRecognitionException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation3aFormatRecognitionModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step Cb
		try {
			if ( this.getValidation3bUnspecifiedFormatModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation3bUnspecifiedFormatModule().getMessageService().print();
			} else {
				valid = false;
				this.getValidation3bUnspecifiedFormatModule().getMessageService().print();
			}
		} catch ( Validation3bUnspecifiedFormatException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cb_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation3bUnspecifiedFormatModule().getMessageService().print();
			valid = false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cb_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step 3c
		try {
			if ( this.getValidation3cFormatValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation3cFormatValidationModule().getMessageService().print();
			} else {
				this.getValidation3cFormatValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation3cFormatValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cc_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation3cFormatValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cc_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step 3d
		try {
			if ( this.getValidation3dPeriodModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidation3dPeriodModule().getMessageService().print();
			} else {
				this.getValidation3dPeriodModule().getMessageService().print();
				valid = false;
			}
		} catch ( Validation3dPeriodException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidation3dPeriodModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;
	}
}
