/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2016 Claire Röthlisberger (KOST-CECO), Christian
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * @author Rc Claire Röthlisberger, KOST-CECO */

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
		String jbig2Allowed = getConfigurationService().jbig2allowed();
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
			valid = true;
		}

		return valid;
	}

	/** Parses a PDF and extracts all the images.
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
							+ getTextResourceService().getText( ERROR_XML_J_CATCH4 ) );
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
										// invalide
										isValidJPEG = false;
										delFile = false;
										invalidJPEG = invalidJPEG + "   " + filename;
										jpegCounter = jpegCounter + 1;
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

							// TODO: JP2 Validierung

							String pathToJpylyzerExe = "resources" + File.separator + "jpylyzer" + File.separator
									+ "jpylyzer.exe";

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

										// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
										// löschen wir
										// es
										if ( report.exists() ) {
											report.delete();
										}

										/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem
										 * geschachtellten Befehl gehts: cmd /c\"urspruenlicher Befehl\" */
										String command = "cmd /c \"" + pathToJpylyzerExe + " \"" + fl.getAbsolutePath()
												+ "\" > \"" + output.getAbsolutePath() + "\"\"";
										proc = rt.exec( command.toString().split( " " ) );
										// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden
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
										/* // Warte, bis wget fertig ist 0 = Alles io int exitStatus = proc.waitFor();
										 * // 10ms warten bis die Konsole umgeschaltet wird Thread.sleep( 10 );
										 * Util.switchOnConsole();
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

										// Node isValidJP2 enthält im TextNode das Resultat TextNode ist ein ChildNode
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
													+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
									isValidJP2 = false;
									delFile = false;
									invalidJP2 = invalidJP2 + "   " + filename;
									jp2Counter = jp2Counter + 1;
								}
								if ( report.exists() ) {
									report.delete();
								}

							} catch ( Exception e ) {
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
							}

							// End JP2 Validierung

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
				String objectNumber = "" + renderInfo.getRef().getNumber();

				if ( input.contains( "is not supported" ) ) {
					if ( input.contains( "The color space" ) ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText( ERROR_XML_J_CATCH1, objectNumber ) );
					} else if ( input.contains( "The color depth" ) ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText( ERROR_XML_J_CATCH2, objectNumber ) );
					} else {
						System.out.println( e.getMessage() );
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
										+ getTextResourceService().getText( ERROR_XML_J_CATCH3, objectNumber ) );
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
