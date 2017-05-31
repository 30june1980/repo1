/**
 * 
 */
package com.shutterfly.missioncontrol.restful;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class ProcessFulfillmentRequestTransactionalInlineDataOnly extends ConfigLoader {
	/**
	 * 
	 */
	String uri = null;
	String myJson = null;
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
		return uri;
	}

	private String buildJson() throws IOException {
		URL file = Resources.getResource("payload/BulkPrintReady.json");
		myJson = Resources.toString(file, StandardCharsets.UTF_8);

		return myJson = myJson.replaceAll("REQUEST_101", record);

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_TIDO")
	private void getResponse() throws IOException {
		basicConfigNonWeb();
		Response response = RestAssured.given().header("samlValue", config.getProperty("SamlValue")).log().all()
				.contentType("application/json").body(this.buildJson()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));
		cwr.writeToCsv(record);

	}

	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	@Test(groups = "database", dependsOnGroups = { "Test_TIDO" })
	private void validateRecordsInDatabase() throws IOException, InterruptedException {
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

		ArrayList<Document> requestTrackingDoc = (ArrayList<Document>) fulfillment_status_tracking_doc
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