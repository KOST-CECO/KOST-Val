/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2019 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
 * -----------------------------------------------------------------------------------------------
 * KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all copyright
 * interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March
 * 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.BufferedReader;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.io.FileReader;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.List;

import ch.enterag.utils.zip.Zip64File;
import ch.enterag.utils.zip.FileEntry;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationAzipException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationAzipModule;

/** Validierungsschritt A (Lesbarkeit) Kann die SIARD-Datei gelesen werden? valid --> lesbare und
 * nicht passwortgesch�tzte ZIP-Datei oder ZIP64-Datei valid --> unkomprimierte ZIP64-Datei oder
 * unkomprimierte ZIP-Datei, seit dem Addendum auch Deflate-Komprimierung erlaubt ==> Bei den Module
 * A, B, C und D wird die Validierung abgebrochen, sollte das Resulat invalid sein!
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationAzipModuleImpl extends ValidationModuleImpl implements ValidationAzipModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap )
			throws ValidationAzipException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "no" ) ) {
			// keine Ausgabe
		} else if ( onWork.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD ) + onWork );
			return false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "A    " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean valid = false;
		boolean validC = false;
		/* boolean store = false; boolean def = false; boolean defX = false; boolean defN = false; */

		// die Datei darf kein Directory sein
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
							+ getTextResourceService().getText( ERROR_XML_A_NOFILE ) );
			// Die zu validierende SIARD-Datei ist ein Ordner und keine ZIP-Datei.
			return false;
		}

		// Die Datei muss mit PK.. beginnen
		FileReader fr = null;
		try {
			fr = new FileReader( valDatei );
			BufferedReader read = new BufferedReader( fr );

			// Hex 03 in Char umwandeln
			String str3 = "03";
			int i3 = Integer.parseInt( str3, 16 );
			char c3 = (char) i3;
			// Hex 04 in Char umwandeln
			String str4 = "04";
			int i4 = Integer.parseInt( str4, 16 );
			char c4 = (char) i4;

			// auslesen der ersten 4 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[4];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			// die beiden charArrays (soll und ist) mit einander vergleichen
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				// h�chstwahrscheinlich ein ZIP da es mit 504B0304 respektive PK.. beginnt
				valid = true;
			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
								+ getTextResourceService().getText( ERROR_XML_A_INCORRECTFILEENDING_SIARD ) );
				// Die SIARD-Datei ist kein ZIP.
				read.close();
				// set to null
				read = null;
				return false;
			}
			read.close();
			// set to null
			read = null;
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Die byte 8 und 9 müssen 00 00 STORE / 08 00 DEFLATE sein

		/* Dies ergibt jedoch nur ein Indix darauf, wie die Dateien gezipt sind. Dies weil in einem Zip
		 * unterschiedliche Komprimierungen verwendet werden können und die erste Kopmprimierung nicht
		 * das ganze Zip abbildet. */
		FileReader fr89 = null;
		try {
			fr89 = new FileReader( valDatei );
			BufferedReader read = new BufferedReader( fr89 );

			// Hex 00 in Char umwandeln
			String str00 = "00";
			int i00 = Integer.parseInt( str00, 16 );
			char c00 = (char) i00;
			// Hex 08 in Char umwandeln
			String str08 = "08";
			int i08 = Integer.parseInt( str08, 16 );
			char c08 = (char) i08;

			// auslesen der 8-9 Zeichen der Datei

			int length;
			int i;
			char[] buffer = new char[9];
			char c8 = 0;
			char c9 = 0;
			length = read.read( buffer );
			for ( i = 8; i != length; i++ ) {
				if ( i == 8 ) {
					c8 = buffer[i];
				}
				if ( i == 9 ) {
					c9 = buffer[i];
				}
			}

			// die beiden charArrays (soll und ist) mit einander vergleichen
			char[] charArray1 = new char[] { c8, c9 };
			char[] charArray2 = new char[] { c00, c00 }; // store
			char[] charArray3 = new char[] { c08, c00 }; // def

			String hex8 = String.format( "%04x", (int) c8 );
			int dec8 = Integer.parseInt( hex8, 16 );

			if ( Arrays.equals( charArray1, charArray3 ) ) {
				// def: DEFLATED -> validC = true seit Addendum 1 durch ist
				validC = true;
			} else if ( Arrays.equals( charArray1, charArray2 ) ) {
				// h�chstwahrscheinlich ein unkomprimiertes ZIP
				validC = true;
			} else {
				validC = false;
			}

			if ( validC ) {
				// Versuche das ZIP file zu �ffnen
				// Zuerst mit Java.util.zip und dann Zip64_1.0
				try {
					Integer compressed = 1000;
					ZipFile zf = new ZipFile( valDatei.getAbsolutePath() );
					Enumeration<? extends ZipEntry> entries = zf.entries();
					while ( entries.hasMoreElements() ) {
						ZipEntry zEntry = entries.nextElement();
						compressed = zEntry.getMethod();
						// Compression method for uncompressed entries = STORED = 0
						// Compression method for deflate compression = 8
						if ( compressed == 8 ) {
							// def: DEFLATE
						} else if ( compressed == 0 ) {
							// store element
						} else {
							// weder store noch def
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
											+ getTextResourceService().getText( ERROR_XML_A_DEFLATED, compressed ) );
							zf.close();
							return false;
						}
					}
					// und wenn es klappt, gleich wieder schliessen
					zf.close();
					// set to null
					zf = null;
				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
									+ getTextResourceService().getText( ERROR_XML_A_INCORRECTZIP, e.getMessage() ) );
					read.close();
					// set to null
					read = null;
					return false;
				}
				// Versuche das ZIP file zu �ffnen
				Zip64File zfe = null;
				try {
					Integer compressed = 0;
					zfe = new Zip64File( valDatei );
					// auslesen der Komprimierungsmethode aus allen FileEntries der zip(64)-Datei
					List<FileEntry> fileEntryList = zfe.getListFileEntries();
					for ( FileEntry fileEntry : fileEntryList ) {
						compressed = fileEntry.getMethod();
						// Compression method for uncompressed entries = STORED = 0
						// Compression method for deflate compression = 8
						if ( compressed == 8 ) {
							// def: DEFLATE
						} else if ( compressed == 0 ) {
							// store element
						} else {
							// weder store noch def
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
											+ getTextResourceService().getText( ERROR_XML_A_DEFLATED, compressed ) );
							read.close();
							// set to null
							read = null;
							return false;
						}
					}
					// und wenn es klappt, gleich wieder schliessen
					zfe.close();
					// set to null
					zfe = null;
				} catch ( Exception ee ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
									+ getTextResourceService().getText( ERROR_XML_A_INCORRECTZIP, ee.getMessage() ) );
					read.close();
					// set to null
					read = null;
					return false;
				}

			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
								+ getTextResourceService().getText( ERROR_XML_A_DEFLATED, dec8 ) );
				read.close();
				// set to null
				read = null;
				return false;
			}
			read.close();
			// set to null
			read = null;
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return (valid);
	}
}
