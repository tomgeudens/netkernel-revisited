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
 * Milieuinfo CBB SPARQL Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBSPARQLAccessor: start of id - " + vId);
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
		aQuery = (String)aContext.source("milieuinfo:query-cbb", String.class);
	}
	catch (Exception e) {
		// sensible default
		aQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .} LIMIT 5";
	}
}
Pattern vInjectionPattern = Pattern.compile("(?i)\\bINSERT|DELETE|LOAD|CLEAR|CREATE|DROP|COPY|MOVE|ADD\\b");
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
sparqlrequest.addArgument("database","milieuinfo:database-cbb");
sparqlrequest.addArgument("expiry","milieuinfo:expiry-cbb");
sparqlrequest.addArgument("credentials","milieuinfo:credentials-cbb");
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
			INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
			xsltcrequest.addArgumentByValue("operand", vSPARQLResult);
			xsltcrequest.addArgument("operator", "res:/resources/xsl/milieuinfocbbsparql.xsl");
			xsltcrequest.addArgument("replace","milieuinfo:baseurl");
			xsltcrequest.addArgumentByValue("with", aWith);
			xsltcrequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
			
			INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
			tagsouprequest.addArgumentByRequest("operand", xsltcrequest);
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBSPARQLAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//