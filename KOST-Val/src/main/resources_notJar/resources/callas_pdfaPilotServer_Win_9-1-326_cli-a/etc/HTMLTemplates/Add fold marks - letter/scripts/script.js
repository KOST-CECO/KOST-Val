//-------------------------------------------------------------------------------------------------
// Script content for the "Place fold marks (letter)" fixup
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2018 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++ ) {

		// Make sure we're working 1 on 1
		pcAdjustDocumentSizeToMediabox( thePageIndex );

		// Remove any existing fold marks
		$( '.foldmark' ).remove();

		// Add fold marks
		addFoldMarks( thePageIndex );

		// Output to the current page
		cchip.printPages(1);
	}

}


//-------------------------------------------------------------------------------------------------
// Support functions
//-------------------------------------------------------------------------------------------------

// This function adds fold marks
//
function addFoldMarks( inPageIndex ) {

	// Calculate the position of the fold marks
	var thePageBoxInfo = pcGetPageboxInfo( inPageIndex, pcPagebox.trimbox );
	var theTrimHeight = thePageBoxInfo[5];
	var theMark1Y = (theTrimHeight / 3);
	var theMark2Y = (theTrimHeight / 3) * 2;

	// Add a div for each fold mark in the correct location
	var theMark1 = $('<div>',{id:'mark1'}).appendTo( $( "#main" ) ).attr( 'class', 'foldmark' ); 
	debugLog( theMark1.parent().attr( 'id' ) );
	pcPositionElement( '#mark1', pcAnchorPoints.leftMiddle, inPageIndex, pcPagebox.trimbox, pcAnchorPoints.leftTop, 0, theMark1Y );
	var theMark2 = $('<div>',{id:'mark2'}).appendTo( $( "#main" ) ).attr( 'class', 'foldmark' ); 
	pcPositionElement( '#mark2', pcAnchorPoints.leftMiddle, inPageIndex, pcPagebox.trimbox, pcAnchorPoints.leftTop, 0, theMark2Y );
}