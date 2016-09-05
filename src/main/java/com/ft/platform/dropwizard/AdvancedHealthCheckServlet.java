package com.ft.platform.dropwizard;

import com.codahale.metrics.servlets.HealthCheckServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.setup.Environment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@SuppressWarnings("serial")
public class AdvancedHealthCheckServlet extends HttpServlet {

    public static final String REGISTRY_ATTRIBUTE = HealthCheckServlet.class.getName() + ".registry";

    private final String appName;
    private final String appDescription;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public AdvancedHealthCheckServlet(String appName, String appDescription, ObjectMapper objectMapper, Environment environment) {
        this.appName = appName;
        this.appDescription = appDescription;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        AdvancedHealthChecksRunner runner = new AdvancedHealthChecksRunner(environment, objectMapper, appName, appDescription);
        HealthCheckPageData pageData = runner.run();

        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        resp.setStatus(SC_OK);

        final PrintWriter writer = resp.getWriter();
        writer.write(pageData.toString());
        writer.close();
    }
}
