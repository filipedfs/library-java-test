package org.coldis.library.test;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Test helper.
 */
public class TestHelper {

	/**
	 * Short wait time (milliseconds).
	 */
	public static final Integer SHORT_WAIT = 500;

	/**
	 * Regular wait time (milliseconds).
	 */
	public static final Integer REGULAR_WAIT = 25 * 100;

	/**
	 * Long wait time (milliseconds).
	 */
	public static final Integer LONG_WAIT = 11 * 1000;

	/**
	 * Long wait time (milliseconds).
	 */
	public static final Integer VERY_LONG_WAIT = 29 * 1000;

	/**
	 * Waits until variable is valid.
	 *
	 * @param                     <Type> The variable type.
	 * @param  variableSupplier   Variable supplier function.
	 * @param  validVariableState The variable valid state verification.
	 * @param  maxWait            Milliseconds to wait until valid state is met.
	 * @param  poll               Milliseconds between validity verification.
	 * @param  exceptionsToIgnore Exceptions to be ignored on validity verification.
	 * @return                    If a valid variable state has been met within the
	 *                            maximum wait period.
	 * @throws Exception          If the validity verification throws a non
	 *                                ignorable exception.
	 */
	@SafeVarargs
	public static <Type> Boolean waitUntilValid(final Supplier<Type> variableSupplier,
			final Predicate<Type> validVariableState, final Integer maxWait, final Integer poll,
			final Class<? extends Throwable>... exceptionsToIgnore) throws Exception {
		// Valid state is not considered met by default.
		Boolean validStateMet = false;
		// Validation start time stamp.
		final Long startTimestamp = System.currentTimeMillis();
		// Until wait time is not reached.
		for (Long currentTimestamp = System.currentTimeMillis(); (startTimestamp
				+ maxWait) > currentTimestamp; currentTimestamp = System.currentTimeMillis()) {
			// If the variable state is valid.
			try {
				if (validVariableState.test(variableSupplier.get())) {
					// Valid state has been met.
					validStateMet = true;
					break;
				}
			}
			// If the variable state cannot be tested.
			catch (final Throwable throwable) {
				// If the exception is not to be ignored.
				if ((exceptionsToIgnore == null) || !Arrays.asList(exceptionsToIgnore).stream()
						.anyMatch(exception -> exception.isAssignableFrom(throwable.getClass()))) {
					// Throws the exception and stops the wait.
					throw throwable;
				}
			}
			// Waits a bit.
			Thread.sleep(poll);
		}
		// Returns if valid state has been met.
		return validStateMet;
	}

}
