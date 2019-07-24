package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Calendar;

@Listeners(TestListener.class)
@SuppressWarnings("SpellCheckingInspection")
public class TestSessionStartInfo {



    @Test()
    public void TestSessionInfo(){
        Calendar instance = Calendar.getInstance();
        instance.set(2017, 6, 29, 11, 1, 0);
        BatchInfo batchInfo = new BatchInfo("some batch", instance);
        batchInfo.setId("someBatchId");

        SessionStartInfo sessionStartInfo = new SessionStartInfo(
                "agent",SessionType.SEQUENTIAL,
                "some app",
                "1.0",
                "some test",
                batchInfo,
                "baseline", "some environment",
                new AppEnvironment("windows","test suite", new RectangleSize(234, 456)),
                new ImageMatchSettings(MatchLevel.STRICT, null, true),
                "some branch",
                "parent branch",
                "baseline branch",
                null,
                null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String targetJsonObj = mapper.writeValueAsString(sessionStartInfo);
            String sourceJsonAsString = GeneralUtils.readToEnd(TestDomCapture.class.getResourceAsStream("/sessionStartInfo.json"));
            boolean equals = targetJsonObj.equals(sourceJsonAsString);
            Assert.assertTrue(equals, "JSON strings are different.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
