/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C) 
 * 2016-2022 Claire Roethlisberger (KOST-CECO)
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

package ch.kostceco.tools.siardexcerpt.excerption;

import ch.kostceco.tools.siardexcerpt.service.MessageServiceExc;
import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public abstract class ValidationModuleImpl
{

	protected final String			UNZIPDIRECTORY	= "unzipped";
	protected final String			METADATA				= "metadata.xml";
	protected final String			XSD_ARELDA			= "arelda_v3.13.2.xsd";
	protected final String			XSD_METADATA		= "metadata.xsd";

	private TextResourceServiceExc	textResourceServiceExc;
	private MessageServiceExc			messageServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc()
	{
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc( TextResourceServiceExc textResourceServiceExc )
	{
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public void setMessageServiceExc( MessageServiceExc messageServiceExc )
	{
		this.messageServiceExc = messageServiceExc;
	}

	public MessageServiceExc getMessageServiceExc()
	{
		return messageServiceExc;
	}

}
