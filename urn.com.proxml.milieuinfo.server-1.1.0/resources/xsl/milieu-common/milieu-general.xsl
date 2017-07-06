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
	
	<xsl:import href="../lod-core/core-general.xsl"/>
	
	<xsl:import href="milieu-blank-nodes.xsl"/>
	<xsl:import href="milieu-html-skin.xsl"/>
	<xsl:import href="milieu-common.xsl"/>


	<!-- CONTENT -->
	
	<xsl:function name="fun:content-block-geo-map">
		<xsl:param name="this-about"/>
		<div class="geo-map"><em>loading map…</em></div>
	</xsl:function>
	

	<xsl:function name="fun:content-block-additional">
		<xsl:param name="this-about"/>
		
		<!-- cube related properties -->
		<xsl:copy-of select="fun:block-presentation($this-about, 'MEASUREMENT', 'properties', '')"/>
		<xsl:copy-of select="fun:block-presentation($this-about, 'DIMENSION', 'properties', '')"/>
		
		<!-- geo properties -->
		<xsl:copy-of select="fun:block-presentation($this-about, 'LOC', 'properties', '')"/>
		
	</xsl:function>
	
	<xsl:function name="fun:content-block-post-inc">
		<xsl:param name="this-about"/>
		<xsl:copy-of select="fun:block-presentation($this-about, 'DOC', 'properties', 'De beschrijvende documenten')"/>
		<xsl:copy-of select="fun:block-presentation($this-about, 'DATASET', 'properties', 'Dataset linken')"/>
		
		<xsl:variable name="doc-about" select="substring-before($this-about, '#id')"/>
		<xsl:copy-of select="fun:block-presentation($doc-about, 'OBJECT', 'properties', concat('Document &lt;', $doc-about, '&gt; eigenschappen'))"/>
	</xsl:function>
	
	
	<!-- COMMONS -->
	
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
