/* == KOST-Val ==================================================================================
 * The KOST-Val v1.7.6 application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2016 Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.controller.Controllerjp2;
import ch.kostceco.tools.kostval.controller.Controllerjpeg;
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

/** Dies ist die Starter-Klasse, verantwortlich für das Initialisieren des Controllers, des Loggings
 * und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

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

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	/** Die Eingabe besteht aus 2 oder 3 Parameter: [0] Validierungstyp [1] Pfad zur Val-File [2]
	 * option: Verbose
	 * 
	 * @param args
	 * @throws IOException */

	public static void main( String[] args ) throws IOException
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		// Zeitstempel Start
		java.util.Date nowStart = new java.util.Date();
		java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
		String ausgabeStart = sdfStart.format( nowStart );

		/* TODO: siehe Bemerkung im applicationContext-services.xml bezüglich Injection in der
		 * Superklasse aller Impl-Klassen ValidationModuleImpl validationModuleImpl =
		 * (ValidationModuleImpl) context.getBean("validationmoduleimpl"); */

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );
		File configFile = new File( "configuration" + File.separator + "kostval.conf.xml" );

		// Ueberprüfung des Parameters (Log-Verzeichnis)
		String pathToLogfile = kostval.getConfigurationService().getPathToLogfile();
		if ( pathToLogfile.startsWith( "Configuration-Error:" ) ) {
			System.out.println( pathToLogfile );
			System.exit( 1 );
		}
		File directoryOfLogfile = new File( pathToLogfile );

		if ( !directoryOfLogfile.exists() ) {
			directoryOfLogfile.mkdir();
		}

		// Im Logverzeichnis besteht kein Schreibrecht
		if ( !directoryOfLogfile.canWrite() ) {
			System.out.println( kostval.getTextResourceService().getText( ERROR_LOGDIRECTORY_NOTWRITABLE,
					directoryOfLogfile ) );
			System.exit( 1 );
		}

		if ( !directoryOfLogfile.isDirectory() ) {
			System.out
					.println( kostval.getTextResourceService().getText( ERROR_LOGDIRECTORY_NODIRECTORY ) );
			System.exit( 1 );
		}
		File pdftronReport = new File( pathToLogfile, "PDFTron.xml" );
		File pdftronReportXsl = new File( pathToLogfile, "report.xsl");
		

		if ( pdftronReport.exists() ) {
			Util.deleteFile( pdftronReport );
		}

		// Ist die Anzahl Parameter (mind. 2) korrekt?
		if ( args.length < 2 ) {
			System.out.println( kostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		File valDatei = new File( args[1] );
		File logDatei = null;
		logDatei = valDatei;
		File logDirValDatei = new File( pathToLogfile, valDatei.getName()  );

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = kostval.getConfigurationService().getPathToWorkDir();
		if ( pathToWorkDir.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ pathToWorkDir );
			System.exit( 1 );
		}
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidation = kostval.getConfigurationService().pdfaValidation();
		String siardValidation = kostval.getConfigurationService().siardValidation();
		String tiffValidation = kostval.getConfigurationService().tiffValidation();
		String jp2Validation = kostval.getConfigurationService().jp2Validation();
		String jpegValidation = kostval.getConfigurationService().jpegValidation();
		if ( pdfaValidation.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ pdfaValidation );
			System.exit( 1 );
		}
		if ( siardValidation.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ siardValidation );
			System.exit( 1 );
		}
		if ( tiffValidation.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ tiffValidation );
			System.exit( 1 );
		}
		if ( jp2Validation.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ jp2Validation );
			System.exit( 1 );
		}
		if ( jpegValidation.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ jpegValidation );
			System.exit( 1 );
		}

		// Konfiguration des Loggings, ein File Logger wird zusätzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context.getBean( "logconfigurator" );
		String logFileName = logConfigurator.configure( directoryOfLogfile.getAbsolutePath(),
				logDatei.getName() );
		File logFile = new File( logFileName );
		// Ab hier kann ins log geschrieben werden...

		// ggf alte SIP-Validierung-Versions-Notiz löschen
		File BAR1 = new File( directoryOfLogfile.getAbsolutePath() + File.separator + "BAR1.txt" );
		File ECH1_1 = new File( directoryOfLogfile.getAbsolutePath() + File.separator + "ECH1.1.txt" );
		File ECH1_0 = new File( directoryOfLogfile.getAbsolutePath() + File.separator + "ECH1.0.txt" );
		if ( BAR1.exists() ) {
			Util.deleteFile( BAR1 );
		} else if ( ECH1_1.exists() ) {
			Util.deleteFile( ECH1_1 );
		} else if ( ECH1_0.exists() ) {
			Util.deleteFile( ECH1_0 );
		}

		String formatValOn = "";
		// ermitteln welche Formate validiert werden können respektive eingeschaltet sind
		if ( pdfaValidation.equals( "yes" ) ) {
			formatValOn = "PDF/A";
			if ( tiffValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", TIFF";
			}
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( tiffValidation.equals( "yes" ) ) {
			formatValOn = "TIFF";
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jp2Validation.equals( "yes" ) ) {
			formatValOn = "JP2";
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( siardValidation.equals( "yes" ) ) {
			formatValOn = "SIARD";
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jpegValidation.equals( "yes" ) ) {
			formatValOn = "JPEG";
		}

		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_HEADER ) );
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_START, ausgabeStart ) );
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_END ) );
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMATON, formatValOn ) );
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_INFO ) );
		System.out.println( "KOST-Val" );

		if ( args[0].equals( "--format" ) && formatValOn.equals( "" ) ) {
			// Formatvalidierung aber alle Formate ausgeschlossen
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_NOFILEENDINGS ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_NOFILEENDINGS ) );
			System.exit( 1 );
		}

		File xslOrig = new File( "resources" + File.separator + "kost-val.xsl" );
		File xslCopy = new File( directoryOfLogfile.getAbsolutePath() + File.separator + "kost-val.xsl" );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		File tmpDir = new File( pathToWorkDir );

		/* bestehendes Workverzeichnis Abbruch wenn nicht leer, da am Schluss das Workverzeichnis
		 * gelöscht wird und entsprechend bestehende Dateien gelöscht werden können */
		if ( tmpDir.exists() ) {
			if ( tmpDir.isDirectory() ) {
				// Get list of file in the directory. When its length is not zero the folder is not empty.
				String[] files = tmpDir.list();
				if ( files.length > 0 ) {
					LOGGER.logError( kostval.getTextResourceService()
							.getText(
									ERROR_IOE,
									kostval.getTextResourceService().getText( ERROR_WORKDIRECTORY_EXISTS,
											pathToWorkDir ) ) );
					System.out.println( kostval.getTextResourceService().getText( ERROR_WORKDIRECTORY_EXISTS,
							pathToWorkDir ) );
					System.exit( 1 );
				}
			}
		}

		// Im Pfad keine Sonderzeichen xml-Validierung SIP 1d und SIARD C stürzen ab

		String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~:\\\\a-zA-Z0-9 ]";
		Pattern pattern = Pattern.compile( patternStr );

		String name = tmpDir.getAbsolutePath();

		String[] pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name ) ) );
				System.out.println( kostval.getTextResourceService()
						.getText( ERROR_SPECIAL_CHARACTER, name ) );
				System.exit( 1 );
			}
		}

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_WRONG_JRE ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_WRONG_JRE ) );
			System.exit( 1 );
		}

		// bestehendes Workverzeichnis wieder anlegen
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		// Im workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) ) );
			System.out.println( kostval.getTextResourceService().getText(
					ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) );
			System.exit( 1 );
		}

		/* Vorberitung für eine allfällige Festhaltung bei unterschiedlichen PDFA-Validierungsresultaten
		 * in einer PDF_Diagnosedatei sowie Zähler der SIP-Dateiformate */
		String diaPath = kostval.getConfigurationService().getPathToDiagnose();
		if ( diaPath.startsWith( "Configuration-Error:" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE ) + diaPath );
			System.out.println( diaPath );
			System.exit( 1 );
		}

		// Im diaverzeichnis besteht kein Schreibrecht
		File diaDir = new File( diaPath );

		if ( !diaDir.exists() ) {
			diaDir.mkdir();
		}

		if ( !diaDir.canWrite() ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_DIADIRECTORY_NOTWRITABLE, diaDir ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_DIADIRECTORY_NOTWRITABLE,
					diaDir ) );
			System.exit( 1 );
		}

		File xmlDiaOrig = new File( "resources" + File.separator + "KaD-Diagnosedaten.kost-val.xml" );
		File xmlDiaCopy = new File( diaPath + File.separator + "KaD-Diagnosedaten.kost-val.xml" );
		if ( !xmlDiaCopy.exists() ) {
			Util.copyFile( xmlDiaOrig, xmlDiaCopy );
		}
		File xslDiaOrig = new File( "resources" + File.separator + "kost-val_KaDdia.xsl" );
		File xslDiaCopy = new File( diaPath + File.separator + "kost-val_KaDdia.xsl" );
		if ( !xslDiaCopy.exists() ) {
			Util.copyFile( xslDiaOrig, xslDiaCopy );
		}

		/* Ueberprüfung des optionalen Parameters (2 -v --> im Verbose-mode werden die originalen Logs
		 * nicht gelöscht (PDFTron, Jhove & Co.) */
		boolean verbose = false;
		if ( args.length > 2 ) {
			if ( !(args[2].equals( "-v" )) ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_PARAMETER_OPTIONAL_1 ) ) );
				System.out.println( kostval.getTextResourceService().getText( ERROR_PARAMETER_OPTIONAL_1 ) );
				System.exit( 1 );
			} else {
				verbose = true;
			}
		}

		/* Initialisierung TIFF-Modul B (JHove-Validierung) überprüfen der Konfiguration: existiert die
		 * jhove.conf am angebenen Ort? */
		String jhoveConf = kostval.getConfigurationService().getPathToJhoveConfiguration();
		if ( diaPath.startsWith( "Configuration-Error:" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ diaPath );
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ diaPath );
			System.exit( 1 );
		}
		File fJhoveConf = new File( jhoveConf );
		if ( !fJhoveConf.exists() || !fJhoveConf.getName().equals( "jhove.conf" ) ) {

			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_JHOVECONF_MISSING ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_JHOVECONF_MISSING ) );
			System.exit( 1 );
		}

		// Im Pfad keine Sonderzeichen xml-Validierung SIP 1d und SIARD C stürzen ab

		name = valDatei.getAbsolutePath();

		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name ) ) );
				System.out.println( kostval.getTextResourceService()
						.getText( ERROR_SPECIAL_CHARACTER, name ) );
				System.exit( 1 );
			}
		}

		// Ueberprüfung des Parameters (Val-Datei): existiert die Datei?
		if ( !valDatei.exists() ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_VALFILE_FILENOTEXISTING ) ) );
			System.out
					.println( kostval.getTextResourceService().getText( ERROR_VALFILE_FILENOTEXISTING ) );
			System.exit( 1 );
		}

		if ( args[0].equals( "--format" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT1 ) );
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
			Integer jp2CountIo = 0;
			Integer jp2CountNio = 0;
			Integer jpegCountIo = 0;
			Integer jpegCountNio = 0;

			// TODO: Formatvalidierung an einer Datei --> erledigt --> nur Marker
			if ( !valDatei.isDirectory() ) {

				boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );

				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				// Zeitstempel End
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );
				ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( ausgabeEnd, "", logFile );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFile );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( logFile );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( logFile );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zurück ändern
					String newstring = kostval.getTextResourceService().getText( MESSAGE_XML_HEADER );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTValLog>";
					Util.oldnewstring( oldstring, newstring, logFile );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
				}

				if ( valFile ) {
					// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// Validierte Datei valide
					System.exit( 0 );
				} else {
					// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// Fehler in Validierte Datei --> invalide
					System.exit( 2 );

				}
			} else {
				// TODO: Formatvalidierung über ein Ordner --> erledigt --> nur Marker
				try {
					/* PDFTron über content durchführen. Performance optimierung */
					String firstValidator = kostval.getConfigurationService().firstValidator();
					if ( firstValidator.startsWith( "Configuration-Error:" ) ) {
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
								+ firstValidator );
						System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
								+ firstValidator );
						System.exit( 1 );
					}

					if ( firstValidator.equalsIgnoreCase( "PDFTron" ) ) {
						Controllerpdfa controller3 = (Controllerpdfa) context.getBean( "controllerpdfa" );
						@SuppressWarnings("unused")
						boolean okPdftron = controller3.executePdftron( valDatei, directoryOfLogfile );
					}

					Map<String, File> fileMap = Util.getFileMap( valDatei, false );
					Set<String> fileMapKeys = fileMap.keySet();
					for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
						String entryName = iterator.next();
						File newFile = fileMap.get( entryName );
						if ( !newFile.isDirectory() ) {
							valDatei = newFile;
							String valDateiName = valDatei.getName();
							String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
							count = count + 1;

							if ( ((valDateiExt.equals( ".jp2" ))) && jp2Validation.equals( "yes" ) ) {

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

								// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( valFile ) {
									jp2CountIo = jp2CountIo + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								} else {
									jp2CountNio = jp2CountNio + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								}
							} else if ( ((valDateiExt.equals( ".jpeg" ) || valDateiExt.equals( ".jpg" ) || valDatei
									.getAbsolutePath().toLowerCase().endsWith( ".jpe" )))
									&& jpegValidation.equals( "yes" ) ) {

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

								// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( valFile ) {
									jpegCountIo = jpegCountIo + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								} else {
									jpegCountNio = jpegCountNio + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								}
							} else if ( ((valDateiExt.equals( ".tiff" ) || valDatei.getAbsolutePath()
									.toLowerCase().endsWith( ".tif" )))
									&& tiffValidation.equals( "yes" ) ) {

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

								// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( valFile ) {
									tiffCountIo = tiffCountIo + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								} else {
									tiffCountNio = tiffCountNio + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								}
							} else if ( (valDateiExt.equals( ".siard" )) && siardValidation.equals( "yes" ) ) {

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

								// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( valFile ) {
									siardCountIo = siardCountIo + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								} else {
									siardCountNio = siardCountNio + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								}

							} else if ( ((valDateiExt.equals( ".pdf" ) || valDatei.getAbsolutePath()
									.toLowerCase().endsWith( ".pdfa" )))
									&& pdfaValidation.equals( "yes" ) ) {

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );

								// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( valFile ) {
									pdfaCountIo = pdfaCountIo + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								} else {
									pdfaCountNio = pdfaCountNio + 1;
									// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
									if ( tmpDir.exists() ) {
										Util.deleteDir( tmpDir );
									}
								}
								if ( tmpDir.exists() ) {
									tmpDir.deleteOnExit();
								}
							} else {
								countNio = countNio + 1;
							}
						}
					}
				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() )
							+ kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 )
							+ kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
					System.out.println( "Exception: " + e.getMessage() );
				} catch ( StackOverflowError eso ) {
					LOGGER
							.logError( "<Validation><ValType>StackOverflowError</ValType><Invalid>invalid</Invalid></Validation></Format></KOSTValLog>" );
					System.out.println( "Exception: " + "StackOverflowError" );
				} catch ( OutOfMemoryError eoom ) {
					LOGGER
							.logError( "<Validation><ValType>OutOfMemoryError</ValType><Invalid>invalid</Invalid></Validation></Format></KOSTValLog>" );
					System.out.println( "Exception: " + "OutOfMemoryError" );
				}

				System.out.print( kostval.getTextResourceService().getText( MESSAGE_XML_LOG ) );
				System.out.print( "\r" );

				if ( countNio.equals( count ) ) {
					// keine Dateien Validiert
					LOGGER.logError( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS,
							formatValOn ) );
					System.out.println( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS,
							formatValOn ) );
				}

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				// Zeitstempel End
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );
				ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( ausgabeEnd, "", logFile );

				countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio + jp2CountNio + jpegCountNio;
				countSummaryIo = pdfaCountIo + siardCountIo + tiffCountIo + jp2CountIo + jpegCountIo;

				/* Summary über Formatvaliderung herausgeben analog 3c.
				 * 
				 * message.xml.summary.3c = <Message>Von den {0} Dateien sind {1} ({4}%) valid, {2} ({5}%)
				 * invalid und {3} ({6}%) wurden nicht validiert.</Message></Error>
				 * 
				 * countNio=3 count=0 countSummaryIo=1 countSummaryNio=2
				 * 
				 * diese meldung einfach noch mit info einfassen. */

				float countSummaryIoP = 100 / (float) count * (float) countSummaryIo;
				float countSummaryNioP = 100 / (float) count * (float) countSummaryNio;
				float countNioP = 100 / (float) count * (float) countNio;
				String summaryFormat = kostval.getTextResourceService().getText(
						MESSAGE_XML_SUMMARY_FORMAT, count, countSummaryIo, countSummaryNio, countNio,
						countSummaryIoP, countSummaryNioP, countNioP );
				String newFormat = "<Format><Info><Message>" + summaryFormat + "</Message></Info>";
				Util.oldnewstring( "<Format>", newFormat, logFile );

				System.out.println( kostval.getTextResourceService().getText(
						MESSAGE_FORMATVALIDATION_DONE, summaryFormat, logFile.getAbsolutePath() ) );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFile );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( logFile );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( logFile );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zurück ändern
					String newstring = kostval.getTextResourceService().getText( MESSAGE_XML_HEADER );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTValLog>";
					Util.oldnewstring( oldstring, newstring, logFile );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
				}
				System.out.print( "                                                                    " );
				System.out.print( "\r" );

				Util.deleteFile( pdftronReport );
				Util.deleteFile( pdftronReportXsl );
				Util.deleteDir( logDirValDatei );

				if ( countNio.equals( count ) ) {
					// keine Dateien Validiert bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
					}
					System.exit( 1 );
				} else if ( countSummaryNio == 0 ) {
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
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
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
					tmpDir.deleteOnExit();
				}
			}
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );

		} else if ( args[0].equals( "--sip" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT1 ) );

			// TODO: Sipvalidierung --> erledigt --> nur Marker

			/* TODO: PDF-Validierung über den GANZEN Ordner wenn der Hauptvalidator PDFTron ist. Dies
			 * bringt eine extreme Performanceoptimierung!
			 * 
			 * Dieser Report analysieren. Sollte Datei nicht dort sein noch separat anstossen */
			try {
				boolean validFormat = false;
				File originalSipFile = valDatei;
				File unSipFile = valDatei;
				File outputFile3c = null;
				String fileName3c = null;
				File tmpDirZip = null;
				String valDateiName = valDatei.getName();
				String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();

				// zuerst eine Formatvalidierung über den Content dies ist analog aufgebaut wie --format
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
				Integer jp2CountIo = 0;
				Integer jp2CountNio = 0;
				Integer jpegCountIo = 0;
				Integer jpegCountNio = 0;

				if ( !valDatei.isDirectory() ) {
					Boolean zip = false;
					// Eine ZIP Datei muss mit PK.. beginnen
					if ( (valDateiExt.equals( ".zip" ) || valDatei.getAbsolutePath().toLowerCase()
							.endsWith( ".zip64" )) ) {

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

							// die beiden charArrays (soll und ist) mit einander vergleichen
							char[] charArray1 = buffer;
							char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

							if ( Arrays.equals( charArray1, charArray2 ) ) {
								// höchstwahrscheinlich ein ZIP da es mit 504B0304 respektive PK.. beginnt
								zip = true;
							}
							read.close();
						} catch ( Exception e ) {
							LOGGER.logError( "<Error>"
									+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
							System.out.println( "Exception: " + e.getMessage() );
						}
					}

					// wenn die Datei kein Directory ist, muss sie mit zip oder zip64 enden
					if ( (!(valDateiExt.equals( ".zip" ) || valDatei.getAbsolutePath().toLowerCase()
							.endsWith( ".zip64" )))
							|| zip == false ) {
						// Abbruch! D.h. Sip message beginnen, Meldung und Beenden ab hier bis System.exit( 1 );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP1 ) );
						valDatei = originalSipFile;
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
								kostval.getTextResourceService().getText( MESSAGE_SIPVALIDATION ) ) );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
								valDatei.getAbsolutePath() ) );
						System.out.println( "" );
						System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

						// die eigentliche Fehlermeldung
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Aa_SIP )
								+ kostval.getTextResourceService().getText( ERROR_XML_AA_INCORRECTFILEENDING ) );
						System.out.println( kostval.getTextResourceService().getText(
								ERROR_XML_AA_INCORRECTFILEENDING ) );

						// Fehler im Validierten SIP --> invalide & Abbruch
						LOGGER.logError( kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_INVALID ) );
						LOGGER.logError( kostval.getTextResourceService().getText(
								MESSAGE_XML_VALERGEBNIS_CLOSE ) );
						System.out.println( "Invalid" );
						System.out.println( "" );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
						LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );

						// Zeitstempel End
						java.util.Date nowEnd = new java.util.Date();
						java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
								"dd.MM.yyyy HH:mm:ss" );
						String ausgabeEnd = sdfEnd.format( nowEnd );
						ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
						// logFile bereinigung (& End und ggf 3c)
						Util.valEnd3cAmp( ausgabeEnd, "", logFile );

						// Die Konfiguration hereinkopieren
						try {
							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							factory.setValidating( false );

							factory.setExpandEntityReferences( false );

							Document docConfig = factory.newDocumentBuilder().parse( configFile );
							NodeList list = docConfig.getElementsByTagName( "configuration" );
							Element element = (Element) list.item( 0 );

							Document docLog = factory.newDocumentBuilder().parse( logFile );

							Node dup = docLog.importNode( element, true );

							docLog.getDocumentElement().appendChild( dup );
							FileWriter writer = new FileWriter( logFile );

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ElementToStream( docLog.getDocumentElement(), baos );
							String stringDoc2 = new String( baos.toByteArray() );
							writer.write( stringDoc2 );
							writer.close();

							// Der Header wird dabei leider verschossen, wieder zurück ändern
							String newstring = kostval.getTextResourceService().getText( MESSAGE_XML_HEADER );
							String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTValLog>";
							Util.oldnewstring( oldstring, newstring, logFile );

						} catch ( Exception e ) {
							LOGGER.logError( "<Error>"
									+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
							System.out.println( "Exception: " + e.getMessage() );
						}

						// bestehendes Workverzeichnis ggf. löschen
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( tmpDir.exists() ) {
							tmpDir.deleteOnExit();
						}
						System.exit( 1 );

					} else {
						// geziptes SIP --> in temp dir entzipen
						String toplevelDir = valDatei.getName();
						int lastDotIdx = toplevelDir.lastIndexOf( "." );
						toplevelDir = toplevelDir.substring( 0, lastDotIdx );
						tmpDirZip = new File( tmpDir.getAbsolutePath() + File.separator + "ZIP"
								+ File.separator + toplevelDir );
						try {
							Zip64Archiver.unzip( valDatei.getAbsolutePath(), tmpDirZip.getAbsolutePath() );
						} catch ( Exception e ) {
							try {
								Zip64Archiver.unzip64( valDatei, tmpDirZip );
							} catch ( Exception e1 ) {
								// Abbruch! D.h. Sip message beginnen, Meldung und Beenden ab hier bis System.exit
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP1 ) );
								valDatei = originalSipFile;
								LOGGER
										.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
										kostval.getTextResourceService().getText( MESSAGE_SIPVALIDATION ) ) );
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
										valDatei.getAbsolutePath() ) );
								System.out.println( "" );
								System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

								// die eigentliche Fehlermeldung
								LOGGER.logError( kostval.getTextResourceService()
										.getText( MESSAGE_XML_MODUL_Aa_SIP )
										+ kostval.getTextResourceService().getText( ERROR_XML_AA_CANNOTEXTRACTZIP ) );
								System.out.println( kostval.getTextResourceService().getText(
										ERROR_XML_AA_CANNOTEXTRACTZIP ) );

								// Fehler im Validierten SIP --> invalide & Abbruch
								LOGGER.logError( kostval.getTextResourceService().getText(
										MESSAGE_XML_VALERGEBNIS_INVALID ) );
								LOGGER.logError( kostval.getTextResourceService().getText(
										MESSAGE_XML_VALERGEBNIS_CLOSE ) );
								System.out.println( "Invalid" );
								System.out.println( "" );
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
								LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );

								// Zeitstempel End
								java.util.Date nowEnd = new java.util.Date();
								java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat(
										"dd.MM.yyyy HH:mm:ss" );
								String ausgabeEnd = sdfEnd.format( nowEnd );
								ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
								// logFile bereinigung (& End und ggf 3c)
								Util.valEnd3cAmp( ausgabeEnd, "", logFile );

								// Die Konfiguration hereinkopieren
								try {
									DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
									factory.setValidating( false );

									factory.setExpandEntityReferences( false );

									Document docConfig = factory.newDocumentBuilder().parse( configFile );
									NodeList list = docConfig.getElementsByTagName( "configuration" );
									Element element = (Element) list.item( 0 );

									Document docLog = factory.newDocumentBuilder().parse( logFile );

									Node dup = docLog.importNode( element, true );

									docLog.getDocumentElement().appendChild( dup );
									FileWriter writer = new FileWriter( logFile );

									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ElementToStream( docLog.getDocumentElement(), baos );
									String stringDoc2 = new String( baos.toByteArray() );
									writer.write( stringDoc2 );
									writer.close();

									// Der Header wird dabei leider verschossen, wieder zurück ändern
									String newstring = kostval.getTextResourceService().getText( MESSAGE_XML_HEADER );
									String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTValLog>";
									Util.oldnewstring( oldstring, newstring, logFile );

								} catch ( Exception e2 ) {
									LOGGER.logError( "<Error>"
											+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN,
													e2.getMessage() ) );
									System.out.println( "Exception: " + e2.getMessage() );
								}

								// bestehendes Workverzeichnis ggf. löschen
								if ( tmpDir.exists() ) {
									Util.deleteDir( tmpDir );
								}
								if ( tmpDir.exists() ) {
									tmpDir.deleteOnExit();
								}
								System.exit( 1 );
							}
						}
						valDatei = tmpDirZip;

						File toplevelfolder = new File( valDatei.getAbsolutePath() + File.separator
								+ valDatei.getName() );
						if ( toplevelfolder.exists() ) {
							valDatei = toplevelfolder;
						}
						unSipFile = valDatei;
						valDateiName = valDatei.getName();
						valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
					}
				} else {
					// SIP ist ein Ordner valDatei bleibt unverändert
				}

				// Vorgängige Formatvalidierung (Schritt 3c)
				Map<String, File> fileMap = Util.getFileMap( valDatei, false );
				Set<String> fileMapKeys = fileMap.keySet();

				int pdf = 0;
				int tiff = 0;
				int siard = 0;
				int txt = 0;
				int csv = 0;
				int xml = 0;
				int xsd = 0;
				int wave = 0;
				int mp3 = 0;
				int jp2 = 0;
				int jpx = 0;
				int jpeg = 0;
				int png = 0;
				int dng = 0;
				int svg = 0;
				int mpeg2 = 0;
				int mp4 = 0;
				int xls = 0;
				int odt = 0;
				int ods = 0;
				int odp = 0;
				int other = 0;
				
				/* PDFTron über content durchführen. Performance optimierung */
				String firstValidator = kostval.getConfigurationService().firstValidator();
				if ( firstValidator.startsWith( "Configuration-Error:" ) ) {
					LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
							+ firstValidator );
					System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
							+ firstValidator );
					System.exit( 1 );
				}

				if ( firstValidator.equalsIgnoreCase( "PDFTron" ) ) {
					Controllerpdfa controller3 = (Controllerpdfa) context.getBean( "controllerpdfa" );
					@SuppressWarnings("unused")
					boolean okPdftron = controller3.executePdftron( valDatei, directoryOfLogfile );
				}	

				for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
					String entryName = iterator.next();
					File newFile = fileMap.get( entryName );

					if ( !newFile.isDirectory()
							&& newFile.getAbsolutePath().contains( File.separator + "content" + File.separator ) ) {
						valDatei = newFile;
						valDateiName = valDatei.getName();
						valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
						count = count + 1;

						/* String extension = valDatei.getName(); int lastIndexOf = extension.lastIndexOf( "."
						 * ); if ( lastIndexOf == -1 ) { // empty extension extension = "other"; } else {
						 * extension = extension.substring( lastIndexOf ); } */

						if ( valDateiExt.equals( ".pdf" ) || valDateiExt.equals( ".pdfa" ) ) {
							pdf = pdf + 1;
							if ( pdfaValidation.equals( "yes" ) ) {
								// Validierung durchführen
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );
								if ( valFile ) {
									pdfaCountIo = pdfaCountIo + 1;
								} else {
									pdfaCountNio = pdfaCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
							}
						} else if ( valDateiExt.equals( ".tiff" ) || valDateiExt.equals( ".tif" ) ) {
							tiff = tiff + 1;
							if ( tiffValidation.equals( "yes" ) ) {
								// Validierung durchführen
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );
								if ( valFile ) {
									tiffCountIo = tiffCountIo + 1;
								} else {
									tiffCountNio = tiffCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
							}
						} else if ( valDateiExt.equals( ".siard" ) ) {
							siard = siard + 1;
							if ( siardValidation.equals( "yes" ) ) {
								// Validierung durchführen

								// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
								String pathToWorkDirSiard = kostval.getConfigurationService().getPathToWorkDir();
								File tmpDirSiard = new File( pathToWorkDirSiard + File.separator + "SIARD" );
								if ( tmpDirSiard.exists() ) {
									Util.deleteDir( tmpDirSiard );
								}
								if ( !tmpDirSiard.exists() ) {
									tmpDirSiard.mkdir();
								}
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );
								if ( valFile ) {
									siardCountIo = siardCountIo + 1;
								} else {
									siardCountNio = siardCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
							}
						} else if ( valDateiExt.equals( ".jpe" ) || valDateiExt.equals( ".jpeg" )
								|| valDateiExt.equals( ".jpg" ) ) {
							jpeg = jpeg + 1;
							if ( jpegValidation.equals( "yes" ) ) {
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );
								if ( valFile ) {
									jpegCountIo = jpegCountIo + 1;
								} else {
									jpegCountNio = jpegCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
							}
						} else if ( valDateiExt.equals( ".jp2" ) ) {
							jp2 = jp2 + 1;
							if ( jp2Validation.equals( "yes" ) ) {
								// Validierung durchführen
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose );
								if ( valFile ) {
									jp2CountIo = jp2CountIo + 1;
								} else {
									jp2CountNio = jp2CountNio + 1;
								}
							} else {
								countNio = countNio + 1;
							}
						} else if ( valDateiExt.equals( ".txt" ) ) {
							txt = txt + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".csv" ) ) {
							csv = csv + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".xml" ) ) {
							xml = xml + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".xsd" ) ) {
							xsd = xsd + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".wav" ) ) {
							wave = wave + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".mp3" ) ) {
							mp3 = mp3 + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".jpx" ) || valDateiExt.equals( ".jpf" ) ) {
							jpx = jpx + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".png" ) ) {
							png = png + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".dng" ) ) {
							dng = dng + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".svg" ) ) {
							svg = svg + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".mpeg" ) || valDateiExt.equals( ".mpg" ) ) {
							mpeg2 = mpeg2 + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".f4a" ) || valDateiExt.equals( ".f4v" )
								|| valDateiExt.equals( ".m4a" ) || valDateiExt.equals( ".m4v" )
								|| valDateiExt.equals( ".mp4" ) ) {
							mp4 = mp4 + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".xls" ) || valDateiExt.equals( ".xlw" )
								|| valDateiExt.equals( ".xlsx" ) ) {
							xls = xls + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".odt" ) || valDateiExt.equals( ".ott" ) ) {
							odt = odt + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".ods" ) || valDateiExt.equals( ".ots" ) ) {
							ods = ods + 1;
							countNio = countNio + 1;
						} else if ( valDateiExt.equals( ".odp" ) || valDateiExt.equals( ".otp" ) ) {
							odp = odp + 1;
							countNio = countNio + 1;
						} else {
							other = other + 1;
							countNio = countNio + 1;
						}

					}
				}

				countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio + jp2CountNio + jpegCountNio;
				countSummaryIo = pdfaCountIo + siardCountIo + tiffCountIo + jp2CountIo + jpegCountIo;
				float countSummaryIoP = 100 / (float) count * (float) countSummaryIo;
				float countSummaryNioP = 100 / (float) count * (float) countSummaryNio;
				float countNioP = 100 / (float) count * (float) countNio;
				String summary3c = kostval.getTextResourceService()
						.getText( MESSAGE_XML_SUMMARY_3C, count, countSummaryIo, countSummaryNio, countNio,
								countSummaryIoP, countSummaryNioP, countNioP );

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
				outputFile3c = new File( pathToWorkDir + File.separator + fileName3c );
				try {
					outputFile3c.createNewFile();
				} catch ( IOException e ) {
					e.printStackTrace();
				}

				if ( countNio == count ) {
					// keine Dateien Validiert
					LOGGER.logError( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS,
							formatValOn ) );
					System.out.println( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDINGS,
							formatValOn ) );
				}

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );

				// Start Normale SIP-Validierung mit auswertung Format-Val. im 3c

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP1 ) );
				valDatei = unSipFile;
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
						kostval.getTextResourceService().getText( MESSAGE_SIPVALIDATION ) ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
						originalSipFile.getAbsolutePath() ) );
				System.out.println( "" );
				System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

				Controllersip controller = (Controllersip) context.getBean( "controllersip" );
				boolean okMandatory = false;
				okMandatory = controller.executeMandatory( valDatei, directoryOfLogfile );
				boolean ok = false;

				/* die Validierungen 1a - 1d sind obligatorisch, wenn sie bestanden wurden, können die
				 * restlichen Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt
				 * werden.
				 * 
				 * 1a wurde bereits getestet (vor der Formatvalidierung entsprechend fängt der Controller
				 * mit 1b an */
				if ( okMandatory ) {
					ok = controller.executeOptional( valDatei, directoryOfLogfile );
				}
				// Formatvalidierung validFormat
				ok = (ok && okMandatory && validFormat);

				if ( ok ) {
					// Validiertes SIP valide
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					System.out.println( "Valid" );
					System.out.println( "" );
				} else {
					// Fehler im Validierten SIP --> invalide
					LOGGER.logError( kostval.getTextResourceService().getText(
							MESSAGE_XML_VALERGEBNIS_INVALID ) );
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					System.out.println( "Invalid" );
					System.out.println( "" );

				}

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				// Zeitstempel End
				java.util.Date nowEnd = new java.util.Date();
				java.text.SimpleDateFormat sdfEnd = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
				String ausgabeEnd = sdfEnd.format( nowEnd );
				ausgabeEnd = "<End>" + ausgabeEnd + "</End>";
				// ggf. Fehlermeldung 3c ergänzen Util.val3c(summary3c, logFile );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( ausgabeEnd, summary3c, logFile );

				// Ergänzen welche SIP-Validierung durchgeführt wurde
				String sipVersion = " ";
				if ( BAR1.exists() ) {
					sipVersion = " (BARv1)";
					Util.deleteFile( BAR1 );
				} else if ( ECH1_1.exists() ) {
					sipVersion = " (eCH-0160v1.1)";
					Util.deleteFile( ECH1_1 );
				} else if ( ECH1_0.exists() ) {
					sipVersion = " (eCH-0160v1.0)";
					Util.deleteFile( ECH1_0 );
				}
				Util.valSipversion( sipVersion, logFile );

				System.out.print( kostval.getTextResourceService().getText( MESSAGE_XML_LOG ) );
				System.out.print( "\r" );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFile );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( logFile );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( logFile );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zurück ändern
					String newstring = kostval.getTextResourceService().getText( MESSAGE_XML_HEADER );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KOSTValLog>";
					Util.oldnewstring( oldstring, newstring, logFile );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
				}

				// bestehendes Workverzeichnis ggf. löschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( tmpDir.exists() ) {
					tmpDir.deleteOnExit();
				}

				try {
					// KaD-Diagnose-Datei mit den neusten Anzahl Dateien pro KaD-Format Updaten
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse( xmlDiaCopy );

					doc.getDocumentElement().normalize();

					NodeList nList = doc.getElementsByTagName( "KOSTVal_FFCounter" );

					for ( int temp = 0; temp < nList.getLength(); temp++ ) {

						Node nNode = nList.item( temp );

						if ( nNode.getNodeType() == Node.ELEMENT_NODE ) {
							Element eElement = (Element) nNode;

							if ( pdf > 0 ) {
								String pdfNodeString = eElement.getElementsByTagName( "pdf" ).item( 0 )
										.getTextContent();
								int pdfNodeValue = Integer.parseInt( pdfNodeString );
								pdf = pdf + pdfNodeValue;
								Util.kadDia( "<pdf>" + pdfNodeValue + "</pdf>", "<pdf>" + pdf + "</pdf>",
										xmlDiaCopy );
							}
							if ( tiff > 0 ) {
								String tiffNodeString = eElement.getElementsByTagName( "tiff" ).item( 0 )
										.getTextContent();
								int tiffNodeValue = Integer.parseInt( tiffNodeString );
								tiff = tiff + tiffNodeValue;
								Util.kadDia( "<tiff>" + tiffNodeValue + "</tiff>", "<tiff>" + tiff + "</tiff>",
										xmlDiaCopy );
							}
							if ( siard > 0 ) {
								String siardNodeString = eElement.getElementsByTagName( "siard" ).item( 0 )
										.getTextContent();
								int siardNodeValue = Integer.parseInt( siardNodeString );
								siard = siard + siardNodeValue;
								Util.kadDia( "<siard>" + siardNodeValue + "</siard>", "<siard>" + siard
										+ "</siard>", xmlDiaCopy );
							}
							if ( txt > 0 ) {
								String txtNodeString = eElement.getElementsByTagName( "txt" ).item( 0 )
										.getTextContent();
								int txtNodeValue = Integer.parseInt( txtNodeString );
								txt = txt + txtNodeValue;
								Util.kadDia( "<txt>" + txtNodeValue + "</txt>", "<txt>" + txt + "</txt>",
										xmlDiaCopy );
							}
							if ( csv > 0 ) {
								String csvNodeString = eElement.getElementsByTagName( "csv" ).item( 0 )
										.getTextContent();
								int csvNodeValue = Integer.parseInt( csvNodeString );
								csv = csv + csvNodeValue;
								Util.kadDia( "<csv>" + csvNodeValue + "</csv>", "<csv>" + csv + "</csv>",
										xmlDiaCopy );
							}
							if ( xml > 0 ) {
								String xmlNodeString = eElement.getElementsByTagName( "xml" ).item( 0 )
										.getTextContent();
								int xmlNodeValue = Integer.parseInt( xmlNodeString );
								xml = xml + xmlNodeValue;
								Util.kadDia( "<xml>" + xmlNodeValue + "</xml>", "<xml>" + xml + "</xml>",
										xmlDiaCopy );
							}
							if ( xsd > 0 ) {
								String xsdNodeString = eElement.getElementsByTagName( "xsd" ).item( 0 )
										.getTextContent();
								int xsdNodeValue = Integer.parseInt( xsdNodeString );
								xsd = xsd + xsdNodeValue;
								Util.kadDia( "<xsd>" + xsdNodeValue + "</xsd>", "<xsd>" + xsd + "</xsd>",
										xmlDiaCopy );
							}
							if ( wave > 0 ) {
								String waveNodeString = eElement.getElementsByTagName( "wave" ).item( 0 )
										.getTextContent();
								int waveNodeValue = Integer.parseInt( waveNodeString );
								wave = wave + waveNodeValue;
								Util.kadDia( "<wave>" + waveNodeValue + "</wave>", "<wave>" + wave + "</wave>",
										xmlDiaCopy );
							}
							if ( mp3 > 0 ) {
								String mp3NodeString = eElement.getElementsByTagName( "mp3" ).item( 0 )
										.getTextContent();
								int mp3NodeValue = Integer.parseInt( mp3NodeString );
								mp3 = mp3 + mp3NodeValue;
								Util.kadDia( "<mp3>" + mp3NodeValue + "</mp3>", "<mp3>" + mp3 + "</mp3>",
										xmlDiaCopy );
							}
							if ( jp2 > 0 ) {
								String jp2NodeString = eElement.getElementsByTagName( "jp2" ).item( 0 )
										.getTextContent();
								int jp2NodeValue = Integer.parseInt( jp2NodeString );
								jp2 = jp2 + jp2NodeValue;
								Util.kadDia( "<jp2>" + jp2NodeValue + "</jp2>", "<jp2>" + jp2 + "</jp2>",
										xmlDiaCopy );
							}
							if ( jpx > 0 ) {
								String jpxNodeString = eElement.getElementsByTagName( "jpx" ).item( 0 )
										.getTextContent();
								int jpxNodeValue = Integer.parseInt( jpxNodeString );
								jpx = jpx + jpxNodeValue;
								Util.kadDia( "<jpx>" + jpxNodeValue + "</jpx>", "<jpx>" + jpx + "</jpx>",
										xmlDiaCopy );
							}
							if ( jpeg > 0 ) {
								String jpegNodeString = eElement.getElementsByTagName( "jpeg" ).item( 0 )
										.getTextContent();
								int jpegNodeValue = Integer.parseInt( jpegNodeString );
								jpeg = jpeg + jpegNodeValue;
								Util.kadDia( "<jpeg>" + jpegNodeValue + "</jpeg>", "<jpeg>" + jpeg + "</jpeg>",
										xmlDiaCopy );
							}
							if ( png > 0 ) {
								String pngNodeString = eElement.getElementsByTagName( "png" ).item( 0 )
										.getTextContent();
								int pngNodeValue = Integer.parseInt( pngNodeString );
								png = png + pngNodeValue;
								Util.kadDia( "<png>" + pngNodeValue + "</png>", "<png>" + png + "</png>",
										xmlDiaCopy );
							}
							if ( dng > 0 ) {
								String dngNodeString = eElement.getElementsByTagName( "dng" ).item( 0 )
										.getTextContent();
								int dngNodeValue = Integer.parseInt( dngNodeString );
								dng = dng + dngNodeValue;
								Util.kadDia( "<dng>" + dngNodeValue + "</dng>", "<dng>" + dng + "</dng>",
										xmlDiaCopy );
							}
							if ( svg > 0 ) {
								String svgNodeString = eElement.getElementsByTagName( "svg" ).item( 0 )
										.getTextContent();
								int svgNodeValue = Integer.parseInt( svgNodeString );
								svg = svg + svgNodeValue;
								Util.kadDia( "<svg>" + svgNodeValue + "</svg>", "<svg>" + svg + "</svg>",
										xmlDiaCopy );
							}
							if ( mpeg2 > 0 ) {
								String mpeg2NodeString = eElement.getElementsByTagName( "mpeg2" ).item( 0 )
										.getTextContent();
								int mpeg2NodeValue = Integer.parseInt( mpeg2NodeString );
								mpeg2 = mpeg2 + mpeg2NodeValue;
								Util.kadDia( "<mpeg2>" + mpeg2NodeValue + "</mpeg2>", "<mpeg2>" + mpeg2
										+ "</mpeg2>", xmlDiaCopy );
							}
							if ( mp4 > 0 ) {
								String mp4NodeString = eElement.getElementsByTagName( "mp4" ).item( 0 )
										.getTextContent();
								int mp4NodeValue = Integer.parseInt( mp4NodeString );
								mp4 = mp4 + mp4NodeValue;
								Util.kadDia( "<mp4>" + mp4NodeValue + "</mp4>", "<mp4>" + mp4 + "</mp4>",
										xmlDiaCopy );
							}
							if ( xls > 0 ) {
								String xlsNodeString = eElement.getElementsByTagName( "xls" ).item( 0 )
										.getTextContent();
								int xlsNodeValue = Integer.parseInt( xlsNodeString );
								xls = xls + xlsNodeValue;
								Util.kadDia( "<xls>" + xlsNodeValue + "</xls>", "<xls>" + xls + "</xls>",
										xmlDiaCopy );
							}
							if ( odt > 0 ) {
								String odtNodeString = eElement.getElementsByTagName( "odt" ).item( 0 )
										.getTextContent();
								int odtNodeValue = Integer.parseInt( odtNodeString );
								odt = odt + odtNodeValue;
								Util.kadDia( "<odt>" + odtNodeValue + "</odt>", "<odt>" + odt + "</odt>",
										xmlDiaCopy );
							}
							if ( ods > 0 ) {
								String odsNodeString = eElement.getElementsByTagName( "ods" ).item( 0 )
										.getTextContent();
								int odsNodeValue = Integer.parseInt( odsNodeString );
								ods = ods + odsNodeValue;
								Util.kadDia( "<ods>" + odsNodeValue + "</ods>", "<ods>" + ods + "</ods>",
										xmlDiaCopy );
							}
							if ( odp > 0 ) {
								String odpNodeString = eElement.getElementsByTagName( "odp" ).item( 0 )
										.getTextContent();
								int odpNodeValue = Integer.parseInt( odpNodeString );
								odp = odp + odpNodeValue;
								Util.kadDia( "<odp>" + odpNodeValue + "</odp>", "<odp>" + odp + "</odp>",
										xmlDiaCopy );
							}
							if ( other > 0 ) {
								String otherNodeString = eElement.getElementsByTagName( "other" ).item( 0 )
										.getTextContent();
								int otherNodeValue = Integer.parseInt( otherNodeString );
								other = other + otherNodeValue;
								Util.kadDia( "<other>" + otherNodeValue + "</other>", "<other>" + other
										+ "</other>", xmlDiaCopy );
							}
						}
					}
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				System.out.print( "                                                                    " );
				System.out.print( "\r" );

				System.out.println( kostval.getTextResourceService().getText( MESSAGE_SIPVALIDATION_DONE,
						logFile.getAbsolutePath() ) );

				Util.deleteFile( pdftronReport );
				Util.deleteFile( pdftronReportXsl );
				Util.deleteDir( logDirValDatei );

				if ( ok ) {
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
					}
					System.exit( 0 );
				} else {
					// bestehendes Workverzeichnis ggf. löschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
					}
					System.exit( 2 );
				}
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
			} catch ( Exception e ) {
				LOGGER.logError( "<Error>"
						+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() )
						+ kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 )
						+ kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				System.out.println( "Exception: " + e.getMessage() );
			} catch ( StackOverflowError eso ) {
				LOGGER
						.logError( "<Validation><ValType>StackOverflowError</ValType><Invalid>invalid</Invalid></Validation></Format></KOSTValLog>" );
				System.out.println( "Exception: " + "StackOverflowError" );
			} catch ( OutOfMemoryError eoom ) {
				LOGGER
						.logError( "<Validation><ValType>OutOfMemoryError</ValType><Invalid>invalid</Invalid></Validation></Format></KOSTValLog>" );
				System.out.println( "Exception: " + "OutOfMemoryError" );
			}

		} else {
			/* Ueberprüfung des Parameters (Val-Typ): format / sip args[0] ist nicht "--format" oder
			 * "--sip" --> INVALIDE */
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
				tmpDir.deleteOnExit();
			}
			System.exit( 1 );
		}
	}

	// TODO: ValFile --> Formatvalidierung einer Datei --> erledigt --> nur Marker
	private static boolean valFile( File valDatei, String logFileName, File directoryOfLogfile,
			boolean verbose ) throws IOException
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );
		String originalValName = valDatei.getAbsolutePath();
		String valDateiName = valDatei.getName();
		String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
		boolean valFile = false;

		if ( (valDateiExt.equals( ".jp2" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_JP2VALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			System.out.print( "JP2:   " + originalValName + " " );
			Controllerjp2 controller1 = (Controllerjp2) context.getBean( "controllerjp2" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile );
			valFile = okMandatory;

			if ( okMandatory ) {
				// Validierte Datei valide
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				LOGGER
						.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			/* Ausgabe der Pfade zu den Jpylyzer Reports, falls welche generiert wurden (-v) oder Jpylyzer
			 * Report löschen */
			File JpylyzerReport = new File( directoryOfLogfile, valDatei.getName() + ".jpylyzer-log.xml" );

			if ( JpylyzerReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> Jpylyzer-Report lassen
				} else {
					// kein optionaler Parameter --> Jpylyzer-Report loeschen!
					JpylyzerReport.delete();
				}
			}

		} else if ( (valDateiExt.equals( ".jpeg" ) || valDateiExt.equals( ".jpg" ) || valDateiExt
				.equals( ".jpe" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_JPEGVALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			System.out.print( "JPEG:  " + originalValName + " " );
			Controllerjpeg controller1 = (Controllerjpeg) context.getBean( "controllerjpeg" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile );
			valFile = okMandatory;

			if ( okMandatory ) {
				// Validierte Datei valide
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				LOGGER
						.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

		} else if ( (valDateiExt.equals( ".tiff" ) || valDateiExt.equals( ".tif" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_TIFFVALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			System.out.print( "TIFF:  " + originalValName + " " );
			Controllertiff controller1 = (Controllertiff) context.getBean( "controllertiff" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile );
			boolean ok = false;

			/* die Validierungen A sind obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */
			if ( okMandatory ) {
				ok = controller1.executeOptional( valDatei, directoryOfLogfile );
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				LOGGER
						.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			/* Ausgabe der Pfade zu den Jhove Reports, falls welche generiert wurden (-v) oder Jhove
			 * Report löschen */
			File jhoveReport = new File( directoryOfLogfile, valDatei.getName() + ".jhove-log.txt" );

			if ( jhoveReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> Jhove-Report lassen
				} else {
					// kein optionaler Parameter --> Jhove-Report loeschen!
					jhoveReport.delete();
				}
			}

		} else if ( (valDateiExt.equals( ".siard" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_SIARDVALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			System.out.print( "SIARD: " + originalValName + " " );
			Controllersiard controller2 = (Controllersiard) context.getBean( "controllersiard" );
			boolean okMandatory = controller2.executeMandatory( valDatei, directoryOfLogfile );
			boolean ok = false;

			/* die Validierungen A-D sind obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */
			if ( okMandatory ) {
				ok = controller2.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der optionalen Schritte
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( ok ) {
				// Validierte Datei valide
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Fehler in Validierte Datei --> invalide
				LOGGER
						.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

		} else if ( (valDateiExt.equals( ".pdf" ) || valDateiExt.equals( ".pdfa" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_PDFAVALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			System.out.print( "PDFA:  " + originalValName );
			Controllerpdfa controller3 = (Controllerpdfa) context.getBean( "controllerpdfa" );

			boolean okMandatory = controller3.executeMandatory( valDatei, directoryOfLogfile );
			boolean ok = false;

			/* die Initialisierung ist obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */

			if ( okMandatory ) {
				ok = controller3.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der validierung und optionalen Bildvalidierung
			}

			ok = (ok && okMandatory);
			valFile = ok;

			if ( valFile ) {
				// Validierte Datei valide
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Valid" );
			} else {
				// Validierte Datei invalide
				LOGGER
						.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				System.out.println( " = Invalid" );
			}

			/* Ausgabe der Pfade zu den Pdftron Reports, falls welche generiert wurden Pdftron Reports
			 * löschen */
			File pdftronReport = new File( directoryOfLogfile, "report.xml" );
			File pdftronXsl = new File( directoryOfLogfile, "report.xsl" );

			if ( pdftronReport.exists() ) {
				// PDFTron-Report loeschen!
				pdftronReport.delete();
				pdftronXsl.delete();
			}

		} else {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
		}
		return valFile;
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
