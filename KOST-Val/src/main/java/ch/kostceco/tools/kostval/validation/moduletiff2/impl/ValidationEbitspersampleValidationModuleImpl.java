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

package ch.kostceco.tools.kostval.validation.moduletiff2.impl;

import java.io.BufferedReader;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.io.FileReader;

import ch.kostceco.tools.kostval.exception.moduletiff2.ValidationEbitspersampleValidationException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.moduletiff2.ValidationEbitspersampleValidationModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt E (BitsPerSample-Validierung) Ist die TIFF-Datei gem�ss
 * Konfigurationsdatei valid?
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationEbitspersampleValidationModuleImpl extends ValidationModuleImpl
		implements ValidationEbitspersampleValidationModule {

	public static String NEWLINE = System.getProperty("line.separator");

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationEbitspersampleValidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}

		boolean isValid = true;

		/*
		 * TODO: jhoveReport auswerten! Auf Exiftool wird verzichtet. Exiftool verwendet
		 * Perl, welche seit einiger Zeit hohe nicht geloese Sicherheitsrisiken birgt.
		 * zudem koennen die Metadaten vermehrt komplett durch jhove ausgelesen werden.
		 * Jhove hat bereits einen der Probleme, welche das teilweise die Ausgabe der
		 * Metadaten verhindert behoben.
		 */
		File jhoveReport = new File(directoryOfLogfile, valDatei.getName() + ".jhove-log.txt");

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
			String bps1 = configMap.get("AllowedBitspersample1");
			String bps2 = configMap.get("AllowedBitspersample2");
			String bps4 = configMap.get("AllowedBitspersample4");
			String bps8 = configMap.get("AllowedBitspersample8");
			String bps16 = configMap.get("AllowedBitspersample16");
			String bps32 = configMap.get("AllowedBitspersample32");

			if (bps1.equals("")) {
				bps1 = "DieseBitspersampleIstNichtErlaubt";
			}
			if (bps2.equals("")) {
				bps2 = "DieseBitspersampleIstNichtErlaubt";
			}
			if (bps4.equals("")) {
				bps4 = "DieseBitspersampleIstNichtErlaubt";
			}
			if (bps8.equals("")) {
				bps8 = "DieseBitspersampleIstNichtErlaubt";
			}
			if (bps16.equals("")) {
				bps16 = "DieseBitspersampleIstNichtErlaubt";
			}
			if (bps32.equals("")) {
				bps32 = "DieseBitspersampleIstNichtErlaubt";
			}

			Integer jhoveio = 0;
			String oldErrorLine1 = "";
			String oldErrorLine2 = "";
			String oldErrorLine3 = "";
			String oldErrorLine4 = "";
			String oldErrorLine5 = "";

			Boolean tiffLine = false;
			try {
				BufferedReader in = new BufferedReader(new FileReader(jhoveReport));
				String line;
				while ((line = in.readLine()) != null) {
					/*
					 * zu analysierende TIFF-IFD-Zeile die BitsPerSample-Zeile enthaaelt einer
					 * dieser Freitexte der BitsPerSampleart max ist 1, 2, 4, 8, 16, 32 erlaubt
					 * 
					 * Varianten: BitsPerSample: 8 BitsPerSample: 8 8 8 BitsPerSample: 8, 8, 8 evtl
					 * noch mehr
					 */

					if (line.contains(" Type: TIFF")) {
						tiffLine = true;
					} else if (line.contains(" Type: ")) {
						tiffLine = false;
					}

					if (line.contains(" BitsPerSample: ") && tiffLine) {
						jhoveio = 1;
						if (((line.contains("BitsPerSample: 1 ") || (line.contains("BitsPerSample: 1,"))
								|| (line.endsWith("BitsPerSample: 1"))) && bps1.contains("1"))
								|| ((line.contains("BitsPerSample: 2 ") || (line.contains("BitsPerSample: 2,"))
										|| (line.endsWith("BitsPerSample: 2"))) && bps2.contains("2"))
								|| ((line.contains("BitsPerSample: 4 ") || (line.contains("BitsPerSample: 4,"))
										|| (line.endsWith("BitsPerSample: 4"))) && bps4.contains("4"))
								|| ((line.contains("BitsPerSample: 8 ") || (line.contains("BitsPerSample: 8,"))
										|| (line.endsWith("BitsPerSample: 8"))) && bps8.contains("8"))
								|| ((line.contains("BitsPerSample: 16 ") || (line.contains("BitsPerSample: 16,"))
										|| (line.endsWith("BitsPerSample: 16"))) && bps16.contains("16"))
								|| ((line.contains("BitsPerSample: 32 ") || (line.contains("BitsPerSample: 32,"))
										|| (line.endsWith("BitsPerSample: 32"))) && bps32.contains("32"))) {
							// Valid
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
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_TIFF)
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
					// default = 1
					if (bps1.contains("1")) {
						// Valid
					} else {
						line = "Default BitsPerSample 1";

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_TIFF)
								+ getTextResourceService().getText(locale, MESSAGE_XML_CG_INVALID, line));
					}
				}
				in.close();
			} catch (Exception e) {
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_TIFF)
							+ getTextResourceService().getText(locale, MESSAGE_XML_CG_CANNOTFINDETREPORT));
					return false;
				}
			}
			return isValid;
		}
	}
}