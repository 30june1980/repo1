package com.shutterfly.missioncontrol.fulfillmenthub.PageFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

/**
 * Created by kedar on 28-09-2016.
 */
public class SearchResultsPage {
    private WebDriver driver;

    @FindBy(xpath = ".//*[@id='rso']/div[1]/div/h3")
    private WebElement topResult;

    @FindBy(xpath = ".//*[@id='logocont']/a/img")
    private WebElement searchPageLink;

    @FindBy(xpath = ".//*[@id='gs_htif0']")
    private WebElement searchbox;

    /*@FindBy(xpath = "./*//*[@id='toc']/ul/li[2]/ul/li[3]/a/span[2]")
    private WebElement linkToConclusion;

    @FindBy(xpath = "./*//*[@id='Conclusion']")
    private WebElement headerConclusion;*/
    public SearchResultsPage(WebDriver driver){
        this.driver=driver;
        PageFactory.initElements(driver, this);
    }


    public void clickTopResult(){

        String linktext = topResult.getText();
        System.out.println(linktext);
    }
    public String checkSearchBoxText(){
        String srptext=searchbox.getText();
        return srptext;
    }
    public void goBackToSearchPage(){

        Assert.assertTrue(searchPageLink.isEnabled());
        System.out.println("Element present");
        searchPageLink.click();
        //SearchHomePage searchHomePage = new SearchHomePage(driver);


    }





}
