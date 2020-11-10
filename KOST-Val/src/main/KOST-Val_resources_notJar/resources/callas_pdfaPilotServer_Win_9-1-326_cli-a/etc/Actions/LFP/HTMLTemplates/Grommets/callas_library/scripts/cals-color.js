//-------------------------------------------------------------------------------------------------
// Support functions to build pdfChip compatible color space definitions using Javascript
//
// Dependencies: none
//-------------------------------------------------------------------------------------------------
// Author: David van Driessche
// Copyright: Copyright © 2017 - Four Pees
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Special colors
//-------------------------------------------------------------------------------------------------

function colorSpecialAll() {
	return '-cchip-cmyk( "All", 1, 1, 1, 1 )';
}

function colorSpecialNone() {
	return '-cchip-cmyk( "None", 0, 0, 0, 0 )';
}

function colorCmykCyan( inTint ) {
	return '-cchip-cmyk( ' + inTint + ', 0, 0, 0 )';
}

function colorCmykMagenta( inTint ) {
	return '-cchip-cmyk( 0, ' + inTint + ', 0, 0 )';
}

function colorCmykYellow( inTint ) {
	return '-cchip-cmyk( 0, 0, ' + inTint + ', 0 )';
}

function colorCmykBlack( inTint ) {
	return '-cchip-cmyk( 0, 0, 0, ' + inTint + ' )';
}

function colorCmykWhite() {
	return colorCmykBlack( 0 );
}


//-------------------------------------------------------------------------------------------------
// CMYK
//-------------------------------------------------------------------------------------------------

function colorCmyk( inCyan, inMagenta, inYellow, inBlack ) {
	return '-cchip-cmyk( ' + inCyan + ', ' + inMagenta + ', ' + inYellow + ', ' + inBlack + ' )';
}

function colorCmykPercentage( inCyan, inMagenta, inYellow, inBlack ) {
	return colorCmyk( inCyan/100, inMagenta/100, inYellow/100, inBlack/100 );
}


//-------------------------------------------------------------------------------------------------
// Spot
//-------------------------------------------------------------------------------------------------

function colorSpotCmyk( inName, inC, inM, inY, inK ) {
	return '-cchip-cmyk( "' + inName + '", ' + inC + ', ' + inM + ', ' + inY + ', ' + inK + ' )';
}

function colorSpotCmykTint( inName, inC, inM, inY, inK, inTint ) {
	return '-cchip-cmyk( "' + inName + '", ' + inC + ', ' + inM + ', ' + inY + ', ' + inK + ', ' + inTint + ' )';
}


//-------------------------------------------------------------------------------------------------
// Gray
//-------------------------------------------------------------------------------------------------

function colorGray( inGray ) {
	return '-cchip-gray( ' + inGray + ' )';
}

function colorGrayPercentage( inGray ) {
	return colorGray( inGray/100 );
}


//-------------------------------------------------------------------------------------------------
// RGB
//-------------------------------------------------------------------------------------------------

function colorRgb( inRed, inGreen, inBlue ) {
	return '-cchip-rgb( ' + inRed + ', ' + inGreen + ', ' + inBlue + ' )';
}

function colorRgb255( inRed, inGreen, inBlue ) {
	return '-cchip-rgb( ' + inRed/255 + ', ' + inGreen/255 + ', ' + inBlue/255 + ' )';
}
































