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
	
	xmlns:milieu="http://id.milieuinfo.be/def#"
	
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:qudt="http://qudt.org/schema/qudt#"
	xmlns:blazegeo="http://www.proxml.be/blazegeo/wgs84_pos#"
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"
	
	exclude-result-prefixes="fun xs rdf rdfs skos dct foaf milieu geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<!-- SKIN -->
	
	<xsl:function name="fun:build-html-header">
		<div id="logo">
			<a href="/"><span>LNE</span></a>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:build-html-footer">
		<div>
			<div class="logo">
				<span class="title">Vlaanderen</span>
				<span class="claim">verbeelding werkt</span>
			</div>

			<div class="site-info">
				<h3>Dit is een officiële website van de Vlaamse overheid</h3>
				<span>uitgegeven door het <a href="https://www.lne.be/">Departement Omgeving</a></span>
			</div>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:set-favicon-references">
		<link rel="shortcut icon" href="/img/favicon.ico" />
	</xsl:function>
	
	<xsl:function name="fun:build-css-references-application">
	</xsl:function>
	
	<xsl:function name="fun:build-javascript-references-domain">
		<script src="/js/milieuinfo-map.js">_</script>
		<script src="/js/milieuinfo-section-toggles.js">_</script>
	</xsl:function>
	
</xsl:stylesheet>
