//-------------------------------------------------------------------------------------------------
// Script content
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2017 - Four Pees
// Author: David van Driessche
// Last change: 2017-02-19
//-------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

// Function called when we're done with the printing stuff and the images are ready as well
//
function imagesLoaded() {

	// Get the container where patches should be added
	var theContainer = $( "body" );

	// Text and font definition
	var theVariableText = getVariableValue( 'positioned_text' );
	var theVariableFontSize = parseInt( getVariableValue( 'fontsize_pt' ) );
	var theVariableFontName = getVariableValue( 'font_name' );

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

	// Rotated or not?
	var theVariableRotationDegrees = parseInt( getVariableValue( 'rotation_degrees' ) );

	// Color space definition
	var theVariableColorspace = getVariableValue( 'color_space' );
	var theVariableColorValue1 = parseFloat( getVariableValue( 'color_value_1' ) );
	var theVariableColorValue2 = parseFloat( getVariableValue( 'color_value_2' ) );
	var theVariableColorValue3 = parseFloat( getVariableValue( 'color_value_3' ) );
	var theVariableColorValue4 = parseFloat( getVariableValue( 'color_value_4' ) );
	var theColorDefinition = '';
	if (theVariableColorspace === "CMYKPercent") {
		theColorDefinition = colorCmykPercentage( theVariableColorValue1, theVariableColorValue2, 
												  theVariableColorValue3, theVariableColorValue4 );
	} else if (theVariableColorspace === "RGBZeroTo255") {
		theColorDefinition = colorRgb255( theVariableColorValue1, theVariableColorValue2, 
									      theVariableColorValue3 );
	} else {
		theColorDefinition = colorGrayPercentage( theVariableColorValue1 );
	}

	// Calculate the page range we need to execute this on
	var theLowerLimit = 0; 
	var theUpperLimit = cals_doc_info.pages.length;

	// Loop over all pages
	for( var thePageIndex = theLowerLimit; thePageIndex < theUpperLimit; thePageIndex++ ) {

		// Make sure we're working 1 on 1
		adjustDocumentSizeToMediabox( thePageIndex );

		// Remove everything that is already there
		theContainer.empty();

		// Add the text to our container
        var theDecorationText = $('<p>').appendTo( theContainer ).attr( 'id', 'decoration-text' );
        theDecorationText.text( theVariableText );
        theDecorationText.css( { 
        	'font-family': theVariableFontName,
        	'font-size': theVariableFontSize + 'pt',
        	'color': theColorDefinition
        } );

        // If we need to put the text in a region, we have some more work...
        if (theVariableSetWidthAndHeight) {
        	theDecorationText.css( {
        		'width': theVariableWidth + 'pt',
        		'height': theVariableHeight + 'pt'
        	} );
        }

        // If we need to rotate the text, do that...
        if (theVariableRotationDegrees != 0) {
        	theDecorationText.css( {
        		'-webkit-transform': 'rotate('+ theVariableRotationDegrees +'deg)'
        	} );
        }

		// Position it correctly
		positionElement( '#decoration-text', theAnchorText, thePageIndex, 'mediabox', 
 						 theAnchorBox, theVariableHorizontalOffset, theVariableVerticalOffset );

		// Output to the current page
		cchip.printPages(1);
	}


	// Let pdfChip know we're done
    cchip.endPrinting();
}

function cchipPrintLoop() {

    // Tell pdfChip we're getting ready and don't want to be interrupted too soon
    cchip.beginPrinting();

    // Nothing to load here

    // Do the rest of our work
    $('body').waitForImages( function(){ imagesLoaded(); } );
}




