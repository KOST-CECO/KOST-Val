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
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import ch.kostceco.tools.kosttools.fileservice.Dbptk;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationD2structureException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationD2structureModule;

/**
 * wenn die Validierung der Module A-D durch die eigenen Module bestanden haben,
 * wird eine Validierung mithilfe von DBPTK durchgefuehrt, sofern es sich um
 * eine SIARD 2.1 oder 2.2 (SIARD 2) handelt.
 * 
 * Die einzelen Fehler werden dann den Modulen zugeordnet und ausgegeben.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationD2structureModuleImpl extends ValidationModuleImpl implements ValidationD2structureModule {

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationD2structureException {
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
				 * -sac, --skip-additional-checks optional) Run the SIARD validation without the
				 * additional checks. The additional checks can be found at:
				 * 
				 * (valDatei, directoryOfLogfile, configMap, locale, logFile, dirOfJarPath)
				 * 
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
				 * zugeordnet und Pro Modul eine temporaeren Fehlermodule angelegt welche dann
				 * beim Jeweiligen Modul herangezogen wird und dannach geloescht wird.
				 * 
				 * T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years
				 * 0001-9999 according to the SQL:2008 specification. - Error on
				 * content/schema0/table13/table13.xsd restriction not enforced
				 * 
				 */

				File workDir = new File(pathToWorkDir);
				if (!workDir.exists()) {
					workDir.mkdir();
				}
				File outputDbptk = new File(directoryOfLogfile + File.separator + "dbptk.txt");
				File outputProcessDbptk = new File(directoryOfLogfile + File.separator + "dbptk_process_output.txt");

				// falls das File von einem vorhergehenden Durchlauf bereits
				// existiert, loeschen wir es
				if (outputDbptk.exists()) {
					outputDbptk.delete();
				}
				if (outputProcessDbptk.exists()) {
					outputProcessDbptk.delete();
				}

				// Die Erkennung erfolgt bereits im Vorfeld (Modul A)

				boolean isValid = true;

				// TODO: Start: Validierung mit dbptk

				// - Initialisierung dbptk -> existiert alles zu dbptk?

				// Pfad zum Programm existiert die Dateien?
				String checkTool = Dbptk.checkDbptk(dirOfJarPath);
				if (!checkTool.equals("OK")) {
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_SIARD)
										+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkTool,
												getTextResourceService().getText(locale, ABORTED)));
						return false;
					}
				} else {
					// validator sollte vorhanden sein
					try {
						String resultExec = Dbptk.execDbptk(valDatei, outputDbptk, outputProcessDbptk, workDir,
								dirOfJarPath);
						if (!resultExec.equals("OK") || !outputDbptk.exists()) {
							// Exception oder Report existiert nicht
							if (min) {
								return false;
							} else {
								isValid = false;
								// Erster Fehler! Meldung B ausgeben und invalid setzten
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEINVALID,
														"dbptk", resultExec));
							}
						} else {
							// Report existiert -> Auswerten...
							String appearsValid1 = "Number of errors [0]";
							String appearsValid2 = "Validation process finished the SIARD is valid.";
							String error = "[ERROR]";

							String errE43 = ""; // 4.3 - (E) - Correspondence between metadata and table data
							String errF50 = ""; // 5.0 - (F) - Requirements for metadata
							String errG51 = ""; // 5.1 - (G) - Database level metadata
							String errG52 = ""; // 5.2 - (G) - Schema level metadata
							String errG53 = ""; // 5.3 - (G) - Type level metadata
							String errG54 = ""; // 5.4 - (G) - Attribute level metadata
							String errH55 = ""; // 5.5 - (H) - Table level metadata
							String errH56 = ""; // 5.6 - (H) - Column level metadata
							String errH57 = ""; // 5.7 - (H) - Field level metadata
							String errH60 = ""; // 6.0 - (H) - Requirements for table data
							String errH61 = ""; // 6.1 - (H) - Table schema definition
							String errH63 = ""; // 6.3 - (H) - Date and timestamp data cells
							String errH64 = ""; // 6.4 - (H) - Table data
							String errK510 = ""; // 5.10 - (K) - Reference level metadata
							String errK511 = ""; // 5.11 - (K) - Candidate Key level metadata
							String errK512 = ""; // 5.12 - (K) - Check constraint level metadata
							String errK58 = ""; // 5.8 - (K) - Primary Key level metadata
							String errK59 = ""; // 5.9 - (K) - Foreign Key level metadata
							String errL513 = ""; // 5.13 - (L) - Trigger level metadata
							String errL514 = ""; // 5.14 - (L) - View level metadata
							String errL515 = ""; // 5.15 - (L) - Routine level metadata
							String errL516 = ""; // 5.16 - (L) - Parameter level metadata
							String errL517 = ""; // 5.17 - (L) - User level metadata
							String errL518 = ""; // 5.18 - (L) - Role level metadata
							String errL519 = ""; // 5.19 - (L) - Privilege level metadata
							String errM62 = ""; // 6.2 - (M) - Large object data cells

							String errorHeader = "";
							String errorDetail = "";
							String warning1 = "";
							String warning2 = "";

							Boolean valid1 = false;
							Boolean valid2 = false;

							Scanner scannerProcessOutput = new Scanner(outputProcessDbptk);
							while (scannerProcessOutput.hasNextLine()) {
								// format_name=matroska,webm
								String line = scannerProcessOutput.nextLine();
								// System.out.println(" ->> " + line);
								if (line.contains(appearsValid1)) {
									// Validierung mit dbptk evtl bestanden
									valid1 = true;
									if (valid1 && valid2) {
										/*
										 * Validierung mit dbptk bestanden
										 * 
										 * wenn Number of errors [0] und
										 * "Validation process finished the SIARD is valid." existiert dann valid
										 * seitens dbptk, dh keine temporaeren Fehlermodule werden angelegt und der
										 * report wird geloescht.
										 * 
										 */
										if (outputDbptk.exists()) {
											// report loeschen
											outputDbptk.delete();
										}
										if (outputProcessDbptk.exists()) {
											// report loeschen
											outputProcessDbptk.delete();
										}
										scannerProcessOutput.close();
										valid = true;
										isValid = true;
										return true;
									}
								} else if (line.contains(appearsValid2)) {
									// Validierung mit dbptk evtl bestanden
									valid2 = true;
									if (valid1 && valid2) {
										/*
										 * Validierung mit dbptk bestanden
										 * 
										 * wenn Number of errors [0] und
										 * "Validation process finished the SIARD is valid." existiert dann valid
										 * seitens dbptk, dh keine temporaeren Fehlermodule werden angelegt und der
										 * report wird geloescht.
										 * 
										 */
										if (outputDbptk.exists()) {
											// report loeschen
											outputDbptk.delete();
										}
										if (outputProcessDbptk.exists()) {
											// report loeschen
											outputProcessDbptk.delete();
										}
										scannerProcessOutput.close();
										valid = true;
										isValid = true;
										return true;
									}
								} else if (line.contains("ERROR ")) {
									/*
									 * ->> Start validation ->> ERROR Missing mandatory strings in the metadata.xml
									 * file (schemaName: , schemaFolder: schema0 ->> ERROR Schema name or schema
									 * folder attributes have a blank value. Please check the metadata.xml file for
									 * more information ->> Log files and migration reports were saved in
									 * C:\Users\clair\eclipse-Workspace\KOST-Tools\KOST-Val\resources\dbptk-app-4.0.
									 * 0 ->> Troubleshooting information can be found at
									 * https://github.com/keeps/dbptk-developer/wiki/Troubleshooting ->> Please
									 * report any problems at https://github.com/keeps/dbptk-developer/issues/new
									 */
									// 4.1 - (A) - Construction of the SIARD archive file
									errorDetail = "</Message><Message> - " + line + "";
									errorHeader = "DBPTK";
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK", errorDetail));
									isValid = false;
									valid = false;
								} else {
									// error aus scannerOutput lesen
									// dort ist alles auf einer Zeile
								}
							}
							scannerProcessOutput.close();

							Scanner scannerOutput = new Scanner(outputDbptk);
							while (scannerOutput.hasNextLine()) {
								if (showOnWork) {
									if (onWork == 410) {
										onWork = 2;
										System.out.print("D-   ");
										System.out.print("\b\b\b\b\b");
									} else if (onWork == 110) {
										onWork = onWork + 1;
										System.out.print("D\\   ");
										System.out.print("\b\b\b\b\b");
									} else if (onWork == 210) {
										onWork = onWork + 1;
										System.out.print("D|   ");
										System.out.print("\b\b\b\b\b");
									} else if (onWork == 310) {
										onWork = onWork + 1;
										System.out.print("D/   ");
										System.out.print("\b\b\b\b\b");
									} else {
										onWork = onWork + 1;
									}
								}

								String line = scannerOutput.nextLine();
								// System.out.println(" --> " + line);

								/*
								 * Linie bereinigen
								 * 
								 * jede Zeile, mit [ERROR] weiter analysieren:
								 * 
								 * Sollte kein Fehler in 4.1 und 4.2 vorligen werden die Zeilen den Modulen
								 * zugeordnet und Pro Modul eine temporaeren Fehlermodule angelegt welche dann
								 * beim Jeweiligen Modul herangezogen wird und dannach geloescht wird.
								 * 
								 * T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years
								 * 0001-9999 according to the SQL:2008 specification. - Error on
								 * content/schema0/table13/table13.xsd restriction not enforced
								 * 
								 */

								if (!line.contains(error)) {
									// Zeile ignorieren oder ggf Hinweis ausgeben

									// TODO Warnung ausgeben wenn besonderheit

									if (line.contains("_4.3-") && !line.contains("[SKIPPED]")) {
										// P_4.3-6: [SKIPPED] - No UDT type found
										if (!warning1.contains("UDT")) {
											warning1 = warning1 + " UDT";
										}
									} else if (line.contains("_5.13-") && !line.contains("[SKIPPED]")) {
										// M_5.13-1: [SKIPPED] - Database has no triggers
										if (!warning1.contains("trigger")) {
											warning1 = warning1 + " trigger";
										}
									} else if (line.contains("_5.14-") && !line.contains("[SKIPPED]")) {
										// M_5.14-1: [SKIPPED] - Database has no view
										if (!warning1.contains("view")) {
											warning1 = warning1 + " view";
										}
									} else if (line.contains("_5.15-") && !line.contains("[SKIPPED]")) {
										// M_5.15-1: [SKIPPED] - Database has no routine
										if (!warning1.contains("routine")) {
											warning1 = warning1 + " routine";
										}
									} else if (line.contains("_5.16-") && !line.contains("[SKIPPED]")) {
										// M_5.16-1: [SKIPPED] - Database has no parameters
										if (!warning1.contains("parameter")) {
											warning1 = warning1 + " parameter";
										}
									} else if (line.contains("_5.18-") && !line.contains("[SKIPPED]")) {
										// M_5.18-1: [SKIPPED] - Database has no roles
										if (!warning1.contains("role")) {
											warning1 = warning1 + " role";
										}
									} else if (line.contains("_5.19-") && !line.contains("[SKIPPED]")) {
										// M_5.19-1: [SKIPPED] - Database has no privileges
										if (!warning1.contains("privilege")) {
											warning1 = warning1 + " privilege";
										}
									}

									// warning1: UDT, trigger, view, routine, parameters, roles, privileges

									// val.message.xml.servicemessage.info =
									// <Message>{0}{1}</Message></Error><Warning>warning</Warning>

									// Hinweis: Datenbank enthaelt "{0}", welche teilweise Probleme beim
									// zurueckspielen in eine Datenbank verursachen koennen.

								} else {
									/*
									 * TODO zu ignorierende Fehlermeldungen
									 */

									String ignorP423 = "P_4.2-3: [ERROR] - The individual table folders contain an XML file and an XSD file, the names of";
									String ignorT631 = "T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years 0001-9999 according to the SQL:2008 specification";
									if (line.contains(ignorP423)) {
										/*
										 * P_4.2-3: [ERROR] - The individual table folders contain an XML file and an
										 * XSD file, the names of which (folder designation and both file names) must be
										 * identical. - content\schema0\table1\lob3\rec0.bin
										 * 
										 * Hier sehe ich keinen Fehler in der SIARD-Datei
										 * 
										 * Spez: The individual table folders contain an XML file and an XSD file, the
										 * names of which (folder designation and both file names) must be identical.
										 * With the exception of BLOB and CLOB folders together with their content (BIN,
										 * TXT, or XML files, or a file extension associated with the MIME type of the
										 * lob files in case this is known, e.g. JPG), no other folders or files are
										 * permitted.
										 */
									} else if (line.contains(ignorT631)) {
										/*
										 * T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years
										 * 0001-9999 according to the SQL:2008 specification. - Error on
										 * content/schema0/table13/table13.xsd restriction not enforced ==> Das steht
										 * zwar so in der Spez aber meines Wissens wollte sich der Text hier nur auf die
										 * Jahrzahlen beziehen und nicht nur Jahrzahlen erlauben ansonsten hätten wir
										 * dies so auch im xsd umgesetzt Spez: Dates and timestamps must be restricted
										 * to the years 0001-9999 according to the SQL:2008 specification. This
										 * restriction is enforced in the definitions of dateType and dateTimeType
										 * dateType Spez: <!-- date type between 0001 and 9999 restricted to UTC -->
										 * <xs:simpleType name="dateType"> <xs:restriction base="xs:date">
										 * <xs:minInclusive value="0001-01-01Z"/> <xs:maxExclusive
										 * value="10000-01-01Z"/> <xs:pattern value="\d{4}-\d{2}-\d{2}Z?"/>
										 * </xs:restriction> </xs:simpleType>
										 */
									} else {
										// NOK
										isValid = false;
										valid = false;
										String lineCase = line.toLowerCase();
										// TODO Error auslesen, einordnen und ausgeben

										// System.out.println("dbptk: " + line);
										/*
										 * T_6.3-1: [ERROR] - Dates and timestamps must be restricted to the years
										 * 0001-9999 according to the SQL:2008 specification. - Error on
										 * content/schema0/table13/table13.xsd restriction not enforced
										 */

										if (lineCase.contains("_4.1-")) {
											// 4.1 - (A) - Construction of the SIARD archive file
											errorDetail = "</Message><Message> - " + line + "";
											errorHeader = getTextResourceService().getText(locale,
													MESSAGE_XML_A_HEADER41);
											Logtxt.logtxt(logFile,
													getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_SIARD)
															+ getTextResourceService().getText(locale,
																	MESSAGE_XML_SERVICEINVALID, "DBPTK",
																	"\"" + errorHeader + "\"" + errorDetail));
											if (outputDbptk.exists()) {
												// report loeschen
												outputDbptk.delete();
											}
											if (outputProcessDbptk.exists()) {
												// report loeschen
												outputProcessDbptk.delete();
											}
											scannerOutput.close();
										} else if (lineCase.contains("_4.1-")) {
											// 4.2 - (B) - Structure of the SIARD archive file
											errorDetail = "</Message><Message> - " + line + "";
											errorHeader = getTextResourceService().getText(locale,
													MESSAGE_XML_B_HEADER42);
											Logtxt.logtxt(logFile,
													getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_SIARD)
															+ getTextResourceService().getText(locale,
																	MESSAGE_XML_SERVICEINVALID, "DBPTK",
																	"\"" + errorHeader + "\"" + errorDetail));
											if (outputDbptk.exists()) {
												// report loeschen
												outputDbptk.delete();
											}
											if (outputProcessDbptk.exists()) {
												// report loeschen
												outputProcessDbptk.delete();
											}
											scannerOutput.close();

											/*
											 * Sollte kein Fehler in 4.1 und 4.2 vorligen werden die Zeilen den Modulen
											 * zugeordnet und Pro Modul eine temporaeren Fehlermodule angelegt welche
											 * dann beim Jeweiligen Modul herangezogen wird und dannach geloescht wird.
											 */
										} else if (lineCase.contains("_4.3-")) {
											// errE43 = 4.3 - (E) - Correspondence between metadata and table data
											errE43 = errE43 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.0-")) {
											// errF50 = 5.0 - (F) - Requirements for metadata
											errF50 = errF50 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.1-")) {
											// errG51 = 5.1 - (G) - Database level metadata
											errG51 = errG51 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.2-")) {
											// errG52 = 5.2 - (G) - Schema level metadata
											errG52 = errG52 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.3-")) {
											// errG53 = 5.3 - (G) - Type level metadata
											errG53 = errG53 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.4-")) {
											// errG54 = 5.4 - (G) - Attribute level metadata
											errG54 = errG54 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.5-")) {
											// errH55 = 5.5 - (H) - Table level metadata
											errH55 = errH55 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.6-")) {
											// errH56 = 5.6 - (H) - Column level metadata
											errH56 = errH56 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.7-")) {
											// errH57 = 5.7 - (H) - Field level metadata
											errH57 = errH57 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_6.0-")) {
											// errH60 = 6.0 - (H) - Requirements for table data
											errH60 = errH60 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_6.1-")) {
											// errH61 = 6.1 - (H) - Table schema definition
											errH61 = errH61 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_6.3-")) {
											// errH63 = 6.3 - (H) - Date and timestamp data cells
											errH63 = errH63 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_6.4-")) {
											// errH64 = 6.4 - (H) - Table data
											errH64 = errH64 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.10-")) {
											// errK510 = 5.10 - (K) - Reference level metadata
											errK510 = errK510 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.11-")) {
											// errK511 = 5.11 - (K) - Candidate Key level metadata
											errK511 = errK511 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.12-")) {
											// errK512 = 5.12 - (K) - Check constraint level metadata
											errK512 = errK512 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.8-")) {
											// errK58 = 5.8 - (K) - Primary Key level metadata
											errK58 = errK58 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.9-")) {
											// errK59 = 5.9 - (K) - Foreign Key level metadata
											errK59 = errK59 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.13-")) {
											// errL513 = 5.13 - (L) - Trigger level metadata
											errL513 = errL513 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.14-")) {
											// errL514 = 5.14 - (L) - View level metadata
											errL514 = errL514 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.15-")) {
											// errL515 = 5.15 - (L) - Routine level metadata
											errL515 = errL515 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.16-")) {
											// errL516 = 5.16 - (L) - Parameter level metadata
											errL516 = errL516 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.17-")) {
											// errL517 = 5.17 - (L) - User level metadata
											errL517 = errL517 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.18-")) {
											// errL518 = 5.18 - (L) - Role level metadata
											errL518 = errL518 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_5.19-")) {
											// errL519 = 5.19 - (L) - Privilege level metadata
											errL519 = errL519 + "</Message><Message> - " + line + "";
										} else if (lineCase.contains("_6.2-")) {
											// errM62 = 6.2 - (M) - Large object data cells
											errM62 = errM62 + "</Message><Message> - " + line + "";
										} else {
											errF50 = errF50 + "</Message><Message> - " + line + "";
										}
									}
								}
							}
							scannerOutput.close();

							// TODO Error nach Modul ausgeben
							if (min) {
								return false;
							} else {
								// errE43 = 4.3 - (E) - Correspondence between metadata and table data
								if (errE43 != "") {
									errorDetail = errE43;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_E_HEADER43);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errF50 = 5.0 - (F) - Requirements for metadata
								}
								if (errF50 != "") {
									errorDetail = errF50;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_F_HEADER50);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errG51 = 5.1 - (G) - Database level metadata
								}
								if (errG51 != "") {
									errorDetail = errG51;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_G_HEADER51);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errG52 = 5.2 - (G) - Schema level metadata
								}
								if (errG52 != "") {
									errorDetail = errG52;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_G_HEADER52);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errG53 = 5.3 - (G) - Type level metadata
								}
								if (errG53 != "") {
									errorDetail = errG53;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_G_HEADER53);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errG54 = 5.4 - (G) - Attribute level metadata
								}
								if (errG54 != "") {
									errorDetail = errG54;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_G_HEADER54);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH55 = 5.5 - (H) - Table level metadata
								}
								if (errH55 != "") {
									errorDetail = errH55;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER55);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH56 = 5.6 - (H) - Column level metadata
								}
								if (errH56 != "") {
									errorDetail = errH56;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER56);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH57 = 5.7 - (H) - Field level metadata
								}
								if (errH57 != "") {
									errorDetail = errH57;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER57);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH60 = 6.0 - (H) - Requirements for table data
								}
								if (errH60 != "") {
									errorDetail = errH60;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER60);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH61 = 6.1 - (H) - Table schema definition
								}
								if (errH61 != "") {
									errorDetail = errH61;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER61);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH63 = 6.3 - (H) - Date and timestamp data cells
								}
								if (errH63 != "") {
									errorDetail = errH63;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER63);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errH64 = 6.4 - (H) - Table data
								}
								if (errH64 != "") {
									errorDetail = errH64;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_H_HEADER64);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errK58 = 5.8 - (K) - Primary Key level metadata
								}
								if (errK58 != "") {
									errorDetail = errK58;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_K_HEADER58);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errK59 = 5.9 - (K) - Foreign Key level metadata
								}
								if (errK59 != "") {
									errorDetail = errK59;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_K_HEADER59);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errK510 = 5.10 - (K) - Reference level metadata
								}
								if (errK510 != "") {
									errorDetail = errK510;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_K_HEADER510);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errK511 = 5.11 - (K) - Candidate Key level metadata
								}
								if (errK511 != "") {
									errorDetail = errK511;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_K_HEADER511);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errK512 = 5.12 - (K) - Check constraint level metadata
								}
								if (errK512 != "") {
									errorDetail = errK512;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_K_HEADER512);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL513 = 5.13 - (L) - Trigger level metadata
								}
								if (errL513 != "") {
									errorDetail = errL513;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER513);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL514 = 5.14 - (L) - View level metadata
								}
								if (errL514 != "") {
									errorDetail = errL514;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER514);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL515 = 5.15 - (L) - Routine level metadata
								}
								if (errL515 != "") {
									errorDetail = errL515;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER515);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL516 = 5.16 - (L) - Parameter level metadata
								}
								if (errL516 != "") {
									errorDetail = errL516;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER516);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL517 = 5.17 - (L) - User level metadata
								}
								if (errL517 != "") {
									errorDetail = errL517;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER517);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL518 = 5.18 - (L) - Role level metadata
								}
								if (errL518 != "") {
									errorDetail = errL518;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER518);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
									// errL519 = 5.19 - (L) - Privilege level metadata
								}
								if (errL519 != "") {
									errorDetail = errL519;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_L_HEADER519);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
								} else if (warning1 != "") {
									warning2 = getTextResourceService().getText(locale, MESSAGE_XML_L_WARNING,
											warning1);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_L_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEMESSAGE_INFO, warning2, ""));
									// errM62 = 6.2 - (M) - Large object data cells
								}
								if (errM62 != "") {
									errorDetail = errM62;
									errorHeader = getTextResourceService().getText(locale, MESSAGE_XML_M_HEADER62);
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEINVALID, "DBPTK",
															"\"" + errorHeader + "\"" + errorDetail));
								}
							}
						}
					} catch (Exception e) {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_SIARD)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
						if (outputDbptk.exists()) {
							// report loeschen
							outputDbptk.delete();
						}
						if (outputProcessDbptk.exists()) {
							// report loeschen
							outputProcessDbptk.delete();
						}
						return false;
					}
					// TODO: Ende: Auswertung
				}
				if (outputDbptk.exists()) {
					// report loeschen
					outputDbptk.delete();
				}
				if (outputProcessDbptk.exists()) {
					// report loeschen
					outputProcessDbptk.delete();
				}
				return isValid;

			} else {
				// keine Validierung mit dbptk > andere SIARD Version
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
