package org.coldis.library.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Container extension.
 */
public class TestWithContainerExtension implements BeforeAllCallback, AfterAllCallback {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestWithContainerExtension.class);

	/**
	 * Gets the containers from tests.
	 *
	 * @param  context Test context.
	 * @return
	 */
	protected Collection<Field> getContainersFieldsFromTests(
			final ExtensionContext context) {
		final Collection<Field> containersFields = new ArrayList<>();
		for (final Field field : context.getTestClass().get().getFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				if (field.getType().equals(GenericContainer.class)) {
					containersFields.add(field);
				}
			}
		}
		return containersFields;
	}

	/**
	 * Starts the test container.
	 *
	 * @param field Container field.
	 */
	public static void startTestContainer(
			final Field field) {
		try {
			final GenericContainer<?> container = (GenericContainer<?>) field.get(null);
			container.start();
			// Sets the container ports as system properties.
			container.getExposedPorts().forEach((
					exposedPort) -> {
				final Integer mappedPort = container.getMappedPort(exposedPort);
				final String mappedPortPropertyName = field.getName() + "_" + exposedPort;
				System.setProperty(mappedPortPropertyName, mappedPort.toString());
			});
			// Sets the container host as system property.
			System.setProperty(field.getName() + "_IP", container.getContainerInfo().getNetworkSettings().getIpAddress());
			// TestHelper.waitUntilValid(() -> container, (currentContainer ->
			// currentContainer.isRunning()), TestHelper.LONG_WAIT,
			// TestHelper.VERY_SHORT_WAIT);
		}
		catch (final Exception exception) {
			TestWithContainerExtension.LOGGER.error("Error starting container.", exception);
		}
	}

	/**
	 * If test containers should be started in parallel.
	 *
	 * @param  testClass Test class.
	 * @return           If test containers should be started in parallel.
	 */
	protected Boolean shouldStartTestContainersInParallel(
			final Class<?> testClass) {
		return (testClass.getAnnotation(TestWithContainer.class) != null) && testClass.getAnnotation(TestWithContainer.class).parallel();
	}

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
		final Collection<Field> containersFieldsFromTests = this.getContainersFieldsFromTests(context);
		final Executor executor = this.shouldStartTestContainersInParallel(testClass) ? Executors.newWorkStealingPool() : Executors.newSingleThreadExecutor();
		final CompletableFuture<Void>[] containersFieldsJobs = containersFieldsFromTests.stream()
				.map(field -> (CompletableFuture.runAsync((() -> TestWithContainerExtension.startTestContainer(field)), executor)))
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(containersFieldsJobs).get();
	}

	/**
	 * Stops the test container.
	 *
	 * @param field Container field.
	 */
	public static void stopTestContainer(
			final Field field) {
		try {
			final GenericContainer<?> container = (GenericContainer<?>) field.get(null);
			container.stop();
			container.close();
		}
		catch (final Exception exception) {
			TestWithContainerExtension.LOGGER.error("Error stopping container.", exception);
		}
	}

	/**
	 * After each test.
	 *
	 * @param  context   Test context.
	 * @throws Exception If the test fails.
	 */
	@Override
	public void afterAll(
			final ExtensionContext context) throws Exception {
		final Class<?> testClass = context.getTestClass().orElseThrow();
		final Collection<Field> containersFieldsFromTests = this.getContainersFieldsFromTests(context);
		final Executor executor = this.shouldStartTestContainersInParallel(testClass) ? Executors.newWorkStealingPool() : Executors.newSingleThreadExecutor();
		final CompletableFuture<Void>[] containersFieldsJobs = containersFieldsFromTests.stream()
				.map(field -> (CompletableFuture.runAsync(() -> TestWithContainerExtension.stopTestContainer(field), executor)))
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(containersFieldsJobs).get();
	}

}
