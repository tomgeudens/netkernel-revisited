/**
 *
 * Elephant Bird Consulting
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
 * Fedstats Nace by ID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "FedstatsNacebyIDAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aOwner = null;
try {
	aOwner = aContext.source("arg:owner", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "FedstatsNacebyIDAccessor: invalid - owner - argument");
	throw new Exception("FedstatsNacebyIDAccessor: no valid - owner - argument");
}

String aID = null;
try {
	aID = aContext.source("arg:id", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "FedstatsNacebyIDAccessor: invalid - id - argument");
	throw new Exception("FedstatsNacebyIDAccessor: no valid - id - argument");
}
//

// processing
INKFRequest incacherequest = aContext.createRequest("pds:/" +aOwner + "/" + aID);
incacherequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
incacherequest.setRepresentationClass(Boolean.class);
Boolean vInCache = (Boolean)aContext.issueRequest(incacherequest);

int vHTTPResponseCode = 0;
Object vSPARQLResult

if (vInCache) {
	vSPARQLResult = aContext.source("pds:/" +aOwner + "/" + aID);
	vHTTPResponseCode = 200;
}
else {
	INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
	freemarkerrequest.addArgument("operator", "res:/resources/freemarker/constructnacebyid.freemarker");
	freemarkerrequest.addArgumentByValue("owner", aOwner);
	freemarkerrequest.addArgumentByValue("id", aID);
	freemarkerrequest.setRepresentationClass(String.class);
	String vQuery = (String)aContext.issueRequest(freemarkerrequest);
	
	INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
	sparqlrequest.addArgument("database","fedstats:database-nace");
	sparqlrequest.addArgument("expiry", "fedstats:expiry-nace");
	sparqlrequest.addArgument("credentials","fedstats:credentials-nace");
	sparqlrequest.addArgumentByValue("query", vQuery);
	sparqlrequest.addArgumentByValue("accept","application/rdf+xml");

	INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
	vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
	vSPARQLResult = sparqlresponse.getRepresentation();
	
	if (vHTTPResponseCode == 200) {
		aContext.sink("pds:/" +aOwner + "/" + aID, vSPARQLResult);
	}
}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vSPARQLResult);
vResponse.setHeader("httpresponsecode", vHTTPResponseCode);
if (vHTTPResponseCode >= 400) {
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	vResponse.setMimeType("text/xml");
}

if (vIsHTTPRequest) {
	// pass the code on
	vResponse.setHeader("httpResponse:/code", vHTTPResponseCode);

	String vCORSOrigin = null;
	try {
		vCORSOrigin = aContext.source("httpRequest:/header/Origin", String.class);
	}
	catch (Exception e){
		//
	}
	if (vCORSOrigin != null) {
		// No CORS verification yet, I just allow everything
		vResponse.setHeader("httpResponse:/header/Access-Control-Allow-Origin","*");
	}
}
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "FedstatsNacebyIDAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//