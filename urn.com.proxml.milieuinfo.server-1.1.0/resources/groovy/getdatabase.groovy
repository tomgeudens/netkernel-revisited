/**
 *
 * ProXML
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
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 * Get Database Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "GetDatabaseAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
String aDomain = null;
try {
	aDomain = aContext.source("arg:domain", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "GetDatabaseAccessor: ("
		+ vId
		+ ") - argument domain : invalid");
	throw new Exception("GetDatabaseAccessor: no valid - domain - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "GetDatabaseAccessor: ("
	+ vId
	+ ") - argument domain : "
	+ aDomain);
//

// processing
URL vURL = null;
INKFRequest environmentrequest = aContext.createRequest("active:environment");
environmentrequest.addArgumentByValue("variable","/environment/os-properties/property[name='lod_database_url']/value");
environmentrequest.setRepresentationClass(String.class);
vURL = new URL((String)aContext.issueRequest(environmentrequest));

HashMap<String,String> vDatabase = new HashMap<String, String>();
BufferedReader vBR = null;

try {
	vBR = new BufferedReader(new InputStreamReader(vURL.openStream()));
	while ( ( vLine = vBR.readLine()) != null) {
		String[] vSplit = vLine.split(",");
		vDatabase.put(vSplit[0],vSplit[1]);
	}
}
finally {
	if (vBR != null) {
		vBR.close();
	}
}

String vResult = vDatabase.get(aDomain);
if (vResult == null) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "GetDatabaseAccessor: ("
		+ vId
		+ ") - argument domain : not found");
	throw new Exception("GetDatabaseAccessor: domain argument - " + aDomain + " - is not found");
}

// response
INKFResponse vResponse = aContext.createResponseFrom(vResult);
vResponse.setMimeType("text/plain");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "GetDatabaseAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//