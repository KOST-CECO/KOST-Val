// From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
if (!Object.keys) {
  Object.keys = (function() {
    'use strict';
    var hasOwnProperty = Object.prototype.hasOwnProperty,
        hasDontEnumBug = !({ toString: null }).propertyIsEnumerable('toString'),
        dontEnums = [
          'toString',
          'toLocaleString',
          'valueOf',
          'hasOwnProperty',
          'isPrototypeOf',
          'propertyIsEnumerable',
          'constructor'
        ],
        dontEnumsLength = dontEnums.length;

    return function(obj) {
      if (typeof obj !== 'function' && (typeof obj !== 'object' || obj === null)) {
        throw new TypeError('Object.keys called on non-object');
      }

      var result = [], prop, i;

      for (prop in obj) {
        if (hasOwnProperty.call(obj, prop)) {
          result.push(prop);
        }
      }

      if (hasDontEnumBug) {
        for (i = 0; i < dontEnumsLength; i++) {
          if (hasOwnProperty.call(obj, dontEnums[i])) {
            result.push(dontEnums[i]);
          }
        }
      }
      return result;
    };
  }());
}

/*
t1: "Convert all pages into CMYK images and preserve text information"
t2: "Analyze pages for effectively used plates"

opcodes:

[ ["replace", 0, 2, 0, 1]
["equal", 2, 3, 1, 2]
["delete", 3, 8, 2, 2]
["equal", 8, 10, 2, 4]
["replace", 10, 11, 4, 7]
["equal", 11, 18, 7, 14]
["delete", 18, 55, 14, 14]
["equal", 55, 58, 14, 17]
["replace", 58, 59, 17, 37]
["equal", 59, 61, 37, 39]
["replace", 61, 64, 39, 41]
]
*/

var naText = "";//"n.a."
var kIndent = 10;//pixels per level



/* Add colored spans to p1 and p2 according to the text diff
*/
function createColoredSpans( p1, t1, p2, t2 ){
	var n = 0;
	if( t1 != "" || t2 != "" ){
	    n=1;
	    var sm = new difflib.SequenceMatcher(t1, t2);
	    var opcodes = sm.get_opcodes();
	    var hasDiff = opcodes.length > 1 || opcodes[0][0] != "equal";
	    if(hasDiff){
	    	n=2;
	    }

	    for( var i = 0; i < opcodes.length; ++i){
	    	var opcode = opcodes[i];
	    	var theClass = opcode[0];

			var pos1 = opcode[1];
			var len1 = opcode[2]-opcode[1];
			var pos2 = opcode[3];
			var len2 = opcode[4]-opcode[3];

			if( len1 > 0 ){
				var s1 = document.createElement('div');
				s1.innerHTML = t1.substr(pos1, len1).replace(' ', '&nbsp;');
				s1.className = t2 == "" ? "added" : theClass;
				p1.appendChild(s1);
			}
			if( len2 > 0 ){
				var s2 = document.createElement('div');
				s2.innerHTML = t2.substr(pos2, len2).replace(' ', '&nbsp;');
				s2.className = t1 == "" ? "added" : theClass;
				p2.appendChild(s2);
			}
	    }
	}
	return n;
}

// function isCollapsed( left, right ){
// 	if( prc_diff_settings.collapse ){
		
// 	}else{
// 		return false;
// 	}
// }

function forceParentsVisible( tbody, l, r, indent, nthChild ){
	var c =tbody.childNodes[nthChild];
	var myIndent = tbody.childNodes[nthChild].getAttribute("indent");
	if( myIndent == indent)
	{
		if( c.className.search("hidden") >= 0 ){
			c.className = c.className.replace(/hidden/g, "xxxxxxxxx");
			if(indent > 0 && nthChild > 0){
				if( !l || !r){
					var t1 = l ? c.childNodes[1].innerHTML : "";
					var t2 = r ? c.childNodes[3].innerHTML : "";
					c.childNodes[1].innerHTML ="";
					c.childNodes[3].innerHTML ="";
					createColoredSpans( c.childNodes[1], t1, c.childNodes[3], t2 );
					// if( !l ){
					// 	c.childNodes[1].innerHTML ="";
					// 	// c.childNodes[1].className = c.childNodes[1].className + " added_right";
					// 	// c.className = c.className + " diff_row added_right";
					// }
					// if( !r ){
					// 	c.childNodes[3].innerHTML ="";
					// 	// c.childNodes[3].className = c.childNodes[3].className + " added_right";
					// 	// c.className = c.className + " diff_row added_left";
					// }
				}
				forceParentsVisible( tbody, l, r, indent-1, nthChild-1 );
			}
		}
	}
	else if(myIndent > indent && nthChild > 0)
	{
		forceParentsVisible( tbody, l, r, indent, nthChild-1 );
	}
}

function addTableRow( indent, className, label, t1, t2, forcevisible ){
	var trClassName = className;
	var tbody = document.getElementById("tbody");

	if( prc_diff_settings.collapse){
		if(t1==t2){
			trClassName = trClassName + " hidden";
		}
	}
	if( t1!=t2 && tbody.childNodes.length > 0){
		forceParentsVisible( tbody, t1.length > 0 , t2.length > 0 , indent-1, tbody.childNodes.length-1 );
	}
	// if( forcevisible ){
	// 	className = className + " forcevisible";
	// }

	var a1 = t1.split("\n");
	var a2 = t2.split("\n");
	var n1 = a1.length
	var n2 = a2.length
	var n = Math.max(n1,n2);

	if( n > 1 ){
		var hasDiff = addTableRow( indent, className, label, a1[0], a2[0], forcevisible);
		for( var i = 1; i < n; ++i ){
			hasDiff = addTableRow( indent+1, className, "value", i < n1 ? a1[i] : naText, i < n2 ? a2[i] : naText, forcevisible) || hasDiff;
		}
		return;
	}

	var c1 = document.createElement('td');
	var c2 = document.createElement('td');
	var n = createColoredSpans(c1,t1,c2,t2);
	var hasdiff = n == 2;

	var added_left  = className.indexOf("added_left" ) >= 0 || t2.length==0 && t1.length>0;
	var added_right = className.indexOf("added_right") >= 0 || t1.length==0 && t2.length>0;;
	var diffMarker = added_left  ? "-"
	               : added_right ? "+"
	               : hasdiff     ? "~"
	               :                "";

	var tdLabel = document.createElement('td');
	tdLabel.innerHTML = label;

	var tdDiffMarker = document.createElement('td');
	tdDiffMarker.innerHTML = diffMarker;

	if( n ){
		var paddingLeft = (10 + indent*kIndent) + "px";
		// var tbody = document.getElementById("tbody");
		var row = document.createElement('tr');

		row.className = 'diff_row ' + trClassName;
		tdLabel.className  = 'diff_label ' + (hasdiff ? 'hasdiff ' : '') + className;
		tdDiffMarker.className  = 'diff_marker ' + (hasdiff ? 'hasdiff ' : '') + className;;
		c1.className  = 'diff_cell left ' + className;;
		c2.className  = 'diff_cell right ' + className;;
		c1.style.paddingLeft = c2.style.paddingLeft = paddingLeft;
		row.appendChild(tdLabel);
		row.appendChild(c1);
		row.appendChild(tdDiffMarker);
		row.appendChild(c2);
		row.setAttribute("indent", indent);
		tbody.appendChild(row);
	}
	return true;
}

function makeClass(left,right,className){
	var s = className;
	if(left && !right)
		s += " added_left"
	else if(!left && right)
		s += " added_right"
	return s;
}

function getWzSeverity(sev){
	switch(sev)
	{
		case 0: return "Error";
		case 1: return "Warning";
		case 2: return "Info";
		default: return naText;
	}
}

function addWzChecks(indent, lchecks, rchecks)
{
	var hasDiffs = false;
	var n = lchecks ? lchecks.length : rchecks.length;
	for( var i = 0; i < n; ++i ){
		var l = lchecks ? lchecks[i]: null;
		var r = rchecks ? rchecks[i]: null;

		if( l && l.severity == 3)
			l = null
		if( r && r.severity == 3)
			r = null
		if( l || r ){
			hasDiffs = 
			addTableRow( indent, makeClass(l,r,"typename"), "wizardcheck"
				       , l ? l.name : naText
				       , r ? r.name : naText, false ) || hasDiffs;
			hasDiffs = 
			addTableRow( indent+1, makeClass(l,r,""), "severity"
				       , l ? getWzSeverity(l.severity) : naText
				       , r ? getWzSeverity(r.severity) : naText, false ) || hasDiffs;
		}
	}
	return hasDiffs;
}

function addWizardConversionParams( indent, l, r, show, label )
{
	var hasDiffs = false;
	if( l && r ){
		var nparams = l ? l.length : r.length;
		for( var p = 0; p < nparams; ++p){
			if( l ? l[p].title : r[p].title ){
				hasDiffs = 
				addTableRow( indent, makeClass(l,r,"hidden"), label
					       , l ? l[p].title : naText
					       , r ? r[p].title : naText, false ) || hasDiffs;

				addWizardConversionParams( indent+1, l ? l[p].params : null, r ? r[p].params : null, show, label );
			}else{
				hasDiffs = 
				addTableRow( indent, makeClass(l,r,""), label
					       , l ? l[p] : naText
					       , r ? r[p] : naText, false ) || hasDiffs;
			}
		}
	}
	return hasDiffs;
}

function addWizardConversion( indent, l, r )//wzconversion
{
	var hasDiffs = false;
	var showx  = (l ? l.PDFX.convert : false)
	          || (r ? r.PDFX.convert : false);
	var showa  = (l ? l.PDFA.convert : false)
	          || (r ? r.PDFA.convert : false);
	var showe  = (l ? l.PDFE.convert : false)
	          || (r ? r.PDFE.convert : false);
	var show = showx || showa || showe;

	hasDiffs = 
	addTableRow( indent, makeClass(l,r,"hidden"), "wizardconversion"
		       , l ? l.title : naText
		       , r ? r.title : naText, false ) || hasDiffs;
	//PDF/X
	hasDiffs = 
	addTableRow( indent+1, makeClass(l,r,showx ? "" : "hidden"), "pdfxconversion"
		       , l ? l.PDFX.title : naText
		       , r ? r.PDFX.title : naText, false ) || hasDiffs;
	addWizardConversionParams( indent + 2, l ? l.PDFX.params : null, r ? r.PDFX.params : null, showx, "pdfxparam" );

	//PDF/A
	hasDiffs = 
	addTableRow( indent+1, makeClass(l,r,"hidden"), "pdfaconversion"
		       , l ? l.PDFA.title : naText
		       , r ? r.PDFA.title : naText, false ) || hasDiffs;
	addWizardConversionParams( indent + 2, l ? l.PDFA.params : null, r ? r.PDFA.params : null, showa, "pdfaparam" );

	//PDF/E
	hasDiffs = 
	addTableRow( indent+1, makeClass(l,r,"hidden"), "pdfeconversion"
		       , l ? l.PDFE.title : naText
		       , r ? r.PDFE.title : naText, false ) || hasDiffs;
	addWizardConversionParams( indent + 2, l ? l.PDFE.params : null, r ? r.PDFE.params : null, showe, "pdfeparam" );

	// OI.names
	var showOI = show &&
	                (l ? l.OI.title.length > 0 : false) 
	             || (r ? r.OI.title.length > 0 : false );
	hasDiffs = 
	addTableRow( indent+1, makeClass(l,r,"hidden"), "outputintent"
		       , l ? l.OI.title : naText
		       , r ? r.OI.title : naText, false ) || hasDiffs;
	addWizardConversionParams( indent + 2, l ? l.OI.params : null, r ? r.OI.params : null, showOI, "outputintentparam" );

	return hasDiffs;
}

function addProcessPlanData( indent, obj )
{
	var n1 = obj.left && obj.left.sequence ? obj.left.sequence.length : 0;
	var n2 = obj.right && obj.right.sequence ? obj.right.sequence.length : 0;
	var n = Math.max(n1,n2);

	if( n>0){
		addTableRow( indent, makeClass(t1,t2,"hidden"), "sequence"
			       , "Sequence"
			       , "Sequence", false );

		for( var i = 0; i < n; ++i){
			var t1 = i < n1 ? /*obj.left.sequence[i].type  + ": " +*/ obj.left.sequence[i].name : "";
			var t2 = i < n2 ? /*obj.right.sequence[i].type + ": " +*/ obj.right.sequence[i].name : "";

			addTableRow( indent+1, makeClass(t1,t2,""), "step"
				       , i < n1 ? t1 : naText
				       , i < n2 ? t2 : naText, false );
			addTableRow( indent+2, makeClass(t1,t2,""), "type"
				       , i < n1 ? obj.left.sequence[i].type : naText
				       , i < n2 ? obj.right.sequence[i].type : naText, false );
		}
	}
}

function addProfileData( indent, obj )
{
	var hasDiffs = false;

	// wzchecks

	var nGroups = obj.left ? obj.left.wzchecks.length : obj.right.wzchecks.length;
	for( var g = 0; g < nGroups; ++g)
	{
		var l = obj.left  ? obj.left.wzchecks[g] : null;
		var r = obj.right ? obj.right.wzchecks[g]: null;
		hasDiffs = 
		addTableRow( indent, makeClass(l,r,"hidden"), "wizardgroup"
			       , l ? l.name : naText
			       , r ? r.name : naText, false ) || hasDiffs;
		hasDiffs = 
		addWzChecks( indent+1, l ? l.checks : null, r ? r.checks : null ) || hasDiffs;
	}

	addWizardConversion( indent, obj.left ? obj.left.wzconversion : null, obj.right ? obj.right.wzconversion : null );
	addWizardConversion( indent, obj.left ? obj.left.wzconversionemb : null, obj.right ? obj.right.wzconversionemb : null );

	return hasDiffs;
}

function addConditionData( indent, obj )
{
	// addTableRow( indent, makeClass(obj.left,obj.right,""), "datatype"
	// 	       , obj.left ? obj.left.datatype : naText
	// 	       , obj.right ? obj.right.datatype : naText, false );

	addTableRow( indent, makeClass(obj.left,obj.right,""), "operator"
		       , obj.left ? obj.left.operator : naText
		       , obj.right ? obj.right.operator : naText, false );

	var n1 = obj.left && obj.left.values ? obj.left.values.length : 0;
	var n2 = obj.right && obj.right.values ? obj.right.values.length : 0;
	var n = Math.max(n1,n2);

	for( var i = 0; i < n; ++i){
		var label = i < n1 ? obj.left.values[i].label 
		          : i < n2 ? obj.right.values[i].label 
		          : "";
		addTableRow( indent, makeClass(obj.left,obj.right,""), label
			       , i < n1 ? obj.left.values[i].value : naText
			       , i < n2 ? obj.right.values[i].value : naText, false );

	}
}

function addRuleData( indent, obj )
{
	addTableRow( indent, makeClass(obj.left,obj.right,""), "apply to"
		       , obj.left ? obj.left.applyTo : naText
		       , obj.right ? obj.right.applyTo : naText, false );

	addTableRow( indent, makeClass(obj.left,obj.right,""), "options"
		       , obj.left ? obj.left.options : naText
		       , obj.right ? obj.right.options : naText, false );
}

function addFixupParams( indent, leftParams, rightParams )
{
	var n1 = leftParams ? leftParams.length : 0;
	var n2 = rightParams  ? rightParams.length : 0;
	var n = Math.max(n1,n2);

	for( var i = 0; i < n; ++i){
		addTableRow( indent, makeClass(leftParams,rightParams,""), "param"
			       , i < n1 ? leftParams[i].label  + ": " + leftParams[i].value : naText
			       , i < n2 ? rightParams[i].label + ": " + rightParams[i].value : naText, false );
	}
}

function addFixupData( indent, obj )
{
	var n1 = obj.left && obj.left.features ? obj.left.features.length : 0;
	var n2 = obj.right && obj.right.features ? obj.right.features.length : 0;
	var n = Math.max(n1,n2);

	for( var i = 0; i < n; ++i){
		addTableRow( indent, makeClass(obj.left ? obj.left.features[i] : null,obj.right ? obj.right.features[i] : null,""), "feature"
			       , i < n1 ? obj.left.features[i].name : naText
			       , i < n2 ? obj.right.features[i].name : naText, false );
		addFixupParams( indent+1
			          , i < n1 ? obj.left.features[i].params : null 
			          , i < n2 ? obj.right.features[i].params : null 
			          );

	}
}

function addActionData( indent, obj )
{
	var n1 = obj.left && obj.left.params ? obj.left.params.length : 0;
	var n2 = obj.right && obj.right.params ? obj.right.params.length : 0;
	var n = Math.max(n1,n2);

	var t1 = obj.left  ? obj.left.id  : "";
	var t2 = obj.right ? obj.right.id : "";
	addTableRow( indent, makeClass(t1,t2,""), "feature"
		       , t1
		       , t2, false );

	if( t1 == t2 && n1 == n2 ){
		for( var i = 0; i < n; ++i){
			addTableRow( indent+1, makeClass( obj.left.params[i].value, obj.right.params[i],""), obj.left.params[i].key
				       , obj.left.params[i].value
				       , obj.right.params[i].value, false );
		}
	}else{
		for( var i = 0; i < n1; ++i){
			addTableRow( indent+1, makeClass(obj.left.params[i].value,null,""), obj.left.params[i].key
				       , obj.left.params[i].value
				       , naText, false );
		}
		for( var i = 0; i < n2; ++i){
			addTableRow( indent+1, makeClass(null,obj.right.params[i].value,""), obj.right.params[i].key
				       , naText
				       , obj.right.params[i].value, false );
		}
	}
}
/*
					"key" : "purpose_with_value_list",
					"name" : "purpose_with_value_list",
					"rangetype" : "List",
					"value" : "",
					"values" : [ "1", "2", "3", "4" ],
					"vartype" : "Simple value"
*/
function addVariableData( indent, obj )
{
	var k1 = obj.left  ? obj.left.key  : null;
	var k2 = obj.right ? obj.right.key : null;

	// addTableRow( indent, makeClass(k1,k2,""), "key"
	// 	       , k1 ? k1 : naText
	// 	       , k2 ? k2 : naText, obj.dist > 0 );

	addTableRow( indent, makeClass(k1,k2,""), "type"
		       , obj.left  ? obj.left.vartype  : naText
		       , obj.right ? obj.right.vartype : naText, false );
	addTableRow( indent, makeClass(k1,k2,""), "value"
		       , obj.left  ? obj.left.value  : naText
		       , obj.right ? obj.right.value : naText, false );

	if( (obj.left && obj.left.rangetype.length > 0) || (obj.right && obj.right.rangetype.length > 0) ){
		addTableRow( indent, makeClass(k1,k2,""), "range"
			       , obj.left  ? obj.left.rangetype  : naText
			       , obj.right ? obj.right.rangetype : naText, false );
		
		var n1 = obj.left && obj.left.values ? obj.left.values.length : 0;
		var n2 = obj.right && obj.right.values ? obj.right.values.length : 0;
		var n = Math.max(n1,n2);
		if( n > 0 ){
			addTableRow( indent, "", "values"
				       , "Values"
				       , "Values", false );
			for( var i = 0; i < n; ++i){
				addTableRow( indent+1, makeClass(i < n1 ? obj.left.values[i] : null,i < n2 ? obj.right.values[i] : null,""), "value"
					       , i < n1 ? obj.left.values[i] : naText
					       , i < n2 ? obj.right.values[i] : naText, false );
			}
		}
	}

}

function addMetadata( indent, lmetadata, rmetadata ){
	var n1 = lmetadata ? lmetadata.length  : 0;
	var n2 = rmetadata ? rmetadata.length  : 0;
	var n = Math.max(n1,n2);
	if( n > 0 ){
		addTableRow( indent, "hidden", "metadata", "Metadata", "Metadata", false );
		for( var i = 0; i < n; ++i){
			var k1 = i < n1 ? lmetadata[i].key : null;
			var k2 = i < n2 ? rmetadata[i].key : null;
			var v1 = i < n1 ? lmetadata[i].value : null;
			var v2 = i < n2 ? rmetadata[i].value : null;

			addTableRow( indent+1, makeClass(k1,k2,""), "entry"
				       , k1 ? k1 + ": " + v1 : naText
				       , k2 ? k2 + ": " + v2 : naText, false );
		}
	}
}


function addGroup( indent, type, childs )
{
	for (var i = 0; i < childs.length; ++i ) {
		addChilds( indent, childs[i]);
	}	
}

function addFlags( indent, lflags, rflags ){
	var dict1 = lflags ? lflags.search("D") >= 0 : null;
	var dict2 = rflags ? rflags.search("D") >= 0 : null;
	var edit1 = lflags ? lflags.search("E") >= 0 : null;
	var edit2 = rflags ? rflags.search("E") >= 0 : null;
	var f1 = dict1 != null ? (dict1 ? "localized" : "not localized") : naText;
	var f2 = dict2 != null ? (dict2 ? "localized" : "not localized") : naText;
	f1 += (f1.length >0?", ":"") + (edit1 != null ? (edit1 ? "editable" : "locked") : naText);
	f2 += (f2.length >0?", ":"") + (edit2 != null ? (edit2 ? "editable" : "locked") : naText);
	addTableRow( indent, makeClass(f1,f2,""), "flags", f1, f2, false );

	// if( dict1 != null || dict2 != null ){
	// 	addTableRow( indent+1, makeClass(dict1,dict2,""), "localizable"
	// 		       , dict1 != null ? dict1 ? "localized" : "not localized" : naText
	// 		       , dict2 != null ? dict2 ? "localized" : "not localized" : naText, false );
	// }
	// if( edit1 != null || edit2 != null ){
	// 	addTableRow( indent+1, makeClass(edit1,edit2,""), "editable"
	// 		       , edit1 != null ? edit1 ? "editable" : "locked" : naText
	// 		       , edit2 != null ? edit2 ? "editable" : "locked" : naText, false );
	// }
}

function addChilds( indent, obj )
{
	addTableRow( indent, makeClass(obj.left,obj.right,"typename"), obj.type
		       , obj.left  && obj.left.name  ? obj.left.name : naText
		       , obj.right && obj.right.name ? obj.right.name: naText, obj.dist > 0 );

	++indent;
	addTableRow( indent+1, makeClass(obj.left,obj.right,"comment"), "comment"
		       , obj.left  && obj.left.comment  ? obj.left.comment : naText
		       , obj.right && obj.right.comment ? obj.right.comment: naText, false );

	addFlags( indent+1, obj.left  && obj.left.flags  ? obj.left.flags  : null
	                  , obj.right && obj.right.flags ? obj.right.flags : null  );
	
	if( obj.left && obj.left.metadata || obj.right && obj.right.metadata ){
		addMetadata( indent+1, obj.left ? obj.left.metadata : null, obj.right ? obj.right.metadata : null );
	}

	switch( obj.type){

		case "processplan":
			addProcessPlanData( indent+1, obj );
			break;
		case "profile":
			addProfileData( indent+1, obj );
			break;
		case "condition":
			addConditionData( indent+1, obj );
			break;
		case "rule":
			addRuleData( indent+1, obj );
			break;
		case "fixup":
			addFixupData( indent+1, obj );
			break;
		case "action":
			addActionData( indent+1, obj );
			break;
		case "variable":
			addVariableData( indent+1, obj );
			break;
	}

	for (var prop in obj.childs ) {
		if (obj.childs.hasOwnProperty(prop)) {
			addGroup( indent+1, prop, obj.childs[prop]);
		}
	}	
}

function onLoad()
{
	try
	{
		if( prc_diff.msg){
			document.getElementById("msg").innerHTML = prc_diff.msg;
		}
		if( prc_diff.childs){
			var type = Object.keys(prc_diff.childs)[0];
			for (var prop in prc_diff.childs ) {
				if (prc_diff.childs.hasOwnProperty(prop)) {
					addGroup( 0, prop, prc_diff.childs[prop]);
				}
			}	
		}
	}catch(x)
	{
		document.getElementById("msg").innerHTML = x.message;
	}
}
