package com.ebc.tool.system;
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
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import org.netkernel.mod.hds.*;
import org.netkernel.mod.hds.HDSFactory;

/**
 * Environment Accessor
 * Reads a specific variable from netkernel:/environment
 */

public class EnvironmentAccessor extends StandardAccessorImpl {

	public EnvironmentAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareArgument(new SourcedArgumentMetaImpl("variable",null,null,new Class[] {String.class}));
		this.declareSourceRepresentation(String.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "EnvironmentAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		String aVariable = null;
		try {
			aVariable = aContext.source("arg:variable", String.class);
		}
		catch (Exception e) {
			// sensible default that we will not find in netkernel:/environment
			aVariable = vId.toString();
		}		
		//
		
		// processing
		IHDSDocument vEnvironment = null;
		try {
			INKFRequest nkenvironmentrequest = aContext.createRequest("netkernel:/environment");
			nkenvironmentrequest.setVerb(INKFRequestReadOnly.VERB_SOURCE);
			nkenvironmentrequest.setHeader("exclude-dependencies", true);
			nkenvironmentrequest.setRepresentationClass(IHDSDocument.class);
			vEnvironment = (IHDSDocument)aContext.issueRequest(nkenvironmentrequest);
		}
		catch (Exception e) {
			// create empty environment IHDSDocument
			IHDSMutator vNewDocument = HDSFactory.newDocument();
			vNewDocument.pushNode("environment");
			vEnvironment = vNewDocument.toDocument(false);
		}
		
		IHDSReader vEnvironmentReader = vEnvironment.getReader();
		String vResult = null;
		vResult = (String)vEnvironmentReader.getFirstValueOrNull(aVariable);
		if (vResult == null) {
			vResult = "UNKNOWNVARIABLE";
		}
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vResult);
		vResponse.setMimeType("text/plain");
		vResponse.setExpiry(INKFResponse.EXPIRY_NEVER);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "EnvironmentAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//		
	}
}