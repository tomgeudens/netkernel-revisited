<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:nk="http://1060.org"
	exclude-result-prefixes="rdf nk"
	version="1.0">
	
	<xsl:output 
    	method="html" 
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"/>

    <xsl:param name="replace" nk:class="java.lang.String" />
    <xsl:param name="with" nk:class="java.lang.String" />

	<xsl:template match="rdf:RDF">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
			    <title>NACE</title>
				<style type="text/css">
					@import url(/css/pure-min.css);
   	    		</style>
			</head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="*[@rdf:about]">
		<xsl:variable name="abouturl" select="@rdf:about"/>
		<xsl:variable name="modifiedabouturl">
		<xsl:call-template name="replace-string">
			<xsl:with-param name="text" select="$abouturl"/>
			<xsl:with-param name="replace" select="$replace"/>
			<xsl:with-param name="with" select="$with"/>															
			</xsl:call-template>
		</xsl:variable>
		<h2>Identifier: <a href="{$modifiedabouturl}"><xsl:value-of select="@rdf:about"/></a></h2>
		<table class="pure-table pure-table-bordered">
			<xsl:for-each select="*[not(@rdf:resource)]">
				<tr>
					<th><xsl:value-of select="local-name()"/></th>
					<td><xsl:value-of select="."/></td>
				</tr>
			</xsl:for-each>
			<xsl:for-each select="*[@rdf:resource]">
				<tr>
					<xsl:variable name="resourceurl" select="@rdf:resource"/>
					<xsl:variable name="modifiedresourceurl">
					<xsl:call-template name="replace-string">
						<xsl:with-param name="text" select="$resourceurl"/>
						<xsl:with-param name="replace" select="$replace"/>
						<xsl:with-param name="with" select="$with"/>															
						</xsl:call-template>
					</xsl:variable>
					<th><xsl:value-of select="local-name()"/></th>
					<td><a href="{$modifiedresourceurl}"><xsl:value-of select="@rdf:resource"/></a></td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template name="replace-string">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="with"/>
		
		<xsl:choose>
			<xsl:when test="contains($text,$replace)">
				<xsl:value-of select="substring-before($text,$replace)"/>
				<xsl:value-of select="$with"/>
				
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="substring-after($text,$replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="with" select="$with"/>
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	
</xsl:stylesheet>
