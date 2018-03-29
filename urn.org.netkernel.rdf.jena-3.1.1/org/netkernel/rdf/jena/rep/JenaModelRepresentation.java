/******************************************************************************
  (c) Copyright 2002-2005, 1060 Research Ltd                                   

  This Software is licensed to You, the licensee, for use under the terms of   
  the 1060 Public License v1.0. Please read and agree to the 1060 Public       
  License v1.0 [www.1060research.com/license] before using or redistributing   
  this software.                                                               

  In summary the 1060 Public license has the following conditions.             
  A. You may use the Software free of charge provided you agree to the terms   
  laid out in the 1060 Public License v1.0                                     
  B. You are only permitted to use the Software with components or applications
  that provide you with OSI Certified Open Source Code [www.opensource.org], or
  for which licensing has been approved by 1060 Research Limited.              
  You may write your own software for execution by this Software provided any  
  distribution of your software with this Software complies with terms set out 
  in section 2 of the 1060 Public License v1.0                                 
  C. You may redistribute the Software provided you comply with the terms of   
  the 1060 Public License v1.0 and that no warranty is implied or given.       
  D. If you find you are unable to comply with this license you may seek to    
  obtain an alternative license from 1060 Research Limited by contacting       
  license@1060research.com or by visiting www.1060research.com                 

  NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC  
  LICENSE V1.0 FOR DETAILS                                                     

  THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ     
  THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license      

  File:          $RCSfile: JenaModelRepresentation.java,v $
  Version:       $Name:  $ $Revision: 1.4 $
  Last Modified: $Date: 2005/07/28 14:13:51 $
 *****************************************************************************/

package org.netkernel.rdf.jena.rep;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.shared.Lock;

public class JenaModelRepresentation implements IRepJenaModel
{
	private Model mModel;
	
	/** Creates a new instance of JenaModelRepresentation */
	public JenaModelRepresentation(Model aModel)
	{	mModel=aModel;
	}
	
	public Model getModelReadOnly()
	{	return mModel;
	}
	
	public Model getModel()
	{	//Create selector that matches everything.
		SimpleSelector s=new SimpleSelector((Resource)null, (Property)null, (RDFNode)null);
		//Issue query to clone the model
		Lock lock=mModel.getLock();
		lock.enterCriticalSection(Lock.READ);
		try
		{	Model m=mModel.query(s);
			return m;
		}
		finally
		{	lock.leaveCriticalSection();
		}
		
	}

	@Override
	public String toString()
	{	return mModel.toString();
	}
}
