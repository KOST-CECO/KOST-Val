<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<style>
					body  {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; }
					table {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; }
					.logow	{font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 32pt; color: #ffffff; }
					.logoff	{font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 18pt; color: #000000; }
					.logo	{font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #ffffff; }
					.logov	{font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #0cc10c; }
					.logol	{font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #000000; }
					h1	{font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 18pt; color: #000000; }
					h2	{font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 14pt; color: #000000; }
					h3	{font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
					.footer	{font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
					tr	{background-color: #f0f0f0;}
					tr.caption {background-color: #b2b2c5; font-weight:bold}
					td.captionw {background-color: #ffffff}
					tr.captionm {background-color: #f8dfdf}
					tr.captionio {background-color: #afeeaf; font-weight:bold}
					tr.captioniom {background-color: #ccffcc}
					tr.captioninfo {background-color: #e2e4e2}
					td.captioninfor {background-color: #e2e4e2; text-align: end}
				</style>
			</head>
		<body>
			<p class="logow"><span class="logol">.</span><span class="logo">KOST-</span><span class="logov">V</span><span class="logo">al</span><span class="logol">.</span><span class="logox"> - </span></p>
			<h1>KaD-Diagnosedaten:</h1>
			<xsl:for-each select="KOSTVal_FFCounter">
				<div>
					<table width="94%">
						<tr class="caption">
							<td>Zähler der verschiedenen Dateiformate im content der validierten SIPs</td>
						</tr>
					</table>
					<table width="94%">
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="pdf"/></td>
							<td width="10%">PDF</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="tiff"/></td>
							<td width="10%">TIFF</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="siard"/></td>
							<td width="10%">SIARD</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="txt"/></td>
							<td width="10%">TXT</td>
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="csv"/></td>
							<td width="10%">CSV</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="xml"/></td>
							<td width="10%">XML</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="xsd"/></td>
							<td width="10%">XSD</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="wave"/></td>
							<td width="10%">WAVE</td>
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="mp3"/></td>
							<td width="10%">MP3</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="jp2"/></td>
							<td width="10%">JP2</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="jpx"/></td>
							<td width="10%">JPX</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="jpeg"/></td>
							<td width="10%">JPEG</td>
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="png"/></td>
							<td width="10%">PNG</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="dng"/></td>
							<td width="10%">DNG</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="svg"/></td>
							<td width="10%">SVG</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="mpeg2"/></td>
							<td width="10%">MPEG2</td>
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="mp4"/></td>
							<td width="10%">MP4</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="xls"/></td>
							<td width="10%">XLS</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="odt"/></td>
							<td width="10%">ODT</td>
							<td width="2%" class="captionw">   </td>
							<td width="12%" class="captioninfor"><xsl:value-of select="ods"/></td>
							<td width="10%">ODS</td>
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="odp"/></td>
							<td width="10%">ODP</td>
						</tr>
						<tr class="captioninfo">
						</tr>
						<tr class="captioninfo">
						</tr>
						<tr class="captioninfo">
							<td width="12%" class="captioninfor"><xsl:value-of select="other"/></td>
							<td width="10%">Sonstige</td>
						</tr>
					</table>
				</div>
				<br/>
			</xsl:for-each>
			<br/>
			<br/>
			<hr noshade="noshade" size="1"/>
			<h3>Diese Erhebung anhand der Dateiendung dient der KOST und der PreservationPlanning-Expertengruppe der KOST. Dies bietet die Möglichkeit, zu erfahren welche Dateiformate in welcher Häufigkeit im content-Ordner der validierten SIPs enthalten sind und kann entsprechend der Weiterentwicklung von KOST-Val (zusätzliche Dateiformate) als auch eines systematischen PreservationPlannings mit einem Risikobasierten Ansatz verwendet werden.</h3>
			<h3>Wir sind Ihnen dankbar, wenn Sie diese KaD-Diagnosedatei auf Anfrage der KOST-Geschäftsstelle (kost-val@kost-ceco.ch) zusenden könnten.</h3>
		</body>
		</html>
	</xsl:template>
</xsl:stylesheet>