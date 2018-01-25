package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.BDOPage;
import com.shutterfly.missioncontrolportal.pageobject.BDOSAPage;
import com.shutterfly.missioncontrolportal.pageobject.BPRPage;
import com.shutterfly.missioncontrolportal.pageobject.BPRSAPage;
import com.shutterfly.missioncontrolportal.pageobject.EUTEPRPage;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import com.shutterfly.missioncontrolportal.pageobject.TEDOPage;
import com.shutterfly.missioncontrolportal.pageobject.TEPRPage;
import com.shutterfly.missioncontrolportal.pageobject.TIDOBPage;
import com.shutterfly.missioncontrolportal.pageobject.TIDOPage;
import com.shutterfly.missioncontrolportal.pageobject.TIPRMINBSIPage;
import com.shutterfly.missioncontrolportal.pageobject.TIPRMIPage;
import com.shutterfly.missioncontrolportal.pageobject.TIPRSIPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

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
    private TEDOPage tedoPage;
    private TIDOPage tidoPage;
    private BDOPage bdoPage;
    private BDOSAPage bdosaPage;
    private BPRPage bprPage;
    private BPRSAPage bprsaPage;
    private TEPRPage teprPage;
    private EUTEPRPage euteprPage;
    private TIDOBPage tidobPage;
    private TIPRMINBSIPage tiprminbsiPage;
    private TIPRMIPage tiprmiPage;
    private TIPRSIPage tiprsiPage;

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
        tedoPage = PageFactory.initElements(driver, TEDOPage.class);
        tidoPage = PageFactory.initElements(driver, TIDOPage.class);
        bdoPage = PageFactory.initElements(driver, BDOPage.class);
        bdosaPage = PageFactory.initElements(driver, BDOSAPage.class);
        bprPage = PageFactory.initElements(driver, BPRPage.class);
        bprsaPage = PageFactory.initElements(driver, BPRSAPage.class);
        euteprPage = PageFactory.initElements(driver, EUTEPRPage.class);
        teprPage = PageFactory.initElements(driver, TEPRPage.class);
        tidobPage = PageFactory.initElements(driver, TIDOBPage.class);
        tiprminbsiPage = PageFactory.initElements(driver, TIPRMINBSIPage.class);
        tiprmiPage = PageFactory.initElements(driver, TIPRMIPage.class);
        tiprsiPage = PageFactory.initElements(driver, TIPRSIPage.class);
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


    //    @Test(dependsOnGroups = "Process_TIDO_Valid_Request_Validation")
    @Test
    public void tidoTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIDO);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tido.equals(tidoPage.getRequestType()));
    }

    @Test
    public void tedoTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TEDO);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tedo.equals(tedoPage.getRequestType()));
    }

    @Test
    public void bdoTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BDO);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(bdo.equals(bdoPage.getRequestType()));
    }

    @Test
    public void bdosaTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BDO_SA);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);

        Assert.assertTrue(bdo.equals(bdosaPage.getRequestType()));
    }

    @Test
    public void bprTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BPR);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(bpr.equals(bprPage.getRequestType()));
    }

    @Test
    public void bprsaTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.BPR_SA);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(bpr.equals(bprsaPage.getRequestType()));
    }

    @Test
    public void euteprTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.EUTEPR);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tepr.equals(euteprPage.getRequestType()));
    }

    @Test
    public void teprTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TEPR);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tepr.equals(teprPage.getRequestType()));
    }

    @Test
    public void tidobTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIDOB);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tido.equals(tidobPage.getRequestType()));
    }

    @Test
    public void tiprminbsiTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRMI_NBSI);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprmi.equals(tiprminbsiPage.getRequestType()));
    }

    @Test
    public void tiprmiTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRMI);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprmi.equals(tiprmiPage.getRequestType()));
    }

    @Test
    public void tiprsiTest() {
        driver.get(portalUrl);
        String record;
        try {
            record = crw.getRequestIdByKeys(AppConstants.TIPRSI);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read from the CSV file");
        }

        portalPage.setRequestIdTxt("\"" + record + "\"");
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());

        portalPage.clickOnIthResult(0);
        Assert.assertTrue(tiprsi.equals(tiprsiPage.getRequestType()));
    }

}
