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
import org.netkernel.layer0.representation.*;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import java.io.InputStream;

/**
 * KBOData FedX Query Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataFedXQueryAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

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
		aQuery = (String)aContext.source("kbodata:query", String.class);
	}
	catch (Exception e) {
		// sensible default
		aQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .} LIMIT 5";
	}
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

IReadableBinaryStreamRepresentation aEndpointsRBS = null;
if (vIsHTTPRequest) {
	try {
		String aEndpointsResource = (String)aContext.source("httpRequest:/param/endpoints", String.class);
		aEndpointsRBS = (IReadableBinaryStreamRepresentation)aContext.source(aEndpointsResource, IReadableBinaryStreamRepresentation.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("endpoints")) {// endpoints is an optional argument {
		try {
			aEndpointsRBS = (IReadableBinaryStreamRepresentation)aContext.source("arg:endpoints", IReadableBinaryStreamRepresentation.class);
		}
		catch (Exception e) {
			//
		}
	}
}
if (aEndpointsRBS == null) {
	// sensible default
	aEndpointsRBS = (IReadableBinaryStreamRepresentation)aContext.source("res:/resources/txt/endpoints.txt", IReadableBinaryStreamRepresentation.class);
}
//

// processing
INKFRequest fedxqueryrequest = aContext.createRequest("active:fedxquery");
fedxqueryrequest.addArgument("expiry", "kbodata:expiry");
fedxqueryrequest.addArgumentByValue("query", aQuery);
String vEndpoint = null;
InputStream vEndpointsInputStream = aEndpointsRBS.getInputStream();
BufferedReader vEndpointsReader = new BufferedReader(new InputStreamReader(vEndpointsInputStream));
while ((vEndpoint = vEndpointsReader.readLine()) != null) {
	fedxqueryrequest.addArgumentByValue("endpoint", vEndpoint);
}
vEndpointsInputStream.close();
if (aAccept.startsWith("text/html")) {
	fedxqueryrequest.addArgumentByValue("accept","application/sparql-results+xml");
}
else {
	fedxqueryrequest.addArgumentByValue("accept", aAccept);
}

INKFResponseReadOnly fedxqueryresponse = aContext.issueRequestForResponse(fedxqueryrequest);
// int vHTTPResponseCode = fedxqueryresponse.getHeader("httpresponsecode");
Object vFEDXQueryResult = fedxqueryresponse.getRepresentation();
//

// response
INKFResponse vResponse = null;
if (aAccept.startsWith("text/html")) {
	INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
	xsltcrequest.addArgumentByValue("operand", vFEDXQueryResult);
	xsltcrequest.addArgument("operator", "res:/resources/xsl/kbodatafedxquery.xsl");
	xsltcrequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
	
	INKFRequest tagsouprequest = aContext.createRequest("active:tagSoup");
	tagsouprequest.addArgumentByRequest("operand", xsltcrequest);
	tagsouprequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
	
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(tagsouprequest));
}
else {
	vResponse = aContext.createResponseFrom(vFEDXQueryResult);
}

vResponse.setMimeType(aAccept);

if (vIsHTTPRequest) {
	// pass the code on
	// vResponse.setHeader("httpResponse:/code", vHTTPResponseCode);
	
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataFedXQueryAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//