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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

import ch.kostceco.tools.kostval.logging.Logger;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.util.Zip64Archiver;

/** kostval --> Controllervalsip
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection eingebunden. */

public class Controllervalsip implements MessageConstants
{

	private static final Logger					LOGGER	= new Logger( Controllervalsip.class );
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

	public boolean valSip( File valDatei, String logFileName, File directoryOfLogfile,
			boolean verbose, String dirOfJarPath, Map<String, String> configMap,
			ApplicationContext context, Locale locale ) throws IOException
	{
		// SIP-Validierung

		boolean valSip = false;
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );
		File logFile = new File( directoryOfLogfile.getAbsolutePath() + File.separator
				+ valDatei.getName() + ".kost-val.log.xml" );

		// ggf alte SIP-Validierung-Versions-Notiz loeschen
		File ECH160_1_1 = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "ECH160_1.1.txt" );
		File ECH160_1_0 = new File(
				directoryOfLogfile.getAbsolutePath() + File.separator + "ECH160_1.0.txt" );
		if ( ECH160_1_1.exists() ) {
			Util.deleteFile( ECH160_1_1 );
		} else if ( ECH160_1_0.exists() ) {
			Util.deleteFile( ECH160_1_0 );
		}

		// Informationen holen, welche Formate validiert werden sollen
		String pdfaValidationPdftools = configMap.get( "pdftools" );
		String pdfaValidationCallas = configMap.get( "callas" );
		String pdfaValidation = "no";
		if ( pdfaValidationPdftools.equalsIgnoreCase( "yes" )
				|| pdfaValidationCallas.equalsIgnoreCase( "yes" ) ) {
			pdfaValidation = "yes";
		}
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
		LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_FORMAT1 ) );

		try {
			boolean validFormat = false;
			File originalSipFile = valDatei;
			File unSipFile = valDatei;
			File outputFile3c = null;
			String fileName3c = null;
			File tmpDirZip = null;
			String valDateiName = valDatei.getName();
			String valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();

			// zuerst eine Formatvalidierung ueber den Content dies ist analog aufgebaut wie --format
			String newFormat = "";
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

			if ( !valDatei.isDirectory() ) {
				Boolean zip = false;
				// Eine ZIP Datei muss mit PK.. beginnen
				if ( (valDateiExt.equals( ".zip" )
						|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".zip64" )) ) {

					FileReader fr = null;
					BufferedReader read = null;

					try {
						fr = new FileReader( valDatei );
						read = new BufferedReader( fr );

						// Hex 03 in Char umwandeln
						String str3 = "03";
						int i3 = Integer.parseInt( str3, 16 );
						char c3 = (char) i3;
						// Hex 04 in Char umwandeln
						String str4 = "04";
						int i4 = Integer.parseInt( str4, 16 );
						char c4 = (char) i4;

						// auslesen der ersten 4 Zeichen der Datei
						int length;
						int i;
						char[] buffer = new char[4];
						length = read.read( buffer );
						for ( i = 0; i != length; i++ )
							;

						// die beiden charArrays (soll und ist) mit einander vergleichen
						char[] charArray1 = buffer;
						char[] charArray2 = new char[] { 'P', 'K', c3, c4 };

						if ( Arrays.equals( charArray1, charArray2 ) ) {
							// hoechstwahrscheinlich ein ZIP da es mit 504B0304 respektive PK.. beginnt
							zip = true;
						}
						read.close();
						fr.close();
						// set to null
						read = null;
						fr = null;
					} catch ( Exception e ) {
						LOGGER.logError( "<Error>" + getTextResourceService().getText( locale,
								ERROR_XML_UNKNOWN, "ZIP-Header-Exception: " + e.getMessage() ) );
						System.out.println( "Exception: " + e.getMessage() );
						read.close();
						fr.close();
						// set to null
						read = null;
						fr = null;
					}
				}

				// wenn die Datei kein Directory ist, muss sie mit zip oder zip64 enden
				if ( (!(valDateiExt.equals( ".zip" )
						|| valDatei.getAbsolutePath().toLowerCase().endsWith( ".zip64" ))) || zip == false ) {
					// Abbruch! D.h. Sip message beginnen, Meldung und Beenden ab hier bis System.exit( 1 );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_FORMAT2 ) );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP1 ) );
					valDatei = originalSipFile;
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
							getTextResourceService().getText( locale, MESSAGE_SIPVALIDATION ) ) );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALFILE,
							valDatei.getAbsolutePath() ) );
					System.out.println( "" );
					System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

					// die eigentliche Fehlermeldung
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Aa_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_AA_INCORRECTFILEENDING ) );
					System.out.println(
							getTextResourceService().getText( locale, ERROR_XML_AA_INCORRECTFILEENDING ) );

					// Fehler im Validierten SIP --> invalide & Abbruch
					LOGGER.logError(
							getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
					LOGGER.logError(
							getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
					System.out.println( "Invalid" );
					System.out.println( "" );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP2 ) );
					LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );

					// logFile bereinigung (& End und ggf 3c)
					Util.valEnd3cAmp( "", logFile );

					// bestehendes Workverzeichnis ggf. loeschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( tmpDir.exists() ) {
						tmpDir.deleteOnExit();
					}
					System.exit( 1 );

				} else {
					// geziptes SIP --> in temp dir entzipen
					String toplevelDir = valDatei.getName();
					int lastDotIdx = toplevelDir.lastIndexOf( "." );
					toplevelDir = toplevelDir.substring( 0, lastDotIdx );
					tmpDirZip = new File(
							tmpDir.getAbsolutePath() + File.separator + "ZIP" + File.separator + toplevelDir );
					try {
						FileInputStream fis = null;
						ZipInputStream zipfile = null;
						ZipEntry zEntry = null;
						fis = new FileInputStream( valDatei );
						zipfile = new ZipInputStream( new BufferedInputStream( fis ) );
						while ( (zEntry = zipfile.getNextEntry()) != null ) {
							try {
								if ( !zEntry.isDirectory() ) {
									byte[] tmp = new byte[4 * 1024];
									FileOutputStream fos = null;
									String opFilePath = tmpDirZip + File.separator + zEntry.getName();
									File newFile = new File( opFilePath );
									File parent = newFile.getParentFile();
									if ( !parent.exists() ) {
										parent.mkdirs();
									}
									// System.out.println( "Extracting file to " + newFile.getAbsolutePath() );
									fos = new FileOutputStream( opFilePath );
									int size = 0;
									while ( (size = zipfile.read( tmp )) != -1 ) {
										fos.write( tmp, 0, size );
									}
									fos.flush();
									fos.close();
								} else {
									/* Scheibe den Ordner wenn noch nicht vorhanden an den richtigen Ort respektive in
									 * den richtigen Ordner der ggf angelegt werden muss. Dies muss gemacht werden,
									 * damit auch leere Ordner ins Work geschrieben werden. Diese werden danach im J
									 * als Fehler angegeben */
									File newFolder = new File( tmpDirZip, zEntry.getName() );
									if ( !newFolder.exists() ) {
										File parent = newFolder.getParentFile();
										if ( !parent.exists() ) {
											parent.mkdirs();
										}
										newFolder.mkdirs();
									}
								}
							} catch ( IOException e ) {
								System.out.println( e.getMessage() );
							}
						}
						zipfile.close();
						// set to null
						zipfile = null;
					} catch ( Exception e ) {
						try {
							Zip64Archiver.unzip64( valDatei, tmpDirZip );
						} catch ( Exception e1 ) {
							// Abbruch! D.h. Sip message beginnen, Meldung und Beenden ab hier bis System.exit
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_FORMAT2 ) );
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP1 ) );
							valDatei = originalSipFile;
							LOGGER
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
									getTextResourceService().getText( locale, MESSAGE_SIPVALIDATION ) ) );
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALFILE,
									valDatei.getAbsolutePath() ) );
							System.out.println( "" );
							System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

							// die eigentliche Fehlermeldung
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Aa_SIP )
									+ getTextResourceService().getText( locale, ERROR_XML_AA_CANNOTEXTRACTZIP ) );
							System.out.println(
									getTextResourceService().getText( locale, ERROR_XML_AA_CANNOTEXTRACTZIP ) );

							// Fehler im Validierten SIP --> invalide & Abbruch
							LOGGER.logError(
									getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
							LOGGER.logError(
									getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
							System.out.println( "Invalid" );
							System.out.println( "" );
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP2 ) );
							LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );

							// logFile bereinigung (& End und ggf 3c)
							Util.valEnd3cAmp( "", logFile );

							// bestehendes Workverzeichnis ggf. loeschen
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
							if ( tmpDir.exists() ) {
								tmpDir.deleteOnExit();
							}
							System.exit( 1 );
						}
					}
					valDatei = tmpDirZip;

					File toplevelfolder = new File(
							valDatei.getAbsolutePath() + File.separator + valDatei.getName() );
					if ( toplevelfolder.exists() ) {
						valDatei = toplevelfolder;
					}
					unSipFile = valDatei;
					valDateiName = valDatei.getName();
					valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
				}
			} else {
				// SIP ist ein Ordner valDatei bleibt unveraendert
			}

			// Vorgaengige Formatvalidierung (Schritt 3c)
			Map<String, File> fileUnsortedMap = Util.getFileMapFile( valDatei );
			Map<String, File> fileMap = new TreeMap<String, File>( fileUnsortedMap );
			int numberInFileMap = fileMap.size();
			Set<String> fileMapKeys = fileMap.keySet();

			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				// configmap neu auslesen im bereich pdf, da veraenderungen moeglich sind
				pdfaValidationPdftools = configMap.get( "pdftools" );
				pdfaValidationCallas = configMap.get( "callas" );
				pdfaValidation = "no";
				if ( pdfaValidationPdftools.equalsIgnoreCase( "yes" )
						|| pdfaValidationCallas.equalsIgnoreCase( "yes" ) ) {
					pdfaValidation = "yes";
				}
				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );

				if ( !newFile.isDirectory()
						&& newFile.getAbsolutePath().contains( File.separator + "content" + File.separator ) ) {
					valDatei = newFile;
					valDateiName = valDatei.getName();
					valDateiExt = "." + FilenameUtils.getExtension( valDateiName ).toLowerCase();
					count = count + 1;
					countProgress = countProgress + 1;

					/* String extension = valDatei.getName(); int lastIndexOf = extension.lastIndexOf( "." );
					 * if ( lastIndexOf == -1 ) { // empty extension extension = "other"; } else { extension =
					 * extension.substring( lastIndexOf ); } */

					if ( valDateiExt.equals( ".pdf" ) || valDateiExt.equals( ".pdfa" ) ) {
						if ( pdfaValidation.equals( "yes" ) ) {
							// Validierung durchfuehren
							int countToValidated = numberInFileMap - countProgress;
							System.out.print( countToValidated + " " + "PDFA:  " + valDatei.getAbsolutePath() );
							/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
							 * dirOfJarPath, configMap, context ); */
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
									verbose, dirOfJarPath, configMap, context, locale );
							if ( valFile ) {
								pdfaCountIo = pdfaCountIo + 1;
							} else {
								pdfaCountNio = pdfaCountNio + 1;
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
					} else if ( valDateiExt.equals( ".tiff" ) || valDateiExt.equals( ".tif" ) ) {
						if ( tiffValidation.equals( "yes" ) ) {
							// Validierung durchfuehren
							int countToValidated = numberInFileMap - countProgress;
							System.out
									.print( countToValidated + " " + "TIFF:  " + valDatei.getAbsolutePath() + " " );
							/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
							 * dirOfJarPath, configMap, context ); */
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
									verbose, dirOfJarPath, configMap, context, locale );
							if ( valFile ) {
								tiffCountIo = tiffCountIo + 1;
							} else {
								tiffCountNio = tiffCountNio + 1;
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
					} else if ( valDateiExt.equals( ".siard" ) ) {
						if ( siardValidation.equals( "yes" ) ) {
							// Validierung durchfuehren
							int countToValidated = numberInFileMap - countProgress;
							System.out
									.print( countToValidated + " " + "SIARD: " + valDatei.getAbsolutePath() + " " );

							// Arbeitsverzeichnis zum Entpacken des Archivs erstellen
							String pathToWorkDirSiard = configMap.get( "PathToWorkDir" );
							File tmpDirSiard = new File( pathToWorkDirSiard + File.separator + "SIARD" );
							if ( tmpDirSiard.exists() ) {
								Util.deleteDir( tmpDirSiard );
							}
							if ( !tmpDirSiard.exists() ) {
								tmpDirSiard.mkdir();
							}
							/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
							 * dirOfJarPath, configMap, context ); */
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
									verbose, dirOfJarPath, configMap, context, locale );
							if ( valFile ) {
								siardCountIo = siardCountIo + 1;
							} else {
								siardCountNio = siardCountNio + 1;
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
					} else if ( valDateiExt.equals( ".jpe" ) || valDateiExt.equals( ".jpeg" )
							|| valDateiExt.equals( ".jpg" ) ) {
						if ( jpegValidation.equals( "yes" ) ) {
							int countToValidated = numberInFileMap - countProgress;
							System.out
									.print( countToValidated + " " + "JPEG:  " + valDatei.getAbsolutePath() + " " );
							/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
							 * dirOfJarPath, configMap, context ); */
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
									verbose, dirOfJarPath, configMap, context, locale );
							if ( valFile ) {
								jpegCountIo = jpegCountIo + 1;
							} else {
								jpegCountNio = jpegCountNio + 1;
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
					} else if ( valDateiExt.equals( ".jp2" ) ) {
						if ( jp2Validation.equals( "yes" ) ) {
							// Validierung durchfuehren
							int countToValidated = numberInFileMap - countProgress;
							System.out
									.print( countToValidated + " " + "JP2:   " + valDatei.getAbsolutePath() + " " );
							/* boolean valFile = valFile( valDatei, logFileName, directoryOfLogfile, verbose,
							 * dirOfJarPath, configMap, context ); */
							Controllervalfile controller1 = (Controllervalfile) context
									.getBean( "controllervalfile" );
							boolean valFile = controller1.valFile( valDatei, logFileName, directoryOfLogfile,
									verbose, dirOfJarPath, configMap, context, locale );
							if ( valFile ) {
								jp2CountIo = jp2CountIo + 1;
							} else {
								jp2CountNio = jp2CountNio + 1;
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

			countSummaryNio = pdfaCountNio + siardCountNio + tiffCountNio + jp2CountNio + jpegCountNio;
			countSummaryIo = pdfaCountIo + siardCountIo + tiffCountIo + jp2CountIo + jpegCountIo;
			float countSummaryIoP = 100 / (float) count * (float) countSummaryIo;
			float countSummaryNioP = 100 / (float) count * (float) countSummaryNio;
			float countNioP = 100 / (float) count * (float) countNio;
			String summary3c = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARY_3C, count,
					countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP, countNioP );

			String summary = "";
			if ( countNio > 0 ) {
				// mit Detail weil countNio > 0
				summary = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARYDETAIL, count,
						countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP, countNioP,
						countNioDetail, countNioExtension );
			} else {
				// ohne Detail weil countNio == 0
				summary = getTextResourceService().getText( locale, MESSAGE_XML_SUMMARY, count,
						countSummaryIo, countSummaryNio, countNio, countSummaryIoP, countSummaryNioP,
						countNioP );
			}

			if ( countSummaryNio == 0 ) {
				// alle Validierten Dateien valide
				validFormat = true;
				fileName3c = "3c_Valide.txt";
			} else {
				// Fehler in Validierten Dateien --> invalide
				validFormat = false;
				fileName3c = "3c_Invalide.txt";
			}
			// outputFile3c = new File( directoryOfLogfile + fileName3c );
			outputFile3c = new File( pathToWorkDir + File.separator + fileName3c );
			try {
				outputFile3c.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}

			if ( countNio == count ) {
				// keine Dateien Validiert
				LOGGER.logError(
						getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDINGS, formatValOn ) );
				System.out.println(
						getTextResourceService().getText( locale, ERROR_INCORRECTFILEENDINGS, formatValOn ) );
			}

			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_FORMAT2 ) );

			// Start Normale SIP-Validierung mit auswertung Format-Val. im 3c

			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP1 ) );
			valDatei = unSipFile;
			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS ) );
			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALTYPE,
					getTextResourceService().getText( locale, MESSAGE_SIPVALIDATION ) ) );
			LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALFILE,
					originalSipFile.getAbsolutePath() ) );
			System.out.println( "" );
			System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

			Controllersip controller = (Controllersip) context.getBean( "controllersip" );
			boolean okMandatory = false;
			okMandatory = controller.executeMandatory( valDatei, directoryOfLogfile, configMap, locale );
			boolean ok = false;

			/* die Validierungen 1a - 1d sind obligatorisch, wenn sie bestanden wurden, koennen die
			 * restlichen Validierungen, welche nicht zum Abbruch der Applikation fuehren, ausgefuehrt
			 * werden.
			 * 
			 * 1a wurde bereits getestet (vor der Formatvalidierung entsprechend faengt der Controller mit
			 * 1b an */
			if ( okMandatory ) {
				ok = controller.executeOptional( valDatei, directoryOfLogfile, configMap, locale );
			}
			// Formatvalidierung validFormat
			ok = (ok && okMandatory && validFormat);

			if ( ok ) {
				// Validiertes SIP valide
				LOGGER
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_VALID ) );
				LOGGER
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP2 ) );
				LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );
				System.out.println( "Valid" );
				System.out.println( "" );
			} else {
				// Fehler im Validierten SIP --> invalide
				LOGGER.logError(
						getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_INVALID ) );
				LOGGER
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_VALERGEBNIS_CLOSE ) );
				LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_SIP2 ) );
				LOGGER.logError( getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );
				System.out.println( "Invalid" );
				System.out.println( "" );

			}

			// Garbage Collecter aufruf zur Bereinigung
			System.gc();

			// Bereinigungen und ergaenzungen durchfuehren

			// Ergaenzung Format Summary
			newFormat = "<Format>" + summary;
			Util.oldnewstring( "<Format>", newFormat, logFile );

			// ggf. Fehlermeldung 3c ergaenzen Util.val3c(summary3c, logFile );
			// logFile bereinigung (& End und ggf 3c)
			Util.valEnd3cAmp( summary3c, logFile );

			// Ergaenzen welche SIP-Validierung durchgefuehrt wurde
			String sipVersion = " ";
			if ( ECH160_1_1.exists() ) {
				sipVersion = " (eCH-0160v1.1)";
				Util.deleteFile( ECH160_1_1 );
			} else if ( ECH160_1_0.exists() ) {
				sipVersion = " (eCH-0160v1.0)";
				Util.deleteFile( ECH160_1_0 );
			}
			Util.valSipversion( sipVersion, logFile );

			System.out.print( getTextResourceService().getText( locale, MESSAGE_XML_LOG ) );
			System.out.print( "\r" );

			// bestehendes Workverzeichnis ggf. loeschen
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}

			File pathTemp = new File( directoryOfLogfile, "path.tmp" );
			if ( pathTemp.exists() ) {
				pathTemp.delete();
			}
			if ( pathTemp.exists() ) {
				pathTemp.deleteOnExit();
			}

			File callasNo = new File( directoryOfLogfile + File.separator + "_callas_NO.txt" );
			if ( callasNo.exists() ) {
				callasNo.delete();
			}

			System.out.print( "                                                                    " );
			System.out.print( "\r" );

			System.out.println( getTextResourceService().getText( locale, MESSAGE_SIPVALIDATION_DONE,
					logFile.getAbsolutePath() ) );

			if ( ok ) {
				// bestehendes Workverzeichnis ggf. loeschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( tmpDir.exists() ) {
					tmpDir.deleteOnExit();
				}
				valSip = true;
				return valSip;
			} else {
				// bestehendes Workverzeichnis ggf. loeschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( tmpDir.exists() ) {
					tmpDir.deleteOnExit();
				}
				valSip = false;
				return valSip;
			}
		} catch ( Exception e ) {
			LOGGER.logError( "<Error>"
					+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
							"SIP-ValidationException: " + e.getMessage() )
					+ getTextResourceService().getText( locale, MESSAGE_XML_SIP2 )
					+ getTextResourceService().getText( locale, MESSAGE_XML_LOGEND ) );
			System.out.println( "Exception: " + e.getMessage() );
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}
			valSip = false;
			return valSip;
		} catch ( StackOverflowError eso ) {
			LOGGER.logError( getTextResourceService().getText( locale, ERROR_XML_STACKOVERFLOWMAIN ) );
			System.out.println( "Exception: " + "StackOverflowError" );
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}
			valSip = false;
			return valSip;
		} catch ( OutOfMemoryError eoom ) {
			LOGGER.logError( getTextResourceService().getText( locale, ERROR_XML_OUTOFMEMORYMAIN ) );
			System.out.println( "Exception: " + "OutOfMemoryError" );
			if ( tmpDir.exists() ) {
				tmpDir.deleteOnExit();
			}
			valSip = false;
			return valSip;
		}
	}
}