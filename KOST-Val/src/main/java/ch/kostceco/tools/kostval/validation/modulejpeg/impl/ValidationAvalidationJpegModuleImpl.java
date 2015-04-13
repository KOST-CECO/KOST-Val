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

package ch.kostceco.tools.kostval.validation.modulejpeg.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

import coderslagoon.badpeggy.scanner.ImageFormat;
import coderslagoon.badpeggy.scanner.ImageScanner;
import coderslagoon.badpeggy.scanner.ImageScanner.Callback;

import ch.kostceco.tools.kostval.exception.modulejpeg.ValidationAjpegvalidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejpeg.ValidationAvalidationJpegModule;

/** Ist die vorliegende JPEG-Datei eine valide JPEG-Datei? JPEG Validierungs mit BadPeggy.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit BadPeggy.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 * @author Markus Hahn, coderslagoon */

public class ValidationAvalidationJpegModuleImpl extends ValidationModuleImpl implements
		ValidationAvalidationJpegModule, Callback
{
	private ConfigurationService	configurationService;

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
			throws ValidationAjpegvalidationException
	{
		// Start mit der Erkennung

		// Eine JPEG Datei (.jpg / .jpeg) muss mit FFD8FF -> ÿØÿ beginnen
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( ERROR_XML_A_JPEG_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".jpeg" )
				|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".jpg" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".jpe" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

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
					/* höchstwahrscheinlich ein JPEG da es mit FFD8FF respektive ÿØÿ beginnt */
				} else {
					// Droid-Erkennung, damit Details ausgegeben werden können
					String nameOfSignature = getConfigurationService().getPathToDroidSignatureFile();
					/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
					 * entsprechenden Modul die property anzugeben: <property name="configurationService"
					 * ref="configurationService" /> */

					if ( nameOfSignature == null ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
										+ getTextResourceService().getText(
												MESSAGE_XML_CONFIGURATION_ERROR_NO_SIGNATURE ) );
						read.close();
						return false;
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
										+ getTextResourceService().getText( MESSAGE_XML_CA_DROID ) );
						read.close();
						return false;
					}

					Droid droid = null;
					try {
						/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den
						 * Output stören Util.switchOffConsole() als Kommentar markieren wenn man die
						 * Fehlermeldung erhalten möchte */
						Util.switchOffConsole();
						droid = new Droid();

						droid.readSignatureFile( nameOfSignature );

					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
										+ getTextResourceService().getText( ERROR_XML_CANNOT_INITIALIZE_DROID ) );
						read.close();
						return false;
					} finally {
						Util.switchOnConsole();
					}
					File file = valDatei;
					String puid = "";
					IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
					for ( int x = 0; x < ifile.getNumHits(); x++ ) {
						FileFormatHit ffh = ifile.getHit( x );
						FileFormat ff = ffh.getFileFormat();
						puid = ff.getPUID();
					}
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
									+ getTextResourceService().getText( ERROR_XML_A_JPEG_INCORRECTFILE, puid ) );
					read.close();
					return false;
				}
				read.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
								+ getTextResourceService().getText( ERROR_XML_A_JPEG_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit jpeg/jpg/jpe -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( ERROR_XML_A_JPEG_INCORRECTFILEENDING ) );
			return false;
		}
		// Ende der Erkennung

		boolean isValid = true;

		// TODO: Erledigt: JPEG Validierung

		/* Code schnippsel erhalten von M. Hahn: ImageScannerDemo
		 * 
		 * und umgeschrieben durch C. Röthlisberger auf KOST-Val
		 * 
		 * TODO: JIIO-Fehlermeldungen Übersetzten und Gruppieren; Doc mit BadPeggy + Lizenz erweitern */

		// determine if the file format is known by the scanner
		File fl = valDatei;
		ImageFormat ifmt = ImageFormat.fromFileName( fl.getName() );
		if ( null == ifmt ) {
			System.err.println( "file type not supported" );
			// invalide
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_FAIL ) );
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_FILETYPE ) );
			isValid = false;
		}

		// open the file
		try {
			InputStream is = new FileInputStream( valDatei );

			// scan the file, the return value just tells us good or bad or ...
			ImageScanner iscan = new ImageScanner();
			Boolean ok = iscan.scan( is, ifmt, this );
			if ( null == ok ) {
				// ... that the scanner itself could not do its job at all
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
								+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_FAIL ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
								+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_SCANFAILED ) );
				// invalide
				isValid = false;
			}
			if ( ok ) {
				// valide
				isValid = true;
			} else {
				// invalide
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
								+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_FAIL ) );
				isValid = false;

				ImageScanner.Result ires = iscan.lastResult();

				// display the scanner's log messages
				for ( String msg : ires.collapsedMessages() ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
									+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_ERROR, msg ) );
				}

			}
			is.close();
		} catch ( IOException ioe ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
							+ getTextResourceService().getText( ERROR_XML_A_JPEG_JIIO_FAIL ) );
			getMessageService()
					.logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JPEG )
									+ getTextResourceService().getText( ERROR_XML_A_JPEG_SERVICEFAILED,
											ioe.getMessage() ) );
			isValid = false;
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
