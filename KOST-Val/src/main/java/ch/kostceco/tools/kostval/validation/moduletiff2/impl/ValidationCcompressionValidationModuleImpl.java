﻿/* == KOST-Val ==================================================================================
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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationCcompressionValidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationCcompressionValidationModule;

/**
 * Validierungsschritt C (Komprimierung-Validierung) Ist die TIFF-Datei gemäss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationCcompressionValidationModuleImpl extends ValidationModuleImpl
		implements ValidationCcompressionValidationModule {

	public static String NEWLINE = System.getProperty("line.separator");

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationCcompressionValidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}
		String pathToWorkDir = configMap.get("PathToWorkDir");
		File workDir = new File(pathToWorkDir);
		if (!workDir.exists()) {
			workDir.mkdir();
		}

		boolean isValid = true;

		// Informationen zum Logverzeichnis holen
		String pathToExiftoolOutput = directoryOfLogfile.getAbsolutePath();
		File exiftoolReport = new File(pathToExiftoolOutput, valDatei.getName() + ".exiftool-log.txt");
		pathToExiftoolOutput = exiftoolReport.getAbsolutePath();

		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */

		String com1 = configMap.get("AllowedCompression1");
		String com2 = configMap.get("AllowedCompression2");
		String com3 = configMap.get("AllowedCompression3");
		String com4 = configMap.get("AllowedCompression4");
		String com5 = configMap.get("AllowedCompression5");
		String com7 = configMap.get("AllowedCompression7");
		String com8 = configMap.get("AllowedCompression8");
		String com32773 = configMap.get("AllowedCompression32773");

		if (com1.equals("")) {
			com1 = "DieseKompressionIstNichtErlaubt";
		}
		if (com2.equals("")) {
			com2 = "DieseKompressionIstNichtErlaubt";
		}
		if (com3.equals("")) {
			com3 = "DieseKompressionIstNichtErlaubt";
		}
		if (com4.equals("")) {
			com4 = "DieseKompressionIstNichtErlaubt";
		}
		if (com5.equals("")) {
			com5 = "DieseKompressionIstNichtErlaubt";
		}
		if (com7.equals("")) {
			com7 = "DieseKompressionIstNichtErlaubt";
		}
		if (com8.equals("")) {
			com8 = "DieseKompressionIstNichtErlaubt";
		}
		if (com32773.equals("")) {
			com32773 = "DieseKompressionIstNichtErlaubt";
		}

		Integer jhoveio = 0;
		String oldErrorLine1 = "";
		String oldErrorLine2 = "";
		String oldErrorLine3 = "";
		String oldErrorLine4 = "";
		String oldErrorLine5 = "";

		/*
		 * TODO: jhoveReport auswerten! Auf Exiftool wird verzichtet. Exiftool verwendet
		 * Perl, welche seit einiger Zeit hohe nicht geloese Sicherheitsrisiken birgt.
		 * zudem koennen die Metadaten vermehrt komplett durch jhove ausgelesen werden.
		 * Jhove hat bereits einen der Probleme, welche das teilweise die Ausgabe der
		 * Metadaten verhindert behoben.
		 */
		File jhoveReport = new File(directoryOfLogfile, valDatei.getName() + ".jhove-log.txt");

		// existiert der jhoveReport?
		if (!jhoveReport.exists()) {
			isValid = false;
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_TIFF)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "No Jhove report."));
				return false;
			}
		} else {
			Boolean tiffLine = false;
			try {
				BufferedReader in = new BufferedReader(new FileReader(jhoveReport));
				String line;
				while ((line = in.readLine()) != null) {
					/*
					 * zu analysierende TIFF-IFD-Zeile die CompressionScheme-Zeile enthält einer
					 * dieser Freitexte der Komprimierungsart
					 */

					if (line.contains(" Type: TIFF")) {
						// System.out.println( "Line=" + line );
						tiffLine = true;
					} else if (line.contains(" Type: ")) {
						// System.out.println( "Line=" + line );
						tiffLine = false;
					}

					if (line.contains(" CompressionScheme: ") && tiffLine) {
						jhoveio = 1;
						if (line.contains(" CompressionScheme: " + com1) || line.contains(" CompressionScheme: " + com2)
								|| line.contains(" CompressionScheme: " + com3)
								|| line.contains(" CompressionScheme: " + com4)
								|| line.contains(" CompressionScheme: " + com5)
								|| line.contains(" CompressionScheme: " + com7)
								|| line.contains(" CompressionScheme: " + com8)
								|| line.contains(" CompressionScheme: " + com32773)) {
							// Valider Status
						} else {
							// Invalider Status
							isValid = false;
							if (min) {
								in.close();
								return false;
							} else {
								if (!line.equals(oldErrorLine1) && !line.equals(oldErrorLine2)
										&& !line.equals(oldErrorLine3) && !line.equals(oldErrorLine4)
										&& !line.equals(oldErrorLine5)) {
									// neuer Fehler
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_TIFF)
													+ getTextResourceService().getText(locale, MESSAGE_XML_CG_INVALID,
															line));
									if (oldErrorLine1.equals("")) {
										oldErrorLine1 = line;
									} else if (oldErrorLine2.equals("")) {
										oldErrorLine2 = line;
									} else if (oldErrorLine3.equals("")) {
										oldErrorLine3 = line;
									} else if (oldErrorLine4.equals("")) {
										oldErrorLine4 = line;
									} else if (oldErrorLine5.equals("")) {
										oldErrorLine5 = line;
									}
								}
							}
						}
					}
				}
				if (jhoveio == 0) {
					// Invalider Status
					isValid = false;
					if (min) {
						in.close();
						return false;
					} else {

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_TIFF)
								+ getTextResourceService().getText(locale, MESSAGE_XML_CG_JHOVENIO, "C"));
					}
				}
				in.close();
			} catch (Exception e) {
				if (min) {
					/* exiftoolReport loeschen */
					if (exiftoolReport.exists()) {
						exiftoolReport.delete();
					}
					return false;
				} else {

					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_TIFF)
							+ getTextResourceService().getText(locale, MESSAGE_XML_CG_CANNOTFINDETREPORT));
					return false;
				}
			}
		}
		return isValid;
	}
}