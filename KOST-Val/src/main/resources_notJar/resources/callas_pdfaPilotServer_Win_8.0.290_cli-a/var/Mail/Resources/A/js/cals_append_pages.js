function cals_addAsPages( attachment, prefix )
{
	if( attachment.dst_append && attachment.dst_pages > 0 )
	{
		for( var i=0; i < attachment.dst_pages; i++)
		{
			var div = document.createElement("div");
			div.className = "cchip_attachment_page";
			if(i==0){
				cals_addPageBookmark( div, "attachments", prefix + attachment.dst_bookmark );
			}

			var div2 = document.createElement("div");
			div2.className = "cchip_attachment_page_header";
			div2.innerHTML = prefix + attachment.src_name + ": " + (i+1) + "/" + attachment.dst_pages;
			div.appendChild(div2);

			var img = document.createElement("img");
			img.src = cals_makeURL(attachment.dst_path,true) + "#page=" + (i+1);
			div.appendChild(img);

			document.body.appendChild(div);
		}
	}
}

function cals_addAttachmentsAsPages()
{
	for( var i=0; i < cals_ccmip_mapping.attachments.length; i++)
	{
		cals_addAsPages( cals_ccmip_mapping.attachments[i], "" );
		if( typeof cals_ccmip_mapping.attachments[i].dst_unpacked != 'undefined' )
		{
			for( var j=0; j < cals_ccmip_mapping.attachments[i].dst_unpacked.length; j++)
			{
				cals_addAsPages( cals_ccmip_mapping.attachments[i].dst_unpacked[j], cals_ccmip_mapping.attachments[i].src_name + "/" );
			}
		}
	}
}

cals_addAttachmentsAsPages();
