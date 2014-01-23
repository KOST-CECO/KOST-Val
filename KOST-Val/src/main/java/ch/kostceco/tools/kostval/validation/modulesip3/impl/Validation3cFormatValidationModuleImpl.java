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

//TODO: Ganzes Modul umschreiben, da die Formatvalidierung intern stattfinden wird


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;
import ch.kostceco.tools.kostval.enums.PronomUniqueIdEnum;
import ch.kostceco.tools.kostval.exception.modulesip3.Validation3cFormatValidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
//import ch.kostceco.tools.kostval.service.JhoveService;
//import ch.kostceco.tools.kostval.service.KostValService;
//import ch.kostceco.tools.kostval.service.vo.ValidatedFormat;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3cFormatValidationModule;

/**
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */


public class Validation3cFormatValidationModuleImpl extends
		ValidationModuleImpl implements Validation3cFormatValidationModule
{
/*
	private KostValService			kostValService;
	private ConfigurationService	configurationService;
	private JhoveService			jhoveService;

	public static String			NEWLINE	= System.getProperty( "line.separator" );

	public KostValService getKostValService()
	{
		return kostValService;
	}

	public void setKostValService( KostValService kostValService )
	{
		this.kostValService = kostValService;
	}

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
	}*/

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation3cFormatValidationException
	{

		boolean isValid = true;

/*		Map<String, File> filesInSipFile = new HashMap<String, File>();

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

		Droid droid = null;
		try {
			Util.switchOffConsole();
			droid = new Droid();

			droid.readSignatureFile( nameOfSignature );
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Cc )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ getTextResourceService().getText(
									ERROR_CANNOT_INITIALIZE_DROID ) );
			return false;
		} finally {
			Util.switchOnConsole();
		}

		// Die Archivdatei wurde bereits vom Schritt 1d in das
		// Arbeitsverzeichnis entpackt
		int countContentFiles = 0;
		String pathToWorkDir = getConfigurationService().getPathToWorkDir();
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
/*		File workDir = new File( pathToWorkDir );
		Map<String, File> fileMap = Util.getFileMap( workDir, true );
		Set<String> fileMapKeys = fileMap.keySet();
		for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
				.hasNext(); ) {
			String entryName = iterator.next();
			File newFile = fileMap.get( entryName );

			if ( !newFile.isDirectory()
					&& newFile.getAbsolutePath().contains( "\\content\\" ) ) {
				filesInSipFile.put( newFile.getAbsolutePath(), newFile );
				countContentFiles++;
			}
		}

		getMessageService().logError(
				getTextResourceService().getText( MESSAGE_MODULE_Cc )
						+ getTextResourceService().getText( MESSAGE_DASHES )
						+ String.valueOf( countContentFiles )
						+ " "
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CD_NUMBER_OF_CONTENT_FILES ) );

		List<String> filesToProcessWithJhove = new ArrayList<String>();
		List<String> filesToProcessWithKostVal = new ArrayList<String>();

		Set<String> fileKeys = filesInSipFile.keySet();

		for ( Iterator<String> iterator = fileKeys.iterator(); iterator
				.hasNext(); ) {
			String fileKey = iterator.next();
			File file = filesInSipFile.get( fileKey );

			// eine der PUIDs des archivierten Files muss in der Konfiguration
			// als validatedformat vorkommen,
			// diese Konfiguration bestimmt, ob ein File selektiert wird zur
			// Format-Validierung mit JHOVE oder KOST-Val
			boolean selected = false;
			ValidatedFormat value = null;

			IdentificationFile ifile = droid.identify( file.getAbsolutePath() );

			for ( int x = 0; x < ifile.getNumHits(); x++ ) {
				FileFormatHit ffh = ifile.getHit( x );
				FileFormat ff = ffh.getFileFormat();
				String puid = ff.getPUID();

				value = mapValidatedFormats.get( puid );

				if ( value != null ) {
					selected = true;
					break;
				}
			}

			// die PUID des SIP-Files wurde in der Liste der zu validierenden
			// Formate (gemäss Konfigurationsdatei) gefunden
			if ( selected ) {
				// in der Konfiguration wird bestimmt, welcher PUID-Typ mit
				// welchem Validator (JHOVE oder KOST-Val)
				// untersucht wird
				if ( value.getValidator().equals(
						PronomUniqueIdEnum.JHOVE.name() ) ) {
					filesToProcessWithJhove.add( fileKey );
				} else if ( value.getValidator().equals(
						PronomUniqueIdEnum.KOSTVAL.name() ) ) {
					filesToProcessWithKostVal.add( fileKey );
				}
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
		String pathToJhoveOutput = directoryOfLogfile.getAbsolutePath();

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

/*		File jhoveDir = new File( pathToJhoveOutput );
		if ( !jhoveDir.exists() ) {
			jhoveDir.mkdir();
		}

		Set<String> extMapKeys = extensionsMap.keySet();
		for ( String extMapKey : extMapKeys ) {
			StringBuffer pathsJhove = extensionsMap.get( extMapKey );
			String extension = extMapKey;
			if ( extension.equals( "gif" ) || extension.equals( "html" )
					|| extension.equals( "htm" ) || extension.equals( "jpg" )
					|| extension.equals( "jpeg" ) || extension.equals( "jpe" )
					|| extension.equals( "jfif" ) || extension.equals( "jfi" )
					|| extension.equals( "jif" ) || extension.equals( "jls" )
					|| extension.equals( "spf" ) || extension.equals( "jp2" )
					|| extension.equals( "jpg2" ) || extension.equals( "j2c" )
					|| extension.equals( "jpf" ) || extension.equals( "jpx" )
					|| extension.equals( "wav" ) || extension.equals( "wave" )
					|| extension.equals( "bwf" ) || extension.equals( "xml" )
					|| extension.equals( "xsd" ) ) {
				try {
					jhoveReport = getJhoveService().executeJhove(
							pathToJhoveJar, pathsJhove.toString(),
							pathToJhoveOutput, valDatei.getName(), extMapKey );

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

		// alle Files, die mit KOST-Val verarbeitet werden, einzel an die
		// Applikation übergeben

		if ( filesToProcessWithKostVal.size() > 0 ) {

			String kostvalReport = null;

			StringBuffer concatenatedOutputs2 = new StringBuffer();

			String pathToKostValJar = getConfigurationService()
					.getPathToKostValJar();

			getKostValService().setPathToKostValJar( pathToKostValJar );

			// Informationen zum KOST-Val-Logverzeichnis holen
			String pathToKostValOutput = directoryOfLogfile.getAbsolutePath();

			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

/*			File kostvalDir = new File( pathToKostValOutput );
			if ( !kostvalDir.exists() ) {
				kostvalDir.mkdir();
			}

			Integer countInvalidSiard = new Integer( 0 );
			Integer countValidSiard = new Integer( 0 );
			Integer countInvalidTiff = new Integer( 0 );
			Integer countValidTiff = new Integer( 0 );
			Integer countInvalidPdfa = new Integer( 0 );
			Integer countValidPdfa = new Integer( 0 );

			Integer firstFileK = new Integer( 0 );

			for ( String pathToProcessWithKostVal : filesToProcessWithKostVal ) {
				firstFileK = firstFileK + 1;
				if ( firstFileK != 1 ) {
					// die einzelnen Logs sollen getrent werden
					concatenatedOutputs2.append( NEWLINE );
					concatenatedOutputs2
							.append( "***************************************************************************" );
					concatenatedOutputs2.append( NEWLINE );
				}

				String pathsKostVal = new String( pathToProcessWithKostVal );

				try {
					kostvalReport = getKostValService().executeKostVal(
							pathToKostValJar, pathsKostVal.toString(),
							pathToKostValOutput, valDatei.getName() );

					BufferedReader in = new BufferedReader( new FileReader(
							kostvalReport ) );
					String line;

					while ( (line = in.readLine()) != null ) {

						concatenatedOutputs2.append( line );
						concatenatedOutputs2.append( NEWLINE );

						// Valide Datei enthält: "TOTAL = Valid"
						// Invalide Datei enthält: "TOTAL = Invalid"
						if ( pathsKostVal.toLowerCase().endsWith( ".siard" ) ) {
							if ( line.contains( "TOTAL = Invalid" ) ) {
								// Invalider Status
								countInvalidSiard = countInvalidSiard + 1;
								isValid = false;
							}
							if ( line.contains( "TOTAL = Valid" ) ) {
								// Valider Status
								countValidSiard = countValidSiard + 1;
							}
						} else if ( pathsKostVal.toLowerCase()
								.endsWith( ".tif" )
								|| pathsKostVal.toLowerCase()
										.endsWith( ".tiff" ) ) {
							if ( line.contains( "TOTAL = Invalid" ) ) {
								// Invalider Status
								countInvalidTiff = countInvalidTiff + 1;
								isValid = false;
							}
							if ( line.contains( "TOTAL = Valid" ) ) {
								// Valider Status
								countValidTiff = countValidTiff + 1;
							}
						} else if ( pathsKostVal.toLowerCase()
								.endsWith( ".pdf" )
								|| pathsKostVal.toLowerCase()
										.endsWith( ".pdfa" ) ) {
							if ( line.contains( "TOTAL = Invalid" ) ) {
								// Invalider Status
								countInvalidPdfa = countInvalidPdfa + 1;
								isValid = false;
							}
							if ( line.contains( "TOTAL = Valid" ) ) {
								// Valider Status
								countValidPdfa = countValidPdfa + 1;
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
			}
			Integer iValidKostvalSiard = countValidSiard;
			Integer totalErrorsSiard = countInvalidSiard;
			Integer iValidKostvalTiff = countValidTiff;
			Integer totalErrorsTiff = countInvalidTiff;
			Integer iValidKostvalPdfa = countValidPdfa;
			Integer totalErrorsPdfa = countInvalidPdfa;

			StringBuffer errorSummarySiard;
			StringBuffer errorSummaryTiff;
			StringBuffer errorSummaryPdfa;

			// Log Ausgabe erstellen nach diesem Prinzip Beispiel:
			// siard Valide = 1, Invalid = 4
			String sValidKostvalSiard = "siard Valid = " + iValidKostvalSiard
					+ ", ";
			if ( totalErrorsSiard == 0 ) {
				errorSummarySiard = new StringBuffer( sValidKostvalSiard
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsSiard );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummarySiard.toString() );
			} else {
				isValid = false;
				errorSummarySiard = new StringBuffer( sValidKostvalSiard
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsSiard );
				errorSummarySiard = new StringBuffer(
						errorSummarySiard.toString() );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummarySiard.toString() );
			}

			// Log Ausgabe erstellen nach diesem Prinzip Beispiel:
			// tiff Valide = 1, Invalid = 4
			String sValidKostvalTiff = "tiff Valid = " + iValidKostvalTiff
					+ ", ";
			if ( totalErrorsTiff == 0 ) {
				errorSummaryTiff = new StringBuffer( sValidKostvalTiff
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsTiff );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummaryTiff.toString() );
			} else {
				isValid = false;
				errorSummaryTiff = new StringBuffer( sValidKostvalTiff
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsTiff );
				errorSummaryTiff = new StringBuffer(
						errorSummaryTiff.toString() );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummaryTiff.toString() );
			}

			// Log Ausgabe erstellen nach diesem Prinzip Beispiel:
			// pdfa Valide = 1, Invalid = 4
			String sValidKostvalPdfa = "pdfa Valid = " + iValidKostvalPdfa
					+ ", ";
			if ( totalErrorsPdfa == 0 ) {
				errorSummaryPdfa = new StringBuffer( sValidKostvalPdfa
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsPdfa );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummaryPdfa.toString() );
			} else {
				isValid = false;
				errorSummaryPdfa = new StringBuffer( sValidKostvalPdfa
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrorsPdfa );
				errorSummaryPdfa = new StringBuffer(
						errorSummaryPdfa.toString() );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummaryPdfa.toString() );
			}

			if ( kostvalReport != null ) {
				try {
					BufferedWriter out = new BufferedWriter( new FileWriter(
							kostvalReport ) );
					out.write( concatenatedOutputs2.toString() );
					out.close();
					Util.setPathToReportKostVal( kostvalReport );

				} catch ( IOException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_Cc )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															MESSAGE_MODULE_CC_CANNOTWRITEKOSTVALREPORT ) );
					return false;
				}
			}

		}*/
		return isValid;
	}

}