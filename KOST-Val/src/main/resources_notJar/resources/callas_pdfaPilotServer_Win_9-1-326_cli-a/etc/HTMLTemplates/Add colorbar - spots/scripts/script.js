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

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++ ) {

		// Remove what is already there
		$("div.patch").remove();

		// Get the ink list for the current page
		var theInkList = pcGetInkList( thePageIndex );

		// Loop over the ink list and produce patches for any spot color in it
		// Loop over all inks and add informational items for each one
		for (var theInkIndex = 0; theInkIndex < theInkList.length; theInkIndex++ ) {

			// Get the current ink
			var theInk = theInkList[theInkIndex];

			// Only add it if it is actually used and if it's not a CMYK color
			if ((theInk.usagecm > 0) &&
				(theInk.name !== 'Cyan') &
				(theInk.name !== 'Magenta') &
				(theInk.name !== 'Yellow') &
				(theInk.name !== 'Black')) {

				var theBackgroundColorDefinition = pcGetInkDefinitionWithTintAsText( theInk, 0.1 );
				$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theBackgroundColorDefinition );
				var theBackgroundColorDefinition = pcGetInkDefinitionWithTintAsText( theInk, 0.25 );
				$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theBackgroundColorDefinition );
				theBackgroundColorDefinition = pcGetInkDefinitionWithTintAsText( theInk, 0.5 );
				$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theBackgroundColorDefinition );
				theBackgroundColorDefinition = pcGetInkDefinitionWithTintAsText( theInk, 0.75 );
				$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theBackgroundColorDefinition );
				theBackgroundColorDefinition = pcGetInkDefinitionWithTintAsText( theInk, 1 );
				$('<div>').appendTo( theContainer ).attr( 'class', 'patch' ).css( 'background-color', theBackgroundColorDefinition );
			}
		}

		// Adjust the size of the document to the color bar
		pcAdjustDocumentSizeToHtmlElement( '#main');

		// Output to the current page
		cchip.printPages(1);
	}

}

