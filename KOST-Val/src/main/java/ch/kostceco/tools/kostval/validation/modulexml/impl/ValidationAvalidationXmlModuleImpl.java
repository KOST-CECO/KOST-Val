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

package ch.kostceco.tools.kostval.validation.modulexml.impl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.kostceco.tools.kosttools.fileservice.Xmllint;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulexml.ValidationAxmlvalidationException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulexml.ValidationAvalidationXmlModule;

/**
 * Ist die vorliegende XML-Datei eine valide XML-Datei? XML Validierungs mit
 * xmllint.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * xmllint, sofern lokal das Schema vorhanden ist.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationAvalidationXmlModuleImpl extends ValidationModuleImpl implements ValidationAvalidationXmlModule {

	private boolean min = false;

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationAxmlvalidationException {
		String onWork = configMap.get("ShowProgressOnWork");
		if (onWork.equals("nomin")) {
			min = true;
		}
		String pathToWorkDir = configMap.get("PathToWorkDir");
		File workDir = new File(pathToWorkDir);
		if (!workDir.exists()) {
			workDir.mkdir();
		}

		// Die Erkennung erfolgt bereits im Vorfeld

		// TODO: Erledigt: check xmllint
		// - Initialisierung xmllint -> existiert xmllint?

		// Pfad zum Programm existiert die Dateien?
		String checkTool = Xmllint.checkXmllint(dirOfJarPath);
		if (!checkTool.equals("OK")) {
			if (min) {
				return false;
			} else {
				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_A_XML)
								+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkTool,
										getTextResourceService().getText(locale, ABORTED)));
				return false;
			}
		}
		// TODO: Erledigt: Aufbau der xml und xsd
		// - Aufbau Kontrolle mit xmllint
		try {
			String resultStructure = Xmllint.structXmllint(valDatei, workDir, dirOfJarPath, locale);
			if (!resultStructure.equals("OK")) {
				// System.out.println( "Struktur NICHT korrekt: " +
				// resultExec );
				if (min) {
					return false;
				} else {
					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_XML)
							+ getTextResourceService().getText(locale, ERROR_XML_B_XML_XMLLINT_FAILSTR));
					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_B_XML) + getTextResourceService()
									.getText(locale, MESSAGE_XML_SERVICEMESSAGE, "- ", resultStructure));
					return false;
				}
			} else {
				// System.out.println( "Validierung bestanden" );
				// OK
			}

		} catch (Exception e) {
			Logtxt.logtxt(logFile,
					getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML) + getTextResourceService()
							.getText(locale, ERROR_XML_SERVICEFAILED_EXIT, "Xmllint", e.getMessage()));
			return false;
		}

		// TODO: Erledigt: XML Validierung mit XSD (optional / konfigurierbar)
		boolean isValid = false;
		String schema = configMap.get("schema");

		if (schema.equals("no")) {
			isValid = true;
		} else {
			if ((valDatei.getAbsolutePath().toLowerCase().endsWith(".xsd"))
					|| (valDatei.getAbsolutePath().toLowerCase().endsWith(".xsl"))) {
				// keine XML-Validierung bei XSD- und XSL-Dateien
				isValid = true;
			} else {
				File xsdDatei = valDatei;
				Boolean xsdLocal = false;
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db;
					db = dbf.newDocumentBuilder();
					Document document = db.parse(valDatei);
					String xsdPaths = "-";
					if (Util.stringInFile("xsi:schemaLocation", valDatei)) {
						xsdPaths = document.getDocumentElement().getAttributes().getNamedItem("xsi:schemaLocation")
								.getNodeValue();
						// System.out.println( "xsdPath: " + xsdPaths );
						// xsdPath: http://bar.admin.ch/arelda/v4 xsd/arelda.xsd
						// xsdPath: http://www.loc.gov/premis/v3 ../xsd/premis.xsd
						// xsdPath: http://www.loc.gov/premis/v3
						// http://www.loc.gov/standards/premis/premiss.xsd
						// xsdPath: http://www.loc.gov/premis/v3 ../xsd/premis.xsd
						// xsdPath: http://www.loc.gov/premis/v3
						// http://www.loc.gov/standards/premis/premis.xsd

						// in die zwei Teile aufsplitten

						String foo = xsdPaths;
						String parts[] = foo.split(" ", 2);
						// System.out.println( String.format( "1: %s, 2: %s",
						// parts[0],
						// parts[1] ) );

						String xsdPath = parts[1];
						File valFolder = valDatei.toPath().getParent().toFile();
						String xsdAbsolutePath = valFolder.getAbsolutePath() + File.separator + xsdPath;
						// System.out.println( "xsdAbsolutePath 1: " +
						// xsdAbsolutePath
						// );
						xsdDatei = new File(xsdAbsolutePath);

						if (!xsdDatei.exists()) {
							xsdPath = parts[0];
							xsdAbsolutePath = valFolder.getAbsolutePath() + File.separator + xsdPath;
							// System.out.println( "xsdAbsolutePath 0: " +
							// xsdAbsolutePath
							// );
							xsdDatei = new File(xsdAbsolutePath);
							if (!xsdDatei.exists()) {
								if (min) {
									return false;
								} else {
									Logtxt.logtxt(logFile,
											getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
													+ getTextResourceService().getText(locale,
															ERROR_XML_C_XML_NOXSDFILE, xsdPaths));
								}
							}
						}
					} else if (Util.stringInFile("xsi:noNamespaceSchemaLocation", valDatei)) {
						xsdPaths = document.getDocumentElement().getAttributes()
								.getNamedItem("xsi:noNamespaceSchemaLocation").getNodeValue();

						File valFolder = valDatei.toPath().getParent().toFile();
						String xsdAbsolutePath = valFolder.getAbsolutePath() + File.separator + xsdPaths;
						// System.out.println( "xsdAbsolutePath 1: " +
						// xsdAbsolutePath
						// );
						xsdDatei = new File(xsdAbsolutePath);
					}

					if (xsdPaths == "-") {
						if (min) {
							return false;
						} else {
							Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
									+ getTextResourceService().getText(locale, ERROR_XML_C_XML_NOXSDFILE, xsdPaths));
						}
					} else {
						if (xsdDatei.exists()) {
							// System.out.println( xsdDatei.getAbsolutePath() + "
							// existiert!" );
							xsdLocal = true;
						}
					}
				} catch (ParserConfigurationException | SAXException | IOException e1) {
					Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
							+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, e1.getMessage()));
					return false;
				}

				if (xsdLocal) {
					// - Schemavalidierung xmllint
					try {
						String resultExec = Xmllint.execXmllint(valDatei, xsdDatei, workDir, dirOfJarPath, locale);
						if (!resultExec.equals("OK")) {
							// System.out.println( "Validierung NICHT bestanden: " +
							// resultExec );
							if (min) {
								return false;
							} else {
								isValid = false;
								String xmlShortString = valDatei.getAbsolutePath().replace(workDir.getAbsolutePath(),
										"");
								String xsdShortString = xsdDatei.getAbsolutePath().replace(workDir.getAbsolutePath(),
										"");
								// val.message.xml.h.invalid.xml = <Message>{0} ist
								// invalid zu
								// {1}</Message></Error>
								// val.message.xml.h.invalid.error =
								// <Message>{0}</Message></Error>
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
												+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEINVALID,
														"Xmllint", ""));
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
												+ getTextResourceService().getText(locale, MESSAGE_XML_H_INVALID_XML,
														xmlShortString, xsdShortString));
								Logtxt.logtxt(logFile,
										getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
												+ getTextResourceService().getText(locale, MESSAGE_XML_SERVICEMESSAGE,
														"- ", resultExec));
							}
						} else {
							// System.out.println( "Validierung bestanden" );
							// OK
							isValid = true;
						}

					} catch (Exception e) {
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_C_XML)
										+ getTextResourceService().getText(locale, ERROR_XML_SERVICEFAILED_EXIT,
												"Xmllint", e.getMessage()));
						return false;
					}
				}
			}
		}
		// TODO: Erledigt: Fehler Auswertung

		return isValid;
	}
}
