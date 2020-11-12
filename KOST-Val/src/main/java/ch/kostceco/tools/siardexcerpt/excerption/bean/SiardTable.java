/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2019 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.excerption.bean;

import java.util.List;

import org.jdom2.Element;

public class SiardTable
{

	private String				tableName;
	private Element				tableRootElement;
	private Element				tableXSDRootElement;
	private List<Element>	metadataXMLElements;
	private List<Element>	tableXSDElements;
	private List<Element>	tableXMLElements;

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName( String tableName )
	{
		this.tableName = tableName;
	}

	public List<Element> getMetadataXMLElements()
	{
		return metadataXMLElements;
	}

	public void setMetadataXMLElements( List<Element> metadataXMLElements )
	{
		this.metadataXMLElements = metadataXMLElements;
	}

	public List<Element> getTableXSDElements()
	{
		return tableXSDElements;
	}

	public void setTableXSDElements( List<Element> tableXSDElements )
	{
		this.tableXSDElements = tableXSDElements;
	}

	public List<Element> getTableXMLElements()
	{
		return tableXMLElements;
	}

	public void setTableXMLElements( List<Element> tableXMLElements )
	{
		this.tableXMLElements = tableXMLElements;
	}

	/** @return the tableRootElement */
	public Element getTableRootElement()
	{
		return tableRootElement;
	}

	/** @param tableRootElement
	 *          the tableRootElement to set */
	public void setTableRootElement( Element tableRootElement )
	{
		this.tableRootElement = tableRootElement;
	}

	/** @return the tableXSDRootElement */
	public Element getTableXSDRootElement()
	{
		return tableXSDRootElement;
	}

	/** @param tableXSDRootElement
	 *          the tableXSDRootElement to set */
	public void setTableXSDRootElement( Element tableXSDRootElement )
	{
		this.tableXSDRootElement = tableXSDRootElement;
	}

}
