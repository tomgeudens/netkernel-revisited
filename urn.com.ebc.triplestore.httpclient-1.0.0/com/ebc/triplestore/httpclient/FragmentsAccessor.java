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
 * Fragments Accessor
 * Launches a fragments request against a http SPARQL endpoint
 * See also: http://linkeddatafragments.org
 */
public class FragmentsAccessor extends StandardAccessorImpl {

	public FragmentsAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareSourceRepresentation(org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation.class);
		this.declareArgument(new SourcedArgumentMetaImpl("database",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("accept",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("expiry",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("credentials",null,null,new Class[] {IHDSNode.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("subject",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("predicate",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("object",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("limit",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("offset",null,null,new Class[] {Long.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("dataset",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("url",null,null,new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("query",null,null,new Class[] {String.class}));
	}

	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FragmentsAccessor: start of id - " + vId);
		//
		
		// arguments
		String aSubject = null;
		if (aContext.getThisRequest().argumentExists("subject")) {// subject is an optional argument
			try {
				aSubject = aContext.source("arg:subject", String.class);
			}
			catch (Exception e) {
				// sensible default
				aSubject = "?s";
			}
		}
		else {
			// sensible default
			aSubject = "?s";
		}
		if ((!aSubject.equals("?s")) && (!aSubject.startsWith("<"))) {
			// if it is not a variable then it is an uri
			aSubject = "<" + aSubject + ">";
		}
		
		String aPredicate = null;
		if (aContext.getThisRequest().argumentExists("predicate")) {// predicate is an optional argument
			try {
				aPredicate = aContext.source("arg:predicate", String.class);
			}
			catch (Exception e) {
				// sensible default
				aPredicate = "?p";
			}
		}
		else {
			// sensible default
			aPredicate = "?p";
		}
		if ((!aPredicate.equals("?p")) && (!aPredicate.startsWith("<"))) {
			// if it is not a variable then it is an uri
			aPredicate = "<" + aPredicate + ">";
		}

		String aObject = null;
		if (aContext.getThisRequest().argumentExists("object")) {// object is an optional argument
			try {
				aObject = aContext.source("arg:object", String.class);
			}
			catch (Exception e) {
				// sensible default
				aObject = "?o";
			}
		}
		else {
			// sensible default
			aObject = "?o";
		}
		if ((!aObject.equals("?o")) && 
			(!aObject.startsWith("<")) &&
			(!aObject.startsWith("'")) && 
			(!aObject.startsWith("\""))) {
			// if it is not a variable and it is not a literal then it is an uri
			aObject = "<" + aObject + ">";
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
					// sensible default, 100
					aLimit = 100L;
					aLimitString = aLimit.toString();
				}
			}
		}
		else {
			// sensible default, 100
			aLimit = 100L;
			aLimitString = aLimit.toString();
		}

		Long aOffset = null;
		String aOffsetString = null;
		if (aContext.getThisRequest().argumentExists("offset")) {// offset is an optional argument
			try {
				aOffset = aContext.source("arg:offset", Long.class);
				aOffsetString = aOffset.toString();
			}
			catch (Exception e) {
				try {
					aOffsetString = aContext.source("arg:offset", String.class);
					aOffset = Long.parseLong(aOffsetString);
				}
				catch (Exception e2) {
					// sensible default, 0
					aOffset = 0L;
					aOffsetString = aOffset.toString();
				}
			}
		}
		else {
			// sensible default, 0
			aOffset = 0L;
			aOffsetString = aOffset.toString();
		}
		
		String aDataset = null;
		if (aContext.getThisRequest().argumentExists("dataset")) {// dataset is an optional argument
			try {
				aDataset = aContext.source("arg:dataset", String.class);
			}
			catch (Exception e) {
				// sensible default
				aDataset = "http://localhost";
			}
		}
		else {
			// sensible default
			aDataset = "http://localhost";
		}
		if (aDataset.equals("")) {
			// sensible default
			aDataset = "http://localhost";
		}

		String aURL = null;
		if (aContext.getThisRequest().argumentExists("url")) {// url is an optional argument
			try {
				aURL = aContext.source("arg:url", String.class);
			}
			catch (Exception e) {
				// sensible default
				aURL = "http://localhost/fragments";
			}
		}
		else {
			// sensible default
			aURL = "http://localhost/fragments";
		}
		if (aURL.equals("")) {
			// sensible default
			aURL = "http://localhost/fragments";
		}
		
		String aQuery = null;
		if (aContext.getThisRequest().argumentExists("query")) {// query is an optional argument
			try {
				aQuery = aContext.source("arg:query", String.class);
			}
			catch (Exception e) {
				// sensible default
				aQuery = "subject=?s&predicate=?p&object=?o";
			}
		}
		else {
			// sensible default
			aQuery = "subject=?s&predicate=?p&object=?o";
		}
		if (aQuery.equals("")) {
			// sensible default
			aQuery = "subject=?s&predicate=?p&object=?o";
		}
		//
		
		// processing
		INKFRequest fragmentscountrequest = aContext.createRequest("active:freemarker");
		fragmentscountrequest.addArgument("operator", "res:/resources/freemarker/fragmentscount.freemarker");
		fragmentscountrequest.addArgumentByValue("subject", aSubject);
		fragmentscountrequest.addArgumentByValue("predicate", aPredicate);
		fragmentscountrequest.addArgumentByValue("object", aObject);
		fragmentscountrequest.setRepresentationClass(String.class);
		
		INKFRequest sparqlcountrequest = aContext.createRequest("active:sparql");
		sparqlcountrequest.addArgument("database", "arg:database");
		sparqlcountrequest.addArgument("expiry", "arg:expiry");
		sparqlcountrequest.addArgument("credentials", "arg:credentials");
		sparqlcountrequest.addArgumentByValue("accept", "application/sparql-results+xml");
		sparqlcountrequest.addArgumentByRequest("query", fragmentscountrequest);
		
		INKFRequest xsltcrequest = aContext.createRequest("active:xsltc");
		xsltcrequest.addArgumentByRequest("operand", sparqlcountrequest);
		xsltcrequest.addArgument("operator", "res:/resources/xsl/sparqlresult_to_count.xsl");
		xsltcrequest.setRepresentationClass(String.class);
		String vCountString = (String)aContext.issueRequest(xsltcrequest);
		Long vCount = Long.parseLong(vCountString);
		
		INKFRequest fragmentsrequest = aContext.createRequest("active:freemarker");
		fragmentsrequest.addArgument("operator", "res:/resources/freemarker/fragments.freemarker");
		fragmentsrequest.addArgumentByValue("subject", aSubject);
		fragmentsrequest.addArgumentByValue("predicate", aPredicate);
		fragmentsrequest.addArgumentByValue("object", aObject);
		fragmentsrequest.addArgumentByValue("limit", aLimitString);
		fragmentsrequest.addArgumentByValue("offset", aOffsetString);
		fragmentsrequest.addArgumentByValue("dataset", aDataset);
		fragmentsrequest.addArgumentByValue("url", aURL);
		fragmentsrequest.addArgumentByValue("query", (aQuery.equals("")) ? "" : "?" + aQuery);
		fragmentsrequest.addArgumentByValue("count", vCountString);
		// we need to determine the urls for the previous and next set of fragments
		// first we remove the offset parameter from the query
		// see also : http://stackoverflow.com/questions/9191327/java-regex-pattern-to-remove-a-parameter-from-query-string
		String vQueryWithoutOffset = ("?" + aQuery).replaceAll("(?<=[?&;])offset=.*?($|[&;])", "").replaceAll("&$","");
		// next we determine the previous and next offset
		Long vPreviousOffset = aOffset - aLimit;
		Long vNextOffset = aOffset + aLimit;
		// we only add the next and previous urls if they return fragments
		if (vPreviousOffset >= 0L) {
			fragmentsrequest.addArgumentByValue("previous", aURL + vQueryWithoutOffset + (aQuery.equals("") ? "" : "&") + "offset=" + vPreviousOffset.toString());
		}
		if (vNextOffset < vCount) {
			fragmentsrequest.addArgumentByValue("next", aURL + vQueryWithoutOffset + (aQuery.equals("") ? "" : "&") + "offset=" + vNextOffset.toString());			
		}
		fragmentsrequest.setRepresentationClass(String.class);

		INKFRequest sparqlrequest = aContext.createRequest("active:sparql");
		sparqlrequest.addArgument("database", "arg:database");
		sparqlrequest.addArgument("expiry", "arg:expiry");
		sparqlrequest.addArgument("credentials", "arg:credentials");
		sparqlrequest.addArgument("accept", "arg:accept");
		sparqlrequest.addArgumentByRequest("query", fragmentsrequest);
		//
		
		// response
		aContext.createResponseFrom(aContext.issueRequestForResponse(sparqlrequest));
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "FragmentsAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
