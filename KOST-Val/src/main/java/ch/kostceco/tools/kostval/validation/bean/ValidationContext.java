/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2-Files and Submission
 * Information Package (SIP). Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian
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

package ch.kostceco.tools.kostval.validation.bean;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import ch.kostceco.tools.kostval.service.ConfigurationService;

public class ValidationContext
{
	/* Validation parameters */
	private boolean								verboseMode;
	private ConfigurationService	configurationService;
	/* Validation context related properties */
	private Properties						validationProperties;
	/* Content of the SIARD package */
	private File									siardArchive;
	private HashMap<String, File>	siardFiles;
	private File									metadataXML;
	private Document							metadataXMLDocument;
	private String								contentPath;
	private String								headerPath;
	/* SIARD XML processing related properties */
	private List<Element>					xmlElements;
	private List<Element>					xsdElements;
	private List<String>					xmlElementsSequence;
	private List<String>					xsdElementsSequence;
	private List<SiardTable>			siardTables;
	private SiardTable						siardTable;
	/* General XML related properties for JDOM Access */
	private String								namespaceURI;
	private String								xmlPrefix;
	private String								xsdPrefix;
	private Namespace							xmlNamespace;
	private Namespace							xsdNamespace;

	/** @return the verboseMode */
	public boolean isVerboseMode()
	{
		return verboseMode;
	}

	/** @param verboseMode
	 *          the verboseMode to set */
	public void setVerboseMode( boolean verboseMode )
	{
		this.verboseMode = verboseMode;
	}

	/** @return the validationProperties */
	public Properties getValidationProperties()
	{
		return validationProperties;
	}

	/** @param validationProperties
	 *          the validationProperties to set */
	public void setValidationProperties( Properties validationProperties )
	{
		this.validationProperties = validationProperties;
	}

	/** @return the siardFiles */
	public HashMap<String, File> getSiardFiles()
	{
		return siardFiles;
	}

	/** @param siardFiles
	 *          the siardFiles to set */
	public void setSiardFiles( HashMap<String, File> siardFiles )
	{
		this.siardFiles = siardFiles;
	}

	/** @return the metadataXML */
	public File getMetadataXML()
	{
		return metadataXML;
	}

	/** @param metadataXML
	 *          the metadataXML to set */
	public void setMetadataXML( File metadataXML )
	{
		this.metadataXML = metadataXML;
	}

	/** @return the metadataXMLDocument */
	public Document getMetadataXMLDocument()
	{
		return metadataXMLDocument;
	}

	/** @param metadataXMLDocument
	 *          the metadataXMLDocument to set */
	public void setMetadataXMLDocument( Document metadataXMLDocument )
	{
		this.metadataXMLDocument = metadataXMLDocument;
	}

	/** @return the contentPath */
	public String getContentPath()
	{
		return contentPath;
	}

	/** @param contentPath
	 *          the contentPath to set */
	public void setContentPath( String contentPath )
	{
		this.contentPath = contentPath;
	}

	/** @return the headerPath */
	public String getHeaderPath()
	{
		return headerPath;
	}

	/** @param headerPath
	 *          the headerPath to set */
	public void setHeaderPath( String headerPath )
	{
		this.headerPath = headerPath;
	}

	/** @return the xmlElements */
	public List<Element> getXmlElements()
	{
		return xmlElements;
	}

	/** @param xmlElements
	 *          the xmlElements to set */
	public void setXmlElements( List<Element> xmlElements )
	{
		this.xmlElements = xmlElements;
	}

	/** @return the xsdElements */
	public List<Element> getXsdElements()
	{
		return xsdElements;
	}

	/** @param xsdElements
	 *          the xsdElements to set */
	public void setXsdElements( List<Element> xsdElements )
	{
		this.xsdElements = xsdElements;
	}

	/** @return the xmlElementsSequence */
	public List<String> getXmlElementsSequence()
	{
		return xmlElementsSequence;
	}

	/** @param xmlElementsSequence
	 *          the xmlElementsSequence to set */
	public void setXmlElementsSequence( List<String> xmlElementsSequence )
	{
		this.xmlElementsSequence = xmlElementsSequence;
	}

	/** @return the xsdElementsSequence */
	public List<String> getXsdElementsSequence()
	{
		return xsdElementsSequence;
	}

	/** @param xsdElementsSequence
	 *          the xsdElementsSequence to set */
	public void setXsdElementsSequence( List<String> xsdElementsSequence )
	{
		this.xsdElementsSequence = xsdElementsSequence;
	}

	/** @return the siardTables */
	public List<SiardTable> getSiardTables()
	{
		return siardTables;
	}

	/** @param siardTables
	 *          the siardTables to set */
	public void setSiardTables( List<SiardTable> siardTables )
	{
		this.siardTables = siardTables;
	}

	/** @return the siardTable */
	public SiardTable getSiardTable()
	{
		return siardTable;
	}

	/** @param siardTable
	 *          the siardTable to set */
	public void setSiardTable( SiardTable siardTable )
	{
		this.siardTable = siardTable;
	}

	/** @return the namespaceURI */
	public String getNamespaceURI()
	{
		return namespaceURI;
	}

	/** @param namespaceURI
	 *          the namespaceURI to set */
	public void setNamespaceURI( String namespaceURI )
	{
		this.namespaceURI = namespaceURI;
	}

	/** @return the xmlPrefix */
	public String getXmlPrefix()
	{
		return xmlPrefix;
	}

	/** @param xmlPrefix
	 *          the xmlPrefix to set */
	public void setXmlPrefix( String xmlPrefix )
	{
		this.xmlPrefix = xmlPrefix;
	}

	/** @return the xsdPrefix */
	public String getXsdPrefix()
	{
		return xsdPrefix;
	}

	/** @param xsdPrefix
	 *          the xsdPrefix to set */
	public void setXsdPrefix( String xsdPrefix )
	{
		this.xsdPrefix = xsdPrefix;
	}

	/** @return the xmlNamespace */
	public Namespace getXmlNamespace()
	{
		return xmlNamespace;
	}

	/** @param xmlNamespace
	 *          the xmlNamespace to set */
	public void setXmlNamespace( Namespace xmlNamespace )
	{
		this.xmlNamespace = xmlNamespace;
	}

	/** @return the xsdNamespace */
	public Namespace getXsdNamespace()
	{
		return xsdNamespace;
	}

	/** @param xsdNamespace
	 *          the xsdNamespace to set */
	public void setXsdNamespace( Namespace xsdNamespace )
	{
		this.xsdNamespace = xsdNamespace;
	}

	/** @return the siardArchive */
	public File getSiardArchive()
	{
		return siardArchive;
	}

	/** @param siardArchive
	 *          the siardArchive to set */
	public void setSiardArchive( File siardArchive )
	{
		this.siardArchive = siardArchive;
	}

	/** @return the configurationService */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/** @param configurationService
	 *          the configurationService to set */
	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}
}
