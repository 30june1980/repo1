package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.controller;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.RequestDetail;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileGenerationRequest {

  private List<RequestDetail> requests;

}