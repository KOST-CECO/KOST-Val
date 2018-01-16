function cals_addHeaderForKey( parent, key_class, display_key, value )
{
	var spanKey = document.createElement("span");
	spanKey.className = "cchip_header_key "+key_class+"_key";
	spanKey.innerHTML = display_key;
	parent.appendChild(spanKey);

	var spanVal = document.createElement("span");
	spanVal.className = "cchip_header_value "+key_class+"_value";
	spanVal.innerHTML = value;
	parent.appendChild(spanVal);

	return 1;
}

function cals_addHeadersForKey( parent, hsettings, key, key_class, display_key )
{
	var n = 0;

	var div = document.createElement("div");
	div.className = "cchip_headers_for_key";

	if( key[0] == "*" && key[1] == "*")
	{
		n += cals_addHeaderForKey( div, key_class, display_key, key.substring(2) );
	}
	else
	{
		var collect = hsettings.mode && hsettings.mode == 3;
		var collected = "";
		for(var i=0; i < cals_ccmip.headers.length; i++)
		{
			var header = cals_ccmip.headers[i];
			if( header.key == key )
			{ 
				if( typeof header.value == "string" )
				{
					var value = header.value;
					// escape < and > to avoid tag rendering of "<name@domain.com>"
					value = value.replace(/</g, "&lt;").replace(/>/g, "&gt;");
		
					if (header.key == "date")
					{
						var format = cals_getString("_date_format"); // get localised format from cals_strings.js
						var rfc1123_date = value; // why rfc1123? -> http://www.csgnetwork.com/timerfc1123calc.html

						moment.lang(cals_ccmip_mapping.language); // http://momentjs.com/docs/#/i18n/changing-language/
						var localised_date = moment(rfc1123_date).format(format);
						value = localised_date;
					}
					if(collect){
						if( collected.length > 0 ){
							collected = collected + hsettings.sep;
						}
						collected = collected + value;
					}else{
						n += cals_addHeaderForKey( div, key_class, display_key, value);
					}
				}
				else if( header.value.length > 0 )
				{
					var value = "";
					for( var f = 0; f < header.value.length; ++f )
					{
						if( f > 0 ){
							value = value + "<span class='cchip_sep'>,</span>";
						}
						var field = header.value[f];
						if( field.display_name ){
							value = value + "<span class='cchip_display_name'>" + field.display_name + "</span>";
						}
						if( field.address ){
							if( field.display_name ){
								value = value + "<span class='cchip_bracket'>&lt;</span>";
							}
							value = value + "<span class='cchip_address'>"  + field.address + "</span>";
							if( field.display_name ){
								value = value + "<span class='cchip_bracket'>&gt;</span>";
							}
						}
					}
					if(collect){
						if( collected.length > 0 ){
							collected = collected + hsettings.sep;
						}
						collected = collected + value;
					}else{
						n += cals_addHeaderForKey( div, key_class, display_key, value);
					}
				}
			}
			if( hsettings.mode && hsettings.mode==2 && n > 0){
				break;
			}
		}
		if( collect && collected.length > 0){
			n += cals_addHeaderForKey( div, key_class, display_key, collected);
		}
	}
	if( n > 0 )
	{
		parent.appendChild(div);
	}
	else if( header_settings[key] && header_settings[key].fallback )
	{
		for( var i=0; i < header_settings[key].fallback.length && n == 0; i++)
		{
			n = cals_addHeadersForKey( parent, hsettings, header_settings[key].fallback[i], key_class, display_key );
		}
	}
	
	return n;
}

function addAttachmentLink( parent, s, href, embed, mimetype, relationship, bookmark, bookmarkPath, pdfa )
{
	var link = document.createElement("a");
	link.innerHTML = s;
	link.setAttribute("href", cals_makeURL(href,true));
	link.setAttribute("data-cchip-embed", cals_makeURL(embed,true) );
	link.setAttribute("data-cchip-mimetype", mimetype );
	link.setAttribute("data-cchip-relationship", relationship );
	link.setAttribute("data-cchip-bookmark", bookmark );
	if(bookmarkPath){
		link.setAttribute("data-cchip-bm-path", bookmarkPath );
	}
	link.setAttribute("cchip-pdfa", pdfa );
	parent.appendChild( link );
}

function cals_addHeadersForAttchemnts( parent, hsettings, display_key )
{
	var n = 0;
	if(cals_ccmip_mapping.attachments.length > 0)
	{
		var collect = hsettings.mode && hsettings.mode == 3;
		var collected = "";

		var div1 = document.createElement("div");
		div1.className = "cchip_headers_for_key";

		var span1 = document.createElement("span");
		span1.className = "cchip_header_key attachments_key";
		span1.innerHTML = display_key;
		div1.appendChild(span1); 

		var span2 = document.createElement("span");
		span2.className = "cchip_header_value attachments_value";
		div1.appendChild(span2); 

		for( var i=0; i < cals_ccmip_mapping.attachments.length; i++)
		{
			var attachment = cals_ccmip_mapping.attachments[i];

			if( i > 0)
			{
				var text = document.createElement("span");
				text.innerHTML = cals_getString("_attachment_sep");
				span2.appendChild( text );
			}

			if(attachment.dst_embed)
			{
				addAttachmentLink( span2, attachment.src_name, attachment.dst_path, attachment.dst_path, attachment.dst_mimetype, attachment.dst_relationship, attachment.dst_bookmark, cals_getBookmapPath("attachments"), attachment.dst_pdfa )
			}
			else if( typeof attachment.dst_unpacked != 'undefined' && attachment.dst_unpacked.length != 0 )
			{
				var text = document.createElement("span");
				text.innerHTML = attachment.src_name +  " [";
				span2.appendChild( text );
				for( var j=0; j < attachment.dst_unpacked.length; j++)
				{
					var attachment2 = attachment.dst_unpacked[j];
					if( j > 0)
					{
						var text = document.createElement("span");
						text.innerHTML = cals_getString("_attachment_sep");
						span2.appendChild( text );
					}
					if(attachment2.dst_embed)
					{
						addAttachmentLink( span2, attachment2.dst_name, attachment2.dst_path, attachment2.dst_path, attachment2.dst_mimetype, attachment2.dst_relationship, attachment.dst_bookmark + "/" + attachment2.dst_bookmark, cals_getBookmapPath("attachments"), attachment2.dst_pdfa )
					} 
					else 
					{
						var text = document.createElement("span");
						text.innerHTML = attachment2.dst_name;
						span2.appendChild( text );
					}
				}
				var text = document.createElement("span");
				text.innerHTML = "]";
				span2.appendChild( text );
			}
			else
			{
				var text = document.createElement("span");
				text.innerHTML = attachment.src_name;
				span2.appendChild( text );
			}
		}

		parent.appendChild(div1);
		n++;
	}
	return n;
}

function cals_addHeadersForSrcFiles( parent, hsettings, display_key )
{
	var n = 0;
	if(cals_ccmip_mapping.srcfiles.length > 0)
	{
		var div1 = document.createElement("div");
		div1.className = "cchip_headers_for_key";

		var span1 = document.createElement("span");
		span1.className = "cchip_header_key srcfiles_key";
		span1.innerHTML = display_key;
		div1.appendChild(span1); 

		var span2 = document.createElement("span");
		span2.className = "cchip_header_value srcfile_value";
		div1.appendChild(span2); 

		for( var i=0; i < cals_ccmip_mapping.srcfiles.length; i++)
		{
			var srcfile = cals_ccmip_mapping.srcfiles[i];

			if( i > 0)
			{
				var text = document.createElement("span");
				text.innerHTML = cals_getString("_attachment_sep");
				span2.appendChild( text );
			}

			addAttachmentLink( span2, srcfile.name, srcfile.path, srcfile.path, srcfile.mimetype, srcfile.relationship, srcfile.bookmark, cals_getBookmapPath("srcfiles"), "" );
		}
		parent.appendChild(div1);
		n++;
	}
	return n;
}

function cals_addHeaders()
{
	var n = 0;

	var divcont = document.createElement("div");
	divcont.className = "cchip_headers_container";

	var div = document.createElement("div");
	div.className = "cchip_headers";
	cals_addPageBookmark( div, "body" );

	for(var j=0; j < displayable_headers.length; j++)
	{
		var key = displayable_headers[j];
		var display_key = cals_getString(key);

		var hsettings = 1; 
		if( header_settings[key]){
			hsettings = header_settings[key];
		}

		if( key == "_attachments")
		{
			n += cals_addHeadersForAttchemnts( div, hsettings, display_key );
		}
		else if( key == "_srcfiles")
		{
			n += cals_addHeadersForSrcFiles( div, hsettings, display_key );
		}
		else
		{
			n += cals_addHeadersForKey( div, hsettings, key, key, display_key);
		}
	}

	if( n > 0 )
	{
		divcont.appendChild(div);
		document.body.insertBefore(divcont, document.body.firstChild );
	}
}

cals_addHeaders();
