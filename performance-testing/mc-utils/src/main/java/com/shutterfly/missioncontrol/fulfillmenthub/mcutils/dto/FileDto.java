package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
* Created by Shweta on 04-10-2017.
*/
@Getter
@Setter
@ToString
public class FileDto {
  String sourceFilePathAndName;
  String destinationFilePath;
  int noOfCopies;
}
