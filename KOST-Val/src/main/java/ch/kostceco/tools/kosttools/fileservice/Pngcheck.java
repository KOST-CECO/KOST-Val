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
import java.io.IOException;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Pngcheck
{
	private static String	exeDir					= "resources"
			+ File.separator + "pngcheck-3.0.2-win32";
	private static String	resourcesPngcheckExe	= exeDir + File.separator
			+ "pngcheck.win32.exe";

	/**
	 * fuehrt eine Validierung mit Pngcheck via cmd durch und speichert das
	 * Ergebnis in ein File (Report). Gibt zurueck ob Report existiert oder
	 * nicht
	 * 
	 * @param pngFile
	 *            png-Datei, welche validiert werden soll
	 * @param report
	 *            Datei fuer den Report
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execPngcheck( File pngFile, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = true;
		File exeFile = new File(
				dirOfJarPath + File.separator + resourcesPngcheckExe );

		// Pngcheck unterstuetzt nicht alle Zeichen
		File pngFileNormalisiert = new File(
				workDir + File.separator + "PNG.png" );
		try {
			Util.copyFile( pngFile, pngFileNormalisiert );
		} catch ( IOException e ) {
			// Normalisierung fehlgeschlagen es wird ohne versucht
			pngFileNormalisiert = pngFile;
		}
		if ( !pngFileNormalisiert.exists() ) {
			pngFileNormalisiert = pngFile;
		}

		// Pngcheck-Befehl: pathToPngcheckExe pngFile > report
		String command = "\"\"" + exeFile.getAbsolutePath() + "\" \""
				+ pngFileNormalisiert.getAbsolutePath() + "\"";

		String resultExec = Cmd.execToString( command, out, workDir );
		Util.deleteFile( new File( workDir + File.separator + "PNG.png" ) );
		// System.out.println("resultExec: "+ resultExec );

		/*
		 * Folgender Output ist keiner sondern nur Info und kann mit OK ersetzt
		 * werden:
		 * 
		 * OK: C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\PNG.png (DETAILS
		 * ...
		 * 
		 * Wenn OK zurueckgegeben wird konnte der PNGCHECK nicht korrekt gemacht
		 * werden
		 */
		if ( resultExec.equals( "OK" ) ) {
			resultExec = "FAIL Pngcheck";
		} else if ( resultExec.startsWith( "OK: " ) ) {
			resultExec = "OK";
		} else {
			// {Dateipfad} invalid IHDR image type (1) ERROR: {Dateipfad}
			resultExec = resultExec.replace( "</Message><Message>ERROR: "
					+ pngFileNormalisiert.getAbsolutePath(), "" );
			resultExec = resultExec.replace(
					pngFileNormalisiert.getAbsolutePath() + "  ", "" );
			resultExec = resultExec
					.replace( pngFileNormalisiert.getAbsolutePath() + " ", "" );
		}
		// System.out.println("resultExec: "+ resultExec );
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von Pngcheck durch und
	 * gibt das Ergebnis als boolean zurueck
	 * 
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return Boolean mit Kontrollergebnis
	 */
	public static String checkPngcheck( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		String pngcheckExe = dirOfJarPath + File.separator
				+ resourcesPngcheckExe;
		File fpngcheckExe = new File( pngcheckExe );
		if ( !fpngcheckExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + pngcheckExe;
				checkFiles = false;
			} else {
				result = result + ", " + pngcheckExe;
				checkFiles = false;
			}
		}
		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
