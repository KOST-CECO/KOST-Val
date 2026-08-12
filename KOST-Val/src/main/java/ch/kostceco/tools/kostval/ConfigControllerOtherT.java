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
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfigControllerOtherT {

	@FXML
	private Button buttonConfigApply, buttonDocx, buttonPptx, buttonRtf;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, config;

	@FXML
	private Label labelOtherT, labelConfig;

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
				labelOtherT.setText("Einstellungen weitere Text-Formate");
				buttonConfigApply.setText("anwenden");
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelOtherT.setText("Paramètres pour d'autres formats de texte");
				buttonConfigApply.setText("appliquer");
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelOtherT.setText("Impostazioni di altri formati di testo");
				buttonConfigApply.setText("Applica");
			} else {
				labelOtherT.setText("Settings other text formats");
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
			String noDocx = "<docxvalidation></docxvalidation>";
			String noPptx = "<pptxvalidation></pptxvalidation>";
			String noRtf = "<rtfvalidation></rtfvalidation>";

			// TODO: bei Controllervalfofile ca Zeile 80 muss die Aenderung
			// neue oder entfernte Formate nachgetragen werden. Damit das Format
			// in der Header-Zeile des Logs erscheint.

			if (config.contains(noDocx)) {
				buttonDocx.setText("✗");
				buttonDocx.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonDocx.setText("(✓)");
				buttonDocx.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noPptx)) {
				buttonPptx.setText("✗");
				buttonPptx.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonPptx.setText("(✓)");
				buttonPptx.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noRtf)) {
				buttonRtf.setText("✗");
				buttonRtf.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonRtf.setText("(✓)");
				buttonRtf.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
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
	void changeDocx(ActionEvent event) {
		String az = "<docxvalidation>DOCX </docxvalidation>";
		String no = "<docxvalidation></docxvalidation>";
		try {
			String optButton = buttonDocx.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonDocx.setText("(✓)");
				buttonDocx.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonDocx.setText("✗");
				buttonDocx.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePptx(ActionEvent event) {
		String az = "<pptxvalidation>PPTX </pptxvalidation>";
		String no = "<pptxvalidation></pptxvalidation>";
		try {
			String optButton = buttonPptx.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonPptx.setText("(✓)");
				buttonPptx.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonPptx.setText("✗");
				buttonPptx.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeRtf(ActionEvent event) {
		String az = "<rtfvalidation>RTF </rtfvalidation>";
		String no = "<rtfvalidation></rtfvalidation>";
		try {
			String optButton = buttonRtf.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonRtf.setText("(✓)");
				buttonRtf.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonRtf.setText("✗");
				buttonRtf.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}