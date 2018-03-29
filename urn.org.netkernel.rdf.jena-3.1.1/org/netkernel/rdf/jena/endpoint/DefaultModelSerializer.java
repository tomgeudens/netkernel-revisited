package org.netkernel.rdf.jena.endpoint;

import java.io.ByteArrayOutputStream;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.ByteArrayRepresentation;
import org.netkernel.layer0.representation.IBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;

public class DefaultModelSerializer extends StandardTransreptorImpl
{
	public DefaultModelSerializer()
	{
		this.declareThreadSafe();
		this.declareFromRepresentation(IRepJenaModel.class);
		this.declareToRepresentation(IBinaryStreamRepresentation.class);
	}

	@Override
	public void onTransrept(INKFRequestContext context) throws Exception
	{	IRepJenaModel jmr=context.sourcePrimary(IRepJenaModel.class);
		ByteArrayOutputStream baos=new ByteArrayOutputStream(2048);
		jmr.getModel().write(baos);
		INKFResponse resp=context.createResponseFrom(new ByteArrayRepresentation(baos.toByteArray()));
		resp.setMimeType("application/rdf+xml");
	}

}
