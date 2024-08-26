const imageResolutionTypeMap = {
    "color": {
        "path": "img_res",
        "string": "NoImageResolutionColorGrayscale"
    },
    "bitmap": {
        "path": "bmp_res",
        "string": "NoImageResolutionBitmap"
    }
};

function getPagesForImageResolutionByType(type = "color") {
    if(!imageResolutionTypeMap.hasOwnProperty(type)) { return [] };
    const pages = [];
    for(const page of cals_doc_info.docs[0].pages) {
        if(cals_sec_info["imageresolution"]["on_pages"].includes(page.page) &&
            page.hasOwnProperty(imageResolutionTypeMap[type].path)
        ) {
            pages.push(page);
        }
    }
    return pages;
}

function addImageResolutionByType(element, type = "color") {
    const pages = getPagesForImageResolutionByType(type);

    if(pages.length === 0) {
        const html = `<div class="image-resolution-no-image"><img src="img/ok.pdf" /><span class="txt-col-red txt-medium" translation-key="${imageResolutionTypeMap[type].string}"></span></div>`;
        element.insertAdjacentHTML("beforeend", html);
        return;
    }

    let html = "";
    pages.forEach(function(page) {
        html += `<div class="image-resolution-image-box">
            <div>
                <span class="txt-col-red txt-bold" translation-key="Page"> ${page.page}</span>
            </div>
            <div class="image-resolution-image-container">
                <img src="${page[imageResolutionTypeMap[type].path]}" />
            </div>
        </div>`;
    });
    element.insertAdjacentHTML("beforeend", html);
}

function addImageResolution() {
    const imageResolutionElement = document.querySelector(".page-image-resolution");
    imageResolutionElement.style.display = "block";

    const displayType = cals_sec_info.imageresolution.type;
    if(displayType === "ALL" || displayType === "IMG") {
        const thresholdColor = imageResolutionElement.querySelector(".image-resolution-threshold-color");
        thresholdColor.style.display = "block";
        thresholdColor.querySelector("span:last-child").textContent = cals_sec_info["imageresolution"]["imgthreshold"];

        const imageResolutionColorElement = imageResolutionElement.querySelector(".image-resolution-color");
        imageResolutionColorElement.style.display = "block";
        addImageResolutionByType(imageResolutionColorElement, "color");
    }

    if(displayType === "ALL" || displayType === "BMP") {
        const thresholdBitmap = imageResolutionElement.querySelector(".image-resolution-threshold-bitmap");
        thresholdBitmap.style.display = "block";
        thresholdBitmap.querySelector("span:last-child").textContent = cals_sec_info["imageresolution"]["bmpthreshold"];

        const imageResolutionBitmapElement = imageResolutionElement.querySelector(".image-resolution-bitmap");
        imageResolutionBitmapElement.style.display = "block";
        addImageResolutionByType(imageResolutionBitmapElement, "bitmap");
    }
}
