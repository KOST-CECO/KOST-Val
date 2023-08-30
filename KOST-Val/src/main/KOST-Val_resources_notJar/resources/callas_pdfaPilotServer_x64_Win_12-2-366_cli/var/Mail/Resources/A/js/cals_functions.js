var cals_DivDebug;

function cals_debug_AddDiv()
{
	if(cals_env_info.tool_configuration && cals_env_info.tool_configuration == "Debug")
	{
		cals_DivDebug = document.createElement("div");
		cals_DivDebug.className = "cchip_debug";

		if(document.body.firstChild){
			document.body.insertBefore(cals_DivDebug,document.body.firstChild);
		}else{
			document.body.appendChild(cals_DivDebug);
		}
	}
}

function cals_debug_add_val( val )
{
	if(cals_DivDebug){
		var div = document.createElement("div");
		div.class = "cchip_debug_entry";
		div.innerHTML = val;
		cals_DivDebug.appendChild(div);
	}
}

function cals_debug_add_key_val( key, val )
{
	cals_debug_add_val(key+" = "+val);
}


function cals_hasBaseTag()
{
	var bases = document.getElementsByTagName('base');
	var baseHref = null;
	return bases.length > 0;
}


var cals_default_css;
var cals_bHasBaseTag = cals_hasBaseTag();

function cals_makeURL( href_, encode )
{
	var url;
	var href = href_;
	if( encode){
		href = encodeURIComponent(href_);
	}else{
		href = href;
	}
	
	if (cals_bHasBaseTag) {
		url = cals_ccmip_mapping.base_path.replace(/\/?$/, '/');
		url += href;
	}else{
		url = href;
	}
	if (cals_bHasBaseTag) {
		url = "file://" + url;
	}
	return url;
}

function cals_getString(key)
{
	try{
		return cals_strings[cals_ccmip_mapping.language][key];
	}catch(err){
		return key;
	}
}

function cals_lookupString( key )
{
	try{
		if( key[0] == "_" && key[1] == "_" )
		{
			return cals_getString( key.substring(2) );
		}
		else if( key[0] == "@" && key[1] == "@" )
		{
			var key = key.substring(2);
			for(var i=0; i < cals_ccmip.headers.length; i++)
			{
				var header = cals_ccmip.headers[i];
				if(header.key == key )
				{
					return header.value;
				}
			}
		}
		else 
		{
			return key;
		}
	}catch(err){
		return key;
	}
}

function cals_getBookmapPath( key )
{
	var path = "";
	try
	{
		if(bookmarks)
		{
			var bm = bookmarks[key];
			if( bm.path )
			{
				for( var i = 0; i < bm.path.length; ++i)
				{
					if( i  > 0 ){
						path = path + "/";
					}
					path = path + cals_lookupString(bm.path[i]).trim();
				}
			}
		}
	}
	catch(err)
	{
		cals_debug_add_val( "cals_addPageBookmark failed: " + err);
	}
	return path;
}

function cals_addPageBookmark( div, key, value )
{
	try
	{
		var bm = bookmarks[key];
		if( bm )
		{
			if( bm.value )
			{
				div.setAttribute("data-cchip-bookmark", cals_lookupString(bm.value) );
			}
			else if( value )
			{
				div.setAttribute("data-cchip-bookmark", cals_lookupString(value) );
			}
			else
			{
				return;
			}
			if( bm.path )
			{
				div.setAttribute("data-cchip-bm-path", cals_getBookmapPath(key) );
			}
		}
	}
	catch(err)
	{
		cals_debug_add_val( "cals_addPageBookmark failed: " + err);
	}
}


function cals_setDefaultCSS(href)
{

	var head  = document.getElementsByTagName('head')[0];

	if( typeof(cals_default_css) == "undefined" )
	{
		cals_default_css = document.createElement('link');
		head.appendChild(cals_default_css);
	}
	cals_default_css.rel  = 'stylesheet';
	cals_default_css.type = 'text/css';
	cals_default_css.href = cals_makeURL(href,false);
	cals_default_css.media = 'all';
}

// cals_debug_AddDiv();
