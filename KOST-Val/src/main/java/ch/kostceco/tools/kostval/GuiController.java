/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.controller.ControllerInit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GuiController
{
	@FXML
	private Button				buttonHelp, buttonFolder, buttonFile,
			buttonFormat, buttonSip, buttonOnlySip, buttonLicence, buttonChange,
			buttonShowConfig, buttonPrint, buttonSave;

	ObservableList<String>		langList		= FXCollections
			.observableArrayList( "Deutsch", "Fran�ais", "English" );
	ObservableList<String>		logTypeList		= FXCollections
			.observableArrayList( " --xml (default)", " --min (valid/invalid)",
					" --max (verbose)" );
	// TODO "LogType: --min",

	@FXML
	private ChoiceBox<String>	lang, logType;

	@FXML
	private Label				labelFileFolder, labelStart, labelConfig, label;

	@FXML
	private TextField			fileFolder;

	@FXML
	private TextArea			console;

	private PrintStream			ps;

	@FXML
	private WebView				wbv;

	private WebEngine			engine;

	private File				logFile,
			configFile = new File( System.getenv( "USERPROFILE" )
					+ File.separator + ".kost-val_2x" + File.separator
					+ "configuration" + File.separator + "kostval.conf.xml" );

	private String				arg0, arg1, arg2, arg3 = "--xml", dirOfJarPath,
			initInstructionsDe, initInstructionsFr, initInstructionsEn;
	private String				versionKostVal	= "2.1.2.0";
	/*
	 * TODO: versionKostVal auch hier anpassen:
	 * 
	 * 2) cmdKOSTVal.java
	 *
	 * 3) ConfigController
	 * 
	 * 4) Konfigurationsdatei inkl 3x xsl
	 * 
	 * 5) Start-Bild (make_exe)
	 * 
	 * 6) launch_KOST-Val_exe.xml --> VersionInfo
	 */

	private Locale				locale			= Locale.getDefault();

	boolean						booSip			= false;

	@FXML
	private ScrollPane			scroll;

	@FXML
	void initialize()
	{
		/*
		 * TODO
		 * 
		 * LogTyp --min programmieren
		 * 
		 * openjdk inkl javafx FULL-Version
		 * (https://bell-sw.com/pages/downloads/#/java-14-current)
		 */

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		Util.switchOffConsole();
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );
		Util.switchOnConsole();

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		label.setText( "Copyright � KOST/CECO          KOST-Val v"
				+ versionKostVal + "          JavaFX " + javafxVersion
				+ "   &   Java-" + java6432 + " " + javaVersion + "." );

		// PrintStream in Konsole umleiten
		ps = new PrintStream( new Console( console ) );
		System.setOut( ps );
		System.setErr( ps );

		// festhalten von wo die Applikation (exe) gestartet wurde
		dirOfJarPath = "";
		try {
			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist
			 * ein generelles TODO in allen Modulen. Zuerst immer dirOfJarPath
			 * ermitteln und dann alle Pfade mit dirOfJarPath + File.separator +
			 * erweitern.
			 */
			String path = new File( "" ).getAbsolutePath();
			String locationOfJarPath = path;
			dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" )
					|| locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		// Sprache definieren
		locale = Locale.getDefault();
		try {
			if ( locale.toString().startsWith( "fr" ) ) {
				locale = new Locale( "fr" );
				arg2 = locale.toString();
				lang.setValue( "Fran�ais" );
				buttonFormat.setText( "format uniquement" );
				buttonSip.setText( "SIP incl. formats" );
				buttonOnlySip.setText( "SIP uniquement" );
				labelFileFolder.setText( "S�lectionnez" );
				buttonFolder.setText( "dossier" );
				buttonFile.setText( "fichier" );
				buttonHelp.setText( "Aide ?" );
				buttonLicence.setText( "Informations sur la licence" );
				buttonChange.setText( "changer" );
				buttonShowConfig.setText( "afficher" );
				labelStart.setText( "Lancer la validation" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "sauvegarder" );
				buttonPrint.setText( "imprimer" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-DE.xsl",
							"kostval-conf-FR.xsl", configFile );
					Util.oldnewstring( "kostval-conf-EN.xsl",
							"kostval-conf-FR.xsl", configFile );
				}
			} else if ( locale.toString().startsWith( "en" ) ) {
				locale = new Locale( "en" );
				arg2 = locale.toString();
				lang.setValue( "English" );
				buttonFormat.setText( "Format only" );
				buttonSip.setText( "SIP incl. Format" );
				buttonOnlySip.setText( "SIP only" );
				labelFileFolder.setText( "Select file / folder" );
				buttonFolder.setText( "folder" );
				buttonFile.setText( "file" );
				buttonHelp.setText( "Help ?" );
				buttonLicence.setText( "License information" );
				buttonChange.setText( "change" );
				buttonShowConfig.setText( "show" );
				labelStart.setText( "Start validation" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "save" );
				buttonPrint.setText( "print" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-DE.xsl",
							"kostval-conf-EN.xsl", configFile );
					Util.oldnewstring( "kostval-conf-FR.xsl",
							"kostval-conf-EN.xsl", configFile );
				}
			} else {
				locale = new Locale( "de" );
				arg2 = locale.toString();
				lang.setValue( "Deutsch" );
				buttonFormat.setText( "nur Formate" );
				buttonSip.setText( "SIP inkl. Formate" );
				buttonOnlySip.setText( "nur SIP" );
				labelFileFolder.setText( "W�hle Datei / Ordner" );
				buttonFolder.setText( "Ordner" );
				buttonFile.setText( "Datei" );
				buttonHelp.setText( "Hilfe ?" );
				buttonLicence.setText( "Lizenzinformationen" );
				buttonChange.setText( "anpassen" );
				buttonShowConfig.setText( "anzeigen" );
				labelStart.setText( "Starte Validierung" );
				labelConfig.setText( "Konfiguration" );
				buttonSave.setText( "speichern" );
				buttonPrint.setText( "drucken" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-FR.xsl",
							"kostval-conf-DE.xsl", configFile );
					Util.oldnewstring( "kostval-conf-EN.xsl",
							"kostval-conf-DE.xsl", configFile );
				}
			}
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}

		engine = wbv.getEngine();

		/*
		 * Bild mit einer Kurzanleitung zum GUI anzeigen
		 * 
		 * in Tabelle damit links
		 */
		String pathImage = "file:/" + dirOfJarPath + File.separator + "doc"
				+ File.separator + "hilfe.jpg";
		if ( locale.toString().startsWith( "fr" ) ) {
			pathImage = "file:/" + dirOfJarPath + File.separator + "doc"
					+ File.separator + "aide.jpg";
		} else if ( locale.toString().startsWith( "en" ) ) {
			pathImage = "file:/" + dirOfJarPath + File.separator + "doc"
					+ File.separator + "help.jpg";
		}
		pathImage = pathImage.replace( "\\\\", "/" );
		pathImage = pathImage.replace( "\\", "/" );
		String helpImg = "<table  width=\"100%\"><tr><td><img  src='"
				+ pathImage + "'></td></tr></table>";
		String textImg = "<html><body>" + helpImg + "</body></html>";
		engine.loadContent( textImg );

		// Format und Sip Validierung erst moeglich wenn fileFolder ausgefuellt
		buttonSip.setDisable( true );
		buttonOnlySip.setDisable( true );
		buttonFormat.setDisable( true );

		// Speichern und drucken des Log erst bei anzeige des log moeglich
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );

		lang.getItems().addAll( langList );
		logType.getItems().addAll( logTypeList );

		/*
		 * Kontrolle der wichtigsten Eigenschaften: Log-Verzeichnis,
		 * Arbeitsverzeichnis, Java, jhove Configuration,
		 * Konfigurationsverzeichnis, path.tmp
		 */
		ControllerInit controllerInit = (ControllerInit) context
				.getBean( "controllerInit" );
		boolean init, init2;
		try {
			init = controllerInit.init( locale, dirOfJarPath, versionKostVal );
			if ( !init ) {
				// zweiter Versuch
				console.setText( " \n" );
				init2 = controllerInit.init( locale, dirOfJarPath,
						versionKostVal );
				if ( !init2 ) {
					// Fehler: es wird abgebrochen
					String text = "Ein Fehler ist aufgetreten. Siehe Konsole.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Une erreur s`est produite. Voir console.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "An error has occurred. See console.";
					}
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				} else {
					console.setText( " \n" );
				}
			} else {
				console.setText( " \n" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		/* Kurzanleitung zum GUI anzeigen */
		String help1, help2, help3, help4, help5;
		help1 = " <h2>Br�ves instructions</h2> ";
		help2 = "<hr>";
		help3 = "<h3>1. Pr�cisez / s�lectionnez le fichier ou le dossier � valider</h3>";
		help4 = "<h3>2. Ajuster la configuration et le LogType si n�cessaire</h3>";
		help5 = "<h3>3. Commencer la validation</h3>";
		initInstructionsFr = "<html>" + help1 + help2 + help3 + help4 + help5
				+ "<br/></html>";
		help1 = "<h2>Brief instructions</h2>";
		help3 = "<h3>1. Specify / select file or folder for validation</h3>";
		help4 = "<h3>2. Adjust configuration and LogType if necessary</h3>";
		help5 = "<h3>3. Start validation</h3>";
		initInstructionsEn = "<html>" + help1 + help2 + help3 + help4 + help5
				+ "<br/></html>";
		help1 = "<h2>Kurzanleitung</h2>";
		help3 = "<h3>1. Datei oder Ordner zur Validierung angeben / ausw�hlen</h3>";
		help4 = "<h3>2. Ggf. Konfiguration und LogType anpassen</h3>";
		help5 = "<h3>3. Validierung starten</h3>";
		initInstructionsDe = "<html>" + help1 + help2 + help3 + help4 + help5
				+ "<br/></html>";
		String initInstructions = initInstructionsDe;
		if ( locale.toString().startsWith( "fr" ) ) {
			initInstructions = initInstructionsFr;
		} else if ( locale.toString().startsWith( "en" ) ) {
			initInstructions = initInstructionsEn;
		} else {
			initInstructions = initInstructionsDe;
		}
		engine.loadContent( initInstructions );

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
		/*
		 * Kurzanleitung 1. Datei oder Ordner zur Validierung angeben /
		 * ausw�hlen 2. ggf. Konfiguration und LogType anpassen 3. Validierung
		 * starten
		 */
		try {
			String initInstructions = initInstructionsDe;
			if ( locale.toString().startsWith( "fr" ) ) {
				initInstructions = initInstructionsFr;
			} else if ( locale.toString().startsWith( "en" ) ) {
				initInstructions = initInstructionsEn;
			} else {
				initInstructions = initInstructionsDe;
			}
			engine.loadContent( initInstructions );

			// Handbuch in externem Viewer oeffnen dann kann parallel gearbeitet
			// werden
			String docPath = dirOfJarPath + File.separator + "doc"
					+ File.separator + "Anwendungshandbuch.pdf";
			File dirDoc = new File( "doc" );
			File[] docArray = dirDoc.listFiles();
			if ( docArray != null ) { // Erforderliche Berechtigungen etc. sind
										// vorhanden
				for ( int i = 0; i < docArray.length; i++ ) {
					if ( docArray[i].isDirectory() ) {
						// System.out.print( " (Ordner)\n" );
					} else {
						// System.out.print( " (Datei)\n" );
						String docName = docArray[i].getName();
						if ( docName.contains( "andbuch" )
								&& locale.toString().startsWith( "de" ) ) {
							docPath = docArray[i].getAbsolutePath();
						} else if ( docName.contains( "uide" )
								&& locale.toString().startsWith( "fr" ) ) {
							docPath = docArray[i].getAbsolutePath();
						} else if ( docName.contains( "anual" )
								&& locale.toString().startsWith( "en" ) ) {
							docPath = docArray[i].getAbsolutePath();
						}
					}
				}
			}
			Runtime.getRuntime()
					.exec( "rundll32 url.dll,FileProtocolHandler " + docPath );
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
			licence3 = "<h4>Il s'agit d'un logiciel libre, et vous �tes invit�s � le redistribuer sous certaines conditions;</h4>";
			licence4 = "- voir le manuel et GPL-3.0_COPYING.txt pour plus de d�tails";
			licence5 = "- KOST-Val utilise des composants non modifi�s d'autres fabricants en les incorporant directement dans le code source.";
			licence6 = "- Les utilisateurs de KOST-Val sont pri�s de respecter les conditions de licence de ces composants.";
		} else if ( locale.toString().startsWith( "en" ) ) {
			licence1 = "<h2>This program comes with ABSOLUTELY NO WARRANTY.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>This is free software, and you are welcome to redistribute it under certain conditions;</h4>";
			licence4 = "- see the manual and GPL-3.0_COPYING.txt for details. ";
			licence5 = "- KOST-Val uses unmodified components of other manufacturers by embedding them directly into the source code.";
			licence6 = "- Users of KOST-Val are requested to adhere to these components �terms of licence.";
		} else {
			licence1 = "<h2>Dieses Programm kommt mit ABSOLUT KEINER GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Es handelt sich um freie Software, und Sie d�rfen sie unter bestimmten Bedingungen gerne weitergeben;</h4>";
			licence4 = "- siehe das Handbuch und GPL-3.0_COPYING.txt f�r Einzelheiten. ";
			licence5 = "- KOST-Val verwendet unmodifizierte Komponenten anderer Hersteller, indem diese direkt in den Quellcode eingebettet werden.";
			licence6 = "- Benutzer von KOST-Val werden gebeten, sich an die Lizenzbedingungen dieser Komponenten zu halten.";
		}
		String text = "<html>" + licence1 + licence2 + licence3 + licence4
				+ "<br/>" + licence5 + "<br/>" + licence6 + "</html>";
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
		String strHeaderText = "W�hlen Sie einen Drucker aus den verf�gbaren Druckern";
		String strTitle = "Druckerauswahl";
		String strNoPrinter = "Kein Drucker. Es ist kein Drucker auf Ihrem System installiert.";
		if ( locale.toString().startsWith( "fr" ) ) {
			strHeaderText = "Choisissez une imprimante parmi les imprimantes disponibles";
			strTitle = "Choix de l'imprimante";
			strNoPrinter = "Pas d'imprimante. Aucune imprimante n'est install�e sur votre syst�me";
		} else if ( locale.toString().startsWith( "en" ) ) {
			strHeaderText = "Choose a printer from available printers";
			strTitle = "Printer Choice";
			strNoPrinter = "No printer. There is no printer installed on your system.";
		}
		if ( printerToUse != null ) {

			ChoiceDialog<Printer> dialog = new ChoiceDialog<Printer>(
					defaultprinter, Printer.getAllPrinters() );
			dialog.initStyle( StageStyle.UTILITY ); // Title ohne icon
			dialog.setHeaderText( strHeaderText );
			dialog.setContentText( null );
			dialog.setTitle( strTitle );
			Optional<Printer> opt = dialog.showAndWait();
			if ( opt.isPresent() ) {
				// ein Drucker wurde ausgewaehlt
				printerToUse = opt.get();
				// start printing ...
				PrinterJob job = PrinterJob.createPrinterJob();
				JobSettings jobSettings = job.getJobSettings();
				PageLayout pageLayout = jobSettings.getPageLayout();
				job.setPrinter( printerToUse );
				pageLayout = printerToUse.createPageLayout( Paper.A4,
						PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT );
				jobSettings.setPageLayout( pageLayout );
				job.getJobSettings();
				/*
				 * showPrintDialog nicht verwenden, da ansonsten nicht
				 * zuverlaessig abgebrochen werden kann. Besser ist es �ber
				 * einen ChoiceDialog den Drucker auszuwaehlen und wenn einer
				 * ausgewaehlt wurde zu drucken!
				 */
				if ( job != null ) {
					engine.print( job );
					job.endJob();
				}
			} else {
				// System.out.println("Kein Drucker ausgewaehlt. [Abbrechen]
				// gedrueckt");
			}
		} else {
			System.out.println( strNoPrinter );
		}
	}

	/*
	 * Wenn saveLog betaetigt wird, kann ein Ordner ausgewaehlt werden in
	 * welcher der log inkl xsl gespeichert wird
	 */
	@FXML
	void saveLog( ActionEvent e )
	{
		console.setText( " \n" );
		try {
			DirectoryChooser folderChooser = new DirectoryChooser();
			String copy = "Kopiere ";
			if ( locale.toString().startsWith( "fr" ) ) {
				folderChooser.setTitle(
						"Choisissez le dossier dans lequel le log doit �tre sauvegard�" );
				copy = "Copie ";
			} else if ( locale.toString().startsWith( "en" ) ) {
				folderChooser.setTitle(
						"Choose the folder where the log should be saved" );
				copy = "Copy ";
			} else {
				folderChooser.setTitle(
						"W�hlen Sie den Ordner in welcher der Log gespeichert werden soll" );
				copy = "Kopiere ";
			}
			File saveFolder = folderChooser.showDialog( new Stage() );
			if ( saveFolder != null ) {
				File logFileXsl = new File( System.getenv( "USERPROFILE" )
						+ File.separator + ".kost-val_2x" + File.separator
						+ "logs" + File.separator + "kost-val.xsl" );
				File logFileXslNew = new File( saveFolder.getAbsolutePath()
						+ File.separator + "kost-val.xsl" );
				File logFileNew = new File( saveFolder.getAbsolutePath()
						+ File.separator + logFile.getName() );
				Util.copyFile( logFileXsl, logFileXslNew );
				System.out.println();
				System.out.println( copy + logFileXsl.getAbsolutePath() + " > "
						+ logFileXslNew.getAbsolutePath() );
				Util.copyFile( logFile, logFileNew );
				System.out.println( copy + logFile.getAbsolutePath() + " > "
						+ logFileNew.getAbsolutePath() );
			}
		} catch ( IOException eSave ) {
			eSave.printStackTrace();
		}
	}

	@FXML
	void showConfig( ActionEvent e )
	{
		console.setText( " \n" );
		engine.load( "file:///" + configFile.getAbsolutePath() );
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	/*
	 * Wenn valFormat betaetigt wird, wird die Formatvalidierung gestartet mit
	 * allen Parametern.
	 */
	@FXML
	void valFormat( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Formatvalidierung wird durchgef�hrt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La validation du format est lanc�e. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>Format validation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );

		// Keine Eingabe w�hrend der Validierung
		buttonHelp.setDisable( true );
		buttonFolder.setDisable( true );
		buttonFile.setDisable( true );
		buttonFormat.setDisable( true );
		if ( !buttonSip.isDisable() ) {
			booSip = true;
		}
		buttonSip.setDisable( true );
		buttonOnlySip.setDisable( true );
		buttonLicence.setDisable( true );
		buttonChange.setDisable( true );
		buttonShowConfig.setDisable( true );
		fileFolder.setDisable( true );
		lang.setDisable( true );
		logType.setDisable( true );

		/*
		 * hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--format"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (=
		 * xml+verbose TODO GUI)
		 * 
		 * 2 und 3 waeren optional werden aber mitgegeben
		 */
		arg0 = "--format";
		arg2 = "--" + locale.toString();
		arg1 = fileFolder.getText();
		File arg1File = new File( arg1 );
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		logFile = new File( System.getenv( "USERPROFILE" ) + File.separator
				+ ".kost-val_2x" + File.separator + "logs" + File.separator
				+ arg1File.getName() + ".kost-val.log.xml" );
		// falls das File bereits existiert, z.B. von einem vorhergehenden
		// Durchlauf, loeschen wir es
		if ( logFile.exists() ) {
			logFile.delete();
		}
		/*
		 * System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2]
		 * + " " + args[3] );
		 */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> val = doVal( args );
		val.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				if ( booSip ) {
					buttonSip.setDisable( false );
					buttonOnlySip.setDisable( false );
				}
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				// kein Handler Problem
				if ( logFile.exists() ) {
					/*
					 * Dieser handler wird bei einer erfolgreichen Validierung
					 * ausgefuehrt.
					 * 
					 * Da es erfolgreich war (valid / invalid) kann der Log
					 * angezeigt werden
					 */
					engine.load( "file:///" + logFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					buttonPrint.setDisable( false );
					buttonSave.setDisable( false );
				} else {
					// Da es nicht erfolgreich war kann der Log nicht angezeigt
					// werden
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
		val.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				if ( booSip ) {
					buttonSip.setDisable( false );
					buttonOnlySip.setDisable( false );
				}
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				/*
				 * Dieser handler wird ausgefuehrt wenn die Validierung nicht
				 * korrekt abgelaufen ist (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt
				 * werden
				 */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0]
						+ " " + args[1] + " " + args[2] + " " + args[3];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent(
						"<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( val ).start();
	}

	public Task<Boolean> doVal( String[] args )
	{
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				Boolean result = false;
				console.setText( " \n" );
				System.out.println( "" );
				System.out.println( "KOST-Val" );
				try {
					if ( KOSTVal.main( args, versionKostVal ) ) {
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

	/*
	 * Wenn valSip betaetigt wird, wird die SIP-Validierung gestartet mit allen
	 * Parametern.
	 */
	@FXML
	void valSip( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>SIP-Validierung wird durchgef�hrt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La validation du SIP est lanc�e. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>SIP validation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );

		// Keine Eingabe w�hrend der Validierung
		buttonHelp.setDisable( true );
		buttonFolder.setDisable( true );
		buttonFile.setDisable( true );
		buttonFormat.setDisable( true );
		buttonSip.setDisable( true );
		buttonOnlySip.setDisable( true );
		buttonLicence.setDisable( true );
		buttonChange.setDisable( true );
		buttonShowConfig.setDisable( true );
		fileFolder.setDisable( true );
		lang.setDisable( true );
		logType.setDisable( true );

		/*
		 * hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--sip"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--xml" / "--min" / "--max" (= xml+verbose TODO GUI)
		 * 
		 * 2 und 3 waeren optional werden aber mitgegeben
		 */
		arg0 = "--sip";
		arg2 = "--" + locale.toString();
		arg1 = fileFolder.getText();
		File arg1File = new File( arg1 );
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator
				+ ".kost-val_2x" + File.separator + "logs" + File.separator
				+ arg1File.getName() + ".kost-val.log.xml" );
		// falls das File bereits existiert, z.B. von einem vorhergehenden
		// Durchlauf, loeschen wir es
		if ( logFile.exists() ) {
			logFile.delete();
		}
		/*
		 * System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2]
		 * + " " + args[3] );
		 */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> val = doVal( args );
		val.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				buttonSip.setDisable( false );
				buttonOnlySip.setDisable( false );
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				// kein Handler Problem
				if ( logFile.exists() ) {
					/*
					 * Dieser handler wird bei einer erfolgreichen Validierung
					 * ausgefuehrt.
					 * 
					 * Da es erfolgreich war (valid / invalid) kann der Log
					 * angezeigt werden
					 */
					engine.load( "file:///" + logFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					buttonPrint.setDisable( false );
					buttonSave.setDisable( false );
				} else {
					// Da es nicht erfolgreich war kann der Log nicht angezeigt
					// werden
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
		val.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				buttonSip.setDisable( false );
				buttonOnlySip.setDisable( false );
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				/*
				 * Dieser handler wird ausgefuehrt wenn die Validierung nicht
				 * korrekt abgelaufen ist (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt
				 * werden
				 */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0]
						+ " " + args[1] + " " + args[2] + " " + args[3];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent(
						"<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( val ).start();
	}

	/*
	 * Wenn valOnlySip betaetigt wird, wird die SIP-Validierung gestartet mit
	 * allen Parametern.
	 */
	@FXML
	void valOnlySip( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Eine reine SIP-Validierung wird durchgef�hrt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La validation du SIP pure est lanc�e. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>A pure SIP validation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );

		// Keine Eingabe w�hrend der Validierung
		buttonHelp.setDisable( true );
		buttonFolder.setDisable( true );
		buttonFile.setDisable( true );
		buttonFormat.setDisable( true );
		buttonSip.setDisable( true );
		buttonOnlySip.setDisable( true );
		buttonLicence.setDisable( true );
		buttonChange.setDisable( true );
		buttonShowConfig.setDisable( true );
		fileFolder.setDisable( true );
		lang.setDisable( true );
		logType.setDisable( true );

		/*
		 * hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--onlysip"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (=
		 * xml+verbose TODO GUI)
		 * 
		 * 2 und 3 waeren optional werden aber mitgegeben
		 */
		arg0 = "--onlysip";
		arg2 = "--" + locale.toString();
		arg1 = fileFolder.getText();
		File arg1File = new File( arg1 );
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator
				+ ".kost-val_2x" + File.separator + "logs" + File.separator
				+ arg1File.getName() + ".kost-val.log.xml" );
		// falls das File bereits existiert, z.B. von einem vorhergehenden
		// Durchlauf, loeschen wir es
		if ( logFile.exists() ) {
			logFile.delete();
		}
		/*
		 * System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2]
		 * + " " + args[3] );
		 */
		// KOSTVal.main im Hintergrund Task (val) starten
		final Task<Boolean> val = doVal( args );
		val.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				buttonSip.setDisable( false );
				buttonOnlySip.setDisable( false );
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				// kein Handler Problem
				if ( logFile.exists() ) {
					/*
					 * Dieser handler wird bei einer erfolgreichen Validierung
					 * ausgefuehrt.
					 * 
					 * Da es erfolgreich war (valid / invalid) kann der Log
					 * angezeigt werden
					 */
					engine.load( "file:///" + logFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					buttonPrint.setDisable( false );
					buttonSave.setDisable( false );
				} else {
					// Da es nicht erfolgreich war kann der Log nicht angezeigt
					// werden
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
		val.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				// Validierung beeindet Buttons freigeben
				buttonHelp.setDisable( false );
				buttonFolder.setDisable( false );
				buttonFile.setDisable( false );
				buttonFormat.setDisable( false );
				buttonSip.setDisable( false );
				buttonOnlySip.setDisable( false );
				buttonLicence.setDisable( false );
				buttonChange.setDisable( false );
				buttonShowConfig.setDisable( false );
				fileFolder.setDisable( false );
				lang.setDisable( false );
				logType.setDisable( false );

				/*
				 * Dieser handler wird ausgefuehrt wenn die Validierung nicht
				 * korrekt abgelaufen ist (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt
				 * werden
				 */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + args[0]
						+ " " + args[1] + " " + args[2] + " " + args[3];
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent(
						"<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( val ).start();
	}

	/* Wenn choseFile betaetigt wird, kann eine Datei ausgewaehlt werden */
	@FXML
	void chooseFile( ActionEvent e )
	{
		console.setText( " \n" );
		FileChooser fileChooser = new FileChooser();
		if ( !fileFolder.getText().isEmpty() ) {
			File dirFileFolder = new File( fileFolder.getText() );
			if ( dirFileFolder.isFile() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			fileChooser.setInitialDirectory( dirFileFolder );
		} else if ( !Util.stringInFileLine(
				"<standardinputdir></standardinputdir>", configFile ) ) {
			try {
				Document doc = null;
				BufferedInputStream bis;
				bis = new BufferedInputStream(
						new FileInputStream( configFile ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();
				String standardinputdir = doc
						.getElementsByTagName( "standardinputdir" ).item( 0 )
						.getTextContent();
				fileChooser.setInitialDirectory( new File( standardinputdir ) );
			} catch ( ParserConfigurationException | SAXException
					| IOException e1 ) {
				e1.printStackTrace();
			}
		}
		if ( locale.toString().startsWith( "fr" ) ) {
			fileChooser.setTitle( "Choisissez le fichier" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			fileChooser.setTitle( "Choose the file" );
		} else {
			fileChooser.setTitle( "W�hlen Sie die Datei" );
		}
		File valFile = fileChooser.showOpenDialog( new Stage() );
		if ( valFile != null ) {
			fileFolder.clear();
			fileFolder.setText( valFile.getAbsolutePath() );
			// Anzeige in WebView wenn image
			String pathDetail = "file:/" + valFile.getAbsolutePath();
			pathDetail = pathDetail.replace( "\\\\", "/" );
			pathDetail = pathDetail.replace( "\\", "/" );

			String fileFolderName = valFile.getName();
			String fileFolderExt = "." + FilenameUtils
					.getExtension( fileFolderName ).toLowerCase();
			String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Ausgew�hlte Datei: <br>"
					+ valFile.getAbsolutePath() + "</h3></td></tr>";
			String sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ggf. Konfiguration und LogType anpassen </h3></td></tr>";
			String sel3 = "<tr><td><h3>3.</h3></td><td><h3>Validierung starten </h3></td></tr></table>";
			String selDetail = "<br/>";
			if ( fileFolderExt.equals( ".jpeg" )
					|| fileFolderExt.equals( ".jpg" )
					|| fileFolderExt.equals( ".png" )
					|| fileFolderExt.equals( ".svg" ) ) {
				selDetail = "<table  width=\"100%\"><tr><td width=\"25%\"></td><td><img  src='"
						+ pathDetail
						+ "' width=\"300\" style=\"border:1px solid gray\" ></td><td width=\"25%\"></td></tr></table>";
			} else {
				// TODO: hier laufend weitere Viewer einbauen
			}
			if ( locale.toString().startsWith( "fr" ) ) {
				sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Fichier s�lectionn� : <br>"
						+ valFile.getAbsolutePath() + "</h3></td></tr>";
				sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ajuster la configuration et le LogType si n�cessaire </h3></td></tr>";
				sel3 = "<tr><td><h3>3.</h3></td><td><h3>D�marrer la validation </h3></td></tr></table>";
			} else if ( locale.toString().startsWith( "en" ) ) {
				sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Selected file: <br>"
						+ valFile.getAbsolutePath() + "</h3></td></tr>";
				sel2 = "<tr><td><h3>2.</h3></td><td><h3>Adjust configuration and LogType if necessary </h3></td></tr>";
				sel3 = "<tr><td><h3>3.</h3></td><td><h3>Start validation </h3></td></tr></table>";
			}
			String text = "<html><body>" + sel1 + sel2 + sel3 + selDetail
					+ "</body></html>";
			engine.loadContent( text );
			// Format und Sip Validierung erst moeglich wenn fileFolder
			// ausgefuellt
			// und auch in der Config erlaubt
			if ( Util.stringInFileLine( "<pdfavalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<siardalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jp2validation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jpegvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<pngvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<tiffvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else {
				buttonFormat.setDisable( true );
			}
			if ( valFile.getName().startsWith( "SIP" ) ) {
				String configSip0160 = "<ech0160validation>no</ech0160validation>";
				if ( Util.stringInFileLine( configSip0160, configFile ) ) {
					buttonSip.setDisable( true );
					buttonOnlySip.setDisable( true );
				} else {
					buttonSip.setDisable( false );
					buttonOnlySip.setDisable( false );
				}
			} else {
				buttonSip.setDisable( true );
				buttonOnlySip.setDisable( true );
			}
		}
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	/* Wenn choseFoder betaetigt wird, kann ein Ordner ausgewaehlt werden */
	@FXML
	void chooseFolder( ActionEvent e )
	{
		console.setText( " \n" );
		DirectoryChooser folderChooser = new DirectoryChooser();
		if ( !fileFolder.getText().isEmpty() ) {
			File dirFileFolder = new File( fileFolder.getText() );
			if ( dirFileFolder.isFile() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			folderChooser.setInitialDirectory( dirFileFolder );
		} else if ( !Util.stringInFileLine(
				"<standardinputdir></standardinputdir>", configFile ) ) {
			try {
				Document doc = null;
				BufferedInputStream bis;
				bis = new BufferedInputStream(
						new FileInputStream( configFile ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();
				String standardinputdir = doc
						.getElementsByTagName( "standardinputdir" ).item( 0 )
						.getTextContent();
				folderChooser
						.setInitialDirectory( new File( standardinputdir ) );
			} catch ( ParserConfigurationException | SAXException
					| IOException e1 ) {
				e1.printStackTrace();
			}
		}
		if ( locale.toString().startsWith( "fr" ) ) {
			folderChooser.setTitle( "Choisissez le dossier" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			folderChooser.setTitle( "Choose the folder" );
		} else {
			folderChooser.setTitle( "W�hlen Sie den Ordner" );
		}
		File valFolder = folderChooser.showDialog( new Stage() );
		if ( valFolder != null ) {
			fileFolder.clear();
			fileFolder.setText( valFolder.getAbsolutePath() );
			// Format und Sip Validierung erst moeglich wenn fileFolder
			// ausgefuellt
			// und auch in der Config erlaubt
			if ( Util.stringInFileLine( "<pdfavalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<siardalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jp2validation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jpegvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<pngvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<tiffvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else {
				buttonFormat.setDisable( true );
			}
			if ( valFolder.getName().startsWith( "SIP" ) ) {
				String configSip0160 = "<ech0160validation>no</ech0160validation>";
				if ( Util.stringInFileLine( configSip0160, configFile ) ) {
					buttonSip.setDisable( true );
					buttonOnlySip.setDisable( true );
				} else {
					buttonSip.setDisable( false );
					buttonOnlySip.setDisable( false );
				}
			} else {
				buttonSip.setDisable( true );
				buttonOnlySip.setDisable( true );
			}
			Map<String, File> fileMap = Util.getFileMapFile( valFolder );
			int numberFile = fileMap.size();
			String numberInFileMap = String.format( "%,d", numberFile );

			String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Ausgew�hlter Ordner: <br>"
					+ valFolder.getAbsolutePath() + "<br>(" + numberInFileMap
					+ " Dateien)</h3></td></tr>";
			String sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ggf. Konfiguration und LogType anpassen </h3></td></tr>";
			String sel3 = "<tr><td><h3>3.</h3></td><td><h3>Validierung starten </h3></td></tr></table>";
			if ( locale.toString().startsWith( "fr" ) ) {
				sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Dossier s�lectionn� : <br>"
						+ valFolder.getAbsolutePath() + "<br>("
						+ numberInFileMap + " fichier)</h3></td></tr>";
				sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ajuster la configuration et le LogType si n�cessaire </h3></td></tr>";
				sel3 = "<tr><td><h3>3.</h3></td><td><h3>D�marrer la validation </h3></td></tr></table>";
			} else if ( locale.toString().startsWith( "en" ) ) {
				sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Selected folder: <br>"
						+ valFolder.getAbsolutePath() + "<br>("
						+ numberInFileMap + " files)</h3></td></tr>";
				sel2 = "<tr><td><h3>2.</h3></td><td><h3>Adjust configuration and LogType if necessary </h3></td></tr>";
				sel3 = "<tr><td><h3>3.</h3></td><td><h3>Start validation </h3></td></tr></table>";
			}
			String text = "<html><body>" + sel1 + sel2 + sel3
					+ "</body></html>";
			engine.loadContent( text );
		}
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	/*
	 * Wenn changeConfig betaetigt wird, oeffnet sich das KonfigGui im neuen
	 * Fenster
	 */
	@FXML
	void changeConfig( ActionEvent e )
	{
		console.setText( " \n" );
		String text = "<html><h2>Die Konfiguration wird in einem neuen Fenster bearbeitet. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La configuration est �dit�e dans une nouvelle fen�tre. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>The configuration is edited in a new window. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );
		try {
			StackPane configLayout = new StackPane();

			configLayout = FXMLLoader
					.load( getClass().getResource( "ConfigView.fxml" ) );
			Scene configScene = new Scene( configLayout );
			configScene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage configStage = new Stage();

			configStage.setTitle( "KOST-Val   -   Configuration" );
			Image kostvalIcon = new Image( "file:" + dirOfJarPath
					+ File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			configStage.initModality( Modality.APPLICATION_MODAL );
			configStage.getIcons().add( kostvalIcon );
			configStage.setScene( configScene );
			configStage.setOnCloseRequest( event -> {
				File configFileBackup = new File( System.getenv( "USERPROFILE" )
						+ File.separator + ".kost-val_2x" + File.separator
						+ "configuration" + File.separator
						+ "BACKUP.kostval.conf.xml" );
				try {
					Util.copyFile( configFileBackup, configFile );
				} catch ( IOException e1 ) {
					e1.printStackTrace();
				}
				configFileBackup.delete();
				Util.deleteFile( configFileBackup );
			} );
			configStage.show();
			console.setText( " \n" );
			configStage.setOnHiding( event -> {
				// hier engeben was beim schliessen gemacht werden soll
				console.setText( " \n" );
				engine.load( "file:///" + configFile.getAbsolutePath() );
				buttonPrint.setDisable( true );
				buttonSave.setDisable( true );
			} );
			console.setText( " \n" );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/* TODO --> TextField ================= */

	@SuppressWarnings("unused")
	/* Wenn Aenderungen an changeFileFolder gemacht wird, wird es ausgeloest */
	@FXML
	void changeFileFolder( ActionEvent event )
	{
		console.setText( " \n" );
		String pathFileFolder = fileFolder.getText();
		File valFileFolder = new File( pathFileFolder );
		if ( valFileFolder.exists() ) { // Format und Sip Validierung erst
										// moeglich wenn fileFolder
										// ausgefuellt
			// und auch in der Config erlaubt
			if ( Util.stringInFileLine( "<pdfavalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<siardalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jp2validation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<jpegvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<pngvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else if ( Util.stringInFileLine( "<tiffvalidation>yes</",
					configFile ) ) {
				buttonFormat.setDisable( false );
			} else {
				buttonFormat.setDisable( true );
			}
			if ( valFileFolder.getName().startsWith( "SIP" ) ) {
				String configSip0160 = "<ech0160validation>no</ech0160validation>";
				if ( Util.stringInFileLine( configSip0160, configFile ) ) {
					buttonSip.setDisable( true );
					buttonOnlySip.setDisable( true );
				} else {
					buttonSip.setDisable( false );
					buttonOnlySip.setDisable( false );
				}
			} else {
				buttonSip.setDisable( true );
				buttonOnlySip.setDisable( true );
			}
		} else {
			// Format und Sip Validierung erst moeglich wenn valFileFolder
			// existiert
			buttonSip.setDisable( true );
			buttonOnlySip.setDisable( true );
			buttonFormat.setDisable( true );
		}
		// Anzeige in WebView wenn image
		if ( valFileFolder.isFile() ) {
			if ( valFileFolder.exists() ) {
				if ( valFileFolder != null ) {
					fileFolder.clear();
					fileFolder.setText( valFileFolder.getAbsolutePath() );
					// Anzeige in WebView wenn image
					String pathDetail = "file:/"
							+ valFileFolder.getAbsolutePath();
					pathDetail = pathDetail.replace( "\\\\", "/" );
					pathDetail = pathDetail.replace( "\\", "/" );

					String fileFolderName = valFileFolder.getName();
					String fileFolderExt = "." + FilenameUtils
							.getExtension( fileFolderName ).toLowerCase();
					String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Ausgew�hlte Datei: <br>"
							+ valFileFolder.getAbsolutePath()
							+ "</h3></td></tr>";
					String sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ggf. Konfiguration und LogType anpassen </h3></td></tr>";
					String sel3 = "<tr><td><h3>3.</h3></td><td><h3>Validierung starten </h3></td></tr></table>";
					String selDetail = "<br/>";
					if ( fileFolderExt.equals( ".jpeg" )
							|| fileFolderExt.equals( ".jpg" )
							|| fileFolderExt.equals( ".png" )
							|| fileFolderExt.equals( ".svg" ) ) {
						selDetail = "<table  width=\"100%\"><tr><td width=\"25%\"></td><td><img  src='"
								+ pathDetail
								+ "' width=\"300\" style=\"border:1px solid gray\" ></td><td width=\"25%\"></td></tr></table>";
					} else {
						// TODO: hier laufend weitere Viewer einbauen
					}
					if ( locale.toString().startsWith( "fr" ) ) {
						sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Fichier s�lectionn� : <br>"
								+ valFileFolder.getAbsolutePath()
								+ "</h3></td></tr>";
						sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ajuster la configuration et le LogType si n�cessaire </h3></td></tr>";
						sel3 = "<tr><td><h3>3.</h3></td><td><h3>D�marrer la validation </h3></td></tr></table>";
					} else if ( locale.toString().startsWith( "en" ) ) {
						sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Selected file: <br>"
								+ valFileFolder.getAbsolutePath()
								+ "</h3></td></tr>";
						sel2 = "<tr><td><h3>2.</h3></td><td><h3>Adjust configuration and LogType if necessary </h3></td></tr>";
						sel3 = "<tr><td><h3>3.</h3></td><td><h3>Start validation </h3></td></tr></table>";
					} else {
						sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br>&nbsp;</h3></td><td><h3>Ausgew�hlte Datei: <br>"
								+ valFileFolder.getAbsolutePath()
								+ "</h3></td></tr>";
						sel2 = "<tr><td><h3>2.</h3></td><td><h3>Ggf. Konfiguration und LogType anpassen </h3></td></tr>";
						sel3 = "<tr><td><h3>3.</h3></td><td><h3>Validierung starten </h3></td></tr></table>";
					}
					String text = "<html><body>" + sel1 + sel2 + sel3
							+ selDetail + "</body></html>";
					engine.loadContent( text );
					// Format und Sip Validierung erst moeglich wenn fileFolder
					// ausgefuellt
					buttonFormat.setDisable( false );
				} else {
					String notexist = "Ung�ltiger Pfad! "
							+ valFileFolder.getAbsolutePath()
							+ " existiert nicht.";
					if ( locale.toString().startsWith( "fr" ) ) {
						notexist = "Lien invalide ! "
								+ valFileFolder.getAbsolutePath()
								+ " n'existe pas.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						notexist = "Illegal path! "
								+ valFileFolder.getAbsolutePath()
								+ " doesn't exist.";
					}
					engine.loadContent(
							"<html><h2>" + notexist + "</h2></html>" );
				}
			}
		} else {
			if ( valFileFolder.exists() ) {
				Map<String, File> fileMap = Util
						.getFileMapFile( valFileFolder );
				int numberFile = fileMap.size();
				String numberInFileMap = String.format( "%,d", numberFile );

				String sel1Folder = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Ausgew�hlter Ordner: <br>"
						+ valFileFolder.getAbsolutePath() + "<br>("
						+ numberInFileMap + " Dateien)</h3></td></tr>";
				String sel2Folder = "<tr><td><h3>2.</h3></td><td><h3>Ggf. Konfiguration und LogType anpassen </h3></td></tr>";
				String sel3Folder = "<tr><td><h3>3.</h3></td><td><h3>Validierung starten </h3></td></tr></table>";
				if ( locale.toString().startsWith( "fr" ) ) {
					sel1Folder = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Dossier s�lectionn� : <br>"
							+ valFileFolder.getAbsolutePath() + "<br>("
							+ numberInFileMap + " fichier)</h3></td></tr>";
					sel2Folder = "<tr><td><h3>2.</h3></td><td><h3>Ajuster la configuration et le LogType si n�cessaire </h3></td></tr>";
					sel3Folder = "<tr><td><h3>3.</h3></td><td><h3>D�marrer la validation </h3></td></tr></table>";
				} else if ( locale.toString().startsWith( "en" ) ) {
					sel1Folder = "<table  width=\"100%\"><tr><td width=\"30px\"><h3>1.<br><br>&nbsp;</h3></td><td><h3>Selected folder: <br>"
							+ valFileFolder.getAbsolutePath() + "<br>("
							+ numberInFileMap + " files)</h3></td></tr>";
					sel2Folder = "<tr><td><h3>2.</h3></td><td><h3>Adjust configuration and LogType if necessary </h3></td></tr>";
					sel3Folder = "<tr><td><h3>3.</h3></td><td><h3>Start validation </h3></td></tr></table>";
				}
				String text = "<html><body>" + sel1Folder + sel2Folder
						+ sel3Folder + "</body></html>";
				engine.loadContent( text );
			} else {
				String notexist = "Ung�ltiger Pfad! "
						+ valFileFolder.getAbsolutePath() + " existiert nicht.";
				if ( locale.toString().startsWith( "fr" ) ) {
					notexist = "Lien invalide ! "
							+ valFileFolder.getAbsolutePath()
							+ " n'existe pas.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					notexist = "Illegal path! "
							+ valFileFolder.getAbsolutePath()
							+ " doesn't exist.";
				}
				engine.loadContent( "<html><h2>" + notexist + "</h2></html>" );
			}
		}
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeLoType wird die Log umgestellt
	@FXML
	void changeLogType( ActionEvent event )
	{
		console.setText( " \n" );
		String selLogType = logType.getValue();
		if ( selLogType.contains( "--min" ) ) {
			arg3 = "--min";
		} else if ( selLogType.contains( "--max" ) ) {
			arg3 = "--max";
		} else {
			arg3 = "--xml";
		}
	}

	// Mit changeLang wird die Sprache umgestellt
	@FXML
	void changeLang( ActionEvent event )
	{
		console.setText( " \n" );
		String selLang = lang.getValue();
		try {
			if ( selLang.equals( "Deutsch" ) ) {
				buttonFormat.setText( "nur Formate" );
				buttonSip.setText( "SIP inkl. Formate" );
				buttonOnlySip.setText( "nur SIP" );
				labelFileFolder.setText( "W�hle Datei / Ordner" );
				buttonFolder.setText( "Ordner" );
				buttonFile.setText( "Datei" );
				buttonHelp.setText( "Hilfe ?" );
				buttonLicence.setText( "Lizenzinformationen" );
				buttonChange.setText( "anpassen" );
				buttonShowConfig.setText( "anzeigen" );
				labelStart.setText( "Starte Validierung" );
				labelConfig.setText( "Konfiguration" );
				buttonSave.setText( "speichern" );
				buttonPrint.setText( "drucken" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-FR.xsl",
							"kostval-conf-DE.xsl", configFile );
					Util.oldnewstring( "kostval-conf-EN.xsl",
							"kostval-conf-DE.xsl", configFile );
				}
				locale = new Locale( "de" );
			} else if ( selLang.equals( "English" ) ) {
				buttonFormat.setText( "Format only" );
				buttonSip.setText( "SIP incl. Format" );
				buttonOnlySip.setText( "SIP only" );
				labelFileFolder.setText( "Select file / folder" );
				buttonFolder.setText( "folder" );
				buttonFile.setText( "file" );
				buttonHelp.setText( "Help ?" );
				buttonLicence.setText( "License information" );
				buttonChange.setText( "change" );
				buttonShowConfig.setText( "show" );
				labelStart.setText( "Start validation" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "save" );
				buttonPrint.setText( "print" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-DE.xsl",
							"kostval-conf-EN.xsl", configFile );
					Util.oldnewstring( "kostval-conf-FR.xsl",
							"kostval-conf-EN.xsl", configFile );
				}
				locale = new Locale( "en" );
			} else {
				buttonFormat.setText( "format uniquement" );
				buttonSip.setText( "SIP incl. formats" );
				buttonOnlySip.setText( "SIP uniquement" );
				labelFileFolder.setText( "S�lectionnez" );
				buttonFolder.setText( "dossier" );
				buttonFile.setText( "fichier" );
				buttonHelp.setText( "Aide ?" );
				buttonLicence.setText( "Informations sur la licence" );
				buttonChange.setText( "changer" );
				buttonShowConfig.setText( "afficher" );
				labelStart.setText( "Lancer la validation" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "sauvegarder" );
				buttonPrint.setText( "imprimer" );
				if ( configFile.exists() ) {
					Util.oldnewstring( "kostval-conf-DE.xsl",
							"kostval-conf-FR.xsl", configFile );
					Util.oldnewstring( "kostval-conf-EN.xsl",
							"kostval-conf-FR.xsl", configFile );
				}
				locale = new Locale( "fr" );
			}
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

}