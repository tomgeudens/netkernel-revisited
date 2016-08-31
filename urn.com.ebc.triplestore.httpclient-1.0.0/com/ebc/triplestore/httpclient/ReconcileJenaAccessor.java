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
 * Reconcile Jena Accessor
 * Launches a reconciliation request against a http SPARQL endpoint
 * See also: http://linkeddatafragments.org
 * See also: https://github.com/OpenRefine/OpenRefine/wiki/Reconciliation-Service-Api
 */
public class ReconcileJenaAccessor extends StandardAccessorImpl {

	public ReconcileJenaAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("database",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("credentials",null,null,new Class[] {IHDSNode.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("search",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("baseurl",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("limit",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("type",null,null,new Class[] {String.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ReconcileJenaAccessor: start of id - " + vId);
		//
		
		// arguments
		String aAccept = null;
		try {
			aAccept = aContext.source("arg:accept", String.class);
		}
		catch (Exception e) {
			throw new Exception("SPARQLAccessor: no valid - accept - argument");
		}
		if (aAccept.equals("")) {
			// providing sensible default, in this case only application/json will do
			aAccept = "application/json";
		}		
		
		String aSearch = null;
		try {
			aSearch = aContext.source("arg:search", String.class);
		}
		catch (Exception e) {
			throw new Exception("ReconcileJenaAccessor: no valid - search - argument");
		}
		if (aSearch.equals("")) {
			throw new Exception("ReconcileJenaAccessor: argument - search - can not be empty");
		}

		String aBaseURL = null;
		try {
			aBaseURL = aContext.source("arg:baseurl", String.class);
		}
		catch (Exception e) {
			throw new Exception("ReconcileJenaAccessor: no valid - baseurl - argument");
		}
		if (aSearch.equals("")) {
			throw new Exception("ReconcileJenaAccessor: argument - baseurl - can not be empty");
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
		
		String aType = null;
		if (aContext.getThisRequest().argumentExists("type")) {// limit is an optional argument
			try {
				aType = aContext.source("arg:type", String.class);
			}
			catch (Exception e) {
				throw new Exception("ReconcileJenaAccessor: no valid - type - argument");
			}
		} // no else here, we'll use the null value
		//
		
		// processing
		String vTypeFilterClause = null;
		if (aType == null) {
			vTypeFilterClause = "";
		}
		else {
			INKFRequest tfcrequest = aContext.createRequest("active:freemarker");
			tfcrequest.addArgument("operator", "res:/resources/freemarker/typefilterclause.freemarker");
			tfcrequest.addArgumentByValue("type", aType);
			tfcrequest.setRepresentationClass(String.class);
			vTypeFilterClause = (String)aContext.issueRequest(tfcrequest);
		}
		
		INKFRequest reconcilerequest = aContext.createRequest("active:freemarker");
		reconcilerequest.addArgument("operator", "res:/resources/freemarker/reconcilejena.freemarker");
		reconcilerequest.addArgumentByValue("typefilterclause", vTypeFilterClause);
		reconcilerequest.addArgumentByValue("search", aSearch);
		reconcilerequest.addArgumentByValue("limit", aLimitString);
		reconcilerequest.addArgumentByValue("baseurl", aBaseURL);
		
		INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
		sparqlrequest.addArgument("database", "arg:database");
		sparqlrequest.addArgument("expiry", "arg:expiry");
		sparqlrequest.addArgument("credentials", "arg:credentials");
		sparqlrequest.addArgumentByValue("accept", "application/sparql-results+xml");
		sparqlrequest.addArgumentByRequest("query", reconcilerequest);
		
		INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
		xsltcrequest.addArgumentByRequest("operand", sparqlrequest);
		xsltcrequest.addArgument("operator", "res:/resources/xsl/sparqlresult_to_json.xsl");
		xsltcrequest.addArgumentByValue("search", aSearch);
		xsltcrequest.setRepresentationClass(String.class);
		
		StringBuilder vResult = new StringBuilder("\"result\": [");
		vResult.append((String)aContext.issueRequest(xsltcrequest));
		vResult.append("]");
		//

		// response
		INKFResponse vResponse = aContext.createResponseFrom(vResult.toString());
		vResponse.setMimeType(aAccept);
		vResponse.setExpiry(INKFResponse.EXPIRY_DEPENDENT);
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "ReconcileJenaAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
