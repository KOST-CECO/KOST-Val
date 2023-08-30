
function addSeperationImages(data, page) {
    if(data.length === 0) return html;

    let html = `<div class="seperation-page-container">
        <div class="txt-bold txt-col-red header" translation-key="Page"> ${page}</div>
        ${data.map(function(entry) {
            return `<div class="seperation-image-box">
                <div>
                    <span>${entry.name}</span>
                </div>
                <div class="seperation-image-container">
                    <img src="${entry.img}"/>
                </div>
            </div>`;
        }).join("")}
    </div>`;

    return html;
}

function addSeperationProcess(element) {
    let html = "";

    cals_doc_info["docs"][0]["pages"].forEach(function(page, index) {
        if(!cals_sec_info["separations"]["on_pages"].includes(index + 1)) return;
        if(!page["separations"].hasOwnProperty("process")) return;

        html += addSeperationImages(page["separations"]["process"], index + 1);
    });

    element.querySelector(".seperation-process").insertAdjacentHTML("beforeend", html);
}


function addSeperationSpot(element) {
    let html = "";

    cals_doc_info["docs"][0]["pages"].forEach(function(page, index) {
        if(!cals_sec_info["separations"]["on_pages"].includes(index + 1)) return;
        if(!page["separations"].hasOwnProperty("spot")) return;

        html += addSeperationImages(page["separations"]["spot"], index + 1);
    });

    element.querySelector(".seperation-spot").insertAdjacentHTML("beforeend", html);
}

function addSeperations() {
    const seperationProcessElement = document.querySelector(".page-seperation-process");
    seperationProcessElement.style.display = "block";

    const seperationSpotElement = document.querySelector(".page-seperation-spot");
    seperationSpotElement.style.display = "block";

    addSeperationProcess(seperationProcessElement);
    addSeperationSpot(seperationSpotElement);
}