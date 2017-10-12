//-------------------------------------------------------------------------------------------------
// Script content for the "LFP - Place Grommets" fixup
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2016 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

// Function called when we're done with the printing stuff and the images are ready as well
//
function imagesLoaded() {

	// Read all variables coming from pdfToolbox
	var theMeasurementUnit = getVariableValue( 'measurement_unit' );		// "pt", "mm", "cm", "in"
	var theDistanceTop = convertToPoints( getVariableValue( 'distance_top' ), theMeasurementUnit);
	var theDistanceBottom = convertToPoints( getVariableValue( 'distance_bottom' ), theMeasurementUnit);
	var theDistanceLeft = convertToPoints( getVariableValue( 'distance_left' ), theMeasurementUnit);
	var theDistanceRight = convertToPoints( getVariableValue( 'distance_right' ), theMeasurementUnit);
	var theCountHorizontal = getVariableValue( 'count_horizontal' );		// -1 == not used, 0 == none
	var theCountVertical = getVariableValue( 'count_vertical' );			// -1 == not used, 0 == none
	var theSpacingHorizontal = convertToPoints( getVariableValue( 'spacing_horizontal' ), theMeasurementUnit);	// -1 == not used, 0 == none
	var theSpacingVertical = convertToPoints( getVariableValue( 'spacing_vertical' ), theMeasurementUnit);		// -1 == not used, 0 == none

	// debugLog( "Distances in points: ( " + theDistanceTop + ", " + theDistanceRight + ", " + 
	// 									 theDistanceBottom + ", " + theDistanceLeft + " )" );
	// debugLog( "Counts: [ " + theCountHorizontal + ", " + theCountVertical + " ]" );

	// Start by assuming that it's the count that is used. If that is not the case, we'll have to 
	// adjust that in our page loop
	var theNumberHorizontal = theCountHorizontal;
	var theNumberVertical = theCountVertical;
	var theCalculateBySpacing = (theNumberVertical < 0) || (theNumberHorizontal < 0);

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < cals_doc_info.pages.length; thePageIndex++ ) {

		// Make sure we're working 1 on 1
		adjustDocumentSizeToMediabox( 0 );

		var theMediabox = getPagebox( thePageIndex, "mediabox" )
		var theMediaboxInfo = getPageboxInfo( thePageIndex, "mediabox" )
		// debugLog( "media: " + theMediabox.join( ", " ) );
		// debugLog( "media info: " + theMediaboxInfo.join( ", " ) );

		// If we don't have exact numbers of grommets, calculate how many we need
		if (theCalculateBySpacing) {

			var thePageBoxInfo = getPageboxInfo( thePageIndex, 'trimbox' );
			var theTrimboxWidth = thePageBoxInfo[4];
			var theTrimboxHeight = thePageBoxInfo[5];
			if (theNumberHorizontal < 0)
				theNumberHorizontal = (theSpacingHorizontal < 1) ? 0 : Math.round( theTrimboxWidth / theSpacingHorizontal );
			if (theNumberVertical < 0)
				theNumberVertical = (theSpacingVertical < 1) ? 0 : Math.round( theTrimboxHeight / theSpacingVertical );
			// debugLog( "Spacing: [ " + theSpacingHorizontal + ", " + theSpacingVertical + " ]" );
			// debugLog( "Counts: [ " + theCountHorizontal + ", " + theCountVertical + " ]" );
			// debugLog( "Numbers: [ " + theNumberHorizontal + ", " + theNumberVertical + " ]" );
		}

		// Remove anything with class grommet
		$( '.grommet').remove();

		// Show the template grommet (for when we begin cloning)
		$( '#template_grommet').show();

		// Add the correct amount of grommets
		addGrommets( $( '#template_grommet'), theNumberHorizontal, theNumberVertical, thePageIndex,
					 theDistanceTop, theDistanceBottom, theDistanceLeft, theDistanceRight );

		// Hide the template grommet for output
		$( '#template_grommet').hide();

		// And output (always 1 page)
		cchip.printPages( 1 );
	}

	// Let pdfChip know we're done
    cchip.endPrinting();
}

function cchipPrintLoop() {

    // Tell pdfChip we're getting ready and don't want to be interrupted too soon
    cchip.beginPrinting();

    // Replace the source for our grommet template
    var theTemplateGrommet = $( '#template_grommet' );
    if (theTemplateGrommet) {
		var theTemplateGrommetPath = getVariableValue( 'grommet_template' ).replace(/\\/g,"/");		// Path to grommet file to place
	    if (theTemplateGrommetPath[0] !== '/') { // Windows drive letter must be prefixed with a slash
        theTemplateGrommetPath = '/' + theTemplateGrommetPath;
    }
	theTemplateGrommet.attr( "src", encodeURI('file://' + theTemplateGrommetPath) );
	}

	// As we changed the image for the grommet, wait until it's loaded
    $('body').waitForImages( function(){ imagesLoaded(); } );
}

// This function adds the necessary amount of grommets
//
function addGrommets( inTemplateGrommet, inNumberHorizontal, inNumberVertical, inPageIndex,
					  theDistanceTop, theDistanceBottom, theDistanceLeft, theDistanceRight ) {

	// We can deal with a number of grommets that is 0 (or none) but we can't deal with 1 or less than 0
	if ((inNumberHorizontal == 1) || (inNumberHorizontal < 0)) inNumberHorizontal = 2;
	if ((inNumberVertical == 1) || (inNumberVertical < 0)) inNumberVertical = 2;

	// Calculate the width and height we have (trimbox - the margins
	var thePageBoxInfo = getPageboxInfo( inPageIndex, 'trimbox' );
	var theTrimboxWidth = thePageBoxInfo[4];
	var theTrimboxHeight = thePageBoxInfo[5];
	var theMarginsHorizontal = theDistanceLeft + theDistanceRight;
	var theMarginsVertical = theDistanceTop + theDistanceBottom;
	var theTrimboxWidthMinusMargins = theTrimboxWidth - theMarginsHorizontal;
	var theTrimboxHeightMinusMargins = theTrimboxHeight - theMarginsVertical;

	// Calculate how much spacing we need between grommets
	var theDeltaX = theTrimboxWidthMinusMargins / (inNumberHorizontal-1);
	var theDeltaY = theTrimboxHeightMinusMargins / (inNumberVertical-1);

	// An index for the grommet we're placing
	var theGrommetIndex = 1;

	// Add grommets vertically - we need to add "inNumberVertical"
	for (var theIndexY = 0; theIndexY < inNumberVertical; theIndexY++) {

		// Add a grommet vertically along the left border
		addGrommet( inTemplateGrommet, 'grommet_' + theGrommetIndex, anchorPoints.centerMiddle, inPageIndex,
					'trimbox', anchorPoints.leftTop, 
					theDistanceLeft, theDistanceTop + (theIndexY * theDeltaY) );
		theGrommetIndex += 1;

		// Add a grommet vertically along the right border
		addGrommet( inTemplateGrommet, 'grommet_' + theGrommetIndex, anchorPoints.centerMiddle, inPageIndex,
					'trimbox', anchorPoints.leftTop, 
					theTrimboxWidth - theDistanceRight, theDistanceTop + (theIndexY * theDeltaY) );
		theGrommetIndex += 1;
	}

	// Add grommets horizontally - we need to add "inNumberHorizontal - 2". The difference is
	// because we don't want to add grommets to the corners twice so we have to skip those
	for (var theIndexX = 1; theIndexX < (inNumberHorizontal-1) ; theIndexX++) {

		// Add a grommet vertically along the left border
		addGrommet( inTemplateGrommet, 'grommet_' + theGrommetIndex, anchorPoints.centerMiddle, inPageIndex,
					'trimbox', anchorPoints.leftTop, 
					theDistanceLeft + (theIndexX * theDeltaX), theDistanceTop );
		theGrommetIndex += 1;

		// Add a grommet vertically along the right border
		addGrommet( inTemplateGrommet, 'grommet_' + theGrommetIndex, anchorPoints.centerMiddle, inPageIndex,
					'trimbox', anchorPoints.leftTop, 
					theDistanceLeft + (theIndexX * theDeltaX), theTrimboxHeight - theDistanceBottom );
		theGrommetIndex += 1;
	}
}

// This function creates one specific grommet in one specific location
//
function addGrommet( inTemplateGrommet, inID, inElementAnchor, inPageIndex, inPagebox, inPageboxAnchor, inOffsetX, inOffsetY ) {

	// Clone the template
	var theGrommet = inTemplateGrommet.clone().attr('class', 'grommet').attr('id', inID);
	theGrommet.appendTo( 'body' );

	// Position it
	positionElement( '#' + inID, inElementAnchor, inPageIndex, inPagebox, inPageboxAnchor, inOffsetX, inOffsetY );
}

// Calculates a given value into points, taking into account a given unit ("pt", "mm", "cm", "in")
//
function convertToPoints( inValue, inUnit ) {

	// Inch
	if (inUnit == "in") {
		return inValue * 72;
	}

	// mm
	if (inUnit == "mm") {
		return inValue * 2.83465;
	}

	// cm
	if (inUnit == "cm") {
		return inValue * 28.3465;
	}

	// Anything else is simply interpreted as points
	return inValue
}




