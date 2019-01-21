// quick and dirty & inelegant interception of non-supported languages, will be improved
/*
<meta name="From" 
      content="Ulrich Frotscher &amp;lt;u.frotscher@callassoftware.com&amp;gt;" 
	  data-cchip-xmp-ns="http://www.callassoftware.com/ccmip/1.0/" 
	  data-cchip-xmp-prefix="ccmip" 
	  data-cchip-xmp-property="to" 
	  data-cchip-xmp-type="simple"
	  >
*/

function cals_xmpAddStructMember( meta, name, type, value, suffix )
{
	meta.setAttribute("data-cchip-xmp-struct-member-name-"+suffix,name);
	meta.setAttribute("data-cchip-xmp-struct-member-type-"+suffix,type);
	meta.setAttribute("data-cchip-xmp-struct-member-value-"+suffix,value);
}

function cals_xmpAddStructData( meta, struct, value )
{
	meta.setAttribute("data-cchip-xmp-struct-ns",xmp_schemas[struct.schema].ns);
	meta.setAttribute("data-cchip-xmp-struct-prefix",xmp_schemas[struct.schema].prefix);
	
	if(struct.type == "Date"){
		var mdate = moment(value);
		value= mdate.toISOString();
	
		var date = new Date(value);
		value=date.toISOString();
	}
	
	var suffix = 1;
	cals_xmpAddStructMember( meta, struct.property, struct.type, value, suffix++ );

	for (var property in struct.properties ) 
	{
		var value = struct.properties[property];
		if( value[0] == "_" && value[1] == "_" )
		{
			cals_xmpAddStructMember( meta, property, "Text", cals_getString(value), suffix++ );
		}
		else if( value[0] == "@" && value[1] == "@" )
		{
			var key = value.substring(2);
			for(var i=0; i < cals_ccmip.headers.length; i++)
			{
				var header = cals_ccmip.headers[i];
				if(header.key == key )
				{
					cals_xmpAddStructMember( meta, property, "Text", header.value, suffix++ );
				}
			}
		}
		else
		{
			cals_xmpAddStructMember( meta, property, "Text", value, suffix++ );
		}
	}
}

function cals_xmpDoAddOne( header, data )
{
	if(data)
	{
		var meta = document.createElement("meta");

		meta.name=header.key;
		meta.setAttribute("property",meta.name);
		if( data.type=="Date" ) {
			var date = new Date(header.value);
			meta.content=date.toISOString();
		} else {
			meta.content=header.value;
		}
	
		meta.setAttribute("data-cchip-xmp-ns",xmp_schemas[data.schema].ns);
		meta.setAttribute("data-cchip-xmp-prefix",xmp_schemas[data.schema].prefix);
		meta.setAttribute("data-cchip-xmp-type",data.type);

		if(data.property=="*")
			meta.setAttribute("data-cchip-xmp-property",header.key);
		else
			meta.setAttribute("data-cchip-xmp-property",data.property);

		if( data.struct )
		{
			cals_xmpAddStructData( meta, data.struct, header.value );
		}

		document.head.appendChild(meta);
		return true;
	}
	return false;
}

function cals_xmpAddOne( header )
{
	for(var i=0; i < xmp_headers.length; i++)
	{
		if( !cals_xmpDoAddOne( header, xmp_headers[i][header.key]) && cals_ccmip_mapping.xmp_all )
		{
			cals_xmpDoAddOne( header, xmp_headers[i]["*"]);
		}
	}
}

function cals_xmpAddMetaData()
{
	try
	{
		for(var i=0; i < cals_ccmip.headers.length; i++)
		{
			var header = cals_ccmip.headers[i];
			if( typeof header.value == "string" ){
				cals_xmpAddOne( header );
			}else{
				for( var h = 0; h < header.value.length; h++)
				{
					var value = "";
					if( header.value[h].display_name){
						value = value + header.value[h].display_name;
					}
					if( header.value[h].address){
						value = value + " <" + header.value[h].address + ">";
					}
					var hh = { key: header.key, value: value };
					cals_xmpAddOne( hh );
				}
			}
		}
	}
	catch(err)
	{
		alert( "cals_xmpAddMetaData error:" + err.message );
	}
}


cals_xmpAddMetaData();
