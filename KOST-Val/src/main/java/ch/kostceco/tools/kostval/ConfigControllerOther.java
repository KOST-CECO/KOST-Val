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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class ConfigControllerOther {

	@FXML
	private Button buttonConfigApply, buttonHtml, buttonArc, buttonWarc, buttonWacz, buttonInterlis, buttonDwg,
			buttonIfc, buttonMsg, buttonEml, buttonMbox, buttonPst, buttonDicom, buttonDxf, buttonOther2;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, config, stringPuid;

	private Locale locale;

	@FXML
	private Label labelOther, labelConfig, labelHyper, labelGis, labelCad, labelMedicine, labelMail, labelOther2;

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
				locale = new Locale("de");
				labelOther.setText("Formate von weiteren Formatkategorien");
				buttonConfigApply.setText("anwenden");
				labelHyper.setText("Hypertext");
				labelGis.setText("GIS");
				labelCad.setText("CAD/CAM");
				labelMedicine.setText("Medizin");
				labelMail.setText("Mail");
				labelOther2.setText("Weitere");
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				locale = new Locale("fr");
				labelOther.setText("Formats d'autres catégories de formats");
				buttonConfigApply.setText("appliquer");
				labelHyper.setText("Hypertexte");
				labelGis.setText("GIS");
				labelCad.setText("CAD/CAM");
				labelMedicine.setText("Médecine");
				labelMail.setText("Courriel");
				labelOther2.setText("Autres");
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				locale = new Locale("it");
				labelOther.setText("Formati di altre categorie di formati");
				buttonConfigApply.setText("Applica");
				labelHyper.setText("Ipertesto");
				labelGis.setText("GIS");
				labelCad.setText("CAD/CAM");
				labelMedicine.setText("Medicina");
				labelMail.setText("Mail");
				labelOther2.setText("Altri");
			} else {
				locale = new Locale("en");
				labelOther.setText("Formats from other format categories");
				buttonConfigApply.setText("apply");
				labelHyper.setText("Hypertext");
				labelGis.setText("GIS");
				labelCad.setText("CAD/CAM");
				labelMedicine.setText("Medicine");
				labelMail.setText("Mail");
				labelOther2.setText("Other");
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
			String noHtml = "<htmlvalidation></htmlvalidation>";
			String noWacz = "<waczvalidation></waczvalidation>";
			String noWarc = "<warcvalidation></warcvalidation>";
			String noArc = "<arcvalidation></arcvalidation>";
			String noDwg = "<dwgvalidation></dwgvalidation>";
			String noIfc = "<ifcvalidation></ifcvalidation>";
			String noDxf = "<dxfvalidation></dxfvalidation>";
			String noInterlis = "<interlisvalidation></interlisvalidation>";
			String noDicom = "<dicomvalidation></dicomvalidation>";
			String noMsg = "<msgvalidation></msgvalidation>";
			String noEml = "<emlvalidation></emlvalidation>";
			String noMbox = "<mboxvalidation></mboxvalidation>";
			String noPst = "<pstvalidation></pstvalidation>";

			// TODO: bei Controllervalfofile ca Zeile 80 muss die Aenderung
			// neue oder entfernte Formate nachgetragen werden. Damit das Format
			// in der Header-Zeile des Logs erscheint.

			if (config.contains(noHtml)) {
				buttonHtml.setText("✗");
				buttonHtml.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonHtml.setText("(✓)");
				buttonHtml.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noWacz)) {
				buttonWacz.setText("✗");
				buttonWacz.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonWacz.setText("(✓)");
				buttonWacz.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noWarc)) {
				buttonWarc.setText("✗");
				buttonWarc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonWarc.setText("(✓)");
				buttonWarc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noArc)) {
				buttonArc.setText("✗");
				buttonArc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonArc.setText("(✓)");
				buttonArc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noInterlis)) {
				buttonInterlis.setText("✗");
				buttonInterlis.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonInterlis.setText("(✓)");
				buttonInterlis.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noDwg)) {
				buttonDwg.setText("✗");
				buttonDwg.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonDwg.setText("(✓)");
				buttonDwg.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noIfc)) {
				buttonIfc.setText("✗");
				buttonIfc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonIfc.setText("(✓)");
				buttonIfc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noDxf)) {
				buttonDxf.setText("✗");
				buttonDxf.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonDxf.setText("(✓)");
				buttonDxf.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noDicom)) {
				buttonDicom.setText("✗");
				buttonDicom.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonDicom.setText("(✓)");
				buttonDicom.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noMsg)) {
				buttonMsg.setText("✗");
				buttonMsg.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonMsg.setText("(✓)");
				buttonMsg.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noEml)) {
				buttonEml.setText("✗");
				buttonEml.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonEml.setText("(✓)");
				buttonEml.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noMbox)) {
				buttonMbox.setText("✗");
				buttonMbox.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonMbox.setText("(✓)");
				buttonMbox.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			if (config.contains(noPst)) {
				buttonPst.setText("✗");
				buttonPst.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			} else {
				buttonPst.setText("(✓)");
				buttonPst.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			}
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(configFile));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(bis);
			doc.normalize();

			stringPuid = doc.getElementsByTagName("othervalidation").item(0).getTextContent();
			buttonOther2.setText(stringPuid);
			bis.close();
		} catch (IOException | ParserConfigurationException | SAXException e1) {
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
	void changeHtml(ActionEvent event) {
		String az = "<htmlvalidation>HTML </htmlvalidation>";
		String no = "<htmlvalidation></htmlvalidation>";
		try {
			String optButton = buttonHtml.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonHtml.setText("(✓)");
				buttonHtml.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonHtml.setText("✗");
				buttonHtml.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeWacz(ActionEvent event) {
		String az = "<waczvalidation>WACZ </waczvalidation>";
		String no = "<waczvalidation></waczvalidation>";
		try {
			String optButton = buttonWacz.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonWacz.setText("(✓)");
				buttonWacz.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonWacz.setText("✗");
				buttonWacz.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeWarc(ActionEvent event) {
		String az = "<warcvalidation>WARC </warcvalidation>";
		String no = "<warcvalidation></warcvalidation>";
		try {
			String optButton = buttonWarc.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonWarc.setText("(✓)");
				buttonWarc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonWarc.setText("✗");
				buttonWarc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeArc(ActionEvent event) {
		String az = "<arcvalidation>ARC </arcvalidation>";
		String no = "<arcvalidation></arcvalidation>";
		try {
			String optButton = buttonArc.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonArc.setText("(✓)");
				buttonArc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonArc.setText("✗");
				buttonArc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDwg(ActionEvent event) {
		String az = "<dwgvalidation>DWG </dwgvalidation>";
		String no = "<dwgvalidation></dwgvalidation>";
		try {
			String optButton = buttonDwg.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonDwg.setText("(✓)");
				buttonDwg.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonDwg.setText("✗");
				buttonDwg.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeIfc(ActionEvent event) {
		String az = "<ifcvalidation>IFC </ifcvalidation>";
		String no = "<ifcvalidation></ifcvalidation>";
		try {
			String optButton = buttonIfc.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonIfc.setText("(✓)");
				buttonIfc.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonIfc.setText("✗");
				buttonIfc.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDxf(ActionEvent event) {
		String az = "<dxfvalidation>DXF </dxfvalidation>";
		String no = "<dxfvalidation></dxfvalidation>";
		try {
			String optButton = buttonDxf.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonDxf.setText("(✓)");
				buttonDxf.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonDxf.setText("✗");
				buttonDxf.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeInterlis(ActionEvent event) {
		String az = "<interlisvalidation>INTERLIS </interlisvalidation>";
		String no = "<interlisvalidation></interlisvalidation>";
		try {
			String optButton = buttonInterlis.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonInterlis.setText("(✓)");
				buttonInterlis.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonInterlis.setText("✗");
				buttonInterlis.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDicom(ActionEvent event) {
		String az = "<dicomvalidation>DICOM </dicomvalidation>";
		String no = "<dicomvalidation></dicomvalidation>";
		try {
			String optButton = buttonDicom.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonDicom.setText("(✓)");
				buttonDicom.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonDicom.setText("✗");
				buttonDicom.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMsg(ActionEvent event) {
		String az = "<msgvalidation>MSG </msgvalidation>";
		String no = "<msgvalidation></msgvalidation>";
		try {
			String optButton = buttonMsg.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonMsg.setText("(✓)");
				buttonMsg.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonMsg.setText("✗");
				buttonMsg.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeEml(ActionEvent event) {
		String az = "<emlvalidation>EML </emlvalidation>";
		String no = "<emlvalidation></emlvalidation>";
		try {
			String optButton = buttonEml.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonEml.setText("(✓)");
				buttonEml.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonEml.setText("✗");
				buttonEml.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMbox(ActionEvent event) {
		String az = "<mboxvalidation>MBOX </mboxvalidation>";
		String no = "<mboxvalidation></mboxvalidation>";
		try {
			String optButton = buttonMbox.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonMbox.setText("(✓)");
				buttonMbox.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonMbox.setText("✗");
				buttonMbox.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePst(ActionEvent event) {
		String az = "<pstvalidation>PST </pstvalidation>";
		String no = "<pstvalidation></pstvalidation>";
		try {
			String optButton = buttonPst.getText();
			if (optButton.equals("✗")) {
				Util.oldnewstring(no, az, configFile);
				buttonPst.setText("(✓)");
				buttonPst.setStyle("-fx-text-fill: Orange; -fx-background-color: WhiteSmoke");
			} else {
				Util.oldnewstring(az, no, configFile);
				buttonPst.setText("✗");
				buttonPst.setStyle("-fx-text-fill: Red; -fx-background-color: WhiteSmoke");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Wenn Aenderungen an changeOther2 gemacht wird, wird es ausgeloest */
	@FXML
	void changeOther2(ActionEvent event) {
		stringPuid = buttonOther2.getText();
		// create a TextInputDialog mit der Texteingabe der Puid
		TextInputDialog dialog = new TextInputDialog(stringPuid);

		// Set title & header text
		String puidIntInit = stringPuid;

		dialog.setTitle("KOST-Val - Configuration");
		String headerDeFrItEn = "Auflistung der weiteren akzeptierten Dateiformate []:";
		if (locale.toString().startsWith("fr")) {
			headerDeFrItEn = "Liste des autres formats de fichiers acceptés [] :";
		} else if (locale.toString().startsWith("it")) {
			headerDeFrItEn = "Elenco degli altri formati di file accettati []:";
		} else if (locale.toString().startsWith("en")) {
			headerDeFrItEn = "List of other accepted file formats []:";
		}
		dialog.setHeaderText(headerDeFrItEn);
		dialog.setContentText("");

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringPuidNew = "";
		if (result.isPresent()) {
			try {
				stringPuidNew = result.get();
				stringPuid = stringPuidNew;
				buttonOther2.setText(stringPuid);
				String allowedformats = "<othervalidation>" + puidIntInit + "</othervalidation>";
				String allowedformatsNew = "<othervalidation>" + stringPuidNew + "</othervalidation>";
				Util.oldnewstring(allowedformats, allowedformatsNew, configFile);
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		} else {
			// Keine Aktion
		}
	}

}