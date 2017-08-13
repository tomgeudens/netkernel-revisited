/**
 *
 * Elephant Bird Consulting BVBA
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse Unique Constraint Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "ParseUniqueConstraintAccessor: ("
		+ vId
		+ ") - start");
//

// arguments
String aConstraintText = null;
try {
	aConstraintText = aContext.source("arg:constrainttext", String.class);
}
catch (Exception e) {
	aContext.logRaw(INKFLocale.LEVEL_SEVERE, "ParseUniqueConstraintAccessor: ("
		+ vId
		+ ") - no valid constrainttext argument");
	throw new Exception("ParseUniqueConstraintAccessor: no valid - constrainttext - argument");
}
aContext.logRaw(INKFLocale.LEVEL_INFO, "ParseUniqueConstraintAccessor: ("
	+ vId
	+ ") - argument constrainttext : "
	+ aConstraintText);
//

// processing
Pattern vPattern = Pattern.compile("CONSTRAINT ON \\( .*:(.*) \\) ASSERT .*?\\.(.*) IS UNIQUE");
Matcher vMatcher = vPattern.matcher(aConstraintText);

String vResult = "";

if (vMatcher.find()) {
	INKFRequest freemarkerrequest = aContext.createRequest("active:freemarker");
	freemarkerrequest.addArgument("operator", "res:/resources/freemarker/endpoint_uniqueproperty.freemarker");
	freemarkerrequest.addArgumentByValue("label", vMatcher.group(1));
	freemarkerrequest.addArgumentByValue("uniqueproperty", vMatcher.group(2));
	freemarkerrequest.addArgumentByValue("uuid", UUID.randomUUID().toString());
	freemarkerrequest.setRepresentationClass(String.class);
	vResult = (String)aContext.issueRequest(freemarkerrequest);
}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vResult);
vResponse.setMimeType("text/plain");
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "ParseUniqueConstraintAccessor: ("
		+ vId
		+ ") - finish - duration : " + vElapsedSeconds + " seconds");
//