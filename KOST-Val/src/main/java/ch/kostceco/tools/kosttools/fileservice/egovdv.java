/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO.
 * -----------------------------------------------------------------------------------------------
 * KOST-Tools is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all
 * copyright interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland,
 * 1 March 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kosttools.fileservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class egovdv {
	private static String exeDir = "resources" + File.separator + "egov-validationclient-cli";
	private static String validateBat = exeDir + File.separator + "validate.bat";

	// TODO: muss aktualisiert werden
	private static String versionKostVal = "2.4.0.0";
	private static String versionEgoDv = "2.1.5";
	private static String versionVerapdf = "1.31.17";

	private static String egovCli = exeDir + File.separator + "lib" + File.separator
			+ "intarsys-egov-validationclient-cli-" + versionEgoDv + ".jar";
	// intarsys-egov-validationclient-cli-2.1.5.jar
	private static String stringFile = "";
//	private static String stringFileEmpty = "";

	/**
	 * TODO: Listet mit egovdv via cmd die Signaturnamen in pdf auf und speichert
	 * das Ergebnis in ein File (Output). Gibt zurueck ob Output existiert oder
	 * nicht
	 * 
	 * Fuer diesen Schritt braucht es weder Internet/URL noch einen account
	 * 
	 * validate -list -f <filename> -l de -u no
	 * 
	 * @param -list List digital signatures of given PDF file
	 * @param -f    file to validate
	 * @param -l    get pdf report in the given language, supported codes: de, fr,
	 *              it, en. This is an optional parameter, if omitted de is used.
	 * @param -u    URL of the validation webservice. (Can also be defined in config
	 *              file)
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execEgovdvList(File fileToList, File output, File workDir, String dirOfJarPath)
			throws InterruptedException {
		boolean out = true;
		File fvalidateBat = new File(dirOfJarPath + File.separator + validateBat);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (output.exists()) {
			output.delete();
		}

		// validate -list -f <filename> -l de -u no

		String command = "\"\"" + fvalidateBat.getAbsolutePath() + "\" " + "-list -f \"" + fileToList.getAbsolutePath()
				+ "\" -l de -u no > \"" + output.getAbsolutePath() + "\"\"";

		// System.out.println( "" );
		// System.out.println( "command: " + command );

		String resultExec = Cmd.execToStringSplit(command, out, workDir);
		// System.out.println( "resultExec: " + resultExec );

		// egovdv gibt keine Info raus, die replaced oder ignoriert werden muss

		if (resultExec.equals("OK")) {
			if (output.exists()) {
				// alles io bleibt bei OK
			} else {
				// Datei nicht angelegt...
				resultExec = "NoReport";
			}
		}
		// System.out.println( "resultExec= " +resultExec );
		return resultExec;
	}

	/**
	 * TODO: Gibt mit egovdv via cmd die Anzahl Signaturen in pdf aus
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
	public static Integer execEgovdvCountSig(File valDatei, File workDir, String dirOfJarPath)
			throws InterruptedException {
		/*
		 * Doppelleerschlag im Pfad oder im Namen einer Datei bereitet Probleme (leerer
		 * Report) Video-Datei wird bei Doppelleerschlag in temp-Verzeichnis kopiert
		 */
		String pathToWorkDir = workDir.getAbsolutePath();
		String valDateiPath = valDatei.getAbsolutePath();
		String valDateiName = valDatei.getName().replace("  ", " ");
		valDateiName = valDateiName.replace("  ", " ");
		valDateiName = valDateiName.replace("  ", " ");

		File valDateiTemp = new File(pathToWorkDir + File.separator + valDateiName);
		File valDateiTempWorkDir = new File(pathToWorkDir + File.separator + valDateiName);
		if (valDateiPath.contains("  ")) {
			try {
				Util.copyFile(valDatei, valDateiTemp);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			valDateiTemp = valDatei;
		}

		Integer count = 0;

		// Ermittlung ob Signaturen enthalten sind
		if (!workDir.exists()) {
			workDir.mkdir();
		}

		File outputList = new File(workDir.getAbsolutePath() + File.separator + "egovdvList.txt");
		// falls das File von einem vorhergehenden Durchlauf bereits
		// existiert, loeschen wir es
		if (outputList.exists()) {
			outputList.delete();
		}

		// - Initialisierung egovdv -> existiert alles zu egovdv?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = egovdv.checkEgovdv(dirOfJarPath);
		if (!checkTool.equals("OK")) {
			// es fehlen Dateien
			count = 999;
			if (valDateiTempWorkDir.exists()) {
				valDateiTempWorkDir.delete();
			}
			return count;
		} else {
			// egovdv sollte vorhanden sein
			try {
				String resultExec = egovdv.execEgovdvList(valDateiTemp, outputList, workDir, dirOfJarPath);
				if (!resultExec.equals("OK") || !outputList.exists()) {
					// Exception oder Report existiert nicht
					count = 998;
					if (valDateiTempWorkDir.exists()) {
						valDateiTempWorkDir.delete();
					}
					return count;
				} else {
					// Report existiert -> Auswerten...

					// valDateiMit2Signaturen.pdf
					// 15:09:12.042 [main] INFO de.intarsys.tools.yalf.api -
					// Yalf implementation is class
					// de.intarsys.tools.yalf.logback.LogbackProvider
					// Signature1
					// SignatureAttributeName_20230308T081454

					// valDateiOhneSignatur.pdf
					// 15:14:14.741 [main] INFO de.intarsys.tools.yalf.api -
					// Yalf implementation is class
					// de.intarsys.tools.yalf.logback.LogbackProvider

					String valDateiTempName = valDateiTemp.getName();
					// String mainInfo = "[main] INFO de.intarsys.tools.yalf.api";
					String mainExeption = "[main] ERROR d.intarsys.egov.validationclient.cli - Unexpected exception";
					String mainExeption1 = "ERROR";
					String mainExeption2 = "Unexpected exception";
					Boolean valDateiNameBoo = false;
					Boolean mainInfoBoo = false;
					int counterSig = 0;
					Scanner scannerFormat = new Scanner(outputList);
					while (scannerFormat.hasNextLine()) {
						// format_name=mov,mp4,m4a,3gp,3g2,mj2
						String line = scannerFormat.nextLine();
						// System.out.println( "egovdv: " + line );
						if (line.equals(valDateiTempName)) {
							// erste Linie vorhanden
							valDateiNameBoo = true;
							/*
							 * } else if (line.contains(mainInfo)) { // zweite Linie vorhanden mainInfoBoo =
							 * true;
							 *
							 * in der Version 2.0.0 nicht mehr vorhanden
							 */
						} else if (line.contains(mainExeption) || line.contains(mainExeption1)
								|| line.contains(mainExeption2)) {
							// Unexpected exception vorhanden
							mainInfoBoo = false;
						} else {
							// andere Linie
							// if (valDateiNameBoo && mainInfoBoo) {
							if (valDateiNameBoo) {
								if (line.contains("   ")) {
									counterSig = counterSig + 1;
									// Signame ausgeben
									// System.out.println( "egovdv Signame: " + line );
								}
							}
						}
					}
					count = counterSig;

					scannerFormat.close();

					if (!valDateiNameBoo && !mainInfoBoo) {
						// die ersten Zeilen fehlen
						count = 997;
						if (valDateiTempWorkDir.exists()) {
							valDateiTempWorkDir.delete();
						}
						return count;
					}
				}
			} catch (Exception e) {
				count = 996;
				if (valDateiTempWorkDir.exists()) {
					valDateiTempWorkDir.delete();
				}
				return count;
			}
		}

		if (outputList.exists()) {
			outputList.delete();
		}
		if (valDateiTempWorkDir.exists()) {
			valDateiTempWorkDir.delete();
		}

		// Ende Ermittlung ob Signaturen enthalten sind
		// System.out.println( "Anzahl Signaturen= " +count );
		return count;
	}

	/**
	 * TODO: Validiert mit egovdv via cmd die Signaturen in pdf und speichert das
	 * Ergebnis in ein File (Output). Dazu wird der Mandant Mixed verwendet. Gibt
	 * zurueck ob Output existiert oder nicht
	 * 
	 * Fuer diesen Schritt braucht es jetzt Internet/URL sowie einen account
	 * 
	 * validate -list -f <filename> -l de -u no
	 * 
	 * validate <account> -u https://egovsigval-backend.bit.admin.ch -m Mixed -f
	 * <filename> -c -e -d -o <report>
	 * 
	 * @param -c   Container check, validates all signatures in the pdf file.
	 * @param -d   Logs the JSON object of the request and response.
	 * @param -e   Generate report even for unsigned files
	 * @param -f   file to validate
	 * @param -l   get pdf report in the given language, supported codes: de, fr,
	 *             it, en. This is an optional parameter, if omitted de is used.
	 * @param -m   mandator to use
	 * @param -o   pdf report will be saved at the given name
	 * @param -suo "Test Archiv" Optional parameter for examiner organization
	 * 
	 * @param -u   URL of the validation webservice.
	 *
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execEgovdvCheck(File fileToCheck, File output, File xmlFile, File workDir, String dirOfJarPath,
			String mandant, Locale locale) throws InterruptedException {
		boolean out = true;
		File fvalidateBat = new File(dirOfJarPath + File.separator + validateBat);
		File fexeDir = new File(exeDir);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (output.exists()) {
			output.delete();
		}

		// egovdv cli hat Probleme wenn doppelleerschlag im Pfad und Namen
		String pdfNameNormalisiert = fileToCheck.getName().replace("  ", " .");
		File pdfFileNormalisiert = new File(workDir + File.separator + pdfNameNormalisiert);
		try {
			Util.copyFile(fileToCheck, pdfFileNormalisiert);
		} catch (IOException e) {
			// Normalisierung fehlgeschlagen es wird ohne versucht
			pdfFileNormalisiert = fileToCheck;
		}
		if (!pdfFileNormalisiert.exists()) {
			pdfFileNormalisiert = fileToCheck;
		}

		String pathToKostValDir = System.getenv("USERPROFILE") + File.separator + ".kost-val_2x";
		File directoryOfConfigfile = new File(pathToKostValDir + File.separator + "configuration");
		File configFile = new File(directoryOfConfigfile + File.separator + "kostval.conf.xml");

		Document doc = null;
		String institut = "Institut";

		try {
			BufferedInputStream bis;
			bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();

			institut = doc.getElementsByTagName("Institut").item(0).getTextContent();
			bis.close();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			System.out.println("Fehler beim auslesen der config (egovdv)");
		}

		String account = egovdvIntern.egovdvInternas(institut, directoryOfConfigfile.getAbsolutePath());
		// System.out.println( "" );
		// System.out.println( "account: " + account );

		String resultSummary = "_";

		boolean connectivity;
		URL url;
		try {
			url = new URL("https://egovsigval-backend.bit.admin.ch");
			URLConnection conn = url.openConnection();
			conn.connect();
			connectivity = true;
		} catch (Exception e) {
			connectivity = false;
		}
		// System.out.println("https://egovsigval-backend.bit.admin.ch ->
		// "+connectivity);

		if (account.equals("noLicense")) {
			resultSummary = "noLicense";
		} else if (!connectivity) {
			resultSummary = "noConnectivity";
		} else {
			String optionLanguage = "de";
			if (locale.toString().contains("fr")) {
				optionLanguage = "fr";
			} else if (locale.toString().contains("it")) {
				optionLanguage = "it";
			} else if (locale.toString().contains("en")) {
				optionLanguage = "en";
			}
			// -l get pdf report in the given language, supported codes: de, fr, it, en.

			String command = "\"\"cd " + fexeDir.getAbsolutePath() + "\" & \"" + fvalidateBat.getAbsolutePath() + "\" "
					+ account + "-u https://egovsigval-backend.bit.admin.ch -m " + mandant + " -f \""
					+ pdfFileNormalisiert.getAbsolutePath() + "\" -l " + optionLanguage + " -c -e -suo \"" + institut
					+ "\" -o \"" + output.getAbsolutePath() + "\"\"";

			// validate <account> -u https://egovsigval-backend.bit.admin.ch -m
			// Mixed -f <filename> -c -e -d -o <report>

			// System.out.println("command: " + command);

			String resultExec = Cmd.execToStringSplitDv(command, out, workDir);

			// System.out.println( "resultExec: " + resultExec );

			// Normalisierte Datei wieder loeschen
			if (pdfFileNormalisiert.exists()) {
				pdfFileNormalisiert.delete();
			}

			// egovdv gibt zu viele Infos raus. Entsprechend hier eine kleine vorab analyse

			/*
			 * Validity of file report: VALID was the document modified after last
			 * signature?: false mandator requirements not met?: false results for signature
			 * with name: Name of check: CERTIFICATE status: VALID Name of check: INTEGRITY
			 * status: VALID Name of check: MANDATOR status: VALID Name of check: REVOCATION
			 * status: VALID Name of check: TIMESTAMP status: VALID
			 */

			if (resultExec.contains("Validity of file report: VALID")) {
				resultSummary = resultSummary + "Validity-VALID_";
			}
			if (resultExec.contains("Validity of file report: INVALID")) {
				resultSummary = resultSummary + "Validity-INVALID_";
			}
			if (resultExec.contains("was the document modified after last signature?: false")) {
				resultSummary = resultSummary + "Modified-NO_";
			}
			if (resultExec.contains("was the document modified after last signature?: true")) {
				resultSummary = resultSummary + "Modified-YES_";
			}
			if (resultExec.contains("mandator requirements not met?: false")) {
				resultSummary = resultSummary + "MIXED-YES_";
			}
			if (resultExec.contains("mandator requirements not met?: true")) {
				resultSummary = resultSummary + "MIXED-NO_";
			}
			if (resultExec.contains("CERTIFICATE status: VALID")) {
				resultSummary = resultSummary + "CERTIFICATE-VALID_";
			}
			if (resultExec.contains("CERTIFICATE status: INVALID")) {
				resultSummary = resultSummary + "CERTIFICATE-INVALID_";
			}
			if (resultExec.contains("INTEGRITY status: VALID")) {
				resultSummary = resultSummary + "INTEGRITY-VALID_";
			}
			if (resultExec.contains("INTEGRITY status: INVALID")) {
				resultSummary = resultSummary + "INTEGRITY-INVALID_";
			}
			if (resultExec.contains("MANDATOR status: VALID")) {
				resultSummary = resultSummary + "MANDATOR-VALID_";
			}
			if (resultExec.contains("MANDATOR status: INVALID")) {
				resultSummary = resultSummary + "MANDATOR-INVALID_";
			}
			if (resultExec.contains("REVOCATION status: VALID")) {
				resultSummary = resultSummary + "REVOCATION-VALID_";
			}
			if (resultExec.contains("REVOCATION status: INVALID")) {
				resultSummary = resultSummary + "REVOCATION-INVALID_";
			}
			if (resultExec.contains("TIMESTAMP status: VALID")) {
				resultSummary = resultSummary + "TIMESTAMP-VALID_";
			}
			if (resultExec.contains("TIMESTAMP status: INVALID")) {
				resultSummary = resultSummary + "TIMESTAMP-INVALID_";
			}

			if (!output.exists()) {
				// Datei nicht angelegt...
				resultSummary = resultSummary + "NoReport_";
			}
		}
		// System.out.println( "resultSummary= " + resultSummary );
		return resultSummary;
	}

	/**
	 * TODO: List den PDF-Report aus und gibt das Ergebnis aus.
	 * 
	 * Prüfbericht für elektronische Signaturen
	 * 
	 * @param File output, welcher analysiert wird
	 * @return String mit PDF-ergebnis
	 */
	public static String analyseEgovdvPdf(File valDatei, File output, Map<String, String> configMap, File txtFile,
			File xmlFile, Locale locale) {
		String lineOut = "LineNotFound";
		@SuppressWarnings("unused")
		String lineOutXml = "LineNotFound";
		String sigDoku = configMap.get("sigDoku");

		try {
			// Auslesen mit pdfbox
			// lineOut = pdfbox.getTextPdfbox(output);
			lineOut = pdfbox.getTxtPdfbox(output, txtFile);
			// System.out.println( "lineOut=" + lineOut );
			if (sigDoku.equals("yes")) {
				lineOutXml = prettyEgovdvPdfXml(valDatei, lineOut, configMap, txtFile, xmlFile, locale);
			}
			lineOut = prettyEgovdvPdf(lineOut);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Fehler beim auslesen des egovdv-Reports (analyseEgovdvPdf)");
		}
		// System.out.println("lineOutXml=" + lineOutXml);
		return lineOut;
	}

	/**
	 * TODO: Bereinigung des Ergebnisses aus dem PDF-Report
	 * 
	 * @param String line, welcher bereinigt wird
	 * @return String bereinigter String
	 */
	public static String prettyEgovdvPdf(String line) {
		String lineOut = "LineNotFound";
		// System.out.println("1 " + line);

		String newLine = "</Message><Message></Message><Message>" + line;
		// System.out.println( "newLine=" + newLine );
		String prettyPrint = newLine.replaceAll(":__", ": ");
		prettyPrint = prettyPrint.replaceAll("__ __", "</Message><Message> - ");

		/*
		 * 1. Abschnitt
		 * 
		 * Datum/Zeit der Prüfung: 09.04.2025 09:13:29 UTC Angaben der prüfenden Person:
		 * Staatsarchiv Basel-Stadt Die Eingabe dieser Informationen ist nicht Teil des
		 * Validierungsprozesses und unterliegt weder einer Überprüfung noch einer
		 * Bestätigung. Name der signierten Datei: AS-2018-89-DE.pdf Hash der Datei
		 * (SHA256): 91a62f1409693f77c6ed60eb12ab58e1 869e99f053c1155bc7f6e3c71922e1b4
		 * Der Validator prüft, ob die in einem Dokument enthaltenen Signaturen den für
		 * die Prüfung auszuwählenden Kriterien entsprechen. Die Kriterien können sich
		 * auf die Gültigkeit des Dokuments als Ganzes (z. B. gültiger
		 * Strafregisterauszug) oder auf die Gültigkeit aller darin enthaltenen
		 * Unterschriften beziehen (z.B . qualifiziert signiertes Dokument).
		 * 
		 */
		prettyPrint = prettyPrint.replaceAll("__Datum/Zeit der Prüfung:",
				"</Message><Message> - Datum/Zeit der Prüfung:");
		prettyPrint = prettyPrint.replaceAll("__Angaben der prüfenden", "</Message><Message> - Angaben der prüfenden");
		prettyPrint = prettyPrint.replaceAll("__ Die Eingabe dieser Informationen ist nicht Teil des ", "");
		prettyPrint = prettyPrint.replaceAll("__Validierungsprozesses und unterliegt weder einer Überprüfung noch", "");
		prettyPrint = prettyPrint.replaceAll("__einer Bestätigung.", "");
		prettyPrint = prettyPrint.replaceAll("__Name der signierten __Datei:",
				"</Message><Message> - Name der signierten __Datei:");
		prettyPrint = prettyPrint.replaceAll("__Name der signierten", "</Message><Message> - Name der signierten");
		prettyPrint = prettyPrint.replaceAll("__Datei:", "Datei:");
		prettyPrint = prettyPrint.replaceAll("__Hash der Datei ", "</Message><Message> - Hash der Datei ");
		prettyPrint = prettyPrint.replaceAll(
				"__Der Validator prüft, ob die in einem Dokument enthaltenen Signaturen den für die Prüfung ", "");
		prettyPrint = prettyPrint.replaceAll(
				"__auszuwählenden Kriterien entsprechen. Die Kriterien können sich auf die Gültigkeit des Dokuments ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__als Ganzes \\(z. B. gültiger Strafregisterauszug\\) oder auf die Gültigkeit aller darin enthaltenen",
				"");
		prettyPrint = prettyPrint.replaceAll("__Unterschriften beziehen \\(z.B . qualifiziert signiertes Dokument\\).",
				"");

		/*
		 * 2. Abschnitt
		 * 
		 * Zusammenfassung der Dokumentprüfung Das Dokument ist gültig signiert. Das
		 * geprüfte Dokument trägt mehrere elektronische Signaturen mit
		 * unterschiedlichen Zertifikatsklassen, gemäss ZertES.
		 * 
		 * Das Dokument ist teilweise nicht gültig signiert. Das Dokument weist mehrere
		 * elektronische Signaturen mit unterschiedlichen Zertifikatsklassen auf.
		 * Mindestens eine der elektronischen Signaturen auf dem validierten Dokument
		 * konnte keiner Dokumentenart (Mandant) zugeordnet werden. Die Prüfergebnisse
		 * der einzelnen Signaturen sind im Detailbericht ersichtlich
		 */
		// invalide Fehlermeldungen
		prettyPrint = prettyPrint.replaceAll("__Zusammenfassung der Dokumentprüfung", "");
		prettyPrint = prettyPrint.replaceAll("__Das Dokument weist mehrere elektronische Signaturen mit",
				"</Message><Message>Das Dokument weist mehrere elektronische Signaturen mit");
		prettyPrint = prettyPrint.replaceAll("__unterschiedlichen Zertifikatsklassen auf. Mindestens eine der ",
				"unterschiedlichen Zertifikatsklassen auf. Mindestens eine der ");
		prettyPrint = prettyPrint.replaceAll("__elektronischen Signaturen auf dem validierten Dokument",
				"elektronischen Signaturen auf dem validierten Dokument");
		prettyPrint = prettyPrint.replaceAll("__konnte keiner Dokumentenart \\(Mandant\\) zugeordnet werden. ",
				"konnte keiner Dokumentenart (Mandant) zugeordnet werden. ");
		prettyPrint = prettyPrint.replaceAll("__Die Prüfergebnisse der einzelnen Signaturen sind im", "");
		prettyPrint = prettyPrint.replaceAll("__Detailbericht ersichtlich.", "");

		/*
		 * 3. Abschnitt
		 * 
		 * Folgende Prüfungen wurden durchgeführt: Anzahl Signaturen im Dokument: 1 Das
		 * Dokument ist nach der letzten Signatur nicht mehr verändert worden. Alle
		 * validierten Signaturen des Dokumentes sind gültig gemäss ZertES. Alle zur
		 * Signatur verwendeten Zertifikate sind nicht revoziert, also gültig. Alle in
		 * diesem Dokument angebrachten
		 */
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 0", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 1", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 2", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 3", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 4", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 5", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 6", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 7", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 8", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 9", "");

		prettyPrint = prettyPrint.replaceAll("__Folgende Prüfungen wurden durchgeführt:", "");
		prettyPrint = prettyPrint.replaceAll("__Das Dokument ist ",
				"</Message><Message></Message><Message>Pruefergebnis [egovdv]:</Message><Message>Das Dokument ist ");
		prettyPrint = prettyPrint.replaceAll("__ Das Dokument ist ", "</Message><Message> - Das Dokument ist ");
		prettyPrint = prettyPrint.replaceAll("__ Das Dokument weist ", "</Message><Message> - Das Dokument weist ");
		prettyPrint = prettyPrint.replaceAll("__ Alle validierten ", "</Message><Message> - Alle validierten ");
		prettyPrint = prettyPrint.replaceAll("__ Alle zur Signatur", "</Message><Message> - Alle zur Signatur");
		/*
		 * if (prettyPrint.contains("Diese Signatur ist nicht LTV-fähig")) { prettyPrint
		 * = prettyPrint.replaceAll("</Message><Message> - Das Dokument ist ",
		 * "</Message><Message> - Nicht alle Signaturen sind LTV-fähig.</Message><Message> - Das Dokument ist "
		 * ); } else { prettyPrint =
		 * prettyPrint.replaceAll("</Message><Message> - Das Dokument ist ",
		 * "</Message><Message> - Alle Signaturen sind LTV-fähig.</Message><Message> - Das Dokument ist "
		 * ); }
		 */
		prettyPrint = prettyPrint.replaceAll("__ Alle in diesem Dokument",
				"</Message><Message> - Alle in diesem Dokument");
		if (prettyPrint.contains("Diese Signatur ist nicht LTV-fähig")) {
			prettyPrint = prettyPrint.replaceAll("</Message><Message> - Alle in diesem Dokument",
					"</Message><Message> - Nicht alle Signaturen sind LTV-fähig.</Message><Message> - Alle in diesem Dokument");
		} else if (prettyPrint.contains("Diese Signatur ist LTV-fähig")) {
			prettyPrint = prettyPrint.replaceAll("</Message><Message> - Alle in diesem Dokument",
					"</Message><Message> - Alle Signaturen sind LTV-fähig.</Message><Message> - Alle in diesem Dokument");
		}

		/*
		 * 4. Abschnitt
		 * 
		 */
		// Bereinigung Prüfdetails
		prettyPrint = prettyPrint.replaceAll("__Prüfdetails Signatur",
				"</Message><Message></Message><Message>Prüfdetails Signatur [egovdv]");
		prettyPrint = prettyPrint.replaceAll("__Informationen zur Signatur", "");
		prettyPrint = prettyPrint.replaceAll("__Zeitpunkt der ", "</Message><Message> - Zeitpunkt der ");
		prettyPrint = prettyPrint.replaceAll("__Signaturalgorithmus:", "</Message><Message> - Signaturalgorithmus:");
		prettyPrint = prettyPrint.replace("Grund: ", "</Message><Message> - Grund: ");
		prettyPrint = prettyPrint.replaceAll("__Die digitale Signatur ist",
				"</Message><Message> - Die digitale Signatur ist");
		prettyPrint = prettyPrint.replaceAll("__Information über den Zeitstempel",
				"</Message><Message> - Information über den Zeitstempel");
		prettyPrint = prettyPrint.replaceAll("__Information über das Unterzeichnerzertifikat",
				"</Message><Message> - Information über das Unterzeichnerzertifikat");
		prettyPrint = prettyPrint.replaceAll("__Zertifikat ausgestellt für",
				"</Message><Message> - - Zertifikat ausgestellt für");
		prettyPrint = prettyPrint.replaceAll("__Organisation: ", "</Message><Message> - - Organisation: ");
		prettyPrint = prettyPrint.replaceAll("__Organisationseinheit", "</Message><Message> - - Organisationseinheit");
		prettyPrint = prettyPrint.replaceAll("__Zertifikat ausgestellt",
				"</Message><Message> - - Zertifikat ausgestellt");
		prettyPrint = prettyPrint.replaceAll("__Gültigkeit des", "</Message><Message> - - Gültigkeit des");
		prettyPrint = prettyPrint.replaceAll("__Der Zeitstempel ist gültig",
				"</Message><Message> - - Der Zeitstempel ist gültig");
		prettyPrint = prettyPrint.replaceAll("__Revokationsstatus:", "</Message><Message> - - Revokationsstatus:");
		prettyPrint = prettyPrint.replaceAll("__Zertifikatsträger:", "</Message><Message> - - Zertifikatsträger:");
		prettyPrint = prettyPrint.replaceAll("__Zertifikatsklasse:", "</Message><Message> - - Zertifikatsklasse:");
		prettyPrint = prettyPrint.replaceAll("__Diese Signatur ist", "</Message><Message> - Diese Signatur ist");
		prettyPrint = prettyPrint.replaceAll("__Informationen über die unterzeichnende staatliche Einrichtung",
				"</Message><Message> - Informationen über die unterzeichnende staatliche Einrichtung: ");
		prettyPrint = prettyPrint.replaceAll("__Bezeichnung der", "</Message><Message> - Bezeichnung der");

		prettyPrint = prettyPrint
				.replaceAll("__Prüfung: Die Zertifikate entsprechen unterschiedlichen Zertifikatsklassen", "");
		prettyPrint = prettyPrint.replaceAll("__gemäss ZertES.", "");

		// Entferne alle Zeichen nach "__Prozessbezogene Prüfung"
		prettyPrint = prettyPrint.replaceAll("__Prozessbezogene Prüfung", "");
		prettyPrint = prettyPrint.replaceAll("__Validator: Mehrere elektronische Signaturen mit unterschiedlichen", "");
		prettyPrint = prettyPrint.replaceAll("__Zertifikatsklassen gemäss ZertES.", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Speicherung, Ausdruck oder Übermittlung durch elektronische Medien. Das Ergebnis einer", "");
		prettyPrint = prettyPrint.replaceAll("__Gültigkeit einer Signatur", "");
		prettyPrint = prettyPrint.replaceAll("__\\(A\\) Eine gültige Signatur besitzt folgende Eigenschaften:", "");
		prettyPrint = prettyPrint.replaceAll("__Alle Zertifikate in der Signatur wurden mathematisch geprüft.", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Es ist sichergestellt, dass der Unterzeichner den Schlüssel seines Zertifikats für die Signatur ",
				"");
		prettyPrint = prettyPrint.replaceAll("__verwendete.", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Der Zertifikatspfad jedes Zertifikats wurde geprüft. Dadurch wird die Echtheit des Zertifikats ",
				"");
		prettyPrint = prettyPrint
				.replaceAll("__des Unterzeichners durch unabhängige, vertrauenswürdige Zertifikate bestätigt.", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Das Zertifikat des Unterzeichners sowie alle übergeordneten Zertifikate des Ausstellers ", "");
		prettyPrint = prettyPrint.replaceAll("__waren zum Zeitpunkt der Signatur gültig.", "");
		prettyPrint = prettyPrint.replaceAll("__Wichtige rechtliche Hinweise zur Prüfung", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Diese Signaturprüfung wurde zum oben angegebenen Datum und Uhrzeit durchgeführt und ", "");
		prettyPrint = prettyPrint.replaceAll(
				"__bestätigt die Richtigkeit der Angaben zum jeweiligen Zeitpunkt. Der Betreiber dieses Dienstes ", "");
		prettyPrint = prettyPrint.replaceAll(
				"__übernimmt keine Gewähr für die Angaben Dritter sowie die Unveränderlichkeit dieses Berichts nach ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__Speicherung, Ausdruck oder ￜbermittlung durch elektronische Medien. Das Ergebnis einer ", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Verifikation einer Signatur beruht ausschliesslich auf der Auskunft des jeweiligen Ausstellers des ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__Zertifikats, welches der Ersteller zur Erstellung der elektronischen Signatur verwendet hat. Es wird ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__darauf hingewiesen, dass die Verifikation von Signaturen von der Verfügbarkeit und technischen ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__Kompatibilität von Auskunftsdiensten des jeweiligen Ausstellers des Zertifikats abhängt, welches ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__zur Erstellung der Signatur verwendet wurde. Um eine eindeutige und überprüfbare Zeitangabe zu ",
				"");
		prettyPrint = prettyPrint.replaceAll(
				"__ermöglichen, entsprechen alle in diesem Verifikationsbericht angezeigten Zeitangaben der UTC ", "");
		prettyPrint = prettyPrint.replaceAll(
				"__Zeitzone. Diese Zeitangabe kann von der jeweiligen gesetzlich gültigen Lokalzeit abweichen.", "");
		prettyPrint = prettyPrint.replaceAll(":", ": ");
		prettyPrint = prettyPrint.replaceAll(":  ", ": ");

		prettyPrint = prettyPrint.replaceAll("__Das geprüfte Dokument trägt mehrere elektronische ", "");
		prettyPrint = prettyPrint.replaceAll("__Signaturen mit unterschiedlichen Zertifikatsklassen, gemäss ", "");
		prettyPrint = prettyPrint.replaceAll("__ZertES.", "");

		prettyPrint = prettyPrint.replaceAll(" \\(Details siehe A\\)", "");
		// System.out.println( "2 " + prettyPrint );

		lineOut = prettyPrint;

		return lineOut;
	}

	/**
	 * TODO: Bereinigung des Ergebnisses aus dem PDF-Report
	 * 
	 * @param String line, welcher bereinigt wird
	 * @return String bereinigter XML-String
	 */
	public static String prettyEgovdvPdfXml(File valDatei, String line, Map<String, String> configMap, File txtFile,
			File xmlFile, Locale locale) {

		try {
			Util.oldnewstringAll("ü", "ue", txtFile);
			Util.oldnewstringAll("ö", "oe", txtFile);
			Util.oldnewstringAll("ä", "ae", txtFile);
			Util.oldnewstringAll("Ü", "Ue", txtFile);
			Util.oldnewstringAll("Ö", "Oe", txtFile);
			Util.oldnewstringAll("Ä", "Ae", txtFile);
			Util.oldnewstringAll("\n", "n3wL1n3", txtFile);

			Util.oldnewstringAll(":n3wL1n3", ": ", txtFile);
			Util.oldnewstringAll("n3wL1n3 ", "n3wL1n3", txtFile);
			Util.oldnewstringAll("n3wL1n3n3wL1n3", "n3wL1n3", txtFile);

// header (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Datum/Zeit der Pruefung", "\nDatum/Zeit der Pruefung", txtFile);
			Util.oldnewstringAll("n3wL1n3Angaben der pruefenden", "\nAngaben der pruefenden", txtFile);
			Util.oldnewstringAll("n3wL1n3Die Eingabe dieser Informationen ist nicht Teil des",
					"\nDie Eingabe dieser Informationen ist nicht Teil des", txtFile);
			Util.oldnewstringAll("n3wL1n3Name der signierten", "\nName der signierten", txtFile);
			Util.oldnewstringAll("n3wL1n3Hash der Datei", "\nHash der Datei", txtFile);
			Util.oldnewstringAll("n3wL1n3Der Validator prueft, ob die in einem Dokument",
					"\nDer Validator prueft, ob die in einem Dokument", txtFile);

// summary (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Zusammenfassung der Dokumentpruefung",
					"\nZusammenfassung der Dokumentpruefung", txtFile);
			Util.oldnewstringAll("n3wL1n3Das Dokument ist gueltig signiert", "\nDas Dokument ist gueltig signiert",
					txtFile);
			Util.oldnewstringAll("n3wL1n3Das Dokument ist teilweise nicht gueltig signiert",
					"\nDas Dokument ist teilweise nicht gueltig signiert", txtFile);
			Util.oldnewstringAll("n3wL1n3Das Dokument weist mehrere elektronische Signaturen",
					"\nDas Dokument weist mehrere elektronische Signaturen", txtFile);
			Util.oldnewstringAll("n3wL1n3Das gepruefte Dokument traegt mehrere elektronische",
					"\nDas gepruefte Dokument traegt mehrere elektronische", txtFile);
			// TODO: mit weiteren Varianten erweitern

// checks  (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Folgende Pruefungen wurden durchgefuehrt",
					"\nFolgende Pruefungen wurden durchgefuehrt", txtFile);
			Util.oldnewstringAll("n3wL1n3Das Dokument ist nach der letzten Signatur noch veraendert worden",
					"\nDas Dokument ist nach der letzten Signatur noch veraendert worden", txtFile);
			Util.oldnewstringAll("n3wL1n3Das Dokument ist nach der letzten Signatur nicht mehr veraendert worden",
					"\nDas Dokument ist nach der letzten Signatur nicht mehr veraendert worden", txtFile);
			Util.oldnewstringAll("n3wL1n3Alle validierten Signaturen des Dokumentes sind gueltig gemaess ZertES",
					"\nAlle validierten Signaturen des Dokumentes sind gueltig gemaess ZertES", txtFile);
			Util.oldnewstringAll("n3wL1n3Mindestens eine der validierten Signaturen des Dokumentes ist ungueltig",
					"\nMindestens eine der validierten Signaturen des Dokumentes ist ungueltig", txtFile);
			Util.oldnewstringAll("n3wL1n3Alle zur Signatur verwendeten Zertifikate sind nicht revoziert, also gueltig",
					"\nAlle zur Signatur verwendeten Zertifikate sind nicht revoziert, also gueltig", txtFile);
			Util.oldnewstringAll("n3wL1n3Mindestens eines der zur Signatur verwendeten Zertifikate ist revoziert",
					"\nMindestens eines der zur Signatur verwendeten Zertifikate ist revoziert", txtFile);
			Util.oldnewstringAll("n3wL1n3Alle in diesem Dokument angebrachten Zeitstempel sind gueltig gemaess ZertES",
					"\nAlle in diesem Dokument angebrachten Zeitstempel sind gueltig gemaess ZertES", txtFile);
			Util.oldnewstringAll(
					"n3wL1n3Mindestens einer der in diesem Dokument angebrachten Zeitstempel ist ungueltig",
					"\nMindestens einer der in diesem Dokument angebrachten Zeitstempel ist ungueltig", txtFile);

// signatures (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("Anzahl Signaturen im Dokument", "\nAnzahl Signaturen im Dokument", txtFile);
			Util.oldnewstringAll("n3wL1n3Pruefdetails Signatur", "\nPruefdetails Signatur", txtFile);

// sigInfos (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Informationen zur Signatur", "\nInformationen zur Signatur", txtFile);
			Util.oldnewstringAll("n3wL1n3Zeitpunkt der", "\nZeitpunkt der", txtFile);
			Util.oldnewstringAll("n3wL1n3Signaturalgorithmus:", "\nSignaturalgorithmus:", txtFile);
			Util.oldnewstringAll("n3wL1n3Grund:", "\nGrund:", txtFile);
			Util.oldnewstringAll("n3wL1n3Die digitale Signatur ist", "\nDie digitale Signatur ist", txtFile);

// timeInfos (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Information ueber den Zeitstempel", "\nInformation ueber den Zeitstempel",
					txtFile);
			Util.oldnewstringAll("n3wL1n3Zertifikat ausgestellt fuer", "\nZertifikat ausgestellt fuer", txtFile);
			Util.oldnewstringAll("n3wL1n3Zertifikat ausgestellt", "\nZertifikat ausgestellt", txtFile);
			Util.oldnewstringAll("n3wL1n3Gueltigkeit des", "\nGueltigkeit des", txtFile);
			Util.oldnewstringAll("n3wL1n3Der Zeitstempel ist gueltig", "\nDer Zeitstempel ist gueltig", txtFile);
			Util.oldnewstringAll("n3wL1n3Der hier angebrachte Zeitstempel erfuellt die Anforderungen",
					"\nDer hier angebrachte Zeitstempel erfuellt die Anforderungen", txtFile);

// certInfos (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Information ueber das Unterzeichnerzertifikat",
					"\nInformation ueber das Unterzeichnerzertifikat", txtFile);
			Util.oldnewstringAll("n3wL1n3Organisation:", "\nOrganisation:", txtFile);
			Util.oldnewstringAll("n3wL1n3Organisationseinheit:", "\nOrganisationseinheit:", txtFile);
			Util.oldnewstringAll("n3wL1n3Revokationsstatus:", "\nRevokationsstatus:", txtFile);
			Util.oldnewstringAll("n3wL1n3Zertifikatstraeger:", "\nZertifikatstraeger:", txtFile);
			Util.oldnewstringAll("n3wL1n3Zertifikatsklasse:", "\nZertifikatsklasse:", txtFile);
			Util.oldnewstringAll("n3wL1n3Diese Signatur ist nicht LTV-faehig", "\nDiese Signatur ist nicht LTV-faehig",
					txtFile);
			Util.oldnewstringAll("n3wL1n3Diese Signatur ist LTV-faehig", "\nDiese Signatur ist LTV-faehig", txtFile);

// procInfos (n3wL1n3 durch \n ersetzten wo gewuenscht)		
			Util.oldnewstringAll("n3wL1n3Prozessbezogene Pruefung", "\nProzessbezogene Pruefung", txtFile);
			Util.oldnewstringAll("unterschiedlichen Zertifikatsklassen n3wL1n3gemaess ZertES.",
					"unterschiedlichen Zertifikatsklassen gemaess ZertES.\n", txtFile);

// swInfos (n3wL1n3 durch \n ersetzten wo gewuenscht)
			Util.oldnewstringAll("n3wL1n3Gueltigkeit einer Signatur", "\nGueltigkeit einer Signatur", txtFile);
			Util.oldnewstringAll("n3wL1n3waren zum Zeitpunkt der Signatur gueltig.n3wL1n3",
					"waren zum Zeitpunkt der Signatur gueltig.\n n3wL1n3", txtFile);
			Util.oldnewstringAll("n3wL1n3Wichtige rechtliche Hinweise zur Pruefungn3wL1n3",
					"\nWichtige rechtliche Hinweise zur Pruefung n3wL1n3", txtFile);
			Util.oldnewstringAll("gesetzlich gueltigen Lokalzeit abweichen.n3wL1n3",
					"gesetzlich gueltigen Lokalzeit abweichen.\n\n", txtFile);

			// Util.oldnewstringAll( "n3wL1n3", "\n",txtFile);

// allg. Bereinigung
			Util.oldnewstringAll("n3wL1n3n3wL1n3", "n3wL1n3", txtFile);
			Util.oldnewstringAll("n3wL1n3", " ", txtFile);
			Util.oldnewstringAll("  ", " ", txtFile);
			Util.oldnewstringAll("  ", " ", txtFile);
			Util.oldnewstringAll(" \n", "\n", txtFile);

			BufferedReader reader = new BufferedReader(new FileReader(txtFile));
			String lineTxtFile = "";
			String lineNorm = "";
			String done = ""; // "_done_";
			while ((lineTxtFile = reader.readLine()) != null) {
				if (lineTxtFile.startsWith("Pruefbericht fuer elektronische")
						&& lineTxtFile.endsWith("elektronische Signaturen")) {
					// Bereits enthalten, loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile
						.startsWith("Die Eingabe dieser Informationen ist nicht Teil des Validierungsprozesses und")
						&& lineTxtFile.endsWith("unterliegt weder einer Ueberpruefung noch einer Bestaetigung.")) {
					// Bereits enthalten, loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Der Validator prueft, ob die in einem Dokument enthaltenen")
						|| lineTxtFile.contains("oder auf die Gueltigkeit aller darin enthaltenen")) {
					// Bereits enthalten, loeschen
					lineNorm = "Der Validator prueft, ob die in einem Dokument enthaltenen Signaturen den fuer die Pruefung auszuwaehlenden Kriterien entsprechen. Die Kriterien koennen sich auf die Gueltigkeit des Dokuments als Ganzes \\(z. B. gueltiger Strafregisterauszug\\) oder auf die Gueltigkeit aller darin enthaltenen Unterschriften beziehen \\(z.B . qualifiziert signiertes Dokument\\).";
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Prozessbezogene Pruefung")
						&& lineTxtFile.endsWith("entsprechen unterschiedlichen Zertifikatsklassen gemaess ZertES.")) {
					// Bereits enthalten, loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Gueltigkeit einer Signatur")
						|| lineTxtFile.endsWith("zum Zeitpunkt der Signatur gueltig.")) {
					// Bereits enthalten, loeschen
					lineNorm = "Gueltigkeit einer Signatur \\(A\\) Eine gueltige Signatur besitzt folgende Eigenschaften: Alle Zertifikate in der Signatur wurden mathematisch geprueft. Es ist sichergestellt, dass der Unterzeichner den Schluessel seines Zertifikats fuer die Signatur verwendete. Der Zertifikatspfad jedes Zertifikats wurde geprueft. Dadurch wird die Echtheit des Zertifikats des Unterzeichners durch unabhaengige, vertrauenswuerdige Zertifikate bestaetigt. Das Zertifikat des Unterzeichners sowie alle uebergeordneten Zertifikate des Ausstellers waren zum Zeitpunkt der Signatur gueltig.";
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Wichtige rechtliche Hinweise zur Pruefung")
						&& lineTxtFile.endsWith("jeweiligen gesetzlich gueltigen Lokalzeit abweichen.")) {
					// Bereits enthalten, loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);

// header uebertragen 			TODO: mit weiteren Varianten erweitern
				} else if (lineTxtFile.startsWith("Datum/Zeit der Pruefung: ")) {
					lineNorm = lineTxtFile;
					lineTxtFile = lineTxtFile.replace("Datum/Zeit der Pruefung: ", "");
					Util.oldnewstringAll("></hDate>", ">" + lineTxtFile + "</hDate>", xmlFile);
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Angaben der pruefenden Person: ")) {
					lineNorm = lineTxtFile;
					lineTxtFile = lineTxtFile.replace("Angaben der pruefenden Person: ", "");
					Util.oldnewstringAll("></hName>", ">" + lineTxtFile + "</hName>", xmlFile);
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Name der signierten Datei: ")) {
					lineNorm = lineTxtFile;
					lineTxtFile = lineTxtFile.replace("Name der signierten Datei: ", "");
					Util.oldnewstringAll("></hFilename>", ">" + lineTxtFile + "</hFilename>", xmlFile);
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Hash der Datei (SHA- 256): ")) {
					lineNorm = "Hash der Datei \\(SHA- 256\\): ";
					lineTxtFile = lineTxtFile.replace("Hash der Datei (SHA- 256): ", "");
					lineTxtFile = lineTxtFile.replace("Hash der Datei \\(SHA- 256\\): ", "");
					Util.oldnewstringAll("></hFilehash>", ">" + lineTxtFile + "</hFilehash>", xmlFile);
					// mit weiteren Hash erweitern -> falls es diese gibt
					Util.oldnewstringAll(lineNorm, done, txtFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);

// summary uebertragen 			TODO: mit weiteren Varianten erweitern
				} else if (lineTxtFile.startsWith("Zusammenfassung der Dokumentpruefung")) {
					// bereits enthalten loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Das Dokument ist gueltig signiert")) {
					Util.oldnewstringAll("></sMandant>", ">" + lineTxtFile + "</sMandant>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Das Dokument ist teilweise nicht gueltig")) {
					Util.oldnewstringAll("></sMandant>", ">" + lineTxtFile + "</sMandant>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Das Dokument weist mehrere elektronische Signaturen mit")) {
					lineNorm = "Das Dokument weist mehrere elektronische Signaturen mit unterschiedlichen Zertifikatsklassen auf. Mindestens eine der elektronischen Signaturen auf dem validierten Dokument konnte keiner Dokumentenart \\(Mandant\\) zugeordnet werden. Die Pruefergebnisse der einzelnen Signaturen sind im Detailbericht ersichtlich.";
					Util.oldnewstringAll("></sCert>", ">" + lineTxtFile + "</sCert>", xmlFile);
					Util.oldnewstringAll(lineNorm, done, txtFile);
				} else if (lineTxtFile.startsWith("Das gepruefte Dokument traegt mehrere elektronische")) {
					Util.oldnewstringAll("></sCert>", ">" + lineTxtFile + "</sCert>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);

// checks uebertragen 			TODO: mit weiteren Varianten erweitern
				} else if (lineTxtFile.startsWith("Folgende Pruefungen wurden durchgefuehrt:")) {
					// bereits enthalten loeschen
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Das Dokument ist nach der letzten Signatur")) {
					Util.oldnewstringAll("></cChange>", ">" + lineTxtFile + "</cChange>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile
						.startsWith("Alle validierten Signaturen des Dokumentes sind gueltig gemaess ZertES")) {
					Util.oldnewstringAll("></cSigValid>", ">" + lineTxtFile + "</cSigValid>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile
						.startsWith("Mindestens eine der validierten Signaturen des Dokumentes ist ungueltig")) {
					Util.oldnewstringAll("></cSigValid>", ">" + lineTxtFile + "</cSigValid>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile.startsWith("Alle zur Signatur verwendeten Zertifikate sind nicht revoziert")) {
					Util.oldnewstringAll("></cSigRev>", ">" + lineTxtFile + "</cSigRev>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile
						.startsWith("Mindestens eines der zur Signatur verwendeten Zertifikate ist revoziert")) {
					Util.oldnewstringAll("></cSigRev>", ">" + lineTxtFile + "</cSigRev>", xmlFile);
					Util.oldnewstringAll(
							"Mindestens eines der zur Signatur verwendeten Zertifikate ist revoziert, also nicht \\(mehr\\) gueltig.",
							done, txtFile);
				} else if (lineTxtFile.startsWith("Alle in diesem Dokument angebrachten Zeitstempel sind gueltig")) {
					Util.oldnewstringAll("></cTime>", ">" + lineTxtFile + "</cTime>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
				} else if (lineTxtFile
						.startsWith("Mindestens einer der in diesem Dokument angebrachten Zeitstempel ist ungueltig")) {
					Util.oldnewstringAll("></cTime>", ">" + lineTxtFile + "</cTime>", xmlFile);
					Util.oldnewstringAll(lineTxtFile, done, txtFile);
					// TODO: <cSigLtv>Nicht alle Signaturen sind LTV-faehig.</cSigLtv>

// signatures 			TODO: mit weiteren Varianten erweitern
				} else if (lineTxtFile.startsWith("Anzahl Signaturen im Dokument")) {
					if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 0")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 0",
								xmlFile);
						Util.oldnewstringAll("<signature></signature>", "", xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 1")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 1",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 2")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 2",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 3")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 3",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 4")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 4",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 5")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 5",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 6")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 6",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 7")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 7",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 8")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 8",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					} else if (lineTxtFile.contains("Anzahl Signaturen im Dokument: 9")) {
						Util.oldnewstringAll("Anzahl Signaturen im Dokument: 99", "Anzahl Signaturen im Dokument: 9",
								xmlFile);
						Util.oldnewstringAll(lineTxtFile, done, txtFile);
					}
				}
			}
			reader.close();

// sigInfos
			Util.oldnewstringAll("Pruefdetails Signatur", "<signature label=\"Pruefdetails Signatur", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 1", "Pruefdetails Signatur 1\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 2", "Pruefdetails Signatur 2\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 3", "Pruefdetails Signatur 3\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 4", "Pruefdetails Signatur 4\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 5", "Pruefdetails Signatur 5\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 6", "Pruefdetails Signatur 6\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 7", "Pruefdetails Signatur 7\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 8", "Pruefdetails Signatur 8\"", txtFile);
			Util.oldnewstringAll("Pruefdetails Signatur 9", "Pruefdetails Signatur 9\"", txtFile);

// sigInfos
			Util.oldnewstringAll("Informationen zur Signatur", "><sigInfos label=\"Informationen zur Signatur\">",
					txtFile);
			Util.oldnewstringAll("Zeitpunkt der", "<sigDate label=\"Zeitpunkt der", txtFile);
			Util.oldnewstringAll("Unterschrift:", "Unterschrift:\">", txtFile);
			Util.oldnewstringAll("Signaturalgorithmus:", "</sigDate><sigAlgo label=\"Signaturalgorithmus:\">", txtFile);
			if (Util.stringInFile("Grund:", txtFile)) {
				Util.oldnewstringAll("Grund:", "</sigAlgo><sigReason label=\"Grund:\">", txtFile);
				Util.oldnewstringAll("Die digitale Signatur ist gueltig \\(Details siehe A\\)",
						"</sigReason><sigSum>Die digitale Signatur ist gueltig \\(Details siehe A\\)</sigSum>",
						txtFile);
				Util.oldnewstringAll("Die digitale Signatur ist ungueltig \\(Details siehe A\\)",
						"</sigReason><sigSum>Die digitale Signatur ist ungueltig \\(Details siehe A\\)</sigSum>",
						txtFile);
				Util.oldnewstringAll("Die digitale Signatur ist nicht gueltig \\(Details siehe A\\)",
						"</sigReason><sigSum>Die digitale Signatur ist nicht gueltig \\(Details siehe A\\)</sigSum>",
						txtFile);
			} else {
				Util.oldnewstringAll("Die digitale Signatur ist gueltig \\(Details siehe A\\)",
						"</sigAlgo><sigSum>Die digitale Signatur ist gueltig \\(Details siehe A\\)</sigSum>", txtFile);
				Util.oldnewstringAll("Die digitale Signatur ist ungueltig \\(Details siehe A\\)",
						"</sigAlgo><sigSum>Die digitale Signatur ist ungueltig \\(Details siehe A\\)</sigSum>",
						txtFile);
				Util.oldnewstringAll("Die digitale Signatur ist nicht gueltig \\(Details siehe A\\)",
						"</sigAlgo><sigSum>Die digitale Signatur ist nicht gueltig \\(Details siehe A\\)</sigSum>",
						txtFile);
			}
			// TODO

// timeInfos
			Util.oldnewstringAll("Information ueber den Zeitstempel",
					"</sigInfos><timeInfos label=\"Information ueber den Zeitstempel\">", txtFile);
			Util.oldnewstringAll("Zertifikat ausgestellt fuer:", "<info label=\"Zertifikat ausgestellt fuer:\">",
					txtFile);
			Util.oldnewstringAll("Zertifikat ausgestellt von:", "</info><info label=\"Zertifikat ausgestellt von:\">",
					txtFile);
			Util.oldnewstringAll("Gueltigkeit des Zertifikats:", "</info><info label=\"Gueltigkeit des Zertifikats:\">",
					txtFile);
			Util.oldnewstringAll("Der Zeitstempel ist gueltig",
					"</info><info>Der Zeitstempel ist gueltig</info></timeInfos>", txtFile);
			Util.oldnewstringAll(
					"Der hier angebrachte Zeitstempel erfuellt die Anforderungen von Artikel 2 Buchstabe ZertES nicht.",
					"</info><info>Der hier angebrachte Zeitstempel erfuellt die Anforderungen von Artikel 2 Buchstabe ZertES nicht.</info></timeInfos>",
					txtFile);

// certInfos
			Util.oldnewstringAll("Information ueber das Unterzeichnerzertifikat",
					"<certInfos label=\"Information ueber das Unterzeichnerzertifikat\">", txtFile);
			Util.oldnewstringAll("Organisation:", "</info><info label=\"Organisation:\">", txtFile);
			Util.oldnewstringAll("Organisationseinheit:", "</info><info label=\"Organisationseinheit:\">", txtFile);
//			Util.oldnewstringAll("Zertifikat ausgestellt", "</info><info label=\"Zertifikat ausgestellt", txtFile);
			Util.oldnewstringAll("Gueltigkeit des", "</info><info label=\"Gueltigkeit des", txtFile);
			Util.oldnewstringAll("Revokationsstatus:", "</info><info label=\"Revokationsstatus:\">", txtFile);
			Util.oldnewstringAll("Zertifikatstraeger:", "</info><info label=\"Zertifikatstraeger:\">", txtFile);
			Util.oldnewstringAll("Zertifikatsklasse:", "</info><info label=\"Zertifikatsklasse:\">", txtFile);
			Util.oldnewstringAll("Diese Signatur ist nicht LTV-faehig",
					"</info><info>Diese Signatur ist nicht LTV-faehig", txtFile);
			Util.oldnewstringAll("mehr validiert werden.", "mehr validiert werden.</info></certInfos>", txtFile);
			Util.oldnewstringAll("Zertifikates validiert werden.", "Zertifikates validiert werden.</info></certInfos>",
					txtFile);
			// TODO

// procInfos		
			Util.oldnewstringAll("Prozessbezogene Pruefung", "<procInfos label=\"Prozessbezogene Pruefung\">", txtFile);
			Util.oldnewstringAll("Validator:", "<info label=\"Validator:\">", txtFile);
			Util.oldnewstringAll("Zertifikatsklassen gemaess ZertES.", "Zertifikatsklassen gemaess ZertES.</info>",
					txtFile);
			Util.oldnewstringAll("\nPruefung:", "\n<info label=\"Pruefung:\">", txtFile);
			Util.oldnewstringAll("Die Zertifikate entsprechen unterschiedlichen Zertifikatsklassen",
					"Die Zertifikate entsprechen unterschiedlichen Zertifikatsklassen gemaess ZertES.</info></procInfos></signature>",
					txtFile);
			// TODO

// Bereinigung txtFile
			Util.oldnewstringAll("<info label=\"</info><info label=\"", "</info><info label=\"", txtFile);

			Util.oldnewstringAll("\n\r", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("\n", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("\r", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("\n\r", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("\n", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("\r", "n3w2L1n3", txtFile);

			Util.oldnewstringAll("\t", "", txtFile);
			Util.oldnewstringAll("n3w2L1n3 ", "n3w2L1n3", txtFile);
			Util.oldnewstringAll(" n3w2L1n3", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("n3w2L1n3n3w2L1n3n3w2L1n3", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("n3w2L1n3n3w2L1n3", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("n3w2L1n3 ", "n3w2L1n3", txtFile);
			Util.oldnewstringAll(" n3w2L1n3", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("n3w2L1n3n3w2L1n3n3w2L1n3", "n3w2L1n3", txtFile);
			Util.oldnewstringAll("n3w2L1n3n3w2L1n3", "n3w2L1n3", txtFile);

			stringFile = "";
			FileInputStream fis = new FileInputStream(txtFile);
			stringFile = IOUtils.toString(fis, "UTF-8");
			stringFile = stringFile + "</signature>";
			stringFile = stringFile.replaceAll("\n", "n3w2L1n3").replaceAll("\r", "");

			for (int i = 0; i < 10; i++) {
				stringFile = stringFile.replace(" n3w2L1n3", "n3w2L1n3");
				stringFile = stringFile.replace("n3w2L1n3 ", "n3w2L1n3");
				stringFile = stringFile.replace("n3w2L1n3n3w2L1n3n3w2L1n3", "n3w2L1n3");
				stringFile = stringFile.replace("n3w2L1n3n3w2L1n3", "n3w2L1n3");
			}

			stringFile = stringFile.replace("n3w2L1n3<signature ", "n3w2L1n3</signature><signature ");
			stringFile = stringFile.replace("</signature></signature>", "</signature>");
			stringFile = stringFile.replace("</signature>n3w2L1n3</signature>", "</signature>");
			stringFile = stringFile.replace("n3w2L1n3", "\n");

			Util.oldnewstringAll("<signature></signature>", stringFile, xmlFile);

// softwareInfos
			Util.oldnewstringAll("<version>KOST-Val</version>", "<version>" + versionKostVal + "</version>", xmlFile);
			Util.oldnewstringAll("<version>Diskreter Validator</version>", "<version>" + versionEgoDv + "</version>",
					xmlFile);
			Util.oldnewstringAll("<version>veraPDF</version>", "<version>" + versionVerapdf + "</version>", xmlFile);

// Bereinigung xmlFile
			Util.oldnewstringAll("<timeInfos label=\"Information ueber den Zeitstempel\">\n\r</info>",
					"<timeInfos label=\"Information ueber den Zeitstempel\">\n\r", xmlFile);
			Util.oldnewstringAll("<certInfos label=\"Information ueber das Unterzeichnerzertifikat\">\n\r</info>",
					"<certInfos label=\"Information ueber das Unterzeichnerzertifikat\"\n\r", xmlFile);
			Util.oldnewstringAll("</info></info>", "</info>", xmlFile);

			fis.close();
			reader.close();

// Medatadaten Signaturen ergaenzen
			try {
				Locale localeDe = new Locale("de");

				String pathToWorkDirValdatei = configMap.get("PathToWorkDir");
				File workDir = new File(pathToWorkDirValdatei);
				File signatureTmp = new File(workDir.getAbsolutePath() + File.separator + "veraPDF_signatureTmp.xml");
				String execVerapdfSig = verapdf.execVerapdfSig(valDatei, workDir, signatureTmp, localeDe);

				// <Message></Message><Message>Metadaten der Signatur 1 [verapdf]</Message>
				// <Message> - Zeitpunkt der Unterschrift (Anbringen Signatur): [Date]</Message>
				// <Message> - Name: [Name]</Message>
				// <Message> - Ort: [Location]</Message>
				// <Message> - Grund: [Reason]</Message>

				execVerapdfSig = execVerapdfSig.replace("<Message> - ", "");
				execVerapdfSig = execVerapdfSig.replace("<Message>", "");
				execVerapdfSig = execVerapdfSig.replace("</Message>", "");
				execVerapdfSig = execVerapdfSig.replace("\t", "");
				execVerapdfSig = execVerapdfSig.replaceAll("\n", "</info>\n").replaceAll("\r", "");
				execVerapdfSig = execVerapdfSig.replace("            ", "");

				execVerapdfSig = execVerapdfSig.replace("Metadaten der Signatur",
						"</mSignature><mSignature label=\"Metadaten der Signatur");
				execVerapdfSig = execVerapdfSig.replace("[verapdf]</info>", "[verapdf]\">");
				execVerapdfSig = execVerapdfSig.replace("Zeitpunkt der Unterschrift (Anbringen Signatur): ",
						"<info label=\"Zeitpunkt der Unterschrift (Anbringen Signatur):\">");
				execVerapdfSig = execVerapdfSig.replace("Name: ", "<info label=\"Name:\">");
				execVerapdfSig = execVerapdfSig.replace("Ort: ", "<info label=\"Ort:\">");
				execVerapdfSig = execVerapdfSig.replace("Grund: ", "<info label=\"Grund:\">");
				execVerapdfSig = execVerapdfSig.replace("</mSignature><mSignature label=\"Metadaten der Signatur 1 ",
						"<mSignature label=\"Metadaten der Signatur 1 ");
				execVerapdfSig = execVerapdfSig + "</mSignature>";
				Util.oldnewstringAll("<mSignatureInfo>", execVerapdfSig, xmlFile);
				Util.oldnewstringAll("</mSignatureInfo>", "</mSignature>", xmlFile);
				Util.oldnewstringAll("</mSignature></mSignature>", "</mSignature>", xmlFile);
				Util.oldnewstringAll("</signature><signature label=\"Pruefdetails Signatur 1\"",
						"<signature label=\"Pruefdetails Signatur 1\"", xmlFile);

				// <metadata label="Metadaten (veraPDF)">
				// <mSignatureInfo></mSignatureInfo>
				// </metadata>

				// <metadata label="Metadaten (veraPDF)">
				// <mSignature label="Metadaten der Signatur 1 [verapdf]">
				// <info label="Zeitpunkt der Unterschrift (Anbringen Signatur):">[Date]
				// </info><info label="Name:">[Name]
				// </info><info label="Ort:">[Location]
				// </info><info label="Grund:">[Reason]
				// </info></mSignature>
				// </metadata>

			} catch (InterruptedException e) {
				line = "XML Error";
				System.out.println("Fehler beim auslesen der Signatur-Metadaten (InterruptedException: " + e + ")");
			}

// Konfiguration (Mandant) eintragen
			String Mixed = configMap.get("Mixed");
			String Qualified = configMap.get("Qualified");
			String SwissGovPKI = configMap.get("SwissGovPKI");
			String Upregfn = configMap.get("Upregfn");
			String Siegel = configMap.get("Siegel");
			String Amtsblattportal = configMap.get("Amtsblattportal");
			String Edec = configMap.get("Edec");
			String ESchKG = configMap.get("ESchKG");
			String FederalLaw = configMap.get("FederalLaw");
			String Strafregisterauszug = configMap.get("Strafregisterauszug");
			String KantonZugFinanzdirektion = configMap.get("KantonZugFinanzdirektion");

			if (Mixed == "yes") {
				Util.oldnewstringAll("<Mixed>no</Mixed>", "<Mixed>yes</Mixed>", xmlFile);
			}
			if (Qualified == "yes") {
				Util.oldnewstringAll("<Qualified>no</Qualified>", "<Qualified>yes</Qualified>", xmlFile);
			}
			if (SwissGovPKI == "yes") {
				Util.oldnewstringAll("<SwissGovPKI>no</SwissGovPKI>", "<SwissGovPKI>yes</SwissGovPKI>", xmlFile);
			}
			if (Upregfn == "yes") {
				Util.oldnewstringAll("<Upregfn>no</Upregfn>", "<Upregfn>yes</Upregfn>", xmlFile);
			}
			if (Siegel == "yes") {
				Util.oldnewstringAll("<Siegel>no</Siegel>", "<Siegel>yes</Siegel>", xmlFile);
			}
			if (Amtsblattportal == "yes") {
				Util.oldnewstringAll("<Amtsblattportal>no</Amtsblattportal>", "<Amtsblattportal>yes</Amtsblattportal>",
						xmlFile);
			}
			if (Edec == "yes") {
				Util.oldnewstringAll("<Edec>no</Edec>", "<Edec>yes</Edec>", xmlFile);
			}
			if (ESchKG == "yes") {
				Util.oldnewstringAll("<ESchKG>no</ESchKG>", "<ESchKG>yes</ESchKG>", xmlFile);
			}
			if (FederalLaw == "yes") {
				Util.oldnewstringAll("<FederalLaw>no</FederalLaw>", "<FederalLaw>yes</FederalLaw>", xmlFile);
			}
			if (Strafregisterauszug == "yes") {
				Util.oldnewstringAll("<Strafregisterauszug>no</Strafregisterauszug>",
						"<Strafregisterauszug>yes</Strafregisterauszug>", xmlFile);
			}
			if (KantonZugFinanzdirektion == "yes") {
				Util.oldnewstringAll("<KantonZugFinanzdirektion>no</KantonZugFinanzdirektion>",
						"<KantonZugFinanzdirektion>yes</KantonZugFinanzdirektion>", xmlFile);
			}

			line = "XML OK";

		} catch (FileNotFoundException e) {
			line = "XML Error";
			System.out.println("Fehler beim Umschreiben des egovdv logs (FileNotFoundException: " + e + ")");
		} catch (IOException e) {
			line = "XML Error";
			System.out.println("Fehler beim Umschreiben des egovdv logs (IOException: " + e + ")");
		}

		return line;
	}

	/**
	 * TODO: fuehrt eine Kontrolle aller benoetigten Dateien von egovdv durch und
	 * gibt das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkEgovdv(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fvalidateBat = new File(dirOfJarPath + File.separator + validateBat);
		File fegovCli = new File(dirOfJarPath + File.separator + egovCli);

		if (!fvalidateBat.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + validateBat;
				checkFiles = false;
			} else {
				result = result + ", " + validateBat;
				checkFiles = false;
			}
		}
		if (!fegovCli.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + egovCli;
				checkFiles = false;
			} else {
				result = result + ", " + egovCli;
				checkFiles = false;
			}
		}

		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
