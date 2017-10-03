package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.service;

import com.rits.cloning.Cloner;
import com.shutterfly.missioncontrol.fulfillmenthub.core.doc.FulfillmentTrackingRecordDoc;
import com.shutterfly.missioncontrol.fulfillmenthub.core.dto.FulfillmentTrackingDetailsDto;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.FulfillmentRequest;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.Recipient;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.RequestHeader;
import com.shutterfly.missioncontrol.fulfillmenthub.core.subdoc.RequestTrailer;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.date.DateUtil;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.EventType;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.FulfillmentStatus;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.ParticipantType;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.RequestCategory;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.RequestType;
import com.shutterfly.missioncontrol.fulfillmenthub.core.util.validation.constant.StatusCodeType;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration.FtpGateway;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration.SftpGateway;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.controller.ItemStatusFileGenerationRequest;
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
import com.uhc.dms_fsl.fulfillment.schema.v2.RecipientType;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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

  private static final Cloner CLONER = new Cloner();

  private FtpGateway gateway;

  private SftpGateway sftpGateway;

  @Value("${mc.ftp.remote.dir}")
  private String remoteDirectory;

  @Value("${mc.use.sftp.protocol}")
  private boolean useSftp;

  private FulfillmentTrackingRecordDao fulfillmentTrackingRecordDao;

  @Autowired
  public ItemStatusFileService(FtpGateway gateway,
      SftpGateway sftpGateway,
      ItemStatusFileLocationRepo itemStatusFileLocationRepo,
      FulfillmentTrackingRecordDao fulfillmentTrackingRecordDao,
      ItemStatusGenerationRequestTrackingRepo itemStatusGenerationRequestTrackingRepo,
      FulfillmentTrackingRecordRepo fulfillmentTrackingRecordRepo) {
    this.gateway = gateway;
    this.sftpGateway = sftpGateway;
    this.itemStatusFileLocationRepo = itemStatusFileLocationRepo;
    this.fulfillmentTrackingRecordDao = fulfillmentTrackingRecordDao;
    this.itemStatusGenerationRequestTrackingRepo = itemStatusGenerationRequestTrackingRepo;
    this.fulfillmentTrackingRecordRepo = fulfillmentTrackingRecordRepo;
  }

  @Async
  public Set<String> generateItemStatusFile(List<RequestDetail> requests,
      ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc) {
    final Set<String> bulkRequestIds = new HashSet<>();
    final List<String> requestIds = new ArrayList<>();
    try {
      ItemStatusFileLocationDoc itemStatusFileLocationDoc = new ItemStatusFileLocationDoc();
      String uuid = itemStatusGenerationRequestTrackingDoc.getId();
      log.info("Generating item status files for request received with id: {}", uuid);
      itemStatusFileLocationDoc.setId(uuid);
      requests.forEach(requestDetail -> requestIds.add(requestDetail.getRequestId()));
      boolean areNotAllRequestsTaggedWithBulkId = fulfillmentTrackingRecordDao
          .existsRequestNotTaggedWithBulkRequestId(requestIds);
      if (areNotAllRequestsTaggedWithBulkId) {
        handleError(itemStatusGenerationRequestTrackingDoc,
            "Not all request ids are tagged with bulk request ids.");
      } else {
        itemStatusGenerationRequestTrackingDoc.setStatus(STATUS.PROCESSING);
        itemStatusGenerationRequestTrackingDoc
            .setStatusDetail("Item status file generated successfully.");
        itemStatusGenerationRequestTrackingRepo.save(itemStatusGenerationRequestTrackingDoc);
        List<FulfillmentTrackingRecordDoc> fulfillmentTrackingRecordDocs = fulfillmentTrackingRecordDao
            .getFulfillmentTrackingRecordDocs(requestIds);
        fulfillmentTrackingRecordDocs.forEach(fulfillmentTrackingRecordDoc -> bulkRequestIds
            .add(fulfillmentTrackingRecordDoc.getBulkRequestId()));
        generateItemStatusFile(itemStatusGenerationRequestTrackingDoc, itemStatusFileLocationDoc,
            uuid,
            fulfillmentTrackingRecordDocs, bulkRequestIds);
      }
    } catch (Exception exception) {
      log.error("Error occurred while generating item status files.", exception);
      handleError(itemStatusGenerationRequestTrackingDoc,
          exception.getMessage());
    }
    if (useSftp) {
      sftpGateway.send(bulkRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "BulkRequestIds.csv");
      sftpGateway.send(requestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "ProcessRequestIds.csv");
    } else {
      gateway.send(bulkRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "BulkRequestIds.csv");
      gateway.send(requestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "ProcessRequestIds.csv");
    }
    return bulkRequestIds;
  }

  private void generateItemStatusFile(
      ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc,
      ItemStatusFileLocationDoc itemStatusFileLocationDoc, String uuid,
      List<FulfillmentTrackingRecordDoc> fulfillmentTrackingRecordDocs,
      Set<String> bulkRequestIds) {
    bulkRequestIds.forEach(bulkRequestId -> {
      List<FulfillmentTrackingRecordDoc> matchingIndividualRequestDocs = fulfillmentTrackingRecordDocs
          .stream()
          .filter(fulfillmentTrackingRecordDoc -> bulkRequestId
              .equals(fulfillmentTrackingRecordDoc.getBulkRequestId()))
          .collect(Collectors.toList());
      ItemStatusFile itemStatusFile = generateItemStatusFile(bulkRequestId,
          matchingIndividualRequestDocs);
      itemStatusFileLocationDoc.getItemStatusFiles().add(itemStatusFile);
    });
    itemStatusFileLocationRepo.save(itemStatusFileLocationDoc);
    log.info("Successfully processed request for item status files generation with id: {}",
        uuid);
    itemStatusGenerationRequestTrackingDoc.setStatus(STATUS.COMPLETE);
    itemStatusGenerationRequestTrackingDoc
        .setStatusDetail("Item status file generated successfully.");
    itemStatusGenerationRequestTrackingRepo.save(itemStatusGenerationRequestTrackingDoc);
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
      List<FulfillmentTrackingRecordDoc> processRequestDocs) {
    try {
      FulfillmentTrackingRecordDoc bulkRequestDoc = fulfillmentTrackingRecordRepo
          .findByRequestId(bulkRequestId);
      if (FulfillmentStatus.SENT_TO_SUPPLIER.getValue()
          .equals(bulkRequestDoc.getCurrentFulfillmentStatus())) {
        ItemStatusFile itemStatusFile = new ItemStatusFile();
        itemStatusFile.setBulkRequestId(bulkRequestId);
        log.info("Generating item status file for bulk request with id: {}", bulkRequestId);
        BatchStatus batchStatus = new BatchStatus();
        FulfillmentRequest bulkRequestDocFulfillmentRequest = bulkRequestDoc
            .getFulfillmentRequest();
        batchStatus.setBatchStatusHeader(
            RequestHeader.from(bulkRequestDocFulfillmentRequest.getRequestHeader()));
        batchStatus.getBatchStatusHeader().setBulkRequestHeaderID(StringUtils.EMPTY);
        batchStatus.getBatchStatusHeader().setRequestType(RequestType.POSTSTATUS.getValue());
        batchStatus.getBatchStatusHeader()
            .setSourceID(FulfillmentTrackingDetailsDto.getVendor(bulkRequestDoc));
        batchStatus.getBatchStatusHeader()
            .setDestinationID(ParticipantType.getHub().getName());
        batchStatus.setBatchStatusItems(getBatchStatusItems(processRequestDocs));
        batchStatus.setBatchStatusTrailer(
            RequestTrailer.from(bulkRequestDocFulfillmentRequest.getRequestTrailer()));
        batchStatus.getBatchStatusTrailer().setRequestItemCount(
            batchStatus.getBatchStatusItems().getBatchItemStatusDetail().size());
        Marshaller marshaller = JAXBContext.newInstance(BatchStatus.class).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(batchStatus, stringWriter);
        log.info("Successfully generated item status file for bulk request with id: {}",
            bulkRequestId);
        String fileName = bulkRequestId + ".xml";
        String filePath = remoteDirectory + fileName;
        if (useSftp) {
          sftpGateway.send(stringWriter.toString().getBytes(), filePath);
        } else {
          gateway.send(stringWriter.toString().getBytes(), filePath);
        }
        log.info("Successfully generated item status file for bulk request with id: {}",
            bulkRequestId);
        itemStatusFile.setFolder(remoteDirectory);
        itemStatusFile.setItemStatusFileName(fileName);
        return itemStatusFile;
      } else {
        log.error("Bulk request with id {} not sent to supplier.", bulkRequestId);
        throw new RuntimeException(
            "Bulk request with id " + bulkRequestId + " not sent to supplier.");
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
    List<RecipientType> recipients = batchItemStatusDetail.getBatchItemRequest().getRequestDetail()
        .getTransactionalRequestDetail().getRecipient();
    List<RecipientType> recipientsToBeAdded = new ArrayList<>();
    recipients.forEach(recipient -> {
          recipientsToBeAdded.add(cloneRecipient(recipient));
        }
    );
    recipients.addAll(recipientsToBeAdded);
    fulfillmentRequest.getRequestDetail().getTransactionalRequestDetail().getRecipientList()
        .forEach(recipient -> {
          batchItemStatusDetail.getBatchItemEventLog()
              .add(
                  getBatchItemEventLog(recipient.getRecipientId(), recipient.getDeliveryMethod1()));
          batchItemStatusDetail.getBatchItemEventLog()
              .add(
                  getBatchItemEventLog(recipient.getRecipientId(), recipient.getDeliveryMethod2()));
        });
    return batchItemStatusDetail;
  }

  private RecipientType cloneRecipient(RecipientType recipient) {
    RecipientType recipientWithDeliveryMethodCode2 = CLONER.deepClone(recipient);
    recipientWithDeliveryMethodCode2.setDeliveryMethod1(recipient.getDeliveryMethod2());
    recipient.setDeliveryMethod2(null);
    recipientWithDeliveryMethodCode2.setDeliveryMethod2(null);
    return recipientWithDeliveryMethodCode2;
  }

  private RequestHistoryType getBatchItemEventLog(String recipientId, String deliveryMethodCode) {
    RequestHistoryType requestHistory = OBJECT_FACTORY.createRequestHistoryType();
    requestHistory.setProcessor("PRINT_SUPPLIER");
    requestHistory.setEventType(EventType.FULFILLED.getValue());
    requestHistory.setDispatchedDate(DateUtil.getISODateTime(new Date()));
    requestHistory.setExceptionCount(BigInteger.ZERO);
    requestHistory.setSuccessCount(BigInteger.ONE);
    requestHistory.setStatusCode(StatusCodeType.ACCEPTED.getCode());
    requestHistory.setReceivedDate(DateUtil.getISODateTime(
        DateTime.now().minusDays(1).toDate()));
    requestHistory.setRecipientId(recipientId);
    requestHistory.setDeliveryMethodCd(deliveryMethodCode);
    return requestHistory;
  }


  @Async
  public void runBatch(int pastMinutesToConsiderBulkRequestsFrom) {
    List<String> processRequestIds = new ArrayList<>();
    List<String> bulkRequestIds = fulfillmentTrackingRecordDao
        .getBulkRequestIdsSentToSupplier(pastMinutesToConsiderBulkRequestsFrom);
    int countOfBulkRequests = bulkRequestIds.size();
    log.info("Generating item status file for {} bulk reques  ts.", countOfBulkRequests);
    bulkRequestIds.forEach(bulkRequestId -> {
      List<FulfillmentTrackingRecordDoc> processFulfillmentRequestDocs = fulfillmentTrackingRecordDao
          .getProcessFulfillmentTrackingRecordDocsForBulkRequestId(bulkRequestId);
      processFulfillmentRequestDocs.forEach(doc -> processRequestIds.add(doc.getRequestId()));
      generateItemStatusFile(bulkRequestId, processFulfillmentRequestDocs);
      log.info(
          "Item status file generated successfully for bulk request id: {}, Number of process requests: {}",
          new Object[]{bulkRequestId, processFulfillmentRequestDocs.size()});
    });
    log.info("Number of bulk requests: {}", countOfBulkRequests);
    log.info("Number of process requests: {}", processRequestIds.size());
    if (useSftp) {
      sftpGateway.send(bulkRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "BulkRequestIdsBatch.csv");
      sftpGateway.send(processRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "ProcessRequestIdsBatch.csv");
    } else {
      gateway.send(bulkRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "BulkRequestIdsBatch.csv");
      gateway.send(processRequestIds.stream().collect(Collectors.joining(",\n")).getBytes(),
          remoteDirectory + "/" + "ProcessRequestIdsBatch.csv");
    }
  }

  public ItemStatusFileGenerationRequestTrackingDoc newItemStatusFileGenerationRequestTrackingDoc(
      ItemStatusFileGenerationRequest itemStatusFileGenerationRequest) {
    ItemStatusFileGenerationRequestTrackingDoc itemStatusGenerationRequestTrackingDoc = new ItemStatusFileGenerationRequestTrackingDoc();
    String uuid = UUID.randomUUID().toString();
    itemStatusGenerationRequestTrackingDoc.setId(uuid);
    itemStatusGenerationRequestTrackingDoc.setStatus(STATUS.NEW);
    itemStatusGenerationRequestTrackingDoc
        .setItemStatusFileGenerationRequest(
            itemStatusFileGenerationRequest);
    itemStatusGenerationRequestTrackingRepo.save(itemStatusGenerationRequestTrackingDoc);
    return itemStatusGenerationRequestTrackingDoc;
  }
}
