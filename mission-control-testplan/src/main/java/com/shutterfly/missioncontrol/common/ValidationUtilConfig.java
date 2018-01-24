package com.shutterfly.missioncontrol.common;

import java.util.Objects;

/**
 * Created by Shweta on 12-01-2018.
 */

public class ValidationUtilConfig {

  private static DatabaseValidationUtil databaseValidationUtil;

  private ValidationUtilConfig() {
    throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
  public static DatabaseValidationUtil getInstances() {
    if (Objects.isNull(databaseValidationUtil)) {
      databaseValidationUtil = new DatabaseValidationUtil();
    }
    return databaseValidationUtil;
  }
}
