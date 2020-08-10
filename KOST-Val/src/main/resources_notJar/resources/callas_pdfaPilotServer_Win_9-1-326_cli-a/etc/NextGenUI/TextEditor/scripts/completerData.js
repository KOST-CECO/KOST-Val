const completerData = [
      {
         "name":"app",
         "description":"Returns the global application object"
      },
      {
         "name":"app.requires(\"key\")",
         "description":"Defines a variable key with default value 0 that is required by the current script.\nExample: app.requires('myvar')"
      },
      {
         "name":"app.requires(\"key\",value)",
         "description":"Defines a variable key and its default value that is required by the current script.\nExample: app.requires('myvar',10)"
      },
      {
         "name":"app.requires(\"key\",value,\"label\")",
         "description":"Defines a variable key and its default value and a display name (label) that is required by the current script.\nExample: app.requires('myvar',10,'Input value for myvar')"
      },
      {
         "name":"app.requires(\"key\",value,\"label\",[<value_list>])",
         "description":"Defines a variable key and its default value and a display name (label) that is required by the current script. Additionally a value list is specified that limits the values that can be entered at runtime. Each element in the value list array musth be either a string or an object containing a value and label property.\nExample: app.requires('myvar',10,'Input value for myvar',[10,20,{value:30,label:\"thirty\"}]);"
      },
      {
         "name":"app.name",
         "description":"Returns the application name."
      },
      {
         "name":"app.version",
         "description":"Returns the application version string."
      },
      {
         "name":"app.vars",
         "description":"Returns the var objects containing all variables defined in the current context."
      },
      {
         "name":"app.vars.varkey",
         "description":"Returns the value of the variable 'varkey' if that exists in app.vars.\nExample: app.vars.varname"
      },
      {
         "name":"app.doc",
         "description":"Retuns the doc object for the current PDF document or 'undefined' if no PDF is open."
      },
      {
         "name":"app.doc.info",
         "description":"Returns the docinfo object containing all document info entries of the current PDF document."
      },
      {
         "name":"app.doc.path",
         "description":"Returns the full platform dependent file path of the current pdf document."
      },
      {
         "name":"app.doc.documentFileName",
         "description":"Returns the file name of the current PDF document."
      },
      {
         "name":"app.doc.numPages",
         "description":"Returns the number of pages of the current PDF document."
      },
      {
         "name":"app.doc.getPageBox()",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the CropBox of the first page in pt."
      },
      {
         "name":"app.doc.getPageBox(pageBox)",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the specified page box of the first page in pt. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".\nExample: app.doc.getPageBox('Trim')"
      },
      {
         "name":"app.doc.getPageBox(pageBox,pageNumber)",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the specified page box of the specified page in pt. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".\nExample: app.doc.getPageBox('Trim',0)"
      },
      {
         "name":"app.doc.getPageBox(pageBox,pageNumber,precision)",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the specified page box of the specified page with the given precision in pt. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".\nExample: app.doc.getPageBox('Trim',0,2)"
      },
      {
         "name":"app.doc.hasPageBox()",
         "description":"Returns true if the CropBox is present otherwise false. \n Example: app.doc.pages[0].hasPageBox()"
      },
      {
         "name":"app.doc.hasPageBox(pageBox)",
         "description":"Returns true if the specified page box of the first page is present otherwise false. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".  \n Example: app.doc.hasPageBox('Trim')"
      },
      {
         "name":"app.doc.hasPageBox(pageBox,pageNumber)",
         "description":"Returns true if the specified page box of the specified page is present otherwise false. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".  \n Example: app.doc.hasPageBox('Trim',0)"
      },
      {
         "name":"app.doc.getPageRotation()",
         "description":"Returns the page rotation of the first page."
      },
      {
         "name":"app.doc.getPageRotation(pageNumber)",
         "description":"Returns the page rotation of the specified page.\nExample: app.doc.getPageRotation(0)"
      },
      {
         "name":"app.doc.pages",
         "description":"Returns an array with page objects for the current PDF document."
      },
      {
         "name":"app.doc.pages[i].inks",
         "description":"Returns an array of inks defined on the page.\n Example: app.doc.pages[0].inks"
      },
      {
         "name":"app.doc.pages[i].inks[j].name",
         "description":"Returns the name of the ink.\n Example: app.doc.pages[0].inks[0].name"
      },
      {
         "name":"app.doc.pages[i].getPageBox()",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the CropBox of the specified page box in pt. \n Example: app.doc.pages[0].getPageBox()"
      },
      {
         "name":"app.doc.pages[i].getPageBox(pageBox)",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the specified page box in pt. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".  \n Example: app.doc.pages[0].getPageBox('Trim')"
      },
      {
         "name":"app.doc.pages[i].getPageBox(pageBox,precision)",
         "description":"Returns an array containing the left, top, right and bottom coordinates of the specified page box with the given precision in pt. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\". \n Example: app.doc.pages[0].getPageBox('Trim',2)"
      },
      {
         "name":"app.doc.pages[i].getPageRotation()",
         "description":"Returns the page rotation of the page."
      },
      {
         "name":"app.doc.pages[i].hasPageBox()",
         "description":"Returns true if the CropBox of the specified page box is present otherwise false. \n Example: app.doc.pages[0].hasPageBox()"
      },
      {
         "name":"app.doc.pages[i].hasPageBox(pageBox)",
         "description":"Returns true if the specified page box is present otherwise false. 'pageBox' must be one of \"Art\", \"Bleed\", \"Crop\", \"Trim\" and  \"Media\".  \n Example: app.doc.pages[0].hasPageBox('Trim')"
      },
      {
         "name":"app.doc.pages[i].userUnit",
         "description":"Returns the user unit value of the page."
      },
      {
         "name":"app.doc.xmp",
         "description":"Returns a XMP object for the document XMP metadata of the current PDF document."
      },
      {
         "name":"app.doc.xmp.getProperty(ns,property)",
         "description":"Returns the value of the specified property in the specified namespace or 'undefined' if the property does not exists. 'ns' must be the full namespace uri. For namespaces defined in the XMP spec the predfeined namespace prefix can be used as well.\n Examples: \n app.doc.xmp.getProperty('http://purl.org/dc/elements/1.1/','format') \n app.doc.xmp.getProperty('dc','format')"
      },
      {
         "name":"app.doc.metadata",
         "description":"Returns the document XMP metada as plain XML"
      },
      {
         "name":"app.doc.result",
         "description":"Returns a preflight result object or 'undefined' if no preflight result is available. A preflight result is only available inside process plans if a profile or check was executed in a previous step."
      },
      {
         "name":"app.doc.result.numErrors",
         "description":"Returns the number of errors of a previous preflight result."
      },
      {
         "name":"app.doc.result.numWarnings",
         "description":"Returns the number of warnings of a previous preflight result."
      },
      {
         "name":"app.doc.result.numInfos",
         "description":"Returns the number of info hits of a previous preflight result."
      },
      {
         "name":"app.doc.result.checks",
         "description":"Returns an array of Check objects for the previous preflight result."
      },
      {
         "name":"app.doc.result.checks.length",
         "description":"Returns the lenght of the array of Check objects for the previous preflight result."
      },
      {
         "name":"app.doc.result.checks[i].id",
         "description":"Returns the check ID of the specified check for the previous preflight result. \n Example: app.doc.result.checks[0].id"
      },
      {
         "name":"app.doc.result.checks[i].customId",
         "description":"Returns the user defined custom ID of the specified check for the previous preflight result.\n Example: app.doc.result.checks[0].customId"
      },
      {
         "name":"app.doc.result.checks[i].name",
         "description":"Returns the display name of the specified check of the previous preflight result.\n Example: app.doc.result.checks[0].name"
      },
      {
         "name":"app.doc.result.checks[i].severity",
         "description":"Returns the severity of the specified check for the previous preflight result: 1: Info, 2: Warning, 3: Error.\n Example: app.doc.result.checks[0].severity"
      },
      {
         "name":"app.doc.result.checks[i].numHits",
         "description":"Returns the number of hits of the specified check for the previous preflight result. \n Example: app.doc.result.checks[0].numHits"
      },
      {
         "name":"app.doc.result.checks[i].pageNumbers",
         "description":"Returns an array of page numbers (starting with 0) for pages that had hits with the specified check for the previous preflight result. \n Example: app.doc.result.checks[0].pageNumbers"
      },
      {
         "name":"app.doc.result.checks[i].conditions",
         "description":"Returns an array of conditions contained in the check."
      },
      {
         "name":"app.doc.result.checks[i].conditions[c].id",
         "description":"Returns the id of the check property used in condition c."
      },
      {
         "name":"app.doc.result.checks[i].conditions[c].name",
         "description":"Returns the localized name of the check property used in condition c."
      },
      {
         "name":"app.doc.result.checks[i].hits",
         "description":"Returns an array of hits produced by the check."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].page",
         "description":"Returns the page number where this hit occurred; undefined if the check property does not refer to a specific page.\""
      },
      {
         "name":"app.doc.result.checks[i].hits[h].triggers",
         "description":"Returns an array of trigger values for the hit."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].triggers[t]",
         "description":"Returns the trigger value for the hit related to app.doc.result.checks[i].conditions[t]."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].triggers[t].value",
         "description":"Returns the trigger value."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].llx",
         "description":"Returns the lower left x coordinate of the bounding box of the snippet in pt."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].lly",
         "description":"Returns the lower left y coordinate of the bounding box of the snippet in pt."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].urx",
         "description":"Returns the upper right x coordinate of the bounding box of the snippet in pt."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].ury",
         "description":"Returns the upper right y coordinate of the bounding box of the snippet in pt."
      },
      {
         "name":"app.doc.result.checks[i].hits[h].type",
         "description":"Returns the type of the snippet. Possible values are \"Fill\", \"Stroke\", \"StrokeFill\", \"TextFill\",\"TextOutline\",\"TextOutlineFill\",\"TextInvisible\",\"InlineImage\",\"XObj\",\"Image\",\"FormXObj\",\"PostScript\" and \"Shade\"."
      },
      {
         "name":"app.doc.result.fixups",
         "description":"Returns an array of Fixup objects for the previous preflight run."
      },
      {
         "name":"app.doc.result.fixups.length",
         "description":"Returns the length of the array of Fixup objects for the previous preflight run."
      },
      {
         "name":"app.doc.result.fixups[i].id",
         "description":"Returns the fixup ID of the specified fixup for the previous preflight result run. \n Example: app.doc.result.fixups[0].id"
      },
      {
         "name":"app.doc.result.fixups[i].customId",
         "description":"Returns the user defined custom ID of the specified fixup for the previous preflight result run. \n Example: app.doc.result.fixups[0].customId"
      },
      {
         "name":"app.doc.result.fixups[i].numSucceeded",
         "description":"Returns the number of fixups that were executed successfully for the specified fixup for the previous preflight result run. \n Example: app.doc.result.fixups[0].numSucceeded"
      },
      {
         "name":"app.doc.result.fixups[i].numFailed",
         "description":"Returns the number of fixups that failed for the specified fixup for the previous preflight result run. \n Example: app.doc.result.fixups[0].numSucceeded"
      },
      {
         "name":"app.doc.pages[i].bleedBox",
         "description":"Returns the BleedBox for page i."
      },
      {
         "name":"app.doc.pages[i].bleedBox.exists",
         "description":"Returns true if a bleed box is present in the page."
      },
      {
         "name":"app.doc.pages[i].bleedBox.left",
         "description":"Returns the left coordinate of the BleedBox for page i."
      },
      {
         "name":"app.doc.pages[i].bleedBox.bottom",
         "description":"Returns the bottom coordinate of the BleedBox for page i."
      },
      {
         "name":"app.doc.pages[i].bleedBox.right",
         "description":"Returns the right coordinate of the BleedBox for page i."
      },
      {
         "name":"app.doc.pages[i].bleedBox.top",
         "description":"Returns the top coordinate of the BleedBox for page i."
      },
      {
         "name":"app.doc.pages[i].cropBox",
         "description":"Returns the CropBox for page i."
      },
      {
         "name":"app.doc.pages[i].cropBox.exists",
         "description":"Returns true if a crop box is present in the page."
      },
      {
         "name":"app.doc.pages[i].cropBox.left",
         "description":"Returns the left coordinate of the CropBox for page i."
      },
      {
         "name":"app.doc.pages[i].cropBox.bottom",
         "description":"Returns the bottom coordinate of the CropBox for page i."
      },
      {
         "name":"app.doc.pages[i].cropBox.right",
         "description":"Returns the right coordinate of the CropBox for page i."
      },
      {
         "name":"app.doc.pages[i].cropBox.top",
         "description":"Returns the top coordinate of the CropBox for page i."
      },
      {
         "name":"app.doc.pages[i].trimBox",
         "description":"Returns the TrimBox for page i."
      },
      {
         "name":"app.doc.pages[i].trimBox.exists",
         "description":"Returns true if a trim box is present in the page."
      },
      {
         "name":"app.doc.pages[i].trimBox.left",
         "description":"Returns the left coordinate of the TrimBox for page i."
      },
      {
         "name":"app.doc.pages[i].trimBox.bottom",
         "description":"Returns the bottom coordinate of the TrimBox for page i."
      },
      {
         "name":"app.doc.pages[i].trimBox.right",
         "description":"Returns the right coordinate of the TrimBox for page i."
      },
      {
         "name":"app.doc.pages[i].trimBox.top",
         "description":"Returns the top coordinate of the TrimBox for page i."
      },
      {
         "name":"app.doc.pages[i].mediaBox",
         "description":"Returns the MediaBox for page i."
      },
      {
         "name":"app.doc.pages[i].mediaBox.exists",
         "description":"Returns true if a media box is present in the page."
      },
      {
         "name":"app.doc.pages[i].mediaBox.left",
         "description":"Returns the left coordinate of the MediaBox for page i."
      },
      {
         "name":"app.doc.pages[i].mediaBox.bottom",
         "description":"Returns the bottom coordinate of the MediaBox for page i."
      },
      {
         "name":"app.doc.pages[i].mediaBox.right",
         "description":"Returns the right coordinate of the MediaBox for page i."
      },
      {
         "name":"app.doc.pages[i].mediaBox.top",
         "description":"Returns the top coordinate of the MediaBox for page i."
      },
      {
         "name":"app.doc.pages[i].artBox",
         "description":"Returns the ArtBox for page i."
      },
      {
         "name":"app.doc.pages[i].artBox.exists",
         "description":"Returns true if an art box is present in the page."
      },
      {
         "name":"app.doc.pages[i].artBox.left",
         "description":"Returns the left coordinate of the ArtBox for page i."
      },
      {
         "name":"app.doc.pages[i].artBox.bottom",
         "description":"Returns the bottom coordinate of the ArtBox for page i."
      },
      {
         "name":"app.doc.pages[i].artBox.right",
         "description":"Returns the right coordinate of the ArtBox for page i."
      },
      {
         "name":"app.doc.pages[i].artBox.top",
         "description":"Returns the top coordinate of the ArtBox for page i."
      },
      {
         "name":"app.doc.pages[i].rotate",
         "description":"Returns the rotate entry for page i."
      },
      {
         "name":"app.doc.result.reports",
         "description":"Returns an array of reports generated by the previous process plan step."
      },
      {
         "name":"app.doc.result.reports[i]",
         "description":"Returns a string containing the full platform path of the report."
      },
      {
         "name":"app.doc.result.resultFiles",
         "description":"Returns an array of result files generated by the previous process plan step."
      },
      {
         "name":"app.doc.result.resultFiles[i]",
         "description":"Returns a string containing the full platform path of the result file."
      },
      {
         "name":"app.doc.result.profile",
         "description":"Returns the name of the current profile (may be undefined in some contexts)"
      },
      {
         "name":"app.doc.result.processplan",
         "description":"Returns the name of the current process plan (may be undefined in some contexts)"
      },
      {
         "name":"app.env",
         "description":"Returns information about the execution environment."
      },
      {
         "name":"app.env.name",
         "description":"Returns the name of the computer the programm is running on."
      },
      {
         "name":"app.env.os",
         "description":"Returns information about the operating system (e.g. \"Mac OS X 10.12.0\")."
      },
      {
         "name":"app.env.pathDelimiter",
         "description":"Returns the path delimiter for the current platform."
      },
      {
         "name":"app.env.platform",
         "description":"Returns the current platform (\"Macintosh\", \"Windows\", \"SunOS\", \"AIX\", \"Linux\")"
      },
      {
         "name":"app.env.user",
         "description":"Returns the login user name."
      },
      {
         "name":"app.env.language",
         "description":"Returns the language code (\"en\", \"de\") "
      },
      {
         "name":"app.env.licenses",
         "description":"Returns an array of license informations."
      },
      {
         "name":"app.env.licenses[i]",
         "description":"Returns information for one license."
      },
      {
         "name":"app.env.licenses[i].full",
         "description":"Returns true if this is a full license."
      },
      {
         "name":"app.env.licenses[i].trial",
         "description":"Returns true if this is a trial license."
      },
      {
         "name":"app.env.licenses[i].timeLimited",
         "description":"Returns true if this is a time limited license."
      },
      {
         "name":"app.env.licenses[i].serialNumber",
         "description":"Returns the serial number of the license."
      },
      {
         "name":"app.env.licenses[i].type",
         "description":"Returns the type of the license. One of \"Main\", \"DeviceLink AddOn\" or \"Dispatcher\""
      },
      {
         "name":"app.env.licenses[i].expirationDate",
         "description":"Returns the expiration date for a time limited license."
      },
      {
         "name":"app.setTimeout(seconds)",
         "description":"Sets the timeout for the current script to a custom value. A timeout value of zero seconds disables the timeout. The default timeout is 60 seconds.\n Example: app.setTimeout(120);"
      },
      {
         "name":"app.http",
         "description":"Returns the applications HTTP object."
      },
      {
         "name":"app.http.get(url)",
         "description":"Returns the response from a HTTP GET request for the given URL, "
      },
      {
         "name":"app.http.get(url, {timeout: 100} )",
         "description":"Returns the response from a HTTP GET request for the given URL with a timeout of 100ms."
      },
      {
         "name":"file = new File(\"path\");",
         "description":"Creates a File object that allows for reading the file's contents. "
      },
      {
         "name":"file.read();",
         "description":"Returns the contents of file as string assuming UTF-8 encoding."
      },
      {
         "name":"file.name",
         "description":"Returns the file name."
      },
      {
         "name":"file.fullName",
         "description":"Returns the full platform path of the file."
      },
      {
         "name":"file.exists",
         "description":"Returns true if the file exists."
      },
      {
         "name":"file.lastModified",
         "description":"Returns a Date object representing the files modification date."
      },
      {
         "name":"xml = new XML(\"xml\");",
         "description":"Creates an XML object from a string."
      },
      {
         "name":"xml.xpath(\"expression\")",
         "description":"Returns an array of objects selected by the XPath expression."
      },
      {
         "name":"xml.registerNamespace(\"prefix\",\"uri\")",
         "description":"Registers a namespace prefix for a given namespace URI."
      }
   ];