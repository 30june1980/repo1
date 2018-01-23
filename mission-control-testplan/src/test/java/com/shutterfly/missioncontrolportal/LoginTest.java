package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrol.common.ReadDataFromDataSource;
import com.shutterfly.missioncontrol.dataobjects.TestLoginData;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import java.io.IOException;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LoginTest extends ConfigLoaderWeb {

  private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderWeb.class);

  ReadDataFromDataSource readDataFromDataSource;
  PortalPage portalPage;
  LoginPage loginPage;

  @BeforeClass
  public void setDataSource() {
    readDataFromDataSource = new ReadDataFromDataSource();
    readDataFromDataSource.setDataSource(testingScenariosXlPath);
    loginPage = PageFactory.initElements(driver, LoginPage.class);
    portalPage=PageFactory.initElements(driver, PortalPage.class);
  }

  @Test
  public void loginTest() {


    String url = config.getProperty("QaPortalUrl");
    try {
      isLoginPerformed(this.readDataFromDataSource.readLoginData(), loginPage, url);
    } catch (IOException e) {
      logger.error(e.getLocalizedMessage());
    }
  }


  private void isLoginPerformed(TestLoginData readUserNamePassword, LoginPage loginPage,
      String url) {

    readUserNamePassword.getPortalUsers().forEach(portalUser -> {
      loginPage.login(url,portalUser.getUserName(), portalUser.getPassword());
      Assert.assertEquals("FulfillmenthubWeb", driver.getTitle());
     // WebElement logoutBtn=PageUtils.waitUntilVisibilityIsLocated(driver,portalPage.getActiveUserNameLbl());
      PageUtils.logout(portalPage);
    });

  }

}
