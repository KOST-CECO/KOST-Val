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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class egovdv {
	private static String exeDir = "resources" + File.separator + "egov-validationclient-cli";
	private static String validateBat = exeDir + File.separator + "validate.bat";
	private static String egovCli = exeDir + File.separator + "lib" + File.separator
			+ "intarsys-egov-validationclient-cli-1.0.10.jar";

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
					String mainInfo = "[main] INFO  de.intarsys.tools.yalf.api";
					String mainExeption = "[main] ERROR d.intarsys.egov.validationclient.cli - Unexpected exception";
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
						} else if (line.contains(mainInfo)) {
							// zweite Linie vorhanden
							mainInfoBoo = true;
						} else if (line.contains(mainExeption)) {
							// Unexpected exception vorhanden
							mainInfoBoo = false;
						} else {
							// andere Linie
							if (valDateiNameBoo && mainInfoBoo) {
								if (line.contains("   ")) {
									counterSig = counterSig + 1;
									// Signame ausgeben
									// System.out.println( "egovdv Signame: " +
									// line );
								}
							}
						}
					}
					count = counterSig;

					scannerFormat.close();

					if (!valDateiNameBoo && !mainInfoBoo) {
						// die ersten beiden Zeilen fehlen
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
	 * @param -c Container check, validates all signatures in the pdf file.
	 * @param -d Logs the JSON object of the request and response.
	 * @param -e Generate report even for unsigned files
	 * @param -f file to validate
	 * @param -l get pdf report in the given language, supported codes: de, fr, it,
	 *           en. This is an optional parameter, if omitted de is used.
	 * @param -m mandator to use
	 * @param -o pdf report will be saved at the given name
	 * 
	 * @param -u URL of the validation webservice.
	 *
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execEgovdvCheck(File fileToCheck, File output, File workDir, String dirOfJarPath,
			String mandant, Locale locale) throws InterruptedException {
		boolean out = true;
		File fvalidateBat = new File(dirOfJarPath + File.separator + validateBat);
		File fexeDir = new File(exeDir);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (output.exists()) {
			output.delete();
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
					+ fileToCheck.getAbsolutePath() + "\" -l " + optionLanguage + " -c -e -o \""
					+ output.getAbsolutePath() + "\"\"";

			// validate <account> -u https://egovsigval-backend.bit.admin.ch -m
			// Mixed -f <filename> -c -e -d -o <report>

			// System.out.println("command: " + command);

			String resultExec = Cmd.execToStringSplit(command, out, workDir);

			// System.out.println( "resultExec: " + resultExec );

			// egovdv gibt zu viele Infos raus. Entsprechend hier eine kleine
			// vorab
			// analyse

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
				// TODO: Hinweis Internet / Test Internet
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
	public static String analyseEgovdvPdf(File output) {
		String lineOut = "LineNotFound";
		try {
			// Auslesen mit pdfbox
			lineOut = pdfbox.getTextPdfbox(output);
			// System.out.println( "lineOut=" + lineOut );
			lineOut = prettyEgovdvPdf(lineOut);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Fehler beim auslesen des egovdv-Reports (analyseEgovdvPdf)");
		}
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
		// System.out.println( "1 " + line );

		String newLine = "</Message><Message></Message><Message>" + line;
		// System.out.println( "newLine=" + newLine );
		String prettyPrint = newLine.replaceAll(":__", ": ");
		prettyPrint = prettyPrint.replaceAll("__ __", "</Message><Message> - ");

		String pathToKostValDir = System.getenv("USERPROFILE") + File.separator + ".kost-val_2x";
		File directoryOfConfigfile = new File(pathToKostValDir + File.separator + "configuration");
		File configFile = new File(directoryOfConfigfile + File.separator + "kostval.conf.xml");

		Document doc = null;
		String Institut = "Institut";

		try {
			BufferedInputStream bis;
			bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();

			Institut = doc.getElementsByTagName("Institut").item(0).getTextContent();
			bis.close();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			System.out.println("Fehler beim auslesen der config (egovdv)");
		}

		// Bereinigung ist nur auf de, der log wird danach in Controllervalfofile
		// uebersetzt

		prettyPrint = prettyPrint.replaceAll("Prüfbericht für elektronische Signaturen",
				"Prüfbericht für elektronische Signaturen</Message><Message> - Geprüft durch: " + Institut);

		prettyPrint = prettyPrint.replaceAll("__Datum/Zeit der Prüfung:",
				"</Message><Message> - Datum/Zeit der Prüfung:");
		prettyPrint = prettyPrint.replaceAll("__Name der signierten __Datei:",
				"</Message><Message> - Name der signierten __Datei:");
		prettyPrint = prettyPrint.replaceAll("__Name der signierten", "</Message><Message> - Name der signierten");
		prettyPrint = prettyPrint.replaceAll("__Datei:", "Datei:");
		prettyPrint = prettyPrint.replaceAll("__Hash der Datei ", "</Message><Message> - Hash der Datei ");

		/*
		 * Der Validator prüft, ob die in einem Dokument enthaltenen Signaturen den für
		 * die Prüfung auszuwählenden Kriterien entsprechen. Die Kriterien können sich
		 * auf die Gültigkeit des Dokuments als Ganzes (z. B. gültiger
		 * Strafregisterauszug) oder auf die Gültigkeit aller darin enthaltenen
		 * Unterschriften beziehen (z.B . qualifiziert signiertes Dokument).
		 */
		// __Der Validator prüft, ob die in einem Dokument enthaltenen
		// Signaturen den für die Prüfung
		// __auszuwählenden Kriterien entsprechen. Die Kriterien können sich auf
		// die Gültigkeit des Dokuments
		// __als Ganzes (z. B. gültiger Strafregisterauszug) oder auf die
		// Gültigkeit aller darin enthaltenen
		// __Unterschriften beziehen (z.B . qualifiziert signiertes Dokument).

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
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 1", "");
		prettyPrint = prettyPrint.replaceAll("__Anzahl Signaturen im Dokument: 0", "");
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
		if (prettyPrint.contains("Diese Signatur ist nicht LTV-fähig")) {
			prettyPrint = prettyPrint.replaceAll("</Message><Message> - Das Dokument ist ",
					"</Message><Message> - Nicht alle Signaturen sind LTV-fähig.</Message><Message> - Das Dokument ist ");
		} else {
			prettyPrint = prettyPrint.replaceAll("</Message><Message> - Das Dokument ist ",
					"</Message><Message> - Alle Signaturen sind LTV-fähig.</Message><Message> - Das Dokument ist ");
		}
		prettyPrint = prettyPrint.replaceAll("__ Alle in diesem Dokument",
				"</Message><Message> - Alle in diesem Dokument");

		// Bereinigung Prüfdetails
		prettyPrint = prettyPrint.replaceAll("__Prüfdetails Signatur",
				"</Message><Message></Message><Message>Prüfdetails Signatur [egovdv]");
		prettyPrint = prettyPrint.replaceAll("__Informationen zur Signatur", "");
		prettyPrint = prettyPrint.replaceAll("__Zeitpunkt der ", "</Message><Message> - Zeitpunkt der ");
		prettyPrint = prettyPrint.replaceAll("__Signaturalgorithmus:", "</Message><Message> - Signaturalgorithmus:");
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
		prettyPrint = prettyPrint.replace("Grund: ", "");
		// System.out.println( "2 " + prettyPrint );

		lineOut = prettyPrint;

		return lineOut;
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
