package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FulfillmentTrackingRecordRepo
    extends MongoRepository<FulfillmentTrackingRecordDoc, String> {

  FulfillmentTrackingRecordDoc findByRequestId(String requestID);

}
