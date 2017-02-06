/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2017 Claire Roethlisberger (KOST-CECO), Christian
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

import java.io.File;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfvalidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.StreamGobbler;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationApdftronModule;

/** Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit PDFTron und oder
 * PDF-Tools.
 * 
 * Folgendes ist Konfigurierbar: Hauptvalidator sowie ob eine duale Validierung durchgeführt werden
 * soll oder nicht. Bei der dualen Validierung müssen beide Validatoren die Datei als invalide
 * betrachten, damit diese als invalid gilt. Bei Uneinigkeit gilt diese als valid.
 * 
 * Es wird falls vorhanden die Vollversion von PDF-Tools verwendet. KOST-Val muss nicht angepasst
 * werden und verwendet automatisch den internen Schlüssel, sollte keine Vollversion existieren.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit PDFTron und oder
 * PDF-Tools. Die Fehler werden den Einzelnen Gruppen (Modulen) zugeordnet
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationApdftronModuleImpl extends ValidationModuleImpl implements
		ValidationApdftronModule
{

	private ConfigurationService	configurationService;

	public static String					NEWLINE	= System.getProperty( "line.separator" );

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
		@SuppressWarnings("unused")
		boolean valid = false;

		// Version & Level herausfinden
		String pdfa1 = getConfigurationService().pdfa1();
		String pdfa2 = getConfigurationService().pdfa2();

		Integer pdfaVer1 = 0;
		Integer pdfaVer2 = 0;

		File outputLog = directoryOfLogfile;
		String pathToPdftronOutput = outputLog.getAbsolutePath();

		File reportPdftron = new File( pathToPdftronOutput, "PDFTron.xml" );

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		/* Neu soll die Validierung mit PDFTron konfigurier bar sein Mögliche Werte 1A, 1B und no sowie
		 * 2A, 2B, 2U und no Da Archive beide Versionen erlauben können sind es 2 config einträge Es
		 * gibt mehre Möglichkeiten das PDF in der gewünschten Version zu testen - Unterscheidung anhand
		 * DROID --> braucht viel Zeit auch mit KaD_Signaturefile - Unterscheidung anhand PDF/A-Eintrag
		 * wie Droid aber selber programmiert --> ist viel schneller */
		if ( pdfa2.equals( "2A" ) || pdfa2.equals( "2B" ) || pdfa2.equals( "2U" ) ) {
			// gültiger Konfigurationseintrag und V2 erlaubt
			pdfaVer2 = 2;
		} else {
			// v2 nicht erlaubt oder falscher eintrag
			pdfa2 = "no";
		}
		if ( pdfa1.equals( "1A" ) || pdfa1.equals( "1B" ) ) {
			// gültiger Konfigurationseintrag und V1 erlaubt
			pdfaVer1 = 1;
		} else {
			// v1 nicht erlaubt oder falscher eintrag
			pdfa1 = "no";
		}
		if ( pdfa1 == "no" && pdfa2 == "no" ) {
			// keine Validierung möglich. keine PDFA-Versionen konfiguriert
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_A_PDFA_NOCONFIG ) );
			valid = false;
			return false;
		}

		String level = "no";
		// Richtiges Level definieren
		if ( pdfaVer1 != 1 ) {
			// Level 1 nicht erlaubt --> Level 2
			level = pdfa2;
		} else if ( pdfaVer2 != 2 ) {
			// Level 2 nicht erlaubt --> Level 1
			level = pdfa1;
		} else {
			/* Beide sind möglich, da PDFTron nicht von sich aus anhand der Dateil validiert ist in diesem
			 * Fall eine Validerung über den gesamten Ordner nicht möglich */
			Util.deleteFile( reportPdftron );
		}

		// Start mit der Erkennung

		boolean isValid = false;

		// Initialisierung PDFTron -> überprüfen der Angaben: existiert die PdftronExe am angebenen Ort?
		String pathToPdftronExe = getConfigurationService().getPathToPdftronExe();

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		if ( pathToPdftronExe.startsWith( "Configuration-Error:" ) ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA ) + pathToPdftronExe );
			return false;
		}

		File fPdftronExe = new File( pathToPdftronExe );
		if ( !fPdftronExe.exists() || !fPdftronExe.getName().equals( "pdfa.exe" ) ) {
			return false;
		}

		pathToPdftronExe = "\"" + pathToPdftronExe + "\"";

		try {
			/* PDF-Validierung über den GANZEN Ordner wenn der Hauptvalidator PDFTron ist. Dies bringt
			 * eine extreme Performanceoptimierung! */
			// Pfad zum Programm Pdftron
			File pdftronExe = new File( pathToPdftronExe );

			if ( !reportPdftron.exists() ) {
				/* Gesamtreport existiert nicht. Er wird jetzt erstellt. */
				File reportTemp;

				String pathContent = valDatei.getAbsolutePath();
				try {
					// Pfad zum Programm Pdftron
					StringBuffer command = new StringBuffer( pdftronExe + " " );
					command.append( "-l " + level );
					command.append( " -o " );
					command.append( "\"" );
					command.append( outputLog.getAbsolutePath() );
					command.append( "\" --subfolders" );
					command.append( " " );
					command.append( "\"" );
					command.append( pathContent );
					command.append( "\"" );

					Process proc = null;
					Runtime rt = null;

					try {
						/* Der Name des generierten Reports lautet per default report.xml und es scheint keine
						 * Möglichkeit zu geben, dies zu übersteuern. */
						reportTemp = new File( pathToPdftronOutput, "report.xml" );

						// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen
						// wir es
						if ( reportTemp.exists() ) {
							reportTemp.delete();
						}

						Util.switchOffConsole();

						rt = Runtime.getRuntime();
						proc = rt.exec( command.toString().split( " " ) );
						// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

						// Fehleroutput holen
						StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

						// Output holen
						StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

						// Threads starten
						errorGobbler.start();
						outputGobbler.start();

						// Warte, bis wget fertig ist
						proc.waitFor();

						Util.switchOnConsole();
					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
										+ getTextResourceService().getText( ERROR_XML_A_PDFA_SERVICEFAILED,
												e.getMessage() ) );
						return false;
					} finally {
						if ( proc != null ) {
							closeQuietly( proc.getOutputStream() );
							closeQuietly( proc.getInputStream() );
							closeQuietly( proc.getErrorStream() );
						}
					}
					// Ende PDFTRON direkt auszulösen

					Util.copyFile( reportTemp, reportPdftron );
					Util.deleteFile( reportTemp );

					/* Bringt die ganze <Fail>...</Fail> auf eine Zeile
					 * 
					 * Zuerst alle Leerstellen zwischen den Elementen löschen und dann der Zeilenumbruch vor
					 * <Error und </Fail löschen */
					for ( int r = 0; r < 20; r++ ) {
						Util.oldnewstring( " <", "<", reportPdftron );
					}
					Util.oldnewstring( System.getProperty( "line.separator" ) + "<Error", "<Error",
							reportPdftron );
					Util.oldnewstring( System.getProperty( "line.separator" ) + "</Fail", "</Fail",
							reportPdftron );

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				}
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_PDFA )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}

}
