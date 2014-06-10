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
				.lift(logger(LoggingTest.class).showValue().log())
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
						.excludeValue().subscribed(Level.DEBUG)
						.onCompleted(Level.DEBUG).showCount("files").every(2)
						.log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}

	@Test
	public void testAgain() {
		int count = Observable
				.range(51, 10)
				// log all
				.lift(logger().showCount().start(2).finish(2).showStackTrace()
						.showMemory().log())
				// count
				.count().toBlocking().single();
		assertEquals(10, count);
	}

	@Test
	public void testSubscribe() {
		int count = Observable
				.range(11, 3010)
				.lift(logger().showValue().showCount().every(1000).showMemory()
						.log()).count().toBlocking().single();
		assertEquals(3010, count);

	}

	@Test
	public void testCallingClass() {

		assertEquals(10, new CallingClass().count());
	}

	private static class CallingClass {
		public int count() {
			return Observable.range(1, 10)
			// log all
					.lift(logger().showValue().log())
					// count
					.count().toBlocking().single();
		}
	}
}
