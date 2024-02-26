package org.coldis.library.test;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Container extension.
 */
public class ContainerExtension implements BeforeAllCallback, AfterAllCallback {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerExtension.class);

	/**
	 * Before each test.
	 *
	 * @param  context   Test context.
	 * @throws Exception If the test fails.
	 */
	@Override
	public void beforeAll(
			final ExtensionContext context) throws Exception {
		for (final Field field : context.getTestClass().get().getFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				if (field.getType().equals(GenericContainer.class)) {
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
						System.setProperty(field.getName() + "_HOST", container.getContainerInfo().getConfig().getHostName());
					}
					catch (final Exception exception) {
						ContainerExtension.LOGGER.error("Error starting container.", exception);
					}
				}
			}
		}
		Thread.sleep(5 * 1000);
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
		for (final Field field : context.getTestClass().get().getFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				if (field.getType().equals(GenericContainer.class)) {
					try {
						final GenericContainer<?> container = (GenericContainer<?>) field.get(null);
						container.close();
					}
					catch (final Exception exception) {
						ContainerExtension.LOGGER.error("Error stopping container.", exception);
					}
				}
			}
		}
	}

}
