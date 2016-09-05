package com.ft.platform.dropwizard;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;

import static javax.servlet.http.HttpServletResponse.SC_OK;

public class HealthCheckServletResponseWriter {

    private final HealthCheckPageData pageData;

    public HealthCheckServletResponseWriter(final HealthCheckPageData pageData) {
        this.pageData = pageData;
    }

    public void write(final HttpServletResponse resp) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        resp.setStatus(SC_OK);

        final PrintWriter writer = resp.getWriter();
        writer.write(pageData.toString());
        writer.close();
    }
}
