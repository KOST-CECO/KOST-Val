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

package ch.kostceco.tools.kosttools.fileservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Magic {

	public static boolean magicZip(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine ZIP-Datei muss mit PK.. [504B0304] beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("50 4B 03 04")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein ZIP
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file zip: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicPdf(File file) throws IOException {
		// Eine PDF Datei (.pdf) muss mit %PDF [25504446] beginnen

		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("25 50 44 46")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein PDF
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file pdf: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicTiff(File file) throws IOException {

		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine TIFF Datei (.tiff / .tif) muss entweder mit II*.
			// [49492A00] oder mit MM.* [4D4D002A] beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				// i: A variable to hold a single byte of the file data
				// cnt: A counter to print a new line every 16 bytes read.
				// Read till the end of the file or break and save the byte in
				// hexadecimal valueS.
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("49 49 2A 00") || sb1234.contains("4D 4D 00 2A")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein TIFF
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file tiff: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicPng(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mnPng = false;

		try {
			// Eine PNG Datei (.png) muss mit 89504E47 -> ‰PNG beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("89 50 4E 47")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein PNG
				mnPng = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file png: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mnPng;
	}

	public static boolean magicXml(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine XML Datei (.xml) muss mit 3C3F786D6C -> <?xml beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb12345 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
						sb12345 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb12345.contains("3C 3F 78 6D 6C")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein XML
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file xml: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicJpeg(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine JPEG Datei (.jpg / .jpeg) muss mit FFD8FF -> ÿØÿ beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb123 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
						sb123 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123.contains("FF D8 FF")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein JPEG
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file jpeg: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicJp2(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine JP2 Datei (.jp2 / .jpeg) muss mit 0000000c6a5020200d0a ->
			// ....jP ..� beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb2str8 = "";
				String sb2str9 = "";
				String sb2str10 = "";
				String sb1234567890 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
					} else if (sb2str8 == "") {
						sb2str8 = sb + "";
					} else if (sb2str9 == "") {
						sb2str9 = sb + "";
					} else if (sb2str10 == "") {
						sb2str10 = sb + "";
						sb1234567890 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234567890.contains("00 00 00 0C 6A 50 20 20 0D 0A")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein JP2
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file JP2: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicJp2p1(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			/*
			 * Eine JP2 Datei (.jp2) muss mit ....jP ..�.ftypjp2 [0000000c6a5020200d0a870a]
			 * beginnen
			 */
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb2str8 = "";
				String sb2str9 = "";
				String sb2str10 = "";
				String sb2str11 = "";
				String sb2str12 = "";
				String sb123456789012 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
					} else if (sb2str8 == "") {
						sb2str8 = sb + "";
					} else if (sb2str9 == "") {
						sb2str9 = sb + "";
					} else if (sb2str10 == "") {
						sb2str10 = sb + "";
					} else if (sb2str11 == "") {
						sb2str11 = sb + "";
					} else if (sb2str12 == "") {
						sb2str12 = sb + "";
						sb123456789012 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123456789012.contains("00 00 00 0C 6A 50 20 20 0D 0A 87 0A")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein JP2p1
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file JP2p1: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicJif(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine JIF-Datei (.jif) muss mit 4A4946393961 -> JIF99a beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb123456 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
						sb123456 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123456.contains("4A 49 46 39 39 61")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein JIF
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Jif: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicFlac(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine FLAC-Datei (.flac) muss mit 664C6143 -> fLaC beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("66 4C 61 43")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein FLAC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Jif: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicRiff(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine RIFF-Datei (.wav, .avi) muss mit 52494646 -> RIFF beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("52 49 46 46")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein WAVE
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Wave: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicAlac(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine ALAC-Datei (.m4a, mp4) muss mit {4}667479704D3441 -> ftypM4A
			// beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1ig = "";
				String sb2str2ig = "";
				String sb2str3ig = "";
				String sb2str4ig = "";
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb1234567 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1ig == "") {
						sb2str1ig = sb + "";
					} else if (sb2str2ig == "") {
						sb2str2ig = sb + "";
					} else if (sb2str3ig == "") {
						sb2str3ig = sb + "";
					} else if (sb2str4ig == "") {
						sb2str4ig = sb + "";
						// Die erste 4 Zeichen ignorieren ; sb neu setzten
						sb = new StringBuilder();
					} else if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
						sb1234567 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234567.contains("66 74 79 70 4D 34 41")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein ALAC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Alac: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMp4(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MP4-Datei (.mp4, .mpg4, .m4v, .m4a, .f4v, .f4a) muss mit
			// {4}66747970 -> {4}ftyp beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1ig = "";
				String sb2str2ig = "";
				String sb2str3ig = "";
				String sb2str4ig = "";
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1ig == "") {
						sb2str1ig = sb + "";
					} else if (sb2str2ig == "") {
						sb2str2ig = sb + "";
					} else if (sb2str3ig == "") {
						sb2str3ig = sb + "";
					} else if (sb2str4ig == "") {
						sb2str4ig = sb + "";
						// Die erste 4 Zeichen ignorieren ; sb neu setzten
						sb = new StringBuilder();
					} else if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("66 74 79 70")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein MP4
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file mp4: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMkv(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MKV-Datei (.mkv) muss mit
			// 1A45DFA3 -> .Eß£ beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("1A 45 DF A3")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein MKV
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file mkv: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicProres(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine PRORES-Datei (.mov) muss mit {4}66726565 -> free beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1ig = "";
				String sb2str2ig = "";
				String sb2str3ig = "";
				String sb2str4ig = "";
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1ig == "") {
						sb2str1ig = sb + "";
					} else if (sb2str2ig == "") {
						sb2str2ig = sb + "";
					} else if (sb2str3ig == "") {
						sb2str3ig = sb + "";
					} else if (sb2str4ig == "") {
						sb2str4ig = sb + "";
						// Die erste 4 Zeichen ignorieren ; sb neu setzten
						sb = new StringBuilder();
					} else if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("66 72 65 65")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein PRORES
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file prores: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicQtm(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine QTM-Datei (.mov) muss mit {4}6D6F6F76 -> moov beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1ig = "";
				String sb2str2ig = "";
				String sb2str3ig = "";
				String sb2str4ig = "";
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1ig == "") {
						sb2str1ig = sb + "";
					} else if (sb2str2ig == "") {
						sb2str2ig = sb + "";
					} else if (sb2str3ig == "") {
						sb2str3ig = sb + "";
					} else if (sb2str4ig == "") {
						sb2str4ig = sb + "";
						// Die erste 4 Zeichen ignorieren ; sb neu setzten
						sb = new StringBuilder();
					} else if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("6D 6F 6F 76")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein QTM
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file qtm: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicVob(File file) throws IOException {
		// Eine VOB-Datei muss mit ...° [000001BA] beginnen

		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("00 00 01 BA")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein VOB
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file vob: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicOgg(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine OGG-Datei (.ogg) muss mit 4F6767530002 -> OggS.. beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb123456 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
						sb123456 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123456.contains("4F 67 67 53 00 02")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein OGG
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Ogg: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMp3(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MP3-Datei (.mp3) muss wiefolgt beginnen
			// MP3 .mp3 ÿû FFFB
			// MP3 .mp3 ÿó FFF3
			// MP3 .mp3 ÿú FFFA
			// MP3 .mp3 ÿò FFF2
			// MP3 .mp3 ÿã FFE3
			// MP3 .mp3 ID3 494433
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb12 = "";
				String sb123 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
						sb12 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
						sb123 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123.contains("49 44 33") || sb12.contains("FF FB") || sb12.contains("FF F3")
						|| sb12.contains("FF FA") || sb12.contains("FF F2") || sb12.contains("FF E3")) {
					// MP3 .mp3 ÿû FFFB
					// MP3 .mp3 ÿó FFF3
					// MP3 .mp3 ÿú FFFA
					// MP3 .mp3 ÿò FFF2
					// MP3 .mp3 ÿã FFE3
					// MP3 .mp3 ID3 494433
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein MP3
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Mp3: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMp2(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MP2-Datei (.mp2, .mpw, .mpa) muss wiefolgt beginnen
			// MP2 .mp2, .mpw, .mpa ID3 494433
			// MP2 .mp2, .mpw, .mpa ÿõ FFF5
			// MP2 .mp2, .mpw, .mpa ÿô FFF4
			// MP2 .mp2, .mpw, .mpa ÿý FFFD
			// MP2 .mp2, .mpw, .mpa ÿü FFFC

			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb12 = "";
				String sb123 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
						sb12 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
						sb123 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123.contains("49 44 33") || sb12.contains("FF F5") || sb12.contains("FF F4")
						|| sb12.contains("FF FD") || sb12.contains("FF FC")) {
					// MP2 .mp2, .mpw, .mpa ID3 494433
					// MP2 .mp2, .mpw, .mpa ÿõ FFF5
					// MP2 .mp2, .mpw, .mpa ÿô FFF4
					// MP2 .mp2, .mpw, .mpa ÿý FFFD
					// MP2 .mp2, .mpw, .mpa ÿü FFFC
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein MP2
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file Mp2: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMpeg2(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MPEG2 Datei (.mpg, .mpeg, .m2v) muss mit 000001B3 -> ...³
			// beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("00 00 01 B3")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein MPEG2
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file mpeg2: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicWarc(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine WARC Datei (.warc) muss mit 57415243 -> WARC beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("57 41 52 43")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein WARC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file warc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicArc(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine ARC Datei (.arc) muss mit 66696C656465 -> filede beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb123456 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
						sb123456 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123456.contains("66 69 6C 65 64 65")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein ARC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file arc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicFreearc(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine FREEARC Datei (.arc) muss mit 41724301 -> ArC. beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("41 72 43 01")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein FREEARC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file freearc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicXls(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine XLS Datei (.xls) muss mit 09040600 09020600 09000400 -> ....
			// beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					// System.out.printf("%02X ", i);
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("09 04 06 00") || sb1234.contains("09 02 06 00")
						|| sb1234.contains("09 00 04 00")) {
					reco = true;
				}
			}

			if (reco) {
				// hoechstwahrscheinlich ein XLS
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file arc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicInterlis(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine INTERLIS Datei (.ili, .itf, .xtf, .xml) muss wie folgt
			// beginnen
			// INTERLIS(8) TRANSFER(8) SCNT(4)
			// "494E5445524C4953 5452414E53464552 53434E54"
			// <?xml(5) <.?.x.m.l.(10)
			// 3C3F786D6C 3C003F0078006D006C00
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb2str8 = "";
				String sb2str9 = "";
				String sb2str10 = "";
				String sb1234 = "";
				String sb12345 = "";
				String sb12345678 = "";
				String sb12345678910 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
						sb12345 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
						sb12345 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
					} else if (sb2str8 == "") {
						sb2str8 = sb + "";
						sb12345678 = sb + "";
					} else if (sb2str9 == "") {
						sb2str9 = sb + "";
					} else if (sb2str10 == "") {
						sb2str10 = sb + "";
						sb12345678910 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("53 43 4E 54") || sb12345.contains("3C 3F 78 6D 6C")
						|| sb12345678.contains("49 4E 54 45 52 4C 49 53")
						|| sb12345678.contains("54 52 41 4E 53 46 45 52")
						|| sb12345678910.contains("3C 00 3F 00 78 00 6D 00 6C 00")) {
					reco = true;
					// INTERLIS(8) TRANSFER(8) SCNT(4)
					// "494E5445524C4953 5452414E53464552 53434E54"
					// <?xml(5) <.?.x.m.l.(10)
					// 3C3F786D6C 3C003F0078006D006C00
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein INTERLIS
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file interlis: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicDwg(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine DWG Datei (.dwg) muss wie folgt
			// beginnen MC oder AC 4D43 oder 4143
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb12 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
						sb12 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb12.contains("4D 43") || sb12.contains("41 43")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein DWG
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file ifc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicIfc(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine IFC Datei (.ifc) muss wie folgt
			// beginnen
			// ISO-10303 49534F2D3130333033
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb2str8 = "";
				String sb2str9 = "";
				String sb123456789 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
					} else if (sb2str8 == "") {
						sb2str8 = sb + "";
					} else if (sb2str9 == "") {
						sb2str9 = sb + "";
						sb123456789 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb123456789.contains("49 53 4F 2D 31 30 33 30 33")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein IFC
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file ifc: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicDicom(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine Dicom-Datei (keine Dateiendung noetig) muss mit
			// {128}4449434D
			// -> DICM beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				int counterIgn = 0;
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (counterIgn < 128) {
						counterIgn = counterIgn + 1;
						// Die erste 128 Zeichen ignorieren ; sb neu setzten
						sb = new StringBuilder();
					} else if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb1234.contains("44 49 43 4D")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein Dicom
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file dicom: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicMsOffice(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine MS Office Datei 97-2003 (.doc .xls .ppt .msi .msg) muss wie
			// folgt beginnen
			// D0 CF 11 E0 A1 B1 1A E1
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb2str6 = "";
				String sb2str7 = "";
				String sb2str8 = "";
				String sb12345678 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
					} else if (sb2str6 == "") {
						sb2str6 = sb + "";
					} else if (sb2str7 == "") {
						sb2str7 = sb + "";
					} else if (sb2str8 == "") {
						sb2str8 = sb + "";
						sb12345678 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb12345678.contains("D0 CF 11 E0 A1 B1 1A E1")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein MS Office Datei 97-2003 (.doc .xls .ppt .msi .msg)
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file MsOffice: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

	public static boolean magicRtf(File file) throws IOException {
		FileReader fr = null;
		BufferedReader read = null;
		boolean mn = false;

		try {
			// Eine RTF Datei (.rtf) muss mit 7B5C727466 -> {\rtf beginnen
			fr = new FileReader(file);
			read = new BufferedReader(fr);

			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream(file)) {
				int i = 0;
				int cnt = 0;
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb2str5 = "";
				String sb12345 = "";
				while ((i = fis.read()) != -1) {
					sb.append(String.format("%02X ", i));
					if (sb2str1 == "") {
						sb2str1 = sb + "";
					} else if (sb2str2 == "") {
						sb2str2 = sb + "";
					} else if (sb2str3 == "") {
						sb2str3 = sb + "";
					} else if (sb2str4 == "") {
						sb2str4 = sb + "";
					} else if (sb2str5 == "") {
						sb2str5 = sb + "";
						sb12345 = sb + "";
						break;
					}
					cnt++;
					if (cnt == 16) {
						cnt = 0;
					}
				}
				if (sb12345.contains("7B 5C 72 74 66")) {
					reco = true;
				}
			}
			if (reco) {
				// hoechstwahrscheinlich ein RTF
				mn = true;
			}
			read.close();
			fr.close();
			read = null;
			fr = null;
		} catch (Exception e) {
			System.out.println("Exception magic file rtf: " + e.getMessage());
			read.close();
			fr.close();
			read = null;
			fr = null;
		}
		return mn;
	}

}
