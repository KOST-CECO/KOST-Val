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

package ch.kostceco.tools.kostval.validation.modulesip2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip2.Validation2dGeverFileIntegrityException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip2.Validation2dGeverFileIntegrityModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt 2d Bei GEVER und Files SIP pruefen, ob alle in
 * (metadata.xml) /paket/inhaltsverzeichnis/content referenzierten Dateien auch
 * in (metadata.xml)/paket/ablieferung/ordnungsystem verzeichnet sind.
 * Allfaellige Inkonsistenzen auflisten. ( //dokument[@id] => //datei[@id] ).
 */

public class Validation2dGeverFileIntegrityModuleImpl extends ValidationModuleImpl
		implements Validation2dGeverFileIntegrityModule {

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws Validation2dGeverFileIntegrityException {
		boolean showOnWork = false;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if (onWorkConfig.equals("yes")) {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("2D   ");
			System.out.print("\b\b\b\b\b");
		}
		Map<String, String> dateiRefContent = new HashMap<String, String>();
		Map<String, String> dateiRefOrdnungssystem = new HashMap<String, String>();
		Map<String, String> dateiRef = new HashMap<String, String>();
		Map<String, String> dokRef = new HashMap<String, String>();
		Map<String, String> dosRef = new HashMap<String, String>();
		Map<String, String> mapRef = new HashMap<String, String>();

		boolean valid = true;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db
					.parse(new FileInputStream(new File(valDatei.getAbsolutePath() + "//header//metadata.xml")));
			doc.getDocumentElement().normalize();

			// Pruefung auch fuer FILE-SIP nicht nur GEVER-SIP
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList nodeLst = doc.getElementsByTagName("dateiRef");
			NodeList nodeLstDateiRef = nodeLst;

			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);

				Element fstElement = (Element) fstNode;
				Node parentNode = fstElement.getParentNode();
				Element parentElement = (Element) parentNode;
				NodeList titelList = parentElement.getElementsByTagName("titel");

				Node titelNode = titelList.item(0);
				dateiRefOrdnungssystem.put(fstNode.getTextContent(), titelNode.getTextContent());
				dateiRef.put(fstNode.getTextContent(), titelNode.getTextContent());
				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("2D-  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("2D\\  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("2D|  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("2D/  ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}

			// alle datei ids aus header/content holen
			NodeList nameNodes = (NodeList) xpath.evaluate("//ordner/name", doc, XPathConstants.NODESET);
			for (int s = 0; s < nameNodes.getLength(); s++) {
				Node dateiNode = nameNodes.item(s);
				if (dateiNode.getTextContent().equals("content")) {
					Element dateiElement = (Element) dateiNode;
					Element parentElement = (Element) dateiElement.getParentNode();

					NodeList dateiNodes = parentElement.getElementsByTagName("datei");
					for (int x = 0; x < dateiNodes.getLength(); x++) {
						Node dateiNode2 = dateiNodes.item(x);
						Node id = dateiNode2.getAttributes().getNamedItem("id");

						Element dateiElement2 = (Element) dateiNode2;
						NodeList nameList = dateiElement2.getElementsByTagName("name");
						Node titelNode = nameList.item(0);

						Node dateiParentNode = dateiElement2.getParentNode();
						Element dateiParentElement = (Element) dateiParentNode;
						NodeList nameNodes2 = dateiParentElement.getElementsByTagName("name");
						Node contentName = nameNodes2.item(0);

						dateiRefContent.put(id.getNodeValue(),
								"content/" + contentName.getTextContent() + "/" + titelNode.getTextContent());
						dateiRef.put(id.getNodeValue(),
								"content/" + contentName.getTextContent() + "/" + titelNode.getTextContent());
					}
				}
				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("2D-  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("2D\\  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("2D|  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("2D/  ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}

			Set<String> keysContent = dateiRefContent.keySet();
			boolean titlePrinted = false;
			for (Iterator<String> iterator = keysContent.iterator(); iterator.hasNext();) {
				String keyContent = iterator.next();
				String deleted = dateiRefOrdnungssystem.remove(keyContent);
				if (deleted == null) {
					if (!titlePrinted) {

						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Bd_SIP)
										+ getTextResourceService().getText(locale, MESSAGE_XML_BD_MISSINGINABLIEFERUNG,
												keyContent));
						titlePrinted = true;
					}
					valid = false;
				}
				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("2D-  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("2D\\  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("2D|  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("2D/  ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}

			Set<String> keysRefOrd = dateiRefOrdnungssystem.keySet();
			for (Iterator<String> iterator = keysRefOrd.iterator(); iterator.hasNext();) {
				String keyOrd = iterator.next();
				/*
				 * Die folgende DateiRef vorhanden in metadata/paket/ablieferung/ordnungssystem,
				 * aber nicht in metadata/paket/inhaltsverzeichnis/content
				 */

				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Bd_SIP)
						+ getTextResourceService().getText(locale, MESSAGE_XML_BD_MISSINGININHALTSVERZEICHNIS, keyOrd));
				valid = false;
				if (showOnWork) {
					if (onWork == 410) {
						onWork = 2;
						System.out.print("2D-  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 110) {
						onWork = onWork + 1;
						System.out.print("2D\\  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 210) {
						onWork = onWork + 1;
						System.out.print("2D|  ");
						System.out.print("\b\b\b\b\b");
					} else if (onWork == 310) {
						onWork = onWork + 1;
						System.out.print("2D/  ");
						System.out.print("\b\b\b\b\b");
					} else {
						onWork = onWork + 1;
					}
				}
			}

			NodeList nodeLstDok = doc.getElementsByTagName("dokument");
			for (int x = 0; x < nodeLstDok.getLength(); x++) {
				Node dokNode = nodeLstDok.item(x);
				Node id = dokNode.getAttributes().getNamedItem("id");
				dokRef.put(id.getNodeValue(), "dokument");
			}
			NodeList nodeLstDos = doc.getElementsByTagName("dossier");
			for (int x = 0; x < nodeLstDos.getLength(); x++) {
				Node dosNode = nodeLstDos.item(x);
				Node id = dosNode.getAttributes().getNamedItem("id");
				dokRef.put(id.getNodeValue(), "dossier");
			}
			NodeList nodeLstMap = doc.getElementsByTagName("mappe");
			for (int x = 0; x < nodeLstMap.getLength(); x++) {
				Node mapNode = nodeLstMap.item(x);
				Node id = mapNode.getAttributes().getNamedItem("id");
				mapRef.put(id.getNodeValue(), "mappe");
			}

			// Kontrolle ob IDs von information und repraesentation in dateiRef
			// existieren
			// System.out.println( "2g Attribute der dateiRef auslesen..." );

			for (int i = 0, len = nodeLstDateiRef.getLength(); i < len; i++) {
				Element elm = (Element) nodeLstDateiRef.item(i);
				if (elm.hasAttribute("information")) {
					String info = elm.getAttribute("information");
					// System.out.println( "information: " + info );
					if (dateiRef.containsKey(info) || dokRef.containsKey(info) || dosRef.containsKey(info)
							|| mapRef.containsKey(info)) {
						// System.out.println( "information vorhanden (Key): " +
						// info );
					} else {
						// System.out.println( "information trennen: " + info );
						// mehrere IDs werden mit Leerschlag getrennt.
						String[] parts = info.split(" ");
						for (int j = 0, lenJ = parts.length; j < lenJ; j++) {
							String infoPart = parts[j];

							if (dateiRef.containsKey(infoPart) || dokRef.containsKey(infoPart)
									|| dosRef.containsKey(infoPart) || mapRef.containsKey(infoPart)) {
								// System.out.println( "information (part)
								// vorhanden (Key): " + infoPart );
							} else {
								// System.out.println( "information fehlt: " +
								// infoPart );
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Bd_SIP)
												+ getTextResourceService().getText(locale,
														MESSAGE_XML_BD_WARNINGMISSINGINFOID, infoPart));
							}
						}
					}
				}
				if (elm.hasAttribute("repraesentation")) {
					String rep = elm.getAttribute("repraesentation");
					// System.out.println( "repraesentation: " + rep );
					if (dateiRef.containsKey(rep) || dokRef.containsKey(rep) || dosRef.containsKey(rep)
							|| mapRef.containsKey(rep)) {
						// System.out.println( "repraesentation vorhanden (Key):
						// " + rep );
					} else {
						// System.out.println( "repraesentation trennen: " + rep
						// );
						// mehrere IDs werden mit Leerschlag getrennt.
						String[] parts = rep.split(" ");
						for (int j = 0, lenJ = parts.length; j < lenJ; j++) {
							String repPart = parts[j];

							if (dateiRef.containsKey(repPart) || dokRef.containsKey(repPart)
									|| dosRef.containsKey(repPart) || mapRef.containsKey(repPart)) {
								// System.out.println( "repraesentation (part)
								// vorhanden (Key): " + repPart );
							} else {
								// System.out.println( "repraesentation fehlt: "
								// + repPart );
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Bd_SIP)
												+ getTextResourceService().getText(locale,
														MESSAGE_XML_BD_WARNINGMISSINGREPID, repPart));
							}
						}
					}
				}
			}

			// System.out.println("information:
			// "+nodeLstDateiRef.item(x).getAttributes().getNamedItem("information").getNodeValue());
			// System.out.println("repraesentation:
			// "+nodeLstDateiRef.item(x).getAttributes().getNamedItem("repraesentation").getNodeValue());
			// rep

		} catch (Exception e) {

			Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_Bd_SIP)
					+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e.getMessage()));
			return false;
		}
		return valid;
	}

}
