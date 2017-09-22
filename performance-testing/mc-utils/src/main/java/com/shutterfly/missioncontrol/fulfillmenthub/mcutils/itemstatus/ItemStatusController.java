package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemStatusController {

  @PostMapping("/itemstatusfiles/generate")
  public ItemStatusFileGenerationResponse generateItemStatusFiles(){
    ItemStatusFileGenerationResponse itemStatusFileGenerationResponse= new ItemStatusFileGenerationResponse();
    itemStatusFileGenerationResponse.getItemStatusFiles().add(new ItemStatusFile("bulkReqId","/dist/test"));
    return itemStatusFileGenerationResponse;
  }

}