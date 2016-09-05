package com.ft.platform.dropwizard.testsupport;

import com.google.common.io.Resources;

import java.io.File;

public class ConfigPathBuilder {

    public static final String DEFAULT_PATH = new ConfigPathBuilder("test.yaml").build();

    final String filename;

    public ConfigPathBuilder(final String filename) {
        this.filename = filename;
    }

    public String build() {
        try {
            return new File(Resources.getResource(this.filename).toURI()).getCanonicalPath();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
