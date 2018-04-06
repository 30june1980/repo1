package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.itemstatus.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileCopierRequest {

  private String fileName;

  private String destinationPath;

  private int numberOfCopies;

}