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

public class ConfigControllerSiard
{

	@FXML
	private CheckBox	checkSiard10, checkSiard21, checkSiard22;

	@FXML
	private Button		buttonConfigApply;

	private File		configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String		dirOfJarPath, config,
			minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label		labelVersion, labelVal, labelMessage, labelConfig;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText(
				"Copyright © KOST/CECO          KOST-Val v2.2.1.0          JavaFX "
						+ javafxVersion + "   &   Java-" + java6432 + " "
						+ javaVersion + "." );

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

		labelMessage.setText( "" );

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if ( Util.stringInFileLine( "kostval-conf-DE.xsl", configFile ) ) {
				labelVersion.setText( "Versionen" );
				labelVal.setText( "Validierungseinstellung: SIARD" );
				buttonConfigApply.setText( "anwenden" );
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				labelVersion.setText( "Versions" );
				labelVal.setText( "Paramètre de validation: SIARD" );
				buttonConfigApply.setText( "appliquer" );
				minOne = "Au moins une variante doit etre autorisee !";
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				labelVersion.setText( "Versioni" );
				labelVal.setText( "Parametro di convalida: SIARD" );
				buttonConfigApply.setText( "Applica" );
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelVersion.setText( "Versions" );
				labelVal.setText( "Validation setting: SIARD" );
				buttonConfigApply.setText( "apply" );
				minOne = "At least one variant must be allowed!";
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		try {
			byte[] encoded;
			encoded = Files
					.readAllBytes( Paths.get( configFile.getAbsolutePath() ) );
			config = new String( encoded, StandardCharsets.UTF_8 );
			String noSiard = "<siardvalidation>no</siardvalidation>";
			String noSiard10 = "<siard10></siard10>";
			String noSiard21 = "<siard21></siard21>";
			String noSiard22 = "<siard22></siard22>";

			if ( config.contains( noSiard ) ) {
				checkSiard10.setDisable( true );
				checkSiard21.setDisable( true );
				checkSiard22.setDisable( true );
			}
			if ( config.contains( noSiard10 ) ) {
				checkSiard10.setSelected( false );
			}
			if ( config.contains( noSiard21 ) ) {
				checkSiard21.setSelected( false );
			}
			if ( config.contains( noSiard22 ) ) {
				checkSiard22.setSelected( false );
			}
		} catch ( IOException e1 ) {
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

	/* TODO --> CheckBox ================= */

	/*
	 * checkSiard10 schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSiard10( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<siard10>1.0 </siard10>";
		String no = "<siard10></siard10>";
		try {
			if ( checkSiard10.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkSiard21.isSelected()
						&& !checkSiard22.isSelected() ) {
					labelMessage.setText( minOne );
					checkSiard10.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkSiard21 schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSiard21( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<siard21>2.1 </siard21>";
		String no = "<siard21></siard21>";
		try {
			if ( checkSiard21.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkSiard10.isSelected()
						&& !checkSiard22.isSelected() ) {
					labelMessage.setText( minOne );
					checkSiard21.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkSiard22 schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeSiard22( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<siard22>2.2 </siard22>";
		String no = "<siard22></siard22>";
		try {
			if ( checkSiard22.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkSiard10.isSelected()
						&& !checkSiard21.isSelected() ) {
					labelMessage.setText( minOne );
					checkSiard22.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}