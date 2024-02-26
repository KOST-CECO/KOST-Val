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

package ch.kostceco.tools.kostval.validation.modulejp2.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kosttools.fileservice.Jpylyzer;
import ch.kostceco.tools.kostval.exception.modulejp2.ValidationAjp2validationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulejp2.ValidationAvalidationAModule;

/**
 * Ist die vorliegende JP2-Datei eine valide JP2-Datei? JP2 Validierungs mit
 * Jpylyzer.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * Jpylyzer.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationAModuleImpl extends ValidationModuleImpl
		implements ValidationAvalidationAModule
{

	private boolean min = false;

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationAjp2validationException
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

		// Die Erkennung erfolgt bereits im Vorfeld

		boolean isValid = false;

		// TODO: Erledigt - Initialisierung Jpylyzer -> existiert Jpylyzer?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Jpylyzer.checkJpylyzer( dirOfJarPath );
		if ( !checkTool.equals( "OK" ) ) {
			if ( min ) {
				return false;
			} else {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService()
												.getText( locale, ABORTED ) ) );
			}
		}

		try {
			Document doc = null;

			try {
				// jpylyzer-Befehl: pathToJpylyzerExe valDatei >
				// valDatei.jpylyzer-log.xml
				String outputPath = directoryOfLogfile.getAbsolutePath();
				String outputName = File.separator + valDatei.getName()
						+ ".jpylyzer-log.xml";
				String pathToJpylyzerReport = outputPath + outputName;
				File output = new File( pathToJpylyzerReport );

				String resultExec = Jpylyzer.execJpylyzer( valDatei, output,
						workDir, dirOfJarPath );
				if ( !resultExec.equals( "OK" ) ) {
					// Exception oder Report existiert nicht
					if ( min ) {
						return false;
					} else {
						if ( resultExec.equals( "NoReport" ) ) {
							// Report existiert nicht

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_MISSING_REPORT,
											output.getAbsolutePath(),
											getTextResourceService().getText(
													locale, ABORTED ) ) );
							return false;
						} else {
							// Exception

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_SERVICEFAILED_EXIT,
											"Jpylyzer", resultExec ) );
							return false;
						}
					}
				}
				// Ende Jpylyzer direkt auszuloesen

				// TODO: Erledigt - Ergebnis auslesen

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream( pathToJpylyzerReport ) );
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse( bis );
				doc.normalize();

				NodeList nodeLstI = doc.getElementsByTagName( "isValid" );
				// <isValid format="jp2">True</isValid>

				// Node isValidJP2 enthaelt im TextNode das Resultat TextNode
				// ist ein ChildNode
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

							Logtxt.logtxt( logFile, getTextResourceService()
									.getText( locale, MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_SERVICEINVALID,
											"Jpylyzer", "" ) );
							isValid = false;
						}
					}
				}
				bis.close();

			} catch ( Exception e ) {

				Logtxt.logtxt( logFile,
						getTextResourceService().getText( locale,
								MESSAGE_XML_MODUL_A_JP2 )
								+ getTextResourceService().getText( locale,
										ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}
			// TODO: Erledigt: Fehler Auswertung

			if ( !isValid ) {
				// Invalide JP2-Datei

				/*
				 * neu werden die Fehler zusammengefasst, damit nicht bei jeder
				 * neuen Version das Mapping der Pruefschritte zu den Fehlern
				 * komplett neu erstellt werden muss.
				 * 
				 * A Erkennung und Struktur
				 * 
				 * � A SignatureBox � A FileTypeBox � A JP2HeaderBox � A -
				 * ImageHeaderBox � A - ColourSpecificationBox � A -
				 * BitsPerComponentBox � A - PaletteBox � A -
				 * ComponentMappingBox � A - ChannelDefinitionBox � A -
				 * ResolutionBox
				 * 
				 * B Metadaten-Validierung
				 * 
				 * � B XmlBox � B UuidInfoBox � B UuidBox � B
				 * IntellectualProperty
				 * 
				 * C Bild-Validierung
				 * 
				 * � C ContiguousCodestreamBox � C - Siz = {0} {0} in the CC � C
				 * - Coc = {0} {0} in the CC � C - Rgn = {0} {0} in the CC � C -
				 * Qcd = {0} {0} in the CC � C - Qcc = {0} {0} in the CC � C -
				 * Poc = {0} {0} in the CC � C - Crg = {0} {0} in the CC � C -
				 * Com = {0} {0} in the CC � C - tile = {0} {0} in the CC � C -
				 * Soc = {0} {0} in the CC � C - Eoc = {0} {0} in the CC � C -
				 * Cod = {0} {0} in the CC
				 * 
				 * D Sonstige Validierung
				 * 
				 * � D ELSE nicht zugeordnet ({0})
				 */
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
							if ( textChild.getNodeName()
									.equals( "signatureBox" ) ) {
								isignatureBox = isignatureBox + 1;
							} else if ( textChild.getNodeName()
									.equals( "fileTypeBox" ) ) {
								ifileTypeBox = ifileTypeBox + 1;
							} else if ( textChild.getNodeName()
									.equals( "jp2HeaderBox" ) ) {
								ijp2HeaderBox = ijp2HeaderBox + 1;
								NodeList childrenII = textChild.getChildNodes();
								for ( int j = 0; j < childrenII
										.getLength(); j++ ) {
									/*
									 * � A - ImageHeaderBox � A -
									 * ColourSpecificationBox � A -
									 * BitsPerComponentBox � A - PaletteBox � A
									 * - ComponentMappingBox � A -
									 * ChannelDefinitionBox � A - ResolutionBox
									 */
									Node textChildII = childrenII.item( j );
									if ( textChildII
											.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildII.getNodeName()
												.contains( "mageHeaderBox" ) ) {
											iimageHeaderBox = iimageHeaderBox
													+ 1;
										} else if ( textChildII.getNodeName()
												.contains(
														"olourSpecificationBox" ) ) {
											icolourSpecificationBox = icolourSpecificationBox
													+ 1;
										} else if ( textChildII.getNodeName()
												.contains(
														"itsPerComponentBox" ) ) {
											ibitsPerComponentBox = ibitsPerComponentBox
													+ 1;
										} else if ( textChildII.getNodeName()
												.contains( "aletteBox" ) ) {
											ipaletteBox = ipaletteBox + 1;
										} else if ( textChildII.getNodeName()
												.contains(
														"omponentMappingBox" ) ) {
											icomponentMappingBox = icomponentMappingBox
													+ 1;
										} else if ( textChildII.getNodeName()
												.contains(
														"hannelDefinitionBox" ) ) {
											ichannelDefinitionBox = ichannelDefinitionBox
													+ 1;
										} else if ( textChildII.getNodeName()
												.contains( "esolutionBox" ) ) {
											iresolutionBox = iresolutionBox + 1;
										} else {
											iunknown = iunknown + 1;
											sunknown = sunknown + " "
													+ textChildII.getNodeName();
										}
									}
									continue;
								}

							} else if ( textChild.getNodeName()
									.contains( "mlBox" ) ) {
								ixmlBox = ixmlBox + 1;
							} else if ( textChild.getNodeName()
									.contains( "uidBox" ) ) {
								iuuidBox = iuuidBox + 1;
							} else if ( textChild.getNodeName()
									.contains( "uidInfoBox" ) ) {
								iuuidInfoBox = iuuidInfoBox + 1;
							} else if ( textChild.getNodeName()
									.contains( "ntellectualProperty" ) ) {
								iintellectualProperty = iintellectualProperty
										+ 1;

							} else if ( textChild.getNodeName()
									.equals( "contiguousCodestreamBox" ) ) {
								icontiguousCodestreamBox = icontiguousCodestreamBox
										+ 1;
								NodeList childrenIII = textChild
										.getChildNodes();
								for ( int k = 0; k < childrenIII
										.getLength(); k++ ) {
									/*
									 * � C - Siz = {0} {0} in the CC � C - Coc =
									 * {0} {0} in the CC � C - Rgn = {0} {0} in
									 * the CC � C - Qcd = {0} {0} in the CC � C
									 * - Qcc = {0} {0} in the CC � C - Poc = {0}
									 * {0} in the CC � C - Crg = {0} {0} in the
									 * CC � C - Com = {0} {0} in the CC � C -
									 * tile = {0} {0} in the CC � C - Soc = {0}
									 * {0} in the CC � C - Eoc = {0} {0} in the
									 * CC � C - Cod = {0} {0} in the CC
									 */
									Node textChildIII = childrenIII.item( k );
									if ( textChildIII
											.getNodeType() == Node.ELEMENT_NODE ) {
										if ( textChildIII.getNodeName()
												.contains( "siz" )
												|| textChildIII.getNodeName()
														.contains( "SIZ" )
												|| textChildIII.getNodeName()
														.contains( "Siz" ) ) {
											isiz = isiz + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "coc" )
												|| textChildIII.getNodeName()
														.contains( "COC" )
												|| textChildIII.getNodeName()
														.contains( "Coc" ) ) {
											icoc = icoc + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "rgn" )
												|| textChildIII.getNodeName()
														.contains( "RGN" )
												|| textChildIII.getNodeName()
														.contains( "Rgn" ) ) {
											irgn = irgn + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "qcd" )
												|| textChildIII.getNodeName()
														.contains( "QCD" )
												|| textChildIII.getNodeName()
														.contains( "Qcd" ) ) {
											iqcd = iqcd + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "qcc" )
												|| textChildIII.getNodeName()
														.contains( "QCC" )
												|| textChildIII.getNodeName()
														.contains( "Qcc" ) ) {
											iqcc = iqcc + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "poc" )
												|| textChildIII.getNodeName()
														.contains( "POC" )
												|| textChildIII.getNodeName()
														.contains( "Poc" ) ) {
											ipoc = ipoc + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "crg" )
												|| textChildIII.getNodeName()
														.contains( "CRG" )
												|| textChildIII.getNodeName()
														.contains( "Crg" ) ) {
											icrg = icrg + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "com" )
												|| textChildIII.getNodeName()
														.contains( "COM" )
												|| textChildIII.getNodeName()
														.contains( "Com" ) ) {
											icom = icom + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "tile" )
												|| textChildIII.getNodeName()
														.contains( "TILE" )
												|| textChildIII.getNodeName()
														.contains( "Tile" ) ) {
											itile = itile + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "soc" )
												|| textChildIII.getNodeName()
														.contains( "SOC" )
												|| textChildIII.getNodeName()
														.contains( "Soc" ) ) {
											isoc = isoc + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "eoc" )
												|| textChildIII.getNodeName()
														.contains( "EOC" )
												|| textChildIII.getNodeName()
														.contains( "Eoc" ) ) {
											ieoc = ieoc + 1;
										} else if ( textChildIII.getNodeName()
												.contains( "cod" )
												|| textChildIII.getNodeName()
														.contains( "COD" )
												|| textChildIII.getNodeName()
														.contains( "Cod" ) ) {
											icod = icod + 1;
										} else {
											iunknown = iunknown + 1;
											sunknown = sunknown + " "
													+ textChildIII
															.getNodeName();
										}
									}
									continue;
								}

							} else {
								iunknown = iunknown + 1;
								sunknown = sunknown + " "
										+ textChild.getNodeName();
							}

						}
						continue;
					}
					continue;
				}

				if ( isignatureBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_SIGNATURE ) );
				}
				if ( ifileTypeBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_FILETYPE ) );
				}
				if ( ijp2HeaderBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_HEADER ) );
				}
				if ( iimageHeaderBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_IMAGE ) );
				}
				if ( icolourSpecificationBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_COLOUR ) );
				}
				if ( ibitsPerComponentBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_BITSPC ) );
				}
				if ( ipaletteBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_PALETTE ) );
				}
				if ( icomponentMappingBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_MAPPING ) );
				}
				if ( ichannelDefinitionBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_CHANNEL ) );
				}
				if ( iresolutionBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_A_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_A_JP2_RESOLUTION ) );
				}

				if ( ixmlBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_B_JP2_XML ) );
				}
				if ( iuuidInfoBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_B_JP2_UUIDINFO ) );
				}
				if ( iuuidBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_B_JP2_UUID ) );
				}
				if ( iintellectualProperty >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_B_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_B_JP2_INTELLECTUAL ) );
				}

				if ( icontiguousCodestreamBox >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_CODEBOX ) );
				}
				if ( isiz >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_SIZ ) );
				}
				if ( icoc >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_COC ) );
				}
				if ( irgn >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_RGN ) );
				}
				if ( iqcd >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_QCD ) );
				}
				if ( iqcc >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_QCC ) );
				}
				if ( ipoc >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_POC ) );
				}
				if ( icrg >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_CRG ) );
				}
				if ( icom >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_COM ) );
				}
				if ( itile >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_TILE ) );
				}
				if ( isoc >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_SOC ) );
				}
				if ( ieoc >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_EOC ) );
				}
				if ( icod >= 1 ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_C_JP2 )
									+ getTextResourceService().getText( locale,
											ERROR_XML_C_JP2_COD ) );
				}

				if ( iunknown >= 1 ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_D_JP2 )
							+ getTextResourceService().getText( locale,
									ERROR_XML_D_JP2_UNKNOWN, sunknown ) );
				}
			}
			doc = null;

		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_A_JP2 )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		}
		return isValid;
	}
}
