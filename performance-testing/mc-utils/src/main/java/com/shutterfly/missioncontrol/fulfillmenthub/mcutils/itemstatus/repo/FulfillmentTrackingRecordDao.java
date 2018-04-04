package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrolservices.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import java.util.List;
import java.util.Set;

public interface FulfillmentTrackingRecordDao {

  boolean existsRequestNotTaggedWithBulkRequestId(List<String> requestIds);

  List<FulfillmentTrackingRecordDoc> getFulfillmentTrackingRecordDocs(List<String> requestIds);

  List<String> getBulkRequestIdsSentToSupplier(int pastMinutesToConsiderBulkRequestsFrom);

  List<FulfillmentTrackingRecordDoc> getProcessFulfillmentTrackingRecordDocsForBulkRequestId(String bulkRequestId);
}
