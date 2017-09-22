package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.service;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration.FtpGateway;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.ItemStatusFile;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileLocationDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.ItemStatusFileLocationRepo;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemStatusFileService {

  private final ItemStatusFileLocationRepo itemStatusFileLocationRepo;

  private FtpGateway gateway;

  @Value("${mc.ftp.remote.dir}")
  private String remoteDirectory;

  @Value("${mc.local.file.to.upload}")
  private String localFilePathToUpload;

  @Autowired
  public ItemStatusFileService(FtpGateway gateway,
      ItemStatusFileLocationRepo itemStatusFileLocationRepo) {
    this.gateway = gateway;
    this.itemStatusFileLocationRepo = itemStatusFileLocationRepo;

  }

  @Async
  public void generateItemStatusFile(
      ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc) {
    try {
      ItemStatusFileLocationDoc itemStatusFileLocationDoc = new ItemStatusFileLocationDoc();
      String uuid = itemStatusGenerationRequestTrackingDoc.getId();
      log.info("Generating item status files for request received with id: {}", uuid);
      itemStatusFileLocationDoc.setId(uuid);
      String bulkRequestId = "test";
      ItemStatusFile itemStatusFile = generateItemStatusFile(bulkRequestId);
      itemStatusFileLocationDoc.getItemStatusFiles().add(itemStatusFile);
      itemStatusFileLocationRepo.save(itemStatusFileLocationDoc);
      log.info("Successfully processed request for item status files generation with id: {}",
          uuid);
    } catch (Exception exception) {
      log.error("Error occurred while generating item status files.", exception);
    }
  }

  private ItemStatusFile generateItemStatusFile(String bulkRequestId) throws IOException {
    ItemStatusFile itemStatusFile = new ItemStatusFile();
    itemStatusFile.setBulkRequestId(bulkRequestId);
    log.info("Generating item status file for bulk request with id: {}", bulkRequestId);
    log.info("Successfully generated item status file for bulk request with id: {}",
        bulkRequestId);
    String filePath = remoteDirectory + UUID.randomUUID().toString() + ".xml";
    gateway
        .send(
            FileUtils.readFileToByteArray(new File(localFilePathToUpload)),
            filePath);
    log.info("Successfully generated item status file for bulk request with id: {}",
        bulkRequestId);
    itemStatusFile.setItemStatusFilePath(filePath);
    return itemStatusFile;
  }

}
