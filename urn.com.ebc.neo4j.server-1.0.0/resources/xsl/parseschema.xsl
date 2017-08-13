<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output
		method="xml"
		encoding="UTF-8"
		media-type="text/xml"/>
	
	<xsl:template match="/results">
		<results>
			<xsl:for-each select="//constraints">
				<xsl:for-each select="node()">
					<xsl:if test="contains(.,'UNIQUE')">
						<uniqueconstraint>
							<xsl:value-of select="."/>
						</uniqueconstraint>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
		</results>
	</xsl:template>
</xsl:stylesheet>