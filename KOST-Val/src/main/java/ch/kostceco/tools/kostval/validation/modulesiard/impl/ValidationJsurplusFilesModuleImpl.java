/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationJsurplusFilesException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationJsurplusFilesModule;

/** Validierungsschritt J (Zusätzliche Primärdateien) Enthält der content-Ordner Dateien oder Ordner
 * die nicht in metadata.xml beschrieben sind ? invalid --> Zusätzliche Ordner oder Dateien im
 * content-Ordner
 * 
 * @author Ec Christian Eugster */

public class ValidationJsurplusFilesModuleImpl extends ValidationModuleImpl implements
		ValidationJsurplusFilesModule
{

	public ConfigurationService	configurationService;

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
			throws ValidationJsurplusFilesException
	{
		// Ausgabe SIARD-Modul Ersichtlich das KOST-Val arbeitet
		System.out.print( "J   " );
		System.out.print( "\r" );
		int onWork = 41;

		boolean valid = true;
		try {
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */
			File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
					.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );
			InputStream fin = new FileInputStream( metadataXml );
			InputSource source = new InputSource( fin );
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware( true );
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse( source );
			fin.close();

			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			xPath.setNamespaceContext( new SiardNamespaceContext() );

			File content = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
					.append( "content" ).toString() );
			File[] schemas = content.listFiles();
			for ( File schema : schemas ) {
				valid = valid && validateSchema( schema, xPath, doc );
				if ( onWork == 41 ) {
					onWork = 2;
					System.out.print( "J-   " );
					System.out.print( "\r" );
				} else if ( onWork == 11 ) {
					onWork = 12;
					System.out.print( "J\\   " );
					System.out.print( "\r" );
				} else if ( onWork == 21 ) {
					onWork = 22;
					System.out.print( "J|   " );
					System.out.print( "\r" );
				} else if ( onWork == 31 ) {
					onWork = 32;
					System.out.print( "J/   " );
					System.out.print( "\r" );
				} else {
					onWork = onWork + 1;
				}
			}
			xPath = null;
			doc = null;
		} catch ( java.io.IOException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
									e.getMessage() + " (IOException)" ) );
		} catch ( XPathExpressionException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
									e.getMessage() + " (XPathExpressionException)" ) );
		} catch ( ParserConfigurationException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
									e.getMessage() + " (ParserConfigurationException)" ) );
		} catch ( SAXException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
									e.getMessage() + " (SAXException)" ) );
		}
		return valid;
	}

	private boolean validateSchema( File schema, XPath xPath, Document doc )
			throws XPathExpressionException
	{
		boolean valid = true;
		XPathExpression expression = xPath.compile( "//siard:schema[siard:folder='" + schema.getName()
				+ "']/siard:folder/text()" );
		Node node = (Node) expression.evaluate( doc, XPathConstants.NODE );
		if ( node == null ) {
			valid = false;
			File content = new File( schema.getParent() );
			if ( schema.isFile() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FILE, content.getName(),
										schema.getName() ) );
			} else if ( schema.isDirectory() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FOLDER,
										content.getName(), schema.getName() ) );
			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_ENTRY, content.getName(),
										schema.getName() ) );
			}
		}
		if ( valid ) {
			File[] tables = schema.listFiles();
			for ( File table : tables ) {
				valid = validateTable( table, xPath, doc ) && valid;
			}
		}
		xPath = null;
		doc = null;
		schema = null;
		return valid;

	}

	private boolean validateTable( final File table, XPath xPath, Document doc )
			throws XPathExpressionException
	{
		boolean valid = true;
		XPathExpression expression = xPath.compile( "//siard:table[siard:folder='" + table.getName()
				+ "']/siard:folder/text()" );
		Node node = (Node) expression.evaluate( doc, XPathConstants.NODE );
		if ( node == null ) {
			valid = false;
			File schema = new File( table.getParent() );
			if ( table.isFile() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FILE, schema.getName(),
										table.getName() ) );
			} else if ( table.isDirectory() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FOLDER, schema.getName(),
										table.getName() ) );
			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_ENTRY, schema.getName(),
										table.getName() ) );
			}
		}
		if ( valid ) {
			File[] files = table.listFiles( new FileFilter() {
				@Override
				public boolean accept( File file )
				{
					String[] parts = file.getName().split( "[.]" );
					String name = parts[0];
					return !name.equals( table.getName() );
				}
			} );
			for ( File file : files ) {
				valid = validateFile( table, file, xPath, doc ) && valid;
			}
		}
		xPath = null;
		doc = null;
		return valid;
	}

	private boolean validateFile( File folder, File file, XPath xPath, Document doc )
			throws XPathExpressionException
	{
		boolean valid = true;
		StringBuilder builder = new StringBuilder( "//siard:table[siard:folder='" ).append( folder
				.getName() + "']/siard:columns/siard:column[siard:folder='" + file.getName() + "']/text()" );
		XPathExpression expression = xPath.compile( builder.toString() );
		Node node = (Node) expression.evaluate( doc, XPathConstants.NODE );
		if ( node == null ) {
			valid = false;
			File table = new File( file.getParent() );
			if ( file.isFile() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FILE, table.getName(),
										file.getName() ) );
			} else if ( file.isDirectory() ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_FOLDER, table.getName(),
										file.getName() ) );
			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_ENTRY, table.getName(),
										file.getName() ) );
			}
		}
		folder = null;
		file = null;
		xPath = null;
		return valid;
	}

	private class SiardNamespaceContext implements NamespaceContext
	{
		private static final String	SIARD_URI	= "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd";

		private static final String	DB_URI		= "http://db.apache.org/torque/4.0/templates/database";

		@Override
		public String getNamespaceURI( String prefix )
		{
			if ( prefix == null ) {
				throw new NullPointerException( "Null prefix is not allowed" );
			}
			if ( "siard".equals( prefix ) ) {
				return SIARD_URI;
			}
			if ( "db".equals( prefix ) ) {
				return DB_URI;
			}
			return XMLConstants.XML_NS_URI;
		}

		@Override
		public String getPrefix( String uri )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<String> getPrefixes( String uri )
		{
			throw new UnsupportedOperationException();
		}

	}
}
