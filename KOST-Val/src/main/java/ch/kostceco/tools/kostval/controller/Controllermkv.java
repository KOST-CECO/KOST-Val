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

import ch.kostceco.tools.kostval.exception.modulemkv.ValidationAmkvvalidationException;
import ch.kostceco.tools.kostval.exception.modulemkv.ValidationBHmkvvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulemkv.ValidationAvalidationMkvModule;
import ch.kostceco.tools.kostval.validation.modulemkv.ValidationBHvalidationMkvModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die beoetigten Module zur Erkennung und spaeter zur
 * Validierung der MKV-Datei in der beoetigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllermkv implements MessageConstants {

	private TextResourceService textResourceService;

	private ValidationAvalidationMkvModule validationAvalidationMkvModule;
	private ValidationBHvalidationMkvModule validationBHvalidationMkvModule;

	public ValidationAvalidationMkvModule getValidationAvalidationMkvModule() {
		return validationAvalidationMkvModule;
	}

	public void setValidationAvalidationMkvModule(ValidationAvalidationMkvModule validationAvalidationMkvModule) {
		this.validationAvalidationMkvModule = validationAvalidationMkvModule;
	}

	public ValidationBHvalidationMkvModule getValidationBHvalidationMkvModule() {
		return validationBHvalidationMkvModule;
	}

	public void setValidationBHvalidationMkvModule(ValidationBHvalidationMkvModule validationBHvalidationMkvModule) {
		this.validationBHvalidationMkvModule = validationBHvalidationMkvModule;
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
			if (this.getValidationAvalidationMkvModule().validate(valDatei, directoryOfLogfile, configMap, locale,
					logFile, dirOfJarPath)) {
				this.getValidationAvalidationMkvModule().getMessageService().print();
			} else {
				this.getValidationAvalidationMkvModule().getMessageService().print();
				return false;
			}
		} catch (ValidationAmkvvalidationException e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_MKV)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			this.getValidationAvalidationMkvModule().getMessageService().print();
			return false;
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_MKV)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return valid;

	}

	public boolean executeOptional(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) {

		boolean valid = true;
		// Validation Step B-H
		try {
			if (this.getValidationBHvalidationMkvModule().validate(valDatei, directoryOfLogfile, configMap, locale,
					logFile, dirOfJarPath)) {
				this.getValidationBHvalidationMkvModule().getMessageService().print();
			} else {
				this.getValidationBHvalidationMkvModule().getMessageService().print();
				valid = false;
			}
		} catch (ValidationBHmkvvalidationException e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_MKV)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			this.getValidationBHvalidationMkvModule().getMessageService().print();
			return false;
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_MKV)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}

		return valid;
	}
}
