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
	
	<xsl:variable name="definition-types" select="
		( 'http://www.w3.org/2002/07/owl#Class'
		, 'http://www.w3.org/2000/01/rdf-schema#Class'
		, 'http://www.w3.org/2002/07/owl#ObjectProperty'
		, 'http://www.w3.org/2002/07/owl#DatatypeProperty'	
		)"/>
	
	<xsl:template match="rdf:RDF">
		<xsl:copy-of select="fun:html-skin(., 'def')"/>
	</xsl:template>
	
	<xsl:function name="fun:get-type-specific-content">
		<xsl:param name="context"/>
		<table class="pure-table pure-table-bordered">
			<xsl:for-each-group select="$context/*[rdf:type/@rdf:resource = $definition-types]" group-by="rdf:type[@rdf:resource = $definition-types]/@rdf:resource">
				<xsl:sort select="index-of($definition-types, current-grouping-key())" order="ascending" data-type="number"/>
				<xsl:apply-templates select="current-group()" mode="def-nodes">
					<xsl:sort select="@rdf:about"/>
				</xsl:apply-templates>
			</xsl:for-each-group>
		</table>
	</xsl:function>
	
	
	<xsl:template match="*[@rdf:about]" mode="def-nodes">
		<tr>
			<th colspan="2" class="h2" id="{substring-after(@rdf:about,'#')}">
				<xsl:text>Identifier: </xsl:text>
				<a href="{fun:domain-modify-uri(@rdf:about)}">
					<xsl:value-of select="@rdf:about"/>
				</a>
			</th>
		</tr>
		<xsl:apply-templates select="*[not(@rdf:resource)]" mode="def-nodes-row"/>
		<xsl:apply-templates select="*[@rdf:resource]" mode="def-nodes-row"/>
	</xsl:template>
	
	<xsl:template match="*" mode="def-nodes-row">
		<tr>
			<th>
				<xsl:value-of select="local-name()"/>
			</th>
			<td>
				<xsl:apply-templates select="." mode="def-nodes-cell"/>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="*[not(@rdf:resource)]" mode="def-nodes-cell">
		<xsl:value-of select="."/>
	</xsl:template>
	
	<xsl:template match="*[@rdf:resource]" mode="def-nodes-cell">
		<a href="{fun:domain-modify-uri(@rdf:resource)}">
			<xsl:value-of select="@rdf:resource"/>
		</a>
	</xsl:template>
	
	
</xsl:stylesheet>
