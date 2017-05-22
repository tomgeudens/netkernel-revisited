package com.ebc.neo4j.client;

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
import org.netkernel.mod.hds.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.util.Pair;


/**
 * Rows Accessor
 * neo4j database accessor for cypher queries that returns rows
 */
public class RowsAccessor extends StandardAccessorImpl {

	public RowsAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareArgument(new SourcedArgumentMetaImpl("databaseurl",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("databaseuser",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("databasepassword",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("cypher",null,null,new Class[] {String.class}));
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		String aDatabaseURL = null;
		try {
			aDatabaseURL = aContext.source("arg:databaseurl", String.class);
		}
		catch (Exception e) {
			throw new Exception("RowsAccessor: no valid - databaseurl - argument");
		}
		if (aDatabaseURL.equals("")) {
			throw new Exception("RowsAccessor: argument - databaseurl - can not be empty");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: (" 
				+ vId 
				+ ") - argument databaseurl : " 
				+ aDatabaseURL);

		String aDatabaseUser = null;
		try {
			aDatabaseUser = aContext.source("arg:databaseuser", String.class);
		}
		catch (Exception e) {
			throw new Exception("RowsAccessor: no valid - databaseuser - argument");
		}
		if (aDatabaseUser.equals("")) {
			throw new Exception("RowsAccessor: argument - databaseuser - can not be empty");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: (" 
				+ vId 
				+ ") - argument databaseuser : " 
				+ aDatabaseUser);
		
		String aDatabasePassword = null;
		try {
			aDatabasePassword = aContext.source("arg:databasepassword", String.class);
		}
		catch (Exception e) {
			throw new Exception("RowsAccessor: no valid - databasepassword - argument");
		}
		if (aDatabasePassword.equals("")) {
			throw new Exception("RowsAccessor: argument - databasepassword - can not be empty");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: (" 
				+ vId 
				+ ") - argument databasepassword : " 
				+ aDatabasePassword);

		Long aExpiry = null;
		try {
			aExpiry = aContext.source("arg:expiry", Long.class);
		}
		catch (Exception e) {
			try {
				String aExpiryString = aContext.source("arg:expiry", String.class);
				aExpiry = Long.parseLong(aExpiryString);
			}
			catch (Exception el2) {
				throw new Exception("RowsAccessor: no valid - expiry - argument");
			}
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: (" 
				+ vId 
				+ ") - argument expiry : " 
				+ aExpiry.toString());

		String aCypher = null;
		try {
			aCypher = aContext.source("arg:cypher", String.class);
		}
		catch (Exception e) {
			throw new Exception("RowsAccessor: no valid - cypher - argument");
		}
		if (aCypher.equals("")) {
			// providing sensible default
			aCypher = "MATCH (n) RETURN ID(n) as rowid LIMIT 5";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: (" 
				+ vId 
				+ ") - argument cypher : " 
				+ aCypher.toString());

		//
		
		// processing
		IHDSMutator vMutator = HDSFactory.newDocument();
		vMutator.pushNode("results");
		
		Driver vDriver = null;
		Session vSession = null;
		
		try {
			vDriver = GraphDatabase.driver(aDatabaseURL,
					AuthTokens.basic(aDatabaseUser, aDatabasePassword));
			vSession = vDriver.session();
			
			StatementResult vResult = vSession.run(aCypher);
			
			while (vResult.hasNext()) {
				Record vRecord = vResult.next();
				
				vMutator.pushNode("row");
				
			    for (Pair <String, Value> ListEntry : vRecord.fields()) {
			    	if (ListEntry.value().type().equals(vSession.typeSystem().STRING())) {
			    		vMutator.addNode(ListEntry.key(), ListEntry.value().asString());
			    	}
			    	
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().INTEGER())) {
			    		vMutator.addNode(ListEntry.key(), Integer.toString(ListEntry.value().asInt()));
			    	}
			    	
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().FLOAT())) {
			    		vMutator.addNode(ListEntry.key(), Float.toString(ListEntry.value().asFloat()));
			    	}
			    	
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().MAP())) {
			    		Iterator<Entry<String, Object>> vIterator = ListEntry.value().asMap().entrySet().iterator();
			    		while (vIterator.hasNext()) {
			    			Map.Entry<String, Object> vEntry = (Map.Entry<String, Object>)vIterator.next();
			    			vMutator.addNode(ListEntry.key() + "." + vEntry.getKey(), vEntry.getValue().toString());
			    		}
			    	}
			    	
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().LIST())) {
			    		for (int i = 0; i < ListEntry.value().asList().size(); i++) {
			    			vMutator.addNode(ListEntry.key() + "." + i, ListEntry.value().asList().get(i).toString());
			    		}
			    	}
			    	
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().NODE())) {
			    		Iterator<Entry<String, Object>> vIterator = ListEntry.value().asNode().asMap().entrySet().iterator();
			    		while (vIterator.hasNext()) {
			    			Map.Entry<String, Object> vEntry = (Map.Entry<String, Object>)vIterator.next();
			    			vMutator.addNode(ListEntry.key() + "." + vEntry.getKey(), vEntry.getValue().toString());
			    		}
			    	}
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().RELATIONSHIP())) {
			    		Iterator<Entry<String, Object>> vIterator = ListEntry.value().asRelationship().asMap().entrySet().iterator();
			    		while (vIterator.hasNext()) {
			    			Map.Entry<String, Object> vEntry = (Map.Entry<String, Object>)vIterator.next();
			    			vMutator.addNode(ListEntry.key() + "." + vEntry.getKey(), vEntry.getValue().toString());
			    		}
			    	}
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().PATH())) {
			    		System.out.println(ListEntry.key() + " - " +  ListEntry.value());
			    	}
			    	else if (ListEntry.value().type().equals(vSession.typeSystem().BOOLEAN())) {
			    		vMutator.addNode(ListEntry.key(), Boolean.toString(ListEntry.value().asBoolean()));
			    	}
			    	else {
			    		System.out.println(ListEntry.key() + " - " +  ListEntry.value());
			    	}
			    }
			    
			    vMutator.popNode(); // row
			}			
			
		}
		catch (Exception e) {
			
		}
		finally {
			if (vSession != null) {
				vSession.close();
			}
			if (vDriver != null) {
				vDriver.close();
			}
		}
		vMutator.popNode(); // results
		
		IHDSDocument vRepresentation = vMutator.toDocument(false);
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vRepresentation);
		vResponse.setExpiry(INKFResponse.EXPIRY_MIN_CONSTANT_DEPENDENT, System.currentTimeMillis() + aExpiry);
		//		

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "RowsAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
}
