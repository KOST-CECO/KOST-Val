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

public class ConfigControllerPdfa
{

	@FXML
	private CheckBox	checkPdfa, checkPdftools, checkCallas, checkPdfa1a,
			checkPdfa2a, checkFont, checkJbig2, checkDetail, checkNentry,
			checkPdfa1b, checkPdfa2b, checkFontTol, checkPdfa2u,
			checkWarning3to2;

	@FXML
	private Button		buttonConfigApply;

	private File		configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String		dirOfJarPath, config,
			minOne = "Mindestens eine Variante muss erlaubt sein!";

	@FXML
	private Label		labelOtherPdfa, labelVersion, labelVal, labelMessage,
			labelConfig;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText(
				"Copyright © KOST/CECO          KOST-Val v2.2.0.1          JavaFX "
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
				labelOtherPdfa.setText( "Sonstiges" );
				labelVersion.setText( "Versionen" );
				labelVal.setText( "Validierungseinstellung: PDF/A" );
				buttonConfigApply.setText( "anwenden" );
				minOne = "Mindestens eine Variante muss erlaubt sein!";
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				labelOtherPdfa.setText( "Divers" );
				labelVersion.setText( "Versions" );
				labelVal.setText( "Paramètre de validation: PDF/A" );
				buttonConfigApply.setText( "appliquer" );
				minOne = "Au moins une variante doit etre autorisee !";
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				labelOtherPdfa.setText( "Altro" );
				labelVersion.setText( "Versioni" );
				labelVal.setText( "Parametro di convalida: PDF/A" );
				buttonConfigApply.setText( "Applica" );
				minOne = "Almeno una variante deve essere consentita!";
			} else {
				labelOtherPdfa.setText( "Other" );
				labelVersion.setText( "Versions" );
				labelVal.setText( "Validation setting: PDF/A" );
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
			String noPdftools = "<pdftools>no</pdftools>";
			String noDetail = "<detail>no</detail>";
			String noCallas = "<callas>no</callas>";
			String noNentry = "<nentry>W</nentry>";
			String noPdfa1a = "<pdfa1a></pdfa1a>";
			String noPdfa1b = "<pdfa1b></pdfa1b>";
			String noPdfa2a = "<pdfa2a></pdfa2a>";
			String noPdfa2b = "<pdfa2b></pdfa2b>";
			String noPdfa2u = "<pdfa2u></pdfa2u>";
			String noWarning3to2 = "<warning3to2>no</warning3to2>";
			String pdfaFont = "<pdfafont>strict</pdfafont>"; // tolerant oder
																// strict-->
			String pdfaFontTolerant = "<pdfafont>tolerant</pdfafont>"; // tolerant
																		// oder
																		// strict-->
			String noPdfaJbig2 = "<jbig2allowed>no</jbig2allowed>";

			if ( config.contains( noPdftools ) ) {
				checkPdftools.setSelected( false );
				checkFont.setDisable( true );
				checkDetail.setDisable( true );
				checkFontTol.setDisable( true );
			}
			if ( config.contains( noDetail ) ) {
				checkDetail.setSelected( false );
			}
			if ( config.contains( noWarning3to2 ) ) {
				checkWarning3to2.setSelected( false );
			}
			if ( config.contains( noPdfa2a ) && config.contains( noPdfa2u )
					&& config.contains( noPdfa2b ) ) {
				checkWarning3to2.setDisable( true );
			}
			if ( config.contains( noCallas ) ) {
				checkCallas.setSelected( false );
				checkNentry.setDisable( true );
			}
			if ( config.contains( noNentry ) ) {
				checkNentry.setSelected( false );
			}
			if ( config.contains( noPdfa1a ) ) {
				checkPdfa1a.setSelected( false );
			}
			if ( config.contains( noPdfa1b ) ) {
				checkPdfa1b.setSelected( false );
			}
			if ( config.contains( noPdfa2a ) ) {
				checkPdfa2a.setSelected( false );
			}
			if ( config.contains( noPdfa2b ) ) {
				checkPdfa2b.setSelected( false );
			}
			if ( config.contains( noPdfa2u ) ) {
				checkPdfa2u.setSelected( false );
			}

			if ( config.contains( pdfaFont )
					|| config.contains( pdfaFontTolerant ) ) {
				// checkFont.setSelected( true );
				if ( config.contains( pdfaFontTolerant ) ) {
					// checkFontTol.setSelected( true );
				} else {
					checkFontTol.setSelected( false );
				}
			} else {
				checkFont.setSelected( false );
				checkFontTol.setSelected( false );
			}
			if ( config.contains( noPdfaJbig2 ) ) {
				checkJbig2.setSelected( false );
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
	 * checkPdftools schaltet diese Validierung in der Konfiguration ein oder
	 * aus
	 */
	@FXML
	void changePdftools( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdftools>yes</pdftools>";
		String no = "<pdftools>no</pdftools>";
		try {
			if ( checkPdftools.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkDetail.setDisable( false );
				checkFont.setDisable( false );
				checkFontTol.setDisable( false );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkCallas.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdftools.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					checkDetail.setDisable( true );
					checkFont.setDisable( true );
					checkFontTol.setDisable( true );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkDetail schaltet diese Details von PDF Tools in der Konfiguration ein
	 * oder aus
	 */
	@FXML
	void changeDetail( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<detail>yes</detail>";
		String no = "<detail>no</detail>";
		try {
			if ( checkDetail.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkCallas schaltet diese Callas in der Konfiguration ein oder aus */
	@FXML
	void changeCallas( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<callas>yes</callas>";
		String no = "<callas>no</callas>";
		try {
			if ( checkCallas.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkNentry.setDisable( false );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdftools.isSelected() ) {
					labelMessage.setText( minOne );
					checkCallas.setSelected( true );
				} else {
					checkNentry.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkNentry schaltet diese Nentry bei Callas in der Konfiguration ein (E)
	 * oder aus (W)
	 */
	@FXML
	void changeNentry( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<nentry>E</nentry>";
		String no = "<nentry>W</nentry>";
		String ignorePt = "</ignore>";
		String nkeyPt = "The value of the key N is 4 but must be 3. [PDF Tools: 0x80410607]";
		try {
			if ( checkNentry.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				Util.oldnewstring( nkeyPt, " ", configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
				if ( !Util.stringInFileLine( nkeyPt, configFile ) ) {
					Util.oldnewstring( ignorePt, nkeyPt + ignorePt,
							configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkPdf.. schaltet diese Version in der Konfiguration ein oder aus */
	@FXML
	void changePdfa1a( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfa1a>1A</pdfa1a>";
		String no = "<pdfa1a></pdfa1a>";
		try {
			if ( checkPdfa1a.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected()
						&& !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdfa1a.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa1b( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfa1b>1B</pdfa1b>";
		String no = "<pdfa1b></pdfa1b>";
		try {
			if ( checkPdfa1b.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				;
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1a.isSelected() && !checkPdfa2a.isSelected()
						&& !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdfa1b.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2a( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfa2a>2A</pdfa2a>";
		String no = "<pdfa2a></pdfa2a>";
		try {
			if ( checkPdfa2a.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkWarning3to2.setDisable( false );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa1a.isSelected()
						&& !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdfa2a.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					if ( checkPdfa2u.isSelected()
							|| checkPdfa2b.isSelected() ) {
						checkWarning3to2.setDisable( false );
					} else {
						checkWarning3to2.setDisable( true );
					}
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2b( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfa2b>2B</pdfa2b>";
		String no = "<pdfa2b></pdfa2b>";
		try {
			if ( checkPdfa2b.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkWarning3to2.setDisable( false );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected()
						&& !checkPdfa1a.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdfa2b.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					if ( checkPdfa2a.isSelected()
							|| checkPdfa2u.isSelected() ) {
						checkWarning3to2.setDisable( false );
					} else {
						checkWarning3to2.setDisable( true );
					}
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2u( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfa2u>2U</pdfa2u>";
		String no = "<pdfa2u></pdfa2u>";
		try {
			if ( checkPdfa2u.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkWarning3to2.setDisable( false );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected()
						&& !checkPdfa2b.isSelected()
						&& !checkPdfa1a.isSelected() ) {
					labelMessage.setText( minOne );
					checkPdfa2u.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					if ( checkPdfa2a.isSelected()
							|| checkPdfa2b.isSelected() ) {
						checkWarning3to2.setDisable( false );
					} else {
						checkWarning3to2.setDisable( true );
					}
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkWarning3to2 validiert wenn eingeschaltet PDF/A-3 nach PDF/A-2 und
	 * ignoriert den Fehler betreffend der Version und gibt stattdessten eine
	 * Warnung aus.
	 */
	@FXML
	void changeWarning3to2( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<warning3to2>yes</warning3to2>";
		String no = "<warning3to2>no</warning3to2>";
		try {
			if ( checkWarning3to2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkFont schaltet diese Font-Validierung in der Konfiguration ein oder
	 * aus
	 */
	@FXML
	void changeFont( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfafont>strict</pdfafont>";
		String no = "<pdfafont>no</pdfafont>";
		if ( checkFontTol.isSelected() ) {
			yes = "<pdfafont>tolerant</pdfafont>";
		}
		try {
			if ( checkFont.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkFontTol.setDisable( false );
			} else {
				Util.oldnewstring( yes, no, configFile );
				checkFontTol.setSelected( false );
				checkFontTol.setDisable( true );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeFontTol( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<pdfafont>tolerant</pdfafont>";
		String no = "<pdfafont>no</pdfafont>";
		if ( checkFont.isSelected() ) {
			no = "<pdfafont>strict</pdfafont>";
		}
		try {
			if ( checkFontTol.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkFont.setSelected( true );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/*
	 * checkJbig2 schaltet diese Validierung in der Konfiguration ein oder aus
	 */
	@FXML
	void changeJbig2( ActionEvent event )
	{
		labelMessage.setText( "" );
		String yes = "<jbig2allowed>yes</jbig2allowed>";
		String no = "<jbig2allowed>no</jbig2allowed>";
		try {
			if ( checkJbig2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}