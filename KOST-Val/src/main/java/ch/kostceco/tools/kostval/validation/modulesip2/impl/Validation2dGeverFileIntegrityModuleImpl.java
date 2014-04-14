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

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */

public class Validation2dGeverFileIntegrityModuleImpl extends
		ValidationModuleImpl implements Validation2dGeverFileIntegrityModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation2dGeverFileIntegrityException
	{
		Map<String, String> dateiRefContent = new HashMap<String, String>();
		Map<String, String> dateiRefOrdnungssystem = new HashMap<String, String>();

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		FileEntry metadataxml = null;
		boolean valid = true;

		try {
			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {
				if ( fileEntry.getName().equals( "header/" + METADATA )
						|| fileEntry.getName().equals(
								toplevelDir + "/header/" + METADATA ) ) {
					metadataxml = fileEntry;
					break;
				}
			}

			// keine metadata.xml in der SIP-Datei gefunden
			if ( metadataxml == null ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bd_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_AE_NOMETADATAFOUND ) );
				return false;
			}

			EntryInputStream eis = zipfile.openEntryInputStream( metadataxml
					.getName() );
			BufferedInputStream is = new BufferedInputStream( eis );

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( is );
				doc.getDocumentElement().normalize();

				XPath xpath = XPathFactory.newInstance().newXPath();
				Element elementName = (Element) xpath.evaluate(
						"/paket/ablieferung", doc, XPathConstants.NODE );

				if ( elementName == null ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bd_SIP )
											+ getTextResourceService()
													.getText(
															ERROR_XML_AE_ABLIEFERUNGSTYPUNDEFINED ) );
					return false;
				}

				if ( elementName.getAttribute( "xsi:type" ).equals(
						"ablieferungGeverSIP" ) ) {

					NodeList nodeLst = doc.getElementsByTagName( "dateiRef" );

					for ( int s = 0; s < nodeLst.getLength(); s++ ) {
						Node fstNode = nodeLst.item( s );

						Element fstElement = (Element) fstNode;
						Node parentNode = fstElement.getParentNode();
						Element parentElement = (Element) parentNode;
						NodeList titelList = parentElement
								.getElementsByTagName( "titel" );

						Node titelNode = titelList.item( 0 );
						dateiRefOrdnungssystem.put( fstNode.getTextContent(),
								titelNode.getTextContent() );
					}

					// alle datei ids aus header/content holen
					NodeList nameNodes = (NodeList) xpath.evaluate(
							"//ordner/name", doc, XPathConstants.NODESET );
					for ( int s = 0; s < nameNodes.getLength(); s++ ) {
						Node dateiNode = nameNodes.item( s );
						if ( dateiNode.getTextContent().equals( "content" ) ) {
							Element dateiElement = (Element) dateiNode;
							Element parentElement = (Element) dateiElement
									.getParentNode();

							NodeList dateiNodes = parentElement
									.getElementsByTagName( "datei" );
							for ( int x = 0; x < dateiNodes.getLength(); x++ ) {
								Node dateiNode2 = dateiNodes.item( x );
								Node id = dateiNode2.getAttributes()
										.getNamedItem( "id" );

								Element dateiElement2 = (Element) dateiNode2;
								NodeList nameList = dateiElement2
										.getElementsByTagName( "name" );
								Node titelNode = nameList.item( 0 );

								Node dateiParentNode = dateiElement2
										.getParentNode();
								Element dateiParentElement = (Element) dateiParentNode;
								NodeList nameNodes2 = dateiParentElement
										.getElementsByTagName( "name" );
								Node contentName = nameNodes2.item( 0 );

								dateiRefContent.put(
										id.getNodeValue(),
										"content/"
												+ contentName.getTextContent()
												+ "/"
												+ titelNode.getTextContent() );
							}
						}
					}

					Set<String> keysContent = dateiRefContent.keySet();
					boolean titlePrinted = false;
					for ( Iterator<String> iterator = keysContent.iterator(); iterator
							.hasNext(); ) {
						String keyContent = iterator.next();
						String deleted = dateiRefOrdnungssystem
								.remove( keyContent );
						if ( deleted == null ) {
							if ( !titlePrinted ) {
								getMessageService()
										.logError(
												getTextResourceService()
														.getText(
																MESSAGE_XML_MODUL_Bd_SIP )
														+ getTextResourceService()
																.getText(
																		MESSAGE_XML_BD_MISSINGINABLIEFERUNG,
																		keyContent ) );
								titlePrinted = true;
							}
							valid = false;
						}
					}

					Set<String> keysRefOrd = dateiRefOrdnungssystem.keySet();
					for ( Iterator<String> iterator = keysRefOrd.iterator(); iterator
							.hasNext(); ) {
						String keyOrd = iterator.next();
						// Die folgende DateiRef vorhanden in
						// metadata/paket/ablieferung/ordnungssystem,
						// aber nicht in
						// metadata/paket/inhaltsverzeichnis/content
						getMessageService()
								.logError(
										getTextResourceService().getText(
												MESSAGE_XML_MODUL_Bd_SIP )
												+ getTextResourceService()
														.getText(
																MESSAGE_XML_BD_MISSINGINABLIEFERUNG,
																keyOrd ) );
						valid = false;
					}

				} else if ( elementName.getAttribute( "xsi:type" ).equals(
						"ablieferungFilesSIP" ) ) {
					// im Falle Ablieferungstyp FILE macht die Validierung
					// nichts
					valid = true;

				} else {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Bd_SIP )
											+ getTextResourceService()
													.getText(
															ERROR_XML_AE_ABLIEFERUNGSTYPUNDEFINED ) );
					return false;
				}

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bd_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

			zipfile.close();
			is.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Bd_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;
	}

}
