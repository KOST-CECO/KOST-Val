/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

import ch.kostceco.tools.kostval.exception.modulejpeg.ValidationAjpegvalidationException;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulejpeg.ValidationAvalidationJpegModule;

/** kostval -->
 * 
 * Der Controller ruft die beoetigten Module zur Validierung der JPEG-Datei in der beoetigten
 * Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllerjpeg implements MessageConstants
{

	private static final Logger							LOGGER	= new Logger( Controllerjpeg.class );
	private TextResourceService							textResourceService;

	private ValidationAvalidationJpegModule	validationAvalidationJpegModule;

	public ValidationAvalidationJpegModule getValidationAvalidationJpegModule()
	{
		return validationAvalidationJpegModule;
	}

	public void setValidationAvalidationJpegModule(
			ValidationAvalidationJpegModule validationAvalidationJpegModule )
	{
		this.validationAvalidationJpegModule = validationAvalidationJpegModule;
	}

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale )
	{
		boolean valid = true;

		// Validation A
		try {
			if ( this.getValidationAvalidationJpegModule().validate( valDatei, directoryOfLogfile,
					configMap, locale ) ) {
				this.getValidationAvalidationJpegModule().getMessageService().print();
			} else {
				this.getValidationAvalidationJpegModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationAjpegvalidationException e ) {
			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JPEG )
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationAvalidationJpegModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JPEG )
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;

	}
}
