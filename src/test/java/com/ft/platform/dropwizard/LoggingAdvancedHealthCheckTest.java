package com.ft.platform.dropwizard;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.codahale.metrics.health.HealthCheck;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * LoggingAdvancedHealthCheckTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggingAdvancedHealthCheckTest {

    ch.qos.logback.classic.Logger logger;

    ListAppender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {

        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        mockAppender = new ListAppender<>();
        mockAppender.setContext(logger.getLoggerContext());

        mockAppender.start();

        logger.addAppender(mockAppender);

    }

    @After
    public void tearDown() {
        logger.detachAppender(mockAppender);
        mockAppender.stop();
    }

    @Test
    public void shouldReportUnhealthyIfCheckLogicThrowsExceptionInLegacyContext() {
        LoggingAdvancedHealthCheck loggingCheck = new LoggingAdvancedHealthCheck(new SyntheticFailingAdvancedHealthCheck("id", "Test",1,"synthetic failure"));

        HealthCheck.Result result = loggingCheck.execute();

        assertThat(result.isHealthy(),is(false));
    }

    @Test
    public void shouldReportErrorIfCheckLogicThrowsExceptionInAdvancedContext() {
        LoggingAdvancedHealthCheck loggingCheck = new LoggingAdvancedHealthCheck(new SyntheticFailingAdvancedHealthCheck("id", "Test",1,"synthetic failure"));

        AdvancedResult result = loggingCheck.executeAdvanced();

        assertThat(result.status(),is(AdvancedResult.Status.ERROR));

    }

    @Test
    public void shouldLogExceptionAsErrorFromCheckLogic() {
        LoggingAdvancedHealthCheck loggingCheck = new LoggingAdvancedHealthCheck(new SyntheticFailingAdvancedHealthCheck("id", "Test",1,"synthetic failure"));

        loggingCheck.execute();

        assertThat(statementsMatching("synthetic failure", Level.WARN), hasSize(1));
    }

    private List<String> statementsMatching(String keyword, ch.qos.logback.classic.Level level) {
        List<String> statements = new ArrayList<>();
        for(ILoggingEvent event : mockAppender.list) {

            if(event.getLevel()!=level) {
                continue;
            }

            String message = event.getFormattedMessage();
            if(message.contains(keyword)) {
                statements.add(message);
            }
        }
        return statements;
    }

    @Test
    public void shouldLogHealthyOutputAsDEBUGFromCheckLogic() {

        AdvancedHealthCheck mockHealthyCheck = mock(AdvancedHealthCheck.class);
        AdvancedResult mockHealthyResult = AdvancedResult.healthy("mock output");
        try {
            when(mockHealthyCheck.checkAdvanced()).thenReturn(mockHealthyResult);
        } catch (Exception e) {
            throw new RuntimeException("Setting up mocks threw an exception. That shouldn't happen",e);
        }

        LoggingAdvancedHealthCheck loggingCheck = new LoggingAdvancedHealthCheck(mockHealthyCheck);

        loggingCheck.execute();

        assertThat(statementsMatching("mock output", Level.DEBUG), hasSize(1));

    }

}
