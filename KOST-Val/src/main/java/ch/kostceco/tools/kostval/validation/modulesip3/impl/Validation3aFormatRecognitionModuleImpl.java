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

package ch.kostceco.tools.kostval.validation.modulesip3.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3aFormatRecognitionException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3aFormatRecognitionModule;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */

public class Validation3aFormatRecognitionModuleImpl extends
		ValidationModuleImpl implements Validation3aFormatRecognitionModule
{

	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation3aFormatRecognitionException
	{
		boolean valid = true;

		Map<String, File> filesInSipFile = new HashMap<String, File>();

		String nameOfSignature = getConfigurationService()
				.getPathToDroidSignatureFile();
		if ( nameOfSignature == null ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText(
									MESSAGE_XML_CONFIGURATION_ERROR_NO_SIGNATURE ) );
			return false;
		}
		// existiert die SignatureFile am angebenen Ort?
		File fnameOfSignature = new File( nameOfSignature );
		if ( !fnameOfSignature.exists() ) {
			getMessageService().logInfo(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText(
									MESSAGE_XML_CA_DROID ) );
			return false;
		}

		Droid droid = null;
		try {
			// kleiner Hack, weil die Droid libraries irgendwo ein System.out
			// drin haben, welche den Output stören
			// Util.switchOffConsole() als Kommentar markieren wenn man die
			// Fehlermeldung erhalten möchte
			Util.switchOffConsole();
			droid = new Droid();

			droid.readSignatureFile( nameOfSignature );

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_CANNOT_INITIALIZE_DROID ) );
			return false;
		} finally {
			Util.switchOnConsole();
		}

		// Die Archivdatei wurde bereits vom Schritt 1d in das
		// Arbeitsverzeichnis entpackt
		String pathToWorkDir = getConfigurationService().getPathToWorkDir()
				+ "\\ZIP"
				+ valDatei.getName() + "\\content" ;
		File workDir = new File( pathToWorkDir );
		if ( !workDir.exists() ) {
			pathToWorkDir= valDatei.getAbsolutePath() + "\\content" ;
			workDir = new File( pathToWorkDir );
		}

		Map<String, File> fileMap = Util.getFileMap( workDir, true );
		Set<String> fileMapKeys = fileMap.keySet();
		for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
				.hasNext(); ) {
			String entryName = iterator.next();
			File newFile = fileMap.get( entryName );

			if ( !newFile.isDirectory() ) {
				filesInSipFile.put( entryName, newFile );
			}
		}

		Map<String, String> hPuids = getConfigurationService()
				.getAllowedPuids();
		Map<String, Integer> counterPuid = new HashMap<String, Integer>();

		Set<String> fileKeys = filesInSipFile.keySet();

		for ( Iterator<String> iterator = fileKeys.iterator(); iterator
				.hasNext(); ) {
			String fileKey = iterator.next();
			File file = filesInSipFile.get( fileKey );

			IdentificationFile ifile = droid.identify( file.getAbsolutePath() );

			if ( ifile.getNumHits() > 0 ) {

				for ( int x = 0; x < ifile.getNumHits(); x++ ) {
					FileFormatHit ffh = ifile.getHit( x );
					FileFormat ff = ffh.getFileFormat();

					String extensionConfig = hPuids.get( ff.getPUID() );

					if ( extensionConfig == null ) {
						valid = false;

						if ( counterPuid.get( ff.getPUID() ) == null ) {
							counterPuid.put( ff.getPUID(), new Integer( 1 ) );
						} else {
							Integer count = counterPuid.get( ff.getPUID() );
							counterPuid.put( ff.getPUID(),
									new Integer( count.intValue() + 1 ) );
						}
					}

				}

			}
		}

		Set<String> keysExt = counterPuid.keySet();
		for ( Iterator<String> iterator = keysExt.iterator(); iterator
				.hasNext(); ) {
			String keyExt = iterator.next();
			Integer value = counterPuid.get( keyExt );
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ca_SIP )
							+ getTextResourceService().getText(
									MESSAGE_XML_CA_FILES, keyExt,
									value.toString() ) );
			valid = false;
		}
		return valid;
	}

}
