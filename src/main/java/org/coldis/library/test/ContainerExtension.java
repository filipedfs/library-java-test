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
		for (final Field field : context.getTestClass().get().getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				if (field.getType().equals(GenericContainer.class)) {
					try {
						final GenericContainer<?> container = (GenericContainer<?>) field.get(null);
						container.start();
						container.getExposedPorts().forEach((
								boundPort) -> {
							final Integer mappedPort = container.getMappedPort(boundPort);
							System.setProperty(field.getName() + "_" + boundPort, mappedPort.toString());
						});
						Thread.sleep(2 * 1000);
					}
					catch (final Exception exception) {
						ContainerExtension.LOGGER.error("Error starting container.", exception);
					}
				}
			}
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
		for (final Field field : context.getTestClass().get().getDeclaredFields()) {
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
