package com.applitools.eyes.selenium.capture;

import com.applitools.eyes.BrowserNames;
import com.applitools.eyes.Logger;
import com.applitools.eyes.UserAgent;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.selenium.SeleniumEyes;
import org.openqa.selenium.TakesScreenshot;

public class ImageProviderFactory {

    public static ImageProvider getImageProvider(UserAgent ua, SeleniumEyes eyes, Logger logger, TakesScreenshot tsInstance) {
        if (ua != null) {
            if (ua.getBrowser().equals(BrowserNames.Firefox)) {
                try {
                    if (Integer.parseInt(ua.getBrowserMajorVersion()) >= 48) {
                        return new FirefoxScreenshotImageProvider(eyes, logger, tsInstance);
                    }
                } catch (NumberFormatException e) {
                    return new TakesScreenshotImageProvider(logger, tsInstance);
                }
            } else if (ua.getBrowser().equals(BrowserNames.Safari)) {
                return new SafariScreenshotImageProvider(eyes, logger, tsInstance, ua);
            }
        }
        return new TakesScreenshotImageProvider(logger, tsInstance);
    }
}
