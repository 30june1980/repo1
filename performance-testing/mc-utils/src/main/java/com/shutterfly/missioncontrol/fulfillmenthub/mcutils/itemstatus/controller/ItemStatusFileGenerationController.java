package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc.STATUS;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileLocationDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.ItemStatusFileLocationRepo;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.ItemStatusGenerationRequestTrackingRepo;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.service.ItemStatusFileService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ItemStatusFileGenerationController {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private ItemStatusFileService itemStatusFileService;

  private ItemStatusGenerationRequestTrackingRepo itemStatusGenerationRequestTrackingRepo;

  private ItemStatusFileLocationRepo itemStatusFileLocationRepo;

  @Value("${request.ids.delimiter}")
  private String requestIdsDelimiter;

  @Autowired
  public ItemStatusFileGenerationController(ItemStatusFileService itemStatusFileService,
      ItemStatusGenerationRequestTrackingRepo itemStatusGenerationRequestTrackingRepo,
      ItemStatusFileLocationRepo itemStatusFileLocationRepo) {
    this.itemStatusFileService = itemStatusFileService;
    this.itemStatusGenerationRequestTrackingRepo = itemStatusGenerationRequestTrackingRepo;
    this.itemStatusFileLocationRepo = itemStatusFileLocationRepo;
  }

  @PostMapping("/item-status-file/generate")
  public String generate(
      @RequestBody String body) {
    ItemStatusFileGenerationRequest itemStatusFileGenerationRequest = ItemStatusFileGenerationRequest
        .from(body, requestIdsDelimiter);
    ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc = itemStatusFileService
        .newItemStatusFileGenerationRequestTrackingDoc(
            itemStatusFileGenerationRequest);
    String id = itemStatusGenerationRequestTrackingDoc.getId();
    log.info("Request for generating item status files is accepted. Request id is: {}", id);
    itemStatusFileService.generateItemStatusFile(itemStatusFileGenerationRequest.getRequests(),
        itemStatusGenerationRequestTrackingDoc);
    return id;
  }

  @PostMapping("/item-status-file/generate/retry/{id}")
  public void retry(@PathVariable String id) {
    ItemStatusFileGenerationRequestTrackingDoc itemStatusFileGenerationRequestTrackingDoc = itemStatusGenerationRequestTrackingRepo
        .findOne(id);
    itemStatusFileService.generateItemStatusFile(
        itemStatusFileGenerationRequestTrackingDoc.getItemStatusFileGenerationRequest()
            .getRequests(), itemStatusFileGenerationRequestTrackingDoc);
  }

  @GetMapping(value = "/item-status-file/status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<StatusQueryResponse> getStatus(@PathVariable String id) {
    ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc = itemStatusGenerationRequestTrackingRepo
        .findOne(id);
    if (Objects.isNull(itemStatusGenerationRequestTrackingDoc)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity
        .ok(new StatusQueryResponse(itemStatusGenerationRequestTrackingDoc.getStatus(),
            itemStatusGenerationRequestTrackingDoc.getStatusDetail()));
  }

  @GetMapping(value = "/item-status-file/file-locations/{id}")
  public void getItemStatusFileLocations(
      @PathVariable String id, HttpServletResponse response) throws IOException {
    ItemStatusFileLocationDoc doc = itemStatusFileLocationRepo
        .findOne(id);
    if (Objects.nonNull(doc)) {
      response.setHeader("Content-Disposition", "attachment; filename=\"" + id + ".json\"");
      response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ServletOutputStream outputStream = response.getOutputStream();
      StringWriter stringWriter = new StringWriter();
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(stringWriter, doc.toDto());
      InputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes(
          StandardCharsets.UTF_8.name()));
      IOUtils.copy(inputStream, outputStream);
      outputStream.close();
      inputStream.close();
    }
  }

  @PostMapping("/item-status-file/generate/batch")
  public void runAsBatch() {
    itemStatusFileService.runBatch();
  }


}