<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
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
					<span class="logoff">Configuration</span>
				</p>
				<table >
					<tr  class="captioninfo">
						<td>Informations sur la validation PDF/A [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfavalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si une validation PDF/A avec les outils PDF doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdftools" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si les PDF Tools doivent également produire des erreurs détaillées en anglais [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/detail" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si une validation PDF/A avec callas doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/callas" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si callas doit émettre une erreur (E) ou un avertissement (W) si l'entrée N ne correspond pas [W]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/nentry" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise quelles versions PDF/A sont autorisées [1A, 1B, 2A, 2B, 2U]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfa1a" />&#160; <xsl:value-of select="configuration/pdfa/pdfa1b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2a" />&#160;<xsl:value-of select="configuration/pdfa/pdfa2b" />&#160; <xsl:value-of select="configuration/pdfa/pdfa2u" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si une validation de la police (recherche et extractibilité) doit avoir lieu [tolerant]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfafont" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si la validation des images (JPEG et JP2) doit avoir lieu [no]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/pdfaimage" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si la compression JBIG2 est autorisée [yes]:</td>
						<td>
							<xsl:value-of select="configuration/pdfa/jbig2allowed" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Précise si une validation SIARD doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/siard/siardvalidation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Précise si une validation JP2 doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jp2/jp2validation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Précise si une validation JPEG doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/jpeg/jpegvalidation" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Précise si une validation TIFF doit avoir lieu [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/tiffvalidation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Spécifie les algorithmes de compression autorisés [Uncompressed, CCITT 1D, T4/Group 3 Fax, T6/Group 4 Fax, LZW, PackBits]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedcompression" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>ASpécification des espaces de couleur autorisés [WhiteIsZero, BlackIsZero, RGB, RGB Palette]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedphotointer" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise le nombre de bits per sample [1, 4, 8, 16]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedbitspersample" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si les TIFF multipages sont autorisés ou non [yes]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedmultipage" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si la construction en carreaux est autorisée ou non [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedtiles" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Précise si les fichiers de 1000 Mo (~1 Go) et plus sont autorisés ou non [no]:</td>
						<td>
							<xsl:value-of select="configuration/tiff/allowedother/allowedsize" />
						</td>
					</tr>

					<tr  class="captioninfo">
						<td>Détails de la validation SIP [yes]:</td>
						<td>
							<xsl:value-of select="configuration/sip/ech0160validation" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Nombre maximal de caractères autorisés dans les chemins d'accès [179]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedlengthofpaths" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Spécifications pour la structure du nom SIP [SIP_[1-2][0-9]{3}[0-1][0-9][0-3][0-9]_\\w{3}]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedsipname" />
						</td>
					</tr>
					<tr  class="captioninfom">
						<td>Liste des formats de fichiers autorisés [TXT, PDFA1, PDFA2, TIFF, JP2, JPEG, WAVE, MP3, MP4, MJ2, CSV, SIARD, WARC]:</td>
						<td>
							<xsl:value-of select="configuration/sip/allowedformats" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>