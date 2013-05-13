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

package ch.kostceco.tools.tiffval.validation.module3;

import java.io.File;

import ch.kostceco.tools.tiffval.exception.module3.Validation3dPeriodException;
import ch.kostceco.tools.tiffval.validation.ValidationModule;

/**
 * Validierungsschritt 3d (einschaltbar) Zeitraum-Validierung. Ältestes und
 * Jüngstes Datum in einer Ordnungssystem Einheit (Dossier, Rubrik) müssen ohne
 * Überlappung nach oben aggregierbar sein, Lehrräume sind aber erlaubt. Dies
 * bedeutet, dass die Dokumente im Zeitraum des Dossiers sein müssen, diese
 * wiederum in der Rubrik und entsprechend auch im SIP.
 * 
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */

public interface Validation3dPeriodModule extends ValidationModule
{

	public boolean validate( File sipDatei ) throws Validation3dPeriodException;

}
