package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.dto;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.ItemStatusFile;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemStatusFileLocationDto {

  private String id;

  private List<ItemStatusFile> itemStatusFiles=new ArrayList<>();

}
