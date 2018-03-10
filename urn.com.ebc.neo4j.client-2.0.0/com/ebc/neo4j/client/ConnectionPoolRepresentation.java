package com.ebc.neo4j.client;

import org.neo4j.driver.v1.*;

public class ConnectionPoolRepresentation implements AutoCloseable {
	Driver mDriver;
	
	public ConnectionPoolRepresentation(String aURL, String aUsername, String aPassword)  {
		mDriver = GraphDatabase.driver(aURL,
				AuthTokens.basic(
						aUsername, 
						aPassword
					)
				);
	}
	
	public Session getSession() {
		return mDriver.session();
	}

	@Override
	public void close() throws Exception {
		mDriver.close();
	}

}
