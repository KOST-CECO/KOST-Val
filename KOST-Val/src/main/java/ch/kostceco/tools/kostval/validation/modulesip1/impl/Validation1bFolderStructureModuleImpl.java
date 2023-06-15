/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.exception.modulesip1.Validation1bFolderStructureException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1bFolderStructureModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Besteht eine korrekte primäre Verzeichnisstruktur: /header/metadata.xml
 * /header/xsd /content
 */
public class Validation1bFolderStructureModuleImpl extends ValidationModuleImpl
		implements Validation1bFolderStructureModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws Validation1bFolderStructureException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if ( onWork.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "1B   " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean isValid = true;
		File content = new File(
				valDatei.getAbsolutePath() + File.separator + "content" );
		File header = new File(
				valDatei.getAbsolutePath() + File.separator + "header" );
		File xsd = new File( valDatei.getAbsolutePath() + File.separator
				+ "header" + File.separator + "xsd" );
		File metadata = new File( valDatei.getAbsolutePath() + File.separator
				+ "header" + File.separator + "metadata.xml" );

		if ( !content.exists() ) {
			isValid = false;

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ab_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_AB_CONTENT ) );
		}
		if ( !header.exists() ) {
			isValid = false;

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ab_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_AB_HEADER ) );
		} else {
			if ( !xsd.exists() ) {
				isValid = false;

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Ab_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_AB_XSD ) );
			}
			if ( !metadata.exists() ) {
				isValid = false;

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_Ab_SIP )
								+ getTextResourceService().getText( locale,
										ERROR_XML_AB_METADATA ) );
			}
		}
		return isValid;
	}
}
