package org.coldis.library.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.coldis.library.test.failfast.TestWithFailFast;
import org.coldis.library.test.retry.TestWithRetry;

@Inherited
@TestWithRetry
@TestWithFailFast
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestWithRetryAndFailFast {}
