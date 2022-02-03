/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2022 Claire Roethlisberger (KOST-CECO)
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
import ch.kostceco.tools.siardexcerpt.logging.Logtxt;
import ch.kostceco.tools.siardexcerpt.SIARDexcerpt;
import ch.kostceco.tools.kosttools.fileservice.Sed;
import ch.kostceco.tools.kosttools.util.StreamGobbler;
import ch.kostceco.tools.kosttools.util.Util;

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
					.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_C_MISSINGFILE,
									fGrepExe.getAbsolutePath() ) );
			return false;
		} else {
			File fMsys10dll = new File(
					"resources" + File.separator + "grep" + File.separator + "msys-1.0.dll" );
			if ( !fMsys10dll.exists() ) {
				// msys-1.0.dll existiert nicht --> Abbruch
				getMessageServiceExc()
						.logError( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
								+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_C_MISSINGFILE,
										fMsys10dll.getAbsolutePath() ) );
				return false;
			}
		}

		/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
		 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
		 * 
		 * dirOfJarPath + File.separator +
		 * 
		 * erweitern. */
		String path = new File(
				SIARDexcerpt.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
						.getAbsolutePath();
		String locationOfJarPath = path;
		String dirOfJarPath = locationOfJarPath;
		if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
				|| locationOfJarPath.endsWith( "." ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Sed.checkSed( dirOfJarPath );
		if ( !checkTool.equals( "OK" ) ) {
			// mindestens eine Datei fehlt --> Abbruch
			Logtxt.logtxt( outFileSearch,
					getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_C_MISSINGFILE,
									checkTool ) );
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
				Logtxt.logtxt( outFileSearch,
						getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + folder );
				return false;
			}
			String insensitiveOption = "";
			String insensitive = configMap.get( "Insensitive" );
			if ( insensitive.startsWith( "Configuration-Error:" ) ) {
				Logtxt.logtxt( outFileSearch,
						getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + insensitive );
				return false;
			} else if ( insensitive.equalsIgnoreCase( "yes" ) ) {
				insensitiveOption = "i";
			}
			File fSchema = new File( siardDatei.getAbsolutePath() + File.separator + "content"
					+ File.separator + folderSchema );
			File fSearchtable = new File(
					fSchema.getAbsolutePath() + File.separator + folder + File.separator + folder + ".xml" );
			File fSearchtableTemp1 = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + "_Temp1.xml" );
			File fSearchtableTemp2 = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + "_Temp2.xml" );
			File fSearchtableTemp3 = new File( fSchema.getAbsolutePath() + File.separator + folder
					+ File.separator + folder + "_Temp3.xml" );

			// System.out.println("pathTofSearchtable: "+fSearchtable.getAbsolutePath());
			/* mit Util.oldnewstring respektive replace koennen sehr grosse files nicht bearbeitet werden!
			 * 
			 * Entsprechend wurde sed verwendet. */

			String sed = configMap.get( "Sed" );
			if ( sed.equalsIgnoreCase( "yes" ) ) {

				// Bringt alles auf eine Zeile
				String sed1 = "-e 's/\\n/ /g' ";
				String sed2 = "-e ':a;N;$!ba;s/\\n/ /g' ";
				// Trennt ><row. Nur eine row auf einer Zeile
				String sed3 = "-e 's/\\d060row/\\n\\d060row/g' ";
				// Trennt ><table. <table auf eine neue Zeile
				String sed4 = "-e 's/\\d060\\d047table/\\n\\d060\\d047table/g' ";

				/* // Bringt alles auf eine Zeile String options = sed1 + sed2 + sed3 + sed4;
				 * 
				 * String pathToWorkDir = configMap.get( "PathToWorkDir" ); File workDir = new File(
				 * pathToWorkDir );
				 * 
				 * // Sed-Befehl: pathToSedExe options fSearchtable > fSearchtableTemp String resultExec =
				 * Sed.execSed( options, fSearchtable, fSearchtableTemp, workDir, dirOfJarPath ); if (
				 * !resultExec.equals( "OK" ) ) { // Exception oder Report existiert nicht Logtxt.logtxt(
				 * outFileSearch, getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_C ) +
				 * getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, " (execSed)" ) ); }
				 * Util.copyFile( fSearchtableTemp, fSearchtable ); // Ende Sed direkt auszuloesen */

				String pathToWorkDir = configMap.get( "PathToWorkDir" );
				File workDir = new File( pathToWorkDir );
				long sleepLong = fSearchtable.length() / 100000;
				// System.out.println( "wait: " + sleepLong );

				// Sed-Befehl: pathToSedExe options fSearchtable > fSearchtableTemp
				String resultExec = Sed.execSed( sed1, fSearchtable, fSearchtableTemp1, workDir,
						dirOfJarPath );
				Thread.sleep( sleepLong );
				if ( !fSearchtableTemp1.exists() ) {
					// System.out.println( "wait 1" );
					Thread.sleep( 10000 );
				}
				if ( fSearchtableTemp1.length() == 0 ) {
					// System.out.println( "wait 1 0" );
					resultExec = Sed.execSed( sed1, fSearchtable, fSearchtableTemp1, workDir, dirOfJarPath );
					Thread.sleep( sleepLong * 5 );
				}
				if ( resultExec.equals( "OK" ) ) {
					resultExec = Sed.execSed( sed2, fSearchtableTemp1, fSearchtableTemp2, workDir,
							dirOfJarPath );
					Thread.sleep( sleepLong );
					if ( !fSearchtableTemp2.exists() ) {
						// System.out.println( "2" );
						Thread.sleep( 10000 );
					}
					if ( fSearchtableTemp2.length() == 0 ) {
						// System.out.println( "wait 2 0" );
						resultExec = Sed.execSed( sed2, fSearchtableTemp1, fSearchtableTemp2, workDir,
								dirOfJarPath );
						Thread.sleep( sleepLong * 5 );
					}
					if ( resultExec.equals( "OK" ) ) {
						resultExec = Sed.execSed( sed3, fSearchtableTemp2, fSearchtableTemp3, workDir,
								dirOfJarPath );
						Thread.sleep( sleepLong );
						if ( !fSearchtableTemp3.exists() ) {
							// System.out.println( "wait 3" );
							Thread.sleep( 10000 );
						}
						if ( fSearchtableTemp3.length() == 0 ) {
							// System.out.println( "wait 3 0" );
							resultExec = Sed.execSed( sed3, fSearchtableTemp2, fSearchtableTemp3, workDir,
									dirOfJarPath );
							Thread.sleep( sleepLong * 5 );
						}
						if ( resultExec.equals( "OK" ) ) {
							resultExec = Sed.execSed( sed4, fSearchtableTemp3, fSearchtable, workDir,
									dirOfJarPath );
							Thread.sleep( sleepLong );
							if ( fSearchtable.length() == 0 ) {
								// System.out.println( "wait 0" );
								resultExec = Sed.execSed( sed4, fSearchtableTemp3, fSearchtable, workDir,
										dirOfJarPath );
								Thread.sleep( sleepLong * 5 );
							}
						}
					}
				}
				if ( !resultExec.equals( "OK" ) || fSearchtable.length() == 0 ) {
					// sed hat nicht funktioniert, ueberspringen
				}
			}
			if ( fSearchtableTemp1.exists() ) {
				Util.deleteDir( fSearchtableTemp1 );
			}
			if ( fSearchtableTemp2.exists() ) {
				Util.deleteDir( fSearchtableTemp2 );
			}
			if ( fSearchtableTemp3.exists() ) {
				Util.deleteDir( fSearchtableTemp3 );
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

				Logtxt.logtxt( outFileSearch,
						getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_ELEMENT_OPEN, name ) );
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
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
									+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN,
											e.getMessage() ) );
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

				// liest das tempOutFile (UTF-8) ein
				Scanner scanner = new Scanner( tempOutFile, "UTF-8"  );
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
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr0 );
					return false;
				}
				if ( nr1.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr1 );
					return false;
				}
				if ( nr2.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr2 );
					return false;
				}
				if ( nr3.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr3 );
					return false;
				}
				if ( nr4.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr4 );
					return false;
				}
				if ( nr5.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr5 );
					return false;
				}
				if ( nr6.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr6 );
					return false;
				}
				if ( nr7.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr7 );
					return false;
				}
				if ( nr8.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr8 );
					return false;
				}
				if ( nr9.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr9 );
					return false;
				}
				if ( nr10.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr10 );
					return false;
				}
				if ( nr11.startsWith( "Configuration-Error:" ) ) {
					Logtxt.logtxt( outFileSearch,
							getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B ) + nr11 );
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
						// die restlichen <cNr> koennen geloescht werden
						content = content.replaceAll( "<c1" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c2" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c3" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c4" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c5" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c6" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c7" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c8" + ".*" + "</row>", "</row>" );
						content = content.replaceAll( "<c9" + ".*" + "</row>", "</row>" );
						break;
					}
				}

				if ( time ) {
					nowTime = new java.util.Date();
					stringNowTime = sdfStartS.format( nowTime );
					System.out.println( stringNowTime + " Ende der Bereinigung" );
				}

				Logtxt.logtxt( outFileSearch, getTextResourceServiceExc().getText( locale,
						EXC_MESSAGE_XML_ELEMENT_CONTENT, content ) );
				Logtxt.logtxt( outFileSearch,
						getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_ELEMENT_CLOSE, name ) );

				if ( tempOutFile.exists() ) {
					Util.deleteDir( tempOutFile );
				}
				contentAll = "";
				content = "";

				// Ende Grep

			} catch ( Exception e ) {
				Logtxt.logtxt( outFileSearch,
						getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
								+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN,
										e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			Logtxt.logtxt( outFileSearch,
					getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_B )
							+ getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN,
									e.getMessage() ) );
			return false;
		}

		return isValid;
	}
}
