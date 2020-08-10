let backEventsHandlers = {};
let settings = {};
let inputTimer = undefined;
let text = undefined;

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
			}
		});
	});
}

function loadText() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadText'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.stringify(JSON.parse(data.value), null, '\t');
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) { 
				}
			}
		});
	});
}

function loadCompleterData() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadCompleterData'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.stringify(JSON.parse(data.value), null, '\t');
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
				}
			}
		});
	});
}

function loadSettings() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'loadSettings'
			},
			onSuccess: (data) => {
				try {
					let value;
					if(data != null && data.value != null && data.contentType != null) {
						switch (data.contentType) {
							case 'json':
								value = JSON.stringify(JSON.parse(data.value), null, '\t');
								break;
							case 'string':
								value = data.value;
								break;
						}
					}
					resolve(value);
				}catch(e) {
				}
			}
		});
	});
}

function setErrorMarker() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'setErrorMarker'
			},
			onSuccess: (data) => {
				if(data != null && data.value != null && data.contentType == 'json') {
					let d = JSON.parse(data.value);
					//also available:
					//d.column
					//d.message

					const session = editor.session;
					if(!session.widgetManager) {
						session.widgetManager = new lineWidgets(session);
						session.widgetManager.attach(editor);
					}
			
					const widget = {
						row: d.line,
						fixedWidth: true,
						coverGutter: true,
						el: document.createElement("div"),
						type: "errorMarker"
					};
					
					const el = widget.el.appendChild(document.createElement("div"));
					const arrow = widget.el.appendChild(document.createElement("div"));
					el.className = "error_widget";
					arrow.className = "error_widget_arrow";
					widget.el.className = "error_widget_wrapper";
			
					const pos = {
						"row": d.line,
						"column": d.column
					};
					const left = editor.renderer.$cursorLayer.getPixelPosition(pos).left;
					arrow.style.left = left + editor.renderer.gutterWidth - 5 + "px";
			
					el.innerHTML = d.message;
			
					session.widgetManager.addLineWidget(widget);
				}
			}
		});
	});
}

function handleEscape() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'cancel'
			}
		});
	});
}

function saveText() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method:'saveText',
				data: editor.getValue()
			}
		});
	});
}

function textChanged() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'textChanged'
			}
		});
	});
}

function insertText() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'insertText'
			},
			onSuccess: (data) => {
				editor.insert(data.value);
			}
		});
	});
}

function setText() {
	return new Promise((resolve, reject) => {
		window.sendExtensionMessage({
			request: {
				method: 'setText'
			},
			onSuccess: (data) => {
				editor.setValue(data.value);
			}
		});
	});
}
