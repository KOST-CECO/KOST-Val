/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
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

public class ConfigControllerMkv
{

	@FXML
	private CheckBox	checkFfv1, checkH264, checkH265, checkAv1, checkFlac,
			checkMp3, checkAac;

	@FXML
	private Button		buttonConfigApply;

	private File		configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String		dirOfJarPath, config,
			minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label		labelVideocodec, labelAudiocodec, labelMkv,
			labelMessage, labelConfig;

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
				labelVideocodec.setText( "Videocodec" );
				labelAudiocodec.setText( "Audiocodec" );
				labelMkv.setText( "Validierungseinstellung: MKV" );
				buttonConfigApply.setText( "anwenden" );
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				labelVideocodec.setText( "Codec vidéo" );
				labelAudiocodec.setText( "Codec audio" );
				labelMkv.setText( "Paramètre de validation: MKV" );
				buttonConfigApply.setText( "appliquer" );
				minOne = "Au moins une variante doit etre autorisee !";
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				labelVideocodec.setText( "Codec video" );
				labelAudiocodec.setText( "Codec audio" );
				labelMkv.setText( "Parametro di convalida: MKV" );
				buttonConfigApply.setText( "Applicare" );
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelVideocodec.setText( "Video codec" );
				labelAudiocodec.setText( "Audio codec" );
				labelMkv.setText( "Validation setting: MKV" );
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
			String noFfv1 = "<allowedmkvffv1></allowedmkvffv1>";
			String noH264 = "<allowedmkvh264></allowedmkvh264>";
			String noH265 = "<allowedmkvh265></allowedmkvh265>";
			String noAv1 = "<allowedmkvav1></allowedmkvav1>";
			String noFlac = "<allowedmkvflac></allowedmkvflac>";
			String noMp3 = "<allowedmkvmp3></allowedmkvmp3>";
			String noAac = "<allowedmkvaac></allowedmkvaac>";

			if ( config.contains( noFfv1 ) ) {
				checkFfv1.setSelected( false );
			}
			if ( config.contains( noH264 ) ) {
				checkH264.setSelected( false );
			}
			if ( config.contains( noH265 ) ) {
				checkH265.setSelected( false );
			}
			if ( config.contains( noAv1 ) ) {
				checkAv1.setSelected( false );
			}
			if ( config.contains( noFlac ) ) {
				checkFlac.setSelected( false );
			}
			if ( config.contains( noMp3 ) ) {
				checkMp3.setSelected( false );
			}
			if ( config.contains( noAac ) ) {
				checkAac.setSelected( false );
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
		// labelMessage.setText(minOne ); "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* TODO --> CheckBox ================= */

	/* checkComp schaltet diesen Codec in der Konfiguration ein oder aus */
	@FXML
	void changeFfv1( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvffv1>FFV1</allowedmkvffv1>";
		String no = "<allowedmkvffv1></allowedmkvffv1>";
		try {
			if ( checkFfv1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkH264.isSelected() && !checkH265.isSelected()
						&& !checkAv1.isSelected()  ) {
					labelMessage.setText( minOne );
					checkFfv1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeH264( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvh264>H.264 (AVC)</allowedmkvh264>";
		String no = "<allowedmkvh264></allowedmkvh264>";
		try {
			if ( checkH264.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkFfv1.isSelected() && !checkH265.isSelected()
						&& !checkAv1.isSelected()  ) {
					labelMessage.setText( minOne );
					checkH264.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeH265( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvh265>H.265 (HEVC)</allowedmkvh265>";
		String no = "<allowedmkvh265></allowedmkvh265>";
		try {
			if ( checkH265.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkH264.isSelected() && !checkFfv1.isSelected()
						&& !checkAv1.isSelected()  ) {
					labelMessage.setText( minOne );
					checkH265.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeAv1( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvav1>AV1</allowedmkvav1>";
		String no = "<allowedmkvav1></allowedmkvav1>";
		try {
			if ( checkAv1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkH264.isSelected() && !checkH265.isSelected()
						&& !checkFfv1.isSelected()  ) {
					labelMessage.setText( minOne );
					checkAv1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeFlac( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvflac>FLAC</allowedmkvflac>";
		String no = "<allowedmkvflac></allowedmkvflac>";
		try {
			if ( checkFlac.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkMp3.isSelected() && !checkAac.isSelected()  ) {
					labelMessage.setText( minOne );
					checkFlac.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMp3( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvmp3>MP3</allowedmkvmp3>";
		String no = "<allowedmkvmp3></allowedmkvmp3>";
		try {
			if ( checkMp3.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkFlac.isSelected() && !checkAac.isSelected()  ) {
					labelMessage.setText( minOne );
					checkMp3.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeAac( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmkvaac>AAC</allowedmkvaac>";
		String no = "<allowedmkvaac></allowedmkvaac>";
		try {
			if ( checkAac.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkMp3.isSelected() && !checkFlac.isSelected()  ) {
					labelMessage.setText( minOne );
					checkAac.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}