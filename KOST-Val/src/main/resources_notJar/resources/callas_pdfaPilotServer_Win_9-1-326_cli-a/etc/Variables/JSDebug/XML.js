let xp = require("./xpath");
let select;

function XML(xml) {
	let dom = require("./dom-parser").DOMParser;
	this.xml = new dom().parseFromString(xml);
}

XML.prototype.registerNamespace = function(prefix,uri) {
	select = xp.useNamespaces({[prefix]: uri});
}

XML.prototype.xpath = function(expression) {
	return select(expression,this.xml);
}

module.exports = XML;