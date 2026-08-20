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

import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class droid {
	private static String exeDir = ".." + File.separator + "DROID";
	private static String resourcesDroidJar = exeDir + File.separator + "droid-command-line-6.9.12.jar";
	private static String javaExe = ".." + File.separator + "Liberica_JRE" + File.separator + "bin" + File.separator
			+ "java.exe";

	/**
	 * fuehrt eine Erkennung mit DROID via cmd durch und gibt PUID und Formatname
	 * aus
	 * 
	 * @param unkFile      Datei, welche erkannt werden soll
	 * @param workDir      Temporaeres Verzeichnis
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit dem Ergebnis PUID = FORMAT_NAME
	 */
	public static String execDroid(File unkFile, File workDir, String dirOfJarPath, File directoryOfLogfile)
			throws InterruptedException {

		File exeFile = new File(dirOfJarPath + File.separator + resourcesDroidJar);
		File reportDroid = new File(directoryOfLogfile + File.separator + "DROID.txt");
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (reportDroid.exists()) {
			reportDroid.delete();
		}
		String resultExec = "";

		/*
		 * ..\Liberica_JRE\bin\java.exe -jar ..\DROID\droid-command-line-6.9.12.jar
		 * doc\KOST-Val_Anwendungshandbuch_v2.4.0.1.pdf -co FORMAT_NAME PUID
		 * 
		 * "PUID","FORMAT_NAME"
		 * 
		 * "fmt/478","Acrobat PDF/A - Portable Document Format"
		 * 
		 * => Linie mit fmt nehmen
		 * 
		 * => "," ersetzten durch = "fmt/478 = Acrobat PDF/A - Portable Document Format"
		 */

		ProcessBuilder pb = new ProcessBuilder(javaExe, "-jar", exeFile.getAbsolutePath(), unkFile.getAbsolutePath(),
				"-co", "FORMAT_NAME", "PUID");

		// Entspricht > reportDroid
		pb.redirectOutput(new java.io.File(reportDroid.getAbsolutePath()));

		pb.redirectErrorStream(true);

		Process process;
		try {
			process = pb.start();

			int exitCode = process.waitFor();

			// System.out.println("DROID beendet mit Exit-Code: " + exitCode);
			// 0 ist gut
			if (reportDroid.exists()) {
				// System.out.println("alles io report auslesen und weitergeben");
				String strReportDroid = Util.getStringFromFile(reportDroid);
				// System.out.println("strReportDroid: " + strReportDroid);
				strReportDroid = strReportDroid.replace("\"PUID\",\"FORMAT_NAME\"", "");
				strReportDroid = strReportDroid.replace("\",\"", " = ");
				// System.out.println("strReportDroid bereinigt: " + strReportDroid);
				resultExec = strReportDroid;
			} else {
				System.out.println("Datei nicht angelegt... DROID Exit-Code: " + exitCode);
				resultExec = "NoReport";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (reportDroid.exists()) {
			reportDroid.delete();
		}
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von droid durch und gibt das
	 * Ergebnis als boolean zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * 
	 * @return Boolean mit Kontrollergebnis
	 */
	public static String checkDroid(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File exeFile = new File(dirOfJarPath + File.separator + resourcesDroidJar);
		if (!exeFile.exists()) {
			// fehlende Datei
			result = " " + exeFile + ": " + exeFile;
			checkFiles = false;
		}
		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
