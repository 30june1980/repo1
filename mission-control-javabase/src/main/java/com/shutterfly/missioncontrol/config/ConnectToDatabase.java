/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * @author Diptman Gupta
 *
 */

public class ConnectToDatabase extends ConfigLoader {
	MongoClient mongoClient;
	int dbPort = 27017;

	public MongoClient getMongoConnection() {
		basicConfigNonWeb();
		String databaseName = config.getProperty("DatabaseName"); // Name of the database
		String userName = config.getProperty("UserName"); // user name

		String password = config.getProperty("DatabasePassword");
		MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName,
				password.toCharArray());

		this.mongoClient = new MongoClient(new ServerAddress(config.getProperty("DatabaseAddress"), dbPort),
				Arrays.asList(credential));
		return mongoClient;
	}

	public void closeMongoConnection() {
		if (this.mongoClient != null) {
			this.mongoClient.close();
		}
	}

}