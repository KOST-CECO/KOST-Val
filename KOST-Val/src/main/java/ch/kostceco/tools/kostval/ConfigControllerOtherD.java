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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import ch.kostceco.tools.kosttools.util.Util;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfigControllerOtherD {

	@FXML
	private CheckBox checkDataRep, checkXlsRep;

	@FXML
	private Button buttonConfigApply, buttonXls, buttonOds;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, config;

	@FXML
	private Label labelOtherD, labelConfig, labelRepD;

	@FXML
	void initialize() {

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright ausgeben
		labelConfig.setText("Copyright © KOST/CECO");

		// festhalten von wo die Applikation (exe) gestartet wurde
		dirOfJarPath = "";
		try {
			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist eine
			 * generelle Aufgabe in allen Modulen. Zuerst immer dirOfJarPath ermitteln und
			 * dann alle Pfade mit dirOfJarPath + File.separator + erweitern.
			 */
			String path = new File("").getAbsolutePath();
			dirOfJarPath = path;
			setLibraryPath(dirOfJarPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if (Util.stringInFileLine("kostval-conf-DE.xsl", configFile)) {
				labelOtherD.setText("Einstellungen weitere Daten-Formate");
				labelRepD.setText("Reparatureinstellungen: Daten-Formate");
				checkDataRep.setText(
						"ich übernehme die Verantwortung für die Qualitätssicherung, Dokumentation und deren Weiterverwendung");
				buttonConfigApply.setText("anwenden");
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelOtherD.setText("Paramètres pour d'autres formats de données");
				labelRepD.setText("Paramètres de réparation: Daten-Formate");
				checkDataRep.setText(
						"j'assume la responsabilité de l'assurance qualité, de la documentation et de leur réutilisation");
				buttonConfigApply.setText("appliquer");
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelOtherD.setText("Impostazioni di altri formati di dati");
				labelRepD.setText("Parametro di riparazione: Daten-Formate");
				checkDataRep.setText(
						"Mi assumo la responsabilità della garanzia di qualità, della documentazione e del suo ulteriore utilizzo");
				buttonConfigApply.setText("Applica");
			} else {
				labelOtherD.setText("Settings other data formats");
				labelRepD.setText("Repair settings: Daten-Formate");
				checkDataRep.setText("I take responsibility for quality assurance, documentation and its further use");
				buttonConfigApply.setText("apply");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			byte[] encoded;
			encoded = Files.readAllBytes(Paths.get(configFile.getAbsolutePath()));
			config = new String(encoded, StandardCharsets.UTF_8);
			// <otherformats>WARC HTML DWG</otherformats>
			String noOds = "<odsvalidation></odsvalidation>";
			String noXls = "<xlsvalidation></xlsvalidation>";
			String noDataRep = "<datarep>no </datarep>";
			String noXlsRep = "<dataxlsrep>no </dataxlsrep>";

			// TODO: bei Controllervalfofile ca Zeile 80 muss die Aenderung
			// neue oder entfernte Formate nachgetragen werden. Damit das Format
			// in der Header-Zeile des Logs erscheint.

			if (config.contains(noOds)) {
				buttonOds.setText("✗");
				buttonOds.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonOds.setText("(✓)");
				buttonOds.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noXls)) {
				buttonXls.setText("✗");
				buttonXls.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonXls.setText("(✓)");
				buttonXls.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}

			if (config.contains(noDataRep)) {
				checkDataRep.setSelected(false);
				checkXlsRep.setDisable(true);
			}

			if (config.contains(noXlsRep)) {
				checkXlsRep.setSelected(false);
			}

		} catch (IOException/* | ParserConfigurationException | SAXException */ e1) {
			e1.printStackTrace();
		}
	}

	public static void setLibraryPath(String path) throws Exception {
		System.setProperty("java.library.path", path);
		// set sys_paths to null so that java.library.path will be reevalueted
		// next time it is needed
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);
	}

	/* TODO --> Button ================= */

	@FXML
	void configApply(ActionEvent e) {
		// labelMessage.setText(minOne ); "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* change... schaltet zwischen x (v) herum */
	@FXML
	void changeOds(ActionEvent event) {
		String az = "<odsvalidation>ODS </odsvalidation>";
		String no = "<odsvalidation></odsvalidation>";
		try {
			String optButton = buttonOds.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonOds.setText("(✓)");
				buttonOds.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonOds.setText("✗");
				buttonOds.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeXls(ActionEvent event) {
		String az = "<xlsvalidation>XLS </xlsvalidation>";
		String no = "<xlsvalidation></xlsvalidation>";
		try {
			String optButton = buttonXls.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonXls.setText("(✓)");
				buttonXls.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonXls.setText("✗");
				buttonXls.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkDataRep aendert die Kontrolle ob Reparatur akzeptiert oder nicht in der
	 * Konfiguration ein oder aus
	 */
	@FXML
	void changeDataRep(ActionEvent event) {
		// labelMessage.setText("");
		String yes = "<datarep>yes </datarep>";
		String no = "<datarep>no </datarep>";
		try {
			if (checkDataRep.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkXlsRep.setDisable(false);
			} else {
				Util.oldnewstring(yes, no, configFile);
				checkXlsRep.setDisable(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkXlsRep aendert die Reparatur in xls nach xlsx in der Konfiguration ein
	 * oder aus
	 */
	@FXML
	void changeXlsRep(ActionEvent event) {
		// labelMessage.setText("");
		String yes = "<dataxlsrep>yes </dataxlsrep>";
		String no = "<dataxlsrep>no </dataxlsrep>";
		try {
			if (checkXlsRep.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}