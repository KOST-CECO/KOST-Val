<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<!-- kostval.conf.xml_v2.2.1.1 -->
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
					tr.captioninfow {background-color: #ffffff }
					.captioninfor {color: #ff0000 }
					.captioninfog {color: #0cc10c }
					.captioninfoo {color: #ffa500 }
					tr.captioninfom {font-size: 9pt; background-color: #e7e7ed }
				</style>
			</head>
			<body>
				<table>
					<tr class="captioninfow">
						<td>
							<span class="logol">.</span>
							<span class="logo">KOST-</span>
							<span class="logov">V</span>
							<span class="logo">al</span>
							<span class="logol">&#160;</span>
							<span class="logox">&#160; &#160; &#160;</span>
							<span class="logoff">Configuration</span>
						</td>
						<td>Legend:</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td class="captioninfow">
							<span class="captioninfog">&#x2713;</span>
							<span>&#160;&#160;&#160;&#160;= accepted and validate</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfoo">(&#x2713;)</span>
							<span>&#160;= accepted</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfor">&#x2717;</span>
							<span>&#160;&#160;&#160;&#160;= not accepted</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;&#160;&#160;</td>
					</tr>

					<!-- TEXT -->
					<tr class="captioninfo">
						<td>PDF/A: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>PDF/A validation with PDF Tools [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- PDF Tools also detailed errors in English [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Validation (searchability and extractability) [tolerant]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>PDF/A validation with callas [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Error (E) / warning (W) if N entry does not match [W]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/nentry" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed PDF/A versions [1A, 1B, 2A, 2B, 2U]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfaversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Validate PDF/A-3 to PDF/A-2 and generate warning message instead of error [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/warning3to2" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>JBIG2 compression allowed [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TXT: Acceptance [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/txt/txtvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PDF: Acceptance [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/pdf/pdfvalidation" />
						</td>
					</tr>

					<!-- BILD -->
					<tr class="captioninfo">
						<td>JPEG2000: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JPEG: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TIFF: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed compression algorithms [uncompressed, CCITT 1D, CCITT Group 3, CCITT Group 4, LZW, PackBits]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed color spaces [white is zero,  black is zero, RGB, palette color]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Bits per sample allowed [1, 4, 8, 16]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Multipage TIFFs allowed [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Build in tiles allowed [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>File sizes of 1000MB (~1GB) and larger allowed [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PNG: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/png/pngvalidation" />
						</td>
					</tr>

					<!-- AUDIO -->
					<tr class="captioninfo">
						<td>FLAC: Acceptance [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/flac/flacvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>WAVE: Acceptance [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/wave/wavevalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP3: Acceptance [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp3/mp3validation" />
						</td>
					</tr>

					<!-- VIDEO -->
					<tr class="captioninfo">
						<td>MKV: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mkv/mkvvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Allowed video codec [FFV1, AVC, HEVC, AV1]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvvideo" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Allowed audio codec [FLAC, MP3, AAC]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Silent movie allowed (no audio codec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnoaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Pure audio file allowed (no video codec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnovideo" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP4: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp4/mp4validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Allowed video codec [AVC, HEVC]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4video" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Allowed audio codec [MP3, AAC]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4audio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Silent movie allowed (no audio codec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4noaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Pure audio file allowed (no video codec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4novideo" />
						</td>
					</tr>

					<!-- DATEN -->
					<tr class="captioninfo">
						<td>XML: Acceptance and validation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xml/xmlvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JSON: Acceptance [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/json/jsonvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>SIARD: Acceptance and validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed SIARD versions [1.0, 2.1, 2.2]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siardversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Claim for incorrect file extensions [Error]:</td>
						<td>
							<xsl:value-of select="configuration/siard/lobExtension" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Remarks on non-accepted file formats [Azepted]:</td>
						<td>
							<xsl:value-of select="configuration/siard/lobAzepted" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Validation of accepted file formats [Validate]:</td>
						<td>
							<xsl:value-of select="configuration/siard/lobValidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>CSV: Acceptance [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/csv/csvvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>XLSX: Acceptance [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xlsx/xlsxvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>ODS: Acceptance [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/ods/odsvalidation" />
						</td>
					</tr>

					<!-- SIP -->
					<tr class="captioninfo">
						<td>SIP: Validation [&#x2713;]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed maximum number of characters in path lengths [179]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Specifications for the structure of the SIP name [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\w{3}]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Allowed eCH-0160 SIP versions [1.0, 1.1, 1.2, 1.3]:</td>
						<td>
							<xsl:value-of select="configuration/sip/sipversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Only warning for old documents (Entstehungszeitraum) [no]:</td>
						<td>
							<xsl:value-of select="configuration/sip/warningolddok" />
						</td>
					</tr>

					<!-- SONSTIGES -->
					<tr class="captioninfo">
						<td>Other accepted file formats [WARC, HTML, DWG]:</td>
						<td>
							<xsl:value-of select="configuration/otherformats/docxvalidation" />
							<xsl:value-of select="configuration/otherformats/pptxvalidation" />
							<xsl:value-of select="configuration/otherformats/rtfvalidation" />
							<xsl:value-of select="configuration/otherformats/jpxvalidation" />
							<xsl:value-of select="configuration/otherformats/jpmvalidation" />
							<xsl:value-of select="configuration/otherformats/svgvalidation" />
							<xsl:value-of select="configuration/otherformats/oggvalidation" />
							<xsl:value-of select="configuration/otherformats/mpeg2validation" />
							<xsl:value-of select="configuration/otherformats/avivalidation" />
							<xsl:value-of select="configuration/otherformats/htmlvalidation" />
							<xsl:value-of select="configuration/otherformats/warcvalidation" />
							<xsl:value-of select="configuration/otherformats/arcvalidation" />
							<xsl:value-of select="configuration/otherformats/dwgvalidation" />
							<xsl:value-of select="configuration/otherformats/ifcvalidation" />
							<xsl:value-of select="configuration/otherformats/dxfvalidation" />
							<xsl:value-of select="configuration/otherformats/interlisvalidation" />
							<xsl:value-of select="configuration/otherformats/dicomvalidation" />
							<xsl:value-of select="configuration/otherformats/msgvalidation" />
							<xsl:value-of select="configuration/otherformats/emlvalidation" />
							<xsl:value-of select="configuration/otherformats/othervalidation" />
						</td>
					</tr>
					
					<tr class="captioninfo">
						<td>Signatur in PDF/A- und PDF-Dateien: Prüfung [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/egovdv/egovdvvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Institution, welche die Prüfung durchführt []:</td>
						<td>
							<xsl:value-of select="configuration/egovdv/Institut" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Report, welcher gesichert wird, wenn valide und yes:</td>
						<td></td>
					</tr>
					<tr class="captioninfom">
						<td> - Mixed (unterschiedlichen Zertifikatsklassen)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Mixed" /></td>
					</tr>
					<tr class="captioninfom">
						<td>Mandante, welche geprüft und gesichert werden, wenn valide und yes:</td>
						<td></td>
					</tr>
					<tr class="captioninfom">
						<td> - Qualifizierte Signatur (QES)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Qualified" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - SG-PKI (Swiss Government PKI Signaturen)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/SwissGovPKI" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Urkunde (Signatur einer Urkundspersonen)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Upregfn" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Siegel (geregeltes elektronisches Siegel)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Siegel" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Amtsblatt-Portal (offizielle amtliche Meldungen)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Amtsblattportal" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - eDec (Bundesamtes für Zoll und Grenzsicherheit BAZG)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Edec" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - eSchKG (Betreibungsamt)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/ESchKG" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Bundesrecht (Publikationsplattform des Bundes)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/FederalLaw" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Strafregister (Strafregisterauszüge vom BJ)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/Strafregisterauszug" /></td>
					</tr>
					<tr class="captioninfom">
						<td> - Kt. Zug (Dokumente von den Zuger Verwaltungsbehörden)  [no]:</td>
						<td><xsl:value-of select="configuration/egovdv/Mandant/KantonZugFinanzdirektion" /></td>
					</tr>
					
					<tr class="captioninfo">
						<td>Calculate and output hash value of files. Empty means no calculation and output []:</td>
						<td>
							<xsl:value-of select="configuration/hash" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Issue warning for small files. Empty means no warning []:</td>
						<td>
							<xsl:value-of select="configuration/sizeWarning" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Working directory []:</td>
						<td>
							<xsl:value-of select="configuration/pathtoworkdir" />
						</td>
					</tr>
					<tr class="captioninfo">
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