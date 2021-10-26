/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO. 2012-2021
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

public class Xmllint
{

	/** fuehrt eine Validierung mit xmllint via cmd durch und gibt das Ergebnis als String zurueck
	 * 
	 * @param xmlFile
	 *          XML-Datei, welche validiert werden soll
	 * @param xsdFile
	 *          XSD-Datei, gegen welche validiert werden soll
	 * @param workDir
	 *          Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *          String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Validierungsergebnis ("OK" oder den Fehler. */
	public static String execXmllint( File xmlFile, File xsdFile, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out=false;
		File exeFile = new File( dirOfJarPath + File.separator + "resources" + File.separator
				+ "xmllint" + File.separator + "xmllint.exe" );

		String command = "\"\"" + exeFile.getAbsolutePath() + "\""
				+ " --noout --stream --nowarning --schema " + "\"" + xsdFile.getAbsolutePath() + "\"" + " "
				+ "\"" + xmlFile.getAbsolutePath() + "\"\"";

		String resultExec = Cmd.execToString( command, out, workDir );
		/* Folgender Error Output ist keiner sondern nur Info und kann mit OK ersetzt werden: ERROR:
		 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\schema0\table4\table4.xml
		 * validates */
		String ignor = "ERROR: " + xmlFile.getAbsolutePath() + " validates";
		if ( resultExec.equals( ignor ) ) {
			resultExec = "OK";
		} else {
			/* ERROR: Schemas validity error : Element
			 * '{http://www.admin.ch/xmlns/siard/1.0/schema0/table2.xsd}row': This element is not
			 * expected.</Message><Message>ERROR:
			 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\schema0\table2\table2.xml fails
			 * to validate */
			String replaceInfo = "</Message><Message>ERROR: " + xmlFile.getAbsolutePath()
					+ " fails to validate";
			resultExec = resultExec.replace( replaceInfo, "" );
		}
		return resultExec;
	}

	/** fuehrt eine Kontrolle aller benooetigten Dateien von xmllint durch und gibt das Ergebnis als
	 * String zurueck
	 * 
	 * @param dirOfJarPath
	 *          String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Validierungsergebnis ("OK" oder den Fehler. */
	public static String checkXmllint( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm xmllint existiert die Dateien?

		// File file = new File( locationOfJarPath );
		// File dirOfJarPath = file.getParent();

		String pathToxmllintExe = dirOfJarPath + File.separator + "resources" + File.separator
				+ "xmllint" + File.separator + "xmllint.exe";
		String pathToxmllintDll1 = dirOfJarPath + File.separator + "resources" + File.separator
				+ "xmllint" + File.separator + "iconv.dll";
		String pathToxmllintDll2 = dirOfJarPath + File.separator + "resources" + File.separator
				+ "xmllint" + File.separator + "libxml2.dll";
		String pathToxmllintDll3 = dirOfJarPath + File.separator + "resources" + File.separator
				+ "xmllint" + File.separator + "zlib1.dll";

		File fpathToxmllintExe = new File( pathToxmllintExe );
		File fpathToxmllintDll1 = new File( pathToxmllintDll1 );
		File fpathToxmllintDll2 = new File( pathToxmllintDll2 );
		File fpathToxmllintDll3 = new File( pathToxmllintDll3 );
		if ( !fpathToxmllintExe.exists() ) {
			result = result + " " + fpathToxmllintExe.getName();
			checkFiles = false;
		} else if ( !fpathToxmllintDll1.exists() ) {
			result = result + " " + fpathToxmllintDll1.getName();
			checkFiles = false;
		} else if ( !fpathToxmllintDll2.exists() ) {
			result = result + " " + fpathToxmllintDll2.getName();
			checkFiles = false;
		} else if ( !fpathToxmllintDll3.exists() ) {
			result = result + " " + fpathToxmllintDll3.getName();
			checkFiles = false;
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}

}
