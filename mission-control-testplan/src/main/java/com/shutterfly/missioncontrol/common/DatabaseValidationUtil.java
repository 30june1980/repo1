/**
 *
 */
package com.shutterfly.missioncontrol.common;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.testng.Assert;

/**
 * @author Diptman Gupta
 */
public class DatabaseValidationUtil extends ConfigLoader {

  private static final String REQUEST_ID = "requestId";

  public void validateRecordsAvailabilityAndStatusCheck(String record, String statusToValidate,
      String requestTypeToValidate) throws Exception {
    basicConfigNonWeb();
    /*
     * Verification of RequestId presence in fulfillment_tracking_record and
		 * fulfillment_status_tracking collection Verification of
		 * fulfillment_status_tracking status where status is not logged as
		 * PutToDeadLetterTopic
		 */
    Document fulfillmentTrackingRecordDoc;
    Document fulfillmentStatusTrackingDoc;

    for (int retry = 0; retry <= 7; retry++) {
      try {
        fulfillmentTrackingRecordDoc = getTrackingRecord(record);
        if (fulfillmentTrackingRecordDoc != null) {
          fulfillmentTrackingRecordDoc.containsKey(REQUEST_ID);
          Assert.assertEquals(record, fulfillmentTrackingRecordDoc.getString(REQUEST_ID));
          fulfillmentStatusTrackingDoc = getStatusTrackingRecord(record);

          if (validateRecordStatus(fulfillmentStatusTrackingDoc, record, statusToValidate,
              requestTypeToValidate)) {
            break;
          } else {
            throw new Exception("Record Status or Request Type mismatch!");
          }
        } else {
          throw new Exception("Record Not Found " + record);
        }
      } catch (Exception ex) {
        if (retry >= 7) {
          throw new Exception(ex.getMessage());
        } else {
          TimeUnit.SECONDS.sleep(20);
        }
      }
    }
  }

  public Document getTrackingRecord(String record) {
    ConnectToDatabase connectToDatabase = new ConnectToDatabase();
    MongoClient client = connectToDatabase.getMongoConnection();
    MongoDatabase database = client.getDatabase("missioncontrol");
    MongoCollection<Document> fulfillmentTrackingRecord = database
        .getCollection("fulfillment_tracking_record");
    Document document = fulfillmentTrackingRecord.find(eq(REQUEST_ID, record)).first();
    connectToDatabase.closeMongoConnection();
    return document;
  }

  public Document getStatusTrackingRecord(String record) {
    ConnectToDatabase connectToDatabase = new ConnectToDatabase();
    MongoClient client = connectToDatabase.getMongoConnection();
    MongoDatabase database = client.getDatabase("missioncontrol");
    MongoCollection<Document> fulfillmentStatusTracking = database
        .getCollection("fulfillment_status_tracking");
    Document document =  fulfillmentStatusTracking.find(eq(REQUEST_ID, record)).first();
    connectToDatabase.closeMongoConnection();
    return document;
  }

  private boolean validateRecordStatus(Document fulfillmentStatusTrackingDoc, String record,
      String statusToValidate,
      String requestTypeToValidate) {
    boolean flag = false;

    @SuppressWarnings("unchecked")
    List<Document> requestTrackingDoc = (ArrayList<Document>) fulfillmentStatusTrackingDoc
        .get("requestTracking");

    for (int i = 0; i < requestTrackingDoc.size(); i++) {
      if (requestTrackingDoc.get(i).getString("status").equals("PutToDeadLetterTopic")) {
        Assert.assertEquals(false, true, " Request is moved to Dead Letter Topic " + record);
        flag = true;
        break;
      } else if (requestTrackingDoc.get(i).getString("status").equals(statusToValidate)
          && requestTrackingDoc.get(i).getString("requestType").equals(requestTypeToValidate)) {
        Assert.assertEquals(true, true, "Request is successfully Processed : " + record);
        flag = true;
        break;
      }
    }
    return flag;
  }
}