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

public class ffmpeg
{
	private static String	exeDir		= ".." + File.separator + "ffmpeg"
			+ File.separator + "bin";
	private static String	ffmpegExe	= exeDir + File.separator
			+ "ffmpeg.exe";
	private static String	ffplayExe	= exeDir + File.separator
			+ "ffplay.exe";
	private static String	ffprobeExe	= exeDir + File.separator
			+ "ffprobe.exe";
	private static String	ffprobeExe2	= File.separator + "ffmpeg" + File.separator + "bin"+ File.separator
			+ "ffprobe.exe";

	/**
	 * fuehrt eine Analyse mit ffprobe via cmd durch und speichert das Ergebnis
	 * in ein File (Output). Gibt zurueck ob Output existiert oder nicht
	 * 
	 * ffprobe -show_format -show_streams -loglevel quiet videofile
	 * 
	 * @param insensitiveOption
	 *            Option betreffend Gross- und Kleinschreibung
	 * @param searchString
	 *            gesuchter Text
	 * @param fileToGrep
	 *            Datei in welcher gesucht werden soll
	 * @param output
	 *            Ausgabe des Resultates
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execFfprobe( File fileToProbe, File output,
			File workDir, String dirOfJarPath ) throws InterruptedException
	{
		boolean out = true;
		File fffprobeExe = new File( dirOfJarPath + File.separator + ffprobeExe  );
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if ( output.exists() ) {
			output.delete();
		}

		// Doppelleerschlag im Pfad einer Datei bereitet Probleme (leerer Report)
		// Video-Datei muss zuvor bei Doppelleerschlag in temp-Verzeichnis kopiert werden

		// ffprobe -show_format -show_streams -loglevel quiet -o outputfile
		// videofile
/*		String command = "\"\"" + fffprobeExe.getAbsolutePath() + "\" "
				+ " -show_format -show_streams -loglevel quiet \""
				+ fileToProbe.getAbsolutePath() + "\"\"";*/
		String command = "\"\"" + fffprobeExe.getAbsolutePath() + "\" "
				+ "-show_format -show_streams -loglevel quiet -o \""
				+ output.getAbsolutePath() + "\" \""
				+ fileToProbe.getAbsolutePath() + "\"\"";

		// System.out.println( "command: " + command );

		String resultExec = Cmd.execToStringSplit( command, out, workDir );
		// System.out.println( "resultExec: " + resultExec );

		// ffprobe gibt keine Info raus, die replaced oder ignoriert werden muss

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

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von ffmpeg/ffplay/ffprobe
	 * durch und gibt das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkFfmpegPlayProbe( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fffmpegExe = new File( dirOfJarPath + File.separator + ffmpegExe );
		File fffplayExe = new File( dirOfJarPath + File.separator + ffplayExe );
		File fffprobeExe = new File(
				dirOfJarPath + File.separator + ffprobeExe );

		if ( !fffmpegExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + ffmpegExe;
				checkFiles = false;
			} else {
				result = result + ", " + ffmpegExe;
				checkFiles = false;
			}
		}
		if ( !fffplayExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + ffplayExe;
				checkFiles = false;
			} else {
				result = result + ", " + ffplayExe;
				checkFiles = false;
			}
		}
		if ( !fffprobeExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + ffprobeExe;
				checkFiles = false;
			} else {
				result = result + ", " + ffprobeExe;
				checkFiles = false;
			}
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
