<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	
	<xsl:output
		method="text"
		encoding="UTF-8"
		media-type="text/plain"/>
    	
	<xsl:template match="sparql">
		<xsl:value-of select="boolean/text()"/>
	</xsl:template>
</xsl:stylesheet>