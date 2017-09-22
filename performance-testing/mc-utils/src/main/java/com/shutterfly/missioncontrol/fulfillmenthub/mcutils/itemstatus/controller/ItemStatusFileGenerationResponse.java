package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.controller;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.dto.ItemStatusFileLocationDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileGenerationResponse {

  private ItemStatusFileLocationDto itemStatusFileLocationDto;

}