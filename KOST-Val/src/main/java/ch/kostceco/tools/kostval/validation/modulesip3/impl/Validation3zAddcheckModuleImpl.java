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

package ch.kostceco.tools.kostval.validation.modulesip3.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip3.Validation3zAddcheckException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3zAddcheckModule;

/**
 * Validierungsschritt 3z (einschaltbar) Validierung von optionalen zusatz
 * Einschraenkungen.
 * 
 * z.B. Auf Stuffe Dossier betreffend Schutzfrist, Oeffentlichkeitstatus und
 * Datenschutz
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */
public class Validation3zAddcheckModuleImpl extends ValidationModuleImpl implements Validation3zAddcheckModule {

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath, String initFolderPath, File fileToOutputStart)
			throws Validation3zAddcheckException {
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		if (onWorkConfig.equals("yes")) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("3Z   ");
			System.out.print("\b\b\b\b\b");
		}

		boolean isValid = true;
		boolean schutzfristBoo = false;
		String schutzfristcheck = configMap.get("schutzfristcheck");
		String schutzfristvalue = "";
		if (schutzfristcheck.equals("yes")) {
			schutzfristBoo = true;
			schutzfristvalue = configMap.get("schutzfristvalue");
		}

		boolean oeffentlichkeitBoo = false;
		String oeffentlichkeitcheck = configMap.get("oeffentlichkeitcheck");
		String oeffentlichkeitvalue = "";
		if (oeffentlichkeitcheck.equals("yes")) {
			oeffentlichkeitBoo = true;
			oeffentlichkeitvalue = configMap.get("oeffentlichkeitvalue");
		}

		boolean datenschutzBoo = false;
		String datenschutzcheck = configMap.get("datenschutzcheck");
		if (datenschutzcheck.equals("yes")) {
			datenschutzBoo = true;
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(new File(
					valDatei.getAbsolutePath() + File.separator + "header" + File.separator + "metadata.xml")));
			doc.normalize();

			// Start mit der Kontrolle der zusaetzlichen Checks auf der Stufe Dossier

			// über alle Dossiers iterieren
			NodeList nodeLstDossier = doc.getElementsByTagName("dossier");
			for (int s = 0; s < nodeLstDossier.getLength(); s++) {
				Node dossierNode = nodeLstDossier.item(s);

				String attDossierNode = dossierNode.getAttributes().getNamedItem("id") + "";
				// System.out.println(attDossierNode); id="DOSbf7187229de6430e89e0abb847b62e12"

				String schutzfristString = null;
				int schutzfristCount = 0;
				String oeffentlichkeitsstatusString = null;
				int oeffentlichkeitsstatusCount = 0;
				int datenschutzCount = 0;

				NodeList childNodesDos = dossierNode.getChildNodes();
				for (int y = 0; y < childNodesDos.getLength(); y++) {
					Node subNodeDos = childNodesDos.item(y);
					if (subNodeDos.getNodeName().equals("schutzfrist")) {
						// System.out.println("Element " + subNodeDos.getNodeName());
						schutzfristCount = schutzfristCount + 1;
						if (schutzfristBoo) {
							// Lesen der Werte vom schutzfrist der Dossier
							// <schutzfristcheck>no</schutzfristcheck> <!-- no = nicht kontrollieren / yes =
							// kontrollieren -->
							// <schutzfristvalue>^(30|110)$</schutzfristvalue> <!-- Regex der moeglichen
							// Werte z.B. ^(30|110)$ -->
							schutzfristString = subNodeDos.getTextContent();
							// System.out.println("getTextContent " + subNodeDos.getTextContent());
							// TODO Kontrolle ob Inhalt mit Regex uebereinstimmt
							Pattern p = Pattern.compile(schutzfristvalue);
							Matcher matcher = p.matcher(schutzfristString);
							boolean matchFound = matcher.find();
							if (!matchFound) {
								// Der Inhalt "{2}" des Metadatum {0} von Dossier {1} entspricht nicht dem
								// geforderten Wert "{3}".
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
												+ getTextResourceService().getText(locale, ERROR_XML_CZ_WRONG_VALUE,
														"schutzfrist", attDossierNode, schutzfristString,
														schutzfristvalue));
								isValid = false;
							}
						}
					} else if (subNodeDos.getNodeName().equals("oeffentlichkeitsstatus")) {
						// System.out.println("Element " + subNodeDos.getNodeName());
						oeffentlichkeitsstatusCount = oeffentlichkeitsstatusCount + 1;
						if (oeffentlichkeitBoo) {
							// Lesen der Werte vom oeffentlichkeitsstatus der Dossier
							// <oeffentlichkeitcheck>no</oeffentlichkeitcheck> <!-- no = nicht kontrollieren
							// / yes = kontrollieren -->
							// <oeffentlichkeitvalue>^(Einsehbar|Nicht Einsehbar)$</oeffentlichkeitvalue>
							// <!-- Regex der moeglichen Werte z.B. ^(Einsehbar|Nicht Einsehbar)$ -->
							oeffentlichkeitsstatusString = subNodeDos.getTextContent();
							// System.out.println("getTextContent " + subNodeDos.getTextContent());
							// TODO Kontrolle ob Inhalt mit Regex uebereinstimmt
							Pattern p = Pattern.compile(oeffentlichkeitvalue);
							Matcher matcher = p.matcher(oeffentlichkeitsstatusString);
							boolean matchFound = matcher.find();
							if (!matchFound) {
								// Der Inhalt "{2}" des Metadatum {0} von Dossier {1} entspricht nicht dem
								// geforderten Wert "{3}".
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
												+ getTextResourceService().getText(locale, ERROR_XML_CZ_WRONG_VALUE,
														"oeffentlichkeitsstatus", attDossierNode,
														oeffentlichkeitsstatusString, oeffentlichkeitvalue));
								isValid = false;
							}
						}
					} else if (subNodeDos.getNodeName().equals("datenschutz")) {
						// System.out.println("Element " + subNodeDos.getNodeName());
						datenschutzCount = datenschutzCount + 1;
						// Lesen der Werte vom datenschutz der Dossier
						// <datenschutzcheck>no</datenschutzcheck> <!-- no = nicht kontrollieren / yes =
						// kontrollieren ob vorhanden -->
					}
				}
				if (schutzfristBoo) {
					if (schutzfristCount == 0) {
						// Fehler "Das Metadatum schutzfrist auf Stufe Dossier wird gefordert und fehlt
						// beim Dossier id="DOSbf7187229de6430e89e0abb847b62e12"."
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
										+ getTextResourceService().getText(locale, ERROR_XML_CZ_MISSING_METADATA,
												"schutzfrist", attDossierNode));
						isValid = false;
					}
				}

				if (oeffentlichkeitBoo) {
					if (oeffentlichkeitsstatusCount == 0) {
						// Fehler "Das Metadatum oeffentlichkeitsstatus auf Stufe Dossier wird gefordert
						// und fehlt beim Dossier id="DOSbf7187229de6430e89e0abb847b62e12"."
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
										+ getTextResourceService().getText(locale, ERROR_XML_CZ_MISSING_METADATA,
												"oeffentlichkeitsstatus", attDossierNode));
						isValid = false;
					}
				}

				if (datenschutzBoo) {
					if (datenschutzCount == 0) {
						// Fehler "Das Metadatum datenschutz auf Stufe Dossier wird gefordert und fehlt
						// beim Dossier id="DOSbf7187229de6430e89e0abb847b62e12"."
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
										+ getTextResourceService().getText(locale, ERROR_XML_CZ_MISSING_METADATA,
												"datenschutz", attDossierNode));
						isValid = false;
					}
				}

				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("3Z-  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("3Z\\  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("3Z|  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("3Z/  ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}
		} catch (Exception e) {
			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Cz_SIP)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return isValid;
	}
}
