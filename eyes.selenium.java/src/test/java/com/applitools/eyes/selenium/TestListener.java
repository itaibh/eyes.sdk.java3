package com.applitools.eyes.selenium;

import com.applitools.eyes.FloatingMatchSettings;
import com.applitools.eyes.Region;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.metadata.ActualAppOutput;
import com.applitools.eyes.metadata.ImageMatchSettings;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        //System.out.println("onTestStart");
        Object instance = result.getInstance();
        if (instance instanceof TestSetup) {
            TestSetup testSetup = (TestSetup) instance;
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            testSetup.beforeMethod(method.getName());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        //System.out.println("onTestSuccess");
        Object instance = result.getInstance();
        if (instance instanceof TestSetup) {
            if (!afterMethodSuccess((TestSetup) instance)){
                result.setStatus(ITestResult.FAILURE);
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        //System.out.println("onTestFailure");
        Object instance = result.getInstance();
        if (instance instanceof TestSetup) {
            TestSetup testSetup = (TestSetup)instance;
            GeneralUtils.logExceptionStackTrace(testSetup.getEyes().getLogger(), result.getThrowable());
            afterMethodFailure(testSetup);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        //System.out.println("onTestSkipped");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        //System.out.println("onTestFailedButWithinSuccessPercentage");
        Object instance = result.getInstance();
        if (instance instanceof TestSetup) {
            afterMethodFailure((TestSetup) instance);
        }
    }

    private void afterMethodFailure(TestSetup testSetup) {
        Eyes eyes = (Eyes) testSetup.getEyes();
        try {
            if (eyes.getIsOpen()) {
                eyes.close(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eyes.abortIfNotClosed();
            if (testSetup.driver != null) {
                testSetup.driver.quit();
            }
        }
    }

    private boolean afterMethodSuccess(TestSetup testSetup) {
        Eyes eyes = (Eyes) testSetup.getEyes();
        try {
            if (eyes.getIsOpen()) {
                TestResults results = eyes.close();
                if (eyes.getIsDisabled()){
                    eyes.getLogger().log("eyes is disabled.");
                    return true;
                } else if (results == null) {
                    eyes.getLogger().verbose("no results returned from eyes.close()");
                    return true;
                }
                SessionResults resultObject = EyesSeleniumUtils.getSessionResults(eyes.getApiKey(), results);

                ActualAppOutput[] actualAppOutput = resultObject.getActualAppOutput();

                if (actualAppOutput.length > 0) {
                    ImageMatchSettings imageMatchSettings = actualAppOutput[0].getImageMatchSettings();
                    FloatingMatchSettings[] floating = imageMatchSettings.getFloating();
                    Region[] ignoreRegions = imageMatchSettings.getIgnore();
                    Region[] layoutRegions = imageMatchSettings.getLayout();
                    Region[] strictRegions = imageMatchSettings.getStrict();
                    Region[] contentRegions = imageMatchSettings.getContent();

                    if (testSetup.compareExpectedRegions) {
                        if (testSetup.expectedFloatingRegions.size() > 0) {
                            HashSet<FloatingMatchSettings> floatingRegionsSet = new HashSet<>(Arrays.asList(floating));
                            Assert.assertEquals(floatingRegionsSet, testSetup.expectedFloatingRegions, "Floating regions lists differ");
                        }

                        if (testSetup.expectedIgnoreRegions.size() > 0) {
                            HashSet<Region> ignoreRegionsSet = new HashSet<>(Arrays.asList(ignoreRegions));
                            Assert.assertEquals(ignoreRegionsSet, testSetup.expectedIgnoreRegions, "Ignore regions lists differ");
                        }

                        if (testSetup.expectedLayoutRegions.size() > 0) {
                            HashSet<Region> layoutRegionsSet = new HashSet<>(Arrays.asList(layoutRegions));
                            Assert.assertEquals(layoutRegionsSet, testSetup.expectedLayoutRegions, "Layout regions lists differ");
                        }

                        if (testSetup.expectedStrictRegions.size() > 0) {
                            HashSet<Region> strictRegionsSet = new HashSet<>(Arrays.asList(strictRegions));
                            Assert.assertEquals(strictRegionsSet, testSetup.expectedStrictRegions, "Strict regions lists differ");
                        }

                        if (testSetup.expectedContentRegions.size() > 0) {
                            HashSet<Region> contentRegionsSet = new HashSet<>(Arrays.asList(contentRegions));
                            Assert.assertEquals(contentRegionsSet, testSetup.expectedContentRegions, "Content regions lists differ");
                        }
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            eyes.abortIfNotClosed();
            if (testSetup.driver != null) {
                testSetup.driver.quit();
            }
        }
    }


    @Override
    public void onStart(ITestContext context) {
        //System.out.println("onStart");
    }

    @Override
    public void onFinish(ITestContext context) {
        //System.out.println("onFinish");
    }

}