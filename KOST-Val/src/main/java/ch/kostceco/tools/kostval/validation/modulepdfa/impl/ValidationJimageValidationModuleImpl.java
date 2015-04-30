/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2015 Claire Röthlisberger (KOST-CECO), Christian
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * eine Warnung, wenn JBIG2 enthalten ist.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class ValidationJimageValidationModuleImpl extends ValidationModuleImpl implements
		ValidationJimageValidationModule
{
	boolean												isValidJPEG		= true;
	boolean												isValidJP2		= true;
	boolean												isValidJBIG2	= true;

	String												invalidFile		= "";
	int												jbig2Counter		= 0;

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
		boolean valid = false;

		// Optionale Bildvalidierung eingeschaltet?
		String pdfaImage = getConfigurationService().pdfaimage();

		if ( pdfaImage.equalsIgnoreCase( "yes" ) ) {
			// Optionale Bildvalidierung eingeschaltet

			// Informationen zum Arbeitsverzeichnis holen
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();

			String srcPdf = valDatei.getAbsolutePath();
			String destImage = pathToWorkDir + File.separator + valDatei.getName();

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
				if (!isValidJPEG || !isValidJP2) {
				// Bildvalidierung nicht bestanden
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
								+ getTextResourceService().getText( ERROR_XML_J_INVALID, invalidFile ) );
				}
				if (!isValidJBIG2) {
					// PDF enthält JBIG2
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
									+ getTextResourceService().getText( ERROR_XML_J_JBIG2, valDatei.getName() ,jbig2Counter ) );
					}
				}
		} else {
			// keine Bildvalidierung
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
		PdfReader reader = new PdfReader( srcPdf );
		PdfReaderContentParser parser = new PdfReaderContentParser( reader );
		MyImageRenderListener listener = new MyImageRenderListener( destImage );
		for ( int i = 1; i <= reader.getNumberOfPages(); i++ ) {
			parser.processContent( i, listener );
		}
		reader.close();
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
				boolean delFile = true;
				FileOutputStream os;
				PdfImageObject image = renderInfo.getImage();

				PdfName filter = (PdfName) image.get( PdfName.FILTER );
				System.out.println("Filter: " + filter);
				String filterText = "" + filter;
				System.out.println(filterText);
				
				/* TODO: B11 und B15 untersuchen*/

				if ( filterText.contains( "JBIG") ) {
					/* Bild mit der JBIG2 Komprimierung */
					isValidJBIG2	= false;
					jbig2Counter = jbig2Counter + 1;
			} else if ( PdfName.DCTDECODE.equals( filter ) ) {
					/* JPEG Bild:
					 * 
					 * Das JPEG wird im Logverzeichnis unter dem [PDF-Name].Obj[objNr].jpg gespeichert */
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
						invalidFile = invalidFile + filename + " ";
						isValidJPEG = false;
						delFile = false;
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
								invalidFile = invalidFile + filename + " ";
							}
							if ( ok ) {
								// valide -> isValidJPEG bleibt unverändert
							} else {
								// invalide
								isValidJPEG = false;
								delFile = false;
								invalidFile = invalidFile + filename + " ";
							}
							is.close();
						} catch ( IOException ioe ) {
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
											+ getTextResourceService().getText( ERROR_XML_A_JPEG_SERVICEFAILED,
													ioe.getMessage() ) );
							isValidJPEG = false;
							delFile = false;
							invalidFile = invalidFile + filename + " ";
						}
					}
					// JPEG-Validierung: Ende
					if ( delFile ) {
						// Validierung diese Bildes bestanden. Das Bild wird aus log gelöscht.
						Util.deleteFile( fl );
					}
					os.close();
				} else if ( PdfName.JPXDECODE.equals( filter ) ) {
					/* JP2 Bild:
					 * 
					 * Das JP2 wird im Logverzeichnis unter dem [PDF-Name].Obj[objNr].jp2 gespeichert */
					filename = pathToLogDir + File.separator + filenamePath + ".Obj"
							+ renderInfo.getRef().getNumber() + ".jp2";
					File fl = new File( filename );
					os = new FileOutputStream( filename );
					os.write( image.getImageAsBytes() );
					os.flush();

				// TODO: JP2 Validierung
					
					if ( delFile ) {
						// Validierung diese Bildes bestanden. Das Bild wird aus log gelöscht.
						Util.deleteFile( fl );
					}
					os.close();
				} else {
					/* kein JPEG, JP2 oder JBIG2. Es wird entsprechend keine Validierung gemacht. */
				}
			} catch ( IOException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_PDFA )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				System.out.println( e.getMessage() );
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
