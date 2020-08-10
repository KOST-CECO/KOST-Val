//-------------------------------------------------------------------------------------------------
// Main Javascript for the Place Content fixup template
//
//-------------------------------------------------------------------------------------------------
// Author: David van Driessche
// Copyright © 2017 - Four Pees
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// cchipPrintLoop
//
// If present in a Javascript file referenced from the template HTML; pdfToolbox will call this
// function (the function with the exact name "cchipPrintLoop") automagically. If it is present,
// this function is responsible for outputting the PDF document that will be overlaid on the PDF
// document that is being processed.
//
// This function can refer to the cals_doc_info object to figure out information about the document
// being processed (such as the number of pages) and can use the cchip.printPages() function to
// generate PDF from the current state of the HTML template.
//-------------------------------------------------------------------------------------------------

function cchipPrintLoop() {

	// Add a container element
	var theContainer = $( '#container' );

	// Loop over all pages
	for( var thePageIndex = 0; thePageIndex < cals_doc_info.pages.length; thePageIndex++ ) {

		// Give us a blank slate by removing everything inside of the main element
		$( '#container *' ).remove();

		// Make sure we're working 1 on 1
		pcAdjustDocumentSizeToMediabox( thePageIndex );

		// Get information on the trim box
		var theTrimboxInfo = pcGetPageboxInfo( thePageIndex, pcPagebox.trimbox );

		// Add a space for the height and width arrow
		var theDivHeight = $('<div>').appendTo( theContainer ).attr( 'class', 'arrow' ).attr( 'id', 'arrow_height' );
		var theDivWidth = $('<div>').appendTo( theContainer ).attr( 'class', 'arrow' ).attr( 'id', 'arrow_width' );

		// Set the height and width of these divs
		theDivHeight.width( "10mm" ).height( theTrimboxInfo[5] / 3 * 4 );
		theDivWidth.height( "10mm" ).width( theTrimboxInfo[4] / 3 * 4 );

		// Add the arrows
		addVerticalArrow( 'arrow_height', theTrimboxInfo[5] );
		addHorizontalArrow( 'arrow_width', theTrimboxInfo[4] );

		// Position both containers correctly outside of the trim box
		var theMargin = 10;
		pcPositionElement( '#arrow_height', pcAnchorPoints.rightMiddle, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.leftMiddle, -theMargin, 0 );
		pcPositionElement( '#arrow_width', pcAnchorPoints.centerTop, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.centerBottom, 0, theMargin );

		// Add the dimension elements
		var theTrimWidthInMm = unitConvertFromPoints( theTrimboxInfo[4], unitUnits.mm );
		var theTrimHeightInMm = unitConvertFromPoints( theTrimboxInfo[5], unitUnits.mm );
		var theMeasurementHorizontal = $('<div>').appendTo( theContainer ).attr( 'id', 'horizontal-dimension' ).text( unitFormatWithUnit( theTrimWidthInMm, unitUnits.mm, 0 ) );
		var theMeasurementVertical = $('<div>').appendTo( theContainer ).attr( 'id', 'vertical-dimension' ).text( unitFormatWithUnit( theTrimHeightInMm, unitUnits.mm, 0 ) );

		// And position them correctly
		pcPositionElement( '#horizontal-dimension', pcAnchorPoints.centerTop, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.centerBottom, 0, theMargin );
		pcPositionElement( '#vertical-dimension', pcAnchorPoints.centerTop, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.leftMiddle, -theMargin - 13, -11 );

		// Output to the current page
		cchip.printPages(1);
	}
}


//-------------------------------------------------------------------------------------------------
// Arrow mark support
//-------------------------------------------------------------------------------------------------

function addHorizontalArrow( inContainerID, inWidth ) {

	// Calculate the width in mm (that's how we want to display it)
	var theWidth = inWidth * 25.4 / 72;
	var theCenter = theWidth / 2;
	var theTextGap = 20;

	// Create an SVG context
	svgCreateContext( inContainerID );
	svgSetMeasurementSystem( svgMeasurements.mm );

	// Draw the horizontal arrow line
	svgPathCreate();
	svgPathMoveTo( 0, 5, false );
	svgPathLineTo( theCenter - (theTextGap / 2), 5, false );
	svgPathMoveTo( theWidth - 0, 5, false );
	svgPathLineTo( theCenter + (theTextGap / 2), 5, false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Draw arrow heads of 3mm
	var theArrowSize = 2;
	var theArrowHeight = 1.5;

	// Left hand side arrow end
	svgPathCreate();
	svgPathMoveTo( theArrowSize, 5 - theArrowHeight, false );
	svgPathLineTo( 0, 5, false );
	svgPathLineTo( theArrowSize, 5 + theArrowHeight, false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Right hand side arrow end
	svgPathCreate();
	svgPathMoveTo( theWidth - theArrowSize, 5 - theArrowHeight, false );
	svgPathLineTo( theWidth - 0, 5, false );
	svgPathLineTo( theWidth - theArrowSize, 5 + theArrowHeight, false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Add the dimensions
	svgCreateText( theCenter, 2, theWidth.toFixed( 1 ) + " mm" );
	svgShapeFill( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ) );
	svgShapeFont( 'FreeUniversal', 10 );
	svgShapeTextAlign( 'middle' );
	
	svgCloseCurrentContext();
}

function addVerticalArrow( inContainerID, inHeight ) {

	// Calculate the height in mm (that's how we want to display it)
	var theHeight = inHeight * 25.4 / 72;
	var theCenter = theHeight / 2;
	var theTextGap = 20;

	// Create an SVG context
	svgCreateContext( inContainerID );
	svgSetMeasurementSystem( svgMeasurements.mm );

	// Draw the vertical arrow line
	svgPathCreate();
	svgPathMoveTo( 5, 0, false );
	svgPathLineTo( 5, theCenter - (theTextGap / 2), false );
	svgPathMoveTo( 5, theHeight - 0, false );
	svgPathLineTo( 5, theCenter + (theTextGap / 2), false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Draw arrow heads of 3mm
	var theArrowSize = 2;
	var theArrowHeight = 1.5;

	// Left hand side arrow end
	svgPathCreate();
	svgPathMoveTo( 5 - theArrowHeight, theArrowSize, false );
	svgPathLineTo( 5, 0, false );
	svgPathLineTo( 5 + theArrowHeight, theArrowSize, false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Right hand side arrow end
	svgPathCreate();
	svgPathMoveTo( 5 - theArrowHeight, theHeight - theArrowSize, false );
	svgPathLineTo( 5, theHeight - 0, false );
	svgPathLineTo( 5 + theArrowHeight, theHeight - theArrowSize, false );
	svgShapeStroke( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ), 0.25 );

	// Add the dimensions
	svgCreateText( 5, theCenter - 2, theHeight.toFixed( 1 ) + " mm" );
	svgShapeFill( colorSpotCmyk( "Dimensions", 0, 0, 0, 0.75 ) );
	svgShapeFont( 'FreeUniversal', 10 );
	svgShapeTextAlign( 'middle' );
	svgGetCurrentShape().rotate( 90 );

	svgCloseCurrentContext();
}


