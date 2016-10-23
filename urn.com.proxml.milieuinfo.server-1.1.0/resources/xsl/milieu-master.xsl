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
	
	<xsl:import href="milieu-html-skin.xsl"/>
	<xsl:import href="milieu-common.xsl"/>
	<xsl:import href="milieu-blank-nodes.xsl"/>
	
	<xsl:param name="maximum-inccoming-per-property">15</xsl:param>
	
	
	<!-- keys for efficient node retrieval (merk op dat sommige resources gesplitst worden over meerdere rdf:Description -->
	<xsl:key name="resource-by-about" match="rdf:Description" use="normalize-space(@rdf:about)"/>
	<xsl:key name="resource-by-node-id" match="rdf:Description" use="normalize-space(@rdf:nodeID)"/>

	<xsl:variable name="global-context" select="."/>
	
	<xsl:template match="rdf:RDF">
		<xsl:variable name="start-point-resource" select="rdf:Description[fun:is-starting-point(.)]"/>
		<xsl:variable name="doc-id" select="substring-before($start-point-resource/@rdf:about, '#')"/>
		<xsl:copy-of select="fun:html-skin(., fun:domain-modify-uri($doc-id))"></xsl:copy-of>
	</xsl:template>
	
	<!-- materialisation of the function that renders the actual content -->
	<xsl:function name="fun:build-html-body">
		<xsl:param name="root" as="element()"/>
		<xsl:apply-templates select="$root/rdf:Description[fun:is-starting-point(.)]" mode="starting-point"/>
	</xsl:function>
	
	<!-- starting point content	-->
	
	<xsl:template match="rdf:Description" mode="starting-point">
		<xsl:variable name="this-about" select="@rdf:about"/>
		<xsl:variable name="doc-id" select="substring-before($this-about,'#')"/>
		
		<div id="content">
			<xsl:copy-of select="fun:export-options(fun:domain-modify-uri($doc-id))"/>
			
			<xsl:copy-of select="fun:get-main-title(.)"/>
			
			
			<!-- regular properties -->
			<xsl:copy-of select="fun:block-presentation($this-about, 'LABEL', 'properties')"/>

			<!-- cube related properties -->
			<xsl:copy-of select="fun:block-presentation($this-about, 'MEASUREMENT', 'properties')"/>
			<xsl:copy-of select="fun:block-presentation($this-about, 'DIMENSION', 'properties')"/>

			<!-- regular properties -->
			<xsl:copy-of select="fun:block-presentation($this-about, 'VALUE', 'properties')"/>

			<!-- outgoing urls -->
			<xsl:copy-of select="fun:block-presentation($this-about, 'OBJECT', 'links outbound')"/>

			<!-- incoming urls -->					
			<xsl:if test="/descendant::rdf:Description[not(rdf:type)]/*[@rdf:resource = $this-about]">
				<h2 class="links-heading">Referenties van andere resources</h2>
				
				<div class="links inbound">
					<!-- loop through the unique predicates -->
					<xsl:for-each-group select="/descendant::rdf:Description[not(rdf:type)][not(@rdf:nodeID)]/*[@rdf:resource = $this-about]" group-by="name()">
						<div class="predicate">
							<xsl:copy-of select="fun:set-label(current-group())"/>
							<xsl:copy-of select="fun:set-outbound-content(current-group())"/>
							
						</div>							
					</xsl:for-each-group>
				</div>
			</xsl:if>
			
			<xsl:copy-of select="fun:block-presentation($this-about, 'DEFINED', 'properties')"/>
			
		</div>
	</xsl:template>
	
	<!-- met de bedoeling om per content type ev. te over rulen -->
	<xsl:function name="fun:get-main-title">
		<xsl:param name="context"/>
		<xsl:choose>
			<xsl:when test="normalize-space($context/dct:title)">
				<h1><xsl:value-of select="normalize-space($context/dct:title)"/></h1>
			</xsl:when>
			<xsl:when test="normalize-space(fun:get-prefered-language-variant($context/rdfs:label))">
				<h1><xsl:value-of select="fun:get-prefered-language-variant($context/rdfs:label)"/></h1>
			</xsl:when>
			<xsl:when test="normalize-space(fun:get-prefered-language-variant($context/skos:prefLabel))">
				<h1><xsl:value-of select="fun:get-prefered-language-variant($context/skos:prefLabel)"/></h1>
			</xsl:when>
		</xsl:choose>
	</xsl:function>
	
	
</xsl:stylesheet>
