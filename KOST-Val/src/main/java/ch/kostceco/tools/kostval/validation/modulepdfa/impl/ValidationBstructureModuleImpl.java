/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationBstructureException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationBstructureModule;

/**
 * PDFA Validierungs mit PDFTron. Ist die vorliegende PDF-Datei eine valide
 * PDFA-Datei
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class ValidationBstructureModuleImpl extends ValidationModuleImpl
		implements ValidationBstructureModule
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
			throws ValidationBstructureException
	{

		boolean valid = true;

		boolean isValid = true;
		boolean erkennung = false;

		// PDF-Datei an Pdftron übergeben wenn die Erkennung erfolgreich
		erkennung = valid;
		if ( erkennung = true ) {
			// Informationen zum PDFTRON-Logverzeichnis holen
			String pathToPdftronOutput = directoryOfLogfile.getAbsolutePath();

			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			try {
				// Report holen und weiter auswerten
				File report = new File( pathToPdftronOutput, valDatei.getName()
						+ ".pdftron-log.xml" );

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
					// und übersetzen

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

						if ( errorDigit.equals( "1" ) ) {
							// Struktur Fehler -> B
							isValid = false;
							getMessageService()
									.logError(
											getTextResourceService().getText(
													MESSAGE_XML_MODUL_B_PDFA )
													+ getTextResourceService()
															.getText(
																	ERROR_XML_AJ_PDFA_ERRORMESSAGE,
																	errorMessage ) );

						}
					}
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_B_PDFA )
								+ e.getMessage() );
				return false;
			}
		}
		valid = erkennung;

		return isValid;
	}

}