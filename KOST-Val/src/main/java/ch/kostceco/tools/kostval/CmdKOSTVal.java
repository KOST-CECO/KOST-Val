/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath,
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
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

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.controller.ControllerInit;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des
 * Controllers, des Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class CmdKOSTVal implements MessageConstants {

	private TextResourceService textResourceService;

	public TextResourceService getTextResourceService() {
		return textResourceService;
	}

	public void setTextResourceService(TextResourceService textResourceService) {
		this.textResourceService = textResourceService;
	}

	/**
	 * Die Eingabe besteht aus 2 bis 4 Parameter:
	 * 
	 * args[0] Validierungstyp "--sip" / "--format" / "--onlysip" (TODO
	 * "--hotfolder")
	 * 
	 * args[1] Pfad zur Val-File
	 * 
	 * args[2] Sprache "--de" / "--fr" / "--it" / "--en"
	 * 
	 * args[3] Logtyp "--xml" / "--min" / "--max" (= xml+verbose)
	 * 
	 * 2 (--de) und 3 (--xml) sind beide optional (Standardwert)
	 * 
	 * @param args
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		Util.switchOffConsole();
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml");
		CmdKOSTVal cmdkostval = (CmdKOSTVal) context.getBean("cmdkostval");
		Util.switchOnConsole();

		String versionKostVal = "2.3.0.0";
		System.out.println("KOST-Val " + versionKostVal);

		// Ist die Anzahl Parameter (mind 2) korrekt?
		if (args.length < 2) {
			System.out.println(cmdkostval.getTextResourceService().getText(ERROR_PARAMETER_USAGE));
			context.close();
			System.exit(1);
		}

		Locale locale = Locale.getDefault();
		String localeSt = "de";
		if (locale.toString().startsWith("fr")) {
			localeSt = "fr";
		} else if (locale.toString().startsWith("it")) {
			localeSt = "it";
		} else if (locale.toString().startsWith("en")) {
			localeSt = "en";
		} else {
			localeSt = "de";
		}

		String arg0 = args[0];
		String arg1 = args[1];
		String arg2 = "";
		String arg3 = "";

		// Standardwerte bei fehlenden Parameter eingeben
		if (args.length == 2) {
			arg2 = "--" + localeSt;
			arg3 = "--xml";
			args = new String[] { arg0, arg1, arg2, arg3 };
		} else if (args.length == 3) {
			if (args[2].contains("de")) {
				arg2 = "--de";
				arg3 = "--xml";
			} else if (args[2].contains("fr")) {
				arg2 = "--fr";
				arg3 = "--xml";
			} else if (args[2].contains("it")) {
				arg2 = "--it";
				arg3 = "--xml";
			} else if (args[2].contains("en")) {
				arg2 = "--en";
				arg3 = "--xml";
			} else if (args[2].contains("xml")) {
				arg2 = "--" + localeSt;
				arg3 = "--xml";
			} else if (args[2].contains("min")) {
				arg2 = "--" + localeSt;
				arg3 = "--min";
			} else if (args[2].contains("max")) {
				arg2 = "--" + localeSt;
				arg3 = "--max";
			} else {
				arg2 = "--" + localeSt;
				arg3 = "--xml";
			}
			args = new String[] { arg0, arg1, arg2, arg3 };
		} else if (args.length == 4) {
			if (args[2].equalsIgnoreCase("--xml") || args[2].equalsIgnoreCase("--min")
					|| args[2].equalsIgnoreCase("--max")) {
				// arg 2 und 3 vertauscht
				arg2 = args[3];
				arg3 = args[2];
			} else {
				arg2 = args[2];
				arg3 = args[3];
			}
			args = new String[] { arg0, arg1, arg2, arg3 };
		}

		/*
		 * Die Eingabe sollte jetzt aus diesen 4 Parameter bestehen:
		 * 
		 * args[0] Validierungstyp "--sip" / "--format" / "--onlysip"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] Sprache "--de" / "--fr" / "--it" / "--en"
		 * 
		 * args[3] Logtyp "--xml" / "--min" / "--max" (= xml+verbose)
		 * 
		 */
		Boolean booArgs = true;
		Boolean booArg0 = false;
		Boolean booArg2 = false;
		Boolean booArg3 = false;
		if (args[0].equals("--sip") || args[0].equals("--format") || args[0].equals("--onlysip")) {
			booArg0 = true;
		}
		if (args[2].equals("--de") || args[2].equals("--fr") || args[2].equals("--it") || args[2].equals("--en")) {
			booArg2 = true;
		}
		if (args[3].equals("--xml") || args[3].equals("--min") || args[3].equals("--max")) {
			booArg3 = true;
		}
		if (booArg0 && booArg2 && booArg3) {
			booArgs = true;
		}

		if (!booArgs) {
			System.out.println(cmdkostval.getTextResourceService().getText(ERROR_PARAMETER_USAGE));
			context.close();
			System.exit(1);
		}

		/*
		 * Kontrolle der wichtigsten Eigenschaften: Log-Verzeichnis, Arbeitsverzeichnis,
		 * Java, jhove Configuration, Konfigurationsverzeichnis, path.tmp
		 */
		ControllerInit controllerInit = (ControllerInit) context.getBean("controllerInit");
		boolean init;
		try {
			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist eine
			 * generelle Aufgabe in allen Modulen. Zuerst immer dirOfJarPath ermitteln und
			 * dann alle Pfade mit dirOfJarPath + File.separator + erweitern.
			 */
			File pathJarFile20 = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
			/*
			 * wennn im Pfad ein Leerschlag ist, muss er noch normalisiert werden
			 */
			String dirOfJarPath = pathJarFile20.getAbsolutePath();
			dirOfJarPath = dirOfJarPath.replaceAll("%20", " ");
			pathJarFile20 = new File(dirOfJarPath);
			init = controllerInit.init(locale, dirOfJarPath, versionKostVal);
			if (!init) {
				// Fehler: es wird abgebrochen
				String text = "Ein Fehler ist aufgetreten. Siehe Konsole.";
				if (locale.toString().startsWith("fr")) {
					text = "Une erreur s`est produite. Voir Console.";
				} else if (locale.toString().startsWith("it")) {
					text = "Si è verificato un errore. Vedere la console.";
				} else if (locale.toString().startsWith("en")) {
					text = "An error has occurred. See Console.";
				}
				System.out.println(text);
				context.close();
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// System.out.println( "args: " + args[0] + " " + args[1] + " " +
		// args[2] + " " + args[3] );
		if (KOSTVal.main(args, versionKostVal)) {
			// Valid
			// alle Validierten Dateien valide
			context.close();
			System.exit(0);
		} else {
			// Invalid
			// Fehler in Validierten Dateien --> invalide
			context.close();
			System.exit(2);
		}

	}

}