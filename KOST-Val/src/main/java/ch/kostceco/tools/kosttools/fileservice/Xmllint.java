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
import java.util.Locale;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.Util;

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
			String dirOfJarPath, Locale locale ) throws InterruptedException
	{
		boolean out = false;
		File exeFile = new File(
				dirOfJarPath + File.separator + pathToxmllintExe );

		// xmllint unterstuetzt nicht alle Zeichen resp Doppelleerschlag
		File xmlFileNormalisiert = new File(
				workDir + File.separator + "XML.xml" );
		File xsdFileNormalisiert = new File(
				workDir + File.separator + xsdFile.getName() );
		try {
			Util.copyFile( xmlFile, xmlFileNormalisiert );
			Util.copyFile( xsdFile, xsdFileNormalisiert );
		} catch ( IOException e ) {
			// Normalisierung fehlgeschlagen es wird ohne versucht
			xmlFileNormalisiert = xmlFile;
			xsdFileNormalisiert = xsdFile;
		}
		if ( !xmlFileNormalisiert.exists() ) {
			xmlFileNormalisiert = xmlFile;
			xsdFileNormalisiert = xsdFile;
		}

		String command = "\"\"" + exeFile.getAbsolutePath() + "\""
				+ " --noout --stream --nowarning --schema " + "\""
				+ xsdFileNormalisiert.getAbsolutePath() + "\"" + " " + "\""
				+ xmlFileNormalisiert.getAbsolutePath() + "\"\"";

		String resultExec = Cmd.execToStringSplit( command, out, workDir );
		/*
		 * Folgender Error Output ist keiner sondern nur Info und kann mit OK
		 * ersetzt werden: ERROR:
		 * C:\Users\X60014195\.kost-val_2x\temp_KOST-Val\SIARD\content\schema0\
		 * table4\table4.xml validates
		 */
		String ignor = "ERROR: " + xmlFileNormalisiert.getAbsolutePath()
				+ " validates";
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
					+ xmlFileNormalisiert.getAbsolutePath()
					+ " fails to validate";
			resultExec = resultExec.replace( replaceInfo, "" );
			if ( locale.toString().startsWith( "fr" ) ) {
				resultExec = resultExec.replace( "Schemas validity error :",
						"Erreur de validite des schemas :" );
				resultExec = resultExec.replace( "This element is not expected",
						"Cet element n`est pas attendu" );
				resultExec = resultExec.replace( "Expected is",
						"L`element attendu est" );
				resultExec = resultExec.replace( "fails to validate",
						"ne parvient pas a etre valide" );
				resultExec = resultExec.replace( "Missing child element(s).",
						"Elements enfants manquants." );
				resultExec = resultExec.replace( "The value has a length of",
						"La valeur a une longueur de" );
				resultExec = resultExec.replace(
						"this underruns the allowed minimum length of",
						"ce qui est inferieur a la longueur minimale autorisee de" );
				resultExec = resultExec.replace(
						"is not a valid value of the atomic type",
						"n`est pas une valeur valide du type atomique" );
			} else if ( locale.toString().startsWith( "de" ) ) {
				resultExec = resultExec.replace( "Schemas validity error :",
						"Fehler bei der Gueltigkeit des Schemas:" );
				resultExec = resultExec.replace( "This element is not expected",
						"Dieses Element wird nicht erwartet" );
				resultExec = resultExec.replace( "Expected is",
						"Erwartet wird" );
				resultExec = resultExec.replace( "fails to validate",
						"kann nicht validiert werden" );
				resultExec = resultExec.replace( "Missing child element(s).",
						"Fehlende untergeordnete Elemente." );
				resultExec = resultExec.replace( "The value has a length of",
						"Der Wert hat eine Laenge von" );
				resultExec = resultExec.replace(
						"this underruns the allowed minimum length of",
						"und unterschreitet damit die zulaessige Mindestlaenge von" );
				resultExec = resultExec.replace(
						"is not a valid value of the atomic type",
						"ist kein gueltiger Wert des atomaren Typs" );
			} else if ( locale.toString().startsWith( "it" ) ) {
				resultExec = resultExec.replace( "Schemas validity error :",
						"Errore nella validita dello schema:" );
				resultExec = resultExec.replace( "This element is not expected",
						"Questo elemento non e previsto" );
				resultExec = resultExec.replace( "Expected is", "Previsto" );
				resultExec = resultExec.replace( "fails to validate",
						"non puo essere validato" );
				resultExec = resultExec.replace( "Missing child element(s).",
						"Elementi subordinati mancanti." );
				resultExec = resultExec.replace( "The value has a length of",
						"Il valore ha una lunghezza di" );
				resultExec = resultExec.replace(
						"this underruns the allowed minimum length of",
						"e quindi e al di sotto della lunghezza minima consentita di" );
				resultExec = resultExec.replace(
						"is not a valid value of the atomic type",
						"non e un valore valido del tipo atomico" );
			}
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
			String dirOfJarPath, Locale locale ) throws InterruptedException
	{
		boolean out = false;
		File exeFile = new File(
				dirOfJarPath + File.separator + pathToxmllintExe );

		// xmllint unterstuetzt nicht alle Zeichen resp Doppelleerschlag
		File xmlFileNormalisiert = new File(
				workDir + File.separator + "XML.xml" );
		try {
			Util.copyFile( xmlFile, xmlFileNormalisiert );
		} catch ( IOException e ) {
			// Normalisierung fehlgeschlagen es wird ohne versucht
			xmlFileNormalisiert = xmlFile;
		}
		if ( !xmlFileNormalisiert.exists() ) {
			xmlFileNormalisiert = xmlFile;
		}

		String command = "\"\"" + exeFile.getAbsolutePath() + "\""
				+ " --noout --stream " + "\""
				+ xmlFileNormalisiert.getAbsolutePath() + "\"\"";

		String resultStru = Cmd.execToStringSplit( command, out, workDir );
		/*
		 * C:\Program Files
		 * (x86)\KOST-CECO\KOST-Tools\KOST-Val\resources\xmllint>xmllint.exe
		 * --noout
		 * C:\Users\clair\Downloads\SIP_20220328_KOST_eCH1.2Fxsd_1a-3d-IO\
		 * content\DOS_04\shiporder.xml
		 */
		// System.out.println( resultStru );
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
					+ xmlFileNormalisiert.getAbsolutePath()
					+ " : failed to parse";
			resultStru = resultStru.replace( replaceInfo, "" );
			String replacePath = xmlFileNormalisiert.getAbsolutePath();
			replacePath = replacePath.replace( "\\", "/" );
			replacePath = replacePath.replace( ":/", "%3A/" );
			replacePath = replacePath.replace( " ", "%20" );
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

			if ( locale.toString().startsWith( "fr" ) ) {
				resultStru = resultStru.replace( "parser error :",
						"erreur d'analyse syntaxique :" );
				resultStru = resultStru.replace(
						"Opening and ending tag mismatch:",
						"Mismatch des balises d'ouverture et de fin :" );
				resultStru = resultStru.replace( " and ", " et " );
			} else if ( locale.toString().startsWith( "de" ) ) {
				resultStru = resultStru.replace( "parser error :",
						"Parser-Fehler:" );
				resultStru = resultStru.replace(
						"Opening and ending tag mismatch:",
						"Anfangs- und End-Tag stimmen nicht überein:" );
				resultStru = resultStru.replace( " and ", " und " );
			}
		}
		return resultStru;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von xmllint durch und
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
