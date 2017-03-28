package com.shutterfly.mc.fulfillmenthub.sgrid.core;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by kedar on 03-10-2016.
 */
public class StartGrid {

    protected RemoteWebDriver driver ;

    @BeforeClass
    @Parameters({"platform","browserName","remoteurl"})
    public void initDriver(String platform,String browserName,String remoteurl)
            throws MalformedURLException {
        DesiredCapabilities cap = null;
        //RemoteWebDriver Setup based on browser name mentioned in the TestNG suite.xml before execution start
        try {
            if(browserName.equals("firefox")) {
                cap = new DesiredCapabilities().firefox();
                cap.setBrowserName("firefox");
            }
            else if(browserName.equals("chrome")) {
                cap = new DesiredCapabilities().chrome();
                cap.setBrowserName("chrome");
            }
            else if(browserName.equals("iexplore")){
                cap = new DesiredCapabilities().internetExplorer();
                cap.setBrowserName("iexplore");
            }

            cap.setPlatform(Platform.WIN10);
            //remoteURL will depend on the server hub and node setup used for execution and is not standard
            driver = new RemoteWebDriver(new URL(remoteurl), cap);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @AfterClass
    //RemoteWebDriver teardown
    public void teardownDriver(){
        try{
            driver.quit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public RemoteWebDriver driver(){
        //RemoteWebDriver object returned and used by testclasses
        return driver;
    }
}
