package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemStatusGenerationRequestTrackingRepo
    extends MongoRepository<ItemStatusFileGenerationRequestTrackingDoc, String> {

}
