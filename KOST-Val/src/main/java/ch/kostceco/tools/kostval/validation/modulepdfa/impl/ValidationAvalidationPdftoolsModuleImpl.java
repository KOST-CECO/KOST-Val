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

package ch.kostceco.tools.kostval.validation.modulepdfa.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.pdftools.NativeLibrary;
import com.pdftools.pdfvalidator.PdfError;
import com.pdftools.pdfvalidator.PdfValidatorAPI;

import ch.kostceco.tools.kosttools.util.UtilPages;
import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfavalidationException;
import ch.kostceco.tools.kostval.service.TextResourceService;
import ch.kostceco.tools.kostval.validation.modulepdfa.ValidationAvalidationPdftoolsModule;

/**
 * Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit
 * PDF-Tools.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationPdftoolsModuleImpl
		/* extends ValidationModuleImpl */ implements ValidationAvalidationPdftoolsModule {

	public static String NEWLINE = System.getProperty("line.separator");
	private TextResourceService textResourceService;

	public TextResourceService getTextResourceService() {
		return textResourceService;
	}

	public void setTextResourceService(TextResourceService textResourceService) {
		this.textResourceService = textResourceService;
	}

	public Map<String, String> valMap(PdfValidatorAPI docPdf, File valDatei, String level, String detailConfigPpdftools,
			Locale locale, File logFile, String warning3to2, File directoryOfLogfile, String initFolderPath,
			File fileToOutputStart) throws ValidationApdfavalidationException {

		Map<String, String> valMap = new HashMap<String, String>();

		String isValidPdftoolsStr = "false";
		String error = "";
		String pdftoolsA = "";
		String pdftoolsB = "";
		String pdftoolsC = "";
		String pdftoolsD = "";
		String pdftoolsE = "";
		String pdftoolsF = "";
		String pdftoolsG = "";
		String pdftoolsH = "";
		String pdftoolsI = "";

		valMap.put("isValidPdftoolsStr", isValidPdftoolsStr);
		valMap.put("error", error);
		valMap.put("pdftoolsA", pdftoolsA);
		valMap.put("pdftoolsB", pdftoolsB);
		valMap.put("pdftoolsC", pdftoolsC);
		valMap.put("pdftoolsD", pdftoolsD);
		valMap.put("pdftoolsE", pdftoolsE);
		valMap.put("pdftoolsF", pdftoolsF);
		valMap.put("pdftoolsG", pdftoolsG);
		valMap.put("pdftoolsH", pdftoolsH);
		valMap.put("pdftoolsI", pdftoolsI);

		// TODO Validierung mit PDFTools
		boolean isValidPdfTools = false;
		int iCategory = 999999999;

		docPdf = new PdfValidatorAPI();

		try {
			UtilPages.setPages(directoryOfLogfile);
			if (docPdf.open(valDatei.getAbsolutePath(), "", NativeLibrary.COMPLIANCE.ePDFUnk)) {
				// PDF Konnte geoeffnet werden
				docPdf.setStopOnError(true);
				docPdf.setReportingLevel(1);
				if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
					error = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_A_ENCRYPTED);
					valMap.put("error", error);
					return valMap;
				}
			} else {
				if (docPdf.getErrorCode() == NativeLibrary.ERRORCODE.PDF_E_PASSWORD) {
					error = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_A_ENCRYPTED);
					valMap.put("error", error);
					return valMap;
				} else {
					error = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
							+ getTextResourceService().getText(locale, ERROR_XML_A_PDFTOOLS_DAMAGED);
					valMap.put("error", error);
					return valMap;
				}
			}

			docPdf = new PdfValidatorAPI();
			docPdf.setStopOnError(false);
			docPdf.setReportingLevel(2);

			/*
			 * ePDFA1a 5122 ePDFA1b 5121 ePDFA2a 5891 ePDFA2b 5889 ePDFA2u 5890
			 */
			if (level.contentEquals("1A")) {
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5122)) {
					docPdf.validate();
				}
			} else if (level.contentEquals("1B")) {
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5121)) {
					docPdf.validate();
				}
			} else if (level.contentEquals("2A")) {
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5891)) {
					docPdf.validate();
				}
			} else if (level.contentEquals("2B")) {
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5889)) {
					docPdf.validate();
				}
			} else if (level.contentEquals("2U")) {
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5890)) {
					docPdf.validate();
				}
			} else {
				// Validierung nach 2b
				level = "2B";
				if (docPdf.open(valDatei.getAbsolutePath(), "", 5889)) {
					docPdf.validate();
				}
			}

			// Anzahl errors
			PdfError err = docPdf.getFirstError();
			int success = 0;

			if (err != null) {
				// auch bei min durchfuehren!
				for (; err != null; err = docPdf.getNextError()) {
					success = success + 1;
				}
			}

			// Error Category
			iCategory = docPdf.getCategories();
			/*
			 * die Zahl kann auch eine Summe von Kategorien sein z.B. 6144=2048+4096 ->
			 * getCategoryText gibt nur die erste Kategorie heraus (z.B. 2048)
			 */

			if (success == 0 && iCategory == 0) {
				// valide
				isValidPdfTools = true;
			}

		} catch (Exception e) {
			error = getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, "Exec PDF Tools: " + e.getMessage());
			valMap.put("error", error);
			return valMap;
		}
		if (!isValidPdfTools) {
			boolean exponent0 = false;
			boolean exponent1 = false;
			boolean exponent2 = false;
			boolean exponent3 = false;
			boolean exponent4 = false;
			boolean exponent5 = false;
			boolean exponent6 = false;
			boolean exponent7 = false;
			boolean exponent8 = false;
			boolean exponent9 = false;
			boolean exponent10 = false;
			boolean exponent11 = false;
			boolean exponent12 = false;
			boolean exponent13 = false;
			boolean exponent14 = false;
			boolean exponent15 = false;
			boolean exponent16 = false;
			boolean exponent17 = false;
			boolean exponent18 = false;

			int iExp0 = (int) Math.pow(2, 0);
			int iExp1 = (int) Math.pow(2, 1);
			int iExp2 = (int) Math.pow(2, 2);
			int iExp3 = (int) Math.pow(2, 3);
			int iExp4 = (int) Math.pow(2, 4);
			int iExp5 = (int) Math.pow(2, 5);
			int iExp6 = (int) Math.pow(2, 6);
			int iExp7 = (int) Math.pow(2, 7);
			int iExp8 = (int) Math.pow(2, 8);
			int iExp9 = (int) Math.pow(2, 9);
			int iExp10 = (int) Math.pow(2, 10);
			int iExp11 = (int) Math.pow(2, 11);
			int iExp12 = (int) Math.pow(2, 12);
			int iExp13 = (int) Math.pow(2, 13);
			int iExp14 = (int) Math.pow(2, 14);
			int iExp15 = (int) Math.pow(2, 15);
			int iExp16 = (int) Math.pow(2, 16);
			int iExp17 = (int) Math.pow(2, 17);
			int iExp18 = (int) Math.pow(2, 18);

			// Invalide Kategorien von PDF-Tools
			if (iCategory >= iExp18) {
				exponent18 = true;
				iCategory = iCategory - iExp18;
			}
			if (iCategory >= iExp17) {
				exponent17 = true;
				iCategory = iCategory - iExp17;
			}
			if (iCategory >= iExp16) {
				exponent16 = true;
				iCategory = iCategory - iExp16;
			}
			if (iCategory >= iExp15) {
				exponent15 = true;
				iCategory = iCategory - iExp15;
			}
			if (iCategory >= iExp14) {
				exponent14 = true;
				iCategory = iCategory - iExp14;
			}
			if (iCategory >= iExp13) {
				exponent13 = true;
				iCategory = iCategory - iExp13;
			}
			if (iCategory >= iExp12) {
				exponent12 = true;
				iCategory = iCategory - iExp12;
			}
			if (iCategory >= iExp11) {
				exponent11 = true;
				iCategory = iCategory - iExp11;
			}
			if (iCategory >= iExp10) {
				exponent10 = true;
				iCategory = iCategory - iExp10;
			}
			if (iCategory >= iExp9) {
				exponent9 = true;
				iCategory = iCategory - iExp9;
			}
			if (iCategory >= iExp8) {
				exponent8 = true;
				iCategory = iCategory - iExp8;
			}
			if (iCategory >= iExp7) {
				exponent7 = true;
				iCategory = iCategory - iExp7;
			}
			if (iCategory >= iExp6) {
				exponent6 = true;
				iCategory = iCategory - iExp6;
			}
			if (iCategory >= iExp5) {
				exponent5 = true;
				iCategory = iCategory - iExp5;
			}
			if (iCategory >= iExp4) {
				exponent4 = true;
				iCategory = iCategory - iExp4;
			}
			if (iCategory >= iExp3) {
				exponent3 = true;
				iCategory = iCategory - iExp3;
			}
			if (iCategory >= iExp2) {
				exponent2 = true;
				iCategory = iCategory - iExp2;
			}
			if (iCategory >= iExp1) {
				exponent1 = true;
				iCategory = iCategory - iExp1;
			}
			if (iCategory >= iExp0) {
				exponent0 = true;
				iCategory = iCategory - iExp0;
			}
			/** Modul A **/
			if (exponent1) {
				pdftoolsA = pdftoolsA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_1, "PDF Tools: iCategory_1");
			}
			if (exponent2) {
				pdftoolsA = pdftoolsA + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_2, "PDF Tools: iCategory_2");
			}

			/** Modul B **/
			if (exponent0) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_0, "PDF Tools: iCategory_0");
			}
			if (exponent7) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_7, "PDF Tools: iCategory_7");
			}
			if (exponent18) {
				pdftoolsB = pdftoolsB + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_18, "PDF Tools: iCategory_18");
			}

			/** Modul C **/
			if (exponent3) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_3, "PDF Tools: iCategory_3");
			}
			if (exponent4) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_4, "PDF Tools: iCategory_4");
			}
			if (exponent5) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_5, "PDF Tools: iCategory_5");
			}
			if (exponent6) {
				pdftoolsC = pdftoolsC + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_6, "PDF Tools: iCategory_6");
			}

			/** Modul D **/
			if (exponent8) {
				pdftoolsD = pdftoolsD + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_8, "PDF Tools: iCategory_8");
			}
			if (exponent9) {
				pdftoolsD = pdftoolsD + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_9, "PDF Tools: iCategory_9");
			}

			/** Modul E **/
			if (exponent10) {
				pdftoolsE = pdftoolsE + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_10, "PDF Tools: iCategory_10");
			}

			/** Modul F **/
			if (exponent11) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_11, "PDF Tools: iCategory_11");
			}
			if (exponent12) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_12, "PDF Tools: iCategory_12");
			}
			if (exponent13) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_13, "PDF Tools: iCategory_13");
			}
			if (exponent14) {
				pdftoolsF = pdftoolsF + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_14, "PDF Tools: iCategory_14");
			}

			/** Modul G **/
			if (exponent15) {
				pdftoolsG = pdftoolsG + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_15, "PDF Tools: iCategory_15");
			}

			/** Modul H **/
			if (exponent16) {
				pdftoolsH = pdftoolsH + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_16, "PDF Tools: iCategory_16");
			}

			/** Modul I **/
			if (exponent17) {
				pdftoolsI = pdftoolsI + getTextResourceService().getText(locale, MESSAGE_XML_MODUL_I_PDFA)
						+ getTextResourceService().getText(locale, ERROR_XML_AI_17, "PDF Tools: iCategory_17");
			}

			// Ermittlung Detail-Fehlermeldungen von pdftools
			// (entspricht -rd)
			PdfError err = docPdf.getFirstError();
			boolean rd = false;

			if (detailConfigPpdftools.equalsIgnoreCase("detail") || detailConfigPpdftools.equalsIgnoreCase("yes")) {
				rd = true;
			}
			if (err != null && rd) {
				for (; err != null; err = docPdf.getNextError()) {
					// Ermittlung der einzelnen Error Code und Message
					int errorCode = err.getErrorCode();
					String errorCode0x = String.format("0x%08X", errorCode);
					String errorMsg = err.getMessage();

					// aus errorMsg < und > entfernen --> Probleme mit XML
					errorMsg = errorMsg.replace("<", "'");
					errorMsg = errorMsg.replace(">", "'");

					// Ausgabe
					String errorMsgCode0xText = errorMsg + " [PDF Tools: " + errorCode0x + "]";
					String errorMsgCode0x = " - " + errorMsgCode0xText;
					// System.out.println(errorMsgCode0x);
					String detailWarning3to2 = "The XMP property 'pdfaid:part' has the invalid value '3'. Required is '2'. [PDF Tools: 0x8341052E]";

					if (errorMsgCode0x.contains("The value of the CIDSet[")) {
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[1",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[2",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[3",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[1",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[2",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[3",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[4",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[5",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[6",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[7",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[8",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[9",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[0",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[1",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[2",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[3",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[4",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[5",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[6",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[7",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[8",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[9",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[0",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[1",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[2",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[3",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[4",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[5",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[6",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[7",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[8",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[9",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[0",
								"The value of the CIDSet[");
						errorMsgCode0x = errorMsgCode0x.replace("The value of the CIDSet[]", "The value of the CIDSet");
					}
					if (warning3to2.equalsIgnoreCase("yes") && errorMsgCode0xText.contains(detailWarning3to2)) {
						// Fehler wird ignoriert. Es wurde bereits eine Warnung ausgegeben.
						/*
						 * Bestehende Catogerie wieder entfernen
						 * 
						 * pdftoolsH = pdftoolsH + getTextResourceService().getText(locale,
						 * MESSAGE_XML_MODUL_H_PDFA) + getTextResourceService().getText(locale,
						 * ERROR_XML_AI_16, "PDF Tools: iCategory_16");
						 */
						pdftoolsH = pdftoolsH.replace(getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
								+ getTextResourceService().getText(locale, ERROR_XML_AI_16, "PDF Tools: iCategory_16"),
								"");
					} else if ("The document does not conform to the requested standard. [PDF Tools: 0x83410612]"
							.contains(errorMsgCode0xText)) {
						// Fehler wird ignoriert. Entsprechend wird kein Detail geschrieben.
					} else {
						// Fehler wird nicht ignoriert und dem Modul zugeordnet
						if (errorMsgCode0x.toLowerCase().contains("graphic")
								|| errorMsgCode0x.toLowerCase().contains("image")
								|| errorMsgCode0x.toLowerCase().contains("interpolate")
								|| errorMsgCode0x.toLowerCase().contains("icc")
								|| errorMsgCode0x.toLowerCase().contains("color")
								|| errorMsgCode0x.toLowerCase().contains("colour")
								|| errorMsgCode0x.toLowerCase().contains("rgb")
								|| errorMsgCode0x.toLowerCase().contains("rvb")
								|| errorMsgCode0x.toLowerCase().contains("cmyk")
								|| errorMsgCode0x.toLowerCase().contains("cmjn")
								|| errorMsgCode0x.toLowerCase().contains("outputintent")
								|| errorMsgCode0x.toLowerCase().contains("jpeg2000")
								|| errorMsgCode0x.toLowerCase().contains("devicegray")
								|| errorMsgCode0x.toLowerCase().contains("key 'tr'.")
								|| errorMsgCode0x.toLowerCase().contains("tr2")) {
							if (pdftoolsC.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsC = pdftoolsC
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("police")
								|| errorMsgCode0x.toLowerCase().contains("font")
								|| errorMsgCode0x.toLowerCase().contains("gly")
								|| errorMsgCode0x.toLowerCase().contains("truetype")
								|| errorMsgCode0x.toLowerCase().contains("unicode")
								|| errorMsgCode0x.toLowerCase().contains("cid")
								|| errorMsgCode0x.toLowerCase().contains("encoding")
								|| errorMsgCode0x.toLowerCase().contains("charset")) {
							if (pdftoolsD.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsD = pdftoolsD
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_D_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("disponibi")
								|| errorMsgCode0x.toLowerCase().contains("accessibi")
								|| errorMsgCode0x.toLowerCase().contains("markinfo")
								|| errorMsgCode0x.toLowerCase().contains("structree")
								|| errorMsgCode0x.toLowerCase().contains("structure tree root")
								|| errorMsgCode0x.toLowerCase().contains(" cross reference ")
								|| errorMsgCode0x.toLowerCase()
										.contains(" but must be a standard type. [PDF Tools: 0x00418607]")
								|| errorMsgCode0x.toLowerCase().contains("strukturbaum")) {
							if (pdftoolsI.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsI = pdftoolsI
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_I_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("structure")
								|| errorMsgCode0x.toLowerCase().contains(" ocproperties")
								|| errorMsgCode0x.toLowerCase().contains(" lzw")
								|| errorMsgCode0x.toLowerCase().contains(" structelem")
								|| errorMsgCode0x.toLowerCase().contains(" xref")
								|| errorMsgCode0x.toLowerCase().contains(" eol")
								|| errorMsgCode0x.toLowerCase().contains(" eof")) {
							if (pdftoolsB.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsB = pdftoolsB
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("metad")
								|| errorMsgCode0x.toLowerCase().contains("xmp")
								|| errorMsgCode0x.toLowerCase().contains("xml")
								|| errorMsgCode0x.toLowerCase().contains("key 'filter'.")
								|| errorMsgCode0x.toLowerCase().contains("schema description for namespace")
								|| errorMsgCode0x.toLowerCase().contains("multiple occurrences of property 'pdf:")
								|| errorMsgCode0x.toLowerCase().contains("is not defined in schema")) {
							if (pdftoolsH.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsH = pdftoolsH
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_H_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("transparen")) {
							if (pdftoolsE.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsE = pdftoolsE
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_E_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("action")
								|| errorMsgCode0x.toLowerCase().contains("aa")
								|| errorMsgCode0x.toLowerCase().contains("key 'a'")
								|| errorMsgCode0x.toLowerCase().contains("javascript")) {
							if (pdftoolsG.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsG = pdftoolsG
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else if (errorMsgCode0x.toLowerCase().contains("annotation")
								|| errorMsgCode0x.toLowerCase().contains("embedd")
								|| errorMsgCode0x.toLowerCase().contains("comment")
								|| errorMsgCode0x.toLowerCase().contains("structure")
								|| errorMsgCode0x.toLowerCase().contains("print")
								|| errorMsgCode0x.toLowerCase().contains("incorpor")
								|| errorMsgCode0x.toLowerCase().contains("key f ")
								|| errorMsgCode0x.toLowerCase().contains("appearance")) {
							if (pdftoolsF.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsF = pdftoolsF
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_F_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}

						} else {
							if (pdftoolsA.toLowerCase().contains(errorMsgCode0x.toLowerCase())) {
								// Fehlermeldung bereits erfasst ->
								// keine Aktion
							} else {
								pdftoolsA = pdftoolsA
										+ getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_PDFA)
										+ "<Message>" + errorMsgCode0x + "</Message></Error>";
							}
						}
					}
				}

				// Kontrolle ob details noch existieren
				if (pdftoolsA.equals("") && pdftoolsB.equals("") && pdftoolsC.equals("") && pdftoolsD.equals("")
						&& pdftoolsE.equals("") && pdftoolsF.equals("") && pdftoolsG.equals("") && pdftoolsH.equals("")
						&& pdftoolsI.equals("")) {
					isValidPdfTools = true;
				}
			}
		}

		if (isValidPdfTools) {
			isValidPdftoolsStr = "true";
		} else {
			isValidPdftoolsStr = "false";
		}
		valMap.put("isValidPdftoolsStr", isValidPdftoolsStr);
		valMap.put("error", error);
		valMap.put("pdftoolsA", pdftoolsA);
		valMap.put("pdftoolsB", pdftoolsB);
		valMap.put("pdftoolsC", pdftoolsC);
		valMap.put("pdftoolsD", pdftoolsD);
		valMap.put("pdftoolsE", pdftoolsE);
		valMap.put("pdftoolsF", pdftoolsF);
		valMap.put("pdftoolsG", pdftoolsG);
		valMap.put("pdftoolsH", pdftoolsH);
		valMap.put("pdftoolsI", pdftoolsI);

		return valMap;
	}
}