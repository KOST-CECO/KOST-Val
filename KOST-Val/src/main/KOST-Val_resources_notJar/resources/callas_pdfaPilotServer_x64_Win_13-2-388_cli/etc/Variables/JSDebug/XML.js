const xp = require("./xpath");
const dom = require("./dom-parser").DOMParser;

class XML {
	#xml;
	#select;
	constructor(xml) {
		this.#xml = new dom().parseFromString(xml);
	}

	registerNamespace(prefix, uri) {
		this.#select = xp.useNamespaces({[prefix]: uri});
	}

	xpath(expression, showTextContent) {
		const values = [];
		let selected = (this.#select === undefined ? xp.select(expression, this.#xml) : this.#select(expression, this.#xml));
		selected.forEach(function(element) {
			if(element.nodeValue) {
				values.push(element.nodeValue);
			}else {
				let value = "<" + element.nodeName + ">" + element.textContent + "</" + element.nodeName + ">";
				if(showTextContent) {
					value = element.textContent;
				}
				values.push(value);
			}
		});
		return values;
	}
}

module.exports = XML;