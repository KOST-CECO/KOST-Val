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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * kostval --> Controllervalinit
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllervalinit implements MessageConstants {

	private static TextResourceService textResourceService;

	public static TextResourceService getTextResourceService() {
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService(TextResourceService textResourceService) {
		this.textResourceService = textResourceService;
	}

	// TODO
	public boolean valInit(String[] args, Map<String, String> configMap) throws IOException {
		boolean valInit = true;
		Locale locale = Locale.getDefault();

		// Ist die Anzahl Parameter (4) korrekt?
		if (args.length != 4) {
			System.out.println(getTextResourceService().getText(locale, ERROR_PARAMETER_USAGE));
			valInit = false;
			return valInit;
		}
		if (args[2].equalsIgnoreCase("--de")) {
			locale = Locale.of("de");
		} else if (args[2].equalsIgnoreCase("--fr")) {
			locale = Locale.of("fr");
		} else if (args[2].equalsIgnoreCase("--it")) {
			locale = Locale.of("it");
		} else if (args[2].equalsIgnoreCase("--en")) {
			locale = Locale.of("en");
		} else {
			System.out.println(getTextResourceService().getText(locale, ERROR_PARAMETER_USAGE));
			valInit = false;
			return valInit;
		}
		if (!args[0].equalsIgnoreCase("--format") && !args[0].equalsIgnoreCase("--sip")
				&& !args[0].equalsIgnoreCase("--onlysip")) {
			System.out.println(getTextResourceService().getText(locale, ERROR_PARAMETER_USAGE));
			valInit = false;
			return valInit;
		}
		File init0File = new File(args[1]);
		if (!init0File.exists()) {
			System.out.println(getTextResourceService().getText(locale, ERROR_VALFILE_FILENOTEXISTING));
			valInit = false;
			return valInit;
		}
		if (!args[3].equalsIgnoreCase("--xml") && !args[3].equalsIgnoreCase("--min")
				&& !args[3].equalsIgnoreCase("--max")) {
			System.out.println(getTextResourceService().getText(locale, ERROR_PARAMETER_USAGE));
			valInit = false;
			return valInit;
		} else {
			/*
			 * Angabe ob dargestellt werden soll, dass KOST-Val noch laeuft --xml (=no)
			 * zaehler anzeigen --max (=yes) auch "Windrad" --min (=nomin) zaehler anzeigen
			 */
			String showprogressonwork = "no";
			if (args[3].equalsIgnoreCase("--max")) {
				showprogressonwork = "yes";
			} else if (args[3].equalsIgnoreCase("--min")) {
				showprogressonwork = "nomin";
			}
			configMap.put("ShowProgressOnWork", showprogressonwork);
		}

		return valInit;

	}
}