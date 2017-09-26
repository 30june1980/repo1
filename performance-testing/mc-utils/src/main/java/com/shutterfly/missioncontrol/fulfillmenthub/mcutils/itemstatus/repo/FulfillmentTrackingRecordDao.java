package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import java.util.List;
import java.util.Set;

public interface FulfillmentTrackingRecordDao {

  boolean existsRequestNotTaggedWithBulkRequestId(List<String> requestIds);

  List<FulfillmentTrackingRecordDoc> getFulfillmentTrackingRecordDocs(List<String> requestIds);

  List<String> getBulkRequestIdsSentToSupplier(int pastMinutesToConsiderBulkRequestsFrom);

  List<FulfillmentTrackingRecordDoc> getProcessFulfillmentTrackingRecordDocsForBulkRequestId(String bulkRequestId);
}
