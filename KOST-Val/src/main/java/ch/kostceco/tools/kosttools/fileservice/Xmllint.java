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

public class Xmllint
{
	private static String	exeDir				= "resources" + File.separator
			+ "xmllint";
	private static String	pathToxmllintExe	= exeDir + File.separator
			+ "xmllint.exe";
	private static String	pathToxmllintDll1	= exeDir + File.separator
			+ "iconv.dll";
	private static String	pathToxmllintDll2	= exeDir + File.separator
			+ "libxml2.dll";
	private static String	pathToxmllintDll3	= exeDir + File.separator
			+ "zlib1.dll";

	/**
	 * fuehrt eine Validierung mit xmllint via cmd durch und gibt das Ergebnis
	 * als String zurueck
	 * 
	 * @param xmlFile
	 *            XML-Datei, welche validiert werden soll
	 * @param xsdFile
	 *            XSD-Datei, gegen welche validiert werden soll
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Validierungsergebnis ("OK" oder den Fehler.
	 */
	public static String execXmllint( File xmlFile, File xsdFile, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = false;
		File exeFile = new File(
				dirOfJarPath + File.separator + pathToxmllintExe );

		String command = "\"\"" + exeFile.getAbsolutePath() + "\""
				+ " --noout --stream --nowarning --schema " + "\""
				+ xsdFile.getAbsolutePath() + "\"" + " " + "\""
				+ xmlFile.getAbsolutePath() + "\"\"";

		String resultExec = Cmd.execToString( command, out, workDir );
		/*
		 * Folgender Error Output ist keiner sondern nur Info und kann mit OK
		 * ersetzt werden: ERROR:
		 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\schema0\
		 * table4\table4.xml validates
		 */
		String ignor = "ERROR: " + xmlFile.getAbsolutePath() + " validates";
		if ( resultExec.equals( ignor ) ) {
			resultExec = "OK";
		} else {
			/*
			 * ERROR: Schemas validity error : Element
			 * '{http://www.admin.ch/xmlns/siard/1.0/schema0/table2.xsd}row':
			 * This element is not expected.</Message><Message>ERROR:
			 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\
			 * schema0\table2\table2.xml fails to validate
			 */
			String replaceInfo = "</Message><Message>ERROR: "
					+ xmlFile.getAbsolutePath() + " fails to validate";
			resultExec = resultExec.replace( replaceInfo, "" );
		}
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle mit xmllint via cmd durch und gibt das Ergebnis als
	 * String zurueck
	 * 
	 * @param xmlFile
	 *            XML-Datei, welche kontrolliert werden soll
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis ("OK" oder den Fehler.
	 */
	public static String structXmllint( File xmlFile, File workDir,
			String dirOfJarPath ) throws InterruptedException
	{
		boolean out = false;
		File exeFile = new File(
				dirOfJarPath + File.separator + pathToxmllintExe );

		String command = "\"\"" + exeFile.getAbsolutePath() + "\""
				+ " --noout --stream " + "\"" + xmlFile.getAbsolutePath()
				+ "\"\"";

		String resultStru = Cmd.execToString( command, out, workDir );
		/*
		 * C:\Program Files
		 * (x86)\KOST-CECO\KOST-Tools\KOST-Val\resources\xmllint>xmllint.exe
		 * --noout
		 * C:\Users\clair\Downloads\SIP_20220328_KOST_eCH1.2Fxsd_1a-3d-IO\
		 * content\DOS_04\shiporder.xml
		 */
		//System.out.println( resultStru );
		String ignor = "";
		if ( resultStru.equals( ignor ) ) {
			resultStru = "OK";
		} else {
			/*
			 * ERROR: file:///C%3A/Users/clair/Downloads/SIP_20220328_KOST_eCH1.
			 * 2Fxsd_1a-3d-IO/content/DOS_04/shiporder1.xml:17: parser error :
			 * Opening and ending tag mismatch: titel line 0 and
			 * title</Message><Message>ERROR: <titel>Hide your
			 * heart</title></Message><Message>ERROR: ^</Message><Message>ERROR:
			 * C:\Users\clair\Downloads\SIP_20220328_KOST_eCH1.2Fxsd_1a-3d-IO\
			 * content\DOS_04\shiporder1.xml : failed to parse
			 */

			String replaceInfo = "</Message><Message>ERROR: "
					+ xmlFile.getAbsolutePath() + " : failed to parse";
			resultStru = resultStru.replace( replaceInfo, "" );
			String replacePath = xmlFile.getAbsolutePath() ;
			replacePath = replacePath.replace ("\\","/");
			replacePath = replacePath.replace (":/","%3A/");
			replacePath = replacePath.replace (" ","%20");
			// System.out.println( replacePath );
			resultStru = resultStru.replace( replacePath, "" );
			// System.out.println( resultStru );
			resultStru = resultStru.replace( "file:///:", "Line " );
			for ( int i = 0; i < 500; i++ ) {
				resultStru = resultStru.replace( "\t", " " );
				resultStru = resultStru.replace( "   ", " " );
				resultStru = resultStru.replace( "  ", " " );
			}
			resultStru = resultStru.replace( "</Message><Message>ERROR: ^",
					"" );
			resultStru = resultStru.replace( "<", "[" );
			resultStru = resultStru.replace( ">", "]" );
			resultStru = resultStru.replace( "[Message]", "<Message>- " );
			resultStru = resultStru.replace( "[/Message]", "</Message>" );
			// System.out.println( resultStru );
		}
		return resultStru;
	}

	/**
	 * fuehrt eine Kontrolle aller benooetigten Dateien von xmllint durch und
	 * gibt das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath
	 *            String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Validierungsergebnis ("OK" oder den Fehler.
	 */
	public static String checkXmllint( String dirOfJarPath )
	{
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fpathToxmllintExe = new File(
				dirOfJarPath + File.separator + pathToxmllintExe );
		File fpathToxmllintDll1 = new File(
				dirOfJarPath + File.separator + pathToxmllintDll1 );
		File fpathToxmllintDll2 = new File(
				dirOfJarPath + File.separator + pathToxmllintDll2 );
		File fpathToxmllintDll3 = new File(
				dirOfJarPath + File.separator + pathToxmllintDll3 );
		if ( !fpathToxmllintExe.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + pathToxmllintExe;
				checkFiles = false;
			} else {
				result = result + ", " + pathToxmllintExe;
				checkFiles = false;
			}
		}
		if ( !fpathToxmllintDll1.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + pathToxmllintDll1;
				checkFiles = false;
			} else {
				result = result + ", " + pathToxmllintDll1;
				checkFiles = false;
			}
		}
		if ( !fpathToxmllintDll2.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + pathToxmllintDll2;
				checkFiles = false;
			} else {
				result = result + ", " + pathToxmllintDll2;
				checkFiles = false;
			}
		}
		if ( !fpathToxmllintDll3.exists() ) {
			if ( checkFiles ) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + pathToxmllintDll3;
				checkFiles = false;
			} else {
				result = result + ", " + pathToxmllintDll3;
				checkFiles = false;
			}
		}

		if ( checkFiles ) {
			result = "OK";
		}
		return result;
	}

}
