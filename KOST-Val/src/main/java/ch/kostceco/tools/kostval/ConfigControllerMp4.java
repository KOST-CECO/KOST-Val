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

public class ConfigControllerMp4 {

	@FXML
	private CheckBox checkH264, checkH265, checkMp3, checkAac, checkNoVideo, checkNoAudio;

	@FXML
	private Button buttonConfigApply;

	private File configFile = new File(System.getenv("USERPROFILE") + File.separator + ".kost-val_2x" + File.separator
			+ "configuration" + File.separator + "kostval.conf.xml");

	private String dirOfJarPath, config, minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label labelVideocodec, labelAudiocodec, labelMp4, labelOther, labelMessage, labelConfig;

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
				labelVideocodec.setText("Videocodec");
				labelAudiocodec.setText("Audiocodec");
				labelMp4.setText("Validierungseinstellung: MP4");
				labelOther.setText("Sonstiges");
				checkNoVideo.setText("Reine Audiodatei erlaubt (kein Videocodec)");
				checkNoAudio.setText("Stummfilm erlaubt (kein Audiocodec)");
				buttonConfigApply.setText("anwenden");
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if (Util.stringInFileLine("kostval-conf-FR.xsl", configFile)) {
				labelVideocodec.setText("Codec vidéo");
				labelAudiocodec.setText("Codec audio");
				labelMp4.setText("Paramètre de validation: MP4");
				labelOther.setText("Autre");
				checkNoVideo.setText("Fichier audio pur autorisé (pas de codec vidéo)");
				checkNoAudio.setText("Film muet autorisé (pas de codec audio)");
				buttonConfigApply.setText("appliquer");
				minOne = "Au moins une variante doit etre autorisee !";
			} else if (Util.stringInFileLine("kostval-conf-IT.xsl", configFile)) {
				labelVideocodec.setText("Codec video");
				labelAudiocodec.setText("Codec audio");
				labelMp4.setText("Parametro di convalida: MP4");
				labelOther.setText("Altro");
				checkNoVideo.setText("File audio puro consentito (nessun codec video)");
				checkNoAudio.setText("Film muto consentito (nessun codec audio)");
				buttonConfigApply.setText("Applica");
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelVideocodec.setText("Video codec");
				labelAudiocodec.setText("Audio codec");
				labelMp4.setText("Validation setting: MP4");
				labelOther.setText("Other");
				checkNoVideo.setText("Pure audio file allowed (no video codec)");
				checkNoAudio.setText("Silent movie allowed (no audio codec)");
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
			String noAvc = "<allowedmp4avc></allowedmp4avc>";
			String noHevc = "<allowedmp4hevc></allowedmp4hevc>";
			String noMp3 = "<allowedmp4mp3></allowedmp4mp3>";
			String noAac = "<allowedmp4aac></allowedmp4aac>";
			String noVideo = "<allowedmp4novideo>Error</allowedmp4novideo>";
			String noAudio = "<allowedmp4noaudio>Error</allowedmp4noaudio>";

			if (config.contains(noAvc)) {
				checkH264.setSelected(false);
			}
			if (config.contains(noHevc)) {
				checkH265.setSelected(false);
			}
			if (config.contains(noMp3)) {
				checkMp3.setSelected(false);
			}
			if (config.contains(noAac)) {
				checkAac.setSelected(false);
			}
			if (config.contains(noVideo)) {
				checkNoVideo.setSelected(false);
			}
			if (config.contains(noAudio)) {
				checkNoAudio.setSelected(false);
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
		// labelMessage.setText(minOne ); "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* TODO --> CheckBox ================= */

	/* checkComp schaltet diesen Codec in der Konfiguration ein oder aus */
	@FXML
	void changeH264(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4avc>AVC </allowedmp4avc>";
		String no = "<allowedmp4avc></allowedmp4avc>";
		try {
			if (checkH264.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkH265.isSelected()) {
					labelMessage.setText(minOne);
					checkH264.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeH265(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4hevc>HEVC </allowedmp4hevc>";
		String no = "<allowedmp4hevc></allowedmp4hevc>";
		try {
			if (checkH265.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkH264.isSelected()) {
					labelMessage.setText(minOne);
					checkH265.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMp3(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4mp3>MP3 </allowedmp4mp3>";
		String no = "<allowedmp4mp3></allowedmp4mp3>";
		try {
			if (checkMp3.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkAac.isSelected()) {
					labelMessage.setText(minOne);
					checkMp3.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeAac(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4aac>AAC </allowedmp4aac>";
		String no = "<allowedmp4aac></allowedmp4aac>";
		try {
			if (checkAac.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if (!checkMp3.isSelected()) {
					labelMessage.setText(minOne);
					checkAac.setSelected(true);
				} else {
					Util.oldnewstring(yes, no, configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeNoVideo(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4novideo>Warning</allowedmp4novideo>";
		String no = "<allowedmp4novideo>Error</allowedmp4novideo>";
		try {
			if (checkNoVideo.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeNoAudio(ActionEvent event) {
		labelMessage.setText("");
		String yes = "<allowedmp4noaudio>Warning</allowedmp4noaudio>";
		String no = "<allowedmp4noaudio>Error</allowedmp4noaudio>";
		try {
			if (checkNoAudio.isSelected()) {
				Util.oldnewstring(no, yes, configFile);
			} else {
				Util.oldnewstring(yes, no, configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}