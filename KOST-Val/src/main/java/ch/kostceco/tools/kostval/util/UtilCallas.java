/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2017 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.util;

import static org.apache.commons.io.IOUtils.closeQuietly;

import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.StreamGobbler;
import ch.kostceco.tools.kostval.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class UtilCallas
{
	private ConfigurationService	configurationService;

	public static String					NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	/** Callas ausführen. Die Art, wie allgemein callas angesprochen wird, wird er nicht publiziert.
	 * Publiziert wird wie callas angesprochen wird, wenn diese separat erworben und aktiviert wurde.
	 * 
	 * @author Rc Claire Roethlisberger, KOST-CECO
	 * @param pdfapilotExe
	 *          : Pfad zum Programm pdfapilot
	 * @param analye
	 *          : Befehl inkl optionen
	 * @param lang
	 *          : Sprache
	 * @param valPath
	 *          : Pfad zur Datei
	 * @param reportPath
	 *          : Pfad zum Report
	 * @return isValid true wenn validierung bestanden wurde */
	public static boolean execCallas( String pdfapilotExe, String analye, String lang,
			String valPath, String reportPath ) throws Exception
	{
		/* Aufbau command:
		 * 
		 * 1) pdfapilotExe: Pfad zum Programm pdfapilot
		 * 
		 * 2) analye: Befehl inkl optionen
		 * 
		 * 3) lang: Sprache
		 * 
		 * 4) valPath: Pfad zur Datei
		 * 
		 * 5) reportPath: Pfad zum Report */

		/* C:\Tools\pdfaPilot\callas_pdfaPilotServer_Win_7.0.268\cli\pdfaPilot.exe -a --noprogress
		 * --nohits --level=1b -l=DE valDatei >> pathToPdfapilotOutput */

		StringBuffer command = new StringBuffer( pdfapilotExe + " " );
		command.append( analye + " " );
		command.append( lang + " \"" );
		command.append( valPath + "\" >> \"" );
		command.append( reportPath + "\"" );

		String commandRed = "cmd /c \"" + command + "\"";
		/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
		 * gehts: cmd /c\"urspruenlicher Befehl\" */

		boolean isValid = false;

		Process proc = null;
		Runtime rt = null;

		try {

			Util.switchOffConsole();

			rt = Runtime.getRuntime();
			proc = rt.exec( commandRed.split( " " ) );
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

			// Fehleroutput holen
			StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

			// Threads starten
			errorGobbler.start();
			outputGobbler.start();

			// Warte, bis wget fertig ist
			int returnCode = proc.waitFor();

			if ( returnCode == 0 ) {
				/* 0 PDF is valid PDF/A-file additional checks wihtout problems
				 * 
				 * 1 PDF is valid PDF/A-file but additional checks with problems – severity info
				 * 
				 * 2 PDF is valid PDF/A-file but additional checks with problems – severity warning
				 * 
				 * 3 PDF is valid PDF/A-file but additional checks with problems – severity error
				 * 
				 * 4 PDF is not a valid PDF/A-file */
				isValid = true;
			}

			Util.switchOnConsole();
		} catch ( Exception e ) {
			e.getMessage();
			return false;
		} finally {
			if ( proc != null ) {
				closeQuietly( proc.getOutputStream() );
				closeQuietly( proc.getInputStream() );
				closeQuietly( proc.getErrorStream() );
			}
		}
		// Validierungsergebnis zurückgeben
		return isValid;
	}

}
