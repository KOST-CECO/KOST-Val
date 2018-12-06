/* == KOST-Val ==================================================================================
 * The KOST-Val v1.9.1 application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO),
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
// import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;

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
		// System.out.println( new Timestamp( System.currentTimeMillis() ) + " 107 Start " );
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );
		/* System.out.println( new Timestamp( System.currentTimeMillis() ) +
		 * " 110 Ende ApplicationContext " ); */

		System.out.println( "KOST-Val" );

		// Zeitstempel Start
		java.util.Date nowStart = new java.util.Date();
		java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
		String ausgabeStart = sdfStart.format( nowStart );

		/* TODO: siehe Bemerkung im applicationContext-services.xml bezüglich Injection in der
		 * Superklasse aller Impl-Klassen ValidationModuleImpl validationModuleImpl =
		 * (ValidationModuleImpl) context.getBean("validationmoduleimpl"); */

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );

		/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
		 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit dirOfJarPath +
		 * File.separator + erweitern. */
		String path = new java.io.File( KOSTVal.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath() ).getAbsolutePath();
		path = path.substring( 0, path.lastIndexOf( "." ) );
		path = path + System.getProperty( "java.class.path" );
		String locationOfJarPath = path;
		String dirOfJarPath = locationOfJarPath;
		if ( locationOfJarPath.endsWith( ".jar" ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		File directoryOfConfigfile = new File( System.getenv( "USERPROFILE" ) + File.separator
				+ ".kost-val" + File.separator + "configuration" );
		File kadZip = new File( dirOfJarPath + File.separator + "configuration" + File.separator
				+ "KaD_SignatureFile_V72.xml" );
		File kadFile = new File( directoryOfConfigfile + File.separator + "KaD_SignatureFile_V72.xml" );
		if ( !kadFile.exists() ) {
			directoryOfConfigfile.mkdirs();
			Util.copyFile( kadZip, kadFile );
		}
		File configFileZip = new File( dirOfJarPath + File.separator + "configuration" + File.separator
				+ "kostval.conf.xml" );
		File configFile = new File( directoryOfConfigfile + File.separator + "kostval.conf.xml" );
		if ( !configFile.exists() ) {
			directoryOfConfigfile.mkdirs();
			Util.copyFile( configFileZip, configFile );
		}
		Map<String, String> configMap = kostval.getConfigurationService().configMap();

		// Ueberprüfung des Parameters (Log-Verzeichnis)
		String pathToLogfile = configMap.get( "PathToLogfile" );
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

		// Ist die Anzahl Parameter (mind. 2) korrekt?
		if ( args.length < 2 ) {
			System.out.println( kostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		File valDatei = new File( args[1] );
		File logDatei = null;
		logDatei = valDatei;
		// File logDirValDatei = new File( pathToLogfile, valDatei.getName() );

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidationPdftools = configMap.get( "pdftools" );
		String pdfaValidationCallas = configMap.get( "callas" );
		String pdfaValidation = "no";
		if ( pdfaValidationPdftools.equalsIgnoreCase( "yes" )
				|| pdfaValidationCallas.equalsIgnoreCase( "yes" ) ) {
			pdfaValidation = "yes";
		}
		String siardValidation = configMap.get( "siardValidation" );
		String tiffValidation = configMap.get( "tiffValidation" );
		String jp2Validation = configMap.get( "jp2Validation" );
		String jpegValidation = configMap.get( "jpegValidation" );
		if ( pdfaValidationPdftools.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ pdfaValidationPdftools );
			System.exit( 1 );
		}
		if ( pdfaValidationCallas.startsWith( "Configuration-Error:" ) ) {
			System.out.println( kostval.getTextResourceService().getText( MESSAGE_XML_MODUL_Ab_SIP )
					+ pdfaValidationCallas );
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
		// TODO: Ab hier kann ins log geschrieben werden...

		// ggf alte SIP-Validierung-Versions-Notiz löschen
		File ECH160_1_1 = new File( directoryOfLogfile.getAbsolutePath() + File.separator
				+ "ECH160_1.1.txt" );
		File ECH160_1_0 = new File( directoryOfLogfile.getAbsolutePath() + File.separator
				+ "ECH160_1.0.txt" );
		if ( ECH160_1_1.exists() ) {
			Util.deleteFile( ECH160_1_1 );
		} else if ( ECH160_1_0.exists() ) {
			Util.deleteFile( ECH160_1_0 );
		}

		String version = "";

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
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMATON, formatValOn,
				version ) );
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_INFO ) );
		String config = "";
		for ( String key : configMap.keySet() ) {
			config = config + key + " " + configMap.get( key ) + "; ";
		}
		LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_CONFIG, config ) );

		if ( args[0].equals( "--format" ) && formatValOn.equals( "" ) ) {
			// Formatvalidierung aber alle Formate ausgeschlossen
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_NOFILEENDINGS ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_NOFILEENDINGS ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			System.exit( 1 );
		}

		File xslOrig = new File( dirOfJarPath + File.separator + "resources" + File.separator
				+ "kost-val.xsl" );
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
					// logFile bereinigung (& End und ggf 3c)
					Util.valEnd3cAmp( "", logFile );
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
				LOGGER.logError( kostval.getTextResourceService().getText(
						ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) ) );
				System.console().printf(
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		name = directoryOfLogfile.getAbsolutePath();

		pathElements = name.split( "/" );
		for ( int i = 0; i < pathElements.length; i++ ) {
			String element = pathElements[i];

			Matcher matcher = pattern.matcher( element );

			boolean matchFound = matcher.find();
			if ( matchFound ) {
				LOGGER.logError( kostval.getTextResourceService().getText(
						ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) ) );
				System.console().printf(
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_WRONG_JRE ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_WRONG_JRE ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
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
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			System.exit( 1 );
		}

		/* Ueberprüfung des optionalen Parameters (2 -v --> im Verbose-mode werden die originalen Logs
		 * nicht gelöscht (PDFTron, Jhove & Co.) */
		boolean verbose = false;
		if ( args.length > 2 ) {
			if ( !(args[2].equals( "-v" )) ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_PARAMETER_OPTIONAL_1 ) ) );
				System.out.println( kostval.getTextResourceService().getText( ERROR_PARAMETER_OPTIONAL_1 ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			} else {
				verbose = true;
			}
		}

		/* Initialisierung TIFF-Modul B (JHove-Validierung) existiert configuration\jhove.conf */
		File fJhoveConf = new File( dirOfJarPath + File.separator + "configuration" + File.separator
				+ "jhove.conf" );
		if ( !fJhoveConf.exists() ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_JHOVECONF_MISSING ) ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_JHOVECONF_MISSING ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
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
				LOGGER.logError( kostval.getTextResourceService().getText(
						ERROR_IOE,
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) ) );
				System.console().printf(
						kostval.getTextResourceService().getText( ERROR_SPECIAL_CHARACTER, name,
								matcher.group( i ) ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );
				System.exit( 1 );
			}
		}

		// Ueberprüfung des Parameters (Val-Datei): existiert die Datei?
		if ( !valDatei.exists() ) {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_IOE,
					kostval.getTextResourceService().getText( ERROR_VALFILE_FILENOTEXISTING ) ) );
			System.out
					.println( kostval.getTextResourceService().getText( ERROR_VALFILE_FILENOTEXISTING ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			System.exit( 1 );
		}

		if ( args[0].equals( "--format" ) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT1 ) );
			String countNioDetail = "";
			String countNioExtension = "";
			Integer countNio = 0;
			Integer countSummaryNio = 0;
			Integer countSummaryIo = 0;
			Integer count = 0;
			Integer countProgress = 0;
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
				System.out.print( valDatei.getAbsolutePath() + " " );
				boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						dirOfJarPath, configMap );

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 ) );

				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}

				LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );

				if ( valFile ) {
					// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
					}

					File pathTemp = new File( directoryOfLogfile, "path.tmp" );
					if ( pathTemp.exists() ) {
						pathTemp.delete();
					}
					if ( pathTemp.exists() ) {
						pathTemp.deleteOnExit();
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
					Map<String, File> fileUnsortedMap = Util.getFileMapFile( valDatei );
					Map<String, File> fileMap = new TreeMap<String, File>( fileUnsortedMap );
					int numberInFileMap = fileMap.size();
					Set<String> fileMapKeys = fileMap.keySet();
					for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
						String entryName = iterator.next();
						File newFile = fileMap.get( entryName );
						if ( !newFile.isDirectory() ) {
							valDatei = newFile;
							String valDateiName = valDatei.getName();
							String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
							count = count + 1;
							countProgress = countProgress + 1;

							if ( ((valDateiExt.equals( ".jp2" ))) && jp2Validation.equals( "yes" ) ) {
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "JP2:   " + valDatei.getAbsolutePath()
										+ " " );

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );

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
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "JPEG:  " + valDatei.getAbsolutePath()
										+ " " );

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );

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
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "TIFF:  " + valDatei.getAbsolutePath()
										+ " " );

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );

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
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "SIARD: " + valDatei.getAbsolutePath()
										+ " " );

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );

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
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "PDFA:  " + valDatei.getAbsolutePath() );

								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );

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
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						}
					}
				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN,
									"Formatvalidation: " + e.getMessage() )
							+ kostval.getTextResourceService().getText( MESSAGE_XML_FORMAT2 )
							+ kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
					System.out.println( "Exception: " + e.getMessage() );
				} catch ( StackOverflowError eso ) {
					LOGGER.logError( kostval.getTextResourceService().getText( ERROR_XML_STACKOVERFLOWMAIN ) );
					System.out.println( "Exception: " + "StackOverflowError" );
				} catch ( OutOfMemoryError eoom ) {
					LOGGER.logError( kostval.getTextResourceService().getText( ERROR_XML_OUTOFMEMORYMAIN ) );
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

				File callasNo = new File( directoryOfLogfile + File.separator + "_callas_NO.txt" );
				if ( callasNo.exists() ) {
					callasNo.delete();
				}

				// Garbage Collecter aufruf zur Bereinigung
				System.gc();

				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( "", logFile );

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
				String summary = "";
				if ( countNio > 0 ) {
					// mit Detail weil countNio > 0
					summary = kostval.getTextResourceService().getText( MESSAGE_XML_SUMMARYDETAIL, count,
							countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
							countNioP, countNioDetail, countNioExtension );
				} else {
					// ohne Detail weil countNio == 0
					summary = kostval.getTextResourceService().getText( MESSAGE_XML_SUMMARY, count,
							countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
							countNioP );
				}
				String newFormat = "<Format>" + summary;
				Util.oldnewstring( "<Format>", newFormat, logFile );

				System.out.println( kostval.getTextResourceService().getText(
						MESSAGE_FORMATVALIDATION_DONE, summaryFormat, logFile.getAbsolutePath() ) );

				System.out.print( "                                                                    " );
				System.out.print( "\r" );

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

					File pathTemp = new File( directoryOfLogfile, "path.tmp" );
					if ( pathTemp.exists() ) {
						pathTemp.delete();
					}
					if ( pathTemp.exists() ) {
						pathTemp.deleteOnExit();
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
				String newFormat = "";
				String countNioDetail = "";
				String countNioExtension = "";
				Integer countNio = 0;
				Integer countSummaryNio = 0;
				Integer countSummaryIo = 0;
				Integer count = 0;
				Integer countProgress = 0;
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
							// set to null
							read = null;
						} catch ( Exception e ) {
							LOGGER.logError( "<Error>"
									+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN,
											"ZIP-Header-Exception: " + e.getMessage() ) );
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

						// logFile bereinigung (& End und ggf 3c)
						Util.valEnd3cAmp( "", logFile );

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

								// logFile bereinigung (& End und ggf 3c)
								Util.valEnd3cAmp( "", logFile );

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
				Map<String, File> fileUnsortedMap = Util.getFileMapFile( valDatei );
				Map<String, File> fileMap = new TreeMap<String, File>( fileUnsortedMap );
				int numberInFileMap = fileMap.size();
				Set<String> fileMapKeys = fileMap.keySet();

				for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
					String entryName = iterator.next();
					File newFile = fileMap.get( entryName );

					if ( !newFile.isDirectory()
							&& newFile.getAbsolutePath().contains( File.separator + "content" + File.separator ) ) {
						valDatei = newFile;
						valDateiName = valDatei.getName();
						valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
						count = count + 1;
						countProgress = countProgress + 1;

						/* String extension = valDatei.getName(); int lastIndexOf = extension.lastIndexOf( "."
						 * ); if ( lastIndexOf == -1 ) { // empty extension extension = "other"; } else {
						 * extension = extension.substring( lastIndexOf ); } */

						if ( valDateiExt.equals( ".pdf" ) || valDateiExt.equals( ".pdfa" ) ) {
							if ( pdfaValidation.equals( "yes" ) ) {
								// Validierung durchführen
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "PDFA:  " + valDatei.getAbsolutePath() );
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );
								if ( valFile ) {
									pdfaCountIo = pdfaCountIo + 1;
								} else {
									pdfaCountNio = pdfaCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						} else if ( valDateiExt.equals( ".tiff" ) || valDateiExt.equals( ".tif" ) ) {
							if ( tiffValidation.equals( "yes" ) ) {
								// Validierung durchführen
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "TIFF:  " + valDatei.getAbsolutePath()
										+ " " );
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );
								if ( valFile ) {
									tiffCountIo = tiffCountIo + 1;
								} else {
									tiffCountNio = tiffCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						} else if ( valDateiExt.equals( ".siard" ) ) {
							if ( siardValidation.equals( "yes" ) ) {
								// Validierung durchführen
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "SIARD: " + valDatei.getAbsolutePath()
										+ " " );

								// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
								String pathToWorkDirSiard = configMap.get( "PathToWorkDir" );
								File tmpDirSiard = new File( pathToWorkDirSiard + File.separator + "SIARD" );
								if ( tmpDirSiard.exists() ) {
									Util.deleteDir( tmpDirSiard );
								}
								if ( !tmpDirSiard.exists() ) {
									tmpDirSiard.mkdir();
								}
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );
								if ( valFile ) {
									siardCountIo = siardCountIo + 1;
								} else {
									siardCountNio = siardCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						} else if ( valDateiExt.equals( ".jpe" ) || valDateiExt.equals( ".jpeg" )
								|| valDateiExt.equals( ".jpg" ) ) {
							if ( jpegValidation.equals( "yes" ) ) {
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "JPEG:  " + valDatei.getAbsolutePath()
										+ " " );
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );
								if ( valFile ) {
									jpegCountIo = jpegCountIo + 1;
								} else {
									jpegCountNio = jpegCountNio + 1;
								}
							} else {
								countNio = countNio + 1;
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						} else if ( valDateiExt.equals( ".jp2" ) ) {
							if ( jp2Validation.equals( "yes" ) ) {
								// Validierung durchführen
								int countToValidated = numberInFileMap - count;
								System.out.print( countToValidated + " " + "JP2:   " + valDatei.getAbsolutePath()
										+ " " );
								boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
										dirOfJarPath, configMap );
								if ( valFile ) {
									jp2CountIo = jp2CountIo + 1;
								} else {
									jp2CountNio = jp2CountNio + 1;
								}
							} else {
								countNio = countNio + 1;
								countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
										+ valDatei.getAbsolutePath();
								if ( countNioExtension == "" ) {
									countNioExtension = valDateiExt;
								} else {
									// bereits Extensions vorhanden
									if ( countNioExtension.contains( valDateiExt ) ) {
										// Extension bereits erfasst
									} else {
										countNioExtension = countNioExtension + ", " + valDateiExt;
									}
								}
							}
						} else {
							countNio = countNio + 1;
							countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
									+ valDatei.getAbsolutePath();
							if ( countNioExtension == "" ) {
								countNioExtension = valDateiExt;
							} else {
								// bereits Extensions vorhanden
								if ( countNioExtension.contains( valDateiExt ) ) {
									// Extension bereits erfasst
								} else {
									countNioExtension = countNioExtension + ", " + valDateiExt;
								}
							}
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

				String summary = "";
				if ( countNio > 0 ) {
					// mit Detail weil countNio > 0
					summary = kostval.getTextResourceService().getText( MESSAGE_XML_SUMMARYDETAIL, count,
							countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
							countNioP, countNioDetail, countNioExtension );
				} else {
					// ohne Detail weil countNio == 0
					summary = kostval.getTextResourceService().getText( MESSAGE_XML_SUMMARY, count,
							countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
							countNioP );
				}

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
				okMandatory = controller.executeMandatory( valDatei, directoryOfLogfile, configMap );
				boolean ok = false;

				/* die Validierungen 1a - 1d sind obligatorisch, wenn sie bestanden wurden, können die
				 * restlichen Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt
				 * werden.
				 * 
				 * 1a wurde bereits getestet (vor der Formatvalidierung entsprechend fängt der Controller
				 * mit 1b an */
				if ( okMandatory ) {
					ok = controller.executeOptional( valDatei, directoryOfLogfile, configMap );
				}
				// Formatvalidierung validFormat
				ok = (ok && okMandatory && validFormat);

				if ( ok ) {
					// Validiertes SIP valide
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_VALID ) );
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
					LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
					System.out.println( "Valid" );
					System.out.println( "" );
				} else {
					// Fehler im Validierten SIP --> invalide
					LOGGER.logError( kostval.getTextResourceService().getText(
							MESSAGE_XML_VALERGEBNIS_INVALID ) );
					LOGGER
							.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 ) );
					LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
					System.out.println( "Invalid" );
					System.out.println( "" );

				}

				// Garbage Collecter aufruf zur Bereinigung
				System.gc();

				// Bereinigungen und ergaenzungen durchfuehren

				// Ergaenzung Format Summary
				newFormat = "<Format>" + summary;
				Util.oldnewstring( "<Format>", newFormat, logFile );

				// ggf. Fehlermeldung 3c ergänzen Util.val3c(summary3c, logFile );
				// logFile bereinigung (& End und ggf 3c)
				Util.valEnd3cAmp( summary3c, logFile );

				// Ergänzen welche SIP-Validierung durchgeführt wurde
				String sipVersion = " ";
				if ( ECH160_1_1.exists() ) {
					sipVersion = " (eCH-0160v1.1)";
					Util.deleteFile( ECH160_1_1 );
				} else if ( ECH160_1_0.exists() ) {
					sipVersion = " (eCH-0160v1.0)";
					Util.deleteFile( ECH160_1_0 );
				}
				Util.valSipversion( sipVersion, logFile );

				System.out.print( kostval.getTextResourceService().getText( MESSAGE_XML_LOG ) );
				System.out.print( "\r" );

				// bestehendes Workverzeichnis ggf. löschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( tmpDir.exists() ) {
					tmpDir.deleteOnExit();
				}

				File pathTemp = new File( directoryOfLogfile, "path.tmp" );
				if ( pathTemp.exists() ) {
					pathTemp.delete();
				}
				if ( pathTemp.exists() ) {
					pathTemp.deleteOnExit();
				}

				File callasNo = new File( directoryOfLogfile + File.separator + "_callas_NO.txt" );
				if ( callasNo.exists() ) {
					callasNo.delete();
				}

				System.out.print( "                                                                    " );
				System.out.print( "\r" );

				System.out.println( kostval.getTextResourceService().getText( MESSAGE_SIPVALIDATION_DONE,
						logFile.getAbsolutePath() ) );

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
			} catch ( Exception e ) {
				LOGGER.logError( "<Error>"
						+ kostval.getTextResourceService().getText( ERROR_XML_UNKNOWN,
								"SIP-ValidationException: " + e.getMessage() )
						+ kostval.getTextResourceService().getText( MESSAGE_XML_SIP2 )
						+ kostval.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				System.out.println( "Exception: " + e.getMessage() );
			} catch ( StackOverflowError eso ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_XML_STACKOVERFLOWMAIN ) );
				System.out.println( "Exception: " + "StackOverflowError" );
			} catch ( OutOfMemoryError eoom ) {
				LOGGER.logError( kostval.getTextResourceService().getText( ERROR_XML_OUTOFMEMORYMAIN ) );
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
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			System.exit( 1 );
		}
	}

	// TODO: ValFile --> Formatvalidierung einer Datei --> erledigt --> nur Marker
	private static boolean valFile( File valDatei, String logFileName, File directoryOfLogfile,
			boolean verbose, String dirOfJarPath, Map<String, String> configMap ) throws IOException
	{
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );
		String originalValName = valDatei.getAbsolutePath();
		String valDateiName = valDatei.getName();
		String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
		boolean valFile = false;
		File pathTemp = new File( directoryOfLogfile, "path.tmp" );

		// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen
		// wir es
		if ( pathTemp.exists() ) {
			pathTemp.delete();
		}
		try {
			pathTemp.createNewFile();
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		Util.oldnewstring( "", originalValName, pathTemp );

		if ( (valDateiExt.equals( ".jp2" )) ) {
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALTYPE,
					kostval.getTextResourceService().getText( MESSAGE_JP2VALIDATION ) ) );
			LOGGER.logError( kostval.getTextResourceService().getText( MESSAGE_XML_VALFILE,
					originalValName ) );
			Controllerjp2 controller1 = (Controllerjp2) context.getBean( "controllerjp2" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap );
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
			Controllerjpeg controller1 = (Controllerjpeg) context.getBean( "controllerjpeg" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap );
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
			Controllertiff controller1 = (Controllertiff) context.getBean( "controllertiff" );
			boolean okMandatory = controller1.executeMandatory( valDatei, directoryOfLogfile, configMap );
			boolean ok = false;

			/* die Validierungen A sind obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */
			if ( okMandatory ) {
				ok = controller1.executeOptional( valDatei, directoryOfLogfile, configMap );
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
			Controllersiard controller2 = (Controllersiard) context.getBean( "controllersiard" );
			boolean okMandatory = controller2.executeMandatory( valDatei, directoryOfLogfile, configMap );
			boolean ok = false;

			/* die Validierungen A-D sind obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */
			if ( okMandatory ) {
				ok = controller2.executeOptional( valDatei, directoryOfLogfile, configMap );
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
			Controllerpdfa controller3 = (Controllerpdfa) context.getBean( "controllerpdfa" );

			boolean okMandatory = controller3.executeMandatory( valDatei, directoryOfLogfile, configMap );
			boolean ok = false;

			/* die Initialisierung ist obligatorisch, wenn sie bestanden wurden, können die restlichen
			 * Validierungen, welche nicht zum Abbruch der Applikation führen, ausgeführt werden. */

			if ( okMandatory ) {
				ok = controller3.executeOptional( valDatei, directoryOfLogfile, configMap );
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

			/* Ausgabe der Pfade zu den _FontValidation.xml Reports, falls welche generiert wurden. Ggf
			 * löschen */
			File fontValidationReport = new File( directoryOfLogfile, valDatei.getName()
					+ "_FontValidation.xml" );
			// Test.pdf_FontValidation.xml
			if ( fontValidationReport.exists() ) {
				if ( verbose ) {
					// optionaler Parameter --> fontValidationReport lassen
				} else {
					// kein optionaler Parameter --> fontValidationReport loeschen!
					fontValidationReport.delete();
					fontValidationReport.deleteOnExit();
					Util.deleteFile( fontValidationReport );
				}
			}

		} else {
			LOGGER.logError( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
			System.out.println( kostval.getTextResourceService().getText( ERROR_INCORRECTFILEENDING,
					valDatei.getName() ) );
		}

		if ( pathTemp.exists() ) {
			// pathTemploeschen!
			pathTemp.delete();
		}

		// Garbage Collecter aufruf zur Bereinigung
		System.gc();

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
