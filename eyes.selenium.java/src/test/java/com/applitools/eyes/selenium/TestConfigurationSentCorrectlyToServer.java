package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Map;

public class TestConfigurationSentCorrectlyToServer {

    private static Map<String, String> unmodEnvMap = null;
//    private static Map<String, String> environmentMap;


    static {
        try {
            Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

            Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");

            Object unmodifiableMap = unmodifiableMapField.get(null);

            Class unmodifiableMapClass = Class.forName("java.util.Collections$UnmodifiableMap");
            Field field = getAccessibleField(unmodifiableMapClass, "m");
            Object obj = field.get(unmodifiableMap);
            unmodEnvMap = (Map<String, String>) obj;

//            Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
//            environmentMap = (Map<String, String>) mapField.get(null);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @DataProvider(name = "dp")
    public static Object[][] dp() {
        return new Object[][]{
                {false, "Test Sequence", "Test Sequence Name Env Var"},
                {false, "Test Sequence", null},
                {true, "Test Sequence", null},
                {false, null, "Test Sequence Name Env Var"},
                {false, null, null},
                {true, "Test Sequence", "Test Sequence Name Env Var"},
                {true, null, "Test Sequence Name Env Var"},
                {true, null, null},
        };
    }

    @Test(dataProvider = "dp")
    public void TestEyesConfiguration(boolean useVisualGrid, String sequenceName, String sequenceNameEnvVar) {
        EyesRunner runner = useVisualGrid ? new VisualGridRunner(10) : new ClassicRunner();
        Eyes eyes = new Eyes(runner);

        WebDriver driver = new ChromeDriver();
        driver.get("https://applitools.github.io/demo/TestPages/FramesTestPage/");

        String originalBatchSequence = System.getenv("APPLITOOLS_BATCH_SEQUENCE");
        setEnvironmentVariable("APPLITOOLS_BATCH_SEQUENCE", sequenceNameEnvVar);

        String effectiveSequenceName = sequenceName != null ? sequenceName : sequenceNameEnvVar;

        BatchInfo batchInfo = new BatchInfo(TestsDataProvider.batchInfo.getName() + "_" + effectiveSequenceName, Calendar.getInstance());
        batchInfo.setId(TestsDataProvider.batchInfo.getId() + "_" + effectiveSequenceName);

        if (sequenceName != null) {
            batchInfo.setSequenceName(sequenceName);
        }

        if (sequenceNameEnvVar != null) {
            try {
                setEnvironmentVariable("APPLITOOLS_BATCH_SEQUENCE", originalBatchSequence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Assert.assertEquals(effectiveSequenceName, batchInfo.getSequenceName());

            Configuration conf = new Configuration();
            String testName = "Test - " + (useVisualGrid ? "Visual Grid" : "Selenium");
            conf.setAppName("app").setTestName(testName)
                    .setHostApp("someHostApp").setHostOS("someHostOs")
                    //.setBaselineBranchName("baseline branch")
                    //.setBaselineEnvName("baseline env")
                    .setEnvironmentName("env name")
                    .setBatch(batchInfo);

            eyes.setConfiguration(conf);
            eyes.open(driver);

            eyes.setMatchLevel(MatchLevel.LAYOUT);
            eyes.check(Target.window());

            eyes.setMatchLevel(MatchLevel.CONTENT);
            eyes.check(Target.window());
        } finally {
            driver.quit();
        }

        TestResults results = eyes.close(false);
        SessionResults sessionResults = null;
        try {
            sessionResults = EyesSeleniumUtils.getSessionResults(eyes.getApiKey(), results);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(sessionResults);

        AppEnvironment env = sessionResults.getEnv();
        Assert.assertEquals("someHostOs", env.getOs());
        Assert.assertEquals("someHostApp", env.getHostingApp());

        Assert.assertEquals(batchInfo.getSequenceName(), sessionResults.getStartInfo().getBatchInfo().getSequenceName());
        //Assert.assertEquals("baseline branch", sessionResults.BaselineBranchName);
        //Assert.assertEquals("baseline env", sessionResults.BaselineEnvId);

        Assert.assertNotNull(sessionResults.getActualAppOutput());
        Assert.assertEquals(2, sessionResults.getActualAppOutput().length);
        Assert.assertEquals(MatchLevel.LAYOUT2, sessionResults.getActualAppOutput()[0].getImageMatchSettings().getMatchLevel());
        Assert.assertEquals(MatchLevel.CONTENT, sessionResults.getActualAppOutput()[1].getImageMatchSettings().getMatchLevel());

        TestResultsSummary resultsSummary = runner.getAllTestResults();
        eyes.abortIfNotClosed();
    }


    private static void setEnvironmentVariable(String key, String value) {
        if (value != null) {
            unmodEnvMap.put(key, value);
        }
        else{
            unmodEnvMap.remove(key);
        }

        //environmentMap.put(key, value);
    }

    private static Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    @Test
    public void testSetEnv() {
        String mockKey = "mockKey";
        String mockValue = "mockValue";
        String envVar = System.getenv(mockKey);
        Assert.assertNull(envVar);
        printEnv();
        setEnvironmentVariable(mockKey, mockValue);
        System.out.println("+++++++++++++++++++++++++++++");
        printEnv();
        String newEnvVar = System.getenv(mockKey);

        Assert.assertEquals(newEnvVar, mockValue);

    }

    private void printEnv() {
        Map<String, String> getenv = System.getenv();
        for (Map.Entry entry : getenv.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

}
