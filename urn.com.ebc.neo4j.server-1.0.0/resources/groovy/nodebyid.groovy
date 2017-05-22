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
 * Node By ID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeByIDAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aLabel = null;
try {
	aLabel = aContext.source("arg:label", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "NodeByIDAccessor: ("
		+ vId
		+ ") - no valid label argument");
	throw new Exception("NodeByIDAccessor: no valid - label - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeByIDAccessor: ("
	+ vId
	+ ") - argument label : "
	+ aLabel);

String aID = null;
try {
	aID = aContext.source("arg:id", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "NodeByIDAccessor: ("
		+ vId
		+ ") - no valid id argument");
	throw new Exception("NodeByIDAccessor: no valid - id - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeByIDAccessor: ("
	+ vId
	+ ") - argument id : "
	+ aID);
//

// processing
INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
freemarkerrequest.addArgument("operator", "res:/resources/freemarker/cypher_by_id.freemarker");
freemarkerrequest.addArgumentByValue("label", aLabel);
freemarkerrequest.addArgumentByValue("id", aID);
Object vFreemarkerResult = aContext.issueRequest(freemarkerrequest);

INKFRequest rowsrequest = aContext.createRequest("active:rows");
rowsrequest.addArgument("databaseurl", "neo4j:databaseurl");
rowsrequest.addArgument("databaseuser", "neo4j:databaseuser");
rowsrequest.addArgument("databasepassword", "neo4j:databasepassword");
rowsrequest.addArgumentByValue("expiry", 10000L);
rowsrequest.addArgumentByValue("cypher", vFreemarkerResult);
Object vRowsResult = aContext.issueRequest(rowsrequest);
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vRowsResult);
vResponse.setMimeType("text/plain");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "NodeByIDAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//