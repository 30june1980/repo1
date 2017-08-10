/**
 * 
 */
package com.shutterfly.missioncontrol.processfulfillment;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
public class BulkPrintReady extends ConfigLoader {
	/**
	 * 
	 */
	private String uri = "";
	private String payload = "";
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
		return uri;
	}

	private String buildPayload() throws IOException {
		URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkPrintReady.xml");
		payload = Resources.toString(file, StandardCharsets.UTF_8);

		return payload = payload.replaceAll("REQUEST_101", record);

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_BPR_XML")
	private void getResponse() throws IOException, InterruptedException {
		basicConfigNonWeb();
		Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log().all()
				.contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));
		cwr.writeToCsv("BPR",record);

	}

	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	@Test(groups = "database", dependsOnGroups = { "Test_BPR_XML" })
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