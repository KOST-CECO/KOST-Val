/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
 * -----------------------------------------------------------------------------------------------
 * KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all copyright
 * interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March
 * 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostval.validation.modulesiard;

import java.io.File;
import java.io.IOException;

import org.jdom2.JDOMException;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationEcolumnException;
import ch.kostceco.tools.kostval.validation.ValidationModule;
import ch.kostceco.tools.kostval.validation.bean.ValidationContext;

/** Validierungsschritt E (Spalten-Validierung) Wurden die Angaben aus metadata.xml korrekt in die
 * tableZ.xsd-Dateien übertragen? valid --> gleiche Spaltendefinitionen (Anzahl, Type, Nullable)
 * 
 * @author Do Olivier Debenath */

public interface ValidationEcolumnModule extends ValidationModule
{
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationEcolumnException;

	public boolean prepareValidation( ValidationContext validationContext ) throws IOException,
			JDOMException, Exception;
}
