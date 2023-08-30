function cals_addAttachment( attachment, parent, prefix )
{
	if( attachment.dst_embed )
	{
		var div = document.createElement("div");
		div.className = "cchip_attachment";
		var link = document.createElement("a");
		link.innerHTML = prefix + attachment.src_name;
		link.setAttribute("href", cals_makeURL(attachment.dst_path,true) );
		link.setAttribute("data-cchip-embed", cals_makeURL(attachment.dst_path,true) );
		link.setAttribute("data-cchip-mimetype", attachment.dst_mimetype );
		link.setAttribute("data-cchip-relationship", attachment.relationship );
		link.setAttribute("data-cchip-bookmark", prefix + attachment.dst_bookmark );
		link.setAttribute("cchip-pdfa", attachment.dst_pdfa );
	
		div.appendChild( link );
		parent.appendChild( div );
		
		return true;
	}
	return false;
}

function cals_addSrcFile( src, parent)
{
	var div = document.createElement("div");
	div.className = "cchip_attachment";
	var link = document.createElement("a");
	link.innerHTML = src.name;
	link.setAttribute("href", cals_makeURL(src.path,true) );
	link.setAttribute("data-cchip-embed", cals_makeURL(src.path,true) );
	link.setAttribute("data-cchip-mimetype", src.mimetype );
	link.setAttribute("data-cchip-relationship", src.relationship );
	link.setAttribute("data-cchip-bookmark", src.bookmark );
	link.setAttribute("cchip-pdfa", "" );

	div.appendChild( link );
	parent.appendChild( div );
	
	return true;
}

function cals_addAttachments()
{
	var n = 0;
	var div = document.createElement("div");
	div.className = "cchip_attachments";

	if( -1 == displayable_headers.indexOf("_attachments") )
	{
		for( var i=0; i < cals_ccmip_mapping.attachments.length; i++)
		{
			if( cals_addAttachment( cals_ccmip_mapping.attachments[i], div, "" ) )
			{
				n++;
			}
			if( typeof cals_ccmip_mapping.attachments[i].dst_unpacked != 'undefined' )
			{
				for( var j=0; j < cals_ccmip_mapping.attachments[i].dst_unpacked.length; j++)
				{
					if( cals_addAttachment( cals_ccmip_mapping.attachments[i].dst_unpacked[j], div, cals_ccmip_mapping.attachments[i].src_name + "/" ) )
					{
						n++;
					}
				}
			}
		}
	}
	if( -1 == displayable_headers.indexOf("_srcfiles") )
	{
		for( var i=0; i < cals_ccmip_mapping.srcfiles.length; i++)
		{
			if( cals_addSrcFile( cals_ccmip_mapping.srcfiles[i], div ) )
			{
				n++;
			}
		}
	}
	if( n > 0 )
	{
		document.body.appendChild(div);
	}
}
