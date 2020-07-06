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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationBjhoveValidationException;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationBjhoveValidationModule;
import edu.harvard.hul.ois.jhove.*;
import edu.harvard.hul.ois.jhove.module.TiffModule;
import edu.harvard.hul.ois.jhove.Module;

/** Validierungsschritt B (Jhove-Validierung) Ist die TIFF-Datei gemäss Jhove valid? valid -->
 * Status: "Well-Formed and valid"
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationBjhoveValidationModuleImpl extends ValidationModuleImpl
		implements ValidationBjhoveValidationModule
{

	public static String NEWLINE = System.getProperty( "line.separator" );

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationBjhoveValidationException
	{

		boolean isValid = true;

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		// Vorbereitungen: valDatei an die JHove Applikation übergeben

		/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
		 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
		 * 
		 * dirOfJarPath + File.separator +
		 * 
		 * erweitern. */
		String path = new java.io.File(
				KOSTVal.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
						.getAbsolutePath();
		String locationOfJarPath = path;
		String dirOfJarPath = locationOfJarPath;
		if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		File jhoveReport = null;
		StringBuffer concatenatedOutputs = new StringBuffer();
		String pathToJhoveConfig = dirOfJarPath + File.separator + "configuration" + File.separator
				+ "jhove.conf";
		String pathToWorkDir = configMap.get( "PathToWorkDir" );

		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */

		// Informationen zum Jhove-Logverzeichnis holen
		String pathToJhoveOutput = System.getProperty( "java.io.tmpdir" );
		String pathToJhoveOutput2 = directoryOfLogfile.getAbsolutePath();
		/* Jhove schreibt ins Work-Verzeichnis, damit danach eine Kopie ins Log-Verzeichnis abgelegt
		 * werden kann, welche auch gelöscht werden kann. */
		File jhoveLog = new File( pathToJhoveOutput2, valDatei.getName() + ".jhove-log.txt" );

		File jhoveDir = new File( pathToJhoveOutput );
		if ( !jhoveDir.exists() ) {
			jhoveDir.mkdir();
		}

		// Jhove direkt ansprechen via dispatch
		try {
			String NAME = new String( "Jhove" );
			String RELEASE = new String( "1.5" );
			int[] DATE = new int[] { 2009, 12, 19 };
			String USAGE = new String( "no usage" );
			String RIGHTS = new String( "LGPL v2.1" );
			App app = new App( NAME, RELEASE, DATE, USAGE, RIGHTS );
			JhoveBase je = new JhoveBase();
			OutputHandler handler = je.getHandler( "XML" );

			// check all modules => null oder new TiffModule ();
			Module module = new TiffModule();
			module.init( "" );
			module.setDefaultParams( new ArrayList<String>() );

			String logLevel = null;
			// null = SEVERE, WARNING, INFO, FINE, FINEST
			// Ausgabe in Konsole --> kein Einfluss auf Report
			je.setLogLevel( logLevel );
			String saxClass = null;
			String configFile = pathToJhoveConfig;
			je.init( configFile, saxClass );

			je.setEncoding( "utf-8" );
			je.setTempDirectory( pathToWorkDir + "/jhove" );
			je.setBufferSize( 4096 );
			je.setChecksumFlag( false );
			je.setShowRawFlag( false );
			je.setSignatureFlag( false );
			try {
				Util.switchOffConsole();
				File newReport = new File( pathToJhoveOutput, valDatei.getName() + ".jhove-log.txt" );
				if ( newReport.exists() ) {
					Util.deleteFile( newReport );
				}
				if ( newReport.exists() ) {
					newReport.delete();
				}
				String outputFile = newReport.getAbsolutePath();
				String[] dirFileOrUri = { valDatei.getAbsolutePath() };
				je.dispatch( app, module, null, handler, outputFile, dirFileOrUri );
				/* TODO: beim Ausführen von je.dispatch gibt es in seltenen Fällen einen Fehler:
				 * 
				 * [Fatal Error] :174:20: Content is not allowed in trailing section.
				 * 
				 * Der Log wird aber korrekt erstellt und der Error kann auch nicht unterdrückt werden */
				jhoveReport = newReport;
				Util.switchOnConsole();
			} catch ( Exception e ) {
				System.out.println( "Jhove dispatch exception" );
				e.printStackTrace();
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
										"Jhove dispatch exception: " + e.getMessage() ) );
			}

			InputStream inStream = null;
			OutputStream outStream = null;

			try {
				// umkopieren, damit es gelöscht werden kann
				File afile = jhoveReport;
				File bfile = jhoveLog;
				inStream = new FileInputStream( afile );
				outStream = new FileOutputStream( bfile );
				byte[] buffer = new byte[1024];
				int length;
				// copy the file content in bytes
				while ( (length = inStream.read( buffer )) > 0 ) {
					outStream.write( buffer, 0, length );
				}
				inStream.close();
				outStream.close();
				Util.deleteFile( jhoveReport );
				Util.deleteFile( afile );

			} catch ( IOException e ) {
				e.printStackTrace();
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
										"Jhove copy report exception: " + e.getMessage() ) );
			}
			inStream.close();
			outStream.close();
			Util.deleteFile( jhoveReport );
		} catch ( Exception e ) {
			e.printStackTrace();
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
									"Jhove exception: " + e.getMessage() ) );
		}

		try {
			BufferedReader in = new BufferedReader( new FileReader( jhoveLog ) );
			String line;
			Set<String> lines = new LinkedHashSet<String>( 100000 ); // evtl vergrössern
			int counter = 0;
			String status = "";
			int statuscounter = 0;
			int ignorcounter = 0;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );

				/* die Status-Zeile enthält diese Möglichkeiten: Valider Status: "Well-Formed and valid"
				 * Invalider Status: "Not well-formed" oder "Well-Formed, but not valid" möglicherweise
				 * existieren weitere Ausgabemöglichkeiten */
				if ( line.contains( "Status:" ) ) {
					if ( !line.contains( "Well-Formed and valid" ) ) {
						status = line;
						/* Status nur als Fehlermeldung ausgeben, wenn nicht alle ErrorMessages ignoriert werden
						 * konnten */
					}
				}
				if ( line.contains( "ErrorMessage:" ) ) {
					if ( line.contains( " out of sequence" ) ) {
						ignorcounter = ignorcounter + 1;
					} else {
						if ( statuscounter == 0 ) {
							// Invalider Status & Status noch nicht ausgegeben
							isValid = false;
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
											+ getTextResourceService().getText( locale, MESSAGE_XML_B_JHOVEINVALID,
													status ) );
							statuscounter = 1; // Marker Status Ausgegeben
						}
						/* Linie mit der Fehlermeldung auch mitausgeben, falls diese neu ist.
						 * 
						 * Korrupte TIFF-Dateien enthalten mehrere Zehntausen Mal den gleichen Eintrag
						 * "  ErrorMessage: Unknown data type: Type = 0, Tag = 0" In einem Test wurde so die
						 * Anzahl Errors von 65'060 auf 63 reduziert */

						if ( lines.contains( line ) ) {
							// Diese Linie = Fehlermelung wurde bereits ausgegeben
						} else {
							// neue Fehlermeldung
							counter = counter + 1;
							// max 10 Meldungen im Modul B
							if ( counter < 11 ) {
								getMessageService()
										.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
												+ getTextResourceService().getText( locale, MESSAGE_XML_B_JHOVEMESSAGE,
														line ) );
								lines.add( line );
							} else if ( counter == 11 ) {
								getMessageService()
										.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
												+ getTextResourceService().getText( locale, MESSAGE_XML_B_JHOVEMESSAGE,
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
			if ( (statuscounter == 0) && (ignorcounter == 0) && !status.equals( "" ) ) {
				// Status noch nicht ausgegeben & keine Errors ignoriert & Invalider Status
				isValid = false;
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
								+ getTextResourceService().getText( locale, MESSAGE_XML_B_JHOVEINVALID, status ) );
				statuscounter = 1; // Marker Status Ausgegeben
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_TIFF )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// die im StringBuffer JHove-Outputs wieder in das Output-File zurückschreiben
		if ( jhoveReport != null ) {
			try {
				BufferedWriter out = new BufferedWriter( new FileWriter( jhoveReport ) );
				out.write( concatenatedOutputs.toString() );
				out.close();
			} catch ( IOException e ) {
				getMessageService().logError( getTextResourceService().getText( locale,
						MESSAGE_XML_MODUL_B_TIFF )
						+ getTextResourceService().getText( locale, MESSAGE_XML_B_CANNOTWRITEJHOVEREPORT ) );
				return false;
			}
		}
		// bestehendes Workverzeichnis löschen
		if ( jhoveReport.exists() ) {
			Util.deleteFile( jhoveReport );
		}
		if ( jhoveReport.exists() ) {
			jhoveReport.delete();
		}
		jhoveReport.deleteOnExit();

		return isValid;
	}
}