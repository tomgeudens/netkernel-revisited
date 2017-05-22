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
	
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"
	
	exclude-result-prefixes="fun xs rdf rdfs skos dct foaf sdmx-attribute"
	version="2.0">
	
	<xsl:import href="milieu-common/milieu-general.xsl"/>

	<xsl:variable name="skin-html-head-title">
		<xsl:text>Meta Data</xsl:text>
	</xsl:variable>
	
	<!-- application specific JS, includes configuration of sparql endpoints -->
	<xsl:function name="fun:build-javascript-references-application">
		<script>
			(function($) {
			window['milieuinfo-imjv-config'] = {
			// SPARQL EndPoint
			sparqlEndpoint: 'https://id-ontwikkel.milieuinfo.be/imjv/sparql', 
			// Keyword Search
			kwsEndpoint: 'https://id-ontwikkel.milieuinfo.be/imjv/keywordsearch'
			};
			})(jQuery);
		</script>
		<!-- vocabulary specific -->
		<script src="/js/milieuinfo-taxonomy.js">_</script>
		<!-- inbound links specific -->
		<script src="/js/milieuinfo-inbound-links.js">_</script>
	</xsl:function>
	
	
</xsl:stylesheet>
