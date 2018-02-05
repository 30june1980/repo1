package com.shutterfly.missioncontrolportal;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.Utils.XmlUtils;
import com.shutterfly.missioncontrolportal.pageobject.BulkDataOnlyPage;
import com.shutterfly.missioncontrolportal.pageobject.BulkPrintReadyPage;
import com.shutterfly.missioncontrolportal.pageobject.EDMSUITransactionalExternalPrintReadyPage;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalExternalDataOnlyPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalExternalPrintReadyPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalInlineDataOnlyBatchablePage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalInlineDataOnlyPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalInlinePrintReadyMultiItemPage;
import com.shutterfly.missioncontrolportal.pageobject.TransactionalInlinePrintReadySingleItemPage;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

public class SearchTests extends ConfigLoaderWeb {

    private String portalUrl;

    private final String tido = "TransactionalInlineDataOnly";
    private final String tedo = "TransactionalExternalDataOnly";
    private final String bdo = "BulkDataOnly";
    private final String bpr = "BulkPrintReady";
    private final String tepr = "TransactionalExternalPrintReady";
    private final String tiprmi = "TransactionalInlinePrintReadyMultItem";
    private final String tiprsi = "TransactionalInlinePrintReadySingleItem";

    private PortalPage portalPage;
    private TransactionalExternalDataOnlyPage transactionalExternalDataOnlyPage;
    private TransactionalInlineDataOnlyPage transactionalInlineDataOnlyPage;
    private BulkDataOnlyPage bulkDataOnlyPage;
    private BulkPrintReadyPage bulkPrintReadyPage;
    private TransactionalExternalPrintReadyPage transactionalExternalPrintReadyPage;
    private EDMSUITransactionalExternalPrintReadyPage edmsuiTransactionalExternalPrintReadyPage;
    private TransactionalInlineDataOnlyBatchablePage transactionalInlineDataOnlyBatchablePage;
    private TransactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage transactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage;
    private TransactionalInlinePrintReadyMultiItemPage transactionalInlinePrintReadyMultiItemPage;
    private TransactionalInlinePrintReadySingleItemPage transactionalInlinePrintReadySingleItemPage;

    private Logger logger = LoggerFactory.getLogger(SearchTests.class);

    private CsvReaderWriter crw;


    public SearchTests() {
    }

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        portalUrl = config.getProperty(AppConstants.QA_PORTAL_URL);
        if (portalUrl == null) {
            throw new RuntimeException("QaPortalUrl property not found");
        }

        crw = new CsvReaderWriter();
        portalPage = PageFactory.initElements(driver, PortalPage.class);
        transactionalExternalDataOnlyPage = PageFactory.initElements(driver, TransactionalExternalDataOnlyPage.class);
        transactionalInlineDataOnlyPage = PageFactory.initElements(driver, TransactionalInlineDataOnlyPage.class);
        bulkDataOnlyPage = PageFactory.initElements(driver, BulkDataOnlyPage.class);
        bulkPrintReadyPage = PageFactory.initElements(driver, BulkPrintReadyPage.class);
        edmsuiTransactionalExternalPrintReadyPage = PageFactory.initElements(driver, EDMSUITransactionalExternalPrintReadyPage.class);
        transactionalExternalPrintReadyPage = PageFactory.initElements(driver, TransactionalExternalPrintReadyPage.class);
        transactionalInlineDataOnlyBatchablePage = PageFactory.initElements(driver, TransactionalInlineDataOnlyBatchablePage.class);
        transactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage = PageFactory.initElements(driver, TransactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage.class);
        transactionalInlinePrintReadyMultiItemPage = PageFactory.initElements(driver, TransactionalInlinePrintReadyMultiItemPage.class);
        transactionalInlinePrintReadySingleItemPage = PageFactory.initElements(driver, TransactionalInlinePrintReadySingleItemPage.class);
    }

    @Test
    public void backButtonTest() {
        driver.get(portalUrl);
        portalPage.setRequestIdTxt("1");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());
        portalPage.clickOnIthResult(0);

        Assert.assertTrue(portalPage.isBackButtonDisplayed());

        String oldUrl = driver.getCurrentUrl();
        portalPage.clickOnBackBtn();
        String newUrl = driver.getCurrentUrl();
        Assert.assertNotEquals(oldUrl, newUrl);
    }


    @Test // (dependsOnGroups = "Process_TIDO_Valid_Request_Validation")
    public void transactionalInlineDataOnlyTest() {
        searchByRequestCategory(AppConstants.TIDO);

        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyPage.getRequestType()));
    }

    @Test
    public void transactionalExternalDataOnlyTest() {
        searchByRequestCategory(AppConstants.TEDO);

        Assert.assertTrue(tedo.equals(transactionalExternalDataOnlyPage.getRequestType()));
    }

    @Test
    public void bulkDataOnlyTest() {
        searchByRequestCategory(AppConstants.BDO);

        Assert.assertTrue(bdo.equals(bulkDataOnlyPage.getRequestType()));
    }

    @Test
    public void bulkPrintReadyTest() {
        searchByRequestCategory(AppConstants.BPR);

        Assert.assertTrue(bpr.equals(bulkPrintReadyPage.getRequestType()));
    }

    @Test
    public void edmsuiTransactionalExternalPrintReadyTest() {
        searchByRequestCategory(AppConstants.EUTEPR);

        Assert.assertTrue(tepr.equals(edmsuiTransactionalExternalPrintReadyPage.getRequestType()));
    }

    @Test
    public void transactionalExternalPrintReadyTest() {
        searchByRequestCategory(AppConstants.TEPR);

        Assert.assertTrue(tepr.equals(transactionalExternalPrintReadyPage.getRequestType()));
    }

    @Test
    public void transactionalInlineDataOnlyBatchableTest() {
        searchByRequestCategory(AppConstants.TIDOB);

        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));

        String filePath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnlyBatchable.xml").getPath();


        Map<String, String> map = XmlUtils.readXml(filePath);
        Assert.assertTrue(map.get("sch:sourceID").equals(transactionalInlineDataOnlyBatchablePage.getRequestor()));
        Assert.assertTrue(map.get("sch:destinationID").equals(transactionalInlineDataOnlyBatchablePage.getVendor()));
        Assert.assertTrue(map.get("sch:businessSegmentID").equals(transactionalInlineDataOnlyBatchablePage.getBs()));
        Assert.assertTrue(map.get("sch:requestCategory").equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));
        Assert.assertTrue(map.get("sch:marketSegmentCd").equals(transactionalInlineDataOnlyBatchablePage.getMs()));
        Assert.assertTrue(map.get("sch:fulfillmentType").equals(transactionalInlineDataOnlyBatchablePage.getMaterialType()));
        Assert.assertTrue(map.get("sch:dataFormat").equals(transactionalInlineDataOnlyBatchablePage.getDataFormat()));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getDeliveryMethod().contains(map.get("sch:deliveryMethod1")));
        Assert.assertTrue(map.get("sch:emailAddress").equals(transactionalInlineDataOnlyBatchablePage.getRecipientEmail()));
        Assert.assertTrue(map.get("sch:faxNumber").equals(transactionalInlineDataOnlyBatchablePage.getRecipientFax()));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getLnameFname().contains(map.get("sch:lastName")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("sch:Address1")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("sch:Address2")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("sch:Address3")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("sch:City")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getTemplateName().contains(map.get("sch:templateName")));
    }

    @Test
    public void transactionalInlinePrintReadyMultiItemNonBatchableSingleItemTest() {
        searchByRequestCategory(AppConstants.TIPRMI_NBSI);

        Assert.assertTrue(tiprmi.equals(transactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage.getRequestType()));
    }

    @Test
    public void transactionalInlinePrintReadyMultiItemTest() {
        searchByRequestCategory(AppConstants.TIPRMI);

        Assert.assertTrue(tiprmi.equals(transactionalInlinePrintReadyMultiItemPage.getRequestType()));
    }

    @Test
    public void transactionalInlinePrintReadySingleItemTest() {
        searchByRequestCategory(AppConstants.TIPRSI);

        Assert.assertTrue(tiprsi.equals(transactionalInlinePrintReadySingleItemPage.getRequestType()));
    }

    @Test
    public void verifyTransactionalInlineDataOnlyPage() {
        searchByRequestCategory(AppConstants.TIDO);

        String filePath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml").getPath();
        Map<String, String> map = XmlUtils.readXml(filePath);
        Assert.assertTrue(map.get("sch:sourceID").equals(transactionalInlineDataOnlyPage.getRequestor()));
        Assert.assertTrue(map.get("sch:destinationID").equals(transactionalInlineDataOnlyPage.getVendor()));
        Assert.assertTrue(map.get("sch:businessSegmentID").equals(transactionalInlineDataOnlyPage.getBusinessSegment()));
        Assert.assertTrue(map.get("sch:requestCategory").equals(transactionalInlineDataOnlyPage.getRequestType()));
        Assert.assertTrue(map.get("sch:marketSegmentCd").equals(transactionalInlineDataOnlyPage.getMarketSegment()));
        Assert.assertTrue(map.get("sch:fulfillmentType").equals(transactionalInlineDataOnlyPage.getMaterialType()));
        Assert.assertTrue(map.get("sch:dataFormat").equals(transactionalInlineDataOnlyPage.getDataFormat()));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getDeliveryMethods().contains(map.get("sch:deliveryMethod1")));
        Assert.assertTrue(map.get("sch:emailAddress").equals(transactionalInlineDataOnlyPage.getRecipientEmail()));
        Assert.assertTrue(map.get("sch:faxNumber").equals(transactionalInlineDataOnlyPage.getFax()));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(map.get("sch:lastName")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(map.get("sch:firstName")));
        String returnAddressXml = XmlUtils.readXmlElement(filePath, "sch:ReturnToAddress");
        String returnAddressPage = removeNewLines(transactionalInlineDataOnlyPage.getReturnToAddress());
        Assert.assertEquals(returnAddressXml, returnAddressPage);
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("sch:Zip")));
        String mailToAddressXml = XmlUtils.readXmlElement(filePath, "sch:MailToAddress");
        String mailToAddressPage = removeNewLines(transactionalInlineDataOnlyPage.getMailToAddress());
        Assert.assertEquals(mailToAddressXml, mailToAddressPage);

    }

    private String removeNewLines(@Nonnull String line) {
        if (line.isEmpty()) {
            return line;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String[] words = line.split(",");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            stringBuilder.append(word.replaceAll("\n", "").trim());
            if (i != words.length - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private void searchByRequestCategory(@Nonnull String requestCategory) {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(requestCategory);
        } catch (IOException exception) {
            throw new RuntimeException("Couldn't read from the CSV file", exception);
        }
        System.out.println(record);
        logger.warn(record);
        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();
        Assert.assertTrue(portalPage.areSearchResultsVisible());
        portalPage.clickOnIthResult(0);
    }

}
