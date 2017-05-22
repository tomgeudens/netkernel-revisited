package com.ebc.neo4j.experiment;

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
import java.util.HashMap;
import java.util.Map;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.util.Pair;

import static org.neo4j.driver.v1.Values.parameters;


/**
 * Simple Accessor
 * Experiments with the Neo4j jdbc kit
 */
public class SimpleAccessor extends StandardAccessorImpl {

	public SimpleAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SimpleAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		//
		
		// processing
		String vDatabaseURL = aContext.source("neo4j:databaseurl", String.class);
		String vDatabaseUser = aContext.source("neo4j:databaseuser", String.class);
		String vDatabasePassword = aContext.source("neo4j:databasepassword", String.class);
		
		Driver vDriver = GraphDatabase.driver(vDatabaseURL, AuthTokens.basic( vDatabaseUser, vDatabasePassword ));
		Session vSession = vDriver.session();

		vSession.run( "MATCH (a:Person) WHERE a.name = {name} " +
				"DETACH DELETE a", 
				parameters( "name", "Arthur") );

		Map<String, Object> vMap = new HashMap<String, Object>(10);
		vMap.put("name", "Arthur");
		vMap.put("title", "King");		
		vSession.run("CREATE (a:Person {name: {name}, title: {title}})", vMap);

		StatementResult vResult = vSession.run( "MATCH (a:Person) WHERE a.name = {name} " + 
				"RETURN a.name AS name, a.title AS title", 
				parameters( "name", "Arthur" ) );
		
		System.out.println("Results");
		System.out.println("*******");
		while ( vResult.hasNext() )
		{
		    Record vRecord = vResult.next();
		    for (Pair <String, Value> ListEntry : vRecord.fields()) {
		    	System.out.println(ListEntry.key() + " - " +  ListEntry.value().asString());
		    }
		    // System.out.println(vRecord.get( "title" ).asString() + " " + vRecord.get( "name" ).asString() );
		}

		vSession.close();
		vDriver.close();
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom("executed SimpleAccessor");
		vResponse.setMimeType("text/plain");
		vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
		//		

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SimpleAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
}
