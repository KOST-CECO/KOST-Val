/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
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

package ch.kostceco.tools.siardexcerpt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.siardexcerpt.controller.Controllerexcerpt;
import ch.kostceco.tools.siardexcerpt.logging.LogConfigurator;
import ch.kostceco.tools.siardexcerpt.logging.Logger;
import ch.kostceco.tools.siardexcerpt.logging.MessageConstants;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationServiceExc;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des Controllers, des
 * Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class SIARDexcerpt implements MessageConstants
{

	private static final Logger			LOGGER	= new Logger( SIARDexcerpt.class );

	private TextResourceServiceExc	textResourceServiceExc;
	private ConfigurationServiceExc	configurationServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc()
	{
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc( TextResourceServiceExc textResourceServiceExc )
	{
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public ConfigurationServiceExc getConfigurationServiceExc()
	{
		return configurationServiceExc;
	}

	public void setConfigurationServiceExc( ConfigurationServiceExc configurationServiceExc )
	{
		this.configurationServiceExc = configurationServiceExc;
	}

	/** Die Eingabe besteht aus mind 3 Parameter: [0] Pfad zur SIARD-Datei oder Verzeichnis [1]
	 * configfile [2] Modul
	 * 
	 * uebersicht der Module: --init --search --extract sowie --finish
	 * 
	 * bei --search kommen danach noch die Suchtexte und bei --extract die Schluessel
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static void main( String[] args ) throws IOException
	{
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		/** SIARDexcerpt: Aufbau des Tools
		 * 
		 * 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken, config bei Bedarf
		 * ausfuellen
		 * 
		 * 2) search: gemäss config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
		 * 
		 * 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen
		 * 
		 * 4) finish: Config-Kopie sowie Workverzeichnis loeschen */

		/* TODO: siehe Bemerkung im applicationContext-services.xml bezueglich Injection in der
		 * Superklasse aller Impl-Klassen ValidationModuleImpl validationModuleImpl =
		 * (ValidationModuleImpl) context.getBean("validationmoduleimpl"); */
		SIARDexcerpt siardexcerpt = (SIARDexcerpt) context.getBean( "siardexcerpt" );

		Locale locale = Locale.getDefault();

		if ( args[2].equalsIgnoreCase( "--de" ) ) {
			locale = new Locale( "de" );
		} else if ( args[2].equalsIgnoreCase( "--fr" ) ) {
			locale = new Locale( "fr" );
		} else if ( args[2].equalsIgnoreCase( "--en" ) ) {
			locale = new Locale( "en" );
		} else {
			// ungueltige Eingabe Fehler wird ignoriert und default oder de wird angenommen
			if ( locale.toString().startsWith( "fr" ) ) {
				locale = new Locale( "fr" );
			} else if ( locale.toString().startsWith( "en" ) ) {
				locale = new Locale( "en" );
			} else {
				locale = new Locale( "de" );
			}
		}
		
		// Ist die Anzahl Parameter (mind 4) korrekt?
		if ( args.length < 4 ) {
			System.out
					.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		String module = new String( args[3] );
		File siardDatei = new File( args[0] );
		String configString = new String( args[1] );
		// Map<String, String> configMap = siardexcerpt.getConfigurationService().configMap();

		/* arg 1 gibt den Pfad zur configdatei an. Da dieser in ConfigurationServiceImpl hartcodiert
		 * ist, wird diese nach ".siardexcerpt/configuration/SIARDexcerpt.conf.xml" kopiert. */
		String userSIARDexcerpt = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt";
		File userSIARDexcerptFile = new File( userSIARDexcerpt );
		if ( !userSIARDexcerptFile.exists() ) {
			userSIARDexcerptFile.mkdirs();
		}
		String config = userSIARDexcerpt + File.separator + "configuration";
		File config1 = new File( config );
		if ( !config1.exists() ) {
			config1.mkdirs();
		}

		File configFileHard = new File( config + File.separator + "SIARDexcerpt.conf.xml" );
		File configFileNo = new File( "configuration" + File.separator + "NoConfig.conf.xml" );

		String pathToOutput = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
				+ File.separator + "Output";
		File directoryOfOutput = new File( pathToOutput );

		String pathToWorkDir = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
				+ File.separator + "temp_SIARDexcerpt";
		File tmpDir = new File( pathToWorkDir );

		boolean okA = false;
		boolean okAConfig = false;
		boolean okB = false;
		boolean okC = false;

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.8.0" ) < 0 ) {
			System.out.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_WRONG_JRE ) );
			System.exit( 1 );
		}

		System.out.println( "" );

		if ( module.equalsIgnoreCase( "--init" ) ) {

			/** 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken
			 * 
			 * a) config muss existieren und SIARDexcerpt.conf.xml noch nicht
			 * 
			 * b) Excerptverzeichnis mit schreibrechte und ggf anlegen
			 * 
			 * c) Workverzeichnis muss leer sein und mit schreibrechte
			 * 
			 * d) SIARD-Datei entpacken
			 * 
			 * e) Struktur-Check SIARD-Verzeichnis
			 * 
			 * f) Config bei Bedarf (..) gemäss metadata.xml ausfuellen
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: init" );

			/** a) config muss existieren und SIARDexcerpt.conf.xml noch nicht */
			File configFile = configFileNo;
			if ( configString.endsWith( ".xml" ) ) {
				configFile = new File( configString );
			} else {
				configFile = configFileNo;
			}

			if ( !configFile.exists() ) {
				System.out.println( siardexcerpt.getTextResourceServiceExc()
						.getText( locale, ERROR_CONFIGFILE_FILENOTEXISTING, configFile.getAbsolutePath() ) );
				System.exit( 1 );
			}

			configFileHard = new File( config + File.separator + "SIARDexcerpt.conf.xml" );
			if ( configFileHard.exists() ) {
				// es wird versucht das configFileHard zu loeschen
				// Util.deleteFile( configFileHard );
				configFileHard.delete();
			}
			if ( configFileHard.exists() ) {
				System.out.println(
						siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_CONFIGFILEHARD_FILEEXISTING ) );
				System.exit( 1 );
			}
			Util.copyFile( configFile, configFileHard );

			Map<String, String> configMap = siardexcerpt.getConfigurationServiceExc().configMap();

			/** b) Excerptverzeichnis mit schreibrechte und ggf anlegen */
			// System.out.println( " b) Excerptverzeichnis mit schreibrechte und ggf anlegen " );
			directoryOfOutput = new File( configMap.get( "PathToOutput" ) );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			// Im Logverzeichnis besteht kein Schreibrecht
			if ( !directoryOfOutput.canWrite() ) {
				System.out.println( siardexcerpt.getTextResourceServiceExc()
						.getText( locale, ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfOutput ) );
				// Loeschen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			if ( !directoryOfOutput.isDirectory() ) {
				System.out.println(
						siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_LOGDIRECTORY_NODIRECTORY ) );
				// Loeschen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			/** c) Workverzeichnis muss leer sein und mit schreibrechte */
			// System.out.println( " c) Workverzeichnis muss leer sein und mit schreibrechte " );
			tmpDir = new File( configMap.get( "PathToWorkDir" ) );

			/* bestehendes Workverzeichnis zuerst versuchen zu loeschen. Wenn nicht moeglich Abbruch */
			if ( tmpDir.exists() ) {
				Util.deleteDirWithoutOnExit( tmpDir );
			}

			if ( tmpDir.exists() ) {
				if ( tmpDir.isDirectory() ) {
					// Get list of file in the directory. When its length is not zero the folder is not empty.
					String[] files = tmpDir.list();
					if ( files.length > 0 ) {
						System.out.println( siardexcerpt.getTextResourceServiceExc()
								.getText( locale, ERROR_WORKDIRECTORY_EXISTS, pathToWorkDir ) );
						System.exit( 1 );
					}
				}
			}
			tmpDir = new File( pathToWorkDir );
			if ( !tmpDir.exists() ) {
				tmpDir.mkdir();
			}

			// Im Workverzeichnis besteht kein Schreibrecht
			if ( !tmpDir.canWrite() ) {
				System.out.println( siardexcerpt.getTextResourceServiceExc()
						.getText( locale, ERROR_WORKDIRECTORY_NOTWRITABLE, pathToWorkDir ) );
				// Loeschen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			try {
				// Im Pfad keine Sonderzeichen kann abstuerzen

				String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~:\\\\a-zA-Z0-9]";
				Pattern pattern = Pattern.compile( patternStr );

				String name = tmpDir.getAbsolutePath();
				String[] pathElements = name.split( "/" );
				for ( int i = 0; i < pathElements.length; i++ ) {
					String element = pathElements[i];

					Matcher matcher = pattern.matcher( element );

					boolean matchFound = matcher.find();
					if ( matchFound ) {
						if ( matcher.group( i ).equals( " " ) ) {
							// Leerschlag ab v0.0.9 OK
						} else {
							System.console().printf( siardexcerpt.getTextResourceServiceExc()
									.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
							Thread.sleep( 5000 );
							System.exit( 1 );
						}
					}
				}

				name = directoryOfOutput.getAbsolutePath();
				pathElements = name.split( "/" );
				for ( int i = 0; i < pathElements.length; i++ ) {
					String element = pathElements[i];

					Matcher matcher = pattern.matcher( element );

					boolean matchFound = matcher.find();
					if ( matchFound ) {
						if ( matcher.group( i ).equals( " " ) ) {
							// Leerschlag ab v0.0.9 OK
						} else {
							System.console().printf( siardexcerpt.getTextResourceServiceExc()
									.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
							Thread.sleep( 5000 );
							System.exit( 1 );
						}
					}
				}

				name = siardDatei.getAbsolutePath();
				pathElements = name.split( "/" );
				for ( int i = 0; i < pathElements.length; i++ ) {
					String element = pathElements[i];

					Matcher matcher = pattern.matcher( element );

					boolean matchFound = matcher.find();
					if ( matchFound ) {
						if ( matcher.group( i ).equals( " " ) ) {
							// Leerschlag ab v0.0.9 OK
						} else {
							System.console().printf( siardexcerpt.getTextResourceServiceExc()
									.getText( locale, ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
							Thread.sleep( 5000 );
							System.exit( 1 );
						}
					}
				}

			} catch ( Exception e ) {
				System.out.println( "Exception: " + e.getMessage() );
			}

			/** d) SIARD-Datei entpacken */
			// System.out.println( " d) SIARD-Datei entpacken " );
			if ( !siardDatei.exists() ) {
				// SIARD-Datei existiert nicht
				System.out.println( siardexcerpt.getTextResourceServiceExc()
						.getText( locale, ERROR_SIARDFILE_FILENOTEXISTING, siardDatei.getAbsolutePath() ) );
				// Loeschen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {

				/* SIARD-Datei ist eine Datei
				 * 
				 * Die Datei muss ins Workverzeichnis extrahiert werden. Dies erfolgt im Modul A.
				 * 
				 * danach der Pfad zu SIARD-Datei dorthin zeigen lassen */

				Controllerexcerpt controllerexcerpt = (Controllerexcerpt) context
						.getBean( "controllerexcerpt" );
				File siardDateiNew = new File( pathToWorkDir + File.separator + siardDatei.getName() );
				okA = controllerexcerpt.executeA( siardDatei, siardDateiNew, "", configMap, locale );

				if ( !okA ) {
					// SIARD Datei konte nicht entpackt werden
					System.out
							.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_A ) );
					System.out.println(
							siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_XML_A_CANNOTEXTRACTZIP ) );

					// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( configFileHard.exists() ) {
						Util.deleteDir( configFileHard );
					}
					// Fehler Extraktion --> invalide
					System.out
							.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_A_INIT_NOK ) );
					System.exit( 2 );
				} else {
					@SuppressWarnings("unused")
					File siardDateiOld = siardDatei;
					siardDatei = siardDateiNew;
				}

			} else {
				/* SIARD-Datei entpackt oder Datei war bereits ein Verzeichnis.
				 * 
				 * Gerade bei groesseren SIARD-Dateien ist es sinnvoll an einer Stelle das ausgepackte SIARD
				 * zu haben, damit diese nicht immer noch extrahiert werden muss */
			}

			/** e) Struktur-Check SIARD-Verzeichnis */
			// System.out.println( " e) Struktur-Check SIARD-Verzeichnis " );
			/* File content = new File( siardDatei.getAbsolutePath() + File.separator + "content" );
			 * 
			 * Content darf nicht existieren. Dann handelt es sich um eine Reine Strukturablieferung */
			File header = new File( siardDatei.getAbsolutePath() + File.separator + "header" );
			File xsd = new File( siardDatei.getAbsolutePath() + File.separator + "header" + File.separator
					+ "metadata.xsd" );
			File metadata = new File( siardDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xml" );

			if ( !header.exists() || !xsd.exists() || !metadata.exists() ) {
				System.out
						.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_XML_B_STRUCTURE ) );
				// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				// Fehler Extraktion --> invalide
				System.out
						.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_A_INIT_NOK ) );
				System.exit( 2 );
			} else {
				// Struktur sieht plausibel aus
			}

			if ( Util.stringInFile( "(..)", configFileHard ) ) {
				Controllerexcerpt controllerexcerptConfig = (Controllerexcerpt) context
						.getBean( "controllerexcerpt" );
				okAConfig = controllerexcerptConfig.executeAConfig( siardDatei, configFileHard,
						configString, configMap, locale );

				if ( !okAConfig ) {
					// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( configFileHard.exists() ) {
						Util.deleteDir( configFileHard );
					}
					// Fehler beim Ausfuellen der Config --> invalide
					System.out.println(
							siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_A_INIT_NOK_CONFIG ) );
					System.exit( 2 );
				}
			}
			// Initialisierung konnte durchgefuehrt werden
			System.out.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_A_INIT_OK ) );
			System.exit( 0 );

		} // End init

		if ( module.equalsIgnoreCase( "--search" ) ) {

			/** 2) search: gemäss config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
			 * 
			 * a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext
			 * 
			 * b) Suchtext einlesen
			 * 
			 * c) search.xml vorbereiten (Header) und xsl in Output kopieren
			 * 
			 * d) grep ausfuehren
			 * 
			 * d1) grep
			 * 
			 * d2) TODO: die 12 searchCells definieren: auslesen der Vorgaben oder automatisch erstellen
			 * (1. pk dann [TODO: trefferspalten dann bB anhand column.name (name, id, ort, town) dann bB]
			 * auffuellen mit den ersten Spalten bis 12 existieren).
			 * 
			 * d3) grep auf die 12 searchCells kuerzen
			 * 
			 * e) Suchergebnis speichern und anzeigen (via GUI)
			 * 
			 * TODO: Noch offen */

			System.out.println( "SIARDexcerpt: search" );

			Map<String, String> configMap = siardexcerpt.getConfigurationServiceExc().configMap();

			if ( pathToWorkDir.startsWith( "Configuration-Error:" ) ) {
				System.out.println( pathToWorkDir );
				System.exit( 1 );
			}

			tmpDir = new File( pathToWorkDir );

			directoryOfOutput = new File( pathToOutput );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			/** a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Suchtext */
			// System.out.println( " a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Suchtext " );
			if ( args.length < 5 ) {
				System.out
						.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_PARAMETER_USAGE ) );
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {
				File siardDateiNew = new File(
						tmpDir.getAbsolutePath() + File.separator + siardDatei.getName() );
				if ( !siardDateiNew.exists() ) {
					System.out.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_NOINIT ) );
					System.exit( 1 );
				} else {
					siardDatei = siardDateiNew;
				}
			}

			/** b) Suchtext einlesen */
			String searchString = new String( args[4] );
			// System.out.println( " b) Suchtext '" + searchString + "' einlesen " );

			/* Der SearchString kann nur die Wildcard (* oder .) enthalten. Da grep es aber nicht
			 * unterstuetzt durch row ersetzten */
			if ( searchString.equals( "*" ) || searchString.equals( "." ) ) {
				searchString = "row";
			}

			/** c) search.xml vorbereiten (Header) und xsl in Output kopieren */
			// System.out.println( " c) search.xml vorbereiten (Header) und xsl in Output kopieren " );
			// Zeitstempel der Datenextraktion
			java.util.Date nowStartS = new java.util.Date();
			java.text.SimpleDateFormat sdfStartS = new java.text.SimpleDateFormat(
					"dd.MM.yyyy HH:mm:ss" );
			String ausgabeStartS = sdfStartS.format( nowStartS );

			/* Der SearchString kann zeichen enthalten, welche nicht im Dateinamen vorkommen duerfen.
			 * Entsprechend werden diese normalisiert */
			String searchStringFilename = searchString.replaceAll( "/", "_" );
			searchStringFilename = searchStringFilename.replaceAll( ">", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "<", "_" );
			searchStringFilename = searchStringFilename.replace( "*", "_" );
			searchStringFilename = searchStringFilename.replace( ".*", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "___", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "__", "_" );

			String outDateiNameS = siardDatei.getName() + "_" + searchStringFilename + "_SIARDsearch.xml";
			outDateiNameS = outDateiNameS.replaceAll( "__", "_" );

			// Informationen zum Archiv holen
			String archiveS = configMap.get( "Archive" );
			if ( archiveS.startsWith( "Configuration-Error:" ) ) {
				System.out.println( archiveS );
				System.exit( 1 );
			}

			// Konfiguration des Outputs, ein File Logger wird zusätzlich erstellt
			LogConfigurator logConfiguratorS = (LogConfigurator) context.getBean( "logconfiguratorExc" );
			String outFileNameS = logConfiguratorS.configure( directoryOfOutput.getAbsolutePath(),
					outDateiNameS );
			File outFileSearch = new File( outFileNameS );
			// Ab hier kann ins Output geschrieben werden...

			// Informationen zum XSL holen
			String pathToXSLS = configMap.get( "PathToXSLsearch" );
			if ( pathToXSLS.startsWith( "Configuration-Error:" ) ) {
				System.out.println( pathToXSLS );
				System.exit( 1 );
			}

			File xslOrigS = new File( pathToXSLS );
			File xslCopyS = new File(
					directoryOfOutput.getAbsolutePath() + File.separator + xslOrigS.getName() );
			if ( !xslCopyS.exists() ) {
				Util.copyFile( xslOrigS, xslCopyS );
			}

			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_HEADER,
					xslCopyS.getName() ) );
			LOGGER.logError(
					siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_START, ausgabeStartS ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT, archiveS,
					"Archive" ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_INFO ) );

			/** d) search: dies ist in einem eigenen Modul realisiert */
			// System.out.println( " d) search: dies ist in einem eigenen Modul realisiert " );
			Controllerexcerpt controllerexcerptS = (Controllerexcerpt) context
					.getBean( "controllerexcerpt" );

			okB = controllerexcerptS.executeB( siardDatei, outFileSearch, searchString, configMap,
					locale );

			/** e) Ausgabe und exitcode */
			// System.out.println( " e) Ausgabe und exitcode " );
			if ( !okB ) {
				// Suche konnte nicht erfolgen
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_B ) );
				LOGGER.logError(
						siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_XML_B_CANNOTSEARCHRECORD ) );
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );
				System.out
						.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_B_SEARCH_NOK ) );
				// Loeschen der temporären Suchergebnisse
				File outFileSearchTmp = new File( outFileSearch.getAbsolutePath() + ".tmp" );
				if ( outFileSearchTmp.exists() ) {
					Util.deleteFile( outFileSearchTmp );
				}
				String noResult = siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_B_SEARCH_NOK );
				if ( outFileSearchTmp.exists() ) {
					Util.replaceAllChar( outFileSearchTmp, noResult );
				}
				if ( outFileSearch.exists() ) {
					Util.replaceAllChar( outFileSearch, noResult );
				}

				// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Fehler Extraktion --> invalide
				System.exit( 2 );
			} else {
				// Suche konnte durchgefuehrt werden

				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFileHard );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( outFileSearch );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( outFileSearch );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zurueck ändern
					String newstring = siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_HEADER,
							xslCopyS.getName() );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>";
					Util.oldnewstring( oldstring, newstring, outFileSearch );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>" + siardexcerpt.getTextResourceServiceExc()
							.getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
					System.exit( 2 );
				}

				// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Record konnte extrahiert werden
				System.out.println(
						siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_B_SEARCH_OK, outFileNameS ) );
				System.exit( 0 );
			}

		} // End search

		if ( module.equalsIgnoreCase( "--excerpt" ) ) {

			/** 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen
			 * 
			 * a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext
			 * 
			 * b) extract.xml vorbereiten (Header) und xsl in Output kopieren
			 * 
			 * c) extraktion: dies ist in einem eigenen Modul realisiert
			 * 
			 * d) Ausgabe und exitcode
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: extract" );

			Map<String, String> configMap = siardexcerpt.getConfigurationServiceExc().configMap();

			if ( pathToWorkDir.startsWith( "Configuration-Error:" ) ) {
				System.out.println( pathToWorkDir );
				System.exit( 1 );
			}

			tmpDir = new File( pathToWorkDir );

			directoryOfOutput = new File( pathToOutput );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			/** a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Schluessel */
			// System.out.println( " a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Schluessel" );
			if ( args.length < 5 ) {
				System.out
						.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_PARAMETER_USAGE ) );
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {
				File siardDateiNew = new File(
						tmpDir.getAbsolutePath() + File.separator + siardDatei.getName() );
				if ( !siardDateiNew.exists() ) {
					System.out.println( siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_NOINIT ) );
					System.exit( 1 );
				} else {
					siardDatei = siardDateiNew;
				}
			}

			/** b) extract.xml vorbereiten (Header) und xsl in Output kopieren */
			// System.out.println( " b) extract.xml vorbereiten (Header) und xsl in Output kopieren" );
			// Zeitstempel der Datenextraktion
			java.util.Date nowStart = new java.util.Date();
			java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeStart = sdfStart.format( nowStart );

			String excerptString = new String( args[4] );
			/* Der excerptString kann zeichen enthalten, welche nicht im Dateinamen vorkommen duerfen.
			 * Entsprechend werden diese normalisiert */
			String excerptStringFilename = excerptString.replaceAll( "/", "_" );
			excerptStringFilename = excerptStringFilename.replaceAll( ">", "_" );
			excerptStringFilename = excerptStringFilename.replaceAll( "<", "_" );
			excerptStringFilename = excerptStringFilename.replace( "*", "_" );
			excerptStringFilename = excerptStringFilename.replace( ".*", "_" );
			excerptStringFilename = excerptStringFilename.replaceAll( "___", "_" );
			excerptStringFilename = excerptStringFilename.replaceAll( "__", "_" );

			excerptString = excerptString.replaceAll( "\\*", "\\." );

			String outDateiName = siardDatei.getName() + "_" + excerptStringFilename
					+ "_SIARDexcerpt.xml";
			outDateiName = outDateiName.replaceAll( "__", "_" );

			// Informationen zum Archiv holen
			String archive = configMap.get( "Archive" );
			if ( archive.startsWith( "Configuration-Error:" ) ) {
				System.out.println( archive );
				System.exit( 1 );
			}

			// Konfiguration des Outputs, ein File Logger wird zusätzlich erstellt
			LogConfigurator logConfigurator = (LogConfigurator) context.getBean( "logconfiguratorExc" );
			String outFileName = logConfigurator.configure( directoryOfOutput.getAbsolutePath(),
					outDateiName );
			File outFile = new File( outFileName );
			// Ab hier kann ins Output geschrieben werden...
			/* BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( new
			 * File( outFileName + "_new.xml" ) ), "UTF-8" ) ); try { */
			// Informationen zum XSL holen
			String pathToXSL = configMap.get( "PathToXSL" );
			if ( pathToXSL.startsWith( "Configuration-Error:" ) ) {
				System.out.println( pathToXSL );
				System.exit( 1 );
			}

			File xslOrig = new File( pathToXSL );
			File xmlExtracted = new File( siardDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xml" );
			File xslCopy;
			if ( !xslOrig.exists() || pathToXSL.equals( "(.. )" ) ) {
				// XSL AutoGenerating mit resources/SIARDexcerptAutoXSL.txt
				File xsltxt = new File( "resources" + File.separator + "SIARDexcerptAutoXSL.txt" );
				if ( !xsltxt.exists() ) {
					System.out.println( siardexcerpt.getTextResourceServiceExc()
							.getText( locale, ERROR_CONFIGFILE_FILENOTEXISTING, xsltxt.getAbsolutePath() ) );
					System.exit( 1 );
				}
				xslCopy = new File( directoryOfOutput.getAbsolutePath() + File.separator
						+ "SIARDexcerptAutoXSL_" + siardDatei.getName() + ".xsl" );
				if ( !xslCopy.exists() ) {
					Util.copyFile( xsltxt, xslCopy );

					/* xslCopy mit den Werten befuellen
					 * 
					 * AUTO_XSL_TABLE_START -> Fuer jede Tabelle anlegen {0} ist die Zahl z.B. 1 fuer table1
					 * AUTO_XSL_COLUMN -> Fuer jede Zeile anlegen {0} ist die columnNr z.B. 3 fur c3
					 * AUTO_XSL_TABLE_END -> am Ende der Tabelle Anlegen. Dann wiederholen der ersten drei
					 * Schritte oder AUTO_XSL_FOOTER zum Abschluss anlegen */

					// aus xmlExtracted die Tabelle herauslesen

					try {

						DocumentBuilderFactory dbfConfig = DocumentBuilderFactory.newInstance();
						DocumentBuilder dbConfig = dbfConfig.newDocumentBuilder();
						Document docConfig = dbConfig.parse( new FileInputStream( xmlExtracted ), "UTF8" );
						docConfig.getDocumentElement().normalize();
						dbfConfig.setFeature( "http://xml.org/sax/features/namespaces", false );

						/* columns.column werden mit number erweitert, damit die Zuordnung nicht jedesmal via
						 * Zähler erfolgen muss */
						NodeList nlColumns = docConfig.getElementsByTagName( "columns" );
						String provEndXSL = "</body></html></xsl:template></xsl:stylesheet>";
						for ( int x = 0; x < nlColumns.getLength(); x++ ) {
							int counterColumn = 0;
							String tableName = "";
							String schemaName = "";
							Node nodeColumns = nlColumns.item( x );
							NodeList nlColumn = nodeColumns.getChildNodes();
							counterColumn = ((nlColumn.getLength() + 1) / 2);
							// counterColumn = Anzahl Column plus 1
							Node nTable = nodeColumns.getParentNode();
							// table

							// Schema name und folder herauslesen
							Node mainTables = nTable.getParentNode();
							Node mainSchema = mainTables.getParentNode();
							NodeList nlSchemaChild = mainSchema.getChildNodes();
							for ( int xs = 0; xs < nlSchemaChild.getLength(); xs++ ) {
								// fuer jedes Subelement der Tabelle (name, folder, description...) ...
								Node subNode = nlSchemaChild.item( xs );
								if ( subNode.getNodeName().equals( "name" ) ) {
									schemaName = subNode.getTextContent();
								}
							}

							NodeList nlTable = nTable.getChildNodes();
							for ( int y = 0; y < nlTable.getLength(); y++ ) {
								Node subNode = nlTable.item( y );
								if ( subNode.getNodeName().equals( "name" ) ) {
									tableName = subNode.getTextContent();
									// System.out.println( tableFolder + ": " + (counterColumn-1) + " Spalten." );
									Util.oldnewstring( provEndXSL, siardexcerpt.getTextResourceServiceExc()
											.getText( locale, AUTO_XSL_TABLE_START, schemaName + "_" + tableName ), xslCopy );
									for ( int z = 1; z < counterColumn; z++ ) {
										Util.oldnewstring( provEndXSL,
												siardexcerpt.getTextResourceServiceExc().getText( locale, AUTO_XSL_COLUMN, z ),
												xslCopy );
									}
									Util.oldnewstring( provEndXSL,
											siardexcerpt.getTextResourceServiceExc().getText( locale, AUTO_XSL_TABLE_END ),
											xslCopy );
								}
							}
						}
						Util.oldnewstring( provEndXSL,
								siardexcerpt.getTextResourceServiceExc().getText( locale, AUTO_XSL_FOOTER ), xslCopy );
					} catch ( Exception e ) {
						LOGGER.logError( "<Error>" + siardexcerpt.getTextResourceServiceExc()
								.getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
						LOGGER
								.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );
						System.out.println( "Exception: " + e.getMessage() );
						System.exit( 2 );
					}
				}
			} else {
				// Original Xsl in output kopieren
				xslCopy = new File(
						directoryOfOutput.getAbsolutePath() + File.separator + xslOrig.getName() );
				if ( !xslCopy.exists() ) {
					Util.copyFile( xslOrig, xslCopy );
				}
			}
			// Information aus metadata holen
			String dbname = "";
			String dataOriginTimespan = "";
			String dbdescription = "";
			String keyexcerpt = "";

			try {

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				// dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();

				Document doc = db.parse( new FileInputStream( xmlExtracted ), "UTF8" );

				doc.getDocumentElement().normalize();

				dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

				NodeList nlDbname = doc.getElementsByTagName( "dbname" );
				Node nodeDbname = nlDbname.item( 0 );
				dbname = nodeDbname.getTextContent();

				NodeList nlDataOriginTimespan = doc.getElementsByTagName( "dataOriginTimespan" );
				Node nodeDataOriginTimespan = nlDataOriginTimespan.item( 0 );
				dataOriginTimespan = nodeDataOriginTimespan.getTextContent();

				NodeList nlSiardArchive = doc.getElementsByTagName( "siardArchive" );
				Node nodeSiardArchive = nlSiardArchive.item( 0 );
				NodeList childNodes = nodeSiardArchive.getChildNodes();
				for ( int x = 0; x < childNodes.getLength(); x++ ) {
					Node subNode = childNodes.item( x );
					if ( subNode.getNodeName().equals( "description" ) ) {
						dbdescription = new String( subNode.getTextContent() );
					}
				}

				String primarykeyname = configMap.get( "MaintablePrimarykeyName" );
				if ( primarykeyname.startsWith( "Configuration-Error:" ) ) {
					System.out.println( primarykeyname );
					System.exit( 1 );
				}
				keyexcerpt = primarykeyname + " = " + excerptString;

			} catch ( Exception e ) {
				LOGGER.logError( "<Error>" + siardexcerpt.getTextResourceServiceExc()
						.getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );
				System.out.println( "Exception: " + e.getMessage() );
				System.exit( 2 );
			}

			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_HEADER,
					xslCopy.getName() ) );
			LOGGER.logError(
					siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_START, ausgabeStart ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT, archive,
					"Archive" ) );
			LOGGER.logError(
					siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT, dbname, "dbname" ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT,
					dataOriginTimespan, "dataOriginTimespan" ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT,
					dbdescription, "dbdescription" ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_TEXT,
					keyexcerpt, "keyexcerpt" ) );
			LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_INFO ) );

			/** c) extraktion: dies ist in einem eigenen Modul realisiert */
			Controllerexcerpt controllerexcerpt = (Controllerexcerpt) context
					.getBean( "controllerexcerpt" );

			okC = controllerexcerpt.executeC( siardDatei, outFile, excerptString, configMap, locale );

			/** d) Ausgabe und exitcode */
			if ( !okC ) {
				// Record konnte nicht extrahiert werden
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_MODUL_C ) );
				LOGGER.logError(
						siardexcerpt.getTextResourceServiceExc().getText( locale, ERROR_XML_C_CANNOTEXTRACTRECORD ) );
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );
				System.out.println( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_C_EXCERPT_NOK,
						outFileName ) );

				// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Fehler Extraktion --> invalide
				System.exit( 2 );
			} else {
				// Record konnte extrahiert werden
				LOGGER.logError( siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_XML_LOGEND ) );

				// Die Konfiguration hereinkopieren
				/* try { DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				 * factory.setValidating( false );
				 * 
				 * factory.setExpandEntityReferences( false );
				 * 
				 * Document docConfig = factory.newDocumentBuilder().parse( configFile ); NodeList list =
				 * docConfig.getElementsByTagName( "configuration" ); Element element = (Element) list.item(
				 * 0 );
				 * 
				 * Document docLog = factory.newDocumentBuilder().parse( outFile );
				 * 
				 * Node dup = docLog.importNode( element, true );
				 * 
				 * docLog.getDocumentElement().appendChild( dup ); FileWriter writer = new FileWriter(
				 * outFile );
				 * 
				 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); ElementToStream(
				 * docLog.getDocumentElement(), baos ); String stringDoc2 = new String( baos.toByteArray()
				 * ); writer.write( stringDoc2 ); writer.close();
				 * 
				 * // Der Header wird dabei leider verschossen, wieder zurueck ändern String newstring =
				 * siardexcerpt.getTextResourceServiceExc().getText( MESSAGE_XML_HEADER, xslCopy.getName()
				 * ); String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>";
				 * Util.oldnewstring( oldstring, newstring, outFile );
				 * 
				 * } catch ( Exception e ) { LOGGER.logError( "<Error>" +
				 * siardexcerpt.getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				 * System.out.println( "Exception: " + e.getMessage() ); } */

				// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Record konnte extrahiert werden
				System.out.println(
						siardexcerpt.getTextResourceServiceExc().getText( locale, MESSAGE_C_EXCERPT_OK, outFileName ) );
				System.exit( 0 );

			}
			/* } finally { out.flush(); out.close(); } */

		} // End extract

		if ( module.equalsIgnoreCase( "--finish" ) ) {

			/** 4) finish: Config-Kopie sowie Workverzeichnis loeschen
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: finish" );

			// Loeschen des Arbeitsverzeichnisses und confiFileHard, falls eines angelegt wurde
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}

		} // End finish

	}

	public static void ElementToStream( Element element, OutputStream out )
	{
		try {
			DOMSource source = new DOMSource( element );
			StreamResult result = new StreamResult( out );
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform( source, result );
		} catch ( Exception ex ) {
		}
	}

}
