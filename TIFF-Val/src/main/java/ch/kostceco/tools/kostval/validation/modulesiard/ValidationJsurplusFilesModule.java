/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF and SIARD-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
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

package ch.kostceco.tools.kostval.validation.modulesiard;

import java.io.File;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationJsurplusFilesException;
import ch.kostceco.tools.kostval.validation.ValidationModule;

/**
 * Validierungsschritt J (Zusätzliche Primärdateien) Enthält der content-Ordner
 * Dateien oder Ordner die nicht in metadata.xml beschrieben sind ? invalid -->
 * Zusätzliche Ordner oder Dateien im content-Ordner
 * 
 * @author Ec Christian Eugster
 */

public interface ValidationJsurplusFilesModule extends ValidationModule
{

	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationJsurplusFilesException;

}
