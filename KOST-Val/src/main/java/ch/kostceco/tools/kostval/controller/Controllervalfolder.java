/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath,
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * kostval --> Controllervalfolder
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllervalfolder implements MessageConstants
{

	private static TextResourceService textResourceService;

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService(
			TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean valFolder( File valDatei, String logFileName,
			File directoryOfLogfile, boolean verbose, String dirOfJarPath,
			Map<String, String> configMap, ApplicationContext context,
			Locale locale, File logFile ) throws IOException
	{
		// Formatvalidierung und Erkennung ueber ein Ordner

		boolean valFolder = false;
		Integer count = 0;
		Integer countValid = 0;
		Integer countInvalid = 0;
		Integer countNotaz = 0;
		Integer countProgress = 0;
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );

		try {
			Map<String, File> fileUnsortedMap = Util.getFileMapFile( valDatei );
			Map<String, File> fileMap = new TreeMap<String, File>(
					fileUnsortedMap );
			int numberInFileMap = fileMap.size();
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
					.hasNext(); ) {

				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );
				if ( !newFile.isDirectory() ) {
					valDatei = newFile;

					count = count + 1;
					countProgress = countProgress + 1;
					int countToValidated = numberInFileMap - countProgress;

					// Kontrolle ob Datei akzeptiert ist und ob sie validiert
					// werden soll
					Controllervalfofile controller1 = (Controllervalfofile) context
							.getBean( "controllervalfofile" );
					String valFile = controller1.valFoFile( valDatei,
							logFileName, directoryOfLogfile, verbose,
							dirOfJarPath, configMap, context, locale, logFile,
							countToValidated );
					if ( valFile.equals( "countValid" ) ) {
						countValid = countValid + 1;
					} else if ( valFile.equals( "countNotaz" ) ) {
						countNotaz = countNotaz + 1;
					} else if ( valFile.equals( "countInvalid" ) ) {
						countInvalid = countInvalid + 1;
					} else {
						// normalerweise kein Bedarf
						countProgress = countProgress + 1;
					}

				} else {
					// Ordner. Count aktualisieren
					countProgress = countProgress + 1;
				}
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					"<Error>"
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN,
									"Formatvalidation: " + e.getMessage() )
							+ "</Format></KOSTValLog>" );
			System.out.println( "Exception: " + e.getMessage() );
		} catch ( StackOverflowError eso ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_STACKOVERFLOWMAIN ) );
			System.out.println( "Exception: " + "StackOverflowError" );
		} catch ( OutOfMemoryError eoom ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_OUTOFMEMORYMAIN ) );
			System.out.println( "Exception: " + "OutOfMemoryError" );
		}

		Logtxt.logtxt( logFile, "</Format></KOSTValLog>" );

		File callasNo = new File(
				directoryOfLogfile + File.separator + "_callas_NO.txt" );
		if ( callasNo.exists() ) {
			callasNo.delete();
		}

		// Garbage Collecter aufruf zur Bereinigung
		System.gc();

		// logFile bereinigung (& End und ggf 3c)
		Util.valEnd3cAmp( "", logFile );

		float countValidP = 100 / (float) count * (float) countValid;
		float countInvalidP = 100 / (float) count * (float) countInvalid;
		float countNotazP = 100 / (float) count * (float) countNotaz;
		String summaryFormat = getTextResourceService().getText( locale,
				MESSAGE_XML_SUMMARY_FORMAT, count.toString(),
				countValid.toString(), countInvalid.toString(),
				countNotaz.toString(), countValidP, countInvalidP,
				countNotazP );

		String summary = "";
		summary = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARY,
				count.toString(), countValid.toString(),
				countInvalid.toString(), countNotaz.toString(), countValidP,
				countInvalidP, countNotazP );

		String newFormat = "<Format>" + summary;
		Util.oldnewstring( "<Format>", newFormat, logFile );

		System.out.println( getTextResourceService().getText( locale,
				MESSAGE_FORMATVALIDATION_DONE, summaryFormat,
				logFile.getAbsolutePath() ) );

		if ( countInvalid == 0 && countNotaz == 0 ) {
			// bestehendes Workverzeichnis ggf. loeschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}

			File pathTemp = new File( directoryOfLogfile, "path.tmp" );
			/*
			 * falls das File bereits existiert, z.B. von einem vorhergehenden
			 * Durchlauf, loeschen wir es
			 */
			if ( pathTemp.exists() ) {
				pathTemp.delete();
			}
			if ( pathTemp.exists() ) {
				// hat nicht funktioniert -> Inhalt leeren
				List<String> oldtextList = Files.readAllLines(
						pathTemp.toPath(), StandardCharsets.UTF_8 );
				for ( int i = 0; i < oldtextList.size(); i++ ) {
					String oldtext = (oldtextList.get( i ));
					Util.oldnewstring( oldtext, "", pathTemp );
				}
			}

			// alle Validierten Dateien valide
			valFolder = true;
			return valFolder;
		} else {
			// bestehendes Workverzeichnis ggf. loeschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			// Fehler in Validierten Dateien --> invalide
			valFolder = false;
			return valFolder;
		}
	}
}