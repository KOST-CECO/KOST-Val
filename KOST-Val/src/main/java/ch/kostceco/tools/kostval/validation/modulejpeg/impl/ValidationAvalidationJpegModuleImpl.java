/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
 * -----------------------------------------------------------------------------------------------
 * KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all copyright
 * interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March
 * 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostval.validation.modulejpeg.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.exception.modulejpeg.ValidationAjpegvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejpeg.ValidationAvalidationJpegModule;
import coderslagoon.badpeggy.scanner.ImageFormat;
import coderslagoon.badpeggy.scanner.ImageScanner;
import coderslagoon.badpeggy.scanner.ImageScanner.Callback;

/**
 * Ist die vorliegende JPEG-Datei eine valide JPEG-Datei? JPEG Validierungs mit
 * BadPeggy.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * BadPeggy.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 * @author Markus Hahn, coderslagoon
 */

public class ValidationAvalidationJpegModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationJpegModule, Callback
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws ValidationAjpegvalidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		// Die Erkennung erfolgt bereits im Vorfeld

		boolean isValid = true;

		// TODO: Erledigt: JPEG Validierung

		/*
		 * Code schnippsel erhalten von M. Hahn: ImageScannerDemo
		 * 
		 * und umgeschrieben durch C. Roethlisberger auf KOST-Val
		 */

		// determine if the file format is known by the scanner
		File fl = valDatei;
		ImageFormat ifmt = ImageFormat.fromFileName( fl.getName() );
		if ( null == ifmt ) {
			if ( min ) {
				return false;
			} else {
				// System.err.println( "file type not supported" );
				// invalide

				Logtxt.logtxt( logFile, getTextResourceService()
						.getText( locale, MESSAGE_XML_MODUL_A_JPEG )
						+ getTextResourceService().getText( locale,
								MESSAGE_XML_SERVICEINVALID, "BadPeggy", "" ) );

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_JPEG )
								+ getTextResourceService().getText( locale,
										ERROR_XML_A_JPEG_JIIO_FILETYPE ) );
				isValid = false;
				return isValid;
			}
		}

		// open the file
		try {
			InputStream is = new FileInputStream( valDatei );

			// scan the file, the return value just tells us good or bad or ...
			ImageScanner iscan = new ImageScanner();
			Boolean ok = iscan.scan( is, ifmt, this );
			if ( null == ok ) {
				// ... that the scanner itself could not do its job at all
				if ( min ) {
					return false;
				} else {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JPEG )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_SERVICEINVALID,
											"BadPeggy", "" ) );

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( locale,
									ERROR_XML_A_JPEG_JIIO_SCANFAILED ) );
					// invalide
					isValid = false;
					return isValid;
				}
			}
			if ( ok ) {
				// valide
				isValid = true;
			} else {
				// invalide oder Warnung

				ImageScanner.Result ires = iscan.lastResult();

				// display the scanner's log messages
				for ( String msg : ires.collapsedMessages() ) {
					// Warnung abfangen
					if ( msg.startsWith( "Unsupported Image Type" ) ) {
						if ( !min ) {
							// Unsupported Image Type => ERROR_XML_A_UNS_IMAGE,
							// msg
							msg = msg.substring( 19 );

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_JPEG )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_UNS_IMAGE, msg ) );
						}
					} else {
						// invalide
						if ( min ) {
							return false;
						} else {

							if ( isValid ) {
								// Erster Fehler! Meldung A ausgeben und invalid
								// setzten

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_A_JPEG )
										+ getTextResourceService().getText(
												locale,
												MESSAGE_XML_SERVICEINVALID,
												"BadPeggy", "" ) );
								isValid = false;
							}

							// msg Not a JPEG file: starts with ... weiter
							// analysieren

							/*
							 * Fehlermeldung "Not a JPEG file: starts with ..."
							 * weiter analysieren. Meist ist zwischen zwei EOI
							 * von Thumbnails kein SOI vorhanden. Ist letzteres
							 * der Fall muss ein EOI-EOI-Fehler ausgegeben
							 * werden.
							 */
							// if SOI+ > EOI -> Invalid JPEG file structure:
							// missing EOI marker
							// else if SOI+ < EOI -> Invalid JPEG file
							// structure: missing SOI marker
							// else
							// if EOI > 2 (zwei oder mehrere Thumbnails)
							// Analysieren ob zwischen den EOI der Thumbnails
							// ein SOI ist
							// . (ggf Wiederholen bei mehrals 2 Thumbnails)
							// . SOI = 0 . -> Invalid JPEG file structure:
							// missing SOI between two EOI thumbnail
							// markers
							// . else -> Orinignalmeldung ausgegeben
							// else -> Orinignalmeldung ausgegeben

							if ( msg.startsWith(
									"Not a JPEG file: starts with" ) ) {
								isValid = false;

								// Detailanalyse //

								// SOI+ = FFD8FF (c1c2c1) und EOI = FFD9 (c1c3)

								// Hex FF in Char umwandeln
								String str1 = "FF";
								int i1 = Integer.parseInt( str1, 16 );
								char c1 = (char) i1;
								// Hex D8 in Char umwandeln
								String str2 = "D8";
								int i2 = Integer.parseInt( str2, 16 );
								char c2 = (char) i2;
								// Hex D9 in Char umwandeln
								String str3 = "D9";
								int i3 = Integer.parseInt( str3, 16 );
								char c3 = (char) i3;
								boolean bc1 = false;
								boolean bd8 = false;
								long iSOI1 = -1;
								long iEOI1 = -1;
								long iSOI2 = -1;
								long iEOI2 = -1;
								long iSOI3 = -1;
								long iEOI3 = -1;
								long iSOI4 = -1;
								long iEOI4 = -1;
								long iSOI5 = -1;
								long iEOI5 = -1;
								long iSOI6 = -1;
								long iEOI6 = -1;
								long iSOI7 = -1;
								long iEOI7 = -1;
								long iSOI8 = -1;
								long iEOI8 = -1;
								long iSOI9 = -1;
								long iEOI9 = -1;

								Reader r = new BufferedReader(
										new InputStreamReader(
												new FileInputStream(
														valDatei ) ) );
								char character;
								long c = 0;
								long intch;

								while ( (intch = r.read()) != -1 ) {
									character = (char) intch;
									c = c + 1;
									if ( !bc1 ) {
										// character=c1?
										if ( character == c1 ) {
											// das Zeichen ist ein FF
											bc1 = true;
										}
									} else {
										if ( !bd8 ) {
											// character=c2 oder character=c3
											if ( character == c2 ) {
												// FFD8
												bd8 = true;
											} else if ( character == c3 ) {
												// FFD9 =EOI
												if ( iEOI1 == -1 ) {
													// das ist das erste EOI
													iEOI1 = c;
													// System.out.println( "das
													// ist das 1. EOI: " + c );
												} else if ( iEOI2 == -1 ) {
													// das ist das 2. EOI
													iEOI2 = c;
													// System.out.println( "das
													// ist das 2. EOI: " + c );
												} else if ( iEOI3 == -1 ) {
													// das ist das 3. EOI
													iEOI3 = c;
													// System.out.println( "das
													// ist das 3. EOI: " + c );
												} else if ( iEOI4 == -1 ) {
													// das ist das 4. EOI
													iEOI4 = c;
													// System.out.println( "das
													// ist das 4. EOI: " + c );
												} else if ( iEOI5 == -1 ) {
													// das ist das 5. EOI
													iEOI5 = c;
													// System.out.println( "das
													// ist das 5. EOI: " + c );
												} else if ( iEOI6 == -1 ) {
													// das ist das 6. EOI
													iEOI6 = c;
													// System.out.println( "das
													// ist das 6. EOI: " + c );
												} else if ( iEOI7 == -1 ) {
													// das ist das 7. EOI
													iEOI7 = c;
													// System.out.println( "das
													// ist das 7. EOI: " + c );
												} else if ( iEOI8 == -1 ) {
													// das ist das 8. EOI
													iEOI8 = c;
													// System.out.println( "das
													// ist das 8. EOI: " + c );
												} else {
													// das ist das 9. EOI oder
													// mehr
													iEOI9 = 999999;
													// System.out.println( "das
													// ist das 9+. EOI: " + c );
												}
												bc1 = false;
											} else {
												// FF nicht gefolgt von D8 oder
												// D9
												bc1 = false;
											}
										} else {
											// bis jetzt FFD8
											// character=c1?
											if ( character == c1 ) {
												// FFD8FF =SOI+
												if ( iSOI1 == -1 ) {
													// das ist das erste SOI
													iSOI1 = c;
													// System.out.println( "das
													// ist das 1. SOI: " + c );
												} else if ( iSOI2 == -1 ) {
													// das ist das 2. SOI
													iSOI2 = c;
													// System.out.println( "das
													// ist das 2. SOI: " + c );
												} else if ( iSOI3 == -1 ) {
													// das ist das 3. SOI
													iSOI3 = c;
													// System.out.println( "das
													// ist das 3. SOI: " + c );
												} else if ( iSOI4 == -1 ) {
													// das ist das 4. SOI
													iSOI4 = c;
													// System.out.println( "das
													// ist das 4. SOI: " + c );
												} else if ( iSOI5 == -1 ) {
													// das ist das 5. SOI
													iSOI5 = c;
													// System.out.println( "das
													// ist das 5. SOI: " + c );
												} else if ( iSOI6 == -1 ) {
													// das ist das 6. SOI
													iSOI6 = c;
													// System.out.println( "das
													// ist das 6. SOI: " + c );
												} else if ( iSOI7 == -1 ) {
													// das ist das 7. SOI
													iSOI7 = c;
													// System.out.println( "das
													// ist das 7. SOI: " + c );
												} else if ( iSOI8 == -1 ) {
													// das ist das 8. SOI
													iSOI8 = c;
													// System.out.println( "das
													// ist das 8. SOI: " + c );
												} else {
													// das ist das 9. SOI oder
													// mehr
													iSOI9 = 999999;
													// System.out.println( "das
													// ist das 9+. SOI: " + c );
												}
												bc1 = false;
												bd8 = false;
											} else {
												// FFD8 nicht gefolgt von FF
												bc1 = false;
												bd8 = false;
											}
										}
									}

								}
								r.close();
								int cSOI = 0;
								int cEOI = 0;

								// Auswerten wie viele SOI marker vorhanden
								if ( iSOI9 > -1 ) {
									cSOI = 9;
								} else if ( iSOI8 > -1 ) {
									cSOI = 8;
								} else if ( iSOI7 > -1 ) {
									cSOI = 7;
								} else if ( iSOI6 > -1 ) {
									cSOI = 6;
								} else if ( iSOI5 > -1 ) {
									cSOI = 5;
								} else if ( iSOI4 > -1 ) {
									cSOI = 4;
								} else if ( iSOI3 > -1 ) {
									cSOI = 3;
								} else if ( iSOI2 > -1 ) {
									cSOI = 2;
								} else if ( iSOI1 > -1 ) {
									cSOI = 1;
								} else {
									// Invalid JPEG file structure: no SOI
									// marker
									msg = "No SOI marker";
								}
								// Auswerten wie viele EOI marker vorhanden
								if ( iEOI9 > -1 ) {
									cEOI = 9;
								} else if ( iEOI8 > -1 ) {
									cEOI = 8;
								} else if ( iEOI7 > -1 ) {
									cEOI = 7;
								} else if ( iEOI6 > -1 ) {
									cEOI = 6;
								} else if ( iEOI5 > -1 ) {
									cEOI = 5;
								} else if ( iEOI4 > -1 ) {
									cEOI = 4;
								} else if ( iEOI3 > -1 ) {
									cEOI = 3;
								} else if ( iEOI2 > -1 ) {
									cEOI = 2;
								} else if ( iEOI1 > -1 ) {
									cEOI = 1;
								} else {
									if ( cSOI == 0 ) {
										// Invalid JPEG file structure: no SOI
										// and EOI marker
										msg = "No SOI and EOI marker";
									} else {
										// Invalid JPEG file structure: no EOI
										// marker
										msg = "No EOI marker";
									}
								}

								if ( cSOI != 0 | cEOI != 0 ) {
									// Analysieren ob zwischen den Thumbnails
									// EOI ein SOI ist

									// Korrekte Struktur mit 3 Thumbnais zur
									// Veranschaulichung

									// � iSOI1
									// �
									// � _ �iSOI2
									// � _ �iEOI1
									// �
									// � _ �iSOI3
									// � _ �iEOI2
									// �
									// � _ �iSOI4
									// � _ �iEOI3
									// �
									// � iEOI4

									if ( cEOI > 2 ) {
										// mindestens 2 Thumbnais vorhanden ->
										// weiter analysieren
										if ( iEOI1 > iSOI3 || iEOI2 < iSOI3
												|| iEOI2 > iSOI4
												|| iEOI3 < iSOI4
												|| iEOI3 > iSOI5
												|| iEOI4 < iSOI5
												|| iEOI4 > iSOI6
												|| iEOI5 < iSOI6
												|| iEOI5 > iSOI7
												|| iEOI6 < iSOI7
												|| iEOI6 > iSOI8
												|| iEOI7 < iSOI8
												|| iEOI7 > iSOI9
												|| iEOI8 < iSOI9 ) {
											// Invalid JPEG file structure:
											// missing SOI between two EOI
											// thumbnail markers
											msg = "Missing SOI between two EOI thumbnail markers";
										} else {
											// SOI = EOI ?
											if ( cSOI > cEOI ) {
												// Invalid JPEG file structure:
												// missing EOI marker
												msg = "Missing EOI marker";
											} else if ( cSOI < cEOI ) {
												// Invalid JPEG file structure:
												// missing SOI marker
												msg = "Missing SOI marker";
											}
										}
									} else {
										// SOI = EOI ?
										if ( cSOI > cEOI ) {
											// Invalid JPEG file structure:
											// missing EOI marker
											msg = "Missing EOI marker";
										} else if ( cSOI < cEOI ) {
											// Invalid JPEG file structure:
											// missing SOI marker
											msg = "Missing SOI marker";
										}
									}
								}
							}

							if ( msg.startsWith(
									"Corrupt JPEG data: premature end of data segment" ) ) {
								isValid = false;

								// Corrupt JPEG data: premature end of data
								// segment => ERROR_XML_A_HIT_MARKER, msg -
								// "Corrupt JPEG data: "
								msg = msg.substring( 19 );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_A_HIT_MARKER,
												msg ) );
							} else if ( msg.startsWith(
									"Corrupt JPEG data: found marker " ) ) {
								// Corrupt JPEG data: found marker 0x%02x
								// instead of RST%d =>
								// ERROR_XML_A_MUST_RESYNC,
								// msg - "Corrupt JPEG data: "
								msg = msg.substring( 19 );
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_A_MUST_RESYNC,
												msg ) );
							} else if ( msg.startsWith(
									"Corrupt JPEG data: bad arithmetic code" ) ) {
								// Corrupt JPEG data: bad arithmetic code =>
								// ERROR_XML_A_ARITH_BAD_CODE, msg -
								// "Corrupt JPEG data: "
								msg = msg.substring( 19 );
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_ARITH_BAD_CODE,
												msg ) );
							} else if ( msg.startsWith(
									"Corrupt JPEG data: bad Huffman code" ) ) {
								// Corrupt JPEG data: bad Huffman code =>
								// ERROR_XML_A_HUFF_BAD_CODE, msg -
								// "Corrupt JPEG data: "
								msg = msg.substring( 19 );
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_HUFF_BAD_CODE,
												msg ) );
							} else if ( msg
									.startsWith( "Corrupt JPEG data:" ) ) {
								// Alle anderen "Corrupt JPEG data:" bereits
								// abgefangen...
								// Corrupt JPEG data: %u extraneous bytes before
								// marker 0x%02x =>
								// ERROR_XML_A_EXTRANEOUS_DATA, msg - "Corrupt
								// JPEG data: "
								msg = msg.substring( 19 );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_EXTRANEOUS_DATA,
												msg ) );
							} else if ( msg.startsWith(
									"Warning: thumbnail image size does not match data length" ) ) {
								// Warning: thumbnail image size does not match
								// data length %u =>
								// ERROR_XML_A_BADTHUMBNAILSIZE, msg - "Warning:
								// "
								msg = msg.substring( 9 );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_BADTHUMBNAILSIZE,
												msg ) );
							} else if ( msg.startsWith(
									"Premature end of input file" ) ) {
								// Premature end of input file =>
								// ERROR_XML_A_INPUT_EOF, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_A_INPUT_EOF,
												msg ) );
							} else if ( msg.startsWith(
									"Premature end of JPEG file" ) ) {
								// Premature end of JPEG file =>
								// ERROR_XML_A_JPEG_EOF, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_A_JPEG_EOF,
												msg ) );
							} else if ( msg.startsWith(
									"Missing Huffman code table entry" ) ) {
								// Missing Huffman code table entry =>
								// ERROR_XML_A_HUFF_MISSING_CODE, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_A_HUFF_MISSING_CODE,
												msg ) );
							} else if ( msg.startsWith(
									"JPEG datastream contains no image" ) ) {
								// JPEG datastream contains no image =>
								// ERROR_XML_A_NO_IMAGE, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_A_NO_IMAGE,
												msg ) );

								// Modul C
							} else if ( msg.startsWith(
									"Invalid JPEG file structure: SOS before SOF" ) ) {
								// Invalid JPEG file structure: SOS before SOF
								// => ERROR_XML_B_SOS_NO_SOF, msg -
								// "Invalid JPEG file structure: "
								msg = msg.substring( 29 );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_SOS_NO_SOF,
												msg ) );
							} else if ( msg.startsWith(
									"Invalid JPEG file structure: two SOI markers" ) ) {
								// Invalid JPEG file structure: two SOI markers
								// => ERROR_XML_B_SOI_DUPLICATE, msg -
								// "Invalid JPEG file structure: "
								msg = msg.substring( 29 );
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_SOI_DUPLICATE,
												msg ) );
							} else if ( msg.startsWith(
									"Invalid JPEG file structure: missing SOS marker" ) ) {
								// Invalid JPEG file structure: missing SOS
								// marker => ERROR_XML_B_SOF_NO_SOS, msg -
								// "Invalid JPEG file structure: "
								msg = msg.substring( 29 );

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_SOF_NO_SOS,
												msg ) );
							} else if ( msg.startsWith(
									"Invalid JPEG file structure: two SOF markers" ) ) {
								// Invalid JPEG file structure: two SOF markers
								// => ERROR_XML_B_SOF_DUPLICATE, msg -
								// "Invalid JPEG file structure: "
								msg = msg.substring( 29 );
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_SOF_DUPLICATE,
												msg ) );
							} else if ( msg.startsWith(
									"Invalid SOS parameters for sequential JPEG" ) ) {
								// Invalid SOS parameters for sequential JPEG =>
								// ERROR_XML_B_NOT_SEQUENTIAL, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_NOT_SEQUENTIAL,
												msg ) );
							} else if ( msg.startsWith(
									"Not a JPEG file: starts with" ) ) {
								// Not a JPEG file: starts with 0x%02x 0x%02x =>
								// ERROR_XML_B_NO_SOI, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_NO_SOI,
												msg ) );
							} else if ( msg
									.startsWith( "No SOI and EOI marker" ) ) {
								// No SOI and EOI marker =>
								// ERROR_XML_B_KC_NO_SOI_EOI, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_KC_NO_SOI_EOI,
												msg ) );
							} else if ( msg.startsWith( "No SOI marker" ) ) {
								// No SOI marker => ERROR_XML_B_KC_NO_SOI, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_KC_NO_SOI,
												msg ) );
							} else if ( msg.startsWith( "No EOI marker" ) ) {
								// No EOI marker => ERROR_XML_B_KC_NO_EOI, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_KC_NO_EOI,
												msg ) );
							} else if ( msg
									.startsWith( "Missing SOI marker" ) ) {
								// Missing SOI marker =>
								// ERROR_XML_B_KC_MISS_SOI, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_KC_MISS_SOI,
												msg ) );
							} else if ( msg
									.startsWith( "Missing EOI marker" ) ) {
								// Missing EOI marker =>
								// ERROR_XML_B_KC_MISS_EOI, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_KC_MISS_EOI,
												msg ) );
							} else if ( msg.startsWith(
									"Missing SOI between two EOI thumbnail markers" ) ) {
								// Missing SOI between two EOI thumbnail markers
								// => ERROR_XML_B_KC_EOI_EOI, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_KC_EOI_EOI,
												msg ) );
							} else if ( msg
									.startsWith( "Empty JPEG image " ) ) {
								// Empty JPEG image (DNL not supported) =>
								// ERROR_XML_B_EMPTY_IMAGE, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_EMPTY_IMAGE,
												msg ) );
							} else if ( msg
									.startsWith( "Invalid component ID " ) ) {
								// Invalid component ID %d in SOS =>
								// ERROR_XML_B_BAD_COMPONENT_ID, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_BAD_COMPONENT_ID,
												msg ) );
							} else if ( msg
									.startsWith( "Arithmetic table " ) ) {
								// Arithmetic table 0x%02x was not defined =>
								// ERROR_XML_B_NO_ARITH_TABLE, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_NO_ARITH_TABLE,
												msg ) );
							} else if ( msg.startsWith( "Huffman table " ) ) {
								// Huffman table 0x%02x was not defined =>
								// ERROR_XML_B_NO_HUFF_TABLE, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_NO_HUFF_TABLE,
												msg ) );
							} else if ( msg.startsWith(
									"Truncated File - Missing EOI marker" ) ) {
								// Truncated File - Missing EOI marker =>
								// ERROR_XML_B_NO_EOI, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_NO_EOI,
												msg ) );
							} else if ( msg.startsWith(
									"JFIF markers not allowed in JFIF JPEG thumbnail" ) ) {
								// JFIF markers not allowed in JFIF JPEG
								// thumbnail; ignored =>
								// ERROR_XML_B_NO_JFIF_IN_THUMB, msg

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_C_JPEG )
										+ getTextResourceService().getText(
												locale,
												ERROR_XML_B_NO_JFIF_IN_THUMB,
												msg ) );
							} else if ( msg.startsWith(
									"Embedded color profile is invalid" ) ) {
								// Embedded color profile is invalid; ignored =>
								// ERROR_XML_B_INVALID_ICC, msg
								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_B_INVALID_ICC,
												msg ) );

								// Modul D
							} else {
								// Fehlermeldung noch nicht übersetzt und
								// zugeordnet

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_D_JPEG )
										+ getTextResourceService().getText(
												locale, ERROR_XML_C_TRANSLATE,
												msg ) );

								/*
								 * TODO: folgende Fehlermeldungen sind es
								 * bereits:
								 * 
								 * momentan keine offenen :-)
								 */

							}
						}
					}
				}
			}
			is.close();
		} catch ( IOException ioe ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_SERVICEINVALID, "BadPeggy" ) );

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( locale,
									ERROR_XML_SERVICEFAILED_EXIT, "BadPeggy",
									ioe.getMessage() ) );
			isValid = false;
			return isValid;
		}

		return isValid;
	}

	@Override
	public boolean onProgress( float percent )
	{
		// Muss auf return true sein, da ansonsten BadPeggy nicht funktioniert
		return true;
	}
}
