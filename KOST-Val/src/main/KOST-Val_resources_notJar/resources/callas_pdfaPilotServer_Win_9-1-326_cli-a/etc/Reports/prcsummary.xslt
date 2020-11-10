<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:prc="http://www.callassoftware.com/namespace/prc" >
<xsl:output method="html" 
            encoding="UTF-8" 
            omit-xml-declaration="yes" 
            doctype-public="-//W3C//DTD HTML 4.01//EN"
            media-type="text/html"
            indent="no"/>
<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="/">
	<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
			<style>
				body{
					font-family: Verdana; 
<!--
					position:absolute;
					top:2em;
					font-size: 9pt;
-->
				}
<!--				span.name {font-size: 12pt;color: red;}-->
				span.label      {font-weight: bold;}
				span.value      {font-style: italic;}
				span.missing    {color: red;}
				span.object     {font-weight: bold; font-size: large;}
				span.metaprof_heading{
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAAF1QTFRF////MX6Tqqqq6urqa2VL9McFwKEgYV5Rq5IrlpaWi4uLoIswdm1G1dXVgHRBi3w7tZomdnZ2lYM2v7+/yqkba2troKCg6r8LysrKYWFh9PT01bAWVlZWgICA/84A3gacdAAAAAJ0Uk5T/wDltzBKAAAAIXRFWHRTb2Z0d2FyZQBHcmFwaGljQ29udmVydGVyIChJbnRlbCl3h/oZAAAAhUlEQVR4nFzP6Q7DIAgA4Go9u7a7Dw55/8ecumq68gOSj0BgUIcYav7gARLcd5C7KZjr0iFFTCQXP40NZgAK1p5Y4w+IVsPGOT81EHH8MKzbSAZhkeDj2IEyiL2BxgrvFSqUtQXO/MIMzqdtpFSm+blsS+s9DHr/i1IR1T/0+AIAAP//AwAcxwn3+Xv4KAAAAABJRU5ErkJggg==) 0em 0.2em no-repeat; )
				  }
				span.profile_heading{
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAAFFQTFRF////gYGBa2trwcHBZYzfZWhvW2qKXW+VYHu1Z5T0V1pgWF5rs7OziIiIWmaAYX+/WWJ1XmFnenp6X3equrq6ZIjUc3Nzj4+PrKysaZn/ijxYMjBqIwAAABt0Uk5T//////////////////////////////////8AJzQLNQAAACF0RVh0U29mdHdhcmUAR3JhcGhpY0NvbnZlcnRlciAoSW50ZWwpd4f6GQAAAIxJREFUeJxcz8kOxSAIQNE6dx5eEYH//9CHtnHRu8GckBAH+TS8E6gFFdawo4P51lKpEDwfRnBi5tzg1BfKljvgyLpquYMzWIQis38AQI/QlaP94VKBZjQ25ova8Qq37nIkKWicPKBZKYlH7JA3QR1nhwnFHOxDg5S1BRzuYa0ApUWf3/b+AAAA//8DADcyFR5l99n2AAAAAElFTkSuQmCC) 0em 0.2em no-repeat; )
				  }
				span.ruleset_heading{
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAADxQTFRF////paWlglyVurq6eluKenp6rmLUn2C/c3NzXV1dbFl1a2trj4+PXVdgwcHBc1qArKysvWTpzGb/tUT4pDtIDgAAABR0Uk5T/////////////////////////wBPT+cRAAAAIXRFWHRTb2Z0d2FyZQBHcmFwaGljQ29udmVydGVyIChJbnRlbCl3h/oZAAAAf0lEQVR4nGSPhw6AIAxEq8geBfv//+ohGhyXNDSvXAfJqQqNjBAl+cAcfCoDqMhthxrH0kGJ635pBSFJ3KvG9F+cADwym53LFtwL1YA3LyJLRiVUqnAY1wc6Aw/ABqBPYAf4Wu6m+m56jbVz7G+x5+rqcdw2j3uffwAAAP//AwAC/w8C5OGlZQAAAABJRU5ErkJggg==) 0em 0.2em no-repeat; )
				  }
				span.rule_heading{
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAADxQTFRF////paWlglyVurq6eluKenp6rmLUn2C/c3NzXV1dbFl1a2trj4+PXVdgwcHBc1qArKysvWTpzGb/tUT4pDtIDgAAABR0Uk5T/////////////////////////wBPT+cRAAAAIXRFWHRTb2Z0d2FyZQBHcmFwaGljQ29udmVydGVyIChJbnRlbCl3h/oZAAAAf0lEQVR4nGSPhw6AIAxEq8geBfv//+ohGhyXNDSvXAfJqQqNjBAl+cAcfCoDqMhthxrH0kGJ635pBSFJ3KvG9F+cADwym53LFtwL1YA3LyJLRiVUqnAY1wc6Aw/ABqBPYAf4Wu6m+m56jbVz7G+x5+rqcdw2j3uffwAAAP//AwAC/w8C5OGlZQAAAABJRU5ErkJggg==) 0em 0.2em no-repeat; )
				  }
				span.condition_heading{
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAADlQTFRF////k5KT2dnZ8PDwq6urkJCQoqGinZydo6Ojm5ubi4uLwsLCjIyMhoaG+Pj4jo2O4eHhp6anrKusBIrm8gAAACF0RVh0U29mdHdhcmUAR3JhcGhpY0NvbnZlcnRlciAoSW50ZWwpd4f6GQAAAHpJREFUeJxkj0kCgCAMA4sgOxT9/2MNi4KaCzAlaUvUVCB6lEP0zD6G3N/S8XFCB7tGshPnkGgkcK1qXX9xAIi4mWRtMuAR+R5n2oi2hIovVODQtoZZDQ/ADqAaMB18LXeoukNHWzPb/gZbR5fLcvtc7r3+BQAA//8DAFQyCfZSm4Z7AAAAAElFTkSuQmCC) 0em 0.2em no-repeat; )
				  }
				span.fixupset_heading{
				    display:block;
				    text-indent:1.5em;
				    background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAACdQTFRF/////4IvdVlFtbW19GUF9IE1//Xv/8WflVw1wcHBa2tr/2YAyHB1D8RHzAAAAA10Uk5T////////////////AD3oIoYAAAAhdEVYdFNvZnR3YXJlAEdyYXBoaWNDb252ZXJ0ZXIgKEludGVsKXeH+hkAAABpSURBVHicTM9RAoAgCATRNStBvP95A1xTv5pXGmLk0t51PoEtFwVsuylg2xJE1+JgTw1BvG+vS3mbuCC/d/E2c0Hud4kOIVjLDlAKWzEOyUPHFpm//UXWYBTZo6fIebnz+h8AAAD//wMAOgkKCeORN9gAAAAASUVORK5CYII=) 0em 0.2em no-repeat; )
				}
				span.fixup_heading{
				    display:block;
				    text-indent:1.5em;
				    background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAACdQTFRF/////4IvdVlFtbW19GUF9IE1//Xv/8WflVw1wcHBa2tr/2YAyHB1D8RHzAAAAA10Uk5T////////////////AD3oIoYAAAAhdEVYdFNvZnR3YXJlAEdyYXBoaWNDb252ZXJ0ZXIgKEludGVsKXeH+hkAAABpSURBVHicTM9RAoAgCATRNStBvP95A1xTv5pXGmLk0t51PoEtFwVsuylg2xJE1+JgTw1BvG+vS3mbuCC/d/E2c0Hud4kOIVjLDlAKWzEOyUPHFpm//UXWYBTZo6fIebnz+h8AAAD//wMAOgkKCeORN9gAAAAASUVORK5CYII=) 0em 0.2em no-repeat; )
				}
				span.action_heading{
				    display:block;
				    text-indent:1.5em;
				    background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAAEhQTFRF////YlaZEK1wz+/iM2Zm7/r1mZmZhbml6urqAJlmMLiDIIlgZsyZn97GJoRfRmVZZmZmj9m8zMzMS2BYEJhjtbW1qqqqFpNiihxIpgAAAAJ0Uk5T/wDltzBKAAAAIXRFWHRTb2Z0d2FyZQBHcmFwaGljQ29udmVydGVyIChJbnRlbCl3h/oZAAAAbElEQVR4nGyO2wqAIBBE1bxtWtl22f//0waFIG0eDsOB1VG6i9KR3zTBVkRCAJKvQhcimp0BjybuhD5lYL2qiBt6ngAqVWh/Ehk3Q1iugi36vgDi/0V/Mjw6fNsPG6bz/Sa26d88AAAA//8DAE8xCEap0ZpyAAAAAElFTkSuQmCC) 0em 0.2em no-repeat; )
				}
				span.step_heading{
					background-color:silver; 
					display:block;
					text-indent:1.5em;
					background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAAXNSR0IArs4c6QAAAWJQTFRF////j79mn5+f8vfuZGNkc69B5fDb1+jJhrpblsJxvNmkaGtl1ufIe7NNUFlJcpBZrdCP5O/bb6NCd3xw5e/bd5paXFxcbaY+XV1dk75wdplZyeC2i71hZGRkX3FQlcFxaYFV8/bvT09PyuG2nMB+nMJ7b25vXFtctba0VVVVfKBeRklElcBxuNaelqSKfpNteH1xjL1ipsyGj7pslsFxfbJSgod+lsJyb6k/kMBoirdkh7FlbGtsl6KOnLSIyMzDiY2FdrFFk7xwjr5ln8h8mcV0i7hleH5yb6RDc65BibtftdWbmMVzf5RugrhWbm5utdWadLBChbZbcq1BfLROm8N6grhVstOWhLlYn7eLjaB9dbFE1+jIncd5bqc/j75mcaxAjL5jeLFIyMzEbqg/0+bDb6VDr9GRnMZ4b6g/hbpZcatAhK9gibpfd7JHh5h4yuC2msJ5VWFLTk5OQkJCJugZuDOKBAAAAHZ0Uk5T////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////AAFiqUcAAAAhdEVYdFNvZnR3YXJlAEdyYXBoaWNDb252ZXJ0ZXIgKEludGVsKXeH+hkAAADoSURBVHicYigFAQkmIJAAMxmAWFNDkRkIFDU0IQLqyYIibEAgImgvBhJQ0uPRNQ5IZWdnj+GxLQYKGIRKCxQpOHkrKxdIFwqXMhSLmnAZFZVo82eEc3HpSBUzsHh4pbsUlZSU8GnFCuQKsTBwp7C6+oAESkrkrHnFZRm4M1kdE6ECOSABljTPwDCIlqzoYKCWYtEE1giQoYZ+eazZQENL3S1CZIoUrGQ4ODiS4oRBDvMNinezZAQCZ1N9kMNKxaIk5TnNOTnlJfPVIZ4rdohUUVVVsTMrhvoWKOSvpmZTDGYCAAAA//8DABNzQ05H8nG0AAAAAElFTkSuQmCC) 0em 0.2em no-repeat; )
					hover:;
				  }
				span.step_heading, span.ruleset_heading, span.fixupset_heading:hover{
					cursor: pointer;
				}
				div.header    {
					display:none;
<!--				    
					display:block;
					position:fixed;
					top:0;
					height:4em;
					width:100%;
					background:#FFF;
					margin:auto; 
					z-index:100000;
				    overflow: scroll;
					text-align:left;
					vertical-align:bottom;
-->
				}
				div.content    {
					display:block;
<!--				    
					position:absolute;
					top:4em;
					height:70%;
-->
				}
				div.metaprof    {margin-left:15px;}
				div.metaprof_details    {margin-left:15px;}
				div.profile     {margin-left:15px;}
				div.profile_details    {margin-left:15px;}
				div.ruleset     {margin-left:15px;}
				div.ruleset_details    {margin-left:15px;}
				div.rule     {margin-left:15px;}
				div.rule_details    {margin-left:15px;}
				div.sequence    {margin-left:15px;}
				div.sequence_details    {margin-left:15px;}
				div.step        {margin-left:15px;}
				div.step_details    {margin-left:15px;}
				div.condition   {margin-left:15px;}
				div.condition_details    {margin-left:15px;}
				div.fixupset   {margin-left:15px;}
				div.fixupset_details    {margin-left:15px;}
				div.fixup   {margin-left:15px;}
				div.fixup_details    {margin-left:15px;}
				div.action   {margin-left:15px;}
				div.action_details    {margin-left:15px;}

				div.fixup_feature   {margin-left:15px;}
				div.fixup_settings   {margin-left:15px;}
			</style>
			<script type="text/javascript">
				<xsl:text>&lt;!--</xsl:text>
				function toggle_visibility(id) {
				   var e = document.getElementById(id);
				   if(e.style.display == 'none')
					  e.style.display = 'block';
				   else
					  e.style.display = 'none';
				}
				<xsl:text>--&gt;</xsl:text>
			</script>			
			<title>
				<xsl:value-of select="/prc:prcsummary/prc:summary/prc:title"/>
			</title>
		</head>
		<body>
			<xsl:apply-templates select="/prc:prcsummary/prc:summary"/>
		</body>
	</html>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

<xsl:template match="/prc:prcsummary/prc:summary">

	<div class="header">
		<xsl:value-of select="prc:title"/><br/>
		<xsl:value-of select="prc:info"/><br/>
	</div>
	<div class="content">
		<h1><xsl:value-of select="prc:title"/></h1>
		<h4><xsl:value-of select="prc:info"/></h4>
		<xsl:variable name="vID" select="@id"/>
		<xsl:choose>
			<xsl:when test="@type='metaprof'">
				<xsl:apply-templates select="/prc:prcsummary/prc:metaprofs/prc:metaprof[@id=$vID]" mode="details"/>
			</xsl:when>
			<xsl:when test="@type='profile'">
				<xsl:apply-templates select="/prc:prcsummary/prc:profiles/prc:profile[@id=$vID]" mode="details"/>
			</xsl:when>
			<xsl:otherwise>
				<div class="other">
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</div>
	<div class="footer">
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="*" mode="comment">
	<xsl:variable name="vType" select="name(.)"/>
<!--
	<span class="label">Type: <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key=$vType]"/></span><br/>
	<span class="label">Name: <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:name"/></span><br/>
-->	
	<span class="label"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='purpose_']"/> <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:comment"/></span>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:metaprof" mode="details">

	<xsl:variable name="vID" select="@id"/>

	<div class="metaprof">
		<span class="metaprof_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='metaprof']"/> </span>
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
		</span>
		<div class="metaprof_details">
			<xsl:apply-templates select="." mode="comment"/><br/>
			<span class="label">Sequence: </span><br/>
			<div class="sequence">
				<xsl:for-each select="prc:sequence/prc:step">
					<xsl:variable name="vStepID">SD<xsl:value-of select="$vID"/><xsl:number/></xsl:variable>
					<div class="step">
						<span class="step_heading">
						<xsl:attribute name="onClick">toggle_visibility('<xsl:value-of select="$vStepID"/>')</xsl:attribute>
						<span class="object">Step <xsl:number />: </span>
						<span class="vlaue"><xsl:apply-templates select="." mode="step_title"/></span>
						</span>
						<div class="step_details">
							<xsl:attribute name="id"><xsl:value-of select="$vStepID" /></xsl:attribute>
							<xsl:apply-templates select="." mode="step_details"/>
						</div>
					</div>
				</xsl:for-each>
			</div>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:step" mode="step_title">
	<xsl:choose>
		<xsl:when test="prc:profile">
			<xsl:variable name="vID" select="prc:profile"/>
			"<xsl:value-of select="/prc:prcsummary/prc:profiles/prc:profile[@id=$vID]/prc:name"/>"
		</xsl:when>
		<xsl:when test="prc:rule">
			<xsl:variable name="vID" select="prc:rule"/>
			"<xsl:value-of select="/prc:prcsummary/prc:rules/prc:rule[@id=$vID]/prc:name"/>"
		</xsl:when>
		<xsl:when test="prc:fixup">
			<xsl:variable name="vID" select="prc:fixup"/>
			"<xsl:value-of select="/prc:prcsummary/prc:fixups/prc:fixup[@id=$vID]/prc:name"/>"
		</xsl:when>
		<xsl:when test="prc:action">
			<xsl:variable name="vID" select="prc:action"/>
			"<xsl:value-of select="/prc:prcsummary/prc:actions/prc:action[@id=$vID]/prc:name"/>"
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:step" mode="step_details">
	<xsl:for-each select="prc:profile">
		<span class="label"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='omit_fixups_']"/><xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="../prc:params/@omit_fixups"/></span><br/>
		<xsl:variable name="vID" select="."/>
		<xsl:apply-templates select="/prc:prcsummary/prc:profiles/prc:profile[@id=$vID]" mode="details">
			<xsl:with-param name="pIDStep" select="../@id"/>
			<xsl:with-param name="omit_fixups" select="../prc:params/@omit_fixups"/>
		</xsl:apply-templates>
	</xsl:for-each>
	<xsl:for-each select="prc:rule">
		<xsl:variable name="vID" select="."/>
		<xsl:apply-templates select="/prc:prcsummary/prc:rules/prc:rule[@id=$vID]" mode="details"/>
	</xsl:for-each>
	<xsl:for-each select="prc:fixup">
		<xsl:variable name="vID" select="."/>
		<xsl:apply-templates select="/prc:prcsummary/prc:fixups/prc:fixup[@id=$vID]" mode="details"/>
	</xsl:for-each>
	<xsl:for-each select="prc:action">
		<xsl:variable name="vID" select="."/>
		<xsl:apply-templates select="/prc:prcsummary/prc:actions/prc:action[@id=$vID]" mode="details"/>
	</xsl:for-each>
<!--
	<xsl:variable name="vID" select="@id"/>
	<xsl:choose>
		<xsl:when test="@type='profile'">
			<xsl:apply-templates select="/prc:prcsummary/prc:profiles/prc:profile[@id=$vID]" mode="details"/>
		</xsl:when>
		<xsl:when test="@type='rule'">
			<xsl:apply-templates select="/prc:prcsummary/prc:rules/prc:rule[@id=$vID]" mode="details"/>
		</xsl:when>
		<xsl:when test="@type='fixup'">
			<xsl:apply-templates select="/prc:prcsummary/prc:fixups/prc:fixup[@id=$vID]" mode="details"/>
		</xsl:when>
		<xsl:when test="@type='action'">
			<xsl:apply-templates select="/prc:prcsummary/prc:actions/prc:action[@id=$vID]" mode="details"/>
		</xsl:when>
	</xsl:choose>
-->
</xsl:template>


<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:profile" mode="details">
    <xsl:param name="pIDStep"/>
    <xsl:param name="omit_fixups" select="0"/>
	<xsl:variable name="vpIDProfile"><xsl:value-of select="$pIDStep"/><xsl:value-of select="@id"/></xsl:variable>
	<div class="profile">
		<span class="profile_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='profile']"/> </span>
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
		</span>
		<div class="profile_details">
			<xsl:apply-templates select="." mode="comment"/><br/>
			<xsl:for-each select="prc:rulesets/prc:ruleset">
				<xsl:variable name="vID" select="."/>
				<xsl:apply-templates select="/prc:prcsummary/prc:rulesets/prc:ruleset[@id=$vID]" mode="details">
					<xsl:with-param name="pIDProfile" select="$vpIDProfile"/>
				</xsl:apply-templates>
			</xsl:for-each>
			<xsl:if test="not(number($omit_fixups))">
				<xsl:for-each select="prc:fixupsets/prc:fixupset">
					<xsl:variable name="vID" select="."/>
					<xsl:apply-templates select="/prc:prcsummary/prc:fixupsets/prc:fixupset[@id=$vID]" mode="details">
						<xsl:with-param name="pIDProfile" select="$vpIDProfile"/>
					</xsl:apply-templates>
				</xsl:for-each>
			</xsl:if>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:ruleset" mode="details">
    <xsl:param name="pIDProfile"/>
	<xsl:variable name="vDetailsID"><xsl:value-of select="$pIDProfile"/><xsl:value-of select="@id"/>D</xsl:variable>
	<div class="ruleset">
		<span class="ruleset_heading">
			<xsl:attribute name="onClick">toggle_visibility('<xsl:value-of select="$vDetailsID"/>')</xsl:attribute>
			<span class="object"><xsl:value-of select="prc:name"/></span><br/>
		</span>
		<div class="ruleset_details">
			<xsl:attribute name="id"><xsl:value-of select="$vDetailsID" /></xsl:attribute>
			<xsl:apply-templates select="." mode="comment"/><br/>
				<xsl:for-each select="prc:rules/prc:rule">
					<xsl:variable name="vID" select="."/>
					<xsl:apply-templates select="/prc:prcsummary/prc:rules/prc:rule[@id=$vID]" mode="details"/>
				</xsl:for-each>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:rule" mode="details">
	<div class="rule">
		<span class="rule_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='rule']"/> </span>
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
		</span>
		<div class="rule_details">
			<xsl:apply-templates select="." mode="comment"/><br/>
			<xsl:for-each select="prc:conditions/prc:condition">
				<xsl:variable name="vID" select="."/>
				<xsl:apply-templates select="/prc:prcsummary/prc:conditions/prc:condition[@id=$vID]" mode="details"/>
			</xsl:for-each>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:condition" mode="details">
	<div class="condition">
		<span class="condition_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='condition']"/> </span>
<!-- conditions have no meaningfull name
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
-->
		</span>
		<div class="condition_details">
			<span class="label"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='group_']"/>  <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:group"/></span><br/>
			<span class="label"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='property_']"/>  <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:property"/></span><br/>
			<span class="label"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='operator_']"/>  <xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:operator"/></span><br/>
			<xsl:choose>
				<xsl:when test="prc:data/@type='text'">
					<span class="label">Text: "<xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:data/prc:text"/>"</span><br/>
				</xsl:when>
				<xsl:when test="prc:data/@type='number'">
					<span class="label">Number: "<xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:data/prc:number"/>"</span><br/>
					<span class="label">Tolerance: "<xsl:text> </xsl:text></span><span class="value"><xsl:value-of select="prc:data/prc:tolerance"/>"</span><br/>
				</xsl:when>
			</xsl:choose>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:fixupset" mode="details">
    <xsl:param name="pIDProfile"/>
	<xsl:variable name="vDetailsID"><xsl:value-of select="$pIDProfile"/><xsl:value-of select="@id"/>D</xsl:variable>
	<div class="fixupset">
		<span class="fixupset_heading">
			<xsl:attribute name="onClick">toggle_visibility('<xsl:value-of select="$vDetailsID"/>')</xsl:attribute>
			<span class="object"><xsl:value-of select="prc:name"/></span><br/>
		</span>
		<div class="fixupset_details">
			<xsl:attribute name="id"><xsl:value-of select="$vDetailsID" /></xsl:attribute>
			<xsl:for-each select="prc:fixups/prc:fixup">
				<xsl:variable name="vID" select="."/>
				<xsl:apply-templates select="/prc:prcsummary/prc:fixups/prc:fixup[@id=$vID]" mode="details"/>
			</xsl:for-each>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:fixup" mode="details">
	<div class="fixup">
		<span class="fixup_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='fixup']"/> </span>
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
		</span>
		<div class="fixup_details">
			<xsl:apply-templates select="." mode="comment"/><br/>
			<xsl:for-each select="prc:configs/prc:config/prc:params/prc:param">
				<span class="label"><xsl:value-of select="prc:name"/>: "</span>
				<span class="value">
				<xsl:choose>
					<xsl:when test="@custom_rule">
						<xsl:variable name="vID" select="@custom_rule"/>
						<xsl:value-of select="/prc:prcsummary/prc:rules/prc:rule[@id=$vID]/prc:name"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="prc:value"/>"
					</xsl:otherwise>
				</xsl:choose>
				</span><br/>
			</xsl:for-each>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="prc:action" mode="details">
	<div class="action">
		<span class="action_heading">
			<span class="object"><xsl:value-of select="/prc:prcsummary/prc:dict/prc:entry[@key='action']"/> </span>
			<span class="value">"<xsl:value-of select="prc:name"/>"</span><br/>
		</span>
		<div class="action_details">
			<xsl:apply-templates select="." mode="comment"/><br/>
			<xsl:for-each select="prc:params/prc:param">
				<span class="label"><xsl:value-of select="prc:name"/>: "</span>
				<span class="value"><xsl:value-of select="prc:value"/>"</span>
				<br/>
			</xsl:for-each>
		</div>
	</div>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

</xsl:stylesheet>
