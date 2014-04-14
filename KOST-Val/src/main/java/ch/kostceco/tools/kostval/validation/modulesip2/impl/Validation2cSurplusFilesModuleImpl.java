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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2cSurplusFilesException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2cSurplusFilesModule;
import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */

public class Validation2cSurplusFilesModuleImpl extends ValidationModuleImpl
		implements Validation2cSurplusFilesModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation2cSurplusFilesException
	{

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		boolean valid = true;
		FileEntry metadataxml = null;
		Map<String, String> filesInSipFile = new HashMap<String, String>();
		Map<String, String> filesInSipFileO = new HashMap<String, String>();
		Map<String, String> filesInMetadata = new HashMap<String, String>();
		Map<String, String> filesInMetadataO = new HashMap<String, String>();

		try {
			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {

				if ( fileEntry.getName().equals( "header/" + METADATA )
						|| fileEntry.getName().equals(
								toplevelDir + "/header/" + METADATA ) ) {
					metadataxml = fileEntry;
				}

				// schreibt alle Dateien und Ordner exkl. metadata(O).xml in
				// filesInSipFile
				if ( !fileEntry.getName().equals( "header/" + METADATA )
						&& !fileEntry.getName().equals(
								toplevelDir + "/header/" + METADATA ) ) {

					String fileName = fileEntry.getName();
					String toReplace = toplevelDir + "/";
					fileName = fileName.replace( toReplace, "" );

					if ( fileEntry.isDirectory() ) {
						fileName = fileName
								.substring( 0, fileName.length() - 1 );
						filesInSipFileO.put( fileName, fileName );
					} else {
						filesInSipFile.put( fileName, fileName );
					}
				}

			}

			// keine metadata.xml in der SIP-Datei gefunden
			if ( metadataxml == null ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_AE_NOMETADATAFOUND ) );
				return false;

			}

			EntryInputStream eis = zipfile.openEntryInputStream( metadataxml
					.getName() );
			BufferedInputStream is = new BufferedInputStream( eis );

			// Liest alle Dateien aus Metadata.xml
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( is );
				doc.normalize();
				NodeList nodeLst = doc.getElementsByTagName( "datei" );

				for ( int s = 0; s < nodeLst.getLength(); s++ ) {
					Node dateiNode = nodeLst.item( s );

					String path = null;

					NodeList childNodes = dateiNode.getChildNodes();
					for ( int y = 0; y < childNodes.getLength(); y++ ) {
						Node subNode = childNodes.item( y );
						if ( subNode.getNodeName().equals( "name" ) ) {
							path = subNode.getTextContent();
						}
					}

					// selectNodeIterator ist zu Zeitintensiv bei grossen
					// XML-Dateien mit getChildNodes() ersetzt

					/*
					 * NodeIterator nl = XPathAPI.selectNodeIterator( dateiNode,
					 * "name" ); Node nameNode = nl.nextNode(); String path =
					 * nameNode.getTextContent();
					 */

					boolean topReached = false;

					while ( !topReached ) {

						Node parentNode = dateiNode.getParentNode();
						if ( parentNode.getNodeName().equals(
								"inhaltsverzeichnis" ) ) {
							topReached = true;
							break;
						}

						NodeList childrenNodes = parentNode.getChildNodes();
						for ( int x = 0; x < childrenNodes.getLength(); x++ ) {
							Node childNode = childrenNodes.item( x );

							if ( childNode.getNodeName().equals( "name" ) ) {
								path = childNode.getTextContent() + "/" + path;
								if ( dateiNode.getParentNode() != null ) {
									dateiNode = dateiNode.getParentNode();
								}
								break;
							}
						}
					}

					filesInMetadata.put( path, path );
					path = "";

				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

			// entfernt in filesInSipFile alle Einträge die in Metadata
			// aufgeführt sind --> Rest sind zusätzliche
			Set<String> keysInMetadata = filesInMetadata.keySet();

			for ( Iterator<String> iterator = keysInMetadata.iterator(); iterator
					.hasNext(); ) {
				String keyMetadata = iterator.next();
				filesInSipFile.remove( keyMetadata );
			}

			Set<String> keysInSipfile = filesInSipFile.keySet();
			for ( Iterator<String> iterator = keysInSipfile.iterator(); iterator
					.hasNext(); ) {
				String keySipfile = iterator.next();

				getMessageService()
						.logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Bc_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_BC_FILEMISSING,
												keySipfile ) );
				valid = false;
			}

			EntryInputStream eisO = zipfile.openEntryInputStream( metadataxml
					.getName() );
			BufferedInputStream isO = new BufferedInputStream( eisO );

			// Liest alle Ordner aus Metadata.xml
			try {
				DocumentBuilderFactory dbfO = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dbO = dbfO.newDocumentBuilder();
				Document docO = dbO.parse( isO );
				docO.normalize();
				NodeList nodeLstO = docO.getElementsByTagName( "ordner" );

				for ( int sO = 0; sO < nodeLstO.getLength(); sO++ ) {
					Node dateiNodeO = nodeLstO.item( sO );

					String pathO = null;

					NodeList childNodesO = dateiNodeO.getChildNodes();
					for ( int y = 0; y < childNodesO.getLength(); y++ ) {
						Node subNodeO = childNodesO.item( y );
						if ( subNodeO.getNodeName().equals( "name" ) ) {
							// System.out.println("name gefunden --> " +
							// subNode.getTextContent());
							pathO = subNodeO.getTextContent();
						}
					}

					// selectNodeIterator ist zu Zeitintensiv bei grossen
					// XML-Dateien mit getChildNodes() ersetzt
					/*
					 * NodeIterator nlO = XPathAPI.selectNodeIterator(
					 * dateiNodeO, "name" ); Node nameNodeO = nlO.nextNode();
					 * String pathO = nameNodeO.getTextContent();
					 */

					boolean topReachedO = false;

					while ( !topReachedO ) {

						Node parentNodeO = dateiNodeO.getParentNode();
						if ( parentNodeO.getNodeName().equals(
								"inhaltsverzeichnis" ) ) {
							topReachedO = true;
							break;
						}

						NodeList childrenNodesO = parentNodeO.getChildNodes();
						for ( int xO = 0; xO < childrenNodesO.getLength(); xO++ ) {
							Node childNodeO = childrenNodesO.item( xO );

							if ( childNodeO.getNodeName().equals( "name" ) ) {
								pathO = childNodeO.getTextContent() + "/"
										+ pathO;
								if ( dateiNodeO.getParentNode() != null ) {
									dateiNodeO = dateiNodeO.getParentNode();
								}
								break;
							}
						}
					}

					filesInMetadataO.put( pathO, pathO );
					pathO = "";

				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

			// entfernt in filesInSipFileO alle Einträge die in MetadataO
			// aufgeführt sind --> Rest sind zusätzliche
			Set<String> keysInMetadataO = filesInMetadataO.keySet();

			for ( Iterator<String> iterator = keysInMetadataO.iterator(); iterator
					.hasNext(); ) {
				String keyMetadataO = iterator.next();
				filesInSipFileO.remove( keyMetadataO );
			}

			Set<String> keysInSipfileO = filesInSipFileO.keySet();
			for ( Iterator<String> iterator = keysInSipfileO.iterator(); iterator
					.hasNext(); ) {
				String keySipfileO = iterator.next();

				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Bc_SIP )
								+ getTextResourceService().getText(
										MESSAGE_XML_BC_FILEMISSINGO,
										keySipfileO ) );
				valid = false;
			}

			zipfile.close();
			is.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Bc_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

}
