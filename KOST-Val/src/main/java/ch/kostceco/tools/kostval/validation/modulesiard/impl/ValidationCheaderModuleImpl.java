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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.io.FileUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.fileservice.Recognition;
import ch.kostceco.tools.kosttools.fileservice.Xmllint;
import ch.kostceco.tools.kosttools.util.Hash;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kosttools.util.UtilZip;
import ch.kostceco.tools.kosttools.util.Zip64Archiver;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationCheaderException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationCheaderModule;

/**
 * Validierungsschritt C (Header-Validierung) Ist der header-Ordner valid? valid
 * --> metadata.xml valid zu metadata.xsd und beides vorhanden Bemerkung -->
 * zusaetzliche Ordner oder Dateien wie z.B. metadata.xls sind im header-Ordner
 * erlaubt ==> Bei den Module A, B, C und D wird die Validierung abgebrochen,
 * sollte das Resulat invalid sein!
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationCheaderModuleImpl extends ValidationModuleImpl implements ValidationCheaderModule {

	public static String NEWLINE = System.getProperty("line.separator");

	private boolean min = false;

	private String records = "";
	private String records0 = "";
	private static Integer cRecInLine = 0;
	private static Integer cEmptyRows = 0;
	private static Integer cExt = 0;

	@SuppressWarnings({ "resource", "unused" })
	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath, String initFolderPath, File fileToOutputStart)
			throws ValidationCheaderException {
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		if (onWorkConfig.equals("yes")) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("C    ");
			System.out.print("\b\b\b\b\b");
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
		}

		Integer cRec = 0;
		Integer cRec0 = 0;
		boolean siard10 = false;
		boolean siard21 = false;
		boolean siard22 = false;
		String siard10St = configMap.get("siard10");
		if (siard10St.equals("1.0")) {
			siard10 = true;
		}
		String siard21St = configMap.get("siard21");
		if (siard21St.equals("2.1")) {
			siard21 = true;
		}
		String siard22St = configMap.get("siard22");
		if (siard22St.equals("2.2")) {
			siard22 = true;
		}

		boolean result = true;
		// Sind im Header-Ordner metadata.xml und metadata.xsd vorhanden?
		ZipEntry metadataxml = null;
		ZipEntry metadataxsd = null;

		try {
			ZipFile zipfile = new ZipFile(valDatei.getAbsolutePath());
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zEntry = entries.nextElement();
				if (zEntry.getName().equals("header/" + METADATA)) {
					metadataxml = zEntry;
				}
				if (zEntry.getName().equals("header/" + XSD_METADATA)) {
					metadataxsd = zEntry;
				}
				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("C-   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("C\\   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("C|   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("C/   ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}
			entries = null;
			zipfile.close();
			if (metadataxml == null) {
				// keine metadata.xml = METADATA in der SIARD-Datei gefunden
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
							+ getTextResourceService().getText(locale, MESSAGE_XML_C_NOMETADATAFOUND));
					return false;
				}
			}
			if (metadataxsd == null) {
				// keine metadata.xsd = XSD_METADATA in der SIARD-Datei gefunden
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
							+ getTextResourceService().getText(locale, MESSAGE_XML_C_NOMETADATAXSD));
					return false;
				}
			}
		} catch (Exception e) {
			if (min) {
				return false;
			} else {

				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage() + " xml und xsd"));
				return false;
			}
		}

		// Start Details ermitteln

		/*
		 * Wenn in der DB die Laenge von VARCHAR sehr gross ist z.B. ueber 4000 legt
		 * SIARD Suite fuer jeden Eintrag eine recordXX.txt an auch wenn dieses leer
		 * ist. Solche leere Dateien werden mehrfach abgelegt und sind identisch.
		 * 
		 * Dies kann zu Datenfehler bei der extraktion fuehren.
		 * 
		 * Erstellen einer Liste mit allen externen lob
		 */
		try {
			ZipFile zf = new ZipFile(valDatei.getAbsolutePath());
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				// nicht nur record sondern moeglichst alle lob-Datien
				ZipEntry zEntry = entries.nextElement();
				String fileName = zEntry.getName();
				// fileName content/schema0/table0/lob7/record99.txt
				// fileName content/schema0/table0/table0.xml
				// fileName content/schema0/table0/table0.xsd

				// if (fileName.contains("record") && fileName.contains(".")) {
				if (fileName.contains(".") && (fileName.contains("lob") || fileName.contains("record"))) {
					long fileSize = zEntry.getSize();
					// TODO jeweils nur 10 Eintraege
					if (fileSize == 0) {
						cRec0++;
						if (cRec0 < 10) {
							records0 = records0 + "</Message><Message> - " + zEntry.toString();
						}
					} else {
						cRec++;
						if (cRec < 10) {
							// System.out.println("fileName: " +fileName + " fileSize: " + fileSize+"
							// String: "+zEntry.toString() );
							records = records + "</Message><Message> - " + zEntry.toString() + "   fileSize: "
									+ fileSize;
						}
					}
				}
			}
			// und wenn es klappt, gleich wieder schliessen
			zf.close();
			// set to null
			zf = null;
			// System.out.println("cRec " + cRec);
			// System.out.println("cRec0 " + cRec0);

		} catch (Exception e) {
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
						+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP, " (Zip Name and Size)"));
				// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
				// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
				// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
				// abgebrochen!</Message></Error>
				return false;
			}
		}
//Ende Details ermitteln

		// Validierung metadata.xml mit metadata.xsd
		File xmlToValidate = null;
		File xsdToValidate = null;
		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf(".");
		toplevelDir = toplevelDir.substring(0, lastDotIdx);

		try {
			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */
			// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
			String pathToWorkDir = configMap.get("PathToWorkDir");
			File tmpDir = new File(pathToWorkDir + File.separator + "SIARD");
			if (tmpDir.exists()) {
				Util.deleteDir(tmpDir);
			}
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}

			/*
			 * Das metadata.xml und sein xsd muessen in das Filesystem extrahiert werden,
			 * weil bei bei Verwendung eines Inputstreams bei der Validierung ein Problem
			 * mit den xs:include Statements besteht, die includes koennen so nicht
			 * aufgeloest werden. Es werden hier jedoch nicht nur diese Files extrahiert,
			 * sondern gleich die ganze Zip-Datei, weil auch spaetere Validierungen nur mit
			 * den extrahierten Files arbeiten koennen.
			 */
			int BUFFER = 2048;
			ZipFile zipfile = new ZipFile(valDatei.getAbsolutePath());
			Enumeration<? extends ZipEntry> entries = zipfile.entries();

			// jeden entry durchgehen
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.getName();
				File destFile = new File(tmpDir, entryName);
				// System.out.println (entryName);

				// erstelle den Ueberordner
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();
				if (!entry.isDirectory()) {
					// Festhalten von metadata.xml und metadata.xsd
					if (destFile.getName().endsWith(METADATA)) {
						xmlToValidate = destFile;
					}
					if (destFile.getName().endsWith(XSD_METADATA)) {
						xsdToValidate = destFile;
					}
					InputStream stream = zipfile.getInputStream(entry);
					BufferedInputStream is = new BufferedInputStream(stream);
					int currentByte;

					// erstellung Buffer zum schreiben der Dateien
					byte data[] = new byte[BUFFER];

					// schreibe die aktuelle Datei an den gewuenschten Ort
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
					stream.close();
					fos.close();
					fos = null;
					is = null;
					stream = null;
				} else {
					destFile.mkdirs();
				}
				if (showOnWork) {
					if (onWork == 41) {
						onWork = 2;
						System.out.print("C-   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 11) {
						onWork = 12;
						System.out.print("C\\   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 21) {
						onWork = 22;
						System.out.print("C|   ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 31) {
						onWork = 32;
						System.out.print("C/   ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
				entry = null;
			}

			// Thread.sleep( 100 );
			// Ausgabe der SIARD-Version
			String pathToWorkDir2 = pathToWorkDir + File.separator + "SIARD";
			File metadataXml = new File(new StringBuilder(pathToWorkDir2).append(File.separator).append("header")
					.append(File.separator).append("metadata.xml").toString());
			Boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			Boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			Boolean version21 = FileUtils.readFileToString(metadataXml, "ISO-8859-1").contains("version=\"2.1\"");
			Boolean version22 = FileUtils.readFileToString(metadataXml, "ISO-8859-1").contains("version=\"2.2\"");
			if (version1) {
				Logtxt.logtxt(logFile, "<FormatVL>-v1.0</FormatVL>");
				// Keine Aktion im Modul C sonder I, damit es nicht abgebrochen wird, falls die
				// Version nicht akzeptiert wird
			} else if (version2) {
				if (version21) {
					Logtxt.logtxt(logFile, "<FormatVL>-v2.1</FormatVL>");
				} else if (version22) {
					Logtxt.logtxt(logFile, "<FormatVL>-v2.2</FormatVL>");
				}
				// Keine Aktion im Modul C sonder I, damit es nicht abgebrochen wird, falls die
				// Version nicht akzeptiert wird
			}

			if (xmlToValidate != null && xsdToValidate != null) {
				// der andere Fall wurde bereits oben abgefangen
				try {

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					// dbf.setValidating(false);
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(new FileInputStream(xmlToValidate));
					doc.getDocumentElement().normalize();

					BufferedReader in = new BufferedReader(new FileReader(xmlToValidate));
					StringBuffer concatenatedOutputs = new StringBuffer();
					String line;
					while ((line = in.readLine()) != null) {

						concatenatedOutputs.append(line);
						concatenatedOutputs.append(NEWLINE);
						/*
						 * Kontrollieren, dass kein Namespace verwendet wurde wie z.B. v4:
						 * 
						 * <dbname>
						 */
						if (line.contains("dbname>")) {
							if (!line.contains("<dbname>")) {
								// Invalider Status
								if (min) {
									return false;
								} else {
									int start = line.indexOf("<") + 1;
									int ns = line.indexOf(":") + 1;
									int end = line.indexOf(">");
									String lineNode = line.substring(ns, end);
									String lineNodeNS = line.substring(start, end);
									// System.out.println( lineNode + " " +
									// lineNodeNS );
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_C_METADATA_NSFOUND, lineNode, lineNodeNS));
									in.close();
									// set to null
									in = null;
									return false;
								}
							} else {
								// valider Status
								line = null;
							}
						}
					}
					in.close();
					// set to null
					in = null;
					dbf = null;
					db = null;
					doc = null;
					concatenatedOutputs = null;

					// Variante Xmllint
					File workDir = new File(pathToWorkDir);
					if (!workDir.exists()) {
						workDir.mkdir();
					}
					// Pfad zum Programm existiert die Dateien?
					String checkTool = Xmllint.checkXmllint(dirOfJarPath);
					if (!checkTool.equals("OK")) {
						// mindestens eine Datei fehlt fuer die Validierung
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE,
													checkTool, getTextResourceService().getText(locale, ABORTED)));
							result = false;
						}
					} else {
						// System.out.println("Validierung mit xmllint: ");
						try {
							String resultExec = Xmllint.execXmllint(xmlToValidate, xsdToValidate, workDir, dirOfJarPath,
									locale);
							if (!resultExec.equals("OK")) {
								// System.out.println("Validierung NICHT
								// bestanden");
								if (min) {
									return false;
								} else {
									result = false;
									String tableXmlShortString = xmlToValidate.getAbsolutePath()
											.replace(workDir.getAbsolutePath(), "");
									String tableXsdShortString = xsdToValidate.getAbsolutePath()
											.replace(workDir.getAbsolutePath(), "");
									// val.message.xml.h.invalid.xml =
									// <Message>{0} ist invalid zu
									// {1}</Message></Error>
									// val.message.xml.h.invalid.error =
									// <Message>{0}</Message></Error>
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_H_INVALID_XML, tableXmlShortString,
															tableXsdShortString));
									resultExec = resultExec.replace("ERROR: ", " - ERROR: ");
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
													+ getTextResourceService().getText(locale,
															MESSAGE_XML_SERVICEMESSAGE, resultExec, ""));
								}
							} else {
								// System.out.println("Validierung bestanden");
							}
						} catch (InterruptedException e1) {
							result = false;
							if (min) {
								return false;
							} else {
								Logtxt.logtxt(logFile, getTextResourceService().getText(locale,
										MESSAGE_XML_MODUL_C_SIARD)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												e1.getMessage() + " (InterruptedException Xmllint.execXmllint)"));
							}
						}
					}
				} catch (java.io.IOException ioe) {
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												ioe.getMessage() + " (IOException)"));
						result = false;
					}
				} catch (SAXException e) {
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												e.getMessage() + " (SAXException)"));
						result = false;
					}
				} catch (ParserConfigurationException e) {
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												e.getMessage() + " (ParserConfigurationException)"));
						result = false;
					}
				}
			}
			zipfile.close();
			// set to null
			zipfile = null;

		} catch (Exception e) {
			/*
			 * TODO Modul gescheitert, versuch anders zu extrahieren
			 * 
			 * Modul wiederholen aber extrahieren mit "Apache Commons Compress"
			 */
			if (onWorkConfig.equals("yes")) {
				// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
				showOnWork = true;
				System.out.print("C    ");
				System.out.print("\b\b\b\b\b");
			} else if (onWorkConfig.equals("nomin")) {
				min = true;
			}
			if (siard10St.equals("1.0")) {
				siard10 = true;
			}
			if (siard21St.equals("2.1")) {
				siard21 = true;
			}
			if (siard22St.equals("2.2")) {
				siard22 = true;
			}

			// Sind im Header-Ordner metadata.xml und metadata.xsd vorhanden?
			try {
				ZipFile zipfile = new ZipFile(valDatei.getAbsolutePath());
				Enumeration<? extends ZipEntry> entries = zipfile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry zEntry = entries.nextElement();
					if (zEntry.getName().equals("header/" + METADATA)) {
						metadataxml = zEntry;
					}
					if (zEntry.getName().equals("header/" + XSD_METADATA)) {
						metadataxsd = zEntry;
					}
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("C-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("C\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("C|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("C/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
				entries = null;
				zipfile.close();
				if (metadataxml == null) {
					// keine metadata.xml = METADATA in der SIARD-Datei gefunden
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_C_NOMETADATAFOUND));
						return false;
					}
				}
				if (metadataxsd == null) {
					// keine metadata.xsd = XSD_METADATA in der SIARD-Datei gefunden
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_C_NOMETADATAXSD));
						return false;
					}
				}
			} catch (Exception e2) {
				if (min) {
					return false;
				} else {
					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											e2.getMessage() + " (Modul C1 b)"));
					return false;
				}
			}

			// Validierung metadata.xml mit metadata.xsd
			toplevelDir = toplevelDir.substring(0, lastDotIdx);

			try {
				/*
				 * Nicht vergessen in
				 * "src/main/resources/config/applicationContext-services.xml" beim
				 * entsprechenden Modul die property anzugeben: <property
				 * name="configurationService" ref="configurationService" />
				 */
				// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
				String pathToWorkDirL2 = configMap.get("PathToWorkDir");
				File tmpDir = new File(pathToWorkDirL2 + File.separator + "SIARD");
				if (tmpDir.exists()) {
					Util.deleteDir(tmpDir);
				}
				if (!tmpDir.exists()) {
					tmpDir.mkdir();
				}

				/*
				 * Das metadata.xml und sein xsd muessen in das Filesystem extrahiert werden,
				 * weil bei bei Verwendung eines Inputstreams bei der Validierung ein Problem
				 * mit den xs:include Statements besteht, die includes koennen so nicht
				 * aufgeloest werden. Es werden hier jedoch nicht nur diese Files extrahiert,
				 * sondern gleich die ganze Zip-Datei, weil auch spaetere Validierungen nur mit
				 * den extrahierten Files arbeiten koennen.
				 */

				String zipFilePath = valDatei.getAbsolutePath(); // Pfad zur ZIP64-Datei

				try {
					// new Expander().expand(archive, destination);
					new Expander().expand(valDatei, tmpDir);
				} catch (IOException e3) {
					/*
					 * Modul gescheitert, versuch 3 es anders zu extrahieren
					 * 
					 * Modul wiederholen aber extrahieren mit "enterag zip64"
					 * 
					 * TODO Dieses wird auch von SIARD Suite verwendet bei der Erstellung dieser
					 * Dateien, dies birgt jedoch ein gewisses Risiko. Ensprechend Warnung
					 * herausgeben und neue SIARD-Datei ertellen anhand der extrahierten Dateien.
					 */

					try {
						try {
							Zip64Archiver.unzip64(valDatei, tmpDir);
							Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
									+ getTextResourceService().getText(locale, MESSAGE_XML_C_WARNING_UNZIP1));
							// <Message>Warnung: Dies SIARD-Datei konnte nur mit einer veralteten Library
							// extrahiert werden. </Message>
						} catch (FileNotFoundException eEnterag) {
							// ermitteln ob leere record.txt enthalten sind
							if (!records0.isEmpty() && !records.isEmpty()) {
								Logtxt.logtxt(logFile, getTextResourceService().getText(locale,
										MESSAGE_XML_MODUL_C_SIARD)
										+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP,
												records0 + " (" + cRec0 + "x) " + records + " (" + cRec + "x)"));
								// es sind leere record.txt enthalten
								// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
								// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
								// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
								// abgebrochen!</Message></Error>

								// cRec0 zaehler leerere txt records
								// records0 = records0 + "</Message><Message> - " + zEntry.toString();

								return false;
							} else if (!records0.isEmpty()) {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP,
														records0 + " (" + cRec0 + "x)"));
								// es sind leere record.txt enthalten
								// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
								// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
								// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
								// abgebrochen!</Message></Error>

								// cRec0 zaehler leerere txt records
								// records0 = records0 + "</Message><Message> - " + zEntry.toString();

								return false;
							} else if (!records.isEmpty()) {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP,
														records + " (" + cRec + "x)"));
								// es sind keine leeren record.txt enthalten aber andere
								// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
								// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
								// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
								// abgebrochen!</Message></Error>

								// cRec zaehler txt records mit inhalt
								// records = records + "</Message><Message> - " + zEntry.toString() + "
								// fileSize: " + fileSize;

								return false;
							} else {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP,
														" (Zip64Archiver.unzip64)"));
								// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
								// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
								// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
								// abgebrochen!</Message></Error>
								return false;

							}
						}

						// Outputverzeichnis zum neuen SIARD erstellen
						String pathToUserVal = directoryOfLogfile.getParent();
						File outDir = new File(pathToUserVal + File.separator + "OUTPUT");
						if (!outDir.exists()) {
							outDir.mkdir();
						}

						// Subordner im Outputverzeichnis anhand Start erstellen
						// <Infos><Start>02.04.2025 12:20:36</Start>
						// 02.04.2025_122036
						String start = "123456789";
						try {
							Scanner scanner = new Scanner(logFile);
							while (scanner.hasNextLine()) {
								String line = scanner.nextLine();
								if (line.contains("<Infos><Start>")) {
									start = line;
									scanner.close();
									break;
								}
							}
							scanner.close();
						} catch (FileNotFoundException e5) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													e5.getMessage() + " (FileNotFoundException start-log)"));
							return false;
						}
						start = start.replace("<Infos><Start>", "");
						start = start.replace("</Start>", "");
						start = start.replace(":", "");
						start = start.replace(" ", "_");
						File outDirStart = new File(outDir.getAbsolutePath() + File.separator + start);
						if (!outDirStart.exists()) {
							outDirStart.mkdir();
						}

						System.out.print("  " + outDirStart.getAbsolutePath());
						// ZIP-Datei erstellen
						File sourceDir = tmpDir;
						File zipFile = new File(outDirStart + File.separator + valDatei.getName());
						try {
							UtilZip.zipDirectory(sourceDir, zipFile);
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, MESSAGE_XML_C_WARNING_UNZIP2,
													zipFile.getAbsolutePath()));
							// <Message> -> Es wurde eine neue SIARD-Datei erstellt und hier gespeichert
							// {0}.</Message></Error><Warning>warning</Warning>
						} catch (IOException ez) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, MESSAGE_XML_C_WARNING_UNZIP2,
													"NoFile - Error during compression: " + ez.getMessage()));
							// <Message> -> Es wurde eine neue SIARD-Datei erstellt und hier gespeichert
							// {0}.</Message></Error><Warning>warning</Warning>
						}

						// Thread.sleep(100);
					} catch (Exception e4) {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
									+ getTextResourceService().getText(locale, MESSAGE_XML_C_INCORRECTZIP, " (e4)"));
							// <Message>Die SIARD-Datei konnte nicht extrahiert werden.
							// {0}</Message><Message> -> Versuchen Sie diese manuell zu extrahieren und
							// wieder zu komprimieren (Deflate-ZIP). </Message><Message> => Validierung
							// abgebrochen!</Message></Error>
							return false;
						}
					}
				}
			} catch (Exception e3) {
				if (min) {
					return false;
				} else {
					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											e3.getMessage() + " (Modul C2 b)"));
					return false;
				}
			}

			try {
				// Thread.sleep( 1000 );
				// Ausgabe der SIARD-Version
				// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
				String pathToWorkDir = configMap.get("PathToWorkDir");
				String pathToWorkDir2 = pathToWorkDir + File.separator + "SIARD";
				File metadataXml = new File(new StringBuilder(pathToWorkDir2).append(File.separator).append("header")
						.append(File.separator).append("metadata.xml").toString());
				Boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
				Boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
				Boolean version21 = FileUtils.readFileToString(metadataXml, "ISO-8859-1").contains("version=\"2.1\"");
				Boolean version22 = FileUtils.readFileToString(metadataXml, "ISO-8859-1").contains("version=\"2.2\"");
				if (version1) {
					Logtxt.logtxt(logFile, "<FormatVL>-v1.0</FormatVL>");
					// Keine Aktion im Modul C sonder I, damit es nicht abgebrochen wird, falls die
					// Version nicht akzeptiert wird
				} else if (version2) {
					if (version21) {
						Logtxt.logtxt(logFile, "<FormatVL>-v2.1</FormatVL>");
					} else if (version22) {
						Logtxt.logtxt(logFile, "<FormatVL>-v2.2</FormatVL>");
					}
					// Keine Aktion im Modul C sonder I, damit es nicht abgebrochen wird, falls die
					// Version nicht akzeptiert wird
				}

				if (xmlToValidate != null && xsdToValidate != null) {
					// der andere Fall wurde bereits oben abgefangen
					try {
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						// dbf.setValidating(false);
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document doc = db.parse(new FileInputStream(xmlToValidate));
						doc.getDocumentElement().normalize();

						BufferedReader in = new BufferedReader(new FileReader(xmlToValidate));
						StringBuffer concatenatedOutputs = new StringBuffer();
						String line;
						while ((line = in.readLine()) != null) {

							concatenatedOutputs.append(line);
							concatenatedOutputs.append(NEWLINE);
							/*
							 * Kontrollieren, dass kein Namespace verwendet wurde wie z.B. v4:
							 * 
							 * <dbname>
							 */
							if (line.contains("dbname>")) {
								if (!line.contains("<dbname>")) {
									// Invalider Status
									if (min) {
										return false;
									} else {
										int start = line.indexOf("<") + 1;
										int ns = line.indexOf(":") + 1;
										int end = line.indexOf(">");
										String lineNode = line.substring(ns, end);
										String lineNodeNS = line.substring(start, end);
										// System.out.println( lineNode + " " +
										// lineNodeNS );
										Logtxt.logtxt(logFile,
												getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_C_METADATA_NSFOUND, lineNode, lineNodeNS));
										in.close();
										// set to null
										in = null;
										return false;
									}
								} else {
									// valider Status
									line = null;
								}
							}
						}
						in.close();
						// set to null
						in = null;
						dbf = null;
						db = null;
						doc = null;
						concatenatedOutputs = null;

						// Variante Xmllint
						File workDir = new File(pathToWorkDir);
						if (!workDir.exists()) {
							workDir.mkdir();
						}
						// Pfad zum Programm existiert die Dateien?
						String checkTool = Xmllint.checkXmllint(dirOfJarPath);
						if (!checkTool.equals("OK")) {
							// mindestens eine Datei fehlt fuer die Validierung
							if (min) {
								return false;
							} else {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE,
														checkTool, getTextResourceService().getText(locale, ABORTED)));
								result = false;
							}
						} else {
							// System.out.println("Validierung mit xmllint: ");
							try {
								String resultExec = Xmllint.execXmllint(xmlToValidate, xsdToValidate, workDir,
										dirOfJarPath, locale);
								if (!resultExec.equals("OK")) {
									// System.out.println("Validierung NICHT
									// bestanden");
									if (min) {
										return false;
									} else {
										result = false;
										String tableXmlShortString = xmlToValidate.getAbsolutePath()
												.replace(workDir.getAbsolutePath(), "");
										String tableXsdShortString = xsdToValidate.getAbsolutePath()
												.replace(workDir.getAbsolutePath(), "");
										// val.message.xml.h.invalid.xml =
										// <Message>{0} ist invalid zu
										// {1}</Message></Error>
										// val.message.xml.h.invalid.error =
										// <Message>{0}</Message></Error>
										Logtxt.logtxt(logFile,
												getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_H_INVALID_XML, tableXmlShortString,
																tableXsdShortString));
										resultExec = resultExec.replace("ERROR: ", " - ERROR: ");
										Logtxt.logtxt(logFile,
												getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_SERVICEMESSAGE, resultExec, ""));
									}
								} else {
									// System.out.println("Validierung bestanden");
								}
							} catch (InterruptedException e1) {
								result = false;
								if (min) {
									return false;
								} else {
									Logtxt.logtxt(logFile, getTextResourceService().getText(locale,
											MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													e1.getMessage() + " (InterruptedException Xmllint.execXmllint b)"));
								}
							}
						}
					} catch (java.io.IOException ioe) {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													ioe.getMessage() + " (IOException b)"));
							result = false;
						}
					} catch (SAXException e4) {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													e4.getMessage() + " (SAXException b)"));
							result = false;
						}
					} catch (ParserConfigurationException e4) {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													e4.getMessage() + " (ParserConfigurationException b)"));
							result = false;
						}
					} catch (Exception e4) {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													e4.getMessage() + " (Exception b)"));
							return false;
						}
					}
				}
			} catch (Exception e4) {
				if (min) {
					return false;
				} else {
					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											e4.getMessage() + " (Modul C3)"));
					return false;
				}
			}
		}

		if ((configMap.get("siardrep").equals("yes"))) {
			if ((configMap.get("siardrowsrep").equals("yes")) || (configMap.get("siardlobrep").equals("yes"))
					|| (configMap.get("siardlobextrep").equals("yes"))) {
				// TODO: nur ausfuehren wenn gewuenscht
				String pathToWorkDir = configMap.get("PathToWorkDir");
				pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
				File metadataXml = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("header")
						.append(File.separator).append("metadata.xml").toString());
				boolean emptyRows;
				try {
					emptyRows = Util.stringInFile("<rows />", metadataXml);
					if (!emptyRows) {
						emptyRows = Util.stringInFile("<rows/>", metadataXml);
					}
					if (!emptyRows) {
						emptyRows = Util.stringInFile("<rows></rows>", metadataXml);
					}
					if (!emptyRows) {
						emptyRows = Util.stringInFile("<rows> </rows>", metadataXml);
					}
					if (cRec > 0 || cRec0 > 0 || emptyRows) {
						int cRecTot = cRec + cRec0;

						String repairSiard = "Start";
						repairSiard = doRepairSiard(valDatei, directoryOfLogfile, configMap, locale, logFile,
								dirOfJarPath, cRecTot, emptyRows);

						if (cRecInLine > 0) {
							String siardPath = repairSiard.replace(".zip", ".siard");

							File zipFile = new File(repairSiard);
							File siardFile = new File(siardPath);
							if (zipFile.exists()) {
								Util.deleteFile(zipFile);
							}
							if (siardFile.exists()) {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_REP_LOB,
														valDatei, cRecTot, cRecInLine, siardFile.getAbsolutePath()));
							}
						}
						if (cExt > 0) {
							String siardPath = repairSiard.replace(".zip", ".siard");

							File zipFile = new File(repairSiard);
							File siardFile = new File(siardPath);
							if (zipFile.exists()) {
								Util.deleteFile(zipFile);
							}
							if (siardFile.exists()) {
								// <Message>{0} LOB-Dateiendungen konnten korrigiert
								// werden.</Message><Message>Speicherort der reparierten SIARD-Kopie: {1}
								// </Message><Message>-> reparierte SIARD-Datei durch das Archiv kontrollieren,
								// revalidieren und falls gewuenscht
								// weiterverwenden.</Message></Error><Warning>warning</Warning>
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_SIARD)
												+ getTextResourceService().getText(locale, MESSAGE_XML_REP_LOBEXT, cExt,
														siardFile.getAbsolutePath()));
							}
						}
						if (emptyRows) {
							String siardPath = repairSiard.replace(".zip", ".siard");

							File zipFile = new File(repairSiard);
							File siardFile = new File(siardPath);
							if (zipFile.exists()) {
								Util.deleteFile(zipFile);
							}
							if (siardFile.exists()) {
								if (siardFile.exists()) {
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_REPAIR_SIARD)
													+ getTextResourceService().getText(locale, MESSAGE_XML_REP_ROWS,
															valDatei, cEmptyRows, siardFile.getAbsolutePath()));
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private static String doRepairSiard(File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale, File logFile, String dirOfJarPath, int cRecTot, boolean emptyRows) {

		/*
		 * TODO - doRepairSiard
		 * 
		 * Reparatur von SIARD-Dateien mit unnoetigen lob-Dateien
		 * 
		 * Dies insbesondere bei der Verwendung von SIARD-Suite und MSQServer und
		 * varchar(max)
		 * 
		 * Obwohl der Standard inline erlaubt, schreibt SIARDSuite alles in lob.
		 * 
		 * Das hat zur folge, dass alles nicht mehr lesbar ist, die Dateien riesig
		 * werden und das Handling dadurch extrem erschwert wird.
		 * 
		 * Im Schnitt wird die Datei 99% kleiner.
		 * 
		 * <c1>1</c1> <c2 digest="8CD479FAC7B2CAE778FB7130AD5AAA43" digestType="MD5"
		 * file="content/schema0/table0/lob1/record0.txt" length="7"/>
		 * 
		 * 
		 * 
		 * 1) extrahiertes in output kopieren
		 * 
		 * 2) wenn lenth <1000 dann integrieren (wenn unbekannt auslesen)
		 * 
		 * 3) lob-Datei mit digest kontrollieren
		 * 
		 * 4) wenn korrekt inhalt der lob mit digest, digestType, file und length
		 * ersetzen
		 */

		cRecInLine = 0;

		// Arbeitsverzeichnis wo das entpackte SIARD zur Reparatur liegt (Original)
		String pathToWorkDir = configMap.get("PathToWorkDir");
		File tmpDir = new File(pathToWorkDir + File.separator + "SIARD");

		// Output-Arbeitsverzeichnis zur Reparatur erstellen
		String name = valDatei.getName().replace(".siard", "");
		String pathToWorkDirOut1Temp = directoryOfLogfile.getAbsolutePath() + File.separator + "OUTPUT-Temp";
		File fileToWorkDirOut1Temp = new File(pathToWorkDirOut1Temp);
		if (fileToWorkDirOut1Temp.exists()) {
			Util.deleteDir(fileToWorkDirOut1Temp);
		}
		if (!fileToWorkDirOut1Temp.exists()) {
			fileToWorkDirOut1Temp.mkdir();
		}
		String pathToWorkDirOutTemp = fileToWorkDirOut1Temp.getAbsolutePath() + File.separator + name;
		File fileToWorkDirOutTemp = new File(pathToWorkDirOutTemp);
		if (fileToWorkDirOutTemp.exists()) {
			Util.deleteDir(fileToWorkDirOutTemp);
		}
		if (!fileToWorkDirOutTemp.exists()) {
			fileToWorkDirOutTemp.mkdir();
		}

		// Output-Verzeichnis erstellen
		File parentLog = new File(directoryOfLogfile.getParent());
		String pathToWorkDirOut = parentLog.getAbsolutePath() + File.separator + "OUTPUT";
		File fileToWorkDirOut = new File(pathToWorkDirOut);
		if (!fileToWorkDirOut.exists()) {
			fileToWorkDirOut.mkdir();
		}

		// Zieldatei
		File siardFile = new File(pathToWorkDirOut + File.separator + name + ".siard");
		if (siardFile.exists()) {
			// von einem vorherigen durchlauf
			Util.deleteFile(siardFile);
			Util.deleteDir(siardFile);
		}

		String doRepairSiard = "subStart";

		try {
			// 1) extrahiertes in output kopieren
			Util.copyDir(tmpDir, fileToWorkDirOutTemp);

			File metadataXml = new File(new StringBuilder(pathToWorkDirOutTemp).append(File.separator).append("header")
					.append(File.separator).append("metadata.xml").toString());
			InputStream fin = new FileInputStream(metadataXml);
			SAXBuilder builder = new SAXBuilder();
			org.jdom2.Document document = builder.build(fin);

			/*
			 * read the document and for each schema and table entry verify existence in
			 * temporary extracted structure
			 */
			boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			Namespace ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			if (version1) {
				// ns = Namespace.getNamespace(
				// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			} else if (version2) {
				ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			}
			// select schema elements and loop
			List<Element> schemas = ((org.jdom2.Document) document).getRootElement().getChild("schemas", ns)
					.getChildren("schema", ns);
			for (Element schema : schemas) {
				Element schemaFolder = schema.getChild("folder", ns);
				File schemaPath = new File(new StringBuilder(pathToWorkDirOutTemp).append(File.separator)
						.append("content").append(File.separator).append(schemaFolder.getText()).toString());
				if (schemaPath.isDirectory()) {
					if (schema.getChild("tables", ns) != null) {

						Element[] tables = schema.getChild("tables", ns).getChildren("table", ns)
								.toArray(new Element[0]);
						for (Element table : tables) {
							Element tableFolder = table.getChild("folder", ns);
							File tablePath = new File(new StringBuilder(schemaPath.getAbsolutePath())
									.append(File.separator).append(tableFolder.getText()).toString());
							if (tablePath.isDirectory()) {
								File tableXml = new File(new StringBuilder(tablePath.getAbsolutePath())
										.append(File.separator).append(tableFolder.getText() + ".xml").toString());

								if ((configMap.get("siardlobrep").equals("yes")
										|| configMap.get("siardlobextrep").equals("yes")) && cRecTot > 0) {

									// <c1>1</c1>
									// <c2 digest="8CD479FAC7B2CAE778FB7130AD5AAA43" digestType="MD5"
									// file="content/schema0/table0/lob1/record0.txt" length="7"/>

									InputStream finTab = new FileInputStream(tableXml);
									SAXBuilder builderTab = new SAXBuilder();
									org.jdom2.Document documentTab = builderTab.build(finTab);

									// select row elements and loop
									List<Element> tableTab = ((org.jdom2.Document) documentTab).getRootElement()
											.getChildren();
									for (Element row : tableTab) {
										List<Element> cells = row.getChildren();
										for (int y = 0; y < cells.size(); y++) {
											Element cell = cells.get(y);
											String cellAttFile = "xy";
											String cellAttDigest = "xy";
											String cellAttDigestType = "xy";
											String cellAttLength = "xy";

											cellAttFile = cell.getAttributeValue("file");
											cellAttDigest = cell.getAttributeValue("digest");
											cellAttDigestType = cell.getAttributeValue("digestType");
											cellAttLength = cell.getAttributeValue("length");

											String cellAttFileS = cellAttFile + " ";
											String cellAttDigestS = cellAttDigest + " ";
											String cellAttDigestTypeS = cellAttDigestType + " ";
											String cellAttLengthS = cellAttLength + " ";

											if ((cellAttFileS).equals("null ")) {
												// keine Aktion noetig
											} else {
												if (configMap.get("siardlobrep").equals("yes")) {
													File lobFile = new File(
															pathToWorkDirOutTemp + File.separator + cellAttFile);
													// separate LOB

													boolean replace = false;

													// wenn lenth <1000 dann integrieren (wenn unbekannt auslesen)
													if ((cellAttLengthS).equals("null ")) {
														int lobStringLenth = Files
																.readString(lobFile.toPath(), StandardCharsets.UTF_8)
																.length();
														if (lobStringLenth < 1000) {
															if ((cellAttDigestS).equals("null ")
																	|| (cellAttDigestTypeS).equals("null ")) {
																replace = true;
															} else {
																// digestType (MD5, SHA-1, or SHA-256)
																String hashFile = "99";
																if (cellAttDigestType.equalsIgnoreCase("md5")) {
																	hashFile = Hash.getMd5(lobFile);
																} else if (cellAttDigestType
																		.equalsIgnoreCase("sha-1")) {
																	hashFile = Hash.getSha1(lobFile);
																} else if (cellAttDigestType
																		.equalsIgnoreCase("sha-256")) {
																	hashFile = Hash.getSha256(lobFile);
																}
																if (hashFile.equalsIgnoreCase(
																		cellAttDigest.toLowerCase())) {
																	replace = true;
																}
															}
														}
													} else {
														int cellAttLengthInt = Integer.parseInt(cellAttLength);
														// System.out.println("cellAttLength "+cellAttLength +"
														// cellAttLengthInt
														// "+cellAttLengthInt);
														if (cellAttLengthInt < 1000) {
															if ((cellAttDigestS).equals("null ")
																	|| (cellAttDigestTypeS).equals("null ")) {
																replace = true;
															} else {
																// digestType (MD5, SHA-1, or SHA-256)
																String hashFile = "99";
																if (cellAttDigestType.equalsIgnoreCase("md5")) {
																	hashFile = Hash.getMd5(lobFile);
																} else if (cellAttDigestType
																		.equalsIgnoreCase("sha-1")) {
																	hashFile = Hash.getSha1(lobFile);
																} else if (cellAttDigestType
																		.equalsIgnoreCase("sha-256")) {
																	hashFile = Hash.getSha256(lobFile);
																}
																if (hashFile.equalsIgnoreCase(
																		cellAttDigest.toLowerCase())) {
																	replace = true;
																}
															}
														}
													}

													if (replace) {
														String lobString = Files.readString(lobFile.toPath(),
																StandardCharsets.UTF_8);
														cell.setText(lobString);
														cell.removeAttribute("file");
														cell.removeAttribute("digest");
														cell.removeAttribute("digestType");
														cell.removeAttribute("length");

														// Save xml writing the modified content into XML file
														TransformerFactory transformerFactory = TransformerFactory
																.newInstance();
														Transformer transformer = transformerFactory.newTransformer();
														FileOutputStream output = new FileOutputStream(
																tableXml.getAbsolutePath());
														JDOMSource source = new JDOMSource(documentTab);
														StreamResult result = new StreamResult(output);
														transformer.transform(source, result);
														lobFile.delete();
														cRecInLine++;
													} else {
														/*
														 * LOB wird nicht integriert. In diesem Fall soll in einem
														 * weiteren Schritt die Dateiendung kontrolliert und ggf.
														 * korrigiert werden.
														 */
														if (configMap.get("siardlobextrep").equals("yes")) {
															String recFormat = "new";
															recFormat = Recognition.formatRec(lobFile);
															String extRec = recFormat.replace("_ext", "").toLowerCase();
															if (recFormat.contains("_ext")) {
																cExt++;
																// Erkannt aber nicht exakte Dateiendung

																// File lobFile = new File(
																// pathToWorkDirOutTemp + File.separator + cellAttFile);

																// <c8 file="content/schema0/table0/lob7/record99.txt"
																// length="13352"/>
																String wrongExt10 = cellAttFile
																		.substring(cellAttFile.lastIndexOf(".") - 10);
																String wrongExt = cellAttFile
																		.substring(cellAttFile.lastIndexOf(".") + 1);
																String repExt10 = wrongExt10.replace(wrongExt, extRec);
																String cellAttFileRep = cellAttFile.replace(wrongExt10,
																		repExt10);
																cell.removeAttribute("file");
																cell.setAttribute("file", cellAttFileRep);

																// Save xml writing the modified content into XML file
																TransformerFactory transformerFactory = TransformerFactory
																		.newInstance();
																Transformer transformer = transformerFactory
																		.newTransformer();
																FileOutputStream output = new FileOutputStream(
																		tableXml.getAbsolutePath());
																JDOMSource source = new JDOMSource(documentTab);
																StreamResult result = new StreamResult(output);
																transformer.transform(source, result);

																int i = lobFile.getName().lastIndexOf('.');
																String nameRep = lobFile.getName().substring(0, i);
																String nameRepExt = nameRep + "." + extRec;
																File lobFileRep = new File(lobFile.getParent(),
																		nameRepExt);

																lobFile.renameTo(lobFileRep);
															}
														}
													}
												} else {
													// nur lob-Extension reparatur
													File lobFile = new File(
															pathToWorkDirOutTemp + File.separator + cellAttFile);
													// separate LOB

													/*
													 * LOB wird nicht integriert. In diesem Fall soll die Dateiendung
													 * kontrolliert und ggf. korrigiert werden.
													 */
													if (configMap.get("siardlobextrep").equals("yes")) {

														String recFormat = "new";
														recFormat = Recognition.formatRec(lobFile);
														String extRec = recFormat.replace("_ext", "").toLowerCase();
														if (recFormat.contains("_ext")) {
															cExt++;
															// Erkannt aber nicht exakte Dateiendung

															// File lobFile = new File(
															// pathToWorkDirOutTemp + File.separator + cellAttFile);

															// <c8 file="content/schema0/table0/lob7/record99.txt"
															// length="13352"/>
															String wrongExt10 = cellAttFile
																	.substring(cellAttFile.lastIndexOf(".") - 10);
															String wrongExt = cellAttFile
																	.substring(cellAttFile.lastIndexOf(".") + 1);
															String repExt10 = wrongExt10.replace(wrongExt, extRec);
															String cellAttFileRep = cellAttFile.replace(wrongExt10,
																	repExt10);
															cell.removeAttribute("file");
															cell.setAttribute("file", cellAttFileRep);

															// Save xml writing the modified content into XML file
															TransformerFactory transformerFactory = TransformerFactory
																	.newInstance();
															Transformer transformer = transformerFactory
																	.newTransformer();
															FileOutputStream output = new FileOutputStream(
																	tableXml.getAbsolutePath());
															JDOMSource source = new JDOMSource(documentTab);
															StreamResult result = new StreamResult(output);
															transformer.transform(source, result);

															int i = lobFile.getName().lastIndexOf('.');
															String nameRep = lobFile.getName().substring(0, i);
															String nameRepExt = nameRep + "." + extRec;
															File lobFileRep = new File(lobFile.getParent(), nameRepExt);

															lobFile.renameTo(lobFileRep);
														}
													}
												}
											}
										}
									}
									finTab.close();
									// set to null
									finTab = null;

								}
								if (configMap.get("siardrowsrep").equals("yes") && emptyRows) {
									/*
									 * teilweise wird rows in metadata.xml nicht befuellt (Fehler in F)
									 * 
									 * Nachfolgend wird dies in der neuen SIARD-Kopie behoben
									 */

									// "<rows />
									// "<rows/>"
									// "<rows></rows>"
									// <rows> </rows>"

									int cRow = 0;

									Element tableRows = table.getChild("rows", ns);
									String rowsValue = tableRows.getValue();
									if (rowsValue.isBlank() || rowsValue.isEmpty() || rowsValue.equals(" ")) {
										cEmptyRows++;
										// dies ist die passende tableXml
										BufferedReader reader = new BufferedReader(new FileReader(tableXml));
										String line = "";
										while ((line = reader.readLine()) != null) {
											if (line.contains("<row>")) {
												cRow++;
											}
										}
										reader.close();
										tableRows.setText(cRow + "");
										// Save xml writing the modified content into XML file
										TransformerFactory transformerFactory = TransformerFactory.newInstance();
										Transformer transformer = transformerFactory.newTransformer();
										FileOutputStream output = new FileOutputStream(metadataXml.getAbsolutePath());
										JDOMSource source = new JDOMSource(document);
										StreamResult result = new StreamResult(output);
										transformer.transform(source, result);
									}
								}
							}
						}
					} else {
						// kein Fehler sondern leeres Schema
					}
				}
			}

			fin.close();
			// set to null
			fin = null;

		} catch (FileNotFoundException e) {
			doRepairSiard = e.getMessage() + " (Repair: FileNotFoundException)";
		} catch (IOException e) {
			doRepairSiard = e.getMessage() + " (Repair: IOException)";
		} catch (JDOMException e) {
			doRepairSiard = e.getMessage() + " (Repair: JDOMException)";
		} catch (InterruptedException e) {
			doRepairSiard = e.getMessage() + " (Repair: InterruptedException)";
		} catch (TransformerConfigurationException e) {
			doRepairSiard = e.getMessage() + " (Repair: TransformerConfigurationException)";
		} catch (TransformerException e) {
			doRepairSiard = e.getMessage() + " (Repair: TransformerException)";
		}

		if (fileToWorkDirOutTemp.exists()) {
			doRepairSiard = "Existiert";
			if (cRecInLine > 0 || emptyRows || cExt > 0) {
				// Zippen mit "Apache Commons Compress"
				// new Expander().expand(archive, destination);

				File sourceDir = fileToWorkDirOutTemp;

				File zipFile = new File(pathToWorkDirOut + File.separator + name + ".zip");
				if (zipFile.exists()) {
					// von einem vorherigen durchlauf
					Util.deleteDir(zipFile);
				}
				if (siardFile.exists()) {
					// von einem vorherigen durchlauf
					Util.deleteFile(siardFile);
					Util.deleteDir(siardFile);
				}

				mainCreateZip(sourceDir, zipFile, pathToWorkDirOutTemp);
				if (zipFile.exists()) {
					zipFile.renameTo(new File(pathToWorkDirOut + File.separator + name + ".siard"));
					doRepairSiard = zipFile.getAbsolutePath();
				}
			} else {
				doRepairSiard = "keine";
			}
		}
		// Output-Arbeitsverzeichnis loeschen
		if (fileToWorkDirOutTemp.exists()) {
			Util.deleteDir(fileToWorkDirOutTemp);
		}
		if (fileToWorkDirOut1Temp.exists()) {
			Util.deleteDir(fileToWorkDirOut1Temp);
		}
		if (fileToWorkDirOutTemp.exists()) {
			Util.deleteDir(fileToWorkDirOutTemp);
		}
		if (fileToWorkDirOut1Temp.exists()) {
			Util.deleteDir(fileToWorkDirOut1Temp);
		}

		return doRepairSiard;
	}

	public static void mainCreateZip(File sourceDir, File zipFile, String rootName) {
		// TODO - mainCreateZip
		String[] files = {};
		String[] directories = { sourceDir.getAbsolutePath() };

		try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {

			// Adding files
			for (String file : files) {
				Path filePath = Paths.get(file);
				if (!Files.exists(filePath)) {
					System.err.println("File does not exists: " + filePath);
					// throw new FileNotFoundException("File does not exists: " + filePath);
				}
				addToZipFile(filePath, zos, rootName);
			}

			// Adding directories
			for (String dir : directories) {
				Path dirPath = Paths.get(dir);
				if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
					System.err.println("Directory does not exist: " + dirPath);
					// throw new IOException("Directory does not exist: " + dirPath);
				}

				try {
					List<Path> allPaths = new ArrayList<>();
					Files.walk(dirPath).forEach(allPaths::add);

					for (Path path : allPaths) {
						File child = new File(path.toString());
						if (!Files.isDirectory(path)) {
							addToZipFile(path, zos, rootName);
						} else if (child.isDirectory() && child.list().length == 0
								&& !child.getName().contains("lob")) {
							// leere Ordner anlegen (z.B. Version)
							String pathStr = path.toString().replace("\\", "/");
							rootName = rootName.replace("\\", "/");
							// root Verzeichnis nicht schreiben
							pathStr = pathStr.replace((rootName + "/"), "");
							zos.putNextEntry(new ZipEntry(pathStr + "/"));
							zos.closeEntry();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Repair (ZIP): mainCreateZip) ");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addToZipFile(Path file, ZipOutputStream zos, String rootName) {
		// TODO - addToZipFile
		try (FileInputStream fis = new FileInputStream(file.toFile())) {
			// Replace backslashes with forward slashes for compatibility
			String zipEntryName = file.toString().replace("\\", "/");
			rootName = rootName.replace("\\", "/");
			// root Verzeichnis nicht schreiben
			zipEntryName = zipEntryName.replace((rootName + "/"), "");

			zos.putNextEntry(new ZipEntry(zipEntryName));

			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			zos.closeEntry();
		} catch (IOException e) {
			System.err.println("Failed to zip file: " + file);
			e.printStackTrace();
		}
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		// TODO - newFile
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

}
