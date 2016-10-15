<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	exclude-result-prefixes="rdf" 
	version="1.0">

	<xsl:output 
		method="html" 
		indent="no" 
		encoding="UTF-8"
		omit-xml-declaration="yes" 
		media-type="text/html" />

	<xsl:template match="rdf:RDF">
		<html>
			<body>
				<xsl:apply-templates select="rdf:Description" />
			</body>
		</html>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:for-each select="*[not(@rdf:resource)]">
			<h1><xsl:value-of select="." /></h1>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>