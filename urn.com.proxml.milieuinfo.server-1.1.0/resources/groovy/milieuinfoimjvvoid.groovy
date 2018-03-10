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
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSReader;


/**
 * Milieuinfo IMJV VOID Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVVOIDAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
Boolean vIsHTTPRequest = (Boolean)aContext.exists("httpRequest:/remote-host");
//

// processing
int vHTTPResponseCode = 200;
//Object vJenaModel = null;
//Object vJenaSerializeResult = null;
//
//INKFRequest incacherequest = aContext.createRequest("pds:/dataset/imjv");
//incacherequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
//incacherequest.setRepresentationClass(Boolean.class);
//Boolean vInCache = (Boolean)aContext.issueRequest(incacherequest);
//
//if (vInCache) {
//	vJenaSerializeResult = aContext.source("pds:/dataset/imjv");
//	vHTTPResponseCode = 200;
//}
//else {
//	INKFRequest emptyrequest = aContext.createRequest("active:jRDFEmptyModel");
//	vJenaModel = aContext.issueRequest(emptyrequest);
//	
//	INKFRequest modulerequest = aContext.createRequest("active:modulelistquery");
//	modulerequest.addArgumentByValue("xpath", "/modules/module[id=\"urn:com:proxml:milieuinfo:server\"]/source");
//	modulerequest.setRepresentationClass(String.class);
//	
//	String vDirectory = (String) aContext.issueRequest(modulerequest);
//	
//	if (vDirectory.startsWith("file:/")) {
//		
//		IHDSDocument vFLSResult = null;
//		if (vDirectory.endsWith(".jar")) {
//			INKFRequest flsrequest = aContext.createRequest("active:flsjar");
//			flsrequest.addArgumentByValue("root", vDirectory);
//			flsrequest.addArgumentByValue("filter","resources/sparql/void/imjv/.*sparql");
//			flsrequest.setRepresentationClass(IHDSDocument.class);
//			vFLSResult = (IHDSDocument) aContext.issueRequest(flsrequest);
//		}
//		else {
//			INKFRequest flsrequest = aContext.createRequest("active:fls");
//			flsrequest.addArgumentByValue("operator", "<fls><root>" + vDirectory + "resources/sparql/void/imjv/" + "</root><uri/></fls>");
//			flsrequest.setRepresentationClass(IHDSDocument.class);
//			vFLSResult = (IHDSDocument) aContext.issueRequest(flsrequest);
//		}
//		
//		IHDSReader vHDSReader = vFLSResult.getReader();
//		for (IHDSReader vHDSLooper: vHDSReader.getNodes("//uri")) {
//			 processing each sparql resource
//			
//			INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
//			sparqlrequest.addArgument("database","milieuinfo:database-imjv");
//			sparqlrequest.addArgument("expiry", "milieuinfo:expiry-imjv");
//			sparqlrequest.addArgument("credentials","milieuinfo:credentials-imjv");
//			sparqlrequest.addArgument("query", (String) vHDSLooper.getFirstValue("."));
//			sparqlrequest.addArgumentByValue("accept","application/rdf+xml");
//		
//			INKFResponseReadOnly sparqlresponse = aContext.issueRequestForResponse(sparqlrequest);
//			vHTTPResponseCode = sparqlresponse.getHeader("httpresponsecode");
//			
//			if (vHTTPResponseCode == 200) {
//				INKFRequest jenaunionrequest = aContext.createRequest("active:jRDFModelUnion");
//				jenaunionrequest.addArgumentByValue("model1", vJenaModel);
//				jenaunionrequest.addArgumentByValue("model2", sparqlresponse.getRepresentation());
//				
//				vJenaModel = aContext.issueRequest(jenaunionrequest);
//			}
			
//		}
//	}
//	INKFRequest jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
//	jenaserializerequest.addArgumentByValue("operand", vJenaModel);
//	vJenaSerializeResult = aContext.issueRequest(jenaserializerequest);
//	
//	aContext.sink("pds:/dataset/imjv", vJenaSerializeResult);
//	vHTTPResponseCode = 200;
//}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(aContext.source("res:/resources/rdf/imjv_void.rdf"));
vResponse.setHeader("httpresponsecode", vHTTPResponseCode);
vResponse.setMimeType("text/xml");

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
aContext.logRaw(INKFLocale.LEVEL_INFO, "MilieuinfoIMJVVOIDAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//