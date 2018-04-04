package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.domain;

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
public class ItemStatusFile {

  private String bulkRequestId;

  private String folder;

  private String itemStatusFileName;

}
