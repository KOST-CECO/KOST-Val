// Template for Thunderbird
// List of displayable header-elements, order inside array = order in template view!

var displayable_headers =
[
	"subject",
	"from",
	"in-reply-to",
	"date",
	"to",
	"cc",
	"bcc",
	"_attachments", //META KEY: Add attachments
	"_srcfiles"  //META KEY: Add src files 
];

/*
	header_settings[KEY].mode
		Display mode for multiple occurences
		1 (default): repeat
		2: use first, omit others
		3: Concatenate (separated by header_settings[KEY].sep)

	header_settings[KEY].fallback
		List of fallbacks if header is not found.
		"**<VALUE>": Add <VALUE> as header value
*/
var header_settings =
{
	"to":{ "mode": 3, "sep":"<span class='cchip_sep'>,</span>" },
	"cc":{ "mode": 3, "sep":"<span class='cchip_sep'>,</span>" },
	"bcc":{ "mode": 3, "sep":"<span class='cchip_sep'>,</span>" },
	"subject":{ "mode": 2 }
};

var bookmarks =
{
	"body":{ "value": "@@subject" },
	"attachments":{ "path": ["___attachments"] },
	"srcfiles":{ "path": ["___srcfiles"] }
};


// Due to known limitations in the HTMLConverter page_size.offsetWidth 
// must match the setting in the css
// A4 portrait: 210mm
// margins 2 x 13mm
var page_size =
{
	"offsetWidth":680.4 // (210mm - 15mm - 15mm) * 3.78 px/mm
};

var settings = {
	"pageScale": true
};

/* Mappings for XMP namespaces
*/
var xmp_schemas =
{
	"xmp":
	{
		"ns": "http://ns.adobe.com/xap/1.0/",
		"prefix": "xmp"
	},
	"dc":
	{
		"ns": "http://purl.org/dc/elements/1.1/",
		"prefix": "dc"
	},
	"xmpMM":
	{
		"ns": "http://ns.adobe.com/xap/1.0/mm/",
		"prefix": "xmpMM"
	},
	"stRef":
	{
		"ns": "http://ns.adobe.com/xap/1.0/sType/ResourceRef#",
		"prefix": "stRef"
	},
	"stEvt":
	{
		"ns": "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#",
		"prefix": "stEvt"
	},
	"ccmip":
	{
		"ns": "http://ns.callassoftware.com/mail/1.0/",
		"prefix": "ccmip"
	}
};

var xmp_headers =
[
	{
		"subject":
		{
			"schema": "dc",
			"property": "title",
			"type": "LangAlt"
		},
		"from":
		{
			"schema": "dc",
			"property": "creator",
			"type": "Seq"
		},
		"date":
		{
			"schema": "xmpMM",
			"property": "History",
			"type": "SeqStruct",
			"struct":
			{
				"name": "ResourceEvent",
				"schema": "stEvt",
				"property": "when",
				"type": "Date",
				"properties": 
				{
					"action": "email_sent",
					"parameters": "Time when original email was sent",
					"softwareAgent": "@@x-mailer"
				}
			}
		},
		"x-mailer":
		{
			"schema": "xmp",
			"property": "CreatorTool",
			"type": "Text"
		},
		"message-id":
		{
			"schema": "xmpMM",
			"property": "DocumentID",
			"type": "Text"
		},
		"in-reply-to":
		{
			"schema": "xmpMM",
			"property": "Ingredients",
			"type": "BagStruct",
			"struct":
			{
				"name": "ResourceRef",
				"schema": "stRef",
				"property": "documentID",
				"type": "Text",
				"properties": 
				{
				}
			}
		},
		"references":
		{
			"schema": "xmpMM",
			"property": "Ingredients",
			"type": "BagStruct",
			"struct":
			{
				"name": "ResourceRef",
				"schema": "stRef",
				"property": "documentID",
				"type": "Text",
				"properties": 
				{
				}
			}
		}
	},
	{
		"subject":{"schema":"ccmip","property":"*","type":"Text"},
		"from":{"schema":"ccmip","property":"*","type":"Text"},
		"return-path":{"schema":"ccmip","property":"*","type":"Text"},
		"date":{"schema":"ccmip","property":"*","type":"Date"},
		"to":{"schema":"ccmip","property":"*","type":"Seq"},
		"cc":{"schema":"ccmip","property":"*","type":"Seq"},
		"x-mailer":{"schema":"ccmip","property":"*","type":"Text"},
		"message-id":{"schema":"ccmip","property":"*","type":"Text"},
		"received":{"schema":"ccmip","property":"*","type":"Seq"},
		"in-reply-to":{"schema":"ccmip","property":"*","type":"Text"},
		"x-envelope-to":{"schema":"ccmip","property":"*","type":"Text"},
		"x-original-authentication-results":{"schema":"ccmip","property":"*","type":"Text"},
		"references":{"schema":"ccmip","property":"*","type":"Text"},
		"reply-to":{"schema":"ccmip","property":"*","type":"Text"},
		"*":{"schema":"ccmip","property":"*","type":"Seq"}
	}
];
