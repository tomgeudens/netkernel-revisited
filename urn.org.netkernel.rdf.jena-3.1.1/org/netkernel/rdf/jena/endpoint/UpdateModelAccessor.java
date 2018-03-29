package org.netkernel.rdf.jena.endpoint;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;
import org.netkernel.xml.xda.IXDAReadOnly;
import org.netkernel.xml.xda.IXDAReadOnlyIterator;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

public class UpdateModelAccessor extends StandardAccessorImpl
{
	public UpdateModelAccessor()
	{	this.declareThreadSafe();	
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	Model m=null;
		IRepJenaModel jmr=context.source("arg:operand", IRepJenaModel.class);
		m=jmr.getModel();
		IXDAReadOnly x=context.source("arg:operator",IXDAReadOnly.class);
		IXDAReadOnlyIterator roi=x.readOnlyIterator("/jRDFUpdateModel/*");
		Lock ml=m.getLock();
		ml.enterCriticalSection(Lock.WRITE);
		try
		{	while(roi.hasNext())
			{	roi.next();
				if(roi.isTrue("name()='addTriple'"))
				{	String resource=roi.getText("resource", true);
					String propertyNS=roi.getText("property/namespace", true);
					String propertyName=roi.getText("property/name", true);
					String value=roi.getText("value",true);
					String valueType=null;
					if(roi.isTrue("value/@type"))
					{	valueType=roi.getText("value/@type", true);
					}

					Resource r=m.createResource(resource);
					Property p=m.createProperty(propertyNS, propertyName);
					
					if(valueType==null)
					{	r.addProperty(p, value);
					}
					else if(valueType.equals("resource")) 
					{	Resource o=m.createResource(value);
						r.addProperty(p,o);

					}
					else
					{	XSDDatatype type=new XSDDatatype(valueType.substring(3));
						r.addProperty(p, value, type);
					}
				}
				else if(roi.isTrue("name()='removeAllMatching'"))	
				{	String resource=null;
					String propertyNS=null;
					String propertyName=null;
					String value=null;
					if(roi.isTrue("resource"))
					{	resource=roi.getText("resource",true);
					}
					if(roi.isTrue("property"))
					{	propertyNS=roi.getText("property/namespace", true);
						propertyName=roi.getText("property/name", true);
					}
					if(roi.isTrue("value"))
					{	value=roi.getText("value", true);
					}
					Resource r=null;
					Property p=null;
					if(resource!=null)
					{	r=m.createResource(resource);					
					}
					if(propertyNS!=null && propertyName!=null)
					{	p=m.createProperty(propertyNS, propertyName);
					}
					SimpleSelector s=null;
					if(value!=null)
					{	if(roi.isTrue("value/@type"))
						{	String typelex=roi.getText("value/@type", true);
							XSDDatatype type=new XSDDatatype(typelex.substring(3));
							Object o=type.parse(value);
							s=new SimpleSelector(r,p, o);
						}
						else
						{	s=new SimpleSelector(r,p, value);
						}
					}
					else
					{	s=new SimpleSelector(r,p,value);					
					}
					StmtIterator si=m.listStatements(s);
					m.remove(si);
				}
			}
		}
		finally
		{	ml.leaveCriticalSection();			
		}
		context.createResponseFrom(new JenaModelRepresentation(m));
	}

	
}
