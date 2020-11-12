/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2019 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptCGrepException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptCGrepModule;
import ch.kostceco.tools.siardexcerpt.util.StreamGobbler;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen */
public class ExcerptCGrepModuleImpl extends ValidationModuleImpl implements ExcerptCGrepModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	@Override
	public boolean validate( File siardDatei, File outFile, String excerptString,
			Map<String, String> configMap, Locale locale ) throws ExcerptCGrepException
	{
		// Ausgabe -> Ersichtlich das SIARDexcerpt arbeitet
		int onWork = 41;

		boolean isValid = true;

		/* // Schema herausfinden File fSchema = new File( siardDatei.getAbsolutePath() + File.separator
		 * + "content" + File.separator + "schema0" ); for ( int s = 0; s < 9999999; s++ ) { fSchema =
		 * new File( siardDatei.getAbsolutePath() + File.separator + "content" + File.separator +
		 * "schema" + s ); if ( fSchema.exists() ) { break; } } */

		File fGrepExe = new File( "resources" + File.separator + "grep" + File.separator + "grep.exe" );
		String pathToGrepExe = fGrepExe.getAbsolutePath();
		if ( !fGrepExe.exists() ) {
			// grep.exe existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									fGrepExe.getAbsolutePath() ) );
			return false;
		} else {
			File fMsys10dll = new File( "resources" + File.separator + "grep" + File.separator
					+ "msys-1.0.dll" );
			if ( !fMsys10dll.exists() ) {
				// msys-1.0.dll existiert nicht --> Abbruch
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
								+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
										fMsys10dll.getAbsolutePath() ) );
				return false;
			}
		}

		File fSedExe = new File( "resources" + File.separator + "sed" + File.separator + "sed.exe" );
		File msys20dll = new File( "resources" + File.separator + "sed" + File.separator
				+ "msys-2.0.dll" );
		File msysgccs1dll = new File( "resources" + File.separator + "sed" + File.separator
				+ "msys-gcc_s-1.dll" );
		File msysiconv2dll = new File( "resources" + File.separator + "sed" + File.separator
				+ "msys-iconv-2.dll" );
		File msysintl8dll = new File( "resources" + File.separator + "sed" + File.separator
				+ "msys-intl-8.dll" );
		String pathToSedExe = fSedExe.getAbsolutePath();
		if ( !fSedExe.exists() ) {
			// sed.exe existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									fSedExe.getAbsolutePath() ) );
			return false;
		}
		if ( !msys20dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									msys20dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysgccs1dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									msysgccs1dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysiconv2dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									msysiconv2dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysintl8dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_C_MISSINGFILE,
									msysintl8dll.getAbsolutePath() ) );
			return false;
		}

		File tempOutFile = new File( outFile.getAbsolutePath() + ".tmp" );
		File xmlExtracted = new File( siardDatei.getAbsolutePath() + File.separator + "header"
				+ File.separator + "metadata.xml" );
		String content = "";

		// TODO: Record aus Maintable herausholen
		try {
			if ( tempOutFile.exists() ) {
				tempOutFile.delete();
				if ( tempOutFile.exists() ) {
					Util.replaceAllChar( tempOutFile, "" );
				}
				/* Util.deleteDir( tempOutFile );
				 * 
				 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens bei exit.
				 * wenn es nicht gelöchscht werden kann wird es geleert. */
			}

			String folder = configMap.get( "MaintableFolder" );
			String name = configMap.get( "MaintableName" );
			String cell = configMap.get( "MaintablePrimarykeyCell" );
			String schemafolder = configMap.get( "MschemaFolder" );
			String schemaname = configMap.get( "MschemaName" );
			if ( folder.startsWith( "Configuration-Error:" ) ) {
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_B ) + folder );
				return false;
			}
			if ( cell.startsWith( "Configuration-Error:" ) ) {
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_B ) + cell );
				return false;
			}
			String tabfolder = "";
			String tabname = "";
			String tabdescription = "";
			String tabdescriptionProv = "";
			String cellname = "";
			String celldescription = "";

			File fSchema = new File( siardDatei.getAbsolutePath() + File.separator + "content"
					+ File.separator + schemafolder );
			File fMaintable = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + ".xml" );
			File fMaintableTemp = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + "_Temp.xml" );
			String pathTofMaintable = fMaintable.getAbsolutePath();
			String pathTofMaintableTemp = fMaintableTemp.getAbsolutePath();
			/* mit Util.oldnewstring respektive replace können sehr grosse files nicht bearbeitet werden!
			 * 
			 * Entsprechend wurde sed verwendet. */

			String sed = configMap.get( "Sed" );
			if ( sed.equalsIgnoreCase( "yes" ) ) {
				// Bringt alles auf eine Zeile
				String commandSed = "cmd /c \"" + pathToSedExe + " 's/\\n/ /g' " + pathTofMaintable + " > "
						+ pathTofMaintableTemp + "\"";
				String commandSed2 = "cmd /c \"" + pathToSedExe + " ':a;N;$!ba;s/\\n/ /g' "
						+ pathTofMaintableTemp + " > " + pathTofMaintable + "\"";
				// Trennt ><row. Nur eine row auf einer Zeile
				String commandSed3 = "cmd /c \"" + pathToSedExe + " 's/\\d060row/\\n\\d060row/g' "
						+ pathTofMaintable + " > " + pathTofMaintableTemp + "\"";
				// Trennt ><table. <table auf eine neue Zeile
				String commandSed4 = "cmd /c \"" + pathToSedExe
						+ " 's/\\d060\\d047table/\\n\\d060\\d047table/g' " + pathTofMaintableTemp + " > "
						+ pathTofMaintable + "\"";

				// String commandSed = "cmd /c \"\"pathToSedExe\"  's/row/R0W/g\' 'hallo row.'\"";
				/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
				 * gehts: cmd /c\"urspruenlicher Befehl\" */

				Process procSed = null;
				Runtime rtSed = null;
				Process procSed2 = null;
				Runtime rtSed2 = null;
				Process procSed3 = null;
				Runtime rtSed3 = null;
				Process procSed4 = null;
				Runtime rtSed4 = null;

				try {
					Util.switchOffConsole();
					rtSed = Runtime.getRuntime();
					procSed = rtSed.exec( commandSed.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobblerSed = new StreamGobbler( procSed.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobblerSed = new StreamGobbler( procSed.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobblerSed.start();
					outputGobblerSed.start();

					// Warte, bis wget fertig ist
					procSed.waitFor();

					// ---------------------------

					rtSed2 = Runtime.getRuntime();
					procSed2 = rtSed2.exec( commandSed2.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobblerSed2 = new StreamGobbler( procSed2.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobblerSed2 = new StreamGobbler( procSed2.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobblerSed2.start();
					outputGobblerSed2.start();

					// Warte, bis wget fertig ist
					procSed2.waitFor();

					// ---------------------------

					rtSed3 = Runtime.getRuntime();
					procSed3 = rtSed3.exec( commandSed3.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobblerSed3 = new StreamGobbler( procSed3.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobblerSed3 = new StreamGobbler( procSed3.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobblerSed3.start();
					outputGobblerSed3.start();

					// Warte, bis wget fertig ist
					procSed3.waitFor();

					// ---------------------------

					rtSed4 = Runtime.getRuntime();
					procSed4 = rtSed4.exec( commandSed4.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobblerSed4 = new StreamGobbler( procSed4.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobblerSed4 = new StreamGobbler( procSed4.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobblerSed4.start();
					outputGobblerSed4.start();

					// Warte, bis wget fertig ist
					procSed4.waitFor();

					// ---------------------------

					Util.switchOnConsole();

				} catch ( Exception e ) {
					getMessageServiceExc().logError(
							getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
									+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				} finally {
					if ( procSed != null ) {
						procSed.getOutputStream().close();
						procSed.getInputStream().close();
						procSed.getErrorStream().close();
					}
					if ( procSed2 != null ) {
						procSed2.getOutputStream().close();
						procSed2.getInputStream().close();
						procSed2.getErrorStream().close();
					}
					if ( procSed3 != null ) {
						procSed3.getOutputStream().close();
						procSed3.getInputStream().close();
						procSed3.getErrorStream().close();
					}
					if ( procSed4 != null ) {
						procSed4.getOutputStream().close();
						procSed4.getInputStream().close();
						procSed4.getErrorStream().close();
					}
				}
			}

			if ( fMaintableTemp.exists() ) {
				Util.deleteDir( fMaintableTemp );
			}

			try {
				/* Der excerptString kann Leerschläge enthalten, welche bei grep Problem verursachen.
				 * Entsprechend werden diese durch . ersetzt (Wildcard) */
				String excerptStringM = excerptString.replaceAll( " ", "." );
				excerptStringM = excerptStringM.replaceAll( "\\*", "\\." );
				excerptStringM = excerptStringM.replaceAll( "\\.", "\\.*" );
				excerptStringM = "<" + cell + ">" + excerptStringM + "</" + cell + ">";
				// grep "<c11>7561234567890</c11>" table13.xml >> output.txt
				String command = "cmd /c \"\"" + pathToGrepExe + "\" -E \"" + excerptStringM + "\" \""
						+ fMaintable.getAbsolutePath() + "\" >> \"" + tempOutFile.getAbsolutePath() + "\"\"";
				/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
				 * gehts: cmd /c\"urspruenlicher Befehl\" */

				// System.out.println( command );

				Process proc = null;
				Runtime rt = null;

				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_ELEMENT_OPEN, schemaname + "_" + name ) );

				// Informationen zur Tabelle aus metadata.xml herausholen

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				// dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				org.w3c.dom.Document doc = db.parse( new FileInputStream( xmlExtracted ), "UTF8" );
				doc.getDocumentElement().normalize();

				dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

				NodeList nlTable = doc.getElementsByTagName( "table" );
				for ( int i = 0; i < nlTable.getLength(); i++ ) {
					/* cellname und celldescription leeren, da vorgängige Tabelle nicht die Richtige war. */
					cellname = "";
					celldescription = "";
					Node nodenlTable = nlTable.item( i );
					NodeList childNodes = nodenlTable.getChildNodes();
					for ( int x = 0; x < childNodes.getLength(); x++ ) {
						Node subNodeI = childNodes.item( x );
						if ( subNodeI.getNodeName().equals( "folder" ) ) {
							// System.out.println( subNodeI.getNodeName()+": "+subNodeI.getTextContent() );
							if ( subNodeI.getTextContent().equals( folder ) ) {
								/* Es ist die richtige Tabelle. Ensprechend wird i ans ende Gestellt, damit die
								 * i-Schlaufe beendet wird. */
								// System.out.println ("Richtige Tabelle");
								tabfolder = subNodeI.getTextContent();
								i = nlTable.getLength();
							}
						} else if ( subNodeI.getNodeName().equals( "name" ) ) {
							// System.out.println( subNodeI.getNodeName()+": "+subNodeI.getTextContent() );
							tabname = subNodeI.getTextContent();
						} else if ( subNodeI.getNodeName().equals( "description" ) ) {
							// System.out.println( subNodeI.getNodeName()+": "+subNodeI.getTextContent() );
							tabdescriptionProv = new String( subNodeI.getTextContent() );
							/* in der description generiert mit csv2siard wird nach "word" der Select Befehl
							 * angehängt. Dieser soll nicht mit ausgegeben werden. */
							String word = "\\u000A";
							int endIndex = tabdescriptionProv.indexOf( word );
							if ( endIndex == -1 ) {
								tabdescription = tabdescriptionProv;
							} else {
								tabdescription = tabdescriptionProv.substring( 0, endIndex );
							}
							// System.out.println( "tabdescription: "+tabdescription );
						} else if ( subNodeI.getNodeName().equals( "columns" ) ) {
							NodeList childNodesColumns = subNodeI.getChildNodes();
							int counter = 0;
							for ( int y = 0; y < childNodesColumns.getLength(); y++ ) {
								Node subNodeII = childNodesColumns.item( y );
								if ( subNodeII.getNodeName().equals( "column" ) ) {
									// nur Column mit nummer erweitern und nicht auch Kommentare
									counter = counter + 1;
									NodeList childNodesColumn = subNodeII.getChildNodes();
									for ( int z = 0; z < childNodesColumn.getLength(); z++ ) {
										int cellNumber = counter;
										// System.out.println( "Zelle Nr " + cellNumber );
										Node subNodeIII = childNodesColumn.item( z );
										if ( subNodeIII.getNodeName().equals( "name" ) ) {
											// System.out.println(
											// subNodeIII.getNodeName()+": "+subNodeIII.getTextContent()
											// );
											cellname = cellname + "<c" + cellNumber + ">" + subNodeIII.getTextContent()
													+ "</c" + cellNumber + ">";
										} else if ( subNodeIII.getNodeName().equals( "description" ) ) {
											// System.out.println(
											// subNodeIII.getNodeName()+": "+subNodeIII.getTextContent()
											// );
											celldescription = celldescription + "<c" + cellNumber + ">"
													+ new String( subNodeIII.getTextContent() ) + "</c" + cellNumber + ">";
										}
									}
								}
							}
						}
					}
				}
				// System.out.println(tabname+" "+
				// tabfolder+" "+tabdescription+" "+cellname+" "+celldescription);

				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, tabname, "tabname" ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, schemafolder + "/" + tabfolder,
								"tabfolder" ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, tabdescription, "tabdescription" ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, cellname, "name" ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, celldescription, "description" ) );

				try {
					Util.switchOffConsole();
					rt = Runtime.getRuntime();
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobbler.start();
					outputGobbler.start();

					// Warte, bis wget fertig ist
					proc.waitFor();

					Util.switchOnConsole();

				} catch ( Exception e ) {
					getMessageServiceExc().logError(
							getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
									+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				} finally {
					if ( proc != null ) {
						proc.getOutputStream().close();
						proc.getInputStream().close();
						proc.getErrorStream().close();
					}
				}

				Scanner scanner = new Scanner( tempOutFile, "UTF-8" );
				content = "";
				try {
					content = scanner.useDelimiter( "\\Z" ).next();
				} catch ( Exception e ) {
					// Grep ergab kein treffer Content Null
					content = "";
				}
				scanner.close();

				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_ELEMENT_CONTENT, content ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_ELEMENT_CLOSE, schemaname + "_" + name ) );

				if ( tempOutFile.exists() ) {
					tempOutFile.delete();
					if ( tempOutFile.exists() ) {
						Util.replaceAllChar( tempOutFile, "" );
					}
					/* Util.deleteDir( tempOutFile );
					 * 
					 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens bei exit.
					 * wenn es nicht gelöchscht werden kann wird es geleert. */
				}
				content = "";

				// Ende Grep

			} catch ( Exception e ) {
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
								+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Ende MainTable

		// TODO: grep der SubTables
		try {
			// String name = null;
			String folder = null;
			String name = null;
			String cell = null;
			String schemafolder = null;
			String schemaname = null;

			for ( int j = 1; j < 21; j++ ) {
				// name = subtable.getChild( "name", ns ).getText();*/
				folder = configMap.get( "st" + j + "Folder" );
				name = configMap.get( "st" + j + "Name" );
				cell = configMap.get( "st" + j + "Fkcell" );
				schemafolder = configMap.get( "st" + j + "SchemaFolder" );
				schemaname = configMap.get( "st" + j + "SchemaName" );
				String tabfolder = "";
				String tabkeyname = configMap.get( "st" + j + "Keyname" );
				String tabname = "";
				String tabschema = "";
				String tabdescription = "";
				String tabdescriptionProv = "";
				String cellname = "";
				String celldescription = "";

				// System.out.println( j + " - " + folder + " - " + cell+ " - " + schemafolder );
				if ( folder.equals( "(..)" ) ) {
					folder = null;
					cell = null;
					schemafolder = null;

				} else {
					File fSchema = new File( siardDatei.getAbsolutePath() + File.separator + "content"
							+ File.separator + schemafolder );
					File fSubtable = new File( fSchema.getAbsolutePath() + File.separator + folder
							+ File.separator + folder + ".xml" );
					File fSubtableTemp = new File( fSchema.getAbsolutePath() + File.separator + folder
							+ File.separator + folder + "_Temp.xml" );
					String pathTofSubtable = fSubtable.getAbsolutePath();
					String pathTofSubtableTemp = fSubtableTemp.getAbsolutePath();
					/* mit Util.oldnewstring respektive replace können sehr grosse files nicht bearbeitet
					 * werden!
					 * 
					 * Entsprechend wurde sed verwendet. */

					String sed = configMap.get( "Sed" );
					if ( sed.equalsIgnoreCase( "yes" ) ) {
						// Bringt alles auf eine Zeile
						String commandSed = "cmd /c \"" + pathToSedExe + " 's/\\n/ /g' " + pathTofSubtable
								+ " > " + pathTofSubtableTemp + "\"";
						String commandSed2 = "cmd /c \"" + pathToSedExe + " ':a;N;$!ba;s/\\n/ /g' "
								+ pathTofSubtableTemp + " > " + pathTofSubtable + "\"";
						// Trennt ><row. Nur eine row auf einer Zeile
						String commandSed3 = "cmd /c \"" + pathToSedExe + " 's/\\d060row/\\n\\d060row/g' "
								+ pathTofSubtable + " > " + pathTofSubtableTemp + "\"";
						// Trennt ><table. <table auf eine neue Zeile
						String commandSed4 = "cmd /c \"" + pathToSedExe
								+ " 's/\\d060\\d047table/\\n\\d060\\d047table/g' " + pathTofSubtableTemp + " > "
								+ pathTofSubtable + "\"";

						// String commandSed = "cmd /c \"\"pathToSedExe\"  's/row/R0W/g\' 'hallo row.'\"";
						/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten
						 * Befehl gehts: cmd /c\"urspruenlicher Befehl\" */

						Process procSed = null;
						Runtime rtSed = null;
						Process procSed2 = null;
						Runtime rtSed2 = null;
						Process procSed3 = null;
						Runtime rtSed3 = null;
						Process procSed4 = null;
						Runtime rtSed4 = null;

						try {
							Util.switchOffConsole();
							rtSed = Runtime.getRuntime();
							procSed = rtSed.exec( commandSed.toString().split( " " ) );
							// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

							// Fehleroutput holen
							StreamGobbler errorGobblerSed = new StreamGobbler( procSed.getErrorStream(), "ERROR" );

							// Output holen
							StreamGobbler outputGobblerSed = new StreamGobbler( procSed.getInputStream(),
									"OUTPUT" );

							// Threads starten
							errorGobblerSed.start();
							outputGobblerSed.start();

							// Warte, bis wget fertig ist
							procSed.waitFor();

							// ---------------------------

							rtSed2 = Runtime.getRuntime();
							procSed2 = rtSed2.exec( commandSed2.toString().split( " " ) );
							// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

							// Fehleroutput holen
							StreamGobbler errorGobblerSed2 = new StreamGobbler( procSed2.getErrorStream(),
									"ERROR" );

							// Output holen
							StreamGobbler outputGobblerSed2 = new StreamGobbler( procSed2.getInputStream(),
									"OUTPUT" );

							// Threads starten
							errorGobblerSed2.start();
							outputGobblerSed2.start();

							// Warte, bis wget fertig ist
							procSed2.waitFor();

							// ---------------------------

							rtSed3 = Runtime.getRuntime();
							procSed3 = rtSed3.exec( commandSed3.toString().split( " " ) );
							// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

							// Fehleroutput holen
							StreamGobbler errorGobblerSed3 = new StreamGobbler( procSed3.getErrorStream(),
									"ERROR" );

							// Output holen
							StreamGobbler outputGobblerSed3 = new StreamGobbler( procSed3.getInputStream(),
									"OUTPUT" );

							// Threads starten
							errorGobblerSed3.start();
							outputGobblerSed3.start();

							// Warte, bis wget fertig ist
							procSed3.waitFor();

							// ---------------------------

							rtSed4 = Runtime.getRuntime();
							procSed4 = rtSed4.exec( commandSed4.toString().split( " " ) );
							// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

							// Fehleroutput holen
							StreamGobbler errorGobblerSed4 = new StreamGobbler( procSed4.getErrorStream(),
									"ERROR" );

							// Output holen
							StreamGobbler outputGobblerSed4 = new StreamGobbler( procSed4.getInputStream(),
									"OUTPUT" );

							// Threads starten
							errorGobblerSed4.start();
							outputGobblerSed4.start();

							// Warte, bis wget fertig ist
							procSed4.waitFor();

							// ---------------------------

							Util.switchOnConsole();

						} catch ( Exception e ) {
							getMessageServiceExc().logError(
									getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
											+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
							return false;
						} finally {
							if ( procSed != null ) {
								procSed.getOutputStream().close();
								procSed.getInputStream().close();
								procSed.getErrorStream().close();
							}
							if ( procSed2 != null ) {
								procSed2.getOutputStream().close();
								procSed2.getInputStream().close();
								procSed2.getErrorStream().close();
							}
							if ( procSed3 != null ) {
								procSed3.getOutputStream().close();
								procSed3.getInputStream().close();
								procSed3.getErrorStream().close();
							}
							if ( procSed4 != null ) {
								procSed4.getOutputStream().close();
								procSed4.getInputStream().close();
								procSed4.getErrorStream().close();
							}
						}
					}

					if ( fSubtableTemp.exists() ) {
						Util.deleteDir( fSubtableTemp );
					}

					try {
						/* Der excerptString kann Leerschläge enthalten, welche bei grep Problem verursachen.
						 * Entsprechend werden diese durch . ersetzt (Wildcard) */
						String excerptStringM = excerptString.replaceAll( " ", "." );
						excerptStringM = excerptStringM.replaceAll( "\\.", "\\.*" );
						excerptStringM = "<" + cell + ">" + excerptStringM + "</" + cell + ">";
						// grep "<c11>7561234567890</c11>" table13.xml >> output.txt
						String command = "cmd /c \"\"" + pathToGrepExe + "\" -E \"" + excerptStringM + "\" \""
								+ fSubtable.getAbsolutePath() + "\" >> \"" + tempOutFile.getAbsolutePath() + "\"\"";
						/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten
						 * Befehl gehts: cmd /c\"urspruenlicher Befehl\" */

						// System.out.println( command );

						Process proc = null;
						Runtime rt = null;

						getMessageServiceExc().logError(
								getTextResourceServiceExc()
										.getText( locale,MESSAGE_XML_ELEMENT_OPEN, schemaname + "_" + name ) );
						// TODO Start Wie maintable
						// Informationen zur Tabelle aus metadata.xml herausholen

						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						// dbf.setValidating(false);
						DocumentBuilder db = dbf.newDocumentBuilder();
						org.w3c.dom.Document doc = db.parse( new FileInputStream( xmlExtracted ), "UTF8" );
						doc.getDocumentElement().normalize();

						dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

						NodeList nlTable = doc.getElementsByTagName( "table" );
						for ( int i = 0; i < nlTable.getLength(); i++ ) {
							// für jede Tabelle (table) ...
							tabfolder = "";
							tabname = "";
							tabschema = "";
							tabdescription = "";
							tabdescriptionProv = "";
							cellname = "";
							celldescription = "";

							Node nodenlTable = nlTable.item( i );

							// Schema name und folder herauslesen
							Node mainTables = nodenlTable.getParentNode();
							Node mainSchema = mainTables.getParentNode();
							NodeList nlSchemaChild = mainSchema.getChildNodes();
							for ( int x1 = 0; x1 < nlSchemaChild.getLength(); x1++ ) {
								// für jedes Subelement der Tabelle (name, folder, description...) ...
								Node subNode = nlSchemaChild.item( x1 );
								if ( subNode.getNodeName().equals( "folder" ) ) {
									tabschema = subNode.getTextContent();
									if ( subNode.getTextContent().equals( schemafolder ) ) {
										tabschema = subNode.getTextContent();
										// das richtige schema
										NodeList childNodes = nodenlTable.getChildNodes();
										for ( int x = 0; x < childNodes.getLength(); x++ ) {
											Node subNodeI = childNodes.item( x );
											if ( subNodeI.getNodeName().equals( "folder" ) ) {
												// System.out.println( subNodeI.getTextContent() );
												if ( subNodeI.getTextContent().equals( folder ) ) {
													/* Es ist die richtige Tabelle. Ensprechend wird i ans ende Gestellt,
													 * damit die i-Schlaufe beendet wird. */
													tabfolder = subNodeI.getTextContent();
													i = nlTable.getLength();
												}
											} else if ( subNodeI.getNodeName().equals( "name" ) ) {
												tabname = subNodeI.getTextContent();
											} else if ( subNodeI.getNodeName().equals( "description" ) ) {
												tabdescriptionProv = new String( subNodeI.getTextContent() );
												/* in der description generiert mit csv2siard wird nach "word" der Select
												 * Befehl angehängt. Dieser soll nicht mit ausgegeben werden. */
												String word = "\\u000A";
												int endIndex = tabdescriptionProv.indexOf( word );
												if ( endIndex == -1 ) {
													tabdescription = tabdescriptionProv;
												} else {
													tabdescription = tabdescriptionProv.substring( 0, endIndex );
												}
											} else if ( subNodeI.getNodeName().equals( "columns" ) ) {
												NodeList childNodesColumns = subNodeI.getChildNodes();
												int counter = 0;
												for ( int y = 0; y < childNodesColumns.getLength(); y++ ) {
													Node subNodeII = childNodesColumns.item( y );
													if ( subNodeII.getNodeName().equals( "column" ) ) {
														// nur Column mit nummer erweitern und nicht auch Kommentare
														counter = counter + 1;
														NodeList childNodesColumn = subNodeII.getChildNodes();
														for ( int z = 0; z < childNodesColumn.getLength(); z++ ) {
															int cellNumber = counter;
															// System.out.println( "Zelle Nr " + cellNumber );
															Node subNodeIII = childNodesColumn.item( z );
															if ( subNodeIII.getNodeName().equals( "name" ) ) {
																cellname = cellname + "<c" + cellNumber + ">"
																		+ subNodeIII.getTextContent() + "</c" + cellNumber + ">";
																// System.out.println( cellname );
															} else if ( subNodeIII.getNodeName().equals( "description" ) ) {
																celldescription = celldescription + "<c" + cellNumber + ">"
																		+ new String( subNodeIII.getTextContent() ) + "</c"
																		+ cellNumber + ">";
															}
														}
													}
												}
											}
										}
									}
								}
							}
							if ( i == nlTable.getLength() ) {
								// Ausgabe für jede Tabelle
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, tabname, "tabname" ) );
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT,
												(tabschema + "/" + tabfolder), "tabfolder" ) );
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, tabkeyname, "tabkeyname" ) );
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, tabdescription,
												"tabdescription" ) );
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, cellname, "name" ) );
								getMessageServiceExc().logError(
										getTextResourceServiceExc().getText( locale,MESSAGE_XML_TEXT, celldescription,
												"description" ) );
							}
						}
						// TODO End Wie maintable

						try {
							Util.switchOffConsole();
							rt = Runtime.getRuntime();
							proc = rt.exec( command.toString().split( " " ) );
							// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

							// Fehleroutput holen
							StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

							// Output holen
							StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

							// Threads starten
							errorGobbler.start();
							outputGobbler.start();

							// Warte, bis wget fertig ist
							proc.waitFor();

							Util.switchOnConsole();

						} catch ( Exception e ) {
							getMessageServiceExc().logError(
									getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
											+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
							return false;
						} finally {
							if ( proc != null ) {
								proc.getOutputStream().close();
								proc.getInputStream().close();
								proc.getErrorStream().close();
							}
						}

						Scanner scanner = new Scanner( tempOutFile, "UTF-8" );
						content = "";
						try {
							content = scanner.useDelimiter( "\\Z" ).next();
						} catch ( Exception e ) {
							// Grep ergab kein treffer Content Null
							content = "";
						}
						scanner.close();

						getMessageServiceExc().logError(
								getTextResourceServiceExc().getText( locale,MESSAGE_XML_ELEMENT_CONTENT, content ) );
						getMessageServiceExc().logError(
								getTextResourceServiceExc().getText( locale,MESSAGE_XML_ELEMENT_CLOSE,
										schemaname + "_" + name ) );

						if ( tempOutFile.exists() ) {
							tempOutFile.delete();
							if ( tempOutFile.exists() ) {
								Util.replaceAllChar( tempOutFile, "" );
							}
							/* Util.deleteDir( tempOutFile );
							 * 
							 * wird nicht verwendet, da es jetzt gelöscht werden muss und nicht spätestens bei
							 * exit. wenn es nicht gelöchscht werden kann wird es geleert. */
						}
						content = "";

						// Ende Grep

					} catch ( Exception e ) {
						getMessageServiceExc().logError(
								getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
										+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
						return false;
					}

					// Ende SubTables
					if ( onWork == 41 ) {
						onWork = 3;
						System.out.print( "-   " );
						System.out.print( "\r" );
					} else if ( onWork == 11 ) {
						onWork = 13;
						System.out.print( "\\   " );
						System.out.print( "\r" );
					} else if ( onWork == 21 ) {
						onWork = 23;
						System.out.print( "|   " );
						System.out.print( "\r" );
					} else if ( onWork == 31 ) {
						onWork = 33;
						System.out.print( "/   " );
						System.out.print( "\r" );
					} else {
						onWork = onWork + 2;
					}
				}
			}
			System.out.print( "   " );
			System.out.print( "\r" );
		} catch ( Exception e ) {
			getMessageServiceExc().logError(
					getTextResourceServiceExc().getText( locale,MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale,ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return isValid;
	}
}
