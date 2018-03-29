package org.netkernel.rdf.jena.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.rdf.jena.rep.IRepJenaModel;
import org.netkernel.rdf.jena.rep.JenaModelRepresentation;
import org.apache.jena.assembler.ImportManager;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.function.library.leviathan.e;

public class QueryModelAccessor extends StandardAccessorImpl
{
	private static final String ASK="jRDFQueryModelASK";
	private static final int ASK_INT=1;
	private static final String CONSTRUCT="jRDFQueryModelCONSTRUCT";
	private static final int CONSTRUCT_INT=2;
	private static final String DESCRIBE="jRDFQueryModelDESCRIBE";
	private static final int DESCRIBE_INT=3;
	private static final String SELECT="jRDFQueryModelSELECT";
	private static final int SELECT_INT=4;
	
	private static final Map<String, Integer> map=new HashMap<String, Integer>();
	
	//Do not try this at home - very dangerous hacking to force Jena into the ROC domain...
	private static ThreadLocal<Stack<INKFRequestContext>> mContextThreadLocal;
	
	{	map.put(ASK, ASK_INT);
		map.put(CONSTRUCT, CONSTRUCT_INT);
		map.put(DESCRIBE, DESCRIBE_INT);
		map.put(SELECT, SELECT_INT);
	}

	public QueryModelAccessor()
	{	this.declareThreadSafe();
		mContextThreadLocal=new ThreadLocal<Stack<INKFRequestContext>>()
			{
            	@Override
            	protected Stack<INKFRequestContext> initialValue()
            	{	return new Stack<INKFRequestContext>();
            	}
			};
	}

	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	Model m=null;
		if(context.getThisRequest().argumentExists("operand"))
		{	IRepJenaModel jmr=context.source("arg:operand", IRepJenaModel.class);
			m=jmr.getModelReadOnly();
		}
		Query q=context.source("arg:operator",Query.class);
		boolean modelflag=context.getThisRequest().argumentExists("model");
		Object result=null;
		String key=context.getThisRequest().getArgumentValue("activeType");
		Integer type=map.get(key);
		synchronized(q)
		{	Lock ml=null;
			QueryExecution qe=null;
			if(m!=null)
			{	qe=QueryExecutionFactory.create(q, m);
				ml=m.getLock();
				ml.enterCriticalSection(Lock.READ);
			}
			else
			{	qe=QueryExecutionFactory.create(q);
			}
			/* Put context in Stack in ThreadLocal so that low level Jena can be forced to use this to load resources!!  Sooo Ugly!!!
			 * We call getMyThreadLocalContext() in overridden: org.apache.riot.RDFDataMgr
			 * @ Line: 836  public static TypedInputStream open(String filenameOrURI, StreamManager streamManager) 
			 */
			Stack<INKFRequestContext> stack=mContextThreadLocal.get();
			try
			{	stack.push(context);
				//System.out.println(stack.size()+" PUSH THREAD LOCAL ON "+Thread.currentThread().toString());
				switch(type)
				{	case ASK_INT:
						result=qe.execAsk();
					break;
					case CONSTRUCT_INT:
						result=new JenaModelRepresentation(qe.execConstruct());
					break;
					case DESCRIBE_INT:
						result=new JenaModelRepresentation(qe.execDescribe());
					break;
					case SELECT_INT:
						ResultSet rs=qe.execSelect();
						if(modelflag)
						{	result=new JenaModelRepresentation(rs.getResourceModel());						
						}
						else
						{	result=ResultSetFormatter.asXMLString(rs);							
						}
					break;
				}
			}
			finally
			{	//Remove context from ThreadLocal stack to ensure we don't leak state
				//System.out.println(stack.size()+" POP THREAD LOCAL ON "+Thread.currentThread().toString());
				stack.pop();
				if(stack.isEmpty())		//Now if empty we need to remove the stack from threadlocal
				{	//System.out.println(stack.size()+" REMOVE THREAD LOCAL ON "+Thread.currentThread().toString());
					mContextThreadLocal.remove();
				}
				if(ml!=null)
				{	ml.leaveCriticalSection();
				}
			}
		}
		context.createResponseFrom(result);
	}
	
	public static INKFRequestContext getMyThreadLocalContext()
	{	Stack<INKFRequestContext> stack=mContextThreadLocal.get();
		//System.out.println(stack.size()+" GET THREAD LOCAL ON "+Thread.currentThread().toString());
		return stack.peek();
	}
	
}
