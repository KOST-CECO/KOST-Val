/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationEcolumnException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.bean.SiardTable;
import ch.kostceco.tools.kostval.validation.bean.ValidationContext;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationEcolumnModule;

/** Validierungsschritt E (Spalten-Validierung) Wurden die Angaben aus metadata.xml korrekt in die
 * tableZ.xsd-Dateien �bertragen? valid --> gleiche Spaltendefinitionen (Anzahl, Type, Nullable)
 * 
 * 
 * The module <code>ValidationEcolumnModule</code> validates the columns specified in the file
 * <code>metadata.xml</code> against the according XML schema files.
 * 
 * <p>
 * The column validation process consists of three steps. a) The validation of the attribute count,
 * b) the validation of the attribute's occurrence - which conforms to the nullable attribute of the
 * table definition - and c) the validation of the attribute's type. The table columns are only
 * valid if - and only if - the three validation steps are completed successfully.
 * 
 * The table and their columns are described in the file <code>metadata.xml</code> The element
 * <code> &lt;table&gt</code> and its children are decisive for the table description: <blockquote>
 * 
 * <pre>
 *  <code>&lt;table&gt</code>
 * 	<code>&lt;name&gtTABLE_NAME&lt;/name&gt</code>
 * 	<code>&lt;folder&gtFOLDER_NAME&lt;/folder&gt</code>
 * 	<code>&lt;description&gtDESCRIPTION&lt;/description&gt</code>
 * 	<code>&lt;columns&gt</code>
 * 	<code>&lt;column&gt</code>
 * 	<code>&lt;name&gtCOLUMN_NAME&lt;/name&gt</code>
 * 	<code>&lt;type&gtCOLUMN_TYPE&lt;/type&gt</code>
 * 	<code>&lt;typeOriginal&gtCOLUMN_ORIGINAL_TYPE&lt;/typeOriginal&gt</code>
 * 	<code>&lt;nullable&gt</code>COLUMN_MINIMAL_OCCURRENCE<code>&lt;nullable&gt</code>
 * 	<code>&lt;description&gt</code>COLUMN_DESCRIPTION<code>&lt;description&gt</code>
 * 	<code>&lt;/column&gt</code>
 * 	<code>&lt;/columns&gt</code>
 * <code>&lt;/table&gt</code>
 * </pre>
 * 
 * </blockquote
 * 
 * @author Do Olivier Debenath
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationEcolumnModuleImpl extends ValidationModuleImpl implements
		ValidationEcolumnModule
{
	// TODO: schema1 aus Beispiel funktioniert noch nicht (null)
	/* Validation Context */
	private ValidationContext					validationContext;
	/* Service related properties */
	/* Validation error related properties */
	private StringBuilder							incongruentColumnCount;
	private StringBuilder							incongruentColumnOccurrence;
	private StringBuilder							incongruentColumnType;
	private StringBuilder							warningColumnType;
	private StringBuilder							incongruentColumnSequence;
	public static Map<String, String>	configMapFinal	= null;

	boolean														udtColumn				= false;

	/** Start of the column validation. The <code>validate</code> method act as a controller. First it
	 * initializes the validation by calling the <code>validationPrepare()</code> method and
	 * subsequently it starts the validation process by executing the validation subroutines:
	 * <code>validateAttributeCount()</code>, <code>validateAttributeOccurrence()</code> and finally
	 * <code>validateAttributeType()</code>.
	 * 
	 * @param SIARD
	 *          archive containing the tables whose columns are to be validated
	 * @exception ValidationEcolumnException
	 *              if the representation of the columns is invalid */
	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap )
			throws ValidationEcolumnException
	{
		configMapFinal = configMap;
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWork.equals( "no" ) ) {
			// keine Ausgabe
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}

		// All over validation flag
		boolean valid = true;
		// Important validation flag
		boolean congruentColumnCount = false;
		ValidationContext validationContext = new ValidationContext();
		validationContext.setSiardArchive( valDatei );
		this.setValidationContext( validationContext );
		try {
			// Initialize the validation context
			valid = (prepareValidation( this.getValidationContext() ) == false ? false : true);
			// Get the prepared SIARD tables from the validation context
			valid = (this.getValidationContext().getSiardTables() == null ? false : true);
			// Validates the number of the attributes
			congruentColumnCount = validateColumnCount( this.getValidationContext(), configMap );
			if ( congruentColumnCount == false ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_E_INVALID_ATTRIBUTE_COUNT,
										this.getIncongruentColumnCount() ) );
			}
			// Validates the nullable property in metadata.xml
			if ( validateColumnOccurrence( this.getValidationContext(), configMap ) == false ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_E_INVALID_ATTRIBUTE_OCCURRENCE,
										this.getIncongruentColumnOccurrence() ) );
			}
			// Validates the type of table attributes in metadata.xml
			if ( validateColumnType( this.getValidationContext(), configMap ) == false ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_E_INVALID_ATTRIBUTE_TYPE,
										this.getIncongruentColumnType() ) );
				if ( udtColumn ) {
					// UDT-Warning mit ausgeben wenn vorhanden: MESSAGE_XML_E_TYPE_NOT_VALIDATED
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
									+ getTextResourceService().getText( MESSAGE_XML_E_TYPE_NOT_VALIDATED,
											this.getWarningColumnType() ) );
				}
			} else {
				if ( udtColumn ) {
					// UDT-Warning mit ausgeben wenn vorhanden: MESSAGE_XML_E_TYPE_NOT_VALIDATED
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
									+ getTextResourceService().getText( MESSAGE_XML_E_TYPE_NOT_VALIDATED,
											this.getWarningColumnType() ) );
				}
			}
		} catch ( Exception e ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return valid;
	}

	/* [E.0]Prepares the validation process by executing the following steps and stores the resultsto
	 * the validation context:- Getting the Properties- Initializing the SIARD path configuration-
	 * Extracting the SIARD package- Pick up the metadata.xml- Prepares the XML Access (without
	 * XPath)- Prepares the table information from metadata.xml */
	@Override
	public boolean prepareValidation( ValidationContext validationContext ) throws IOException,
			JDOMException, Exception
	{
		// All over preparation flag
		boolean prepared = true;
		// Load the Java properties to the validation context
		boolean propertiesLoaded = initializeProperties();
		if ( propertiesLoaded == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_PROPERTIES_ERROR ) );
		}
		// Initialize internal path configuration of the SIARD archive
		boolean pathInitialized = initializePath( validationContext, configMapFinal );
		if ( pathInitialized == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_PATH_ERROR ) );
		}
		// Extract the SIARD archive and distribute the content to the validation context
		boolean siardArchiveExtracted = extractSiardArchive( validationContext, configMapFinal );
		if ( siardArchiveExtracted == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_EXTRACT_ERROR ) );
		}
		// Pick the metadata.xml and load it to the validation context
		boolean metadataXMLpicked = pickMetadataXML( validationContext, configMapFinal );
		if ( metadataXMLpicked == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_METADATA_ACCESS_ERROR ) );
		}
		// Prepare the XML configuration and store it to the validation context
		boolean xmlAccessPrepared = prepareXMLAccess( validationContext );
		if ( xmlAccessPrepared == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_XML_ACCESS_ERROR ) );
		}
		// Prepare the data to be validated such as metadata.xml and the according XML schemas
		boolean validationDataPrepared = prepareValidationData( validationContext, configMapFinal );
		if ( validationDataPrepared == false ) {
			prepared = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
							+ getTextResourceService().getText( MESSAGE_XML_E_PREVALIDATION_ERROR ) );
		}
		return prepared;
	}

	/* [E.1]Counts the columns in metadata.xml and compares it to the number of columns in the
	 * according XML schema files */
	private boolean validateColumnCount( ValidationContext validationContext,
			Map<String, String> configMap ) throws Exception
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean validDatabase = true;
		StringBuilder namesOfInvalidTables = new StringBuilder();
		List<SiardTable> siardTables = validationContext.getSiardTables();
		// Initializing validation Logging
		// Iteratic over all SIARD tables to count the table attributes
		// and compare it to the number of registered attributes in the
		// according XML schemas
		for ( SiardTable siardTable : siardTables ) {
			int metadataXMLColumnsCount = siardTable.getMetadataXMLElements().size();
			int tableXSDColumnsCount = siardTable.getTableXSDElements().size();
			// Checks whether the columns count is correct
			if ( metadataXMLColumnsCount != tableXSDColumnsCount ) {
				int columnsDifference = metadataXMLColumnsCount - tableXSDColumnsCount;
				validDatabase = false;
				namesOfInvalidTables.append( (namesOfInvalidTables.length() > 0) ? ", " : "" );
				namesOfInvalidTables.append( siardTable.getTableName() );
				namesOfInvalidTables.append( ".xsd" );
				namesOfInvalidTables.append( "(" );
				namesOfInvalidTables.append( (columnsDifference > 0 ? "+" + columnsDifference
						: columnsDifference) );
				namesOfInvalidTables.append( ")" );
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "E-   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "E\\   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "E|   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "E/   " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		// Writing back error log
		this.setIncongruentColumnCount( namesOfInvalidTables );
		// Return the current validation state
		return validDatabase;
	}

	/* [E.2]Compares the <nullable> Element of the metadata.xml to the minOccurs attributesin the
	 * according XML schemata */
	private boolean validateColumnOccurrence( ValidationContext validationContext,
			Map<String, String> configMap ) throws Exception
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean validTable;
		boolean validDatabase;
		List<SiardTable> siardTables = validationContext.getSiardTables();
		StringBuilder namesOfInvalidTables = new StringBuilder();
		StringBuilder namesOfInvalidColumns = new StringBuilder();
		validDatabase = true;
		// Iterate over the SIARD tables and verify the nullable attribute
		for ( SiardTable siardTable : siardTables ) {
			String minOccurs = new String();
			validTable = true;
			// Number of attributes in metadata.xml
			int metadataXMLColumnsCount = siardTable.getMetadataXMLElements().size();
			// Number of attributes in schema.xsd
			int tableXSDColumnsCount = siardTable.getTableXSDElements().size();
			/* Counter. In order to prevent indexOutOfBorder errors columnsCounter is assigned to the
			 * smaller number of columns */
			int columnCount = (metadataXMLColumnsCount >= tableXSDColumnsCount) ? tableXSDColumnsCount
					: metadataXMLColumnsCount;
			// Element/Attributes of the actual SIARD table
			List<Element> xmlElements = siardTable.getMetadataXMLElements();
			// Elements/Attributes of the according XML schema
			List<Element> xsdElements = siardTable.getTableXSDElements();
			Namespace xmlNamespace = validationContext.getXmlNamespace();
			for ( int i = 0; i < columnCount; i++ ) {
				String nullable = new String();
				// Actual Element of the metadata.xml
				Element xmlElement = xmlElements.get( i );
				// Actual Element of the according XML schema
				Element xsdElement = xsdElements.get( i );
				String nullableElementDescription = "nullable";
				String minuOccursAttributeDescription = "minOccurs";
				String nameAttributeDescription = "name";
				// Value of the minOccurs attribute in the according XML schema
				if ( xsdElement.getAttributeValue( minuOccursAttributeDescription ) == null ) {
					minOccurs = "1";
				} else {
					minOccurs = xsdElement.getAttributeValue( minuOccursAttributeDescription );
				}
				if ( xmlElement.getChild( nullableElementDescription, xmlNamespace ) == null ) {
					nullable = "true";
				} else {
					// Value of the nullable Element in metadata.xml
					nullable = xmlElement.getChild( nullableElementDescription, xmlNamespace ).getValue();
				}
				// If the nullable Element is set to true and the minOccurs attribute is null
				if ( nullable.equalsIgnoreCase( "true" ) && minOccurs.equalsIgnoreCase( "1" ) ) {
					validTable = false;
					validDatabase = false;
					namesOfInvalidColumns.append( (namesOfInvalidColumns.length() > 0) ? ", " : "" );
					namesOfInvalidColumns.append( xsdElement.getAttributeValue( nameAttributeDescription ) );
					// If the nullable Element is set to true and the minOccurs attribute is set to zero
				} else if ( nullable.equalsIgnoreCase( "true" ) && minOccurs.equalsIgnoreCase( "0" ) ) {
				} else if ( nullable.equalsIgnoreCase( "false" ) && minOccurs.equalsIgnoreCase( "1" ) ) {
				} else if ( nullable.equalsIgnoreCase( "false" ) && minOccurs.equalsIgnoreCase( "0" ) ) {
					validTable = false;
					validDatabase = false;
					namesOfInvalidColumns.append( (namesOfInvalidColumns.length() > 0) ? ", " : "" );
					namesOfInvalidColumns.append( xsdElement.getAttributeValue( nameAttributeDescription ) );
				}
			}
			if ( validTable == false ) {
				namesOfInvalidTables.append( (namesOfInvalidTables.length() > 0) ? ", " : "" );
				namesOfInvalidTables.append( siardTable.getTableName() );
				namesOfInvalidTables.append( ".xsd" );
				namesOfInvalidTables.append( "(" );
				namesOfInvalidTables.append( namesOfInvalidColumns );
				namesOfInvalidTables.append( ")" );
				namesOfInvalidColumns = null;
				namesOfInvalidColumns = new StringBuilder();
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "E-   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "E\\   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "E|   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "E/   " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		// Writing back error log
		this.setIncongruentColumnOccurrence( namesOfInvalidTables );
		return validDatabase;
	}

	/* [E.3]Compares the column types in the metadata.xml to the accordingXML schemata */
	private boolean validateColumnType( ValidationContext validationContext,
			Map<String, String> configMap ) throws Exception
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean validTable;
		udtColumn = false;
		boolean validDatabase;
		Properties properties = validationContext.getValidationProperties();
		List<SiardTable> siardTables = validationContext.getSiardTables();
		StringBuilder namesOfInvalidTables = new StringBuilder();
		StringBuilder namesOfInvalidColumns = new StringBuilder();
		StringBuilder namesOfUdtTables = new StringBuilder();
		StringBuilder namesOfUdtColumns = new StringBuilder();
		// List of all XML column elements to verify the allover sequence
		List<String> xmlElementSequence = new ArrayList<String>();
		// List of all XSD column elements
		List<String> xsdElementSequence = new ArrayList<String>();
		// Iterate over the SIARD tables and verify the column types
		validDatabase = true;
		for ( SiardTable siardTable : siardTables ) {
			// Elements of the actual SIARD table
			List<Element> xmlElements = siardTable.getMetadataXMLElements();
			// Elements of the according XML schema
			List<Element> xsdElements = siardTable.getTableXSDElements();
			// Number of attributes in metadata.xml
			int metadataXMLColumnsCount = xmlElements.size();
			// Number of attributes in schema.xsd
			int tableXSDColumnsCount = xsdElements.size();
			/* Counter. In order to prevent indexOutOfBorder errors columnsCounter is assigned to the
			 * smaller number of columns */
			int columnCount = (metadataXMLColumnsCount >= tableXSDColumnsCount) ? tableXSDColumnsCount
					: metadataXMLColumnsCount;
			validTable = true;
			udtColumn = false;
			// Verify whether the number of column elements in XML and XSD are equal
			for ( int i = 0; i < columnCount; i++ ) {
				Element xmlElement = xmlElements.get( i );
				Element xsdElement = xsdElements.get( i );
				// Retrieve the Elements name
				String xmlTypeElementName = "type";
				String xsdTypeAttribute = "type";
				String xsdAttribute = "name";
				String leftSide = "";
				String rightSide = "";
				String columnName = xsdElement.getAttributeValue( xsdAttribute );
				Namespace xmlNamespace = validationContext.getXmlNamespace();
				if ( xmlElement.getChild( xmlTypeElementName, xmlNamespace ) == null
						|| xmlElement.getChild( xmlTypeElementName, xmlNamespace ).getValue() == null ) {
					/* TODO: bei UDT stehen beide typen in den subelementen (beide null). UDT koennen wiederum
					 * Array oder UDT enthalen und diese mehrfach. Entsprechend wird das noch nicht
					 * unterstützt! */
					udtColumn = true;
					// validDatabase = unverändert -> nur Warnung
					namesOfUdtColumns.append( (namesOfUdtColumns.length() > 0) ? ", " : "" );
					namesOfUdtColumns.append( columnName );
					// Keine Typenvalidierung dieser Spalte
				} else {
					// Typenvalidierung der Spalte möglich

					// Retrieve the original column type from metadata.xml
					leftSide = xmlElement.getChild( xmlTypeElementName, xmlNamespace ).getValue();
					// Retrieve the original column type from table.xsd
					rightSide = xsdElement.getAttributeValue( xsdTypeAttribute );
					if ( rightSide == null || rightSide == "" ) {
						/* null bei Array, weil type in jedem subelement steht (alle muessen gleich sein) */

						List<Element> childListElement = xsdElement.getChildren();
						for ( int y = 0; y < childListElement.size(); y++ ) {
							Element subElement = childListElement.get( y );
							List<Element> arrayListElement = subElement.getChildren();
							for ( int x = 0; x < arrayListElement.size(); x++ ) {
								Element subSubElement = arrayListElement.get( x );
								List<Element> array2ListElement = subSubElement.getChildren();
								String array0 = "";
								String arrays = "";
								for ( int z = 0; z < array2ListElement.size(); z++ ) {
									Element arrayElement = array2ListElement.get( z );
									arrays = arrayElement.getAttributeValue( "type" );
									if ( array0.equals( "" ) ) {
										array0 = arrays;
										rightSide = array0;
									} else {
										// alle arrys müssen den gleichen Typ haben
										if ( !array0.equalsIgnoreCase( arrays ) ) {
											validTable = false;
											validDatabase = false;
											namesOfInvalidColumns.append( (namesOfInvalidColumns.length() > 0) ? ", "
													: "" );
											namesOfInvalidColumns.append( columnName );
											getMessageService().logError(
													getTextResourceService().getText( MESSAGE_XML_MODUL_E_SIARD )
															+ getTextResourceService().getText( MESSAGE_XML_E_ARRAY,
																	(siardTable.getTableName() + ".xsd(" + columnName + ")") ) );
										}
									}
								}
							}
						}
					}
					String delimiter = "(";
					// Trim the column types - eliminates the brackets and specific numeric parameters
					String trimmedExpectedType = trimLeftSideType( leftSide, delimiter );
					// Designing expected column type in table.xsd, called "rightSide"
					String pathToWorkDir = configMap.get( "PathToWorkDir" );
					pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
					File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
							.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );
					Boolean version1 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" ).contains(
							"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
					Boolean version2 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" ).contains(
							"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );

					/* Interval wird vereinfacht kontrolliert */
					if ( trimmedExpectedType.startsWith( "INTERVAL" )
							|| trimmedExpectedType.startsWith( "interval" ) ) {
						trimmedExpectedType = "INTERVAL";
					}

					if ( version1 ) {
						trimmedExpectedType = "1_" + trimmedExpectedType;
					} else if ( version2 ) {
						trimmedExpectedType = "2_" + trimmedExpectedType;
					}
					String expectedType = properties.getProperty( trimmedExpectedType );
					// In case the expected type does not exist
					if ( expectedType == null ) {
						System.out.print( " Unknown Type " + trimmedExpectedType );
						expectedType = "Unknown Type";
						validTable = false;
						validDatabase = false;
						namesOfInvalidColumns.append( (namesOfInvalidColumns.length() > 0) ? ", " : "" );
						namesOfInvalidColumns.append( columnName + "(" + trimmedExpectedType + "=?)" );
					} else if ( !expectedType.equalsIgnoreCase( rightSide ) ) {
						/* grössere strings dürfen auch als clob separat abgespeichert werden. Das gleiche für
						 * hexBinarx als blob
						 * 
						 * xs:hexBinary ~ blobType && xs:string ~ clobType */
						if ( expectedType.equalsIgnoreCase( "xs:hexBinary" )
								&& rightSide.equalsIgnoreCase( "blobType" ) ) {
							// valid: xs:hexBinary ~ blobType
						} else if ( expectedType.equalsIgnoreCase( "xs:string" )
								&& rightSide.equalsIgnoreCase( "clobType" ) ) {
							// valid: xs:string ~ clobType
						} else {
							validTable = false;
							validDatabase = false;
							String columnNameLsRs = columnName + " (" + expectedType + " - " + rightSide + ")";
							namesOfInvalidColumns.append( (namesOfInvalidColumns.length() > 0) ? ", " : "" );
							namesOfInvalidColumns.append( columnNameLsRs );
						}
					}
					// Convey the column types for the all over sequence test [E.4]
					xmlElementSequence.add( expectedType );
					xsdElementSequence.add( rightSide );
				}
			}
			if ( !validTable ) {
				namesOfInvalidTables.append( (namesOfInvalidTables.length() > 0) ? ", " : "" );
				namesOfInvalidTables.append( siardTable.getTableName() );
				namesOfInvalidTables.append( ".xsd" );
				namesOfInvalidTables.append( "(" );
				namesOfInvalidTables.append( namesOfInvalidColumns );
				namesOfInvalidTables.append( ")" );
				namesOfInvalidColumns = null;
				namesOfInvalidColumns = new StringBuilder();
			}
			if ( udtColumn ) {
				namesOfUdtTables.append( (namesOfUdtTables.length() > 0) ? ", " : "" );
				namesOfUdtTables.append( siardTable.getTableName() );
				namesOfUdtTables.append( ".xsd" );
				namesOfUdtTables.append( "(" );
				namesOfUdtTables.append( namesOfUdtColumns );
				namesOfUdtTables.append( ")" );
				namesOfUdtColumns = null;
				namesOfUdtColumns = new StringBuilder();
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "E-   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "E\\   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "E|   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "E/   " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		this.setIncongruentColumnType( namesOfInvalidTables );
		this.setWarningColumnType( namesOfUdtTables );
		// Save the allover column elements for [E.4]
		validationContext.setXmlElementsSequence( xmlElementSequence );
		validationContext.setXsdElementsSequence( xsdElementSequence );
		this.setValidationContext( validationContext );
		// Return the current validation state
		return validDatabase;
	}

	/* Internal helper methods */
	/* [E.0.1]Load the validation properties */
	private boolean initializeProperties() throws IOException
	{
		ValidationContext validationContext = this.getValidationContext();
		boolean successfullyCommitted = false;
		// Initializing the validation context properties
		String propertiesName = "/validation.properties";
		// Get the properties file
		InputStream propertiesInputStream = getClass().getResourceAsStream( propertiesName );
		Properties properties = new Properties();
		properties.load( propertiesInputStream );
		validationContext.setValidationProperties( properties );
		// Log messages are created inside the if clause to catch missing properties errors
		if ( validationContext.getValidationProperties() != null ) {
			successfullyCommitted = true;
		} else {
			successfullyCommitted = false;
			throw new IOException();
		}
		return successfullyCommitted;
	}

	/* [E.0.2]Initializes the SIARD path configuration */
	private boolean initializePath( ValidationContext validationContext, Map<String, String> configMap )
			throws Exception
	{
		boolean successfullyCommitted = false;
		StringBuilder headerPath = new StringBuilder();
		StringBuilder contentPath = new StringBuilder();
		// Initializing validation Logging
		String workDir = configMap.get( "PathToWorkDir" );
		// Preparing the internal SIARD directory structure
		headerPath.append( workDir );
		headerPath.append( File.separator );
		headerPath.append( "header" );
		contentPath.append( workDir );
		contentPath.append( File.separator );
		contentPath.append( "content" );
		// Writing back the directory structure to the validation context
		validationContext.setHeaderPath( headerPath.toString() );
		validationContext.setContentPath( contentPath.toString() );
		if ( validationContext.getHeaderPath() != null && validationContext.getContentPath() != null ) {
			successfullyCommitted = true;
			this.setValidationContext( validationContext );
		} else {
			successfullyCommitted = false;
			this.setValidationContext( null );
			throw new Exception();
		}
		return successfullyCommitted;
	}

	/* [E.0.5]Prepares the XML access */
	private boolean prepareXMLAccess( ValidationContext validationContext ) throws JDOMException,
			IOException, Exception
	{
		boolean successfullyCommitted = false;
		File metadataXML = validationContext.getMetadataXML();
		InputStream inputStream = new FileInputStream( metadataXML );
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build( inputStream );
		// Assigning JDOM Document to the validation context
		validationContext.setMetadataXMLDocument( document );
		String xmlPrefix = "pre";
		String xsdPrefix = "xs";
		// Setting the namespaces to access metadata.xml and the different table.xsd
		Element rootElement = document.getRootElement();
		String namespaceURI = rootElement.getNamespaceURI();
		Namespace xmlNamespace = Namespace.getNamespace( xmlPrefix, namespaceURI );
		Namespace xsdNamespace = Namespace.getNamespace( xsdPrefix, namespaceURI );
		// Assigning prefix to the validation context
		validationContext.setXmlPrefix( xmlPrefix );
		validationContext.setXsdPrefix( xsdPrefix );
		// Assigning namespace info to the validation context
		validationContext.setXmlNamespace( xmlNamespace );
		validationContext.setXsdNamespace( xsdNamespace );
		if ( validationContext.getXmlNamespace() != null && validationContext.getXsdNamespace() != null
				&& validationContext.getXmlPrefix() != null && validationContext.getXsdPrefix() != null
				&& validationContext.getMetadataXMLDocument() != null ) {
			this.setValidationContext( validationContext );
			successfullyCommitted = true;
		} else {
			successfullyCommitted = false;
			this.setValidationContext( null );
			throw new Exception();
		}
		return successfullyCommitted;
	}

	/* Trimming the search terms for column type validation */
	private String trimLeftSideType( String leftside, String delimiter ) throws Exception
	{
		return (leftside.indexOf( delimiter ) > -1) ? leftside.substring( 0,
				leftside.indexOf( delimiter ) ) : leftside;
	}

	/* [E.0.3]Extracting the SIARD packages */
	private boolean extractSiardArchive( ValidationContext validationContext,
			Map<String, String> configMap ) throws FileNotFoundException, IOException, Exception
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean sucessfullyCommitted = false;
		// Initializing the access to the SIARD archive
		Zip64File zipfile = new Zip64File( validationContext.getSiardArchive() );
		List<FileEntry> fileEntryList = zipfile.getListFileEntries();
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );
		// Initializing the resulting Hashmap containing all files, indexed by its absolute path
		HashMap<String, File> extractedSiardFiles = new HashMap<String, File>();
		// Iterating over the whole SIARD archive
		for ( FileEntry fileEntry : fileEntryList ) {
			if ( !fileEntry.isDirectory() ) {
				byte[] buffer = new byte[8192];
				EntryInputStream eis = zipfile.openEntryInputStream( fileEntry.getName() );
				File newFile = new File( tmpDir, fileEntry.getName() );
				File parent = newFile.getParentFile();
				if ( !parent.exists() ) {
					parent.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream( newFile );
				for ( int iRead = eis.read( buffer ); iRead >= 0; iRead = eis.read( buffer ) ) {
					fos.write( buffer, 0, iRead );
				}
				extractedSiardFiles.put( newFile.getPath(), newFile );
				eis.close();
				fos.close();
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "E-   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "E\\   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "E|   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "E/   " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		validationContext.setSiardFiles( extractedSiardFiles );
		// Checks whether the siard extraction succeeded or not
		if ( validationContext.getSiardFiles() != null ) {
			this.setValidationContext( validationContext );
			sucessfullyCommitted = true;
		} else {
			this.setValidationContext( null );
			sucessfullyCommitted = false;
			throw new Exception();
		}
		return sucessfullyCommitted;
	}

	/* [E.0.4]Pick up the metadata.xml from the SIARD package */
	private boolean pickMetadataXML( ValidationContext validationContext,
			Map<String, String> configMap ) throws Exception
	{
		boolean successfullyCommitted = false;
		StringBuilder pathToMetadataXML = new StringBuilder();
		pathToMetadataXML.append( configMap.get( "PathToWorkDir" ) );
		pathToMetadataXML.append( File.separator );
		pathToMetadataXML.append( "header" );
		pathToMetadataXML.append( File.separator );
		pathToMetadataXML.append( "metadata.xml" );
		HashMap<String, File> siardFiles = validationContext.getSiardFiles();
		File metadataXML = siardFiles.get( pathToMetadataXML.toString() );
		// Retreave the metadata.xml from the SIARD archive and writes it back to the validation context
		validationContext.setMetadataXML( metadataXML );
		// Checks whether the metadata.xml could be picked up
		if ( validationContext.getMetadataXML() != null ) {
			this.setValidationContext( validationContext );
			successfullyCommitted = true;
		} else {
			this.setValidationContext( null );
			successfullyCommitted = false;
			throw new Exception();
		}
		return successfullyCommitted;
	}

	/* [E.0.6]Preparing the data to be validated */
	private boolean prepareValidationData( ValidationContext validationContext,
			Map<String, String> configMap ) throws JDOMException, IOException, Exception
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "E    " );
			System.out.print( "\b\b\b\b\b" );
		}
		boolean successfullyCommitted = false;
		Namespace xmlNamespace = validationContext.getXmlNamespace();
		// Gets the tables to be validated
		List<SiardTable> siardTables = new ArrayList<SiardTable>();
		Document document = validationContext.getMetadataXMLDocument();
		Element rootElement = document.getRootElement();
		String workingDirectory = configMap.get( "PathToWorkDir" );
		String siardSchemasElementsName = "schemas";
		// Gets the list of <schemas> elements from metadata.xml
		List<Element> siardSchemasElements = rootElement.getChildren( siardSchemasElementsName,
				xmlNamespace );
		for ( Element siardSchemasElement : siardSchemasElements ) {
			// Gets the list of <schema> elements from metadata.xml
			List<Element> siardSchemaElements = siardSchemasElement.getChildren( "schema", xmlNamespace );
			// Iterating over all <schema> elements
			for ( Element siardSchemaElement : siardSchemaElements ) {
				String schemaFolderName = siardSchemaElement.getChild( "folder", xmlNamespace ).getValue();
				if ( siardSchemaElement.getChild( "tables", xmlNamespace ) != null ) {
					Element siardTablesElement = siardSchemaElement.getChild( "tables", xmlNamespace );
					List<Element> siardTableElements = siardTablesElement.getChildren( "table", xmlNamespace );
					// Iterating over all containing table elements
					for ( Element siardTableElement : siardTableElements ) {
						Element siardColumnsElement = siardTableElement.getChild( "columns", xmlNamespace );
						List<Element> siardColumnElements = siardColumnsElement.getChildren( "column",
								xmlNamespace );
						String tableName = siardTableElement.getChild( "folder", xmlNamespace ).getValue();
						SiardTable siardTable = new SiardTable();
						siardTable.setMetadataXMLElements( siardColumnElements );
						siardTable.setTableName( tableName );
						String siardTableFolderName = siardTableElement.getChild( "folder", xmlNamespace )
								.getValue();
						StringBuilder pathToTableSchema = new StringBuilder();
						// Preparing access to the according XML schema file
						pathToTableSchema.append( workingDirectory );
						pathToTableSchema.append( File.separator );
						pathToTableSchema.append( "content" );
						pathToTableSchema.append( File.separator );
						pathToTableSchema.append( schemaFolderName.replaceAll( " ", "" ) );
						pathToTableSchema.append( File.separator );
						pathToTableSchema.append( siardTableFolderName.replaceAll( " ", "" ) );
						pathToTableSchema.append( File.separator );
						pathToTableSchema.append( siardTableFolderName.replaceAll( " ", "" ) );
						pathToTableSchema.append( ".xsd" );
						// Retrieve the according XML schema
						File tableSchema = validationContext.getSiardFiles().get( pathToTableSchema.toString() );
						SAXBuilder builder = new SAXBuilder();
						Document tableSchemaDocument = builder.build( tableSchema );
						Element tableSchemaRootElement = tableSchemaDocument.getRootElement();
						Namespace namespace = tableSchemaRootElement.getNamespace();
						// Getting the tags from XML schema to be validated
						Element tableSchemaComplexType = tableSchemaRootElement.getChild( "complexType",
								namespace );
						Element tableSchemaComplexTypeSequence = tableSchemaComplexType.getChild( "sequence",
								namespace );
						List<Element> tableSchemaComplexTypeElements = tableSchemaComplexTypeSequence
								.getChildren( "element", namespace );
						siardTable.setTableXSDElements( tableSchemaComplexTypeElements );
						siardTables.add( siardTable );
						// Writing back the List off all SIARD tables to the validation context
						validationContext.setSiardTables( siardTables );
					}
				} else {
					// Kein Fehler sondern leeres schema
				}
			}
			if ( showOnWork ) {
				if ( onWork == 410 ) {
					onWork = 2;
					System.out.print( "E-   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 110 ) {
					onWork = onWork + 1;
					System.out.print( "E\\   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 210 ) {
					onWork = onWork + 1;
					System.out.print( "E|   " );
					System.out.print( "\b\b\b\b\b" );
				} else if ( onWork == 310 ) {
					onWork = onWork + 1;
					System.out.print( "E/   " );
					System.out.print( "\b\b\b\b\b" );
				} else {
					onWork = onWork + 1;
				}
			}
		}
		if ( validationContext.getSiardTables().size() > 0 ) {
			this.setValidationContext( validationContext );
			successfullyCommitted = true;
		} else {
			this.setValidationContext( null );
			successfullyCommitted = false;
			throw new Exception();
		}
		return successfullyCommitted;
	}

	// Setter and Getter methods

	/** @return the validationContext */
	public ValidationContext getValidationContext()
	{
		return validationContext;
	}

	/** @param validationContext
	 *          the validationContext to set */
	public void setValidationContext( ValidationContext validationContext )
	{
		this.validationContext = validationContext;
	}

	/** @return the incongruentColumnCount */
	public StringBuilder getIncongruentColumnCount()
	{
		return incongruentColumnCount;
	}

	/** @param incongruentColumnCount
	 *          the incongruentColumnCount to set */
	public void setIncongruentColumnCount( StringBuilder incongruentColumnCount )
	{
		this.incongruentColumnCount = incongruentColumnCount;
	}

	/** @return the incongruentColumnOccurrence */
	public StringBuilder getIncongruentColumnOccurrence()
	{
		return incongruentColumnOccurrence;
	}

	/** @param incongruentColumnOccurrence
	 *          the incongruentColumnOccurrence to set */
	public void setIncongruentColumnOccurrence( StringBuilder incongruentColumnOccurrence )
	{
		this.incongruentColumnOccurrence = incongruentColumnOccurrence;
	}

	/** @return the incongruentColumnType */
	public StringBuilder getIncongruentColumnType()
	{
		return incongruentColumnType;
	}

	/** @param incongruentColumnType
	 *          the incongruentColumnType to set */
	public void setIncongruentColumnType( StringBuilder incongruentColumnType )
	{
		this.incongruentColumnType = incongruentColumnType;
	}

	/** @return the warningColumnType */
	public StringBuilder getWarningColumnType()
	{
		return warningColumnType;
	}

	/** @param warningColumnType
	 *          the warningColumnType to set */
	public void setWarningColumnType( StringBuilder warningColumnType )
	{
		this.warningColumnType = warningColumnType;
	}

	/** @return the incongruentColumnSequence */
	public StringBuilder getIncongruentColumnSequence()
	{
		return incongruentColumnSequence;
	}

	/** @param incongruentColumnSequence
	 *          the incongruentColumnSequence to set */
	public void setIncongruentColumnSequence( StringBuilder incongruentColumnSequence )
	{
		this.incongruentColumnSequence = incongruentColumnSequence;
	}
}
