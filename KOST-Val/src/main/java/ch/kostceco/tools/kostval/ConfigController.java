/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigController
{

	private File				configFileBackup	= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x"
					+ File.separator + "configuration" + File.separator
					+ "BACKUP.kostval.conf.xml" );

	private File				configFileStandard	= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x"
					+ File.separator + "configuration" + File.separator
					+ "STANDARD.kostval.conf.xml" );

	private File				configFile			= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x"
					+ File.separator + "configuration" + File.separator
					+ "kostval.conf.xml" );

	private String				dirOfJarPath, inputString, workString, config,
			stringPuid, minOne = "Mindestens eine Variante muss erlaubt sein!";;

	private Locale				locale				= Locale.getDefault();

	@FXML
	private Button				buttonConfigApply, buttonConfigApplyStandard,
			buttonConfigCancel;

	@FXML
	private Label				labelText, labelPdfa, labelTxt, labelPdf;

	@FXML
	private Button				buttonPdfa, buttonPdfaVal, buttonTxt, buttonPdf;

	@FXML
	private Label				labelImage, labelJpeg2000, labelJpeg, labelTiff,
			labelPng;

	@FXML
	private Button				buttonJpeg2000, buttonJpeg, buttonTiff,
			buttonTiffVal, buttonPng;

	@FXML
	private Label				labelAudio, labelFlac, labelWave, labelMp3;

	@FXML
	private Button				buttonFlac, buttonWave, buttonMp3;

	@FXML
	private Label				labelVideo, labelFfv1, labelMp4;

	@FXML
	private Button				buttonFfv1, buttonMp4;

	@FXML
	private Label				labelData, labelXml, labelSiard, labelCsv,
			labelXlsx, labelOds;

	@FXML
	private Button				buttonXml, buttonSiard, buttonSiardVal,
			buttonCsv, buttonXlsx, buttonOds;

	@FXML
	private Label				labelSip, labelEch0160;

	@FXML
	private Button				buttonSip0160, buttonSipVal;

	@FXML
	private Label				labelOther, labelWork, labelInput, labelHint, labelHint1,
			labelConfig;

	@FXML
	private Button				buttonPuid, buttonWork, buttonInput;

	ObservableList<String>		hashAlgoList		= FXCollections
			.observableArrayList( "MD5", "SHA-1", "SHA-256", "SHA-512", "" );
	@FXML
	private ChoiceBox<String>	hashAlgo;

	@FXML
	private WebView				wbv;

	private WebEngine			engine;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText(
				"Copyright © KOST/CECO          KOST-Val v2.1.4.0          JavaFX "
						+ javafxVersion + "   &   Java-" + java6432 + " "
						+ javaVersion + "." );

		// Original Config Kopieren
		try {
			Util.copyFile( configFile, configFileBackup );
		} catch ( IOException e2 ) {
			e2.printStackTrace();
		}

		// festhalten von wo die Applikation (exe) gestartet wurde
		dirOfJarPath = "";
		try {
			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist
			 * eine generelle Aufgabe in allen Modulen. Zuerst immer
			 * dirOfJarPath ermitteln und dann alle Pfade mit dirOfJarPath +
			 * File.separator + erweitern.
			 */
			String path = new File( "" ).getAbsolutePath();
			dirOfJarPath = path;
			setLibraryPath( dirOfJarPath );
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		hashAlgo.getItems().addAll( hashAlgoList );
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String hashAlgoInit = "";
			hashAlgoInit = doc.getElementsByTagName( "hash" ).item( 0 )
					.getTextContent();
			bis.close();
			doc = null;
			hashAlgo.setValue( hashAlgoInit );
		} catch ( IOException | ParserConfigurationException
				| SAXException e1 ) {
			e1.printStackTrace();
		}

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if ( Util.stringInFileLine( "kostval-conf-DE.xsl", configFile ) ) {
				locale = new Locale( "de" );
				buttonConfigApply.setText( "anwenden" );
				buttonConfigApplyStandard.setText( "Standard anwenden" );
				buttonConfigCancel.setText( "verwerfen" );
				labelText.setText( "Text" );
				labelImage.setText( "Bild" );
				labelAudio.setText( "Audio" );
				labelVideo.setText( "Video" );
				labelData.setText( "Daten" );
				labelSip.setText( "SIP" );
				labelOther.setText( "Sonstige" );
				buttonWork.setText( "Arbeitsverzeichnis" );
				buttonInput.setText( "Inputverzeichnis" );
				labelHint1.setText(
						"Hinweis:" );
				labelHint.setText(
						"öffnet die jeweilige Detailkonfiguration" );
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				locale = new Locale( "fr" );
				buttonConfigApply.setText( "appliquer" );
				buttonConfigApplyStandard.setText( "appliquer le standard" );
				buttonConfigCancel.setText( "annuler" );
				labelText.setText( "Texte" );
				labelImage.setText( "Image" );
				labelAudio.setText( "Audio" );
				labelVideo.setText( "Vidéo" );
				labelData.setText( "Données" );
				labelSip.setText( "SIP" );
				labelOther.setText( "Autres" );
				buttonWork.setText( "Répertoire de travail" );
				buttonInput.setText( "Répertoire d'entrée" );
				labelHint1.setText(
						"Remarque :" );
				labelHint.setText(
						"ouvre la configuration détaillée correspondante" );
				minOne = "Au moins une variante doit etre autorisee !";
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				locale = new Locale( "it" );
				buttonConfigApply.setText( "applica" );
				buttonConfigApplyStandard.setText( "applica standard" );
				buttonConfigCancel.setText( "annulla" );
				labelText.setText( "Testo" );
				labelImage.setText( "Immagine" );
				labelAudio.setText( "Audio" );
				labelVideo.setText( "Video" );
				labelData.setText( "Dati" );
				labelSip.setText( "SIP" );
				labelOther.setText( "Altro" );
				buttonWork.setText( "Directory di lavoro" );
				buttonInput.setText( "Directory di input" );
				labelHint1.setText(
						"Nota:" );
				labelHint.setText(
						"apre la configurazione dettagliata corrispondente" );
				minOne = "Deve essere consentita almeno una variante!";
			} else {
				locale = new Locale( "en" );
				buttonConfigApply.setText( "apply" );
				buttonConfigApplyStandard.setText( "apply Standard" );
				buttonConfigCancel.setText( "cancel" );
				labelText.setText( "Text" );
				labelImage.setText( "Image" );
				labelAudio.setText( "Audio" );
				labelVideo.setText( "Video" );
				labelData.setText( "Data" );
				labelSip.setText( "SIP" );
				labelOther.setText( "Other" );
				buttonWork.setText( "Working directory" );
				buttonInput.setText( "Input directory" );
				labelHint1.setText(
						"Note:" );
				labelHint.setText(
						"opens the respective detailed configuration" );
				minOne = "At least one variant must be allowed!";
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		// checkPdfa, checkJpeg2000, checkJpeg, checkPng, checkXml, checkTiff,
		// checkSiard;
		try {
			byte[] encoded;
			encoded = Files
					.readAllBytes( Paths.get( configFile.getAbsolutePath() ) );
			config = new String( encoded, StandardCharsets.UTF_8 );
			String noPdfa = "<pdfavalidation>&#x2717;</pdfavalidation>";
			String yesPdfa = "<pdfavalidation>&#x2713;</pdfavalidation>";
			String noTxt = "<txtvalidation>&#x2717;</txtvalidation>";
			String noPdf = "<pdfvalidation>&#x2717;</pdfvalidation>";

			String noJpeg2000 = "<jp2validation>&#x2717;</jp2validation>";
			String yesJpeg2000 = "<jp2validation>&#x2713;</jp2validation>";
			String yesJpeg = "<jpegvalidation>&#x2713;</jpegvalidation>";
			String noJpeg = "<jpegvalidation>&#x2717;</jpegvalidation>";
			String noTiff = "<tiffvalidation>&#x2717;</tiffvalidation>";
			String yesTiff = "<tiffvalidation>&#x2713;</tiffvalidation>";
			String noPng = "<pngvalidation>&#x2717;</pngvalidation>";
			String yesPng = "<pngvalidation>&#x2713;</pngvalidation>";

			String noFlac = "<flacvalidation>&#x2717;</pdfvalidation>";
			String noWave = "<wavevalidation>&#x2717;</wavevalidation>";
			String noMp3 = "<mp3validation>&#x2717;</mp3validation>";

			String noFfv1 = "<mkvvalidation>&#x2717;</mkvvalidation>";
			String noMp4 = "<mp4validation>&#x2717;</mp4validation>";

			String noXml = "<xmlvalidation>&#x2717;</xmlvalidation>";
			String yesXml = "<xmlvalidation>&#x2713;</xmlvalidation>";
			String noSiard = "<siardvalidation>&#x2717;</siardvalidation>";
			String yesSiard = "<siardvalidation>&#x2713;</siardvalidation>";
			String noCsv = "<csvvalidation>&#x2717;</csvvalidation>";
			String noXlsx = "<xlsxvalidation>&#x2717;</xlsxvalidation>";
			String noOds = "<odsvalidation>&#x2717;</odsvalidation>";

			String noSip0160 = "<ech0160validation>&#x2717;</ech0160validation>";
			String yesSip0160 = "<ech0160validation>&#x2713;</ech0160validation>";

			if ( config.contains( noPdfa ) ) {
				buttonPdfaVal.setDisable( true );
				buttonPdfa.setText( "✗" );
				buttonPdfa.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesPdfa ) ) {
				buttonPdfaVal.setDisable( false );
				buttonPdfa.setText( "✓" );
				buttonPdfa.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonPdfaVal.setDisable( true );
				buttonPdfa.setText( "(✓)" );
				buttonPdfa.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noTxt ) ) {
				buttonTxt.setText( "✗" );
				buttonTxt.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonTxt.setText( "(✓)" );
				buttonTxt.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noPdf ) ) {
				buttonPdf.setText( "✗" );
				buttonPdf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonPdf.setText( "(✓)" );
				buttonPdf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noJpeg2000 ) ) {
				buttonJpeg2000.setText( "✗" );
				buttonJpeg2000.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesJpeg2000 ) ) {
				buttonJpeg2000.setText( "✓" );
				buttonJpeg2000.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonJpeg2000.setText( "(✓)" );
				buttonJpeg2000.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noJpeg ) ) {
				buttonJpeg.setText( "✗" );
				buttonJpeg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesJpeg ) ) {
				buttonJpeg.setText( "✓" );
				buttonJpeg.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonJpeg.setText( "(✓)" );
				buttonJpeg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noTiff ) ) {
				buttonTiffVal.setDisable( true );
				buttonTiff.setText( "✗" );
				buttonTiff.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesTiff ) ) {
				buttonTiffVal.setDisable( false );
				buttonTiff.setText( "✓" );
				buttonTiff.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonTiff.setText( "(✓)" );
				buttonTiff.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noPng ) ) {
				buttonPng.setText( "✗" );
				buttonPng.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesPng ) ) {
				buttonPng.setText( "✓" );
				buttonPng.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonPng.setText( "(✓)" );
				buttonPng.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noFlac ) ) {
				buttonFlac.setText( "✗" );
				buttonFlac.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonFlac.setText( "(✓)" );
				buttonFlac.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noWave ) ) {
				buttonWave.setText( "✗" );
				buttonWave.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonWave.setText( "(✓)" );
				buttonWave.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noMp3 ) ) {
				buttonMp3.setText( "✗" );
				buttonMp3.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonMp3.setText( "(✓)" );
				buttonMp3.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noFfv1 ) ) {
				buttonFfv1.setText( "✗" );
				buttonFfv1.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonFfv1.setText( "(✓)" );
				buttonFfv1.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noMp4 ) ) {
				buttonMp4.setText( "✗" );
				buttonMp4.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonMp4.setText( "(✓)" );
				buttonMp4.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noXml ) ) {
				buttonXml.setText( "✗" );
				buttonXml.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesXml ) ) {
				buttonXml.setText( "✓" );
				buttonXml.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonXml.setText( "(✓)" );
				buttonXml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noSiard ) ) {
				buttonSiardVal.setDisable( true );
				buttonSiard.setText( "✗" );
				buttonSiard.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesSiard ) ) {
				buttonSiardVal.setDisable( false );
				buttonSiard.setText( "✓" );
				buttonSiard.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonSiard.setText( "(✓)" );
				buttonSiard.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noCsv ) ) {
				buttonCsv.setText( "✗" );
				buttonCsv.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonCsv.setText( "(✓)" );
				buttonCsv.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noXlsx ) ) {
				buttonXlsx.setText( "✗" );
				buttonXlsx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonXlsx.setText( "(✓)" );
				buttonXlsx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noOds ) ) {
				buttonOds.setText( "✗" );
				buttonOds.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonOds.setText( "(✓)" );
				buttonOds.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			if ( config.contains( noSip0160 ) ) {
				buttonSipVal.setDisable( true );
				buttonSip0160.setText( "✗" );
				buttonSip0160.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else if ( config.contains( yesSip0160 ) ) {
				buttonSipVal.setDisable( false );
				buttonSip0160.setText( "✓" );
				buttonSip0160.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
			} else {
				buttonSip0160.setText( "(✓)" );
				buttonSip0160.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}

			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			stringPuid = doc.getElementsByTagName( "otherformats" ).item( 0 )
					.getTextContent();
			buttonPuid.setText( stringPuid );
			workString = doc.getElementsByTagName( "pathtoworkdir" ).item( 0 )
					.getTextContent();
			labelWork.setText( workString );
			inputString = doc.getElementsByTagName( "standardinputdir" )
					.item( 0 ).getTextContent();
			labelInput.setText( inputString );

		} catch ( IOException | SAXException
				| ParserConfigurationException e1 ) {
			e1.printStackTrace();
		}

		engine = wbv.getEngine();
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	protected void updateEngine( String message )
	{
		engine.loadContent( message );
	}

	public static void setLibraryPath( String path ) throws Exception
	{
		System.setProperty( "java.library.path", path );
		// set sys_paths to null so that java.library.path will be reevalueted
		// next time it is needed
		final Field sysPathsField = ClassLoader.class
				.getDeclaredField( "sys_paths" );
		sysPathsField.setAccessible( true );
		sysPathsField.set( null, null );
	}

	/* TODO --> Button Exit ================= */

	@FXML
	void configApply( ActionEvent e )
	{
		engine.loadContent( "Apply" );
		Util.deleteFile( configFileBackup );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	@FXML
	void configApplyStandard( ActionEvent e )
	{
		engine.loadContent( "Apply Standard" );
		configFile.delete();
		Util.deleteFile( configFileBackup );
		try {
			Util.copyFile( configFileStandard, configFile );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	@FXML
	void configCancel( ActionEvent e )
	{
		configFile.delete();
		engine.loadContent( "Cancel" );
		try {
			Util.copyFile( configFileBackup, configFile );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		Util.deleteFile( configFileBackup );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* TODO --> Text ================= */

	/* changePdfa schaltet zwischen x (v) V herum */
	@FXML
	void changePdfa( ActionEvent event )
	{
		String yes = "<pdfavalidation>&#x2713;</pdfavalidation>";
		String az = "<pdfavalidation>(&#x2713;)</pdfavalidation>";
		String no = "<pdfavalidation>&#x2717;</pdfavalidation>";
		try {
			String optButton = buttonPdfa.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonPdfa.setText( "(✓)" );
				buttonPdfa.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				buttonPdfaVal.setDisable( false );
				Util.oldnewstring( az, yes, configFile );
				buttonPdfa.setText( "✓" );
				buttonPdfa.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					buttonPdfaVal.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
					buttonPdfa.setText( "✗" );
					buttonPdfa.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	// Mit changePdfaVal wird die Pdfa-Haupteinstellung umgestellt
	@FXML
	void changePdfaVal( ActionEvent eventPdfa )
	{
		try {
			StackPane pdfaLayout = new StackPane();

			pdfaLayout = FXMLLoader
					.load( getClass().getResource( "ConfigViewPdfa.fxml" ) );
			Scene pdfaScene = new Scene( pdfaLayout );
			pdfaScene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage pdfaStage = new Stage();

			pdfaStage.setTitle( "KOST-Val   -   Configuration   -   PDF/A" );
			Image kostvalIcon = new Image( "file:" + dirOfJarPath
					+ File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			pdfaStage.initModality( Modality.APPLICATION_MODAL );
			pdfaStage.getIcons().add( kostvalIcon );
			pdfaStage.setScene( pdfaScene );
			pdfaStage.setOnCloseRequest( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
			pdfaStage.show();
			pdfaStage.setOnHiding( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/* changeTxt schaltet zwischen x (v) herum */
	@FXML
	void changeTxt( ActionEvent event )
	{
		String az = "<txtvalidation>(&#x2713;)</txtvalidation>";
		String no = "<txtvalidation>&#x2717;</txtvalidation>";
		try {
			String optButton = buttonTxt.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonTxt.setText( "(✓)" );
				buttonTxt.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPdf.getText().equals( "✗" )
						&& buttonJpeg.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					Util.oldnewstring( az, no, configFile );
					buttonTxt.setText( "✗" );
					buttonTxt.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changePdf schaltet zwischen x (v) herum */
	@FXML
	void changePdf( ActionEvent event )
	{
		String az = "<pdfvalidation>(&#x2713;)</pdfvalidation>";
		String no = "<pdfvalidation>&#x2717;</pdfvalidation>";
		try {
			String optButton = buttonPdf.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonPdf.setText( "(✓)" );
				buttonPdf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonPdf.setText( "✗" );
				buttonPdf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* TODO --> Image ================= */

	/* changeJpeg2000 schaltet zwischen x (v) V herum */
	@FXML
	void changeJpeg2000( ActionEvent event )
	{
		String yes = "<jp2validation>&#x2713;</jp2validation>";
		String az = "<jp2validation>(&#x2713;)</jp2validation>";
		String no = "<jp2validation>&#x2717;</jp2validation>";
		try {
			String optButton = buttonJpeg2000.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonJpeg2000.setText( "(✓)" );
				buttonJpeg2000.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				Util.oldnewstring( az, yes, configFile );
				buttonJpeg2000.setText( "✓" );
				buttonJpeg2000.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					Util.oldnewstring( yes, no, configFile );
					buttonJpeg2000.setText( "✗" );
					buttonJpeg2000.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeJpeg schaltet zwischen x (v) V herum */
	@FXML
	void changeJpeg( ActionEvent event )
	{
		String yes = "<jpegvalidation>&#x2713;</jpegvalidation>";
		String az = "<jpegvalidation>(&#x2713;)</jpegvalidation>";
		String no = "<jpegvalidation>&#x2717;</jpegvalidation>";
		try {
			String optButton = buttonJpeg.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonJpeg.setText( "(✓)" );
				buttonJpeg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				Util.oldnewstring( az, yes, configFile );
				buttonJpeg.setText( "✓" );
				buttonJpeg.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					Util.oldnewstring( yes, no, configFile );
					buttonJpeg.setText( "✗" );
					buttonJpeg.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeTiff schaltet zwischen x (v) V herum */
	@FXML
	void changeTiff( ActionEvent event )
	{
		String yes = "<tiffvalidation>&#x2713;</tiffvalidation>";
		String az = "<tiffvalidation>(&#x2713;)</tiffvalidation>";
		String no = "<tiffvalidation>&#x2717;</tiffvalidation>";
		try {
			String optButton = buttonTiff.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonTiff.setText( "(✓)" );
				buttonTiff.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				buttonTiffVal.setDisable( false );
				Util.oldnewstring( az, yes, configFile );
				buttonTiff.setText( "✓" );
				buttonTiff.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonJpeg.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					buttonTiffVal.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
					buttonTiff.setText( "✗" );
					buttonTiff.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	// Mit changeTiffVal wird die TIFF-Haupteinstellung umgestellt
	@FXML
	void changeTiffVal( ActionEvent eventTiff )
	{
		try {
			StackPane tiffLayout = new StackPane();

			tiffLayout = FXMLLoader
					.load( getClass().getResource( "ConfigViewTiff.fxml" ) );
			Scene tiffScene = new Scene( tiffLayout );
			tiffScene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage tiffStage = new Stage();

			tiffStage.setTitle( "KOST-Val   -   Configuration   -   TIFF" );
			Image kostvalIcon = new Image( "file:" + dirOfJarPath
					+ File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			tiffStage.initModality( Modality.APPLICATION_MODAL );
			tiffStage.getIcons().add( kostvalIcon );
			tiffStage.setScene( tiffScene );
			tiffStage.setOnCloseRequest( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
			tiffStage.show();
			tiffStage.setOnHiding( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/* changePng schaltet zwischen x (v) V herum */
	@FXML
	void changePng( ActionEvent event )
	{
		String yes = "<pngvalidation>&#x2713;</pngvalidation>";
		String az = "<pngvalidation>(&#x2713;)</pngvalidation>";
		String no = "<pngvalidation>&#x2717;</pngvalidation>";
		try {
			String optButton = buttonPng.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonPng.setText( "(✓)" );
				buttonPng.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				Util.oldnewstring( az, yes, configFile );
				buttonPng.setText( "✓" );
				buttonPng.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonJpeg.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					Util.oldnewstring( yes, no, configFile );
					buttonPng.setText( "✗" );
					buttonPng.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* TODO --> Audio ================= */

	/* changeFlac schaltet zwischen x (v) herum */
	@FXML
	void changeFlac( ActionEvent event )
	{
		String az = "<flacvalidation>(&#x2713;)</flacvalidation>";
		String no = "<flacvalidation>&#x2717;</flacvalidation>";
		try {
			String optButton = buttonFlac.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonFlac.setText( "(✓)" );
				buttonFlac.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonFlac.setText( "✗" );
				buttonFlac.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeWave schaltet zwischen x (v) herum */
	@FXML
	void changeWave( ActionEvent event )
	{
		String az = "<wavevalidation>(&#x2713;)</wavevalidation>";
		String no = "<wavevalidation>&#x2717;</wavevalidation>";
		try {
			String optButton = buttonWave.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonWave.setText( "(✓)" );
				buttonWave.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonWave.setText( "✗" );
				buttonWave.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeMp3 schaltet zwischen x (v) herum */
	@FXML
	void changeMp3( ActionEvent event )
	{
		String az = "<mp3validation>(&#x2713;)</mp3validation>";
		String no = "<mp3validation>&#x2717;</mp3validation>";
		try {
			String optButton = buttonMp3.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonMp3.setText( "(✓)" );
				buttonMp3.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonMp3.setText( "✗" );
				buttonMp3.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* TODO --> Video ================= */

	/* changeFfv1 schaltet zwischen x (v) herum */
	@FXML
	void changeFfv1( ActionEvent event )
	{
		String az = "<mkvvalidation>(&#x2713;)</mkvvalidation>";
		String no = "<mkvvalidation>&#x2717;</mkvvalidation>";
		try {
			String optButton = buttonFfv1.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonFfv1.setText( "(✓)" );
				buttonFfv1.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonFfv1.setText( "✗" );
				buttonFfv1.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeMp4 schaltet zwischen x (v) herum */
	@FXML
	void changeMp4( ActionEvent event )
	{
		String az = "<mp4validation>(&#x2713;)</mp4validation>";
		String no = "<mp4validation>&#x2717;</mp4validation>";
		try {
			String optButton = buttonMp4.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonMp4.setText( "(✓)" );
				buttonMp4.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonMp4.setText( "✗" );
				buttonMp4.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* TODO --> Data ================= */

	/* changeXml schaltet zwischen x (v) V herum */
	@FXML
	void changeXml( ActionEvent event )
	{
		String yes = "<xmlvalidation>&#x2713;</xmlvalidation>";
		String az = "<xmlvalidation>(&#x2713;)</xmlvalidation>";
		String no = "<xmlvalidation>&#x2717;</xmlvalidation>";
		try {
			String optButton = buttonXml.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonXml.setText( "(✓)" );
				buttonXml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				Util.oldnewstring( az, yes, configFile );
				buttonXml.setText( "✓" );
				buttonXml.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					Util.oldnewstring( yes, no, configFile );
					buttonXml.setText( "✗" );
					buttonXml.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeSiard schaltet zwischen x (v) V herum */
	@FXML
	void changeSiard( ActionEvent event )
	{
		String yes = "<siardvalidation>&#x2713;</siardvalidation>";
		String az = "<siardvalidation>(&#x2713;)</siardvalidation>";
		String no = "<siardvalidation>&#x2717;</siardvalidation>";
		try {
			String optButton = buttonSiard.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonSiard.setText( "(✓)" );
				buttonSiard.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				buttonSiardVal.setDisable( false );
				Util.oldnewstring( az, yes, configFile );
				buttonSiard.setText( "✓" );
				buttonSiard.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonJpeg.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSip0160.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					buttonSiardVal.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
					buttonSiard.setText( "✗" );
					buttonSiard.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* Mit changeSiardVal wird die Pdfa-Haupteinstellung umgestellt */
	@FXML
	void changeSiardVal( ActionEvent eventSiard )
	{
		try {
			StackPane siardLayout = new StackPane();

			siardLayout = FXMLLoader
					.load( getClass().getResource( "ConfigViewSiard.fxml" ) );
			Scene siardScene = new Scene( siardLayout );
			siardScene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage siardStage = new Stage();

			siardStage.setTitle( "KOST-Val   -   Configuration   -   SIARD" );
			Image kostvalIcon = new Image( "file:" + dirOfJarPath
					+ File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			siardStage.initModality( Modality.APPLICATION_MODAL );
			siardStage.getIcons().add( kostvalIcon );
			siardStage.setScene( siardScene );
			siardStage.setOnCloseRequest( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
			siardStage.show();
			siardStage.setOnHiding( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/* changeCsv schaltet zwischen x (v) herum */
	@FXML
	void changeCsv( ActionEvent event )
	{
		String az = "<csvvalidation>(&#x2713;)</csvvalidation>";
		String no = "<csvvalidation>&#x2717;</csvvalidation>";
		try {
			String optButton = buttonCsv.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonCsv.setText( "(✓)" );
				buttonCsv.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonCsv.setText( "✗" );
				buttonCsv.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeXlsx schaltet zwischen x (v) herum */
	@FXML
	void changeXlsx( ActionEvent event )
	{
		String az = "<xlsxvalidation>(&#x2713;)</xlsxvalidation>";
		String no = "<xlsxvalidation>&#x2717;</xlsxvalidation>";
		try {
			String optButton = buttonXlsx.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonXlsx.setText( "(✓)" );
				buttonXlsx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonXlsx.setText( "✗" );
				buttonXlsx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* changeOds schaltet zwischen x (v) herum */
	@FXML
	void changeOds( ActionEvent event )
	{
		String az = "<odsvalidation>(&#x2713;)</odsvalidation>";
		String no = "<odsvalidation>&#x2717;</odsvalidation>";
		try {
			String optButton = buttonOds.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonOds.setText( "(✓)" );
				buttonOds.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonOds.setText( "✗" );
				buttonOds.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				// TODO Check etwas angewaehlt
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* TODO --> SIP ================= */

	/* changeSip0160 schaltet zwischen x V herum */
	@FXML
	void changeSip0160( ActionEvent event )
	{
		String yes = "<ech0160validation>&#x2713;</ech0160validation>";
		String az = "<ech0160validation>(&#x2713;)</ech0160validation>";
		String no = "<ech0160validation>&#x2717;</ech0160validation>";
		try {
			String optButton = buttonSip0160.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonSip0160.setText( "(✓)" );
				buttonSip0160.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else if ( optButton.equals( "(✓)" ) ) {
				buttonSipVal.setDisable( false );
				Util.oldnewstring( az, yes, configFile );
				buttonSip0160.setText( "✓" );
				buttonSip0160.setStyle(
						"-fx-text-fill: LimeGreen; -fx-background-color: WhiteSmoke" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected / (v) / V ist
				if ( buttonJpeg2000.getText().equals( "✗" )
						&& buttonPng.getText().equals( "✗" )
						&& buttonTiff.getText().equals( "✗" )
						&& buttonPdfa.getText().equals( "✗" )
						&& buttonJpeg.getText().equals( "✗" )
						&& buttonXml.getText().equals( "✗" )
						&& buttonSiard.getText().equals( "✗" ) ) {
					engine.loadContent(
							"<html><h2>" + minOne + "</h2></html>" );
				} else {
					buttonSipVal.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
					buttonSip0160.setText( "✗" );
					buttonSip0160.setStyle(
							"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	// Mit changeSipVal wird die Pdfa-Haupteinstellung umgestellt
	@FXML
	void changeSipVal( ActionEvent eventSip )
	{
		try {
			StackPane sipLayout = new StackPane();

			sipLayout = FXMLLoader
					.load( getClass().getResource( "ConfigViewSip.fxml" ) );
			Scene sipScene = new Scene( sipLayout );
			sipScene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage sipStage = new Stage();

			sipStage.setTitle( "KOST-Val   -   Configuration   -   SIP" );
			Image kostvalIcon = new Image( "file:" + dirOfJarPath
					+ File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			sipStage.initModality( Modality.APPLICATION_MODAL );
			sipStage.getIcons().add( kostvalIcon );
			sipStage.setScene( sipScene );
			sipStage.setOnCloseRequest( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
			sipStage.show();
			sipStage.setOnHiding( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/* TODO --> Other ================= */

	/* Wenn Aenderungen an changePuid gemacht wird, wird es ausgeloest */
	@FXML
	void changePuid( ActionEvent event )
	{
		stringPuid = buttonPuid.getText();
		// create a TextInputDialog mit der Texteingabe der Puid
		TextInputDialog dialog = new TextInputDialog( stringPuid );

		// Set title & header text
		String puidIntInit = stringPuid;

		dialog.setTitle( "KOST-Val - Configuration" );
		String headerDeFrItEn = "Auflistung der weiteren akzeptierten Dateiformate [WARC HTML DWG]:";
		if ( locale.toString().startsWith( "fr" ) ) {
			headerDeFrItEn = "Liste des autres formats de fichiers acceptés [WARC HTML DWG] :";
		} else if ( locale.toString().startsWith( "it" ) ) {
			headerDeFrItEn = "Elenco degli altri formati di file accettati [WARC HTML DWG]:";
		} else if ( locale.toString().startsWith( "en" ) ) {
			headerDeFrItEn = "List of other accepted file formats [WARC HTML DWG]:";
		}
		dialog.setHeaderText( headerDeFrItEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringPuidNew = "";
		if ( result.isPresent() ) {
			try {
				stringPuidNew = result.get();
				stringPuid = stringPuidNew;
				buttonPuid.setText( stringPuid );
				String allowedformats = "<otherformats>" + puidIntInit
						+ "</otherformats>";
				String allowedformatsNew = "<otherformats>" + stringPuidNew
						+ "</otherformats>";
				Util.oldnewstring( allowedformats, allowedformatsNew,
						configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} catch ( NumberFormatException | IOException eInt ) {
				String message = eInt.getMessage();
				engine.loadContent( message );
			}
		} else {
			// Keine Aktion
		}
	}

	/* Wenn chooseWork betaetigt wird, kann ein Ordner ausgewaehlt werden */
	@FXML
	void chooseWork( ActionEvent e )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String workInit = "";
			if ( !config.contains( "<pathtoworkdir></pathtoworkdir>" ) ) {
				workInit = doc.getElementsByTagName( "pathtoworkdir" ).item( 0 )
						.getTextContent();
			}
			bis.close();
			doc = null;
			String pathtoworkdir = "<pathtoworkdir>" + workInit
					+ "</pathtoworkdir>";
			DirectoryChooser folderChooser = new DirectoryChooser();
			if ( locale.toString().startsWith( "fr" ) ) {
				folderChooser.setTitle( "Choisissez le dossier" );
			} else if ( locale.toString().startsWith( "it" ) ) {
				folderChooser.setTitle( "Scegliere la cartella" );
			} else if ( locale.toString().startsWith( "en" ) ) {
				folderChooser.setTitle( "Choose the folder" );
			} else {
				folderChooser.setTitle( "Wählen Sie den Ordner" );
			}
			File workFolder = folderChooser.showDialog( new Stage() );
			if ( workFolder != null ) {
				labelWork.setText( workFolder.getAbsolutePath() );
				String pathtoworkdirNew = "<pathtoworkdir>"
						+ workFolder.getAbsolutePath() + "</pathtoworkdir>";
				Util.oldnewstring( pathtoworkdir, pathtoworkdirNew,
						configFile );
			}
			engine.load( "file:///" + configFile.getAbsolutePath() );
		} catch ( IOException | ParserConfigurationException
				| SAXException e1 ) {
			e1.printStackTrace();
		}
	}

	/* Wenn chooseInput betaetigt wird, kann ein Ordner ausgewaehlt werden */
	@FXML
	void chooseInput( ActionEvent e )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String inputInit = "";
			if ( !config.contains( "<standardinputdir></standardinputdir>" ) ) {
				inputInit = doc.getElementsByTagName( "standardinputdir" )
						.item( 0 ).getTextContent();
			}
			bis.close();
			doc = null;
			String standardinputdir = "<standardinputdir>" + inputInit
					+ "</standardinputdir>";
			DirectoryChooser folderChooser = new DirectoryChooser();
			if ( locale.toString().startsWith( "fr" ) ) {
				folderChooser.setTitle( "Choisissez le dossier" );
			} else if ( locale.toString().startsWith( "it" ) ) {
				folderChooser.setTitle( "Scegliere la cartella" );
			} else if ( locale.toString().startsWith( "en" ) ) {
				folderChooser.setTitle( "Choose the folder" );
			} else {
				folderChooser.setTitle( "Wählen Sie den Ordner" );
			}
			File inputFolder = folderChooser.showDialog( new Stage() );
			if ( inputFolder != null ) {
				labelInput.setText( inputFolder.getAbsolutePath() );
				String standardinputdirNew = "<standardinputdir>"
						+ inputFolder.getAbsolutePath() + "</standardinputdir>";
				Util.oldnewstring( standardinputdir, standardinputdirNew,
						configFile );
			}
			engine.load( "file:///" + configFile.getAbsolutePath() );
		} catch ( IOException | ParserConfigurationException
				| SAXException e1 ) {
			e1.printStackTrace();
		}
	}
	/* TODO --> ChoiceBox ================= */

	// Mit changeHashAlgo wird die Hash-Auswahl umgestellt
	@FXML
	void changeHashAlgo( ActionEvent event )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String hashAlgoInit = "";
			hashAlgoInit = doc.getElementsByTagName( "hash" ).item( 0 )
					.getTextContent();
			bis.close();
			doc = null;
			String hashAlgoOld = "<hash>" + hashAlgoInit + "</hash>";
			String selHashType = hashAlgo.getValue();
			String hashAlgoNew = "<hash></hash>";
			if ( selHashType.equals( "MD5" ) || selHashType.equals( "SHA-1" )
					|| selHashType.equals( "SHA-256" )
					|| selHashType.equals( "SHA-512" ) ) {
				hashAlgoNew = "<hash>" + selHashType + "</hash>";
				hashAlgo.setValue( selHashType );
			} else {
				hashAlgoNew = "<hash></hash>";
				hashAlgo.setValue( "" );
			}
			Util.oldnewstring( hashAlgoOld, hashAlgoNew, configFile );

			engine = wbv.getEngine();
			engine.load( "file:///" + configFile.getAbsolutePath() );
		} catch ( IOException | ParserConfigurationException
				| SAXException e1 ) {
			e1.printStackTrace();
		}

	}

}