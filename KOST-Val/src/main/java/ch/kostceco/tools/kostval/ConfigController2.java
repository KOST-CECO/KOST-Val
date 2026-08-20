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
import java.util.Locale;
import java.util.Optional;

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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ConfigController2 {

	private File configFileBackup = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x"
			+ File.separator + "configuration" + File.separator + "BACKUP.kostval.conf.xml");

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, inputString, workString, config;

	private String versionKostVal = "   (v2.4.0.3)";

	private Locale locale = Locale.getDefault();

	@FXML
	private Button buttonConfigApply;


	@FXML
	private Label labelConfig2, labelSignatur, labelDv, labelEgovDV, labelWarning, labelSize, labelWork, labelInstitution,
			labelInput, labelHint, labelHint1, labelConfig;

	@FXML
	private Button buttonDv, buttonDvVal, buttonWork,
			buttonInput, buttonInstitution;

	ObservableList<String> hashAlgoList = FXCollections.observableArrayList("MD5", "SHA-1", "SHA-256", "SHA-512", "");
	@FXML
	private ChoiceBox<String> hashAlgo;

	ObservableList<String> sizeWarningList = FXCollections.observableArrayList("", "0.5KB", "1KB", "5KB");

	@FXML
	private ChoiceBox<String> sizeWarning;

	@FXML
	void initialize() {

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright ausgeben
		labelConfig.setText("Copyright © KOST/CECO");

		// Original Config Kopieren
		try {
			Util.copyFile(configFile, configFileBackup);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

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

		hashAlgo.getItems().addAll(hashAlgoList);
		sizeWarning.getItems().addAll(sizeWarningList);
		try {
			Document docH = null;
			BufferedInputStream bisH = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbfH = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbH = dbfH.newDocumentBuilder();
			docH = dbH.parse(bisH);
			docH.normalize();
			String hashAlgoInit = "";
			hashAlgoInit = docH.getElementsByTagName("hash").item(0).getTextContent();
			bisH.close();
			docH = null;
			hashAlgo.setValue(hashAlgoInit);
			Document docS = null;
			BufferedInputStream bisS = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbfS = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbS = dbfS.newDocumentBuilder();
			docS = dbS.parse(bisS);
			docS.normalize();
			String sizeWarningInit = "";
			sizeWarningInit = docS.getElementsByTagName("sizeWarning").item(0).getTextContent();
			bisS.close();
			docS = null;
			sizeWarning.setValue(sizeWarningInit);
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if (Util.stringInFileLine("kostval-conf-DE.xsl", configFile)) {
				locale = new Locale("de");
				labelConfig2.setText("Weitere Konfigurationen");
				buttonConfigApply.setText("Anwenden");
				labelEgovDV.setText("Prüfung von el. Signaturen in PDF/A- und PDF-Dateien (Lizenz erforderlich)");
				labelWarning.setText("Dateigrösse");
				labelSize.setText("Warnung ausgeben, wenn die Datei kleiner als die ausgewählte Dateigrösse ist");
				buttonWork.setText("Arbeitsverzeichnis");
				buttonInput.setText("Inputverzeichnis");
				labelInstitution.setText("Institution");
				labelHint1.setText("Hinweis:");
				labelHint.setText("öffnet die jeweilige Detailkonfiguration");
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				locale = new Locale("fr");
				labelConfig2.setText("Autres configurations");
				buttonConfigApply.setText("Appliquer");
				labelEgovDV.setText("Vérification des signatures él. dans PDF/A et PDF (licence requise)");
				labelWarning.setText("Taille");
				labelSize.setText(
						"Afficher un avertissement si le fichier est plus petit que la taille de fichier sélectionnée");
				buttonWork.setText("Répertoire de travail");
				buttonInput.setText("Répertoire d'entrée");
				labelInstitution.setText("Institution");
				labelHint1.setText("Remarque :");
				labelHint.setText("ouvre la configuration détaillée correspondante");
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				locale = new Locale("it");
				labelConfig2.setText("Altre configurazioni");
				buttonConfigApply.setText("Applica");
				labelEgovDV.setText("Verifica delle firme el. nei file PDF/A e PDF (licenza necessaria)");
				labelWarning.setText("Dimensione");
				labelSize.setText("Visualizza l'avviso se il file è più piccolo della dimensione selezionata");
				buttonWork.setText("Directory di lavoro");
				buttonInput.setText("Directory di input");
				labelInstitution.setText("Istituzione");
				labelHint1.setText("Nota:");
				labelHint.setText("apre la configurazione dettagliata corrispondente");
			} else {
				locale = new Locale("en");
				labelConfig2.setText("Additional Configurations");
				buttonConfigApply.setText("Apply");
				labelEgovDV.setText("Verification of el. signatures in PDF/A and PDF files (license required)");
				labelWarning.setText("File size");
				labelSize.setText("Display warning if the file is smaller than the selected file size");
				buttonWork.setText("Working directory");
				buttonInput.setText("Input directory");
				labelInstitution.setText("Institution");
				labelHint1.setText("Note:");
				labelHint.setText("opens the respective detailed configuration");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			byte[] encoded;
			encoded = Files.readAllBytes(Paths.get(configFile.getAbsolutePath()));
			config = new String(encoded, StandardCharsets.UTF_8);

			String noDv = "<egovdvvalidation>&#x2717;</egovdvvalidation>";
			String yesDv = "<egovdvvalidation>&#x2713;</egovdvvalidation>";


			if (config.contains(noDv)) {
				buttonDvVal.setDisable(true);
				buttonDv.setText("✗");
				buttonDv.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else if (config.contains(yesDv)) {
				buttonDvVal.setDisable(false);
				buttonDv.setText("✓");
				buttonDv.setStyle("-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke");
			} else {
				buttonDvVal.setDisable(false);
				buttonDv.setText("✓");
				buttonDv.setStyle("-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke");
			}

			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			/*
			 * stringPuid = doc.getElementsByTagName( "otherformats" ).item( 0 )
			 * .getTextContent();
			 */
			workString = doc.getElementsByTagName("pathtoworkdir").item(0).getTextContent();
			labelWork.setText(workString);
			inputString = doc.getElementsByTagName("standardinputdir").item(0).getTextContent();
			labelInput.setText(inputString);
			String institutionInit = doc.getElementsByTagName("Institution").item(0).getTextContent();
			buttonInstitution.setText(institutionInit);

		} catch (IOException | SAXException | ParserConfigurationException e1) {
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

	/* TODO --> Button Exit ================= */

	@FXML
	void configApply(ActionEvent e) {
		int countCheck;
		try {
			countCheck = Util.stringCountInFile("<pdfa>", configFile);
			if (countCheck != 1) {
				String info1 = "Die Änderung der Konfiguration ergab einen Fehler. ";
				String info2 = "Die Änderung wurde nicht übernommen sondern abgebrochen.";
				if (locale.toString().startsWith("fr")) {
					info1 = "La modification de la configuration a généré une erreur. ";
					info2 = "La modification n'a pas été enregistrée et a été annulée.";
				} else if (locale.toString().startsWith("en")) {
					info1 = "The configuration change resulted in an error. ";
					info2 = "The change was not applied but was aborted.";
				} else if (locale.toString().startsWith("it")) {
					info1 = "La modifica alla configurazione ha generato un errore. ";
					info2 = "La modifica non è stata applicata, ma è stata interrotta.";
				}
				Alert alertInfo = new Alert(AlertType.INFORMATION);
				alertInfo.setTitle(info1);
				alertInfo.setHeaderText(null);
				alertInfo.setContentText(info1 + info2);
				alertInfo.initStyle(StageStyle.UTILITY);
				alertInfo.showAndWait();

				configFile.delete();
				try {
					Util.copyFile(configFileBackup, configFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Util.deleteFile(configFileBackup);
				((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
			} else {
				Util.deleteFile(configFileBackup);
				((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@FXML
	void chooseWork(ActionEvent e) {
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			String workInit = "";
			if (!config.contains("<pathtoworkdir></pathtoworkdir>")) {
				workInit = doc.getElementsByTagName("pathtoworkdir").item(0).getTextContent();
			}
			bis.close();
			doc = null;
			String pathtoworkdir = "<pathtoworkdir>" + workInit + "</pathtoworkdir>";
			DirectoryChooser folderChooser = new DirectoryChooser();
			if (locale.toString().startsWith("fr")) {
				folderChooser.setTitle("Choisissez le dossier");
			} else if (locale.toString().startsWith("it")) {
				folderChooser.setTitle("Selezionare la directory");
			} else if (locale.toString().startsWith("en")) {
				folderChooser.setTitle("Choose the folder");
			} else {
				folderChooser.setTitle("Wählen Sie den Ordner");
			}
			File workFolder = folderChooser.showDialog(new Stage());
			if (workFolder != null) {
				labelWork.setText(workFolder.getAbsolutePath());
				String pathtoworkdirNew = "<pathtoworkdir>" + workFolder.getAbsolutePath() + "</pathtoworkdir>";
				Util.oldnewstring(pathtoworkdir, pathtoworkdirNew, configFile);
			}
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}

	/* Wenn chooseInput betaetigt wird, kann ein Ordner ausgewaehlt werden */
	@FXML
	void chooseInput(ActionEvent e) {
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			String inputInit = "";
			if (!config.contains("<standardinputdir></standardinputdir>")) {
				inputInit = doc.getElementsByTagName("standardinputdir").item(0).getTextContent();
			}
			bis.close();
			doc = null;
			String standardinputdir = "<standardinputdir>" + inputInit + "</standardinputdir>";
			DirectoryChooser folderChooser = new DirectoryChooser();
			if (locale.toString().startsWith("fr")) {
				folderChooser.setTitle("Choisissez le dossier");
			} else if (locale.toString().startsWith("it")) {
				folderChooser.setTitle("Selezionare la directory");
			} else if (locale.toString().startsWith("en")) {
				folderChooser.setTitle("Choose the folder");
			} else {
				folderChooser.setTitle("Wählen Sie den Ordner");
			}
			File inputFolder = folderChooser.showDialog(new Stage());
			if (inputFolder != null) {
				labelInput.setText(inputFolder.getAbsolutePath());
				String standardinputdirNew = "<standardinputdir>" + inputFolder.getAbsolutePath()
						+ "</standardinputdir>";
				Util.oldnewstring(standardinputdir, standardinputdirNew, configFile);
			}
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}

	/* changeDv schaltet zwischen x V herum */
	@FXML
	void changeDv(ActionEvent event) {
		String yes = "<egovdvvalidation>&#x2713;</egovdvvalidation>";
		String no = "<egovdvvalidation>&#x2717;</egovdvvalidation>";
		try {
			String optButton = buttonDv.getText();
			if (optButton.equals("✗")) {
				buttonDvVal.setDisable(false);
				Util.oldnewstring(no, yes, configFile);
				buttonDv.setText("✓");
				buttonDv.setStyle("-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke");
			} else {
				buttonDvVal.setDisable(true);
				Util.oldnewstring(yes, no, configFile);
				buttonDv.setText("✗");
				buttonDv.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Mit changeDvVal wird die Signatur-Haupteinstellung umgestellt
	@FXML
	void changeDvVal(ActionEvent eventpdfa) {
		try {
			StackPane dvLayout = new StackPane();

			dvLayout = FXMLLoader.load(getClass().getResource("ConfigViewDv.fxml"));
			Scene dvScene = new Scene(dvLayout);
			dvScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			// New window (Stage)
			Stage dvStage = new Stage();

			dvStage.setTitle("KOST-Val   -   Configuration   -    eGov diskret Signaturvalidator" + versionKostVal);
			Image kostvalIcon = new Image(
					"file:" + dirOfJarPath + File.separator + "doc" + File.separator + "valicon.png");
			// Image kostvalIcon = new Image( "file:valicon.png" );
			dvStage.initModality(Modality.APPLICATION_MODAL);
			dvStage.getIcons().add(kostvalIcon);
			dvStage.setScene(dvScene);
			dvStage.setOnCloseRequest(event -> {
				// hier engeben was beim schliessen gemacht werden soll
			});
			dvStage.show();
			dvStage.setOnHiding(event -> {
				// hier engeben was beim schliessen gemacht werden soll
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/* Wenn Aenderungen an changeInstitution gemacht wird, wird es ausgeloest */
	@FXML
	void changeInstitution(ActionEvent event) {
		String stringInstitution = buttonInstitution.getText();
		// create a TextInputDialog mit der Texteingabe der Puid
		TextInputDialog dialog = new TextInputDialog(stringInstitution);

		// Set title & header text
		String stringInstitutionInit = stringInstitution;

		dialog.setTitle("KOST-Val - Configuration");
		String headerDeFrItEn = "Name der Institution [Archiv]:";
		if (locale.toString().startsWith("fr")) {
			headerDeFrItEn = "Nom de l'institution [Archiv] :";
		} else if (locale.toString().startsWith("it")) {
			headerDeFrItEn = "Nome dell'istituzione [Archiv]:";
		} else if (locale.toString().startsWith("en")) {
			headerDeFrItEn = "Name of the institution [Archiv]:";
		}
		dialog.setHeaderText(headerDeFrItEn);
		dialog.setContentText("");

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringInstitutionNew = "";
		if (result.isPresent()) {
			try {
				stringInstitutionNew = result.get();
				buttonInstitution.setText(stringInstitutionNew);
				String allowedformats = "<Institution>" + stringInstitutionInit + "</Institution>";
				String allowedformatsNew = "<Institution>" + stringInstitutionNew + "</Institution>";
				Util.oldnewstring(allowedformats, allowedformatsNew, configFile);
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		} else {
			// Keine Aktion
		}
	}

	/* TODO --> ChoiceBox ================= */
	// Mit changeHashAlgo wird die Hash-Auswahl umgestellt
	@FXML
	void changeHashAlgo(ActionEvent event) {
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			String hashAlgoInit = "";
			hashAlgoInit = doc.getElementsByTagName("hash").item(0).getTextContent();
			bis.close();
			doc = null;
			String hashAlgoOld = "<hash>" + hashAlgoInit + "</hash>";
			String selHashType = hashAlgo.getValue();
			String hashAlgoNew = "<hash></hash>";
			if (selHashType.equals("MD5") || selHashType.equals("SHA-1") || selHashType.equals("SHA-256")
					|| selHashType.equals("SHA-512")) {
				hashAlgoNew = "<hash>" + selHashType + "</hash>";
				hashAlgo.setValue(selHashType);
			} else {
				hashAlgoNew = "<hash></hash>";
				hashAlgo.setValue("");
			}
			Util.oldnewstring(hashAlgoOld, hashAlgoNew, configFile);
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}

	// Mit changeSizeWarning wird die Warnung kleiner Dateien umgestellt

	@FXML
	void changeSizeWarning(ActionEvent event) {
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();
			String sizeWarningInit = "";
			sizeWarningInit = doc.getElementsByTagName("sizeWarning").item(0).getTextContent();
			bis.close();
			doc = null;
			String sizeWarningOld = "<sizeWarning>" + sizeWarningInit + "</sizeWarning>";
			String selSizeWarning = sizeWarning.getValue();
			String sizeWarningNew = sizeWarningOld;
			if (selSizeWarning.equals("0.5KB")) {
				sizeWarningNew = "<sizeWarning>" + selSizeWarning + "</sizeWarning>";
			} else if (selSizeWarning.equals("1KB")) {
				sizeWarningNew = "<sizeWarning>" + selSizeWarning + "</sizeWarning>";
			} else if (selSizeWarning.equals("5KB")) {
				sizeWarningNew = "<sizeWarning>" + selSizeWarning + "</sizeWarning>";
			} else {
				sizeWarningNew = "<sizeWarning></sizeWarning>";
			}
			Util.oldnewstring(sizeWarningOld, sizeWarningNew, configFile);
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			e1.printStackTrace();
		}
	}

}