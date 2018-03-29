package org.netkernel.rdf.jena.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

public class DefaultModelParser extends StandardTransreptorImpl
{
	public DefaultModelParser()
	{
		this.declareThreadSafe();
		this.declareToRepresentation(IRepJenaModel.class);
		this.declareFromRepresentation(IReadableBinaryStreamRepresentation.class);
		RDFLanguages.init();
	}

	@Override
	public void onTransrept(INKFRequestContext context) throws Exception
	{	INKFResponseReadOnly<IReadableBinaryStreamRepresentation> resp=context.sourcePrimaryForResponse(IReadableBinaryStreamRepresentation.class);
		IReadableBinaryStreamRepresentation rbs=resp.getRepresentation();
		Model m=ModelFactory.createDefaultModel();
		try
		{	
			Lang lang=RDFLanguages.contentTypeToLang(resp.getMimeType());
			if(lang==null)
			{	lang=RDFLanguages.filenameToLang(context.getThisRequest().getIdentifier());				
			}
			if(lang!=null)
			{	RDFDataMgr.read(m, rbs.getInputStream(), lang);
			}
			else
			{	m.read(rbs.getInputStream(), context.getThisRequest().getIdentifier());
			}
		}
		catch(Exception e)
		{	throw new NKFException("Default RDF Parse failed - are you sure this "+resp.getMimeType()+" is RDF?", e.getMessage(), e);			
		}
		context.createResponseFrom(new JenaModelRepresentation(m));
	}
}
