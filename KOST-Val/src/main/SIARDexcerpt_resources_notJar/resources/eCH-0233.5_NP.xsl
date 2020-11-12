<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<style>
			body {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; }
			table {font-family: Verdana, Geneva, sans-serif; font-size: 10pt; }
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
			tr 	{background-color: #d8d8d8}
			tr.caption {background-color: #ffffff}
			tr.captionb {background-color: #ffffff; font-weight:bold}
			tr.captionm {background-color: #f8dfdf}
			tr.captionio {background-color: #afeeaf; font-weight:bold}
			tr.captioniom {background-color: #ccffcc}
			tr.captioninfo {background-color: #b2b2c5}
			td.caption {background-color: #ffffff}
			td.right {text-align: right}
				</style>
			</head>
			<body>
				<xsl:for-each select="table/tax0_taxDeclarationMainForm/row">
					<h1>Veranlagungsdaten&#xA0;<xsl:value-of select="c1" />&#xA0;von&#xA0;<xsl:value-of select="c9" />&#xA0;<xsl:value-of select="c8" />&#xA0;(<xsl:value-of select="c13" />):
					</h1>
				</xsl:for-each>
				<div>
					<xsl:for-each select="table/tax0_taxDeclarationMainForm/row">
						<table width="100%">
							<tr class="caption">
								<td width="33%">Steuerjahr:</td>
								<td width="33%">Steuerperiode Von:</td>
								<td width="34%">Bis:</td>
							</tr>
							<tr>
								<td><xsl:value-of select="c1" /></td>
								<td><xsl:value-of select="c2" /></td>
								<td><xsl:value-of select="c3" /></td>
							</tr>
						</table>
						<table width="100%">
							<tr class="caption">
								<td width="33%">Status:</td>
								<td width="33%">Quelle der Daten:</td>
								<td width="34%">Letzte Steuergemeinde:</td>
							</tr>
							<tr>
								<td>
									<xsl:choose >
										<xsl:when test="c4 = 0">Abgeschlossen</xsl:when>
										<xsl:when test="c4 = 1">Offen</xsl:when>
										<xsl:when test="c4 = 2">Unbekannt</xsl:when>
									</xsl:choose>
								</td>
								<td>
									<xsl:choose >
										<xsl:when test="c5 = 0">Software</xsl:when>
										<xsl:when test="c5 = 1">Barcode-Scanning</xsl:when>
										<xsl:when test="c5 = 2">OCR-Scanning</xsl:when>
									</xsl:choose>
								</td>
								<td><xsl:value-of select="c6" />&#xA0;<xsl:value-of select="c7" /></td>
							</tr>
						</table>
						<br />
						<h2>Personalien:</h2>
						<table width="100%">
							<tr class="captionb">
								<td width="33%" class="caption">&#xA0;</td>
								<td width="33%">P1 (Ehemann/Einzelperson)</td>
								<td width="34%">P2 (Ehefrau)</td>
							</tr>
						</table>
						<table width="100%">
							<tr>
								<td width="33%" class="caption">Name:</td>
								<td width="33%"><xsl:value-of select="c8" /></td>
								<td width="34%"><xsl:value-of select="c31" /></td>
							</tr>
							<tr>
								<td class="caption">Vorname(n):</td>
								<td><xsl:value-of select="c9" /></td>
								<td><xsl:value-of select="c32" /></td>
							</tr>
							<tr>
								<td class="caption">Ledigname:</td>
								<td><xsl:value-of select="c10" /></td>
								<td><xsl:value-of select="c33" /></td>
							</tr>
							<tr>
								<td class="caption">Geschlecht:</td>
								<td><xsl:choose >
										<xsl:when test="c11 = 1">männlich</xsl:when>
										<xsl:when test="c11 = 2">weiblich</xsl:when>
										<xsl:when test="c11 = 3">unbestimmt</xsl:when>
									</xsl:choose></td>
								<td><xsl:choose >
										<xsl:when test="c34 = 1">männlich</xsl:when>
										<xsl:when test="c34 = 2">weiblich</xsl:when>
										<xsl:when test="c34 = 3">unbestimmt</xsl:when>
									</xsl:choose></td>
							</tr>
							<tr>
								<td class="caption">Geburtsdatum:</td>
								<td><xsl:value-of select="c12" /></td>
								<td><xsl:value-of select="c35" /></td>
							</tr>
							<tr>
								<td class="caption">Steuer-ID:</td>
								<td><xsl:value-of select="c13" /></td>
								<td class="caption">&#xA0;</td>
							</tr>
							<tr>
								<td class="caption">AHV-Nummer:</td>
								<td><xsl:value-of select="c14" /></td>
								<td><xsl:value-of select="c36" /></td>
							</tr>
							<tr>
								<td class="caption">Alte AHV-Nummer:</td>
								<td><xsl:value-of select="c15" /></td>
								<td><xsl:value-of select="c37" /></td>
							</tr>
							<tr>
								<td class="caption">PID-Nummer:</td>
								<td><xsl:value-of select="c16" /></td>
								<td><xsl:value-of select="c38" /></td>
							</tr>
							<tr>
								<td class="caption">UID:</td>
								<td><xsl:value-of select="c17" /></td>
								<td><xsl:value-of select="c39" /></td>
							</tr>
							<tr>
								<td class="caption">Sonstige Identifikation:</td>
								<td><xsl:value-of select="c18" /></td>
								<td><xsl:value-of select="c40" /></td>
							</tr>
							<tr>
								<td class="caption">Strasse Nr.:</td>
								<td><xsl:value-of select="c19" />&#xA0;<xsl:value-of select="c20" /></td>
								<td><xsl:value-of select="c41" />&#xA0;<xsl:value-of select="c42" /></td>
							</tr>
							<tr>
								<td class="caption">Ländercode PLZ Ort:</td>
								<td><xsl:value-of select="c21" />&#xA0;<xsl:value-of select="c22" /></td>
								<td><xsl:value-of select="c43" />&#xA0;<xsl:value-of select="c44" /></td>
							</tr>
							<tr>
								<td class="caption">Zivilstand:</td>
								<td><xsl:choose >
										<xsl:when test="c23 = 1">ledig</xsl:when>
										<xsl:when test="c23 = 2">verheiratet</xsl:when>
										<xsl:when test="c23 = 3">verwitwet</xsl:when>
										<xsl:when test="c23 = 4">geschieden</xsl:when>
										<xsl:when test="c23 = 5">unverheiratet (wegen Ungültigkeitserklärung einer Ehe)</xsl:when>
										<xsl:when test="c23 = 6">in eingetragener Partnerschaft</xsl:when>
										<xsl:when test="c23 = 7">aufgelöste Partnerschaft</xsl:when>
										<xsl:when test="c23 = 9">unbekannt</xsl:when>
										<xsl:when test="c23 = 99">getrennt</xsl:when>
									</xsl:choose></td>
								<td><xsl:choose >
										<xsl:when test="c45 = 1">ledig</xsl:when>
										<xsl:when test="c45 = 2">verheiratet</xsl:when>
										<xsl:when test="c45 = 3">verwitwet</xsl:when>
										<xsl:when test="c45 = 4">geschieden</xsl:when>
										<xsl:when test="c45 = 5">unverheiratet (wegen Ungültigkeitserklärung einer Ehe)</xsl:when>
										<xsl:when test="c45 = 6">in eingetragener Partnerschaft</xsl:when>
										<xsl:when test="c45 = 7">aufgelöste Partnerschaft</xsl:when>
										<xsl:when test="c45 = 9">unbekannt</xsl:when>
										<xsl:when test="c45 = 99">getrennt</xsl:when>
									</xsl:choose></td>
							</tr>
							<tr>
								<td class="caption">Konfession:</td>
								<td><xsl:choose >
										<xsl:when test="c24 = 111">evangelisch-reformierte (protestantische) Kirche </xsl:when>
										<xsl:when test="c24 = 121">römisch-katholische Kirche </xsl:when>
										<xsl:when test="c24 = 122">christkatholische / altkatholische Kirche </xsl:when>
										<xsl:when test="c24 = 211">israelitische Gemeinschaft / jüdische Glaubensgemeinschaft </xsl:when>
										<xsl:when test="c24 = 211201">Israelitische Cultusgemeinde </xsl:when>
										<xsl:when test="c24 = 211301">Jüdisch Liberale Gemeinde </xsl:when>
										<xsl:when test="c24 = 998">andere Religionen </xsl:when>
										<xsl:when test="c24 = 999">konfessionslos </xsl:when>
										<xsl:when test="c24 = 000">Unbekannt</xsl:when>
									</xsl:choose></td>
								<td><xsl:choose >
										<xsl:when test="c46 = 111">evangelisch-reformierte (protestantische) Kirche </xsl:when>
										<xsl:when test="c46 = 121">römisch-katholische Kirche </xsl:when>
										<xsl:when test="c46 = 122">christkatholische / altkatholische Kirche </xsl:when>
										<xsl:when test="c46 = 211">israelitische Gemeinschaft / jüdische Glaubensgemeinschaft </xsl:when>
										<xsl:when test="c46 = 211201">Israelitische Cultusgemeinde </xsl:when>
										<xsl:when test="c46 = 211301">Jüdisch Liberale Gemeinde </xsl:when>
										<xsl:when test="c46 = 998">andere Religionen </xsl:when>
										<xsl:when test="c46 = 999">konfessionslos </xsl:when>
										<xsl:when test="c46 = 000">Unbekannt</xsl:when>
									</xsl:choose></td>
							</tr>
							<tr>
								<td class="caption">Beruf:</td>
								<td><xsl:value-of select="c25" /></td>
								<td><xsl:value-of select="c47" /></td>
							</tr>
							<tr>
								<td class="caption">Arbeitgeber:</td>
								<td><xsl:value-of select="c26" /></td>
								<td><xsl:value-of select="c48" /></td>
							</tr>
							<tr>
								<td class="caption">Arbeitsort:</td>
								<td><xsl:value-of select="c27" /></td>
								<td><xsl:value-of select="c49" /></td>
							</tr>
							<tr>
								<td class="caption">Zahlung 2. Saeule:</td>
								<td><xsl:value-of select="c28" /></td>
								<td><xsl:value-of select="c50" /></td>
							</tr>
							<tr>
								<td class="caption">Steuergemeinde:</td>
								<td><xsl:value-of select="c29" />&#xA0;<xsl:value-of select="c30" /></td>
								<td><xsl:value-of select="c51" />&#xA0;<xsl:value-of select="c52" /></td>
							</tr>
						</table>
					</xsl:for-each>
					<br />
					<h2>Kinder:</h2>
					<table width="100%">
						<tr class="caption">
							<td>1: Name</td>
							<td>2: Geschlecht</td>
							<td>3: Geburtsdatum</td>
							<td>4: Extern</td>
						</tr>
						<tr class="caption">
							<td>5: Unterhaltsbeitraege</td>
							<td>6: Adresse</td>
							<td>7: ID (AHV, PID...)</td>
							<td>8: Schule oder Lehrfirma</td>
						</tr>
					</table>
					<table width="100%">
						<tr class="caption">
							<td>1:</td>
							<td>2:</td>
							<td>3:</td>
							<td>4:</td>
							<td>5:</td>
							<td>6:</td>
							<td>7:</td>
							<td>8:</td>
						</tr>
						<xsl:for-each select="table/tax0_childData/row">
							<tr>
								<td><xsl:value-of select="c3"/>&#xA0;<xsl:value-of select="c2"/>&#xA0;<xsl:value-of select="c4"/></td>
								<td><xsl:choose >
										<xsl:when test="c5 = 1">männlich</xsl:when>
										<xsl:when test="c5 = 2">weiblich</xsl:when>
										<xsl:when test="c5 = 3">unbestimmt</xsl:when>
									</xsl:choose></td>
								<td><xsl:value-of select="c6"/></td>
								<td><xsl:value-of select="c15"/></td>
								<td><xsl:value-of select="c17"/></td>
								<td><xsl:value-of select="c11"/>&#xA0;<xsl:value-of select="c12"/>&#xA0;<xsl:value-of select="c13"/>&#xA0;<xsl:value-of select="c14"/></td>
								<td><xsl:value-of select="c7"/>&#xA0;<xsl:value-of select="c8"/>&#xA0;<xsl:value-of select="c9"/>&#xA0;<xsl:value-of select="c10"/></td>
								<td><xsl:value-of select="c16"/></td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
					<h2>Unterstuetzungsbeduerftige Personen:</h2>
					<table width="100%">
						<tr class="caption">
							<td>1: Name</td>
							<td>2: Geschlecht</td>
							<td>3: Geburtsdatum</td>
							<td>4: Extern</td>
						</tr>
						<tr class="caption">
							<td>5: Adresse</td>
							<td>6: ID (AHV, PID...)</td>
							<td>7: Unterstuetzungsbeitrag</td>
						</tr>
					</table>
					<table width="100%">
						<tr class="caption">
							<td>1:</td>
							<td>2:</td>
							<td>3:</td>
							<td>4:</td>
							<td>5:</td>
							<td>6:</td>
							<td>7:</td>
						</tr>
						<xsl:for-each select="table/tax0_disabledPersonSupport/row">
							<tr>
								<td><xsl:value-of select="c3"/>&#xA0;<xsl:value-of select="c2"/>&#xA0;<xsl:value-of select="c4"/></td>
								<td><xsl:choose >
										<xsl:when test="c5 = 1">männlich</xsl:when>
										<xsl:when test="c5 = 2">weiblich</xsl:when>
										<xsl:when test="c5 = 3">unbestimmt</xsl:when>
									</xsl:choose></td>
								<td><xsl:value-of select="c6"/></td>
								<td><xsl:value-of select="c15"/></td>
								<td><xsl:value-of select="c11"/>&#xA0;<xsl:value-of select="c12"/>&#xA0;<xsl:value-of select="c13"/>&#xA0;<xsl:value-of select="c14"/></td>
								<td><xsl:value-of select="c7"/>&#xA0;<xsl:value-of select="c8"/>&#xA0;<xsl:value-of select="c9"/>&#xA0;<xsl:value-of select="c10"/></td>
								<td class="right"><xsl:value-of select="c16"/></td>
							</tr>
						</xsl:for-each>
						<tr class="caption">
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td>Ja</td>
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c56" /></td>
						</tr>
						<tr class="caption">
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td>Nein</td>
							<td>&#xA0;</td>
							<td>&#xA0;</td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c58" /></td>
						</tr>
					</table>
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Einkuenfte P1 P2:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="65" class="caption">Ziffer</td>
							<td width="120">Einkuenfte P1 Fr.</td>
							<td width="120">Einkuenfte P2 Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Unselbstaendiger Haupterwerb:</td>
							<td width="65" class="caption">100/101:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c59" /></td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c60" /></td>
						</tr>
						<tr>
							<td class="caption">Unselbstaendiger Nebenerwerb:</td>
							<td class="caption">102/103:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c61" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c62" /></td>
						</tr>
						<tr>
							<td class="caption">Selbstaendiger Haupterwerb:</td>
							<td class="caption">120/121:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c63" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c64" /></td>
						</tr>
						<tr>
							<td class="caption">Selbstaendiger Nebenerwerb:</td>
							<td class="caption">122/123:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c65" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c66" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">AHV- / IV-Renten:</td>
										<td class="caption">Type P1</td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c67" /></td>
										<td class="caption">Type P2</td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c68" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">130/131:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c69" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c70" /></td>
						</tr>
						<tr>
							<td class="caption">Rente Pension 1:</td>
							<td class="caption">134/136:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c71" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c73" /></td>
						</tr>
						<tr>
							<td class="caption">Rente Pension 2:</td>
							<td class="caption">135/137:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c72" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c74" /></td>
						</tr>
						<tr>
							<td class="caption">Erwerbsausfallentschaedigung:</td>
							<td class="caption">140/141:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c75" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c76" /></td>
						</tr>
						<tr>
							<td class="caption">Kinder- und Familienzulagen:</td>
							<td class="caption">142/143:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c77" /></td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c78" /></td>
						</tr>
					</table>
					<br />
					<h2>Weitere Einkuenfte:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="45" class="caption">Ziffer</td>
							<td width="120">Einkuenfte Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Ertrag aus Wertschriften:</td>
							<td width="45" class="caption">150:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c79" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Anteil qualifizierte Beteiligungen:</td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c80" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">151:</td>
							<td class="caption">&#xA0;</td>
						</tr>
						<tr>
							<td class="caption">Unterhaltsbeitraege von getrenntem Partner:</td>
							<td class="caption">160:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c81" /></td>
						</tr>
						<tr>
							<td class="caption">Unterhaltsbeitraege fuer Kinder:</td>
							<td class="caption">161:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c82" /></td>
						</tr>
						<tr>
							<td class="caption">Ertrag aus unverteilten Erbschaften, Geschaefts-, Korporationsanteilen:</td>
							<td class="caption">162:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c83" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Weitere Einkuenfte, Bezeichnung:</td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c84" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">163:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c85" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Kapitalabfindungen fuer </td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c86" /></td>
										<td class="caption"> Monate:</td>
									</tr>
									<tr>
										<td width="50%" class="caption">&#xA0; </td>
										<td class="caption">Naehere Bezeichnung: </td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c88" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">164:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c87" /></td>
						</tr>
						<tr>
							<td class="caption">
						Totalertrag aus allen Liegenschaften:
							</td>
							<td class="caption">186 + 188:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c94" /></td>
						</tr>
						<tr>
							<td class="caption">Total der Einkuenfte</td>
							<td class="caption">199:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c95" /></td>
						</tr>				
					</table>
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Abzuege:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="45" class="caption">Ziffer</td>
							<td width="120">Abzuege Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Berufsauslagen unselbstaendig P1:</td>
							<td width="45" class="caption">220:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c96" /></td>
						</tr>
						<tr>
							<td class="caption">Berufsauslagen unselbstaendig P2:</td>
							<td class="caption">240:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c97" /></td>
						</tr>
						<tr>
							<td class="caption">Schuldzinsen:</td>
							<td class="caption">250:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c98" /></td>
						</tr>
						<tr>
							<td class="caption">Unterhaltsbeitraege an den geschiedenen oder getrennt lebenden Partner:</td>
							<td class="caption">254:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c99" /></td>
						</tr>
						<tr>
							<td class="caption">Unterhaltsbeitraege fue minderjaehrige Kinder:</td>
							<td class="caption">255:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c100" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Rentenleistungen:</td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c102" /></td>
										<td class="caption">Abzugsfaehig:</td>
									</tr>
								</table>
							</td>
							<td class="caption">256:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c101" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Beitraege an 3a P1:</td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c104" /></td>
										<td class="caption">Abzugsfaehig:</td>
									</tr>
								</table>
							</td>
							<td class="caption">260:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c103" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Beitraege an 3a P2:</td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c106" /></td>
										<td class="caption">Abzugsfaehig:</td>
									</tr>
								</table>
							</td>
							<td class="caption">261:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c105" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Versicherungspraemien, Zinsen von Sparkapitalien:</td>
									</tr>
								</table>
								<table width="100%">
									<tr class="caption">
										<td>A.1: Private Krankenversicherung</td>
										<td>A.2: Private Unfallversicherung</td>
									</tr>
									<tr class="caption">
										<td>A.3: Private Lebens- und Rentenversicherung</td>
										<td>A.4: Zinsen von Sparkapitalien</td>
									</tr>
									<tr class="caption">
										<td>A.5: Zwischentotal</td>
										<td>A.6: Abzueglich Praemienverbilligung</td>
									</tr>
									<tr class="caption">
										<td>A: Total bezahlte Versicherungspraemien und Zinsen</td>
									</tr>
								</table>
								<table width="100%">
									<tr class="caption">
										<td>A.1:</td>
										<td>A.2:</td>
										<td>A.3:</td>
										<td>A.4:</td>
										<td>A.5:</td>
										<td>A.6:</td>
										<td>A:</td>
									</tr>
									<tr>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c132" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c133" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c134" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c135" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c136" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c137" /></td>
										<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c138" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">270:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c107" /></td>
						</tr>
						<tr>
							<td class="caption">Beitraege an die AHV, IV und 2. Saeule:</td>
							<td class="caption">280:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c108" /></td>
						</tr>
						<tr>
							<td class="caption">Beitraege an politische Parteien:</td>
							<td class="caption">281:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c112" /></td>
						</tr>
						<tr>
							<td class="caption">Kosten fuer die Verwaltung des Privatvermoegens:</td>
							<td class="caption">283:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c110" /></td>
						</tr>
						<tr>
							<td class="caption">Behinderungsbedingte Kosten:</td>
							<td class="caption">3160:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c111" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Weitere Abzuege, Bezeichnung:</td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c113" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">284:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c114" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug fuer fremdbereute Kinder:</td>
							<td class="caption">376:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c115" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug Erwerbstaetigkeit beide Partner:</td>
							<td class="caption">290:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c116" /></td>
						</tr>
						<tr>
							<td class="caption">Total der Abzuege:</td>
							<td class="caption">299:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c118" /></td>
						</tr>
					</table>
					<br />
					<br />
					<hr noshade="noshade" size="1" />
					<br />
					<br />
					<h2>Einkommensberechnung:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="45" class="caption">Ziffer</td>
							<td width="120">Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Total der Einkuenfte:</td>
							<td width="45" class="caption">199:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c117" /></td>
						</tr>
						<tr>
							<td class="caption">Total der Abzuege:</td>
							<td class="caption">299:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c118" /></td>
						</tr>
						<tr>
							<td class="caption">Nettoeinkommen:</td>
							<td class="caption">310:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c119" /></td>
						</tr>
						<tr>
							<td class="caption">Krankheits- und Unfallkosten:</td>
							<td class="caption">320:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c120" /></td>
						</tr>
						<tr>
							<td class="caption">Gemeinnuetzige Zuwendungen:</td>
							<td class="caption">324:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c121" /></td>
						</tr>
						<tr>
							<td class="caption">Reineinkommen:</td>
							<td class="caption">350:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c122" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug fuer Kinder in Ihrem Haushalt:</td>
							<td class="caption">370:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c123" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug fuer Kinder ausserhalb Ihres Haushaltes:</td>
							<td class="caption">372:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c124" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug fuer unterstuetzte Personen:</td>
							<td class="caption">374:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c125" /></td>
						</tr>
						<tr>
							<td class="caption">Abzug fuer Ehegatten / Partner:</td>
							<td class="caption">365:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c126" /></td>
						</tr>
						<tr>
							<td class="caption">Steuerbares Einkommen Gesamt:</td>
							<td class="caption">390:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c128" /></td>
						</tr>
						<tr>
							<td class="caption">Davon Anteil in anderen Kantonen:</td>
							<td class="caption">394:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c129" /></td>
						</tr>
						<tr>
							<td class="caption">Davon Anteil im Ausland:</td>
							<td class="caption">396:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c130" /></td>
						</tr>
						<tr>
							<td class="caption">Steuerbares Einkommen im Kanton:</td>
							<td class="caption">398:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c131" /></td>
						</tr>
					</table>
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Vermoegen im In- und Ausland:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="45" class="caption">Ziffer</td>
							<td width="120">Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Wertschriften und Guthaben:</td>
							<td width="45" class="caption">400:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c152" /></td>
						</tr>
						<tr>
							<td class="caption">Bargeld, Gold und andere Edelmetalle:</td>
							<td class="caption">404:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c153" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Lebens- und Rentenversicherungen::</td>
										<td>Abschlussjahr:</td>
										<td>Ablaufjahr:</td>
										<td>Steuerwert:</td>
									</tr>
									<xsl:for-each select="table/tax0_movablePropertyLifeInsurance/row">
										<tr>
											<td class="right"><xsl:value-of select="c2"/></td>
											<td class="right"><xsl:value-of select="c3"/></td>
											<td class="right"><xsl:value-of select="c4"/></td>
											<td class="right"><xsl:value-of select="c5"/></td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
							<td class="caption">406:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c155" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Motorfahrzeug:</td>
										<td>Kaufpreis:</td>
										<td>Jahrgang:</td>
										<td>Steuerwert:</td>
									</tr>
									<xsl:for-each select="table/tax0_movablePropertyVehicle/row">
										<tr>
											<td><xsl:value-of select="c2"/></td>
											<td class="right"><xsl:value-of select="c3"/></td>
											<td class="right"><xsl:value-of select="c4"/></td>
											<td class="right"><xsl:value-of select="c5"/></td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
							<td class="caption">412:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c156" /></td>
						</tr>
						<tr>
							<td class="caption">Anteile an unverteilten Erbschaften, Geschaefts-/Korporationsanteile:</td>
							<td class="caption">414:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c158" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="50%" class="caption">Uebrige Vermoegenswerte, Bezeichnung:</td>
										<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c160" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">416:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c161" /></td>
						</tr>
						<tr>
							<td class="caption">Liegenschaften: Einfamilienhaus oder Stockwerkeigentum:</td>
							<td class="caption">420:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c163" /></td>
						</tr>
						<tr>
							<td class="caption">Geschaefts-/Beteiligungskapital:</td>
							<td width="45" class="caption">430:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c169" /></td>
						</tr>
						<tr>
							<td class="caption">Kunden- und andere Guthaben:</td>
							<td width="45" class="caption">431:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c171" /></td>
						</tr>
						<tr>
							<td class="caption">Vorraete und Warenlager:</td>
							<td width="45" class="caption">432:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c173" /></td>
						</tr>
						<tr>
							<td class="caption">
								<table width="100%">
									<tr>
										<td width="33%" class="caption">Viehhabe:</td>
										<td width="33%" class="caption">Versicherungswert CHF:</td>
										<td width="34%"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c177" /></td>
									</tr>
								</table>
							</td>
							<td class="caption">433:</td>
							<td class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c175" /></td>
						</tr>
						<tr>
							<td class="caption">Anlagevermoegen ohne Grundeigentum:</td>
							<td width="45" class="caption">434:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c178" /></td>
						</tr>
						<tr>
							<td class="caption">Total der Vermoegenswerte:</td>
							<td width="45" class="caption">460:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c180" /></td>
						</tr>
						<tr>
							<td class="caption">Schulden:</td>
							<td width="45" class="caption">470:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c188" /></td>
						</tr>
						<tr>
							<td class="caption">Steuerbares Vermoegen gesamt:</td>
							<td width="45" class="caption">490:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c190" /></td>
						</tr>
						<tr>
							<td class="caption">Anteil in anderen Kantonen:</td>
							<td width="45" class="caption">494:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c192" /></td>
						</tr>
						<tr>
							<td class="caption">Anteil im Ausland:</td>
							<td width="45" class="caption">496:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c194" /></td>
						</tr>
						<tr>
							<td class="caption">Steuerbares Vermoegen im Kanton:</td>
							<td width="45" class="caption">498:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c196" /></td>
						</tr>
					</table>
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Kapitalleistungen:</h2>
					<table width="100%">
						<tr class="captionb">
							<td class="caption">&#xA0;</td>
							<td width="45" class="caption">Ziffer</td>
							<td width="120">Fr.</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Auszahlung</td>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Herkunft:</td>
										<td>Betrag:</td>
									</tr>
									<xsl:for-each select="table/tax0_payment/row">
										<tr>
											<td>
												<xsl:choose>
													<xsl:when test="c2 = 1">aus AHV/IV</xsl:when>
													<xsl:when test="c2 = 2">Freizuegigkeitskonto/-police</xsl:when>
													<xsl:when test="c2 = 3">Tod oder Invalidität</xsl:when>
													<xsl:when test="c2 = 4">2. Saeule</xsl:when>
													<xsl:when test="c2 = 5">3. Saeule a</xsl:when>
													<xsl:when test="c2 = 6">Sonstiges</xsl:when>
												</xsl:choose>
											</td>
											<td class="right"><xsl:value-of select="c3"/></td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
							<td width="45" class="caption">510:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c198" /></td>
						</tr>
						<tr>
							<td class="caption">Erhaltene Leistungen</td>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Art:</td>
										<td>Name:</td>
										<td>Adresse:</td>
										<td>Relation:</td>
										<td>Betrag:</td>
									</tr>
									<xsl:for-each select="table/tax0_paymentReceived/row">
										<tr>
											<td>
												<xsl:choose>
													<xsl:when test="c18 = 1">Schenkung</xsl:when>
													<xsl:when test="c18 = 2">Erbvorbezug</xsl:when>
													<xsl:when test="c18 = 3">Erbschaften</xsl:when>
													<xsl:when test="c18 = 4">Beteiligung an Erbengemeinschaften</xsl:when>
												</xsl:choose>
											</td>
											<td><xsl:value-of select="c3" />&#160; <xsl:value-of select="c2" />&#160; <xsl:value-of select="c4" /></td>
											<td><xsl:value-of select="c11" />&#160; <xsl:value-of select="c12" />&#160; <xsl:value-of select="c13" />&#160; <xsl:value-of select="c14" />&#160; <xsl:value-of select="c15" />&#160; <xsl:value-of select="c16" /></td>
											<td><xsl:value-of select="c17" /></td>
											<td><xsl:value-of select="c19" /></td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
							<td width="45" class="caption">516:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c199" /></td>
						</tr>
						<tr>
							<td class="caption">Ausgerichtete Leistungen</td>
							<td class="caption">
								<table width="100%">
									<tr class="caption">
										<td>Art:</td>
										<td>Name:</td>
										<td>Adresse:</td>
										<td>Relation:</td>
										<td>Betrag:</td>
									</tr>
									<xsl:for-each select="table/tax0_paidOut/row">
										<tr>
											<td>
												<xsl:choose>
													<xsl:when test="c18 = 1">Schenkung</xsl:when>
													<xsl:when test="c18 = 2">Erbvorbezug</xsl:when>
													<xsl:when test="c18 = 3">Erbschaften</xsl:when>
													<xsl:when test="c18 = 4">Beteiligung an Erbengemeinschaften</xsl:when>
												</xsl:choose>
											</td>
											<td><xsl:value-of select="c3" />&#160; <xsl:value-of select="c2" />&#160; <xsl:value-of select="c4" /></td>
											<td><xsl:value-of select="c11" />&#160; <xsl:value-of select="c12" />&#160; <xsl:value-of select="c13" />&#160; <xsl:value-of select="c14" />&#160; <xsl:value-of select="c15" />&#160; <xsl:value-of select="c16" /></td>
											<td><xsl:value-of select="c17" /></td>
											<td><xsl:value-of select="c19" /></td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
							<td width="45" class="caption">519:</td>
							<td width="120" class="right"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c200" /></td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="caption">Bemerkungen:</td>
							<td width="45" class="caption">&#160; </td>
							<td width="120"><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c201" /></td>
						</tr>
						<tr>
							<td class="caption">Kantonaler Steuertarif</td>
							<td>&#160; </td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c202" /></td>
						</tr>
						<tr>
							<td class="caption">Total kantonaler Einkommenssteuer</td>
							<td>&#160; </td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c203" /></td>
						</tr>
						<tr>
							<td class="caption">Total kantonaler Vermögenssteuer</td>
							<td>&#160; </td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c204" /></td>
						</tr>
						<tr>
							<td class="caption">Total kantonaler Steuer</td>
							<td>&#160; </td>
							<td><xsl:value-of select="table/tax0_taxDeclarationMainForm/row/c205" /></td>
						</tr>
					</table>
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Wertschriften- und Guthabenverzeichnis:</h2>
					<table width="100%">
						<tr class="captionb">
							<td>Code</td>
							<td>Währung</td>
							<td>Nennwert, Stückzahl</td>
							<td>Valoren-Nr.</td>
							<td>Genaue Bezeichnung</td>
							<td>Zugang</td>
							<td>Abgang</td>
							<td>Steuerwert</td>
							<td>Bruttoertrag</td>
						</tr>
						<xsl:for-each select="table/tax0_securityEntry/row">
							<tr>
								<td><xsl:value-of select="c2"/></td>
								<td><xsl:value-of select="c3"/></td>
								<td><xsl:value-of select="c4"/></td>
								<td><xsl:value-of select="c5"/></td>
								<td><xsl:value-of select="c6"/></td>
								<td><xsl:value-of select="c7"/></td>
								<td><xsl:value-of select="c8"/></td>
								<td><xsl:value-of select="c9"/></td>
								<td><xsl:value-of select="c10"/>&#160;<xsl:value-of select="c11"/></td>
							</tr>
						</xsl:for-each>
					</table>		
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Qualifizierte private Beteiligungen:</h2>
					<table width="100%">
						<tr class="captionb">
							<td>Währung</td>
							<td>Beteiligungsquote in %</td>
							<td>Beteiligungsrechte</td>
							<td>Bruttoertrag</td>
						</tr>
						<xsl:for-each select="table/tax0_privateQualifiedInvestments/row">
							<tr>
								<td><xsl:value-of select="c2"/></td>
								<td><xsl:value-of select="c3"/></td>
								<td><xsl:value-of select="c4"/></td>
								<td><xsl:value-of select="c5"/></td>
							</tr>
						</xsl:for-each>
					</table>		
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Schuldenverzeichnis:</h2>
					<table width="100%">
						<tr class="captionb">
							<td>Gläubiger</td>
							<td>Schuldbetrag</td>
							<td>Schuldzins</td>
						</tr>
						<xsl:for-each select="table/tax0_privateLiabilitiesListing/row">
							<tr>
								<td><xsl:value-of select="c2"/></td>
								<td><xsl:value-of select="c3"/></td>
								<td><xsl:value-of select="c4"/></td>
							</tr>
						</xsl:for-each>
					</table>		
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<div>
					<h2>Liegenschaftsverzeichnis:</h2>
					<table width="100%">
						<tr class="captionb">
							<td>Strasse</td>
							<td>Ort</td>
							<td>Art</td>
							<td>Steuerwert / Verkehrswert / Ertragswert</td>
							<td>Mietwert / Mietzins</td>
							<td>Brutto Ertrag</td>
							<td>Abzüge Unterhalt Pauschalkosten / eff. Kosten</td>
							<td>Netto Ertrag</td>
						</tr>
						<xsl:for-each select="table/tax0_propertyHouseOrFlat/row">
							<tr>
								<td><xsl:value-of select="c2"/></td>
								<td><xsl:value-of select="c3"/></td>
								<td><xsl:value-of select="c4"/></td>
								<td><xsl:value-of select="c5"/> / <xsl:value-of select="c7"/> / <xsl:value-of select="c9"/></td>
								<td><xsl:value-of select="c11"/> / <xsl:value-of select="c12"/></td>
								<td><xsl:value-of select="c13"/></td>
								<td><xsl:value-of select="c14"/> / <xsl:value-of select="c15"/></td>
								<td><xsl:value-of select="c16"/></td>
							</tr>
						</xsl:for-each>
					</table>		
				</div>
				<br />
				<br />
				<hr noshade="noshade" size="1" />
				<br />
				<br />
				<p class="footer">Diese Veranlagungsdaten stammen aus dem <xsl:value-of select="table/Archive"/> basierend auf eCH-0233.</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>