package org.netkernel.rdf.jena.util;

import org.apache.jena.util.Locator;
import org.apache.jena.util.TypedStream;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;

public class NKFContextLocator implements Locator
{
	INKFRequestContext mContext;

	public NKFContextLocator(INKFRequestContext aContext)
	{	mContext=aContext;
	}
	
	@Override
	public String getName()
	{	return "NKFContextLocator";
	}

	@Override
	public TypedStream open(String uri)
	{	
		System.out.println("NKFContextLocator open: "+uri);
		TypedStream rep=null;
		try
		{
			INKFResponseReadOnly<IReadableBinaryStreamRepresentation> resp=mContext.sourceForResponse(uri,IReadableBinaryStreamRepresentation.class);
			String mimetype=resp.getMimeType();
			if(mimetype!=null)
			{	rep=new TypedStream(resp.getRepresentation().getInputStream(), mimetype, null);				
			}
			else
			{	rep=new TypedStream(resp.getRepresentation().getInputStream());				
			}
		}
		catch(Exception e)
		{	mContext.logRaw(mContext.LEVEL_WARNING,"Cannot load resource "+uri);			
		}
		return rep;
	}

}
