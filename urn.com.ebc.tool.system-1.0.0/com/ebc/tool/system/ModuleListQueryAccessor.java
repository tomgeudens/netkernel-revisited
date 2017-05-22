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
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSReader;
import org.netkernel.ext.system.representation.IRepDeployedModules;

/**
 * Module List Query Accessor
 * Performs XPATH query on the deployed module list and returns the result
 */
public class ModuleListQueryAccessor extends StandardAccessorImpl {

	public ModuleListQueryAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareArgument(new SourcedArgumentMetaImpl("xpath",null,null,new Class[] {String.class}));
		this.declareSourceRepresentation(String.class);
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ModuleListQueryAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		String aXpath = null;
		try {
			aXpath = aContext.source("arg:xpath", String.class);
		}
		catch (Exception e) {
			throw new Exception("ModuleListQueryAccessor: no valid - xpath - argument");
		}
		if (aXpath.equals("")) {
			// sensible default
			aXpath = "/module/modules/id[1]";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ModuleListQueryAccessor: (" 
				+ vId 
				+ ") - argument xpath : " 
				+ aXpath);		
		//
		
		// processing
		IRepDeployedModules vModuleList = null;
		String vResult = null;
		
		try {
			INKFRequest modulerequest = aContext.createRequest("active:moduleList");
			modulerequest.setRepresentationClass(IRepDeployedModules.class);
			vModuleList = (IRepDeployedModules)aContext.issueRequest(modulerequest);

			IHDSDocument vModuleListHDS = aContext.transrept(vModuleList.getHierarchy(), IHDSDocument.class);
			IHDSReader vModuleListHDSReader = vModuleListHDS.getReader();
			vResult = (String) vModuleListHDSReader.getFirstValue(aXpath); 
		}
		catch (Exception e) {
			vResult = "UNKNOWN";
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
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ModuleListQueryAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//		
	}
}
