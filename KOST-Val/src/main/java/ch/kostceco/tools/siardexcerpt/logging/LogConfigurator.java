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

package ch.kostceco.tools.siardexcerpt.logging;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

public class LogConfigurator implements MessageConstants
{

	/** @author Rc Claire Roethlisberger, KOST-CECO */

	private static final ch.kostceco.tools.siardexcerpt.logging.Logger	LOGGER	= new ch.kostceco.tools.siardexcerpt.logging.Logger(
			LogConfigurator.class );

	private TextResourceServiceExc																			textResourceServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc()
	{
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc( TextResourceServiceExc textResourceServiceExc )
	{
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public String configure( String directoryOfLogfile, String nameOfLogfile )
	{

		String logFileName = directoryOfLogfile + File.separator + nameOfLogfile;
		Logger rootLogger = Logger.getRootLogger();

		MessageOnlyLayout layout = new MessageOnlyLayout();
		try {
			FileAppender logfile = new FileAppender( layout, logFileName );
			logfile.setName( "logfile" );
			logfile.setAppend( false );
			logfile.setEncoding( "UTF-8" );
			logfile.activateOptions();

			rootLogger.addAppender( logfile );

		} catch ( IOException e ) {
			LOGGER.logError( getTextResourceServiceExc().getText( ERROR_IOE,
					getTextResourceServiceExc().getText( ERROR_LOGGING_NOFILEAPPENDER ) ) );
		}

		return logFileName;
	}

}
