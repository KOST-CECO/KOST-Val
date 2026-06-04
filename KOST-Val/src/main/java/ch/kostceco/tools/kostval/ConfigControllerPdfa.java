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

public class ConfigControllerPdfa {

	@FXML
	private CheckBox checkPdfa, checkVerapdf, checkPdftools, checkPdfa1a, checkPdfa2a, checkFont, checkPdfaRep,
			checkPdfa2uRep,checkPdfa2bRep, checkJbig2, checkDetailpt, checkDetailvp, checkPdfa1b, checkPdfa2b, checkFontTol,
			checkPdfa2u, checkWarning3to2;

	@FXML
	private Button buttonConfigApply;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, config, minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label labelOtherPdfa, labelVersion, labelVal, labelRep, labelDms, labelDms2, labelMessage, labelConfig;

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

		labelMessage.setText("");

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if (Util.stringInFileLine("kostval-conf-DE.xsl", configFile)) {
				labelOtherPdfa.setText("Sonstiges");
				labelVersion.setText("Versionen");
				labelVal.setText("Validierungseinstellung: PDF/A");
				labelRep.setText("Reparatureinstellungen: PDF/A (nur über die Benutzeroberfläche)");
				checkPdfaRep.setText(
						"ich übernehme die Verantwortung für die Qualitätssicherung, Dokumentation und deren Weiterverwendung");
				checkPdfa2uRep.setText("invalide PDF/A- sowie nicht akzeptierte PDF-Dateien nach PDF/A-2u reparieren");
				checkPdfa2bRep.setText("wenn Reparatur nach PDFA-2u invalide, dann nach PDF/A-2b reparieren");
				labelDms.setText(
						"Reparatur bedingt die eigenständige Installation des 'dmstools PDF/A Converter' (v1.7.0.1) sowie die Aktivierung eurer Lizenz.");
				labelDms2.setText("Die integrierte KOST-Lizenz steht nur unseren Trägern und Supporter zur Verfügung.");
				buttonConfigApply.setText("anwenden");
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelOtherPdfa.setText("Divers");
				labelVersion.setText("Versions");
				labelVal.setText("Paramètre de validation: PDF/A");
				labelRep.setText("Paramètres de réparation: PDF/A (uniquement via l'interface graphique)");
				checkPdfaRep.setText(
						"j'assume la responsabilité de l'assurance qualité, de la documentation et de leur réutilisation");
				checkPdfa2uRep.setText(
						"réparer les fichiers PDF/A invalides et les fichiers PDF non acceptés selon PDF/A-2u");
				checkPdfa2bRep.setText("Si la réparation est invalide selon la norme PDF/A-2u, réparer selon la norme PDF/A-2b");
				labelDms.setText(
						"Cette réparation nécessite l'installation autonome du logiciel 'dmstools PDF/A Converter' (v1.7.0.1) ainsi que l'activation de votre licence.");
				labelDms2.setText("La licence KOST intégrée est réservée à nos membres et supporters.");
				buttonConfigApply.setText("appliquer");
				minOne = "Au moins une variante doit etre autorisee !";
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelOtherPdfa.setText("Altro");
				labelVersion.setText("Versioni");
				labelVal.setText("Parametro di convalida: PDF/A");
				labelRep.setText("Parametro di riparazione: PDF/A (solo tramite interfaccia grafica)");
				checkPdfaRep.setText(
						"Mi assumo la responsabilità della garanzia di qualità, della documentazione e del suo ulteriore utilizzo");
				checkPdfa2uRep.setText(
						"riparare i file PDF/A non validi e i file PDF non accettati secondo lo standard PDF/A-2u");
				checkPdfa2bRep.setText("Se la riparazione secondo lo standard PDF/A-2u non è valida, eseguire la riparazione secondo lo standard PDF/A-2b");				labelDms.setText(
						"La riparazione richiede l'installazione autonoma di 'dmstools PDF/A Converter' (v1.7.0.1) e l'attivazione della vostra licenza.");
				labelDms2.setText("La licenza KOST integrata è disponibile solo per i nostri membri e supportatori.");
				buttonConfigApply.setText("Applica");
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelOtherPdfa.setText("Other");
				labelVersion.setText("Versions");
				labelVal.setText("Validation setting: PDF/A");
				labelRep.setText("Repair settings: PDF/A (via GUI only)");
				checkPdfaRep.setText("I take responsibility for quality assurance, documentation and its further use");
				checkPdfa2uRep.setText("repair invalid PDF/A and unacceptable PDF files to PDF/A-2u");
				checkPdfa2bRep.setText("If repair to PDF/A-2u is invalid, repair to PDF/A-2b");
				labelDms.setText(
						"This repair requires you to install the 'dmstools PDF/A Converter' (v1.7.0.1) on your own and activate your license.");
				labelDms2.setText("The integrated KOST license is only available to our members and supporters.");
				buttonConfigApply.setText("apply");
				minOne = "At least one variant must be allowed!";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			byte[] encoded;
			encoded = Files.readAllBytes(Paths.get(configFile.getAbsolutePath()));
			config = new String(encoded, StandardCharsets.UTF_8);
			String noPdftools = "<pdftools>no</pdftools>";
			String noDetailpt = "<detailpt>no</detailpt>";
			String pdfaFont = "<pdfafontpt>strict</pdfafontpt>"; // tolerant oder
			// strict-->
			String pdfaFontTolerant = "<pdfafontpt>tolerant</pdfafontpt>"; // tolerant
			// oder
			// strict-->
			String noVerapdf = "<verapdf>no</verapdf>";
			String noDetailvp = "<detailvp>no</detailvp>";
			String noPdfa1a = "<pdfa1a></pdfa1a>";
			String noPdfa1b = "<pdfa1b></pdfa1b>";
			String noPdfa2a = "<pdfa2a></pdfa2a>";
			String noPdfa2b = "<pdfa2b></pdfa2b>";
			String noPdfa2u = "<pdfa2u></pdfa2u>";
			String noWarning3to2 = "<warning3to2>no</warning3to2>";
			String noPdfaJbig2 = "<jbig2allowed>no</jbig2allowed>";
			String noPdfaRep = "<pdfarep>no </pdfarep>";
			String noPdfa2uRep = "<pdfa2urep>no </pdfa2urep>";
			String noPdfa2bRep = "<pdfa2brep>no </pdfa2brep>";

			if (config.contains(noPdftools)) {
				checkPdftools.setSelected(false);
				checkDetailpt.setDisable(true);
				checkFont.setDisable(true);
				checkFontTol.setDisable(true);
			}
			if (config.contains(noDetailpt)) {
				checkDetailpt.setSelected(false);
			}
			if (config.contains(noVerapdf)) {
				checkVerapdf.setSelected(false);
				checkDetailvp.setDisable(true);
			}
			if (config.contains(noDetailvp)) {
				checkDetailvp.setSelected(false);
			}
			if (config.contains(noWarning3to2)) {
				checkWarning3to2.setSelected(false);
			}
			if (config.contains(noPdfa2a) && config.contains(noPdfa2u) && config.contains(noPdfa2b)) {
				checkWarning3to2.setDisable(true);
			}
			if (config.contains(noPdfa1a)) {
				checkPdfa1a.setSelected(false);
			}
			if (config.contains(noPdfa1b)) {
				checkPdfa1b.setSelected(false);
			}
			if (config.contains(noPdfa2a)) {
				checkPdfa2a.setSelected(false);
			}
			if (config.contains(noPdfa2b)) {
				checkPdfa2b.setSelected(false);
			}
			if (config.contains(noPdfa2u)) {
				checkPdfa2u.setSelected(false);
			}

			if (config.contains(pdfaFont) || config.contains(pdfaFontTolerant)) {
				// checkFont.setSelected( true );
				if (config.contains(pdfaFontTolerant)) {
					// checkFontTol.setSelected( true );
				} else {
					checkFontTol.setSelected(false);
				}
			} else {
				checkFont.setSelected(false);
				checkFontTol.setSelected(false);
			}
			if (config.contains(noPdfaJbig2)) {
				checkJbig2.setSelected(false);
			}
			if (config.contains(noPdfaRep)) {
				checkPdfaRep.setSelected(false);
				checkPdfa2uRep.setDisable(true);
				checkPdfa2bRep.setDisable(true);
			}
			if (config.contains(noPdfa2uRep)) {
				checkPdfa2uRep.setSelected(false);
				checkPdfa2bRep.setDisable(true);
			}
			if (config.contains(noPdfa2bRep)) {
				checkPdfa2bRep.setSelected(false);
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
	 * checkPdftools schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changePdftools(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdftools>yes</pdftools>";
		String no = "<pdftools>no</pdftools>";
		try {
			if (checkPdftools.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkDetailpt.setDisable(false);
				checkFont.setDisable(false);
				checkFontTol.setDisable(false);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkVerapdf.isSelected()) {
					labelMessage.setText(minOne);
					checkPdftools.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
					checkDetailpt.setDisable(true);
					checkFont.setDisable(true);
					checkFontTol.setDisable(true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkDetailpt schaltet diese Details von PDF Tools in der Konfiguration ein
	 * oder aus
	 */
	@FXML
	void changeDetailpt(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<detailpt>yes</detailpt>";
		String no = "<detailpt>no</detailpt>";
		try {
			if (checkDetailpt.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkVerapdf schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeVerapdf(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<verapdf>yes</verapdf>";
		String no = "<verapdf>no</verapdf>";
		try {
			if (checkVerapdf.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkDetailvp.setDisable(false);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdftools.isSelected()) {
					labelMessage.setText(minOne);
					checkVerapdf.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
					checkDetailvp.setDisable(true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkDetailvp schaltet diese Details von verpdf in der Konfiguration ein oder
	 * aus
	 */
	@FXML
	void changeDetailvp(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<detailvp>yes</detailvp>";
		String no = "<detailvp>no</detailvp>";
		try {
			if (checkDetailvp.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* checkPdf.. schaltet diese Version in der Konfiguration ein oder aus */
	@FXML
	void changePdfa1a(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa1a>1A </pdfa1a>";
		String no = "<pdfa1a></pdfa1a>";
		try {
			if (checkPdfa1a.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected()) {
					labelMessage.setText(minOne);
					checkPdfa1a.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa1b(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa1b>1B </pdfa1b>";
		String no = "<pdfa1b></pdfa1b>";
		try {
			if (checkPdfa1b.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				;
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdfa1a.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected()) {
					labelMessage.setText(minOne);
					checkPdfa1b.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2a(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa2a>2A </pdfa2a>";
		String no = "<pdfa2a></pdfa2a>";
		try {
			if (checkPdfa2a.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkWarning3to2.setDisable(false);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdfa1b.isSelected() && !checkPdfa1a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected()) {
					labelMessage.setText(minOne);
					checkPdfa2a.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
					if (checkPdfa2u.isSelected() || checkPdfa2b.isSelected()) {
						checkWarning3to2.setDisable(false);
					} else {
						checkWarning3to2.setDisable(true);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2b(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa2b>2B </pdfa2b>";
		String no = "<pdfa2b></pdfa2b>";
		try {
			if (checkPdfa2b.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkWarning3to2.setDisable(false);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa1a.isSelected()
						&& !checkPdfa2u.isSelected()) {
					labelMessage.setText(minOne);
					checkPdfa2b.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
					if (checkPdfa2a.isSelected() || checkPdfa2u.isSelected()) {
						checkWarning3to2.setDisable(false);
					} else {
						checkWarning3to2.setDisable(true);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2u(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa2u>2U </pdfa2u>";
		String no = "<pdfa2u></pdfa2u>";
		try {
			if (checkPdfa2u.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkWarning3to2.setDisable(false);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa1a.isSelected()) {
					labelMessage.setText(minOne);
					checkPdfa2u.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
					if (checkPdfa2a.isSelected() || checkPdfa2b.isSelected()) {
						checkWarning3to2.setDisable(false);
					} else {
						checkWarning3to2.setDisable(true);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkWarning3to2 validiert wenn eingeschaltet PDF/A-3 nach PDF/A-2 und
	 * ignoriert den Fehler betreffend der Version und gibt stattdessten eine
	 * Warnung aus.
	 */
	@FXML
	void changeWarning3to2(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<warning3to2>yes</warning3to2>";
		String no = "<warning3to2>no</warning3to2>";
		try {
			if (checkWarning3to2.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkFont schaltet diese Font-Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeFont(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfafontpt>strict</pdfafontpt>";
		String no = "<pdfafontpt>no</pdfafontpt>";
		if (checkFontTol.isSelected()) {
			yes = "<pdfafontpt>tolerant</pdfafontpt>";
		}
		try {
			if (checkFont.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkFontTol.setDisable(false);
			} else {
				Util.oldnewstring(yes, no, configFile);
				checkFontTol.setSelected(false);
				checkFontTol.setDisable(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeFontTol(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfafontpt>tolerant</pdfafontpt>";
		String no = "<pdfafontpt>no</pdfafontpt>";
		if (checkFont.isSelected()) {
			no = "<pdfafontpt>strict</pdfafontpt>";
		}
		try {
			if (checkFontTol.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkFont.setSelected(true);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkPdfaRep aendert die Kontrolle ob Reparatur akzeptiert oder nicht in der
	 * Konfiguration ein oder aus
	 */
	@FXML
	void changePdfaRep(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfarep>yes </pdfarep>";
		String no = "<pdfarep>no </pdfarep>";
		try {
			if (checkPdfaRep.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkPdfa2uRep.setDisable(false);
			} else {
				Util.oldnewstring(yes, no, configFile);
				checkPdfa2uRep.setDisable(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkPdfa2uRep aendert die Kontrolle ob PDF/A-2u-Reparatur akzeptiert oder
	 * nicht in der Konfiguration ein oder aus
	 */
	@FXML
	void changePdfa2uRep(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa2urep>yes </pdfa2urep>";
		String no = "<pdfa2urep>no </pdfa2urep>";
		try {
			if (checkPdfa2uRep.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
				checkPdfa2bRep.setDisable(false);
			} else {
				Util.oldnewstring(yes, no, configFile);
				checkPdfa2bRep.setDisable(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkPdfa2bRep aendert die Kontrolle ob PDF/A-2b-Reparatur akzeptiert oder
	 * nicht in der Konfiguration ein oder aus
	 */
	@FXML
	void changePdfa2bRep(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<pdfa2brep>yes </pdfa2brep>";
		String no = "<pdfa2brep>no </pdfa2brep>";
		try {
			if (checkPdfa2bRep.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * checkJbig2 schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeJbig2(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<jbig2allowed>yes</jbig2allowed>";
		String no = "<jbig2allowed>no</jbig2allowed>";
		try {
			if (checkJbig2.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}