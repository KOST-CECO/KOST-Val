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

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pdftools.FileStream;
import com.pdftools.NativeLibrary;
import com.pdftools.Stream;
import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kosttools.fileservice.verapdf;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kosttools.util.UtilCallas;
import ch.kostceco.tools.kosttools.util.UtilCharacter;
import ch.kostceco.tools.kosttools.util.UtilTranslate;
import ch.kostceco.tools.kostval.controller.Controllervalfofile;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfavalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAvalidationAiModule;

/**
 * Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit
 * veraPDF und oder callas.
 * 
 * Folgendes ist Konfigurierbar: welche Validatoren verwendet werden sollen.
 * Sollen beide verwendet werden wird die Duale Validierung durchgefuehrt. Bei
 * der dualen Validierung muessen beide Validatoren die Datei als invalide
 * betrachten, damit diese als invalid gilt. Bei Uneinigkeit gilt diese als
 * valid.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * veraPDF und oder callas. Die Fehler werden den Einzelnen Gruppen (Modulen)
 * zugeordnet
 * 
 * Direkt nach der Erkennung wird noch ausgegeben ob signaturen enthalten sind
 * oder nicht
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationAiModuleImpl extends ValidationModuleImpl implements ValidationAvalidationAiModule {

	public static String NEWLINE = System.getProperty("line.separator");

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationApdfavalidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}
		String errorK = "";
		// Create object
		PdfValidatorAPI docPdf = null;

		// Version & Level herausfinden
		String pdfa1a = configMap.get("pdfa1a");
		String pdfa1b = configMap.get("pdfa1b");
		String pdfa2a = configMap.get("pdfa2a");
		String pdfa2b = configMap.get("pdfa2b");
		String pdfa2u = configMap.get("pdfa2u");
		String warning3to2 = configMap.get("warning3to2");

		Integer pdfaVer1 = 0;
		Integer pdfaVer2 = 0;

		String pdfaCl = "B";

		String pathToLogDir = configMap.get("PathToLogfile");
		String pathToWorkDirValdatei = configMap.get("PathToWorkDir");

		String pathToWorkDir = pathToLogDir;
		/*
		 * Beim schreiben ins Workverzeichnis trat ab und zu ein fehler auf.
		 * entsprechend wird es jetzt ins logverzeichnis geschrieben
		 */

		String pdf3warning = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
				+ getTextResourceService().getText(locale, WARNING_XML_A_PDFA3);

		File callasNo = new File(pathToWorkDir + File.separator + "_callas_NO.txt");

		String pathToPdfapilotOutput = pathToLogDir + File.separator + "callasTEMP.txt";
		File report = new File(pathToPdfapilotOutput);
		String pathToPdfapilotOutputReport = pathToLogDir + File.separator + "callasTEMPreport.txt";
		File reportOriginal = new File(pathToPdfapilotOutputReport);

		File verapdfReportFile = new File(pathToLogDir + File.separator + "veraPDF.xml");

		// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
		// loeschen wir es
		if (report.exists()) {
			report.delete();
		}
		if (reportOriginal.exists()) {
			reportOriginal.delete();
		}
		if (verapdfReportFile.exists()) {
			verapdfReportFile.delete();
		}

		/*
		 * Neu soll die Validierung konfigurier bar sein Moegliche Werte 1A, 1B und no
		 * sowie 2A, 2B, 2U und no Da Archive beide Versionen erlauben koennen sind es 2
		 * config eintraege Es gibt mehre Moeglichkeiten das PDF in der gewuenschten
		 * Version zu testen - Unterscheidung anhand PDF/A-Eintrag
		 */
		if (pdfa2a.equals("2A") || pdfa2b.equals("2B") || pdfa2u.equals("2U")) {
			// gueltiger Konfigurationseintrag und V2 erlaubt
			pdfaVer2 = 2;
		} else {
			// v2 nicht erlaubt oder falscher eintrag
			pdfa2a = "no";
			pdfa2b = "no";
			pdfa2u = "no";
		}
		if (pdfa1a.equals("1A") || pdfa1b.equals("1B")) {
			// gueltiger Konfigurationseintrag und V1 erlaubt
			pdfaVer1 = 1;
		} else {
			// v1 nicht erlaubt oder falscher eintrag
			pdfa1a = "no";
			pdfa1b = "no";
		}
		if (pdfaVer1 == 0 && pdfaVer2 == 0) {
			// keine Validierung moeglich. keine PDFA-Versionen konfiguriert
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_A_PDFA_NOCONFIG));
				return false;
			}
		}

		String level = "no";
		// Richtiges Level definieren
		try {
			// Level je nach File auswaehlen
			int pdfaVer = 0;
			BufferedReader in = new BufferedReader(new FileReader(valDatei));
			String line;
			while ((line = in.readLine()) != null) {
				// haeufige Partangaben: pdfaid:part>1< pdfaid:part='1'
				// pdfaid:part="1"

				// <pdfaid:part>2</pdfaid:part>
				// <pdfaid:conformance>U</pdfaid:conformance>

				if (line.contains("pdfaid:part")) {
					// pdfaid:part
					if (line.contains("pdfaid:part>1<") || line.contains("pdfaid:part='1'")
							|| line.contains("pdfaid:part=\"1\"")) {
						pdfaVer = 1;
					} else if (line.contains("pdfaid:part>2<") || line.contains("pdfaid:part='2'")
							|| line.contains("pdfaid:part=\"2\"")) {
						pdfaVer = 2;
					} else if (line.contains("pdfaid:part>3<") || line.contains("pdfaid:part='3'")
							|| line.contains("pdfaid:part=\"3\"")) {
						// 3 wird nie akzeptier, Validierung gegen 2
						pdfaVer = 2;
						if (warning3to2.equalsIgnoreCase("yes")) {
							// Fehler betreffend Version 3 statt 2 wird
							// ignoriert.
							// Es wird eine Warnung ausgegeben.
							Logtxt.logtxt(logFile, pdf3warning);
						}
					} else if (line.contains("pdfaid:part") && line.contains("1")) {
						// PDFA-Version = 1
						pdfaVer = 1;
					} else if (line.contains("pdfaid:part") && line.contains("2")) {
						// PDFA-Version = 2
						pdfaVer = 2;
					}
				}
				if (line.contains("pdfaid:conformance")) {
					// pdfaid:part
					if (line.contains("pdfaid:conformance>A<") || line.contains("pdfaid:conformance='A'")
							|| line.contains("pdfaid:conformance=\"A\"") || line.contains("pdfaid:conformance>a<")
							|| line.contains("pdfaid:conformance='a'") || line.contains("pdfaid:conformance=\"a\"")) {
						pdfaCl = "A";
					} else if (line.contains("pdfaid:conformance>U<") || line.contains("pdfaid:conformance='U'")
							|| line.contains("pdfaid:conformance=\"U\"") || line.contains("pdfaid:conformance>u<")
							|| line.contains("pdfaid:conformance='u'") || line.contains("pdfaid:conformance=\"u\"")) {
						pdfaCl = "U";
					} else {
						pdfaCl = "B";
					}
				}
				if (pdfaVer == 0) {
					// der Part wurde nicht gefunden --> Level 2B
					pdfaVer = 2;
				}
				level = pdfaVer + pdfaCl;
				if (level == "1U") {
					level = "1B";
				}
			}
			// System.out.println( " " );
			// System.out.print( " Level " + level + " geaendert ... " );
			if (level.toLowerCase().contains("1a")) {
				// wurde als 1A erkannt, wenn erlaubt als 1a validieren
				if (pdfa1a.equals("1A")) {
					// 1A erlaubt, Level 1A bleibt
				} else {
					if (pdfa1b.equals("1B")) {
						level = "1B";
					} else if (pdfa2a.equals("2A")) {
						level = "2A";
					} else if (pdfa2u.equals("2U")) {
						level = "2U";
					} else {
						level = "2B";
					}
				}
			}
			if (level.toLowerCase().contains("1b")) {
				// wurde als 1B erkannt, wenn erlaubt als 1b validieren
				if (pdfa1b.equals("1B")) {
					// 1B erlaubt, Level 1B bleibt
				} else {
					if (pdfa1a.equals("1A")) {
						level = "1A";
					} else if (pdfa2b.equals("2B")) {
						level = "2B";
					} else if (pdfa2u.equals("2U")) {
						level = "2U";
					} else {
						level = "2A";
					}
				}
			}
			if (level.toLowerCase().contains("2a")) {
				// wurde als 2A erkannt, wenn erlaubt als 2a validieren
				if (pdfa2a.equals("2A")) {
					// 2A erlaubt, Level 2A bleibt
				} else {
					if (pdfa2u.equals("2U")) {
						level = "2U";
					} else if (pdfa2b.equals("2B")) {
						level = "2B";
					} else if (pdfa1a.equals("1A")) {
						level = "1A";
					} else {
						level = "1B";
					}
				}
			}
			if (level.toLowerCase().contains("2u")) {
				// wurde als 2U erkannt, wenn erlaubt als 2u validieren
				if (pdfa2u.equals("2U")) {
					// 2U erlaubt, Level 2U bleibt
				} else {
					if (pdfa2b.equals("2B")) {
						level = "2B";
					} else if (pdfa2a.equals("2A")) {
						level = "2A";
					} else if (pdfa1a.equals("1A")) {
						level = "1A";
					} else {
						level = "1B";
					}
				}
			}
			if (level.toLowerCase().contains("2b")) {
				// wurde als 2B erkannt, wenn erlaubt als 2b validieren
				if (pdfa2b.equals("2B")) {
					// 2B erlaubt, Level 2B bleibt
				} else {
					if (pdfa2u.equals("2U")) {
						level = "2U";
					} else if (pdfa2a.equals("2A")) {
						level = "2A";
					} else if (pdfa1b.equals("1B")) {
						level = "1B";
					} else {
						level = "1A";
					}
				}
			}
			// System.out.println( " --> " + level + "!" );
			in.close();
			// set to null
			in = null;

		} catch (Throwable e) {
			if (min) {
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "Version " + e.getMessage()));
			}
		}

		Logtxt.logtxt(logFile, "<FormatVL>-" + level + "</FormatVL>");

		// Die Erkennung erfolgt bereits im Vorfeld

		/*
		 * Fuer das weitere Vorgehen ist es wichtig zu wissen ob Signaturen enthalten
		 * sind.
		 * 
		 * Entsprechend werden diese jetzt hier ermittelt:
		 * 
		 * ggf auch direkt validieren und dokumentieren
		 */
		String egovdvMsg = "";
		try {
			egovdvMsg = Controllervalfofile.valFoFileEgodv(valDatei, directoryOfLogfile, dirOfJarPath, configMap,
					locale);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Fehler beim auslesen der config (pdfa)");
		}
		if (egovdvMsg == "NoSignature") {
			// keine Signature
		} else {
			Logtxt.logtxt(logFile, egovdvMsg);
		}

		boolean isValid = false;
		boolean isValidverapdf = false;
		boolean isValidCallas = false;
		boolean isValidVa = true;
		boolean isValidVb = true;
		boolean isValidVc = true;
		boolean isValidVd = true;
		boolean isValidVe = true;
		boolean isValidVf = true;
		boolean isValidVg = true;
		boolean isValidVh = true;
		boolean isValidVi = true;
		boolean isValidCa = true;
		boolean isValidCb = true;
		boolean isValidCc = true;
		boolean isValidCd = true;
		boolean isValidCe = true;
		boolean isValidCf = true;
		boolean isValidCg = true;
		boolean isValidCh = true;
		boolean isValidCi = true;
		boolean isValidJ = true;
		String isValidFont = "noFontVal";
		String fontYesNo = configMap.get("pdfafont");
		boolean ignorUndefinied = false;
		// String undefiniedWarningString = "";
		// int symbolWarning = 0;
		// String symbolWarningString = "";
		String fontErrorIgnor = "I";
		boolean callas = false;
		boolean verapdfBo = false;
		int callasReturnCode = 9;
		int callasReturnCodeTest = 9;
		boolean callasServiceFailed = false;

		String verapdfA = "";
		String verapdfB = "";
		String verapdfC = "";
		String verapdfD = "";
		String verapdfE = "";
		String verapdfF = "";
		String verapdfG = "";
		String verapdfH = "";
		String verapdfI = "";
		String callasA = "";
		String callasB = "";
		String callasC = "";
		String callasD = "";
		String callasE = "";
		String callasF = "";
		String callasG = "";
		String callasH = "";
		String callasI = "";

		String verapdfConfig = configMap.get("verapdf");
		String callasConfig = configMap.get("callas");
		String detailConfig = configMap.get("detail");

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		if (callasConfig.contentEquals("yes")) {
			// callas Validierung gewuenscht
			if (callasNo.exists()) {
				/*
				 * Callas wurde in einem frueheren durchgang getestet und es funktioniert bei
				 * dem Benutzer nicht korrekt. Entsprechend ist die Validierung mit callas nicht
				 * moeglich
				 */
				callas = false;
			} else {
				callas = true;
			}
		}
		if (verapdfConfig.contentEquals("yes")) {
			// verapdf Validierung gewuenscht
			verapdfBo = true;
		}

		if (!verapdfBo && !callas) {
			// pdf Validierung nicht moeglich
			configMap.put("pdfavalidation", "no");
		}

		try {
			if (verapdfBo) {
				try {
					/*
					 * TODO: Erledigt Start mit veraPDF
					 * 
					 * Wenn veraPDF eingeschaltet ist, wird immer zuerst veraPDF genommen, da dieser
					 * in KOST-Val schneller und auch unlimitiert ist als callas
					 */

					/*
					 * Aktualisieren von verapdf =========================
					 * 
					 * herunterladen von verapdf-gf-installer.zip auf der Seite
					 * https://software.verapdf.org/dev/
					 * 
					 * Installieren. Danach die Datei greenfield-apps-1.27.59.jar vom bin Ordner des
					 * Installationsverzeichnises in die lib kopieren
					 */

					// System.out.println(" initialise VeraGreenfieldFoundryProvider ");

					File workDir = new File(pathToWorkDirValdatei);

					String execVerapdfVal = verapdf.execVerapdfVal(valDatei, workDir, dirOfJarPath, level,
							verapdfReportFile);

					File valDateiNorm = new File(workDir.getAbsolutePath() + File.separator + "veraPDF.pdf");
					String veraPDFvalid = "<validationReports compliant=\"1\" ";
					if (Util.stringInFile(valDateiNorm.getAbsolutePath(), verapdfReportFile)) {
						// System.out.println(" verapdf wurde korrekt durchgefuehrt");
						if (Util.stringInFile(veraPDFvalid, verapdfReportFile)) {
							isValidverapdf = true;
							// System.out.println(valDatei.getName() + " ist gemaess veraPDF eine valide
							// PDF/A-" + level + " Datei!");
						} else {
							isValidverapdf = false;
							// System.out.println(valDatei.getName() + " ist gemaess veraPDF eine invalide
							// PDF/A-" + level + " Datei!");
						}
					} else {
						isValidverapdf = false;
						// System.out.println(" valDatei.getAbsolutePath() wurde nicht im Report
						// gefunden. FEHLER");
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, " (File not in Report)"));
					}
					if (execVerapdfVal.equals("valid") || execVerapdfVal.equals("invalid")) {
						// keine Aktion erforderlich es geht ums else
					} else {
						// Fehlermeldung ausgeben
						isValidverapdf = false;
						verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, execVerapdfVal);
					}

					if (isValidverapdf) {
						// valid Report loeschen
						if (verapdfReportFile.exists()) {
							verapdfReportFile.delete();
						}
					} else {
						// invalid
						// Start unnoetige Zeile zu loeschen
						BufferedReader reader = new BufferedReader(new FileReader(verapdfReportFile));
						String lineModif = "";
						StringBuilder sb = new StringBuilder();
						while ((lineModif = reader.readLine()) != null) {
							if (lineModif.contains("<object>")) {
								// line mit <object> wird nicht behalten
							} else if (lineModif.contains("<test>")) {
								// line mit <test> wird nicht behalten
							} else if (lineModif.contains("<context>")) {
								// line mit <context> wird nicht behalten
							} else if (lineModif.contains("<check ")) {
								// line mit <check> wird nicht behalten
							} else if (lineModif.contains("</check>")) {
								// line mit </check> wird nicht behalten
							} else if (lineModif.contains("<errorMessage>")) {
								// line wird nicht behalten wenn bereits vorhanden
								if (sb.toString().contains(lineModif)) {
									// line bereits vorhanden
								} else {
									sb.append(lineModif);
									sb.append("\r\n");
								}
							} else if (lineModif.contains("<rule ")) {
								// line wird vereinfacht
								String iso1 = "specification=\"ISO 19005-1:2005\"";
								String iso2 = "specification=\"ISO 19005-2:2011\"";
								String iso3 = "specification=\"ISO 19005-3:2012\"";
								String iso4 = "specification=\"ISO 19005-4:2020\"";

								// <rule specification="ISO 19005-1:2005" clause="6.3.7"
								// testNumber="3" status="failed" failedChecks="4">
								lineModif = lineModif.replace("\" testNumber=\"", "_");
								// <rule specification="ISO 19005-1:2005"
								// clause="6.3.7_3" status="failed" failedChecks="4">
								if (lineModif.contains(iso1)) {
									lineModif = lineModif.replace(iso1 + " ", "");
									// <rule clause="6.3.7_3" status="failed"
									// failedChecks="4">
									lineModif = lineModif.replace("\" status=\"failed", "_" + level);
									// <rule clause="6.3.7_3_1" failedChecks="4">
								} else if (lineModif.contains(iso2)) {
									lineModif = lineModif.replace(iso2 + " ", "");
									// <rule clause="6.3.7_3" status="failed"
									// failedChecks="4">
									lineModif = lineModif.replace("\" status=\"failed", "_" + level);
									// <rule clause="6.3.7_3_2" failedChecks="4">
								} else if (lineModif.contains(iso3)) {
									lineModif = lineModif.replace(iso3 + " ", "");
									// <rule clause="6.3.7_3" status="failed"
									// failedChecks="4">
									lineModif = lineModif.replace("\" status=\"failed", "_3");
									// <rule clause="6.3.7_3_3" failedChecks="4">
								} else if (lineModif.contains(iso4)) {
									lineModif = lineModif.replace(iso4 + " ", "");
									// <rule clause="6.3.7_3" status="failed"
									// failedChecks="4">
									lineModif = lineModif.replace("\" status=\"failed", "_4");
									// <rule clause="6.3.7_3_4" failedChecks="4">
								}
								sb.append(lineModif);
								sb.append("\r\n");
								// <rule specification="ISO 19005-1:2005" clause="6.3.7"
								// testNumber="3" status="failed" failedChecks="4">

								// <rule clause="6.3.7_3_1" failedChecks="4">

							} else {
								sb.append(lineModif);
								sb.append("\r\n");
							}
						}
						String modiftext = sb.toString();
						reader.close();
						// set to null
						reader = null;
						FileWriter writer = new FileWriter(verapdfReportFile);
						writer.write(modiftext);
						writer.close();

						// END: unnoetige Zeile zu loeschen

						/*
						 * Moegliche Fehlermeldungen: https://github.com/veraPDF/veraPDF-library/blob/
						 * be36436422591172bdf84137d48597fa54903300/core/src/main/
						 * resources/org/verapdf/pdfa/validation/PDFA-1B.xml#L383
						 * https://github.com/veraPDF/veraPDF-library/tree/
						 * be36436422591172bdf84137d48597fa54903300/core/src/main/
						 * resources/org/verapdf/pdfa/validation
						 */

						// START: verapdfReportFile auslesen und in der gewuenschten
						// Sprache ausgeben (UtilTranslate)

						DocumentBuilderFactory dbfV = DocumentBuilderFactory.newInstance();
						// dbf.setValidating(false);
						DocumentBuilder dbV = dbfV.newDocumentBuilder();
						Document logVera = dbV.parse(verapdfReportFile);
						logVera.getDocumentElement().normalize();
						NodeList nodeLstVr = logVera.getElementsByTagName("rule");

						for (int s = 0; s < nodeLstVr.getLength(); s++) {
							String description = "", descriptionTrans = "", errorMessage = "";
							Node ruleNode = nodeLstVr.item(s);
							// System.out.println(dateiNode.getTextContent());
							NodeList childNodesR = ruleNode.getChildNodes();
							String clause = ruleNode.getAttributes().getNamedItem("clause").getTextContent();
							// System.out.println("clause: " + clause);
							for (int y = 0; y < childNodesR.getLength(); y++) {
								Node subNode = childNodesR.item(y);
								if (subNode.getNodeName().equals("description")) {
									description = subNode.getTextContent();
									// System.out.println("description: " + description);
									description = description.replaceAll("\"", "");
									description = description.replaceAll("'", "");
									// System.out.println("description Norm: " + description);
									/*
									 * description in der gewuenschten Sprache ausgeben.
									 * 
									 * Fehlermeldung besteht aus der uebersetzten description mit zusaetzlich dem
									 * passenden Modul.
									 */
									if (locale.toString().startsWith("de")) {
										descriptionTrans = UtilTranslate.enTOde(description);
									} else if (locale.toString().startsWith("fr")) {
										descriptionTrans = UtilTranslate.enTOfr(description);
									} else if (locale.toString().startsWith("it")) {
										descriptionTrans = UtilTranslate.enTOit(description);
									} else {
										descriptionTrans = description;
									}
									descriptionTrans = descriptionTrans.replace("..", ".");
								} else if (subNode.getNodeName().equals("errorMessage")) {
									errorMessage = "";
									if (detailConfig.equalsIgnoreCase("detail")
											|| detailConfig.equalsIgnoreCase("yes")) {
										errorMessage = subNode.getTextContent();
										// System.out.println("errorMessage: " + errorMessage);
										// Detailfehlermeldung in englisch ausgeben mit - vorangestellt
										errorMessage = "<Message> - " + errorMessage + "</Message>";
										// System.out.println("errorMessage: " + errorMessage);
									}
									// Start Modulzuordnung und Erstellung Fehlermeldung
									/*
									 * Die description anhand von Woerter den Modulen zuordnen
									 * 
									 * A) Allgemeines B) Struktur C) Grafiken D) Schrift E) transparen F) annotation
									 * G) aktion H) metadata I) Zugaenglichkeit
									 */

									if (warning3to2.equalsIgnoreCase("yes") && description.contains(
											"The value of pdfaid:part shall be the part number of ISO 19005 to which the file conforms")
											&& errorMessage.contains(
													"the PDF/A Identification Schema is 3 instead of 2 for PDF/A-2 conforming file")) {
										verapdfA = verapdfA
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error><Warning>warning</Warning>";
									} else if (description.toLowerCase().contains("schrift")
											|| description.toLowerCase().contains("police")
											|| description.toLowerCase().contains("font")
											|| description.toLowerCase().contains("gly")
											|| description.toLowerCase().contains("truetype")
											|| description.toLowerCase().contains("unicode")
											|| description.toLowerCase().contains("cid")
											|| description.toLowerCase().contains("charset")) {
										verapdfD = verapdfD
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVd = false;
									} else if (description.toLowerCase().contains("graphic")
											|| description.toLowerCase().contains("image")
											|| description.toLowerCase().contains("icc")
											|| description.toLowerCase().contains("color")
											|| description.toLowerCase().contains("rgb")
											|| description.toLowerCase().contains("rvb")
											|| description.toLowerCase().contains("cmyk")
											|| description.toLowerCase().contains("cmjn")
											|| description.toLowerCase().contains("outputintent")
											|| description.toLowerCase().contains("jpeg2000")
											|| description.toLowerCase().contains("devicegray")
											|| description.toLowerCase().contains("tr2")) {
										verapdfC = verapdfC
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVc = false;
									} else if (description.toLowerCase().contains("disponibi")
											|| description.toLowerCase().contains("accessibi")
											|| description.toLowerCase().contains("markinfo")
											|| description.toLowerCase().contains("structree")
											|| description.toLowerCase().contains("structure tree root")) {
										verapdfI = verapdfI
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_I_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVi = false;
									} else if (description.toLowerCase().contains("struktur")
											|| description.toLowerCase().contains("structur")
											|| description.toLowerCase().contains("lzw")
											|| description.toLowerCase().contains("xref")
											|| description.toLowerCase().contains(" eol")) {
										verapdfB = verapdfB
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVb = false;
									} else if (description.toLowerCase().contains("metad")
											|| description.toLowerCase().contains("xmp")) {
										verapdfH = verapdfH
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVh = false;
									} else if (description.toLowerCase().contains("transparen")
											|| description.toLowerCase().contains(" ca key ")) {
										verapdfE = verapdfE
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVe = false;
									} else if (description.toLowerCase().contains("aktion")
											|| description.toLowerCase().contains("action")
											|| description.toLowerCase().contains("aa")
											|| description.toLowerCase().contains("javascript")) {
										verapdfG = verapdfG
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVg = false;
									} else if (description.toLowerCase().contains("annotation")
											|| description.toLowerCase().contains("embedd")
											|| description.toLowerCase().contains("comment")
											|| description.toLowerCase().contains("print")
											|| description.toLowerCase().contains("incorpor")) {
										verapdfF = verapdfF
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
												+ "<Message>" + descriptionTrans + "</Message>" + errorMessage
												+ "</Error>";
										isValidVf = false;
									} else {
										verapdfA = verapdfA
												+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ "<Message>" + descriptionTrans + " [verapdf " + clause + "]</Message>"
												+ errorMessage + "</Error>";
										isValidVa = false;
									}
									// Ende Modulzuordnung und Erstellung Fehlermeldung
								}
							}
						}
						// END: verapdfReportFile auslesen und in der gewuenschten Sprache ausgeben
					}
					/*
					 * durch die diversen nacharbeiten (Warnung anstelle Fehler) muss kontrolliert
					 * werden ob es valide mit Warnungen oder Invalide ist.
					 */
					if (isValidVa && isValidVb && isValidVc && isValidVd && isValidVe && isValidVf && isValidVg
							&& isValidVh && isValidVi) {
						isValidverapdf = true;
					}

					if (verapdfReportFile.exists()) {
						verapdfReportFile.delete();
					}
				} catch (Exception e) {
					isValidverapdf = false;
					/*
					 * Logtxt.logtxt(logFile, getTextResourceService().getText(locale,
					 * MESSAGE_XML_MODUL_A_PDFA) + getTextResourceService().getText(locale,
					 * ERROR_XML_UNKNOWN, "Exception (veraPDF): " + e.getMessage()));
					 */
					verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
									"</Message><Message> - Exception (veraPDF): " + e.getMessage());
				}
			} else {
				isValidverapdf = false;
			}
			if (verapdfBo) {
				if (!isValidverapdf) {
					// veraPDF eingeschaltet aber nicht bestanden.

					/*
					 * Kontrolle ob ein Fehler gespeichert wurde. Wenn nicht eine allg.
					 * Fehlermeldung ausgeben.
					 */
					String verapdfABCDEFGHI = verapdfA + verapdfB + verapdfC + verapdfD + verapdfE + verapdfF + verapdfG
							+ verapdfH + verapdfI;
					if (verapdfABCDEFGHI.equals("")) {
						// Kein Fehler gespeichert
						verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_SERVICEFAILED, "veraPDF",
										"veraPDF exception could be visible in the console");
					}
				}
			}

			// TODO: Validierung mit callas
			if (isValidverapdf) {
				// keine Validierung mit Callas
			} else {
				// ggf invalid
				if (callas) {
					// Validierung mit callas
					// Initialisierung callas -> existiert pdfaPilot in den resources?
					String folderCallas = "callas_pdfaPilotServer_x64_Win_13-0-380_cli";
					/*
					 * Update von Callas: callas_pdfaPilotServer_Win_...-Version herunterladen,
					 * installieren, odner im Workbench umbenennen alle Dateine vom Ordner cli
					 * ersetzen aber lizenz.txt und N-Entry.kfpx muessen die alten bleiben
					 */

					File fpdfapilotExe = new File(dirOfJarPath + File.separator + "resources" + File.separator
							+ folderCallas + File.separator + "pdfaPilot.exe");
					if (!fpdfapilotExe.exists()) {
						if (verapdfBo) {
							callas = false;

							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_CALLAS_MISSING2,
													fpdfapilotExe.getAbsolutePath()));
							isValidCallas = false;
						} else {

							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_CALLAS_MISSING,
													fpdfapilotExe.getAbsolutePath()));
							return false;
						}
					}
					String pdfapilotExe = fpdfapilotExe.getAbsolutePath();
					String levelCallas = level.toLowerCase();

					String profile = dirOfJarPath + File.separator + "resources" + File.separator + folderCallas
							+ File.separator + "N-Entry.kfpx";
					String analye = "-a --noprogress --nohits --level=" + levelCallas + " --profile=\"" + profile
							+ "\"";
					String lang = "-l=" + getTextResourceService().getText(locale, MESSAGE_XML_LANGUAGE);
					String reportPath = report.getAbsolutePath();

					// Zeitstempel Heute
					java.util.Date nowDate = new java.util.Date();
					java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd.MM.yyyy");
					String ausgabeDate = sdfDate.format(nowDate);

					File directoryOfConfigfile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x"
							+ File.separator + "configuration");

					File ioCallasCheck = new File(directoryOfConfigfile + File.separator + "_ioCallasCheck.txt");

					if (ioCallasCheck.exists()) {
						// Kontrollieren ob der Check von heute ist
						if (Util.stringInFile(ausgabeDate, ioCallasCheck)) {
							// Callas wurde heute bereits Kontrolliert
						} else {
							// veralteter Test. Datei loeschen
							Util.deleteFile(ioCallasCheck);
						}
					}

					if (!ioCallasCheck.exists()) {
						// Kontrolle ob callas heute korrekt funktioniert
						String valPathTest = dirOfJarPath + File.separator + "license" + File.separator
								+ "other_License" + File.separator + "3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf";

						/*
						 * Testen der Installation und System anhand
						 * 3-Heights(TM)_PDFA_Validator_API_LICENSE.pdf -> invalid
						 */
						callasReturnCodeTest = UtilCallas.execCallas(pdfapilotExe, analye, lang, valPathTest,
								reportPath);

						// report des Testdurchlaufes loeschen
						if (report.exists()) {
							report.delete();
						}

						if (callasReturnCodeTest == 0 || callasReturnCodeTest == 1 || callasReturnCodeTest == 2
								|| callasReturnCodeTest == 3) {
							// Keine callas Validierung moeglich
							configMap.put("callas", "no");

							// -callas_NO -Fileanlegen, damit in J nicht validiert wird
							if (!callasNo.exists()) {
								try {
									callasNo.createNewFile();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							if (verapdfBo) {
								callas = false;
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ getTextResourceService().getText(locale, ERROR_XML_CALLAS_FATAL2,
														fpdfapilotExe.getAbsolutePath()));
								isValidCallas = false;
							} else {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ getTextResourceService().getText(locale, ERROR_XML_CALLAS_FATAL,
														fpdfapilotExe.getAbsolutePath()));
								return false;
							}
						} else {
// Heutige Koontrolle bestanden
							ioCallasCheck.createNewFile();
							FileWriter writer = new FileWriter(ioCallasCheck, true);
							writer.write(ausgabeDate);
							writer.close();
							Files.setAttribute(ioCallasCheck.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
						}
					}

					try {

						/*
						 * Aufbau command:
						 * 
						 * 1) pdfapilotExe: Pfad zum Programm pdfapilot
						 * 
						 * 2) analye: Befehl inkl optionen
						 * 
						 * 3) lang: Sprache
						 * 
						 * 4) valPath: Pfad zur Datei
						 * 
						 * 5) reportPath: Pfad zum Report
						 */

						// Callas unterstuetzt nicht Doppelleerschlag
						String pathToWorkDirD = configMap.get("PathToWorkDir");
						File workDir = new File(pathToWorkDirD);
						if (!workDir.exists()) {
							workDir.mkdir();
						}
						File valDateiNormalisiert = new File(workDir + File.separator + "callas.pdf");
						File valDateiNormalisiertDel = valDateiNormalisiert;
						try {
							Util.copyFile(valDatei, valDateiNormalisiert);
						} catch (IOException e) {
							// Normalisierung fehlgeschlagen es wird ohne versucht
							valDateiNormalisiert = valDatei;
						}
						if (!valDateiNormalisiert.exists()) {
							valDateiNormalisiert = valDatei;
						}
						String valPath = valDateiNormalisiert.getAbsolutePath();
						if (callas) {
							if (report.exists()) {
								report.delete();
							}

							// callas separat ausfuehren und Ergebnis in isValid zurueckgeben
							callasReturnCode = UtilCallas.execCallas(pdfapilotExe, analye, lang, valPath, reportPath);

							Util.copyFile(report, reportOriginal);
							if (!reportOriginal.exists()) {
								Thread.sleep(1000);
								Util.copyFile(report, reportOriginal);
								Thread.sleep(1000);
							}

							if (callasReturnCode == 0) {
								/*
								 * 0 PDF is valid PDF/A-file additional checks wihtout problems
								 * 
								 * 1 PDF is valid PDF/A-file but additional checks with problems severity info
								 * 
								 * 2 PDF is valid PDF/A-file but additional checks with problems severity
								 * warning
								 * 
								 * 3 PDF is valid PDF/A-file but additional checks with problems severity error
								 * --> N-Eintrag
								 * 
								 * 4 PDF is not a valid PDF/A-file
								 */
								isValidCallas = true;
							} else if (callasReturnCode > 3) {
								isValidCallas = false;
							} else if (callasReturnCode == 1 || callasReturnCode == 2 || callasReturnCode == 3) {
								// Zusatzpruefung nicht bestanden
								isValidCallas = false;
							} else {
								isValidCallas = false;
								callasServiceFailed = true;
							}
							if (valDateiNormalisiertDel.exists()) {
								valDateiNormalisiertDel.delete();
							}
							// Ende callas direkt auszuloesen
						}
					} catch (Exception e) {
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												"Exec pdfaPilot: " + e.getMessage()));
						return false;
					}

					// TODO Fehlerzuordnung Callas
					if (callas && !isValidCallas) {
						if (callasServiceFailed) {

							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_SERVICEFAILED,
													"pdfaPilot", callasReturnCode));
						}

						// aus dem Output die Fehler holen

						try {

							if (!reportOriginal.exists()) {
								// Keine Callas Fehlermeldungen
							} else {

								BufferedReader br = new BufferedReader(
										new InputStreamReader(new FileInputStream(reportOriginal)));

								/*
								 * Datei Zeile fuer Zeile lesen und ermitteln ob "Error" darin enthalten ist
								 * 
								 * Errors 1013 CharSet incomplete for Type 1 font Errors 9 OpenType font used
								 * Errors 790 Transparency used (transparency group)
								 * 
								 * Error: The document structure is corrupt.
								 */
								for (String line = br.readLine(); line != null; line = br.readLine()) {
									int index = 0;

									line = line.replace("â€“", "-");
									line = line.replace("â€”", "-");
									line = line.replace("â€˜", "'");
									line = line.replace("â€™", "'");
									line = line.replace("â€š", ",");
									line = line.replace("â€œ", "'");
									line = line.replace("â€?", "'");
									line = line.replace("â€ž", "'");
									line = line.replace("â€¢", "-");
									line = line.replace("â€°", "‰");
									line = line.replace("â€¹", "<");
									line = line.replace("â€º", ">");
									line = line.replace("â‚¬", "E");
									line = line.replace("â„¢", "TM");
									line = line.replace("â€“", "–");
									line = line.replace("a€™", "'");
									line = line.replace("Ãoe", "Ue");

									line = line.replace("Ã´", "o");
									line = line.replace("Å¡", "s");
									line = line.replace("Ã¶", "o");
									line = line.replace("Ãº", "u");
									line = line.replace("Å¤", "?");
									line = line.replace("Ã¼", "u");
									line = line.replace("Å¥", "?");
									line = line.replace("Â©", "(c) ");
									line = line.replace("Ã½", "y");
									line = line.replace("Å®", "U");
									line = line.replace("Â«", "'");
									line = line.replace("Ä‚", "A");
									line = line.replace("Å¯", "u");
									line = line.replace("Äƒ", "a");
									line = line.replace("Å°", "U");
									line = line.replace("Â-", "-");
									line = line.replace("Ä„", "Y");
									line = line.replace("Å±", "u");
									line = line.replace("Â®", "(R) ");
									line = line.replace("Ä…", "1");
									line = line.replace("Å¹", "?");
									line = line.replace("Ä†", "Ae");
									line = line.replace("Åº", "Y");
									line = line.replace("Â±", "+-");
									line = line.replace("Ä‡", "ae");
									line = line.replace("ÄŒ", "E");
									line = line.replace("Å¼", "?");
									line = line.replace("Âµ", "u");
									line = line.replace("Ä?", "e");
									line = line.replace("Å½", "Z");
									line = line.replace("ÄŽ", "I");
									line = line.replace("Å¾", "z");
									line = line.replace("Â·", "");
									line = line.replace("Ä?", "I");
									line = line.replace("Ë‡", "i");
									line = line.replace("Â¸", "");
									line = line.replace("Ä?", "D");
									line = line.replace("Ë˜", "c");
									line = line.replace("Â»", "'");
									line = line.replace("Ä‘", "d");
									line = line.replace("Ë™", "y");
									line = line.replace("Ã?", "A");
									line = line.replace("Ä˜", "E");
									line = line.replace("Ë›", "2");
									line = line.replace("Ã‚", "A");
									line = line.replace("Ä™", "e");
									line = line.replace("Ë?", "1/2");
									line = line.replace("Ã„", "Ae");
									line = line.replace("Äš", "I");
									line = line.replace("Ã‡", "C");
									line = line.replace("Ä›", "i");
									line = line.replace("Ã‰", "E");
									line = line.replace("Ä¹", "A");
									line = line.replace("Ã‹", "E");
									line = line.replace("Äº", "a");
									line = line.replace("Ã?", "I");
									line = line.replace("Ä½", "1/4");
									line = line.replace("ÃŽ", "I");
									line = line.replace("Ä¾", "3/4");
									line = line.replace("Ã“", "O");
									line = line.replace("Å?", "L");
									line = line.replace("Ã”", "O");
									line = line.replace("Å‚", "3");
									line = line.replace("Ã–", "Oe");
									line = line.replace("Åƒ", "N");
									line = line.replace("Ã—", "x");
									line = line.replace("Å„", "n");
									line = line.replace("Ãš", "U");
									line = line.replace("Å‡", "O");
									line = line.replace("Ãœ", "Ue");
									line = line.replace("Åˆ", "o");
									line = line.replace("Ã?", "Y");
									line = line.replace("Å?", "O");
									line = line.replace("ÃŸ", "ss");
									line = line.replace("Å‘", "o");
									line = line.replace("Ã¡", "a");
									line = line.replace("Å”", "A");
									line = line.replace("Ã¢", "a");
									line = line.replace("Å•", "a");
									line = line.replace("Ã¤", "ae");
									line = line.replace("Å˜", "O");
									line = line.replace("Ã§", "c");
									line = line.replace("Å™", "o");
									line = line.replace("Ã©", "e");
									line = line.replace("Åš", "Oe");
									line = line.replace("Ã«", "e");
									line = line.replace("Å›", "oe");
									line = line.replace("Ã-", "i");
									line = line.replace("Åž", "a");
									line = line.replace("Ã®", "i");
									line = line.replace("ÅŸ", "o");
									line = line.replace("Ã³", "o");
									line = line.replace("Ã¨", "e");
									line = line.replace("Ãª", "e");
									line = line.replace("Ã¬", "i");
									line = line.replace("Ã¯", "i");
									line = line.replace("Ã¶", "oe");
									line = line.replace("Ã¹", "u");
									line = line.replace("Ã»", "u");
									line = line.replace("Ã¼", "ue");

									line = line.replace("Â", " ");
									line = line.replace("Å", "S");
									line = line.replace("Ã", "a");
									line = line.replace("ß", "ss");
									line = line.replace("Ä", "Ae");
									line = line.replace("‘", "'");
									line = line.replace("’", "'");
									line = line.replace("Ò", "O");
									line = line.replace("Ó", "O");
									line = line.replace("Ô", "O");
									line = line.replace("Ö", "Oe");
									line = line.replace("œ", "oe");
									line = line.replace("Ü", "Ue");
									line = line.replace("á", "a");
									line = line.replace("â", "a");
									line = line.replace("ä", "ae");
									line = line.replace("ç", "c");
									line = line.replace("è", "e");
									line = line.replace("é", "e");
									line = line.replace("ê", "e");
									line = line.replace("ë", "e");
									line = line.replace("ì", "i");
									line = line.replace("í", "i");
									line = line.replace("î", "i");
									line = line.replace("ï", "i");
									line = line.replace("ö", "oe");
									line = line.replace("ù", "u");
									line = line.replace("ú", "u");
									line = line.replace("û", "u");
									line = line.replace("ü", "ue");
									line = line.replace("à", "a");

									/*
									 * Die Linien (Fehlermeldung von Callas) anhand von Woerter den Modulen zuordnen
									 * 
									 * A) Allgemeines B) Struktur C) Grafiken D) Schrift E) transparen F) annotation
									 * G) aktion H) metadata I) Zugaenglichkeit
									 */

									if (line.startsWith("Error")) {
										// Error plus Zahl entfernen aus Linie
										index = line.indexOf("\t", 8);
										line = line.substring(index);
										if (line.contains(
												"Komponentenanzahl im N-Eintrag des PDF/A Output Intent stimmt nicht mit ICC-Profil ueberein")
												|| line.contains(
														"Number of components in PDF/A OutputIntent N entry does not match ICC profile")) {
											// als zusatz im Log kennzeichnen
											line = line + " [callas] ";
										} else if (line.contains("Le nombre de composants dans l'entr")
												&& line.contains(
														"N des conditions de sortie PDF/A ne correspond pas au profil ICC")) {
											// als zusatz im Log kennzeichnen enthaelt " l'entree� N " entsprechend
											// alles neu...
											line = "Le nombre de composants dans l'entree N des conditions de sortie PDF/A ne correspond pas au profil ICC [callas] ";
										} else {
											line = line + " [callas] ";
										}

										String callasAwarningDE = "Ungueltige PDF/A-Versionsnummer (muss";
										String callasAwarningDE2 = "Ungultige PDF/A-Versionsnummer (muss";
										String callasAwarningFR = "Numero de version PDF/A incorrect (doit etre";
										String callasAwarningIT = "Numero di versione PDF/A scorretto (deve essere";
										String callasAwarningEN = "Incorrect PDF/A version number (must be";
										if (warning3to2.equalsIgnoreCase("yes") && line.contains("2")
												&& (line.contains(callasAwarningDE) || line.contains(callasAwarningDE2)
														|| line.contains(callasAwarningFR)
														|| line.contains(callasAwarningIT)
														|| line.contains(callasAwarningEN))) {
											// Fehler wird ignoriert. Es wird eine Warnung ausgegeben.
											callasA = callasA
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
													+ "<Message>" + line
													+ "</Message></Error><Warning>warning</Warning>";
											System.out.println(callasA);
										} else if (line.toLowerCase().contains("schrift")
												|| line.toLowerCase().contains("police")
												|| line.toLowerCase().contains("font")
												|| line.toLowerCase().contains("gly")
												|| line.toLowerCase().contains("truetype")
												|| line.toLowerCase().contains("unicode")
												|| line.toLowerCase().contains("cid")
												|| line.toLowerCase().contains("charset")) {
											isValidCd = false;
											if (callasD.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasD = callasD
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_D_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("grafiken")
												|| line.toLowerCase().contains("graphique")
												|| line.toLowerCase().contains("graphic")
												|| line.toLowerCase().contains("image")
												|| line.toLowerCase().contains("bild")
												|| line.toLowerCase().contains("icc")
												|| line.toLowerCase().contains("color")
												|| line.toLowerCase().contains("couleur")
												|| line.toLowerCase().contains("farb")
												|| line.toLowerCase().contains("rgb")
												|| line.toLowerCase().contains("rvb")
												|| line.toLowerCase().contains("cmyk")
												|| line.toLowerCase().contains("cmjn")
												|| line.toLowerCase().contains("outputintent")
												|| line.toLowerCase().contains("jpeg2000")
												|| line.toLowerCase().contains("devicegray")
												|| line.toLowerCase().contains("tr2")) {
											isValidCc = false;
											if (callasC.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasC = callasC
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_C_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("zugaenglich")
												|| line.toLowerCase().contains("disponibi")
												|| line.toLowerCase().contains("accessibi")
												|| line.toLowerCase().contains("markinfo")
												|| line.toLowerCase().contains("structree")
												|| line.toLowerCase().contains("structure tree root")
												|| line.toLowerCase().contains("strukturbaum")) {
											isValidCi = false;
											if (callasI.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasI = callasI
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_I_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("struktur")
												|| line.toLowerCase().contains("ebenen")
												|| line.toLowerCase().contains("structure")
												|| line.toLowerCase().contains("lzw")
												|| line.toLowerCase().contains("xref")
												|| line.toLowerCase().contains(" eol")) {
											isValidCb = false;
											if (callasB.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasB = callasB
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_B_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("metad")
												|| line.toLowerCase().contains("xmp")) {
											isValidCh = false;
											if (callasH.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasH = callasH
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_H_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("transparen")) {
											isValidCe = false;
											if (callasE.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasE = callasE
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_E_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("aktion")
												|| line.toLowerCase().contains("action")
												|| line.toLowerCase().contains("aa")
												|| line.toLowerCase().contains("javascript")) {
											isValidCg = false;
											if (callasG.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasG = callasG
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_G_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else if (line.toLowerCase().contains("annotation")
												|| line.toLowerCase().contains("embedd")
												|| line.toLowerCase().contains("komment")
												|| line.toLowerCase().contains("comment")
												|| line.toLowerCase().contains("structure")
												|| line.toLowerCase().contains("drucke")
												|| line.toLowerCase().contains("print")
												|| line.toLowerCase().contains("imprim")
												|| line.toLowerCase().contains("eingebette")
												|| line.toLowerCase().contains("incorpor")) {
											isValidCf = false;
											if (callasF.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasF = callasF
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_F_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}

										} else {
											isValidCa = false;
											if (callasA.toLowerCase().contains(line.toLowerCase())) {
												// Fehlermeldung bereits erfasst -> keine Aktion
											} else {
												callasA = callasA
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_MODUL_A_PDFA)
														+ "<Message>" + line + "</Message></Error>";
											}
										}
									} else if (line.startsWith("Error:")) {
										line = line.substring(7);
										line = line + " [callas] ";
										if (callasA.toLowerCase().contains(line.toLowerCase())) {
											// Fehlermeldung bereits erfasst -> keine Aktion
										} else {
											callasA = callasA
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
													+ "<Message>" + line + "</Message></Error>";
										}
									} else if (line.startsWith("Error")) {
										line = line.substring(11);
										line = line + " [callas] ";
										if (callasA.toLowerCase().contains(line.toLowerCase())) {
											// Fehlermeldung bereits erfasst -> keine Aktion
										} else {
											callasA = callasA
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
													+ "<Message>" + line + "</Message></Error>";
										}
									}
								}

								br.close();
								// set to null
								br = null;
							}
						} catch (FileNotFoundException e) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													"FileNotFoundException (Callas) " + e));
							return false;
						} catch (Exception e) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													(e.getMessage() + " 1"))); //
							return false;
						}
						if (!callasA.equals("") && isValidCa) {
							Logtxt.logtxt(logFile, callasA);
						}
					}

				}
			}

			/** Modul J **/
			String jbig2allowed = configMap.get("jbig2allowed");
			if (jbig2allowed.contentEquals("yes")) {
				// JBIG2 erlaubt kein Fehler moeglich und auch kein Test noetig
			} else {
				try {
					BufferedReader in = new BufferedReader(new FileReader(valDatei));
					String line;
					while ((line = in.readLine()) != null) {
						line = line.toLowerCase();
						if (line.contains("jbig2decode")) {
							isValidJ = false;
							if (min) {
								in.close();
								return false;
							} else {
								break;
							}
						}
					}
					in.close();
					// set to null
					in = null;

				} catch (Throwable e) {
					if (min) {
					} else {
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												" Modul J " + e.getMessage()));
					}
				}
			}
			/*
			 * durch die diversen nacharbeiten (Warnung anstelle Fehler) muss kontrolliert
			 * werden ob es valide mit Warnungen oder Invalide ist.
			 */
			if (callas && isValidCa && isValidCb && isValidCc && isValidCd && isValidCe && isValidCf && isValidCg
					&& isValidCh && isValidCi) {
				isValidCallas = true;
			}

			// TODO: Erledigt: Fehler Auswertung
			if (verapdfBo && callas) {
				// Duale Validierung

				// nur wenn beide invalid dann invalid
				// sobald eines Valid dann valid
				if (isValidverapdf) {
					isValid = true;
				}
				if (isValidCallas) {
					isValid = true;
				}
			} else if (verapdfBo) {
				// nur validierung mit verapdf
				if (isValidverapdf) {
					isValid = true;
				}
			} else if (callas) {
				// nur validierung mit callas
				if (isValidCallas) {
					isValid = true;
				}
			}

			if (isValid) {
				if (!isValidJ) {
					isValid = false;
				}
				// TODO: Falls gewunscht Fontvalidierung mit PDFTools nur durchfuehren wenn
				// callas und verapdf bestanden haben.
				if (!fontYesNo.equalsIgnoreCase("no")) {
					docPdf = new PdfValidatorAPI();
					try {
						if (docPdf.open(valDatei.getAbsolutePath(), "", NativeLibrary.COMPLIANCE.ePDFUnk)) {
							// PDF Konnte geoeffnet werden
							docPdf.setStopOnError(true);
							docPdf.setReportingLevel(1);
							if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
								// Keine Meldung da nur noch Fontvalidierung
							}
						} else {
							// Keine Meldung da nur noch Fontvalidierung
						}

						docPdf = new PdfValidatorAPI();
						docPdf.setStopOnError(false);
						docPdf.setReportingLevel(2);

						/*
						 * ePDFA1a 5122 ePDFA1b 5121 ePDFA2a 5891 ePDFA2b 5889 ePDFA2u 5890
						 */
						if (level.contentEquals("1A")) {
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5122)) {
								docPdf.validate();
							}
						} else if (level.contentEquals("1B")) {
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5121)) {
								docPdf.validate();
							}
						} else if (level.contentEquals("2A")) {
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5891)) {
								docPdf.validate();
							}
						} else if (level.contentEquals("2B")) {
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5889)) {
								docPdf.validate();
							}
						} else if (level.contentEquals("2U")) {
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5890)) {
								docPdf.validate();
							}
						} else {
							// Validierung nach 2b
							level = "2B";
							if (docPdf.open(valDatei.getAbsolutePath(), "", 5889)) {
								docPdf.validate();
							}
						}

						// Anzahl errors
						PdfError err = docPdf.getFirstError();
						int success = 0;

						if (err != null) {
							// auch bei min durchfuehren!
							for (; err != null; err = docPdf.getNextError()) {
								success = success + 1;
							}
						}

						String pathToFontOutput = pathToLogDir + File.separator + valDatei.getName()
								+ "_FontValidation.xml";
						File fontReport = new File(pathToFontOutput);
						if (fontReport.exists()) {
							fontReport.delete();
						}
						String pathToFontOutputError = pathToLogDir + File.separator + valDatei.getName()
								+ "_FontValidation_Error.xml";
						File fontReportError = new File(pathToFontOutputError);
						if (fontReportError.exists()) {
							fontReportError.delete();
						}

						// Write font validation information
						boolean fontReportFailed = true;
						FileStream fs = new FileStream(fontReport, "rw");
						Stream xmlStream = fs;
						boolean docPdfXML = false;
						try {
							docPdfXML = docPdf.writeFontValidationXML(xmlStream);
						} catch (Error e2) {
							Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											"Failed to write font validation information (error): " + e2.getMessage()));
						} catch (IllegalArgumentException | SecurityException e) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													"Failed to write font validation information (exception): "
															+ e.getMessage()));
						} catch (Exception ex) {
							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
											+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
													"Failed to write font validation information: " + ex.getMessage()));
						}
						if (!docPdfXML) {
							/*
							 * throw new Exception(String.format("Failed to write font validation
							 * information: %s", docPdf.getErrorMessage()));
							 */
							System.out.println(String.format("Failed to write font validation information: %s",
									docPdf.getErrorMessage()));
						}
						fs.close();
						// set to null
						fs = null;
						fontReportFailed = false;

						if (!fontReportFailed) {
							long length = 99999;
							length = fontReport.length();
							if (10 > length) {
								// System.out.println( "Warnung ausgeben" );
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
														"Failed to write font validation information"));
								fontReportFailed = true;

							} else {
								fontReportFailed = false;
							}
						}

						// TODO erledigt: Start der Font-Auswertung betreffend
						// unbekannt und undefiniert

						if (!fontReportFailed) {
							try {
								FileChannel inputChannel = null;
								FileChannel outputChannel = null;
								FileInputStream fis = null;
								FileOutputStream fos = null;
								try {
									fis = new FileInputStream(fontReport);
									inputChannel = fis.getChannel();
									fos = new FileOutputStream(fontReportError);
									outputChannel = fos.getChannel();
									outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
								} finally {
									inputChannel.close();
									outputChannel.close();
									// set to null
									inputChannel = null;
									outputChannel = null;
								}
								fis.close();
								fos.close();
								// set to null
								fis = null;
								fos = null;
							} catch (Exception e) {
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
												+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
														"Exec PDF Tools FileChannel: " + e.getMessage()));
								return false;
							}

							Document doc = null;
							Document docError = null;

							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fontReportError));
							DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder();
							doc = db.parse(bis);
							doc.normalize();

							// <characterCount>122</characterCount>
							// <characterUnknown>0</characterUnknown>
							// <characterUnknownPercentage>0</characterUnknownPercentage>
							// <characterUndefined>122</characterUndefined>
							// <characterUndefinedPercentage>100</characterUndefinedPercentage>

							String elementCount = doc.getElementsByTagName("characterCount").item(0).getTextContent();
							int count = Integer.valueOf(elementCount);
							String elementUnknown = doc.getElementsByTagName("characterUnknown").item(0)
									.getTextContent();
							// int unknown=Integer.valueOf( elementUnknown );
							String elementUnknownP = doc.getElementsByTagName("characterUnknownPercentage").item(0)
									.getTextContent();
							double unknownPdouble = Double.valueOf(elementUnknownP);
							String elementUndefined = doc.getElementsByTagName("characterUndefined").item(0)
									.getTextContent();
							// int undefined=Integer.valueOf( elementUndefined );
							String elementUndefinedP = doc.getElementsByTagName("characterUndefinedPercentage").item(0)
									.getTextContent();
							double undefinedPdouble = Double.valueOf(elementUndefinedP);
							double elementTolerance = unknownPdouble + undefinedPdouble;
							String docInfo = nodeToString(doc.getElementsByTagName("docInfo").item(0));
							String docInfoCharacterCount = docInfo + "<characterCount>" + elementCount
									+ "</characterCount>";
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n  ", "");
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n ", "");
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n", "");
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n  ", "");
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n ", "");
							docInfoCharacterCount = docInfoCharacterCount.replaceAll("\n", "");

							if (elementUnknown.equals("0") && elementUndefined.equals("0")) {
								if (fontYesNo.equalsIgnoreCase("only")) {
									// Modul K bestanden

									/*
									 * Bei only wird in Log_Modul_K.txt die Dateien, hereingeschrieben, welche die
									 * Strikte Validierung nicht bestanden haben. Dies ist hier nicht der Fall.
									 * Entsprechend wird mit return true die Validierung beendet
									 */
									return false;
								} else {
									// Modul K bestanden

									// <docInfo> und <characterCount> auf eine Zeile ausgeben
									if (min) {
									} else {
										Logtxt.logtxt(logFile, docInfoCharacterCount);
									}
								}
							} else {
								// Modul K evtl nicht bestanden
								try {
									if (fontYesNo.equalsIgnoreCase("only")) {
										// only=strict -> Modul K nicht bestanden

										/*
										 * Bei only wird in Log_Modul_K.txt die Dateien, hereingeschrieben, welche die
										 * Strikte Validierung nicht bestanden haben. Danach wird mit return false die
										 * Validierung beendet
										 */
										String modulKFilePath = directoryOfLogfile.getAbsolutePath() + File.separator
												+ "Log_Modul_K.txt";
										String line = valDatei.getAbsolutePath() + " ";
										Files.write(Paths.get(modulKFilePath), line.getBytes(),
												StandardOpenOption.APPEND);
										return false;
									} else if (fontYesNo.equalsIgnoreCase("tolerant")) {
										/*
										 * Ausnahmen ermitteln und abfangen bei tolerant:
										 * 
										 * A) max 5 Zeichen
										 * 
										 * B) Undefinierte Zeichen ignorieren wenn Maengel<=20%
										 * 
										 * C) Bekannte Abstaende und Aufzaehlungszeichen ignorieren
										 * 
										 * D) Symbol-Schriften ignorieren
										 * 
										 * E) Wenn am Schluss nur noch max 4 Zeichen bemaengelt werden
										 */
										if (count <= 5) {
											/*
											 * => A) max 5 Zeichen
											 * 
											 * -> Modul K bestanden, keine Auswertung noetig
											 * 
											 * <docInfo> und <characterCount> auf eine Zeile ausgeben
											 */
											if (min) {
											} else {
												Logtxt.logtxt(logFile, docInfoCharacterCount);
											}
										} else {
											double tolerance = 20.000;
											fontErrorIgnor = "I";
											if (tolerance > elementTolerance) {
												/*
												 * elementTolerance = unknownPdouble + undefinedPdouble;
												 * toleranzschwelle nicht ueberschritten undefiniertes kann bei tolerant
												 * ignoriert werden
												 */
												ignorUndefinied = true;
											} else {
												/*
												 * toleranzschwelle ueberschritten -> Modul K nicht bestanden
												 */
												ignorUndefinied = false;
												fontErrorIgnor = "E";
											}

											if (elementUnknown.equals("0") && ignorUndefinied) {
												/*
												 * => B) Undefinierte Zeichen ignorieren wenn Maengel<=20%
												 * 
												 * -> Modul K bestanden, keine Auswertung noetig da nur undefiniertes
												 * 
												 * <docInfo> und <characterCount> auf eine Zeile ausgeben
												 */
												if (min) {
												} else {
													Logtxt.logtxt(logFile, docInfoCharacterCount);
												}
											} else {
												// weiter auswerten
												NodeList nodeCharacterLst = doc.getElementsByTagName("character");
												Set<Node> targetNode = new HashSet<Node>();

												for (int s = 0; s < nodeCharacterLst.getLength(); s++) {
													// unnoetige character aus log loeschen
													boolean charUndef = false;
													boolean unicode = false;
													Node charNode = nodeCharacterLst.item(s);

													if (charNode.hasAttributes()) {
														NamedNodeMap attrs = charNode.getAttributes();
														for (int i = 0; i < attrs.getLength(); i++) {
															Attr attribute = (Attr) attrs.item(i);
															String attName = attribute.getName();
															String attNameValue = attribute.getName() + " = "
																	+ attribute.getValue();

															/*
															 * -> cid = 60 -> glyphId = 60 -> unicode = U+FFFD ->
															 * unicodeUndefined = true
															 */
															if (attName.equalsIgnoreCase("unicode")) {
																unicode = true;
															}
															if (attNameValue
																	.equalsIgnoreCase("unicodeUndefined = true")) {
																charUndef = true;
															}
														}
														if (!unicode) {
															// unicode nicht bekannt -> node weiteranalysieren

															/*
															 * wenn I = tolerant analysieren ob es ein bekanntes
															 * Abstand- oder Aufzaehlungszeichen ist.
															 * 
															 * ist es ein solches bekanntes Zeichen das Zeichen
															 * ignorieren ansonsten belassen
															 */
															if (fontErrorIgnor.equalsIgnoreCase("I")) {
																String characterTextContent = charNode.getTextContent();
																characterTextContent = characterTextContent
																		.replaceAll("\n", "");
																boolean ignoreCharacter = UtilCharacter
																		.ignoreCharacter(characterTextContent);

																if (ignoreCharacter) {
																	/*
																	 * => C) Bekannte Abstaende und Aufzaehlungszeichen
																	 * ignorieren
																	 *
																	 * Node zum leschen vormerken, da er ein bekanntes
																	 * Aufzaehlungszeichen oder Abstand ist
																	 */
																	targetNode.add(charNode);
																}
															}
														} else if (charUndef) {
															// System.out.println( " unicode nicht definiert ");
															// wenn I ignorieren und sonst belassen
															if (fontErrorIgnor.equalsIgnoreCase("I")) {
																/*
																 * => B) Undefinierte Zeichen ignorieren wenn
																 * Maengel<=20%
																 *
																 * Node zum loeschen vormerken, da er ignoriert werden
																 * kann (I)
																 */
																targetNode.add(charNode);
															}
														} else {
															// unicode bekannt -> dieser node kann geloescht werden
															// Node zum loeschen vormerken
															targetNode.add(charNode);
														}
													}
												}

												for (Node e : targetNode) {
													e.getParentNode().removeChild(e);
												}

												// write the content into xml file
												TransformerFactory transformerFactory = TransformerFactory
														.newInstance();
												Transformer transformer = transformerFactory.newTransformer();
												DOMSource source = new DOMSource(doc);
												StreamResult result = new StreamResult(fontReportError);
												// Output to console for testing
												// result = new StreamResult(System.out );

												transformer.transform(source, result);

												// Fonts ohne character loeschen
												BufferedInputStream bisError = new BufferedInputStream(
														new FileInputStream(fontReportError));
												DocumentBuilderFactory dbfError = DocumentBuilderFactory.newInstance();
												DocumentBuilder dbError = dbfError.newDocumentBuilder();
												docError = dbError.parse(bisError);
												docError.normalize();

												NodeList nodeFontLst = docError.getElementsByTagName("font");
												Set<Node> targetNodeFont = new HashSet<Node>();

												for (int s = 0; s < nodeFontLst.getLength(); s++) {
													Node fontNode = nodeFontLst.item(s);

													NodeList nodeFontCharLst = fontNode.getChildNodes();
													if (nodeFontCharLst.getLength() <= 1) {
														// Leeren Font durch B) und C) oder unicode-Zeichen
														// font Node zum leschen vormerken
														targetNodeFont.add(fontNode);
													} else if (fontNode.hasAttributes()
															&& fontErrorIgnor.equalsIgnoreCase("I")) {
														NamedNodeMap attrs = fontNode.getAttributes();
														for (int i = 0; i < attrs.getLength(); i++) {
															Attr attribute = (Attr) attrs.item(i);
															String attName = attribute.getName();
															String attValue = attribute.getValue();

															/*
															 * <font fontfile= "TrueType" fullname="Symbol"
															 * name="Symbol" objectNo="161" type="Type0 (CIDFontType2)"
															 * >
															 */
															if (attName.equalsIgnoreCase("name")) {
																if (attValue.contains("Symbol")
																		|| attValue.contains("Webdings")
																		|| attValue.contains("Wingdings")
																		|| attValue.contains("Math")
																		|| attValue.contains("symbol")
																		|| attValue.contains("webdings")
																		|| attValue.contains("wingdings")
																		|| attValue.contains("math")) {
																	// => D) Symbol-Schriften ignorieren
																	// font Node zum leschen vormerken
																	targetNodeFont.add(fontNode);
																}
															}
														}
													}
												}
												for (Node f : targetNodeFont) {
													f.getParentNode().removeChild(f);
												}
												docError.getDocumentElement().normalize();
												XPathExpression xpath = XPathFactory.newInstance().newXPath()
														.compile("//text()[normalize-space(.) = '']");
												NodeList blankTextNodes = (NodeList) xpath.evaluate(docError,
														XPathConstants.NODESET);
												for (int i = 0; i < blankTextNodes.getLength(); i++) {
													blankTextNodes.item(i).getParentNode()
															.removeChild(blankTextNodes.item(i));
												}
												// Ende Bereinigung

												/*
												 * Nach der Bereinigung ermitteln wieviele unbekannt und undefiniert
												 * sind und ob es ein Fehler ist oder nicht
												 * 
												 * unbekannt unknown = count - undefined
												 * 
												 * undefiniert undefined = (unicodeUndefined = true)
												 */
												int undefinedE = 0;
												NodeList nodeCharacterLstError = docError
														.getElementsByTagName("character");
												for (int s = 0; s < nodeCharacterLstError.getLength(); s++) {
													Node charNode = nodeCharacterLstError.item(s);
													if (charNode.hasAttributes()) {
														NamedNodeMap attrs = charNode.getAttributes();
														for (int i = 0; i < attrs.getLength(); i++) {
															Attr attribute = (Attr) attrs.item(i);
															String attNameValue = attribute.getName() + " = "
																	+ attribute.getValue();
															if (attNameValue
																	.equalsIgnoreCase("unicodeUndefined = true")) {
																if (fontErrorIgnor.equalsIgnoreCase("E")) {
																	undefinedE = undefinedE + 1;
																}
															}
														}
													}
												}
												int unknownW = nodeCharacterLstError.getLength();
												float unknownPdoubleW = 100 / (float) count * (float) unknownW;

												if (unknownW <= 4) {
													// => E) Wenn am Schluss nur noch max 4 Zeichen bemaengelt werden
													// -> Modul K bestanden, keine Auswertung noetig
													isValidFont = "true";
													errorK = "";
												} else {
													// Fehler im Modul K
													if (fontErrorIgnor.equalsIgnoreCase("I")) {
														isValidFont = "false";
														errorK = errorK
																+ getTextResourceService().getText(locale,
																		MESSAGE_XML_MODUL_K_PDFA)
																+ getTextResourceService().getText(locale,
																		ERROR_XML_K_OVERVIEW2, count, unknownW,
																		unknownPdoubleW);
													} else {
														isValidFont = "false";
														errorK = errorK
																+ getTextResourceService().getText(locale,
																		MESSAGE_XML_MODUL_K_PDFA)
																+ getTextResourceService().getText(locale,
																		ERROR_XML_K_OVERVIEW, elementCount,
																		elementUnknown, elementUnknownP,
																		elementUndefined, elementUndefinedP);
													}
												}
												if (isValidFont.equals("false")) {
													Node nodeInfo = docError.getElementsByTagName("docInfo").item(0);
													String stringInfo = nodeToString(nodeInfo);
													Node nodeFonts = docError.getElementsByTagName("fonts").item(0);
													String stringFonts = nodeToString(nodeFonts);
													errorK = errorK + getTextResourceService().getText(locale,
															ERROR_XML_K_DETAIL, stringInfo, stringFonts);
												} else {
													// <docInfo> und <characterCount> auf eine Zeile ausgeben
													if (min) {
													} else {
														Logtxt.logtxt(logFile, docInfoCharacterCount);
													}
												}
												bisError.close();
												// set to null
												bisError = null;
												source = null;
												result = null;
											}
										}
									} else {
										/* strict: Fehler ausgeben */
										NodeList nodeCharacterLst = doc.getElementsByTagName("character");
										Set<Node> targetNode = new HashSet<Node>();

										for (int s = 0; s < nodeCharacterLst.getLength(); s++) {
											// unnoetige character aus log loeschen
											boolean charUndef = false;
											boolean unicode = false;
											Node charNode = nodeCharacterLst.item(s);

											if (charNode.hasAttributes()) {
												NamedNodeMap attrs = charNode.getAttributes();
												for (int i = 0; i < attrs.getLength(); i++) {
													Attr attribute = (Attr) attrs.item(i);
													String attName = attribute.getName();
													String attNameValue = attribute.getName() + " = "
															+ attribute.getValue();
													/*
													 * -> cid = 60 -> glyphId = 60 -> unicode = U+FFFD ->
													 * unicodeUndefined = true
													 */
													if (attName.equalsIgnoreCase("unicode")) {
														unicode = true;
													}
													if (attNameValue.equalsIgnoreCase("unicodeUndefined = true")) {
														charUndef = true;
													}
												}
												if (!unicode) {
													// unicode nicht bekannt
													// -> node behalten da strict
												} else if (charUndef) {
													// unicode nicht definiert
													// -> node behalten da strict
												} else {
													// unicode bekannt
													// -> dieser node kann geloescht werden

													// Node zum leschen vormerken
													targetNode.add(charNode);
												}
											}
										}
										for (Node e : targetNode) {
											e.getParentNode().removeChild(e);
										}

										// write the content into xml file
										TransformerFactory transformerFactory = TransformerFactory.newInstance();
										Transformer transformer = transformerFactory.newTransformer();
										DOMSource source = new DOMSource(doc);
										StreamResult result = new StreamResult(fontReportError);
										// Output to console for testing
										// result = new StreamResult( System.out);

										transformer.transform(source, result);

										// Fonts ohne character loeschen
										BufferedInputStream bisError = new BufferedInputStream(
												new FileInputStream(fontReportError));
										DocumentBuilderFactory dbfError = DocumentBuilderFactory.newInstance();
										DocumentBuilder dbError = dbfError.newDocumentBuilder();
										docError = dbError.parse(bisError);
										docError.normalize();

										NodeList nodeFontLst = docError.getElementsByTagName("font");
										Set<Node> targetNodeFont = new HashSet<Node>();

										for (int s = 0; s < nodeFontLst.getLength(); s++) {
											Node fontNode = nodeFontLst.item(s);

											NodeList nodeFontCharLst = fontNode.getChildNodes();
											if (nodeFontCharLst.getLength() <= 1) {
												/*
												 * Leeren Font durch B) und C) oder unicode-Zeichen
												 * 
												 * font Node zum leschen vormerken
												 */
												targetNodeFont.add(fontNode);
											}
										}

										for (Node f : targetNodeFont) {
											f.getParentNode().removeChild(f);
										}
										docError.getDocumentElement().normalize();
										XPathExpression xpath = XPathFactory.newInstance().newXPath()
												.compile("//text()[normalize-space(.) = '']");
										NodeList blankTextNodes = (NodeList) xpath.evaluate(docError,
												XPathConstants.NODESET);

										for (int i = 0; i < blankTextNodes.getLength(); i++) {
											blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
										}

										// Ende Bereinigung

										/*
										 * Nach der Bereinigung ermitteln wieviele unbekannt und undefiniert sind
										 * 
										 * unbekannt unknown = count - undefined
										 * 
										 * undefiniert undefined = (unicodeUndefined = true)
										 */
										int undefinedE = 0;

										NodeList nodeCharacterLstError = docError.getElementsByTagName("character");

										for (int s = 0; s < nodeCharacterLstError.getLength(); s++) {
											Node charNode = nodeCharacterLstError.item(s);

											if (charNode.hasAttributes()) {
												NamedNodeMap attrs = charNode.getAttributes();
												for (int i = 0; i < attrs.getLength(); i++) {
													Attr attribute = (Attr) attrs.item(i);
													String attNameValue = attribute.getName() + " = "
															+ attribute.getValue();
													if (attNameValue.equalsIgnoreCase("unicodeUndefined = true")) {
														if (fontErrorIgnor.equalsIgnoreCase("E")) {
															undefinedE = undefinedE + 1;
														}
													}
												}
											}
										}

										if (!elementUnknown.equals("0") || !elementUndefined.equals("0")) {
											isValidFont = "false";
											errorK = errorK
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
													+ getTextResourceService().getText(locale, ERROR_XML_K_OVERVIEW,
															elementCount, elementUnknown, elementUnknownP,
															elementUndefined, elementUndefinedP);
										}

										if (isValidFont.equals("false")) {
											Node nodeInfo = docError.getElementsByTagName("docInfo").item(0);
											String stringInfo = nodeToString(nodeInfo);
											Node nodeFonts = docError.getElementsByTagName("fonts").item(0);
											String stringFonts = nodeToString(nodeFonts);
											errorK = errorK + getTextResourceService().getText(locale,
													ERROR_XML_K_DETAIL, stringInfo, stringFonts);
										} else {
											// <docInfo> und <characterCount> auf eine Zeile ausgeben
											if (min) {
											} else {
												Logtxt.logtxt(logFile, docInfoCharacterCount);
											}
										}
										bisError.close();
										// set to null
										bisError = null;
										source = null;
										result = null;

									}
								} catch (Exception e) {
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
													+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
															"Exec PDF Tools Font: " + e.getMessage()));
									return false;
								}
							}
							bis.close();
							// set to null
							bis = null;
							doc = null;
							docError = null;
							if (fontReportError.exists()) {
								// Kann noch nicht geloescht werden, da noch aktiv
								// fontReportError wird in Controllervalfile
								// geloescht (je nach verbose)
							}
						}
					} catch (Exception e) {
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
										+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
												"Exec PDF Tools: " + e.getMessage()));
						return false;
					}
				} else {
					// ohne pdftools auch keine Font validierung
					isValidFont = "noFontVal";
				}

				if (isValidFont.equals("false")) {
					isValid = false;
				}
			}

			// TODO erledigt: Ggf Fehler und Warnungen ausgeben
			if (!isValid) {
				if (min) {
					return false;
				} else {
					// Warnung ausgeben wenn Fontvalidierung nicht durchgefuehrt wurde
					if (isValidFont.equals("noFontVal")) {
						if (!fontYesNo.equalsIgnoreCase("no")) {
							// Fontvalidierung waere eingeschaltet aber nicht durchgefuehrt
							errorK = errorK + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
									+ getTextResourceService().getText(locale, WARNING_XML_K_NOFONTVAL);
						}
					}

					/** Modul A **/
					Logtxt.logtxt(logFile, verapdfA);
					Logtxt.logtxt(logFile, callasA);

					/** Modul B **/
					Logtxt.logtxt(logFile, verapdfB);
					Logtxt.logtxt(logFile, callasB);

					/** Modul C **/
					Logtxt.logtxt(logFile, verapdfC);
					Logtxt.logtxt(logFile, callasC);

					/** Modul D **/
					Logtxt.logtxt(logFile, verapdfD);
					Logtxt.logtxt(logFile, callasD);
				}

				/** Modul E **/
				Logtxt.logtxt(logFile, verapdfE);
				Logtxt.logtxt(logFile, callasE);

				/** Modul F **/
				Logtxt.logtxt(logFile, verapdfF);
				Logtxt.logtxt(logFile, callasF);

				/** Modul G **/
				Logtxt.logtxt(logFile, verapdfG);
				Logtxt.logtxt(logFile, callasG);

				/** Modul H **/
				Logtxt.logtxt(logFile, verapdfH);
				Logtxt.logtxt(logFile, callasH);

				/** Modul I **/
				Logtxt.logtxt(logFile, verapdfI);
				Logtxt.logtxt(logFile, callasI);

				/** Modul J **/
				if (!isValidJ) {
					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_J_JBIG2));
				}

				/** Modul K **/
				Logtxt.logtxt(logFile, errorK);

				try {
					docPdf.close();
					// Destroy the object and set to null
					docPdf.destroyObject();
					docPdf = null;
					PdfValidatorAPI.terminate();
					File internLicenseFile = new File(directoryOfLogfile + File.separator + ".useKOSTValLicense.txt");
					if (internLicenseFile.exists()) {
						// interne Lizenz verwendet. Lizenz ueberschreiben
						internLicenseFile.delete();
						if (internLicenseFile.exists()) {
							internLicenseFile.deleteOnExit();
						}
						if (internLicenseFile.exists()) {
							Util.deleteFile(internLicenseFile);
						}
						PdfValidatorAPI.setLicenseKey(" ");
					}
				} catch (Exception ed1) {
				}
			} else {
				try {
					docPdf.close();
					// Destroy the object and set to null
					docPdf.destroyObject();
					docPdf = null;
					PdfValidatorAPI.terminate();
					File internLicenseFile = new File(directoryOfLogfile + File.separator + ".useKOSTValLicense.txt");
					if (internLicenseFile.exists()) {
						// interne Lizenz verwendet. Lizenz ueberschreiben
						internLicenseFile.delete();
						if (internLicenseFile.exists()) {
							internLicenseFile.deleteOnExit();
						}
						if (internLicenseFile.exists()) {
							Util.deleteFile(internLicenseFile);
						}
						PdfValidatorAPI.setLicenseKey(" ");
					}
				} catch (Exception ed1) {
				}

				/** Warnungen aus Modul A **/
				Logtxt.logtxt(logFile, verapdfA);
				Logtxt.logtxt(logFile, callasF);

				// Modul J noch ueberpruefen
				/** Modul J **/
				if (!isValidJ) {
					isValid = false;
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_J_JBIG2));
					}
				}

				/** Modul K **/
				if (isValidFont.equals("false")) {
					isValid = false;
					if (min) {
					} else {
						Logtxt.logtxt(logFile, errorK);
					}
				} else {
					if (errorK.isEmpty()) {
						// System.out.println("errorK.isEmpty");
					} else {
						// ggf Warning Modul K ausgeben
						Logtxt.logtxt(logFile, errorK);
					}
				}
			}
			if (report.exists()) {
				report.delete();
			}
			if (reportOriginal.exists()) {
				reportOriginal.delete();
			}
			if (verapdfReportFile.exists()) {
				verapdfReportFile.delete();
			}

		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
		}
		try {
			PdfValidatorAPI.terminate();
			docPdf.close();
			// Destroy the object and set to null
			docPdf.destroyObject();
			docPdf = null;
			File internLicenseFile = new File(directoryOfLogfile + File.separator + ".useKOSTValLicense.txt");
			if (internLicenseFile.exists()) {
				// interne Lizenz verwendet. Lizenz ueberschreiben
				internLicenseFile.delete();
				if (internLicenseFile.exists()) {
					internLicenseFile.deleteOnExit();
				}
				if (internLicenseFile.exists()) {
					Util.deleteFile(internLicenseFile);
				}
				PdfValidatorAPI.setLicenseKey(" ");
			}
		} catch (Exception ed3) {
		}

		/*
		 * durch die diversen nacharbeiten (Warnung anstelle Fehler) muss kontrolliert
		 * werden ob es valide mit Warnungen oder Invalide ist.
		 */

		if (callas && isValidCa && isValidCb && isValidCc && isValidCd && isValidCe && isValidCf && isValidCg
				&& isValidCh && isValidCi) {
			isValidCallas = true;
		}

		if (verapdfBo) {
			// verapdf eingeschaltet
			if (isValidverapdf) {
				// Validierung mit verapdf bestanden
				isValid = true;
			} else {
				// Validierung mit verapdf nicht bestanden
				if (callas) {
					// callas eingeschaltet (dual)
					if (isValidCallas) {
						// Validierung mit callas bestanden
						isValid = true;
					} else {
						// Validierung mit verapdf&callas nicht bestanden
						isValid = false;
					}
				} else {
					// Keine Validierung mit Callas; bleibt invalid
					isValid = false;
				}
			}
		} else {
			// verapdf nicht eingeschaltet
			if (callas) {
				// callas eingeschaltet (simple)
				if (isValidCallas) {
					// Validierung mit callas bestanden
					isValid = true;
				} else {
					// Validierung mit callas nicht bestanden
					isValid = false;
				}
			} else {
				// Keine Validierung
				// Fehler bereits aufgefangen
				isValid = false;
			}
		}
		if (isValidFont.equals("false") || !isValidJ) {
			isValid = false;
		}

		return isValid;
	}

	private String nodeToString(Node node) {
		String swString = "";
		try {
			StringWriter sw = new StringWriter();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
			swString = sw.toString();
			sw.close();
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException:" + e);
		}
		return swString;
	}
}