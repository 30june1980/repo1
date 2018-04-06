package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.controller;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.dto.ItemStatusFileLocationDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileGenerationResponse {

  private ItemStatusFileLocationDto itemStatusFileLocationDto;

}