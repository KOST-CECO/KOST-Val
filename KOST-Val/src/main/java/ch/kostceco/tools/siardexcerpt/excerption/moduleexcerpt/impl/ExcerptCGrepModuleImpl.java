/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
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

package ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kosttools.fileservice.Grep;
import ch.kostceco.tools.kosttools.fileservice.Sed;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptCGrepException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptCGrepModule;

/**
 * 3) extract: mit den Keys anhand der config einen Records herausziehen und
 * anzeigen
 */
public class ExcerptCGrepModuleImpl extends ValidationModuleImpl implements ExcerptCGrepModule {

	public static String NEWLINE = System.getProperty("line.separator");

	@Override
	public boolean validate(File siardDatei, File outFile, String excerptString, Map<String, String> configMap,
			Locale locale) throws ExcerptCGrepException {
		// Ausgabe -> Ersichtlich das SIARDexcerpt arbeitet
		int onWork = 41;

		boolean isValid = true;

		/*
		 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein
		 * generelles TODO in allen Modulen. Zuerst immer dirOfJarPath ermitteln und
		 * dann alle Pfade mit
		 * 
		 * dirOfJarPath + File.separator +
		 * 
		 * erweitern.
		 */
		File pathFile = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		String locationOfJarPath = pathFile.getAbsolutePath();
		String dirOfJarPath = locationOfJarPath;
		if (locationOfJarPath.endsWith(".jar") || locationOfJarPath.endsWith(".exe")
				|| locationOfJarPath.endsWith(".")) {
			File file = new File(locationOfJarPath);
			dirOfJarPath = file.getParent();
		}

		String pathToWorkDir = configMap.get("PathToWorkDir");
		File workDir = new File(pathToWorkDir);

		String sed = configMap.get("Sed");

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Sed.checkSed(dirOfJarPath);
		if (!checkTool.equals("OK")) {
			// mindestens eine Datei fehlt --> Abbruch
			Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
					+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_C_MISSINGFILE, checkTool));
			sed = "nok";
		}
		// Pfad zum Programm existiert die Dateien?
		String checkToolGrep = Grep.checkGrep(dirOfJarPath);
		if (!checkToolGrep.equals("OK")) {
			// mindestens eine Datei fehlt --> Abbruch
			Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
					+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_C_MISSINGFILE, checkToolGrep));
			return false;
		}

		File tempOutFileMt = new File(outFile.getAbsolutePath() + ".tmpMt");
		File tempOutFile = new File(outFile.getAbsolutePath() + ".tmp");
		File xmlExtracted = new File(
				siardDatei.getAbsolutePath() + File.separator + "header" + File.separator + "metadata.xml");
		String content = "";

		// TODO: Record aus Maintable herausholen
		try {
			if (tempOutFileMt.exists()) {
				tempOutFileMt.delete();
				if (tempOutFileMt.exists()) {
					Util.replaceAllChar(tempOutFileMt, "");
				}
				/*
				 * Util.deleteDir( tempOutFile );
				 * 
				 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens
				 * bei exit. wenn es nicht gelöchscht werden kann wird es geleert.
				 */
			}
			if (tempOutFile.exists()) {
				tempOutFile.delete();
				if (tempOutFile.exists()) {
					Util.replaceAllChar(tempOutFile, "");
				}
				/*
				 * Util.deleteDir( tempOutFile );
				 * 
				 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens
				 * bei exit. wenn es nicht gelöchscht werden kann wird es geleert.
				 */
			}

			String folder = configMap.get("MaintableFolder");
			String name = configMap.get("MaintableName");
			String cell = configMap.get("MaintablePrimarykeyCell");
			String schemafolder = configMap.get("MschemaFolder");
			String schemaname = configMap.get("MschemaName");
			if (folder.startsWith("Configuration-Error:")) {
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_B) + folder);
				return false;
			}
			if (cell.startsWith("Configuration-Error:")) {
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_B) + cell);
				return false;
			}
			String tabfolder = "";
			String tabname = "";
			String tabdescription = "";
			String tabdescriptionProv = "";
			String cellname = "";
			String celldescription = "";

			File fSchema = new File(
					siardDatei.getAbsolutePath() + File.separator + "content" + File.separator + schemafolder);
			File fMaintable = new File(
					fSchema.getAbsolutePath() + File.separator + folder + File.separator + folder + ".xml");

			try {
				/*
				 * Der excerptString kann Leerschläge enthalten, welche bei grep Problem
				 * verursachen. Entsprechend werden diese durch . ersetzt (Wildcard)
				 */
				String excerptStringM = excerptString.replaceAll(" ", ".");
				excerptStringM = excerptStringM.replaceAll("\\*", "\\.");
				excerptStringM = excerptStringM.replaceAll("\\.", "\\.*");
				excerptStringM = "<" + cell + ">" + excerptStringM + "</" + cell + ">";

				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_OPEN,
						schemaname.replace(" ", "") + "_" + name.replace(" ", "")));

				// Informationen zur Tabelle aus metadata.xml (UTF8) herausholen
				// mit grep zu xmlExtracted (normales txt file)
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				// Document doc = db.parse( new FileInputStream( xmlExtracted
				// ));
				Document doc = db.parse(new FileInputStream(xmlExtracted), "UTF8");
				doc.getDocumentElement().normalize();
				dbf.setFeature("http://xml.org/sax/features/namespaces", false);

				NodeList nlTable = doc.getElementsByTagName("table");
				for (int i = 0; i < nlTable.getLength(); i++) {
					/*
					 * cellname und celldescription leeren, da vorgängige Tabelle nicht die Richtige
					 * war.
					 */
					cellname = "";
					celldescription = "";
					Node nodenlTable = nlTable.item(i);
					NodeList childNodes = nodenlTable.getChildNodes();
					for (int x = 0; x < childNodes.getLength(); x++) {
						Node subNodeI = childNodes.item(x);
						if (subNodeI.getNodeName().equals("folder")) {
							// System.out.println( subNodeI.getNodeName()+":
							// "+subNodeI.getTextContent() );
							if (subNodeI.getTextContent().equals(folder)) {
								/*
								 * Es ist die richtige Tabelle. Ensprechend wird i ans ende Gestellt, damit die
								 * i-Schlaufe beendet wird.
								 */
								// System.out.println ("Richtige Tabelle");
								tabfolder = subNodeI.getTextContent();
								i = nlTable.getLength();
							}
						} else if (subNodeI.getNodeName().equals("name")) {
							// System.out.println( subNodeI.getNodeName()+":
							// "+subNodeI.getTextContent() );
							tabname = subNodeI.getTextContent();
						} else if (subNodeI.getNodeName().equals("description")) {
							tabdescriptionProv = new String(subNodeI.getTextContent());
							// Umlaute konnte nicht korrekt in UTF-8
							// wiedergegeben werden --> normalisieren
							tabdescriptionProv = Util.umlaute(tabdescriptionProv);
							tabdescriptionProv = new String(tabdescriptionProv.getBytes(), StandardCharsets.UTF_8);
							/*
							 * in der description generiert mit csv2siard wird nach "word" der Select Befehl
							 * angehängt. Dieser soll nicht mit ausgegeben werden.
							 */
							String word = "\\u000A";
							int endIndex = tabdescriptionProv.indexOf(word);
							if (endIndex == -1) {
								tabdescription = tabdescriptionProv;
							} else {
								tabdescription = tabdescriptionProv.substring(0, endIndex);
							}
							// System.out.println( "tabdescription:
							// "+tabdescription );
						} else if (subNodeI.getNodeName().equals("columns")) {
							NodeList childNodesColumns = subNodeI.getChildNodes();
							int counter = 0;
							for (int y = 0; y < childNodesColumns.getLength(); y++) {
								Node subNodeII = childNodesColumns.item(y);
								if (subNodeII.getNodeName().equals("column")) {
									// nur Column mit nummer erweitern und nicht
									// auch Kommentare
									counter = counter + 1;
									NodeList childNodesColumn = subNodeII.getChildNodes();
									for (int z = 0; z < childNodesColumn.getLength(); z++) {
										int cellNumber = counter;
										// System.out.println( "Zelle Nr " +
										// cellNumber );
										Node subNodeIII = childNodesColumn.item(z);
										if (subNodeIII.getNodeName().equals("name")) {
											// System.out.println(
											// subNodeIII.getNodeName()+":
											// "+subNodeIII.getTextContent()
											// );
											cellname = cellname + "<c" + cellNumber + ">" + subNodeIII.getTextContent()
													+ "</c" + cellNumber + ">";
										} else if (subNodeIII.getNodeName().equals("description")) {
											// System.out.println(
											// subNodeIII.getNodeName()+":
											// "+subNodeIII.getTextContent()
											// );
											String celldescriptionProv = new String(subNodeIII.getTextContent());
											celldescriptionProv = Util.umlaute(celldescriptionProv);
											celldescription = celldescription + "<c" + cellNumber + ">"
													+ new String(celldescriptionProv.getBytes(), StandardCharsets.UTF_8)
													+ "</c" + cellNumber + ">";
										}
									}
								}
							}
						}
					}
				}
				// System.out.println(tabname+" "+
				// tabfolder+" "+tabdescription+" "+cellname+"
				// "+celldescription);

				Logtxt.logtxt(outFile,
						getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT, tabname, "tabname"));
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
						schemafolder + "/" + tabfolder, "tabfolder"));
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT, tabdescription,
						"tabdescription"));
				Logtxt.logtxt(outFile,
						getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT, cellname, "name"));
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
						celldescription, "description"));

				// grep "<c11>7561234567890</c11>" table13.xml >> output.txt
				String resultExec = Grep.execGrep(" -E ", excerptStringM, fMaintable, tempOutFileMt, workDir,
						dirOfJarPath);

				if (!resultExec.equals("OK")) {
					// grep hat nicht funktioniert
					Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
							+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, resultExec));
					isValid = false;
				}

				// liest das tempOutFile (UTF-8) ein
				// Scanner scanner = new Scanner( tempOutFile, "UTF-8" );
				Scanner scanner = new Scanner(tempOutFileMt);
				content = "";
				try {
					content = scanner.useDelimiter("\\Z").next();
				} catch (Exception e) {
					// Grep ergab kein treffer Content Null
					content = "";
				}
				scanner.close();

				Logtxt.logtxt(outFile,
						getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_CONTENT, content));
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_CLOSE,
						schemaname.replace(" ", "") + "_" + name.replace(" ", "")));

				if (tempOutFileMt.exists()) {
					tempOutFileMt.delete();
					if (tempOutFileMt.exists()) {
						Util.replaceAllChar(tempOutFileMt, "");
					}
					/*
					 * Util.deleteDir( tempOutFile );
					 * 
					 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens
					 * bei exit. wenn es nicht gelöchscht werden kann wird es geleert.
					 */
				}
				content = "";

				// Ende Grep

			} catch (Exception e) {
				Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
						+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, e.getMessage()));
				return false;
			}

		} catch (Exception e) {
			Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
					+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}

		// Ende MainTable

		// TODO: grep der SubTables
		try {
			// String name = null;
			String folder = null;
			String name = null;
			String cell = null;
			String schemafolder = null;
			String schemaname = null;

			for (int j = 1; j < 21; j++) {
				// name = subtable.getChild( "name", ns ).getText();*/
				folder = configMap.get("st" + j + "Folder");
				name = configMap.get("st" + j + "Name");
				cell = configMap.get("st" + j + "Fkcell");
				schemafolder = configMap.get("st" + j + "SchemaFolder");
				schemaname = configMap.get("st" + j + "SchemaName");
				String tabfolder = "";
				String tabkeyname = configMap.get("st" + j + "Keyname");
				String tabname = "";
				String tabschema = "";
				String tabdescription = "";
				String tabdescriptionProv = "";
				String cellname = "";
				String celldescription = "";

				// System.out.println( j + " - " + folder + " - " + cell+ " - "
				// + schemafolder );
				if (folder.equals("(..)")) {
					folder = null;
					cell = null;
					schemafolder = null;

				} else {
					File fSchema = new File(
							siardDatei.getAbsolutePath() + File.separator + "content" + File.separator + schemafolder);
					File fSubtable = new File(
							fSchema.getAbsolutePath() + File.separator + folder + File.separator + folder + ".xml");
					File fSubtableTemp1 = new File(fSchema.getAbsolutePath() + File.separator + folder + File.separator
							+ folder + "_Temp1.xml");
					File fSubtableTemp2 = new File(fSchema.getAbsolutePath() + File.separator + folder + File.separator
							+ folder + "_Temp2.xml");
					File fSubtableTemp3 = new File(fSchema.getAbsolutePath() + File.separator + folder + File.separator
							+ folder + "_Temp3.xml");
					/*
					 * mit Util.oldnewstring respektive replace können sehr grosse files nicht
					 * bearbeitet werden!
					 * 
					 * Entsprechend wurde sed verwendet.
					 */

					if (sed.equalsIgnoreCase("yes")) {
						// Bringt alles auf eine Zeile
						String sed1 = " 's/\\n/ /g' ";
						String sed2 = " ':a;N;$!ba;s/\\n/ /g' ";
						// Trennt ><row. Nur eine row auf einer Zeile
						String sed3 = " 's/\\d060row/\\n\\d060row/g' ";
						// Trennt ><table. <table auf eine neue Zeile
						String sed4 = " 's/\\d060\\d047table/\\n\\d060\\d047table/g' ";

						long sleepLong = fSubtable.length() / 1000000;

						// Sed-Befehl: pathToSedExe options fSearchtable >
						// fSearchtableTemp
						String resultExec = Sed.execSed(sed1, fSubtable, fSubtableTemp1, workDir, dirOfJarPath);
						Thread.sleep(sleepLong);
						if (resultExec.equals("OK")) {
							resultExec = Sed.execSed(sed2, fSubtableTemp1, fSubtableTemp2, workDir, dirOfJarPath);
							Thread.sleep(sleepLong);
							if (resultExec.equals("OK")) {
								resultExec = Sed.execSed(sed3, fSubtableTemp2, fSubtableTemp3, workDir, dirOfJarPath);
								Thread.sleep(sleepLong);
								if (resultExec.equals("OK")) {
									resultExec = Sed.execSed(sed4, fSubtableTemp3, fSubtable, workDir, dirOfJarPath);
									Thread.sleep(sleepLong);
								}
							}
						}
						if (!resultExec.equals("OK") || fSubtable.length() == 0) {
							// sed hat nicht funktioniert, ueberspringen
						}
					}

					if (fSubtableTemp1.exists()) {
						Util.deleteDir(fSubtableTemp1);
					}
					if (fSubtableTemp2.exists()) {
						Util.deleteDir(fSubtableTemp2);
					}
					if (fSubtableTemp3.exists()) {
						Util.deleteDir(fSubtableTemp3);
					}

					try {
						/*
						 * Der excerptString kann Leerschläge enthalten, welche bei grep Problem
						 * verursachen. Entsprechend werden diese durch . ersetzt (Wildcard)
						 */
						String excerptStringM = excerptString.replaceAll(" ", ".");
						excerptStringM = excerptStringM.replaceAll("\\.", "\\.*");
						excerptStringM = "<" + cell + ">" + excerptStringM + "</" + cell + ">";

						Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_OPEN,
								schemaname.replace(" ", "") + "_" + name.replace(" ", "")));

						// TODO Start Wie maintable
						// Informationen zur Tabelle aus metadata.xml (UTF8)
						// herausholen
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document doc = db.parse(new FileInputStream(xmlExtracted));
						doc.getDocumentElement().normalize();
						dbf.setFeature("http://xml.org/sax/features/namespaces", false);

						NodeList nlTable = doc.getElementsByTagName("table");
						for (int i = 0; i < nlTable.getLength(); i++) {
							// für jede Tabelle (table) ...
							tabfolder = "";
							tabname = "";
							tabschema = "";
							tabdescription = "";
							tabdescriptionProv = "";
							cellname = "";
							celldescription = "";

							Node nodenlTable = nlTable.item(i);

							// Schema name und folder herauslesen
							Node mainTables = nodenlTable.getParentNode();
							Node mainSchema = mainTables.getParentNode();
							NodeList nlSchemaChild = mainSchema.getChildNodes();
							for (int x1 = 0; x1 < nlSchemaChild.getLength(); x1++) {
								// für jedes Subelement der Tabelle (name,
								// folder, description...) ...
								Node subNode = nlSchemaChild.item(x1);
								if (subNode.getNodeName().equals("folder")) {
									tabschema = subNode.getTextContent();
									if (subNode.getTextContent().equals(schemafolder)) {
										tabschema = subNode.getTextContent();
										// das richtige schema
										NodeList childNodes = nodenlTable.getChildNodes();
										for (int x = 0; x < childNodes.getLength(); x++) {
											Node subNodeI = childNodes.item(x);
											if (subNodeI.getNodeName().equals("folder")) {
												// System.out.println(
												// subNodeI.getTextContent() );
												if (subNodeI.getTextContent().equals(folder)) {
													/*
													 * Es ist die richtige Tabelle. Ensprechend wird i ans ende
													 * Gestellt, damit die i-Schlaufe beendet wird.
													 */
													tabfolder = subNodeI.getTextContent();
													i = nlTable.getLength();
												}
											} else if (subNodeI.getNodeName().equals("name")) {
												tabname = subNodeI.getTextContent();
											} else if (subNodeI.getNodeName().equals("description")) {
												tabdescriptionProv = new String(subNodeI.getTextContent());
												tabdescriptionProv = Util.umlaute(tabdescriptionProv);
												tabdescriptionProv = new String(tabdescriptionProv.getBytes(),
														StandardCharsets.UTF_8);
												/*
												 * in der description generiert mit csv2siard wird nach "word" der
												 * Select Befehl angehängt. Dieser soll nicht mit ausgegeben werden.
												 */
												String word = "\\u000A";
												int endIndex = tabdescriptionProv.indexOf(word);
												if (endIndex == -1) {
													tabdescription = tabdescriptionProv;
												} else {
													tabdescription = tabdescriptionProv.substring(0, endIndex);
												}
											} else if (subNodeI.getNodeName().equals("columns")) {
												NodeList childNodesColumns = subNodeI.getChildNodes();
												int counter = 0;
												for (int y = 0; y < childNodesColumns.getLength(); y++) {
													Node subNodeII = childNodesColumns.item(y);
													if (subNodeII.getNodeName().equals("column")) {
														// nur Column mit nummer
														// erweitern und nicht
														// auch Kommentare
														counter = counter + 1;
														NodeList childNodesColumn = subNodeII.getChildNodes();
														for (int z = 0; z < childNodesColumn.getLength(); z++) {
															int cellNumber = counter;
															// System.out.println(
															// "Zelle Nr " +
															// cellNumber );
															Node subNodeIII = childNodesColumn.item(z);
															if (subNodeIII.getNodeName().equals("name")) {
																cellname = cellname + "<c" + cellNumber + ">"
																		+ subNodeIII.getTextContent() + "</c"
																		+ cellNumber + ">";
																// System.out.println(
																// cellname );
															} else if (subNodeIII.getNodeName().equals("description")) {
																String celldescriptionProv = new String(
																		subNodeIII.getTextContent());
																celldescriptionProv = Util.umlaute(celldescriptionProv);
																celldescription = celldescription + "<c" + cellNumber
																		+ ">"
																		+ new String(celldescriptionProv.getBytes(),
																				StandardCharsets.UTF_8)
																		+ "</c" + cellNumber + ">";
															}
														}
													}
												}
											}
										}
									}
								}
							}
							if (i == nlTable.getLength()) {
								// Ausgabe für jede Tabelle
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										tabname, "tabname"));
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										(tabschema + "/" + tabfolder), "tabfolder"));
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										tabkeyname, "tabkeyname"));
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										tabdescription, "tabdescription"));
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										cellname, "name"));
								Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
										celldescription, "description"));
							}
						}
						// TODO End Wie maintable

						// grep "<c11>7561234567890</c11>" table13.xml >>
						// output.txt
						String resultExec = Grep.execGrep(" -E ", excerptStringM, fSubtable, tempOutFile, workDir,
								dirOfJarPath);

						if (!resultExec.equals("OK")) {
							// grep hat nicht funktioniert
							Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_B)
									+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, resultExec));
							isValid = false;
						}

						// liest das tempOutFile (UTF-8) ein
						Scanner scanner = new Scanner(tempOutFile);
						content = "";
						try {
							content = scanner.useDelimiter("\\Z").next();
						} catch (Exception e) {
							// Grep ergab kein treffer Content Null
							content = "";
						}
						scanner.close();

						Logtxt.logtxt(outFile,
								getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_CONTENT, content));
						Logtxt.logtxt(outFile,
								getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_ELEMENT_CLOSE,
										schemaname.replace(" ", "") + "_" + name.replace(" ", "")));

						if (tempOutFile.exists()) {
							tempOutFile.delete();
							if (tempOutFile.exists()) {
								Util.replaceAllChar(tempOutFile, "");
							}
							/*
							 * Util.deleteDir( tempOutFile );
							 * 
							 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens
							 * bei exit. wenn es nicht gelöchscht werden kann wird es geleert.
							 */
						}
						content = "";

						// Ende Grep

					} catch (Exception e) {
						Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
								+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, e.getMessage()));
						return false;
					}

					// Ende SubTables
					if (onWork == 41) {
						onWork = 3;
						System.out.print("-   ");
						System.out.print("\r");
					} else if (onWork == 11) {
						onWork = 13;
						System.out.print("\\   ");
						System.out.print("\r");
					} else if (onWork == 21) {
						onWork = 23;
						System.out.print("|   ");
						System.out.print("\r");
					} else if (onWork == 31) {
						onWork = 33;
						System.out.print("/   ");
						System.out.print("\r");
					} else {
						onWork = onWork + 2;
					}
				}
			}
			System.out.print("   ");
			System.out.print("\r");
		} catch (Exception e) {
			Logtxt.logtxt(outFile, getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C)
					+ getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return isValid;
	}
}
