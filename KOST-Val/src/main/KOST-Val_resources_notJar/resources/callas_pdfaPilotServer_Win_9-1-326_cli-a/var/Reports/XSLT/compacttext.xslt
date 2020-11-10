<?xml version="1.0" encoding="UTF-8"?>

<!-- ================================================================================

		Transformation of a pdfToolbox XML report to a compact text report
		
		(c) callas software gmbh 2008
		
		history: 
			UFr-2008-09-19: creation
			PKl-2008-10-14: finalization
		
     ================================================================================ -->
     
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:pi4="http://www.callassoftware.com/namespace/pi4">
	<xsl:output method="text"/>
	<xsl:output encoding="UTF-8"/>
	<xsl:variable name="newline">
		<xsl:text>
</xsl:text>
	</xsl:variable>
	<xsl:template match="/">
		<!--BOM -->
		<xsl:text>&#xFEFF;</xsl:text>
		<xsl:variable name="theLang">
			<xsl:value-of select="/pi4:report/pi4:information/pi4:report_language"/>
		</xsl:variable>
		<xsl:value-of select="/pi4:report/pi4:information/pi4:product_name"/>
		<xsl:text> - </xsl:text>
		<xsl:value-of select="/pi4:report/pi4:information/pi4:date_time"/>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_USEDPROFILE'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:profile_info/pi4:profile_name"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_DOCUMENT'"/>
			
		</xsl:call-template>
		<xsl:text> (</xsl:text>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_INFORMATION'"/>
			
		</xsl:call-template>
		<xsl:text>)</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_FILENAME'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:filename"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_PATH'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:path"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:if test="/pi4:report/pi4:document/pi4:pages">
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_NUMBEROFPAGES'"/>
				
			</xsl:call-template>
			<xsl:text>: "</xsl:text>
			<xsl:value-of select="count(//pi4:page)"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_PDFVERSIONNUMBER'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:pdfversion"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_FILESIZE'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:variable name="fs">
			<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:filesize_byte"/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$fs &gt; 1000000">
				<xsl:value-of select="round(number($fs) div 1024 div 1024 * 10) div 10"/>
				<xsl:text> MByte"</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="round(number($fs) div 1024 * 10) div 10"/>
				<xsl:text> KByte"</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_TITLE'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:title"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_AUTHOR'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:author"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_CREATOR'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:creator"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_PRODUCER'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:producer"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_CREATED'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:created"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_MODIFIED'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:modified"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:if test="/pi4:report/pi4:document/pi4:doc_info/pi4:trapped">
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_TRAPPING'"/>
				
			</xsl:call-template>
			<xsl:text>: "</xsl:text>
			<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:trapped"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="/pi4:report/pi4:document/pi4:doc_info/pi4:pdfxversion">
			<xsl:text>================================================================================</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_STANDARD'"/>
				
			</xsl:call-template>
			<xsl:text> (</xsl:text>
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_INFORMATION'"/>
				
			</xsl:call-template>
			<xsl:text>)</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:text>================================================================================</xsl:text>
			<xsl:value-of select="$newline"/>

			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_PDFXVERSION'"/>
				
			</xsl:call-template>
			<xsl:text>: "</xsl:text>
			<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:pdfxversion"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:output_intent/@output_condition_identifier">
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_OUTPUTINTENT'"/>
				
			</xsl:call-template>
			<xsl:text>: "</xsl:text>
			<xsl:value-of select="//pi4:output_intent[@id='OI1']/@output_condition_identifier"
				xml:base=""/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_COLOR'"/>
			
		</xsl:call-template>
		<xsl:text> (</xsl:text>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_INFORMATION'"/>
			
		</xsl:call-template>
		<xsl:text>)</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>

		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_NUMBEROFPLATES'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:value-of select="/pi4:report/pi4:document/pi4:doc_info/pi4:plates"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_NAMESOFPLATES'"/>
			
		</xsl:call-template>
		<xsl:text>: "</xsl:text>
		<xsl:call-template name="doc_platenames"/>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_PAGE'"/>
			
		</xsl:call-template>
		<xsl:text> (</xsl:text>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_INFORMATION'"/>
			
		</xsl:call-template>
		<xsl:text>)</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>

		<xsl:if test="//pi4:page/@mediabox">
			<xsl:text>Mediabox (pt): "</xsl:text>
			<xsl:value-of select="//pi4:page[@nr='1']/@mediabox"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@trimbox">
			<xsl:text>Trimbox (pt): "</xsl:text>
			<xsl:value-of select="//pi4:page[@nr='1']/@trimbox"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@bleedbox">
			<xsl:text>Bleedbox (pt): "</xsl:text>
			<xsl:value-of select="//pi4:page[@nr='1']/@bleedbox"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@cropbox">
			<xsl:text>Cropbox (pt): "</xsl:text>
			<xsl:value-of select="//pi4:page[@nr='1']/@cropbox"/>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@mediabox">
			<xsl:text>Mediabox (mm): "</xsl:text>
			<xsl:call-template name="get_pagebox">
				<xsl:with-param name="get_dimension" select="'mm'"/>
				<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@mediabox"/>
			</xsl:call-template>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@trimbox">
			<xsl:text>Trimbox (mm): "</xsl:text>
			<xsl:call-template name="get_pagebox">
				<xsl:with-param name="get_dimension" select="'mm'"/>
				<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@trimbox"/>
			</xsl:call-template>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@mediabox">
			<xsl:text>Mediabox (in): "</xsl:text>
			<xsl:call-template name="get_pagebox">
				<xsl:with-param name="get_dimension" select="'in'"/>
				<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@mediabox"/>
			</xsl:call-template>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:if test="//pi4:page/@trimbox">
			<xsl:text>Trimbox (in): "</xsl:text>
			<xsl:call-template name="get_pagebox">
				<xsl:with-param name="get_dimension" select="'in'"/>
				<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@trimbox"/>
			</xsl:call-template>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="$newline"/>
		</xsl:if>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_RESULTS'"/>
			
		</xsl:call-template>
		<xsl:text> (</xsl:text>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_SUMMARY'"/>
			
		</xsl:call-template>
		<xsl:text>)</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:text>================================================================================</xsl:text>
		<xsl:value-of select="$newline"/>
		<xsl:variable name="hits_error"
			select="/pi4:report/pi4:results/pi4:hits[@severity='Error'  ]"/>
		<xsl:variable name="hits_warning"
			select="/pi4:report/pi4:results/pi4:hits[@severity='Warning']"/>
		<xsl:variable name="hits_info"
			select="/pi4:report/pi4:results/pi4:hits[@severity='Info'   ]"/>
		<xsl:if test="count($hits_error  ) > 0">
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_ERROR'"/>
				
			</xsl:call-template>
			<xsl:text> (</xsl:text>
			<xsl:value-of select="count($hits_error  )"/>
			<xsl:text>)</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:apply-templates mode="result_summary" select="$hits_error"/>
		</xsl:if>
		<xsl:if test="count($hits_warning) > 0">
			<xsl:value-of select="$newline"/>
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_WARNING'"/>
				
			</xsl:call-template>
			<xsl:text> (</xsl:text>
			<xsl:value-of select="count($hits_warning  )"/>
			<xsl:text>)</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:apply-templates mode="result_summary" select="$hits_warning"/>
		</xsl:if>
		<xsl:if test="count($hits_info   ) > 0">
			<xsl:value-of select="$newline"/>
			<xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_INFO'"/>
				
			</xsl:call-template>
			<xsl:text> (</xsl:text>
			<xsl:value-of select="count($hits_info  )"/>
			<xsl:text>)</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:apply-templates mode="result_summary" select="$hits_info"/>
		</xsl:if>
		<xsl:value-of select="$newline"/>
		<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_ENDOFREPORT'"/>
			
		</xsl:call-template>
	</xsl:template>

	<!--  Results summary template -->
	<xsl:template match="*" mode="result_summary">
		<xsl:text>- </xsl:text>
		<xsl:value-of
			select="/pi4:report/pi4:profile_info/pi4:rules/pi4:rule[@id=current()/@rule_id]/pi4:display_name"/>
		<xsl:text> </xsl:text>
		<xsl:call-template name="matches"/>
		<xsl:value-of select="$newline"/>
	</xsl:template>

	<!--  Results (detailed info) -->
	<xsl:template match="/pi4:report/pi4:results" mode="result_detailed">
		<xsl:for-each select="pi4:hits">
			<xsl:text>--------------------------------------------------------------------------------
	</xsl:text>
			<xsl:value-of
				select="/pi4:report/pi4:profile_info/pi4:rules/pi4:rule[@id=current()/@rule_id]/pi4:display_name"/>
			<xsl:text> </xsl:text>
			<xsl:call-template name="matches"/>
			<xsl:text> *</xsl:text>
			<xsl:value-of select="@severity"/>
			<xsl:text>*</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:text>--------------------------------------------------------------------------------</xsl:text>
			<xsl:value-of select="$newline"/>
			<xsl:variable name="hits" select="."/>
			<xsl:for-each select="/pi4:report/pi4:document/pi4:pages/pi4:page">
				<xsl:variable name="hits_on_page"
					select="$hits/pi4:hit[@type!='PageInfo' and (@page=current()/@id or @page=current()/@nr)]"/>
				<xsl:if test="count($hits_on_page) > 0">
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGE'"/>
					</xsl:call-template>
					<xsl:value-of select="@nr"/>
					<xsl:text> ***</xsl:text>
					<xsl:value-of select="$newline"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<!--  Results (detailed info) "PageInfo"-->
	<!--  Example: Page 1: 590.0 pt by 782.0 pt 0 pt bleed -->
	<!--  Candidates: mediabox="0.0/0.0/590.0/782.0" trimbox="0.0/0.0/590.0/782.0" bleedbox="0.0/0.0/590.0/782.0" -->
	<xsl:template match="/pi4:report/pi4:results/pi4:hits/pi4:hit[@type='PageInfo']"
		mode="result_detailed_hit">
		<xsl:variable name="page"
			select="/pi4:report/pi4:document/pi4:pages/pi4:page[@id=current()/@page or @nr=current()/@page]"/>
		<xsl:text>Page </xsl:text>
		<xsl:value-of select="$page/@nr"/>
		<xsl:text>: </xsl:text>
		<xsl:value-of select="$page/@trimbox"/>
		<xsl:value-of select="$newline"/>
	</xsl:template>

	<!--  Results (detailed info) "DocumentInfo"-->
	<xsl:template match="/pi4:report/pi4:results/pi4:hits/pi4:hit[@type='DocumentInfo']"
		mode="result_detailed_hit"> </xsl:template>

	<!--  (n matches) -->
	<!--  (n matches on m pages) -->
	<xsl:template name="matches" match="/pi4:report/pi4:results/pi4:hits">
		<xsl:variable name="n">
			<xsl:value-of select="count(pi4:hit)"/>
		</xsl:variable>
		<xsl:variable name="m">
			<xsl:value-of select="count(pi4:hit[@page and not(@page=following::*/@page)])"/>
		</xsl:variable>
		<xsl:text>(</xsl:text>
		<xsl:value-of select="$n"/>
		<xsl:text> </xsl:text>
		<xsl:choose>
			<xsl:when test="$n = 1">
				<xsl:call-template name="doc_strings">
					<xsl:with-param name="doc_string" select="'DICT_MATCH'"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="doc_strings">
					<xsl:with-param name="doc_string" select="'DICT_MATCHES'"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="$m > 0">
			<xsl:text> on </xsl:text>
			<xsl:value-of select="$m"/>
			<xsl:choose>
				<xsl:when test="$m = 1">
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGE'"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGES'"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:text>)</xsl:text>
	</xsl:template>

	<!--  Platenames: (Cyan) (Magenta) (Yellow) (Black) -->
	<!--  /pi4:report/pi4:document/pi4:doc_info/pi4:platenames/pi4:platename -->
	<xsl:template name="doc_platenames" match="/pi4:report/pi4:results/pi4:hits">
		<xsl:for-each select="/pi4:report/pi4:document/pi4:doc_info/pi4:platenames/pi4:platename">
			<xsl:text>(</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>) </xsl:text>
		</xsl:for-each>
	</xsl:template>


	<!--  Alternate_Values: (Cyan) (Magenta) (Yellow) (Black) -->
	<xsl:template name="alt_values" match="/pi4:report/pi4:results/pi4:hits/pi4:hit/pi4:gstate">
		<xsl:variable name="id" select="//pi4:colorspace/@id"/>
		<xsl:for-each select="/pi4:report/pi4:results/pi4:hits/pi4:hit">
			<xsl:text>(</xsl:text>
			<xsl:value-of select="//pi4:gstate/@fill_colorant_values"/>
			<xsl:text>) </xsl:text>
		</xsl:for-each>
	</xsl:template>

	<!--  Retrieve pagebox information in given dimension -->
	<xsl:template name="get_pagebox">
		<xsl:param name="get_dimension" select="'in'"/>
		<xsl:param name="get_box"/>
		<xsl:variable name="t_1" select="substring($get_box,1,string-length($get_box))"/>
		<xsl:variable name="pos_1" select="substring-before($t_1,'/')"/>
		<xsl:variable name="t_2" select="substring-after($t_1,'/')"/>
		<xsl:variable name="pos_2" select="substring-before($t_2,'/')"/>
		<xsl:variable name="t_3" select="substring-after($t_2,'/')"/>
		<xsl:variable name="pos_3" select="substring-before($t_3,'/')"/>
		<xsl:variable name="pos_4" select="substring-after($t_3,'/')"/>
		<xsl:call-template name="calc_dimension">
			<xsl:with-param name="calc_dimension" select="$get_dimension"/>
			<xsl:with-param name="calc_value" select="number($pos_3 - $pos_1)"/>
		</xsl:call-template><xsl:text>/</xsl:text><xsl:call-template name="calc_dimension">
			<xsl:with-param name="calc_dimension" select="$get_dimension"/>
			<xsl:with-param name="calc_value" select="number($pos_4 - $pos_2)"/>
		</xsl:call-template> (<xsl:call-template name="doc_strings">
			<xsl:with-param name="doc_string" select="'DICT_OFFSET'"/>
		</xsl:call-template><xsl:text>: </xsl:text><xsl:call-template name="calc_dimension">
			<xsl:with-param name="calc_dimension" select="$get_dimension"/>
			<xsl:with-param name="calc_value" select="$pos_1"/>
		</xsl:call-template><xsl:text>/</xsl:text><xsl:call-template name="calc_dimension">
			<xsl:with-param name="calc_dimension" select="$get_dimension"/>
			<xsl:with-param name="calc_value" select="$pos_2"/>
		</xsl:call-template><xsl:text>)</xsl:text>
	</xsl:template>


	<!--  Convert pt to in or mm -->
	<xsl:template name="calc_dimension">
		<xsl:param name="calc_dimension" select="'mm'"/>
		<xsl:param name="calc_value"/>

		<xsl:choose>
			<xsl:when test="$calc_dimension = 'mm'">
				<xsl:value-of select="round(number($calc_value * 3.527778)) div 10"/>
			</xsl:when>
			<xsl:when test="$calc_dimension = 'in'">
				<xsl:value-of select="round(number($calc_value div 72 * 1000)) div 1000"/>

			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Get localized strings -->

	<xsl:template name="doc_strings">
		<xsl:param name="doc_lang" select="/pi4:report/pi4:information/pi4:report_language"/>
		<xsl:param name="doc_string"/>

		<xsl:choose>
			<!-- Chinese -->
			<xsl:when test="$doc_lang = 'chs' or $doc_lang = 'zh_CN'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>结果</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>小结</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>使用的配置文件</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>错误</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>警告</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>信息</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>文件名</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>路径</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>页数</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>PDF 版本号</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>文件大小</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>标题</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>作者</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>创建程序</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>制作程序</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>创建日期</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>修改日期</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>陷印</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>PDF 标准</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>输出方法</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>印版数</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>印版名称</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ 报告结尾 pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text> 页</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text> 页</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text> 个匹配项</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text> 个匹配项</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>信息</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>文档</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>偏移量</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>颜色</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>标准</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- German -->
			<xsl:when test="$doc_lang = 'de' or $doc_lang = 'DEU' or $doc_lang = 'GER'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Resultate</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Zusammenfassung</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>Benutztes Profil</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>Fehler</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>Warnung</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Dateiname</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>Pfad</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>Anzahl der Seiten</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>PDF Versionsnummer</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Dateigrösse</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titel</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>Autor</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Erstellt mit</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Produziert mit</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Erstellt am</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Geändert am</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Überfüllt</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>PDF Standard</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>Ausgabebedingung</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ADDITIONALACTIONS'">
						<xsl:text>Zusätzliche Actionen: nicht enthalten</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>Anzahl der Platten</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>Namen der Platten</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ Ende des Reports pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text>Seite</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text>Seiten</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text>Übereinstimmung</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text>Übereinstimmungen</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>Dokument</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>Versatz</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>Farbe</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>Standard</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- Spanish -->
			<xsl:when test="$doc_lang = 'es' or $doc_lang = 'ESP'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Resultados</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Resumen</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>Perfil usado</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>Advertencia</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>Información</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nombre del archivo</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>Trazado</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>Número de páginas</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>Número de versión de PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Tamaño de archivo</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Título</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>Autor</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creador</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Productor</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Creado el</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modificado</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Reventado</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>Estándar PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>Calidad de salida</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>Número de placas</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>Nombres de las placas</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ Fin del informe pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text> página</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text> páginas</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text> coincidencia</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text> coincidencias</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>información</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>Documento</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>Offset</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>Color</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>Estándar</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- French -->
			<xsl:when test="$doc_lang = 'fr' or $doc_lang = 'FRE' or $doc_lang = 'FRA'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Résultats</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Résumé</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>Profil utilisé</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>Erreur</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>Avertissement</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>Infos</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nom du fichier</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>Chemin</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>Nombre de pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>Numéro de version PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Taille de fichier</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titre</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>Auteur</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Application d'origine</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Mode de conversion</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Date de création</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Date de modification</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Recouvrement</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>Norme PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>Mode de sortie</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>Nombre de plaques</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>Nom des plaques</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ Fin du rapport pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text> page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text> pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text> occurrence</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text> occurrences</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>informations</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>Document</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>Offset</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>Couleur</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>Norme</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- Italian -->
			<xsl:when test="$doc_lang = 'it' or $doc_lang = 'ITA'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Risultati</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Riepilogo</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>Profilo utilizzato</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>Errore</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>Avviso</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>Info</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nome file</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>Percorso</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>Numero di pagine</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>Numero versione PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Dimensione file</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titolo</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>Autore</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creato in</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Prodotto con</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Creato il</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modificato il</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Trapping</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>Standard PDF</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>Intento di output</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>Numero di lastre</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>Nomi delle lastre</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ Fine del rapporto pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text> pagina</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text> pagine</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text> corrispondenza</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text> corrispondenze</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>informazioni</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>Documento</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>Offset</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>Colore</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>Standard</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- Japanese -->
			<xsl:when test="$doc_lang = 'jp' or $doc_lang = 'JPN'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>##Results</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>##Summary</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>##Used profile</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>##Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>##Warning</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>##Info</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>##File name</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>##Path</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>##Number of pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>##PDF version number</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>##File size</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>##Title</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>##Author</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>##Creator</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>##Producer</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>##Created</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>##Modified</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>##Trapping</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>##PDF Standard</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>##Output Intent</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>##Number of plates</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>##Names of plates</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>##+++ End of Report pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text>## page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text>## pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text>## match</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text>## matches</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>##information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>##Document</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>##Offset</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>##Color</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>##Standard</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- English -->
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Results</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Summary</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USEDPROFILE'">
						<xsl:text>Used profile</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>Warning</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>Info</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>File name</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PATH'">
						<xsl:text>Path</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPAGES'">
						<xsl:text>Number of pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFVERSIONNUMBER'">
						<xsl:text>PDF version number</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>File size</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Title</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_AUTHOR'">
						<xsl:text>Author</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creator</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Producer</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Created</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modified</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Trapping</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDFXVERSION'">
						<xsl:text>PDF Standard</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUTINTENT'">
						<xsl:text>Output Intent</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NUMBEROFPLATES'">
						<xsl:text>Number of plates</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NAMESOFPLATES'">
						<xsl:text>Names of plates</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ENDOFREPORT'">
						<xsl:text>+++ End of Report pdfToolbox Server - http://www.callassoftware.com ++++</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE'">
						<xsl:text> page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGES'">
						<xsl:text> pages</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCH'">
						<xsl:text> match</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MATCHES'">
						<xsl:text> matches</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFORMATION'">
						<xsl:text>information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT'">
						<xsl:text>Document</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OFFSET'">
						<xsl:text>Offset</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR'">
						<xsl:text>Color</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_STANDARD'">
						<xsl:text>Standard</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
