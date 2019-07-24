package com.applitools.eyes.selenium;

import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;

import java.util.List;

public interface IConfigurationSetter extends com.applitools.eyes.config.IConfigurationSetter {
    IConfigurationSetter setWaitBeforeScreenshots(int waitBeforeScreenshots);

    IConfigurationSetter setStitchMode(StitchMode stitchMode);

    IConfigurationSetter setHideScrollbars(boolean hideScrollbars);

    IConfigurationSetter setHideCaret(boolean hideCaret);

    IConfigurationSetter addBrowsers(RenderBrowserInfo... browsersInfo);

    IConfigurationSetter addBrowser(RenderBrowserInfo browserInfo);

    IConfigurationSetter addBrowser(int width, int height, BrowserType browserType, String baselineEnvName);

    IConfigurationSetter addBrowser(int width, int height, BrowserType browserType);

    IConfigurationSetter addDeviceEmulation(DeviceName deviceName, ScreenOrientation orientation);

    IConfigurationSetter addDeviceEmulation(DeviceName deviceName);

    IConfigurationSetter addDeviceEmulation(DeviceName deviceName, String baselineEnvName);

    IConfigurationSetter addDeviceEmulation(DeviceName deviceName, ScreenOrientation orientation, String baselineEnvName);

//    IConfigurationSetter addDeviceEmulation(int width, int height);
//
//    IConfigurationSetter addDeviceEmulation(int width, int height, double scaleFactor);

    IConfigurationSetter setBrowsersInfo(List<RenderBrowserInfo> browsersInfo);

    IConfigurationSetter setTestName(String testName);

    IConfigurationSetter setForceFullPageScreenshot(boolean forceFullPageScreenshot);

    IConfigurationSetter setRenderingConfig(boolean renderingConfig);

}
