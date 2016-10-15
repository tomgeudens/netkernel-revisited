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
 * Milieuinfo CBB Fragments Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBFragmentsAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aSubject = null;
if (vIsHTTPRequest) {
	try {
		aSubject = (String)aContext.source("httpRequest:/param/subject", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("subject")) {// subject is an optional argument
		try {
			aSubject = (String)aContext.source("arg:subject", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aSubject == null || aSubject == "") {
	// sensible default
	aSubject = "?s"
}

String aPredicate = null;
if (vIsHTTPRequest) {
	try {
		aPredicate = (String)aContext.source("httpRequest:/param/predicate", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("predicate")) {// predicate is an optional argument
		try {
			aPredicate = (String)aContext.source("arg:predicate", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aPredicate == null || aPredicate == "") {
	// sensible default
	aPredicate = "?p"
}

String aObject = null;
if (vIsHTTPRequest) {
	try {
		aObject = (String)aContext.source("httpRequest:/param/object", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("object")) {// object is an optional argument
		try {
			aObject = (String)aContext.source("arg:object", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aObject == null || aObject == "") {
	// sensible default
	aObject = "?o"
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
	aLimit = "100"
}

String aOffset = null;
if (vIsHTTPRequest) {
	try {
		aOffset = (String)aContext.source("httpRequest:/param/offset", String.class);
	}
	catch (Exception e) {
		//
	}
}
else {
	if (aContext.getThisRequest().argumentExists("offset")) {// offset is an optional argument
		try {
			aOffset = (String)aContext.source("arg:offset", String.class)
		}
		catch (Exception e) {
			//
		}
	}
}
if (aOffset == null || aOffset == "") {
	// sensible default
	aOffset = "0"
}

String aQuery = null;
if (vIsHTTPRequest) {
	try {
		aQuery = (String)aContext.source("httpRequest:/query", String.class);
	}
	catch (Exception e) {
		//
	}
}
if (aQuery == null || aQuery == "") {
	// can be deducted from subject, predicate, object, limit and offset
	aQuery = new StringBuilder().append("subject=").append(aSubject).append("&predicate=").append(aPredicate).append("&object=").append(aObject).append("&limit=").append(aLimit).append("&offset=").append(aOffset).toString();
}

String aURL = null;
if (vIsHTTPRequest) {
	try {
		aURL = (String)aContext.source("httpRequest:/url", String.class);
	}
	catch (Exception e) {
		//
	}
}
if (aURL == null || aURL == "") {
	try {
		aURL = (String)aContext.source("milieuinfo:baseurl-cbb", String.class) + "/fragments";
	}
	catch (Exception e) {
		// sensible default
		aURL = "http://localhost/fragments";
	}
}

String aDataset = null;
try {
	aDataset = (String)aContext.source("milieuinfo:dataset-cbb", String.class);
}
catch (Exception e) {
	// sensible default
	aDataset = "http://localhost";
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
INKFRequest fragmentsrequest = aContext.createRequest("active:fragments");
fragmentsrequest.addArgument("database","milieuinfo:database-cbb");
fragmentsrequest.addArgumentByValue("accept", "application/rdf+xml");
fragmentsrequest.addArgument("expiry","milieuinfo:expiry-cbb");
fragmentsrequest.addArgument("credentials","milieuinfo:credentials-cbb");
fragmentsrequest.addArgumentByValue("subject", aSubject);
fragmentsrequest.addArgumentByValue("predicate", aPredicate);
fragmentsrequest.addArgumentByValue("object", aObject);
fragmentsrequest.addArgumentByValue("limit", aLimit);
fragmentsrequest.addArgumentByValue("offset", aOffset);
fragmentsrequest.addArgumentByValue("dataset", aDataset);
fragmentsrequest.addArgumentByValue("url", aURL);
fragmentsrequest.addArgumentByValue("query", aQuery);

INKFResponseReadOnly fragmentsresponse = aContext.issueRequestForResponse(fragmentsrequest);
int vHTTPResponseCode = fragmentsresponse.getHeader("httpresponsecode");
Object vFragmentsResult = fragmentsresponse.getRepresentation();
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vFragmentsResult);
vResponse.setHeader("httpresponsecode", vHTTPResponseCode);
if (vHTTPResponseCode >= 400) {
	vResponse.setMimeType("text/plain"); // best mimetype for an errormessage
	vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS); // we don't want to cache this
}
else {
	vResponse.setMimeType("text/xml");
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoCBBFragmentsAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
