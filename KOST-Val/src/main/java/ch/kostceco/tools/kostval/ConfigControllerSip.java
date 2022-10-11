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

public class ConfigControllerSip
{

	@FXML
	private Button		buttonConfigApply, buttonLength, buttonName;

	private File		configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String		dirOfJarPath, stringName, stringLength;

	private Locale		locale		= Locale.getDefault();

	@FXML
	private Label		labelVal, labelMessage, labelConfig;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText(
				"Copyright © KOST/CECO          KOST-Val v2.1.3.0          JavaFX "
						+ javafxVersion + "   &   Java-" + java6432 + " "
						+ javaVersion + "." );

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
			setLibraryPath( dirOfJarPath );
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		labelMessage.setText( "" );

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if ( Util.stringInFileLine( "kostval-conf-DE.xsl", configFile ) ) {
				labelVal.setText( "Validierungseinstellung: SIP" );
				buttonConfigApply.setText( "anwenden" );
				locale = new Locale( "de" );
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				labelVal.setText( "Paramètre de validation: SIP" );
				buttonConfigApply.setText( "appliquer" );
				locale = new Locale( "fr" );
			} else {
				labelVal.setText( "Validation setting: SIP" );
				buttonConfigApply.setText( "apply" );
				locale = new Locale( "en" );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String allowedlengthofpaths = doc
					.getElementsByTagName( "allowedlengthofpaths" ).item( 0 )
					.getTextContent();
			String allowedsipname = doc.getElementsByTagName( "allowedsipname" )
					.item( 0 ).getTextContent();
			buttonLength.setText( allowedlengthofpaths );
			buttonName.setText( allowedsipname );

		} catch ( IOException | ParserConfigurationException
				| SAXException e1 ) {
			e1.printStackTrace();
		}
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

	/* TODO --> Button ================= */

	@FXML
	void configApply( ActionEvent e )
	{
		// engine.loadContent( "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* Wenn Aenderungen an changeLength gemacht wird, wird es ausgeloest */
	@FXML
	void changeLength( ActionEvent event )
	{
		labelMessage.setText( "" );
		stringLength = buttonLength.getText();
		// create a TextInputDialog mit der Texteingabe der Laenge
		TextInputDialog dialog = new TextInputDialog( stringLength );

		// Set title & header text
		String lengthIntInit = stringLength;

		dialog.setTitle( "KOST-Val - Configuration - SIP" );
		String headerDeFrEn = "Geben sie die erlaubte maximale Anzahl Zeichen in Pfadlängen ein [179]:";
		if ( locale.toString().startsWith( "fr" ) ) {
			headerDeFrEn = "Entrez le nombre maximum de caractères autorisés dans la longueur du chemin [179]:";
		} else if ( locale.toString().startsWith( "en" ) ) {
			headerDeFrEn = "Enter the allowed maximum number of characters in path lengths [179]:";
		}
		dialog.setHeaderText( headerDeFrEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringLengthNew = "";
		if ( result.isPresent() ) {
			try {
				stringLengthNew = result.get();
				stringLength = stringLengthNew;
				buttonLength.setText( stringLength );

				String allowedlengthofpaths = "<allowedlengthofpaths>"
						+ lengthIntInit + "</allowedlengthofpaths>";
				String allowedlengthofpathsNew = "<allowedlengthofpaths>"
						+ stringLengthNew + "</allowedlengthofpaths>";
				Util.oldnewstring( allowedlengthofpaths,
						allowedlengthofpathsNew, configFile );
			} catch ( NumberFormatException | IOException eInt ) {
				String message = eInt.getMessage();
				labelMessage.setText( message );

			}
		} else {
			// Keine Aktion
		}
	}

	/* Wenn Aenderungen an changeName gemacht wird, wird es ausgeloest */
	@FXML
	void changeName( ActionEvent event )
	{
		labelMessage.setText( "" );
		stringName = buttonName.getText();
		// create a TextInputDialog mit der Texteingabe der Namen
		TextInputDialog dialog = new TextInputDialog( stringName );

		// Set title & header text
		String nameIntInit = stringName;

		dialog.setTitle( "KOST-Val - Configuration - SIP" );
		String headerDeFrEn = "Geben Sie die Vorgaben zum Aufbau des SIP-Namens ein [ SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3} ]:";
		if ( locale.toString().startsWith( "fr" ) ) {
			headerDeFrEn = "Entrez les valeurs par défaut pour construire le nom du SIP [ SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3} ] :";
		} else if ( locale.toString().startsWith( "en" ) ) {
			headerDeFrEn = "Enter the defaults to build the SIP name [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3} ]:";
		}
		dialog.setHeaderText( headerDeFrEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringNameNew = "";
		if ( result.isPresent() ) {
			try {
				stringNameNew = result.get();
				stringName = stringNameNew;
				buttonName.setText( stringName );
				String allowedsipname = "<allowedsipname>" + nameIntInit
						+ "</allowedsipname>";
				String allowedsipnameNew = "<allowedsipname>" + stringNameNew
						+ "</allowedsipname>";
				Util.oldnewstring( allowedsipname, allowedsipnameNew,
						configFile );

			} catch ( NumberFormatException | IOException eInt ) {
				String message = eInt.getMessage();
				labelMessage.setText( message );
			}
		} else {
			// Keine Aktion
		}
	}

}