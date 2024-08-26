
function addPageInformationVisual(element, data) {
    const selectedPageIndex = (cals_sec_info["pageinfo"]["on_pages"][0] - 1) || 0;
    const selectedPageCropbox = data["doc"]["pages"][selectedPageIndex]["cropBox"];
    const selectedPageCropboxWidth = selectedPageCropbox["right"] - selectedPageCropbox["left"];
    const selectedPageCropboxHeight = selectedPageCropbox["top"] - selectedPageCropbox["bottom"];
    if(selectedPageCropboxWidth - 100 > selectedPageCropboxHeight) {
        element.parentElement.classList.add("landscape");
    }

    const img = element.querySelector("img");
    img.src = cals_doc_info.docs[0].pages[selectedPageIndex]["safety_zone"];
}

function unitConvertFromPoints(value, unit) {
    let factor;
    switch(unit) {
        case "mm"  : factor = 2.83465; break;
        case "cm"  : factor = 28.3465; break;
        case "inch": factor = 72; break;
        default    : factor = 1; break;
    }
    return value / factor;
}

function getBoxSize(box) {
    return {
        width: box["right"] - box["left"],
        height: box["top"] - box["bottom"]
    }
}

function addPageInformationData(element, pagenumberElement, data) {
    const pages = data["doc"]["pages"];
    const selectedPageIndex = (cals_sec_info["pageinfo"]["on_pages"][0] - 1) || 0;
    const selectedPage = pages[selectedPageIndex];

    let pageSizesEqual = true;
    for(let i = 0; i < pages.length; i++) {
        let selectedCropbox = getBoxSize(selectedPage["cropBox"]);
        let pageCropbox = getBoxSize(pages[i]["cropBox"]);

        if(selectedCropbox.width !== pageCropbox.width || selectedCropbox.height !== pageCropbox.height) {
            pageSizesEqual = false;
            break;
        }
    }

    let boxTableRows = "";
    let pageUnit = cals_sec_info["pageinfo"]["unit"] || "pt";
    const keys = [
        {box: "mediaBox", displayName: "MediaBox", effective: "effectiveMediaBox"},
        {box: "cropBox", displayName: "CropBox", effective: "effectiveCropBox"},
        {box: "trimBox", displayName: "TrimBox", effective: "effectiveTrimBox", color: "00EF03"},
        {box: "bleedBox", displayName: "BleedBox", effective: "effectiveBleedBox", color: "2222FF"},
        {box: "artBox", displayName: "ArtBox", effective: "effectiveArtBox"}
    ];

    keys.forEach(function(key, index) {
        let box = selectedPage[key.box];
        let effectiveSize = "";

        if(selectedPage["userUnit"] !== 1) {
            let effectiveBox = getBoxSize(selectedPage[key.effective]);
            effectiveSize = `<td>${unitConvertFromPoints(effectiveBox.width, pageUnit).toFixed(2)} x ${unitConvertFromPoints(effectiveBox.height, pageUnit).toFixed(2)} ${pageUnit}</td>`;
        }

        let boxSize = getBoxSize(box);
        boxTableRows += `<tr>
            <td class="txt-bold" ${key.hasOwnProperty("color") ? `style="color: #${key.color};"` : ''}>${key.displayName}</td>
            <td>${unitConvertFromPoints(boxSize.width, pageUnit).toFixed(2)} x ${unitConvertFromPoints(boxSize.height, pageUnit).toFixed(2)} ${pageUnit}</td>
            ${effectiveSize}
        </tr>`;
    });

    let html = `<div class="txt-small">
        <span class="txt-col-red txt-bold" translation-key="SafetyZone"></span><br>
        <table class="page-information-table">
            <tr><th translation-key="SafetyDistanceInside"></th><td>${cals_sec_info["pageinfo"]["safetyzoneinside"]}</td></tr>
            <tr><th translation-key="SafetyDistanceOutside"></th><td>${cals_sec_info["pageinfo"]["safetyzoneoutside"]}</td></tr>
        </table>
        <span class="txt-col-red txt-bold" translation-key="PageGeometryBoxes"></span><br>
        <table class="page-information-table">
            <tr>
                <th></th>
                <th translation-key="DefinedSize"></th>
                ${selectedPage["userUnit"] !== 1 ? '<th translation-key="EffectiveSize"></th>' : ''}
            </tr>
            ${boxTableRows}
        </table>
        <table class="page-information-table">
            <tr><th translation-key="PageScalingFactor"></th><td>${selectedPage["userUnit"]}</td></tr>
            <tr><th translation-key="PageRotation"></th><td>${selectedPage["rotate"]}</td></tr>
        </table>
        <span translation-key="${pageSizesEqual ? "AllPagesSame" : "PagesVary"}"></span>
    </div>`;

    pagenumberElement.querySelector(".pagenum").innerHTML = (selectedPageIndex + 1);
    element.insertAdjacentHTML("beforeend", html);
}

function addPageInformation(json) {
    const pageInformationElement = document.querySelector(".page-page-information");
    pageInformationElement.style.display = "block";

    const pageInfoVisual = pageInformationElement.querySelector(".page-information-visual");
    const pageInfoData = pageInformationElement.querySelector(".page-information-data");
    const pageInfoPagenumber = pageInformationElement.querySelector(".page-information-pagenumber");

    addPageInformationVisual(pageInfoVisual, json);
    addPageInformationData(pageInfoData, pageInfoPagenumber, json);
}
