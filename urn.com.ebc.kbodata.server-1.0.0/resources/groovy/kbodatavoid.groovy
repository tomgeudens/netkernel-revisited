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
//import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
//import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Processing Imports
 */
import java.util.UUID;

/**
 * KBOData Void Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// register start
long vStartTime = System.nanoTime();
UUID vId = UUID.randomUUID();
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataVoidAccessor: start of id - " + vId);
//

// response
INKFResponse vResponse = aContext.createResponseFrom("to be done");
vResponse.setMimeType("text/plain")
vResponse.setExpiry(INKFResponse.EXPIRY_ALWAYS);
//

// register finish
long vElapsed = System.nanoTime() - vStartTime;
double vElapsedSeconds = (double)vElapsed / 1000000000.0;
aContext.logRaw(INKFLocale.LEVEL_INFO, "KBODataVoidAccessor: finish of id - " + vId + ", duration was " + vElapsedSeconds + " seconds");
//
