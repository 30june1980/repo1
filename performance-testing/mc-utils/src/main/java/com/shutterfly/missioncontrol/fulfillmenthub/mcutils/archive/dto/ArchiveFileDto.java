package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.archive.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
* Created by Shweta on 04-10-2017.
*/
@Getter
@Setter
@ToString
public class ArchiveFileDto {
  String sourceFilePathAndName;
  String destinationFilePath;
  int noOfFiles;
}
