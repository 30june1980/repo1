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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    private static final String COULDN_T_READ_FROM_THE_CSV_FILE = "Couldn't read from the CSV file";

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
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIDO);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyPage.getRequestType()));
    }

    @Test
    public void transactionalExternalDataOnlyTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TEDO);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tedo.equals(transactionalExternalDataOnlyPage.getRequestType()));
    }

    @Test
    public void bulkDataOnlyTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BDO);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(bdo.equals(bulkDataOnlyPage.getRequestType()));
    }

    @Test
    public void bulkPrintReadyTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BPR);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(bpr.equals(bulkPrintReadyPage.getRequestType()));
    }

    @Test
    public void edmsuiTransactionalExternalPrintReadyTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.EUTEPR);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tepr.equals(edmsuiTransactionalExternalPrintReadyPage.getRequestType()));
    }

    @Test
    public void transactionalExternalPrintReadyTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TEPR);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tepr.equals(transactionalExternalPrintReadyPage.getRequestType()));
    }

    @Test
    public void transactionalInlineDataOnlyBatchableTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIDOB);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));

        String filePath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnlyBatchable.xml").getPath();


        Map<String, String> map = XmlUtils.readXml(filePath);
        Assert.assertTrue(map.get("sourceID").equals(transactionalInlineDataOnlyBatchablePage.getRequestor()));
        Assert.assertTrue(map.get("destinationID").equals(transactionalInlineDataOnlyBatchablePage.getVendor()));
        Assert.assertTrue(map.get("businessSegmentID").equals(transactionalInlineDataOnlyBatchablePage.getBs()));
        Assert.assertTrue(map.get("requestCategory").equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));
        Assert.assertTrue(map.get("marketSegmentCd").equals(transactionalInlineDataOnlyBatchablePage.getMs()));
        Assert.assertTrue(map.get("fulfillmentType").equals(transactionalInlineDataOnlyBatchablePage.getMaterialType()));
        Assert.assertTrue(map.get("dataFormat").equals(transactionalInlineDataOnlyBatchablePage.getDataFormat()));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getDeliveryMethod().contains(map.get("deliveryMethod1")));
        Assert.assertTrue(map.get("emailAddress").equals(transactionalInlineDataOnlyBatchablePage.getRecipientEmail()));
        Assert.assertTrue(map.get("faxNumber").equals(transactionalInlineDataOnlyBatchablePage.getRecipientFax()));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getLnameFname().contains(map.get("lastName")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("Address1")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("Address2")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("Address3")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getReturnToAddress().contains(map.get("City")));
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getTemplateName().contains(map.get("templateName")));
    }

    @Test
    public void transactionalInlinePrintReadyMultiItemNonBatchableSingleItemTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRMI_NBSI);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprmi.equals(transactionalInlinePrintReadyMultiItemNonBatchableSingleItemPage.getRequestType()));
    }

    @Test
    public void transactionalInlinePrintReadyMultiItemTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRMI);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprmi.equals(transactionalInlinePrintReadyMultiItemPage.getRequestType()));
    }

    @Test
    public void transactionalInlinePrintReadySingleItemTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRSI);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprsi.equals(transactionalInlinePrintReadySingleItemPage.getRequestType()));
    }

    @Test
    public void verifyTransactionalInlineDataOnlyPage() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIDO);
        } catch (IOException exception) {
            throw new RuntimeException(COULDN_T_READ_FROM_THE_CSV_FILE, exception);
        }


        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        String filePath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml").getPath();
        Map<String, String> map = XmlUtils.readXml(filePath);
        Assert.assertTrue(map.get("sourceID").equals(transactionalInlineDataOnlyPage.getRequestor()));
        Assert.assertTrue(map.get("destinationID").equals(transactionalInlineDataOnlyPage.getVendor()));
        Assert.assertTrue(map.get("businessSegmentID").equals(transactionalInlineDataOnlyPage.getBusinessSegment()));
        Assert.assertTrue(map.get("requestCategory").equals(transactionalInlineDataOnlyPage.getRequestType()));
        Assert.assertTrue(map.get("marketSegmentCd").equals(transactionalInlineDataOnlyPage.getMarketSegment()));
        Assert.assertTrue(map.get("fulfillmentType").equals(transactionalInlineDataOnlyPage.getMaterialType()));
        Assert.assertTrue(map.get("dataFormat").equals(transactionalInlineDataOnlyPage.getDataFormat()));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getDeliveryMethods().contains(map.get("deliveryMethod1")));
        Assert.assertTrue(map.get("emailAddress").equals(transactionalInlineDataOnlyPage.getRecipientEmail()));
        Assert.assertTrue(map.get("faxNumber").equals(transactionalInlineDataOnlyPage.getFax()));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(map.get("lastName")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(map.get("firstName")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("Address1")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("Address2")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("Address3")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("City")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("State")));
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(map.get("Zip")));
    }

}
