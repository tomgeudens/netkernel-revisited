<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sp="http://www.w3.org/2005/sparql-results#"
	exclude-result-prefixes="sp"
	version="1.0">
	
	<xsl:output 
    	method="html" 
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"/>
    
	<xsl:template match="sp:sparql">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<title>Milieuinfo CBB : SPARQL Query Results</title>
				<style type="text/css">
					@import url(/css/pure-min.css);
   	    		</style>
			</head>
			<body>
				<h1>SPARQL Query Results</h1>
				<table class="pure-table pure-table-bordered">
					<tr>
						<xsl:for-each select="sp:head/sp:variable">
							<th><xsl:value-of select="@name"/></th>
						</xsl:for-each>
					</tr>
					<xsl:for-each select="sp:results/sp:result">
						<tr>
							<xsl:for-each select="sp:binding">
								<td>
									<xsl:choose>
										<xsl:when test="sp:literal">
											<xsl:value-of select="sp:literal"/>
										</xsl:when>
										<xsl:otherwise>
											<a href="{sp:uri}"><xsl:value-of select="sp:uri"/></a>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</xsl:for-each>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>