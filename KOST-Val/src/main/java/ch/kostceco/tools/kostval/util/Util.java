/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ch.kostceco.tools.kostval.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Util
{
	static PrintStream				original;
	static String							originalPath;
	static Map<String, File>	fileMap	= new HashMap<String, File>();

	/** Schaltet die Konsolen-Ausgabe aus durch Umleitung in ein Null-Device. */
	public static void switchOffConsole()
	{
		// Keep a copy of the original out stream.
		original = new PrintStream( System.out );

		// replace the System.out, redirect to NUL
		try {
			System.setOut( new PrintStream( new FileOutputStream( "NUL:" ) ) );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}

	/** Schaltet die Konsolen-Ausgabe in ein file um. */
	public static void switchOffConsoleToTxt( File file ) throws FileNotFoundException
	{
		// Keep a copy of the original out stream.
		original = new PrintStream( System.out );

		// replace the System.out, redirect to file
		try {
			FileOutputStream fos = new FileOutputStream( file );
			PrintStream ps = new PrintStream( fos );
			System.setOut( ps );
			fos.close();
			ps.close();
			// set to null
			fos = null;
			ps = null;
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/** Schaltet die Konsolen-Ausgabe in ein file um und beendet den Stream, damit dieser geloescht
	 * werden kann. */
	public static void switchOffConsoleToTxtClose( File file ) throws FileNotFoundException
	{
		// Keep a copy of the original out stream.
		original = new PrintStream( System.out );

		// replace the System.out, redirect to file
		try {
			FileOutputStream fos = new FileOutputStream( file );
			PrintStream ps = new PrintStream( fos );
			System.setOut( ps );
			fos.close();
			ps.close();
			// set to null
			fos = null;
			ps = null;
		} catch ( FileNotFoundException e ) {
			// e.printStackTrace();
		} catch ( IOException e ) {
			// e.printStackTrace();
		}
	}

	/** Schaltet die mit switchOffConsole ausgeschaltete Konsole wieder ein. */
	public static void switchOnConsole()
	{
		System.setOut( original );
	}

	/** Loescht ein Verzeichnis rekursiv.
	 * 
	 * @param dir
	 *          das zu loeschende Verzeichnis
	 * @return true wenn alle Files und Verzeichnisse geloescht werden konnten */
	public static boolean deleteDir( File dir )
	{
		if ( dir.exists() ) {
			if ( dir.isDirectory() ) {
				String[] children = dir.list();
				for ( int i = 0; i < children.length; i++ ) {
					boolean success = deleteFile( new File( dir, children[i] ) );
					if ( !success ) {
						// return false;
						dir.deleteOnExit();
					}
				}
			}
			dir.delete();
			if ( !dir.delete() ) {
				dir.deleteOnExit();
			}
			// The directory is now empty so delete it
			return dir.delete();
		} else {
			// existiert nicht, im Vorfeld geloescht, true
			return true;
		}
	}

	public static boolean deleteFile( File file )
	{
		if ( file.exists() ) {
			if ( file.isDirectory() ) {
				String[] children = file.list();
				for ( int i = 0; i < children.length; i++ ) {
					boolean success = deleteFile( new File( file, children[i] ) );
					if ( !success ) {
						// return false;
						file.deleteOnExit();
					}
				}
			} else {
				file.delete();
				if ( !file.delete() ) {
					file.deleteOnExit();
				}
			}
			return file.delete();
		} else {
			// existiert nicht, im Vorfeld geloescht, true
			return true;
		}
	}

	/** Kontrolliert ob text in einer Linie im File vorkommt */
	public static boolean stringInFile( String text, File file )
	{
		try {
			Scanner scanner = new Scanner( file );
			while ( scanner.hasNextLine() ) {
				String line = scanner.nextLine();
				if ( line.contains( text ) ) {
					scanner.close();
					return true;
				}
			}
			scanner.close();
			return false;
		} catch ( FileNotFoundException e ) {
			return false;
		}
	}

	public static Map<String, File> getContent( File dir, HashMap<String, File> fileMap )
	{
		String filePath = dir.getPath();
		String content = "temp_KOST-Val" + File.separator + "SIARD" + File.separator + "content";
		if ( filePath.equalsIgnoreCase( content ) ) {
			// kein Eintrag in die Map
		} else {
			fileMap.put( filePath, dir );
		}
		if ( dir.isDirectory() ) {
			String[] subNote = dir.list();
			for ( String filename : subNote ) {
				getContent( new File( dir, filename ), fileMap );
			}
		}
		return fileMap;
	}

	public static Map<String, File> getFileMap( File dir, boolean nurPrimaerDateien )
	{
		originalPath = dir.getAbsolutePath();
		fileMap = new HashMap<String, File>();
		visitAllDirsAndFiles( dir, nurPrimaerDateien );
		return fileMap;
	}

	// Process all files and directories under dir
	public static void visitAllDirsAndFiles( File dir, boolean nurPrimaerDateien )
	{

		String filePath = dir.getAbsolutePath();
		filePath = filePath.replace( originalPath, "" );

		if ( filePath.length() > 0 ) {
			filePath = filePath.replaceAll( "\\\\", "/" );
			if ( filePath.startsWith( "/" ) ) {
				filePath = filePath.substring( 1 );
			}
			if ( dir.isDirectory() && !filePath.endsWith( "/" ) ) {
				filePath += "/";
			}

			if ( nurPrimaerDateien ) {
				if ( filePath.contains( "content/" ) && !dir.isDirectory() ) {
					fileMap.put( filePath, dir );
				}

			} else {
				fileMap.put( filePath, dir );
			}

		}

		if ( dir.isDirectory() ) {
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				visitAllDirsAndFiles( new File( dir, children[i] ), nurPrimaerDateien );
			}
		}
	}

	public static Map<String, File> getFileMapFile( File dir )
	{
		originalPath = dir.getAbsolutePath();
		fileMap = new HashMap<String, File>();
		visitAllDirsAndFilesStoreFiles( dir );
		return fileMap;
	}

	// Process all files and directories under dir aber nur Files speichern
	public static void visitAllDirsAndFilesStoreFiles( File dir )
	{

		String filePath = dir.getAbsolutePath();
		filePath = filePath.replace( originalPath, "" );

		if ( filePath.length() > 0 ) {
			filePath = filePath.replaceAll( "\\\\", "/" );
			if ( filePath.startsWith( "/" ) ) {
				filePath = filePath.substring( 1 );
			}
			if ( dir.isDirectory() && !filePath.endsWith( "/" ) ) {
				filePath += "/";
			}
			File file = new File( filePath );

			if ( file.isDirectory() ) {
				// filePath nicht in Map schreiben
			} else {
				fileMap.put( filePath, dir );
			}

		}

		if ( dir.isDirectory() ) {
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				visitAllDirsAndFilesStoreFiles( new File( dir, children[i] ) );
			}
		}
	}

	/** Kopiert ein Verzeichnis.
	 * 
	 * @param quelle
	 *          das zu kopierende Verzeichnis
	 * @param ziel
	 *          das Ziel-Verzeichnis */
	public static void copyDir( File quelle, File ziel ) throws FileNotFoundException, IOException
	{

		File[] files = quelle.listFiles();
		File newFile = null;
		// in diesem Objekt wird fuer jedes File der Zielpfad gespeichert.
		// 1. Der alte Zielpfad
		// 2. Das systemspezifische Pfadtrennungszeichen
		// 3. Der Name des aktuellen Ordners/der aktuellen Datei
		ziel.mkdirs(); // erstellt alle benoetigten Ordner
		if ( files != null ) {
			for ( int i = 0; i < files.length; i++ ) {
				newFile = new File(
						ziel.getAbsolutePath() + System.getProperty( "file.separator" ) + files[i].getName() );
				if ( files[i].isDirectory() ) {
					copyDir( files[i], newFile );
				} else {
					copyFile( files[i], newFile );
				}
			}
		}
	}

	/** Kopiert eine Datei.
	 * 
	 * @param file
	 *          die zu kopierende Datei
	 * @param ziel
	 *          die Ziel-Datei */
	public static void copyFile( File file, File ziel ) throws FileNotFoundException, IOException
	{
		if ( ziel.exists() ) {
			// Datei loeschen ansonsten wird Text hinzugefuegt
			ziel.delete();
		}
		BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );
		BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( ziel, true ) );
		int bytes = 0;
		while ( (bytes = in.read()) != -1 ) { // Datei einlesen
			out.write( bytes ); // Datei schreiben
		}
		in.close();
		out.close();
		// set to null
		in = null;
		out = null;
	}

	/* TODO: Wichtige Notiz zur Performance
	 * 
	 * Statt
	 * 
	 * while ( (line = reader.readLine()) != null ) { oldtext += line + "\r\n"; }
	 * 
	 * Soll
	 * 
	 * StringBuilder sb = new StringBuilder(); while ( (line = reader.readLine()) != null ) {
	 * sb.append( line ); sb.append( "\r\n" ); } oldtext= sb.toString();
	 * 
	 * verwendet werden. Dies insbesondere bei grossem Text massiv schneller. Da bei diesen
	 * Ersetzungen meist der Output gelesen wird, kann dieser natuerlich gross sein. */

	/** Veraendert [& mit &amp;], [ '<' mit '&lt;' ] sowie [ '>' mit '&gt;' ] und ergaenzt das
	 * XML-Element "<End></End>" mit dem ergebnis (stringEnd) sowie <Message>3c</Message></Error> mit
	 * dem ergebnis (string3c) in dem kost-val.log.xml (file)
	 * 
	 * ! Solche Ersetzungen duerfen nicht in einer Schleife gemacht werden sondern erst am Schluss, da
	 * diese sehr Zeitintensiv sind !!!
	 * 
	 * @throws IOException
	 */
	public static void valEnd3cAmp( String string3c, File file ) throws IOException
	{
		try {
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			String line = "", oldtext = "";
			StringBuilder sb = new StringBuilder();
			while ( (line = reader.readLine()) != null ) {
				sb.append( line );
				sb.append( "\r\n" );
			}
			// Zeitstempel End
			java.util.Date nowEnd = new java.util.Date();
			java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeEnd = sdfEnd.format( nowEnd );
			ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
			String stringEnd = ausgabeEnd;
			oldtext = sb.toString();
			reader.close();
			// set to null
			reader = null;
			String newtext = oldtext.replace( "<End></End>", stringEnd );
			newtext = newtext.replace( "<Message>3c</Message></Error>", string3c );
			newtext = newtext.replace( "&", "&amp;" );
			newtext = newtext.replace( "<http", "&lt;http" );
			newtext = newtext.replace( "'<'", "'&lt;'" );
			newtext = newtext.replace( "'>'", "'&gt;'" );
			newtext = newtext.replace( "<<", "<" );
			newtext = newtext.replace( "& ", "&amp; " );
			newtext = newtext.replace( (char) 0, (char) 32 );
			FileWriter writer = new FileWriter( file );
			writer.write( newtext );
			writer.close();
			// set to null
			writer = null;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	/** Ergaenzt "Validierung: SIP" mit der Version (string) in dem kost-val.log.xml (file)
	 * 
	 * ! Solche Ersetzungen duerfen nicht in einer Schleife gemacht werden sondern erst am Schluss, da
	 * diese sehr Zeitintensiv sind !!!
	 * 
	 * @throws IOException
	 */
	public static void valSipversion( String string, File file ) throws IOException
	{
		try {
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			String line = "", oldtext = "";
			StringBuilder sb = new StringBuilder();
			while ( (line = reader.readLine()) != null ) {
				sb.append( line );
				sb.append( "\r\n" );
			}
			oldtext = sb.toString();
			reader.close();
			// set to null
			reader = null;
			string = "Validierung: SIP" + string;
			String newtext = oldtext.replace( "Validierung: SIP", string );
			newtext = newtext.replace( (char) 0, (char) 32 );
			FileWriter writer = new FileWriter( file );
			writer.write( newtext );
			writer.close();
			// set to null
			writer = null;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	/** Veraendert ersetzt oldstring mit newstring in file
	 * 
	 * ! Solche Ersetzungen duerfen nicht in einer Schleife gemacht werden sondern erst am Schluss, da
	 * diese sehr Zeitintensiv sind !!!
	 * 
	 * @throws IOException
	 */
	public static void oldnewstring( String oldstring, String newstring, File file )
			throws IOException
	{
		try {
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			String line = "", oldtext = "";
			StringBuilder sb = new StringBuilder();
			while ( (line = reader.readLine()) != null ) {
				sb.append( line );
				sb.append( "\r\n" );
			}
			oldtext = sb.toString();
			reader.close();
			// set to null
			reader = null;
			String newtext = oldtext.replace( oldstring, newstring );
			newtext = newtext.replace( (char) 0, (char) 32 );
			FileWriter writer = new FileWriter( file );
			writer.write( newtext );
			writer.close();
			// set to null
			writer = null;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

}
