package com.ebc.neo4j.embedded.endpoint;

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

import java.io.File;
/**
 * Processing Imports
 */
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.ebc.neo4j.embedded.representation.Neo4jInstance;

/**
 * Neo4j Instance Accessor
 * management of embedded neo4j instances
 */
public class Neo4jInstanceAccessor extends StandardAccessorImpl {
	private static ConcurrentHashMap<String, GraphDatabaseService> mInstances;
	
	class InstanceExpiry implements INKFExpiryFunction {
		private Neo4jInstance mInstanceRepresentation = null;
		
		public InstanceExpiry(Neo4jInstance aInstanceRepresentation) {
			mInstanceRepresentation = aInstanceRepresentation;
		}
		
		public boolean isExpired(long aNow) {
			return mInstanceRepresentation.getInstance().isAvailable(200);
		}
	}
	
	public Neo4jInstanceAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE|INKFRequestReadOnly.VERB_DELETE);
		this.declareSourceRepresentation(Neo4jInstance.class);
		this.declareArgument(new SourcedArgumentMetaImpl("databasename",null,null,new Class[] {String.class}));
		
		mInstances = new ConcurrentHashMap<String, GraphDatabaseService>();
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: ("
				+ vId
				+ ") - start - onSource");
		//
		
		// arguments
		String aEmbeddedLocation = aContext.source("neo4j:embeddedlocation", String.class);
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
				+ vId 
				+ ") - argument embeddedlocation : " 
				+ aEmbeddedLocation + " - onSource");
		
		String aDatabaseName = null;
		try {
			aDatabaseName = aContext.source("arg:databasename", String.class);
		}
		catch (Exception e) {
			throw new Exception("Neo4jInstanceAccessor: no valid - databasename - argument");
		}
		if (aDatabaseName.equals("")) {
			aDatabaseName = "graph.db";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
				+ vId 
				+ ") - argument databasename : " 
				+ aDatabaseName + " - onSource");
		//
		
		// processing
		File vPath = new File(aEmbeddedLocation + aDatabaseName + File.separator);
		
		if (! mInstances.containsKey(aDatabaseName)) {
			// start new instance
			if (! vPath.exists()) {
				if (! vPath.mkdirs()) {
					throw new Exception("Neo4jInstanceAccessor: can not create " + vPath.getPath());
				}
			}
			
			GraphDatabaseService vInstance = new GraphDatabaseFactory().newEmbeddedDatabase(vPath);
			registerShutdownHook(vInstance);
			
			aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
					+ vId 
					+ ") - starting instance : " 
					+ aDatabaseName + " - onSource");			
			
			mInstances.put(aDatabaseName, vInstance);
		}
		else {
			// existing instance (but may be shutdown)
			GraphDatabaseService vInstance = mInstances.get(aDatabaseName);
			if (! vInstance.isAvailable(200) ) {
				vInstance = new GraphDatabaseFactory().newEmbeddedDatabase(vPath);
				registerShutdownHook(vInstance);
				mInstances.put(aDatabaseName, vInstance);
				aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
						+ vId 
						+ ") - restarting instance : " 
						+ aDatabaseName + " - onSource");
			}
			else {
				aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
						+ vId 
						+ ") - reusing instance : " 
						+ aDatabaseName + " - onSource");				
			}
		}
		//
		
		// response
		Neo4jInstance vRepresentation = new Neo4jInstance(mInstances.get(aDatabaseName));
		INKFResponse vResponse = aContext.createResponseFrom(vRepresentation);
		vResponse.setExpiry(INKFResponse.EXPIRY_FUNCTION, new InstanceExpiry(vRepresentation));
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds - onSource");
		//
	}
	
	public void onDelete(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: ("
				+ vId
				+ ") - start - onDelete");
		//

		// arguments
		String aDatabaseName = null;
		try {
			aDatabaseName = aContext.source("arg:databasename", String.class);
		}
		catch (Exception e) {
			throw new Exception("Neo4jInstanceAccessor: no valid - databasename - argument");
		}
		if (aDatabaseName.equals("")) {
			aDatabaseName = "graph.db";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
				+ vId 
				+ ") - argument databasename : " 
				+ aDatabaseName + " - onDelete");
		//
		
		// processing
		Boolean vResult = null;
		
		if (mInstances.containsKey(aDatabaseName)) {
			GraphDatabaseService vInstance = mInstances.get(aDatabaseName);
			
			if (! vInstance.isAvailable(200) ) {
				vInstance.shutdown();
			
				aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: (" 
						+ vId 
						+ ") - stopping instance : " 
						+ aDatabaseName + " - onDelete");					
			}
			
			mInstances.remove(aDatabaseName);
			
			vResult = true;
		}
		else {
			vResult = false;
		}
		//
		
		// response
		aContext.createResponseFrom(vResult);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jInstanceAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds - onDelete");
		//
	}

	private static void registerShutdownHook( final GraphDatabaseService vInstance ) {
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
	        public void run() {
	            vInstance.shutdown();
	        }
	    });
	}
}
