package com.ft.platform.dropwizard.testsupport;


import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.platform.dropwizard.html.HTMLWrapperFilter;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class HealthCheckTestService extends Application<HealthCheckTestConfig> {
    @Override
    public void initialize(final Bootstrap<HealthCheckTestConfig> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final HealthCheckTestConfig configuration, final Environment environment) throws Exception {
        environment.jersey().register(new HealthCheckTestRootResource());
        environment.servlets().addFilter("",new HTMLWrapperFilter("test")).addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST), false, "/__healthcheck.html");

        environment.healthChecks().register("TestAdvancedHealthCheck", new TestAdvancedHealthCheck());
    }
}
