//-----------------------------------------------------------------------
// Script for tiling template
//
//---------
// Copyright: © 2016 - David van Driessche, Four Pees
//-----------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Global variables
//-------------------------------------------------------------------------------------------------

var globalMeasurementUnit;
var globalCount;
var globalCountHorizontal;
var globalCountVertical;
var globalSizeHorizontal;
var globalSizeVertical;
var globalTrimboxWidth;
var globalTrimboxHeight;
var globalOverlapHorizontal;
var globalOverlapVertical;
var globalLeftToRight;
var globalTopToBottom;


//-------------------------------------------------------------------------------------------------
// Print Loop
//-------------------------------------------------------------------------------------------------

// Function called when we're done with the printing stuff and the images are ready as well
//
function imagesLoaded() {

    // *** Remember incoming variables
    globalMeasurementUnit = cchip.user.config.measurement_unit;
    globalOverlapHorizontal = convertToPoints( cchip.user.config.overlap_horizontal, globalMeasurementUnit );
    globalOverlapVertical = convertToPoints( cchip.user.config.overlap_vertical, globalMeasurementUnit );
    globalLeftToRight = cchip.user.config.left_to_right;
    globalTopToBottom = cchip.user.config.top_to_bottom;
    var theSizeHorizontal = convertToPoints( cchip.user.config.size_horizontal, globalMeasurementUnit );
    var theSizeVertical = convertToPoints( cchip.user.config.size_vertical, globalMeasurementUnit );

    // *** Do calculations to prepare tile creation

    // Get the trim box size of our given PDF file
    var theTemplateImage = $( '#template_image' );
    var theTrimBoxDimensions = cchip.getPDFPageBox( theTemplateImage[0], "trim");
    globalTrimboxWidth = theTrimBoxDimensions.width;
    globalTrimboxHeight = theTrimBoxDimensions.height;

    // Proceed depending on whether or not the requested number or size is given
    if ((cchip.user.config.count_horizontal < 0) || (cchip.user.config.count_vertical < 0)) {

        // Size is given, calculate the number of tiles, but don't touch the size
        globalCountHorizontal = Math.ceil( (globalTrimboxWidth / theSizeHorizontal) );
        globalCountVertical = Math.ceil( (globalTrimboxHeight / theSizeVertical) );
        globalSizeHorizontal = theSizeHorizontal;
        globalSizeVertical = theSizeVertical;
    } else {

        // Number is given, calculate the size
        globalCountHorizontal = (cchip.user.config.count_horizontal < 1) ? 1 : cchip.user.config.count_horizontal;
        globalCountVertical = (cchip.user.config.count_vertical < 1) ? 1 : cchip.user.config.count_vertical;
        globalSizeHorizontal = globalTrimboxWidth / globalCountHorizontal;
        globalSizeVertical = globalTrimboxHeight / globalCountVertical;
    } 

    // Remember the total amount of tiles
    globalCount = globalCountHorizontal * globalCountVertical;

    // *** If we need to make a construction page, do so
    if (cchip.user.config.construction_information) {

        addConstructionInformation( $( '#construction_information' ) );

        // Generate the construction page
        $( '#template_image' ).hide();
        cchip.printPages();
   }
   $( '#template_image' ).hide();
   $( '#construction_information' ).remove();

    // *** Now generate tiles
    generateTiles();

    // Done!
    cchip.endPrinting();
}

// Print loop
//
function cchipPrintLoop() {

    // Tell pdfChip we're getting ready and don't want to be interrupted too soon
    cchip.beginPrinting();

    // Put an image template on the page, we're loading it here once but we'll hide it later. This 
    // is mainly so we can allow it to be loaded and we're sure we have a loaded image to work with
    // later when we have to do calculations.
    createImage( 'template_image' ).appendTo( 'body' );

    // As we changed image sources, make sure they are loaded
    $('body').waitForImages( function(){ imagesLoaded(); } );
}



//-------------------------------------------------------------------------------------------------
// Support functions for Construction Information sheet
//-------------------------------------------------------------------------------------------------

// Adds all construction information
function addConstructionInformation( inContainer ) {

    // Details for the sheet we're creating
    addConstructionInformationDetail( inContainer, "File name", 
                                      getFileNameFromPath( cchip.user.config.pdf_path ) );
    addConstructionInformationDetail( inContainer, "Number of tiles", 
                                      globalCount + " (" + globalCountHorizontal + " x " + globalCountVertical + ")" );

    // Add a preview of the final image
    var thePreviewContainer = $( '<div>' ).appendTo( inContainer ).addClass( 'ci_preview' );
    $( '<h2>' ).appendTo( thePreviewContainer ).text( "Preview" );
    createImage( 'ci_preview_image' ).appendTo( thePreviewContainer );

    // Add a tile layout
    var theTileContainer = $( '<div>' ).appendTo( inContainer ).addClass( 'ci_tiles' );
    $( '<h2>' ).appendTo( theTileContainer ).text( "Tile layout" );
    var theTiles = $( '<div>' ).appendTo( theTileContainer ).addClass( 'ci_tiles_container' );
    var theTilesBackground = createImage( 'ci_tiles_image' ).appendTo( theTiles );

    // Find out the width of the image we just added
    var theTilesWidth = theTilesBackground.width();

    // Run over all tiles sequentially and add them
    for (var theTileSequenceNumber = 1; theTileSequenceNumber <= globalCount; theTileSequenceNumber++ ) {

        // Create the tile
        var theTile = $( '<div>' ).appendTo( theTiles ).addClass( 'ci_tile_mockup' );

        // Calculate tile information, scaled down to the size of the image
        var theTileInfo = calculateTileInformation( calculateTileColumn(theTileSequenceNumber), calculateTileRow(theTileSequenceNumber), theTilesWidth );

        // Set its width and height
        theTile.width( theTileInfo[2]-2 );
        theTile.height( theTileInfo[3]-2 );
        theTile.css( "left", theTileInfo[7] );
        theTile.css( "top", theTileInfo[8] );

        // Add the tile number
        $( '<p>' ).appendTo( theTile ).text( theTileInfo[4] ).css({'line-height': (theTileInfo[3]) + 'px' });
    }
}

// Write one line with a key value pair for construction information
//
function addConstructionInformationDetail( inContainer, inKey, inValue ) {

    // Add a container for the line
    var theDetailContainer = $('<div>').appendTo( inContainer ).addClass( 'ci_detail' );
    $('<p>').appendTo( theDetailContainer ).addClass( 'ci_detail_key' ).text( inKey );
    $('<p>').appendTo( theDetailContainer ).addClass( 'ci_detail_value' ).text( inValue );
}



//-------------------------------------------------------------------------------------------------
// Support functions for tile creation
//-------------------------------------------------------------------------------------------------

// Generates PDF for all tiles as necessary
//
function generateTiles() {

    for (var theTileSequenceNumber = 1; theTileSequenceNumber <= globalCount; theTileSequenceNumber++ ) {

        // Generate a container element for the tile
        var theTileContainer = $('<div>').attr({ id:'tile_container' }).appendTo( 'body' );

        // Add all tile information
        generateTile( theTileContainer, theTileSequenceNumber );

        // Generate the page for the tile
        // console.log($( "html" ).html());
        cchip.printPages( 1 );

        // And clean up
        theTileContainer.remove();
    }
}

// Generate HTML as necessary for one particular tile
//
function generateTile( inContainer, inTileSequenceNumber ) {

    // Get tile information
    var theTileInfo = calculateTileInformation( calculateTileColumn(inTileSequenceNumber), calculateTileRow(inTileSequenceNumber), -1 );

    // Each tile will have the tile itself and then spacing around it as governed by these setup variables
    var theMarginBetweenTileAndMarks = convertToPoints( 5, "mm" );
    var theMarginMarkSize = convertToPoints( 5, "mm" );
    var theMarginSlug = convertToPoints( 20, "mm" );

    // Calculate the total page size for this tile (tile size + margins)
    var theMargin = theMarginBetweenTileAndMarks + theMarginMarkSize + theMarginSlug;
    var thePageWidth = theTileInfo[9] + theMargin * 2;
    var thePageHeight = theTileInfo[10] + theMargin * 2;

    // Set our HTML document to this size
    adjustDocumentSizeToSizeInPointsWithTrimbox( thePageWidth, thePageHeight, theMargin, theMargin,
                                                 theTileInfo[9], theTileInfo[10] );
    inContainer.height( thePageHeight + "pt" );

    // Create an image element for the part of the image we need
    var theTileImage = createPartialImage( "tile_image", "trim", -theTileInfo[11], -theTileInfo[12], 
                                           (theTileInfo[11] + theTileInfo[9]) - globalTrimboxWidth, 
                                           (theTileInfo[12] + theTileInfo[10]) - globalTrimboxHeight );
    theTileImage.appendTo( inContainer );    

    // Add trim marks
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_horizontal' ).css({ 'left':'20mm', 'top':'30mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_horizontal' ).css({ 'left':'20mm', 'bottom':'30mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_horizontal' ).css({ 'right':'20mm', 'top':'30mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_horizontal' ).css({ 'right':'20mm', 'bottom':'30mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_vertical' ).css({ 'left':'30mm', 'top':'20mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_vertical' ).css({ 'left':'30mm', 'bottom':'20mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_vertical' ).css({ 'right':'30mm', 'top':'20mm' });
    $( '<div>' ).appendTo( inContainer ).addClass( 'trim_mark_vertical' ).css({ 'right':'30mm', 'bottom':'20mm' });

    // If there are overlays, add overlay marks
    if (theTileInfo[9] > theTileInfo[2]) {
        // Glue marks
        var theMarkPosition = globalLeftToRight ? Math.round((30 * 2.83465) + theTileInfo[2])
                                             : Math.round((30 * 2.83465) + globalOverlapHorizontal);
        $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_mark_vertical' ).css({ 'left':theMarkPosition + 'pt', 'top':'25mm' });
        $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_mark_vertical' ).css({ 'left':theMarkPosition + 'pt', 'bottom':'25mm' });

        // Add a sequence number div
        var theSequencePosition = globalLeftToRight ? (theMarkPosition + 20)
                                                 : (theMarkPosition - (globalOverlapHorizontal - 20 ));
        var theSequenceDiv = $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_sequence_number' ).css({ 'left':theSequencePosition + 'pt', 'top':'50mm' });
        $( '<p>' ).appendTo( theSequenceDiv ).text( theTileInfo[4] );

    }
    if (theTileInfo[10] > theTileInfo[3]) {

        // Glue marks
        var theMarkPosition = globalTopToBottom ? Math.round((30 * 2.83465) + theTileInfo[3])
                                             : Math.round((30 * 2.83465) + globalOverlapVertical);
        $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_mark_horizontal' ).css({ 'top':theMarkPosition + 'pt', 'left':'25mm' });
        $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_mark_horizontal' ).css({ 'top':theMarkPosition + 'pt', 'right':'25mm' });

        // Add a sequence number div
        var theSequencePosition = globalTopToBottom ? (theMarkPosition + 20)
                                                 : (theMarkPosition - (globalOverlapVertical + -20));
        var theSequenceDiv = $( '<div>' ).appendTo( inContainer ).addClass( 'overlay_sequence_number' ).css({ 'top':theSequencePosition + 'pt', 'left':'50mm' });
        $( '<p>' ).appendTo( theSequenceDiv ).text( theTileInfo[4] );
    }

    // Add gutter information for identification purposes after printing
    var theGutterContainer = $( '<div>' ).appendTo( inContainer ).addClass( 'gutter_information' ).css({ 'bottom':'10mm', 'left':'50mm' });
    $( '<p>' ).appendTo( theGutterContainer ).text( "File name: " + getFileNameFromPath( cchip.user.config.pdf_path ) );
    $( '<p>' ).appendTo( theGutterContainer ).text( "Number of tiles: " + globalCount + " (" + globalCountHorizontal + " x " + globalCountVertical + ")" );
    $( '<p>' ).appendTo( theGutterContainer ).text( "This tile: " + theTileInfo[4] );
    $( '<p>' ).appendTo( theGutterContainer ).text( "Tile size: " + formatMeasurement( theTileInfo[2], "pt", globalMeasurementUnit, 0 ) + " x " +
                                                                    formatMeasurement( theTileInfo[3], "pt", globalMeasurementUnit, 0 ) );
}



//-------------------------------------------------------------------------------------------------
// Utility functions
//-------------------------------------------------------------------------------------------------


// Gets the filename (including extension) from a full path
//
function getFileNameFromPath( inPath ) {
    return inPath.replace(/^.*[\\\/]/, '');
}

// Get the URL to include our PDF file to tile
//
function getImageURL() {
    var pathName = cchip.user.config.pdf_path.replace(/\\/g,"/");
    if (pathName[0] !== '/') { // Windows drive letter must be prefixed with a slash
        pathName = '/' + pathName;
    }
    return encodeURI('file://' + pathName);
}

// Create an image from our passed variable
//
function createImage( inID ) {
    return $('<img>').attr({ src:getImageURL(), id:inID });
}

// Create a partial image
//
function createPartialImage( inID, inBoxName, inLeftMargin, inTopMargin, inRightMargin, inBottomMargin ) {
    var theImageURL = getImageURL() + "#box=" + inBoxName + "&boxadj=" + inLeftMargin + "," + inTopMargin + "," + inRightMargin + "," + inBottomMargin;
    return $('<img>').attr({ src:theImageURL, id:inID });
}

// Calculates all information about a given tile, given the global tile information
// and the information about the PDF file we're tiling. If you want to get the information
// "scaled" down, specify a reference width. If not, specify -1.  Information returned
// in the return array:
//  0: column
//  1: row
//  2: horizontal size of the tile (without overlap) (scaled)
//  3: vertical size of the tile (without overlap) (scaled)
//  4: the tile sequence number 
//  5: last in row (horizontally) (0 or 1)
//  6: last in column (vertically) (0 or 1)
//  7: horizontal position of the tile in the larger image (without overlap) (scaled)
//  8: vertical position of the tile in the larger image (without overlap) (scaled)
//  9: horizontal size of the tile (with overlap) (scaled)
// 10: vertical size of the tile (with overlap) (scaled)
// 11: horizontal position of the tile in the larger image (with overlap) (scaled)
// 12: vertical position of the tile in the larger image (with overlap) (scaled)
//
function calculateTileInformation( inTileColumn, inTileRow, inReferenceWidth ) {

    // Start filling up our result information with the easy stuff
    var theResult = [];
    theResult[0] = inTileColumn;
    theResult[1] = inTileRow;

    // Last in its column or row?
    theResult[5] = globalLeftToRight ? ((inTileColumn+1) % globalCountHorizontal) == 0 
                                  : ((globalCountHorizontal-inTileColumn) % globalCountHorizontal) == 0;
    theResult[6] = globalTopToBottom ? ((inTileRow+1) % globalCountVertical) == 0 
                                  : ((globalCountVertical-inTileRow) % globalCountVertical) == 0;

    // Tile width and height are typically not hard, but the last tile in a row / column
    // can be partial and we need to take that into account
    theResult[2] = (theResult[5] == false) ? globalSizeHorizontal
                                           : globalTrimboxWidth - (globalSizeHorizontal * (globalCountHorizontal-1));
    theResult[3] = (theResult[6] == false) ? globalSizeVertical
                                           : globalTrimboxHeight - (globalSizeVertical * (globalCountVertical-1));

    // Calculate the tile sequence number based on how the tiles are laid out
    var theTileRowBegin = (globalTopToBottom) ? (inTileRow * globalCountHorizontal) + 1
                                           : ((globalCountVertical - inTileRow - 1) * globalCountHorizontal) + 1; 
    theResult[4] = (globalLeftToRight) ? (theTileRowBegin + inTileColumn)
                                    : (theTileRowBegin + (globalCountHorizontal - inTileColumn - 1));

    // Calculate the tile position from the left and top. This is more difficult as it requires keeping into
    // account that there may be a partial tile somewhere (and it's not always at the right bottom, it depends
    // on glueing order)
    theResult[7] = globalLeftToRight ? (inTileColumn * globalSizeHorizontal)
                                  : ((inTileColumn == 0) ? 0 : (globalTrimboxWidth - ((globalCountHorizontal-inTileColumn)*globalSizeHorizontal)));
    theResult[8] = globalTopToBottom ? (inTileRow * globalSizeVertical)
                                  : ((inTileRow == 0) ? 0 : (globalTrimboxHeight - ((globalCountVertical-inTileRow)*globalSizeVertical)));

    // If there is overlap, the size and positioning of the tiles may change. This has to take into account that the
    // last tile in the column / row is different (doesn't feature overlap) 
    theResult[9] = (theResult[5] == false) ? (theResult[2] + globalOverlapHorizontal)
                                           : theResult[2];
    theResult[10] = (theResult[6] == false) ? (theResult[3] + globalOverlapVertical)
                                            : theResult[3];
    theResult[11] = (globalLeftToRight || theResult[5]) ? theResult[7]
                                                     : theResult[7] - globalOverlapHorizontal;
    theResult[12] = (globalTopToBottom || theResult[6]) ? theResult[8]
                                                     : theResult[8] - globalOverlapVertical;

    // If requested, scale down all relevant information in our result
    if (inReferenceWidth > 0) {
        var theRatio = inReferenceWidth / globalTrimboxWidth;
        theResult[2] = theResult[2] * theRatio;
        theResult[3] = theResult[3] * theRatio;
        theResult[7] = theResult[7] * theRatio;
        theResult[8] = theResult[8] * theRatio;
        theResult[9] = theResult[9] * theRatio;
        theResult[10] = theResult[10] * theRatio;
        theResult[11] = theResult[11] * theRatio;
        theResult[12] = theResult[12] * theRatio;
    }

    // Return our result to the caller
    return theResult;
}

// From a tile sequence number, get the column (sequence number is 1 based, column 0 based)
//
function calculateTileColumn( inSequenceNumber ) {

    var theModulus = (inSequenceNumber-1) % globalCountHorizontal;
    return (globalLeftToRight ? theModulus : (globalCountHorizontal-theModulus-1));
}

// From a tile sequence number, get the row (sequence number is 1 based, row 0 based)
//
function calculateTileRow( inSequenceNumber ) {

    var theRow = Math.floor( (inSequenceNumber-1) / globalCountHorizontal );
    return (globalTopToBottom ? theRow : (globalCountVertical-theRow-1))
}

