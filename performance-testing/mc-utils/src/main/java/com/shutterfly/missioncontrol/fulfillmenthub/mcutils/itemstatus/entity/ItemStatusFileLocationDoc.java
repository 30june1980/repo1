package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.entity;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.domain.ItemStatusFile;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.itemstatus.dto.ItemStatusFileLocationDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "item-status-file-location-detail")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemStatusFileLocationDoc {

  @Id
  private String id;

  private List<ItemStatusFile> itemStatusFiles = new ArrayList<>();

  public ItemStatusFileLocationDto toDto() {
    ItemStatusFileLocationDto itemStatusFileLocationDto = new ItemStatusFileLocationDto();
    BeanUtils.copyProperties(this, itemStatusFileLocationDto);
    return itemStatusFileLocationDto;
  }
}