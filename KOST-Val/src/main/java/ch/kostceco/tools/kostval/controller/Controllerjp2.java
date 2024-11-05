/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath,
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
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

import ch.kostceco.tools.kostval.exception.modulejp2.ValidationAjp2validationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulejp2.ValidationAvalidationAModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die benoetigten Module zur Validierung der JPEG2000-Datei
 * in der benoetigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllerjp2 implements MessageConstants {

	private TextResourceService textResourceService;

	private ValidationAvalidationAModule validationAvalidationAModule;

	public ValidationAvalidationAModule getValidationAvalidationAModule() {
		return validationAvalidationAModule;
	}

	public void setValidationAvalidationAModule(ValidationAvalidationAModule validationAvalidationAModule) {
		this.validationAvalidationAModule = validationAvalidationAModule;
	}

	public TextResourceService getTextResourceService() {
		return textResourceService;
	}

	public void setTextResourceService(TextResourceService textResourceService) {
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory(File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale, File logFile, String dirOfJarPath) {
		boolean valid = true;

		// Validation A
		try {
			if (this.getValidationAvalidationAModule().validate(valDatei, directoryOfLogfile, configMap, locale,
					logFile, dirOfJarPath)) {
				this.getValidationAvalidationAModule().getMessageService().print();
			} else {
				this.getValidationAvalidationAModule().getMessageService().print();
				return false;
			}
		} catch (ValidationAjp2validationException e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_JP2)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			this.getValidationAvalidationAModule().getMessageService().print();
			return false;
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_JP2)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return valid;

	}
}
