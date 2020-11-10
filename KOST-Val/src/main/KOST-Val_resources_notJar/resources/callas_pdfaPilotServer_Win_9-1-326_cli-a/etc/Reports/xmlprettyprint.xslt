<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">

<xsl:output method="html" 
            encoding="UTF-8" 
            omit-xml-declaration="yes" 
            media-type="text/html"
            doctype-public="-//W3C//DTD HTML 4.01//EN"
            indent="no"/>
	
    <xsl:template match="/">
		<html>
		<head>
			<style type="text/css">
				body {
					display:block;
					margin-left:-1em;
					margin-right:1em;
					font-family:Arial,san-serif;
				}
				.element{
					margin-left:2em;
					font-weight: normal;
				}
				.content{
					font-weight: bold;
				}
				.comment {
					margin-left:2em;
					font-weight: normal;
					color:gray;
				}
				.tag_name_start{
					color:green;
				}
				.tag_name_end{
					color:green;
				}
				.attr {
					color:blue;
				}
				.attr .value {
					font-weight: bold;
				}
			</style>
			<script type="text/javascript">
				function isVisible(el) {
					return "none" != el.style.display;
				}
				function toggle(n) {
					if (isVisible(n))
						n.style.display = "none";
					else
						n.style.display = "";
				}
				function getChild(element) {
					while (element = element.nextSibling) {
						if (element.nodeType == Node.ELEMENT_NODE) {
							if (-1 != element.className.indexOf("content")) {
								return element;
							}
						}
					}
					return null;
				}
				function expand(b) {
					var child = getChild(b);
					toggle(child);
					if (isVisible(child))
						b.innerHTML = "- ";
					else
						b.innerHTML = "+ ";
				}
			</script>
		</head>
		<body>
			<xsl:apply-templates/>
		</body>
		</html>
    </xsl:template>

    <xsl:template match="*">
		<!-- process elements -->
        <div class="element">
			<!-- open tag -->
			<span class="toggle_button" onclick="expand(this);">-</span>
			<span class="open_bracket_start">&lt;</span>
			<span class="tag_name_start">
				<xsl:value-of select="name()"/>
			</span>

			<!-- process attributes of top level element -->
            <xsl:variable name="vTopLevel" select="/* = ."/>
            	
            <xsl:if test="$vTopLevel">
				<!-- add xmlns namespace, suppress xml namespace -->
				<xsl:for-each select="namespace::*">
		            <xsl:if test="name() != 'xml'">
						<xsl:text> </xsl:text>
						<span class="attr">
							<span class="name">
							<xsl:text>xmlns:</xsl:text>
							<xsl:value-of name="name" select="name()"/>
							</span>
							<xsl:text>=</xsl:text>
							<span class="value">
							<xsl:text>"</xsl:text>
							<xsl:value-of name="name" select="."/>
							<xsl:text>"</xsl:text>
							</span>
						</span>
					</xsl:if>
				</xsl:for-each>
            </xsl:if>

			<!-- process attributes -->
            <xsl:apply-templates select="@*"/>

			<!-- subtree value and closing tag -->
            <xsl:choose>
                <xsl:when test="count(*) or string-length()">
					<span class="open_bracket_end">&gt;</span>
					<!-- process subtrees -->
					<span class="content">
						<xsl:apply-templates/>
					</span>
					<!-- closing tag -->
					<span class="close_bracket_start">&lt;/</span>
					<span class="tag_name_end"><xsl:value-of select="name()"/></span>
					<span class="close_bracket_end">&gt;</span>
                </xsl:when>
                <xsl:otherwise>
					<!-- self closing tag -->
					<span class="open_bracket_end">/&gt;</span>
                </xsl:otherwise>
            </xsl:choose>
		</div>
    </xsl:template>

    <xsl:template match="@*">
		<xsl:text> </xsl:text>
		<span class="attr">
			<span class="name">
			<xsl:value-of name="name" select="name()"/>
			</span>
			<xsl:text>=</xsl:text>
			<span class="value">
			<xsl:text>"</xsl:text>
			<xsl:value-of name="name" select="."/>
			<xsl:text>"</xsl:text>
			</span>
		</span>
    </xsl:template>

    <xsl:template match="comment()">
        <div class="comment">
        <xsl:text>&lt;!-- </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text> --&gt;</xsl:text>
        </div>
    </xsl:template>


</xsl:stylesheet>

