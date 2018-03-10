<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	xmlns:fun="http://www.proxml.be/functions/"
	
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	
	xmlns:cube="http://purl.org/linked-data/cube#"
	
	xmlns:milieu="https://id.milieuinfo.be/def#"
	
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:qudt="http://qudt.org/schema/qudt#"
	xmlns:blazegeo="http://www.proxml.be/blazegeo/wgs84_pos#"
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"
	
	exclude-result-prefixes="fun xs rdf rdfs skos dct foaf milieu geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<xsl:import href="milieu-common/milieu-general.xsl"/>
	
	<xsl:param name="domain" select="'imjv'"></xsl:param>
	
	<xsl:variable name="skin-html-head-title">
		<xsl:choose>
			<xsl:when test="$domain = 'imjv'">
				<xsl:text>Data Integraal Milieu Jaarverslag</xsl:text>
			</xsl:when>
			<xsl:when test="$domain = 'cbb'">
				<xsl:text>Data Centraal Bedrijven Bestand</xsl:text>
			</xsl:when>
			<xsl:when test="$domain = 'gpbv'">
				<xsl:text>Data GPBV</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>Data</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!-- application specific JS, includes configuration of sparql endpoints -->
	<xsl:function name="fun:build-javascript-references-application">
		<!-- config -->
		<xsl:copy-of select="fun:domain-specific-config($domain)"/>
		<!-- inbound links specific -->
		<script src="/js/milieuinfo-inbound-links.js">_</script>
	</xsl:function>
	
	
	
</xsl:stylesheet>
