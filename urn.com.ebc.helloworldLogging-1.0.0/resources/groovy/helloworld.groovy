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

/**
 * Hello World Accessor
 */

// context
INKFRequestContext aContext = (INKFRequestContext)context;
//

// logging
aContext.logRaw(INKFLocale.LEVEL_INFO, "example logging from hello world groovy accessor");
//

// response
INKFResponse vResponse = aContext.createResponseFrom("Hello World !");
vResponse.setMimeType("text/plain")
//