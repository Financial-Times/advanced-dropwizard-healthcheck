package com.ft.platform.dropwizard.async;

import com.ft.platform.dropwizard.HealthCheckPageData;
import com.ft.platform.dropwizard.HealthCheckServletResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("serial")
public class AdvancedAsyncHealthCheckServlet extends HttpServlet {

    private final AdvancedAsyncHealthCheckRunner runner;

    public AdvancedAsyncHealthCheckServlet(final AdvancedAsyncHealthCheckRunner runner) {
        this.runner = runner;
    }

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        new HealthCheckServletResponseWriter(runner.healthCheckPageData()).write(resp);
    }
}

