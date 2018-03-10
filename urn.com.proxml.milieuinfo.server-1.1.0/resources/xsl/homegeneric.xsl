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
	
	<xsl:import href="milieu-common/milieu-static.xsl"/>

	<xsl:param name="domain" select="'imjv'"/>
	
	<xsl:param name="supported-domains" as="element(domains)">
		<domains>
			<domain name="IMJV" lookup="1" sparql="1" kws="1"/>
			<domain name="CBB" lookup="1" sparql="1" kws="0"/>
		</domains>
	</xsl:param>
	
	<xsl:variable name="this-domain" select="$supported-domains/domain[lower-case(normalize-space(@name)) = lower-case(normalize-space($domain))]" as="element(domain)*" />
	
	<xsl:variable name="skin-html-head-title">
		<xsl:text>Departement Omgeving Linked Data</xsl:text>
	</xsl:variable>
	
	<!-- application specific JS, includes configuration of sparql endpoints -->
	<xsl:function name="fun:build-javascript-references-application">
		<!-- config -->
		<xsl:copy-of select="fun:domain-specific-config($this-domain/@name)"/>
	</xsl:function>
	
	
	
</xsl:stylesheet>