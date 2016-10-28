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
		<xsl:variable name="property-resource" select="key('resource-by-about', $namespace, $global-context)"/>
		<a class="label">
			<xsl:copy-of select="fun:set-domain-modified-href($namespace)"/>
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
		<xsl:choose>
			<xsl:when test="contains($fback, 'id.milieuinfo.be/def#')">
				<xsl:value-of select="substring-after($fback, 'id.milieuinfo.be/def#')"/>
			</xsl:when>
			<xsl:when test="contains($fback, 'id.milieuinfo.be/imjv/')">
				<xsl:value-of select="fun:strip-domain($fback, 'id.milieuinfo.be/imjv/')"/>
			</xsl:when>
			<xsl:when test="contains($fback, 'id.milieuinfo.be/')">
				<xsl:value-of select="fun:strip-domain($fback, 'id.milieuinfo.be/')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$fback"/>
			</xsl:otherwise>
		</xsl:choose>
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
			<xsl:variable name="unsorted-p-block" as="element(p)*">
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
			</xsl:variable>
			<xsl:for-each select="$unsorted-p-block">
				<xsl:sort select="normalize-space()" data-type="text" order="ascending"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</div>
	</xsl:function>
	
	<xsl:function name="fun:set-outbound-content">
		<xsl:param name="nodes" as="element()*"/>
		<div class="objects">
			<xsl:variable name="unsorted-p-block" as="element(p)*">
				<xsl:for-each select="$nodes[position() &lt;= $maximum-inccoming-per-property]">
					<xsl:variable name="that-about" select="../@rdf:about"/>
					<p>
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
			<xsl:if test="normalize-space($title)">
				<h2 class="links-heading"><xsl:value-of select="$title"/></h2>
			</xsl:if>
			<div class="{$class}">
				<!-- loop through the unique predicates -->
				<xsl:for-each-group select="key('resource-by-about', $about, $global-context)/*[fun:presentation-category(.) = $presentation-category]" group-by="name()">
					<xsl:sort data-type="number" order="ascending" select="if(self::rdfs:label | self::skos:prefLabel) then(1) else(2)"/>
					<div class="predicate">
						<xsl:copy-of select="fun:set-label(current-group())"/>
						<xsl:copy-of select="fun:set-content(current-group())"/>
					</div>
				</xsl:for-each-group>
			</div>
		</xsl:if>
	</xsl:function>
	
	<xsl:function name="fun:presentation-category">
		<xsl:param name="node" as="element()"/>
		<xsl:choose>
			<!-- don't show -->
			<xsl:when test="$node/self::blazegeo:lat_long">
				<xsl:text>NULL</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::skos:hiddenLabel">
				<xsl:text>NULL</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/self::geo:lat">
				<xsl:text>LOC</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::geo:long">
				<xsl:text>LOC</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::milieu:lambert72_y">
				<xsl:text>LOC</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::milieu:lambert72_x">
				<xsl:text>LOC</xsl:text>
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
			<xsl:when test="$node/self::*[fun:is-cube-property(., 'MeasureProperty')]">
				<xsl:text>MEASUREMENT</xsl:text>
			</xsl:when>
			<xsl:when test="$node/self::sdmx-attribute:unitMeasure">
				<xsl:text>MEASUREMENT</xsl:text>
			</xsl:when>
			<!--  -->
			<xsl:when test="$node/self::*[fun:is-cube-property(., 'DimensionProperty')]">
				<xsl:text>DIMENSION</xsl:text>
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
	
	<xsl:function name="fun:is-cube-property" as="xs:boolean">
		<xsl:param name="node" as="element()"/>
		<xsl:param name="prop" as="xs:string"/>
		<xsl:variable name="namespace" select="concat(namespace-uri($node),local-name($node))"/>
		<xsl:variable name="property-resource" select="key('resource-by-about', $namespace, $global-context)"/>
		<xsl:value-of select="exists($property-resource/rdf:type[@rdf:resource = concat('http://purl.org/linked-data/cube#', normalize-space($prop))])"/>
	</xsl:function>
	
	

</xsl:stylesheet>
