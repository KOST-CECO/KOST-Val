/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jdom2.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationWwarningException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.bean.ValidationContext;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationWwarningModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/** Validierungsschritt W (Warnungen) Wurden dataOwner und dataOriginTimespan ausgefuellt und nicht
 * auf (...) belassen? <dataOwner>(...)</dataOwner> <dataOriginTimespan>(...)</dataOriginTimespan>
 * ab 1.8 auch <dbname>(...)</dbname> plus dito fuer unspecified
 * 
 * nur Messeage ausgeben aber immer valid
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationWwarningModuleImpl extends ValidationModuleImpl
		implements ValidationWwarningModule

{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale, File logFile ) throws ValidationWwarningException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWork.equals( "yes" ) ) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "W    " );
			System.out.print( "\b\b\b\b\b" );
		} else if ( onWork.equals( "nomin" ) ) {
			min = true;
		} else if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		try {

			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
					.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream( metadataXml ) );
			doc.getDocumentElement().normalize();

			// Lesen der Werte von dataOwner und dataOriginTimespan sowie dbname
			XPath xpath = XPathFactory.newInstance().newXPath();
			Element elementDataOwner = null;
			Element elementDataOriginTimespan = null;
			Element elementDbName = null;

			elementDataOwner = (Element) xpath.evaluate( "/siardArchive/dataOwner", doc,
					XPathConstants.NODE );

			elementDataOriginTimespan = (Element) xpath.evaluate( "/siardArchive/dataOriginTimespan", doc,
					XPathConstants.NODE );

			elementDbName = (Element) xpath.evaluate( "/siardArchive/dbname", doc, XPathConstants.NODE );

			if ( elementDataOwner != null ) {
				String dataOwnerValue = elementDataOwner.getTextContent();
				if ( dataOwnerValue.equals( "(...)" ) || dataOwnerValue.equals( "unspecified" ) ) {
					/* Der Initialwert wurde nicht veraendert respektive ausgefuellt, entsprechend wird eine
					 * Warnung ausgegeben */
					if ( min ) {
					} else {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale, MESSAGE_XML_MODUL_W_SIARD )
										+ getTextResourceService().getText( locale, MESSAGE_XML_W_WARNING_INITVALUE,
												"dataOwner", dataOwnerValue ) );
					}
				}
			}

			if ( elementDataOriginTimespan != null ) {
				String dataOriginTimespanValue = elementDataOriginTimespan.getTextContent();
				if ( dataOriginTimespanValue.equals( "(...)" ) ) {
					/* Der Initialwert wurde nicht veraendert respektive ausgefuellt, entsprechend wird eine
					 * Warnung ausgegeben */
					if ( min ) {
					} else {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale, MESSAGE_XML_MODUL_W_SIARD )
										+ getTextResourceService().getText( locale, MESSAGE_XML_W_WARNING_INITVALUE,
												"dataOriginTimespan", dataOriginTimespanValue ) );
					}
				}
			}

			if ( elementDbName != null ) {
				String dataDbNameValue = elementDbName.getTextContent();
				if ( dataDbNameValue.equals( "(...)" ) ) {
					/* Der Initialwert wurde nicht veraendert respektive ausgefuellt, entsprechend wird eine
					 * Warnung ausgegeben */
					if ( min ) {
					} else {

						Logtxt.logtxt( logFile,
								getTextResourceService().getText( locale, MESSAGE_XML_MODUL_W_SIARD )
										+ getTextResourceService().getText( locale, MESSAGE_XML_W_WARNING_INITVALUE,
												"dbname", dataDbNameValue ) );
					}
				}
			}

		} catch ( Exception e ) {
			if ( min ) {
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_MODUL_W_SIARD )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			}
			return true;
		}

		return true;
	}

	@Override
	public boolean prepareValidation( ValidationContext validationContext )
			throws IOException, JDOMException, Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

}
