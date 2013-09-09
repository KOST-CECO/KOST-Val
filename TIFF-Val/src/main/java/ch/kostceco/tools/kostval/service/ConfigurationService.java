/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate SIP, TIFF-Files and SIARD-Files.
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
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

package ch.kostceco.tools.kostval.service;

/**
 * Service Interface für die Konfigurationsdatei.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public interface ConfigurationService extends Service
{

	/**
	 * Gibt die Komprimierung aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Komprimierung, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedCompression1();

	String getAllowedCompression2();

	String getAllowedCompression3();

	String getAllowedCompression4();

	String getAllowedCompression5();

	String getAllowedCompression7();

	String getAllowedCompression8();

	String getAllowedCompression32773();

	/**
	 * Gibt die Farbraum aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Farbraum, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedPhotointer0();

	String getAllowedPhotointer1();

	String getAllowedPhotointer2();

	String getAllowedPhotointer3();

	String getAllowedPhotointer4();

	String getAllowedPhotointer5();

	String getAllowedPhotointer6();

	String getAllowedPhotointer8();

	/**
	 * Gibt die BitsPerSample aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der BitsPerSample, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedBitspersample1();

	String getAllowedBitspersample2();

	String getAllowedBitspersample4();

	String getAllowedBitspersample8();

	String getAllowedBitspersample16();

	String getAllowedBitspersample32();

	String getAllowedBitspersample64();

	/**
	 * Gibt an ob Multipage im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Multipage, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedMultipage();

	/**
	 * Gibt an ob Tiles im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Tiles, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedTiles();

	/**
	 * Gibt an ob Giga-TIFF vorkommen dürfen.
	 * 
	 * @return Wert 0/1
	 */
	String getAllowedSize ();

	/**
	 * Diverse Angaben zu Jhove
	 */
	String getPathToJhoveJar();

	String getPathToJhoveConfiguration();

}
