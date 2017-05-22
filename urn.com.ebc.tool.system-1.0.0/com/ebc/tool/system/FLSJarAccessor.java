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
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netkernel.mod.hds.*;

/**
 * FLS for Jars Accessor
 * Simulates the FLS capabilities with a jar as starting point, returns jar:file: uris
 */
public class FLSJarAccessor extends StandardAccessorImpl {

	public FLSJarAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareArgument(new SourcedArgumentMetaImpl("root",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("filter",null,null,new Class[] {String.class}));
		this.declareSourceRepresentation(IHDSDocument.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FLSJarAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		String aRoot = null;
		try {
			aRoot = aContext.source("arg:root", String.class);
		}
		catch (Exception e) {
			throw new Exception("FLSJarAccessor: no valid - root - argument");
		}
		if (aRoot.equals("")) {
			// sensible default
			throw new Exception("FLSJarAccessor: no valid - root - argument, can not be empty");
		}
		if (! aRoot.startsWith("file:/")) {
			// sensible default
			throw new Exception("FLSJarAccessor: no valid - root - argument, must be a file:/ uri");
		}
		if (! aRoot.endsWith(".jar")) {
			// sensible default
			throw new Exception("FLSJarAccessor: no valid - root - argument, must be a .jar uri");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FLSJarAccessor: (" 
				+ vId 
				+ ") - argument root : " 
				+ aRoot);
		
		String aFilter = null;
		if (aContext.getThisRequest().argumentExists("filter")) {// filter is an optional argument
			try {
				aFilter = aContext.source("arg:filter", String.class);
			}
			catch (Exception e) {
				// sensible default is to allow everything
				aFilter = ".*";
			}			
		}
		else {
			// sensible default is to allow everything
			aFilter = ".*";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FLSJarAccessor: (" 
				+ vId 
				+ ") - argument filter : " 
				+ aFilter);
		//
		
		// processing
		URL vJar = new URL(aRoot);
		Pattern vFilterPattern = Pattern.compile(aFilter);
		IHDSMutator vMutator = HDSFactory.newDocument();
		ZipInputStream vZip = null;
		
		vMutator.pushNode("fls");
		
		try {
			vZip = new ZipInputStream(vJar.openStream());
			while(true) {
				ZipEntry vEntry = vZip.getNextEntry();
				if (vEntry == null) {
					break;
				}
				Matcher vFilterMatcher = vFilterPattern.matcher(vEntry.getName());
				Boolean vFilterFound = vFilterMatcher.find();
				if (vFilterFound) {
					vMutator.addNode("uri", "jar:" + aRoot + "!/" + vEntry.getName());
				}
			}
		}
		finally {
			if (vZip != null) {
				vZip.close();
			}
		}
		
		vMutator.popNode();
		IHDSDocument vRepresentation = vMutator.toDocument(false);
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vRepresentation);
		vResponse.setExpiry(INKFResponse.EXPIRY_NEVER);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FLSJarAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
}
