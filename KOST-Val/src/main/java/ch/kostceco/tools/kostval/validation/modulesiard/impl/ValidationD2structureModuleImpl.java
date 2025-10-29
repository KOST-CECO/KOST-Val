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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationDstructureException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationDstructureModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * wenn die Validierung der Module A-D durch die eigenen Module bestanden haben,
 * wird eine Validierung mithilfe von DBPTK durchgefuehrt, sofern es sich um
 * eine SIARD 2.1 oder 2.2 (SIARD 2) handelt.
 * 
 * Die einzelen Fehler werden dann den Modulen zugeordnet. Sollte seitens DBPTK
 * eines der Moodule A-D nicht bestanden haben, wird dieser Fehler ausgegeben
 * und abgebrochen.
 * 
 * Ansonsten werden die allfaelligen zugeordneten Fehler (E-M) gesichert und
 * beim jeweiligen Modul ausgegeben.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationD2structureModuleImpl extends ValidationModuleImpl implements ValidationDstructureModule {

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationDstructureException {
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		if (onWorkConfig.equals("yes")) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("D    ");
			System.out.print("\b\b\b\b\b");
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
		}

		// 4.1 - (A) - Construction of the SIARD archive file
		// 4.2 - (B) - Structure of the SIARD archive file
		// 4.3 - (E) - Correspondence between metadata and table data

		// 5.0 - (F) - Requirements for metadata
		// 5.1 - (G) - Database level metadata
		// 5.2 - (G) - Schema level metadata
		// 5.3 - (G) - Type level metadata
		// 5.4 - (G) - Attribute level metadata
		// 5.5 - (H) - Table level metadata
		// 5.6 - (H) - Column level metadata
		// 5.7 - (H) - Field level metadata
		// 5.8 - (K) - Primary Key level metadata
		// 5.9 - (K) - Foreign Key level metadata
		// 5.10 - (K) - Reference level metadata
		// 5.11 - (K) - Candidate Key level metadata
		// 5.12 - (K) - Check constraint level metadata
		// 5.13 - (L) - Trigger level metadata
		// 5.14 - (L) - View level metadata
		// 5.15 - (L) - Routine level metadata
		// 5.16 - (L) - Parameter level metadata
		// 5.17 - (L) - User level metadata
		// 5.18 - (L) - Role level metadata
		// 5.19 - (L) - Privilege level metadata

		// 6.0 - (H) - Requirements for table data
		// 6.1 - (H) - Table schema definition
		// 6.2 - (M) - Large object data cells
		// 6.3 - (H) - Date and timestamp data cells
		// 6.4 - (H) - Table data

		boolean valid = true;
		try {
			String pathToWorkDir = configMap.get("PathToWorkDir");
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File metadataXml = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("header")
					.append(File.separator).append("metadata.xml").toString());

			/*
			 * read the document and for each schema and table entry verify existence in
			 * temporary extracted structure
			 */
			Boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			Boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			if (version1) {
				// keine Validierung mit dbptk
			} else if (version2) {
				// TODO Validierung mit dbptk

				/*
				 * Usage: dbptk validate [OPTIONS]
				 * 
				 * 
				 * -if, --import-file=value (required) Path to SIARD2 archive file.
				 * 
				 * 
				 * -r, --report=value (optional) Path to save the validation report. If not set
				 * a report will be generated in the installation folder.
				 * 
				 */

				/*
				 * Falls report existiert wird als erstes die aufgefangene Konsole ausgewertet:
				 * 
				 * Number of requirements passed [27] Number of sub-requirements ok [84] Number
				 * of requirements failed [0] Number of errors [0] Number of warnings [51]
				 * Number of requirements skipped [14] Validation process finished the SIARD is
				 * valid. A report was generated with a listing of information about the
				 * individual validations.
				 * 
				 * wenn Number of errors [0] und
				 * "Validation process finished the SIARD is valid." existiert dann valid
				 * seitens dbptk, dh keine temporaeren Fehlermodule werden angelegt und der
				 * report wird geloescht.
				 * 
				 * falls invalid wird der Report als erstes bereinigt.
				 * 
				 * jede Zeile, welche nicht [ERROR] enthaelt wird geloescht
				 * 
				 * Die noch vorhanden Zeilen weiter analysieren:
				 * 
				 * Wenn G_4.1 gefunden wird hat das Modul A nicht bestanden seitens dbptk. der
				 * Fehler (die Treffer-Zeile) werden ausgegeben und Abgebrochen
				 * 
				 * Wenn P_4.2 gefunden wird hat das Modul B nicht bestanden seitens dbptk. der
				 * Fehler (die Treffer-Zeile) werden ausgegeben und Abgebrochen
				 * 
				 * Sollte kein Fehler in 4.1 und 4.2 vorligen werden die Zeilen den Modulen
				 * zugeordnet und Pro Moodul eine temporaeren Fehlermodule angelegt welche dann
				 * beim Jeweiligen Modul herangezogen wird und dannach geloescht wird.
				 * 
				 * T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years
				 * 0001-9999 according to the SQL:2008 specification. - Error on
				 * content/schema0/table13/table13.xsd restriction not enforced
				 * 
				 */

				// 4.1 - (A) - Construction of the SIARD archive file
				// 4.2 - (B) - Structure of the SIARD archive file
				// 4.3 - (E) - Correspondence between metadata and table data

				// 5.0 - (F) - Requirements for metadata
				// 5.1 - (G) - Database level metadata
				// 5.2 - (G) - Schema level metadata
				// 5.3 - (G) - Type level metadata
				// 5.4 - (G) - Attribute level metadata
				// 5.5 - (H) - Table level metadata
				// 5.6 - (H) - Column level metadata
				// 5.7 - (H) - Field level metadata
				// 5.8 - (K) - Primary Key level metadata
				// 5.9 - (K) - Foreign Key level metadata
				// 5.10 - (K) - Reference level metadata
				// 5.11 - (K) - Candidate Key level metadata
				// 5.12 - (K) - Check constraint level metadata
				// 5.13 - (L) - Trigger level metadata
				// 5.14 - (L) - View level metadata
				// 5.15 - (L) - Routine level metadata
				// 5.16 - (L) - Parameter level metadata
				// 5.17 - (L) - User level metadata
				// 5.18 - (L) - Role level metadata
				// 5.19 - (L) - Privilege level metadata

				// 6.0 - (H) - Requirements for table data
				// 6.1 - (H) - Table schema definition
				// 6.2 - (M) - Large object data cells
				// 6.3 - (H) - Date and timestamp data cells
				// 6.4 - (H) - Table data

			} else {
				// keine Validierung mit dbptk
			}
		} catch (java.io.IOException ioe) {
			valid = false;
			if (min) {
				return false;
			} else {

				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_SIARD) + getTextResourceService()
								.getText(locale, ERROR_XML_UNKNOWN, ioe.getMessage() + " (IOException)"));
			}
		}
		return valid;
	}
}
