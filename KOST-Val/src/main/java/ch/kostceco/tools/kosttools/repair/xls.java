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

package ch.kostceco.tools.kosttools.repair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class xls {
    static String excelCnv = "C:\\Program Files\\Microsoft Office\\root\\Office16\\excelcnv.exe";
	private static File exeFile = new File(excelCnv);

	/**
	 * Migration der xls zu eine xlsx
	 * 
	 * @param xlsOldFile       XLS-Datei, welche migriert werden soll
	 * @param xlsxNewFile      XLSX-Datei, welche erstellt wird Temporaeres Verzeichnis
	 * @param workDir          Temporaeres Verzeichnis
	 * @param dirOfJarPath     String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit dem Ergebnis 
	 */
	public  String repairXlsXlsx(File xlsOldFile, File xlsxNewFile, String dirOfJarPath, File directoryOfLogfile, File workDir)
			throws InterruptedException {
		// System.out.println("repairXlsXlsx" );
		if (xlsxNewFile.exists()) {
			return "xlsxAllreadyExists";
		}
		String check=checkExcelcnv(  dirOfJarPath);
		if (check.equals("OK")) {
			Boolean repaired = convert( xlsOldFile,  xlsxNewFile);
			String repair="OK";
			if (repaired) {
			 repair="OK";
				if (xlsxNewFile.exists()) {
					return "OK";
				} else {
					// System.out.println("Datei nicht angelegt...  2" );
					return  "NoXlsx";
				}
			} else {
				 repair="nOK";
			}
			return repair;
		} else {
			return check;
		}
	}
	
	/**
	 * fuehrt eine Migration mit excelcnv via cmd durch und erstellt aus der xls eine xlsx
	 * 
	 * @param xlsOldFile       XLS-Datei, welche migriert werden soll
	 * @param xlsxNewFile      XLSX-Datei, welche erstellt wird Temporaeres Verzeichnis
	 * @param dirOfJarPath     String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit dem Ergebnis 
	 */
   public static boolean convert(File xlsFile, File xlsxFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    excelCnv,
                    "-oice",
                    xlsFile.getAbsolutePath(),
                    xlsxFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                @SuppressWarnings("unused")
				String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println(line);
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0 && xlsxFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von Excelcnv durch und gibt das
	 * Ergebnis als boolean zurueck
	 * 
	 * @param exeFile C:\Program Files\Microsoft Office\root\Office16\excelcnv.exe
	 * 
	 * @return Boolean mit Kontrollergebnis
	 */
	public static String checkExcelcnv(String dirOfJarPath) {
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?
		if (!exeFile.exists()) {
			// fehlende Datei
			result = "exeFileMissing: " + exeFile.getAbsolutePath();
			checkFiles = false;
		}
		if (checkFiles) {
			result = "OK";
		}
		return result;
	}
}
