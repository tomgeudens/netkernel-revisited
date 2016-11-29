package org.netkernel.email.core.endpoint;

import org.netkernel.layer0.nkf.INKFRequest;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.util.XMLReadable;
import org.netkernel.module.standard.endpoint.StandardTransportImpl;
import org.netkernel.request.IRequestScopeLevel;
import org.netkernel.request.impl.RequestScopeLevelImpl;
import org.w3c.dom.Document;

import javax.mail.*;
import java.util.Properties;

public class MailFolderTransport extends StandardTransportImpl
{
	private String mHost;
	private String mIdentity;
	private int mPort=-1;
	private String mUser;
	private String mPassword;
	private String mFolder="INBOX";
	private String mProtocol;    //supports:  pop3, imap, pop3s and imaps
	private int mPollInterval=60000;
	private MessagePoll mMessagePoll;
	private boolean mPoll=true;
	private boolean mLeaveInFolder;

	@Override
	protected void postCommission(INKFRequestContext context) throws Exception
	{	XMLReadable config=new XMLReadable(((Document)getParameter("config")).getDocumentElement());
		mHost=config.getText("/config/hostname");
		mIdentity=config.getText("/config/mailboxName");
		mProtocol=config.getText("/config/protocol");
		mUser=config.getText("/config/user");
		mPassword=config.getText("/config/password");
		if(config.getNodes("/config/folder").size()>0)
		{	mFolder=config.getText("/config/folder");
		}
		if(config.getNodes("/config/port").size()>0)
		{	mPort=Integer.parseInt(config.getText("/config/port"));
		}
		if(config.getNodes("/config/pollInterval").size()>0)
		{	mPollInterval=1000*Integer.parseInt(config.getText("/config/pollInterval"));
		}
		mLeaveInFolder=config.getNodes("/config/leaveInFolder")!=null;
		mMessagePoll=new MessagePoll();
		mMessagePoll.start();
		context.logFormatted(INKFRequestContext.LEVEL_INFO, "MAILFOLDER_MSG_STARTED", mIdentity, mHost, mUser);
	}

	@Override
	protected void preDecommission(INKFRequestContext context) throws Exception
	{	mPoll=false;
		mMessagePoll.interrupt();
		context.logFormatted(INKFRequestContext.LEVEL_INFO, "MAILFOLDER_MSG_STOPPED", mIdentity, mHost, mUser);
	}

	private class MessagePoll extends Thread
	{
		public void run()
		{	while(mPoll)
			{	try
				{	processMailbox();
				}
				catch(Exception e)
				{	e.printStackTrace();
				}
				try
				{	sleep(mPollInterval);
				}
				catch(InterruptedException e)
				{	//Do nothing					
				}
			}
		}
	}
	
	protected void processMailbox() throws Exception
	{	Properties p=new Properties();
		p.setProperty("mail.store.protocol",mProtocol);
		Session s=Session.getInstance(p);
		Store store=null;
		try
		{	store=s.getStore(mProtocol);
		}
		catch(NoSuchProviderException nspe)
		{	String error="No provider for mail store protocol "+p.getProperty("mail.store.protocol")+" for mail processor "+mIdentity;
			//Log Error
			nspe.printStackTrace();
		}
		if(store!=null)
		{	try
			{	store.connect(mHost, mPort, mUser,  mPassword);
				Folder f=store.getFolder(mFolder);
				f.open(Folder.READ_WRITE);
				Message[] messages=f.getMessages();
				for(int i=0;i<messages.length ;i++)
				{	if(!messages[i].getFlags().contains(Flags.Flag.SEEN) )
					{	try
						{	issueRequest(messages[i]);
						}
						catch(Exception e)
						{	String error="Error processing mailbox +"+mIdentity+" on host "+mHost+" "+e.getMessage();
							//Log error
							e.printStackTrace();							
						}
						//Mark message for deletion
						if(!mLeaveInFolder)
						{	messages[i].setFlag(Flags.Flag.DELETED, true);
							messages[i].setFlag(Flags.Flag.SEEN, true);
						}
						else
						{	messages[i].setFlag(Flags.Flag.SEEN, true);
						}
					}
				}
				try
				{	f.expunge();
				}
				catch(MessagingException e)
				{	/*Do nothing - method not implemented for this provider*/ }
				f.close(true);
				store.close();
			}
			catch(MessagingException me)
			{	String error="Error processing mailbox "+mIdentity+" on host "+mHost+" "+me.getMessage();
				//Log Error
				me.printStackTrace();
			}
		}
	}
	
	private void issueRequest(Message m) throws Exception
	{	INKFRequestContext context=this.getTransportContext();
		context.logFormatted(INKFRequestContext.LEVEL_FINE, "MAILFOLDER_MSG_RECEIVED", mIdentity, m.getSubject());
		try
		{	MessageSpace space=new MessageSpace(m);
			space.onCommissionSpace(context.getKernelContext().getKernel());
			INKFRequest req=context.createRequest("mailfolder:message");
			req.addArgument("mailbox",mIdentity);
			IRequestScopeLevel target=context.getKernelContext().getRequestScope();
			IRequestScopeLevel insert=RequestScopeLevelImpl.createDurableScopeLevel(space,target);
			req.setRequestScope(insert);
			context.issueRequest(req);   //Can't do this async since the folder must not be closed in order to read message in the message space! - need to add async listener pattern to manage folder.
		}
		catch(Exception e)
		{	//e.printStackTrace();
			context.logFormatted(INKFRequestContext.LEVEL_WARNING, "MAILFOLDER_EX_REQUEST_FAILED", mIdentity, mHost, mUser, e.getMessage());
		}
	}
}
