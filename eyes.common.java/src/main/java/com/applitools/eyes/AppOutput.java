package com.applitools.eyes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An application output (title, image, etc).
 */
@JsonIgnoreProperties({"screenshot64"})
public class AppOutput {

    /**
     * The title of the screen of the application being captured.
     */
    private final String title;
    private final String domUrl;
    private final String screenshotUrl;
    private final String screenshot64;

    /**
     * @param title         The title of the window.
     * @param screenshot64  Base64 encoding of the screenshot's bytes.
     * @param domUrl        A URL to a DOM snapshot.
     * @param screenshotUrl A URL to a screenshot.
     */
    public AppOutput(String title, String screenshot64, String domUrl, String screenshotUrl) {
        this.title = title;
        this.screenshot64 = screenshot64;
        this.domUrl = domUrl;
        this.screenshotUrl = screenshotUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getScreenshot64() {
        return screenshot64;
    }

    public String getDomUrl() {
        return domUrl;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }
}