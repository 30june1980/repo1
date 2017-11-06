/**
 * 
 */
package com.shutterfly.missioncontrol.common;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

	public void validateRecordsAvailabilityAndStatusCheck(String record, String statusToValidate) throws Exception   {
		client = connectToDatabase.getMongoConnection();

		basicConfigNonWeb();
		MongoDatabase database = client.getDatabase("missioncontrol");
		MongoCollection<Document> fulfillmentTrackingRecord = database.getCollection("fulfillment_tracking_record");
		MongoCollection<Document> fulfillmentStatusTracking = database.getCollection("fulfillment_status_tracking");

		/*
		 * Verification of RequestId presence in fulfillment_tracking_record and
		 * fulfillment_status_tracking collection Verification of
		 * fulfillment_status_tracking status where status is not logged as
		 * PutToDeadLetterTopic
		 */
		Document fulfillmentTrackingRecordDoc = null;
		Document fulfillmentStatusTrackingDoc = null;
		final String requestId = "requestId";

		for (int retry = 0; retry <= 7; retry++) {
			try {
				if ((fulfillmentTrackingRecord.find(eq(requestId, record)).first()) != null) {
					fulfillmentTrackingRecordDoc = fulfillmentTrackingRecord.find(eq(requestId, record)).first();
					fulfillmentTrackingRecordDoc.containsKey(requestId);
					Assert.assertEquals(record, fulfillmentTrackingRecordDoc.getString(requestId));

					fulfillmentStatusTrackingDoc = fulfillmentStatusTracking.find(eq(requestId, record)).first();

					if (validateRecordStatus(fulfillmentStatusTrackingDoc, record, statusToValidate)) {
						break;

					} else 

						throw new Exception("Record Status not  Found " + statusToValidate);
					

				} else {

					throw new Exception("Record Not Found " + record);
				}
			} catch (Exception ex) {
				if (retry >= 7) 

					throw new Exception(ex.getMessage());

				 else {
					
						TimeUnit.SECONDS.sleep(20);
					
				}
			}
		}

		
		connectToDatabase.closeMongoConnection();

	}

	private boolean validateRecordStatus(Document fulfillmentStatusTrackingDoc, String record,
			String statusToValidate) {
		boolean flag = false;

		@SuppressWarnings("unchecked")
		List<Document> requestTrackingDoc = (ArrayList<Document>) fulfillmentStatusTrackingDoc
				.get("requestTracking");

		for (int i = 0; i < requestTrackingDoc.size(); i++) {

			if (requestTrackingDoc.get(i).getString("status").equals("PutToDeadLetterTopic")) {
				Assert.assertEquals(false, true, " Request is moved to Dead Letter Topic " + record);
				flag = true;
				break;
			} else {

				if (requestTrackingDoc.get(i).getString("status").equals(statusToValidate)) {

					Assert.assertEquals(true, true, "Request is successfully Processed : " + record);
					flag = true;
					break;
				} else {
					flag = false;
				}

			}

			
		}
		
		return flag;

	}
}