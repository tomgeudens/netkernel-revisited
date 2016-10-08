<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:nk="http://1060.org"
	exclude-result-prefixes="rdf nk rdfs"
	version="1.0">
	
	<xsl:output 
    	method="html" 
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"/>

	<xsl:param name="replace" nk:class="java.lang.String" />
	<xsl:param name="with" nk:class="java.lang.String" />

	<!-- keys for muenchian method -->
	<xsl:key name="predicates-by-tag-incoming-links" match="rdf:RDF/rdf:Description[not(rdf:type)]/*" use="name()" />
	<xsl:key name="predicates-by-tag" match="rdf:RDF/rdf:Description[rdf:type]/*" use="name()" />

	<xsl:template match="rdf:RDF">
		<xsl:apply-templates select="rdf:Description[rdf:type]"/>
	</xsl:template>
	
	<xsl:template match="rdf:Description[rdf:type]">
		<xsl:variable name="idurl" select="@rdf:about"/>
		<xsl:variable name="modifiedidurl">
		<xsl:call-template name="replace-string">
			<xsl:with-param name="text" select="$idurl"/>
			<xsl:with-param name="replace" select="$replace"/>
			<xsl:with-param name="with" select="$with"/>															
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="docid">
			<xsl:value-of select="substring-before($idurl,'#')"/>
		</xsl:variable>
		<xsl:variable name="modifieddocid">
			<xsl:call-template name="replace-string">
				<xsl:with-param name="text" select="$docid"/>
				<xsl:with-param name="replace" select="$replace"/>
				<xsl:with-param name="with" select="$with"/>															
			</xsl:call-template>
		</xsl:variable>		
		<html>
			<head>
			    <title>Data Integraal Milieu Jaarverslag</title>
				<link rel="alternate" type="application/rdf+xml" href="{$modifieddocid}.rdf"/>
				<link rel="alternate" type="text/turtle" href="{$modifieddocid}.ttl"/>
				<link rel="alternate" type="text/plain" href="{$modifieddocid}.nt"/>
				<link rel="alternate" type="application/ld+json" href="{$modifieddocid}.jsonld"/>
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

				<div id="content">
					<div class="export-options">
						<a href="">HTML</a>
						<a href="{$modifieddocid}.jsonld">JSON-LD</a>
						<a href="{$modifieddocid}.ttl">TURTLE</a>
						<a href="{$modifieddocid}.nt">N-TRIPLES</a>
						<a href="{$modifieddocid}.rdf">XML</a>
					</div>
					
					<xsl:if test="rdfs:label">
						<h1><xsl:value-of select="rdfs:label"/></h1>
					</xsl:if>
					
					<!-- regular properties -->
					<div class="properties">
						<!-- loop through the unique predicates -->
						<xsl:for-each select="/descendant::rdf:Description[rdf:type]/*[not(@rdf:resource)][count(. | key('predicates-by-tag', name())[1]) = 1]">

							<div class="predicate">
								<a href="{namespace-uri()}{local-name()}" class="label"><xsl:value-of select="local-name()"/></a>
							
								<div class="objects">
									<!-- loop through the occurences for the unique predicates -->
									<xsl:for-each select="key('predicates-by-tag', name())">
										<p><xsl:value-of select="."/></p>
									</xsl:for-each>
								</div>
							</div>
						</xsl:for-each>
					</div>
					
					<!-- outgoing urls -->
					<div class="links outbound">
						<xsl:for-each select="/descendant::rdf:Description[rdf:type]/*[@rdf:resource][count(. | key('predicates-by-tag', name())[1]) = 1]">						
							<div class="predicate">
								<a href="{namespace-uri()}{local-name()}" class="label"><xsl:value-of select="local-name()"/></a>
							
								<div class="objects">
									<!-- loop through the occurences for the unique predicates -->									
									<xsl:for-each select="key('predicates-by-tag', name())">
										<xsl:variable name="resourceurl" select="@rdf:resource"/>
										<xsl:variable name="modifiedresourceurl">
											<xsl:call-template name="replace-string">
												<xsl:with-param name="text" select="$resourceurl"/>
												<xsl:with-param name="replace" select="$replace"/>
												<xsl:with-param name="with" select="$with"/>															
											</xsl:call-template>
										</xsl:variable>

										<p><a href="{$modifiedresourceurl}"><xsl:value-of select="$resourceurl"/></a></p>
									</xsl:for-each>
								</div>
							</div>						
						</xsl:for-each>
					</div>

					<!-- incoming urls -->					
					<xsl:if test="/descendant::rdf:Description[not(rdf:type)]/*[@rdf:resource=$idurl]">
						<h2 class="links-heading">Referenties van andere resources</h2>
						
						<div class="links inbound">
							<!-- loop through the unique predicates -->
							<xsl:for-each select="/descendant::rdf:Description[not(rdf:type)]/*[@rdf:resource=$idurl][count(. | key('predicates-by-tag-incoming-links', name())[1]) = 1]">
								<div class="predicate">
									<a href="{namespace-uri()}{local-name()}" class="label"><xsl:value-of select="local-name()"/></a>
									
									<div class="objects">
										<!-- loop through the occurences for the unique predicates -->
										<xsl:for-each select="key('predicates-by-tag-incoming-links', name())">
											<!-- we have to go a level up to find the inbound link -->
											<xsl:variable name="abouturl" select="../@rdf:about"/>
											<xsl:variable name="modifiedabouturl">
												<xsl:call-template name="replace-string">
													<xsl:with-param name="text" select="$abouturl"/>
													<xsl:with-param name="replace" select="$replace"/>
													<xsl:with-param name="with" select="$with"/>															
												</xsl:call-template>
											</xsl:variable>

											<p><a href="{$modifiedabouturl}"><xsl:value-of select="$abouturl"/></a></p>
										</xsl:for-each>
									</div>
								</div>							
							</xsl:for-each>
						</div>
					</xsl:if>
				</div>

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
	</xsl:template>

	<xsl:template name="replace-string">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="with"/>
		
		<xsl:choose>
			<xsl:when test="contains($text,$replace)">
				<xsl:value-of select="substring-before($text,$replace)"/>
				<xsl:value-of select="$with"/>
				
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="substring-after($text,$replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="with" select="$with"/>
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	
</xsl:stylesheet>
