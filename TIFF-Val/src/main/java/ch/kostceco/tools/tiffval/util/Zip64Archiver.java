/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
 

This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.tiffval.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipException;

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

}
