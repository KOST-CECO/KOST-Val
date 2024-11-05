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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class ConfigControllerOther
{

	@FXML
	private Button	buttonConfigApply, buttonRtf, buttonPptx, buttonDocx,
			buttonHtml, buttonOgg, buttonSvg, buttonJpm, buttonJpx, buttonMpeg2,
			buttonAvi, buttonArc, buttonWarc, buttonInterlis, buttonDwg,
			buttonIfc, buttonMsg, buttonEml, buttonDicom, buttonDxf,
			buttonOther2;

	private File	configFile	= new File( System.getenv( "USERPROFILE" )
			+ File.separator + ".kost-val_2x" + File.separator + "configuration"
			+ File.separator + "kostval.conf.xml" );

	private String	dirOfJarPath, config, stringPuid;

	private Locale	locale;

	@FXML
	private Label	labelOther, labelConfig, labelText, labelImage,
			labelAudioVideo, labelHyper, labelGis, labelCad, labelMedicine,
			labelMail, labelOther2;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText(
				"Copyright © KOST/CECO          KOST-Val v2.2.1.1          JavaFX "
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

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if ( Util.stringInFileLine( "kostval-conf-DE.xsl", configFile ) ) {
				locale = Locale.of( "de" );
				labelOther.setText( "Einstellungen weitere Formate" );
				buttonConfigApply.setText( "anwenden" );
				labelText.setText( "Text" );
				labelImage.setText( "Bild" );
				labelAudioVideo.setText( "Audio/Video" );
				labelHyper.setText( "Hypertext" );
				labelGis.setText( "GIS" );
				labelCad.setText( "CAD/CAM" );
				labelMedicine.setText( "Medizin" );
				labelMail.setText( "Mail" );
				labelOther2.setText( "Weitere" );
			} else if ( Util.stringInFileLine( "kostval-conf-FR.xsl",
					configFile ) ) {
				locale = Locale.of( "fr" );
				labelOther.setText( "Paramètres d'autres formats" );
				buttonConfigApply.setText( "appliquer" );
				labelText.setText( "Texte" );
				labelImage.setText( "Image" );
				labelAudioVideo.setText( "Audio/Vidéo" );
				labelHyper.setText( "Hypertexte" );
				labelGis.setText( "GIS" );
				labelCad.setText( "CAD/CAM" );
				labelMedicine.setText( "Médecine" );
				labelMail.setText( "Courriel" );
				labelOther2.setText( "Autres" );
			} else if ( Util.stringInFileLine( "kostval-conf-IT.xsl",
					configFile ) ) {
				locale = Locale.of( "it" );
				labelOther.setText( "Impostazioni di altri formati" );
				buttonConfigApply.setText( "Applica" );
				labelText.setText( "Testo" );
				labelImage.setText( "Immagine" );
				labelAudioVideo.setText( "Audio/Video" );
				labelHyper.setText( "Ipertesto" );
				labelGis.setText( "GIS" );
				labelCad.setText( "CAD/CAM" );
				labelMedicine.setText( "Medicina" );
				labelMail.setText( "Mail" );
				labelOther2.setText( "Altri" );
			} else {
				locale = Locale.of( "en" );
				labelOther.setText( "Settings other formats" );
				buttonConfigApply.setText( "apply" );
				labelText.setText( "Text" );
				labelImage.setText( "Image" );
				labelAudioVideo.setText( "Audio/Video" );
				labelHyper.setText( "Hypertext" );
				labelGis.setText( "GIS" );
				labelCad.setText( "CAD/CAM" );
				labelMedicine.setText( "Medicine" );
				labelMail.setText( "Mail" );
				labelOther2.setText( "Other" );
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
			// <otherformats>WARC HTML DWG</otherformats>
			String noDocx = "<docxvalidation></docxvalidation>";
			String noPptx = "<pptxvalidation></pptxvalidation>";
			String noRtf = "<rtfvalidation></rtfvalidation>";
			String noJpx = "<jpxvalidation></jpxvalidation>";
			String noJpm = "<jpmvalidation></jpmvalidation>";
			String noSvg = "<svgvalidation></svgvalidation>";
			String noOgg = "<oggvalidation></oggvalidation>";
			String noMpeg2 = "<mpeg2validation></mpeg2validation>";
			String noAvi = "<avivalidation></avivalidation>";
			String noHtml = "<htmlvalidation></htmlvalidation>";
			String noWarc = "<warcvalidation></warcvalidation>";
			String noArc = "<arcvalidation></arcvalidation>";
			String noDwg = "<dwgvalidation></dwgvalidation>";
			String noIfc = "<ifcvalidation></ifcvalidation>";
			String noDxf = "<dxfvalidation></dxfvalidation>";
			String noInterlis = "<interlisvalidation></interlisvalidation>";
			String noDicom = "<dicomvalidation></dicomvalidation>";
			String noMsg = "<msgvalidation></msgvalidation>";
			String noEml = "<emlvalidation></emlvalidation>";

			// TODO: bei Controllervalfofile ca Zeile 80 muss die Aenderung
			// neue oder entfernte Formate nachgetragen werden. Damit das Format
			// in der Header-Zeile des Logs erscheint.

			if ( config.contains( noDocx ) ) {
				buttonDocx.setText( "✗" );
				buttonDocx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonDocx.setText( "(✓)" );
				buttonDocx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noPptx ) ) {
				buttonPptx.setText( "✗" );
				buttonPptx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonPptx.setText( "(✓)" );
				buttonPptx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noRtf ) ) {
				buttonRtf.setText( "✗" );
				buttonRtf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonRtf.setText( "(✓)" );
				buttonRtf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noJpx ) ) {
				buttonJpx.setText( "✗" );
				buttonJpx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonJpx.setText( "(✓)" );
				buttonJpx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noJpm ) ) {
				buttonJpm.setText( "✗" );
				buttonJpm.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonJpm.setText( "(✓)" );
				buttonJpm.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noSvg ) ) {
				buttonSvg.setText( "✗" );
				buttonSvg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonSvg.setText( "(✓)" );
				buttonSvg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noOgg ) ) {
				buttonOgg.setText( "✗" );
				buttonOgg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonOgg.setText( "(✓)" );
				buttonOgg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noMpeg2 ) ) {
				buttonMpeg2.setText( "✗" );
				buttonMpeg2.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonMpeg2.setText( "(✓)" );
				buttonMpeg2.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noAvi ) ) {
				buttonAvi.setText( "✗" );
				buttonAvi.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonAvi.setText( "(✓)" );
				buttonAvi.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noHtml ) ) {
				buttonHtml.setText( "✗" );
				buttonHtml.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonHtml.setText( "(✓)" );
				buttonHtml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noWarc ) ) {
				buttonWarc.setText( "✗" );
				buttonWarc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonWarc.setText( "(✓)" );
				buttonWarc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noArc ) ) {
				buttonArc.setText( "✗" );
				buttonArc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonArc.setText( "(✓)" );
				buttonArc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noInterlis ) ) {
				buttonInterlis.setText( "✗" );
				buttonInterlis.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonInterlis.setText( "(✓)" );
				buttonInterlis.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noDwg ) ) {
				buttonDwg.setText( "✗" );
				buttonDwg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonDwg.setText( "(✓)" );
				buttonDwg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noIfc ) ) {
				buttonIfc.setText( "✗" );
				buttonIfc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonIfc.setText( "(✓)" );
				buttonIfc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noDxf ) ) {
				buttonDxf.setText( "✗" );
				buttonDxf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonDxf.setText( "(✓)" );
				buttonDxf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noDicom ) ) {
				buttonDicom.setText( "✗" );
				buttonDicom.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonDicom.setText( "(✓)" );
				buttonDicom.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noMsg ) ) {
				buttonMsg.setText( "✗" );
				buttonMsg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonMsg.setText( "(✓)" );
				buttonMsg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			if ( config.contains( noEml ) ) {
				buttonEml.setText( "✗" );
				buttonEml.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			} else {
				buttonEml.setText( "(✓)" );
				buttonEml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			}
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();

			stringPuid = doc.getElementsByTagName( "othervalidation" ).item( 0 )
					.getTextContent();
			buttonOther2.setText( stringPuid );
			bis.close();
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
		// labelMessage.setText(minOne ); "Apply" );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* change... schaltet zwischen x (v) herum */
	@FXML
	void changeDocx( ActionEvent event )
	{
		String az = "<docxvalidation>DOCX </docxvalidation>";
		String no = "<docxvalidation></docxvalidation>";
		try {
			String optButton = buttonDocx.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonDocx.setText( "(✓)" );
				buttonDocx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonDocx.setText( "✗" );
				buttonDocx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePptx( ActionEvent event )
	{
		String az = "<pptxvalidation>PPTX </pptxvalidation>";
		String no = "<pptxvalidation></pptxvalidation>";
		try {
			String optButton = buttonPptx.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonPptx.setText( "(✓)" );
				buttonPptx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonPptx.setText( "✗" );
				buttonPptx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeRtf( ActionEvent event )
	{
		String az = "<rtfvalidation>RTF </rtfvalidation>";
		String no = "<rtfvalidation></rtfvalidation>";
		try {
			String optButton = buttonRtf.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonRtf.setText( "(✓)" );
				buttonRtf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonRtf.setText( "✗" );
				buttonRtf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeJpx( ActionEvent event )
	{
		String az = "<jpxvalidation>JPX </jpxvalidation>";
		String no = "<jpxvalidation></jpxvalidation>";
		try {
			String optButton = buttonJpx.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonJpx.setText( "(✓)" );
				buttonJpx.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonJpx.setText( "✗" );
				buttonJpx.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeJpm( ActionEvent event )
	{
		String az = "<jpmvalidation>JPM </jpmvalidation>";
		String no = "<jpmvalidation></jpmvalidation>";
		try {
			String optButton = buttonJpm.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonJpm.setText( "(✓)" );
				buttonJpm.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonJpm.setText( "✗" );
				buttonJpm.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeSvg( ActionEvent event )
	{
		String az = "<svgvalidation>SVG </svgvalidation>";
		String no = "<svgvalidation></svgvalidation>";
		try {
			String optButton = buttonSvg.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonSvg.setText( "(✓)" );
				buttonSvg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonSvg.setText( "✗" );
				buttonSvg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeOgg( ActionEvent event )
	{
		String az = "<oggvalidation>OGG </oggvalidation>";
		String no = "<oggvalidation></oggvalidation>";
		try {
			String optButton = buttonOgg.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonOgg.setText( "(✓)" );
				buttonOgg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonOgg.setText( "✗" );
				buttonOgg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMpeg2( ActionEvent event )
	{
		String az = "<mpeg2validation>MPEG2 </mpeg2validation>";
		String no = "<mpeg2validation></mpeg2validation>";
		try {
			String optButton = buttonMpeg2.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonMpeg2.setText( "(✓)" );
				buttonMpeg2.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonMpeg2.setText( "✗" );
				buttonMpeg2.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeAvi( ActionEvent event )
	{
		String az = "<avivalidation>AVI </avivalidation>";
		String no = "<avivalidation></avivalidation>";
		try {
			String optButton = buttonAvi.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonAvi.setText( "(✓)" );
				buttonAvi.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonAvi.setText( "✗" );
				buttonAvi.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeHtml( ActionEvent event )
	{
		String az = "<htmlvalidation>HTML </htmlvalidation>";
		String no = "<htmlvalidation></htmlvalidation>";
		try {
			String optButton = buttonHtml.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonHtml.setText( "(✓)" );
				buttonHtml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonHtml.setText( "✗" );
				buttonHtml.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeWarc( ActionEvent event )
	{
		String az = "<warcvalidation>WARC </warcvalidation>";
		String no = "<warcvalidation></warcvalidation>";
		try {
			String optButton = buttonWarc.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonWarc.setText( "(✓)" );
				buttonWarc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonWarc.setText( "✗" );
				buttonWarc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeArc( ActionEvent event )
	{
		String az = "<arcvalidation>ARC </arcvalidation>";
		String no = "<arcvalidation></arcvalidation>";
		try {
			String optButton = buttonArc.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonArc.setText( "(✓)" );
				buttonArc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonArc.setText( "✗" );
				buttonArc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDwg( ActionEvent event )
	{
		String az = "<dwgvalidation>DWG </dwgvalidation>";
		String no = "<dwgvalidation></dwgvalidation>";
		try {
			String optButton = buttonDwg.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonDwg.setText( "(✓)" );
				buttonDwg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonDwg.setText( "✗" );
				buttonDwg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeIfc( ActionEvent event )
	{
		String az = "<ifcvalidation>IFC </ifcvalidation>";
		String no = "<ifcvalidation></ifcvalidation>";
		try {
			String optButton = buttonIfc.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonIfc.setText( "(✓)" );
				buttonIfc.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonIfc.setText( "✗" );
				buttonIfc.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDxf( ActionEvent event )
	{
		String az = "<dxfvalidation>DXF </dxfvalidation>";
		String no = "<dxfvalidation></dxfvalidation>";
		try {
			String optButton = buttonDxf.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonDxf.setText( "(✓)" );
				buttonDxf.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonDxf.setText( "✗" );
				buttonDxf.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeInterlis( ActionEvent event )
	{
		String az = "<interlisvalidation>INTERLIS </interlisvalidation>";
		String no = "<interlisvalidation></interlisvalidation>";
		try {
			String optButton = buttonInterlis.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonInterlis.setText( "(✓)" );
				buttonInterlis.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonInterlis.setText( "✗" );
				buttonInterlis.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeDicom( ActionEvent event )
	{
		String az = "<dicomvalidation>DICOM </dicomvalidation>";
		String no = "<dicomvalidation></dicomvalidation>";
		try {
			String optButton = buttonDicom.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonDicom.setText( "(✓)" );
				buttonDicom.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonDicom.setText( "✗" );
				buttonDicom.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeMsg( ActionEvent event )
	{
		String az = "<msgvalidation>MSG </msgvalidation>";
		String no = "<msgvalidation></msgvalidation>";
		try {
			String optButton = buttonMsg.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonMsg.setText( "(✓)" );
				buttonMsg.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonMsg.setText( "✗" );
				buttonMsg.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeEml( ActionEvent event )
	{
		String az = "<emlvalidation>EML </emlvalidation>";
		String no = "<emlvalidation></emlvalidation>";
		try {
			String optButton = buttonEml.getText();
			if ( optButton.equals( "✗" ) ) {
				Util.oldnewstring( no, az, configFile );
				buttonEml.setText( "(✓)" );
				buttonEml.setStyle(
						"-fx-text-fill: Orange; -fx-background-color: WhiteSmoke" );
			} else {
				Util.oldnewstring( az, no, configFile );
				buttonEml.setText( "✗" );
				buttonEml.setStyle(
						"-fx-text-fill: Red; -fx-background-color: WhiteSmoke" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* Wenn Aenderungen an changeOther2 gemacht wird, wird es ausgeloest */
	@FXML
	void changeOther2( ActionEvent event )
	{
		stringPuid = buttonOther2.getText();
		// create a TextInputDialog mit der Texteingabe der Puid
		TextInputDialog dialog = new TextInputDialog( stringPuid );

		// Set title & header text
		String puidIntInit = stringPuid;

		dialog.setTitle( "KOST-Val - Configuration" );
		String headerDeFrItEn = "Auflistung der weiteren akzeptierten Dateiformate []:";
		if ( locale.toString().startsWith( "fr" ) ) {
			headerDeFrItEn = "Liste des autres formats de fichiers acceptés [] :";
		} else if ( locale.toString().startsWith( "it" ) ) {
			headerDeFrItEn = "Elenco degli altri formati di file accettati []:";
		} else if ( locale.toString().startsWith( "en" ) ) {
			headerDeFrItEn = "List of other accepted file formats []:";
		}
		dialog.setHeaderText( headerDeFrItEn );
		dialog.setContentText( "" );

		// Show the dialog and capture the result.
		Optional<String> result = dialog.showAndWait();

		// If the "Okay" button was clicked, the result will contain our String
		// in the get() method
		String stringPuidNew = "";
		if ( result.isPresent() ) {
			try {
				stringPuidNew = result.get();
				stringPuid = stringPuidNew;
				buttonOther2.setText( stringPuid );
				String allowedformats = "<othervalidation>" + puidIntInit
						+ "</othervalidation>";
				String allowedformatsNew = "<othervalidation>" + stringPuidNew
						+ "</othervalidation>";
				Util.oldnewstring( allowedformats, allowedformatsNew,
						configFile );
			} catch ( NumberFormatException | IOException e ) {
				e.printStackTrace();
			}
		} else {
			// Keine Aktion
		}
	}

}