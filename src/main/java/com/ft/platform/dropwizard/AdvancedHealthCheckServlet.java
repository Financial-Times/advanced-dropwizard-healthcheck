package com.ft.platform.dropwizard;

import com.codahale.metrics.servlets.HealthCheckServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.platform.dropwizard.metrics.HealthcheckFailureReporter;
import io.dropwizard.setup.Environment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("serial")
public class AdvancedHealthCheckServlet extends HttpServlet {

    public static final String REGISTRY_ATTRIBUTE = HealthCheckServlet.class.getName() + ".registry";

    private final String appName;
    private final String appDescription;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final String systemCode;

    public AdvancedHealthCheckServlet(final String appName,
                                      final String appDescription,
                                      final ObjectMapper objectMapper,
                                      final Environment environment,
                                      final String systemCode
                                      ) {
        this.appName = appName;
        this.appDescription = appDescription;
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.systemCode = systemCode;
    }

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {

        new HealthCheckServletResponseWriter(
                new AdvancedSynchronousHealthChecksRunner(
                        environment,
                        objectMapper,
                        appName,
                        appDescription,
                        systemCode,
                        new HealthcheckFailureReporter(environment.metrics())
                ).run()
        ).write(resp);
    }
}
