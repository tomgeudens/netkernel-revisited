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
 * Milieuinfo Generic by ID with Extension Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDwithExtensionAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aExtension = null;
try {
	aExtension = aContext.source("arg:extension", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_WARNING, "MilieuinfoGenericbyIDwithExtensionAccessor: ("
		+ vId
		+ ") - argument extension : invalid");
	// sensible default
	aExtension = "html"
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDwithExtensionAccessor: ("
	+ vId
	+ ") - argument extension : "
	+ aExtension);
//

// processing
INKFRequest genericbyidrequest = aContext.createRequest("active:milieuinfogenericbyid");
genericbyidrequest.addArgument("owner","arg:owner");
genericbyidrequest.addArgument("id","arg:id");

INKFResponseReadOnly genericbyidresponse = aContext.issueRequestForResponse(genericbyidrequest);
int vHTTPResponseCode = genericbyidresponse.getHeader("httpresponsecode");
Object vGenericbyIDResult = genericbyidresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;

if (vHTTPResponseCode >= 400) {
	vResponse = aContext.createResponseFrom(vGenericbyIDResult);
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	switch (aExtension) {
	case "rdf":
		INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
		rdfxml2rdfxmlrequest.addArgumentByValue("operand", vGenericbyIDResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2rdfxmlrequest));
		vResponse.setMimeType("application/rdf+xml");
		break;
	case "ttl":
		INKFRequest rdfxml2turtlerequest = aContext.createRequest("active:rdfxml2turtle");
		rdfxml2turtlerequest.addArgumentByValue("operand", vGenericbyIDResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2turtlerequest));
		vResponse.setMimeType("text/turtle");
		break;
	case "nt":
		INKFRequest rdfxml2ntriplerequest = aContext.createRequest("active:rdfxml2ntriple");
		rdfxml2ntriplerequest.addArgumentByValue("operand", vGenericbyIDResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2ntriplerequest));
		vResponse.setMimeType("text/plain");
		break;
	case "jsonld":
		INKFRequest rdfxml2jsonldrequest = aContext.createRequest("active:rdfxml2jsonld");
		rdfxml2jsonldrequest.addArgumentByValue("operand", vGenericbyIDResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2jsonldrequest));
		vResponse.setMimeType("application/ld+json");
		break;
	case "html":
		INKFRequest rdfxml2htmlsaxonrequest = aContext.createRequest("active:rdfxml2htmlsaxon");
		rdfxml2htmlsaxonrequest.addArgumentByValue("operand", vGenericbyIDResult);
		rdfxml2htmlsaxonrequest.addArgument("operator", "res:/resources/xsl/rdfxml2htmlgeneric.xsl");
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2htmlsaxonrequest));
		vResponse.setMimeType("text/html");
		break;
	default:
		INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
		rdfxml2rdfxmlrequest.addArgumentByValue("operand", vGenericbyIDResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2rdfxmlrequest));
		vResponse.setMimeType("application/rdf+xml");
		break;
	}
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDwithExtensionAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//