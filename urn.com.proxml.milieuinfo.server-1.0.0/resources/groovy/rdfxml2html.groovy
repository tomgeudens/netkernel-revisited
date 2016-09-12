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
//

// processing
INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
rdfxml2rdfxmlrequest.addArgument("operand", "arg:operand");
Object vRDFXML = aContext.issueRequest(rdfxml2rdfxmlrequest);

INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
xsltcrequest.addArgumentByValue("operand", vRDFXML);
xsltcrequest.addArgument("operator","arg:operator");
xsltcrequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);

INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
tagsouprequest.addArgumentByRequest("operand", xsltcrequest);
tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(tagsouprequest));
vResponse.setMimeType("text/html");
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
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXML2HTMLAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
