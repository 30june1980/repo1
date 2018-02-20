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
import org.testng.annotations.BeforeGroups;
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

    private Map<String, String> tidoXmlData;
    private Map<String, String> tidobXmlData;
    private String xmlPath;

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

    @BeforeGroups(groups = "tido")
    private void loadTidoData() {
        xmlPath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml").getPath();
        tidoXmlData = XmlUtils.readXml(xmlPath);
        searchByRequestCategory(AppConstants.TIDO);
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyRequestType() {
        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyPage.getRequestType()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlySourceId() {
        Assert.assertTrue(tidoXmlData.get("sch:sourceID").equals(transactionalInlineDataOnlyPage.getRequestor()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyDestinationId() {
        Assert.assertTrue(tidoXmlData.get("sch:destinationID").equals(transactionalInlineDataOnlyPage.getVendor()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyBusinessSegmentId() {
        Assert.assertTrue(tidoXmlData.get("sch:businessSegmentID").equals(transactionalInlineDataOnlyPage.getBusinessSegment()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyRequestCategory() {
        Assert.assertTrue(tidoXmlData.get("sch:requestCategory").equals(transactionalInlineDataOnlyPage.getRequestType()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyMarketSegmentCd() {
        Assert.assertTrue(tidoXmlData.get("sch:marketSegmentCd").equals(transactionalInlineDataOnlyPage.getMarketSegment()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyFulfillmentType() {
        Assert.assertTrue(tidoXmlData.get("sch:fulfillmentType").equals(transactionalInlineDataOnlyPage.getMaterialType()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyDataFormat() {
        Assert.assertTrue(tidoXmlData.get("sch:dataFormat").equals(transactionalInlineDataOnlyPage.getDataFormat()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyDeliveryMethods() {
        Assert.assertTrue(transactionalInlineDataOnlyPage.getDeliveryMethods().contains(tidoXmlData.get("sch:deliveryMethod1")));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyEmailAddress() {
        Assert.assertTrue(tidoXmlData.get("sch:emailAddress").equals(transactionalInlineDataOnlyPage.getRecipientEmail()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyFaxNumber() {
        Assert.assertTrue(tidoXmlData.get("sch:faxNumber").equals(transactionalInlineDataOnlyPage.getFax()));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyLastName() {
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(tidoXmlData.get("sch:lastName")));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyFirstName() {
        Assert.assertTrue(transactionalInlineDataOnlyPage.getLastFirstName().contains(tidoXmlData.get("sch:firstName")));
    }

    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyZip() {
        Assert.assertTrue(transactionalInlineDataOnlyPage.getReturnToAddress().contains(tidoXmlData.get("sch:Zip")));
    }

    //    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyReturnToAddress() {
        String returnAddressXml = XmlUtils.readXmlElement(xmlPath, "sch:ReturnToAddress");
        String returnAddressPage = removeNewLines(transactionalInlineDataOnlyPage.getReturnToAddress());
        Assert.assertEquals(returnAddressXml, returnAddressPage);
    }

    //    @Test(groups = "tido")
    public void verifyTransactionalInlineDataOnlyMailToAddress() {
        String mailToAddressXml = XmlUtils.readXmlElement(xmlPath, "sch:MailToAddress");
        String mailToAddressPage = removeNewLines(transactionalInlineDataOnlyPage.getMailToAddress());
        Assert.assertEquals(mailToAddressXml, mailToAddressPage);
    }


    @BeforeGroups(groups = "tidob")
    private void loadTidobData() {
        xmlPath = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnlyBatchable.xml").getPath();
        tidobXmlData = XmlUtils.readXml(xmlPath);
        searchByRequestCategory(AppConstants.TIDOB);
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableRequestType() {
        Assert.assertTrue(tido.equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableSourceId() {
        Assert.assertTrue(tidobXmlData.get("sch:sourceID").equals(transactionalInlineDataOnlyBatchablePage.getRequestor()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableDestinationId() {
        Assert.assertTrue(tidobXmlData.get("sch:destinationID").equals(transactionalInlineDataOnlyBatchablePage.getVendor()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableBusinessSegmentId() {
        Assert.assertTrue(tidobXmlData.get("sch:businessSegmentID").equals(transactionalInlineDataOnlyBatchablePage.getBs()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableRequestCategory() {
        Assert.assertTrue(tidobXmlData.get("sch:requestCategory").equals(transactionalInlineDataOnlyBatchablePage.getRequestType()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableMarketSegmentCd() {
        Assert.assertTrue(tidobXmlData.get("sch:marketSegmentCd").equals(transactionalInlineDataOnlyBatchablePage.getMs()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableMaterialType() {
        Assert.assertTrue(tidobXmlData.get("sch:fulfillmentType").equals(transactionalInlineDataOnlyBatchablePage.getMaterialType()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableDataFormat() {
        Assert.assertTrue(tidobXmlData.get("sch:dataFormat").equals(transactionalInlineDataOnlyBatchablePage.getDataFormat()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableDeliveryMethod() {
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getDeliveryMethod().contains(tidobXmlData.get("sch:deliveryMethod1")));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableRecipientEmail() {
        Assert.assertTrue(tidobXmlData.get("sch:emailAddress").equals(transactionalInlineDataOnlyBatchablePage.getRecipientEmail()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableFaxNumber() {
        Assert.assertTrue(tidobXmlData.get("sch:faxNumber").equals(transactionalInlineDataOnlyBatchablePage.getRecipientFax()));
    }

    @Test(groups = "tidob")
    public void verifyTransactionalInlineDataOnlyBatchableLastName() {
        Assert.assertTrue(transactionalInlineDataOnlyBatchablePage.getLnameFname().contains(tidobXmlData.get("sch:lastName")));
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

    //    @Test
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
        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();
        Assert.assertTrue(portalPage.areSearchResultsVisible());
        portalPage.clickOnIthResult(0);
    }

}
