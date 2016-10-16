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
	
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"

	exclude-result-prefixes="fun xs rdf purl rdfs skos"
	version="2.0">
	
	<xsl:import href="milieu-html-skin.xsl"/>
	<xsl:import href="milieu-common.xsl"/>
	<xsl:import href="milieu-blank-nodes.xsl"/>
	
	
	<!-- keys for muenchian method -->
	<xsl:key name="resource-by-about" match="rdf:Description" use="normalize-space(@rdf:about)"/>
	<xsl:key name="resource-by-node-id" match="rdf:Description" use="normalize-space(@rdf:nodeID)"/>
	
	<xsl:key name="predicates-by-tag-incoming-links" match="rdf:RDF/rdf:Description[not(rdf:type)]/*" use="name()" />
	
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
			
			<xsl:if test="purl:title">
				<h1><xsl:value-of select="purl:title"/></h1>
			</xsl:if>
			
			<!-- regular properties -->
			<div class="properties">
				<!-- loop through the unique predicates -->
				<xsl:for-each-group select="key('resource-by-about', $this-about)/*[not(@rdf:resource)]" group-by="name()">
					<div class="predicate">
						<xsl:copy-of select="fun:set-label(current-group())"/>
						<xsl:copy-of select="fun:set-content(current-group())"/>
					</div>
				</xsl:for-each-group>
			</div>
			
			<!-- outgoing urls -->
			<div class="links outbound">
				<!-- loop through the unique predicates -->
				<xsl:for-each-group select="key('resource-by-about', $this-about)/*[@rdf:resource]" group-by="name()">
					<div class="predicate">
						<xsl:copy-of select="fun:set-label(current-group())"/>
						<xsl:copy-of select="fun:set-content(current-group())"/>
					</div>						
				</xsl:for-each-group>
			</div>
			
			<!-- GBNOTE - REFACTORING TODO INCOMING -->
			<!-- incoming urls -->					
			<xsl:if test="/descendant::rdf:Description[not(rdf:type)]/*[@rdf:resource = $this-about]">
				<h2 class="links-heading">Referenties van andere resources</h2>
				
				<div class="links inbound">
					<!-- loop through the unique predicates -->
					<xsl:for-each select="/descendant::rdf:Description[not(rdf:type)]/*[@rdf:resource = $this-about][count(. | key('predicates-by-tag-incoming-links', name())[1]) = 1]">
						<div class="predicate">
							<a href="{namespace-uri()}{local-name()}" class="label"><xsl:value-of select="local-name()"/></a>
							
							<div class="objects">
								<!-- loop through the occurences for the unique predicates -->
								<xsl:for-each select="key('predicates-by-tag-incoming-links', name())">
									<!-- we have to go a level up to find the inbound link -->
									<xsl:variable name="about-url" select="../@rdf:about"/>
									<p>
										<a>
											<xsl:copy-of select="fun:set-domain-modified-href($about-url)"></xsl:copy-of>
											<xsl:value-of select="$about-url"/>
										</a>
									</p>
								</xsl:for-each>
							</div>
						</div>							
					</xsl:for-each>
				</div>
			</xsl:if>
		</div>
	</xsl:template>
	
	
</xsl:stylesheet>
