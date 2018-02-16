package com.shutterfly.missioncontrol.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.json.JSONObject;
import org.json.XML;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

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
    validateAuditHistory(fulfillmentTrackingRecordDoc);
    validateReceivedPendingEvent(fulfillmentTrackingRecordDoc, statusCode);
    validateMetadata(fulfillmentTrackingRecordDoc);
    validateProcessRequestStructure(fulfillmentTrackingRecordDoc);
  }

  private static void validateProcessRequestStructure(Document fulfillmentTrackingRecordDoc) {
    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    assertNotNull(fulfillmentRequest);
    assertNotNull(fulfillmentRequest.get("requestHeader"));
    assertNotNull(fulfillmentRequest.get("requestDetail"));
    assertNotNull(fulfillmentRequest.get("requestTrailer"));

  }

  private static void validateAuditHistory(Document fulfillmentTrackingRecordDoc) {
    Document auditHistory = (Document) fulfillmentTrackingRecordDoc.get("auditHistory");
    assertNotNull(auditHistory);
    assertNotNull(auditHistory.get("createdBy"));
    assertNotNull(auditHistory.get("createdDate"));
    assertNotNull(auditHistory.get("updatedBy"));
    assertNotNull(auditHistory.get("updatedDate"));
  }

  private static void validateMetadata(Document fulfillmentTrackingRecordDoc) {
    ArrayList fulfillmentMetaDataList = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("fulfillmentMetaData");

    Document fulfillmentMetaData = (Document) fulfillmentMetaDataList.get(0);
    assertNotNull(fulfillmentMetaData);
    assertNotNull(fulfillmentMetaData.get("name"));
    assertNotNull(fulfillmentMetaData.get("value"));
  }

  private static void validateReceivedPendingEvent(Document fulfillmentTrackingRecordDoc,
      String statusCode) {
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
  }

  public static void validateTransactionalProcessRequestFields(String payload,
      Document fulfillmentTrackingRecordDoc) {
    JSONObject xmlJSONObj = XML.toJSONObject(payload);
    String jsonToString = xmlJSONObj.toString().replaceAll("sch:", "");
    Document bsonFromPayload = Document.parse(jsonToString);
    Document xmlProcessFulfillmentRequest = (Document) bsonFromPayload
        .get("processFulfillmentRequest");
    Document xmlFulfillmentRequest = (Document) xmlProcessFulfillmentRequest
        .get("fulfillmentRequest");
    Document docFulfillmentRequest = (Document) fulfillmentTrackingRecordDoc
        .get("fulfillmentRequest");

    validateRequestHeader(xmlFulfillmentRequest, docFulfillmentRequest);
    validateTransactionalRequestDetails(xmlFulfillmentRequest, docFulfillmentRequest);
    validateRequestTrailer(xmlFulfillmentRequest, docFulfillmentRequest);
  }

  public static void validateBulkProcessRequestFields(String payload,
      Document fulfillmentTrackingRecordDoc) {
    JSONObject xmlJSONObj = XML.toJSONObject(payload);
    String jsonToString = xmlJSONObj.toString().replaceAll("sch:", "");
    Document bsonFromPayload = Document.parse(jsonToString);
    Document xmlProcessFulfillmentRequest = (Document) bsonFromPayload
        .get("processFulfillmentRequest");
    Document xmlFulfillmentRequest = (Document) xmlProcessFulfillmentRequest
        .get("fulfillmentRequest");
    Document docFulfillmentRequest = (Document) fulfillmentTrackingRecordDoc
        .get("fulfillmentRequest");

    validateRequestHeader(xmlFulfillmentRequest, docFulfillmentRequest);
    validateBulkRequestDetails(xmlFulfillmentRequest, docFulfillmentRequest);
    validateRequestTrailer(xmlFulfillmentRequest, docFulfillmentRequest);
  }

  private static void validateBulkRequestDetails(Document xmlFulfillmentRequest,
      Document docFulfillmentRequest) {
    Document xmlBulkRequestDetail = (Document) ((Document) xmlFulfillmentRequest
        .get("requestDetail")).get("bulkRequestDetail");
    xmlBulkRequestDetail.put("fileSize", xmlBulkRequestDetail.get("fileSize").toString());
    Document docBulkRequestDetail = (Document) ((ArrayList) ((Document) docFulfillmentRequest
        .get("requestDetail")).get("bulkRequestDetail")).get(0);

    validateEcgDetail(xmlBulkRequestDetail, docBulkRequestDetail);
    validateSourceDetail(xmlBulkRequestDetail, docBulkRequestDetail);

    JSONObject xmlJson = new JSONObject(xmlBulkRequestDetail.toJson());
    JSONObject docJson = new JSONObject(docBulkRequestDetail.toJson());
    JSONAssert.assertEquals(xmlJson, docJson, false);
  }

  private static void validateEcgDetail(Document xmlBulkRequestDetail,
      Document docBulkRequestDetail) {
    assertTrue(docBulkRequestDetail.get("ecgDetail").toString()
        .contains(xmlBulkRequestDetail.get("ecgDetail").toString()));
    docBulkRequestDetail.put("ecgDetail", xmlBulkRequestDetail.get("ecgDetail").toString());
  }

  private static void validateSourceDetail(Document xmlBulkRequestDetail,
      Document docBulkRequestDetail) {
    assertTrue(docBulkRequestDetail.get("sourceDetail").toString()
        .contains(xmlBulkRequestDetail.get("sourceDetail").toString()));
    docBulkRequestDetail.put("sourceDetail", xmlBulkRequestDetail.get("sourceDetail").toString());
  }

  private static void validateRequestHeader(Document xmlFulfillmentRequest,
      Document docFulfillmentRequest) {
    Document xmlRequestHeader = (Document) xmlFulfillmentRequest.get("requestHeader");
    Date date = TrackingRecordValidationUtil
        .toJavaDate(xmlRequestHeader.get("requestDate").toString());
    xmlRequestHeader.put("requestDate", date);
    xmlRequestHeader.put("fulfillmentType", xmlRequestHeader.get("fulfillmentType").toString());
    Document docRequestHeader = (Document) docFulfillmentRequest.get("requestHeader");

    JSONObject xmlJson = new JSONObject(xmlRequestHeader.toJson());
    JSONObject docJson = new JSONObject(docRequestHeader.toJson());
    JSONAssert.assertEquals(xmlJson, docJson, false);
  }


  private static void validateRequestTrailer(Document xmlFulfillmentRequest,
      Document docFulfillmentRequest) {
    Document xmlRequestTrailer = (Document) xmlFulfillmentRequest.get("requestTrailer");
    Document docRequestTrailer = (Document) docFulfillmentRequest.get("requestTrailer");

    JSONObject xmlJson = new JSONObject(xmlRequestTrailer.toJson());
    JSONObject docJson = new JSONObject(docRequestTrailer.toJson());
    JSONAssert.assertEquals(xmlJson, docJson, false);
  }

  private static void validateTransactionalRequestDetails(Document xmlFulfillmentRequest,
      Document docFulfillmentRequest) {
    Document xmlRequestDetail = (Document) xmlFulfillmentRequest.get("requestDetail");
    Document docRequestDetail = (Document) docFulfillmentRequest.get("requestDetail");

    Document xmlTransactionalDetail = (Document) xmlRequestDetail.get("transactionalRequestDetail");
    Document docTransactionalDetail = (Document) docRequestDetail.get("transactionalRequestDetail");

    try {
      validateRecipients((Document) xmlTransactionalDetail.get("recipient"), (Document) ((ArrayList) docTransactionalDetail.get("recipientList")).get(0));
      docTransactionalDetail.put("recipient", xmlTransactionalDetail.get("recipient"));
      docTransactionalDetail.remove("recipientList");
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    //validate template Details
    docTransactionalDetail.put("template", docTransactionalDetail.get("templateDetail"));
    docTransactionalDetail.remove("templateDetail");

    //validate data
    validateData(xmlTransactionalDetail, docTransactionalDetail);

    JSONObject docJson = new JSONObject(docTransactionalDetail.toJson());
    JSONObject xmlJson = new JSONObject(xmlTransactionalDetail.toJson());
    JSONAssert.assertEquals(xmlJson, docJson, JSONCompareMode.LENIENT);
  }

  private static void validateData(Document xmlTransactionalDetail,
      Document docTransactionalDetail) {
    Document docData = (Document) docTransactionalDetail.get("data");
    Document xmlData = (Document) xmlTransactionalDetail.get("data");

    //validate contentFormatType
    validateContentFormatType(xmlData, docData);

    //validate embeddedDataType
    validateEmbeddedDataType(xmlData, docData);

    //validate externalFileType
    validateExternalFileType(xmlData);

    docTransactionalDetail.put("data", docData);
    xmlTransactionalDetail.put("data", xmlData);
  }

  private static void validateExternalFileType(Document xmlData) {
    if (Objects.nonNull(xmlData.get("externalFileType"))) {
      Document externalFileType = (Document) xmlData.get("externalFileType");
      Map<String, Object> externalFileTypeMap = externalFileType.entrySet()
          .stream()
          .filter(e -> !e.getValue().toString().equals(""))
          .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue().toString()));
      xmlData.put("externalFileType", externalFileTypeMap);
    }
  }

  private static void validateEmbeddedDataType(Document xmlData, Document docData) {
    if (Objects.nonNull(xmlData.get("embeddedDataType")) && !xmlData.get("embeddedDataType")
        .toString().trim().isEmpty()) {
      String docEmbeddedDataType = docData.get("embeddedDataType").toString();
      Document xmlEmbeddedDataType = (Document) xmlData.get("embeddedDataType");
      if (Objects.nonNull(xmlEmbeddedDataType.get("formData"))) {
        if (xmlEmbeddedDataType.get("formData") instanceof Document) {
          validateFormData(docEmbeddedDataType, xmlEmbeddedDataType);
          docData.remove("embeddedDataType");
          xmlData.remove("embeddedDataType");
        } else if (xmlEmbeddedDataType.get("formData") instanceof ArrayList) {
          ArrayList<Document> formDataList = (ArrayList) xmlEmbeddedDataType.get("formData");
          formDataList.forEach(formData -> validateFormData(docEmbeddedDataType, formData));
          docData.remove("embeddedDataType");
          xmlData.remove("embeddedDataType");
        }
      }
    } else if (Objects.nonNull(docData.get("embeddedDataType"))) {
      String embeddedDataType = docData.get("embeddedDataType").toString().replace(
          "<sch:embeddedDataType xmlns:sch=\"http://dms-fsl.uhc.com/fulfillment/schema\" xmlns:v7=\"http://enterprise.unitedhealthgroup.com/schema/canonical/base/common/v7_00\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>",
          "").replace("</sch:embeddedDataType", "").trim();
      docData.put("embeddedDataType", embeddedDataType);
    }
  }

  private static void validateFormData(String docEmbeddedDataType, Document formData) {
    String formItemUOM = formData.get("formItemUOM").toString();
    String formItemQuantity = formData.get("formItemQuantity").toString();
    String formItemID = formData.get("formItemID").toString();
    boolean isValid =
        docEmbeddedDataType.contains(formItemUOM) && docEmbeddedDataType
            .contains(formItemQuantity)
            && docEmbeddedDataType.contains(formItemID);
    assertTrue(isValid);
  }

  private static void validateContentFormatType(Document xmlData, Document docData) {
    if (Objects.nonNull(docData.get("contentFormatType"))) {
      Document docContentFormatType = (Document) docData.get("contentFormatType");
      Document xmlContentFormatType = (Document) xmlData.get("contentFormatType");
      validateContentStream(docContentFormatType, xmlContentFormatType);
      validateDocumentMetadata(docContentFormatType, xmlContentFormatType);
    }
  }

  private static void validateDocumentMetadata(Document docContentFormatType,
      Document xmlContentFormatType) {
    boolean valid = false;
    docContentFormatType.put("documentMetadata", docContentFormatType.get("documentMetaDataList"));
    docContentFormatType.remove("documentMetaDataList");
    List<String> docKeyList = new ArrayList<>();
    docKeyList.add("name");
    docKeyList.add("value");

    List<String> xmlKeyList = new ArrayList<>();
    xmlKeyList.add("v7:name");
    xmlKeyList.add("v7:value");
    ArrayList<Document> docDocumentMetadata = (ArrayList) docContentFormatType
        .get("documentMetadata");
    if (xmlContentFormatType.get("documentMetadata") instanceof ArrayList) {
      ArrayList<Document> xmlDocumentMetadata = (ArrayList) xmlContentFormatType
          .get("documentMetadata");

      if (docDocumentMetadata.size() == xmlDocumentMetadata.size()) {
        for (int i = 0; i < docDocumentMetadata.size(); i++) {
          if (checkEquals(docKeyList, xmlKeyList, docDocumentMetadata.get(i),
              xmlDocumentMetadata.get(i))) {
            valid = true;
          } else {
            break;
          }
        }
      }
    } else if (xmlContentFormatType.get("documentMetadata") instanceof Document) {
      valid = checkEquals(docKeyList, xmlKeyList,
          (Document) ((ArrayList) docContentFormatType.get("documentMetadata")).get(0),
          (Document) xmlContentFormatType.get("documentMetadata"));
    }
    assertTrue(valid);
    docContentFormatType.put("documentMetadata", xmlContentFormatType.get("documentMetadata"));
  }

  private static void validateContentStream(Document docContentFormatType,
      Document xmlContentFormatType) {
    String docContentStream = docContentFormatType.get("contentStream").toString();
    String xmlContentStream = xmlContentFormatType.get("contentStream").toString();
    assertTrue(docContentStream.contains(xmlContentStream));
    docContentFormatType.put("contentStream", xmlContentStream);
  }

  public static Date toJavaDate(String inputString) {
    Date date = new Date();
    if (Objects.nonNull(inputString)) {
      date = Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(inputString))
          .toInstant());
    }
    return date;
  }

  public static void validatePostRequestFields(String payload,
      Document fulfillmentTrackingRecordDoc) {
    JSONObject xmlJSONObj = XML.toJSONObject(payload);
    String jsonToString = xmlJSONObj.toString().replaceAll("sch:", "");
    Document bsonFromPayload = Document.parse(jsonToString);
    Document xmlPostFulfillmentRequestStatus = (Document) bsonFromPayload
        .get("postFulfillmentRequestStatus");
    Document xmlFulfillmentRequestStatus = (Document) xmlPostFulfillmentRequestStatus
        .get("fulfillmentRequestStatus");
    Document docPostFulfillmentStatus = (Document) ((ArrayList) fulfillmentTrackingRecordDoc
        .get("postFulfillmentStatus")).get(0);

    validateRequestHeader(xmlFulfillmentRequestStatus, docPostFulfillmentStatus);
    validateRequestHistory(xmlFulfillmentRequestStatus, docPostFulfillmentStatus);
    validateRequestTrailer(xmlFulfillmentRequestStatus, docPostFulfillmentStatus);
  }

  private static void validateRequestHistory(Document xmlFulfillmentRequest,
      Document docFulfillmentRequest) {
    Document xmlRequestHistory = (Document) xmlFulfillmentRequest.get("requestHistory");
    xmlRequestHistory
        .put("receivedDate", toJavaDate(xmlRequestHistory.get("receivedDate").toString()));
    xmlRequestHistory
        .put("dispatchedDate", toJavaDate(xmlRequestHistory.get("dispatchedDate").toString()));
    xmlRequestHistory.put("successCount", xmlRequestHistory.get("successCount").toString());
    xmlRequestHistory.put("exceptionCount", xmlRequestHistory.get("exceptionCount").toString());
    xmlRequestHistory.put("recipientId", xmlRequestHistory.get("recipientId").toString());

    Document docRequestHistory = (Document) ((ArrayList) docFulfillmentRequest
        .get("requestHistory")).get(0);
    docRequestHistory.remove("updateDate");
    docRequestHistory.remove("insertDate");

    assertTrue(validateExceptionDetails(docRequestHistory, xmlRequestHistory));
    assertTrue(validatePostFeedBack(docRequestHistory, xmlRequestHistory));

    JSONObject xmlJson = new JSONObject(xmlRequestHistory.toJson());

    JSONObject docJson = new JSONObject(docRequestHistory.toJson());
    JSONAssert.assertEquals(xmlJson, docJson, false);
  }

  private static boolean validatePostFeedBack(Document docRequestHistory,
      Document xmlRequestHistory) {
    boolean valid = false;
    docRequestHistory.put("postFeedback", docRequestHistory.get("postFeedbackList"));
    docRequestHistory.remove("postFeedbackList");
    ArrayList<Document> docPostFeedback = (ArrayList) docRequestHistory.get("postFeedback");
    ArrayList<Document> xmlPostFeedback = (ArrayList) xmlRequestHistory.get("postFeedback");

    List<String> docKeyList = new ArrayList<>();
    docKeyList.add("name");
    docKeyList.add("value");

    List<String> xmlKeyList = new ArrayList<>();
    xmlKeyList.add("v7:name");
    xmlKeyList.add("v7:value");

    if (docPostFeedback.size() == xmlPostFeedback.size()) {
      for (int i = 0; i < docPostFeedback.size(); i++) {
        if (checkEquals(docKeyList, xmlKeyList, docPostFeedback.get(i),
            xmlPostFeedback.get(i))) {
          valid = true;
        } else {
          break;
        }
      }
    }
    return valid;
  }

  private static boolean validateExceptionDetails(Document docRequestHistory,
      Document xmlRequestHistory) {
    boolean valid = false;
    docRequestHistory.put("exceptionDetail", docRequestHistory.get("exceptionDetailList"));
    docRequestHistory.remove("exceptionDetailList");
    ArrayList<Document> docExceptionDetail = (ArrayList) docRequestHistory.get("exceptionDetail");
    ArrayList<Document> xmlExceptionDetail = (ArrayList) xmlRequestHistory.get("exceptionDetail");

    List<String> docKeyList = new ArrayList<>();
    docKeyList.add("errorCode");
    docKeyList.add("errorMessage");

    List<String> xmlKeyList = new ArrayList<>();
    xmlKeyList.add("errorCode");
    xmlKeyList.add("message");

    if (docExceptionDetail.size() == xmlExceptionDetail.size()) {
      for (int i = 0; i < docExceptionDetail.size(); i++) {
        if (checkEquals(docKeyList, xmlKeyList, docExceptionDetail.get(i),
            xmlExceptionDetail.get(i))) {
          valid = true;
        } else {
          break;
        }
      }
    }
    return valid;
  }

  private static boolean checkEquals(List<String> docKeyList, List<String> xmlKeyList,
      Document doc, Document xml) {
    boolean valid = false;
    if (docKeyList.size() == xmlKeyList.size()) {
      for (int i = 0; i < docKeyList.size(); i++) {
        if (!docKeyList.get(i).equals(xmlKeyList.get(i))) {
          if (doc.get(docKeyList.get(i)).equals(xml.get(xmlKeyList.get(i)))) {
            doc.put(xmlKeyList.get(i), xml.get(xmlKeyList.get(i)));
            doc.remove(docKeyList.get(i));
            valid = true;
          } else {
            valid = false;
            break;
          }
        } else {
          valid = doc.get(docKeyList.get(i)).equals(xml.get(xmlKeyList.get(i)));
        }
      }
    }
    return valid;
  }

  public static void validateRecipients(Document docRecipient, Document xmlRecipient) throws JsonProcessingException {


    JSONObject docRecipientObject = new JSONObject(docRecipient.toJson());
    Map<String, Object> docRecipientObjectMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    docRecipientObjectMap.putAll(docRecipientObject.toMap());



    JSONObject xmlRecipientObject = new JSONObject(xmlRecipient.toJson());
    Map<String, Object> xmlRecipientObjectMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    xmlRecipientObjectMap.putAll(xmlRecipientObject.toMap());

    Map<String, String> nodeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Map.Entry e : docRecipientObjectMap.entrySet()) {
       nodeMap.put(e.getKey().toString().toLowerCase(), String.valueOf(e.getValue()));
    }



    Map<String, String> nodeMap2 = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Map.Entry e : xmlRecipientObjectMap.entrySet()) {
      nodeMap2.put(e.getKey().toString().toLowerCase(), String.valueOf(e.getValue()));
    }
    nodeMap.remove("mailtoaddress");
    nodeMap.remove("returntoaddress");
    nodeMap.remove("person");
    nodeMap.remove("organization");
    nodeMap2.remove("mailtoaddress");
    nodeMap2.remove("returntoaddress");
    nodeMap2.remove("person");
    nodeMap2.remove("organization");

    Object mailToAddress1 = docRecipientObjectMap.get("mailtoaddress");
    Object returnToAddress1 = docRecipientObjectMap.get("returntoaddress") ;
    Object person1 =  docRecipientObjectMap.get("person");
    Object organization1= docRecipientObjectMap.get("organization");
    Object MailToAddressInternational1=docRecipientObjectMap.get("MailToAddressInternational");
    Object ReturnToAddressInternational1=docRecipientObjectMap.get("ReturnToAddressInternational");

    Object mailToAddress2 = xmlRecipientObjectMap.get("mailtoaddress");
    Object returnToAddress2 = xmlRecipientObjectMap.get("returntoaddress");
    Object person2 = xmlRecipientObjectMap.get("person");
    Object organization2= xmlRecipientObjectMap.get("organization");
    Object MailToAddressInternational2=xmlRecipientObjectMap.get("MailToAddressInternational");
    Object ReturnToAddressInternational2=docRecipientObjectMap.get("ReturnToAddressInternational");



    if(Objects.nonNull(mailToAddress1)){
      Map<String,String> mailToAddress1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(mailToAddress1));
      Map<String,String> mailToAddress2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(mailToAddress2));
      assertEquals(mailToAddress1Map.equals(mailToAddress2Map),true);
    }
    if(Objects.nonNull(returnToAddress1)){
      Map<String,String> returnToAddress1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(returnToAddress1));
      Map<String,String> returnToAddress2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(returnToAddress2));
      assertEquals(returnToAddress1Map.equals(returnToAddress2Map),true);
    }
    if(Objects.nonNull(person1)){
      Map<String,String> person1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(person1));
      Map<String,String> person2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(person2));
      assertEquals(person1Map.equals(person2Map),true);
    }

    if(Objects.nonNull(organization1)){
      Map<String,String> person1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(organization1));
      Map<String,String> person2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(organization2));
      assertEquals(person1Map.equals(person2Map),true);
    }
    if(Objects.nonNull(MailToAddressInternational1)){
      Map<String,String> person1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(MailToAddressInternational1));
      Map<String,String> person2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(MailToAddressInternational2));
      assertEquals(person1Map.equals(person2Map),true);
    }
    if(Objects.nonNull(ReturnToAddressInternational1)){
      Map<String,String> person1Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(ReturnToAddressInternational1));
      Map<String,String> person2Map=convertJsonStringToMapAndKeysToLowerCase(javaObjectToJsonString(ReturnToAddressInternational1));
      assertEquals(person1Map.equals(person2Map),true);
    }
    assertEquals(nodeMap.equals(nodeMap2),true);
  }


  public static Map<String,String> convertJsonStringToMapAndKeysToLowerCase(String jsonString){

    JSONObject jsonObject = new JSONObject(jsonString);
    Map<String, Object> jsonObjectMap = jsonObject.toMap();

    Map<String, String> map = new HashMap<>();
    for (Map.Entry e : jsonObjectMap.entrySet()) {
      map.put(e.getKey().toString().toLowerCase(), String.valueOf(e.getValue()));
    }
    return map;
  }

  private static String javaObjectToJsonString(Object object) throws JsonProcessingException {
    ObjectMapper mapper=new ObjectMapper();
    return mapper.writeValueAsString(object);
  }
}
