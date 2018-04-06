package com.shutterfly.missioncontrolservices.common;

import com.shutterfly.missioncontrolservices.dataobjects.PortalUser;
import com.shutterfly.missioncontrolservices.dataobjects.TestLoginData;
import com.shutterfly.missioncontrolservices.util.XlSheetUtils;
import com.shutterfly.missioncontrolservices.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadDataFromDataSource {

  public static final String USERNAMEKEY= "UserNames";
  public static final String PASSWORDKEY= "Passwords";
  private String dataSource;
  private static final Logger logger = LoggerFactory.getLogger(ReadDataFromDataSource.class);

  public TestLoginData readLoginData() throws IOException {
    Map<Object,List<Object>> readUserNamePassword;
    readUserNamePassword =new XlSheetUtils().readSpecificSheetNumber(this.dataSource,1);
    return this.mapTestLoginDataFromDataSource(readUserNamePassword);
  }

  private  TestLoginData mapTestLoginDataFromDataSource(Map<Object , List<Object>> readUserNamePassword){
    List<PortalUser> portalUsers=null;
    if( Objects.nonNull(readUserNamePassword) && !readUserNamePassword.isEmpty()) {
      List<Object> userNames=readUserNamePassword.get(USERNAMEKEY);
      List<Object> passwords=readUserNamePassword.get(PASSWORDKEY);
      portalUsers=new ArrayList<>(10);
      for(int i=0;i<userNames.size();i++){
        try {
          portalUsers.add(new PortalUser(String.valueOf(userNames.get(i)),
              Util.getDecryptedString(String.valueOf(passwords.get(i)).trim(), Util.getSecretKey())));
        } catch (Exception e) {
           e.printStackTrace();
           logger.error("failed for user"+userNames.get(i)+e.getMessage());
        }
      }
    }
    return new TestLoginData(portalUsers);
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }
}
