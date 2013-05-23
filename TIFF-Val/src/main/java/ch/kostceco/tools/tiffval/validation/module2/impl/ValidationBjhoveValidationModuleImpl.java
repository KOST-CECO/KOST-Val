/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Tagged Image File Format (TIFF).
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

package ch.kostceco.tools.tiffval.validation.module2.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ch.kostceco.tools.tiffval.exception.module2.ValidationBjhoveValidationException;
import ch.kostceco.tools.tiffval.service.ConfigurationService;
import ch.kostceco.tools.tiffval.service.JhoveService;
import ch.kostceco.tools.tiffval.util.Util;
import ch.kostceco.tools.tiffval.validation.ValidationModuleImpl;
import ch.kostceco.tools.tiffval.validation.module2.ValidationBjhoveValidationModule;

/**
 * Validierungsschritt B (Jhove-Validierung) Ist die TIFF-Datei gemäss Jhove
 * valid? valid --> Status: "Well-Formed and valid"
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class ValidationBjhoveValidationModuleImpl extends ValidationModuleImpl
		implements ValidationBjhoveValidationModule
{

	private ConfigurationService	configurationService;
	private JhoveService			jhoveService;

	public static String			NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setJhoveService( JhoveService jhoveService )
	{
		this.jhoveService = jhoveService;
	}

	public JhoveService getJhoveService()
	{
		return jhoveService;
	}

	@Override
	public boolean validate( File tiffDatei )
			throws ValidationBjhoveValidationException
	{

		boolean isValid = true;

		String toplevelDir = tiffDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		// Vorbereitungen: tiffDatei an die JHove Applikation übergeben
		File jhoveReport = null;
		StringBuffer concatenatedOutputs = new StringBuffer();
		String pathToJhoveJar = getConfigurationService().getPathToJhoveJar();
		// Informationen zum Jhove-Logverzeichnis holen
		String pathToJhoveOutput = getConfigurationService()
				.getPathToJhoveOutput();

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		File jhoveDir = new File( pathToJhoveOutput );
		if ( !jhoveDir.exists() ) {
			jhoveDir.mkdir();
		}

		try {
			String tiffDateiStr = tiffDatei.getAbsolutePath();
			// pathsJhove = path to InputFile = path to tiffDatei
			StringBuffer pathsJhove = new StringBuffer( tiffDateiStr );
			jhoveReport = getJhoveService().executeJhove( pathToJhoveJar,
					pathsJhove.toString(), pathToJhoveOutput,
					tiffDatei.getName() );

			BufferedReader in = new BufferedReader(
					new FileReader( jhoveReport ) );
			String line;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );

				// die Status-Zeile enthält diese Möglichkeiten:
				// Valider Status: "Well-Formed and valid"
				// Invalider Status: "Not well-formed" oder
				// "Well-Formed, but not valid" möglicherweise
				// existieren weitere Ausgabemöglichkeiten
				if ( line.contains( "Status:" ) ) {
					if ( !line.contains( "Well-Formed and valid" ) ) {
						// Invalider Status
						isValid = false;
						getMessageService()
								.logError(
										getTextResourceService().getText(
												MESSAGE_MODULE_B )
												+ getTextResourceService()
														.getText(
																MESSAGE_DASHES )
												+ getTextResourceService()
														.getText(
																MESSAGE_MODULE_B_JHOVEINVALID ) );
					}
				}
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_B )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ e.getMessage() );
			return false;
		}

		// die im StringBuffer JHove-Outputs
		// wieder in das Output-File zurückschreiben
		if ( jhoveReport != null ) {
			try {
				BufferedWriter out = new BufferedWriter( new FileWriter(
						jhoveReport ) );
				out.write( concatenatedOutputs.toString() );
				out.close();
				Util.setPathToReportJHove( jhoveReport.getAbsolutePath() );

			} catch ( IOException e ) {
				getMessageService()
						.logError(
								getTextResourceService().getText(
										MESSAGE_MODULE_B )
										+ getTextResourceService().getText(
												MESSAGE_DASHES )
										+ getTextResourceService()
												.getText(
														MESSAGE_MODULE_B_CANNOTWRITEJHOVEREPORT ) );
				return false;
			}
		}
		return isValid;
	}

}