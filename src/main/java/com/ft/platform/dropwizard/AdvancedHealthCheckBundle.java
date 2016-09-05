package com.ft.platform.dropwizard;

import io.dropwizard.Bundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import static com.ft.platform.dropwizard.AdvancedHealthcheckRegistration.registerServlets;

public class AdvancedHealthCheckBundle implements ConfiguredBundle<ConfigWithAppInfo> {

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final ConfigWithAppInfo config, final Environment environment) {
        registerServlets(
            environment,
            new AdvancedHealthCheckServlet(environment.getName(),
                config.getAppInfo().getDescription(),
                environment.getObjectMapper(),
                environment,
                config.getAppInfo().getSystemCode()
            )
        );
    }
}
