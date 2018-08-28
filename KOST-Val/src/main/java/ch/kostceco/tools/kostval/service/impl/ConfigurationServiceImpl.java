/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.service.impl;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// http://commons.apache.org/proper/commons-configuration/userguide/upgradeto2_0.html
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

	private static final Logger	LOGGER		= new Logger( ConfigurationServiceImpl.class );
	XMLConfiguration						config		= null;
	Map<String, String>					configMap	= null;
	// TODO Hier alle Werte definieren, bei getConfig füllen und dann nur noch Wert gleichsetzen
	private TextResourceService	textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public Map<String, String> configMap()
	{
		if ( this.config == null ) {

			try {
				File directoryOfConfigfile = new File( System.getenv( "USERPROFILE" ) + File.separator
						+ ".kost-val" + File.separator + "configuration" );
				File configFile = new File( directoryOfConfigfile + File.separator + "kostval.conf.xml" );
				InputStream inputStream;
				inputStream = new FileInputStream( configFile );

				config = new BasicConfigurationBuilder<>( XMLConfiguration.class ).configure(
						new Parameters().xml() ).getConfiguration();
				FileHandler fh = new FileHandler( config );
				fh.load( inputStream );

				Map<String, String> configMap = new HashMap<String, String>();

				/** Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird zum Entpacken des
				 * .zip-Files verwendet.
				 * 
				 * Pfad des Arbeitsverzeichnisses = USERPROFILE/.kost-val/temp_KOST-Val */
				String pathtoworkdir = System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
						+ File.separator + "temp_KOST-Val";
				File dir = new File( pathtoworkdir );
				if ( !dir.exists() ) {
					dir.mkdirs();
				}

				/** Gibt den Pfad des Logverzeichnisses zurück.
				 * 
				 * Pfad des Logverzeichnisses = USERPROFILE/.kost-val/logs */
				String logs = System.getenv( "USERPROFILE" ) + File.separator + ".kost-val"
						+ File.separator + "logs";
				File dir1 = new File( logs );
				if ( !dir1.exists() ) {
					dir1.mkdirs();
				}

				/** Gibt den Pfad des Logverzeichnisses zurück.
				 * 
				 * Pfad des Logverzeichnisses = configuration\KaD_SignatureFile_V72.xml */
				String droid = "configuration" + File.separator + "KaD_SignatureFile_V72.xml";

				// Gibt den Pfad des Arbeitsverzeichnisses zurück.
				configMap.put( "PathToWorkDir", pathtoworkdir );
				// Gibt den Pfad des Logverzeichnisses zurück.
				configMap.put( "PathToLogfile", logs );
				// Gibt den Namen des DROID Signature Files zurück.
				configMap.put( "PathToDroidSignatureFile", droid );
				// Angabe ob dargestellt werden soll, dass KOST-Val noch läuft */
				configMap.put( "ShowProgressOnWork", config.getString( "showprogressonwork" ) );
				// Gibt an ob pdfa mit PDF Tools validiert werden soll
				configMap.put( "pdftools", config.getString( "pdfa.pdftools" ) );
				// Gibt an ob pdfa mit PDF Tools im detail validiert werden soll
				configMap.put( "detail", config.getString( "pdfa.detail" ) );
				// Gibt an ob pdfa mit callas validiert werden soll
				configMap.put( "callas", config.getString( "pdfa.callas" ) );
				// N-Eintrag: Soll seitens callas ein Fehler (E) oder eine Warnung (W) ausgegeben werden
				configMap.put( "nentry", config.getString( "pdfa.nentry" ) );
				// Gibt an welche 1erKonformität mindestens erreicht werden muss
				configMap.put( "pdfa1", config.getString( "pdfa.pdfa1" ) );
				// Gibt an welche 2erKonformität mindestens erreicht werden muss
				configMap.put( "pdfa2", config.getString( "pdfa.pdfa2" ) );
				// Gibt an ob die Schriften in pdfa validiert werden soll
				configMap.put( "pdfafont", config.getString( "pdfa.pdfafont" ) );
				// Gibt an ob die Bilder in pdfa validiert werden soll
				configMap.put( "pdfaimage", config.getString( "pdfa.pdfaimage" ) );
				// Gibt an ob JBIG2 erlaubt ist oder nicht
				configMap.put( "jbig2allowed", config.getString( "pdfa.jbig2allowed" ) );
				// Gibt an ob siard validiert werden soll
				configMap.put( "siardValidation", config.getString( "siard.siardvalidation" ) );
				// Gibt an ob jp2 validiert werden soll
				configMap.put( "jp2Validation", config.getString( "jp2.jp2validation" ) );
				// Gibt an ob jpeg validiert werden soll
				configMap.put( "jpegValidation", config.getString( "jpeg.jpegvalidation" ) );
				// Gibt an welche Fehler ignoriert werden sollen
				configMap.put( "ignore", config.getString( "ignoreerror.ignore" ) );
				// Gibt eine Liste mit den PUIDs aus, welche im SIP vorkommen dürfen.
				configMap.put( "allowedformats", config.getString( "sip.allowedformats" ) );
				// Gibt die Maximal erlaubte Länge eines Pfades in der SIP-Datei aus.
				configMap.put( "MaximumPathLength", config.getString( "sip.allowedlengthofpaths" ) );
				// Die Einschränkung des SIP-Namen ist konfigurierbar
				configMap.put( "AllowedSipName", config.getString( "sip.allowedsipname" ) );
				// Gibt an ob tiff validiert werden soll
				configMap.put( "tiffValidation", config.getString( "tiff.tiffvalidation" ) );
				// Gibt die Komprimierung aus, welche im TIFF vorkommen dürfen.
				configMap.put( "AllowedCompression1",
						config.getString( "tiff.allowedcompression.allowedcompression1" ) );
				configMap.put( "AllowedCompression2",
						config.getString( "tiff.allowedcompression.allowedcompression2" ) );
				configMap.put( "AllowedCompression3",
						config.getString( "tiff.allowedcompression.allowedcompression3" ) );
				configMap.put( "AllowedCompression4",
						config.getString( "tiff.allowedcompression.allowedcompression4" ) );
				configMap.put( "AllowedCompression5",
						config.getString( "tiff.allowedcompression.allowedcompression5" ) );
				configMap.put( "AllowedCompression7",
						config.getString( "tiff.allowedcompression.allowedcompression7" ) );
				configMap.put( "AllowedCompression8",
						config.getString( "tiff.allowedcompression.allowedcompression8" ) );
				configMap.put( "AllowedCompression32773",
						config.getString( "tiff.allowedcompression.allowedcompression32773" ) );
				// Gibt die Farbraum aus, welche im TIFF vorkommen dürfen.
				configMap.put( "AllowedPhotointer0",
						config.getString( "tiff.allowedphotointer.allowedphotointer0" ) );
				configMap.put( "AllowedPhotointer1",
						config.getString( "tiff.allowedphotointer.allowedphotointer1" ) );
				configMap.put( "AllowedPhotointer2",
						config.getString( "tiff.allowedphotointer.allowedphotointer2" ) );
				configMap.put( "AllowedPhotointer3",
						config.getString( "tiff.allowedphotointer.allowedphotointer3" ) );
				configMap.put( "AllowedPhotointer4",
						config.getString( "tiff.allowedphotointer.allowedphotointer4" ) );
				configMap.put( "AllowedPhotointer5",
						config.getString( "tiff.allowedphotointer.allowedphotointer5" ) );
				configMap.put( "AllowedPhotointer6",
						config.getString( "tiff.allowedphotointer.allowedphotointer6" ) );
				configMap.put( "AllowedPhotointer8",
						config.getString( "tiff.allowedphotointer.allowedphotointer8" ) );
				// Gibt die BitsPerSample aus, welche im TIFF vorkommen dürfen.
				configMap.put( "AllowedBitspersample1",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample1" ) );
				configMap.put( "AllowedBitspersample2",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample2" ) );
				configMap.put( "AllowedBitspersample4",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample4" ) );
				configMap.put( "AllowedBitspersample8",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample8" ) );
				configMap.put( "AllowedBitspersample16",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample16" ) );
				configMap.put( "AllowedBitspersample32",
						config.getString( "tiff.allowedbitspersample.allowedbitspersample32" ) );
				// Gibt an ob Multipage im TIFF vorkommen dürfen.
				configMap
						.put( "AllowedMultipage", config.getString( "tiff.allowedother.allowedmultipage" ) );
				// Gibt an ob Tiles im TIFF vorkommen dürfen.
				configMap.put( "AllowedTiles", config.getString( "tiff.allowedother.allowedtiles" ) );
				// Gibt an ob Giga-TIFF vorkommen dürfen.
				configMap.put( "AllowedSize", config.getString( "tiff.allowedother.allowedsize" ) );

				// System.out.println("Value is b : " + (map.get("key") == b));

				return configMap;

			} catch ( ConfigurationException cex ) {
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
				System.exit( 1 );
			} catch ( FileNotFoundException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit( 1 );
			}
		}
		return configMap;
	}

}
