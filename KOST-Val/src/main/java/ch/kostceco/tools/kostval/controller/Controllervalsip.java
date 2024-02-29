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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

import ch.kostceco.tools.kosttools.fileservice.Magic;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kosttools.util.Zip64Archiver;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.logging.MessageConstants;
import ch.kostceco.tools.kostval.service.TextResourceService;

/**
 * kostval --> Controllervalsip
 * 
 * Der Controller ruft die benoetigten Module zur Validierung auf.
 * 
 * Die Validierungs-Module werden mittels Spring-Dependency-Injection
 * eingebunden.
 */

public class Controllervalsip implements MessageConstants
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

	public boolean valSip( File valDatei, String logFileName,
			File directoryOfLogfile, boolean verbose, String dirOfJarPath,
			Map<String, String> configMap, ApplicationContext context,
			Locale locale, Boolean onlySip, File logFile ) throws IOException
	{
		// SIP-Validierung

		boolean valSip = false;
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File tmpDir = new File( pathToWorkDir );
		try {
			if ( !tmpDir.exists() ) {
				tmpDir.createNewFile();
			}
		} catch ( IOException e ) {
			e.printStackTrace();
			System.out.println( tmpDir.getAbsolutePath() );
		}

		// ggf alte SIP-Validierung-Versions-Notiz loeschen
		File ECH160_1_2 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.2.txt" );
		File ECH160_1_1 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.1.txt" );
		File ECH160_1_0 = new File( directoryOfLogfile.getAbsolutePath()
				+ File.separator + "ECH160_1.0.txt" );
		if ( ECH160_1_2.exists() ) {
			Util.deleteFile( ECH160_1_2 );
		} else if ( ECH160_1_1.exists() ) {
			Util.deleteFile( ECH160_1_1 );
		} else if ( ECH160_1_0.exists() ) {
			Util.deleteFile( ECH160_1_0 );
		}

		Logtxt.logtxt( logFile, "<Format>" );

		// TODO Sip fuer Validierung vorbereiten
		try {
			boolean validFormat = false;
			File originalSipFile = valDatei;
			File unSipFile = valDatei;
			File outputFile3c = null;
			String fileName3c = "3c_Invalide.txt";
			File tmpDirZip = null;
			String valDateiName = valDatei.getName();
			String valDateiExt = "."
					+ FilenameUtils.getExtension( valDateiName ).toLowerCase();

			// zuerst eine Formatvalidierung ueber den Content dies ist analog
			// aufgebaut wie --format
			String newFormat = "";
			Integer countValid = 0;
			Integer countInvalid = 0;
			Integer countNotaz = 0;
			Integer count = 0;
			Integer countProgress = 0;

			if ( !valDatei.isDirectory() ) {
				Boolean zip = false;
				// Eine ZIP Datei muss mit PK.. beginnen
				if ( (valDateiExt.equals( ".zip" ) || valDatei.getAbsolutePath()
						.toLowerCase().endsWith( ".zip64" )) ) {
					// System.out.println("ueberpruefe Magic number zip...");
					if ( Magic.magicZip( valDatei ) ) {
						// System.out.println(" -> es ist eine Zip-Datei");
						zip = true;
					} else {
						// System.out.println(" -> es ist KEINE Zip-Datei");
						zip = false;
					}
				}

				// wenn die Datei kein Directory ist, muss sie mit zip oder
				// zip64 enden
				if ( (!(valDateiExt.equals( ".zip" ) || valDatei
						.getAbsolutePath().toLowerCase().endsWith( ".zip64" )))
						|| zip == false ) {
					// Abbruch! D.h. Sip message beginnen, Meldung und Beenden
					// ab hier bis System.exit( 1 );
					Logtxt.logtxt( logFile, "</Format><Sip><Validation>" );
					valDatei = originalSipFile;
					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_VALTYPE, "SIP" ) );
					Logtxt.logtxt( logFile, "<ValFile>"
							+ valDatei.getAbsolutePath() + "</ValFile>" );
					System.out.println( "" );
					System.out
							.println( "SIP:   " + valDatei.getAbsolutePath() );

					// die eigentliche Fehlermeldung
					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_Aa_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_AA_INCORRECTFILEENDING ) );
					System.out.println( getTextResourceService().getText(
							locale, ERROR_XML_AA_INCORRECTFILEENDING ) );

					// Fehler im Validierten SIP --> invalide & Abbruch
					Logtxt.logtxt( logFile,
							"<Invalid>invalid</Invalid></Validation>" );
					System.out.println( "Invalid" );
					System.out.println( "" );
					Logtxt.logtxt( logFile, "</Sip></KOSTValLog>" );

					// logFile bereinigung (& End und ggf 3c)
					Util.valEnd3cAmp( "", logFile );

					// bestehendes Workverzeichnis ggf. loeschen
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					// System.exit( 2 );
					return false;

				} else {
					// geziptes SIP --> in temp dir entzipen
					String toplevelDir = valDatei.getName();
					int lastDotIdx = toplevelDir.lastIndexOf( "." );
					toplevelDir = toplevelDir.substring( 0, lastDotIdx );
					tmpDirZip = new File(
							tmpDir.getAbsolutePath() + File.separator + "ZIP"
									+ File.separator + toplevelDir );
					try {
						FileInputStream fis = null;
						ZipInputStream zipfile = null;
						ZipEntry zEntry = null;
						fis = new FileInputStream( valDatei );
						zipfile = new ZipInputStream(
								new BufferedInputStream( fis ) );
						while ( (zEntry = zipfile.getNextEntry()) != null ) {
							try {
								if ( !zEntry.isDirectory() ) {
									byte[] tmp = new byte[4 * 1024];
									FileOutputStream fos = null;
									String opFilePath = tmpDirZip
											+ File.separator + zEntry.getName();
									File newFile = new File( opFilePath );
									File parent = newFile.getParentFile();
									if ( !parent.exists() ) {
										parent.mkdirs();
									}
									// System.out.println( "Extracting file to "
									// + newFile.getAbsolutePath() );
									fos = new FileOutputStream( opFilePath );
									int size = 0;
									while ( (size = zipfile
											.read( tmp )) != -1 ) {
										fos.write( tmp, 0, size );
									}
									fos.flush();
									fos.close();
								} else {
									/*
									 * Scheibe den Ordner wenn noch nicht
									 * vorhanden an den richtigen Ort respektive
									 * in den richtigen Ordner der ggf angelegt
									 * werden muss. Dies muss gemacht werden,
									 * damit auch leere Ordner ins Work
									 * geschrieben werden. Diese werden danach
									 * im J als Fehler angegeben
									 */
									File newFolder = new File( tmpDirZip,
											zEntry.getName() );
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
							// Abbruch! D.h. Sip message beginnen, Meldung und
							// Beenden ab hier bis System.exit
							Logtxt.logtxt( logFile,
									"</Format><Sip><Validation>" );
							valDatei = originalSipFile;
							Logtxt.logtxt( logFile,
									getTextResourceService().getText( locale,
											MESSAGE_XML_VALTYPE, "SIP" ) );
							Logtxt.logtxt( logFile,
									"<ValFile>" + valDatei.getAbsolutePath()
											+ "</ValFile>" );
							System.out.println( "" );
							System.out.println(
									"SIP:   " + valDatei.getAbsolutePath() );

							// die eigentliche Fehlermeldung
							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_Aa_SIP )
									+ getTextResourceService().getText( locale,
											ERROR_XML_AA_CANNOTEXTRACTZIP ) );
							System.out.println(
									getTextResourceService().getText( locale,
											ERROR_XML_AA_CANNOTEXTRACTZIP ) );

							// Fehler im Validierten SIP --> invalide & Abbruch
							Logtxt.logtxt( logFile,
									"<Invalid>invalid</Invalid></Validation>" );
							System.out.println( "Invalid" );
							System.out.println( "" );
							Logtxt.logtxt( logFile, "</Sip></KOSTValLog>" );

							// logFile bereinigung (& End und ggf 3c)
							Util.valEnd3cAmp( "", logFile );

							// bestehendes Workverzeichnis ggf. loeschen
							if ( tmpDir.exists() ) {
								Util.deleteDir( tmpDir );
							}
							// System.exit( 2 );
							return false;
						}
					}
					valDatei = tmpDirZip;

					File toplevelfolder = new File( valDatei.getAbsolutePath()
							+ File.separator + valDatei.getName() );
					if ( toplevelfolder.exists() ) {
						valDatei = toplevelfolder;
					}
					unSipFile = valDatei;
					valDateiName = valDatei.getName();
					valDateiExt = "." + FilenameUtils
							.getExtension( valDateiName ).toLowerCase();
				}
			} else {
				// SIP ist ein Ordner valDatei bleibt unveraendert
			}

			// TODO Vorgaengige Formatvalidierung (Schritt 3c)
			File sipSipContent = new File(
					valDatei.getAbsolutePath() + File.separator
							+ valDatei.getName() + File.separator + "content" );
			File sipContent = new File(
					valDatei.getAbsolutePath() + File.separator + "content" );
			File valDateiContent = valDatei;
			if ( sipSipContent.exists() ) {
				valDateiContent = sipSipContent;
			} else if ( sipContent.exists() ) {
				valDateiContent = sipContent;
			} else {
				valDateiContent = valDatei;
			}
			Map<String, File> fileUnsortedMap = Util
					.getFileMapFile( valDateiContent );
			Map<String, File> fileMap = new TreeMap<String, File>(
					fileUnsortedMap );
			int numberInFileMap = fileMap.size();
			Set<String> fileMapKeys = fileMap.keySet();

			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
					.hasNext(); ) {
				// configmap neu auslesen im bereich pdf, da veraenderungen
				// moeglich sind
				// pdfaValidation = configMap.get( "pdfavalidation" );
				String entryName = iterator.next();
				File newFile = fileMap.get( entryName );

				if ( !newFile.isDirectory()
						&& newFile.getAbsolutePath().contains( File.separator
								+ "content" + File.separator ) ) {
					valDatei = newFile;
					valDateiName = valDatei.getName();
					valDateiExt = "." + FilenameUtils
							.getExtension( valDateiName ).toLowerCase();
					count = count + 1;
					countProgress = countProgress + 1;

					/*
					 * String extension = valDatei.getName(); int lastIndexOf =
					 * extension.lastIndexOf( "." ); if ( lastIndexOf == -1 ) {
					 * // empty extension extension = "other"; } else {
					 * extension = extension.substring( lastIndexOf ); }
					 */

					if ( onlySip ) {
						// keine Formatvalidierung
					} else {
						int countToValidated = numberInFileMap - countProgress;
						// Kontrolle ob Datei akzeptiert ist und ob sie
						// validiert werden soll
						Controllervalfofile controller1 = (Controllervalfofile) context
								.getBean( "controllervalfofile" );
						String valFile = controller1.valFoFile( valDatei,
								logFileName, directoryOfLogfile, verbose,
								dirOfJarPath, configMap, context, locale,
								logFile, countToValidated );
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
					}
				} else {
					// Ordner. Count aktualisieren
					countProgress = countProgress + 1;
				}
			}

			float countValidP = 100 / (float) count * (float) countValid;
			float countInvalidP = 100 / (float) count * (float) countInvalid;
			float countNotazP = 100 / (float) count * (float) countNotaz;

			String summary3c = getTextResourceService().getText( locale,
					MESSAGE_XML_SUMMARY_3C, count, countValid, countInvalid,
					countNotaz, countValidP, countInvalidP, countNotazP );

			String summary = "";
			if ( onlySip ) {
				summary = getTextResourceService().getText( locale,
						MESSAGE_XML_SUMMARY_NO3C );
			} else {
				summary = getTextResourceService().getText( locale,
						MESSAGE_XML_SUMMARY, count.toString(),
						countValid.toString(), countInvalid.toString(),
						countNotaz.toString(), countValidP, countInvalidP,
						countNotazP );
			}

			if ( count.equals( countValid ) || onlySip ) {
				// alle Validierten Dateien valide
				validFormat = true;
				fileName3c = "3c_Valide.txt";
			} else {
				// Fehler in Validierten Dateien --> invalide
				// nicht akzeptierte Dateien
				validFormat = false;
				fileName3c = "3c_Invalide.txt";
			}
			// outputFile3c = new File( directoryOfLogfile + fileName3c );
			outputFile3c = new File(
					pathToWorkDir + File.separator + fileName3c );
			try {
				if ( !outputFile3c.exists() ) {
					if ( !tmpDir.exists() ) {
						tmpDir.createNewFile();
					}
					outputFile3c.createNewFile();
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}

			Logtxt.logtxt( logFile, "</Format>" );

			// TODO Start Normale SIP-Validierung mit auswertung Format-Val. im
			// 3c
			Logtxt.logtxt( logFile, "<Sip><Validation>" );
			valDatei = unSipFile;
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					MESSAGE_XML_VALTYPE, "SIP" ) );
			Logtxt.logtxt( logFile, "<ValFile>"
					+ originalSipFile.getAbsolutePath() + "</ValFile>" );
			System.out.println( "" );
			System.out.println( "SIP:   " + valDatei.getAbsolutePath() );

			Controllersip controller = (Controllersip) context
					.getBean( "controllersip" );
			boolean okMandatory = false;
			okMandatory = controller.executeMandatory( valDatei,
					directoryOfLogfile, configMap, locale, logFile,
					dirOfJarPath );
			boolean ok = false;

			/*
			 * die Validierungen 1a - 1d sind obligatorisch, wenn sie bestanden
			 * wurden, koennen die restlichen Validierungen, welche nicht zum
			 * Abbruch der Applikation fuehren, ausgefuehrt werden.
			 * 
			 * 1a wurde bereits getestet (vor der Formatvalidierung entsprechend
			 * faengt der Controller mit 1b an
			 */
			if ( okMandatory ) {
				ok = controller.executeOptional( valDatei, directoryOfLogfile,
						configMap, locale, logFile, dirOfJarPath );
			}
			// Formatvalidierung validFormat
			ok = (ok && okMandatory && validFormat);

			if ( ok ) {
				// Validiertes SIP valide
				Logtxt.logtxt( logFile,
						"<Valid>valid</Valid></Validation></Sip></KOSTValLog>" );
				System.out.println( "Valid" );
				System.out.println( "" );
			} else {
				// Fehler im Validierten SIP --> invalide
				Logtxt.logtxt( logFile,
						"<Invalid>invalid</Invalid></Validation></Sip></KOSTValLog>" );
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
			if ( ECH160_1_2.exists() ) {
				sipVersion = " (eCH-0160v1.2)";
				Util.deleteFile( ECH160_1_2 );
			} else if ( ECH160_1_1.exists() ) {
				sipVersion = " (eCH-0160v1.1)";
				Util.deleteFile( ECH160_1_1 );
			} else if ( ECH160_1_0.exists() ) {
				sipVersion = " (eCH-0160v1.0)";
				Util.deleteFile( ECH160_1_0 );
			}
			Util.valSipversion( sipVersion, logFile );

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

			File callasNo = new File(
					directoryOfLogfile + File.separator + "_callas_NO.txt" );
			if ( callasNo.exists() ) {
				callasNo.delete();
			}

			System.out.println( getTextResourceService().getText( locale,
					MESSAGE_SIPVALIDATION_DONE, logFile.getAbsolutePath() ) );

			if ( ok ) {
				// bestehendes Workverzeichnis ggf. loeschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				valSip = true;
				return valSip;
			} else {
				// bestehendes Workverzeichnis ggf. loeschen
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				valSip = false;
				return valSip;
			}
		} catch ( Exception e ) {
			Logtxt.logtxt( logFile,
					"<Error>"
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN,
									"SIP-ValidationException: "
											+ e.getMessage() )
							+ "</Sip></KOSTValLog>" );
			System.out.println( "Exception: " + e.getMessage() );
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			valSip = false;
			return valSip;
		} catch ( StackOverflowError eso ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_STACKOVERFLOWMAIN ) );
			System.out.println( "Exception: " + "StackOverflowError" );
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			valSip = false;
			return valSip;
		} catch ( OutOfMemoryError eoom ) {
			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					ERROR_XML_OUTOFMEMORYMAIN ) );
			System.out.println( "Exception: " + "OutOfMemoryError" );
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			valSip = false;
			return valSip;
		}
	}
}