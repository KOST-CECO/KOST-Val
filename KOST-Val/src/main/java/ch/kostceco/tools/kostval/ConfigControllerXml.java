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

public class ConfigControllerXml {

	@FXML
	private CheckBox checkSyntax, checkSchema;

	@FXML
	private Button buttonConfigApply;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	@SuppressWarnings("unused")
	private String dirOfJarPath, config, minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label labelSchema, labelVal, labelMessage, labelConfig, labelSyntax;

	@FXML
	void initialize() {

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright ausgeben
		labelConfig.setText("Copyright © KOST/CECO" );

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

		labelMessage.setText("");

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if (Util.stringInFileLine("kostval-conf-DE.xsl", configFile)) {
				labelSchema.setText("Schema-Validierung");
				labelSyntax.setText("Syntax-Validierung (Wohlgeformt)");
				labelVal.setText("Validierungseinstellung: XML");
				buttonConfigApply.setText("anwenden");
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelSchema.setText("Validation du schéma") ;
				labelSyntax.setText("Validation de la syntaxe (bien formé)") ;
				labelVal.setText("Paramètre de validation: XML");
				buttonConfigApply.setText("appliquer");
				minOne = "Au moins une variante doit etre autorisee !";
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelSchema.setText("Convalida dello schema");
				labelSyntax.setText("Convalida della sintassi (ben formata)");
				labelVal.setText("Parametro di convalida: XML");
				buttonConfigApply.setText("Applica");
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelSchema.setText("Schema validation");
				labelSyntax.setText("Syntax validation (well-formed)");
				labelVal.setText("Validation setting: XML");
				buttonConfigApply.setText("apply");
				minOne = "At least one variant must be allowed!";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	// Syntax immer eingeschaltet, kann nicht geaendert werden	
		checkSyntax.setDisable(true);
		checkSyntax.setSelected(true);

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			byte[] encoded;
			encoded = Files.readAllBytes(Paths.get(configFile.getAbsolutePath()));
			config = new String(encoded, StandardCharsets.UTF_8);
			String noXml = "<xmlvalidation>no</xmlvalidation>";
			String noXmlSchema = "<schema>no</schema>";

			if (config.contains(noXml)) {
				checkSchema.setDisable(true);
			} else {
				checkSchema.setDisable(false);
			}
			
			if (config.contains(noXmlSchema)) {
				checkSchema.setSelected(false);
			} else {
				checkSchema.setSelected(true);
			}

		} catch (IOException e1) {
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
		// engine.loadContent( "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* TODO --> CheckBox ================= */

	/*
	 * checkSchema schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSchema(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<schema>yes</schema>";
		String no = "<schema>no</schema>";
		try {
			if (checkSchema.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
					Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}