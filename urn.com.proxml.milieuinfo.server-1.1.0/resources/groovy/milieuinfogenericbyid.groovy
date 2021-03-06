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
 * Milieuinfo Generic by ID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aOwner = null;
try {
	aOwner = aContext.source("arg:owner", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoGenericbyIDAccessor: ("
		+ vId
		+ ") - argument owner : invalid");
	throw new Exception("MilieuinfoGenericbyIDAccessor: no valid - owner - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDAccessor: ("
	+ vId
	+ ") - argument owner : "
	+ aOwner);

String aID = null;
try {
	aID = aContext.source("arg:id", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoGenericbyIDAccessor: ("
		+ vId
		+ ") - argument id : invalid");
	throw new Exception("MilieuinfoGenericbyIDAccessor: no valid - id - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDAccessor: ("
	+ vId
	+ ") - argument id : "
	+ aID);
//

// processing
String vExtension = "rdf";
if (vIsHTTPRequest) {
	String vAccept = aContext.source("httpRequest:/accept/preferred",String.class);
	switch (vAccept) {
	case "text/turtle":
		vExtension = "ttl";
		break;
	case "text/plain":
		vExtension = "nt";
		break;
	case "application/ld+json":
		vExtension = "jsonld";
		break;
	case "application/json":
		vExtension = "jsonld";
		break;
	case "text/html":
		vExtension = "html";
		break;
	case "text/xml":
		vExtension = "rdf";
		break;
	case "application/xml":
		vExtension = "rdf";
		break;
	case "application/rdf+xml":
		vExtension = "rdf";
		break;
	default:
		vExtension = "rdf";
		break;
	}
}


INKFRequest genericbyidwithextensionrequest = aContext.createRequest("active:milieuinfogenericbyidwithextension");
genericbyidwithextensionrequest.addArgument("owner","arg:owner");
genericbyidwithextensionrequest.addArgument("id","arg:id");
genericbyidwithextensionrequest.addArgumentByValue("extension",vExtension);
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(genericbyidwithextensionrequest));

if (vIsHTTPRequest) {
	// pass the code on
	vResponse.setHeader("httpResponse:/code", 200);

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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericbyIDAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//