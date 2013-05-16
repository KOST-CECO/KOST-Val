/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
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

package ch.kostceco.tools.tiffval.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rc Claire Röthlisberger, KOST-CECO Mit dieser Klasse werden
 *         die Error Codes, welche von Pdftron generiert werden, übersetzt. Bsp:
 *         <Error Code="e_PDFA173"
 *         Message="The value of Length does not match the number of bytes" ...
 *         wobei die erste Ziffer nach e_PDFA den Code darstellt. Als Code kann
 *         auch "e_PDF_Unknown" zurückgegeben werden, dies wird dann als Code 0
 *         / General interpretiert.
 */

public class PdftronErrorCodes
{
	final static Map<String, String>	pdftronErrorCodes	= new HashMap<String, String>();

	static {
		pdftronErrorCodes.put( "0", "General" );
		pdftronErrorCodes.put( "1", "File structure" );
		pdftronErrorCodes.put( "2", "Graphics" );
		pdftronErrorCodes.put( "3", "Fonts" );
		pdftronErrorCodes.put( "4", "Transparency" );
		pdftronErrorCodes.put( "5", "Annotations" );
		pdftronErrorCodes.put( "6", "Actions" );
		pdftronErrorCodes.put( "7", "Metadata" );
		pdftronErrorCodes.put( "8", "Pdf-1 a" );
		pdftronErrorCodes.put( "9", "Interactive Forms" );
	}

	/**
	 * Gibt die Bezeichnung des Error Codes zurück.
	 * 
	 * @param errorCode
	 *            Pdftron Error Code
	 * @return Error Label
	 */
	public static String getErrorLabel( String errorCode )
	{
		return pdftronErrorCodes.get( errorCode );
	}

}
