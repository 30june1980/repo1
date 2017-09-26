package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.appconstants.AppConstants;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.FulfillmentStatus;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.ParticipantType;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentTrackingRecordDaoImpl implements FulfillmentTrackingRecordDao {

  private MongoOperations mongoOperation;

  @Value("${past.minutes.to.consider.sent.to.supplier.bulk.requests}")
  private int pastMinutesToConsiderBulkRequests;

  public FulfillmentTrackingRecordDaoImpl(MongoOperations mongoOperation) {
    this.mongoOperation = mongoOperation;
  }

  @Override
  public boolean existsRequestNotTaggedWithBulkRequestId(List<String> requestIds) {
    Query query = new Query(
        Criteria.where(FulfillmentTrackingRecordDoc.REQUEST_ID).in(requestIds)).addCriteria(
        Criteria.where(AppConstants.BULK_REQUEST_ID).exists(false));
    return mongoOperation.count(query, FulfillmentTrackingRecordDoc.class) > 0;
  }

  @Override
  public List<FulfillmentTrackingRecordDoc> getFulfillmentTrackingRecordDocs(
      List<String> requestIds) {
    Query query = new Query(
        Criteria.where(FulfillmentTrackingRecordDoc.REQUEST_ID).in(requestIds));
    return mongoOperation.find(query, FulfillmentTrackingRecordDoc.class);
  }

  @Override
  public List<String> getBulkRequestIdsSentToSupplier(int pastMinutesToConsiderBulkRequestsFrom) {
    Criteria criteriaDefinition = Criteria.where("fulfillmentRequest.requestHeader.sourceID")
        .is(ParticipantType.getHub().getName());
    criteriaDefinition = criteriaDefinition.and("auditHistory.createdDate")
        .gte(DateTime.now().minusMinutes(
            pastMinutesToConsiderBulkRequestsFrom <= 0 ? pastMinutesToConsiderBulkRequests
                : pastMinutesToConsiderBulkRequestsFrom).toDate());
    criteriaDefinition = criteriaDefinition.and("currentFulfillmentStatus")
        .is(FulfillmentStatus.SENT_TO_SUPPLIER.getValue());
    Query query = new Query(
        criteriaDefinition);
    query.fields().include(FulfillmentTrackingRecordDoc.REQUEST_ID);
    List<FulfillmentTrackingRecordDoc> bulkRequestDocuments = mongoOperation
        .find(query, FulfillmentTrackingRecordDoc.class);
    List<String> bulkRequestIds = new ArrayList<>();
    bulkRequestDocuments.forEach(document -> bulkRequestIds.add(document.getRequestId()));
    return bulkRequestIds;
  }

  @Override
  public List<FulfillmentTrackingRecordDoc> getProcessFulfillmentTrackingRecordDocsForBulkRequestId(
      String bulkRequestId) {
    Query query = new Query(Criteria.where(AppConstants.BULK_REQUEST_ID).is(bulkRequestId));
    return mongoOperation.find(query, FulfillmentTrackingRecordDoc.class);
  }

}
