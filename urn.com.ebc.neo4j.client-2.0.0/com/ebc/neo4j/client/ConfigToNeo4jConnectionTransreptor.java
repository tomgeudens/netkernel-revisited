package com.ebc.neo4j.client;

import org.netkernel.layer0.nkf.INKFLocale;

/**
 * 
 * Elephant Bird Consulting
 * 
 * @author tomgeudens
 *
 */

/**
 * Transreptor Imports
 */
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

import org.netkernel.mod.hds.*;

import com.ebc.neo4j.client.ConnectionPoolRepresentation;

/**
 * ConfigToNeo4jConnection Accessor
 * transrepts a database configuration into a driver connection
 */
public class ConfigToNeo4jConnectionTransreptor extends StandardTransreptorImpl {

	public ConfigToNeo4jConnectionTransreptor() {
		this.declareToRepresentation(ConnectionPoolRepresentation.class);
		this.declareThreadSafe();
	}

	@Override
	public void onTransrept(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ConfigToNeo4jConnectionTransreptor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		IHDSDocument aDatabaseConfig = aContext.sourcePrimary(IHDSDocument.class);
		IHDSReader aDatabaseConfigReader = aDatabaseConfig.getReader();
		//
		
		// processing
		ConnectionPoolRepresentation vCPR = new ConnectionPoolRepresentation(
				aDatabaseConfigReader.getFirstValue("/config/url").toString(),
				aDatabaseConfigReader.getFirstValue("/config/username").toString(),
				aDatabaseConfigReader.getFirstValue("/config/password").toString());
		//
		
		// response
		//
		aContext.createResponseFrom(vCPR);
				
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ConfigToNeo4jConnectionTransreptor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//		
	}
}
