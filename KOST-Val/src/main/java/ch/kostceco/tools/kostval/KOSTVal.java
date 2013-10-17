/*== KOST-Val ==================================================================================
The KOST-Val v1.0.0 application is used for validate TIFF, SIARD, and PDF/A-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.kostceco.tools.kostval.controller.Controllertiff;
import ch.kostceco.tools.kostval.controller.Controllersiard;
import ch.kostceco.tools.kostval.controller.Controllerpdfa;
import ch.kostceco.tools.kostval.logging.LogConfigurator;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;

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
	 * Die Eingabe besteht aus Parameter 1: Pfad zur Val-File Parameter 2: Pfad
	 * zum Logging-Verzeichnis
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{

		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		// TODO: siehe Bemerkung im applicationContext-services.xml bezüglich
		// Injection in der Superklasse aller Impl-Klassen
		// ValidationModuleImpl validationModuleImpl = (ValidationModuleImpl)
		// context.getBean("validationmoduleimpl");

		KOSTVal kostval = (KOSTVal) context.getBean( "kostval" );

		// Ist die Anzahl Parameter (2) korrekt?
		if ( args.length < 2 ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		File valDatei = new File( args[0] );
		LOGGER.logInfo( kostval.getTextResourceService().getText(
				MESSAGE_KOSTVALIDATION ) );

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_WRONG_JRE ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		// Ueberprüfung des 2. Parameters (Log-Verzeichnis)
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

		// bestehendes Workverzeichnis ggf. löschen und wieder anlegen
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

		// Ueberprüfung des 1. Parameters (Val-Datei): existiert die Datei?
		if ( !valDatei.exists() ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_TIFFFILE_FILENOTEXISTING ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}

		String originalValName = valDatei.getAbsolutePath();

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
			System.exit( 1 );
		}

		// Konfiguration des Loggings, ein File Logger wird zusätzlich erstellt
		LogConfigurator logConfigurator = (LogConfigurator) context
				.getBean( "logconfigurator" );
		String logFileName = logConfigurator.configure(
				directoryOfLogfile.getAbsolutePath(), valDatei.getName() );

		LOGGER.logError( kostval.getTextResourceService().getText(
				MESSAGE_KOSTVALIDATION ) );

		if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".tiff" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".tif" )) ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_TIFFVALIDATION, valDatei.getName() ) );
			Controllertiff controller1 = (Controllertiff) context
					.getBean( "controllertiff" );
			boolean okMandatory = controller1.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierungen A sind obligatorisch, wenn sie bestanden
			// wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der Applikation führen,
			// ausgeführt werden.
			if ( okMandatory ) {
				ok = controller1.executeOptional( valDatei, directoryOfLogfile );
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

			// Ausgabe der Pfade zu den Jhove Reports, falls welche
			// generiert wurden
			if ( Util.getPathToReportJHove() != null ) {
				LOGGER.logInfo( kostval.getTextResourceService()
						.getText( MESSAGE_FOOTER_REPORTJHOVE,
								Util.getPathToReportJHove() ) );
			}

			LOGGER.logInfo( "" );
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

			if ( ok ) {
				System.exit( 0 );
			} else {
				System.exit( 2 );
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

			// die Validierungen A-D sind obligatorisch, wenn sie bestanden
			// wurden,
			// können die restlichen
			// Validierungen, welche nicht zum Abbruch der Applikation führen,
			// ausgeführt werden.
			if ( okMandatory ) {
				ok = controller2.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der optionalen Schritte
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

			// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( ok ) {
				System.exit( 0 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
			} else {
				System.exit( 2 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
			}

		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".pdf" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )) ) {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_PDFAVALIDATION, valDatei.getName() ) );
			Controllerpdfa controller3 = (Controllerpdfa) context
					.getBean( "controllerpdfa" );
			boolean okMandatory = controller3.executeMandatory( valDatei,
					directoryOfLogfile );
			boolean ok = false;

			// die Validierung A ist obligatorisch, wenn sie bestanden
			// wurden, können die restlichen
			// Validierungen, welche nicht zum Abbruch der Applikation führen,
			// ausgeführt werden.
			if ( okMandatory ) {
				ok = controller3.executeOptional( valDatei, directoryOfLogfile );
				// Ausführen der optionalen Schritte
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

			// Ausgabe der Pfade zu den Pdftron Reports, falls welche
			// generiert wurden
			if ( Util.getPathToReportPdftron() != null ) {
				LOGGER.logInfo( kostval.getTextResourceService().getText(
						MESSAGE_FOOTER_REPORTPDFTRON,
						Util.getPathToReportPdftron() ) );
			}

			LOGGER.logInfo( "" );
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

			// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( ok ) {
				System.exit( 0 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
			} else {
				System.exit( 2 );
				// Löschen des Arbeitsverzeichnisses, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
			}

		} else {
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					ERROR_INCORRECTFILEENDING ) );
			LOGGER.logInfo( kostval.getTextResourceService().getText(
					MESSAGE_VALIDATION_INTERRUPTED ) );
			System.exit( 1 );
		}
	}

}
