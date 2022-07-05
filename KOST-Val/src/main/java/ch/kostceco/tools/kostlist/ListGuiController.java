/* == KOST-Ran ==================================================================================
 * The KOST-Ran application is used for excerpt a random sample with metadata from a SIARD-File.
 * Copyright (C) 2021-2022 Claire Roethlisberger (KOST-CECO)
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

package ch.kostceco.tools.kostlist;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.util.Util;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ListGuiController
{
	@FXML
	private Button		buttonTestfilesFolder, buttonDel, buttonSkrip,
			buttonChart, buttonNext, buttonFile, buttonFolder, buttonLicence,
			buttonOriginalSourceLicense, buttonDescription,
			buttonModificationdescription;
	// private Button buttonHelp, buttonSave,
	// buttonPrint, buttonFinish;

	/* @FXML private Button buttonChoice1, buttonChoice2, buttonExcerpt; */

	/*
	 * ObservableList<String> langList = FXCollections.observableArrayList(
	 * "Deutsch", "Fran�ais", "English" );
	 * 
	 * @FXML private ChoiceBox<String> lang;
	 */

	@FXML
	private Label		labelTestfilesFolder, labelChoiceTestfilesFolder,
			labelFileFolder, labelChoiceFileFolder, labelMd5sum, labelMd5,
			label, labelOriginalSourceLicense, labelDescription,
			labelModificationdescription, labelExiftool, labelKOSTVal,
			labelJHOVE, labelTxtExiftool, labelTxtKOSTVal, labelTxtJHOVE;

	@FXML
	private TextArea	console;

	private PrintStream	ps;

	@FXML
	private WebView		wbv;

	private WebEngine	engine;

	private File		testfilesFolder, fileTodo, fileFolderTodo;

	/*
	 * private File configFile = new File( kostRanFolder.getAbsolutePath() +
	 * File.separator + "configuration" + File.separator +
	 * "SIARDexcerpt.conf.xml" );
	 */

	// private String dirOfJarPath;

	private String		versionList	= "0.0.1";
	/*
	 * TODO: version auch hier anpassen:
	 * 
	 * 2) Start-Bild (make_exe)
	 * 
	 * 3) launch_KOST-Ran_exe.xml --> VersionInfo
	 */

	@FXML
	private ScrollPane	scroll;

	@FXML
	void initialize()
	{
		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		label.setText( "Copyright � KOST/CECO          KOST-List v"
				+ versionList + "          JavaFX " + javafxVersion
				+ "   &   Java-" + java6432 + " " + javaVersion + "." );

		// PrintStream in Konsole umleiten
		ps = new PrintStream( new Console( console ) );
		System.setOut( ps );
		System.setErr( ps );

		// festhalten von wo die Applikation (exe) gestartet wurde
		/*
		 * dirOfJarPath = ""; try { /* dirOfJarPath damit auch absolute Pfade
		 * kein Problem sind Dies ist ein generelles TODO in allen Modulen.
		 * Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
		 * dirOfJarPath + File.separator + erweitern.
		 */
		/*
		 * String path = new File( "" ).getAbsolutePath(); String
		 * locationOfJarPath = path; dirOfJarPath = locationOfJarPath; if (
		 * locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith(
		 * ".exe" ) || locationOfJarPath.endsWith( "." ) ) { File file = new
		 * File( locationOfJarPath ); dirOfJarPath = file.getParent(); } } catch
		 * ( Exception e1 ) { e1.printStackTrace(); }
		 */

		labelTestfilesFolder.setText( "Verzeichnis der Testdaten" );
		labelChoiceTestfilesFolder.setText( "(Pfad Testdaten)" );
		labelFileFolder.setText( "W�hle" );
		labelChoiceFileFolder.setText( "(Pfad Datei / Ordner)" );
		labelMd5.setText( "MD5" );
		labelMd5sum.setText( "" );
		labelOriginalSourceLicense.setText( "Original-Source, License" );

		labelDescription.setText( "Description" );
		labelModificationdescription.setText( "Modificationdescription" );
		labelExiftool.setText( "Exiftool" );
		labelKOSTVal.setText( "KOST-Val" );
		labelJHOVE.setText( "JHOVE" );
		labelTxtExiftool.setText( "" );
		labelTxtKOSTVal.setText( "" );
		labelTxtJHOVE.setText( "" );

		buttonTestfilesFolder.setText( "..." );
		buttonFolder.setText( "Ordner" );
		buttonFile.setText( "Datei" );
		buttonLicence.setText( "Lizenz-Informationen" );
		buttonDel.setText( "l�schen" );
		buttonSkrip.setText( "�berspringen" );
		buttonChart.setText( "beschreiben" );
		buttonNext.setText( "weiter" );
		buttonOriginalSourceLicense.setText( "(Bitte eingeben)" );
		buttonDescription.setText( "(Bitte eingeben)" );
		buttonModificationdescription.setText( "(Bitte eingeben)" );

		engine = wbv.getEngine();

		// Inaktiv bei Initialisierung
		buttonDel.setDisable( true );
		buttonSkrip.setDisable( true );
		buttonChart.setDisable( true );
		buttonNext.setDisable( true );
		buttonFile.setDisable( true );
		buttonFolder.setDisable( true );
		buttonOriginalSourceLicense.setDisable( true );
		buttonDescription.setDisable( true );
		buttonModificationdescription.setDisable( true );

		// unsichtbar bei Initialisierung
		buttonDel.setVisible( false );
		buttonSkrip.setVisible( false );
		buttonChart.setVisible( false );
		buttonNext.setVisible( false );
		buttonFile.setVisible( false );
		buttonFolder.setVisible( false );
		buttonOriginalSourceLicense.setVisible( false );
		buttonDescription.setVisible( false );
		buttonModificationdescription.setVisible( false );
		labelFileFolder.setVisible( false );
		labelChoiceFileFolder.setVisible( false );
		labelMd5.setVisible( false );
		labelMd5sum.setVisible( false );
		labelOriginalSourceLicense.setVisible( false );
		labelDescription.setVisible( false );
		labelModificationdescription.setVisible( false );
		labelExiftool.setVisible( false );
		labelKOSTVal.setVisible( false );
		labelJHOVE.setVisible( false );
		labelTxtExiftool.setVisible( false );
		labelTxtKOSTVal.setVisible( false );
		labelTxtJHOVE.setVisible( false );

		/* Kurzanleitung zum GUI anzeigen */
		String help1, help2, help3, help4, help5;
		help1 = "<h2>Kurzanleitung</h2>";
		help2 = "<hr>";
		help3 = "<h4>1) Datei / Ordner ausw�hlen</h4>";
		help4 = "<h4>2) md5sum angeben</h4>";
		help5 = "<h4>3) Metadata erstellen</h4>";
		String initInstructions = "<html>" + help1 + help2 + help3 + help4
				+ help5 + "<br/></html>";
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
	void showLicence( ActionEvent e )
	{
		console.setText( " \n" );
		String licence1;
		String licence2;
		String licence3;
		String licence4;
		// String licence5;
		String licence6;

		licence1 = "<h2>Dieses Programm kommt mit ABSOLUT KEINER GARANTIE.</h2>";
		licence2 = "<hr>";
		licence3 = "<h4>Es handelt sich um freie Software, und Sie d�rfen sie unter bestimmten Bedingungen gerne weitergeben;</h4>";
		licence4 = "- siehe GPL-3.0_COPYING.txt f�r Einzelheiten. ";
		// licence4 = "- siehe das Handbuch und GPL-3.0_COPYING.txt f�r
		// Einzelheiten. ";
		// licence5 = "- KOST-Ran verwendet unmodifizierte Komponenten anderer
		// Hersteller, indem diese
		// direkt in den Quellcode eingebettet werden.";
		licence6 = "- Benutzer werden gebeten, sich an die Lizenzbedingungen dieser Komponenten zu halten.";
		String text = "<html>" + licence1 + licence2 + licence3 + licence4
				+ "<br/>" + /* licence5 + "<br/>" + */ licence6 + "</html>";
		engine.loadContent( text );
	}

	/*
	 * TODO Erledigt-Marker: Wenn chooseTestfilesFolder betaetigt wird, kann ein
	 * Ordner ausgewaehlt werden
	 */
	@FXML
	void chooseTestfilesFolder( ActionEvent e )
	{
		console.setText( " \n" );
		/*
		 * if ( tempFile.exists() ) { Util.deleteDir( tempFile );
		 * tempFile.mkdirs(); }
		 */
		// bestehender Ordner auswaehlen
		DirectoryChooser folderChooser = new DirectoryChooser();
		if ( !labelChoiceTestfilesFolder.getText().isEmpty() ) {
			// setInitialDirectory mit vorgaeniger Auswahl (Ordner)
			File dirFileFolder = new File(
					labelChoiceTestfilesFolder.getText() );
			if ( dirFileFolder.isFile() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( dirFileFolder.isDirectory() ) {
				folderChooser.setInitialDirectory( dirFileFolder );
			}
		}
		labelChoiceTestfilesFolder.setText( "" );
		// Set title
		folderChooser.setTitle( "W�hlen Sie den Ordner mit den Testdaten" );
		testfilesFolder = folderChooser.showDialog( new Stage() );
		labelChoiceTestfilesFolder.setText( testfilesFolder.getAbsolutePath() );

		chooseFileTodo( testfilesFolder );
	}

	/*
	 * TODO Erledigt-Marker: chooseFileTodo definiert die naechste zu
	 * beschreibende Datei
	 */
	void chooseFileTodo( File testfilesFolder )
	{
		console.setText( " \n" );
		buttonOriginalSourceLicense.setText( "(Bitte eingeben)" );
		buttonDescription.setText( "(Bitte eingeben)" );
		buttonModificationdescription.setText( "(Bitte eingeben)" );

		// (In)aktiv bei der Auswahl
		buttonDel.setDisable( false );
		buttonSkrip.setDisable( false );
		buttonChart.setDisable( false );
		buttonFile.setDisable( true );
		buttonFolder.setDisable( true );
		buttonOriginalSourceLicense.setDisable( true );
		buttonDescription.setDisable( true );
		buttonModificationdescription.setDisable( true );
		buttonNext.setDisable( true );

		// (un)sichtbar bei der Auswahl
		buttonDel.setVisible( true );
		buttonSkrip.setVisible( true );
		buttonChart.setVisible( true );
		buttonNext.setVisible( false );
		buttonFile.setVisible( false );
		buttonFolder.setVisible( false );
		buttonOriginalSourceLicense.setVisible( false );
		buttonDescription.setVisible( false );
		buttonModificationdescription.setVisible( false );
		labelFileFolder.setVisible( false );
		labelChoiceFileFolder.setVisible( false );
		labelMd5.setVisible( false );
		labelMd5sum.setVisible( false );
		labelOriginalSourceLicense.setVisible( false );
		labelDescription.setVisible( false );
		labelModificationdescription.setVisible( false );
		labelExiftool.setVisible( false );
		labelKOSTVal.setVisible( false );
		labelJHOVE.setVisible( false );
		labelTxtExiftool.setVisible( false );
		labelTxtKOSTVal.setVisible( false );
		labelTxtJHOVE.setVisible( false );

		try {
			Map<String, File> fileUnsortedMap = Util
					.getFileMapFile( testfilesFolder );
			Map<String, File> fileMap = new TreeMap<String, File>(
					fileUnsortedMap );
			Set<String> fileMapKeys = fileMap.keySet();
			Iterator<String> iter = fileMapKeys.iterator();
			boolean oneFileTodo = false;
			while ( iter.hasNext() ) {
				String s = iter.next();
				String entryName = s;
				File newFile = fileMap.get( entryName );
				File fileTodoXml;
				File fileTodoTmp;
				fileTodo = newFile;
				fileTodoXml = new File(
						fileTodo.getParentFile().getAbsolutePath()
								+ File.separator + "_" + fileTodo.getName()
								+ "_.xml" );
				fileTodoTmp = new File(
						fileTodo.getParentFile().getAbsolutePath()
								+ File.separator + "_" + fileTodo.getName()
								+ "_.tmp" );
				String fileTodoName = fileTodo.getName();
				if ( !newFile.isDirectory() ) {
					// console.setText( fileTodoName+" \n" );
					String fileTodoExt = "." + FilenameUtils
							.getExtension( fileTodoName ).toLowerCase();
					if ( fileTodo != null ) {
						if ( !fileTodoXml.exists() && !fileTodoTmp.exists() ) {
							if ( fileTodoExt.equals( ".jpeg" )
									|| fileTodoExt.equals( ".jpg" )
									|| fileTodoExt.equals( ".png" ) ) {
								// Anzeige in WebView wenn image
								String pathDetail = "file:/"
										+ fileTodo.getAbsolutePath();
								pathDetail = pathDetail.replace( "\\\\", "/" );
								pathDetail = pathDetail.replace( "\\", "/" );
								String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h4>1.<br>&nbsp;</h4></td><td><h4>Aktuelle Datei: <br>"
										+ fileTodo.getAbsolutePath()
										+ "</h4></td></tr>";
								String sel2 = "<tr><td><h4>2.</h4></td><td><h4>Datei l�schen, �berspringen oder beschreiben</h4></td></tr>";
								String selDetail = "<br/>";
								selDetail = "<table  width=\"100%\"><tr><td width=\"30px\"></td><td><img  src='"
										+ pathDetail
										+ "' width=\"100%\" style=\"border:1px solid gray\" ></td><td width=\"30px\"></td></tr></table>";
								String text = "<html><body>" + sel1 + sel2
										+ selDetail + "</body></html>";
								engine.loadContent( text );
								oneFileTodo = true;
								break;
							} else if ( fileTodoExt.equals( ".jp2" ) ) {
							} else if ( fileTodoExt.equals( ".tiff" )
									|| fileTodoExt.equals( "tif" ) ) {
								// Runtime.getRuntime().exec( "rundll32
								// url.dll,FileProtocolHandler " +
								// fileTodo.getAbsolutePath() );
							} else if ( fileTodoExt.equals( ".siard" ) ) {
							} else if ( fileTodoExt.equals( ".pdf" )
									|| fileTodoExt.equals( ".pdfa" ) ) {
								// Anzeige in eigenem Fenster, da nicht via
								// WebView moeglich
								String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h4>1.<br>&nbsp;</h4></td><td><h4>Aktuelle Datei: <br>"
										+ fileTodo.getAbsolutePath()
										+ "</h4></td></tr>";
								String sel2 = "<tr><td><h4>2.</h4></td><td><h4>Datei l�schen, �berspringen oder beschreiben</h4></td></tr>";
								String selDetail = "<br/>";
								selDetail = "<table  width=\"100%\"><tr><td width=\"30px\"></td><td><h4>(Datei wurde in seperatem Fenster ge�ffnet)</h4></td><td width=\"30px\"></td></tr></table>";
								String text = "<html><body>" + sel1 + sel2
										+ selDetail + "</body></html>";
								engine.loadContent( text );
								Runtime.getRuntime().exec(
										"rundll32 url.dll,FileProtocolHandler "
												+ fileTodo.getAbsolutePath() );
								oneFileTodo = true;
								break;
							} else {
								// weitere Formate spaeter hinzufuegen
							}
						} else {
							String sel1 = "<table  width=\"100%\"><tr><td><h4>Jede Datei wurde beschrieben oder geloescht.</h4></td></tr>";
							String text = "<html><body>" + sel1
									+ "</body></html>";
							engine.loadContent( text );
							oneFileTodo = false;
							buttonDel.setDisable( true );
							buttonSkrip.setDisable( true );
							buttonChart.setDisable( true );
							buttonNext.setDisable( true );

							// unsichtbar bei Initialisierung
							buttonDel.setVisible( false );
							buttonSkrip.setVisible( false );
							buttonChart.setVisible( false );
							buttonNext.setVisible( false );
						}
					}
				}
			}
			if ( oneFileTodo ) {
				// Inaktiv bei Initialisierung
				buttonDel.setDisable( false );
				buttonSkrip.setDisable( false );
				buttonChart.setDisable( false );
				buttonNext.setDisable( true );

				// unsichtbar bei Initialisierung
				buttonDel.setVisible( true );
				buttonSkrip.setVisible( true );
				buttonChart.setVisible( true );
				buttonNext.setVisible( false );
			} else {
				// jetzt werden die zuvor ueberprungenen angegangen
				// (In)aktiv bei der Auswahl
				buttonSkrip.setDisable( true );
				// (un)sichtbar bei der Auswahl
				buttonSkrip.setVisible( false );

				Iterator<String> iterTmp = fileMapKeys.iterator();
				while ( iterTmp.hasNext() ) {
					String s = iterTmp.next();
					String entryName = s;
					File newFile = fileMap.get( entryName );
					File fileTodoXml;
					File fileTodoTmp;
					fileTodo = newFile;
					fileTodoXml = new File(
							fileTodo.getParentFile().getAbsolutePath()
									+ File.separator + "_" + fileTodo.getName()
									+ "_.xml" );
					fileTodoTmp = new File(
							fileTodo.getParentFile().getAbsolutePath()
									+ File.separator + "_" + fileTodo.getName()
									+ "_.tmp" );
					if ( !newFile.isDirectory() ) {
						String fileTodoName = fileTodo.getName();
						// console.setText( fileTodoName+" \n" );
						String fileTodoExt = "." + FilenameUtils
								.getExtension( fileTodoName ).toLowerCase();
						if ( fileTodo != null ) {
							if ( !fileTodoXml.exists()
									&& fileTodoTmp.exists() ) {
								if ( fileTodoExt.equals( ".jpeg" )
										|| fileTodoExt.equals( ".jpg" )
										|| fileTodoExt.equals( ".png" ) ) {
									// Anzeige in WebView wenn image
									String pathDetail = "file:/"
											+ fileTodo.getAbsolutePath();
									pathDetail = pathDetail.replace( "\\\\",
											"/" );
									pathDetail = pathDetail.replace( "\\",
											"/" );
									String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h4>1.<br>&nbsp;</h4></td><td><h4>Aktuelle Datei: <br>"
											+ fileTodo.getAbsolutePath()
											+ "</h4></td></tr>";
									String sel2 = "<tr><td><h4>2.</h4></td><td><h4>Datei l�schen oder beschreiben</h4></td></tr>";
									String selDetail = "<br/>";
									selDetail = "<table  width=\"100%\"><tr><td width=\"30px\"></td><td><img  src='"
											+ pathDetail
											+ "' width=\"100%\" style=\"border:1px solid gray\" ></td><td width=\"30px\"></td></tr></table>";
									String text = "<html><body>" + sel1 + sel2
											+ selDetail + "</body></html>";
									engine.loadContent( text );
									oneFileTodo = true;
									break;
								} else if ( fileTodoExt.equals( ".jp2" ) ) {
								} else if ( fileTodoExt.equals( ".tiff" )
										|| fileTodoExt.equals( "tif" ) ) {
									// Runtime.getRuntime().exec( "rundll32
									// url.dll,FileProtocolHandler " +
									// fileTodo.getAbsolutePath() );
								} else if ( fileTodoExt.equals( ".siard" ) ) {
								} else if ( fileTodoExt.equals( ".pdf" )
										|| fileTodoExt.equals( ".pdfa" ) ) {
									// Anzeige in eigenem Fenster, da nicht via
									// WebView moeglich
									String sel1 = "<table  width=\"100%\"><tr><td width=\"30px\"><h4>1.<br>&nbsp;</h4></td><td><h4>Aktuelle Datei: <br>"
											+ fileTodo.getAbsolutePath()
											+ "</h4></td></tr>";
									String sel2 = "<tr><td><h4>2.</h4></td><td><h4>Datei l�schen, �berspringen oder beschreiben</h4></td></tr>";
									String selDetail = "<br/>";
									selDetail = "<table  width=\"100%\"><tr><td width=\"30px\"></td><td><h4>(Datei wurde in seperatem Fenster ge�ffnet)</h4></td><td width=\"30px\"></td></tr></table>";
									String text = "<html><body>" + sel1 + sel2
											+ selDetail + "</body></html>";
									engine.loadContent( text );
									Runtime.getRuntime().exec(
											"rundll32 url.dll,FileProtocolHandler "
													+ fileTodo
															.getAbsolutePath() );
									oneFileTodo = true;
									break;
								} else {
									// weitere Formate spaeter hinzufuegen
								}
							} else {
								String sel1 = "<table  width=\"100%\"><tr><td><h4>Jede Datei wurde beschrieben oder geloescht.</h4></td></tr>";
								String text = "<html><body>" + sel1
										+ "</body></html>";
								engine.loadContent( text );
								oneFileTodo = false;
								buttonDel.setDisable( true );
								buttonSkrip.setDisable( true );
								buttonChart.setDisable( true );
								buttonNext.setDisable( true );

								// unsichtbar bei Initialisierung
								buttonDel.setVisible( false );
								buttonSkrip.setVisible( false );
								buttonChart.setVisible( false );
								buttonNext.setVisible( false );
							}
						} else {
							oneFileTodo = false;
						}
					}
				}
			}
			if ( oneFileTodo ) {
				// Inaktiv bei Initialisierung
				buttonDel.setDisable( false );
				buttonChart.setDisable( false );
				buttonNext.setDisable( true );

				// unsichtbar bei Initialisierung
				buttonDel.setVisible( true );
				buttonChart.setVisible( true );
				buttonNext.setVisible( false );
			} else {
				String sel1 = "<table  width=\"100%\"><tr><td><h4>Jede Datei wurde beschrieben oder geloescht.</h4></td></tr>";
				String text = "<html><body>" + sel1 + "</body></html>";
				engine.loadContent( text );
				oneFileTodo = false;
				buttonDel.setDisable( true );
				buttonSkrip.setDisable( true );
				buttonChart.setDisable( true );
				buttonNext.setDisable( true );

				// unsichtbar bei Initialisierung
				buttonDel.setVisible( false );
				buttonSkrip.setVisible( false );
				buttonChart.setVisible( false );
				buttonNext.setVisible( false );
			}
		} catch ( Exception etff ) {
			System.out.println( "Exception: " + etff.getMessage() );
		} catch ( StackOverflowError eso ) {
			System.out.println(
					"Exception: " + "StackOverflowError " + eso.getMessage() );
		} catch ( OutOfMemoryError eoom ) {
			System.out.println(
					"Exception: " + "OutOfMemoryError " + eoom.getMessage() );
		}

	}

	/*
	 * TODO Erledigt-Marker: Wenn doChart betaetigt wird, kann es beschrieben
	 * werden
	 */
	@FXML
	void doChart( ActionEvent e )
	{
		// (In)aktiv bei der Beschreibung
		// setDisable( true ) = inaktiv
		// setDisable( false ) = aktiv, kann ausgewaehlt werden
		buttonDel.setDisable( true );
		buttonSkrip.setDisable( true );
		buttonChart.setDisable( true );
		buttonFile.setDisable( true );
		buttonFolder.setDisable( true );
		buttonOriginalSourceLicense.setDisable( false );
		buttonDescription.setDisable( false );
		buttonModificationdescription.setDisable( false );
		buttonNext.setDisable( false );

		// (un)sichtbar bei der Beschreibung
		// setVisible( false ) = unsichtbar
		// setVisible( true ) = sichtbar, wird angezeigt
		buttonDel.setVisible( false );
		buttonSkrip.setVisible( false );
		buttonChart.setVisible( false );
		buttonNext.setVisible( true );
		buttonFile.setVisible( false );
		buttonFolder.setVisible( false );
		buttonOriginalSourceLicense.setVisible( true );
		buttonDescription.setVisible( true );
		buttonModificationdescription.setVisible( true );
		labelFileFolder.setVisible( false );
		labelChoiceFileFolder.setVisible( false );
		labelMd5.setVisible( true );
		labelMd5sum.setVisible( true );
		labelOriginalSourceLicense.setVisible( true );
		labelDescription.setVisible( true );
		labelModificationdescription.setVisible( true );
		labelExiftool.setVisible( true );
		labelKOSTVal.setVisible( true );
		labelJHOVE.setVisible( true );
		labelTxtExiftool.setVisible( true );
		labelTxtKOSTVal.setVisible( true );
		labelTxtJHOVE.setVisible( true );

		/*
		 * <?xml version="1.0" encoding="ISO-8859-1"?> <KOST-List>
		 * <Name>TIFF_valid_gpl2.tif</Name>
		 * <MD5>6e945e26be4019fc5ab472066a00c2bc</MD5>
		 * <OriginalSourceLicense></OriginalSourceLicense>
		 * <Description></Description>
		 * <Modificationdescription></Modificationdescription>
		 * <Exiftool></Exiftool> <KOST-Val></KOST-Val> <JHOVE></JHOVE>
		 * </KOST-List>
		 */
		console.setText( " \n" );
		console.appendText( fileTodo.getAbsolutePath() );
		// bestehende Datei auswaehlen
		try {
			String fileName = fileTodo.getName();
			File fileTodoTmp = new File(
					fileTodo.getParentFile().getAbsolutePath() + File.separator
							+ "_" + fileTodo.getName() + "_.tmp" );
			if ( fileTodoTmp.exists() ) {
				Util.deleteFile( fileTodoTmp );
			}
			File fileTodoImport = new File(
					fileTodo.getParentFile().getAbsolutePath() + File.separator
							+ "_" + fileTodo.getName() + "_.import" );
			File outputFileTodo = new File(
					fileTodo.getParentFile().getAbsolutePath() + File.separator
							+ "_" + fileTodo.getName() + "_.xml" );
			if ( !outputFileTodo.exists() ) {
				outputFileTodo.createNewFile();
				try (FileWriter fw = new FileWriter( outputFileTodo, true );
						BufferedWriter bw = new BufferedWriter( fw );
						PrintWriter out = new PrintWriter( bw )) {
					/*
					 * <?xml version="1.0" encoding="ISO-8859-1"?> <KOST-List>
					 * <Name>TIFF_valid_gpl2.tif</Name>
					 * <MD5>6e945e26be4019fc5ab472066a00c2bc</MD5>
					 * <OriginalSourceLicense></OriginalSourceLicense>
					 * <Description></Description>
					 * <Modificationdescription></Modificationdescription>
					 * <Exiftool></Exiftool> <KOST-Val></KOST-Val>
					 * <JHOVE></JHOVE> </KOST-List>
					 */
					out.println(
							"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
					out.println( "<KOST-List>" );
					out.println( "<Name>" + fileName + "</Name>" );

					// <MD5>6e945e26be4019fc5ab472066a00c2bc</MD5>
					try {
						String filepath = fileTodo.getAbsolutePath();
						MessageDigest messageDigest = MessageDigest
								.getInstance( "MD5" );

						FileInputStream fileInput = new FileInputStream(
								filepath );
						byte[] dataBytes = new byte[1024];
						int bytesRead = 0;
						while ( (bytesRead = fileInput
								.read( dataBytes )) != -1 ) {
							messageDigest.update( dataBytes, 0, bytesRead );
						}
						byte[] digestBytes = messageDigest.digest();
						StringBuffer sb = new StringBuffer( "" );
						for ( int i = 0; i < digestBytes.length; i++ ) {
							sb.append( Integer
									.toString( (digestBytes[i] & 0xff) + 0x100,
											16 )
									.substring( 1 ) );
						}
						String md5 = sb.toString();
						while ( md5.length() < 32 ) {
							md5 = "0" + md5;
						}
						out.println( "<MD5>" + md5 + "</MD5>" );
						fileInput.close();
					} catch ( IOException | NoSuchAlgorithmException eMd5 ) {
						System.out.println( "catch MD5: " + eMd5 );
						eMd5.printStackTrace();
					}
					/*
					 * <OriginalSourceLicense></OriginalSourceLicense>
					 * <Description></Description>
					 * <Modificationdescription></Modificationdescription>
					 * <Exiftool></Exiftool> <KOST-Val></KOST-Val>
					 * <JHOVE></JHOVE> </KOST-List>
					 */
					if ( fileTodoImport.exists() ) {
						Scanner scanner = new Scanner( fileTodoImport );
						while ( scanner.hasNextLine() ) {
							String line = scanner.nextLine();
							if ( line.contains( "<OriginalSourceLicense>" ) ) {
								out.println( line );
								String folderOriginalSourceLicense = line
										.replaceFirst(
												"<OriginalSourceLicense>", "" );
								folderOriginalSourceLicense = folderOriginalSourceLicense
										.replaceFirst(
												"</OriginalSourceLicense>",
												"" );
								buttonOriginalSourceLicense
										.setText( folderOriginalSourceLicense );
							}
							if ( line.contains( "<Description>" ) ) {
								out.println( line );
								String folderOriginalSourceLicense = line
										.replaceFirst( "<Description>", "" );
								folderOriginalSourceLicense = folderOriginalSourceLicense
										.replaceFirst( "</Description>", "" );
								buttonDescription
										.setText( folderOriginalSourceLicense );
							}
							if ( line
									.contains( "<Modificationdescription>" ) ) {
								out.println( line );
								String folderOriginalSourceLicense = line
										.replaceFirst(
												"<Modificationdescription>",
												"" );
								folderOriginalSourceLicense = folderOriginalSourceLicense
										.replaceFirst(
												"</Modificationdescription>",
												"" );
								buttonModificationdescription
										.setText( folderOriginalSourceLicense );
							}
						}
						scanner.close();
						Util.deleteFile( fileTodoImport );
					} else {
						out.println(
								"<OriginalSourceLicense></OriginalSourceLicense>" );
						out.println( "<Description></Description>" );
						out.println(
								"<Modificationdescription></Modificationdescription>" );
					}
					out.println( "<Exiftool></Exiftool>" );
					out.println( "<KOST-Val></KOST-Val>" );
					out.println( "<JHOVE></JHOVE>" );
					out.println( "</KOST-List>" );

					out.close();
					bw.close();
					fw.close();
				} catch ( IOException e3 ) {
					System.out.println( "catch: " + e3 );
					e3.printStackTrace();
				}
			}
			// Werte auslesen und anzeigen
			// inaktiv oder nicht
			buttonChart.setDisable( true );
			buttonNext.setDisable( false );
			buttonOriginalSourceLicense.setDisable( false );
			buttonDescription.setDisable( false );
			buttonModificationdescription.setDisable( false );

			// sichtbar oder nicht
			buttonChart.setVisible( false );
			buttonNext.setVisible( true );
			buttonOriginalSourceLicense.setVisible( true );
			buttonDescription.setVisible( true );
			buttonModificationdescription.setVisible( true );
			labelMd5.setVisible( true );
			labelMd5sum.setVisible( true );
			labelOriginalSourceLicense.setVisible( true );
			labelDescription.setVisible( true );
			labelModificationdescription.setVisible( true );
			labelExiftool.setVisible( true );
			labelKOSTVal.setVisible( true );
			labelJHOVE.setVisible( true );
			labelTxtExiftool.setVisible( true );
			labelTxtKOSTVal.setVisible( true );
			labelTxtJHOVE.setVisible( true );

			try {
				Document doc = null;
				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( outputFileTodo ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db;
				db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();
				labelMd5sum.setText( doc.getElementsByTagName( "MD5" ).item( 0 )
						.getTextContent() );
			} catch ( ParserConfigurationException | SAXException eDoc ) {
				eDoc.printStackTrace();
			}

		} catch ( TransformerFactoryConfigurationError | IOException e1 ) {
			e1.printStackTrace();
		}
	}

	/*
	 * TODO Erledigt-Marker: Wenn Aenderungen an txtOriginalSourceLicense
	 * gemacht wird, wird es ausgeloest
	 */
	@FXML
	void txtOriginalSourceLicense( ActionEvent event )
	{
		console.setText( " \n" );
		console.appendText( "OriginalSourceLicense" );

		String stringButton = buttonOriginalSourceLicense.getText();
		// create a TextInputDialog mit der Texteingabe der Laenge
		TextInputDialog dialog = new TextInputDialog( stringButton );

		// Set title & header text
		dialog.setTitle( "KOST-List - OriginalSourceLicense" );
		String headerDeFrEn = "Geben sie ggf. die �nderung ein:";
		dialog.setHeaderText( headerDeFrEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		File outputFileTodo = new File(
				fileTodo.getParentFile().getAbsolutePath() + File.separator
						+ "_" + fileTodo.getName() + "_.xml" );
		String stringNew = "";
		String stringXml = "";

		if ( result.isPresent() ) {
			try {
				Scanner scanner;
				scanner = new Scanner( outputFileTodo );
				while ( scanner.hasNextLine() ) {
					String line = scanner.nextLine();
					if ( line.contains( "<OriginalSourceLicense>" ) ) {
						stringXml = line;
						break;
					}
				}
				scanner.close();
				stringNew = result.get();
				stringButton = stringNew;
				buttonOriginalSourceLicense.setText( stringButton );
				String xmlNew = "<OriginalSourceLicense>" + stringNew
						+ "</OriginalSourceLicense>";
				Util.oldnewstring( stringXml, xmlNew, outputFileTodo );
			} catch ( IOException eInt ) {
				String message = eInt.getMessage();
				console.appendText( message );
			}
		} else {
			// Keine Aktion
		}
	}

	/*
	 * TODO Erledigt-Marker: Wenn Aenderungen an txtDescription gemacht wird,
	 * wird es ausgeloest
	 */
	@FXML
	void txtDescription( ActionEvent event )
	{
		console.setText( " \n" );
		console.appendText( "Description" );

		String stringButton = buttonDescription.getText();
		// create a TextInputDialog mit der Texteingabe der Laenge
		TextInputDialog dialog = new TextInputDialog( stringButton );

		// Set title & header text
		dialog.setTitle( "KOST-List - Description" );
		String headerDeFrEn = "Geben sie ggf. die �nderung ein:";
		dialog.setHeaderText( headerDeFrEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		File outputFileTodo = new File(
				fileTodo.getParentFile().getAbsolutePath() + File.separator
						+ "_" + fileTodo.getName() + "_.xml" );
		String stringNew = "";
		String stringXml = "";

		if ( result.isPresent() ) {
			try {
				Scanner scanner;
				scanner = new Scanner( outputFileTodo );
				while ( scanner.hasNextLine() ) {
					String line = scanner.nextLine();
					if ( line.contains( "<Description>" ) ) {
						stringXml = line;
						break;
					}
				}
				scanner.close();
				stringNew = result.get();
				stringButton = stringNew;
				buttonDescription.setText( stringButton );
				String xmlNew = "<Description>" + stringNew + "</Description>";
				Util.oldnewstring( stringXml, xmlNew, outputFileTodo );
			} catch ( IOException eInt ) {
				String message = eInt.getMessage();
				console.appendText( message );
			}
		} else {
			// Keine Aktion
		}
	}

	/*
	 * TODO Erledigt-Marker: Wenn Aenderungen an txtModificationdescription
	 * gemacht wird, wird es ausgeloest
	 */
	@FXML
	void txtModificationdescription( ActionEvent event )
	{
		console.setText( " \n" );
		console.appendText( "Modificationdescription" );

		String stringButton = buttonModificationdescription.getText();
		// create a TextInputDialog mit der Texteingabe der Laenge
		TextInputDialog dialog = new TextInputDialog( stringButton );

		// Set title & header text
		dialog.setTitle( "KOST-List - Modificationdescription" );
		String headerDeFrEn = "Geben sie ggf. die �nderung ein:";
		dialog.setHeaderText( headerDeFrEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		File outputFileTodo = new File(
				fileTodo.getParentFile().getAbsolutePath() + File.separator
						+ "_" + fileTodo.getName() + "_.xml" );
		String stringNew = "";
		String stringXml = "";

		if ( result.isPresent() ) {
			try {
				Scanner scanner;
				scanner = new Scanner( outputFileTodo );
				while ( scanner.hasNextLine() ) {
					String line = scanner.nextLine();
					if ( line.contains( "<Modificationdescription>" ) ) {
						stringXml = line;
						break;
					}
				}
				scanner.close();
				stringNew = result.get();
				stringButton = stringNew;
				buttonModificationdescription.setText( stringButton );
				String xmlNew = "<Modificationdescription>" + stringNew
						+ "</Modificationdescription>";
				Util.oldnewstring( stringXml, xmlNew, outputFileTodo );
			} catch ( IOException eInt ) {
				String message = eInt.getMessage();
				console.appendText( message );
			}
		} else {
			// Keine Aktion
		}
	}

	/*
	 * TODO Erledigt-Marker: Wenn doNext betaetigt wird, kann ein Ordner
	 * ausgewaehlt werden
	 */
	@FXML
	void doNext( ActionEvent e )
	{
		chooseFileTodo( testfilesFolder );
	}

	/*
	 * TODO Erledigt-Marker: Wenn doSkrip betaetigt wird, kann ein Ordner
	 * ausgewaehlt werden
	 */
	@FXML
	void doSkrip( ActionEvent e )
	{
		File fileTodoTmp = new File( fileTodo.getParentFile().getAbsolutePath()
				+ File.separator + "_" + fileTodo.getName() + "_.tmp" );
		if ( !fileTodoTmp.exists() ) {
			try {
				fileTodoTmp.createNewFile();
			} catch ( IOException e1 ) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		chooseFileTodo( testfilesFolder );
	}

	/*
	 * TODO Erledigt-Marker: Wenn doDel betaetigt wird, kann ein Ordner
	 * ausgewaehlt werden
	 */
	@FXML
	void doDel( ActionEvent e )
	{
		File fileTodoTmp = new File( fileTodo.getParentFile().getAbsolutePath()
				+ File.separator + "_" + fileTodo.getName() + "_.tmp" );
		if ( fileTodoTmp.exists() ) {
			Util.deleteFile( fileTodoTmp );
		}
		File outputFileTodo = new File(
				fileTodo.getParentFile().getAbsolutePath() + File.separator
						+ "_" + fileTodo.getName() + "_.xml" );
		if ( outputFileTodo.exists() ) {
			Util.deleteFile( outputFileTodo );
		}
		if ( fileTodo.exists() ) {
			Util.deleteFile( fileTodo );
		}
		chooseFileTodo( testfilesFolder );
	}

	/*
	 * TODO Erledigt-Marker: Wenn choseFile betaetigt wird, kann eine Datei
	 * ausgewaehlt werden
	 */
	@FXML
	void chooseFile( ActionEvent e )
	{
		console.setText( " \n" );
		/*
		 * if ( tempFile.exists() ) { Util.deleteDir( tempFile );
		 * tempFile.mkdirs(); }
		 */
		// bestehende Datei auswaehlen
		FileChooser fileChooser = new FileChooser();
		if ( !labelChoiceFileFolder.getText().isEmpty() ) {
			// setInitialDirectory mit vorgaeniger Auswahl (Ordner)
			File dirFileFolder = new File( labelChoiceFileFolder.getText() );
			if ( dirFileFolder.isFile() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( dirFileFolder.exists() ) {
				fileChooser.setInitialDirectory( dirFileFolder );
			}
		}
		labelChoiceFileFolder.setText( "" );
		// Set extension filter
		/*
		 * FileChooser.ExtensionFilter extFilter = new
		 * FileChooser.ExtensionFilter( "SIARD files (*.siard)", "*.siard" );
		 * fileChooser.getExtensionFilters().add( extFilter );
		 */
		// Set title
		fileChooser.setTitle( "W�hlen Sie die Datei" );
		fileFolderTodo = fileChooser.showOpenDialog( new Stage() );
		labelChoiceFileFolder.setText( fileFolderTodo.getAbsolutePath() );
	}

	/*
	 * TODO Erledigt-Marker: Wenn choseFolder betaetigt wird, kann ein Ordner
	 * ausgewaehlt werden
	 */
	@FXML
	void chooseFolder( ActionEvent e )
	{
		console.setText( " \n" );
		/*
		 * if ( tempFile.exists() ) { Util.deleteDir( tempFile );
		 * tempFile.mkdirs(); }
		 */
		// bestehender Ordner auswaehlen
		DirectoryChooser folderChooser = new DirectoryChooser();
		if ( !labelChoiceFileFolder.getText().isEmpty() ) {
			// setInitialDirectory mit vorgaeniger Auswahl (Ordner)
			File dirFileFolder = new File( labelChoiceFileFolder.getText() );
			if ( dirFileFolder.isFile() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( !dirFileFolder.exists() ) {
				dirFileFolder = dirFileFolder.getParentFile();
			}
			if ( dirFileFolder.exists() ) {
				folderChooser.setInitialDirectory( dirFileFolder );
			}
		}
		labelChoiceFileFolder.setText( "" );
		// Set extension filter
		/*
		 * FileChooser.ExtensionFilter extFilter = new
		 * FileChooser.ExtensionFilter( "SIARD files (*.siard)", "*.siard" );
		 * fileChooser.getExtensionFilters().add( extFilter );
		 */
		// Set title
		folderChooser.setTitle( "W�hlen Sie den Ordner" );
		fileFolderTodo = folderChooser.showDialog( new Stage() );
		labelChoiceFileFolder.setText( fileFolderTodo.getAbsolutePath() );
	}

	/* TODO --> ChoiceBox ================= */

	// Mit changeLang wird die Sprache umgestellt
	/*
	 * @FXML void changeLang( ActionEvent event ) { console.setText( " \n" );
	 * String selLang = lang.getValue(); if ( selLang.equals( "Fran�ais" ) ) {
	 * locale = new Locale( "fr" ); lang.setValue( "Fran�ais" );
	 * labelSiard.setText( "Fichier SIARD" ); labelTable.setText(
	 * "Tableau principale" ); labelList.setText( "Donn�es de configuration" );
	 * buttonSiard.setText( "s�lectionnez" ); buttonTable.setText(
	 * "s�lectionnez" ); buttonList.setText( "s�lectionnez" );
	 * buttonHelp.setText( "Aide ?" ); buttonLicence.setText(
	 * "Informations sur la licence" ); buttonSave.setText( "sauvegarder" );
	 * buttonPrint.setText( "imprimer" ); buttonFinish.setText(
	 * "Effectuer l'�chantillonnage" ); } else if ( selLang.equals( "English" )
	 * ) { locale = new Locale( "en" ); lang.setValue( "English" );
	 * labelSiard.setText( "SIARD file" ); labelTable.setText( "Main table" );
	 * labelList.setText( "Configuration data" ); buttonSiard.setText( "Select"
	 * ); buttonTable.setText( "select" ); buttonList.setText( "select" );
	 * buttonHelp.setText( "Help ?" ); buttonLicence.setText(
	 * "License information" ); buttonSave.setText( "save" );
	 * buttonPrint.setText( "print" ); buttonFinish.setText( "Perform sampling"
	 * ); } else { locale = new Locale( "de" ); lang.setValue( "Deutsch" );
	 * labelSiard.setText( "SIARD-Datei" ); labelTable.setText( "Haupttabelle"
	 * ); labelList.setText( "Konfigurationsdaten" ); buttonSiard.setText(
	 * "Ausw�hlen" ); buttonTable.setText( "Ausw�hlen" ); buttonList.setText(
	 * "Ausw�hlen" ); buttonHelp.setText( "Hilfe ?" ); buttonLicence.setText(
	 * "Lizenz-Informationen" ); buttonSave.setText( "speichern" );
	 * buttonPrint.setText( "drucken" ); buttonFinish.setText(
	 * "Sampling durchf�hren" ); } }
	 */

}