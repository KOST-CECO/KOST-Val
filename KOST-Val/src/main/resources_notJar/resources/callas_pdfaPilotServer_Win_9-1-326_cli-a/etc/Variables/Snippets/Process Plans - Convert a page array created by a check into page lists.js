/*This scripts uses the "pageNumbers" output from a previous Single Check, which is an array of page numbers.
It converts all pages numbers from that array into two page lists: One is all pages that generated a hit, and one with
the remaining page numbers.
These lists can then be used with the Check "Sequential page number" and "is contained in".
When that Check is then used in the "Apply to" Pop up of a fixup only those pages will be processed by that fixup
that had generated a hit (with positivepagelist) or that did not (with negativepagelist).*/

 pageNumbers

// Initialize variables, positivepagelist will hold all page numbers where a previous check generated a hit, negativepagelist will hold all other pages
positivepagelist = "";
negativepagelist = "";
j =0;

// Derive array with page numbers from the previous check and write that array into a local variable array
local_pagearray=app.doc.result.checks[0].pageNumbers;

//For all pages in the PDF
for ( i=0; i<=app.doc.numPages-1; i++ )
{	
	//Increase page number by one because the page check is 1 based while the array is 0 based
	j = i+1;
	//If current page number is not in the page array, in which case indexOf will return -1
	if ( local_pagearray.indexOf(i) == -1 )
		negativepagelist = negativepagelist + " " + j;
	else
		positivepagelist = positivepagelist + " " + j;
}

//If no pages where found we need to add 0 because an empty list will not be processed, but since page 0 does never exist that is not a problem
if ( negativepagelist == "" )
	negativepagelist = "0";
if ( positivepagelist == "" )
	positivepagelist = "0";

//Write the pagelists into variables that are available within the process plan. For later use
app.vars.negativepagelist = negativepagelist;
app.vars.pixrepeatpagelist = pixrepeatpagelist;