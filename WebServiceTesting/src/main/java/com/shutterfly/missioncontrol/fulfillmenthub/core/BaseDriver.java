package com.shutterfly.missioncontrol.fulfillmenthub.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by kedar on 27-09-2016.
 */
public class BaseDriver {
    protected Actions builder = null;
    private WebDriver driver = null;

    @BeforeTest
    public void startDriver(){
        builder = new Actions(driver);
        driver = new FirefoxDriver();
        driver.get("https://google.com");
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterTest
    public void tearDownDriver(){
        driver.quit();
    }
    public static void haltScript() throws InterruptedException {
        Thread.sleep(5000);
    }

    public WebDriver getDriver(){
        return driver;
    }
    public Actions getBuilder(){
        return builder;
    }


}
