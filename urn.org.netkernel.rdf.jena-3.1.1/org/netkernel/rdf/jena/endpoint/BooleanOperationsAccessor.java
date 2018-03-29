package org.netkernel.rdf.jena.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

public class BooleanOperationsAccessor extends StandardAccessorImpl
{
	private static final Map<String, Integer> map=new HashMap<String, Integer>();
	
	private static final String CONTAINS_ALL="jRDFModelContainsAll";
	private static final int CONTAINS_ALL_INT=1;
	private static final String CONTAINS_ANY="jRDFModelContainsAny";
	private static final int CONTAINS_ANY_INT=2;
	private static final String ISOMORPHIC="jRDFModelIsIsomorphic";
	private static final int ISOMORPHIC_INT=3;
	private static final String EMPTY="jRDFModelIsEmpty";
	private static final int EMPTY_INT=4;
	
	{	map.put(CONTAINS_ALL,new Integer(CONTAINS_ALL_INT));
		map.put(CONTAINS_ANY,new Integer(CONTAINS_ANY_INT));
		map.put(ISOMORPHIC,new Integer(ISOMORPHIC_INT));
		map.put(EMPTY,new Integer(EMPTY_INT));
	}

	public BooleanOperationsAccessor()
	{	this.declareThreadSafe();
	}
	
	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{
		String key=context.getThisRequest().getArgumentValue("activeType");
		Integer i=map.get(key);
		Boolean result=null;
		if(i!=EMPTY_INT)
		{	IRepJenaModel jmr=context.source("arg:model1", IRepJenaModel.class);
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
			{	switch(i)
				{	case CONTAINS_ALL_INT:
					{	result=m1.containsAll(m2);
					}
					break;
					case CONTAINS_ANY_INT:
					{	result=m1.containsAny(m2);
					}
					break;
					case ISOMORPHIC_INT:
					{	result=m1.isIsomorphicWith(m2);
					}
				}
			}
			finally
			{	ml1.leaveCriticalSection();
				ml2.leaveCriticalSection();
			}
		}
		else
		{	IRepJenaModel jmr=context.source("arg:operand", IRepJenaModel.class);
			Model m=jmr.getModelReadOnly();
			Lock ml=m.getLock();
			ml.enterCriticalSection(Lock.READ);
			try
			{	result=m.isEmpty();
			}
			finally
			{	ml.leaveCriticalSection();				
			}
		}
		context.createResponseFrom(result);
	}
	
	

}
