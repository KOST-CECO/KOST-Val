const asyncForEach = async(array, callback) => {
	for (let i = 0; i < array.length; i++) {
		await callback(array[i], i, array);
	}
}

async function isRegexValid(exp) {
	let ret = false;
	await validateRegex(exp).then(function(response) {
		if(typeof response === "boolean") {
			ret = response;
		}
	});
	return ret;
}

function isJSONValid(json) {
	try {
		JSON.parse(json)
	}catch(err) {
		return false;
	}
	return true;
}

function removeException(element) {
	let exception = element.closest(".exception");
	exception.parentNode.removeChild(exception);
}

function createErrorBox(err) {
	let target = document.getElementById("exception-box");
	let rendered = "";
	let view = {
		"ex": err
	}
	rendered = Mustache.render(templateException, view);
	target.insertAdjacentHTML('afterbegin', rendered);
}

function loadInspect() {
	let container = document.querySelector('#inspect-scroll-container');
	let divider = document.createElement("div");
	divider.className = "container-divider";
	container.parentNode.insertBefore(divider, container);
	container.style.display = 'block';

	let anim = container.animate([{
		opacity: 0
	}, {
		opacity: 1
	}], {
		duration: 1300,
		delay: 155,
		easing: "ease"
	});
	
	anim.onfinish = function() {
		container.style.opacity = '1';
	}
}

const tree = new InfiniteTree({
	el: document.querySelector('#usage-tree'),
	autoOpen: true,
	rowRenderer: renderer,
	nodeIdAttr: 'data-id'
});

function toggleModal() {
	let modal = document.querySelector(".usage-modal-container");
	modal.classList.toggle("show-modal");
}

function loadUsageModal(key) {
	loadUsage(key).then(function(response) {
		response = {
			"id": "root",
			"name": key,
			"props": {
				"icon": "variable"
			},
			"children": [formatUsageJson(response["usage"])]
		};
		tree.loadData(response);
		toggleModal();
	});	
}

var treeId = 0;
const formatUsageJson = (obj, ret = {}) => {
	if(obj instanceof Object) {
		for (key in obj) {
			if(key == "params") {

				if(!ret.hasOwnProperty("children")) {
					ret["children"] = [];
				}
				for(o of obj[key]) {
					ret["children"].push({
						"id": `id ${++treeId}`,
						"name": o["label"],
						"props": {
							"icon": o["type"]
						}
					});
				}

			}else if(key == "childs") {
				if(!ret.hasOwnProperty("children")) {
					ret["children"] = [];
				}
				for(o of obj[key]) {
					ret["children"].push(formatUsageJson(o));
				}
			}else if(key == "name") {
				ret["id"] = `id ${++treeId}`;
				ret["name"] = obj["name"];
			}else if(key == "type") {
				ret["props"] = { "icon": obj["type"] };
			}
		}
	}
	return ret;
}

function loadInspectTableData(data) {
	let rows = '';
	if(data !== undefined) {
		data.forEach(function(value, index, array) {
			let error = "";
			if(value.type === "exception") {
				error = " error";
				if(inspectActive === false) {
					let inspectWrapper = document.querySelector('#inspect-scroll-container .inspect-wrapper');
					inspectWrapper.setAttribute('data-in-animation', 'true');
					toggleUsage(inspectWrapper, 220);
				}
			}
			let row = `<div class="trow${error}">`;
	
			row += `<div class="tcell" data-title="Label">${value.key}</div>`;
			row += `<div class="tcell" data-title="Value">${value.value}</div>`;
			row += `<div class="tcell" data-title="Type">${value.type}</div>`;
			row += `<div class="tcell" data-title="Usage"><button class="usage-button" onclick="loadUsageModal('${value.key}');" ${value.is_used == true ? "" : "disabled"}>i</button></div>`;
	
			rows += row + '</div>';
		});
	}
	document.querySelector('#inspect-scroll-container .inspect-table .inspect-table-body').innerHTML = rows;
}

function setInspectTableTimer() {
	clearTimeout(inspectTimer);
	inspectTimer = setTimeout(function() {
		loadProvided().then(function(response) {
			loadInspectTableData(response.provided);
		});
	}, 250);
}

function slideOutDetailsTable(target, duration = 500) {
	let targetStyle = window.getComputedStyle(target);

	let anim = target.animate([{
		height: targetStyle.height
	}, {
		height: '0px'
	}], duration);

	anim.onfinish = function() {
		target.style.display = 'none';
		let event = document.createEvent("Event");
		event.initEvent("animationend");
		target.dispatchEvent(event)
	}
}

function slideInDetailsTable(target, duration = 500) {
	let targetStyle = window.getComputedStyle(target);

	target.style.display = 'block';
	let anim = target.animate([{
		height: '0px'
	}, {
		height: targetStyle.height
	}], duration);

	anim.onfinish = function() {
		let event = document.createEvent("Event");
		event.initEvent("animationend");
		target.dispatchEvent(event)
	}
}

function toggleUsage(target, duration = 500) {
	if(window.getComputedStyle(target).display === 'none') {
		slideInDetailsTable(target, duration);
		slideInUsageButtons(duration);
	}else {
		slideOutDetailsTable(target, duration);
		slideOutUsageButtons(duration);
	}
}

function slideInUsageButtons(duration = 500) {
	const els = document.querySelectorAll('.usage-input-container .usage-button-anim-container');

	for (let el of els) {
		const targetStyle = window.getComputedStyle(el);
		el.style.display = 'block';
		let anim = el.animate([{
			opacity: '0',
			width: '0px'
		}, {
			opacity: '1',
			width: targetStyle.width
		}], duration);
	}
}

function slideOutUsageButtons(duration = 500) {
	const els = document.querySelectorAll('.usage-input-container .usage-button-anim-container');

	for (let el of els) {
		const targetStyle = window.getComputedStyle(el);

		let anim = el.animate([{
			opacity: '1',
			width: targetStyle.width
		}, {
			opacity: '0',
			width: '0px'
		}], duration);

		anim.onfinish = function() {
			el.style.display = 'none';
		}
	}
}

function numberBetweenRanges(num, ranges) {
	let i = ranges.length;
	while(i > 0) {
		if(num >= ranges[0] && num <= ranges[1]) {
			return true;
		}
		ranges.splice(0, 2);
		i -= 2;
	}
	return false;
}

function sortInputJson() {
	return function(x, y) {
		if(x.key < y.key) {
			return -1;
		}else if(x.key > y.key) {
			return 1;
		}
		return 0; //default return value (no sorting)
	}
}

function swapLabel() {
	let el = document.querySelectorAll('.input-label');
	for(let i = 0; i < el.length; i++) {
		const newText = el[i].getAttribute("data-label-swap-against");
		const currentText = el[i].textContent;
		el[i].textContent = newText;
		el[i].setAttribute("data-label-swap-against", currentText);
	}
}

function setTranslatedStrings() {
	let el = document.querySelectorAll('[data-translate]');
	for(let i = 0; i < el.length; i++) {
		el[i].textContent = kvm.translations[el[i].dataset.translate];
	}
}

function getTranslatedString(key) {
	if(kvm.translations.hasOwnProperty(key)) {
		return kvm.translations[key];
	}
	return `key: ${key} does not exist`;
}

function setTooltips() {
	let el = document.querySelectorAll('[data-tooltip]');
	let follow = false;
	if(el.length === 1) {
		follow = "horizontal";
	}
	for(let i = 0; i < el.length; i++) {
		if(el[i].dataset.tooltip != "") {
			tippy(el[i], {
				content: el[i].dataset.tooltip,
				arrow: true,
				distance: 8.5,
				animation: "scale",
				inertia: true,
				trigger: "mouseenter",
				followCursor: follow
			});
		}
	}	
}

function addErrorClassToElement(element, input) {
	if(!element.classList.contains("error")) {
		element.classList.add("error");
		addToErrorObject(input.dataset.id, input.dataset.type, input.dataset.index);
	}
}

function removeErrorClassFromElement(element, id) {
	if(element.classList.contains("error")) {
		element.classList.remove("error");
		removeFromErrorObject(id);
	}
}

function addToErrorObject(key, type, index) {
	inputErrors.push({
		"key": key,
		"type": type,
		"index": index
	});
	handleErrorBar();
	inputErrors.sort(function(a, b){return a.index-b.index});
}

function removeFromErrorObject(key) {
	inputErrors.splice(inputErrors.findIndex(function(input) {
		return input["key"] == key;
	}), 1);
	handleErrorBar();
}

// let errorBarAnimation = new Animation();

function handleErrorBar() {
	let error = document.querySelector(".error-bar");
	let errorContent = error.querySelector(".error-content");
	if(inputErrors.length > 0) {
		if(window.getComputedStyle(errorContent).display === "none") {

			errorContent.querySelector(".error-header").innerHTML = getTranslatedString("value_error_label");
			errorContent.querySelector(".error-counter .count").innerHTML = inputErrors.length;
			errorContent.style.display = "flex";

			let anim = errorContent.animate([{
				transform: "scale(0)",
				height: 0,
				opacity: 0
			}, {
				transform: "scale(1)",
				height: window.getComputedStyle(error).height,
				opacity: 1
			}], {
				duration: 450,
				easing: "ease-in-out"
			});

		}else {
			errorContent.querySelector(".error-counter .count").innerHTML = inputErrors.length;
		}
	}else {
		let anim = errorContent.animate([{
			transform: "scale(1)",
			height: window.getComputedStyle(error).height,
			opacity: 1
		}, {
			transform: "scale(0)",
			height: 0,
			opacity: 0
		}], {
			duration: 450,
			easing: "ease-in-out"
		});

		anim.onfinish = function() {
			errorContent.style.display = "none";
		}
	}
}

function scrollThroughErrors(direction) {
	if(inputErrors.length == 0) {
		return undefined;
	}

	if(selectedErrorKey === undefined) {
		selectedErrorKey = 0;
	}else if(direction === "down") {
		if(inputErrors.length === selectedErrorKey + 1) {
			selectedErrorKey = 0;
		}else {
			if(inputErrors.length > 1) {
				selectedErrorKey++;
			}
		}
	}else if(direction === "up") {
		if(selectedErrorKey === 0) {
			if(inputErrors.length > 1) {
				selectedErrorKey = inputErrors.length - 1;
			}
		}else {
			selectedErrorKey--;
		}
	}

	document.querySelector(".error-content .error-counter .current").innerHTML = selectedErrorKey + 1;

	let els;
	switch(inputErrors[selectedErrorKey].type) {
		case "array":
			els = document.querySelectorAll(".array.error input");
			break;
		case "popup":
			els = document.querySelectorAll(".popup.error select");
			break;
		case "regex":
			els = document.querySelectorAll(".regex.error input");
			break;
		case "number":
			els = document.querySelectorAll(".number.error input");
			break;
		case "object":
			els = document.querySelectorAll(".object.error input");
			break;
		default:
			return undefined;
	}

	for(let i = 0; i < els.length; i++) {
		if(els[i].dataset.id == inputErrors[selectedErrorKey].key) {
			els[i].scrollIntoView({block: "start", behavior: "smooth"});
			els[i].focus();
			if(typeof els[i].select === "function") {
				els[i].select();
			}
			break;
		}
	}
}

function selectFirstInputField(type) {
	let el;
	if(type == "boolean") {
		el = document.querySelector(".boolean");
	}else if(type == "array") {
		el = document.querySelector(".array input")
	}else if(type == "popup") {
		el = document.querySelector(".popup select");
	}else if(type == "regex") {
		el = document.querySelector(".regex input");
	}else if(type == "string") {
		el = document.querySelector(".string input");
	}else if(type == "number" || type == "float" || type == "integer") {
		el = document.querySelector(".number input");
	}else if(type == "file" || type == "folder") {
		el = document.querySelector(".path input");
	}else if(type == "popup-browse") {
		el = document.querySelector(".popup-browse select");
	}else if(type == "object") {
		el = document.querySelector(".object input");
	}
	if(el != undefined) {
		let firstRow = document.querySelector('#target .row');
		firstRow.addEventListener('animationend', function(event) {
			el.focus();
			if(typeof el.select === "function") {
				el.select();
			}
		});
	}
}