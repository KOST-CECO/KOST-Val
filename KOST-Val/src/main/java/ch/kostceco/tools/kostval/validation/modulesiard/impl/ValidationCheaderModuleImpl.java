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
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.fileservice.Xmllint;
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

	@SuppressWarnings({ "resource", "unused" })
	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationCheaderException {
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
		 */
		try {
			ZipFile zf = new ZipFile(valDatei.getAbsolutePath());
			Enumeration<? extends ZipEntry> entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zEntry = entries.nextElement();
				String fileName = zEntry.getName();
				if (fileName.contains("record") && fileName.contains(".txt")) {
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
					 * TODO Dieses wird auch von SIARD Suite verwendet bei der erstellung dieser
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
		return result;
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

}
