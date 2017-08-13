package com.ebc.grapql.experiment;

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
import java.util.Map;
import java.util.UUID;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLFieldDefinition;
import graphql.GraphQL;
import graphql.Scalars;

/**
 * Hello Accessor
 * Experiments with the grapql-java kit
 */
public class HelloAccessor extends StandardAccessorImpl {

	public HelloAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "HelloAccessor: ("
				+ vId
				+ ") - start");
		//
		
		// arguments
		//
		
		// processing
		GraphQLObjectType vQueryType = GraphQLObjectType.newObject()
				.name("helloQuery")
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.type(Scalars.GraphQLString)
						.name("hello")
						.staticValue("world"))
				.build();
		
		GraphQLSchema vSchema = GraphQLSchema.newSchema()
				.query(vQueryType)
				.build();
		
		GraphQL vGraphQL = GraphQL.newGraphQL(vSchema).build();
		
		Map<String, Object> vResult = vGraphQL.execute("{hello}").getData();
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vResult.toString());
		vResponse.setMimeType("text/plain");
		vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
		//		

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "HelloAccessor: ("
				+ vId
				+ ") - finish - duration : " + vElapsedSeconds + " seconds");
		//
	}
}
