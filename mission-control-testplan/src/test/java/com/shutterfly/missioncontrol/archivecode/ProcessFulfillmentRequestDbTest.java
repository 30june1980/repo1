/**
 * 
 */
package com.shutterfly.missioncontrol.archivecode;

import static com.mongodb.client.model.Filters.eq;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

/**
 * @author Diptman Gupta
 *
 */

public class ProcessFulfillmentRequestDbTest extends ConfigLoader {
	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	@Test
	public void databaseQueryTest() throws IOException {
		client = connectToDatabase.getMongoConnection();
		basicConfigNonWeb();
		MongoDatabase database = client.getDatabase("missioncontrol");
		MongoCollection<Document> fulfillment_tracking_record = database.getCollection("fulfillment_tracking_record");
		MongoCollection<Document> fulfillment_status_tracking = database.getCollection("fulfillment_status_tracking");

		CsvReaderWriter wr = new CsvReaderWriter();

		for (String record : wr.readCsv()) {
			/*
			 * Verification of RequestId presence in fulfillment_tracking_record
			 * and fulfillment_status_tracking collection Verification of
			 * fulfillment_status_tracking status where status is not logged as
			 * PutToDeadLetterTopic
			 */
			Document fulfillment_tracking_record_doc = fulfillment_tracking_record.find(eq("requestId", record))
					.first();
			fulfillment_tracking_record_doc.containsKey("requestId");
			Assert.assertEquals(record, fulfillment_tracking_record_doc.getString("requestId"));

			Document fulfillment_status_tracking_doc = fulfillment_status_tracking.find(eq("requestId", record))
					.first();

			@SuppressWarnings("unchecked")
			ArrayList<Document> requestTrackingDoc = (ArrayList<Document>) fulfillment_status_tracking_doc
					.get("requestTracking");
			requestTrackingDoc.forEach(documentRequestTrackingCollection -> {
				if (documentRequestTrackingCollection.getString("status").equals("PutToDeadLetterTopic")) {
					System.out.println("Request is moved to Dead Letter Topic");
					assertEquals(true, false);

				}
				;
			});
			;
		}

		connectToDatabase.closeMongoConnection();
	}

	@Test(dependsOnMethods = { "databaseQueryTest" })
	public void clearCsvData() throws FileNotFoundException {
		File requestId = new File(config.getProperty("RequestIdCsvPath"));
		PrintWriter pw = new PrintWriter(requestId);
		pw.print("");
		pw.close();
	}
}