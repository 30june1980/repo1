package com.shutterfly.missioncontrol.util;

import com.shutterfly.missioncontrol.util.AppConstants;
import org.bson.Document;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by Shweta on 15-01-2018.
 */
public class TrackingRecordValidationUtil {

  public static void validateTrackingRecordForProcessRequest(Document fulfillmentTrackingRecordDoc,
      String record, String statusCode) {
    assertNotNull(fulfillmentTrackingRecordDoc.get("_id"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("_class"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("currentFulfillmentStatus"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("requestId"));
    assertEquals(fulfillmentTrackingRecordDoc.get("requestId"), record);

    Document auditHistory = (Document) fulfillmentTrackingRecordDoc.get("auditHistory");
    assertNotNull(auditHistory);
    assertNotNull(auditHistory.get("createdBy"));
    assertNotNull(auditHistory.get("createdDate"));
    assertNotNull(auditHistory.get("updatedBy"));
    assertNotNull(auditHistory.get("updatedDate"));

    ArrayList eventHistoryList = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(0);
    assertEquals(eventHistory.get("processor"), "MC");
    assertEquals(eventHistory.get("eventType"), "ReceivedPending");
    assertNotNull(eventHistory.get("receivedDate"));
    assertEquals(eventHistory.get("statusCode"), statusCode);
    if (statusCode.equals(AppConstants.ACCEPTED)) {
      assertEquals(eventHistory.get("successCount"), "1");
      assertEquals(eventHistory.get("exceptionCount"), "0");
    } else if (statusCode.equals(AppConstants.REJECTED)) {
      assertEquals(eventHistory.get("successCount"), "0");
      assertEquals(eventHistory.get("exceptionCount"), "1");
    }
    assertNotNull(eventHistory.get("exceptionDetailList"));

    ArrayList fulfillmentMetaDataList = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("fulfillmentMetaData");

    Document fulfillmentMetaData = (Document) fulfillmentMetaDataList.get(0);
    assertNotNull(fulfillmentMetaData);
    assertNotNull(fulfillmentMetaData.get("name"));
    assertNotNull(fulfillmentMetaData.get("value"));

    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    assertNotNull(fulfillmentRequest);
    assertNotNull(fulfillmentRequest.get("requestHeader"));
    assertNotNull(fulfillmentRequest.get("requestDetail"));
    assertNotNull(fulfillmentRequest.get("requestTrailer"));
  }


}
