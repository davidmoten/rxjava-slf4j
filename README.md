rxjava-slf4j
============

Logging utilities for use with RxJava and [SLF4J](http://www.slf4j.org/) which bridges to the major popular logging frameworks.

Status: *pre-alpha*

Getting started
-----------------
Add this to your pom.xml:
```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>rxjava-slf4j</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

You will also need to add the slf4j library for your logging framework if you are not using slf4j already. For example if you are using log4j:

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>1.7.7</version>
</dependency>
```

Example
-----------
To log every 1000th value in an observable, show the count, and show the memory usage at that point:

```java
import static com.github.davidmoten.rx.slf4j.Logging.*;

Observable.range(11,3011)
          .lift(logger().showValue().showCount().every(1000).showMemory().log())
          .subscribe();
```

This is the result:
```
2014-06-11 09:35:54.673 [main] DEBUG com.github.davidmoten.rx.slf4j.LoggingTest - onSubscribe
2014-06-11 09:35:54.685 [main] INFO  com.github.davidmoten.rx.slf4j.LoggingTest - 1010, count=1000, mem=10MB, percent=0.0
2014-06-11 09:35:54.694 [main] INFO  com.github.davidmoten.rx.slf4j.LoggingTest - 2010, count=2000, mem=11MB, percent=0.0
2014-06-11 09:35:54.705 [main] INFO  com.github.davidmoten.rx.slf4j.LoggingTest - 3010, count=3000, mem=11MB, percent=0.0
2014-06-11 09:35:54.705 [main] INFO  com.github.davidmoten.rx.slf4j.LoggingTest - onCompleted, count=3010, mem=11MB, percent=0.0
2014-06-11 09:35:54.705 [main] DEBUG com.github.davidmoten.rx.slf4j.LoggingTest - onUnsubscribe
```

Note that logger for the class is automatically specified and that subscribe, unsubscribe and completed events are all logged (by default at different log levels).

Repetition
-------------------------
Some methods on the ```logger()``` builder can be called repeatedly with a compounding effect:

This shows every 3000th value:
```java
logger().every(1000).every(3).log()
```

Order
----------------
Order of some methods called on the ```logger()``` builder is significant. For instance:

This 
```java
Observable.range(11,3011)
          .lift(logger().showValue().showCount().every(1000).log())
          .subscribe();
```
produces (output abbreviated for presentation here)
```
onSubscribe
1010, count=1000
2010, count=2000
3010, count=3000
onCompleted, count=3
onUnsubscribe
```
but with the order of ```every``` and ```showCount``` reversed:
```java
logger().showValue().every(1000).showCount().log()
```
produces (abbreviated)
```
onSubscribe
1010, count=1
2010, count=2
3010, count=3
onCompleted, count=3
onUnsubscribe
```

```java
logger().showValue().showCount().every(1000).log()
```
produces (abbreviated)
```
onSubscribe
1010, count=1000
2010, count=2000
3010, count=3000
onCompleted, count=3
onUnsubscribe
```

You can have both counts differentiated by specifying a label for the ```showCount``` method:

```java
logger().showValue().showCount().every(1000).showCount("inner").log()
```
produces (abbreviated)
```
onSubscribe
1010, count=1000, inner=1
2010, count=2000, inner=2
3010, count=3000, inner=3
onCompleted, count=3000, inner=3
onUnsubscribe
```

Kitchen Sink Example
---------------------------
This demos most stuff:

```java
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
			// show onNext items
			.showValue()
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
```
produces
```
2014-06-11 10:18:43.536 [main] INFO  Boo - created subscription
2014-06-11 10:18:43.537 [main] DEBUG Boo - 4, count=4,finalCount=1
2014-06-11 10:18:43.537 [main] DEBUG Boo - 10, count=10,finalCount=2
2014-06-11 10:18:43.537 [main] DEBUG Boo - 16, count=16,finalCount=3
2014-06-11 10:18:43.538 [main] INFO  Boo - onCompleted, count=100,finished,finalCount=3
2014-06-11 10:18:43.539 [main] DEBUG Boo - ended subscription
```
