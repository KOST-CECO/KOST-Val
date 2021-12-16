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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/** kostval --> Controllervalfile
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllervalfile implements MessageConstants
{

	private static TextResourceService textResourceService;

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean valFile( File valDatei, String logFileName, File directoryOfLogfile,
			boolean verbose, String dirOfJarPath, Map<String, String> configMap,
			ApplicationContext context, Locale locale, File logFile ) throws IOException
	{
		String originalValName = valDatei.getAbsolutePath();
		String valDateiName = valDatei.getName();
		String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
		boolean valFile = false;
		File pathTemp = new File( directoryOfLogfile, "path.tmp" );

		/* falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, loeschen wir es */
		if ( pathTemp.exists() ) {
			pathTemp.delete();
		}
		if ( pathTemp.exists() ) {
			List<String> oldtextList = Files.readAllLines( pathTemp.toPath(), StandardCharsets.UTF_8 );
			for ( int i = 0; i < oldtextList.size(); i++ ) {
				String oldtext = (oldtextList.get( i ));
				Util.oldnewstring( oldtext, "", pathTemp );
			}
		} else {
			pathTemp.createNewFile();
		}
		FileUtils.writeStringToFile( pathTemp, originalValName, "UTF-8" );

		if ( (valDateiExt.equals( ".jp2" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_JP2VALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllerjp2 controller1 = (Controllerjp2) context.getBean( "controllerjp2" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			valFile = okMandatory;

			if ( okMandatory ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			/* Ausgabe der Pfade zu den Jpylyzer Reports, falls welche generiert wurden (-v) oder Jpylyzer
			 * Report loeschen */
			File JpylyzerReport = new File( directoryOfLogfile,
					valDatei.getName() + ".jpylyzer-log.xml" );

			if ( JpylyzerReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> Jpylyzer-Report lassen
				} else {
					// kein optionaler Parameter --> Jpylyzer-Report loeschen!
					Util.deleteFile( JpylyzerReport );
				}
			}

		} else if ( (valDateiExt.equals( ".jpeg" ) || valDateiExt.equals( ".jpg" )
				|| valDateiExt.equals( ".jpe" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_JPEGVALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllerjpeg controller1 = (Controllerjpeg) context.getBean( "controllerjpeg" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			valFile = okMandatory;

			if ( okMandatory ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

		} else if ( (valDateiExt.equals( ".png" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_PNGVALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllerpng controller1 = (Controllerpng) context.getBean( "controllerpng" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			valFile = okMandatory;

			if ( okMandatory ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

		} else if ( (valDateiExt.equals( ".tiff" ) || valDateiExt.equals( ".tif" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_TIFFVALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllertiff controller1 = (Controllertiff) context.getBean( "controllertiff" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			boolean ok = false;

			/* die Validierungen A sind obligatorisch, wenn sie bestanden wurden, koennen die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation fuehren, ausgefuehrt werden. */
			if ( okMandatory ) {
				ok = controller1.executeOptional( valDatei, directoryOfLogfile, configMap, locale,
						logFile );
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			/* Ausgabe der Pfade zu den Jhove Reports, falls welche generiert wurden (-v) oder Jhove
			 * Report loeschen */
			File jhoveReport = new File( directoryOfLogfile, valDatei.getName() + ".jhove-log.txt" );
			File exifReport = new File( directoryOfLogfile, valDatei.getName() + ".exiftool-log.txt" );

			if ( verbose ) {
				// optionaler Parameter --> Reports lassen
			} else {
				if ( jhoveReport.exists() ) {
					// kein optionaler Parameter --> Jhove-Report loeschen!
					Util.deleteFile( jhoveReport );
				}
				if ( exifReport.exists() ) {
					// kein optionaler Parameter --> Exiftool-Report loeschen!
					Util.deleteFile( exifReport );
				}
			}

		} else if ( (valDateiExt.equals( ".siard" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_SIARDVALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllersiard controller2 = (Controllersiard) context.getBean( "controllersiard" );
			boolean okMandatory = controller2.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			boolean ok = false;

			/* die Validierungen A-D sind obligatorisch, wenn sie bestanden wurden, koennen die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation fuehren, ausgefuehrt werden. */
			if ( okMandatory ) {
				ok = controller2.executeOptional( valDatei, directoryOfLogfile, configMap, locale,
						logFile );
				// Ausfuehren der optionalen Schritte
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

		} else if ( (valDateiExt.equals( ".pdf" ) || valDateiExt.equals( ".pdfa" )) ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_PDFAVALIDATION ) ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale, MESSAGE_XML_VALFILE, originalValName ) );
			Controllerpdfa controller3 = (Controllerpdfa) context.getBean( "controllerpdfa" );

			boolean okMandatory = controller3.executeMandatory( valDatei, directoryOfLogfile, configMap,
					locale, logFile );
			boolean ok = false;

			/* die Initialisierung ist obligatorisch, wenn sie bestanden wurden, koennen die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation fuehren, ausgefuehrt werden. */

			if ( okMandatory ) {
				ok = controller3.executeOptional( valDatei, directoryOfLogfile, configMap, locale,
						logFile );
				// Ausfuehren der validierung und optionalen Bildvalidierung
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( valFile ) {
				// Validierte Datei valide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Validierte Datei invalide
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			PdfValidatorAPI.terminate();

			/* Ausgabe der Pfade zu den _FontValidation.xml und _FontValidation_Error.xml Reports, falls
			 * welche generiert wurden. Ggf loeschen */
			File fontValidationReport = new File( directoryOfLogfile,
					valDatei.getName() + "_FontValidation.xml" );
			// Test.pdf_FontValidation.xml
			if ( fontValidationReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> fontValidationReport lassen
				} else {
					// kein optionaler Parameter --> fontValidationReport loeschen!
					Util.deleteFile( fontValidationReport );
				}
			}
			File fontValidationErrorReport = new File( directoryOfLogfile,
					valDatei.getName() + "_FontValidation_Error.xml" );
			if ( fontValidationErrorReport.exists() ) {
				// fontValidationErrorReport loeschen!
				Util.deleteFile( fontValidationErrorReport );
			}
			File callasTEMPreport = new File( directoryOfLogfile, "callasTEMPreport.txt" );
			if ( callasTEMPreport.exists() ) {
				// callasTEMPreport loeschen!
				Util.deleteFile( callasTEMPreport );
			}
			File internLicenseFile = new File(
					directoryOfLogfile + File.separator + ".useKOSTValLicense.txt" );
			if ( internLicenseFile.exists() ) {
				// interne Lizenz verwendet. Lizenz ueberschreiben
				Util.deleteFile( internLicenseFile );
				PdfValidatorAPI.setLicenseKey( " " );
			}

		} else {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
			System.out.println( getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
		}

		if ( pathTemp.exists() ) {
			// pathTemploeschen!
			pathTemp.delete();
		}

		// Garbage Collecter aufruf zur Bereinigung
		System.gc();

		return valFile;
	}
}