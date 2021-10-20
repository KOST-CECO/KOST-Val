/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

import java.io.BufferedReader;
import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

import ch.kostceco.tools.kosttools.util.StreamGobbler;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationHcontentException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationHcontentModule;

/** Validierungsschritt H (Content-Validierung) Sind die XML-Dateien im content valid zu ihrer
 * Schema-Definition (XSD-Dateien)? valid --> tableZ.xml valid zu tableZ.xsd
 * 
 * @author Ec Christian Eugster
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationHcontentModuleImpl extends ValidationModuleImpl
		implements ValidationHcontentModule
{
	private boolean						min				= false;

	Boolean										version1	= false;
	Boolean										version2	= false;

	private static final int	UNBOUNDED	= -1;

	private XMLReader					reader;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationHcontentException
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
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
			/* Extract the metadata.xml from the temporary work folder and build a jdom document */
			String pathToWorkDir = configMap.get( "PathToWorkDir" );
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
					.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );
			InputStream fin = new FileInputStream( metadataXml );
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build( fin );

			/* read the document and for each schema and table entry verify existence in temporary
			 * extracted structure */
			version1 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" )
					.contains( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			version2 = FileUtils.readFileToString( metadataXml, "ISO-8859-1" )
					.contains( "http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			Namespace ns = Namespace
					.getNamespace( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			if ( version1 ) {
				// ns = Namespace.getNamespace( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			} else if ( version2 ) {
				ns = Namespace.getNamespace( "http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			}
			// select schema elements and loop
			List<Element> schemas = document.getRootElement().getChild( "schemas", ns )
					.getChildren( "schema", ns );
			for ( Element schema : schemas ) {
				Element schemaFolder = schema.getChild( "folder", ns );
				File schemaPath = new File(
						new StringBuilder( pathToWorkDir ).append( File.separator ).append( "content" )
								.append( File.separator ).append( schemaFolder.getText() ).toString() );
				if ( schemaPath.isDirectory() ) {
					if ( schema.getChild( "tables", ns ) != null ) {

						Element[] tables = schema.getChild( "tables", ns ).getChildren( "table", ns )
								.toArray( new Element[0] );
						for ( Element table : tables ) {
							Element tableFolder = table.getChild( "folder", ns );
							File tablePath = new File( new StringBuilder( schemaPath.getAbsolutePath() )
									.append( File.separator ).append( tableFolder.getText() ).toString() );
							if ( tablePath.isDirectory() ) {
								File tableXml = new File( new StringBuilder( tablePath.getAbsolutePath() )
										.append( File.separator ).append( tableFolder.getText() + ".xml" ).toString() );
								File tableXsd = new File( new StringBuilder( tablePath.getAbsolutePath() )
										.append( File.separator ).append( tableFolder.getText() + ".xsd" ).toString() );
								// hier erfolgt die Validerung
								if ( verifyRowCount( tableXml, tableXsd, locale ) ) {
									// valid = validate1( tableXml, tableXsd ) && valid;

									// cmd: resources\xmllint\xmllint --noout --stream --schema tableXsd tableXml
									try {
										// Pfad zum Programm xmllint existiert die Dateien?
										/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein
										 * generelles TODO in allen Modulen. Zuerst immer dirOfJarPath ermitteln und
										 * dann alle Pfade mit
										 * 
										 * dirOfJarPath + File.separator +
										 * 
										 * erweitern. */
										String path = new java.io.File( KOSTVal.class.getProtectionDomain()
												.getCodeSource().getLocation().getPath() ).getAbsolutePath();
										String locationOfJarPath = path;
										String dirOfJarPath = locationOfJarPath;
										if ( locationOfJarPath.endsWith( ".jar" )
												|| locationOfJarPath.endsWith( ".exe" )
												|| locationOfJarPath.endsWith( "." ) ) {
											File file = new File( locationOfJarPath );
											dirOfJarPath = file.getParent();
										}

										String pathToxmllintExe = dirOfJarPath + File.separator + "resources"
												+ File.separator + "xmllint" + File.separator + "xmllint.exe";
										String pathToxmllintDll1 = dirOfJarPath + File.separator + "resources"
												+ File.separator + "xmllint" + File.separator + "iconv.dll";
										String pathToxmllintDll2 = dirOfJarPath + File.separator + "resources"
												+ File.separator + "xmllint" + File.separator + "libxml2.dll";
										String pathToxmllintDll3 = dirOfJarPath + File.separator + "resources"
												+ File.separator + "xmllint" + File.separator + "zlib1.dll";

										File fpathToxmllintExe = new File( pathToxmllintExe );
										File fpathToxmllintDll1 = new File( pathToxmllintDll1 );
										File fpathToxmllintDll2 = new File( pathToxmllintDll2 );
										File fpathToxmllintDll3 = new File( pathToxmllintDll3 );
										if ( !fpathToxmllintExe.exists() ) {
											if ( min ) {
												return false;
											} else {
												getMessageService().logError(
														getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale,
																		ERROR_XML_XMLLINT1_MISSING ) );
												valid = false;
											}
										} else if ( !fpathToxmllintDll1.exists() ) {
											if ( min ) {
												return false;
											} else {
												getMessageService().logError(
														getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale,
																		ERROR_XML_XMLLINT2_MISSING ) );
												valid = false;
											}
										} else if ( !fpathToxmllintDll2.exists() ) {
											if ( min ) {
												return false;
											} else {
												getMessageService().logError(
														getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale,
																		ERROR_XML_XMLLINT3_MISSING ) );
												valid = false;
											}
										} else if ( !fpathToxmllintDll3.exists() ) {
											if ( min ) {
												return false;
											} else {
												getMessageService().logError(
														getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale,
																		ERROR_XML_XMLLINT4_MISSING ) );
												valid = false;
											}
										} else {
											StringBuffer command = new StringBuffer(
													"\"" + dirOfJarPath + File.separator + "resources" + File.separator
															+ "xmllint" + File.separator + "xmllint\" " );
											// command.append( "--noout --stream " );
											command.append( "--noout --stream --nowarning" );
											command.append( " --schema " );
											command.append( " " );
											command.append( "\"" );
											command.append( tableXsd.getAbsolutePath() );
											command.append( "\"" );
											command.append( " " );
											command.append( "\"" );
											command.append( tableXml.getAbsolutePath() );
											command.append( "\"" );

											Process proc = null;
											Runtime rt = null;
											try {
												File outTableXml = new File( pathToWorkDir + File.separator + "SIARD_H_"
														+ tableXml.getName() + ".txt" );
												Util.switchOffConsoleToTxt( outTableXml );
												rt = Runtime.getRuntime();
												proc = rt.exec( command.toString().split( " " ) );
												/* .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag
												 * vorhanden ist! */
												// Fehleroutput holen
												StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(),
														"ERROR-" + tableXml.getName() );
												// Output holen
												StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(),
														"OUTPUT-" + tableXml.getName() );
												// Threads starten
												errorGobbler.start();
												outputGobbler.start();
												// Warte, bis wget fertig ist 0 = Alles io
												int exitStatus = proc.waitFor();
												// 50ms warten bis die Konsole umgeschaltet wird, damit wirklich alles im
												// file landet
												Thread.sleep( 50 );
												Util.switchOnConsole();

												if ( 2 < exitStatus ) {
													if ( min ) {
														return false;
													} else {
														// message.xml.h.invalid.xml = <Message>{0} ist invalid zu
														// {1}</Message></Error>
														getMessageService().logError( getTextResourceService().getText( locale,
																MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale,
																		MESSAGE_XML_H_INVALID_XML, tableXml.getName(),
																		tableXsd.getName() ) );
														valid = false;

														// Fehlermeldung aus outTableXml auslesen
														BufferedReader br = new BufferedReader( new FileReader( outTableXml ) );
														Set<String> lines = new LinkedHashSet<String>( 100000 ); // evtl
																																											// vergroessern
														int counter = 0;
														try {
															String line = br.readLine();
															String linePrev = null;
															/* Fehlermeldungen holen, ausser die letzte, die besagt, dass es
															 * invalide ist (wurde bereits oben in D, F,E ausgegeben */
															while ( line != null ) {
																if ( linePrev != null ) {
																	/* Nur neue Fehlermeldungen ausgeben und diese auf maximal 10
																	 * beschraenken */
																	if ( lines.contains( linePrev ) ) {
																		// Diese Linie = Fehlermelung wurde bereits ausgegeben
																	} else {
																		// neue Fehlermeldung
																		counter = counter + 1;
																		// max 5 Meldungen pro Tabelle im Modul H
																		if ( counter < 6 ) {
																			getMessageService().logError( getTextResourceService()
																					.getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																					+ getTextResourceService()
																							.getText( MESSAGE_XML_H_INVALID_ERROR, linePrev ) );
																			lines.add( linePrev );
																		} else if ( counter == 6 ) {
																			getMessageService().logError( getTextResourceService()
																					.getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																					+ getTextResourceService().getText(
																							MESSAGE_XML_H_INVALID_ERROR, " ERROR  . . ." ) );
																			lines.add( linePrev );
																		} else {
																			// Tabellenauswertung abbrechen indem line=null
																			line = null;
																		}
																	}
																}
																linePrev = line;
																line = br.readLine();
															}
														} finally {
															br.close();
															// set to null
															br = null;
															/* Konsole zuerst einmal noch umleiten und die Streams beenden, damit
															 * die dateien geloescht werden koennen */
															Util.switchOffConsoleToTxtClose( outTableXml );
															System.out.println( " . " );
															Util.switchOnConsole();
															Util.deleteFile( outTableXml );
														}
													}
												} else {
													/* Konsole zuerst einmal noch umleiten und die Streams beenden, damit die
													 * dateien geloescht werden koennen */
													Util.switchOffConsoleToTxtClose( outTableXml );
													System.out.println( " . " );
													Util.switchOnConsole();
													Util.deleteFile( outTableXml );
												}
												/* Konsole zuerst einmal noch umleiten und die Streams beenden, damit die
												 * dateien geloescht werden koennen */
												Util.switchOffConsoleToTxtClose( outTableXml );
												System.out.println( " . " );
												Util.switchOnConsole();
												Util.deleteFile( outTableXml );

											} catch ( Exception e ) {
												getMessageService().logError(
														getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
																+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
																		e.getMessage() ) );
												return false;
											} finally {
												if ( proc != null ) {
													proc.getOutputStream().close();
													proc.getInputStream().close();
													proc.getErrorStream().close();
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
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
										ioe.getMessage() + " (IOException)" ) );
			}
		} catch ( JDOMException e ) {
			valid = false;
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
										e.getMessage() + " (JDOMException)" ) );
			}
		} catch ( SAXException e ) {
			valid = false;
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
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

	/* Verify the number of rows in the table. If the xsd minOccurs = o and maxOccurs = unbounded the
	 * validation of the numbers can't been executed. A Warning is given. */
	private boolean verifyRowCount( File xmlFile, File schemaLocation, Locale locale )
			throws SAXException, IOException
	{
		Range range = getRange( schemaLocation );
		if ( range.min == 0 && range.max == UNBOUNDED ) {
			/* die effektive Zahl in schemaLocation (Work) konnte im H nicht hereingeschrieben werden.
			 * Eine Warnung wird herausgegeben */
			if ( min ) {
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_H_SIARD )
								+ getTextResourceService().getText( locale, MESSAGE_XML_H_TABLE_NOT_VALIDATED1,
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
			reader.setFeature( "http://xml.org/sax/features/validation", false );
			reader.setFeature( "http://apache.org/xml/features/validation/schema", false );
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
		public void startElement( String uri, String localName, String qName, Attributes attributes )
				throws SAXException
		{
			if ( "row".equals( attributes.getValue( "name" ) ) ) {
				if ( "rowType".equals( attributes.getValue( "type" ) ) ) {
					this.range.min = getRange( attributes.getValue( "minOccurs" ) );
					this.range.max = getRange( attributes.getValue( "maxOccurs" ) );
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
