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

public class ConnectToDatabase {
	MongoClient mongoClient;

	public MongoClient getMongoConnection() {
		String databaseName = "missioncontrol"; // the name of the database in
												// which
		// the user is defined
		String userName = "svc_sbs_missioncontrol_rw"; // the user name

		char[] password = { 'm', 'c', '_', '0', '9', '5', 't', 'v', 'n' }; // password

		MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName, password);
		return this.mongoClient = new MongoClient(new ServerAddress("tsbsmdb01-lv.internal.shutterfly.com", 27017),
				Arrays.asList(credential));

	}

	public void closeMongoConnection() {
		if (this.mongoClient != null) {
			this.mongoClient.close();
		}
	}

}