<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sp="http://www.w3.org/2005/sparql-results#"
	xmlns:nk="http://1060.org"
	exclude-result-prefixes="sp nk"
	version="1.0">
	
	<xsl:output 
    	method="html" 
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/html"/>
    	
    <xsl:param name="search" nk:class="java.lang.String" />

	<xsl:template match="sp:sparql">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<title>KBOData : Keyword Search Results</title>
				<style type="text/css">
					@import url(/css/pure-min.css);
   	    		</style>
			</head>
			<body>
				<h1>Keyword Search Results for "<xsl:value-of select="$search"/>"</h1>
				<table class="pure-table pure-table-bordered">
					<tr>
						<xsl:for-each select="sp:head/sp:variable">
							<th><xsl:value-of select="@name"/></th>
						</xsl:for-each>
					</tr>
					<xsl:for-each select="sp:results/sp:result">
						<xsl:variable name="current" select="."/>
						<tr>
							<xsl:for-each select="//sp:head/sp:variable">
								<xsl:variable name="name" select="@name"/>
								<td>
									<xsl:choose>
										<xsl:when test="$current/sp:binding[@name=$name]">
											<xsl:choose>
												<xsl:when test="$current/sp:binding[@name=$name]/sp:literal">
													<xsl:value-of select="$current/sp:binding[@name=$name]/sp:literal"/>
												</xsl:when>
												<xsl:otherwise>
													<a href="{$current/sp:binding[@name=$name]/sp:uri}"><xsl:value-of select="$current/sp:binding[@name=$name]/sp:uri"/></a>
												</xsl:otherwise>
											</xsl:choose>											
										</xsl:when>
										<xsl:otherwise>
											<!-- no value for the header -->
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