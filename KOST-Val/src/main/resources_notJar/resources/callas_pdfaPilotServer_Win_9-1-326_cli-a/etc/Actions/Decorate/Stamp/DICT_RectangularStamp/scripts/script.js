//-------------------------------------------------------------------------------------------------
// Script content
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2017 - Four Pees
// Author: David van Driessche
// Last change: 2017-02-21
//-------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Customizations
//-------------------------------------------------------------------------------------------------
// You can use the following variables to customize how your stamp is constructed. 

var cStampReceived_left				= "5mm";
var cStampReceived_top				= "11mm";
var cStampReceived_width			= "38mm";
var cStampReceived_align			= "center";
var cStampReceived_point_size		= "14pt";

var cStampReference_left			= "5mm";
var cStampReference_top				= "42mm";
var cStampReference_width			= "38mm";
var cStampReference_align			= "center";
var cStampReference_point_size		= "12pt";

var cStampDate_left					= "5mm";
var cStampDate_top					= "27mm";
var cStampDate_width				= "38mm";
var cStampDate_align				= "center";
var cStampDate_point_size			= "12pt";



//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	// Calculate the page range we need to execute this on
	var theLowerLimit = 0; 
	var theUpperLimit = cals_doc_info.pages.length;

	// Loop over all pages
	for( var thePageIndex = theLowerLimit; thePageIndex < theUpperLimit; thePageIndex++ ) {

		// Our generated PDF file will be just as big as the stamp
		adjustDocumentSizeToHtmlElement( '#decoration-stamp' );

		// The received text should be translated. We only do this if the user didn't change the value in the 
		// HTML file
		if ($('#decoration-stamp-received').text() == "RECEIVED") {

			var theReceivedLocalised = "RECEIVED";
			switch (getVariableValue("language")) {
				case "de": {
					theReceivedLocalised = "EINGANG";
					break;
				}
				case "nl": {
					theReceivedLocalised = "BINNEN";
					break;
				}
				case "it": {
					theReceivedLocalised = "RICEVUTO";
					break;
				}
				case "fr": {
					theReceivedLocalised = "REÇU";
					break;
				}
				case "es": {
					theReceivedLocalised = "RECIBIDO";
					break;
				}
			}
			$('#decoration-stamp-received').text( theReceivedLocalised );
		}

		// Set the reference text
		var theVariableReference = getVariableValue( 'internalnumber' );
		$( '#decoration-stamp-reference').text( theVariableReference );

		// Set the date if necessary (if the date isn't necessary, it will be empty at this point)
		var theVariableDate = getVariableValue( 'stamp_date' );
		$( '#decoration-stamp-date').text( theVariableDate );

		// Setup the text positions and sizes with the variable from above
		$( '#decoration-stamp-received').css({ 'left': cStampReceived_left, 
											   'top': cStampReceived_top,
											   'width': cStampReceived_width,
											   'text-align': cStampReceived_align,
											   'font-size': cStampReceived_point_size });
		$( '#decoration-stamp-reference').css({ 'left': cStampReference_left, 
											    'top': cStampReference_top,
											    'width': cStampReference_width,
											    'text-align': cStampReference_align,
											    'font-size': cStampReference_point_size });
		$( '#decoration-stamp-date').css({ 'left': cStampDate_left, 
											'top': cStampDate_top,
										    'width': cStampDate_width,
										    'text-align': cStampDate_align,
											'font-size': cStampDate_point_size });

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

