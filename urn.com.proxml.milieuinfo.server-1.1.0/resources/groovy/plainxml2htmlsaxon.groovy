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
 * Plain XML to HTML Saxon Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "PlainXML2HTMLSaxonAccessor: start of id - " + vId);
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
INKFRequest xslt2request = aContext.createRequest("active:xslt2");
xslt2request.addArgument("operand", "arg:operand");
xslt2request.addArgument("operator","arg:operator");
xslt2request.addArgument("domain", "arg:domain");
xslt2request.addArgumentByValue("replace", aReplace);
xslt2request.addArgumentByValue("with", aWith);

INKFRequest serializerequest = aContext.createRequest("active:saxonSerialize");
serializerequest.addArgumentByRequest("operand", xslt2request);
serializerequest.addArgumentByValue("operator", "<serialize><indent>yes</indent><omit-declaration>yes</omit-declaration><encoding>UTF-8</encoding><method>xhtml</method><mimeType>text/html</mimeType></serialize>");

//INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
//tagsouprequest.addArgumentByRequest("operand", serializerequest);
//tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(serializerequest));
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "PlainXML2HTMLSaxonAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
