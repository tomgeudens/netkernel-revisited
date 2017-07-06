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
//import org.netkernel.layer0.representation.*;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * RDF XML To Boolean Accessor
 * Transforms incoming RDF XML (result of ASK query) to Boolean
 */

public class RDFXMLToBooleanAccessor extends StandardAccessorImpl {
	public RDFXMLToBooleanAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(Boolean.class);		
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXMLToBooleanAccessor: ("
				+ vId
				+ ") - start");
		//

		// arguments
		//
		
		// processing
		INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
		xsltcrequest.addArgument("operand","arg:operand");
		xsltcrequest.addArgument("operator","res:/resources/xsl/asktoboolean.xsl");
		xsltcrequest.setRepresentationClass(String.class);
		Boolean vResult = Boolean.parseBoolean(((String)aContext.issueRequest(xsltcrequest)).trim());
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vResult);
		vResponse.setExpiry(INKFResponse.EXPIRY_DEPENDENT);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RDFXMLToBooleanAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
}