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
//import org.netkernel.layer0.vocab.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSReader;

/**
 * Milieuinfo Vocab with Extension Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoVocabwithExtensionAccessor: start of id - " + vId);
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");

String aOwner = null;
try {
	aOwner = aContext.source("arg:owner", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoVocabwithExtensionAccessor: ("
		+ vId
		+ ") - argument owner : invalid");
	throw new Exception("MilieuinfoVocabwithExtensionAccessor: no valid - owner - argument");
}

aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoVocabwithExtensionAccessor: ("
	+ vId
	+ ") - argument owner : "
	+ aOwner);

String aID = null;
try {
	aID = aContext.source("arg:id", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "MilieuinfoVocabwithExtensionAccessor: ("
		+ vId
		+ ") - argument id : invalid");
	throw new Exception("MilieuinfoVocabwithExtensionAccessor: no valid - id - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoVocabwithExtensionAccessor: ("
	+ vId
	+ ") - argument id : "
	+ aID);

String aExtension = null;
try {
	aExtension = aContext.source("arg:extension", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_WARNING, "MilieuinfoVocabwithExtensionAccessor: ("
		+ vId
		+ ") - argument extension : invalid");
	// sensible default
	aExtension = "html"
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoVocabwithExtensionAccessor: ("
	+ vId
	+ ") - argument extension : "
	+ aExtension);

String vBaseURL = aContext.source("milieuinfo:baseurl", String.class);
String vFullURI = vBaseURL + "/vocab/" + aOwner + "/" + aID;
//

// processing
int vHTTPResponseCode = 0;
Object vJenaModel = null;
Object vJenaSerializeResult = null;

INKFRequest emptyrequest = aContext.createRequest("active:jRDFEmptyModel");
vJenaModel = aContext.issueRequest(emptyrequest);

IHDSDocument vCatalog = aContext.source("res:/resources/xml/vocab-sparql-catalog.xml", IHDSDocument.class);
IHDSReader vCatalogReader = vCatalog.getReader();

for (IHDSReader vQueryReader: vCatalogReader.getNodes("/sparql-catalog/sparql")) {
	String vURI = (String)vQueryReader.getFirstValue("@uri");
	Boolean vProcess = true;
	for (IHDSReader vConditionReader: vQueryReader.getNodes("condition")) {
		String vParameter = vConditionReader.getFirstValueOrNull("@parameter");
		String vHasNotValue =  vConditionReader.getFirstValueOrNull("@has-not-value");
		String vHasValue =  vConditionReader.getFirstValueOrNull("@has-value");
		
		if (vParameter == "domain" && vHasNotValue != null && vHasNotValue == aOwner) {
			vProcess = false;
			break;
		}
		
		if (vParameter == "format" && vHasNotValue != null && vHasNotValue == aExtension) {
			vProcess = false;
			break;
		}

		if (vParameter == "domain" && vHasValue != null && vHasValue != aOwner) {
			vProcess = false;
			break;
		}

		if (vParameter == "format" && vHasValue != null && vHasValue != aExtension) {
			vProcess = false;
			break;
		}
	}

	if (vProcess) {
		INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
		freemarkerrequest.addArgument("operator", "res:/resources/freemarker/" + vURI);
		freemarkerrequest.addArgumentByValue("domain", aOwner);
		freemarkerrequest.addArgumentByValue("uri", vFullURI);
		freemarkerrequest.setRepresentationClass(String.class);
		String vQuery = (String)aContext.issueRequest(freemarkerrequest);
		
		INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
		sparqlrequest.addArgument("database","milieuinfo:database-vocab");
		sparqlrequest.addArgument("expiry", "milieuinfo:expiry-vocab");
		sparqlrequest.addArgument("credentials","milieuinfo:credentials-vocab");
		sparqlrequest.addArgumentByValue("query", vQuery);
		sparqlrequest.addArgumentByValue("accept","application/rdf+xml");

		INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
		vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
			
		if (vHTTPResponseCode == 200) {
			INKFRequest jenaunionrequest = aContext.createRequest("active:jRDFModelUnion");
			jenaunionrequest.addArgumentByValue("model1", vJenaModel);
			jenaunionrequest.addArgumentByValue("model2", sparqlresponse.getRepresentation());
				
			vJenaModel = aContext.issueRequest(jenaunionrequest);
		}
	}
}

INKFRequest jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
jenaserializerequest.addArgumentByValue("operand", vJenaModel);
vJenaSerializeResult = aContext.issueRequest(jenaserializerequest);

vHTTPResponseCode = 200;
//

// response
INKFResponse vResponse = null;
switch (aExtension) {
case "rdf":
	INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
	rdfxml2rdfxmlrequest.addArgumentByValue("operand", vJenaSerializeResult);
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2rdfxmlrequest));
	vResponse.setMimeType("application/rdf+xml");
	break;
case "ttl":
	INKFRequest rdfxml2turtlerequest = aContext.createRequest("active:rdfxml2turtle");
	rdfxml2turtlerequest.addArgumentByValue("operand", vJenaSerializeResult);
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2turtlerequest));
	vResponse.setMimeType("text/turtle");
	break;
case "nt":
	INKFRequest rdfxml2ntriplerequest = aContext.createRequest("active:rdfxml2ntriple");
	rdfxml2ntriplerequest.addArgumentByValue("operand", vJenaSerializeResult);
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2ntriplerequest));
	vResponse.setMimeType("text/plain");
	break;
case "jsonld":
	INKFRequest rdfxml2jsonldrequest = aContext.createRequest("active:rdfxml2jsonld");
	rdfxml2jsonldrequest.addArgumentByValue("operand", vJenaSerializeResult);
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2jsonldrequest));
	vResponse.setMimeType("application/ld+json");
	break;
case "html":
	INKFRequest rdfxml2htmlsaxonrequest = aContext.createRequest("active:rdfxml2htmlsaxon");
	rdfxml2htmlsaxonrequest.addArgumentByValue("domain", aOwner);
	rdfxml2htmlsaxonrequest.addArgumentByValue("operand", vJenaSerializeResult);
	rdfxml2htmlsaxonrequest.addArgument("operator", "res:/resources/xsl/rdfxml2htmlvocabgeneric.xsl");
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2htmlsaxonrequest));
	vResponse.setMimeType("text/html");
	break;
default:
	INKFRequest rdfxml2rdfxmlrequest = aContext.createRequest("active:rdfxml2rdfxml");
	rdfxml2rdfxmlrequest.addArgumentByValue("operand", vJenaSerializeResult);
	vResponse = aContext.createResponseFrom(aContext.issueRequestForResponse(rdfxml2rdfxmlrequest));
	vResponse.setMimeType("application/rdf+xml");
	break;
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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoVocabwithExtensionAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//