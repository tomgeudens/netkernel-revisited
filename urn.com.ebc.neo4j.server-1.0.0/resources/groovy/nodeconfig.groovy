/**
 *
 * Elephant Bird Consulting BVBA
 *
 * @author tomgeudens
 *
 */

/**
 * Accessor Imports
 */
import org.netkernel.layer0.nkf.*;
//import org.netkernel.layer0.representation.*;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

import org.netkernel.mod.hds.*;

/**
 * Node Configuration Resource
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeConfigurationResource: ("
		+ vId
		+ ") - start");
//
INKFRequest rowslabelsrequest = aContext.createRequest("active:rows");
rowslabelsrequest.addArgument("databaseurl", "neo4j:databaseurl");
rowslabelsrequest.addArgument("databaseuser", "neo4j:databaseuser");
rowslabelsrequest.addArgument("databasepassword", "neo4j:databasepassword");
rowslabelsrequest.addArgumentByValue("expiry", 3600000L);
rowslabelsrequest.addArgument("cypher", "res:/resources/cypher/get_labels.cypher");
// Object vRowsLabelsResult = aContext.issueRequest(rowslabelsrequest);

INKFRequest rowsschemarequest = aContext.createRequest("active:rows");
rowsschemarequest.addArgument("databaseurl", "neo4j:databaseurl");
rowsschemarequest.addArgument("databaseuser", "neo4j:databaseuser");
rowsschemarequest.addArgument("databasepassword", "neo4j:databasepassword");
rowsschemarequest.addArgumentByValue("expiry", 3600000L);
rowsschemarequest.addArgument("cypher", "res:/resources/cypher/get_schema.cypher");
//Object vRowsSchemaResult = aContext.issueRequest(rowsschemarequest);

INKFRequest xsltclabelsrequest = aContext.createRequest("active:xsltc");
xsltclabelsrequest.addArgumentByRequest("operand",rowslabelsrequest);
xsltclabelsrequest.addArgument("operator","res:/resources/xsl/parselabels.xsl");
xsltclabelsrequest.setRepresentationClass(String.class);
// String vXSLTCLabelsResult = (String)aContext.issueRequest(xsltlabelscrequest);

INKFRequest xsltcschemarequest = aContext.createRequest("active:xsltc");
xsltcschemarequest.addArgumentByRequest("operand",rowsschemarequest);
xsltcschemarequest.addArgument("operator","res:/resources/xsl/parseschema.xsl");
xsltcschemarequest.setRepresentationClass(IHDSDocument.class);
IHDSDocument vXSLTCSchemaResult = (IHDSDocument)aContext.issueRequest(xsltcschemarequest);

IHDSReader vReader = vXSLTCSchemaResult.getReader();
String vUniquePropertyEndpoints = "";
for (IHDSReader vConstraint : vReader.getNodes("/results/uniqueconstraint")) {
	INKFRequest parseuniqueconstraintrequest = aContext.createRequest("active:parseuniqueconstraint");
	parseuniqueconstraintrequest.addArgumentByValue("constrainttext",vConstraint.getFirstValue("."));
	parseuniqueconstraintrequest.setRepresentationClass(String.class);
	
	String vParseUniqueConstraintResult = (String)aContext.issueRequest(parseuniqueconstraintrequest);
	vUniquePropertyEndpoints = vUniquePropertyEndpoints + vParseUniqueConstraintResult;
}
	
INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
freemarkerrequest.addArgument("operator", "res:/resources/freemarker/nodeconfig.freemarker");
freemarkerrequest.addArgumentByRequest("labels", xsltclabelsrequest);
freemarkerrequest.addArgumentByValue("uniquepropertyendpoints", vUniquePropertyEndpoints);
Object vFreemarkerResult = aContext.issueRequest(freemarkerrequest);

// response
INKFResponse vResponse = aContext.createResponseFrom(vFreemarkerResult);
vResponse.setMimeType("text/xml");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeConfigurationResource: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//