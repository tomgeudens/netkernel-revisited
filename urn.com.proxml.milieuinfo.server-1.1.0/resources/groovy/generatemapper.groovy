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
import java.util.ArrayList;

/**
 * Generate Mapper Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "GenerateMapperAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
String aRoot = null;
try {
	aRoot = aContext.getThisRequest().getArgumentValue("root");
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "GenerateMapperAccessor: ("
		+ vId
		+ ") - argument root : invalid");
	throw new Exception("GenerateMapperAccessor: no valid - root - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "GenerateMapperAccessor: ("
	+ vId
	+ ") - argument root : "
	+ aRoot);
//

// processing
URL vURL = null;
INKFRequest environmentrequest = aContext.createRequest("active:environment");
environmentrequest.addArgumentByValue("variable","/environment/os-properties/property[name='lod_layout_url']/value");
environmentrequest.setRepresentationClass(String.class);
vURL = new URL((String)aContext.issueRequest(environmentrequest));

HashMap<String,ArrayList<String>> vLayout = new HashMap<String, ArrayList<String>>();
BufferedReader vBR = null;

try {
	vBR = new BufferedReader(new InputStreamReader(vURL.openStream()));
	while ( ( vLine = vBR.readLine()) != null) {
		String[] vSplit = vLine.split(",");
		
		if (vSplit.length < 2) {
			vLayout.put(vSplit[0], null);
		}
		else {
			if ( vLayout.containsKey(vSplit[0])) {
				ArrayList<String> vTemp = vLayout.get(vSplit[0]);
				vTemp.add(vSplit[1]);
				vLayout.put(vSplit[0], vTemp);			
			}
			else {
				ArrayList<String> vTemp = new ArrayList<String>();
				vTemp.add(vSplit[1]);
				vLayout.put(vSplit[0], vTemp);
			} 
		}
	}
}
finally {
	if (vBR != null) {
		vBR.close();
	}
}
//

if (! vLayout.containsKey(aRoot) ) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "GenerateMapperAccessor: ("
		+ vId
		+ ") - argument root : not found");
	throw new Exception("GenerateMapperAccessor: root argument - " + aRoot + " - is not found");
}

ArrayList<String> vTemp = vLayout.get(aRoot);
String vAllowedwithslash = null;
String vOwnerwithslash = null;

if (vTemp == null) {
	vAllowedwithslash = "";
	vOwnerwithslash = "";
}
else {
  vAllowedwithslash = "(" + String.join("|", vTemp) + ")/";
  vOwnerwithslash = "{owner:(" + String.join("|", vTemp) + ")}/";
}

INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
freemarkerrequest.addArgument("operator", "res:/resources/freemarker/mapper.freemarker");
freemarkerrequest.addArgumentByValue("root", aRoot);
freemarkerrequest.addArgumentByValue("allowedwithslash", vAllowedwithslash);
freemarkerrequest.addArgumentByValue("ownerwithslash", vOwnerwithslash);
freemarkerrequest.setRepresentationClass(String.class);
String vResult = (String)aContext.issueRequest(freemarkerrequest);

// response
INKFResponse vResponse = aContext.createResponseFrom(vResult);
vResponse.setMimeType("text/plain");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "GenerateMapperAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//
