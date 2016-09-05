package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.html.HTMLWrapperFilter;
import io.dropwizard.setup.Environment;

import javax.servlet.Servlet;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.REQUEST;

public class AdvancedHealthcheckRegistration {
    public static void registerServlets(final Environment environment, final Servlet healthcheckServlet) {
        environment.servlets().addFilter("HealthCheckHTMLWrapperFilter", new HTMLWrapperFilter(environment.getName()))
                .addMappingForUrlPatterns(EnumSet.of(REQUEST), false, "/__health.html");
        environment.servlets().addServlet("AdvancedHealthCheckServlet", healthcheckServlet)
                .addMapping("/__health.html", "/__health", "/__health.json");
    }
}
