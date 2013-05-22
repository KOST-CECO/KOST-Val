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

package ch.kostceco.tools.tiffval.service.vo;

/**
 * Ein Value Object, das die "validatedformat" Elemente aus der
 * Konfigurationsdatei einkapselt.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public class ValidatedFormat
{

	final static String	JHOVE	= "JHOVE";
	final static String	PDFTRON	= "PDFTRON";
	final static String	SIARDVAL	= "SIARDVAL";
	
	private String		pronomUniqueId;
	private String		validator;
	private String		extension;
	private String		description;

	public ValidatedFormat( String pronomUniqueId, String validator,
			String extension, String description )
	{
		super();
		this.pronomUniqueId = pronomUniqueId;
		this.validator = validator;
		this.extension = extension;
		this.description = description;
	}

	public String getPronomUniqueId()
	{
		return pronomUniqueId;
	}

	public void setPronomUniqueId( String pronomUniqueId )
	{
		this.pronomUniqueId = pronomUniqueId;
	}

	public String getValidator()
	{
		return validator;
	}

	public void setValidator( String validator )
	{
		this.validator = validator;
	}

	public String getExtension()
	{
		return extension;
	}

	public void setExtension( String extension )
	{
		this.extension = extension;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

}
