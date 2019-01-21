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

function shrinkToFit()
{
	try
	{
		//DIN A4 
		var maxWidthOfPage = 210*3.78;
		if(page_size){
			maxWidthOfPage = page_size.offsetWidth;
			cals_debug_add_key_val( "maxWidthOfPage = page_size.offsetWidth", page_size.offsetWidth );
		}
		if( document.body.style.width ){
			maxWidthOfPage = document.body.style.width;
			cals_debug_add_key_val( "maxWidthOfPage = document.body.style.width", document.body.style.width );
		}
/*
		if( document.body.offsetWidth > 0 ){
			maxWidthOfPage = document.body.offsetWidth;
			cals_debug_add_key_val( "maxWidthOfPage = document.body.offsetWidth", document.body.offsetWidth );
		}
*/	
		document.body.style.padding=0;
		document.body.style.margin=0;
	
		var w = document.body.scrollWidth;

		cals_debug_add_key_val( "maxWidthOfPage", maxWidthOfPage );
		cals_debug_add_key_val( "document.body.scrollWidth", w );
	
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
				 && node.nodeName != "SCRIPT"  ){
					var child = document.body.removeChild(node);
					div.appendChild(child );
					i--;
				}
			}
			if( fixAbsolut || div.childNodes.length > 0 ){
				document.body.appendChild(divp);
				cals_debug_add_key_val( "div.offsetWidth", div.offsetWidth);
				divp.style.maxWidth = maxWidthOfPage + "px";
				divp.style.width = maxWidthOfPage + "px";
				cals_debug_add_key_val( "div.offsetWidth", div.offsetWidth);

				//body may be smaller now if div.cchip_body has word-break: break-word defined:
				w = divp.scrollWidth;
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

					div.style.webkitTransform = "scale("+f+","+f+")";
					div.style.webkitTransformOrigin = "0% 0%";
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

shrinkToFit();
