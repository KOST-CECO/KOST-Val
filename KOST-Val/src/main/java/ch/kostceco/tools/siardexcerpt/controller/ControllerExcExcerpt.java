/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;

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
import org.xml.sax.InputSource;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.siardexcerpt.logging.LogConfigurator;
import ch.kostceco.tools.siardexcerpt.logging.Logtxt;
import ch.kostceco.tools.siardexcerpt.logging.MessageConstants;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationServiceExc;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

/**
 * Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des
 * Controllers, des Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ControllerExcExcerpt implements MessageConstants {

	private TextResourceServiceExc textResourceServiceExc;
	private ConfigurationServiceExc configurationServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc() {
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc(TextResourceServiceExc textResourceServiceExc) {
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public ConfigurationServiceExc getConfigurationServiceExc() {
		return configurationServiceExc;
	}

	public void setConfigurationServiceExc(ConfigurationServiceExc configurationServiceExc) {
		this.configurationServiceExc = configurationServiceExc;
	}

	/**
	 * Die Eingabe besteht aus mind 3 Parameter: [0] Pfad zur SIARD-Datei oder
	 * Verzeichnis [1] configfile [2] Modul
	 * 
	 * uebersicht der Module: --init --search --extract sowie --finish
	 * 
	 * bei --search kommen danach noch die Suchtexte und bei --extract die
	 * Schluessel
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static boolean mainExcerpt(String[] args) throws IOException {
		boolean excerpt = true;
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:config/applicationContext.xml");

		/**
		 * SIARDexcerpt: Aufbau des Tools
		 * 
		 * 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken,
		 * config bei Bedarf ausfuellen
		 * 
		 * 2) search: gemaess config die Tabelle mit Suchtext befragen und Ausgabe des
		 * Resultates
		 * 
		 * 3) extract: mit den Keys anhand der config einen Records herausziehen und
		 * anzeigen
		 * 
		 * 4) finish: Config-Kopie sowie Workverzeichnis loeschen
		 */

		/*
		 * TODO: siehe Bemerkung im applicationContext-services.xml bezueglich Injection
		 * in der Superklasse aller Impl-Klassen ValidationModuleImpl
		 * validationModuleImpl = (ValidationModuleImpl)
		 * context.getBean("validationmoduleimpl");
		 */
		ControllerExcExcerpt controllerExcExcerpt = (ControllerExcExcerpt) context.getBean("controllerExcExcerpt");

		Locale locale = Locale.getDefault();

		if (args[2].equalsIgnoreCase("--de")) {
			locale = new Locale("de");
		} else if (args[2].equalsIgnoreCase("--fr")) {
			locale = new Locale("fr");
		} else if (args[2].equalsIgnoreCase("--en")) {
			locale = new Locale("en");
		} else {
			// ungueltige Eingabe Fehler wird ignoriert und default oder de wird
			// angenommen
			if (locale.toString().startsWith("fr")) {
				locale = new Locale("fr");
			} else if (locale.toString().startsWith("en")) {
				locale = new Locale("en");
			} else {
				locale = new Locale("de");
			}
		}

		// Ist die Anzahl Parameter (mind 4) korrekt?
		if (args.length == 4) {
			System.out.println(
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_ERROR_PARAMETER_USAGE));
			return false;
		}

		File siardDatei = new File(args[0]);
		// Map<String, String> configMap =
		// siardexcerpt.getConfigurationService().configMap();

		/*
		 * arg 1 gibt den Pfad zur configdatei an. Da dieser in ConfigurationServiceImpl
		 * hartcodiert ist, wird diese nach
		 * ".siardexcerpt/configuration/SIARDexcerpt.conf.xml" kopiert.
		 */
		String userSIARDexcerpt = System.getenv("USERPROFILE") + File.separator + ".siardexcerpt";
		File userSIARDexcerptFile = new File(userSIARDexcerpt);
		if (!userSIARDexcerptFile.exists()) {
			userSIARDexcerptFile.mkdirs();
		}
		String config = userSIARDexcerpt + File.separator + "configuration";
		File config1 = new File(config);
		if (!config1.exists()) {
			config1.mkdirs();
		}

		String pathToOutput = System.getenv("USERPROFILE") + File.separator + ".siardexcerpt" + File.separator
				+ "Output";
		File directoryOfOutput = new File(pathToOutput);

		String pathToWorkDir = System.getenv("USERPROFILE") + File.separator + ".siardexcerpt" + File.separator
				+ "temp_SIARDexcerpt";
		File tmpDir = new File(pathToWorkDir);

		boolean okC = false;

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty("java.vm.version");
		if (javaRuntimeVersion.compareTo("1.8.0") < 0) {
			System.out.println(controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_ERROR_WRONG_JRE));
			return false;
		}

		System.out.println("");

		/**
		 * 3) extract: mit den Keys anhand der config einen Records herausziehen und
		 * anzeigen
		 * 
		 * a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext
		 * 
		 * b) extract.xml vorbereiten (Header) und xsl in Output kopieren
		 * 
		 * c) extraktion: dies ist in einem eigenen Modul realisiert
		 * 
		 * d) Ausgabe und exitcode
		 */

		System.out.println("SIARDexcerpt: extract");

		Map<String, String> configMap = controllerExcExcerpt.getConfigurationServiceExc().configMap(locale);

		if (pathToWorkDir.startsWith("Configuration-Error:")) {
			System.out.println(pathToWorkDir);
			return false;
		}

		tmpDir = new File(pathToWorkDir);

		directoryOfOutput = new File(pathToOutput);

		if (!directoryOfOutput.exists()) {
			directoryOfOutput.mkdir();
		}

		/** a) Ist die Anzahl Parameter (mind 5) korrekt? arg4 = Schluessel */
		// System.out.println( " a) Ist die Anzahl Parameter (mind 5) korrekt?
		// arg4 = Schluessel" );
		if (args.length < 5) {
			System.out.println(
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_ERROR_PARAMETER_USAGE));
			return false;
		}

		if (!siardDatei.isDirectory()) {
			File siardDateiNew = new File(tmpDir.getAbsolutePath() + File.separator + siardDatei.getName());
			if (!siardDateiNew.exists()) {
				System.out.println(controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_ERROR_NOINIT));
				return false;
			} else {
				siardDatei = siardDateiNew;
			}
		}

		/** b) extract.xml vorbereiten (Header) und xsl in Output kopieren */
		// System.out.println( " b) extract.xml vorbereiten (Header) und xsl in
		// Output kopieren" );
		// Zeitstempel der Datenextraktion
		java.util.Date nowStart = new java.util.Date();
		java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		String ausgabeStart = sdfStart.format(nowStart);

		String excerptString = new String(args[4]);
		/*
		 * Der excerptString kann zeichen enthalten, welche nicht im Dateinamen
		 * vorkommen duerfen. Entsprechend werden diese normalisiert
		 */
		String excerptStringFilename = excerptString.replaceAll("/", "_");
		excerptStringFilename = excerptStringFilename.replaceAll(">", "_");
		excerptStringFilename = excerptStringFilename.replaceAll("<", "_");
		excerptStringFilename = excerptStringFilename.replace("*", "_");
		excerptStringFilename = excerptStringFilename.replace(".*", "_");
		excerptStringFilename = excerptStringFilename.replaceAll("___", "_");
		excerptStringFilename = excerptStringFilename.replaceAll("__", "_");

		excerptString = excerptString.replaceAll("\\*", "\\.");

		String outDateiName = siardDatei.getName() + "_" + excerptStringFilename + "_SIARDexcerpt.xml";
		outDateiName = outDateiName.replaceAll("__", "_");

		// Informationen zum Archiv holen
		String archive = configMap.get("Archive");
		if (archive.startsWith("Configuration-Error:")) {
			System.out.println(archive);
			return false;
		}

		// Konfiguration des Outputs, ein File Logger wird zusaetzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context.getBean("logconfiguratorExc");
		String outFileName = logConfigurator.configure(directoryOfOutput.getAbsolutePath(), outDateiName);
		File outFile = new File(outFileName);
		// Ab hier kann ins Output geschrieben werden...
		/*
		 * BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new
		 * FileOutputStream( new File( outFileName + "_new.xml" ) ), "UTF-8" ) ); try {
		 */
		// Informationen zum XSL holen
		String pathToXSL = configMap.get("PathToXSL");
		if (pathToXSL.startsWith("Configuration-Error:")) {
			System.out.println(pathToXSL);
			return false;
		}

		File xslOrig = new File(pathToXSL);
		File xmlExtracted = new File(
				siardDatei.getAbsolutePath() + File.separator + "header" + File.separator + "metadata.xml");
		File xslCopy;
		if (!xslOrig.exists() || pathToXSL.equals("(.. )")) {
			// XSL AutoGenerating mit resources/SIARDexcerptAutoXSL.txt
			File xsltxt = new File("resources" + File.separator + "SIARDexcerptAutoXSL.txt");
			if (!xsltxt.exists()) {
				System.out.println(controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
						EXC_ERROR_CONFIGFILE_FILENOTEXISTING, xsltxt.getAbsolutePath()));
				return false;
			}
			xslCopy = new File(directoryOfOutput.getAbsolutePath() + File.separator + "SIARDexcerptAutoXSL_"
					+ siardDatei.getName() + ".xsl");
			if (!xslCopy.exists()) {
				Util.copyFile(xsltxt, xslCopy);

				/*
				 * xslCopy mit den Werten befuellen
				 * 
				 * AUTO_XSL_TABLE_START -> Fuer jede Tabelle anlegen {0} ist die Zahl z.B. 1
				 * fuer table1 AUTO_XSL_COLUMN -> Fuer jede Zeile anlegen {0} ist die columnNr
				 * z.B. 3 fur c3 AUTO_XSL_TABLE_END -> am Ende der Tabelle Anlegen. Dann
				 * wiederholen der ersten drei Schritte oder AUTO_XSL_FOOTER zum Abschluss
				 * anlegen
				 */

				// aus xmlExtracted die Tabelle herauslesen

				try {

					InputStream inputStreamConfig = new FileInputStream(xmlExtracted);
					Reader readerConfig = new InputStreamReader(inputStreamConfig, "UTF-8");
					InputSource isConfig = new InputSource(readerConfig);
					isConfig.setEncoding("UTF-8");

					DocumentBuilderFactory dbfConfig = DocumentBuilderFactory.newInstance();
					DocumentBuilder dbConfig = dbfConfig.newDocumentBuilder();
					Document docConfig = dbConfig.parse(isConfig);
					docConfig.getDocumentElement().normalize();
					dbfConfig.setFeature("http://xml.org/sax/features/namespaces", false);

					/*
					 * columns.column werden mit number erweitert, damit die Zuordnung nicht
					 * jedesmal via Zaehler erfolgen muss
					 */
					NodeList nlColumns = docConfig.getElementsByTagName("columns");
					String provEndXSL = "</body></html></xsl:template></xsl:stylesheet>";
					for (int x = 0; x < nlColumns.getLength(); x++) {
						int counterColumn = 0;
						String tableName = "";
						String schemaName = "";
						Node nodeColumns = nlColumns.item(x);
						NodeList nlColumn = nodeColumns.getChildNodes();
						counterColumn = ((nlColumn.getLength() + 1) / 2);
						// counterColumn = Anzahl Column plus 1
						Node nTable = nodeColumns.getParentNode();
						// table

						// Schema name und folder herauslesen
						Node mainTables = nTable.getParentNode();
						Node mainSchema = mainTables.getParentNode();
						NodeList nlSchemaChild = mainSchema.getChildNodes();
						for (int xs = 0; xs < nlSchemaChild.getLength(); xs++) {
							// fuer jedes Subelement der Tabelle (name, folder,
							// description...) ...
							Node subNode = nlSchemaChild.item(xs);
							if (subNode.getNodeName().equals("name")) {
								schemaName = subNode.getTextContent();
							}
						}

						NodeList nlTable = nTable.getChildNodes();
						for (int y = 0; y < nlTable.getLength(); y++) {
							Node subNode = nlTable.item(y);
							if (subNode.getNodeName().equals("name")) {
								tableName = subNode.getTextContent();
								// System.out.println( tableFolder + ": " +
								// (counterColumn-1) + " Spalten." );
								Util.oldnewstring(provEndXSL,
										controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
												EXC_AUTO_XSL_TABLE_START,
												schemaName.replace(" ", "") + "_" + tableName.replace(" ", "")),
										xslCopy);
								for (int z = 1; z < counterColumn; z++) {
									Util.oldnewstring(provEndXSL, controllerExcExcerpt.getTextResourceServiceExc()
											.getText(locale, EXC_AUTO_XSL_COLUMN, z), xslCopy);
								}
								Util.oldnewstring(provEndXSL, controllerExcExcerpt.getTextResourceServiceExc()
										.getText(locale, EXC_AUTO_XSL_TABLE_END), xslCopy);
							}
						}
					}
					Util.oldnewstring(provEndXSL,
							controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_AUTO_XSL_FOOTER),
							xslCopy);
				} catch (Exception e) {
					Logtxt.logtxt(outFile, "<Error>" + controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
							EXC_ERROR_XML_UNKNOWN, e.getMessage()));
					Logtxt.logtxt(outFile,
							controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_LOGEND));
					System.out.println("Exception: " + e.getMessage());
					return false;
				}
			}
		} else {
			// Original Xsl in output kopieren
			xslCopy = new File(directoryOfOutput.getAbsolutePath() + File.separator + xslOrig.getName());
			if (!xslCopy.exists()) {
				Util.copyFile(xslOrig, xslCopy);
			}
		}
		// Information aus metadata holen
		String dbname = "";
		String dataOriginTimespan = "";
		String dbdescription = "";
		String keyexcerpt = "";

		try {

			// Informationen zur Tabelle aus metadata.xml (UTF8) herausholen
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(xmlExtracted));
			doc.getDocumentElement().normalize();
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);

			NodeList nlDbname = doc.getElementsByTagName("dbname");
			Node nodeDbname = nlDbname.item(0);
			dbname = nodeDbname.getTextContent();

			NodeList nlDataOriginTimespan = doc.getElementsByTagName("dataOriginTimespan");
			Node nodeDataOriginTimespan = nlDataOriginTimespan.item(0);
			dataOriginTimespan = nodeDataOriginTimespan.getTextContent();

			NodeList nlSiardArchive = doc.getElementsByTagName("siardArchive");
			Node nodeSiardArchive = nlSiardArchive.item(0);
			NodeList childNodes = nodeSiardArchive.getChildNodes();
			for (int x = 0; x < childNodes.getLength(); x++) {
				Node subNode = childNodes.item(x);
				if (subNode.getNodeName().equals("description")) {
					dbdescription = new String(subNode.getTextContent());
				}
			}

			String primarykeyname = configMap.get("MaintablePrimarykeyName");
			if (primarykeyname.startsWith("Configuration-Error:")) {
				System.out.println(primarykeyname);
				return false;
			}
			keyexcerpt = primarykeyname + " = " + excerptString;

		} catch (Exception e) {
			Logtxt.logtxt(outFile, "<Error>" + controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
					EXC_ERROR_XML_UNKNOWN, e.getMessage()));
			Logtxt.logtxt(outFile,
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_LOGEND));
			System.out.println("Exception: " + e.getMessage());
			return false;
		}

		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_HEADER,
				xslCopy.getName()));
		Logtxt.logtxt(outFile,
				controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_START, ausgabeStart));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
				archive, "Archive"));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
				dbname, "dbname"));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
				dataOriginTimespan, "dataOriginTimespan"));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
				dbdescription, "dbdescription"));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_TEXT,
				keyexcerpt, "keyexcerpt"));
		Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_INFO));

		/** c) extraktion: dies ist in einem eigenen Modul realisiert */
		Controllerexcerpt controllerexcerpt = (Controllerexcerpt) context.getBean("controllerexcerpt");

		okC = controllerexcerpt.executeC(siardDatei, outFile, excerptString, configMap, locale);

		/** d) Ausgabe und exitcode */
		if (!okC) {
			// Record konnte nicht extrahiert werden
			Logtxt.logtxt(outFile,
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_MODUL_C));
			Logtxt.logtxt(outFile, controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
					EXC_ERROR_XML_C_CANNOTEXTRACTRECORD));
			Logtxt.logtxt(outFile,
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_LOGEND));
			System.out.println(controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
					EXC_MESSAGE_C_EXCERPT_NOK, outFileName));

			// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt
			// erst bei schritt 4 finish

			// Fehler Extraktion --> invalide
			return false;
		} else {
			// Record konnte extrahiert werden
			Logtxt.logtxt(outFile,
					controllerExcExcerpt.getTextResourceServiceExc().getText(locale, EXC_MESSAGE_XML_LOGEND));

			// Die Konfiguration hereinkopieren
			/*
			 * try { DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			 * factory.setValidating( false );
			 * 
			 * factory.setExpandEntityReferences( false );
			 * 
			 * Document docConfig = factory.newDocumentBuilder().parse( configFile );
			 * NodeList list = docConfig.getElementsByTagName( "configuration" ); Element
			 * element = (Element) list.item( 0 );
			 * 
			 * Document docLog = factory.newDocumentBuilder().parse( outFile );
			 * 
			 * Node dup = docLog.importNode( element, true );
			 * 
			 * docLog.getDocumentElement().appendChild( dup ); FileWriter writer = new
			 * FileWriter( outFile );
			 * 
			 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); ElementToStream(
			 * docLog.getDocumentElement(), baos ); String stringDoc2 = new String(
			 * baos.toByteArray() ); writer.write( stringDoc2 ); writer.close();
			 * 
			 * // Der Header wird dabei leider verschossen, wieder zurueck aendern String
			 * newstring = controllerExcExcerpt.getTextResourceServiceExc().getText(
			 * MESSAGE_XML_HEADER, xslCopy.getName() ); String oldstring =
			 * "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>"; Util.oldnewstring(
			 * oldstring, newstring, outFile );
			 * 
			 * } catch ( Exception e ) { LOGGER.logError( "<Error>" +
			 * controllerExcExcerpt.getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN,
			 * e.getMessage() ) ); System.out.println( "Exception: " + e.getMessage() ); }
			 */

			// Loeschen des Arbeitsverzeichnisses und configFileHard erfolgt
			// erst bei schritt 4 finish

			// Record konnte extrahiert werden
			System.out.println(controllerExcExcerpt.getTextResourceServiceExc().getText(locale,
					EXC_MESSAGE_C_EXCERPT_OK, outFileName));
			excerpt = true;
			return excerpt;

		}

		// End extract

	}

	public static void ElementToStream(Element element, OutputStream out) {
		try {
			DOMSource source = new DOMSource(element);
			StreamResult result = new StreamResult(out);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform(source, result);
		} catch (Exception ex) {
		}
	}

}
