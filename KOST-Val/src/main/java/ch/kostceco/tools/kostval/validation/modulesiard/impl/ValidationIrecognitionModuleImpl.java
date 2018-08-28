/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2018 Claire Roethlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationIrecognitionException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationIrecognitionModule;

/** Validierungsschritt I (SIARD-Erkennung) Wird die SIARD-Datei als SIARD erkannt? valid -->
 * Extension = .siard
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationIrecognitionModuleImpl extends ValidationModuleImpl implements
		ValidationIrecognitionModule
{
	Boolean	version1	= false;
	Boolean	version2	= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap )
			throws ValidationIrecognitionException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
		File metadataXml = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
				.append( "header" ).append( File.separator ).append( "metadata.xml" ).toString() );
		if ( onWork.equals( "no" ) ) {
			// keine Ausgabe
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "I    " );
			System.out.print( "\b\b\b\b\b" );
		}

		Boolean valid = true;
		try {
			/** Validierung ob die Extension .siard lautet */
			if ( !valDatei.getAbsolutePath().toLowerCase().endsWith( ".siard" ) ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_I_SIARD )
								+ getTextResourceService().getText( MESSAGE_XML_I_NOTALLOWEDEXT ) );
				// Die SIARD-Datei wurde nicht als solche erkannt, weil sie keine .siard Extension hat.
				valid = false;
			}

			version1 = FileUtils.readFileToString( metadataXml ).contains(
					"http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			version2 = FileUtils.readFileToString( metadataXml ).contains(
					"http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd" );
			if ( version1 ) {
				// keine zusaetzliche kontrolle
			} else if ( version2 ) {
				File version21 = new File( new StringBuilder( pathToWorkDir ).append( File.separator )
						.append( "header" ).append( File.separator ).append( "siardversion" )
						.append( File.separator ).append( "2.1" ).toString() );

				/* bestehendes Workverzeichnis Abbruch wenn nicht leer, da am Schluss das Workverzeichnis
				 * gelöscht wird und entsprechend bestehende Dateien gelöscht werden können */
				if ( !version21.exists() ) {
					valid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_I_SIARD )
									+ getTextResourceService().getText( MESSAGE_XML_I_SIARDVERSION, "2.1",
											version21.getAbsolutePath() ) );
				}

			}
		} catch ( java.io.IOException ioe ) {
			valid = false;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_I_SIARD )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
									ioe.getMessage() + " (IOException)" ) );
		}

		/** Validierung ob die PUID richtig erkannt wird (z.B mit DROID) => Auf diese Validierung kann
		 * verzichtet werden, da bereits vorg�ngig gepr�ft wurde ob es ein unkomprimiertes ZIP mit dem
		 * entsprechenden metadata.xml ist. */

		return valid;
	}
}
