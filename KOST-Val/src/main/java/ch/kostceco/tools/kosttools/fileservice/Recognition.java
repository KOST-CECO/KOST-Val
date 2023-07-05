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

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FilenameUtils;

import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Recognition
{
	/**
	 * fuehrt eine Formaterkennung basierend auf den Formaten des KaD durch und
	 * gibt das Ergebnis als String zurueck
	 * 
	 * @param checkFile
	 *            Datei, welche erkannt werden soll
	 * @return String mit Validierungsergebnis ("Format" oder den Fehler.
	 */
	public static String formatRec( File checkFile ) throws InterruptedException
	{
		String checkFileName = checkFile.getName();
		String checkFileExt = "."
				+ FilenameUtils.getExtension( checkFileName ).toLowerCase();
		String extUpperCase = checkFileExt.toUpperCase();

		/*
		 * A) Sortieren anhand des 1. Char der Datei
		 * 
		 * B) Wenn 1. Char bekann dann die möglichen BOF kontrollieren
		 * 
		 * C) ggf auf SiF weiterkontrollieren
		 * 
		 * D) Wenn erkannt dann passende Extension kontrollieren
		 * 
		 * E) Wenn alles OK Format ausgeben ansonsten den Fehler
		 */

		try {
			// TODO A) Sortieren anhand des 1. Char der Datei

			FileInputStream fis = new FileInputStream( checkFile );
			// A variable to hold a single byte of the file data
			int i = 0;

			// A counter to print a new line every 16 bytes read.
			int cnt = 0;

			// Read till the end of the file and print the byte in
			// hexadecimal valueS.
			StringBuilder sb = new StringBuilder();
			String sb2str1 = "";
			while ( (i = fis.read()) != -1 ) {
				// System.out.printf("%02X ", i);
				sb.append( String.format( "%02X ", i ) );
				if ( sb2str1 == "" ) {
					sb2str1 = sb + "";
					break;
				}
				cnt++;
				if ( cnt == 16 ) {
					cnt = 0;
				}
			}
			fis.close();

			// System.out.println("Das erste Zeichen der Datei (Hex) =
			// "+sb2str1);

			// das IST character einordnen mit den bekannten cXY
			if ( sb2str1.contains( "00" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 00)

				if ( Magic.magicJp2( checkFile ) ) {
					// Eine JP?-Datei muss mit ....jP .. [0000000c6a5020200d0a]
					// beginnen
					if ( Util.stringInFile( "ftypjp2", checkFile ) ) {
						// C) fuer JP2 auf SiF weiterkontrollieren
						// Eine JP2-Datei muss den String "ftypjp2" im
						// File haben
						// D) passende Extension kontrollieren
						// Eine JP2-Datei muss die extension .jp2 haben
						if ( checkFileExt.equals( ".jp2" ) ) {
							// eindeutig als JP2-Datei erkannt
							return "JP2";
						} else {
							// als JP2-Datei erkannt aber falsche Extension
							return "JP2_ext";
						}
					} else if ( Util.stringInFile( "ftypjpx", checkFile ) ) {
						// C) fuer JPX auf SiF weiterkontrollieren
						// Eine JPX-Datei muss den String "ftypjpx"
						// im File haben
						// D) passende Extension kontrollieren
						// Eine JPX-Datei muss die extension .jpx, .jpf haben
						if ( checkFileExt.equals( ".jpx" )
								|| checkFileExt.equals( ".jpf" ) ) {
							// eindeutig als JPX-Datei erkannt
							return "JPX";
						} else {
							// als JPX-Datei erkannt aber falsche Extension
							return "JPX_ext";
						}
					} else if ( Util.stringInFile( "ftypjpm", checkFile ) ) {
						// C) fuer JPM auf SiF weiterkontrollieren
						// Eine JPM-Datei muss den String
						// "ftypjpm" im File haben
						// D) passende Extension kontrollieren
						// Eine JPM-Datei muss die extension .jpm haben
						if ( checkFileExt.equals( ".jpm" ) ) {
							// eindeutig als JPM-Datei erkannt
							return "JPM";
						} else {
							// als JPM-Datei erkannt aber falsche Extension
							return "JPM_ext";
						}
					} else if ( Util.stringInFile( "ftypmjp2", checkFile ) ) {
						// C) fuer MJ2 auf SiF weiterkontrollieren
						// Eine MJ2-Datei muss den String "ftypmjp2"
						// im File haben
						// D) passende Extension kontrollieren
						// Eine MJ2-Datei muss die extension .mjp2, .mj2 haben
						if ( checkFileExt.equals( ".mjp2" )
								|| checkFileExt.equals( ".mj2" ) ) {
							// eindeutig als MJ2-Datei erkannt
							return "MJ2";
						} else {
							// als MJ2-Datei erkannt aber falsche Extension
							return "MJ2_ext";
						}
					} else {
						// scheint eine andere JP?-Datei zu sein
						// Kontrolle Dateiendung
						if ( checkFileExt.equals( ".jp2" )
								|| checkFileExt.equals( ".jp2" )
								|| checkFileExt.equals( ".jpx" )
								|| checkFileExt.equals( ".jpf" )
								|| checkFileExt.equals( ".jpm" ) ) {
							// als JP?-Datei erkannt aber falsche Extension
							return "JP?_ext";
						} else {
							// eindeutig als JP?-Datei erkannt
							return "JP?";
						}
					}
				} else if ( Magic.magicMpeg2( checkFile ) ) {
					// BOF entspricht einer MPEG2-Datei (...³ [000001B3])
					// D) passende Extension kontrollieren (keine SiF)
					// Eine MPEG2-Datei muss die extension .mpg, .mpeg, .m2v
					// haben
					if ( checkFileExt.equals( ".mpg" )
							|| checkFileExt.equals( ".mpeg" )
							|| checkFileExt.equals( ".m2v" ) ) {
						// eindeutig als MPEG2-Datei erkannt
						return "MPEG2";
					} else {
						// als MPEG2-Datei erkannt aber falsche Extension
						return "MPEG2_ext";
					}
				}
			} else if ( sb2str1.contains( "09" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 09)
				if ( Magic.magicXls( checkFile ) ) {
					// Eine XLS-Datei muss mit .... [09040600 09020600 09000400]
					// beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine XLS-Datei muss die extension .xls haben
					if ( checkFileExt.equals( ".xls" ) ) {
						// eindeutig als XLS-Datei erkannt
						return "XLS";
					} else {
						// als XLS-Datei erkannt aber falsche Extension
						return "XLS_ext";
					}
				}
			} else if ( sb2str1.contains( "1A" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 1A)
				if ( Magic.magicMkv( checkFile ) ) {
					// Eine MKV-Datei muss mit.Eß£ [1A45DFA3] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine MKV-Datei muss die extension .mkv haben
					if ( checkFileExt.equals( ".mkv" ) ) {
						// eindeutig als MKV-Datei erkannt
						return "MKV";
					} else {
						// als MKV-Datei erkannt aber falsche Extension
						return "MKV_ext";
					}
				}
			} else if ( sb2str1.contains( "25" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 25)
				if ( Magic.magicPdf( checkFile ) ) {
					// Eine PDF-Datei muss mit %PDF [25504446] beginnen
					if ( Util.stringInFile( "pdfaid", checkFile ) ) {
						// C) fuer PDFA auf SiF weiterkontrollieren
						// Eine PDF/A-Datei muss den String "pdfaid" im File
						// haben
						// D) passende Extension kontrollieren
						// Eine PDF/A-Datei muss die extension .pdf haben
						if ( checkFileExt.equals( ".pdf" ) ) {
							// eindeutig als PDF/A-Datei erkannt
							return "PDFA";
						} else {
							// als PDF/A-Datei erkannt aber falsche Extension
							return "PDFA_ext";
						}
					} else {
						// scheint eine PDF-Datei zu sein
						// Eine PDF-Datei muss die extension .pdf haben
						if ( checkFileExt.equals( ".pdf" ) ) {
							// eindeutig als PDF-Datei erkannt
							return "PDF";
						} else {
							// als PDF-Datei erkannt aber falsche Extension
							return "PDF_ext";
						}
					}
				}
			} else if ( sb2str1.contains( "3C" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 3c)
				if ( Magic.magicXml( checkFile ) ) {
					// Eine XML-Datei muss mit <?xml [3C3F786D6C] beginnen
					if ( Util.string15InFile( "INTERLIS", "I.N.T.E.R.L.I.S.",
							"INTERLIS", "INTERLIS", "INTERLIS", checkFile ) ) {
						// C) fuer INTERLIS auf SiF weiterkontrollieren
						// Eine INTERLIS-Datei muss den String "INTERLIS" oder
						// "I.N.T.E.R.L.I.S." im File haben
						// INTERLIS .xtf, .xml "<?xml <.?.x.m.l." "3C3F786D6C
						// 3C003F0078006D006C00" "INTERLIS I.N.T.E.R.L.I.S."
						// D) passende Extension kontrollieren
						// Eine solche INTERLIS-Datei muss die extension .xtf,
						// .xml haben
						if ( checkFileExt.equals( ".xtf" )
								|| checkFileExt.equals( ".xml" ) ) {
							// eindeutig als INTERLIS-Datei erkannt
							return "INTERLIS";
						} else {
							// als INTERLIS-Datei erkannt aber falsche Extension
							return "INTERLIS_ext";
						}
					}
					// D) passende Extension kontrollieren (keine SiF)
					// Eine XML-Datei muss die extension .xml, .xsd, .xsl haben
					if ( checkFileExt.equals( ".xml" )
							|| checkFileExt.equals( ".xsd" )
							|| checkFileExt.equals( ".xsl" ) ) {
						// eindeutig als XML-Datei erkannt
						return "XML";
					} else if ( checkFileExt.equals( ".svg" ) ) {
						// eindeutig als SVG-Datei erkannt
						return "SVG";
					} else {
						// als XML-Datei erkannt aber falsche Extension
						return "XML_ext";
					}
				} else if ( Magic.magicInterlis( checkFile ) ) {
					// BOF entspricht einer INTERLIS-Datei
					// (<.?.x.m.l. [3C003F0078006D006C00])
					if ( Util.string15InFile( "INTERLIS", "I.N.T.E.R.L.I.S.",
							"INTERLIS", "INTERLIS", "INTERLIS", checkFile ) ) {
						// C) fuer INTERLIS auf SiF weiterkontrollieren
						// Eine INTERLIS-Datei muss den String "INTERLIS" oder
						// "I.N.T.E.R.L.I.S." im File haben
						// INTERLIS .xtf, .xml "<?xml <.?.x.m.l." "3C3F786D6C
						// 3C003F0078006D006C00" "INTERLIS I.N.T.E.R.L.I.S."
						// D) passende Extension kontrollieren
						// Eine solche INTERLIS-Datei muss die extension .xtf,
						// .xml haben
						if ( checkFileExt.equals( ".xtf" )
								|| checkFileExt.equals( ".xml" ) ) {
							// eindeutig als INTERLIS-Datei erkannt
							return "INTERLIS";
						} else {
							// als INTERLIS-Datei erkannt aber falsche Extension
							return "INTERLIS_ext";
						}
					}
				} else {
					// keine passende BOF gefunden ggf. Format mit nur einem Hex
					if ( Util.string15InFile( "HTML", "html", "html", "html",
							"html", checkFile ) ) {
						// C) fuer HTML auf SiF weiterkontrollieren
						// Eine HTML-Datei muss den String "HTML" oder "html"
						// im File haben
						// HTML .html, .htm < 3C HTML html
						// D) passende Extension kontrollieren
						// Eine MJ2-Datei muss die extension .html, .htm haben
						if ( checkFileExt.equals( ".html" )
								|| checkFileExt.equals( ".htm" ) ) {
							// eindeutig als HTML-Datei erkannt
							return "HTML";
						} else {
							// als HTML-Datei erkannt aber falsche Extension
							return "HTML_ext";
						}
					}
				}
			} else if ( sb2str1.contains( "41" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 41)

				if ( Magic.magicFreearc( checkFile ) ) {
					// Eine FREEARC-Datei muss mit ArC. [41724301] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine FREEARC-Datei muss die extension .arc haben
					if ( checkFileExt.equals( ".arc" ) ) {
						// eindeutig als FREEARC-Datei erkannt
						return "FREEARC";
					} else {
						// als FREEARC-Datei erkannt aber falsche Extension
						return "FREEARC_ext";
					}
				} else if ( Magic.magicDwg( checkFile ) ) {
					// Eine DWG-Datei muss mit MC oder AC [4D43 oder 4143]
					// beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine DWG-Datei muss die extension .dwg haben
					if ( checkFileExt.equals( ".dwg" ) ) {
						// eindeutig als DWG-Datei erkannt
						return "DWG";
					} else {
						// als DWG-Datei erkannt aber falsche Extension
						return "DWG_ext";
					}
				}

			} else if ( sb2str1.contains( "49" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 49)

				if ( Magic.magicTiff( checkFile ) ) {
					// Eine TIFF-Datei (DNG) muss mit II*. [49492A00] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine TIFF-Datei muss die extension .tiff, .tif haben
					if ( checkFileExt.equals( ".tiff" )
							|| checkFileExt.equals( ".tif" ) ) {
						// eindeutig als TIFF-Datei erkannt
						return "TIFF";
					} else if ( checkFileExt.equals( ".dng" ) ) {
						// Eine DNG-Datei muss die extension .dng haben
						// eindeutig als DNG-Datei erkannt
						return "DNG";
					} else {
						// als TIFF-Datei erkannt aber falsche Extension
						return "TIFF_ext";
					}
				} else if ( Magic.magicMp3( checkFile ) ) {
					// BOF entspricht einer MP3-Datei (ID3 [494433])
					// D) passende Extension kontrollieren (keine SiF)
					// Eine MP3-Datei muss die extension .mp3 haben
					if ( checkFileExt.equals( ".mp3" ) ) {
						// eindeutig als MP3-Datei erkannt
						return "MP3";
					} else if ( checkFileExt.equals( ".mp2" )
							|| checkFileExt.equals( ".mpw" )
							|| checkFileExt.equals( ".mpa" ) ) {
						// eindeutig als MP2-Datei erkannt
						return "MP2";
					} else {
						// als MP3-Datei erkannt aber falsche Extension
						return "MP3_ext";
					}
				} else if ( Magic.magicInterlis( checkFile ) ) {
					// BOF entspricht einer INTERLIS-Datei
					// INTERLIS .ili, .itf
					// D) passende Extension kontrollieren (kein SiF)
					// Eine solche INTERLIS-Datei muss die extension .ili, .itf
					// haben
					if ( checkFileExt.equals( ".ili" )
							|| checkFileExt.equals( ".itf" ) ) {
						// eindeutig als INTERLIS-Datei erkannt
						return "INTERLIS";
					} else {
						// als INTERLIS-Datei erkannt aber falsche Extension
						return "INTERLIS_ext";
					}
				} else if ( Magic.magicIfc( checkFile ) ) {
					// Eine IFC-Datei muss mit ISO-10303 [49534F2D3130333033]
					// beginnen
					// IFC .ifc ISO-10303 49534F2D3130333033 IFC
					if ( Util.stringInFile( "IFC", checkFile ) ) {
						// C) fuer IFC auf SiF weiterkontrollieren
						// Eine IFC-Datei muss den String "IFC" im
						// File haben
						// D) passende Extension kontrollieren
						// Eine IFC-Datei muss die extension .ifc haben
						if ( checkFileExt.equals( ".ifc" ) ) {
							// eindeutig als IFC-Datei erkannt
							return "IFC";
						} else {
							// als IFC-Datei erkannt aber falsche Extension
							return "IFC_ext";
						}
					}
				}
			} else if ( sb2str1.contains( "4A" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 4a)

				if ( Magic.magicJif( checkFile ) ) {
					// Eine JIF-Datei muss mit JIF99a [4A4946393961] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine JIF-Datei muss die extension .jif haben
					if ( checkFileExt.equals( ".jif" ) ) {
						// eindeutig als JIF-Datei erkannt
						return "JIF";
					} else {
						// als JIF-Datei erkannt aber falsche Extension
						return "JIF_ext";
					}
				}
			} else if ( sb2str1.contains( "4D" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 4d)

				if ( Magic.magicTiff( checkFile ) ) {
					// Eine TIFF-Datei (DNG) muss mit MM.* [4D4D002A] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine TIFF-Datei muss die extension .tiff, .tif haben
					if ( checkFileExt.equals( ".tiff" )
							|| checkFileExt.equals( ".tif" ) ) {
						// eindeutig als TIFF-Datei erkannt
						return "TIFF";
					} else if ( checkFileExt.equals( ".dng" ) ) {
						// Eine DNG-Datei muss die extension .dng haben
						// eindeutig als DNG-Datei erkannt
						return "DNG";
					} else {
						// als TIFF-Datei erkannt aber falsche Extension
						return "TIFF_ext";
					}
				} else if ( Magic.magicDwg( checkFile ) ) {
					// Eine DWG-Datei muss mit MC oder AC [4D43 oder 4143]
					// beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine DWG-Datei muss die extension .dwg haben
					if ( checkFileExt.equals( ".dwg" ) ) {
						// eindeutig als DWG-Datei erkannt
						return "DWG";
					} else {
						// als DWG-Datei erkannt aber falsche Extension
						return "DWG_ext";
					}
				} else {
					// keine passende BOF gefunden ggf. Format mit nur einem Hex
					if ( Util.stringInFile( "multipart", checkFile ) ) {
						// C) fuer MHT auf SiF weiterkontrollieren
						// Eine MHT-Datei muss den String "multipart" im File
						// haben
						// MHT .mht, .mhtml M 4D multipart
						// D) passende Extension kontrollieren
						// Eine MHT-Datei muss die extension .mht, .mhtml haben
						if ( checkFileExt.equals( ".mht" )
								|| checkFileExt.equals( ".mhtml" ) ) {
							// eindeutig als MHT-Datei erkannt
							return "MHT";
						} else {
							// als MHT-Datei erkannt aber falsche Extension
							return "MHT_ext";
						}
					}
				}
			} else if ( sb2str1.contains( "4F" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 4f)

				if ( Magic.magicOgg( checkFile ) ) {
					// Eine OGG-Datei muss mit OggS.. [4F6767530002] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine OGG-Datei muss die extension .ogg haben
					if ( checkFileExt.equals( ".ogg" ) ) {
						// eindeutig als OGG-Datei erkannt
						return "OGG";
					} else {
						// als OGG-Datei erkannt aber falsche Extension
						return "OGG_ext";
					}
				}
			} else if ( sb2str1.contains( "50" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 50)

				if ( Magic.magicZip( checkFile ) ) {
					// Eine ZIP-Datei muss mit PK.. [504B0304] beginnen

					/*
					 * ZIP koennen riesig sein und dann geht viel Zeit verloren.
					 * Entsprechend wird hier die Dateiendung sehr schnell als
					 * Filter eingesetzt um Zeit zu sparen.
					 */
					if ( checkFileExt.equals( ".siard" ) ) {
						if ( Util.string15InFile( "content", "header",
								"metadata.xml", "metadata.xml", "metadata.xml",
								checkFile ) ) {
							// C) fuer SIARD auf SiF weiterkontrollieren
							// Eine SIARD-Datei muss den String "content",
							// "header",
							// "metadata.xml" im File haben
							return "SIARD";
						}
					} else if ( checkFileExt.equals( ".xlsx" ) ) {
						if ( Util.stringInFile( "workbook.xml.rels",
								checkFile ) ) {
							// C) fuer XLSX auf SiF weiterkontrollieren
							// Eine XLSX-Datei muss den String
							// "workbook.xml.rels"
							// im File haben
							return "XLSX";
						}
					} else if ( checkFileExt.equals( ".docx" ) ) {
						if ( Util.stringInFile( "document.xml.rels",
								checkFile ) ) {
							// C) fuer DOCX auf SiF weiterkontrollieren
							// Eine DOCX-Datei muss den String
							// "document.xml.rels"
							// im File haben
							return "DOCX";
						}
					} else if ( checkFileExt.equals( ".pptx" ) ) {
						if ( Util.stringInFile( "slide1.xml.rels",
								checkFile ) ) {
							// C) fuer PPTX auf SiF weiterkontrollieren
							// Eine PPTX-Datei muss den String
							// "slide1.xml.rels"
							// im File haben
							return "PPTX";
						}
					} else if ( checkFileExt.equals( ".ods" ) ) {
						if ( Util.stringInFile( "opendocument.spreadsheet",
								checkFile ) ) {
							// C) fuer ODS auf SiF weiterkontrollieren
							// Eine ODS-Datei muss den String
							// "opendocument.spreadsheet" im File haben
							return "ODS";
						}
					} else {
						// scheint eine normale ZIP-Datei zu sein
						// Eine ZIP-Datei muss die extension .zip haben
						if ( checkFileExt.equals( ".zip" ) ) {
							// eindeutig als ZIP-Datei erkannt
							return "ZIP";
						} else {
							// als ZIP-Datei erkannt aber falsche Extension
							return "ZIP_ext";
						}
					}
				}
			} else if ( sb2str1.contains( "52" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 52)

				if ( Magic.magicWave( checkFile ) ) {
					// Eine WAVE-Datei muss mit RIFF [52494646] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine WAVE-Datei muss die extension .wav haben
					if ( checkFileExt.equals( ".wav" ) ) {
						// eindeutig als WAVE-Datei erkannt
						return "WAVE";
					} else {
						// als WAVE-Datei erkannt aber falsche Extension
						return "WAVE_ext";
					}
				} else {
					// keine passende BOF gefunden ggf. Format ohne BOF (am
					// Schluss)
				}
			} else if ( sb2str1.contains( "53" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 53)

				if ( Magic.magicInterlis( checkFile ) ) {
					// BOF entspricht einer INTERLIS-Datei
					// INTERLIS .ili, .itf
					// D) passende Extension kontrollieren (kein SiF)
					// Eine solche INTERLIS-Datei muss die extension .ili, .itf
					// haben
					if ( checkFileExt.equals( ".ili" )
							|| checkFileExt.equals( ".itf" ) ) {
						// eindeutig als INTERLIS-Datei erkannt
						return "INTERLIS";
					} else {
						// als INTERLIS-Datei erkannt
						// aber falsche Extension
						return "INTERLIS_ext";
					}
				}
			} else if ( sb2str1.contains( "54" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 54)

				if ( Magic.magicInterlis( checkFile ) ) {
					// BOF entspricht einer INTERLIS-Datei
					// INTERLIS .ili, .itf
					// D) passende Extension kontrollieren (kein SiF)
					// Eine solche INTERLIS-Datei muss die extension .ili, .itf
					// haben
					if ( checkFileExt.equals( ".ili" )
							|| checkFileExt.equals( ".itf" ) ) {
						// eindeutig als INTERLIS-Datei erkannt
						return "INTERLIS";
					} else {
						// als INTERLIS-Datei erkannt
						// aber falsche Extension
						return "INTERLIS_ext";
					}
				}
			} else if ( sb2str1.contains( "57" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 57)

				if ( Magic.magicWarc( checkFile ) ) {
					// Eine WARC-Datei muss mit WARC [57415243] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine WARC-Datei muss die extension .warc haben
					if ( checkFileExt.equals( ".warc" ) ) {
						// eindeutig als WARC-Datei erkannt
						return "WARC";
					} else {
						// als WARC-Datei erkannt aber falsche Extension
						return "WARC_ext";
					}
				}
			} else if ( sb2str1.contains( "66" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 66)

				if ( Magic.magicFlac( checkFile ) ) {
					// Eine FLAC-Datei muss mit fLaC [664C6143] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine FLAC-Datei muss die extension .flac haben
					if ( checkFileExt.equals( ".flac" ) ) {
						// eindeutig als FLAC-Datei erkannt
						return "FLAC";
					} else {
						// als FLAC-Datei erkannt aber falsche Extension
						return "FLAC_ext";
					}
				} else if ( Magic.magicArc( checkFile ) ) {
					// Eine ARC-Datei muss mit filede [66696C656465] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine ARC-Datei muss die extension .arc haben
					if ( checkFileExt.equals( ".arc" ) ) {
						// eindeutig als ARC-Datei erkannt
						return "ARC";
					} else {
						// als ARC-Datei erkannt aber falsche Extension
						return "ARC_ext";
					}
				}
			} else if ( sb2str1.contains( "7B" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 7B)

				if ( Magic.magicRtf( checkFile ) ) {
					// Eine RTF Datei (.rtf) muss mit {\rtf [7B5C727466] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine RTF-Datei muss die extension .rtf haben
					if ( checkFileExt.equals( ".rtf" ) ) {
						// eindeutig als RTF-Datei erkannt
						return "RTF";
					} else {
						// als RTF-Datei erkannt aber falsche Extension
						return "RTF_ext";
					}
				}
			} else if ( sb2str1.contains( "89" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit 89)

				if ( Magic.magicPng( checkFile ) ) {
					// Eine PNG-Datei muss mit ‰PNG [89504E47] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine PNG-Datei muss die extension .png haben
					if ( checkFileExt.equals( ".png" ) ) {
						// eindeutig als PNG-Datei erkannt
						return "PNG";
					} else {
						// als PNG-Datei erkannt aber falsche Extension
						return "PNG_ext";
					}
				}
			} else if ( sb2str1.contains( "D0" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit D0)
				if ( Magic.magicMsOffice( checkFile ) ) {
					// Eine MS Office Datei 97-2003 muss mit [D0CF11E0A1B11AE1] beginnen

					// (.doc .xls .ppt .msi .msg) 
					// SiF nicht möglich wegen NUL [00] 
					if ( checkFileExt.equals( ".doc" ) ) {
						// eindeutig als DOC-Datei erkannt
						return "DOC";
					} else if ( checkFileExt.equals( ".xls" ) ) {
						// eindeutig als XLS-Datei erkannt
						return "XLS";
					} else if ( checkFileExt.equals( ".ppt" ) ) {
						// eindeutig als ppt-Datei erkannt
						return "PPT";
					} else if ( checkFileExt.equals( ".msi" ) ) {
						// eindeutig als MSI-Datei erkannt
						return "MSI";
					} else if ( checkFileExt.equals( ".msg" ) ) {
						// eindeutig als MSG-Datei erkannt
						return "MSG";
					} else {
						// andere MS Office 97-2003 Datei
						return "MsOffice";
					}
				}
			} else if ( sb2str1.contains( "FF" ) ) {
				// TODO B) Die moeglichen BOF kontrollieren (beginnt mit ff)

				if ( Magic.magicJpeg( checkFile ) ) {
					// Eine JPEG-Datei muss mit ÿØÿ [ffd8ff] beginnen
					// D) passende Extension kontrollieren (keine SiF)
					// Eine JPEG-Datei muss die extension .jpg, .jpeg, .jpe
					// haben
					if ( checkFileExt.equals( ".jpg" )
							|| checkFileExt.equals( ".jpeg" )
							|| checkFileExt.equals( ".jpe" ) ) {
						// eindeutig als JPEG-Datei erkannt
						return "JPEG";
					} else {
						// als JPEG-Datei erkannt aber falsche Extension
						return "JPEG_ext";
					}
				} else if ( Magic.magicMp3( checkFile ) ) {
					// BOF entspricht einer MP3-Datei (ÿû
					// FFFB ; ÿó FFF3 ; ÿú FFFA ; ÿò FFF2 ; ÿã FFE3
					// D) passende Extension kontrollieren (keine SiF)
					// Eine MP3-Datei muss die extension .mp3 haben
					if ( checkFileExt.equals( ".mp3" ) ) {
						// eindeutig als MP3-Datei erkannt
						return "MP3";
					} else {
						// als MP3-Datei erkannt aber falsche Extension
						return "MP3_ext";
					}
				} else if ( Magic.magicMp2( checkFile ) ) {
					// BOF entspricht einer MP2-Datei (ÿõ
					// FFF5 ; ÿô FFF4 ; ÿý FFFD ; ÿü FFFC
					// D) passende Extension kontrollieren (keine SiF)
					// Eine MP2-Datei muss die extension .mp2, .mpw, .mpa haben
					if ( checkFileExt.equals( ".mp2" )
							|| checkFileExt.equals( ".mpw" )
							|| checkFileExt.equals( ".mpa" ) ) {
						// eindeutig als MP2-Datei erkannt
						return "MP2";
					} else {
						// als MP3-Datei erkannt aber falsche Extension
						return "MP2_ext";
					}
				}

			} else {
				// kein bekanntes BOF
			}

			// TODO: Kein passendes BOF; ggf. Format mit Offset im BOF
			if ( Magic.magicAlac( checkFile ) ) {
				// Eine ALAC-Datei muss mit {4}ftypM4A [{4}667479704D3441]
				// beginnen !!! Offset 4 !!!
				if ( Util.stringInFile( "alac", checkFile ) ) {
					// C) fuer ALAC auf SiF weiterkontrollieren
					// Eine ALAC-Datei muss den String "alac" im
					// File haben
					// D) passende Extension kontrollieren
					// Eine ALAC-Datei muss die extension .mp4, m4a haben
					if ( checkFileExt.equals( ".mp4" )
							|| checkFileExt.equals( ".m4a" ) ) {
						// eindeutig als ALAC-Datei erkannt
						return "ALAC";
					} else {
						// als ALAC-Datei erkannt aber falsche Extension
						return "ALAC_ext";
					}
				}
			} else if ( Magic.magicMp4( checkFile ) ) {
				// Eine MP4-Datei muss mit {4}ftyp [{4}66747970]
				// beginnen !!! Offset 4 !!!
				if ( Util.string15InFile( "mp42", "mp41", "isom", "iso2",
						"iso2", checkFile ) ) {
					// C) fuer MP4 auf SiF weiterkontrollieren
					// Eine MP4-Datei muss den String "mp42 mp41 isom iso2" im
					// File haben
					// D) passende Extension kontrollieren
					// Eine MP4-Datei muss die extension .mp4, .mpg4, .m4v,
					// .m4a, .f4v, .f4a haben
					if ( checkFileExt.equals( ".mp4" )
							|| checkFileExt.equals( ".mpg4" )
							|| checkFileExt.equals( ".m4v" )
							|| checkFileExt.equals( ".m4a" )
							|| checkFileExt.equals( ".f4v" )
							|| checkFileExt.equals( ".f4a" ) ) {
						// eindeutig als MP4-Datei erkannt
						return "MP4";
					} else {
						// als MP4-Datei erkannt aber falsche Extension
						return "MP4_ext";
					}
				}
			} else if ( Magic.magicProres( checkFile ) ) {
				// Eine PRORES-Datei muss mit {4}free [{4}66726565]
				// beginnen !!! Offset 4 !!!
				if ( Util.stringInFile( "icpf", checkFile ) ) {
					// C) fuer PRORES auf SiF weiterkontrollieren
					// Eine PRORES-Datei muss den String "icpf" im
					// File haben
					// D) passende Extension kontrollieren
					// Eine PRORES-Datei muss die extension .mov haben
					if ( checkFileExt.equals( ".mov" ) ) {
						// eindeutig als PRORES-Datei erkannt
						return "PRORES";
					} else {
						// als PRORES-Datei erkannt aber falsche Extension
						return "PRORES_ext";
					}
				}
			} else if ( Magic.magicQtm( checkFile ) ) {
				// Eine QTM-Datei muss mit {4}moov [{4}6D6F6F76]
				// beginnen !!! Offset 4 !!!
				if ( Util.string15InFile( "mvhd", "cmov", "rmra", "rmra",
						"rmra", checkFile ) ) {
					// C) fuer QTM auf SiF weiterkontrollieren
					// Eine QTM-Datei muss den String "mvhd cmov rmra" im
					// File haben
					// D) passende Extension kontrollieren
					// Eine QTM-Datei muss die extension .mov haben
					if ( checkFileExt.equals( ".mov" ) ) {
						// eindeutig als QTM-Datei erkannt
						return "QTM";
					} else {
						// als QTM-Datei erkannt aber falsche Extension
						return "QTM_ext";
					}
				}
			} else if ( Magic.magicDicom( checkFile ) ) {
				// Eine Dicom-Datei (keine Dateiendung noetig) muss mit
				// {128}4449434D beginnen !!! Offset 128 !!!
				// hat auf keine SiF oder Dateiendung
				return "DICOM";
			}

			// TODO: Kein passendes BOF; ggf. Format ohne BOF
			if ( checkFileExt.equals( ".txt" ) || checkFileExt.equals( ".ans" )
					|| checkFileExt.equals( ".asc" ) ) {
				// Eine TXT-Datei muss die extension .txt, .ans, .asc haben
				return "TXT";
			} else if ( checkFileExt.equals( ".csv" ) ) {
				// Eine CSV-Datei muss die extension .csv haben
				return "CSV";
			} else if ( checkFileExt.equals( ".dxf" ) ) {
				// Eine DXF-Datei muss die extension .dxf haben
				return "DXF";
			} else if ( checkFileExt.equals( ".eps" ) ) {
				// Eine EPS-Datei muss die extension .eps haben
				return "EPS";
			} else if ( checkFileExt.equals( ".ai" ) ) {
				// Eine AI-Datei muss die extension .ai haben
				return "AI";
			} else if ( checkFileName.equals( "Thumbs.db" ) ) {
				// Eine Thumbs.db-Datei
				return "Thumbs.db";
			}	else if ( checkFileExt.equals( ".json" )&& Util.stringInFile( "{",
					checkFile )&& Util.stringInFile( "}",
					checkFile ) ) {
				// Eine JSON Datei (.json) beginnt in der Regel mit { (7B) 
				// und enthaelt auch } alternativ [ (5B) respektive ]
				// als JSON-Datei erkannt (keine SiF)
				return "JSON";
			}	else if ( checkFileExt.equals( ".json" )&& Util.stringInFile( "[",
						checkFile )&& Util.stringInFile( "]",
						checkFile ) ) {
					// Eine JSON Datei (.json) beginnt in der Regel mit { (7B) 
					// und enthaelt auch } alternativ [ (5B) respektive ]
					// als JSON-Datei erkannt (keine SiF)
					return "JSON";
			} else {
				// System.out.println("kein bekannte Archivformat erkannt " +
				// checkFileExt );
				return "UNKNOWN_" + extUpperCase;
			}

		} catch ( Exception e ) {
			// System.out.println("Exception magic file recognition: " +
			// e.getMessage() );
			return "ERROR_recFile";
		}
	}
}
