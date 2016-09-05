package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.html.HTMLWrapperFilter;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;

import static javax.servlet.DispatcherType.REQUEST;

public class AdvancedHealthCheckBundle implements Bundle {

    private String applicationName;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        applicationName = bootstrap.getApplication().getName();
    }

    @Override
    public void run(Environment environment) {
        environment.servlets().addFilter("HealthCheckHTMLWrapperFilter", new HTMLWrapperFilter(applicationName))
            .addMappingForUrlPatterns(EnumSet.of(REQUEST), false, "/__health.html");

        AdvancedHealthCheckServlet servlet = new AdvancedHealthCheckServlet(environment.getName(),
                                                                            environment.getName(),
                                                                            environment.getObjectMapper(),
                                                                            environment);
        environment.servlets().addServlet("AdvancedHealthCheckServlet", servlet)
                .addMapping("/__health.html", "/__health", "/__health.json");
    }
}
