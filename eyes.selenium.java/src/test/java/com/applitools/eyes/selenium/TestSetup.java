package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.utils.SeleniumUtils;
import com.applitools.eyes.utils.TestUtils;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITest;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TestSetup implements ITest {

    private static String testNameSuffix = System.getenv("TEST_NAME_SUFFIX");
    private final String mode;

    private boolean useVisualGrid = false;
    StitchMode stitchMode = StitchMode.SCROLL;

    private EyesRunner runner = new ClassicRunner();
    private Capabilities options;
    private String testName;

    public TestSetup(String testSuitName, Capabilities options, String mode) {
        if (testNameSuffix == null) testNameSuffix = "";
        this.testSuitName = testSuitName + testNameSuffix;
        this.options = options;
        this.mode = mode;
        switch (mode) {
            case "VG":
                this.useVisualGrid = true;
                break;
            case "CSS":
                this.stitchMode = StitchMode.CSS;
                break;
            case "SCROLL":
                this.stitchMode = StitchMode.SCROLL;
                break;
        }
    }

    class SpecificTestContextRequirements {

        private Eyes eyes;
        private WebDriver driver;
        private WebDriver webDriver;
        private Map<String, Object> expectedProperties = new HashMap<>();

        public HashSet<FloatingMatchSettings> expectedFloatingRegions = new HashSet<>();
        public HashSet<Region> expectedIgnoreRegions = new HashSet<>();
        public HashSet<Region> expectedLayoutRegions = new HashSet<>();
        public HashSet<Region> expectedStrictRegions = new HashSet<>();
        public HashSet<Region> expectedContentRegions = new HashSet<>();

        public SpecificTestContextRequirements(Eyes eyes) {
            this.eyes = eyes;
        }

        public Eyes getEyes() {
            return this.eyes;
        }

        public WebDriver getWrappedDriver() {
            return this.driver;
        }

        public void setWrappedDriver(WebDriver driver) {
            this.driver = driver;
        }

        public WebDriver getWebDriver() {
            return this.webDriver;
        }

        public void setDriver(WebDriver driver) {
            this.webDriver = driver;
        }
    }

    private Map<Object, SpecificTestContextRequirements> testDataByTestId = new ConcurrentHashMap<>();

    protected String testSuitName;

    protected String testedPageUrl = "http://applitools.github.io/demo/TestPages/FramesTestPage/";
    //protected RectangleSize testedPageSize = new RectangleSize(1200, 800);
    protected RectangleSize testedPageSize = new RectangleSize(700, 460);

    protected boolean compareExpectedRegions = false;

    protected String platform;
    protected boolean forceFPS;

    @BeforeClass(alwaysRun = true)
    public void OneTimeSetUp() {
        if (TestUtils.runOnCI && System.getenv("TRAVIS") != null) {
            System.setProperty("webdriver.chrome.driver", "/home/travis/build/chromedriver"); // for travis build.
        }

        String batchId = System.getenv("APPLITOOLS_BATCH_ID");
        if (batchId != null) {
            TestsDataProvider.batchInfo.setId(batchId);
        }

        this.runner = this.useVisualGrid ? new VisualGridRunner(10) : new ClassicRunner();
    }

    public SpecificTestContextRequirements getTestData(Object testId) {
        return this.testDataByTestId.get(testId);
    }

    public WebDriver getDriver() {
        return getTestData("testId").getWrappedDriver();
    }

    protected WebDriver getWebDriver() {
        return getTestData("testId").getWebDriver();
    }

    public Eyes getEyes() {
        return getTestData("testId").getEyes();
    }

    protected void setExpectedIgnoreRegions(Region... expectedIgnoreRegions) {
        getTestData("testId").expectedIgnoreRegions = new HashSet<>(Arrays.asList(expectedIgnoreRegions));
    }

    protected void setExpectedLayoutRegions(Region... expectedLayoutRegions) {
        getTestData("testId").expectedLayoutRegions = new HashSet<>(Arrays.asList(expectedLayoutRegions));
    }

    protected void setExpectedStrictRegions(Region... expectedStrictRegions) {
        getTestData("testId").expectedStrictRegions = new HashSet<>(Arrays.asList(expectedStrictRegions));
    }

    protected void setExpectedContentRegions(Region... expectedContentRegions) {
        getTestData("testId").expectedContentRegions = new HashSet<>(Arrays.asList(expectedContentRegions));
    }

    protected void setExpectedFloatingsRegions(FloatingMatchSettings... expectedFloatingsRegions) {
        getTestData("testId").expectedFloatingRegions = new HashSet<>(Arrays.asList(expectedFloatingsRegions));
    }

    void beforeMethod(String testName) {
        // Initialize the eyes SDK and set your private API key.
        this.testName = testName + " " + options.getBrowserName() + " (" + this.mode + ")";
        Eyes eyes = initEyes();
        SpecificTestContextRequirements testData = new SpecificTestContextRequirements(eyes);
        testDataByTestId.put("testId", testData);

        if (this.runner instanceof VisualGridRunner) {
            testName += "_VG";
        }

        if (this.stitchMode == StitchMode.SCROLL) {
            testName += "_Scroll";
        }

        RemoteWebDriver webDriver = null;
        String seleniumServerUrl = System.getenv("SELENIUM_SERVER_URL");
        try {
            if (seleniumServerUrl != null) {
                webDriver = new RemoteWebDriver(new URL(seleniumServerUrl), this.options);
            }
        } catch (MalformedURLException ignored) {
        }

        if (webDriver == null) {
            webDriver = (RemoteWebDriver) SeleniumUtils.createWebDriver(this.options);
        }

        eyes.addProperty("Selenium Session ID", webDriver.getSessionId().toString());

        eyes.addProperty("ForceFPS", eyes.getForceFullPageScreenshot() ? "true" : "false");
        eyes.addProperty("Agent ID", eyes.getFullAgentId());

        //IWebDriver webDriver = new RemoteWebDriver(new Uri("http://localhost:4444/wd/hub"), capabilities_);

        TestUtils.setupLogging(eyes, testName + "_" + options.getPlatform());

        eyes.getLogger().log("navigating to URL: " + testedPageUrl);
        WebDriver driver;
        try {
            driver = eyes.open(webDriver, this.testSuitName, testName, testedPageSize);
        } catch (Throwable e) {
            webDriver.quit();
            throw e;
        }
        //string userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        driver.get(testedPageUrl);
        eyes.getLogger().log(testName + ": " + TestsDataProvider.batchInfo.getName());

        testData.setWrappedDriver(driver);
        testData.setDriver(webDriver);
    }

    private Eyes initEyes() {
        Eyes eyes = new Eyes(this.runner);

        String serverUrl = System.getenv("APPLITOOLS_SERVER_URL");
        if (serverUrl != null && serverUrl.length() > 0) {
            eyes.setServerUrl(serverUrl);
        }

        eyes.setHideScrollbars(true);
        eyes.setStitchMode(this.stitchMode);
        eyes.setSaveNewTests(false);
        eyes.setBatch(TestsDataProvider.batchInfo);
        if (System.getenv("APPLITOOLS_USE_PROXY") != null) {
            eyes.setProxy(new ProxySettings("http://127.0.0.1", 8888));
        }
        return eyes;
    }

    @Override
    public String getTestName() {
        return testName;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s, force FPS: %s)",
                this.getClass().getSimpleName(),
                this.options.getBrowserName(),
                this.platform,
                this.forceFPS);
    }
}
