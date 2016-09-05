package com.ft.platform.dropwizard.testsupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AdvancedHealthCheckConfig;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.async.AsyncScheduleConfig;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;

import javax.ws.rs.client.Client;
import java.util.concurrent.TimeUnit;

public class HealthCheckTestConfig extends Configuration implements AdvancedHealthCheckConfig {

    @JsonProperty
    private AppInfo appInfo;

    @JsonProperty
    private String dummy = "";

    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();
    private AsyncScheduleConfig asyncSchedule = new AsyncScheduleConfig(0, 10, 10, TimeUnit.SECONDS);

    public Client buildJerseyClient(Environment environment) {
        return new JerseyClientBuilder(environment).using(jerseyClient).using(environment).build("TestJersey");
    }

    @Override
    public AsyncScheduleConfig getAsyncSchedule() {
        return asyncSchedule;
    }

    public void setAsyncSchedule(AsyncScheduleConfig asyncSchedule) {
        this.asyncSchedule = asyncSchedule;
    }

    public JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    @Override
    public AppInfo getAppInfo() {
        return appInfo;
    }
}
