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
 * Milieuinfo Generic SPARQL Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericSPARQLAccessor: ("
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
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoGenericSPARQLAccessor: ("
		+ vId
		+ ") - argument domain : invalid");
	throw new Exception("MilieuinfoGenericSPARQLAccessor: no valid - domain - argument");
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
Pattern vConstructPattern = Pattern.compile("(?i)\\bCONSTRUCT\\b");
Matcher vConstructMatcher = vConstructPattern.matcher(aQuery);
Boolean vConstructFound = vConstructMatcher.find();
Pattern vDescribePattern = Pattern.compile("(?i)\\bDESCRIBE\\b");
Matcher vDescribeMatcher = vDescribePattern.matcher(aQuery);
Boolean vDescribeFound = vDescribeMatcher.find();
Pattern vAskPattern = Pattern.compile("(?i)\\bASK\\b");
Matcher vAskMatcher = vAskPattern.matcher(aQuery);
Boolean vAskFound = vAskMatcher.find();
if (aAccept == null || aAccept == "") {
	if (vConstructFound || vDescribeFound) {
		// sensible default
		aAccept = "application/rdf+xml";
	}
	else if (vAskFound) {
		// sensible default, should be text/boolean ?
		aAccept = "application/sparql-results+xml";
	}
	else {
		// sensible default
		aAccept = "text/html";
	}
}
//

// processing
INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
sparqlrequest.addArgument("database","milieuinfo:database-" + vDatabase);
sparqlrequest.addArgument("expiry","milieuinfo:expiry-" + vDatabase);
sparqlrequest.addArgument("credentials","milieuinfo:credentials-" + vDatabase);
sparqlrequest.addArgumentByValue("query",aQuery);
if (aAccept.startsWith("text/html")) {
	if (vConstructFound || vDescribeFound) {
		sparqlrequest.addArgumentByValue("accept","application/rdf+xml");
	}
	else if (vAskFound) {
		// should be text/boolean ?
		sparqlrequest.addArgumentByValue("accept","application/sparql-results+xml");
	}
	else {
		sparqlrequest.addArgumentByValue("accept","application/sparql-results+xml");
	}
}
else {
	sparqlrequest.addArgumentByValue("accept",aAccept);
}

INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
int vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
Object vSPARQLResult = sparqlresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;
String vMimeType = null;

if (aAccept.startsWith("text/html")) {
	if (vConstructFound || vDescribeFound) {
		vMimeType = "application/rdf+xml";
	}
	else if (vAskFound) {
		// should be text/boolean ?
		vMimeType = "application/sparql-results+xml";
	}
	else {
		vMimeType = "text/html";
	}
}
else {
	vMimeType = aAccept;
}

if (vHTTPResponseCode >= 400) {
	vResponse = aContext.createResponseFrom(vSPARQLResult);
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	if (aAccept.startsWith("text/html")) {
		if (vConstructFound || vDescribeFound || vAskFound) {
			vResponse = aContext.createResponseFrom(vSPARQLResult);
		}
		else {
			INKFRequest xslt2request = aContext.createRequest("active:xslt2");
			xslt2request.addArgumentByValue("operand", vSPARQLResult);
			xslt2request.addArgument("operator", "res:/resources/xsl/sparqlgeneric.xsl");
			xslt2request.addArgument("replace","milieuinfo:baseurl");
			xslt2request.addArgumentByValue("domain", aDomain);
			xslt2request.addArgumentByValue("with", aWith);
	
			INKFRequest serializerequest = aContext.createRequest("active:saxonSerialize");
			serializerequest.addArgumentByRequest("operand", xslt2request);
			serializerequest.addArgumentByValue("operator", "<serialize><indent>yes</indent><omit-declaration>yes</omit-declaration><encoding>UTF-8</encoding><method>xhtml</method><mimeType>text/html</mimeType></serialize>");
			
			INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
			tagsouprequest.addArgumentByRequest("operand", serializerequest);
			tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
			
			vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(tagsouprequest));
		}
	}
	else {
		vResponse = aContext.createResponseFrom(vSPARQLResult);
	}
	vResponse.setMimeType(vMimeType);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoGenericSPARQLAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//
