<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
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
					tr.captioninfo {background-color: #b2b2c5 }
					tr.captioninfom {background-color: #e7e7ed }
				</style>
			</head>
			<body>
				<p class="logow">
					<span class="logol">.</span>
					<span class="logo">KOST-</span>
					<span class="logov">R</span>
					<span class="logo">an</span>
					<span class="logol">.</span>
					<span class="logox"> - </span>
				</p>
				<table width="100%">
				    <tr class="captioninfo">
				      <th></th>
				      <th style="font-style:italic; text-align:center">sel1</th>
				      <th style="font-style:italic; text-align:center">sel2</th>
				      <th style="font-style:italic; text-align:center">sel3</th>
				      <th style="font-style:italic; text-align:center">sel4</th>
				      <th style="font-style:italic; text-align:center">sel5</th>
				      <th style="font-style:italic; text-align:center">sel6</th>
				    </tr>
				    <xsl:for-each select="table/row">
				    <tr class="captioninfom">
				      <td></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel1"/></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel2"/></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel3"/></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel4"/></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel5"/></td>
				      <td style="font-style:italic; text-align:center"><xsl:value-of select="sel6"/></td>
				    </tr>
				    </xsl:for-each>
				  </table>
				<br />
				<hr noshade="noshade" size="1" />
				<h3>
					<xsl:value-of select="table/Infos/Start" />
				</h3>
				<p class="footer">
					<xsl:value-of select="table/Infos/Info" />
				</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>