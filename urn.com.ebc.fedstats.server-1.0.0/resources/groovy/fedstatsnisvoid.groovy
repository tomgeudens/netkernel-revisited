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
 * Fedstats NIS VOID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "FedstatsNISVOIDAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");
//

// processing
INKFRequest incacherequest = aContext.createRequest("pds:/dataset/nis");
incacherequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
incacherequest.setRepresentationClass(Boolean.class);
Boolean vInCache = (Boolean)aContext.issueRequest(incacherequest);

int vHTTPResponseCode = 0;
Object vSPARQLResult

if (vInCache) {
	vSPARQLResult = aContext.source("pds:/dataset/nis");
	vHTTPResponseCode = 200;
}
else {
	INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
	sparqlrequest.addArgument("database","fedstats:database-nis");
	sparqlrequest.addArgument("expiry", "fedstats:expiry-nis");
	sparqlrequest.addArgument("credentials","fedstats:credentials-nis");
	sparqlrequest.addArgument("query", "res:/resources/sparql/fedstatsnisvoid.sparql");
	sparqlrequest.addArgumentByValue("accept","application/rdf+xml");

	INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
	vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
	vSPARQLResult = sparqlresponse.getRepresentation();
	
	if (vHTTPResponseCode == 200) {
		aContext.sink("pds:/dataset/nis", vSPARQLResult);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "FedstatsNISVOIDAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//