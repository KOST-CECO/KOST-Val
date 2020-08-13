function printLoop() {
	// changeSuffix("pdf");
	printPages();
}

function padDigits(number, digits) {
    return Array(Math.max(digits - String(number).length + 1, 0)).join(0) + number;
}

function getDate(){
	var d = new Date(Date.parse(cals_env_info.date));
	s = padDigits(d.getDate(),2) + "." + padDigits(d.getMonth()+1,2) + "." + d.getFullYear() + " " + padDigits(d.getHours(),2) + ":" + padDigits(d.getMinutes(),2);
	return s;
}

function getHitNumString(n)
{
	if(n>0)
		return n.toString();
	else
		return "-";
}

function addHRule( parent)
{
	var hr = document.createElement("div");
	hr.className="hrule";
	parent.appendChild(hr);
}

function addOverviewHitIcon( parent, n, src_c, src_g, label_g )
{
	var src;
	var label;
	if( n > 0 )
	{
		src = src_c;
		label = n.toString();
	}
	else
	{
		src = src_g;
		label = label_g;
	}
	if( src.length > 0 )
	{
		var div =  document.createElement("div");
		div.className="ov_hit_box";

		var img =  document.createElement("img");
		img.className="ov_hit_icon";
		img.src=src;
		div.appendChild(img);

		if( label.length > 0 )
		{
			var divText =  document.createElement("div");
			divText.className="ov_hit_text";
			divText.innerHTML = label;
			div.appendChild(divText);
		}

		parent.appendChild(div);
	}
}

function addOverviewHitIcons( parent )
{
	var err = parseInt( cals_res_info.errors );
	var wrn = parseInt( cals_res_info.warnings );
	var inf = parseInt( cals_res_info.infos );
	var sum = err + wrn + inf;
	
	addOverviewHitIcon( parent, err, "img/error.pdf"  , "img/error_g.pdf"  , "-" );
	addOverviewHitIcon( parent, wrn, "img/warning.pdf", "img/warning_g.pdf", "-" );
	addOverviewHitIcon( parent, inf, "img/info.pdf"   , "img/info_g.pdf"   , "-" );
	addOverviewHitIcon( parent, sum, ""               , "img/ok.pdf"       , "" );
}

function writeHits( parent, step, id, severity, label, icon ){

	var kMaxRules = 9999;

	var hits = new Array();
	var matches = 0;
	
	for (var i=0;i<cals_res_info.steps[step].hits.length;i++)
	{ 
		var hit = cals_res_info.steps[step].hits[i];
		if( hit.matches > 0 && hit.severity == severity)
		{
			hits.push(hit);
			matches = matches + parseInt(hit.matches);
		}
	}

	if( hits.length > 0 )
	{
		var div = document.createElement("div");
		div.id=id;

		var img = document.createElement("img");
		img.src = icon;
		div.appendChild(img);

		var span_label =  document.createElement("span");
		span_label.className="severity_label dict";
		span_label.setAttribute("cals_dict",label);
		span_label.appendChild(document.createTextNode( label));
		div.appendChild(span_label);
	
//		var parent = document.getElementById("details_hits");
		parent.appendChild(div);
		
		var ul =  document.createElement("ul");
		
		var remaining = matches;

		for (var i=0; i < hits.length && i < kMaxRules;i++)
		{
			var hit = hits[i];

			remaining = remaining - hit.matches;
			//- White text smaller than 10 pt (25 matches on 1 page)
			var s = new String("");
			s = s.concat( "(" );
			s = s.concat( hit.matches );
			s = s.concat( " matches" );
			if( hit.pages > 0 )
			{
				s = s.concat( " on " );
				s = s.concat( hit.pages );
				if( hit.pages == 1 )
				{
					s = s.concat( " page" );
				}
				else
				{
					s = s.concat( " pages" );
				}
			}
			s = s.concat( ")" );
		
	//		alert(s);

			var span_rule_name =  document.createElement("span");
			span_rule_name.className="rule_name";
			span_rule_name.appendChild(document.createTextNode( hit.rule_name));

			var span_rule_matches =  document.createElement("span");
			span_rule_matches.className="rule_matches";
			span_rule_matches.appendChild(document.createTextNode( s));

			var li =  document.createElement("li");
			ul.appendChild(li);

			li.appendChild(span_rule_name);
			li.appendChild(span_rule_matches);
		}
		if( remaining > 0 )
		{
			//+ 61.559 other items
			var s = new String("+ ");
			s = s.concat( remaining );
			if( remaining == 1 )
			{
				s = s.concat(" other item");
			}
			else
			{
				s = s.concat(" other items");
			}

			var span_other_items =  document.createElement("span");
			span_other_items.className="other_items";
			span_other_items.appendChild(document.createTextNode( s));
			
			var li2 =  document.createElement("li");
			li2.appendChild(span_other_items);
			ul.appendChild(li2);
		}
		
		parent.appendChild(ul);
	}
	return matches;
}

function writeFixups( parent, step, label, icon  )
{
	var fixups = new Array();
	var succeeded = 0;
	var failed = 0;
	
	for (var i=0;i<cals_res_info.steps[step].fixups.length;i++)
	{ 
		var fixup = cals_res_info.steps[step].fixups[i];
		succeeded += parseInt(fixup.succeeded);
		failed += parseInt(fixup.failed);

		if( succeeded > 0 || failed > 0)
		{
			fixups.push(fixup);
		}
	}

	if( fixups.length > 0 )
	{
		var img = document.createElement("img");
		img.src = icon;
		parent.appendChild(img);

		var span_label =  document.createElement("span");
		span_label.className="fixup_label dict";
		span_label.setAttribute("cals_dict",label);
		span_label.appendChild(document.createTextNode( label));
		parent.appendChild(span_label);

		var ul =  document.createElement("ul");
		for (var i=0;i<fixups.length;i++)
		{
			var fixup = fixups[i];
			var li =  document.createElement("li");

			//(16 objects)
			var s = new String("(");
			s = s.concat( fixup.succeeded );
			s = s.concat( " objects)" );

			var span_fixup_name =  document.createElement("span");
			span_fixup_name.className="fixup_name";
			span_fixup_name.appendChild(document.createTextNode( fixup.fixup_name));

			var span_fixup_objects =  document.createElement("span");
			span_fixup_objects.className="fixup_objects";
			span_fixup_objects.appendChild(document.createTextNode( s));

			li.appendChild(span_fixup_name);
			li.appendChild(span_fixup_objects);

			ul.appendChild(li);
		}
		parent.appendChild(ul);
	}
	return fixups.length;
}

function getPlateNames()
{
	var s = new String;
	for (var i=0;i<cals_doc_info.docs[0].plates.length;i++)
	{
		if( i>0)
		{
			s+=", ";
		}
		s += cals_doc_info.docs[0].plates[i] ;
	}
	return s;
}

function getStandards()
{
	var s = new String;
	for (var i=0;i<cals_doc_info.docs[0].standards.length;i++)
	{
		if( i>0)
		{
			s+=", ";
		}
		s += cals_doc_info.docs[0].standards[i] ;
	}
	return s;
}

function writeStepDetails( parent, step )
{
	var divHits = document.createElement("div");
	divHits.className="details_hits";
	divHits.id="details_hits_"+step;

	if( cals_res_info.steps[step].type == "action" )
	{
		//TODO
	}

	if( cals_res_info.steps[step].type == "profile" || cals_res_info.steps[step].type == "rule" )
	{
		var nHits = 0;
		nHits += writeHits( divHits, step, "details_hits_error"  , "error"  , "Errors"  , "img/error.pdf" )
		nHits += writeHits( divHits, step, "details_hits_warning", "warning", "Warnings", "img/warning.pdf" )
		nHits += writeHits( divHits, step, "details_hits_info"   , "info"   , "Infos"   , "img/info.pdf" )

		if( nHits > 0 )
		{
			parent.appendChild(divHits);
		}
	}

	if( cals_res_info.steps[step].type == "profile" || cals_res_info.steps[step].type == "fixup" )
	{
		var divFixups = document.createElement("div");
		divFixups.className="details_fixups";
		divFixups.id="details_fixup_"+step;

		var nFixups = writeFixups( divFixups, step, "Fixups" ,"img/fixup.pdf" );
		if( nFixups > 0 )
		{
			parent.appendChild(divFixups);
		}
	}

}

function writeStep( parent, step )
{
	var div = document.createElement("div");
	div.className="processstep";
	div.id="step_"+step;

	var divHeader = document.createElement("div");
	divHeader.className="step_header";
	divHeader.id="step_"+step;

	var p = document.createElement("p");
	p.innerHTML=cals_res_info.steps[step].name;
	divHeader.appendChild(p);

	div.appendChild(divHeader);
	parent.appendChild(div);

	writeStepDetails(div,step);
}

function writeDetails()
{
	var parent = document.getElementById("details");
	for (var i=0;i<cals_res_info.steps.length;i++)
	{
		writeStep(parent,i);
	}
}

function changeSuffix(new_suffix)
{
	var image = document.getElementsByTagName("img");

	for (var i=0, im=image.length; im>i; i++)
	{
		var id_check = image[i].getAttribute("id");

		if (id_check != "overview_img")
		{
			var src_prefix = image[i].getAttribute("src").slice(0, -3);
			image[i].setAttribute("src",src_prefix+new_suffix);
		}
	}
}

function translate()
{
	var e = document.getElementsByClassName("dict");

	for (var i=0, l=e.length; i<l; i++)
	{
		var key = "cals_dict." + e[i].getAttribute("cals_dict");
		var val = eval(key);
		if(val===undefined)
			e[i].innerHTML = "##"+e[i].innerHTML + "{" + key + "}";
		else
			e[i].innerHTML = val;
	}
}

function setFileSize()
{
	var key = document.getElementById("fileSizeKey");
	var val = document.getElementById("fileSizeValue");

	if( cals_doc_info.docs[0].file_size > 1073741824 ) {
		key.innerHTML="File size (GB)";
		key.setAttribute("cals_dict","FileSizeGB");
		val.innerHTML=(cals_doc_info.docs[0].file_size/1073741824).toFixed(2);
	}else if( cals_doc_info.docs[0].file_size > 1048576 ){
		key.innerHTML="File size (MB)";
		key.setAttribute("cals_dict","FileSizeMB");
		val.innerHTML=(cals_doc_info.docs[0].file_size/1048576).toFixed(2);	
	}else if( cals_doc_info.docs[0].file_size > 1024 ){
		key.innerHTML="File size (KB)";
		key.setAttribute("cals_dict","FileSizeKB");
		val.innerHTML=(cals_doc_info.docs[0].file_size/1024).toFixed(2);	
	}else{
		key.innerHTML="File size (Byte)";
		key.setAttribute("cals_dict","FileSizeByte");
		val.innerHTML=(cals_doc_info.docs[0].file_size).toFixed(2);	
	}
}

function init()
{
	translate();
	//replace all pdf images by the png variants if not running in pdfChip
	//hint: cchip object is disabled for report creation, so the following does not work:
	// if( typeof cchip == "undefined"){
	if( navigator.appVersion.search("pdfChip") == -1){//"5.0 (Macintosh; Intel Mac OS X) AppleWebKit/538.1 (KHTML, like Gecko) pdfChip Safari/538.1"
		changeSuffix("png");
	}
}
