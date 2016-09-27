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
    	
    <xsl:param name="replace" nk:class="java.lang.String" />
    <xsl:param name="with" nk:class="java.lang.String" />
    
	<xsl:template match="sp:sparql">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
			<head>
				<title>Milieuinfo CBB : SPARQL Query Results</title>
				<style type="text/css">
					@import url(/css/documentation.css);
					@import url(/css/explorer.css);
					@import url(/css/page.css);
					@import url(/css/print.css);
					@import url(/css/resource.css);
					@import url(/css/responsive.css);
   	    		</style>
			</head>
			<body>
				<div id="header">
					<div id="logo">
						<a href="/"><span>LNE</span></a>
					</div>
				</div>

				<div id="content">
					<h1>SPARQL Query Results</h1>
					<table>
						<thead>
							<tr>
								<xsl:for-each select="sp:head/sp:variable">
									<th><xsl:value-of select="@name"/></th>
								</xsl:for-each>
							</tr>
						</thead>
						<tbody>
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
															<xsl:variable name="url" select="$current/sp:binding[@name=$name]/sp:uri"/>
															<xsl:variable name="modifiedurl">
																<xsl:call-template name="replace-string">
																	<xsl:with-param name="text" select="$url"/>
																	<xsl:with-param name="replace" select="$replace"/>
																	<xsl:with-param name="with" select="$with"/>															
																</xsl:call-template>
															</xsl:variable>
															<a href="{$modifiedurl}"><xsl:value-of select="$current/sp:binding[@name=$name]/sp:uri"/></a>
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
						</tbody>
					</table>
				</div>
				
				<div id="footer">
					<div>
						<div class="logo">
							<span class="title">Vlaanderen</span>
							<span class="claim">verbeelding werkt</span>
						</div>
				
						<div class="site-info">
							<h3>Dit is een officiële website van de Vlaamse overheid</h3>
							<span>uitgegeven door het <a href="https://www.lne.be/">Departement Leefmilieu, Natuur en Energie (LNE)</a></span>
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="replace-string">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="with"/>
		
		<xsl:choose>
			<xsl:when test="contains($text,$replace)">
				<xsl:value-of select="substring-before($text,$replace)"/>
				<xsl:value-of select="$with"/>
				
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="substring-after($text,$replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="with" select="$with"/>
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	
</xsl:stylesheet>