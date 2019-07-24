package com.applitools.eyes.config;

import com.applitools.eyes.config.IConfigurationGetter;
import com.applitools.eyes.config.IConfigurationSetter;

public interface IConfigurationProvider {

    IConfigurationGetter get();

    IConfigurationSetter set();
}
