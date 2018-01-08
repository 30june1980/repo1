package com.shutterfly.missioncontrolportal.pageobject;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends ConfigLoaderWeb {

    @Test
    public void loginTest() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        String url = "http://missioncontrolportal-qa.internal.shutterfly.com/loginpage";
        loginPage.login(url);
        Assert.assertEquals("FulfillmenthubWeb", driver.getTitle());
    }
}
