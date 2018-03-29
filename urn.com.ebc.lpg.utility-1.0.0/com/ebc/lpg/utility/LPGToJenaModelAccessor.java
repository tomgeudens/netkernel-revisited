package com.ebc.lpg.utility;

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
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import org.netkernel.mod.hds.*;

import java.util.HashMap;
import java.util.UUID;

/**
 * LPGToJenaModel Accessor
 * turns HDS operand containing LPG data into Jena Model
 * applies ontology information passed in as HDS operator (currently not implemented) 
 * 
 */
public class LPGToJenaModelAccessor extends StandardAccessorImpl {

	public LPGToJenaModelAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(IRepJenaModel.class);
		this.declareArgument(new SourcedArgumentMetaImpl("operand",null,null,new Class[] {IHDSDocument.class}));		
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "LPGToJenaModelAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		IHDSDocument aOperand = null;
		try {
			aOperand = aContext.source("arg:operand", IHDSDocument.class);
		}
		catch (Exception e) {
			throw new Exception("LPGToJenaModelAccessor: no valid - operand - argument");
		}
		aContext.logRaw(INKFLocale.LEVEL_INFO, "LPGToJenaModelAccessor: (" 
				+ vId 
				+ ") - argument operand (hascode): " 
				+ aOperand.hashCode());
		//
		
		// processing
		INKFRequest emptymodelrequest = aContext.createRequest("active:jRDFEmptyModel");
		IRepJenaModel vResultJenaModel = (IRepJenaModel)aContext.issueRequest(emptymodelrequest);
		
		HashMap<String, String> vNodes = new HashMap<String, String>(); // we need to save the nodes
		IHDSMutator vNewTriples = HDSFactory.newDocument(); // one document to hold them all
		vNewTriples.pushNode("jRDFUpdateModel");
				
		// nodes at the first level
		for (IHDSReader vNode : aOperand.getReader().getNodes("/results/row/*[_ontologyobject='node']")) {
			String vNodeResource = "https://neo4j.com/" + 
					vNode.getFirstValue("_label").toString().toLowerCase() + "/" +
					vNode.getFirstValue("_id").toString() + "#id";
			
			INKFRequest freemarkernoderequest = aContext.createRequest("active:freemarker");
			freemarkernoderequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
			freemarkernoderequest.addArgumentByValue("resource", vNodeResource);
			freemarkernoderequest.addArgumentByValue("namespace", "http://www.w3.org/1999/02/");
			freemarkernoderequest.addArgumentByValue("name", "22-rdf-syntax-ns#type");
			freemarkernoderequest.addArgumentByValue("valuetype", "resource");
			freemarkernoderequest.addArgumentByValue("value", "https://neo4j.com/ns/randomdomain#" +
					vNode.getFirstValue("_label").toString()
			);
			freemarkernoderequest.setRepresentationClass(IHDSDocument.class);
			
			IHDSDocument freemarkernoderesult = (IHDSDocument)aContext.issueRequest(freemarkernoderequest);
			vNewTriples.appendChildren(freemarkernoderesult.getReader());
			
			vNodes.put(vNode.getFirstValue("_id").toString(), vNodeResource);
			
			for (IHDSReader vProperty: vNode.getNodes("*[_ontologyobject='property']")) {
				
				IHDSDocument propertyresult = handleProperty(vProperty.getFirstValue("name()").toString(),
						vProperty.getFirstValue("value").toString(),
						vProperty.getFirstValue("_type").toString(),
						"https://neo4j.com/" + vNode.getFirstValue("_label").toString().toLowerCase() + "/" + vNode.getFirstValue("_id").toString() + "#id",
						"https://neo4j.com/ns/",
						"randomdomain",
						aContext
				);
				
				vNewTriples.appendChildren(propertyresult.getReader());
			}
			
			for (IHDSReader vList: vNode.getNodes("*[_ontologyobject='list']")) {
				String vPropertyName = vList.getFirstValue("name()").toString();
				for (IHDSReader vProperty: vList.getNodes("*[_ontologyobject='property']")) {
					IHDSDocument propertyresult = handleProperty(vPropertyName,
							vProperty.getFirstValue("value").toString(),
							vProperty.getFirstValue("_type").toString(),
							"https://neo4j.com/" + vNode.getFirstValue("_label").toString().toLowerCase() + "/" + vNode.getFirstValue("_id").toString() + "#id",
							"https://neo4j.com/ns/",
							"randomdomain",
							aContext
					);
					
					vNewTriples.appendChildren(propertyresult.getReader());					
				}
			}
		}
		
		// relationships at the first level
		for (IHDSReader vRelationship : aOperand.getReader().getNodes("/results/row/*[_ontologyobject='relationship']")) {
			String vRelationshipResource = "https://neo4j.com/" + 
					vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" +
					vRelationship.getFirstValue("_id").toString() + "#id";
			String vStartnodeResource = vNodes.get(vRelationship.getFirstValue("_startid").toString());
			String vEndnodeResource = vNodes.get(vRelationship.getFirstValue("_endid").toString());;
			
			INKFRequest freemarkerrelationshiprequest = aContext.createRequest("active:freemarker");
			freemarkerrelationshiprequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
			freemarkerrelationshiprequest.addArgumentByValue("resource", vRelationshipResource);
			freemarkerrelationshiprequest.addArgumentByValue("namespace", "http://www.w3.org/1999/02/");
			freemarkerrelationshiprequest.addArgumentByValue("name", "22-rdf-syntax-ns#type");
			freemarkerrelationshiprequest.addArgumentByValue("valuetype", "resource");
			freemarkerrelationshiprequest.addArgumentByValue("value", "https://neo4j.com/ns/randomdomain#" +
					vRelationship.getFirstValue("_type").toString()
			);
			freemarkerrelationshiprequest.setRepresentationClass(IHDSDocument.class);
			
			IHDSDocument freemarkerrelationshipresult = (IHDSDocument)aContext.issueRequest(freemarkerrelationshiprequest);
			vNewTriples.appendChildren(freemarkerrelationshipresult.getReader());
			
			INKFRequest freemarkerlinkrequest = aContext.createRequest("active:freemarker");
			freemarkerlinkrequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
			freemarkerlinkrequest.addArgumentByValue("resource", vStartnodeResource);
			freemarkerlinkrequest.addArgumentByValue("namespace", vRelationshipResource.substring(0, vRelationshipResource.lastIndexOf('/')));
			freemarkerlinkrequest.addArgumentByValue("name", vRelationshipResource.substring(vRelationshipResource.lastIndexOf('/')));
			freemarkerlinkrequest.addArgumentByValue("valuetype", "resource");
			freemarkerlinkrequest.addArgumentByValue("value", vEndnodeResource);
			freemarkerlinkrequest.setRepresentationClass(IHDSDocument.class);
			
			IHDSDocument freemarkerlinkresult = (IHDSDocument)aContext.issueRequest(freemarkerlinkrequest);
			vNewTriples.appendChildren(freemarkerlinkresult.getReader());
			
			for (IHDSReader vProperty: vRelationship.getNodes("*[_ontologyobject='property']")) {
				IHDSDocument propertyresult = handleProperty(vProperty.getFirstValue("name()").toString(),
						vProperty.getFirstValue("value").toString(),
						vProperty.getFirstValue("_type").toString(),
						"https://neo4j.com/" + vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" + vRelationship.getFirstValue("_id").toString() + "#id",
						"https://neo4j.com/ns/",
						"randomdomain",
						aContext
				);
				
				vNewTriples.appendChildren(propertyresult.getReader());
			}

			for (IHDSReader vList: vRelationship.getNodes("*[_ontologyobject='list']")) {
				String vPropertyName = vList.getFirstValue("name()").toString();
				for (IHDSReader vProperty: vList.getNodes("*[_ontologyobject='property']")) {
					IHDSDocument propertyresult = handleProperty(vPropertyName,
							vProperty.getFirstValue("value").toString(),
							vProperty.getFirstValue("_type").toString(),
							"https://neo4j.com/" + vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" + vRelationship.getFirstValue("_id").toString() + "#id",
							"https://neo4j.com/ns/",
							"randomdomain",
							aContext
					);
					
					vNewTriples.appendChildren(propertyresult.getReader());					
				}
			}
		}
		
		// paths at the first level
		for (IHDSReader vPath : aOperand.getReader().getNodes("/results/row/*[_ontologyobject='path']")) {

			for (IHDSReader vNode : vPath.getNodes("nodes/*[_ontologyobject='node']")) {
				String vNodeResource = "https://neo4j.com/" + 
						vNode.getFirstValue("_label").toString().toLowerCase() + "/" +
						vNode.getFirstValue("_id").toString() + "#id";
				
				INKFRequest freemarkernoderequest = aContext.createRequest("active:freemarker");
				freemarkernoderequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
				freemarkernoderequest.addArgumentByValue("resource", vNodeResource);
				freemarkernoderequest.addArgumentByValue("namespace", "http://www.w3.org/1999/02/");
				freemarkernoderequest.addArgumentByValue("name", "22-rdf-syntax-ns#type");
				freemarkernoderequest.addArgumentByValue("valuetype", "resource");
				freemarkernoderequest.addArgumentByValue("value", "https://neo4j.com/ns/randomdomain#" +
						vNode.getFirstValue("_label").toString()
				);
				freemarkernoderequest.setRepresentationClass(IHDSDocument.class);
				
				IHDSDocument freemarkernoderesult = (IHDSDocument)aContext.issueRequest(freemarkernoderequest);
				vNewTriples.appendChildren(freemarkernoderesult.getReader());
				
				vNodes.put(vNode.getFirstValue("_id").toString(), vNodeResource);
				
				for (IHDSReader vProperty: vNode.getNodes("*[_ontologyobject='property']")) {
					
					IHDSDocument propertyresult = handleProperty(vProperty.getFirstValue("name()").toString(),
							vProperty.getFirstValue("value").toString(),
							vProperty.getFirstValue("_type").toString(),
							"https://neo4j.com/" + vNode.getFirstValue("_label").toString().toLowerCase() + "/" + vNode.getFirstValue("_id").toString() + "#id",
							"https://neo4j.com/ns/",
							"randomdomain",
							aContext
					);
					
					vNewTriples.appendChildren(propertyresult.getReader());
				}
				
				for (IHDSReader vList: vNode.getNodes("*[_ontologyobject='list']")) {
					String vPropertyName = vList.getFirstValue("name()").toString();
					for (IHDSReader vProperty: vList.getNodes("*[_ontologyobject='property']")) {
						IHDSDocument propertyresult = handleProperty(vPropertyName,
								vProperty.getFirstValue("value").toString(),
								vProperty.getFirstValue("_type").toString(),
								"https://neo4j.com/" + vNode.getFirstValue("_label").toString().toLowerCase() + "/" + vNode.getFirstValue("_id").toString() + "#id",
								"https://neo4j.com/ns/",
								"randomdomain",
								aContext
						);
						
						vNewTriples.appendChildren(propertyresult.getReader());					
					}
				}
			}
			
			for (IHDSReader vRelationship :  vPath.getNodes("relationships/*[_ontologyobject='relationship']")) {
				String vRelationshipResource = "https://neo4j.com/" + 
						vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" +
						vRelationship.getFirstValue("_id").toString() + "#id";
				String vStartnodeResource = vNodes.get(vRelationship.getFirstValue("_startid").toString());
				String vEndnodeResource = vNodes.get(vRelationship.getFirstValue("_endid").toString());;
				
				INKFRequest freemarkerrelationshiprequest = aContext.createRequest("active:freemarker");
				freemarkerrelationshiprequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
				freemarkerrelationshiprequest.addArgumentByValue("resource", vRelationshipResource);
				freemarkerrelationshiprequest.addArgumentByValue("namespace", "http://www.w3.org/1999/02/");
				freemarkerrelationshiprequest.addArgumentByValue("name", "22-rdf-syntax-ns#type");
				freemarkerrelationshiprequest.addArgumentByValue("valuetype", "resource");
				freemarkerrelationshiprequest.addArgumentByValue("value", "https://neo4j.com/ns/randomdomain#" +
						vRelationship.getFirstValue("_type").toString()
				);
				freemarkerrelationshiprequest.setRepresentationClass(IHDSDocument.class);
				
				IHDSDocument freemarkerrelationshipresult = (IHDSDocument)aContext.issueRequest(freemarkerrelationshiprequest);
				vNewTriples.appendChildren(freemarkerrelationshipresult.getReader());
				
				INKFRequest freemarkerlinkrequest = aContext.createRequest("active:freemarker");
				freemarkerlinkrequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
				freemarkerlinkrequest.addArgumentByValue("resource", vStartnodeResource);
				freemarkerlinkrequest.addArgumentByValue("namespace", vRelationshipResource.substring(0, vRelationshipResource.lastIndexOf('/')));
				freemarkerlinkrequest.addArgumentByValue("name", vRelationshipResource.substring(vRelationshipResource.lastIndexOf('/')));
				freemarkerlinkrequest.addArgumentByValue("valuetype", "resource");
				freemarkerlinkrequest.addArgumentByValue("value", vEndnodeResource);
				freemarkerlinkrequest.setRepresentationClass(IHDSDocument.class);
				
				IHDSDocument freemarkerlinkresult = (IHDSDocument)aContext.issueRequest(freemarkerlinkrequest);
				vNewTriples.appendChildren(freemarkerlinkresult.getReader());
				
				for (IHDSReader vProperty: vRelationship.getNodes("*[_ontologyobject='property']")) {
					IHDSDocument propertyresult = handleProperty(vProperty.getFirstValue("name()").toString(),
							vProperty.getFirstValue("value").toString(),
							vProperty.getFirstValue("_type").toString(),
							"https://neo4j.com/" + vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" + vRelationship.getFirstValue("_id").toString() + "#id",
							"https://neo4j.com/ns/",
							"randomdomain",
							aContext
					);
					
					vNewTriples.appendChildren(propertyresult.getReader());
				}

				for (IHDSReader vList: vRelationship.getNodes("*[_ontologyobject='list']")) {
					String vPropertyName = vList.getFirstValue("name()").toString();
					for (IHDSReader vProperty: vList.getNodes("*[_ontologyobject='property']")) {
						IHDSDocument propertyresult = handleProperty(vPropertyName,
								vProperty.getFirstValue("value").toString(),
								vProperty.getFirstValue("_type").toString(),
								"https://neo4j.com/" + vRelationship.getFirstValue("_type").toString().toLowerCase() + "/" + vRelationship.getFirstValue("_id").toString() + "#id",
								"https://neo4j.com/ns/",
								"randomdomain",
								aContext
						);
						
						vNewTriples.appendChildren(propertyresult.getReader());					
					}
				}
			}
		}
		vNewTriples.popNode();
		
		IHDSDocument vNewTriplesRepresentation = vNewTriples.toDocument(false);

		INKFRequest updatemodelrequest = aContext.createRequest("active:jRDFUpdateModel");
		updatemodelrequest.addArgumentByValue("operand", vResultJenaModel);
		updatemodelrequest.addArgumentByValue("operator", vNewTriplesRepresentation);
		vResultJenaModel = (IRepJenaModel)aContext.issueRequest(updatemodelrequest);
		
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vResultJenaModel);
		vResponse.setExpiry(INKFResponse.EXPIRY_MIN_CONSTANT_DEPENDENT, System.currentTimeMillis() + 30);
		//		

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "LPGToJenaModelAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//		
	}

	private IHDSDocument handleProperty(String aPropertyname, String aPropertyvalue, String aPropertytype, String aResource, String aNamespace, String aDomain, INKFRequestContext aContext) throws Exception {
		INKFRequest freemarkerpropertyrequest = aContext.createRequest("active:freemarker");
		freemarkerpropertyrequest.addArgument("operator", "res:/resources/freemarker/triple.freemarker");
		freemarkerpropertyrequest.addArgumentByValue("resource", aResource);
		freemarkerpropertyrequest.addArgumentByValue("namespace", aNamespace);
		freemarkerpropertyrequest.addArgumentByValue("name", aDomain + "#" + aPropertyname);
		freemarkerpropertyrequest.addArgumentByValue("value", aPropertyvalue);

		if (aPropertytype.equals("String")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:string");
		}
		else if (aPropertytype.equals("Integer")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:integer");
		}
		else if (aPropertytype.equals("Long")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:long");
		}
		else if (aPropertytype.equals("Float")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:float");
		}
		else if (aPropertytype.equals("Double")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:double");
		}
		else if (aPropertytype.equals("Boolean")) {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:boolean");
		}
		else {
			freemarkerpropertyrequest.addArgumentByValue("valuetype", "xs:string");
		}
		
		freemarkerpropertyrequest.setRepresentationClass(IHDSDocument.class);
		IHDSDocument freemarkerpropertyresult = (IHDSDocument)aContext.issueRequest(freemarkerpropertyrequest);
		
		return freemarkerpropertyresult;
	}

}
