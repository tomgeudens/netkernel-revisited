package org.netkernel.email.core.endpoint;

import org.netkernel.email.core.representation.MailSessionRep;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IHDSNodeList;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SMTPClientAccessor extends StandardAccessorImpl
{
	public SMTPClientAccessor()
	{	this.declareThreadSafe();		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSource(INKFRequestContext context) throws Exception
	{	MailSessionRep session=null;
		if(context.getThisRequest().argumentExists("config"))
		{	session=context.source("arg:config", MailSessionRep.class);			
		}
		else
		{	session=context.source("res:/etc/SMTPConfig.xml", MailSessionRep.class);
		}
	
		MimeMessage message = new MimeMessage(session.getSessionReadOnly() );
		
		IHDSNode header=context.source("arg:header", IHDSNode.class);
		message.setSubject((String)header.getFirstValue("/email/subject"));
		if(header.getFirstNode("/email/from")!=null)
		{	String from=(String)header.getFirstValue("/email/from");
			InternetAddress IAfrom = new InternetAddress( from );
			message.setSender( IAfrom );
			message.setFrom( IAfrom );
		}
		@SuppressWarnings("rawtypes")
		Map map=new HashMap(5);
		IHDSNodeList nl=header.getNodes("/email/to");
		for(int i=0; i<nl.size(); i++)
		{	IHDSNode n=nl.get(i);
			String to=(String)n.getValue();
			if( !map.containsKey( to ) )
			{	message.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
				map.put( to, Boolean.TRUE );
			}
		}
		map.clear();
		nl=header.getNodes("/email/cc");
		for(int i=0; i<nl.size(); i++)
		{	IHDSNode n=nl.get(i);
			String cc=(String)n.getValue();
			if( !map.containsKey( cc ) )
			{	message.addRecipient( Message.RecipientType.CC, new InternetAddress( cc ) );
				map.put( cc, Boolean.TRUE );
			}
		}
		map.clear();
		nl=header.getNodes("/email/bcc");
		for(int i=0; i<nl.size(); i++)
		{	IHDSNode n=nl.get(i);
			String bcc=(String)n.getValue();
			if( !map.containsKey( bcc ) )
			{	message.addRecipient( Message.RecipientType.BCC, new InternetAddress( bcc ) );
				map.put( bcc, Boolean.TRUE );
			}
		}
		map.clear();
		nl=header.getNodes("/email/attachment");
		for(int i=0; i<nl.size(); i++)
		{	IHDSNode n=nl.get(i);
			String name=(String)n.getFirstValue("name");
			String filename=(String)n.getFirstValue("filename");
			if( !map.containsKey( name ) )
			{	map.put( name, filename );
			}
		}

    String multipartSubType =  (String) header.getFirstValue("/email/multipartSubType");

		//Add message parts
		boolean messageflag=false;
		MimeMultipart mpart;
    if (multipartSubType == null) {
      mpart = new MimeMultipart();
    } else {
      mpart = new MimeMultipart(multipartSubType);
    }
    for(int i=0; i<context.getThisRequest().getArgumentCount(); i++)
		{	String arg=context.getThisRequest().getArgumentName(i);
			if(!arg.equals("header") && !arg.equals("config") && !arg.equals("activeType") && !arg.equals("scheme"))
			{	@SuppressWarnings("rawtypes")
				INKFResponseReadOnly resp=context.sourceForResponse("arg:"+arg, IReadableBinaryStreamRepresentation.class);
				IReadableBinaryStreamRepresentation rbs=(IReadableBinaryStreamRepresentation)resp.getRepresentation();
				 ByteArrayOutputStream baos = new ByteArrayOutputStream(rbs.getContentLength());
				 rbs.write( baos );
	             baos.flush();
	             MimeBodyPart mbp = new MimeBodyPart();
	             if( resp.getMimeType().startsWith( "text" ) )
	             {	mbp.setText(baos.toString(), "UTF-8");
					mbp.setHeader("Content-Type",	resp.getMimeType()+"; charset=\"UTF-8\"");
					mbp.setHeader("Content-Transfer-Encoding", "quoted-printable");
	             }
	             else
	             {	ByteArrayDataSource bads = new ByteArrayDataSource( baos.toByteArray(), resp.getMimeType() );
	             	DataHandler dh = new DataHandler( bads );
	                String filename=(String)map.get(arg);
                	if(filename!=null)
                	{	mbp.setFileName(filename);	                		
                	}
                	else 
	                {	mbp.setFileName( arg );
	                }
                	mbp.setDataHandler( dh );
	              }
	              mpart.addBodyPart( mbp );
	              messageflag=true;
			}
		}
		if( !messageflag ) throw new Exception( "Must provide at least one message part argument" );
		message.setContent( mpart );
		message.saveChanges();
		Transport.send(message);
	}
}