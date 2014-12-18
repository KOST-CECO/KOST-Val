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

import ch.kostceco.tools.kostval.exception.moduletiff1.ValidationArecognitionException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationBjhoveValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationCcompressionValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationDphotointerValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationEbitspersampleValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationFmultipageValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationGtilesValidationException;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationHsizeValidationException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
// import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.moduletiff1.ValidationArecognitionModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationBjhoveValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationCcompressionValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationDphotointerValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationEbitspersampleValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationFmultipageValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationGtilesValidationModule;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationHsizeValidationModule;

/** Der Controller ruft die benötigten Module zur Validierung des TIFF-Archivs in der benötigten
 * Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllertiff implements MessageConstants
{

	private static final Logger												LOGGER	= new Logger( Controllertiff.class );

	private ValidationArecognitionModule							validationArecognitionModule;
	private ValidationBjhoveValidationModule					validationBjhoveValidationModule;
	private ValidationCcompressionValidationModule		validationCcompressionValidationModule;
	private ValidationDphotointerValidationModule			validationDphotointerValidationModule;
	private ValidationEbitspersampleValidationModule	validationEbitspersampleValidationModule;
	private ValidationFmultipageValidationModule			validationFmultipageValidationModule;
	private ValidationGtilesValidationModule					validationGtilesValidationModule;
	private ValidationHsizeValidationModule						validationHsizeValidationModule;

	private TextResourceService												textResourceService;

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

	public ValidationDphotointerValidationModule getValidationDphotointerValidationModule()
	{
		return validationDphotointerValidationModule;
	}

	public void setValidationDphotointerValidationModule(
			ValidationDphotointerValidationModule validationDphotointerValidationModule )
	{
		this.validationDphotointerValidationModule = validationDphotointerValidationModule;
	}

	public ValidationEbitspersampleValidationModule getValidationEbitspersampleValidationModule()
	{
		return validationEbitspersampleValidationModule;
	}

	public void setValidationEbitspersampleValidationModule(
			ValidationEbitspersampleValidationModule validationEbitspersampleValidationModule )
	{
		this.validationEbitspersampleValidationModule = validationEbitspersampleValidationModule;
	}

	public ValidationFmultipageValidationModule getValidationFmultipageValidationModule()
	{
		return validationFmultipageValidationModule;
	}

	public void setValidationFmultipageValidationModule(
			ValidationFmultipageValidationModule validationFmultipageValidationModule )
	{
		this.validationFmultipageValidationModule = validationFmultipageValidationModule;
	}

	public ValidationGtilesValidationModule getValidationGtilesValidationModule()
	{
		return validationGtilesValidationModule;
	}

	public void setValidationGtilesValidationModule(
			ValidationGtilesValidationModule validationGtilesValidationModule )
	{
		this.validationGtilesValidationModule = validationGtilesValidationModule;
	}

	public ValidationHsizeValidationModule getValidationHsizeValidationModule()
	{
		return validationHsizeValidationModule;
	}

	public void setValidationHsizeValidationModule(
			ValidationHsizeValidationModule validationHsizeValidationModule )
	{
		this.validationHsizeValidationModule = validationHsizeValidationModule;
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
		// Validation Step A
		try {
			if ( this.getValidationArecognitionModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationArecognitionModule().getMessageService().print();
			} else {
				// Ein negatives Validierungsresultat in diesem Schritt führt zum Abbruch der weiteren
				// Verarbeitung
				this.getValidationArecognitionModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationArecognitionException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationArecognitionModule().getMessageService().print();
			return false;

		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;
	}

	public boolean executeOptional( File valDatei, File directoryOfLogfile )
	{
		boolean valid = true;
		// Validation Step B
		try {
			if ( this.getValidationBjhoveValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationBjhoveValidationModule().getMessageService().print();
			} else {
				this.getValidationBjhoveValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationBjhoveValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationBjhoveValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step C
		try {
			if ( this.getValidationCcompressionValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationCcompressionValidationModule().getMessageService().print();
			} else {
				this.getValidationCcompressionValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationCcompressionValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationCcompressionValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_C_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step D
		try {
			if ( this.getValidationDphotointerValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationDphotointerValidationModule().getMessageService().print();
			} else {
				this.getValidationDphotointerValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationDphotointerValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationDphotointerValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_D_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step E
		try {
			if ( this.getValidationEbitspersampleValidationModule().validate( valDatei,
					directoryOfLogfile ) ) {
				this.getValidationEbitspersampleValidationModule().getMessageService().print();
			} else {
				this.getValidationEbitspersampleValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationEbitspersampleValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationEbitspersampleValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step F
		try {
			if ( this.getValidationFmultipageValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationFmultipageValidationModule().getMessageService().print();
			} else {
				this.getValidationFmultipageValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationFmultipageValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_F_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationFmultipageValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_F_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step G
		try {
			if ( this.getValidationGtilesValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationGtilesValidationModule().getMessageService().print();
			} else {
				this.getValidationGtilesValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationGtilesValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_G_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationGtilesValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_G_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step H
		try {
			if ( this.getValidationHsizeValidationModule().validate( valDatei, directoryOfLogfile ) ) {
				this.getValidationHsizeValidationModule().getMessageService().print();
			} else {
				this.getValidationHsizeValidationModule().getMessageService().print();
				valid = false;
			}
		} catch ( ValidationHsizeValidationException e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_H_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationHsizeValidationModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_H_TIFF )
					+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;
	}
}
