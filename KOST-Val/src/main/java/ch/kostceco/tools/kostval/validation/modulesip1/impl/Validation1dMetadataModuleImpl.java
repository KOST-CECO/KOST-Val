/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2016 Claire Roethlisberger (KOST-CECO), Christian
 * Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon),
 * Daniel Ludin (BEDAG AG)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.service.ConfigurationService;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;

public class Validation1dMetadataModuleImpl extends ValidationModuleImpl implements
		Validation1dMetadataModule
{

	public static String					NEWLINE	= System.getProperty( "line.separator" );

	private ConfigurationService	configurationService;

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	final int	BUFFER	= 2048;

	public boolean validate( File valDatei, File directoryOfLogfile )
			throws Validation1dMetadataException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = getConfigurationService().getShowProgressOnWork();
		/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
		 * entsprechenden Modul die property anzugeben: <property name="configurationService"
		 * ref="configurationService" /> */
		if ( onWork.equals( "no" ) ) {
			// keine Ausgabe
		} else {
			// Ausgabe SIP-Modul Ersichtlich das KOST-Val arbeitet
			System.out.print( "1D   " );
			System.out.print( "\b\b\b\b\b" );
		}

		boolean result = false;
		String sipVer = "ECH1.0.txt";
		File sipVersionFile;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new FileInputStream( new File( valDatei.getAbsolutePath()
					+ File.separator + "header" + File.separator + "metadata.xml" ) ) );
			doc.getDocumentElement().normalize();

			BufferedReader in = new BufferedReader( new FileReader( new File( valDatei.getAbsolutePath()
					+ File.separator + "header" + File.separator + "metadata.xml" ) ) );
			StringBuffer concatenatedOutputs = new StringBuffer();
			String line;
			while ( (line = in.readLine()) != null ) {

				concatenatedOutputs.append( line );
				concatenatedOutputs.append( NEWLINE );
				/* Kontrollieren, dass kein Namespace verwendet wurde wie z.B. v4:
				 * 
				 * <?xml version="1.0" encoding="UTF-8"?> <v4:paket schemaVersion="4.1"
				 * xsi:type="v4:paketSIP" xmlns:v4="http://bar.admin.ch/arelda/v4"
				 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <v4:paketTyp>SIP</v4:paketTyp>
				 * <v4:inhaltsverzeichnis> */
				if ( line.contains( "paketTyp>" ) ) {
					if ( !line.contains( "<paketTyp>" ) ) {
						// Invalider Status
						int start = line.indexOf( "<" ) + 1;
						int ns = line.indexOf( ":" ) + 1;
						int end = line.indexOf( ">" );
						String lineNode = line.substring( ns, end );
						String lineNodeNS = line.substring( start, end );
						getMessageService()
								.logError(
										getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
												+ getTextResourceService().getText( ERROR_XML_AD_NSFOUND, lineNode,
														lineNodeNS ) );
						in.close();
						return false;
					} else {
						// valider Status
						line = null;
					}
				}
			}
			in.close();

			File xmlToValidate = new File( valDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xml" );
			File xsdToValidateBar1 = new File( valDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "xsd" + File.separator + "arelda_v3.13.2.xsd" );
			File xsdToValidateEch1 = new File( valDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "xsd" + File.separator + "arelda.xsd" );

			File xsd10 = new File( "resources" + File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.0" + File.separator + "xsd" + File.separator + "arelda.xsd" );
			File xsd11 = new File( "resources" + File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.1" + File.separator + "xsd" + File.separator + "arelda.xsd" );
			File xml10 = new File( "resources" + File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.0" + File.separator + "metadata.xml" );
			File xml11 = new File( "resources" + File.separator + "header_1d" + File.separator
					+ "eCH-0160v1.1" + File.separator + "metadata.xml" );

			String allowedV4 = getConfigurationService().getAllowedVersionBar4Ech1();
			String allowedV1 = getConfigurationService().getAllowedVersionBar1();

			if ( (xmlToValidate.exists() && xsdToValidateBar1.exists()) || allowedV4.startsWith( "0" ) ) {
				// Schemavalidierung Nach Version BAR 1
				sipVer = "BAR1.txt";
				sipVersionFile = new File( directoryOfLogfile.getAbsolutePath() + File.separator + sipVer );
				try {
					sipVersionFile.createNewFile();
				} catch ( IOException e ) {
					e.printStackTrace();
				}

				try {
					System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
							"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
					DocumentBuilderFactory factoryMsg = DocumentBuilderFactory.newInstance();
					factoryMsg.setNamespaceAware( true );
					factoryMsg.setValidating( true );
					factoryMsg.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
							"http://www.w3.org/2001/XMLSchema" );
					factoryMsg.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
							xsdToValidateBar1.getAbsolutePath() );
					DocumentBuilder builderMsg = factoryMsg.newDocumentBuilder();
					ValidatorMsg handlerMsg = new ValidatorMsg();
					builderMsg.setErrorHandler( handlerMsg );
					builderMsg.parse( xmlToValidate.getAbsolutePath() );
					if ( handlerMsg.validationErrorMsg == true ) {
						result = false;
					} else {
						result = true;
					}
				} catch ( java.io.IOException ioe ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											ioe.getMessage() + " (IOException)" ) );
					result = false;
				} catch ( SAXException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											e.getMessage() + " (SAXException)" ) );
					result = false;
				} catch ( ParserConfigurationException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )

									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											e.getMessage() + " (ParserConfigurationException)" ) );
					result = false;
				}
			} else if ( (xmlToValidate.exists() && xsdToValidateEch1.exists())
					|| allowedV1.startsWith( "0" ) ) {

				/* Legende:
				 * 
				 * Osi10: Validierung ohne Fehlermeldung [Validator] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.0. Dies ist auf jeden Fall erlaubt, auch wenn 1.1 definiert wurde) mit dem Xml
				 * des SIP [xmlToValidate] und der KOST-Val xsd [xsd10]
				 * 
				 * 
				 * Oi10s: Validierung ohne Fehlermeldung [Validator] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.0. Dies ist auf jeden Fall erlaubt, auch wenn 1.1 definiert wurde) mit dem
				 * KOST-Val Xml [xml10] und der KOST-Val xsd [xsdToValidateEch1]
				 * 
				 * 
				 * Mss: Validierung mit Fehlermeldung [ValidatorMsg] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.0. Dies ist auf jeden Fall erlaubt, auch wenn 1.1 definiert wurde) mit dem Xml
				 * des SIP [xmlToValidate] und xsd des SIP [xsdToValidateEch1]
				 * 
				 * Msi10: Validierung mit Fehlermeldung [ValidatorMsg] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.0. Dies ist auf jeden Fall erlaubt, auch wenn 1.1 definiert wurde) mit dem Xml
				 * des SIP [xmlToValidate] und der KOST-Val xsd [xsd10]
				 * 
				 * 
				 * Mi10s: Validierung mit Fehlermeldung [ValidatorMsg] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.0. Dies ist auf jeden Fall erlaubt, auch wenn 1.1 definiert wurde) mit dem
				 * KOST-Val Xml [xml10] und der KOST-Val xsd [xsdToValidateEch1]
				 * 
				 * Msi11: Validierung mit Fehlermeldung [ValidatorMsg] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.1) mit dem Xml des SIP [xmlToValidate] und der KOST-Val xsd [xsd11]
				 * 
				 * 
				 * Mi11s: Validierung mit Fehlermeldung [ValidatorMsg] der Version 1.0 ( Validierung nach
				 * eCH-0160v1.1) mit dem KOST-Val Xml [xml11] und der KOST-Val xsd [xsdToValidateEch1]
				 * 
				 * 
				 * Diagramm:
				 * 
				 * <<Mss>> -valid-> <<1.1>> -false-> <<Msi10>> -valid-> <<Mi10s>> -valid-> || 1.0 VALID ||
				 * 
				 * invalid . . . . . true . . . . . . invalid-----------invalid----------> || 1.0 INVALID ||
				 * 
				 * invalid . . . . <<Osi10>> ---valid---> <<Oi10s>> ---valid---> || 1.0 VALID ||
				 * 
				 * invalid . . . . invalid <--------------invalid
				 * 
				 * invalid . . . . <<Msi11>> ---valid---> <<Mi11s>> ---valid---> || 1.1 VALID ||
				 * 
				 * invalid---------invalid----------------invalid--------------> || 1.0 & 1.1 INVALID ||
				 * 
				 * 
				 * . */

				/* Schmavalidierung nach eCH. Dies erfolgt mit einer Validierung gemäss Diagramm */

				try {
					if ( allowedV4.startsWith( "1" ) ) {
						// Start Mss
						System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
								"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
						DocumentBuilderFactory factoryMss = DocumentBuilderFactory.newInstance();
						factoryMss.setNamespaceAware( true );
						factoryMss.setValidating( true );
						factoryMss.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
								"http://www.w3.org/2001/XMLSchema" );
						// XSD-Variable
						factoryMss.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
								xsdToValidateEch1.getAbsolutePath() );
						DocumentBuilder builderMss = factoryMss.newDocumentBuilder();
						// Validator-Variable
						ValidatorMss handler = new ValidatorMss();
						builderMss.setErrorHandler( handler );
						// XML-Variable
						builderMss.parse( xmlToValidate.getAbsolutePath() );
						if ( handler.validationErrorMss == true ) {
							/* Validierungsfehler. eCH-0160v1.0 und eCH-0160v1.1 invalide. */
							sipVer = "ECH1.1.txt";
							sipVersionFile = new File( directoryOfLogfile.getAbsolutePath() + File.separator
									+ sipVer );
							try {
								sipVersionFile.createNewFile();
							} catch ( IOException e ) {
								e.printStackTrace();
							}
							builderMss.reset();
							result = false;
						} else {
							if ( !allowedV4.startsWith( "1.1" ) ) {
								builderMss.reset();
								// Nur Version 1.0 erlaubt

								// Start Msi10
								System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
										"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
								DocumentBuilderFactory factoryMsi10 = DocumentBuilderFactory.newInstance();
								factoryMsi10.setNamespaceAware( true );
								factoryMsi10.setValidating( true );
								factoryMsi10.setAttribute(
										"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
										"http://www.w3.org/2001/XMLSchema" );
								// XSD-Variable
								factoryMsi10.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
										xsd10.getAbsolutePath() );
								DocumentBuilder builderMsi10 = factoryMsi10.newDocumentBuilder();
								// Validator-Variable
								ValidatorMsi10 handlerMsi10 = new ValidatorMsi10();
								builderMsi10.setErrorHandler( handlerMsi10 );
								// XML-Variable
								builderMsi10.parse( xmlToValidate.getAbsolutePath() );
								if ( handlerMsi10.validationErrorMsi10 == true ) {
									/* Validierungsfehler. eCH-0160v1.0 invalide. */
									sipVer = "ECH1.0.txt";
									sipVersionFile = new File( directoryOfLogfile.getAbsolutePath() + File.separator
											+ sipVer );
									try {
										sipVersionFile.createNewFile();
									} catch ( IOException e ) {
										e.printStackTrace();
									}
									builderMsi10.reset();
									result = false;
								} else {
									builderMsi10.reset();

									// Start Mi10s
									System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
											"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
									DocumentBuilderFactory factoryMi10s = DocumentBuilderFactory.newInstance();
									factoryMi10s.setNamespaceAware( true );
									factoryMi10s.setValidating( true );
									factoryMi10s.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
											"http://www.w3.org/2001/XMLSchema" );
									// XSD-Variable
									factoryMi10s.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaSource",
											xsdToValidateEch1.getAbsolutePath() );
									DocumentBuilder builderMi10s = factoryMi10s.newDocumentBuilder();
									// Validator-Variable
									ValidatorMi10s handlerMi10s = new ValidatorMi10s();
									builderMi10s.setErrorHandler( handlerMi10s );
									// XML-Variable
									builderMi10s.parse( xml10.getAbsolutePath() );
									if ( handlerMi10s.validationErrorMi10s == true ) {
										/* Validierungsfehler. eCH-0160v1.0 invalide. */
										sipVer = "ECH1.0.txt";
										sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
												+ File.separator + sipVer );
										try {
											sipVersionFile.createNewFile();
										} catch ( IOException e ) {
											e.printStackTrace();
										}
										builderMi10s.reset();
										result = false;
									} else {
										/* eCH-0160v1.0 valide. */
										sipVer = "ECH1.0.txt";
										sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
												+ File.separator + sipVer );
										try {
											sipVersionFile.createNewFile();
										} catch ( IOException e ) {
											e.printStackTrace();
										}
										builderMi10s.reset();
										result = true;
									}
									// Ende Mi10s
								}
								// Ende Msi10

							} else {
								// Auch Version 1.1 erlaubt

								// Start Osi10
								System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
										"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
								DocumentBuilderFactory factoryOsi10 = DocumentBuilderFactory.newInstance();
								factoryOsi10.setNamespaceAware( true );
								factoryOsi10.setValidating( true );
								factoryOsi10.setAttribute(
										"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
										"http://www.w3.org/2001/XMLSchema" );
								// XSD-Variable
								factoryOsi10.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
										xsd10.getAbsolutePath() );
								DocumentBuilder builderOsi10 = factoryOsi10.newDocumentBuilder();
								// Validator-Variable
								ValidatorOsi10 handlerOsi10 = new ValidatorOsi10();
								builderOsi10.setErrorHandler( handlerOsi10 );
								// XML-Variable
								builderOsi10.parse( xmlToValidate.getAbsolutePath() );
								if ( handlerOsi10.validationErrorOsi10 == true ) {
									builderOsi10.reset();
									/* Validierungsfehler. eCH-0160v1.0 invalide, Validierung nach eCH-0160v1.1 */

									// Start Msi11
									System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
											"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
									DocumentBuilderFactory factoryMsi11 = DocumentBuilderFactory.newInstance();
									factoryMsi11.setNamespaceAware( true );
									factoryMsi11.setValidating( true );
									factoryMsi11.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
											"http://www.w3.org/2001/XMLSchema" );
									// XSD-Variable
									factoryMsi11.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaSource",
											xsd11.getAbsolutePath() );
									DocumentBuilder builderMsi11 = factoryMsi11.newDocumentBuilder();
									// Validator-Variable
									ValidatorMsi11 handlerMsi11 = new ValidatorMsi11();
									builderMsi11.setErrorHandler( handlerMsi11 );
									// XML-Variable
									builderMsi11.parse( xmlToValidate.getAbsolutePath() );
									if ( handlerMsi11.validationErrorMsi11 == true ) {
										/* Validierungsfehler. eCH-0160v1.1 invalide */
										sipVer = "ECH1.1.txt";
										sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
												+ File.separator + sipVer );
										try {
											sipVersionFile.createNewFile();
										} catch ( IOException e ) {
											e.printStackTrace();
										}
										builderMsi11.reset();
										result = false;
									} else {
										builderMsi11.reset();

										// Start Mi11s
										System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
												"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
										DocumentBuilderFactory factoryMi11s = DocumentBuilderFactory.newInstance();
										factoryMi11s.setNamespaceAware( true );
										factoryMi11s.setValidating( true );
										factoryMi11s.setAttribute(
												"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
												"http://www.w3.org/2001/XMLSchema" );
										// XSD-Variable
										factoryMi11s.setAttribute(
												"http://java.sun.com/xml/jaxp/properties/schemaSource",
												xsdToValidateEch1.getAbsolutePath() );
										DocumentBuilder builderMi11s = factoryMi11s.newDocumentBuilder();
										// Validator-Variable
										ValidatorMi11s handlerMi11s = new ValidatorMi11s();
										builderMi11s.setErrorHandler( handlerMi11s );
										// XML-Variable
										builderMi11s.parse( xml11.getAbsolutePath() );
										if ( handlerMi11s.validationErrorMi11s == true ) {
											/* Validierungsfehler. eCH-0160v1.1 invalide */
											sipVer = "ECH1.1.txt";
											sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
													+ File.separator + sipVer );
											try {
												sipVersionFile.createNewFile();
											} catch ( IOException e ) {
												e.printStackTrace();
											}
											builderMi11s.reset();
											result = false;
										} else {
											/* eCH-0160v1.1 valide. */
											sipVer = "ECH1.1.txt";
											sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
													+ File.separator + sipVer );
											try {
												sipVersionFile.createNewFile();
											} catch ( IOException e ) {
												e.printStackTrace();
											}
											builderMi11s.reset();
											result = true;
										}
										// Ende Mi11s
									}
									// Ende Msi11

								} else {
									builderOsi10.reset();

									// Start Oi10s
									System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
											"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
									DocumentBuilderFactory factoryOi10s = DocumentBuilderFactory.newInstance();
									factoryOi10s.setNamespaceAware( true );
									factoryOi10s.setValidating( true );
									factoryOi10s.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
											"http://www.w3.org/2001/XMLSchema" );
									// XSD-Variable
									factoryOi10s.setAttribute(
											"http://java.sun.com/xml/jaxp/properties/schemaSource",
											xsdToValidateEch1.getAbsolutePath() );
									DocumentBuilder builderOi10s = factoryOi10s.newDocumentBuilder();
									// Validator-Variable
									ValidatorOi10s handlerOi10s = new ValidatorOi10s();
									builderOi10s.setErrorHandler( handlerOi10s );
									// XML-Variable
									builderOi10s.parse( xml10.getAbsolutePath() );
									if ( handlerOi10s.validationErrorOi10s == true ) {
										builderOi10s.reset();
										/* Validierungsfehler. eCH-0160v1.0 invalide, Validierung nach eCH-0160v1.1 */

										// Start Msi11
										System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
												"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
										DocumentBuilderFactory factoryMsi11 = DocumentBuilderFactory.newInstance();
										factoryMsi11.setNamespaceAware( true );
										factoryMsi11.setValidating( true );
										factoryMsi11.setAttribute(
												"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
												"http://www.w3.org/2001/XMLSchema" );
										// XSD-Variable
										factoryMsi11.setAttribute(
												"http://java.sun.com/xml/jaxp/properties/schemaSource",
												xsd11.getAbsolutePath() );
										DocumentBuilder builderMsi11 = factoryMsi11.newDocumentBuilder();
										// Validator-Variable
										ValidatorMsi11 handlerMsi11 = new ValidatorMsi11();
										builderMsi11.setErrorHandler( handlerMsi11 );
										// XML-Variable
										builderMsi11.parse( xmlToValidate.getAbsolutePath() );
										if ( handlerMsi11.validationErrorMsi11 == true ) {
											/* Validierungsfehler. eCH-0160v1.1 invalide */
											sipVer = "ECH1.1.txt";
											sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
													+ File.separator + sipVer );
											try {
												sipVersionFile.createNewFile();
											} catch ( IOException e ) {
												e.printStackTrace();
											}
											builderMsi11.reset();
											result = false;
										} else {
											builderMsi11.reset();

											// Start Mi11s
											System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
													"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
											DocumentBuilderFactory factoryMi11s = DocumentBuilderFactory.newInstance();
											factoryMi11s.setNamespaceAware( true );
											factoryMi11s.setValidating( true );
											factoryMi11s.setAttribute(
													"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
													"http://www.w3.org/2001/XMLSchema" );
											// XSD-Variable
											factoryMi11s.setAttribute(
													"http://java.sun.com/xml/jaxp/properties/schemaSource",
													xsdToValidateEch1.getAbsolutePath() );
											DocumentBuilder builderMi11s = factoryMi11s.newDocumentBuilder();
											// Validator-Variable
											ValidatorMi11s handlerMi11s = new ValidatorMi11s();
											builderMi11s.setErrorHandler( handlerMi11s );
											// XML-Variable
											builderMi11s.parse( xml11.getAbsolutePath() );
											if ( handlerMi11s.validationErrorMi11s == true ) {
												/* Validierungsfehler. eCH-0160v1.1 invalide */
												sipVer = "ECH1.1.txt";
												sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
														+ File.separator + sipVer );
												try {
													sipVersionFile.createNewFile();
												} catch ( IOException e ) {
													e.printStackTrace();
												}
												builderMi11s.reset();
												result = false;
											} else {
												/* eCH-0160v1.1 valide. */
												sipVer = "ECH1.1.txt";
												sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
														+ File.separator + sipVer );
												try {
													sipVersionFile.createNewFile();
												} catch ( IOException e ) {
													e.printStackTrace();
												}
												builderMi11s.reset();
												result = true;
											}
											// Ende Mi11s
										}
										// Ende Msi11

									} else {
										/* eCH-0160v1.0 valide. */
										sipVer = "ECH1.0.txt";
										sipVersionFile = new File( directoryOfLogfile.getAbsolutePath()
												+ File.separator + sipVer );
										try {
											sipVersionFile.createNewFile();
										} catch ( IOException e ) {
											e.printStackTrace();
										}
										builderOi10s.reset();
										result = true;
									}
									// Ende Oi10s
								}
								// Ende Osi10

							}
						}
						// Ende Mss

					}
				} catch ( java.io.IOException ioe ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											ioe.getMessage() + " (IOException)" ) );
					result = false;
				} catch ( SAXException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											e.getMessage() + " (SAXException)" ) );
					result = false;
				} catch ( ParserConfigurationException e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )

									+ getTextResourceService().getText( ERROR_XML_UNKNOWN,
											e.getMessage() + " (ParserConfigurationException)" ) );
					result = false;
				}
			}
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}

	private class ValidatorOsi10 extends DefaultHandler
	{
		// Validierung OHNE Fehlermeldung
		public boolean	validationErrorOsi10	= false;

		public void error( SAXParseException exception ) throws SAXException
		{
			validationErrorOsi10 = true;
			// ohne Msg, da ggf Validierung nach eCH 1.1 valide ist und Fehler nach 1.0 nicht ausgegeben
			// werden d�rfen
		}

		public void fatalError( SAXParseException exception ) throws SAXException
		{
			validationErrorOsi10 = true;
			// ohne Msg, da ggf Validierung nach eCH 1.1 valide ist und Fehler nach 1.0 nicht ausgegeben
			// werden d�rfen
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

	private class ValidatorOi10s extends DefaultHandler
	{
		// Validierung OHNE Fehlermeldung
		public boolean	validationErrorOi10s	= false;

		public void error( SAXParseException exception ) throws SAXException
		{
			validationErrorOi10s = true;
			// ohne Msg, da ggf Validierung nach eCH 1.1 valide ist und Fehler nach 1.0 nicht ausgegeben
			// werden d�rfen
		}

		public void fatalError( SAXParseException exception ) throws SAXException
		{
			validationErrorOi10s = true;
			// ohne Msg, da ggf Validierung nach eCH 1.1 valide ist und Fehler nach 1.0 nicht ausgegeben
			// werden d�rfen
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

	private class ValidatorMss extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMss		= false;

		public SAXParseException	saxParseExceptionMss	= null;

		public void error( SAXParseException exceptionMss ) throws SAXException
		{
			validationErrorMss = true;
			saxParseExceptionMss = exceptionMss;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMss.getLineNumber(),
									saxParseExceptionMss.getMessage() + " (Mss)" ) );

		}

		public void fatalError( SAXParseException exceptionMss ) throws SAXException
		{
			validationErrorMss = true;
			saxParseExceptionMss = exceptionMss;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMss.getLineNumber(),
									saxParseExceptionMss.getMessage() + " (Mss)" ) );
		}

		public void warning( SAXParseException exceptionMss ) throws SAXException
		{
		}
	}

	private class ValidatorMsi10 extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMsi10		= false;

		public SAXParseException	saxParseExceptionMsi10	= null;

		public void error( SAXParseException exceptionMsi10 ) throws SAXException
		{
			validationErrorMsi10 = true;
			saxParseExceptionMsi10 = exceptionMsi10;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsi10.getLineNumber(),
									saxParseExceptionMsi10.getMessage() + " (Msi10)" ) );

		}

		public void fatalError( SAXParseException exceptionMsi10 ) throws SAXException
		{
			validationErrorMsi10 = true;
			saxParseExceptionMsi10 = exceptionMsi10;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsi10.getLineNumber(),
									saxParseExceptionMsi10.getMessage() + " (Msi10)" ) );
		}

		public void warning( SAXParseException exceptionMsi10 ) throws SAXException
		{
		}
	}

	private class ValidatorMi10s extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMi10s		= false;

		public SAXParseException	saxParseExceptionMi10s	= null;

		public void error( SAXParseException exceptionMi10s ) throws SAXException
		{
			validationErrorMi10s = true;
			saxParseExceptionMi10s = exceptionMi10s;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMi10s.getLineNumber(),
									saxParseExceptionMi10s.getMessage() + " (Mi10s)" ) );

		}

		public void fatalError( SAXParseException exceptionMi10s ) throws SAXException
		{
			validationErrorMi10s = true;
			saxParseExceptionMi10s = exceptionMi10s;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMi10s.getLineNumber(),
									saxParseExceptionMi10s.getMessage() + " (Mi10s)" ) );
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

	private class ValidatorMsi11 extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMsi11		= false;

		public SAXParseException	saxParseExceptionMsi11	= null;

		public void error( SAXParseException exceptionMsi11 ) throws SAXException
		{
			validationErrorMsi11 = true;
			saxParseExceptionMsi11 = exceptionMsi11;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsi11.getLineNumber(),
									saxParseExceptionMsi11.getMessage() + " (Msi11)" ) );

		}

		public void fatalError( SAXParseException exceptionMsi11 ) throws SAXException
		{
			validationErrorMsi11 = true;
			saxParseExceptionMsi11 = exceptionMsi11;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsi11.getLineNumber(),
									saxParseExceptionMsi11.getMessage() + " (Msi11)" ) );
		}

		public void warning( SAXParseException exceptionMsi11 ) throws SAXException
		{
		}
	}

	private class ValidatorMi11s extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMi11s		= false;

		public SAXParseException	saxParseExceptionMi11s	= null;

		public void error( SAXParseException exceptionMi11s ) throws SAXException
		{
			validationErrorMi11s = true;
			saxParseExceptionMi11s = exceptionMi11s;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMi11s.getLineNumber(),
									saxParseExceptionMi11s.getMessage() + "( Mi11s)" ) );

		}

		public void fatalError( SAXParseException exceptionMi11s ) throws SAXException
		{
			validationErrorMi11s = true;
			saxParseExceptionMi11s = exceptionMi11s;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMi11s.getLineNumber(),
									saxParseExceptionMi11s.getMessage() + " (Mi11s)" ) );
		}

		public void warning( SAXParseException exceptionMi11s ) throws SAXException
		{
		}
	}

	private class ValidatorMsg extends DefaultHandler
	{
		// Validierung mit Fehlermeldung
		public boolean						validationErrorMsg		= false;

		public SAXParseException	saxParseExceptionMsg	= null;

		public void error( SAXParseException exceptionMsg ) throws SAXException
		{
			validationErrorMsg = true;
			saxParseExceptionMsg = exceptionMsg;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsg.getLineNumber(), saxParseExceptionMsg.getMessage() ) );

		}

		public void fatalError( SAXParseException exceptionMsg ) throws SAXException
		{
			validationErrorMsg = true;
			saxParseExceptionMsg = exceptionMsg;
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMsg.getLineNumber(), saxParseExceptionMsg.getMessage() ) );
		}

		public void warning( SAXParseException exceptionMsg ) throws SAXException
		{
		}
	}

}
