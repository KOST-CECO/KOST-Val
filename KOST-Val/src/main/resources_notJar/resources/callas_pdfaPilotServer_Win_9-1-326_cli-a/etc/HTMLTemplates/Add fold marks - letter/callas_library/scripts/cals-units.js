//-------------------------------------------------------------------------------------------------
// Support functions to work with units
//
// Dependencies: none
//-------------------------------------------------------------------------------------------------
// Author: David van Driessche
// Copyright © 2017 - Four Pees
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Unit definitions
//-------------------------------------------------------------------------------------------------
// This Javascript object defines the different units that can be used. Each unit defines what the
// conversion rate is from the given measurement into points and pixels. You should only use the
// name of the units in the functions defined in this file (as in the examples below)
//
// Example use: unitUnits.mm
var unitUnits = {

    point:          { value: 1, ptFactor: 1,            pxFactor: 1.3333333333333 },
    mm:             { value: 2, ptFactor: 2.83465,      pxFactor: 3.7795275590551 },
    cm:             { value: 3, ptFactor: 28.3465,      pxFactor: 37.795275590551 },
    m:              { value: 4, ptFactor: 2834.65,      pxFactor: 3779.5275590551 },
    inch:           { value: 5, ptFactor: 72,           pxFactor: 96 },
    foot:           { value: 6, ptFactor: 864,          pxFactor: 1152 },
    px:             { value: 7, ptFactor: 0.75,         pxFactor: 1 }
};
Object.freeze(unitUnits);


//-------------------------------------------------------------------------------------------------
// Converting to and from units
//-------------------------------------------------------------------------------------------------

// Converts the incoming value (in the given unit) to points
// Example: var thePoints = unitConvertToPoints( 256.3, unitUnits.mm )
//
function unitConvertToPoints( inValue, inUnit ) {

    return inValue * inUnit.ptFactor;
}

// Converts the incoming value in points to the given unit
// Example: var theValueInInch = unitConvertFromPoints( 72, unitUnits.inch )
//
function unitConvertFromPoints( inValue, inUnit ) {

    return inValue / inUnit.ptFactor;
}

// Converts the incoming value (in the given unit) to pixels
// Example: var thePixels = unitConvertToPixels( 256.3, unitUnits.mm )
//
function unitConvertToPixels( inValue, inUnit ) {

    return inValue * inUnit.pxFactor;
}

// Converts the incoming value in pixels to the given unit
// Example: var thePoints = unitConvertFromPixels( 200, unitUnits.point )
//
function unitConvertFromPixels( inValue, inUnit ) {

    return inValue / inUnit.pxFactor;
}

// Convenience routine to convert points to pixels
// Example: var thePixels = unitPoints2Pixels( 200 )
//
function unitPoints2Pixels( inPoints ) {

    return unitConvertToPixels( inPoints, unitUnits.point );
}

// Convenience routine to convert pixels to points
// Example: var thePoints = unitPixels2Point( 200 )
//
function unitPixels2Point( inPixels ) {

    return unitConvertToPoints( inPixels, unitUnits.px );
}

// Convenience routine to convert one unit into another unit
// Example: var theMeter = unitConvertFromTo( 700, unitUnits.cm, unitUnits.m )
//
function unitConvertToFrom( inValue, inFromUnit, inToUnit ) {

    var thePoints = unitConvertToPoints( inValue, inFromUnit );
    return unitConvertFromPoints( thePoints, inToUnit );
}



//-------------------------------------------------------------------------------------------------
// Working with strings
//-------------------------------------------------------------------------------------------------

// Converts the given value and measurement unit into a nicely formatted string
// Example: var theString = unitFormatWithUnit( 100, unitUnits.px );
//
function unitFormatWithUnit( inValue, inUnit, inNumDigits ) {
    
    switch( inUnit ) {
            
        case unitUnits.point: {
            return inValue.toFixed( inNumDigits ) + " pt";
        }   
            
        case unitUnits.px: {
            return inValue.toFixed( inNumDigits ) + " px";
        }   
            
        case unitUnits.mm: {
            return inValue.toFixed( inNumDigits ) + " mm";
        }   
            
        case unitUnits.cm: {
            return inValue.toFixed( inNumDigits ) + " cm";
        }   
            
        case unitUnits.m: {
            return inValue.toFixed( inNumDigits ) + " m";
        }   
            
        case unitUnits.inch: {
            var theInch = inValue % 12;
            var theFeet = inValue / 12;
            return theFeet.toFixed(0) + "' " + theInch.toFixed( inNumDigits ) + "\"";
        }   
        
        case unitUnits.foot: {
            return inValue.toFixed( inNumDigits ) + "'";
        }   
            
        default: {
            return inValue.toFixed( inNumDigits ) + " penguins";
        }       
    }
}

// Converts the given string into a unit value
// Example: var theUnit = unitConvertStringToUnit( "mm" );
//
function unitConvertStringToUnit( inUnitString ) {

    switch( inUnitString ) {
            
        case "pt": {
            return unitUnits.point;
        }   
      
        case "px": {
            return unitUnits.px;
        }   
            
        case "mm": {
            return unitUnits.mm;
        }   
            
        case "cm": {
            return unitUnits.cm;
        }   
            
        case "m": {
            return unitUnits.m;
        }   
            
        case "in": {
            return unitUnits.inch;
        }   
        
        case "ft": {
            return unitUnits.foot;
        }   

        default: {
            return unitUnits.point;
        }
    }
}

















