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

package ch.kostceco.tools.kostval.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

	Map<String, String>			configMap	= null;
	// TODO Hier alle Werte definieren, bei getConfig fuellen und dann nur noch
	// Wert gleichsetzen
	private TextResourceService	textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService(
			TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public Map<String, String> configMap( Locale locale, String logtype,
			File valDatei )
	{
		File logFile = new File( "LOGS.kost-val.log.xml" );

		try {
			File directoryOfConfigfile = new File(
					System.getenv( "USERPROFILE" ) + File.separator
							+ ".kost-val_2x" + File.separator
							+ "configuration" );
			File configFile = new File( directoryOfConfigfile + File.separator
					+ "kostval.conf.xml" );

			Document doc = null;

			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();

			Map<String, String> configMap = new HashMap<String, String>();

			// TODO Allgemeines

			// Gibt den Pfad des Arbeitsverzeichnisses zurueck. wenn leer
			// USERPROFILE/.kost-val_2x/temp_KOST-Val
			Boolean work = false;
			String pathtoworkdir = doc.getElementsByTagName( "pathtoworkdir" )
					.item( 0 ).getTextContent();
			if ( !pathtoworkdir.isEmpty() ) {
				File dir = new File( pathtoworkdir );
				if ( !dir.exists() ) {
					dir.mkdirs();
				}
				pathtoworkdir = dir.getAbsolutePath() + File.separator
						+ "temp_KOST-Val";
				if ( dir.canWrite() ) {
					work = true;
					File tmpDir = new File( pathtoworkdir );
					tmpDir.mkdirs();
				}
			}
			if ( !work ) {
				pathtoworkdir = System.getenv( "USERPROFILE" ) + File.separator
						+ ".kost-val_2x" + File.separator + "temp_KOST-Val";
				File dir = new File( pathtoworkdir );
				if ( !dir.exists() ) {
					dir.mkdirs();
				}
			}
			configMap.put( "PathToWorkDir", pathtoworkdir );

			// Gibt den Pfad des Standardinputs zurueck. wenn leer bleibt leer =
			// "Dieser PC"
			Boolean input = false;
			String standardinputdir = doc
					.getElementsByTagName( "standardinputdir" ).item( 0 )
					.getTextContent();
			if ( !standardinputdir.isEmpty() ) {
				File dir = new File( standardinputdir );
				if ( dir.exists() ) {
					input = true;
				}
			}
			if ( !input ) {
				standardinputdir = "";
			}
			configMap.put( "StandardInputDir", standardinputdir );

			// Gibt den Pfad des Logverzeichnisses zurueck. =
			// USERPROFILE/.kost-val_2x/logs
			String logs = System.getenv( "USERPROFILE" ) + File.separator
					+ ".kost-val_2x" + File.separator + "logs";
			File dir1 = new File( logs );
			if ( !dir1.exists() ) {
				dir1.mkdirs();
			}
			configMap.put( "PathToLogfile", logs );
			logFile = new File( logs + File.separator + valDatei.getName()
					+ ".kost-val.log.xml" );

			/*
			 * Angabe ob dargestellt werden soll, dass KOST-Val noch laeuft
			 * --xml (=no) zaehler anzeigen --max (=yes) auch "Windrad" --min
			 * (=nomin) zaehler anzeigen
			 */
			String showprogressonwork = "no";
			if ( logtype.equalsIgnoreCase( "--max" ) ) {
				showprogressonwork = "yes";
			} else if ( logtype.equalsIgnoreCase( "--min" ) ) {
				showprogressonwork = "nomin";
			}
			configMap.put( "ShowProgressOnWork", showprogressonwork );

			// Gibt an welche Fehler ignoriert werden sollen
			String ignore = doc.getElementsByTagName( "ignore" ).item( 0 )
					.getTextContent();
			configMap.put( "ignore", ignore );

			byte[] encoded;
			encoded = Files
					.readAllBytes( Paths.get( configFile.getAbsolutePath() ) );
			String config = new String( encoded, StandardCharsets.UTF_8 );

			// TODO Text

			// Gibt an ob pdfa validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesPdfa = "<pdfavalidation>&#x2713;</pdfavalidation>";
			String azPdfa = "<pdfavalidation>(&#x2713;)</pdfavalidation>";
			// String noPdfa = "<pdfavalidation>&#x2717;</pdfavalidation>";
			String pdfavalidation = "no";
			if ( config.contains( yesPdfa ) ) {
				pdfavalidation = "yes";
			} else if ( config.contains( azPdfa ) ) {
				pdfavalidation = "az";
			} else {
				pdfavalidation = "no";
			}
			configMap.put( "pdfaValidation", pdfavalidation );

			// Gibt an ob pdfa mit PDF Tools validiert werden soll
			String pdftools = doc.getElementsByTagName( "pdftools" ).item( 0 )
					.getTextContent();
			configMap.put( "pdftools", pdftools );

			// Gibt an ob pdfa mit PDF Tools im detail validiert werden soll
			String detail = doc.getElementsByTagName( "detail" ).item( 0 )
					.getTextContent();
			configMap.put( "detail", detail );

			// Gibt an ob pdfa mit callas validiert werden soll
			String callas = doc.getElementsByTagName( "callas" ).item( 0 )
					.getTextContent();
			configMap.put( "callas", callas );

			// N-Eintrag: Soll seitens callas ein Fehler (E) oder eine Warnung
			// (W) ausgegeben werden
			String nentry = doc.getElementsByTagName( "nentry" ).item( 0 )
					.getTextContent();
			configMap.put( "nentry", nentry );

			// Gibt an welche Konformitaeten erlaubt sind
			String pdfa1a = "no";
			if ( doc.getElementsByTagName( "pdfa1a" ).item( 0 ) != null ) {
				pdfa1a = doc.getElementsByTagName( "pdfa1a" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "pdfa1a", pdfa1a );

			String pdfa1b = "no";
			if ( doc.getElementsByTagName( "pdfa1b" ).item( 0 ) != null ) {
				pdfa1b = doc.getElementsByTagName( "pdfa1b" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "pdfa1b", pdfa1b );

			String pdfa2a = "no";
			if ( doc.getElementsByTagName( "pdfa2a" ).item( 0 ) != null ) {
				pdfa2a = doc.getElementsByTagName( "pdfa2a" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "pdfa2a", pdfa2a );

			String pdfa2b = "no";
			if ( doc.getElementsByTagName( "pdfa2b" ).item( 0 ) != null ) {
				pdfa2b = doc.getElementsByTagName( "pdfa2b" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "pdfa2b", pdfa2b );

			String pdfa2u = "no";
			if ( doc.getElementsByTagName( "pdfa2u" ).item( 0 ) != null ) {
				pdfa2u = doc.getElementsByTagName( "pdfa2u" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "pdfa2u", pdfa2u );

			// Gibt an ob die Schriften in pdfa validiert werden soll
			String pdfafont = doc.getElementsByTagName( "pdfafont" ).item( 0 )
					.getTextContent();
			configMap.put( "pdfafont", pdfafont );

			/*
			 * checkWarning3to2 validiert wenn eingeschaltet PDF/A-3 nach
			 * PDF/A-2 und ignoriert den Fehler betreffend der Version und gibt
			 * stattdessten eine Warnung aus.
			 */
			String warning3to2 = doc.getElementsByTagName( "warning3to2" )
					.item( 0 ).getTextContent();
			configMap.put( "warning3to2", warning3to2 );

			// Gibt an ob JBIG2 erlaubt ist oder nicht
			String jbig2allowed = doc.getElementsByTagName( "jbig2allowed" )
					.item( 0 ).getTextContent();
			configMap.put( "jbig2allowed", jbig2allowed );

			// Gibt an ob txt akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azTxt = "<txtvalidation>(&#x2713;)</txtvalidation>";
			String txtvalidation = "no";
			if ( config.contains( azTxt ) ) {
				txtvalidation = "az";
			} else {
				txtvalidation = "no";
			}
			configMap.put( "txtValidation", txtvalidation );

			// Gibt an ob pdf akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azPdf = "<pdfvalidation>(&#x2713;)</pdfvalidation>";
			String pdfvalidation = "no";
			if ( config.contains( azPdf ) ) {
				pdfvalidation = "az";
			} else {
				pdfvalidation = "no";
			}
			configMap.put( "pdfValidation", pdfvalidation );

			// TODO Bild

			// Gibt an ob jp2 validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesJp2 = "<jp2validation>&#x2713;</jp2validation>";
			String azJp2 = "<jp2validation>(&#x2713;)</jp2validation>";
			String jp2validation = "no";
			if ( config.contains( yesJp2 ) ) {
				jp2validation = "yes";
			} else if ( config.contains( azJp2 ) ) {
				jp2validation = "az";
			} else {
				jp2validation = "no";
			}
			configMap.put( "jp2Validation", jp2validation );

			// Gibt an ob jpeg validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesJpeg = "<jpegvalidation>&#x2713;</jpegvalidation>";
			String azJpeg = "<jpegvalidation>(&#x2713;)</jpegvalidation>";
			String jpegvalidation = "no";
			if ( config.contains( yesJpeg ) ) {
				jpegvalidation = "yes";
			} else if ( config.contains( azJpeg ) ) {
				jpegvalidation = "az";
			} else {
				jpegvalidation = "no";
			}
			configMap.put( "jpegValidation", jpegvalidation );

			// Gibt an ob png validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesPng = "<pngvalidation>&#x2713;</pngvalidation>";
			String azPng = "<pngvalidation>(&#x2713;)</pngvalidation>";
			String pngvalidation = "no";
			if ( config.contains( yesPng ) ) {
				pngvalidation = "yes";
			} else if ( config.contains( azPng ) ) {
				pngvalidation = "az";
			} else {
				pngvalidation = "no";
			}
			configMap.put( "pngValidation", pngvalidation );

			// Gibt an ob tiff validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesTiff = "<tiffvalidation>&#x2713;</tiffvalidation>";
			String azTiff = "<tiffvalidation>(&#x2713;)</tiffvalidation>";
			String tiffvalidation = "no";
			if ( config.contains( yesTiff ) ) {
				tiffvalidation = "yes";
			} else if ( config.contains( azTiff ) ) {
				tiffvalidation = "az";
			} else {
				tiffvalidation = "no";
			}
			configMap.put( "tiffValidation", tiffvalidation );

			// Gibt die Komprimierung aus, welche im TIFF vorkommen duerfen.
			String allowedcompression1 = "0";
			if ( doc.getElementsByTagName( "allowedcompression1" )
					.item( 0 ) != null ) {
				allowedcompression1 = doc
						.getElementsByTagName( "allowedcompression1" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression2 = "0";
			if ( doc.getElementsByTagName( "allowedcompression2" )
					.item( 0 ) != null ) {
				allowedcompression2 = doc
						.getElementsByTagName( "allowedcompression2" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression3 = "0";
			if ( doc.getElementsByTagName( "allowedcompression3" )
					.item( 0 ) != null ) {
				allowedcompression3 = doc
						.getElementsByTagName( "allowedcompression3" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression4 = "0";
			if ( doc.getElementsByTagName( "allowedcompression4" )
					.item( 0 ) != null ) {
				allowedcompression4 = doc
						.getElementsByTagName( "allowedcompression4" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression5 = "0";
			if ( doc.getElementsByTagName( "allowedcompression5" )
					.item( 0 ) != null ) {
				allowedcompression5 = doc
						.getElementsByTagName( "allowedcompression5" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression7 = "0";
			if ( doc.getElementsByTagName( "allowedcompression7" )
					.item( 0 ) != null ) {
				allowedcompression7 = doc
						.getElementsByTagName( "allowedcompression7" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression8 = "0";
			if ( doc.getElementsByTagName( "allowedcompression8" )
					.item( 0 ) != null ) {
				allowedcompression8 = doc
						.getElementsByTagName( "allowedcompression8" ).item( 0 )
						.getTextContent();
			}
			String allowedcompression32773 = "0";
			if ( doc.getElementsByTagName( "allowedcompression32773" )
					.item( 0 ) != null ) {
				allowedcompression32773 = doc
						.getElementsByTagName( "allowedcompression32773" )
						.item( 0 ).getTextContent();
			}
			configMap.put( "AllowedCompression1", allowedcompression1 );
			configMap.put( "AllowedCompression2", allowedcompression2 );
			configMap.put( "AllowedCompression3", allowedcompression3 );
			configMap.put( "AllowedCompression4", allowedcompression4 );
			configMap.put( "AllowedCompression5", allowedcompression5 );
			configMap.put( "AllowedCompression7", allowedcompression7 );
			configMap.put( "AllowedCompression8", allowedcompression8 );
			configMap.put( "AllowedCompression32773", allowedcompression32773 );

			// Gibt die Farbraum aus, welche im TIFF vorkommen duerfen.
			String allowedphotointer0 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer0" )
					.item( 0 ) != null ) {
				allowedphotointer0 = doc
						.getElementsByTagName( "allowedphotointer0" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer1 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer1" )
					.item( 0 ) != null ) {
				allowedphotointer1 = doc
						.getElementsByTagName( "allowedphotointer1" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer2 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer2" )
					.item( 0 ) != null ) {
				allowedphotointer2 = doc
						.getElementsByTagName( "allowedphotointer2" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer3 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer3" )
					.item( 0 ) != null ) {
				allowedphotointer3 = doc
						.getElementsByTagName( "allowedphotointer3" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer4 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer4" )
					.item( 0 ) != null ) {
				allowedphotointer4 = doc
						.getElementsByTagName( "allowedphotointer4" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer5 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer5" )
					.item( 0 ) != null ) {
				allowedphotointer5 = doc
						.getElementsByTagName( "allowedphotointer5" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer6 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer6" )
					.item( 0 ) != null ) {
				allowedphotointer6 = doc
						.getElementsByTagName( "allowedphotointer6" ).item( 0 )
						.getTextContent();
			}
			String allowedphotointer8 = "0";
			if ( doc.getElementsByTagName( "allowedphotointer8" )
					.item( 0 ) != null ) {
				allowedphotointer8 = doc
						.getElementsByTagName( "allowedphotointer8" ).item( 0 )
						.getTextContent();
			}
			configMap.put( "AllowedPhotointer0", allowedphotointer0 );
			configMap.put( "AllowedPhotointer1", allowedphotointer1 );
			configMap.put( "AllowedPhotointer2", allowedphotointer2 );
			configMap.put( "AllowedPhotointer3", allowedphotointer3 );
			configMap.put( "AllowedPhotointer4", allowedphotointer4 );
			configMap.put( "AllowedPhotointer5", allowedphotointer5 );
			configMap.put( "AllowedPhotointer6", allowedphotointer6 );
			configMap.put( "AllowedPhotointer8", allowedphotointer8 );

			// Gibt die BitsPerSample aus, welche im TIFF vorkommen duerfen.
			String allowedbitspersample1 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample1" )
					.item( 0 ) != null ) {
				allowedbitspersample1 = doc
						.getElementsByTagName( "allowedbitspersample1" )
						.item( 0 ).getTextContent();
			}
			String allowedbitspersample2 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample2" )
					.item( 0 ) != null ) {
				allowedbitspersample2 = doc
						.getElementsByTagName( "allowedbitspersample2" )
						.item( 0 ).getTextContent();
			}
			String allowedbitspersample4 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample4" )
					.item( 0 ) != null ) {
				allowedbitspersample4 = doc
						.getElementsByTagName( "allowedbitspersample4" )
						.item( 0 ).getTextContent();
			}
			String allowedbitspersample8 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample8" )
					.item( 0 ) != null ) {
				allowedbitspersample8 = doc
						.getElementsByTagName( "allowedbitspersample8" )
						.item( 0 ).getTextContent();
			}
			String allowedbitspersample16 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample16" )
					.item( 0 ) != null ) {
				allowedbitspersample16 = doc
						.getElementsByTagName( "allowedbitspersample16" )
						.item( 0 ).getTextContent();
			}
			String allowedbitspersample32 = "0";
			if ( doc.getElementsByTagName( "allowedbitspersample32" )
					.item( 0 ) != null ) {
				allowedbitspersample32 = doc
						.getElementsByTagName( "allowedbitspersample32" )
						.item( 0 ).getTextContent();
			}
			configMap.put( "AllowedBitspersample1", allowedbitspersample1 );
			configMap.put( "AllowedBitspersample2", allowedbitspersample2 );
			configMap.put( "AllowedBitspersample4", allowedbitspersample4 );
			configMap.put( "AllowedBitspersample8", allowedbitspersample8 );
			configMap.put( "AllowedBitspersample16", allowedbitspersample16 );
			configMap.put( "AllowedBitspersample32", allowedbitspersample32 );

			// Gibt an ob Multipage im TIFF vorkommen duerfen.
			String allowedmultipage = doc
					.getElementsByTagName( "allowedmultipage" ).item( 0 )
					.getTextContent();
			configMap.put( "AllowedMultipage", allowedmultipage );

			// Gibt an ob Tiles im TIFF vorkommen duerfen.
			String allowedtiles = doc.getElementsByTagName( "allowedtiles" )
					.item( 0 ).getTextContent();
			configMap.put( "AllowedTiles", allowedtiles );

			// Gibt an ob Giga-TIFF vorkommen duerfen.
			String allowedsize = doc.getElementsByTagName( "allowedsize" )
					.item( 0 ).getTextContent();
			configMap.put( "AllowedSize", allowedsize );

			// TODO Audio

			// Gibt an ob flac akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azFlac = "<flacvalidation>(&#x2713;)</flacvalidation>";
			String flacvalidation = "no";
			if ( config.contains( azFlac ) ) {
				flacvalidation = "az";
			} else {
				flacvalidation = "no";
			}
			configMap.put( "flacValidation", flacvalidation );

			// Gibt an ob wave akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azWave = "<wavevalidation>(&#x2713;)</wavevalidation>";
			String wavevalidation = "no";
			if ( config.contains( azWave ) ) {
				wavevalidation = "az";
			} else {
				wavevalidation = "no";
			}
			configMap.put( "waveValidation", wavevalidation );

			// Gibt an ob mp3 akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azMp3 = "<mp3validation>(&#x2713;)</mp3validation>";
			String mp3validation = "no";
			if ( config.contains( azMp3 ) ) {
				mp3validation = "az";
			} else {
				mp3validation = "no";
			}
			configMap.put( "mp3Validation", mp3validation );

			// TODO Video

			// Gibt an ob mkv akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azMkv = "<mkvvalidation>(&#x2713;)</mkvvalidation>";
			String mkvvalidation = "no";
			if ( config.contains( azMkv ) ) {
				mkvvalidation = "az";
			} else {
				mkvvalidation = "no";
			}
			configMap.put( "mkvValidation", mkvvalidation );

			// Gibt an ob mp4 akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azMp4 = "<mp4validation>(&#x2713;)</mp4validation>";
			String mp4validation = "no";
			if ( config.contains( azMp4 ) ) {
				mp4validation = "az";
			} else {
				mp4validation = "no";
			}
			configMap.put( "mp4Validation", mp4validation );

			// TODO Daten

			// Gibt an ob xml validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesXml = "<xmlvalidation>&#x2713;</xmlvalidation>";
			String azXml = "<xmlvalidation>(&#x2713;)</xmlvalidation>";
			String xmlvalidation = "no";
			if ( config.contains( yesXml ) ) {
				xmlvalidation = "yes";
			} else if ( config.contains( azXml ) ) {
				xmlvalidation = "az";
			} else {
				xmlvalidation = "no";
			}
			configMap.put( "xmlValidation", xmlvalidation );

			// Gibt an ob siard validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesSiard = "<siardvalidation>&#x2713;</siardvalidation>";
			String azSiard = "<siardvalidation>(&#x2713;)</siardvalidation>";
			String siardvalidation = "no";
			if ( config.contains( yesSiard ) ) {
				siardvalidation = "yes";
			} else if ( config.contains( azSiard ) ) {
				siardvalidation = "az";
			} else {
				siardvalidation = "no";
			}
			configMap.put( "siardValidation", siardvalidation );

			// Gibt an ob siard 1.0 validiert werden soll
			String siard10 = doc.getElementsByTagName( "siard10" ).item( 0 )
					.getTextContent();
			configMap.put( "siard10", siard10 );

			// Gibt an ob siard 2.1 validiert werden soll
			String siard21 = doc.getElementsByTagName( "siard21" ).item( 0 )
					.getTextContent();
			configMap.put( "siard21", siard21 );

			// Gibt an ob csv akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azCsv = "<csvvalidation>(&#x2713;)</csvvalidation>";
			String csvvalidation = "no";
			if ( config.contains( azCsv ) ) {
				csvvalidation = "az";
			} else {
				csvvalidation = "no";
			}
			configMap.put( "csvValidation", csvvalidation );

			// Gibt an ob xlsx akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azXlsx = "<xlsxvalidation>(&#x2713;)</xlsxvalidation>";
			String xlsxvalidation = "no";
			if ( config.contains( azXlsx ) ) {
				xlsxvalidation = "az";
			} else {
				xlsxvalidation = "no";
			}
			configMap.put( "xlsxValidation", xlsxvalidation );

			// Gibt an ob ods akzeptiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String azOds = "<odsvalidation>(&#x2713;)</odsvalidation>";
			String odsvalidation = "no";
			if ( config.contains( azOds ) ) {
				odsvalidation = "az";
			} else {
				odsvalidation = "no";
			}
			configMap.put( "odsValidation", odsvalidation );

			// otherformats
			// Gibt eine Liste mit den PUIDs aus, welche auch akzeptiert sind.
			String otherformats = doc.getElementsByTagName( "otherformats" )
					.item( 0 ).getTextContent();
			configMap.put( "otherformats", otherformats );

			// hash
			/* Hashwert von Dateien berechnen und ausgeben. Leer bedeutet keine Berechnung und Ausgabe []
			 * 
			 * [] MD5, SHA-1, SHA-256, SHA-512
			 * */
			String hash = doc.getElementsByTagName( "hash" )
					.item( 0 ).getTextContent();
			configMap.put( "hash", hash );

			// TODO SIP

			// Gibt an ob eCH-0160 validiert werden soll
			/* durch die Sonderzeichen muss es anders ausgelesen werden */
			String yesEch0160 = "<ech0160validation>&#x2713;</ech0160validation>";
			String ech0160validation = "no";
			if ( config.contains( yesEch0160 ) ) {
				ech0160validation = "yes";
			} else {
				ech0160validation = "no";
			}
			configMap.put( "ech0160validation", ech0160validation );

			// Gibt eine Liste mit den PUIDs aus, welche im SIP vorkommen
			// duerfen.
			String allowedformats = doc.getElementsByTagName( "allowedformats" )
					.item( 0 ).getTextContent();
			configMap.put( "allowedformats", allowedformats );

			// Gibt die Maximal erlaubte Laenge eines Pfades in der SIP-Datei
			// aus.
			String allowedlengthofpaths = doc
					.getElementsByTagName( "allowedlengthofpaths" ).item( 0 )
					.getTextContent();
			configMap.put( "MaximumPathLength", allowedlengthofpaths );

			// Die Einschraenkung des SIP-Namen ist konfigurierbar
			String allowedsipname = doc.getElementsByTagName( "allowedsipname" )
					.item( 0 ).getTextContent();
			configMap.put( "AllowedSipName", allowedsipname );

			// System.out.println("Value is b : " + (map.get("key") == b));
			bis.close();
			return configMap;

		} catch ( FileNotFoundException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
			String error = e.getMessage() + " (FileNotFoundException)";
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		} catch ( ParserConfigurationException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
			String error = e.getMessage() + " (ParserConfigurationException)";
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		} catch ( SAXException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
			String error = e.getMessage() + " (SAXException)";
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		} catch ( IOException e ) {
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
			String error = e.getMessage() + " (IOException)";
			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		}
		return configMap;
	}

}
