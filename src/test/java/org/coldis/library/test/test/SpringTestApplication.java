package org.coldis.library.test.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring test application. */
@SpringBootApplication(scanBasePackages = "org.coldis")
public class SpringTestApplication {

	/**
	 * Runs the application.
	 */
	public static void main(
			final String[] args) {
		new SpringApplication(SpringTestApplication.class).run(args);
	}

}
