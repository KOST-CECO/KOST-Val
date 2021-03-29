/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2021 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.controller;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptAZipException;
import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptAConfigException;
import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptBSearchException;
import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptCGrepException;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptAZipModule;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptAConfigModule;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptBSearchModule;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptCGrepModule;
import ch.kostceco.tools.siardexcerpt.logging.Logger;
import ch.kostceco.tools.siardexcerpt.logging.MessageConstants;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

/** Der Controller ruft die benoetigten Module zur Extraktion in der benoetigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllerexcerpt implements MessageConstants
{

	private static final Logger			LOGGER	= new Logger( Controllerexcerpt.class );

	private ExcerptAZipModule				excerptAZipModule;
	private ExcerptAConfigModule		excerptAConfigModule;

	private ExcerptBSearchModule		excerptBSearchModule;

	private ExcerptCGrepModule			excerptCGrepModule;

	private TextResourceServiceExc	textResourceServiceExc;

	public ExcerptAZipModule getExcerptAZipModule()
	{
		return excerptAZipModule;
	}

	public void setExcerptAZipModule( ExcerptAZipModule excerptAZipModule )
	{
		this.excerptAZipModule = excerptAZipModule;
	}

	public ExcerptAConfigModule getExcerptAConfigModule()
	{
		return excerptAConfigModule;
	}

	public void setExcerptAConfigModule( ExcerptAConfigModule excerptAConfigModule )
	{
		this.excerptAConfigModule = excerptAConfigModule;
	}

	public ExcerptBSearchModule getExcerptBSearchModule()
	{
		return excerptBSearchModule;
	}

	public void setExcerptBSearchModule( ExcerptBSearchModule excerptBSearchModule )
	{
		this.excerptBSearchModule = excerptBSearchModule;
	}

	public ExcerptCGrepModule getExcerptCGrepModule()
	{
		return excerptCGrepModule;
	}

	public void setExcerptCGrepModule( ExcerptCGrepModule excerptCGrepModule )
	{
		this.excerptCGrepModule = excerptCGrepModule;
	}

	public TextResourceServiceExc getTextResourceServiceExc()
	{
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc( TextResourceServiceExc textResourceServiceExc )
	{
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public boolean executeA( File siardDatei, File siardDateiNew, String noString,
			Map<String, String> configMap, Locale locale )
	{
		boolean valid = true;

		// TODO-Marker Excerpt Step A (SIARD-Datei ins Workverzeichnis extrahieren)

		try {
			if ( this.getExcerptAZipModule().validate( siardDatei, siardDateiNew, noString, configMap,
					locale ) ) {
				this.getExcerptAZipModule().getMessageServiceExc().print();
			} else {
				this.getExcerptAZipModule().getMessageServiceExc().print();
				valid = false;
			}
		} catch ( ExcerptAZipException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getExcerptAZipModule().getMessageServiceExc().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

	public boolean executeAConfig( File siardDatei, File configFileHard, String noString,
			Map<String, String> configMap, Locale locale )
	{
		boolean valid = true;

		// Excerpt Step A Config (Config Datei ausfuellen)

		try {
			if ( this.getExcerptAConfigModule().validate( siardDatei, configFileHard, noString, configMap,
					locale ) ) {
				this.getExcerptAConfigModule().getMessageServiceExc().print();
			} else {
				this.getExcerptAConfigModule().getMessageServiceExc().print();
				valid = false;
			}
		} catch ( ExcerptAConfigException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getExcerptAConfigModule().getMessageServiceExc().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

	public boolean executeB( File siardDatei, File outFileSearch, String searchString,
			Map<String, String> configMap, Locale locale )
	{
		boolean valid = true;
		// TODO-Marker Excerpt Step B (Suche)
		try {
			if ( this.getExcerptBSearchModule().validate( siardDatei, outFileSearch, searchString,
					configMap, locale ) ) {
				this.getExcerptBSearchModule().getMessageServiceExc().print();
			} else {
				this.getExcerptBSearchModule().getMessageServiceExc().print();
				valid = false;
			}
		} catch ( ExcerptBSearchException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getExcerptBSearchModule().getMessageServiceExc().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

	public boolean executeC( File siardDatei, File outFile, String excerptString,
			Map<String, String> configMap, Locale locale )
	{
		boolean valid = true;
		// TODO-Marker Excerpt Step C (Extraktion)
		try {
			if ( this.getExcerptCGrepModule().validate( siardDatei, outFile, excerptString, configMap,
					locale ) ) {
				this.getExcerptCGrepModule().getMessageServiceExc().print();
			} else {
				this.getExcerptCGrepModule().getMessageServiceExc().print();
				valid = false;
			}
		} catch ( ExcerptCGrepException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_C )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getExcerptCGrepModule().getMessageServiceExc().print();
			return false;
		} catch ( Exception e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_C )
					+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;
	}
}
