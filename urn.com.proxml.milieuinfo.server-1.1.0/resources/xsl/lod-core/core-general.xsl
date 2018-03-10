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
	
	exclude-result-prefixes="fun xs rdf rdfs skos dct foaf geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<xsl:import href="core-common.xsl"/>
	
	
	<xsl:template match="rdf:RDF">
		<xsl:variable name="start-point-resource" select="rdf:Description[fun:is-starting-point(.)]"/>
		<xsl:copy-of select="fun:html-skin($start-point-resource, $start-point-resource/@rdf:about)"></xsl:copy-of>
	</xsl:template>
	
	<xsl:function name="fun:get-type-specific-content">
		<xsl:param name="context"/>
		<xsl:apply-templates select="$context" mode="starting-point"/>
	</xsl:function>
	
	<!-- starting point content	-->
	
	<xsl:template match="rdf:Description" mode="starting-point">
		<xsl:variable name="this-about" select="@rdf:about"/>
		<xsl:variable name="doc-id" select="substring-before($this-about,'#')"/>
		
			
			<xsl:copy-of select="fun:content-block-geo-map(($this-about))"/>
			
			<!-- part of the rendering to be shown before the incoming urls -->
			<xsl:copy-of select="fun:content-block-pre-inc($this-about)"/>
			
			<!-- incoming urls -->					
			<xsl:if test="/descendant::rdf:Description[not(rdf:type)]/*[not(self::foaf:primaryTopic)][@rdf:resource = $this-about]">
				<h2 class="links-heading">Inkomende relaties</h2>
				
				<div class="links inbound">
					<!-- loop through the unique predicates -->
					<xsl:for-each-group select="/descendant::rdf:Description[not(rdf:type)]/*[not(self::foaf:primaryTopic)][@rdf:resource = $this-about]" group-by="name()">
						<div class="predicate">
							<xsl:copy-of select="fun:set-orig-label(current-group())"/>
							<div class="objects">
								<xsl:text>...</xsl:text>
							</div>
						</div>							
					</xsl:for-each-group>
				</div>
			</xsl:if>
			
			<!-- part of the rendering to be shown after the incoming urls -->
			<xsl:copy-of select="fun:content-block-post-inc($this-about)"/>
			
	</xsl:template>
	
	<!-- met de bedoeling om te overschrijven in importer -->
	<xsl:function name="fun:content-block-geo-map">
		<xsl:param name="this-about"/>
	</xsl:function>
	
	<!-- met de bedoeling om te overschrijven in importer -->
	<xsl:function name="fun:content-block-pre-inc">
		<xsl:param name="this-about"/>
		<!-- regular properties -->
		<xsl:copy-of select="fun:block-presentation($this-about, 'LABEL', 'properties', '')"/>
		
		<xsl:copy-of select="fun:content-block-additional($this-about)"/>
		
		<!-- regular properties -->
		<xsl:copy-of select="fun:block-presentation($this-about, 'VALUE', 'properties', '')"/>
		
		<!-- outgoing urls -->
		<xsl:copy-of select="fun:block-presentation($this-about, 'OBJECT', 'links outbound', '')"/>
		
	</xsl:function>
	
	<xsl:function name="fun:content-block-additional">
		<xsl:param name="this-about"/>
	</xsl:function>
	
	<!-- met de bedoeling om te overschrijven in importer -->
	<xsl:function name="fun:content-block-post-inc">
		<xsl:param name="this-about"/>
		<xsl:copy-of select="fun:block-presentation($this-about, 'DOC', 'properties', 'Document linken')"/>
		<xsl:copy-of select="fun:block-presentation($this-about, 'DATASET', 'properties', 'Link naar kubus')"/>
	</xsl:function>
	
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
	
	
	

</xsl:stylesheet>
