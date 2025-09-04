package org.coldis.library.test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Container extension.
 */
public class StartTestWithContainerExtension implements BeforeAllCallback {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestWithContainerExtension.class);

	/** Container lock */
	public static Map<GenericContainer<?>, Integer> CONTAINER_LOCK = new IdentityHashMap<>();

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
		final CompletableFuture<Void>[] containersFieldsJobs = containersFieldsFromTests.stream().map(field -> (CompletableFuture.runAsync((() -> {
			// Starts the container if not already started.
			try {
				final GenericContainer<?> container = (GenericContainer<?>) field.get(null);
				StartTestWithContainerExtension.CONTAINER_LOCK.computeIfAbsent(container, k -> 0);
				StartTestWithContainerExtension.CONTAINER_LOCK.put(container, StartTestWithContainerExtension.CONTAINER_LOCK.get(container) + 1);
				if (!TestWithContainerExtensionHelper.shouldReuseTestContainers(testClass) || !container.isRunning()
						|| (StartTestWithContainerExtension.CONTAINER_LOCK.getOrDefault(container, 0) <= 0)) {
					TestWithContainerExtensionHelper.startTestContainer(testClass, field);
				}
			}
			catch (final Exception exception) {
				throw new RuntimeException(exception);
			}
		}), executor))).toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(containersFieldsJobs).get();
	}

}
