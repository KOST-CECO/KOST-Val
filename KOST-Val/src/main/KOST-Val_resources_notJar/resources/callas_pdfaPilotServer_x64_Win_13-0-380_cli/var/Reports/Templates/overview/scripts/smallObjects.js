const smallObjectsTypeMap = {
    "line": {
        "path": "smallobj_lines",
        "string": "NoSmallLineObjects"
    },
    "text": {
        "path": "smallobj_text",
        "string": "NoSmallTextObjects"
    }
};

function getPagesForSmallObjectsByType(type = "line") {
    if(!smallObjectsTypeMap.hasOwnProperty(type)) { return [] };
    const pages = [];
    for(const page of cals_doc_info.docs[0].pages) {
        if(cals_sec_info["smallobjects"]["on_pages"].includes(page.page) &&
            page.hasOwnProperty(smallObjectsTypeMap[type].path)
        ) {
            pages.push(page);
        }
    }
    return pages;
}

function addSmallObjectsByType(element, type = "line") {
    const pages = getPagesForSmallObjectsByType(type);

    if(pages.length === 0) {
        const html = `<div class="small-objects-no-line-text"><img src="img/ok.pdf" /><span class="txt-col-red txt-medium" translation-key="${smallObjectsTypeMap[type].string}"></span></div>`;
        element.insertAdjacentHTML("beforeend", html);
        return;
    }

    let html = "";
    pages.forEach(function(page) {
        html += `<div class="small-objects-image-box">
            <div>
                <span class="txt-col-red txt-bold" translation-key="Page"> ${page.page}</span>
            </div>
            <div class="small-objects-image-container">
                <img src="${page[smallObjectsTypeMap[type].path]}" />
            </div>
        </div>`;
    });
    element.insertAdjacentHTML("beforeend", html);
}

function addSmallObjects() {
    const smallObjectsElement = document.querySelector(".page-small-objects");
    smallObjectsElement.style.display = "block";

    const thresholdType = cals_sec_info["smallobjects"]["threshold"];
    const thresholdStringMap = {
        "LOW": {
            "string": "LowResolution",
            "singleLine": "0.2",
            "multiLine": "0.5",
            "singleText": "8",
            "multiText": "10"
        },
        "MEDIUM": {
            "string": "MediumResolution",
            "singleLine": "0.125",
            "multiLine": "0.25",
            "singleText": "5",
            "multiText": "9"
        },
        "HIGH": {
            "string": "HightResolution",
            "singleLine": "0.125",
            "multiLine": "0.25",
            "singleText": "5",
            "multiText": "8"
        }
    }

    const lineTextDataElement = smallObjectsElement.querySelector(".small-objects-line-text-data");
    let html = `<table class="small-objects-color-table">
		<tr>
            <th translation-key="SingleColor"></th>
            <td><img src="img/smallLinesSinglecolor.pdf"/> ${thresholdStringMap[thresholdType].singleLine} pt</td>
            <td><img src="img/smallTextSinglecolor.pdf"/> ${thresholdStringMap[thresholdType].singleText} pt</td>
        </tr>
		<tr>
            <th translation-key="Multicolor"></th>
            <td><img src="img/smallLinesMulticolor.pdf"/> ${thresholdStringMap[thresholdType].multiLine} pt</td>
            <td><img src="img/smallTextMulticolor.pdf"/> ${thresholdStringMap[thresholdType].multiText} pt</td>
        </tr>
	</table>`
    lineTextDataElement.insertAdjacentHTML("beforeend", html);

    const threshold = smallObjectsElement.querySelector(".small-objects-threshold span:last-child");
    threshold.setAttribute("translation-key", thresholdStringMap[thresholdType].string);

    const displayType = cals_sec_info.smallobjects.type;
    if(displayType === "ALL" || displayType === "LINES") {
        const smallLineObjects = smallObjectsElement.querySelector(".small-objects-line");
        smallLineObjects.style.display = "block";
        addSmallObjectsByType(smallLineObjects, "line");
    }

    if(displayType === "ALL" || displayType === "TEXT") {
        const smallTextObjects = smallObjectsElement.querySelector(".small-objects-text");
        smallTextObjects.style.display = "block";
        addSmallObjectsByType(smallTextObjects, "text");
    }
}
