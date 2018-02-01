package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class TransactionalInlineDataOnlyPage {

    private WebDriver driver;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(1) > div > span")
    private WebElement requestDateTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(2) > div > span")
    private WebElement requestTypeTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(3) > div > span")
    private WebElement requestorTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(4) > div > span")
    private WebElement vendorTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(5) > div > span")
    private WebElement businessSegmentTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(6) > div > span")
    private WebElement fulfillmentStatusTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(1) > div > span")
    private WebElement batchedTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(2) > div > span")
    private WebElement associatedBulkIdTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(3) > div > span")
    private WebElement requestorRefNoTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(4) > div > span")
    private WebElement supplierNoTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(5) > div > span")
    private WebElement marketSegmentTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(2) > div:nth-child(6) > div > span")
    private WebElement archiveStatusTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(3) > div:nth-child(2) > div > span")
    private WebElement materialTypeTxt;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(3) > div:nth-child(3) > div > span")
    private WebElement dataFormatTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(1) > div > span")
    private WebElement lastFirstNameTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(2) > div > span")
    private WebElement memberIdTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(3) > div > span")
    private WebElement recipientIdTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(4) > div > span")
    private WebElement recipientIdQualifierTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(5) > div > span")
    private WebElement recipientTypeTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(6) > div > span")
    private WebElement ccRecipientTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(7) > div > span")
    private WebElement documentIdTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(8) > div > span")
    private WebElement deliveryMethodsTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(9) > div > span")
    private WebElement recipientEmailTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(10) > div > span")
    private WebElement faxTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(11) > div > span")
    private WebElement templateNameTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.row > div:nth-child(12) > div > span")
    private WebElement templateIdTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.box-border.margin-top-25 > div.row > div > div:nth-child(1) > div > span")
    private WebElement organizationTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.box-border.margin-top-25 > div.row > div > div:nth-child(2) > div > span")
    private WebElement mailToAddressTxt;

    @FindBy(how = How.CSS, using = "app-transactional > div > div > div.box-border.margin-top-25 > div.row > div > div:nth-child(3) > div > span")
    private WebElement returnToAddressTxt;

    @FindBy(how = How.XPATH, using = "//text()[contains(.,'Back')]/ancestor::a[1]")
    private WebElement backBtn;

    public TransactionalInlineDataOnlyPage(WebDriver eDriver) {
        this.driver = eDriver;
    }

    public void clickOnBackBtn() {
        backBtn.click();
    }

    public String getRequestType() {
        return requestTypeTxt.getText();
    }

    public String getRequestDate() {
        return requestDateTxt.getText();
    }

    public String getReturnToAddress() {
        return returnToAddressTxt.getText();
    }

    public String getMailToAddress() {
        return mailToAddressTxt.getText();
    }

    public String getOrganization() {
        return organizationTxt.getText();
    }

    public String getTemplateId() {
        return templateIdTxt.getText();
    }

    public String getTemplateName() {
        return templateNameTxt.getText();
    }

    public String getFax() {
        return faxTxt.getText();
    }

    public String getRecipientEmail() {
        return recipientEmailTxt.getText();
    }

    public String getDeliveryMethods() {
        return deliveryMethodsTxt.getText();
    }

    public String getDocumentId() {
        return documentIdTxt.getText();
    }

    public String getCcRecipient() {
        return ccRecipientTxt.getText();
    }

    public String getRecipientType() {
        return recipientTypeTxt.getText();
    }

    public String getRecipientIdQualifier() {
        return recipientIdQualifierTxt.getText();
    }

    public String getRecipientId() {
        return recipientIdTxt.getText();
    }

    public String getMemberId() {
        return memberIdTxt.getText();
    }

    public String getLastFirstName() {
        return lastFirstNameTxt.getText();
    }

    public String getAssociatedBulkIdTxt() {
        return associatedBulkIdTxt.getText();
    }

    public String getDataFormat() {
        return dataFormatTxt.getText();
    }

    public String getMaterialType() {
        return materialTypeTxt.getText();
    }

    public String getArchiveStatus() {
        return archiveStatusTxt.getText();
    }

    public String marketSegment() {
        return marketSegmentTxt.getText();
    }

    public String getSupplierNumber() {
        return supplierNoTxt.getText();
    }

    public String getRequestorReferenceNumber() {
        return requestorRefNoTxt.getText();
    }

    public String getAssociatedBulkId() {
        return associatedBulkIdTxt.getText();
    }

    public String getBusinessSegment() {
        return businessSegmentTxt.getText();
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatusTxt.getText();
    }

    public String getBatched() {
        return batchedTxt.getText();
    }

    public String getVendor() {
        return vendorTxt.getText();
    }

    public String getRequestor() {
        return requestorTxt.getText();
    }

}
