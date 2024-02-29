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

package ch.kostceco.tools.kostval.validation.modulemkv.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import ch.kostceco.tools.kosttools.fileservice.Mkvalidator;
import ch.kostceco.tools.kostval.exception.modulemkv.ValidationBHmkvvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulemkv.ValidationBHvalidationMkvModule;

/**
 * a) MKV-Erkennung mit ffprobe inkl Codecs
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationBHvalidationMkvModuleImpl extends ValidationModuleImpl
		implements ValidationBHvalidationMkvModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationBHmkvvalidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}
		String pathToWorkDir = configMap.get( "PathToWorkDir" );
		File workDir = new File( pathToWorkDir );
		if ( !workDir.exists() ) {
			workDir.mkdir();
		}
		File outputMkvalidator = new File(
				pathToWorkDir + File.separator + "mkvalidator.txt" );
		// falls das File von einem vorhergehenden Durchlauf bereits
		// existiert, loeschen wir es
		if ( outputMkvalidator.exists() ) {
			outputMkvalidator.delete();
		}

		// Die Erkennung erfolgt bereits im Vorfeld (Modul A)

		boolean isValid = true;

		// TODO: Start: Validierung mit mkvalidator

		// - Initialisierung mkvalidator -> existiert alles zu mkvalidator?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Mkvalidator.checkMkvalidator( dirOfJarPath );
		if ( !checkTool.equals( "OK" ) ) {
			if ( min ) {
				return false;
			} else {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_B_MKV )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService()
												.getText( locale, ABORTED ) ) );
				return false;
			}
		} else {
			// mkvalidator sollte vorhanden sein
			try {
				String resultExec = Mkvalidator.execMkvalidator( valDatei,
						outputMkvalidator, workDir, dirOfJarPath );
				if ( !resultExec.equals( "OK" )
						|| !outputMkvalidator.exists() ) {
					// Exception oder Report existiert nicht
					if ( min ) {
						return false;
					} else {
						isValid = false;
						// Erster Fehler! Meldung B ausgeben und invalid setzten
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_A_MKV )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_SERVICEINVALID,
										"mkvalidator", "" ) );
					}
				} else {
					// Report existiert -> Auswerten...
					String appearsValid = "the file appears to be valid";
					String error = "ERR";
					String track = "Track #";
					String errB1 = "";
					String errB2 = "";
					String errB3 = "";
					String errB4 = "";
					String errB5 = "";
					String errC1 = "";
					String errC2 = "";
					String errC3 = "";
					String errC4 = "";
					String errC5 = "";
					String errD1 = "";
					String errD2 = "";
					String errD3 = "";
					String errD4 = "";
					String errD5 = "";
					String errE1 = "";
					String errE2 = "";
					String errE3 = "";
					String errE4 = "";
					String errE5 = "";
					String errF1 = "";
					String errF2 = "";
					String errF3 = "";
					String errF4 = "";
					String errF5 = "";
					String errG1 = "";
					String errG2 = "";
					String errG3 = "";
					String errG4 = "";
					String errG5 = "";
					String errH1 = "";
					String errH2 = "";
					String errH3 = "";
					String errH4 = "";
					String errH5 = "";
					Scanner scannerOutput = new Scanner( outputMkvalidator );
					while ( scannerOutput.hasNextLine() ) {
						// format_name=matroska,webm
						String line = scannerOutput.nextLine();
						if ( line.contains( appearsValid ) ) {
							// Validierung mit mkvalidator bestanden
							scannerOutput.close();
							return true;
						} else {
							if ( line.startsWith( track ) ) {
								// OK, nur Informationen
							} else if ( line.startsWith( error ) ) {
								// NOK
								isValid = false;
								String lineCase = line.toLowerCase();

								// TODO Error auslesen, einordnen und ausgeben

								// ERR003: EBML head not found! Are you sure
								// it's a matroska/webm file?
								// ERR0E2: Video track #1 at 315 has an implied
								// non pixel height
								// ERR0E7: Video track #1 at 315 has a null
								// display height

								/*
								 * B EBML • Usage: mkvalidator • EBML
								 * 
								 * C Struktur • Segment • Failed to • Extra tags
								 * • Element • Invalid • Block at
								 * 
								 * D Seek • Seek
								 * 
								 * E Cluster • Cluster
								 * 
								 * F Cue • Cue
								 * 
								 * H Audio Track • Audio
								 * 
								 * G Video Track: • Video • Track
								 */
								if ( lineCase.contains( "usage: mkvalidator" )
										|| lineCase.contains( "ebml" ) ) {
									// B EBML • Usage: mkvalidator • EBML
									if ( errB1 == "" ) {
										errB1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB2 == "" ) {
										errB2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB3 == "" ) {
										errB3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB4 == "" ) {
										errB4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errB5 == "" ) {
										errB5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errB5 = errB5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "segment" )
										|| lineCase.contains( "failed to" )
										|| lineCase.contains( "extra tags" )
										|| lineCase.contains( "element" )
										|| lineCase.contains( "invalid" )
										|| lineCase.contains( "block at" ) ) {
									// C Struktur • Segment • Failed to • Extra
									// tags • Element • Invalid • Block at
									if ( errC1 == "" ) {
										errC1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC2 == "" ) {
										errC2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC3 == "" ) {
										errC3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC4 == "" ) {
										errC4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC5 == "" ) {
										errC5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errC5 = errC5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "seek" ) ) {
									// D Seek • Seek
									if ( errD1 == "" ) {
										errD1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errD2 == "" ) {
										errD2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errD3 == "" ) {
										errD3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errD4 == "" ) {
										errD4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errD5 == "" ) {
										errD5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errD5 = errD5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "cluster" ) ) {
									// E Cluster • Cluster
									if ( errE1 == "" ) {
										errE1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errE2 == "" ) {
										errE2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errE3 == "" ) {
										errE3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errE4 == "" ) {
										errE4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errE5 == "" ) {
										errE5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errE5 = errE5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "cue" ) ) {
									// F Cue • Cue
									if ( errF1 == "" ) {
										errF1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errF2 == "" ) {
										errF2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errF3 == "" ) {
										errF3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errF4 == "" ) {
										errF4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errF5 == "" ) {
										errF5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errF5 = errF5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "audio" ) ) {
									// H Audio Track • Audio
									if ( errH1 == "" ) {
										errH1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errH2 == "" ) {
										errH2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errH3 == "" ) {
										errH3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errH4 == "" ) {
										errH4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errH5 == "" ) {
										errH5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errH5 = errH5.replace( "</Message>",
												" ... </Message>" );
									}
								} else if ( lineCase.contains( "video" )
										|| lineCase.contains( "track" ) ) {
									// G Video Track: • Video • Track
									if ( errG1 == "" ) {
										errG1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errG2 == "" ) {
										errG2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errG3 == "" ) {
										errG3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errG4 == "" ) {
										errG4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errG5 == "" ) {
										errG5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errG5 = errG5.replace( "</Message>",
												" ... </Message>" );
									}
								} else {
									// C Struktur
									if ( errC1 == "" ) {
										errC1 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC2 == "" ) {
										errC2 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC3 == "" ) {
										errC3 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC4 == "" ) {
										errC4 = "<Message> - " + line
												+ "</Message>";
									} else if ( errC5 == "" ) {
										errC5 = "<Message> - " + line
												+ "</Message>";
									} else {
										errC5 = errC5.replace( "</Message>",
												" ... </Message>" );
									}
								}
							}
						}
					}
					scannerOutput.close();
					// TODO Error nach Modul ausgeben

					// B
					if ( errB1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_B_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_B_MKV_ERROR, errB1 + errB2
												+ errB3 + errB4 + errB5 ) );
					}

					// C
					if ( errC1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_C_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_C_MKV_ERROR, errC1 + errC2
												+ errC3 + errC4 + errC5 ) );
					}

					// D
					if ( errD1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_D_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_D_MKV_ERROR, errD1 + errD2
												+ errD3 + errD4 + errD5 ) );
					}

					// E
					if ( errE1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_E_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_E_MKV_ERROR, errE1 + errE2
												+ errE3 + errE4 + errE5 ) );
					}

					// F
					if ( errF1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_F_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_F_MKV_ERROR, errF1 + errF2
												+ errF3 + errF4 + errF5 ) );
					}

					// G
					if ( errG1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_G_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_G_MKV_ERROR, errG1 + errG2
												+ errG3 + errG4 + errG5 ) );
					}

					// H
					if ( errH1 != "" ) {
						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_H_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_H_MKV_ERROR, errH1 + errH2
												+ errH3 + errH4 + errH5 ) );
					}
				}
			} catch ( Exception e ) {
				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_B_MKV )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Ende: Codec Auswertung
		}
		return isValid;
	}
}
