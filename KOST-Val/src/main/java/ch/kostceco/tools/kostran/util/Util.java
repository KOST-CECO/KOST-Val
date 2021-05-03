/* == KOST-Ran ==================================================================================
 * The KOST-Ran application is used for excerpt a random sample with metadata from a SIARD-File.
 * Copyright (C) 2021 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * KOST-Ran is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostran.util;

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

import ch.kostceco.tools.kostran.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Util
{
	static PrintStream				original;
	static String							originalPath;
	static Map<String, File>	fileMap	= new HashMap<String, File>();

	/** Loescht ein Verzeichnis rekursiv.
	 * 
	 * @param dir
	 *          das zu loeschende Verzeichnis
	 * @return true wenn alle Files und Verzeichnisse geloescht werden konnten */
	public static boolean deleteDir( File dir )
	{
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
	}

	/** Loescht ein Verzeichnis rekursiv.
	 * 
	 * @param dir
	 *          das zu loeschende Verzeichnis
	 * @return true wenn alle Files und Verzeichnisse geloescht werden konnten */
	public static boolean deleteDirWithoutOnExit( File dir )
	{
		if ( dir.isDirectory() ) {
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				deleteFileWithoutOnExit( new File( dir, children[i] ) );
			}
		}
		if ( dir.isDirectory() ) {
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				deleteFileWithoutOnExit( new File( dir, children[i] ) );
			}
		}
		dir.delete();
		// The directory is now empty so delete it
		return dir.delete();
	}

	public static boolean deleteFileWithoutOnExit( File file )
	{
		if ( file.isDirectory() ) {
			String[] children = file.list();
			for ( int i = 0; i < children.length; i++ ) {
				deleteFileWithoutOnExit( new File( file, children[i] ) );
			}
			file.delete();
		} else {
			file.delete();
		}
		if (file.exists()) {
			try {
				replaceAllChar(file, "");
			} catch ( IOException e ) {
				// ein Versuch wars wert
			}
		}
		return file.delete();
	}

	public static boolean deleteFile( File file )
	{
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
		if (ziel.exists()) {
				deleteFileWithoutOnExit(ziel);
		}

		BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );
		BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( ziel, true ) );
		int bytes = 0;
		while ( (bytes = in.read()) != -1 ) { // Datei einlesen
			out.write( bytes ); // Datei schreiben
		}
		out.close();
		in.close();
	}

	/** Kontrolliert ob String existiert in file
	 * 
	 * Solche Sachen duerfen nicht in einer Schleife gemacht werden, da diese sehr Zeitintensiv sind!
	 * 
	 * @return true wenn String im File vorhanden @throws IOException */
	public static boolean stringInFile( String string, File file ) throws IOException
	{
		boolean stringInFile = false;
		try {
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			String line = "";
			while ( (line = reader.readLine()) != null ) {
				if ( line.contains( string ) ) {
					stringInFile = true;
				}
			}
			reader.close();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
		return stringInFile;
	}

	/** ersetzt alle Zeichen mit ""
	 * 
	 * @throws IOException
	 */
	public static void replaceAllChar( File file, String newString ) throws IOException
	{
		try {
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			reader.close();
			FileWriter writer = new FileWriter( file );
			writer.write( newString );
			writer.close();
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
			String newtext = oldtext.replace( oldstring, newstring );
			newtext = newtext.replace( (char) 0, (char) 32 );
			FileWriter writer = new FileWriter( file );
			writer.write( newtext );
			writer.close();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

}
