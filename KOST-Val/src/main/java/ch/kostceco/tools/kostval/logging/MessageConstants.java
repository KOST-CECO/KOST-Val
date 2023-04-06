/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) 2012-2022 Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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

/**
 * Interface fuer den Zugriff auf Resourcen aus dem ResourceBundle.
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */
public interface MessageConstants
{

	// Initialisierung und Parameter-Ueberpruefung
	String	ERROR_IOE									= "val.error.ioe";
	String	ERROR_PARAMETER_USAGE						= "val.error.parameter.usage";
	String	ERROR_LOGDIRECTORY_NODIRECTORY				= "val.error.logdirectory.nodirectory";
	String	ERROR_LOGDIRECTORY_NOTWRITABLE				= "val.error.logdirectory.notwritable";
	String	WARNING_JARDIRECTORY_NOTWRITABLE			= "val.warning.jardirectory.notwritable";
	String	WARNING_JARDIRECTORY_NOTWRITABLEXML			= "val.warning.jardirectory.notwritablexml";
	String	ERROR_WORKDIRECTORY_NOTDELETABLE			= "val.error.workdirectory.notdeletable";
	String	ERROR_WORKDIRECTORY_NOTWRITABLE				= "val.error.workdirectory.notwritable";
	String	ERROR_WORKDIRECTORY_EXISTS					= "val.error.workdirectory.exists";
	String	ERROR_VALFILE_FILENOTEXISTING				= "val.error.valfile.filenotexisting";
	String	ERROR_CANNOTCREATEZIP						= "val.error.cannotcreatezip";
	String	ERROR_JHOVECONF_MISSING						= "val.error.jhoveconf.missing";
	String	ERROR_PARAMETER_OPTIONAL_1					= "val.error.parameter.optional.1";
	String	ERROR_INCORRECTFILEENDING					= "val.error.incorrectfileending";
	String	ERROR_INCORRECTFILEENDINGS					= "val.error.incorrectfileendings";
	String	ERROR_NOFILEENDINGS							= "val.error.nofileendings";
	String	ERROR_WRONG_JRE								= "val.error.wrong.jdk";
	String	ERROR_MISSING								= "val.error.missing";

	// Globale Meldungen
	String	MESSAGE_XML_SUMMARY_3C						= "val.message.xml.summary.3c";
	String	MESSAGE_XML_SUMMARY_NO3C					= "val.message.xml.summary.no3c";
	String	MESSAGE_XML_SUMMARY_FORMAT					= "val.message.xml.summary.format";
	String	MESSAGE_XML_SUMMARY							= "val.message.xml.summary";
	String	MESSAGE_XML_VALFILE							= "val.message.xml.valfile";
	String	MESSAGE_XML_HEADER							= "val.message.xml.header";
	String	MESSAGE_XML_START							= "val.message.xml.start";
	String	MESSAGE_XML_END								= "val.message.xml.end";
	String	MESSAGE_XML_FORMATON						= "val.message.xml.formaton";
	String	MESSAGE_XML_INFO							= "val.message.xml.info";
	String	MESSAGE_XML_CONFIG							= "val.message.xml.config";
	String	MESSAGE_TIFFVALIDATION						= "val.message.tiffvalidation";
	String	MESSAGE_SIARDVALIDATION						= "val.message.siardvalidation";
	String	MESSAGE_PDFAVALIDATION						= "val.message.pdfavalidation";
	String	MESSAGE_FORMATVALIDATION_VL					= "val.message.formatvalidation.vl";
	String	MESSAGE_JP2VALIDATION						= "val.message.jp2validation";
	String	MESSAGE_JPEGVALIDATION						= "val.message.jpegvalidation";
	String	MESSAGE_PNGVALIDATION						= "val.message.pngvalidation";
	String	MESSAGE_XMLVALIDATION						= "val.message.xmlvalidation";
	String	MESSAGE_SIPVALIDATION						= "val.message.sipvalidation";
	String	MESSAGE_XML_VALERGEBNIS						= "val.message.xml.valergebnis";
	String	MESSAGE_XML_VALTYPE							= "val.message.xml.valtype";
	String	MESSAGE_XML_AZTYPE							= "val.message.xml.aztype";
	String	MESSAGE_XML_FORMAT1							= "val.message.xml.format1";
	String	MESSAGE_XML_FORMAT2							= "val.message.xml.format2";
	String	MESSAGE_XML_LOGEND							= "val.message.xml.logend";
	String	MESSAGE_XML_SIP1							= "val.message.xml.sip1";
	String	MESSAGE_XML_SIP2							= "val.message.xml.sip2";
	String	MESSAGE_XML_VALERGEBNIS_VALID				= "val.message.xml.valergebnis.valid";
	String	MESSAGE_XML_VALERGEBNIS_INVALID				= "val.message.xml.valergebnis.invalid";
	String	MESSAGE_XML_VALERGEBNIS_AZ					= "val.message.xml.valergebnis.az";
	String	MESSAGE_XML_VALERGEBNIS_NOTAZ				= "val.message.xml.valergebnis.notaz";
	String	MESSAGE_XML_VALERGEBNIS_CLOSE				= "val.message.xml.valergebnis.close";
	String	MESSAGE_FORMATVALIDATION_DONE				= "val.message.formatvalidation.done";
	String	MESSAGE_SIPVALIDATION_DONE					= "val.message.sipvalidation.done";

	String	MESSAGE_XML_MODUL_A_TIFF					= "val.message.xml.modul.a.tiff";
	String	MESSAGE_XML_MODUL_B_TIFF					= "val.message.xml.modul.b.tiff";
	String	MESSAGE_XML_MODUL_C_TIFF					= "val.message.xml.modul.c.tiff";
	String	MESSAGE_XML_MODUL_D_TIFF					= "val.message.xml.modul.d.tiff";
	String	MESSAGE_XML_MODUL_E_TIFF					= "val.message.xml.modul.e.tiff";
	String	MESSAGE_XML_MODUL_F_TIFF					= "val.message.xml.modul.f.tiff";
	String	MESSAGE_XML_MODUL_G_TIFF					= "val.message.xml.modul.g.tiff";
	String	MESSAGE_XML_MODUL_H_TIFF					= "val.message.xml.modul.h.tiff";

	String	MESSAGE_XML_MODUL_A_SIARD					= "val.message.xml.modul.a.siard";
	String	MESSAGE_XML_MODUL_B_SIARD					= "val.message.xml.modul.b.siard";
	String	MESSAGE_XML_MODUL_C_SIARD					= "val.message.xml.modul.c.siard";
	String	MESSAGE_XML_MODUL_D_SIARD					= "val.message.xml.modul.d.siard";
	String	MESSAGE_XML_MODUL_E_SIARD					= "val.message.xml.modul.e.siard";
	String	MESSAGE_XML_MODUL_F_SIARD					= "val.message.xml.modul.f.siard";
	String	MESSAGE_XML_MODUL_G_SIARD					= "val.message.xml.modul.g.siard";
	String	MESSAGE_XML_MODUL_H_SIARD					= "val.message.xml.modul.h.siard";
	String	MESSAGE_XML_MODUL_I_SIARD					= "val.message.xml.modul.i.siard";
	String	MESSAGE_XML_MODUL_J_SIARD					= "val.message.xml.modul.j.siard";
	String	MESSAGE_XML_MODUL_W_SIARD					= "val.message.xml.modul.w.siard";

	String	MESSAGE_XML_MODUL_A_PDFA					= "val.message.xml.modul.a.pdfa";
	String	MESSAGE_XML_MODUL_B_PDFA					= "val.message.xml.modul.b.pdfa";
	String	MESSAGE_XML_MODUL_C_PDFA					= "val.message.xml.modul.c.pdfa";
	String	MESSAGE_XML_MODUL_D_PDFA					= "val.message.xml.modul.d.pdfa";
	String	MESSAGE_XML_MODUL_E_PDFA					= "val.message.xml.modul.e.pdfa";
	String	MESSAGE_XML_MODUL_F_PDFA					= "val.message.xml.modul.f.pdfa";
	String	MESSAGE_XML_MODUL_G_PDFA					= "val.message.xml.modul.g.pdfa";
	String	MESSAGE_XML_MODUL_H_PDFA					= "val.message.xml.modul.h.pdfa";
	String	MESSAGE_XML_MODUL_I_PDFA					= "val.message.xml.modul.i.pdfa";
	String	MESSAGE_XML_MODUL_J_PDFA					= "val.message.xml.modul.j.pdfa";
	String	MESSAGE_XML_MODUL_K_PDFA					= "val.message.xml.modul.k.pdfa";

	String	MESSAGE_XML_MODUL_A_JP2						= "val.message.xml.modul.a.jp2";
	String	MESSAGE_XML_MODUL_B_JP2						= "val.message.xml.modul.b.jp2";
	String	MESSAGE_XML_MODUL_C_JP2						= "val.message.xml.modul.c.jp2";
	String	MESSAGE_XML_MODUL_D_JP2						= "val.message.xml.modul.d.jp2";

	String	MESSAGE_XML_MODUL_A_JPEG					= "val.message.xml.modul.a.jpeg";
	String	MESSAGE_XML_MODUL_B_JPEG					= "val.message.xml.modul.b.jpeg";
	String	MESSAGE_XML_MODUL_C_JPEG					= "val.message.xml.modul.c.jpeg";
	String	MESSAGE_XML_MODUL_D_JPEG					= "val.message.xml.modul.d.jpeg";

	String	MESSAGE_XML_MODUL_A_PNG						= "val.message.xml.modul.a.png";
	String	MESSAGE_XML_MODUL_B_PNG						= "val.message.xml.modul.b.png";
	String	MESSAGE_XML_MODUL_C_PNG						= "val.message.xml.modul.c.png";
	String	MESSAGE_XML_MODUL_D_PNG						= "val.message.xml.modul.d.png";
	String	MESSAGE_XML_MODUL_E_PNG						= "val.message.xml.modul.e.png";
	String	MESSAGE_XML_MODUL_F_PNG						= "val.message.xml.modul.f.png";

	String	MESSAGE_XML_MODUL_A_XML						= "val.message.xml.modul.a.xml";
	String	MESSAGE_XML_MODUL_B_XML						= "val.message.xml.modul.b.xml";
	String	MESSAGE_XML_MODUL_C_XML						= "val.message.xml.modul.c.xml";

	String	MESSAGE_XML_MODUL_Aa_SIP					= "val.message.xml.modul.aa.sip";
	String	MESSAGE_XML_MODUL_Ab_SIP					= "val.message.xml.modul.ab.sip";
	String	MESSAGE_XML_MODUL_Ac_SIP					= "val.message.xml.modul.ac.sip";
	String	MESSAGE_XML_MODUL_Ad_SIP					= "val.message.xml.modul.ad.sip";
	String	MESSAGE_XML_MODUL_Ae_SIP					= "val.message.xml.modul.ae.sip";
	String	MESSAGE_XML_MODUL_Af_SIP					= "val.message.xml.modul.af.sip";
	String	MESSAGE_XML_MODUL_Ag_SIP					= "val.message.xml.modul.ag.sip";
	String	MESSAGE_XML_MODUL_Ba_SIP					= "val.message.xml.modul.ba.sip";
	String	MESSAGE_XML_MODUL_Bb_SIP					= "val.message.xml.modul.bb.sip";
	String	MESSAGE_XML_MODUL_Bc_SIP					= "val.message.xml.modul.bc.sip";
	String	MESSAGE_XML_MODUL_Bd_SIP					= "val.message.xml.modul.bd.sip";
	String	MESSAGE_XML_MODUL_Ca_SIP					= "val.message.xml.modul.ca.sip";
	String	MESSAGE_XML_MODUL_Cb_SIP					= "val.message.xml.modul.cb.sip";
	String	MESSAGE_XML_MODUL_Cc_SIP					= "val.message.xml.modul.cc.sip";
	String	MESSAGE_XML_MODUL_Cd_SIP					= "val.message.xml.modul.cd.sip";

	String	MESSAGE_XML_MODUL_A_AZ						= "val.message.xml.modul.a.az";
	String	ERROR_XML_A_AZ_INCORRECTFILE				= "val.error.xml.a.az.incorrectfile";
	String	ERROR_XML_A_NOTAZ							= "val.error.xml.a.notaz";

	String	MESSAGE_XML_CONFIGURATION_ERROR_1			= "val.message.xml.configuration.error.1";
	String	MESSAGE_XML_CONFIGURATION_ERROR_2			= "val.message.xml.configuration.error.2";
	String	MESSAGE_XML_CONFIGURATION_ERROR_3			= "val.message.xml.configuration.error.3";
	String	MESSAGE_XML_MISSING_FILE					= "val.message.xml.missing.file";
	String	MESSAGE_XML_MISSING_REPORT					= "val.message.xml.missing.report";
	String	ABORTED										= "val.aborted";

	String	MESSAGE_XML_LANGUAGE						= "val.message.xml.language";

	String	ERROR_XML_UNKNOWN							= "val.error.xml.unknown";
	String	ERROR_XML_OUTOFMEMORYERROR					= "val.error.xml.outofmemoryerror";
	String	ERROR_XML_OUTOFMEMORYMAIN					= "val.error.xml.outofmemorymain";
	String	ERROR_XML_STACKOVERFLOWMAIN					= "val.error.xml.stackoverflowmain";

	String	ERROR_XML_SERVICEFAILED_EXIT				= "val.error.xml.servicefailed.exit";
	String	ERROR_XML_SERVICEFAILED						= "val.error.xml.servicefailed";
	String	MESSAGE_XML_SERVICEINVALID					= "val.message.xml.serviceinvalid";
	String	MESSAGE_XML_SERVICEMESSAGE					= "val.message.xml.servicemessage";

	// *************TIFF-Meldungen*************************************************************************
	// Modul A Meldungen --> neu in der Erkennung

	// Modul B Meldungen
	String	MESSAGE_XML_B_CANNOTWRITEJHOVEREPORT		= "val.message.xml.b.cannotwritejhovereport";

	// Modul C-G Meldungen
	String	MESSAGE_XML_CG_CANNOTFINDETREPORT			= "val.message.xml.cg.cannotfindetreport";
	String	MESSAGE_XML_CG_INVALID						= "val.message.xml.cg.invalid";
	String	MESSAGE_XML_CG_ETNIO						= "val.message.xml.cg.etnio";

	// *************SIARD-Meldungen*************************************************************************
	// Modul A Meldungen
	String	ERROR_XML_A_DEFLATED						= "val.error.xml.a.deflated";
	String	ERROR_XML_A_INCORRECTZIP					= "val.error.xml.a.incorrectzip";

	// Modul B Meldungen
	String	MESSAGE_XML_B_NOTALLOWEDFILE				= "val.message.xml.b.notallowedfile";
	String	MESSAGE_XML_B_CONTENT						= "val.message.xml.b.content";
	String	MESSAGE_XML_B_HEADER						= "val.message.xml.b.header";

	// Modul C Meldungen
	String	MESSAGE_XML_C_NOMETADATAFOUND				= "val.message.xml.c.nometadatafound";
	String	MESSAGE_XML_C_NOMETADATAXSD					= "val.message.xml.c.nometadataxsd";
	String	MESSAGE_XML_C_METADATA_ERRORS				= "val.message.xml.c.metadata.errors";
	String	MESSAGE_XML_C_METADATA_ORIGERRORS			= "val.message.xml.c.metadata.origerrors";
	String	MESSAGE_XML_C_METADATA_NSFOUND				= "val.message.xml.c.metadata.nsfound";
	String	MESSAGE_XML_C_INVALID_VERSION				= "val.message.xml.c.invalid.version";

	// Modul D Meldungen
	String	MESSAGE_XML_D_INVALID_FOLDER				= "val.message.xml.d.invalid.folder";
	String	MESSAGE_XML_D_MISSING_FOLDER				= "val.message.xml.d.missing.folder";
	String	MESSAGE_XML_D_INVALID_FILE					= "val.message.xml.d.invalid.file";
	String	MESSAGE_XML_D_MISSING_FILE					= "val.message.xml.d.missing.file";
	String	MESSAGE_XML_D_INVALID_XMLNS					= "val.message.xml.d.invalid.xmlns";

	// Modul E Meldungen
	String	MESSAGE_XML_E_PROPERTIES_ERROR				= "val.message.xml.e.properties.error";
	String	MESSAGE_XML_E_PATH_ERROR					= "val.message.xml.e.path.error";
	String	MESSAGE_XML_E_EXTRACT_ERROR					= "val.message.xml.e.extract.error";
	String	MESSAGE_XML_E_METADATA_ACCESS_ERROR			= "val.message.xml.e.metadata.access.error";
	String	MESSAGE_XML_E_XML_ACCESS_ERROR				= "val.message.xml.e.xml.access.error";
	String	MESSAGE_XML_E_PREVALIDATION_ERROR			= "val.message.xml.e.prevalidation.error";
	String	MESSAGE_XML_E_INVALID_ATTRIBUTE_COUNT		= "val.message.xml.e.attribute.count.validation.failed";
	String	MESSAGE_XML_E_INVALID_ATTRIBUTE_OCCURRENCE	= "val.message.xml.e.attribute.occurrence.validation.failed";
	String	MESSAGE_XML_E_INVALID_ATTRIBUTE_TYPE		= "val.message.xml.e.attribute.type.validation.failed";
	String	MESSAGE_XML_E_ARRAY							= "val.message.xml.e.array.failed";
	String	MESSAGE_XML_E_TYPE_NOT_VALIDATED			= "val.message.xml.e.type.not.validated";

	// Modul F Meldungen
	String	MESSAGE_XML_F_PROPERTIES_ERROR				= "val.message.xml.f.properties.error";
	String	MESSAGE_XML_F_PATH_ERROR					= "val.message.xml.f.path.error";
	String	MESSAGE_XML_F_INVALID_TABLE_XML_FILES		= "val.message.xml.f.invalid.table.xml.files";

	// Modul G Meldungen
	String	MESSAGE_XML_G_DUPLICATE_SCHEMA				= "val.message.xml.g.duplicate.schema";
	String	MESSAGE_XML_G_DUPLICATE_TABLE				= "val.message.xml.g.duplicate.table";
	String	MESSAGE_XML_G_DUPLICATE_COLUMN				= "val.message.xml.g.duplicate.column";

	// Modul H Meldungen
	String	MESSAGE_XML_H_INVALID_FOLDER				= "val.message.xml.h.invalid.folder";
	String	MESSAGE_XML_H_INVALID_XML					= "val.message.xml.h.invalid.xml";
	String	MESSAGE_XML_H_TABLE_NOT_VALIDATED1			= "val.message.xml.h.table.not.validated1";

	// Modul I Meldungen
	String	MESSAGE_XML_I_NOTALLOWEDEXT					= "val.message.xml.i.notallowedext";
	String	MESSAGE_XML_I_SIARDVERSION					= "val.message.xml.i.siardversion";

	// Modul J Meldungen
	String	MESSAGE_XML_J_INVALID_ENTRY					= "val.message.xml.j.invalid.entry";

	// Modul W Meldungen
	String	MESSAGE_XML_W_WARNING_INITVALUE				= "val.message.xml.w.warning.initvalue";

	// *************PDFA-Meldungen*************************************************************************
	// Modul A Meldungen
	String	ERROR_XML_CALLAS_MISSING					= "val.error.xml.callas.missing";
	String	ERROR_XML_CALLAS_MISSING2					= "val.error.xml.callas.missing2";
	String	ERROR_XML_CALLAS_FATAL						= "val.error.xml.callas.fatal";
	String	ERROR_XML_CALLAS_FATAL2						= "val.error.xml.callas.fatal2";
	String	ERROR_XML_A_PDFA_NOCONFIG					= "val.error.xml.a.pdfa.noconfig";
	String	WARNING_XML_A_PDFTOOLS_LICENSE_1			= "val.warning.xml.a.pdftools.license.1";
	String	WARNING_XML_A_PDFTOOLS_LICENSE_2			= "val.warning.xml.a.pdftools.license.2";
	String	ERROR_XML_A_PDFTOOLS_LICENSE				= "val.error.xml.a.pdftools.license";
	String	ERROR_XML_A_PDFTOOLS_ENCRYPTED				= "val.error.xml.a.pdftools.encrypted";
	String	ERROR_XML_A_PDFTOOLS_DAMAGED				= "val.error.xml.a.pdftools.damaged";
	String	WARNING_XML_A_PDFA3							= "val.warning.xml.a.pdfa3";

	String	ERROR_XML_AI_0								= "val.error.xml.ai.0";
	String	ERROR_XML_AI_1								= "val.error.xml.ai.1";
	String	ERROR_XML_AI_2								= "val.error.xml.ai.2";
	String	ERROR_XML_AI_3								= "val.error.xml.ai.3";
	String	ERROR_XML_AI_4								= "val.error.xml.ai.4";
	String	ERROR_XML_AI_5								= "val.error.xml.ai.5";
	String	ERROR_XML_AI_6								= "val.error.xml.ai.6";
	String	ERROR_XML_AI_7								= "val.error.xml.ai.7";
	String	ERROR_XML_AI_8								= "val.error.xml.ai.8";
	String	ERROR_XML_AI_9								= "val.error.xml.ai.9";
	String	ERROR_XML_AI_10								= "val.error.xml.ai.10";
	String	ERROR_XML_AI_11								= "val.error.xml.ai.11";
	String	ERROR_XML_AI_12								= "val.error.xml.ai.12";
	String	ERROR_XML_AI_13								= "val.error.xml.ai.13";
	String	ERROR_XML_AI_14								= "val.error.xml.ai.14";
	String	ERROR_XML_AI_15								= "val.error.xml.ai.15";
	String	ERROR_XML_AI_16								= "val.error.xml.ai.16";
	String	ERROR_XML_AI_17								= "val.error.xml.ai.17";
	String	ERROR_XML_AI_18								= "val.error.xml.ai.18";

	String	ERROR_XML_AI_TRANSLATE						= "val.error.xml.ai.translate";

	String	ERROR_XML_J_JBIG2							= "val.error.xml.j.jbig2";

	String	ERROR_XML_K_OVERVIEW						= "val.error.xml.k.overview";
	String	ERROR_XML_K_OVERVIEW2						= "val.error.xml.k.overview2";
	String	ERROR_XML_K_DETAIL							= "val.error.xml.k.detail";

	// *************JP2-Meldungen*************************************************************************
	// Modul A Meldungen

	// Modul A-D Meldungen (Zusammengefasste Jylyzer-Meldungen)
	String	ERROR_XML_A_JP2_SIGNATURE					= "val.error.xml.a.jp2.signature";
	String	ERROR_XML_A_JP2_FILETYPE					= "val.error.xml.a.jp2.filetype";
	String	ERROR_XML_A_JP2_HEADER						= "val.error.xml.a.jp2.header";
	String	ERROR_XML_A_JP2_IMAGE						= "val.error.xml.a.jp2.image";
	String	ERROR_XML_A_JP2_COLOUR						= "val.error.xml.a.jp2.colour";
	String	ERROR_XML_A_JP2_BITSPC						= "val.error.xml.a.jp2.bitspc";
	String	ERROR_XML_A_JP2_PALETTE						= "val.error.xml.a.jp2.palette";
	String	ERROR_XML_A_JP2_MAPPING						= "val.error.xml.a.jp2.mapping";
	String	ERROR_XML_A_JP2_CHANNEL						= "val.error.xml.a.jp2.channel";
	String	ERROR_XML_A_JP2_RESOLUTION					= "val.error.xml.a.jp2.resolution";

	String	ERROR_XML_B_JP2_XML							= "val.error.xml.b.jp2.xml";
	String	ERROR_XML_B_JP2_UUIDINFO					= "val.error.xml.b.jp2.uuidinfo";
	String	ERROR_XML_B_JP2_UUID						= "val.error.xml.b.jp2.uuid";
	String	ERROR_XML_B_JP2_INTELLECTUAL				= "val.error.xml.b.jp2.intellectual";

	String	ERROR_XML_C_JP2_CODEBOX						= "val.error.xml.c.jp2.codebox";
	String	ERROR_XML_C_JP2_SIZ							= "val.error.xml.c.jp2.siz";
	String	ERROR_XML_C_JP2_COC							= "val.error.xml.c.jp2.coc";

	String	ERROR_XML_C_JP2_RGN							= "val.error.xml.c.jp2.rgn";
	String	ERROR_XML_C_JP2_QCD							= "val.error.xml.c.jp2.qcd";
	String	ERROR_XML_C_JP2_QCC							= "val.error.xml.c.jp2.qcc";
	String	ERROR_XML_C_JP2_POC							= "val.error.xml.c.jp2.poc";
	String	ERROR_XML_C_JP2_CRG							= "val.error.xml.c.jp2.crg";
	String	ERROR_XML_C_JP2_COM							= "val.error.xml.c.jp2.com";
	String	ERROR_XML_C_JP2_TILE						= "val.error.xml.c.jp2.tile";
	String	ERROR_XML_C_JP2_SOC							= "val.error.xml.c.jp2.soc";
	String	ERROR_XML_C_JP2_EOC							= "val.error.xml.c.jp2.eoc";
	String	ERROR_XML_C_JP2_COD							= "val.error.xml.c.jp2.cod";

	String	ERROR_XML_D_JP2_UNKNOWN						= "val.error.xml.d.jp2.unknown";

	// *************JPEG-Meldungen*************************************************************************
	// Modul A Meldungen
	String	ERROR_XML_A_JPEG_JIIO_FILETYPE				= "val.error.xml.a.jpeg.jiio.filetype";
	String	ERROR_XML_A_JPEG_JIIO_SCANFAILED			= "val.error.xml.a.jpeg.jiio.scanfailed";

	String	ERROR_XML_A_UNS_IMAGE						= "val.error.xml.a.uns.image";
	String	ERROR_XML_A_ARITH_BAD_CODE					= "val.error.xml.a.arith.bad.code";
	String	ERROR_XML_A_BADTHUMBNAILSIZE				= "val.error.xml.a.badthumbnailsize";
	String	ERROR_XML_A_EXTRANEOUS_DATA					= "val.error.xml.a.extraneous.data";
	String	ERROR_XML_A_HIT_MARKER						= "val.error.xml.a.hit.marker";
	String	ERROR_XML_A_HUFF_BAD_CODE					= "val.error.xml.a.huff.bad.code";
	String	ERROR_XML_A_HUFF_MISSING_CODE				= "val.error.xml.a.huff.missing.code";
	String	ERROR_XML_A_INPUT_EOF						= "val.error.xml.a.input.eof";
	String	ERROR_XML_A_JPEG_EOF						= "val.error.xml.a.jpeg.eof";
	String	ERROR_XML_A_MUST_RESYNC						= "val.error.xml.a.must.resync";
	String	ERROR_XML_A_NO_IMAGE						= "val.error.xml.a.no.image";

	String	ERROR_XML_B_BAD_COMPONENT_ID				= "val.error.xml.b.bad.component.id";
	String	ERROR_XML_B_EMPTY_IMAGE						= "val.error.xml.b.empty.image";
	String	ERROR_XML_B_KC_EOI_EOI						= "val.error.xml.b.kc.eoi.eoi";
	String	ERROR_XML_B_KC_MISS_EOI						= "val.error.xml.b.kc.miss.eoi";
	String	ERROR_XML_B_KC_MISS_SOI						= "val.error.xml.b.kc.miss.soi";
	String	ERROR_XML_B_KC_NO_EOI						= "val.error.xml.b.kc.no.eoi";
	String	ERROR_XML_B_KC_NO_SOI						= "val.error.xml.b.kc.no.soi";
	String	ERROR_XML_B_KC_NO_SOI_EOI					= "val.error.xml.b.kc.no.soi.eoi";
	String	ERROR_XML_B_NO_ARITH_TABLE					= "val.error.xml.b.no.arith.table";
	String	ERROR_XML_B_NO_HUFF_TABLE					= "val.error.xml.b.no.huff.table";
	String	ERROR_XML_B_NO_SOI							= "val.error.xml.b.no.soi";
	String	ERROR_XML_B_NOT_SEQUENTIAL					= "val.error.xml.b.not.sequential";
	String	ERROR_XML_B_SOF_DUPLICATE					= "val.error.xml.b.sof.duplicate";
	String	ERROR_XML_B_SOF_NO_SOS						= "val.error.xml.b.sof.no.sos";
	String	ERROR_XML_B_SOI_DUPLICATE					= "val.error.xml.b.soi.duplicate";
	String	ERROR_XML_B_SOS_NO_SOF						= "val.error.xml.b.sos.no.sof";
	String	ERROR_XML_B_NO_EOI							= "val.error.xml.b.no.eoi";
	String	ERROR_XML_B_NO_JFIF_IN_THUMB				= "val.error.xml.b.no.jfif.in.thumb";
	String	ERROR_XML_B_INVALID_ICC						= "val.error.xml.b.invalid.icc";

	String	ERROR_XML_C_TRANSLATE						= "val.error.xml.c.translate";

	// *************PNG-Meldungen*************************************************************************
	// Modul A Meldungen

	// Modul A-F Meldungen
	String	ERROR_XML_AF_PNG_TRANSLATE					= "val.error.xml.af.png.translate";

	// *************XML-Meldungen*************************************************************************
	// Modul A Meldungen

	// Modul B Meldungen
	String	ERROR_XML_B_XML_XMLLINT_FAILSTR				= "val.error.xml.b.xml.xmllint.failstr";

	// Modul C Meldungen
	String	ERROR_XML_C_XML_NOXSDFILE					= "val.error.xml.c.xml.noxsdfile";
	String	ERROR_XML_C_XML_NOSCHEMA					= "val.error.xml.c.xml.noschema";

	// *************SIP-Meldungen*************************************************************************
	// Modul 1a Meldungen
	String	ERROR_XML_AA_INCORRECTFILEENDING			= "val.error.xml.aa.incorrectfileending";
	String	ERROR_XML_AA_CANNOTEXTRACTZIP				= "val.error.xml.aa.cannotextractzip";
	String	ERROR_XML_CONIG_SIP							= "val.error.xml.config.sip";

	// Modul 1b Meldungen
	String	ERROR_XML_AB_CONTENT						= "val.error.xml.ab.content";
	String	ERROR_XML_AB_HEADER							= "val.error.xml.ab.header";
	String	ERROR_XML_AB_XSD							= "val.error.xml.ab.xsd";
	String	ERROR_XML_AB_METADATA						= "val.error.xml.ab.metadata";

	// Modul 1c Meldungen
	String	MESSAGE_XML_AC_NOTALLOWEDFILE				= "val.message.xml.ac.notallowedfile";
	String	MESSAGE_XML_AC_NOTALLOWEDV					= "val.message.xml.ac.notallowedv";
	String	MESSAGE_XML_AC_MISSINGFILE					= "val.message.xml.ac.missingfile";
	String	MESSAGE_XML_AC_PATHTOOLONG					= "val.message.xml.ac.pathtoolong";
	String	MESSAGE_XML_AC_FILENAMETOOLONG				= "val.message.xml.ac.filenametoolong";
	String	MESSAGE_XML_AC_INVALIDCHARACTERS			= "val.message.xml.ac.invalidcharacters";
	String	MESSAGE_XML_AC_INVALIDREGEX					= "val.message.xml.ac.invalidregex";
	String	MESSAGE_XML_AC_INVALIDFILENAME				= "val.message.xml.ac.invalidfilename";

	// Modul 1d Meldungen
	String	ERROR_XML_AD_INVALID_XML					= "val.error.xml.ad.invalid.xml";
	String	ERROR_XML_AD_NSFOUND						= "val.error.xml.ad.nsfound";
	String	ERROR_XML_AD_NOOSP							= "val.error.xml.ad.noosp";
	String	ERROR_XML_AD_UADEP							= "val.error.xml.ad.uadep";
	String	ERROR_XML_AD_AVAN_WARNING					= "val.error.xml.ad.avan.warning";

	// Modul 1e Meldungen
	String	ERROR_XML_AE_NOMETADATAFOUND				= "val.error.xml.ae.nometadatafound";
	String	ERROR_XML_AE_ABLIEFERUNGSTYPUNDEFINED		= "val.error.xml.ae.ablieferungstypundefined";

	// Modul 1f Meldungen
	String	ERROR_XML_AF_FILESIPWITHOUTPRIMARYDATA		= "val.error.xml.af.filesipwithoutprimarydata";

	// Modul 1g Meldungen
	String	ERROR_XML_AG_WARNINGBIGSIP					= "val.error.xml.ag.warningbigsip";
	String	ERROR_XML_AG_TOOMANYFILESFOLDER				= "val.error.xml.ag.toomanyfilesfolder";
	String	ERROR_XML_AG_TOOMANYFILESSIP				= "val.error.xml.ag.toomanyfilessip";

	// Modul 2a Meldungen
	String	MESSAGE_XML_BA_FILEMISSING					= "val.message.xml.ba.filemissing";

	// Modul 2b Meldungen
	String	MESSAGE_XML_BB_FILEMISSING					= "val.message.xml.bb.filemissing";
	String	MESSAGE_XML_BB_FILEMISSINGO					= "val.message.xml.bb.filemissingo";

	// Modul 2c Meldungen
	String	ERROR_XML_BC_CANNOTPROCESSMD5				= "val.error.xml.bc.cannotprocessmd5";
	String	ERROR_XML_BC_CANNOTCLOSESTREAMMD5			= "val.error.xml.bc.cannotclosestreammd5";
	String	MESSAGE_XML_BC_WRONGMD5						= "val.error.xml.bc.wrongmd5";

	// Modul 2d Meldungen
	String	MESSAGE_XML_BD_MISSINGINABLIEFERUNG			= "val.message.xml.bd.missinginablieferung";
	String	MESSAGE_XML_BD_MISSINGININHALTSVERZEICHNIS	= "val.message.xml.bd.missingininhaltsverzeichnis";
	String	MESSAGE_XML_BD_WARNINGMISSINGINFOID			= "val.message.xml.bd.warningmissinginfoid";
	String	MESSAGE_XML_BD_WARNINGMISSINGREPID			= "val.message.xml.bd.warningmissingrepid";

	// Modul 3a Meldungen
	String	MESSAGE_XML_CA_FILES						= "val.message.xml.ca.files";

	// Modul 3b Meldungen

	// Modul 3c Meldungen

	// Modul 3d Meldungen
	String	ERROR_XML_CD_DATUM_ENTSTEHUNG_IN_FUTURE		= "val.error.xml.cd.datum.entstehung.in.future";
	String	ERROR_XML_CD_DATUM_IN_FUTURE				= "val.error.xml.cd.datum.in.future";

	String	ERROR_XML_CD_INVALID_ABLIEFERUNG_RANGE		= "val.error.xml.cd.invalid.ablieferung.range";
	String	ERROR_XML_CD_INVALID_DOSSIER_RANGE_CA		= "val.error.xml.cd.invalid.dossier.range.ca";
	String	ERROR_XML_CD_INVALID_DOSSIER_RANGE_CA_ABL	= "val.error.xml.cd.invalid.dossier.range.ca.abl";
	String	ERROR_XML_CD_INVALID_DOKUMENT_RANGE_CA		= "val.error.xml.cd.invalid.dokument.range.ca";
	String	ERROR_XML_CD_UNPARSEABLE_DATE				= "val.error.xml.cd.unparseable.date";
	String	ERROR_XML_CD_WARNING_ANMERKUNG_CA			= "val.error.xml.cd.warning.anmerkung.ca";
}
