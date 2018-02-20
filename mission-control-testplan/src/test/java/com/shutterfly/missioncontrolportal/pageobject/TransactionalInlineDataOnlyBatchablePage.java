package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class TransactionalInlineDataOnlyBatchablePage {

    private WebDriver driver;

    @FindBy(how = How.CSS, using = "details-component > div > div:nth-child(1) > div:nth-child(2) > div > span")
    private WebElement requestTypeTxt;

    @FindBy(how = How.XPATH, using = "//text()[contains(.,'Back')]/ancestor::a[1]")
    private WebElement backBtn;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[1]/div[3]/div/span")
    private WebElement requestor;

    @FindBy(how = How.XPATH,using="//details-component/div/div[1]/div[4]/div/span")
    private WebElement vendor;

    @FindBy(how=How.XPATH, using = "//details-component/div/div[1]/div[5]/div/span")
    private WebElement bs;

    @FindBy(how=How.XPATH, using = "//details-component/div/div[1]/div[6]/div/span")
    private WebElement fulfillmentStatus;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[2]/div[1]/div/span")
    private WebElement batched;

    @FindBy(how= How.XPATH, using = "//details-component/div/div[2]/div[2]/div/span/a")
    private WebElement assoBulkId;

    @FindBy(how = How.XPATH,using ="//details-component/div/div[2]/div[3]/div/span")
    private WebElement requestorRefNo;

    @FindBy(how=How.XPATH, using="//details-component/div/div[2]/div[4]/div/span")
    private WebElement supplierNo;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[2]/div[5]/div/span")
    private WebElement ms;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[2]/div[6]/div/span")
    private WebElement activeStatus;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[3]/div[2]/div/span")
    private WebElement materialType;

    @FindBy(how = How.XPATH, using = "//details-component/div/div[3]/div[3]/div/span")
    private WebElement dataFormat;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[1]/div/span")
    private WebElement lnameFname;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[2]/div/span")
    private WebElement memberId;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[3]/div/span")
    private WebElement recipientId;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[4]/div/span")
    private WebElement recipientIdQual;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[5]/div/span")
    private WebElement recipientType;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[6]/div")
    private WebElement ccRecipient;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[7]/div/span")
    private WebElement documentId;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[8]/div/span")
    private WebElement deliveryMethod;

    @FindBy(how = How.XPATH,using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[9]/div/span")
    private WebElement recipientEmail;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[10]/div/span")
    private WebElement recipientFax;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[11]/div/span")
    private WebElement templateName;

    @FindBy(how = How.XPATH,using = "//app-request-detail/div[1]/app-transactional/div/div/div[1]/div[12]/div/span")
    private WebElement templateId;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[2]/div[1]/div/div[1]/div/span")
    private WebElement organization;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[2]/div[1]/div/div[2]/div/span")
    private WebElement mailToAddress;

    @FindBy(how = How.XPATH, using = "//app-request-detail/div[1]/app-transactional/div/div/div[2]/div[1]/div/div[3]/div/span")
    private WebElement returnToAddress;

    public TransactionalInlineDataOnlyBatchablePage(WebDriver eDriver) {
        this.driver = eDriver;
    }

    public void clickOnBackBtn() {
        backBtn.click();
    }

    public String getRequestType() {
        return requestTypeTxt.getText().trim();
    }

    public String getRequestTypeTxt() {
        return requestTypeTxt.getText();
    }

    public String getRequestor() {
        return requestor.getText().trim();
    }

    public String getVendor() {
        return vendor.getText().trim();
    }

    public String getBs() {
        return bs.getText().trim();
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus.getText().trim();
    }

    public String getBatched() {
        return batched.getText().trim();
    }

    public String getAssoBulkId() {
        return assoBulkId.getText().trim();
    }

    public String getRequestorRefNo() {
        return requestorRefNo.getText().trim();
    }

    public String getSupplierNo() {
        return supplierNo.getText().trim();
    }

    public String getMs() {
        return ms.getText().trim();
    }

    public String getActiveStatus() {
        return activeStatus.getText().trim();
    }

    public String getMaterialType() {
        return materialType.getText().trim();
    }

    public String getDataFormat() {
        return dataFormat.getText().trim();
    }

    public String getLnameFname() {
        return lnameFname.getText().trim();
    }

    public String getMemberId() {
        return memberId.getText().trim();
    }

    public String getRecipientId() {
        return recipientId.getText().trim();
    }

    public String getRecipientIdQual() {
        return recipientIdQual.getText().trim();
    }

    public String getRecipientType() {
        return recipientType.getText().trim();
    }

    public String getCcRecipient() {
        return ccRecipient.getText().trim();
    }

    public String getDocumentId() {
        return documentId.getText().trim();
    }

    public String getDeliveryMethod() {
        return deliveryMethod.getText().trim();
    }

    public String getRecipientEmail() {
        return recipientEmail.getText().trim();
    }

    public String getRecipientFax() {
        return recipientFax.getText().trim();
    }

    public String getTemplateName() {
        return templateName.getText().trim();
    }

    public String getTemplateId() {
        return templateId.getText().trim();
    }

    public String getOrganization() {
        return organization.getText().trim();
    }

    public String getMailToAddress() {
        return mailToAddress.getText().trim();
    }

    public String getReturnToAddress() {
        return returnToAddress.getText().trim();
    }
}
