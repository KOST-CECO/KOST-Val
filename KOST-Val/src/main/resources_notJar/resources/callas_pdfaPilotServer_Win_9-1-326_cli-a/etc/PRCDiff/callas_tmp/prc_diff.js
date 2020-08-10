prc_diff =

{
	"childs" : 
	{
		"profiles" : 
		[
			
			{
				"childs" : 
				{
					"fixupsets" : 
					[
						
						{
							"childs" : 
							{
								"fixups" : 
								[
									
									{
										"dist" : 0,
										"hasdiff" : true,
										"left" : null,
										"reason" : "added",
										"right" : 
										{
											"comment" : "Makes an invisible copy of all text. This can be used in combination with \"Convert page content into image\" in order to enable text features like copying or searching even for those pages that are converted into images.",
											"features" : 
											[
												
												{
													"comment" : "FEATURE_DuplicateTextAsInvisibleTitle_short",
													"name" : "Create an invisible text copy for all text",
													"params" : 
													[
														
														{
															"label" : "Apply to:",
															"value" : "All"
														}
													]
												}
											],
											"flags" : "DEV-",
											"name" : "Create an invisible text copy for all text"
										},
										"type" : "fixup"
									},
									
									{
										"dist" : 0,
										"hasdiff" : true,
										"left" : null,
										"reason" : "added",
										"right" : 
										{
											"comment" : "Converts all page contents into a CMYK image. JPEG compression with high quality, smoothing of line art, text and images and simulate overprinting will be applied. This Fixup can be combined in one Profile with the \"Create an invisible text copy for all text\"-Fixup to maintain all existing text information.",
											"features" : 
											[
												
												{
													"comment" : "FEATURE_RemoveContentByImageTitle_short",
													"name" : "Convert page content into image",
													"params" : 
													[
														
														{
															"label" : "Image Resolution",
															"value" : "150"
														},
														
														{
															"label" : "Apply to:",
															"value" : "All"
														},
														
														{
															"label" : "Compression:",
															"value" : "JPEGHigh"
														},
														
														{
															"label" : "Colorspace:",
															"value" : "CMYK"
														},
														
														{
															"label" : "Smoothing:",
															"value" : "LineArtTextImages"
														},
														
														{
															"label" : "No thin line heuristics",
															"value" : "0"
														},
														
														{
															"label" : "Simulate overprinting",
															"value" : "1"
														}
													]
												}
											],
											"flags" : "DEV-",
											"name" : "Convert page content into CMYK image (150 ppi, high JPEG quality)"
										},
										"type" : "fixup"
									}
								]
							},
							"dist" : 0,
							"hasdiff" : true,
							"left" : null,
							"reason" : "added",
							"right" : 
							{
								"flags" : "DEV-",
								"name" : "Custom fixups"
							},
							"type" : "fixupset"
						}
					]
				},
				"dist" : 25,
				"left" : 
				{
					"comment" : "",
					"flags" : "-EV-",
					"metadata" : 
					[
						
						{
							"key" : "author",
							"value" : "ufr"
						}
					],
					"name" : "Convert to PDF/A-1b (ISO Uncoated (ECI))",
					"wzchecks" : 
					[
						
						{
							"checks" : 
							[
								
								{
									"name" : "The PDF document uses features that require at least",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "The PDF document is encrypted",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "The PDF document is damaged and needs repair",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Document"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Page size is not 0  by 0  within a tolerance of plus/minus 0 ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Number of pages is  0",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Page size or page orientation is different from page to page",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "At least one page is empty",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Pages"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Resolution of color and grayscale images is lower than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of color and grayscale images is higher than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of bitmap images is lower than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of bitmap images is higher than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images are uncompressed",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images use lossy compression",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images use OPI",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Images"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "When the pages in this document are separated they will generate PDF images on Cyan, Magenta or Yellow plates",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "When the pages in this document are separated they will generate more than 0 spot color plates",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use RGB",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use Device independent color",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use spot colors whose name is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Naming of spot colors is inconsistent",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Colors"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Font is not embedded",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font is embedded as a subset",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is Type 1",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is TrueType",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is CID (Type 1)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is CID (TrueType)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is Type 3",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is OpenType",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "A font is used which is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Fonts"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Transparency used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Custom halftone setting used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Custom transfer curve used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Line thickness is less than 0 points",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Contains embedded PostScript code",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Rendering"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "PDF is not compliant with any of PDF/X-3 (2002)",
									"severity" : 0,
									"severity_string" : "Error"
								},
								
								{
									"name" : "PDF is not compliant with any of PDF/A-1b",
									"severity" : 0,
									"severity_string" : "Error"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Contains a PDF/X Output Intent for an RGB printing condition",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF/X Output Intent is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF contains a PDF/X Output Intent for an RGB printing condition",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Standards compliance"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF/X Output Intent of embedded PDF is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Standards compliance for embedded files"
						}
					],
					"wzconversion" : 
					{
						"OI" : 
						{
							"params" : [ "", "" ],
							"title" : ""
						},
						"PDFA" : 
						{
							"convert" : true,
							"params" : 
							[
								"Apply fixups",
								
								{
									"params" : 
									[
										"Re-convert via PostScript",
										"Convert pages with problems into images",
										"Convert all pages into images",
										"Image resolution: 300ppi"
									],
									"title" : "If regular conversion fails:"
								}
							],
							"title" : "Convert PDF document to PDF/A-1b"
						},
						"PDFE" : 
						{
							"convert" : false,
							"params" : [ "" ],
							"title" : ""
						},
						"PDFX" : 
						{
							"convert" : false,
							"params" : [ "", "", "" ],
							"title" : ""
						},
						"title" : "Convert to standard"
					},
					"wzconversionemb" : 
					{
						"OI" : 
						{
							"params" : [ "", "" ],
							"title" : ""
						},
						"PDFA" : 
						{
							"convert" : false,
							"params" : 
							[
								"",
								
								{
									"params" : [ "", "", "", "" ],
									"title" : "If regular conversion fails:"
								},
								""
							],
							"title" : ""
						},
						"PDFE" : 
						{
							"convert" : false,
							"params" : [ "" ],
							"title" : ""
						},
						"PDFX" : 
						{
							"convert" : false,
							"params" : [ "", "", "" ],
							"title" : ""
						},
						"SRE" : 
						{
							"title" : "Do not convert but set relationship entry (PDF/A-3) to Alternative"
						},
						"title" : "Convert to standard for embedded files"
					}
				},
				"reason" : "changed",
				"right" : 
				{
					"comment" : "Converts all page contents into CMYK images (150 ppi, high JPEG quality) and creates an insible text copy for all text in order to preserve features like copying or searching of text.",
					"flags" : "D-V-",
					"metadata" : 
					[
						
						{
							"key" : "author",
							"value" : "callas software"
						},
						
						{
							"key" : "email",
							"value" : "support@callassoftware.com"
						}
					],
					"name" : "Convert all pages into CMYK images and preserve text information",
					"wzchecks" : 
					[
						
						{
							"checks" : 
							[
								
								{
									"name" : "The PDF document uses features that require at least",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "The PDF document is encrypted",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "The PDF document is damaged and needs repair",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Document"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Page size is not 0  by 0  within a tolerance of plus/minus 0 ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Number of pages is  0",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Page size or page orientation is different from page to page",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "At least one page is empty",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Pages"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Resolution of color and grayscale images is lower than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of color and grayscale images is higher than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of bitmap images is lower than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Resolution of bitmap images is higher than 0 pixels per inch (ppi)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images are uncompressed",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images use lossy compression",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Images use OPI",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Images"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "When the pages in this document are separated they will generate PDF images on Cyan, Magenta or Yellow plates",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "When the pages in this document are separated they will generate more than 0 spot color plates",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use RGB",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use Device independent color",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Objects on the page use spot colors whose name is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Naming of spot colors is inconsistent",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Colors"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Font is not embedded",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font is embedded as a subset",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is Type 1",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is TrueType",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is CID (Type 1)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is CID (TrueType)",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is Type 3",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Font type is OpenType",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "A font is used which is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Fonts"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Transparency used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Custom halftone setting used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Custom transfer curve used",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Line thickness is less than 0 points",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Contains embedded PostScript code",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Rendering"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Contains a PDF/X Output Intent for an RGB printing condition",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF/X Output Intent is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF contains a PDF/X Output Intent for an RGB printing condition",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Standards compliance"
						},
						
						{
							"checks" : 
							[
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "Embedded PDF is not compliant with any of",
									"severity" : 3,
									"severity_string" : "Disabled"
								},
								
								{
									"name" : "PDF/X Output Intent of embedded PDF is in this list ",
									"severity" : 3,
									"severity_string" : "Disabled"
								}
							],
							"name" : "Standards compliance for embedded files"
						}
					],
					"wzconversion" : 
					{
						"OI" : 
						{
							"params" : [ "", "" ],
							"title" : ""
						},
						"PDFA" : 
						{
							"convert" : false,
							"params" : 
							[
								"",
								
								{
									"params" : [ "", "", "", "" ],
									"title" : "If regular conversion fails:"
								}
							],
							"title" : ""
						},
						"PDFE" : 
						{
							"convert" : false,
							"params" : [ "" ],
							"title" : ""
						},
						"PDFX" : 
						{
							"convert" : false,
							"params" : [ "", "", "" ],
							"title" : ""
						},
						"title" : "Convert to standard"
					},
					"wzconversionemb" : 
					{
						"OI" : 
						{
							"params" : [ "", "" ],
							"title" : ""
						},
						"PDFA" : 
						{
							"convert" : false,
							"params" : 
							[
								"",
								
								{
									"params" : [ "", "", "", "" ],
									"title" : "If regular conversion fails:"
								},
								""
							],
							"title" : ""
						},
						"PDFE" : 
						{
							"convert" : false,
							"params" : [ "" ],
							"title" : ""
						},
						"PDFX" : 
						{
							"convert" : false,
							"params" : [ "", "", "" ],
							"title" : ""
						},
						"SRE" : {},
						"title" : "Convert to standard for embedded files"
					}
				},
				"type" : "profile"
			}
		]
	}
}
;