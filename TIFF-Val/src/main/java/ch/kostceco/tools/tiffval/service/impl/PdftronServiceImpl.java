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

package ch.kostceco.tools.tiffval.service.impl;

import java.io.File;
import ch.kostceco.tools.tiffval.exception.SystemException;
import ch.kostceco.tools.tiffval.logging.Logger;
import ch.kostceco.tools.tiffval.service.PdftronService;
import ch.kostceco.tools.tiffval.service.TextResourceService;
import ch.kostceco.tools.tiffval.util.StreamGobbler;
import ch.kostceco.tools.tiffval.util.Util;

/**
 * Dieser Service stellt die Schnittstelle zur Pdftron Software dar.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public class PdftronServiceImpl implements PdftronService
{

	private static final Logger	LOGGER	= new Logger( PdftronServiceImpl.class );

	private TextResourceService	textResourceService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	@Override
	public String executePdftron( String pathToPdftronExe,
			String pathToInputFile, String pathToOutput, String nameOfSip )
			throws SystemException
	{

		File report;
		File pdftronExe = new File( pathToPdftronExe ); // Pfad zum Programm
														// Pdftron
		File output = new File( pathToOutput );
		StringBuffer command = new StringBuffer( pdftronExe + " " );

		command.append( "-l B " );
		command.append( "-o " );
		command.append( "\"" );
		command.append( output.getAbsolutePath() );
		command.append( "\"" );
		command.append( " " );
		command.append( pathToInputFile );

		try {

			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec( command.toString() );

			// Fehleroutput holen
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR" );

			// Output holen
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT" );

			Util.switchOffConsole();

			// Threads starten
			errorGobbler.start();
			outputGobbler.start();

			// Warte, bis wget fertig ist
			proc.waitFor();

			Util.switchOnConsole();

			// System.out.println("exit value: " + exitValue);

			// Der Name des generierten Reports lautet per default report.xml
			// und es scheint keine
			// Möglichkeit zu geben, dies zu übersteuern.
			report = new File( pathToOutput, "report.xml" );
			File newReport = new File( pathToOutput, nameOfSip
					+ ".pdftron-log.xml" );

			// falls das File bereits existiert, z.B. von einem vorhergehenden
			// Durchlauf, löschen wir es
			if ( newReport.exists() ) {
				newReport.delete();
			}

			boolean renameOk = report.renameTo( newReport );
			if ( !renameOk ) {
				throw new SystemException(
						"Der Report konnte nicht umbenannt werden." );
			}
			report = newReport;

		} catch ( Exception e ) {
			LOGGER.logDebug( "Pdftron Service failed: " + e.getMessage() );
			throw new SystemException( e.toString() );
		}

		return report.getAbsolutePath();

	}

	@Override
	public String getPathToInputFile()
	{
		return null;
	}

	@Override
	public String getPathToPdftronExe()
	{
		return null;
	}

	@Override
	public void setPathToInputFile( String pathToInputFile )
	{

	}

	@Override
	public void setPathToPdftronExe( String pathToPdftronExe )
	{

	}

}
