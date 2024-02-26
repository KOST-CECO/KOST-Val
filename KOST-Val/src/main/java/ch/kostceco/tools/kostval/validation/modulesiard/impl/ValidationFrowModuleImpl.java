/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
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
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationFrowException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.bean.ValidationContext;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationFrowModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt F (Zeilen-Validierung) Wurden die Angaben aus
 * metadata.xml korrekt in die tableZ.xsd-Dateienuebertragen? valid --> gleiche
 * Zeilenzahl (rows in metadata.xml = max = minOccurs in tableZ.xsd)
 * 
 * bei 0 bis unbounded rows von metadata.xml in max = minOccurs von tableZ.xsd
 * uebertragen, damit im Modul H validiert werden kann.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 * @param <Range>
 * @param <RangeHandler>
 */

public class ValidationFrowModuleImpl extends ValidationModuleImpl
		implements ValidationFrowModule
/*
 * public class ValidationFrowModuleImpl<Range, RangeHandler> extends
 * ValidationModuleImpl implements ValidationFrowModule
 * 
 * <Range, RangeHandler> bereitet Probleme beim Projekt-Build. Da es nicht
 * benoetigt wird, wurde es entfernt.
 */
{
	private boolean				min			= false;

	Boolean						version1	= false;
	Boolean						version2	= false;

	private static final int	UNBOUNDED	= -1;

	private XMLReader			reader;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationFrowException
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "F    " );
			System.out.print( "\b\b\b\b\b" );
		} else if ( onWorkConfig.equals( "nomin" ) ) {
			min = true;
		}

		boolean valid = true;
		try {
			/*
			 * Extract the metadata.xml from the temporare work folder and build
			 * a jdom document
			 */
			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File metadataXml = new File(
					new StringBuilder( pathToWorkDir ).append( File.separator )
							.append( "header" ).append( File.separator )
							.append( "metadata.xml" ).toString() );
			InputStream fin = new FileInputStream( metadataXml );
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build( fin );
			fin.close();
			// set to null
			fin = null;

			/*
			 * read the document and for each schema and table entry verify
			 * existence in temporary extracted structure and compare the
			 * rownumber
			 */
			version1 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" )
					.contains(
							"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			version2 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" )
					.contains(
							"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			Namespace ns = Namespace.getNamespace(
					"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			if ( version1 ) {
				// ns = Namespace.getNamespace(
				// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			} else if ( version2 ) {
				ns = Namespace.getNamespace(
						"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			}
			// select schema elements and loop
			List<Element> schemas = document.getRootElement()
					.getChild( "schemas", ns ).getChildren( "schema", ns );
			for ( Element schema : schemas ) {
				valid = validateSchema( schema, ns, pathToWorkDir, configMap,
						locale, logFile );
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "F-   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "F\\   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "F|   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "F/   " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( java.io.IOException ioe ) {
			valid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_F_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										ioe.getMessage() + " (IOException)" ) );
			}
		} catch ( JDOMException e ) {
			valid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_F_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (JDOMException)" ) );
			}
		}

		return valid;
	}

	private class Range
	{
		public int	min	= 1;
		public int	max	= 1;
	}

	private boolean validateSchema( Element schema, Namespace ns,
			String pathToWorkDir, Map<String, String> configMap, Locale locale,
			File logFile )
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "F    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean valid = true;
		boolean validT = true;
		Element schemaFolder = schema.getChild( "folder", ns );
		File schemaPath = new File(
				new StringBuilder( pathToWorkDir ).append( File.separator )
						.append( "content" ).append( File.separator )
						.append( schemaFolder.getText() ).toString() );
		if ( schemaPath.isDirectory() ) {
			if ( schema.getChild( "tables", ns ) != null ) {
				List<Element> tables = schema.getChild( "tables", ns )
						.getChildren( "table", ns );
				for ( Element table : tables ) {
					// Valid = True ansonsten validiert er nicht
					validT = true;
					validT = validT && validateTable( table, ns, pathToWorkDir,
							schemaPath, locale, logFile );
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "F-   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "F\\   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "F|   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "F/   " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
					// Validierungsergebnis in valid speichern
					valid = valid && validT;
				}
			} else {
				// kein Fehler sondern leeres schema
			}
		} else {
			valid = false;
		}
		valid = valid && validT;
		return valid;
	}

	private boolean validateTable( Element table, Namespace ns,
			String pathToWorkDir, File schemaPath, Locale locale, File logFile )
	{
		boolean valid = true;
		boolean validR = true;
		Element tableFolder = table.getChild( "folder", ns );
		Element tablerows = table.getChild( "rows", ns );
		int rowmax = Integer.parseInt( tablerows.getText() );

		File tablePath = new File(
				new StringBuilder( schemaPath.getAbsolutePath() )
						.append( File.separator )
						.append( tableFolder.getText() ).toString() );
		File tableXsd = new File(
				new StringBuilder( tablePath.getAbsolutePath() )
						.append( File.separator )
						.append( tableFolder.getText() + ".xsd" ).toString() );
		validR = validateRow( tableXsd, rowmax, locale, min, logFile );
		valid = valid && validR;

		return valid;
	}

	private boolean validateRow( File tableXsd, int rowmax, Locale locale,
			Boolean min, File logFile )
	{
		boolean valid = false;
		try {
			Range range;
			try {
				range = getRange( tableXsd );
				if ( range.min == 0 && range.max == UNBOUNDED ) {
					// die effektive Zahl in schemaLocation (Work)
					// hereinschreiben
					String oldstring = "minOccurs=\"0\" maxOccurs=\"unbounded";
					String newstring = "minOccurs=\"" + rowmax
							+ "\" maxOccurs=\"" + rowmax;
					Util.oldnewstring( oldstring, newstring, tableXsd );

					// in einigen Faellen ist zuerst max und dann min
					oldstring = "maxOccurs=\"unbounded\" minOccurs=\"0";
					newstring = "maxOccurs=\"" + rowmax + "\" minOccurs=\""
							+ rowmax;
					Util.oldnewstring( oldstring, newstring, tableXsd );

					valid = true;
				} else {
					if ( range.min == rowmax && range.max == rowmax ) {
						valid = true;
					} else {
						valid = false;
						if ( min ) {
							return false;
						} else {

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale,
											MESSAGE_XML_MODUL_F_SIARD )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_F_INVALID_TABLE_XML_FILES,
											tableXsd ) );
						}
					}
				}
			} catch ( IOException e ) {
				valid = false;

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_F_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (IOException)" ) );
			}
		} catch ( SAXException e ) {
			valid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_F_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (SAXException)" ) );
			}
		}
		return valid;
	}

	private Range getRange( File xsdFile ) throws SAXException, IOException
	{
		Range range = new Range();
		RangeHandler rangeHandler = new RangeHandler();
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setFeature( "http://xml.org/sax/features/validation",
					false );
			reader.setFeature(
					"http://apache.org/xml/features/validation/schema", false );
			reader.setContentHandler( rangeHandler );
			reader.parse( new InputSource( new FileInputStream( xsdFile ) ) );
		} catch ( SAXException e ) {
			range = rangeHandler.getRange();
		}
		return range;
	}

	private class RangeHandler extends DefaultHandler
	{
		private Range range = new Range();

		@Override
		public void startElement( String uri, String localName, String qName,
				Attributes attributes ) throws SAXException
		{
			if ( "row".equals( attributes.getValue( "name" ) ) ) {
				if ( version1 ) {
					// version 1.0 hat rowType
					if ( "rowType".equals( attributes.getValue( "type" ) ) ) {
						this.range.min = getRange(
								attributes.getValue( "minOccurs" ) );
						this.range.max = getRange(
								attributes.getValue( "maxOccurs" ) );
						throw new SAXException();
					}
				} else {
					// version 2.0 har recordType
					if ( "recordType"
							.equals( attributes.getValue( "type" ) ) ) {
						this.range.min = getRange(
								attributes.getValue( "minOccurs" ) );
						this.range.max = getRange(
								attributes.getValue( "maxOccurs" ) );
						throw new SAXException();
					}
				}
			}
		}

		private int getRange( String attributeValue )
		{
			int value = 1;
			if ( attributeValue == null ) {
				return value;
			}
			if ( attributeValue.equalsIgnoreCase( "unbounded" ) ) {
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

	public boolean prepareValidation( ValidationContext validationContext )
			throws IOException, JDOMException, Exception
	{
		return false;
	}
}
