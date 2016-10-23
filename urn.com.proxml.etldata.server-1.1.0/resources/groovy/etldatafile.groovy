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
 * ETL Data File Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "ETLDataFileAccessor: start of id - " + vId);
//

// arguments
String aFileResource = null;
try {
	aFileResource = aContext.source("arg:fileresource", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "ETLDataFileAccessor: invalid - owner - argument");
	throw new Exception("ETLDataFileAccessor: no valid - owner - argument");
}

String aMimeType = null;
switch (aFileResource.substring(aFileResource.lastIndexOf(".") + 1)) {
	case "json":
		aMimeType = "application/json";
		break;
	case "rdf":
		aMimeType = "application/rdf+xml";
		break;
	case "ttl":
		aMimeType = "text/turtle";
		break;
	case "nt":
		aMimeType = "text/plain";
		break;
	case "jsonld":
		aMimeType = "application/ld+json";
		break;
	default:
		aMimeType = "text/plain";
		break;
}
//

// processing
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.sourceForResponse(aFileResource));
vResponse.setMimeType(aMimeType);
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "ETLDataFileAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//