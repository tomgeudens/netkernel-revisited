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
		<xsl:param name="root" as="element()"/>
		<xsl:param name="param-1" as="xs:string"/>
		<html>
			<head>
			    <title><xsl:value-of select="$skin-html-head-title"/></title>
				<link rel="alternate" type="application/rdf+xml" href="{$param-1}.rdf"/>
				<link rel="alternate" type="text/turtle" href="{$param-1}.ttl"/>
				<link rel="alternate" type="text/plain" href="{$param-1}.nt"/>
				<link rel="alternate" type="application/ld+json" href="{$param-1}.jsonld"/>
				<style type="text/css">
					@import url(/css/documentation.css);
					@import url(/css/explorer.css);
					@import url(/css/page.css);
					@import url(/css/print.css);
					@import url(/css/resource.css);
					@import url(/css/responsive.css);
   	    		</style>
			</head>
			<body>
				<div id="header">
					<div id="logo">
						<a href="/"><span>LNE</span></a>
					</div>
				</div>
				
				<xsl:copy-of select="fun:build-html-body($root)"/>

				<div id="footer">
					<div>
						<div class="logo">
							<span class="title">Vlaanderen</span>
							<span class="claim">verbeelding werkt</span>
						</div>
				
						<div class="site-info">
							<h3>Dit is een officiële website van de Vlaamse overheid</h3>
							<span>uitgegeven door het <a href="https://www.lne.be/">Departement Leefmilieu, Natuur en Energie (LNE)</a></span>
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:function>
	
	<!-- requires implementation in the calling stylesheet -->
	<xsl:function name="fun:build-html-body">
		<xsl:param name="root" as="element()"/>
		<div>[DUMMY CONTENT NEEDS REPLACING]</div>
	</xsl:function>

</xsl:stylesheet>
