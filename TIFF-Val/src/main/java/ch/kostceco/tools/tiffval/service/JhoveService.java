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

import java.io.File;

import ch.kostceco.tools.tiffval.exception.SystemException;

/**
 * Service Interface für Jhove.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public interface JhoveService extends Service
{

	/**
	 * Führt die Validierung mit Jhove aus
	 * 
	 * @param pathToJhoveJar
	 *            Pfad zur JHove Applikation (JhoveApp.jar)
	 * @param pathToInputFile
	 *            Pfad(e) zu den Input-Files, mit Blanks separiert
	 * @param pathToOutput
	 *            Pfad zum Output-File
	 * @param nameOfSip
	 *            Name des Original-SIP-Input Files
	 * @param extension
	 *            die Extension der Input Files
	 * @return Name des Output Files (der pathToOutput wurde eventuell
	 *         umbenannt)
	 * @throws SystemException
	 */
	public File executeJhove( String pathToJhoveJar, String pathToInputFile,
			String pathToOutput, String nameOfSip, String extension )
			throws SystemException;
}
