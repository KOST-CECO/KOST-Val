/*== KOST-Val ==================================================================================
The KOST-Val v1.0.7 application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
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

import java.io.File;
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
	 * Die Eingabe besteht aus 3 oder 4 Parameter: [0] Validierungstyp [1] Pfad
	 * zum Logging-Verzeichnis [2] Pfad zur Val-File [3] option: Verbose
	 * 
	 * @param args
	 */
	public static void main( String[] args )
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

		// Ist die Anzahl Parameter (mind. 3) korrekt?
		if ( args.length < 3 ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_PARAMETER_USAGE ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
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

		File tmpDir = new File( pathToWorkDir );

		// bestehendes Workverzeichnis ggf. löschen
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_WRONG_JRE ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		// Ueberprüfung des Parameters (Log-Verzeichnis)
		File directoryOfLogfile = new File( args[1] );
		if ( !directoryOfLogfile.exists() ) {
			directoryOfLogfile.mkdir();
		}

		// Im Logverzeichnis besteht kein Schreibrecht
		if ( !directoryOfLogfile.canWrite() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfLogfile ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		if ( !directoryOfLogfile.isDirectory() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_LOGDIRECTORY_NODIRECTORY ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		// bestehendes Workverzeichnis ggf. löschen und wieder anlegen
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}

		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		// Im workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		// Ueberprüfung des optionalen Parameters (3 -v --> im Verbose-mode
		// werden die originalen Logs nicht gelöscht (PDFTron, Jhove & Co.)
		boolean verbose = false;
		if ( args.length > 3 ) {
			if ( !(args[3].equals( "-v" )) ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						ERROR_PARAMETER_OPTIONAL_1 ) );
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_INTERRUPTED ) );
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
					ERROR_JHOVECONF_MISSING ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		File valDatei = new File( args[2] );
		File logDatei = null;
		logDatei = valDatei;

		// Ueberprüfung des Parameters (Val-Datei): existiert die Datei?
		if ( !valDatei.exists() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_VALFILE_FILENOTEXISTING ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		// Konfiguration des Loggings, ein File Logger wird
		// zusätzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context
				.getBean( "logconfigurator" );
		String logFileName = logConfigurator.configure(
				directoryOfLogfile.getAbsolutePath(), logDatei.getName() );
		LOGGER.logInfo( kostval.getTextResourceService().getText(
				MESSAGE_KOSTVALIDATION ) );

		if ( args[0].equals( "--format" ) ) {

			Integer countNio = 0;
			Integer countSummaryNio = 0;
			Integer count = 0;
			Integer pdfaCountIo = 0;
			Integer pdfaCountNio = 0;
			Integer siardCountIo = 0;
			Integer siardCountNio = 0;
			Integer tiffCountIo = 0;
			Integer tiffCountNio = 0;

			if ( !valDatei.isDirectory() ) {

				boolean valFile = valFile( valDatei, logFileName,
						directoryOfLogfile, verbose );

				// Löschen des Arbeitsverzeichnisses, falls eines
				// angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				} else {
					Util.deleteDir( tmpDir );
				}
				if ( valFile ) {
					// Löschen des Arbeitsverzeichnisses, falls eines
					// angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
				} else {
					// Löschen des Arbeitsverzeichnisses, falls eines
					// angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
				}
			} else {
				Map<String, File> fileMap = Util.getFileMap( valDatei, false );
				Set<String> fileMapKeys = fileMap.keySet();
				for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
						.hasNext(); ) {
					String entryName = iterator.next();
					File newFile = fileMap.get( entryName );
					if ( !newFile.isDirectory() ) {
						valDatei = newFile;
						count = count + 1;

						if ( (valDatei.getAbsolutePath().toLowerCase()
								.endsWith( ".tiff" ) || valDatei
								.getAbsolutePath().toLowerCase()
								.endsWith( ".tif" )) ) {

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
								.endsWith( ".siard" )) ) {

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

						} else if ( (valDatei.getName().endsWith( ".pdf" ) || valDatei
								.getName().endsWith( ".pdfa" )) ) {

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
							/*
							 * LOGGER.logInfo(
							 * kostval.getTextResourceService().getText(
							 * ERROR_INCORRECTFILEENDING ) ); LOGGER.logInfo(
							 * kostval.getTextResourceService().getText(
							 * MESSAGE_VALIDATION_INTERRUPTED ) );
							 */
							countNio = countNio + 1;
						}
					}
				}
				// Zeitstempel Start
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
						"dd.MM.yyyy HH.mm.ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );

				/*
				 * *************************************************************
				 * * Zusammenfassung der Formatvalidierung *
				 * ===================================== * Total: = count{0} * *
				 * PDF/A: Valid = countPdfaIo{1} Invalid = countPdfaNio{2} *
				 * SIARD: Valid = countSiardIo{3} Invalid = countSiardNio{4} *
				 * TIFF: Valid = countTiffIo{5} Invalid = countTiffNio{6} * *
				 * Sonstige Dateien: countNio{7} (ohne Formatvalidierung)
				 * Validierungszeitraum: ausgabeStart {8} - ausgabeEnd{9}
				 * *************************************************************
				 */
				countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio;

				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_FOOTER_SUMMARY, count, pdfaCountIo,
						pdfaCountNio, siardCountIo, siardCountNio, tiffCountIo,
						tiffCountNio, countNio, ausgabeStart, ausgabeEnd ) );

				if ( countNio == count ) {
					// keine Dateien Validiert
					LOGGER.logInfo( kostval.getTextResourceService().getText(
							ERROR_INCORRECTFILEENDING ) );
					LOGGER.logInfo( kostval.getTextResourceService().getText(
							MESSAGE_VALIDATION_INTERRUPTED ) );

					System.exit( 1 );
				} else if ( countSummaryNio == 0 ) {
					// alle Validierten Dateien valide
					System.exit( 0 );
				} else {
					// Fehler in Validierten Dateien --> invalide
					System.exit( 2 );
				}
			}
		} else if ( args[0].equals( "--sip" ) ) {
			// TODO: SIP-Validierung
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_SIPVALIDATION, valDatei.getName() ) );

			// Ueberprüfung des 1. Parameters (SIP-Datei): ist die Datei ein
			// Verzeichnis? Wenn ja, wird im work-Verzeichnis eine Zip-Datei
			// daraus erstellt, damit die weiteren Validierungen Gebrauch von
			// der java.util.zip API machen können, und somit die zu
			// Validierenden Archive gleichartig behandelt werden können, egal
			// ob es sich um eine Verzeichnisstruktur oder ein Zip-File handelt.
			// Informationen zum Arbeitsverzeichnis holen

			String originalSipName = valDatei.getAbsolutePath();
			if ( valDatei.isDirectory() ) {
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				tmpDir.mkdir();

				try {
					File targetFile = new File( pathToWorkDir,
							valDatei.getName() + ".zip" );
					Zip64Archiver.archivate( valDatei, targetFile );
					valDatei = targetFile;

				} catch ( Exception e ) {
					LOGGER.logInfo( kostval.getTextResourceService()
							.getText( ERROR_CANNOTCREATEZIP ) );
					System.exit( 1 );
				}

			} else {
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				File workDir = new File( pathToWorkDir );
				if ( workDir.exists() ) {
					Util.deleteDir( workDir );
				}
				workDir.mkdir();
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

			ok = (ok && okMandatory);

			LOGGER.logInfo( "" );
			if ( ok ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_VALID, valDatei.getAbsolutePath() ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_INVALID, valDatei.getAbsolutePath() ) );
			}
			LOGGER.logInfo( "" );

// TODO: Pfad zu Formatvalidierungsteil
			// Ausgabe der Pfade zu den Jhove/KOST-Val & Co. Reports, falls
			// welche
			// generiert wurden
/*			if ( Util.getPathToReportJHove() != null ) {
				LOGGER.logInfo( kostval.getTextResourceService()
						.getText( MESSAGE_FOOTER_REPORTJHOVE,
								Util.getPathToReportJHove() ) );
			}
			if ( Util.getPathToReportKostVal() != null ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_FOOTER_REPORTKOSTVAL,
						Util.getPathToReportKostVal() ) );
			}*/

			LOGGER.logInfo( "" );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_SIP, originalSipName ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_LOG, logFileName ) );
			LOGGER.logInfo( "" );

			if ( okMandatory ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_FINISHED ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_INTERRUPTED ) );
			}

			// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde

			File workDir = new File( pathToWorkDir );
			if ( workDir.exists() ) {
				Util.deleteDir( workDir );
			}
			if ( ok ) {
				System.exit( 0 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( workDir.exists() ) {
					Util.deleteDir( workDir );
				}

			} else {
				System.exit( 2 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( workDir.exists() ) {
					Util.deleteDir( workDir );
				}
				if ( workDir.exists() ) {
					Util.deleteDir( workDir );
				}
			}

		} else {
			// Ueberprüfung des Parameters (Val-Typ): format / sip
			// args[0] ist nicht "--format" oder "--sip" --> INVALIDE
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_PARAMETER_USAGE ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}
		// bestehendes Workverzeichnis ggf. löschen
		if ( tmpDir.exists() ) {
			tmpDir.delete();
			tmpDir.deleteOnExit();
		}
		// bestehendes Workverzeichnis ggf. löschen
		if ( tmpDir.exists() ) {
			tmpDir.delete();
			tmpDir.deleteOnExit();
		}
	}

	private static boolean valFile( File valDatei, String logFileName,
			File directoryOfLogfile, boolean verbose )
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );
		String originalValName = valDatei.getAbsolutePath();
		boolean valFile = false;

		if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".tiff" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".tif" )) ) {

			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_TIFFVALIDATION, valDatei.getName() ) );
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

			LOGGER.logInfo( "" );
			if ( ok ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_VALID, valDatei.getAbsolutePath() ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_INVALID, valDatei.getAbsolutePath() ) );
			}
			LOGGER.logInfo( "" );

			// Ausgabe der Pfade zu den Jhove Reports, falls welche
			// generiert wurden (-v) oder Jhove Report löschen
			File jhoveReport = new File( directoryOfLogfile, valDatei.getName()
					+ ".jhove-log.txt" );

			if ( jhoveReport.exists() ) {
				if ( verbose ) {
					LOGGER.logInfo( kostval.getTextResourceService().getText(
							MESSAGE_FOOTER_REPORTJHOVE,
							Util.getPathToReportJHove() ) );
					LOGGER.logInfo( "" );
				} else {
					// kein optionaler Parameter --> Jhove-Report loeschen!
					jhoveReport.delete();
				}
			}

			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_TIFF, originalValName ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_LOG, logFileName ) );
			LOGGER.logInfo( "" );

			if ( okMandatory ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_FINISHED ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_INTERRUPTED ) );
			}

		} else if ( (valDatei.getAbsolutePath().toLowerCase()
				.endsWith( ".siard" )) ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_SIARDVALIDATION, valDatei.getName() ) );
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

			LOGGER.logInfo( "" );
			if ( ok ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_VALID, valDatei.getAbsolutePath() ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_INVALID, valDatei.getAbsolutePath() ) );
			}
			LOGGER.logInfo( "" );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_SIARD, originalValName ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_LOG, logFileName ) );
			LOGGER.logInfo( "" );

			if ( okMandatory ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_FINISHED ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_INTERRUPTED ) );
			}

		} else if ( (valDatei.getName().endsWith( ".pdf" ) || valDatei
				.getName().endsWith( ".pdfa" )) ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_PDFAVALIDATION, valDatei.getName() ) );
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

			LOGGER.logInfo( "" );
			if ( ok ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_VALID, valDatei.getAbsolutePath() ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_TOTAL_INVALID, valDatei.getAbsolutePath() ) );
			}
			LOGGER.logInfo( "" );

			// Ausgabe der Pfade zu den Pdftron Reports, falls welche
			// generiert wurden (-v) oder Pdftron Reports löschen
			File pdftronReport = new File( directoryOfLogfile,
					valDatei.getName() + ".pdftron-log.xml" );
			File pdftronXsl = new File( directoryOfLogfile, "report.xsl" );

			if ( pdftronReport.exists() ) {
				if ( verbose ) {
					LOGGER.logInfo( kostval.getTextResourceService().getText(
							MESSAGE_FOOTER_REPORTPDFTRON,
							Util.getPathToReportPdftron() ) );
					LOGGER.logInfo( "" );
				} else {
					// kein optionaler Parameter --> PDFTron-Report loeschen!
					pdftronReport.delete();
					pdftronXsl.delete();
				}
			}

			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_PDFA, originalValName ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_FOOTER_LOG, logFileName ) );
			LOGGER.logInfo( "" );

			if ( okMandatory ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_FINISHED ) );
			} else {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_VALIDATION_INTERRUPTED ) );
			}

		} else {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_INCORRECTFILEENDING ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
		}
		return valFile;
	}

}
