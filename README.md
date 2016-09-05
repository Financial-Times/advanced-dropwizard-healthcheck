About
-----

The Advanced Healthcheck library conforms to the FT Alerts Standards.

To use simply create a Healthcheck that extends AdvancedHealthCheck.

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
    
It can then be added to dropwizard in the standard healthcheck way.

    environment.healthChecks().register("my health check", new HelloworldHealthCheck("my health check"));

The AdvancedHealthCheckServlet (by way of the HealthCheckPageData object) provides info level logging for splunk.

    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Offer API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Cassandra", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="Subscription API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="detail", name="Sign Up App", checkName="User API", ok="true", checkOutput="OK"
    event="advancedHealthCheck", action="summary", name="Sign Up App", status="OK"

This is configured in your app's yaml.

    loggers:
      #enable advanced health check logging
      "com.ft.platform.dropwizard.HealthCheckPageData": INFO
      #disable advanced health check logging
      "com.ft.platform.dropwizard.HealthCheckPageData": OFF

"Good to go" is also supported as an additional bundle:

    @Override
    public void initialize(final Bootstrap bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
        bootstrap.addBundle(new GoodToGoBundle(new DefaultGoodToGoChecker()));
    }

This example will result in a /\_\_gtg endpoint that is based on the health check results.  One or more errors imply not good to go.

If needed, you can supply your own GoodToGoChecker implementation instead of using DefaultGoodToGoChecker.

