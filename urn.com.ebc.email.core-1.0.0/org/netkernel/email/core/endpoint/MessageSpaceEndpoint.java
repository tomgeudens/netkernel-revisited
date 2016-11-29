package org.netkernel.email.core.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.impl.NKFEndpointImpl;

public class MessageSpaceEndpoint extends NKFEndpointImpl
{
	public MessageSpaceEndpoint()
	{	super(true);
	}
	
	public void onRequest(INKFRequestContext aContext) throws Exception
	{	MessageSpace space=(MessageSpace)aContext.getKernelContext().getThisKernelRequest().getRequestScope().getSpace();
		space.handleRequestFromEndpoint(aContext);
	}

}
