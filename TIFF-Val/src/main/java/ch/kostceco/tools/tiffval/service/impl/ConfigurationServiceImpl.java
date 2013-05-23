/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Tagged Image File Format (TIFF).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.tiffval.service.impl;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import ch.kostceco.tools.tiffval.TIFFVal;
import ch.kostceco.tools.tiffval.logging.Logger;
import ch.kostceco.tools.tiffval.service.ConfigurationService;
import ch.kostceco.tools.tiffval.service.TextResourceService;

public class ConfigurationServiceImpl implements ConfigurationService
{

	private static final Logger	LOGGER	= new Logger(
												ConfigurationServiceImpl.class );
	XMLConfiguration			config	= null;
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

				String path = "configuration/TIFFVal.conf.xml";

				URL locationOfJar = TIFFVal.class.getProtectionDomain()
						.getCodeSource().getLocation();
				String locationOfJarPath = locationOfJar.getPath();

				if ( locationOfJarPath.endsWith( ".jar" ) ) {
					File file = new File( locationOfJarPath );
					String fileParent = file.getParent();
					path = fileParent + "/" + path;
				}

				config = new XMLConfiguration( path );

			} catch ( ConfigurationException e ) {
				System.out
						.print( "\r                                                                                                                                     " );
				System.out.flush();
				System.out.print( "\r" );
				System.out.flush();

				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_CONFIGURATION_ERROR_1 ) );
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_CONFIGURATION_ERROR_2 ) );
				LOGGER.logInfo( getTextResourceService().getText(
						MESSAGE_CONFIGURATION_ERROR_3 ) );
				System.exit( 1 );
			}
		}
		return config;
	}

	@Override
	public String getAllowedCompression1() 
	{
		Object prop = getConfig().getProperty( "allowedcompression1" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression2() 
	{
		Object prop = getConfig().getProperty( "allowedcompression2" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression3() 
	{
		Object prop = getConfig().getProperty( "allowedcompression3" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression4() 
	{
		Object prop = getConfig().getProperty( "allowedcompression4" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression5() 
	{
		Object prop = getConfig().getProperty( "allowedcompression5" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression7() 
	{
		Object prop = getConfig().getProperty( "allowedcompression7" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression8() 
	{
		Object prop = getConfig().getProperty( "allowedcompression8" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getAllowedCompression32773() 
	{
		Object prop = getConfig().getProperty( "allowedcompression32773" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getPathToWorkDir()
	{
		Object prop = getConfig().getProperty( "pathtoworkdir" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getPathToJhoveJar()
	{
		Object prop = getConfig().getProperty( "pathtojhovejar" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getPathToJhoveOutput()
	{
		Object prop = getConfig().getProperty( "pathtojhoveoutput" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}

	@Override
	public String getPathToJhoveConfiguration()
	{
		Object prop = getConfig().getProperty( "pathtojhoveconfig" );

		if ( prop instanceof String ) {
			String value = (String) prop;
			return value;
		}
		return null;
	}
}
