package org.netkernel.email.core.endpoint;

import org.netkernel.email.core.representation.MailSessionRep;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class MailSessionTransreptor extends StandardTransreptorImpl
{
	public MailSessionTransreptor()
	{	this.declareToRepresentation(MailSessionRep.class);		
	}

	@Override
	public void onTransrept(INKFRequestContext context) throws Exception
	{	IHDSNode cfg=context.sourcePrimary(IHDSNode.class);
		
		Properties p=new Properties();
		p.setProperty("mail.transport.protocol", "smtp");
		p.setProperty("mail.smtp.host", (String)cfg.getFirstValue("/SMTPConfig/gateway"));
		p.setProperty("mail.smtp.user", (String)cfg.getFirstValue("/SMTPConfig/user"));
		p.setProperty("mail.from", (String)cfg.getFirstValue("/SMTPConfig/sender"));
		
		String port=null;
		if(cfg.getFirstNode("/SMTPConfig/port")!=null)
		{	port=(String)cfg.getFirstValue("/SMTPConfig/port");
			p.setProperty("mail.smtp.port", port);
		}
		
		boolean ssl=cfg.getFirstNode("/SMTPConfig/ssl")!=null;
		boolean tls=cfg.getFirstNode("/SMTPConfig/tls")!=null;
		Session s=null;
		if (ssl || tls)
		{	p.setProperty("mail.smtp.auth", "true");
			//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	     	if(port==null)
	     	{	port="465";
	     		p.setProperty("mail.smtp.port", port);
	     	}
	     	if(tls)
	     	{	p.setProperty("mail.smtp.starttls.enable","true");
	     	}
	     	p.setProperty("mail.smtp.socketFactory.port", port);
	     	p.setProperty( "mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
	     	s=Session.getInstance(p, new SMTPAuthenticator(p.getProperty("mail.smtp.user"), (String)cfg.getFirstValue("/SMTPConfig/password")));
		}
		else
		{	s=Session.getInstance(p);			
		}
		context.createResponseFrom(new MailSessionRep(s));
	}
	
	private class SMTPAuthenticator extends Authenticator
	{	private String mUser,mPassword;
		
		public SMTPAuthenticator(String aUser, String aPassword)
		{	mUser=aUser;
			mPassword=aPassword;
		}
		
		public PasswordAuthentication getPasswordAuthentication()
		{	return new PasswordAuthentication(mUser, mPassword);
		}
	}
}
