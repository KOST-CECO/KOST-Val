/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.io.FileReader;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationDphotointerValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationDphotointerValidationModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt D (Farbraum-Validierung) Ist die TIFF-Datei gemäss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationDphotointerValidationModuleImpl extends
		ValidationModuleImpl implements ValidationDphotointerValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath )
			throws ValidationDphotointerValidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File( pathToExiftoolOutput,
				valDatei.getName() + ".exiftool-log.txt" );
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		if ( !exiftoolReport.exists() ) {
			// Report existiert nicht

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_D_TIFF )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_MISSING_REPORT,
									exiftoolReport.getAbsolutePath(),
									getTextResourceService().getText( locale,
											ABORTED ) ) );
			return false;
		} else {
			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			String pi0 = configMap.get( "AllowedPhotointer0" );
			String pi1 = configMap.get( "AllowedPhotointer1" );
			String pi2 = configMap.get( "AllowedPhotointer2" );
			String pi3 = configMap.get( "AllowedPhotointer3" );
			String pi4 = configMap.get( "AllowedPhotointer4" );
			String pi5 = configMap.get( "AllowedPhotointer5" );
			String pi6 = configMap.get( "AllowedPhotointer6" );
			String pi8 = configMap.get( "AllowedPhotointer8" );

			if ( pi0.equals( "" ) ) {
				pi0 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi1.equals( "" ) ) {
				pi1 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi2.equals( "" ) ) {
				pi2 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi3.equals( "" ) ) {
				pi3 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi4.equals( "" ) ) {
				pi4 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi5.equals( "" ) ) {
				pi5 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi6.equals( "" ) ) {
				pi6 = "DiesePhotointerIstNichtErlaubt";
			}
			if ( pi8.equals( "" ) ) {
				pi8 = "DiesePhotointerIstNichtErlaubt";
			}

			Integer exiftoolio = 0;
			String oldErrorLine1 = "";
			String oldErrorLine2 = "";
			String oldErrorLine3 = "";
			String oldErrorLine4 = "";
			String oldErrorLine5 = "";

			try {
				BufferedReader in = new BufferedReader(
						new FileReader( exiftoolReport ) );
				String line;
				while ( (line = in.readLine()) != null ) {
					// die PhotometricInterpretation-Zeile enthält einer dieser
					// Freitexte der Farbraumart
					if ( line.contains( "PhotometricInterpretation: " )
							&& line.contains( "[EXIF:IFD" ) ) {
						exiftoolio = 1;
						if ( line
								.contains( "PhotometricInterpretation: " + pi0 )
								|| line.contains(
										"PhotometricInterpretation: " + pi1 )
								|| line.contains(
										"PhotometricInterpretation: " + pi2 )
								|| line.contains(
										"PhotometricInterpretation: " + pi3 )
								|| line.contains(
										"PhotometricInterpretation: " + pi4 )
								|| line.contains(
										"PhotometricInterpretation: " + pi5 )
								|| line.contains(
										"PhotometricInterpretation: " + pi6 )
								|| line.contains( "PhotometricInterpretation: "
										+ pi8 ) ) {
							// Valider Status
						} else {
							// Invalider Status
							isValid = false;
							if ( min ) {
								in.close();
								/* exiftoolReport loeschen */
								if ( exiftoolReport.exists() ) {
									exiftoolReport.delete();
								}
								return false;
							} else {
								if ( !line.equals( oldErrorLine1 )
										&& !line.equals( oldErrorLine2 )
										&& !line.equals( oldErrorLine3 )
										&& !line.equals( oldErrorLine4 )
										&& !line.equals( oldErrorLine5 ) ) {
									// neuer Fehler
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_D_TIFF )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_CG_INVALID,
																	line ) );
									if ( oldErrorLine1.equals( "" ) ) {
										oldErrorLine1 = line;
									} else if ( oldErrorLine2.equals( "" ) ) {
										oldErrorLine2 = line;
									} else if ( oldErrorLine3.equals( "" ) ) {
										oldErrorLine3 = line;
									} else if ( oldErrorLine4.equals( "" ) ) {
										oldErrorLine4 = line;
									} else if ( oldErrorLine5.equals( "" ) ) {
										oldErrorLine5 = line;
									}
								}
							}
						}
						/*
						 * die PlanarConfiguration-Zeile muss Chunkey sein, da
						 * ansonsten nicht Baseline. Planar wird kaum
						 * unterstützt
						 */
						if ( line.contains( "PlanarConfiguration: " )
								&& line.contains( "[EXIF:IFD" ) ) {
							exiftoolio = 1;
							if ( line.contains( "Chunky" ) ) {
								// Valider Status
							} else {
								// Invalider Status
								isValid = false;
								if ( min ) {
									in.close();
									/* exiftoolReport loeschen */
									if ( exiftoolReport.exists() ) {
										exiftoolReport.delete();
									}
									return false;
								} else {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_D_TIFF )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_CG_INVALID,
																	line ) );
								}
							}
						}
					}
				}
				if ( exiftoolio == 0 ) {
					// Invalider Status
					isValid = false;
					if ( min ) {
						in.close();
						/* exiftoolReport loeschen */
						if ( exiftoolReport.exists() ) {
							exiftoolReport.delete();
						}
						return false;
					} else {

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_D_TIFF )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_CG_ETNIO, "D" ) );
					}
				}
				in.close();
			} catch ( Exception e ) {
				if ( min ) {
					/* exiftoolReport loeschen */
					if ( exiftoolReport.exists() ) {
						exiftoolReport.delete();
					}
					return false;
				} else {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_D_TIFF )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CG_CANNOTFINDETREPORT ) );
					return false;
				}
			}
			return isValid;
		}
	}
}