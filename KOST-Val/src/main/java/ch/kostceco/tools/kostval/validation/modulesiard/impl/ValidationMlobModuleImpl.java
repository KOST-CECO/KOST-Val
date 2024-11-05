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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.kostceco.tools.kosttools.fileservice.Recognition;
import ch.kostceco.tools.kosttools.fileservice.Sed;
import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.kostval.exception.modulesiard.ValidationMlobException;
import ch.kostceco.tools.kostval.logging.Logtxt;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesiard.ValidationMlobModule;

/**
 * Validierungsschritt M (LOB)
 * 
 * a) Kontrolle der Dateiendung (je nach Konfig Warnung oder Fehler)
 * 
 * b) Kontrolle der erlaubten Dateiformate
 * 
 * c) Validierung der LOB-Dateien (noch nicht umgesetzt ist komplizierter
 * umzusetzten)
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public class ValidationMlobModuleImpl extends ValidationModuleImpl implements ValidationMlobModule {
	private boolean min = false;

	Map<String, String> filesInSiardUnsorted = new HashMap<String, String>();
	Map<String, String> lobsInSiard = new HashMap<String, String>();

	@Override
	public boolean validate(File valDatei, File directoryOfLogfile, Map<String, String> configMap, Locale locale,
			File logFile, String dirOfJarPath) throws ValidationMlobException {
		filesInSiardUnsorted.clear();
		lobsInSiard.clear();

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
			System.out.print("M    ");
			System.out.print("\b\b\b\b\b");
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
		}
		String warningEn = "Warning: ";
		String warningDe = "Warnung: ";
		String warningFr = "Avertissement : ";
		String warningIt = "Avviso: ";
		String warning = "";
		String warningEl = "";

		boolean valid = true;
		try {
			String pathToWorkDir = configMap.get("PathToWorkDir");
			pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
			String contentString = new StringBuilder(pathToWorkDir).append(File.separator).append("content").toString();
			File content = new File(contentString);
			HashMap<String, File> hashMap = new HashMap<String, File>();
			Map<String, File> fileMap = Util.getContent(content, hashMap);
			Set<String> fileMapKeys = fileMap.keySet();
			// alle Dateien in SIARD in die Map filesInSiard schreiben (Inhalt)
			for (Iterator<String> iterator = fileMapKeys.iterator(); iterator.hasNext();) {
				String entryName = iterator.next();
				// System.out.println( "entryName: " + entryName );
				// entryName: content/schema1/table7/table7.xsd
				if (!entryName.equalsIgnoreCase(contentString)) {
					filesInSiardUnsorted.put(entryName, entryName);
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("M-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("M\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("M|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("M/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			}

			lobsInSiard = new TreeMap<String, String>(filesInSiardUnsorted);

			// Pfad zum Programm existiert die Dateien?
			String checkTool = Sed.checkSed(dirOfJarPath);
			if (!checkTool.equals("OK")) {
				// mindestens eine Datei fehlt fuer die Validierung
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
									+ getTextResourceService().getText(locale, MESSAGE_XML_MISSING_FILE, checkTool,
											getTextResourceService().getText(locale, ABORTED)));
					return false;
				}
			}

			try {
				// entfernen der tables.Dateien und co damit nur noch lobs in
				// der Map enthalten ist

				// Struktur aus metadata.xml herauslesen (path)
				pathToWorkDir = configMap.get("PathToWorkDir");
				pathToWorkDir = pathToWorkDir + File.separator + "SIARD";
				File metadataXml = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("header")
						.append(File.separator).append("metadata.xml").toString());
				InputStream fin = new FileInputStream(metadataXml);
				SAXBuilder builder = new SAXBuilder();
				Document document = (Document) builder.build(fin);
				fin.close();
				// set to null
				fin = null;

				Boolean version1 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
				Boolean version2 = FileUtils.readFileToString(metadataXml, "ISO-8859-1")
						.contains("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
				Namespace ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd");
				if (version1) {
					// ns = Namespace.getNamespace(
					// "http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" );
				} else {
					if (version2) {
						ns = Namespace.getNamespace("http://www.bar.admin.ch/xmlns/siard/2/metadata.xsd");
					} else {
						valid = false;
						if (min) {
							return false;
						} else {

							Logtxt.logtxt(logFile,
									getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
											+ getTextResourceService().getText(locale, MESSAGE_XML_D_INVALID_XMLNS,
													metadataXml));
						}
					}
				}
				// select schema elements and loop
				List<Element> schemas = ((org.jdom2.Document) document).getRootElement().getChild("schemas", ns)
						.getChildren("schema", ns);
				for (Element schema : schemas) {
					valid = validateSchema(schema, ns, pathToWorkDir, configMap);
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("M-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("M\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("M|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("M/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} catch (java.io.IOException ioe) {
				valid = false;
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											ioe.getMessage() + " (IOException)"));
				}
			} catch (JDOMException e) {
				valid = false;
				if (min) {
					return false;
				} else {

					Logtxt.logtxt(logFile,
							getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
									+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN,
											e.getMessage() + " (JDOMException)"));
				}
			}

			if (lobsInSiard.size() > 0) {
				// <lobExtension>Error </lobExtension> <!--Warning / Error -->
				// <lobAzepted>Check </lobAzepted> <!--"" / Check -->
				String lobExtensionError = configMap.get("lobExtension");
				if (!lobExtensionError.contains("Error")) {
					warning = warningDe;
					warningEl = "<Warning>warning</Warning>";
					if (locale.toString().startsWith("fr")) {
						warning = warningFr;
					} else if (locale.toString().startsWith("it")) {
						warning = warningIt;
					} else if (locale.toString().startsWith("en")) {
						warning = warningEn;
					} else {
						warning = warningDe;
					}
				}

				String lobAzeptedCheck = configMap.get("lobAzepted");

				// Info, welche Formate validiert oder akzeptiert werden
				String pdfaValidation = configMap.get("pdfaValidation");
				String txtValidation = configMap.get("txtValidation");
				String pdfValidation = configMap.get("pdfValidation");
				String jp2Validation = configMap.get("jp2Validation");
				String jpegValidation = configMap.get("jpegValidation");
				String tiffValidation = configMap.get("tiffValidation");
				String pngValidation = configMap.get("pngValidation");
				String flacValidation = configMap.get("flacValidation");
				String waveValidation = configMap.get("waveValidation");
				String mp3Validation = configMap.get("mp3Validation");
				String mkvValidation = configMap.get("mkvValidation");
				String mp4Validation = configMap.get("mp4Validation");
				String xmlValidation = configMap.get("xmlValidation");
				String jsonValidation = configMap.get("jsonValidation");
				String siardValidation = configMap.get("siardValidation");
				String csvValidation = configMap.get("csvValidation");
				String xlsxValidation = configMap.get("xlsxValidation");
				String odsValidation = configMap.get("odsValidation");
				String otherformats = configMap.get("otherformats");

				try {
					// in lobsInSiard sind jetzt noch die drinnen, welche LOBs
					// sind

					String msgRec = "";
					String msgUnknown = "";
					String msgNotAz = "";
					String msgAz = "";
					String oldParentRec = "";
					String oldParentUnknown = "";
					String oldParentNotaz = "";
					int counterDetailDirUnknown = 1;
					int counterDetailDirNotaz = 1;

					Set<String> filesInSiardKeys = lobsInSiard.keySet();
					for (Iterator<String> iterator = filesInSiardKeys.iterator(); iterator.hasNext();) {
						String entryName = iterator.next();
						File entryNameFile = new File(entryName);
						File entryNameParent = entryNameFile.getParentFile();
						if (entryNameFile.isDirectory()) {
							// muss nicht erkannt werden
						} else {
							// Formaterkennung der Datei
							String recFormat = "new";
							recFormat = Recognition.formatRec(entryNameFile);
							String extRec = recFormat.replace("_ext", "");

							// TODO: A) Kontrolle der Dateiendung (je nach
							// Konfig Warnung oder Fehler)
							if (recFormat.contains("_ext")) {
								// Erkannt aber nicht exakte Dateiendung
								if (lobExtensionError.contains("Error")) {
									valid = false;
									if (min) {
										return false;
									}
								}
								// Aufzaehlung der anders erkannten LOB-Dateien
								// Fehlermeldung pro ordner mit Auzaehlung aller
								// LOB-Nmen pro Format in diesem ordner
								if (entryNameParent.getAbsolutePath().equals(oldParentRec)) {
									// bestehender Ordner, Fehlermeldung
									// erweitern
									if (msgRec.contains(" = " + extRec)) {
										// Fehlermeldung bestehendes Format
										// erweitern
										msgRec = msgRec.replace(" = " + extRec,
												", " + entryNameFile.getName() + " = " + extRec);
									} else {
										// bestehender Ordner aber neues Format
										msgRec = msgRec + " </Message><Message> - " + entryNameFile.getName() + " = "
												+ extRec;
									}
								} else {
									// neuer Ordner

									// Fehlermeldung nicht leer (anderer Ordner)
									// diese ausgeben und zuruecksetzen
									if (!msgRec.equals("")) {
										String oldParentRecShort = oldParentRec.replace(pathToWorkDir, "");
										// val.message.xml.m.az.incorrectfile
										// Die Erkennung folgender LOB-Datei
										// fuer den Orner {0} ergab ein anderes
										// Resulat: {1}
										Logtxt.logtxt(logFile,
												getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
														+ getTextResourceService().getText(locale,
																MESSAGE_XML_M_AZ_INCORRECTFILE, oldParentRecShort,
																msgRec, warning, warningEl));
										msgRec = "";
									}

									// neuer Ordner => neues Format
									msgRec = " </Message><Message> - " + entryNameFile.getName() + " = " + extRec;

								}
								oldParentRec = entryNameParent.getAbsolutePath();
							} else if (recFormat.contains("UNKNOWN_") || recFormat.contains("_")) {
								// Wurde nicht erkannt (oder es gab ein Problem)
								if (lobExtensionError.contains("Error")) {
									valid = false;
									if (min) {
										return false;
									}
								}
								// Aufzaehlung der nicht erkannten
								// LOB-Dateien

								// nur max 3 pro ordner mit zaehler weitere
								// Dateien in diesem ordner
								if (entryNameParent.getAbsolutePath().equals(oldParentUnknown)) {
									if (counterDetailDirUnknown == 3) {
										msgUnknown = msgUnknown + " ... [+" + (counterDetailDirUnknown - 2) + "] Temp";
									} else if (counterDetailDirUnknown > 3) {
										// letzter zaehler mit Temp anpassen
										msgUnknown = msgUnknown.replace((counterDetailDirUnknown - 3) + "] Temp",
												(counterDetailDirUnknown - 2) + "] Temp");
									} else {
										msgUnknown = msgUnknown + ", " + entryNameFile.getName();
									}
									counterDetailDirUnknown = counterDetailDirUnknown + 1;
								} else {
									// neuer Ordner

									// letzter zaehler mit Temp das Temp
									// loeschen
									msgUnknown = msgUnknown.replace("] Temp", "]");
									counterDetailDirUnknown = 1;
									msgUnknown = msgUnknown + " </Message><Message> - "
											+ entryNameParent.getAbsolutePath().replace(pathToWorkDir, "") + ": "
											+ entryNameFile.getName();
								}
								oldParentUnknown = entryNameParent.getAbsolutePath();
							} else {
								// Exakte Dateienduung
								/*
								 * System.out.println( "Exakte Dateiendung der LOB-Datei: " + entryNameShort +
								 * "  =  " + recFormat );
								 */
							}

							// TODO: B) Kontrolle der erlaubten Dateiformate
							if (lobAzeptedCheck.contains("Check")) {
								// weitere Kontrolle der LOB-Dateien
								String recMsg = "AZ";

								// Text
								if (recFormat.contains("PDFA")) {
									if (pdfaValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("TXT")) {
									if (txtValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("PDF")) {
									if (pdfValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}

									// Bilder
								} else if (recFormat.contains("JP2")) {
									if (jp2Validation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("JPEG")) {
									if (jpegValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("TIFF")) {
									if (tiffValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("PNG")) {
									if (pngValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}

									// Audio
								} else if (recFormat.contains("FLAC")) {
									if (flacValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("WAVE")) {
									if (waveValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("MP3")) {
									if (mp3Validation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}

									// Video
								} else if (recFormat.contains("MKV")) {
									if (mkvValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("MP4")) {
									if (mp4Validation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}

									// Daten
								} else if (recFormat.contains("XML")) {
									if (xmlValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("JSON")) {
									if (jsonValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("SIARD")) {
									if (siardValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("CSV")) {
									if (csvValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("XLSX")) {
									if (xlsxValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								} else if (recFormat.contains("ODS")) {
									if (odsValidation.equals("no")) {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}

								} else {
									// Kontrolle weitere Formate

									String otherExt = recFormat.replace("UNKNOWN_.", "");
									if (otherformats.contains(otherExt)) {
										// Format akzeptiert
										recMsg = "AZ";
									} else {
										// NICHT akzeptiert -> invalid
										recMsg = "notAZ";
									}
								}
								if (recMsg.equals("notAZ")) {
									// LOB-Datei wird nicht akzeptiert
									valid = false;
									if (min) {
										return false;
									}
									// Aufzaehlung der nicht akzeptierten
									// LOB-Dateien

									// nur max 3 pro ordner mit zaehler weitere
									// Dateien in diesem ordner
									if (entryNameParent.getAbsolutePath().equals(oldParentNotaz)) {
										if (counterDetailDirNotaz == 3) {
											msgNotAz = msgNotAz + " ... [+" + (counterDetailDirNotaz - 2) + "] Temp";
										} else if (counterDetailDirNotaz > 3) {
											// letzter zaehler mit Temp anpassen
											msgNotAz = msgNotAz.replace((counterDetailDirNotaz - 3) + "] Temp",
													(counterDetailDirNotaz - 2) + "] Temp");
										} else {
											msgNotAz = msgNotAz + ", " + entryNameFile.getName();
										}
										counterDetailDirNotaz = counterDetailDirNotaz + 1;
									} else {
										// neuer Ordner

										// letzter zaehler mit Temp das Temp
										// loeschen
										msgNotAz = msgNotAz.replace("] Temp", "]");
										counterDetailDirNotaz = 1;
										msgNotAz = msgNotAz + " </Message><Message> - "
												+ entryNameParent.getAbsolutePath().replace(pathToWorkDir, "") + ": "
												+ entryNameFile.getName();
									}
									oldParentNotaz = entryNameParent.getAbsolutePath();
								} else {
									// LOB-Datei wird akzeptiert
									// Aufzaehlung der akzeptierten LOB-Formaten
									String extAz = extRec;
									extAz = extAz.replace("UNKNOWN_.", "");

									if (!msgAz.contains(extAz)) {
										msgAz = msgAz + " </Message><Message> - " + extAz;
									}
								}
							} // End Check
						}
					}

					// TODO: Ausgabe der vorbereiteten Fehlermeldungen
					if (!msgRec.equals("")) {
						String oldParentRecShort = oldParentRec.replace(pathToWorkDir, "");
						// val.message.xml.m.az.incorrectfile
						// Die Erkennung folgender LOB-Datei fuer den Orner {0}
						// ergab ein anderes Resulat: {1}
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
										+ getTextResourceService().getText(locale, MESSAGE_XML_M_AZ_INCORRECTFILE,
												oldParentRecShort, msgRec, warning, warningEl));
					}
					if (!msgUnknown.equals("")) {
						// Wurde nicht erkannt (oder es gab ein Problem)

						// letzter zaehler mit Temp das Temp loeschen
						msgUnknown = msgUnknown.replace("] Temp", "]");

						// val.message.xml.m.unknown
						// Das Dateiformat der LOB-Datei konnte
						// nicht erkannt werden. dann aufzaehlung
						Logtxt.logtxt(logFile,
								getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
										+ getTextResourceService().getText(locale, MESSAGE_XML_M_UNKNOWN, msgUnknown,
												warning, warningEl));
					}
					if (!msgNotAz.equals("")) {
						// Wurde nicht erkannt (oder es gab ein Problem)

						// letzter zaehler mit Temp das Temp loeschen
						msgNotAz = msgNotAz.replace("] Temp", "]");

						// val.message.xml.m.notaz
						// Die LOB-Datei wird nicht akzeptiert.
						// - {0] dann aufzaehlung
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_M_NOTAZ, msgNotAz));
					}
					if (!msgAz.equals("")) {
						// Information mit den akzeptierten und vorhandenen
						// LOB-Formaten

						// val.message.xml.m.az
						// Information: Die SIARD-Datei enthaelt folgende
						// akzeptierte LOB-Formate.
						// - {0] dann aufzaehlung
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
								+ getTextResourceService().getText(locale, MESSAGE_XML_M_AZ, msgAz));
					}
				} catch (Exception e) {
					if (min) {
					} else {
						Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
								+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, (e.getMessage() + " 2")));
						// return false;
					}
				}
			} else {
				// System.out.println( "keine LOBs vorhanden" );
				valid = true;
			}
		} catch (Exception e) {
			if (min) {
			} else {
				Logtxt.logtxt(logFile, getTextResourceService().getText(locale, MESSAGE_XML_MODUL_M_SIARD)
						+ getTextResourceService().getText(locale, ERROR_XML_UNKNOWN, (e.getMessage() + " 3")));
			} // return false;
		}
		filesInSiardUnsorted.clear();
		lobsInSiard.clear();

		return valid;

	}

	private boolean validateSchema(Element schema, Namespace ns, String pathToWorkDir, Map<String, String> configMap) {
		boolean showOnWork = true;
		int onWork = 410;
		// Informationen zur Darstellung "onWork" holen
		String onWorkConfig = configMap.get("ShowProgressOnWork");
		/*
		 * Nicht vergessen in
		 * "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property
		 * name="configurationService" ref="configurationService" />
		 */
		if (onWorkConfig.equals("no")) {
			// keine Ausgabe
			showOnWork = false;
		} else if (onWorkConfig.equals("nomin")) {
			min = true;
			// keine Ausgabe
			showOnWork = false;
		} else {
			if (onWork == 410) {
				onWork = 2;
				System.out.print("M-   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 110) {
				onWork = onWork + 1;
				System.out.print("M\\   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 210) {
				onWork = onWork + 1;
				System.out.print("M|   ");
				System.out.print("\b\b\b\b\b");
			} else if (onWork == 310) {
				onWork = onWork + 1;
				System.out.print("M/   ");
				System.out.print("\b\b\b\b\b");
			} else {
				onWork = onWork + 1;
			}
		}
		boolean valid = true;
		Element schemaFolder = schema.getChild("folder", ns);
		File schemaPath = new File(new StringBuilder(pathToWorkDir).append(File.separator).append("content")
				.append(File.separator).append(schemaFolder.getText()).toString());
		String schemaPathString = schemaPath.toString();
		lobsInSiard.remove(schemaPathString);

		if (schemaPath.isDirectory()) {
			if (schema.getChild("tables", ns) != null) {

				List<Element> tables = schema.getChild("tables", ns).getChildren("table", ns);
				for (Element table : tables) {
					String name = "";
					Element tableFolder = table.getChild("folder", ns);
					File tablePath = new File(new StringBuilder(schemaPath.getPath()).append(File.separator)
							.append(tableFolder.getText()).toString());
					name = tablePath.toString();
					lobsInSiard.remove(name);
					// die Datei "name" aus filesInSiard entfernen
					// System.out.println( "Remove von metadata.xml: " + name );

					if (tablePath.isDirectory()) {
						File tableXsd = new File(new StringBuilder(tablePath.getPath()).append(File.separator)
								.append(tableFolder.getText() + ".xsd").toString());
						name = tableXsd.toString();
						// die Datei "name" aus filesInSiard entfernen
						lobsInSiard.remove(name);

						File tableXml = new File(new StringBuilder(tablePath.getPath()).append(File.separator)
								.append(tableFolder.getText() + ".xml").toString());
						name = tableXml.toString();
						// die Datei "name" aus filesInSiard entfernen
						lobsInSiard.remove(name);

					}
					if (showOnWork) {
						if (onWork == 410) {
							onWork = 2;
							System.out.print("M-   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 110) {
							onWork = onWork + 1;
							System.out.print("M\\   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 210) {
							onWork = onWork + 1;
							System.out.print("M|   ");
							System.out.print("\b\b\b\b\b");
						} else if (onWork == 310) {
							onWork = onWork + 1;
							System.out.print("M/   ");
							System.out.print("\b\b\b\b\b");
						} else {
							onWork = onWork + 1;
						}
					}
				}
			} else {
				// Kein Fehler sondern leeres Schema
			}
		}
		return valid;
	}
}
