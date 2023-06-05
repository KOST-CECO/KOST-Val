/*
function changeDottedAndDashedStyleBordersToSolid()
{
   var elements = document.getElementsByTagName("*");

   for (var i = 0; i < elements.length; i++)
   {
	   var stylesString = elements[i].style.cssText;
	   var dotted = stylesString.indexOf("dotted");
	   var dashed = stylesString.indexOf("dashed");

	   if (dotted != -1 || dashed != -1)
	   {
		   stylesString = stylesString.replace(/dotted/g,"solid");
		   stylesString = stylesString.replace(/dashed/g,"solid");
		   elements[i].style.cssText = stylesString;
	   }
   }

   var stylesElements = document.getElementsByTagName("style");

   for (var i = 0; i < stylesElements.length; i++)
   {
	   stylesString = stylesElements[i].innerHTML;
	   stylesString = stylesString.replace(/dotted/g,"solid");
	   stylesString = stylesString.replace(/dashed/g,"solid");
	   stylesElements[i].innerHTML = stylesString;
   }
}
*/

function cals_uses_absolut(parent)
{
	var nodes = parent.childNodes;
	for(var i=0; i < nodes.length; i++){
		var node = nodes[i];
		if( node.nodeType == 1 && node.style.position=="absolute" ){
			return true;
		}
	}
	return false;
}

function makeAbsolute(div)
{
	var images = div.getElementsByTagName('img');

	for(var i = 0; i < images .length; i++) 
	{
		var img = images[i];
		var oldOffsetLeft = img.offsetLeft;

		// insert div with same dimensions as image 
		var newDiv = document.createElement("div");
		
		newDiv.style.height = img.clientHeight + "px";
		newDiv.style.width = img.clientWidth + "px";
		newDiv.style.left = img.clientLeft + "px";
		newDiv.style.top = img.clientTop + "px";
		newDiv.style.position = img.style.position;

		img.parentNode.insertBefore(newDiv, img.nextSibling); //insert after

		img.style.position = "absolute";
		img.style.left = oldOffsetLeft + "px";
	}
}

function getWidestImage() {
	var widestImage = 0;
	var allImages = document.images;

	for(var i = 0; i < allImages.length; i++) {
		var image = null;

		image = allImages[i].offsetWidth + allImages[i].offsetLeft;

		if(image > widestImage) {
			widestImage = image;
		}
	}
	return widestImage;
}

function getWidestElement()
{
	var widestObject = 0;
	var allObjects = document.all;
	var indexOfBody = 0;

	for (; indexOfBody < allObjects.length; indexOfBody++) {
		if(allObjects[indexOfBody].previousElementSibling != null) {
			if(allObjects[indexOfBody].previousElementSibling.className == "cchip_headers_container") { break; }
		}
	}

	for (var i = indexOfBody; i < allObjects.length; i++) {
		var object = null;
		if(allObjects[i].nodeName != "SPAN") {
			object = allObjects[i].offsetWidth + allObjects[i].offsetLeft;
		}else {
			object = allObjects[i].offsetWidth;
		}

		if(object > widestObject) {
			widestObject = object;
		}
	}

	return widestObject;
}

function shrinkToFit()
{
	try
	{
		// DIN A4
		var maxWidthOfPage = 210*3.78;
		if(page_size){
			maxWidthOfPage = page_size.offsetWidth;
			cals_debug_add_key_val("maxWidthOfPage = page_size.offsetWidth", page_size.offsetWidth);
		}
		if( document.body.style.width ){
			maxWidthOfPage = document.body.style.width;
			cals_debug_add_key_val("maxWidthOfPage = document.body.style.width", document.body.style.width);
		}
/*
		if( document.body.offsetWidth > 0 ){
			maxWidthOfPage = document.body.offsetWidth;
			cals_debug_add_key_val( "maxWidthOfPage = document.body.offsetWidth", document.body.offsetWidth );
		}
*/	
		document.body.style.padding=0;
		document.body.style.margin=0;

		var w = undefined;

		if(settings.pageScale) {
			w = getWidestElement();
		}else {
			w = getWidestImage();
		}

		cals_debug_add_key_val("maxWidthOfPage", maxWidthOfPage);
		cals_debug_add_key_val("document.body.scrollWidth", w);
	
		//absolute positioned DIVs cover mail headers
		//this can be avoided if identity matrix is applied to parent div
		var fixAbsolut = cals_uses_absolut(document.body);

		if( fixAbsolut || w > maxWidthOfPage)
		{
			var divp = document.createElement("div");
			divp.className = "cchip_body_parent";
			var div = document.createElement("div");
			div.className = "cchip_body";
			divp.appendChild(div);

			var nodes = document.body.childNodes;
			for(var i=0; i < nodes.length; i++){
				var node = nodes[i];
				if( node.className != "cchip_headers_container"
				 && node.className != "cchip_debug"
				 && node.className != "ccmip-text-plain"
				 && node.nodeName != "SCRIPT" ){
					var child = document.body.removeChild(node);
					div.appendChild(child);
					i--;
				}
			}
			if( fixAbsolut || div.childNodes.length > 0 ){
				document.body.appendChild(divp);
				cals_debug_add_key_val( "div.offsetWidth", div.offsetWidth);
				divp.style.maxWidth = maxWidthOfPage + "px";
				divp.style.width = maxWidthOfPage + "px";
				cals_debug_add_key_val( "div.offsetWidth", div.offsetWidth);

				cals_debug_add_key_val( "document.body.scrollWidth (2)", document.body.scrollWidth );
				cals_debug_add_key_val( "divp.scrollWidth", divp.scrollWidth );

				if( fixAbsolut || w > maxWidthOfPage)
				{
//					changeDottedAndDashedStyleBordersToSolid();
				
					var f = maxWidthOfPage/w;
	
					cals_debug_add_key_val( "document.body.offsetWidth", document.body.offsetWidth );
					cals_debug_add_key_val( "div.offsetLeft", div.offsetLeft);
					cals_debug_add_key_val( "div.offsetWidth", div.offsetWidth);
					cals_debug_add_key_val( "div.offsetHeight", div.offsetHeight);
					cals_debug_add_key_val( "div.clientWidth", div.clientWidth);
					cals_debug_add_key_val( "f", f );	

					divp.style.height = f*div.offsetHeight + "px";

					cals_debug_add_key_val( "divp.offsetWidth", divp.offsetWidth);
					cals_debug_add_key_val( "divp.offsetHeight", divp.offsetHeight);

					if(!settings.pageScale) {
						var allImages = document.images;
						for(var i = 0; i < allImages.length; i++) {
							allImages[i].style.width = ((allImages[i].offsetWidth + allImages[i].offsetLeft) * f);
							allImages[i].style.height = "auto";
						}
					}

					makeAbsolute(div);

					if(settings.pageScale) {
						div.style.webkitTransform = "scale("+f+","+f+")";
						div.style.webkitTransformOrigin = "0% 0%";					
					}
				}
			}
		}
		cals_debug_add_key_val( "document.body.scrollWidth", document.body.scrollWidth );
	}
	catch(err)
	{
		cals_debug_add_key_val( "shrinkToFit error: ", err );
	}
}
