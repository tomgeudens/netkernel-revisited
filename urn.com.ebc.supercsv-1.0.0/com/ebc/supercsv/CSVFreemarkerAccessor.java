package com.ebc.supercsv;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.io.CsvMapReader;

/**
 * CSV Freemarker Accessor
 * Applies a freemarker template to every row of an input file and writes the
 * result to an output file. The first row of the input file should contain
 * the column names (which can then be used in the freemarker template).
 */
public class CSVFreemarkerAccessor extends StandardAccessorImpl {
	
	public CSVFreemarkerAccessor() {
		this.declareThreadSafe();
		this.declareSupportedVerbs(INKFRequestReadOnly.VERB_SOURCE);
		this.declareArgument(new SourcedArgumentMetaImpl("in", null, null, new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("out", null, null, new Class[] {String.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("template", null, null, new Class[] {String.class}));
		this.declareSourceRepresentation(String.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception {
		// register start
		long vStartTime = System.nanoTime();
		UUID vId = UUID.randomUUID();
		aContext.logRaw(INKFLocale.LEVEL_INFO, "CVSFreemarkerAccessor: start of id - " + vId);
		//
		
		// arguments
		String aIn = null;
		try {
			aIn = aContext.source("arg:in", String.class);
		}
		catch (Exception e) {
			throw new Exception("CSVFreemarkerAccessor: no valid - in - argument");
		}
		if (! aIn.startsWith("file:/")) {
			throw new Exception("CSVFreemarkerAccessor: argument - in - should be a file:/ resource");
		}
		
		String aOut = null;
		try {
			aOut = aContext.source("arg:out", String.class);
		}
		catch (Exception e) {
			throw new Exception("CSVFreemarkerAccessor: no valid - out - argument");
		}
		if (! aOut.startsWith("file:/")) {
			throw new Exception("CSVFreemarkerAccessor: argument - out - should be a file:/ resource");
		}
		
		String aTemplate = null;
		try {
			aTemplate = aContext.source("arg:template", String.class);
		}
		catch (Exception e) {
			throw new Exception("CSVFreemarkerAccessor: no valid - template - argument");
		}
		if (! aTemplate.startsWith("file:/")) {
			throw new Exception("CSVFreemarkerAccessor: argument - template - should be a file:/ resource");
		}
		//
		
		// processing
		ICsvMapReader vMapReader = null;
		BufferedWriter vOutWriter = null;
		try {
			File vOutFile = new File(new URI(aOut));
			vOutWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vOutFile), "UTF8"));
			vMapReader = new CsvMapReader(new FileReader((new URI(aIn)).getRawPath()), CsvPreference.STANDARD_PREFERENCE);
			
			// read the column names
			final String[] vHeader = vMapReader.getHeader(true);
			
			Map<String, String> vMap;
			while( (vMap = vMapReader.read(vHeader)) != null) {
				// for each data line
				INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
				freemarkerrequest.addArgument("operator", aTemplate);
				for (Map.Entry<String, String> vMapEntry : vMap.entrySet()) {
					// for each column
					freemarkerrequest.addArgumentByValue(vMapEntry.getKey().toUpperCase(), vMapEntry.getValue());
				}
				freemarkerrequest.setRepresentationClass(String.class);
				
				// issue the freemarkerrequest
				String vFreemarkerResult = (String)aContext.issueRequest(freemarkerrequest);
				//System.out.println(String.format("lineNo=%s, rowNo=%s, customerMap=%s", 
				//		vMapReader.getLineNumber(), 
				//		vMapReader.getRowNumber(), 
				//		vMap));
				
				// write the result
				vOutWriter.append(vFreemarkerResult);
				//vOutWriter.append(vFreemarkerResult).append("\r\n");
			}
		}
		finally {
			if (vMapReader != null) {
				vMapReader.close();
			}
			if (vOutWriter != null) {
				vOutWriter.flush();
				vOutWriter.close();
			}
		}
		//
		
		// response
		INKFResponse vResponse = aContext.createResponseFrom("processed " + aIn + " into " + aOut + ", using " + aTemplate);
		vResponse.setMimeType("text/plain");
		vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
		//

		// register finish
		long vElapsed = System.nanoTime() - vStartTime;
		double vElapsedSeconds = (double)vElapsed / 1000000000.0;
		aContext.logRaw(INKFLocale.LEVEL_INFO, "CVSFreemarkerAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
		//
	}
}
