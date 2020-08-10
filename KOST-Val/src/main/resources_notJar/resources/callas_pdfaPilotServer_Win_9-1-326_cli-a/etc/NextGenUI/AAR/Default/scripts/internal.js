/*
	All functions in this file are responsible for the communication
	between the PDFToolbox and the AskAtRuntime template.
	Changes to this file should only be made when absolutely necessary.
*/

let backEventsHandlers = {};
let kvm = {};

let inputErrors = [];
let selectedErrorKey = undefined;

let inspectActive = false;
let inspectTimer = undefined;
let inspectValues = "";

function registerEventCallback() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method:'registerEventCallback',
			},
			persistent: true,
			onSuccess: (data) => {
				let callback = backEventsHandlers[data.eventName];
				if(typeof callback == "function") {
					callback(data);
				}
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function loadJson() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadDefault'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.parse(data.value);
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function browse(data) {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'browse',
				data
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function validateRegex(data) {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'regex',
				data
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'bool':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function loadProvided() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadProvided'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.parse(data.value);
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
				createErrorBox(code);
			}
		});
	});
}

function loadUsage(key) {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadUsage',
				data: key
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.parse(data.value);
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
				resolve(message);
			}
		});
	});
}

function handleEnter() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'ok'
			},
			onSuccess: (data) => {
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function handleEscape() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'cancel'
			},
			onSuccess: (data) => {
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function saveVariables() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method:'saveVariables',
				data: getValues()
			},
			onSuccess: (data) => {
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function importVariables() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method:'importVariables'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.parse(data.value);
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					updateControls(value);
				}catch(e) {
					createErrorBox(e);
				}
			},
			onFailure: (code, message) => {
			}
		});
	});
}

function updateControls(response) {
	try {
		let allInputFields = document.querySelectorAll("[data-id]");

		let event = document.createEvent("Event");
		event.initEvent("input");

		for(let i = 0; i < allInputFields.length; i++) {

			let type = allInputFields[i].dataset.type;
			let id = allInputFields[i].dataset.id;
			if(response[id] == undefined) {
				continue;
			}
			let valueCase = ["regex", "string", "number", "path", "array", "object"];
			let selectCase = ["popup", "popup-browse"];
			let activeClassCase = ["boolean"];
			let triggerInputEvent = ["number", "regex", "array", "object"];

			if(valueCase.indexOf(type) !== -1) {
				allInputFields[i].value = response[id];
			}else if(selectCase.indexOf(type) !== -1) {
				for(oi = 0; oi < allInputFields[i].options.length; oi++) {
					if(allInputFields[i].options[oi].value == response[id]) {
						allInputFields[i].selectedIndex = allInputFields[i].options[oi].index
						break;
					}
				}
			}else if(activeClassCase.indexOf(type) !== -1) {
				if(response[id] == true) {
					if(!allInputFields[i].classList.contains("active")) {
						allInputFields[i].classList.add("active");
					}
				}else {
					if(allInputFields[i].classList.contains("active")) {
						allInputFields[i].classList.remove("active");
					}
				}
			}

			if(triggerInputEvent.indexOf(type) !== -1) {
				allInputFields[i].dispatchEvent(event);
			}
		}
	}catch(e) {
		createErrorBox(e);
	}
}
