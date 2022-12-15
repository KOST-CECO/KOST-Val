
function containsColorObject(object, list) {
    for(let i = 0; i < list.length; i++) {
        const listentry = JSON.parse(JSON.stringify(list[i]));
        const tempobject = JSON.parse(JSON.stringify(object));

        // we have to remove to pagenumbers entry to be able to compare the two
        delete listentry.pagenumbers;
        delete tempobject.pagenumbers;

        // debugger;
        if(isObjectEqual(listentry, tempobject)) {
            return i;
        }
    }
    return -1;
}

function prepareSpotcolorData(json) {
    const pages = json["doc"]["quickcheck"]["aggregated"]["pages"]["page"];
    let colors = [];

    for(let i = 0; i < pages.length; i++) {
        const pagenum = pages[i]["info"]["pagenum"];
        const spotcolors = pages[i]["resources"]["color"]["spotcolors"]["spotcolor"];

        if(!cals_sec_info["spotcolors"]["on_pages"].includes(i + 1)) continue;
        if(!spotcolors) continue;

        for(let x = 0; x < spotcolors.length; x++) {
            const color = {
                name: spotcolors[x].name,
                pagenumbers: [pagenum],
                colorspace: spotcolors[x].alternatespace,
                colorvalues: spotcolors[x].alternatevalues
            }

            const contains = containsColorObject(color, colors);
            if(contains === -1) {
                colors.push(color);
            }else {
                // check if pagenumber already exists
                // because the same color can exist multiple times on the same page
                if(!colors[contains].pagenumbers.includes(pagenum)) {
                    colors[contains].pagenumbers.push(pagenum);
                }
            }
        }
    }
    return colors;
}

function addSpotcolors(json) {
    const spotcolorsElement = document.querySelector(".page-spotcolors");
    spotcolorsElement.style.display = "block";

    const spotColorList = spotcolorsElement.querySelector(".spot-color-list");
    const spotcolordata = prepareSpotcolorData(json);

    let html = "";
    if(spotcolordata.length === 0) {
        html = '<div class="spot-color-no-spot"><img src="img/error.png" /><span class="txt-col-red" translation-key="NoSpotColors"></span></div>';
    }else {
        spotcolordata.forEach(function(spotcolor, i) {
            if(spotcolor.colorspace === "undefined") return;

            let color = "background: ";
            switch (spotcolor.colorspace) {
                case "ICCBased_CMYK" :
                case "DeviceCMYK"    :
                    color += "-cchip-cmyk"; break;
                case "ICCBased_Gray" :
                case "DeviceGray"    :
                    color += "-cchip-gray"; break;
                case "ICCBased_RGB"  :
                case "DeviceRGB"     :
                    color += "-cchip-rgb"; break;
                case "ICCBased_Lab"  :
                case "Lab"           :
                    color += "-cchip-lab"; break;
            }

            const spotcolorValueList = spotcolor.colorvalues.map(function(colorvalue, index) {
                return colorvalue.toFixed(2);
            });

            color += "(" + spotcolorValueList.join(", ") + ");"

            html += `<div class="spot-color-container">
                <div class="spot-color-flex">
                    <div class="color-round" style="${(spotcolorValueList.length > 0 ? color : "")}"></div>
                    <div class="spot-color-data txt-small">
                        <span translation-key="SpotName">: <span class="txt-bold">${spotcolor.name}</span></span>
                        <span translation-key="AlternateColorSpace">: <span class="txt-bold">${spotcolor.colorspace}</span></span>
                        <span translation-key="AlternateColorValue">: <span class="txt-bold">(${spotcolorValueList.join("/")})</span></span>
                        <span translation-key="UsedOnPage">: <span class="txt-bold">${spotcolor.pagenumbers.join(", ")}</span></span>
                    </div>
                </div>
            </div>`;
        });
    }

    spotColorList.insertAdjacentHTML("beforeend", html);
}