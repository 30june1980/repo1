package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.archive.controller;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.dto.FileDto;
import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.archive.service.ProcessArchiveFileGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Shweta on 03-10-2017.
 */
@RestController
@Slf4j
public class ProcessArchiveFileGenerationController {

  private ProcessArchiveFileGenerationService processArchiveFileGenerationService;

  @Autowired
  public ProcessArchiveFileGenerationController(
      ProcessArchiveFileGenerationService processArchiveFileGenerationService) {
    this.processArchiveFileGenerationService = processArchiveFileGenerationService;
  }

  @PostMapping("/filecopies")
  public void generateFilesForArchiveRequest(@RequestBody FileDto fileDto) {
      log.info("Request for generating multiple files for archive request");
      processArchiveFileGenerationService.generateFilesForArchive(fileDto);
  }
}
