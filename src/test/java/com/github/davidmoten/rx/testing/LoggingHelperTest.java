package com.github.davidmoten.rx.testing;

import com.github.davidmoten.rx.slf4j.Logging;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import rx.Observable;
import rx.functions.Func1;

public class LoggingHelperTest extends TestCase {

    public static TestSuite suite() {
        return TestingHelper.function(LOGGER)
        // test logger
                .name("testLogger").from(1, 2, 3).expect(1, 2, 3)
                // test empty
                .name("testLoggerOnEmptyStream").fromEmpty().expectEmpty()
                // test error
                .name("testLoggerOnErrorStream").fromError().expectError()
                // get test suites
                .testSuite(LoggingHelperTest.class);
    }

    public void testDummy() {
        // just here to fool eclipse
    }

    private static final Func1<Observable<Integer>, Observable<Integer>> LOGGER = new Func1<Observable<Integer>, Observable<Integer>>() {
        @Override
        public Observable<Integer> call(Observable<Integer> o) {
            return o.lift(Logging.<Integer> logger().showValue().log());
        }
    };

}
