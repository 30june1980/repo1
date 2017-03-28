package com.shutterfly.missioncontrol.fulfillmenthub.PageFactory;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Created by kedar on 28-09-2016.
 */
public class PageLeftMargin {

    /*Wikipedia LOGO*/
    @FindBy(xpath = ".//*[@id='p-logo']")
    private WebElement wikiLogo;

    /*public PageLeftMargin checkWikipediaLogoPresent(){
        wikiLogo.isDisplayed();
        return PageFactory.initElements();
    }*/
}
