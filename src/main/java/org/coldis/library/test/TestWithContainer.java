package org.coldis.library.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test with container.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestWithContainerExtension.class)
public @interface TestWithContainer {

	/**
	 * Whether containers should start in parallel. Defaults to {@code true}.
	 *
	 * @return if the containers should start in parallel.
	 */
	boolean parallel() default true;
}
