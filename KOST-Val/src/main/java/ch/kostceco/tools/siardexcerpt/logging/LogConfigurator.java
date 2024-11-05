/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ch.kostceco.tools.siardexcerpt.service.TextResourceServiceExc;

public class LogConfigurator implements MessageConstants {

	private TextResourceServiceExc textResourceServiceExc;

	public TextResourceServiceExc getTextResourceServiceExc() {
		return textResourceServiceExc;
	}

	public void setTextResourceServiceExc(TextResourceServiceExc textResourceServiceExc) {
		this.textResourceServiceExc = textResourceServiceExc;
	}

	public String configure(String directoryOfLogfile, String nameOfLogfile) {

		String logFileName = directoryOfLogfile + File.separator + nameOfLogfile;
		File logFile = new File(logFileName);

		// MessageOnlyLayout layout = new MessageOnlyLayout();

		try {

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			out.close();

		} catch (IOException e) {
			Logtxt.logtxt(logFile, getTextResourceServiceExc().getText(EXC_ERROR_IOE, e + " (LogConfig)"));
		}

		return logFileName;
	}

}
