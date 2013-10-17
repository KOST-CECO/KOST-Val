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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationHcontentException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationHcontentModule;

/**
 * Validierungsschritt H (Content-Validierung) Sind die XML-Dateien im content
 * valid zu ihrer Schema-Definition (XSD-Dateien)? valid --> tableZ.xml valid zu
 * tableZ.xsd
 * 
 * @author Ec Christian Eugster
 */

public class ValidationHcontentModuleImpl extends ValidationModuleImpl
		implements ValidationHcontentModule
{

	private static final int	UNBOUNDED	= -1;

	public ConfigurationService	configurationService;

	private XMLReader			reader;

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
			throws ValidationHcontentException
	{
		boolean valid = true;
		try {
			/*
			 * Extract the metadata.xml from the temporary work folder and build
			 * a jdom document
			 */
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();
			File metadataXml = new File( new StringBuilder( pathToWorkDir )
					.append( File.separator ).append( "header" )
					.append( File.separator ).append( "metadata.xml" )
					.toString() );
			InputStream fin = new FileInputStream( metadataXml );
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build( fin );
			fin.close();

			/*
			 * read the document and for each schema and table entry verify
			 * existence in temporary extracted structure
			 */
			Namespace ns = Namespace
					.getNamespace( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			// select schema elements and loop
			List<Element> schemas = document.getRootElement()
					.getChild( "schemas", ns ).getChildren( "schema", ns );
			for ( Element schema : schemas ) {
				Element schemaFolder = schema.getChild( "folder", ns );
				File schemaPath = new File( new StringBuilder( pathToWorkDir )
						.append( File.separator ).append( "content" )
						.append( File.separator )
						.append( schemaFolder.getText() ).toString() );
				if ( schemaPath.isDirectory() ) {
					Element[] tables = schema.getChild( "tables", ns )
							.getChildren( "table", ns )
							.toArray( new Element[0] );
					for ( Element table : tables ) {
						Element tableFolder = table.getChild( "folder", ns );
						File tablePath = new File( new StringBuilder(
								schemaPath.getAbsolutePath() )
								.append( File.separator )
								.append( tableFolder.getText() ).toString() );
						if ( tablePath.isDirectory() ) {
							File tableXml = new File( new StringBuilder(
									tablePath.getAbsolutePath() )
									.append( File.separator )
									.append( tableFolder.getText() + ".xml" )
									.toString() );
							File tableXsd = new File( new StringBuilder(
									tablePath.getAbsolutePath() )
									.append( File.separator )
									.append( tableFolder.getText() + ".xsd" )
									.toString() );
							if ( verifyRowCount( tableXml, tableXsd ) ) {

								valid = validate1( tableXml, tableXsd ) && valid;
							}
						}
					}
				}
			}
		} catch ( java.io.IOException ioe ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_H )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ "IOException " + ioe.getMessage() );
		} catch ( JDOMException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_H )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ "JDOMException " + e.getMessage() );
		} catch ( SAXException e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_H )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ "SAXException " + e.getMessage() );
		}

		return valid;
	}

	private boolean validate1( File xmlFile, File schemaLocation )
			throws SAXException, IOException
	{
		SchemaFactory factory = SchemaFactory
				.newInstance( "http://www.w3.org/2001/XMLSchema" );
		ValidationErrorHandler errorHandler = new ValidationErrorHandler(
				xmlFile, schemaLocation );
		Schema schema = factory.newSchema( schemaLocation );
		Validator validator = schema.newValidator();
		validator.setErrorHandler( errorHandler );
		Source source = new StreamSource( xmlFile );
		validator.validate( source );
		return errorHandler.isValid();
	}

	/**
	 * Verify the number of rows in the table. If table count >=
	 * kostval.conf.xml.table-rows-limit, the xsd minOccurs and maxOccurs
	 * values are extracted. If those values are numbers, then the validation is
	 * not executed, because the risk of an OutOfMemoryError is given.
	 * 
	 * @param xmlFile
	 * @param schemaLocation
	 * @return <code>true</code> if validation should be excecuted, else
	 *         <code>false</code>
	 * @throws SAXException
	 * @throws IOException
	 */
	private boolean verifyRowCount( File xmlFile, File schemaLocation )
			throws SAXException, IOException
	{
		Range range = getRange( schemaLocation );
		if ( range.min == 0 && range.max == UNBOUNDED ) {
			return true;
		} else {
			int limit = configurationService.getTableRowsLimit();
			if ( range.max > limit ) {
				getMessageService().logInfo(
						getTextResourceService().getText( MESSAGE_MODULE_H )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ getTextResourceService().getText(
										MESSAGE_MODULE_H_TABLE_NOT_VALIDATED1,
										xmlFile.getName() ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_H )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ getTextResourceService().getText(
										MESSAGE_MODULE_H_TABLE_NOT_VALIDATED2,
										range.max, limit,
										schemaLocation.getName() ) );
			}
			return range.min <= limit && range.max <= limit;
		}
	}

	// private int getRowCount(File xmlFile) throws SAXException, IOException
	// {
	// reader = XMLReaderFactory.createXMLReader();
	// reader.setFeature( "http://xml.org/sax/features/validation", false );
	// reader.setFeature( "http://apache.org/xml/features/validation/schema",
	// false );
	// CountRowsHandler countRowsHandler = new CountRowsHandler();
	// reader.setContentHandler( countRowsHandler );
	// reader.parse( new InputSource(new FileInputStream(xmlFile)) );
	// return countRowsHandler.getRows();
	// }

	private Range getRange( File xsdFile ) throws SAXException, IOException
	{
		Range range = new Range();
		RangeHandler rangeHandler = new RangeHandler();
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setFeature( "http://xml.org/sax/features/validation", false );
			reader.setFeature(
					"http://apache.org/xml/features/validation/schema", false );
			reader.setContentHandler( rangeHandler );
			reader.parse( new InputSource( new FileInputStream( xsdFile ) ) );
		} catch ( BreakException e ) {
			range = rangeHandler.getRange();
		}
		return range;
	}

	private class ValidationErrorHandler implements ErrorHandler
	{
		private boolean	valid	= true;

		private File	xmlFile;

		private File	schemaLocation;

		public ValidationErrorHandler( File xmlFile, File schemaLocation )
		{
			this.xmlFile = xmlFile;
			this.schemaLocation = schemaLocation;
		}

		@Override
		public void error( SAXParseException e ) throws SAXException
		{
			if ( valid ) {
				valid = false;
			}
			logError( e );
		}

		@Override
		public void fatalError( SAXParseException e ) throws SAXException
		{
			if ( valid ) {
				valid = false;
			}
			logError( e );
		}

		@Override
		public void warning( SAXParseException e ) throws SAXException
		{
			if ( valid ) {
				valid = false;
			}
			logError( e );
		}

		private void logError( SAXParseException e )
		{
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_H )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									MESSAGE_MODULE_H_INVALID_ERROR,
									xmlFile.getName(),
									schemaLocation.getName(),
									e.getLineNumber(), e.getColumnNumber(),
									e.getLocalizedMessage() ) );
		}

		public boolean isValid()
		{
			return valid;
		}
	}

	@SuppressWarnings("unused")
	private class CountRowsHandler extends DefaultHandler
	{
		private int	rows	= 0;

		@Override
		public void startElement( String uri, String localName, String qName,
				Attributes attributes ) throws SAXException
		{
			if ( localName.equals( "row" ) ) {
				rows++;
			}
		}

		public int getRows()
		{
			return rows;
		}
	}

	private class RangeHandler extends DefaultHandler
	{
		private Range	range	= new Range();

		@Override
		public void startElement( String uri, String localName, String qName,
				Attributes attributes ) throws SAXException
		{
			if ( "row".equals( attributes.getValue( "name" ) ) ) {
				if ( "rowType".equals( attributes.getValue( "type" ) ) ) {
					this.range.min = getRange( attributes
							.getValue( "minOccurs" ) );
					this.range.max = getRange( attributes
							.getValue( "maxOccurs" ) );
					throw new BreakException();
				}
			}
		}

		private int getRange( String attributeValue )
		{
			int value = 1;
			if ( attributeValue == null ) {
				return value;
			}
			if ( attributeValue.equals( "unbounded" ) ) {
				return -1;
			}
			try {
				value = Integer.valueOf( attributeValue ).intValue();
			} catch ( NumberFormatException e ) {
			}
			return value;
		}

		public Range getRange()
		{
			return range;

		}
	}

	private class BreakException extends SAXException
	{

		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;

	}

	private class Range
	{
		public int	min	= 1;

		public int	max	= 1;
	}
}
