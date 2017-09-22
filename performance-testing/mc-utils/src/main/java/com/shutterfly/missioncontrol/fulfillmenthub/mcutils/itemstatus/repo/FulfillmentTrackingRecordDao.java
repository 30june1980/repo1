package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import java.util.List;
import java.util.Set;

public interface FulfillmentTrackingRecordDao {

  boolean existsRequestNotTaggedWithBulkRequestId(List<String> requestIds);

  boolean areRequestIdsValid(List<String> requestIds);

  public List<FulfillmentTrackingRecordDoc> getFulfillmentTrackingRecordDocs(List<String> requestIds);
}
