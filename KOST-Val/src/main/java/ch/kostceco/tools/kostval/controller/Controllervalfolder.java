/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
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

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;

/** kostval --> Controllervalfolder
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllervalfolder implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger( Controllervalfolder.class );
	private static TextResourceService	textResourceService;

	public static TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	@SuppressWarnings("static-access")
	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public boolean valFolder( File valDatei, String logFileName, File directoryOfLogfile,
			boolean verbose, String dirOfJarPath, Map<String, String> configMap,
			ApplicationContext context, Locale locale ) throws IOException
	{
		// Formatvalidierung ueber ein Ordner

		boolean valFolder = false;
		String countNioDetail = "";
		String countNioExtension = "";
		Integer countNio = 0;
		Integer countSummaryNio = 0;
		Integer countSummaryIo = 0;
		Integer count = 0;
		Integer countProgress = 0;
		Integer pdfaCountIo = 0;
		Integer pdfaCountNio = 0;
		Integer siardCountIo = 0;
		Integer siardCountNio = 0;
		Integer tiffCountIo = 0;
		Integer tiffCountNio = 0;
		Integer jp2CountIo = 0;
		Integer jp2CountNio = 0;
		Integer jpegCountIo = 0;
		Integer jpegCountNio = 0;
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );
		File logFile = new File( directoryOfLogfile.getAbsolutePath() + File.separator
				+ valDatei.getName() + ".kost-val.log.xml" );

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidation = configMap.get( "pdfavalidation" );
		String siardValidation = configMap.get( "siardValidation" );
		String tiffValidation = configMap.get( "tiffValidation" );
		String jp2Validation = configMap.get( "jp2Validation" );
		String jpegValidation = configMap.get( "jpegValidation" );

		String formatValOn = "";
		// ermitteln welche Formate validiert werden koennen respektive eingeschaltet sind
		if ( pdfaValidation.equals( "yes" ) ) {
			formatValOn = "PDF/A";
			if ( tiffValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", TIFF";
			}
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( tiffValidation.equals( "yes" ) ) {
			formatValOn = "TIFF";
			if ( jp2Validation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JP2";
			}
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jp2Validation.equals( "yes" ) ) {
			formatValOn = "JP2";
			if ( siardValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", SIARD";
			}
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( siardValidation.equals( "yes" ) ) {
			formatValOn = "SIARD";
			if ( jpegValidation.equals( "yes" ) ) {
				formatValOn = formatValOn + ", JPEG";
			}
		} else if ( jpegValidation.equals( "yes" ) ) {
			formatValOn = "JPEG";
		}

		if ( formatValOn.equals( "" ) ) {
			// Formatvalidierung aber alle Formate ausgeschlossen
			LOGGER.logError( getTextResourceService().getText( ERROR_IOE,
					getTextResourceService().getText( locale, ERROR_NOFILEENDINGS ) ) );
			System.out.println( getTextResourceService().getText( locale, ERROR_NOFILEENDINGS ) );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( "", logFile );
			valFolder = false;
			return valFolder;
		}

		try {
			Map<String, File> fileUnsortedMap = Util.getFileMapFile( valDatei );
			Map<String, File> fileMap = new TreeMap<String, File>( fileUnsortedMap );
			int numberInFileMap = fileMap.size();
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				// configmap neu auslesen im bereich pdf, da veraenderungen moeglich sind
				pdfaValidation = configMap.get( "pdfavalidation" );

				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );
				if ( !newFile.isDirectory() ) {
					valDatei = newFile;
					String valDateiName = valDatei.getName();
					String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
					count = count + 1;
					countProgress = countProgress + 1;

					if ( ((valDateiExt.equals( ".jp2" ))) && jp2Validation.equals( "yes" ) ) {
						int countToValidated = numberInFileMap - countProgress;
						System.out
								.print( countToValidated + " " + "JP2:   " + valDatei.getAbsolutePath() + " " );

						/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						 * dirOfJarPath, configMap, context ); */
						Controllervalfile controller1 = (Controllervalfile) context
								.getBean( "controllervalfile" );
						boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
								verbose, dirOfJarPath, configMap, context, locale );

						// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( valFile ) {
							jp2CountIo = jp2CountIo + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						} else {
							jp2CountNio = jp2CountNio + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						}
					} else if ( ((valDateiExt.equals( ".jpeg" ) || valDateiExt.equals( ".jpg" )
							|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".jpe" )))
							&& jpegValidation.equals( "yes" ) ) {
						int countToValidated = numberInFileMap - countProgress;
						System.out
								.print( countToValidated + " " + "JPEG:  " + valDatei.getAbsolutePath() + " " );

						/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						 * dirOfJarPath, configMap, context ); */
						Controllervalfile controller1 = (Controllervalfile) context
								.getBean( "controllervalfile" );
						boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
								verbose, dirOfJarPath, configMap, context, locale );

						// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( valFile ) {
							jpegCountIo = jpegCountIo + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						} else {
							jpegCountNio = jpegCountNio + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						}
					} else if ( ((valDateiExt.equals( ".tiff" )
							|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".tif" )))
							&& tiffValidation.equals( "yes" ) ) {
						int countToValidated = numberInFileMap - countProgress;
						System.out
								.print( countToValidated + " " + "TIFF:  " + valDatei.getAbsolutePath() + " " );

						/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						 * dirOfJarPath, configMap, context ); */
						Controllervalfile controller1 = (Controllervalfile) context
								.getBean( "controllervalfile" );
						boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
								verbose, dirOfJarPath, configMap, context, locale );

						// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( valFile ) {
							tiffCountIo = tiffCountIo + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						} else {
							tiffCountNio = tiffCountNio + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						}
					} else if ( (valDateiExt.equals( ".siard" )) && siardValidation.equals( "yes" ) ) {
						int countToValidated = numberInFileMap - countProgress;
						System.out
								.print( countToValidated + " " + "SIARD: " + valDatei.getAbsolutePath() + " " );

						/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						 * dirOfJarPath, configMap, context ); */
						Controllervalfile controller1 = (Controllervalfile) context
								.getBean( "controllervalfile" );
						boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
								verbose, dirOfJarPath, configMap, context, locale );

						// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( valFile ) {
							siardCountIo = siardCountIo + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						} else {
							siardCountNio = siardCountNio + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						}

					} else if ( ((valDateiExt.equals( ".pdf" )
							|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".pdfa" )))
							&& pdfaValidation.equals( "yes" ) ) {
						int countToValidated = numberInFileMap - countProgress;
						System.out.print( countToValidated + " " + "PDFA:  " + valDatei.getAbsolutePath() );

						/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
						 * dirOfJarPath, configMap, context ); */
						Controllervalfile controller1 = (Controllervalfile) context
								.getBean( "controllervalfile" );
						boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
								verbose, dirOfJarPath, configMap, context, locale );

						// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
						if ( tmpDir.exists() ) {
							Util.deleteDir( tmpDir );
						}
						if ( valFile ) {
							pdfaCountIo = pdfaCountIo + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						} else {
							pdfaCountNio = pdfaCountNio + 1;
							// Loeschen des Arbeitsverzeichnisses, falls eines angelegt wurde
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
						}
						if ( tmpDir.exists() ) {
							tmpDir.deleteOnExit();
						}
					} else {
						countNio = countNio + 1;
						countNioDetail = countNioDetail + "</Message></Info><Info><Message> - "
								+ valDatei.getAbsolutePath();
						if ( countNioExtension == "" ) {
							countNioExtension = valDateiExt;
						} else {
							// bereits Extensions vorhanden
							if ( countNioExtension.contains( valDateiExt ) ) {
								// Extension bereits erfasst
							} else {
								countNioExtension = countNioExtension + ", " + valDateiExt;
							}
						}
					}
				} else {
					// Ordner. Count aktualisieren
					countProgress = countProgress + 1;
				}
			}
		} catch ( Exception e ) {
			LOGGER.logError( "<Error>"
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
							"Formatvalidation: " + e.getMessage() )
					+ getTextResourceService().getText( locale, MESSAGE_XML_FORMAT2 )
					+ getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );
			System.out.println( "Exception: " + e.getMessage() );
		} catch ( StackOverflowError eso ) {
			LOGGER.logError( getTextResourceService().getText( locale, ERROR_XML_STACKOVERFLOWMAIN ) );
			System.out.println( "Exception: " + "StackOverflowError" );
		} catch ( OutOfMemoryError eoom ) {
			LOGGER.logError( getTextResourceService().getText( locale, ERROR_XML_OUTOFMEMORYMAIN ) );
			System.out.println( "Exception: " + "OutOfMemoryError" );
		}

		if ( countNio.equals( count ) ) {
			// keine Dateien Validiert
			LOGGER.logError(
					getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDINGS, formatValOn ) );
			System.out.println(
					getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDINGS, formatValOn ) );
		}

		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_FORMAT2 ) );

		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );

		File callasNo = new File( directoryOfLogfile + File.separator + "_callas_NO.txt" );
		if ( callasNo.exists() ) {
			callasNo.delete();
		}

		// Garbage Collecter aufruf zur Bereinigung
		System.gc();

		// logFile bereinigung (& End und ggf 3c)
		Util.valEnd3cAmp( "", logFile );

		countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio + jp2CountNio + jpegCountNio;
		countSummaryIo = pdfaCountIo + siardCountIo + tiffCountIo + jp2CountIo + jpegCountIo;

		/* Summary ueber Formatvaliderung herausgeben analog 3c.
		 * 
		 * message.xml.summary.3c = <Message>Von den {0} Dateien sind {1} ({4}%) valid, {2} ({5}%)
		 * invalid und {3} ({6}%) wurden nicht validiert.</Message></Error>
		 * 
		 * countNio=3 count=0 countSummaryIo=1 countSummaryNio=2
		 * 
		 * diese meldung einfach noch mit info einfassen. */

		float countSummaryIoP = 100 / (float) count * (float) countSummaryIo;
		float countSummaryNioP = 100 / (float) count * (float) countSummaryNio;
		float countNioP = 100 / (float) count * (float) countNio;
		String summaryFormat = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARY_FORMAT,
				count, countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
				countNioP );
		String summary = "";
		if ( countNio > 0 ) {
			// mit Detail weil countNio > 0
			summary = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARYDETAIL, count,
					countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP, countNioP,
					countNioDetail, countNioExtension );
		} else {
			// ohne Detail weil countNio == 0
			summary = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARY, count,
					countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP, countNioP );
		}
		String newFormat = "<Format>" + summary;
		Util.oldnewstring( "<Format>", newFormat, logFile );

		System.out.println( getTextResourceService().getText( locale, MESSAGE_FORMATVALIDATION_DONE,
				summaryFormat, logFile.getAbsolutePath() ) );

		if ( countNio.equals( count ) ) {
			// keine Dateien Validiert bestehendes Workverzeichnis ggf. loeschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}
			// System.exit( 1 );
			valFolder = true;
			return valFolder;
		} else if ( countSummaryNio == 0 ) {
			// bestehendes Workverzeichnis ggf. loeschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}

			File pathTemp = new File( directoryOfLogfile, "path.tmp" );
			/* falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, loeschen wir
			 * es */
			if ( pathTemp.exists() ) {
				pathTemp.delete();
			}
			if ( pathTemp.exists() ) {
				// hat nicht funktioniert -> Inhalt leeren
				List<String> oldtextList = Files.readAllLines( pathTemp.toPath(), StandardCharsets.UTF_8 );
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