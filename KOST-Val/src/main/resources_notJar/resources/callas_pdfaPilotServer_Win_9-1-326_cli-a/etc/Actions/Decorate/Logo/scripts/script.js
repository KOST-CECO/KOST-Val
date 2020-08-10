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

	// Position and (optionally size) of the text block
	var theVariableHorizontalOffset = parseFloat( getVariableValue( 'hor_offset' ) ); 
	var theVariableVerticalOffset = parseFloat( getVariableValue( 'ver_offset' ) );
	var theVariableWidth = parseFloat( getVariableValue( 'hor_size_mouse' ) );
	var theVariableHeight = parseFloat( getVariableValue( 'ver_size_mouse' ) );
	var theVariableSetWidthAndHeight = (theVariableWidth > 0) && (theVariableHeight > 0);
	var theVariableRelativeTo = getVariableValue( 'position' );

	// Decide how to position, based on the incoming "position" parameter
	var theAnchorText = anchorPoints.leftBottom;
	var theAnchorBox = anchorPoints.leftBottom;
	switch (theVariableRelativeTo) {

		case "UpperLeft":
			var theAnchorText = anchorPoints.leftTop;
			var theAnchorBox = anchorPoints.leftTop;
			break;

	 	case "Top":
			var theAnchorText = anchorPoints.centerTop;
			var theAnchorBox = anchorPoints.centerTop;
			break;

		case "UpperRight":
			var theAnchorText = anchorPoints.rightTop;
			var theAnchorBox = anchorPoints.rightTop;
			break;

		case "LowerLeft":
		case "MouseArea":
			// nothing to do
			break;

	 	case "Bottom":
			var theAnchorText = anchorPoints.centerBottom;
			var theAnchorBox = anchorPoints.centerBottom;
			break;

	 	case "LowerRight":
			var theAnchorText = anchorPoints.rightBottom;
			var theAnchorBox = anchorPoints.rightBottom;
			break;

	 	case "Left":
			var theAnchorText = anchorPoints.leftMiddle;
			var theAnchorBox = anchorPoints.leftMiddle;
			break;

	 	case "Center":
			var theAnchorText = anchorPoints.centerMiddle;
			var theAnchorBox = anchorPoints.centerMiddle;
			break;

	 	case "Right":
			var theAnchorText = anchorPoints.rightMiddle;
			var theAnchorBox = anchorPoints.rightMiddle;
			break;
	}

   	// If the image needs to be scaled, do so
    if (theVariableSetWidthAndHeight) {
     	$('#decoration-image').css( {
       		'width': theVariableWidth + 'pt',
       		'height': theVariableHeight + 'pt'
       	} );
    }

	// Calculate the page range we need to execute this on
	var theLowerLimit = 0; 
	var theUpperLimit = cals_doc_info.pages.length;

	// Loop over all pages
	for( var thePageIndex = theLowerLimit; thePageIndex < theUpperLimit; thePageIndex++ ) {

		// Make sure we're working 1 on 1
		adjustDocumentSizeToMediabox( thePageIndex );

		// Position it correctly
		positionElement( '#decoration-image', theAnchorText, thePageIndex, 'mediabox', 
 						 theAnchorBox, theVariableHorizontalOffset, theVariableVerticalOffset );

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







