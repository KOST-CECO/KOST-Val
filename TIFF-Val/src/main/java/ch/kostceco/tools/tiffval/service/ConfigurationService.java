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
	 * Gibt eine Liste mit den PUIDs aus, welche im SIP vorkommen dürfen.
	 * 
	 * @return Liste mit den PUIDs aus, welche im SIP vorkommen dürfen.
	 */
	Map<String, String> getAllowedPuids();


	/**
	 * Gibt den Namen des DROID Signature Files zurück. Die Signaturen werden
	 * laufend aktualisiert und müssen deshalb von Zeit zu Zeit ausgetauscht
	 * werden. Daher der konfigurierbare Name.
	 * 
	 * @return Namen des DROID Signature Files
	 * @author Rc Claire Röthlisberger-Jourdan, KOST-CECO, @version 0.2.1, date
	 *         28.03.2011
	 */
	String getPathToDroidSignatureFile();

	String getPathOfDroidSignatureFile() throws MalformedURLException;

	/**
	 * Gibt den Pfad zum Pdftron Exe zurück.
	 * 
	 * @return Pfad zum Pdftron Exe
	 */
	String getPathToPdftronExe();

	/**
	 * Gibt den Pfad zum Output Folder des Pdftron zurück.
	 * 
	 * @return Pfad zum Output Folder des Pdftron
	 */
	String getPathToPdftronOutputFolder();

	/**
	 * Gibt den Pfad zum Siard-Val Exe zurück.
	 * 
	 * @return Pfad zum Siard-Val Exe
	 */
	String getPathToSiardValJar();

	/**
	 * Gibt den Pfad zum Output Folder des Siard-Val zurück.
	 * 
	 * @return Pfad zum Output Folder des Siard-Val
	 */
	String getPathToSiardValOutputFolder();

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

	/**
	 * Gibt eine Liste mit den zu validierenden Formaten zurück.
	 * 
	 * @return Liste mit den zu validierenden Formaten
	 */
	List<ValidatedFormat> getValidatedFormats();
	
}
