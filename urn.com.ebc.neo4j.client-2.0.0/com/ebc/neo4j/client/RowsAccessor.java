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
import com.ebc.neo4j.client.ConnectionPoolRepresentation;

import org.netkernel.mod.hds.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.types.TypeSystem;
import org.neo4j.driver.v1.util.Pair;


/**
 * Rows Accessor
 * neo4j database accessor for cypher queries that returns rows
 */
public class RowsAccessor extends StandardAccessorImpl {

	public RowsAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(IHDSDocument.class);
		this.declareArgument(new SourcedArgumentMetaImpl("databaseconfiguration",null,null,new Class[] {IHDSDocument.class}));
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
		ConnectionPoolRepresentation vCPR = null;
		if (aContext.getThisRequest().argumentExists("databaseconfiguration")) {// databaseconfiguration is an optional argument
			try {
				vCPR = aContext.source("arg:databaseconfiguration", ConnectionPoolRepresentation.class);
			}
			catch (Exception e) {
				throw new Exception("RowsAccessor: no valid - databaseconfiguration - argument");
			}			
		}
		else {// neo4j:databaseconfiguration is the default
			try {
				vCPR = aContext.source("neo4j:databaseconfiguration", ConnectionPoolRepresentation.class);
			}
			catch (Exception e) {
				throw new Exception("RowsAccessor: no valid - neo4j:databaseconfiguration - resource");
			}					
		}

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
		
		Session vSession = null;
		
		try {
			vSession = vCPR.getSession();
			
			StatementResult vResult = vSession.run(aCypher);
			
			while (vResult.hasNext()) {
				Record vRecord = vResult.next();
				
				vMutator.pushNode("row");
				
			    for (Pair <String, Value> vListEntry : vRecord.fields()) {
			    	process_listentry(vSession.typeSystem(), vMutator, vListEntry.key(), vListEntry.value());
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

	private void process_listentry(TypeSystem vTS, IHDSMutator vMutator, String vKey, Object vValue) {		
		if (vValue instanceof String) {
			String vString = (String)vValue;
			vMutator.addNode(vKey, vString);
		}
		
		else if (vValue instanceof Integer) {
			Integer vInteger = (Integer)vValue;
			vMutator.addNode(vKey, Integer.toString(vInteger));
		}
		
		else if (vValue instanceof Long) {
			Long vLong = (Long)vValue;
			vMutator.addNode(vKey, Long.toString(vLong));
		}
		
		else if (vValue instanceof Float) {
			Float vFloat = (Float)vValue;
			vMutator.addNode(vKey, Float.toString(vFloat));
		}
		
		else if (vValue instanceof Number) {
			Number vNumber = (Number)vValue;
			vMutator.addNode(vKey, vNumber.toString());
		}
		
		else if (vValue instanceof Node) {
			vMutator.pushNode(vKey);
			vMutator.addNode("_ontologyobject", "node");
			Node vNode = (Node)vValue;
			vMutator.addNode("_id", Long.toString(vNode.id()));
			for (String lLabel : vNode.labels()) {
				vMutator.addNode("_label", lLabel);
			}
			for (String lKey : vNode.keys()) {
				Value lValue = vNode.get(lKey);
				process_listentry(vTS, vMutator, lKey, lValue);
			}
    		vMutator.popNode();
		}
		
		else if (vValue instanceof Relationship) {
			vMutator.pushNode(vKey);
			vMutator.addNode("_ontologyobject", "relationship");
			Relationship vRelationship = (Relationship)vValue;
			vMutator.addNode("_startid", Long.toString(vRelationship.startNodeId()));
			vMutator.addNode("_endid", Long.toString(vRelationship.endNodeId()));
			vMutator.addNode("_type", vRelationship.type());
			for (String lKey : vRelationship.keys()) {
				Value lValue = vRelationship.get(lKey);
				process_listentry(vTS, vMutator, lKey, lValue);
			}			
			vMutator.popNode();
		}
		
		else if (vValue instanceof List) {
			vMutator.pushNode(vKey);
			@SuppressWarnings("rawtypes")
			List vList = (List)vValue;
			for (int i = 0; i < vList.size(); i++) {
				process_listentry(vTS, vMutator, vKey +  "." + i , vList.get(i));
			}
			vMutator.popNode();			
		}
		
		else if (vValue instanceof Map) {
			vMutator.pushNode(vKey);
			@SuppressWarnings("unchecked")
			Map<String, Object> vMap = (Map<String, Object>)vValue;
			Iterator<Entry<String, Object>> vIterator = vMap.entrySet().iterator();
			while (vIterator.hasNext()) {
				Map.Entry<String, Object> vEntry = (Map.Entry<String, Object>)vIterator.next();
				process_listentry(vTS, vMutator, vEntry.getKey(), vEntry.getValue());
			}
			vMutator.popNode();			
		}
		
		else if (vValue instanceof Path) {
			vMutator.pushNode(vKey);
			Path vPath = (Path)vValue;
			
			process_listentry(vTS, vMutator, "_startnode", vPath.start());
			
			vMutator.pushNode("nodes");
			int iNode = 0;
			for (Node vNode : vPath.nodes()) {
				process_listentry(vTS, vMutator, "node." + iNode, vNode);
				iNode++;
			}
			vMutator.popNode();
			
			vMutator.pushNode("relationships");
			int iRelationship = 0;
			for (Relationship vRelationship : vPath.relationships()) {
				process_listentry(vTS, vMutator, "relationship." + iRelationship, vRelationship);
				iRelationship++;
			}
			vMutator.popNode();
			
			process_listentry(vTS, vMutator, "_endnode", vPath.end());
			
			vMutator.popNode();
		}
		
		else {
			System.out.println(vKey + " - " +  vValue);
		}
	}
	
	private void process_listentry(TypeSystem vTS, IHDSMutator vMutator, String vKey, Value vValue) {
		if (vValue.hasType(vTS.STRING())) {
    		vMutator.addNode(vKey, vValue.asString());
    	}
		
		else if (vValue.hasType(vTS.INTEGER())) {
    		vMutator.addNode(vKey, Integer.toString(vValue.asInt()));
    	}
		
		else if (vValue.hasType(vTS.NUMBER())) {
    		vMutator.addNode(vKey, vValue.asNumber().toString());
    	}
		
		else if (vValue.hasType(vTS.FLOAT())) {
    		vMutator.addNode(vKey, Float.toString(vValue.asFloat()));
    	}

		else if (vValue.hasType(vTS.NODE()) && vValue.hasType(vTS.MAP())){
			vMutator.pushNode(vKey);
			vMutator.addNode("_ontologyobject", "node");
			vMutator.addNode("_id", Long.toString(vValue.asNode().id()));
			for (String lLabel : vValue.asNode().labels()) {
				vMutator.addNode("_label", lLabel);
			}
			for (String lKey : vValue.asNode().keys()) {
				Value lValue = vValue.asNode().get(lKey);
				process_listentry(vTS, vMutator, lKey, lValue);
			}
    		vMutator.popNode();
		}
		
		else if (vValue.hasType(vTS.RELATIONSHIP()) && vValue.hasType(vTS.MAP())){
			vMutator.pushNode(vKey);
			vMutator.addNode("_ontologyobject", "relationship");
			vMutator.addNode("_startid", Long.toString(vValue.asRelationship().startNodeId()));
			vMutator.addNode("_endid", Long.toString(vValue.asRelationship().endNodeId()));
			vMutator.addNode("_type", vValue.asRelationship().type());
			for (String lKey : vValue.asRelationship().keys()) {
				Value lValue = vValue.asRelationship().get(lKey);
				process_listentry(vTS, vMutator, lKey, lValue);
			}			
			vMutator.popNode();			
		}
		
		else if (vValue.hasType(vTS.LIST())) {
			vMutator.pushNode(vKey);
			for (int i = 0; i < vValue.asList().size(); i++) {
				process_listentry(vTS, vMutator, vKey +  "." + i , vValue.asList().get(i));
			}
			vMutator.popNode();
    	}
		
		else if (vValue.hasType(vTS.MAP())) {
			vMutator.pushNode(vKey);
			Iterator<Entry<String, Object>> vIterator = vValue.asMap().entrySet().iterator();
			while (vIterator.hasNext()) {
				Map.Entry<String, Object> vEntry = (Map.Entry<String, Object>)vIterator.next();
				process_listentry(vTS, vMutator, vEntry.getKey(), vEntry.getValue());
			}
			vMutator.popNode();
		}
		
		else if (vValue.hasType(vTS.PATH())){
			vMutator.pushNode(vKey);
			
			process_listentry(vTS, vMutator, "_startnode", vValue.asPath().start());
			
			vMutator.pushNode("nodes");
			int iNode = 0;
			for (Node vNode : vValue.asPath().nodes()) {
				process_listentry(vTS, vMutator, "node." + iNode, vNode);
				iNode++;
			}
			vMutator.popNode();
			
			vMutator.pushNode("relationships");
			int iRelationship = 0;
			for (Relationship vRelationship : vValue.asPath().relationships()) {
				process_listentry(vTS, vMutator, "relationship." + iRelationship, vRelationship);
				iRelationship++;
			}
			vMutator.popNode();
			
			process_listentry(vTS, vMutator, "_endnode", vValue.asPath().end());
			
			vMutator.popNode();
		}
		
		else {
			System.out.println(vKey + " - " +  vValue);
		}
	}
}
