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
 * Parse Text Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "ParseTextAccessor: start of id - " + vId);
//

// arguments
//
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aReplace = null;
if (aContext.getThisRequest().argumentExists("replace")) {// replace is an optional argument
	try {
		aReplace = (String)aContext.source("arg:replace", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	try {
		aReplace = (String)aContext.source("milieuinfo:baseurl", String.class);
	}
	catch (Exception e) {
		//
	}
}

String aWith = null;
if (aContext.getThisRequest().argumentExists("with")) {// with is an optional argument
	try {
		aWith = (String)aContext.source("arg:with", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	try {
		aWith = (String)aContext.source("milieuinfo:activeurl", String.class);
	}
	catch (Exception e) {
		//
	}
}
if (aWith == null || aWith == "") {
	// sensible default
	aWith = "http://localhost:8080";
}

// processing
String vResult = null;
if (aReplace == null || aReplace == "") {
	// nothing to replace
	vResult = (String)aContext.source("arg:operand", String.class);	
}
else {
	INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
	freemarkerrequest.addArgument("operator", "res:/resources/freemarker/sed.freemarker");
	freemarkerrequest.addArgumentByValue("replace", aReplace);
	freemarkerrequest.addArgumentByValue("with", aWith);
	freemarkerrequest.setRepresentationClass(String.class);
	
	INKFRequest sedrequest = aContext.createRequest("active:sed");
	sedrequest.addArgument("operand","arg:operand");
	sedrequest.addArgumentByRequest("operator", freemarkerrequest);
	sedrequest.setRepresentationClass(String.class);
	vResult = ((String)aContext.issueRequest(sedrequest)).trim();
}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vResult);
vResponse.setMimeType("text/plain");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "ParseTextscriptAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
