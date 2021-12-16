/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO. 2012-2021
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

package ch.kostceco.tools.kosttools.fileservice;

import java.io.File;

import ch.kostceco.tools.kosttools.util.Util;
import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class DroidPuid
{

	public static String getPuid( File file, String nameOfSignature )
	{
		/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den Output
		 * stoeren Util.switchOffConsole() als Kommentar markieren wenn man die Fehlermeldung erhalten
		 * moechte */
		Util.switchOffConsole();
		String puid = " ??? ";
		try {
			Droid droid = null;
			droid = new Droid();

			droid.readSignatureFile( nameOfSignature );

			IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
			for ( int x = 0; x < ifile.getNumHits(); x++ ) {
				FileFormatHit ffh = ifile.getHit( x );
				FileFormat ff = ffh.getFileFormat();
				puid = ff.getPUID();
			}
		} catch ( Exception e ) {
			Util.switchOnConsole();
			System.out.println( "Exception fileservice getPuid: " + e.getMessage() );
			return " ERROR ";
		} finally {
			Util.switchOnConsole();
		}
		return puid;
	}

}
