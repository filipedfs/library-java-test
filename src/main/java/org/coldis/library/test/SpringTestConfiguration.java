package org.coldis.library.test;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring test configuration.
 */
@Configuration
@PropertySource(
		value = { "classpath:default-test.properties" },
		ignoreResourceNotFound = true
)
public class SpringTestConfiguration {

}
