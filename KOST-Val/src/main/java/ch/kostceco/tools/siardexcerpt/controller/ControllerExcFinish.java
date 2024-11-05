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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

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

/**
 * Dies ist die Starter-Klasse, verantwortlich fuer das Initialisieren des
 * Controllers, des Loggings und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ControllerExcFinish implements MessageConstants {

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

	public static boolean mainFinish(String[] args) throws IOException {
		boolean finish = true;
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
		ControllerExcFinish controllerExcFinish = (ControllerExcFinish) context.getBean("controllerExcFinish");

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
		if (args.length < 4) {
			System.out.println(
					controllerExcFinish.getTextResourceServiceExc().getText(locale, EXC_ERROR_PARAMETER_USAGE));
			return false;
		}

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

		File configFileHard = new File(config + File.separator + "SIARDexcerpt.conf.xml");

		String pathToWorkDir = System.getenv("USERPROFILE") + File.separator + ".siardexcerpt" + File.separator
				+ "temp_SIARDexcerpt";
		File tmpDir = new File(pathToWorkDir);

		// die Anwendung muss mindestens unter Java 8 laufen
		String javaRuntimeVersion = System.getProperty("java.vm.version");
		if (javaRuntimeVersion.compareTo("1.8.0") < 0) {
			System.out.println(controllerExcFinish.getTextResourceServiceExc().getText(locale, EXC_ERROR_WRONG_JRE));
			return false;
		}

		System.out.println("");

		/**
		 * 4) finish: Config-Kopie sowie Workverzeichnis loeschen
		 * 
		 * TODO: Erledigt
		 */

		System.out.println("SIARDexcerpt: finish");

		// Loeschen des Arbeitsverzeichnisses und confiFileHard, falls eines
		// angelegt wurde
		if (tmpDir.exists()) {
			Util.deleteDir(tmpDir);
		}
		if (configFileHard.exists()) {
			Util.deleteDir(configFileHard);
		}
		finish = true;
		// End finish
		return finish;

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
