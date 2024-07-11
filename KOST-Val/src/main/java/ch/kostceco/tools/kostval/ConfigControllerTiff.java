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

public class ConfigControllerTiff
{

	@FXML
	private CheckBox	checkComp1, checkComp5, checkPi0, checkPi4, checkComp2,
			checkComp7, checkPi1, checkPi5, checkBps1, checkBps8,
			checkMultipage, checkBps2, checkBps16, checkTiles, checkComp3,
			checkComp8, checkPi2, checkPi6, checkBps4, checkBps32, checkSize,
			checkComp4, checkComp32773, checkPi3, checkPi8;

	@FXML
	private Button		buttonConfigApply;

	private File		configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String		dirOfJarPath, config,
			minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label		labelBps, labelComp, labelOther, labelPi, labelImage,
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
				labelBps.setText( "Bits per Sample (pro Kanal)" );
				labelComp.setText( "Komprimierungsalgorithmus" );
				labelOther.setText( "Sonstiges" );
				labelPi.setText( "Farbraum" );
				labelImage.setText( "Validierungseinstellung: TIFF" );
				buttonConfigApply.setText( "anwenden" );
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				labelBps.setText( "Bits par échantillon (par canal)" );
				labelComp.setText( "Algorithme de compression" );
				labelOther.setText( "Divers" );
				labelPi.setText( "Espace couleur" );
				labelImage.setText( "Paramètre de validation: TIFF" );
				buttonConfigApply.setText( "appliquer" );
				minOne = "Au moins une variante doit etre autorisee !";
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				labelBps.setText( "Bit per campione (per canale)" );
				labelComp.setText( "Algoritmo di compressione" );
				labelOther.setText( "Diversi" );
				labelPi.setText( "Spazio colore" );
				labelImage.setText( "Parametro di convalida: TIFF" );
				buttonConfigApply.setText( "Applica" );
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelBps.setText( "Bits per sample (per channel)" );
				labelComp.setText( "Compression algorithm" );
				labelOther.setText( "Other" );
				labelPi.setText( "Color space" );
				labelImage.setText( "Validation setting: TIFF" );
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
			String noComp1 = "<allowedcompression1></allowedcompression1>";
			String noComp2 = "<allowedcompression2></allowedcompression2>";
			String noComp3 = "<allowedcompression3></allowedcompression3>";
			String noComp4 = "<allowedcompression4></allowedcompression4>";
			String noComp5 = "<allowedcompression5></allowedcompression5>";
			String noComp7 = "<allowedcompression7></allowedcompression7>";
			String noComp8 = "<allowedcompression8></allowedcompression8>";
			String noComp32773 = "<allowedcompression32773></allowedcompression32773>";
			String noPi0 = "<allowedphotointer0></allowedphotointer0>";
			String noPi1 = "<allowedphotointer1></allowedphotointer1>";
			String noPi2 = "<allowedphotointer2></allowedphotointer2>";
			String noPi3 = "<allowedphotointer3></allowedphotointer3>";
			String noPi4 = "<allowedphotointer4></allowedphotointer4>";
			String noPi5 = "<allowedphotointer5></allowedphotointer5>";
			String noPi6 = "<allowedphotointer6></allowedphotointer6>";
			String noPi8 = "<allowedphotointer8></allowedphotointer8>";
			String noBps1 = "<allowedbitspersample1></allowedbitspersample1>";
			String noBps2 = "<allowedbitspersample2></allowedbitspersample2>";
			String noBps4 = "<allowedbitspersample4></allowedbitspersample4>";
			String noBps8 = "<allowedbitspersample8></allowedbitspersample8>";
			String noBps16 = "<allowedbitspersample16></allowedbitspersample16>";
			String noBps32 = "<allowedbitspersample32></allowedbitspersample32>";
			String noMultipage = "<allowedmultipage>no</allowedmultipage>";
			String noTiles = "<allowedtiles>no</allowedtiles>";
			String noSize = "<allowedsize>no</allowedsize>";

			if ( config.contains( noComp1 ) ) {
				checkComp1.setSelected( false );
			}
			if ( config.contains( noComp2 ) ) {
				checkComp2.setSelected( false );
			}
			if ( config.contains( noComp3 ) ) {
				checkComp3.setSelected( false );
			}
			if ( config.contains( noComp4 ) ) {
				checkComp4.setSelected( false );
			}
			if ( config.contains( noComp5 ) ) {
				checkComp5.setSelected( false );
			}
			if ( config.contains( noComp7 ) ) {
				checkComp7.setSelected( false );
			}
			if ( config.contains( noComp8 ) ) {
				checkComp8.setSelected( false );
			}
			if ( config.contains( noComp32773 ) ) {
				checkComp32773.setSelected( false );
			}
			if ( config.contains( noPi0 ) ) {
				checkPi0.setSelected( false );
			}
			if ( config.contains( noPi1 ) ) {
				checkPi1.setSelected( false );
			}
			if ( config.contains( noPi2 ) ) {
				checkPi2.setSelected( false );
			}
			if ( config.contains( noPi3 ) ) {
				checkPi3.setSelected( false );
			}
			if ( config.contains( noPi4 ) ) {
				checkPi4.setSelected( false );
			}
			if ( config.contains( noPi5 ) ) {
				checkPi5.setSelected( false );
			}
			if ( config.contains( noPi6 ) ) {
				checkPi6.setSelected( false );
			}
			if ( config.contains( noPi8 ) ) {
				checkPi8.setSelected( false );
			}
			if ( config.contains( noBps1 ) ) {
				checkBps1.setSelected( false );
			}
			if ( config.contains( noBps2 ) ) {
				checkBps2.setSelected( false );
			}
			if ( config.contains( noBps4 ) ) {
				checkBps4.setSelected( false );
			}
			if ( config.contains( noBps8 ) ) {
				checkBps8.setSelected( false );
			}
			if ( config.contains( noBps16 ) ) {
				checkBps16.setSelected( false );
			}
			if ( config.contains( noBps32 ) ) {
				checkBps32.setSelected( false );
			}
			if ( config.contains( noMultipage ) ) {
				checkMultipage.setSelected( false );
			}
			if ( config.contains( noTiles ) ) {
				checkTiles.setSelected( false );
			}
			if ( config.contains( noSize ) ) {
				checkSize.setSelected( false );
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

	/* checkComp schaltet diese Compression in der Konfiguration ein oder aus */
	@FXML
	void changeComp1( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression1>uncompressed</allowedcompression1>";
		String no = "<allowedcompression1></allowedcompression1>";
		try {
			if ( checkComp1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp2( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression2>CCITT 1D</allowedcompression2>";
		String no = "<allowedcompression2></allowedcompression2>";
		try {
			if ( checkComp2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp1.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp3( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression3>CCITT Group 3</allowedcompression3>";
		String no = "<allowedcompression3></allowedcompression3>";
		try {
			if ( checkComp3.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp1.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp3.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp4( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression4>CCITT Group 4</allowedcompression4>";
		String no = "<allowedcompression4></allowedcompression4>";
		try {
			if ( checkComp4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp1.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp5( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression5>LZW</allowedcompression5>";
		String no = "<allowedcompression5></allowedcompression5>";
		try {
			if ( checkComp5.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp1.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp5.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp7( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression7>ISO JPEG</allowedcompression7>";
		String no = "<allowedcompression7></allowedcompression7>";
		try {
			if ( checkComp7.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp1.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp7.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp8( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression8>Deflate</allowedcompression8>";
		String no = "<allowedcompression8></allowedcompression8>";
		try {
			if ( checkComp8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp1.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp32773( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedcompression32773>PackBits</allowedcompression32773>";
		String no = "<allowedcompression32773></allowedcompression32773>";
		try {
			if ( checkComp32773.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected()
						&& !checkComp7.isSelected() && !checkComp3.isSelected()
						&& !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp1.isSelected() ) {
					labelMessage.setText( minOne );
					checkComp32773.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkPi schaltet diese photointer in der Konfiguration ein oder aus */
	@FXML
	void changePi0( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer0>white is zero</allowedphotointer0>";
		String no = "<allowedphotointer0></allowedphotointer0>";
		try {
			if ( checkPi0.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi0.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi1( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer1>black is zero</allowedphotointer1>";
		String no = "<allowedphotointer1></allowedphotointer1>";
		try {
			if ( checkPi1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi0.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi2( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer2>RGB</allowedphotointer2>";
		String no = "<allowedphotointer2></allowedphotointer2>";
		try {
			if ( checkPi2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi0.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi3( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer3>palette color</allowedphotointer3>";
		String no = "<allowedphotointer3></allowedphotointer3>";
		try {
			if ( checkPi3.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi0.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi3.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi4( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer4>transparency mask</allowedphotointer4>";
		String no = "<allowedphotointer4></allowedphotointer4>";
		try {
			if ( checkPi4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi0.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi5( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer5>CMYK</allowedphotointer5>";
		String no = "<allowedphotointer5></allowedphotointer5>";
		try {
			if ( checkPi5.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi0.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi5.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi6( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer6>YCbCr</allowedphotointer6>";
		String no = "<allowedphotointer6></allowedphotointer6>";
		try {
			if ( checkPi6.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi0.isSelected()
						&& !checkPi8.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi6.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi8( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedphotointer8>CIE L*a*b*</allowedphotointer8>";
		String no = "<allowedphotointer8></allowedphotointer8>";
		try {
			if ( checkPi8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected()
						&& !checkPi3.isSelected() && !checkPi4.isSelected()
						&& !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi0.isSelected() ) {
					labelMessage.setText( minOne );
					checkPi8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkBps schaltet diese bitspersample in der Konfiguration ein oder aus
	 */
	@FXML
	void changeBps1( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample1>1</allowedbitspersample1>";
		String no = "<allowedbitspersample1></allowedbitspersample1>";
		try {
			if ( checkBps1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected()
						&& !checkBps8.isSelected() && !checkBps16.isSelected()
						&& !checkBps32.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps2( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample2>2</allowedbitspersample2>";
		String no = "<allowedbitspersample2></allowedbitspersample2>";
		try {
			if ( checkBps2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps1.isSelected() && !checkBps4.isSelected()
						&& !checkBps8.isSelected() && !checkBps16.isSelected()
						&& !checkBps32.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps4( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample4>4</allowedbitspersample4>";
		String no = "<allowedbitspersample4></allowedbitspersample4>";
		try {
			if ( checkBps4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps1.isSelected()
						&& !checkBps8.isSelected() && !checkBps16.isSelected()
						&& !checkBps32.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps8( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample8>8</allowedbitspersample8>";
		String no = "<allowedbitspersample8></allowedbitspersample8>";
		try {
			if ( checkBps8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected()
						&& !checkBps1.isSelected() && !checkBps16.isSelected()
						&& !checkBps32.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps16( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample16>16</allowedbitspersample16>";
		String no = "<allowedbitspersample16></allowedbitspersample16>";
		try {
			if ( checkBps16.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected()
						&& !checkBps8.isSelected() && !checkBps1.isSelected()
						&& !checkBps32.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps16.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps32( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedbitspersample32>32</allowedbitspersample32>";
		String no = "<allowedbitspersample32></allowedbitspersample32>";
		try {
			if ( checkBps32.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected()
						&& !checkBps8.isSelected() && !checkBps16.isSelected()
						&& !checkBps1.isSelected() ) {
					labelMessage.setText( minOne );
					checkBps32.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkMultipage schaltet diese Multipage in der Konfiguration ein oder aus
	 */
	@FXML
	void changeMultipage( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedmultipage>yes</allowedmultipage>";
		String no = "<allowedmultipage>no</allowedmultipage>";
		try {
			if ( checkMultipage.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkTiles schaltet diese Tiles in der Konfiguration ein oder aus */
	@FXML
	void changeTiles( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedtiles>yes</allowedtiles>";
		String no = "<allowedtiles>no</allowedtiles>";
		try {
			if ( checkTiles.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkSize schaltet diese Size in der Konfiguration ein oder aus */
	@FXML
	void changeSize( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<allowedsize>yes</allowedsize>";
		String no = "<allowedsize>no</allowedsize>";
		try {
			if ( checkSize.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}