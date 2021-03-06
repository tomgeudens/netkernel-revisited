<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:nk="http://1060.org"
	exclude-result-prefixes="nk">
	
	<xsl:output
		method="text"
		encoding="UTF-8"
		media-type="text/plain"/>

	<xsl:param name="service" nk:class="java.lang.String"/>
	<xsl:param name="region" nk:class="java.lang.String"/>
	
	<xsl:template match="/regions">
	<xsl:if test="region[systemname=$region]/services/service[@name=$service]">
		<xsl:value-of select="region[systemname=$region]/services/service[@name=$service]"/>
	</xsl:if>
	<xsl:if test="not(region[systemname=$region]/services/service[@name=$service])">
		<xsl:text>unknown</xsl:text>
	</xsl:if>	
	</xsl:template>
</xsl:stylesheet>