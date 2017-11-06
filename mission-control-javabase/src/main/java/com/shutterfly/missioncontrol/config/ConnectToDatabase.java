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
		String databaseName = "missioncontrol"; // Name of the database
		String userName = "svc_sbs_missioncontrol_rw"; // user name

		 char[] password = { 'm', 'c', '_', '0', '9', '5', 't', 'v', 'n' };
		// qa password
		//char[] password = { 'x', 'm', 'p', 'o', '4', '8', '5', 'n', 'h' }; // dev
																			// password
		MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName, password);
		
		this.mongoClient = new MongoClient(new ServerAddress(config.getProperty("DatabaseAddress"), dbPort),
				Arrays.asList(credential));
		return mongoClient ;
	}

	public void closeMongoConnection() {
		if (this.mongoClient != null) {
			this.mongoClient.close();
		}
	}

}