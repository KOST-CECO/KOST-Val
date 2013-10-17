/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, and PDF/A-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.SystemException;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdftronException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.StreamGobbler;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationApdftronModule;

/**
 * PDFA Validierungs mit PDFTron. Ist die vorliegende PDF-Datei eine valide
 * PDFA-Datei
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class ValidationApdftronModuleImpl extends ValidationModuleImpl
		implements ValidationApdftronModule
{

	private ConfigurationService	configurationService;

	public static String			NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationApdftronException
	{

		// Start mit der Erkennung
		boolean valid = false;

		// Eine PDF Datei (.tiff / .tif / .tfx) muss mit %PDF [25504446]
		// beginnen
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_A )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_MODULE_A_PDFA_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".pdf" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// Hex 25 in Char umwandeln
				String str1 = "25";
				int i1 = Integer.parseInt( str1, 16 );
				char c1 = (char) i1;
				// Hex 50 in Char umwandeln
				String str2 = "50";
				int i2 = Integer.parseInt( str2, 16 );
				char c2 = (char) i2;
				// Hex 44 in Char umwandeln
				String str3 = "44";
				int i3 = Integer.parseInt( str3, 16 );
				char c3 = (char) i3;
				// Hex 46 in Char umwandeln
				String str4 = "46";
				int i4 = Integer.parseInt( str4, 16 );
				char c4 = (char) i4;

				// auslesen der ersten 4 Zeichen der Datei
				int length;
				int i;
				char[] buffer = new char[4];
				length = read.read( buffer );
				for ( i = 0; i != length; i++ )
					;

				// die beiden charArrays (soll und ist) mit einander
				// vergleichen IST = c1c2c3c4
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { c1, c2, c3, c4 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					// höchstwahrscheinlich ein PDF da es mit
					// 25504446 respektive &PDF beginnt
					valid = true;
				} else {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_A )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															ERROR_MODULE_A_PDFA_INCORRECTFILE ) );
					return false;
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_A )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ getTextResourceService().getText(
										ERROR_MODULE_A_PDFA_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit pdf oder pdfa -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_A )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_MODULE_A_PDFA_INCORRECTFILEENDING ) );
			return false;
		}
		// Ende der Erkennung

		boolean isValid = true;
		boolean erkennung = false;

		// PDF-Datei an Pdftron übergeben wenn die Erkennung erfolgreich
		erkennung = valid;
		if ( erkennung = true ) {
			String pathToPdftronExe = getConfigurationService()
					.getPathToPdftronExe();

			// getPdftronService().setPathToPdftronExe( pathToPdftronExe );

			// Informationen zum PDFTRON-Logverzeichnis holen
			String pathToPdftronOutput = directoryOfLogfile.getAbsolutePath();

			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			File pdftronDir = new File( pathToPdftronOutput );
			if ( !pdftronDir.exists() ) {
				pdftronDir.mkdir();
			}

			try {
				// Start PDFTRON direkt auszulösen
				File report;
				// Pfad zum Programm Pdftron
				File pdftronExe = new File( pathToPdftronExe );
				File output = new File( pathToPdftronOutput );
				StringBuffer command = new StringBuffer( pdftronExe + " " );

				// TODO: Version und Conformancelevel je nach config

				command.append( "-l B " );
				command.append( "-o " );
				command.append( "\"" );
				command.append( output.getAbsolutePath() );
				command.append( "\"" );
				command.append( " " );
				command.append( valDatei.getAbsolutePath() );

				try {
					Runtime rt = Runtime.getRuntime();
					Process proc = rt.exec( command.toString() );

					// Fehleroutput holen
					StreamGobbler errorGobbler = new StreamGobbler(
							proc.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobbler = new StreamGobbler(
							proc.getInputStream(), "OUTPUT" );

					Util.switchOffConsole();

					// Threads starten
					errorGobbler.start();
					outputGobbler.start();

					// Warte, bis wget fertig ist
					proc.waitFor();

					Util.switchOnConsole();

					// Der Name des generierten Reports lautet per default
					// report.xml
					// und es scheint keine
					// Möglichkeit zu geben, dies zu übersteuern.
					report = new File( pathToPdftronOutput, "report.xml" );
					File newReport = new File( pathToPdftronOutput,
							valDatei.getName() + ".pdftron-log.xml" );

					// falls das File bereits existiert, z.B. von einem
					// vorhergehenden
					// Durchlauf, löschen wir es
					if ( newReport.exists() ) {
						newReport.delete();
					}

					boolean renameOk = report.renameTo( newReport );
					if ( !renameOk ) {
						throw new SystemException(
								"Der Report konnte nicht umbenannt werden." );
					}
					report = newReport;

				} catch ( Exception e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_A )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															ERROR_MODULE_A_PDFA_SERVICEFAILED ) );
					return false;
				}
				// Ende PDFTRON direkt auszulösen

				String pathToPdftronReport = report.getAbsolutePath();

				Util.setPathToReportPdftron( pathToPdftronReport );

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( pathToPdftronReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( bis );
				doc.normalize();

				Integer passCount = new Integer( 0 );
				NodeList nodeLstI = doc.getElementsByTagName( "Pass" );

				// Valide pdfa-Dokumente enthalten
				// "<Validation> <Pass FileName..."
				// Anzahl pass = anzahl Valider pdfa
				for ( int s = 0; s < nodeLstI.getLength(); s++ ) {
					passCount = passCount + 1;
					// Valide PDFA-Datei
					// Module A-J sind Valid
					isValid = true;
				}

				if ( passCount == 0 ) {
					// Invalide PDFA-Datei

					// aus dem Output von Pdftron die Fehlercodes extrahieren
					// und
					// übersetzen

					NodeList nodeLst = doc.getElementsByTagName( "Error" );
					// Bsp. für einen Error Code: <Error Code="e_PDFA173"
					// die erste Ziffer nach e_PDFA ist der Error Code.
					for ( int s = 0; s < nodeLst.getLength(); s++ ) {
						Node dateiNode = nodeLst.item( s );
						NamedNodeMap nodeMap = dateiNode.getAttributes();
						Node errorNode = nodeMap.getNamedItem( "Code" );
						String errorCode = errorNode.getNodeValue();
						Node errorNodeM = nodeMap.getNamedItem( "Message" );
						String errorMessage = errorNodeM.getNodeValue();
						String errorDigit = errorCode.substring( 6, 7 );

						// der Error Code kann auch "Unknown" sein, dieser wird
						// in
						// den Code "0" übersetzt
						if ( errorDigit.equals( "U" ) ) {
							errorDigit = "0";
						}
						if ( errorDigit.equals( "n" ) ) {
							errorDigit = "0";
						}
						/*
						 * System.out.print( "errorDigit = " + errorDigit +
						 * " > errorMessage = " + errorMessage + "  " );
						 */

						if ( errorDigit.equals( "0" ) ) {
							// Allgemeiner Fehler -> A
							isValid = false;
							getMessageService()
									.logError(
											getTextResourceService().getText(
													MESSAGE_MODULE_A )
													+ getTextResourceService()
															.getText(
																	MESSAGE_DASHES )
													+ getTextResourceService()
															.getText(
																	ERROR_MODULE_AJ_PDFA_ERRORMESSAGE,
																	errorMessage ) );

						}
					}
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_A )
								+ getTextResourceService().getText(
										MESSAGE_DASHES ) + e.getMessage() );
				return false;
			}
		}
		valid = erkennung;

		return isValid;
	}

}