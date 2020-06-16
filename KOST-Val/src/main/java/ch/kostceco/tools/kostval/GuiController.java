package ch.kostceco.tools.kostval;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;

import ch.kostceco.tools.kostval.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GuiController
{/* VM arguments:
	 *
	 * --module-path "C:\Program Files\openjfx-14.0.1_windows-x64_bin-sdk\javafx-sdk-14.0.1\lib"
	 * --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web */
	@FXML
	private CheckBox					checkPdfa, checkJpeg2000, checkJpeg, checkTiff, checkSiard;

	@FXML
	private Button						buttonHelp, buttonFolder, buttonFile, buttonFormat, buttonSip,
			buttonLicence;

	ObservableList<String>		langList		= FXCollections.observableArrayList( "Deutsch", "Français",
			"English" );

	@FXML
	private ChoiceBox<String>	lang;

	@FXML
	private Label							labelFileFolder, label;

	@FXML
	private TextField					fileFolder;

	@FXML
	private TextArea					console;

	private PrintStream				ps;

	@FXML
	private WebView						wbv;

	private WebEngine					engine;

	private File							configFile	= new File( System.getenv( "USERPROFILE" ) + File.separator
			+ ".kost-val" + File.separator + "configuration" + File.separator + "kostval.conf.xml" );

	private String						arg0, arg1, arg2, arg3, arg4, dirOfJarPath;

	private Locale						locale			= Locale.getDefault();

	@FXML
	void initialize()
	{
		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		label.setText(
				"Copyright © KOST/CECO          JavaFX " + javafxVersion + " & Java " + javaVersion + "." );

		// PrintStream in Konsole umleiten
		ps = new PrintStream( new Console( console ) );
		System.setOut( ps );
		System.setErr( ps );

		// Sprache definieren
		locale = Locale.getDefault();
		if ( locale.toString().startsWith( "fr" ) ) {
			locale = new Locale( "fr" );
			arg2 = locale.toString();
			lang.setValue( "Français" );
			buttonFormat.setText( "Validation du format" );
			buttonSip.setText( "Validation du SIP" );
			labelFileFolder.setText( "Sélectionnez" );
			buttonFolder.setText( "dossier" );
			buttonFile.setText( "fichier" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			locale = new Locale( "en" );
			arg2 = locale.toString();
			lang.setValue( "English" );
			buttonFormat.setText( "Format validation" );
			buttonSip.setText( "SIP Validation" );
			labelFileFolder.setText( "Select file / folder" );
			buttonFolder.setText( "folder" );
			buttonFile.setText( "file" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
		} else {
			locale = new Locale( "de" );
			arg2 = locale.toString();
			lang.setValue( "Deutsch" );
			buttonFormat.setText( "Formatvalidierung" );
			buttonSip.setText( "SIP-Validierung" );
			labelFileFolder.setText( "Wähle Datei / Ordner" );
			buttonFolder.setText( "Ordner" );
			buttonFile.setText( "Datei" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenzinformationen" );
		}

		// festhalten von wo die Applikation (exe) gestartet wurde
		dirOfJarPath = "";
		try {
			/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
			 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit dirOfJarPath +
			 * File.separator + erweitern. */
			String path = new File( "" ).getAbsolutePath();
			String locationOfJarPath = path;
			dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}
			setLibraryPath( dirOfJarPath );
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		engine = wbv.getEngine();
		// String strTxtUrl "file:///C:/Users/KOST/.kost-val/logs/doc.kost-val.log.xml" ;
		// engine.load( strTxtUrl );
		// Bild mit einer Kurzanleitung zum GUI anzeigen
		String pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator
				+ "hilfe.jpg";
		if ( locale.toString().startsWith( "fr" ) ) {
			pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator + "aide.jpg";
		} else if ( locale.toString().startsWith( "en" ) ) {
			pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator + "help.jpg";
		}
		engine.load( pathImage );
		// Format und Sip Validierung erst moeglich wenn fileFolder ausgefuellt
		buttonSip.setDisable( true );
		buttonFormat.setDisable( true );

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		// checkPdfa, checkJpeg2000, checkJpeg, checkTiff, checkSiard;
		try {
			byte[] encoded;
			encoded = Files.readAllBytes( Paths.get( configFile.getAbsolutePath() ) );
			String config = new String( encoded, StandardCharsets.UTF_8 );
			String noPdfa = "<pdftools>no</pdftools>";
			String noJpeg2000 = "<jp2validation>no</jp2validation>";
			String noJpeg = "<jpegvalidation>no</jpegvalidation>";
			String noTiff = "<tiffvalidation>no</tiffvalidation>";
			String noSiard = "<siardvalidation>no</siardvalidation>";
			if ( config.contains( noPdfa ) ) {
				checkPdfa.setSelected( false );
			}
			if ( config.contains( noJpeg2000 ) ) {
				checkJpeg2000.setSelected( false );
			}
			if ( config.contains( noJpeg ) ) {
				checkJpeg.setSelected( false );
			}
			if ( config.contains( noTiff ) ) {
				checkTiff.setSelected( false );
			}
			if ( config.contains( noSiard ) ) {
				checkSiard.setSelected( false );
			}
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		lang.getItems().addAll( langList );
	}

	public static void setLibraryPath( String path ) throws Exception
	{
		System.setProperty( "java.library.path", path );
		// set sys_paths to null so that java.library.path will be reevalueted next time it is needed
		final Field sysPathsField = ClassLoader.class.getDeclaredField( "sys_paths" );
		sysPathsField.setAccessible( true );
		sysPathsField.set( null, null );
	}

	// Leitet die Konsole in die TextArea um
	public class Console extends OutputStream
	{
		private TextArea console;

		public Console( TextArea console )
		{
			this.console = console;
			// letzte Zeile anzeigen
			console.positionCaret( console.getText().length() );
		}

		public void appendText( String valueOf )
		{
			Platform.runLater( () -> console.appendText( valueOf ) );
			;
			// letzte Zeile anzeigen
			console.positionCaret( console.getText().length() );
		}

		public void write( int b ) throws IOException
		{
			appendText( String.valueOf( (char) b ) );
			// letzte Zeile anzeigen
			console.positionCaret( console.getText().length() );
		}
	}

	/* TODO --> Button =============== */

	@FXML
	void showManual( ActionEvent e )
	{
		/* Kurzanleitung 1. Sprache wählen 2. Datei oder Ordner zur Validierung angeben / auswählen 3.
		 * ggf. Konfiguration anpassen 4. Validierung starten */

		try {
			// Bild mit einer Kurzanleitung zum GUI anzeigen
			String pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator
					+ "hilfe.jpg";
			if ( locale.toString().startsWith( "fr" ) ) {
				pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator
						+ "aide.jpg";
			} else if ( locale.toString().startsWith( "en" ) ) {
				pathImage = "file:///" + dirOfJarPath + File.separator + "doc" + File.separator
						+ "help.jpg";
			}
			engine.load( pathImage );
			// Handbuch in externem Viewer oeffnen
			String docPath = dirOfJarPath + File.separator + "doc" + File.separator
					+ "KOST-Val_Anwendungshandbuch_v1.9.8.pdf";
			// Spaeter in Center oeffnen, wenn WebView pdf unterstuetzt
			File dirDoc = new File( "doc" );
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
			Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " + docPath );
		} catch ( IOException eManual ) {
			eManual.printStackTrace();
		}
	}

	@FXML
	void showLicence( ActionEvent e )
	{
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
			licence5 = "- KOST-Val utilise des composants non modifiés d'autres fabricants en les incorporant directement dans le code source.";
			licence6 = "- Les utilisateurs de KOST-Val sont priés de respecter les conditions de licence de ces composants.";
		} else if ( locale.toString().startsWith( "en" ) ) {
			licence1 = "<h2>This program comes with ABSOLUTELY NO WARRANTY.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>This is free software, and you are welcome to redistribute it under certain conditions;</h4>";
			licence4 = "- see the manual and GPL-3.0_COPYING.txt for details. ";
			licence5 = "- KOST-Val uses unmodified components of other manufacturers by embedding them directly into the source code.";
			licence6 = "- Users of KOST-Val are requested to adhere to these components ‘terms of licence.";
		} else {
			licence1 = "<h2>Dieses Programm kommt mit ABSOLUT KEINER GARANTIE.</h2>";
			licence2 = "<hr>";
			licence3 = "<h4>Es handelt sich um freie Software, und Sie dürfen sie unter bestimmten Bedingungen gerne weitergeben;</h4>";
			licence4 = "- siehe das Handbuch und GPL-3.0_COPYING.txt für Einzelheiten. ";
			licence5 = "- KOST-Val verwendet unmodifizierte Komponenten anderer Hersteller, indem diese direkt in den Quellcode eingebettet werden.";
			licence6 = "- Benutzer von KOST-Val werden gebeten, sich an die Lizenzbedingungen dieser Komponenten zu halten.";
		}
		String text = "<html>" + licence1 + licence2 + licence3 + licence4 + "<br/>" + licence5
				+ "<br/>" + licence6 + "</html>";
		engine.loadContent( text );
	}

	/* @FXML void showConfig( ActionEvent e ) { // engine.reload(); // String strUrl =
	 * "https://kost-ceco.ch" ; // engine.load( strUrl);
	 * 
	 * /* try { byte[] encoded; encoded = Files.readAllBytes( Paths.get( configFile.getAbsolutePath()
	 * ) ); String config = new String( encoded, StandardCharsets.UTF_8 ); // Show in VIEW
	 * engine.loadContent( config, "text/plain" ); /* engine.loadContent( config, "text/xml" ); mit
	 * xml werden nur die Inhalte angezeigt */
	/* } catch ( IOException e1 ) { e1.printStackTrace(); } */
	/* engine.load( "file:///" + configFile.getAbsolutePath() );
	 * 
	 * } */

	/* Wenn valFormat betaetigt wird, wird die Formatvalidierung gestartet mit allen Parametern. */
	@FXML
	void valFormat( ActionEvent event )
	{
		// engine leeren
		engine.loadContent( "                           " );
		String text = "Formatvalidierung wird durchgefuehrt. Bitte warten ...";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "La validation du format est effectuee. Veuillez patienter ...";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "Format validation is performed. Please wait ...";
		}
		engine.loadContent( text );

		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--format"
		 * 
		 * args[1] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (= xml+verbose TODO GUI)
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--gui"
		 * 
		 * args[4] Pfad zur Val-File */
		arg0 = "--format";
		arg1 = "--xml";
		arg2 = "--" + locale.toString();
		arg3 = "--gui";
		arg4 = fileFolder.getText();
		File arg4File = new File( arg4 );
		String[] args = new String[] { arg0, arg1, arg2, arg3, arg4 };
		// letzte Zeile anzeigen
		console.positionCaret( console.getText().length() );
		// KOSTVal.main starten
		/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " +
		 * args[4] + " " ); */
		try {
			if ( KOSTVal.main( args ) ) {
				// Valid
				// System.out.println("valid: "+arg4File.getAbsolutePath());
			} else {
				// Invalid
				// System.out.println("invalid: "+arg4File.getAbsolutePath());
			}
			// letzte Zeile anzeigen
			console.positionCaret( console.getText().length() );
			File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
					+ File.separator + "logs" + File.separator + arg4File.getName() + ".kost-val.log.xml" );
			engine.load( "file:///" + logFile.getAbsolutePath() );
		} catch ( IOException e ) {
			System.out.println( e );
		}
		// letzte Zeile anzeigen
		console.positionCaret( console.getText().length() );
	}

	/* Wenn valSip betaetigt wird, wird die SIP-Validierung gestartet mit allen Parametern. */
	@FXML
	void valSip( ActionEvent event )
	{
		// engine leeren
		engine.loadContent( "                           " );
		String text = "SIP-Validierung wird durchgefuehrt. Bitte warten ...";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "La validation du SIP est effectuee. Veuillez patienter ...";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "SIP validation is performed. Please wait ...";
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--sip"
		 * 
		 * args[1] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (= xml+verbose TODO GUI)
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--gui"
		 * 
		 * args[4] Pfad zur Val-File */
		arg0 = "--sip";
		arg1 = "--xml";
		arg2 = "--" + locale.toString();
		arg3 = "--gui";
		arg4 = fileFolder.getText();
		File arg4File = new File( arg4 );
		String[] args = new String[] { arg0, arg1, arg2, arg3, arg4 };
		// letzte Zeile anzeigen
		console.positionCaret( console.getText().length() );
		// KOSTVal.main starten
		/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " +
		 * args[4] + " " ); */
		try {
			if ( KOSTVal.main( args ) ) {
				// Valid
				// System.out.println("valid: "+arg4File.getAbsolutePath());
			} else {
				// Invalid
				// System.out.println("invalid: "+arg4File.getAbsolutePath());
			}
			// letzte Zeile anzeigen
			console.positionCaret( console.getText().length() );
			File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
					+ File.separator + "logs" + File.separator + arg4File.getName() + ".kost-val.log.xml" );
			engine.load( "file:///" + logFile.getAbsolutePath() );
		} catch ( IOException e ) {
			System.out.println( e );
		}
		// letzte Zeile anzeigen
		console.positionCaret( console.getText().length() );
	}

	/* Wenn choseFile betaetigt wird, kann eine Datei ausgewaehlt werden */
	@FXML
	void chooseFile( ActionEvent event )
	{
		FileChooser fileChooser = new FileChooser();
		if ( locale.toString().startsWith( "fr" ) ) {
			fileChooser.setTitle( "choisissez le fichier" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			fileChooser.setTitle( "choose the file" );
		} else {
			fileChooser.setTitle( "wählen Sie die Datei" );
		}
		File valFile = fileChooser.showOpenDialog( new Stage() );
		if ( valFile != null ) {
			fileFolder.clear();
			fileFolder.setText( valFile.getAbsolutePath() );
			// Anzeige in WebView wenn image
			String pathImage = "file:///" + valFile.getAbsolutePath();
			String fileFolderName = valFile.getName();
			String fileFolderExt = "." + FilenameUtils.getExtension( fileFolderName ).toLowerCase();
			if ( fileFolderExt.equals( ".jp2" ) || fileFolderExt.equals( ".jpeg" )
					|| fileFolderExt.equals( ".jpg" ) || fileFolderExt.equals( ".tiff" )
					|| fileFolderExt.equals( ".tif" ) || fileFolderExt.equals( ".png" )
					|| fileFolderExt.equals( ".svg" ) ) {
				engine.load( pathImage );
			} else {
				// TODO: hier laufend weitere Viewer einbauen
				String text = " --> " + valFile.getAbsolutePath();
				engine.loadContent( text );
			}
			// Format und Sip Validierung erst moeglich wenn fileFolder ausgefuellt
			buttonFormat.setDisable( false );
			if ( valFile.getName().startsWith( "SIP" ) ) {
				buttonSip.setDisable( false );
			} else {
				buttonSip.setDisable( true );
			}
		}
	}

	/* Wenn choseFoder betaetigt wird, kann ein Ordner ausgewaehlt werden */
	@FXML
	void chooseFolder( ActionEvent event )
	{
		DirectoryChooser folderChooser = new DirectoryChooser();
		if ( locale.toString().startsWith( "fr" ) ) {
			folderChooser.setTitle( "choisissez le dossier" );
		} else if ( locale.toString().startsWith( "en" ) ) {
			folderChooser.setTitle( "choose the folder" );
		} else {
			folderChooser.setTitle( "wählen Sie den Ordner" );
		}
		File valFolder = folderChooser.showDialog( new Stage() );
		if ( valFolder != null ) {
			fileFolder.clear();
			fileFolder.setText( valFolder.getAbsolutePath() );
			// Format und Sip Validierung erst moeglich wenn fileFolder ausgefuellt
			buttonFormat.setDisable( false );
			if ( valFolder.getName().startsWith( "SIP" ) ) {
				buttonSip.setDisable( false );
			} else {
				buttonSip.setDisable( true );
			}
			String textFolder = " --> " + valFolder.getAbsolutePath();
			engine.loadContent( textFolder );
		}
	}

	/* TODO --> TextField ================= */

	/* Wenn Aenderungen an changeFileFolder gemacht wird, wird es ausgeloest */
	@FXML
	void changeFileFolder( ActionEvent event )
	{
		String pathFileFolder = fileFolder.getText();
		File valFileFolder = new File( pathFileFolder );
		if ( valFileFolder.exists() ) {
			// Format und Sip Validierung erst moeglich wenn fileFolder ausgefuellt
			buttonFormat.setDisable( false );
			if ( valFileFolder.getName().startsWith( "SIP" ) ) {
				buttonSip.setDisable( false );
			} else {
				buttonSip.setDisable( true );
			}
		} else {
			// Format und Sip Validierung erst moeglich wenn valFileFolder existiert
			buttonSip.setDisable( true );
			buttonFormat.setDisable( true );
		}
		// Anzeige in WebView wenn image
		if ( valFileFolder.isFile() ) {
			String pathImage = "file:///" + valFileFolder.getAbsolutePath();
			String fileFolderName = valFileFolder.getName();
			String fileFolderExt = "." + FilenameUtils.getExtension( fileFolderName ).toLowerCase();
			if ( fileFolderExt.equals( ".jp2" ) || fileFolderExt.equals( ".jpeg" )
					|| fileFolderExt.equals( ".jpg" ) || fileFolderExt.equals( ".tiff" )
					|| fileFolderExt.equals( ".tif" ) || fileFolderExt.equals( ".png" )
					|| fileFolderExt.equals( ".svg" ) ) {
				engine.load( pathImage );
			} else {
				// TODO: hier laufend weitere Viewer einbauen
				String text = " --> " + valFileFolder.getAbsolutePath();
				engine.loadContent( text );
			}
		} else {
			String textFolder = " --> " + valFileFolder.getAbsolutePath();
			engine.loadContent( textFolder );
		}
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeLang wird die Sprache umgestellt
	@FXML
	void changeLang( ActionEvent event )
	{
		String selLang = lang.getValue();
		if ( selLang.equals( "Deutsch" ) ) {
			buttonFormat.setText( "Formatvalidierung" );
			buttonSip.setText( "SIP-Validierung" );
			labelFileFolder.setText( "Wähle Datei / Ordner" );
			buttonFolder.setText( "Ordner" );
			buttonFile.setText( "Datei" );
			buttonHelp.setText( "Hilfe ?" );
			buttonLicence.setText( "Lizenzinformationen" );
			locale = new Locale( "de" );
		} else if ( selLang.equals( "English" ) ) {
			buttonFormat.setText( "Format validation" );
			buttonSip.setText( "SIP Validation" );
			labelFileFolder.setText( "Select file / folder" );
			buttonFolder.setText( "folder" );
			buttonFile.setText( "file" );
			buttonHelp.setText( "Help ?" );
			buttonLicence.setText( "License information" );
			locale = new Locale( "en" );
		} else {
			buttonFormat.setText( "Validation du format" );
			buttonSip.setText( "Validation du SIP" );
			labelFileFolder.setText( "Sélectionnez" );
			buttonFolder.setText( "dossier" );
			buttonFile.setText( "fichier" );
			buttonHelp.setText( "Aide ?" );
			buttonLicence.setText( "Informations sur la licence" );
			locale = new Locale( "fr" );
		}
	}

	/* TODO --> CheckBox ================= */

	/* checkPdfa schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeConfigPdfa( ActionEvent event )
	{
		String yes = "<pdftools>yes</pdftools>";
		String no = "<pdftools>no</pdftools>";
		try {
			if ( checkPdfa.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkSiard schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeConfigSiard( ActionEvent event )
	{
		String yes = "<siardvalidation>yes</siardvalidation>";
		String no = "<siardvalidation>no</siardvalidation>";
		try {
			if ( checkSiard.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkJpeg2000 schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeConfigJpeg2000( ActionEvent event )
	{
		String yes = "<jp2validation>yes</jp2validation>";
		String no = "<jp2validation>no</jp2validation>";
		try {
			if ( checkJpeg2000.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
		PrinterJob job = PrinterJob.createPrinterJob();

		engine.print( job );
		job.endJob();
	}

	/* checkJpeg schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeConfigJpeg( ActionEvent event )
	{
		String yes = "<jpegvalidation>yes</jpegvalidation>";
		String no = "<jpegvalidation>no</jpegvalidation>";
		try {
			if ( checkJpeg.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkTiff schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeConfigTiff( ActionEvent event )
	{
		String yes = "<tiffvalidation>yes</tiffvalidation>";
		String no = "<tiffvalidation>no</tiffvalidation>";
		try {
			if ( checkTiff.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

}