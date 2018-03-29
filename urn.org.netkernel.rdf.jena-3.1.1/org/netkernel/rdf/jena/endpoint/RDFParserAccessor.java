package org.netkernel.rdf.jena.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.JenaDatasetRepresentation;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

public class RDFParserAccessor extends StandardAccessorImpl
{
	public RDFParserAccessor()
	{	this.declareThreadSafe();
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	Model m=ModelFactory.createDefaultModel();
		INKFResponseReadOnly<IReadableBinaryStreamRepresentation> resp=context.sourceForResponse("arg:operand", IReadableBinaryStreamRepresentation.class);
		IReadableBinaryStreamRepresentation opd=resp.getRepresentation();
		String base=context.getThisRequest().getArgumentValue("base"); //The Base URI from which to resolve relatives - null if doesn't exist.
		String type=context.getThisRequest().getArgumentValue("activeType");
		
		Object rep=null;
		if(type.equals("jRDFParse"))
		{	String mime=resp.getMimeType();
			Lang lang=RDFLanguages.contentTypeToLang(mime);
			if(lang==null)
			{	lang=RDFLanguages.filenameToLang(context.getThisRequest().getArgumentValue("operand"));					
			}
			RDFDataMgr.read(m, opd.getInputStream(), lang);
		}
		else if(type.equals("jRDFParseXML"))
		{	RDFDataMgr.read(m, opd.getInputStream(),base, RDFLanguages.RDFXML);
		}
		else if (type.equals("jRDFParseN3"))
		{	RDFDataMgr.read(m, opd.getInputStream(),base, RDFLanguages.N3);
		}
		else if (type.equals("jRDFParseN-Triple"))
		{	RDFDataMgr.read(m, opd.getInputStream(),base, RDFLanguages.NTRIPLES);
		}
		else if (type.equals("jRDFParseTURTLE"))
		{	RDFDataMgr.read(m, opd.getInputStream(),base, RDFLanguages.TURTLE);
		}
		else if (type.equals("jRDFParseJSONLD"))
		{	RDFDataMgr.read(m, opd.getInputStream(),base, RDFLanguages.JSONLD);
		}
		else if (type.equals("jRDFParseTRIG"))
		{	Dataset ds=TDBFactory.createDataset();
			RDFDataMgr.read(ds, opd.getInputStream(),base, RDFLanguages.TRIG);
			rep=new JenaDatasetRepresentation(ds);
		}
		if(rep==null)
		{	rep=new JenaModelRepresentation(m);
		}
		context.createResponseFrom(rep);
	}
}
