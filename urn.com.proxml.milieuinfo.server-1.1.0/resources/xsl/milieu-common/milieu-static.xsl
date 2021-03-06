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
	
	<xsl:import href="../lod-core/core-static.xsl"/>
	
	<xsl:import href="milieu-html-skin.xsl"/>
	<xsl:import href="milieu-common.xsl"/>

	<xsl:function name="fun:static-home-page">
		<h1>Departement Leefmilieu Natuur en Energie - Linked Data.</h1>
		<p>Vanaf hier kan U naar:</p>
		<ul>
			<xsl:for-each select="$supported-domains/domain">
				<li>
					<a href="/{lower-case(normalize-space(@name))}/home">
						<xsl:value-of select="upper-case(normalize-space(@name))"/>
					</a>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:function>
	
</xsl:stylesheet>
