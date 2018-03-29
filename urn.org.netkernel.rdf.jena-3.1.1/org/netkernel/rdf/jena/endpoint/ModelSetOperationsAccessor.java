package org.netkernel.rdf.jena.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

public class ModelSetOperationsAccessor extends StandardAccessorImpl
{
	private static final String UNION="jRDFModelUnion";
	private static final int UNION_INT=1;
	private static final String INTERSECTION="jRDFModelIntersection";
	private static final int INTERSECTION_INT=2;
	private static final String DIFFERENCE="jRDFModelDifference";
	private static final int DIFFERENCE_INT=3;
	
	private static final Map<String, Integer> map=new HashMap<String, Integer>();
	
	{	map.put(UNION,new Integer(UNION_INT));
		map.put(INTERSECTION, new Integer(INTERSECTION_INT));
		map.put(DIFFERENCE, new Integer(DIFFERENCE_INT));
	}
	
	public ModelSetOperationsAccessor()
	{	this.declareThreadSafe();
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	String key=context.getThisRequest().getArgumentValue("activeType");
		Integer i=(Integer)map.get(key);
		Model result=null;
		IRepJenaModel jmr=context.source("arg:model1", IRepJenaModel.class);
		Model m1=jmr.getModelReadOnly();
		if (!m1.supportsSetOperations()) throw new Exception("Set Operations not supported on Model1");
		jmr=context.source("arg:model2", IRepJenaModel.class);
		Model m2=jmr.getModelReadOnly();
		if (!m2.supportsSetOperations()) throw new Exception("Set Operations not supported on Model2");
		Lock ml1=m1.getLock();
		ml1.enterCriticalSection(Lock.READ);
		Lock ml2=m2.getLock();
		ml2.enterCriticalSection(Lock.READ);
		try
		{	switch(i.intValue())
			{	case UNION_INT:
				{	result=m1.union(m2);
				}
				break;
				case INTERSECTION_INT:
				{	result=m1.intersection(m2);
				}
				break;
				case DIFFERENCE_INT:
				{	result=m1.difference(m2);
				}
				break;
			}
		}
		finally
		{	ml1.leaveCriticalSection();
			ml2.leaveCriticalSection();
		}
		context.createResponseFrom(new JenaModelRepresentation(result));
	}
}
