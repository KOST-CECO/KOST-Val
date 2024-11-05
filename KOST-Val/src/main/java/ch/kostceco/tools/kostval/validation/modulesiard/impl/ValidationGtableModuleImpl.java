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

package ch.kostceco.tools.kostval.validation.modulesiard.impl;

import java.io.File;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kostval.exception.modulesiard.ValidationGtableException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationGtableModule;
import ch.kostceco.tools.kostval.logging.Logtxt;

/**
 * Validierungsschritt G (Tabellen-Validierung) prueft, ob Spaltennamen
 * innerhalb der Tabelle(n) resp. Tabellennamen innerhalb der Schema(s) und
 * Schemanamen einmalig sind.
 * 
 * @author Sp Peter Schneider, Staatsarchiv Aargau
 */

public class ValidationGtableModuleImpl extends ValidationModuleImpl implements ValidationGtableModule {
	private boolean min = false;

	Boolean version1 = false;
	Boolean version2 = false;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationGtableException {
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
			// Ausgabe Modul Ersichtlich das KOST-Val arbeitet
			showOnWork = true;
			System.out.print("G    ");
			System.out.print("\b\b\b\b\b");
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
		}

		boolean valid = true;
		try {
			/*
			 * Extract the metadata.xml from the temporare work folder and build a jdom
			 * document
			 */

			String pathToWorkDir = configMap.get("PathToWorkDir");
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			/*
			 * Nicht vergessen in
			 * "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property
			 * name="configurationService" ref="configurationService" />
			 */
			File metadataXml = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("header")
					.append(File.separator).append("metadata.xml").toString());

			InputStream fin = new FileInputStream(metadataXml);
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(fin);
			fin.close();
			// set to null
			fin = null;

			// declare ArrayLists
			List listSchemas = new ArrayList();
			List listTables = new ArrayList();
			List listColumns = new ArrayList();

			/*
			 * read the document and for each schema and table entry verify existence in
			 * temporary extracted structure
			 */
			version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
					.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			Namespace ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
			if (version1) {
				// ns = Namespace.getNamespace(
				// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
			} else if (version2) {
				ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
			}

			// select schema elements and loop
			List<Element> schemas = document.getRootElement().getChild("schemas", ns).getChildren("schema", ns);
			for (Element schema : schemas) {
				String schemaName = schema.getChild("name", ns).getText();

				String lsSch = (new StringBuilder().append(schemaName).toString());
				if (schema.getChild("tables", ns) != null) {

					// select table elements and loop
					List<Element> tables = schema.getChild("tables", ns).getChildren("table", ns);
					for (Element table : tables) {
						String tableName = table.getChild("name", ns).getText();

						// Concatenate schema and table
						String lsTab = (new StringBuilder().append(schemaName).append(" / ").append(tableName)
								.toString());

						// select column elements and loop
						List<Element> columns = table.getChild("columns", ns).getChildren("column", ns);
						for (Element column : columns) {
							String columnName = column.getChild("name", ns).getText();

							// Concatenate schema, table and column
							String lsCol = (new StringBuilder().append(schemaName).append(" / ").append(tableName)
									.append(" / ").append(columnName).toString());
							listColumns.add(lsCol);
							// concatenating Strings
						}
						listTables.add(lsTab);
						// concatenating Strings (table names)
						if (showOnWork) {
							if (onWork == 410) {
								onWork = 2;
								System.out.print("G-   ");
								System.out.print("\b\b\b\b\b");
							} else if (onWork == 110) {
								onWork = onWork + 1;
								System.out.print("G\\   ");
								System.out.print("\b\b\b\b\b");
							} else if (onWork == 210) {
								onWork = onWork + 1;
								System.out.print("G|   ");
								System.out.print("\b\b\b\b\b");
							} else if (onWork == 310) {
								onWork = onWork + 1;
								System.out.print("G/   ");
								System.out.print("\b\b\b\b\b");
							} else {
								onWork = onWork + 1;
							}
						}
					}
					listSchemas.add(lsSch);
					// concatenating Strings (schema names)
				} else {
					// kein Fehler sondern leeres schema
				}
			}
			HashSet hashSchemas = new HashSet(); // check for duplicate schemas
			for (Object value : listSchemas)
				if (!hashSchemas.add(value)) {
					valid = false;
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_G_DUPLICATE_SCHEMA, value));
					}
				}
			HashSet hashTables = new HashSet(); // check for duplicate tables
			for (Object value : listTables)
				if (!hashTables.add(value)) {
					valid = false;
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_G_DUPLICATE_TABLE, value));
					}
				}
			HashSet hashColumns = new HashSet(); // check for duplicate columns
			for (Object value : listColumns)
				if (!hashColumns.add(value)) {
					valid = false;
					if (min) {
						return false;
					} else {

						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_G_DUPLICATE_COLUMN, value));
					}
				}

		} catch (java.io.IOException ioe) {
			valid = false;
			if (min) {
				return false;
			} else {

				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD) + getTextResourceService()
								.getText(locale, ERROR_XML_UNKNOWN, ioe.getMessage() + " (IOException)"));
			}
		} catch (JDOMException e) {
			valid = false;
			if (min) {
				return false;
			} else {

				Logtxt.logtxt(logFile,
						getTextResourceService().getText(locale, MESSAGE_XML_MODUL_G_SIARD) + getTextResourceService()
								.getText(locale, ERROR_XML_UNKNOWN, e.getMessage() + " (JDOMException)"));
			}
			return valid;
		}
		return valid;
	}
}
