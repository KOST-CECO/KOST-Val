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
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationMlobException;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationWwarningException;
import ch.kostceco.tools.kostval.logging.Logtxt;
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
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationMlobModule;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationWwarningModule;

/**
 * kostval -->
 * 
 * Der Controller ruft die benoetigten Module zur Validierung der SIARD-Datei in
 * der benoetigten Reihenfolge auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllersiard implements MessageConstants
{

	private boolean								min	= false;

	private TextResourceService					textResourceService;

	private ValidationAzipModule				validationAzipModule;
	private ValidationBprimaryStructureModule	validationBprimaryStructureModule;
	private ValidationCheaderModule				validationCheaderModule;
	private ValidationDstructureModule			validationDstructureModule;
	private ValidationEcolumnModule				validationEcolumnModule;
	private ValidationFrowModule				validationFrowModule;
	private ValidationGtableModule				validationGtableModule;
	private ValidationHcontentModule			validationHcontentModule;
	private ValidationIrecognitionModule		validationIrecognitionModule;
	private ValidationJsurplusFilesModule		validationJsurplusFilesModule;
	private ValidationMlobModule		validationMlobModule;
	private ValidationWwarningModule			validationWwarningModule;

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

	public ValidationMlobModule getValidationMlobModule()
	{
		return validationMlobModule;
	}

	public void setValidationMlobModule(
			ValidationMlobModule validationMlobModule )
	{
		this.validationMlobModule = validationMlobModule;
	}

	public ValidationWwarningModule getValidationWwarningModule()
	{
		return validationWwarningModule;
	}

	public void setValidationWwarningModule(
			ValidationWwarningModule validationWwarningModule )
	{
		this.validationWwarningModule = validationWwarningModule;
	}

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService(
			TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean executeMandatory( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath )
	{
		boolean valid = true;

		// Validation Step A (Lesbarkeit)
		try {
			if ( this.getValidationAzipModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationAzipModule().getMessageService().print();
			} else {
				// Ein negatives Validierungsresultat in diesem Schritt fuehrt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidationAzipModule().getMessageService().print();
				return false;
			}
		} catch ( ValidationAzipException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationAzipModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step B (primaere Verzeichnisstruktur)
		try {
			if ( this.getValidationBprimaryStructureModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationBprimaryStructureModule().getMessageService()
						.print();
			} else {
				// Ein negatives Validierungsresultat in diesem Schritt fuehrt
				// zum Abbruch der weiteren Verarbeitung
				this.getValidationBprimaryStructureModule().getMessageService()
						.print();
				return false;
			}
		} catch ( ValidationBprimaryStructureException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_B_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationBprimaryStructureModule().getMessageService()
					.print();
			return false;
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_B_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step C (Header-Validierung)
		try {
			if ( this.getValidationCheaderModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationCheaderModule().getMessageService().print();
			} else {
				this.getValidationCheaderModule().getMessageService().print();
				// Ein negatives Validierungsresultat in diesem Schritt fuehrt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( ValidationCheaderException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_C_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationCheaderModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_C_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step D (Struktur-Validierung)
		try {
			if ( this.getValidationDstructureModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationDstructureModule().getMessageService()
						.print();
			} else {
				this.getValidationDstructureModule().getMessageService()
						.print();
				// Ein negatives Validierungsresultat in diesem Schritt fuehrt
				// zum Abbruch der weiteren Verarbeitung
				return false;
			}
		} catch ( ValidationDstructureException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_D_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationDstructureModule().getMessageService().print();
			return false;
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_D_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;
	}

	public boolean executeOptional( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath )
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		boolean valid = true;

		// Validation Step E (Spalten-Validierung)
		try {
			if ( this.getValidationEcolumnModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationEcolumnModule().getMessageService().print();
			} else {
				this.getValidationEcolumnModule().getMessageService().print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationEcolumnException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		try {
			if ( this.getValidationFrowModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationFrowModule().getMessageService().print();
			} else {
				this.getValidationFrowModule().getMessageService().print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationFrowException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_F_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationFrowModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_F_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step G (Tabellen-Validierung)
		try {
			if ( this.getValidationGtableModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationGtableModule().getMessageService().print();
			} else {
				this.getValidationGtableModule().getMessageService().print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationGtableException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_G_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationGtableModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_G_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step H (Content-Validierung)
		try {
			if ( this.getValidationHcontentModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationHcontentModule().getMessageService().print();
			} else {
				this.getValidationHcontentModule().getMessageService().print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationHcontentException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_H_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationHcontentModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_H_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step I (SIARD-Erkennung)
		try {
			if ( this.getValidationIrecognitionModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationIrecognitionModule().getMessageService()
						.print();
			} else {
				this.getValidationIrecognitionModule().getMessageService()
						.print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationIrecognitionException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_I_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationIrecognitionModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_I_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step J (Zusaetzliche Primaerdateien)
		try {
			if ( this.getValidationJsurplusFilesModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationJsurplusFilesModule().getMessageService()
						.print();
			} else {
				this.getValidationJsurplusFilesModule().getMessageService()
						.print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationJsurplusFilesException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationJsurplusFilesModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation Step M (LOB)
		try {
			if ( this.getValidationMlobModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationMlobModule().getMessageService()
						.print();
			} else {
				this.getValidationMlobModule().getMessageService()
						.print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationMlobException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_M_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationMlobModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_M_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Validation W (Warnungen)
		try {
			if ( this.getValidationWwarningModule().validate( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath ) ) {
				this.getValidationWwarningModule().getMessageService().print();
			} else {
				this.getValidationWwarningModule().getMessageService().print();
				if ( min ) {
					return false;
				} else {
					valid = false;
				}
			}
		} catch ( ValidationWwarningException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_W_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			this.getValidationWwarningModule().getMessageService().print();
			if ( min ) {
				return false;
			} else {
				valid = false;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_W_SIARD )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
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

	/** @return the validationFrowModule */
	public ValidationFrowModule getValidationFrowModule()
	{
		return validationFrowModule;
	}

	/**
	 * @param validationFrowModule
	 *            the validationFrowModule to set
	 */
	public void setValidationFrowModule(
			ValidationFrowModule validationFrowModule )
	{
		this.validationFrowModule = validationFrowModule;
	}
}