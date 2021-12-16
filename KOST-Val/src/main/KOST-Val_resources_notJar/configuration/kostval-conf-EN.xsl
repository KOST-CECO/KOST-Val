<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/"><!-- kostval.conf.xml_v2.1.1.0 -->
		<html>
			<head>
				<style>
				    body {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; -webkit-print-color-adjust: exact; }
					table {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; -webkit-print-color-adjust: exact; }
					.logow {font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 32pt; color: #ffffff; -webkit-print-color-adjust: exact; }
					.logoff {font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 18pt; color: #000000; -webkit-print-color-adjust: exact; }
					.logo {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #ffffff; -webkit-print-color-adjust: exact; }
					.logov {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #0cc10c; -webkit-print-color-adjust: exact; }
					.logol {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #000000; -webkit-print-color-adjust: exact; }
					h1 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 18pt; color: #000000; -webkit-print-color-adjust: exact; }
					h2 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 14pt; color: #000000; -webkit-print-color-adjust: exact; }
					h3 {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; -webkit-print-color-adjust: exact; }
					.footer {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; -webkit-print-color-adjust: exact; }
					h4 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 10pt; color: #000000; -webkit-print-color-adjust: exact; }
					tr {background-color: #f0f0f0; -webkit-print-color-adjust: exact; }
					tr.caption {background-color: #eeafaf; font-weight:bold }
					tr.captionm {background-color: #f8dfdf }
					tr.captionio {background-color: #afeeaf; font-weight:bold }
					tr.captioniom {background-color: #ccffcc }
					tr.captioninfo {background-color: #b2b2c5 }
					tr.captioninfom {background-color: #e7e7ed }
				</style>
			</head>
			<body>
				<p class="logow">
					<span class="logol">.</span>
					<span class="logo">KOST-</span>
					<span class="logov">V</span>
					<span class="logo">al</span>
					<span class="logol">.</span>
					<span class="logox"> - </span>
					<span class="logoff">Configuration</span>
				</p>
				<table >
					<tr  class="captioninfo">
						<td>Specifies whether a JPEG validation should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Specifies whether a JP2 validation should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Specifies whether a PNG validation should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/png/pngvalidation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Specifies whether a SIARD validation should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies which SIARD versions are allowed [1.0, 2.1]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siard10" />&#160; <xsl:value-of select="configuration/siard/siard21" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Information about PDF/A validation [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether a PDF/A validation with PDF Tools should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether PDF Tools should also output detailed errors in English [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether a font validation (search and extractability) should take place [tolerant]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether a PDF/A validation with callas should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether callas should issue an error (E) or a warning (W) if the N entry does not match [W]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/nentry" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies which PDF/A versions are allowed [1A, 1B, 2A, 2B, 2U]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfa1a" />&#160; <xsl:value-of select="configuration/pdfa/pdfa1b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2a" />&#160;<xsl:value-of select="configuration/pdfa/pdfa2b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2u" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether JBIG2 compression is allowed [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Specifies whether a TIFF validation should take place [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies the permitted compression algorithms [Uncompressed, CCITT 1D, T4/Group 3 Fax, T6/Group 4 Fax, LZW, PackBits]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specification of the allowed color spaces [WhiteIsZero, BlackIsZero, RGB, RGB Palette]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies the number of bits allowed per sample [1, 4, 8, 16]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether multipage TIFFs are allowed or not [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether the construction in tiles is allowed or not [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifies whether file sizes of 1000MB (~1GB) and larger are allowed or not [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>SIP validation details [yes]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Allowed maximum number of characters in path lengths [179]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Specifications for the structure of the SIP name [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3}]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>AListing of the allowed file formats [TXT, PDFA1, PDFA2, TIFF, JP2, JPEG, PNG, WAVE, MP3, MP4, MJ2, CSV, SIARD, WARC]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedformats" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Working directory []:</td>
						<td>
							<xsl:value-of select="configuration/pathtoworkdir" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Input directory []:</td>
						<td>
							<xsl:value-of select="configuration/standardinputdir" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>