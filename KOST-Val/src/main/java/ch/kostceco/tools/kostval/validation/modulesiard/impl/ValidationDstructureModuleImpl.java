/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
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
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationDstructureException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationDstructureModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt D (Struktur-Validierung) Stimmt die Struktur aus
 * metadata.xml mit der Datei-Struktur von content ueberein? valid -->
 * schema0/table3 in metadata.xml == schema0/table3/tabe3.xsd und table3.xml in
 * content ==> Bei den Module A, B, C und D wird die Validierung abgebrochen,
 * sollte das Resulat invalid sein!
 * 
 * @author Christian Eugster
 */

public class ValidationDstructureModuleImpl extends ValidationModuleImpl
		implements ValidationDstructureModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationDstructureException
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "D    " );
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
			 * existence in temporary extracted structure
			 */
			Boolean version1 = FileUtils
					.readFileToString( metadataXml, "ISO-8859-1" ).contains(
							"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			Boolean version2 = FileUtils
					.readFileToString( metadataXml, "ISO-8859-1" ).contains(
							"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			Namespace ns = Namespace.getNamespace(
					"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			if ( version1 ) {
				// ns = Namespace.getNamespace(
				// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			} else {
				if ( version2 ) {
					ns = Namespace.getNamespace(
							"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
				} else {
					valid = false;
					if ( min ) {
						return false;
					} else {

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_D_SIARD )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_D_INVALID_XMLNS,
										metadataXml ) );
					}
				}
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
						System.out.print( "D-   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "D\\   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "D|   " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "D/   " );
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
								MESSAGE_XML_MODUL_D_SIARD )
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
								MESSAGE_XML_MODUL_D_SIARD )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN,
										e.getMessage() + " (JDOMException)" ) );
			}
		}

		return valid;
	}

	private boolean validateSchema( Element schema, Namespace ns,
			String pathToWorkDir, Map<String, String> configMap, Locale locale,
			File logFile )
	{
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "D    " );
			System.out.print( "\b\b\b\b\b" );
		} else if ( onWorkConfig.equals( "nomin" ) ) {
			min = true;
			// keine Ausgabe
			showOnWork = false;
		} else {
			// keine Ausgabe
			showOnWork = false;
		}

		boolean valid = true;
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
					valid = valid && validateTable( table, ns, pathToWorkDir,
							schemaPath, locale, min, logFile );
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "D-   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "D\\   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "D|   " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "D/   " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} else {
				// Kein Fehler sondern leeres Schema
			}
		} else {
			valid = false;
			if ( min ) {
				return false;
			} else {
				if ( schemaPath.exists() ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_D_SIARD )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_D_INVALID_FOLDER,
											"content", schemaPath.getName() ) );
				} else {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_D_SIARD )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_D_MISSING_FOLDER,
											"content", schemaPath.getName() ) );
				}
			}
		}
		return valid;
	}

	private boolean validateTable( Element table, Namespace ns,
			String pathToWorkDir, File schemaPath, Locale locale, Boolean min,
			File logFile )
	{
		boolean valid = true;
		Element tableFolder = table.getChild( "folder", ns );
		File tablePath = new File(
				new StringBuilder( schemaPath.getAbsolutePath() )
						.append( File.separator )
						.append( tableFolder.getText() ).toString() );
		if ( tablePath.isDirectory() ) {
			File tableXml = new File(
					new StringBuilder( tablePath.getAbsolutePath() )
							.append( File.separator )
							.append( tableFolder.getText() + ".xml" )
							.toString() );
			valid = valid && validateFile( tableXml, tablePath, locale, min,
					logFile );
			File tableXsd = new File(
					new StringBuilder( tablePath.getAbsolutePath() )
							.append( File.separator )
							.append( tableFolder.getText() + ".xsd" )
							.toString() );
			valid = valid && validateFile( tableXsd, tablePath, locale, min,
					logFile );
		} else {
			valid = false;
			if ( min ) {
				return false;
			} else {
				if ( tablePath.exists() ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_D_SIARD )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_D_INVALID_FOLDER,
											schemaPath.getName(),
											tablePath.getName() ) );
				} else {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_D_SIARD )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_D_MISSING_FOLDER,
											schemaPath.getName(),
											tablePath.getName() ) );
				}
			}
		}
		return valid;
	}

	private boolean validateFile( File file, File parent, Locale locale,
			Boolean min, File logFile )
	{
		boolean valid = true;
		if ( !file.isFile() ) {
			valid = false;
			if ( min ) {
				return false;
			} else {
				if ( file.exists() ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_D_SIARD )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_D_INVALID_FILE,
									parent.getName(), file.getName() ) );
				} else {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_D_SIARD )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_D_MISSING_FILE,
									parent.getName(), file.getName() ) );
				}
			}
		}
		return valid;
	}
}
