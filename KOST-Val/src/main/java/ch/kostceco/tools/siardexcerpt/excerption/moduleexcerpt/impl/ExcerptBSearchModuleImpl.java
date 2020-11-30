/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2020 Claire Roethlisberger (KOST-CECO)
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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptBSearchException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptBSearchModule;
import ch.kostceco.tools.siardexcerpt.util.StreamGobbler;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** 2) search: gemäss config die Tabelle mit Suchtext befragen und Ausgabe des Resultates */
public class ExcerptBSearchModuleImpl extends ValidationModuleImpl implements ExcerptBSearchModule
{

	public static String NEWLINE = System.getProperty( "line.separator" );

	@Override
	public boolean validate( File siardDatei, File outFileSearch, String searchString,
			Map<String, String> configMap, Locale locale ) throws ExcerptBSearchException
	{
		boolean isValid = true;
		boolean time = false;
		// Zeitstempel für Performance tests wird nur ausgegeben wenn time = true
		java.util.Date nowTime = new java.util.Date();
		java.text.SimpleDateFormat sdfStartS = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
		String stringNowTime = sdfStartS.format( nowTime );

		if ( time ) {
			nowTime = new java.util.Date();
			stringNowTime = sdfStartS.format( nowTime );
			System.out.println( stringNowTime + " Start der Suche" );
		}

		File fGrepExe = new File( "resources" + File.separator + "grep" + File.separator + "grep.exe" );
		String pathToGrepExe = fGrepExe.getAbsolutePath();
		if ( !fGrepExe.exists() ) {
			// grep.exe existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									fGrepExe.getAbsolutePath() ) );
			return false;
		} else {
			File fMsys10dll = new File(
					"resources" + File.separator + "grep" + File.separator + "msys-1.0.dll" );
			if ( !fMsys10dll.exists() ) {
				// msys-1.0.dll existiert nicht --> Abbruch
				getMessageServiceExc()
						.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
								+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
										fMsys10dll.getAbsolutePath() ) );
				return false;
			}
		}

		File fSedExe = new File( "resources" + File.separator + "sed" + File.separator + "sed.exe" );
		File msys20dll = new File(
				"resources" + File.separator + "sed" + File.separator + "msys-2.0.dll" );
		File msysgccs1dll = new File(
				"resources" + File.separator + "sed" + File.separator + "msys-gcc_s-1.dll" );
		File msysiconv2dll = new File(
				"resources" + File.separator + "sed" + File.separator + "msys-iconv-2.dll" );
		File msysintl8dll = new File(
				"resources" + File.separator + "sed" + File.separator + "msys-intl-8.dll" );
		String pathToSedExe = fSedExe.getAbsolutePath();
		if ( !fSedExe.exists() ) {
			// sed.exe existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									fSedExe.getAbsolutePath() ) );
			return false;
		}
		if ( !msys20dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									msys20dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysgccs1dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									msysgccs1dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysiconv2dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									msysiconv2dll.getAbsolutePath() ) );
			return false;
		}
		if ( !msysintl8dll.exists() ) {
			// existiert nicht --> Abbruch
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_C_MISSINGFILE,
									msysintl8dll.getAbsolutePath() ) );
			return false;
		}

		File tempOutFile = new File( outFileSearch.getAbsolutePath() + ".tmp" );
		String content = "";
		String contentAll = "";

		// Records aus table herausholen
		try {
			if ( tempOutFile.exists() ) {
				Util.deleteDir( tempOutFile );
			}

			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */

			// String name = getConfigurationService().getSearchtableName();
			String name = "siardexcerptsearch";
			String folder = configMap.get( "MaintableFolder" );
			String folderSchema = configMap.get( "MschemaFolder" );
			if ( folder.startsWith( "Configuration-Error:" ) ) {
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + folder );
				return false;
			}
			String insensitiveOption = "";
			String insensitive = configMap.get( "Insensitive" );
			if ( insensitive.startsWith( "Configuration-Error:" ) ) {
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + insensitive );
				return false;
			} else if ( insensitive.equalsIgnoreCase( "yes" ) ) {
				insensitiveOption = "i";
			}
			File fSchema = new File( siardDatei.getAbsolutePath() + File.separator + "content"
					+ File.separator + folderSchema );
			File fSearchtable = new File(
					fSchema.getAbsolutePath() + File.separator + folder + File.separator + folder + ".xml" );
			File fSearchtableTemp = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + "_Temp.xml" );
			String pathTofSearchtable = fSearchtable.getAbsolutePath();
			String pathTofSearchtableTemp = fSearchtableTemp.getAbsolutePath();

			// System.out.println("pathTofSearchtable: "+pathTofSearchtable);
			/* mit Util.oldnewstring respektive replace koennen sehr grosse files nicht bearbeitet werden!
			 * 
			 * Entsprechend wurde sed verwendet. */

			String sed = configMap.get( "Sed" );
			if ( sed.equalsIgnoreCase( "yes" ) ) {
				// Bringt alles auf eine Zeile
				String commandSed = "cmd /c \"" + pathToSedExe + " 's/\\n/ /g' " + pathTofSearchtable
						+ " > " + pathTofSearchtableTemp + "\"";
				String commandSed2 = "cmd /c \"" + pathToSedExe + " ':a;N;$!ba;s/\\n/ /g' "
						+ pathTofSearchtableTemp + " > " + pathTofSearchtable + "\"";
				// Trennt ><row. Nur eine row auf einer Zeile
				String commandSed3 = "cmd /c \"" + pathToSedExe + " 's/\\d060row/\\n\\d060row/g' "
						+ pathTofSearchtable + " > " + pathTofSearchtableTemp + "\"";
				// Trennt ><table. <table auf eine neue Zeile
				String commandSed4 = "cmd /c \"" + pathToSedExe
						+ " 's/\\d060\\d047table/\\n\\d060\\d047table/g' " + pathTofSearchtableTemp + " > "
						+ pathTofSearchtable + "\"";

				// String commandSed = "cmd /c \"\"pathToSedExe\" 's/row/R0W/g\' 'hallo row.'\"";
				/* Das redirect Zeichen verunmoeglicht eine direkte eingabe. mit dem geschachtellten Befehl
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
					StreamGobbler errorGobblerSed3 = new StreamGobbler( procSed3.getErrorStream(), "ERROR" );

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
					StreamGobbler errorGobblerSed4 = new StreamGobbler( procSed4.getErrorStream(), "ERROR" );

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
					getMessageServiceExc().logError( getTextResourceServiceExc().getText( locale,
							MESSAGE_XML_MODUL_C )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
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

			if ( fSearchtableTemp.exists() ) {
				Util.deleteDir( fSearchtableTemp );
			}

			/* Der SearchString soll nur über <row>...</row> durchgeführt werden
			 * 
			 * einmal mit row erweitern */
			searchString = "row." + searchString;

			/* Der SearchString kann Leerschläge enthalten, welche bei grep Problem verursachen.
			 * Entsprechend werden diese durch . ersetzt (Wildcard) */
			searchString = searchString.replaceAll( " ", "." );
			searchString = searchString.replaceAll( "\\*", "\\." );
			searchString = searchString.replaceAll( "\\.", "\\.*" );

			try {
				// grep -E "REGEX-Suchbegriff" table13.xml >> output.txt
				String command = "cmd /c \"\"" + pathToGrepExe + "\" -E" + insensitiveOption + " \""
						+ searchString + "\" \"" + fSearchtable.getAbsolutePath() + "\" >> \""
						+ tempOutFile.getAbsolutePath() + "\"\"";
				/* Das redirect Zeichen verunmoeglicht eine direkte eingabe. mit dem geschachtellten Befehl
				 * gehts: cmd /c\"urspruenlicher Befehl\" */

				// System.out.println( command );

				Process proc = null;
				Runtime rt = null;

				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale, MESSAGE_XML_ELEMENT_OPEN, name ) );
				if ( time ) {
					nowTime = new java.util.Date();
					stringNowTime = sdfStartS.format( nowTime );
					System.out.println( stringNowTime + " Ende der Initialisierung" );
				}

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
					if ( time ) {
						nowTime = new java.util.Date();
						stringNowTime = sdfStartS.format( nowTime );
						System.out.println( stringNowTime + " Ende der Ausführung von GREP" );
					}

				} catch ( Exception e ) {
					getMessageServiceExc().logError( getTextResourceServiceExc().getText( locale,
							MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
					isValid = false;
				} finally {
					if ( proc != null ) {
						proc.getOutputStream().close();
						proc.getInputStream().close();
						proc.getErrorStream().close();
					}
				}

				if ( time ) {
					nowTime = new java.util.Date();
					stringNowTime = sdfStartS.format( nowTime );
					System.out.println( stringNowTime + " Start der Bereinigung" );
				}

				Scanner scanner = new Scanner( tempOutFile, "UTF-8" );
				contentAll = "";
				content = "";
				contentAll = scanner.useDelimiter( "\\Z" ).next();
				scanner.close();
				content = contentAll;
				/* im contentAll ist jetzt der Gesamtstring, dieser soll anschliessend nur noch aus den 12
				 * ResultateZellen bestehen -> content */
				String nr0 = configMap.get( "MaintablePrimarykeyCell" );
				String nr1 = configMap.get( "CellNumber1" );
				String nr2 = configMap.get( "CellNumber2" );
				String nr3 = configMap.get( "CellNumber3" );
				String nr4 = configMap.get( "CellNumber4" );
				String nr5 = configMap.get( "CellNumber5" );
				String nr6 = configMap.get( "CellNumber6" );
				String nr7 = configMap.get( "CellNumber7" );
				String nr8 = configMap.get( "CellNumber8" );
				String nr9 = configMap.get( "CellNumber9" );
				String nr10 = configMap.get( "CellNumber10" );
				String nr11 = configMap.get( "CellNumber11" );
				if ( nr0.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr0 );
					return false;
				}
				if ( nr1.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr1 );
					return false;
				}
				if ( nr2.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr2 );
					return false;
				}
				if ( nr3.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr3 );
					return false;
				}
				if ( nr4.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr4 );
					return false;
				}
				if ( nr5.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr5 );
					return false;
				}
				if ( nr6.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr6 );
					return false;
				}
				if ( nr7.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr7 );
					return false;
				}
				if ( nr8.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr8 );
					return false;
				}
				if ( nr9.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc()
							.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr9 );
					return false;
				}
				if ( nr10.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc().logError(
							getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr10 );
					return false;
				}
				if ( nr11.startsWith( "Configuration-Error:" ) ) {
					getMessageServiceExc().logError(
							getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) + nr11 );
					return false;
				}

				File xmlExtracted = new File( siardDatei.getAbsolutePath() + File.separator + "header"
						+ File.separator + "metadata.xml" );
				DocumentBuilderFactory dbfConfig = DocumentBuilderFactory.newInstance();
				DocumentBuilder dbConfig = dbfConfig.newDocumentBuilder();
				Document docConfig = dbConfig.parse( new FileInputStream( xmlExtracted ), "UTF8" );
				docConfig.getDocumentElement().normalize();
				dbfConfig.setFeature( "http://xml.org/sax/features/namespaces", false );

				NodeList nlColumn = docConfig.getElementsByTagName( "column" );
				int counterColumn = nlColumn.getLength();
				/* counterColumn ist zwar zu hoch aber betreffend der performance am besten. durch break
				 * wird zudem abgebrochen, sobald alle behalten wurden. */
				String cellLoop = "";
				String modifString = "";
				// Loop von 1, 2, 3 ... bis counterColumn.
				boolean col0 = false, col1 = false, col2 = false, col3 = false, col4 = false, col5 = false;
				boolean col6 = false, col7 = false, col8 = false, col9 = false, col10 = false,
						col11 = false;
				for ( int i = 1; i < counterColumn; i++ ) {
					cellLoop = "";
					cellLoop = "c" + i;
					if ( cellLoop.equals( nr0 ) || cellLoop.equals( nr1 ) || cellLoop.equals( nr2 )
							|| cellLoop.equals( nr3 ) || cellLoop.equals( nr4 ) || cellLoop.equals( nr5 )
							|| cellLoop.equals( nr6 ) || cellLoop.equals( nr7 ) || cellLoop.equals( nr8 )
							|| cellLoop.equals( nr9 ) || cellLoop.equals( nr10 ) || cellLoop.equals( nr11 ) ) {
						// wird behalten
						modifString = "c" + i + ">";

						if ( cellLoop.equals( nr0 ) ) {
							content = content.replaceAll( modifString, "col0>" );
							col0 = true;
						} else {
							if ( cellLoop.equals( nr1 ) ) {
								content = content.replaceAll( modifString, "col1>" );
								col1 = true;
							} else {
								if ( cellLoop.equals( nr2 ) ) {
									content = content.replaceAll( modifString, "col2>" );
									col2 = true;
								} else {
									if ( cellLoop.equals( nr3 ) ) {
										content = content.replaceAll( modifString, "col3>" );
										col3 = true;
									} else {
										if ( cellLoop.equals( nr4 ) ) {
											content = content.replaceAll( modifString, "col4>" );
											col4 = true;
										} else {
											if ( cellLoop.equals( nr5 ) ) {
												content = content.replaceAll( modifString, "col5>" );
												col5 = true;
											} else {
												if ( cellLoop.equals( nr6 ) ) {
													content = content.replaceAll( modifString, "col6>" );
													col6 = true;
												} else {
													if ( cellLoop.equals( nr7 ) ) {
														content = content.replaceAll( modifString, "col7>" );
														col7 = true;
													} else {
														if ( cellLoop.equals( nr8 ) ) {
															content = content.replaceAll( modifString, "col8>" );
															col8 = true;
														} else {
															if ( cellLoop.equals( nr9 ) ) {
																content = content.replaceAll( modifString, "col9>" );
																col9 = true;
															} else {
																if ( cellLoop.equals( nr10 ) ) {
																	content = content.replaceAll( modifString, "col10>" );
																	col10 = true;
																} else {
																	if ( cellLoop.equals( nr11 ) ) {
																		content = content.replaceAll( modifString, "col11>" );
																		col11 = true;
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					} else {
						String deletString = "<c" + i + ">" + ".*" + "</c" + i + ">";
						content = content.replaceAll( deletString, "" );
					}
					if ( col0 && col1 && col2 && col3 && col4 && col5 && col6 && col7 && col8 && col9 && col10
							&& col11 ) {
						int j = i + 1;
						String deletString = "<c" + j + ">" + ".*" + "</row>";
						content = content.replaceAll( deletString, "</row>" );
						break;
					}
				}

				if ( time ) {
					nowTime = new java.util.Date();
					stringNowTime = sdfStartS.format( nowTime );
					System.out.println( stringNowTime + " Ende der Bereinigung" );
				}

				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale, MESSAGE_XML_ELEMENT_CONTENT, content ) );
				getMessageServiceExc().logError(
						getTextResourceServiceExc().getText( locale, MESSAGE_XML_ELEMENT_CLOSE, name ) );

				if ( tempOutFile.exists() ) {
					Util.deleteDir( tempOutFile );
				}
				contentAll = "";
				content = "";

				// Ende Grep

			} catch ( Exception e ) {
				getMessageServiceExc().logError( getTextResourceServiceExc().getText( locale,
						MESSAGE_XML_MODUL_B )
						+ getTextResourceServiceExc().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			getMessageServiceExc()
					.logError( getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return isValid;
	}
}
