package org.netkernel.rdf.jena.endpoint;

import java.io.ByteArrayOutputStream;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.representation.ByteArrayRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;

public class RDFSerializerAccessor extends StandardAccessorImpl
{
	public RDFSerializerAccessor()
	{	this.declareThreadSafe();
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	IRepJenaModel jmr=context.source("arg:operand", IRepJenaModel.class);
		ByteArrayOutputStream baos=new ByteArrayOutputStream(2048);
		String type=context.getThisRequest().getArgumentValue("activeType");
		Model m=jmr.getModelReadOnly();
		Lock lock=m.getLock();
		String mime=null;
		lock.enterCriticalSection(Lock.READ);
		try
		{	if(type.equals("jRDFSerializeXML"))
			{	RDFDataMgr.write(baos, m, RDFFormat.RDFXML_PLAIN);
				mime="application/rdf+xml";
			}
			else if (type.equals("jRDFSerializeN3"))
			{	RDFDataMgr.write(baos, m, RDFLanguages.N3);
				mime="text/rdf+n3";
			}
			else if (type.equals("jRDFSerializeN-TRIPLE"))
			{	RDFDataMgr.write(baos, m, RDFLanguages.NTRIPLES);
				mime="text/plain";
			}
			else if (type.equals("jRDFSerializeTURTLE"))
			{	RDFDataMgr.write(baos, m, RDFLanguages.TURTLE);
				mime="text/turtle";
			}
			else if (type.equals("jRDFSerializeJSONLD"))
			{	RDFDataMgr.write(baos, m, RDFLanguages.JSONLD);
				mime="application/ld+json";
			}
			else if (type.equals("jRDFSerializeTRIG"))
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
