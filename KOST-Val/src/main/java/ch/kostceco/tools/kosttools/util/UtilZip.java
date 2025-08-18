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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class UtilZip {

	public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
		try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(zipFile))) {
			zipOut.setUseZip64(Zip64Mode.AsNeeded);
			zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
			addDirectoryToZip(zipOut, sourceDir, sourceDir.getAbsolutePath());
		}
	}

	private static void addDirectoryToZip(ZipArchiveOutputStream zipOut, File currentFile, String basePath)
			throws IOException {
		String relativePath = currentFile.getAbsolutePath().substring(basePath.length()).replace("\\", "/");

		if (currentFile.isDirectory()) {
			if (!relativePath.isEmpty()) {
				if (!relativePath.endsWith("/")) {
					relativePath += "/";
				}

				// Create a DEFLATED empty directory entry
				byte[] emptyData = new byte[0];
				CRC32 crc32 = new CRC32();
				crc32.update(emptyData);

				ZipArchiveEntry dirEntry = new ZipArchiveEntry(currentFile, relativePath);
				dirEntry.setMethod(ZipArchiveEntry.DEFLATED);
				dirEntry.setSize(0);
				dirEntry.setCompressedSize(0); // May be omitted for DEFLATED
				dirEntry.setCrc(crc32.getValue());

				zipOut.putArchiveEntry(dirEntry);
				zipOut.closeArchiveEntry();
			}

			File[] children = currentFile.listFiles();
			if (children != null) {
				for (File child : children) {
					addDirectoryToZip(zipOut, child, basePath);
				}
			}
		} else {
			String entryName = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

			// Load file into memory
			byte[] fileData = readFileToByteArray(currentFile);

			CRC32 crc32 = new CRC32();
			crc32.update(fileData);

			ZipArchiveEntry entry = new ZipArchiveEntry(currentFile, entryName);
			entry.setMethod(ZipArchiveEntry.DEFLATED);
			entry.setSize(fileData.length);
			entry.setCrc(crc32.getValue());

			zipOut.putArchiveEntry(entry);
			zipOut.write(fileData);
			zipOut.closeArchiveEntry();
		}
	}

	private static byte[] readFileToByteArray(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			byte[] chunk = new byte[8192];
			int bytesRead;
			while ((bytesRead = fis.read(chunk)) != -1) {
				buffer.write(chunk, 0, bytesRead);
			}
			return buffer.toByteArray();
		}
	}

	public static void main(File sourceDir, File zipFile) {
		try {
			zipDirectory(sourceDir, zipFile);
			System.out.println("ZIP file created: " + zipFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
