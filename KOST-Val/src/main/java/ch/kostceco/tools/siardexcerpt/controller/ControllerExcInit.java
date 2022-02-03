/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
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

package ch.kostceco.tools.siardexcerpt.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;

import ch.kostceco.tools.siardexcerpt.logging.MessageConstants;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationServiceExc;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;
import ch.kostceco.tools.kosttools.util.Util;

/** Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des Controllers, des
 * Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ControllerExcInit implements MessageConstants
{

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

	public static boolean mainInit( String[] args ) throws IOException
	{
		boolean init = true;
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		/** SIARDexcerpt: Aufbau des Tools
		 * 
		 * 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken, config bei Bedarf
		 * ausfuellen
		 * 
		 * 2) search: gemaess config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
		 * 
		 * 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen
		 * 
		 * 4) finish: Config-Kopie sowie Workverzeichnis loeschen */

		/* TODO: siehe Bemerkung im applicationContext-services.xml bezueglich Injection in der
		 * Superklasse aller Impl-Klassen ValidationModuleImpl validationModuleImpl =
		 * (ValidationModuleImpl) context.getBean("validationmoduleimpl"); */
		ControllerExcInit controllerExcInit = (ControllerExcInit) context
				.getBean( "controllerExcInit" );

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
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_PARAMETER_USAGE ) );
			return false;
		}

		// String module = new String( args[3] ); // Modul wird nicht verwendet im ControllerInit
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

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.8.0" ) < 0 ) {
			System.out.println(
					controllerExcInit.getTextResourceServiceExc().getText( locale, EXC_ERROR_WRONG_JRE ) );
			return false;
		}

		System.out.println( "" );

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
		 * f) Config bei Bedarf (..) gemaess metadata.xml ausfuellen */

		System.out.println( "SIARDexcerpt: init" );

		/** a) config muss existieren und SIARDexcerpt.conf.xml noch nicht */
		File configFile = configFileNo;
		if ( configString.endsWith( ".xml" ) ) {
			configFile = new File( configString );
		} else {
			configFile = configFileNo;
		}

		if ( !configFile.exists() ) {
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_CONFIGFILE_FILENOTEXISTING, configFile.getAbsolutePath() ) );
			return false;
		}

		configFileHard = new File( config + File.separator + "SIARDexcerpt.conf.xml" );
		if ( configFileHard.exists() ) {
			// es wird versucht das configFileHard zu loeschen
			// Util.deleteFile( configFileHard );
			configFileHard.delete();
		}
		if ( configFileHard.exists() ) {
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_CONFIGFILEHARD_FILEEXISTING ) );
			return false;
		}
		Util.copyFile( configFile, configFileHard );

		Map<String, String> configMap = controllerExcInit.getConfigurationServiceExc()
				.configMap( locale );

		/** b) Excerptverzeichnis mit schreibrechte und ggf anlegen */
		// System.out.println( " b) Excerptverzeichnis mit schreibrechte und ggf anlegen " );
		directoryOfOutput = new File( configMap.get( "PathToOutput" ) );

		if ( !directoryOfOutput.exists() ) {
			directoryOfOutput.mkdir();
		}

		// Im Logverzeichnis besteht kein Schreibrecht
		if ( !directoryOfOutput.canWrite() ) {
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfOutput ) );
			// Loeschen des configFileHard, falls eines angelegt wurde
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}
			return false;
		}

		if ( !directoryOfOutput.isDirectory() ) {
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_LOGDIRECTORY_NODIRECTORY ) );
			// Loeschen des configFileHard, falls eines angelegt wurde
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}
			return false;
		}

		/** c) Workverzeichnis muss leer sein und mit schreibrechte */
		// System.out.println( " c) Workverzeichnis muss leer sein und mit schreibrechte " );
		tmpDir = new File( configMap.get( "PathToWorkDir" ) );

		/* bestehendes Workverzeichnis zuerst versuchen zu loeschen. Wenn nicht moeglich Abbruch */
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}

		if ( tmpDir.exists() ) {
			if ( tmpDir.isDirectory() ) {
				// Get list of file in the directory. When its length is not zero the folder is not empty.
				String[] files = tmpDir.list();
				if ( files.length > 0 ) {
					System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
							EXC_ERROR_WORKDIRECTORY_EXISTS, pathToWorkDir ) );
					return false;
				}
			}
		}
		tmpDir = new File( pathToWorkDir );
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		// Im Workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_WORKDIRECTORY_NOTWRITABLE, pathToWorkDir ) );
			// Loeschen des configFileHard, falls eines angelegt wurde
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}
			return false;
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
						System.console().printf( controllerExcInit.getTextResourceServiceExc().getText( locale,
								EXC_ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
						Thread.sleep( 5000 );
						return false;
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
						System.console().printf( controllerExcInit.getTextResourceServiceExc().getText( locale,
								EXC_ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
						Thread.sleep( 5000 );
						return false;
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
						System.console().printf( controllerExcInit.getTextResourceServiceExc().getText( locale,
								EXC_ERROR_SPECIAL_CHARACTER, name, matcher.group( i ) ) );
						Thread.sleep( 5000 );
						return false;
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
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_SIARDFILE_FILENOTEXISTING, siardDatei.getAbsolutePath() ) );
			// Loeschen des configFileHard, falls eines angelegt wurde
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}
			return false;
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
				// System.out.println(" SIARD Datei konnte nicht entpackt werden");
				System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
						EXC_MESSAGE_XML_MODUL_A ) );
				System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
						EXC_ERROR_XML_A_CANNOTEXTRACTZIP ) );

				// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				// Fehler Extraktion --> invalide
				System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
						EXC_MESSAGE_A_INIT_NOK ) );
				return false;
			} else {
				// System.out.println(" SIARD Datei konnte entpackt werden "+siardDatei.getAbsolutePath());
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
			System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_XML_B_STRUCTURE ) );
			// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}
			// Fehler Extraktion --> invalide
			System.out.println(
					controllerExcInit.getTextResourceServiceExc().getText( locale, EXC_MESSAGE_A_INIT_NOK ) );
			return false;
		} else {
			// Struktur sieht plausibel aus
		}

		if ( Util.stringInFile( "(..)", configFileHard ) ) {
			Controllerexcerpt controllerexcerptConfig = (Controllerexcerpt) context
					.getBean( "controllerexcerpt" );
			okAConfig = controllerexcerptConfig.executeAConfig( siardDatei, configFileHard, configString,
					configMap, locale );

			if ( !okAConfig ) {
				// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				// Fehler beim Ausfuellen der Config --> invalide
				System.out.println( controllerExcInit.getTextResourceServiceExc().getText( locale,
						EXC_MESSAGE_A_INIT_NOK_CONFIG ) );
				return false;
			}
		}
		// Initialisierung konnte durchgefuehrt werden
		System.out.println(
				controllerExcInit.getTextResourceServiceExc().getText( locale, EXC_MESSAGE_A_INIT_OK ) );
		init = true;

		return init;

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
