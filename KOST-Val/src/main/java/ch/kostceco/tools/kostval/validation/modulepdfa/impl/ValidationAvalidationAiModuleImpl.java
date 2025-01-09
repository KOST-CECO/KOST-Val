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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
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
import ch.kostceco.tools.kosttools.util.UtilCharacter;
import ch.kostceco.tools.kosttools.util.UtilPages;
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

	boolean isValid = false;
	boolean isValidPdftools = false;
	boolean isValidVerapdf = false;

	String pdftoolsA = "";
	String pdftoolsB = "";
	String pdftoolsC = "";
	String pdftoolsD = "";
	String pdftoolsE = "";
	String pdftoolsF = "";
	String pdftoolsG = "";
	String pdftoolsH = "";
	String pdftoolsI = "";

	String errorK = "";
	boolean isValidVa = true;
	boolean isValidVb = true;
	boolean isValidVc = true;
	boolean isValidVd = true;
	boolean isValidVe = true;
	boolean isValidVf = true;
	boolean isValidVg = true;
	boolean isValidVh = true;
	boolean isValidVi = true;
	String verapdfA = "";
	String verapdfB = "";
	String verapdfC = "";
	String verapdfD = "";
	String verapdfE = "";
	String verapdfF = "";
	String verapdfG = "";
	String verapdfH = "";
	String verapdfI = "";

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationApdfavalidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}
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

		/*
		 * Beim schreiben ins Workverzeichnis trat ab und zu ein fehler auf.
		 * entsprechend wird es jetzt ins logverzeichnis geschrieben
		 */

		String pdf3warning = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
				+ getTextResourceService().getText(locale, WARNING_XML_A_PDFA3);

		File verapdfReportFile = new File(pathToLogDir + File.separator + "veraPDF.xml");
		// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
		// loeschen wir es
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

		boolean isValidJ = true;
		String isValidFont = "noFontVal";
		String fontYesNo = configMap.get("pdfafontpt");
		boolean ignorUndefinied = false;
		// String undefiniedWarningString = "";
		// int symbolWarning = 0;
		// String symbolWarningString = "";
		String fontErrorIgnor = "I";
		boolean pdftools = false;
		boolean verapdf = false;

		String pdftoolsConfig = configMap.get("pdftools");
		String detailptConfig = configMap.get("detailpt");
		String verapdfConfig = configMap.get("verapdf");
		String detailvpConfig = configMap.get("detailvp");

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		if (pdftoolsConfig.contentEquals("yes")) {
			// pdftools Validierung gewuenscht
			pdftools = true;
		}
		if (verapdfConfig.contentEquals("yes")) {
			// verapdf Validierung gewuenscht
			verapdf = true;
		}

		int cPT = 0;
		int pT = 0;

		try {
			if (pdftools) {
				File internLicenseFile = new File(directoryOfLogfile + File.separator + ".useKOSTValLicense.txt");
				if (internLicenseFile.exists()) {
					// interne Lizenz verwendet.
					cPT = UtilPages.getPages(directoryOfLogfile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cPT < 72000) {
			// alles iO pT = 0
		} else if (cPT < 100000) {
			// Lizenz leicht ueberschritten, Warnung ausgeben
			pT = 1;
		} else if (cPT < 144000) {
			// Lizenz deutlich ueberschritten, Warnung ausgeben und
			// verzoegern
			pT = 2;
		} else {
			// Lizenz massiv ueberschritten, Abbrechen
			pT = 3;
			fontYesNo = "no";
		}

		if (!verapdf && !pdftools) {
			// pdf Validierung nicht moeglich
			configMap.put("pdfavalidation", "no");
			fontYesNo = "no";
		}

		// Validierung nur mit PDF Tools
		if (pdftools && !verapdf) {
			isValidPdftools = validatePDFTools(docPdf, valDatei, level, detailptConfig, locale, logFile, warning3to2,
					directoryOfLogfile);
			isValid = isValidPdftools;
		}

		// Validierung nur mit veraPDF
		if (!pdftools && verapdf) {
			isValidVerapdf = validateVeraPDF(valDatei, pathToWorkDirValdatei, dirOfJarPath, level, verapdfReportFile,
					locale, logFile, detailvpConfig, warning3to2);
			isValid = isValidVerapdf;
			fontYesNo = "no";
		}

		// evtl Duale Validierung
		if (pdftools && verapdf) {
			if (pT == 3) {
				// keine Validierung mit PDF Tools mehr moeglich (Lizenz massiv ueberschritten)
				// Validierung nur mit veraPDF
				isValidVerapdf = validateVeraPDF(valDatei, pathToWorkDirValdatei, dirOfJarPath, level,
						verapdfReportFile, locale, logFile, detailvpConfig, warning3to2);
				isValid = isValidVerapdf;
				fontYesNo = "no";
			} else if (pT == 1) {
				// PDF Tools als Hauptvalidator, da Lizenz eingehalten
				isValidPdftools = validatePDFTools(docPdf, valDatei, level, detailptConfig, locale, logFile,
						warning3to2, directoryOfLogfile);
				if (isValidPdftools) {
					isValid = true;
				} else {
					// evtl invalid --> Zweitmeinung anholen
					isValidVerapdf = validateVeraPDF(valDatei, pathToWorkDirValdatei, dirOfJarPath, level,
							verapdfReportFile, locale, logFile, detailvpConfig, warning3to2);
					isValid = isValidVerapdf;
				}
			} else {
				// veraPDF als Hauptvalidator, da Lizenz von PDF Tools bereits ueberschritten
				isValidVerapdf = validateVeraPDF(valDatei, pathToWorkDirValdatei, dirOfJarPath, level,
						verapdfReportFile, locale, logFile, detailvpConfig, warning3to2);
				if (isValidVerapdf) {
					isValid = true;
				} else {
					// evtl invalid --> Zweitmeinung anholen
					isValidPdftools = validatePDFTools(docPdf, valDatei, level, detailptConfig, locale, logFile,
							warning3to2, directoryOfLogfile);
					isValid = isValidPdftools;
				}
			}
		}

		try {
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

			// TODO: Erledigt: Fehler Auswertung
			if (isValid) {
				// isValid beruecksichtigt hier nur A-I
				if (!isValidJ) {
					isValid = false;
				}
				// Falls gewunscht Fontvalidierung mit PDFTools nur durchfuehren wenn
				// pdftools und verapdf (A-I) bestanden haben.
				if (!fontYesNo.equalsIgnoreCase("no")) {
					isValidFont = validateFont(docPdf, valDatei, level, pathToLogDir, fontYesNo, locale, logFile,
							directoryOfLogfile, ignorUndefinied, fontErrorIgnor);
				} else {
					// ohne pdftools auch keine Font validierung
					isValidFont = "noFontVal";
				}
				if (isValidFont.equals("false")) {
					isValid = false;
				}
			}

			// erledigt: Ggf Fehler und Warnungen ausgeben
			if (!isValid) {
				if (min) {
					return false;
				} else {
					// Warnung ausgeben wenn Fontvalidierung nicht durchgefuehrt wurde
					if (isValidFont.equals("noFontVal")) {
						if (!fontYesNo.equalsIgnoreCase("no")) {
							// Fontvalidierung waere eingeschaltet aber nicht durchgefuehrt
							errorK = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
									+ getTextResourceService().getText(locale, WARNING_XML_K_NOFONTVAL);
						}
					}

					/** Modul A **/
					Logtxt.logtxt(logFile, pdftoolsA);
					Logtxt.logtxt(logFile, verapdfA);

					/** Modul B **/
					Logtxt.logtxt(logFile, pdftoolsB);
					Logtxt.logtxt(logFile, verapdfB);

					/** Modul C **/
					Logtxt.logtxt(logFile, pdftoolsC);
					Logtxt.logtxt(logFile, verapdfC);

					/** Modul D **/
					Logtxt.logtxt(logFile, pdftoolsD);
					Logtxt.logtxt(logFile, verapdfD);

					/** Modul E **/
					Logtxt.logtxt(logFile, pdftoolsE);
					Logtxt.logtxt(logFile, verapdfE);

					/** Modul F **/
					Logtxt.logtxt(logFile, pdftoolsF);
					Logtxt.logtxt(logFile, verapdfF);

					/** Modul G **/
					Logtxt.logtxt(logFile, pdftoolsG);
					Logtxt.logtxt(logFile, verapdfG);

					/** Modul H **/
					Logtxt.logtxt(logFile, pdftoolsH);
					Logtxt.logtxt(logFile, verapdfH);

					/** Modul I **/
					Logtxt.logtxt(logFile, pdftoolsI);
					Logtxt.logtxt(logFile, verapdfI);

					/** Modul J **/
					if (!isValidJ) {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_J_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_J_JBIG2));
					}

					/** Modul K **/
					Logtxt.logtxt(logFile, errorK);
				}
			}
			if (verapdfReportFile.exists()) {
				verapdfReportFile.delete();
			}
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
		}

		/*
		 * durch die diversen nacharbeiten (Warnung anstelle Fehler) muss kontrolliert
		 * werden ob es valide mit Warnungen oder Invalide ist.
		 */

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
		pdftoolsA = "";
		pdftoolsB = "";
		pdftoolsC = "";
		pdftoolsD = "";
		pdftoolsE = "";
		pdftoolsF = "";
		pdftoolsG = "";
		pdftoolsH = "";
		pdftoolsI = "";

		errorK = "";
		verapdfA = "";
		verapdfB = "";
		verapdfC = "";
		verapdfD = "";
		verapdfE = "";
		verapdfF = "";
		verapdfG = "";
		verapdfH = "";
		verapdfI = "";

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

	private Boolean validatePDFTools(PdfValidatorAPI docPdf, File valDatei, String level, String detailConfigPpdftools,
			Locale locale, File logFile, String warning3to2, File directoryOfLogfile) {
		// TODO Validierung mit PDFTools
		boolean isValidPdfTools = false;
		int iCategory = 999999999;
		Boolean warning3to2done = false;
		String pdf3warning = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
				+ getTextResourceService().getText(locale, WARNING_XML_A_PDFA3);

		docPdf = new PdfValidatorAPI();

		try {
			UtilPages.setPages(directoryOfLogfile);
			if (docPdf.open(valDatei.getAbsolutePath(), "", NativeLibrary.COMPLIANCE.ePDFUnk)) {
				// PDF Konnte geoeffnet werden
				docPdf.setStopOnError(true);
				docPdf.setReportingLevel(1);
				if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_A_ENCRYPTED));
						return false;
					}
				}
			} else {
				if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_A_ENCRYPTED));
						return false;
					}
				} else {
					if (min) {
						return false;
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_A_PDFTOOLS_DAMAGED));
						return false;
					}
				}
			}

			docPdf = new PdfValidatorAPI();
			if (min) {
				docPdf.setStopOnError(true);
			} else {
				docPdf.setStopOnError(false);
			}
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

			// Error Category
			iCategory = docPdf.getCategories();
			/*
			 * die Zahl kann auch eine Summe von Kategorien sein z.B. 6144=2048+4096 ->
			 * getCategoryText gibt nur die erste Kategorie heraus (z.B. 2048)
			 */

			if (success == 0 && iCategory == 0) {
				// valide
				isValidPdfTools = true;
			}

		} catch (Exception e) {

			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "Exec PDF Tools: " + e.getMessage()));
			return false;
		}
		if (!isValidPdfTools) {
			boolean exponent0 = false;
			boolean exponent1 = false;
			boolean exponent2 = false;
			boolean exponent3 = false;
			boolean exponent4 = false;
			boolean exponent5 = false;
			boolean exponent6 = false;
			boolean exponent7 = false;
			boolean exponent8 = false;
			boolean exponent9 = false;
			boolean exponent10 = false;
			boolean exponent11 = false;
			boolean exponent12 = false;
			boolean exponent13 = false;
			boolean exponent14 = false;
			boolean exponent15 = false;
			boolean exponent16 = false;
			boolean exponent17 = false;
			boolean exponent18 = false;

			int iExp0 = (int) Math.pow(2, 0);
			int iExp1 = (int) Math.pow(2, 1);
			int iExp2 = (int) Math.pow(2, 2);
			int iExp3 = (int) Math.pow(2, 3);
			int iExp4 = (int) Math.pow(2, 4);
			int iExp5 = (int) Math.pow(2, 5);
			int iExp6 = (int) Math.pow(2, 6);
			int iExp7 = (int) Math.pow(2, 7);
			int iExp8 = (int) Math.pow(2, 8);
			int iExp9 = (int) Math.pow(2, 9);
			int iExp10 = (int) Math.pow(2, 10);
			int iExp11 = (int) Math.pow(2, 11);
			int iExp12 = (int) Math.pow(2, 12);
			int iExp13 = (int) Math.pow(2, 13);
			int iExp14 = (int) Math.pow(2, 14);
			int iExp15 = (int) Math.pow(2, 15);
			int iExp16 = (int) Math.pow(2, 16);
			int iExp17 = (int) Math.pow(2, 17);
			int iExp18 = (int) Math.pow(2, 18);

			// Invalide Kategorien von PDF-Tools
			if (iCategory >= iExp18) {
				exponent18 = true;
				iCategory = iCategory - iExp18;
			}
			if (iCategory >= iExp17) {
				exponent17 = true;
				iCategory = iCategory - iExp17;
			}
			if (iCategory >= iExp16) {
				exponent16 = true;
				iCategory = iCategory - iExp16;
			}
			if (iCategory >= iExp15) {
				exponent15 = true;
				iCategory = iCategory - iExp15;
			}
			if (iCategory >= iExp14) {
				exponent14 = true;
				iCategory = iCategory - iExp14;
			}
			if (iCategory >= iExp13) {
				exponent13 = true;
				iCategory = iCategory - iExp13;
			}
			if (iCategory >= iExp12) {
				exponent12 = true;
				iCategory = iCategory - iExp12;
			}
			if (iCategory >= iExp11) {
				exponent11 = true;
				iCategory = iCategory - iExp11;
			}
			if (iCategory >= iExp10) {
				exponent10 = true;
				iCategory = iCategory - iExp10;
			}
			if (iCategory >= iExp9) {
				exponent9 = true;
				iCategory = iCategory - iExp9;
			}
			if (iCategory >= iExp8) {
				exponent8 = true;
				iCategory = iCategory - iExp8;
			}
			if (iCategory >= iExp7) {
				exponent7 = true;
				iCategory = iCategory - iExp7;
			}
			if (iCategory >= iExp6) {
				exponent6 = true;
				iCategory = iCategory - iExp6;
			}
			if (iCategory >= iExp5) {
				exponent5 = true;
				iCategory = iCategory - iExp5;
			}
			if (iCategory >= iExp4) {
				exponent4 = true;
				iCategory = iCategory - iExp4;
			}
			if (iCategory >= iExp3) {
				exponent3 = true;
				iCategory = iCategory - iExp3;
			}
			if (iCategory >= iExp2) {
				exponent2 = true;
				iCategory = iCategory - iExp2;
			}
			if (iCategory >= iExp1) {
				exponent1 = true;
				iCategory = iCategory - iExp1;
			}
			if (iCategory >= iExp0) {
				exponent0 = true;
				iCategory = iCategory - iExp0;
			}
			/** Modul A **/
			if (exponent1) {
				pdftoolsA = pdftoolsA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_1, "PDF Tools: iCategory_1");
			}
			if (exponent2) {
				pdftoolsA = pdftoolsA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_2, "PDF Tools: iCategory_2");
				return false;
			}

			/** Modul B **/
			if (exponent0) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_0, "PDF Tools: iCategory_0");
			}
			if (exponent7) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_7, "PDF Tools: iCategory_7");
			}
			if (exponent18) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_18, "PDF Tools: iCategory_18");
			}

			/** Modul C **/
			if (exponent3) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_3, "PDF Tools: iCategory_3");
			}
			if (exponent4) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_4, "PDF Tools: iCategory_4");
			}
			if (exponent5) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_5, "PDF Tools: iCategory_5");
			}
			if (exponent6) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_6, "PDF Tools: iCategory_6");
			}

			/** Modul D **/
			if (exponent8) {
				pdftoolsD = pdftoolsD + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_8, "PDF Tools: iCategory_8");
			}
			if (exponent9) {
				pdftoolsD = pdftoolsD + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_9, "PDF Tools: iCategory_9");
			}

			/** Modul E **/
			if (exponent10) {
				pdftoolsE = pdftoolsE + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_10, "PDF Tools: iCategory_10");
			}

			/** Modul F **/
			if (exponent11) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_11, "PDF Tools: iCategory_11");
			}
			if (exponent12) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_12, "PDF Tools: iCategory_12");
			}
			if (exponent13) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_13, "PDF Tools: iCategory_13");
			}
			if (exponent14) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_14, "PDF Tools: iCategory_14");
			}

			/** Modul G **/
			if (exponent15) {
				pdftoolsG = pdftoolsG + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_15, "PDF Tools: iCategory_15");
			}

			/** Modul H **/
			if (exponent16) {
				pdftoolsH = pdftoolsH + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_16, "PDF Tools: iCategory_16");
			}

			/** Modul I **/
			if (exponent17) {
				pdftoolsI = pdftoolsI + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_I_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_17, "PDF Tools: iCategory_17");
			}

			// Ermittlung Detail-Fehlermeldungen von pdftools
			// (entspricht -rd)
			PdfError err = docPdf.getFirstError();
			boolean rd = false;

			if (detailConfigPpdftools.equalsIgnoreCase("detail") || detailConfigPpdftools.equalsIgnoreCase("yes")) {
				rd = true;
			}
			if (err != null && rd) {
				for (; err != null; err = docPdf.getNextError()) {
					// Ermittlung der einzelnen Error Code und Message
					int errorCode = err.getErrorCode();
					String errorCode0x = String.format("0x%08X", errorCode);
					String errorMsg = err.getMessage();

					// aus errorMsg < und > entfernen --> Probleme mit XML
					errorMsg = errorMsg.replace("<", "'");
					errorMsg = errorMsg.replace(">", "'");

					// Ausgabe
					String errorMsgCode0xText = errorMsg + " [PDF Tools: " + errorCode0x + "]";
					String errorMsgCode0x = " - " + errorMsgCode0xText;
					// System.out.println(errorMsgCode0x);
					String detailWarning3to2 = "The XMP property 'pdfaid:part' has the invalid value '3'. Required is '2'. [PDF Tools: 0x8341052E]";

					if (errorMsgCode0x.contains("The value of the CIDSet[")) {
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[1", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[2", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[3", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[1", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[2", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[3", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[4", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[5", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[6", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[7", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[8", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[9", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[0", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[1", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[2", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[3", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[4", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[5", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[6", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[7", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[8", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[9", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[0", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[1", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[2", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[3", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[4", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[5", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[6", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[7", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[8", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[9", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[0", "The value of the CIDSet[");
						errorMsgCode0x=errorMsgCode0x.replace("The value of the CIDSet[]", "The value of the CIDSet");					}
					if (warning3to2.equalsIgnoreCase("yes") && errorMsgCode0xText.contains(detailWarning3to2)) {
						// Fehler wird ignoriert. Es wird ggf eine Warnung ausgegeben.
						if (!warning3to2done) {
							// Fehler wurde noch nicht ausgegeben.
							Logtxt.logtxt(logFile, pdf3warning);
							warning3to2done = true;
						}
					} else if ("The document does not conform to the requested standard. [PDF Tools: 0x83410612]"
							.contains(errorMsgCode0xText)) {
						// Fehler wird ignoriert. Entsprechend wird kein Detail geschrieben.
					} else {
						// Fehler wird nicht ignoriert und dem Modul zugeordnet
						if (errorMsgCode0x.toLowerCase().contains("graphic")
								|| errorMsgCode0x.toLowerCase().contains("image")
								|| errorMsgCode0x.toLowerCase().contains("interpolate")
								|| errorMsgCode0x.toLowerCase().contains("icc")
								|| errorMsgCode0x.toLowerCase().contains("color")
								|| errorMsgCode0x.toLowerCase().contains("colour")
								|| errorMsgCode0x.toLowerCase().contains("rgb")
								|| errorMsgCode0x.toLowerCase().contains("rvb")
								|| errorMsgCode0x.toLowerCase().contains("cmyk")
								|| errorMsgCode0x.toLowerCase().contains("cmjn")
								|| errorMsgCode0x.toLowerCase().contains("outputintent")
								|| errorMsgCode0x.toLowerCase().contains("jpeg2000")
								|| errorMsgCode0x.toLowerCase().contains("devicegray")
								|| errorMsgCode0x.toLowerCase().contains("key 'tr'.")
								|| errorMsgCode0x.toLowerCase().contains("tr2")) {
							if (pdftoolsC.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsC = pdftoolsC
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("police")
								|| errorMsgCode0x.toLowerCase().contains("font")
								|| errorMsgCode0x.toLowerCase().contains("gly")
								|| errorMsgCode0x.toLowerCase().contains("truetype")
								|| errorMsgCode0x.toLowerCase().contains("unicode")
								|| errorMsgCode0x.toLowerCase().contains("cid")
								|| errorMsgCode0x.toLowerCase().contains("encoding")
								|| errorMsgCode0x.toLowerCase().contains("charset")) {
							if (pdftoolsD.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsD = pdftoolsD
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("disponibi")
								|| errorMsgCode0x.toLowerCase().contains("accessibi")
								|| errorMsgCode0x.toLowerCase().contains("markinfo")
								|| errorMsgCode0x.toLowerCase().contains("structree")
								|| errorMsgCode0x.toLowerCase().contains("structure tree root")
								|| errorMsgCode0x.toLowerCase().contains(" cross reference ")
								|| errorMsgCode0x.toLowerCase()
										.contains(" but must be a standard type. [PDF Tools: 0x00418607]")
								|| errorMsgCode0x.toLowerCase().contains("strukturbaum")) {
							if (pdftoolsI.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsI = pdftoolsI
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_I_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("structure")
								|| errorMsgCode0x.toLowerCase().contains(" ocproperties")
								|| errorMsgCode0x.toLowerCase().contains(" lzw")
								|| errorMsgCode0x.toLowerCase().contains(" structelem")
								|| errorMsgCode0x.toLowerCase().contains(" xref")
								|| errorMsgCode0x.toLowerCase().contains(" eol")) {
							if (pdftoolsB.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsB = pdftoolsB
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("metad")
								|| errorMsgCode0x.toLowerCase().contains("xmp")
								|| errorMsgCode0x.toLowerCase().contains("xml")
								|| errorMsgCode0x.toLowerCase().contains("key 'filter'.")
								|| errorMsgCode0x.toLowerCase().contains("schema description for namespace")
								|| errorMsgCode0x.toLowerCase().contains("multiple occurrences of property 'pdf:")
								|| errorMsgCode0x.toLowerCase().contains("is not defined in schema")) {
							if (pdftoolsH.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsH = pdftoolsH
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("transparen")) {
							if (pdftoolsE.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsE = pdftoolsE
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("action")
								|| errorMsgCode0x.toLowerCase().contains("aa")
								|| errorMsgCode0x.toLowerCase().contains("key 'a'")
								|| errorMsgCode0x.toLowerCase().contains("javascript")) {
							if (pdftoolsG.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsG = pdftoolsG
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("annotation")
								|| errorMsgCode0x.toLowerCase().contains("embedd")
								|| errorMsgCode0x.toLowerCase().contains("comment")
								|| errorMsgCode0x.toLowerCase().contains("structure")
								|| errorMsgCode0x.toLowerCase().contains("print")
								|| errorMsgCode0x.toLowerCase().contains("incorpor")
								|| errorMsgCode0x.toLowerCase().contains("key f ")
								|| errorMsgCode0x.toLowerCase().contains("appearance")) {
							if (pdftoolsF.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsF = pdftoolsF
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else {
							if (pdftoolsA.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsA = pdftoolsA
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}
						}
					}
				}

				// Kontrolle ob details noch existieren
				if (pdftoolsA.equals("") && pdftoolsB.equals("") && pdftoolsC.equals("") && pdftoolsD.equals("")
						&& pdftoolsE.equals("") && pdftoolsF.equals("") && pdftoolsG.equals("") && pdftoolsH.equals("")
						&& pdftoolsI.equals("")) {
					isValidPdfTools = true;
				}
			}
		}
		return isValidPdfTools;
	}

	private String validateFont(PdfValidatorAPI docPdf, File valDatei, String level, String pathToLogDir,
			String fontYesNo, Locale locale, File logFile, File directoryOfLogfile, Boolean ignorUndefinied,
			String fontErrorIgnor) {
		// TODO FontValidierung
		String isValidFont = "noFontVal";
		errorK = "";
		docPdf = new PdfValidatorAPI();
		try {
			UtilPages.setPages(directoryOfLogfile);
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

			String pathToFontOutput = pathToLogDir + File.separator + valDatei.getName() + "_FontValidation.xml";
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
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
										"Failed to write font validation information (error): " + e2.getMessage()));
			} catch (IllegalArgumentException | SecurityException e) {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
										"Failed to write font validation information (exception): " + e.getMessage()));
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
				System.out.println(
						String.format("Failed to write font validation information: %s", docPdf.getErrorMessage()));
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

			// erledigt: Start der Font-Auswertung betreffend unbekannt und undefiniert

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
					return "false";
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
				String elementUnknown = doc.getElementsByTagName("characterUnknown").item(0).getTextContent();
				// int unknown=Integer.valueOf( elementUnknown );
				String elementUnknownP = doc.getElementsByTagName("characterUnknownPercentage").item(0)
						.getTextContent();
				double unknownPdouble = Double.valueOf(elementUnknownP);
				String elementUndefined = doc.getElementsByTagName("characterUndefined").item(0).getTextContent();
				// int undefined=Integer.valueOf( elementUndefined );
				String elementUndefinedP = doc.getElementsByTagName("characterUndefinedPercentage").item(0)
						.getTextContent();
				double undefinedPdouble = Double.valueOf(elementUndefinedP);
				double elementTolerance = unknownPdouble + undefinedPdouble;
				String docInfo = nodeToString(doc.getElementsByTagName("docInfo").item(0));
				String docInfoCharacterCount = docInfo + "<characterCount>" + elementCount + "</characterCount>";
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
						return "false";
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
							Files.write(Paths.get(modulKFilePath), line.getBytes(), StandardOpenOption.APPEND);
							return "false";
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
									 * elementTolerance = unknownPdouble + undefinedPdouble; toleranzschwelle nicht
									 * ueberschritten undefiniertes kann bei tolerant ignoriert werden
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
												 * -> cid = 60 -> glyphId = 60 -> unicode = U+FFFD -> unicodeUndefined =
												 * true
												 */
												if (attName.equalsIgnoreCase("unicode")) {
													unicode = true;
												}
												if (attNameValue.equalsIgnoreCase("unicodeUndefined = true")) {
													charUndef = true;
												}
											}
											if (!unicode) {
												// unicode nicht bekannt -> node weiteranalysieren

												/*
												 * wenn I = tolerant analysieren ob es ein bekanntes Abstand- oder
												 * Aufzaehlungszeichen ist.
												 * 
												 * ist es ein solches bekanntes Zeichen das Zeichen ignorieren ansonsten
												 * belassen
												 */
												if (fontErrorIgnor.equalsIgnoreCase("I")) {
													String characterTextContent = charNode.getTextContent();
													characterTextContent = characterTextContent.replaceAll("\n", "");
													boolean ignoreCharacter = UtilCharacter
															.ignoreCharacter(characterTextContent);

													if (ignoreCharacter) {
														/*
														 * => C) Bekannte Abstaende und Aufzaehlungszeichen ignorieren
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
													 * => B) Undefinierte Zeichen ignorieren wenn Maengel<=20%
													 *
													 * Node zum loeschen vormerken, da er ignoriert werden kann (I)
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
									TransformerFactory transformerFactory = TransformerFactory.newInstance();
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
										} else if (fontNode.hasAttributes() && fontErrorIgnor.equalsIgnoreCase("I")) {
											NamedNodeMap attrs = fontNode.getAttributes();
											for (int i = 0; i < attrs.getLength(); i++) {
												Attr attribute = (Attr) attrs.item(i);
												String attName = attribute.getName();
												String attValue = attribute.getValue();

												/*
												 * <font fontfile= "TrueType" fullname="Symbol" name="Symbol"
												 * objectNo="161" type="Type0 (CIDFontType2)" >
												 */
												if (attName.equalsIgnoreCase("name")) {
													if (attValue.contains("Symbol") || attValue.contains("Webdings")
															|| attValue.contains("Wingdings")
															|| attValue.contains("Math") || attValue.contains("symbol")
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
										blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
									}
									// Ende Bereinigung

									/*
									 * Nach der Bereinigung ermitteln wieviele unbekannt und undefiniert sind und ob
									 * es ein Fehler ist oder nicht
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
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
													+ getTextResourceService().getText(locale, ERROR_XML_K_OVERVIEW2,
															count, unknownW, unknownPdoubleW);
										} else {
											isValidFont = "false";
											errorK = errorK
													+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
													+ getTextResourceService().getText(locale, ERROR_XML_K_OVERVIEW,
															elementCount, elementUnknown, elementUnknownP,
															elementUndefined, elementUndefinedP);
										}
									}
									if (isValidFont.equals("false")) {
										Node nodeInfo = docError.getElementsByTagName("docInfo").item(0);
										String stringInfo = nodeToString(nodeInfo);
										Node nodeFonts = docError.getElementsByTagName("fonts").item(0);
										String stringFonts = nodeToString(nodeFonts);
										errorK = errorK + getTextResourceService().getText(locale, ERROR_XML_K_DETAIL,
												stringInfo, stringFonts);
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
										String attNameValue = attribute.getName() + " = " + attribute.getValue();
										/*
										 * -> cid = 60 -> glyphId = 60 -> unicode = U+FFFD -> unicodeUndefined = true
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
							NodeList blankTextNodes = (NodeList) xpath.evaluate(docError, XPathConstants.NODESET);

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
										String attNameValue = attribute.getName() + " = " + attribute.getValue();
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
								errorK = errorK + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_K_PDFA)
										+ getTextResourceService().getText(locale, ERROR_XML_K_OVERVIEW, elementCount,
												elementUnknown, elementUnknownP, elementUndefined, elementUndefinedP);
							}

							if (isValidFont.equals("false")) {
								Node nodeInfo = docError.getElementsByTagName("docInfo").item(0);
								String stringInfo = nodeToString(nodeInfo);
								Node nodeFonts = docError.getElementsByTagName("fonts").item(0);
								String stringFonts = nodeToString(nodeFonts);
								errorK = errorK + getTextResourceService().getText(locale, ERROR_XML_K_DETAIL,
										stringInfo, stringFonts);
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
						return "false";
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
			docPdf.close();
			// Destroy the object and set to null
			docPdf.destroyObject();
			docPdf = null;
			PdfValidatorAPI.terminate();
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "Exec PDF Tools: " + e.getMessage()));
			return "false";
		}
		return isValidFont;
	}

	private Boolean validateVeraPDF(File valDatei, String pathToWorkDirValdatei, String dirOfJarPath, String level,
			File verapdfReportFile, Locale locale, File logFile, String detailConfig, String warning3to2) {
		boolean isValidverapdf = false;
		try {
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
						/*
						 * <taskException type="VALIDATE" isExecuted="true" isSuccess="false"> <duration
						 * start="1734600179497" finish="1734600180445">00:00:00.948</duration>
						 * <exceptionMessage>Exception: Caught unexpected runtime exception during
						 * validation caused by exception: ReferenceError: "BM" is not
						 * defined.</exceptionMessage> </taskException>
						 * 
						 * unexpected runtime exception haben keine rule. Entsprechend diese noch
						 * seperat ausgeben
						 *
						 * Datei Zeile fuer Zeile lesen und ermitteln ob
						 * "<exceptionMessage>Exception: Caught unexpected runtime exception" darin
						 * enthalten ist
						 */
						if (lineModif.contains("<exceptionMessage>Exception: Caught unexpected runtime exception")) {
							String lineModifRTE = lineModif.replace("<exceptionMessage>", "");
							lineModifRTE = lineModifRTE.replace("</exceptionMessage>", "");

							verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
									+ "<Message>" + lineModifRTE + " [verapdf runtime exception]</Message>"
									+ "</Error>";
							isValidVa = false;
						}
						if (lineModif.contains("<batchSummary totalJobs") && lineModif.contains("encrypted=")
								&& !lineModif.contains("encrypted=\"0\"")) {
							String lineModifRTE = lineModif.replace("<exceptionMessage>", "");
							lineModifRTE = lineModifRTE.replace("</exceptionMessage>", "");

							verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
									+ getTextResourceService().getText(locale, ERROR_XML_A_ENCRYPTED);
							isValidVa = false;
						}
						if (lineModif.contains("<batchSummary totalJobs") && lineModif.contains("outOfMemory=")
								&& !lineModif.contains("outOfMemory=\"0\"")) {
							String lineModifRTE = lineModif.replace("<exceptionMessage>", "");
							lineModifRTE = lineModifRTE.replace("</exceptionMessage>", "");

							verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
									+ "<Message>Out of memory exception [verapdf OOM exception]</Message>" + "</Error>";
							isValidVa = false;
						}
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
								if (detailConfig.equalsIgnoreCase("detail") || detailConfig.equalsIgnoreCase("yes")) {
									errorMessage = subNode.getTextContent();
									errorMessage = errorMessage.replace("<", "&lt;");
									errorMessage = errorMessage.replace(">", "&gt;");
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
											+ "<Message>" + descriptionTrans + "</Message>" + errorMessage + "</Error>";
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
				if (isValidVa && isValidVb && isValidVc && isValidVd && isValidVe && isValidVf && isValidVg && isValidVh
						&& isValidVi) {
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
		if (!isValidverapdf) {
			// Kontrolle ob ein Fehler gespeichert wurde.
			// Wenn nicht eine allg. Fehlermeldung ausgeben.
			String verapdfABCDEFGHI = verapdfA + verapdfB + verapdfC + verapdfD + verapdfE + verapdfF + verapdfG
					+ verapdfH + verapdfI;
			if (verapdfABCDEFGHI.equals("")) {
				// Kein Fehler gespeichert
				verapdfA = verapdfA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_SERVICEFAILED, "veraPDF",
								"veraPDF exception could be visible in the console");
			}
		}

		return isValidverapdf;
	}
}