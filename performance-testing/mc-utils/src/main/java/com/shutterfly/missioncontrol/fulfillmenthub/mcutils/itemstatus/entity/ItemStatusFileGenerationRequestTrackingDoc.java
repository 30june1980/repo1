package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.entity;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.controller.ItemStatusFileGenerationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "item-status-file-generation-request-tracking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemStatusFileGenerationRequestTrackingDoc {

  public enum STATUS{
    NEW,
    PROCESSING,
    COMPLETE,
    FAILED
  }

  @Id
  private String id;

  private STATUS status;

  private String statusDetail;

  private ItemStatusFileGenerationRequest itemStatusFileGenerationRequest;

}