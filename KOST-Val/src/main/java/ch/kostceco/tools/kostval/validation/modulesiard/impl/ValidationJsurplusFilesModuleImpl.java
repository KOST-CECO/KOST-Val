/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2017 Claire Roethlisberger (KOST-CECO), Christian
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationJsurplusFilesException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationJsurplusFilesModule;

/** Validierungsschritt J (zusätzliche Primärdateien) Enthält der content-Ordner Dateien oder Ordner
 * die nicht in metadata.xml oder in table[Zahl].xml beschrieben sind ? invalid --> zusätzliche
 * Ordner oder Dateien im content-Ordner
 * 
 * @author Ec Christian Eugster
 * @author Rc Claire Roethlisberger, KOST-CECO */

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

	Map<String, String>	filesInSiard		= new HashMap<String, String>();
	Map<String, String>	tablesInSiard		= new HashMap<String, String>();
	Map<String, String>	tablesToRemove	= new HashMap<String, String>();
	Map<String, String>	filesToRemove		= new HashMap<String, String>();

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationJsurplusFilesException
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = getConfigurationService().getShowProgressOnWork();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "J    " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean valid = true;
		try {
			String pathToWorkDir = getConfigurationService().getPathToWorkDir();
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			File content = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
					.append( "content" ).toString() );
			HashMap<String, File> hashMap = new HashMap<String, File>();
			Map<String, File> fileMap = Util.getContent( content, hashMap );
			Set<String> fileMapKeys = fileMap.keySet();
			// alle Dateien in SIARD in die Map filesInSiard schreiben (Inhalt)
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				String entryName = iterator.next();
				// entryName: content/schema1/table7/table7.xsd
				filesInSiard.put( entryName, entryName );
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "J-   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "J\\   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "J|   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "J/   " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}

			try {
				// Struktur aus metadata.xml herauslesen (path)
				pathToWorkDir = getConfigurationService().getPathToWorkDir();
				pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
				File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
						.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );
				InputStream fin = new FileInputStream( metadataXml );
				SAXBuilder builder = new SAXBuilder();
				Document document = (Document) builder.build( fin );
				fin.close();

				Boolean version1 = FileUtils.readFileToString( metadataXml ).contains(
						"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
				Boolean version2 = FileUtils.readFileToString( metadataXml ).contains(
						"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
				Namespace ns = Namespace
						.getNamespace( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
				if ( version1 ) {
					// ns = Namespace.getNamespace( "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
				} else {
					if ( version2 ) {
						ns = Namespace.getNamespace( "http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
					} else {
						valid = false;
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
										+ getTextResourceService().getText( MESSAGE_XML_D_INVALID_XMLNS, metadataXml ) );
					}
				}
				// select schema elements and loop
				// TODO: schema2 aus Beispiel unterstützen
				List<Element> schemas = ((org.jdom2.Document) document).getRootElement()
						.getChild( "schemas", ns ).getChildren( "schema", ns );
				for ( Element schema : schemas ) {
					valid = validateSchema( schema, ns, pathToWorkDir );
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "J-   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "J\\   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "J|   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "J/   " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} catch ( java.io.IOException ioe ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
										ioe.getMessage() + " (IOException)" ) );
			} catch ( JDOMException e ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
										e.getMessage() + " (JDOMException)" ) );
			}

			if ( filesInSiard.size() > 0 ) {
				try {
					// in filesInSiard sind jetzt noch die drinnen, welche nicht in metadata.xml erwaehnt
					// wurden

					Set<String> filesInSiardKeys = filesInSiard.keySet();
					for ( Iterator<String> iterator = filesInSiardKeys.iterator(); iterator.hasNext(); ) {
						String entryName = iterator.next();

						Set<String> tablesInSiardKeys = tablesInSiard.keySet();
						for ( Iterator<String> iteratorTables = tablesInSiardKeys.iterator(); iteratorTables
								.hasNext(); ) {
							String tableName = iteratorTables.next();
							File tableXml = new File( tableName );
							boolean tableFile = false;
							try {
								Scanner scanner = new Scanner( tableXml );

								// Datei Zeile für Zeile lesen und ermitteln ob "file=" darin enthalten ist
								tableFile = false;
								while ( scanner.hasNextLine() ) {
									String line = scanner.nextLine();
									if ( line.contains( " file=" ) ) {
										tableFile = true;
										String newEntryName = entryName.substring( entryName.indexOf( "content" ),
												entryName.length() );
										if ( line.contains( newEntryName ) ) {
											// entryName ist in der Tabelle enthalten und wird spaeter aus liste geloescht
											filesToRemove.put( entryName, entryName );
											break;
										} else {
											newEntryName = newEntryName.replace( "\\", "/" );
											if ( line.contains( newEntryName ) ) {
												// entryName ist in der Tabelle enthalten und wird spaeter aus liste
												// geloescht
												filesToRemove.put( entryName, entryName );
												break;
											} else {
												// entryName wurde nicht in dieser Zeile gefunden
												// keine Aktion
											}
										}
									}
								}
								scanner.close();
							} catch ( FileNotFoundException e ) {
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
														"FileNotFoundException" ) );
							} catch ( Exception e ) {
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
												+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
														(e.getMessage() + " 1") ) ); //
								return false;
							} // table durch scannen
							if ( !tableFile ) {
								// tableName enthaelt keine file und wird aus liste geloescht
								tablesToRemove.put( tableName, tableName );
							}
						} // tablesInSiard durchgehen
						// tablesInSiard mit tablesToRemove bereinigen
						Set<String> tablesToRemoveKeys = tablesToRemove.keySet();
						for ( Iterator<String> iteratorRemove = tablesToRemoveKeys.iterator(); iteratorRemove
								.hasNext(); ) {
							String tableName = iteratorRemove.next();
							tablesInSiard.remove( tableName );
						}
					} // filesInSiard durchgehen und mit tablesInSiard ausduennen
					// filesInSiard mit filesToRemove bereinigen
					Set<String> filesToRemoveKeys = filesToRemove.keySet();
					for ( Iterator<String> iteratorRemove = filesToRemoveKeys.iterator(); iteratorRemove
							.hasNext(); ) {
						String fileName = iteratorRemove.next();
						filesInSiard.remove( fileName );
					}
				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, (e.getMessage() + " 2") ) );
					// return false;
				}
			}

			// in filesInSiard sind jetzt noch die drinnen, welche nicht in table.xml erwaehnt wurden
			if ( filesInSiard.size() > 0 ) {
				valid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_J_INVALID_ENTRY,
										filesInSiard.keySet() ) );
			} else {
				valid = true;
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_J_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, (e.getMessage() + " 3") ) );
			// return false;
		}

		return valid;

	}

	private boolean validateSchema( Element schema, Namespace ns, String pathToWorkDir )
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = getConfigurationService().getShowProgressOnWork();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWorkConfig.equals( "no" ) ) {
			// keine Ausgabe
			showOnWork = false;
		} else {
			if ( onWork == 410 ) {
				onWork = 2;
				System.out.print( "J-   " );
				System.out.print( "\b\b\b\b\b" );
			} else if ( onWork == 110 ) {
				onWork = onWork + 1;
				System.out.print( "J\\   " );
				System.out.print( "\b\b\b\b\b" );
			} else if ( onWork == 210 ) {
				onWork = onWork + 1;
				System.out.print( "J|   " );
				System.out.print( "\b\b\b\b\b" );
			} else if ( onWork == 310 ) {
				onWork = onWork + 1;
				System.out.print( "J/   " );
				System.out.print( "\b\b\b\b\b" );
			} else {
				onWork = onWork + 1;
			}
		}
		boolean valid = true;
		Element schemaFolder = schema.getChild( "folder", ns );
		File schemaPath = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
				.append( "content" ).append( File.separator ).append( schemaFolder.getText() ).toString() );
		String schemaPathString = schemaPath.toString();
		filesInSiard.remove( schemaPathString );

		if ( schemaPath.isDirectory() ) {
			if ( schema.getChild( "tables", ns ) != null ) {

				List<Element> tables = schema.getChild( "tables", ns ).getChildren( "table", ns );
				for ( Element table : tables ) {
					String name = "";
					Element tableFolder = table.getChild( "folder", ns );
					File tablePath = new File( new StringBuilder( schemaPath.getPath() )
							.append( File.separator ).append( tableFolder.getText() ).toString() );
					name = tablePath.toString();
					filesInSiard.remove( name );
					// die Datei "name" aus filesInSiard entfernen

					if ( tablePath.isDirectory() ) {
						File tableXsd = new File( new StringBuilder( tablePath.getPath() )
								.append( File.separator ).append( tableFolder.getText() + ".xsd" ).toString() );
						name = tableXsd.toString();
						// die Datei "name" aus filesInSiard entfernen
						filesInSiard.remove( name );

						File tableXml = new File( new StringBuilder( tablePath.getPath() )
								.append( File.separator ).append( tableFolder.getText() + ".xml" ).toString() );
						name = tableXml.toString();
						// die Datei "name" aus filesInSiard entfernen
						filesInSiard.remove( name );
						// die Datei "name" in die Liste aller table.xml eintragen
						tablesInSiard.put( name, name );

					}
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "J-   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "J\\   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "J|   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "J/   " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} else {
				// Kein Fehler sondern leeres Schema
			}
		}
		return valid;
	}
}
