package com.shutterfly.missioncontrol.fulfillmenthub.test;

import com.shutterfly.missioncontrol.fulfillmenthub.PageFactory.SearchHomePage;
import com.shutterfly.missioncontrol.fulfillmenthub.PageFactory.SearchResultsPage;
import com.shutterfly.missioncontrol.fulfillmenthub.util.ExcelReadWrite;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kedar on 28-09-2016.
 */
public class PageFactoryTest {
        WebDriver driver;

    @BeforeClass
    public void setup(){
        //use FF Driver
        driver = new FirefoxDriver();
        driver.get("http://google.com");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }
    @AfterClass
    public void teardown(){
        //Quit Driver
        driver.quit();
    }

    @Test(priority = 1)
    public void openGoogle() throws InterruptedException {
        SearchHomePage searchhomepage = new SearchHomePage(driver);
        searchhomepage.enterSearchPhrase();
        Thread.sleep(5000);
        Assert.assertEquals((driver.getTitle()),"TestNG - Google Search");
        Reporter.log("Test Passed : Searching For TestNG");
    }
    @Test(priority = 2)
    public void searchTestng() throws InterruptedException {
        SearchHomePage searchhomepage = new SearchHomePage(driver);
        searchhomepage.clickSearchButton();
        SearchResultsPage searchresultpage = new SearchResultsPage(driver);
        searchresultpage.clickTopResult();
        Thread.sleep(5000);
        Assert.assertTrue((driver.getTitle()).contains("TestNG"));
        Reporter.log("Test Passed : Search Result page shows search results");
    }
    @Test(priority = 4)
    public void backToGoogleSearchPage() throws InterruptedException {
        SearchResultsPage searchresultpage = new SearchResultsPage(driver);
        Thread.sleep(5000);
        Assert.assertEquals((searchresultpage.checkSearchBoxText()),"TestNG");
        Reporter.log("Test Passed : Back on Google Search Page");
    }
    @Test(priority = 3)
    public void readExcel() throws IOException {
        ExcelReadWrite exrw = new ExcelReadWrite();
        Assert.assertFalse(exrw.excelReadWrite().isEmpty());
        Reporter.log("Test Passed : Compare excel Data with expected");
    }


}
