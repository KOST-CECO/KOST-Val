/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO. 2012-2022
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

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Sed
{
	private static String	exeDir				= "resources" + File.separator + "sed";
	private static String	sedExe				= exeDir + File.separator + "sed.exe";
	private static String	msys20dll			= exeDir + File.separator + "msys-2.0.dll";
	private static String	msysgccs1dll	= exeDir + File.separator + "msys-gcc_s-1.dll";
	private static String	msysiconv2dll	= exeDir + File.separator + "msys-iconv-2.dll";
	private static String	msysintl8dll	= exeDir + File.separator + "msys-intl-8.dll";

	/** fuehrt eine Validierung mit Exiftool via cmd durch und speichert das Ergebnis in ein File
	 * (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param options
	 *          Option wie exiftool angesprochen werden soll
	 * @param tiffFile
	 *          tiff-Datei, welche validiert werden soll
	 * @param report
	 *          Datei fuer den Report
	 * @param workDir
	 *          Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *          String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception */
	public static String execSed( String options, File fileToSed, File output, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = true;
		File fsedExe = new File( dirOfJarPath + File.separator + sedExe );
		// falls das File von einem vorhergehenden Durchlauf bereits existiert, loeschen wir es
		if ( output.exists() ) {
			output.delete();
		}

		// Sed-Befehl: pathToSedExe options fileToSed > output
		String command = "\"\"" + fsedExe.getAbsolutePath()
				+ "\" " + options + " \"" + fileToSed.getAbsolutePath() + "\" > \""
				+ output.getAbsolutePath() + "\"\"";

		// System.out.println( "command: " + command );

		String resultExec = Cmd.execToString( command, out, workDir );

		// Sed gibt keine Info raus, die replaced oder ignoriert werden muss

		if ( resultExec.equals( "OK" ) ) {
			if ( output.exists() ) {
				// alles io bleibt bei OK
			} else {
				// Datei nicht angelegt...
				resultExec = "NoReport";
			}
		}
		return resultExec;
	}

	/** fuehrt eine Kontrolle aller benoetigten Dateien von Exiftool durch und gibt das Ergebnis als
	 * String zurueck
	 * 
	 * @param dirOfJarPath
	 *          String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis */
	public static String checkSed( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fsedExe = new File( dirOfJarPath + File.separator + sedExe );
		File fmsys20dll = new File( dirOfJarPath + File.separator + msys20dll );
		File fmsysgccs1dll = new File( dirOfJarPath + File.separator + msysgccs1dll );
		File fmsysiconv2dll = new File( dirOfJarPath + File.separator + msysiconv2dll );
		File fmsysintl8dll = new File( dirOfJarPath + File.separator + msysintl8dll );

		if ( !fsedExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + sedExe;
				checkFiles = false;
			} else {
				result = result + ", " + sedExe;
				checkFiles = false;
			}
		}
		if ( !fmsys20dll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + msys20dll;
				checkFiles = false;
			} else {
				result = result + ", " + msys20dll;
				checkFiles = false;
			}
		}
		if ( !fmsysgccs1dll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + msysgccs1dll;
				checkFiles = false;
			} else {
				result = result + ", " + msysgccs1dll;
				checkFiles = false;
			}
		}
		if ( !fmsysiconv2dll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + msysiconv2dll;
				checkFiles = false;
			} else {
				result = result + ", " + msysiconv2dll;
				checkFiles = false;
			}
		}
		if ( !fmsysintl8dll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + msysintl8dll;
				checkFiles = false;
			} else {
				result = result + ", " + msysintl8dll;
				checkFiles = false;
			}
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
