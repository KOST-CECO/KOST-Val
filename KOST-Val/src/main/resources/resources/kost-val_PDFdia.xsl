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
					tr.caption {background-color: #eeafaf; font-weight:bold}
					tr.captionm {background-color: #f8dfdf}
					tr.captionio {background-color: #afeeaf; font-weight:bold}
					tr.captioniom {background-color: #ccffcc}
					tr.captioninfo {background-color: #b2b2c5}
				</style>
			</head>
		<body>
			<p class="logow"><span class="logol">.</span><span class="logo">KOST-</span><span class="logov">V</span><span class="logo">al</span><span class="logol">.</span><span class="logox"> - </span></p>
			<h1>PDF-Diagnosedaten:</h1>
			<xsl:for-each select="KOSTVal_PDF-Diagnose/Validation">
				<div>
					<table width="100%">
						<tr class="caption">
							<td><xsl:value-of select="ValFile"/> (PDF/A<xsl:value-of select="PdfaVL"/>)</td>
						</tr>
					</table>
					<table width="100%">
						<tr class="captionm">
							<td width="24%">PDFTools</td>
							<td width="38%">Kategorie: <xsl:value-of select="PDFTools/iCategory"/></td>
							<td width="38%">1. Error: <xsl:value-of select="PDFTools/iError"/></td>
						</tr>
					</table>
					<table width="100%">
						<tr class="captionm">
							<td width="24%">PDFTron</td>
							<td width="76%">Code: <xsl:value-of select="PDFTron/Code"/></td>
						</tr>
					</table>
				</div>
				<br/>
			</xsl:for-each>
			<br/>
			<br/>
			<hr noshade="noshade" size="1"/>
			<h3>Während der Verwendung der dualen PDF/A-Validierung sammelt KOST-Val bei Uneinigkeit der Validatoren in dieser Datei entsprechende PDF-Diagnosedaten, die für die Verbesserung der Tools und des Standards hilfreich sein können.</h3>
			<h3>Wir sind Ihnen dankbar, wenn Sie diese PDF-Diagnosedatei bei Gelegenheit der KOST-Geschäftsstelle (kost-val@kost-ceco.ch) zusenden könnten.</h3>
		</body>
		</html>
	</xsl:template>
</xsl:stylesheet>