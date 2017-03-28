/**
 * 
 */
package com.shutterfly.missioncontrol.soap;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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
		MongoCollection<Document> collection = database.getCollection("fulfillment_tracking_record");
		CsvReaderWriter wr = new CsvReaderWriter();

		for (String record : wr.readCsv()) {
			Document myDoc = collection.find(eq("requestId", record)).first();
			myDoc.containsKey("requestId");
			Assert.assertEquals(record, myDoc.getString("requestId"));

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