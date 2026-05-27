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

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class ImageMagick {
	private static String exeDir = "resources" + File.separator + "ImageMagick-7.1.2-18-portable-Q16-x64";
	private static String magickExe = exeDir + File.separator + "magick.exe";

	/**
	 * fuehrt eine Lesekontrolle mit ImageMagick via cmd durch und speichert das
	 * Ergebnis in ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param checkFile      MKV-Datei, welche validiert werden soll
	 * @param report       Datei fuer den Report
	 * @param workDir      Temporaeres Verzeichnis
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execImageMagick(File checkFile, File report, File workDir, String dirOfJarPath)
			throws InterruptedException {
		boolean out = true;
		File fmagickExe = new File(dirOfJarPath + File.separator + magickExe);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (report.exists()) {
			report.delete();
		}

		// ImageMagick-Befehl: PathTo_magick.exe checkFile -regard-warnings -verbose info: 2> report
		String command = "\"\"" + fmagickExe.getAbsolutePath() + "\" \""
				+ checkFile.getAbsolutePath() + "\" -regard-warnings -verbose info: 2>\"" + report.getAbsolutePath() + "\"\"";

		// System.out.println( "command: " + command );

		Util.switchOffConsole();
		String resultExec = Cmd.execToString(command, out, workDir);
		Util.switchOnConsole();

		// ImageMagick gibt teilweise Eigenschaften raus, die replaced oder ignoriert werden
		// muss

		 //System.out.println( "resultExec: " + resultExec );
		resultExec="OK";

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
	 * fuehrt eine Kontrolle aller benoetigten Dateien von ImageMagick durch und
	 * gibt das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkImageMagick(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fmagickExe = new File(dirOfJarPath + File.separator + magickExe);

		if (!fmagickExe.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + magickExe;
				checkFiles = false;
			} else {
				result = result + ", " + magickExe;
				checkFiles = false;
			}
		}

		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
