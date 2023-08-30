function cchipPrintLoop() {
    var logo = document.querySelector(".logo.product");
    if(cals_env_info["tool_name"] === "callas pdfaPilot") {
        logo.src = "img/logo_pdfapilot.pdf";
    }else {
        logo.src = "img/logo_pdftoolbox.pdf";
    }

    for(var i = 0; i < cchip.pages.length; i++) {
        cchip.printPages();
    }
}