<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/"><!-- kostval.conf.xml_v2.0.0 -->
		<html>
			<head>
				<style>
					body {font-family: Verdana, Geneva, sans-serif; font-size:
					10pt; }
					table {font-family: Verdana, Geneva, sans-serif; font-size:
					10pt; }
					.logow {font-family: Verdana, Geneva, sans-serif;
					background-color: #ffffff; font-weight:bold; font-size: 32pt;
					color: #ffffff; }
					.logoff {font-family: Verdana, Geneva, sans-serif;
					background-color: #ffffff; font-weight:bold; font-size: 18pt;
					color: #000000; }
					.logo {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #ffffff; }
					.logov {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #0cc10c; }
					.logol {font-family: Verdana, Geneva, sans-serif;
					background-color: #000000; font-weight:bold; font-size: 32pt;
					color: #000000; }
					h1 {font-family: Verdana, Geneva, sans-serif;
					font-weight:bold; font-size: 18pt; color: #000000; }
					h2
					{font-family: Verdana, Geneva, sans-serif; font-weight:bold;
					font-size: 14pt; color: #000000; }
					h3 {font-family: Verdana, Geneva,
					sans-serif; font-size: 10pt; color: #808080; }
					.footer {font-family:
					Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
					h4
					{font-family: Verdana, Geneva, sans-serif; font-weight:bold;
					font-size: 10pt; color: #000000; }
					tr
					{background-color: #f0f0f0;}
					tr.caption {background-color: #eeafaf;
					font-weight:bold}
					tr.captionm {background-color: #f8dfdf}
					tr.captionio {background-color: #afeeaf; font-weight:bold}
					tr.captioniom {background-color: #ccffcc}
					tr.captioninfo {background-color: #b2b2c5}
					tr.captioninfom {background-color: #e7e7ed}
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
					<span class="logoff">Konfiguration</span>
				</p>
				<table >
					<tr  class="captioninfo">
						<td>Angaben zur PDF/A-Validierung [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob eine PDF/A-Validierung mit PDF Tools stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob seitens PDF Tools auch detaillierte Fehler in Englisch ausgegeben werden sollen [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob eine PDF/A-Validierung mit callas stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob seitens callas ein Fehler (E) oder eine Warnung (W) ausgegeben werden soll, wenn der N-Eintrag nicht übereinstimmt [W]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/nentry" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, welche PDF/A Versionen erlaubt sind [1A, 1B, 2A, 2B, 2U]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfa1a" />&#160; <xsl:value-of select="configuration/pdfa/pdfa1b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2a" />&#160;<xsl:value-of select="configuration/pdfa/pdfa2b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2u" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob eine Schrift-Validierung (Durchsuch- und Extrahierbarkeit) stattfinden soll [tolerant]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob eine Bild-Validierung (JPEG und JP2) stattfinden soll [no]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfaimage" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob die JBIG2-Komprimierung erlaubt ist [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Angabe, ob eine SIARD-Validierung stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, welche SIARD Versionen erlaubt sind [1.0, 2.1]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siard10" />&#160; <xsl:value-of select="configuration/siard/siard21" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Angabe, ob eine JP2-Validierung stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Angabe, ob eine JPEG-Validierung stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Angabe, ob eine TIFF-Validierung stattfinden soll [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, der erlaubten Komprimierungsalgorithmen [Uncompressed, CCITT 1D, T4/Group 3 Fax, T6/Group 4 Fax, LZW, PackBits]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, der erlaubten Farbräume [WhiteIsZero, BlackIsZero, RGB, RGB Palette]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, der erlaubten Bits per Sample [1, 4, 8, 16]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob Multipage-TIFFs erlaubt sind oder nicht [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob der Aufbau in Kacheln erlaubt ist oder nicht [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Angabe, ob Dateigrössen von 1000MB (~1GB) und grösser erlaubt sind oder nicht [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Angaben zur SIP-Validierung [yes]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Erlaubte maximale Anzahl Zeichen in Pfadlängen [179]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Vorgaben zum Aufbau des SIP-Namens [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3}]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Auflistung der erlaubten Dateiformate [TXT, PDFA1, PDFA2, TIFF, JP2, JPEG, WAVE, MP3, MP4, MJ2, CSV, SIARD, WARC]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedformats" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>