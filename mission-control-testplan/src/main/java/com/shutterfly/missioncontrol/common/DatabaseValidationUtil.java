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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * @author Diptman Gupta
 */

public class DatabaseValidationUtil extends ConfigLoader {

  private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

  private static final String REQUEST_ID = "requestId";
  private ConnectToDatabase connectToDatabase;
  private MongoClient client;
  private MongoDatabase database;
  private MongoCollection<Document> fulfillmentTrackingRecord;
  private MongoCollection<Document> fulfillmentStatusTracking;

  public DatabaseValidationUtil() {
    connectToDatabase = new ConnectToDatabase();
    client = connectToDatabase.getMongoConnection();
    database = client.getDatabase("missioncontrol");
    fulfillmentTrackingRecord = database.getCollection("fulfillment_tracking_record");
    fulfillmentStatusTracking = database.getCollection("fulfillment_status_tracking");
  }


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
    return fulfillmentTrackingRecord.find(eq(REQUEST_ID, record)).first();
  }

  public Document getStatusTrackingRecord(String record) {
    return fulfillmentStatusTracking.find(eq(REQUEST_ID, record)).first();
  }

  public boolean validateRecordStatus(Document fulfillmentStatusTrackingDoc, String record,
      String statusToValidate, String requestTypeToValidate) {
    boolean flag = false;
    List<Document> requestTrackingDoc = (ArrayList<Document>) fulfillmentStatusTrackingDoc
        .get("requestTracking");

    for (int i = 0; i < requestTrackingDoc.size(); i++) {
      if (requestTrackingDoc.get(i).getString("status").equals(statusToValidate)
          && requestTrackingDoc.get(i).getString("requestType").equals(requestTypeToValidate)) {
        logger.info("Request is successfully Processed : " + record);
        flag = true;
        break;
      } else if (requestTrackingDoc.get(i).getString("status").equals("PutToDeadLetterTopic")) {
        logger.info(" Request is moved to Dead Letter Topic " + record);
        flag = true;
        break;
      }
    }
    return flag;
  }
}