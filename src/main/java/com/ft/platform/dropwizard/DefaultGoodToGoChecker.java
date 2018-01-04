package com.ft.platform.dropwizard;

import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This implementation of {@link GoodToGoChecker} is based solely on the health
 * check responses. One or more error results implies not good to go. Otherwise
 * we're considered good.
 */
public class DefaultGoodToGoChecker implements GoodToGoChecker {

	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 3;
	private int timeOutInSeconds;

	public DefaultGoodToGoChecker(int timeOutInSeconds) {
		this.timeOutInSeconds = timeOutInSeconds;
	}

	public DefaultGoodToGoChecker() {
		this.timeOutInSeconds = DEFAULT_TIMEOUT_IN_SECONDS;
	}

	public GoodToGoResult runCheck(Environment environment) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			return new GoodToGoResult(executorService.submit(() -> {
				for (AdvancedResult result : HealthChecks.runAdvancedHealthChecksIn(environment).values()) {
					if (result.status() == AdvancedResult.Status.ERROR) {
						return false;
					}
				}
				return true;
			}).get(this.timeOutInSeconds, TimeUnit.SECONDS), "");
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return new GoodToGoResult(false, "Timed out after " + this.timeOutInSeconds + " second(s)");
		} finally {
			executorService.shutdownNow();
		}
	}

}
