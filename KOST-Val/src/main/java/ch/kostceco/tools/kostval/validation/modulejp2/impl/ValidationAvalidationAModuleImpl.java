/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2021 Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.modulejp2.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulejp2.ValidationAjp2validationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejp2.ValidationAvalidationAModule;

/** Ist die vorliegende JP2-Datei eine valide JP2-Datei? JP2 Validierungs mit Jpylyzer.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit Jpylyzer.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */

public class ValidationAvalidationAModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationAModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws ValidationAjp2validationException
	{
		String onWork = configMap.get( "ShowProgressOnWork" );
		if ( onWork.equals( "nomin" ) ) {
			min = true;
		}

		// Start mit der Erkennung

		// Eine JP2 Datei (.jp2) muss mit ....jP ..ï¿½.ftypjp2
		// [0000000c6a5020200d0a870a] beginnen

		/* Sicherstellen, dass es ein JP2 und kein JPX ist. Jpylyzer kann nur JP2 und gibt sonst keine
		 * korrekte Fehlermeldung raus.
		 * 
		 * JP2-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 32
		 * 
		 * 1 2 3 4 5 6 7 8 9 10 11 12 17 18 19 20 21 22 23
		 * 
		 * JPX-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 78
		 * 
		 * JPM-BOF: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A {4} 66 74 79 70 6A 70 6D */
		if ( valDatei.isDirectory() ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_ISDIRECTORY ) );
				return false;
			}
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".jp2" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// wobei hier nur die ersten 23 Zeichen der Datei ausgelesen werden
				// 1 00 010203
				// 2 0c 04
				// 3 6a 0521
				// 4 50 06
				// 5 20 0708
				// 6 0d 09
				// 7 0a 1012

				// 9 66 17
				// 10 74 18
				// 11 79 19
				// 12 70 2022
				// 13 32 23

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

				// Hex 66 in Char umwandeln
				String str9 = "66";
				int i9 = Integer.parseInt( str9, 16 );
				char c9 = (char) i9;
				// Hex 74 in Char umwandeln
				String str10 = "74";
				int i10 = Integer.parseInt( str10, 16 );
				char c10 = (char) i10;
				// Hex 79 in Char umwandeln
				String str11 = "79";
				int i11 = Integer.parseInt( str11, 16 );
				char c11 = (char) i11;
				// Hex 70 in Char umwandeln
				String str12 = "70";
				int i12 = Integer.parseInt( str12, 16 );
				char c12 = (char) i12;
				// Hex 32 in Char umwandeln
				String str13 = "32";
				int i13 = Integer.parseInt( str13, 16 );
				char c13 = (char) i13;

				// auslesen der ersten 10 Zeichen der Datei
				int length;
				int i;
				char[] buffer = new char[10];
				length = read.read( buffer );
				for ( i = 0; i != length; i++ )
					;

				/* die beiden charArrays (soll und ist) mit einander vergleichen IST =
				 * c1c1c1c2c3c4c5c5c6c7 */
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { c1, c1, c1, c2, c3, c4, c5, c5, c6, c7 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					/* hoechstwahrscheinlich ein JP2 da es mit 0000000c6a5020200d0a respektive ....jP ..ï¿½
					 * beginnt */
					// System.out.println("es ist ein JP2 oder JPX ");

					// auslesen der ersten 23 Zeiche der Datei
					FileReader fr23 = new FileReader( valDatei );
					BufferedReader read23 = new BufferedReader( fr23 );
					int length23;
					int i23;
					char[] buffer23 = new char[23];
					length23 = read23.read( buffer23 );
					for ( i23 = 0; i23 != length23; i23++ )
						;

					/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c9, c10, c11, c12,
					 * c3, c12, c13 */
					char[] charArray3 = buffer23;
					char[] charArray4 = new char[] { c9, c10, c11, c12, c3, c12, c13 };
					String stringCharArray3 = String.valueOf( charArray3 );
					String stringCharArray4 = String.valueOf( charArray4 );
					buffer23 = null;
					read23.close();
					fr23.close();
					if ( stringCharArray3.endsWith( stringCharArray4 ) ) {
						// System.out.print("es ist ein JP2 (JPEG2000 Part1)");
					} else {
						// System.out.print("es ist ein JPX (Part2) / JPM (Part6)");
						fr.close();
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
											+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_INCORRECTFILE,
													"JPX/JPM" ) );
							return false;
						}
					}

				} else {
					// Droid-Erkennung, damit Details ausgegeben werden koennen

					String nameOfSignature = configMap.get( "PathToDroidSignatureFile" );
					if ( nameOfSignature.startsWith( "Configuration-Error:" ) ) {
						fr.close();
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
											+ nameOfSignature );
							return false;
						}
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						fr.close();
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
											+ getTextResourceService().getText( locale, MESSAGE_XML_CA_DROID ) );
							return false;
						}
					}

					Droid droid = null;
					try {
						/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den
						 * Output stoeren Util.switchOffConsole() als Kommentar markieren wenn man die
						 * Fehlermeldung erhalten moechte */
						Util.switchOffConsole();
						droid = new Droid();

						droid.readSignatureFile( nameOfSignature );

					} catch ( Exception e ) {
						fr.close();
						read.close();
						if ( min ) {
							return false;
						} else {
							getMessageService().logError( getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_CANNOT_INITIALIZE_DROID ) );
							return false;
						}
					} finally {
						Util.switchOnConsole();
					}
					File file = valDatei;
					String puid = " ??? ";
					IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
					for ( int x = 0; x < ifile.getNumHits(); x++ ) {
						FileFormatHit ffh = ifile.getHit( x );
						FileFormat ff = ffh.getFileFormat();
						puid = ff.getPUID();
					}
					fr.close();
					read.close();
					if ( min ) {
						return false;
					} else {
						getMessageService().logError( getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_INCORRECTFILE, puid ) );
						return false;
					}
				}
				read.close();
			} catch ( Exception e ) {
				if ( min ) {
					return false;
				} else {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_INCORRECTFILE ) );
					return false;
				}
			}
		} else {
			// die Datei endet nicht mit jp2 -> Fehler
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_INCORRECTFILEENDING ) );
				return false;
			}
		}
		// Ende der Erkennung

		boolean isValid = false;

		// TODO: Erledigt - Initialisierung Jpylyzer -> existiert Jpylyzer?

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
		if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" )
				|| locationOfJarPath.endsWith( "." ) ) {
			File file = new File( locationOfJarPath );
			dirOfJarPath = file.getParent();
		}

		String pathToJpylyzerExe = dirOfJarPath + File.separator + "resources" + File.separator
				+ "jpylyzer_2.0.0_win32" + File.separator + "jpylyzer.exe";

		File fJpylyzerExe = new File( pathToJpylyzerExe );
		if ( !fJpylyzerExe.exists() ) {
			if ( min ) {
				return false;
			} else {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_JPYLYZER_MISSING ) );
			}
		}

		pathToJpylyzerExe = "\"" + pathToJpylyzerExe + "\"";

		try {
			File report;
			Document doc = null;

			try {
				// jpylyzer-Befehl: pathToJpylyzerExe valDatei > valDatei.jpylyzer-log.xml
				String outputPath = directoryOfLogfile.getAbsolutePath();
				String outputName = File.separator + valDatei.getName() + ".jpylyzer-log.xml";
				String pathToJpylyzerReport = outputPath + outputName;
				File output = new File( pathToJpylyzerReport );
				Runtime rt = Runtime.getRuntime();
				Process proc = null;

				try {
					report = output;

					// falls das File von einem vorhergehenden Durchlauf bereits existiert, loeschen wir es
					if ( report.exists() ) {
						report.delete();
					}

					/* Das redirect Zeichen verunmoeglicht eine direkte eingabe. mit dem geschachtellten
					 * Befehl gehts: cmd /c\"urspruenlicher Befehl\" */
					String command = "cmd /c \"" + pathToJpylyzerExe + " \"" + valDatei.getAbsolutePath()
							+ "\" > \"" + output.getAbsolutePath() + "\"\"";
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Warte, bis proc fertig ist
					proc.waitFor();

				} catch ( Exception e ) {
					if ( min ) {
						return false;
					} else {
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_SERVICEFAILED,
												e.getMessage() ) );
						return false;
					}
				} finally {
					if ( proc != null ) {
						proc.getOutputStream().close();
						proc.getInputStream().close();
						proc.getErrorStream().close();
					}
				}
				if ( report.exists() ) {
					// alles io
				} else {
					// Datei nicht angelegt...
					if ( min ) {
						return false;
					} else {
						getMessageService()
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_NOREPORT ) );
						return false;
					}
				}

				// Ende Jpylyzer direkt auszuloesen

				// TODO: Erledigt - Ergebnis auslesen

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( pathToJpylyzerReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();

				NodeList nodeLstI = doc.getElementsByTagName( "isValid" );
				// <isValid format="jp2">True</isValid>

				// Node isValidJP2 enthaelt im TextNode das Resultat TextNode ist ein ChildNode
				for ( int s = 0; s < nodeLstI.getLength(); s++ ) {
					Node resultNode = nodeLstI.item( s );
					StringBuffer buf = new StringBuffer();
					NodeList children = resultNode.getChildNodes();
					for ( int i = 0; i < children.getLength(); i++ ) {
						Node textChild = children.item( i );
						if ( textChild.getNodeType() != Node.TEXT_NODE ) {
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
						if ( min ) {
							return false;
						} else {
							getMessageService()
									.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
											+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_JPYLYZER_FAIL ) );
							isValid = false;
						}
					}
				}
				bis.close();

			} catch ( Exception e ) {
				getMessageService()
						.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Erledigt: Fehler Auswertung

			if ( !isValid ) {
				// Invalide JP2-Datei

				/* neu werden die Fehler zusammengefasst, damit nicht bei jeder neuen Version das Mapping
				 * der Pruefschritte zu den Fehlern komplett neu erstellt werden muss.
				 * 
				 * A Erkennung und Struktur
				 * 
				 * • A SignatureBox • A FileTypeBox • A JP2HeaderBox • A - ImageHeaderBox • A -
				 * ColourSpecificationBox • A - BitsPerComponentBox • A - PaletteBox • A -
				 * ComponentMappingBox • A - ChannelDefinitionBox • A - ResolutionBox
				 * 
				 * B Metadaten-Validierung
				 * 
				 * • B XmlBox • B UuidInfoBox • B UuidBox • B IntellectualProperty
				 * 
				 * C Bild-Validierung
				 * 
				 * • C ContiguousCodestreamBox • C - Siz = {0} {0} in the CC • C - Coc = {0} {0} in the CC •
				 * C - Rgn = {0} {0} in the CC • C - Qcd = {0} {0} in the CC • C - Qcc = {0} {0} in the CC •
				 * C - Poc = {0} {0} in the CC • C - Crg = {0} {0} in the CC • C - Com = {0} {0} in the CC •
				 * C - tile = {0} {0} in the CC • C - Soc = {0} {0} in the CC • C - Eoc = {0} {0} in the CC
				 * • C - Cod = {0} {0} in the CC
				 * 
				 * D Sonstige Validierung
				 * 
				 * • D ELSE nicht zugeordnet ({0}) */
				int isignatureBox = 0;
				int ifileTypeBox = 0;
				int ijp2HeaderBox = 0;
				int iimageHeaderBox = 0;
				int icolourSpecificationBox = 0;
				int ibitsPerComponentBox = 0;
				int ipaletteBox = 0;
				int icomponentMappingBox = 0;
				int ichannelDefinitionBox = 0;
				int iresolutionBox = 0;

				int ixmlBox = 0;
				int iuuidInfoBox = 0;
				int iuuidBox = 0;
				int iintellectualProperty = 0;

				int icontiguousCodestreamBox = 0;
				int isiz = 0;
				int icoc = 0;
				int irgn = 0;
				int iqcd = 0;
				int iqcc = 0;
				int ipoc = 0;
				int icrg = 0;
				int icom = 0;
				int itile = 0;
				int isoc = 0;
				int ieoc = 0;
				int icod = 0;

				int iunknown = 0;
				String sunknown = "";

				NodeList nodeLstTest = doc.getElementsByTagName( "tests" );

				// Node test enthaelt alle invaliden tests
				for ( int s = 0; s < nodeLstTest.getLength(); s++ ) {
					Node testNode = nodeLstTest.item( s );
					NodeList children = testNode.getChildNodes();
					for ( int i = 0; i < children.getLength(); i++ ) {
						Node textChild = children.item( i );
						if ( textChild.getNodeType() == Node.ELEMENT_NODE ) {
							if ( textChild.getNodeName().equals( "signatureBox" ) ) {
								isignatureBox = isignatureBox + 1;
							} else if ( textChild.getNodeName().equals( "fileTypeBox" ) ) {
								ifileTypeBox = ifileTypeBox + 1;
							} else if ( textChild.getNodeName().equals( "jp2HeaderBox" ) ) {
								ijp2HeaderBox = ijp2HeaderBox + 1;
								NodeList childrenII = textChild.getChildNodes();
								for ( int j = 0; j < childrenII.getLength(); j++ ) {
									/* • A - ImageHeaderBox • A - ColourSpecificationBox • A - BitsPerComponentBox • A
									 * - PaletteBox • A - ComponentMappingBox • A - ChannelDefinitionBox • A -
									 * ResolutionBox */
									Node textChildII = childrenII.item( j );
									if ( textChildII.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildII.getNodeName().contains( "mageHeaderBox" ) ) {
											iimageHeaderBox = iimageHeaderBox + 1;
										} else if ( textChildII.getNodeName().contains( "olourSpecificationBox" ) ) {
											icolourSpecificationBox = icolourSpecificationBox + 1;
										} else if ( textChildII.getNodeName().contains( "itsPerComponentBox" ) ) {
											ibitsPerComponentBox = ibitsPerComponentBox + 1;
										} else if ( textChildII.getNodeName().contains( "aletteBox" ) ) {
											ipaletteBox = ipaletteBox + 1;
										} else if ( textChildII.getNodeName().contains( "omponentMappingBox" ) ) {
											icomponentMappingBox = icomponentMappingBox + 1;
										} else if ( textChildII.getNodeName().contains( "hannelDefinitionBox" ) ) {
											ichannelDefinitionBox = ichannelDefinitionBox + 1;
										} else if ( textChildII.getNodeName().contains( "esolutionBox" ) ) {
											iresolutionBox = iresolutionBox + 1;
										} else {
											iunknown = iunknown + 1;
											sunknown = sunknown + " " + textChildII.getNodeName();
										}
									}
									continue;
								}

							} else if ( textChild.getNodeName().contains( "mlBox" ) ) {
								ixmlBox = ixmlBox + 1;
							} else if ( textChild.getNodeName().contains( "uidBox" ) ) {
								iuuidBox = iuuidBox + 1;
							} else if ( textChild.getNodeName().contains( "uidInfoBox" ) ) {
								iuuidInfoBox = iuuidInfoBox + 1;
							} else if ( textChild.getNodeName().contains( "ntellectualProperty" ) ) {
								iintellectualProperty = iintellectualProperty + 1;

							} else if ( textChild.getNodeName().equals( "contiguousCodestreamBox" ) ) {
								icontiguousCodestreamBox = icontiguousCodestreamBox + 1;
								NodeList childrenIII = textChild.getChildNodes();
								for ( int k = 0; k < childrenIII.getLength(); k++ ) {
									/* • C - Siz = {0} {0} in the CC • C - Coc = {0} {0} in the CC • C - Rgn = {0} {0}
									 * in the CC • C - Qcd = {0} {0} in the CC • C - Qcc = {0} {0} in the CC • C - Poc
									 * = {0} {0} in the CC • C - Crg = {0} {0} in the CC • C - Com = {0} {0} in the CC
									 * • C - tile = {0} {0} in the CC • C - Soc = {0} {0} in the CC • C - Eoc = {0}
									 * {0} in the CC • C - Cod = {0} {0} in the CC */
									Node textChildIII = childrenIII.item( k );
									if ( textChildIII.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildIII.getNodeName().contains( "siz" )
												|| textChildIII.getNodeName().contains( "SIZ" )
												|| textChildIII.getNodeName().contains( "Siz" ) ) {
											isiz = isiz + 1;
										} else if ( textChildIII.getNodeName().contains( "coc" )
												|| textChildIII.getNodeName().contains( "COC" )
												|| textChildIII.getNodeName().contains( "Coc" ) ) {
											icoc = icoc + 1;
										} else if ( textChildIII.getNodeName().contains( "rgn" )
												|| textChildIII.getNodeName().contains( "RGN" )
												|| textChildIII.getNodeName().contains( "Rgn" ) ) {
											irgn = irgn + 1;
										} else if ( textChildIII.getNodeName().contains( "qcd" )
												|| textChildIII.getNodeName().contains( "QCD" )
												|| textChildIII.getNodeName().contains( "Qcd" ) ) {
											iqcd = iqcd + 1;
										} else if ( textChildIII.getNodeName().contains( "qcc" )
												|| textChildIII.getNodeName().contains( "QCC" )
												|| textChildIII.getNodeName().contains( "Qcc" ) ) {
											iqcc = iqcc + 1;
										} else if ( textChildIII.getNodeName().contains( "poc" )
												|| textChildIII.getNodeName().contains( "POC" )
												|| textChildIII.getNodeName().contains( "Poc" ) ) {
											ipoc = ipoc + 1;
										} else if ( textChildIII.getNodeName().contains( "crg" )
												|| textChildIII.getNodeName().contains( "CRG" )
												|| textChildIII.getNodeName().contains( "Crg" ) ) {
											icrg = icrg + 1;
										} else if ( textChildIII.getNodeName().contains( "com" )
												|| textChildIII.getNodeName().contains( "COM" )
												|| textChildIII.getNodeName().contains( "Com" ) ) {
											icom = icom + 1;
										} else if ( textChildIII.getNodeName().contains( "tile" )
												|| textChildIII.getNodeName().contains( "TILE" )
												|| textChildIII.getNodeName().contains( "Tile" ) ) {
											itile = itile + 1;
										} else if ( textChildIII.getNodeName().contains( "soc" )
												|| textChildIII.getNodeName().contains( "SOC" )
												|| textChildIII.getNodeName().contains( "Soc" ) ) {
											isoc = isoc + 1;
										} else if ( textChildIII.getNodeName().contains( "eoc" )
												|| textChildIII.getNodeName().contains( "EOC" )
												|| textChildIII.getNodeName().contains( "Eoc" ) ) {
											ieoc = ieoc + 1;
										} else if ( textChildIII.getNodeName().contains( "cod" )
												|| textChildIII.getNodeName().contains( "COD" )
												|| textChildIII.getNodeName().contains( "Cod" ) ) {
											icod = icod + 1;
										} else {
											iunknown = iunknown + 1;
											sunknown = sunknown + " " + textChildIII.getNodeName();
										}
									}
									continue;
								}

							} else {
								iunknown = iunknown + 1;
								sunknown = sunknown + " " + textChild.getNodeName();
							}

						}
						continue;
					}
					continue;
				}

				if ( isignatureBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_SIGNATURE ) );
				}
				if ( ifileTypeBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_FILETYPE ) );
				}
				if ( ijp2HeaderBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_HEADER ) );
				}
				if ( iimageHeaderBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_IMAGE ) );
				}
				if ( icolourSpecificationBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_COLOUR ) );
				}
				if ( ibitsPerComponentBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_BITSPC ) );
				}
				if ( ipaletteBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_PALETTE ) );
				}
				if ( icomponentMappingBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_MAPPING ) );
				}
				if ( ichannelDefinitionBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_CHANNEL ) );
				}
				if ( iresolutionBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_A_JP2_RESOLUTION ) );
				}

				if ( ixmlBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_B_JP2_XML ) );
				}
				if ( iuuidInfoBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_B_JP2_UUIDINFO ) );
				}
				if ( iuuidBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_B_JP2_UUID ) );
				}
				if ( iintellectualProperty >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_B_JP2_INTELLECTUAL ) );
				}

				if ( icontiguousCodestreamBox >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_CODEBOX ) );
				}
				if ( isiz >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_SIZ ) );
				}
				if ( icoc >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_COC ) );
				}
				if ( irgn >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_RGN ) );
				}
				if ( iqcd >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_QCD ) );
				}
				if ( iqcc >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_QCC ) );
				}
				if ( ipoc >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_POC ) );
				}
				if ( icrg >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_CRG ) );
				}
				if ( icom >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_COM ) );
				}
				if ( itile >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_TILE ) );
				}
				if ( isoc >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_SOC ) );
				}
				if ( ieoc >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_EOC ) );
				}
				if ( icod >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_C_JP2_COD ) );
				}

				if ( iunknown >= 1 ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_D_JP2 )
									+ getTextResourceService().getText( locale, ERROR_XML_D_JP2_UNKNOWN, sunknown ) );
				}
			}
		 doc = null;

		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}
}
