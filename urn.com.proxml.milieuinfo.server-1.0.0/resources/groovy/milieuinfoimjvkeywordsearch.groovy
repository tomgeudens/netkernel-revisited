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
 * Milieuinfo IMJV Keyword Search Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVKWSAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aWith = null;
if (vIsHTTPRequest) {
	try {
		javax.servlet.http.HttpServletRequest vURL = (javax.servlet.http.HttpServletRequest)aContext.source("httpRequest:/advanced/HttpServletRequest", javax.servlet.http.HttpServletRequest.class);
		aWith = vURL.getScheme() + "://" + vURL.serverName + ":" + vURL.serverPort.toString();
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
	aSearch = "supercalifragilisticexpialidocious";
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
	aLimit = "25"
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
INKFRequest kwsrequest = aContext.createRequest("active:keywordsearch");
kwsrequest.addArgument("database","milieuinfo:database-imjv");
kwsrequest.addArgument("expiry","milieuinfo:expiry-imjv");
kwsrequest.addArgument("credentials","milieuinfo:credentials-imjv");
if (aAccept.startsWith("text/html")) {
	kwsrequest.addArgumentByValue("accept", "application/sparql-results+xml");
}
else {
	kwsrequest.addArgumentByValue("accept", aAccept);
}
kwsrequest.addArgumentByValue("search", aSearch);
kwsrequest.addArgumentByValue("limit", aLimit);

INKFResponseReadOnly kwsresponse = aContext.issueRequestForResponse(kwsrequest);
int vHTTPResponseCode = kwsresponse.getHeader("httpresponsecode");
Object vKWSResult = kwsresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;
if (vHTTPResponseCode >= 400) {
	vResponse = aContext.createResponseFrom(vKWSResult);
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	if (aAccept.startsWith("text/html")) {
		INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
		xsltcrequest.addArgumentByValue("operand", vKWSResult);
		xsltcrequest.addArgumentByValue("search", aSearch);
		xsltcrequest.addArgument("replace","milieuinfo:baseurl");
		xsltcrequest.addArgumentByValue("with", aWith);
		xsltcrequest.addArgument("operator", "res:/resources/xsl/milieuinfoimjvkeywordsearch.xsl");
		xsltcrequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
		
		INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
		tagsouprequest.addArgumentByRequest("operand", xsltcrequest);
		tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
		
		vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(tagsouprequest));
		vResponse.setMimeType("text/html");
	}
	else {
		vResponse = aContext.createResponseFrom(vKWSResult);
		vResponse.setMimeType(aAccept);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVKWSAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//

