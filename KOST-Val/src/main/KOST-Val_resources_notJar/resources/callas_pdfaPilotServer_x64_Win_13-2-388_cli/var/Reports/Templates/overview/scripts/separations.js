
function addSeparationImages(data, page) {
    if(data.length === 0) return html;

    let html = `<div class="separation-page-container">
        <div class="txt-bold txt-col-red header" translation-key="Page"> ${page}</div>
        ${data.map(function(entry) {
            return `<div class="separation-image-box">
                <div>
                    <span>${entry.name}</span>
                </div>
                <div class="separation-image-container">
                    <img src="${entry.img}"/>
                </div>
            </div>`;
        }).join("")}
    </div>`;

    return html;
}

function getPagesForSeparationInDocumentByType(type = "process") {
    if(type !== "process" && type !== "spot") { return []; }
    const pages = [];
    for(const page of cals_doc_info.docs[0].pages) {
        if(cals_sec_info["separations"]["on_pages"].includes(page.page) &&
            page.hasOwnProperty("separations") &&
            page["separations"].hasOwnProperty(type)
        ) {
            pages.push(page);
        }
    }

    return pages;
}

function addSeparationByType(element, type = "process") {
    if(type !== "process" && type !== "spot") return;
    const displayType = cals_sec_info.separations.type;
    const pages = getPagesForSeparationInDocumentByType(type);

    if(pages.length === 0) {
        if(type === "spot" && displayType === "SPOTIFPRESENT") {
            element.style.display = "none";
            return;
        }
        const translationKey = type === "spot" ? "NoSpotColors" : "NoProcessColors";
        const html = `<div class="separation-no-color"><img src="img/ok.pdf" /><span class="txt-col-red txt-medium" translation-key="${translationKey}"></span></div>`;
        element.querySelector(".separation-" + type).insertAdjacentHTML("beforeend", html);
        return;
    }

    let html = "";
    pages.forEach(function(page) {
        html += addSeparationImages(page["separations"][type], page.page);
    });

    element.querySelector(".separation-" + type).insertAdjacentHTML("beforeend", html);
}

function addSeparations() {
    const displayType = cals_sec_info.separations.type;
    if(displayType === "ALL" || displayType === "PROCESS" || displayType === "SPOTIFPRESENT") {
        const separationProcessElement = document.querySelector(".page-separation-process");
        separationProcessElement.style.display = "block";
        addSeparationByType(separationProcessElement, "process");
    }
    if(displayType === "ALL" || displayType === "SPOT" || displayType === "SPOTIFPRESENT") {
        const separationSpotElement = document.querySelector(".page-separation-spot");
        separationSpotElement.style.display = "block";
        addSeparationByType(separationSpotElement, "spot");
    }
}
