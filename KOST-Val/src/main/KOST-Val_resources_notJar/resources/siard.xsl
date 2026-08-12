<?xml version="1.0" encoding="iso-8859-1"?>
<!--
=== SiardMetaToXhtml.xsl ===============================================
alte metadata.xsl erneuert
========================================================================
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:siard="http://www.bar.admin.ch/xmlns/siard/1.0/metadata.xsd" xmlns:html="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="html" version="2.0">
	<xsl:output method="xml" indent="yes" encoding="iso-8859-1" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
	<xsl:template match="/*[local-name()='siardArchive']">
		<html>
			<head>
				<style type="text/css">
					<xsl:call-template name="put-table-styles"/>
					<xsl:call-template name="put-new-table-styles"/>
				</style>
				<xsl:call-template name="put-legacy-styles"/>
			</head>
			<body>
				<h1>
					SIARD Summary
				</h1>
				<xsl:apply-templates select="." mode="header-table"/>
				<p>
					<nbsp/>
				</p>
				<!-- skip everything if there is no schema -->
				<xsl:if test="count(/*[local-name()='siardArchive']/*[local-name()='schemas']/*[local-name()='schema']) &gt; 0">
					<xsl:call-template name="build-schemas-and-tables"/>
					<p>
						<nbsp/>
					</p>
					<xsl:call-template name="build-overview"/>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="put-table-styles">
		<!-- Common table  styles -->
		.tableTitleDark { 
			text-align: left; font-weight: bold; 
			color: black;
			border-style: none; }

		.horizontalTitleColumn { text-align: left; }

		table.light th { <!-- Spaltenbeschriftung -->
			border-width: 1px; padding: 1px;
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #afdfee; <!-- tuerkis-blau -->
		}

		table.light td { <!-- Spaltendetails -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #F0F8FF; <!-- hellblau -->
		}

		table.strong td { <!-- Schemazeile -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #5da0e3; <!-- dunkelblau -->
		}

		table.title th { <!-- Metadatenbeschriftung -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #c9c9c9; <!-- grau -->
		}

		table.title td { <!-- Metadatentext -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #F5F5F5; <!-- hellgrau -->
		}
		<!-- Einrückung (statt verschachtelte Listen) -->
		.indented {
			margin-left: 20px; margin-top: 10px;
		}
		.indented2 {
			margin-left: 20px; margin-top: 2px;
		}
		.margin10 {
			margin-left: 0px; margin-top: 10px;
		}
		.margin2 {
			margin-left: 0px; margin-top: 2px;
		}
		
	<!-- Data base description styles -->
	.databaseTitleColumn { text-align: left; } 
	.databaseValueColumn { text-align: left; }

	<!-- Schema description styles -->
	.schemaValueColumn { text-align: left; }

	h1.small { font-size: 14pt; }

	}
</xsl:template>
	<xsl:template name="put-new-table-styles">
		table.tabelle { <!-- TABLE Tabellen -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #add6ff; <!-- blau -->
		}
	
		table.tabelle td { <!-- TABLE Tabellen -->
			border-width: 1px; padding: 1px; 
			border-style: none; border-color: white;
			vertical-align: text-top; 
			background-color: #add6ff; <!-- blau -->
		}
		
	</xsl:template>
	<!-- show the table in the head of the document / Metadaten -->
	<xsl:template match="/*[local-name()='siardArchive']" mode="header-table">
		<table width="100%" class="title">
			<tr>
				<th width="170px" class="databaseTitleColumn">
					<xsl:text>Name</xsl:text>
				</th>
				<th class="databaseValueColumn">
					<xsl:value-of select="*[local-name()='dbname']"/>
				</th>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Version</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:value-of select="/*[local-name()='siardArchive']/@version"/>
				</td>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Description</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:call-template name="get-node-value">
						<xsl:with-param name="tag" select="'description'"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Archiver</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:call-template name="get-node-value"><xsl:with-param name="tag" select="'archiver'"/></xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Data owner</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:call-template name="get-node-value"><xsl:with-param name="tag" select="'dataOwner'"/></xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Data origin timespan</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:call-template name="get-node-value"><xsl:with-param name="tag" select="'dataOriginTimespan'"/></xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="databaseTitleColumn">
					<xsl:text>Database product</xsl:text>
				</td>
				<td class="databaseValueColumn">
					<xsl:call-template name="get-node-value"><xsl:with-param name="tag" select="'databaseProduct'"/></xsl:call-template>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- Schema / Small TOC-like listing showing schemas and tables initiates mode="schemes-and-tables" -->
	<xsl:template name="build-schemas-and-tables">
		<div class="margin2">
			<xsl:apply-templates select="*[local-name()='schemas']/*[local-name()='schema']" mode="schemes-and-tables"/>
		</div>
	</xsl:template>
	<!-- Shema-Tables /  the TOC consists of the schemas and their tables (alphabetically sorted by name) mode= schemes-and-tab" -->
	<xsl:template match="*[local-name()='schema']" mode="schemes-and-tables">
		<a class="margin2">
			<xsl:call-template name="get-node-value">
				<!-- <xsl:with-param name="tag" select="'name'"/> -->
			</xsl:call-template>
		</a>
		<!-- output tables -->
		<xsl:for-each select="*[local-name()='tables']/*[local-name()='table']">
			<xsl:sort/>
			<a class="margin2">
				<xsl:call-template name="get-node-value">
					<!-- <xsl:with-param name="tag" select="'name'"/> -->
				</xsl:call-template>
			</a>
		</xsl:for-each>
	</xsl:template>
	<!-- "big" Overwiev (incl. columns) initiates mode="overview" -->
	<xsl:template name="build-overview">
		<xsl:apply-templates select="*[local-name()='schemas']/*[local-name()='schema']" mode="overview"/>
	</xsl:template>
	<!-- mode="overview" Level1: Schema -->
	<xsl:template match="*[local-name()='schema']" mode="overview">
		<xsl:variable name="schemaName" select="*[local-name()='name']"/>
		<div class="margin10">
			<table class="strong" id="toc-{generate-id()}" width="100%">
				<tr>
					<td width="20%" class="tableTitleDark">
						SCHEMA: 
					</td>
					<td width="40%" class="tableTitleDark">
						<xsl:call-template name="get-node-value">
							<xsl:with-param name="tag" select="'name'"/>
						</xsl:call-template>
					</td>
					<td width="40%" class="tableTitleDark">
						<xsl:call-template name="get-node-value">
							<xsl:with-param name="tag" select="'folder'"/>
						</xsl:call-template>
					</td>
				</tr>
			</table>
			<!-- recurse into tables-->
			<xsl:apply-templates select="*[local-name()='tables']/*[local-name()='table']" mode="overview"/>
		</div>
	</xsl:template>
	<!-- mode="overview" Level2: Tables (styles spoiled) -->
	<xsl:template match="*[local-name()='table']" mode="overview">
		<div class="indented">
			<table class="tabelle" id="toc-{generate-id()}" width="100%">
				<tr>
					<td width="10%" class="tableTitleDark">
						<!-- <a href="#{generate-id()}">TABLE:</a> -->
						TABLE:
					</td>
					<td width="38%" class="tableTitleDark">
						<xsl:call-template name="get-node-value">
							<xsl:with-param name="tag" select="'name'"/>
						</xsl:call-template>
					</td>
					<td width="38%" class="tableTitleDark">
						<xsl:call-template name="get-node-value">
							<xsl:with-param name="tag" select="'folder'"/>
						</xsl:call-template>
					</td>
					<td width="14%">
						rows: <xsl:call-template name="get-node-value">
							<xsl:with-param name="tag" select="'rows'"/>
						</xsl:call-template>
					</td>
				</tr>
				<xsl:if test="normalize-space(*[local-name()='description'])">
					<tr>
						<td colspan="4">
							<xsl:call-template name="get-node-value">
								<xsl:with-param name="tag" select="'description'"/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:if>
			</table>
			<!--recurse into columns-->
			<xsl:apply-templates select="*[local-name()='columns']"/>
		</div>
	</xsl:template>
	<!-- mode="overview" level3: column group (columns) Create the columns table -->
	<xsl:template match="*[local-name()='columns']">
		<!-- <div class="not-indented"> -->
		<div class="indented2">
			<table class="light" width="100%">
				<tr>
					<th width="25%" class="horizontalTitleColumn">
						Name
					</th>
					<th width="25%" class="horizontalTitleColumn">
						Type
					</th>
					<th width="25%" class="horizontalTitleColumn">
						Original type
					</th>
					<th width="25%" class="horizontalTitleColumn">
						Description
					</th>
				</tr>
				<xsl:apply-templates select="*[local-name()='column']">
					<xsl:sort/>
				</xsl:apply-templates>
			</table>
		</div>
	</xsl:template>
	<!-- mode="overview" level3b: column add each column (styles spoiled should match to table) -->
	<xsl:template match="*[local-name()='column']">
		<tr>
			<td class="schemaValueColumn">
				<xsl:call-template name="get-node-value">
					<xsl:with-param name="tag" select="'name'"/>
				</xsl:call-template>
			</td>
			<td class="schemaValueColumn">
				<xsl:call-template name="get-node-value">
					<xsl:with-param name="tag" select="'type'"/>
				</xsl:call-template>
			</td>
			<td class="schemaValueColumn">
				<xsl:call-template name="get-node-value">
					<xsl:with-param name="tag" select="'typeOriginal'"/>
				</xsl:call-template>
			</td>
			<td class="schemaValueColumn">
				<xsl:call-template name="get-node-value">
					<xsl:with-param name="tag" select="'description'"/>
				</xsl:call-template>
			</td>
		</tr>
	</xsl:template>
	<!-- helper to make things a bit more readable-->
	<xsl:template name="get-node-value">
		<xsl:param name="tag"/>
		<xsl:value-of select="*[local-name()=$tag]"/>
	</xsl:template>
	<xsl:template name="put-legacy-styles">
		<style type="text/css">
			body {
				font-family: Verdana, Geneva, sans-serif;
				font-size: 10pt;
			}
		</style>
	</xsl:template>
</xsl:stylesheet>