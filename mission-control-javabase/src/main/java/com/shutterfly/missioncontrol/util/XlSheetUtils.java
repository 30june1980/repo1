package com.shutterfly.missioncontrol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class XlSheetUtils {

  public Map<String ,List<String>> readSheetsFromXlFile(String fileName,int noofSheetsWithSameInputDataSchema){

  }

  private FileInputStream getFile(String filePath) throws FileNotFoundException {
    if(Objects.isNull(filePath))
      throw new RuntimeException("fileN name cannot be null");
    return new FileInputStream(new File(filePath));
  }
}
