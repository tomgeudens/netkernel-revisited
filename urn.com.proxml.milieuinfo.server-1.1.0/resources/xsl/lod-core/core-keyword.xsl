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
	
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:qudt="http://qudt.org/schema/qudt#"
	xmlns:blazegeo="http://www.proxml.be/blazegeo/wgs84_pos#"
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"
	
	xmlns:sp="http://www.w3.org/2005/sparql-results#"

	exclude-result-prefixes="sp fun xs rdf rdfs skos dct foaf geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<xsl:import href="core-sparql.xsl"/>
	
	<xsl:param name="search" as="xs:string" select="'#search'"/>
	
	<xsl:function name="fun:get-main-title">
		<xsl:param name="context"/>
		<h1>
			<xsl:text>Resultaten zoekopdracht met sleutelwoord "</xsl:text>
			<xsl:value-of select="$search"/>
			<xsl:text>"</xsl:text>
		</h1>
	</xsl:function>
	
	<xsl:template match="sp:sparql">
		<xsl:copy-of select="fun:html-skin(., 'keywordsearch')"/>
	</xsl:template>
	
</xsl:stylesheet>
