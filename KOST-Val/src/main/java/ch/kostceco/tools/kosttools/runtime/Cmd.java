/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO. 2012-2022
 * -----------------------------------------------------------------------------------------------
 * KOST-Tools is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all
 * copyright interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland,
 * 1 March 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kosttools.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Cmd
{

	/**
	 * fuehrt eine cmd durch und gibt den Text der Konsole als String zurueck
	 * 
	 * @param command
	 *            String des Command, wie er in der Konsole eingegeben wird
	 * @param out
	 *            bei true wird der OUTPUT auch in den String geschrieben. Bei
	 *            False nur ERROR.
	 * @param workDir
	 *            Temporaeres Verzeichnis
	 * @return String mit der Konsolenausgabe von ERROR und ggf OUTPUT
	 */
	public static String execToString( String command, boolean out,
			File workDir ) throws InterruptedException
	{
		/*
		 * command = "\"\"" + exeFile.getAbsolutePath() + "\"" +
		 * " --noout --stream --nowarning --schema " + "\"" +
		 * xsdFile.getAbsolutePath() + "\"" + " " + "\"" +
		 * xmlFile.getAbsolutePath() + "\"\"";
		 */

		// System.out.println( "executing command: " + command );
		Process p = null;
		try {
			p = Runtime.getRuntime().exec( "cmd /c " + command, null, workDir );

		} catch ( IOException ex ) {
			System.out.println( "IOException exec P: " + ex );
		}
		String line = "";
		String lineE = "";
		String lineReturn = line;
		try {
			if ( out ) {
				// System.out.println( "OUTPUT" );
				InputStream stream = p.getInputStream();
				BufferedReader in = new BufferedReader(
						new InputStreamReader( stream ) );
				while ( (line = in.readLine()) != null ) {
					// System.out.println(line);
					if ( lineReturn.equals( "" ) ) {
						lineReturn = line;
					} else {
						if ( lineReturn.contains( line ) ) {
							// Fehler bereits festgehalten (dublikat)
						} else {
							lineReturn = lineReturn + "</Message><Message>"
									+ line;
						}
					}
				}
				in.close();
			}
			// System.out.println( "ERROR-OUTPUT" );
			InputStream streamE = p.getErrorStream();
			BufferedReader inE = new BufferedReader(
					new InputStreamReader( streamE ) );
			while ( (lineE = inE.readLine()) != null ) {
				// System.out.println(lineE);
				if ( lineReturn.equals( "" ) ) {
					lineReturn = "ERROR: " + lineE;
				} else {
					if ( lineReturn.contains( lineE ) ) {
						// Fehler bereits festgehalten (dublikat)
					} else {
						lineReturn = lineReturn + "</Message><Message>ERROR: "
								+ lineE;
					}
				}
			}
			inE.close();
		} catch ( IOException ex ) {
			System.out.println( "IOException exec Out Err: " + ex );
		}
		if ( lineReturn.equals( "" ) ) {
			lineReturn = "OK";
		}
		// System.out.println("return String exec: "+lineReturn);
		return lineReturn;
	}

}
