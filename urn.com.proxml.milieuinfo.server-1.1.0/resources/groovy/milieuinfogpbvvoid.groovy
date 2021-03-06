/**
 *
 * ProXML
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
 * Milieuinfo GPBV VOID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGPBVVOIDAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");
//

// processing
INKFRequest incacherequest = aContext.createRequest("pds:/dataset/gpbv");
incacherequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
incacherequest.setRepresentationClass(Boolean.class);
Boolean vInCache = (Boolean)aContext.issueRequest(incacherequest);

int vHTTPResponseCode = 0;
Object vJenaSerializeResult;

if (vInCache) {
	vJenaSerializeResult = aContext.source("pds:/dataset/gpbv");
	vHTTPResponseCode = 200;
}
else {
	INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
	sparqlrequest.addArgument("database","milieuinfo:database-gpbv");
	sparqlrequest.addArgument("expiry", "milieuinfo:expiry-gpbv");
	sparqlrequest.addArgument("credentials","milieuinfo:credentials-gpbv");
	sparqlrequest.addArgument("query", "res:/resources/sparql/milieuinfogpbvvoid.sparql");
	sparqlrequest.addArgumentByValue("accept","application/rdf+xml");

	INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
	vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
	
	String vGPBVBlazegraphURL = aContext.source("milieuinfo:database-gpbv", String.class);
	INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
	xsltcrequest.addArgument("operand", vGPBVBlazegraphURL);
	xsltcrequest.addArgument("operator","res:/resources/xsl/blazegraphgpbv.xsl");
	
	INKFRequest jenaupdaterequest = aContext.createRequest("active:jRDFUpdateModel");
	jenaupdaterequest.addArgumentByValue("operand", sparqlresponse.getRepresentation());
	jenaupdaterequest.addArgumentByRequest("operator", xsltcrequest);
	
	INKFRequest jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
	jenaserializerequest.addArgumentByRequest("operand", jenaupdaterequest);
	vJenaSerializeResult = aContext.issueRequest(jenaserializerequest);
	
	if (vHTTPResponseCode == 200) {
		//aContext.sink("pds:/dataset/gpbv", vJenaSerializeResult);
	}
}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vJenaSerializeResult);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGPBVVOIDAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//