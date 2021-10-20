/* == KOST-Ran ==================================================================================
 * The KOST-Ran application is used for excerpt a random sample with metadata from a SIARD-File.
 * Copyright (C) 2021 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * KOST-Ran is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This application is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or
 * see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostran;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.util.Util;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
// import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RanGuiController
{
	@FXML
	private Button						buttonSiard, buttonTable, buttonList, buttonHelp, buttonLicence,
			buttonSave, buttonPrint, buttonFinish;

	/* @FXML private Button buttonChoice1, buttonChoice2, buttonExcerpt; */

	ObservableList<String>		langList				= FXCollections.observableArrayList( "Deutsch",
			"Français", "English" );

	@FXML
	private ChoiceBox<String>	lang;

	@FXML
	private Label							labelSiard, labelTable, labelList, labelChoiceSiard, labelChoiceTable,
			labelChoiceList, label;

	@FXML
	private TextArea					console;

	private PrintStream				ps;

	@FXML
	private WebView						wbv;

	private WebEngine					engine;

	private File							fileSiard, outputFile, fileList, outputFileXsl,
			kostRanFolder = new File( System.getenv( "USERPROFILE" ) + File.separator + ".kost-ran" ),
			outputFolder = new File( kostRanFolder.getAbsolutePath() + File.separator + "Output" ),
			configFolder = new File( kostRanFolder.getAbsolutePath() + File.separator + "configuration" ),
			tempFile = new File( kostRanFolder.getAbsolutePath() + File.separator + "temp_KOST-Ran" ),
			metadataXml = new File( tempFile.getAbsolutePath() + File.separator + "metadata.xml" ),
			tableXml = new File( tempFile.getAbsolutePath() + File.separator + "table.xml" ),
			tableXmlRed = new File( tempFile.getAbsolutePath() + File.separator + "tableRed.xml" ),
			tableXmlRed1 = new File( tempFile.getAbsolutePath() + File.separator + "tableRed1.xml" ),
			fileXslEmpty = new File(
					configFolder.getAbsolutePath() + File.separator + "kost-ran_empty.xsl" );

	/* private File configFile = new File( kostRanFolder.getAbsolutePath() + File.separator +
	 * "configuration" + File.separator + "SIARDexcerpt.conf.xml" ); */

	private String						result, dirOfJarPath, initInstructionsDe, initInstructionsFr,
			initInstructionsEn, filter1, filter2, filterSize1, filterSize2;

	private List<Integer>			filter1Numbers	= new ArrayList<Integer>(),
			filter2Numbers = new ArrayList<Integer>();
	private Integer						filterSize1Int	= 100, filterSize2Int = 100, rowCounter = 0,
			noLayerint = 1;

	private String						versionKostRan	= "0.0.1";
	/* TODO: version auch hier anpassen:
	 * 
	 * 2) Start-Bild (make_exe)
	 * 
	 * 3) launch_KOST-Ran_exe.xml --> VersionInfo */

	private Locale						locale					= Locale.getDefault();

	@FXML
	private ScrollPane				scroll;

	@FXML
	void initialize()
	{
		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		labelChoiceSiard.setText( "" );
		labelChoiceTable.setText( "" );
		labelChoiceList.setText( "" );
		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		label
				.setText( "Copyright © KOST/CECO          KOST-Ran v" + versionKostRan + "          JavaFX "
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
			lang.setValue( "Français" );
			labelSiard.setText( "Fichier SIARD" );
			labelTable.setText( "Tableau principale" );
			labelList.setText( "Données de configuration" );
			buttonSiard.setText( "sélectionnez" );
			buttonTable.setText( "sélectionnez" );
			buttonList.setText( "sélectionnez" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
			buttonSave.setText( "sauvegarder" );
			buttonPrint.setText( "imprimer" );
			buttonFinish.setText( "Effectuer l'échantillonnage" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			locale = new Locale( "en" );
			lang.setValue( "English" );
			labelSiard.setText( "SIARD file" );
			labelTable.setText( "Main table" );
			labelList.setText( "Configuration data" );
			buttonSiard.setText( "Select" );
			buttonTable.setText( "select" );
			buttonList.setText( "select" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
			buttonSave.setText( "save" );
			buttonPrint.setText( "print" );
			buttonFinish.setText( "Perform sampling" );
		} else {
			locale = new Locale( "de" );
			lang.setValue( "Deutsch" );
			labelSiard.setText( "SIARD-Datei" );
			labelTable.setText( "Haupttabelle" );
			labelList.setText( "Konfigurationsdaten" );
			buttonSiard.setText( "Auswählen" );
			buttonTable.setText( "Auswählen" );
			buttonList.setText( "Auswählen" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenz-Informationen" );
			buttonSave.setText( "speichern" );
			buttonPrint.setText( "drucken" );
			buttonFinish.setText( "Sampling durchführen" );
		}
		if ( !kostRanFolder.exists() ) {
			kostRanFolder.mkdir();
		}
		if ( !outputFolder.exists() ) {
			outputFolder.mkdir();
		}
		if ( !configFolder.exists() ) {
			configFolder.mkdir();
		}
		if ( tempFile.exists() ) {
			Util.deleteDir( tempFile );
			tempFile.mkdirs();
		}
		try {
			if ( !fileXslEmpty.exists() ) {
				String xslEmptyJar = dirOfJarPath + File.separator + "configuration" + File.separator
						+ "kost-ran_empty.xsl";
				File fileXslEmptyJar = new File( xslEmptyJar );
				if ( fileXslEmptyJar.exists() ) {
					Util.copyFile( fileXslEmptyJar, fileXslEmpty );
				} else {
					System.out.println( "Missing " + xslEmptyJar );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		engine = wbv.getEngine();

		// Inaktiv bei Initialisierung
		buttonTable.setDisable( true );
		buttonList.setDisable( true );
		buttonFinish.setDisable( true );
		// Speichern und drucken des Log erst bei anzeige des log moeglich
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );
		lang.setDisable( false );

		lang.getItems().addAll( langList );

		/* Kurzanleitung zum GUI anzeigen */
		String help1, help2, help3, help4, help5, help6;
		help1 = " <h2>Brèves instructions</h2> ";
		help2 = "<hr>";
		help3 = "<h4>1) Sélectionnez le fichier SIARD</h4>";
		help4 = "<h4>2) Sélectionnez le tableau principal</h4>";
		help5 = "<h4>3) Sélectionnez la liste des entrées définies et des matadonnées à extraire</h4>";
		help6 = "<h4>4) Effectuer l'échantillonnage</h4>";
		initInstructionsFr = "<html>" + help1 + help2 + help3 + help4 + help5 + help6 + "<br/></html>";
		help1 = "<h2>Brief instructions</h2>";
		help3 = "<h4>1) Select SIARD file</h4>";
		help4 = "<h4>2) Select main table</h4>";
		help5 = "<h4>3) Select list of defined entries and matadata to extract</h4>";
		help6 = "<h4>4) Perform sampling</h4>";
		initInstructionsEn = "<html>" + help1 + help2 + help3 + help4 + help5 + help6 + "<br/></html>";
		help1 = "<h2>Kurzanleitung</h2>";
		help3 = "<h4>1) SIARD-Datei auswählen</h4>";
		help4 = "<h4>2) Haupttabelle auswählen</h4>";
		help5 = "<h4>3) Liste der definierten zu extrahierenden Einträge und Matadaten auswählen</h4>";
		help6 = "<h4>4) Sampling durchführen</h4>";
		initInstructionsDe = "<html>" + help1 + help2 + help3 + help4 + help5 + help6 + "<br/></html>";
		String initInstructions = initInstructionsDe;
		if ( locale.toString().startsWith( "fr" ) ) {
			initInstructions = initInstructionsFr;
		} else if ( locale.toString().startsWith( "en" ) ) {
			initInstructions = initInstructionsEn;
		} else {
			initInstructions = initInstructionsDe;
		}
		engine.loadContent( initInstructions );

		// context.close();

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
			String initInstructions = initInstructionsDe;
			if ( locale.toString().startsWith( "fr" ) ) {
				initInstructions = initInstructionsFr;
			} else if ( locale.toString().startsWith( "en" ) ) {
				initInstructions = initInstructionsEn;
			} else {
				initInstructions = initInstructionsDe;
			}
			engine.loadContent( initInstructions );

			// Handbuch in externem Viewer oeffnen dann kann parallel gearbeitet werden
			String docPath = dirOfJarPath + File.separator + "doc" + File.separator
					+ "Anwendungshandbuch.pdf";
			File dirDoc = new File( "doc" );
			File[] docArray = dirDoc.listFiles();
			if ( docArray != null ) { // Erforderliche Berechtigungen etc. sind vorhanden
				for ( int i = 0; i < docArray.length; i++ ) {
					if ( docArray[i].isDirectory() ) {
						// System.out.print( " (Ordner)\n" );
					} else {
						// System.out.print( " (Datei)\n" );
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
		// String licence5;
		String licence6;
		if ( locale.toString().startsWith( "fr" ) ) {
			licence1 = "<h2>Ce programme ne comporte ABSOLUMENT AUCUNE GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Il s'agit d'un logiciel libre, et vous êtes invités à le redistribuer sous certaines conditions;</h4>";
			licence4 = "- voir GPL-3.0_COPYING.txt pour plus de détails";
			// licence4 = "- voir le manuel et GPL-3.0_COPYING.txt pour plus de détails";
			// licence5 = "- KOST-Ran utilise des composants non modifiés d'autres fabricants en les
			// incorporant directement dans le code source.";
			licence6 = "- Les utilisateurs de KOST-Ran sont priés de respecter les conditions de licence de ces composants.";
		} else if ( locale.toString().startsWith( "en" ) ) {
			licence1 = "<h2>This program comes with ABSOLUTELY NO WARRANTY.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>This is free software, and you are welcome to redistribute it under certain conditions;</h4>";
			licence4 = "- see GPL-3.0_COPYING.txt for details. ";
			// licence4 = "- see the manual and GPL-3.0_COPYING.txt for details. ";
			// licence5 = "- KOST-Ran uses unmodified components of other manufacturers by embedding them
			// directly into the source code.";
			licence6 = "- Users of KOST-Ran are requested to adhere to these components ‘terms of licence.";
		} else {
			licence1 = "<h2>Dieses Programm kommt mit ABSOLUT KEINER GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Es handelt sich um freie Software, und Sie dürfen sie unter bestimmten Bedingungen gerne weitergeben;</h4>";
			licence4 = "- siehe GPL-3.0_COPYING.txt für Einzelheiten. ";
			// licence4 = "- siehe das Handbuch und GPL-3.0_COPYING.txt für Einzelheiten. ";
			// licence5 = "- KOST-Ran verwendet unmodifizierte Komponenten anderer Hersteller, indem diese
			// direkt in den Quellcode eingebettet werden.";
			licence6 = "- Benutzer von KOST-Ran werden gebeten, sich an die Lizenzbedingungen dieser Komponenten zu halten.";
		}
		String text = "<html>" + licence1 + licence2 + licence3 + licence4 + "<br/>"
				+ /* licence5 + "<br/>" + */ licence6 + "</html>";
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
		String strHeaderText = "Wählen Sie einen Drucker aus den verfügbaren Druckern";
		String strTitle = "Druckerauswahl";
		String strNoPrinter = "Kein Drucker. Es ist kein Drucker auf Ihrem System installiert.";
		if ( locale.toString().startsWith( "fr" ) ) {
			strHeaderText = "Choisissez une imprimante parmi les imprimantes disponibles";
			strTitle = "Choix de l'imprimante";
			strNoPrinter = "Pas d'imprimante. Aucune imprimante n'est installée sur votre système";
		} else if ( locale.toString().startsWith( "en" ) ) {
			strHeaderText = "Choose a printer from available printers";
			strTitle = "Printer Choice";
			strNoPrinter = "No printer. There is no printer installed on your system.";
		}
		if ( printerToUse != null ) {

			ChoiceDialog<Printer> dialog = new ChoiceDialog<Printer>( defaultprinter,
					Printer.getAllPrinters() );
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
				pageLayout = printerToUse.createPageLayout( Paper.A4, PageOrientation.PORTRAIT,
						Printer.MarginType.DEFAULT );
				jobSettings.setPageLayout( pageLayout );
				job.getJobSettings();
				/* showPrintDialog nicht verwenden, da ansonsten nicht zuverlaessig abgebrochen werden kann.
				 * Besser ist es über einen ChoiceDialog den Drucker auszuwaehlen und wenn einer ausgewaehlt
				 * wurde zu drucken! */
				if ( job != null ) {
					engine.print( job );
					job.endJob();
				}
			} else {
				// System.out.println("Kein Drucker ausgewaehlt. [Abbrechen] gedrueckt");
			}
		} else {
			System.out.println( strNoPrinter );
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

	/* TODO SIARD-Marker
	 * 
	 * Wenn choseSiard betaetigt wird, kann eine SIARD-Datei ausgewaehlt werden */
	@FXML
	void chooseSiard( ActionEvent e )
	{
		console.setText( " \n" );
		if ( tempFile.exists() ) {
			Util.deleteDir( tempFile );
			tempFile.mkdirs();
		}
		// bestehende SIARD-Datei auswaehlen
		FileChooser fileChooser = new FileChooser();
		if ( !labelChoiceSiard.getText().isEmpty() ) {
			// setInitialDirectory mit vorgaeniger Auswahl (Ordner)
			File dirFileFolder = new File( labelChoiceSiard.getText() );
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
		}
		labelChoiceSiard.setText( "" );
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
		fileSiard = fileChooser.showOpenDialog( new Stage() );

		final Task<Boolean> ranSiard = doRanSiard( fileSiard, locale );
		ranSiard.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			// Initialisierung erfolgreich
			@Override
			public void handle( WorkerStateEvent t )
			{
				// kein Handler Problem

				// <Label "mainFolderName" ausgeben
				Boolean initMain = true;
				if ( initMain ) {
					// Zusätzliche Kontrolle
					File tempSiardFolder = new File(
							tempFile.getAbsolutePath() + File.separator + fileSiard.getName() );
					if ( !tempSiardFolder.exists() || !metadataXml.exists() ) {
						initMain = false;
					}
				}

				if ( initMain ) {
					labelChoiceSiard.setText( fileSiard.getAbsolutePath() );
					buttonSiard.setDisable( true );
					buttonTable.setDisable( false );
					buttonList.setDisable( true );
					buttonHelp.setDisable( false );
					buttonLicence.setDisable( false );
					buttonSave.setDisable( true );
					buttonPrint.setDisable( true );
					lang.setDisable( false );

					// kein Output bei der Initialisierung welcher angezeigt werden kann
					String text = "SIARD-Datei konnte extrahiert werden. <br/><br/>Jetzt kann die Haupttabelle ausgewählt werden.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Le fichier SIARD a pu être extrait. <br/><br/>On peut maintenant sélectionner le tableau principal.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "SIARD file could be extracted. <br/><br/>Now the main table can be selected.";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				} else {
					// Problem: keine SIARD-Daei extrahiert
					labelChoiceSiard.setText( "" );
					buttonSiard.setDisable( false );
					buttonTable.setDisable( true );
					buttonList.setDisable( true );
					buttonHelp.setDisable( false );
					buttonLicence.setDisable( false );
					buttonSave.setDisable( true );
					buttonPrint.setDisable( true );
					lang.setDisable( false );

					String text = "SIARD-Datei konnte NICHT extrahiert werden. <br/><br/>Validieren sie die SIARD-Datei mit KOST-Val.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Le fichier SIARD n'a pas pu être extrait. <br/><br/>Validez le fichier SIARD avec KOST-Val.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "SIARD file could NOT be extracted. <br/><br/>Validate the SIARD file with KOST-Val.";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );

					if ( tempFile.exists() ) {
						Util.deleteDir( tempFile );
						tempFile.mkdirs();
					}
				}
			}
		} );
		ranSiard.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				/* Dieser handler wird ausgefuehrt wenn die Initialisierung nicht korrekt abgelaufen ist
				 * (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + fileSiard.getAbsolutePath();
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );

				if ( tempFile.exists() ) {
					Util.deleteDir( tempFile );
					tempFile.mkdirs();
				}
			}
		} );
		new Thread( ranSiard ).start();
	}

	public Task<Boolean> doRanSiard( File fileSiard, Locale locale )
	{
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				String toplevelDir = fileSiard.getName();
				int lastDotIdx = toplevelDir.lastIndexOf( "." );
				toplevelDir = toplevelDir.substring( 0, lastDotIdx );
				if ( tempFile.exists() ) {
					Util.deleteDir( tempFile );
					tempFile.mkdirs();
				}
				File fileSiardNew = new File(
						tempFile.getAbsolutePath() + File.separator + fileSiard.getName() );

				try {
					// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
					if ( fileSiardNew.exists() ) {
						Util.deleteDir( fileSiardNew );
					}
					if ( !fileSiardNew.exists() ) {
						fileSiardNew.mkdirs();
					}
					int BUFFER = 2048;
					ZipFile zipfile = new ZipFile( fileSiard.getAbsolutePath() );
					Enumeration<? extends ZipEntry> entries = zipfile.entries();
					// jeden entry durchgechen
					while ( entries.hasMoreElements() ) {
						ZipEntry entry = (ZipEntry) entries.nextElement();
						String entryName = entry.getName();
						File destFile = new File( fileSiardNew, entryName );
						// erstelle den Ueberordner
						File destinationParent = destFile.getParentFile();
						destinationParent.mkdirs();
						if ( !entry.isDirectory() ) {
							InputStream stream = zipfile.getInputStream( entry );
							BufferedInputStream is = new BufferedInputStream( stream );
							int currentByte;
							// erstellung Buffer zum schreiben der Dateien
							byte data[] = new byte[BUFFER];
							// schreibe die aktuelle Datei an den gewuenschten Ort
							FileOutputStream fos = new FileOutputStream( destFile );
							BufferedOutputStream dest = new BufferedOutputStream( fos, BUFFER );
							while ( (currentByte = is.read( data, 0, BUFFER )) != -1 ) {
								dest.write( data, 0, currentByte );
							}
							dest.flush();
							dest.close();
							is.close();
							stream.close();
						} else {
							destFile.mkdirs();
						}
					}
					zipfile.close();
					/** Struktur-Check SIARD-Verzeichnis */
					File header = new File( fileSiardNew.getAbsolutePath() + File.separator + "header" );
					File xsd = new File( fileSiardNew.getAbsolutePath() + File.separator + "header"
							+ File.separator + "metadata.xsd" );
					File metadata = new File( fileSiardNew.getAbsolutePath() + File.separator + "header"
							+ File.separator + "metadata.xml" );

					String text = "Extrahierte SIARD-Datei ist NICHT korrekt aufgebaut. Validieren sie die SIARD-Datei mit KOST-Val.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Le fichier SIARD extrait n`est PAS structure correctement. Validez le fichier SIARD avec KOST-Val.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "Extracted SIARD file is NOT built correctly. Validate the SIARD file with KOST-Val.";
					}

					if ( !header.exists() || !xsd.exists() || !metadata.exists() ) {
						System.out.println( text );
						// Loeschen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
						if ( fileSiardNew.exists() ) {
							Util.deleteDir( fileSiardNew );
						}
						// Fehler Extraktion --> invalide
						return false;
					} else {
						// Struktur sieht plausibel aus
						Util.copyFile( metadata, metadataXml );
						return true;
					}

				} catch ( Exception e ) {
					System.out.println( "catch: " + e );
					e.printStackTrace();
					return false;
				}
			}
		};
	}

	/* TODO Table-Marker
	 * 
	 * Wenn choseTable betaetigt wird, kann eine Tabelle derSIARD-Datei ausgewaehlt werden */
	@FXML
	void chooseTable( ActionEvent e )
	{
		console.setText( " \n" );
		if ( tableXmlRed.exists() ) {
			Util.deleteFile( tableXmlRed );
		}
		if ( tableXml.exists() ) {
			Util.deleteFile( tableXml );
		}

		// bestehende Tabelle auswaehlen

		List<String> list = new ArrayList<String>();
		try {
			ZipFile zipFile = new ZipFile( fileSiard.getAbsolutePath() );
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() ) {
				String result = "";
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if ( name.contains( "metadata.xml" ) ) {
					InputStream in = zipFile.getInputStream( entry );
					BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
					String line;
					boolean table = false;
					while ( (line = reader.readLine()) != null ) {
						// <table>
						// <name>Journal</name>
						if ( table ) {
							result = line.substring( line.indexOf( "<name>" ) + "</name>".length() - 1,
									line.length() );
							// zuvieles loeschen
							result = result.substring( 0, result.indexOf( "</name>" ) );
							list.add( result );
						}
						if ( line.contains( "<table>" ) ) {
							table = true;
						} else {
							table = false;
						}
					}
				}
			}
			zipFile.close();
		} catch ( IOException et ) {
			et.printStackTrace();
		}

		// Name der Haupttabelle
		String[] tablenames = list.toArray( new String[0] );

		// create a choice dialog mit allen moeglichen Tabellennamen
		ChoiceDialog<String> dialogC = new ChoiceDialog<String>( tablenames[0], tablenames );

		// Set title & header text
		if ( locale.toString().startsWith( "fr" ) ) {
			dialogC.setTitle( "Nom de la table principale" );
			dialogC.setHeaderText( "Quel est le nom de la table principale?" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			dialogC.setTitle( "Name of the main table" );
			dialogC.setHeaderText( "What is the name of the main table?" );
		} else {
			dialogC.setTitle( "Name der Haupttabelle" );
			dialogC.setHeaderText( "Wie lautet der Name der Haupttabelle?" );
		}

		// Show the dialog and capture the result.
		Optional<String> resultC = dialogC.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String in the get() method
		if ( resultC.isPresent() ) {
			result = resultC.get();
			String mainnameProv = null, mainfolderProv = null, mainfolder = null, mainschemafolder = null;
			String cellNoXml = "";
			try {
				Boolean boolMainname = false;
				DocumentBuilderFactory dbfTable = DocumentBuilderFactory.newInstance();
				DocumentBuilder dbTable = dbfTable.newDocumentBuilder();
				Document docTable;
				docTable = dbTable.parse( new FileInputStream( metadataXml ), "UTF-8" );
				docTable.getDocumentElement().normalize();
				dbfTable.setFeature( "http://xml.org/sax/features/namespaces", false );
				NodeList nlTables = docTable.getElementsByTagName( "tables" );
				for ( int x0 = 0; x0 < nlTables.getLength(); x0++ ) {
					Node nodeTablesDetail = nlTables.item( x0 );
					NodeList nlTable = nodeTablesDetail.getChildNodes();
					for ( int x = 0; x < nlTable.getLength(); x++ ) {
						if ( !boolMainname ) {
							Node nodeTable = nlTable.item( x );
							NodeList nlChildTable = nodeTable.getChildNodes();
							for ( int y = 0; y < nlChildTable.getLength(); y++ ) {
								Node nodeDetail = nlChildTable.item( y );
								if ( nodeDetail.getNodeName().equals( "name" ) ) {
									mainnameProv = nodeDetail.getTextContent();
									// System.out.println( "mainnameProv " + mainnameProv );
									if ( mainnameProv.equals( result ) ) {
										boolMainname = true;
										x0 = nlTables.getLength();

										// mainfolder herauslesen
										Node mainTables = nodeDetail.getParentNode();
										NodeList nlTablesChild = mainTables.getChildNodes();
										for ( int x2 = 0; x2 < nlTablesChild.getLength(); x2++ ) {
											// fuer jedes Subelement der Tabelle (name, folder, description...) ...
											Node subNode = nlTablesChild.item( x2 );
											if ( subNode.getNodeName().equals( "folder" ) ) {
												mainfolder = subNode.getTextContent();
											} else if ( subNode.getNodeName().equals( "columns" ) ) {
												/* <cX> nummer ermitteln sowie name und description auslesen
												 * 
												 * Dies wird für die Legende verwendet
												 * 
												 * <legend><column><name>AddressId</name><description>ID der
												 * Adresse</description><cellno>c1</cellno></column><column><name>CountryId<
												 * /name><description>Land</description><cellno>c14</cellno></column></
												 * legend> */
												int cellNoInt = 1;
												NodeList nlColumn = subNode.getChildNodes();
												for ( int xC = 1; xC < nlColumn.getLength(); xC++ ) {
													Node nodeColumn = nlColumn.item( xC );
													NodeList nlColumnDetail = nodeColumn.getChildNodes();
													if ( nlColumnDetail.getLength() > 0 ) {
														String cellNo = "c" + cellNoInt;
														cellNoXml = cellNoXml + System.lineSeparator() + "<column><cellno>"
																+ cellNo + "</cellno>";
														cellNoInt++;
														for ( int xCD = 0; xCD < nlColumnDetail.getLength(); xCD++ ) {
															Node nodeColumnDetail = nlColumnDetail.item( xCD );
															String rawString = nodeColumnDetail.getTextContent();
															ByteBuffer buffer = StandardCharsets.UTF_8.encode( rawString );
															String utf8EncodedString = StandardCharsets.UTF_8.decode( buffer )
																	.toString();
															String newString = utf8EncodedString.replace( "\u00c0", "A" )
																	.replace( "\u00c1", "A" ).replace( "\u00c2", "A" )
																	.replace( "\u00c4", "Ae" ).replace( "\u00c6", "Ae" )
																	.replace( "\u00c8", "E" ).replace( "\u00c9", "E" )
																	.replace( "\u00ca", "E" ).replace( "\u00cb", "E" )
																	.replace( "\u00cc", "I" ).replace( "\u00cd", "I" )
																	.replace( "\u00ce", "I" ).replace( "\u00cf", "I" )
																	.replace( "\u00d2", "O" ).replace( "\u00d3", "O" )
																	.replace( "\u00d4", "O" ).replace( "\u00d6", "Oe" )
																	.replace( "\u00d9", "U" ).replace( "\u00da", "U" )
																	.replace( "\u00db", "U" ).replace( "\u00dc", "Ue" )
																	.replace( "\u00df", "ss" ).replace( "\u00e0", "a" )
																	.replace( "\u00e1", "a" ).replace( "\u00e2", "a" )
																	.replace( "\u00e4", "ae" ).replace( "\u00e6", "ae" )
																	.replace( "\u00e7", "c" ).replace( "\u00e8", "e" )
																	.replace( "\u00e9", "e" ).replace( "\u00ea", "e" )
																	.replace( "\u00eb", "e" ).replace( "\u00ec", "i" )
																	.replace( "\u00ed", "i" ).replace( "\u00ee", "i" )
																	.replace( "\u00ef", "i" ).replace( "\u00f2", "o" )
																	.replace( "\u00f3", "o" ).replace( "\u00f4", "o" )
																	.replace( "\u00f6", "oe" ).replace( "\u00f9", "u" )
																	.replace( "\u00fa", "u" ).replace( "\u00fb", "u" )
																	.replace( "\u00fc", "ue" );
															// https://www.utf8-zeichentabelle.de/unicode-utf8-table.pl?number=1024&htmlent=1
															if ( nodeColumnDetail.getNodeName().equals( "name" ) ) {
																cellNoXml = cellNoXml + "<name>" + newString + "</name>";
															}
															if ( nodeColumnDetail.getNodeName().equals( "description" ) ) {
																// String textNode = nodeColumnDetail.getTextContent();
																cellNoXml = cellNoXml + "<description>" + newString
																		+ "</description>";
															}
														}
														cellNoXml = cellNoXml + "</column>";
													}
												}
											}
										}
										// Schema name und folder herauslesen
										Node parentMainTables = mainTables.getParentNode().getParentNode();
										NodeList nlSchemaChild = parentMainTables.getChildNodes();
										for ( int x3 = 0; x3 < nlSchemaChild.getLength(); x3++ ) {
											// fuer jedes Subelement der Tabelle (name, folder, description...) ...
											Node subNode3 = nlSchemaChild.item( x3 );
											if ( subNode3.getNodeName().equals( "folder" ) ) {
												mainschemafolder = subNode3.getTextContent();
											}
										}

									}
								} else if ( nodeDetail.getNodeName().equals( "folder" ) ) {
									mainfolderProv = nodeDetail.getTextContent();
									// System.out.println( "mainfolderProv " + mainfolderProv );
								}
							}
							if ( boolMainname ) {
								/* haupttabelle gefunden */
								mainfolder = mainfolderProv;
							}
						} else {
							/* haupttabelle gefunden */
							mainfolder = mainfolderProv;

							x0 = nlTables.getLength();
							// beendet die For-schleife
						}
					}
				}
				String arg1Path = tempFile + File.separator + fileSiard.getName() + File.separator
						+ "content" + File.separator + mainschemafolder + File.separator + mainfolder
						+ File.separator + mainfolder + ".xml";
				Util.copyFile( new File( arg1Path ), tableXml );
				Util.oldnewstring( "</table>", "<legend>" + cellNoXml + "</legend></table>", tableXml );
			} catch ( SAXException | IOException | ParserConfigurationException e1 ) {
				// Auto-generated catch block
				e1.printStackTrace();
			}

			if ( tableXml.exists() ) {
				String text = "Haupttabelle konnte extrahiert werden. <br/><br/>Jetzt kann die Liste der definierten zu extrahierenden Einträge und Matadaten ausgewählen werden.";
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Le tableau principal a pu être extrait. <br/><br/>La liste des entrées définies et des matadonnées à extraire peut maintenant être sélectionnée.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "Main table could be extracted. <br/><br/>Now the list of defined entries and matadata to be extracted can be selected.";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + "</h2></html>" );

				labelChoiceTable.setText( result );
				buttonSiard.setDisable( true );
				buttonTable.setDisable( true );
				buttonList.setDisable( false );
				buttonHelp.setDisable( false );
				buttonLicence.setDisable( false );
				buttonSave.setDisable( true );
				buttonPrint.setDisable( true );
				lang.setDisable( false );
			} else {
				String textE = "Extrahierte SIARD-Datei ist NICHT korrekt aufgebaut. <br/><br/>Validieren sie die SIARD-Datei mit KOST-Val.";
				if ( locale.toString().startsWith( "fr" ) ) {
					textE = "Le fichier SIARD extrait n`est PAS structure correctement. <br/><br/>Validez le fichier SIARD avec KOST-Val.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					textE = "Extracted SIARD file is NOT built correctly. <br/><br/>Validate the SIARD file with KOST-Val.";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + textE + "</h2></html>" );

				labelChoiceSiard.setText( "" );
				buttonSiard.setDisable( true );
				buttonTable.setDisable( false );
				buttonList.setDisable( true );
				buttonHelp.setDisable( false );
				buttonLicence.setDisable( false );
				buttonSave.setDisable( true );
				buttonPrint.setDisable( true );
				lang.setDisable( false );
				if ( tempFile.exists() ) {
					Util.deleteDir( tempFile );
					tempFile.mkdirs();
				}
			}
		} else {
			result = "(..)";
			buttonSiard.setDisable( true );
			buttonTable.setDisable( false );
			buttonList.setDisable( true );
			buttonHelp.setDisable( false );
			buttonLicence.setDisable( false );
			buttonSave.setDisable( true );
			buttonPrint.setDisable( true );
			lang.setDisable( false );
		}
		console.setText( " \n" );
	}

	/* TODO List-Marker
	 * 
	 * Wenn chooseList betaetigt wird, kann eine liste ausgewaehlt werden */
	@FXML
	void chooseList( ActionEvent e )
	{
		if ( tableXmlRed.exists() ) {
			Util.deleteFile( tableXmlRed );
		}

		scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
		engine.loadContent( "<html><h2> ... </h2></html>" );
		console.setText( " \n" );
		// bestehende Liste (TXT-Datei) auswaehlen
		FileChooser fileChooser = new FileChooser();
		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "txt file (*.txt)",
				"*.txt" );
		fileChooser.getExtensionFilters().add( extFilter );
		// Set title
		if ( locale.toString().startsWith( "fr" ) ) {
			fileChooser.setTitle( "Sélectionnez le fichier TXT contenant les données pertinentes" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			fileChooser.setTitle( "Select the TXT file with the relevant data" );
		} else {
			fileChooser.setTitle( "Wählen Sie die TXT-Datei mit den relevanten Daten" );
		}
		fileList = fileChooser.showOpenDialog( new Stage() );
		labelChoiceList.setText( fileList.getName() );

		String text = "Die XML-Tabelle wird gemäss den Angaben der Liste bearbeitet. <br/><br/>Bitte warten...";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "Le tableau XML est traité selon les indications de la liste. <br/><br/>Veuillez attendre...";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "The XML table is edited according to the list data. <br/><br/>Please wait...";
		}
		scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
		engine.loadContent( "<html><h2>" + text + "</h2></html>" );

		outputFileXsl = new File( outputFolder.getAbsolutePath() + File.separator + "kost-ran_"
				+ fileSiard.getName() + "_" + fileList.getName() + ".xsl" );
		try {
			Util.copyFile( fileXslEmpty, outputFileXsl );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}

		buttonSiard.setDisable( true );
		buttonTable.setDisable( true );
		buttonList.setDisable( true );
		buttonHelp.setDisable( true );
		buttonLicence.setDisable( true );
		buttonSave.setDisable( true );
		buttonPrint.setDisable( true );
		lang.setDisable( true );

		final Task<Boolean> clearTable = doClearTable( fileSiard, locale );
		clearTable.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
			// Initialisierung erfolgreich
			@Override
			public void handle( WorkerStateEvent t )
			{
				// kein Handler Problem

				// <Label "mainFolderName" ausgeben
				Boolean initMain = true;
				if ( initMain ) {
					// Zusätzliche Kontrolle
					if ( !tableXmlRed.exists() ) {
						initMain = false;
					}
				}

				if ( initMain ) {
					// kein Output bei der Initialisierung welcher angezeigt werden kann
					String textC = "Die XML-Datei wurde gemaess den Angaben der Liste bearbeitet.  ";
					if ( locale.toString().startsWith( "fr" ) ) {
						textC = "Le fichier XML a ete edite selon les specifications de la liste.  ";
					} else if ( locale.toString().startsWith( "en" ) ) {
						textC = "The XML file was edited according to the list specifications.  ";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					System.out.println( textC );

					// ************** sel1 - sel6 **************
					final Task<Boolean> selTable = doSelTable( tableXmlRed, locale );
					selTable.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
						// Initialisierung erfolgreich
						@Override
						public void handle( WorkerStateEvent t )
						{
							// kein Handler Problem

							// <Label "mainFolderName" ausgeben
							Boolean initMain = true;
							if ( initMain ) {
								// Zusätzliche Kontrolle
								if ( !tableXmlRed.exists() ) {
									initMain = false;
								}
							}

							if ( initMain ) {
								buttonSiard.setDisable( true );
								buttonTable.setDisable( true );
								buttonList.setDisable( true );
								buttonHelp.setDisable( false );
								buttonLicence.setDisable( false );
								buttonSave.setDisable( true );
								buttonPrint.setDisable( true );
								buttonFinish.setDisable( false );
								lang.setDisable( false );

								String text = "Die XML-Datei wurde gemäss der Konfiguration ergänzt.<br/><br/>Jetzt kann das überflüssige mit [Sampling durchführen] gelöscht werden.";
								String textC = "Die XML-Datei wurde gemaess der Konfiguration ergaenzt.";
								if ( locale.toString().startsWith( "fr" ) ) {
									text = "Le fichier XML a été ajouté selon la configuration.<br/><br/>Maintenant, le superflu peut être supprimé avec [Effectuer l'échantillonnage].";
									textC = "Le fichier XML a ete ajoute selon la configuration.";
								} else if ( locale.toString().startsWith( "en" ) ) {
									text = "The XML file has been added according to the configuration.<br/><br/>Now the superfluous can be deleted with [Perform sampling].";
									textC = "The XML file has been added according to the configuration.";
								}
								scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
								engine.loadContent( "<html><h2>" + text + "</h2></html>" );
								System.out.println( "" );
								System.out.println( textC );
							} else {
								// Problem:
								buttonSiard.setDisable( false );
								buttonTable.setDisable( true );
								buttonList.setDisable( true );
								buttonHelp.setDisable( false );
								buttonLicence.setDisable( false );
								buttonSave.setDisable( true );
								buttonPrint.setDisable( true );
								lang.setDisable( false );

								String text = "XML-Datei konnte NICHT ergänzt werden.";
								String textC = "XML-Datei konnte NICHT ergänzt werden.";
								if ( locale.toString().startsWith( "fr" ) ) {
									text = "Le fichier XML n'a pas pu été complété. ";
									textC = "Le fichier XML n`a pas pu ete complété. ";
								} else if ( locale.toString().startsWith( "en" ) ) {
									text = "The XML file has NOT been completed.";
									textC = "The XML file has NOT been completed.";
								}
								scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
								engine.loadContent( "<html><h2>" + text + "</h2></html>" );
								System.out.println( "" );
								System.out.println( textC );
								System.out.println( "" );
								scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
							}
						}
					} );
					selTable.setOnFailed( new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle( WorkerStateEvent t )
						{
							/* Dieser handler wird ausgefuehrt wenn die Initialisierung nicht korrekt abgelaufen
							 * ist (Fehler).
							 * 
							 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
							String text = "Ein unbekannter Fehler ist aufgetreten ";
							String textArgs = "(WorkerStateEvent).<br/><br/>" + fileSiard.getAbsolutePath();
							if ( locale.toString().startsWith( "fr" ) ) {
								text = "Une erreur inconnue s`est produite ";
							} else if ( locale.toString().startsWith( "en" ) ) {
								text = "An unknown error has occurred ";
							}
							scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
							engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );
						}
					} );
					new Thread( selTable ).start();
					// ************** END sel1 - sel6 **************
				} else {
					// Problem:
					buttonSiard.setDisable( false );
					buttonTable.setDisable( true );
					buttonList.setDisable( true );
					buttonHelp.setDisable( false );
					buttonLicence.setDisable( false );
					buttonSave.setDisable( true );
					buttonPrint.setDisable( true );
					lang.setDisable( false );

					String text = "XML-Datei konnte NICHT bereinigt werden.";
					String textC = "XML-Datei konnte NICHT bereinigt werden.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Le fichier XML n'a pas pu été purifié. ";
						textC = "Le fichier XML n`a pas pu ete purifie. ";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "The XML file has NOT been cleared.";
						textC = "The XML file has NOT been cleared.";
					}
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
					System.out.println( "" );
					System.out.println( textC );
				}
			}
		} );
		clearTable.setOnFailed( new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle( WorkerStateEvent t )
			{
				/* Dieser handler wird ausgefuehrt wenn die Initialisierung nicht korrekt abgelaufen ist
				 * (Fehler).
				 * 
				 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
				String text = "Ein unbekannter Fehler ist aufgetreten ";
				String textArgs = "(WorkerStateEvent).<br/><br/>" + fileSiard.getAbsolutePath();
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur inconnue s`est produite ";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An unknown error has occurred ";
				}
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				engine.loadContent( "<html><h2>" + text + textArgs + "</h2></html>" );
			}
		} );
		new Thread( clearTable ).start();
	}

	public Task<Boolean> doClearTable( File fileSiard, Locale locale )
	{
		// TODO nur relevante Metadaten
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				try {
					FileReader fr = new FileReader( fileList );
					BufferedReader br = new BufferedReader( fr );

					String line;
					while ( (line = br.readLine()) != null ) {
						if ( line.contains( "filter1:" ) ) {
							filter1 = line.replace( "filter1:", "" );
						} else if ( line.contains( "filterSize1:" ) ) {
							filterSize1 = line.replace( "filterSize1:", "" );
							try {
								filterSize1Int = Integer.parseInt( filterSize1 );
							} catch ( NumberFormatException eInt ) {
								filterSize1Int = 100;
							}
						} else if ( line.contains( "filter2:" ) ) {
							filter2 = line.replace( "filter2:", "" );
						} else if ( line.contains( "filterSize2:" ) ) {
							filterSize2 = line.replace( "filterSize2:", "" );
							try {
								filterSize2Int = Integer.parseInt( filterSize2 );
							} catch ( NumberFormatException eInt ) {
								filterSize2Int = 100;
							}
						}
					}
					br.close();
					fr.close();

					DocumentBuilderFactory dbfTable = DocumentBuilderFactory.newInstance();
					DocumentBuilder dbTable = dbfTable.newDocumentBuilder();
					Document docTable;
					docTable = dbTable.parse( new FileInputStream( tableXml ), "UTF-8" );
					docTable.getDocumentElement().normalize();
					dbfTable.setFeature( "http://xml.org/sax/features/namespaces", false );
					NodeList nlRows = docTable.getElementsByTagName( "row" );
					int count = 0;
					Set<Node> targetNodeFont = new HashSet<Node>();
					// rowCounter = nlRows.getLength();
					for ( int x0 = 0; x0 < nlRows.getLength(); x0++ ) {
						Node nodeRowDetail = nlRows.item( x0 );
						NodeList nlCell = nodeRowDetail.getChildNodes();
						for ( int x = 0; x < nlCell.getLength(); x++ ) {
							Node nodeCell = nlCell.item( x );
							String metadataNode = "metadata:<" + nodeCell.getNodeName() + ">";
							if ( Util.stringInFile( metadataNode, fileList ) ) {
								// Node behalten
								// filter1
								String filterNode = "<" + nodeCell.getNodeName() + ">";

								if ( filterNode.equals( filter1 ) ) {
									Integer numberInt1 = 0;
									try {
										numberInt1 = Integer.parseInt( nodeCell.getTextContent() );
									} catch ( NumberFormatException eInt ) {
										numberInt1 = 0;
									}
									filter1Numbers.add( numberInt1 );
								}
								// filter2
								String filter2Node = "<" + nodeCell.getNodeName() + ">";
								if ( filter2Node.equals( filter2 ) ) {
									Integer numberInt2 = 0;
									try {
										numberInt2 = Integer.parseInt( nodeCell.getTextContent() );
									} catch ( NumberFormatException eInt ) {
										numberInt2 = 0;
									}
									filter2Numbers.add( numberInt2 );
								}
							} else {
								// Node zum leschen vormerken
								targetNodeFont.add( nodeCell );
							}
						}
						if ( count == 250 ) {
							System.out.print( "." );
							count = 0;
						} else {
							count++;
						}
					}

					Collections.sort( filter1Numbers );
					Collections.reverse( filter1Numbers );
					Collections.sort( filter2Numbers );
					Collections.reverse( filter2Numbers );

					for ( Node f : targetNodeFont ) {
						f.getParentNode().removeChild( f );
					}

					System.out.println( "" );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					Result output = new StreamResult( tableXmlRed1 );
					Source input = new DOMSource( docTable );
					transformer.transform( input, output );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole

					DocumentBuilderFactory dbfLegend = DocumentBuilderFactory.newInstance();
					DocumentBuilder dbLegend = dbfLegend.newDocumentBuilder();
					Document docLegend;
					docLegend = dbLegend.parse( new FileInputStream( tableXmlRed1 ), "UTF-8" );
					docLegend.getDocumentElement().normalize();
					dbfLegend.setFeature( "http://xml.org/sax/features/namespaces", false );
					NodeList nlColumn = docLegend.getElementsByTagName( "cellno" );
					int countL = 0;
					Set<Node> targetNodeFontLegend = new HashSet<Node>();
					for ( int xL = 0; xL < nlColumn.getLength(); xL++ ) {
						Node nodeColumnDetail = nlColumn.item( xL );
						String metadataNo = "metadata:<" + nodeColumnDetail.getTextContent() + ">";
						// System.out.println(metadataNo);
						if ( Util.stringInFile( metadataNo, fileList ) ) {
							// System.out.println("Node behalten");
						} else {
							// System.out.println("Parent Node zum leschen vormerken:
							// "+nodeColumnDetail.getParentNode().getNodeName());
							targetNodeFontLegend.add( nodeColumnDetail.getParentNode() );
						}
						if ( countL == 250 ) {
							System.out.print( "." );
							countL = 0;
						} else {
							countL++;
						}
					}

					for ( Node f : targetNodeFontLegend ) {
						f.getParentNode().removeChild( f );
					}

					System.out.println( "" );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					Transformer transformerL = TransformerFactory.newInstance().newTransformer();
					Result outputL = new StreamResult( tableXmlRed );
					Source inputL = new DOMSource( docLegend );
					transformerL.transform( inputL, outputL );

					// TODO Schichtung in den relevante Metadaten eintragen
					// nolay:5
					// layer1:<c3>A
					FileReader frL = new FileReader( fileList );
					BufferedReader brL = new BufferedReader( frL );
					String noLayerString = "";
					String lineL;
					while ( (lineL = brL.readLine()) != null ) {
						if ( lineL.contains( "nolay:" ) ) {
							noLayerString = lineL.replace( "nolay:", "" );
							noLayerint = Integer.parseInt( noLayerString ) + 1;
						} else if ( lineL.contains( "layer" ) ) {
							for ( int noL = 1; noL < noLayerint; noL++ ) {
								int noL1 = noL;
								// System.out.println( "Layer = " + noL1 );
								String lineLayer = "layer" + noL1 + ":";
								if ( lineL.contains( lineLayer ) ) {
									lineLayer = lineL.replace( lineLayer, "" );
									// System.out.println( lineLayer );
									String newLineLayer = "<layer" + noL1 + "></layer" + noL1 + ">" + lineLayer;
									Util.oldnewstring( lineLayer, newLineLayer, tableXmlRed );
								}
							}
						}
					}
					brL.close();
					frL.close();

					// layer0 muss noch angelegt werden mit dem Rest (row ohne Layer)
					try {
						BufferedReader reader = new BufferedReader( new FileReader( tableXmlRed ) );
						String lineNoLayer = "";
						while ( (lineNoLayer = reader.readLine()) != null ) {
							if ( lineNoLayer.contains( "<row>" ) && !lineNoLayer.contains( "<layer" ) ) {
								String newLineNoLayer = lineNoLayer.replace( "<row>", "<row><layer0></layer0>" );
								// System.out.println( counter+" " + counterSel6+" " + cMulti6 );
								Util.oldnewstring( lineNoLayer, newLineNoLayer, tableXmlRed );
							}
						}
						reader.close();
					} catch ( IOException ioe ) {
						ioe.printStackTrace();
					}
					// Ende Schichtung

					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					return true;

				} catch ( SAXException | IOException | ParserConfigurationException
						| TransformerFactoryConfigurationError | TransformerException e1 ) {
					// Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
			}
		};
	}

	public Task<Boolean> doSelTable( File tableXmlRed, Locale locale )
	{
		// TODO sel1, sel2, sel3, sel4, sel5, sel6
		return new Task<Boolean>() {
			@Override
			protected Boolean call()
			{
				try {
					FileReader fr = new FileReader( fileList );
					BufferedReader br = new BufferedReader( fr );

					String line;
					while ( (line = br.readLine()) != null ) {
						if ( line.contains( "metadata:" ) ) {
							String stringCell = line.replace( "metadata:<", "" );
							stringCell = stringCell.replace( ">", "" );
							String newLineTh = "<th style=\"text-align:left\">" + stringCell + "</th><th></th>";
							String newLineTd = "<td><xsl:value-of select=\"" + stringCell + "\"/></td><td></td>";
							Util.oldnewstring( "<th></th>", newLineTh, outputFileXsl );
							Util.oldnewstring( "<td></td>", newLineTd, outputFileXsl );
						} else if ( line.contains( "masterfile:" ) ) {
							// sel1
							String lineMf = line.replace( "masterfile:", "" );
							String newLine = lineMf + "<sel1>X</sel1>";
							Util.oldnewstring( lineMf, newLine, tableXmlRed );
						} else if ( line.contains( "filter1:" ) ) {
							// sel2
							for ( int i1 = 0; i1 < filterSize1Int; i1++ ) {
								String lineF1 = filter1 + filter1Numbers.get( i1 + 1 ) + "<";
								String newLineF1 = "<sel2>X</sel2>" + lineF1;
								Util.oldnewstring( lineF1, newLineF1, tableXmlRed );
							}
						} else if ( line.contains( "filter2:" ) ) {
							// sel3
							for ( int i2 = 0; i2 < filterSize2Int; i2++ ) {
								String lineF2 = filter2 + filter2Numbers.get( i2 + 1 ) + "<";
								String newLineF2 = "<sel3>X</sel3>" + lineF2;
								Util.oldnewstring( lineF2, newLineF2, tableXmlRed );
							}
						} else if ( line.contains( "selection:" ) ) {
							// sel4
							String lineSelection = line.replace( "selection:", "" );
							String newLineSelection = lineSelection + "<sel4>X</sel4>";
							Util.oldnewstring( lineSelection, newLineSelection, tableXmlRed );
						} else if ( line.contains( "keptfile:" ) ) {
							// sel5
							String lineKept = line.replace( "keptfile:", "" );
							String newLineKept = lineKept + "<sel5>X</sel5>";
							Util.oldnewstring( lineKept, newLineKept, tableXmlRed );
						}
					}
					br.close();
					fr.close();

					// do sel6 tableXmlRed pro Layer inkl 0
					for ( int noL = 0; noL < noLayerint; noL++ ) {

						try {
							DocumentBuilderFactory dbfTable = DocumentBuilderFactory.newInstance();
							DocumentBuilder dbTable;
							dbTable = dbfTable.newDocumentBuilder();
							Document docTable;
							docTable = dbTable.parse( new FileInputStream( tableXmlRed ), "UTF-8" );
							docTable.getDocumentElement().normalize();
							dbfTable.setFeature( "http://xml.org/sax/features/namespaces", false );
							NodeList nlLayer = docTable.getElementsByTagName( "layer" + noL );
							rowCounter = nlLayer.getLength();
						} catch ( ParserConfigurationException | SAXException e ) {
							// Auto-generated catch block
							e.printStackTrace();
						}

						int n = rowCounter;
						double d1 = (n - 384);
						double d2 = (n - 1);
						double d = d1 / d2;
						double sqrtD = Math.sqrt( d );
						double sampleCount = sqrtD * 384;
						double c = n / sampleCount;
						// System.out.println( " n="+n + " d1="+d1+" d2="+d2+" d="+d+" sqrtD="+sqrtD);
						// System.out.println( " sampleCount="+ sampleCount +" c="+ c );

						int counter = 1;
						int counterSel6 = 1;
						int dot = 0;
						try {
							BufferedReader reader = new BufferedReader( new FileReader( tableXmlRed ) );
							String line6 = "";
							while ( (line6 = reader.readLine()) != null ) {
								/* Vorher <?xml version="1.0" encoding="utf-8" standalone="no"?><table
								 * xmlns="http://www.admin.ch/xmlns/siard/1.0/schema0/table4.xsd"
								 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:
								 * schemaLocation="http://www.admin.ch/xmlns/siard/1.0/schema0/table4.xsd table4.xsd"
								 * > */

								/* Nachher <?xml version="1.0" encoding="utf-8"?><?xml-stylesheet type="text/xsl"
								 * href="kost-ran.xsl"?><table><Infos><Start>08.04.2021</Start><Info>KOST-Ran
								 * v0.0.1, Copyright (C) 2021 Claire Roethlisberger (KOST-CECO). This program comes
								 * with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to
								 * redistribute it under certain conditions; see GPL-3.0_COPYING.txt for
								 * details</Info></Infos> */

								java.util.Date nowStart = new java.util.Date();
								java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat(
										"dd.MM.yyyy" );
								String ausgabeStart = sdfStart.format( nowStart );
								String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?><?xml-stylesheet type=\"text/xsl\" href=\"kost-ran_"
										+ fileSiard.getName() + "_" + fileList.getName()
										+ ".xsl\"?><table><Infos><Start>" + ausgabeStart + "</Start><Info>KOST-Ran v"
										+ versionKostRan
										+ ", Copyright (C) 2021 Claire Roethlisberger (KOST-CECO). This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; see GPL-3.0_COPYING.txt for details</Info></Infos>";
								/* String header =
								 * "<?xml version=\"1.0\" encoding=\"utf-8\"?><?xml-stylesheet type=\"text/xsl\" href=\"kost-ran.xsl\"?><table><Infos><Start>"
								 * + ausgabeStart + "</Start><Info>KOST-Ran v" + versionKostRan +
								 * ", Copyright (C) 2021 Claire Roethlisberger (KOST-CECO). This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; see GPL-3.0_COPYING.txt for details</Info></Infos>"
								 * ; */
								// System.out.print( counter+" " );
								if ( line6.contains( "<?xml version=" ) ) {
									Util.oldnewstring( line6, header, tableXmlRed );
								} else if ( line6.contains( "<layer" + noL + ">" ) ) {
									double cMulti6 = c * counterSel6;
									if ( counter == cMulti6 || counter > cMulti6 ) {
										if ( dot == 10 ) {
											System.out.print( "." );
											dot = 0;
										} else {
											dot++;
										}
										String newLineSel6 = line6.replace( "<row>", "<row><sel6>L" + noL + "</sel6>" );
										Util.oldnewstring( line6, newLineSel6, tableXmlRed );
										++counterSel6;
										++counter;
									} else {
										++counter;
									}
								}
							}
							reader.close();
						} catch ( IOException ioe ) {
							ioe.printStackTrace();
						}
					}

					Util.oldnewstring( "<sel1>X</sel1><sel1>X</sel1>", "<sel1>X</sel1>", tableXmlRed );
					Util.oldnewstring( "<sel2>X</sel2><sel2>X</sel2>", "<sel2>X</sel2>", tableXmlRed );
					Util.oldnewstring( "<sel3>X</sel3><sel3>X</sel3>", "<sel3>X</sel3>", tableXmlRed );
					Util.oldnewstring( "<sel4>X</sel4><sel4>X</sel4>", "<sel4>X</sel4>", tableXmlRed );
					Util.oldnewstring( "<sel5>X</sel5><sel5>X</sel5>", "<sel5>X</sel5>", tableXmlRed );
					Util.oldnewstring( "</sel6><sel6>", "", tableXmlRed );

					/* Util.oldnewstring( "<sel6>X</sel6>  <row>", "  <row><sel6>X</sel6>", tableXmlRed );
					 * Util.oldnewstring( "<sel6>X</sel6> <row>", " <row><sel6>X</sel6>", tableXmlRed );
					 * Util.oldnewstring( "<sel6>X</sel6><row>", "<row><sel6>X</sel6>", tableXmlRed );
					 * Util.oldnewstring( "<sel6>X</sel6>   <row>", "   <row><sel6>X</sel6>", tableXmlRed
					 * ); */

				} catch ( IOException | TransformerFactoryConfigurationError e1 ) {
					// Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
				return true;

			}
		};
	}

	/* TODO Finish-Marker
	 * 
	 * Wenn buttonFinish betaetigt wird, wird die alle Rows geloescht ohne sel */
	@FXML
	void doFinish( ActionEvent e )
	{
		console.setText( " \n" );
		buttonFinish.setDisable( true );

		try {
			BufferedReader br = new BufferedReader( new FileReader( tableXmlRed ) );
			StringBuffer inputBuffer = new StringBuffer();
			String line;

			while ( (line = br.readLine()) != null ) {
				if ( !line.contains( "<row>" ) ) {
					// Line behalten
					inputBuffer.append( line );
					inputBuffer.append( '\n' );
				} else if ( line.contains( "<sel" ) ) {
					// Line behalten
					inputBuffer.append( line );
					inputBuffer.append( '\n' );
				} else {
					String newLine = "";
					line = newLine;
					// line = line.replace( line + "\n", newLine );
					inputBuffer.append( line );
				}
			}
			br.close();

			FileOutputStream fileOut = new FileOutputStream( tableXmlRed );
			fileOut.write( inputBuffer.toString().getBytes() );
			fileOut.close();

			// Zeitstempel Start
			java.util.Date nowStart = new java.util.Date();
			java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "yyyy-mm-dd_HH.mm" );
			String ausgabeStart = sdfStart.format( nowStart );

			outputFile = new File( outputFolder.getAbsolutePath() + File.separator + ausgabeStart + "_"
					+ fileSiard.getName() + "_" + fileList.getName() + ".xml" );
			if ( outputFile.exists() ) {
				Util.deleteFile( outputFile );
				outputFile.mkdirs();
			}

			ch.kostceco.tools.kosttools.util.Util.copyFile( tableXmlRed, outputFile );

			if ( outputFile.exists() ) {
				System.out.println( outputFile.getAbsolutePath() );
				engine.load( "file:///" + outputFile.getAbsolutePath() );
				scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				buttonPrint.setDisable( false );
				buttonSave.setDisable( false );
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
			if ( tempFile.exists() ) {
				Util.deleteDir( tempFile );
				tempFile.mkdirs();
			}
		} catch ( IOException | TransformerFactoryConfigurationError e1 ) {
			e1.printStackTrace();
		}
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeLang wird die Sprache umgestellt
	@FXML
	void changeLang( ActionEvent event )
	{
		console.setText( " \n" );
		String selLang = lang.getValue();
		if ( selLang.equals( "Français" ) ) {
			locale = new Locale( "fr" );
			lang.setValue( "Français" );
			labelSiard.setText( "Fichier SIARD" );
			labelTable.setText( "Tableau principale" );
			labelList.setText( "Données de configuration" );
			buttonSiard.setText( "sélectionnez" );
			buttonTable.setText( "sélectionnez" );
			buttonList.setText( "sélectionnez" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
			buttonSave.setText( "sauvegarder" );
			buttonPrint.setText( "imprimer" );
			buttonFinish.setText( "Effectuer l'échantillonnage" );
		} else if ( selLang.equals( "English" ) ) {
			locale = new Locale( "en" );
			lang.setValue( "English" );
			labelSiard.setText( "SIARD file" );
			labelTable.setText( "Main table" );
			labelList.setText( "Configuration data" );
			buttonSiard.setText( "Select" );
			buttonTable.setText( "select" );
			buttonList.setText( "select" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
			buttonSave.setText( "save" );
			buttonPrint.setText( "print" );
			buttonFinish.setText( "Perform sampling" );
		} else {
			locale = new Locale( "de" );
			lang.setValue( "Deutsch" );
			labelSiard.setText( "SIARD-Datei" );
			labelTable.setText( "Haupttabelle" );
			labelList.setText( "Konfigurationsdaten" );
			buttonSiard.setText( "Auswählen" );
			buttonTable.setText( "Auswählen" );
			buttonList.setText( "Auswählen" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenz-Informationen" );
			buttonSave.setText( "speichern" );
			buttonPrint.setText( "drucken" );
			buttonFinish.setText( "Sampling durchführen" );
		}
	}

}