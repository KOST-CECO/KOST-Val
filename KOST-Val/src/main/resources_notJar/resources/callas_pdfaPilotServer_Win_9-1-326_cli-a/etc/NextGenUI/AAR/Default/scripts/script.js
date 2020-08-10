
function getValues() {
	let ret = {
		"input": {},
		"errors": false
	};

	if(checkForErrors() === false) {
		ret.errors = true;
	}

	let booleans = document.getElementsByClassName("boolean");
	for(let i = 0; i < booleans.length; i++) {
		ret.input[booleans[i].dataset.id] = booleans[i].classList.contains("active");
	}

	let popups = document.getElementsByClassName("popup");
	for(let i = 0; i < popups.length; i++) {
		let popup = popups[i].querySelector("select");
		ret.input[popup.dataset.id] = popup.options[popup.selectedIndex].value;
	}

	let regex = document.getElementsByClassName("regex");
	for(let i = 0; i < regex.length; i++) {
		let input = regex[i].querySelector("input");
		ret.input[input.dataset.id] = input.value;
	}

	let strings = document.getElementsByClassName("string");
	for(let i = 0; i < strings.length; i++) {
		let input = strings[i].querySelector("input");
		ret.input[input.dataset.id] = input.value;
	}

	let numbers = document.getElementsByClassName("number");
	for(let i = 0; i < numbers.length; i++) {
		let input = numbers[i].querySelector("input");
		ret.input[input.dataset.id] = input.value;
	}

	let paths = document.getElementsByClassName("path");
	for(let i = 0; i < paths.length; i++) {
		let input = paths[i].querySelector('input');
		ret.input[input.dataset.id] = input.value;
	}

	let popup_browse = document.getElementsByClassName("popup-browse");
	for(let i = 0; i < popup_browse.length; i++) {
		let select = popup_browse[i].querySelector('select');
		ret.input[select.dataset.id] = select.options[select.selectedIndex].value;
	}

	let array = document.getElementsByClassName("array");
	for(let i = 0; i < array.length; i++) {
		let input = array[i].querySelector("input");
		ret.input[input.dataset.id] = input.value;
	}

	let object = document.getElementsByClassName("object");
	for(let i = 0; i < object.length; i++) {
		let input = object[i].querySelector("textarea");
		ret.input[input.dataset.id] = input.value;
	}
	
	return JSON.stringify(ret);
}

function checkForErrors() {
	let target = document.getElementById("target");
	let inspect = document.getElementById("inspect");
	let targetHasError = target.getElementsByClassName("error").length > 0;
	let inspectHasError = inspect.getElementsByClassName("error").length > 0;

	if(targetHasError || inspectHasError) {
		return false;
	}
	return true;
}

const start = async () => {
	setTranslatedStrings();
	kvm.input.sort(sortInputJson());

	Mustache.parse(templateBoolean);
	Mustache.parse(templatePopup);
	Mustache.parse(templatePopupBrowse);
	Mustache.parse(templateNumber);
	Mustache.parse(templateString);
	Mustache.parse(templateArray);
	Mustache.parse(templateObject);
	Mustache.parse(templateRegex);
	Mustache.parse(templatePath);

	let target = document.getElementById("target");
	await asyncForEach(kvm.input, async (obj, index) => {
		let rendered = "";

		let tooltip = "";
		if(obj.hasOwnProperty("tooltip")) {
			tooltip = obj.tooltip;
		}

		let usageButtonActive = "disabled";
		if(obj.hasOwnProperty("is_used")) {
			if(obj.is_used == true) {
				usageButtonActive = "";
			}
		}

		if(obj.type == "boolean") {
			let view = {
				"label": obj.label,
				"id": obj.key,
				"active": (obj.value == 1 ? "active" : ""),
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateBoolean, view);
		}else if(obj.type == "popup") {
			obj.values.forEach(function(val) {
				if(val.value == obj.value) {
					val.selected = "selected";
				}else {
					val.selected = "";
				}
			});
			let view = {
				"label": obj.label,
				"id": obj.key,
				"values": obj.values,
				"settings": JSON.stringify(obj),
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			if(obj.hasOwnProperty("browse")) {
				rendered = Mustache.render(templatePopupBrowse, view);
			}else {
				rendered = Mustache.render(templatePopup, view);
			}
		}else if(obj.type == "integer" || obj.type == "float" || obj.type == "number") {
			let rangesString = "";
			if(obj.hasOwnProperty("ranges")) {
				for(let i = 0; i < obj.ranges.length; i++) {
					rangesString += obj.ranges[i].first + " " + obj.ranges[i].last;
					if((i+1) != obj.ranges.length) {
						rangesString += " ";
					}
				}
			}
			let view = {
				"label": obj.label,
				"value": obj.value,
				"id": obj.key,
				"ranges": rangesString,
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateNumber, view);
		}else if(obj.type == "regex") {
			let view = {
				"label": obj.label,
				"id": obj.key,
				"value": obj.value,
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateRegex, view);
		}else if(obj.type == "string") {
			let view = {
				"label": obj.label,
				"id": obj.key,
				"value": obj.value,
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateString, view);
		}else if(obj.type == "file" || obj.type == "folder") {
			let view = {
				"label": obj.label,
				"id": obj.key,
				"value": obj.value,
				"settings": JSON.stringify(obj),
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templatePath, view);
		}else if(obj.type == "array") {
			let view = {
				"label": obj.label,
				"id": obj.key,
				"value": obj.value,
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateArray, view);
		}else if(obj.type == "object") {
			let val = obj.value;
			val = val.replace(/[\n\r\f\t\b\\]/g, '');
			val = val.replace(/[\"]/g, '"');
			let view = {
				"label": obj.label,
				"id": obj.key,
				"value": val,
				"tooltip": tooltip,
				"index": index,
				"usageButtonActive": usageButtonActive
			}
			rendered = Mustache.render(templateObject, view);
		}else {
			createErrorBox(`unhandled input type: ${JSON.stringify(obj)}`);
		}
		target.insertAdjacentHTML('beforeend', rendered);
	});

	setTooltips();

	setInputEvents();

	if(kvm.hasOwnProperty('provided') && kvm.provided.length > 0) {
		loadInspect();
		loadInspectTableData(kvm.provided);
		
	}

	selectFirstInputField(kvm.input[0].type);
}

document.addEventListener("DOMContentLoaded", function() {
	if(typeof window.sendExtensionMessage === "function") { //CEF

		backEventsHandlers["saveVariables"] = saveVariables;
		backEventsHandlers["importVariables"] = importVariables;
		registerEventCallback();

		loadJson().then(function(response) {
			kvm = response;
			start();
		});

	}else {
		kvm = cals_params;
		start();
	}

});
