/*== KOST-Val ==================================================================================
The KOST-Val v1.2.7 application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.kostceco.tools.kostval.controller.Controllersip;
import ch.kostceco.tools.kostval.controller.Controllertiff;
import ch.kostceco.tools.kostval.controller.Controllersiard;
import ch.kostceco.tools.kostval.controller.Controllerpdfa;
import ch.kostceco.tools.kostval.logging.LogConfigurator;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.util.Zip64Archiver;

/**
 * Dies ist die Starter-Klasse, verantwortlich für das Initialisieren des
 * Controllers, des Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public class KOSTVal implements MessageConstants
{

	private static final Logger		LOGGER	= new Logger( KOSTVal.class );

	private TextResourceService		textResourceService;
	private ConfigurationService	configurationService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	/**
	 * Die Eingabe besteht aus 2 oder 3 Parameter: [0] Validierungstyp [1] Pfad
	 * zur Val-File [2] option: Verbose
	 * 
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public static void main( String[] args ) throws IOException
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		// Zeitstempel Start
		java.util.Date nowStart = new java.util.Date();
		java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat(
				"dd.MM.yyyy HH.mm.ss" );
		String ausgabeStart = sdfStart.format( nowStart );

		// TODO: siehe Bemerkung im applicationContext-services.xml bezüglich
		// Injection in der Superklasse aller Impl-Klassen
		// ValidationModuleImpl validationModuleImpl = (ValidationModuleImpl)
		// context.getBean("validationmoduleimpl");

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );

		// Ueberprüfung des Parameters (Log-Verzeichnis)
		String pathToLogfile = kostval.getConfigurationService()
				.getPathToLogfile();

		File directoryOfLogfile = new File( pathToLogfile );

		if ( !directoryOfLogfile.exists() ) {
			directoryOfLogfile.mkdir();
		}

		// Im Logverzeichnis besteht kein Schreibrecht
		if ( !directoryOfLogfile.canWrite() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfLogfile ) );
			System.exit( 1 );
		}

		if ( !directoryOfLogfile.isDirectory() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_LOGDIRECTORY_NODIRECTORY ) );
			System.exit( 1 );
		}

		File valDatei = new File( args[1] );
		File logDatei = null;
		logDatei = valDatei;

		// Konfiguration des Loggings, ein File Logger wird
		// zusätzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context
				.getBean( "logconfigurator" );
		String logFileName = logConfigurator.configure(
				directoryOfLogfile.getAbsolutePath(), logDatei.getName() );
		File logFile = new File( logFileName );
		LOGGER.logInfo( kostval.getTextResourceService().getText(
				MESSAGE_XML_HEADER ) );
		LOGGER.logError( kostval.getTextResourceService().getText(
				MESSAGE_XML_START, ausgabeStart ) );
		LOGGER.logError( kostval.getTextResourceService().getText(
				MESSAGE_XML_END ) );
		LOGGER.logInfo( kostval.getTextResourceService().getText(
				MESSAGE_XML_INFO ) );

		// Ist die Anzahl Parameter (mind. 2) korrekt?
		if ( args.length < 2 ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE,
					kostval.getTextResourceService().getText(
							ERROR_PARAMETER_USAGE ) ) );
			System.exit( 1 );
		}

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = kostval.getConfigurationService()
				.getPathToWorkDir();
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidation = kostval.getConfigurationService()
				.pdfaValidation();
		String siardValidation = kostval.getConfigurationService()
				.siardValidation();
		String tiffValidation = kostval.getConfigurationService()
				.tiffValidation();

		File tmpDir = new File( pathToWorkDir );

		// bestehendes Workverzeichnis Abbruch, da am Schluss das
		// Workverzeichnis gelöscht wird und entsprechend bestehende Dateien
		// gelöscht werden können
		if ( tmpDir.exists() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE,
					kostval.getTextResourceService().getText(
							ERROR_WORKDIRECTORY_EXISTS, pathToWorkDir ) ) );
			System.exit( 1 );
		}

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logInfo( kostval.getTextResourceService()
					.getText(
							ERROR_IOE,
							kostval.getTextResourceService().getText(
									ERROR_WRONG_JRE ) ) );
			System.exit( 1 );
		}

		// bestehendes Workverzeichnis wieder anlegen
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		// Im workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE,
					kostval.getTextResourceService().getText(
							ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) ) );
			System.exit( 1 );
		}

		// Ueberprüfung des optionalen Parameters (2 -v --> im Verbose-mode
		// werden die originalen Logs nicht gelöscht (PDFTron, Jhove & Co.)
		boolean verbose = false;
		if ( args.length > 2 ) {
			if ( !(args[2].equals( "-v" )) ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						ERROR_IOE,
						kostval.getTextResourceService().getText(
								ERROR_PARAMETER_OPTIONAL_1 ) ) );
				System.exit( 1 );
			} else {
				verbose = true;
			}
		}

		// Initialisierung TIFF-Modul B (JHove-Validierung)
		// überprüfen der Konfiguration: existiert die jhove.conf am
		// angebenen Ort?
		String jhoveConf = kostval.getConfigurationService()
				.getPathToJhoveConfiguration();
		File fJhoveConf = new File( jhoveConf );
		if ( !fJhoveConf.exists()
				|| !fJhoveConf.getName().equals( "jhove.conf" ) ) {

			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE, kostval.getTextResourceService().getText(ERROR_JHOVECONF_MISSING ) ));
			System.exit( 1 );
		}

		// Ueberprüfung des Parameters (Val-Datei): existiert die Datei?
		if ( !valDatei.exists() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE, kostval.getTextResourceService().getText(ERROR_VALFILE_FILENOTEXISTING )) );
			System.exit( 1 );
		}

		if ( args[0].equals( "--format" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_FORMAT1 ) );
			Integer countNio = 0;
			Integer countSummaryNio = 0;
			Integer count = 0;
			Integer pdfaCountIo = 0;
			Integer pdfaCountNio = 0;
			Integer siardCountIo = 0;
			Integer siardCountNio = 0;
			Integer tiffCountIo = 0;
			Integer tiffCountNio = 0;

			// TODO: Formatvalidierung an einer Datei
			// --> erledigt --> nur Marker
			if ( !valDatei.isDirectory() ) {

				boolean valFile = valFile( valDatei, logFileName,
						directoryOfLogfile, verbose );

				LOGGER.logError( kostval.getTextResourceService().getText(
						MESSAGE_XML_FORMAT2 ) );

				// Löschen des Arbeitsverzeichnisses, falls eines
				// angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}

				LOGGER.logError( kostval.getTextResourceService().getText(
						MESSAGE_XML_LOGEND ) );
				// Zeitstempel End
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
						"dd.MM.yyyy HH.mm.ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );
				ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
				Util.valEnd( ausgabeEnd, logFile );

				if ( valFile ) {
					// Löschen des Arbeitsverzeichnisses, falls eines
					// angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// Validierte Datei valide
					System.exit( 0 );
				} else {
					// Löschen des Arbeitsverzeichnisses, falls eines
					// angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// Fehler in Validierte Datei --> invalide
					System.exit( 2 );

				}
			} else {
				// TODO: Formatvalidierung über ein Ordner
				// --> erledigt --> nur Marker
				Map<String, File> fileMap = Util.getFileMap( valDatei, false );
				Set<String> fileMapKeys = fileMap.keySet();
				for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
						.hasNext(); ) {
					String entryName = iterator.next();
					File newFile = fileMap.get( entryName );
					if ( !newFile.isDirectory() ) {
						valDatei = newFile;
						count = count + 1;

						if ( ((valDatei.getAbsolutePath().toLowerCase()
								.endsWith( ".tiff" ) || valDatei
								.getAbsolutePath().toLowerCase()
								.endsWith( ".tif" )))
								&& tiffValidation.equals( "yes" ) ) {

							boolean valFile = valFile( valDatei, logFileName,
									directoryOfLogfile, verbose );

							// Löschen des Arbeitsverzeichnisses, falls eines
							// angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
							if ( valFile ) {
								tiffCountIo = tiffCountIo + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							} else {
								tiffCountNio = tiffCountNio + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							}
						} else if ( (valDatei.getAbsolutePath().toLowerCase()
								.endsWith( ".siard" ))
								&& siardValidation.equals( "yes" ) ) {

							boolean valFile = valFile( valDatei, logFileName,
									directoryOfLogfile, verbose );

							// Löschen des Arbeitsverzeichnisses, falls eines
							// angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
							if ( valFile ) {
								siardCountIo = siardCountIo + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							} else {
								siardCountNio = siardCountNio + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							}

						} else if ( ((valDatei.getName().endsWith( ".pdf" ) || valDatei
								.getName().endsWith( ".pdfa" )))
								&& pdfaValidation.equals( "yes" ) ) {

							boolean valFile = valFile( valDatei, logFileName,
									directoryOfLogfile, verbose );

							// Löschen des Arbeitsverzeichnisses, falls eines
							// angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
							if ( valFile ) {
								pdfaCountIo = pdfaCountIo + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							} else {
								pdfaCountNio = pdfaCountNio + 1;
								// Löschen des Arbeitsverzeichnisses, falls
								// eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
							}

						} else {
							countNio = countNio + 1;
						}
					}
				}

				LOGGER.logError( kostval.getTextResourceService().getText(
						MESSAGE_XML_FORMAT2 ) );

				LOGGER.logError( kostval.getTextResourceService().getText(
						MESSAGE_XML_LOGEND ) );
				// Zeitstempel End
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
						"dd.MM.yyyy HH.mm.ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );
				ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
				Util.valEnd( ausgabeEnd, logFile );

				if ( countNio == count ) {
					// keine Dateien Validiert
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					System.exit( 1 );
				} else if ( countSummaryNio == 0 ) {
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// alle Validierten Dateien valide
					System.exit( 0 );
				} else {
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// Fehler in Validierten Dateien --> invalide
					System.exit( 2 );
				}
			}
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_FORMAT2 ) );

		} else if ( args[0].equals( "--sip" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_FORMAT1 ) );

			// TODO: Sipvalidierung
			// --> erledigt --> nur Marker
			boolean validFormat = false;
			File originalSipFile = valDatei;
			File outputFile3c = null;
			String fileName3c = null;
			File tmpDirZip = null;

			// zuerst eine Formatvalidierung über den Content
			// dies ist analog aufgebaut wie --format
			Integer countNio = 0;
			Integer countSummaryNio = 0;
			Integer countSummaryIo = 0;
			Integer count = 0;
			Integer pdfaCountIo = 0;
			Integer pdfaCountNio = 0;
			Integer siardCountIo = 0;
			Integer siardCountNio = 0;
			Integer tiffCountIo = 0;
			Integer tiffCountNio = 0;

			if ( !valDatei.isDirectory() ) {
				Boolean zip = false;
				// Eine ZIP Datei muss mit PK.. beginnen
				if ( (valDatei.getAbsolutePath().toLowerCase()
						.endsWith( ".zip" ) || valDatei.getAbsolutePath()
						.toLowerCase().endsWith( ".zip64" )) ) {

					FileReader fr = null;

					try {
						fr = new FileReader( valDatei );
						BufferedReader read = new BufferedReader( fr );

						// Hex 03 in Char umwandeln
						String str3 = "03";
						int i3 = Integer.parseInt( str3, 16 );
						char c3 = (char) i3;
						// Hex 04 in Char umwandeln
						String str4 = "04";
						int i4 = Integer.parseInt( str4, 16 );
						char c4 = (char) i4;

						// auslesen der ersten 4 Zeichen der Datei
						int length;
						int i;
						char[] buffer = new char[4];
						length = read.read( buffer );
						for ( i = 0; i != length; i++ )
							;

						// die beiden charArrays (soll und ist) mit einander
						// vergleichen
						char[] charArray1 = buffer;
						char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

						if ( Arrays.equals( charArray1, charArray2 ) ) {
							// höchstwahrscheinlich ein ZIP da es mit
							// 504B0304 respektive
							// PK.. beginnt
							zip = true;
						}
					} catch ( Exception e ) {
						LOGGER.logInfo( e.getMessage() );
					}
				}

				// wenn die Datei kein Directory ist, muss sie mit zip oder
				// zip64 enden
				if ( (!(valDatei.getAbsolutePath().toLowerCase()
						.endsWith( ".zip" ) || valDatei.getAbsolutePath()
						.toLowerCase().endsWith( ".zip64" )))
						|| zip == false ) {

					LOGGER.logInfo( kostval.getTextResourceService().getText(
							ERROR_XML_AA_INCORRECTFILEENDING ) );

				} else {
					// geziptes SIP --> in temp dir entzipen
					tmpDirZip = new File( tmpDir.getAbsolutePath() + "\\ZIP" );
					try {
						Zip64Archiver.unzip( valDatei.getAbsolutePath(),
								tmpDirZip.getAbsolutePath() );
					} catch ( Exception e ) {
						try {
							Zip64Archiver.unzip64( valDatei, tmpDirZip );
						} catch ( Exception e1 ) {
							LOGGER.logInfo( kostval.getTextResourceService()
									.getText( ERROR_XML_AA_CANNOTEXTRACTZIP ) );
						}
					}
					valDatei = tmpDirZip;
				}
			} else {
				// SIP ist ein Ordner
				// Für 1d header in tempDir\ZIP\SIP-name\header kopieren
				File headerOrig = new File( valDatei.getAbsolutePath()
						+ "\\header" );
				File zipCopy = new File( pathToWorkDir + "\\ZIP" );
				zipCopy.mkdir();
				File valCopy = new File( pathToWorkDir + "\\ZIP\\"
						+ valDatei.getName() );
				valCopy.mkdir();
				File headerCopy = new File( pathToWorkDir + "\\ZIP\\"
						+ valDatei.getName() + "\\header" );
				headerCopy.mkdir();
				if ( !headerCopy.exists() ) {
					System.out.println( "Ordneranlegen fehlgeschlagen" );
				}
				File metadataXml = new File( headerCopy.getAbsolutePath()
						+ "\\metadata.xml" );
				try {
					Util.copyDir( headerOrig, headerCopy );
					if ( !metadataXml.exists() ) {
						System.out
								.println( "Metadata.xml anlegen fehlgeschlagen" );
					}

				} catch ( FileNotFoundException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch ( IOException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Map<String, File> fileMap = Util.getFileMap( valDatei, false );
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
					.hasNext(); ) {
				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );

				if ( !newFile.isDirectory()
						&& newFile.getAbsolutePath().contains( "\\content\\" ) ) {
					valDatei = newFile;
					count = count + 1;

					if ( ((valDatei.getAbsolutePath().toLowerCase()
							.endsWith( ".tiff" ) || valDatei.getAbsolutePath()
							.toLowerCase().endsWith( ".tif" )))
							&& tiffValidation.equals( "yes" ) ) {

						boolean valFile = valFile( valDatei, logFileName,
								directoryOfLogfile, verbose );

						if ( valFile ) {
							tiffCountIo = tiffCountIo + 1;
						} else {
							tiffCountNio = tiffCountNio + 1;
						}

					} else if ( (valDatei.getAbsolutePath().toLowerCase()
							.endsWith( ".siard" ))
							&& siardValidation.equals( "yes" ) ) {

						boolean valFile = valFile( valDatei, logFileName,
								directoryOfLogfile, verbose );

						if ( valFile ) {
							siardCountIo = siardCountIo + 1;
						} else {
							siardCountNio = siardCountNio + 1;
						}

					} else if ( ((valDatei.getName().endsWith( ".pdf" ) || valDatei
							.getName().endsWith( ".pdfa" )))
							&& pdfaValidation.equals( "yes" ) ) {

						boolean valFile = valFile( valDatei, logFileName,
								directoryOfLogfile, verbose );

						if ( valFile ) {
							pdfaCountIo = pdfaCountIo + 1;
						} else {
							pdfaCountNio = pdfaCountNio + 1;
						}

					} else {
						countNio = countNio + 1;
					}
				}
			}

			countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio;
			countSummaryIo = pdfaCountIo + siardCountIo + tiffCountIo;
			String summary3c = kostval.getTextResourceService().getText(
					MESSAGE_XML_SUMMARY_3C, count, countSummaryIo,
					countSummaryNio, countNio );

			if ( countSummaryNio == 0 ) {
				// alle Validierten Dateien valide
				validFormat = true;
				fileName3c = "3c_Valide.txt";
			} else {
				// Fehler in Validierten Dateien --> invalide
				validFormat = false;
				fileName3c = "3c_Invalide.txt";
			}
			// outputFile3c = new File( directoryOfLogfile + fileName3c );
			outputFile3c = new File( pathToWorkDir + "\\" + fileName3c );
			try {
				outputFile3c.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_FORMAT2 ) );

			// Start Normale SIP-Validierung mit auswertung Format-Val. im 3c

			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_SIP1 ) );
			valDatei = originalSipFile;
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText(
							MESSAGE_SIPVALIDATION ) ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALFILE, valDatei.getAbsolutePath() ) );
			File targetFile = new File( pathToWorkDir + "\\SIP-Validierung",
					valDatei.getName() + ".zip" );
			File tmpDirSip = new File( pathToWorkDir + "\\SIP-Validierung" );
			tmpDirSip.mkdir();

			// Ueberprüfung des 1. Parameters (SIP-Datei): ist die Datei ein
			// Verzeichnis? Wenn ja, wird im work-Verzeichnis eine Zip-Datei
			// daraus erstellt, damit die weiteren Validierungen Gebrauch von
			// der java.util.zip API machen können, und somit die zu
			// Validierenden Archive gleichartig behandelt werden können, egal
			// ob es sich um eine Verzeichnisstruktur oder ein Zip-File handelt.
			// Informationen zum Arbeitsverzeichnis holen

			String originalSipName = valDatei.getAbsolutePath();
			if ( valDatei.isDirectory() ) {
				try {
					Zip64Archiver.archivate( valDatei, targetFile );
					valDatei = targetFile;

				} catch ( Exception e ) {
					LOGGER.logInfo( kostval.getTextResourceService().getText(
							ERROR_CANNOTCREATEZIP ) );
					System.exit( 1 );
				}

			}
			Controllersip controller = (Controllersip) context
					.getBean( "controllersip" );
			boolean okMandatory = false;
			okMandatory = controller.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierungen 1a - 1d sind obligatorisch, wenn sie bestanden
			// wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der Applikation führen,
			// ausgeführt werden.
			if ( okMandatory ) {
				ok = controller.executeOptional( valDatei, directoryOfLogfile );
			}
			// Formatvalidierung validFormat
			ok = (ok && okMandatory && validFormat);
			
			if ( ok ) {
				// Validiertes SIP valide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ) );
			} else {
				// Fehler im Validierten SIP --> invalide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ) );
			}
			
			// ggf. Fehlermeldung 3c ergänzen
//			Util.val3c(summary3c, logFile );

			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_SIP2 ) );

			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_LOGEND ) );
			// Zeitstempel End
			java.util.Date nowEnd = new java.util.Date();
			java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
					"dd.MM.yyyy HH.mm.ss" );
			String ausgabeEnd = sdfEnd.format( nowEnd );
			ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
			Util.valEnd( ausgabeEnd, logFile );
			Util.val3c(summary3c, logFile );

			// bestehendes Workverzeichnis ggf. löschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			StringBuffer command = new StringBuffer( "rd "
					+ tmpDir.getAbsolutePath() + " /s /q" );
			if ( ok ) {
				if ( tmpDir.exists() ) {

					try {
						Runtime rt = Runtime.getRuntime();
						Process proc = rt.exec( command.toString() );
					} catch ( IOException e ) {
						System.out.println( "" );
						System.out.println( tmpDir.getAbsolutePath() );
						System.out
								.println( "... konnte nicht geloescht werden. Bitte manuell loeschen." );
						System.out
								.println( "... n`a pas pu etre supprime. S`il vous plait supprimer manuellement." );
						System.out.println( "" );
					}
				}
				System.exit( 0 );
			} else {
				if ( tmpDir.exists() ) {
					try {
						Runtime rt = Runtime.getRuntime();
						Process proc = rt.exec( command.toString() );
					} catch ( IOException e ) {
						System.out.println( "" );
						System.out.println( tmpDir.getAbsolutePath() );
						System.out
								.println( "... konnte nicht geloescht werden. Bitte manuell loeschen." );
						System.out
								.println( "... n`a pas pu etre supprime. S`il vous plait supprimer manuellement." );
						System.out.println( "" );
					}
				}
				System.exit( 2 );
			}
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_SIP2 ) );

		} else {
			// Ueberprüfung des Parameters (Val-Typ): format / sip
			// args[0] ist nicht "--format" oder "--sip" --> INVALIDE
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_IOE,kostval.getTextResourceService().getText(
					ERROR_PARAMETER_USAGE )) );
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
				tmpDir.deleteOnExit();
			}
			System.exit( 1 );
		}
	}

	// TODO: ValFile --> Formatvalidierung einer Datei
	// --> erledigt --> nur Marker
	private static boolean valFile( File valDatei, String logFileName,
			File directoryOfLogfile, boolean verbose ) throws IOException
	{
		File logFile = new File( logFileName );
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );
		String originalValName = valDatei.getAbsolutePath();
		boolean valFile = false;

		if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".tiff" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".tif" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText(
							MESSAGE_TIFFVALIDATION ) ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALFILE, originalValName ) );
			Controllertiff controller1 = (Controllertiff) context
					.getBean( "controllertiff" );
			boolean okMandatory = controller1.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierungen A sind obligatorisch, wenn sie
			// bestanden wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der
			// Applikation führen, ausgeführt werden.
			if ( okMandatory ) {
				ok = controller1.executeOptional( valDatei, directoryOfLogfile );
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ) );
			} else {
				// Fehler in Validierte Datei --> invalide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ) );
			}

			// Ausgabe der Pfade zu den Jhove Reports, falls welche
			// generiert wurden (-v) oder Jhove Report löschen
			File jhoveReport = new File( directoryOfLogfile, valDatei.getName()
					+ ".jhove-log.txt" );

			if ( jhoveReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> Jhove-Report lassen
				} else {
					// kein optionaler Parameter --> Jhove-Report loeschen!
					jhoveReport.delete();
				}
			}

		} else if ( (valDatei.getAbsolutePath().toLowerCase()
				.endsWith( ".siard" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText(
							MESSAGE_SIARDVALIDATION ) ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALFILE, originalValName ) );
			Controllersiard controller2 = (Controllersiard) context
					.getBean( "controllersiard" );
			boolean okMandatory = controller2.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierungen A-D sind obligatorisch, wenn sie
			// bestanden wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der
			// Applikation führen, ausgeführt werden.
			if ( okMandatory ) {
				ok = controller2.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der optionalen Schritte
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ) );
			} else {
				// Fehler in Validierte Datei --> invalide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ) );
			}

		} else if ( (valDatei.getName().endsWith( ".pdf" ) || valDatei
				.getName().endsWith( ".pdfa" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText(
							MESSAGE_PDFAVALIDATION ) ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_XML_VALFILE, originalValName ) );
			Controllerpdfa controller3 = (Controllerpdfa) context
					.getBean( "controllerpdfa" );
			boolean okMandatory = controller3.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierung A ist obligatorisch, wenn sie
			// bestanden wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der
			// Applikation führen, ausgeführt werden.
			if ( okMandatory ) {
				ok = controller3.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der optionalen Schritte
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_VALID ) );
			} else {
				// Validierte Datei invalide
				Util.valElement(
						kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ), logFile );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_XML_VALERGEBNIS_CLOSE )
						+ kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ) );
			}

			// Ausgabe der Pfade zu den Pdftron Reports, falls welche
			// generiert wurden (-v) oder Pdftron Reports löschen
			File pdftronReport = new File( directoryOfLogfile,
					valDatei.getName() + ".pdftron-log.xml" );
			File pdftronXsl = new File( directoryOfLogfile, "report.xsl" );

			if ( pdftronReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> PDFTron-Report lassen
				} else {
					// kein optionaler Parameter --> PDFTron-Report loeschen!
					pdftronReport.delete();
					pdftronXsl.delete();
				}
			}

		} else {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_INCORRECTFILEENDING ) );
		}
		return valFile;
	}

}
