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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;
import ch.kostceco.tools.tiffval.enums.PronomUniqueIdEnum;
import ch.kostceco.tools.tiffval.exception.module3.Validation3cFormatValidationException;
import ch.kostceco.tools.tiffval.service.ConfigurationService;
import ch.kostceco.tools.tiffval.service.JhoveService;
import ch.kostceco.tools.tiffval.service.PdftronService;
import ch.kostceco.tools.tiffval.service.SiardValService;
import ch.kostceco.tools.tiffval.service.vo.ValidatedFormat;
import ch.kostceco.tools.tiffval.util.PdftronErrorCodes;
import ch.kostceco.tools.tiffval.util.Util;
import ch.kostceco.tools.tiffval.validation.ValidationModuleImpl;
import ch.kostceco.tools.tiffval.validation.module3.Validation3cFormatValidationModule;

/**
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class Validation3cFormatValidationModuleImpl extends
		ValidationModuleImpl implements Validation3cFormatValidationModule
{

	private PdftronService			pdftronService;
	private SiardValService			siardValService;
	private ConfigurationService	configurationService;
	private JhoveService			jhoveService;

	public static String			NEWLINE	= System.getProperty( "line.separator" );

	public PdftronService getPdftronService()
	{
		return pdftronService;
	}

	public void setPdftronService( PdftronService pdftronService )
	{
		this.pdftronService = pdftronService;
	}

	public SiardValService getSiardValService()
	{
		return siardValService;
	}

	public void setSiardValService( SiardValService siardValService )
	{
		this.siardValService = siardValService;
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
	}

	@Override
	public boolean validate( File sipDatei )
			throws Validation3cFormatValidationException
	{

		boolean isValid = true;
		Integer zaehlerWait = 1;
		System.out.print( getTextResourceService().getText(
				MESSAGE_MODULE_WAITZAEHLER, zaehlerWait ) );
		System.out.flush();

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

		Droid droid = null;
		try {
			Util.switchOffConsole();
			droid = new Droid();

			String pathOfDroidConfig = getConfigurationService()
					.getPathOfDroidSignatureFile();
			droid.readSignatureFile( pathOfDroidConfig );
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
		File workDir = new File( pathToWorkDir );
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
						 );

		List<String> filesToProcessWithJhove = new ArrayList<String>();
		List<String> filesToProcessWithPdftron = new ArrayList<String>();
		List<String> filesToProcessWithSiardVal = new ArrayList<String>();

		Set<String> fileKeys = filesInSipFile.keySet();

		for ( Iterator<String> iterator = fileKeys.iterator(); iterator
				.hasNext(); ) {
			String fileKey = iterator.next();
			File file = filesInSipFile.get( fileKey );

			// eine der PUIDs des archivierten Files muss in der Konfiguration
			// als validatedformat vorkommen,
			// diese Konfiguration bestimmt, ob ein File selektiert wird zur
			// Format-Validierung mit JHOVE, Pdftron oder SIARD-Val
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
				// welchem Validator (JHOVE, Pdftron oder SIARD-Val)
				// untersucht wird
				if ( value.getValidator().equals(
						PronomUniqueIdEnum.PDFTRON.name() ) ) {
					filesToProcessWithPdftron.add( fileKey );
				} else if ( value.getValidator().equals(
						PronomUniqueIdEnum.JHOVE.name() ) ) {
					filesToProcessWithJhove.add( fileKey );
				} else if ( value.getValidator().equals(
						PronomUniqueIdEnum.SIARDVAL.name() ) ) {
					filesToProcessWithSiardVal.add( fileKey );
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
			if ( extension.equals( "gif" ) || extension.equals( "html" )
					|| extension.equals( "htm" ) || extension.equals( "jpg" )
					|| extension.equals( "jpeg" ) || extension.equals( "jpe" )
					|| extension.equals( "jfif" ) || extension.equals( "jfi" )
					|| extension.equals( "jif" ) || extension.equals( "jls" )
					|| extension.equals( "spf" ) || extension.equals( "jp2" )
					|| extension.equals( "jpg2" ) || extension.equals( "j2c" )
					|| extension.equals( "jpf" ) || extension.equals( "jpx" )
					|| extension.equals( "pdf" ) || extension.equals( "tif" )
					|| extension.equals( "tiff" ) || extension.equals( "tfx" )
					|| extension.equals( "wav" ) || extension.equals( "wave" )
					|| extension.equals( "bwf" ) || extension.equals( "xml" )
					|| extension.equals( "xsd" ) ) {
				try {
					jhoveReport = getJhoveService().executeJhove(
							pathToJhoveJar, pathsJhove.toString(),
							pathToJhoveOutput, sipDatei.getName(), extMapKey );

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
			System.out
					.print( "\r                                                                                                                                     " );
			System.out.flush();
			System.out.print( "\r" );
			System.out.flush();

			zaehlerWait = zaehlerWait + 1;

			System.out.print( getTextResourceService().getText(
					MESSAGE_MODULE_WAITZAEHLER, zaehlerWait ) );
			System.out.flush();

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

		// alle Files, die mit Pdftron verarbeitet werden, bulk-mässig an die
		// Applikation übergeben
		if ( filesToProcessWithPdftron.size() > 0 ) {

			StringBuffer pathsPdftron = new StringBuffer();
			for ( String pathToProcessWithPdftron : filesToProcessWithPdftron ) {
				pathsPdftron.append( "\"" );
				pathsPdftron.append( pathToProcessWithPdftron );
				pathsPdftron.append( "\"" );
				pathsPdftron.append( " " );
			}

			String pathToPdftronExe = getConfigurationService()
					.getPathToPdftronExe();
			getPdftronService().setPathToPdftronExe( pathToPdftronExe );

			// Informationen zum PDFTRON-Logverzeichnis holen
			String pathToPdftronOutput = getConfigurationService()
					.getPathToPdftronOutputFolder();

			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			File pdftronDir = new File( pathToPdftronOutput );
			if ( !pdftronDir.exists() ) {
				pdftronDir.mkdir();
			}

			try {
				String pathToPdftronReport = getPdftronService()
						.executePdftron( pathToPdftronExe,
								pathsPdftron.toString(), pathToPdftronOutput,
								sipDatei.getName() );

				Util.setPathToReportPdftron( pathToPdftronReport );

				// aus dem Output von Pdftron die Fehlercodes extrahieren und
				// übersetzen
				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( pathToPdftronReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( bis );
				doc.normalize();
				NodeList nodeLst = doc.getElementsByTagName( "Error" );

				Map<String, Integer> errorCounter = new HashMap<String, Integer>();

				// Bsp. für einen Error Code: <Error Code="e_PDFA173"
				// die erste Ziffer nach e_PDFA ist der Error Code.
				for ( int s = 0; s < nodeLst.getLength(); s++ ) {
					Node dateiNode = nodeLst.item( s );
					NamedNodeMap nodeMap = dateiNode.getAttributes();
					Node errorNode = nodeMap.getNamedItem( "Code" );
					String errorCode = errorNode.getNodeValue();
					String errorDigit = errorCode.substring( 6, 7 );

					// der Error Code kann auch "Unknown" sein, dieser wird in
					// den Code "0" übersetzt
					if ( errorDigit.equals( "U" ) ) {
						errorDigit = "0";
					}

					Integer errorCount = errorCounter.get( errorDigit );
					if ( errorCount == null ) {
						errorCounter.put( errorDigit, 1 );
					} else {
						errorCount = errorCount + 1;
						errorCounter.put( errorDigit, errorCount );
					}
				}

				StringBuffer errorSummary = new StringBuffer();
				errorSummary.append( getTextResourceService().getText(
						MESSAGE_MODULE_CC_ERRORS_IN ) );
				Integer totalErrors = new Integer( 0 );

				List<String> sortedKeys = new ArrayList<String>();
				sortedKeys.addAll( errorCounter.keySet() );
				Collections.sort( sortedKeys );

				for ( Iterator<String> iterator = sortedKeys.iterator(); iterator
						.hasNext(); ) {
					String errorCounterKey = iterator.next();

					Integer value = errorCounter.get( errorCounterKey );
					totalErrors = totalErrors + value;

					errorSummary.append( value.toString() );
					errorSummary.append( " " );
					errorSummary.append( PdftronErrorCodes
							.getErrorLabel( errorCounterKey ) );
					if ( iterator.hasNext() ) {
						errorSummary.append( ", " );
					} else {
						errorSummary.append( ")" );
					}
				}

				Integer passCount = new Integer( 0 );

				NodeList nodeLstI = doc.getElementsByTagName( "Pass" );

				// Valide pdfa-Dokumente enthalten
				// "<Validation> <Pass FileName..."
				// Anzahl pass = anzahl Valider pdfa
				for ( int s = 0; s < nodeLstI.getLength(); s++ ) {
					passCount = passCount + 1;
				}

				Integer failCount = new Integer( 0 );

				NodeList nodeLstII = doc.getElementsByTagName( "Fail" );

				// Invalide pdfa-Dokumente enthalten "<Validation> <Fail..."
				// Anzahl fail = anzahl invalider pdfa
				for ( int s = 0; s < nodeLstII.getLength(); s++ ) {
					failCount = failCount + 1;
				}

				int iValidPdfs = passCount;
				totalErrors = failCount;
				String sValidPdfs = "pdf Valid = " + iValidPdfs + ", ";

				if ( totalErrors == 0 ) {
					errorSummary = new StringBuffer( sValidPdfs
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_INVALID ) + totalErrors );
				} else {
					isValid = false;
					errorSummary = new StringBuffer( sValidPdfs
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_INVALID ) + totalErrors
							+ " " + errorSummary.toString() );
				}

				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES )
								+ errorSummary.toString() );

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_MODULE_Cc )
								+ getTextResourceService().getText(
										MESSAGE_DASHES ) + e.getMessage() );

				System.out
						.print( "\r                                                                                                                                     " );
				System.out.flush();
				System.out.print( "\r" );
				System.out.flush();

				return false;
			}
			System.out
					.print( "\r                                                                                                                                     " );
			System.out.flush();
			System.out.print( "\r" );
			System.out.flush();

			zaehlerWait = zaehlerWait + 1;

			System.out.print( getTextResourceService().getText(
					MESSAGE_MODULE_WAITZAEHLER, zaehlerWait ) );
			System.out.flush();
		}

		// alle Files, die mit SIARD-Val verarbeitet werden, einzel an die
		// Applikation übergeben

		if ( filesToProcessWithSiardVal.size() > 0 ) {

			String siardReport = null;

			StringBuffer concatenatedOutputs2 = new StringBuffer();

			String pathToSiardValJar = getConfigurationService()
					.getPathToSiardValJar();

			getSiardValService().setPathToSiardValJar( pathToSiardValJar );

			// Informationen zum SIARD-Val-Logverzeichnis holen
			String pathToSiardValOutput = getConfigurationService()
					.getPathToSiardValOutputFolder();

			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */

			File siardvalDir = new File( pathToSiardValOutput );
			if ( !siardvalDir.exists() ) {
				siardvalDir.mkdir();
			}

			Integer countInvalid = new Integer( 0 );
			Integer countValid = new Integer( 0 );
			Integer failCountA = new Integer( 0 );
			Integer failCountB = new Integer( 0 );
			Integer failCountC = new Integer( 0 );
			Integer failCountD = new Integer( 0 );
			Integer failCountE = new Integer( 0 );
			Integer failCountF = new Integer( 0 );
			Integer failCountG = new Integer( 0 );
			Integer failCountH = new Integer( 0 );
			Integer failCountI = new Integer( 0 );
			Integer failCountJ = new Integer( 0 );

			for ( String pathToProcessWithSiardVal : filesToProcessWithSiardVal ) {
				String pathsSiardVal = new String( pathToProcessWithSiardVal );

				try {
					siardReport = getSiardValService().executeSiardVal(
							pathToSiardValJar, pathsSiardVal.toString(),
							pathToSiardValOutput, sipDatei.getName() );

					BufferedReader in = new BufferedReader( new FileReader(
							siardReport ) );
					String line;

					while ( (line = in.readLine()) != null ) {

						concatenatedOutputs2.append( line );
						concatenatedOutputs2.append( NEWLINE );

						// Valide SIARD-Datei enthält: "TOTAL = Valid"
						// Invalide SIARD-Datei enthält: "TOTAL = Invalid"
						// if ( line.contains( "Status:" ) ) {
						if ( line.contains( "TOTAL = Invalid" ) ) {
							// Invalider Status
							countInvalid = countInvalid + 1;
							isValid = false;
						}
						if ( line.contains( "TOTAL = Valid" ) ) {
							// Valider Status
							countValid = countValid + 1;
						}
						if ( line.contains( "[A] = Invalid" ) ) {
							// Invalides Modul A
							failCountA = failCountA + 1;
						}
						if ( line.contains( "[B] = Invalid" ) ) {
							failCountB = failCountB + 1;
						}
						if ( line.contains( "[C] = Invalid" ) ) {
							failCountC = failCountC + 1;
						}
						if ( line.contains( "[D] = Invalid" ) ) {
							failCountD = failCountD + 1;
						}
						if ( line.contains( "[E] = Invalid" ) ) {
							failCountE = failCountE + 1;
						}
						if ( line.contains( "[F] = Invalid" ) ) {
							failCountF = failCountF + 1;
						}
						if ( line.contains( "[G] = Invalid" ) ) {
							failCountG = failCountG + 1;
						}
						if ( line.contains( "[H] = Invalid" ) ) {
							failCountH = failCountH + 1;
						}
						if ( line.contains( "[I] = Invalid" ) ) {
							failCountI = failCountI + 1;
						}
						if ( line.contains( "[J] = Invalid" ) ) {
							failCountJ = failCountJ + 1;
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

			System.out
					.print( "\r                                                                                                                                     " );
			System.out.flush();
			System.out.print( "\r" );
			System.out.flush();

			zaehlerWait = zaehlerWait + 1;

			System.out.print( getTextResourceService().getText(
					MESSAGE_MODULE_WAITZAEHLER, zaehlerWait ) );
			System.out.flush();

			Integer iValidSiard = countValid;
			Integer totalErrors = countInvalid;

			StringBuffer errorSummary;

			// Log Ausgabe erstellen nach diesem Prinzip Beispiel:
			// siard Valide = 1, Invalid = 4 (Anzahl Fehler nach Typen:
			// 1 Lesbarkeit 4 Content 1 Erkennung)
			String sValidSiard = "siard Valid = " + iValidSiard + ", ";
			if ( totalErrors == 0 ) {
				errorSummary = new StringBuffer( sValidSiard
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID ) + totalErrors );
			} else {
				isValid = false;
				errorSummary = new StringBuffer( sValidSiard
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_INVALID )
						+ totalErrors
						+ " "
						+ getTextResourceService().getText(
								MESSAGE_MODULE_CC_ERRORS_IN ) );
				if ( failCountA != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountA
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_A ) );
				}
				if ( failCountB != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountB
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_B ) );
				}
				if ( failCountC != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountC
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_C ) );
				}
				if ( failCountD != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountD
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_D ) );
				}
				if ( failCountE != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountE
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_E ) );
				}
				if ( failCountF != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountF
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_F ) );
				}
				if ( failCountG != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountG
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_G ) );
				}
				if ( failCountH != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountH
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_H ) );
				}
				if ( failCountI != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountI
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_I ) );
				}
				if ( failCountJ != 0 ) {
					errorSummary = new StringBuffer( errorSummary.toString()
							+ failCountJ
							+ getTextResourceService().getText(
									MESSAGE_MODULE_CC_ERRORS_IN_J ) );
				}
				errorSummary = new StringBuffer( errorSummary.toString() + ")" );
			}

			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Cc )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ errorSummary.toString() );

			// TODO: die im StringBuffer konkatinierten Outputs der einzelnen
			// Verarbeitungen wieder in das Output-File zurückschreiben
			if ( siardReport != null ) {
				try {
					BufferedWriter out = new BufferedWriter( new FileWriter(
							siardReport ) );
					out.write( concatenatedOutputs2.toString() );
					out.close();
					Util.setPathToReportSiardVal( siardReport );

				} catch ( IOException e ) {
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_MODULE_Cc )
											+ getTextResourceService().getText(
													MESSAGE_DASHES )
											+ getTextResourceService()
													.getText(
															MESSAGE_MODULE_CC_CANNOTWRITESIARDVALREPORT ) );
					return false;
				}
			}

		}

		System.out
				.print( "\r                                                                                                                                     " );
		System.out.flush();
		System.out.print( "\r" );
		System.out.flush();

		return isValid;
	}

}