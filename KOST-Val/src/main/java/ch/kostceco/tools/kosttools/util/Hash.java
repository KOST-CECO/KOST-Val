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
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Hash {
	private static String hash;

	/**
	 * fuehrt die gewuenschte Hashwert-Berechnung durch und gibt diesen aus
	 * 
	 * @param fileFolder Datei oder Ordner fue die Berechnung
	 * @return String hashwert
	 */
	public static String getMd5(File fileFolder) throws InterruptedException {
		hash = "";
		// Start MD5 Berechnung
		String algo = "MD5";

		hash = Hash.getHash(fileFolder, algo);
		return hash;
	}

	public static String getSha1(File fileFolder) throws InterruptedException {
		hash = "";
		// Start SHA-1 Berechnung
		String algo = "SHA-1";

		hash = Hash.getHash(fileFolder, algo);
		return hash;
	}

	public static String getSha256(File fileFolder) throws InterruptedException {
		hash = "";
		// Start SHA-256 Berechnung
		String algo = "SHA-256";

		hash = Hash.getHash(fileFolder, algo);
		return hash;
	}

	public static String getSha512(File fileFolder) throws InterruptedException {
		hash = "";
		// Start SHA-512 Berechnung
		String algo = "SHA-512";

		hash = Hash.getHash(fileFolder, algo);
		return hash;
	}

	public static String getHash(File fileFolder, String algo) throws InterruptedException {
		hash = "";
		// Start Hash Berechnung
		String output = algo;
		if (fileFolder.exists()) {
			if (algo.equals("MD5") || algo.equals("SHA-1") || algo.equals("SHA-256") || algo.equals("SHA-512"))
				// Andere wurden nicht getestet
				try {
					String filepath = fileFolder.getAbsolutePath();
					MessageDigest messageDigest;
					messageDigest = MessageDigest.getInstance(algo);

					FileInputStream fileInput = new FileInputStream(filepath);
					byte[] dataBytes = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = fileInput.read(dataBytes)) != -1) {
						messageDigest.update(dataBytes, 0, bytesRead);
					}
					byte[] digestBytes = messageDigest.digest();
					StringBuffer sb = new StringBuffer("");

					for (int i = 0; i < digestBytes.length; i++) {
						sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
					}
					output = sb.toString();
					while (output.length() < 32) {
						output = "0" + output;
					}

					fileInput.close();
				} catch (NoSuchAlgorithmException | IOException e1) {
					e1.printStackTrace();
				}
		} else {
			System.out.println("Only MD5, SHA-1, SHA-256 & SHA-521. Not " + algo);
		}
		hash = output;
		return hash;
	}

}