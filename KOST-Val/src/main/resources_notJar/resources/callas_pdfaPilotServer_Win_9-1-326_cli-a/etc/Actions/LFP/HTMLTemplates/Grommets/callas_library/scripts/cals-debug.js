//-------------------------------------------------------------------------------------------------
// Support functions for debugging
//
// Dependencies: none
//-------------------------------------------------------------------------------------------------
// Author: David van Driessche
// Copyright: Copyright © 2017 - Four Pees
//-------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------
// Logging
//-------------------------------------------------------------------------------------------------

// Generic logging function that logs correctly whether or not pdfChip technology is used
// Example: debugLog( "Penguins were here..." )
//
function debugLog( inMessage ) {
	console.log( inMessage );
}

// Log a message and corresponding object (which is stringified)
// Example: debugLogObject( "Penguins age is", theAge )
//
function debugLogObject( inMessage, inObject ) {
	console.log( inMessage + ": " + JSON.stringify( inObject ) );
}


