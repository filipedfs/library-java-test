package org.coldis.library.test.test;

import org.coldis.library.exception.BusinessException;
import org.coldis.library.exception.IntegrationException;
import org.coldis.library.test.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test helper test.
 */
public class TestHelperTest {

	/**
	 * Test variable.
	 */
	private Integer testVar;

	/**
	 * Tests a not valid variable state.
	 *
	 * @throws Exception If the test fails.
	 */
	@Test
	public void test00WaitUntilValidFailed() throws Exception {
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
	public void test01WaitUntilValidFailedWithIgnorableExceptions() throws Exception {
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
	public void test02WaitUntilValidFailedWithNonIgnorableExceptions() throws Exception {
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
	public void test03WaitUntilValidSucceeded() throws Exception {
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
	public void test04WaitUntilValidSucceededWithIgnorableExceptions() throws Exception {
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

}