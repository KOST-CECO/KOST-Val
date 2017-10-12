//-------------------------------------------------------------------------------------------------
// Support functions for callas "Place Content" fixup
//
// Version: 1.3
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2017 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// LOGGING
//-------------------------------------------------------------------------------------------------

// Generic logging function that logs correctly whether or not pdfChip technology is used
//
function debugLog( inMessage ) {
	console.log( inMessage );
}



//-------------------------------------------------------------------------------------------------
// PAGES
//-------------------------------------------------------------------------------------------------

// Generic function to adjust the actual size of our document to the page size in the current page
//
function adjustDocumentSizeToMediabox( inPageNumber ) {

	// Get the page object
	var thePage = cals_doc_info.pages[inPageNumber];
	var theMediaboxWidth = thePage.mediabox[2]; 
	var theMediaboxHeight = thePage.mediabox[3]; 

	// Add a new style object to play with
	var cssPagedMedia = (function () {
    	var style = document.createElement('style');
    	document.head.appendChild(style);
    	return function (rule) {
        	style.innerHTML = rule;
    	};
	}());

	// We're actually going to add a margin and then use the pdfChip cropbox feature to get
	// the correct size
	var theMargin = 5;

	// Change the size
	cssPagedMedia('@page {size: ' + (theMediaboxWidth + theMargin) + 'pt ' + (theMediaboxHeight + theMargin) + 'pt; ' + 
				  '-cchip-cropbox: 0 ' + theMargin + 'pt ' + theMediaboxWidth + 'pt ' + theMediaboxHeight + 'pt;' +
				  '}');
}

// Generic function to adjust the actual size of our generated PDF document to the size of one
// or more elements in the HTML file. The objects in question should be at (0, 0) (left, top)
// ElementID: the CSS identifier for the object we want to move. Anything supported by jQuery is
//            supported here.
//
function adjustDocumentSizeToHtmlElement( inElementID ) {

	// Get the width and height of the element in points
	var theElementWidth = $(inElementID).width() * 0.75 + 12;
	var theElementHeight = $(inElementID).height() * 0.75 + 6;

	// Add a new style object to play with
	var cssPagedMedia = (function () {
    	var style = document.createElement('style');
    	document.head.appendChild(style);
    	return function (rule) {
        	style.innerHTML = rule;
    	};
	}());

	// Change the size
	cssPagedMedia('@page {size: ' + theElementWidth + 'pt ' + theElementHeight + 'pt;}');
}

// Generic function to resize the page that will be generated to a specific size in points
//
function adjustDocumentSizeToSizeInPoints( inWidth, inHeight ) {

	// Add a new style object to play with
	var cssPagedMedia = (function () {
    	var style = document.createElement('style');
    	document.head.appendChild(style);
    	return function (rule) {
        	style.innerHTML = rule;
    	};
	}());

	// Change the size
	cssPagedMedia('@page {size: ' + Math.ceil( inWidth ) + 'pt ' + Math.ceil( inHeight ) + 'pt;}');
}

// Generic function to resize the page that will be generated to a specific size in points. This
// variant will also set the trimbox
//
function adjustDocumentSizeToSizeInPointsWithTrimbox( inWidth, inHeight, inTrimboxLeft, inTrimboxTop, inTrimboxWidth, inTrimboxHeight ) {

	// Add a new style object to play with
	var cssPagedMedia = (function () {
    	var style = document.createElement('style');
    	document.head.appendChild(style);
    	return function (rule) {
        	style.innerHTML = rule;
    	};
	}());

	// Change the size
	cssPagedMedia('@page {size: ' + Math.ceil( inWidth ) + 'pt ' + Math.ceil( inHeight ) + 'pt; ' +
				  '-cchip-trimbox: ' + inTrimboxLeft + 'pt ' + inTrimboxTop + 'pt ' + 
				  inTrimboxWidth + 'pt ' + inTrimboxHeight + 'pt;}');
}

// Utility function to get a certain pagebox for a page given the page number and the name
// of the pagebox
//
function getPagebox( inPageNumber, inPagebox ) {

	// Get the page object
	var thePage = cals_doc_info.pages[inPageNumber];

	// Handle each page box
	if (inPagebox === 'trimbox') {
		if ( thePage.trimbox != null) {
			return thePage.trimbox;
		} else {
			return getPagebox( inPageNumber, 'cropbox');
		}
	}
	if (inPagebox === 'bleedbox') {
		if (thePage.bleedbox != null) {
			return thePage.bleedbox;
		} else {
			return getPagebox( inPageNumber, 'cropbox');
		}
	}
	if (inPagebox === 'artbox') {
		if (thePage.artbox != null) {
			return thePage.artbox;
		} else {
			return getPagebox( inPageNumber, 'cropbox');
		}
	}
	if (inPagebox === 'cropbox') {
		if (thePage.cropbox != null) {
			return thePage.cropbox;
		} else {
			return getPagebox( inPageNumber, 'mediabox');
		}
	}

	// Coming to the end of the line, we always return mediabox to cover all bases
	return thePage.mediabox;
}

// Given a page box, get an array with information for it. The coordinate system for the information
// returned is the HTML coordinate system: (0,0) equals top left corner, x-axis pointing right,
// y-axis pointing down. The following information is available in the different array slots
// 0: left
// 1: top
// 2: right
// 3: bottom
// 4: width
// 5: height
// 6: center (X)
// 7: middle (Y)
//
function getPageboxInfo( inPageNumber, inPagebox ) {

	// Start an empty array
	var theInfo = [];

	// Get the mediabox and the requested pagebox
	var theMediabox = getPagebox( inPageNumber, 'mediabox' );
	var thePagebox = getPagebox( inPageNumber, inPagebox );

	// The left of the box is the left of the pagebox - the left of the mediabox
	var theLeft = (thePagebox[0] - theMediabox[0]);

	// The width and height are independent of the mediabox
	var theWidth = thePagebox[2];
	var theHeight = thePagebox[3];

	// The right of the box is the left + width
	var theRight = theLeft + theWidth;

	// The bottom of the page box is the height of the mediabox - the y offset 
	// of the page box
	var theBottom = theHeight - (thePagebox[1] - theMediabox[1]); 

	// The top of the page box is the bottom of the pagebox - its height
	var theTop = theBottom - theHeight;

	// Center and middle
	var theCenter = theLeft + (theWidth / 2);
	var theMiddle = theTop + (theHeight / 2);

	// Store results
	theInfo.push( theLeft ); theInfo.push( theTop ); theInfo.push( theRight );
	theInfo.push( theBottom ); theInfo.push( theWidth ); theInfo.push( theHeight );
	theInfo.push( theCenter ); theInfo.push( theMiddle );

	// Return the information
	return theInfo;
}



//-------------------------------------------------------------------------------------------------
// POSITIONING
//-------------------------------------------------------------------------------------------------

// An enum to represent the different anchor points
//
var anchorPoints = {

	leftTop: 		{ value: 1, x: 'left', y: 'top' },
	centerTop: 		{ value: 2, x: 'center', y: 'top' },
	rightTop: 		{ value: 3, x: 'right', y: 'top' },
	leftMiddle: 	{ value: 4, x: 'left', y: 'middle' },
	centerMiddle: 	{ value: 5, x: 'center', y: 'middle' },
	rightMiddle: 	{ value: 6, x: 'right', y: 'middle' },
	leftBottom: 	{ value: 7, x: 'left', y: 'bottom' },
	centerBottom: 	{ value: 8, x: 'center', y: 'bottom' },
	rightBottom: 	{ value: 9, x: 'right', y: 'bottom' }
};
Object.freeze(anchorPoints);

// Positions the given element on the page
// ElementID: the CSS identifier for the object we want to move. Anything supported by jQuery is
//            supported here.
// ElementAnchor: the anchor you want to match up on the element.
// Pagenumber: the page about which we're excited
// Pagebox: the name of the pagebox you want to match up to.
// PageboxAnchor: the anchor you want to match up on the pagebox.
// OffsetX: an additional horizontal offset (positive is to the right) between the anchor points of
//          the object and the pagebox. Specified in pt.
// OffsetY: an additional vertical offset (positive is to the bottom) between the anchor points of
//          the object and the pagebox. Specified in pt.
//
function positionElement( inElementID, inElementAnchor, inPageNumber, inPagebox, inPageboxAnchor, inOffsetX, inOffsetY ) {

	// Start by copying the initial offsets - this will be adjusted to provide anchoring
	var theXPosition = inOffsetX;
	var theYPosition = inOffsetY;

	// Get the information for the anchor box
	var theAnchorboxInfo = getPageboxInfo( inPageNumber, inPagebox );

	// Adjust for pagebox anchoring
	if (inPageboxAnchor.x === 'left') {
		theXPosition += theAnchorboxInfo[0];
	} else if (inPageboxAnchor.x === 'center') {
		theXPosition += theAnchorboxInfo[6];
	} else {
		theXPosition += theAnchorboxInfo[2];
	}
	if (inPageboxAnchor.y === 'top') {
		theYPosition += theAnchorboxInfo[1];
	} else if (inPageboxAnchor.y === 'middle') {
		theYPosition += theAnchorboxInfo[7];
	} else {
		theYPosition += theAnchorboxInfo[3];
	}

	// Get the width and height of the element
	var theElementWidth = $(inElementID).innerWidth() * 0.75;
	var theElementHeight = $(inElementID).innerHeight() * 0.75;

	// Adjust for element anchoring
	if (inElementAnchor.x === 'center') {
		theXPosition -= (theElementWidth / 2);
	} else if (inElementAnchor.x === 'right') {
		theXPosition -= theElementWidth;
	}
	if (inElementAnchor.y === 'middle') {
		theYPosition -= (theElementHeight / 2);
	} else if (inElementAnchor.y === 'bottom') {
		theYPosition -= theElementHeight;
	}

	// Take the final position and adjust the position of the object
	$(inElementID).css({
		position: 'absolute',
		left: theXPosition + 'pt',
		top: theYPosition + 'pt'
	});
}



//-------------------------------------------------------------------------------------------------
// INFORMATION
//-------------------------------------------------------------------------------------------------

// Get the full file name of the file we're processing
//
function getFileName( inWithExtension ) {

	// By default return the filename without extension
	inWithExtension = typeof inWithExtension !== 'undefined' ? inWithExtension : false;

	// Get the filename itself and return the correct value	
	return (inWithExtension === true) ? cals_doc_info.document.name : cals_doc_info.document.name.split('.')[0];
}

// Get the path to the file we're processing
//
function getFilePath() {

	return cals_doc_info.document.path;
}

// Get the number of pages in the document
//
function getNumberOfPages() {

	return cals_doc_info.document.numberofpages;
}

// Get the value of the named variable (or NULL) if it doesn't exist
//
function getVariableValue( inName ) {

	// Get the array with variables
	var theVariables = cals_doc_info.document.variables;

	// Loop over them and try to find the correct one
	for (var theIndex = 0; theIndex < theVariables.length; theIndex++) {
		if (theVariables[theIndex].name === inName) return theVariables[theIndex].value;
	}

	// Found nothing
	debugLog( "Variable not found while searching for: " + inName );
	return null;
}

// Get the value of the named variable or use the default if it doesn't exist
//
function getVariableValueWithDefault( inName, inDefault ) {

	// Get the array with variables
	var theVariables = cals_doc_info.document.variables;

	// Loop over them and try to find the correct one
	for (var theIndex = 0; theIndex < theVariables.length; theIndex++) {
		if (theVariables[theIndex].name === inName) return theVariables[theIndex].value;
	}

	// Found nothing
	return inDefault;
}




//-------------------------------------------------------------------------------------------------
// BARCODES
//-------------------------------------------------------------------------------------------------

// Set the correct value to the barcode represented by the given object ID
//
function updateBarcodeData( inElementID, inValue ) {

	// Find the object and its data member and set the value attribute
	$(inElementID).find('param[name=data]').attr('value', inValue);

	// In order to update the barcode, we need to detach and reattach it
	var theParent = $(inElementID).parent();
	var theBarcode = $(inElementID).detach();
	theParent.append( theBarcode );
}



//-------------------------------------------------------------------------------------------------
// INKS
//-------------------------------------------------------------------------------------------------

// Gets the list of inks for a page
//
function getInkList( inPageNumber ) {

	return cals_doc_info.pages[inPageNumber].inks;
}

// Gets the number of inks for a page
//
function getInkCount( inPageNumber ) {

	return cals_doc_info.pages[inPageNumber].inks.length;
}

// Returns a textual color definition that is understood by cchip for a specific ink
//
function getInkDefinitionAsText( inInk ) {

	// Look at the ink name and differentiate between the different possibilities
	if (inInk.name === 'Cyan') return '-cchip-cmyk( 1.0, 0, 0, 0)';
	if (inInk.name === 'Magenta') return '-cchip-cmyk( 0, 1.0, 0, 0)';
	if (inInk.name === 'Yellow') return '-cchip-cmyk( 0, 0, 1.0, 0)';
	if (inInk.name === 'Black') return '-cchip-cmyk( 0, 0, 0, 1.0)';

	// Coming here we have a spot color. Look at the alternate name to decide what kind of model
	if (inInk.alternatename === 'cmyk') {
		return '-cchip-cmyk( "' + inInk.name + '", ' + inInk.alternatecomps.join( ', ') + ')'; 
	}
	if (inInk.alternatename === 'lab') {

		return '-cchip-lab( "' + inInk.name + '", ' + inInk.alternatecomps.join( ', ') + ')'; 
	}

	// Unknown case, just return something wrong and obviously wrong
	return 'pink';
}

// Returns a textual color definition that is understood by cchip for a specific ink
//
function getInkDefinitionWithTintAsText( inInk, inTint ) {

	// Look at the ink name and differentiate between the different possibilities - for CMYK the tint
	// is taken as the actual value
	if (inInk.name === 'Cyan') return '-cchip-cmyk( inTint, 0, 0, 0)';
	if (inInk.name === 'Magenta') return '-cchip-cmyk( 0, inTint, 0, 0)';
	if (inInk.name === 'Yellow') return '-cchip-cmyk( 0, 0, inTint, 0)';
	if (inInk.name === 'Black') return '-cchip-cmyk( 0, 0, 0, inTint)';

	// Coming here we have a spot color. Look at the alternate name to decide what kind of model. In each
	// case return the spot color value including a tint
	if (inInk.alternatename === 'cmyk') {
		return '-cchip-cmyk( "' + inInk.name + '", ' + inInk.alternatecomps.join( ', ') + ',' + inTint + ')'; 
	}
	if (inInk.alternatename === 'lab') {

		return '-cchip-lab( "' + inInk.name + '", ' + inInk.alternatecomps.join( ', ') + ',' + inTint + ')'; 
	}

	// Unknown case, just return something wrong and obviously wrong
	return 'pink';
}


//-------------------------------------------------------------------------------------------------
// MEASUREMENT SUPPORT
//-------------------------------------------------------------------------------------------------

// Converts the incoming value (with a measurement name), to points
//
function convertToPoints( inValue, inUnit ) {

    // Feet
    if (inUnit == "ft") {
        return inValue * 12 * 72;
    }

    // Inch
    if (inUnit == "in") {
        return inValue * 72;
    }

    // mm
    if (inUnit == "mm") {
        return inValue * 2.83465;
    }

    // cm
    if (inUnit == "cm") {
        return inValue * 28.3465;
    }

    // meter
    if (inUnit == "m") {
        return inValue * 2834.65;
    }

    // Anything else is simply interpreted as points
    return inValue;
}

// Converts the incoming value from points to pixels
//
function pointToPixel( inPoints ) {

	return inPoints * .75;
}

// Converts the incoming value from pixels to points
//
function pixelToPoint( inPixels ) {

	return inPixels / 4 * 3;
}

// Converts the incoming value (in the given unit), into a value in the destination unit and then
// formats it into a nicely readable string.
// Supported units: "pt", "mm", "cm", "in", "ft", "m"
//
function formatMeasurement( inValue, inFromUnit, inToUnit, inDigits ) {

	// Take the value and convert it to points
	var theResultValue = convertToPoints( inValue, inFromUnit );

	// feet
	if (inToUnit == "ft") {
        theResultValue = theResultValue / 72 / 12;
        return theResultValue.toFixed( inDigits ) + "ft";
    }

	// inch
	if (inToUnit == "in") {
        theResultValue = theResultValue / 72;
        return theResultValue.toFixed( inDigits ) + "pt";
    }

    // mm
    if (inToUnit == "mm") {
        theResultValue = theResultValue / 2.83465;
        return theResultValue.toFixed( inDigits ) + "mm";
    }

    // cm
    if (inToUnit == "cm") {
        theResultValue = theResultValue / 28.3465;
        return theResultValue.toFixed( inDigits ) + "cm";
    }

    // m
    if (inToUnit == "m") {
        theResultValue = theResultValue / 2834.65;
        return theResultValue.toFixed( inDigits ) + "m";
    }

    // points
    return theResultValue.toFixed( inDigits ) + "pt";
}

