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
 * Milieuinfo IMJV by ID Remainder with Extension Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVbyIDRemainderwithExtensionAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aExtension = null;
try {
	aExtension = aContext.source("arg:extension", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_WARNING, "MilieuinfoIMJVbyIDRemainderwithExtensionAccessor: invalid - extension - argument");
	// sensible default
	aExtension = "html"
}
//

// processing
INKFRequest imjvbyidremainderrequest = aContext.createRequest("active:milieuinfoimjvbyidremainder");
imjvbyidremainderrequest.addArgument("owner","arg:owner");
imjvbyidremainderrequest.addArgument("id","arg:id");
imjvbyidremainderrequest.addArgument("remainder","arg:remainder");

INKFResponseReadOnly imjvbyidremainderresponse = aContext.issueRequestForResponse(imjvbyidremainderrequest);
int vHTTPResponseCode = imjvbyidremainderresponse.getHeader("httpresponsecode");
Object vIMJVbyIDRemainderResult = imjvbyidremainderresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;

if (vHTTPResponseCode >= 400) {
	vResponse = aContext.createResponseFrom(vIMJVbyIDRemainderResult);
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	switch (aExtension) {
	case "rdf":
		INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
		rdfxml2rdfxmlrequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2rdfxmlrequest));
		vResponse.setMimeType("application/rdf+xml");
		break;
	case "ttl":
		INKFRequest rdfxml2turtlerequest = aContext.createRequest("active:rdfxml2turtle");
		rdfxml2turtlerequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2turtlerequest));
		vResponse.setMimeType("text/turtle");
		break;
	case "nt":
		INKFRequest rdfxml2ntriplerequest = aContext.createRequest("active:rdfxml2ntriple");
		rdfxml2ntriplerequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2ntriplerequest));
		vResponse.setMimeType("text/plain");
		break;
	case "jsonld":
		INKFRequest rdfxml2jsonldrequest = aContext.createRequest("active:rdfxml2jsonld");
		rdfxml2jsonldrequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2jsonldrequest));
		vResponse.setMimeType("application/ld+json");
		break;
	case "html":
		INKFRequest rdfxml2htmlsaxonrequest = aContext.createRequest("active:rdfxml2htmlsaxon");
		rdfxml2htmlsaxonrequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
		rdfxml2htmlsaxonrequest.addArgument("operator", "res:/resources/xsl/rdfxml2htmlimjv.xsl");
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2htmlsaxonrequest));
		vResponse.setMimeType("text/html");
		break;
	default:
		INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
		rdfxml2rdfxmlrequest.addArgumentByValue("operand", vIMJVbyIDRemainderResult);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVbyIDRemainderwithExtensionAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//