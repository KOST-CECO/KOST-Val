/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1gPackageSizeFilesException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1gPackageSizeFilesModule;

/** Validierungsschritt 1g Es wir ein Fehler ausgegeben wenn ueber 1 Mio Dateien im Paket sind. Es
 * wird eine Warnung ausgegeben wenn das SIP ueber 8GB gross ist oder mehr als 5000 Dateien direkt
 * im Ordner sind. * */

public class Validation1gPackageSizeFilesModuleImpl extends ValidationModuleImpl
		implements Validation1gPackageSizeFilesModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale, File logFile ) throws Validation1gPackageSizeFilesException
	{
		boolean isValid = true;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "1G   " );
			System.out.print( "\b\b\b\b\b" );
		}

		try {
			/* S_5.1-1: Warnung 8GB gross
			 * 
			 * S_5.2-1: Fehler über 1 Mio Dateien im Paket
			 * 
			 * S_5.2-2: Warnung wenn mehr als 5000 Dateien direkt im Ordner */
			long size = 7900000000L;
			long sizeK = 7900000000L;
			long sizeM = 7900000000L;
			int sizeMint = 999;
			size = FileUtils.sizeOfDirectory( valDatei );
			sizeK = size / 1024;
			sizeM = sizeK / 1024;
			sizeMint = (int) sizeM;
			int warningSizeM = 8192;
			// System.out.println( "sizeOfDirectory: " + size + "Byte = " + sizeK + "KB = " + sizeM + "MB"
			// );
			if ( sizeMint > warningSizeM ) {
				// Warnung 8GB gross
				// System.out.println( "Size more than 8192MB: " + sizeMint + "MB" );
				// val.error.xml.ag.warningbigsip =
				// Warnung: Das SIP ist {0}MB gross und koennte Probleme verursachen (Limite 8GB = 8192MB).
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ag_SIP )
								+ getTextResourceService().getText( locale, ERROR_XML_AG_WARNINGBIGSIP,
										String.valueOf( sizeMint ) ) );
			}

			int numFilesContent = 0;
			Map<String, File> fileMapFile = Util.getFileMapFile( valDatei );
			Set<String> fileMapKeys = fileMapFile.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				String entryName = iterator.next();
				// entryName: content/DOS_02/gpl2.pdf
				if ( entryName.startsWith( "content/" ) ) {
					numFilesContent++;
					// System.out.println("entryName "+numFilesContent+": "+entryName);
				}
			}
			if ( numFilesContent > 1000000 ) {
				// Fehler ueber 1 Mio Dateien im Paket
				System.out.println( "Files in content more than 1000000: " + numFilesContent );

				// val.error.xml.ag.toomanyfilessip =
				// <Message>Im /content befinden sich {0} Dateien. Maximal sind 1 Million Dateien im
				// SIP/content erlaubt.</Message></Error>
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ag_SIP )
								+ getTextResourceService().getText( locale, ERROR_XML_AG_TOOMANYFILESSIP,
										String.valueOf( numFilesContent ) ) );
				isValid = false;
			}
			if ( numFilesContent > 4999 ) {
				// System.out.println( "Files in content more than 5000: " + numFilesContent );
				File valDateiContent = new File( valDatei.getAbsolutePath() + File.separator + "content" );

				Collection<File> files = FileUtils.listFilesAndDirs( valDateiContent, TrueFileFilter.TRUE,
						TrueFileFilter.TRUE );
				// .listFiles(valDateiContent, null, true);
				for ( File file2 : files ) {
					if ( file2.isDirectory() ) {
						// System.out.println(file2.getName());
						File entryDir = file2;
						String entryNameDir = entryDir.getAbsolutePath();
						// System.out.println( "entryDir : " + entryNameDir );
						Collection<File> filesInFolder = FileUtils.listFiles( entryDir, TrueFileFilter.INSTANCE,
								null );
						int numFilesDir = filesInFolder.size();
						// System.out.println( "Files in " + entryNameDir + ": " + numFilesDir );
						if ( numFilesDir > 5000 ) {
							// Warnung wenn mehr als 5000 Dateien direkt im Ordner
							// System.out.println( "Files in folder more than 5000: " + numFilesDir );
							entryNameDir = entryNameDir.replace( valDatei.getAbsolutePath(), "" );

							// val.error.xml.ag.toomanyfilesfolder =
							// <Message>Warnung: Im Ordner {0} befinden sich {1} Dateien. Ein einzelner Ordner
							// sollte nicht mehr als 5000 Dateien enthalten.</Message></Error>
							Logtxt.logtxt( logFile,
									getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ag_SIP )
											+ getTextResourceService().getText( locale, ERROR_XML_AG_TOOMANYFILESFOLDER,
													entryNameDir, String.valueOf( numFilesDir ) ) );
						}
					}
				}
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ag_SIP )
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			isValid = false;
		}
		return isValid;
	}
}
