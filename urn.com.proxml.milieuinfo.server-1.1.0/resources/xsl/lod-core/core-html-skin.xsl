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
	
	<xsl:output 
    	method="html"
    	version="5.0"
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"
	/>
	
	<!-- moet overschreven worden in callers -->
	<xsl:variable name="skin-html-head-title">
		<xsl:text>DEFAULT</xsl:text>
	</xsl:variable>
	
	<!-- noteer dat HTML5 doctype correct gezet wordt door Saxon door in de serialisatie de versie op 5.0 te zetten-->
	
	<xsl:function name="fun:html-skin">
		<xsl:param name="context" as="element()"/>
		<xsl:param name="doc-uri" as="xs:string"/>
		<html>
			<head>
			    <title><xsl:value-of select="$skin-html-head-title"/></title>
				<xsl:copy-of select="fun:alternate-files($doc-uri)"></xsl:copy-of>
				<style type="text/css">
					@import url(/css/documentation.css);
					@import url(/css/explorer.css);
					@import url(/css/page.css);
					@import url(/css/print.css);
					@import url(/css/resource.css);
					@import url(/css/responsive.css);
   	    		</style>
				<xsl:copy-of select="fun:set-favicon-references()"/>
				<xsl:copy-of select="fun:build-css-references()"/>
			</head>
			<body about="{$doc-uri}">
				<div id="header">
					<xsl:copy-of select="fun:build-html-header()"/>
				</div>
				
				<xsl:copy-of select="fun:build-html-body($context, $doc-uri)"/>

				<div id="footer">
					<xsl:copy-of select="fun:build-html-footer()"/>
				</div>
				<xsl:copy-of select="fun:build-javascript-references()"/>
			</body>
		</html>
	</xsl:function>
	
	<!-- materialisation of the function that renders the actual content -->
	<xsl:function name="fun:build-html-body">
		<xsl:param name="context" as="element()"/>
		<xsl:param name="doc-uri"/>
		<div id="content">
			<xsl:copy-of select="fun:export-options($doc-uri)"/>
			<xsl:copy-of select="fun:get-main-title($context)"/>
			<xsl:copy-of select="fun:get-type-specific-content($context)"/>
		</div>
	</xsl:function>
	
	<!-- requires implementation in the calling stylesheet -->
	<xsl:function name="fun:get-type-specific-content">
		<xsl:param name="context"/>
	</xsl:function>
	
	<!-- requires implementation in the calling stylesheet -->
	<xsl:function name="fun:build-html-header">
		<div>[STANDARD HEADER NEEDS REPLACING]</div>
	</xsl:function>
	
	<xsl:function name="fun:build-html-footer">
		<div>[STANDARD FOOTER NEEDS REPLACING]</div>
	</xsl:function>
	
	<!-- FAVICON -->
	
	<xsl:function name="fun:set-favicon-references">
		<!-- standard should be replaced -->
	</xsl:function>
	
	<!-- CSS references -->
	
	<xsl:function name="fun:build-css-references">
		<xsl:copy-of select="fun:build-css-references-core()"/>
		<xsl:copy-of select="fun:build-css-references-domain()"/>
		<xsl:copy-of select="fun:build-css-references-application()"/>
	</xsl:function>
	
	<xsl:function name="fun:build-css-references-core">
		<!-- standard should be replaced -->
	</xsl:function>
	
	<xsl:function name="fun:build-css-references-domain">
		<!-- standard should be replaced -->
	</xsl:function>
	
	<xsl:function name="fun:build-css-references-application">
		<!-- standard should be replaced -->
	</xsl:function>

	<!-- javascript references -->
	
	<xsl:function name="fun:build-javascript-references">
		<xsl:copy-of select="fun:build-javascript-references-core()"/>
		<xsl:copy-of select="fun:build-javascript-references-domain()"/>
		<xsl:copy-of select="fun:build-javascript-references-application()"/>
	</xsl:function>
	
	<xsl:function name="fun:build-javascript-references-core">
		<script src="/js/jquery-3.1.1.min.js">_</script>
	</xsl:function>
	
	<xsl:function name="fun:build-javascript-references-domain">
		<!-- standard should be replaced -->
	</xsl:function>
	
	<xsl:function name="fun:build-javascript-references-application">
		<!-- standard should be replaced -->
	</xsl:function>
	
	
	<!-- met de bedoeling om per content type ev. te over rulen -->
	<xsl:function name="fun:get-main-title">
		<xsl:param name="context"/>
		<xsl:choose>
			<xsl:when test="normalize-space(fun:get-prefered-language-variant($context/rdfs:label))">
				<h1><xsl:value-of select="fun:get-prefered-language-variant($context/rdfs:label)"/></h1>
			</xsl:when>
			<xsl:when test="normalize-space(fun:get-prefered-language-variant($context/skos:prefLabel))">
				<h1><xsl:value-of select="fun:get-prefered-language-variant($context/skos:prefLabel)"/></h1>
			</xsl:when>
			<xsl:when test="normalize-space($context/dct:title)">
				<h1><xsl:value-of select="normalize-space($context/dct:title)"/></h1>
			</xsl:when>
		</xsl:choose>
	</xsl:function>
	
	
	
</xsl:stylesheet>
