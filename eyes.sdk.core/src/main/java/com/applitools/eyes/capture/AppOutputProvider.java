package com.applitools.eyes.capture;

import com.applitools.eyes.EyesScreenshot;
import com.applitools.eyes.Location;
import com.applitools.eyes.Region;
import com.applitools.eyes.fluent.ICheckSettingsInternal;

/**
 * Encapsulates a callback which returns an application output.
 */
public interface AppOutputProvider {
    AppOutputWithScreenshot getAppOutput(Region region, EyesScreenshot lastScreenshot,
                                         ICheckSettingsInternal checkSettingsInternal);
}
