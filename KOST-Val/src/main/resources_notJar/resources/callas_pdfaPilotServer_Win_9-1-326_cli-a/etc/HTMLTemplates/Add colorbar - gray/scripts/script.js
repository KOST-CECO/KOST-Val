//-------------------------------------------------------------------------------------------------
// Script content for the "Add colorbar - gray" fixup
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2018 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	// Get the container where patches should be added
	var theContainer = $( "#main" );

	// Get the percentage for the current patch and the other details for the bar
	var thePercentage = 100;
	var theNumberOfSteps = 11;
	var theDelta = 10;

	// Loop over all patches and create them
	for (var theIndex = 0; theIndex < theNumberOfSteps; theIndex++) {

		// Create a new div, with the correct class and background color
		var the100th = String( (thePercentage - (theIndex*theDelta)) / 100 );
		var theColorDefinition = colorCmyk( the100th, the100th, the100th, the100th );
    	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theColorDefinition );
	}

	// Adjust the size of the PDF we're generating to our logo - we only have to do this once
	// (and we'll probably get away with it!)
	pcAdjustDocumentSizeToHtmlElement( '#main');

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++ ) {

		// Output to the current page
		cchip.printPages(1);
	}

}

