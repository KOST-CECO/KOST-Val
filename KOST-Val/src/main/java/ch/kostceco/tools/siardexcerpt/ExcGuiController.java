/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2020 Claire Roethlisberger (KOST-CECO)
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

package ch.kostceco.tools.siardexcerpt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.siardexcerpt.controller.ControllerExcExcerpt;
import ch.kostceco.tools.siardexcerpt.controller.ControllerExcFinish;
import ch.kostceco.tools.siardexcerpt.controller.ControllerExcInit;
import ch.kostceco.tools.siardexcerpt.controller.ControllerExcSearch;
import ch.kostceco.tools.siardexcerpt.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExcGuiController
{
	@FXML
	private Button						buttonFile, buttonInit, buttonFinish, buttonHelp, buttonLicence,
			buttonSave, buttonPrint, buttonSearchExcerpt;

	ObservableList<String>		langList						= FXCollections.observableArrayList( "Deutsch",
			"Français", "English" );
	ObservableList<String>		ConfigTypeList			= FXCollections
			.observableArrayList( "keine Vorgabe   (..)", "Haupttabellename", "bestehende Konfig" );
	ObservableList<String>		SearchExcerptList		= FXCollections
			.observableArrayList( "Suche   (--search)", "Extraktion   (--excerpt)" );

	@FXML
	private ChoiceBox<String>	lang, configChoice, searchExcerptChoice;

	@FXML
	private Label							labelFileFolder, labelConfig, labelMainFolderName, mainFolderName,
			labelSearchExcerpt, configResult, label, labelFileSiard;

	@FXML
	private TextField					searchExcerptString;

	@FXML
	private TextArea					console;

	private PrintStream				ps;

	@FXML
	private WebView						wbv;

	private WebEngine					engine;

	private File							outputFile, outputFileXsl, siardFile,
			outputFolder = new File( System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
					+ File.separator + "Output" ),
			configFile = new File( System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
					+ File.separator + "configuration" + File.separator + "SIARDexcerpt.conf.xml" );

	private String						arg0, arg1, arg2, arg3 = "--init", arg4 = "*", dirOfJarPath;
	private String						versionSiardExcerpt	= "0.9.1";
	/* TODO: version auch hier anpassen:
	 * 
	 * 2) messages_xx.properties (de, fr, en)
	 *
	 * 3) Start-Bild (make_exe)
	 * 
	 * 4) launch_SIARDexcerpt_exe.xml --> VersionInfo */

	private Locale						locale							= Locale.getDefault();

	@FXML
	private ScrollPane				scroll;

	@FXML
	void initialize()
	{
		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)
		labelFileSiard.setText( "" );
		configResult.setText( "" );
		mainFolderName.setText( "" );

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		label.setText(
				"Copyright © KOST/CECO          SIARDexcerpt v" + versionSiardExcerpt + "          JavaFX "
						+ javafxVersion + "   &   Java-" + java6432 + " " + javaVersion + "." );

		// PrintStream in Konsole umleiten
		ps = new PrintStream( new Console( console ) );
		System.setOut( ps );
		System.setErr( ps );

		// festhalten von wo die Applikation (exe) gestartet wurde
		dirOfJarPath = "";
		try {
			/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
			 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit dirOfJarPath +
			 * File.separator + erweitern. */
			String path = new File( "" ).getAbsolutePath();
			String locationOfJarPath = path;
			dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		// Sprache definieren
		locale = Locale.getDefault();
		if ( locale.toString().startsWith( "fr" ) ) {
			locale = new Locale( "fr" );
			arg2 = locale.toString();
			lang.setValue( "Français" );
			labelFileFolder.setText( "fichier SIARD" );
			labelConfig.setText( "Configuration" );
			labelMainFolderName.setText( "Nom de la table principale" );
			labelSearchExcerpt.setText( "Recherche / Extraction" );
			buttonFile.setText( "sélectionnez" );
			buttonInit.setText( "initialiser" );
			buttonFinish.setText( "Effacer l'initialisation" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
			buttonSave.setText( "sauvegarder" );
			buttonPrint.setText( "imprimer" );
			buttonSearchExcerpt.setText( "recherche / extraction" );
			ConfigTypeList = FXCollections.observableArrayList( "pas de défaut (..)",
					"nom de la table principale", "configuration existante" );
			SearchExcerptList = FXCollections.observableArrayList( "Recherche (--search)",
					"Extraction (--excerpt)" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			locale = new Locale( "en" );
			arg2 = locale.toString();
			lang.setValue( "English" );
			labelFileFolder.setText( "SIARD file" );
			labelConfig.setText( "Configuration" );
			labelMainFolderName.setText( "Name of the main table" );
			labelSearchExcerpt.setText( "Search / Extract" );
			labelSearchExcerpt.setText( "Search / Extract" );
			buttonFile.setText( "select" );
			buttonInit.setText( "initialize" );
			buttonFinish.setText( "Clear initialization" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
			buttonSave.setText( "save" );
			buttonPrint.setText( "print" );
			buttonSearchExcerpt.setText( "search / extract" );
			ConfigTypeList = FXCollections.observableArrayList( "no defaults (..)",
					"Name of the main table", "existing configuration" );
			SearchExcerptList = FXCollections.observableArrayList( "Search (--search)",
					"Extraction (--excerpt)" );
		} else {
			locale = new Locale( "de" );
			arg2 = locale.toString();
			lang.setValue( "Deutsch" );
			labelFileFolder.setText( "SIARD-Datei" );
			labelConfig.setText( "Konfiguration" );
			labelMainFolderName.setText( "Name der Haupttabelle" );
			labelSearchExcerpt.setText( "Suchen / Extrahieren" );
			buttonFile.setText( "auswählen" );
			buttonInit.setText( "initialisieren" );
			buttonFinish.setText( "Initialisierung löschen" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenz-Informationen" );
			buttonSave.setText( "speichern" );
			buttonPrint.setText( "drucken" );
			buttonSearchExcerpt.setText( "suchen / extrahieren" );
			ConfigTypeList = FXCollections.observableArrayList( "keine Vorgaben (..)",
					"Name der Haupttabelle", "bestehende Konfiguration" );
			SearchExcerptList = FXCollections.observableArrayList( "Suchen (--search)",
					"Extraktion (--excerpt)" );
		}

		engine = wbv.getEngine();

		/* Bild mit einer Kurzanleitung zum GUI anzeigen
		 * 
		 * in Tabelle damit links */
		String pathImage = "file:/" + dirOfJarPath + File.separator + "doc" + File.separator
				+ "hilfe.jpg";
		if ( locale.toString().startsWith( "fr" ) ) {
			pathImage = "file:/" + dirOfJarPath + File.separator + "doc" + File.separator + "aide.jpg";
		} else if ( locale.toString().startsWith( "en" ) ) {
			pathImage = "file:/" + dirOfJarPath + File.separator + "doc" + File.separator + "help.jpg";
		}
		pathImage = pathImage.replace( "\\\\", "/" );
		pathImage = pathImage.replace( "\\", "/" );
		String helpImg = "<table  width=\"100%\"><tr><td><img  src='" + pathImage
				+ "'></td></tr></table>";
		String textImg = "<html><body>" + helpImg + "</body></html>";
		engine.loadContent( textImg );

		// Inaktiv vor Initialisierung
		searchExcerptString.setDisable( true );
		buttonSearchExcerpt.setDisable( true );
		buttonInit.setDisable( true );
		buttonFinish.setDisable( true );
		searchExcerptChoice.setDisable( true );

		// Speichern und drucken des Log erst bei anzeige des log moeglich
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );

		lang.getItems().addAll( langList );
		configChoice.getItems().addAll( ConfigTypeList );
		searchExcerptChoice.getItems().addAll( SearchExcerptList );

		String help1, help2, help3, help4, help5;
		if ( locale.toString().startsWith( "fr" ) ) {
			help1 = " <h2>Brèves instructions</h2> ";
			help2 = "<hr>";
			help3 = "<h4>1) Sélectionnez le fichier SIARD</h4>";
			help4 = "<h4>2) Définir la configuration</h4>";
			help5 = "<h4>3) Démarrer l'initialisation</h4>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			help1 = "<h2>Brief instructions</h2>";
			help2 = "<hr>";
			help3 = "<h4>1) Select SIARD file</h4>";
			help4 = "<h4>2) Set configuration</h4>";
			help5 = "<h4>3) Start initialisation</h4>";
		} else {
			help1 = "<h2>Kurzanleitung</h2>";
			help2 = "<hr>";
			help3 = "<h4>1) SIARD-Datei auswählen</h4>";
			help4 = "<h4>2) Konfiguration festlegen</h4>";
			help5 = "<h4>3) Initialisierung starten</h4>";
		}
		String text = "<html>" + help1 + help2 + help3 + help4 + help5 + "<br/></html>";
		engine.loadContent( text );

		context.close();

	}

	protected void updateEngine( String message )
	{
		engine.loadContent( message );
	}

	// Leitet die Konsole in die TextArea um
	public class Console extends OutputStream
	{
		private TextArea console;

		public Console( TextArea console )
		{
			this.console = console;
		}

		public void appendText( String valueOf )
		{
			Platform.runLater( () -> console.appendText( valueOf ) );
		}

		public void write( int b ) throws IOException
		{
			appendText( String.valueOf( (char) b ) );
			scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
		}
	}

	/* TODO --> Button =============== */

	@FXML
	void showManual( ActionEvent e )
	{
		console.setText( " \n" );
		/* Kurzanleitung 1. Datei oder Ordner zur Validierung angeben / auswählen 2. ggf. Konfiguration
		 * und LogType anpassen 3. Validierung starten */
		try {
			String help1, help2, help3, help4, help5;
			if ( locale.toString().startsWith( "fr" ) ) {
				help1 = " <h2>Brèves instructions</h2> ";
				help2 = "<hr>";
				help3 = "<h4>1) Sélectionnez le fichier SIARD</h4>";
				help4 = "<h4>2) Définir la configuration</h4>";
				help5 = "<h4>3) Démarrer l'initialisation</h4>";
			} else if ( locale.toString().startsWith( "en" ) ) {
				help1 = "<h2>Brief instructions</h2>";
				help2 = "<hr>";
				help3 = "<h4>1) Select SIARD file</h4>";
				help4 = "<h4>2) Set configuration</h4>";
				help5 = "<h4>3) Start initialisation</h4>";
			} else {
				help1 = "<h2>Kurzanleitung</h2>";
				help2 = "<hr>";
				help3 = "<h4>1) SIARD-Datei auswählen</h4>";
				help4 = "<h4>2) Konfiguration festlegen</h4>";
				help5 = "<h4>3) Initialisierung starten</h4>";
			}
			String text = "<html>" + help1 + help2 + help3 + help4 + help5 + "<br/></html>";
			engine.loadContent( text );

			// Handbuch in externem Viewer oeffnen dann kann parallel gearbeitet werden
			String docPath = dirOfJarPath + File.separator + "doc" + File.separator
					+ "SIARDexcerpt_Anwendungshandbuch_v0.0.6.pdf";
			File dirDoc = new File( "doc" );
			Util.switchOffConsole();
			// ansonsten wird 7x (Datei) ausgegeben
			File[] docArray = dirDoc.listFiles();
			if ( docArray != null ) { // Erforderliche Berechtigungen etc. sind vorhanden
				for ( int i = 0; i < docArray.length; i++ ) {
					if ( docArray[i].isDirectory() ) {
						System.out.print( " (Ordner)\n" );
					} else {
						System.out.print( " (Datei)\n" );
						String docName = docArray[i].getName();
						if ( docName.contains( "andbuch" ) && locale.toString().startsWith( "de" ) ) {
							docPath = docArray[i].getAbsolutePath();
						} else if ( docName.contains( "uide" ) && locale.toString().startsWith( "fr" ) ) {
							docPath = docArray[i].getAbsolutePath();
						} else if ( docName.contains( "anual" ) && locale.toString().startsWith( "en" ) ) {
							docPath = docArray[i].getAbsolutePath();
						}
					}
				}
			}
			Util.switchOnConsole();
			Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " + docPath );
		} catch ( IOException eManual ) {
			eManual.printStackTrace();
		}
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	@FXML
	void showLicence( ActionEvent e )
	{
		console.setText( " \n" );
		String licence1;
		String licence2;
		String licence3;
		String licence4;
		String licence5;
		String licence6;
		if ( locale.toString().startsWith( "fr" ) ) {
			licence1 = "<h2>Ce programme ne comporte ABSOLUMENT AUCUNE GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Il s'agit d'un logiciel libre, et vous êtes invités à le redistribuer sous certaines conditions;</h4>";
			licence4 = "- voir le manuel et GPL-3.0_COPYING.txt pour plus de détails";
			licence5 = "- SIARDexcerpt utilise des composants non modifiés d'autres fabricants en les incorporant directement dans le code source.";
			licence6 = "- Les utilisateurs de SIARDexcerpt sont priés de respecter les conditions de licence de ces composants.";
		} else if ( locale.toString().startsWith( "en" ) ) {
			licence1 = "<h2>This program comes with ABSOLUTELY NO WARRANTY.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>This is free software, and you are welcome to redistribute it under certain conditions;</h4>";
			licence4 = "- see the manual and GPL-3.0_COPYING.txt for details. ";
			licence5 = "- SIARDexcerpt uses unmodified components of other manufacturers by embedding them directly into the source code.";
			licence6 = "- Users of SIARDexcerpt are requested to adhere to these components ‘terms of licence.";
		} else {
			licence1 = "<h2>Dieses Programm kommt mit ABSOLUT KEINER GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Es handelt sich um freie Software, und Sie dürfen sie unter bestimmten Bedingungen gerne weitergeben;</h4>";
			licence4 = "- siehe das Handbuch und GPL-3.0_COPYING.txt für Einzelheiten. ";
			licence5 = "- SIARDexcerpt verwendet unmodifizierte Komponenten anderer Hersteller, indem diese direkt in den Quellcode eingebettet werden.";
			licence6 = "- Benutzer von SIARDexcerpt werden gebeten, sich an die Lizenzbedingungen dieser Komponenten zu halten.";
		}
		String text = "<html>" + licence1 + licence2 + licence3 + licence4 + "<br/>" + licence5
				+ "<br/>" + licence6 + "</html>";
		engine.loadContent( text );
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	@FXML
	void printLog( ActionEvent e )
	{
		console.setText( " \n" );
		Printer defaultprinter = Printer.getDefaultPrinter();
		Printer printerToUse = defaultprinter;
		if ( printerToUse != null ) {
			PrinterJob job = PrinterJob.createPrinterJob();
			JobSettings jobSettings = job.getJobSettings();
			PageLayout pageLayout = jobSettings.getPageLayout();
			job.setPrinter( printerToUse );
			pageLayout = printerToUse.createPageLayout( Paper.A4, PageOrientation.PORTRAIT,
					Printer.MarginType.DEFAULT );
			jobSettings.setPageLayout( pageLayout );
			job.getJobSettings();
			job.showPrintDialog( null );
			// job.showPageSetupDialog(null);
			if ( job != null ) {
				engine.print( job );
				job.endJob();
			}
		}
	}

	/* Wenn saveLog betaetigt wird, kann ein Ordner ausgewaehlt werden in welcher der output inkl xsl
	 * gespeichert wird */
	@FXML
	void saveLog( ActionEvent e )
	{
		console.setText( " \n" );
		try {
			DirectoryChooser folderChooser = new DirectoryChooser();
			String copy = "Kopiere ";
			if ( locale.toString().startsWith( "fr" ) ) {
				folderChooser
						.setTitle( "Choisissez le dossier dans lequel le résultat doit être sauvegardé" );
				copy = "Copie ";
			} else if ( locale.toString().startsWith( "en" ) ) {
				folderChooser.setTitle( "Choose the folder where the result should be saved" );
				copy = "Copy ";
			} else {
				folderChooser
						.setTitle( "Wählen Sie den Ordner in welcher das Resultat gespeichert werden soll" );
				copy = "Kopiere ";
			}
			File saveFolder = folderChooser.showDialog( new Stage() );
			if ( saveFolder != null ) {
				File outputFileXslNew = new File(
						saveFolder.getAbsolutePath() + File.separator + outputFileXsl.getName() );
				File outputFileNew = new File(
						saveFolder.getAbsolutePath() + File.separator + outputFile.getName() );
				Util.copyFile( outputFileXsl, outputFileXslNew );
				System.out.println();
				System.out.println(
						copy + outputFileXsl.getAbsolutePath() + " > " + outputFileXslNew.getAbsolutePath() );
				Util.copyFile( outputFile, outputFileNew );
				System.out.println(
						copy + outputFile.getAbsolutePath() + " > " + outputFileNew.getAbsolutePath() );
			}
		} catch ( IOException eSave ) {
			eSave.printStackTrace();
		}
	}

	/* Wenn choseFile betaetigt wird, kann eine Datei ausgewaehlt werden */
	@FXML
	void chooseFile( ActionEvent e )
	{
		console.setText( " \n" );
		// bestehende SIARD-Datei auswaehlen
		FileChooser fileChooser = new FileChooser();
		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"SIARD files (*.siard)", "*.siard" );
		fileChooser.getExtensionFilters().add( extFilter );
		// Set title
		if ( locale.toString().startsWith( "fr" ) ) {
			fileChooser.setTitle( "Choisissez le fichier d file" );
		} else {
			fileChooser.setTitle( "Wählen Sie die SIARD-Datei" );
		}
		siardFile = fileChooser.showOpenDialog( new Stage() );
		arg0 = siardFile.getAbsolutePath();
		labelFileSiard.setText( arg0 );
	}

	/* Wenn buttonInit betaetigt wird, wird die excInit gestartet mit allen Parametern. */
	@FXML
	void excInit( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Initialisierung wird durchgeführt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>L'initialisation est lancée. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>Initialisation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "SiardFile"
		 * 
		 * args[1] config
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] Modul = --init
		 * 
		 * args[4] optional und nicht verwendet */
		arg2 = "--" + locale.toString();
		arg3 = "--init";
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] ); */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> init = doExcInit( args );
		init.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			// Initialisierung erfolgreich
			@Override
			public void handle( WorkerStateEvent t )
			{
				// kein Handler Problem

				// <Label "mainFolderName" ausgeben
				Boolean initMain = true;
				try {
					Document doc = null;
					BufferedInputStream bis;
					bis = new BufferedInputStream( new FileInputStream( configFile ) );
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					doc = db.parse( bis );
					doc.normalize();
					String mfolder = doc.getElementsByTagName( "mfolder" ).item( 0 ).getTextContent();
					String mschemaname = doc.getElementsByTagName( "mschemaname" ).item( 0 ).getTextContent();
					if ( mfolder.isEmpty() || mschemaname.isEmpty() ) {
						initMain = false;
					}
					mainFolderName.setText( mschemaname + "/" + mfolder );
					bis.close();
				} catch ( ParserConfigurationException | SAXException | IOException e ) {
					e.printStackTrace();
				}

				if ( initMain ) {
					// Deaktivieren: Initialisierung; Datei auswaehlen; Configtyp
					buttonFile.setDisable( true );
					buttonInit.setDisable( true );
					configChoice.setDisable( true );

					// Aktivierung SucheExtraktion; arg4; finish
					searchExcerptChoice.setDisable( false );
					searchExcerptString.setDisable( false );
					buttonFinish.setDisable( false );

					// kein Output bei der Initialisierung welcher angezeigt werden kann
					String text = "Initialisierung durchgeführt. <br/><br/>Ab jetzt kann gesucht oder extrahiert werden.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Initialisation fait. <br/><br/>A partir de maintenant il est possible de rechercher ou d'extraire.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "Initialization done. <br/><br/>From now on can be searched or extracted.";
					}
					String help1, help2, help3, help4, help5;
					if ( locale.toString().startsWith( "fr" ) ) {
						help1 = "<h2>Brèves instructions</h2>";
						help2 = "<hr>";
						help3 = "<h4>1) Précisez s'il faut rechercher ou extraire</h4>";
						help4 = "<h4>2) Entrez le texte de recherche ou la clé d'extraction et confirmez avec Enter</h4>";
						help5 = "<h4>3) Démarrer la recherche ou l'extraction</h4>";
					} else if ( locale.toString().startsWith( "en" ) ) {
						help1 = "<h2>Brief instructions</h2>";
						help2 = "<hr>";
						help3 = "<h4>1) Specify whether to search or extract</h4>";
						help4 = "<h4>2) Enter the search text or extraction key and confirm with Enter</h4>";
						help5 = "<h4>3) Start search or extraction</h4>";
					} else {
						help1 = "<h2>Kurzanleitung</h2>";
						help2 = "<hr>";
						help3 = "<h4>1) Festlegen ob gesucht oder extrahiert werden soll</h4>";
						help4 = "<h4>2) Suchtext respektive Extraktionsschlüssel eintragen und mit Enter bestätigen</h4>";
						help5 = "<h4>3) Suche oder Extraktion starten</h4>";
					}
					String textHelp = help1 + help2 + help3 + help4 + help5 + "<br/>";
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2>" + textHelp + "</html>" );
				} else {
					// Problem: keine Haupttabelle ermittelt

					// Aktivieren: finish
					buttonFinish.setDisable( false );

					// Deaktivierung SucheExtraktion; arg4; Initialisierung; Datei auswaehlen; Configtyp
					searchExcerptChoice.setDisable( true );
					searchExcerptString.setDisable( true );
					buttonFile.setDisable( true );
					buttonInit.setDisable( true );
					configChoice.setDisable( true );

					// kein Output bei der Initialisierung welcher angezeigt werden kann
					String text = "Initialisierung durchgeführt, aber es konnte keine Haupttabelle ermittelt werden.<br/>"
							+ "Die Initialisierung muss gelöscht und mit einer anderen Konfiguration neu Initialisiert werden.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Initialisation effectuée, mais aucune table principale n'a pu être déterminée.<br/>"
								+ "L'initialisation doit être supprimée et réinitialisée avec une autre configuration.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "Initialization performed, but no main table could be determined.<br/>"
								+ "The initialization must be deleted and reinitialized with another configuration.";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				}
			}
		} );
		init.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				/* Dieser handler wird ausgefuehrt wenn die Initialisierung nicht korrekt abgelaufen ist
				 * (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0] + " " + args[1] + " " + args[2]
						+ " " + args[3];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( init ).start();
	}

	public Task<Boolean> doExcInit( String[] args )
	{
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				Boolean result = false;
				console.setText( " \n" );
				try {
					if ( ControllerExcInit.mainInit( args ) ) {
						result = true;
					} else {
						result = false;
					}
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				System.out.println( "" );
				return result;
			}
		};
	}

	/* Wenn buttonSearchExcerpt betaetigt wird, wird die excSearchExcerpt gestartet mit allen
	 * Parametern. */
	@FXML
	void excSearchExcerpt( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Suche/Extraktion wird durchgeführt. <br/><br/>Bitte warten ...</h2></html>";
		if ( arg3.equals( "--search" ) ) {
			outputFile = new File(
					outputFolder + File.separator + siardFile.getName() + "_" + arg4 + "_SIARDsearch.xml" );
			if ( locale.toString().startsWith( "fr" ) ) {
				text = "<html><h2>La recherche est lancée. <br/><br/>Veuillez patienter ...</h2></html>";
			} else if ( locale.toString().startsWith( "en" ) ) {
				text = "<html><h2>Search is performed. <br/><br/>Please wait ...</h2></html>";
			} else {
				text = "<html><h2>Suche wird durchgeführt. <br/><br/>Bitte warten ...</h2></html>";
			}
		} else {
			outputFile = new File(
					outputFolder + File.separator + siardFile.getName() + "_" + arg4 + "_SIARDexcerpt.xml" );
			if ( locale.toString().startsWith( "fr" ) ) {
				text = "<html><h2>L'extraction est lancée. <br/><br/>Veuillez patienter ...</h2></html>";
			} else if ( locale.toString().startsWith( "en" ) ) {
				text = "<html><h2>Extraction is performed. <br/><br/>Please wait ...</h2></html>";
			} else {
				text = "<html><h2>Extraktion wird durchgeführt. <br/><br/>Bitte warten ...</h2></html>";
			}
		}
		if ( outputFile.exists() ) {
			outputFile.delete();
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "SiardFile"
		 * 
		 * args[1] config
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] Modul = --search / --excerpt
		 * 
		 * args[4] Suchtext */
		arg2 = "--" + locale.toString();
		String[] args = new String[] { arg0, arg1, arg2, arg3, arg4 };
		/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " +
		 * args[4] ); */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> searchExcerpt = doExcSearchExcerpt( args );
		searchExcerpt.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			// Modul erfolgreich
			@Override
			public void handle( WorkerStateEvent t )
			{
				// kein Handler Problem
				if ( outputFile.exists() ) {
					// Output anzeigen
					engine.load( "file:///" + outputFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					buttonPrint.setDisable( false );
					buttonSave.setDisable( false );
					// vorherige logs entfernen (nicht weiterloggen in alte Logs)
					Logger rootLogger = Logger.getRootLogger();
					rootLogger.removeAllAppenders();
				} else {
					// Da es nicht erfolgreich war kann der Log nicht angezeigt werden
					String text = "Ein Fehler ist aufgetreten. Siehe Konsole.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Une erreur s`est produite. Voir console.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "An error has occurred. See console.";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				}
			}
		} );
		searchExcerpt.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				System.out.println( "setOnFailed" );
				/* Dieser handler wird ausgefuehrt wenn das Modul nicht korrekt abgelaufen ist (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0] + " " + args[1] + " " + args[2]
						+ " " + args[3] + " " + args[4];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( searchExcerpt ).start();

	}

	public Task<Boolean> doExcSearchExcerpt( String[] args )
	{
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				Boolean result = false;
				console.setText( " \n" );
				try {
					if ( arg3.equals( "--search" ) ) {
						if ( ControllerExcSearch.mainSearch( args ) ) {
							result = true;
						} else {
							result = false;
						}
					} else {
						if ( ControllerExcExcerpt.mainExcerpt( args ) ) {
							result = true;
						} else {
							result = false;
						}
					}
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				return result;
			}
		};
	}

	/* Wenn buttonFinish betaetigt wird, wird die excFinish gestartet mit allen Parametern. */
	@FXML
	void excFinish( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Initialisierung wird zurückgesetzt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>L'initialisation est annulée. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>Initialisation is reset. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "SiardFile"
		 * 
		 * args[1] config
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] Modul = --finish
		 * 
		 * args[4] optional und nicht verwendet */
		arg2 = "--" + locale.toString();
		arg3 = "--finish";
		arg4 = "*";
		searchExcerptString.setText( "" );
		if ( locale.toString().startsWith( "fr" ) ) {
			buttonSearchExcerpt.setText( "recherche / extraction" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			buttonSearchExcerpt.setText( "search / extract" );
		} else {
			buttonSearchExcerpt.setText( "suchen / extrahieren" );
		}
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] ); */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> finish = doExcFinish( args );
		finish.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			// reintitialisierung erfolgreich
			@Override
			public void handle( WorkerStateEvent t )
			{
				// kein Handler Problem

				// Aktivieren: Initialisierung; Datei auswaehlen; Configtyp
				buttonFile.setDisable( false );
				buttonInit.setDisable( false );
				configChoice.setDisable( false );

				// Inaktiv vor Initialisierung
				searchExcerptString.setDisable( true );
				buttonSearchExcerpt.setDisable( true );
				buttonFinish.setDisable( true );
				searchExcerptChoice.setDisable( true );
				mainFolderName.setText( "" );

				// Speichern und drucken des Log erst bei anzeige des log moeglich
				buttonPrint.setDisable( true );
				buttonSave.setDisable( true );

				// <Label "mainFolderName" ausgeben
				try {
					Document doc = null;
					BufferedInputStream bis;
					bis = new BufferedInputStream( new FileInputStream( configFile ) );
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					doc = db.parse( bis );
					doc.normalize();
					String mfolder = doc.getElementsByTagName( "mfolder" ).item( 0 ).getTextContent();
					String mschemaname = doc.getElementsByTagName( "mschemaname" ).item( 0 ).getTextContent();
					mainFolderName.setText( mschemaname + "/" + mfolder );
					bis.close();
				} catch ( ParserConfigurationException | SAXException | IOException e ) {
					e.printStackTrace();
				}

				// kein Output bei der Initialisierung welcher angezeigt werden kann
				String text = "<html><h2>Initialisierung wurde zurückgesetzt. </h2></html>";
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "<html><h2>L'initialisation est annulée. </h2></html>";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "<html><h2>Initialisation is reset.</h2></html>";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				String help1, help2, help3, help4, help5;
				if ( locale.toString().startsWith( "fr" ) ) {
					help1 = " <h2>Brèves instructions</h2> ";
					help2 = "<hr>";
					help3 = "<h4>1) Sélectionnez le fichier SIARD</h4>";
					help4 = "<h4>2) Définir la configuration</h4>";
					help5 = "<h4>3) Démarrer l'initialisation</h4>";
				} else if ( locale.toString().startsWith( "en" ) ) {
					help1 = "<h2>Brief instructions</h2>";
					help2 = "<hr>";
					help3 = "<h4>1) Select SIARD file</h4>";
					help4 = "<h4>2) Set configuration</h4>";
					help5 = "<h4>3) Start initialisation</h4>";
				} else {
					help1 = "<h2>Kurzanleitung</h2>";
					help2 = "<hr>";
					help3 = "<h4>1) SIARD-Datei auswählen</h4>";
					help4 = "<h4>2) Konfiguration festlegen</h4>";
					help5 = "<h4>3) Initialisierung starten</h4>";
				}
				String textHelp = help1 + help2 + help3 + help4 + help5;
				engine.loadContent( "<html><h2>" + text + "</h2>" + textHelp + "</html>" );
			}
		} );
		finish.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				/* Dieser handler wird ausgefuehrt wenn die Initialisierung nicht korrekt abgelaufen ist
				 * (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0] + " " + args[1] + " " + args[2]
						+ " " + args[3];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( finish ).start();
	}

	public Task<Boolean> doExcFinish( String[] args )
	{
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				Boolean result = false;
				console.setText( " \n" );
				try {
					if ( ControllerExcFinish.mainFinish( args ) ) {
						result = true;
					} else {
						result = false;
					}
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				System.out.println( "" );
				return result;
			}
		};
	}

	/* TODO --> TextField ================= */

	/* Wenn Aenderungen an changeSearchExcerpt gemacht wird, wird es ausgeloest */
	@FXML
	void changeSearchExcerpt( ActionEvent event )
	{
		console.setText( " \n" );
		arg4 = searchExcerptString.getText();
		buttonSearchExcerpt.setDisable( false );
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeSearchExcerpt wird die Suche oder Extraktion ausgewaehlt
	@FXML
	void choiceSearchExcerpt( ActionEvent event )
	{
		console.setText( " \n" );
		String locSearch, locExcerpt;

		if ( locale.toString().startsWith( "fr" ) ) {
			locSearch = "recherche";
			locExcerpt = "extraction";
		} else if ( locale.toString().startsWith( "en" ) ) {
			locSearch = "search";
			locExcerpt = "extract";
		} else {
			locSearch = "suchen";
			locExcerpt = "extrahieren";
		}
		String selSearchExcerptChoice = searchExcerptChoice.getValue();
		if ( selSearchExcerptChoice.contains( "(--search)" ) ) {
			// suche
			arg3 = "--search";
			searchExcerptString.setDisable( false );
			buttonSearchExcerpt.setText( locSearch );

		} else if ( selSearchExcerptChoice.contains( "(--excerpt)" ) ) {
			// Extraktion
			arg3 = "--excerpt";
			searchExcerptString.setDisable( false );
			buttonSearchExcerpt.setText( locExcerpt );
		} else {
			arg3 = "--init";
			searchExcerptString.setDisable( true );
			buttonSearchExcerpt.setText( locSearch + "/" + locExcerpt );
		}
	}

	// Mit changeConfigType wird die Config ausgewaehlt oder gesetzt
	@FXML
	void changeConfigType( ActionEvent event )
	{
		console.setText( " \n" );
		String selConfigChoice = configChoice.getValue();
		if ( selConfigChoice.contains( "(..)" ) ) {
			// keine Vorgaben (..)
			arg1 = "(..)";
		} else if ( selConfigChoice.contains( "onfig" ) ) {
			// bestehende Konfiguration auswaehlen
			FileChooser fileChooser = new FileChooser();
			// Set initial directory
			File configInit = new File( dirOfJarPath + File.separator + "configuration" );
			if ( configInit.exists() ) {
				fileChooser.setInitialDirectory( configInit );
			} else {
				fileChooser.setInitialDirectory( configInit.getParentFile() );
			}
			// Set extension filter
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "XML files (*.xml)",
					"*.xml" );
			fileChooser.getExtensionFilters().add( extFilter );
			// Set title
			if ( locale.toString().startsWith( "fr" ) ) {
				fileChooser.setTitle( "Choisissez le fichier de configuration" );
			} else if ( locale.toString().startsWith( "en" ) ) {
				fileChooser.setTitle( "Choose the config file" );
			} else {
				fileChooser.setTitle( "Wählen Sie die Konfigurationsdatei" );
			}
			File configFile = fileChooser.showOpenDialog( new Stage() );
			arg1 = configFile.getAbsolutePath();
		} else {
			// Name der Haupttabelle
			TextInputDialog dialog = new TextInputDialog();
			// Set title & header text
			if ( locale.toString().startsWith( "fr" ) ) {
				dialog.setTitle( "Nom de la table principale" );
				dialog.setHeaderText( "Quel est le nom de la table principale" );
			} else if ( locale.toString().startsWith( "en" ) ) {
				dialog.setTitle( "Name of the main table" );
				dialog.setHeaderText( "What is the name of the main table" );
			} else {
				dialog.setTitle( "Name der Haupttabelle" );
				dialog.setHeaderText( "Wie lautet der Name der Haupttabelle" );
			}

			// Show the dialog and capture the result.
			Optional<String> result = dialog.showAndWait();

			// If the "Okay" button was clicked, the result will contain our String in the get() method
			if ( result.isPresent() ) {
				arg1 = result.get();
			} else {
				arg1 = "(..)";
			}
			// mainFolderName.setText( arg1 );
		}
		configResult.setText( arg1 );
		// Initialisierung moeglich; Button aktivieren
		buttonInit.setDisable( false );
	}

	// Mit changeLang wird die Sprache umgestellt
	@FXML
	void changeLang( ActionEvent event )
	{
		console.setText( " \n" );
		String selLang = lang.getValue();
		if ( selLang.equals( "Français" ) ) {
			locale = new Locale( "fr" );
			arg2 = locale.toString();
			lang.setValue( "Français" );
			labelFileFolder.setText( "fichier SIARD" );
			labelConfig.setText( "Configuration" );
			labelMainFolderName.setText( "Nom de la table principale" );
			labelSearchExcerpt.setText( "Recherche / Extraction" );
			buttonFile.setText( "sélectionnez" );
			buttonInit.setText( "initialiser" );
			buttonFinish.setText( "Effacer l'initialisation" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
			buttonSave.setText( "sauvegarder" );
			buttonPrint.setText( "imprimer" );
			buttonSearchExcerpt.setText( "recherche / extraction" );
			ConfigTypeList = FXCollections.observableArrayList( "pas de défaut (..)",
					"nom de la table principale", "configuration existante" );
			SearchExcerptList = FXCollections.observableArrayList( "Recherche (--search)",
					"Extraction (--excerpt)" );
		} else if ( selLang.equals( "English" ) ) {
			locale = new Locale( "en" );
			arg2 = locale.toString();
			lang.setValue( "English" );
			labelFileFolder.setText( "SIARD file" );
			labelConfig.setText( "Configuration" );
			labelMainFolderName.setText( "Name of the main table" );
			labelSearchExcerpt.setText( "Search / Extract" );
			labelSearchExcerpt.setText( "Search / Extract" );
			buttonFile.setText( "select" );
			buttonInit.setText( "initialize" );
			buttonFinish.setText( "Clear initialization" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
			buttonSave.setText( "save" );
			buttonPrint.setText( "print" );
			buttonSearchExcerpt.setText( "search / extract" );
			ConfigTypeList = FXCollections.observableArrayList( "no defaults (..)",
					"Name of the main table", "existing configuration" );
			SearchExcerptList = FXCollections.observableArrayList( "Search (--search)",
					"Extraction (--excerpt)" );
		} else {
			locale = new Locale( "de" );
			arg2 = locale.toString();
			lang.setValue( "Deutsch" );
			labelFileFolder.setText( "SIARD-Datei" );
			labelConfig.setText( "Konfiguration" );
			labelMainFolderName.setText( "Name der Haupttabelle" );
			labelSearchExcerpt.setText( "Suchen / Extrahieren" );
			buttonFile.setText( "auswählen" );
			buttonInit.setText( "initialisieren" );
			buttonFinish.setText( "Initialisierung löschen" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenz-Informationen" );
			buttonSave.setText( "speichern" );
			buttonPrint.setText( "drucken" );
			buttonSearchExcerpt.setText( "suchen / extrahieren" );
			ConfigTypeList = FXCollections.observableArrayList( "keine Vorgaben (..)",
					"Name der Haupttabelle", "bestehende Konfiguration" );
			SearchExcerptList = FXCollections.observableArrayList( "Suchen (--search)",
					"Extraktion (--excerpt)" );
		}
	}

}