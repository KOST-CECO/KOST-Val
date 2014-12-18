/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationBjhoveValidationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationBjhoveValidationModule;

import edu.harvard.hul.ois.jhove.*;
import edu.harvard.hul.ois.jhove.module.TiffModule;
import edu.harvard.hul.ois.jhove.Module;

/** Validierungsschritt B (Jhove-Validierung) Ist die TIFF-Datei gemäss Jhove valid? valid -->
 * Status: "Well-Formed and valid"
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class ValidationBjhoveValidationModuleImpl extends ValidationModuleImpl implements
		ValidationBjhoveValidationModule
{

	private ConfigurationService	configurationService;

	public static String					NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationBjhoveValidationException
	{

		boolean isValid = true;

		String toplevelDir = valDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		// Vorbereitungen: valDatei an die JHove Applikation übergeben
		File jhoveReport = null;
		StringBuffer concatenatedOutputs = new StringBuffer();
		String pathToJhoveConfig = getConfigurationService().getPathToJhoveConfiguration();
		String pathToWorkDir = getConfigurationService().getPathToWorkDir();

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
				File newReport = new File( pathToJhoveOutput, valDatei.getName() + ".jhove-log.txt" );
				String outputFile = newReport.getAbsolutePath();
				String[] dirFileOrUri = { valDatei.getAbsolutePath() };
				je.dispatch( app, module, null, handler, outputFile, dirFileOrUri );
				jhoveReport = newReport;
			} catch ( Exception e ) {
				e.printStackTrace();
			}

			InputStream inStream = null;
			OutputStream outStream = null;

			try {
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
			}
			inStream.close();
			outStream.close();
			Util.deleteFile( jhoveReport );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		try {
			BufferedReader in = new BufferedReader( new FileReader( jhoveLog ) );
			String line;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );

				/* die Status-Zeile enthält diese Möglichkeiten: Valider Status: "Well-Formed and valid"
				 * Invalider Status: "Not well-formed" oder "Well-Formed, but not valid" möglicherweise
				 * existieren weitere Ausgabemöglichkeiten */
				if ( line.contains( "Status:" ) ) {
					if ( !line.contains( "Well-Formed and valid" ) ) {
						// Invalider Status
						isValid = false;
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
										+ getTextResourceService().getText( MESSAGE_XML_B_JHOVEINVALID, line ) );
					}
				}
				if ( line.contains( "ErrorMessage:" ) ) {
					// Linie mit der Fehlermeldung auch mitausgeben
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
									+ getTextResourceService().getText( MESSAGE_XML_B_JHOVEMESSAGE, line ) );
				}
			}
			in.close();
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// die im StringBuffer JHove-Outputs wieder in das Output-File zurückschreiben
		if ( jhoveReport != null ) {
			try {
				BufferedWriter out = new BufferedWriter( new FileWriter( jhoveReport ) );
				out.write( concatenatedOutputs.toString() );
				out.close();
			} catch ( IOException e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_B_TIFF )
								+ getTextResourceService().getText( MESSAGE_XML_B_CANNOTWRITEJHOVEREPORT ) );
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