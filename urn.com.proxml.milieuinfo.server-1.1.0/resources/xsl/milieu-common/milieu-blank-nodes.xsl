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
	
	
	<!-- blank node processing - zeer specifiek -->
	
	<xsl:template match="*[rdf:value]" mode="blank-nodes">
		<p>
			<xsl:value-of select="rdf:value"/>
			<xsl:text> </xsl:text>
			<a>
				<xsl:copy-of select="fun:set-domain-modified-href(sdmx-attribute:unitMeasure/@rdf:resource)"></xsl:copy-of>
				<xsl:value-of select="
					(
					key('resource-by-about', sdmx-attribute:unitMeasure/@rdf:resource, $global-context)/qudt:symbol[normalize-space()][not(normalize-space() = '.')],
					fun:set-property-label(key('resource-by-about', sdmx-attribute:unitMeasure/@rdf:resource, $global-context), sdmx-attribute:unitMeasure/@rdf:resource)
					)[normalize-space()][1]
					"/>
			</a>
		</p>
	</xsl:template>
	
	
</xsl:stylesheet>
