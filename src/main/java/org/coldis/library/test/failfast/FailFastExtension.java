package org.coldis.library.test.failfast;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class FailFastExtension implements BeforeEachCallback, TestWatcher {
  private static final Log LOGGER = LogFactory.getLog(FailFastExtension.class);

  private static final Path FAIL_FAST_FLAG = Paths.get("target/test-failed.lock");

  public static boolean hasFailed() {
    return Files.exists(FAIL_FAST_FLAG);
  }

  public static boolean isFailFastEnabled() {
    return Objects.equals(System.getProperty("project.config.source.test.retry-and-fail-fast.fail-fast-enabled"), "true");
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    Assumptions.assumeFalse(FailFastExtension.hasFailed(), "A test has already failed, skipping remaining tests.");
  }

  @Override
  public void testFailed(final ExtensionContext context, final Throwable cause) {
    // If the test method failed after all attempts, set the fail fast flag and throw the exception.
    if (FailFastExtension.isFailFastEnabled()) {
      try {
        Files.createFile(FAIL_FAST_FLAG);
      } catch (final FileAlreadyExistsException ignore) {
      } catch (final IOException e) {
        LOGGER.error("Error creating fail fast flag file: " + e.getMessage(), e);
      }
    }
  }
}
