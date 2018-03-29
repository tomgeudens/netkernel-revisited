package org.netkernel.rdf.jena.endpoint;

import java.io.ByteArrayOutputStream;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.ByteArrayRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaDataset;

import org.apache.jena.query.Dataset;
import org.apache.jena.shared.Lock;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

public class RDFSerializeDatasetAccessor extends StandardAccessorImpl
{
	public RDFSerializeDatasetAccessor()
	{	this.declareThreadSafe();
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	IRepJenaDataset mr=context.source("arg:operand", IRepJenaDataset.class);
		ByteArrayOutputStream baos=new ByteArrayOutputStream(2048);
		String type=context.getThisRequest().getArgumentValue("activeType");
		Dataset m=mr.getDatasetReadOnly();
		Lock lock=m.getLock();
		String mime=null;
		lock.enterCriticalSection(Lock.READ);
		try
		{	if(type.equals("jRDFSerializeTRIG"))
			{	RDFDataMgr.write(baos, m, RDFLanguages.TRIG);
				mime="application/trig";
			}
		}
		finally
		{	lock.leaveCriticalSection();
		}
		ByteArrayRepresentation bar=new ByteArrayRepresentation(baos.toByteArray());
		INKFResponse resp=context.createResponseFrom(bar);
		resp.setMimeType(mime);
	}
}
