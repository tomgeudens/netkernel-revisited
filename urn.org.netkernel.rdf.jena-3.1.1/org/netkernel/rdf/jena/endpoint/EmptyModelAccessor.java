package org.netkernel.rdf.jena.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class EmptyModelAccessor extends StandardAccessorImpl
{
	public EmptyModelAccessor()
	{	this.declareThreadSafe();
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	Model m=ModelFactory.createDefaultModel();
		context.createResponseFrom(new JenaModelRepresentation(m));
	}
}

