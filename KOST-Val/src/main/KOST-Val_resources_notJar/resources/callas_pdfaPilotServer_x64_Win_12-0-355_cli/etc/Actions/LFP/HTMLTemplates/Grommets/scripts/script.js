//-------------------------------------------------------------------------------------------------
// Script content for the "LFP - Place Grommets" fixup
//
//-------------------------------------------------------------------------------------------------
// Copyright © 2022 - Four Pees
// Author: David van Driessche
// Last change: 2022-07-02
//-------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------
// Print loop
//-------------------------------------------------------------------------------------------------

// Function called when we're done with the printing stuff and the images are ready as well
//
function imagesLoaded() {
  // Read all variables coming from pdfToolbox
  var theMeasurementUnit = unitConvertStringToUnit(pcGetVariableValue('measurement_unit')); // "pt", "mm", "cm", "in"
  var theDistanceTop = unitConvertToPoints(pcGetVariableValue('distance_top'), theMeasurementUnit);
  var theDistanceBottom = unitConvertToPoints(pcGetVariableValue('distance_bottom'), theMeasurementUnit);
  var theDistanceLeft = unitConvertToPoints(pcGetVariableValue('distance_left'), theMeasurementUnit);
  var theDistanceRight = unitConvertToPoints(pcGetVariableValue('distance_right'), theMeasurementUnit);
  var theCountHorizontal = pcGetVariableValue('count_horizontal'); // -1 == not used, 0 == none
  var theCountVertical = pcGetVariableValue('count_vertical'); // -1 == not used, 0 == none
  var theSpacingHorizontal = unitConvertToPoints(pcGetVariableValue('spacing_horizontal'), theMeasurementUnit); // -1 == not used, 0 == none
  var theSpacingVertical = unitConvertToPoints(pcGetVariableValue('spacing_vertical'), theMeasurementUnit); // -1 == not used, 0 == none

  // Loop over all pages
  for (var thePageIndex = 0; thePageIndex < pcGetNumberOfPages(); thePageIndex++) {
    // Make sure we're working 1 on 1
    pcAdjustDocumentSizeToMediabox(thePageIndex);

    // Start by assuming that it's the count that is used.
    var theNumberHorizontal = theCountHorizontal;
    var theNumberVertical = theCountVertical;
    var theCalculateBySpacing = theNumberVertical < 0 || theNumberHorizontal < 0;

    var theMediabox = pcGetPagebox(thePageIndex, pcPagebox.mediabox);
    var theMediaboxInfo = pcGetPageboxInfo(thePageIndex, pcPagebox.mediabox);

    var theUserUnit = pcGetUserUnits(thePageIndex);
    var theScalingForGrommet = 1 / theUserUnit;

    // If we don't have exact numbers of grommets, calculate how many we need
    if (theCalculateBySpacing) {
      // First calculate the distance we need to work with on this page, taking the user units
      // into account
      var theRealSpacingHorizontal = theSpacingHorizontal;
      var theRealSpacingVertical = theSpacingVertical;

      var thePageBoxInfo = pcGetPageboxInfo(thePageIndex, pcPagebox.trimbox);
      var theTrimboxWidth = thePageBoxInfo[4];
      var theTrimboxHeight = thePageBoxInfo[5];
      if (theNumberHorizontal < 0) theNumberHorizontal = theRealSpacingHorizontal < 1 ? 0 : Math.round(theTrimboxWidth / theRealSpacingHorizontal);
      if (theNumberVertical < 0) theNumberVertical = theRealSpacingVertical < 1 ? 0 : Math.round(theTrimboxHeight / theRealSpacingVertical);
    }

    // Remove anything with class grommet
    $('.grommet').remove();

    // Show the template grommet (for when we begin cloning)
    $('#template_grommet').show();

    // Add the correct amount of grommets
    addGrommets(
      $('#template_grommet'),
      theNumberHorizontal,
      theNumberVertical,
      thePageIndex,
      theDistanceTop,
      theDistanceBottom,
      theDistanceLeft,
      theDistanceRight,
      theScalingForGrommet
    );

    // Hide the template grommet for output
    $('#template_grommet').hide();

    // And output (always 1 page)
    cchip.printPages(1);
  }

  // Let pdfChip know we're done
  cchip.endPrinting();
}

function cchipPrintLoop() {
  // Tell pdfChip we're getting ready and don't want to be interrupted too soon
  cchip.beginPrinting();

  // Replace the source for our grommet template
  var theTemplateGrommet = $('#template_grommet');
  if (theTemplateGrommet) {
    var theTemplateGrommetPath = pcGetVariableValue('grommet_template').replace(/\\/g, '/'); // Path to grommet file to place
    if (theTemplateGrommetPath[0] !== '/') {
      // Windows drive letter must be prefixed with a slash
      theTemplateGrommetPath = '/' + theTemplateGrommetPath;
    }
    theTemplateGrommetPath = encodeURI('file://' + theTemplateGrommetPath)
      .replace(/\#/g, '%23')
      .replace(/\?/g, '%3F');
    theTemplateGrommet.attr('src', theTemplateGrommetPath);
  }
  // As we changed the image for the grommet, wait until it's loaded
  $('body').waitForImages(function () {
    imagesLoaded();
  });
}

// This function adds the necessary amount of grommets
//
function addGrommets(
  inTemplateGrommet,
  inNumberHorizontal,
  inNumberVertical,
  inPageIndex,
  theDistanceTop,
  theDistanceBottom,
  theDistanceLeft,
  theDistanceRight,
  inScaling
) {
  // We can deal with a number of grommets that is 0 (or none) but we can't deal with 1 or less than 0
  if (inNumberHorizontal == 1 || inNumberHorizontal < 0) inNumberHorizontal = 2;
  if (inNumberVertical == 1 || inNumberVertical < 0) inNumberVertical = 2;

  // Calculate the width and height we have (trimbox - the margins
  var thePageBoxInfo = pcGetPageboxInfo(inPageIndex, pcPagebox.trimbox);
  var theTrimboxWidth = thePageBoxInfo[4];
  var theTrimboxHeight = thePageBoxInfo[5];
  var theMarginsHorizontal = theDistanceLeft + theDistanceRight;
  var theMarginsVertical = theDistanceTop + theDistanceBottom;
  var theTrimboxWidthMinusMargins = theTrimboxWidth - theMarginsHorizontal;
  var theTrimboxHeightMinusMargins = theTrimboxHeight - theMarginsVertical;

  // Calculate how much spacing we need between grommets
  var theDeltaX = theTrimboxWidthMinusMargins / (inNumberHorizontal - 1);
  var theDeltaY = theTrimboxHeightMinusMargins / (inNumberVertical - 1);

  // An index for the grommet we're placing
  var theGrommetIndex = 1;

  // Add grommets vertically - we need to add "inNumberVertical"
  for (var theIndexY = 0; theIndexY < inNumberVertical; theIndexY++) {
    // Add a grommet vertically along the left border
    addGrommet(
      inTemplateGrommet,
      'grommet_' + theGrommetIndex,
      pcAnchorPoints.centerMiddle,
      inPageIndex,
      pcPagebox.trimbox,
      pcAnchorPoints.leftTop,
      theDistanceLeft,
      theDistanceTop + theIndexY * theDeltaY,
      inScaling
    );
    theGrommetIndex += 1;

    // Add a grommet vertically along the right border
    addGrommet(
      inTemplateGrommet,
      'grommet_' + theGrommetIndex,
      pcAnchorPoints.centerMiddle,
      inPageIndex,
      pcPagebox.trimbox,
      pcAnchorPoints.leftTop,
      theTrimboxWidth - theDistanceRight,
      theDistanceTop + theIndexY * theDeltaY,
      inScaling
    );
    theGrommetIndex += 1;
  }

  // Add grommets horizontally - we need to add "inNumberHorizontal - 2". The difference is
  // because we don't want to add grommets to the corners twice so we have to skip those
  for (var theIndexX = 1; theIndexX < inNumberHorizontal - 1; theIndexX++) {
    // Add a grommet vertically along the left border
    addGrommet(
      inTemplateGrommet,
      'grommet_' + theGrommetIndex,
      pcAnchorPoints.centerMiddle,
      inPageIndex,
      pcPagebox.trimbox,
      pcAnchorPoints.leftTop,
      theDistanceLeft + theIndexX * theDeltaX,
      theDistanceTop,
      inScaling
    );
    theGrommetIndex += 1;

    // Add a grommet vertically along the right border
    addGrommet(
      inTemplateGrommet,
      'grommet_' + theGrommetIndex,
      pcAnchorPoints.centerMiddle,
      inPageIndex,
      pcPagebox.trimbox,
      pcAnchorPoints.leftTop,
      theDistanceLeft + theIndexX * theDeltaX,
      theTrimboxHeight - theDistanceBottom,
      inScaling
    );
    theGrommetIndex += 1;
  }
}

// This function creates one specific grommet in one specific location
//
function addGrommet(inTemplateGrommet, inID, inElementAnchor, inPageIndex, inPagebox, inPageboxAnchor, inOffsetX, inOffsetY, inScaling) {
  // Clone the template
  var theGrommet = inTemplateGrommet.clone().attr('class', 'grommet').attr('id', inID);

  // If necessary, scale it
  /*
	if (inScaling != 1) {
		var theScalingString = 'scale(' + inScaling + ')';
		theGrommet.css( 'webkitTransform', theScalingString );
	}
  */
  // Add it to our body element
  theGrommet.appendTo('body');

  // Position it
  pcPositionElement('#' + inID, inElementAnchor, inPageIndex, inPagebox, inPageboxAnchor, inOffsetX, inOffsetY);
}
