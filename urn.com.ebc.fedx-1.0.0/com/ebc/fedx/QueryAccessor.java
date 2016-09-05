package com.ebc.fedx;

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
//import org.netkernel.layer0.representation.impl.*;
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.QueryManager;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.util.EndpointFactory;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * Query Accessor
 * Launches request against a set of SPARQL endpoints
 */
public class QueryAccessor extends StandardAccessorImpl {

	public QueryAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("query",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("endpoint",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "QueryAccessor: start of id - " + vId);
		//
		
		// arguments
		String aQuery = null;
		try {
			aQuery = aContext.source("arg:query", String.class);
		}
		catch (Exception e) {
			throw new Exception("QueryAccessor: no valid - query - argument");
		}
		if (aQuery.equals("")) {
			// providing sensible default
			aQuery = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . } LIMIT 5";
		}
		
		String aAccept = null;
		try {
			aAccept = aContext.source("arg:accept", String.class);
		}
		catch (Exception e) {
			throw new Exception("QueryAccessor: no valid - accept - argument");
		}
		if (aAccept.equals("")) {
			// providing sensible default
			// other posibilities :
			//   application/sparql-results+json
			//   text/csv; charset=utf-8
			//   text/tab-separated-values; charset=utf-8
			aAccept = "application/sparql-results+xml";
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
				throw new Exception("QueryAccessor: no valid - expiry - argument");
			}
		}
		
		List<String> aEndpoints = aContext.getThisRequest().getArgumentValues("endpoint");
		//
		
		// processing
		Config.initialize();
		
		ByteArrayOutputStream vResult = new ByteArrayOutputStream();
		try {
			List<Endpoint> vEndpoints = new ArrayList<Endpoint>();
			for (String aEndpointIdentifier : aEndpoints) {
				String aEndpoint = (String)aContext.source(aEndpointIdentifier,String.class);
				
				INKFRequest md5request = aContext.createRequest("active:md5");
				md5request.addArgumentByValue("operand", aEndpoint);
				String aEndpointHash = (String)aContext.issueRequest(md5request);
				try {
					vEndpoints.add(EndpointFactory.loadSPARQLEndpoint(aEndpointHash, aEndpoint));					
				}
				catch (Exception e) {
					aContext.logRaw(INKFLocale.LEVEL_WARNING, "QueryAccessor: adding endpoint " + aEndpoint + " - " + e.getMessage());
					//
				}

			}
			FedXFactory.initializeFederation(vEndpoints);
		
			TupleQuery vQuery = QueryManager.prepareTupleQuery(aQuery);
			TupleQueryResultFormat vFormat = TupleQueryResultFormat.forMIMEType(aAccept, TupleQueryResultFormat.SPARQL);
			TupleQueryResultWriter vWriter = QueryResultIO.createWriter(vFormat, vResult);
			vQuery.evaluate(vWriter);

		}
		catch (Exception e) {
			//
		}
		finally {
			try {
				FederationManager.getInstance().shutDown();
			}
			catch (Exception e) {
				//
			}
		}
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(new ByteArrayRepresentation(vResult));
		vResponse.setMimeType(aAccept);
		vResponse.setExpiry(INKFResponse.EXPIRY_MIN_CONSTANT_DEPENDENT, System.currentTimeMillis() + aExpiry);
		//
		
		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "QueryAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//		
	}
}