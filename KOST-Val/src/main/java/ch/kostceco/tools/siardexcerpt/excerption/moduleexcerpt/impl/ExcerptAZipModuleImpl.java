/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptAZipException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptAZipModule;
import ch.kostceco.tools.kosttools.util.Util;

/** SIARD-Datei entpacken */
public class ExcerptAZipModuleImpl extends ValidationModuleImpl implements ExcerptAZipModule {

	public static String NEWLINE = System.getProperty("line.separator");

	@Override
	public boolean validate(File siardDatei, File siardDateiNew, String noString, Map<String, String> configMap,
			Locale locale) throws ExcerptAZipException {

		boolean result = true;

		String toplevelDir = siardDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf(".");
		toplevelDir = toplevelDir.substring(0, lastDotIdx);

		try {
			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */
			// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
			if (siardDateiNew.exists()) {
				Util.deleteDir(siardDateiNew);
			}
			if (!siardDateiNew.exists()) {
				siardDateiNew.mkdir();
			}

			int BUFFER = 2048;
			ZipFile zipfile = new ZipFile(siardDatei.getAbsolutePath());
			Enumeration<? extends ZipEntry> entries = zipfile.entries();

			// jeden entry durchgechen
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.getName();
				File destFile = new File(siardDateiNew, entryName);
				// System.out.println( entryName + " - " +
				// destFile.getAbsolutePath() );

				// erstelle den Ueberordner
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();
				if (!entry.isDirectory()) {
					InputStream stream = zipfile.getInputStream(entry);
					BufferedInputStream is = new BufferedInputStream(stream);
					int currentByte;

					// erstellung Buffer zum schreiben der Dateien
					byte data[] = new byte[BUFFER];

					// schreibe die aktuelle Datei an den geuenschten Ort
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
					stream.close();
				} else {
					destFile.mkdirs();
				}
			}
			zipfile.close();
		} catch (Exception e) {
			System.out.println(getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_A));
			System.out.println(getTextResourceServiceExc().getText(locale, EXC_ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return result;
	}
}
