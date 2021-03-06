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
import org.netkernel.layer0.representation.*;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * RDFXML to HTML Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXML2HTMLAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aWith = null;
try {
	aWith = (String)aContext.source("milieuinfo:activeurl", String.class);
}
catch (Exception e) {
	//
}
if (aWith == null || aWith == "") {
	// sensible default
	aWith = "http://localhost:8080";
}

String aReplace = null;
try {
	aReplace = (String)aContext.source("milieuinfo:baseurl", String.class);
}
catch (Exception e) {
	//
}
if (aReplace == null || aReplace == "") {
	// sensible default
	aReplace = "http://localhost:8080";
}
//

// processing
INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
rdfxml2rdfxmlrequest.addArgument("operand", "arg:operand");

INKFResponseReadOnly rdfxml2rdfxmlresponse = aContext.issueRequestForResponse(rdfxml2rdfxmlrequest);
Boolean vIsModelEmpty = rdfxml2rdfxmlresponse.getHeader("empty");
Object vRDFXML = rdfxml2rdfxmlresponse.getRepresentation();

INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
if (vIsModelEmpty) {
	xsltcrequest.addArgument("operand", "res:/resources/rdf/empty.rdf");
}
else {
	xsltcrequest.addArgumentByValue("operand", vRDFXML);
}
xsltcrequest.addArgument("operator","arg:operator");
xsltcrequest.addArgumentByValue("replace",aReplace);
xsltcrequest.addArgumentByValue("with", aWith);
xsltcrequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);

INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
tagsouprequest.addArgumentByRequest("operand", xsltcrequest);
tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(tagsouprequest));
vResponse.setMimeType("text/html");

if (vIsHTTPRequest) {
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXML2HTMLAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
