/*
	The bc_lookup table contains definitions of extra parameters for each symbology
	'n' the parmeter name attribute
	'v' the parmeter value attribute

	e.q. Quietzones:
	For more details see section 4.4 Quiet Zone in the Barcode Reference. 

	quietzoneleft
	quietzoneright
	quietzonetop
	quietzonebottom
	quietzoneunit [mm, ", mils, pixel, X], X: multiples of module width

	List of all supported custom barcode parameters names:

	"activetextindex"
	"autocorrect"
	"aztec_appendactive"
	"aztec_appenddata_index"
	"aztec_appenddata_messageid"
	"aztec_appenddata_total"
	"aztec_enforcebinaryencoding"
	"aztec_errorcorrection"
	"aztec_format_format"
	"aztec_format_specifier"
	"aztec_runemode"
	"aztec_size"
	"barshape_imgfile"
	"barshape_shape"
	"barwidthreduction"
	"bearerbars"
	"bearerwidth"
	"bkmode"
	"cbf_columns"
	"cbf_format"
	"cbf_rowheight"
	"cbf_rows"
	"cbf_rowseparatorheight"
	"cdmethod"
	"characterspacing"
	"codepage"
	"codepagecustom"
	"colormode"
	"compression"
	"dcconversion"
	"dcompositecomponent"
	"decoder"
	"displaytext"
	"dm_append_fileid"
	"dm_append_index"
	"dm_append_sum"
	"dm_enforcebinaryencoding"
	"dm_format"
	"dm_rectangular"
	"dm_size"
	"dotcode_append_index"
	"dotcode_append_total"
	"dotcode_appendactive"
	"dotcode_enforcebinaryencoding"
	"dotcode_format_format"
	"dotcode_format_specifier"
	"dotcode_mask"
	"dotcode_printdirection"
	"dotcode_size_mode"
	"dotcode_size_size"
	"drawmode_ext"
	"drawmode"
	"encodingmode"
	"epssubstwdevicefonts"
	"format"
	"hanxin_eclevel"
	"hanxin_enforcebinaryencoding"
	"hanxin_mask"
	"hanxin_version"
	"hres"
	"logfont_charset"
	"logfont_clipprecision"
	"logfont_escapement"
	"logfont_facename"
	"logfont_height"
	"logfont_italic"
	"logfont_orientation"
	"logfont_outprecision"
	"logfont_pitchandfamily"
	"logfont_quality"
	"logfont_strikeout"
	"logfont_underline"
	"logfont_weight"
	"logfont_width"
	"lp160_5_bitsflipped"
	"lp160_5_encodeplatemessagedirectly"
	"maxi_append_index"
	"maxi_append_sum"
	"maxi_mode"
	"maxi_scm_countrycode"
	"maxi_scm_postalcode"
	"maxi_scm_serviceclass"
	"maxi_undercut"
	"maxi_usepreamble_date"
	"maxi_usepreamble_use"
	"mirror"
	"modulewidth"
	"modwidth"
	"mpdf417_mode"
	"mpdf417_version"
	"mqr_mask"
	"mqr_version"
	"multibc_columns"
	"multibc_datalimit"
	"multibc_dynamicboundingrect"
	"multibc_enabled"
	"multibc_hdist"
	"multibc_rows"
	"multibc_structapp"
	"multibc_vdist"
	"mustfit"
	"notchheight"
	"options"
	"pclmode"
	"pdf417_addressee"
	"pdf417_checksum"
	"pdf417_columns"
	"pdf417_eclevel"
	"pdf417_encodingmode"
	"pdf417_fileid"
	"pdf417_filename"
	"pdf417_filesize"
	"pdf417_rowcolratio"
	"pdf417_rowheight"
	"pdf417_rows"
	"pdf417_segcount"
	"pdf417_segindex"
	"pdf417_seglast"
	"pdf417_sender"
	"pdf417_timestamp"
	"qr_append_index"
	"qr_append_parity"
	"qr_append_sum"
	"qr_eclevel"
	"qr_fmtappindicator"
	"qr_format"
	"qr_kanjichinesecompaction"
	"qr_mask"
	"qr_version"
	"quietzonebottom"
	"quietzoneleft"
	"quietzoneright"
	"quietzonetop"
	"quietzoneunit"
	"ratio"
	"rotation"
	"rss_segmperrow"
	"sizemode"
	"swap_foreground_background"
	"textalignment"
	"textdistance"
	"textebcdic_codepage"
	"textebcdic_text"
	"textplacement"
	"textposition_clipping"
	"textposition_rect_bottom"
	"textposition_rect_left"
	"textposition_rect_right"
	"textposition_rect_top"
	"textposition_wordwrap"
	"textrotation"
	"vres"

	The following parameter names must not be used in bc_lookup:

	"type"
	"data"
*/

var bc_lookup = 
{
'Code 11':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.191mm '}],'height_mm': 14.000 },
'Code 2 of 5 Standard':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 2 of 5 Interleaved':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 2 of 5 IATA':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 2 of 5 Matrix':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 2 of 5 DataLogic':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'Code 2 of 5 Industry':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 39':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 16.000 },
'Code 39 Full ASCII':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 16.000 },
'EAN 8':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 7'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 21.640 },
'EAN 8 + 2 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 21.640 },
'EAN 8 + 5 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 21.640 },
'EAN 13':{'params': [  {'n':'quietzoneleft', 'v':' 11'}, {'n':'quietzoneright', 'v':' 7'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'EAN 13 + 2 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'EAN 13 + 5 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'EAN/UCC 128':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'UPC 12':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 9'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.100 },
'Codabar 2 Widths':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 128':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'DP Leitcode':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 25.400 },
'DP Identcode':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 25.400 },
'ISBN 13 + 5 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'ISMN':{'params': [  {'n':'quietzoneleft', 'v':' 11'}, {'n':'quietzoneright', 'v':' 7'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'Code 93':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'ISSN':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'ISSN + 2 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'Flattermarken':{'params': [  {'n':'quietzoneleft', 'v':' '}, {'n':'quietzoneright', 'v':' '}, {'n':'quietzoneunit', 'v':' '}, {'n':'modulewidth', 'v':' 2mm'}],'height_mm': 6.000 },
'GS1 DataBar (RSS-14)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 16.000 },
'GS1 DataBar Limited (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 16.000 },
'GS1 DataBar Expanded (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'Telepen Alpha':{'params': [  {'n':'quietzoneleft', 'v':' '}, {'n':'quietzoneright', 'v':' '}, {'n':'quietzoneunit', 'v':' '}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'UCC 128':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'UPC A':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 9'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.100 },
'UPC A + 2 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.100 },
'UPC A + 5 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.100 },
'UPC E':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 7'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 21.640 },
'UPC E + 2 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 21.640 },
'UPC E + 5 Digits':{'params': [  {'n':'quietzoneleft', 'v':' 9'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 21.640 },
'USPS PostNet 5 (ZIP)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'USPS PostNet 6 (ZIP+cd)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'USPS PostNet 9 (ZIP+4)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'USPS PostNet 10 (ZIP+4+cd)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'USPS PostNet 11 (ZIP+4+2)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'USPS PostNet 12 (ZIP+4+2+cd)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'Plessey':{'params': [  {'n':'quietzoneleft', 'v':' 12'}, {'n':'quietzoneright', 'v':' 12'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'MSI':{'params': [  {'n':'quietzoneleft', 'v':' 12'}, {'n':'quietzoneright', 'v':' 12'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'SSCC 18':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'LOGMARS':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 16.000 },
'Pharmacode One-Track':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' 2mm'}],'height_mm': 8.000 },
'PZN7':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Pharmacode Two-Track':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '}],'height_mm': 8.000 },
'Brazilian CEPNet':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 0.762 },
'PDF417 Truncated':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 0.762 },
'MaxiCode':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'QR-Code':{'params': [  {'n':'quietzoneleft', 'v':' 4'}, {'n':'quietzoneright', 'v':' 4'}, {'n':'quietzonetop', 'v':' 4'}, {'n':'quietzonebottom', 'v':' 4'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'Code 128 Subset A':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 128 Subset B':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 128 Subset C':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Code 93 Full ASCII':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Australian Post Custom':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'Australian Post Custom2':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'Australian Post Custom3':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'Australian Post Reply Paid':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'Australian Post Routing':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'Australian Post Redirect':{'params': [  {'n':'quietzoneleft', 'v':' 6'}, {'n':'quietzoneright', 'v':' 6'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 5.000 },
'ISBN 13':{'params': [  {'n':'quietzoneleft', 'v':' 7'}, {'n':'quietzoneright', 'v':' 5'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.33mm'}],'height_mm': 26.26 },
'Royal Mail 4 State (RM4SCC)':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '}],'height_mm': 5.100 },
'Data Matrix':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'EAN 14 (GTIN 14)':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'VIN / FIN':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 16.000 },
'Codablock-F':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzonetop', 'v':' 10'}, {'n':'quietzonebottom', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'} ], 'height_mm': 2.540 },
'NVE 18':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'Japanese Postal':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.500 },
'Korean Postal Authority':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 4.000 },
'GS1 DataBar Truncated (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 10.200 },
'GS1 DataBar Stacked (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 4.410 },
'GS1 DataBar Stacked Omnidir (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 23.370 },
'GS1 DataBar Expanded Stacked (RSS)':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 16.000 },
'PLANET 12 digit':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'PLANET 14 digit':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.200 },
'Micro PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 0.762 },
'USPS Intelligent Mail Barcode (IM)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 3.700 },
'Plessey Bidirectional':{'params': [  {'n':'quietzoneleft', 'v':' 12'}, {'n':'quietzoneright', 'v':' 12'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'Telepen':{'params': [  {'n':'quietzoneleft', 'v':' '}, {'n':'quietzoneright', 'v':' '}, {'n':'quietzoneunit', 'v':' '}, {'n':'modulewidth', 'v':' '}],'height_mm': 14.000 },
'GS1 128 (EAN/UCC 128)':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'ITF 14 (GTIN 14)':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.051mm'}],'height_mm': 14.000 },
'KIX':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' 0.38mm'} ], 'height_mm': 5.100 },
'Code 32':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.252mm'}],'height_mm': 16.000 },
'Aztec Code':{'params': [  {'n':'quietzoneleft', 'v':' 0'}, {'n':'quietzoneright', 'v':' 0'}, {'n':'quietzonetop', 'v':' 0'}, {'n':'quietzonebottom', 'v':' 0'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'DAFT Code':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzoneunit', 'v':' mm'}, {'n':'modulewidth', 'v':' '}],'height_mm': 10.200 },
'Italian Postal 2 of 5':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 10.200 },
'DPD':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 10.200 },
'Micro QR-Code':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'HIBC LIC 128':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'HIBC LIC 39':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'HIBC PAS 128':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'HIBC PAS 39':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'HIBC LIC Data Matrix':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'HIBC PAS Data Matrix':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'HIBC LIC QR-Code':{'params': [  {'n':'quietzoneleft', 'v':' 4'}, {'n':'quietzoneright', 'v':' 4'}, {'n':'quietzonetop', 'v':' 4'}, {'n':'quietzonebottom', 'v':' 4'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'HIBC PAS QR-Code':{'params': [  {'n':'quietzoneleft', 'v':' 4'}, {'n':'quietzoneright', 'v':' 4'}, {'n':'quietzonetop', 'v':' 4'}, {'n':'quietzonebottom', 'v':' 4'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'HIBC LIC PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0.762 },
'HIBC PAS PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 2'}, {'n':'quietzoneright', 'v':' 2'}, {'n':'quietzonetop', 'v':' 2'}, {'n':'quietzonebottom', 'v':' 2'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0.762 },
'HIBC LIC Micro PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0.762 },
'HIBC PAS Micro PDF417':{'params': [  {'n':'quietzoneleft', 'v':' 1'}, {'n':'quietzoneright', 'v':' 1'}, {'n':'quietzonetop', 'v':' 1'}, {'n':'quietzonebottom', 'v':' 1'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0.762 },
'HIBC LIC Codablock-F':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzonetop', 'v':' 10'}, {'n':'quietzonebottom', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'} ], 'height_mm': 2.540 },
'HIBC PAS Codablock-F':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzonetop', 'v':' 10'}, {'n':'quietzonebottom', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'} ], 'height_mm': 2.540 },
'QR-Code 2005':{'params': [  {'n':'quietzoneleft', 'v':' 4'}, {'n':'quietzoneright', 'v':' 4'}, {'n':'quietzonetop', 'v':' 4'}, {'n':'quietzonebottom', 'v':' 4'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'PZN8':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.19mm'}],'height_mm': 14.000 },
'DotCode':{'params': [  {'n':'quietzoneleft', 'v':' 3'}, {'n':'quietzoneright', 'v':' 3'}, {'n':'quietzonetop', 'v':' 3'}, {'n':'quietzonebottom', 'v':' 3'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'Han Xin Code':{'params': [  {'n':'quietzoneleft', 'v':' 3'}, {'n':'quietzoneright', 'v':' 3'}, {'n':'quietzonetop', 'v':' 3'}, {'n':'quietzonebottom', 'v':' 3'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 0 },
'USPS Intelligent Mail Package (IMpb)':{'params': [  {'n':'quietzoneleft', 'v':' 0.125'}, {'n':'quietzoneright', 'v':' 0.125'}, {'n':'quietzonetop', 'v':' 0.04'}, {'n':'quietzonebottom', 'v':' 0.04'}, {'n':'quietzoneunit', 'v':' in'}, {'n':'modulewidth', 'v':' '} ], 'height_mm': 25.400 },
'Swedish Postal Shipment Item ID':{'params': [  {'n':'quietzoneleft', 'v':' 10'}, {'n':'quietzoneright', 'v':' 10'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.28mm'}],'height_mm': 25.000 },
'Royal Mail Complex Mail Data Mark (CMDM) Mailmark®':{'params': [  {'n':'quietzoneleft', 'v':' 4'}, {'n':'quietzoneright', 'v':' 4'}, {'n':'quietzonetop', 'v':' 4'}, {'n':'quietzonebottom', 'v':' 4'}, {'n':'quietzoneunit', 'v':' X'}, {'n':'modulewidth', 'v':' 0.5mm'} ], 'height_mm': 5.100 },
};
