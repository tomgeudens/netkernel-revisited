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
import org.netkernel.layer0.representation.*;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */

/**
 * Test Node Equals
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// arguments
String aTagValue = (String)aContext.source("arg:tagvalue", String.class);
String aPath = aTagValue.substring(0,aTagValue.lastIndexOf("/") + 1);
String aValue = aTagValue.substring(aTagValue.lastIndexOf("/") + 1);

INKFResponseReadOnly aResponse = (INKFResponseReadOnly)aContext.source("arg:response", INKFResponseReadOnly.class);
IHDSNode aNode = aContext.transrept(aResponse.getRepresentation(), IHDSNode.class);
//

// processing
Boolean vResult = false;
String vValue = (String)aNode.getFirstValue(aPath);
if (aNode.getFirstValue(aPath) == aValue) {
	vResult = true;
}
else {
	vResult = false;
	aContext.sink("active:assert/Expected", aValue);
	aContext.sink("active:assert/Received", vValue);
}
//

// response
INKFResponse vResponse = aContext.createResponseFrom(vResult);
vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
//