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

package ch.kostceco.tools.kostval.service.impl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import ch.kostceco.tools.kostval.service.TextResourceService;

/** Dieser Service managt die Zugriffe auf die Resource Bundles.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */
public class TextResourceServiceImpl implements TextResourceService
{
	// Per Default ist es dieser Name, kann jedoch auch mittels Dependency Injection überschrieben
	// werden.
	private String	bundleBaseName	= "messages";

	/** Gibt den Wert des Attributs <code>bundleBaseName</code> zurück.
	 * 
	 * @return Wert des Attributs bundleBaseName. */
	public String getBundleBaseName()
	{
		return bundleBaseName;
	}

	/** Setzt den Wert des Attributs <code>bundleBaseName</code>.
	 * 
	 * @param bundleBaseName
	 *          Wert für das Attribut bundleBaseName. */
	public void setBundleBaseName( String bundleBaseName )
	{
		this.bundleBaseName = bundleBaseName;
	}

	/** {@inheritDoc} */
	public String getText( String aKey, Object... values )
	{

		// For the time being, we use the VM Default Locale
		Locale locale = Locale.getDefault();

		return this.getText( locale, aKey, values );
	}

	/** {@inheritDoc} */
	public String getText( Locale locale, String aKey, Object... values )
	{

		String theValue = ResourceBundle.getBundle( this.bundleBaseName, locale ).getString( aKey );
		return MessageFormat.format( theValue, values );
	}

}
