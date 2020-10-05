/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;

/** kostval --> ControllerInit
 * 
 * Kontrolle der Funktionsfaehigkeit von KOST-Val.
 * 
 * Wird vor der Validierung einmal ausgefuehrt. */

public class ControllerInit implements MessageConstants
{

	private static TextResourceService	textResourceService;
	private String											pathToKostValDir	= System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x";

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean init( Locale locale, String dirOfJarPath, String versionKostVal ) throws IOException
	{
		boolean init = true;
		File directoryOfKostValDir = new File( pathToKostValDir );
		if ( !directoryOfKostValDir.exists() ) {
			directoryOfKostValDir.mkdir();
		}

		// Ueberpruefung des Parameters (Log-Verzeichnis)
		String logs = pathToKostValDir + File.separator + "logs";
		String pathToLogfile = logs;
		File directoryOfLogfile = new File( pathToLogfile );
		if ( !directoryOfLogfile.exists() ) {
			directoryOfLogfile.mkdir();
		}
		if ( !directoryOfLogfile.canWrite() ) {
			System.out.println( getTextResourceService().getText( locale, ERROR_LOGDIRECTORY_NOTWRITABLE,
					directoryOfLogfile ) );
			init = false;
			return init;
		}
		if ( !directoryOfLogfile.isDirectory() ) {
			System.out
					.println( getTextResourceService().getText( locale, ERROR_LOGDIRECTORY_NODIRECTORY ) );
			init = false;
			return init;
		}

		// Enthaelt Pfad zu der zu Validierenden Datei (vorher)
		File pathTmp = new File( logs + File.separator + "path.tmp" );
		if ( pathTmp.exists() ) {
			pathTmp.delete();
		}
		if ( pathTmp.exists() ) {
			List<String> oldtextList = Files.readAllLines( pathTmp.toPath(), StandardCharsets.UTF_8 );
			for ( int i = 0; i < oldtextList.size(); i++ ) {
				String oldtext = (oldtextList.get( i ));
				Util.oldnewstring( oldtext, "", pathTmp );
			}
		}

		// Informationen zum Arbeitsverzeichnis holen
		String pathToWorkDir = pathToKostValDir + File.separator + "temp_KOST-Val";
		File xslOrig = new File(
				dirOfJarPath + File.separator + "resources" + File.separator + "kost-val.xsl" );
		File xslCopy = new File( logs + File.separator + "kost-val.xsl" );
		if ( !xslCopy.exists() ) {
			Util.copyFile( xslOrig, xslCopy );
		}

		File tmpDir = new File( pathToWorkDir );

		/* bestehendes Workverzeichnis:loeschen wenn nicht leer, da am Schluss das Workverzeichnis
		 * geloescht wird und entsprechend bestehende Dateien geloescht werden koennen */
		if ( tmpDir.exists() ) {
			Util.deleteDir( tmpDir );
		}
		if ( tmpDir.exists() ) {
			if ( tmpDir.isDirectory() ) {
				// Get list of file in the directory. When its length is not zero the folder is not empty.
				String[] files = tmpDir.list();
				if ( files.length > 0 ) {
					System.out.println( getTextResourceService().getText( locale, ERROR_WORKDIRECTORY_EXISTS,
							pathToWorkDir ) );
					init = false;
					return init;
				}
			}
		}

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.8.0" ) < 0 ) {
			System.out.println( getTextResourceService().getText( locale, ERROR_WRONG_JRE ) );
			init = false;
			return init;
		}

		// bestehendes Workverzeichnis wieder anlegen
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		// Im workverzeichnis besteht kein Schreibrecht
		if ( !tmpDir.canWrite() ) {
			System.out.println(
					getTextResourceService().getText( locale, ERROR_WORKDIRECTORY_NOTWRITABLE, tmpDir ) );
			init = false;
			return init;
		}

		/* Vorbereitung Konfiguration
		 * 
		 * benoetigte Dateien in User config kopieren
		 * 
		 * wenn nicht vorhanden oder veraltete Version */
		String version = "kostval.conf.xml_v"+versionKostVal;
		File directoryOfConfigfile = new File( pathToKostValDir + File.separator + "configuration" );
		File directoryOfConfigfileInit = new File( dirOfJarPath + File.separator + "configuration" );
		if ( !directoryOfConfigfile.exists() ) {
			directoryOfConfigfile.mkdirs();
		}
		File configFileInit = new File(
				directoryOfConfigfileInit + File.separator + "kostval.conf.xml" );
		File configFile = new File( directoryOfConfigfile + File.separator + "kostval.conf.xml" );
		if ( !configFile.exists() ) {
			Util.copyFile( configFileInit, configFile );
		} else {
			if ( !Util.stringInFile( version, configFile ) ) {
				Util.copyFile( configFileInit, configFile );
			}
		}
		File configFileStandard = new File(
				directoryOfConfigfile + File.separator + "STANDARD.kostval.conf.xml" );
		if ( !configFileStandard.exists() ) {
			Util.copyFile( configFileInit, configFileStandard );
		}
		File xslDeInit = new File( directoryOfConfigfileInit + File.separator + "kostval-conf-DE.xsl" );
		File xslDe = new File( directoryOfConfigfile + File.separator + "kostval-conf-DE.xsl" );
		if ( !xslDe.exists() ) {
			Util.copyFile( xslDeInit, xslDe );
		} else {
			if ( !Util.stringInFile( version, xslDe ) ) {
				Util.copyFile( xslDeInit, xslDe );
			}
		}
		File xslEnInit = new File( directoryOfConfigfileInit + File.separator + "kostval-conf-EN.xsl" );
		File xslEn = new File( directoryOfConfigfile + File.separator + "kostval-conf-EN.xsl" );
		if ( !xslEn.exists() ) {
			Util.copyFile( xslEnInit, xslEn );
		} else {
			if ( !Util.stringInFile( version, xslEn ) ) {
				Util.copyFile( xslEnInit, xslEn );
			}
		}
		File xslFrInit = new File( directoryOfConfigfileInit + File.separator + "kostval-conf-FR.xsl" );
		File xslFr = new File( directoryOfConfigfile + File.separator + "kostval-conf-FR.xsl" );
		if ( !xslFr.exists() ) {
			Util.copyFile( xslFrInit, xslFr );
		} else {
			if ( !Util.stringInFile( version, xslFr ) ) {
				Util.copyFile( xslFrInit, xslFr );
			}
		}
		File kadInit = new File(
				directoryOfConfigfileInit + File.separator + "KaD_SignatureFile_V72.xml" );
		File kadFile = new File( directoryOfConfigfile + File.separator + "KaD_SignatureFile_V72.xml" );
		if ( !kadFile.exists() ) {
			Util.copyFile( kadInit, kadFile );
		}

		/* Initialisierung TIFF-Modul B (JHove-Validierung) existiert configuration\jhove.conf */
		File fJhoveConf = new File(
				dirOfJarPath + File.separator + "configuration" + File.separator + "jhove.conf" );
		if ( !fJhoveConf.exists() ) {
			System.out.println( getTextResourceService().getText( locale, ERROR_JHOVECONF_MISSING ) );
			init = false;
			return init;
		}

		return init;

	}
}