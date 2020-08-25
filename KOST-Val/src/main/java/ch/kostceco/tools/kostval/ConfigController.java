/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kostval.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ConfigController
{

	@FXML
	private CheckBox				checkPdfa, checkPdftools, checkCallas, checkPdfa1a, checkPdfa2a,
			checkFont, checkImage, checkJbig2, checkDetail, checkNentry, checkPdfa1b, checkPdfa2b,
			checkFontTol, checkPdfa2u, checkSiard, checkSiard10, checkSiard21, checkJpeg2000, checkJpeg,
			checkTiff, checkComp1, checkComp5, checkPi0, checkPi4, checkComp2, checkComp7, checkPi1,
			checkPi5, checkBps1, checkBps8, checkMultipage, checkBps2, checkBps16, checkTiles, checkComp3,
			checkComp8, checkPi2, checkPi6, checkBps4, checkBps32, checkSize, checkComp4, checkComp32773,
			checkPi3, checkPi8, checkSip0160;

	@FXML
	private Button					buttonConfigApply, buttonConfigApplyStandard, buttonConfigCancel;

	private File						configFileBackup		= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x" + File.separator
					+ "configuration" + File.separator + "BACKUP.kostval.conf.xml" );

	private File						configFileStandard	= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x" + File.separator
					+ "configuration" + File.separator + "STANDARD.kostval.conf.xml" );

	private File						configFile					= new File(
			System.getenv( "USERPROFILE" ) + File.separator + ".kost-val_2x" + File.separator
					+ "configuration" + File.separator + "kostval.conf.xml" );

	private String					dirOfJarPath;

	private Locale					locale							= Locale.getDefault();

	ObservableList<String>	langList						= FXCollections.observableArrayList( "Deutsch",
			"Français", "English" );

	@FXML
	private Label						labelBps, labelComp, labelOther, labelPi, labelLength, labelName,
			labelPuid;

	@FXML
	private WebView					wbv;

	private WebEngine				engine;

	@FXML
	private TextField				textLength, textName, textPuid;

	@FXML
	private Label						labelConfig;

	@FXML
	void initialize()
	{

		// TODO --> initialize (wird einmalig am Anfang ausgefuehrt)

		// Copyright und Versionen ausgeben
		String java6432 = System.getProperty( "sun.arch.data.model" );
		String javaVersion = System.getProperty( "java.version" );
		String javafxVersion = System.getProperty( "javafx.version" );
		labelConfig.setText( "Copyright © KOST/CECO          KOST-Val v2.0.0.alpha2          JavaFX "
				+ javafxVersion + "   &   Java-" + java6432 + " " + javaVersion + "." );

		// Original Config Kopieren
		try {
			Util.copyFile( configFile, configFileBackup );
		} catch ( IOException e2 ) {
			e2.printStackTrace();
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
			if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}
			setLibraryPath( dirOfJarPath );
		} catch ( Exception e1 ) {
			e1.printStackTrace();
		}

		// Sprache anhand configFile (HauptGui) setzten
		try {
			if ( Util.stringInFile( "kostval-conf-DE.xsl", configFile ) ) {
				labelBps.setText( "Bits per Sample (pro Kanal)" );
				labelComp.setText( "Komprimierungsalgorithmus" );
				labelOther.setText( "Sonstiges" );
				labelPi.setText( "Farbraum" );
				labelLength.setText( "Pfadlänge" );
				labelName.setText( "SIP Name" );
				labelPuid.setText( "PUID" );
				locale = new Locale( "de" );
				buttonConfigApply.setText( "anwenden" );
				buttonConfigApplyStandard.setText( "Standard anwenden" );
				buttonConfigCancel.setText( "verwerfen" );
			} else if ( Util.stringInFile( "kostval-conf-FR.xsl", configFile ) ) {
				labelBps.setText( "Bits par échantillon (par canal)" );
				labelComp.setText( "Algorithme de compression" );
				labelOther.setText( "Divers" );
				labelPi.setText( "Espace couleur" );
				labelLength.setText( "Longueur du chemin" );
				labelName.setText( "Nom du SIP" );
				labelPuid.setText( "PUID" );
				locale = new Locale( "fr" );
				buttonConfigApply.setText( "appliquer" );
				buttonConfigApplyStandard.setText( "appliquer le standard" );
				buttonConfigCancel.setText( "annuler" );
			} else {
				labelBps.setText( "Bits per sample (per channel)" );
				labelComp.setText( "Compression algorithm" );
				labelOther.setText( "Other" );
				labelPi.setText( "Color space" );
				labelLength.setText( "Path length" );
				labelName.setText( "SIP Name" );
				labelPuid.setText( "PUID" );
				locale = new Locale( "en" );
				buttonConfigApply.setText( "apply" );
				buttonConfigApplyStandard.setText( "apply Standard" );
				buttonConfigCancel.setText( "cancel" );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// Werte aus Konfiguration lesen und Check-Box entsprechend setzten
		// checkPdfa, checkJpeg2000, checkJpeg, checkTiff, checkSiard;
		try {
			byte[] encoded;
			encoded = Files.readAllBytes( Paths.get( configFile.getAbsolutePath() ) );
			String config = new String( encoded, StandardCharsets.UTF_8 );
			String noPdfa = "<pdfavalidation>no</pdfavalidation>";
			String noPdftools = "<pdftools>no</pdftools>";
			String noDetail = "<detail>no</detail>";
			String noCallas = "<callas>no</callas>";
			String noNentry = "<nentry>W</nentry>";
			String noPdfa1a = "<pdfa1a></pdfa1a>";
			String noPdfa1b = "<pdfa1b></pdfa1b>";
			String noPdfa2a = "<pdfa2a></pdfa2a>";
			String noPdfa2b = "<pdfa2b></pdfa2b>";
			String noPdfa2u = "<pdfa2u></pdfa2u>";
			String pdfaFont = "<pdfafont>strict</pdfafont>"; // tolerant oder strict-->
			String pdfaFontTolerant = "<pdfafont>tolerant</pdfafont>"; // tolerant oder strict-->
			String noPdfaImage = "<pdfaimage>no</pdfaimage>";
			String noPdfaJbig2 = "<jbig2allowed>yes</jbig2allowed>";
			String noJpeg2000 = "<jp2validation>no</jp2validation>";
			String noJpeg = "<jpegvalidation>no</jpegvalidation>";
			String noSiard = "<siardvalidation>no</siardvalidation>";
			String noSiard10 = "<siard10></siard10>";
			String noSiard21 = "<siard21></siard21>";
			String noTiff = "<tiffvalidation>no</tiffvalidation>";
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
			String noSip0160 = "<ech0160validation>no</ech0160validation>";

			if ( config.contains( noPdfa ) ) {
				checkPdfa.setSelected( false );
				checkPdftools.setDisable( true );
				checkCallas.setDisable( true );
				checkPdfa1a.setDisable( true );
				checkPdfa2a.setDisable( true );
				checkFont.setDisable( true );
				checkImage.setDisable( true );
				checkJbig2.setDisable( true );
				checkDetail.setDisable( true );
				checkNentry.setDisable( true );
				checkPdfa1b.setDisable( true );
				checkPdfa2b.setDisable( true );
				checkFontTol.setDisable( true );
				checkPdfa2u.setDisable( true );
			}
			if ( config.contains( noPdftools ) ) {
				checkPdftools.setSelected( false );
				checkFont.setDisable( true );
				checkDetail.setDisable( true );
				checkFontTol.setDisable( true );
			}
			if ( config.contains( noDetail ) ) {
				checkDetail.setSelected( false );
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

			if ( config.contains( pdfaFont ) || config.contains( pdfaFontTolerant ) ) {
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
			if ( config.contains( noPdfaImage ) ) {
				checkImage.setSelected( false );
			}
			if ( config.contains( noPdfaJbig2 ) ) {
				checkJbig2.setSelected( false );
			}
			if ( config.contains( noJpeg2000 ) ) {
				checkJpeg2000.setSelected( false );
			}
			if ( config.contains( noJpeg ) ) {
				checkJpeg.setSelected( false );
			}
			if ( config.contains( noSiard ) ) {
				checkSiard.setSelected( false );
				checkSiard10.setDisable( true );
				checkSiard21.setDisable( true );
			}
			if ( config.contains( noSiard10 ) ) {
				checkSiard10.setSelected( false );
			}
			if ( config.contains( noSiard21 ) ) {
				checkSiard21.setSelected( false );
			}
			if ( config.contains( noTiff ) ) {
				checkTiff.setSelected( false );
				checkComp1.setDisable( true );
				checkComp5.setDisable( true );
				checkPi0.setDisable( true );
				checkPi4.setDisable( true );
				checkComp2.setDisable( true );
				checkComp7.setDisable( true );
				checkPi1.setDisable( true );
				checkPi5.setDisable( true );
				checkBps1.setDisable( true );
				checkBps8.setDisable( true );
				checkMultipage.setDisable( true );
				checkBps2.setDisable( true );
				checkBps16.setDisable( true );
				checkTiles.setDisable( true );
				checkComp3.setDisable( true );
				checkComp8.setDisable( true );
				checkPi2.setDisable( true );
				checkPi6.setDisable( true );
				checkBps4.setDisable( true );
				checkBps32.setDisable( true );
				checkSize.setDisable( true );
				checkComp4.setDisable( true );
				checkComp32773.setDisable( true );
				checkPi3.setDisable( true );
				checkPi8.setDisable( true );
			}
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
			if ( config.contains( noSip0160 ) ) {
				checkSip0160.setSelected( false );
				textLength.setDisable( true );
				textName.setDisable( true );
				textPuid.setDisable( true );
			}

			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String allowedformats = doc.getElementsByTagName( "allowedformats" ).item( 0 )
					.getTextContent();
			textPuid.setText( allowedformats );
			String allowedlengthofpaths = doc.getElementsByTagName( "allowedlengthofpaths" ).item( 0 )
					.getTextContent();
			textLength.setText( allowedlengthofpaths );
			String allowedsipname = doc.getElementsByTagName( "allowedsipname" ).item( 0 )
					.getTextContent();
			textName.setText( allowedsipname );

		} catch ( IOException | SAXException | ParserConfigurationException e1 ) {
			e1.printStackTrace();
		}

		engine = wbv.getEngine();
		engine.load( "file:///" + configFile.getAbsolutePath() );
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

	/* TODO --> Button ================= */

	@FXML
	void configApply( ActionEvent e )
	{
		engine.loadContent( "Apply" );
		Util.deleteFile( configFileBackup );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	@FXML
	void configApplyStandard( ActionEvent e )
	{
		engine.loadContent( "Apply Standard" );
		configFile.delete();
		Util.deleteFile( configFileBackup );
		try {
			Util.copyFile( configFileStandard, configFile );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	@FXML
	void configCancel( ActionEvent e )
	{
		configFile.delete();
		engine.loadContent( "Cancel" );
		try {
			Util.copyFile( configFileBackup, configFile );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		Util.deleteFile( configFileBackup );
		((Stage) (((Button) e.getSource()).getScene().getWindow())).close();
	}

	/* TODO --> TextField ================= */

	/* Wenn Aenderungen an changeLength gemacht wird, wird es ausgeloest */
	@FXML
	void changeLength( ActionEvent event )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String lengthIntInit = doc.getElementsByTagName( "allowedlengthofpaths" ).item( 0 )
					.getTextContent();
			bis.close();
			doc = null;
			String allowedlengthofpaths = "<allowedlengthofpaths>" + lengthIntInit
					+ "</allowedlengthofpaths>";
			int lengthInt = 179;
			try {
				if ( textLength.getText().isEmpty() ) {
					textLength.setText( lengthIntInit );
				}
				lengthInt = Integer.parseInt( textLength.getText() );
			} catch ( NumberFormatException e ) {
				lengthInt = Integer.parseInt( lengthIntInit );
				String message = e.getMessage();
				engine.loadContent( message );
				textLength.setText( lengthIntInit );
			}
			String allowedlengthofpathsNew = "<allowedlengthofpaths>" + lengthInt
					+ "</allowedlengthofpaths>";
			Util.oldnewstring( allowedlengthofpaths, allowedlengthofpathsNew, configFile );
		} catch ( IOException | SAXException | ParserConfigurationException e1 ) {
			e1.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* Wenn Aenderungen an changeName gemacht wird, wird es ausgeloest */
	@FXML
	void changeName( ActionEvent event )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String regexInit = doc.getElementsByTagName( "allowedsipname" ).item( 0 ).getTextContent();
			bis.close();
			doc = null;
			String allowedsipname = "<allowedsipname>" + regexInit + "</allowedsipname>";
			String regex = textName.getText();
			if ( regex.isEmpty() ) {
				regex = regexInit;
				textName.setText( regexInit );
			}
			String allowedsipnameNew = "<allowedsipname>" + regex + "</allowedsipname>";
			Util.oldnewstring( allowedsipname, allowedsipnameNew, configFile );
		} catch ( IOException | SAXException | ParserConfigurationException e1 ) {
			e1.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* Wenn Aenderungen an changePuid gemacht wird, wird es ausgeloest */
	@FXML
	void changePuid( ActionEvent event )
	{
		try {
			Document doc = null;
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();
			String puidInit = doc.getElementsByTagName( "allowedformats" ).item( 0 ).getTextContent();
			bis.close();
			doc = null;
			String allowedformats = "<allowedformats>" + puidInit + "</allowedformats>";
			String puid = textPuid.getText();
			if ( puid.isEmpty() ) {
				puid = puidInit;
				textPuid.setText( puidInit );
			}
			String allowedformatsNew = "<allowedformats>" + puid + "</allowedformats>";
			Util.oldnewstring( allowedformats, allowedformatsNew, configFile );
		} catch ( IOException | SAXException | ParserConfigurationException e1 ) {
			e1.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* TODO --> CheckBox ================= */

	/* checkSip0160 schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeSip0160( ActionEvent event )
	{
		String yes = "<ech0160validation>yes</ech0160validation>";
		String no = "<ech0160validation>no</ech0160validation>";
		try {
			if ( checkSip0160.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				textLength.setDisable( false );
				textName.setDisable( false );
				textPuid.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg2000.isSelected() && !checkSiard.isSelected() && !checkPdfa.isSelected()
						&& !checkTiff.isSelected() && !checkJpeg.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkSip0160.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
					textLength.setDisable( true );
					textName.setDisable( true );
					textPuid.setDisable( true );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkPdfa schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changePdfa( ActionEvent event )
	{
		String yes = "<pdfavalidation>yes</pdfavalidation>";
		String no = "<pdfavalidation>no</pdfavalidation>";
		try {
			if ( checkPdfa.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkPdftools.setDisable( false );
				checkCallas.setDisable( false );
				checkPdfa1a.setDisable( false );
				checkPdfa2a.setDisable( false );
				checkFont.setDisable( false );
				checkImage.setDisable( false );
				checkJbig2.setDisable( false );
				checkDetail.setDisable( false );
				checkNentry.setDisable( false );
				checkPdfa1b.setDisable( false );
				checkPdfa2b.setDisable( false );
				checkFontTol.setDisable( false );
				checkPdfa2u.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg2000.isSelected() && !checkSiard.isSelected() && !checkJpeg.isSelected()
						&& !checkTiff.isSelected() && !checkSip0160.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
					checkPdftools.setDisable( true );
					checkCallas.setDisable( true );
					checkPdfa1a.setDisable( true );
					checkPdfa2a.setDisable( true );
					checkFont.setDisable( true );
					checkImage.setDisable( true );
					checkJbig2.setDisable( true );
					checkDetail.setDisable( true );
					checkNentry.setDisable( true );
					checkPdfa1b.setDisable( true );
					checkPdfa2b.setDisable( true );
					checkFontTol.setDisable( true );
					checkPdfa2u.setDisable( true );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkPdftools schaltet diese Validierung in der Konfiguration ein oder aus */
	@FXML
	void changePdftools( ActionEvent event )
	{
		String yes = "<pdftools>yes</pdftools>";
		String no = "<pdftools>no</pdftools>";
		try {
			if ( checkPdftools.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkDetail.setDisable( false );
				checkFont.setDisable( false );
				checkFontTol.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkCallas.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdftools.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					checkDetail.setDisable( true );
					checkFont.setDisable( true );
					checkFontTol.setDisable( true );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkDetail schaltet diese Details von PDF Tools in der Konfiguration ein oder aus */
	@FXML
	void changeDetail( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkCallas schaltet diese Callas in der Konfiguration ein oder aus */
	@FXML
	void changeCallas( ActionEvent event )
	{
		String yes = "<callas>yes</callas>";
		String no = "<callas>no</callas>";
		try {
			if ( checkCallas.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				checkNentry.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdftools.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkCallas.setSelected( true );
				} else {
					checkNentry.setDisable( true );
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkNentry schaltet diese Nentry bei Callas in der Konfiguration ein (E) oder aus (W) */
	@FXML
	void changeNentry( ActionEvent event )
	{
		String yes = "<nentry>E</nentry>";
		String no = "<nentry>W</nentry>";
		try {
			if ( checkNentry.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkPdf.. schaltet diese Version in der Konfiguration ein oder aus */
	@FXML
	void changePdfa1a( ActionEvent event )
	{
		String yes = "<pdfa1a>1A</pdfa1a>";
		String no = "<pdfa1a></pdfa1a>";
		try {
			if ( checkPdfa1a.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa1a.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa1b( ActionEvent event )
	{
		String yes = "<pdfa1b>1B</pdfa1b>";
		String no = "<pdfa1b></pdfa1b>";
		try {
			if ( checkPdfa1b.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1a.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa1b.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2a( ActionEvent event )
	{
		String yes = "<pdfa2a>2A</pdfa2a>";
		String no = "<pdfa2a></pdfa2a>";
		try {
			if ( checkPdfa2a.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa1a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa2a.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2b( ActionEvent event )
	{
		String yes = "<pdfa2b>2B</pdfa2b>";
		String no = "<pdfa2b></pdfa2b>";
		try {
			if ( checkPdfa2b.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa1a.isSelected()
						&& !checkPdfa2u.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa2b.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePdfa2u( ActionEvent event )
	{
		String yes = "<pdfa2u>2U</pdfa2u>";
		String no = "<pdfa2u></pdfa2u>";
		try {
			if ( checkPdfa2u.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPdfa1b.isSelected() && !checkPdfa2a.isSelected() && !checkPdfa2b.isSelected()
						&& !checkPdfa1a.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPdfa2u.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkFont schaltet diese Font-Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeFont( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	@FXML
	void changeFontTol( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkImage schaltet diese Font-Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeImage( ActionEvent event )
	{
		String yes = "<pdfaimage>yes</pdfaimage>";
		String no = "<pdfaimage>no</pdfaimage>";
		try {
			if ( checkImage.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
			} else {
				Util.oldnewstring( yes, no, configFile );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkJbig2 schaltet diese Font-Validierung in der Konfiguration ein oder aus */
	@FXML
	void changeJbig2( ActionEvent event )
	{
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
				checkSiard10.setDisable( false );
				checkSiard21.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {

				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg2000.isSelected() && !checkJpeg.isSelected() && !checkPdfa.isSelected()
						&& !checkTiff.isSelected() && !checkSip0160.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkSiard.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					checkSiard10.setDisable( true );
					checkSiard21.setDisable( true );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkSiard10 schaltet diese Version in der Konfiguration ein oder aus */
	@FXML
	void changeSiard10( ActionEvent event )
	{
		String yes = "<siard10>1.0</siard10>";
		String no = "<siard10></siard10>";
		try {
			if ( checkSiard10.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkSiard21.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkSiard10.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkSiard21 schaltet diese Version in der Konfiguration ein oder aus */
	@FXML
	void changeSiard21( ActionEvent event )
	{
		String yes = "<siard21>2.1</siard21>";
		String no = "<siard21></siard21>";
		try {
			if ( checkSiard21.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkSiard10.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkSiard21.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg.isSelected() && !checkSiard.isSelected() && !checkPdfa.isSelected()
						&& !checkTiff.isSelected() && !checkSip0160.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkJpeg2000.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg2000.isSelected() && !checkSiard.isSelected() && !checkPdfa.isSelected()
						&& !checkTiff.isSelected() && !checkSip0160.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkJpeg.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
				checkComp1.setDisable( false );
				checkComp5.setDisable( false );
				checkPi0.setDisable( false );
				checkPi4.setDisable( false );
				checkComp2.setDisable( false );
				checkComp7.setDisable( false );
				checkPi1.setDisable( false );
				checkPi5.setDisable( false );
				checkBps1.setDisable( false );
				checkBps8.setDisable( false );
				checkMultipage.setDisable( false );
				checkBps2.setDisable( false );
				checkBps16.setDisable( false );
				checkTiles.setDisable( false );
				checkComp3.setDisable( false );
				checkComp8.setDisable( false );
				checkPi2.setDisable( false );
				checkPi6.setDisable( false );
				checkBps4.setDisable( false );
				checkBps32.setDisable( false );
				checkSize.setDisable( false );
				checkComp4.setDisable( false );
				checkComp32773.setDisable( false );
				checkPi3.setDisable( false );
				checkPi8.setDisable( false );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkJpeg2000.isSelected() && !checkSiard.isSelected() && !checkPdfa.isSelected()
						&& !checkJpeg.isSelected() && !checkSip0160.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkTiff.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
					checkComp1.setDisable( true );
					checkComp5.setDisable( true );
					checkPi0.setDisable( true );
					checkPi4.setDisable( true );
					checkComp2.setDisable( true );
					checkComp7.setDisable( true );
					checkPi1.setDisable( true );
					checkPi5.setDisable( true );
					checkBps1.setDisable( true );
					checkBps8.setDisable( true );
					checkMultipage.setDisable( true );
					checkBps2.setDisable( true );
					checkBps16.setDisable( true );
					checkTiles.setDisable( true );
					checkComp3.setDisable( true );
					checkComp8.setDisable( true );
					checkPi2.setDisable( true );
					checkPi6.setDisable( true );
					checkBps4.setDisable( true );
					checkBps32.setDisable( true );
					checkSize.setDisable( true );
					checkComp4.setDisable( true );
					checkComp32773.setDisable( true );
					checkPi3.setDisable( true );
					checkPi8.setDisable( true );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkComp schaltet diese Compression in der Konfiguration ein oder aus */
	@FXML
	void changeComp1( ActionEvent event )
	{
		String yes = "<allowedcompression1>Uncompressed</allowedcompression1>";
		String no = "<allowedcompression1></allowedcompression1>";
		try {
			if ( checkComp1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp2( ActionEvent event )
	{
		String yes = "<allowedcompression2>CCITT 1D</allowedcompression2>";
		String no = "<allowedcompression2></allowedcompression2>";
		try {
			if ( checkComp2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp1.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp3( ActionEvent event )
	{
		String yes = "<allowedcompression3>T4/Group 3 Fax</allowedcompression3>";
		String no = "<allowedcompression3></allowedcompression3>";
		try {
			if ( checkComp3.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp1.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp3.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp4( ActionEvent event )
	{
		String yes = "<allowedcompression4>T6/Group 4 Fax</allowedcompression4>";
		String no = "<allowedcompression4></allowedcompression4>";
		try {
			if ( checkComp4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp1.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp5( ActionEvent event )
	{
		String yes = "<allowedcompression5>LZW</allowedcompression5>";
		String no = "<allowedcompression5></allowedcompression5>";
		try {
			if ( checkComp5.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp1.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp5.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp7( ActionEvent event )
	{
		String yes = "<allowedcompression7>JPEG</allowedcompression7>";
		String no = "<allowedcompression7></allowedcompression7>";
		try {
			if ( checkComp7.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp1.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp7.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp8( ActionEvent event )
	{
		String yes = "<allowedcompression8>Deflate</allowedcompression8>";
		String no = "<allowedcompression8></allowedcompression8>";
		try {
			if ( checkComp8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp1.isSelected() && !checkComp4.isSelected()
						&& !checkComp32773.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeComp32773( ActionEvent event )
	{
		String yes = "<allowedcompression32773>PackBits</allowedcompression32773>";
		String no = "<allowedcompression32773></allowedcompression32773>";
		try {
			if ( checkComp32773.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkComp5.isSelected() && !checkComp2.isSelected() && !checkComp7.isSelected()
						&& !checkComp3.isSelected() && !checkComp8.isSelected() && !checkComp4.isSelected()
						&& !checkComp1.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkComp32773.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
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
		String yes = "<allowedphotointer0>WhiteIsZero</allowedphotointer0>";
		String no = "<allowedphotointer0></allowedphotointer0>";
		try {
			if ( checkPi0.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi0.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi1( ActionEvent event )
	{
		String yes = "<allowedphotointer1>BlackIsZero</allowedphotointer1>";
		String no = "<allowedphotointer1></allowedphotointer1>";
		try {
			if ( checkPi1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi0.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi2( ActionEvent event )
	{
		String yes = "<allowedphotointer2>RGB</allowedphotointer2>";
		String no = "<allowedphotointer2></allowedphotointer2>";
		try {
			if ( checkPi2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi0.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi3( ActionEvent event )
	{
		String yes = "<allowedphotointer3>RGB Palette</allowedphotointer3>";
		String no = "<allowedphotointer3></allowedphotointer3>";
		try {
			if ( checkPi3.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi0.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi3.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi4( ActionEvent event )
	{
		String yes = "<allowedphotointer4>transparency mask</allowedphotointer4>";
		String no = "<allowedphotointer4></allowedphotointer4>";
		try {
			if ( checkPi4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi0.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi5( ActionEvent event )
	{
		String yes = "<allowedphotointer5>CMYK</allowedphotointer5>";
		String no = "<allowedphotointer5></allowedphotointer5>";
		try {
			if ( checkPi5.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi0.isSelected() && !checkPi6.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi5.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi6( ActionEvent event )
	{
		String yes = "<allowedphotointer6>YCbCr</allowedphotointer6>";
		String no = "<allowedphotointer6></allowedphotointer6>";
		try {
			if ( checkPi6.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi0.isSelected()
						&& !checkPi8.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi6.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changePi8( ActionEvent event )
	{
		String yes = "<allowedphotointer8>CIE L*a*b*</allowedphotointer8>";
		String no = "<allowedphotointer8></allowedphotointer8>";
		try {
			if ( checkPi8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkPi1.isSelected() && !checkPi2.isSelected() && !checkPi3.isSelected()
						&& !checkPi4.isSelected() && !checkPi5.isSelected() && !checkPi6.isSelected()
						&& !checkPi0.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkPi8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkBps schaltet diese bitspersample in der Konfiguration ein oder aus */
	@FXML
	void changeBps1( ActionEvent event )
	{
		String yes = "<allowedbitspersample1>1</allowedbitspersample1>";
		String no = "<allowedbitspersample1></allowedbitspersample1>";
		try {
			if ( checkBps1.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected() && !checkBps8.isSelected()
						&& !checkBps16.isSelected() && !checkBps32.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps1.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps2( ActionEvent event )
	{
		String yes = "<allowedbitspersample2>2</allowedbitspersample2>";
		String no = "<allowedbitspersample1>2</allowedbitspersample2>";
		try {
			if ( checkBps2.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps1.isSelected() && !checkBps4.isSelected() && !checkBps8.isSelected()
						&& !checkBps16.isSelected() && !checkBps32.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps2.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps4( ActionEvent event )
	{
		String yes = "<allowedbitspersample4>4</allowedbitspersample4>";
		String no = "<allowedbitspersample4></allowedbitspersample4>";
		try {
			if ( checkBps4.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps1.isSelected() && !checkBps8.isSelected()
						&& !checkBps16.isSelected() && !checkBps32.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps4.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps8( ActionEvent event )
	{
		String yes = "<allowedbitspersample8>8</allowedbitspersample8>";
		String no = "<allowedbitspersample8></allowedbitspersample8>";
		try {
			if ( checkBps8.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected() && !checkBps1.isSelected()
						&& !checkBps16.isSelected() && !checkBps32.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps8.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps16( ActionEvent event )
	{
		String yes = "<allowedbitspersample16>16</allowedbitspersample16>";
		String no = "<allowedbitspersample16></allowedbitspersample16>";
		try {
			if ( checkBps16.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected() && !checkBps8.isSelected()
						&& !checkBps1.isSelected() && !checkBps32.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps16.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@FXML
	void changeBps32( ActionEvent event )
	{
		String yes = "<allowedbitspersample32>32</allowedbitspersample32>";
		String no = "<allowedbitspersample32></allowedbitspersample32>";
		try {
			if ( checkBps32.isSelected() ) {
				Util.oldnewstring( no, yes, configFile );
				engine.load( "file:///" + configFile.getAbsolutePath() );
			} else {
				// abwaehlen nur moeglich wenn noch eines selected
				if ( !checkBps2.isSelected() && !checkBps4.isSelected() && !checkBps8.isSelected()
						&& !checkBps16.isSelected() && !checkBps1.isSelected() ) {
					String minOne = "Mindestens eine Variante muss erlaubt sein!";
					if ( locale.toString().startsWith( "fr" ) ) {
						minOne = "Au moins une variante doit être autorisée !";
					} else if ( locale.toString().startsWith( "en" ) ) {
						minOne = "At least one variant must be allowed!";
					}
					engine.loadContent( "<html><h2>" + minOne + "</h2></html>" );
					checkBps32.setSelected( true );
				} else {
					Util.oldnewstring( yes, no, configFile );
					engine.load( "file:///" + configFile.getAbsolutePath() );
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/* checkMultipage schaltet diese Multipage in der Konfiguration ein oder aus */
	@FXML
	void changeMultipage( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkTiles schaltet diese Tiles in der Konfiguration ein oder aus */
	@FXML
	void changeTiles( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

	/* checkSize schaltet diese Size in der Konfiguration ein oder aus */
	@FXML
	void changeSize( ActionEvent event )
	{
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
		engine.load( "file:///" + configFile.getAbsolutePath() );
	}

}