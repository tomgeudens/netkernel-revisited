package org.netkernel.email.core.endpoint;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.util.XMLReadable;
import org.netkernel.module.standard.endpoint.StandardTransportImpl;
import org.netkernel.request.IRequestScopeLevel;
import org.netkernel.request.impl.RequestScopeLevelImpl;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.w3c.dom.Document;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.util.ArrayList;

public class SMTPServerTransport extends StandardTransportImpl
{
    private static int DEFAULT_SMTP_PORT = 25000;
    private static int MESSAGE_THREAD_SLEEP_TIME_MS = 1000;

	Wiser mSMTPServer;
	boolean mPoll=true;
	MessagePoll mMessagePoll=new MessagePoll();
	
	public SMTPServerTransport()
	{	this.declareSupportedVerbs(0);
		mSMTPServer=new Wiser();
		//System.out.println("SMTP Constructed");
		
	}
	
	@Override
	protected void postCommission(INKFRequestContext context) throws Exception
    {	Object cfg=this.getParameter("config");
		String hostName=null;
		int port=DEFAULT_SMTP_PORT;
		if(cfg!=null)
		{	XMLReadable config=new XMLReadable(((Document)getParameter("config")).getDocumentElement());
			hostName=config.getText("/config/hostname");
			port=Integer.parseInt(config.getText("/config/port"));
		}
		if(hostName==null)
		{	hostName=InetAddress.getLocalHost().getHostName();
		}
		mSMTPServer.setHostname(hostName);
		mSMTPServer.setPort(port);
		mSMTPServer.start();
		mMessagePoll.start();
		context.logFormatted(INKFRequestContext.LEVEL_INFO, "SMTPTRANSPORT_MSG_STARTED", hostName, port);
	}

	@Override
	protected void preDecommission(INKFRequestContext context) throws Exception
	{
		mPoll=false;
		mMessagePoll.interrupt();
		mSMTPServer.stop();
		context.logFormatted(INKFRequestContext.LEVEL_INFO, "SMTPTRANSPORT_MSG_STOPPED", mSMTPServer.getServer().getHostName());
	}

	private class MessagePoll extends Thread
	{
		public void run()
		{	while(mPoll)
			{	//System.out.println("SMTP Polling");
				@SuppressWarnings({ "rawtypes", "unchecked" })
				ArrayList<WiserMessage> processed=new ArrayList();
				for (WiserMessage message : mSMTPServer.getMessages())
				{	String envelopeSender = message.getEnvelopeSender();
					String envelopeReceiver = message.getEnvelopeReceiver();
					try
					{	INKFRequestContext context=getTransportContext();
						context.logFormatted(INKFRequestContext.LEVEL_FINE,"SMTPTRANSPORT_MSG_RECEIVED", envelopeSender, envelopeReceiver);
						MimeMessage mess = message.getMimeMessage();
						MessageSpace space=new MessageSpace(mess);
						space.onCommissionSpace(context.getKernelContext().getKernel());
						
						INKFRequest req=context.createRequest("smtp:message");
						req.addArgument("from", envelopeSender);
						req.addArgument("to", envelopeReceiver);
						IRequestScopeLevel target=context.getKernelContext().getRequestScope();
						IRequestScopeLevel insert=RequestScopeLevelImpl.createDurableScopeLevel(space,target);
						req.setRequestScope(insert);
						context.issueAsyncRequest(req);
						
					}
					catch(MessagingException e)
					{	e.printStackTrace();						
					}
					catch(Exception e)
					{	e.printStackTrace();
					}
					processed.add(message);
				}
				if(processed.size()>0)
				{	mSMTPServer.getMessages().removeAll(processed);
					processed.clear();
				}
				try
				{	if(mSMTPServer.getMessages().size()==0)
					{	sleep(MESSAGE_THREAD_SLEEP_TIME_MS);
					}
				}
				catch(InterruptedException e)
				{	//Do nothing					
				}
			}
		}
	}

}
