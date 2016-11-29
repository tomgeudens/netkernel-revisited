package org.netkernel.email.core.representation;

import javax.mail.Session;

public class MailSessionRep
{
	private Session mSession;
	
	public MailSessionRep(Session aSession)
	{	mSession=aSession;		
	}
	
	public Session getSessionReadOnly()
	{	return mSession;
	}
	
}
