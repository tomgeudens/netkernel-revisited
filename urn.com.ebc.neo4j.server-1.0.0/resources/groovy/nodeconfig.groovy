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
INKFRequest rowsrequest = aContext.createRequest("active:rows");
rowsrequest.addArgument("databaseurl", "neo4j:databaseurl");
rowsrequest.addArgument("databaseuser", "neo4j:databaseuser");
rowsrequest.addArgument("databasepassword", "neo4j:databasepassword");
rowsrequest.addArgumentByValue("expiry", 3600000L);
rowsrequest.addArgument("cypher", "res:/resources/cypher/get_labels.cypher");
// Object vRowsResult = aContext.issueRequest(rowsrequest);

INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
xsltcrequest.addArgumentByRequest("operand",rowsrequest);
xsltcrequest.addArgument("operator","res:/resources/xsl/parselabels.xsl");
xsltcrequest.setRepresentationClass(String.class);
// String vXSLTCResult = (String)aContext.issueRequest(xsltcrequest);
	
INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
freemarkerrequest.addArgument("operator", "res:/resources/freemarker/nodeconfig.freemarker");
freemarkerrequest.addArgumentByRequest("labels", xsltcrequest);
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