package com.ft.platform.dropwizard.testsupport;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class HealthCheckTestRootResource {

    @GET
    public String root() {
        return "Test for advanced health checks";
    }

}
