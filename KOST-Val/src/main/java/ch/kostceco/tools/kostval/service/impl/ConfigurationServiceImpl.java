/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2017 Claire Roethlisberger (KOST-CECO), Christian
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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

	private static final Logger	LOGGER	= new Logger( ConfigurationServiceImpl.class );
	XMLConfiguration						config	= null;
	private TextResourceService	textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	private XMLConfiguration getConfig()
	{
		if ( this.config == null ) {

			try {

				String path = "configuration/kostval.conf.xml";

				URL locationOfJar = KOSTVal.class.getProtectionDomain().getCodeSource().getLocation();
				String locationOfJarPath = locationOfJar.getPath();

				if ( locationOfJarPath.endsWith( ".jar" ) ) {
					File file = new File( locationOfJarPath );
					String fileParent = file.getParent();
					path = fileParent + "/" + path;
				}

				config = new XMLConfiguration( path );

			} catch ( ConfigurationException e ) {
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_1 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_2 ) );
				LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
						+ getTextResourceService().getText( MESSAGE_XML_CONFIGURATION_ERROR_3 ) );
				System.exit( 1 );
			}
		}
		return config;
	}

	@Override
	public String getPathToWorkDir()
	{
		/** Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird zum Entpacken des
		 * .zip-Files verwendet.
		 * 
		 * @return Pfad des Arbeitsverzeichnisses */
		Object prop = getConfig().getProperty( "pathtoworkdir" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pathtoworkdir";
		return error;
	}

	@Override
	public String getPathToLogfile()
	{
		/** Gibt den Pfad des Logverzeichnisses zurück.
		 * 
		 * @return Pfad des Logverzeichnisses */
		Object prop = getConfig().getProperty( "pathtologfile" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pathtologfile";
		return error;
	}

	@Override
	public String getPathToDroidSignatureFile()
	{
		Object prop = getConfig().getProperty( "pathtodroidsignature" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pathtodroidsignature";
		return error;
	}

	@Override
	public String getShowProgressOnWork()
	{
		/** Angabe ob dargestellt werden soll, dass KOST-Val noch läuft */
		Object prop = getConfig().getProperty( "showprogressonwork" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing showprogressonwork";
		return error;
	}

	/*--- PDF/A ---------------------------------------------------------------------*/
	@Override
	public String pdftools()
	{
		Object prop = getConfig().getProperty( "pdfa.pdftools" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.pdftools";
		return error;
	}

	@Override
	public String callas()
	{
		Object prop = getConfig().getProperty( "pdfa.callas" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.callas";
		return error;
	}

	@Override
	public String nentry()
	{
		Object prop = getConfig().getProperty( "pdfa.nentry" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.nentry";
		return error;
	}

	@Override
	public String pdfa1()
	{
		Object prop = getConfig().getProperty( "pdfa.pdfa1" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.pdfa1";
		return error;
	}

	@Override
	public String pdfa2()
	{
		Object prop = getConfig().getProperty( "pdfa.pdfa2" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.pdfa2";
		return error;
	}

	@Override
	public String pdfaimage()
	{
		Object prop = getConfig().getProperty( "pdfa.pdfaimage" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.pdfaimage";
		return error;
	}

	@Override
	public String jbig2allowed()
	{
		Object prop = getConfig().getProperty( "pdfa.jbig2allowed" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing pdfa.jbig2allowed";
		return error;
	}

	/*--- SIARD ---------------------------------------------------------------------*/
	@Override
	public String siardValidation()
	{
		Object prop = getConfig().getProperty( "siard.siardvalidation" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing siard.siardvalidation";
		return error;
	}

	@Override
	public String siardFrowValidation()
	{
		Object prop = getConfig().getProperty( "siard.frowvalidation" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing siard.frowvalidation";
		return error;
	}

	@Override
	public int getTableRowsLimit()
	{
		/** Gibt die maximale Anzahl von Rows zurück. Dieser Wert wird in Modul H verwendet. Module H
		 * validiert die table.xml Dateien gegen ihre table.xsd Schemas. Wenn ein Schema <xs:element
		 * name="row" type="rowType" minOccurs="0" maxOccurs="unbounded"/> in minOccurs oder maxOccurs
		 * hohe Zahlenwerte enthält, führt die Validierung zu einem java.lang.OutOfMemoryError. Da
		 * dieser Error nicht aufgefangen werden kann, werden vor der Validierung die Rows der Tabelle
		 * gezählt. Die ermittelte Zahl darf nicht über dem hier zurückgegebenen Wert liegen. */
		int value = 20000;
		Object prop = getConfig().getProperty( "siard.table-rows-limit" );
		if ( prop != null ) {
			try {
				value = Integer.valueOf( prop.toString() ).intValue();
			} catch ( NumberFormatException e ) {
				// Do nothing
			}
		}
		return value;
	}

	/*--- JP2 ---------------------------------------------------------------------*/
	@Override
	public String jp2Validation()
	{
		Object prop = getConfig().getProperty( "jp2.jp2validation" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing jp2.jp2validation";
		return error;
	}

	/*--- JPEG ---------------------------------------------------------------------*/
	@Override
	public String jpegValidation()
	{
		Object prop = getConfig().getProperty( "jpeg.jpegvalidation" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing jpeg.jpegvalidation";
		return error;
	}

	/*--- SIP ---------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getAllowedPuids()
	{
		Map<String, String> result = new HashMap<String, String>();
		List<HierarchicalConfiguration> fields = getConfig().configurationsAt(
				"sip.allowedformats.allowedformat" );
		for ( Iterator<HierarchicalConfiguration> it = fields.iterator(); it.hasNext(); ) {
			HierarchicalConfiguration sub = it.next();
			// sub contains now all data about a single field
			String fieldPuid = sub.getString( "puid" );
			String fieldExt = sub.getString( "extension" );
			result.put( fieldPuid, fieldExt );
		}
		return result;
	}

	@Override
	public Integer getMaximumPathLength()
	{
		Object prop = getConfig().getProperty( "sip.allowedlengthofpaths" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			Integer intValue = new Integer( value );
			return intValue;
		}
		return 99999;
	}

	/** Die Einschränkung des SIP-Namen ist konfigurierbar -> getAllowedSipName */
	@Override
	public String getAllowedSipName()
	{
		Object prop = getConfig().getProperty( "sip.allowedsipname" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		} else {
			LOGGER.logError( getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
					+ getTextResourceService().getText( MESSAGE_XML_AC_INVALIDREGEX ) );
		}
		String error = "Configuration-Error: Missing sip.allowedsipname";
		return error;
	}

	/*--- TIFF ---------------------------------------------------------------------*/
	@Override
	public String tiffValidation()
	{
		Object prop = getConfig().getProperty( "tiff.tiffvalidation" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	// AllowedCompression
	@Override
	public String getAllowedCompression1()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression1" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression1";
		return error;
	}

	@Override
	public String getAllowedCompression2()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression2" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression2";
		return error;
	}

	@Override
	public String getAllowedCompression3()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression3" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression3";
		return error;
	}

	@Override
	public String getAllowedCompression4()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression4" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression4";
		return error;
	}

	@Override
	public String getAllowedCompression5()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression5" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression5";
		return error;
	}

	@Override
	public String getAllowedCompression7()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression7" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression7";
		return error;
	}

	@Override
	public String getAllowedCompression8()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression8" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression8";
		return error;
	}

	@Override
	public String getAllowedCompression32773()
	{
		Object prop = getConfig().getProperty( "tiff.allowedcompression.allowedcompression32773" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedcompression.allowedcompression32773";
		return error;
	}

	// AllowedPhotointer
	@Override
	public String getAllowedPhotointer0()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer0" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer0";
		return error;
	}

	@Override
	public String getAllowedPhotointer1()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer1" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer1";
		return error;
	}

	@Override
	public String getAllowedPhotointer2()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer2" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer2";
		return error;
	}

	@Override
	public String getAllowedPhotointer3()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer3" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer3";
		return error;
	}

	@Override
	public String getAllowedPhotointer4()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer4" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer4";
		return error;
	}

	@Override
	public String getAllowedPhotointer5()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer5" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer5";
		return error;
	}

	@Override
	public String getAllowedPhotointer6()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer6" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer6";
		return error;
	}

	@Override
	public String getAllowedPhotointer8()
	{
		Object prop = getConfig().getProperty( "tiff.allowedphotointer.allowedphotointer8" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedphotointer.allowedphotointer8";
		return error;
	}

	// AllowedBitspersample
	@Override
	public String getAllowedBitspersample1()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample1" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample1";
		return error;
	}

	@Override
	public String getAllowedBitspersample2()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample2" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample2";
		return error;
	}

	@Override
	public String getAllowedBitspersample4()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample4" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample4";
		return error;
	}

	@Override
	public String getAllowedBitspersample8()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample8" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample8";
		return error;
	}

	@Override
	public String getAllowedBitspersample16()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample16" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample16";
		return error;
	}

	@Override
	public String getAllowedBitspersample32()
	{
		Object prop = getConfig().getProperty( "tiff.allowedbitspersample.allowedbitspersample32" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedbitspersample.allowedbitspersample32";
		return error;
	}

	// AllowedMultipage
	@Override
	public String getAllowedMultipage()
	{
		Object prop = getConfig().getProperty( "tiff.allowedother.allowedmultipage" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedother.allowedmultipage";
		return error;
	}

	// AllowedTiles
	@Override
	public String getAllowedTiles()
	{
		Object prop = getConfig().getProperty( "tiff.allowedother.allowedtiles" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedother.allowedtiles";
		return error;
	}

	// AllowedSize
	@Override
	public String getAllowedSize()
	{
		Object prop = getConfig().getProperty( "tiff.allowedother.allowedsize" );
		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		String error = "Configuration-Error: Missing tiff.allowedother.allowedsize";
		return error;
	}
}
