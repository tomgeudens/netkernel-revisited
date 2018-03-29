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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Milieuinfo Generic Data Accessor
 *
 * Is very similar to the SPARQL Accessor but the return formats are not RDF.
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericDataAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aDomain = null;
try {
	aDomain = aContext.source("arg:domain", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoGenericDataAccessor: ("
		+ vId
		+ ") - argument domain : invalid");
	throw new Exception("MilieuinfoGenericDataAccessor: no valid - domain - argument");
}
INKFRequest getdatabaserequest = aContext.createRequest("active:getdatabase");
getdatabaserequest.addArgumentByValue("domain", aDomain);
getdatabaserequest.setRepresentationClass(String.class);
String vDatabase = (String) aContext.issueRequest(getdatabaserequest);

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

String aQuery = null;
if (vIsHTTPRequest) {
	try {
		aQuery = (String)aContext.source("httpRequest:/param/query", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("query")) {// query is an optional argument
		try {
			aQuery = (String)aContext.source("arg:query", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aQuery == null || aQuery == "") {
	try {
		aQuery = (String)aContext.source("milieuinfo:query-" + vDatabase, String.class);
	}
	catch (Exception e) {
		// sensible default
		aQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .} LIMIT 5";
	}
}

Pattern vInjectionPattern = Pattern.compile("(?i)\\bINSERT\\b|\\bDELETE\\b|\\bLOAD\\b|\\bCLEAR\\b|\\bCREATE\\b|\\bDROP\\b|\\bCOPY\\b|\\bMOVE\b|\\bADD\\b");
Matcher vInjectionMatcher = vInjectionPattern.matcher(aQuery);
Boolean vInjectionFound = vInjectionMatcher.find();
if (vInjectionFound) { // we don't allow updates through the SPARQL endpoint
	// sensible default
	aQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .} LIMIT 5";
}

String aFormat = null;
if (vIsHTTPRequest) {
	try {
		aFormat = (String)aContext.source("httpRequest:/param/format", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("format")) {// format is an optional argument
		try {
			aFormat = (String)aContext.source("arg:format", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aFormat == null || aFormat == "") {
	// sensible default
	aFormat = "csv";
}


String aAccept = "text/csv"; // hard coded for the moment
//

// processing
INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
sparqlrequest.addArgument("database","milieuinfo:database-" + vDatabase);
sparqlrequest.addArgument("expiry","milieuinfo:expiry-" + vDatabase);
sparqlrequest.addArgument("credentials","milieuinfo:credentials-" + vDatabase);
sparqlrequest.addArgumentByValue("query",aQuery);
sparqlrequest.addArgumentByValue("accept",aAccept);

INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
int vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
Object vSPARQLResult = sparqlresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;
String vMimeType = null;

if (vHTTPResponseCode >= 400) {
	vResponse = aContext.createResponseFrom(vSPARQLResult);
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	vResponse = aContext.createResponseFrom(vSPARQLResult);
	vResponse.setMimeType(aAccept);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericDataAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//
