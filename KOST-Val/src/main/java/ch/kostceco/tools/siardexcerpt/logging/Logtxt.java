/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Logging Klasse.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */
public class Logtxt
{

	public static void logtxt( File logFile, String txt )
	{
		try {
			if ( !logFile.exists() ) {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			}

			PrintWriter out = new PrintWriter(
					new BufferedWriter( new FileWriter( logFile, true ) ) );
			/*
			 * kann nicht direkt ausgegeben werden, es ist UTF-8
			 * 
			 * out.println( txt );
			 */
			byte[] byteTxt = txt.getBytes( StandardCharsets.UTF_8 );
			String txtByte = new String( byteTxt );
			out.println( txtByte );

			out.close();
		} catch ( IOException e ) {
			System.out.println( "Logtxt: " + e );
		}

		// oeffnet das UTF-8 logFile, welches nicht ueberschrieben wird (=true)
		/*
		 * try (FileOutputStream fos = new FileOutputStream( logFile, true );
		 * OutputStreamWriter osw = new OutputStreamWriter( fos,
		 * StandardCharsets.UTF_8 ); BufferedWriter writer = new BufferedWriter(
		 * osw )) { // txt ist standard UTF-16 byte[] byteTxt = txt.getBytes();
		 * // byte[] byteTxt = txt.getBytes( StandardCharsets.UTF_8 ); String
		 * txtByte = new String( byteTxt ); // fuegt den utf8 txt hinten an und
		 * erstellt eine neue Zeile writer.append( txt ); writer.newLine();
		 * writer.append( txtByte ); writer.newLine(); } catch ( IOException e )
		 * { e.printStackTrace(); }
		 */

	}

}
