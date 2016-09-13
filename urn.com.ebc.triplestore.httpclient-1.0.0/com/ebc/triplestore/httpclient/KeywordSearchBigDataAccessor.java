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
//import org.netkernel.layer0.representation.impl.*;
import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * Keyword Search BigData Accessor
 * Launches a keyword search request against a http SPARQL endpoint
 * that uses the BigData Full Text Search module.
 * See also: https://wiki.blazegraph.com/wiki/index.php/FullTextSearch
 */
public class KeywordSearchBigDataAccessor extends StandardAccessorImpl {

	public KeywordSearchBigDataAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("database",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("search",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("limit",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("credentials",null,null,new Class[] {IHDSNode.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "KeywordSearchBigDataAccessor: start of id - " + vId);
		//
		
		// arguments
		String aSearch = null;
		try {
			aSearch = aContext.source("arg:search", String.class);
		}
		catch (Exception e) {
			throw new Exception("KeywordSearchBigDataAccessor: no valid - search - argument");
		}
		if (aSearch.equals("")) {
			throw new Exception("KeywordSearchBigDataAccessor: argument - search - can not be empty");
		}
		
		Long aLimit = null;
		String aLimitString = null;
		if (aContext.getThisRequest().argumentExists("limit")) {// limit is an optional argument
			try {
				aLimit = aContext.source("arg:limit", Long.class);
				aLimitString = aLimit.toString();
			}
			catch (Exception e) {
				try {
					aLimitString = aContext.source("arg:limit", String.class);
					aLimit = Long.parseLong(aLimitString);
				}
				catch (Exception e2) {
					// sensible default, 25
					aLimit = 25L;
					aLimitString = aLimit.toString();
				}
			}
		}
		else {
			// sensible default, 25
			aLimit = 25L;
			aLimitString = aLimit.toString();
		}
		//
		
		// processing
		String vQuery = null;
		INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
		freemarkerrequest.addArgument("operator", "res:/resources/freemarker/keywordsearchbigdata.freemarker");
		freemarkerrequest.addArgumentByValue("search", aSearch);
		freemarkerrequest.addArgumentByValue("limit", aLimitString);
		freemarkerrequest.setRepresentationClass(String.class);
		vQuery = (String)aContext.issueRequest(freemarkerrequest);
		
		INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
		sparqlrequest.addArgument("database", "arg:database");
		sparqlrequest.addArgument("accept", "arg:accept");
		sparqlrequest.addArgument("expiry", "arg:expiry");
		sparqlrequest.addArgument("credentials", "arg:credentials");
		sparqlrequest.addArgumentByValue("query", vQuery);
		//
		
		// response
		aContext.createResponseFrom(aContext.issueRequestForResponse(sparqlrequest));
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "KeywordSearchBigDataAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
