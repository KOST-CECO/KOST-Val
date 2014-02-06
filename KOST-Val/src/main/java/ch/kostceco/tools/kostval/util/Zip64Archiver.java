/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipException;

import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.EntryOutputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0 Diese Klasse benutzt die
 *         Zip64File Library zum Komprimieren und Archivieren von Dateien,
 *         welche grösser als 4 G sein können. Es gibt momentan keine andere
 *         Software ausser pkzip, welche ein kommerzielles Produkt ist, die dazu
 *         in der Lage wäre. Bspw. führt Izarc die Archivierung zwar durch, aber
 *         erzeugt fehlerhafte Metadaten, die das Dekomprimieren dann
 *         verunmöglichen. Uebrigens werden von IZArc auch die
 *         Zeichenkodierungen nicht richtig abgehandelt.
 */

public class Zip64Archiver
{

	static byte[]	buffer	= new byte[8192];

	// Process only directories under dir
	private static void visitAllDirs( File dir, Zip64File zip64File,
			File originalDir ) throws FileNotFoundException, ZipException,
			IOException
	{
		if ( dir.isDirectory() ) {
			String sDirToCreate = dir.getAbsolutePath();

			sDirToCreate = sDirToCreate.replace( originalDir.getAbsolutePath(),
					"" );
			if ( sDirToCreate.startsWith( "/" )
					|| sDirToCreate.startsWith( "\\" ) ) {
				sDirToCreate = sDirToCreate.substring( 1 );
			}
			if ( !sDirToCreate.endsWith( "/" ) && sDirToCreate.length() > 0 ) {
				sDirToCreate = sDirToCreate + "/";
			}
			sDirToCreate = sDirToCreate.replaceAll( "\\\\", "/" );

			if ( sDirToCreate.length() > 0 ) {

				buffer = new byte[0];
				Date dateModified = new Date( dir.lastModified() );
				EntryOutputStream eos = zip64File.openEntryOutputStream(
						sDirToCreate, FileEntry.iMETHOD_STORED, dateModified );
				eos.write( buffer, 0, buffer.length );
				eos.close();

			}

			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				visitAllDirs( new File( dir, children[i] ), zip64File,
						originalDir );
			}
		}
	}

	// Process only files under dir
	private static void visitAllFiles( File dir, Zip64File zip64File,
			File originalDir ) throws FileNotFoundException, ZipException,
			IOException
	{
		if ( dir.isDirectory() ) {
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ ) {
				visitAllFiles( new File( dir, children[i] ), zip64File,
						originalDir );
			}
		} else {

			String sFileToCreate = dir.getAbsolutePath();

			sFileToCreate = sFileToCreate.replace(
					originalDir.getAbsolutePath(), "" );
			if ( sFileToCreate.startsWith( "/" )
					|| sFileToCreate.startsWith( "\\" ) ) {
				sFileToCreate = sFileToCreate.substring( 1 );
			}
			sFileToCreate = sFileToCreate.replaceAll( "\\\\", "/" );

			if ( sFileToCreate.length() > 0 ) {
				buffer = new byte[8192];
				Date dateModified = new Date( dir.lastModified() );
				FileInputStream fis = new FileInputStream( dir );
				EntryOutputStream eos = zip64File
						.openEntryOutputStream( sFileToCreate,
								FileEntry.iMETHOD_DEFLATED, dateModified );
				for ( int iRead = fis.read( buffer ); iRead >= 0; iRead = fis
						.read( buffer ) ) {
					eos.write( buffer, 0, iRead );
				}
				fis.close();
				eos.close();

			}

		}
	}

	public static void archivate( File inputDir, File outpFile )
			throws FileNotFoundException, ZipException, IOException
	{
		Zip64File zip64File = new Zip64File( outpFile );
		// create all necessary folders first
		Zip64Archiver.visitAllDirs( inputDir, zip64File, inputDir );
		// then create the file entries
		Zip64Archiver.visitAllFiles( inputDir, zip64File, inputDir );

		zip64File.close();
	}

	public static void unzip( File inputFile, File outDir )
			throws FileNotFoundException, ZipException, IOException
	{
		Zip64File zipfile = new Zip64File( inputFile );

		List<FileEntry> fileEntryList = zipfile.getListFileEntries();
		for ( FileEntry fileEntry : fileEntryList ) {

			if ( !fileEntry.isDirectory() ) {

				byte[] buffer = new byte[8192];

				// Write the file to the original position in the fs.
				EntryInputStream eis = zipfile.openEntryInputStream( fileEntry
						.getName() );

				File newFile = new File( outDir, fileEntry.getName() );
				File parent = newFile.getParentFile();
				if ( !parent.exists() ) {
					parent.mkdirs();
				}

				FileOutputStream fos = new FileOutputStream( newFile );
				for ( int iRead = eis.read( buffer ); iRead >= 0; iRead = eis
						.read( buffer ) ) {
					fos.write( buffer, 0, iRead );
				}
				fos.close();
				eis.close();
			} else {
				// Scheibe den Ordner wenn noch nicht vorhanden an den richtigen
				// Ort respektive in den richtigen Ordner der ggf angelegt
				// werden muss. Dies muss gemacht werden, damit auch leere
				// Ordner geschrieben werden.
				EntryInputStream eis = zipfile.openEntryInputStream( fileEntry
						.getName() );
				File newFolder = new File( outDir, fileEntry.getName() );
				if ( !newFolder.exists() ) {
					File parent = newFolder.getParentFile();
					if ( !parent.exists() ) {
						parent.mkdirs();
					}
					newFolder.mkdirs();
				}
				eis.close();
			}
		}
		zipfile.close();
	}

}
