Building
========

Creating a git release on Github will trigger a build on jitpack: https://jitpack.io/#Financial-Times/advanced-dropwizard-healthcheck

The pom file will NOT be updated and should stay as 'snapshot'.


About
=====

The Advanced Healthcheck library conforms to the [FT Health Check Standard](https://docs.google.com/document/d/18hefJjImF5IFp9WvPAm9Iq5_GmWzI9ahlKSzShpQl1s/edit).

To use, simply create a health check that extends `AdvancedHealthCheck`.

E.g.

    public class HelloworldHealthCheck extends AdvancedHealthCheck {
    
        public HelloworldHealthCheck(final String name) {
    
            super(name);
        }
    
        @Override
        protected AdvancedResult checkAdvanced() throws Exception {
    
            if (true) {
                return AdvancedResult.healthy("All is ok");
            }
    
            return AdvancedResult.error(this, "Not ok");
    
        }

        @Override
        protected int severity() {
            return 0;
        }
    
        @Override
        protected String businessImpact() {
            return "business impact";
        }
    
        @Override
        protected String technicalSummary() {
            return "technical summary";
        }
    
        @Override
        protected String panicGuideUrl() {
            return "http://mypanicguide.com";
        }
    
    }

##### Health check id

By default a health check will return its name as its ID. This can be overridden:

        @Override
        protected String id() {
            return "healthcheck-one";
        }
    
It can then be added to Dropwizard in the standard healthcheck way.

    environment.healthChecks().register("my health check", new HelloworldHealthCheck("my health check"));

The `AdvancedHealthCheckServlet` (by way of the `HealthCheckPageData` object) provides INFO level logging for Splunk.

    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Offer API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Cassandra", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Subscription API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="User API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="summary", name="Sign Up App", status="OK"

This is configured in your app's YAML configuration file.

    loggers:
      #enable advanced health check logging
      "com.ft.platform.dropwizard.HealthCheckPageData": INFO
      #disable advanced health check logging
      "com.ft.platform.dropwizard.HealthCheckPageData": OFF

This will ensure all errors reported through the `__health` endpoint will be logged. If your monitoring hits another endpoint, such
as the built-in Dropwizard endpoint, then wrap your advanced checks in a `LoggingAdvancedHealthCheck`.

Bundle
======

The advanced health check page can be added to your app using a convenient bundle.

Configuration of the `AdvancedHealthCheck` class is done via the Dropwizard config file. For example:

    appInfo:
      systemCode: test-system-code
      description: "tests description"
      runbookUrl: "http://test-runbook/url"


To support this, your application's configuration class needs to implement the following interface:

[src/main/java/com/ft/platform/dropwizard/ConfigWithAppInfo.java](src/main/java/com/ft/platform/dropwizard/ConfigWithAppInfo.java)

Example:

    public class AppConfig extends Configuration implements ConfigWithAppInfo {
        @JsonProperty
        private AppInfo appInfo;
        @Override
        public AppInfo getAppInfo() { return appInfo; }
    }

GTG
===

"Good to go" is also supported as an additional bundle. Your dropwizard config will need to implement the ConfigWithGtg interface:

    public static class MyConfig extends Configuration implements ConfigWithGTG{    
        @JsonProperty
        private GTGConfig gtg = new GTGConfig();

Add the bundle:

    @Override
    public void initialize(final Bootstrap bootstrap) {
        bootstrap.addBundle(new GoodToGoConfiguredBundle(new DefaultGoodToGoChecker()));
    }

This example will result in a `/__gtg` endpoint that is based on the health check results.  One or more errors imply not good to go.

If needed, you can supply your own `GoodToGoChecker` implementation instead of using `DefaultGoodToGoChecker`.

Async Health Checks
===================

WARNING: this feature should be considered experimental!

This library also provides an asynchronous version of the advanced health check. These health checks are run on a schedule, 
instead of being triggered by requests to the `__health` endpoint; this ensures a timely response to the endpoint, 
returning the result of the most recently run health check:

To register the `AdvancedAsyncHealthCheckBundle` bundle, your Dropwizard app config class will need to implement the
`AdvancedHealthCheckConfig` interface in order to configure the scheduling of the health check runs.

Settings, in addition to `AppInfo` (described above), defined in the `AsyncScheduleConfig` class are:

* The initial delay period before health checks start to run (this may be useful if your server startup takes a long time).
* The period to wait after one health check finishes before a new one should be started.
* The `TimeUnit` to use for the other 2 fields.


Health check failure Graphite stats
===================================

If you are using the [Metrics](http://metrics.dropwizard.io/) library, this bundle will increment a counter 
whenever a health check fails, allowing you to collect statistics on the health checks that fail most often, 
which may help you to prioritise the order of health checks that you focus on fixing.

To start reporting metrics to Graphite, take a look at 
[Reporting to Graphite](http://metrics.dropwizard.io/3.1.0/manual/graphite/#manual-graphite).

NB. The metrics will appear in Graphite in a folder (under your chosen application prefix)
called `healthcheck-failures.<name of health check>`, with some special characters (such as `/` and `.`)
converted into underscores. For example, a user-cred-svc health check failure count for a health check called
_"Kafka health check for http://cmdb.ft.com/systems/usr-cred-svc"_ will exist under the following path in Graphite:

    /membership/aim/user-credentials-service/local/local/web/local/healthcheck-failures/Kafka-healthcheck-for-http:__cmdb_ft_com_
