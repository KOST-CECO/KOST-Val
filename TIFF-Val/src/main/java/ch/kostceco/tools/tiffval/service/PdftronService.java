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

import ch.kostceco.tools.tiffval.exception.SystemException;

/**
 * Service Interface für Pdftron.
 * 
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public interface PdftronService extends Service
{

	/**
	 * Gibt den Pfad zum Pdftron Executable zurück
	 * 
	 * @return Pfad zum Pdftron Executable
	 */
	public String getPathToPdftronExe();

	/**
	 * Setzt den Pfad zum Pdftron Executable
	 * 
	 * @return Pfad zum Pdftron Executable
	 */
	public void setPathToPdftronExe( String pathToPdftronExe );

	/**
	 * Gibt den Pfad zum Input File (das zu validierende Dokument) zurück
	 * 
	 * @return Pfad zum Input File
	 */
	public String getPathToInputFile();

	/**
	 * Setzt den Pfad zum Input File (dem zu validierenden Dokument)
	 * 
	 * @return Pfad zum Input File
	 */
	public void setPathToInputFile( String pathToInputFile );

	/**
	 * Führt die Validierung mit Pdftron aus
	 * 
	 * @return Pfad zum von Pdftron generierten XML-Report
	 * @throws Exception
	 */
	public String executePdftron( String pathToPdftronExe,
			String pathToInputFile, String pathToOutput, String nameOfSip )
			throws SystemException;
}
