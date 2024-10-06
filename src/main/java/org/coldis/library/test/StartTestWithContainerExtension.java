package org.coldis.library.test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container extension.
 */
public class StartTestWithContainerExtension implements BeforeAllCallback {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestWithContainerExtension.class);

	/**
	 * Before each test.
	 *
	 * @param  context   Test context.
	 * @throws Exception If the test fails.
	 */
	@Override
	public void beforeAll(
			final ExtensionContext context) throws Exception {
		final Class<?> testClass = context.getTestClass().orElseThrow();
		final Collection<Field> containersFieldsFromTests = TestWithContainerExtensionHelper.getContainersFieldsFromTests(context);
		final Executor executor = TestWithContainerExtensionHelper.shouldStartTestContainersInParallel(testClass) ? Executors.newWorkStealingPool()
				: Executors.newSingleThreadExecutor();
		@SuppressWarnings("unchecked")
		final CompletableFuture<Void>[] containersFieldsJobs = containersFieldsFromTests.stream()
				.map(field -> (CompletableFuture.runAsync((() -> TestWithContainerExtensionHelper.startTestContainer(field)), executor)))
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(containersFieldsJobs).get();
	}

}
