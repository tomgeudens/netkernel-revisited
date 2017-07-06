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
 * Milieuinfo CBB by ID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aOwner = null;
try {
	aOwner = (String)aContext.source("arg:owner", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - argument owner : invalid");
	throw new Exception("MilieuinfoCBBbyIDAccessor: no valid - owner - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
	+ vId
	+ ") - argument owner : "
	+ aOwner);

String aID = null;
try {
	aID = (String)aContext.source("arg:id", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - argument id : invalid");
	throw new Exception("MilieuinfoCBBbyIDAccessor: no valid - id - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
	+ vId
	+ ") - argument id : "
	+ aID);
//

String aLoggedInHeader = null;
try {
	aLoggedInHeader = (String)aContext.source("milieuinfo:loggedinheader", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - resource milieuinfo:loggedinheader : missing");
	throw new Exception("MilieuinfoCBBbyIDAccessor: no valid - milieuinfo:loggedinheader - resource");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
	+ vId
	+ ") - argument loggedinheader : "
	+ aLoggedInHeader);

String aHeader = null;
try {
	aHeader = (String)aContext.source("httpRequest:/header/" + aLoggedInHeader, String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - httpheader " + aLoggedInHeader + " : missing");
	throw new Exception("MilieuinfoCBBbyIDAccessor: no valid - httpRequest:/header/" + aLoggedInHeader + " - resource");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
	+ vId
	+ ") - header " + aLoggedInHeader  + " : "
	+ aHeader);
//

// processing
INKFRequest incacherequest = aContext.createRequest("pds:/" +aOwner + "/" + aID);
incacherequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
incacherequest.setRepresentationClass(Boolean.class);
Boolean vInCache = (Boolean)aContext.issueRequest(incacherequest);

int vHTTPResponseCode = 0;
Object vSPARQLResult;
Boolean vASKResult = false;

// two paths - logged in or not logged in
if (aHeader == null) {
	// not logged in
	INKFRequest freemarkeraskrequest = aContext.createRequest("active:freemarker");
	freemarkeraskrequest.addArgument("operator", "res:/resources/freemarker/askprivaatcbbbyid.freemarker");
	freemarkeraskrequest.addArgumentByValue("owner", aOwner);
	freemarkeraskrequest.addArgumentByValue("id", aID);
	freemarkeraskrequest.setRepresentationClass(String.class);
	String vASKQuery = (String)aContext.issueRequest(freemarkeraskrequest);
	
	INKFRequest sparqlaskrequest = aContext.createRequest("active:sparql");
	sparqlaskrequest.addArgument("database","milieuinfo:database-cbb");
	sparqlaskrequest.addArgument("expiry", "milieuinfo:expiry-cbb");
	sparqlaskrequest.addArgument("credentials","milieuinfo:credentials-cbb");
	sparqlaskrequest.addArgumentByValue("query", vASKQuery);
	sparqlaskrequest.addArgumentByValue("accept","application/sparql-results+xml");
	vSPARQLResult = aContext.issueRequest(sparqlaskrequest);

	INKFRequest rdfxml2booleanrequest = aContext.createRequest("active:rdfxml2boolean");
	rdfxml2booleanrequest.addArgumentByValue("operand", vSPARQLResult);
	vASKResult = (Boolean)aContext.issueRequest(rdfxml2booleanrequest);
}

if ( (aHeader != null) || (aHeader == null && (!vASKResult)) ) {
	// logged in or not restricted
	if (vInCache) {
		vSPARQLResult = aContext.source("pds:/" +aOwner + "/" + aID);
		vHTTPResponseCode = 200;
	}
	else {
		INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
		freemarkerrequest.addArgument("operator", "res:/resources/freemarker/constructcbbbyid.freemarker");
		freemarkerrequest.addArgumentByValue("owner", aOwner);
		freemarkerrequest.addArgumentByValue("id", aID);
		freemarkerrequest.setRepresentationClass(String.class);
		String vQuery = (String)aContext.issueRequest(freemarkerrequest);
		
		INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
		sparqlrequest.addArgument("database","milieuinfo:database-cbb");
		sparqlrequest.addArgument("expiry", "milieuinfo:expiry-cbb");
		sparqlrequest.addArgument("credentials","milieuinfo:credentials-cbb");
		sparqlrequest.addArgumentByValue("query", vQuery);
		sparqlrequest.addArgumentByValue("accept","application/rdf+xml");
	
		INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
		vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
		vSPARQLResult = sparqlresponse.getRepresentation();
		
		if (vHTTPResponseCode == 200) {
			//aContext.sink("pds:/" +aOwner + "/" + aID, vSPARQLResult);
		}
	}
}
else {
	// restricted
	vSPARQLResult = aContext.source("res:/resources/rdf/privaat.rdf");
	vHTTPResponseCode = 200; // force 200
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBbyIDAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//