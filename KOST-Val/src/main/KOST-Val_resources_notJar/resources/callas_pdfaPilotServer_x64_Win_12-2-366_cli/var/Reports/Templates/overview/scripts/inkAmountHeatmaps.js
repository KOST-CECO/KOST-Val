
function addInkAmountHeatmaps() {
    const inkAmountElement = document.querySelector(".page-ink-amount-heatmaps");
    inkAmountElement.style.display = "block";

    const threshold = inkAmountElement.querySelector(".ink-amount-threshold span:last-child");
    threshold.textContent = " " + cals_sec_info["inkamountheatmaps"]["threshold"];

    const visiblePages = cals_sec_info["inkamountheatmaps"]["on_pages"];
    const pages = cals_doc_info["docs"][0]["pages"];

    let html = "";
    pages.forEach(function(page, index) {
        if(visiblePages.includes(index + 1)) {
            html += `<div class="ink-amount-image-box">
                <div>
                    <span class="txt-col-red txt-bold" translation-key="Page"> ${page["page"]}</span>
                </div>
                <div class="ink-amount-image-container">
                    <img src="${page["ink_amount"]}" />
                </div>
            </div>`;
        }
    });

    inkAmountElement.querySelector(".ink-amount-images").insertAdjacentHTML("beforeend", html);
}
