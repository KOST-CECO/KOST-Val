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

	// Calculate all values we want to show
	var theTitle = cals_doc_info.document.info.title ? cals_doc_info.document.info.title : "-";
	var theCreationDateObject = dateFromPDFDate( getVariableValue( "creation_date" ) );
	var theCreationDate = (theCreationDateObject == null) ? "-" : theCreationDateObject.toLocaleDateString() + ", " + theCreationDateObject.toLocaleTimeString();
	var theModificationDateObject = dateFromPDFDate( getVariableValue( "modification_date" ) );
	var theModificationDate = (theModificationDateObject == null) ? "-" : theModificationDateObject.toLocaleDateString() + ", " + theModificationDateObject.toLocaleTimeString();
	var theAuthor = cals_doc_info.document.info.author ? cals_doc_info.document.info.author : "-";
	var theNumberOfPages = cals_doc_info.document.numberofpages;

	// Set the values
	$( '#document-title .value' ).text( theTitle );
	$( '#creation-date .value' ).text( theCreationDate );
	$( '#modification-date .value' ).text( theModificationDate );
	$( '#author .value' ).text( theAuthor );
	$( '#number-of-pages .value' ).text( theNumberOfPages );

	for( var thePageIndex = 0; thePageIndex < cals_doc_info.pages.length; thePageIndex++ ) {
		if (thePageIndex == 1) {
			$( '#summary' ).hide();
		}

		// We only want this on the first page...
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

// Get a PDF date string and return a date object
//
function dateFromPDFDate( inDateString ) {

	if ((inDateString == null) || (inDateString.length < 16)) {
		return null;
	}

	var theYear = inDateString.substring( 2, 6 );
	var theMonth = parseInt( inDateString.substring( 6, 8 ) ) - 1;
	var theDay = inDateString.substring( 8, 10 );
	var theHour = inDateString.substring( 10, 12 );
	var theMinute = inDateString.substring( 12, 14 );
	var theSecond = inDateString.substring( 14, 16 );
	return new Date( theYear, theMonth, theDay, theHour, theMinute, theSecond, 0 );
}
























