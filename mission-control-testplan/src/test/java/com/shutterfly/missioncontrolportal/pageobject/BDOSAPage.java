package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class BDOSAPage {

    private WebDriver driver;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(2) > div > span")
    private WebElement requestTypeTxt;

    @FindBy(how = How.XPATH, using = "//text()[contains(.,'Back')]/ancestor::a[1]")
    private WebElement backBtn;

    public BDOSAPage(WebDriver eDriver) {
        this.driver = eDriver;
    }

    public void clickOnBackBtn() {
        backBtn.click();
    }

    public String getRequestType() {
        return requestTypeTxt.getText().trim();
    }

}
