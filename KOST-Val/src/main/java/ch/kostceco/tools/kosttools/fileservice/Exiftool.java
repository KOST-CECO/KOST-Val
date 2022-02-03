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

public class Exiftool
{
	private static String	exeDir			= "resources" + File.separator + "ExifTool-10.15";
	private static String	identifyPl	= exeDir + File.separator + "exiftool.pl";
	private static String	perl				= exeDir + File.separator + "Perl" + File.separator + "bin"
			+ File.separator + "perl.exe";

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
	public static String execExiftool( String options, File tiffFile, File report, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = true;
		File fIdentifyPl = new File( dirOfJarPath + File.separator + identifyPl );
		File fPerl = new File( dirOfJarPath + File.separator + perl );
		// falls das File von einem vorhergehenden Durchlauf bereits existiert, loeschen wir es
		if ( report.exists() ) {
			report.delete();
		}

		// Exiftool-Befehl: pathToPerl pathToExiftoolExe options tiffFile >> report
		String command = "\"\"" + fPerl.getAbsolutePath() + "\" \"" + fIdentifyPl.getAbsolutePath()
				+ "\" " + options + " \"" + tiffFile.getAbsolutePath() + "\" >>\""
				+ report.getAbsolutePath() + "\"\"";

		// System.out.println( "command: " + command );

		String resultExec = Cmd.execToString( command, out, workDir );

		// Exiftool gibt keine Info raus, die replaced oder ignoriert werden muss

		// System.out.println( "resultExec: " + resultExec );
		/* Folgender Error Output ist keiner sondern nur Info und kann mit OK ersetzt werden: ERROR:
		 * User warning: ignoring unknown box String ignor =
		 * "ERROR: User warning: ignoring unknown box"; if ( resultExec.equals( ignor ) ) { resultExec =
		 * "OK"; } else { /* ERROR: Schemas validity error : Element
		 * '{http://www.admin.ch/xmlns/siard/1.0/schema0/table2.xsd}row': This element is not
		 * expected.</Message><Message>ERROR:
		 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\schema0\table2\table2.xml fails
		 * to validate */

		/* String replaceInfo = "</Message><Message>ERROR: " + jp2File.getAbsolutePath() +
		 * " fails to validate"; resultExec = resultExec.replace( replaceInfo, "" ); } */

		if ( resultExec.equals( "OK" ) ) {
			if ( report.exists() ) {
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
	public static String checkExiftool( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fIdentifyPl = new File( dirOfJarPath + File.separator + identifyPl );
		File fPerl = new File( dirOfJarPath + File.separator + perl );

		if ( !fIdentifyPl.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + identifyPl;
				checkFiles = false;
			} else {
				result = result + ", " + identifyPl;
				checkFiles = false;
			}
		}
		if ( !fPerl.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + perl;
				checkFiles = false;
			} else {
				result = result + ", " + perl;
				checkFiles = false;
			}
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
