/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire RÃ¶thlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kostval.exception.modulesip3.Validation3dPeriodException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip3.Validation3dPeriodModule;

/** Stimmen die Zeitangaben in (metadata.xml)/ablieferung überein? */

public class Validation3dPeriodModuleImpl extends ValidationModuleImpl implements
		Validation3dPeriodModule
{

	DateFormat	formatter1	= new SimpleDateFormat( "yyyy-MM-dd" );
	DateFormat	formatter2	= new SimpleDateFormat( "dd.MM.yyyy" );
	DateFormat	formatter3	= new SimpleDateFormat( "yyyy" );

	@Override
	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation3dPeriodException
	{
		// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
		System.out.print( "3D   " );
		System.out.print( "\r" );
		int onWork = 41;

		boolean valid = true;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream( new File( valDatei.getAbsolutePath()
					+ "//header//metadata.xml" ) ) );
			doc.normalize();

			// Lesen der Werte vom Entstehungszeitraum der Ablieferung
			XPath xpath = XPathFactory.newInstance().newXPath();
			Element elementAblDatumVon = null;
			Element elementAblDatumBis = null;
			Element elementAblCaVon = null;
			Element elementAblCaBis = null;

			elementAblDatumVon = (Element) xpath.evaluate(
					"/paket/ablieferung/entstehungszeitraum/von/datum", doc, XPathConstants.NODE );

			elementAblDatumBis = (Element) xpath.evaluate(
					"/paket/ablieferung/entstehungszeitraum/bis/datum", doc, XPathConstants.NODE );

			elementAblCaVon = (Element) xpath.evaluate( "/paket/ablieferung/entstehungszeitraum/von/ca",
					doc, XPathConstants.NODE );

			elementAblCaBis = (Element) xpath.evaluate( "/paket/ablieferung/entstehungszeitraum/bis/ca",
					doc, XPathConstants.NODE );

			/* Wenn nachträglich nicht geändert ist der Ablieferungs-entstehungszeitraum brauchbar, d.h.
			 * er soll mit dem Zeitraum vom Dossier validiert werden. */
			boolean dateAblieferungUseable = true;

			Calendar calAblieferungVon = Calendar.getInstance();
			Calendar calAblieferungBis = Calendar.getInstance();

			// Existiert das "Ablieferungsdatum Von"?
			if ( elementAblDatumVon != null ) {

				/* Das elementAblDatumVon existiert und wird gemäss dem Subprogramm parseDatumVon in ein
				 * Datum umgewandelt und validiert */
				Date date = parseDatumVon( elementAblDatumVon.getTextContent() );

				// das umgewandelte Datum wird als calAblieferungVon übernommen
				calAblieferungVon.setTime( date );
				if ( date == null ) {

					// Umwandlung respektive Validierung fehlgeschlagen
					valid = false;
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
									+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
					return false;
				}

				// Existiert das "Ablieferungsdatum Bis"?
				if ( elementAblDatumBis != null ) {

					/* Das elementAblDatumBis existiert und wird gemäss dem Subprogramm parseDatumBis in ein
					 * Datum umgewandelt und validiert */
					Date dateB = parseDatumBis( elementAblDatumBis.getTextContent() );

					// das umgewandelte Datum wird als calAblieferungBis übernommen
					calAblieferungBis.setTime( dateB );
					if ( dateB == null ) {

						// Umwandlung respektive Validierung fehlgeschlagen
						valid = false;
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
						return false;
					}

					/* der String datumVon respektive datumBis enthält die reelle Eingabe des
					 * Entstehungszeitraums Von und Bis und wird für den allfälligen Fehlerlog benötigt */
					String datumVon = ((elementAblCaVon != null && elementAblCaVon.getTextContent().equals(
							"true" )) ? "ca. " : "")
							+ elementAblDatumVon.getTextContent();
					String datumBis = ((elementAblCaBis != null && elementAblCaBis.getTextContent().equals(
							"true" )) ? "ca. " : "")
							+ elementAblDatumBis.getTextContent();

					/* Liegt eines der Daten in der Zukunft? calNow2 muss gesetzt und verwendet werden
					 * (=jetzt), weil ansonsten manchmal einen Fehler ausgegeben wird, weil ein andere
					 * calNow-Wert wegen wenigen Sekunden in der Zukunft liegt */
					Calendar calNow2 = Calendar.getInstance();

					if ( calAblieferungVon.after( calNow2 ) ) {

						// Der Von-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid
						valid = false;

						/* Weil der Von-Wert in der Zukunft liegt, ist es auch nicht sinnvoll der
						 * Ablieferungs-entstehungszeitraum mit dem Zeitraum vom Dossier zu validieren.
						 * Entsprechend wird es auf "unbrauchbar" gesetzt. */
						dateAblieferungUseable = false;

						// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_DATUM_IN_FUTURE, datumVon ) );
					}

					if ( calAblieferungBis.after( calNow2 ) ) {

						// Der Bis-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid
						valid = false;

						/* Weil der Bis-Wert in der Zukunft liegt, ist es auch nicht sinnvoll der
						 * Ablieferungs-entstehungszeitraum mit dem Zeitraum vom Dossier zu validieren.
						 * Entsprechend wird es auf "unbrauchbar" gesetzt. */
						dateAblieferungUseable = false;

						// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_DATUM_IN_FUTURE, datumBis ) );
					}

					/* falls das Ablieferungs-Datum "Bis" vor dem Datum "Von" liegt, gehen wir davon aus, das
					 * eine Verwechslung vorliegt, d.h. wir geben den Fehler aus, Schritt 3d ist invalid und
					 * für die weiterverarbeitung tauschen die calDaten gegeneinander aus. */
					if ( calAblieferungBis.before( calAblieferungVon ) ) {
						Calendar calTmp = calAblieferungBis;
						calAblieferungBis = calAblieferungVon;
						calAblieferungVon = calTmp;

						// Zusammenstellung der parameter die für den logreport gebraucht werden (die reellen
						// Daten)
						String[] params = new String[4];
						params[0] = (elementAblCaVon != null && elementAblCaVon.getTextContent()
								.equals( "true" )) ? "ca. " : "";
						params[1] = elementAblDatumVon.getTextContent();
						params[2] = (elementAblCaBis != null && elementAblCaBis.getTextContent()
								.equals( "true" )) ? "ca. " : "";
						params[3] = elementAblDatumBis.getTextContent();

						// Log-Ausgabe mit den vertauschten Werte
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_INVALID_ABLIEFERUNG_RANGE,
												(Object[]) params ) );

						// 3d wird auf invalid gesetzt
						valid = false;
					}

				} else {

					/* Das elementAblDatumBis existiert nicht Dies bedeutet, dass dieser Schritt noch immer
					 * Valid sein könnte, da der Entstehungszeitraum auf der Stufe Ablieferung optional ist.
					 * Es Soll kein Fehler ausgegeben werden sondern nur der Marker dateAblieferungUsable =
					 * false (unbrauchbar) gesetzt werden. */
					dateAblieferungUseable = false;
				}

			} else {

				/* Das elementAblDatumVon existiert nicht Dies bedeutet, dass dieser Schritt noch immer
				 * Valid sein könnte, da der Entstehungszeitraum auf der Stufe Ablieferung optional ist. Es
				 * Soll kein Fehler ausgegeben werden sondern nur der Marker dateAblieferungUsable = false
				 * (unbrauchbar) gesetzt werden. */
				dateAblieferungUseable = false;
			}

			// Ende der Zeitraumvalidierung auf der Stufe Ablieferung
			// ******************************************************
			// Start mit der Zeitraumvalidierung auf der Stufe Dossier
			// mit allfälliger Validierung gegenüber jener der Ablieferung

			// über alle Dossiers iterieren
			boolean noDateValidation = false;
			NodeList nodeLstDossier = doc.getElementsByTagName( "dossier" );
			for ( int s = 0; s < nodeLstDossier.getLength(); s++ ) {
				Node dossierNode = nodeLstDossier.item( s );

				// Lesen der Werte vom Entstehungszeitraum der Dossier

				String dateDossierVon = null;
				String dateDossierBis = null;
				Node circaDossierVonNode = null;
				Node circaDossierBisNode = null;

				NodeList childNodesDos = dossierNode.getChildNodes();
				for ( int y = 0; y < childNodesDos.getLength(); y++ ) {
					Node subNodeDos = childNodesDos.item( y );
					if ( subNodeDos.getNodeName().equals( "entstehungszeitraum" ) ) {
						NodeList childNodesDosVon = subNodeDos.getChildNodes();
						for ( int yD = 0; yD < childNodesDosVon.getLength(); yD++ ) {
							Node subNodeDosVon = childNodesDosVon.item( yD );
							if ( subNodeDosVon.getNodeName().equals( "von" ) ) {
								NodeList childNodesDosVonDC = subNodeDosVon.getChildNodes();
								for ( int yV = 0; yV < childNodesDosVonDC.getLength(); yV++ ) {
									Node subNodeDosVonDC = childNodesDosVonDC.item( yV );
									if ( subNodeDosVonDC.getNodeName().equals( "datum" ) ) {
										dateDossierVon = subNodeDosVonDC.getTextContent();
									} else if ( subNodeDosVonDC.getNodeName().equals( "ca" ) ) {
										circaDossierVonNode = subNodeDosVonDC;
									}
								}
							} else if ( subNodeDosVon.getNodeName().equals( "bis" ) ) {
								NodeList childNodesDosBisDC = subNodeDosVon.getChildNodes();
								for ( int yB = 0; yB < childNodesDosBisDC.getLength(); yB++ ) {
									Node subNodeDosBisDC = childNodesDosBisDC.item( yB );
									if ( subNodeDosBisDC.getNodeName().equals( "datum" ) ) {
										dateDossierBis = subNodeDosBisDC.getTextContent();
									} else if ( subNodeDosBisDC.getNodeName().equals( "ca" ) ) {
										circaDossierBisNode = subNodeDosBisDC;
									}
								}
							}
						}
					}
				}

				// selectNodeIterator ist zu Zeitintensiv bei grossen XML-Dateien mit getChildNodes()
				// ersetzt

				// Existiert das "Dossierdatum Von und Bis"?
				Calendar calDossierVon = Calendar.getInstance();
				Calendar calDossierBis = Calendar.getInstance();
				boolean dossierRangeOk = true;
				if ( dateDossierVon != null && dateDossierBis != null ) {

					/* dateDossierVon existiert und wird geäss dem Subprogramm parseDatumVon in ein Datum
					 * umgewandelt und validiert */
					Date date = parseDatumVon( dateDossierVon );

					// Umwandlung respektive Validierung fehlgeschlagen
					if ( date == null ) {
						valid = false;
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
						return false;
					}

					// das umgewandelte Datum wird als calDossierVon übernommen
					calDossierVon.setTime( date );

					/* dateDossierBis existiert und wird gemäss dem Subprogramm parseDatumBis in ein Datum
					 * umgewandelt und validiert */
					date = parseDatumBis( dateDossierBis );

					// Umwandlung respektive Validierung fehlgeschlagen
					if ( date == null ) {
						valid = false;
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
						return false;
					}

					// das umgewandelte Datum wird als calDossierBis übernommen
					calDossierBis.setTime( date );

					/* der String datumVonDos respektive datumBisDos enthält die reelle Eingabe des
					 * Entstehungszeitraums Von und Bis und wird ür den allfälligen Fehlerlog benötigt */
					String datumVonDos = ((circaDossierVonNode != null && circaDossierVonNode
							.getTextContent().equals( "true" )) ? "ca. " : "") + dateDossierVon;
					String datumBisDos = ((circaDossierBisNode != null && circaDossierBisNode
							.getTextContent().equals( "true" )) ? "ca. " : "") + dateDossierBis;

					/* Liegt eines der Daten in der Zukunft? calNow3 muss gesetzt und verwendet werden
					 * (=jetzt), weil ansonsten manchmal einen Fehler ausgegeben wird, weil ein andere
					 * calNow-Wert wegen wenigen Sekunden in der Zukunft liegt */
					Calendar calNow3 = Calendar.getInstance();

					if ( calDossierVon.after( calNow3 ) ) {

						/* Der Von-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid */
						valid = false;

						/* Weil der Von-Wert in der Zukunft liegt, ist es auch nicht sinnvoll der
						 * Dossier-entstehungszeitraum mit dem Zeitraum vom Dokument zu validieren. Entsprechend
						 * wird es auf "nicht ok" gesetzt. */
						dossierRangeOk = false;

						// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
												+ getTextResourceService().getText( ERROR_XML_CD_DATUM_IN_FUTURE,
														datumVonDos ) );
					}

					if ( calDossierBis.after( calNow3 ) ) {

						// Der Bis-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid
						valid = false;

						/* Weil der Bis-Wert in der Zukunft liegt, ist es auch nicht sinnvoll der
						 * Dossier-entstehungszeitraum mit dem Zeitraum vom Dokument zu validieren. Entsprechend
						 * wird es auf "nicht ok" gesetzt. */
						dossierRangeOk = false;

						// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
												+ getTextResourceService().getText( ERROR_XML_CD_DATUM_IN_FUTURE,
														datumBisDos ) );
					}

					/* falls das Dossier-Datum "Bis" vor dem Datum "Von" liegt, gehen wir davon aus, das eine
					 * Verwechslung vorliegt, d.h. wir geben den Fehler aus, Schritt 3d ist invalid und für
					 * die weiterverarbeitung tauschen die calDaten gegeneinander aus. */
					if ( calDossierBis.before( calDossierVon ) ) {
						Calendar calTmp = calDossierBis;
						calDossierBis = calDossierVon;
						calDossierVon = calTmp;

						Element dossierElement = null;
						dossierElement = (Element) dossierNode;
						String dossierId = dossierElement.getAttribute( "id" );

						// Zusammenstellung der parameter die für den logreport gebraucht werden (die reellen
						// Daten)
						String[] params = new String[5];
						params[0] = dossierId;
						params[1] = (circaDossierVonNode != null && circaDossierVonNode.getTextContent()
								.equals( "true" )) ? "ca. " : "";
						params[2] = dateDossierVon;
						params[3] = (circaDossierBisNode != null && circaDossierBisNode.getTextContent()
								.equals( "true" )) ? "ca. " : "";
						params[4] = dateDossierBis;

						// Log-Ausgabe mit den vertauschten Werte
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
										+ getTextResourceService().getText( ERROR_XML_CD_INVALID_DOSSIER_RANGE_CA,
												(Object[]) params ) );

						// 3d wird auf invalid gesetzt
						valid = false;
					}

					// Validierung der Zeiträume gegenüber jener der Ablieferung
					// ---------------------------------------------------------

					/* nur wenn ein der Ablieferungszeitraum brauchbar ist, wird ein Dossierzeitraum darauf
					 * geprüft, dass er in diesen hineinpasst. */
					if ( dateAblieferungUseable ) {
						// "keine Angabe" auf Stufe Dossier wird mit dem calWert von der Ablieferung gesetzt
						if ( dateDossierVon.equals( "keine Angabe" ) ) {
							calDossierVon = calAblieferungVon;
							dateDossierVon = elementAblDatumVon.getTextContent();
							circaDossierVonNode = elementAblCaVon;
						}
						if ( dateDossierBis.equals( "keine Angabe" ) ) {
							calDossierBis = calAblieferungBis;
							dateDossierBis = elementAblDatumBis.getTextContent();
							circaDossierBisNode = elementAblCaBis;
						}

						/* Wenn DossierVon vor AblieferungVon oder DossierBis nach AblieferungBis liegt dann
						 * liegt der Dossierzeitraum nicht innerhalb des Ablieferungszeitraums -> Fehler */
						if ( (calDossierVon.before( calAblieferungVon ) || calDossierBis
								.after( calAblieferungBis )) ) {

							Element dossierElement = null;
							dossierElement = (Element) dossierNode;
							String dossierId = dossierElement.getAttribute( "id" );

							/* Zusammenstellung der parameter die für den logreport gebraucht werden (die reellen
							 * Daten) */
							String[] params = new String[9];
							params[0] = dossierId;
							params[1] = (circaDossierVonNode != null && circaDossierVonNode.getTextContent()
									.equals( "true" )) ? "ca. " : "";
							params[2] = dateDossierVon;
							params[3] = (circaDossierBisNode != null && circaDossierBisNode.getTextContent()
									.equals( "true" )) ? "ca. " : "";
							params[4] = dateDossierBis;
							params[5] = (elementAblCaVon != null && elementAblCaVon.getTextContent().equals(
									"true" )) ? "ca. " : "";
							params[6] = elementAblDatumVon.getTextContent();
							params[7] = (elementAblCaBis != null && elementAblCaBis.getTextContent().equals(
									"true" )) ? "ca. " : "";
							params[8] = elementAblDatumBis.getTextContent();

							// Log-Ausgabe mit den entsprechenden Werte
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
											+ getTextResourceService().getText(
													ERROR_XML_CD_INVALID_DOSSIER_RANGE_CA_ABL, (Object[]) params ) );

							dossierRangeOk = false;
							// 3d wird auf invalid gesetzt
							valid = false;
						}
					}

				}

				if ( !dossierRangeOk ) {
					// kein gültiger Dossierzeitraum vorhanden
					if ( dateAblieferungUseable ) {
						/* wir haben keinen gültigen Dossierzeitraum, jedoch Ablieferungszeitraum und verwenden
						 * diesen als Validierungszeitraum für untergeordnete Dokumente */
						calDossierVon = calAblieferungVon;
						calDossierBis = calAblieferungBis;
					} else {
						/* wir haben weder einen gültigen Dossier- noch Ablieferungs-Zeitraum, allfällige
						 * untergeordnete Dokumente werden also nicht gegenüber der oberen Ebene validiert. */
						noDateValidation = true;
					}
				}

				// Ende der Zeitraumvalidierung auf der Stufe Dossier
				// ******************************************************
				// Start mit der Zeitraumvalidierung auf der Stufe Dokument
				// mit allfälliger Validierung gegenüber jener der Dossier

				// Lesen der Werte vom Entstehungszeitraum der Dokumente

				Node dokEntstehungszeitraumNode = null;

				NodeList childNodesDok = dossierNode.getChildNodes();
				for ( int y = 0; y < childNodesDok.getLength(); y++ ) {
					Node subNodeDok = childNodesDok.item( y );
					if ( subNodeDok.getNodeName().equals( "dokument" ) ) {
						NodeList childNodesDokEzr = subNodeDok.getChildNodes();
						for ( int yV = 0; yV < childNodesDokEzr.getLength(); yV++ ) {
							Node subNodeDokEzr = childNodesDokEzr.item( yV );
							if ( subNodeDokEzr.getNodeName().equals( "entstehungszeitraum" ) ) {
								dokEntstehungszeitraumNode = subNodeDokEzr;

								/* selectNodeIterator ist zu Zeitintensiv bei grossen XML-Dateien mit
								 * getChildNodes() ersetzt
								 * 
								 * id des Dokument-Nodes ermitteln */
								Node dokNode = dokEntstehungszeitraumNode.getParentNode();
								Element dokElement = null;
								dokElement = (Element) dokNode;
								String dokumentId = dokElement.getAttribute( "id" );

								Node nodeVon = null;
								Node nodeBis = null;
								Node nodeCaVon = null;
								Node nodeCaBis = null;

								NodeList childNodesDokVon = dokEntstehungszeitraumNode.getChildNodes();
								for ( int yD = 0; yD < childNodesDokVon.getLength(); yD++ ) {
									Node subNodeDokVon = childNodesDokVon.item( yD );
									if ( subNodeDokVon.getNodeName().equals( "von" ) ) {
										NodeList childNodesDokVonDC = subNodeDokVon.getChildNodes();
										for ( int yB = 0; yB < childNodesDokVonDC.getLength(); yB++ ) {
											Node subNodeDokVonDC = childNodesDokVonDC.item( yB );
											if ( subNodeDokVonDC.getNodeName().equals( "datum" ) ) {
												nodeVon = subNodeDokVonDC;
											} else if ( subNodeDokVonDC.getNodeName().equals( "ca" ) ) {
												nodeCaVon = subNodeDokVonDC;
											}
										}
									} else if ( subNodeDokVon.getNodeName().equals( "bis" ) ) {
										NodeList childNodesDokBisDC = subNodeDokVon.getChildNodes();
										for ( int yB = 0; yB < childNodesDokBisDC.getLength(); yB++ ) {
											Node subNodeDokBisDC = childNodesDokBisDC.item( yB );
											if ( subNodeDokBisDC.getNodeName().equals( "datum" ) ) {
												nodeBis = subNodeDokBisDC;
											} else if ( subNodeDokBisDC.getNodeName().equals( "ca" ) ) {
												nodeCaBis = subNodeDokBisDC;
											}
										}
									}
									if ( onWork == 41 ) {
										onWork = 2;
										System.out.print( "3D-   " );
										System.out.print( "\r" );
									} else if ( onWork == 11 ) {
										onWork = 12;
										System.out.print( "3D\\   " );
										System.out.print( "\r" );
									} else if ( onWork == 21 ) {
										onWork = 22;
										System.out.print( "3D|   " );
										System.out.print( "\r" );
									} else if ( onWork == 31 ) {
										onWork = 32;
										System.out.print( "3D/   " );
										System.out.print( "\r" );
									} else {
										onWork = onWork + 1;
									}
								}

								/* selectNodeIterator ist zu Zeitintensiv bei grossen XML-Dateien mit
								 * getChildNodes() ersetzt */

								Date dateDokVon = null;

								// Existiert das "Dokumentdatum Von"?
								if ( nodeVon != null && nodeVon.getTextContent() != null ) {

									/* dateDokVon existiert und wird gemäss dem Subprogramm parseDatumVon in ein Datum
									 * umgewandelt und validiert */
									dateDokVon = parseDatumVon( nodeVon.getTextContent() );
									if ( dateDokVon == null ) {

										// Umwandlung respektive Validierung fehlgeschlagen
										valid = false;
										getMessageService().logError(
												getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
														+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
										return false;
									}

								}

								// das umgewandelte Datum wird als calDokVon übernommen
								Calendar calDokVon = Calendar.getInstance();
								calDokVon.setTime( dateDokVon );

								Date dateDokBis = null;

								// Existiert das "Dokumentdatum Bis"?
								if ( nodeBis != null && nodeBis.getTextContent() != null ) {

									/* dateDokBis existiert und wird gemäss dem Subprogramm parseDatumVon in ein Datum
									 * umgewandelt und validiert */
									dateDokBis = parseDatumBis( nodeBis.getTextContent() );
									if ( dateDokBis == null ) {

										// Umwandlung respektive Validierung fehlgeschlagen
										valid = false;
										getMessageService().logError(
												getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
														+ getTextResourceService().getText( ERROR_XML_CD_UNPARSEABLE_DATE ) );
										return false;
									}
								}

								// das umgewandelte Datum wird als calDokBis übernommen
								Calendar calDokBis = Calendar.getInstance();
								calDokBis.setTime( dateDokBis );

								/* Liegt eines der Daten in der Zukunft? calNow4 muss gesetzt und verwendet werden
								 * (=jetzt), weil ansonsten manchmal einen Fehler ausgegeben wird, weil ein andere
								 * calNow-Wert wegen wennigen Sekunden in der Zukunft liegt */
								Calendar calNow4 = Calendar.getInstance();

								if ( calDokVon.after( calNow4 ) ) {

									/* Der Von-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid */
									valid = false;

									/* Zusammenstellung der parameter die für den logreport gebraucht werden (die
									 * reellen Daten) */
									String[] params = new String[3];
									params[0] = dokumentId;
									params[1] = nodeCaVon == null ? "" : "ca. ";
									params[2] = nodeVon.getTextContent();

									// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
									getMessageService().logError(
											getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
													+ getTextResourceService().getText(
															ERROR_XML_CD_DATUM_ENTSTEHUNG_IN_FUTURE, (Object[]) params ) );
								}

								// liegt das Datum "Entstehungszeitraum bis" in der Zukunft?
								if ( calDokBis.after( calNow4 ) ) {

									// Der Von-Wert liegt nach jetzt und ist entsprechend in der Zukunft = invalid
									valid = false;

									/* Zusammenstellung der parameter die für den logreport gebraucht werden (die
									 * reellen Daten) */
									String[] params = new String[3];
									params[0] = dokumentId;
									params[1] = nodeCaBis == null ? "" : "ca. ";
									params[2] = nodeBis.getTextContent();

									// Log-Ausgabe mit dem Wert, welcher in der Zukunft liegt
									getMessageService().logError(
											getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
													+ getTextResourceService().getText(
															ERROR_XML_CD_DATUM_ENTSTEHUNG_IN_FUTURE, (Object[]) params ) );
								}

								/* falls das Dokument-Datum "Bis" vor dem Datum "Von" liegt, gehen wir davon aus,
								 * das eine Verwechslung vorliegt, d.h. wir geben den Fehler aus, Schritt 3d ist
								 * invalid und für die weiterverarbeitung tauschen die calDaten gegeneinander aus. */
								if ( calDokBis.before( calDokVon ) ) {
									Calendar calTmp = calDokBis;
									calDokBis = calDokVon;
									calDokVon = calTmp;

									/* Zusammenstellung der parameter die für den logreport gebraucht werden (die
									 * reellen Daten) */
									String[] params = new String[5];
									params[0] = dokumentId;
									params[1] = nodeCaVon == null ? "" : "ca. ";
									params[2] = nodeVon.getTextContent();
									params[3] = nodeCaBis == null ? "" : "ca. ";
									params[4] = nodeBis.getTextContent();

									// Log-Ausgabe mit den vertauschten Werte
									getMessageService().logError(
											getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
													+ getTextResourceService().getText(
															ERROR_XML_CD_INVALID_DOSSIER_RANGE_CA, (Object[]) params ) );

									// 3d wird auf invalid gesetzt
									valid = false;
								}

								// Validierung der Zeiträume gegenüber jener der Dossier
								// ---------------------------------------------------------

								/* nur wenn ein Zeitraum brauchbar ist, wird ein Dokumentzeitraum darauf geprüft,
								 * dass er in diesen hineinpasst. */
								if ( !noDateValidation ) {
									// "keine Angabe" auf Stufe Dok wird mit dem calWert vom Dossier gesetzt
									if ( nodeVon.equals( "keine Angabe" ) ) {
										calDokVon = calDossierVon;
									}
									if ( nodeBis.equals( "keine Angabe" ) ) {
										calDokBis = calDossierBis;
									}

									/* Wenn DokVon vor DossierVon oder DokBis nach DossierBis liegt dann liegt der
									 * Dossierzeitraum nicht innerhalb des Ablieferungszeitraums -> Fehler */
									if ( (calDokVon.before( calDossierVon ) || calDokBis.after( calDossierBis )) ) {

										/* Zusammenstellung der parameter die für den logreport gebraucht werden (die
										 * reellen Daten) */

										String[] params = new String[9];
										params[0] = dokumentId;
										params[1] = nodeCaVon == null ? "" : "ca. ";
										params[2] = nodeVon.getTextContent();
										params[3] = nodeCaBis == null ? "" : "ca. ";
										params[4] = nodeBis.getTextContent();
										params[5] = (circaDossierVonNode != null && circaDossierVonNode
												.getTextContent().equals( "true" )) ? "ca. " : "";
										params[6] = dateDossierVon;
										params[7] = (circaDossierBisNode != null && circaDossierBisNode
												.getTextContent().equals( "true" )) ? "ca. " : "";
										params[8] = dateDossierBis;

										// Log-Ausgabe mit den entsprechenden Werte
										getMessageService().logError(
												getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
														+ getTextResourceService().getText(
																ERROR_XML_CD_INVALID_DOKUMENT_RANGE_CA, (Object[]) params ) );

										// 3d wird auf invalid gesetzt
										valid = false;
									}
								}

							}
						}
					}
				}

				// Ende der Zeitraumvalidierung auf der Stufe Dokument

				if ( onWork == 41 ) {
					onWork = 2;
					System.out.print( "3D-   " );
					System.out.print( "\r" );
				} else if ( onWork == 11 ) {
					onWork = 12;
					System.out.print( "3D\\   " );
					System.out.print( "\r" );
				} else if ( onWork == 21 ) {
					onWork = 22;
					System.out.print( "3D|   " );
					System.out.print( "\r" );
				} else if ( onWork == 31 ) {
					onWork = 32;
					System.out.print( "3D/   " );
					System.out.print( "\r" );
				} else {
					onWork = onWork + 1;
				}
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Cd_SIP )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return valid;
	}

	/** Diese Methode generiert aus einem String ein Datum, der String kann folgende Werte haben:
	 * 2007-09-30 => 30.09.2007 2007 => 31.12.2007 "keine Angabe" => aktuelles Datum
	 * 
	 * @param sDate
	 *          der Datumsstring aus dem Entstehungszeitraum in metadata.xml
	 * @return das umgewandelte Datum */
	private Date parseDatumBis( String sDate )
	{
		Date date = null;

		// "keine Angabe"
		if ( sDate.equals( "keine Angabe" ) ) {
			date = new Date();
			return date;
		}

		// "2007"
		if ( sDate.length() == 4 ) {
			int year = Integer.parseInt( sDate );
			Calendar endOfYear = new GregorianCalendar( year, Calendar.DECEMBER, 31 );
			date = endOfYear.getTime();
			Calendar calNow = Calendar.getInstance();
			Calendar endOfYearJan = new GregorianCalendar( year, Calendar.JANUARY, 1 );
			if ( endOfYear.after( calNow ) ) {
				if ( endOfYearJan.before( calNow ) ) {
					date = calNow.getTime();
					return date;
				}
				return date;
			}
			return date;
		}

		// "2007-09-30"
		try {
			date = (Date) formatter1.parse( sDate );
			return date;
		} catch ( ParseException e ) {
			return null;
		}
	}

	/** Diese Methode generiert aus einem String ein Datum, der String kann folgende Werte haben:
	 * 2007-09-30 => 30.09.2007 2007 => 01.01.2007 "keine Angabe" => 01.01.0000
	 * 
	 * @param sDate
	 *          der Datumsstring aus dem Entstehungszeitraum in metadata.xml
	 * @return das umgewandelte Datum */
	private Date parseDatumVon( String sDate )
	{
		Date date = null;

		// "keine Angabe"
		if ( sDate.equals( "keine Angabe" ) ) {
			Calendar earliestPossibleDate = new GregorianCalendar( 0, Calendar.JANUARY, 1 );
			date = earliestPossibleDate.getTime();
			return date;
		}

		// "2007"
		if ( sDate.length() == 4 ) {
			int year = Integer.parseInt( sDate );
			Calendar endOfYear = new GregorianCalendar( year, Calendar.JANUARY, 1 );
			date = endOfYear.getTime();
			return date;
		}

		// "2007-09-30"
		try {
			date = (Date) formatter1.parse( sDate );
			return date;
		} catch ( ParseException e ) {
			return null;
		}
	}

	public static void main( String[] args )
	{
		Validation3dPeriodModuleImpl v3d = new Validation3dPeriodModuleImpl();

		String sDate = "keine Angabe";
		v3d.parseDatumVon( sDate );
	}
}
