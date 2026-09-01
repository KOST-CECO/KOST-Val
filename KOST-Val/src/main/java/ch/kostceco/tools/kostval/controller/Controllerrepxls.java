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

import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kosttools.repair.xls;

/**
 * kostval -->
 * 
 * Der Controller ruft die benoetigten Module zur Reparatur der XLS-Datei in der
 * benoetigten Reihenfolge auf.
 * 
 * Keine Validierung nur Reparatur
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllerrepxls implements MessageConstants {

	private TextResourceService textResourceService;

	private xls xls;

	public xls getXls() {
		return xls;
	}

	public void setXls(xls xls) {
		this.xls = xls;
	}

	public TextResourceService getTextResourceService() {
		return textResourceService;
	}

	public void setTextResourceService(TextResourceService textResourceService) {
		this.textResourceService = textResourceService;
	}

	public boolean executeOptional(File xlsOldFile, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale, File logFile, String dirOfJarPath, File fileToOutputStart) {
		String pathToWorkDir = configMap.get("PathToWorkDir");
		File workDir = new File(pathToWorkDir);
		if (!workDir.exists()) {
			workDir.mkdir();
		}
		// System.out.println("executeOptional" );

		/*		String xlsOldPath = xlsOldFile.getAbsolutePath() + "?";
		String xlsxNewPath = "";
		if (xlsOldPath.contains(".xls?")) {
			xlsxNewPath = xlsOldPath.replace(".xls?", ".xlsx");
		} else if (xlsOldPath.contains(".XLS?")) {
			xlsxNewPath = xlsOldPath.replace(".XLS?", ".xlsx");
		} else {
			return false;
		}
		File xlsxNewFile = new File(xlsxNewPath);*/
		String xlsOldName = xlsOldFile.getName() + "?";
		String xlsxNewName = "";
		if (xlsOldName.contains(".xls?")) {
			xlsxNewName = xlsOldName.replace(".xls?", ".xlsx");
		} else if (xlsOldName.contains(".XLS?")) {
			xlsxNewName = xlsOldName.replace(".XLS?", ".xlsx");
		} else {
			return false;
		}

		File xlsxNewFile =new File(fileToOutputStart.getAbsolutePath()+File.separator+xlsxNewName);
		// Reparatur
		try {
			String repXls = xls.repairXlsXlsx(xlsOldFile, xlsxNewFile, dirOfJarPath, directoryOfLogfile, workDir);
			// System.out.println("repXls = "+repXls);
			if (repXls.equals("OK")) {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN) + getTextResourceService()
								.getText(locale, INFO_XML_Z_REP_OK, xlsxNewFile.getAbsolutePath(), "xls"));
				return true;
			} else if (repXls.equals("xlsxAllreadyExists")) {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN) + getTextResourceService()
								.getText(locale, INFO_XML_Z_NOREP_ALLREADYEXISTS, xlsxNewFile.getAbsolutePath()));
			} else if (repXls.equals("NoXlsx")) {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN)
						+ getTextResourceService().getText(locale, INFO_XML_Z_NOREP_NOOUTPUT));
			} else if (repXls.startsWith("exeFileMissing: ")) {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN) + getTextResourceService()
								.getText(locale, INFO_XML_Z_NOREP_NOREPEXE, repXls.replace("exeFileMissing: ", "")));
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN)
						+ getTextResourceService().getText(locale, INFO_XML_Z_NOREP_NOOUTPUT));
			}
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_GEN)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage() + " Rep"));
		}
		return false;
	}

}
