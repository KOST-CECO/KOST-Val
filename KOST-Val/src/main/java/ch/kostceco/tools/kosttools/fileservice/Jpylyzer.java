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

import java.io.File;
import java.io.IOException;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Jpylyzer {
	private static String exeDir = "resources" + File.separator + "jpylyzer_2.2.1_win64";
	private static String resourcesJpylyzerExe = exeDir + File.separator + "jpylyzer.exe";

	/**
	 * fuehrt eine Validierung mit jpylyzer via cmd durch und speichert das Ergebnis
	 * in ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param jp2File      JP2-Datei, welche validiert werden soll
	 * @param report       Datei fuer den Report
	 * @param workDir      Temporaeres Verzeichnis
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execJpylyzer(File jp2File, File report, File workDir, String dirOfJarPath)
			throws InterruptedException {
		boolean out = false;
		File exeFile = new File(dirOfJarPath + File.separator + resourcesJpylyzerExe);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (report.exists()) {
			report.delete();
		}

		// jpylyzer unterstuetzt nicht alle Zeichen resp Doppelleerschlag
		File jp2FileNormalisiert = new File(workDir + File.separator + "JP2.jp2");
		try {
			Util.copyFile(jp2File, jp2FileNormalisiert);
		} catch (IOException e) {
			// Normalisierung fehlgeschlagen es wird ohne versucht
			jp2FileNormalisiert = jp2File;
		}
		if (!jp2FileNormalisiert.exists()) {
			jp2FileNormalisiert = jp2File;
		}

		// jpylyzer-Befehl: pathToJpylyzerExe jp2File > report
		String command = "\"\"" + exeFile.getAbsolutePath() + "\" \"" + jp2FileNormalisiert.getAbsolutePath()
				+ "\" > \"" + report.getAbsolutePath() + "\"\"";

		String resultExec = Cmd.execToStringSplit(command, out, workDir);

		// System.out.println( "resultExec: " + resultExec );
		/*
		 * Folgender Error Output ist keiner sondern nur Info und kann mit OK ersetzt
		 * werden: ERROR: User warning: ignoring unknown box
		 */
		String ignor = "ERROR: User warning: ignoring unknown box";
		if (resultExec.equals(ignor)) {
			resultExec = "OK";
		} else {
			/*
			 * ERROR: Schemas validity error : Element
			 * '{http://www.admin.ch/xmlns/siard/1.0/schema0/table2.xsd}row': This element
			 * is not expected.</Message><Message>ERROR:
			 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\
			 * schema0\table2\table2.xml fails to validate
			 */
			/*
			 * String replaceInfo = "</Message><Message>ERROR: " + jp2File.getAbsolutePath()
			 * + " fails to validate"; resultExec = resultExec.replace( replaceInfo, "" );
			 */

			// Jpylyzer gibt keine Info raus, die replaced werden muss
		}

		if (resultExec.equals("OK")) {
			if (report.exists()) {
				// alles io bleibt bei OK
			} else {
				// Datei nicht angelegt...
				resultExec = "NoReport";
			}
		}
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von jpylyzer durch und gibt
	 * das Ergebnis als boolean zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return Boolean mit Kontrollergebnis
	 */
	public static String checkJpylyzer(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		String jpylyzerExe = dirOfJarPath + File.separator + resourcesJpylyzerExe;
		File fjpylyzerExe = new File(jpylyzerExe);
		if (!fjpylyzerExe.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + jpylyzerExe;
				checkFiles = false;
			} else {
				result = result + ", " + jpylyzerExe;
				checkFiles = false;
			}
		}
		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
