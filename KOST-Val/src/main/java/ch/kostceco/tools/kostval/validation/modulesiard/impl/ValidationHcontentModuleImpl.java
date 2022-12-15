/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.kostceco.tools.kosttools.fileservice.Xmllint;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationHcontentException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationHcontentModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt H (Content-Validierung) Sind die XML-Dateien im content
 * valid zu ihrer Schema-Definition (XSD-Dateien)? valid --> tableZ.xml valid zu
 * tableZ.xsd
 * 
 * @author Ec Christian Eugster
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

@SuppressWarnings("deprecation")
public class ValidationHcontentModuleImpl extends ValidationModuleImpl
		implements ValidationHcontentModule
{
	private boolean				min			= false;

	Boolean						version1	= false;
	Boolean						version2	= false;

	private static final int	UNBOUNDED	= -1;

	private XMLReader			reader;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationHcontentException
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
			System.out.print( "H    " );
			System.out.print( "\b\b\b\b\b" );
		} else if ( onWorkConfig.equals( "nomin" ) ) {
			min = true;
		}

		boolean valid = true;
		try {
			/*
			 * Extract the metadata.xml from the temporary work folder and build
			 * a jdom document
			 */
			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File workDir = new File( pathToWorkDir );
			File metadataXml = new File(
					new StringBuilder( pathToWorkDir ).append( File.separator )
							.append( "header" ).append( File.separator )
							.append( "metadata.xml" ).toString() );
			InputStream fin = new FileInputStream( metadataXml );
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build( fin );

			/*
			 * read the document and for each schema and table entry verify
			 * existence in temporary extracted structure
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
				Element schemaFolder = schema.getChild( "folder", ns );
				File schemaPath = new File( new StringBuilder( pathToWorkDir )
						.append( File.separator ).append( "content" )
						.append( File.separator )
						.append( schemaFolder.getText() ).toString() );
				if ( schemaPath.isDirectory() ) {
					if ( schema.getChild( "tables", ns ) != null ) {

						Element[] tables = schema.getChild( "tables", ns )
								.getChildren( "table", ns )
								.toArray( new Element[0] );
						for ( Element table : tables ) {
							Element tableFolder = table.getChild( "folder",
									ns );
							File tablePath = new File( new StringBuilder(
									schemaPath.getAbsolutePath() )
											.append( File.separator )
											.append( tableFolder.getText() )
											.toString() );
							if ( tablePath.isDirectory() ) {
								File tableXml = new File( new StringBuilder(
										tablePath.getAbsolutePath() )
												.append( File.separator )
												.append( tableFolder.getText()
														+ ".xml" )
												.toString() );
								File tableXsd = new File( new StringBuilder(
										tablePath.getAbsolutePath() )
												.append( File.separator )
												.append( tableFolder.getText()
														+ ".xsd" )
												.toString() );
								// hier erfolgt die Validerung
								if ( verifyRowCount( tableXml, tableXsd, locale,
										logFile ) ) {
									// valid = validate1( tableXml, tableXsd )
									// && valid;

									try {
										// Pfad zum Programm existiert die
										// Dateien?
										String checkTool = Xmllint
												.checkXmllint( dirOfJarPath );
										if ( !checkTool.equals( "OK" ) ) {
											// mindestens eine Datei fehlt fuer
											// die Validierung
											if ( min ) {
												return false;
											} else {
												Logtxt.logtxt( logFile,
														getTextResourceService()
																.getText(
																		locale,
																		MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService()
																		.getText(
																				locale,
																				MESSAGE_XML_MISSING_FILE,
																				checkTool,
																				getTextResourceService()
																						.getText(
																								locale,
																								ABORTED,
																								"XML-" ) ) );
												valid = false;
											}
										} else {
											// System.out.println("Validierung
											// mit xmllint: ");
											try {
												String resultExec = Xmllint
														.execXmllint( tableXml,
																tableXsd,
																workDir,
																dirOfJarPath,
																locale );
												if ( !resultExec
														.equals( "OK" ) ) {
													// System.out.println("Validierung
													// NICHT bestanden");
													if ( min ) {
														return false;
													} else {
														valid = false;
														String tableXmlShortString = tableXml
																.getAbsolutePath()
																.replace(
																		workDir.getAbsolutePath(),
																		"" );
														String tableXsdShortString = tableXsd
																.getAbsolutePath()
																.replace(
																		workDir.getAbsolutePath(),
																		"" );
														// val.message.xml.h.invalid.xml
														// = <Message>{0} ist
														// invalid zu
														// {1}</Message></Error>
														// val.message.xml.h.invalid.error
														// =
														// <Message>{0}</Message></Error>
														Logtxt.logtxt( logFile,
																getTextResourceService()
																		.getText(
																				locale,
																				MESSAGE_XML_MODUL_H_SIARD )
																		+ getTextResourceService()
																				.getText(
																						locale,
																						MESSAGE_XML_H_INVALID_XML,
																						tableXmlShortString,
																						tableXsdShortString ) );
														Logtxt.logtxt( logFile,
																getTextResourceService()
																		.getText(
																				locale,
																				MESSAGE_XML_MODUL_H_SIARD )
																		+ getTextResourceService()
																				.getText(
																						locale,
																						MESSAGE_XML_SERVICEMESSAGE,
																						resultExec,
																						"" ) );
													}
												} else {
													// System.out.println("Validierung
													// bestanden");
												}
											} catch ( InterruptedException e1 ) {
												valid = false;
												if ( min ) {
													return false;
												} else {
													Logtxt.logtxt( logFile,
															getTextResourceService()
																	.getText(
																			locale,
																			MESSAGE_XML_MODUL_H_SIARD )
																	+ getTextResourceService()
																			.getText(
																					locale,
																					ERROR_XML_UNKNOWN,
																					e1.getMessage()
																							+ " (InterruptedException Xmllint.execXmllint)" ) );
												}
											}
										}
									} finally {
									}
								}
							}
							if ( showOnWork ) {
								if ( onWork == 410 ) {
									onWork = 2;
									System.out.print( "H-   " );
									System.out.print( "\b\b\b\b\b" );
								} else if ( onWork == 110 ) {
									onWork = onWork + 1;
									System.out.print( "H\\   " );
									System.out.print( "\b\b\b\b\b" );
								} else if ( onWork == 210 ) {
									onWork = onWork + 1;
									System.out.print( "H|   " );
									System.out.print( "\b\b\b\b\b" );
								} else if ( onWork == 310 ) {
									onWork = onWork + 1;
									System.out.print( "H/   " );
									System.out.print( "\b\b\b\b\b" );
								} else {
									onWork = onWork + 1;
								}
							}
						}
					} else {
						// kein Fehler sondern leeres Schema
					}
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "H-   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "H\\   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "H|   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "H/   " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
			fin.close();
			// set to null
			fin = null;
		} catch ( java.io.IOException ioe ) {
			valid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_H_SIARD )
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
								MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (JDOMException)" ) );
			}
		} catch ( SAXException e ) {
			valid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (SAXException)" ) );
			}
		}

		return valid;
	}

	private class Range
	{
		public int	min	= 1;

		public int	max	= 1;
	}

	/*
	 * Verify the number of rows in the table. If the xsd minOccurs = o and
	 * maxOccurs = unbounded the validation of the numbers can't been executed.
	 * A Warning is given.
	 */
	private boolean verifyRowCount( File xmlFile, File schemaLocation,
			Locale locale, File logFile ) throws SAXException, IOException
	{
		Range range = getRange( schemaLocation );
		if ( range.min == 0 && range.max == UNBOUNDED ) {
			/*
			 * die effektive Zahl in schemaLocation (Work) konnte im H nicht
			 * hereingeschrieben werden. Eine Warnung wird herausgegeben
			 */
			if ( min ) {
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_H_TABLE_NOT_VALIDATED1,
										schemaLocation.getName() ) );
			}
			return true;
		} else {
			return true;
		}
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
		} catch ( BreakException e ) {
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
				if ( "rowType".equals( attributes.getValue( "type" ) ) ) {
					this.range.min = getRange(
							attributes.getValue( "minOccurs" ) );
					this.range.max = getRange(
							attributes.getValue( "maxOccurs" ) );
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
		private static final long serialVersionUID = 1L;
	}
}
