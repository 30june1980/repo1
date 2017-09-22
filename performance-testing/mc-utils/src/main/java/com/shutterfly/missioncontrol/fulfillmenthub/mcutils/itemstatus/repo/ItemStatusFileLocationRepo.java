package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileLocationDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemStatusFileLocationRepo
    extends MongoRepository<ItemStatusFileLocationDoc, String> {

}
