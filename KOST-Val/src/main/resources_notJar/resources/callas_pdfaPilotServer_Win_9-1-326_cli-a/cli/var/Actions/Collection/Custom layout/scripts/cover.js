//-------------------------------------------------------------------------------------------------
// Script content for the cover page for file packages
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2015 - callas software gmbh
// Author: Ulrich Frotscher
//-------------------------------------------------------------------------------------------------

// Translatable text (en, fr, de, es, it, jp)
var cLanguageCodes 		= [ "en", "fr", "de", "es", "it", "jp" ];
var cT_Title 			= [ "Embedded Files Summary", 
							"Récapitulatif des fichiers incorporés", 
							"Zusammenfassung der eingebetteten Dateien", 
							"Resumen de archivos incrustados", 
							"Riepilogo file incorporati", 
							"ファイル要約の埋め込み" ];
var cT_Subtitle 		= [ "Embedded Files Summary Report created by", 
							"Rapport récapitulatif des fichiers incorporés créé par", 
							"Zusammenfassungsbericht der eingebetteten Dateien erzeugt von", 
							"Informe del resumen de archivos incrustados creado por", 
							"Report di riepilogo dei file incorporati creato da", 
							"ファイル要約レポートの埋め込み 作成者" ];
var cT_On 				= [ "on", "le", "am", "el", "il", "日付" ];
var cT_At 				= [ "at", "à", "um", "en", "alle", "時刻" ];
var cT_EmbeddedFiles 	= [ "embedded files", 
							"fichiers incorporés", 
							"eingebettete Dateien", 
							"archivos incrustados", 
							"file incorporati", 
							"埋め込みファイル" ];


function T( inTranslations ) {

	// Get the index for the language we get (with a default for English)
	var theLanguageIndex = (cLanguageCodes.indexOf(cals_env_info.language) == -1) ? 0 : cLanguageCodes.indexOf(cals_env_info.language);

	// And return the correct translation
	return inTranslations[theLanguageIndex];
}

function makeURL( url ){
	return encodeURIComponent(url);
}
function basename(path, suffix) {
  var b = path;
  var lastChar = b.charAt(b.length - 1);

  if (lastChar === '/' || lastChar === '\\') {
    b = b.slice(0, -1);
  }

  b = b.replace(/^.*[\/\\]/g, '');

  if (typeof suffix === 'string' && b.substr(b.length - suffix.length) == suffix) {
    b = b.substr(0, b.length - suffix.length);
  }

  return b;
}
function dirname(path) {
  return path.replace(/\\/g, '/')
    .replace(/\/[^\/]*\/?$/, '');
}

function humanFileSize(bytes, si) {
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    var units = si ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
        		   : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1)+' '+units[u];
}

function getClassName( theExtension ) {
	theExtension = theExtension.toLowerCase();
	switch (theExtension) {
		case "pdf": 	
			return "type-pdf";

		case "doc":
		case "docx":
		case "dot":
		case "dotx":
			return "type-word";

		case "jpg":
		case "jpeg":
			return "type-jpg";

		case "htm":
		case "html":
			return "type-html";

		case "css":
			return "type-css";

		case "ods":
		case "ots":
			return "type-calc";

		case "msg":
		case "pst":
			return "type-email";

		case "eml":
		case "emlx":
			return "type-eml";

		case "xls":
		case "xlsx":
			return "type-excel";

		case "js":
			return "type-js";

		case "odp":
		case "odg":
		case "mdb":
		case "accdb":
		case "pub":
			return "type-office";

		case "png":
			return "type-png";

		case "ppt":
		case "pptx":
			return "type-powerpoint";

		case "txt":
		case "text":
			return "type-txt";

		case "odt":
		case "ott":
			return "type-writer";

		case "xml":
			return "type-xml";

	}
	return "file";
}

function pad2( inString ) {

	var thePaddedString = "0" + inString;
	return (thePaddedString.length == 2) ? thePaddedString : thePaddedString.substring(1);
}

function formatDate( inDateString ) {

	var theDate = new Date(inDateString);
	return pad2(theDate.getDate()) + "/" + pad2(theDate.getMonth()+1) + "/" + theDate.getFullYear();
}

function formatTime( inDateString ) {

	var theDate = new Date(inDateString);
	return pad2(theDate.getHours()) + ":" + pad2(theDate.getMinutes());
}

//-------------------------------------------------------------------------------------------------
// Embedd file as file attachemnt annotation
//-------------------------------------------------------------------------------------------------
function addAttachment( parent, url, item )
{
	var url_ = makeURL(url);
	var link = document.createElement("a");
	link.innerHTML = item.name;
	// link.setAttribute("href", cals_makeURL(url,true) );
	link.setAttribute("href", url_ );
	link.setAttribute("data-cchip-embed", url_ );
	if(item.hasOwnProperty('mimetype')){
		link.setAttribute("data-cchip-mimetype", item.mimetype );
	}
	if(item.hasOwnProperty('relationship')){
		link.setAttribute("data-cchip-relationship", item.relationship );
	}
	if(item.hasOwnProperty('bookmark')){
		 link.setAttribute("data-cchip-bookmark", basename(item.bookmark) );
		 link.setAttribute("data-cchip-bm-path", dirname(item.bookmark) );
	}
	if(item.hasOwnProperty('pdfa')){
		 link.setAttribute("cchip-pdfa", item.pdfa );
	}
	parent.appendChild( link );
}

//-------------------------------------------------------------------------------------------------
// Add folders
//-------------------------------------------------------------------------------------------------
function addItem( parent, item, url, className ) {
	// console.log("addFolder()");

	// Create the new bullet
	var li = document.createElement("li");

	// Assign the default classname to the object (which would be either "file" or "folder")
	li.className=className;

	// Treat files differently
	if(className=="file") {

		// Add the information of our attachment
		addAttachment(li,url+item.name,item);

		// See if we can set a custom filetype
		var re = /(?:\.([^.]+))?$/;
		var theFileExtension = re.exec(item.name)[1];
		if (typeof theFileExtension != 'undefined') {
			li.className = getClassName( theFileExtension );
		}

		// Add the size
		var theInfoSize = document.createTextNode( " (" + humanFileSize( item.size, true ) );

		// Add the date
		var theDate = new Date(item.modified);
		var theInfoDate = document.createTextNode( ", " + formatDate(item.modified) + " " +
											       formatTime(item.modified) + ")" );
		var theInfoSpan = document.createElement( "span" );
		theInfoSpan.appendChild( theInfoSize );
		theInfoSpan.appendChild( theInfoDate );
		theInfoSpan.className = "details-file-info";
		li.appendChild( theInfoSpan );

	}
	else
		li.appendChild(document.createTextNode( item.name));

	// Add the bullet
	parent.appendChild(li);

	if(item.hasOwnProperty('files')){
		addItems(li,item.files,url+item.name+"/","file");
	}
	if(item.hasOwnProperty('folders')){
		addItems(li,item.folders,url+item.name+"/","folder");
	}
}

function addItems( parent, items, url, className ) {
	if(items.length>0)
	{	
		var ul =  document.createElement("ul");
		parent.appendChild(ul);
		for (var i=0;i<items.length;i++)
		{ 
			addItem(ul,items[i],url,className);
		}
	}
}

function getFilesNumber(obj) {
  var count = 0;
  for (var k in obj) {
    if (k == 'files') count += obj.files.length;
	if (obj[k] !== null && typeof(obj[k])=="object") {
		count += getFilesNumber(obj[k]);
	}    
  }
  return count;
}

function addSubtitle( item ) {
	item.innerHTML = cals_env_info.tool_name + " - " + T( cT_Subtitle ) + " " + cals_env_info.user_name +
					 " " + T( cT_On ) + " " + formatDate(cals_env_info.date) + 
					 " " + T( cT_At ) + " " + formatTime(cals_env_info.date) + 
					 " - " + getFilesNumber(cals_package_info) + " " + T( cT_EmbeddedFiles );
}


//-------------------------------------------------------------------------------------------------
// Init
//-------------------------------------------------------------------------------------------------

function init() {
	var url = "callas_tmp/files/";
	document.getElementById('details-title').innerHTML = T( cT_Title );
	addSubtitle( document.getElementById('details-subtitle') );
	addItems( document.getElementById('details-embedded-files'), cals_package_info.files  , url, "file"  );
	addItems( document.getElementById('details-embedded-files'), cals_package_info.folders, url, "folder");
}
