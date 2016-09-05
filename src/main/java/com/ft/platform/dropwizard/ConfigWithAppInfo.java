package com.ft.platform.dropwizard;

/**
 * Interface that supports configuration of AdvancedHealthcheck by the AdvancedHealthcheckBundle.
 * Dropwizard applications config class needs to implement this interface in order to register the
 * bundle with dropwizard.
 *
 * SystemCode, description and runbookUrl can then be set in the applications yaml config file.
 */
public interface ConfigWithAppInfo {
    AppInfo getAppInfo();
}
