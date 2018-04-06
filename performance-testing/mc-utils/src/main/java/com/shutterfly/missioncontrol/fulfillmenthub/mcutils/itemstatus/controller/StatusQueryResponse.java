package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.controller;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.entity.ItemStatusFileGenerationRequestTrackingDoc.STATUS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatusQueryResponse {

  private STATUS status;

  private String statusDetail;

}
