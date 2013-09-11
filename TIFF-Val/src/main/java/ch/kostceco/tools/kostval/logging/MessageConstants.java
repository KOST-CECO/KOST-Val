/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF and SIARD-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
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

package ch.kostceco.tools.kostval.logging;

/**
 * Interface für den Zugriff auf Resourcen aus dem ResourceBundle.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	ERROR_PARAMETER_USAGE					= "error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY			= "error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE			= "error.logdirectory.notwritable";
	String	ERROR_TIFFFILE_FILENOTEXISTING			= "error.tifffile.filenotexisting";
	String	ERROR_LOGGING_NOFILEAPPENDER			= "error.logging.nofileappender";
	String	ERROR_JHOVEAPP_MISSING					= "error.jhoveapp.missing";
	String	ERROR_JHOVECONF_MISSING					= "error.jhoveconf.missing";

	String	ERROR_WRONG_JRE							= "error.wrong.jdk";

	String	MESSAGE_TOTAL_VALID						= "message.total.valid";
	String	MESSAGE_TOTAL_INVALID					= "message.total.invalid";

	String	MESSAGE_FOOTER_LOG						= "message.footer.log";
	String	MESSAGE_FOOTER_TIFF						= "message.footer.tiff";

	String	MESSAGE_FOOTER_REPORTJHOVE				= "message.footer.reportjhove";

	// Globale Meldungen
	String	MESSAGE_kostvalIDATION					= "message.kostvalidation";
	String	MESSAGE_VALIDATION_INTERRUPTED			= "message.validation.interrupted";
	String	MESSAGE_VALIDATION_FINISHED				= "message.validation.finished";
	String	MESSAGE_MODULE_VALID					= "message.module.valid";
	String	MESSAGE_MODULE_INVALID					= "message.module.invalid";
	String	MESSAGE_MODULE_INVALID_2ARGS			= "message.module.invalid.2args";

	String	MESSAGE_MODULE_A						= "message.module.a";
	String	MESSAGE_MODULE_B						= "message.module.b";
	String	MESSAGE_MODULE_C						= "message.module.c";
	String	MESSAGE_MODULE_D						= "message.module.d";
	String	MESSAGE_MODULE_E						= "message.module.e";
	String	MESSAGE_MODULE_F						= "message.module.f";
	String	MESSAGE_MODULE_G						= "message.module.g";
	String	MESSAGE_MODULE_H						= "message.module.h";

	String	MESSAGE_STEPERGEBNIS_A					= "message.stepergebnis.a";
	String	MESSAGE_STEPERGEBNIS_B					= "message.stepergebnis.b";
	String	MESSAGE_STEPERGEBNIS_C					= "message.stepergebnis.c";
	String	MESSAGE_STEPERGEBNIS_D					= "message.stepergebnis.d";
	String	MESSAGE_STEPERGEBNIS_E					= "message.stepergebnis.e";
	String	MESSAGE_STEPERGEBNIS_F					= "message.stepergebnis.f";
	String	MESSAGE_STEPERGEBNIS_G					= "message.stepergebnis.g";
	String	MESSAGE_STEPERGEBNIS_H					= "message.stepergebnis.h";

	String	MESSAGE_DASHES							= "message.dashes";

	String	MESSAGE_CONFIGURATION_ERROR_1			= "message.configuration.error.1";
	String	MESSAGE_CONFIGURATION_ERROR_2			= "message.configuration.error.2";
	String	MESSAGE_CONFIGURATION_ERROR_3			= "message.configuration.error.3";

	String	ERROR_UNKNOWN							= "error.unknown";

	// Modul A Meldungen
	String	ERROR_MODULE_A_INCORRECTFILEENDING		= "error.module.a.incorrectfileending";
	String	ERROR_MODULE_A_INCORRECTFILE			= "error.module.a.incorrectfile";
	String	ERROR_MODULE_A_ISDIRECTORY				= "error.module.a.isdirectory";

	// Modul B Meldungen
	String	MESSAGE_MODULE_B_CANNOTWRITEJHOVEREPORT	= "message.module.b.cannotwritejhovereport";
	String	MESSAGE_MODULE_B_JHOVEINVALID			= "message.module.b.jhoveinvalid";

	// Modul C-G Meldungen
	String	MESSAGE_MODULE_CG_CANNOTFINDJHOVEREPORT	= "message.module.cg.cannotfindjhovereport";
	String	MESSAGE_MODULE_CG_INVALID				= "message.module.cg.invalid";
	String	MESSAGE_MODULE_CG_JHOVENIO				= "message.module.cg.jhovenio";
	String	MESSAGE_MODULE_CG_JHOVEN15				= "message.module.cg.jhoven15";
	
// *************SIARD-Meldungen*************************************************************************
	// Initialisierung und Parameter-Ueberpruefung
//	String	ERROR_PARAMETER_USAGE							= "error.parameter.usage";
//	String	ERROR_LOGDIRECTORY_NODIRECTORY					= "error.logdirectory.nodirectory";
//	String	ERROR_LOGDIRECTORY_NOTWRITABLE					= "error.logdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE					= "error.workdirectory.notwritable";
	String	ERROR_SIARDFILE_FILENOTEXISTING					= "error.siardfile.filenotexisting";
//	String	ERROR_LOGGING_NOFILEAPPENDER					= "error.logging.nofileappender";

//	String	ERROR_WRONG_JRE									= "error.wrong.jdk";

//	String	MESSAGE_TOTAL_VALID								= "message.total.valid";
//	String	MESSAGE_TOTAL_INVALID							= "message.total.invalid";

//	String	MESSAGE_FOOTER_LOG								= "message.footer.log";
	String	MESSAGE_FOOTER_SIARD							= "message.footer.siard";

	// Globale Meldungen
//	String	MESSAGE_kostvalIDATION							= "message.kostvalidation";
//	String	MESSAGE_VALIDATION_INTERRUPTED					= "message.validation.interrupted";
//	String	MESSAGE_VALIDATION_FINISHED						= "message.validation.finished";
//	String	MESSAGE_MODULE_VALID							= "message.module.valid";
//	String	MESSAGE_MODULE_INVALID							= "message.module.invalid";
//	String	MESSAGE_MODULE_INVALID_2ARGS					= "message.module.invalid.2args";

	String	MESSAGE_MODULE_A_SIARD								= "message.module.a.siard";
	String	MESSAGE_MODULE_B_SIARD								= "message.module.b.siard";
	String	MESSAGE_MODULE_C_SIARD								= "message.module.c.siard";
	String	MESSAGE_MODULE_D_SIARD								= "message.module.d.siard";
	String	MESSAGE_MODULE_E_SIARD								= "message.module.e.siard";
	String	MESSAGE_MODULE_F_SIARD								= "message.module.f.siard";
	String	MESSAGE_MODULE_G_SIARD								= "message.module.g.siard";
	String	MESSAGE_MODULE_H_SIARD								= "message.module.h.siard";
	String	MESSAGE_MODULE_I_SIARD								= "message.module.i.siard";
	String	MESSAGE_MODULE_J_SIARD								= "message.module.j.siard";
	String	MESSAGE_MODULE_K_SIARD								= "message.module.k.siard";
	String	MESSAGE_MODULE_L_SIARD								= "message.module.l.siard";

	String	MESSAGE_STEPERGEBNIS_A_SIARD							= "message.stepergebnis.a.siard";
	String	MESSAGE_STEPERGEBNIS_B_SIARD							= "message.stepergebnis.b.siard";
	String	MESSAGE_STEPERGEBNIS_C_SIARD							= "message.stepergebnis.c.siard";
	String	MESSAGE_STEPERGEBNIS_D_SIARD							= "message.stepergebnis.d.siard";
	String	MESSAGE_STEPERGEBNIS_E_SIARD							= "message.stepergebnis.e.siard";
	String	MESSAGE_STEPERGEBNIS_F_SIARD							= "message.stepergebnis.f.siard";
	String	MESSAGE_STEPERGEBNIS_G_SIARD							= "message.stepergebnis.g.siard";
	String	MESSAGE_STEPERGEBNIS_H_SIARD							= "message.stepergebnis.h.siard";
	String	MESSAGE_STEPERGEBNIS_I_SIARD							= "message.stepergebnis.i.siard";
	String	MESSAGE_STEPERGEBNIS_J_SIARD							= "message.stepergebnis.j.siard";
	String	MESSAGE_STEPERGEBNIS_K_SIARD							= "message.stepergebnis.k.siard";
	String	MESSAGE_STEPERGEBNIS_L_SIARD							= "message.stepergebnis.l.siard";

//	String	MESSAGE_DASHES									= "message.dashes";

//	String	MESSAGE_CONFIGURATION_ERROR_1					= "message.configuration.error.1";
//	String	MESSAGE_CONFIGURATION_ERROR_2					= "message.configuration.error.2";
//	String	MESSAGE_CONFIGURATION_ERROR_3					= "message.configuration.error.3";

//	String	ERROR_UNKNOWN									= "error.unknown";

	// Modul A Meldungen
	String	ERROR_MODULE_A_NOFILE							= "error.module.a.nofile";
	String	ERROR_MODULE_A_INCORRECTFILEENDING_SIARD		= "error.module.a.incorrectfileending.siard";
	String	ERROR_MODULE_A_DEFLATED							= "error.module.a.deflated";

	// Modul B Meldungen
	String	MESSAGE_MODULE_B_NOTALLOWEDFILE					= "message.module.b.notallowedfile";
	String	MESSAGE_MODULE_B_CONTENT						= "message.module.b.content";
	String	MESSAGE_MODULE_B_HEADER							= "message.module.b.header";

	// Modul C Meldungen
	String	MESSAGE_MODULE_C_NOMETADATAFOUND				= "message.module.c.nometadatafound";
	String	MESSAGE_MODULE_C_NOMETADATAXSD					= "message.module.c.nometadataxsd";
	String	MESSAGE_MODULE_C_METADATA_ERRORS				= "message.module.c.metadata.errors";
	String	MESSAGE_MODULE_C_METADATA_ORIGERRORS			= "message.module.c.metadata.origerrors";

	// Modul D Meldungen
	String	MESSAGE_MODULE_D_INVALID_FOLDER					= "message.module.d.invalid.folder";
	String	MESSAGE_MODULE_D_MISSING_FOLDER					= "message.module.d.missing.folder";
	String	MESSAGE_MODULE_D_INVALID_FILE					= "message.module.d.invalid.file";
	String	MESSAGE_MODULE_D_MISSING_FILE					= "message.module.d.missing.file";

	// Modul E Meldungen
	String	MESSAGE_MODULE_E_PROPERTIES_ERROR				= "message.module.e.properties.error";
	String	MESSAGE_MODULE_E_PATH_ERROR						= "message.module.e.path.error";
	String	MESSAGE_MODULE_E_EXTRACT_ERROR					= "message.module.e.extract.error";
	String	MESSAGE_MODULE_E_METADATA_ACCESS_ERROR			= "message.module.e.metadata.access.error";
	String	MESSAGE_MODULE_E_XML_ACCESS_ERROR				= "message.module.e.xml.access.error";
	String	MESSAGE_MODULE_E_PREVALIDATION_ERROR			= "message.module.e.prevalidation.error";
	String	MESSAGE_MODULE_E_INVALID_ATTRIBUTE_COUNT		= "message.module.e.attribute.count.validation.failed";
	String	MESSAGE_MODULE_E_INVALID_ATTRIBUTE_OCCURRENCE	= "message.module.e.attribute.occurrence.validation.failed";
	String	MESSAGE_MODULE_E_INVALID_ATTRIBUTE_TYPE			= "message.module.e.attribute.type.validation.failed";

	// Modul F Meldungen
	String	MESSAGE_MODULE_F_PROPERTIES_ERROR				= "message.module.f.properties.error";
	String	MESSAGE_MODULE_F_PATH_ERROR						= "message.module.f.path.error";
	String	MESSAGE_MODULE_F_INVALID_TABLE_XML_FILES		= "message.module.f.invalid.table.xml.files";
	String	MESSAGE_MODULE_F_INVALID_TABLE_XSD_FILES		= "message.module.f.invalid.table.xsd.files";

	// Modul G Meldungen
	String	MESSAGE_MODULE_G_DUPLICATE_SCHEMA				= "message.module.g.duplicate.schema";
	String	MESSAGE_MODULE_G_DUPLICATE_TABLE				= "message.module.g.duplicate.table";
	String	MESSAGE_MODULE_G_DUPLICATE_COLUMN				= "message.module.g.duplicate.column";

	// Modul H Meldungen
	String	MESSAGE_MODULE_H_INVALID_FOLDER					= "message.module.h.invalid.folder";
	String	MESSAGE_MODULE_H_INVALID_XML					= "message.module.h.invalid.xml";
	String	MESSAGE_MODULE_H_INVALID_ERROR					= "message.module.h.invalid.error";
	String	MESSAGE_MODULE_H_TABLE_NOT_VALIDATED1			= "message.module.h.table.not.validated1";
	String	MESSAGE_MODULE_H_TABLE_NOT_VALIDATED2			= "message.module.h.table.not.validated2";

	// Modul I Meldungen
	String	MESSAGE_MODULE_I_NOTALLOWEDEXT					= "message.module.i.notallowedext";

	// Modul J Meldungen
	String	MESSAGE_MODULE_J_INVALID_FOLDER					= "message.module.j.invalid.folder";
	String	MESSAGE_MODULE_J_INVALID_FILE					= "message.module.j.invalid.file";
	String	MESSAGE_MODULE_J_INVALID_ENTRY					= "message.module.j.invalid.entry";

}
