package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileGenerationResponse {

  private List<ItemStatusFile> itemStatusFiles=new ArrayList<>();

}