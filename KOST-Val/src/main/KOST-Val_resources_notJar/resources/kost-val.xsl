<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
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
					tr.captioninfo {background-color: #b2b2c5; font-weight:bold }
					tr.captioninfom {background-color: #e7e7ed }
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
				</p>
				<xsl:for-each select="KOSTValLog/Init">
					<div>
						<table width="100%">
							<tr class="captioninfo">
								<td>
									<xsl:value-of select="Warning" />
								</td>
							</tr>
							<tr class="captioninfom">
								<td>
									<xsl:value-of select="Message" />
								</td>
							</tr>
						</table>
					</div>
					<br />
				</xsl:for-each>
				<xsl:for-each select="KOSTValLog/IoExeption">
					<h1>Error:</h1>
					<div>
						<table width="100%">
							<tr class="caption">
								<td>
									<xsl:value-of select="Error" />
								</td>
							</tr>
						</table>
					</div>
					<br />
				</xsl:for-each>
				<xsl:for-each select="KOSTValLog/Sip/Validation">
					<xsl:if test="Invalid">
						<h1>SIP:</h1>
						<h2>Invalid:</h2>
						<div>
							<table width="100%">
								<tr class="caption">
									<td>
										<xsl:value-of select="ValType" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
							</table>
							<table width="100%">
								<xsl:for-each select="Error">
									<tr class="captionm">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:value-of select="Message" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
					</xsl:if>
					<br />
				</xsl:for-each>
				<xsl:for-each select="KOSTValLog/Sip/Validation">
					<xsl:if test="Valid">
						<h1>SIP:</h1>
						<h2>Valid:</h2>
						<div>
							<table width="100%">
								<tr class="captionio">
									<td>
										<xsl:value-of select="ValType" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
								<xsl:for-each select="Error">
									<tr class="captionio">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:value-of select="Message" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
					</xsl:if>
				</xsl:for-each>
				<h1>
					Format (
					<xsl:value-of select="KOSTValLog/Infos/FormatValOn" />
					):
				</h1>
				<xsl:for-each select="KOSTValLog/Format/Infos">
					<div>
						<table width="100%">
							<tr class="captioninfo">
								<td>
									<xsl:value-of select="Summary" />
								</td>
							</tr>
						</table>
						<table width="100%">
							<xsl:for-each select="Info">
								<tr class="captioninfom">
									<td>
										<xsl:value-of select="Message" />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</div>
					<br />
				</xsl:for-each>
				<h2>Invalid:</h2>
				<xsl:for-each select="KOSTValLog/Format/Validation">
					<xsl:if test="Invalid">
						<div>
							<table width="100%">
								<tr class="caption">
									<td>
										<xsl:value-of select="ValType" />
										<xsl:value-of select="FormatVL" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
							</table>
							<table width="100%">
								<xsl:for-each select="Error">
									<tr class="captionm">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:for-each select="Message">
												<xsl:value-of select="." />
												<br />
											</xsl:for-each>
											<xsl:if test="MessageFont">
												<table width="100%">
													<tr class="captionm">
														<td width="100%">
															<strong>Producer: <xsl:value-of select="MessageFont/docInfo/producer"/>
															</strong>	
															<xsl:for-each select="MessageFont/fonts/font" >
																<br />
																<txt>
																	<strong>Font: <xsl:value-of select="@name"/>
																	</strong>
																	<txt>  Full name: </txt>
																	<xsl:value-of select="@fullname"/>
																	<xsl:if test="@objectNo"> (object no <xsl:value-of select="@objectNo"/>)
																	</xsl:if>
																	<txt>  Type: </txt>
																	<xsl:value-of select="@type"/>
																	<xsl:if test="@fontfile">
																		<txt>  Font file: </txt>
																		<xsl:value-of select="@fontfile"/>
																	</xsl:if>
																</txt>
																<br />
																<txt>
																	<xsl:for-each select="character">
																		<xsl:choose>
																			<xsl:when test="@unicodeUndefined">
																				<img height="30">
																					<xsl:attribute name="src">
																						<xsl:value-of select="."/>
																					</xsl:attribute>
																				</img>
																			</xsl:when>
																			<xsl:when test="@unicode">
																			</xsl:when>
																			<xsl:otherwise>
																				<img height="30"  border="1px">
																					<xsl:attribute name="src">
																						<xsl:value-of select="."/>
																					</xsl:attribute>
																				</img>
																			</xsl:otherwise>
																		</xsl:choose>
																	</xsl:for-each>
																</txt>
															</xsl:for-each>
														</td>
													</tr>
												</table>
											</xsl:if>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
						<br />
					</xsl:if>
				</xsl:for-each>
				<h2>Valid:</h2>
				<xsl:for-each select="KOSTValLog/Format/Validation">
					<xsl:if test="Valid">
						<div>
							<table width="100%">
								<tr class="captionio">
									<td>
										<xsl:value-of select="ValType" />
										<xsl:value-of select="FormatVL" />
										->
										<xsl:value-of select="ValFile" />
									</td>
								</tr>
							</table>
							<table width="100%">
								<xsl:for-each select="Error">
									<tr class="captioniom">
										<td width="25%">
											<xsl:value-of select="Modul" />
										</td>
										<td width="75%">
											<xsl:for-each select="Message">
												<xsl:value-of select="." />
												<br />
											</xsl:for-each>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
					</xsl:if>
				</xsl:for-each>
				<br />
				<hr noshade="noshade" size="1" />
				<h3>
					<xsl:value-of select="KOSTValLog/Infos/Start" />
					-
					<xsl:value-of select="KOSTValLog/Infos/End" />
				</h3>
				<p class="footer">
					<xsl:value-of select="KOSTValLog/Infos/Info" />
				</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>