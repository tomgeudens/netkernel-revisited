package com.ebc.neo4j.embedded.representation;

import org.neo4j.graphdb.GraphDatabaseService;

public class Neo4jInstance {
	
	private final GraphDatabaseService mInstance;

	public Neo4jInstance(GraphDatabaseService aInstance) {
		mInstance = aInstance;
	}
	
	public GraphDatabaseService getInstance() {
		return mInstance;
	}
	
	public String toString() {
		return mInstance.toString();
	}
	
	protected void finalize() throws Throwable {
		try {
			if (mInstance.isAvailable(200)) {
				mInstance.shutdown();
			}
		}
		finally {
			super.finalize();
		}
	}
}