package com.ebc.tool.pdsfile;

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
import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.io.File;
import java.net.URI;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * PDS Accessor
 * Implements a file-based backend for the pds:/ scheme 
 */
public class PDSAccessor extends StandardAccessorImpl {

	public PDSAccessor() {
		this.declareThreadSafe();
		this.declareInhibitCheckForBadExpirationOnMutableResource();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE|INKFRequestReadOnly.VERB_EXISTS|INKFRequestReadOnly.VERB_SINK|INKFRequestReadOnly.VERB_DELETE);
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (source): start of id - " + vId);
		//
		
		// arguments
		Arguments aArguments = new Arguments(aContext);
		
		IHDSNode aPDSConfig=(IHDSNode)aContext.source("res:/etc/pdsConfig.xml", IHDSNode.class);
		String aPDSFileDirectory = (String)aPDSConfig.getFirstValue("/config/pdsfiledirectory");
		//
		
		// processing
		String vFileResource = aPDSFileDirectory + aArguments.mZone + aArguments.mIdentifier.replaceFirst("pds:", "");
		
		//System.out.println("instance = " + aArguments.mInstance);
		//System.out.println("zone = " + aArguments.mZone);
		//System.out.println("identifier = " + aArguments.mIdentifier);
		//System.out.println("pdsfiledirectory = " + aPDSFileDirectory);
		//System.out.println("fileresource = " + vFileResource);
		
		if (aArguments.isIdentifierSetBoundary()) {
			HDSBuilder vFLSConfig = new HDSBuilder();
			vFLSConfig.pushNode("fls");
			vFLSConfig.addNode("root", vFileResource);
			vFLSConfig.addNode("uri", "");
			vFLSConfig.addNode("keepEmptyDir", "");
			vFLSConfig.popNode();
			
			INKFRequest flsrequest = aContext.createRequest("active:fls");
			flsrequest.addArgumentByValue("operator", vFLSConfig.getRoot());
			Document vFLSResult = (Document)aContext.issueRequest(flsrequest);
			
			HDSBuilder vSet = new HDSBuilder();
			vSet.pushNode("set");
			
		    NodeList nodeList = vFLSResult.getElementsByTagName("uri");
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node node = nodeList.item(i);
		        if (node.getNodeType() == Node.ELEMENT_NODE) {
		        	vSet.addNode("identifier", node.getTextContent().replaceFirst(aPDSFileDirectory + aArguments.mZone, "pds:"));
		        }
		    }
		    
		    nodeList = vFLSResult.getElementsByTagName("dir");
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node node = nodeList.item(i);
		        if (node.getNodeType() == Node.ELEMENT_NODE) {
		        	vSet.addNode("identifier", aArguments.mIdentifier + node.getTextContent() + "/");
		        }
		    }
		    
		    vSet.popNode();
		    
		    // response
		    INKFResponse vResponse = aContext.createResponseFrom(vSet.getRoot());
		    vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
		    //
		}
		else {
			INKFRequest sourcerequest = aContext.createRequest(vFileResource);
			sourcerequest.setVerb(INKFRequestReadOnly.VERB_SOURCE);
			
			// response
			aContext.createResponseFrom(aContext.issueRequestForResponse(sourcerequest));
			//
		}
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (source) : finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}

	public void onExists(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (exists): start of id - " + vId);
		//

		// arguments
		Arguments aArguments = new Arguments(aContext);
		
		IHDSNode aPDSConfig=(IHDSNode)aContext.source("res:/etc/pdsConfig.xml", IHDSNode.class);
		String aPDSFileDirectory = (String)aPDSConfig.getFirstValue("/config/pdsfiledirectory");
		//
		
		// processing
		String vFileResource = aPDSFileDirectory + aArguments.mZone + aArguments.mIdentifier.replaceFirst("pds:", "");
		
		//System.out.println("instance = " + aArguments.mInstance);
		//System.out.println("zone = " + aArguments.mZone);
		//System.out.println("identifier = " + aArguments.mIdentifier);
		//System.out.println("pdsfiledirectory = " + aPDSFileDirectory);
		//System.out.println("fileresource = " + vFileResource);
		
		INKFRequest existsrequest = aContext.createRequest(vFileResource);
		existsrequest.setVerb(INKFRequestReadOnly.VERB_EXISTS);
		//
		
		// response
		aContext.createResponseFrom(aContext.issueRequestForResponse(existsrequest));
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (exists) : finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}

	public synchronized void onSink(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (sink): start of id - " + vId);
		//
	
		// arguments
		Arguments aArguments = new Arguments(aContext);
		
		IHDSNode aPDSConfig=(IHDSNode)aContext.source("res:/etc/pdsConfig.xml", IHDSNode.class);
		String aPDSFileDirectory = (String)aPDSConfig.getFirstValue("/config/pdsfiledirectory");
		//
		
		// processing
		String vFileResource = aPDSFileDirectory + aArguments.mZone + aArguments.mIdentifier.replaceFirst("pds:", "");
		
		//System.out.println("instance = " + aArguments.mInstance);
		//System.out.println("zone = " + aArguments.mZone);
		//System.out.println("identifier = " + aArguments.mIdentifier);
		//System.out.println("pdsfiledirectory = " + aPDSFileDirectory);
		//lhvSystem.out.println("fileresource = " + vFileResource);
		//
		
		INKFRequest sinkrequest = aContext.createRequest(vFileResource);
		sinkrequest.setVerb(INKFRequestReadOnly.VERB_SINK);
		sinkrequest.addPrimaryArgument(aContext.sourcePrimary(IReadableBinaryStreamRepresentation.class));
		aContext.issueRequest(sinkrequest);
		
		// response
		// no response for a sink
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (sink) : finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}

	public void onDelete(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (delete): start of id - " + vId);
		//

		// arguments
		Arguments aArguments = new Arguments(aContext);
		
		IHDSNode aPDSConfig=(IHDSNode)aContext.source("res:/etc/pdsConfig.xml", IHDSNode.class);
		String aPDSFileDirectory = (String)aPDSConfig.getFirstValue("/config/pdsfiledirectory");
		//
		
		// processing
		String vFileResource = aPDSFileDirectory + aArguments.mZone + aArguments.mIdentifier.replaceFirst("pds:", "");
		
		//System.out.println("instance = " + aArguments.mInstance);
		//System.out.println("zone = " + aArguments.mZone);
		//System.out.println("identifier = " + aArguments.mIdentifier);
		//System.out.println("pdsfiledirectory = " + aPDSFileDirectory);
		//System.out.println("fileresource = " + vFileResource);
		
		if (aArguments.isIdentifierSetBoundary()) {
			HDSBuilder vFLSConfig = new HDSBuilder();
			vFLSConfig.pushNode("fls");
			vFLSConfig.addNode("root", vFileResource);
			vFLSConfig.addNode("uri", "");
			vFLSConfig.addNode("keepEmptyDir", "");
			vFLSConfig.popNode();
			
			INKFRequest flsrequest = aContext.createRequest("active:fls");
			flsrequest.addArgumentByValue("operator", vFLSConfig.getRoot());
			Document vFLSResult = (Document)aContext.issueRequest(flsrequest);

			int vDeleteCount = 0;
			
		    NodeList nodeList = vFLSResult.getElementsByTagName("uri");
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node node = nodeList.item(i);
		        if (node.getNodeType() == Node.ELEMENT_NODE) {
		        	String vFileURLToDelete = node.getTextContent();
		        	INKFRequest deleterequest = aContext.createRequest(vFileURLToDelete);
		        	deleterequest.setVerb(INKFRequestReadOnly.VERB_DELETE);
		        	aContext.issueRequest(deleterequest);
		        	vDeleteCount++;
		        }
		    }
		    
		    nodeList = vFLSResult.getElementsByTagName("dir");
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node node = nodeList.item(i);
		        if (node.getNodeType() == Node.ELEMENT_NODE) {
		        	File vDirectory = new File(new URI(vFileResource + node.getTextContent() + "/"));
		        	deleteDir(vDirectory);
		        	vDeleteCount++;
		        }
		    }
		    
		    // response
		    INKFResponse vResponse = null;
		    if (vDeleteCount > 0) {
		    	vResponse = aContext.createResponseFrom(true);
		    }
		    else {
		    	vResponse = aContext.createResponseFrom(false);
		    }
		    vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
		    //
		}
		else {
			INKFRequest deleterequest = aContext.createRequest(vFileResource);
			deleterequest.setVerb(INKFRequestReadOnly.VERB_DELETE);
			
			// response
			aContext.createResponseFrom(aContext.issueRequestForResponse(deleterequest));
			//
		}
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "PDSAccessor (delete) : finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
	
	public static class Arguments {
		public String mInstance;
		public String mZone;
		public String mIdentifier;
		
		private Arguments(INKFRequestContext aContext) {
			mInstance = aContext.getThisRequest().getArgumentValue("instance");
			mZone = aContext.getThisRequest().getArgumentValue("zone");
			mIdentifier = aContext.getThisRequest().getArgumentValue("pds");
		}
		
		private Arguments(String aInstance, String aZone, String aIdentifier) {
			mInstance = aInstance;
			mZone = aZone;
			mIdentifier = aIdentifier;
		}
		
		public String getInstance() {
			return mInstance;
		}
		
		public String getZone() {
			return mZone;
		}

		public String getIdentifier() {
			return mIdentifier;
		}
		
		public boolean isIdentifierSetBoundary() {
			return mIdentifier.endsWith("/");
		}
		
		public String getSetIdentifier() {
			return mIdentifier.substring(0, mIdentifier.lastIndexOf("/") + 1);
		}
		
		public boolean equals(Object aObject) {
			boolean vResult = false;
			
			if (aObject instanceof Arguments) {
				Arguments vOther = (Arguments)aObject;
				vResult = mInstance.equals(vOther.mInstance) && mZone.equals(vOther.mZone) && mIdentifier.equals(vOther.mIdentifier);
			}
			
			return vResult;
		}
		
		public int hashCode() {
			return mInstance.hashCode() ^ mZone.hashCode() ^ mIdentifier.hashCode();
		}
	}
	
	public void deleteDir(File vFile) {
	    File[] vContents = vFile.listFiles();
	    if (vContents != null) {
	        for (File f : vContents) {
	            deleteDir(f);
	        }
	    }
	    vFile.delete();		
	}
}
