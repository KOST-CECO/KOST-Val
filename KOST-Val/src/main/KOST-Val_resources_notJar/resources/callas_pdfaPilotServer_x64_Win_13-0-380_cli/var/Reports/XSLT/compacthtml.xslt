<?xml version="1.0" encoding="UTF-8"?>
<!-- 20090909: Filesize in MBytes or KBytes -->
<!-- 20090909: only output size of trimbox if different from media -->

<!-- 20100910: reporting of fixups -->
<!-- 20100910: new Strings: DICT_PDF_FIXUPS_FAIL, DICT_PDF_FIXUPS_SUCCESS -->
<!-- 20100913: only print PDF Standards Info if entry is present -->
<!-- 20120227: added more languages codes -->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:pi4="http://www.callassoftware.com/namespace/pi4">
	<xsl:output method="html"/>

	<xsl:template match="/">
		<html>
			<head>
				<style>
						
						body { 
						font-family: Verdana; font-size: 9pt;
						}
						
						div.General {
						padding: 4px;
						margin: 20pt 20pt;
						
						}
						
						div.Head {
						margin-bottom: 15px;
						}
						
						table.information {
						border-width: 1px;
						border-style: none;
						border-color: gray;
						border-collapse: separate;
						background-color: rgb(250, 240, 230);
						width: 500px;
						margin-bottom: 15px;
						}
						
					
						th.property_head {
						border-width: 1px;
						border-style: none;
						border-color: blue;
						background-color: rgb(255, 245, 239);
						font-weight: bold;
						font-size: 12pt;
						text-align:left;
						height:40px;
						valign="top";
						}
						
						
						td.property_name {
						border-width: 1px;
						padding: 1px;
						border-style: none;
						border-color: blue;
						background-color: rgb(255, 245, 239);
						font-size: 10pt;
						width:145px;
						}
						
						td.property_content {
						border-width: 1px;
						padding: 1px;
						border-style: none;
						border-color: blue;
						background-color: rgb(255, 245, 239);
						font-weight: bold;
						font-size: 10pt;
						}
						
						div.Infoline {
						font-size: 8pt;
						}
						
						div.DocInfoHeader {
						font-weight: bold;
						font-size: 12pt;
						}
						
						div.DocInfo {
						padding-left: 16px;
						margin-bottom: 5px;
						}
						
						div.DocInfoGroup {
						margin-bottom: 15px;
						}
						
						table.result {
						border-width: 1px;
						border-style: none;
						border-color: gray;
						border-collapse: separate;
						background-color: rgb(255, 245, 239);
						width: 990px;
						margin-bottom:15px;
						}
						
						th.result_head {
						border-width: 1px;
						border-style: none;
						border-color: blue;
						font-weight: bold;
						font-size: 12pt;
						text-align:left;
						height:40px;
						valign="top";
						}
							
						td.result_content {
						border-width: 1px;
						padding: 1px;
						border-style: none;
						font-weight: bold;
						font-size: 10pt;
						}
						
						td.result_cell_error,
						td.result_cell_warning,
						td.result_cell_info {
						padding: 0px;
						padding-bottom:4px;
						padding-left: 4px;	
						border-left: 6px solid;
						border-bottom: 1px solid;
						clear: both;
						font-size: 12pt;
						}
						
						td.result_cell_error {
						border-color: red;
						color: red;
						}
						
						td.result_cell_warning {
						border-color: orange;
						color: orange;
						}
						
						td.result_cell_info {
						border-color: silver;
						color: gray;
						}
						
						div.ResultPropertyHit {
						font-size: 10pt;
						margin-top: 10px;
						display: list-item;
						}
						
						div.ResultPropertyInfo {
						font-size: 9pt;
						}
						
						div.SystemInfo {
						font-size: 8pt;
						}
						
					</style>

				<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
				<title>callas pdfToolbox Report</title>
			</head>
			<body>
				<div class="General">
					<div class="Head">
						<table width="1006">
							<tr>
								<td width="32">
									<xsl:call-template name="pdfToolbox_icon"/>
								</td>
								<td>
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_FILE'"/>
									</xsl:call-template>
									<b>
										<xsl:if test="//pi4:doc_info">
											<xsl:value-of select="//pi4:filename"/>
										</xsl:if>
									</b>
								</td>
							</tr>
							<tr>
								<td/>
								<td>
									<div class="Infoline">
										<xsl:text/>
										<xsl:call-template name="doc_strings">
											<xsl:with-param name="doc_string"
												select="'DICT_PREFLIGHT_PROFILE'"/>
										</xsl:call-template>
										<b>
											<xsl:if test="//pi4:profile_info">
												<xsl:value-of select="//pi4:profile_name"/>
											</xsl:if>
										</b>
									</div>
								</td>
								<td align="right" valign="bottom" width="150">
									<div class="Infoline">
										<xsl:text>callas pdfToolbox</xsl:text>
										<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABYAAAAQCAYAAAAS7Y8mAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAfVJREFUeNqMVM1Kw0AQ3i0+QC62FH+Ib1CfIInoRRAtCOrJBk+erAfP6tmDFEQ8ab0IekkVBFExyRO0PoFBEf8uudu6frvZ2DWNpgMfuzs78+3M7OxSkiHuoFHBsAyYiS0POLY+/HqaH/2L8G7Q0DE4QCnj7BZQnvjwg0zi2zxIGWliU1PUgQRJRs8ICcE0PvnuB8KXkLmBNOIvREop0Vg3ovWpd9+L92/yBj/wiBNIFbd1rvMGP0RkmEuSXuWNCohLHcyBAHNLJeWCdYi9UNoIcB/Fj/RE/EXFRcViT7/5IZ9cFoxdpd7nIKn8V/ge4k63fuHMWxTpRcGogqiqmJlZ3dRLTKl62zILqpE+Bd42hsYv4rOiWWqzn5vWY32b9C/zr57oa3pSNDWcsslvmCpkknxs6cULToumjvlDgmMb9gFLPJ7FF0+knGOUp0+qgN7GPAFxQQsgx9xW9HUQbEFf/6TEV/TerwdyNGQ6Sk+qEvJ2W3n2WmlpHw6ZOgiapPuQbPs5KkVO1vAcJxMBpCcQrTVk4h4Mmz1dAF0JETrcRvq1YtKfroBBA7UysF9bRXT7w6KmTRJ1A4e7N2I1MN5Lv9mOfAzRZbAQzVTu6xOqjVic3GEZnxAIWkB57ckN+iKOZWfUqrD0b5MT1jYe3dRv81uAAQAIdcJiKQ0HYgAAAABJRU5ErkJggg=="/>
									</div>
								</td>
							</tr>
						</table>
					</div>

					<div class="DocInfoGroup">
						<div class="DocInfo"/>
						<div class="DocInfoHeader"/>
						<xsl:apply-templates
							select="//pi4:doc_info | //pi4:results | //pi4:information"/>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<!-- DOCUMENT INFORMATION -->
	<xsl:template match="//pi4:document/pi4:doc_info" name="test">
		<table>
			<tr>
				<td valign="top">
					<table class="information">
						<tr>
							<th class="property_head" colspan="2">
								<xsl:call-template name="Document_Information_Icon"/>
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_DOCUMENT_INFO'"
									/>
								</xsl:call-template>
							</th>
						</tr>
						<tr>
							<td class="property_name">
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_FILENAME'"/>
								</xsl:call-template>
							</td>
							<td class="property_content">
								<xsl:apply-templates select="pi4:filename"/>
							</td>
						</tr>
						<xsl:if test="pi4:title">
							<tr>
								<td class="property_name">
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_TITLE'"/>
									</xsl:call-template>
								</td>
								<td class="property_content">
									<xsl:apply-templates select="pi4:title"/>
								</td>
							</tr>
						</xsl:if>
						<tr>
							<td class="property_name">
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_PDF_VERSION'"/>
								</xsl:call-template>
							</td>
							<td class="property_content">
								<xsl:apply-templates select="pi4:pdfversion"/>
							</td>
						</tr>
						<xsl:if test="pi4:creator">
							<tr>
								<td class="property_name">
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_CREATOR'"/>
									</xsl:call-template>
								</td>
								<td class="property_content">
									<xsl:apply-templates select="pi4:creator"/>
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="pi4:producer">
							<tr>
								<td class="property_name">
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_PRODUCER'"/>
									</xsl:call-template>
								</td>
								<td class="property_content">
									<xsl:apply-templates select="pi4:producer"/>
								</td>
							</tr>
						</xsl:if>
						<tr>
							<td class="property_name">
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_FILESIZE'"/>
								</xsl:call-template>
							</td>
							<td class="property_content">
								<xsl:variable name="fs">
									<xsl:value-of select="pi4:filesize_byte"/>
								</xsl:variable>
							<xsl:choose>
								<xsl:when test="$fs &gt; 1000000">
									<xsl:value-of select="format-number(round(number($fs) div 1024 div 1024 * 10) div 10,'0.0')"/>
									<xsl:text> MByte</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="format-number(round(number($fs) div 1024 * 10) div 10,'0.0')"/>
									<xsl:text> KByte</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
								
							</td>
						</tr>
						<tr>
							<td class="property_name">
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_CREATED'"/>
								</xsl:call-template>
							</td>
							<td class="property_content">
								<xsl:apply-templates select="pi4:created"/>
							</td>
						</tr>
						<xsl:if test="pi4:modified">
							<tr>
								<td class="property_name">
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_MODIFIED'"/>
									</xsl:call-template>
								</td>
								<td class="property_content">
									<xsl:apply-templates select="pi4:modified"/>
								</td>
							</tr>
						</xsl:if>

					</table>
				</td>
				<td valign="top">
					<!-- COLOR INFORMATION -->
					<table class="information">
						<tr>
							<th class="property_head" colspan="2">
								<xsl:call-template name="Color_Information_Icon"/>
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_COLOR_INFO'"/>
								</xsl:call-template>
							</th>
						</tr>
						<tr>
							<td class="property_name">
								<xsl:text/>
								<xsl:call-template name="doc_strings">
									<xsl:with-param name="doc_string" select="'DICT_SEPERATION'"/>
								</xsl:call-template>
							</td>
							<td class="property_content">
								<xsl:value-of select="//pi4:doc_info/pi4:plates"/>
							</td>
						</tr>
						<xsl:for-each select="//pi4:doc_info/pi4:platenames/pi4:platename">
							<tr>
								<td class="property_name">
									<xsl:text/>
									<xsl:call-template name="doc_strings">
										<xsl:with-param name="doc_string" select="'DICT_PLATENAME'"
										/>
									</xsl:call-template>
								</td>
								<td class="property_content">
									<xsl:value-of select="."/>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</table>

		<table>
			<tr>
				<td valign="top">
					<xsl:call-template name="page_info"/>
				</td>
				<td valign="top">
					<xsl:if test="//pi4:pdfxversion">
						<xsl:call-template name="PDF_Standard"/>
					</xsl:if>
				</td>
			</tr>
		</table>
		<!-- <xsl:call-template name="fonts_info"/> -->
	</xsl:template>



	<!-- RESULT INFORMATION -->
	<xsl:template match="pi4:results">

		<table class="information">
			<tr>
				<th class="property_head" colspan="2">
					<xsl:text> </xsl:text>
					<xsl:call-template name="Results_Icon"/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_RESULTS'"/>
					</xsl:call-template>
				</th>
			</tr>

			<xsl:if test="count(//pi4:results/pi4:fixup[@severity='SUCCESS'])&gt;0">
						<tr>
							<td class="result_cell_info">
								
								<table class="result">
									<tr>
										<th class="result_head" width="32">
											<xsl:call-template name="Info"/>
										</th>
										<th class="result_head">
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
													select="'DICT_PDF_FIXUPS_SUCCESS'"/>
											</xsl:call-template>
											<xsl:value-of
												select="count(//pi4:results/pi4:fixup[@severity='SUCCESS'])"/>
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
													select="'DICT_INFO'"/>
											</xsl:call-template>
										</th>
									</tr>
									<tr>
										<td/>
										<td> </td>
									</tr>
									
									
									<xsl:call-template name="fixupSuccess"/>
									
									
								</table>
							</td>
						</tr>
					</xsl:if>
					<xsl:if test="count(//pi4:results/pi4:fixup[@severity='ERROR'])&gt;0">
						<tr>
							<td class="result_cell_error">
								
								<table class="result">
									<tr>
										<th class="result_head" width="32">
											<xsl:call-template name="hit"/>
										</th>
										<th class="result_head">
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
													select="'DICT_PDF_FIXUPS_FAIL'"/>
											</xsl:call-template>
											<xsl:value-of
												select="count(//pi4:results/pi4:fixup[@severity='ERROR'])"/>
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
													select="'DICT_INFO'"/>
											</xsl:call-template>
										</th>
									</tr>
									<tr>
										<td/>
										<td> </td>
									</tr>
									
									
									<xsl:call-template name="fixupError"/>
									
									
								</table>
							</td>
						</tr>
					</xsl:if>
				
			<xsl:choose>
					
				<xsl:when test="//pi4:results/pi4:hits">
					<xsl:if test="count(//pi4:results/pi4:hits[@severity='Error']/pi4:hit)&gt;0">
						<tr>
							<td class="result_cell_error">

								<table class="result">
									<tr>
										<th class="result_head" width="32">
											<xsl:call-template name="hit"/>
										</th>
										<th class="result_head">
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
													select="'DICT_CHECKED_PDF_CONTAIN'"/>
											</xsl:call-template>
											<xsl:value-of
												select="count(//pi4:results/pi4:hits[@severity='Error'])"/>
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_ERROR'"/>
											</xsl:call-template>
										</th>
									</tr>
									<tr>
										<td/>
										<td> </td>
									</tr>


									<xsl:call-template name="fehlerError"/>


								</table>
							</td>
						</tr>
					</xsl:if>
					<xsl:if
						test="count(//pi4:results/pi4:hits[@severity='Warning']/pi4:hit)&gt;0">
						<tr>
							<td class="result_cell_warning">
								<table class="result">
									<tr>
										<th class="result_head" width="32">
											<xsl:call-template name="Warning"/>
										</th>
										<th class="result_head">
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_CHECKED_PDF_CONTAIN'"/>
											</xsl:call-template>
											<xsl:value-of
												select="count(//pi4:results/pi4:hits[@severity='Warning'])"/>
											<xsl:choose>
												<xsl:when
												test="count(//pi4:results/pi4:hits[@severity='Warning']) > 1">

												<xsl:text/>
												<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_WARNINGS'"/>
												</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
												<xsl:text/>
												<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_WARNING'"/>
												</xsl:call-template>
												</xsl:otherwise>
											</xsl:choose>
										</th>
									</tr>

									<xsl:call-template name="fehlerWarning"/>
								</table>
							</td>
						</tr>
					</xsl:if>

					<xsl:if test="count(//pi4:results/pi4:hits[@severity='Info']/pi4:hit)&gt;0">
						<tr>
							<td class="result_cell_info">
								<table class="result">
									<tr>
										<th class="result_head" width="32">
											<xsl:call-template name="Info"/>
										</th>
										<th class="result_head">
											<xsl:text/>
											<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_CHECKED_PDF_CONTAIN'"/>
											</xsl:call-template>
											<xsl:value-of
												select="count(//pi4:results/pi4:hits[@severity='Info'])"/>
											<xsl:choose>
												<xsl:when
												test="count(//pi4:results/pi4:hits[@severity='Info']) > 1">

												<xsl:text/>
												<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_INFOS'"/>
												</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
												<xsl:text/>
												<xsl:call-template name="doc_strings">
												<xsl:with-param name="doc_string"
												select="'DICT_INFO'"/>
												</xsl:call-template>
												</xsl:otherwise>
											</xsl:choose>
										</th>
									</tr>
									<xsl:call-template name="fehlerInfo"/>


								</table>

							</td>
						</tr>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<tr>
						<td class="result_cell_info">
							<xsl:text/>
							<xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_NO_PROBLEMS_FOUND'"
								/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:otherwise>
			</xsl:choose>
		</table>

		<!--  
		<table>
			<tr>
				<td>
					<xsl:call-template name="Results"/>
				</td>
				<td>
				<b><xsl:text></xsl:text><xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_DETAILED_INFO'"/>
				</xsl:call-template></b>
				</td>
			</tr>
		</table>
		<xsl:choose>
			<xsl:when test="//pi4:hit/@page">
				<table>
					<tr>
						<td/>
						<td>
							<xsl:call-template name="failsonpages"/>
						</td>
					</tr>
				</table>
			</xsl:when>
			<xsl:otherwise>
				<p>
				<b><xsl:text></xsl:text><xsl:call-template name="doc_strings">
				<xsl:with-param name="doc_string" select="'DICT_NO_HITS_ON_PAGE'"/>
				</xsl:call-template></b>
				</p>
			</xsl:otherwise>
		</xsl:choose>
		-->
	</xsl:template>

	<xsl:template match="//pi4:information">
		<div class="SystemInfo">
			<p>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PRODUCT'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:product_name"/>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_DATE'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:date_time"/>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_USERNAME'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:username"/>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_COMPUTERNAME'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:computername"/>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_OPERATING'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:operating_system"/>
				<b>
					<xsl:text> </xsl:text>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_DURATION'"/>
					</xsl:call-template>
				</b>
				<xsl:apply-templates select="//pi4:duration"/>
			</p>
		</div>
	</xsl:template>

	<!-- CALL_TEMPLATES -->
	<xsl:variable name="CountHits" select="//@id"/>
	
	<xsl:template name="fixupSuccess">
		<xsl:for-each select="pi4:fixup[@severity='SUCCESS']">
			<xsl:variable name="thefixup" select="@fixup_id"/>
			<xsl:variable name="counthitsonpage" select="//pi4:fixup/@fixup_id"/>
			<tr>
				<td/>
				<td>
					<div class="ResultProperty">
						<div class="ResultPropertyHit">
							<b>
								<xsl:value-of
									select="//pi4:fixups/pi4:fixup[@fixup_id=$thefixup]/pi4:display_name"
								/>
							</b><xsl:text> (</xsl:text><xsl:if test="//pi4:fixup">
								<xsl:value-of select="@count"/>
							</xsl:if>
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_HITS'"/>
							</xsl:call-template>)</div>
						
						<div class="ResultPropertyInfo">
							<xsl:value-of
								select="//pi4:fixups/pi4:fixup[@fixup_id=$thefixup]/pi4:display_comment"
							/>
						</div>
					</div>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="fixupError">
		<xsl:for-each select="pi4:fixup[@severity='ERROR']">
			<xsl:variable name="thefixup" select="@fixup_id"/>
			<xsl:variable name="counthitsonpage" select="//pi4:fixup/@fixup_id"/>
			<tr>
				<td/>
				<td>
					<div class="ResultProperty">
						<div class="ResultPropertyHit">
							<b>
								<xsl:value-of
									select="//pi4:fixups/pi4:fixup[@fixup_id=$thefixup]/pi4:display_name"
								/>
							</b><xsl:text> (</xsl:text><xsl:if test="//pi4:fixup">
								<xsl:value-of select="@count_fail"/>
							</xsl:if>
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_HITS'"/>
							</xsl:call-template>)</div>
						
						<div class="ResultPropertyInfo">
							<xsl:value-of
								select="//pi4:fixups/pi4:fixup[@fixup_id=$thefixup]/pi4:display_comment"
							/>
						</div>
					</div>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="fehlerError">
		<xsl:for-each select="pi4:hits[@severity='Error']">
			<xsl:variable name="HitsRuleError" select="@rule_id"/>
			<xsl:variable name="counthitsonpage" select="//pi4:rule/@id"/>
			<tr>
				<td/>
				<td>
					<div class="ResultProperty">
						<div class="ResultPropertyHit">
							<b>
								<xsl:value-of
									select="//pi4:rules/pi4:rule[@id=$HitsRuleError]/pi4:display_name"
								/>
							</b><xsl:text> (</xsl:text><xsl:if test="//pi4:hits">
								<xsl:value-of select="count(pi4:hit)"/>
							</xsl:if>
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_HITS'"/>
							</xsl:call-template>)</div>

						<div class="ResultPropertyInfo">
							<xsl:value-of
								select="//pi4:rules/pi4:rule[@id=$HitsRuleError]/pi4:display_comment"
							/>
						</div>
					</div>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="fehlerWarning">
		<xsl:for-each select="pi4:hits[@severity='Warning']">
			<xsl:variable name="HitsRuleWarning" select="@rule_id"/>
			<tr>
				<td/>
				<td>
					<div class="ResultProperty">
						<div class="ResultPropertyHit">
							<b>
								<xsl:value-of
									select="//pi4:rules/pi4:rule[@id=$HitsRuleWarning]/pi4:display_name"
								/>
							</b> (<xsl:if test="//pi4:hits">
								<xsl:value-of select="count(pi4:hit)"/>
							</xsl:if>
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_HITS'"/>
							</xsl:call-template>)</div>
					</div>
					<div class="ResultPropertyInfo">
						<xsl:value-of
							select="//pi4:rules/pi4:rule[@id=$HitsRuleWarning]/pi4:display_comment"
						/>
					</div>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="fehlerInfo">
		<xsl:for-each select="pi4:hits[@severity='Info']">
			<xsl:variable name="HitsRuleInfo" select="@rule_id"/>
			<tr>
				<td/>
				<td>
					<div class="ResultProperty">
						<div class="ResultPropertyHit">
							<b>
								<xsl:value-of
									select="//pi4:rules/pi4:rule[@id=$HitsRuleInfo]/pi4:display_name"
								/>
							</b> (<xsl:if test="//pi4:hits">
								<xsl:value-of select="count(pi4:hit)"/>
							</xsl:if>
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_HITS'"/>
							</xsl:call-template>)</div>
					</div>
					<div class="ResultPropertyInfo">
						<xsl:value-of
							select="//pi4:rules/pi4:rule[@id=$HitsRuleInfo]/pi4:display_comment"
						/>
					</div>
				</td>
			</tr>

		</xsl:for-each>
	</xsl:template>

	<xsl:template name="failsonpages">
		<xsl:variable name="pageid" select="//pi4:page/@id"/>
		<font size="2pt">
			<p>
				<xsl:for-each select="//pi4:hit[@page=$pageid]/parent::pi4:hits">
					<xsl:variable name="HitsRule" select="@rule_id"/>
					<p>
						<b>
							<xsl:value-of
								select="//pi4:rules/pi4:rule[@id=$HitsRule]/pi4:display_name"/>
						</b>
					</p>
					<xsl:variable name="hits" select="."/>
					<xsl:for-each select="/pi4:report/pi4:document/pi4:pages/pi4:page">
						<xsl:variable name="hits_on_page"
							select="$hits/pi4:hit[@type!='PageInfo' and (@page=current()/@id or @page=current()/@nr)]"/>
						<xsl:if test="count($hits_on_page) > 0">
							<xsl:text/><xsl:call-template name="doc_strings">
								<xsl:with-param name="doc_string" select="'DICT_ON_PAGE'"/>
							</xsl:call-template>
							<xsl:value-of select="@nr"/>:<br/>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</p>
		</font>
	</xsl:template>

	<xsl:template name="fonts_info" match="//pi4:fonts">
		<table>
			<tr>
				<td>
					<xsl:call-template name="logo_callas"/>
				</td>
				<td>
					<b>
						<xsl:text/>
						<xsl:call-template name="doc_strings">
							<xsl:with-param name="doc_string" select="'DICT_FONTS_INFO'"/>
						</xsl:call-template>
					</b>
				</td>
			</tr>
		</table>
		<div class="Property">
			<p>
				<xsl:text/>
				<xsl:call-template name="doc_strings">
					<xsl:with-param name="doc_string" select="'DICT_FONTS'"/>
				</xsl:call-template>
				<xsl:for-each select="//pi4:font/pi4:name">
					<br/>
					<b>
						<xsl:value-of select="."/>
					</b>
				</xsl:for-each>
			</p>
		</div>
	</xsl:template>

	<!-- PAGE INFORMATION (1st Page)-->

	<xsl:template name="page_info">

		<table class="information">
			<tr class="property_head">
				<th class="property_head" colspan="2">
					<xsl:call-template name="Page_Information_Icon"/>
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGE_INFO'"/>
					</xsl:call-template>
				</th>
			</tr>
			<tr>
				<td class="property_name">
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_COUNT_PAGES'"/>
					</xsl:call-template>
				</td>
				<td class="property_content">
					<xsl:value-of select="count(//pi4:page)"/>
				</td>
			</tr>
			<tr>
				<td class="property_name">
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGE_SIZE'"/>
					</xsl:call-template>
					<xsl:text> (in)</xsl:text>
				</td>
				<td class="property_content">
					<xsl:if test="//pi4:page/@trimbox and //pi4:page[@nr='1']/@trimbox != //pi4:page[@nr='1']/@mediabox">
						<xsl:call-template name="get_pagebox">
							<xsl:with-param name="get_dimension" select="'in'"/>
							<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@trimbox"/>
						</xsl:call-template>
						<xsl:text> (</xsl:text>
					</xsl:if>
					<xsl:call-template name="get_pagebox">
						<xsl:with-param name="get_dimension" select="'in'"/>
						<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@mediabox"/>
					</xsl:call-template>
					<xsl:if test="//pi4:page/@trimbox and //pi4:page[@nr='1']/@trimbox != //pi4:page[@nr='1']/@mediabox">
						<xsl:text>)</xsl:text>
					</xsl:if>
				</td>
			</tr>

			<tr>
				<td class="property_name">
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PAGE_SIZE'"/>
					</xsl:call-template>
					<xsl:text> (mm)</xsl:text>
				</td>
				<td class="property_content">
					<xsl:if test="//pi4:page/@trimbox and //pi4:page[@nr='1']/@trimbox != //pi4:page[@nr='1']/@mediabox">
						<xsl:call-template name="get_pagebox">
							<xsl:with-param name="get_dimension" select="'mm'"/>
							<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@trimbox"/>
						</xsl:call-template>
						<xsl:text> (</xsl:text>
					</xsl:if>
					<xsl:call-template name="get_pagebox">
						<xsl:with-param name="get_dimension" select="'mm'"/>
						<xsl:with-param name="get_box" select="//pi4:page[@nr='1']/@mediabox"/>
					</xsl:call-template>
					<xsl:if test="//pi4:page/@trimbox and //pi4:page[@nr='1']/@trimbox != //pi4:page[@nr='1']/@mediabox">
						<xsl:text>)</xsl:text>
					</xsl:if>
				</td>
			</tr>

			<xsl:if test="//pi4:userunit">
			<tr>
				<td class="property_name">
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_USER_UNIT'"/>
					</xsl:call-template>
				</td>
				<td class="property_content">
					<xsl:value-of select="//pi4:userunit"/>
				</td>
			</tr>
			</xsl:if>

		</table>
	</xsl:template>

	<!-- PDF STANDARD INFORMATION -->
	<xsl:template name="PDF_Standard">

		<table class="information">
			<tr class="property_head">
				<th class="property_head" colspan="2">
					<xsl:call-template name="PDF_Standard_Icon"/>
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_SEITEN_STANDARD'"/>
					</xsl:call-template>
				</th>
			</tr>

			<tr>
				<td class="property_name">
					<xsl:text/>
					<xsl:call-template name="doc_strings">
						<xsl:with-param name="doc_string" select="'DICT_PDF_STANDARD'"/>
					</xsl:call-template>
				</td>
				<td class="property_content">
					<xsl:if test="//pi4:pdfxversion">

						<xsl:value-of select="//pi4:pdfxversion"/>
					</xsl:if>
				</td>
			</tr>

			<xsl:if test="//pi4:output_intent">
				<tr>
					<td class="property_name">
						<xsl:text/>
						<xsl:call-template name="doc_strings">
							<xsl:with-param name="doc_string" select="'DICT_OUTPUT_INTENT'"/>
						</xsl:call-template>
					</td>
					<td class="property_content">
						<xsl:value-of select="//pi4:output_profilename"/>
					</td>
				</tr>
			</xsl:if>

			<xsl:if test="//pi4:trapped">
				<tr>
					<td class="property_name">
						<xsl:text/>
						<xsl:call-template name="doc_strings">
							<xsl:with-param name="doc_string" select="'DICT_TRAPPING'"/>
						</xsl:call-template>
					</td>
					<td class="property_content">
						<xsl:value-of select="//pi4:trapped"/>
					</td>
				</tr>

			</xsl:if>
		</table>
	</xsl:template>


	<xsl:template name="logo_callas">
        <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABYAAAAQCAYAAAAS7Y8mAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAfVJREFUeNqMVM1Kw0AQ3i0+QC62FH+Ib1CfIInoRRAtCOrJBk+erAfP6tmDFEQ8ab0IekkVBFExyRO0PoFBEf8uudu6frvZ2DWNpgMfuzs78+3M7OxSkiHuoFHBsAyYiS0POLY+/HqaH/2L8G7Q0DE4QCnj7BZQnvjwg0zi2zxIGWliU1PUgQRJRs8ICcE0PvnuB8KXkLmBNOIvREop0Vg3ovWpd9+L92/yBj/wiBNIFbd1rvMGP0RkmEuSXuWNCohLHcyBAHNLJeWCdYi9UNoIcB/Fj/RE/EXFRcViT7/5IZ9cFoxdpd7nIKn8V/ge4k63fuHMWxTpRcGogqiqmJlZ3dRLTKl62zILqpE+Bd42hsYv4rOiWWqzn5vWY32b9C/zr57oa3pSNDWcsslvmCpkknxs6cULToumjvlDgmMb9gFLPJ7FF0+knGOUp0+qgN7GPAFxQQsgx9xW9HUQbEFf/6TEV/TerwdyNGQ6Sk+qEvJ2W3n2WmlpHw6ZOgiapPuQbPs5KkVO1vAcJxMBpCcQrTVk4h4Mmz1dAF0JETrcRvq1YtKfroBBA7UysF9bRXT7w6KmTRJ1A4e7N2I1MN5Lv9mOfAzRZbAQzVTu6xOqjVic3GEZnxAIWkB57ckN+iKOZWfUqrD0b5MT1jYe3dRv81uAAQAIdcJiKQ0HYgAAAABJRU5ErkJggg=="/>
	</xsl:template>

	<xsl:template name="pdfToolbox_icon">
		
        <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAACLdJREFUeNrcWw1QVPUWP3elLD50U4FAi0URFpEEFT+e8NiFpFCQRdOorKQpnTITEpve6/V8jNnY9J7QWJROzW7Oe5qKLgR+AOIuo1YjIYurAstH6xcm2Aj5kaZw3/lfVkZk73L3fqzgb+bMvXv/93/uOb//Oef/vx9LgcQwjFepcENkEorctu8IJpR2lArbvlHdYGyXyj5KbIUHx6uJkxoAOqV7KwqMaGohbnVxDYb2AUlA+Xh1BCpbibtLJA6qAhrgs/gGg3FAEFAerFbgRsshtMUGISA73iKMCN4EHAiOI6G+BiUD7i8KUDKfthy0uoyAsuB4jW3U5TAwQOpC9mxLea7kBJQGx+cMgFF3FA3pCZbydtEJ2B/ytBxP1t+HXHcWJiySqc/WH7CKRsC+kNkk1A14cgQMAtDdKaFOrC8zCSZgrzKBcR4GifP31AX1nLpSE28CipXPDKqRZ4uEpLoSEy8CipTPGgZBznOpCep5dfvtFkY3tl7fhybm0IPfebClLpmyUzlHQEHoHDLP6+HBQqamdm9uvwTsmjCXTHe/SLnICX3rRRiuHAsPDfNgft/6/Rp01DVDbd5WqYti5PxTe6wOU4AGag0tofPuo31AufzFPsf94mdAa+UJaKs0S3VpuS0V1HcflN39Y0fYPEUXRWWggFQyatpTrBaOmhYOUl4bRbUjLFnDSkAXUNouPCRUhivHgZuXl/22kLHsBESFO9Qphm0oOXYJ2DpRQ0ZfJZTlsOVpkJCfA0mlm0GhievT7h01kZUAH2zrGzHhjC6ic9q6d8SIAgX6uqRPEfxveKpW6MOMmWtXwNiUXikGFytPgvnL7eAbFca0efj7ONTx55VrcO7gUWguNIBycRKMiZvWq50c//HDjYKfJSw269U9BOjCF5DKf1mIxklvLmLEFWhCEo58+IVQNYGvmndZmRSgKZmmi8L84ClyZaDLnCcYh5FEokmIzSgre6bBTopKEWJQx4VLTOg+7OXhMhJaqmqJ3UJUkNkgU9Zd/TECBFTWG1duwI+fbnGZ81Vf5UNHy29CZwPF5og0hezLiBdUhEmhUlt0CFp+rpXc+SstbXB8awmIYXMnUCo3zAXRbniObtoNmqkf9HvepfrTUIeEXbKcZn57+XmDcl4MjJ4S2m/fyk16+OPqH1i+ZWKYHOvWSckmiUWA/9QJDttvXrkOe1blwvmqunta6uFU8WEkQAkxWYvBO/hJVh1+U0PhJJ4rEhSyTpDJUUAMUSbHOHQ+f9l6OFNlYe1P2vKXroc2yxn2Gym8hpuXJ4hks4r695R0mi99w/1HwhgctSemhIAPjpqjkdux7BM4W1XPSa938BPwytZs1vbfcdY5i1FE9J3DLSmIfOHGdyqZtTQFZr0xj9O5Z9BQ6zEL5i23a/3acA5OFB+BiUmz7LYP8xsFYUnRjBA0VFSDPutzvgTwKyaORvteWCpM4Ox16itqWAm4F0M93YGvH7wJGOrlzvncCziizl6nBftwfvjJTGsuJsCZwkGWns5e5/rVG07Zch8I4F47fLCoNVU3SpZixBa+fshu43TAR5qdcChgcrDT+pWxzr2K4OsHRsAQXsyVflMCjdVNMDYyCMZNDmK2bJjw16fAF0e0peE8J92P+Y2AyXOnOzyHDADR13Sskdnn6we16i8Z1bgR5c3P2pJ18KjnoyxF7TzkrfiCuXHqD+/qVoH/+NGs7eueWwuXL1wWw2RMAWqI9TbmjxhiPnSSfZmMDr258W2MhDGs/YfhwipTl+XQeRJ1bb92gEg2m4ZEBUSH4jSiIlOJUCGVe0biVFbjh430glmamUyIk/P/vNWJBUwGARMDIHZRDCzKWgAjsM0R9moPwNnGCyCGvSg/uREWxLqzUC2M5nTedCRpugOiHF5jUTQc2X9MLJMryDRopEX4WCwkIhAmxYRJ/jxgTJA/khADB3b+III22sh4nh77D0Fvgd09H4F/fb0cRj3umk+GSKq9l/YfpxZLdmDVVnwUyKweMA0KsRgCX5H7jXSZ83cIn5k4BYTYjD4X9LwYwTTQ3ZZhVeQpzc2tUFFS4zICTtWchj27j4IQm5GEb4kuZvVgthpvhAbGR3RRlJLvG5fKHyzg7SsHRZBvL2PbLnaALq8Mrl+7CYpxvpwcPIkO7txyCHwwquQjPHvHbdNFWPf+NriJMwjvt0NAWbcb/vm3nhcjBKnxH6vwl0Ho6Ly9OgniEsKZ/e+2HIbi3ZXofHeuuns8Ahs2vQY+vsNZ+y9bnMeQdgdz50dB2ssx4OE5FE7UnIH1a3b16ONf+yBdX/53XS8CCJJnrxflkxhCADG29S5H7mBl1lyIT7D/ftCMfT5Yva3PcQ8kjugs0leKkUHWorL3A3vuBnvdUFBDssUgoLTslG2h3Xd9XnP8HCsBx7Http0+Hddvgb7gmF19PNDrWVsvAvaVrjbOfmZDAYj3mXsfVJvZb4gafrlklwARYSoreVfHSoBtRsi0fRwlybx2vvUqmFhIqDK3MGt0qYCL3/S+x+wgOnFjBrbkwIMEGnIP71uRyYkAghlz8vTYqnlAnDf9tPetSLuPxNj6YCqk0xQoYJB+JXoX2nHqT2VPCweITN6swBulahg4/wtw3nmaVlcXL+X3qSxBePLXEXjfjOsDatCRQNFdkeai1/l/LH0HyhRtBEaCYRBFQrsM6NTawnRj/zMDRwRpthAStNhjYNcEGsMeaHVjwSucHvQ49SQkIPV/ckwHrZQLJaELHcz51NP6l6zc1wY84L9gewbd/Y+xAZMS6Ehuy67nM3n04wef53aSGUKLU6XqvjqOczyGfGZr/kIjT+KE4bGFuzWYFmTVqHCx71YM9+zLO+frBEaOOPB8vnAJRsSrqFLiiKCtOOLZV7en6ERKHXExNK2YpMZKplBSlDhRQdOkqBWg49/e/C7JJHLtkA6yF/YRAshLl1hbinCNDpMtxMlf6I1d2xJNUtn4fwEGAOORp0Nifz1sAAAAAElFTkSuQmCC"/>
	</xsl:template>

	<xsl:template name="Results_Icon">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZiAL29vKSkpKmpqbu7uoSEhO3t7Xh4eJycnKKionx8fImJiZ2dnePj45CQkJGRkX9%2Ff6GhoYqKiq%2Bvr4GBgaCgoOfn58LCwu7u7oeHh4iIiPHx8fLy8aOjo6enp46OjpSUlJOTk6Wlpaamprq6utHR0Y%2BPj42Njfz8%2FOvr64yMjL29vaioqOfn5uvr6p6enpubm5WVlZaWloODg5qamoKCgurq6qysq%2BLi4enp6eLi4uzs7IaGhuHh4eXl5X19faurq%2B7u7ZeXl%2Bbm5ujo6MHBwbKyst3d3aurqqmpqLa2ttra2q2trcDAwPv7%2B8jIyKysrKWlpJKSkvPz8%2B%2Fv79PT09vb28fHxsvLyrS0tMvLy7q6ufb29piYmJ%2Bfn8rKyuTk5JmZmf%2F%2F%2F%2F%2F%2F%2FwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAGIALAAAAAAgACAAAAf%2FgGKCg4SEYYdhhYqLigVZSlInkidNKFQFjJliX1w%2FU1ugoaFEXF%2BahFsSCUsarRuvsK0aTgkSW6dbDRMrOgUFQBfBwkC%2BOkwTDbeMWyAEHkZDODUtLSjW1DU1OEMVXAQgyoW5BCVYXz1CLCwV7O3qQj1fViUEyYoSEx5gVzw3OQwAAwrMcYNHFTAeJkgo9CVBhBhgkowYMaCixYsVJ2pZECNCAlODuOwAMeMLopMoU36Z4YAAl0EFHpgAc8BkypsnvxwAk8IHJjEWZDg40MUmTpxfuhxwIMOCIC4YYCygoMKL1atYs15V0WXBBwwvxTzIwKVLAA5PAKhdy7YtgCIc%2FwJ04ZLhgSADCrhQQEDii9%2B%2FgAP%2FJQGBAhgFBu4qAAMBgdGjKhFAABMhsRi8YPY%2BhpxTMmXLePVC6Cu4NGASCAwjFjQ2iNm4AWIHgLLiiA0bP5CEkB077ty6TzF8WNClqtbjWLl6BSvIAo0GRDdzPpT0hQMaTsXEnFlzusoDXFIk%2BClGJEnp07%2BAaRlWUMOHNF10mU%2B%2Ffn0XO2Eo%2BFgInz4XCIjQgQAEFmhgByEgsAAXJiikyDglgLEAAgF00MEKBq7QgQgBQLAgPfY82IwHQRCFAAchhCDCiiFwgEAXL8BgAgFRhPOgLhE4QBNxFEBQWFcvcOGAAsjYuEgqCRCQgjgDH3ABxpNgcPGBAykQUIuRmXDigwwYZKBABBEokAEGNCRQyimLFGABFw8Y4KYBD3BhAXlo1mlnIAA7"
		/>
	</xsl:template>

	<xsl:template name="hit">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAOaQkP8BAf86OuYAAP84OP%2FFxP9CQvQAAP9jY%2F8yMv8FBf8kJP8VFeIAAP8DA%2BoAAP8ZGf8REdZAQNQAAPgAAP%2Fp6f8XF%2F8lJf8jI%2F82Nv8fH%2Ffg4P%2Fe3v8bG9QwMNwAANxwcP%2Ff3%2Frw8PPQ0P8NDfvw8OwAAP%2B6us0QEP8rK%2F8ICOgAAP8pKfwAAPIAAO4AAP%2Bwr%2F%2FU1P%2F29vAAAP8LC%2F%2FEw9dAQP%2FNzP8xMf%2Bfn94AAP%2FW1dkwMP%2FNzeAAAPoAAP%2Bpqf%2FLy%2F83N%2F%2Bzs%2F82Nf9TUf%2B9vf%2FIyP%2Fc3P9%2Bfv%2FV1f9OTv81Nf%2FS0v84N9gAAP9QUP9SUv%2FR0PuHhv8PD%2F%2BYl%2F9bWv9LSv%2FJyf%2BPj%2F%2Bmpf8eHtYAAP%2Fd3fE%2FP%2F%2F39%2F9UVPZzc%2F91deyurv8%2FP%2FjY2Pvn5%2F9qaf3a2v9gX%2Bqfn%2F8TE%2F%2FW1v8sLP%2BAf%2FG5uf%2BWluZWVe5QUPMXF9cwMP8yMe2Bgf%2BsrP%2FMzP9hYP9ISP8tLf%2Fv78wAAP%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLihtqb2YykjJfZWMbjJl%2FIxJxaBWgoaF2NiOahCIgE3JIXRyvsK9dSFMTICKnJR4fXlJNMTFKO2w7O0rATVJhHx4lmSU8DS53R1hBNzc92th4QVhHBXMNPM6KIh4NB3pGQzg5NTUF8gXwOThDRmIHDR64hSAfXChwAyRKCAFVTihcWEVAiChAtChw8QFEoRETTASIAAcKHz98BGSBQRJGFgEf%2BUDJ0SaAiQmmBkkY8IMEhCUfD4WcokXLFJSI%2BCypQ4JCAwmDNjyZoYCBihCIdApIkgRo1BAqGCh4wQXTHwA%2BKJDooCFBzqACrAYloKFDUR8A%2FwTN3IiBxRUnZ3XmBekkDwsNEVoMsCEIxQoHDDCkMIBAyN6oIIUgAJMCAwMHK1AI6vPAgYULe%2FaksZLhsc4MVs7g2IPBgoIHfTY%2FUAAB9B4CCIoQecyHSJEzBkJfgKDAROw%2FnBV8Dr0ngx4mvJlcIcN8ePHjnD1jYL7ALOSgCRZUdw278IoAiVPs6W4avPjKlzPLHdAiggYW7CHz2csn%2FN%2FAAyD1lQ4HjFUWfwl4F1V%2FW3RAAwU6xPWHUkw5BdWC4eV3VVYOvDCBV3%2FYQJNNpbkXmoYgZWDBg0cRgpFGEXRQYn%2FicecdHxlAQEUAD8D0T0AKrCFjCOExZ2MIOUbgwI0MFZmDzgEKRACBCvepx1wKLGyhggVUOLBPP4tAI00AJDBAFgYYXKAmBm0xQEMAMzRARzlh7mICBQqQEAEDFkAAgQUMRECDAxQ8wAydjKQyQQMvUNCCAwpEqoADLVDwQgO2%2BHPKJhJw4cMAKzxgggkPrDCADhNIENOmhGwAgA0o9CFrHyhIAACIrOa6aSAAOw%3D%3D"
		/>
	</xsl:template>

	<xsl:template name="Color_Information_Icon">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAMzOzIDjb%2Fj2auj86Ixp1fn2TK1xcff5L9llOdScluzLkIqG8Hd38Pf49qTfO8kiIeitOMpMRpaWlrnNxvXNcpqLnOH72LmeiNXw4MHZ3M3NEa0xSnymlqisy7nK5kyqh%2BX5vGjFaKWnprjDsZ6dyfnRT9v03bC26OH6ybmrkKRxueyzd8X3VJ2I1cfKLGyPyvPXMGzPPi8x809O9DhmrLrtuvWzTdv1x%2BTk5Pb2fjROzC8rzrqLftHyL7e2oe%2BORlE00WQ6sI1Pt8rk3fLrk%2BTjGKFZm%2Ffjeufmss7Xs56Euj%2BqWgYGyuriqIs%2FmZpxhdDq29SARGtZ6T%2BJlJyeps%2FPTW5G0EFvye7Y2BkZ5Z9Dd53NnaF6nGBo8c3Nm%2FTi4k5Oz6u2rBoi1h4az7kiKundl9%2Ff8%2Fb25M%2F5ns1EO5ub5%2B3ys8PGZfH2leb5X2hpwZPXqpXml6TlqZ7AesatQcK%2FapXKxPDw26i5srO%2Bwez5svP4p%2Bz4iLCwsP%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLigNyNQMNkg1%2BJpCMmH8mIQEWA5%2BgoHEhJpmEDVsxAaGsoHJLWw2mDXOqFrcWJhMjIwAmv79wSxyyjLQxMTe4JiJUfSISI1AY1BgfU8SLtA4OLCgot2EilH4AEhNDUOp2U1MvxYRe3A5o3yg3EgCIfiJhGUMAM0yhQYNEoTsuHPRgAcIePhz7RIiY8C9DhhdXaOgwQ4iNi4UMG96TSM4cHooWPWDUoePNoDMfQYKYeeNGEgkiAOSRQCUPSg8qr%2BjYsYPjnzIaehxw40bPzBESokqNOhEo0BdddMgQo0ZQlSIHCjBt43SECBxo00rMY%2FUEgy4z%2F7aCEaQBrFgBfPSs8XEWIqI%2BVDqc8HBCzdu4WcbQtSsgR449e6P22QdY8IkTCw7LyMJkcdjGOdqsQZLEx%2BS%2FgS%2BryQx3c%2Bc%2FdT87bkMECRLTlFOvZo34dewCoIkQadIEN2oSanZr5uwZuIAjwsuUMX6oTwUlLVosyCwFseI%2FX8MWOEJeuoIUp6tfz66dQXcZY%2BYe1QADRgnyFBToR0%2F5OvYWBBAgBRA7jNHVHzDVV0IJFDS4X3p%2BWMeFEkqoEKAVBBY1SB0u2MdggyuscAGEEqpgIgFCYAiES4PcQQcENsQY4goJjNgfF1yYKIQQQfRo1CBevAijDSJKRllUXBhhhHEQTvRoUCENpBDFD1T%2BwAOElD2hhRZOdKkEPKdcEMWUVuYEwJloAiCClltqUQGYUPKAwJwIGGDnnXhqscEGWvAApyINJBBBGoQWauieiCbwJyNYGPDAo5BGSgYZBmBhyiJfCBrpoxEk8EUil4Yq6h%2BBAAA7"
		/>
	</xsl:template>


	<xsl:template name="Document_Metadata">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FALbY5gBmjgBpkwBwnfH3%2BlOatbra59To8BGBrIK803y4z5bH2kCGodDg5l2oxqHN3tHn78zk7cji7KnR4dnr8ebx9pC4yL7c6I3C1wBiivb6%2FDqWutzs8wB1pABrla%2FU4wBzoS%2BOtUacvjWUucPf6k2gwTB%2FnnK0zmWtyXGzzT6YvJHF2Ia%2F1fD195vK3KXP4GGryAV6qLDV4wBfhBuGsABtmCWLsymNten0%2BBWDrgBumeDr7zB8mgN5qA1%2Bq3CitiuOtsrj7MXg6yOKs97u9Nfq8RBoiwBsl8Tg6iGJsvH4%2BhmFryeMtABvmgF4p9bp8R%2BIsQt9qs3l7QBnkFKjwt7t8w9%2FrAB0opnJ28Dd6cvk7aXP3xeEr%2Bz1%2BOPw9a3U4h2HsTiVur3c6N7t9Bd9pvX6%2B7nU3qfR4H260SWLtGmvyj%2BTs9vs8m6yzKzT4p%2FCz8bh63W2z0edv2OsyHOwyFelxNjn7ZzK3Hi30Ie6zsHe6a7L10CZvC2Ptv%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLijtvZhUakpJ2ezuMmH8NDAUUOJ%2BgoAoMDZmELT8ZBRxjY0SvsGNVHHkZPy2mLTwBaxAQBwdPwsPAvnQBPLiMLSYCA24SQVpS1NVSEVpBEkJkAibKhbrOcmJZJCRISELrQukkellicQMCyYo%2FAQMxCQAABv8AA%2F7rdybGgAA%2FCjXIoMMJggdfZEicSJHilwl9nOjIUGoQAwFXfNB4sOWFyZMoUW55sMEHCAEMBu3IFyPHECwucurcydMFFhU5YjTJcOmPhSkgRDJhsaKp06dQm46g4XKKBUEMPHRAkOQGChZgw4ody0LBDSgIOniI%2BcfIkQR%2B%2F7yEOeOnwp1DVQ7ByevngoK8FA4hQXDEiKAZNXoI8RPETxkMCw51gSCZsp8TbPwoWUDAT4IaMw7XiMGks%2Bc5GA5hSO1nAR7VFJQEgVGCQBYdof8gjpHGy6E2I%2FAUKeJAwXAHDj58SAF5AW3buEX3AOCniGY5cw5RQaOdyiE1jZUo6PwAtCC3KfwQCCOjOoxDKk4cEsHnEIzwhwgkKYzVQ4gSG9ywAQwwyEFgHwPC0EcfBG7QGAwO1AGFWmwdlRQNTCyo4YYc9vGgDTREAYJVgsykTw5ddaiihjckkUMPQxX1x0ch0WADECt2CMQQS4gIEyELNYQAGDfmqCEQIFrRQZ4NHBWCjz5D2nCDkTcMQQMCPRyUUDg8OBMDAjQkwcQNOB55gw1JLGFFlvWAc0ozAzjhQw40QDHEEDbkOQQUNOQQRQf0fIOJLgHoAEIMPiCQAxdLLMFFDghE0QMINSDj5iKoZCBAEyB00EMMoMbQQwcgNCGALZdiskkGU3hwRA066FDDER5MkQEppiyygwUMGDHDrzMYwYAFMuZq7LGBAAA7"
		/>
	</xsl:template>

	<xsl:template name="Document_Information_Icon">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAPLFyLYADfbW2O2tscsRH7QwOsoPHs4fLbAADc0bKc4dK64ADb4ADssSIckFFOylq%2B6wtMgDEp8ADPC6vqUADLVASMEADvrp6q8wOfLg4evQ0vjw8fnw8cRwdvje4NAlMqsADLgADfTN0MoNHMwVI7MADdErOKMQGvTP0s0ZJ99veNEvPM8jMM0XJcgBELwADsQADvLEx7kADvbU17ZASLsADv3299EpNswWJMoLGsYADuqfpdOQleaPluN%2Fh%2Fvv8NKQle2vtPXR0%2B%2B1uvTLzvjd39xfaccCEcMADqcADPC3u%2FXS1fG9wfPIy%2FPJzeulqc8hL%2FC5vfLDx%2FbV2Pjc3qgADNhOWNhPWuuip9ZETthNV%2FXa3NU9SeF1fdyHjvfn6coMG8IXJNAnNMlTXN6usssVI8wTIdmfo%2FTMz9EsOPjf4eynrNdIU%2BOAh%2BeRl%2F33%2BOW5vMoOHNaBh%2B2qr8o%2FSc8nNNpzevHY2vLBxfLCxdU%2FS%2FG%2Fw9EtOscAD%2F%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLihlncF82kjZvd2QZjJl%2FGhVjWxegoaFyNBqahBsdFGNURR6vsK9FVF4UHRunHBggdEJLMzNTAsMCU8BLQnYgGByZHAUILwNNTkQi19giaEROTQBhCAXNihsYCAxsTHt5UjExAPAA7lJ4e0xdDAgYuIUdIC8O2gxREmWCwYMIoygZssbBCxAdCmmgEMIFgR0DgkDYyLHjxiAD5qRxEYKCqUEVECAZkQDLkwcwY8qU%2BQQLlxEWEFQYlKFKDQd9ggZVYCWL0D5E3Wg5GjSCjCSY%2FvBYYGFEnx57sqow0GdF1j1bE%2Bjx%2BrVPDgsLeAiqUEIHgT57%2FxD5QWFAj1y6fewi6gMGRgkagk4EiEAC7lwVh4zo3fHDz468h%2FTo6WMgQoATgiQMbmF4T5%2FGPvTqQeynbuTJBBwEkJA5gIMUhlFcSSy6ttw%2BDRyEYP1HswPOcRHRte1H8u3cu1tHAO5HjY%2BtkIvP9nNAr9DUqwMHcFE4ruccKcro9YHCj2frKVKYsYz5D1sYb7334cMHh95DP1ZE73PgAAG%2FO%2F0BxAIMWIVVD%2FPVR9YeOxxwRBxk9ZFAAmctAIQgPf3EFH04HHWEAXV0KFQKJDhFQVR%2F0KASSx%2BYQN%2BLMMZoAgspnKUTIRNVRIACLcboIx8mfJCAAToEYFI%2F%2FzhgBpePN%2Fz44g0sJEBABDVARI45DDhAQAJQiHGDiy%2BacMMHUKRQWT77LPJMNC6MQEICB7DAwgd0snBAAiTkoEMN4Yyj5i4hWODACASQ0EJ6LZBAQA4RWBDAMn4ykgoFCMhgAQwROKCpAxHAYIEMCNjCzymbVJDEAiUEEEAIIahawgIUVHASqYRkwAMNJ0igqwQnVAAEirQGS2ogADs%3D"
		/>
	</xsl:template>

	<xsl:template name="PDF_Standard_Icon">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAFWudfj8%2BUqpbHW9jqvXu9fs38zn1Rl%2BPeHx5hh6OyWXTabVtjKdWMrl08fk0USmZ1GscTuhXy%2BcVkORXiKWS%2FH2847Joz6jYrDZvjCcV7nexhd1OBmCPj2iYSCVSjWfW8LizVCWaRqGQS2bVOz27xyORSmZUXyujuPt5kCkY%2BLx5xqIQSeYT9Tk2kKlZUKOXdPq2yuZUjSeWjuiXxqEQLjdxSV9RBqFQHG7jPD48zGdVzmhXi6bVR6USNvu4RyRRhuLQ9Tq3BuJQhuMQxuKQlCXasvm1DmgXkenaeLx6BySRpzPrn%2FCl2O0gBmBPprBqJrCqCuaUiyaU73gysrm1ByQRcDhyzegXNDo2dHp2sHizdnt4Nrt4M7o15bNqbbcwxh8PK3YvK%2FZvcXjzxmDPxuNRECjY9vu4lOmcbXSv6fKtL%2FayGu4hrLav1Stc43An9zr4WKtfIK%2Fl9Lp2unz7JPGpjGcVz%2BjY97u5IXFnDCWVLTbwUWnaB2TR%2F%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLiihqa3QBkpJwaSiMmH8tIXF4JJ%2BgoG9FLZmEFScJcQgIKq6vrqx1CScVphUvB2gFBVtcPj5nwD5cW7xyBy%2B2jBUTHEBfXVhZMNXWMHNZWF0GehwTy4W4HENNYw4NVEZGBu0G61QNDmN5QxzKiicHQBQWU1ZaQAgcSFCLlSltKAA5cKJQiwQreowg8KWGhosYM16s8WXPgx4rEpQaFIJMFRMfwojBwLKlS5diwkAwUYJDiEEowBChkMGOmRRALbABKmDAAi8QgELwssBNBgpCElz6A8VJCZQ8jliw0EdBDCYWmvQBoECrhSMKBgj4QNMJFEEh%2F2goGRFBAoBDffoEQZQXByI%2FOBTc2THiB40igmzc8JChQ4a7fvr49bPEQZ8YOfzUqOEnRwwGjj3csCFogwgPDC7ogNzHgp8kefswORQjxiEcJjowoCBiQ2kRFGSoZg05SIzWnfMessDiggwKK3z%2FMU0h9Wq8yP0EQb69z%2FLmz6P%2FRt3heuS%2BhwC4Pv%2F9wu7eiW%2F0aPwYbxMkkAEgOdRkth8kPIQ2Glw0%2FDDCDnbhtd5reXGGCAF9RECYYTf98cQBZWDFGg5JaIaWFDEskUMOS%2FTBwwUfsFDCAU8IktNOGUgR24x9sOACHzrMyIMLEWTgQVRT%2FVGESSYw8EEEM8zgQr%2BSKfDhpJNJuvBABzKoaBMhD0U0whUXPPDkl2A%2BgGIUSoggUiH68CMBlzeC%2BaULHXwwggdEMKTIOENQMMKRKUj55QMuXBCBDFF4YA8%2BdzoDRA8mZPDBDh10cMGkHezwQQYsKEHEN%2BHcmcsKJVBgwggZMCCDDAxkMAILHpQgQjKdLoJKAhwIUcIPHlCgKwUe%2FFCCEBzQEismmyTgBA03iLDCCiLcQMMBCYQwkimFoABFETZsoO0GNoTwRJDUhituIAA7"
		/>
	</xsl:template>

	<xsl:template name="Warning">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FANCSMfzNhPy1Rf7t0eKeNf%2F57vuzQ%2F7it%2F7juvy9XPCoOP7nwv%2F05P3Wmvu1Sf3Ngfy2SfzFbvzBZv7lvf%2F8%2Bf%2Fx3f%2Fv1%2F%2Fu1PzDav7pyfu6Vf7gsP3Vlf3SjvzIdvzHc%2FzBY%2FyyP%2F3XnfzDbN6bNPy3TPy8WMiML%2Fy5UPyxPuahNv7dqfy7VtOnYvy9W%2FSrOf7gsv7aofbw5eiiNvLo2Nm6if%2F15v7rzvr38tGhVfv489iXMv3Tkfy4T%2Fy4Tfy7VeSgNf3QicqSO%2BTLpO6nN%2FitOvyxPPy6U%2BylN%2Fu4UOqkN9SoYv7qytakV%2Fu7V%2Fy9WdqZM9yaM%2FqvOv%2Fy3vasOf7oxv3PiP768vy5UtSVMfy1R%2Fu5U%2Fu9W%2FzKfP7rzP3ZoP7sz%2Fu3TPv27O%2B6aPu6U%2F%2Fv2NSjVvDgxvCvSfjv4Pu0RfzEa%2BvZvPTNkf3ku%2F3Ynfzy4vy%2FYf3bpuzMmujSsPu4Tvu%2BX%2Bi8dv3mwf7lwP7mwPPToP3Jefy%2BXv%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLijJ0Z2IUkpJpbDKMmH80LXdwBZ%2BgoHNLNJmEODUAdww2ra6vDHsANTimOjk7YxUVUxcHv8BTU7ttOzk6mDpNJER5FxYWGyMi1CJbZdAXF2gkTciKODkkCh9eN2ADKyKIfiMDA2A3XhwKJDm1hTU7RCFvVRkZmMhZh2gEEyYAq7gJQWRHjUI0AMwwAuGAHjwLFnwheGhExgV49EzoY2QGgFKDWhCgIgDFAQQwETTg2C4mzAMYBLwg0WKQjCxIQpRgsQGGURgcaI44anQDhhIhlAC49GdIlBctn3xZwXVFB6Vdu4JAoTPKEEEqpUD44cJKjLcx%2F6wohfu2gYsjEIoQWCJICJAUQ%2FtEaEC4wQOlhQl36cOiRAogQgSdUJHCh4k%2BfQLw2NxF6ebNHeIw9hFCxQnJKkL0uNxHgpUgQTwohQ07AmYTPULMOP1ncgjLmFs%2FeBBB6fAHa4Lj1s17cmUWwfuA%2BIAhwI3rN%2Bzw8SAhugnSpvsCMRI4ep8Edcj8%2BKEhSXfzjR9H%2FqOyCIQjLsxjdrJFAxf9fdyVFwE9VQWFAlnp58A0IjjghH4moKDGC1Cc9cdPQZXAVnRJBIDIFQ78F5wLPzgmFVV%2FLLFSS6xhZsAV7KyQRHQs9DAhT4RENBEEWLRoADt%2B4BGGcihoIYUKJ%2BWzT6MIPJqQnwM3sBPAgy6wgAIEKSDhEDjiKMAkCj88oQGUh4ighgsm%2FNCDFinUc88iyjBjhAAloHBEEgZowCQLR6BQghpSIEGCGd%2FAicsML4QgAAQl%2BNBDDz6UAIEaKbyggjGFMoIKACQo8UIRKYQgaggpFPGCEiTMgo8pmrQAQBQEAKHCDDOoAAQBUADQAkqsEiLDEEsIccKwJwjRwhAo9qosq4EAADs%3D"
		/>
	</xsl:template>

	<xsl:template name="Info">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAEm4573l9trw%2Bk6656Pb8kW25tTu%2BZrX8ZTV8J3J3Mrq%2BIDN7uP0%2B9Lt%2BXnK7bHh9GfD6kuhxc%2Fs%2Bczr%2BH7M7rnj9aHa8nHH7GrF61%2FA6i6t4yCItCSXyDuy5Sys46ve9CKOuyWczzix5FikxUe35kC05rTi9Sem2%2B34%2FZHU8Ey55%2BT0%2ByOTwvL3%2BeTv9CWe0TSw5Nbn70qewfL3%2BoG4zz605cnq9z2z5SWbzS6PuIjQ7ySayyai1lG65yqs4%2Fj8%2FnfJ7Cio30O15Sej2Cag1FilxjOv5N3y%2Biaf0tzx%2BjKu4ySWxle96Lrk9iOUxMbp91W86Ciq4V2%2F6SKQvyin3a7f9G7G6%2FH5%2Fare80K15iKPvcjp%2BNnw%2BsPn98To9zaw5CSYyli96cHn91u%2B6U2551u33avR4ZLK4YXP7%2Br1%2Bd3u9Tmy5eDy%2Bdbv%2BU6550C05cLf6zqr2ojL52LB6pnQ6LjY5XTI7IzS71K76Lbi9Wm63G221l7A6U%2B66P%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLii5mcGk%2FkpJqdS6MmH8xI3tsKJ%2BgoGdFMZmELTQgegwMK66vrqx0IDQtpjMyLGVcAkm%2BR8BHvkkCXHIsMjOYMxEcPE0NDQZtFip8PRgKBgbREnEcEcqKLTIcQxBbChMTFW5XhxYXEusKW3dDHDK2hTQsPBoIxHTx8mRBFUR%2ByDx54qWLmDwaeLCgUSgGiBc%2BRDxoEqCjnQAIhXTs2KRCHx8vQJQaNAIMFRglHpiYaeJAj3d%2BqkihOfPBGBgnOIwY5GIKEQ0dSGD5wJSpAyEIHLwh0JQpFikdNCDRculPgiUnYAI4QKAshLMZSPQ5W7YtASgl%2F4AuSSBoxI4oIgqooHCgbw8bgAP36Es4hYoCIoLsKCIoBw4PSQdkSEH5bxuEfChrvjCARAcPOHII2hDCww0Affo40KGDCRM%2BmFmzRoOnD4kbGkJsGB1CQw3UfcJQWEAcNiI%2BxInPSQ2ghoYXu%2F%2BQ1nA6dR8mdoAAMX6Ij%2Fbt1ps%2Fj07atFrrUCBY4e6HjxUMYaz3AYBbd2McPiLL7wOFPZ%2F48nX2WWh17RBEXirs14d%2FCh6W2A5DeeXEEGIpyOB%2BAJRgxAlO0PVHUUd1oNd%2BF1p32GdIgNDVH0W4JNYA8pXYR2c1bCgUIRZhJEIWAMCYWokDZPhFFCGoxI8%2FGqzBo5IKSijhX5MqkFCCCB4QMdE45QyhgQglFMAeZgXU8IUH%2BOizCDPO%2BABDB1Ah4OabCAjRgRFREAGOOGfm8sIJGsAgQgc31FDDDR2IYIQHJ4SADJ6MoAICB0icEIQHGlSqgQdBnIAEB7TsY4omI2ixxA44hPDCCyHgsIMTIIyw0qeEuJBAETlsYOsGOYyQwIqw9vppIAA7"
		/>
	</xsl:template>

	<xsl:template name="PDF">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZ%2FAPLFyLYADfbW2O2tscsRH7QwOsoPHs4fLbAADc0bKc4dK64ADb4ADssSIckFFOylq%2B6wtMgDEp8ADPC6vqUADLVASMEADvrp6q8wOfLg4evQ0vjw8fnw8cRwdvje4NAlMqsADLgADfTN0MoNHMwVI7MADdErOKMQGvTP0s0ZJ99veNEvPM8jMM0XJcgBELwADsQADvLEx7kADvbU17ZASLsADv3299EpNswWJMoLGsYADuqfpdOQleaPluN%2Fh%2Fvv8NKQle2vtPXR0%2B%2B1uvTLzvjd39xfaccCEcMADqcADPC3u%2FXS1fG9wfPIy%2FPJzeulqc8hL%2FC5vfLDx%2FbV2Pjc3qgADNhOWNhPWuuip9ZETthNV%2FXa3NU9SeF1fdyHjvfn6coMG8IXJNAnNMlTXN6usssVI8wTIdmfo%2FTMz9EsOPjf4eynrNdIU%2BOAh%2BeRl%2F33%2BOW5vMoOHNaBh%2B2qr8o%2FSc8nNNpzevHY2vLBxfLCxdU%2FS%2FG%2Fw9EtOscAD%2F%2F%2F%2F%2F%2F%2F%2FyH5BAEAAH8ALAAAAAAgACAAAAf%2FgH%2BCg4SEfod%2BhYqLihlncF82kjZvd2QZjJl%2FGhVjWxegoaFyNBqahBsdFGNURR6vsK9FVF4UHRunHBggdEJLMzNTAsMCU8BLQnYgGByZHAUILwNNTkQi19giaEROTQBhCAXNihsYCAxsTHt5UjExAPAA7lJ4e0xdDAgYuIUdIC8O2gxREmWCwYMIoygZssbBCxAdCmmgEMIFgR0DgkDYyLHjxiAD5qRxEYKCqUEVECAZkQDLkwcwY8qU%2BQQLlxEWEFQYlKFKDQd9ggZVYCWL0D5E3Wg5GjSCjCSY%2FvBYYGFEnx57sqow0GdF1j1bE%2Bjx%2BrVPDgsLeAiqUEIHgT57%2FxD5QWFAj1y6fewi6gMGRgkagk4EiEAC7lwVh4zo3fHDz468h%2FTo6WMgQoATgiQMbmF4T5%2FGPvTqQeynbuTJBBwEkJA5gIMUhlFcSSy6ttw%2BDRyEYP1HswPOcRHRte1H8u3cu1tHAO5HjY%2BtkIvP9nNAr9DUqwMHcFE4ruccKcro9YHCj2frKVKYsYz5D1sYb7334cMHh95DP1ZE73PgAAG%2FO%2F0BxAIMWIVVD%2FPVR9YeOxxwRBxk9ZFAAmctAIQgPf3EFH04HHWEAXV0KFQKJDhFQVR%2F0KASSx%2BYQN%2BLMMZoAgspnKUTIRNVRIACLcboIx8mfJCAAToEYFI%2F%2FzhgBpePN%2Fz44g0sJEBABDVARI45DDhAQAJQiHGDiy%2BacMMHUKRQWT77LPJMNC6MQEICB7DAwgd0snBAAiTkoEMN4Yyj5i4hWODACASQ0EJ6LZBAQA4RWBDAMn4ykgoFCMhgAQwROKCpAxHAYIEMCNjCzymbVJDEAiUEEEAIIahawgIUVHASqYRkwAMNJ0igqwQnVAAEirQGS2ogADs%3D"
		/>
	</xsl:template>


	<xsl:template name="Page_Information_Icon">
		<img
			src="data:image/gif;base64,R0lGODlhIAAgAOZtAL6%2Bvp%2Bfn6WlpYSEhHh4eJGRkZCQkO3t7aampomJia%2Bvr6Kionx8fOTk5I%2BPj7Kysqenp39%2Ff5ycnH19fYqKioGBgZKSkqCgoKOjo46OjqGhoZWVlY2NjYiIiO7u7oeHh8LCwpSUlKioqPLy8fHx8YWFhZeXl5ubm4yMjObm5vz8%2FOvr6uvr66SkpN7e3oODg4KCgurq6q2trZ6enqurq%2B7u7dXV1YaGhtzc3Ojo6NnZ2d3d3enp6ezs7Kqqqufn5rW1tcjIyNTU1OLi4uDg4Nvb2%2Fv7%2B8vLypqamq2trOfn58DAv9DQ0MHBwcfHxuHh4bGxscnJyPPz89PT09jY2O%2Fv78DAwKysrLS0tI6OjeXl5cbGxtLS0cvLy7Cwr%2BLi4bm5udra2s3NzNnZ2Nra2b29vampqZOTk%2Fb29piYmJ2dnZmZmf%2F%2F%2F%2F%2F%2F%2FwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAG0ALAAAAAAgACAAAAf%2FgG2Cg4SEbIdshYqLigddYVIqkipGLFMHjJltDWk0VWigoaFNaQ2ahGgKDDIkrSOvsK0kQQwKaKdoBhUiPQcHNR7BwjW%2BPVYVBreMaGcDGTs5PDErKyzW1DExPDlKaQNnyoW5Aw5YZTMP6erqRD8pWg1ODgPJigoVGWtHCiVq%2Fv9qSkwwgGPIlydF1mSooKBQAwYUTKihIkMNootqgGxwMMbFDhwiTFBgYGpQmhtnJCzQQcPixUNqAKQokUEIGR0KJBQYkGbQgQgc1gQQYMOMy5cx2czMwsSGlwBrUEzA1AbEiwIqIYiBcBSjDBcutkxIwuXKAp0vQAhK82GDmhYQ%2F5YI6Ipo5oS7EwREgYBBTYgPPdtE6JBmqAgofV8qZqMGAxgRAgKk6RBBEIEEaS4gMGMGCd3FagL4MCPgwpoEBCwnWKNhs5kznxX75YxAwxoKqdtcXqOZM%2BzFSM%2FQto1bdWYBvmMHp20ataDBJgy%2FVo5ROGTJlNd%2BCNEYwnTg1c3w9QtYEAgYBrJ%2BBw9TOIIFJwrAUNvmZ9AAcH%2BzZ3wGQgvJKDBAVRsnpbQAAhZQh4gaFgigQXw8EfJQRGosUICCMBWwgBobJEBSIffkM4MDHABg4okomsiBA2qkwQFDiozjwBobGWDBjWfkeMaNBRjgwAZpzFNPjM1kYIIEASyAgWgAAiDgpAAYLBDACRtwMIAF4cSoCwUFrCFBaBdooMEFAahxQhoFJIBMloukwsAAKBQQQhpr1LlGGiEUgMIAtbCZCScTvPBBBwlQQEECHXwAAwOlnLLIASCkEQEBlBIQQRogDOjoppwGAgA7"
		/>
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
		</xsl:call-template>
		<xsl:value-of select="$get_dimension"/>
		<xsl:text>/</xsl:text>
		<xsl:call-template name="calc_dimension">
			<xsl:with-param name="calc_dimension" select="$get_dimension"/>
			<xsl:with-param name="calc_value" select="number($pos_4 - $pos_2)"/>
		</xsl:call-template>
		<xsl:value-of select="$get_dimension"/>
	</xsl:template>


	<!--  Convert pt to in or mm -->
	<xsl:template name="calc_dimension">
		<xsl:param name="calc_dimension" select="'mm'"/>
		<xsl:param name="calc_value"/>

		<xsl:choose>
			<xsl:when test="$calc_dimension = 'mm'">
				<xsl:value-of select="format-number(round(number($calc_value * 3.527778)) div 10,'0.0')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="format-number(round(number($calc_value div 72 * 1000)) div 1000,'0.000')"/>

			</xsl:otherwise>
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
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>文件: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>印前配置文件: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>文档信息: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>文件名: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>标题: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>PDF 版本: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>创建程序: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>制作程序: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>文件大小: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>创建日期: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>修改日期: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>颜色信息: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>分色: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>分色名称: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>检查后的 PDF 包含: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> 错误</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> 警告 </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> 警告 </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> 信息 </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> 信息 </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>未发现问题</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>详细信息</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>页面上无提示</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>产品: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>日期: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>用户名: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>计算机名: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>操作系统: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>持续时间: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> 提示</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> 页</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>字体信息</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>字体: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>页面信息(第 1 页): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>计算页数: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>页面大小: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>PDF 标准: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>PDF 标准: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>输出方法: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>陷印: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>##Page scaling factor (UserUnits):</xsl:text>
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
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>Datei: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>Prüfprofil: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>Dokumentinformation: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Dateiname: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titel: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>PDF-Version: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Erstellt mit: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Produziert mit: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Dateigröße: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Erstellt am: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Geändert am: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>Farbinformation: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>Farbauszüge: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>Auszugsname: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>Die geprüfte PDF enthält: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> Fehler</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> Warnungen </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> Warnung </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> Infos </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> Info </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>Keine Fehler gefunden</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>Detaillierte Informationen</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>Keine Fehler auf der Seite</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>Produkt: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>Datum: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>Benutzer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>Computer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>Betriebssystem: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>Verarbeitungsdauer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> Treffer</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> auf Seite</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>Schrift-Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>Schriften: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>Seiteninformation (Seite 1): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>Seiten: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>Seitengröße: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>PDF-Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>PDF-Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>Output Intent: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Überfüllung: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_FIXUPS_FAIL'">
						<xsl:text>Die folgenden Korrekturen schlugen fehl: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_FIXUPS_SUCCESS'">
						<xsl:text>Die folgenden Korrekturen wurden durchgeführt: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>Seitenskalierungsfaktor (UserUnits):</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- Spanisch -->
			<xsl:when test="$doc_lang = 'es' or $doc_lang = 'ESP'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Resultados</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
						<xsl:text>Resumen</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>Archivo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>Perfil de comprobación previa: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>Información del documento: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nombre del archivo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Título: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>Número de versión de PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creador: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Productor: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Tamaño de archivo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Creado el: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modificado: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>Información de color: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>Separación: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>Nombre de separación: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>El PDF comprobado contiene: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> Advertencias </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> Advertencia </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> tipos de información </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> tipo de información </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>Ningún problema detectado</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>Información detallada</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>Ningún impacto en la página</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>Producto: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>Fecha: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>Nombre de usuario: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>Nombre del equipo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>Sistema operativo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>Duración: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> impactos</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> en la página</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>Información de fuente</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>Fuentes: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>Información de página (Página 1): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>Recuento de páginas: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>Tamaño de página: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>Estándar PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>Estándar PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>Calidad de salida: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Reventado: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>##Page scaling factor (UserUnits):</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<!-- French-->
			<xsl:when test="$doc_lang = 'fr' or $doc_lang = 'FRE' or $doc_lang = 'FRA'">
				<xsl:choose>
					<xsl:when test="$doc_string = 'DICT_RESULTS'">
						<xsl:text>Résultats</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SUMMARY'">
							<xsl:text>Résumé</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>Fichier : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>Profil de contrôle en amont : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>Informations sur le document : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nom du fichier : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titre : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>Numéro de version PDF : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Application d'origine : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Mode de conversion : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Taille de fichier : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Date de création : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Date de modification : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>Informations sur les couleurs : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>Séparation : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>Nom de séparation des couleurs : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>Le PDF vérifié contient : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> Erreur</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> Avertissements </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> Avertissement </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> Infos </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> Infos </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>Aucun problème trouvé</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>Informations détaillées</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>Aucune occurrence sur la page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>Produit : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>Date : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>Nom d'utilisateur : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>Nom de l'ordinateur : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>Système d'exploitation : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>Durée : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> occurrences</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> sur la page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>Informations sur les polices</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>Polices : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>Informations sur la page (Page 1) : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>Nombre de pages : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>Format de page : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>Norme PDF : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>Norme PDF : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>Mode de sortie : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Recouvrement : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>##Page scaling factor (UserUnits):</xsl:text>
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
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>File: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>Profilo di preflight: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>Informazioni documento: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Nome file: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Titolo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>Numero versione PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creato in: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Prodotto con: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Dimensione file: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Creato il: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modificato il: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>Informazioni colore: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>Separazione: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>Nome separazione: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>Il PDF controllato contiene: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> Errore</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> Avvisi </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> Avviso </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> Info </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> Info </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>Nessun problema rilevato</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>Informazioni dettagliate</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>Nessuna segnalazione sulla pagina</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>Prodotto: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>Data : </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>Nome utente: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>Nome computer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>Sistema operativo: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>Durata: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> segnalazioni</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> sulla pagina</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>Informazioni font</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>Font: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>Informazioni pagina (Pagina 1): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>Numero pagine: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>Dimensioni pagina: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>Standard PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>Standard PDF: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>Intento di output: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Trapping: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>##Page scaling factor (UserUnits):</xsl:text>
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
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>##File: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>##Preflight Profile: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>##Document Information: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>##Filename: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>##Title: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>##PDF Version: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>##Creator: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>##Producer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>##Filesize: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>##Created: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>##Modified: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>##Color Information: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>##Separation: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>##Separation name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>##The checked PDF contains: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text>## Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text>## Warnings </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text>## Warning </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text>## Infos </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text>## Info </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>##No problem found</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>##Detailed Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>##No hit on page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>##Product: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>##Date: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>##User name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>##Computer name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>##Operating system: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>##Duration: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text>## hits</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text>## on page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>##Font Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>##Fonts: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>##Page Information (Page 1): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>##Count Pages: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>##Page size: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>##PDF Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>##PDF Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>##Output Intent: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>##Trapping: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>##Page scaling factor (UserUnits):</xsl:text>
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
					<xsl:when test="$doc_string = 'DICT_FILE'">
						<xsl:text>File: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PREFLIGHT_PROFILE'">
						<xsl:text>Preflight Profile: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DOCUMENT_INFO'">
						<xsl:text>Document Information: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILENAME'">
						<xsl:text>Filename: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TITLE'">
						<xsl:text>Title: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_VERSION'">
						<xsl:text>PDF Version: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATOR'">
						<xsl:text>Creator: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCER'">
						<xsl:text>Producer: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FILESIZE'">
						<xsl:text>Filesize: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CREATED'">
						<xsl:text>Created: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_MODIFIED'">
						<xsl:text>Modified: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COLOR_INFO'">
						<xsl:text>Color Information: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEPERATION'">
						<xsl:text>Separation: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PLATENAME'">
						<xsl:text>Separation name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_CHECKED_PDF_CONTAIN'">
						<xsl:text>The checked PDF contains: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ERROR'">
						<xsl:text> Error</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNINGS'">
						<xsl:text> Warnings </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_WARNING'">
						<xsl:text> Warning </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFOS'">
						<xsl:text> Infos </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_INFO'">
						<xsl:text> Info </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_PROBLEMS_FOUND'">
						<xsl:text>No problem found</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DETAILED_INFO'">
						<xsl:text>Detailed Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_NO_HITS_ON_PAGE'">
						<xsl:text>No hit on page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PRODUCT'">
						<xsl:text>Product: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DATE'">
						<xsl:text>Date: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USERNAME'">
						<xsl:text>User name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COMPUTERNAME'">
						<xsl:text>Computer name: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OPERATING'">
						<xsl:text>Operating system: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_DURATION'">
						<xsl:text>Duration: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_HITS'">
						<xsl:text> hits</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_ON_PAGE'">
						<xsl:text> on page</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS_INFO'">
						<xsl:text>Font Information</xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_FONTS'">
						<xsl:text>Fonts: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_INFO'">
						<xsl:text>Page Information (Page 1): </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_COUNT_PAGES'">
						<xsl:text>Count Pages: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PAGE_SIZE'">
						<xsl:text>Page size: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_SEITEN_STANDARD'">
						<xsl:text>PDF Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_STANDARD'">
						<xsl:text>PDF Standard: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_OUTPUT_INTENT'">
						<xsl:text>Output Intent: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_TRAPPING'">
						<xsl:text>Trapping: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_FIXUPS_FAIL'">
						<xsl:text>The following fixes failed: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_PDF_FIXUPS_SUCCESS'">
						<xsl:text>The following fixes were executed sucessfully: </xsl:text>
					</xsl:when>
					<xsl:when test="$doc_string = 'DICT_USER_UNIT'">
						<xsl:text>Page scaling factor (UserUnits):</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
