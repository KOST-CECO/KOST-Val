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

package ch.kostceco.tools.kostval.validation.modulejp2.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulejp2.ValidationAjp2validationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejp2.ValidationAvalidationAModule;

/**
 * Ist die vorliegende JP2-Datei eine valide JP2-Datei? JP2 Validierungs mit
 * Jpylyzer.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * Jpylyzer.
 * 
 * TODO: Die Fehler werden soweit wie möglich als sprechender Text ausgegeben
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */

public class ValidationAvalidationAModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationAModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws ValidationAjp2validationException
	{

		// Start mit der Erkennung

		// Eine JP2 Datei (.jp2) muss mit ....jP ..‡.ftypjp2
		// [0000000c6a5020200d0a870a] beginnen
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText(
									ERROR_XML_A_JP2_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".jp2" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// wobei hier nur die ersten 10 Zeichen der Datei ausgelesen
				// werden
				// 1 00 010203
				// 2 0c 04
				// 3 6a 05
				// 4 50 06
				// 5 20 0708
				// 6 0d 09
				// 7 0a 10

				// Hex 00 in Char umwandeln
				String str1 = "00";
				int i1 = Integer.parseInt( str1, 16 );
				char c1 = (char) i1;
				// Hex 0c in Char umwandeln
				String str2 = "0c";
				int i2 = Integer.parseInt( str2, 16 );
				char c2 = (char) i2;
				// Hex 6a in Char umwandeln
				String str3 = "6a";
				int i3 = Integer.parseInt( str3, 16 );
				char c3 = (char) i3;
				// Hex 50 in Char umwandeln
				String str4 = "50";
				int i4 = Integer.parseInt( str4, 16 );
				char c4 = (char) i4;
				// Hex 20 in Char umwandeln
				String str5 = "20";
				int i5 = Integer.parseInt( str5, 16 );
				char c5 = (char) i5;
				// Hex 0d in Char umwandeln
				String str6 = "0d";
				int i6 = Integer.parseInt( str6, 16 );
				char c6 = (char) i6;
				// Hex 0a in Char umwandeln
				String str7 = "0a";
				int i7 = Integer.parseInt( str7, 16 );
				char c7 = (char) i7;

				// auslesen der ersten 10 Zeichen der Datei
				int length;
				int i;
				char[] buffer = new char[10];
				length = read.read( buffer );
				for ( i = 0; i != length; i++ )
					;

				// die beiden charArrays (soll und ist) mit einander
				// vergleichen IST = c1c1c1c2c3c4c5c5c6c7
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { c1, c1, c1, c2, c3, c4, c5,
						c5, c6, c7 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					// höchstwahrscheinlich ein JP2 da es mit
					// 0000000c6a5020200d0a respektive ....jP ..‡ beginnt
				} else {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText(
											ERROR_XML_A_JP2_INCORRECTFILE ) );
					return false;
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText(
										ERROR_XML_A_JP2_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit jp2 -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText(
									ERROR_XML_A_JP2_INCORRECTFILEENDING ) );
			return false;
		}
		// Ende der Erkennung

		boolean isValid = false;

		// TODO: Erledigt - Initialisierung Jpylyzer -> existiert Jpylyzer?
		String pathToJpylyzerExe = "resources\\jpylyzer\\jpylyzer.exe";

		File fJpylyzerExe = new File( pathToJpylyzerExe );
		if ( !fJpylyzerExe.exists() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
							+ getTextResourceService().getText(
									ERROR_XML_B_JP2_JPYLYZER_MISSING ) );
		}

		pathToJpylyzerExe = "\"" + pathToJpylyzerExe + "\"";

		try {
			File report;
			Document doc = null;

			try {
				// jpylyzer-Befehl:
				// pathToJpylyzerExe valDatei > valDatei.jpylyzer-log.xml
				String outputPath = directoryOfLogfile.getAbsolutePath();
				String outputName = "\\" + valDatei.getName()
						+ ".jpylyzer-log.xml";
				String pathToJpylyzerReport = outputPath + outputName;
				File output = new File( pathToJpylyzerReport );
				Runtime rt = Runtime.getRuntime();
				Process proc = null;

				try {
					report = output;

					// falls das File bereits existiert, z.B. von einem
					// vorhergehenden Durchlauf, löschen wir es
					if ( report.exists() ) {
						report.delete();
					}

					// Das redirect Zeichen verunmöglicht eine direkte eingabe.
					// mit dem geschachtellten Befehl gehts:
					// cmd /c\"urspruenlicher Befehl\"
					String command = "cmd /c \"" + pathToJpylyzerExe + " \""
							+ valDatei.getAbsolutePath() + "\" > \""
							+ output.getAbsolutePath() + "\"\"";
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein
					// Doppelleerschlag vorhanden ist!

					// Warte, bis proc fertig ist
					proc.waitFor();
					
				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText(
											ERROR_XML_B_JP2_SERVICEFAILED,
											e.getMessage() ) );
					return false;
				} finally {
					if ( proc != null ) {
						closeQuietly( proc.getOutputStream() );
						closeQuietly( proc.getInputStream() );
						closeQuietly( proc.getErrorStream() );
					}
				}
				if ( report.exists() ) {
					// alles io
				} else {
					// Datei nicht angelegt...
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText(
											ERROR_XML_B_JP2_NOREPORT ) );
					return false;
				}

				// Ende Jpylyzer direkt auszulösen

				// TODO: Erledigt - Ergebnis auslesen

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( pathToJpylyzerReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();

				NodeList nodeLstI = doc.getElementsByTagName( "isValidJP2" );

				// Node isValidJP2 enthält im TextNode das Resultat
				// TextNode ist ein ChildNode
				for ( int s = 0; s < nodeLstI.getLength(); s++ ) {
					Node resultNode = nodeLstI.item( s );
					StringBuffer buf = new StringBuffer();
					NodeList children = resultNode.getChildNodes();
					for ( int i = 0; i < children.getLength(); i++ ) {
						Node textChild = children.item( i );
						if ( textChild.getNodeType() != Node.TEXT_NODE ) {
							// System.err.println("Mixed content! Skipping child element "
							// + textChild.getNodeName());
							continue;
						}
						buf.append( textChild.getNodeValue() );
					}
					String result = buf.toString();

					// Das Resultat ist False oder True
					if ( result.equalsIgnoreCase( "True" ) ) {
						// valid
						isValid = true;
					} else {
						// invalide
						getMessageService()
								.logError(
										getTextResourceService().getText(
												MESSAGE_XML_MODUL_B_JP2 )
												+ getTextResourceService()
														.getText(
																ERROR_XML_B_JP2_JPYLYZER_FAIL ) );
						isValid = false;
					}
				}

				// TODO: Fehler Auswertung

				if ( !isValid ) {
					// Invalide JP2-Datei
					NodeList nodeLstII = doc.getElementsByTagName( "tests" );

					// Node tests enthält die Schritte welche nicht bestanden
					// wurden

				}

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText(
								MESSAGE_XML_MODUL_B_JP2 )
								+ getTextResourceService().getText(
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		}

		return isValid;
	}

}
