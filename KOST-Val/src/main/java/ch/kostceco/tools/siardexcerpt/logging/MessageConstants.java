/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2021 Claire Roethlisberger (KOST-CECO)
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

/** Interface fuer den Zugriff auf Resourcen aus dem ResourceBundle.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	EXC_ERROR_IOE													= "exc.error.ioe";
	String	EXC_ERROR_PARAMETER_USAGE							= "exc.error.parameter.usage";
	String	EXC_ERROR_LOGDIRECTORY_NODIRECTORY		= "exc.error.logdirectory.nodirectory";
	String	EXC_ERROR_LOGDIRECTORY_NOTWRITABLE		= "exc.error.logdirectory.notwritable";
	String	EXC_ERROR_WORKDIRECTORY_NOTWRITABLE		= "exc.error.workdirectory.notwritable";
	String	EXC_ERROR_WORKDIRECTORY_EXISTS				= "exc.error.workdirectory.exists";
	String	EXC_ERROR_SIARDFILE_FILENOTEXISTING		= "exc.error.siardfile.filenotexisting";
	String	EXC_ERROR_CONFIGFILE_FILENOTEXISTING	= "exc.error.configfile.filenotexisting";
	String	EXC_ERROR_CONFIGFILEHARD_FILEEXISTING	= "exc.error.configfilehard.fileexisting";
	String	EXC_ERROR_LOGGING_NOFILEAPPENDER			= "exc.error.logging.nofileappender";
	String	EXC_ERROR_WRONG_JRE										= "exc.error.wrong.jdk";
	String	EXC_ERROR_NOINIT											= "exc.error.noinit";
	String	EXC_ERROR_SPECIAL_CHARACTER						= "exc.error.special.character";

	// Globale Meldungen
	String	EXC_MESSAGE_XML_HEADER								= "exc.message.xml.header";
	String	EXC_MESSAGE_XML_START									= "exc.message.xml.start";
	String	EXC_MESSAGE_XML_TEXT									= "exc.message.xml.text";
	String	EXC_MESSAGE_XML_INFO									= "exc.message.xml.info";
	String	EXC_MESSAGE_XML_LOGEND								= "exc.message.xml.logend";
	String	EXC_MESSAGE_XML_TITLE									= "exc.message.xml.title";

	String	EXC_MESSAGE_XML_MODUL_A								= "exc.message.xml.modul.a";
	String	EXC_MESSAGE_XML_MODUL_B								= "exc.message.xml.modul.b";
	String	EXC_MESSAGE_XML_MODUL_C								= "exc.message.xml.modul.c";
	String	EXC_MESSAGE_XML_MODUL_D								= "exc.message.xml.modul.d";

	String	EXC_MESSAGE_XML_CONFIGURATION_ERROR_1	= "exc.message.xml.configuration.error.1";

	String	EXC_ERROR_XML_UNKNOWN									= "exc.error.xml.unknown";

	String	EXC_MESSAGE_XML_ELEMENT_OPEN					= "exc.message.xml.element.open";
	String	EXC_MESSAGE_XML_ELEMENT_CONTENT				= "exc.message.xml.element.content";
	String	EXC_MESSAGE_XML_ELEMENT_CLOSE					= "exc.message.xml.element.close";

	// ************* AutoXSL *************************************************************************
	String	EXC_AUTO_XSL_TABLE_START							= "exc.auto.xsl.table.start";
	String	EXC_AUTO_XSL_COLUMN										= "exc.auto.xsl.column";
	String	EXC_AUTO_XSL_TABLE_END								= "exc.auto.xsl.table.end";
	String	EXC_AUTO_XSL_FOOTER										= "exc.auto.xsl.footer";

	// *************Meldungen*************************************************************************
	// Modul a Meldungen
	String	EXC_ERROR_XML_A_CANNOTEXTRACTZIP			= "exc.error.xml.a.cannotextractzip";
	String	EXC_MESSAGE_A_INIT_OK									= "exc.message.a.init.ok";
	String	EXC_MESSAGE_A_INIT_NOK								= "exc.message.a.init.nok";
	String	EXC_MESSAGE_A_INIT_NOK_CONFIG					= "exc.message.a.init.nok.config";

	// Modul b Meldungen
	String	EXC_ERROR_XML_B_STRUCTURE							= "exc.error.xml.b.structure";
	String	EXC_ERROR_XML_B_CANNOTSEARCHRECORD		= "exc.error.xml.b.cannotsearchrecord";
	String	EXC_MESSAGE_B_SEARCH_OK								= "exc.message.b.search.ok";
	String	EXC_MESSAGE_B_SEARCH_NOK							= "exc.message.b.search.nok";

	// Modul c Meldungen
	String	EXC_ERROR_XML_C_MISSINGFILE						= "exc.error.xml.c.missingfile";
	String	EXC_ERROR_XML_C_CANNOTEXTRACTRECORD		= "exc.error.xml.c.cannotextractrecord";
	String	EXC_MESSAGE_C_EXCERPT_OK							= "exc.message.c.excerpt.ok";
	String	EXC_MESSAGE_C_EXCERPT_NOK							= "exc.message.c.excerpt.nok";
}
