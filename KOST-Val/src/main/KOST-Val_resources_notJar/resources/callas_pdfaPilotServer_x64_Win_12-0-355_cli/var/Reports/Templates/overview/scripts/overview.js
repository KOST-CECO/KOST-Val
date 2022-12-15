function printLoop() {
	printPages();
}

function padDigits(number, digits) {
    return Array(Math.max(digits - String(number).length + 1, 0)).join(0) + number;
}

function getDate() {
	let d = new Date(Date.parse(cals_env_info.date));
	let s = padDigits(d.getDate(),2) + "." + padDigits(d.getMonth()+1,2) + "." + d.getFullYear() + " " + padDigits(d.getHours(),2) + ":" + padDigits(d.getMinutes(),2);
	return s;
}

function getHitCounts(type) {
	let count = 0;
	cals_res_info.steps.forEach(function(step, stepIndex) {
		if(type === "fixups" && step.type === "fixup") {
			step.fixups.forEach(function(fixup, fixupIndex) {
				count += fixup.succeeded;
			});
		}else if(step.type === "action") {
		}else if(type === "errors" || type === "warnings" || type === "infos") {
			count += parseInt(step[type]);
		}
	});
	return count;
}

function addOverviewIcons() {
	const element = document.querySelector(".overview-details-hits");

	let lastFixup = 0;
	let steps = cals_res_info.steps;
	for(let stepIndex = steps.length - 1; stepIndex >= 0; stepIndex--) {
		if(steps[stepIndex].type === "fixup" && steps[stepIndex].fixups.length > 0) {
			lastFixup = steps[stepIndex].fixups[steps[stepIndex].fixups.length - 1].succeeded;
			break;
		}
	}

	const iconsArray = [{
		"lastResult": lastFixup,
		"totalResult": getHitCounts("fixups"),
		"image": "img/wrench.pdf",
		"imageGrey": "img/wrench_g.pdf",
	}, {
		"lastResult": parseInt(cals_res_info.errors),
		"totalResult": getHitCounts("errors"),
		"image": "img/error.pdf",
		"imageGrey": "img/error_g.pdf"
	}, {
		"lastResult": parseInt(cals_res_info.warnings),
		"totalResult": getHitCounts("warnings"),
		"image": "img/warning.pdf",
		"imageGrey": "img/warning_g.pdf"
	}, {
		"lastResult": parseInt(cals_res_info.infos),
		"totalResult": getHitCounts("infos"),
		"image": "img/info.pdf",
		"imageGrey": "img/info_g.pdf"
	}];

	let thIcons = "";
	let lastResult = "";
	let totalResult = "";

	if(cals_res_info["profile_type"] === "metaprofile") {
		thIcons += "<th></th>";
		lastResult += '<td translation-key="LastResult"></td>';
		totalResult += '<td translation-key="TotalResults"></td>';
	}

	let lastResultEmpty = true;
	iconsArray.forEach(function(icon, index) {
		if(icon.lastResult === 0) {
			thIcons += '<th><img src="' + icon.imageGrey + '"/></th>';
			lastResult += '<td class="center">-</td>';
		}else {
			lastResultEmpty = false;
			thIcons += `<th><img src="${icon.image}"/></th>`;
			lastResult += `<td class="center">${icon.lastResult}</td>`;
		}
		totalResult += `<td class="center">${icon.totalResult === 0 ? "-" : icon.totalResult}</td>`;
	});

	let html = `<table class="overview-details-table">
		<tr>${thIcons}</tr>
		<tr class="txt-bold">${lastResult}</tr>
		${cals_res_info["profile_type"] === "metaprofile" ? `<tr>${totalResult}</tr>` : ''}
	</table>
	${lastResultEmpty ? '<img class="overview-result-ok" src="img/ok.pdf" />' : ''}`;

	element.insertAdjacentHTML("beforeend", html);
}

function getPlateNames() {
	return cals_doc_info.docs[0].plates.join(", ");
}

function getStandards() {
	return cals_doc_info.docs[0].standards.join(", ");
}

function addDetails() {
	cals_res_info.steps.forEach(function(step, index) {
		addStep(index);
	});
}

function addStep(stepIndex) {
	const detailsElement = document.querySelector(".details");

	let html = `<div class="processstep">
		<div class="step-header-container">
			<img src="img/${cals_res_info.steps[stepIndex].type}.pdf"/>
			<h2>${cals_res_info.steps[stepIndex].name}</h2>
		</div>
		${addStepDetails(stepIndex)}
	</div>`;

	detailsElement.insertAdjacentHTML("beforeend", html);
}

function addStepDetails(stepIndex) {
	let step = cals_res_info.steps[stepIndex];
	let html = "";
	let noProblemsFound = true;

	if(step.type === "profile" || step.type === "fixup") {
		let fixups = addFixups(stepIndex);
		if(fixups !== "") {
			noProblemsFound = false;
			html += `<div class="details-fixups">${fixups}</div>`;
		}
	}

	if(step.type === "profile" || step.type === "rule") {
		let hits =  addHits(stepIndex, "Errors", "img/error.pdf", "error");
			hits += addHits(stepIndex, "Warnings", "img/warning.pdf", "warning");
			hits += addHits(stepIndex, "Infos", "img/info.pdf", "info");
		if(hits !== "") {
			noProblemsFound = false;
			html += `<div class="details-hits">
				${hits}
			</div>`;
		}
	}

	const noProblemsTypeArry = ["profile", "rule", "fixup"]
	if(noProblemsFound && noProblemsTypeArry.includes(step.type)) {
		return '<div class="no-details-header-container"><img src="img/ok.pdf"/><h3 translation-key="NoProblemsFound"></h3></div>';
	}

	return html;
}

function addFixups(stepIndex) {
	let listEntries = "";
	let fixupCount = 0;

	cals_res_info.steps[stepIndex].fixups.forEach(function(fixup, index) {
		if(parseInt(fixup.succeeded) + parseInt(fixup.failed) > 0) {
			fixupCount++;
			let objectMessageKey = fixup.succeeded > 1 ? "FixupMatches" : "FixupMatch";
			let objectString = getTranslatedMessage(objectMessageKey, {"FixupCount": fixup.succeeded, "FixupName": ""})
			listEntries += `<li><span class="txt-bold">${fixup.fixup_name}</span> <span>${objectString}</span></li>`;
		}
	});

	if(fixupCount === 0) return "";

	let html = `<div class="hit-fix-header-container">
		<img src="img/fixup.pdf"/>
		<h3 translation-key="Fixups"></h3>
	</div>
	<ul class="txt-small">${listEntries}</ul>`;

	return html;
}

function addHits(stepIndex, translationKey, image, severity) {
	let listEntries = "";
	let hitCount = 0;

	cals_res_info.steps[stepIndex].hits.forEach(function(hit, index) {
		if(hit.matches > 0 && hit.severity === severity) {
			hitCount++;
			let hitMessageKey = "";
			if(hit.pages === 0) {
				hitMessageKey = hit.matches === 1 ? "MatchValue" : "MatchesValue"
			}else if(hit.pages === 1) {
				hitMessageKey = hit.matches === 1 ? "RuleOneMatchOnOnePage" : "RuleMoreMatchesOnOnePage"
			}else {
				hitMessageKey = hit.matches === 1 ? "RuleOneMatchOnMorePages" : "RuleMoreMatcheshOnMorePages"
			}

			let hitString = getTranslatedMessage(hitMessageKey, {"RULE_NAME": "", "MATCH_COUNT": hit.matches, "PAGE_COUNT": hit.pages});
			listEntries += `<li><span class="txt-bold">${hit.rule_name}</span> <span>${hitString}</span></li>`;
		}
	});

	if(hitCount === 0) return "";

	let html = `<div class="hit-fix-header-container">
		<img src="${image}"/>
		<h3 translation-key="${translationKey}"></h3>
	</div>
	<ul class="txt-small">${listEntries}</ul>`;

	return html;
}

function setFileSize() {
	const translationKeys = ["FileSizeByte", "FileSizeKB", "FileSizeMB", "FileSizeGB"]
	const size = cals_doc_info.docs[0].file_size;

	const i = size == 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
	document.getElementById("fileSizeKey").setAttribute("translation-key", translationKeys[i]);
	document.getElementById("fileSizeValue").innerText = (size / Math.pow(1024, i)).toFixed(2);
}

function swapToIMG() {
	const images = document.querySelectorAll("img");
	images.forEach(function(image, index) {
		let imageSrc = image.getAttribute("src");
		if(imageSrc !== "" && imageSrc !== null) {
			image.setAttribute("src", imageSrc.slice(0, -3) + "png");
		}
	});
}

document.addEventListener("DOMContentLoaded", function() {
	if(typeof cchip === "undefined") {
		swapToIMG();
	}

	if(typeof cals_sec_info !== 'undefined') {
		if(cals_res_info.hasOwnProperty("json_report")) {
			try {
				const xhttp = new XMLHttpRequest();
				xhttp.open("GET", cals_res_info["json_report"], false);
				xhttp.send();
				const json = JSON.parse(xhttp.responseText);

				cals_sec_info.hasOwnProperty("spotcolors") && addSpotcolors(json);
				cals_sec_info.hasOwnProperty("pageinfo") && addPageInformation(json);
				cals_sec_info.hasOwnProperty("inkcoverage") && addInkCoverage(json);
			}catch(err) {}
		}

		cals_sec_info.hasOwnProperty("inkamountheatmaps") && addInkAmountHeatmaps();
		cals_sec_info.hasOwnProperty("separations") && addSeperations();
	}

	translate();
});
