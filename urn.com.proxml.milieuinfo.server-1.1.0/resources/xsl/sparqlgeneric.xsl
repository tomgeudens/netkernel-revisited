<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">
	
	<xsl:import href="milieu-common/milieu-sparql.xsl"/>
	
	<xsl:param name="domain" select="'imjv'"></xsl:param>
	
	<xsl:variable name="skin-html-head-title">
		<xsl:choose>
			<xsl:when test="$domain = 'imjv'">
				<xsl:text>Milieuinfo IMJV : SPARQL resultaten</xsl:text>
			</xsl:when>
			<xsl:when test="$domain = 'cbb'">
				<xsl:text>Milieuinfo CBB : SPARQL resultaten</xsl:text>
			</xsl:when>
			<xsl:when test="$domain = 'gpbv'">
				<xsl:text>Milieuinfo GPBV : SPARQL resultaten</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>Milieuinfo : SPARQL resultaten</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
</xsl:stylesheet>