package org.coldis.library.test.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.coldis.library.test.TestHelper;
import org.coldis.library.test.TestWithContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

/**
 * Test helper test.
 */
@TestWithContainer(parallel = true)
public class ContainerTestHelperTest {

	/**
	 * Test data.
	 */
	private static final List<TestClass> COMPLETE_TEST_DATA = List.of(new TestClass("1", 1l, new TestClass("1", 1L, null)),
			new TestClass("2", 2l, new TestClass("2", 2L, null)));

	/**
	 * Postgres container.
	 */
	public static GenericContainer<?> POSTGRES_CONTAINER = TestHelper.createPostgresContainer();

	/**
	 * Artemis container.
	 */
	public static GenericContainer<?> ARTEMIS_CONTAINER = TestHelper.createArtemisContainer();

	/**
	 * Redis container.
	 */
	public static GenericContainer<?> REDIS_CONTAINER = TestHelper.createRedisContainer();

	/**
	 * Test Postgres container.
	 */
	@Test
	public void testPostgresContainer() throws Exception {
		try (Connection connection = DriverManager.getConnection(
				"jdbc:postgresql://" + System.getProperty("POSTGRES_CONTAINER_IP") + ":5432/" + TestHelper.TEST_USER_NAME, TestHelper.TEST_USER_NAME,
				TestHelper.TEST_USER_PASSWORD)) {
			try (Statement statement = connection.createStatement()) {
				Class.forName("org.postgresql.Driver");
				final String sql = "SELECT 10;";
				final ResultSet queryResult = statement.executeQuery(sql);
				queryResult.next();
				Assertions.assertEquals(10, queryResult.getInt(1));
				Assertions.assertNotNull(System.getProperty("POSTGRES_CONTAINER_IP"));
				Assertions.assertNotNull(System.getProperty("POSTGRES_CONTAINER_5432"));
				System.out.println(System.getProperty("POSTGRES_CONTAINER_IP"));
				System.out.println(System.getProperty("POSTGRES_CONTAINER_5432"));
			}
		}
	}

}
