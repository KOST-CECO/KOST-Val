const editor = ace.edit("editor");
const StatusBar = ace.require("ace/ext/statusbar").StatusBar;
const lineWidgets = ace.require("ace/line_widgets").LineWidgets;
const langTools = ace.require('ace/ext/language_tools');


document.addEventListener('keydown', function(event) {
	const keycode = event.key;
	const control = event.ctrlKey || event.metaKey;

	if(keycode == 'Escape') {
		handleEscape();
	}
});

function removeErrorMarkers() {
	let markers = editor.session.getMarkers();
	for(let property in markers) {
		if(markers[property].clazz === "highlightError") {
			editor.session.removeMarker(markers[property].id);
		}
	}
}

function removeLineWidgets() {
	const widgets = editor.session.lineWidgets;
	for(const widget in widgets) {
		if(widgets[widget] !== undefined) {
			editor.session.widgetManager.removeLineWidget(widgets[widget]);
		}
	}
}

function loadExtensionsTheme() {
	const aceGutter = document.querySelector(".ace_gutter"),
		  computedAceGutter = window.getComputedStyle(aceGutter),
		  statusbar = document.querySelector("#status-bar");

	statusbar.style.backgroundColor = computedAceGutter.backgroundColor;
	statusbar.style.color = computedAceGutter.color;
}

const start = () => {
	editor.setOptions({
		enableBasicAutocompletion: false,
		enableSnippets: false,
		enableLiveAutocompletion: true,
		useSoftTabs: false,
		showInvisibles: true,
		fontSize: 13,
		mode: "ace/mode/javascript",
		theme: "ace/theme/monokai"
	});
	editor.setValue(text);
	let statusbar = new StatusBar(editor, document.getElementById("status-bar"));
	editor.session.setUndoManager(new ace.UndoManager())
	editor.gotoLine(0);
	editor.focus();

	editor.session.on('change', function(delta) {
		clearTimeout(inputTimer);
		inputTimer = setTimeout(function() {
			removeErrorMarkers();
			removeLineWidgets();
			textChanged();
		}, 500);
	});

	editor.renderer.on('themeLoaded', function() {
		this.loadExtensionsTheme();
	}.bind(this));

	editor.commands.addCommand({
		name: 'beautify',
		bindKey: {win: 'Ctrl-B',  mac: 'Command-B'},
		exec: function(editor) {
			editor.setValue(js_beautify(editor.getValue(), {
				indent_size: editor.session.getTabSize(),
				indent_with_tabs: !editor.session.$useSoftTabs,
				preserve_newlines: true,
				max_preserve_newlines: 3
			}));
		},
		readOnly: false
	});

	editor.commands.removeCommands(["showSettingsMenu", "toggleBlockComment"]);
	editor.commands.addCommand({
		name: 'increaseFontSize',
		bindKey: {win: 'Ctrl-=|Ctrl-+', mac: 'Command-=|Command-+'},
		exec: function(editor) {
			editor.setFontSize(editor.getFontSize() + 1);
		},
		readOnly: false
	});

	editor.commands.addCommand({
		name: 'decreaseFontSize',
		bindKey: {win: 'Ctrl+-|Ctrl-_', mac: 'Command+-|Command-_'},
		exec: function(editor) {
			editor.setFontSize(editor.getFontSize() - 1);
		},
		readOnly: false
	});

	editor.commands.addCommand({
		name: 'decreaseFontSize',
		bindKey: {win: 'Ctrl+-|Ctrl-_', mac: 'Command+-|Command-_'},
		exec: function(editor) {
			editor.setFontSize(editor.getFontSize() - 1);
		},
		readOnly: false
	});

	editor.commands.bindKey({
		win: 'ctrl-shift-7',
		mac: 'command-shift-7'
	}, "togglecomment");

	document.querySelector("#status-bar .status-theme").addEventListener("change", function(event) {
		editor.setTheme("ace/theme/" + this.value);
	});

	(function() {
		const tabsElement = document.querySelector("#status-bar .status-tabs");
		var tabPreviousValue = tabsElement.options[tabsElement.selectedIndex].value;
		tabsElement.addEventListener("change", function(event) {
			let selectedGroup = this.options[this.selectedIndex].parentElement.getAttribute("label");
			if(selectedGroup === "Tab Size") {
				tabPreviousValue = this.value;
				editor.session.setOptions({tabSize: this.value});
			}else if(selectedGroup === "Convert") {
				if(this.value === "1") {
					// var tabString = editor.session.getTabString();
				}else {

				}
				this.value = tabPreviousValue;
			}
		});
	})();

	const pdfToolboxVarsCompleter = {
		identifierRegexps: [/[a-zA-Z_0-9\.\[\]\-\u00A2-\uFFFF]/],
		getCompletions: function(editor, session, pos, prefix, callback) {
			callback(null, completerData.filter(function(entry) {
				return entry.name.startsWith(prefix);
			}).map(function(entry) {
				return {
					value: entry.name,
					description: entry.description,
					meta: "App"
				}
			}));
		},
		getDocTooltip: function(item) {
			if(item.meta === "App") {
				item.docHTML = "<b>" + item.value + "</b><hr></hr>" + item.description;
			}
		}
	};

	// langTools.addCompleter(pdfToolboxVarsCompleter);
}

document.addEventListener("DOMContentLoaded", function() {
	if(typeof window.sendExtensionMessage === "function") { //CEF
		loadText().then(function(response) {
			text = response;
			start();
		});
	}else {
		text = 'function myFunction() {\n\tconsole.log("Hello World!");\n}\n\nmyFunction();';
		start();
	}
});