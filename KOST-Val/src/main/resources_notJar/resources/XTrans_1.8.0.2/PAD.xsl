<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/TR/WD-xsl">
	<xsl:template match="/">
		<H1><xsl:value-of select="//Program_Name"/></H1>
		<h2>Product description</h2>
		<h3>Version</h3>
		<p><xsl:value-of select="//Program_Version"/></p>
		<h3>Abstract</h3>
		<p><xsl:value-of select="//Char_Desc_250"/></p>
		<h3>License type</h3>
		<p><xsl:value-of select="//Program_Type"/></p>
		<h3>Description</h3>
		<p><xsl:value-of select="//Char_Desc_450"/></p>
		<h3>Screenshot</h3>
		Preview a <A>
  		<xsl:attribute name="href"><xsl:value-of select="//Application_Screenshot_URL"/></xsl:attribute>
		screenshot</A>
		
		<h3>Download</h3>
		<A>
  		<xsl:attribute name="href">
		<xsl:value-of select="//Primary_Download_URL"/>
		</xsl:attribute>
		ZIP file archive </A>
		<h3>Feedback</h3>
		Mail found bugs reports and suggestions to<A>
  		<xsl:attribute name="href"><xsl:value-of select="//Author_Email"/></xsl:attribute>
		<xsl:value-of select="//Author_Email"/>
		</A>
	</xsl:template>
</xsl:stylesheet>