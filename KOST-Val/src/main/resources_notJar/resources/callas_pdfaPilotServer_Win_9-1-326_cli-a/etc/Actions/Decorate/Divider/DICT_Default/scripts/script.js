//-------------------------------------------------------------------------------------------------
// Script content
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2017 - Four Pees
// Author: David van Driessche
// Last change: 2017-02-21
//-------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	var theMultiple = parseInt( getVariableValue( "page_number_multiple" ) );
	var theDivisor = theMultiple + 1;

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < cals_doc_info.pages.length; thePageIndex++ ) {

		// Change the text to the current page number
		$( '#decoration-page-number').text( thePageIndex + 1 );

		// We only want to show this in the correct places
		var theIsAfter = getVariableValue( "after_before" ) == "After";

		// We inserted pages, so we need to change the divisor
		var thePageNumberToTest = theIsAfter ? (thePageIndex +1) : (thePageIndex+2);
		var theMustShow = (thePageNumberToTest % theDivisor) === 0;

		// Show or hide the page number
		if (theMustShow) {
			$( '#decoration-page-number').show();
		} else {
			$( '#decoration-page-number').hide();
		}

		// Our generated PDF file will be just as big as the stamp
		adjustDocumentSizeToHtmlElement( '#decoration' );

		// Output to the current page
		cchip.printPages(1);
	}
}


//-------------------------------------------------------------------------------------------------
// Utility routines
//-------------------------------------------------------------------------------------------------

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
	return null;
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

