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
 * KBOData KeywordSearch Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataKeywordSearcAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aSearch = null;
if (vIsHTTPRequest) {
	try {
		aSearch = (String)aContext.source("httpRequest:/param/search", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("search")) {// search is an optional argument
		try {
			aSearch = (String)aContext.source("arg:search", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aSearch == null || aSearch == "") {
	// sensible default
	aSubject = "supercalifragilisticexpialidocious";
}

String aLimit = null;
if (vIsHTTPRequest) {
	try {
		aLimit = (String)aContext.source("httpRequest:/param/limit", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("limit")) {// limit is an optional argument
		try {
			aLimit = (String)aContext.source("arg:limit", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aLimit == null || aLimit == "") {
	// sensible default
	aLimit = "10"
}

String aAccept = null;
if (vIsHTTPRequest) {
	try {
		aAccept = (String)aContext.source("httpRequest:/header/Accept", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("accept")) {// accept is an optional argument
		try {
			aAccept = (String)aContext.source("arg:accept", String.class);
		}
		catch (Exception e) {
			//
		}
	}
}
if (aAccept == null || aAccept == "") {
	// sensible default
	aAccept = "text/html";
}
//

// processing
//

// response
INKFResponse vResponse = aContext.createResponseFrom("to do");
vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataKeywordSearcAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//