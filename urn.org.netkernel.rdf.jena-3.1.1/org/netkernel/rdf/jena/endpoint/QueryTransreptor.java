package org.netkernel.rdf.jena.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer1.representation.IDeterminateStringRepresentation;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;
import org.netkernel.rdf.jena.util.NKFContextLocator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.util.FileManager;

public class QueryTransreptor extends StandardTransreptorImpl
{
	public QueryTransreptor()
	{	this.declareThreadSafe();
		this.declareToRepresentation(Query.class);
	}

	@Override
	public void onTransrept(INKFRequestContext context) throws Exception
	{	IDeterminateStringRepresentation ds=context.sourcePrimary(IDeterminateStringRepresentation.class);
		/*
		FileManager fm=new FileManager();
		fm.addLocator(new NKFContextLocator(context));
		Query q=QueryFactory.read(url, filemanager, baseURI, langURI);  //Can't do this primary is not referencable!!
		*/
		Query q=QueryFactory.create(ds.getString());
		//Op op=Algebra.compile(q);
		context.createResponseFrom(q);
	}

}
