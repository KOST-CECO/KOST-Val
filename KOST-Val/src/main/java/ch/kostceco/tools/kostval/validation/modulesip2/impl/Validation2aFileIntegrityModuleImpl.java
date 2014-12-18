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

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2aFileIntegrityException;
import ch.kostceco.tools.kostval.util.Util;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2aFileIntegrityModule;

/** Validierungsschritt 2a: Sind alle referenzierten Dateien vorhanden? von allen datei nodes den
 * subnode name holen und diesen mit der Struktur vergleichen */

public class Validation2aFileIntegrityModuleImpl extends ValidationModuleImpl implements
		Validation2aFileIntegrityModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation2aFileIntegrityException
	{
		// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
		System.out.print( "2A   " );
		System.out.print( "\r" );

		int onWork = 41;

		Map<String, String> filesInSip = new HashMap<String, String>();
		boolean valid = true;

		try {
			Map<String, File> fileMap = Util.getFileMap( valDatei, false );
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext(); ) {
				String entryName = iterator.next();
				// entryName: content/DOS_02/gpl2.pdf
				filesInSip.put( entryName, entryName );
				if ( onWork == 41 ) {
					onWork = 2;
					System.out.print( "2A-   " );
					System.out.print( "\r" );
				} else if ( onWork == 11 ) {
					onWork = 12;
					System.out.print( "2A\\   " );
					System.out.print( "\r" );
				} else if ( onWork == 21 ) {
					onWork = 22;
					System.out.print( "2A|   " );
					System.out.print( "\r" );
				} else if ( onWork == 31 ) {
					onWork = 32;
					System.out.print( "2A/   " );
					System.out.print( "\r" );
				} else {
					onWork = onWork + 1;
				}
			}

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				// dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( new FileInputStream( new File( valDatei.getAbsolutePath()
						+ "//header//metadata.xml" ) ) );
				doc.getDocumentElement().normalize();
				NodeList nodeLst = doc.getElementsByTagName( "datei" );
				NodeList nodeLstO = doc.getElementsByTagName( "ordner" );

				for ( int s = 0; s < nodeLst.getLength(); s++ ) {
					Node dateiNode = nodeLst.item( s );
					String path = null;
					if ( onWork == 41 ) {
						onWork = 2;
						System.out.print( "2A-   " );
						System.out.print( "\r" );
					} else if ( onWork == 11 ) {
						onWork = 12;
						System.out.print( "2A\\   " );
						System.out.print( "\r" );
					} else if ( onWork == 21 ) {
						onWork = 22;
						System.out.print( "2A|   " );
						System.out.print( "\r" );
					} else if ( onWork == 31 ) {
						onWork = 32;
						System.out.print( "2A/   " );
						System.out.print( "\r" );
					} else {
						onWork = onWork + 1;
					}

					NodeList childNodes = dateiNode.getChildNodes();
					for ( int y = 0; y < childNodes.getLength(); y++ ) {
						Node subNode = childNodes.item( y );
						if ( subNode.getNodeName().equals( "name" ) ) {
							path = subNode.getTextContent();
						}
						if ( onWork == 41 ) {
							onWork = 2;
							System.out.print( "2A-   " );
							System.out.print( "\r" );
						} else if ( onWork == 11 ) {
							onWork = 12;
							System.out.print( "2A\\   " );
							System.out.print( "\r" );
						} else if ( onWork == 21 ) {
							onWork = 22;
							System.out.print( "2A|   " );
							System.out.print( "\r" );
						} else if ( onWork == 31 ) {
							onWork = 32;
							System.out.print( "2A/   " );
							System.out.print( "\r" );
						} else {
							onWork = onWork + 1;
						}
					}

					// selectNodeIterator ist zu Zeitintensiv bei grossen XML-Dateien mit getChildNodes()
					// ersetzt

					/* NodeIterator nl = XPathAPI.selectNodeIterator( dateiNode, "name" ); Node nameNode =
					 * nl.nextNode(); String path = nameNode.getTextContent(); */

					boolean topReached = false;
					while ( !topReached ) {
						Node parentNode = dateiNode.getParentNode();
						if ( parentNode.getNodeName().equals( "inhaltsverzeichnis" ) ) {
							topReached = true;
							break;
						}
						NodeList childrenNodes = parentNode.getChildNodes();
						for ( int x = 0; x < childrenNodes.getLength(); x++ ) {
							Node childNode = childrenNodes.item( x );
							if ( childNode.getNodeName().equals( "name" ) ) {
								path = childNode.getTextContent() + "/" + path;
								if ( dateiNode.getParentNode() != null ) {
									dateiNode = dateiNode.getParentNode();
								}
								break;
							}
						}
						if ( onWork == 41 ) {
							onWork = 2;
							System.out.print( "2A-   " );
							System.out.print( "\r" );
						} else if ( onWork == 11 ) {
							onWork = 12;
							System.out.print( "2A\\   " );
							System.out.print( "\r" );
						} else if ( onWork == 21 ) {
							onWork = 22;
							System.out.print( "2A|   " );
							System.out.print( "\r" );
						} else if ( onWork == 31 ) {
							onWork = 32;
							System.out.print( "2A/   " );
							System.out.print( "\r" );
						} else {
							onWork = onWork + 1;
						}
					}
					String name = path;

					String removedEntry = filesInSip.remove( name );
					if ( removedEntry == null ) {
						// Test von 2A
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Ba_SIP )
										+ getTextResourceService().getText( MESSAGE_XML_BA_FILEMISSING, name ) );
						valid = false;
					}
					path = "";

				}
				// das gleiche mit den Ordnern
				for ( int sO = 0; sO < nodeLstO.getLength(); sO++ ) {
					Node dateiNodeO = nodeLstO.item( sO );

					String pathO = null;

					NodeList childNodesO = dateiNodeO.getChildNodes();
					for ( int y = 0; y < childNodesO.getLength(); y++ ) {
						Node subNodeO = childNodesO.item( y );
						if ( subNodeO.getNodeName().equals( "name" ) ) {
							pathO = subNodeO.getTextContent() + "/";
						}
					}

					// selectNodeIterator ist zu Zeitintensiv bei grossen XML-Dateien mit getChildNodes()
					// ersetzt
					/* NodeIterator nlO = XPathAPI.selectNodeIterator( dateiNodeO, "name" ); Node nameNodeO =
					 * nlO.nextNode(); String pathO = nameNodeO.getTextContent(); */

					boolean topReachedO = false;

					while ( !topReachedO ) {

						Node parentNodeO = dateiNodeO.getParentNode();
						if ( parentNodeO.getNodeName().equals( "inhaltsverzeichnis" ) ) {
							topReachedO = true;
							break;
						}

						NodeList childrenNodesO = parentNodeO.getChildNodes();
						for ( int xO = 0; xO < childrenNodesO.getLength(); xO++ ) {
							Node childNodeO = childrenNodesO.item( xO );

							if ( childNodeO.getNodeName().equals( "name" ) ) {
								pathO = childNodeO.getTextContent() + "/" + pathO;
								if ( dateiNodeO.getParentNode() != null ) {
									dateiNodeO = dateiNodeO.getParentNode();
								}
								break;
							}
						}
						if ( onWork == 41 ) {
							onWork = 2;
							System.out.print( "2A-   " );
							System.out.print( "\r" );
						} else if ( onWork == 11 ) {
							onWork = 12;
							System.out.print( "2A\\   " );
							System.out.print( "\r" );
						} else if ( onWork == 21 ) {
							onWork = 22;
							System.out.print( "2A|   " );
							System.out.print( "\r" );
						} else if ( onWork == 31 ) {
							onWork = 32;
							System.out.print( "2A/   " );
							System.out.print( "\r" );
						} else {
							onWork = onWork + 1;
						}
					}

					String name = pathO;

					@SuppressWarnings("unused")
					String removedEntry = filesInSip.remove( name );
					pathO = "";
				}

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_Ba_SIP )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				valid = false;
			}

			Set<String> filesInSipKeys = filesInSip.keySet();
			for ( Iterator<String> iterator = filesInSipKeys.iterator(); iterator.hasNext(); ) {
				String entryName = iterator.next();
				if ( entryName.startsWith( "header" ) ) {
					// header wird in 2c ignoriert
				} else {
					if ( entryName.endsWith( "/" ) ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Bb_SIP )
										+ getTextResourceService().getText( MESSAGE_XML_BB_FILEMISSINGO, entryName ) );
					} else {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Bb_SIP )
										+ getTextResourceService().getText( MESSAGE_XML_BB_FILEMISSING, entryName ) );
					}
					valid = false;
				}
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ba_SIP )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return valid;

	}

}
