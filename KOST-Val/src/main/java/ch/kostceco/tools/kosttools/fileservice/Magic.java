/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO. 2012-2022
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
import java.util.Arrays;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Magic
{


	public static boolean magicZip( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnZip = false;

		try {
			// Eine ZIP Datei muss mit PK.. beginnen
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// Hex 03 in Char umwandeln
			String str3 = "03";
			int i3 = Integer.parseInt( str3, 16 );
			char c3 = (char) i3;
			// Hex 04 in Char umwandeln
			String str4 = "04";
			int i4 = Integer.parseInt( str4, 16 );
			char c4 = (char) i4;

			// auslesen der ersten 4 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[4];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			// die beiden charArrays (soll und ist) mit einander vergleichen
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				// hoechstwahrscheinlich ein ZIP da es mit 504B0304 respektive PK.. beginnt
				// System.out.println("wahrscheinlich ein ZIP da es mit 504B0304 respektive PK.. beginnt");
				mnZip = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file zip: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnZip;
	}

	public static boolean magicPdf( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnPdf = false;

		try {
			// Eine PDF Datei (.pdf / .pdfa) muss mit %PDF [25504446] beginnen
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// Hex 25 in Char umwandeln
			String str1 = "25";
			int i1 = Integer.parseInt( str1, 16 );
			char c1 = (char) i1;
			// Hex 50 in Char umwandeln
			String str2 = "50";
			int i2 = Integer.parseInt( str2, 16 );
			char c2 = (char) i2;
			// Hex 44 in Char umwandeln
			String str3 = "44";
			int i3 = Integer.parseInt( str3, 16 );
			char c3 = (char) i3;
			// Hex 46 in Char umwandeln
			String str4 = "46";
			int i4 = Integer.parseInt( str4, 16 );
			char c4 = (char) i4;

			// auslesen der ersten 4 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[4];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			// die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c2c3c4
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { c1, c2, c3, c4 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				// hoechstwahrscheinlich ein PDF da es mit 25504446 respektive %PDF beginnt
				// System.out.println("wahrscheinlich ein PDF da es mit 25504446 respektive %PDF beginnt");
				mnPdf = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file pdf: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnPdf;
	}

	public static boolean magicTiff( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnTiff = false;

		try {
			/* Eine TIFF Datei (.tiff / .tif / .tfx) muss entweder mit II*. [49492A00] oder mit MM.*
			 * [4D4D002A] beginnen */
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// Hex 49 in Char umwandeln
			String str1 = "49";
			int i1 = Integer.parseInt( str1, 16 );
			char c1 = (char) i1;
			// Hex 4D in Char umwandeln
			String str2 = "4D";
			int i2 = Integer.parseInt( str2, 16 );
			char c2 = (char) i2;
			// Hex 2A in Char umwandeln
			String str3 = "2A";
			int i3 = Integer.parseInt( str3, 16 );
			char c3 = (char) i3;
			// Hex 00 in Char umwandeln
			String str4 = "00";
			int i4 = Integer.parseInt( str4, 16 );
			char c4 = (char) i4;

			// auslesen der ersten 4 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[4];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c1c3c4 /c2c2c4c3 */
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { c1, c1, c3, c4 };
			char[] charArray3 = new char[] { c2, c2, c4, c3 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				// hoechstwahrscheinlich ein TIFF da es mit 49492A00 respektive II*. beginnt
				// System.out.println("wahrscheinlich ein TIFF da es mit 49492A00 respektive II*. beginnt");
				mnTiff = true;
			} else if ( Arrays.equals( charArray1, charArray3 ) ) {
				// hoechstwahrscheinlich ein TIFF da es mit 4D4D002A respektive MM.* beginnt
				// System.out.println("wahrscheinlich ein TIFF da es mit 4D4D002A respektive MM.* beginnt");
				mnTiff = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file tiff: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnTiff;
	}

	public static boolean magicPng( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnPng = false;

		try {
			// Eine JPEG Datei (.png) muss mit 89504E47 -> ‰PNG beginnen
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// die ersten 4 Zeichen einer PNG Datei

			// 1 89
			// 2 50
			// 3 4e
			// 4 47

			// Open the file using FileInputStream
			Boolean reco = false;
			try (FileInputStream fis = new FileInputStream( file )) {
				// A variable to hold a single byte of the file data
				int i = 0;

				// A counter to print a new line every 16 bytes read.
				int cnt = 0;

				// Read till the end of the file and print the byte in hexadecimal valueS.
				StringBuilder sb = new StringBuilder();
				String sb2str1 = "";
				String sb2str2 = "";
				String sb2str3 = "";
				String sb2str4 = "";
				String sb1234 = "";
				while ( (i = fis.read()) != -1 ) {
					// System.out.printf("%02X ", i);
					sb.append( String.format( "%02X ", i ) );
					if ( sb2str1 == "" ) {
						sb2str1 = sb + "";
					} else if ( sb2str2 == "" ) {
						sb2str2 = sb + "";
					} else if ( sb2str3 == "" ) {
						sb2str3 = sb + "";
					} else if ( sb2str4 == "" ) {
						sb2str4 = sb + "";
						sb1234 = sb + "";
						break;
					}
					cnt++;
					if ( cnt == 16 ) {
						cnt = 0;
					}
				}
				if ( sb1234.contains( "89 50 4E 47" ) ) {
					reco = true;
				}
			}

			if ( reco ) {
				// hoechstwahrscheinlich ein PNG da es mit 89504E47 respektive ‰PNG beginnt
				// System.out.println( "wahrscheinlich ein PNG da es mit 89504E47 respektive ‰PNG beginnt" );
				mnPng = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file png: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnPng;
	}

	public static boolean magicJpeg( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnJpeg = false;

		try {
			// Eine JPEG Datei (.jpg / .jpeg) muss mit FFD8FF -> ÿØÿ beginnen
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// wobei hier nur die ersten 3 Zeichen der Datei ausgelesen werden
			// 1 FF
			// 2 D8
			// 3 FF = 1

			// Hex FF in Char umwandeln
			String str1 = "FF";
			int i1 = Integer.parseInt( str1, 16 );
			char c1 = (char) i1;
			// Hex D8 in Char umwandeln
			String str2 = "D8";
			int i2 = Integer.parseInt( str2, 16 );
			char c2 = (char) i2;

			// auslesen der ersten 3 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[3];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c2c1 */
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { c1, c2, c1 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				// hoechstwahrscheinlich ein JPEG da es mit FFD8FF respektive ÿØÿ beginnt
				// System.out.println( "wahrscheinlich ein JPEG da es mit FFD8FF respektive ÿØÿ beginnt" );
				mnJpeg = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file jpeg: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnJpeg;
	}

	public static boolean magicJp2( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnJp2 = false;

		try {
			// Eine JP2 Datei (.jp2 / .jpeg) muss mit 0000000c6a5020200d0a -> ....jP ..ï¿½ beginnen
			fr = new FileReader( file );
			read = new BufferedReader( fr );

			// wobei hier nur die ersten 10 Zeichen der Datei ausgelesen werden
			// 1 00 010203
			// 2 0c 04
			// 3 6a 05
			// 4 50 06
			// 5 20 0708
			// 6 0d 09
			// 7 0a 10

			// Hex 00 in Char umwandeln
			String str1 = "00";
			int i1 = Integer.parseInt( str1, 16 );
			char c1 = (char) i1;
			// Hex 0c in Char umwandeln
			String str2 = "0c";
			int i2 = Integer.parseInt( str2, 16 );
			char c2 = (char) i2;
			// Hex 6a in Char umwandeln
			String str3 = "6a";
			int i3 = Integer.parseInt( str3, 16 );
			char c3 = (char) i3;
			// Hex 50 in Char umwandeln
			String str4 = "50";
			int i4 = Integer.parseInt( str4, 16 );
			char c4 = (char) i4;
			// Hex 20 in Char umwandeln
			String str5 = "20";
			int i5 = Integer.parseInt( str5, 16 );
			char c5 = (char) i5;
			// Hex 0d in Char umwandeln
			String str6 = "0d";
			int i6 = Integer.parseInt( str6, 16 );
			char c6 = (char) i6;
			// Hex 0a in Char umwandeln
			String str7 = "0a";
			int i7 = Integer.parseInt( str7, 16 );
			char c7 = (char) i7;

			// auslesen der ersten 10 Zeichen der Datei
			int length;
			int i;
			char[] buffer = new char[10];
			length = read.read( buffer );
			for ( i = 0; i != length; i++ )
				;

			/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c1c1c2c3c4c5c5c6c7 */
			char[] charArray1 = buffer;
			char[] charArray2 = new char[] { c1, c1, c1, c2, c3, c4, c5, c5, c6, c7 };

			if ( Arrays.equals( charArray1, charArray2 ) ) {
				/* hoechstwahrscheinlich ein JP2 da es mit 0000000c6a5020200d0a respektive ....jP ..ï¿½
				 * beginnt */
				// System.out.println("wahrscheinlich ein JP2 oder JPX da es mit 0000000c6a5020200d0a
				// respektive ....jP ..ï¿½ beginnt");
				mnJp2 = true;
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file jp2: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnJp2;
	}

	public static boolean magicJp2p1( File file ) throws IOException
	{

		FileReader fr = null;
		BufferedReader read = null;
		boolean mnJp2p1 = false;

		try {
			/* Eine JP2 Datei (.jp2) muss mit ....jP ..ï¿½.ftypjp2 [0000000c6a5020200d0a870a] beginnen */
			// Test Magic.magicJpeg

			/* Sicherstellen, dass es ein JP2 (Part1) und kein JPX/JPM (Part2) ist. Jpylyzer kann nur JP2
			 * und gibt sonst keine korrekte Fehlermeldung raus.
			 * 
			 * JP2-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 32
			 * 
			 * 1 2 3 4 5 6 7 8 9 10 11 12 17 18 19 20 21 22 23
			 * 
			 * JPX-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 78
			 * 
			 * JPM-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 6D */

			if ( Magic.magicJp2( file ) ) {
				/* hoechstwahrscheinlich ein JP2 da es mit 0000000c6a5020200d0a respektive ....jP ..ï¿½
				 * beginnt */
				// System.out.println("es ist ein JP2 oder JPX ");

				fr = new FileReader( file );
				read = new BufferedReader( fr );

				// wobei hier nur die Zeichen 17-23 der Datei verglichen werden (Part-Teil)
				// 3 6a 21
				// 9 66 17
				// 10 74 18
				// 11 79 19
				// 12 70 2022
				// 13 32 23

				// Hex 6a in Char umwandeln
				String str3 = "6a";
				int i3 = Integer.parseInt( str3, 16 );
				char c3 = (char) i3;
				// Hex 66 in Char umwandeln
				String str9 = "66";
				int i9 = Integer.parseInt( str9, 16 );
				char c9 = (char) i9;
				// Hex 74 in Char umwandeln
				String str10 = "74";
				int i10 = Integer.parseInt( str10, 16 );
				char c10 = (char) i10;
				// Hex 79 in Char umwandeln
				String str11 = "79";
				int i11 = Integer.parseInt( str11, 16 );
				char c11 = (char) i11;
				// Hex 70 in Char umwandeln
				String str12 = "70";
				int i12 = Integer.parseInt( str12, 16 );
				char c12 = (char) i12;
				// Hex 32 in Char umwandeln
				String str13 = "32";
				int i13 = Integer.parseInt( str13, 16 );
				char c13 = (char) i13;

				// auslesen der ersten 23 Zeiche der Datei
				int length23;
				int i23;
				char[] buffer23 = new char[23];
				length23 = read.read( buffer23 );
				for ( i23 = 0; i23 != length23; i23++ )
					;

				/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c9, c10, c11, c12,
				 * c3, c12, c13 */
				char[] charArray3 = buffer23;
				char[] charArray4 = new char[] { c9, c10, c11, c12, c3, c12, c13 };
				String stringCharArray3 = String.valueOf( charArray3 );
				String stringCharArray4 = String.valueOf( charArray4 );
				buffer23 = null;
				read.close();
				fr.close();
				if ( stringCharArray3.endsWith( stringCharArray4 ) ) {
					/* hoechstwahrscheinlich ein JP2 (JPEG2000 Part1) da es mit 0000000c6a5020200d0a
					 * respektive ....jP ..ï¿½ beginnt gefolgt von {4} 66 74 79 70 6A 70 32 */
					// System.out.println("wahrscheinlich ein JP2 (JPEG2000 Part1) da es mit
					// 0000000c6a5020200d0a respektive ....jP ..ï¿½ beginnt gefolgt von {4} 66 74 79 70 6A 70
					// 32 ");
					mnJp2p1 = true;
					// System.out.print("es ist ein JP2 (JPEG2000 Part1)");
				} else {
					// System.out.print("es ist ein JPX (Part2) / JPM (Part6)");
					mnJp2p1 = false;
				}
			}
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		} catch ( Exception e ) {
			System.out.println( "Exception magic file jp2 part1: " + e.getMessage() );
			read.close();
			fr.close();
			// set to null
			read = null;
			fr = null;
		}
		return mnJp2p1;
	}

}
