/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
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
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	MESSAGE_WAIT									= "message.wait";
	String	ERROR_PARAMETER_USAGE							= "error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY					= "error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE					= "error.logdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE					= "error.workdirectory.notwritable";
	String	ERROR_SIPFILE_FILENOTEXISTING					= "error.sipfile.filenotexisting";
	String	ERROR_LOGGING_NOFILEAPPENDER					= "error.logging.nofileappender";
	String	ERROR_CANNOTCREATEZIP							= "error.cannotcreatezip";
	String	ERROR_PARAMETER_OPTIONAL_1						= "error.parameter.optional.1";
	String	ERROR_PARAMETER_OPTIONAL_2						= "error.parameter.optional.2";
	String	ERROR_JHOVEAPP_MISSING							= "error.jhoveapp.missing";
	String	ERROR_JHOVECONF_MISSING							= "error.jhoveconf.missing";

	String	ERROR_WRONG_JRE									= "error.wrong.jdk";

	String	MESSAGE_TOTAL_VALID								= "message.total.valid";
	String	MESSAGE_TOTAL_INVALID							= "message.total.invalid";

	String	MESSAGE_FOOTER_LOG								= "message.footer.log";
	String	MESSAGE_FOOTER_SIP								= "message.footer.sip";

	String	MESSAGE_FOOTER_REPORTJHOVE						= "message.footer.reportjhove";
	String	MESSAGE_FOOTER_REPORTPDFTRON					= "message.footer.reportpdftron";
	String	MESSAGE_FOOTER_REPORTSIARDVAL					= "message.footer.reportsiardval";

	// Globale Meldungen
	String	MESSAGE_tiffvalIDATION							= "message.tiffvalidation";
	String	MESSAGE_VALIDATION_INTERRUPTED					= "message.validation.interrupted";
	String	MESSAGE_VALIDATION_FINISHED						= "message.validation.finished";
	String	MESSAGE_MODULE_WAIT								= "message.module.wait";
	String	MESSAGE_MODULE_WAITZAEHLER						= "message.module.waitzaehler";
	String	MESSAGE_MODULE_VALID							= "message.module.valid";
	String	MESSAGE_MODULE_INVALID							= "message.module.invalid";
	String	MESSAGE_MODULE_INVALID_2ARGS					= "message.module.invalid.2args";

	String	MESSAGE_MODULE_Aa								= "message.module.aa";
	String	MESSAGE_MODULE_Ab								= "message.module.ab";
	String	MESSAGE_MODULE_Ac								= "message.module.ac";
	String	MESSAGE_MODULE_Ad								= "message.module.ad";
	String	MESSAGE_MODULE_Ae								= "message.module.ae";
	String	MESSAGE_MODULE_Af								= "message.module.af";
	String	MESSAGE_MODULE_Ba								= "message.module.ba";
	String	MESSAGE_MODULE_Bb								= "message.module.bb";
	String	MESSAGE_MODULE_Bc								= "message.module.bc";
	String	MESSAGE_MODULE_Bd								= "message.module.bd";
	String	MESSAGE_MODULE_Ca								= "message.module.ca";
	String	MESSAGE_MODULE_Cb								= "message.module.cb";
	String	MESSAGE_MODULE_Cc								= "message.module.cc";
	String	MESSAGE_MODULE_Cd								= "message.module.cd";

	String	MESSAGE_STEPERGEBNIS_Aa							= "message.stepergebnis.aa";
	String	MESSAGE_STEPERGEBNIS_Ab							= "message.stepergebnis.ab";
	String	MESSAGE_STEPERGEBNIS_Ac							= "message.stepergebnis.ac";
	String	MESSAGE_STEPERGEBNIS_Ad							= "message.stepergebnis.ad";
	String	MESSAGE_STEPERGEBNIS_Ae							= "message.stepergebnis.ae";
	String	MESSAGE_STEPERGEBNIS_Af							= "message.stepergebnis.af";
	String	MESSAGE_STEPERGEBNIS_Ba							= "message.stepergebnis.ba";
	String	MESSAGE_STEPERGEBNIS_Bb							= "message.stepergebnis.bb";
	String	MESSAGE_STEPERGEBNIS_Bc							= "message.stepergebnis.bc";
	String	MESSAGE_STEPERGEBNIS_Bd							= "message.stepergebnis.bd";
	String	MESSAGE_STEPERGEBNIS_Ca							= "message.stepergebnis.ca";
	String	MESSAGE_STEPERGEBNIS_Cb							= "message.stepergebnis.cb";
	String	MESSAGE_STEPERGEBNIS_Cc							= "message.stepergebnis.cc";
	String	MESSAGE_STEPERGEBNIS_Cd							= "message.stepergebnis.cd";

	String	MESSAGE_DASHES									= "message.dashes";
	String	MESSAGE_INDENT									= "message.indent";
	String	MESSAGE_SLASH									= "message.slash";

	String	MESSAGE_CONFIGURATION_ERROR_1					= "message.configuration.error.1";
	String	MESSAGE_CONFIGURATION_ERROR_2					= "message.configuration.error.2";
	String	MESSAGE_CONFIGURATION_ERROR_3					= "message.configuration.error.3";
	String	MESSAGE_CONFIGURATION_ERROR_NO_SIGNATURE		= "message.configuration.error.no.signature";

	String	ERROR_CANNOT_INITIALIZE_DROID					= "error.cannot.initialize.droid";

	String	ERROR_UNKNOWN									= "error.unknown";

	// Modul 1 Meldungen
	String	ERROR_MODULE_A_INCORRECTFILEENDING				= "error.module.a.incorrectfileending";

	// Modul 1c Meldungen
	String	MESSAGE_MODULE_AC_NOTALLOWEDFILE				= "message.module.ac.notallowedfile";
	String	MESSAGE_MODULE_AC_NOTALLOWEDV					= "message.module.ac.notallowedv";
	String	MESSAGE_MODULE_AC_VERSION						= "message.module.ac.version";
	String	MESSAGE_MODULE_AC_MISSINGFILE					= "message.module.ac.missingfile";
	String	MESSAGE_MODULE_AC_PATHTOOLONG					= "message.module.ac.pathtoolong";
	String	MESSAGE_MODULE_AC_FILENAMETOOLONG				= "message.module.ac.filenametoolong";
	String	MESSAGE_MODULE_AC_INVALIDCHARACTERS				= "message.module.ac.invalidcharacters";
	String	MESSAGE_MODULE_AC_INVALIDREGEX					= "message.module.ac.invalidregex";
	String	MESSAGE_MODULE_AC_INVALIDFILENAME				= "message.module.ac.invalidfilename";

	// Modul 1d Meldungen
	String	ERROR_MODULE_AD_WRONGNUMBEROFXSDS				= "error.module.ad.wrongnumberofxsds";
	String	ERROR_MODULE_AD_CONTENTB4HEADER					= "error.module.ad.contentB4header";
	String	ERROR_MODULE_AD_METADATA_ERRORS					= "error.module.ad.metadata.errors";

	// Modul 1e Meldungen
	String	ERROR_MODULE_AE_NOMETADATAFOUND					= "error.module.ae.nometadatafound";
	String	MESSAGE_MODULE_AE_ABLIEFERUNGSTYPFILE			= "message.module.ae.ablieferungstypfile";
	String	MESSAGE_MODULE_AE_ABLIEFERUNGSTYPGEVER			= "message.module.ae.ablieferungstypgever";
	String	ERROR_MODULE_AE_ABLIEFERUNGSTYPUNDEFINED		= "error.module.ae.ablieferungstypundefined";

	// Modul 1f Meldungen
	String	MESSAGE_MODULE_AF_GEVERSIPWITHOUTPRIMARYDATA	= "message.module.af.geversipwithoutprimarydata";
	String	ERROR_MODULE_AF_FILESIPWITHOUTPRIMARYDATA		= "error.module.af.filesipwithoutprimarydata";

	// Modul 2a Meldungen
	String	MESSAGE_MODULE_BA_FILEMISSING					= "message.module.ba.filemissing";

	// Modul 2b Meldungen
	String	ERROR_MODULE_BB_CANNOTPROCESSMD5				= "error.module.bb.cannotprocessmd5";
	String	ERROR_MODULE_BB_CANNOTCLOSESTREAMMD5			= "error.module.bb.cannotclosestreammd5";
	String	MESSAGE_MODULE_Bb_WRONGMD5						= "error.module.bb.wrongmd5";

	// Modul 2c Meldungen
	String	MESSAGE_MODULE_BC_FILEMISSING					= "message.module.bc.filemissing";
	String	MESSAGE_MODULE_BC_FILEMISSINGO					= "message.module.bc.filemissingo";

	// Modul 2d Meldungen
	String	MESSAGE_MODULE_BD_MISSINGINABLIEFERUNG			= "message.module.bd.missinginablieferung";

	// Modul 3a Meldungen
	String	MESSAGE_MODULE_CA_FILES							= "message.module.ca.files";

	// Modul 3c Meldungen
	String	MESSAGE_MODULE_CC_CANNOTWRITEJHOVEREPORT		= "message.module.cc.cannotwritejhovereport";
	String	MESSAGE_MODULE_CC_CANNOTWRITESIARDVALREPORT		= "message.module.cc.cannotwritesiardvalreport";
	String	MESSAGE_MODULE_CC_INVALID						= "message.module.cc.invalid";
	String	MESSAGE_MODULE_CC_ERRORS_IN						= "message.module.cc.errors.in";
	String	MESSAGE_MODULE_CC_ERRORS_IN_A					= "message.module.cc.errors.in.a";
	String	MESSAGE_MODULE_CC_ERRORS_IN_B					= "message.module.cc.errors.in.b";
	String	MESSAGE_MODULE_CC_ERRORS_IN_C					= "message.module.cc.errors.in.c";
	String	MESSAGE_MODULE_CC_ERRORS_IN_D					= "message.module.cc.errors.in.d";
	String	MESSAGE_MODULE_CC_ERRORS_IN_E					= "message.module.cc.errors.in.e";
	String	MESSAGE_MODULE_CC_ERRORS_IN_F					= "message.module.cc.errors.in.f";
	String	MESSAGE_MODULE_CC_ERRORS_IN_G					= "message.module.cc.errors.in.g";
	String	MESSAGE_MODULE_CC_ERRORS_IN_H					= "message.module.cc.errors.in.h";
	String	MESSAGE_MODULE_CC_ERRORS_IN_I					= "message.module.cc.errors.in.i";
	String	MESSAGE_MODULE_CC_ERRORS_IN_J					= "message.module.cc.errors.in.j";
	String	MESSAGE_MODULE_CC_NOJHOVEVAL					= "message.module.cc.nojhoveval";

	// Modul 3d Meldungen
	String	ERROR_MODULE_CD_DATUM_ENTSTEHUNG_IN_FUTURE		= "error.module.cd.datum.entstehung.in.future";
	String	ERROR_MODULE_CD_DATUM_IN_FUTURE					= "error.module.cd.datum.in.future";

	String	ERROR_MODULE_CD_INVALID_ABLIEFERUNG_RANGE		= "error.module.cd.invalid.ablieferung.range";
	String	ERROR_MODULE_CD_INVALID_DOSSIER_RANGE_CA		= "error.module.cd.invalid.dossier.range.ca";
	String	ERROR_MODULE_CD_INVALID_DOSSIER_RANGE_CA_ABL	= "error.module.cd.invalid.dossier.range.ca.abl";
	String	ERROR_MODULE_CD_INVALID_DOKUMENT_RANGE_CA		= "error.module.cd.invalid.dokument.range.ca";
	String	MESSAGE_MODULE_CD_NUMBER_OF_CONTENT_FILES		= "message.module.cd.numberofcontentfiles";
	String	ERROR_MODULE_CD_UNPARSEABLE_DATE				= "error.module.cd.unparseable.date";
}
