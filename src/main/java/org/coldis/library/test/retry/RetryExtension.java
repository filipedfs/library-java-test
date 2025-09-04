package org.coldis.library.test.retry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coldis.library.helper.RandomHelper;
import org.coldis.library.test.failfast.FailFastExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.test.context.TestContextManager;

public class RetryExtension implements TestExecutionExceptionHandler, TestWatcher {
  private static final Log LOGGER = LogFactory.getLog(RetryExtension.class);

  private static final int MAX_ATTEMPTS = 3;

  public static final Integer FIXED_DELAY_BEFORE_NEXT_ATTEMPT = 1000;

  public static final Integer RANDOM_DELAY_BEFORE_NEXT_ATTEMPT = 10000;

  public static int getMaxAttempts() {
    return Integer.parseInt(System.getProperty("project.config.source.test.retry-and-fail-fast.max-attempts", String.valueOf(RetryExtension.MAX_ATTEMPTS)));
  }

  public static Long getDelayBeforeNextAttempt(
    final Integer attempt
  ) {
    return (Integer.parseInt(System.getProperty("project.config.source.test.retry-and-fail-fast.fixed-delay-before-next-attempt", String.valueOf(RetryExtension.FIXED_DELAY_BEFORE_NEXT_ATTEMPT)))
            + RandomHelper.getPositiveRandomLong(Long.parseLong(System.getProperty("project.config.source.test.retry-and-fail-fast.random-delay-before-next-attempt", String.valueOf(RetryExtension.RANDOM_DELAY_BEFORE_NEXT_ATTEMPT)))))
           * attempt;
  }

  @Override
  public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable) throws Throwable {

    // Retries the test method up to a maximum number of attempts.
    final TestContextManager testContextManager = new TestContextManager(context.getRequiredTestClass());

    for (int attempt = 1; attempt <= RetryExtension.getMaxAttempts() && !FailFastExtension.hasFailed(); attempt++) {
      try {
        // Runs before methods.
        testContextManager.beforeTestMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod());

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
      } catch (Throwable error) {
        // Gets the actual error, if it is an InvocationTargetException and the cause is not null
        final Throwable originalError = this.getOriginalError(error);

        // Log the exception.
        final String testName = context.getRequiredTestMethod().getDeclaringClass().getName()
                                + "."
                                + context.getRequiredTestMethod().getName();
        RetryExtension.LOGGER.error(
          testName
          + " -- Attempt "
          + attempt
          + " of "
          + RetryExtension.getMaxAttempts()
          + " <<< FAILURE!", originalError
        );
      }
      // Finish the test context manager.
      finally {
        try {
          testContextManager.afterTestMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod(), null);
        } catch (final Throwable error) {
          RetryExtension.LOGGER.error(
            "Error finishing test context manager for "
            + context.getRequiredTestMethod().getDeclaringClass().getName()
            + "."
            + context.getRequiredTestMethod().getName(), error
          );
        }
      }

      // Waits before the next attempt.
      try {
        Thread.sleep(RetryExtension.getDelayBeforeNextAttempt(attempt));
      } catch (final InterruptedException e) {
        LOGGER.error("Error sleeping before next attempt: " + e.getMessage(), e);
      }
    }

    // If the test method failed after all attempts throw the exception.
    throw throwable;
  }

  private Throwable getOriginalError(final Throwable error) {
    Throwable originalError = error;
    // Gets the actual error, if it is an InvocationTargetException and the cause is not null,
    if ((error instanceof InvocationTargetException)
        && (error.getCause() != null)
        && (error.getStackTrace().length
            >= 3)
        && error.getStackTrace()[2].getClassName().equals(RetryExtension.class.getName())) {
      // Get the original exception
      originalError = error.getCause();
    }

    return originalError;
  }
}
