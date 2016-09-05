package com.ft.platform.dropwizard.async;

import com.ft.platform.dropwizard.testsupport.ConfigPathBuilder;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestConfig;
import com.ft.platform.dropwizard.testsupport.HealthCheckTestService;
import com.ft.platform.dropwizard.testsupport.TestAdvancedHealthCheck;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ft.platform.dropwizard.AdvancedResult.Status.OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AdvancedAsyncHealthCheckIntegrationTest {

    @Rule
    public final DropwizardAppRule<HealthCheckTestConfig> app =
            new DropwizardAppRule<>(TestService.class, ConfigPathBuilder.DEFAULT_PATH);

    Client client;

    @Before
    public void init() {
        client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
    }

    @Test
    public void shouldReturn200WhenCheckIsOK() throws InterruptedException {
        TestAdvancedHealthCheck.status = OK;

        assertHealthCheckRanAndGetEntity(getHealthCheckPage());
    }

    @Test
    public void shouldNotUpdateResultOnRequest() throws InterruptedException {
        TestAdvancedHealthCheck.status = OK;

        final LocalDateTime initialDateTime = entityToLocalDateTime(
                assertHealthCheckRanAndGetEntity(
                        getHealthCheckPage()
                )
        );

        final LocalDateTime secondDateTime = entityToLocalDateTime(
                assertHealthCheckRanAndGetEntity(
                        getHealthCheckPage()
                )
        );

        assertThat(initialDateTime, is(secondDateTime));
    }

    private String assertHealthCheckRanAndGetEntity(final Response response){
        assertThat(response.getStatus(), is(200));
        assertThat(response.getStringHeaders().getFirst("Content-Type"), is("application/json"));
        assertThat(response.getStringHeaders().getFirst("Cache-Control"), containsString("no-cache"));
        final String entity = response.readEntity(String.class);
        assertThat(entity, containsString("TestService"));
        assertThat(entity, not(containsString("First run not complete")));
        return entity;
    }

    private final Pattern extractDate = Pattern.compile("\\d{4}[-]\\d{2}[-]\\d{2}T\\d{2}:\\d{2}:\\d{2}");

    private LocalDateTime entityToLocalDateTime(final String entity) {
        final Matcher matcher = extractDate.matcher(entity);
        if(matcher.find()){
            return LocalDateTime.parse(matcher.group(0));
        }
        fail("date not found in healthcheck");
        return null;
    }

    private Response getHealthCheckPage() {
        return client.target(url("/__health")).request().get();
    }

    private String url(final String pathAndQuery) {
        return "http://localhost:" + app.getLocalPort() + pathAndQuery;
    }

    public static class TestService extends HealthCheckTestService {
        @Override
        public void initialize(final Bootstrap<HealthCheckTestConfig> bootstrap) {
            bootstrap.addBundle(new AdvancedAsyncHealthCheckBundle());
        }
    }


}