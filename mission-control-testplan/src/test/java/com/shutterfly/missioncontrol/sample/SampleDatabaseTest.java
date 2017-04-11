/**
 * 
 */
package com.shutterfly.missioncontrol.sample;

import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.testng.annotations.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;

/**
 * @author Diptman Gupta
 *
 */
public class SampleDatabaseTest extends ConfigLoader {

	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	@Test
	public void databaseQueryTest() {
		client = connectToDatabase.getMongoConnection();
		basicConfigNonWeb();
		MongoDatabase database = client.getDatabase("missioncontrol");
		MongoCollection<Document> collection = database.getCollection("participant");

		// Reference
		// http://mongodb.github.io/mongo-java-driver/3.4/driver/tutorials/perform-read-operations/
		Document myDoc = collection.find(eq("participantId", "participant_001")).first();

		System.out.println("Document printing :: " + myDoc.toJson());
		connectToDatabase.closeMongoConnection();
	}
}
