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

	<xsl:import href="core-html-skin.xsl"/>
	<xsl:import href="core-blank-nodes.xsl"/>
	
	<xsl:param name="replace" as="xs:string" select="'#rep'"/><!-- void default waarde om geen inf. loops te hebben-->
	<xsl:param name="with" as="xs:string" select="'#with'"/><!-- void default waarde om geen inf. loops te hebben-->

	<xsl:variable name="global-context" select="."/>
	
	<!-- keys for efficient node retrieval (merk op dat sommige resources gesplitst worden over meerdere rdf:Description -->
	<xsl:key name="resource-by-about" match="rdf:Description" use="normalize-space(@rdf:about)"/>
	<xsl:key name="resource-by-node-id" match="rdf:Description" use="normalize-space(@rdf:nodeID)"/>
	
	
	<!-- alternate file references -->
	
	<xsl:function name="fun:get-alternate-file-ref">
		<xsl:param name="doc-uri"></xsl:param>
		<xsl:variable name="doc-about" select="replace($doc-uri, '^([^#]+)#?.*$', '$1')"/>
		<xsl:value-of select="if(matches($doc-about, '^http')) then(fun:domain-modify-uri($doc-about)) else(concat('/', $doc-about))" />
	</xsl:function>
	
	<xsl:function name="fun:alternate-files" as="element(link)*">
		<xsl:param name="doc-uri"/>
		<xsl:variable name="doc-id" select="fun:get-alternate-file-ref($doc-uri)"/>
		<link rel="alternate" type="application/rdf+xml" href="{$doc-id}.rdf"/>
		<link rel="alternate" type="text/turtle" href="{$doc-id}.ttl"/>
		<link rel="alternate" type="text/plain" href="{$doc-id}.nt"/>
		<link rel="alternate" type="application/ld+json" href="{$doc-id}.jsonld"/>
	</xsl:function>
	
	<xsl:function name="fun:export-options" as="element(div)*">
		<xsl:param name="doc-uri"/>
		<xsl:variable name="doc-id" select="fun:get-alternate-file-ref($doc-uri)"/>
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
		<xsl:variable name="property-resource" select="key('resource-by-about', $namespace, $global-context)"/>
		<a about="{if(normalize-space($property-resource/@rdf:about)) then($property-resource/@rdf:about) else(fun:set-domain-modified-href($namespace))}" class="label" >
			<xsl:copy-of select="fun:set-domain-modified-href($namespace)"/>
			<xsl:value-of select="fun:set-property-label($property-resource, local-name($nodes[1]))"/>
		</a>
	</xsl:function>
	
	<xsl:function name="fun:set-orig-label">
		<xsl:param name="nodes" as="element()*"/>
		<xsl:variable name="namespace" select="concat(namespace-uri($nodes[1]),local-name($nodes[1]))"/>
		<xsl:variable name="property-resource" select="key('resource-by-about', $namespace, $global-context)"/>
		<a about="{$namespace}" class="label" href="{fun:set-domain-modified-href($namespace)}">
			<xsl:value-of select="fun:set-property-label($property-resource, local-name($nodes[1]))"/>
		</a>
	</xsl:function>
	
	<!-- hier kan er nog toegevoegd worden, momenteel gebruiken we enkel label en prefLabel -->
	<xsl:function name="fun:set-property-label">
		<xsl:param name="prop-resource"/>
		<xsl:param name="fall-back"/>
		<!-- omslachtige choose om markers te kunnen toevoegen en talen elegant te controleren-->
		<xsl:variable name="return">
			<xsl:choose>
				<xsl:when test="normalize-space(fun:get-prefered-language-variant($prop-resource/rdfs:label))">
					<xsl:value-of select="fun:get-prefered-language-variant($prop-resource/rdfs:label)"/>
				</xsl:when>
				<xsl:when test="normalize-space(fun:get-prefered-language-variant($prop-resource/skos:prefLabel))">
					<xsl:value-of select="fun:get-prefered-language-variant($prop-resource/skos:prefLabel)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="fun:process-label-fallback($fall-back)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="normalize-space($return)"/>
	</xsl:function>
	
	<xsl:function name="fun:process-label-fallback">
		<xsl:param name="fback" as="xs:string"/>
		<xsl:value-of select="$fback"/>
	</xsl:function>
	
	<xsl:function name="fun:strip-domain">
		<xsl:param name="uri"/>
		<xsl:param name="domain"/>
		<xsl:value-of select="replace(substring-after($uri, $domain), '^(.+?)(#id)?$', '$1')"/>
	</xsl:function>
	
	<xsl:function name="fun:get-prefered-language-variant" as="xs:string">
		<xsl:param name="label" as="element()*"/>
		<xsl:variable name="return">
			<xsl:sequence select="($label[not(@xml:lang)], $label[xml:lang = 'nl'], $label[matches(@xml:lang, '^en')], $label)[normalize-space()][1]"/>
		</xsl:variable>
		<xsl:value-of select="normalize-space($return)"/>
	</xsl:function>
	
	<xsl:function name="fun:set-content">
		<xsl:param name="nodes" as="element()*"/>
		<div class="objects">
			<xsl:variable name="unsorted-p-block" as="element(block)">
				<block>
					<xsl:for-each select="$nodes">
						<xsl:choose>
							<xsl:when test="@rdf:resource">
								<xsl:variable name="this-resource" select="normalize-space(@rdf:resource)"/>
								<p>
									<a>
										<xsl:copy-of select="fun:set-domain-modified-href($this-resource)"></xsl:copy-of>
										<xsl:value-of select="fun:set-property-label(key('resource-by-about', $this-resource), $this-resource)"/>
									</a>
								</p>
							</xsl:when>
							<xsl:when test="@rdf:nodeID">
								<xsl:variable name="this-node-id" select="normalize-space(@rdf:nodeID)"/>
								<xsl:apply-templates select="key('resource-by-node-id', $this-node-id)" mode="blank-nodes"/>
							</xsl:when>
							<xsl:otherwise>
								<p>
									<xsl:choose>
										<xsl:when test=". = 'false'"><span class="false"><xsl:value-of select="."/></span></xsl:when>
										<xsl:when test=". = 'true'"><span class="true"><xsl:value-of select="."/></span></xsl:when>
										<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
									</xsl:choose>
								</p>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</block>
			</xsl:variable>
			<xsl:for-each select="$unsorted-p-block/*[self::div | self::p[not(deep-equal(., preceding-sibling::p))]]">
				<xsl:sort select="normalize-space()" data-type="text" order="ascending"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:set-outbound-content">
		<xsl:param name="nodes" as="element()*"/>
		<div class="objects">
			<xsl:variable name="unsorted-p-block" as="element(p)*">
				<xsl:for-each select="$nodes">
					<xsl:variable name="that-about" select="../@rdf:about"/>
					<p about="{$that-about}">
						<a>
							<xsl:copy-of select="fun:set-domain-modified-href($that-about)"></xsl:copy-of>
							<xsl:value-of select="fun:set-property-label(key('resource-by-about', $that-about), $that-about)"/>
						</a>
					</p>
				</xsl:for-each>
			</xsl:variable>
			<xsl:for-each select="$unsorted-p-block">
				<xsl:sort select="normalize-space()" data-type="text" order="ascending"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:block-presentation">
		<xsl:param name="about"/>
		<xsl:param name="presentation-category"/>
		<xsl:param name="class"/>
		<xsl:param name="title"/>
		<xsl:if test="key('resource-by-about', $about, $global-context)/*[fun:presentation-category(.) = $presentation-category]">
			<xsl:copy-of select="fun:block-presentation-title($title, $presentation-category)"/>
			<div class="{$class}">
				<!-- loop through the unique predicates -->
				<xsl:for-each-group select="key('resource-by-about', $about, $global-context)/*[fun:presentation-category(.) = $presentation-category]" group-by="name()">
					<xsl:sort data-type="number" order="ascending" select="if(self::rdfs:label | self::skos:prefLabel) then(1) else(2)"/>
					<xsl:sort data-type="text" order="ascending" select="fun:set-label(current-group())//text()"/>
					<div class="predicate">
						<xsl:copy-of select="fun:set-label(current-group())"/>
						<xsl:copy-of select="fun:set-content(current-group())"/>
					</div>
				</xsl:for-each-group>
			</div>
		</xsl:if>
	</xsl:function>
	
	<xsl:function name="fun:block-presentation-title">
		<xsl:param name="title"/>
		<xsl:param name="presentation-category"/>
		<xsl:if test="normalize-space($title)">
			<h2>
				<xsl:attribute name="class">
					<xsl:text>links-heading</xsl:text>
					<xsl:if test="$presentation-category = ('DOC', 'OBJECT')">
						<xsl:text> collapsed</xsl:text>
					</xsl:if>
				</xsl:attribute>
				<xsl:value-of select="$title"/>
			</h2>
		</xsl:if>
	</xsl:function>
	
	<xsl:function name="fun:presentation-category">
		<xsl:param name="node" as="element()"/>
		<xsl:choose>
			<!-- don't show -->
			<xsl:when test="$node/self::skos:hiddenLabel">
				<xsl:text>NULL</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/self::rdfs:label">
				<xsl:text>LABEL</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::skos:prefLabel">
				<xsl:text>LABEL</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::rdf:type">
				<xsl:text>LABEL</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::dct:title">
				<xsl:text>LABEL</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::dct:identifier">
				<xsl:text>LABEL</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/self::rdfs:isDefinedBy">
				<xsl:text>DOC</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::foaf:page">
				<xsl:text>DOC</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/self::cube:dataSet">
				<xsl:text>DATASET</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::dct:isPartOf">
				<xsl:text>DATASET</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="not($node/@rdf:resource)">
				<xsl:text>VALUE</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/@rdf:resource">
				<xsl:text>OBJECT</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:otherwise>
				<xsl:text>UNMATCHED</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	

</xsl:stylesheet>
