/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2019 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptAZipException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptAZipModule;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** SIARD-Datei entpacken */
public class ExcerptAZipModuleImpl extends ValidationModuleImpl implements ExcerptAZipModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	@Override
	public boolean validate( File siardDatei, File siardDateiNew, String noString,
			Map<String, String> configMap, Locale locale ) throws ExcerptAZipException
	{

		boolean result = true;

		String toplevelDir = siardDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		try {
			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */
			// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
			if ( siardDateiNew.exists() ) {
				Util.deleteDir( siardDateiNew );
			}
			if ( !siardDateiNew.exists() ) {
				siardDateiNew.mkdir();
			}

			FileInputStream fis = null;
			ZipInputStream zipfile = null;
			ZipEntry zEntry = null;
			fis = new FileInputStream( siardDatei );
			zipfile = new ZipInputStream( new BufferedInputStream( fis ) );

			while ( (zEntry = zipfile.getNextEntry()) != null ) {
				try {
					if ( !zEntry.isDirectory() ) {
						byte[] tmp = new byte[4 * 1024];
						FileOutputStream fos = null;
						String opFilePath = siardDateiNew + File.separator + zEntry.getName();
						File newFile = new File( opFilePath );
						File parent = newFile.getParentFile();
						if ( !parent.exists() ) {
							parent.mkdirs();
						}
						// System.out.println( "Extracting file to " + newFile.getAbsolutePath() );
						fos = new FileOutputStream( opFilePath );
						int size = 0;
						while ( (size = zipfile.read( tmp )) != -1 ) {
							fos.write( tmp, 0, size );
						}
						fos.flush();
						fos.close();
					} else {
						/* Scheibe den Ordner wenn noch nicht vorhanden an den richtigen Ort respektive in den
						 * richtigen Ordner der ggf angelegt werden muss. Dies muss gemacht werden, damit auch
						 * leere Ordner ins Work geschrieben werden. Diese werden danach im J als Fehler
						 * angegeben */
						File newFolder = new File( siardDateiNew, zEntry.getName() );
						if ( !newFolder.exists() ) {
							File parent = newFolder.getParentFile();
							if ( !parent.exists() ) {
								parent.mkdirs();
							}
							newFolder.mkdirs();
						}
					}
				} catch ( IOException e ) {
					System.out.println( e.getMessage() );
				}
			}

			zipfile.close();
		} catch ( Exception e ) {
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_A )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}
}
