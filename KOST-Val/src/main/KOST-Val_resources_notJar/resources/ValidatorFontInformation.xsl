<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:f="http://www.pdf-tools.com/Validator/FontInformation/1">
<xsl:template match="/f:document">
<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html;charset=iso-8859-1" />
        <title>3-Heights™ Font Unicode Extraction</title>
        <style type="text/css">
            body {
                font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
                font-size: 14px;
            }
            h1, h2, h3 {
                color: #078ad8;
                line-height: 1.5;
                border-bottom:#004583 solid 1px;
            }
            ul, table {
                margin-left: 0;
                padding-left: 2em;
            }
            td, th {
                padding-left: 1em;
                padding-right: 1em;
            }
            th {
                text-align: left;
            }
        </style>
    </head>

    <body>

        <h1>3-Heights™ Font Unicode Extraction</h1>
        
        <h2>Document Metadata</h2>
        
        <ul>
            <xsl:if test="f:docInfo/f:creationDate">
                <li><strong>Creation date: </strong> <xsl:value-of select="f:docInfo/f:creationDate"/></li>
            </xsl:if>
            <xsl:if test="f:docInfo/f:producer">
                <li><strong>Producer: </strong> <xsl:value-of select="f:docInfo/f:producer"/></li>
            </xsl:if>
            <li><strong>PDF version: </strong> <xsl:value-of select="f:docInfo/f:pdfVersion"/></li>
            <xsl:if test="f:docInfo/f:pdfaVersion">
                <li><strong>PDF/A version: </strong> <xsl:value-of select="f:docInfo/f:pdfaVersion"/></li>
            </xsl:if>
        </ul>
        
        <h2>Fonts</h2>
        
            <ul>
                <li><strong>Character count: </strong> <xsl:value-of select="f:fonts/f:characterCount"/></li>
                <li><strong>Unknown Unicode count: </strong> <xsl:value-of select="f:fonts/f:characterUnknown"/> (<xsl:value-of select="f:fonts/f:characterUnknownPercentage"/>%)</li>
            </ul>
        
        <xsl:for-each select="f:fonts/f:font" >
            <h3>Font <xsl:value-of select="@name"/></h3>
            
            <ul>
                <li><strong>Full name: </strong> <xsl:value-of select="@fullname"/>
                    <xsl:if test="@objectNo">
                        (object no <xsl:value-of select="@objectNo"/>)
                    </xsl:if>
                </li>
                <li><strong>Type: </strong> <xsl:value-of select="@type"/></li>
                <xsl:if test="@fontfile">
                    <li><strong>Font file: </strong> <xsl:value-of select="@fontfile"/></li>
                </xsl:if>
            </ul>
            
            <table>
                <tr>
                    <th>CID</th>
                    <th>Name</th>
                    <th>Unicode</th>
                    <th>Glyph</th>
                    <th>Image</th>
                </tr>
                <xsl:for-each select="f:character">
                    <tr>
                        <td><xsl:value-of select="@cid"/></td>
                        <td><xsl:value-of select="@name"/></td>
                        <td>
                            <xsl:choose>
                                <xsl:when test="@unicodeUnknown">
                                    <xsl:attribute name="style">color: red;</xsl:attribute>
                                    unknown
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="@unicode"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td><xsl:value-of select="@glyphId"/></td>
                        <td><img>
                            <xsl:attribute name="src"><xsl:value-of select="."/></xsl:attribute>
                        </img></td>
                    </tr>
                </xsl:for-each>
            </table>
            
        </xsl:for-each>
    </body>
</html>
</xsl:template>  
</xsl:stylesheet>
