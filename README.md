rxjava-slf4j
============
<a href="https://github.com/davidmoten/rxjava-slf4j/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/rxjava-slf4j/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/rxjava-slf4j-runtime/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/rxjava-slf4j)<br/>

Logging utilities for use with [RxJava](https://github.com/Netflix/RxJava) and [SLF4J](http://www.slf4j.org/) which bridges to the major popular logging frameworks.

Status: *released to Maven Central*

[Maven reports](http://davidmoten.github.io/rxjava-slf4j/) including [javadoc](http://davidmoten.github.io/rxjava-slf4j/apidocs/index.html).

As a quick example my favourite use case is to have a window into a fast moving stream of the number of items processesd and memory consumption:

```java
observable
    .lift(Logging.logger().showCount().showMemory().every(100000).log())
    .subscribe(subscriber);
```
Features
--------------
* ```source.lift(logger().cmd1().cmd2().. .log())``` is the pattern
* Don't need to make the static ```Logger``` declaration in each class (```private static final Logger log = LoggerFactory.getLogger(A.class);```)
* Convenient chained methods for specifying what things to log and at what level
* Log subscription and unsubscription (handy for checking your custom operator is doing the right thing)
* Log counts
* Log memory usage
* Avoid anonymous ```ActionN``` classes for logging (< java 8)
* Still convenient for java 8 despite the existence of lambdas

Getting started
-----------------
Add this to your pom.xml:
```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>rxjava-slf4j</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```

You will also need to add the slf4j library for your logging framework if you are not using slf4j already. For example if you are using log4j:

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>SLF4J_VERSION_HERE</version>
</dependency>
```

And you would need a ```log4j.properties``` on the classpath. [Here's one](src/test/resources/log4j.properties).

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

Note that the logger for the class is automatically set and that subscribe, unsubscribe and completed events are all logged (by default at different log levels).

Available builder methods 
---------------------------
The methods of ```logger()``` are listed [here](http://davidmoten.github.io/rxjava-slf4j/apidocs/com/github/davidmoten/rx/slf4j/Logging.Parameters.Builder.html).

Composition
-------------------------
Some methods on the ```logger()``` [builder](http://davidmoten.github.io/rxjava-slf4j/apidocs/com/github/davidmoten/rx/slf4j/Logging.Parameters.Builder.html). can be called repeatedly or in different orders with a compounding effect:

The methods are: 
* ```every```
* ```start```
* ```finish```
* ```showCount```
* ```when```
* ```onNext(Boolean)```
* ```onError(Boolean)```

###Repetition
This shows every 3000th value:
```java
logger().every(1000).every(3).log()
```

###Order
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

More examples
---------------
Here's a useful one for me. I want to count the total number of items and see the total number of items that pass a criterion. Let's count the number of integers divisible by 3 between 0 and 1000:

```java
Observable
    .range(1,1000)
    .lift(Logging.<Integer>logger()
                 .showCount("total")
                 .when( x -> x%3==0)
                 .showCount("divisibleBy3")
                 .onNext(false)
                 .log())
    .subscribe();
```

produces (abbreviated):
```
onCompleted, total=1000, divisibleBy3=333
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
            // show recent rate
            .showRateSince("rate",TimeUnit.SECONDS.toMillis(10))
            // show rate since start
            .showRateSinceStart("rate")
			// start on 2nd item
			.start(2)
			// ignore after 18th item
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
			.onNextFormat("time=%s days")
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
2014-06-11 10:31:33.431 [main] INFO  Boo - created subscription
2014-06-11 10:31:33.438 [main] DEBUG Boo - time=4 days, count=4, finalCount=1
2014-06-11 10:31:33.438 [main] DEBUG Boo - time=10 days, count=10, finalCount=2
2014-06-11 10:31:33.439 [main] DEBUG Boo - time=16 days, count=16, finalCount=3
2014-06-11 10:31:33.440 [main] INFO  Boo - finished, count=100, finalCount=3
2014-06-11 10:31:33.441 [main] DEBUG Boo - ended subscription
```
