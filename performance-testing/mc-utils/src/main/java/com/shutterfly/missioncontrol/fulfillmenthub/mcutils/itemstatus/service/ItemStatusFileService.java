package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.service;

import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.FulfillmentRequest;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.Recipient;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.RequestHeader;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.RequestTrailer;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.date.DateUtil;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.EventType;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.FulfillmentStatus;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.StatusCodeType;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration.FtpGateway;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.ItemStatusFile;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc.STATUS;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileLocationDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.FulfillmentTrackingRecordDao;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.FulfillmentTrackingRecordRepo;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.ItemStatusFileLocationRepo;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.repo.ItemStatusGenerationRequestTrackingRepo;
import com.uhc.dms_fsl.fulfillment.schema.v2.BatchStatus;
import com.uhc.dms_fsl.fulfillment.schema.v2.BatchStatus.BatchStatusItems;
import com.uhc.dms_fsl.fulfillment.schema.v2.BatchStatus.BatchStatusItems.BatchItemStatusDetail;
import com.uhc.dms_fsl.fulfillment.schema.v2.ObjectFactory;
import com.uhc.dms_fsl.fulfillment.schema.v2.RequestHistoryType;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ItemStatusFileService {

  private final ItemStatusFileLocationRepo itemStatusFileLocationRepo;

  private final ItemStatusGenerationRequestTrackingRepo itemStatusGenerationRequestTrackingRepo;

  private final FulfillmentTrackingRecordRepo fulfillmentTrackingRecordRepo;

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  private FtpGateway gateway;

  @Value("${mc.ftp.remote.dir}")
  private String remoteDirectory;

  @Value("${mc.local.file.to.upload}")
  private String localFilePathToUpload;

  private FulfillmentTrackingRecordDao fulfillmentTrackingRecordDao;

  @Autowired
  public ItemStatusFileService(FtpGateway gateway,
      ItemStatusFileLocationRepo itemStatusFileLocationRepo,
      FulfillmentTrackingRecordDao fulfillmentTrackingRecordDao,
      ItemStatusGenerationRequestTrackingRepo itemStatusGenerationRequestTrackingRepo,
      FulfillmentTrackingRecordRepo fulfillmentTrackingRecordRepo) {
    this.gateway = gateway;
    this.itemStatusFileLocationRepo = itemStatusFileLocationRepo;
    this.fulfillmentTrackingRecordDao = fulfillmentTrackingRecordDao;
    this.itemStatusGenerationRequestTrackingRepo = itemStatusGenerationRequestTrackingRepo;
    this.fulfillmentTrackingRecordRepo = fulfillmentTrackingRecordRepo;
  }

  @Async
  public void generateItemStatusFile(List<RequestDetail> requests,
      ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc) {
    try {
      ItemStatusFileLocationDoc itemStatusFileLocationDoc = new ItemStatusFileLocationDoc();
      String uuid = itemStatusGenerationRequestTrackingDoc.getId();
      log.info("Generating item status files for request received with id: {}", uuid);
      itemStatusFileLocationDoc.setId(uuid);
      List<String> requestIds = new ArrayList<>();
      requests.forEach(requestDetail -> requestIds.add(requestDetail.getRequestId()));
      boolean areRequestIdsValid = fulfillmentTrackingRecordDao.areRequestIdsValid(requestIds);
      if (areRequestIdsValid) {
        boolean areNotAllRequestsTaggedWithBulkId = fulfillmentTrackingRecordDao
            .existsRequestNotTaggedWithBulkRequestId(requestIds);
        if (!areNotAllRequestsTaggedWithBulkId) {
          handleError(itemStatusGenerationRequestTrackingDoc,
              "Not all request ids are tagged with bulk request ids.");
        } else {
          List<FulfillmentTrackingRecordDoc> fulfillmentTrackingRecordDocs = fulfillmentTrackingRecordDao
              .getFulfillmentTrackingRecordDocs(requestIds);
          Set<String> bulkRequestIds = new HashSet<>();
          fulfillmentTrackingRecordDocs.forEach(fulfillmentTrackingRecordDoc -> bulkRequestIds
              .add(fulfillmentTrackingRecordDoc.getBulkRequestId()));
          bulkRequestIds.forEach(bulkRequestId -> {
            ItemStatusFile itemStatusFile = generateItemStatusFile(bulkRequestId,
                fulfillmentTrackingRecordDocs);
            itemStatusFileLocationDoc.getItemStatusFiles().add(itemStatusFile);
          });
          itemStatusFileLocationRepo.save(itemStatusFileLocationDoc);
          log.info("Successfully processed request for item status files generation with id: {}",
              uuid);
        }
      } else {
        handleError(itemStatusGenerationRequestTrackingDoc, "Not all requests ids are valid.");
      }
    } catch (Exception exception) {
      log.error("Error occurred while generating item status files.", exception);
      handleError(itemStatusGenerationRequestTrackingDoc,
          exception.getMessage());
    }
  }

  private void handleError(
      ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc,
      String message) {
    log.error("Could not process request for uuuid: {}. {}",
        new Object[]{itemStatusGenerationRequestTrackingDoc.getId(), message});
    itemStatusGenerationRequestTrackingDoc.setStatus(STATUS.FAILED);
    itemStatusGenerationRequestTrackingDoc.setStatusDetail(message);
    itemStatusGenerationRequestTrackingRepo.save(itemStatusGenerationRequestTrackingDoc);
  }

  private ItemStatusFile generateItemStatusFile(String bulkRequestId,
      List<FulfillmentTrackingRecordDoc> fulfillmentTrackingRecordDocs) {
    try {
      FulfillmentTrackingRecordDoc bulkRequestDoc = fulfillmentTrackingRecordRepo
          .findByRequestId(bulkRequestId);
      if (FulfillmentStatus.SENT_TO_SUPPLIER.equals(bulkRequestDoc.getCurrentFulfillmentStatus())) {
        ItemStatusFile itemStatusFile = new ItemStatusFile();
        itemStatusFile.setBulkRequestId(bulkRequestId);
        log.info("Generating item status file for bulk request with id: {}", bulkRequestId);
        List<FulfillmentTrackingRecordDoc> matchingIndividualRequestDocs = fulfillmentTrackingRecordDocs
            .stream()
            .filter(fulfillmentTrackingRecordDoc -> bulkRequestId
                .equals(fulfillmentTrackingRecordDoc.getBulkRequestId()))
            .collect(Collectors.toList());
        BatchStatus batchStatus = new BatchStatus();
        FulfillmentRequest bulkRequestDocFulfillmentRequest = bulkRequestDoc
            .getFulfillmentRequest();
        batchStatus.setBatchStatusHeader(
            RequestHeader.from(bulkRequestDocFulfillmentRequest.getRequestHeader()));
        batchStatus.setBatchStatusTrailer(
            RequestTrailer.from(bulkRequestDocFulfillmentRequest.getRequestTrailer()));
        batchStatus.setBatchStatusItems(getBatchStatusItems(matchingIndividualRequestDocs));
        Marshaller marshaller = JAXBContext.newInstance(BatchStatus.class).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(batchStatus, stringWriter);
        log.info("Successfully generated item status file for bulk request with id: {}",
            bulkRequestId);
        String filePath = remoteDirectory + UUID.randomUUID().toString() + ".xml";
        gateway.send(stringWriter.toString().getBytes(), filePath);
        log.info("Successfully generated item status file for bulk request with id: {}",
            bulkRequestId);
        itemStatusFile.setItemStatusFilePath(filePath);
        return itemStatusFile;
      } else {
        log.error("Bulk request with id {} not sent to supplier.", bulkRequestId);
        throw new RuntimeException(
            "Bulk request with id " + bulkRequestId + "not sent to supplier.");
      }
    } catch (JAXBException exception) {
      throw new RuntimeException(exception);
    }
  }

  private BatchStatusItems getBatchStatusItems(
      List<FulfillmentTrackingRecordDoc> matchingIndividualRequestDocs) {
    BatchStatusItems batchStatusItems = new BatchStatusItems();
    matchingIndividualRequestDocs.forEach(fulfillmentTrackingRecordDoc -> {
      batchStatusItems.getBatchItemStatusDetail()
          .add(getBatchItemStatusDetail(fulfillmentTrackingRecordDoc));
    });
    return batchStatusItems;
  }

  private BatchItemStatusDetail getBatchItemStatusDetail(
      FulfillmentTrackingRecordDoc fulfillmentTrackingRecordDoc) {
    BatchItemStatusDetail batchItemStatusDetail = new BatchItemStatusDetail();
    FulfillmentRequest fulfillmentRequest = fulfillmentTrackingRecordDoc.getFulfillmentRequest();
    batchItemStatusDetail.setBatchItemRequest(
        FulfillmentRequest.from(fulfillmentRequest));
    fulfillmentRequest.getRequestDetail().getTransactionalRequestDetail().getRecipientList()
        .forEach(recipient -> {
          batchItemStatusDetail.getBatchItemEventLog()
              .add(getBatchItemEventLog(recipient));
        });
    return batchItemStatusDetail;
  }

  private RequestHistoryType getBatchItemEventLog(Recipient recipient) {
    RequestHistoryType requestHistory = OBJECT_FACTORY.createRequestHistoryType();
    requestHistory.setProcessor("PRINT_SUPPLIER");
    requestHistory.setEventType(EventType.RECEIVED.getValue());
    requestHistory.setDeliveryMethodCd(recipient.getDeliveryMethod1());
    requestHistory.setDispatchedDate(DateUtil.getISODateTime(new Date()));
    requestHistory.setExceptionCount(BigInteger.ZERO);
    requestHistory.setSuccessCount(BigInteger.ONE);
    requestHistory.setStatusCode(StatusCodeType.ACCEPTED.getCode());
    requestHistory.setRecipientId(recipient.getRecipientId());
    requestHistory.setReceivedDate(DateUtil.getISODateTime(DateTime.now().minusDays(1).toDate()));
    return requestHistory;
  }

}
