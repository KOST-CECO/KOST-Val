
function layout()
{
	cals_strings_initLanguage();
	cals_addAttachments();
	cals_addHeaders();
	cals_xmpAddMetaData();
	shrinkToFit();
	cchip.onPrintReady(cchip.printPages);
}

try
{
	cchip;
	function cchipPrintLoop()
	{
		console.log("--- cchipPrintLoop ---");
		cals_setDefaultCSS(cals_ccmip_mapping.default_css);
		cchip.onPrintReady(layout);
	}

}
catch(e)
{
	cals_strings_initLanguage();
	cals_addAttachments();
	cals_addHeaders();
	cals_setDefaultCSS(cals_ccmip_mapping.default_css);
	cals_xmpAddMetaData();
	shrinkToFit();
}
