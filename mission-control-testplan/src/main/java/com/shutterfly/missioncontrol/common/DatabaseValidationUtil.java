/**
 * 
 */
package com.shutterfly.missioncontrol.common;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.testng.Assert;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;

/**
 * @author Diptman Gupta
 *
 */
public class DatabaseValidationUtil extends ConfigLoader {

	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	public void validateRecordsAvailabilityAndStatusCheck(String record) throws IOException, InterruptedException {
		client = connectToDatabase.getMongoConnection();
		Thread.sleep(20000);
		basicConfigNonWeb();
		MongoDatabase database = client.getDatabase("missioncontrol");
		MongoCollection<Document> fulfillment_tracking_record = database.getCollection("fulfillment_tracking_record");
		MongoCollection<Document> fulfillment_status_tracking = database.getCollection("fulfillment_status_tracking");

		/*
		 * Verification of RequestId presence in fulfillment_tracking_record and
		 * fulfillment_status_tracking collection Verification of
		 * fulfillment_status_tracking status where status is not logged as
		 * PutToDeadLetterTopic
		 */
		Document fulfillment_tracking_record_doc = fulfillment_tracking_record.find(eq("requestId", record)).first();
		fulfillment_tracking_record_doc.containsKey("requestId");
		Assert.assertEquals(record, fulfillment_tracking_record_doc.getString("requestId"));

		Document fulfillment_status_tracking_doc = fulfillment_status_tracking.find(eq("requestId", record)).first();

		/*
		 * if(fulfillment_status_tracking_doc.get("requestTracking") instanceof
		 * List<?>){ Object class1 =
		 * fulfillment_status_tracking_doc.get("requestTracking").getClass();
		 * if(class1 instanceof Document){
		 * 
		 * } }
		 */

		@SuppressWarnings("unchecked")
		List<Document> requestTrackingDoc = (ArrayList<Document>) fulfillment_status_tracking_doc
				.get("requestTracking");
		requestTrackingDoc.forEach(documentRequestTrackingCollection -> {
			if (documentRequestTrackingCollection.getString("status").equals("PutToDeadLetterTopic")) {
				System.out.println("Request is moved to Dead Letter Topic");
				System.out.println("Request Id: " + record);
				Assert.assertEquals(true, false);

			}
			;
		});

		connectToDatabase.closeMongoConnection();

	}
}
