/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2021 Claire Roethlisberger (KOST-CECO)
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

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
import ch.kostceco.tools.kosttools.util.Util;

/** Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des Controllers, des
 * Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ControllerExcSearch implements MessageConstants
{

	private static final Logger			LOGGER	= new Logger( ControllerExcSearch.class );

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

	public static boolean mainSearch( String[] args ) throws IOException
	{
		boolean search = false;
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
		ControllerExcSearch controllerExcSearch = (ControllerExcSearch) context
				.getBean( "controllerExcSearch" );

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
		if ( args.length == 4 ) {
			System.out.println( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_PARAMETER_USAGE ) );
			return false;
		}

		File siardDatei = new File( args[0] );
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

		String pathToOutput = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
				+ File.separator + "Output";
		File directoryOfOutput = new File( pathToOutput );

		String pathToWorkDir = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
				+ File.separator + "temp_SIARDexcerpt";
		File tmpDir = new File( pathToWorkDir );

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.8.0" ) < 0 ) {
			System.out.println(
					controllerExcSearch.getTextResourceServiceExc().getText( locale, EXC_ERROR_WRONG_JRE ) );
			return false;
		}

		System.out.println( "" );

		/** 2) search: gemaess config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
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
		 * d2) TODO: die 12 searchCells definieren: auslesen der Vorgaben oder automatisch erstellen (1.
		 * pk dann [TODO: trefferspalten dann bB anhand column.name (name, id, ort, town) dann bB]
		 * auffuellen mit den ersten Spalten bis 12 existieren).
		 * 
		 * d3) grep auf die 12 searchCells kuerzen
		 * 
		 * e) Suchergebnis speichern und anzeigen (via GUI) */

		System.out.println( "SIARDexcerpt: search" );

		Map<String, String> configMap = controllerExcSearch.getConfigurationServiceExc().configMap();

		if ( pathToWorkDir.startsWith( "Configuration-Error:" ) ) {
			System.out.println( pathToWorkDir );
			return false;
		}

		tmpDir = new File( pathToWorkDir );

		directoryOfOutput = new File( pathToOutput );

		if ( !directoryOfOutput.exists() ) {
			directoryOfOutput.mkdir();
		}

		/** a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Suchtext */
		// System.out.println( " a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Suchtext " );
		if ( args.length < 5 ) {
			System.out.println( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_PARAMETER_USAGE ) );
			return false;
		}

		if ( !siardDatei.isDirectory() ) {
			File siardDateiNew = new File(
					tmpDir.getAbsolutePath() + File.separator + siardDatei.getName() );
			if ( !siardDateiNew.exists() ) {
				System.out.println(
						controllerExcSearch.getTextResourceServiceExc().getText( locale, EXC_ERROR_NOINIT ) );
				return false;
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
		java.text.SimpleDateFormat sdfStartS = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
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
			return false;
		}

		// Konfiguration des Outputs, ein File Logger wird zusaetzlich erstellt
		LogConfigurator logConfiguratorS = (LogConfigurator) context.getBean( "logconfiguratorExc" );
		String outFileNameS = logConfiguratorS.configure( directoryOfOutput.getAbsolutePath(),
				outDateiNameS );
		File outFileSearch = new File( outFileNameS );
		// Ab hier kann ins Output geschrieben werden...

		// Informationen zum XSL holen
		String pathToXSLS = configMap.get( "PathToXSLsearch" );
		if ( pathToXSLS.startsWith( "Configuration-Error:" ) ) {
			System.out.println( pathToXSLS );
			return false;
		}

		File xslOrigS = new File( pathToXSLS );
		File xslCopyS = new File(
				directoryOfOutput.getAbsolutePath() + File.separator + xslOrigS.getName() );
		if ( !xslCopyS.exists() ) {
			Util.copyFile( xslOrigS, xslCopyS );
		}

		LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
				EXC_MESSAGE_XML_HEADER, xslCopyS.getName() ) );
		LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
				EXC_MESSAGE_XML_START, ausgabeStartS ) );
		LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
				EXC_MESSAGE_XML_TEXT, archiveS, "Archive" ) );
		LOGGER.logError(
				controllerExcSearch.getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_INFO ) );

		/** d) search: dies ist in einem eigenen Modul realisiert */
		// System.out.println( " d) search: dies ist in einem eigenen Modul realisiert " );
		Controllerexcerpt controllerexcerptS = (Controllerexcerpt) context
				.getBean( "controllerexcerpt" );

		search = controllerexcerptS.executeB( siardDatei, outFileSearch, searchString, configMap,
				locale );

		/** e) Ausgabe und exitcode */
		// System.out.println( " e) Ausgabe und exitcode " );
		if ( !search ) {
			// Suche konnte nicht erfolgen
			LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_XML_MODUL_B ) );
			LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_ERROR_XML_B_CANNOTSEARCHRECORD ) );
			LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_XML_LOGEND ) );
			System.out.println( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_B_SEARCH_NOK ) );
			// Loeschen der temporaeren Suchergebnisse
			File outFileSearchTmp = new File( outFileSearch.getAbsolutePath() + ".tmp" );
			if ( outFileSearchTmp.exists() ) {
				Util.deleteFile( outFileSearchTmp );
			}
			String noResult = controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_B_SEARCH_NOK );
			if ( outFileSearchTmp.exists() ) {
				Util.replaceAllChar( outFileSearchTmp, noResult );
			}
			if ( outFileSearch.exists() ) {
				Util.replaceAllChar( outFileSearch, noResult );
			}

			// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

			// Fehler Extraktion --> invalide
			return false;
		} else {
			// Suche konnte durchgefuehrt werden

			LOGGER.logError( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_XML_LOGEND ) );

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

				// Der Header wird dabei leider verschossen, wieder zurueck aendern
				String newstring = controllerExcSearch.getTextResourceServiceExc().getText( locale,
						EXC_MESSAGE_XML_HEADER, xslCopyS.getName() );
				String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>";
				Util.oldnewstring( oldstring, newstring, outFileSearch );

			} catch ( Exception e ) {
				LOGGER.logError( "<Error>" + controllerExcSearch.getTextResourceServiceExc()
						.getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
				System.out.println( "Exception: " + e.getMessage() );
				return false;
			}

			// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

			// Record konnte extrahiert werden
			System.out.println( controllerExcSearch.getTextResourceServiceExc().getText( locale,
					EXC_MESSAGE_B_SEARCH_OK, outFileNameS ) );
			// search = true;
			return search;

		}

		// End search

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
