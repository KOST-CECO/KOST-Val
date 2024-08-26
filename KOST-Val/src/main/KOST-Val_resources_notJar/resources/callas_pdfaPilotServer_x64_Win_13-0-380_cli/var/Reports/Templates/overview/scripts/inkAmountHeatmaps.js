
function getPagesForInkAmountHeatmaps() {
    const pages = [];
    const visiblePages = cals_sec_info["inkamountheatmaps"]["on_pages"];
    for(const page of cals_doc_info.docs[0].pages) {
        if(visiblePages.includes(page.page) && page.hasOwnProperty("ink_amount")) {
            pages.push(page);
        }
    }
    return pages;
}

function addInkAmountHeatmaps() {
    const inkAmountElement = document.querySelector(".page-ink-amount-heatmaps");
    inkAmountElement.style.display = "block";

    const threshold = inkAmountElement.querySelector(".ink-amount-threshold span:last-child");
    threshold.textContent = " " + cals_sec_info["inkamountheatmaps"]["threshold"];

    const pages = getPagesForInkAmountHeatmaps();

    if(pages.length === 0) {
        const html = `<div class="ink-amount-heatmap-no-ink"><img src="img/ok.pdf" /><span class="txt-col-red txt-medium" translation-key="NoInkAmount"></span></div>`;
        inkAmountElement.insertAdjacentHTML("beforeend", html);
        return;
    }

    let html = "";
    pages.forEach(function(page) {
        html += `<div class="ink-amount-image-box">
            <div>
                <span class="txt-col-red txt-bold" translation-key="Page"> ${page.page}</span>
            </div>
            <div class="ink-amount-image-container">
                <img src="${page["ink_amount"]}" />
            </div>
        </div>`;
    });

    inkAmountElement.querySelector(".ink-amount-images").insertAdjacentHTML("beforeend", html);
}
