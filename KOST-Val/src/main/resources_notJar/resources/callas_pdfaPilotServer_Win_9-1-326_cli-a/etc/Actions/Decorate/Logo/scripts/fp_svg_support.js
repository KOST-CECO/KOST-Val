//-------------------------------------------------------------------------------------------------
// Support functions for the use of SVG using jsSVG
//
// Version: 1.0
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2016 - Four Pees
// Author: David van Driessche
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// SVG document support
//-------------------------------------------------------------------------------------------------

// The current SVG context
var sCurrentSvgContext = null;

// Creates a new SVG context
// ElementID: The ID of the element in which this context has to be created. By default, the context
//            is created full size in this element.
// Returns: the SVG context. This SVG context is also stored in sCurrentSvgContext
//
function svgCreateContext( inElementID ) {

	sCurrentSvgContext = SVG( inElementID );
	return sCurrentSvgContext;
}

// Nullifies (closes) the current SVG context
//
function svgCloseCurrentContext() {
	sCurrentSvgContext = null;
}



//-------------------------------------------------------------------------------------------------
// Measurement support
//-------------------------------------------------------------------------------------------------

// The current measurement system. This starts with assuming points
var sCurrentMeasurementFactor = 1;

// The supported measurement systems
var svgMeasurements = {

	point: 			{ value: 1, factor: 1 },
	pt: 			{ value: 1, factor: 96.0 / 72.0 },
	mm: 			{ value: 1, factor: 96.0 / 25.4 },
	cm: 			{ value: 1, factor: 96.0 / 2.54 },
	inch: 			{ value: 1, factor: 96.0 }
};
Object.freeze(anchorPoints);

// Given a measurement unit, sets the current measurement correction factor
function svgSetMeasurementSystem( inMeasurementSystem ) {

	sCurrentMeasurementFactor = inMeasurementSystem.factor;
}

// Correct an incoming value to take the measurement system into account
function svgCM( inValue ) {
	return inValue * sCurrentMeasurementFactor;
}



//-------------------------------------------------------------------------------------------------
// Shape support
//-------------------------------------------------------------------------------------------------

// Each of the shape creation methods sets the "current shape" which can be used to set parameters
// of that object

// The current SVG shape
var sCurrentSvgShape = null;

// Sets an incoming shape as current shape and returns it
//
function svgSetCurrentShape( inShape ) {

	sCurrentSvgShape = inShape;
	return sCurrentSvgShape;
}

// Gets the current path
//
function svgGetCurrentShape() {

	return sCurrentSvgShape;
}

// Creates a line of given dimensions at a given location
// inX1 : the X position in points of the first point
// inY1: the Y position in points of the first point
// inX2 : the X position in points of the second point
// inY2: the Y position in points of the second point
// Returns: a reference to the newly created rectangle
//
function svgCreateLine( inX1, inY1, inX2, inY2 ) {

	sCurrentSvgShape = sCurrentSvgContext.line( svgCM(inX1), svgCM(inY1), svgCM(inX2), svgCM(inY2) );
	return sCurrentSvgShape;
}

// Creates a rectangle of given dimensions at a given location, either by defining the top left 
// corner or by defining the center
// inX : the X position in points
// inY: the Y position in points
// inWidth: width in points
// inHeight:  height in points
// Returns: a reference to the newly created rectangle
//
function svgCreateRectangle( inX, inY, inWidth, inHeight ) {

	sCurrentSvgShape = sCurrentSvgContext.rect( svgCM(inWidth), svgCM(inHeight) ).move( svgCM(inX), svgCM(inY) );
	return sCurrentSvgShape;
}

function svgCreateCenteredRectangle( inX, inY, inWidth, inHeight ) {

	sCurrentSvgShape = sCurrentSvgContext.rect( svgCM(inWidth), svgCM(inHeight) ).move( svgCM(inX - inWidth / 2), svgCM(inY - inHeight / 2) );
	return sCurrentSvgShape;
}

// Creates a cirle with the given radius at a given location, either by defining the top left
// corner or by defining the center
// inX : the X position in points
// inY: the Y position in points
// inRadius: radius in points
// Returns: a reference to the newly created circle
//
function svgCreateCircle( inX, inY, inRadius ) {

	sCurrentSvgShape = sCurrentSvgContext.circle( svgCM(inRadius) ).move( svgCM(inX), svgCM(inY) );
	return sCurrentSvgShape;
}

function svgCreateCenteredCircle( inX, inY, inRadius ) {

	sCurrentSvgShape = sCurrentSvgContext.circle( svgCM(inRadius) ).move( svgCM(inX - inRadius / 2), svgCM(inY - inRadius / 2) );
	return sCurrentSvgShape;
}



//-------------------------------------------------------------------------------------------------
// Path support
//-------------------------------------------------------------------------------------------------

// Creates a new path
//
function svgPathCreate() {

	sCurrentSvgShape = sCurrentSvgContext.path('');
	return sCurrentSvgShape;
}

// Moves the current point
//
function svgPathMoveTo( inX, inY, inRelative ) {

	var theCurrentPathString = sCurrentSvgShape.attr('d');
	sCurrentSvgShape.attr('d', theCurrentPathString + ' ' + (inRelative ? 'm ' : 'M ') + svgCM(inX) + ' '  + svgCM(inY) );
}

// Adds a straight segment
//
function svgPathLineTo( inX, inY, inRelative ) {

	var theCurrentPathString = sCurrentSvgShape.attr('d');
	sCurrentSvgShape.attr('d', theCurrentPathString + ' ' + (inRelative ? 'l ' : 'L ') + svgCM(inX) + ' '  + svgCM(inY) );
}

// Closes the current path
//
function svgPathClose() {

	var theCurrentPathString = sCurrentSvgShape.attr('d');
	sCurrentSvgShape.attr('d', theCurrentPathString + ' Z' );
}



//-------------------------------------------------------------------------------------------------
// Graphic state support
//-------------------------------------------------------------------------------------------------

// Sets a shape to "fill" mode with the given color. This turns off stroke
//
function svgShapeFill( inFillColor ) {

	sCurrentSvgShape.attr({
		'fill': inFillColor,
		'stroke': 'none' });
}

// Sets a shape to "stroke" mode with the given color and stroke width. This turns off fill
//
function svgShapeStroke( inStrokeColor, inStrokeWidth ) {

	sCurrentSvgShape.attr({
		'fill': 'none',
		'stroke': inStrokeColor,
		'stroke-width': svgCM(inStrokeWidth) });
}

// Sets a shape to "fill+stroke" mode with the given colors and stroke width.
//
function svgShapeFillStroke( inFillColor, inStrokeColor, inStrokeWidth ) {

	sCurrentSvgShape.attr({
		'fill': inFillColor,
		'stroke': inStrokeColor,
		'stroke-width': svgCM(inStrokeWidth) });
}

// Sets the linecap the current path
//
function svgShapeLinecap( inLinecap ) {

	sCurrentSvgShape.attr({
		'stroke-linecap': inLinecap });
}































