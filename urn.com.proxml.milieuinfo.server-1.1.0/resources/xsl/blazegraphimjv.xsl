<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	exclude-result-prefixes="rdf"
	version="1.0">
	
	<xsl:output 
    	method="xml" 
    	indent="yes"
    	encoding="UTF-8"
    	omit-xml-declaration="yes"
    	media-type="text/xml"/>
    	
	<xsl:template match="rdf:RDF">
		<jRDFUpdateModel>
			<addTriple>
				<resource>http://id.milieuinfo.be/dataset/imjv#id</resource>
				<property>
					<namespace>http://rdfs.org/ns/void#</namespace>
					<name>triples</name>
				</property>
				<value type="xs:long"><xsl:value-of select="rdf:Description[@rdf:nodeID='defaultGraph' and rdf:type/@rdf:resource='http://www.w3.org/ns/sparql-service-description#Graph']/*[local-name()='triples']/text()"/></value>
			</addTriple>
			<addTriple>
				<resource>http://id.milieuinfo.be/dataset/imjv#id</resource>
				<property>
					<namespace>http://rdfs.org/ns/void#</namespace>
					<name>entities</name>
				</property>
				<value type="xs:long"><xsl:value-of select="rdf:Description[@rdf:nodeID='defaultGraph' and rdf:type/@rdf:resource='http://www.w3.org/ns/sparql-service-description#Graph']/*[local-name()='entities']/text()"/></value>
			</addTriple>
			<addTriple>
				<resource>http://id.milieuinfo.be/dataset/imjv#id</resource>
				<property>
					<namespace>http://rdfs.org/ns/void#</namespace>
					<name>properties</name>
				</property>
				<value type="xs:long"><xsl:value-of select="rdf:Description[@rdf:nodeID='defaultGraph' and rdf:type/@rdf:resource='http://www.w3.org/ns/sparql-service-description#Graph']/*[local-name()='properties']/text()"/></value>
			</addTriple>
			<addTriple>
				<resource>http://id.milieuinfo.be/dataset/imjv#id</resource>
				<property>
					<namespace>http://rdfs.org/ns/void#</namespace>
					<name>classes</name>
				</property>
				<value type="xs:long"><xsl:value-of select="rdf:Description[@rdf:nodeID='defaultGraph' and rdf:type/@rdf:resource='http://www.w3.org/ns/sparql-service-description#Graph']/*[local-name()='classes']/text()"/></value>
			</addTriple>
		</jRDFUpdateModel>
	</xsl:template>
</xsl:stylesheet>