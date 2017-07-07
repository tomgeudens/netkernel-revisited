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

/**
 * Processing Imports
 */
import java.util.UUID;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ebc.neo4j.embedded.representation.Neo4jInstance;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSMutator;

/**
 * Neo4j Cyper Accessor
 * execute cypher query on embedded instance
 */
public class Neo4jCypherAccessor extends StandardAccessorImpl {

	public Neo4jCypherAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(IHDSDocument.class);
		this.declareArgument(new SourcedArgumentMetaImpl("instance",null,null,new Class[] {Neo4jInstance.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("cypher",null,null,new Class[] {String.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jCypherAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		Neo4jInstance aInstance = null;
		try {
			aInstance = aContext.source("arg:instance", Neo4jInstance.class);
		}
		catch (Exception e) {
			throw new Exception("Neo4jCypherAccessor: no valid - instance - argument");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jCypherAccessor: (" 
				+ vId 
				+ ") - argument instance : " 
				+ aInstance.toString());
		
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
				throw new Exception("Neo4jCypherAccessor: no valid - expiry - argument");
			}
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jCypherAccessor: (" 
				+ vId 
				+ ") - argument expiry : " 
				+ aExpiry.toString());

		String aCypher = null;
		try {
			aCypher = aContext.source("arg:cypher", String.class);
		}
		catch (Exception e) {
			throw new Exception("Neo4jCypherAccessor: no valid - cypher - argument");
		}
		if (aCypher.equals("")) {
			// providing sensible default
			aCypher = "MATCH (n) RETURN ID(n) as rowid LIMIT 5";
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jCypherAccessor: (" 
				+ vId 
				+ ") - argument cypher : " 
				+ aCypher.toString());
		//
		
		// processing
		IHDSMutator vMutator = HDSFactory.newDocument();
		vMutator.pushNode("results");
		
		try (
				Transaction vTx = aInstance.getInstance().beginTx();
				Result vResult = aInstance.getInstance().execute(aCypher);
			) {
			
			while (vResult.hasNext()) {
				Map<String, Object> vRecord = vResult.next();
				
				vMutator.pushNode("row");
				
				for ( Entry<String,Object> vRecordEntry : vRecord.entrySet() ) {
					process_listentry(vMutator, vRecordEntry.getKey(), vRecordEntry.getValue());
				}
				
				vMutator.popNode(); // row
			}
			
			vResult.close();
			vTx.success();
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
		aContext.logRaw(INKFLocale.LEVEL_INFO, "Neo4jCypherAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
	
	private void process_listentry(IHDSMutator vMutator, String vKey, Object vValue) {
		if (vValue instanceof String) {
			String vString = (String)vValue;
			vMutator.addNode(vKey, vString);
		}
		
		else if (vValue instanceof String[]) {
			String[] vStringArray = (String[])vValue;
			
			vMutator.pushNode(vKey);
			int iProperty = 0;
			for (String lValue : vStringArray) {
				process_listentry(vMutator, vKey + "." + iProperty, lValue);
			}
			vMutator.popNode();
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
			Node vNode = (Node)vValue;
			vMutator.addNode("_id", Long.toString(vNode.getId()));
			for (Label lLabel : vNode.getLabels()) {
				vMutator.addNode("_label", lLabel.name());
			}
			for (String lKey : vNode.getPropertyKeys()) {
				Object lValue = vNode.getProperty(lKey);
				process_listentry(vMutator, lKey, lValue);
			}
    		vMutator.popNode();
		}
		
		else if (vValue instanceof List) {
			vMutator.pushNode(vKey);
			@SuppressWarnings("rawtypes")
			List vList = (List)vValue;
			for (int i = 0; i < vList.size(); i++) {
				process_listentry(vMutator, vKey +  "." + i , vList.get(i));
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
				process_listentry(vMutator, vEntry.getKey(), vEntry.getValue());
			}
			vMutator.popNode();			
		}
		
		else if (vValue instanceof Relationship) {
			vMutator.pushNode(vKey);
			Relationship vRelationship = (Relationship)vValue;
			vMutator.addNode("_startid", Long.toString(vRelationship.getStartNodeId()));
			vMutator.addNode("_endid", Long.toString(vRelationship.getEndNodeId()));
			vMutator.addNode("_type", vRelationship.getType().name());
			for (String lKey : vRelationship.getPropertyKeys()) {
				Object lValue = vRelationship.getProperty(lKey);
				process_listentry(vMutator, lKey, lValue);
			}			
			vMutator.popNode();
		}
		
		else if (vValue instanceof Path) {
			vMutator.pushNode(vKey);
			Path vPath = (Path)vValue;
			
			process_listentry(vMutator, "_startnode", vPath.startNode());
			
			vMutator.pushNode("nodes");
			int iNode = 0;
			for (Node vNode : vPath.nodes()) {
				process_listentry(vMutator, "node." + iNode, vNode);
				iNode++;
			}
			vMutator.popNode();
			
			vMutator.pushNode("relationships");
			int iRelationship = 0;
			for (Relationship vRelationship : vPath.relationships()) {
				process_listentry(vMutator, "relationship." + iRelationship, vRelationship);
				iRelationship++;
			}
			vMutator.popNode();
			
			process_listentry(vMutator, "_endnode", vPath.endNode());
			
			vMutator.popNode();
		}
		
		else {
			System.out.println(vKey + " - " +  vValue);
		}
	}
}
