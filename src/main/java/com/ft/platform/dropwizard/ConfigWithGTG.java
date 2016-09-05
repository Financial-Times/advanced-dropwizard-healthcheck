package com.ft.platform.dropwizard;

/**
 * Interface that supports configuration of GTG by the GoodToGoConfiguredBundle.
 * Dropwizard applications config class needs to implement this interface in order to register the
 * bundle with dropwizard.
 *
 * OK response body and content type can then be set by the applications yaml config or supplied as defaults
 * in the applications config class.
 *
 * In most cases use the defaults supplied the the GTGConfig class of responseBody:OK and content-type:text/plain
 */
public interface ConfigWithGTG {
    GTGConfig getGtg();
}
