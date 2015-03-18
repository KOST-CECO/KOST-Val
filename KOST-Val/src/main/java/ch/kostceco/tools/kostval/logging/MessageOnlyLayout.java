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

package ch.kostceco.tools.kostval.logging;

import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;

/** Erzeugt ein vollkommen "nacktes" Layout, welches nichts als die eigentliche Message enthält.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO */
public class MessageOnlyLayout extends SimpleLayout
{

	StringBuffer	sbuf	= new StringBuffer( 128 );

	@Override
	public String format( LoggingEvent event )
	{
		sbuf.setLength( 0 );
		sbuf.append( event.getRenderedMessage() );
		sbuf.append( LINE_SEP );
		return sbuf.toString();
	}

}
