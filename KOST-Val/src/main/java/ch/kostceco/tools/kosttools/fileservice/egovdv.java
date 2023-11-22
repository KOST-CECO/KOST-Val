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

public class egovdv
{
	private static String	exeDir		= ".." + File.separator
			+ "egov-validationclient-cli";
	private static String	validateBat	= exeDir + File.separator
			+ "validate.bat";
	private static String	egovCli		= exeDir + File.separator + "lib"
			+ File.separator + "intarsys-egov-validationclient-cli-1.0.10.jar";

	/**
	 * Listet mit egovdv via cmd die Signaturnamen in pdf auf und speichert das
	 * Ergebnis in ein File (Output). Gibt zurueck ob Output existiert oder
	 * nicht
	 * 
	 * Fuer diesen Schritt braucht es weder Internet/URL noch einen account
	 * 
	 * validate -list -f <filename> -l de -u no
	 * 
	 * @param -list
	 *            List digital signatures of given PDF file
	 * @param -f
	 *            file to validate
	 * @param -l
	 *            get pdf report in the given language, supported codes: de, fr,
	 *            it, en. This is an optional parameter, if omitted de is used.
	 * @param -u
	 *            URL of the validation webservice. (Can also be defined in
	 *            config file)
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execEgovdvList( File fileToList, File output,
			File workDir, String dirOfJarPath ) throws InterruptedException
	{
		boolean out = true;
		File fvalidateBat = new File(
				dirOfJarPath + File.separator + validateBat );
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if ( output.exists() ) {
			output.delete();
		}

		// validate -list -f <filename> -l de -u no

		String command = "\"\"" + fvalidateBat.getAbsolutePath() + "\" "
				+ "-list -f \"" + fileToList.getAbsolutePath()
				+ "\" -l de -u no > \"" + output.getAbsolutePath() + "\"\"";

		// System.out.println( "" );
		// System.out.println( "command: " + command );

		String resultExec = Cmd.execToStringSplit( command, out, workDir );
		// System.out.println( "resultExec: " + resultExec );

		// egovdv gibt keine Info raus, die replaced oder ignoriert werden muss

		if ( resultExec.equals( "OK" ) ) {
			if ( output.exists() ) {
				// alles io bleibt bei OK
			} else {
				// Datei nicht angelegt...
				resultExec = "NoReport";
			}
		}
		// System.out.println( "resultExec= " +resultExec );
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von egovdv durch und gibt
	 * das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkEgovdv( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fvalidateBat = new File(
				dirOfJarPath + File.separator + validateBat );
		File fegovCli = new File( dirOfJarPath + File.separator + egovCli );

		if ( !fvalidateBat.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + validateBat;
				checkFiles = false;
			} else {
				result = result + ", " + validateBat;
				checkFiles = false;
			}
		}
		if ( !fegovCli.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + egovCli;
				checkFiles = false;
			} else {
				result = result + ", " + egovCli;
				checkFiles = false;
			}
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}
}
