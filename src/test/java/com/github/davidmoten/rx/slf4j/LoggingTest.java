package com.github.davidmoten.rx.slf4j;

import static com.github.davidmoten.rx.slf4j.Logging.logger;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.text.DecimalFormat;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.github.davidmoten.rx.slf4j.Logging.Level;

public class LoggingTest {

    @Test
    public void testName() {
        Logger logger = Mockito.mock(Logger.class);
        int count = Observable.range(1, 3)
        // log all
                .lift(logger(logger).showValue().log())
                // count
                .count().toBlocking().single();
        assertEquals(3, count);
        verify(logger).debug("onSubscribe");
        verify(logger).info("1");
        verify(logger).info("2");
        verify(logger).info("3");
        verify(logger).info("onCompleted");
        verify(logger).debug("onUnsubscribe");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void testDecimalFormat() {
        assertEquals("96.1", new DecimalFormat("0.0").format(96.1));
    }

    @Test
    public void testCountEvery() {
        Logger logger = Mockito.mock(Logger.class);
        // TODO because of mock rx.Server logger name not used
        int count = Observable
                .range(1, 6)
                // log all
                .lift(logger(logger).name("rx.Server").prefix("count every test").excludeValue()
                        .subscribed(Level.DEBUG).onCompleted(Level.DEBUG).showCount("files")
                        .every(2).log())
                // count
                .count().toBlocking().single();
        assertEquals(6, count);
        verify(logger).debug("onSubscribe");
        verify(logger).info("files=2");
        verify(logger).info("files=4");
        verify(logger).info("files=6");
        verify(logger).debug("onCompleted, files=6");
        verify(logger).debug("onUnsubscribe");
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void testAgain() {
        int count = Observable.range(51, 10)
        // log all
                .lift(logger().showCount().start(2).finish(2).showStackTrace().showMemory().log())
                // count
                .count().toBlocking().single();
        assertEquals(10, count);
    }

    @Test
    public void testSubscribe() {
        int count = Observable.range(11, 3010)
                .lift(logger().showValue().showCount().every(1000).showMemory().log()).count()
                .toBlocking().single();
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

    @Test
    public void testExample() {
        Observable
                .range(1, 1000)
                .lift(Logging.<Integer> logger().showCount("total")
                        .when(new Func1<Integer, Boolean>() {

                            @Override
                            public Boolean call(Integer n) {
                                return n % 3 == 0;
                            }
                        }).showCount("divisbleBy3").onNext(false).log()).subscribe();
    }

    private static final Logger log = LoggerFactory.getLogger(LoggingTest.class);

    @Test
    public void testExample2() {
        Observable.range(1, 100).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer x) {
                return x * x + 1;
            }
        }).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer x) {
                log.info("n1=" + x);
            }
        }).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer x) {
                return x % 3 == 0;
            }
        }).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer x) {
                log.info("n1=" + x);
            }
        }).subscribe();
    }

    @Test
    public void testExample2UsingLibrary() {
        Observable
        // range
                .range(1, 100)
                // map
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer x) {
                        return x * x + 1;
                    }
                })
                // log
                .lift(Logging.<Integer> log("n2=%s"))
                // filter
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer x) {
                        return x % 5 == 0;
                    }
                })
                // log
                .lift(Logging.log("n3=%s"))
                // run
                .subscribe();
    }

    @Test
    public void testKitchenSink() {
        Observable.range(1, 100)
        // log
                .lift(Logging.<Integer> logger("Boo")
                // count
                        .showCount()
                        // start on 2nd item
                        .start(2)
                        // ignore after 8th item
                        .finish(18)
                        // take every third item
                        .every(3)
                        // set the onCompleted message
                        .onCompleted("finished")
                        // at logging level
                        .onCompleted(Level.INFO)
                        // set the error logging level
                        .onError(Level.WARN)
                        // onNext at debug level
                        .onNext(Level.DEBUG)
                        // how to format the onNext item
                        .onNextFormat("time=%sdays")
                        // show subscribed message at INFO level
                        .subscribed(Level.INFO)
                        // the message to show at subscription time
                        .subscribed("created subscription")
                        // the unsubscribe message at DEBUG level
                        .unsubscribed(Level.DEBUG)
                        // the unsubscribe message
                        .unsubscribed("ended subscription")
                        // only when item is an even number
                        .when(new Func1<Integer, Boolean>() {
                            @Override
                            public Boolean call(Integer n) {
                                return n % 2 == 0;
                            }
                        })
                        // count those items passing the filters above
                        .showCount("finalCount")
                        // build the operator
                        .log())
                // block and get the answer
                .toBlocking().last();
    }
}
