/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
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

package ch.kostceco.tools.kostval.validation.modulesip1.impl;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1cNamingException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1cNamingModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Diverse Validierungen zu den Namen der Files und Ordner, erlaubte Laengen,
 * verwendete Zeichen usw.
 */
public class Validation1cNamingModuleImpl extends ValidationModuleImpl
		implements Validation1cNamingModule
{

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile )
			throws Validation1cNamingException
	{
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get( "ShowProgressOnWork" );
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if ( onWorkConfig.equals( "yes" ) ) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print( "1C   " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean valid = true;
		boolean charIo = true;
		boolean sipIo = true;
		boolean tlIo = true;
		boolean xsdIo = true;
		boolean lengthIo = true;

		String fileName = valDatei.getName();

		// I.) Validierung der Namen aller Dateien: sind die enthaltenen Zeichen
		// alle erlaubt?
		String patternStr = "[^!#\\$%\\(\\)\\+,\\-_\\.=@\\[\\]\\{\\}\\~a-zA-Z0-9 ]";
		Pattern pattern = Pattern.compile( patternStr );

		try {
			String entryNameI = valDatei.getName();
			String nameI = entryNameI;

			String[] pathElementsI = nameI.split( "/" );
			for ( int i = 0; i < pathElementsI.length; i++ ) {
				String elementI = pathElementsI[i];

				Matcher matcherI = pattern.matcher( elementI );

				boolean matchFoundI = matcherI.find();
				if ( matchFoundI ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ac_SIP )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AC_INVALIDCHARACTERS,
											entryNameI, matcherI.group( i ) ) );
					charIo = false;
				}
			}
			Map<String, File> fileMap = Util.getFileMap( valDatei, true );
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
					.hasNext(); ) {
				String entryName = iterator.next();
				// entryName: content/DOS_02/gpl2.pdf name: gpl2.pdf
				File newFile = fileMap.get( entryName );
				String name = newFile.getName();

				String[] pathElements = name.split( "/" );
				for ( int i = 0; i < pathElements.length; i++ ) {
					String element = pathElements[i];

					Matcher matcher = pattern.matcher( element );

					boolean matchFound = matcher.find();
					if ( matchFound ) {

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Ac_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_AC_INVALIDCHARACTERS,
										element, matcher.group( i ) ) );
						charIo = false;
					}
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "1C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "1C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "1C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "1C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
		}

		// II.) Validierung des Formats des Dateinamen
		patternStr = configMap.get( "AllowedSipName" );
		Pattern p = Pattern.compile( patternStr );
		Matcher matcher = p.matcher( fileName );

		boolean matchFound = matcher.find();
		if ( !matchFound ) {

			Logtxt.logtxt( logFile, getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_Ac_SIP )
					+ getTextResourceService().getText( locale,
							MESSAGE_XML_AC_INVALIDFILENAME, patternStr ) );
			sipIo = false;
		}

		// III.) Validierung der beiden Second-Level-Ordner im Toplevel-Ordner,
		// es müssen genau header/
		// und content/ vorhanden sein und nichts anderes.
		try {
			File dir = valDatei;
			// Liste mit den File objects
			File[] files = dir.listFiles();

			for ( File file : files ) {
				String name = file.getName();
				// toplevel ordner nur content oder header ist erlaubt
				if ( !(name.equals( "content" ) || name.equals( "header" )) ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_AC_NOTALLOWEDFILE, name ) );
					tlIo = false;
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "1C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "1C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "1C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "1C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// IV.) Validierung des header-Ordner, es müssen genau xsd/ und
		// metadata.xml vorhanden sein und
		// nichts anderes.
		try {
			File dir = new File(
					valDatei.getAbsolutePath() + File.separator + "header" );
			// Liste mit den File objects
			File[] files = dir.listFiles();

			for ( File file : files ) {
				String name = file.getName();
				// header ordner nur metadata.xml oder xsd ist erlaubt
				if ( !(name.equals( "metadata.xml" )
						|| name.equals( "xsd" )) ) {

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ac_SIP )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AC_NOTALLOWEDFILE,
											"header/" + name ) );
					tlIo = false;
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "1C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "1C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "1C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "1C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// V.) Im xsd Folder wiederum duerfen sich nur eine Reihe *.xsd files
		// sein
		// generiert eine Map mit den xsd-files und Ordnern, welche in
		// header/xsd/ enthalten sein
		// muessen
		Map<String, String> allowedXsdFiles = new HashMap<String, String>();

		try {
			File xsd = new File( valDatei.getAbsolutePath() + File.separator
					+ "header" + File.separator + "xsd" );
			// Liste mit den File objects in xsd
			File[] filesXsd = xsd.listFiles();

			for ( File fileXsd : filesXsd ) {
				String name = fileXsd.getName();

				if ( name.startsWith( "arel" ) ) {
					// dann handelt es sich um eCH-160_v1 oder eCH-160_v1.1
					allowedXsdFiles.put( "ablieferung.xsd", "ablieferung.xsd" );
					allowedXsdFiles.put( "archivischeNotiz.xsd",
							"archivischeNotiz.xsd" );
					allowedXsdFiles.put( "archivischerVorgang.xsd",
							"archivischerVorgang.xsd" );
					allowedXsdFiles.put( "arelda.xsd", "arelda.xsd" );
					allowedXsdFiles.put( "base.xsd", "base.xsd" );
					allowedXsdFiles.put( "datei.xsd", "datei.xsd" );
					allowedXsdFiles.put( "dokument.xsd", "dokument.xsd" );
					allowedXsdFiles.put( "dossier.xsd", "dossier.xsd" );
					allowedXsdFiles.put( "ordner.xsd", "ordner.xsd" );
					allowedXsdFiles.put( "ordnungssystem.xsd",
							"ordnungssystem.xsd" );
					allowedXsdFiles.put( "ordnungssystemposition.xsd",
							"ordnungssystemposition.xsd" );
					allowedXsdFiles.put( "paket.xsd", "paket.xsd" );
					allowedXsdFiles.put( "provenienz.xsd", "provenienz.xsd" );
					allowedXsdFiles.put( "zusatzDaten.xsd", "zusatzDaten.xsd" );
				}
				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "1C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "1C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "1C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "1C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}

			if ( valid != false ) {
				// Dieser Schritt wird nur durchgefuehrt, wenn die verwendete
				// Version erlaubt ist

				// Liste mit den File objects in xsd
				File[] filesXsd2 = xsd.listFiles();

				for ( File fileXsd2 : filesXsd2 ) {
					String name = fileXsd2.getName();

					String removedEntry = allowedXsdFiles.remove( name );
					if ( removedEntry == null ) {

						Logtxt.logtxt( logFile, getTextResourceService()
								.getText( locale, MESSAGE_XML_MODUL_Ac_SIP )
								+ getTextResourceService().getText( locale,
										MESSAGE_XML_AC_NOTALLOWEDFILE,
										"header/xsd/" + name ) );
						valid = false;
					}
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "1C-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "1C\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "1C|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "1C/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}

				Set<String> keys = allowedXsdFiles.keySet();
				for ( Iterator<String> iterator = keys.iterator(); iterator
						.hasNext(); ) {
					String string = iterator.next();

					Logtxt.logtxt( logFile,
							getTextResourceService().getText( locale,
									MESSAGE_XML_MODUL_Ac_SIP )
									+ getTextResourceService().getText( locale,
											MESSAGE_XML_AC_MISSINGFILE,
											"header/xsd/" + string ) );
					valid = false;
					if ( showOnWork ) {
						if ( onWork == 410 ) {
							onWork = 2;
							System.out.print( "1C-  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 110 ) {
							onWork = onWork + 1;
							System.out.print( "1C\\  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 210 ) {
							onWork = onWork + 1;
							System.out.print( "1C|  " );
							System.out.print( "\b\b\b\b\b" );
						} else if ( onWork == 310 ) {
							onWork = onWork + 1;
							System.out.print( "1C/  " );
							System.out.print( "\b\b\b\b\b" );
						} else {
							onWork = onWork + 1;
						}
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );

			return false;
		}

		// VI.+ VII) Laenge der Pfade (< 180)
		String maxPathLengthString = configMap.get( "MaximumPathLength" );
		Integer maxPathLength = Integer.parseInt( maxPathLengthString );
		try {
			Map<String, File> fileMap = Util.getFileMap( valDatei, true );
			Set<String> fileMapKeys = fileMap.keySet();
			for ( Iterator<String> iterator = fileMapKeys.iterator(); iterator
					.hasNext(); ) {
				String entryName = iterator.next();
				// entryName: content/DOS_02/gpl2.pdf name:
				// SIP_20110310_KOST_1a-3d-IO/content/DOS_02/gpl2.pdf
				String name = valDatei.getName() + "/" + entryName;

				if ( name.length() > maxPathLength.intValue() ) {

					Logtxt.logtxt( logFile, getTextResourceService()
							.getText( locale, MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									MESSAGE_XML_AC_PATHTOOLONG, name ) );
					lengthIo = false;
				}

				if ( showOnWork ) {
					if ( onWork == 410 ) {
						onWork = 2;
						System.out.print( "1C-  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 110 ) {
						onWork = onWork + 1;
						System.out.print( "1C\\  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 210 ) {
						onWork = onWork + 1;
						System.out.print( "1C|  " );
						System.out.print( "\b\b\b\b\b" );
					} else if ( onWork == 310 ) {
						onWork = onWork + 1;
						System.out.print( "1C/  " );
						System.out.print( "\b\b\b\b\b" );
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch ( Exception e ) {

			Logtxt.logtxt( logFile,
					getTextResourceService().getText( locale,
							MESSAGE_XML_MODUL_Ac_SIP )
							+ getTextResourceService().getText( locale,
									ERROR_XML_UNKNOWN, e.getMessage() ) );

			return false;

		}

		if ( charIo == false || sipIo == false || tlIo == false
				|| xsdIo == false || valid == false || lengthIo == false ) {
			return false;
		} else {
			return true;
		}
	}
}
