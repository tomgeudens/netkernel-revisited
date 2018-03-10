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
	
	
	<xsl:template match="/">
		<xsl:variable name="start-point-resource" as="element(null)"><null/></xsl:variable>
		<xsl:copy-of select="fun:html-skin($start-point-resource, '#NULL')"></xsl:copy-of>
	</xsl:template>
	
	<xsl:function name="fun:get-type-specific-content">
		<xsl:param name="context"/>
		<xsl:apply-templates select="$context" mode="starting-point"/>
	</xsl:function>
	
	<!-- starting point content	-->
	
	<xsl:function name="fun:alternate-files" as="element(link)*">
		<xsl:param name="doc-uri"/>
	</xsl:function>
	
	<xsl:function name="fun:export-options" as="element(div)*">
		<xsl:param name="doc-uri"/>
	</xsl:function>
	
	<xsl:function name="fun:static-home-page">
		<h1>Static Page Content Here</h1>
	</xsl:function>
	
	<xsl:template match="null" mode="starting-point">
		<xsl:choose>
			<xsl:when test="$this-domain/self::domain">
				
				<xsl:copy-of select="fun:domain-specific-homepage-title($domain)"/>
				
				<h2>Gepubliceerde datasets</h2>
				<p>U vindt hier:</p>

				<ul>
					<li><a href="/dataset/{lower-case($domain)}#id">
						<xsl:text>Dataset </xsl:text>
						<xsl:value-of select="upper-case($domain)"/>
					</a>
					</li>
				</ul>
				
				<h2>Exploreer de dataset</h2>

				<div id="explorer">
					<xsl:for-each select="('lookup', 'sparql', 'kws')">
						<xsl:copy-of select="fun:create-exploration-block($this-domain, .)"/>
					</xsl:for-each>
				</div>
				
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:copy-of select="fun:static-home-page()"/>
			</xsl:otherwise>
			
		</xsl:choose>
	</xsl:template>
	
	
	
	<xsl:function name="fun:create-exploration-block">
		<xsl:param name="domain" as="element(domain)"/>
		<xsl:param name="type" as="xs:string"/>
		<xsl:if test="$domain/@*[name() = $type] = 1">
			<div data-api="{$type}">
				<xsl:attribute name="class" select="if($type = 'lookup') then('api active') else('api')"/>
				<xsl:choose>
					<xsl:when test="$type = 'lookup'">
						<h4>Opzoeken op basis van identifier</h4>
					</xsl:when>
					<xsl:when test="$type = 'sparql'">
						<h4>Opzoeken met een SPARQL zoekopdracht</h4>
					</xsl:when>
					<xsl:when test="$type = 'kws'">
						<h4>Opzoeken met een sleutelwoord</h4>
					</xsl:when>
				</xsl:choose>
				<div class="form-area">
					<form action="#" method="GET">
						<xsl:choose>
							<xsl:when test="$type = 'lookup'">
								<input name="identifier" type="text" placeholder="Voer een identifier in ..." />
							</xsl:when>
							<xsl:when test="$type = 'sparql'">
								<textarea name="query" placeholder="Voer een SPARQL zoekopdracht in ..."></textarea>
							</xsl:when>
							<xsl:when test="$type = 'kws'">
								<input name="keyword" type="text" placeholder="Voer een sleutelwoord in ..." />
							</xsl:when>
						</xsl:choose>
						
						<select class="samples" size="1">
							<option value="">Voorbeelden ...</option>
						</select>
						
						<div class="buttons">
							<button type="submit">Zoekopdracht uitvoeren</button>
							<button type="reset">Herladen</button>
						</div>
					</form>
				</div>
			</div>
		</xsl:if>
	</xsl:function>
	
		
	
	

</xsl:stylesheet>
