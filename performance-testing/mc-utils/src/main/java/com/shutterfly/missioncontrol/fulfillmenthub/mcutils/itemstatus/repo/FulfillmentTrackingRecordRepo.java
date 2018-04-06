package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrolservices.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FulfillmentTrackingRecordRepo
    extends MongoRepository<FulfillmentTrackingRecordDoc, String> {

  FulfillmentTrackingRecordDoc findByRequestId(String requestID);

}
