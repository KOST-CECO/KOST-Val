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
// import java.util.concurrent.TimeUnit;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Dbptk {
	private static String exeDir = "resources" + File.separator + "dbptk-app-4.0.0";
	private static String dbptkJar = exeDir + File.separator + "dbptk-app-4.0.0.jar";

	/**
	 * fuehrt eine Validierung mit dbptk via cmd durch und speichert das Ergebnis in
	 * ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param siardFile    SIARD-Datei, welche validiert werden soll
	 * @param report       Datei fuer den Report
	 * @param workDir      Temporaeres Verzeichnis
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execDbptk(File siardFile, File report, File logFile, File workDir, String dirOfJarPath)
			throws InterruptedException {
		File fdbptkJar = new File(dirOfJarPath + File.separator + dbptkJar);
		File filedirOfJarPath = new File(dirOfJarPath);
		File javaFile = new File(filedirOfJarPath.getParent() + File.separator + "Liberica_JRE" + File.separator + "bin"
				+ File.separator + "java.exe");
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (report.exists()) {
			report.delete();
		}
		if (logFile.exists()) {
			logFile.delete();
		}

		String resultExec = "NoReport";
		if (javaFile.exists()) {
			// alles ok
		} else {
			// java.exe fehlt
			System.out.println("java.exe fehlt bei " + javaFile.getAbsolutePath());
		}

		/*
		 * Usage: dbptk validate [OPTIONS]
		 * 
		 * 
		 * -if, --import-file=value (required) Path to SIARD2 archive file.
		 * 
		 * 
		 * -r, --report=value (optional) Path to save the validation report. If not set
		 * a report will be generated in the installation folder.
		 * 
		 * -sac, --skip-additional-checks optional) Run the SIARD validation without the
		 * additional checks. The additional checks can be found at:
		 * 
		 */

		/*
		 * //String command = "\"" + javaFile.getAbsolutePath() + "\" -jar \"" +
		 * fdbptkJar.getAbsolutePath() + "\" validate -if \"" // +
		 * siardFile.getAbsolutePath() + "\" -r \"" + report.getAbsolutePath() +
		 * "\" -sac";
		 * 
		 * System.out.println("command: " + command);
		 * 
		 * try { ProcessBuilder pb = new ProcessBuilder("java",
		 * "NameDesAnderenProgramms"); Process process = pb.start(); // Hier können Sie
		 * weitere Operationen mit dem Prozess durchführen, z.B. auf die Ausgabe warten
		 * process.waitFor(); } catch (IOException | InterruptedException e) {
		 * e.printStackTrace(); }
		 * 
		 * String resultExec = Cmd.execToStringSplit(command, out, workDir);
		 * 
		 * // dbptk gibt keine Info raus, die replaced oder ignoriert werden muss
		 * 
		 * System.out.println("resultExec: " + resultExec);
		 * 
		 * if (resultExec.equals("OK")) { if (report.exists()) { // alles io bleibt bei
		 * OK } else { // Datei nicht angelegt... resultExec = "NoReport"; } } return
		 * resultExec;
		 * 
		 * Runtime.getRuntime().exec funktioniert hier nicht!
		 * 
		 * Entsprechend wurde processBuilder verwendet
		 */

// Define the command and arguments
		ProcessBuilder processBuilder = new ProcessBuilder(
//    "C:\\Users\\clair\\eclipse-Workspace\\KOST-Tools\\Liberica_JRE\\bin\\java.exe",
				javaFile.getAbsolutePath().toString(), "-jar",
//    "C:\\Users\\clair\\eclipse-Workspace\\KOST-Tools\\KOST-Val\\resources\\dbptk-app-4.0.0\\dbptk-app-4.0.0.jar",
				fdbptkJar.getAbsolutePath().toString(), "validate", "-if",
//    "C:\\Users\\clair\\Documents\\_Entwicklung\\inAr_2025-11_SIARD-Validierung_DBPTK\\SIARD-2.1_valid_sakila.siard",
				siardFile.getAbsolutePath().toString(), "-r",
//    "C:\\Users\\clair\\.kost-val_2x\\logs\\dbptk.txt",
				report.getAbsolutePath().toString(), "-sac");

// Optional: set working directory if needed
//processBuilder.directory(new File("C:\\Users\\clair\\eclipse-Workspace\\KOST-Tools\\KOST-Val"));
		processBuilder.directory(workDir);

// Define where to store the process output
//File logFile = new File("C:\\Users\\clair\\.kost-val_2x\\logs\\dbptk_process_output.log");

// Redirect both stdout and stderr to the same log file
		processBuilder.redirectOutput(logFile);
		processBuilder.redirectErrorStream(true);

		try {
			// Start the process
			Process process = processBuilder.start();

			// Wait until the process completes
			int exitCode = process.waitFor();
			// System.out.println("Process finished with exit code: " + exitCode);
			// System.out.println("Output saved to: " + logFile.getAbsolutePath());

			if (exitCode == 0) {
				if (report.exists() && logFile.exists()) {
					// alles io bleibt bei OK
					resultExec = "OK";
				}
			}

			if (report.exists() && logFile.exists()) {
				// alles io bleibt bei OK
				resultExec = "OK";
			} else {
				if (!report.exists()) {

					// Datei nicht angelegt...
					resultExec = exitCode + ": NoReport";
				} else if (!logFile.exists()) {

					// Datei nicht angelegt...
					resultExec = exitCode + ": NoLogFile";
				}
			}
			/*
			 * * TODO Kontrollieren ob destroy & destroyForcibly eingesetzt werden kann
			 * 
			 * if (process != null && process.isAlive()) { process.destroy();
			 * 
			 * try { if (!process.waitFor(1, TimeUnit.SECONDS)) { process.destroyForcibly();
			 * } } catch (InterruptedException ep) { Thread.currentThread().interrupt();
			 * process.destroyForcibly(); }
			 *
			 * }
			 */

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von dbptk durch und gibt das
	 * Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkDbptk(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fdbptkJar = new File(dirOfJarPath + File.separator + dbptkJar);

		if (!fdbptkJar.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + dbptkJar;
				checkFiles = false;
			} else {
				result = result + ", " + dbptkJar;
				checkFiles = false;
			}
		}

		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
