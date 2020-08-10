//-------------------------------------------------------------------------------------------------
// Script content for the "Add job ID" fixup
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2018 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	// Get the current job ID value
	var theJobID = pcGetVariableValue( 'place_content_jobid' );
	$('#gutterinfo_jobID_text').text( theJobID );
	pcUpdateBarcodeData( '#gutterinfo_jobID_barcode', theJobID );

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++ ) {

		// Make sure we're working 1 on 1
		pcAdjustDocumentSizeToMediabox( thePageIndex );

		// Position our HTML elements
		pcPositionElement( '#gutterinfo_jobID_text', pcAnchorPoints.leftBottom, 
						   thePageIndex, pcPagebox.cropbox, pcAnchorPoints.leftBottom, 
						   5, -5 );
		pcPositionElement( '#gutterinfo_jobID_barcode', pcAnchorPoints.rightBottom, 
						    thePageIndex, pcPagebox.cropbox, pcAnchorPoints.rightBottom, 
						   -5, -5 );

		// Output the current page
		cchip.printPages(1);
	}
}

