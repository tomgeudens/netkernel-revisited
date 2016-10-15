<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	xmlns:fun="http://www.proxml.be/functions/"
	
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:purl="http://purl.org/dc/terms/"
	
	exclude-result-prefixes="fun xs rdf purl rdfs skos"
	version="2.0">
	
	<xsl:output 
    	method="html"
    	version="5.0"
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"
	/>
	
	<!-- noteer dat HTML5 doctype correct gezet wordt door Saxon door in de serialisatie de versie op 5.0 te zetten-->
	
	<xsl:function name="fun:html-skin">
		<xsl:param name="root" as="element()"/>
		<xsl:param name="param-1" as="xs:string"/>
		<html>
			<head>
			    <title>Data Integraal Milieu Jaarverslag</title>
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
