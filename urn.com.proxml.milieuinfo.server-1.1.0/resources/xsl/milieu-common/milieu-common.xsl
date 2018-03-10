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
	
	xmlns:milieu="https://id.milieuinfo.be/def#"
	
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:qudt="http://qudt.org/schema/qudt#"
	xmlns:blazegeo="http://www.proxml.be/blazegeo/wgs84_pos#"
	xmlns:sdmx-attribute="http://purl.org/linked-data/sdmx/2009/attribute#"
	
	exclude-result-prefixes="fun xs rdf rdfs skos dct foaf milieu geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<!-- COMMONS -->
	
	<xsl:function name="fun:process-label-fallback">
		<xsl:param name="fback" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="$fback = 'https://id.milieuinfo.be/def#'">
				<xsl:text>https://id.milieuinfo.be/def#</xsl:text>
			</xsl:when>
			<xsl:when test="contains($fback, 'id.milieuinfo.be/def#') and $fback != 'https://id.milieuinfo.be/def#'">
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
	
	<!-- overloaded for qudt mapping -->
	<xsl:function name="fun:domain-modify-uri" as="xs:string">
		<xsl:param name="uri" as="xs:string"/>
		<xsl:variable name="return">
			<xsl:choose>
				<xsl:when test="contains($uri, $replace)">
					<xsl:value-of select="substring-before($uri, $replace)"/>
					<xsl:value-of select="$with"/>
					<xsl:value-of select="substring-after($uri, $replace)"/>
				</xsl:when>
				<xsl:when test="contains($uri, 'http://qudt.org/vocab/unit#')">
					<xsl:value-of select="substring-before($uri, 'http://qudt.org/vocab/unit#')"/>
					<xsl:text>http://qudt.org/1.1/vocab/unit#</xsl:text>
					<xsl:value-of select="substring-after($uri, 'http://qudt.org/vocab/unit#')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$uri"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$return"/>
	</xsl:function>
	


	<xsl:function name="fun:domain-specific-homepage-title">
		<xsl:param name="dom"/>
		<h1>
			<xsl:choose>
				<xsl:when test="$dom = 'imjv'">
					<xsl:text>Het Integraal Milieu JaarVerslag (IMJV) Archief als Linked Data.</xsl:text>
				</xsl:when>
				<xsl:when test="$dom = 'cbb'">
					<xsl:text>Het Centraal Bedrijven Bestand (CBB) als Linked Data.</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>Departement Omgeving Linked Data</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</h1>
	</xsl:function>
	
	<xsl:function name="fun:domain-specific-config">
		<xsl:param name="dom"/>
		<xsl:choose>
			<xsl:when test="lower-case($dom) = 'imjv'">
				<script src="/js/milieuinfo-imjv-config.js">_</script>
				<script src="/js/milieuinfo-imjv-explorer.js">_</script>
			</xsl:when>
			<xsl:when test="lower-case($dom) = 'cbb'">
				<script src="/js/milieuinfo-cbb-config.js">_</script>
				<script src="/js/milieuinfo-cbb-explorer.js">_</script>
			</xsl:when>
		</xsl:choose>
	</xsl:function>
	
</xsl:stylesheet>
