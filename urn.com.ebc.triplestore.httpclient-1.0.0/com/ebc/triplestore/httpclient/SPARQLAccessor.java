package com.ebc.triplestore.httpclient;

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
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * SPARQL Accessor
 * Launches request against a http SPARQL endpoint
 */
public class SPARQLAccessor extends StandardAccessorImpl {

	public SPARQLAccessor() {
		this.declareThreadSafe();
		this.declareArgument(new SourcedArgumentMetaImpl("database",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("query",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("credentials",null,null,new Class[] {IHDSNode.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SPARQLAccessor: start of id - " + vId);
		//

		// response
		INKFResponse vResponse = aContext.createResponseFrom("to implement");
		//vResponse.setMimeType(vMimetype);
		vResponse.setExpiry(INKFResponse.EXPIRY_DEPENDENT);
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SPARQLAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//

	}
}
