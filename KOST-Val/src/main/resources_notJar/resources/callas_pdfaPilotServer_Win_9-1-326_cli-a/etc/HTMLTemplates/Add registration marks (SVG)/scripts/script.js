//-------------------------------------------------------------------------------------------------
// Script content for the "Add registration marks (SVG)" fixup
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

		// Give us a blank slate
		$( 'div.mark' ).remove();

		// Make sure we're working 1 on 1
		pcAdjustDocumentSizeToMediabox( thePageIndex );

		// Add four registration marks 	
		var theMargin = 15;

		var theMark = $('<div>').appendTo( theContainer ).attr( 'class', 'mark' ).attr( 'id', 'mark_1' );
		addRegistrationMark( 'mark_1' );
		pcPositionElement( '#mark_1', pcAnchorPoints.centerMiddle, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.leftMiddle, -theMargin, 0 );

		theMark = $('<div>').appendTo( theContainer ).attr( 'class', 'mark' ).attr( 'id', 'mark_2' );
		addRegistrationMark( 'mark_2' );
		pcPositionElement( '#mark_2', pcAnchorPoints.centerMiddle, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.centerTop, 0, -theMargin );

		theMark = $('<div>').appendTo( theContainer ).attr( 'class', 'mark' ).attr( 'id', 'mark_3' );
		addRegistrationMark( 'mark_3' );
		pcPositionElement( '#mark_3', pcAnchorPoints.centerMiddle, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.centerBottom, 0, theMargin );

		theMark = $('<div>').appendTo( theContainer ).attr( 'class', 'mark' ).attr( 'id', 'mark_4' );
		addRegistrationMark( 'mark_4' );
		pcPositionElement( '#mark_4', pcAnchorPoints.centerMiddle, thePageIndex, pcPagebox.trimbox, pcAnchorPoints.rightMiddle, theMargin, 0 );

		// Output to the current page
		cchip.printPages(1);
	}

}


//-------------------------------------------------------------------------------------------------
// Registration mark support
//-------------------------------------------------------------------------------------------------

function addRegistrationMark( inContainerID ) {

	// Create an SVG context
	svgCreateContext( inContainerID );
	svgSetMeasurementSystem( svgMeasurements.mm );

	svgCreateCenteredCircle( 5, 5, 3.5 );
	svgShapeStroke( colorSpecialAll(), 0.25 );

	svgCreateCenteredCircle( 5, 5, 2 );
	svgShapeFill( colorSpecialAll() );

	svgPathCreate();
	svgPathMoveTo( 2, 5, false );
	svgPathLineTo( 6, 0, true );
	svgShapeStroke( colorSpecialAll(), 0.25 );

	svgPathCreate();
	svgPathMoveTo( 5, 2, false );
	svgPathLineTo( 0, 6, true );
	svgShapeStroke( colorSpecialAll(), 0.25 );

	svgCloseCurrentContext();
}











