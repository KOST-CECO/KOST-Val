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

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.kostceco.tools.kostval.exception.modulesip1.Validation1cNamingException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1cNamingModule;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * Diverse Validierungen zu den Namen der Files und Ordner, erlaubte Längen,
 * verwendete Zeichen usw.
 * 
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public class Validation1cNamingModuleImpl extends ValidationModuleImpl
		implements Validation1cNamingModule
{

	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(
			ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation1cNamingException
	{

		boolean valid = true;

		String fileName = valDatei.getName();
		Integer version = (0);

		// I.) Validierung der Namen aller Dateien: sind die enthaltenen Zeichen
		// alle erlaubt?

		String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~a-zA-Z0-9 ]";
		Pattern pattern = Pattern.compile( patternStr );

		try {
			Zip64File zipfile2 = new Zip64File( valDatei );
			List<FileEntry> fileEntryList2 = zipfile2.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList2 ) {

				String name = fileEntry.getName();

				String[] pathElements = name.split( "/" );
				for ( int i = 0; i < pathElements.length; i++ ) {
					String element = pathElements[i];

					Matcher matcher = pattern.matcher( element );

					boolean matchFound = matcher.find();
					if ( matchFound ) {
						getMessageService()
								.logError(
										getTextResourceService().getText(
												MESSAGE_XML_MODUL_Ac_SIP )
												+ getTextResourceService()
														.getText(
																MESSAGE_XML_AC_INVALIDCHARACTERS,
																element ) );
						return false;
					}
				}

				zipfile2.close();

			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		}

		// II.) Validierung des Formats des Dateinamen

		patternStr = getConfigurationService().getAllowedSipName();
		Pattern p = Pattern.compile( patternStr );
		Matcher matcher = p.matcher( fileName );

		boolean matchFound = matcher.find();
		if ( !matchFound ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText(
									MESSAGE_XML_AC_INVALIDFILENAME ) );
			return false;
		}

		// III.) Validierung der beiden Second-Level-Ordner im Toplevel-Ordner,
		// es müssen genau header/ und content/ vorhanden sein
		// und nichts anderes.

		List<String> listOfFolders = new ArrayList<String>();

		try {

			String toplevelDir = valDatei.getName();
			int lastDotIdx = toplevelDir.lastIndexOf( "." );
			toplevelDir = toplevelDir.substring( 0, lastDotIdx );

			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {

				String name = fileEntry.getName();

				if ( (((name.equals( "header/" ) || name.equals( toplevelDir
						+ "/" + "header/" )) && fileEntry.isDirectory()))
						|| (((name.equals( "content/" ) || name
								.equals( toplevelDir + "/" + "content/" )) && fileEntry
								.isDirectory())) ) {

					listOfFolders.add( name );
				}

				if ( (name.startsWith( "header/" ) || name
						.startsWith( toplevelDir + "/" + "header/" ))
						&& !name.endsWith( ".xsd" ) ) {
					if ( !(name.endsWith( "header/metadata.xml" )
							|| name.endsWith( "header/xsd/" ) || name
							.endsWith( "header/" )) ) {
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Ac_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_AC_NOTALLOWEDFILE,
												name ) );

						return false;

					}

				}

				// alle file namen müssen mit content oder header anfangen
				if ( !(name.startsWith( toplevelDir )
						|| name.startsWith( "content/" )
						|| name.startsWith( "header/" )
						|| name.startsWith( toplevelDir + "/" + "content/" ) || name
						.startsWith( toplevelDir + "/" + "header/" )) ) {

					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ac_SIP )
											+ getTextResourceService()
													.getText(
															MESSAGE_XML_AC_NOTALLOWEDFILE,
															name ) );

					return false;
				}

			}
			zipfile.close();

			if ( listOfFolders.size() != 2 ) {
				return false;
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );

			return false;

		}

		// IV.+V.) Validierung des header Ordners: es dürfen sich nur ein
		// weiterer folder namens xsd und eine file namens
		// metadata.xml darin befinden. Im xsd Folder wiederum dürfen sich nur
		// eine Reihe *.xsd files, welche sich je nach SIP-Version
		// unterscheiden.

		Integer allowedV1 = getConfigurationService().getAllowedVersionBar1();
		Integer allowedV4 = getConfigurationService()
				.getAllowedVersionBar4Ech1();
		// generiert eine Map mit den xsd-files und Ordnern, welche in
		// header/xsd/ enthalten sein müssen
		Map<String, String> allowedXsdFiles = new HashMap<String, String>();
		allowedXsdFiles.put( "header/", "header/" );
		allowedXsdFiles.put( "header/xsd/", "header/xsd/" );
		allowedXsdFiles.put( "header/metadata.xml", "header/metadata.xml/" );

		try {
			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {

				String name = fileEntry.getName();

				if ( name.endsWith( XSD_ARELDA ) ) {
					// dann handelt es sich um die Version BAR1
					version = 1;
					if ( allowedV1 == 1 ) {
						allowedXsdFiles.put( "header/xsd/ablieferung.xsd",
								"header/xsd/ablieferung.xsd/" );
						allowedXsdFiles.put( "header/xsd/archivischeNotiz.xsd",
								"header/xsd/archivischeNotiz.xsd/" );
						allowedXsdFiles.put(
								"header/xsd/archivischerVorgang.xsd",
								"header/xsd/archivischerVorgang.xsd/" );
						allowedXsdFiles.put( "header/xsd/arelda_v3.13.2.xsd",
								"header/xsd/arelda_v3.13.2.xsd/" );
						allowedXsdFiles.put( "header/xsd/base.xsd",
								"header/xsd/base.xsd/" );
						allowedXsdFiles.put( "header/xsd/datei.xsd",
								"header/xsd/datei.xsd/" );
						allowedXsdFiles.put( "header/xsd/dokument.xsd",
								"header/xsd/dokument.xsd/" );
						allowedXsdFiles.put( "header/xsd/dossier.xsd",
								"header/xsd/dossier.xsd/" );
						allowedXsdFiles.put( "header/xsd/ordner.xsd",
								"header/xsd/ordner.xsd/" );
						allowedXsdFiles.put( "header/xsd/ordnungssystem.xsd",
								"header/xsd/ordnungssystem.xsd/" );
						allowedXsdFiles.put(
								"header/xsd/ordnungssystemposition.xsd",
								"header/xsd/ordnungssystemposition.xsd/" );
						allowedXsdFiles.put( "header/xsd/paket.xsd",
								"header/xsd/paket.xsd/" );
						allowedXsdFiles.put( "header/xsd/provenienz.xsd",
								"header/xsd/provenienz.xsd/" );
					} else {
						// Version 1 ist nicht erlaubt
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Ac_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_AC_NOTALLOWEDV,
												version ) );
						valid = false;

					}
				}
				if ( name.endsWith( "arelda.xsd" ) ) {
					// dann handelt es sich um die Version BAR4 respektive eCH1
					version = 4;
					if ( allowedV4 == 1 ) {

						allowedXsdFiles.put( "header/xsd/ablieferung.xsd",
								"header/xsd/ablieferung.xsd/" );
						allowedXsdFiles.put( "header/xsd/archivischeNotiz.xsd",
								"header/xsd/archivischeNotiz.xsd/" );
						allowedXsdFiles.put(
								"header/xsd/archivischerVorgang.xsd",
								"header/xsd/archivischerVorgang.xsd/" );
						allowedXsdFiles.put( "header/xsd/arelda.xsd",
								"header/xsd/arelda.xsd/" );
						allowedXsdFiles.put( "header/xsd/base.xsd",
								"header/xsd/base.xsd/" );
						allowedXsdFiles.put( "header/xsd/datei.xsd",
								"header/xsd/datei.xsd/" );
						allowedXsdFiles.put( "header/xsd/dokument.xsd",
								"header/xsd/dokument.xsd/" );
						allowedXsdFiles.put( "header/xsd/dossier.xsd",
								"header/xsd/dossier.xsd/" );
						allowedXsdFiles.put( "header/xsd/ordner.xsd",
								"header/xsd/ordner.xsd/" );
						allowedXsdFiles.put( "header/xsd/ordnungssystem.xsd",
								"header/xsd/ordnungssystem.xsd/" );
						allowedXsdFiles.put(
								"header/xsd/ordnungssystemposition.xsd",
								"header/xsd/ordnungssystemposition.xsd/" );
						allowedXsdFiles.put( "header/xsd/paket.xsd",
								"header/xsd/paket.xsd/" );
						allowedXsdFiles.put( "header/xsd/provenienz.xsd",
								"header/xsd/provenienz.xsd/" );
						allowedXsdFiles.put( "header/xsd/zusatzDaten.xsd",
								"header/xsd/zusatzDaten.xsd/" );
					} else {
						// Version 4 ist nicht erlaubt
						getMessageService().logError(
								getTextResourceService().getText(
										MESSAGE_XML_MODUL_Ac_SIP )
										+ getTextResourceService().getText(
												MESSAGE_XML_AC_NOTALLOWEDV,
												version ) );
						valid = false;

					}

				}
			}

			if ( valid != false ) {
				// Dieser Schritt wird nur durchgeführt, wenn die verwendete
				// Version erlaubt ist
				for ( FileEntry fileEntry : fileEntryList ) {

					String name = fileEntry.getName();

					// wenn das SIP-Archiv einen Top-Level-Folder enthält, der
					// gleich heisst wie das SIP-Archiv,
					// müssen die Pfade entsprechend korrigiert werden
					String toplevelDir = valDatei.getName();
					int lastDotIdx = toplevelDir.lastIndexOf( "." );
					toplevelDir = toplevelDir.substring( 0, lastDotIdx );

					if ( name.startsWith( toplevelDir ) ) {
						String folderToSubtract = toplevelDir + "/";
						name = name.replace( folderToSubtract, "" );
					}

					if ( name.startsWith( "header/" ) ) {
						String removedEntry = allowedXsdFiles.remove( name );
						if ( removedEntry == null ) {
							getMessageService()
									.logError(
											getTextResourceService().getText(
													MESSAGE_XML_MODUL_Ac_SIP )
													+ getTextResourceService()
															.getText(
																	MESSAGE_XML_AC_NOTALLOWEDFILE,
																	name ) );
							valid = false;
						}
					}
				}
				zipfile.close();

				Set<String> keys = allowedXsdFiles.keySet();
				for ( Iterator<String> iterator = keys.iterator(); iterator
						.hasNext(); ) {
					String string = iterator.next();
					getMessageService()
							.logError(
									getTextResourceService().getText(
											MESSAGE_XML_MODUL_Ac_SIP )
											+ getTextResourceService().getText(
													MESSAGE_XML_AC_MISSINGFILE,
													string ) );
					valid = false;
				}
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );

			return false;
		}

		// VI.+ VII) Länge der Pfade (< 180) und File/Ordnernamen (<41)

		Integer maxPathLength = getConfigurationService()
				.getMaximumPathLength();
		Integer maxFileLength = getConfigurationService()
				.getMaximumFileLength();
		try {
			Zip64File zipfile = new Zip64File( valDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {

				String name = valDatei.getName() + "/" + fileEntry.getName();

				if ( name.length() > maxPathLength.intValue() ) {
					getMessageService().logError(
							getTextResourceService().getText(
									MESSAGE_XML_MODUL_Ac_SIP )
									+ getTextResourceService().getText(
											MESSAGE_XML_AC_PATHTOOLONG, name ) );
					valid = false;
					break;
				}

				String[] pathElements = name.split( "/" );
				if ( version == 1 ) {
					// Einschränkung der Namenslänge muss geprüft werden
					for ( int i = 0; i < pathElements.length; i++ ) {
						String pathElement = pathElements[i];
						if ( pathElement.length() > maxFileLength.intValue() ) {
							getMessageService()
									.logError(
											getTextResourceService().getText(
													MESSAGE_XML_MODUL_Ac_SIP )
													+ getTextResourceService()
															.getText(
																	MESSAGE_XML_AC_FILENAMETOOLONG,
																	pathElement ) );
							valid = false;
						}
					}
				}

			}
			zipfile.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText(
									ERROR_XML_UNKNOWN, e.getMessage() ) );

			return false;

		}

		return valid;

	}

	/*
	 * public static void main( String[] args ) {
	 * 
	 * File valDatei = new File(
	 * "C:\\ludin\\A6Z-SIP-Validator\\SIP-Beispiele etc\\SIP_20101018_RIS_4.zip"
	 * );
	 * 
	 * Validation1cNamingModuleImpl module = new Validation1cNamingModuleImpl();
	 * try { module.validate( valDatei, directoryOfLogfile ); } catch (
	 * Validation1cNamingException e ) { e.printStackTrace(); } }
	 */

}
