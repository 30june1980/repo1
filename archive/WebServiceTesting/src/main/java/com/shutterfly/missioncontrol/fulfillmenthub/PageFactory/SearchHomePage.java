package com.shutterfly.missioncontrolservices.fulfillmenthub.PageFactory;

import com.shutterfly.missioncontrolservices.fulfillmenthub.util.FileDataProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Created by kedar on 28-09-2016.
 */
public class SearchHomePage {

    private WebDriver driver;


    FileDataProvider fldp = new FileDataProvider();

    @FindBy(xpath = ".//*[@id='sb_ifc0']")
    private WebElement searchBox;

    @FindBy(xpath = ".//*[@id='sblsbb']")
    private WebElement searchButton;

    @FindBy(xpath = ".//*[@name='btnI']")
    private WebElement iAmFeelingLuckyButton;

    public SearchHomePage(WebDriver driver){
        this.driver=driver;
        PageFactory.initElements(driver, this);
    }
    String searchphrase =  fldp.getProperty("searchString");
    public void enterSearchPhrase(){

        searchBox.sendKeys(searchphrase);
    }
    public SearchResultsPage clickSearchButton(){
        searchButton.click();
        SearchResultsPage searchresultpage = PageFactory.initElements(driver,SearchResultsPage.class);
        return searchresultpage;
    }

}
