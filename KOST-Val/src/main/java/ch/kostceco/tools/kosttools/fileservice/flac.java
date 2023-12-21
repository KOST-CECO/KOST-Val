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

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class flac
{
	private static String	exeDir					= ".." + File.separator
			+ "flac" + File.separator + "Win64";
	private static String	resourcesFlacExe		= exeDir + File.separator
			+ "flac.exe";
	private static String	resourcesLibFlacPpDll	= exeDir + File.separator
			+ "libFLAC++.dll";
	private static String	resourcesLibFlacDll		= exeDir + File.separator
			+ "libFLAC.dll";
	private static String	resourcesMetaFlacExe	= exeDir + File.separator
			+ "metaflac.exe";

	/**
	 * fuehrt eine Validierung mit flac via cmd durch und speichert das Ergebnis
	 * in ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param flacFile
	 *            FLAC-Datei, welche getestet werden soll
	 * @param report
	 *            Datei fuer den Report
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execFlac( File flacFile, File report, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = false;
		File exeFile = new File(
				dirOfJarPath + File.separator + resourcesFlacExe );
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if ( report.exists() ) {
			report.delete();
		}

		// flac-Befehl: pathToFlacExe -t flacFile 2> report
		String command = "\"\"" + exeFile.getAbsolutePath() + "\" -t \""
				+ flacFile.getAbsolutePath() + "\" 2> \""
				+ report.getAbsolutePath() + "\"\"";

		String resultExec = Cmd.execToStringSplit( command, out, workDir );

		// System.out.println( "resultExec: " + resultExec );

		// Flac gibt keine Info raus, die replaced werden muss

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

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von flac durch und gibt
	 * das Ergebnis als boolean zurueck
	 * 
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return Boolean mit Kontrollergebnis
	 */
	public static String checkFlac( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		String flacExe = dirOfJarPath + File.separator + resourcesFlacExe;
		String libFlacPpDll = dirOfJarPath + File.separator
				+ resourcesLibFlacPpDll;
		String libFlacDll = dirOfJarPath + File.separator + resourcesLibFlacDll;
		String metaFlacExe = dirOfJarPath + File.separator
				+ resourcesMetaFlacExe;
		File fflacExe = new File( flacExe );
		File flibFlacPpDll = new File( libFlacPpDll );
		File flibFlacDll = new File( libFlacDll );
		File fmetaFlacExe = new File( metaFlacExe );
		if ( !fflacExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + flacExe;
				checkFiles = false;
			} else {
				result = result + ", " + flacExe;
				checkFiles = false;
			}
		}
		if ( !flibFlacPpDll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + libFlacPpDll;
				checkFiles = false;
			} else {
				result = result + ", " + libFlacPpDll;
				checkFiles = false;
			}
		}
		if ( !flibFlacDll.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + libFlacDll;
				checkFiles = false;
			} else {
				result = result + ", " + libFlacDll;
				checkFiles = false;
			}
		}
		if ( !fmetaFlacExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + metaFlacExe;
				checkFiles = false;
			} else {
				result = result + ", " + metaFlacExe;
				checkFiles = false;
			}
		}
		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
