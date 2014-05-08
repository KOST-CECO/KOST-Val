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

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
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

	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation1dMetadataException
	{

		try {
			File xmlToValidate = new File( valDatei.getAbsolutePath()
					+ "\\header\\metadata.xml" );
			File xsdToValidateBar1 = new File(
					valDatei.getAbsolutePath()
							+ "\\header\\xsd\\arelda_v3.13.2.xsd" );
			File xsdToValidateEch1 = new File(
					valDatei.getAbsolutePath()
							+ "\\header\\xsd\\arelda.xsd" );

			if ( xmlToValidate.exists() && xsdToValidateBar1.exists() ) {
				// Schemavalidierung Nach Version BAR 1
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
							xsdToValidateBar1.getAbsolutePath() );
					DocumentBuilder builder = factory.newDocumentBuilder();
					Validator handler = new Validator();
					builder.setErrorHandler( handler );
					builder.parse( xmlToValidate.getAbsolutePath() );
					if ( handler.validationError == true ) {
						return false;
					}

				} catch ( java.io.IOException ioe ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService().getText(
													ERROR_XML_UNKNOWN,
													ioe.getMessage()
															+ " (IOException)" ) );
				} catch ( SAXException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (SAXException)" ) );
				} catch ( ParserConfigurationException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )

											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (ParserConfigurationException)" ) );
				}
			} else if ( xmlToValidate.exists() && xsdToValidateEch1.exists() ) {
				// Schmavalidierung nach eCH Version 1 inkl Addendum in den
				// Ressourcen
				// Dies erfolgt mit einer Validierung übers Kreuz
				try {
					// xmlToValidate mit xsdToValidateEch1Add
					File xsdToValidateEch1Add = new File(
							"resources\\header_1d\\xsd\\arelda.xsd" );
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
							xsdToValidateEch1Add.getAbsolutePath() );
					DocumentBuilder builder = factory.newDocumentBuilder();
					Validator handler = new Validator();
					builder.setErrorHandler( handler );
					builder.parse( xmlToValidate.getAbsolutePath() );
					if ( handler.validationError == true ) {
						return false;
					}

				} catch ( java.io.IOException ioe ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService().getText(
													ERROR_XML_UNKNOWN,
													ioe.getMessage()
															+ " (IOException)" ) );
				} catch ( SAXException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (SAXException)" ) );
				} catch ( ParserConfigurationException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )

											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (ParserConfigurationException)" ) );
				}

				try {
					// xmlToValidateAdd mit xsdToValidateEch1
					File xmlToValidateAdd = new File(
							"resources\\header_1d\\metadata.xml" );
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
							xsdToValidateEch1.getAbsolutePath() );
					DocumentBuilder builder = factory.newDocumentBuilder();
					Validator handler = new Validator();
					builder.setErrorHandler( handler );
					builder.parse( xmlToValidateAdd.getAbsolutePath() );
					if ( handler.validationError == true ) {
						return false;
					}

				} catch ( java.io.IOException ioe ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService().getText(
													ERROR_XML_UNKNOWN,
													ioe.getMessage()
															+ " (IOException)" ) );
				} catch ( SAXException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )
											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (SAXException)" ) );
				} catch ( ParserConfigurationException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ad_SIP )

											+ getTextResourceService()
													.getText(
															ERROR_XML_UNKNOWN,
															e.getMessage()
																	+ " (ParserConfigurationException)" ) );
				}
			}

/*			if ( xmlToValidate == null || metadataxml == null ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_AE_NOMETADATAFOUND ) );
				return false;
			}*/
			
			// TODO: Validieren ob die xsd korrekt in Metadata.xml enthalten sind
			//evtl ist dies gar nicht nötig falls dies durch Modul2 abgefangen wird

/*			EntryInputStream eis = zipfile.openEntryInputStream( metadataxml
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
/*				Node parentNode = elementName.getParentNode();
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
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_Ad_SIP )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

			eis.close();
			is.close();
			zipfile.close();

			// alle Files, die in metadata.xml unter <header><xsd>
			// aufgelistet sind, müssen im Folder
			// /header/xsd vorhanden sein, und umgekehrt
			// Map<String, String> xsdsInZip
			// Map<String, String> xsdsInMetadata


			if ( xsdsInZip.size() != xsdsInMetadata.size() ) {
				if ( xsdsInMetadata.size() == 0 ) {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText(
											ERROR_XML_AD_CONTENTB4HEADER ) );
					return false;

				} else {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText(
											ERROR_XML_AD_WRONGNUMBEROFXSDS ) );
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
												MESSAGE_XML_MODUL_Ad_SIP )
												+ getTextResourceService()
														.getText(
																ERROR_XML_AD_WRONGNUMBEROFXSDS ) );
						return false;
					}
					xsdsInZipControl.remove( key );
				}
				if ( xsdsInZipControl.size() != 0 ) {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText(
											ERROR_XML_AD_WRONGNUMBEROFXSDS ) );
					return false;
				}
			}*/

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;

		}
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
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_AD_METADATA_ERRORS,
									saxParseException.getLineNumber(),
									saxParseException.getMessage() ) );

		}

		public void fatalError( SAXParseException exception )
				throws SAXException
		{
			validationError = true;
			saxParseException = exception;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_AD_METADATA_ERRORS,
									saxParseException.getLineNumber(),
									saxParseException.getMessage() ) );
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

}
