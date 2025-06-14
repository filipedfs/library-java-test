package org.coldis.library.test.test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.coldis.library.exception.BusinessException;
import org.coldis.library.exception.IntegrationException;
import org.coldis.library.helper.DateTimeHelper;
import org.coldis.library.test.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test helper test.
 */
public class TestHelperTest extends TestHelper {

	
	/**
	 * Test data.
	 */
	private static final List<TestClass> COMPLETE_TEST_DATA = List.of(new TestClass("1", 1l, new TestClass("1", 1L, null)),
			new TestClass("2", 2l, new TestClass("2", 2L, null)));

	
	/**
	 * Test variable.
	 */
	private Integer testVar;
	
	@BeforeAll
	public static void setup() {
	}

	/**
	 * Tests a not valid variable state.
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testWaitUntilValidFailed() throws Exception {
		this.testVar = 0;
		Assertions.assertFalse(TestHelper.waitUntilValid(() -> this.testVar, bool -> {
			this.testVar++;
			return this.testVar > 7;
		}, TestHelper.REGULAR_WAIT, TestHelper.SHORT_WAIT));
	}

	/**
	 * Tests a not valid variable state (with ignorable exceptions).
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testWaitUntilValidFailedWithIgnorableExceptions() throws Exception {
		Assertions.assertFalse(TestHelper.waitUntilValid(() -> false, bool -> {
			throw new IntegrationException();
		}, TestHelper.REGULAR_WAIT, TestHelper.SHORT_WAIT, IntegrationException.class));
	}

	/**
	 * Tests a non ignorable exception.
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testWaitUntilValidFailedWithNonIgnorableExceptions() throws Exception {
		try {
			TestHelper.waitUntilValid(() -> true, bool -> {
				throw new IntegrationException();
			}, TestHelper.REGULAR_WAIT, TestHelper.SHORT_WAIT, BusinessException.class);
			Assertions.fail();
		}
		// If an integration exception is thrown.
		catch (final IntegrationException exception) {
			// It is expected. Tests succeeds.
		}
	}

	/**
	 * Tests a valid variable state.
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testWaitUntilValidSucceeded() throws Exception {
		this.testVar = 0;
		Assertions.assertTrue(TestHelper.waitUntilValid(() -> this.testVar, bool -> {
			this.testVar++;
			return this.testVar > 4;
		}, TestHelper.REGULAR_WAIT, TestHelper.SHORT_WAIT));
	}

	/**
	 * Tests a valid variable state (with ignorable exceptions).
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testWaitUntilValidSucceededWithIgnorableExceptions() throws Exception {
		this.testVar = 0;
		Assertions.assertTrue(TestHelper.waitUntilValid(() -> this.testVar, bool -> {
			this.testVar++;
			if (this.testVar > 4) {
				return true;
			}
			else {
				throw new IntegrationException();
			}
		}, TestHelper.REGULAR_WAIT, TestHelper.SHORT_WAIT, IntegrationException.class));
	}

	/**
	 * Tests creating incomplete objects.
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void testCreateIncompleteObjects() throws Exception {
		// For each test data.
		for (final TestClass testData : TestHelperTest.COMPLETE_TEST_DATA) {
			// Makes sure the test data is complete.
			Assertions.assertNotNull(testData.getTest1());
			Assertions.assertNotNull(testData.getTest2());
			Assertions.assertNotNull(testData.getTest3().getTest1());
			Assertions.assertNotNull(testData.getTest3().getTest2());
			// For each incomplete object created from the test data.
			for (final TestClass incompleteTestData : new HashSet<>(TestHelper.createIncompleteObjects(testData, data -> {
				try {
					final TestClass clone = (TestClass) data.clone();
					clone.setTest3((TestClass) data.getTest3().clone());
					return clone;
				}
				catch (final CloneNotSupportedException exception) {
					throw new IllegalStateException(exception);
				}
			}, List.of("test1", "test2", "test3.test1", "test3.test2")))) {
				// Counts the number of missing attributes.
				Integer missingAttributes = 0;
				missingAttributes = (incompleteTestData.getTest1() == null ? missingAttributes + 1 : missingAttributes);
				missingAttributes = (incompleteTestData.getTest2() == null ? missingAttributes + 1 : missingAttributes);
				missingAttributes = (incompleteTestData.getTest3().getTest1() == null ? missingAttributes + 1 : missingAttributes);
				missingAttributes = (incompleteTestData.getTest3().getTest2() == null ? missingAttributes + 1 : missingAttributes);
				// Makes sure only one attribute is missing at a time.
				Assertions.assertEquals(1, missingAttributes.intValue());
			}
			// Makes sure the test data is still complete.
			Assertions.assertNotNull(testData.getTest1());
			Assertions.assertNotNull(testData.getTest2());
			Assertions.assertNotNull(testData.getTest3().getTest1());
			Assertions.assertNotNull(testData.getTest3().getTest2());

		}
	}

	/** Tests moving the clock forward. */
	@Test
	public void testMoveClockForward() {
		// Makes sure the clock has moved forward.
		LocalDateTime startTime = DateTimeHelper.getCurrentLocalDateTime();
		TestHelper.moveClockTo(startTime.plusDays(1));
		Assertions.assertTrue(startTime.plusDays(1).isBefore(DateTimeHelper.getCurrentLocalDateTime()));
		Assertions.assertTrue(startTime.plusDays(1).plusMinutes(1).isAfter(DateTimeHelper.getCurrentLocalDateTime()));
		TestHelper.cleanClock();

		// Makes sure the clock has moved forward.
		startTime = DateTimeHelper.getCurrentLocalDateTime();
		TestHelper.moveClockBy(Duration.ofDays(1));
		Assertions.assertTrue(startTime.plusDays(1).isBefore(DateTimeHelper.getCurrentLocalDateTime()));
		Assertions.assertTrue(startTime.plusDays(1).plusMinutes(1).isAfter(DateTimeHelper.getCurrentLocalDateTime()));
	}

}
