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
import org.netkernel.layer0.util.RequestBuilder;
//import org.netkernel.layer0.representation.impl.*;
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;
import org.netkernel.layer0.util.RequestBuilder;
import org.w3c.dom.Document;

/**
 * Message Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "MessageAccessor: start of id - " + vId);
//

// arguments
String aFrom = (String)aContext.source("arg:from",String.class);
String aTo = (String)aContext.source("arg:to",String.class);
String aBody = (String)aContext.source("emailMessage:/part/0/body",String.class);
println(aBody);
Document aBodyDoc = aContext.transrept(aBody, Document.class);

// processing
org.netkernel.container.ILogger vLogger=aContext.getKernelContext().getKernel().getLogger()
RequestBuilder vBuilder = new RequestBuilder(aBodyDoc.getDocumentElement(), vLogger);
INKFRequest vRequest = vBuilder.buildRequest(aContext,null,null);
aContext.issueRequest(vRequest);
//

// response
INKFResponse vResponse = aContext.createResponseFrom("Message from " + aFrom + " to " + aTo + " about " + aSubject);
vResponse.setMimeType("text/plain");
vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "MessageAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//