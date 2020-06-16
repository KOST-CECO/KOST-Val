/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2020 Claire Roethlisberger (KOST-CECO), Christian
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
import java.util.Locale;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.kostceco.tools.kostval.KOSTVal;
import ch.kostceco.tools.kostval.exception.modulesip1.Validation1dMetadataException;
import ch.kostceco.tools.kostval.validation.ValidationModuleImpl;
import ch.kostceco.tools.kostval.validation.modulesip1.Validation1dMetadataModule;

public class Validation1dMetadataModuleImpl extends ValidationModuleImpl
		implements Validation1dMetadataModule
{

	public static String	NEWLINE	= System.getProperty( "line.separator" );

	final int							BUFFER	= 2048;

	public boolean validate( File valDatei, File directoryOfLogfile, Map<String, String> configMap,
			Locale locale ) throws Validation1dMetadataException
	{
		// Informationen zur Darstellung "onWork" holen
		String onWork = configMap.get( "ShowProgressOnWork" );
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
		String sipVer = "ECH160_1.0.txt";
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
								.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
										+ getTextResourceService().getText( locale, ERROR_XML_AD_NSFOUND, lineNode,
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
			File xsdToValidateEch160 = new File( valDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "xsd" + File.separator + "arelda.xsd" );

			/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
			 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit
			 * 
			 * dirOfJarPath + File.separator +
			 * 
			 * erweitern. */
			String path = new java.io.File(
					KOSTVal.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
							.getAbsolutePath();
			path = path.substring( 0, path.lastIndexOf( "." ) );
			path = path + System.getProperty( "java.class.path" );
			String locationOfJarPath = path;
			String dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}

			File xsd10 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.0" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xsd11 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.1" + File.separator + "xsd" + File.separator
					+ "arelda.xsd" );
			File xml10 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.0" + File.separator + "metadata.xml" );
			File xml11 = new File( dirOfJarPath + File.separator + "resources" + File.separator
					+ "header_1d" + File.separator + "eCH-0160v1.1" + File.separator + "metadata.xml" );

			System.out.println (dirOfJarPath+" "+xsd10.getAbsolutePath()+" "+xsd11.getAbsolutePath()+" "+xml10.getAbsolutePath()+" "+xml11.getAbsolutePath());
			File xmlIntern = xml10;
			File xsdIntern = xsd10;

			if ( (xmlToValidate.exists() && xsdToValidateEch160.exists()) ) {
				/* eCH-0160_v1.1 enthält in arelda.xsd neu "vorgangAktivitaet" */
				try {
					Scanner scanner = new Scanner( xsdToValidateEch160 );

					// Datei Zeile für Zeile lesen und ermitteln ob "vorgangAktivitaet" darin enthalten ist
					while ( scanner.hasNextLine() ) {
						String lineArelda = scanner.nextLine();
						if ( lineArelda.contains( "vorgangAktivitaet" ) ) {
							// es ist eine eCH-0160 v1.1
							xmlIntern = xml11;
							xsdIntern = xsd11;
							sipVer = "ECH160_1.1.txt";
						}
					}
					scanner.close();

					// ins log eine txt anlegen mit der Version
					sipVersionFile = new File(
							directoryOfLogfile.getAbsolutePath() + File.separator + sipVer );
					try {
						sipVersionFile.createNewFile();
					} catch ( IOException e ) {
						e.printStackTrace();
					}

				} catch ( FileNotFoundException e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_J_SIARD )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											"FileNotFoundException" ) );
				} catch ( Exception e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_J_SIARD )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											(e.getMessage() + " 1") ) ); //
					return false;
				}

				try {
					// Start Validierung SIP-xml mit SIP-xsd [ss]
					System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
							"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
					DocumentBuilderFactory factoryMss = DocumentBuilderFactory.newInstance();
					factoryMss.setNamespaceAware( true );
					factoryMss.setValidating( true );
					factoryMss.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
							"http://www.w3.org/2001/XMLSchema" );
					// XSD-Variable
					factoryMss.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
							xsdToValidateEch160.getAbsolutePath() );
					DocumentBuilder builderMss = factoryMss.newDocumentBuilder();
					// Validator-Variable
					ValidatorMss handler = new ValidatorMss();
					builderMss.setErrorHandler( handler );
					// XML-Variable
					builderMss.parse( xmlToValidate.getAbsolutePath() );
					if ( handler.validationErrorMss == true ) {
						/* Validierungsfehler [ss]. invalide.
						 * 
						 * es braucht keine zusätzliche Validierung mit den internen xml und xsd */

						builderMss.reset();
						result = false;
					} else {
						// [ss] valide. jetzt erfolgt die Validierung mit den interen xsd und xml
						builderMss.reset();

						// [si] SIP-xml mit Intern-xsd
						System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
								"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
						DocumentBuilderFactory factoryMsi = DocumentBuilderFactory.newInstance();
						factoryMsi.setNamespaceAware( true );
						factoryMsi.setValidating( true );
						factoryMsi.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
								"http://www.w3.org/2001/XMLSchema" );
						// XSD-Variable
						factoryMsi.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
								xsdIntern.getAbsolutePath() );
						DocumentBuilder builderMsi = factoryMsi.newDocumentBuilder();
						// Validator-Variable
						ValidatorMsi handlerMsi = new ValidatorMsi();
						builderMsi.setErrorHandler( handlerMsi );
						// XML-Variable
						builderMsi.parse( xmlToValidate.getAbsolutePath() );
						if ( handlerMsi.validationErrorMsi == true ) {
							/* Validierungsfehler [si]. invalide.
							 * 
							 * es braucht keine zusätzliche Validierung mit den internen xml */
							builderMsi.reset();
							result = false;
						} else {
							builderMsi.reset();

							// [is] Intern-xml mit SIP-xsd
							System.setProperty( "javax.xml.parsers.DocumentBuilderFactory",
									"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" );
							DocumentBuilderFactory factoryMis = DocumentBuilderFactory.newInstance();
							factoryMis.setNamespaceAware( true );
							factoryMis.setValidating( true );
							factoryMis.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
									"http://www.w3.org/2001/XMLSchema" );
							// XSD-Variable
							factoryMis.setAttribute( "http://java.sun.com/xml/jaxp/properties/schemaSource",
									xsdToValidateEch160.getAbsolutePath() );
							DocumentBuilder builderMis = factoryMis.newDocumentBuilder();
							// Validator-Variable
							ValidatorMis handlerMis = new ValidatorMis();
							builderMis.setErrorHandler( handlerMis );
							// XML-Variable
							builderMis.parse( xmlIntern.getAbsolutePath() );
							if ( handlerMis.validationErrorMis == true ) {
								/* Validierungsfehler [is]. invalide. */
								builderMis.reset();
								result = false;
							} else {
								/* valide. */
								builderMis.reset();
								result = true;
							}
							// Ende is
						}
						// Ende si

					}
					// Ende ss

				} catch ( java.io.IOException ioe ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											ioe.getMessage() + " (IOException)" ) );
					result = false;
				} catch ( SAXException e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											e.getMessage() + " (SAXException)" ) );
					result = false;
				} catch ( ParserConfigurationException e ) {
					getMessageService()
							.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )

									+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN,
											e.getMessage() + " (ParserConfigurationException)" ) );
					result = false;
				}
			}
		} catch ( Exception e ) {
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}

	private class ValidatorMss extends DefaultHandler
	{
		public boolean						validationErrorMss		= false;

		public SAXParseException	saxParseExceptionMss	= null;

		@SuppressWarnings("unused")
		public void error( SAXParseException exceptionMss, Locale locale ) throws SAXException
		{
			validationErrorMss = true;
			saxParseExceptionMss = exceptionMss;
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMss.getLineNumber(),
									saxParseExceptionMss.getMessage() + " (Mss)" ) );

		}

		@SuppressWarnings("unused")
		public void fatalError( SAXParseException exceptionMss, Locale locale ) throws SAXException
		{
			validationErrorMss = true;
			saxParseExceptionMss = exceptionMss;
			getMessageService()
					.logError( getTextResourceService().getText( locale, MESSAGE_XML_MODUL_Ad_SIP )
							+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
									saxParseExceptionMss.getLineNumber(),
									saxParseExceptionMss.getMessage() + " (Mss)" ) );
		}

		public void warning( SAXParseException exceptionMss ) throws SAXException
		{
		}
	}

	private class ValidatorMsi extends DefaultHandler
	{
		public boolean						validationErrorMsi		= false;

		public SAXParseException	saxParseExceptionMsi	= null;

		@SuppressWarnings("unused")
		public void error( SAXParseException exceptionMsi, Locale locale ) throws SAXException
		{
			validationErrorMsi = true;
			saxParseExceptionMsi = exceptionMsi;
			getMessageService().logError( getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
							saxParseExceptionMsi.getLineNumber(), saxParseExceptionMsi.getMessage() + " (si)" ) );

		}

		@SuppressWarnings("unused")
		public void fatalError( SAXParseException exceptionMsi, Locale locale ) throws SAXException
		{
			validationErrorMsi = true;
			saxParseExceptionMsi = exceptionMsi;
			getMessageService().logError( getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
							saxParseExceptionMsi.getLineNumber(), saxParseExceptionMsi.getMessage() + " (si)" ) );
		}

		public void warning( SAXParseException exceptionMsi ) throws SAXException
		{
		}
	}

	private class ValidatorMis extends DefaultHandler
	{
		public boolean						validationErrorMis		= false;

		public SAXParseException	saxParseExceptionMis	= null;

		@SuppressWarnings("unused")
		public void error( SAXParseException exceptionMis, Locale locale ) throws SAXException
		{
			validationErrorMis = true;
			saxParseExceptionMis = exceptionMis;
			getMessageService().logError( getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
							saxParseExceptionMis.getLineNumber(), saxParseExceptionMis.getMessage() + " (is)" ) );

		}

		@SuppressWarnings("unused")
		public void fatalError( SAXParseException exceptionMis, Locale locale ) throws SAXException
		{
			validationErrorMis = true;
			saxParseExceptionMis = exceptionMis;
			getMessageService().logError( getTextResourceService().getText( locale,
					MESSAGE_XML_MODUL_Ad_SIP )
					+ getTextResourceService().getText( locale, ERROR_XML_AD_METADATA_ERRORS,
							saxParseExceptionMis.getLineNumber(), saxParseExceptionMis.getMessage() + " (is)" ) );
		}

		public void warning( SAXParseException exception ) throws SAXException
		{
		}
	}

}
