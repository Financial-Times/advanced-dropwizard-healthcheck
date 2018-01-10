package com.ft.platform.dropwizard;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.dropwizard.setup.Environment;

/**
 * This implementation of {@link GoodToGoChecker} is based solely on the health
 * check responses. One or more error results implies not good to go. Otherwise
 * we're considered good.
 */
public class DefaultGoodToGoChecker implements GoodToGoChecker {

	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 3;
	private final ExecutorService executor;
	private int timeOutInSeconds;

	public DefaultGoodToGoChecker(ExecutorService executor, int timeOutInSeconds) {
		this.timeOutInSeconds = timeOutInSeconds;
		this.executor = executor;
	}

	public DefaultGoodToGoChecker() {
		this.timeOutInSeconds = DEFAULT_TIMEOUT_IN_SECONDS;
		this.executor = Executors.newFixedThreadPool(3);
	}

	public GoodToGoResult runCheck(Environment environment) {
		try {
			return executor.submit(() -> {
				for (AdvancedResult r : HealthChecks.runAdvancedHealthChecksIn(environment).values()) {
					if (r.status() == AdvancedResult.Status.ERROR) {
						return new GoodToGoResult(false, r.checkOutput());
					}
				}
				return new GoodToGoResult(true, "OK");
			}).get(this.timeOutInSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return new GoodToGoResult(false, "Timed out after " + this.timeOutInSeconds + " second(s)");
		}
	}
}
