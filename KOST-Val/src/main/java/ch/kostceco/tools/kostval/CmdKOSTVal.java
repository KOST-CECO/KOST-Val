/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.kostceco.tools.kostval.controller.ControllerInit;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/** Dies ist die Starter-Klasse, verantwortlich für das Initialisieren des Controllers, des Loggings
 * und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class CmdKOSTVal implements MessageConstants
{

	private TextResourceService textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	/** Die Eingabe besteht aus 2 bis 4 Parameter:
	 * 
	 * args[0] Validierungstyp "--sip" / "--format" / "--onlysip" (TODO "--hotfolder")
	 * 
	 * args[1] Pfad zur Val-File
	 * 
	 * args[2] Sprache "--de" / "--fr" / "--en"
	 * 
	 * args[3] Logtyp "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (= xml+verbose)
	 * 
	 * 2 (--de) und 3 (--xml) sind beide optional (Standardwert)
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static void main( String[] args ) throws IOException
	{
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );
		CmdKOSTVal cmdkostval = (CmdKOSTVal) context.getBean( "cmdkostval" );

		System.out.println( "KOST-Val" );

		// Ist die Anzahl Parameter (mind 2) korrekt?
		if ( args.length < 2 ) {
			System.out.println( cmdkostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			context.close();
			System.exit( 1 );
		}

		Locale locale = Locale.getDefault();
		String localeSt = "de";
		if ( locale.toString().startsWith( "fr" ) ) {
			localeSt = "fr";
		} else if ( locale.toString().startsWith( "en" ) ) {
			localeSt = "en";
		} else {
			localeSt = "de";
		}

		String arg0 = args[0];
		String arg1 = args[1];
		String arg2 = "";
		String arg3 = "";

		String versionKostVal = "2.0.3";

		// Standardwerte bei fehlenden Parameter eingeben
		if ( args.length == 2 ) {
			arg2 = "--" + localeSt;
			arg3 = "--xml";
			args = new String[] { arg0, arg1, arg2, arg3 };
		} else if ( args.length == 3 ) {
			if ( args[2].equalsIgnoreCase( "--de" ) ) {
				arg2 = "--de";
				arg3 = "--xml";
			} else if ( args[2].equalsIgnoreCase( "--fr" ) ) {
				arg2 = "--fr";
				arg3 = "--xml";
			} else if ( args[2].equalsIgnoreCase( "--en" ) ) {
				arg2 = "--en";
				arg3 = "--xml";
			} else if ( args[2].equalsIgnoreCase( "--xml" ) ) {
				arg2 = "--" + localeSt;
				arg3 = "--xml";
			} else if ( args[2].equalsIgnoreCase( "--min" ) ) {
				arg2 = "--" + localeSt;
				arg3 = "--min";
			} else if ( args[2].equalsIgnoreCase( "--max" ) ) {
				arg2 = "--" + localeSt;
				arg3 = "--max";
			} else {
				System.out.println( cmdkostval.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
				context.close();
				System.exit( 1 );
			}
			args = new String[] { arg0, arg1, arg2, arg3 };
		} else if ( args.length == 4 ) {
			if ( args[2].equalsIgnoreCase( "--xml" ) || args[2].equalsIgnoreCase( "--min" )
					|| args[2].equalsIgnoreCase( "--max" ) ) {
				// arg 2 und 3 vertauscht
				arg2 = args[3];
				arg3 = args[2];
			} else {
				arg2 = args[2];
				arg3 = args[3];
			}
			args = new String[] { arg0, arg1, arg2, arg3 };
		}

		/* Kontrolle der wichtigsten Eigenschaften: Log-Verzeichnis, Arbeitsverzeichnis, Java, jhove
		 * Configuration, Konfigurationsverzeichnis, path.tmp */
		ControllerInit controllerInit = (ControllerInit) context.getBean( "controllerInit" );
		boolean init;
		try {
			String path = new java.io.File(
					KOSTVal.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
							.getAbsolutePath();
			String locationOfJarPath = path;
			String dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}
			init = controllerInit.init( locale, dirOfJarPath, versionKostVal );
			if ( !init ) {
				// Fehler: es wird abgebrochen
				String text = "Ein Fehler ist aufgetreten. Siehe Konsole.";
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur s`est produite. Voir Console.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An error has occurred. See Console.";
				}
				System.out.println( text );
				context.close();
				System.exit( 1 );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		// System.out.println( "args: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] );
		if ( KOSTVal.main( args, versionKostVal ) ) {
			// Valid
			// alle Validierten Dateien valide
			context.close();
			System.exit( 0 );
		} else {
			// Invalid
			// Fehler in Validierten Dateien --> invalide
			context.close();
			System.exit( 2 );
		}

	}

}