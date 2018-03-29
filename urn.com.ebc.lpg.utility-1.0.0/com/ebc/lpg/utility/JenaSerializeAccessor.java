package com.ebc.lpg.utility;/**
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
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import java.util.UUID;

/**
 * Jena Serialize Accessor
 * turns JenaModel operand into serialization depending on the request
 * 
 */
public class JenaSerializeAccessor extends StandardAccessorImpl {

	public JenaSerializeAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("operand",null,null,new Class[] {IRepJenaModel.class}));
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "JenaSerializeAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		// there is an operand but we don't need to get it out here
		//
		
		// processing
		IReadableBinaryStreamRepresentation vRBS = null;
		String vMimetype = null;
		INKFRequest jenaserializerequest = null;

		String vActiveType = aContext.getThisRequest().getArgumentValue("activeType");
		switch (vActiveType) {
		case "jena2rdfxml":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
			vMimetype = "application/rdf+xml";
			break;
		case "jena2turtle":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeTURTLE");
			vMimetype = "text/turtle";
			break;
		case "jena2ntriple":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeN-TRIPLE");
			vMimetype = "text/plain";
			break;
		case "jena2jsonld":
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeJSONLD");
			vMimetype = "application/ld+json";
			break;
		default:
			jenaserializerequest = aContext.createRequest("active:jRDFSerializeXML");
			vMimetype = "application/rdf+xml";			
			break;
		}

		jenaserializerequest.addArgument("operand", "arg:operand");
		jenaserializerequest.setRepresentationClass(IReadableBinaryStreamRepresentation.class);
		vRBS = (IReadableBinaryStreamRepresentation)aContext.issueRequest(jenaserializerequest);
		
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vRBS);
		vResponse.setMimeType(vMimetype);
		vResponse.setExpiry(INKFResponse.EXPIRY_DEPENDENT);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "JenaSerializeAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//		
	}
}
