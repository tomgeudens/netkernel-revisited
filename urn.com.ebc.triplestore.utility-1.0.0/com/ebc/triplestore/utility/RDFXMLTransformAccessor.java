package com.ebc.triplestore.utility;

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
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * RDF XML Transform Accessor
 * Transforms incoming RDF XML to RDF Turtle, RDF N-Triples or RDF JSON-LD
 */
public class RDFXMLTransformAccessor extends StandardAccessorImpl {

	public RDFXMLTransformAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(IReadableBinaryStreamRepresentation.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXMLTransformAccessor: start of id - " + vId);
		//
		
		// processing
		
		//parse no longer necessary
		//INKFRequest jenaparserequest = aContext.createRequest("active:jRDFParseXML");
		//jenaparserequest.addArgument("operand", "arg:operand");
		//Object vJenaParseResult = aContext.issueRequest(jenaparserequest);
		
		INKFRequest modelemptyrequest = aContext.createRequest("active:jRDFModelIsEmpty");
		//modelemptyrequest.addArgumentByValue("operand", vJenaParseResult);
		modelemptyrequest.addArgument("operand", "arg:operand");
		modelemptyrequest.setRepresentationClass(Boolean.class);
		Boolean vIsModelEmpty = (Boolean)aContext.issueRequest(modelemptyrequest);
		
		IReadableBinaryStreamRepresentation vRBS = null;
		String vMimetype = null;
		INKFRequest jenaserializerequest = null;
		
		String vActiveType = aContext.getThisRequest().getArgumentValue("activeType");
		switch (vActiveType) {
		case "rdfxml2rdfxml":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
			vMimetype = "application/rdf+xml";
			break;
		case "rdfxml2turtle":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeTURTLE");
			vMimetype = "text/turtle";
			break;
		case "rdfxml2ntriple":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeN-TRIPLE");
			vMimetype = "text/plain";
			break;
		case "rdfxml2jsonld":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeJSONLD");
			vMimetype = "application/ld+json";
			break;
		default:
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
			vMimetype = "application/rdf+xml";			
			break;
		}
		
		//jenaserializerequest.addArgumentByValue("operand", vJenaParseResult);
		jenaserializerequest.addArgument("operand", "arg:operand");
		jenaserializerequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
		vRBS = (IReadableBinaryStreamRepresentation)aContext.issueRequest(jenaserializerequest);
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vRBS);
		vResponse.setMimeType(vMimetype);
		vResponse.setHeader("empty", vIsModelEmpty);
		vResponse.setExpiry(INKFResponse.EXPIRY_DEPENDENT);
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXMLTransformAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
