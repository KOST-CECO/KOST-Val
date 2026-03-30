<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <title> XML View:</title>
                <style>
                    body {
                        font-family: Verdana, Geneva, sans-serif;
                        font-size: 10pt;
                    }

                    .tree {
                        padding-left: 5px;
                    }

                    .node {
                        position: relative;
                        padding-left: 24px;
                        line-height: 1.4em;
                    }

                    .node::before {
                        content: "";
                        position: absolute;
                        left: 12px;
                        top: 0;
                        bottom: 0;
                        border-left: 1px solid #ccc;
                    }

                    .node::after {
                        content: "";
                        position: absolute;
                        left: 12px;
                        top: 0.8em;
                        width: 10px;
                        border-top: 1px solid #ccc;
                    }

                    .node.last::before {
                        height: 0.8em;
                    }

                    /* Toggle Pfeil */
                    .toggle {
                        cursor: pointer;
                        user-select: none;
                    }
					
					.toggle::before {
                        content: "+";
                        display: inline-block;
                        width: 12px;
                        margin-left: 1px; margin-right: 1px;
                        font-size: 12px;
						font-weight: bold;
						color: grey;
                    }

                    .expanded > .toggle::before {
                        content: "-";
                        width: 12px;
                        margin-left: 1px; margin-right: 1px;
                        font-size: 12px;
						font-weight: bold;
						color: grey;
                    }

                    .children {
                        display: none;
                        margin-left: 4px;
                    }

                    .expanded > .children {
                        display: block;
                    }

                    .element-name { color: blue; }
                    .attr { color: green; }
                    .text { color: black; }
                </style>

                <script>
                    function toggleNode(el) {
                        var parent = el.parentNode;
                        if (parent.classList.contains('expanded')) {
                            parent.classList.remove('expanded');
                        } else {
                            parent.classList.add('expanded');
                        }
                    }
                </script>
            </head>
            <body>
                <h2> XML View:</h2>
                <div class="tree">
                    <xsl:apply-templates select="/*"/>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- Root -->
    <xsl:template match="/*">
        <div class="expanded">
            <!-- <span class="element-name toggle" onclick="toggleNode(this)"> -->
			<span class="element-name">
                <xsl:value-of select="name() "/>
            </span>

            <xsl:if test="@*">
                <xsl:text> </xsl:text>
                <xsl:for-each select="@*">
                    <span class="attr">
                        <xsl:value-of select="name()"/>="<xsl:value-of select="."/>"
                    </span>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>

            <div class="children">
                <xsl:apply-templates select="*"/>
            </div>
        </div>
    </xsl:template>

    <!-- Generisches Element -->
    <xsl:template match="*">
        <div>
            <xsl:attribute name="class">
                <xsl:text>node</xsl:text>
                <xsl:if test="position() = last()">
                    <xsl:text> last</xsl:text>
                </xsl:if>
                <xsl:if test="*">
                    <!-- <xsl:text> expandable</xsl:text>-->
					<xsl:text> expanded</xsl:text>
                </xsl:if>
            </xsl:attribute>

            <!-- Kinder: Toggle -->
            <xsl:choose>
                <xsl:when test="*">
                    <span class="element-name toggle" onclick="toggleNode(this)">
                        <xsl:value-of select="name()"/>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <span class="element-name">
                        <xsl:value-of select="name()"/>
                    </span>
                </xsl:otherwise>
            </xsl:choose>

            <!-- Attribute -->
            <xsl:if test="@*">
                <xsl:text> </xsl:text>
                <xsl:for-each select="@*">
                    <span class="attr">
                        <xsl:value-of select="name()"/>="<xsl:value-of select="."/>"
                    </span>
                    <xsl:if test="position() != last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>

            <!-- Textinhalt -->
            <xsl:if test="not(*) and normalize-space(.) != ''">
                <xsl:text>: </xsl:text>
                <span class="text">
                    <xsl:value-of select="normalize-space(.)"/>
                </span>
            </xsl:if>

            <!-- Kinder -->
            <xsl:if test="*">
                <div class="children">
                    <xsl:apply-templates select="*"/>
                </div>
            </xsl:if>

        </div>
    </xsl:template>

</xsl:stylesheet>