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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationEbitspersampleValidationModule;

/** Validierungsschritt E (BitsPerSample-Validierung) Ist die TIFF-Datei gemäss Konfigurationsdatei
 * valid?
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class ValidationEbitspersampleValidationModuleImpl extends ValidationModuleImpl implements
		ValidationEbitspersampleValidationModule
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

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
	{

		boolean isValid = true;

		// Informationen zum Jhove-Logverzeichnis holen
		String pathToJhoveOutput = directoryOfLogfile.getAbsolutePath();
		File jhoveReport = new File( pathToJhoveOutput, valDatei.getName() + ".jhove-log.txt" );

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		String bps1 = getConfigurationService().getAllowedBitspersample1();
		String bps2 = getConfigurationService().getAllowedBitspersample2();
		String bps4 = getConfigurationService().getAllowedBitspersample4();
		String bps8 = getConfigurationService().getAllowedBitspersample8();
		String bps16 = getConfigurationService().getAllowedBitspersample16();
		String bps32 = getConfigurationService().getAllowedBitspersample32();
		String bps64 = getConfigurationService().getAllowedBitspersample64();

		Integer jhoveio = 0;
		Integer typetiff = 0;

		try {
			BufferedReader in = new BufferedReader( new FileReader( jhoveReport ) );
			String line;
			while ( (line = in.readLine()) != null ) {
				if ( line.contains( "Type: TIFF" ) ) {
					typetiff = 1;
					// TIFF-IFD
				} else if ( line.contains( "Type: Exif" ) ) {
					typetiff = 0;
					// Exif-IFD
				}
				if ( typetiff == 1 ) {
					/* zu analysierende TIFF-IFD-Zeile die BitsPerSample-Zeile enthält einer dieser Freitexte
					 * der BitsPerSampleart */
					if ( line.contains( "BitsPerSample:" ) ) {
						jhoveio = 1;
						if ( line.contains( "5" ) || line.contains( "7" ) || line.contains( "9" )
								|| line.contains( "10" ) || line.contains( "11" ) || line.contains( "12" )
								|| line.contains( "14" ) || line.contains( "18" ) || line.contains( "20" )
								|| line.contains( "21" ) || line.contains( "22" ) || line.contains( "24" )
								|| line.contains( "26" ) || line.contains( "28" ) ) {
							// Invalider Status
							isValid = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
						} else if ( line.contains( "40" ) || line.contains( "41" ) || line.contains( "42" )
								|| line.contains( "44" ) || line.contains( "46" ) || line.contains( "48" )
								|| line.contains( "80" ) || line.contains( "81" ) || line.contains( "82" )
								|| line.contains( "84" ) || line.contains( "86" ) || line.contains( "88" ) ) {
							// Invalider Status
							isValid = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
						} else if ( line.contains( "3" ) && !line.contains( "32" ) ) {
							// Invalider Status
							isValid = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
						} else if ( line.contains( "64" ) ) {
							// Status 64
							if ( !line.contains( bps64 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "32" ) ) {
							// Status 32
							if ( !line.contains( bps32 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "16" ) ) {
							// Status 16
							if ( !line.contains( bps16 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "8" ) ) {
							// Status 8
							if ( !line.contains( bps8 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "4" ) ) {
							// Status 4
							if ( !line.contains( bps4 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "2" ) ) {
							// Status 2
							if ( !line.contains( bps2 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else if ( line.contains( "1" ) ) {
							// Status 1
							if ( !line.contains( bps1 ) ) {
								// Invalider Status
								isValid = false;
								getMessageService().logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
												+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
							}
						} else {
							// Invalider Status
							isValid = false;
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
											+ getTextResourceService().getText( MESSAGE_XML_CG_INVALID, line ) );
						}
					}
				}
			}
			if ( jhoveio == 0 ) {
				// Invalider Status
				isValid = false;
				isValid = false;
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_CG_JHOVENIO, "E" ) );
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_E_TIFF )
							+ getTextResourceService().getText( MESSAGE_XML_CG_CANNOTFINDJHOVEREPORT ) );
			return false;
		}
		return isValid;
	}
}