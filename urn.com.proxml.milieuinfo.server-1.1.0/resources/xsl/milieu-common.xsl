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
	
	<xsl:function name="fun:replace-string"> <!-- wrapper -->
		<xsl:param name="text" as="xs:string"/>
		<xsl:value-of select="string-join(fun:__replace-string($text), '')"/>
	</xsl:function>
		
	
	<xsl:function name="fun:__replace-string">
		<xsl:param name="text" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text, $replace)"/>
				<xsl:value-of select="$with"/>
				<xsl:value-of select="fun:__replace-string(substring-after($text, $replace))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
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
							<a href="{fun:replace-string($this-resource)}">
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
