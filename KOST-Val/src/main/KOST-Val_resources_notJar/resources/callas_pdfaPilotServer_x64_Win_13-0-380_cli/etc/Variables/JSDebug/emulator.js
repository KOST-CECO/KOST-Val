global.File = require("./File");
global.XML = require("./XML");
global.app = JSON.parse(require("fs").readFileSync("./pdfToolbox/app.json", "utf8"));

if( typeof global.app.doc != "undefined") {
	global.app.doc.getPageBox = function(pageBox,pageNumber,precision) {
		if(typeof pageNumber == "undefined")
			pageNumber = 0;
		if(typeof pageBox == "undefined")
			pageBox = "Crop";
		let b = null;
		switch(pageBox) {
			case "Art": b = global.app.doc.pages[pageNumber].artBox; break;
			case "Bleed": b = global.app.doc.pages[pageNumber].bleedBox; break;
			case "Crop": b = global.app.doc.pages[pageNumber].cropBox; break;
			case "Trim": b = global.app.doc.pages[pageNumber].trimBox; break;
			case "Media": b = global.app.doc.pages[pageNumber].mediaBox; break;
			default: b = global.app.doc.pages[pageNumber].cropBox;
		}
		if(typeof precision == "undefined")
			return [ b.left, b.top, b.right, b.bottom ];
		else
			return [ b.left.toFixed(precision), b.top.toFixed(precision), b.right.toFixed(precision), b.bottom.toFixed(precision) ];
	}

	global.app.doc.getPageRotation = function(pageNumber) {
		if(typeof pageNumber == "undefined")
			pageNumber = 0;
		return global.app.doc.pages[pageNumber].rotate;
	}

	global.app.doc.hasPageBox = function(pageBox,pageNumber) {
		let ret = false;
		if(typeof pageBox == "undefined")
			pageBox = "Crop";
		if(typeof pageNumber == "undefined")
			pageNumber = 0;
		switch(pageBox) {
			case "Art": ret = global.app.doc.pages[pageNumber].artBox.exists; break;
			case "Bleed": ret = global.app.doc.pages[pageNumber].bleedBox.exists; break;
			case "Crop": ret = global.app.doc.pages[pageNumber].cropBox.exists; break;
			case "Trim": ret = global.app.doc.pages[pageNumber].trimBox.exists; break;
			case "Media": ret = global.app.doc.pages[pageNumber].mediaBox.exists; break;
			default: ret = global.app.doc.pages[pageNumber].cropBox.exists;
		}
		return ret;
	}

	for(let i = 0; i < global.app.doc.pages.length; i++) {
		global.app.doc.pages[i].getPageBox = function(pageBox,precision) {
			if(typeof pageBox == "undefined")
				pageBox = "Crop";
			let b = null;
			switch(pageBox) {
				case "Art": b = this.artBox; break;
				case "Bleed": b = this.bleedBox; break;
				case "Crop": b = this.cropBox; break;
				case "Trim": b = this.trimBox; break;
				case "Media": b = this.mediaBox; break;
				default: b = this.cropBox;
			}
			if(typeof precision == "undefined")
				return [ b.left, b.top, b.right, b.bottom ];
			else
				return [ b.left.toFixed(precision), b.top.toFixed(precision), b.right.toFixed(precision), b.bottom.toFixed(precision) ];
		}
	
		global.app.doc.pages[i].getPageRotation = function() {
			return this.rotate;
		}
	
		global.app.doc.pages[i].hasPageBox = function(pageBox) {
			let ret = false;
			if(typeof pageBox == "undefined")
				pageBox = "Crop";
			switch(pageBox) {
				case "Art": ret = this.artBox.exists; break;
				case "Bleed": ret = this.bleedBox.exists; break;
				case "Crop": ret = this.cropBox.exists; break;
				case "Trim": ret = this.trimBox.exists; break;
				case "Media": ret = this.mediaBox.exists; break;
				default: ret = this.cropBox.exists;
			}
			return ret;
		}
	}
}

global.app.requires = function(name,def,label,list) {
	if(typeof name == "undefined")
		return false;
	if(typeof def == "undefined")
		def = 0;
	if( typeof app.vars[name] == "undefined")
	{
		app.vars[name] = def;
		return true;
	}
	else
		return false;
}
