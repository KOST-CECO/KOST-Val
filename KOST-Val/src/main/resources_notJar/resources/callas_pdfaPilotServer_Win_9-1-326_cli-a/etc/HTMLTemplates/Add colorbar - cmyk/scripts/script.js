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

	// Add the regular CMY patches
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(1,0,0,0)" );
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(0,1,0,0)" );
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(0,0,1,0)" );

	// Add secondary colors
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(1,1,0,0)" );
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(0,1,1,0)" );
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(1,0,1,0)" );

	// Add CMY and black
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(1,1,1,0)" );
	$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', "-cchip-cmyk(0,0,0,1)" );

	// Adjust the size of the PDF we're generating to our logo - we only have to do this once
	// (and we'll probably get away with it!)
	pcAdjustDocumentSizeToHtmlElement( '#main');

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++ ) {

		// Output to the current page
		cchip.printPages(1);
	}

}

