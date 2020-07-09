package ch.kostceco.tools.kostval;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.kostceco.tools.kostval.controller.ControllerInit;
import ch.kostceco.tools.kostval.util.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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

public class GuiController
{/* VM arguments:
	 *
	 * --module-path "C:\Program Files\openjfx-14.0.1_windows-x64_bin-sdk\javafx-sdk-14.0.1\lib"
	 * --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web */
	@FXML
	private Button						buttonHelp, buttonFolder, buttonFile, buttonFormat, buttonSip,
			buttonLicence, buttonChange, buttonShowConfig, buttonPrint, buttonSave;

	ObservableList<String>		langList		= FXCollections.observableArrayList( "Deutsch", "Français",
			"English" );
	ObservableList<String>		logTypeList	= FXCollections.observableArrayList( "LogType: --xml",
			"LogType: --max" );
	// TODO "LogType: --min",

	@FXML
	private ChoiceBox<String>	lang, logType;

	@FXML
	private Label							labelFileFolder, labelStart, labelConfig, label;

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

	private String						arg0, arg1, arg2, arg3 = "--max", dirOfJarPath;

	private Locale						locale			= Locale.getDefault();

	@FXML
	private ScrollPane				scroll;

	@FXML
	void initialize()
	{
		/* TODO
		 * 
		 * LogTyp --min programmieren
		 * 
		 * speichern drucken von log */

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

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
		try {
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
				buttonSave.setText( "sauvegarder" );
				buttonPrint.setText( "imprimer" );
				Util.oldnewstring( "kostval-conf-DE.xsl", "kostval-conf-FR.xsl", configFile );
				Util.oldnewstring( "kostval-conf-EN.xsl", "kostval-conf-FR.xsl", configFile );
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
				buttonSave.setText( "save" );
				buttonPrint.setText( "print" );
				Util.oldnewstring( "kostval-conf-DE.xsl", "kostval-conf-EN.xsl", configFile );
				Util.oldnewstring( "kostval-conf-FR.xsl", "kostval-conf-EN.xsl", configFile );
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
				buttonSave.setText( "speichern" );
				buttonPrint.setText( "drucken" );
				Util.oldnewstring( "kostval-conf-FR.xsl", "kostval-conf-DE.xsl", configFile );
				Util.oldnewstring( "kostval-conf-EN.xsl", "kostval-conf-DE.xsl", configFile );
			}
		} catch ( IOException e1 ) {
			e1.printStackTrace();
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

		// Speichern und drucken des Log erst bei anzeige des log moeglich
		// TODO Speichern und drucken des Log noch nicht programiert
		buttonPrint.setDisable( true );
		buttonSave.setDisable( true );

		lang.getItems().addAll( langList );
		logType.getItems().addAll( logTypeList );

		/* Kontrolle der wichtigsten Eigenschaften: Log-Verzeichnis, Arbeitsverzeichnis, Java, jhove
		 * Configuration, Konfigurationsverzeichnis, path.tmp */
		ControllerInit controllerInit = (ControllerInit) context.getBean( "controllerInit" );
		boolean init;
		try {
			init = controllerInit.init( locale, dirOfJarPath );
			if ( !init ) {
				// Fehler: es wird abgebrochen
				String text = "Ein Fehler ist aufgetreten. Siehe Konsole.";
				if ( locale.toString().startsWith( "fr" ) ) {
					text = "Une erreur s`est produite. Voir Console.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					text = "An error has occurred. See Console.";
				}
				engine.loadContent( "<html><h2>" + text + "</h2></html>" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		context.close();

	}

	protected void updateEngine( String message )
	{
		engine.loadContent( message );
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
		/* Kurzanleitung 1. Datei oder Ordner zur Validierung angeben / auswählen 2. ggf. Konfiguration
		 * und LogType anpassen 3. Validierung starten */
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

	@FXML
	void showConfig( ActionEvent e )
	{
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* Wenn valFormat betaetigt wird, wird die Formatvalidierung gestartet mit allen Parametern. */
	@FXML
	void valFormat( ActionEvent e )
	{
		String text = "<html><h2>Formatvalidierung wird durchgefuehrt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La validation du format est effectuee. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>Format validation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--format"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (= xml+verbose TODO GUI)
		 * 
		 * 2 und 3 waeren optional werden aber mitgegeben */
		arg0 = "--format";
		arg2 = "--" + locale.toString();
		arg1 = fileFolder.getText();
		File arg1File = new File( arg1 );
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
				+ File.separator + "logs" + File.separator + arg1File.getName() + ".kost-val.log.xml" );
		// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, loeschen wir es
		if ( logFile.exists() ) {
			logFile.delete();
		}
		if ( logFile.exists() ) {
			String exist = "Wurde bereits validiert. Keine neue Validierung durchgefuehrt!";
			if ( locale.toString().startsWith( "fr" ) ) {
				exist = "A deja ete valide. Aucune nouvelle validation n'est effectuee !";
			} else if ( locale.toString().startsWith( "en" ) ) {
				exist = "Has already been validated. No new validation performed!";
			}
			System.out.println( exist );
			System.out.println( "" );
			engine.loadContent( "<html><h2>" + exist + "</h2></html>" );
		} else {
			/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] ); */
			// KOSTVal.main im Hintergrund Task (val) starten
			final Task<Boolean> val = doVal( args );
			val.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle( WorkerStateEvent t )
				{
					/* Dieser handler wird bei einer erfolgreichen Validierung ausgefuehrt.
					 * 
					 * Da es erfolgreich war (valid / invalid) kann der Log angezeigt werden */
					engine.load( "file:///" + logFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				}
			} );
			val.setOnFailed( new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle( WorkerStateEvent t )
				{
					/* Dieser handler wird ausgefuehrt wenn die Validierung nicht korrekt abgelaufen ist
					 * (Fehler).
					 * 
					 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
					String text = "Ein unbekannter Fehler ist aufgetreten.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Une erreur inconnue s`est produite.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "An unknown error has occurred.";
					}
					System.out.println( text );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				}
			} );
			new Thread( val ).start();
		}
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
					if ( KOSTVal.main( args ) ) {
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

	/* Wenn valSip betaetigt wird, wird die SIP-Validierung gestartet mit allen Parametern. */
	@FXML
	void valSip( ActionEvent e )
	{
		String text = "<html><h2>Formatvalidierung wird durchgefuehrt. <br/><br/>Bitte warten ...</h2></html>";
		if ( locale.toString().startsWith( "fr" ) ) {
			text = "<html><h2>La validation du format est effectuee. <br/><br/>Veuillez patienter ...</h2></html>";
		} else if ( locale.toString().startsWith( "en" ) ) {
			text = "<html><h2>Format validation is performed. <br/><br/>Please wait ...</h2></html>";
		}
		engine.loadContent( text );
		/* hier die diversen args an main uebergeben
		 * 
		 * main( String[] args )
		 * 
		 * args[0] "--sip"
		 * 
		 * args[1] Pfad zur Val-File
		 * 
		 * args[2] "--de" / "--fr" / "--en"
		 * 
		 * args[3] "--xml" / "--min" (TODO nur valid oder invalid) / "--max" (= xml+verbose TODO GUI)
		 * 
		 * 2 und 3 waeren optional werden aber mitgegeben */
		arg0 = "--sip";
		arg2 = "--" + locale.toString();
		arg1 = fileFolder.getText();
		File arg1File = new File( arg1 );
		String[] args = new String[] { arg0, arg1, arg2, arg3 };
		File logFile = new File( System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
				+ File.separator + "logs" + File.separator + arg1File.getName() + ".kost-val.log.xml" );
		// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, loeschen wir es
		if ( logFile.exists() ) {
			logFile.delete();
		}
		if ( logFile.exists() ) {
			String exist = "Wurde bereits validiert. Keine neue Validierung durchgefuehrt!";
			if ( locale.toString().startsWith( "fr" ) ) {
				exist = "A deja ete valide. Aucune nouvelle validation n'est effectuee !";
			} else if ( locale.toString().startsWith( "en" ) ) {
				exist = "Has already been validated. No new validation performed!";
			}
			System.out.println( exist );
			System.out.println( "" );
			engine.loadContent( "<html><h2>" + exist + "</h2></html>" );
		} else {
			/* System.out.println( "cmd: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] ); */
			// KOSTVal.main im Hintergrund Task (val) starten
			final Task<Boolean> val = doVal( args );
			val.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle( WorkerStateEvent t )
				{
					/* Dieser handler wird bei einer erfolgreichen Validierung ausgefuehrt.
					 * 
					 * Da es erfolgreich war (valid / invalid) kann der Log angezeigt werden */
					engine.load( "file:///" + logFile.getAbsolutePath() );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
				}
			} );
			val.setOnFailed( new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle( WorkerStateEvent t )
				{
					/* Dieser handler wird ausgefuehrt wenn die Validierung nicht korrekt abgelaufen ist
					 * (Fehler).
					 * 
					 * Da es nicht erfolgreich war kann der Log nicht angezeigt werden */
					String text = "Ein unbekannter Fehler ist aufgetreten.";
					if ( locale.toString().startsWith( "fr" ) ) {
						text = "Une erreur inconnue s`est produite.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						text = "An unknown error has occurred.";
					}
					System.out.println( text );
					scroll.setVvalue( 1.0 ); // 1.0 = letzte Zeile der Konsole
					engine.loadContent( "<html><h2>" + text + "</h2></html>" );
				}
			} );
			new Thread( val ).start();
		}
	}

	/* Wenn choseFile betaetigt wird, kann eine Datei ausgewaehlt werden */
	@FXML
	void chooseFile( ActionEvent e )
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
	void chooseFolder( ActionEvent e )
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

	/* Wenn changeConfig betaetigt wird, oeffnet sich das KonfigGui im neuen Fenster */
	@FXML
	void changeConfig( ActionEvent e )
	{
		try {
			StackPane configLayout = new StackPane();

			configLayout = FXMLLoader.load( getClass().getResource( "ConfigView.fxml" ) );
			Scene configScene = new Scene( configLayout );
			configScene.getStylesheets()
					.add( getClass().getResource( "application.css" ).toExternalForm() );

			// New window (Stage)
			Stage configStage = new Stage();

			configStage.setTitle( "KOST-Val   -   Configuration" );
			Image kostvalIcon = new Image(
					"file:" + dirOfJarPath + File.separator + "doc" + File.separator + "valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			configStage.initModality( Modality.APPLICATION_MODAL );
			configStage.getIcons().add( kostvalIcon );
			configStage.setScene( configScene );
			configStage.show();
		} catch ( IOException e1 ) {
			e1.printStackTrace();
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
				if ( valFileFolder.exists() ) {
					// TODO: hier laufend weitere Viewer einbauen
					String text = " --> " + valFileFolder.getAbsolutePath();
					engine.loadContent( text );
				} else {
					String notexist = "Ungültiger Pfad! " + valFileFolder.getAbsolutePath()
							+ " existiert nicht.";
					if ( locale.toString().startsWith( "fr" ) ) {
						notexist = "Lien invalide ! " + valFileFolder.getAbsolutePath() + " n'existe pas.";
					} else if ( locale.toString().startsWith( "en" ) ) {
						notexist = "Illegal path! " + valFileFolder.getAbsolutePath() + " doesn't exist.";
					}
					engine.loadContent( notexist );
				}
			}
		} else {
			if ( valFileFolder.exists() ) {
				String text = " --> " + valFileFolder.getAbsolutePath();
				engine.loadContent( text );
			} else {
				String notexist = "Ungültiger Pfad! " + valFileFolder.getAbsolutePath()
						+ "existiert nicht.";
				if ( locale.toString().startsWith( "fr" ) ) {
					notexist = "Lien invalide ! " + valFileFolder.getAbsolutePath() + " n'existe pas.";
				} else if ( locale.toString().startsWith( "en" ) ) {
					notexist = "Illegal path! " + valFileFolder.getAbsolutePath() + " doesn't exist.";
				}
				engine.loadContent( notexist );
			}
		}
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeLoType wird die Log umgestellt
	@FXML
	void changeLogType( ActionEvent event )
	{
		String selLogType = logType.getValue();
		if ( selLogType.equals( "LogType: --min" ) ) {
			arg3 = "--min";
		} else if ( selLogType.equals( "LogType: --max" ) ) {
			arg3 = "--max";
		} else {
			arg3 = "--xml";
		}
	}

	// Mit changeLang wird die Sprache umgestellt
	@FXML
	void changeLang( ActionEvent event )
	{
		String selLang = lang.getValue();
		try {
			if ( selLang.equals( "Deutsch" ) ) {
				buttonFormat.setText( "Formatvalidierung" );
				buttonSip.setText( "SIP-Validierung" );
				labelFileFolder.setText( "Wähle Datei / Ordner" );
				buttonFolder.setText( "Ordner" );
				buttonFile.setText( "Datei" );
				buttonHelp.setText( "Hilfe ?" );
				buttonLicence.setText( "Lizenzinformationen" );
				buttonChange.setText( "ändere" );
				buttonShowConfig.setText( "zeige" );
				labelStart.setText( "Starte" );
				labelConfig.setText( "Konfiguration" );
				buttonSave.setText( "speichern" );
				buttonPrint.setText( "drucken" );
				Util.oldnewstring( "kostval-conf-FR.xsl", "kostval-conf-DE.xsl", configFile );
				Util.oldnewstring( "kostval-conf-EN.xsl", "kostval-conf-DE.xsl", configFile );
				locale = new Locale( "de" );
			} else if ( selLang.equals( "English" ) ) {
				buttonFormat.setText( "Format validation" );
				buttonSip.setText( "SIP Validation" );
				labelFileFolder.setText( "Select file / folder" );
				buttonFolder.setText( "folder" );
				buttonFile.setText( "file" );
				buttonHelp.setText( "Help ?" );
				buttonLicence.setText( "License information" );
				buttonChange.setText( "change" );
				buttonShowConfig.setText( "show" );
				labelStart.setText( "Start" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "save" );
				buttonPrint.setText( "print" );
				Util.oldnewstring( "kostval-conf-DE.xsl", "kostval-conf-EN.xsl", configFile );
				Util.oldnewstring( "kostval-conf-FR.xsl", "kostval-conf-EN.xsl", configFile );
				locale = new Locale( "en" );
			} else {
				buttonFormat.setText( "Validation du format" );
				buttonSip.setText( "Validation du SIP" );
				labelFileFolder.setText( "Sélectionnez" );
				buttonFolder.setText( "dossier" );
				buttonFile.setText( "fichier" );
				buttonHelp.setText( "Aide ?" );
				buttonLicence.setText( "Informations sur la licence" );
				buttonChange.setText( "changer" );
				buttonShowConfig.setText( "afficher" );
				labelStart.setText( "Lancer" );
				labelConfig.setText( "Configuration" );
				buttonSave.setText( "sauvegarder" );
				buttonPrint.setText( "imprimer" );
				Util.oldnewstring( "kostval-conf-DE.xsl", "kostval-conf-FR.xsl", configFile );
				Util.oldnewstring( "kostval-conf-EN.xsl", "kostval-conf-FR.xsl", configFile );
				locale = new Locale( "fr" );
			}
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}

}