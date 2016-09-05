package com.ft.platform.dropwizard;

import com.ft.platform.dropwizard.testsupport.ConfigPathBuilder;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestConfig;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestService;
import com.ft.platform.dropwizard.testsupport.TestAdvancedHealthCheck;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.ft.platform.dropwizard.AdvancedResult.Status.ERROR;
import static com.ft.platform.dropwizard.AdvancedResult.Status.OK;
import static com.ft.platform.dropwizard.AdvancedResult.Status.WARN;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdvancedHealthCheckIntegrationTest {

    @Rule
    public final DropwizardAppRule<HealthCheckTestConfig> app =
            new DropwizardAppRule<>(HealthCheckTestService.class, ConfigPathBuilder.DEFAULT_PATH);

    Client client;

    @Before
    public void init() {
        client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
    }

    @Test
    public void shouldReturn200WhenCheckIsOK() {
        TestAdvancedHealthCheck.status = OK;
        final Response result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getStringHeaders().getFirst("Content-Type"), is("application/json"));
        assertThat(result.getStringHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        assertThat(result.readEntity(String.class), containsString("ok"));
    }

    @Test
    public void shouldReturn200WhenHtmlCheckIsOK() {
        TestAdvancedHealthCheck.status = OK;
        final Response result = getHealthCheckHtmlPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getStringHeaders().getFirst("Content-Type"), is("text/html;charset=utf-8"));
        assertThat(result.getStringHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        final String entity = result.readEntity(String.class);
        assertThat(entity, containsString("ok"));
        assertThat(entity, containsString("\"technicalSummary\":\"The audit system has failed\""));
        assertThat(entity, containsString("\"businessImpact\":\"The CEO will go to prison\""));
    }

    @Test
    public void shouldReturn200WhenCheckIsWARN() {
        TestAdvancedHealthCheck.status = WARN;
        final Response result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        assertThat(result.getStringHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        assertThat(result.readEntity(String.class), containsString("Hmmm...not looking so good"));
    }

    @Test
    public void shouldReturn200WhenCheckIsERROR() {
        TestAdvancedHealthCheck.status = ERROR;
        final Response result = getHealthCheckPage();

        assertThat(result.getStatus(), is(200));
        final String entity = result.readEntity(String.class);
        assertThat(entity, containsString("The audit host's RAID array is on fire"));
        assertThat(entity, containsString("\"technicalSummary\":\"The audit system has failed\""));
        assertThat(entity, containsString("\"businessImpact\":\"The CEO will go to prison\""));
    }

    private Response getHealthCheckPage() {
        return client.target(url("/__health")).request().get();
    }

    private Response getHealthCheckHtmlPage() {
        return client.target(url("/__health.html")).request().accept("text/html").get();
    }

    private String url(final String pathAndQuery) {
        return "http://localhost:" + app.getLocalPort() + pathAndQuery;
    }

}
