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
	
	xmlns:sp="http://www.w3.org/2005/sparql-results#"

	exclude-result-prefixes="sp fun xs rdf rdfs skos dct foaf geo blazegeo sdmx-attribute cube"
	version="2.0">
	
	<xsl:import href="core-common.xsl"/>
	
	<!-- voor sparql results => geen alternatieve bestanden -->
	<xsl:function name="fun:alternate-files">
		<xsl:param name="doc-uri"/>
	</xsl:function>
	
	<!-- voor sparql results => geen alternatieve bestanden -->
	<xsl:function name="fun:export-options">
		<xsl:param name="doc-uri"/>
	</xsl:function>
	
	<xsl:function name="fun:get-main-title">
		<xsl:param name="context"/>
		<h1>SPARQL zoekopdracht resultaten</h1>
	</xsl:function>
	
	
	<xsl:template match="sp:sparql">
		<xsl:copy-of select="fun:html-skin(., 'sparql')"/>
	</xsl:template>
	
	<xsl:function name="fun:get-type-specific-content">
		<xsl:param name="context"/>
		<xsl:apply-templates select="$context" mode="sparql-result-nodes">
			<xsl:sort select="@rdf:about"/>
		</xsl:apply-templates>
	</xsl:function>
	
	
	<xsl:template match="sp:sparql" mode="sparql-result-nodes">
		<xsl:variable name="variable-names" select="sp:head/sp:variable/@name"/>
		<table>
			<thead>
				<tr>
					<xsl:for-each select="$variable-names">
						<th><xsl:value-of select="."/></th>
					</xsl:for-each>
				</tr>
			</thead>
			<tbody>
				<xsl:apply-templates select="sp:results/sp:result" mode="sparql-result-nodes">
					<xsl:with-param name="variable-names" select="$variable-names" tunnel="yes"/>
				</xsl:apply-templates>
			</tbody>
		</table>
	</xsl:template>
	
	<xsl:template match="sp:result" mode="sparql-result-nodes">
		<xsl:param name="variable-names" tunnel="yes"/>
		<xsl:variable name="this-result" select="."/>
		<tr>
			<xsl:for-each select="$variable-names">
				<xsl:variable name="var-name" select="."/>
				<td>
					<xsl:choose>
						<xsl:when test="$this-result/sp:binding[@name=$var-name]/sp:literal">
							<xsl:value-of select="$this-result/sp:binding[@name=$var-name]/sp:literal"/>
						</xsl:when>
						<xsl:when test="$this-result/sp:binding[@name=$var-name]/sp:uri">
							<xsl:variable name="url" select="$this-result/sp:binding[@name=$var-name]/sp:uri"/>
							<a href="{fun:domain-modify-uri($url)}"><xsl:value-of select="$url"/></a>
						</xsl:when>
						<xsl:otherwise>
							<!-- no value for the header -->
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>
	
</xsl:stylesheet>
