package com.ebc.helloworldLogging;

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

/**
 * Hello World Accessor
 */
public class HelloWorldAccessor extends StandardAccessorImpl {

	public HelloWorldAccessor() {
		this.declareThreadSafe();
		this.declareSourceRepresentation(String.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		aContext.logRaw(INKFLocale.LEVEL_INFO, "example logging from hello world java accessor");
		aContext.createResponseFrom("Hello World !");
	}
}
