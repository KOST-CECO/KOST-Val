/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ch.kostceco.tools.kosttools.fileservice.Jhove;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationBjhoveValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationBjhoveValidationModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt B (Jhove-Validierung) Ist die TIFF-Datei gemaess Jhove
 * valid? valid --> Status: "Well-Formed and valid"
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationBjhoveValidationModuleImpl extends ValidationModuleImpl
		implements ValidationBjhoveValidationModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	private boolean			min		= false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationBjhoveValidationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		boolean isValid = true;
		// Informationen zum Jhove-Logverzeichnis holen
		String pathToJhoveOutput2 = directoryOfLogfile.getAbsolutePath();
		/*
		 * Jhove schreibt ins Work-Verzeichnis, damit danach eine Kopie ins
		 * Log-Verzeichnis abgelegt werden kann, welche auch geloescht werden
		 * kann.
		 */
		File jhoveLog = new File( pathToJhoveOutput2,
				valDatei.getName() + ".jhove-log.txt" );
		String pathToWorkDir = configMap.get( "PathToWorkDir" );

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		// Vorbereitungen: valDatei an die JHove Applikation übergeben
		String jhoveString = Jhove.valTiff( valDatei, jhoveLog, pathToWorkDir );
		if ( jhoveString.equals( "OK" ) ) {
			if ( jhoveLog.exists() ) {
				isValid = true;
			} else {
				isValid = false;
				if ( min ) {
					return false;
				} else {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_B_TIFF )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, "No Jhove report." ) );
					return false;
				}
			}
		} else {
			isValid = false;
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_B_TIFF )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, jhoveString ) );
				return false;
			}
		}

		// Report auswerten
		try {
			BufferedReader in = new BufferedReader(
					new FileReader( jhoveLog ) );
			String line;
			Set<String> lines = new LinkedHashSet<String>( 100000 ); // evtl
																		// vergroessern
			int counter = 0;
			String status = "";
			int statuscounter = 0;
			int ignorcounter = 0;
			while ( (line = in.readLine()) != null ) {
				/*
				 * die Status-Zeile enthaelt diese Moeglichkeiten: Valider
				 * Status: "Well-Formed and valid" Invalider Status:
				 * "Not well-formed" oder "Well-Formed, but not valid"
				 * moeglicherweise existieren weitere Ausgabemoeglichkeiten
				 */
				if ( line.contains( "Status:" ) ) {
					if ( !line.contains( "Well-Formed and valid" ) ) {
						status = line;
						/*
						 * Status nur als Fehlermeldung ausgeben, wenn nicht
						 * alle ErrorMessages ignoriert werden konnten
						 */
					}
				}

				// Ignore macht nur Sinn wenn Jhove immer zu ende validiert,
				// entsprechend noch nicht umgesetz
				// String detailIgnore = configMap.get( "ignore" );

				if ( line.contains( "ErrorMessage:" ) ) {
					if ( line.contains( " out of sequence" ) ) {
						ignorcounter = ignorcounter + 1;
						// } else if (detailIgnore.contains(line)){
						// ignorcounter = ignorcounter + 1;
					} else {
						if ( statuscounter == 0 ) {
							// Invalider Status & Status noch nicht ausgegeben
							if ( min ) {
								in.close();
								return false;
							} else {
								isValid = false;

								Logtxt.logtxt( logFile, getTextResourceService()
										.getText( locale,
												MESSAGE_XML_MODUL_B_TIFF )
										+ getTextResourceService().getText(
												locale,
												MESSAGE_XML_SERVICEINVALID,
												"Jhove", status ) );
							}
							statuscounter = 1; // Marker Status Ausgegeben
						}
						/*
						 * Linie mit der Fehlermeldung auch mitausgeben, falls
						 * diese neu ist.
						 * 
						 * Korrupte TIFF-Dateien enthalten mehrere Zehntausen
						 * Mal den gleichen Eintrag
						 * "  ErrorMessage: Unknown data type: Type = 0, Tag = 0"
						 * In einem Test wurde so die Anzahl Errors von 65'060
						 * auf 63 reduziert
						 */

						if ( lines.contains( line ) ) {
							// Diese Linie = Fehlermelung wurde bereits
							// ausgegeben
						} else {
							if ( min ) {
								in.close();
								return false;
							} else {
								// neue Fehlermeldung
								counter = counter + 1;
								// max 10 Meldungen im Modul B
								if ( counter < 11 ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_B_TIFF )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_SERVICEMESSAGE,
																	"- Jhove",
																	line ) );
									lines.add( line );
								} else if ( counter == 11 ) {
									Logtxt.logtxt( logFile,
											getTextResourceService().getText(
													locale,
													MESSAGE_XML_MODUL_B_TIFF )
													+ getTextResourceService()
															.getText( locale,
																	MESSAGE_XML_SERVICEMESSAGE,
																	"- Jhove",
																	" ErrorMessage: . . ." ) );
									lines.add( line );
								} else {
									// Modul B Abbrechen. Spart viel Zeit.
									in.close();
									return false;
								}
							}
						}
					}
				}
			}
			if ( (statuscounter == 0) && (ignorcounter == 0)
					&& !status.equals( "" ) ) {
				// Status noch nicht ausgegeben & keine Errors ignoriert &
				// Invalider Status
				isValid = false;

				Logtxt.logtxt( logFile, getTextResourceService()
						.getText( locale, MESSAGE_XML_MODUL_B_TIFF )
						+ getTextResourceService().getText( locale,
								MESSAGE_XML_SERVICEINVALID, "Jhove", status ) );
				statuscounter = 1; // Marker Status Ausgegeben
			}
			in.close();
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_B_TIFF )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		// jhoveReport / temp wird in Controllervalfile geloescht */
		return isValid;
	}
}