package com.ebc.triplestore.httpclient;

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
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * SPARQL Accessor
 * Launches request against a http SPARQL endpoint
 */
public class SPARQLAccessor extends StandardAccessorImpl {

	public SPARQLAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("database",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("query",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("credentials",null,null,new Class[] {IHDSNode.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SPARQLAccessor: start of id - " + vId);
		//
		
		// arguments
		String aDatabase = null;
		try {
			aDatabase = aContext.source("arg:database", String.class);
		}
		catch (Exception e) {
			throw new Exception("SPARQLAccessor: no valid - database - argument");
		}
		if (aDatabase.equals("")) {
			throw new Exception("SPARQLAccessor: argument - database - can not be empty");
		}
		
		String aQuery = null;
		try {
			aQuery = aContext.source("arg:query", String.class);
		}
		catch (Exception e) {
			throw new Exception("SPARQLAccessor: no valid - query - argument");
		}
		if (aQuery.equals("")) {
			// providing sensible default
			aQuery = "SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object . } LIMIT 10";
		}
		
		String aAccept = null;
		try {
			aAccept = aContext.source("arg:accept", String.class);
		}
		catch (Exception e) {
			throw new Exception("SPARQLAccessor: no valid - accept - argument");
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
				throw new Exception("SPARQLAccessor: no valid - expiry - argument");
			}
		}
		
		IHDSNode aCredentials = null;
		try {
			aCredentials = aContext.source("arg:credentials", IHDSNode.class);
		}
		catch (Exception e) {
			throw new Exception("SPARQLAccessor: no valid - credentials - argument");
		}
		//
		
		// processing
		HDSBuilder vHeaders = new HDSBuilder();
		vHeaders.pushNode("Accept", aAccept);
		
		HDSBuilder vBody = new HDSBuilder();
		vBody.pushNode("query", aQuery);
		
		INKFRequest staterequest = aContext.createRequest("active:httpState");
		staterequest.addArgumentByValue("credentials", aCredentials);
		staterequest.setVerb(INKFRequestReadOnly.VERB_NEW);
		staterequest.setHeader("exclude-dependencies", true);
		String vStateID = (String)aContext.issueRequest(staterequest);
		
		INKFRequest triplestorerequest = aContext.createRequest("active:httpAsyncPost");
		triplestorerequest.addArgument("url", aDatabase);
		triplestorerequest.addArgumentByValue("headers",vHeaders.getRoot());
		triplestorerequest.addArgument("state", vStateID);
		triplestorerequest.addArgumentByValue("nvp",vBody.getRoot());
		triplestorerequest.addArgument("config", "res:/resources/cfg/http.cfg");
		triplestorerequest.setHeader("exclude-dependencies",true);
		
		@SuppressWarnings("rawtypes")
		INKFResponseReadOnly httpresponse = aContext.issueRequestForResponse(triplestorerequest);
		int vHTTPResponseCode = (Integer)httpresponse.getHeader("HTTP_ACCESSOR_STATUS_CODE_METADATA");
		org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation vTripleStoreResult = (org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation)httpresponse.getRepresentation();
		
		INKFRequest removestaterequest = aContext.createRequest(vStateID);
		removestaterequest.setVerb(INKFRequestReadOnly.VERB_DELETE);
		removestaterequest.setHeader("exclude-dependencies", true);
		aContext.issueRequest(removestaterequest);
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom(vTripleStoreResult);
		vResponse.setMimeType(aAccept);
		vResponse.setHeader("httpresponsecode", vHTTPResponseCode);
		vResponse.setExpiry(INKFResponse.EXPIRY_MIN_CONSTANT_DEPENDENT, System.currentTimeMillis() + aExpiry);
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "SPARQLAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
