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

package ch.kostceco.tools.kosttools.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0 Diese Klasse benutzt die
 *         Zip64File Library zum Komprimieren und Archivieren von Dateien,
 *         welche groesser als 4 G sein koennen.
 */

public class Zip64Archiver {

	static byte[] buffer = new byte[8192];

	/**
	 * Unzip it (zip64)
	 * 
	 * @param zipFile input zip file
	 * @param output  zip file output folder
	 */
	public static void unzip64(File inputFile, File outDir) throws FileNotFoundException, ZipException, IOException {
		// TODO verwendet bei Controllervalsip
		Zip64File zipfile = new Zip64File(inputFile);

		List<FileEntry> fileEntryList = zipfile.getListFileEntries();
		for (FileEntry fileEntry : fileEntryList) {

			if (!fileEntry.isDirectory()) {

				byte[] buffer = new byte[8192];

				// Write the file to the original position in the fs.
				EntryInputStream eis = zipfile.openEntryInputStream(fileEntry.getName());

				File newFile = new File(outDir, fileEntry.getName());
				File parent = newFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(newFile);
				for (int iRead = eis.read(buffer); iRead >= 0; iRead = eis.read(buffer)) {
					fos.write(buffer, 0, iRead);
				}
				fos.close();
				eis.close();
			} else {
				/*
				 * Scheibe den Ordner wenn noch nicht vorhanden an den richtigen Ort respektive
				 * in den richtigen Ordner der ggf angelegt werden muss. Dies muss gemacht
				 * werden, damit auch leere Ordner geschrieben werden.
				 */
				EntryInputStream eis = zipfile.openEntryInputStream(fileEntry.getName());
				File newFolder = new File(outDir, fileEntry.getName());
				if (!newFolder.exists()) {
					File parent = newFolder.getParentFile();
					if (!parent.exists()) {
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