/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationJimageValidationModule;
import coderslagoon.badpeggy.scanner.ImageFormat;
import coderslagoon.badpeggy.scanner.ImageScanner;
import coderslagoon.badpeggy.scanner.ImageScanner.Callback;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/** Enthält die vorliegende PDF-Datei valide JP2- und JPEG-Bilder? Sind JBIG2-Bilder im PDF
 * enthalten? Die Bildextraktion erfolgt mit iText.
 * 
 * Danach erfolgt eine optionale (konfigurierbar) Bildvalidierung (JP2- und JPEG-Validierung, sowie
 * eine Fehlermeldung, wenn JBIG2 enthalten ist.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationJimageValidationModuleImpl extends ValidationModuleImpl implements
		ValidationJimageValidationModule
{
	boolean												isValidJPEG		= true;
	boolean												isValidJP2		= true;
	boolean												isValidJBIG2	= true;

	String												invalidJPEG		= "";
	String												invalidJP2		= "";
	int														jpegCounter		= 0;
	int														jp2Counter		= 0;
	int														jbig2Counter	= 0;
	String												jbig2Obj			= "";
	private ConfigurationService	configurationService;

	public static String					NEWLINE				= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationApdfvalidationException
	{
		boolean valid = true;
		boolean isAllowedJBIG2 = false;

		// Optionale Bildvalidierung eingeschaltet?
		String pdfaImage = getConfigurationService().pdfaimage();
		if ( pdfaImage.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA ) + pdfaImage );
			return false;
		}
		String jbig2Allowed = getConfigurationService().jbig2allowed();
		if ( jbig2Allowed.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA ) + jbig2Allowed );
			return false;
		}
		if ( jbig2Allowed.equalsIgnoreCase( "yes" ) ) {
			// JBIG2 ist erlaubt
			isAllowedJBIG2 = true;
		}
		if ( pdfaImage.equalsIgnoreCase( "yes" ) || !isAllowedJBIG2 ) {
			// Optionale Bildvalidierung eingeschaltet und oder JBIG2 nicht erlaubt

			// Informationen zum Arbeitsverzeichnis holen
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();

			String srcPdf = valDatei.getAbsolutePath();
			String destImage = pathToWorkDir + File.separator + valDatei.getName();

			String pathToLogDir = getConfigurationService().getPathToLogfile();

			File encrypt = new File( pathToLogDir + File.separator + valDatei.getName() + "_encrypt.txt" );

			if ( encrypt.exists() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
								+ getTextResourceService().getText( ERROR_XML_J_ENCRYPT ) );
				valid = false;
				Util.deleteFile( encrypt );
			} else {
				Util.deleteFile( encrypt );
				try {
					extractImages( srcPdf, destImage );
				} catch ( DocumentException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				} catch ( IOException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				}
				if ( isValidJPEG && isValidJP2 && isValidJBIG2 ) {
					// Bildvalidierung bestanden
					valid = true;
				} else {
					if ( pdfaImage.equalsIgnoreCase( "yes" ) && !isValidJPEG ) {
						// eingeschaltete Bildvalidierung nicht bestanden
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText( ERROR_XML_J_INVALID_JPEG, invalidJPEG,
												jpegCounter ) );
						valid = false;
					}
					if ( pdfaImage.equalsIgnoreCase( "yes" ) && !isValidJP2 ) {
						// eingeschaltete Bildvalidierung nicht bestanden
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText( ERROR_XML_J_INVALID_JP2, invalidJP2,
												jp2Counter ) );
						valid = false;
					}
					if ( !isValidJBIG2 && !isAllowedJBIG2 ) {
						// PDF enthält JBIG2, welches nicht erlaubt ist
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
												+ getTextResourceService().getText( ERROR_XML_J_JBIG2, jbig2Counter,
														jbig2Obj ) );
						valid = false;
					}
				}
			}
		} else {
			// keine Bildvalidierung und JBIG2 erlaubt
			String pathToLogDir = getConfigurationService().getPathToLogfile();
			File encrypt = new File( pathToLogDir + File.separator + valDatei.getName() + "_encrypt.txt" );

			if ( encrypt.exists() ) {
				/* getMessageService().logError( getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA
				 * ) + getTextResourceService().getText( ERROR_XML_J_ENCRYPT ) ); */
				valid = false;
				Util.deleteFile( encrypt );
			}
			valid = true;
		}

		return valid;
	}

	/** TODO: Parses a PDF and extracts all the images.
	 * 
	 * @param src
	 *          the source PDF
	 * @param dest
	 *          the resulting PDF */
	public void extractImages( String srcPdf, String destImage ) throws IOException,
			DocumentException
	{
		try {
			PdfReader reader = new PdfReader( srcPdf );
			PdfReaderContentParser parser = new PdfReaderContentParser( reader );
			MyImageRenderListener listener = new MyImageRenderListener( destImage );
			for ( int i = 1; i <= reader.getNumberOfPages(); i++ ) {
				parser.processContent( i, listener );
			}
			reader.close();

		} catch ( OutOfMemoryError e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
							+ getTextResourceService().getText( ERROR_XML_OUTOFMEMORYERROR ) );
		} catch ( IOException e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
	}

	public class MyImageRenderListener implements RenderListener, Callback
	{
		String	path	= "";

		/** Creates a RenderListener that will look for images. */
		public MyImageRenderListener( String path )
		{
			this.path = path;
		}

		public void beginTextBlock()
		{
		}

		public void endTextBlock()
		{
		}

		public void renderImage( ImageRenderInfo renderInfo )
		{
			try {
				String filename;
				File filePath = new File( path );
				String filenamePath = filePath.getName();
				String pathToLogDir = getConfigurationService().getPathToLogfile();
				String pdfaImage = getConfigurationService().pdfaimage();
				boolean delFile = true;
				FileOutputStream os;
				PdfImageObject image = renderInfo.getImage();

				PdfName filter = PdfName.CCITTFAXDECODE;
				try {
					PdfObject myObj = image.get( PdfName.FILTER );
					myObj = PdfReader.getPdfObject( myObj );
					if ( myObj instanceof PdfName ) {
						filter = (PdfName) myObj;
					}

					if ( PdfName.DCTDECODE.equals( filter ) ) {
						/* JPEG Bild:
						 * 
						 * Das JPEG wird im Logverzeichnis unter dem [PDF-Name].Obj[objNr].jpg gespeichert,
						 * falls Bildvalidierung eingeschaltet ist. */
						if ( pdfaImage.equalsIgnoreCase( "yes" ) ) {
							filename = pathToLogDir + File.separator + filenamePath + ".Obj"
									+ renderInfo.getRef().getNumber() + ".jpg";
							os = new FileOutputStream( filename );
							os.write( image.getImageAsBytes() );
							os.flush();

							// JPEG-Validierung: Start
							File fl = new File( filename );
							ImageFormat ifmt = ImageFormat.fromFileName( fl.getName() );
							if ( null == ifmt ) {
								// System.err.println( "file type not supported" );
								// invalide
								invalidJPEG = invalidJPEG + "   " + filename;
								isValidJPEG = false;
								delFile = false;
								jpegCounter = jpegCounter + 1;
							}

							// open the file
							if ( delFile ) {
								try {
									InputStream is = new FileInputStream( fl );

									// scan the file, the return value just tells us good or bad or ...
									ImageScanner iscan = new ImageScanner();
									Boolean ok = iscan.scan( is, ifmt, this );
									if ( null == ok ) {
										// ... that the scanner itself could not do its job at all
										// invalide
										isValidJPEG = false;
										delFile = false;
										invalidJPEG = invalidJPEG + "   " + filename;
										jpegCounter = jpegCounter + 1;
									}
									if ( ok ) {
										// valide -> isValidJPEG bleibt unverändert
									} else {
										// invalide oder Warnung

										ImageScanner.Result ires = iscan.lastResult();

										// display the scanner's log messages
										for ( String msg : ires.collapsedMessages() ) {
											// Warnung abfangen
											if ( msg.startsWith( "Unsupported Image Type" ) ) {
												// Unsupported Image Type => ERROR_XML_A_UNS_IMAGE, msg

												// nur Warnung, könnte valide sein
											} else {
												// invalide
												isValidJPEG = false;
											}
										}
										if ( !isValidJPEG ) {
											delFile = false;
											invalidJPEG = invalidJPEG + "   " + filename;
											jpegCounter = jpegCounter + 1;
										}
									}
									is.close();
								} catch ( IOException ioe ) {
									getMessageService().logError(
											getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
													+ getTextResourceService().getText( ERROR_XML_A_JPEG_SERVICEFAILED,
															ioe.getMessage() ) );
									isValidJPEG = false;
									delFile = false;
									invalidJPEG = invalidJPEG + "   " + filename;
									jpegCounter = jpegCounter + 1;
								}
							}
							// JPEG-Validierung: Ende
							if ( delFile ) {
								// Validierung diese Bildes bestanden. Das Bild wird aus log gelöscht.
								Util.deleteFile( fl );
							}
							os.close();
						}
					} else if ( PdfName.JPXDECODE.equals( filter ) ) {
						/* JP2 Bild:
						 * 
						 * Das JP2 wird im Logverzeichnis unter dem [PDF-Name].Obj[objNr].jp2 gespeichert,
						 * falls Bildvalidierung eingeschaltet ist. */
						if ( pdfaImage.equalsIgnoreCase( "yes" ) ) {
							filename = pathToLogDir + File.separator + filenamePath + ".Obj"
									+ renderInfo.getRef().getNumber() + ".jp2";
							File fl = new File( filename );
							os = new FileOutputStream( filename );
							os.write( image.getImageAsBytes() );
							os.flush();

							// Erkennung JP2 oder JPX
							FileReader fr = null;

							fr = new FileReader( fl );
							BufferedReader read = new BufferedReader( fr );

							// wobei hier nur die ersten 23 Zeichen der Datei ausgelesen werden
							// 1 00 010203
							// 2 0c 04
							// 3 6a 0521
							// 4 50 06
							// 5 20 0708
							// 6 0d 09
							// 7 0a 1012

							// 9 66 17
							// 10 74 18
							// 11 79 19
							// 12 70 2022
							// 13 32 23

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

							// auslesen der ersten 10 Zeichen der Datei
							int length;
							char[] buffer = new char[10];
							length = read.read( buffer );
							for ( int y = 0; y != length; y++ )
								;

							/* die beiden charArrays (soll und ist) mit einander vergleichen IST =
							 * c1c1c1c2c3c4c5c5c6c7 */
							char[] charArray1 = buffer;
							char[] charArray2 = new char[] { c1, c1, c1, c2, c3, c4, c5, c5, c6, c7 };

							if ( Arrays.equals( charArray1, charArray2 ) ) {
								/* höchstwahrscheinlich ein JP2 da es mit 0000000c6a5020200d0a respektive ....jP ..�
								 * beginnt */
								// System.out.println("es ist ein JP2 oder JPX ");

								// auslesen der ersten 23 Zeiche der Datei
								FileReader fr23 = new FileReader( fl );
								@SuppressWarnings("resource")
								BufferedReader read23 = new BufferedReader( fr23 );
								int length23;
								int i23;
								char[] buffer23 = new char[23];
								length23 = read23.read( buffer23 );
								for ( i23 = 0; i23 != length23; i23++ )
									;

								/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c9, c10, c11,
								 * c12, c3, c12, c13 */
								char[] charArray3 = buffer23;
								char[] charArray4 = new char[] { c9, c10, c11, c12, c3, c12, c13 };
								String stringCharArray3 = String.valueOf( charArray3 );
								String stringCharArray4 = String.valueOf( charArray4 );
								if ( stringCharArray3.endsWith( stringCharArray4 ) ) {
									// System.out.print("es ist ein JP2 (JPEG2000 Part1)");
									// TODO: JP2 Validierung

									/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein
									 * generelles TODO in allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann
									 * alle Pfade mit
									 * 
									 * dirOfJarPath + File.separator +
									 * 
									 * erweitern. */
									String path = new java.io.File( KOSTVal.class.getProtectionDomain()
											.getCodeSource().getLocation().getPath() ).getAbsolutePath();
									path = path.substring( 0, path.lastIndexOf( "." ) );
									path = path + System.getProperty( "java.class.path" );
									String locationOfJarPath = path;
									String dirOfJarPath = locationOfJarPath;
									if ( locationOfJarPath.endsWith( ".jar" ) ) {
										File file = new File( locationOfJarPath );
										dirOfJarPath = file.getParent();
									}

									String pathToJpylyzerExe = dirOfJarPath + File.separator + "resources"
											+ File.separator + "jpylyzer" + File.separator + "jpylyzer.exe";

									File fJpylyzerExe = new File( pathToJpylyzerExe );
									if ( !fJpylyzerExe.exists() ) {
										getMessageService().logError(
												getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
														+ getTextResourceService().getText( ERROR_XML_A_JP2_JPYLYZER_MISSING ) );
									}

									pathToJpylyzerExe = "\"" + pathToJpylyzerExe + "\"";

									try {
										File report = null;

										try {
											// jpylyzer-Befehl: pathToJpylyzerExe valDatei > valDatei.jpylyzer-log.xml
											String outputPath = pathToLogDir;
											String outputName = File.separator + fl.getName() + ".jpylyzer-log.xml";
											String pathToJpylyzerReport = outputPath + outputName;
											File output = new File( pathToJpylyzerReport );
											Runtime rt = Runtime.getRuntime();
											Process proc = null;

											try {
												report = output;

												// falls das File bereits existiert, z.B. von einem vorhergehenden
												// Durchlauf,
												// löschen wir
												// es
												if ( report.exists() ) {
													report.delete();
												}

												/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem
												 * geschachtellten Befehl gehts: cmd /c\"urspruenlicher Befehl\" */
												String command = "cmd /c \"" + pathToJpylyzerExe + " \""
														+ fl.getAbsolutePath() + "\" > \"" + output.getAbsolutePath() + "\"\"";
												proc = rt.exec( command.toString().split( " " ) );
												// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag
												// vorhanden
												// ist!

												// Warte, bis proc fertig ist
												proc.waitFor();

											} catch ( Exception e ) {
												getMessageService().logError(
														getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
																+ getTextResourceService().getText( ERROR_XML_A_JP2_SERVICEFAILED,
																		e.getMessage() ) );
												isValidJP2 = false;
												delFile = false;
												invalidJP2 = invalidJP2 + "   " + filename;
												jp2Counter = jp2Counter + 1;
											} finally {
												/* // Warte, bis wget fertig ist 0 = Alles io int exitStatus =
												 * proc.waitFor(); // 10ms warten bis die Konsole umgeschaltet wird
												 * Thread.sleep( 10 ); Util.switchOnConsole();
												 * 
												 * if ( 0 != exitStatus ) { // invalide isValidJP2 = false; delFile = false;
												 * invalidFile = invalidFile + filename + " "; } */
												if ( proc != null ) {
													closeQuietly( proc.getOutputStream() );
													closeQuietly( proc.getInputStream() );
													closeQuietly( proc.getErrorStream() );
												}
											}
											if ( report.exists() ) {
												// TODO: auswerten
												Document doc = null;

												BufferedInputStream bis = new BufferedInputStream( new FileInputStream(
														pathToJpylyzerReport ) );
												DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
												DocumentBuilder db = dbf.newDocumentBuilder();
												doc = db.parse( bis );
												doc.normalize();

												NodeList nodeLstI = doc.getElementsByTagName( "isValidJP2" );

												// Node isValidJP2 enthält im TextNode das Resultat TextNode ist ein
												// ChildNode
												for ( int s = 0; s < nodeLstI.getLength(); s++ ) {
													Node resultNode = nodeLstI.item( s );
													StringBuffer buf = new StringBuffer();
													NodeList children = resultNode.getChildNodes();
													for ( int i = 0; i < children.getLength(); i++ ) {
														Node textChild = children.item( i );
														if ( textChild.getNodeType() != Node.TEXT_NODE ) {
															continue;
														}
														buf.append( textChild.getNodeValue() );
													}
													String result = buf.toString();

													// Das Resultat ist False oder True
													if ( result.equalsIgnoreCase( "True" ) ) {
														// valid
													} else {
														// invalide
														isValidJP2 = false;
														delFile = false;
														invalidJP2 = invalidJP2 + "   " + filename;
														jp2Counter = jp2Counter + 1;
													}
												}

											} else {
												// Datei nicht angelegt...
												getMessageService().logError(
														getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
																+ getTextResourceService().getText( ERROR_XML_A_JP2_NOREPORT ) );
												isValidJP2 = false;
												delFile = false;
												invalidJP2 = invalidJP2 + "   " + filename;
												jp2Counter = jp2Counter + 1;
											}
										} catch ( Exception e ) {
											getMessageService().logError(
													getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
															+ getTextResourceService()
																	.getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
											isValidJP2 = false;
											delFile = false;
											invalidJP2 = invalidJP2 + "   " + filename;
											jp2Counter = jp2Counter + 1;
										}
										if ( report.exists() ) {
											report.delete();
										}

									} catch ( Exception e ) {
										getMessageService()
												.logError(
														getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
																+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
																		e.getMessage() ) );
									}

									// End JP2 Validierung

								} else {
									// System.out.print("es ist ein JPX (extended JPEG2000 Part2)");

									/* JPX wird nicht validiert, entsprechend File löschen */
									Util.deleteFile( fl );
								}

							}
							read.close();

							if ( delFile ) {
								// Validierung diese Bildes bestanden. Das Bild wird aus log gelöscht.
								Util.deleteFile( fl );
							}
							os.close();
						}
					} else if ( PdfName.JBIG2DECODE.equals( filter ) ) {
						/* Bild mit der JBIG2 Komprimierung */
						isValidJBIG2 = false;
						jbig2Counter = jbig2Counter + 1;
						jbig2Obj = jbig2Obj + renderInfo.getRef().getNumber() + " ";
					} else {
						/* kein JPEG, JP2 oder JBIG2. Es wird entsprechend keine Validierung gemacht. */
					}
				} catch ( IOException ioe ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, ioe.getMessage() ) );
				}
			} catch ( IOException e ) {
				String input = e.getMessage();

				if ( input.contains( "is not supported" ) ) {
					if ( input.contains( "The color space" ) ) {
						// Warnung wird nicht ausgegeben
					} else if ( input.contains( "The color depth" ) ) {
						// Warnung wird nicht ausgegeben
					} else {
						// Warnung wird nicht ausgegeben
					}
				} else {
					System.out.println( e.getMessage() );
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				}
			}
		}

		public void renderText( TextRenderInfo renderInfo )
		{
		}

		@Override
		public boolean onProgress( float percent )
		{
			// Muss auf return true sein, da ansonsten BadPeggy nicht funktioniert
			return true;
		}

	}

}
