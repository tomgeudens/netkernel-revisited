package org.netkernel.email.core.endpoint;

import org.netkernel.container.IKernel;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFRequestReadOnly;
import org.netkernel.layer0.nkf.INKFResolutionContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.impl.NKFSpaceImpl;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.util.Utils;
import org.netkernel.urii.IEndpoint;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class MessageSpace extends NKFSpaceImpl
{	private Message mMessage;
	private static IEndpoint sEndpoint = new MessageSpaceEndpoint();
	public static String MESSAGE_SCHEME="emailMessage:/";
	public static String PATH_PART_COUNT="partCount";
	public static String PATH_PART="part/";
	public static String PATH_PART_MIMETYPE="mimetype";
	public static String PATH_PART_FILENAME="filename";
	public static String PATH_PART_BODY="body";
	public static String PATH_HEADERS="headers";
	public static String PATH_HEADER="header/";
	public static String PATH_FROM="from";
	public static String PATH_TO="to";
	public static String PATH_DATE="date";
	public static String PATH_SUBJECT="subject";
	public static String PATH_RAW_MESSAGE="raw";
	
	public MessageSpace(Message aMessage)
	{	mMessage=aMessage;
	}

	@Override
	public void onCommissionSpace(IKernel kernel) throws Exception
	{	super.onCommissionSpace(kernel);
		sEndpoint.onCommissionEndpoint(kernel, this);
	}

	@Override
	public void onResolve(INKFResolutionContext context) throws Exception
	{	
		if (context.getRequestToResolve().getVerb()==INKFRequestReadOnly.VERB_SOURCE)
		{	String requestId = context.getRequestToResolve().getIdentifier();
			if(requestId.startsWith(MESSAGE_SCHEME))
			{	context.createResolutionResponse(sEndpoint);
			}
		}
	}
	
	public void handleRequestFromEndpoint(INKFRequestContext context) throws Exception
	{	String identifier=context.getThisRequest().getIdentifier();
		Object result=null;
		String mimetype=null;
		identifier=identifier.substring(MESSAGE_SCHEME.length());
		if(identifier.startsWith(PATH_FROM))
		{	result=mMessage.getFrom()[0].toString();
		}
		if(identifier.startsWith(PATH_TO))
		{	Address[] addresses=mMessage.getAllRecipients();
			HDSBuilder b=new HDSBuilder();
			b.pushNode("to");
			for(int i=0; i< addresses.length; i++)
			{	b.addNode("address", addresses[i].toString());
			}
			b.popNode();
			result=b.getRoot();
		}
		else if(identifier.startsWith(PATH_SUBJECT))
		{	result=mMessage.getSubject();
		}
		else if(identifier.startsWith(PATH_PART))
		{	identifier=identifier.substring(PATH_PART.length());
			String[] parts=identifier.split("/");
			int part=Integer.parseInt(parts[0]);
			Object content=mMessage.getContent();
			if((content instanceof String))
			{	if(part==0)
				{	if(parts[1].equals(PATH_PART_BODY))
					{	result=content;
						mimetype=mMessage.getContentType();
					}
					else if(parts[1].equals(PATH_PART_MIMETYPE))
					{	result=mMessage.getContentType();
					}
					else if(parts[1].equals(PATH_PART_FILENAME))
					{	result=mMessage.getFileName();
					}
				}
				else
				{	throw new Exception("Message part "+part +" does not exist");					
				}
			}
			if((content instanceof Multipart))
			{	Multipart mp=(Multipart)content;
				BodyPart bp=mp.getBodyPart(part);
				if(parts[1].equals(PATH_PART_BODY))
				{	result=new ReadableBinaryStreamRepresentation(bp.getInputStream(), bp.getSize());
					mimetype=bp.getContentType();
				}
				else if(parts[1].equals(PATH_PART_MIMETYPE))
				{	result=bp.getContentType();
				}
				else if(parts[1].equals(PATH_PART_FILENAME))
				{	result=bp.getFileName();
				}
			}
		}
		else if(identifier.startsWith(PATH_RAW_MESSAGE))
		{	result=mMessage;
		}
		else if(identifier.startsWith(PATH_DATE))
		{	result=mMessage.getSentDate();
		}
		else if(identifier.startsWith(PATH_HEADER))
		{	identifier=identifier.substring(PATH_HEADER.length());
			String[] headers=mMessage.getHeader(identifier);
			HDSBuilder b=new HDSBuilder();
			b.pushNode("header");
			b.addNode("name", identifier);
			for(int i=0; i<headers.length; i++)
			{	b.addNode("value", MimeUtility.unfold(headers[i]));
			}
			b.popNode();
			result=b.getRoot();
		}
		else if(identifier.startsWith(PATH_HEADERS))
		{	Enumeration headers=mMessage.getAllHeaders();
			HDSBuilder b=new HDSBuilder();
			b.pushNode("headers");
			while(headers.hasMoreElements())
			{	Header header=(Header)headers.nextElement();
				b.pushNode("header");
				b.addNode("name",header.getName());
				b.addNode("value",MimeUtility.unfold(header.getValue()));
				b.popNode();
			}
			b.popNode();
			result=b.getRoot();
		}
		else if(identifier.startsWith(PATH_PART_COUNT))
		{	Object content=mMessage.getContent();
			if(content instanceof String)
			{	result=1;				
			}
			else if(content instanceof Multipart)
			{	result=((Multipart)content).getCount();				
			}
		}
		
		INKFResponse resp=context.createResponseFrom(result);
		if(mimetype!=null)
		{	resp.setMimeType(mimetype);
		}
	}
	
	private class ReadableBinaryStreamRepresentation implements IReadableBinaryStreamRepresentation
	{
		InputStream mStream;
		int mSize;
		
		public ReadableBinaryStreamRepresentation(InputStream aStream, int aSize)
		{	mStream=aStream;
			mSize=aSize;
		}
		
		public int getContentLength()
		{	return mSize;
		}

		public InputStream getInputStream() throws IOException
		{	return mStream;
		}

		public String getEncoding()
		{	return null;
		}

		public void write(OutputStream aStream) throws IOException
		{	Utils.pipe(mStream, aStream);
		}
	}
}
