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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfigControllerDv {

	@FXML
	private CheckBox checkMixed, checkQualified, checkSwissGovPKI, checkUpregfn, checkSiegel, checkAmtsblattportal,
			checkEdec, checkESchKG, checkFederalLaw, checkStrafregisterauszug, checkKantonZugFinanzdirektion;

	@FXML
	private Button buttonConfigApply;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	@SuppressWarnings("unused")
	private String dirOfJarPath, config, minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label labelInstitut, labelVal, labelMessage, labelMandant, labelConfig, labelStar, labelStar1;

	ObservableList<String> sizeInstitutList = FXCollections.observableArrayList("", "Staatsarchiv Aargau",
			"Staatsarchiv Basel-Stadt", "Staatsarchiv Bern", "Staatsarchiv Luzern", "Staatsarchiv St. Gallen",
			"Staatsarchiv Thurgau", "Stadtarchiv Bern", "Stadtarchiv Luzern", "Stadtarchiv St. Gallen",
			"Burgerbibliothek Bern");

	@FXML
	private ChoiceBox<String> institut;

	/*
	 * ObservableList<String> sizeInstitutList = FXCollections .observableArrayList(
	 * "", "andere", "Staatsarchiv Aargau", "Staatsarchiv Appenzell Ausserrhoden",
	 * "Landesarchiv Appenzell Innerrhoden", "Staatsarchiv Basel-Land",
	 * "Staatsarchiv Basel-Stadt", "Staatsarchiv Bern", "Staatsarchiv Freiburg",
	 * "Archives de l'Etat de Genève", "Landesarchiv Glarus",
	 * "Staatsarchiv Graubünden", "Archives cantonales jurassiennes",
	 * "Staatsarchiv Luzern", "Archives de l'Etat de Neuchâtel",
	 * "Staatsarchiv Nidwalden", "Staatsarchiv Obwalden", "Staatsarchiv Solothurn",
	 * "Staatsarchiv St. Gallen", "Staatsarchiv Schaffhausen",
	 * "Staatsarchiv Schwyz", "Staatsarchiv Thurgau",
	 * "Archivio di Stato del Cantone Ticino", "Staatsarchiv Uri",
	 * "Archives cantonales vaudoises", "Archives de l'Etat du Valais",
	 * "Staatsarchiv Zug", "Staatsarchiv Zürich", "Schweizerisches Bundesarchiv",
	 * "Landesarchiv Fürstentum Liechtenstein", "Burgerbibliothek Bern",
	 * "Stadtarchiv Bern", "Stadtarchiv Luzern", "Stadtarchiv St. Gallen",
	 * "Stadtarchiv Zürich", "UZH Archiv", "KOST-Geschäftsstelle" );
	 */

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
				labelMandant.setText("Mandant");
				labelInstitut.setText("Institution");
				labelVal.setText("Einstellungen für eGov diskret Signaturvalidator");
				buttonConfigApply.setText("anwenden");
				minOne = "Mindestens eine Variante muss erlaubt sein!";
				labelStar.setText("* Wird immer geprüft, aber nur wenn es angewählt ist, wird der Report nicht gelöscht.");
				labelStar1.setText("° Wird nur geprüft, wenn es angewählt ist und der valide Report wird nicht gelöscht.");
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelMandant.setText("Mandant");
				labelInstitut.setText("Institution");
				labelVal.setText("Paramètres du validateur de signature discrète eGov");
				buttonConfigApply.setText("appliquer");
				minOne = "Au moins une variante doit etre autorisee !";
				labelStar.setText("* Toujours vérifié, mais le rapport n'est pas supprimé uniquement lorsqu'il est sélectionné.");
				labelStar1.setText("° N'est vérifié que s'il est sélectionné et le rapport valide n'est pas supprimé.");
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelMandant.setText("Cliente");
				labelInstitut.setText("Istituzione");
				labelVal.setText("Impostazioni per il validatore di firma discreta eGov");
				buttonConfigApply.setText("Applica");
				minOne = "Almeno una variante deve essere consentita!";
				labelStar.setText("* È sempre controllato, ma solo se è selezionato, il rapporto non viene eliminato.");
				labelStar1.setText("° Viene controllato solo se è selezionato e il rapporto valido non viene eliminato.");
			} else {
				labelMandant.setText("Client");
				labelInstitut.setText("Institution");
				labelVal.setText("Settings for eGov discrete signature validator");
				buttonConfigApply.setText("apply");
				minOne = "At least one variant must be allowed!";
				labelStar.setText("* Is always checked, but only if it is selected, the report is not deleted.");
				labelStar1.setText("° Is only checked if it is selected and the valid report is not deleted.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		institut.getItems().addAll(sizeInstitutList);
		try {
			Document docS = null;
			BufferedInputStream bisS = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbfS = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbS = dbfS.newDocumentBuilder();
			docS = dbS.parse(bisS);
			docS.normalize();
			String institutInit = "";
			institutInit = docS.getElementsByTagName("Institut").item(0).getTextContent();
			bisS.close();
			docS = null;
			institut.setValue(institutInit);
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			byte[] encoded;
			encoded = Files.readAllBytes(Paths.get(configFile.getAbsolutePath()));
			config = new String(encoded, StandardCharsets.UTF_8);
			String noMixed = "<Mixed>no</Mixed>";
			String noQualified = "<Qualified>no</Qualified>";
			String noSwissGovPKI = "<SwissGovPKI>no</SwissGovPKI>";
			String noUpregfn = "<Upregfn>no</Upregfn>";
			String noSiegel = "<Siegel>no</Siegel>";
			String noAmtsblattportal = "<Amtsblattportal>no</Amtsblattportal>";
			String noEdec = "<Edec>no</Edec>";
			String noESchKG = "<ESchKG>no</ESchKG>";
			String noFederalLaw = "<FederalLaw>no</FederalLaw>";
			String noStrafregisterauszug = "<Strafregisterauszug>no</Strafregisterauszug>";
			String noKantonZugFinanzdirektion = "<KantonZugFinanzdirektion>no</KantonZugFinanzdirektion>";

			if (config.contains(noMixed)) {
				checkMixed.setSelected(false);
			}
			if (config.contains(noQualified)) {
				checkQualified.setSelected(false);
			}
			if (config.contains(noSwissGovPKI)) {
				checkSwissGovPKI.setSelected(false);
			}
			if (config.contains(noUpregfn)) {
				checkUpregfn.setSelected(false);
			}
			if (config.contains(noSiegel)) {
				checkSiegel.setSelected(false);
			}
			if (config.contains(noAmtsblattportal)) {
				checkAmtsblattportal.setSelected(false);
			}
			if (config.contains(noEdec)) {
				checkEdec.setSelected(false);
			}
			if (config.contains(noESchKG)) {
				checkESchKG.setSelected(false);
			}
			if (config.contains(noFederalLaw)) {
				checkFederalLaw.setSelected(false);
			}
			if (config.contains(noStrafregisterauszug)) {
				checkStrafregisterauszug.setSelected(false);
			}
			if (config.contains(noKantonZugFinanzdirektion)) {
				checkKantonZugFinanzdirektion.setSelected(false);
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
	 * checkMixed schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeMixed(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Mixed>yes</Mixed>";
		String no = "<Mixed>no</Mixed>";
		try {
			if (checkMixed.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkQualified schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeQualified(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Qualified>yes</Qualified>";
		String no = "<Qualified>no</Qualified>";
		try {
			if (checkQualified.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkSwissGovPKI schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSwissGovPKI(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<SwissGovPKI>yes</SwissGovPKI>";
		String no = "<SwissGovPKI>no</SwissGovPKI>";
		try {
			if (checkSwissGovPKI.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkUpregfn schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeUpregfn(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Upregfn>yes</Upregfn>";
		String no = "<Upregfn>no</Upregfn>";
		try {
			if (checkUpregfn.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkSiegel schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSiegel(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Siegel>yes</Siegel>";
		String no = "<Siegel>no</Siegel>";
		try {
			if (checkSiegel.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkAmtsblattportal schaltet diesen Mandant in der Konfiguration ein oder
	 * aus
	 */
	@FXML
	void changeAmtsblattportal(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Amtsblattportal>yes</Amtsblattportal>";
		String no = "<Amtsblattportal>no</Amtsblattportal>";
		try {
			if (checkAmtsblattportal.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkEdec schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeEdec(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Edec>yes</Edec>";
		String no = "<Edec>no</Edec>";
		try {
			if (checkEdec.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkESchKG schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeESchKG(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<ESchKG>yes</ESchKG>";
		String no = "<ESchKG>no</ESchKG>";
		try {
			if (checkESchKG.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkFederalLaw schaltet diesen Mandant in der Konfiguration ein oder aus
	 */
	@FXML
	void changeFederalLaw(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<FederalLaw>yes</FederalLaw>";
		String no = "<FederalLaw>no</FederalLaw>";
		try {
			if (checkFederalLaw.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkStrafregisterauszug schaltet diesen Mandant in der Konfiguration ein
	 * oder aus
	 */
	@FXML
	void changeStrafregisterauszug(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<Strafregisterauszug>yes</Strafregisterauszug>";
		String no = "<Strafregisterauszug>no</Strafregisterauszug>";
		try {
			if (checkStrafregisterauszug.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkKantonZugFinanzdirektion schaltet diesen Mandant in der Konfiguration
	 * ein oder aus
	 */
	@FXML
	void changeKantonZugFinanzdirektion(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<KantonZugFinanzdirektion>yes</KantonZugFinanzdirektion>";
		String no = "<KantonZugFinanzdirektion>no</KantonZugFinanzdirektion>";
		try {
			if (checkKantonZugFinanzdirektion.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeInstitut wird die Warnung kleiner Dateien umgestellt

	@FXML
	void changeInstitut(ActionEvent event) {
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			String institutInit = "";
			institutInit = doc.getElementsByTagName("Institut").item(0).getTextContent();
			bis.close();
			doc = null;
			String institutOld = "<Institut>" + institutInit + "</Institut>";
			String selInstitut = institut.getValue();
			String institutNew = institutOld;
			institutNew = "<Institut>" + selInstitut + "</Institut>";
			Util.oldnewstring(institutOld, institutNew, configFile);

		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}
}