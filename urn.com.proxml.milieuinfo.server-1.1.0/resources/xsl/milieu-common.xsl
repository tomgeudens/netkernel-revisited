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
	
	<xsl:param name="replace" as="xs:string" select="'#rep'"/><!-- void default waarde om geen inf. loops te hebben-->
	<xsl:param name="with" as="xs:string" select="'#with'"/><!-- void default waarde om geen inf. loops te hebben-->
	
	<!--
	- door de manier waarop we de data klaarzetten heeft ieder startpunt een rdf:type
	- JENA zorgt er voor dat enkel de niet blanke nodes een @rdf:about hebben
	(blank nodes krijgen een @rdf:nodeID)
	- verder andere discriminatoren voor de edge cases
	-->
	<xsl:function name="fun:is-starting-point" as="xs:boolean">
		<xsl:param name="resource" as="element(rdf:Description)"/>
		<xsl:variable name="this-about" select="$resource/@rdf:about"/>
		<xsl:choose>
			<xsl:when test="not(exists($resource/@rdf:about) and exists($resource/rdf:type))">
				<xsl:value-of select="false()"/>
			</xsl:when>
			<xsl:when test="count($resource/ancestor::rdf:RDF/rdf:Description[@rdf:about][rdf:type]) = 1">
				<xsl:value-of select="true()"/>
			</xsl:when>
			<xsl:when test="$resource/rdfs:isDefinedBy and count($resource/ancestor::rdf:RDF/rdf:Description[@rdf:about][rdf:type][rdfs:isDefinedBy]) = 1">
				<xsl:value-of select="true()"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="false()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	
	<xsl:function name="fun:export-options" as="element(div)">
		<xsl:param name="doc-id"/>
		<div class="export-options">
			<a href="">HTML</a>
			<a href="{$doc-id}.jsonld">JSON-LD</a>
			<a href="{$doc-id}.ttl">TURTLE</a>
			<a href="{$doc-id}.nt">N-TRIPLES</a>
			<a href="{$doc-id}.rdf">XML</a>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:set-domain-modified-href" as="attribute(href)">
		<xsl:param name="uri" as="xs:string"/>
		<xsl:attribute name="href" select="fun:domain-modify-uri($uri)"/>
	</xsl:function>
	
	<xsl:function name="fun:domain-modify-uri" as="xs:string">
		<xsl:param name="uri" as="xs:string"/>
		<xsl:variable name="return">
			<xsl:choose>
				<xsl:when test="contains($uri, $replace)">
					<xsl:value-of select="substring-before($uri, $replace)"/>
					<xsl:value-of select="$with"/>
					<xsl:value-of select="substring-after($uri, $replace)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$uri"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$return"/>
	</xsl:function>
	
	
	<xsl:function name="fun:set-label">
		<xsl:param name="nodes" as="element()*"/>
		<xsl:variable name="namespace" select="concat(namespace-uri($nodes[1]),local-name($nodes[1]))"/>
		<xsl:variable name="property-resource" select="key('resource-by-about', $namespace, $nodes[1])"/>
		<a href="{$namespace}" class="label">
			<xsl:value-of select="fun:set-property-label($property-resource, local-name($nodes[1]))"/>
		</a>
	</xsl:function>
	
	<!-- hier kan er nog toegevoegd worden, momenteel gebruiken we enkel label en prefLabel -->
	<xsl:function name="fun:set-property-label">
		<xsl:param name="prop-resource"/>
		<xsl:param name="fall-back"/>
		<xsl:sequence select="
			(
			$prop-resource/rdfs:label, 
			$prop-resource/skos:prefLabel, 
			$fall-back
			)[normalize-space()][1]"/>
	</xsl:function>
	
	<xsl:function name="fun:set-content">
		<xsl:param name="nodes" as="element()*"/>
		<div class="objects">
			<!-- loop through the occurences for the unique predicates -->
			<xsl:for-each select="$nodes">
				<p>
					<xsl:choose>
						<xsl:when test="@rdf:resource">
							<xsl:variable name="this-resource" select="normalize-space(@rdf:resource)"/>
							<a>
								<xsl:copy-of select="fun:set-domain-modified-href($this-resource)"></xsl:copy-of>
								<xsl:value-of select="fun:set-property-label(key('resource-by-about', $this-resource), $this-resource)"/>
							</a>
						</xsl:when>
						<xsl:when test="@rdf:nodeID">
							<xsl:variable name="this-node-id" select="normalize-space(@rdf:nodeID)"/>
							<xsl:apply-templates select="key('resource-by-node-id', $this-node-id)" mode="blank-nodes"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</p>
			</xsl:for-each>
		</div>

	</xsl:function>
	

</xsl:stylesheet>