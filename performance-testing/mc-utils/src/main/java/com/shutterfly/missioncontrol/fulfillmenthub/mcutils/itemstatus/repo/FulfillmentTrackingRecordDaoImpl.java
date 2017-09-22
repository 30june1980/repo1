package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.appconstants.AppConstants;
import java.util.List;
import java.util.Set;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentTrackingRecordDaoImpl implements FulfillmentTrackingRecordDao {

  private MongoOperations mongoOperation;

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
  public boolean areRequestIdsValid(List<String> requestIds) {
//    TODO: Implementation to be added later.
    return true;
  }

  @Override
  public List<FulfillmentTrackingRecordDoc> getFulfillmentTrackingRecordDocs(List<String> requestIds) {
    Query query = new Query(
        Criteria.where(FulfillmentTrackingRecordDoc.REQUEST_ID).in(requestIds));
    return mongoOperation.find(query, FulfillmentTrackingRecordDoc.class);
  }
}
