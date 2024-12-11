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
						<td>Légende :</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td class="captioninfow">
							<span class="captioninfog">&#x2713;</span>
							<span>&#160;&#160;&#160;&#160;= accepté et validé</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfoo">(&#x2713;)</span>
							<span>&#160;= accepté</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;</td>
						<td>
							<span class="captioninfor">&#x2717;</span>
							<span>&#160;&#160;&#160;&#160;= non accepté</span>
						</td>
					</tr>
					<tr class="captioninfow">
						<td>&#160;&#160;&#160;</td>
					</tr>

					<!-- TEXT -->
					<tr class="captioninfo">
						<td>PDF/A : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Validation PDF/A avec veraPDF [yes] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/verapdf" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- veraPDF également des erreurs détaillées en anglais [yes] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Validation PDF/A avec callas [yes] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Versions PDF/A autorisées [1A, 1B, 2A, 2B, 2U] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfaversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Valider le PDF/A-3 vers le PDF/A-2 et générer un avertissement au lieu d'une erreur [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/warning3to2" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Compression JBIG2 autorisée [yes] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Validation Font avec PDF Tools [yes] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Validation (possibilité de recherche et d'extraction) [tolérant] :</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TXT : Acceptation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/txt/txtvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PDF : Acceptation [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/pdf/pdfvalidation" />
						</td>
					</tr>

					<!-- BILD -->
					<tr class="captioninfo">
						<td>JPEG2000 : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JPEG : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>TIFF : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Algorithmes de compression autorisés [uncompressed, CCITT 1D, CCITT Group 3, CCITT Group 4, LZW, PackBits] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Espaces colorimétriques autorisés [white is zero,  black is zero, RGB, palette color] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Bits autorisés par échantillon [1, 4, 8, 16] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>TIFF multipages autorisés [yes] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Structure en carreaux autorisée [no] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Taille de fichier de 1000MB (~1GB) et plus autorisée [no] :</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>PNG : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/png/pngvalidation" />
						</td>
					</tr>

					<!-- AUDIO -->
					<tr class="captioninfo">
						<td>FLAC : Acceptation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/flac/flacvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>WAVE : Acceptation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/wave/wavevalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP3 : Acceptation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp3/mp3validation" />
						</td>
					</tr>

					<!-- VIDEO -->
					<tr class="captioninfo">
						<td>MKV : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mkv/mkvvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Codec vidéo autorisé [FFV1, AVC, HEVC, AV1] :</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvvideo" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Codec audio autorisé [FLAC, MP3, AAC] :</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Film muet autorisé (pas de codec audio) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnoaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Fichier audio pur autorisé (pas de codec vidéo) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mkv/allowedmkvnovideo" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>MP4 : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/mp4/mp4validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Codec vidéo autorisé [AVC, HEVC] :</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4video" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Codec audio autorisé [MP3, AAC] :</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4audio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Film muet autorisé (pas de codec audio) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4noaudio" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>- Fichier audio pur autorisé (pas de codec vidéo) [Warning]:</td>
						<td>
							<xsl:value-of select="configuration/mp4/allowedmp4novideo" />
						</td>
					</tr>

					<!-- DATEN -->
					<tr class="captioninfo">
						<td>XML : Acceptation et validation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xml/xmlvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>JSON : Acceptation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/json/jsonvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>SIARD : Acceptation et validation [&#x2713;]</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Versions SIARD autorisées [2.1, 2.2] :</td>
						<td>
							<xsl:value-of select="configuration/siard/siardversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Réclamer des extensions de fichiers non exactes [Error]:</td>
						<td>
							<xsl:value-of select="configuration/siard/lobExtension" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Remarques sur les formats de fichiers non acceptés [Azepted]:</td>
						<td>
							<xsl:value-of select="configuration/siard/lobAzepted" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>CSV : Acceptation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/csv/csvvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>XLSX : Acceptation [(&#x2713;)]</td>
						<td>
							<xsl:value-of select="configuration/xlsx/xlsxvalidation" />
						</td>
					</tr>

					<tr class="captioninfo">
						<td>ODS : Acceptation [&#x2717;]</td>
						<td>
							<xsl:value-of select="configuration/ods/odsvalidation" />
						</td>
					</tr>

					<!-- SIP -->
					<tr class="captioninfo">
						<td>SIP : Validation [&#x2713;]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Nombre maximal de caractères autorisés dans les longueurs de chemin [179] :</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Spécifications pour la structure du nom SIP [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\w{3}] :</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Versions eCH-0160 SIP autorisées [1.0, 1.1, 1.2, 1.3]:</td>
						<td>
							<xsl:value-of select="configuration/sip/sipversion" />
						</td>
					</tr>
					<tr class="captioninfom">
						<td>Avertissement uniquement pour les anciens documents (Entstehungszeitraum) [no]:</td>
						<td>
							<xsl:value-of select="configuration/sip/warningolddok" />
						</td>
					</tr>

					<!-- SONSTIGES -->
					<tr class="captioninfo">
						<td>Autres formats de fichiers acceptés [WARC, HTML, DWG] :</td>
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
						<td>Calculer et afficher la valeur de hachage des fichiers. Vide signifie pas de calcul ni de sortie [] :</td>
						<td>
							<xsl:value-of select="configuration/hash" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Afficher un avertissement pour les petits fichiers. Vide signifie pas d'avertissement [] :</td>
						<td>
							<xsl:value-of select="configuration/sizeWarning" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Répertoire de travail [] :</td>
						<td>
							<xsl:value-of select="configuration/pathtoworkdir" />
						</td>
					</tr>
					<tr class="captioninfo">
						<td>Répertoire d'entrée [] :</td>
						<td>
							<xsl:value-of select="configuration/standardinputdir" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>