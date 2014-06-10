package com.github.davidmoten.rx.slf4j;

import static com.github.davidmoten.rx.slf4j.Logging.logger;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rx.Observable;

import com.github.davidmoten.rx.slf4j.Logging.Level;

public class LoggingTest {

	@Test
	public void testName() {
		int count = Observable.range(1, 10)
		// log all
				.lift(logger(LoggingTest.class).value().log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}

	@Test
	public void testCountEvery() {
		int count = Observable
				.range(1, 10)
				// log all
				.lift(logger().name("rx.Server").prefix("count every test")
						.exclude().subscribed(Level.DEBUG)
						.onCompleted(Level.DEBUG).count().every(2).log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}

	@Test
	public void testAgain() {
		int count = Observable.range(51, 10)
		// log all
				.lift(logger().prefix("again").every(5).count().memory().log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}

	@Test
	public void testCallingClass() {
		int count = Observable.range(1, 10)
		// log all
				.lift(logger().log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}
}
