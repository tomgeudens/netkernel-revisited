<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output
		method="text"
		encoding="UTF-8"
		media-type="text/plain"/>
	
	<xsl:template match="/results">
		<xsl:for-each select="row/label">
			<xsl:value-of select="."/>
			
			<xsl:if test="position() != last()">
				<xsl:text>|</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>