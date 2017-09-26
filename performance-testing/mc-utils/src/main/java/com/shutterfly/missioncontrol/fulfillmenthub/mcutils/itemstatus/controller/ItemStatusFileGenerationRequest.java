package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.controller;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileGenerationRequest {

  private List<RequestDetail> requests;

  public static final ItemStatusFileGenerationRequest from(String request, String delimiter) {
    ItemStatusFileGenerationRequest itemStatusFileGenerationRequest = new ItemStatusFileGenerationRequest();
    ArrayList<RequestDetail> requests = new ArrayList<>();
    String[] requestIds = request.split(delimiter);
    for (String requestId : requestIds) {
      requests.add(new RequestDetail(requestId));
    }
    itemStatusFileGenerationRequest.setRequests(requests);
    return itemStatusFileGenerationRequest;
  }

}