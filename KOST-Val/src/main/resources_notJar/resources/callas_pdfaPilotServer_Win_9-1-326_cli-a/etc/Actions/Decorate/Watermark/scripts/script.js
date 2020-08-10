//-------------------------------------------------------------------------------------------------
// Script content
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2017 - Four Pees
// Author: David van Driessche
// Last change: 2017-02-20
//-------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

// Function called when we're done with the printing stuff and the images are ready as well
//
function imagesLoaded() {


	// The watermark image is always centered
	var theAnchorText = anchorPoints.centerMiddle;
	var theAnchorBox = anchorPoints.centerMiddle;

	// Calculate the page range we need to execute this on
	var theLowerLimit = 0; 
	var theUpperLimit = cals_doc_info.pages.length;

	// Loop over all pages
	for( var thePageIndex = theLowerLimit; thePageIndex < theUpperLimit; thePageIndex++ ) {

		// Make sure we're working 1 on 1
		adjustDocumentSizeToMediabox( thePageIndex );

   		// If the image needs to be scaled, do so
   		var theVariableSetWidthAndHeight = getVariableValue( "scale_to_fit" );
    	if (theVariableSetWidthAndHeight == "1") {

    		// Get the width and height of the mediabox
			var theMediaboxInfo = getPageboxInfo( thePageIndex, "mediabox" );
			var theWidth = theMediaboxInfo[4] * 0.95;
			var theHeight = theMediaboxInfo[5] * 0.95;

     		$('#decoration-image').css( {
       			'width': theWidth + 'pt',
       			'height': 'auto' ///theHeight + 'pt'
       		} );
    	}
		
		// Position it correctly
		positionElement( '#decoration-image', theAnchorText, thePageIndex, 'mediabox', 
 						 theAnchorBox, 0, 0 );

		//debugLog( $('html').html() );

		// Output to the current page
		cchip.printPages(1);
	}

	// Let pdfChip know we're done
    cchip.endPrinting();
}

function cchipPrintLoop() {

    // Tell pdfChip we're getting ready and don't want to be interrupted too soon
    cchip.beginPrinting();

    // Get the full path to our image and set it as the source of our image in the HTML
  	$( '#decoration-image' ).attr( { 'src' : getPathToImage() } );

    // Do the rest of our work
    $('body').waitForImages( function(){ imagesLoaded(); } );
}


//-------------------------------------------------------------------------------------------------
// Utility functions
//-------------------------------------------------------------------------------------------------

function getEncodedFileUrl( inPath ) {
    var pathName = inPath.replace(/\\/g,"/");
    if (pathName[0] !== '/') { // Windows drive letter must be prefixed with a slash
        pathName = '/' + pathName;
    }
    return encodeURI('file://' + pathName);
}

function getPathToImage() {

	// Get the full path from our variable
	var theVariableImagePath = getVariableValue( 'imagepath' );

	// Modify it to proper path dividers and add the "file" convention
	return getEncodedFileUrl( theVariableImagePath );
}







