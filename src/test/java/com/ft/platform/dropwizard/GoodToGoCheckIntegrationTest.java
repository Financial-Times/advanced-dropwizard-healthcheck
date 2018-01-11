package com.ft.platform.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GoodToGoCheckIntegrationTest {

	@Rule
	public DropwizardAppRule<HealthCheckTestConfig> app = new DropwizardAppRule<>(
			GoodToGoTestService.class, testYamlPath());

	Client client;

	@Before
	public void init() {
		client = app.getConfiguration().buildJerseyClient(app.getEnvironment());
	}

	@Test
	public void shouldReturn200WhenCheckIsOK() {
		FixedResponseChecker.response = true;
		Response result = getHealthCheckPage();

		assertThat(result.getStatus(), is(200));
		assertThat(result.readEntity(String.class), is("OK"));
		assertThat(result.getMediaType(), is(MediaType.TEXT_PLAIN_TYPE.withCharset("US-ASCII")));
		assertThat(result.getStringHeaders().getFirst("Cache-Control"),
				containsString("no-cache"));
	}

	@Test
	public void shouldReturn503WhenCheckIsNotOK() {
		FixedResponseChecker.response = false;
		Response result = getHealthCheckPage();

		assertThat(result.getStatus(), is(503));
	}

	private Response getHealthCheckPage() {
		return client.target(url("/__gtg")).request().get();
	}

	private String url(String pathAndQuery) {
		return "http://localhost:" + app.getLocalPort() + pathAndQuery;
	}

	static String testYamlPath() {
		try {
			return new File(Resources.getResource("test.yaml").toURI())
					.getCanonicalPath();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class HealthCheckTestConfig extends Configuration implements ConfigWithAppInfo, ConfigWithGTG{

		@JsonProperty
		private String dummy = "";

		@JsonProperty
		private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

		@JsonProperty
		private AppInfo appInfo;

		@JsonProperty
		private GTGConfig gtg = new GTGConfig();

		public Client buildJerseyClient(Environment environment) {
			return new JerseyClientBuilder(environment).using(jerseyClient)
					.using(environment).build("TestJersey");
		}

		@Override
		public AppInfo getAppInfo() {
			return appInfo;
		}

		@Override
		public GTGConfig getGtg() {
			return gtg;
		}
	}

	public static class GoodToGoTestService extends
			Application<HealthCheckTestConfig> {
		
		@Override
		public void initialize(Bootstrap<HealthCheckTestConfig> bootstrap) {
			bootstrap.addBundle(new GoodToGoConfiguredBundle(new FixedResponseChecker()));
		}

		@Override
		public void run(HealthCheckTestConfig configuration,
				Environment environment) throws Exception {
            environment.jersey().register(new GoodToGoTestRootResource());
		}
	}

	
	public static class FixedResponseChecker implements GoodToGoChecker {

		private static boolean response;

		@Override
		public GoodToGoResult runCheck(Environment env) {
			return new GoodToGoResult(response, "");
		}

	}
	
    @Path("/")
    public static class GoodToGoTestRootResource {

        @GET
        public String root() {
            return "Test for __gtg endpoint";
        }

    }
}
