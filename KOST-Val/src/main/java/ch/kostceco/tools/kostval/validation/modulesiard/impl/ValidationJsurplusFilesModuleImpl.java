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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kosttools.fileservice.Sed;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationJsurplusFilesException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationJsurplusFilesModule;

/**
 * Validierungsschritt J (zusaetzliche Primaerdateien) Enthaelt der
 * content-Ordner Dateien oder Ordner die nicht in metadata.xml oder in
 * table[Zahl].xml beschrieben sind ? invalid --> zusaetzliche Ordner oder
 * Dateien im content-Ordner
 * 
 * @author Ec Christian Eugster
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationJsurplusFilesModuleImpl extends ValidationModuleImpl implements ValidationJsurplusFilesModule {
	private boolean min = false;

	Map<String, String> filesInSiardUnsorted = new HashMap<String, String>();
	Map<String, String> filesInSiard = new HashMap<String, String>();
	Map<String, String> tablesInSiard = new HashMap<String, String>();
	Map<String, String> tablesToRemove = new HashMap<String, String>();
	Map<String, String> filesToRemove = new HashMap<String, String>();

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationJsurplusFilesException {
		filesInSiardUnsorted.clear();
		filesInSiard.clear();
		tablesInSiard.clear();
		tablesToRemove.clear();
		filesToRemove.clear();
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if (onWorkConfig.equals("yes")) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("J    ");
			System.out.print("\b\b\b\b\b");
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
		}

		boolean valid = true;
		try {
			String pathToWorkDir = configMap.get("PathToWorkDir");
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			String contentString = new StringBuilder(pathToWorkDir).append(File.separator).append("content").toString();
			File content = new File(contentString);
			HashMap<String, File> hashMap = new HashMap<String, File>();
			Map<String, File> fileMap = Util.getContent(content, hashMap);
			Set<String> fileMapKeys = fileMap.keySet();
			// alle Dateien in SIARD in die Map filesInSiard schreiben (Inhalt)
			for (Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext();) {
				String entryName = iterator.next();
				// System.out.println( "entryName: " + entryName );
				// entryName: content/schema1/table7/table7.xsd
				if (!entryName.equalsIgnoreCase(contentString)) {
					filesInSiardUnsorted.put(entryName, entryName);
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("J-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("J\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("J|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("J/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			}

			filesInSiard = new TreeMap<String, String>(filesInSiardUnsorted);

			// Pfad zum Programm existiert die Dateien?
			String checkTool = Sed.checkSed(dirOfJarPath);
			if (!checkTool.equals("OK")) {
				// mindestens eine Datei fehlt fuer die Validierung
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
									+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkTool,
											getTextResourceService().getText(locale, ABORTED)));
					return false;
				}
			}

			try {
				// Struktur aus metadata.xml herauslesen (path)
				pathToWorkDir = configMap.get("PathToWorkDir");
				pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
				File metadataXml = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("header")
						.append(File.separator).append("metadata.xml").toString());
				InputStream fin = new FileInputStream(metadataXml);
				SAXBuilder builder = new SAXBuilder();
				Document document = (Document) builder.build(fin);
				fin.close();
				// set to null
				fin = null;

				Boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
				Boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
				Namespace ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
				if (version1) {
					// ns = Namespace.getNamespace(
					// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
				} else {
					if (version2) {
						ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
					} else {
						valid = false;
						if (min) {
							return false;
						} else {

							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
											+ getTextResourceService().getText(locale, MESSAGE_XML_D_INVALID_XMLNS,
													metadataXml));
						}
					}
				}
				// select schema elements and loop
				List<Element> schemas = ((org.jdom2.Document) document).getRootElement().getChild("schemas", ns)
						.getChildren("schema", ns);
				for (Element schema : schemas) {
					valid = validateSchema(schema, ns, pathToWorkDir, configMap);
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("J-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("J\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("J|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("J/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} catch (java.io.IOException ioe) {
				valid = false;
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											ioe.getMessage() + " (IOException)"));
				}
			} catch (JDOMException e) {
				valid = false;
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											e.getMessage() + " (JDOMException)"));
				}
			}

			if (filesInSiard.size() > 0) {
				try {
					// in filesInSiard sind jetzt noch die drinnen, welche nicht
					// in metadata.xml erwaehnt
					// wurden

					// TODO: grep verwenden, damit auch grosse dateien
					// funktionieren!

					Set<String> filesInSiardKeys = filesInSiard.keySet();
					for (Iterator<String> iterator = filesInSiardKeys.iterator(); iterator.hasNext();) {
						String entryName = iterator.next();
						File entryNameFile = new File(entryName);
						String entryNameParent = entryNameFile.getParent();
						// System.out.println( "gesucht: " + entryName );
						Set<String> tablesInSiardKeys = tablesInSiard.keySet();
						for (Iterator<String> iteratorTables = tablesInSiardKeys.iterator(); iteratorTables
								.hasNext();) {
							String tableName = iteratorTables.next();
							File tableXml = new File(tableName);
							String tableNameParent = tableXml.getParent();
							boolean tableFile = false;

							File fSearchtable = tableXml;
							File fSearchtableTemp = new File(fSearchtable.getAbsolutePath() + "_Temp.xml");
							/*
							 * mit Util.oldnewstring respektive replace koennen sehr grosse files nicht
							 * bearbeitet werden!
							 * 
							 * Entsprechend wurde sed verwendet. die einzelnen Aktionen werden in Serie (-e)
							 * ausgefuehrt:
							 * 
							 * sed -e 's/a/A/g' -e 's/b/B/g' 1.txt
							 */

							// Bringt alles auf eine Zeile
							String sed = "-e 's/\\n/ /g' ";
							String sed2 = "-e ':a;N;$!ba;s/\\n/ /g' ";
							// Trennt ><row. Nur eine row auf einer Zeile
							String sed3 = "-e 's/\\d060row/\\n\\d060row/g' ";
							// Trennt ><table. <table auf eine neue Zeile
							String sed4 = "-e 's/\\d060\\d047table/\\n\\d060\\d047table/g' ";

							// Bringt alles auf eine Zeile
							String options = sed + sed2 + sed3 + sed4;

							File workDir = new File(pathToWorkDir);

							if (!fSearchtableTemp.exists()) {
								// Sed-Befehl: pathToSedExe options fSearchtable
								// > fSearchtableTemp
								String resultExec = Sed.execSed(options, fSearchtable, fSearchtableTemp, workDir,
										dirOfJarPath);
								if (!resultExec.equals("OK")) {
									// Exception oder Report existiert nicht
									if (min) {
										return false;
									} else {
										if (resultExec.equals("NoReport")) {
											// Report existiert nicht
											Logtxt.logtxt(logFile,
													getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
															+ getTextResourceService().getText(locale,
																	MESSAGE_XML_MISSING_REPORT));
											return false;
										} else {
											// Exception
											Logtxt.logtxt(logFile,
													getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
															+ getTextResourceService().getText(locale,
																	ERROR_XML_SERVICEFAILED, "sed", resultExec));
											return false;
										}
									}
								}
								// Ende Sed direkt auszuloesen

								tableXml = fSearchtableTemp;
							} else {
								tableXml = fSearchtableTemp;
							}

							try {
								InputStream fis = new FileInputStream(tableXml);
								BufferedReader br = new BufferedReader(new InputStreamReader(fis));

								File folderTable = tableXml.getParentFile();
								// File file = new File("/path/to/directory");
								String lobName = "nichts";
								String[] directories = folderTable.list(new FilenameFilter() {
									@Override
									public boolean accept(File current, String lobName) {
										return new File(current, lobName).isDirectory();
									}
								});
								lobName = Arrays.toString(directories).toString();
								lobName = lobName.substring(1, lobName.length() - 1);
								// System.out.println( "lobName: " + lobName );

								// Datei Zeile fuer Zeile lesen und ermitteln ob
								// "file=" darin enthalten ist

								for (String line = br.readLine(); line != null; line = br.readLine()) {
									if (line.contains(" file=")) {
										// System.out.println( "file in
										// table.xml: " + line );
										tableFile = true;
										String newEntryName = entryName.substring(entryName.indexOf("content"),
												entryName.length());
										String relEntryName = newEntryName;
										int lobLength = lobName.length() + 1;
										int lobLengthEntryName = entryName.indexOf(lobName) + lobLength;
										if (lobLengthEntryName < entryName.length()) {
											relEntryName = entryName.substring(lobLengthEntryName, entryName.length());
										}
										// System.out.println( "lobLength: " +
										// lobLength );
										/*
										 * System.out.println( "entryName: " + entryName + " -->  newEntryName: " +
										 * newEntryName + " -->  relEntryName: " + relEntryName );
										 */
										if (line.contains(newEntryName)) {
											// entryName ist in der Tabelle
											// enthalten und wird spaeter aus
											// liste geloescht
											filesToRemove.put(entryName, entryName);
											filesToRemove.put(entryNameParent, entryNameParent);
											// System.out.println( "gefunden
											// (1)" );
											break;
										} else {
											newEntryName = newEntryName.replace("\\", "/");
											if (line.contains(newEntryName)) {
												// entryName ist in Tabelle
												// enthalten und wird spaeter
												// aus liste geloescht
												filesToRemove.put(entryName, entryName);
												filesToRemove.put(entryNameParent, entryNameParent);
												// System.out.println( "gefunden
												// (2)" );
												break;
											} else if (line.contains(relEntryName)) {
												// entryName ist in der Tabelle
												// enthalten und wird spaeter
												// aus liste
												// geloescht
												filesToRemove.put(entryName, entryName);
												filesToRemove.put(entryNameParent, entryNameParent);
												// System.out.println( "gefunden
												// (3)" );
												break;
											} else {
												relEntryName = relEntryName.replace("\\", "/");
												if (line.contains(relEntryName)) {
													// entryName ist in Tabelle
													// enthalten und wird
													// spaeter aus liste
													// geloescht
													filesToRemove.put(entryName, entryName);
													filesToRemove.put(entryNameParent, entryNameParent);
													// System.out.println(
													// "gefunden (4)" );
													break;
												} else {
													// entryName wurde nicht in
													// dieser Zeile gefunden
													// keine Aktion
												}
											}
										}
									}
								}

								br.close();
								// set to null
								br = null;
							} catch (FileNotFoundException e) {
								if (min) {
									return false;
								} else {
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
													+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
															"FileNotFoundException"));
									return false;
								}
							} catch (Exception e) {
								if (min) {
									return false;
								} else {
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
													+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
															(e.getMessage() + " 1"))); //
									return false;
								}
							} // table durch scannen
							if (!tableFile) {
								// tableName enthaelt keine file und wird aus
								// liste geloescht
								tablesToRemove.put(tableName, tableName);
								tablesToRemove.put(tableNameParent, tableNameParent);
							}
						} // tablesInSiard durchgehen
							// tablesInSiard mit tablesToRemove bereinigen
						Set<String> tablesToRemoveKeys = tablesToRemove.keySet();
						for (Iterator<String> iteratorRemove = tablesToRemoveKeys.iterator(); iteratorRemove
								.hasNext();) {
							String tableName = iteratorRemove.next();
							tablesInSiard.remove(tableName);
						}
					} // filesInSiard durchgehen und mit tablesInSiard
						// ausduennen
						// filesInSiard mit filesToRemove bereinigen
					Set<String> filesToRemoveKeys = filesToRemove.keySet();
					for (Iterator<String> iteratorRemove = filesToRemoveKeys.iterator(); iteratorRemove.hasNext();) {
						String fileName = iteratorRemove.next();
						filesInSiard.remove(fileName);
					}
				} catch (Exception e) {
					if (min) {
					} else {

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, (e.getMessage() + " 2")));
						// return false;
					}
				}
			}

			// in filesInSiard sind jetzt noch die drinnen, welche nicht in
			// table.xml erwaehnt wurden
			if (filesInSiard.size() > 0) {
				valid = false;
				if (min) {
					return false;
				} else {
					String noPretty = filesInSiard.keySet() + "";
					/*
					 * [C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\
					 * content\schema0\newOrdnerS0,
					 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\
					 * content\schema0\newOrdnerS1,
					 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\ content\schema0\table7\
					 * newOrdner7]
					 */
					String pretty = noPretty.replace("[", "</Message><Message>");
					pretty = pretty.replace("]", "");
					pretty = pretty.replace(", ", "</Message><Message>");
					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
							+ getTextResourceService().getText(locale, MESSAGE_XML_J_INVALID_ENTRY, pretty));
				}
			} else {
				valid = true;
			}

		} catch (Exception e) {
			if (min) {
			} else {

				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_SIARD)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, (e.getMessage() + " 3")));
			} // return false;
		}
		filesInSiardUnsorted.clear();
		filesInSiard.clear();
		tablesInSiard.clear();
		tablesToRemove.clear();
		filesToRemove.clear();

		return valid;

	}

	private boolean validateSchema(Element schema, Namespace ns, String pathToWorkDir, Map<String, String> configMap) {
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if (onWorkConfig.equals("no")) {
			// keine Ausgabe
			showOnWork = false;
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
			// keine Ausgabe
			showOnWork = false;
		} else {
			if (onWork == 410) {
				onWork = 2;
				System.out.print("J-   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 110) {
				onWork = onWork + 1;
				System.out.print("J\\   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 210) {
				onWork = onWork + 1;
				System.out.print("J|   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 310) {
				onWork = onWork + 1;
				System.out.print("J/   ");
				System.out.print("\b\b\b\b\b");
			} else {
				onWork = onWork + 1;
			}
		}
		boolean valid = true;
		Element schemaFolder = schema.getChild("folder", ns);
		File schemaPath = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("content")
				.append(File.separator).append(schemaFolder.getText()).toString());
		String schemaPathString = schemaPath.toString();
		filesInSiard.remove(schemaPathString);

		if (schemaPath.isDirectory()) {
			if (schema.getChild("tables", ns) != null) {

				List<Element> tables = schema.getChild("tables", ns).getChildren("table", ns);
				for (Element table : tables) {
					String name = "";
					Element tableFolder = table.getChild("folder", ns);
					File tablePath = new File(new StringBuilder(schemaPath.getPath()).append(File.separator)
							.append(tableFolder.getText()).toString());
					name = tablePath.toString();
					filesInSiard.remove(name);
					// die Datei "name" aus filesInSiard entfernen
					// System.out.println( "Remove von metadata.xml: " + name );

					if (tablePath.isDirectory()) {
						File tableXsd = new File(new StringBuilder(tablePath.getPath()).append(File.separator)
								.append(tableFolder.getText() + ".xsd").toString());
						name = tableXsd.toString();
						// die Datei "name" aus filesInSiard entfernen
						filesInSiard.remove(name);

						File tableXml = new File(new StringBuilder(tablePath.getPath()).append(File.separator)
								.append(tableFolder.getText() + ".xml").toString());
						name = tableXml.toString();
						// die Datei "name" aus filesInSiard entfernen
						filesInSiard.remove(name);
						// die Datei "name" in die Liste aller table.xml
						// eintragen
						tablesInSiard.put(name, name);

					}
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("J-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("J\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("J|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("J/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} else {
				// Kein Fehler sondern leeres Schema
			}
		}
		return valid;
	}
}
