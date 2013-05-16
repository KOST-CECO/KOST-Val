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

package ch.kostceco.tools.tiffval.validation.module3.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.enterag.utils.zip.EntryInputStream;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;
import ch.kostceco.tools.tiffval.exception.module3.Validation3cFormatValidationException;
import ch.kostceco.tools.tiffval.service.ConfigurationService;
import ch.kostceco.tools.tiffval.service.JhoveService;
import ch.kostceco.tools.tiffval.service.vo.ValidatedFormat;
import ch.kostceco.tools.tiffval.util.Util;
import ch.kostceco.tools.tiffval.validation.ValidationModuleImpl;
import ch.kostceco.tools.tiffval.validation.module3.Validation3cFormatValidationModule;

/**
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class Validation3cFormatValidationModuleImpl extends
		ValidationModuleImpl implements Validation3cFormatValidationModule
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
			throws Validation3cFormatValidationException
	{

		boolean isValid = true;

		Map<String, File> filesInSipFile = new HashMap<String, File>();

		Map<String, ValidatedFormat> mapValidatedFormats = new HashMap<String, ValidatedFormat>();
		List<ValidatedFormat> validatedFormats = getConfigurationService()
				.getValidatedFormats();
		for ( Iterator<ValidatedFormat> iterator = validatedFormats.iterator(); iterator
				.hasNext(); ) {
			ValidatedFormat validatedFormat = iterator.next();
			mapValidatedFormats.put( validatedFormat.getPronomUniqueId(),
					validatedFormat );
		}

		String nameOfSignature = getConfigurationService()
				.getPathToDroidSignatureFile();
		if ( nameOfSignature == null ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Cc )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									MESSAGE_CONFIGURATION_ERROR_NO_SIGNATURE ) );
			return false;
		}

		// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
		String pathToWorkDir0 = getConfigurationService().getPathToWorkDir();
		File tmpDir = new File( pathToWorkDir0 );
		if ( !tmpDir.exists() ) {
			tmpDir.mkdir();
		}

		String toplevelDir = tiffDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		try {
			Zip64File zipfile = new Zip64File( tiffDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {
				if ( !fileEntry.isDirectory() ) {
					byte[] buffer = new byte[8192];
					// Write the file to the original position in the fs.
					EntryInputStream eis = zipfile
							.openEntryInputStream( fileEntry.getName() );
					File newFile = new File( tmpDir, fileEntry.getName() );
					File parent = newFile.getParentFile();
					if ( !parent.exists() ) {
						parent.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream( newFile );
					for ( int iRead = eis.read( buffer ); iRead >= 0; iRead = eis
							.read( buffer ) ) {
						fos.write( buffer, 0, iRead );
					}
					eis.close();
					fos.close();
				}
			}
			zipfile.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Cc )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ e.getMessage() );
			return false;
		}

		String pathToWorkDir = getConfigurationService().getPathToWorkDir();
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		File workDir = new File( pathToWorkDir );
		Map<String, File> fileMap = Util.getFileMap( workDir, true );
		Set<String> fileMapKeys = fileMap.keySet();
		for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
				.hasNext(); ) {
			String entryName = iterator.next();
			File newFile = fileMap.get( entryName );

			if ( !newFile.isDirectory() ) {
				filesInSipFile.put( newFile.getAbsolutePath(), newFile );
			}
		}


		List<String> filesToProcessWithJhove = new ArrayList<String>();
		// List<String> filesToProcessWithPdftron = new ArrayList<String>();
		// List<String> filesToProcessWithSiardVal = new ArrayList<String>();

		Set<String> fileKeys = filesInSipFile.keySet();

		for ( Iterator<String> iterator = fileKeys.iterator(); iterator
				.hasNext(); ) {
			String fileKey = iterator.next();
			boolean selected = false;

			selected = true;

			// die PUID des SIP-Files wurde in der Liste der zu validierenden
			// Formate (gemäss Konfigurationsdatei) gefunden
			if ( selected ) {
				filesToProcessWithJhove.add( fileKey );
			}
		}

		// alt: alle Files, die mit JHove verarbeitet werden, bulk-mässig an die
		// Applikation übergeben
		// neu: die Files werden nach Typ sortiert und aufgeteilt, also z.B.
		// alle wav werden zusammen
		// übergeben, dann all pdf etc.
		// Die Outputs werden in einem einzigen File konkatiniert.

		Map<String, StringBuffer> extensionsMap = new HashMap<String, StringBuffer>();

		for ( String pathToProcessWithJhove : filesToProcessWithJhove ) {
			int idxLastDot = pathToProcessWithJhove.lastIndexOf( "." );
			String extension = pathToProcessWithJhove
					.substring( idxLastDot + 1 );
			StringBuffer sbPath = extensionsMap.get( extension );
			if ( sbPath == null ) {
				sbPath = new StringBuffer( "\"" );
				sbPath.append( pathToProcessWithJhove );
			} else {
				sbPath.append( "\"" );
				sbPath.append( pathToProcessWithJhove );
			}
			sbPath.append( "\" " );
			extensionsMap.put( extension, sbPath );
		}

		File jhoveReport = null;

		Map<String, Integer> countPerExtensionValid = new HashMap<String, Integer>();
		Map<String, Integer> countPerExtensionInvalid = new HashMap<String, Integer>();

		StringBuffer concatenatedOutputs = new StringBuffer();

		String pathToJhoveJar = getConfigurationService().getPathToJhoveJar();
		// Informationen zum Jhove-Jogverzeichnis holen
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

		Set<String> extMapKeys = extensionsMap.keySet();
		for ( String extMapKey : extMapKeys ) {

			StringBuffer pathsJhove = extensionsMap.get( extMapKey );
			String extension = extMapKey;
			if ( extension.equals( "tif" ) || extension.equals( "tiff" )
					|| extension.equals( "tfx" ) ) {
				try {
					jhoveReport = getJhoveService().executeJhove(
							pathToJhoveJar, pathsJhove.toString(),
							pathToJhoveOutput, tiffDatei.getName(), extMapKey );

					BufferedReader in = new BufferedReader( new FileReader(
							jhoveReport ) );
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
								Integer countInvalid = countPerExtensionInvalid
										.get( extMapKey );
								if ( countInvalid == null ) {
									countPerExtensionInvalid.put( extMapKey,
											new Integer( 1 ) );
								} else {
									countInvalid = countInvalid + 1;
									countPerExtensionInvalid.put( extMapKey,
											countInvalid );
								}
								isValid = false;

							}
							if ( line.contains( "Well-Formed and valid" ) ) {
								// Valider Status
								Integer countValid = countPerExtensionValid
										.get( extMapKey );
								if ( countValid == null ) {
									countPerExtensionValid.put( extMapKey,
											new Integer( 1 ) );
								} else {
									countValid = countValid + 1;
									countPerExtensionValid.put( extMapKey,
											countValid );
								}
							}
						}
					}
					in.close();

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService()
									.getText( MESSAGE_MODULE_Cc )
									+ getTextResourceService().getText(
											MESSAGE_DASHES ) + e.getMessage() );
					return false;
				}

			} else {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ getTextResourceService().getText(
										MESSAGE_MODULE_CC_NOJHOVEVAL )
								+ extension );
			}

		}

		// die im StringBuffer konkatinierten Outputs der einzelnen
		// JHove-Verarbeitungen
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
										MESSAGE_MODULE_Cc )
										+ getTextResourceService().getText(
												MESSAGE_DASHES )
										+ getTextResourceService()
												.getText(
														MESSAGE_MODULE_CC_CANNOTWRITEJHOVEREPORT ) );
				return false;
			}
		}

		Set<String> validKeys = countPerExtensionValid.keySet();
		for ( String validKey : validKeys ) {
			Integer valid = countPerExtensionValid.get( validKey );
			if ( valid == null ) {
				valid = new Integer( 0 );
			}
			Integer invalid = countPerExtensionInvalid.get( validKey );
			if ( invalid == null ) {
				invalid = new Integer( 0 );
			}

			String msg = validKey + " Valid = " + valid.toString()
					+ ", Invalid = " + invalid.toString();

			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Cc )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ msg );
		}

		return isValid;
	}

}