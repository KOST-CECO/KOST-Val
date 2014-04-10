/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, PDF/A-Files and Submission 
Information Package (SIP). 
Copyright (C) 2012-2014 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau), Daniel Ludin (BEDAG AG)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
BEDAG AG and Daniel Ludin hereby disclaims all copyright interest in the program 
SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March 2011.
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
	String	ERROR_PARAMETER_USAGE							= "error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY					= "error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE					= "error.logdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_NOTDELETABLE				= "error.workdirectory.notdeletable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE					= "error.workdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_EXISTS						= "error.workdirectory.exists";
	String	ERROR_VALFILE_FILENOTEXISTING					= "error.valfile.filenotexisting";
	String	ERROR_LOGGING_NOFILEAPPENDER					= "error.logging.nofileappender";
	String	ERROR_CANNOTCREATEZIP							= "error.cannotcreatezip";
	String	ERROR_JHOVECONF_MISSING							= "error.jhoveconf.missing";
	String	ERROR_PARAMETER_OPTIONAL_1						= "error.parameter.optional.1";
	String	ERROR_INCORRECTFILEENDING						= "error.incorrectfileending";

	String	ERROR_WRONG_JRE									= "error.wrong.jdk";

	String	MESSAGE_TOTAL_VALID								= "message.total.valid";
	String	MESSAGE_TOTAL_INVALID							= "message.total.invalid";

	String	MESSAGE_FOOTER_LOG								= "message.footer.log";
	String	MESSAGE_FOOTER_TIFF								= "message.footer.tiff";
	String	MESSAGE_FOOTER_SIARD							= "message.footer.siard";
	String	MESSAGE_FOOTER_PDFA								= "message.footer.pdfa";
	String	MESSAGE_FOOTER_SIP								= "message.footer.sip";
	String	MESSAGE_XML_VALFILE								= "message.xml.valfile";
	String	MESSAGE_XML_HEADER								= "message.xml.header";
	String	MESSAGE_XML_START								= "message.xml.start";
	String	MESSAGE_XML_END									= "message.xml.end";
	String	MESSAGE_FOOTER_REPORTJHOVE						= "message.footer.reportjhove";
	String	MESSAGE_FOOTER_REPORTPDFTRON					= "message.footer.reportpdftron";
	String	MESSAGE_FOOTER_SUMMARY_1						= "message.footer.summary.1";
	String	MESSAGE_FOOTER_SUMMARY_PDFA						= "message.footer.summary.pdfa";
	String	MESSAGE_FOOTER_SUMMARY_SIARD					= "message.footer.summary.siard";
	String	MESSAGE_FOOTER_SUMMARY_TIFF						= "message.footer.summary.tiff";
	String	MESSAGE_FOOTER_SUMMARY_2						= "message.footer.summary.2";

	// Globale Meldungen
	String	MESSAGE_KOSTVALIDATION							= "message.kostvalidation";
	String	MESSAGE_XML_INFO								= "message.xml.info";
	String	MESSAGE_TIFFVALIDATION							= "message.tiffvalidation";
	String	MESSAGE_SIARDVALIDATION							= "message.siardvalidation";
	String	MESSAGE_PDFAVALIDATION							= "message.pdfavalidation";
	String	MESSAGE_SIPVALIDATION							= "message.sipvalidation";
	String	MESSAGE_XML_VALERGEBNIS							= "message.xml.valergebnis";
	String	MESSAGE_XML_VALTYPE								= "message.xml.valtype";
	String	MESSAGE_XML_FORMAT1								= "message.xml.format1";
	String	MESSAGE_XML_FORMAT2								= "message.xml.format2";
	String	MESSAGE_XML_SIP1								= "message.xml.sip1";
	String	MESSAGE_XML_SIP2								= "message.xml.sip2";
	String	MESSAGE_XML_VALERGEBNIS_VALID					= "message.xml.valergebnis.valid";
	String	MESSAGE_XML_VALERGEBNIS_INVALID					= "message.xml.valergebnis.invalid";
	String	MESSAGE_XML_VALERGEBNIS_CLOSE					= "message.xml.valergebnis.close";

	String	MESSAGE_VALIDATION_INTERRUPTED					= "message.validation.interrupted";
	String	MESSAGE_VALIDATION_FINISHED						= "message.validation.finished";
	String	MESSAGE_MODULE_VALID							= "message.module.valid";
	String	MESSAGE_MODULE_INVALID							= "message.module.invalid";
	String	MESSAGE_MODULE_INVALID_2ARGS					= "message.module.invalid.2args";

	String	MESSAGE_MODULE_A								= "message.module.a";
	String	MESSAGE_MODULE_B								= "message.module.b";
	String	MESSAGE_MODULE_C								= "message.module.c";
	String	MESSAGE_MODULE_D								= "message.module.d";
	String	MESSAGE_MODULE_E								= "message.module.e";
	String	MESSAGE_MODULE_F								= "message.module.f";
	String	MESSAGE_MODULE_G								= "message.module.g";
	String	MESSAGE_MODULE_H								= "message.module.h";
	String	MESSAGE_MODULE_I								= "message.module.i";
	String	MESSAGE_MODULE_J								= "message.module.j";

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

/*	String	MESSAGE_STEPERGEBNIS_A_TIFF						= "message.stepergebnis.a.tiff";
	String	MESSAGE_STEPERGEBNIS_B_TIFF						= "message.stepergebnis.b.tiff";
	String	MESSAGE_STEPERGEBNIS_C_TIFF						= "message.stepergebnis.c.tiff";
	String	MESSAGE_STEPERGEBNIS_D_TIFF						= "message.stepergebnis.d.tiff";
	String	MESSAGE_STEPERGEBNIS_E_TIFF						= "message.stepergebnis.e.tiff";
	String	MESSAGE_STEPERGEBNIS_F_TIFF						= "message.stepergebnis.f.tiff";
	String	MESSAGE_STEPERGEBNIS_G_TIFF						= "message.stepergebnis.g.tiff";
	String	MESSAGE_STEPERGEBNIS_H_TIFF						= "message.stepergebnis.h.tiff";*/
	String	MESSAGE_XML_MODUL_A_TIFF						= "message.xml.modul.a.tiff";
	String	MESSAGE_XML_MODUL_B_TIFF						= "message.xml.modul.b.tiff";
	String	MESSAGE_XML_MODUL_C_TIFF						= "message.xml.modul.c.tiff";
	String	MESSAGE_XML_MODUL_D_TIFF						= "message.xml.modul.d.tiff";
	String	MESSAGE_XML_MODUL_E_TIFF						= "message.xml.modul.e.tiff";
	String	MESSAGE_XML_MODUL_F_TIFF						= "message.xml.modul.f.tiff";
	String	MESSAGE_XML_MODUL_G_TIFF						= "message.xml.modul.g.tiff";
	String	MESSAGE_XML_MODUL_H_TIFF						= "message.xml.modul.h.tiff";

	String	MESSAGE_STEPERGEBNIS_A_SIARD					= "message.stepergebnis.a.siard";
	String	MESSAGE_STEPERGEBNIS_B_SIARD					= "message.stepergebnis.b.siard";
	String	MESSAGE_STEPERGEBNIS_C_SIARD					= "message.stepergebnis.c.siard";
	String	MESSAGE_STEPERGEBNIS_D_SIARD					= "message.stepergebnis.d.siard";
	String	MESSAGE_STEPERGEBNIS_E_SIARD					= "message.stepergebnis.e.siard";
	String	MESSAGE_STEPERGEBNIS_F_SIARD					= "message.stepergebnis.f.siard";
	String	MESSAGE_STEPERGEBNIS_G_SIARD					= "message.stepergebnis.g.siard";
	String	MESSAGE_STEPERGEBNIS_H_SIARD					= "message.stepergebnis.h.siard";
	String	MESSAGE_STEPERGEBNIS_I_SIARD					= "message.stepergebnis.i.siard";
	String	MESSAGE_STEPERGEBNIS_J_SIARD					= "message.stepergebnis.j.siard";

	String	MESSAGE_STEPERGEBNIS_A_PDFA						= "message.stepergebnis.a.pdfa";
	String	MESSAGE_STEPERGEBNIS_B_PDFA						= "message.stepergebnis.b.pdfa";
	String	MESSAGE_STEPERGEBNIS_C_PDFA						= "message.stepergebnis.c.pdfa";
	String	MESSAGE_STEPERGEBNIS_D_PDFA						= "message.stepergebnis.d.pdfa";
	String	MESSAGE_STEPERGEBNIS_E_PDFA						= "message.stepergebnis.e.pdfa";
	String	MESSAGE_STEPERGEBNIS_F_PDFA						= "message.stepergebnis.f.pdfa";
	String	MESSAGE_STEPERGEBNIS_G_PDFA						= "message.stepergebnis.g.pdfa";
	String	MESSAGE_STEPERGEBNIS_H_PDFA						= "message.stepergebnis.h.pdfa";
	String	MESSAGE_STEPERGEBNIS_I_PDFA						= "message.stepergebnis.i.pdfa";
	String	MESSAGE_STEPERGEBNIS_J_PDFA						= "message.stepergebnis.j.pdfa";

	String	MESSAGE_STEPERGEBNIS_Aa_SIP						= "message.stepergebnis.aa.sip";
	String	MESSAGE_STEPERGEBNIS_Ab_SIP						= "message.stepergebnis.ab.sip";
	String	MESSAGE_STEPERGEBNIS_Ac_SIP						= "message.stepergebnis.ac.sip";
	String	MESSAGE_STEPERGEBNIS_Ad_SIP						= "message.stepergebnis.ad.sip";
	String	MESSAGE_STEPERGEBNIS_Ae_SIP						= "message.stepergebnis.ae.sip";
	String	MESSAGE_STEPERGEBNIS_Af_SIP						= "message.stepergebnis.af.sip";
	String	MESSAGE_STEPERGEBNIS_Ba_SIP						= "message.stepergebnis.ba.sip";
	String	MESSAGE_STEPERGEBNIS_Bb_SIP						= "message.stepergebnis.bb.sip";
	String	MESSAGE_STEPERGEBNIS_Bc_SIP						= "message.stepergebnis.bc.sip";
	String	MESSAGE_STEPERGEBNIS_Bd_SIP						= "message.stepergebnis.bd.sip";
	String	MESSAGE_STEPERGEBNIS_Ca_SIP						= "message.stepergebnis.ca.sip";
	String	MESSAGE_STEPERGEBNIS_Cb_SIP						= "message.stepergebnis.cb.sip";
	String	MESSAGE_STEPERGEBNIS_Cc_SIP						= "message.stepergebnis.cc.sip";
	String	MESSAGE_STEPERGEBNIS_Cd_SIP						= "message.stepergebnis.cd.sip";

	String	MESSAGE_DASHES									= "message.dashes";
	String	MESSAGE_INDENT									= "message.indent";
	String	MESSAGE_SLASH									= "message.slash";

	String	MESSAGE_CONFIGURATION_ERROR_1					= "message.configuration.error.1";
	String	MESSAGE_CONFIGURATION_ERROR_2					= "message.configuration.error.2";
	String	MESSAGE_CONFIGURATION_ERROR_3					= "message.configuration.error.3";

	String	MESSAGE_CONFIGURATION_ERROR_NO_SIGNATURE		= "message.configuration.error.no.signature";
	String	ERROR_CANNOT_INITIALIZE_DROID					= "error.cannot.initialize.droid";

	String	ERROR_UNKNOWN									= "error.unknown";
	String	ERROR_XML_UNKNOWN									= "error.xml.unknown";

	// *************TIFF-Meldungen*************************************************************************
	// Modul A Meldungen
	String	ERROR_MODULE_A_INCORRECTFILEENDING				= "error.module.a.incorrectfileending";
	/*	String	ERROR_MODULE_A_INCORRECTFILE					= "error.module.a.incorrectfile";
	String	ERROR_MODULE_A_ISDIRECTORY						= "error.module.a.isdirectory";

	// Modul B Meldungen
	String	MESSAGE_MODULE_B_CANNOTWRITEJHOVEREPORT			= "message.module.b.cannotwritejhovereport";
	String	MESSAGE_MODULE_B_JHOVEINVALID					= "message.module.b.jhoveinvalid";
	String	MESSAGE_MODULE_B_JHOVEINVALIDFORMAT				= "message.module.b.jhoveinvalidformat";

	// Modul C-G Meldungen
	String	MESSAGE_MODULE_CG_CANNOTFINDJHOVEREPORT			= "message.module.cg.cannotfindjhovereport";
	String	MESSAGE_MODULE_CG_INVALID						= "message.module.cg.invalid";
	String	MESSAGE_MODULE_CG_JHOVENIO						= "message.module.cg.jhovenio";
	String	MESSAGE_MODULE_CG_JHOVEN15						= "message.module.cg.jhoven15";*/

	// Modul A Meldungen
	String	ERROR_XML_A_INCORRECTFILEENDING				= "error.xml.a.incorrectfileending";
	String	ERROR_XML_A_INCORRECTFILE					= "error.xml.a.incorrectfile";
	String	ERROR_XML_A_ISDIRECTORY						= "error.xml.a.isdirectory";

	// Modul B Meldungen
	String	MESSAGE_XML_B_CANNOTWRITEJHOVEREPORT			= "message.xml.b.cannotwritejhovereport";
	String	MESSAGE_XML_B_JHOVEINVALID					= "message.xml.b.jhoveinvalid";
	String	MESSAGE_XML_B_JHOVEMESSAGE				= "message.xml.b.jhovemessage";

	// Modul C-G Meldungen
	String	MESSAGE_XML_CG_CANNOTFINDJHOVEREPORT			= "message.xml.cg.cannotfindjhovereport";
	String	MESSAGE_XML_CG_INVALID						= "message.xml.cg.invalid";
	String	MESSAGE_XML_CG_JHOVENIO						= "message.xml.cg.jhovenio";

	// *************SIARD-Meldungen*************************************************************************
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

	// *************PDFA-Meldungen*************************************************************************
	// Modul A Meldungen
	String	ERROR_MODULE_A_PDFA_INCORRECTFILEENDING			= "error.module.a.pdfa.incorrectfileending";
	String	ERROR_MODULE_A_PDFA_INCORRECTFILE				= "error.module.a.pdfa.incorrectfile";
	String	ERROR_MODULE_A_PDFA_ISDIRECTORY					= "error.module.a.pdfa.isdirectory";
	String	ERROR_MODULE_A_PDFA_SERVICEFAILED				= "error.module.a.pdfa.servicefailed";
	String	ERROR_PDFTRON_MISSING							= "error.pdftron.missing";
	String	ERROR_MODULE_A_PDFA_INIT						= "error.module.a.pdfa.init";
	String	ERROR_MODULE_A_PDFA_NOCONFIG					= "error.module.a.pdfa.noconfig";
	String	INFO_MODULE_A_PDFA_PART							= "info-module.a.pdfa.part";

	String	ERROR_MODULE_AJ_PDFA_ERRORMESSAGE				= "error.module.aj.pdfa.errormessage";

	// *************SIP-Meldungen*************************************************************************
	// Modul 1 Meldungen
	String	ERROR_MODULE_AA_INCORRECTFILEENDING				= "error.module.aa.incorrectfileending";
	String	ERROR_MODULE_AA_CANNOTEXTRACTZIP				= "error.module.aa.cannotextractzip";

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
	String	MESSAGE_MODULE_CA_DROID							= "message.module.ca.droid";

	// Modul 3c Meldungen
	String	MESSAGE_MODULE_CC_INVALID						= "message.module.cc.invalid";

	
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
