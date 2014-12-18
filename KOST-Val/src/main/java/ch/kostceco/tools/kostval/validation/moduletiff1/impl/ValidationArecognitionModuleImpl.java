/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.validation.moduletiff1.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

import ch.kostceco.tools.kostval.exception.moduletiff1.ValidationArecognitionException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff1.ValidationArecognitionModule;

/** Validierungsschritt A (Erkennung) Ist es eine TIFF-Datei? valid --> Extension: tiff / tif / tfx
 * valid --> beginnt mit II*. [49492A00] oder mit MM.* [4D4D002A] ==> Bei dem Modul A wird die
 * Validierung abgebrochen, sollte das Resulat invalid sein!
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */
public class ValidationArecognitionModuleImpl extends ValidationModuleImpl implements
		ValidationArecognitionModule
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
			throws ValidationArecognitionException
	{
		/* Eine TIFF Datei (.tiff / .tif / .tfx) muss entweder mit II*. [49492A00] oder mit MM.*
		 * [4D4D002A] beginnen */

		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
							+ getTextResourceService().getText( ERROR_XML_A_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".tiff" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".tif" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

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
					/* höchstwahrscheinlich ein TIFF da es mit 49492A00 respektive II*. beginnt valid = true; */
				} else if ( Arrays.equals( charArray1, charArray3 ) ) {
					/* höchstwahrscheinlich ein TIFF da es mit 4D4D002A respektive MM.* beginnt valid = true; */
				} else {
					// Droid-Erkennung, damit Details ausgegeben werden können
					String nameOfSignature = getConfigurationService().getPathToDroidSignatureFile();
					if ( nameOfSignature == null ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
										+ getTextResourceService().getText(
												MESSAGE_XML_CONFIGURATION_ERROR_NO_SIGNATURE ) );
						return false;
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
										+ getTextResourceService().getText( MESSAGE_XML_CA_DROID ) );
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
								getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
										+ getTextResourceService().getText( ERROR_XML_CANNOT_INITIALIZE_DROID ) );
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
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
									+ getTextResourceService().getText( ERROR_XML_A_INCORRECTFILE, puid ) );
					return false;
				}
				fr.close();
				read.close();
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit tiff oder tif -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_TIFF )
							+ getTextResourceService().getText( ERROR_XML_A_INCORRECTFILEENDING ) );
			return false;
		}
		return true;
	}
}
