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
							<span class="logoff">Konfiguration</span>
						</td>
						<td>Legende:</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td class="captioninfow">
							<span class="captioninfog">&#x2713;</span>
							<span>&#160;&#160;&#160;&#160;= akzeptiert und validieren</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfoo">(&#x2713;)</span>
							<span>&#160;= akzeptiert</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfor">&#x2717;</span>
							<span>&#160;&#160;&#160;&#160;= nicht akzeptiert</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;&#160;&#160;</td>
					</tr>

					<!-- TEXT -->
					<tr class="captioninfo">
						<td>PDF/A: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>PDF/A-Validierung mit PDF Tools [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- PDF Tools auch detaillierte Fehler in Englisch [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Validierung (Durchsuch- und Extrahierbarkeit) [tolerant]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>PDF/A-Validierung mit callas [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Fehler (E) / Warnung (W), wenn der N-Eintrag nicht übereinstimmt [W]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/nentry" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubte PDF/A Versionen [1A, 1B, 2A, 2B, 2U]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfaversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>PDF/A-3 nach PDF/A-2 validieren und Warnung anstelle eines Fehlers ausgeben [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/warning3to2" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>JBIG2-Komprimierung erlaubt [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TXT: Akzeptanz [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/txt/txtvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PDF: Akzeptanz [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/pdf/pdfvalidation" />
						</td>
					</tr>

					<!-- BILD -->
					<tr class="captioninfo">
						<td>JPEG2000: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JPEG: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TIFF: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubten Komprimierungsalgorithmen [uncompressed, CCITT 1D, CCITT Group 3, CCITT Group 4, LZW, PackBits]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubten Farbräume [white is zero,  black is zero, RGB, palette color]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubten Bits per Sample [1, 4, 8, 16]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Multipage-TIFFs erlaubt [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Aufbau in Kacheln erlaubt [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Dateigrössen von 1000MB (~1GB) und grösser erlaubt [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PNG: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/png/pngvalidation" />
						</td>
					</tr>

					<!-- AUDIO -->
					<tr class="captioninfo">
						<td>FLAC: Akzeptanz [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/flac/flacvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>WAVE: Akzeptanz [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/wave/wavevalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP3: Akzeptanz [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp3/mp3validation" />
						</td>
					</tr>

					<!-- VIDEO -->
					<tr class="captioninfo">
						<td>MKV: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mkv/mkvvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Erlaubter Videocodec [FFV1, AVC, HEVC, AV1]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvvideo" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Erlaubter Audiocodec [FLAC, MP3, AAC]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Stummfilm erlaubt (kein Audiocodec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnoaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Reine Audiodatei erlaubt (kein Videocodec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnovideo" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP4: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp4/mp4validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Erlaubter Videocodec [AVC, HEVC]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4video" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Erlaubter Audiocodec [MP3, AAC]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4audio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Stummfilm erlaubt (kein Audiocodec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4noaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Reine Audiodatei erlaubt (kein Videocodec) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4novideo" />
						</td>
					</tr>

					<!-- DATEN -->
					<tr class="captioninfo">
						<td>XML: Akzeptanz und Validierung [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xml/xmlvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JSON: Akzeptanz [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/json/jsonvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>SIARD: Akzeptanz und Validierung [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubte SIARD Versionen [1.0, 2.1, 2.2]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siardversion" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>CSV: Akzeptanz [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/csv/csvvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>XLSX: Akzeptanz [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xlsx/xlsxvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>ODS: Akzeptanz [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/ods/odsvalidation" />
						</td>
					</tr>

					<!-- SIP -->
					<tr class="captioninfo">
						<td>SIP: Validierung [&#x2713;]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubte maximale Anzahl Zeichen in Pfadlängen [179]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Vorgaben zum Aufbau des SIP-Namens [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\w{3}]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Erlaubte eCH-0160 SIP Versionen [1.0, 1.1, 1.2, 1.3]:</td>
						<td>
							<xsl:value-of select="configuration/sip/sipversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Nur Warnung bei alten Dokumenten (Entstehungszeitraum) [no]:</td>
						<td>
							<xsl:value-of select="configuration/sip/warningolddok" />
						</td>
					</tr>

					<!-- SONSTIGES -->
					<tr class="captioninfo">
						<td>Weitere akzeptierte Dateiformate [WARC, HTML, DWG]:</td>
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
						<td>Hashwert von Dateien berechnen und ausgeben. Leer bedeutet keine Berechnung und Ausgabe []:</td>
						<td>
							<xsl:value-of select="configuration/hash" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Warnung bei kleinen Dateien ausgeben. Leer bedeutet keine Warnung []:</td>
						<td>
							<xsl:value-of select="configuration/sizeWarning" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Arbeitsverzeichnis []:</td>
						<td>
							<xsl:value-of select="configuration/pathtoworkdir" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Inputverzeichnis []:</td>
						<td>
							<xsl:value-of select="configuration/standardinputdir" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>