/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import ch.kostceco.tools.kostval.exception.modulesip1.Validation1aZipException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1aZipModule;
import ch.enterag.utils.zip.Zip64File;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public class Validation1aZipModuleImpl extends ValidationModuleImpl implements
		Validation1aZipModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation1aZipException
	{

		boolean valid = false;

		// Eine ZIP Datei muss mit PK.. beginnen
		if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".zip" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".zip64" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// Hex 03 in Char umwandeln
				String str3 = "03";
				int i3 = Integer.parseInt( str3, 16 );
				char c3 = (char) i3;
				// Hex 04 in Char umwandeln
				String str4 = "04";
				int i4 = Integer.parseInt( str4, 16 );
				char c4 = (char) i4;

				// auslesen der ersten 4 Zeichen der Datei
				int length;
				int i;
				char[] buffer = new char[4];
				length = read.read( buffer );
				for ( i = 0; i != length; i++ )
					;

				// die beiden charArrays (soll und ist) mit einander
				// vergleichen
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					// höchstwahrscheinlich ein ZIP da es mit
					// 504B0304 respektive
					// PK.. beginnt
					valid = true;
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Aa )
								+ getTextResourceService().getText(
										MESSAGE_DASHES ) + e.getMessage() );
				return false;
			}
		}

		// wenn die Datei kein Directory ist, muss sie mit zip oder zip64 enden
		if ( (!(valDatei.getAbsolutePath().toLowerCase().endsWith( ".zip" ) || valDatei
				.getAbsolutePath().toLowerCase().endsWith( ".zip64" )))
				|| valid == false ) {

			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Aa )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_MODULE_AA_INCORRECTFILEENDING ) );

			return false;
		}

		Zip64File zf = null;

		try {
			// Versuche das ZIP file zu öffnen
			zf = new Zip64File( valDatei );
			// und wenn es klappt, gleich wieder schliessen
			zf.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Aa )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ e.getMessage() );

			return false;
		}

		return true;

	}
}
