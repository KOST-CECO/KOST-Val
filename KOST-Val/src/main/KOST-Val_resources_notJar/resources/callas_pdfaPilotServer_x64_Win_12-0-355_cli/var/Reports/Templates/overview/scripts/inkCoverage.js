function createInkCoverageTable(inks, head, index) {
    let html = '<table class="txt-small ink-coverage-table">';

    if(head !== "") {
        html += `<tr class="page-head">
            <th class="txt-col-red" translation-key="${head}"> ${index}</th>
            <td></td><td></td><td></td>
        </tr>`;
    }

    inks.forEach(function(ink, index) {
        html += `<tr>
            <th>${ink["name"]}</th>
            ${ink.hasOwnProperty("inkcov") ?
                `<td>${ink["inkcov"]["prct"].toFixed(2)} %</td>
                <td>${ink["inkcov"]["prct"].toFixed(2)} cm²</td>
                <td>${ink["inkcov"]["prct"].toFixed(2)} inch²</td>`
            :
                '<td>-</td><td>-</td><td>-</td>'
            }
        </tr>`;
    });

    html += "</table>";
    return html;
}

function addDocumentInkCoverage(element, data) {
    let html = createInkCoverageTable(data["doc"]["inks"], "", 0);
    element.insertAdjacentHTML("beforeend", html);
}

function addPagesInkCoverage(element, data) {
    let html = "";

    data["doc"]["pages"].forEach(function(page, index) {
        if(cals_sec_info["inkcoverage"]["on_pages"].includes(index + 1)) {
            html += createInkCoverageTable(page["inks"], "Page", index + 1);
        }
    });

    element.insertAdjacentHTML("beforeend", html);
}

function addInkCoverage(json) {
    const inkCoverageElement = document.querySelector(".page-ink-coverage");
    inkCoverageElement.style.display = "block";

    addDocumentInkCoverage(inkCoverageElement.querySelector(".ink-coverage-document"), json);
    addPagesInkCoverage(inkCoverageElement.querySelector(".ink-coverage-pages"), json);
}
