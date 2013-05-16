/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
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

package ch.kostceco.tools.tiffval.validation.module1.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.kostceco.tools.tiffval.exception.module1.Validation1dMetadataException;
import ch.kostceco.tools.tiffval.service.ConfigurationService;
import ch.kostceco.tools.tiffval.validation.ValidationModuleImpl;
import ch.kostceco.tools.tiffval.validation.module1.Validation1dMetadataModule;
import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class Validation1dMetadataModuleImpl extends ValidationModuleImpl
		implements Validation1dMetadataModule
{

	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	final int	BUFFER	= 2048;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean validate( File sipDatei )
			throws Validation1dMetadataException
	{

		// fetch the metadata.xml file from the zip. There's no need to check
		// for the existence of the metadata.xml file anymore, because this is
		// already done in the previous validation step.

		// ZipEntry metadataxml = null;
		FileEntry metadataxml = null;
		Map<String, String> xsdsInZip = new HashMap<String, String>();
		Map<String, String> xsdsInMetadata = new HashMap<String, String>();

		File xmlToValidate = null;
		File xsdToValidate = null;

		// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
		String pathToWorkDir = getConfigurationService().getPathToWorkDir();
		File tmpDir = new File( pathToWorkDir );
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		String toplevelDir = sipDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		try {
			// Das metadata.xml und seine xsd's müssen in das Filesystem
			// extrahiert werden, weil bei
			// bei Verwendung eines Inputstreams bei der Validierung ein Problem
			// mit
			// den xs:include Statements besteht, die includes können so nicht
			// aufgelöst werden.
			// Es werden hier jedoch nicht nur diese Files extrahiert, sondern
			// gleich die ganze Zip-Datei,
			// weil auch spätere Validierungen (3a - 3c) nur mit den
			// extrahierten Files arbeiten können.
			Zip64File zipfile = new Zip64File( sipDatei );

			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			System.out.print( getTextResourceService().getText(
					MESSAGE_MODULE_WAIT ) );
			System.out.flush();
			for ( FileEntry fileEntry : fileEntryList ) {

				if ( fileEntry.getName().equals( "header/" + METADATA )
						|| fileEntry.getName().equals(
								toplevelDir + "/" + "header/" + METADATA ) ) {
					metadataxml = fileEntry;
				}
				if ( fileEntry.getName().startsWith( "header/xsd/" )
						&& fileEntry.getName().endsWith( ".xsd" ) ) {
					xsdsInZip.put( fileEntry.getName().split( "/" )[2],
							fileEntry.getName().split( "/" )[2] );
				} else if ( fileEntry.getName().startsWith(
						toplevelDir + "/" + "header/xsd/" )
						&& fileEntry.getName().endsWith( ".xsd" ) ) {
					xsdsInZip.put( fileEntry.getName().split( "/" )[3],
							fileEntry.getName().split( "/" )[3] );
				}

				if ( !fileEntry.isDirectory() ) {

					byte[] buffer = new byte[8192];

					// Write the file to the original position in the fs.
					EntryInputStream eis = zipfile
							.openEntryInputStream( fileEntry.getName() );

					File newFile = new File( tmpDir, fileEntry.getName() );
					File parent = newFile.getParentFile();
					if ( !parent.exists() ) {
						parent.mkdirs();
					}

					FileOutputStream fos = new FileOutputStream( newFile );
					for ( int iRead = eis.read( buffer ); iRead >= 0; iRead = eis
							.read( buffer ) ) {
						fos.write( buffer, 0, iRead );
					}
					eis.close();
					fos.close();

					if ( newFile.getName().endsWith( "metadata.xml" ) ) {
						xmlToValidate = newFile;
					}
					if ( newFile.getName().endsWith( XSD_ARELDA ) ) {
						xsdToValidate = newFile;
					} else {
						if ( newFile.getName().endsWith( "arelda.xsd" ) ) {
							xsdToValidate = newFile;
						}
					}

				}
			}

			if ( xmlToValidate != null && xsdToValidate != null ) {

				try {
					System.setProperty(
							"javax.xml.parsers.DocumentBuilderFactory",
							"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					factory.setNamespaceAware( true );
					factory.setValidating( true );
					factory.setAttribute(
							"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
							"http://www.w3.org/2001/XMLSchema" );
					factory.setAttribute(
							"http://java.sun.com/xml/jaxp/properties/schemaSource",
							xsdToValidate.getAbsolutePath() );
					DocumentBuilder builder = factory.newDocumentBuilder();
					Validator handler = new Validator();
					builder.setErrorHandler( handler );
					builder.parse( xmlToValidate.getAbsolutePath() );
					if ( handler.validationError == true ) {
						System.out
								.print( "\r                                                                                                                                     " );
						System.out.flush();
						System.out.print( "\r" );
						System.out.flush();

						return false;
					}

				} catch ( java.io.IOException ioe ) {
					getMessageService().logError(
							getTextResourceService()
									.getText( MESSAGE_MODULE_Ad )
									+ getTextResourceService().getText(
											MESSAGE_DASHES )
									+ "IOException "
									+ ioe.getMessage() );
				} catch ( SAXException e ) {
					getMessageService().logError(
							getTextResourceService()
									.getText( MESSAGE_MODULE_Ad )
									+ getTextResourceService().getText(
											MESSAGE_DASHES )
									+ "SAXException "
									+ e.getMessage() );
				} catch ( ParserConfigurationException e ) {
					getMessageService().logError(
							getTextResourceService()
									.getText( MESSAGE_MODULE_Ad )
									+ getTextResourceService().getText(
											MESSAGE_DASHES )
									+ "ParserConfigurationException "
									+ e.getMessage() );
				}
			}

			if ( metadataxml == null ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Ad )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								 );

				System.out
						.print( "\r                                                                                                                                     " );
				System.out.flush();
				System.out.print( "\r" );
				System.out.flush();

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

				XPath xpath = XPathFactory.newInstance().newXPath();
				Element elementName = (Element) xpath.evaluate(
						"/paket/inhaltsverzeichnis/ordner/ordner/name", doc,
						XPathConstants.NODE );

				// TODO: Gewährleisten, dass
				// /paket/inhaltsverzeichnis/ordner/name = header ist
				/*
				 * Zwischenzeitlich wird eine Spezielle Fehlermeldung
				 * ausgegeben, sollte in metadata.xml der content vor header
				 * aufgeliestet sein (ERROR_MODULE_AD_CONTENTB4HEADER)
				 */
				Node parentNode = elementName.getParentNode();
				NodeList nodeLst = parentNode.getChildNodes();

				for ( int s = 0; s < nodeLst.getLength(); s++ ) {

					Node fstNode = nodeLst.item( s );

					if ( fstNode.getNodeType() == Node.ELEMENT_NODE
							&& fstNode.getNodeName().equals( "datei" ) ) {
						Element fstElmnt = (Element) fstNode;
						NodeList fstElmntList = fstElmnt
								.getElementsByTagName( "originalName" );
						Node wantedNode = fstElmntList.item( 0 );
						xsdsInMetadata.put( wantedNode.getTextContent(),
								wantedNode.getTextContent() );
					}

				}

			} catch ( Exception e ) {

				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Ad )
								+ getTextResourceService().getText(
										MESSAGE_DASHES ) + e.getMessage() );

				System.out
						.print( "\r                                                                                                                                     " );
				System.out.flush();
				System.out.print( "\r" );
				System.out.flush();

				return false;
			}

			eis.close();
			is.close();
			zipfile.close();

			// alle Files, die in metadata.xml unter <header><xsd>
			// aufgelistet sind, müssen im Folder
			// /header/xsd vorhanden sein, und umgekehrt

			if ( xsdsInZip.size() != xsdsInMetadata.size() ) {
				if ( xsdsInMetadata.size() == 0 ) {
					getMessageService().logError(
							getTextResourceService()
									.getText( MESSAGE_MODULE_Ad )
									+ getTextResourceService().getText(
											MESSAGE_DASHES )
									+ getTextResourceService().getText(
											ERROR_MODULE_AD_CONTENTB4HEADER ) );

					System.out
							.print( "\r                                                                                                                                     " );
					System.out.flush();
					System.out.print( "\r" );
					System.out.flush();

					return false;

				} else {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_Ad )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															ERROR_MODULE_AD_WRONGNUMBEROFXSDS ) );

					System.out
							.print( "\r                                                                                                                                     " );
					System.out.flush();
					System.out.print( "\r" );
					System.out.flush();

					return false;
				}
			} else {
				Set keys = xsdsInZip.keySet();
				Map xsdsInZipControl = new HashMap<String, String>();
				xsdsInZipControl.putAll( xsdsInZip );

				for ( Iterator<String> iterator = keys.iterator(); iterator
						.hasNext(); ) {
					String key = iterator.next();
					String removedKey = xsdsInMetadata.remove( key );
					if ( removedKey == null ) {
						getMessageService()
								.logError(
										getTextResourceService().getText(
												MESSAGE_MODULE_Ad )
												+ getTextResourceService()
														.getText(
																MESSAGE_DASHES )
												+ getTextResourceService()
														.getText(
																ERROR_MODULE_AD_WRONGNUMBEROFXSDS ) );

						System.out
								.print( "\r                                                                                                                                     " );
						System.out.flush();
						System.out.print( "\r" );
						System.out.flush();

						return false;
					}
					xsdsInZipControl.remove( key );
				}
				if ( xsdsInZipControl.size() != 0 ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_Ad )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															ERROR_MODULE_AD_WRONGNUMBEROFXSDS ) );

					System.out
							.print( "\r                                                                                                                                     " );
					System.out.flush();
					System.out.print( "\r" );
					System.out.flush();

					return false;
				}

			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Ad )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ e.getMessage() );

			System.out
					.print( "\r                                                                                                                                     " );
			System.out.flush();
			System.out.print( "\r" );
			System.out.flush();

			return false;

		}
		System.out
				.print( "\r                                                                                                                                     " );
		System.out.flush();
		System.out.print( "\r" );
		System.out.flush();

		return true;
	}

	private class Validator extends DefaultHandler
	{
		public boolean				validationError		= false;

		public SAXParseException	saxParseException	= null;

		public void error( SAXParseException exception ) throws SAXException
		{
			validationError = true;
			saxParseException = exception;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Ad )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_MODULE_AD_METADATA_ERRORS,
									saxParseException.getLineNumber() ) );

		}

		public void fatalError( SAXParseException exception )
				throws SAXException
		{
			validationError = true;
			saxParseException = exception;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Ad )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_MODULE_AD_METADATA_ERRORS,
									saxParseException.getLineNumber() ) );
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

}
