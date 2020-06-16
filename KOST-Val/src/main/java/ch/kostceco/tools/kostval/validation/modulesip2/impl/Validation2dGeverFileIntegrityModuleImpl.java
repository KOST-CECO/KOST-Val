/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2dGeverFileIntegrityException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2dGeverFileIntegrityModule;

/** Validierungsschritt 2d Bei GEVER SIP pr�fen, ob alle in (metadata.xml)
 * /paket/inhaltsverzeichnis/content referenzierten Dateien auch in
 * (metadata.xml)/paket/ablieferung/ordnungsystem verzeichnet sind. Allfällige Inkonsistenzen
 * auflisten. ( //dokument[@id] => //datei[@id] ). */

public class Validation2dGeverFileIntegrityModuleImpl extends ValidationModuleImpl
		implements Validation2dGeverFileIntegrityModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws Validation2dGeverFileIntegrityException
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "2D   " );
			System.out.print( "\b\b\b\b\b" );
		}
		Map<String, String> dateiRefContent = new HashMap<String, String>();
		Map<String, String> dateiRefOrdnungssystem = new HashMap<String, String>();

		boolean valid = true;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream(
					new File( valDatei.getAbsolutePath() + "//header//metadata.xml" ) ) );
			doc.getDocumentElement().normalize();
			NodeList layerConfigList = doc.getElementsByTagName( "ablieferung" );
			Node node = layerConfigList.item( 0 );
			Element e = (Element) node;
			String name = e.getAttribute( "xsi:type" );

			if ( name.equals( "ablieferungGeverSIP" ) ) {
				// GEVER-SIP
				XPath xpath = XPathFactory.newInstance().newXPath();

				NodeList nodeLst = doc.getElementsByTagName( "dateiRef" );

				for ( int s = 0; s < nodeLst.getLength(); s++ ) {
					Node fstNode = nodeLst.item( s );

					Element fstElement = (Element) fstNode;
					Node parentNode = fstElement.getParentNode();
					Element parentElement = (Element) parentNode;
					NodeList titelList = parentElement.getElementsByTagName( "titel" );

					Node titelNode = titelList.item( 0 );
					dateiRefOrdnungssystem.put( fstNode.getTextContent(), titelNode.getTextContent() );
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "2D-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "2D\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "2D|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "2D/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}

				// alle datei ids aus header/content holen
				NodeList nameNodes = (NodeList) xpath.evaluate( "//ordner/name", doc,
						XPathConstants.NODESET );
				for ( int s = 0; s < nameNodes.getLength(); s++ ) {
					Node dateiNode = nameNodes.item( s );
					if ( dateiNode.getTextContent().equals( "content" ) ) {
						Element dateiElement = (Element) dateiNode;
						Element parentElement = (Element) dateiElement.getParentNode();

						NodeList dateiNodes = parentElement.getElementsByTagName( "datei" );
						for ( int x = 0; x < dateiNodes.getLength(); x++ ) {
							Node dateiNode2 = dateiNodes.item( x );
							Node id = dateiNode2.getAttributes().getNamedItem( "id" );

							Element dateiElement2 = (Element) dateiNode2;
							NodeList nameList = dateiElement2.getElementsByTagName( "name" );
							Node titelNode = nameList.item( 0 );

							Node dateiParentNode = dateiElement2.getParentNode();
							Element dateiParentElement = (Element) dateiParentNode;
							NodeList nameNodes2 = dateiParentElement.getElementsByTagName( "name" );
							Node contentName = nameNodes2.item( 0 );

							dateiRefContent.put( id.getNodeValue(),
									"content/" + contentName.getTextContent() + "/" + titelNode.getTextContent() );
						}
					}
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "2D-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "2D\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "2D|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "2D/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}

				Set<String> keysContent = dateiRefContent.keySet();
				boolean titlePrinted = false;
				for ( Iterator<String> iterator = keysContent.iterator(); iterator.hasNext(); ) {
					String keyContent = iterator.next();
					String deleted = dateiRefOrdnungssystem.remove( keyContent );
					if ( deleted == null ) {
						if ( !titlePrinted ) {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Bd_SIP )
											+ getTextResourceService().getText( locale,
													MESSAGE_XML_BD_MISSINGINABLIEFERUNG, keyContent ) );
							titlePrinted = true;
						}
						valid = false;
					}
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "2D-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "2D\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "2D|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "2D/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}

				Set<String> keysRefOrd = dateiRefOrdnungssystem.keySet();
				for ( Iterator<String> iterator = keysRefOrd.iterator(); iterator.hasNext(); ) {
					String keyOrd = iterator.next();
					/* Die folgende DateiRef vorhanden in metadata/paket/ablieferung/ordnungssystem, aber
					 * nicht in metadata/paket/inhaltsverzeichnis/content */
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Bd_SIP )
									+ getTextResourceService().getText( locale, MESSAGE_XML_BD_MISSINGINABLIEFERUNG,
											keyOrd ) );
					valid = false;
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "2D-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "2D\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "2D|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "2D/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}

			} else {
				// im Falle Ablieferungstyp FILE macht die Validierung nichts
				valid = true;
			}

		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Bd_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;
	}

}
