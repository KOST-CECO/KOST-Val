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

import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;
import uk.gov.nationalarchives.droid.core.signature.droid4.signaturefile.FileFormat;

import ch.kostceco.tools.kostval.exception.modulejp2.ValidationAjp2validationException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejp2.ValidationAvalidationAModule;

/** Ist die vorliegende JP2-Datei eine valide JP2-Datei? JP2 Validierungs mit Jpylyzer.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit Jpylyzer.
 * 
 * TODO: Die Fehler werden soweit wie möglich als sprechender Text ausgegeben
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */

public class ValidationAvalidationAModuleImpl extends ValidationModuleImpl implements
		ValidationAvalidationAModule
{
	private ConfigurationService	configurationService;

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
			throws ValidationAjp2validationException
	{

		// Start mit der Erkennung

		// Eine JP2 Datei (.jp2) muss mit ....jP ..‡.ftypjp2
		// [0000000c6a5020200d0a870a] beginnen
		if ( valDatei.isDirectory() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( ERROR_XML_A_JP2_ISDIRECTORY ) );
			return false;
		} else if ( (valDatei.getAbsolutePath().toLowerCase().endsWith( ".jp2" )) ) {

			FileReader fr = null;

			try {
				fr = new FileReader( valDatei );
				BufferedReader read = new BufferedReader( fr );

				// wobei hier nur die ersten 10 Zeichen der Datei ausgelesen werden
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

				/* die beiden charArrays (soll und ist) mit einander vergleichen IST = c1c1c1c2c3c4c5c5c6c7 */
				char[] charArray1 = buffer;
				char[] charArray2 = new char[] { c1, c1, c1, c2, c3, c4, c5, c5, c6, c7 };

				if ( Arrays.equals( charArray1, charArray2 ) ) {
					/* höchstwahrscheinlich ein JP2 da es mit 0000000c6a5020200d0a respektive ....jP ..‡
					 * beginnt */
				} else {
					// TODO: Droid-Erkennung, damit Details ausgegeben werden können
					String nameOfSignature = getConfigurationService().getPathToDroidSignatureFile();
					if ( nameOfSignature == null ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText(
												MESSAGE_XML_CONFIGURATION_ERROR_NO_SIGNATURE ) );
						return false;
					}
					// existiert die SignatureFile am angebenen Ort?
					File fnameOfSignature = new File( nameOfSignature );
					if ( !fnameOfSignature.exists() ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText( MESSAGE_XML_CA_DROID ) );
						return false;
					}

					Droid droid = null;
					try {
						/* kleiner Hack, weil die Droid libraries irgendwo ein System.out drin haben, welche den
						 * Output stören Util.switchOffConsole() als Kommentar markieren wenn man die
						 * Fehlermeldung erhalten möchte */
						Util.switchOffConsole();
						droid = new Droid();

						droid.readSignatureFile( nameOfSignature );

					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText( ERROR_XML_CANNOT_INITIALIZE_DROID ) );
						return false;
					} finally {
						Util.switchOnConsole();
					}
					File file = valDatei;
					String puid = "";
					IdentificationFile ifile = droid.identify( file.getAbsolutePath() );
					for ( int x = 0; x < ifile.getNumHits(); x++ ) {
						FileFormatHit ffh = ifile.getHit( x );
						FileFormat ff = ffh.getFileFormat();
						puid = ff.getPUID();
					}
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( ERROR_XML_A_JP2_INCORRECTFILE, puid ) );
					return false;
				}
			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( ERROR_XML_A_JP2_INCORRECTFILE ) );
				return false;
			}
		} else {
			// die Datei endet nicht mit jp2 -> Fehler
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( ERROR_XML_A_JP2_INCORRECTFILEENDING ) );
			return false;
		}
		// Ende der Erkennung

		boolean isValid = false;

		// TODO: Erledigt - Initialisierung Jpylyzer -> existiert Jpylyzer?
		String pathToJpylyzerExe = "resources" + File.separator + "jpylyzer" + File.separator
				+ "jpylyzer.exe";

		File fJpylyzerExe = new File( pathToJpylyzerExe );
		if ( !fJpylyzerExe.exists() ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( ERROR_XML_A_JP2_JPYLYZER_MISSING ) );
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

					// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf, löschen wir
					// es
					if ( report.exists() ) {
						report.delete();
					}

					/* Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem geschachtellten Befehl
					 * gehts: cmd /c\"urspruenlicher Befehl\" */
					String command = "cmd /c \"" + pathToJpylyzerExe + " \"" + valDatei.getAbsolutePath()
							+ "\" > \"" + output.getAbsolutePath() + "\"\"";
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Warte, bis proc fertig ist
					proc.waitFor();

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService()
											.getText( ERROR_XML_A_JP2_SERVICEFAILED, e.getMessage() ) );
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
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( ERROR_XML_A_JP2_NOREPORT ) );
					return false;
				}

				// Ende Jpylyzer direkt auszulösen

				// TODO: Erledigt - Ergebnis auslesen

				BufferedInputStream bis = new BufferedInputStream( new FileInputStream(
						pathToJpylyzerReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();

				NodeList nodeLstI = doc.getElementsByTagName( "isValidJP2" );

				// Node isValidJP2 enthält im TextNode das Resultat TextNode ist ein ChildNode
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
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
										+ getTextResourceService().getText( ERROR_XML_A_JP2_JPYLYZER_FAIL ) );
						isValid = false;
					}
				}

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Erledigt: Fehler Auswertung

			if ( !isValid ) {
				// Invalide JP2-Datei
				int isignatureBox = 0;
				int ifileTypeBox = 0;
				int iimageHeaderBox = 0;
				int ibitsPerComponentBox = 0;
				int icolourSpecificationBox = 0;
				int ipaletteBox = 0;
				int icomponentMappingBox = 0;
				int ichannelDefinitionBox = 0;
				int iresolutionBox = 0;
				int itileParts = 0;
				int isiz = 0;
				int icod = 0;
				int iqcd = 0;
				int icoc = 0;
				int icom = 0;
				int iqcc = 0;
				int irgn = 0;
				int ipoc = 0;
				int iplm = 0;
				int ippm = 0;
				int itlm = 0;
				int icrg = 0;
				int iplt = 0;
				int ippt = 0;
				int ixmlBox = 0;
				int iuuidBox = 0;
				int iuuidInfoBox = 0;
				int iunknownBox = 0;
				int icontainsImageHeaderBox = 0;
				int icontainsColourSpecificationBox = 0;
				int icontainsBitsPerComponentBox = 0;
				int ifirstJP2HeaderBoxIsImageHeaderBox = 0;
				int inoMoreThanOneImageHeaderBox = 0;
				int inoMoreThanOneBitsPerComponentBox = 0;
				int inoMoreThanOnePaletteBox = 0;
				int inoMoreThanOneComponentMappingBox = 0;
				int inoMoreThanOneChannelDefinitionBox = 0;
				int inoMoreThanOneResolutionBox = 0;
				int icolourSpecificationBoxesAreContiguous = 0;
				int ipaletteAndComponentMappingBoxesOnlyTogether = 0;

				int icodestreamStartsWithSOCMarker = 0;
				int ifoundSIZMarker = 0;
				int ifoundCODMarker = 0;
				int ifoundQCDMarker = 0;
				int iquantizationConsistentWithLevels = 0;
				int ifoundExpectedNumberOfTiles = 0;
				int ifoundExpectedNumberOfTileParts = 0;
				int ifoundEOCMarker = 0;

				NodeList nodeLstTest = doc.getElementsByTagName( "tests" );

				// Node test enthält alle invaliden tests
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
								NodeList childrenII = textChild.getChildNodes();
								for ( int j = 0; j < childrenII.getLength(); j++ ) {
									Node textChildII = childrenII.item( j );
									if ( textChildII.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildII.getNodeName().equals( "imageHeaderBox" ) ) {
											iimageHeaderBox = iimageHeaderBox + 1;
										} else if ( textChildII.getNodeName().equals( "bitsPerComponentBox" ) ) {
											ibitsPerComponentBox = ibitsPerComponentBox + 1;
										} else if ( textChildII.getNodeName().equals( "colourSpecificationBox" ) ) {
											icolourSpecificationBox = icolourSpecificationBox + 1;
										} else if ( textChildII.getNodeName().equals( "paletteBox" ) ) {
											ipaletteBox = ipaletteBox + 1;
										} else if ( textChildII.getNodeName().equals( "componentMappingBox" ) ) {
											icomponentMappingBox = icomponentMappingBox + 1;
										} else if ( textChildII.getNodeName().equals( "channelDefinitionBox" ) ) {
											ichannelDefinitionBox = ichannelDefinitionBox + 1;
										} else if ( textChildII.getNodeName().equals( "resolutionBox" ) ) {
											iresolutionBox = iresolutionBox + 1;
										} else if ( textChildII.getNodeName().equals( "containsImageHeaderBox" ) ) {
											icontainsImageHeaderBox = icontainsImageHeaderBox + 1;
										} else if ( textChildII.getNodeName().equals( "containsColourSpecificationBox" ) ) {
											icontainsColourSpecificationBox = icontainsColourSpecificationBox + 1;
										} else if ( textChildII.getNodeName().equals( "containsBitsPerComponentBox" ) ) {
											icontainsBitsPerComponentBox = icontainsBitsPerComponentBox + 1;
										} else if ( textChildII.getNodeName().equals(
												"firstJP2HeaderBoxIsImageHeaderBox" ) ) {
											ifirstJP2HeaderBoxIsImageHeaderBox = ifirstJP2HeaderBoxIsImageHeaderBox + 1;
										} else if ( textChildII.getNodeName().equals( "noMoreThanOneImageHeaderBox" ) ) {
											inoMoreThanOneImageHeaderBox = inoMoreThanOneImageHeaderBox + 1;
										} else if ( textChildII.getNodeName().equals(
												"noMoreThanOneBitsPerComponentBox" ) ) {
											inoMoreThanOneBitsPerComponentBox = inoMoreThanOneBitsPerComponentBox + 1;
										} else if ( textChildII.getNodeName().equals( "noMoreThanOnePaletteBox" ) ) {
											inoMoreThanOnePaletteBox = inoMoreThanOnePaletteBox + 1;
										} else if ( textChildII.getNodeName().equals(
												"noMoreThanOneComponentMappingBox" ) ) {
											inoMoreThanOneComponentMappingBox = inoMoreThanOneComponentMappingBox + 1;
										} else if ( textChildII.getNodeName().equals(
												"noMoreThanOneChannelDefinitionBox" ) ) {
											inoMoreThanOneChannelDefinitionBox = inoMoreThanOneChannelDefinitionBox + 1;
										} else if ( textChildII.getNodeName().equals( "noMoreThanOneResolutionBox" ) ) {
											inoMoreThanOneResolutionBox = inoMoreThanOneResolutionBox + 1;
										} else if ( textChildII.getNodeName().equals(
												"colourSpecificationBoxesAreContiguous" ) ) {
											icolourSpecificationBoxesAreContiguous = icolourSpecificationBoxesAreContiguous + 1;
										} else if ( textChildII.getNodeName().equals(
												"paletteAndComponentMappingBoxesOnlyTogether" ) ) {
											ipaletteAndComponentMappingBoxesOnlyTogether = ipaletteAndComponentMappingBoxesOnlyTogether + 1;
										}
									}
									continue;
								}
							} else if ( textChild.getNodeName().equals( "contiguousCodestreamBox" ) ) {
								NodeList childrenIII = textChild.getChildNodes();
								for ( int k = 0; k < childrenIII.getLength(); k++ ) {
									Node textChildIII = childrenIII.item( k );
									if ( textChildIII.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildIII.getNodeName().equals( "tileParts" ) ) {
											itileParts = itileParts + 1;
										} else if ( textChildIII.getNodeName().equals( "siz" ) ) {
											isiz = isiz + 1;
										} else if ( textChildIII.getNodeName().equals( "cod" ) ) {
											icod = icod + 1;
										} else if ( textChildIII.getNodeName().equals( "qcd" ) ) {
											iqcd = iqcd + 1;
										} else if ( textChildIII.getNodeName().equals( "coc" ) ) {
											icoc = icoc + 1;
										} else if ( textChildIII.getNodeName().equals( "com" ) ) {
											icom = icom + 1;
										} else if ( textChildIII.getNodeName().equals( "qcc" ) ) {
											iqcc = iqcc + 1;
										} else if ( textChildIII.getNodeName().equals( "rgn" ) ) {
											irgn = irgn + 1;
										} else if ( textChildIII.getNodeName().equals( "poc" ) ) {
											ipoc = ipoc + 1;
										} else if ( textChildIII.getNodeName().equals( "plm" ) ) {
											iplm = iplm + 1;
										} else if ( textChildIII.getNodeName().equals( "ppm" ) ) {
											ippm = ippm + 1;
										} else if ( textChildIII.getNodeName().equals( "tlm" ) ) {
											itlm = itlm + 1;
										} else if ( textChildIII.getNodeName().equals( "crg" ) ) {
											icrg = icrg + 1;
										} else if ( textChildIII.getNodeName().equals( "plt" ) ) {
											iplt = iplt + 1;
										} else if ( textChildIII.getNodeName().equals( "ppt" ) ) {
											ippt = ippt + 1;
										} else if ( textChildIII.getNodeName().equals( "codestreamStartsWithSOCMarker" ) ) {
											icodestreamStartsWithSOCMarker = icodestreamStartsWithSOCMarker + 1;
										} else if ( textChildIII.getNodeName().equals( "foundSIZMarker" ) ) {
											ifoundSIZMarker = ifoundSIZMarker + 1;
										} else if ( textChildIII.getNodeName().equals( "foundCODMarker" ) ) {
											ifoundCODMarker = ifoundCODMarker + 1;
										} else if ( textChildIII.getNodeName().equals( "foundQCDMarker" ) ) {
											ifoundQCDMarker = ifoundQCDMarker + 1;
										} else if ( textChildIII.getNodeName().equals(
												"quantizationConsistentWithLevels" ) ) {
											iquantizationConsistentWithLevels = iquantizationConsistentWithLevels + 1;
										} else if ( textChildIII.getNodeName().equals( "foundExpectedNumberOfTiles" ) ) {
											ifoundExpectedNumberOfTiles = ifoundExpectedNumberOfTiles + 1;
										} else if ( textChildIII.getNodeName()
												.equals( "foundExpectedNumberOfTileParts" ) ) {
											ifoundExpectedNumberOfTileParts = ifoundExpectedNumberOfTileParts + 1;
										} else if ( textChildIII.getNodeName().equals( "foundEOCMarker" ) ) {
											ifoundEOCMarker = ifoundEOCMarker + 1;
										}
									}
									continue;
								}
							} else if ( textChild.getNodeName().equals( "xmlBox" ) ) {
								ixmlBox = ixmlBox + 1;
							} else if ( textChild.getNodeName().equals( "uuidBox" ) ) {
								iuuidBox = iuuidBox + 1;
							} else if ( textChild.getNodeName().equals( "uuidInfoBox" ) ) {
								iuuidInfoBox = iuuidInfoBox + 1;
							} else if ( textChild.getNodeName().equals( "unknownBox" ) ) {
								iunknownBox = iunknownBox + 1;
							}
						}
						continue;
					}
					continue;
				}

				if ( isignatureBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( ERROR_XML_A_JP2_SIGNATURE ) );
				}
				if ( ifileTypeBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( ERROR_XML_A_JP2_FILETYPE ) );
				}
				if ( iimageHeaderBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_IMAGE ) );
				}
				if ( ibitsPerComponentBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_BITSPC ) );
				}
				if ( icolourSpecificationBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_COLOUR ) );
				}
				if ( ipaletteBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_PALETTE ) );
				}
				if ( icomponentMappingBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_MAPPING ) );
				}
				if ( ichannelDefinitionBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_CHANNEL ) );
				}
				if ( iresolutionBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_RESOLUTION ) );
				}

				if ( icontainsImageHeaderBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_NOIHB ) );
				}
				if ( icontainsColourSpecificationBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_NOCSB ) );
				}
				if ( icontainsBitsPerComponentBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_NBPCB ) );
				}
				if ( ifirstJP2HeaderBoxIsImageHeaderBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_IHBNF ) );
				}
				if ( inoMoreThanOneImageHeaderBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_IHBMO ) );
				}
				if ( inoMoreThanOneBitsPerComponentBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_OBPCMO ) );
				}
				if ( inoMoreThanOnePaletteBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_OPBMO ) );
				}
				if ( inoMoreThanOneComponentMappingBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_CMBMO ) );
				}
				if ( inoMoreThanOneChannelDefinitionBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_CDBMO ) );
				}
				if ( inoMoreThanOneResolutionBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_RBMO ) );
				}
				if ( icolourSpecificationBoxesAreContiguous >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_CSBNC ) );
				}
				if ( ipaletteAndComponentMappingBoxesOnlyTogether >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( ERROR_XML_B_JP2_PACMB ) );
				}

				if ( icodestreamStartsWithSOCMarker >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_SOC ) );
				}
				if ( ifoundSIZMarker >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_FSIZ ) );
				}
				if ( ifoundCODMarker >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_FCOD ) );
				}
				if ( ifoundQCDMarker >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_FQCD ) );
				}
				if ( iquantizationConsistentWithLevels >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_PQCD ) );
				}
				if ( ifoundExpectedNumberOfTiles >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_NOTILES ) );
				}
				if ( ifoundExpectedNumberOfTileParts >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_NOTILESPART ) );
				}
				if ( ifoundEOCMarker >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_EOC ) );
				}

				if ( itileParts >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_TILEPARTS ) );
				}
				if ( isiz >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_SIZ ) );
				}
				if ( icod >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_COD ) );
				}
				if ( iqcd >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_QCD ) );
				}
				if ( icom >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_COM ) );
				}
				if ( icoc >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_COC ) );
				}
				if ( irgn >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_RGN ) );
				}
				if ( iqcc >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_QCC ) );
				}
				if ( ipoc >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_POC ) );
				}
				if ( iplm >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_PLM ) );
				}
				if ( ippm >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_PPM ) );
				}
				if ( itlm >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_TLM ) );
				}
				if ( icrg >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_CRG ) );
				}
				if ( iplt >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_PLT ) );
				}
				if ( ippt >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( ERROR_XML_C_JP2_PPT ) );
				}

				if ( ixmlBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_JP2 )
									+ getTextResourceService().getText( ERROR_XML_D_JP2_XML ) );
				}
				if ( iuuidBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_JP2 )
									+ getTextResourceService().getText( ERROR_XML_D_JP2_UUID ) );
				}
				if ( iuuidInfoBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_JP2 )
									+ getTextResourceService().getText( ERROR_XML_D_JP2_UUIDINFO ) );
				}
				if ( iunknownBox >= 1 ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_D_JP2 )
									+ getTextResourceService().getText( ERROR_XML_D_JP2_UNKNOWN ) );
				}
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}
}
