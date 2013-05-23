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

package ch.kostceco.tools.tiffval.service;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

//import org.apache.tools.ant.util.regexp.Regexp;

import ch.kostceco.tools.tiffval.service.vo.ValidatedFormat;

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
	 * Diverse Angaben zu Jhove
	 */
	String getPathToJhoveJar();

	String getPathToJhoveOutput();

	String getPathToJhoveConfiguration();

	/**
	 * Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird
	 * z.B. zum Entpacken des .zip-Files verwendet.
	 * 
	 * @return Pfad des Arbeitsverzeichnisses
	 */
	String getPathToWorkDir();
	
}
