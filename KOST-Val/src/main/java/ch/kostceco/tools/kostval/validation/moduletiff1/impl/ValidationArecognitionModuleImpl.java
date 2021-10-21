/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.moduletiff1.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kosttools.fileservice.DroidPuid;
import ch.kostceco.tools.kosttools.fileservice.Magic;
import ch.kostceco.tools.kostval.exception.moduletiff1.ValidationArecognitionException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff1.ValidationArecognitionModule;

/** Validierungsschritt A (Erkennung) Ist es eine TIFF-Datei? valid --> Extension: tiff / tif / tfx
 * valid --> beginnt mit II*. [49492A00] oder mit MM.* [4D4D002A] ==> Bei dem Modul A wird die
 * Validierung abgebrochen, sollte das Resulat invalid sein!
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */
public class ValidationArecognitionModuleImpl extends ValidationModuleImpl
		implements ValidationArecognitionModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationArecognitionException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		/* Eine TIFF Datei (.tiff / .tif / .tfx) muss entweder mit II*. [49492A00] oder mit MM.*
		 * [4D4D002A] beginnen */

		if ( valDatei.isDirectory() ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_TIFF )
								+ getTextResourceService().getText( locale, ERROR_XML_A_ISDIRECTORY ) );
				return false;
			}
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".tiff" )
				|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".tif" )) ) {

			try {
				// System.out.println("ueberpruefe Magic number tiff...");
				if ( Magic.magicTiff( valDatei ) ) {
					// System.out.println(" -> es ist eine Tiff-Datei");
				} else {
					// System.out.println(" -> es ist KEINE Tiff-Datei");
					if ( min ) {
						return false;
					} else {
						// Droid-Erkennung, damit Details ausgegeben werden koennen
						// existiert die SignatureFile am angebenen Ort?
						String nameOfSignature = configMap.get( "PathToDroidSignatureFile" );
						if ( !new File( nameOfSignature ).exists() ) {
							if ( min ) {
								return false;
							} else {
								getMessageService()
										.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_TIFF )
												+ getTextResourceService().getText( locale, MESSAGE_XML_CA_DROID ) );
								return false;
							}
						}

						// Ermittle die DROID-PUID der valDatei mit hilfe der nameOfSignature
						String puid = (DroidPuid.getPuid( valDatei, nameOfSignature ));
						if ( min ) {
							return false;
						} else if ( puid.equals( " ERROR " ) ) {
							// Probleme bei der Initialisierung von DROID
							getMessageService().logError( getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_TIFF )
									+ getTextResourceService().getText( locale, ERROR_XML_CANNOT_INITIALIZE_DROID ) );
							return false;
						} else {
							// Erkennungsergebnis ausgeben
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_TIFF )
											+ getTextResourceService().getText( locale, ERROR_XML_A_INCORRECTFILE,
													puid ) );
							return false;
						}
					} 
				}
			} catch ( Exception e ) {
				if ( min ) {
					return false;
				} else {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_TIFF )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				}
			}
		} else {
			// die Datei endet nicht mit tiff oder tif -> Fehler
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_TIFF )
								+ getTextResourceService().getText( locale, ERROR_XML_A_INCORRECTFILEENDING ) );
				return false;
			}
		}
		return true;
	}
}
