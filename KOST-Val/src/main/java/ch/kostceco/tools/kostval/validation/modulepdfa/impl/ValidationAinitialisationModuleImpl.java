/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Roethlisberger (KOST-CECO), Christian
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
package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import com.pdftools.NativeLibrary;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAinitialisationModule;

/** Initialisierung PDF-Tools Kontrolle ob ein Schluessel vorhanden ist (Vollversion) wenn nicht,
 * wird die eingeschraenkte Version verwendet Da der Schluessel nur fuer KOST-Val verwendet werden
 * darf, wird er nicht publiziert
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationAinitialisationModuleImpl extends ValidationModuleImpl
		implements ValidationAinitialisationModule
{
	public static String NEWLINE = System.getProperty( "line.separator" );

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationApdfvalidationException
	{
		PdfValidatorAPI docPdfInit = null;
		boolean isValid = false;
		int pT = 0;
		@SuppressWarnings("unused")
		boolean dual = false;
		String lk1 = "";
		String plk1 = "WC4F9O";
		String lk2 = "";
		String plk2 = "";
		String lk3 = "";
		String plk3 = "";
		String lk4 = "";
		String plk4 = "";
		String lk5 = "";
		String plk5 = "";
		int cPT = 0;
		String cPTSt = " ";

		// Seiten zaehlen und notieren
		try {
			File directoryOfConfigfile = new File( System.getenv( "USERPROFILE" ) + File.separator
					+ ".kost-val" + File.separator + "configuration" );
			java.util.Date year = new java.util.Date();
			java.text.SimpleDateFormat sdfYear = new java.text.SimpleDateFormat( "yyyy" );
			String stringYear = sdfYear.format( year );

			File cPTyearFile = new File(
					directoryOfConfigfile + File.separator + "." + stringYear + "_cPT.txt" );

			if ( !cPTyearFile.exists() ) {
				cPTyearFile.createNewFile();
				FileWriter writer = new FileWriter( cPTyearFile, true );
				writer.write( "0" );
				writer.close();
			}

			Scanner scanner = new Scanner( cPTyearFile );
			cPTSt = scanner.useDelimiter( "\\A" ).next();
			cPT = Integer.parseInt( cPTSt );
			scanner.close();

			if ( cPT < 72000 ) {
				// alles iO pT = 0
			} else if ( cPT < 100000 ) {
				// Lizenz leicht ueberschritten, Warnung ausgeben
				pT = 1;
			} else if ( cPT < 144000 ) {
				// Lizenz deutlich ueberschritten, Warnung ausgeben und verzoegern
				pT = 2;
			} else {
				// Lizenz massiv ueberschritten, Abbrechen
				pT = 3;
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		// Initialisierung PDFTools
		String pdftoolsValidator = configMap.get( "pdftools" );
		lk5 = "H1ND7SS";
		plk2 = "B64JR5";
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		try {
			lk1 = "0MQAAT";
			plk3 = "MAO081";
			if ( pdftoolsValidator.equalsIgnoreCase( "yes" ) ) {
				/* mit ProductVersion kontrollieren ob die richtige dll verwendet wird und ansonsten
				 * kontrolliert abfangen */
				@SuppressWarnings("unused")
				String ProductVersion = "";
				ProductVersion = PdfValidatorAPI.getProductVersion();
				// PDFTools: Check license
				lk4 = "A2F150";
				plk4 = "ACR25M";
				if ( !PdfValidatorAPI.getLicenseIsValid() ) {
					// System.out.println( "Zaehler nachher: " + cPT );
					// System.out.println( "Status: " + pT );

					// Keine Vollversion vorhanden -> zaehler pruefen
					if ( pT == 3 ) {
						// Validierungsvolumen massiv ueberschritten
					} else {
						// Interne eigeschraenke Version verwenden
						lk3 = "V34U6W";
						plk5 = "ESU434J";
						/** Da der Schluessel nur fuer KOST-Val verwendet werden darf, wird er nicht publiziert
						 * --> 000-YOUR-OWN-OEM-LICENCEKEY-000 mit dem OEM-Lizenzschluessel ersetzten!
						 *
						 * Because the key is only allowed to be used for KOST-Val, he is not published ->
						 * replaced 000-YOUR-OWN-OEM-LICENCEKEY-000 with the your OEM license key! **/

						@SuppressWarnings("unused")
						String strPseudoLicenseKey = plk1 + plk2 + plk3 + plk4 + plk5;
						lk2 = "9U4NA2";
						String strLicenseKey = lk1 + lk2 + lk3 + lk4 + lk5;
						PdfValidatorAPI.setLicenseKey( strLicenseKey );

						// Anzahl Seiten ermitteln
						File directoryOfConfigfile = new File( System.getenv( "USERPROFILE" ) + File.separator
								+ ".kost-val" + File.separator + "configuration" );
						java.util.Date year = new java.util.Date();
						java.text.SimpleDateFormat sdfYear = new java.text.SimpleDateFormat( "yyyy" );
						String stringYear = sdfYear.format( year );

						File cPTyearFile = new File(
								directoryOfConfigfile + File.separator + "." + stringYear + "_cPT.txt" );

						if ( !cPTyearFile.exists() ) {
							// eigentlich unnoetig, muesste bereits oben abgefangen sein
							cPTyearFile.createNewFile();
							FileWriter writer = new FileWriter( cPTyearFile, true );
							writer.write( "0" );
							writer.close();
						}

						if ( cPTyearFile.exists() ) {
							// unset hidden attribute, damit hineingeschrieben werden kann
							Files.setAttribute( cPTyearFile.toPath(), "dos:hidden", false,
									LinkOption.NOFOLLOW_LINKS );
						}

						Scanner scanner = new Scanner( cPTyearFile );
						cPTSt = scanner.useDelimiter( "\\A" ).next();
						cPT = Integer.parseInt( cPTSt );
						scanner.close();

						// mit Hilfe von getPageCount() die Seiten ermitteln.
						int pages;
						docPdfInit = new PdfValidatorAPI();
						if ( docPdfInit.open( valDatei.getAbsolutePath(), "",
								NativeLibrary.COMPLIANCE.ePDFUnk ) ) {
							// PDF konnte geoeffnet werden, jetzt ist zaehlen moeglich
							docPdfInit.setStopOnError( true );
							docPdfInit.setReportingLevel( 1 );
						}
						pages = docPdfInit.getPageCount();
						if ( pages == 0 ) {
							// ua portfolios koennen nicht gezaehlt werden
							pages = 1;
						}

						cPT = cPT + pages;
						cPTSt = cPT + "";
						Writer writer = new BufferedWriter(
								new OutputStreamWriter( new FileOutputStream( cPTyearFile ), "utf-8" ) );
						writer.write( cPTSt );
						writer.close();

						// set hidden attribute
						Files.setAttribute( cPTyearFile.toPath(), "dos:hidden", true,
								LinkOption.NOFOLLOW_LINKS );

						if ( pT == 1 ) {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( locale, WARNING_XML_A_PDFTOOLS_LICENSE_1,
													cPTSt ) );
						}
						if ( pT == 2 ) {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PDFA )
											+ getTextResourceService().getText( locale, WARNING_XML_A_PDFTOOLS_LICENSE_2,
													cPTSt ) );
							// Validierung um 10 Sekunden verzoegern
							Thread.sleep( 10000 );
						}
					}
				}
				if ( !PdfValidatorAPI.getLicenseIsValid() ) {
					getMessageService().logError( getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( locale, ERROR_XML_A_PDFTOOLS_LICENSE, cPTSt ) );
					return false;
				}
			}
			// System.out.println(PdfValidatorAPI.VERSION);
			isValid = true;
		} catch ( UnsatisfiedLinkError exception ) {
			System.out.println( " " );
			System.out.println( " ! " + exception + " !" );
			getMessageService().logError( getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_A_PDFA )
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, exception.getMessage() ) );
			// pdftools ausschalten = no
			configMap.put( "pdftools", "no" );
			return false;
		} catch ( Exception e ) {
			System.out.println( " " );
			System.out.println( e );
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return isValid;
	}
}