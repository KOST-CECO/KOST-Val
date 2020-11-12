<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/"><html>
 <head>
  <style>
   body {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; }
   table {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; border: 2px solid #ffffff; border-collapse: collapse}
   .logow {font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 32pt; color: #ffffff; }
   .logoff {font-family: Verdana, Geneva, sans-serif; background-color: #ffffff; font-weight:bold; font-size: 18pt; color: #000000; }
   .logo {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #ffffff; }
   .logov {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #0cc10c; }
   .logol {font-family: Verdana, Geneva, sans-serif; background-color: #000000; font-weight:bold; font-size: 32pt; color: #000000; }
   h1 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 20pt; color: #000000; }
   h2 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 14pt; color: #000000; }
   h3 {font-family: Verdana, Geneva, sans-serif; font-weight:bold; font-size: 10pt; color: #000000; }
   h4 {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
   .footer {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; color: #808080; }
   tr {background-color: #ffffff}
   tr.caption {background-color: #ffffff}
   tr.captionb {background-color: #ffffff; font-weight:bold}
   tr.captionm {background-color: #f8dfdf}
   tr.captionio {background-color: #afeeaf; font-weight:bold}
   tr.captioniom {background-color: #ccffcc}
   tr.captioninfo {background-color: #b2b2c5}
   td {background-color: #ffffff; border: 2px solid #ffffff; border-collapse: collapse}
   td.caption {background-color: #ffffff; border: 2px solid #ffffff; border-collapse: collapse}
   td.captionb {background-color: #ffffff; font-weight:bold}
   td.captionkey {font-weight:bold}
   td.right {text-align: right}
  </style>
 </head>
 <body>
  <p class="logow">
   <span class="logol">.</span>
   <span class="logo">SIARD</span>
   <span class="logov">excerpt</span>
   <span class="logol">&#160;</span>
   <span class="logow">&#160;</span>
   <span class="logoff"> <xsl:value-of select="table/Infos/keyexcerpt"/></span>
  </p>
  <br/>
  <h1>Gebaeudeblatt aus <xsl:value-of select="table/Infos/dbname"/> (<xsl:value-of select="table/Infos/dataOriginTimespan"/>) </h1>
  <br/>
<div><table border="1" class="caption" width="100%">
	<tr><td width="20%">Police Nr:</td><td width="28%"><xsl:value-of select="table/schema0_gv_gebaeude/row/c3"/></td><td width="4%">&#160;</td><td width="20%">Status:</td><td width="28%"><xsl:value-of select="table/schema0_gv_gebaeude/row/c6"/></td></tr>
	<tr><td>Grundstueck Nr:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c2"/></td><td>&#160;</td><td>Baujahr:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c9"/></td></tr>
	<tr><td>Gemeinde:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c17"/> (<xsl:value-of select="table/schema0_gv_gebaeude/row/c14"/>)</td><td>&#160;</td><td>Zweck:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c7"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c8"/></td></tr>
	<tr><td>Strasse:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c18"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c19"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c20"/></td><td>&#160;</td><td>Bauart:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c11"/></td></tr>
	<tr><td>PLZ Ort:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c21"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c22"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c23"/>&#160;<xsl:value-of select="table/schema0_gv_gebaeude/row/c24"/></td><td>&#160;</td><td>Lage:</td><td><xsl:value-of select="table/schema0_gv_gebaeude/row/c13"/></td></tr>
</table></div>
  <br/>
<div><table border="1" class="caption" width="100%">
	<xsl:for-each select="table/schema0_gv_person/row">
		<tr><td width="20%"><xsl:value-of select="c18"/>:</td><td><xsl:value-of select="c6"/>&#160;<xsl:value-of select="c7"/>&#160;<xsl:value-of select="c8"/>&#160;<xsl:value-of select="c9"/>&#160;<xsl:value-of select="c11"/>&#160;<xsl:value-of select="c12"/>&#160;<xsl:value-of select="c13"/>&#160;<xsl:value-of select="c14"/>&#160;<xsl:value-of select="c10"/></td></tr>
		<tr><td>&#160;</td><td>Gueltig ab:&#160;<xsl:value-of select="c15"/></td></tr>
	<tr><td>&#160;</td><td>Gueltig bis:&#160;<xsl:value-of select="c16"/></td></tr>
	<tr><td>&#160;</td><td>&#160;</td></tr>
	</xsl:for-each>
</table></div>
  <br/>
<div><table border="1" class="caption" width="100%">
	<xsl:for-each select="table/schema0_gv_anlage/row">
		<tr><td width="20%">Brandschutzanlage&#160;<xsl:value-of select="c1"/>:</td><td><xsl:value-of select="c3"/>&#160;<xsl:value-of select="c4"/></td></tr>
		<tr><td>&#160;</td><td>&#160;</td></tr>
	</xsl:for-each>
</table></div>
  <br/>
<div><h2>Schaetzung:</h2><table border="1" class="caption" width="100%">
	<xsl:for-each select="table/schema0_gv_schaetzung/row">
		<tr><td width="20%">Schaetzungsnummer:</td><td  width="28%"><xsl:value-of select="c1"/></td><td width="4%">&#160;</td><td width="20%">Erledigungsdatum:</td><td  width="28%"><xsl:value-of select="c9"/></td></tr>
		<tr><td>Schaetzungsdatum:</td><td><xsl:value-of select="c3"/></td><td>&#160;</td><td>Versicherungsart:</td><td><xsl:value-of select="c14"/>&#160;<xsl:value-of select="c15"/></td></tr>
		<tr><td>Schaetzungsgrund:</td><td><xsl:value-of select="c4"/>&#160;<xsl:value-of select="c5"/></td><td>&#160;</td><td>Versicherungswert aktuell:</td><td><xsl:value-of select="c16"/></td></tr>
		<tr><td>Dispostatus:</td><td><xsl:value-of select="c10"/>&#160;<xsl:value-of select="c11"/></td><td>&#160;</td><td>Versicherungswert alt:</td><td><xsl:value-of select="c17"/></td></tr>
		<tr><td>Ereignis:</td><td><xsl:value-of select="c12"/>&#160;<xsl:value-of select="c13"/></td><td>&#160;</td><td>Basiswert:</td><td><xsl:value-of select="c18"/></td></tr>
		<tr><td>Wertvermehrende Investition:</td><td><xsl:value-of select="c6"/></td><td>&#160;</td><td>Gebaeudevolumen m3:</td><td><xsl:value-of select="c19"/></td></tr>
		<tr><td>Kommentar:</td><td><xsl:value-of select="c7"/>&#160;<xsl:value-of select="c8"/></td></tr>
		<tr><td>&#160;</td><td>&#160;</td></tr>
	</xsl:for-each>
</table></div>
  <br/>
<div><h2>Schaden:</h2><table border="1" class="caption" width="100%">
	<xsl:for-each select="table/schema0_gv_schaden/row">
		<tr><td width="20%">Schadendatum:</td><td  width="28%"><xsl:value-of select="c4"/></td><td width="4%">&#160;</td><td width="20%">Erledigungsdatum:</td><td  width="28%"><xsl:value-of select="c5"/></td></tr>
		<tr><td>Schadenart:</td><td><xsl:value-of select="c6"/>&#160;<xsl:value-of select="c7"/></td><td>&#160;</td><td>Schadenursache:</td><td><xsl:value-of select="c8"/>&#160;<xsl:value-of select="c9"/>&#160;<xsl:value-of select="c10"/></td></tr>
		<tr><td>Schadensumme:</td><td><xsl:value-of select="c11"/></td><td>&#160;</td><td>Schadenstatus:</td><td><xsl:value-of select="c12"/>&#160;<xsl:value-of select="c13"/></td></tr>
		<tr><td>Versicherungswert:</td><td><xsl:value-of select="c14"/></td><td>&#160;</td><td>VKF:</td><td><xsl:value-of select="c15"/>&#160;<xsl:value-of select="c16"/></td></tr>
		<tr><td>&#160;</td><td>&#160;</td></tr>
	</xsl:for-each>
</table></div>
<br/><hr noshade="noshade" size="1" /><br/><p class="footer">Dieser Record stammt vom <xsl:value-of select="table/Infos/Start" /> aus dem <xsl:value-of select="table/Infos/Archive" />.</p><p class="footer"><xsl:value-of select="table/Infos/Info" /></p><br/>
</body></html></xsl:template></xsl:stylesheet>
