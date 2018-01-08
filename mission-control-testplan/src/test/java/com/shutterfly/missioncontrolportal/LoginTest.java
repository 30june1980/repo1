package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;

public class LoginTest extends ConfigLoaderWeb {

    @Test
    public void loginTest() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);

        SecretKey secretKey = Encryption.keyGenerator();
        String url = config.getProperty("QaPortalUrl");
        String userName = config.getProperty("QaPortalUserName");
        String password = null;
        try {
            password = Encryption.decrypt(config.getProperty("QaPortalPassword"), secretKey);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to decrypt username / password");
        }

        loginPage.login(url, userName, password);
        Assert.assertEquals("FulfillmenthubWeb", driver.getTitle());
    }

}
