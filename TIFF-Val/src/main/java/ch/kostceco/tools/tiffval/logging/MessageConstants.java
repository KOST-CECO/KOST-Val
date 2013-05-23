/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Tagged Image File Format (TIFF).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.tiffval.logging;

/**
 * Interface für den Zugriff auf Resourcen aus dem ResourceBundle.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	ERROR_PARAMETER_USAGE						= "error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY				= "error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE				= "error.logdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE				= "error.workdirectory.notwritable";
	String	ERROR_TIFFFILE_FILENOTEXISTING				= "error.tifffile.filenotexisting";
	String	ERROR_LOGGING_NOFILEAPPENDER				= "error.logging.nofileappender";
	String	ERROR_JHOVEAPP_MISSING						= "error.jhoveapp.missing";
	String	ERROR_JHOVECONF_MISSING						= "error.jhoveconf.missing";

	String	ERROR_WRONG_JRE								= "error.wrong.jdk";

	String	MESSAGE_TOTAL_VALID							= "message.total.valid";
	String	MESSAGE_TOTAL_INVALID						= "message.total.invalid";

	String	MESSAGE_FOOTER_LOG							= "message.footer.log";
	String	MESSAGE_FOOTER_TIFF							= "message.footer.tiff";

	String	MESSAGE_FOOTER_REPORTJHOVE					= "message.footer.reportjhove";

	// Globale Meldungen
	String	MESSAGE_tiffvalIDATION						= "message.tiffvalidation";
	String	MESSAGE_VALIDATION_INTERRUPTED				= "message.validation.interrupted";
	String	MESSAGE_VALIDATION_FINISHED					= "message.validation.finished";
	String	MESSAGE_MODULE_VALID						= "message.module.valid";
	String	MESSAGE_MODULE_INVALID						= "message.module.invalid";
	String	MESSAGE_MODULE_INVALID_2ARGS				= "message.module.invalid.2args";

	String	MESSAGE_MODULE_A							= "message.module.a";
	String	MESSAGE_MODULE_B							= "message.module.b";
	String	MESSAGE_MODULE_C							= "message.module.c";
	
	String	MESSAGE_STEPERGEBNIS_A						= "message.stepergebnis.a";
	String	MESSAGE_STEPERGEBNIS_B						= "message.stepergebnis.b";
	String	MESSAGE_STEPERGEBNIS_C						= "message.stepergebnis.c";
	
	String	MESSAGE_DASHES								= "message.dashes";

	String	MESSAGE_CONFIGURATION_ERROR_1				= "message.configuration.error.1";
	String	MESSAGE_CONFIGURATION_ERROR_2				= "message.configuration.error.2";
	String	MESSAGE_CONFIGURATION_ERROR_3				= "message.configuration.error.3";

	String	ERROR_UNKNOWN								= "error.unknown";

	// Modul A Meldungen
	String	ERROR_MODULE_A_INCORRECTFILEENDING			= "error.module.a.incorrectfileending";
	String	ERROR_MODULE_A_INCORRECTFILE				= "error.module.a.incorrectfile";
	String	ERROR_MODULE_A_ISDIRECTORY					= "error.module.a.isdirectory";

	// Modul B Meldungen
	String	MESSAGE_MODULE_B_CANNOTWRITEJHOVEREPORT		= "message.module.b.cannotwritejhovereport";
	String	MESSAGE_MODULE_B_JHOVEINVALID				= "message.module.b.jhoveinvalid";
	
	// Modul C-G Meldungen
	String 	MESSAGE_MODULE_CG_CANNOTFINDJHOVEREPORT		= "message.module.cg.cannotfindjhovereport";
	String	MESSAGE_MODULE_CG_INVALID					= "message.module.cg.invalid";
	String	MESSAGE_MODULE_CG_JHOVENIO					= "message.module.cg.jhovenio";
}
