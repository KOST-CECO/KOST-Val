/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2019 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.siardexcerpt.service.impl.ConfigurationServiceExcImpl;
import ch.kostceco.tools.siardexcerpt.logging.Logger;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationServiceExc;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

public class ConfigurationServiceExcImpl implements ConfigurationServiceExc
{

	private static final Logger	LOGGER		= new Logger( ConfigurationServiceExcImpl.class );
	Map<String, String>					configMap	= null;
	private TextResourceServiceExc	textResourceServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc()
	{
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc( TextResourceServiceExc textResourceServiceExc )
	{
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public Map<String, String> configMap()
	{
		try {
			File directoryOfConfigfile = new File( System.getenv( "USERPROFILE" ) + File.separator
					+ ".siardexcerpt" + File.separator + "configuration" );
			File configFile = new File( directoryOfConfigfile + File.separator + "siardexcerpt.conf.xml" );
			Document doc = null;

			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( configFile ) );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse( bis );
			doc.normalize();

			Map<String, String> configMap = new HashMap<String, String>();
			// ------------------------ Allgemeines ------------------------

			/** Gibt den Pfad des Arbeitsverzeichnisses zurueck. =
			 * USERPROFILE/.siardexcerpt/temp_SIARDexcerpt **/
			String pathtoworkdir = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
					+ File.separator + "temp_SIARDexcerpt";
			File dir = new File( pathtoworkdir );
			if ( !dir.exists() ) {
				dir.mkdirs();
			}
			configMap.put( "PathToWorkDir", pathtoworkdir );

			/** Gibt den Pfad des Output-verzeichnisses zurueck. = USERPROFILE/.siardexcerpt/Output **/
			String logs = System.getenv( "USERPROFILE" ) + File.separator + ".siardexcerpt"
					+ File.separator + "Output";
			File dir1 = new File( logs );
			if ( !dir1.exists() ) {
				dir1.mkdirs();
			}
			configMap.put( "PathToOutput", logs );

			String mfolder = "(..)";
			String mname = "(..)";
			String mtitle = "(..)";
			String mpkname = "(..)";
			String mpkcell = "(..)";
			String mschemaname = "(..)";
			String mschemafolder = "(..)";
			String mcIname = "(..)";
			String mcInumber = "(..)";
			String stJkeyname = "(..)";
			String stJname = "(..)";
			String stJfolder = "(..)";
			String stJfkcell = "(..)";
			String stJschemafolder = "(..)";
			String stJschemaname = "(..)";

			try {
				/** Gibt den Pfad des XSL zurueck. **/
				String pathToXSL = doc.getElementsByTagName( "pathtoxsl" ).item( 0 ).getTextContent();
				configMap.put( "PathToXSL", pathToXSL );
			} catch ( NullPointerException e ) {
				configMap.put( "PathToXSL", "(..)" );
			}

			try {
				/** Gibt den Pfad des XSL search zurueck. **/
				String pathToXSLsearch = doc.getElementsByTagName( "pathtoxslsearch" ).item( 0 )
						.getTextContent();
				configMap.put( "PathToXSLsearch", pathToXSLsearch );
			} catch ( NullPointerException e ) {
				configMap.put( "PathToXSLsearch", "resources" + File.separator + "SIARDexcerptSearch.xsl" );
			}

			try {
				/** Gibt den Namen des Archivs zurueck. **/
				String archive = doc.getElementsByTagName( "archive" ).item( 0 ).getTextContent();
				configMap.put( "Archive", archive );
			} catch ( NullPointerException e ) {
				configMap.put( "Archive", "Archiv" );
			}

			try {
				/** Gibt an ob die Suche case sensitive (Gross- und Kleinschreibung werden beruecksichtigt)
				 * oder insensitive (Gross- und Kleinschreibung werden ignoriert) sein soll. */
				String insensitive = doc.getElementsByTagName( "insensitive" ).item( 0 ).getTextContent();
				configMap.put( "Insensitive", insensitive );
			} catch ( NullPointerException e ) {
				configMap.put( "Insensitive", "yes" );
			}

			try {
				/** Gibt an ob sed verwendet werden soll oder nicht. */
				String sed = doc.getElementsByTagName( "sed" ).item( 0 ).getTextContent();
				configMap.put( "Sed", sed );
			} catch ( NullPointerException e ) {
				configMap.put( "Sed", "no" );
			}

			// ------------------------ Extraktion ------------------------
			try {
				/** Gibt den Namen des Archivs zurueck. **/
				mfolder = doc.getElementsByTagName( "mfolder" ).item( 0 ).getTextContent();
				configMap.put( "MaintableFolder", mfolder );
			} catch ( NullPointerException e ) {
				configMap.put( "MaintableFolder", "(..)" );
			}

			try {
				/** Gibt den Namen der Haupttabelle zurueck. */
				mname = doc.getElementsByTagName( "mname" ).item( 0 ).getTextContent();
				configMap.put( "MaintableName", mname );
			} catch ( NullPointerException e ) {
				configMap.put( "MaintableName", "(..)" );
			}

			try {
				/** Gibt den Titel der Suche zurueck. */
				mtitle = doc.getElementsByTagName( "mtitle" ).item( 0 ).getTextContent();
				configMap.put( "SearchtableTitle", mtitle );
			} catch ( NullPointerException e ) {
				configMap.put( "SearchtableTitle", "(..)" );
			}

			try {
				/** Gibt den Namen des Primärschluessel der Haupttabelle zurueck. */
				mpkname = doc.getElementsByTagName( "mpkname" ).item( 0 ).getTextContent();
				configMap.put( "MaintablePrimarykeyName", mpkname );
			} catch ( NullPointerException e ) {
				configMap.put( "MaintablePrimarykeyName", "(..)" );
			}

			try {
				/** Gibt die Zelle des Primärschluessel der Haupttabelle zurueck. */
				mpkcell = doc.getElementsByTagName( "mpkcell" ).item( 0 ).getTextContent();
				configMap.put( "MaintablePrimarykeyCell", mpkcell );
			} catch ( NullPointerException e ) {
				configMap.put( "MaintablePrimarykeyCell", "(..)" );
			}

			try {
				/** Gibt den SchemaNamen des Archivs zurueck. **/
				mschemaname = doc.getElementsByTagName( "mschemaname" ).item( 0 ).getTextContent();
				configMap.put( "MschemaName", mschemaname );
			} catch ( NullPointerException e ) {
				configMap.put( "MschemaName", "(..)" );
			}

			try {
				/** Gibt den SchemaNamen der Haupttabelle zurueck. */
				mschemafolder = doc.getElementsByTagName( "mschemafolder" ).item( 0 ).getTextContent();
				configMap.put( "MschemaFolder", mschemafolder );
			} catch ( NullPointerException e ) {
				configMap.put( "MschemaFolder", "(..)" );
			}
			// ------------------------ mc1 - mc11 ------------------------
			for ( int i = 1; i < 12; i++ ) {
				try {
					/** Namen den Namen der Suchzelle zurueck. */
					mcIname = doc.getElementsByTagName( "mc" + i + "name" ).item( 0 ).getTextContent();
					configMap.put( "CellName" + i, mcIname );
				} catch ( NullPointerException e ) {
					configMap.put( "CellName" + i, "(..)" );
				}
				try {
					/** Gibt die Nummer der Suchzelle zurueck. */
					mcInumber = doc.getElementsByTagName( "mc" + i + "number" ).item( 0 ).getTextContent();
					configMap.put( "CellNumber" + i, mcInumber );
				} catch ( NullPointerException e ) {
					configMap.put( "CellNumber" + i, "(..)" );
				}
			}

			// ------------------------ Subtable st1 - st20 ------------------------
			for ( int j = 1; j < 21; j++ ) {
				try {
					/** Namen des Schluessel zurueck. */
					stJkeyname = doc.getElementsByTagName( "st" + j + "keyname" ).item( 0 ).getTextContent();
					configMap.put( "st" + j + "Keyname", stJkeyname );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "Keyname", "(..)" );
				}
				try {
					/** Gibt den Ordner der Subtabelle zurueck. */
					stJfolder = doc.getElementsByTagName( "st" + j + "folder" ).item( 0 ).getTextContent();
					configMap.put( "st" + j + "Folder", stJfolder );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "Folder", "(..)" );
				}
				try {
					/** Gibt den Namen der Subtabelle zurueck. */
					stJname = doc.getElementsByTagName( "st" + j + "name" ).item( 0 ).getTextContent();
					configMap.put( "st" + j + "Name", stJname );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "Name", "(..)" );
				}
				try {
					/** Gibt die Zelle des Fremdschluessel der Subtabelle zurueck. */
					stJfkcell = doc.getElementsByTagName( "st" + j + "fkcell" ).item( 0 ).getTextContent();
					configMap.put( "st" + j + "Fkcell", stJfkcell );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "Fkcell", "(..)" );
				}
				try {
					/** Gibt den Namen der Subtabelle zurueck. */
					stJschemafolder = doc.getElementsByTagName( "st" + j + "schemafolder" ).item( 0 )
							.getTextContent();
					configMap.put( "st" + j + "SchemaFolder", stJschemafolder );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "SchemaFolder", "(..)" );
				}
				try {
					stJschemaname = doc.getElementsByTagName( "st" + j + "schemaname" ).item( 0 )
							.getTextContent();
					configMap.put( "st" + j + "SchemaName", stJschemaname );
				} catch ( NullPointerException e ) {
					configMap.put( "st" + j + "SchemaName", "(..)" );
				}
			}

			// System.out.println("Value is b : " + (map.get("key") == b));
			bis.close();
			return configMap;

		} catch ( FileNotFoundException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN, e ) );
			System.exit( 1 );
		} catch ( ParserConfigurationException e ) {
			String error = e.getMessage() + " (ParserConfigurationException)";
			LOGGER.logError( getTextResourceServiceExc().getText( MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		} catch ( SAXException e ) {
			String error = e.getMessage() + " (SAXException)";
			LOGGER.logError( getTextResourceServiceExc().getText( MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		} catch ( IOException e ) {
			String error = e.getMessage() + " (IOException)";
			LOGGER.logError( getTextResourceServiceExc().getText( MESSAGE_XML_MODUL_A )
					+ getTextResourceServiceExc().getText( ERROR_XML_UNKNOWN, error ) );
			System.exit( 1 );
		}
		return configMap;
	}
}
