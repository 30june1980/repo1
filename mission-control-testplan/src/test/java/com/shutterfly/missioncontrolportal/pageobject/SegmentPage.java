package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class SegmentPage {

    private WebDriver driver;

    private final String successToastXpath = "//span[text()='Success: Segments Updated.']";

    @FindBy(how = How.XPATH, using = "//button[text()='ADD BUSINESS SEGMENT']")
    private WebElement addBusinessSegmentBtn;

    @FindBy(how = How.XPATH, using = "//input[@placeholder='Enter a Name for the New Business Segment']")
    private WebElement businessSegmentNameTxt;

    @FindBy(how = How.XPATH, using = "//button[contains(text(),'Save')]")
    private WebElement saveBtn;

    @FindBy(how = How.XPATH, using = "//input[@placeholder='Enter ID']")
    private WebElement segmentIdTxt;

    @FindBy(how = How.XPATH, using = "//app-segments/div/div[2]/div[2]/div[2]/div/input")
    private WebElement startDateTxt;

    @FindBy(how = How.XPATH, using = "//app-segments/div/div[2]/div[2]/div[3]/div/input")
    private WebElement endDateTxt;

    @FindBy(how = How.XPATH, using = "//text()[contains(.,'SAVE BUSINESS SEGMENT')]/ancestor::button[1]")
    private WebElement saveSegmentBtn;

    @FindBy(how = How.XPATH, using = "//span[@class='glyphicon glyphicon-pencil']")
    private WebElement editSegmentBtn;

    public SegmentPage(WebDriver eDriver) {
        this.driver = eDriver;
    }

    public boolean isToastDisplayed() {
        return driver.findElement(By.xpath(successToastXpath)).isDisplayed();
    }

    // action methods
    public void clickOnAddSegmentBtn() {
        addBusinessSegmentBtn.click();
    }

    public void clickOnSaveBtn() {
        saveBtn.click();
    }

    public void clickOnSaveSegmentBtn() {
        saveSegmentBtn.click();
    }

    public void clickOnEditSegmentBtn() {
        editSegmentBtn.click();
    }

    public void setSegmentName(String segmentName) {
        businessSegmentNameTxt.sendKeys(segmentName);
    }

    public void setSegmentId(String segmentId) {
        segmentIdTxt.sendKeys(segmentId);
    }

    public void setStartDate(String startDate) {
        startDateTxt.clear();
        startDateTxt.sendKeys(startDate);
    }

    public void setEndDate(String endDate) {
        endDateTxt.clear();
        endDateTxt.sendKeys(endDate);
    }

}
