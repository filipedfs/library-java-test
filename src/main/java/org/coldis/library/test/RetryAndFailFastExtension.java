package org.coldis.library.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coldis.library.helper.RandomHelper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.springframework.test.context.TestContextManager;

/** Fail fast extension for JUnit 5 tests. */
public class RetryAndFailFastExtension implements BeforeEachCallback, TestExecutionExceptionHandler {

	/** Logger for this class. */
	private static final Log LOGGER = LogFactory.getLog(RetryAndFailFastExtension.class);

	/** Default maximum number of attempts for a test before failing. */
	private static final int MAX_ATTEMPTS = 3;

	/** Minimum delay before next attempt in milliseconds. */
	public static final Integer FIXED_DELAY_BEFORE_NEXT_ATTEMPT = 1000;

	/** Random delay before next attempt in milliseconds. */
	public static final Integer RANDOM_DELAY_BEFORE_NEXT_ATTEMPT = 10000;

	/** If one of the tests has already failed. */
	private static boolean TEST_FAILED = false;

	/**
	 * Gets the maximum number of attempts for a test before failing.
	 */
	public static int getMaxAttempts() {
		return Integer.parseInt(
				System.getProperty("project.config.source.test.retry-and-fail-fast.max-attempts", String.valueOf(RetryAndFailFastExtension.MAX_ATTEMPTS)));
	}

	/** Gets the delay before next attempt in milliseconds. */
	public static Long getDelayBeforeNextAttempt(
			final Integer attempt) {
		return (Integer
				.parseInt(System.getProperty("project.config.source.test.retry-and-fail-fast.fixed-delay-before-next-attempt",
						String.valueOf(RetryAndFailFastExtension.FIXED_DELAY_BEFORE_NEXT_ATTEMPT)))
				+ RandomHelper.getPositiveRandomLong(
						Long.parseLong(System.getProperty("project.config.source.test.retry-and-fail-fast.random-delay-before-next-attempt",
								String.valueOf(RetryAndFailFastExtension.RANDOM_DELAY_BEFORE_NEXT_ATTEMPT)))))
				* attempt;
	}

	/** Gets if fail fast is enabled. */
	public static boolean isFailFastEnabled() {
		return Objects.equals(System.getProperty("project.config.source.test.retry-and-fail-fast.fail-fast-enabled"), "false");
	}

	/**
	 * Sets if fail fast is enabled.
	 *
	 * @param failFastEnabled If fail fast is enabled.
	 */
	@Override
	public void beforeEach(
			final ExtensionContext context) throws Exception {
		if (RetryAndFailFastExtension.isFailFastEnabled() && RetryAndFailFastExtension.TEST_FAILED) {
			Assumptions.assumeTrue(false, "FailFastExtension: A test has already failed, skipping remaining tests.");
		}
	}

	/**
	 * Handles test execution exceptions, retrying the test method up to a maximum
	 * number of attempts.
	 *
	 * @param  context   The extension context.
	 * @param  throwable The throwable that was thrown during test execution.
	 * @throws Throwable If the test method fails after all attempts.
	 */
	@Override
	public void handleTestExecutionException(
			final ExtensionContext context,
			final Throwable throwable) throws Throwable {

		// Retries the test method up to a maximum number of attempts.
		final TestContextManager testContextManager = new TestContextManager(context.getRequiredTestClass());
		for (Integer attempt = 1; attempt <= RetryAndFailFastExtension.getMaxAttempts(); attempt++) {
			try {

				// Runs before methods.
				if (testContextManager != null) {
					testContextManager.beforeTestMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod());
				}

				// Runs beforeEach methods.
				final Method[] beforeEachMethods = context.getRequiredTestClass().getDeclaredMethods();
				for (final Method method : beforeEachMethods) {
					if (method.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class)) {
						method.setAccessible(true);
						method.invoke(context.getRequiredTestInstance());
					}
				}

				// Runs the test method.
				context.getRequiredTestMethod().invoke(context.getRequiredTestInstance());

				// Runs afterEach methods.
				final Method[] afterEachMethods = context.getRequiredTestClass().getDeclaredMethods();
				for (final Method method : afterEachMethods) {
					if (method.isAnnotationPresent(org.junit.jupiter.api.AfterEach.class)) {
						method.setAccessible(true);
						method.invoke(context.getRequiredTestInstance());
					}
				}

				// If the test method was executed successfully, exit the loop/method.
				return;
			}
			catch (Throwable error) {
				// Gets the actual error, if it is an InvocationTargetException and the cause is
				// not null,
				if ((error instanceof InvocationTargetException) && (error.getCause() != null) && (error.getStackTrace().length >= 3)
						&& error.getStackTrace()[2].getClassName().equals(RetryAndFailFastExtension.class.getName())) {
					// Get the original exception
					error = error.getCause();
				}
				// Log the exception.
				final String testName = context.getRequiredTestMethod().getDeclaringClass().getName() + "." + context.getRequiredTestMethod().getName();
				RetryAndFailFastExtension.LOGGER
						.error(testName + " -- Attempt " + attempt + " of " + RetryAndFailFastExtension.getMaxAttempts() + " <<< FAILURE!", error);
			}
			// Finish the test context manager.
			finally {
				try {
					if (testContextManager != null) {
						testContextManager.afterTestMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod(), null);
					}
				}
				catch (final Throwable error) {
					RetryAndFailFastExtension.LOGGER.error("Error finishing test context manager for "
							+ context.getRequiredTestMethod().getDeclaringClass().getName() + "." + context.getRequiredTestMethod().getName(), error);
				}
			}

			// Waits before the next attempt.
			Thread.sleep(RetryAndFailFastExtension.getDelayBeforeNextAttempt(attempt));
		}

		// If the test method failed after all attempts, set the fail fast flag and
		// throw the exception.
		RetryAndFailFastExtension.TEST_FAILED = true;
		throw throwable;
	}
}
