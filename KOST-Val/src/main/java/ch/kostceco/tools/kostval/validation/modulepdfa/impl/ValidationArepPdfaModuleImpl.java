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

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.pdftools.NativeLibrary;
import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kosttools.fileservice.egovdv;
import ch.kostceco.tools.kosttools.fileservice.verapdf;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kosttools.util.UtilPages;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfarepException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationArepPdfaModule;

/**
 * Soll die vorliegende PDF- / PDFA-Datei repariert werden?
 * 
 * Wenn ja wird kontrolliert ob eine entsprechende Lizenz vorhanden ist.
 * 
 * Falls vorhanden wird eine reparatur mit dmstools durchgefuehrt
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationArepPdfaModuleImpl extends ValidationModuleImpl implements ValidationArepPdfaModule {

	public static String NEWLINE = System.getProperty("line.separator");

	boolean isValid = false;
	boolean isValidPdftools = false;
	boolean isValidVerapdf = false;

	private boolean min = false;

	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath, String initFolderPath, File fileToOutputStart)
			throws ValidationApdfarepException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}

		Boolean doRepair = true;
		String noRep = "";

		try {
			/* nur reparieren wenn KEINE Signaturen enthalten sind */

			// Pfad zum Programm existiert die Dateien?
			String checkTool = egovdv.checkEgovdv(dirOfJarPath);
			if (!checkTool.equals("OK")) {
				// es fehlen Dateien -> nach indizien von Signaturen suchen
				// nur reparieren wenn keine signaturen
				if (Util.stringInFile("/SigFlags", valDatei) || Util.stringInFile("FT/Sig", valDatei)
						|| Util.stringInFile("FT /Sig", valDatei) || Util.stringInFile("Type/Sig", valDatei)
						|| Util.stringInFile("Type /Sig", valDatei)) {
					// Signaturen vorhanden oder koennen nicht ausgeschlossen werden
					// keine Reparatur durchfuehren
					noRep = noRep + getTextResourceService().getText(locale, INFO_XML_Z_NOREP_SIGN);
					doRepair = false;
				} else {
					// hoechstwahrscheinlich keine Signaturen
					// repair darf gemacht werden
				}

			} else {
				// egovdv sollte vorhanden sein
				String pathToWorkDirValdatei =

						configMap.get("PathToWorkDir");
				File workDir2 = new File(pathToWorkDirValdatei);
				Integer countSig = egovdv.execEgovdvCountSig(valDatei, workDir2, dirOfJarPath);
				Integer countSigVera = 99;
				/*
				 * Gibt mit egovdv via cmd die Anzahl Signaturen in pdf aus
				 * 
				 * 0 = keine Signatur
				 * 
				 * 999 = Fehler: Es existiert nicht alles zu egovdv
				 * 
				 * 998 = Fehler: Exception oder Report existiert nicht
				 * 
				 * 997 = Fehler: Die ersten beiden Zeilen zu egovdv fehlen
				 * 
				 * 996 = Fehler: Exception UNKNOWN Catch
				 * 
				 * @return Integer mit der Anzahl Signaturen
				 */

				if (countSig == 0) {
					// 0 = keine Signatur
					// repair darf gemacht werden
				} else {
					// signaturen vorhanden oder Fehler (996-998)
					String pathToWorkDirValdateiPre = configMap.get("PathToWorkDir");
					File workDirPre = new File(pathToWorkDirValdateiPre);
					File signatureTmpPre = new File(
							workDirPre.getAbsolutePath() + File.separator + "veraPDF_signatureTmp.xml");
					String execVerapdfSigPre = verapdf.execVerapdfSig(valDatei, workDirPre, signatureTmpPre, locale); //
					// System.out.println("Metadaten Signaturen verapdf: " + execVerapdfSigPre);

					if (countSig == 998 || countSig == 997 || countSig == 996) {
						if (execVerapdfSigPre.equals("")) {
							// 998 verapdf hat keine Metadaten zu Signaturen gefunden
							// dann hoestwahrscheinlich keine Signaturen vorhanden
							// System.out.println("998 verapdf hat keine Metadaten zu Signaturen gefunden");
							countSigVera = 0;
						}
					}
					if (signatureTmpPre.exists()) {
						signatureTmpPre.delete();
					}

					if (countSigVera == 0) {
						// keine Signaturen mit veraPDF gefunden
					} else {
						// signaturen vorhanden oder koennen nicht ausgeschlossen werden
						// keine Reparatur durchfuehren

						noRep = noRep + getTextResourceService().getText(locale, INFO_XML_Z_NOREP_SIGN);
						doRepair = false;
					}
				}
			}

			// nur reparieren wenn keine signaturen
			/*
			 * if (Util.stringInFile("/SigFlags", valDatei) || Util.stringInFile("FT/Sig",
			 * valDatei) || Util.stringInFile("FT /Sig", valDatei) ||
			 * Util.stringInFile("Type/Sig", valDatei) || Util.stringInFile("Type /Sig",
			 * valDatei)) { // Signaturen vorhanden oder koennen nicht ausgeschlossen werden
			 * // keine Reparatur durchfuehren noRep = noRep +
			 * getTextResourceService().getText(locale, INFO_XML_Z_NOREP_SIGN); doRepair =
			 * false; } else { // hoechstwahrscheinlich keine Signaturen // repair darf
			 * gemacht werden }
			 */

			// nur reparieren wenn kein portfolio
			if (Util.stringInFile("/Collection", valDatei) || Util.stringInFile("/Portfolio", valDatei)) {
				// portfolio vorhanden oder koennen nicht ausgeschlossen werden
				// keine Reparatur durchfuehren
				noRep = noRep + getTextResourceService().getText(locale, INFO_XML_Z_NOREP_PORTFOLIO);
				doRepair = false;
			} else {
				// hoechstwahrscheinlich keine Portfolio
				// repair darf gemacht werden
			}

			// nur reparieren wenn keine Attachments
			if (Util.stringInFile("/UseAttachments", valDatei) || Util.stringInFile("/Attachments", valDatei)) {
				// Attachments vorhanden oder koennen nicht ausgeschlossen werden
				// keine Reparatur durchfuehren
				noRep = noRep + getTextResourceService().getText(locale, INFO_XML_Z_NOREP_ATTACHMENTS);
				doRepair = false;
			} else {
				// hoechstwahrscheinlich keine Attachments
				// repair darf gemacht werden
			}

			// nur reparieren wenn kein 3D
			if (Util.stringInFile("Type /3D", valDatei) || Util.stringInFile("Type/3D", valDatei)
					|| Util.stringInFile("/3D/", valDatei) || Util.stringInFile("/3DAnimationStyle", valDatei)) {
				// 3D vorhanden oder koennen nicht ausgeschlossen werden
				// keine Reparatur durchfuehren
				noRep = noRep + getTextResourceService().getText(locale, INFO_XML_Z_NOREP_3D);
				doRepair = false;
			} else {
				// hoechstwahrscheinlich keine Attachements
				// repair darf gemacht werden
			}

			if (!doRepair) {
				// Meldungen warum keine Rep wurde bereits ausgegeben
				noRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA) + noRep;
				Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", noRep + "</Error>", logFile);
				return false;
			}
		} catch (Throwable e) {
			String logRepC = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, " repair signed? " + e.getMessage());
			try {
				Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRepC, logFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		// TODO Kontrolle ob repair eingeschaltet ist und nicht via cli angesprochen
		// wird
		if (logFile.getName().contains(".cli.kost-val.log")) {
			// repair steht nur via gui zur Verfuegung
			return false;
		} else {
			// via gui ok
			String pdfaRep = configMap.get("pdfarep");
			String pdfa2uRep = configMap.get("pdfa2urep");
			String logRep = "";

			if (pdfaRep.equalsIgnoreCase("yes") && pdfa2uRep.equalsIgnoreCase("yes")) {
				// reparatur in config eingeschaltet

				String pathToLogDir = configMap.get("PathToLogfile");
				File fileToLogDir = new File(pathToLogDir);
				File fileToOutput = new File(fileToLogDir.getParent() + File.separator + "OUTPUT");
				if (!fileToOutput.exists()) {
					fileToOutput.mkdir();
				}

				File outFile = new File(fileToOutput + File.separator + valDatei.getName());
				String valDateiExt = "." + FilenameUtils.getExtension(valDatei.getName()).toLowerCase();
				if (!valDateiExt.equals("pdf")) {
					// System.out.println(valDatei.getName() + " falsche Extension "+valDateiExt);
					String outFileRename = valDatei.getName().replace(valDateiExt, ".pdf");
					outFileRename = outFileRename.replace(".pdf.pdf", ".pdf");
					// System.out.println( " Ziel "+outFileRename);
					File outFileRenameExt = new File(
							outFile.getParentFile().getPath() + File.separator + outFileRename);
					// System.out.println( " Ziel-Pfad "+outFileRenameExt.getAbsolutePath());
					outFile = outFileRenameExt;
				}
				// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
				// loeschen wir es
				if (outFile.exists()) {
					outFile.delete();
				}

				// TODO Start Repair
				// System.out.println(" Start Repair ");
				String pathToKostValDir = System.getenv("USERPROFILE") + File.separator + ".kost-val_2x";
				File directoryOfConfigfile = new File(pathToKostValDir + File.separator + "configuration");
				String dmsOwner = "OwnerInit";
				String dmsKey = "KeyInit";

				String dllJacobPath = dirOfJarPath + File.separator + "jacob-1.21-x64.dll";

				try {
					dmsOwner = ValidationAinitialisationModuleImpl
							.dmsInternasOwner(directoryOfConfigfile.getAbsolutePath(), dllJacobPath);
					if (dmsOwner.equals("NoLicense") || dmsOwner.equals("noInstallation")) {
						logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
								+ getTextResourceService().getText(locale, INFO_XML_Z_NOREP_NOREP1,
										directoryOfConfigfile.getAbsolutePath());
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
						if (outFile.exists()) {
							outFile.delete();
						}
						return false;
					}
					dmsKey = ValidationAinitialisationModuleImpl.dmsInternasKey(directoryOfConfigfile.getAbsolutePath(),
							dmsOwner);
				} catch (Throwable e) {
					logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
									" repair owner/key " + e.getMessage());
					try {
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if (outFile.exists()) {
						outFile.delete();
					}
					return false;
				}

				// System.out.println("01a dllJacobPath = " + dllJacobPath);
				File dllJacobFile = new File(dllJacobPath);
				if (dllJacobFile.exists()) {
					// System.out.println("01b dllJacobPath existiert");
				} else {
					// System.out.println("01b dllJacobPath existiert NICHT");
					if (min) {
					} else {
						logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
								+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE,
										dllJacobFile.getParentFile().getAbsolutePath(), dllJacobFile.getName());
						try {
							Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (outFile.exists()) {
							outFile.delete();
						}
					}
					return false;
				}
				System.setProperty("jacob.dll.path", dllJacobPath);

				// COM im STA-Modus initialisieren
				// System.out.println("02 COM im STA-Modus initialisieren");
				ComThread.InitSTA();
				// System.out.println("02a ComThread.InitSTA");

				ActiveXComponent engine = null;
				// System.out.println("02b ActiveXComponent engine");
				try {
					// CreateObject("dmsPDFConverter.Engine")
					// System.out.println("03 CreateObject(\"dmsPDFConverter.Engine\"");
					try {
						engine = new ActiveXComponent("dmsPDFConverter.Engine");
					} catch (ComFailException e) {
						if (min) {
						} else {
							logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											"COM Failure (getMessage): " + e.getMessage()
													+ "  COM Failure (getHResult): " + e.getHResult());
							Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
							if (outFile.exists()) {
								outFile.delete();
							}
						}
						System.err.println("COM Failure (getMessage): " + e.getMessage());
						System.err.println("COM Failure (getHResult): " + e.getHResult());
						// Check if available
						// This "result" is the error information, not a successful return value.
						return false;
					}

					/*
					 * auslesen ob lizenzen in der Registry sind
					 * 
					 * falls ja ob diese noch gueltig ist
					 * 
					 * wenn ja und gueltig diese verwenden, da kein Delay von 20s
					 * 
					 * wenn nicht dann interne verwenden.
					 * 
					 * Wenn intern auslesen ob Delay-Rest vorhanden ist
					 * 
					 * falls vorhanden Info in Log und Konsole ausgeben (Abwarten, kein BatchModusin
					 * mit der internen Lizenz)
					 * 
					 * Reparatur vornehmen
					 */

					// System.out.println("03a Get License object");
					// Get License object
					Dispatch license = engine.getProperty("License").toDispatch();

					// System.out.println("03b Set license properties");
					// Set license properties
					Dispatch.put(license, "Owner", dmsOwner);
					Dispatch.put(license, "Key", dmsKey);

					// WaitTime auslesen
					Variant waitTime = Dispatch.get(license, "WaitTime");
					String milisec = waitTime.toString();
					Integer sec = Integer.parseInt(milisec);
					sec = sec / 1000;
					if (!sec.equals(0)) {
						System.out.print("  You have to wait " + sec
								+ " seconds because the internal dmstools license does not have batch mode. ");
					}

					// System.out.println("04 Engine.IgnoreErrors = False");
					// Engine.IgnoreErrors = False
					engine.setProperty("IgnoreErrors", new Variant(false));

					// System.out.println("05 Engine.Interactive = False");
					// Engine.Interactive = False
					engine.setProperty("Interactive", new Variant(false));

					// System.out.println("05a Engine.ConformanceLevel = 5");
					// Engine.ConformanceLevel = 5
					// 2u=5 2b=4 1b=2 4=9? 4e=10?
					engine.setProperty("ConformanceLevel", 5);

					// System.out.println("06 Engine.AddPDF");
					// Engine.AddPDF "c:\in.pdf", ""
					// Dispatch.call(engine, "AddPDF", "c:\\Temp\\in.pdf", "");
					Dispatch.call(engine, "AddPDF", valDatei.getAbsolutePath(), "");

					// System.out.println("07 Engine.Save");
					// Engine.Save "c:\out.pdf"
					// Dispatch.call(engine, "Save", "c:\\Temp\\out.pdf");
					Dispatch.call(engine, "Save", outFile.getAbsolutePath());

					// System.out.println("PDF erfolgreich verarbeitet.");
				} catch (Exception e) {
					if (min) {
					} else {
						logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "Repair DMS Failure: ");
						try {
							Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if (outFile.exists()) {
							outFile.delete();
						}
					}
					e.printStackTrace();
					return false;

				} finally {
					// COM sauber freigeben
					if (engine != null) {
						engine.safeRelease();
					}
					ComThread.Release();
				}
				if (!outFile.exists()) {
					logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
							+ getTextResourceService().getText(locale, INFO_XML_Z_NOREP_NOOUTPUT);
					try {
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (outFile.exists()) {
						outFile.delete();
					}
					return false;
				}
				// End Repair

				// TODO outfile validieren
				// nur veraPDF und PDF Tools (kein JBIG2 und Font)

				// Create object
				PdfValidatorAPI docPdf = null;

				String pathToWorkDirValdatei = configMap.get("PathToWorkDir");

				/*
				 * Beim schreiben ins Workverzeichnis trat ab und zu ein fehler auf.
				 * entsprechend wird es jetzt ins logverzeichnis geschrieben
				 */
				File verapdfReportFile = new File(pathToLogDir + File.separator + "veraPDF.OUTPUT.xml");
				// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
				// loeschen wir es
				if (verapdfReportFile.exists()) {
					verapdfReportFile.delete();
				}

				String level = "2U";

				boolean pdftools = false;
				boolean verapdf = false;

				String pdftoolsConfig = configMap.get("pdftools");
				String verapdfConfig = configMap.get("verapdf");

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
						File internLicenseFile = new File(
								directoryOfLogfile + File.separator + ".useKOSTValLicense.txt");
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
				}

				if (!verapdf && !pdftools) {
					// pdf Validierung nicht moeglich
					configMap.put("pdfavalidation", "no");
				}

				// Validierung nur mit PDF Tools
				if (pdftools && !verapdf) {
					// System.out.println("Validierung nur mit PDF Tools ");
					isValidPdftools = validatePDFTools(docPdf, outFile, level, directoryOfLogfile);
					isValid = isValidPdftools;
					// System.out.println("PDF Tools " + isValidPdftools);
				}

				// Validierung nur mit veraPDF
				if (!pdftools && verapdf) {
					isValidVerapdf = validateVeraPDF(outFile, pathToWorkDirValdatei, dirOfJarPath, level,
							verapdfReportFile);
					isValid = isValidVerapdf;
					// System.out.println("Validierung nur mit veraPDF ");
					// System.out.println("veraPDF " + isValidVerapdf);
				}

				// evtl Duale Validierung
				if (pdftools && verapdf) {
					// System.out.println("Dualevalidierung pT" + pT + " cpT " + cPT);
					if (pT == 3) {
						// keine Validierung mit PDF Tools mehr moeglich (Lizenz massiv ueberschritten)
						// Validierung nur mit veraPDF
						isValidVerapdf = validateVeraPDF(outFile, pathToWorkDirValdatei, dirOfJarPath, level,
								verapdfReportFile);
						isValid = isValidVerapdf;
						// System.out.println("veraPDF " + isValidVerapdf);
						// System.out.println("PDF Tools ueberschritten 3");
					} else if (pT <= 1) {
						// System.out.println("PDF Tools 1 ");
						// PDF Tools als Hauptvalidator, da Lizenz eingehalten
						isValidPdftools = validatePDFTools(docPdf, outFile, level, directoryOfLogfile);
						isValid = isValidPdftools;
						// System.out.println("PDF Tools 1 " + isValidPdftools);
						if (isValidPdftools) {
							isValid = true;
						} else {
							// evtl invalid --> Zweitmeinung anholen
							isValidVerapdf = validateVeraPDF(outFile, pathToWorkDirValdatei, dirOfJarPath, level,
									verapdfReportFile);
							isValid = isValidVerapdf;
							// System.out.println("veraPDF " + isValidVerapdf);
						}
					} else {
						// veraPDF als Hauptvalidator, da Lizenz von PDF Tools bereits ueberschritten
						isValidVerapdf = validateVeraPDF(outFile, pathToWorkDirValdatei, dirOfJarPath, level,
								verapdfReportFile);
						// System.out.println("veraPDF " + isValidVerapdf);
						if (isValidVerapdf) {
							isValid = true;
						} else {
							// evtl invalid --> Zweitmeinung anholen
							isValidPdftools = validatePDFTools(docPdf, outFile, level, directoryOfLogfile);
							isValid = isValidPdftools;
							// System.out.println("PDF Tools 2 " + isValidPdftools);
						}
					}
				}
				try {
					if (verapdfReportFile.exists()) {
						verapdfReportFile.delete();
					}
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

					// TODO: erledigt: Ggf Fehler und Warnungen ausgeben
					if (!isValid) {
						// Reparierte Datei ist invalid PREMIS [3]
						// schreiben
						logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
								+ getTextResourceService().getText(locale, INFO_XML_Z_NOREP_INVALID);
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
						// outFile wird im Ausbauschritt 2 geloescht
						outFile.delete();
						return false;
					} else {
						// Reparierte Datei ist valid PREMIS [2]

						// TODO Datei ersetzten
						if (initFolderPath != null && !initFolderPath.isEmpty()) {
							char lastChar = initFolderPath.charAt(initFolderPath.length() - 1);
							String lastStr = lastChar + "";
							if (lastStr.equals("/") || lastStr.equals("\\")) {
								if (initFolderPath != null && initFolderPath.length() > 0) {
									initFolderPath = initFolderPath.substring(0, initFolderPath.length() - 1);
								}
							}
						}
						String valDateiPath = valDatei.getAbsolutePath();
						File newOutFile = new File(
								valDateiPath.replace(initFolderPath, fileToOutputStart.getAbsolutePath()));
						Util.copyFile(outFile, newOutFile);
						logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
								+ getTextResourceService().getText(locale, INFO_XML_Z_REP_VALID, "2u",
										newOutFile.getAbsolutePath());
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
						if (outFile.exists()) {
							outFile.delete();
						}
						// Reparatur erfolgreich
						return true;
					}
				} catch (Exception e) {
					logRep = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Z_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage());
					try {
						Util.oldnewstring("<ErrorZrepPdfa></ErrorZrepPdfa>", logRep, logFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if (outFile.exists()) {
						outFile.delete();
					}
				}
			} else {
				// keine Reparatur konfiguriert
				// PREMIS 1 sollte vorgaenig abgefangen sein
				return false;
			}
		}
		return false;
	}

	private Boolean validatePDFTools(PdfValidatorAPI docPdf, File valDatei, String level, File directoryOfLogfile) {
		// TODO Validierung mit PDFTools
		boolean isValidPdfTools = false;
		int iCategory = 999999999;

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
						return false;
					}
				}
			} else {
				if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
					if (min) {
						return false;
					} else {
						return false;
					}
				} else {
					if (min) {
						return false;
					} else {
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
			return false;
		}
		return isValidPdfTools;
	}

	private Boolean validateVeraPDF(File valDatei, String pathToWorkDirValdatei, String dirOfJarPath, String level,
			File verapdfReportFile) {
		boolean isValidverapdf = false;
		try {
			try {
				/*
				 * TODO: Erledigt Start mit veraPDF
				 * 
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

				if (execVerapdfVal.equals("invalid")) {
					isValidverapdf = false;
				} else if (!verapdfReportFile.exists()) {
					isValidverapdf = false;
				} else {
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
					}
				}
				if (verapdfReportFile.exists()) {
					verapdfReportFile.delete();
				}
			} catch (Exception e) {
				isValidverapdf = false;
			}
		} catch (Exception e) {
			isValidverapdf = false;
		}
		return isValidverapdf;
	}
}